package com.sakura.ms0020.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.ms0020.domain.Ms0020FieldAccess;
import com.sakura.ms0020.runtime.Ms0020Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.ScreenModels;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.BankfDataset;
import com.sakura.runtime.io.SuppfDataset;
import com.sakura.runtime.linkage.DateutLinkParm;
import com.sakura.runtime.record.RawDatasetBase;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link Ms0020Service}, derived from COBOL program MS0020 (supplier master
 * maintenance). Ground truth for inputs/expected values: MS0020.cob PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Ms0020ServiceTest {

    @Spy private SuppfDataset suppfSpy = new SuppfDataset();
    @Spy private BankfDataset bankfSpy = new BankfDataset();

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;
    @Mock private ScreenRendererInstance renderer;

    private Ms0020Service service;
    private Ms0020FieldAccess ws;

    private final List<String> screenInteractions = new ArrayList<>();
    private final Map<String, String> acceptFieldValues = new HashMap<>();

    /** Subclass swapping in the spy datasets since Ms0020Datasets builds its own real files. */
    private static class TestDatasets extends Ms0020Datasets {
        private final SuppfDataset suppf;
        private final BankfDataset bankf;

        TestDatasets(SuppfDataset suppf, BankfDataset bankf) {
            this.suppf = suppf;
            this.bankf = bankf;
        }

        @Override
        public SuppfDataset getSuppf() {
            return suppf;
        }

        @Override
        public BankfDataset getBankf() {
            return bankf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Ms0020Datasets fileSet = new TestDatasets(suppfSpy, bankfSpy);
        service = new Ms0020Service(fileSet, dateutService, abortxService, renderer);
        service.setRenderer(renderer);
        ws = getWs();

        for (RawDatasetBase spy : new RawDatasetBase[] {suppfSpy, bankfSpy}) {
            doNothing().when(spy).open(any());
            doNothing().when(spy).close();
            doNothing().when(spy).write();
            doNothing().when(spy).rewrite();
            doReturn("00").when(spy).getFileStatus();
            doReturn(false).when(spy).isInvalidKey();
            doReturn(true).when(spy).readByKey(any());
        }

        doAnswer(
                        inv -> {
                            ScreenModels.ScreenDef def = inv.getArgument(0);
                            screenInteractions.add("displayScreen:" + def.name);
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        doAnswer(
                        inv -> {
                            ScreenModels.InputFieldDef field = inv.getArgument(0);
                            return acceptFieldValues.getOrDefault(field.name, "");
                        })
                .when(renderer)
                .acceptField(any());

        doReturn("00").when(renderer).readEndStatus();

        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            p.getKdate().setKdStatus("00");
                            p.getKdate().setKdDate1(20260918);
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        doAnswer(inv -> null).when(abortxService).execute(any());
    }

    @AfterEach
    void tearDown() {
        screenInteractions.clear();
        acceptFieldValues.clear();
    }

    /* ── reflection helpers ── */

    private Ms0020FieldAccess getWs() throws Exception {
        Field f = Ms0020Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Ms0020FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Ms0020Service.class.getDeclaredMethod(name);
            m.setAccessible(true);
            m.invoke(service);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(cause);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeProtectedWithArg(String name, Class<?> paramType, Object arg) {
        try {
            Method m = Ms0020Service.class.getDeclaredMethod(name, paramType);
            m.setAccessible(true);
            m.invoke(service, arg);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /* ── mainProcess / getCompletionCode / setCompletionCode / getFileSet ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void getCompletionCode_delegatesToWorkingStorage() {
        ws.setCompletionCode(42);
        assertEquals(42, service.getCompletionCode());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void setCompletionCode_delegatesToWorkingStorage() {
        invokeProtectedWithArg("setCompletionCode", int.class, 77);
        assertEquals(77, ws.getCompletionCode());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void getFileSet_returnsRegisteredDatasets() throws Exception {
        Method m = Ms0020Service.class.getDeclaredMethod("getFileSet");
        m.setAccessible(true);
        Object result = m.invoke(service);
        assertTrue(result instanceof Ms0020Datasets);
    }

    /* ── INIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_success_setsHeaderFieldsAndOpensAllFiles() {
        invokePrivate("initializeProgram");

        assertEquals("MS0020", ws.getWkProgid());
        assertEquals("Supplier Master Maintenance", ws.getWkTitle().trim());
        assertEquals("ENTER=Read  PF9=Delete  PF3=End", ws.getWkFkeyLine().trim());
        assertEquals(20260918, ws.getWkSysymd());
        assertEquals(20260918, ws.getWkSysdate());
        verify(suppfSpy).open(FileOpenMode.IO);
        verify(bankfSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_suppfStatus35_reopensAsOutputThenIO() {
        doReturn("35", "00", "00", "00").when(suppfSpy).getFileStatus();

        invokePrivate("initializeProgram");

        verify(suppfSpy, times(2)).open(FileOpenMode.IO);
        verify(suppfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(suppfSpy, times(1)).close();
        verify(bankfSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_suppfStatus30_reopensAsOutputThenIO() {
        doReturn("30", "00", "00", "00").when(suppfSpy).getFileStatus();

        invokePrivate("initializeProgram");

        verify(suppfSpy, times(2)).open(FileOpenMode.IO);
        verify(suppfSpy, times(1)).open(FileOpenMode.OUTPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_suppfErrorStatus_abortsAndSkipsBankf() {
        doReturn("23").when(suppfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeProgram"));

        assertEquals("SUPPF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(bankfSpy, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_bankfErrorStatus_aborts() {
        doReturn("00").when(suppfSpy).getFileStatus();
        doReturn("23").when(bankfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeProgram"));

        assertEquals("BANKF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── ABEND-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abortOnFileOpenError_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortOnFileOpenError"));

        assertEquals("MS0020", ws.getKaProgid());
        assertEquals("23", ws.getKaFsts().trim());
        assertEquals("EOPEN ", ws.getKaMsgcode());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── MAIN-000 loop ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_immediateEnd_closesAllFilesAndThrowsProgramExitSignal() {
        doReturn("03").when(renderer).readEndStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

        assertEquals(1, ws.getEndFlg());
        verify(suppfSpy).close();
        verify(bankfSpy).close();
    }

    /* ── MAIN-RTN-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processMainScreenInput_ests03_setsEndFlg() {
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processMainScreenInput");

        assertEquals(1, ws.getEndFlg());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processMainScreenInput_estsOther_showsInvalidKeyMessage() {
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("processMainScreenInput");

        assertEquals(0, ws.getEndFlg());
        assertEquals("Invalid function key", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processMainScreenInput_ests00_spCodeZero_showsMustNotBeZeroMessage() {
        acceptFieldValues.put("SC-KEY", "0");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("processMainScreenInput");

        assertEquals("Supplier code must not be zero", ws.getWkMsgLine().trim());
        verify(suppfSpy, never()).readByKey(any());
    }

    /* ── CLEAR-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearSupplierWorkFields_resetsWorkFieldsToDefaults() {
        ws.setWkBankName("dirty");
        ws.setWkConfirm("Y");

        invokePrivate("clearSupplierWorkFields");

        verify(suppfSpy).setRecord();
        assertEquals("", ws.getWkBankName().trim());
        assertEquals("", ws.getWkConfirm().trim());
    }

    /* ── PKEY-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processSupplierKeyEntry_spCodeZero_showsMessageAndReturnsWithoutRead() {
        ws.setSpCode(0);

        invokePrivate("processSupplierKeyEntry");

        assertEquals("Supplier code must not be zero", ws.getWkMsgLine().trim());
        verify(suppfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processSupplierKeyEntry_suppfInvalidKey_setsUpAddMode() {
        ws.setSpCode(100);
        doReturn(true).when(suppfSpy).isInvalidKey();
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processSupplierKeyEntry");

        assertEquals(1, ws.getModeFlg());
        assertEquals(100, ws.getSpCode());
        assertEquals("New supplier - enter details", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processSupplierKeyEntry_suppfValidKey_setsUpChangeMode() {
        ws.setSpCode(100);
        ws.setSpDelFlag(0);
        doReturn(false).when(suppfSpy).isInvalidKey();
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processSupplierKeyEntry");

        assertEquals(2, ws.getModeFlg());
        assertEquals("Existing supplier - change or PF9 delete", ws.getWkMsgLine().trim());
    }

    /* ── SADD-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareNewSupplierRecord_setsModeAddCodeAndMessage() {
        ws.setWkSaveCode(555);

        invokePrivate("prepareNewSupplierRecord");

        assertEquals(1, ws.getModeFlg());
        assertEquals(555, ws.getSpCode());
        assertEquals("New supplier - enter details", ws.getWkMsgLine().trim());
        verify(suppfSpy).setRecord();
    }

    /* ── SCHG-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareExistingSupplierRecord_deletedSupplier_setsModeAddAndReregisterMessage() {
        ws.setSpDelFlag(1);

        invokePrivate("prepareExistingSupplierRecord");

        assertEquals(1, ws.getModeFlg());
        assertEquals("Deleted supplier - re-registering", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareExistingSupplierRecord_notDeleted_setsModeChgAndChangeMessage() {
        ws.setSpDelFlag(0);

        invokePrivate("prepareExistingSupplierRecord");

        assertEquals(2, ws.getModeFlg());
        assertEquals("Existing supplier - change or PF9 delete", ws.getWkMsgLine().trim());
    }

    /* ── EDIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptSupplierDetailFields_ests03_acceptsFieldsAndContinues() {
        acceptFieldValues.put("SP-NAME", "Acme Supplies");
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("acceptSupplierDetailFields");

        assertEquals("Acme Supplies", ws.getSpName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptSupplierDetailFields_ests04_showsCancelledMessage() {
        doReturn("04").when(renderer).readEndStatus();

        invokePrivate("acceptSupplierDetailFields");

        assertEquals("Cancelled", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptSupplierDetailFields_ests09_modeChg_callsDeleteWithConfirm() {
        ws.setModeFlg(2);
        acceptFieldValues.put("SC-CONF", "N");
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("acceptSupplierDetailFields");

        assertEquals("Delete cancelled", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptSupplierDetailFields_ests09_modeAdd_showsNothingToDeleteMessage() {
        ws.setModeFlg(1);
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("acceptSupplierDetailFields");

        assertEquals("Nothing to delete", ws.getWkMsgLine().trim());
        assertFalse(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptSupplierDetailFields_ests00_validationFails_doesNotSave() {
        acceptFieldValues.put("SP-NAME", "");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("acceptSupplierDetailFields");

        assertEquals(1, ws.getErrFlg());
        verify(suppfSpy, never()).write();
        verify(suppfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptSupplierDetailFields_ests00_validationPasses_savesAddedSupplier() {
        ws.setModeFlg(1);
        acceptFieldValues.put("SP-NAME", "Acme Supplies");
        acceptFieldValues.put("SP-CLOSE-DAY", "99");
        acceptFieldValues.put("SP-PAY-DAY", "99");
        acceptFieldValues.put("SP-PAY-METHOD", "1");
        acceptFieldValues.put("SP-TAX-TYPE", "1");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("acceptSupplierDetailFields");

        assertEquals(0, ws.getErrFlg());
        verify(suppfSpy).write();
        assertEquals("Supplier added", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptSupplierDetailFields_estsOther_showsInvalidKeyMessage() {
        doReturn("07").when(renderer).readEndStatus();

        invokePrivate("acceptSupplierDetailFields");

        assertEquals("Invalid function key", ws.getWkMsgLine().trim());
    }

    /* ── VAL-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateSupplierFields_blankName_setsErrorAndReturns() {
        ws.setSpName(" ");

        invokePrivate("validateSupplierFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Name is required", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateSupplierFields_closeDayBelowRange_setsError() {
        ws.setSpName("Acme Supplies");
        ws.setSpCloseDay(0);

        invokePrivate("validateSupplierFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Closing day must be 1-31 or 99", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateSupplierFields_closeDayAboveRange_setsError() {
        ws.setSpName("Acme Supplies");
        ws.setSpCloseDay(32);

        invokePrivate("validateSupplierFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Closing day must be 1-31 or 99", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateSupplierFields_payDayOutOfRange_setsError() {
        ws.setSpName("Acme Supplies");
        ws.setSpCloseDay(99);
        ws.setSpPayDay(0);

        invokePrivate("validateSupplierFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Pay day must be 1-31 or 99", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateSupplierFields_payMethodOutOfRange_setsError() {
        ws.setSpName("Acme Supplies");
        ws.setSpCloseDay(99);
        ws.setSpPayDay(99);
        ws.setSpPayMethod(0);

        invokePrivate("validateSupplierFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Pay method must be 1-3", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateSupplierFields_taxTypeOutOfRange_setsError() {
        ws.setSpName("Acme Supplies");
        ws.setSpCloseDay(99);
        ws.setSpPayDay(99);
        ws.setSpPayMethod(1);
        ws.setSpTaxType(0);

        invokePrivate("validateSupplierFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Tax type must be 1-3", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateSupplierFields_bankNotFound_setsError() {
        ws.setSpName("Acme Supplies");
        ws.setSpCloseDay(99);
        ws.setSpPayDay(99);
        ws.setSpPayMethod(1);
        ws.setSpTaxType(1);
        ws.setSpBankCode(30);
        doReturn(true).when(bankfSpy).isInvalidKey();

        invokePrivate("validateSupplierFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Bank code not found", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateSupplierFields_bankFound_setsBankNameAndNoError() {
        ws.setSpName("Acme Supplies");
        ws.setSpCloseDay(99);
        ws.setSpPayDay(99);
        ws.setSpPayMethod(1);
        ws.setSpTaxType(1);
        ws.setSpBankCode(30);
        ws.setBkName("Bank of Sakura");
        doReturn(false).when(bankfSpy).isInvalidKey();

        invokePrivate("validateSupplierFields");

        assertEquals(0, ws.getErrFlg());
        assertEquals("Bank of Sakura", ws.getWkBankName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateSupplierFields_allValidBankCodeZero_noError() {
        ws.setSpName("Acme Supplies");
        ws.setSpCloseDay(99);
        ws.setSpPayDay(99);
        ws.setSpPayMethod(1);
        ws.setSpTaxType(1);
        ws.setSpBankCode(0);

        invokePrivate("validateSupplierFields");

        assertEquals(0, ws.getErrFlg());
        verify(bankfSpy, never()).readByKey(any());
    }

    /* ── VAL-999 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void showValidationMessageIfError_errFlgSet_displaysMessage() {
        ws.setErrFlg(1);

        invokePrivate("showValidationMessageIfError");

        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void showValidationMessageIfError_errFlgNotSet_noDisplay() {
        ws.setErrFlg(0);

        invokePrivate("showValidationMessageIfError");

        assertFalse(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    /* ── SAVE-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveSupplierRecord_modeAdd_setsAddDateUserAndWrites() {
        ws.setModeFlg(1);
        ws.setWkSysdate(20260918);
        ws.setWkUserCode(7);

        invokePrivate("saveSupplierRecord");

        assertEquals(20260918, ws.getSpAddDate());
        assertEquals(7, ws.getSpAddUser());
        assertEquals(20260918, ws.getSpUpdDate());
        assertEquals(7, ws.getSpUpdUser());
        assertEquals(0, ws.getSpDelFlag());
        verify(suppfSpy).write();
        assertEquals("Supplier added", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveSupplierRecord_modeAdd_writeInvalidKey_showsDuplicateMessage() {
        ws.setModeFlg(1);
        doReturn(true).when(suppfSpy).isInvalidKey();

        invokePrivate("saveSupplierRecord");

        assertEquals("Write failed - duplicate", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveSupplierRecord_modeChg_rewriteSuccess_showsUpdatedMessage() {
        ws.setModeFlg(2);

        invokePrivate("saveSupplierRecord");

        verify(suppfSpy).rewrite();
        assertEquals("Supplier updated", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveSupplierRecord_modeChg_rewriteInvalidKey_showsUpdateFailedMessage() {
        ws.setModeFlg(2);
        doReturn(true).when(suppfSpy).isInvalidKey();

        invokePrivate("saveSupplierRecord");

        assertEquals("Update failed", ws.getWkMsgLine().trim());
    }

    /* ── DEL-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteSupplierWithConfirmation_confirmY_deletesAndRewrites() {
        acceptFieldValues.put("SC-CONF", "Y");
        ws.setWkSysdate(20260918);
        ws.setWkUserCode(9);

        invokePrivate("deleteSupplierWithConfirmation");

        assertEquals(1, ws.getSpDelFlag());
        assertEquals(20260918, ws.getSpUpdDate());
        assertEquals(9, ws.getSpUpdUser());
        verify(suppfSpy).rewrite();
        assertEquals("Supplier deleted", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteSupplierWithConfirmation_confirmLowercaseY_deletesAndRewrites() {
        acceptFieldValues.put("SC-CONF", "y");

        invokePrivate("deleteSupplierWithConfirmation");

        assertEquals(1, ws.getSpDelFlag());
        verify(suppfSpy).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteSupplierWithConfirmation_confirmN_cancelsWithoutRewrite() {
        acceptFieldValues.put("SC-CONF", "N");

        invokePrivate("deleteSupplierWithConfirmation");

        assertEquals("Delete cancelled", ws.getWkMsgLine().trim());
        verify(suppfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteSupplierWithConfirmation_confirmYRewriteInvalidKey_showsDeleteFailed() {
        acceptFieldValues.put("SC-CONF", "Y");
        doReturn(true).when(suppfSpy).isInvalidKey();

        invokePrivate("deleteSupplierWithConfirmation");

        assertEquals("Delete failed", ws.getWkMsgLine().trim());
    }

    /* ── LOOK-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupBankName_codeZero_blanksName() {
        ws.setSpBankCode(0);

        invokePrivate("lookupBankName");

        assertEquals("", ws.getWkBankName().trim());
        verify(bankfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupBankName_found_setsName() {
        ws.setSpBankCode(30);
        ws.setBkName("Bank of Sakura");
        doReturn(false).when(bankfSpy).isInvalidKey();

        invokePrivate("lookupBankName");

        assertEquals("Bank of Sakura", ws.getWkBankName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupBankName_notFound_setsUnknownName() {
        ws.setSpBankCode(30);
        doReturn(true).when(bankfSpy).isInvalidKey();

        invokePrivate("lookupBankName");

        assertEquals("??? unknown bank", ws.getWkBankName().trim());
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void closeAllFiles_closesBothFiles() {
        invokePrivate("closeAllFiles");

        verify(suppfSpy).close();
        verify(bankfSpy).close();
    }
}

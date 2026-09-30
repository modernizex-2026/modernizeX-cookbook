package com.sakura.ms0010.service;

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
import com.sakura.ms0010.domain.Ms0010FieldAccess;
import com.sakura.ms0010.runtime.Ms0010Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.ScreenModels;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.BankfDataset;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.RegnfDataset;
import com.sakura.runtime.io.StaffDataset;
import com.sakura.runtime.linkage.AbortxLinkParm;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link Ms0010Service}, derived from COBOL program MS0010 (customer master
 * maintenance). Ground truth for inputs/expected values: MS0010.cob PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Ms0010ServiceTest {

    @Spy private CustfDataset custfSpy = new CustfDataset();
    @Spy private RegnfDataset regnfSpy = new RegnfDataset();
    @Spy private StaffDataset staffSpy = new StaffDataset();
    @Spy private BankfDataset bankfSpy = new BankfDataset();

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;
    @Mock private ScreenRendererInstance renderer;

    private Ms0010Service service;
    private Ms0010FieldAccess ws;

    private final List<String> screenInteractions = new ArrayList<>();
    private final Map<String, String> acceptFieldValues = new HashMap<>();

    /** Subclass swapping in the spy datasets since Ms0010Datasets builds its own real files. */
    private static class TestDatasets extends Ms0010Datasets {
        private final CustfDataset custf;
        private final RegnfDataset regnf;
        private final StaffDataset staff;
        private final BankfDataset bankf;

        TestDatasets(
                CustfDataset custf, RegnfDataset regnf, StaffDataset staff, BankfDataset bankf) {
            this.custf = custf;
            this.regnf = regnf;
            this.staff = staff;
            this.bankf = bankf;
        }

        @Override
        public CustfDataset getCustf() {
            return custf;
        }

        @Override
        public RegnfDataset getRegnf() {
            return regnf;
        }

        @Override
        public StaffDataset getStaff() {
            return staff;
        }

        @Override
        public BankfDataset getBankf() {
            return bankf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Ms0010Datasets fileSet = new TestDatasets(custfSpy, regnfSpy, staffSpy, bankfSpy);
        service = new Ms0010Service(fileSet, dateutService, abortxService, renderer);
        service.setRenderer(renderer);
        ws = getWs();

        for (RawDatasetBase spy : new RawDatasetBase[] {custfSpy, regnfSpy, staffSpy, bankfSpy}) {
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

        doAnswer(
                        inv -> {
                            AbortxLinkParm p = inv.getArgument(0);
                            return null;
                        })
                .when(abortxService)
                .execute(any());
    }

    @AfterEach
    void tearDown() {
        screenInteractions.clear();
        acceptFieldValues.clear();
    }

    /* ── reflection helpers ── */

    private Ms0010FieldAccess getWs() throws Exception {
        Field f = Ms0010Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Ms0010FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Ms0010Service.class.getDeclaredMethod(name);
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
        Method m = Ms0010Service.class.getDeclaredMethod("getFileSet");
        m.setAccessible(true);
        Object result = m.invoke(service);
        assertTrue(result instanceof Ms0010Datasets);
    }

    private void invokeProtectedWithArg(String name, Class<?> paramType, Object arg) {
        try {
            Method m = Ms0010Service.class.getDeclaredMethod(name, paramType);
            m.setAccessible(true);
            m.invoke(service, arg);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /* ── INIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_success_setsHeaderFieldsAndOpensAllFiles() {
        invokePrivate("initializeProgram");

        assertEquals("MS0010", ws.getWkProgid());
        assertEquals("Customer Master Maintenance", ws.getWkTitle().trim());
        assertEquals("ENTER=Read  PF9=Delete  PF3=End", ws.getWkFkeyLine().trim());
        assertEquals(20260918, ws.getWkSysymd());
        assertEquals(20260918, ws.getWkSysdate());
        verify(custfSpy).open(FileOpenMode.IO);
        verify(regnfSpy).open(FileOpenMode.INPUT);
        verify(staffSpy).open(FileOpenMode.INPUT);
        verify(bankfSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_custfStatus35_reopensAsOutputThenIO() {
        doReturn("35", "00", "00", "00").when(custfSpy).getFileStatus();

        invokePrivate("initializeProgram");

        verify(custfSpy, times(2)).open(FileOpenMode.IO);
        verify(custfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(custfSpy, times(1)).close();
        verify(regnfSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_custfStatus30_reopensAsOutputThenIO() {
        doReturn("30", "00", "00", "00").when(custfSpy).getFileStatus();

        invokePrivate("initializeProgram");

        verify(custfSpy, times(2)).open(FileOpenMode.IO);
        verify(custfSpy, times(1)).open(FileOpenMode.OUTPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_custfErrorStatus_abortsAndSkipsRemainingFiles() {
        doReturn("23").when(custfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeProgram"));

        assertEquals("CUSTF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(regnfSpy, never()).open(any());
        verify(staffSpy, never()).open(any());
        verify(bankfSpy, never()).open(any());
    }

    /* ── ABEND-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abortOnFileOpenError_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortOnFileOpenError"));

        assertEquals("MS0010", ws.getKaProgid());
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
        verify(custfSpy).close();
        verify(regnfSpy).close();
        verify(staffSpy).close();
        verify(bankfSpy).close();
    }

    /* ── MAIN-RTN-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processMainScreenCycle_ests03_setsEndFlg() {
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processMainScreenCycle");

        assertEquals(1, ws.getEndFlg());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processMainScreenCycle_estsOther_showsInvalidKeyMessage() {
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("processMainScreenCycle");

        assertEquals(0, ws.getEndFlg());
        assertEquals("Invalid function key", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processMainScreenCycle_ests00_cuCodeZero_showsMustNotBeZeroMessage() {
        acceptFieldValues.put("SC-KEY", "0");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("processMainScreenCycle");

        assertEquals("Customer code must not be zero", ws.getWkMsgLine().trim());
        verify(custfSpy, never()).readByKey(any());
    }

    /* ── CLEAR-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearCustomerWorkFields_resetsWorkFieldsToDefaults() {
        ws.setWkRegionName("dirty");
        ws.setWkStaffName("dirty");
        ws.setWkBankName("dirty");
        ws.setWkConfirm("Y");

        invokePrivate("clearCustomerWorkFields");

        verify(custfSpy).setRecord();
        assertEquals(
                " ", ws.getWkRegionName().trim().isEmpty() ? " " : ws.getWkRegionName().trim());
        assertEquals(" ", ws.getWkStaffName().trim().isEmpty() ? " " : ws.getWkStaffName().trim());
        assertEquals(" ", ws.getWkBankName().trim().isEmpty() ? " " : ws.getWkBankName().trim());
        assertEquals(" ", ws.getWkConfirm().trim().isEmpty() ? " " : ws.getWkConfirm().trim());
    }

    /* ── PKEY-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupCustomerByCode_cuCodeZero_showsMessageAndReturnsWithoutRead() {
        ws.setCuCode(0);

        invokePrivate("lookupCustomerByCode");

        assertEquals("Customer code must not be zero", ws.getWkMsgLine().trim());
        verify(custfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupCustomerByCode_custfInvalidKey_setsUpAddMode() {
        ws.setCuCode(100);
        doReturn(true).when(custfSpy).isInvalidKey();
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("lookupCustomerByCode");

        assertEquals(1, ws.getModeFlg());
        assertEquals(100, ws.getCuCode());
        assertEquals("New customer - enter details", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupCustomerByCode_custfValidKey_setsUpChangeMode() {
        ws.setCuCode(100);
        ws.setCuDelFlag(0);
        doReturn(false).when(custfSpy).isInvalidKey();
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("lookupCustomerByCode");

        assertEquals(2, ws.getModeFlg());
        assertEquals("Existing customer - change or PF9 delete", ws.getWkMsgLine().trim());
    }

    /* ── SADD-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareNewCustomerAdd_setsModeAddCodeAndMessage() {
        ws.setWkSaveCode(555);

        invokePrivate("prepareNewCustomerAdd");

        assertEquals(1, ws.getModeFlg());
        assertEquals(555, ws.getCuCode());
        assertEquals("New customer - enter details", ws.getWkMsgLine().trim());
        verify(custfSpy).setRecord();
    }

    /* ── SCHG-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareCustomerChange_deletedCustomer_setsModeAddAndReregisterMessage() {
        ws.setCuDelFlag(1);

        invokePrivate("prepareCustomerChange");

        assertEquals(1, ws.getModeFlg());
        assertEquals("Deleted customer - re-registering", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareCustomerChange_notDeleted_setsModeChgAndChangeMessage() {
        ws.setCuDelFlag(0);

        invokePrivate("prepareCustomerChange");

        assertEquals(2, ws.getModeFlg());
        assertEquals("Existing customer - change or PF9 delete", ws.getWkMsgLine().trim());
    }

    /* ── EDIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptCustomerDetailFields_ests03_acceptsFieldsAndContinues() {
        acceptFieldValues.put("CU-NAME", "Acme Corp");
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("acceptCustomerDetailFields");

        assertEquals("Acme Corp", ws.getCuName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptCustomerDetailFields_ests04_showsCancelledMessage() {
        doReturn("04").when(renderer).readEndStatus();

        invokePrivate("acceptCustomerDetailFields");

        assertEquals("Cancelled", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptCustomerDetailFields_ests09_modeChg_callsDeleteWithConfirm() {
        ws.setModeFlg(2);
        acceptFieldValues.put("SC-CONF", "N");
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("acceptCustomerDetailFields");

        assertEquals("Delete cancelled", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptCustomerDetailFields_ests09_modeAdd_showsNothingToDeleteMessage() {
        ws.setModeFlg(1);
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("acceptCustomerDetailFields");

        assertEquals("Nothing to delete", ws.getWkMsgLine().trim());
        assertFalse(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptCustomerDetailFields_ests00_validationFails_doesNotSave() {
        acceptFieldValues.put("CU-NAME", "");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("acceptCustomerDetailFields");

        assertEquals(1, ws.getErrFlg());
        verify(custfSpy, never()).write();
        verify(custfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptCustomerDetailFields_ests00_validationPasses_savesAddedCustomer() {
        ws.setModeFlg(1);
        acceptFieldValues.put("CU-NAME", "Acme Corp");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("acceptCustomerDetailFields");

        assertEquals(0, ws.getErrFlg());
        verify(custfSpy).write();
        assertEquals("Customer added", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptCustomerDetailFields_estsOther_showsInvalidKeyMessage() {
        doReturn("07").when(renderer).readEndStatus();

        invokePrivate("acceptCustomerDetailFields");

        assertEquals("Invalid function key", ws.getWkMsgLine().trim());
    }

    /* ── VAL-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateCustomerFields_blankName_setsErrorAndReturns() {
        ws.setCuName(" ");

        invokePrivate("validateCustomerFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Name is required", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateCustomerFields_regionNotFound_setsError() {
        ws.setCuName("Acme Corp");
        ws.setCuRegion(10);
        doReturn(true).when(regnfSpy).isInvalidKey();

        invokePrivate("validateCustomerFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Region code not found", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateCustomerFields_staffNotFound_setsError() {
        ws.setCuName("Acme Corp");
        ws.setCuRegion(0);
        ws.setCuStaff(20);
        doReturn(true).when(staffSpy).isInvalidKey();

        invokePrivate("validateCustomerFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Sales rep not found", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateCustomerFields_bankNotFound_setsError() {
        ws.setCuName("Acme Corp");
        ws.setCuRegion(0);
        ws.setCuStaff(0);
        ws.setCuBankCode(30);
        doReturn(true).when(bankfSpy).isInvalidKey();

        invokePrivate("validateCustomerFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Bank code not found", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateCustomerFields_negativeCreditLimit_setsErrorAndFallsThrough() {
        ws.setCuName("Acme Corp");
        ws.setCuRegion(0);
        ws.setCuStaff(0);
        ws.setCuBankCode(0);
        ws.setCuCreditLimit(new BigDecimal("-100"));

        invokePrivate("validateCustomerFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Credit limit cannot be negative", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateCustomerFields_allValid_noError() {
        ws.setCuName("Acme Corp");
        ws.setCuRegion(0);
        ws.setCuStaff(0);
        ws.setCuBankCode(0);
        ws.setCuCreditLimit(new BigDecimal("100"));

        invokePrivate("validateCustomerFields");

        assertEquals(0, ws.getErrFlg());
    }

    /* ── VAL-999 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void reportValidationError_errFlgSet_displaysMessage() {
        ws.setErrFlg(1);

        invokePrivate("reportValidationError");

        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void reportValidationError_errFlgNotSet_noDisplay() {
        ws.setErrFlg(0);

        invokePrivate("reportValidationError");

        assertFalse(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    /* ── SAVE-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveCustomerRecord_modeAdd_startDateZero_setsStartDateAndWrites() {
        ws.setModeFlg(1);
        ws.setCuStartDate(0);
        ws.setWkSysdate(20260918);
        ws.setWkUserCode(7);

        invokePrivate("saveCustomerRecord");

        assertEquals(20260918, ws.getCuStartDate());
        assertEquals(20260918, ws.getCuAddDate());
        assertEquals(7, ws.getCuAddUser());
        assertEquals(0, ws.getCuDelFlag());
        verify(custfSpy).write();
        assertEquals("Customer added", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveCustomerRecord_modeAdd_writeInvalidKey_showsDuplicateMessage() {
        ws.setModeFlg(1);
        doReturn(true).when(custfSpy).isInvalidKey();

        invokePrivate("saveCustomerRecord");

        assertEquals("Write failed - duplicate", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveCustomerRecord_modeChg_rewriteSuccess_showsUpdatedMessage() {
        ws.setModeFlg(2);

        invokePrivate("saveCustomerRecord");

        verify(custfSpy).rewrite();
        assertEquals("Customer updated", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveCustomerRecord_modeChg_rewriteInvalidKey_showsUpdateFailedMessage() {
        ws.setModeFlg(2);
        doReturn(true).when(custfSpy).isInvalidKey();

        invokePrivate("saveCustomerRecord");

        assertEquals("Update failed", ws.getWkMsgLine().trim());
    }

    /* ── DEL-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteCustomerWithConfirm_confirmY_deletesAndRewrites() {
        acceptFieldValues.put("SC-CONF", "Y");
        ws.setWkSysdate(20260918);
        ws.setWkUserCode(9);

        invokePrivate("deleteCustomerWithConfirm");

        assertEquals(1, ws.getCuDelFlag());
        verify(custfSpy).rewrite();
        assertEquals("Customer deleted", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteCustomerWithConfirm_confirmLowercaseY_deletesAndRewrites() {
        acceptFieldValues.put("SC-CONF", "y");

        invokePrivate("deleteCustomerWithConfirm");

        assertEquals(1, ws.getCuDelFlag());
        verify(custfSpy).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteCustomerWithConfirm_confirmN_cancelsWithoutRewrite() {
        acceptFieldValues.put("SC-CONF", "N");

        invokePrivate("deleteCustomerWithConfirm");

        assertEquals("Delete cancelled", ws.getWkMsgLine().trim());
        verify(custfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteCustomerWithConfirm_confirmYRewriteInvalidKey_showsDeleteFailed() {
        acceptFieldValues.put("SC-CONF", "Y");
        doReturn(true).when(custfSpy).isInvalidKey();

        invokePrivate("deleteCustomerWithConfirm");

        assertEquals("Delete failed", ws.getWkMsgLine().trim());
    }

    /* ── LOOK-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupRelatedNames_allCodesZero_blanksAllNames() {
        ws.setCuRegion(0);
        ws.setCuStaff(0);
        ws.setCuBankCode(0);

        invokePrivate("lookupRelatedNames");

        assertEquals("", ws.getWkRegionName().trim());
        assertEquals("", ws.getWkStaffName().trim());
        assertEquals("", ws.getWkBankName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupRelatedNames_allFound_setsAllNames() {
        ws.setCuRegion(10);
        ws.setCuStaff(20);
        ws.setCuBankCode(30);
        ws.setRgName("Tokyo");
        ws.setSfName("Taro Yamada");
        ws.setBkName("Bank of Sakura");
        doReturn(false).when(regnfSpy).isInvalidKey();
        doReturn(false).when(staffSpy).isInvalidKey();
        doReturn(false).when(bankfSpy).isInvalidKey();

        invokePrivate("lookupRelatedNames");

        assertEquals("Tokyo", ws.getWkRegionName().trim());
        assertEquals("Taro Yamada", ws.getWkStaffName().trim());
        assertEquals("Bank of Sakura", ws.getWkBankName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupRelatedNames_allNotFound_setsUnknownNames() {
        ws.setCuRegion(10);
        ws.setCuStaff(20);
        ws.setCuBankCode(30);
        doReturn(true).when(regnfSpy).isInvalidKey();
        doReturn(true).when(staffSpy).isInvalidKey();
        doReturn(true).when(bankfSpy).isInvalidKey();

        invokePrivate("lookupRelatedNames");

        assertEquals("??? unknown region", ws.getWkRegionName().trim());
        assertEquals("??? unknown staff", ws.getWkStaffName().trim());
        assertEquals("??? unknown bank", ws.getWkBankName().trim());
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void closeAllFiles_closesAllFourFiles() {
        invokePrivate("closeAllFiles");

        verify(custfSpy).close();
        verify(regnfSpy).close();
        verify(staffSpy).close();
        verify(bankfSpy).close();
    }
}

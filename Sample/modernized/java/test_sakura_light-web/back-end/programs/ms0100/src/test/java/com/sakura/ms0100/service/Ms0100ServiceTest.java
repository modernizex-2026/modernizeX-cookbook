package com.sakura.ms0100.service;

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
import com.sakura.ms0100.domain.Ms0100FieldAccess;
import com.sakura.ms0100.runtime.Ms0100Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.ScreenModels;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.BankfDataset;
import com.sakura.runtime.linkage.AbortxLinkParm;
import com.sakura.runtime.linkage.DateutLinkParm;

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
 * Unit tests for {@link Ms0100Service}, derived from COBOL program MS0100 (bank master
 * maintenance). Ground truth for inputs/expected values: MS0100.cob PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Ms0100ServiceTest {

    @Spy private BankfDataset bankfSpy = new BankfDataset();

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;
    @Mock private ScreenRendererInstance renderer;

    private Ms0100Service service;
    private Ms0100FieldAccess ws;

    private final List<String> screenInteractions = new ArrayList<>();
    private final Map<String, String> acceptFieldValues = new HashMap<>();

    /** Subclass swapping in the spy dataset since Ms0100Datasets builds its own real file. */
    private static class TestDatasets extends Ms0100Datasets {
        private final BankfDataset bankf;

        TestDatasets(BankfDataset bankf) {
            this.bankf = bankf;
        }

        @Override
        public BankfDataset getBankf() {
            return bankf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Ms0100Datasets fileSet = new TestDatasets(bankfSpy);
        service = new Ms0100Service(fileSet, dateutService, abortxService, renderer);
        service.setRenderer(renderer);
        ws = getWs();

        doNothing().when(bankfSpy).open(any());
        doNothing().when(bankfSpy).close();
        doNothing().when(bankfSpy).write();
        doNothing().when(bankfSpy).rewrite();
        doReturn("00").when(bankfSpy).getFileStatus();
        doReturn(false).when(bankfSpy).isInvalidKey();
        doReturn(true).when(bankfSpy).readByKey(any());

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

    private Ms0100FieldAccess getWs() throws Exception {
        Field f = Ms0100Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Ms0100FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Ms0100Service.class.getDeclaredMethod(name);
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
            Method m = Ms0100Service.class.getDeclaredMethod(name, paramType);
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
        Method m = Ms0100Service.class.getDeclaredMethod("getFileSet");
        m.setAccessible(true);
        Object result = m.invoke(service);
        assertTrue(result instanceof Ms0100Datasets);
    }

    /* ── INIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeAndOpenBankFile_success_setsHeaderFieldsAndOpensFileIO() {
        invokePrivate("initializeAndOpenBankFile");

        assertEquals("MS0100", ws.getWkProgid().trim());
        assertEquals("Bank Master Maintenance", ws.getWkTitle().trim());
        assertEquals("ENTER=Read  PF9=Delete  PF3=End", ws.getWkFkeyLine().trim());
        assertEquals(20260918, ws.getWkSysymd());
        assertEquals(20260918, ws.getWkSysdate());
        verify(bankfSpy).open(FileOpenMode.IO);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeAndOpenBankFile_status35_reopensAsOutputThenIO() {
        doReturn("35", "00", "00", "00").when(bankfSpy).getFileStatus();

        invokePrivate("initializeAndOpenBankFile");

        verify(bankfSpy, times(2)).open(FileOpenMode.IO);
        verify(bankfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(bankfSpy, times(1)).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeAndOpenBankFile_status30_reopensAsOutputThenIO() {
        doReturn("30", "00", "00", "00").when(bankfSpy).getFileStatus();

        invokePrivate("initializeAndOpenBankFile");

        verify(bankfSpy, times(2)).open(FileOpenMode.IO);
        verify(bankfSpy, times(1)).open(FileOpenMode.OUTPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeAndOpenBankFile_errorStatus_abortsWithFsts() {
        doReturn("23").when(bankfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeAndOpenBankFile"));

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

        assertEquals("MS0100", ws.getKaProgid().trim());
        assertEquals("23", ws.getKaFsts().trim());
        assertEquals("EOPEN ", ws.getKaMsgcode());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── MAIN-000 loop ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_immediateEnd_closesFileAndThrowsProgramExitSignal() {
        doReturn("03").when(renderer).readEndStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

        assertEquals(1, ws.getEndFlg());
        verify(bankfSpy).close();
    }

    /* ── MAIN-RTN-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayMainScreenAndDispatch_ests03_setsEndFlg() {
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("displayMainScreenAndDispatch");

        assertEquals(1, ws.getEndFlg());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayMainScreenAndDispatch_estsOther_showsInvalidKeyMessage() {
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("displayMainScreenAndDispatch");

        assertEquals(0, ws.getEndFlg());
        assertEquals("Invalid function key", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayMainScreenAndDispatch_ests00_bkCodeZero_showsMustNotBeZeroMessage() {
        acceptFieldValues.put("SC-KEY", "0");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("displayMainScreenAndDispatch");

        assertEquals("Bank code must not be zero", ws.getWkMsgLine().trim());
        verify(bankfSpy, never()).readByKey(any());
    }

    /* ── CLEAR-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void resetBankRecord_resetsWorkFieldsToDefaults() {
        ws.setWkConfirm("Y");
        ws.setWkSaveCode(999);

        invokePrivate("resetBankRecord");

        verify(bankfSpy).setRecord();
        assertEquals("", ws.getWkConfirm().trim());
        assertEquals(0, ws.getWkSaveCode());
    }

    /* ── PKEY-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readBankByKeyAndDispatch_bkCodeZero_showsMessageAndReturnsWithoutRead() {
        ws.setBkCode(0);

        invokePrivate("readBankByKeyAndDispatch");

        assertEquals("Bank code must not be zero", ws.getWkMsgLine().trim());
        verify(bankfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readBankByKeyAndDispatch_invalidKey_setsUpAddMode() {
        ws.setBkCode(100);
        doReturn(true).when(bankfSpy).isInvalidKey();
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("readBankByKeyAndDispatch");

        assertEquals(1, ws.getModeFlg());
        assertEquals(100, ws.getBkCode());
        assertEquals("New bank - enter details", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readBankByKeyAndDispatch_validKey_setsUpChangeMode() {
        ws.setBkCode(100);
        ws.setBkDelFlag(0);
        doReturn(false).when(bankfSpy).isInvalidKey();
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("readBankByKeyAndDispatch");

        assertEquals(2, ws.getModeFlg());
        assertEquals("Existing bank - change or PF9 delete", ws.getWkMsgLine().trim());
    }

    /* ── SADD-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareNewBankEntry_setsModeAddAndCode() {
        ws.setWkSaveCode(555);

        invokePrivate("prepareNewBankEntry");

        assertEquals(1, ws.getModeFlg());
        assertEquals(555, ws.getBkCode());
        assertEquals("New bank - enter details", ws.getWkMsgLine().trim());
        verify(bankfSpy).setRecord();
    }

    /* ── SCHG-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareExistingBankChange_deletedBank_setsModeAddAndReregisterMessage() {
        ws.setBkDelFlag(1);

        invokePrivate("prepareExistingBankChange");

        assertEquals(1, ws.getModeFlg());
        assertEquals("Deleted bank - re-registering", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareExistingBankChange_notDeleted_setsModeChgAndChangeMessage() {
        ws.setBkDelFlag(0);

        invokePrivate("prepareExistingBankChange");

        assertEquals(2, ws.getModeFlg());
        assertEquals("Existing bank - change or PF9 delete", ws.getWkMsgLine().trim());
    }

    /* ── EDIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptBankDetailsAndDispatch_ests03_acceptsFieldsAndContinues() {
        acceptFieldValues.put("BK-NAME", "First National");
        acceptFieldValues.put("BK-BRANCH", "Main Branch");
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("acceptBankDetailsAndDispatch");

        assertEquals("First National", ws.getBkName().trim());
        assertEquals("Main Branch", ws.getBkBranch().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptBankDetailsAndDispatch_ests04_showsCancelledMessage() {
        doReturn("04").when(renderer).readEndStatus();

        invokePrivate("acceptBankDetailsAndDispatch");

        assertEquals("Cancelled", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptBankDetailsAndDispatch_ests09_modeChg_callsDeleteWithConfirm() {
        ws.setModeFlg(2);
        acceptFieldValues.put("SC-CONF", "N");
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("acceptBankDetailsAndDispatch");

        assertEquals("Delete cancelled", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptBankDetailsAndDispatch_ests09_modeAdd_showsNothingToDeleteMessage() {
        ws.setModeFlg(1);
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("acceptBankDetailsAndDispatch");

        assertEquals("Nothing to delete", ws.getWkMsgLine().trim());
        assertFalse(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptBankDetailsAndDispatch_ests00_validationFails_doesNotSave() {
        acceptFieldValues.put("BK-NAME", "");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("acceptBankDetailsAndDispatch");

        assertEquals(1, ws.getErrFlg());
        verify(bankfSpy, never()).write();
        verify(bankfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptBankDetailsAndDispatch_ests00_validationPasses_savesAddedBank() {
        ws.setModeFlg(1);
        acceptFieldValues.put("BK-NAME", "First National");
        acceptFieldValues.put("BK-BRANCH", "Main Branch");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("acceptBankDetailsAndDispatch");

        assertEquals(0, ws.getErrFlg());
        verify(bankfSpy).write();
        assertEquals("Bank added", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptBankDetailsAndDispatch_estsOther_showsInvalidKeyMessage() {
        doReturn("07").when(renderer).readEndStatus();

        invokePrivate("acceptBankDetailsAndDispatch");

        assertEquals("Invalid function key", ws.getWkMsgLine().trim());
    }

    /* ── VAL-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateBankName_blankName_setsErrorAndDisplays() {
        ws.setBkName(" ");

        invokePrivate("validateBankName");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Bank name is required", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateBankName_nameProvided_noError() {
        ws.setBkName("First National");

        invokePrivate("validateBankName");

        assertEquals(0, ws.getErrFlg());
        assertFalse(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    /* ── SAVE-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveBankRecord_modeAdd_writeSuccess_clearsDelFlagAndWrites() {
        ws.setModeFlg(1);
        ws.setBkDelFlag(1);

        invokePrivate("saveBankRecord");

        assertEquals(0, ws.getBkDelFlag());
        verify(bankfSpy).write();
        assertEquals("Bank added", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveBankRecord_modeAdd_writeInvalidKey_showsDuplicateMessage() {
        ws.setModeFlg(1);
        doReturn(true).when(bankfSpy).isInvalidKey();

        invokePrivate("saveBankRecord");

        assertEquals("Write failed - duplicate", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveBankRecord_modeChg_rewriteSuccess_showsUpdatedMessage() {
        ws.setModeFlg(2);

        invokePrivate("saveBankRecord");

        verify(bankfSpy).rewrite();
        assertEquals("Bank updated", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveBankRecord_modeChg_rewriteInvalidKey_showsUpdateFailedMessage() {
        ws.setModeFlg(2);
        doReturn(true).when(bankfSpy).isInvalidKey();

        invokePrivate("saveBankRecord");

        assertEquals("Update failed", ws.getWkMsgLine().trim());
    }

    /* ── DEL-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteBank_confirmY_deletesAndRewrites() {
        acceptFieldValues.put("SC-CONF", "Y");

        invokePrivate("confirmAndDeleteBank");

        assertEquals(1, ws.getBkDelFlag());
        verify(bankfSpy).rewrite();
        assertEquals("Bank deleted", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteBank_confirmLowercaseY_deletesAndRewrites() {
        acceptFieldValues.put("SC-CONF", "y");

        invokePrivate("confirmAndDeleteBank");

        assertEquals(1, ws.getBkDelFlag());
        verify(bankfSpy).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteBank_confirmN_cancelsWithoutRewrite() {
        acceptFieldValues.put("SC-CONF", "N");

        invokePrivate("confirmAndDeleteBank");

        assertEquals("Delete cancelled", ws.getWkMsgLine().trim());
        verify(bankfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteBank_confirmYRewriteInvalidKey_showsDeleteFailed() {
        acceptFieldValues.put("SC-CONF", "Y");
        doReturn(true).when(bankfSpy).isInvalidKey();

        invokePrivate("confirmAndDeleteBank");

        assertEquals("Delete failed", ws.getWkMsgLine().trim());
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void closeBankFile_closesFile() {
        invokePrivate("closeBankFile");

        verify(bankfSpy).close();
    }
}

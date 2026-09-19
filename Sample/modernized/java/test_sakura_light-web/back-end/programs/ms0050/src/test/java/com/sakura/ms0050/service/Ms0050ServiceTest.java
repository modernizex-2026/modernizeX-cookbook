package com.sakura.ms0050.service;

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
import com.sakura.ms0050.domain.Ms0050FieldAccess;
import com.sakura.ms0050.runtime.Ms0050Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.ScreenModels;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.DeptfDataset;
import com.sakura.runtime.io.StaffDataset;
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
 * Unit tests for {@link Ms0050Service}, derived from COBOL program MS0050 (staff master
 * maintenance). Ground truth for inputs/expected values: MS0050.cob PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Ms0050ServiceTest {

    @Spy private StaffDataset staffSpy = new StaffDataset();
    @Spy private DeptfDataset deptfSpy = new DeptfDataset();

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;
    @Mock private ScreenRendererInstance renderer;

    private Ms0050Service service;
    private Ms0050FieldAccess ws;

    private final List<String> screenInteractions = new ArrayList<>();
    private final Map<String, String> acceptFieldValues = new HashMap<>();

    /** Subclass swapping in the spy datasets since Ms0050Datasets builds its own real files. */
    private static class TestDatasets extends Ms0050Datasets {
        private final StaffDataset staff;
        private final DeptfDataset deptf;

        TestDatasets(StaffDataset staff, DeptfDataset deptf) {
            this.staff = staff;
            this.deptf = deptf;
        }

        @Override
        public StaffDataset getStaff() {
            return staff;
        }

        @Override
        public DeptfDataset getDeptf() {
            return deptf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Ms0050Datasets fileSet = new TestDatasets(staffSpy, deptfSpy);
        service = new Ms0050Service(fileSet, dateutService, abortxService, renderer);
        service.setRenderer(renderer);
        ws = getWs();

        for (RawDatasetBase spy : new RawDatasetBase[] {staffSpy, deptfSpy}) {
            doNothing().when(spy).open(any());
            doNothing().when(spy).close();
            doNothing().when(spy).write();
            doNothing().when(spy).rewrite();
            doNothing().when(spy).setRecord();
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

    private Ms0050FieldAccess getWs() throws Exception {
        Field f = Ms0050Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Ms0050FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Ms0050Service.class.getDeclaredMethod(name);
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
            Method m = Ms0050Service.class.getDeclaredMethod(name, paramType);
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
        Method m = Ms0050Service.class.getDeclaredMethod("getFileSet");
        m.setAccessible(true);
        Object result = m.invoke(service);
        assertTrue(result instanceof Ms0050Datasets);
    }

    /* ── INIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_success_setsHeaderFieldsAndOpensAllFiles() {
        invokePrivate("initializeProgram");

        assertEquals("MS0050", ws.getWkProgid());
        assertEquals("Staff Master Maintenance", ws.getWkTitle().trim());
        assertEquals("ENTER=Read  PF9=Delete  PF3=End", ws.getWkFkeyLine().trim());
        assertEquals(20260918, ws.getWkSysymd());
        assertEquals(20260918, ws.getWkSysdate());
        verify(staffSpy).open(FileOpenMode.IO);
        verify(deptfSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_staffStatus35_reopensAsOutputThenIO() {
        doReturn("35", "00", "00", "00").when(staffSpy).getFileStatus();

        invokePrivate("initializeProgram");

        verify(staffSpy, times(2)).open(FileOpenMode.IO);
        verify(staffSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(staffSpy, times(1)).close();
        verify(deptfSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_staffStatus30_reopensAsOutputThenIO() {
        doReturn("30", "00", "00", "00").when(staffSpy).getFileStatus();

        invokePrivate("initializeProgram");

        verify(staffSpy, times(2)).open(FileOpenMode.IO);
        verify(staffSpy, times(1)).open(FileOpenMode.OUTPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_staffErrorStatus_abortsAndSkipsDeptf() {
        doReturn("23").when(staffSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeProgram"));

        assertEquals("STAFF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(deptfSpy, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_deptfErrorStatus_aborts() {
        doReturn("00").when(staffSpy).getFileStatus();
        doReturn("23").when(deptfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeProgram"));

        assertEquals("DEPTF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── ABEND-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abortProgram_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortProgram"));

        assertEquals("MS0050", ws.getKaProgid());
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
        verify(staffSpy).close();
        verify(deptfSpy).close();
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
    void processMainScreenCycle_ests00_sfCodeZero_showsMustNotBeZeroMessage() {
        acceptFieldValues.put("SC-KEY", "0");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("processMainScreenCycle");

        assertEquals("Staff code must not be zero", ws.getWkMsgLine().trim());
        verify(staffSpy, never()).readByKey(any());
    }

    /* ── CLEAR-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearWorkFields_resetsWorkFields() {
        ws.setWkDeptName("dirty");
        ws.setWkConfirm("Y");

        invokePrivate("clearWorkFields");

        verify(staffSpy).setRecord();
        assertEquals("", ws.getWkDeptName().trim());
        assertEquals("", ws.getWkConfirm().trim());
    }

    /* ── PKEY-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processStaffCodeKey_sfCodeZero_showsMessageAndReturnsWithoutRead() {
        ws.setSfCode(0);

        invokePrivate("processStaffCodeKey");

        assertEquals("Staff code must not be zero", ws.getWkMsgLine().trim());
        verify(staffSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processStaffCodeKey_staffInvalidKey_setsUpAddMode() {
        ws.setSfCode(100);
        doReturn(true).when(staffSpy).isInvalidKey();
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processStaffCodeKey");

        assertEquals(1, ws.getModeFlg());
        assertEquals(100, ws.getSfCode());
        assertEquals("New staff - enter details", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processStaffCodeKey_staffValidKey_setsUpChangeMode() {
        ws.setSfCode(100);
        ws.setSfDelFlag(0);
        doReturn(false).when(staffSpy).isInvalidKey();
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processStaffCodeKey");

        assertEquals(2, ws.getModeFlg());
        assertEquals("Existing staff - change or PF9 delete", ws.getWkMsgLine().trim());
    }

    /* ── SADD-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareAddMode_setsModeAddCodeAndMessage() {
        ws.setWkSaveCode(555);

        invokePrivate("prepareAddMode");

        assertEquals(1, ws.getModeFlg());
        assertEquals(555, ws.getSfCode());
        assertEquals("New staff - enter details", ws.getWkMsgLine().trim());
        verify(staffSpy).setRecord();
    }

    /* ── SCHG-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareChangeMode_deletedStaff_setsModeAddAndReregisterMessage() {
        ws.setSfDelFlag(1);

        invokePrivate("prepareChangeMode");

        assertEquals(1, ws.getModeFlg());
        assertEquals("Deleted staff - re-registering", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareChangeMode_notDeleted_setsModeChgAndChangeMessage() {
        ws.setSfDelFlag(0);

        invokePrivate("prepareChangeMode");

        assertEquals(2, ws.getModeFlg());
        assertEquals("Existing staff - change or PF9 delete", ws.getWkMsgLine().trim());
    }

    /* ── EDIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptStaffDetailFields_ests03_acceptsFieldsAndContinues() {
        acceptFieldValues.put("SF-NAME", "Taro Yamada");
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("acceptStaffDetailFields");

        assertEquals("Taro Yamada", ws.getSfName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptStaffDetailFields_ests04_showsCancelledMessage() {
        doReturn("04").when(renderer).readEndStatus();

        invokePrivate("acceptStaffDetailFields");

        assertEquals("Cancelled", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptStaffDetailFields_ests09_modeChg_callsDeleteWithConfirm() {
        ws.setModeFlg(2);
        acceptFieldValues.put("SC-CONF", "N");
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("acceptStaffDetailFields");

        assertEquals("Delete cancelled", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptStaffDetailFields_ests09_modeAdd_showsNothingToDeleteMessage() {
        ws.setModeFlg(1);
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("acceptStaffDetailFields");

        assertEquals("Nothing to delete", ws.getWkMsgLine().trim());
        assertFalse(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptStaffDetailFields_ests00_validationFails_doesNotSave() {
        acceptFieldValues.put("SF-NAME", "");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("acceptStaffDetailFields");

        assertEquals(1, ws.getErrFlg());
        verify(staffSpy, never()).write();
        verify(staffSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptStaffDetailFields_ests00_validationPasses_savesAddedStaff() {
        ws.setModeFlg(1);
        acceptFieldValues.put("SF-NAME", "Taro Yamada");
        acceptFieldValues.put("SF-DEPT", "10");
        doReturn(false).when(deptfSpy).isInvalidKey();
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("acceptStaffDetailFields");

        assertEquals(0, ws.getErrFlg());
        verify(staffSpy).write();
        assertEquals("Staff added", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptStaffDetailFields_estsOther_showsInvalidKeyMessage() {
        doReturn("07").when(renderer).readEndStatus();

        invokePrivate("acceptStaffDetailFields");

        assertEquals("Invalid function key", ws.getWkMsgLine().trim());
    }

    /* ── VAL-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateStaffFields_blankName_setsErrorAndReturns() {
        ws.setSfName(" ");

        invokePrivate("validateStaffFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Name is required", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateStaffFields_deptZero_setsErrorAndReturns() {
        ws.setSfName("Taro Yamada");
        ws.setSfDept(0);

        invokePrivate("validateStaffFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Department is required", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateStaffFields_deptNotFound_setsError() {
        ws.setSfName("Taro Yamada");
        ws.setSfDept(10);
        doReturn(true).when(deptfSpy).isInvalidKey();

        invokePrivate("validateStaffFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Department code not found", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateStaffFields_deptDeleted_setsError() {
        ws.setSfName("Taro Yamada");
        ws.setSfDept(10);
        doReturn(false).when(deptfSpy).isInvalidKey();
        ws.setDpDelFlag(1);

        invokePrivate("validateStaffFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Department is deleted", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateStaffFields_allValid_noErrorAndSetsDeptName() {
        ws.setSfName("Taro Yamada");
        ws.setSfDept(10);
        doReturn(false).when(deptfSpy).isInvalidKey();
        ws.setDpDelFlag(0);
        ws.setDpName("Sales");

        invokePrivate("validateStaffFields");

        assertEquals(0, ws.getErrFlg());
        assertEquals("Sales", ws.getWkDeptName().trim());
    }

    /* ── VAL-999 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayValidationError_errFlgSet_displaysMessage() {
        ws.setErrFlg(1);

        invokePrivate("displayValidationError");

        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayValidationError_errFlgNotSet_noDisplay() {
        ws.setErrFlg(0);

        invokePrivate("displayValidationError");

        assertFalse(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    /* ── SAVE-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveStaffRecord_modeAdd_writeSuccess_showsAddedMessage() {
        ws.setModeFlg(1);

        invokePrivate("saveStaffRecord");

        assertEquals(0, ws.getSfDelFlag());
        verify(staffSpy).write();
        assertEquals("Staff added", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveStaffRecord_modeAdd_writeInvalidKey_showsDuplicateMessage() {
        ws.setModeFlg(1);
        doReturn(true).when(staffSpy).isInvalidKey();

        invokePrivate("saveStaffRecord");

        assertEquals("Write failed - duplicate", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveStaffRecord_modeChg_rewriteSuccess_showsUpdatedMessage() {
        ws.setModeFlg(2);

        invokePrivate("saveStaffRecord");

        verify(staffSpy).rewrite();
        assertEquals("Staff updated", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveStaffRecord_modeChg_rewriteInvalidKey_showsUpdateFailedMessage() {
        ws.setModeFlg(2);
        doReturn(true).when(staffSpy).isInvalidKey();

        invokePrivate("saveStaffRecord");

        assertEquals("Update failed", ws.getWkMsgLine().trim());
    }

    /* ── DEL-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteStaff_confirmY_deletesAndRewrites() {
        acceptFieldValues.put("SC-CONF", "Y");

        invokePrivate("confirmAndDeleteStaff");

        assertEquals(1, ws.getSfDelFlag());
        verify(staffSpy).rewrite();
        assertEquals("Staff deleted", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteStaff_confirmLowercaseY_deletesAndRewrites() {
        acceptFieldValues.put("SC-CONF", "y");

        invokePrivate("confirmAndDeleteStaff");

        assertEquals(1, ws.getSfDelFlag());
        verify(staffSpy).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteStaff_confirmN_cancelsWithoutRewrite() {
        acceptFieldValues.put("SC-CONF", "N");

        invokePrivate("confirmAndDeleteStaff");

        assertEquals("Delete cancelled", ws.getWkMsgLine().trim());
        verify(staffSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteStaff_confirmYRewriteInvalidKey_showsDeleteFailed() {
        acceptFieldValues.put("SC-CONF", "Y");
        doReturn(true).when(staffSpy).isInvalidKey();

        invokePrivate("confirmAndDeleteStaff");

        assertEquals("Delete failed", ws.getWkMsgLine().trim());
    }

    /* ── LOOK-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupDeptName_sfDeptZero_blanksName() {
        ws.setSfDept(0);
        ws.setWkDeptName("dirty");

        invokePrivate("lookupDeptName");

        assertEquals("", ws.getWkDeptName().trim());
        verify(deptfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupDeptName_found_setsDeptName() {
        ws.setSfDept(10);
        doReturn(false).when(deptfSpy).isInvalidKey();
        ws.setDpName("Sales");

        invokePrivate("lookupDeptName");

        assertEquals("Sales", ws.getWkDeptName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupDeptName_notFound_setsUnknownName() {
        ws.setSfDept(10);
        doReturn(true).when(deptfSpy).isInvalidKey();

        invokePrivate("lookupDeptName");

        assertEquals("??? unknown department", ws.getWkDeptName().trim());
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void closeAllFiles_closesBothFiles() {
        invokePrivate("closeAllFiles");

        verify(staffSpy).close();
        verify(deptfSpy).close();
    }
}

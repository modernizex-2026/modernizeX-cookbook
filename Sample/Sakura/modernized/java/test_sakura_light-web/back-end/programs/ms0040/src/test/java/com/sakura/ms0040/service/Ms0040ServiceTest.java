package com.sakura.ms0040.service;

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
import com.sakura.ms0040.domain.Ms0040FieldAccess;
import com.sakura.ms0040.runtime.Ms0040Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.ScreenModels;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.StaffDataset;
import com.sakura.runtime.io.WhsefDataset;
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
 * Unit tests for {@link Ms0040Service}, derived from COBOL program MS0040 (warehouse master
 * maintenance). Ground truth for inputs/expected values: MS0040.cob PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Ms0040ServiceTest {

    @Spy private WhsefDataset whsefSpy = new WhsefDataset();
    @Spy private StaffDataset staffSpy = new StaffDataset();

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;
    @Mock private ScreenRendererInstance renderer;

    private Ms0040Service service;
    private Ms0040FieldAccess ws;

    private final List<String> screenInteractions = new ArrayList<>();
    private final Map<String, String> acceptFieldValues = new HashMap<>();

    /** Subclass swapping in the spy datasets since Ms0040Datasets builds its own real files. */
    private static class TestDatasets extends Ms0040Datasets {
        private final WhsefDataset whsef;
        private final StaffDataset staff;

        TestDatasets(WhsefDataset whsef, StaffDataset staff) {
            this.whsef = whsef;
            this.staff = staff;
        }

        @Override
        public WhsefDataset getWhsef() {
            return whsef;
        }

        @Override
        public StaffDataset getStaff() {
            return staff;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Ms0040Datasets fileSet = new TestDatasets(whsefSpy, staffSpy);
        service = new Ms0040Service(fileSet, dateutService, abortxService, renderer);
        service.setRenderer(renderer);
        ws = getWs();

        for (RawDatasetBase spy : new RawDatasetBase[] {whsefSpy, staffSpy}) {
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
                            com.sakura.runtime.linkage.DateutLinkParm p = inv.getArgument(0);
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

    private Ms0040FieldAccess getWs() throws Exception {
        Field f = Ms0040Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Ms0040FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Ms0040Service.class.getDeclaredMethod(name);
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
            Method m = Ms0040Service.class.getDeclaredMethod(name, paramType);
            m.setAccessible(true);
            m.invoke(service, arg);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /* ── mainProcess / getCompletionCode / setCompletionCode / getFileSet / setRenderer ── */

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
        Method m = Ms0040Service.class.getDeclaredMethod("getFileSet");
        m.setAccessible(true);
        Object result = m.invoke(service);
        assertTrue(result instanceof Ms0040Datasets);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void setRenderer_setsRendererWithoutError() {
        service.setRenderer(renderer);
        assertTrue(true);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainProcess_ests03_closesAllFilesAndThrowsProgramExitSignal() throws Exception {
        doReturn("03").when(renderer).readEndStatus();
        Method m = Ms0040Service.class.getDeclaredMethod("mainProcess");
        m.setAccessible(true);

        assertThrows(InvocationTargetException.class, () -> m.invoke(service));

        assertEquals(1, ws.getEndFlg());
        verify(whsefSpy).close();
        verify(staffSpy).close();
    }

    /* ── INIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_success_setsHeaderFieldsAndOpensAllFiles() {
        invokePrivate("initializeProgram");

        assertEquals("MS0040", ws.getWkProgid().trim());
        assertEquals("Warehouse Master Maintenance", ws.getWkTitle().trim());
        assertEquals("ENTER=Read  PF9=Delete  PF3=End", ws.getWkFkeyLine().trim());
        assertEquals(20260918, ws.getWkSysymd());
        assertEquals(20260918, ws.getWkSysdate());
        verify(whsefSpy).open(FileOpenMode.IO);
        verify(staffSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_whsefStatus35_reopensAsOutputThenIO() {
        doReturn("35", "00", "00", "00").when(whsefSpy).getFileStatus();

        invokePrivate("initializeProgram");

        verify(whsefSpy, times(2)).open(FileOpenMode.IO);
        verify(whsefSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(whsefSpy, times(1)).close();
        verify(staffSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_whsefStatus30_reopensAsOutputThenIO() {
        doReturn("30", "00", "00", "00").when(whsefSpy).getFileStatus();

        invokePrivate("initializeProgram");

        verify(whsefSpy, times(2)).open(FileOpenMode.IO);
        verify(whsefSpy, times(1)).open(FileOpenMode.OUTPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_whsefErrorStatus_abortsAndSkipsStaff() {
        doReturn("23").when(whsefSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeProgram"));

        assertEquals("WHSEF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(staffSpy, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_staffErrorStatus_aborts() {
        doReturn("00").when(whsefSpy).getFileStatus();
        doReturn("23").when(staffSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeProgram"));

        assertEquals("STAFF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── ABEND-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abortOnFileError_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortOnFileError"));

        assertEquals("MS0040", ws.getKaProgid().trim());
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
        verify(whsefSpy).close();
        verify(staffSpy).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_loopsUntilEndFlgThenStops() {
        acceptFieldValues.put("SC-KEY", "0");
        doReturn("00", "00", "03").when(renderer).readEndStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

        assertEquals(1, ws.getEndFlg());
        verify(renderer, times(3)).readEndStatus();
    }

    /* ── MAIN-RTN-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processMainScreen_ests03_setsEndFlg() {
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processMainScreen");

        assertEquals(1, ws.getEndFlg());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processMainScreen_estsOther_showsInvalidKeyMessage() {
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("processMainScreen");

        assertEquals(0, ws.getEndFlg());
        assertEquals("Invalid function key", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processMainScreen_ests00_whCodeZero_showsMustNotBeZeroMessage() {
        acceptFieldValues.put("SC-KEY", "0");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("processMainScreen");

        assertEquals("Warehouse code must not be zero", ws.getWkMsgLine().trim());
        verify(whsefSpy, never()).readByKey(any());
    }

    /* ── CLEAR-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearWarehouseWorkArea_resetsWorkFieldsToDefaults() {
        ws.setWkMgrName("dirty");
        ws.setWkConfirm("Y");

        invokePrivate("clearWarehouseWorkArea");

        verify(whsefSpy).setRecord();
        assertEquals("", ws.getWkMgrName().trim());
        assertEquals("", ws.getWkConfirm().trim());
    }

    /* ── PKEY-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processPrimaryKeyEntry_whCodeZero_showsMessageAndReturnsWithoutRead() {
        ws.setWhCode(0);

        invokePrivate("processPrimaryKeyEntry");

        assertEquals("Warehouse code must not be zero", ws.getWkMsgLine().trim());
        verify(whsefSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processPrimaryKeyEntry_whsefInvalidKey_setsUpAddMode() {
        ws.setWhCode(100);
        doReturn(true).when(whsefSpy).isInvalidKey();
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processPrimaryKeyEntry");

        assertEquals(1, ws.getModeFlg());
        assertEquals(100, ws.getWhCode());
        assertEquals("New warehouse - enter details", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processPrimaryKeyEntry_whsefValidKey_setsUpChangeMode() {
        ws.setWhCode(100);
        ws.setWhDelFlag(0);
        doReturn(false).when(whsefSpy).isInvalidKey();
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processPrimaryKeyEntry");

        assertEquals(2, ws.getModeFlg());
        assertEquals("Existing warehouse - change or PF9 delete", ws.getWkMsgLine().trim());
    }

    /* ── SADD-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareNewWarehouseRecord_setsModeAddCodeAndMessage() {
        ws.setWkSaveCode(555);

        invokePrivate("prepareNewWarehouseRecord");

        assertEquals(1, ws.getModeFlg());
        assertEquals(555, ws.getWhCode());
        assertEquals(1, ws.getWhType());
        assertEquals("New warehouse - enter details", ws.getWkMsgLine().trim());
        verify(whsefSpy).setRecord();
    }

    /* ── SCHG-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareChangeWarehouseRecord_deletedWarehouse_setsModeAddAndReregisterMessage() {
        ws.setWhDelFlag(1);

        invokePrivate("prepareChangeWarehouseRecord");

        assertEquals(1, ws.getModeFlg());
        assertEquals("Deleted warehouse - re-registering", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareChangeWarehouseRecord_notDeleted_setsModeChgAndChangeMessage() {
        ws.setWhDelFlag(0);

        invokePrivate("prepareChangeWarehouseRecord");

        assertEquals(2, ws.getModeFlg());
        assertEquals("Existing warehouse - change or PF9 delete", ws.getWkMsgLine().trim());
    }

    /* ── EDIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processWarehouseDetailScreen_ests03_acceptsFieldsAndContinues() {
        acceptFieldValues.put("WH-NAME", "Main Depot");
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processWarehouseDetailScreen");

        assertEquals("Main Depot", ws.getWhName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processWarehouseDetailScreen_ests04_showsCancelledMessage() {
        doReturn("04").when(renderer).readEndStatus();

        invokePrivate("processWarehouseDetailScreen");

        assertEquals("Cancelled", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processWarehouseDetailScreen_ests09_modeChg_callsDeleteWithConfirm() {
        ws.setModeFlg(2);
        acceptFieldValues.put("SC-CONF", "N");
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("processWarehouseDetailScreen");

        assertEquals("Delete cancelled", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processWarehouseDetailScreen_ests09_modeAdd_showsNothingToDeleteMessage() {
        ws.setModeFlg(1);
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("processWarehouseDetailScreen");

        assertEquals("Nothing to delete", ws.getWkMsgLine().trim());
        assertFalse(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processWarehouseDetailScreen_ests00_validationFails_doesNotSave() {
        acceptFieldValues.put("WH-NAME", "");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("processWarehouseDetailScreen");

        assertEquals(1, ws.getErrFlg());
        verify(whsefSpy, never()).write();
        verify(whsefSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processWarehouseDetailScreen_ests00_validationPasses_savesAddedWarehouse() {
        ws.setModeFlg(1);
        acceptFieldValues.put("WH-NAME", "Main Depot");
        acceptFieldValues.put("WH-TYPE", "1");
        acceptFieldValues.put("WH-MANAGER", "0");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("processWarehouseDetailScreen");

        assertEquals(0, ws.getErrFlg());
        verify(whsefSpy).write();
        assertEquals("Warehouse added", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processWarehouseDetailScreen_estsOther_showsInvalidKeyMessage() {
        doReturn("07").when(renderer).readEndStatus();

        invokePrivate("processWarehouseDetailScreen");

        assertEquals("Invalid function key", ws.getWkMsgLine().trim());
    }

    /* ── VAL-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateWarehouseFields_blankName_setsErrorAndReturns() {
        ws.setWhName(" ");

        invokePrivate("validateWarehouseFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Name is required", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateWarehouseFields_typeBelowRange_setsError() {
        ws.setWhName("Main Depot");
        ws.setWhType(0);

        invokePrivate("validateWarehouseFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Type must be 1-3", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateWarehouseFields_typeAboveRange_setsError() {
        ws.setWhName("Main Depot");
        ws.setWhType(4);

        invokePrivate("validateWarehouseFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Type must be 1-3", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateWarehouseFields_managerNotFound_setsError() {
        ws.setWhName("Main Depot");
        ws.setWhType(1);
        ws.setWhManager(30);
        doReturn(true).when(staffSpy).isInvalidKey();

        invokePrivate("validateWarehouseFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Manager code not found", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateWarehouseFields_managerFound_setsMgrNameAndNoError() {
        ws.setWhName("Main Depot");
        ws.setWhType(1);
        ws.setWhManager(30);
        ws.setSfName("Taro Yamada");
        doReturn(false).when(staffSpy).isInvalidKey();

        invokePrivate("validateWarehouseFields");

        assertEquals(0, ws.getErrFlg());
        assertEquals("Taro Yamada", ws.getWkMgrName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateWarehouseFields_allValidManagerZero_noError() {
        ws.setWhName("Main Depot");
        ws.setWhType(1);
        ws.setWhManager(0);

        invokePrivate("validateWarehouseFields");

        assertEquals(0, ws.getErrFlg());
        verify(staffSpy, never()).readByKey(any());
    }

    /* ── VAL-999 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void showValidationMessage_errFlgSet_displaysMessage() {
        ws.setErrFlg(1);

        invokePrivate("showValidationMessage");

        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void showValidationMessage_errFlgNotSet_noDisplay() {
        ws.setErrFlg(0);

        invokePrivate("showValidationMessage");

        assertFalse(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    /* ── SAVE-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveWarehouseRecord_modeAdd_writeSuccess_showsAddedMessage() {
        ws.setModeFlg(1);
        ws.setWhDelFlag(1);

        invokePrivate("saveWarehouseRecord");

        assertEquals(0, ws.getWhDelFlag());
        verify(whsefSpy).write();
        assertEquals("Warehouse added", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveWarehouseRecord_modeAdd_writeInvalidKey_showsDuplicateMessage() {
        ws.setModeFlg(1);
        doReturn(true).when(whsefSpy).isInvalidKey();

        invokePrivate("saveWarehouseRecord");

        assertEquals("Write failed - duplicate", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveWarehouseRecord_modeChg_rewriteSuccess_showsUpdatedMessage() {
        ws.setModeFlg(2);

        invokePrivate("saveWarehouseRecord");

        verify(whsefSpy).rewrite();
        assertEquals("Warehouse updated", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveWarehouseRecord_modeChg_rewriteInvalidKey_showsUpdateFailedMessage() {
        ws.setModeFlg(2);
        doReturn(true).when(whsefSpy).isInvalidKey();

        invokePrivate("saveWarehouseRecord");

        assertEquals("Update failed", ws.getWkMsgLine().trim());
    }

    /* ── DEL-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processWarehouseDeletion_confirmY_deletesAndRewrites() {
        acceptFieldValues.put("SC-CONF", "Y");

        invokePrivate("processWarehouseDeletion");

        assertEquals(1, ws.getWhDelFlag());
        verify(whsefSpy).rewrite();
        assertEquals("Warehouse deleted", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processWarehouseDeletion_confirmLowercaseY_deletesAndRewrites() {
        acceptFieldValues.put("SC-CONF", "y");

        invokePrivate("processWarehouseDeletion");

        assertEquals(1, ws.getWhDelFlag());
        verify(whsefSpy).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processWarehouseDeletion_confirmN_cancelsWithoutRewrite() {
        acceptFieldValues.put("SC-CONF", "N");

        invokePrivate("processWarehouseDeletion");

        assertEquals("Delete cancelled", ws.getWkMsgLine().trim());
        verify(whsefSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processWarehouseDeletion_confirmYRewriteInvalidKey_showsDeleteFailed() {
        acceptFieldValues.put("SC-CONF", "Y");
        doReturn(true).when(whsefSpy).isInvalidKey();

        invokePrivate("processWarehouseDeletion");

        assertEquals("Delete failed", ws.getWkMsgLine().trim());
    }

    /* ── LOOK-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupManagerName_managerZero_blanksName() {
        ws.setWhManager(0);

        invokePrivate("lookupManagerName");

        assertEquals("", ws.getWkMgrName().trim());
        verify(staffSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupManagerName_found_setsName() {
        ws.setWhManager(30);
        ws.setSfName("Taro Yamada");
        doReturn(false).when(staffSpy).isInvalidKey();

        invokePrivate("lookupManagerName");

        assertEquals("Taro Yamada", ws.getWkMgrName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupManagerName_notFound_setsUnknownName() {
        ws.setWhManager(30);
        doReturn(true).when(staffSpy).isInvalidKey();

        invokePrivate("lookupManagerName");

        assertEquals("??? unknown manager", ws.getWkMgrName().trim());
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void closeAllFiles_closesBothFiles() {
        invokePrivate("closeAllFiles");

        verify(whsefSpy).close();
        verify(staffSpy).close();
    }
}

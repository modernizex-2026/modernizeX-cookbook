package com.sakura.ms0060.service;

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
import com.sakura.ms0060.domain.Ms0060FieldAccess;
import com.sakura.ms0060.runtime.Ms0060Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.ScreenModels;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.CatgfDataset;
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
 * Unit tests for {@link Ms0060Service}, derived from COBOL program MS0060 (category master
 * maintenance). Ground truth for inputs/expected values: MS0060.cob PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Ms0060ServiceTest {

    @Spy private CatgfDataset catgfSpy = new CatgfDataset();

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;
    @Mock private ScreenRendererInstance renderer;

    private Ms0060Service service;
    private Ms0060FieldAccess ws;

    private final List<String> screenInteractions = new ArrayList<>();
    private final Map<String, String> acceptFieldValues = new HashMap<>();

    /** Subclass swapping in the spy dataset since Ms0060Datasets builds its own real file. */
    private static class TestDatasets extends Ms0060Datasets {
        private final CatgfDataset catgf;

        TestDatasets(CatgfDataset catgf) {
            this.catgf = catgf;
        }

        @Override
        public CatgfDataset getCatgf() {
            return catgf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Ms0060Datasets fileSet = new TestDatasets(catgfSpy);
        service = new Ms0060Service(fileSet, dateutService, abortxService, renderer);
        service.setRenderer(renderer);
        ws = getWs();

        doNothing().when(catgfSpy).open(any());
        doNothing().when(catgfSpy).close();
        doNothing().when(catgfSpy).write();
        doNothing().when(catgfSpy).rewrite();
        doNothing().when(catgfSpy).setRecord();
        doReturn("00").when(catgfSpy).getFileStatus();
        doReturn(false).when(catgfSpy).isInvalidKey();
        doReturn(true).when(catgfSpy).readByKey(any());

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

    private Ms0060FieldAccess getWs() throws Exception {
        Field f = Ms0060Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Ms0060FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Ms0060Service.class.getDeclaredMethod(name);
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
            Method m = Ms0060Service.class.getDeclaredMethod(name, paramType);
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
        Method m = Ms0060Service.class.getDeclaredMethod("getFileSet");
        m.setAccessible(true);
        Object result = m.invoke(service);
        assertTrue(result instanceof Ms0060Datasets);
    }

    /* ── INIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_success_setsHeaderFieldsAndOpensFile() {
        invokePrivate("initializeProgram");

        assertEquals("MS0060", ws.getWkProgid());
        assertEquals("Category Master Maintenance", ws.getWkTitle().trim());
        assertEquals("ENTER=Read  PF9=Delete  PF3=End", ws.getWkFkeyLine().trim());
        assertEquals(20260918, ws.getWkSysymd());
        assertEquals(20260918, ws.getWkSysdate());
        verify(catgfSpy).open(FileOpenMode.IO);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_status35_reopensAsOutputThenIO() {
        doReturn("35", "00", "00", "00").when(catgfSpy).getFileStatus();

        invokePrivate("initializeProgram");

        verify(catgfSpy, times(2)).open(FileOpenMode.IO);
        verify(catgfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(catgfSpy, times(1)).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_status30_reopensAsOutputThenIO() {
        doReturn("30", "00", "00", "00").when(catgfSpy).getFileStatus();

        invokePrivate("initializeProgram");

        verify(catgfSpy, times(2)).open(FileOpenMode.IO);
        verify(catgfSpy, times(1)).open(FileOpenMode.OUTPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_errorStatus_aborts() {
        doReturn("23").when(catgfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeProgram"));

        assertEquals("CATGF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── ABEND-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abortOnFileOpenError_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortOnFileOpenError"));

        assertEquals("MS0060", ws.getKaProgid());
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
        verify(catgfSpy).close();
    }

    /* ── MAIN-RTN-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainScreenCycle_ests03_setsEndFlg() {
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("runMainScreenCycle");

        assertEquals(1, ws.getEndFlg());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainScreenCycle_estsOther_showsInvalidKeyMessage() {
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("runMainScreenCycle");

        assertEquals(0, ws.getEndFlg());
        assertEquals("Invalid function key", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainScreenCycle_ests00_ctCodeZero_showsMustNotBeZeroMessage() {
        acceptFieldValues.put("SC-KEY", "0");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("runMainScreenCycle");

        assertEquals("Category code must not be zero", ws.getWkMsgLine().trim());
        verify(catgfSpy, never()).readByKey(any());
    }

    /* ── CLEAR-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearWorkingRecord_resetsWorkFields() {
        ws.setWkParentName("dirty");
        ws.setWkConfirm("Y");

        invokePrivate("clearWorkingRecord");

        verify(catgfSpy).setRecord();
        assertEquals("", ws.getWkParentName().trim());
        assertEquals("", ws.getWkConfirm().trim());
    }

    /* ── PKEY-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processCategoryKeyEntry_ctCodeZero_showsMessageAndReturnsWithoutRead() {
        ws.setCtCode(0);

        invokePrivate("processCategoryKeyEntry");

        assertEquals("Category code must not be zero", ws.getWkMsgLine().trim());
        verify(catgfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processCategoryKeyEntry_invalidKey_setsUpAddMode() {
        ws.setCtCode(100);
        doReturn(true).when(catgfSpy).isInvalidKey();
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processCategoryKeyEntry");

        assertEquals(1, ws.getModeFlg());
        assertEquals(100, ws.getCtCode());
        assertEquals("New category - enter details", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processCategoryKeyEntry_validKey_setsUpChangeMode() {
        ws.setCtCode(100);
        ws.setCtDelFlag(0);
        doReturn(false).when(catgfSpy).isInvalidKey();
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processCategoryKeyEntry");

        assertEquals(2, ws.getModeFlg());
        assertEquals("Existing category - change or PF9 delete", ws.getWkMsgLine().trim());
    }

    /* ── SADD-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareNewCategoryEntry_setsModeAddCodeAndMessage() {
        ws.setWkSaveCode(555);

        invokePrivate("prepareNewCategoryEntry");

        assertEquals(1, ws.getModeFlg());
        assertEquals(555, ws.getCtCode());
        assertEquals(1, ws.getCtLevel());
        assertEquals("New category - enter details", ws.getWkMsgLine().trim());
        verify(catgfSpy).setRecord();
    }

    /* ── SCHG-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareExistingCategoryEntry_deletedCategory_setsModeAddAndReregisterMessage() {
        ws.setCtDelFlag(1);

        invokePrivate("prepareExistingCategoryEntry");

        assertEquals(1, ws.getModeFlg());
        assertEquals("Deleted category - re-registering", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareExistingCategoryEntry_notDeleted_setsModeChgAndChangeMessage() {
        ws.setCtDelFlag(0);

        invokePrivate("prepareExistingCategoryEntry");

        assertEquals(2, ws.getModeFlg());
        assertEquals("Existing category - change or PF9 delete", ws.getWkMsgLine().trim());
    }

    /* ── EDIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processCategoryEditScreen_ests03_acceptsFieldsAndContinues() {
        acceptFieldValues.put("CT-NAME", "Beverages");
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processCategoryEditScreen");

        assertEquals("Beverages", ws.getCtName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processCategoryEditScreen_ests04_showsCancelledMessage() {
        doReturn("04").when(renderer).readEndStatus();

        invokePrivate("processCategoryEditScreen");

        assertEquals("Cancelled", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processCategoryEditScreen_ests09_modeChg_callsDeleteWithConfirm() {
        ws.setModeFlg(2);
        acceptFieldValues.put("SC-CONF", "N");
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("processCategoryEditScreen");

        assertEquals("Delete cancelled", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processCategoryEditScreen_ests09_modeAdd_showsNothingToDeleteMessage() {
        ws.setModeFlg(1);
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("processCategoryEditScreen");

        assertEquals("Nothing to delete", ws.getWkMsgLine().trim());
        assertFalse(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processCategoryEditScreen_ests00_validationFails_doesNotSave() {
        acceptFieldValues.put("CT-NAME", "");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("processCategoryEditScreen");

        assertEquals(1, ws.getErrFlg());
        verify(catgfSpy, never()).write();
        verify(catgfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processCategoryEditScreen_ests00_validationPasses_savesAddedCategory() {
        ws.setModeFlg(1);
        acceptFieldValues.put("CT-NAME", "Beverages");
        acceptFieldValues.put("CT-PARENT", "0");
        acceptFieldValues.put("CT-LEVEL", "1");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("processCategoryEditScreen");

        assertEquals(0, ws.getErrFlg());
        verify(catgfSpy).write();
        assertEquals("Category added", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processCategoryEditScreen_estsOther_showsInvalidKeyMessage() {
        doReturn("07").when(renderer).readEndStatus();

        invokePrivate("processCategoryEditScreen");

        assertEquals("Invalid function key", ws.getWkMsgLine().trim());
    }

    /* ── VAL-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateCategoryFields_blankName_setsErrorAndReturns() {
        ws.setCtName(" ");

        invokePrivate("validateCategoryFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Name is required", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateCategoryFields_validNameNoParent_noError() {
        ws.setCtName("Beverages");
        ws.setCtParent(0);

        invokePrivate("validateCategoryFields");

        assertEquals(0, ws.getErrFlg());
        assertFalse(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateCategoryFields_parentInvalid_setsErrorAndDisplays() {
        ws.setCtName("Beverages");
        ws.setCtCode(100);
        ws.setCtParent(200);
        doReturn(true).when(catgfSpy).isInvalidKey();

        invokePrivate("validateCategoryFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Parent category not found", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    /* ── VAL-999 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayValidationMessage_errFlgSet_displaysMessage() {
        ws.setErrFlg(1);

        invokePrivate("displayValidationMessage");

        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayValidationMessage_errFlgNotSet_noDisplay() {
        ws.setErrFlg(0);

        invokePrivate("displayValidationMessage");

        assertFalse(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    /* ── CHKP-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateParentCategory_parentZero_returnsWithoutRead() {
        ws.setCtParent(0);

        invokePrivate("validateParentCategory");

        assertEquals(0, ws.getErrFlg());
        verify(catgfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateParentCategory_parentIsSelf_setsError() {
        ws.setCtCode(100);
        ws.setCtParent(100);

        invokePrivate("validateParentCategory");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Parent cannot be itself", ws.getWkMsgLine().trim());
        verify(catgfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateParentCategory_parentNotFound_setsError() {
        ws.setCtCode(100);
        ws.setCtParent(200);
        doReturn(true).when(catgfSpy).isInvalidKey();

        invokePrivate("validateParentCategory");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Parent category not found", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateParentCategory_parentDeleted_setsError() {
        ws.setCtCode(100);
        ws.setCtParent(200);
        doReturn(false).when(catgfSpy).isInvalidKey();
        doAnswer(
                        inv -> {
                            ws.setCtDelFlag(1);
                            return true;
                        })
                .when(catgfSpy)
                .readByKey(any());

        invokePrivate("validateParentCategory");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Parent category is deleted", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateParentCategory_found_setsParentNameAndRestoresCurrentRecord() {
        ws.setCtCode(100);
        ws.setCtName("Original Name");
        ws.setCtParent(200);
        doReturn(false).when(catgfSpy).isInvalidKey();
        doAnswer(
                        inv -> {
                            ws.setCtName("Parent Category");
                            ws.setCtDelFlag(0);
                            return true;
                        })
                .when(catgfSpy)
                .readByKey(any());

        invokePrivate("validateParentCategory");

        assertEquals(0, ws.getErrFlg());
        assertEquals("Parent Category", ws.getWkParentName().trim());
        // COBOL saves CT-REC before the parent read and restores it after —
        // the currently-edited record's own code/name must come back unchanged.
        assertEquals(100, ws.getCtCode());
        assertEquals("Original Name", ws.getCtName().trim());
    }

    /* ── SAVE-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveCategoryRecord_modeAdd_writeSuccess_showsAddedMessage() {
        ws.setModeFlg(1);

        invokePrivate("saveCategoryRecord");

        assertEquals(0, ws.getCtDelFlag());
        verify(catgfSpy).write();
        assertEquals("Category added", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveCategoryRecord_modeAdd_writeInvalidKey_showsDuplicateMessage() {
        ws.setModeFlg(1);
        doReturn(true).when(catgfSpy).isInvalidKey();

        invokePrivate("saveCategoryRecord");

        assertEquals("Write failed - duplicate", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveCategoryRecord_modeChg_rewriteSuccess_showsUpdatedMessage() {
        ws.setModeFlg(2);

        invokePrivate("saveCategoryRecord");

        verify(catgfSpy).rewrite();
        assertEquals("Category updated", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveCategoryRecord_modeChg_rewriteInvalidKey_showsUpdateFailedMessage() {
        ws.setModeFlg(2);
        doReturn(true).when(catgfSpy).isInvalidKey();

        invokePrivate("saveCategoryRecord");

        assertEquals("Update failed", ws.getWkMsgLine().trim());
    }

    /* ── DEL-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteCategory_confirmY_deletesAndRewrites() {
        acceptFieldValues.put("SC-CONF", "Y");

        invokePrivate("confirmAndDeleteCategory");

        assertEquals(1, ws.getCtDelFlag());
        verify(catgfSpy).rewrite();
        assertEquals("Category deleted", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteCategory_confirmLowercaseY_deletesAndRewrites() {
        acceptFieldValues.put("SC-CONF", "y");

        invokePrivate("confirmAndDeleteCategory");

        assertEquals(1, ws.getCtDelFlag());
        verify(catgfSpy).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteCategory_confirmN_cancelsWithoutRewrite() {
        acceptFieldValues.put("SC-CONF", "N");

        invokePrivate("confirmAndDeleteCategory");

        assertEquals("Delete cancelled", ws.getWkMsgLine().trim());
        verify(catgfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteCategory_confirmYRewriteInvalidKey_showsDeleteFailed() {
        acceptFieldValues.put("SC-CONF", "Y");
        doReturn(true).when(catgfSpy).isInvalidKey();

        invokePrivate("confirmAndDeleteCategory");

        assertEquals("Delete failed", ws.getWkMsgLine().trim());
    }

    /* ── LOOK-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupParentCategoryName_ctParentZero_blanksName() {
        ws.setCtParent(0);
        ws.setWkParentName("dirty");

        invokePrivate("lookupParentCategoryName");

        assertEquals("", ws.getWkParentName().trim());
        verify(catgfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupParentCategoryName_found_setsParentNameAndRestoresCurrentRecord() {
        ws.setCtCode(100);
        ws.setCtName("Original Name");
        ws.setCtParent(200);
        doReturn(false).when(catgfSpy).isInvalidKey();
        doAnswer(
                        inv -> {
                            ws.setCtName("Sales");
                            return true;
                        })
                .when(catgfSpy)
                .readByKey(any());

        invokePrivate("lookupParentCategoryName");

        assertEquals("Sales", ws.getWkParentName().trim());
        assertEquals(100, ws.getCtCode());
        assertEquals("Original Name", ws.getCtName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupParentCategoryName_notFound_setsUnknownName() {
        ws.setCtCode(100);
        ws.setCtParent(200);
        doReturn(true).when(catgfSpy).isInvalidKey();

        invokePrivate("lookupParentCategoryName");

        assertEquals("??? unknown parent", ws.getWkParentName().trim());
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void closeCategoryFile_closesFile() {
        invokePrivate("closeCategoryFile");

        verify(catgfSpy).close();
    }
}

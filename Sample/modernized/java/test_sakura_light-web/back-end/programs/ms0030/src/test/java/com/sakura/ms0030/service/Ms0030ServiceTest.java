package com.sakura.ms0030.service;

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
import com.sakura.ms0030.domain.Ms0030FieldAccess;
import com.sakura.ms0030.runtime.Ms0030Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.ScreenModels;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.CatgfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.SuppfDataset;
import com.sakura.runtime.io.WhsefDataset;
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
 * Unit tests for {@link Ms0030Service}, derived from COBOL program MS0030 (product master
 * maintenance). Ground truth for inputs/expected values: MS0030.cob PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Ms0030ServiceTest {

    @Spy private ProdfDataset prodfSpy = new ProdfDataset();
    @Spy private CatgfDataset catgfSpy = new CatgfDataset();
    @Spy private SuppfDataset suppfSpy = new SuppfDataset();
    @Spy private WhsefDataset whsefSpy = new WhsefDataset();

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;
    @Mock private ScreenRendererInstance renderer;

    private Ms0030Service service;
    private Ms0030FieldAccess ws;

    private final List<String> screenInteractions = new ArrayList<>();
    private final Map<String, String> acceptFieldValues = new HashMap<>();

    /** Subclass swapping in the spy datasets since Ms0030Datasets builds its own real files. */
    private static class TestDatasets extends Ms0030Datasets {
        private final ProdfDataset prodf;
        private final CatgfDataset catgf;
        private final SuppfDataset suppf;
        private final WhsefDataset whsef;

        TestDatasets(
                ProdfDataset prodf, CatgfDataset catgf, SuppfDataset suppf, WhsefDataset whsef) {
            this.prodf = prodf;
            this.catgf = catgf;
            this.suppf = suppf;
            this.whsef = whsef;
        }

        @Override
        public ProdfDataset getProdf() {
            return prodf;
        }

        @Override
        public CatgfDataset getCatgf() {
            return catgf;
        }

        @Override
        public SuppfDataset getSuppf() {
            return suppf;
        }

        @Override
        public WhsefDataset getWhsef() {
            return whsef;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Ms0030Datasets fileSet = new TestDatasets(prodfSpy, catgfSpy, suppfSpy, whsefSpy);
        service = new Ms0030Service(fileSet, dateutService, abortxService, renderer);
        service.setRenderer(renderer);
        ws = getWs();

        for (RawDatasetBase spy : new RawDatasetBase[] {prodfSpy, catgfSpy, suppfSpy, whsefSpy}) {
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

    private Ms0030FieldAccess getWs() throws Exception {
        Field f = Ms0030Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Ms0030FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Ms0030Service.class.getDeclaredMethod(name);
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
            Method m = Ms0030Service.class.getDeclaredMethod(name, paramType);
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
        Method m = Ms0030Service.class.getDeclaredMethod("getFileSet");
        m.setAccessible(true);
        Object result = m.invoke(service);
        assertTrue(result instanceof Ms0030Datasets);
    }

    /* ── INIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_success_setsHeaderFieldsAndOpensAllFiles() {
        invokePrivate("initializeProgram");

        assertEquals("MS0030", ws.getWkProgid().trim());
        assertEquals("Product Master Maintenance", ws.getWkTitle().trim());
        assertEquals("ENTER=Read  PF9=Delete  PF3=End", ws.getWkFkeyLine().trim());
        assertEquals(20260918, ws.getWkSysymd());
        assertEquals(20260918, ws.getWkSysdate());
        verify(prodfSpy).open(FileOpenMode.IO);
        verify(catgfSpy).open(FileOpenMode.INPUT);
        verify(suppfSpy).open(FileOpenMode.INPUT);
        verify(whsefSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_prodfStatus35_reopensAsOutputThenIO() {
        doReturn("35", "00", "00", "00").when(prodfSpy).getFileStatus();

        invokePrivate("initializeProgram");

        verify(prodfSpy, times(2)).open(FileOpenMode.IO);
        verify(prodfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(prodfSpy, times(1)).close();
        verify(catgfSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_prodfStatus30_reopensAsOutputThenIO() {
        doReturn("30", "00", "00", "00").when(prodfSpy).getFileStatus();

        invokePrivate("initializeProgram");

        verify(prodfSpy, times(2)).open(FileOpenMode.IO);
        verify(prodfSpy, times(1)).open(FileOpenMode.OUTPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_prodfErrorStatus_abortsAndSkipsRemainingFiles() {
        doReturn("23").when(prodfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeProgram"));

        assertEquals("PRODF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(catgfSpy, never()).open(any());
        verify(suppfSpy, never()).open(any());
        verify(whsefSpy, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_catgfErrorStatus_abortsAfterProdfOpened() {
        doReturn("00").when(prodfSpy).getFileStatus();
        doReturn("23").when(catgfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeProgram"));

        assertEquals("CATGF", ws.getKaFile().trim());
        verify(suppfSpy, never()).open(any());
        verify(whsefSpy, never()).open(any());
    }

    /* ── ABEND-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abortWithFileOpenError_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortWithFileOpenError"));

        assertEquals("MS0030", ws.getKaProgid().trim());
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
        verify(prodfSpy).close();
        verify(catgfSpy).close();
        verify(suppfSpy).close();
        verify(whsefSpy).close();
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
    void displayMainScreenAndDispatch_ests00_prCodeZero_showsMustNotBeZeroMessage() {
        acceptFieldValues.put("SC-KEY", "0");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("displayMainScreenAndDispatch");

        assertEquals("Product code must not be zero", ws.getWkMsgLine().trim());
        verify(prodfSpy, never()).readByKey(any());
    }

    /* ── CLEAR-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearScreenFields_resetsWorkFieldsToDefaults() {
        ws.setWkCatgName("dirty");
        ws.setWkSuppName("dirty");
        ws.setWkWhseName("dirty");
        ws.setWkConfirm("Y");

        invokePrivate("clearScreenFields");

        verify(prodfSpy).setRecord();
        assertEquals("", ws.getWkCatgName().trim());
        assertEquals("", ws.getWkSuppName().trim());
        assertEquals("", ws.getWkWhseName().trim());
        assertEquals("", ws.getWkConfirm().trim());
    }

    /* ── PKEY-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processProductKeyEntry_prCodeZero_showsMessageAndReturnsWithoutRead() {
        ws.setPrCode(0);

        invokePrivate("processProductKeyEntry");

        assertEquals("Product code must not be zero", ws.getWkMsgLine().trim());
        verify(prodfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processProductKeyEntry_prodfInvalidKey_setsUpAddMode() {
        ws.setPrCode(100);
        doReturn(true).when(prodfSpy).isInvalidKey();
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processProductKeyEntry");

        assertEquals(1, ws.getModeFlg());
        assertEquals(100, ws.getPrCode());
        assertEquals("New product - enter details", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processProductKeyEntry_prodfValidKey_setsUpChangeMode() {
        ws.setPrCode(100);
        ws.setPrDelFlag(0);
        doReturn(false).when(prodfSpy).isInvalidKey();
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processProductKeyEntry");

        assertEquals(2, ws.getModeFlg());
        assertEquals("Existing product - change or PF9 delete", ws.getWkMsgLine().trim());
    }

    /* ── SADD-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeNewProductDefaults_setsModeAddCodeTaxAndStockDefaults() {
        ws.setWkSaveCode(555);

        invokePrivate("initializeNewProductDefaults");

        assertEquals(1, ws.getModeFlg());
        assertEquals(555, ws.getPrCode());
        assertEquals(1, ws.getPrTaxCategory());
        assertEquals(1, ws.getPrStockMng());
        assertEquals("New product - enter details", ws.getWkMsgLine().trim());
        verify(prodfSpy).setRecord();
    }

    /* ── SCHG-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareExistingProductChange_deletedProduct_setsModeAddAndReregisterMessage() {
        ws.setPrDelFlag(1);

        invokePrivate("prepareExistingProductChange");

        assertEquals(1, ws.getModeFlg());
        assertEquals("Deleted product - re-registering", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void prepareExistingProductChange_notDeleted_setsModeChgAndChangeMessage() {
        ws.setPrDelFlag(0);

        invokePrivate("prepareExistingProductChange");

        assertEquals(2, ws.getModeFlg());
        assertEquals("Existing product - change or PF9 delete", ws.getWkMsgLine().trim());
    }

    /* ── EDIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayEditScreenAndDispatch_ests03_acceptsFieldsAndContinues() {
        acceptFieldValues.put("PR-NAME", "Widget");
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("displayEditScreenAndDispatch");

        assertEquals("Widget", ws.getPrName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayEditScreenAndDispatch_ests04_showsCancelledMessage() {
        doReturn("04").when(renderer).readEndStatus();

        invokePrivate("displayEditScreenAndDispatch");

        assertEquals("Cancelled", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayEditScreenAndDispatch_ests09_modeChg_callsDeleteWithConfirm() {
        ws.setModeFlg(2);
        acceptFieldValues.put("SC-CONF", "N");
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("displayEditScreenAndDispatch");

        assertEquals("Delete cancelled", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayEditScreenAndDispatch_ests09_modeAdd_showsNothingToDeleteMessage() {
        ws.setModeFlg(1);
        doReturn("09").when(renderer).readEndStatus();

        invokePrivate("displayEditScreenAndDispatch");

        assertEquals("Nothing to delete", ws.getWkMsgLine().trim());
        assertFalse(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayEditScreenAndDispatch_ests00_validationFails_doesNotSave() {
        acceptFieldValues.put("PR-NAME", "");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("displayEditScreenAndDispatch");

        assertEquals(1, ws.getErrFlg());
        verify(prodfSpy, never()).write();
        verify(prodfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayEditScreenAndDispatch_ests00_validationPasses_savesAddedProduct() {
        ws.setModeFlg(1);
        acceptFieldValues.put("PR-NAME", "Widget");
        acceptFieldValues.put("PR-CATEGORY", "5");
        acceptFieldValues.put("PR-TAX-CATEGORY", "1");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("displayEditScreenAndDispatch");

        assertEquals(0, ws.getErrFlg());
        verify(prodfSpy).write();
        assertEquals("Product added", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayEditScreenAndDispatch_estsOther_showsInvalidKeyMessage() {
        doReturn("07").when(renderer).readEndStatus();

        invokePrivate("displayEditScreenAndDispatch");

        assertEquals("Invalid function key", ws.getWkMsgLine().trim());
    }

    /* ── VAL-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateProductFields_blankName_setsErrorAndReturns() {
        ws.setPrName(" ");

        invokePrivate("validateProductFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Name is required", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateProductFields_categoryZero_setsError() {
        ws.setPrName("Widget");
        ws.setPrCategory(0);

        invokePrivate("validateProductFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Category is required", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateProductFields_categoryNotFound_setsError() {
        ws.setPrName("Widget");
        ws.setPrCategory(10);
        doReturn(true).when(catgfSpy).isInvalidKey();

        invokePrivate("validateProductFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Category code not found", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateProductFields_taxCategoryOutOfRange_setsError() {
        ws.setPrName("Widget");
        ws.setPrCategory(10);
        doReturn(false).when(catgfSpy).isInvalidKey();
        ws.setPrTaxCategory(4);

        invokePrivate("validateProductFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Tax category must be 1-3", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateProductFields_stockMngGreaterThanOne_setsError() {
        ws.setPrName("Widget");
        ws.setPrCategory(10);
        doReturn(false).when(catgfSpy).isInvalidKey();
        ws.setPrTaxCategory(1);
        ws.setPrStockMng(2);

        invokePrivate("validateProductFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Stock mgmt must be 0 or 1", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateProductFields_negativeStdCost_setsError() {
        setBasicValidProductFields();
        ws.setPrStdCost(new BigDecimal("-1"));

        invokePrivate("validateProductFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Cost cannot be negative", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateProductFields_negativeLastCost_setsError() {
        setBasicValidProductFields();
        ws.setPrLastCost(new BigDecimal("-1"));

        invokePrivate("validateProductFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Cost cannot be negative", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateProductFields_negativeListPrice_setsError() {
        setBasicValidProductFields();
        ws.setPrListPrice(new BigDecimal("-1"));

        invokePrivate("validateProductFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("List price cannot be negative", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateProductFields_negativeRankPrice_setsErrorAndFallsThrough() {
        setBasicValidProductFields();
        ws.setPrRankPrice(3, new BigDecimal("-5"));

        invokePrivate("validateProductFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Rank price cannot be negative", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-MSG"));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateProductFields_negativeSafetyStock_setsError() {
        setBasicValidProductFields();
        ws.setPrSafetyStock(new BigDecimal("-1"));

        invokePrivate("validateProductFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Stock levels cannot be negative", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateProductFields_negativeReorderPoint_setsError() {
        setBasicValidProductFields();
        ws.setPrReorderPoint(new BigDecimal("-1"));

        invokePrivate("validateProductFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Stock levels cannot be negative", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateProductFields_negativeReorderQty_setsError() {
        setBasicValidProductFields();
        ws.setPrReorderQty(new BigDecimal("-1"));

        invokePrivate("validateProductFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Reorder qty cannot be negative", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateProductFields_supplierNotFound_setsError() {
        setBasicValidProductFields();
        ws.setPrDfltSupp(20);
        doReturn(true).when(suppfSpy).isInvalidKey();

        invokePrivate("validateProductFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Default supplier not found", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateProductFields_warehouseNotFound_setsError() {
        setBasicValidProductFields();
        ws.setPrDfltSupp(0);
        ws.setPrDfltWhse(30);
        doReturn(true).when(whsefSpy).isInvalidKey();

        invokePrivate("validateProductFields");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Default warehouse not found", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateProductFields_allValid_noError() {
        setBasicValidProductFields();

        invokePrivate("validateProductFields");

        assertEquals(0, ws.getErrFlg());
    }

    private void setBasicValidProductFields() {
        ws.setPrName("Widget");
        ws.setPrCategory(10);
        doReturn(false).when(catgfSpy).isInvalidKey();
        ws.setPrTaxCategory(1);
        ws.setPrStockMng(1);
        ws.setPrStdCost(BigDecimal.ZERO);
        ws.setPrLastCost(BigDecimal.ZERO);
        ws.setPrListPrice(BigDecimal.ZERO);
        for (int i = 1; i <= 5; i++) {
            ws.setPrRankPrice(i, BigDecimal.ZERO);
        }
        ws.setPrSafetyStock(BigDecimal.ZERO);
        ws.setPrReorderPoint(BigDecimal.ZERO);
        ws.setPrReorderQty(BigDecimal.ZERO);
        ws.setPrDfltSupp(0);
        ws.setPrDfltWhse(0);
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

    /* ── CHKS-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateDefaultSupplier_codeZero_returnsWithoutRead() {
        ws.setPrDfltSupp(0);

        invokePrivate("validateDefaultSupplier");

        verify(suppfSpy, never()).readByKey(any());
        assertEquals(0, ws.getErrFlg());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateDefaultSupplier_found_setsSupplierName() {
        ws.setPrDfltSupp(20);
        ws.setSpName("Acme Supply");
        doReturn(false).when(suppfSpy).isInvalidKey();

        invokePrivate("validateDefaultSupplier");

        assertEquals("Acme Supply", ws.getWkSuppName().trim());
        assertEquals(0, ws.getErrFlg());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateDefaultSupplier_notFound_setsError() {
        ws.setPrDfltSupp(20);
        doReturn(true).when(suppfSpy).isInvalidKey();

        invokePrivate("validateDefaultSupplier");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Default supplier not found", ws.getWkMsgLine().trim());
    }

    /* ── CHKW-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateDefaultWarehouse_codeZero_returnsWithoutRead() {
        ws.setPrDfltWhse(0);

        invokePrivate("validateDefaultWarehouse");

        verify(whsefSpy, never()).readByKey(any());
        assertEquals(0, ws.getErrFlg());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateDefaultWarehouse_found_setsWarehouseName() {
        ws.setPrDfltWhse(30);
        ws.setWhName("Central Warehouse");
        doReturn(false).when(whsefSpy).isInvalidKey();

        invokePrivate("validateDefaultWarehouse");

        assertEquals("Central Warehouse", ws.getWkWhseName().trim());
        assertEquals(0, ws.getErrFlg());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validateDefaultWarehouse_notFound_setsError() {
        ws.setPrDfltWhse(30);
        doReturn(true).when(whsefSpy).isInvalidKey();

        invokePrivate("validateDefaultWarehouse");

        assertEquals(1, ws.getErrFlg());
        assertEquals("Default warehouse not found", ws.getWkMsgLine().trim());
    }

    /* ── SAVE-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveProductRecord_modeAdd_writeSuccess_setsAddDateUserAndWrites() {
        ws.setModeFlg(1);
        ws.setWkSysdate(20260918);
        ws.setWkUserCode(7);

        invokePrivate("saveProductRecord");

        assertEquals(20260918, ws.getPrAddDate());
        assertEquals(7, ws.getPrAddUser());
        assertEquals(0, ws.getPrDelFlag());
        verify(prodfSpy).write();
        assertEquals("Product added", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveProductRecord_modeAdd_writeInvalidKey_showsDuplicateMessage() {
        ws.setModeFlg(1);
        doReturn(true).when(prodfSpy).isInvalidKey();

        invokePrivate("saveProductRecord");

        assertEquals("Write failed - duplicate", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveProductRecord_modeChg_rewriteSuccess_showsUpdatedMessage() {
        ws.setModeFlg(2);

        invokePrivate("saveProductRecord");

        verify(prodfSpy).rewrite();
        assertEquals("Product updated", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void saveProductRecord_modeChg_rewriteInvalidKey_showsUpdateFailedMessage() {
        ws.setModeFlg(2);
        doReturn(true).when(prodfSpy).isInvalidKey();

        invokePrivate("saveProductRecord");

        assertEquals("Update failed", ws.getWkMsgLine().trim());
    }

    /* ── DEL-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteProduct_confirmY_deletesAndRewrites() {
        acceptFieldValues.put("SC-CONF", "Y");
        ws.setWkSysdate(20260918);
        ws.setWkUserCode(9);

        invokePrivate("confirmAndDeleteProduct");

        assertEquals(1, ws.getPrDelFlag());
        verify(prodfSpy).rewrite();
        assertEquals("Product deleted", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteProduct_confirmLowercaseY_deletesAndRewrites() {
        acceptFieldValues.put("SC-CONF", "y");

        invokePrivate("confirmAndDeleteProduct");

        assertEquals(1, ws.getPrDelFlag());
        verify(prodfSpy).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteProduct_confirmN_cancelsWithoutRewrite() {
        acceptFieldValues.put("SC-CONF", "N");

        invokePrivate("confirmAndDeleteProduct");

        assertEquals("Delete cancelled", ws.getWkMsgLine().trim());
        verify(prodfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndDeleteProduct_confirmYRewriteInvalidKey_showsDeleteFailed() {
        acceptFieldValues.put("SC-CONF", "Y");
        doReturn(true).when(prodfSpy).isInvalidKey();

        invokePrivate("confirmAndDeleteProduct");

        assertEquals("Delete failed", ws.getWkMsgLine().trim());
    }

    /* ── LOOK-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void refreshRelatedNameFields_allCodesZero_blanksAllNames() {
        ws.setPrCategory(0);
        ws.setPrDfltSupp(0);
        ws.setPrDfltWhse(0);

        invokePrivate("refreshRelatedNameFields");

        assertEquals("", ws.getWkCatgName().trim());
        assertEquals("", ws.getWkSuppName().trim());
        assertEquals("", ws.getWkWhseName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void refreshRelatedNameFields_allFound_setsAllNames() {
        ws.setPrCategory(10);
        ws.setPrDfltSupp(20);
        ws.setPrDfltWhse(30);
        ws.setCtName("Electronics");
        ws.setSpName("Acme Supply");
        ws.setWhName("Central Warehouse");
        doReturn(false).when(catgfSpy).isInvalidKey();
        doReturn(false).when(suppfSpy).isInvalidKey();
        doReturn(false).when(whsefSpy).isInvalidKey();

        invokePrivate("refreshRelatedNameFields");

        assertEquals("Electronics", ws.getWkCatgName().trim());
        assertEquals("Acme Supply", ws.getWkSuppName().trim());
        assertEquals("Central Warehouse", ws.getWkWhseName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void refreshRelatedNameFields_allNotFound_setsUnknownNames() {
        ws.setPrCategory(10);
        ws.setPrDfltSupp(20);
        ws.setPrDfltWhse(30);
        doReturn(true).when(catgfSpy).isInvalidKey();
        doReturn(true).when(suppfSpy).isInvalidKey();
        doReturn(true).when(whsefSpy).isInvalidKey();

        invokePrivate("refreshRelatedNameFields");

        assertEquals("??? unknown category", ws.getWkCatgName().trim());
        assertEquals("??? unknown supplier", ws.getWkSuppName().trim());
        assertEquals("??? unknown warehouse", ws.getWkWhseName().trim());
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void closeAllFiles_closesAllFourFiles() {
        invokePrivate("closeAllFiles");

        verify(prodfSpy).close();
        verify(catgfSpy).close();
        verify(suppfSpy).close();
        verify(whsefSpy).close();
    }
}

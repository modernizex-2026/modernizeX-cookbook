package com.sakura.pu0010.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.sakura.numgen.service.NumgenService;
import com.sakura.pu0010.domain.Pu0010FieldAccess;
import com.sakura.pu0010.runtime.Pu0010Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.ScreenModels;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.PodfDataset;
import com.sakura.runtime.io.PohfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.StokfDataset;
import com.sakura.runtime.io.SuppfDataset;
import com.sakura.runtime.linkage.DateutLinkParm;
import com.sakura.runtime.linkage.NumgenLinkParm;
import com.sakura.runtime.linkage.TaxcalLinkParm;
import com.sakura.runtime.record.RawDatasetBase;
import com.sakura.taxcal.service.TaxcalService;

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
 * Unit tests for {@link Pu0010Service}, derived from COBOL program PU0010 (purchase order entry).
 * Ground truth for inputs/expected values: PU0010.cob PROCEDURE DIVISION + copybooks WCOMMON/WMSG.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Pu0010ServiceTest {

    @Spy private PohfDataset pohfSpy = new PohfDataset();
    @Spy private PodfDataset podfSpy = new PodfDataset();
    @Spy private SuppfDataset suppfSpy = new SuppfDataset();
    @Spy private ProdfDataset prodfSpy = new ProdfDataset();
    @Spy private StokfDataset stokfSpy = new StokfDataset();

    @Mock private DateutService dateutService;
    @Mock private TaxcalService taxcalService;
    @Mock private NumgenService numgenService;
    @Mock private AbortxService abortxService;
    @Mock private ScreenRendererInstance renderer;

    private Pu0010Service service;
    private Pu0010FieldAccess ws;

    private final List<String> screenInteractions = new ArrayList<>();
    private final Map<String, String> acceptFieldValues = new HashMap<>();

    /** Subclass swapping in the spy datasets since Pu0010Datasets builds its own real files. */
    private static class TestDatasets extends Pu0010Datasets {
        private final PohfDataset pohf;
        private final PodfDataset podf;
        private final SuppfDataset suppf;
        private final ProdfDataset prodf;
        private final StokfDataset stokf;

        TestDatasets(
                PohfDataset pohf,
                PodfDataset podf,
                SuppfDataset suppf,
                ProdfDataset prodf,
                StokfDataset stokf) {
            this.pohf = pohf;
            this.podf = podf;
            this.suppf = suppf;
            this.prodf = prodf;
            this.stokf = stokf;
        }

        @Override
        public PohfDataset getPohf() {
            return pohf;
        }

        @Override
        public PodfDataset getPodf() {
            return podf;
        }

        @Override
        public SuppfDataset getSuppf() {
            return suppf;
        }

        @Override
        public ProdfDataset getProdf() {
            return prodf;
        }

        @Override
        public StokfDataset getStokf() {
            return stokf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Pu0010Datasets fileSet = new TestDatasets(pohfSpy, podfSpy, suppfSpy, prodfSpy, stokfSpy);
        service =
                new Pu0010Service(
                        fileSet,
                        dateutService,
                        taxcalService,
                        numgenService,
                        abortxService,
                        renderer);
        service.setRenderer(renderer);
        ws = getWs();

        for (RawDatasetBase spy :
                new RawDatasetBase[] {pohfSpy, podfSpy, suppfSpy, prodfSpy, stokfSpy}) {
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
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("00");
                            p.getKnum().setKnumNumber(1001L);
                            return null;
                        })
                .when(numgenService)
                .execute(any());

        doAnswer(
                        inv -> {
                            TaxcalLinkParm p = inv.getArgument(0);
                            p.getKtax().setKtStatus("00");
                            p.getKtax().setKtNet(BigDecimal.valueOf(1000));
                            p.getKtax().setKtTax(BigDecimal.valueOf(100));
                            p.getKtax().setKtGross(BigDecimal.valueOf(1100));
                            return null;
                        })
                .when(taxcalService)
                .execute(any());
    }

    @AfterEach
    void tearDown() {
        screenInteractions.clear();
        acceptFieldValues.clear();
    }

    /* ── reflection helpers ── */

    private Pu0010FieldAccess getWs() throws Exception {
        Field f = Pu0010Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Pu0010FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Pu0010Service.class.getDeclaredMethod(name);
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

    private void setupValidSupplierRecord() {
        ws.setSpDelFlag(0);
        ws.setSpName("Acme Supplies Co");
        ws.setSpTaxType(2);
    }

    private void setupValidProductRecord() {
        ws.setPrDelFlag(0);
        ws.setPrName("Widget A");
        ws.setPrStockMng(1);
        ws.setPrDfltWhse(10);
        ws.setPrLastCost(BigDecimal.ZERO);
        ws.setPrStdCost(BigDecimal.ZERO);
    }

    /* ── INIT-010 / OPENF-010 / OIPH-010 / OIPD-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_setsHeaderFieldsAndSysDate_success() {
        invokePrivate("initializeProgram");

        assertEquals("PU0010", ws.getWkProgid().trim());
        assertEquals("Purchase Order Entry", ws.getWkTitle().trim());
        assertEquals("ENTER=Next  PF3=End/Finish  PF4=Clear line", ws.getWkFkeyLine().trim());
        assertEquals(20260918, ws.getWkSysymd());
        assertEquals(20260918, ws.getWkSysdate());
        verify(pohfSpy).open(FileOpenMode.IO);
        verify(podfSpy).open(FileOpenMode.IO);
        verify(stokfSpy).open(FileOpenMode.IO);
        verify(suppfSpy).open(FileOpenMode.INPUT);
        verify(prodfSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openAllFiles_allStatus00_opensAllFilesWithoutAbort() {
        invokePrivate("openAllFiles");

        verify(pohfSpy, times(1)).open(FileOpenMode.IO);
        verify(podfSpy, times(1)).open(FileOpenMode.IO);
        verify(stokfSpy, times(1)).open(FileOpenMode.IO);
        verify(suppfSpy, times(1)).open(FileOpenMode.INPUT);
        verify(prodfSpy, times(1)).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openAllFiles_stokfStatus35_reopensAsOutputThenIO() {
        doReturn("35", "00", "00").when(stokfSpy).getFileStatus();

        invokePrivate("openAllFiles");

        verify(stokfSpy, times(2)).open(FileOpenMode.IO);
        verify(stokfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(stokfSpy, times(1)).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openAllFiles_stokfErrorStatus_abortsAndSkipsRemainingFiles() {
        doReturn("23").when(stokfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openAllFiles"));

        assertEquals("STOKF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(suppfSpy, never()).open(any());
        verify(prodfSpy, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openPoHeaderFile_status30_reopensAsOutputThenIO() {
        doReturn("30", "00", "00").when(pohfSpy).getFileStatus();

        invokePrivate("openPoHeaderFile");

        verify(pohfSpy, times(2)).open(FileOpenMode.IO);
        verify(pohfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(pohfSpy, times(1)).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openPoHeaderFile_errorStatus_abortsWithPohfFile() {
        doReturn("23").when(pohfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openPoHeaderFile"));

        assertEquals("POHF", ws.getKaFile().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openPoDetailFile_errorStatus_abortsWithPodfFile() {
        doReturn("23").when(podfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openPoDetailFile"));

        assertEquals("PODF", ws.getKaFile().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abortOnFileOpenError_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortOnFileOpenError"));

        assertEquals("PU0010", ws.getKaProgid().trim());
        assertEquals("23", ws.getKaFsts().trim());
        assertEquals("EOPEN ", ws.getKaMsgcode());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── MAIN-000 loop ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_endFlgOnFirstEntry_closesFilesAndThrowsProgramExitSignal() {
        acceptFieldValues.put("PH-SUPP", "0");
        doReturn("03").when(renderer).readEndStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

        assertEquals(1, ws.getEndFlg());
        verify(pohfSpy).close();
        verify(podfSpy).close();
        verify(stokfSpy).close();
        verify(suppfSpy).close();
        verify(prodfSpy).close();
    }

    /* ── PLOOP-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processOnePurchaseOrder_endFlgSetByHeader_returnsWithoutDetailLoop() {
        acceptFieldValues.put("PH-SUPP", "0");
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("processOnePurchaseOrder");

        assertEquals(1, ws.getEndFlg());
        verify(suppfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processOnePurchaseOrder_hdrNotOk_returnsWithoutDetailLoop() {
        acceptFieldValues.put("PH-SUPP", "0");

        invokePrivate("processOnePurchaseOrder");

        assertEquals(0, ws.getHdrOk());
        assertTrue(screenInteractions.stream().noneMatch(s -> s.equals("displayScreen:DS-DETAIL")));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processOnePurchaseOrder_hdrOkNoLinesEntered_showsDiscardMessage() {
        setupValidSupplierRecord();
        acceptFieldValues.put("PH-SUPP", "500");
        doReturn("00", "03").when(renderer).readEndStatus();

        invokePrivate("processOnePurchaseOrder");

        assertEquals(1, ws.getHdrOk());
        assertEquals(0, ws.getWkLcnt());
        assertEquals("No lines entered - PO discarded", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processOnePurchaseOrder_hdrOkOneLineEntered_callsConfirmAndSave() {
        setupValidSupplierRecord();
        setupValidProductRecord();
        acceptFieldValues.put("PH-SUPP", "500");
        acceptFieldValues.put("WK-D-PROD", "700");
        acceptFieldValues.put("WK-D-QTY", "5");
        acceptFieldValues.put("WK-D-COST", "10.00");
        acceptFieldValues.put("WK-CONFIRM", "N");
        doReturn("00", "00", "03", "00").when(renderer).readEndStatus();

        invokePrivate("processOnePurchaseOrder");

        assertEquals(1, ws.getWkLcnt());
        assertTrue(screenInteractions.contains("displayScreen:DS-CONFIRM"));
    }

    /* ── CLRP-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearPoHeaderFields_resetsWorkFieldsToDefaults() {
        ws.setWkSysdate(20260101);
        ws.setWkLcnt(5);
        ws.setWkNetTotal(999);
        ws.setHdrOk(1);
        ws.setDtlDone(1);
        ws.setWkPoNoD(123);

        invokePrivate("clearPoHeaderFields");

        assertEquals(0, ws.getWkLcnt());
        assertEquals(0, ws.getWkNetTotal());
        assertEquals(0, ws.getWkTaxTotal());
        assertEquals(0, ws.getWkGrsTotal());
        assertEquals(0, ws.getHdrOk());
        assertEquals(0, ws.getDtlDone());
        assertEquals(0, ws.getWkPoNoD());
        assertEquals(20260101, ws.getPhDate());
        assertEquals(20260101, ws.getPhDueDate());
        assertEquals(1, ws.getPhTaxType());
    }

    /* ── EHDR-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayAndAcceptPoHeader_ests03SuppZero_setsEndFlagAndSkipsValidation() {
        acceptFieldValues.put("PH-SUPP", "0");
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("displayAndAcceptPoHeader");

        assertEquals(1, ws.getEndFlg());
        verify(suppfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayAndAcceptPoHeader_ests03SuppNonZero_returnsWithoutSettingEndFlagOrValidating() {
        acceptFieldValues.put("PH-SUPP", "500");
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("displayAndAcceptPoHeader");

        assertEquals(0, ws.getEndFlg());
        assertEquals(0, ws.getHdrOk());
        verify(suppfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayAndAcceptPoHeader_normalEnter_readsFieldsAndValidates() {
        setupValidSupplierRecord();
        acceptFieldValues.put("PH-DATE", "20260101");
        acceptFieldValues.put("PH-SUPP", "500");
        acceptFieldValues.put("PH-WHSE", "10");
        acceptFieldValues.put("PH-STAFF", "42");
        acceptFieldValues.put("PH-DUE-DATE", "20260201");
        acceptFieldValues.put("PH-TAX-TYPE", "1");
        acceptFieldValues.put("PH-REMARK", "Urgent order");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("displayAndAcceptPoHeader");

        assertEquals(20260101, ws.getPhDate());
        assertEquals(500, ws.getPhSupp());
        assertEquals(10, ws.getPhWhse());
        assertEquals(42, ws.getPhStaff());
        assertEquals("Urgent order", ws.getPhRemark().trim());
        assertEquals(1, ws.getHdrOk());
    }

    /* ── VHDR-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePoHeader_supplierCodeZero_setsMessageAndReturns() {
        ws.setPhSupp(0);

        invokePrivate("validatePoHeader");

        assertEquals("Supplier code required", ws.getWkMsgLine().trim());
        assertEquals(0, ws.getHdrOk());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePoHeader_supplierNotFound_setsMessage() {
        ws.setPhSupp(999);
        doReturn(true).when(suppfSpy).isInvalidKey();

        invokePrivate("validatePoHeader");

        assertEquals("Supplier not found", ws.getWkMsgLine().trim());
        assertEquals(0, ws.getHdrOk());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePoHeader_supplierDeleted_setsMessage() {
        ws.setPhSupp(500);
        ws.setSpDelFlag(1);

        invokePrivate("validatePoHeader");

        assertEquals("Supplier is deleted", ws.getWkMsgLine().trim());
        assertEquals(0, ws.getHdrOk());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePoHeader_taxTypeZero_defaultsToSupplierTaxType() {
        ws.setPhSupp(500);
        ws.setPhTaxType(0);
        setupValidSupplierRecord();

        invokePrivate("validatePoHeader");

        assertEquals(2, ws.getPhTaxType());
        assertEquals(1, ws.getHdrOk());
        assertEquals("Acme Supplies Co", ws.getWkSuppName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void validatePoHeader_taxTypeZeroAndSupplierTaxTypeZero_defaultsToOne() {
        ws.setPhSupp(500);
        ws.setPhTaxType(0);
        ws.setSpDelFlag(0);
        ws.setSpName("Acme Supplies Co");
        ws.setSpTaxType(0);

        invokePrivate("validatePoHeader");

        assertEquals(1, ws.getPhTaxType());
        assertEquals(1, ws.getHdrOk());
    }

    /* ── DLOOP-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayAndAcceptDetailLine_ests03_setsDtlDone() {
        doReturn("03").when(renderer).readEndStatus();

        invokePrivate("displayAndAcceptDetailLine");

        assertEquals(1, ws.getDtlDone());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayAndAcceptDetailLine_ests04_showsLineClearedMessage() {
        doReturn("04").when(renderer).readEndStatus();

        invokePrivate("displayAndAcceptDetailLine");

        assertEquals("Line cleared", ws.getWkMsgLine().trim());
        assertEquals(0, ws.getDtlDone());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayAndAcceptDetailLine_estsOther_showsInvalidKeyMessage() {
        doReturn("99").when(renderer).readEndStatus();

        invokePrivate("displayAndAcceptDetailLine");

        assertEquals("Invalid key", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void displayAndAcceptDetailLine_ests00_callsProcessDetailLine() {
        setupValidProductRecord();
        acceptFieldValues.put("WK-D-PROD", "700");
        acceptFieldValues.put("WK-D-QTY", "5");
        acceptFieldValues.put("WK-D-COST", "10.00");
        doReturn("00").when(renderer).readEndStatus();

        invokePrivate("displayAndAcceptDetailLine");

        assertEquals(1, ws.getWkLcnt());
        assertEquals("Line added", ws.getWkMsgLine().trim());
    }

    /* ── CLRD-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearDetailLineFields_resetsFieldsToDefaults() {
        ws.setWkDProd(700);
        ws.setWkDWhse(10);
        ws.setWkDQty(5);
        ws.setWkDCost(new BigDecimal("10.00"));
        ws.setWkDAmt(50);
        ws.setWkDStkmng(1);

        invokePrivate("clearDetailLineFields");

        assertEquals(0, ws.getWkDProd());
        assertEquals(0, ws.getWkDWhse());
        assertEquals(0, ws.getWkDQty());
        assertEquals(0, ws.getWkDCost().compareTo(BigDecimal.ZERO));
        assertEquals(0, ws.getWkDAmt());
        assertEquals(0, ws.getWkDStkmng());
    }

    /* ── PDET-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processDetailLine_productCodeZero_showsMessage() {
        ws.setWkDProd(0);

        invokePrivate("processDetailLine");

        assertEquals("Product code required", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processDetailLine_productNotFound_showsMessage() {
        ws.setWkDProd(700);
        doReturn(true).when(prodfSpy).isInvalidKey();

        invokePrivate("processDetailLine");

        assertEquals("Product not found", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processDetailLine_productDeleted_showsMessage() {
        ws.setWkDProd(700);
        ws.setPrDelFlag(1);

        invokePrivate("processDetailLine");

        assertEquals("Product is deleted", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processDetailLine_warehouseZeroDefaultsFromProduct_thenSucceeds() {
        ws.setWkDProd(700);
        ws.setWkDWhse(0);
        ws.setWkDQty(5);
        ws.setWkDCost(new BigDecimal("10.00"));
        setupValidProductRecord();

        invokePrivate("processDetailLine");

        assertEquals(10, ws.getWkDWhse());
        assertEquals(1, ws.getWkLcnt());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processDetailLine_warehouseZeroAndProductDefaultZero_showsMessage() {
        ws.setWkDProd(700);
        ws.setWkDWhse(0);
        setupValidProductRecord();
        ws.setPrDfltWhse(0);

        invokePrivate("processDetailLine");

        assertEquals("Warehouse required", ws.getWkMsgLine().trim());
        assertEquals(0, ws.getWkLcnt());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processDetailLine_quantityNotPositive_showsMessage() {
        ws.setWkDProd(700);
        ws.setWkDWhse(10);
        ws.setWkDQty(0);
        setupValidProductRecord();

        invokePrivate("processDetailLine");

        assertEquals("Quantity must be positive", ws.getWkMsgLine().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processDetailLine_costZeroResolvesFromLastCost_thenSucceeds() {
        ws.setWkDProd(700);
        ws.setWkDWhse(10);
        ws.setWkDQty(5);
        ws.setWkDCost(BigDecimal.ZERO);
        setupValidProductRecord();
        ws.setPrLastCost(new BigDecimal("12.50"));

        invokePrivate("processDetailLine");

        assertEquals(new BigDecimal("12.50"), ws.getWkDCost());
        assertEquals(1, ws.getWkLcnt());
        assertEquals(63L, ws.getWlAmount(1));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processDetailLine_costStillZeroAfterResolve_showsMessage() {
        ws.setWkDProd(700);
        ws.setWkDWhse(10);
        ws.setWkDQty(5);
        ws.setWkDCost(BigDecimal.ZERO);
        setupValidProductRecord();
        ws.setPrLastCost(BigDecimal.ZERO);
        ws.setPrStdCost(BigDecimal.ZERO);

        invokePrivate("processDetailLine");

        assertEquals("Unit cost required", ws.getWkMsgLine().trim());
        assertEquals(0, ws.getWkLcnt());
    }

    /* ── RCST-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void resolveDefaultUnitCost_lastCostPositive_usesLastCost() {
        ws.setPrLastCost(new BigDecimal("15.00"));
        ws.setPrStdCost(new BigDecimal("20.00"));

        invokePrivate("resolveDefaultUnitCost");

        assertEquals(new BigDecimal("15.00"), ws.getWkDCost());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void resolveDefaultUnitCost_lastCostZero_usesStandardCost() {
        ws.setPrLastCost(BigDecimal.ZERO);
        ws.setPrStdCost(new BigDecimal("20.00"));

        invokePrivate("resolveDefaultUnitCost");

        assertEquals(new BigDecimal("20.00"), ws.getWkDCost());
    }

    /* ── ADDL-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void addDetailLineToOrder_maxLinesReached_showsMessageWithoutAdding() {
        ws.setWkLcnt(200);

        invokePrivate("addDetailLineToOrder");

        assertEquals("Maximum 200 lines reached", ws.getWkMsgLine().trim());
        assertEquals(200, ws.getWkLcnt());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void addDetailLineToOrder_success_incrementsLineCountAndTotals() {
        ws.setWkLcnt(0);
        ws.setWkNetTotal(0);
        ws.setWkDProd(700);
        ws.setWkDWhse(10);
        ws.setWkDQty(5);
        ws.setWkDCost(new BigDecimal("10.00"));
        ws.setWkDAmt(50);
        ws.setWkDStkmng(1);

        invokePrivate("addDetailLineToOrder");

        assertEquals(1, ws.getWkLcnt());
        assertEquals(700, ws.getWlProd(1));
        assertEquals(50L, ws.getWlAmount(1));
        assertEquals(50L, ws.getWkNetTotal());
        assertEquals("Line added", ws.getWkMsgLine().trim());
        assertTrue(screenInteractions.contains("displayScreen:DS-STATUS"));
    }

    /* ── CSAV-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndSaveOrder_confirmY_savesOrder() {
        ws.setWkLcnt(1);
        ws.setWkNetTotal(1000);
        ws.setPhTaxType(1);
        ws.setPhDate(20260101);
        acceptFieldValues.put("WK-CONFIRM", "Y");

        invokePrivate("confirmAndSaveOrder");

        verify(pohfSpy).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndSaveOrder_confirmLowercaseY_savesOrder() {
        ws.setWkLcnt(1);
        ws.setWkNetTotal(1000);
        ws.setPhTaxType(1);
        ws.setPhDate(20260101);
        acceptFieldValues.put("WK-CONFIRM", "y");

        invokePrivate("confirmAndSaveOrder");

        verify(pohfSpy).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmAndSaveOrder_confirmN_discardsOrderWithoutWrite() {
        ws.setWkLcnt(1);
        ws.setWkNetTotal(1000);
        acceptFieldValues.put("WK-CONFIRM", "N");

        invokePrivate("confirmAndSaveOrder");

        assertEquals("PO discarded", ws.getWkMsgLine().trim());
        verify(pohfSpy, never()).write();
    }

    /* ── CTT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void calculateOrderTaxTotals_statusOk_usesTaxcalValues() {
        ws.setPhTaxType(1);
        ws.setPhDate(20260101);
        ws.setWkNetTotal(1000);

        invokePrivate("calculateOrderTaxTotals");

        assertEquals(1000L, ws.getWkNetTotal());
        assertEquals(100L, ws.getWkTaxTotal());
        assertEquals(1100L, ws.getWkGrsTotal());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void calculateOrderTaxTotals_statusNotOk_zerosTaxAndUsesNetAsGross() {
        doAnswer(
                        inv -> {
                            TaxcalLinkParm p = inv.getArgument(0);
                            p.getKtax().setKtStatus("99");
                            return null;
                        })
                .when(taxcalService)
                .execute(any());
        ws.setWkNetTotal(1000);

        invokePrivate("calculateOrderTaxTotals");

        assertEquals(0, ws.getWkTaxTotal());
        assertEquals(1000L, ws.getWkGrsTotal());
    }

    /* ── SPO-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void savePurchaseOrderHeader_numgenAssignmentFails_showsMessageAndSkipsWrite() {
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("XX");
                            return null;
                        })
                .when(numgenService)
                .execute(any());

        invokePrivate("savePurchaseOrderHeader");

        assertEquals("Number assignment failed", ws.getWkMsgLine().trim());
        verify(pohfSpy, never()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void savePurchaseOrderHeader_writeInvalidKey_showsMessageAndSkipsDetails() {
        doReturn(true).when(pohfSpy).isInvalidKey();

        invokePrivate("savePurchaseOrderHeader");

        assertEquals("Header write failed", ws.getWkMsgLine().trim());
        verify(podfSpy, never()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void savePurchaseOrderHeader_success_writesHeaderAndDetailsAndShowsSavedMessage() {
        ws.setWkLcnt(1);
        ws.setLx(1);
        ws.setWlProd(1, 700);
        ws.setWlWhse(1, 10);
        ws.setWlQty(1, 5);
        ws.setWlCost(1, new BigDecimal("10.00"));
        ws.setWlAmount(1, 50);
        ws.setWlStkmng(1, 0);
        ws.setWkNetTotal(50);
        ws.setWkTaxTotal(5);
        ws.setWkGrsTotal(55);
        ws.setWkSysdate(20260918);
        ws.setWkUserCode(7);

        invokePrivate("savePurchaseOrderHeader");

        assertEquals(1001L, ws.getPhNo());
        assertEquals(1001L, ws.getWkPoNoD());
        assertEquals(new BigDecimal("50"), ws.getPhAmount());
        assertEquals(0, ws.getPhStatus());
        assertEquals(1, ws.getPhLines());
        assertEquals(0, ws.getPhDelFlag());
        assertEquals("PO saved", ws.getWkMsgLine().trim());
        verify(pohfSpy).write();
        verify(podfSpy).write();
    }

    /* ── WDET-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void writePurchaseOrderDetails_loopsOverAllLines_writesEachLineAndUpdatesStock() {
        ws.setWkLcnt(3);
        ws.setPhNo(1001L);
        ws.setPhDueDate(20260301);
        for (int i = 1; i <= 3; i++) {
            ws.setWlProd(i, 700 + i);
            ws.setWlWhse(i, 10);
            ws.setWlQty(i, 5);
            ws.setWlCost(i, new BigDecimal("10.00"));
            ws.setWlAmount(i, 50);
            ws.setWlStkmng(i, 0);
        }

        invokePrivate("writePurchaseOrderDetails");

        verify(podfSpy, times(3)).write();
        assertEquals(703, ws.getPdProd());
        assertEquals(3, ws.getPdLine());
    }

    /* ── UONO-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateStockOnOrderQty_stkmngZero_skipsStockUpdate() {
        ws.setLx(1);
        ws.setWlStkmng(1, 0);

        invokePrivate("updateStockOnOrderQty");

        verify(stokfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateStockOnOrderQty_invalidKey_insertsNewStockRecordWithOrderedQty() {
        ws.setLx(1);
        ws.setWlStkmng(1, 1);
        ws.setWlProd(1, 700);
        ws.setWlWhse(1, 10);
        ws.setWlQty(1, 5);
        doReturn(true).when(stokfSpy).isInvalidKey();

        invokePrivate("updateStockOnOrderQty");

        verify(stokfSpy).write();
        verify(stokfSpy, never()).rewrite();
        assertEquals(new BigDecimal("5"), ws.getSkOnOrder());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateStockOnOrderQty_notInvalidKey_addsToExistingOnOrderAndRewrites() {
        ws.setLx(1);
        ws.setWlStkmng(1, 1);
        ws.setWlProd(1, 700);
        ws.setWlWhse(1, 10);
        ws.setWlQty(1, 5);
        ws.setSkOnOrder(new BigDecimal("20"));
        doReturn(false).when(stokfSpy).isInvalidKey();

        invokePrivate("updateStockOnOrderQty");

        verify(stokfSpy).rewrite();
        verify(stokfSpy, never()).write();
        assertEquals(new BigDecimal("25"), ws.getSkOnOrder());
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void closeAllFiles_closesAllFiveFiles() {
        invokePrivate("closeAllFiles");

        verify(pohfSpy).close();
        verify(podfSpy).close();
        verify(stokfSpy).close();
        verify(suppfSpy).close();
        verify(prodfSpy).close();
    }
}

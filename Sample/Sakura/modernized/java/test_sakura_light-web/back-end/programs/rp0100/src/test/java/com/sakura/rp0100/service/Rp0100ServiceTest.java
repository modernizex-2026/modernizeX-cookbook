package com.sakura.rp0100.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0100.domain.Rp0100FieldAccess;
import com.sakura.rp0100.runtime.Rp0100Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.PurdfDataset;
import com.sakura.runtime.io.PurhfDataset;
import com.sakura.runtime.io.RepfDataset;
import com.sakura.runtime.io.SuppfDataset;
import com.sakura.runtime.io.SyscfDataset;
import com.sakura.runtime.linkage.DateutLinkParm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for {@link Rp0100Service}, derived from COBOL program RP0100 (purchase journal: lists
 * PURHF purchases and PURDF detail lines for a purchase-date range, with per-document subtotal and
 * grand totals). Ground truth for inputs/expected values: RP0100.cob PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Rp0100ServiceTest {

    @Spy private PurhfDataset purhfSpy = new PurhfDataset();
    @Spy private PurdfDataset purdfSpy = new PurdfDataset();
    @Spy private SuppfDataset suppfSpy = new SuppfDataset();
    @Spy private ProdfDataset prodfSpy = new ProdfDataset();
    @Spy private SyscfDataset syscfSpy = new SyscfDataset();
    @Spy private RepfDataset repfSpy = new RepfDataset();

    private final DateutService dateutService = mock(DateutService.class);
    private final AbortxService abortxService = mock(AbortxService.class);

    private Rp0100Service service;
    private Rp0100FieldAccess ws;

    private MockedStatic<com.sakura.runtime.Utility> mockedUtility;

    /** Subclass swapping in the spy datasets since Rp0100Datasets builds its own real files. */
    private static class TestDatasets extends Rp0100Datasets {
        private final PurhfDataset purhf;
        private final PurdfDataset purdf;
        private final SuppfDataset suppf;
        private final ProdfDataset prodf;
        private final SyscfDataset syscf;
        private final RepfDataset repf;

        TestDatasets(
                PurhfDataset purhf,
                PurdfDataset purdf,
                SuppfDataset suppf,
                ProdfDataset prodf,
                SyscfDataset syscf,
                RepfDataset repf) {
            this.purhf = purhf;
            this.purdf = purdf;
            this.suppf = suppf;
            this.prodf = prodf;
            this.syscf = syscf;
            this.repf = repf;
        }

        @Override
        public PurhfDataset getPurhf() {
            return purhf;
        }

        @Override
        public PurdfDataset getPurdf() {
            return purdf;
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
        public SyscfDataset getSyscf() {
            return syscf;
        }

        @Override
        public RepfDataset getRepf() {
            return repf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Rp0100Datasets fileSet =
                new TestDatasets(purhfSpy, purdfSpy, suppfSpy, prodfSpy, syscfSpy, repfSpy);
        service = new Rp0100Service(fileSet, dateutService, abortxService);
        ws = getWs();

        for (var spy : new Object[] {purhfSpy, purdfSpy, suppfSpy, prodfSpy, syscfSpy, repfSpy}) {
            var raw = (com.sakura.runtime.record.RawDatasetBase) spy;
            doNothing().when(raw).open(any());
            doNothing().when(raw).close();
            doReturn("00").when(raw).getFileStatus();
            doReturn(false).when(raw).isInvalidKey();
            doReturn(false).when(raw).isAtEnd();
            doReturn(true).when(raw).start(anyString(), anyString());
            doReturn(false).when(raw).readNext();
            doReturn(true).when(raw).readByKey(any());
            doNothing().when(raw).write();
        }

        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            p.getKdate().setKdStatus("00");
                            p.getKdate().setKdDate1(20260918);
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        mockedUtility =
                Mockito.mockStatic(com.sakura.runtime.Utility.class, Mockito.CALLS_REAL_METHODS);
    }

    @AfterEach
    void tearDown() {
        mockedUtility.close();
    }

    /* ── reflection helpers ── */

    private Rp0100FieldAccess getWs() throws Exception {
        Field f = Rp0100Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Rp0100FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Rp0100Service.class.getDeclaredMethod(name);
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

    /**
     * Stubs readNext()/isAtEnd() so exactly one record is returned before EOF. The service checks
     * isAtEnd() TWICE per readNext() call (once to set the eof flag, once to guard whether to
     * process the record) — tracking end-of-file as explicit mutable state keeps isAtEnd()
     * consistent across both checks within the same iteration.
     */
    private void stubOneRecordThenEof(
            com.sakura.runtime.record.RawDatasetBase spy, Runnable onFirstRecord) {
        AtomicBoolean atEnd = new AtomicBoolean(false);
        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            if (callCount.getAndIncrement() == 0) {
                                onFirstRecord.run();
                                return true;
                            }
                            atEnd.set(true);
                            return false;
                        })
                .when(spy)
                .readNext();
        doAnswer(inv -> atEnd.get()).when(spy).isAtEnd();
    }

    /* ── INIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeAndOpenFiles_allStatus00_usesSyscfCompanyName() {
        doAnswer(
                        inv -> {
                            ws.setSyCompanyName("ACME SUPPLY CO");
                            return true;
                        })
                .when(syscfSpy)
                .readByKey(any());

        invokePrivate("initializeAndOpenFiles");

        assertEquals("RP0100", ws.getWkProgid().trim());
        assertEquals("PURCHASE JOURNAL", ws.getWkTitle().trim());
        assertEquals(20260918, ws.getWkSysdate());
        assertEquals("ACME SUPPLY CO", ws.getWkCompany().trim());
        verify(purhfSpy).open(FileOpenMode.INPUT);
        verify(purdfSpy).open(FileOpenMode.INPUT);
        verify(suppfSpy).open(FileOpenMode.INPUT);
        verify(prodfSpy).open(FileOpenMode.INPUT);
        verify(repfSpy).open(FileOpenMode.OUTPUT);
        verify(syscfSpy).close();
        verify(abortxService, never()).execute(any());
        assertEquals(0, ws.getWkMainEof());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeAndOpenFiles_syscfInvalidKey_keepsDefaultCompanyName() {
        doReturn(true).when(syscfSpy).isInvalidKey();

        invokePrivate("initializeAndOpenFiles");

        assertEquals("SAKURA Sales Management System", ws.getWkCompany().trim());
        verify(syscfSpy).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeAndOpenFiles_syscfOpenFails_skipsLookupAndClose() {
        doReturn("35").when(syscfSpy).getFileStatus();

        invokePrivate("initializeAndOpenFiles");

        verify(syscfSpy, never()).readByKey(any());
        verify(syscfSpy, never()).close();
        assertEquals("SAKURA Sales Management System", ws.getWkCompany().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeAndOpenFiles_purhfStatus35_treatedAsAcceptableButSetsMainEof() {
        doReturn("35").when(purhfSpy).getFileStatus();

        invokePrivate("initializeAndOpenFiles");

        verify(abortxService, never()).execute(any());
        assertEquals(1, ws.getWkMainEof());
        verify(purdfSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeAndOpenFiles_purhfStatus30_treatedAsAcceptableButSetsMainEof() {
        doReturn("30").when(purhfSpy).getFileStatus();

        invokePrivate("initializeAndOpenFiles");

        verify(abortxService, never()).execute(any());
        assertEquals(1, ws.getWkMainEof());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeAndOpenFiles_purhfOpenErrorStatus23_abendsWithPurhfLabel() {
        doReturn("23").when(purhfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeAndOpenFiles"));

        assertEquals("PURHF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(purdfSpy, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeAndOpenFiles_repfOpenFails_abendsWithRepfLabel() {
        doReturn("35").when(repfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeAndOpenFiles"));

        assertEquals("REPF", ws.getKaFile().trim());
        assertEquals("Report file error", ws.getKaDetail().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── PARM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readDateRangeParameters_numericDates_setsDateFromAndTo() {
        mockedUtility
                .when(com.sakura.runtime.Utility::readStdinLine)
                .thenReturn("20260101", "20260228");

        invokePrivate("readDateRangeParameters");

        assertEquals(20260101, ws.getWkDateFrom());
        assertEquals(20260228, ws.getWkDateTo());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readDateRangeParameters_blankInputs_usesDefaultFullRange() {
        mockedUtility.when(com.sakura.runtime.Utility::readStdinLine).thenReturn(null, null);

        invokePrivate("readDateRangeParameters");

        assertEquals(0, ws.getWkDateFrom());
        assertEquals(99999999, ws.getWkDateTo());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readDateRangeParameters_nonNumericFromInput_defaultsFromToZero() {
        mockedUtility
                .when(com.sakura.runtime.Utility::readStdinLine)
                .thenReturn("ABCDEFGH", "20261231");

        invokePrivate("readDateRangeParameters");

        assertEquals(0, ws.getWkDateFrom());
        assertEquals(20261231, ws.getWkDateTo());
    }

    /* ── PRINT-010 — main header loop ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processAllPurchaseHeaders_mainEofAlreadySet_skipsStartAndLoopButPrintsSummary() {
        ws.setWkMainEof(1);

        invokePrivate("processAllPurchaseHeaders");

        verify(purhfSpy, never()).start(anyString(), anyString());
        verify(purhfSpy, never()).readNext();
        verify(repfSpy, times(4)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processAllPurchaseHeaders_startInvalidKey_setsMainEofAndSkipsLoop() {
        doReturn(true).when(purhfSpy).isInvalidKey();

        invokePrivate("processAllPurchaseHeaders");

        assertEquals(1, ws.getWkMainEof());
        verify(purhfSpy, never()).readNext();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void
            processAllPurchaseHeaders_singleQualifyingHeaderWithDetail_printsHeaderDetailAndSubtotal() {
        stubOneRecordThenEof(
                purhfSpy,
                () -> {
                    ws.setVhNo(1001);
                    ws.setVhDelFlag(0);
                    ws.setVhStatus(0);
                    ws.setVhDate(20260115);
                    ws.setVhSupp(500);
                    ws.setVhKind(1);
                    ws.setVhAmount(new BigDecimal("100"));
                    ws.setVhTaxAmount(new BigDecimal("10"));
                    ws.setVhTotal(new BigDecimal("110"));
                });
        stubOneRecordThenEof(
                purdfSpy,
                () -> {
                    ws.setVdNo(1001);
                    ws.setVdLine(1);
                    ws.setVdProd(9001);
                    ws.setVdQty(new BigDecimal("5"));
                    ws.setVdUnitCost(new BigDecimal("2.50"));
                    ws.setVdAmount(new BigDecimal("12.50"));
                });
        doReturn(true).when(suppfSpy).isInvalidKey();
        doAnswer(
                        inv -> {
                            ws.setPrName("Widget");
                            return true;
                        })
                .when(prodfSpy)
                .readByKey(any());

        invokePrivate("processAllPurchaseHeaders");

        assertEquals(1, ws.getWkPurCnt());
        assertEquals(1, ws.getWkLinCnt());
        // page-header(3, WK-LINE starts at 99) + header-line(1) + detail-line(1) +
        // subtotal+blank(2)
        // + summary rule+grand-total(2) = 9
        verify(repfSpy, times(9)).write();
    }

    /* ── PP-010 — header skip conditions ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processPurchaseHeaderRecord_delFlagSet_skipsRecordEntirely() {
        ws.setVhDelFlag(1);
        ws.setVhStatus(0);
        ws.setVhDate(20260101);
        ws.setWkDateFrom(0);
        ws.setWkDateTo(99999999);

        invokePrivate("processPurchaseHeaderRecord");

        assertEquals(0, ws.getWkPurCnt());
        verify(repfSpy, never()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processPurchaseHeaderRecord_status9_skipsRecordEntirely() {
        ws.setVhDelFlag(0);
        ws.setVhStatus(9);
        ws.setVhDate(20260101);
        ws.setWkDateFrom(0);
        ws.setWkDateTo(99999999);

        invokePrivate("processPurchaseHeaderRecord");

        assertEquals(0, ws.getWkPurCnt());
        verify(repfSpy, never()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processPurchaseHeaderRecord_dateBeforeFrom_skipsRecord() {
        ws.setVhDelFlag(0);
        ws.setVhStatus(0);
        ws.setVhDate(20260101);
        ws.setWkDateFrom(20260201);
        ws.setWkDateTo(99999999);

        invokePrivate("processPurchaseHeaderRecord");

        assertEquals(0, ws.getWkPurCnt());
        verify(repfSpy, never()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processPurchaseHeaderRecord_dateAfterTo_skipsRecord() {
        ws.setVhDelFlag(0);
        ws.setVhStatus(0);
        ws.setVhDate(20260301);
        ws.setWkDateFrom(0);
        ws.setWkDateTo(20260201);

        invokePrivate("processPurchaseHeaderRecord");

        assertEquals(0, ws.getWkPurCnt());
        verify(repfSpy, never()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processPurchaseHeaderRecord_kind2_setsReturnKindText() {
        ws.setVhDelFlag(0);
        ws.setVhStatus(0);
        ws.setVhDate(20260101);
        ws.setWkDateFrom(0);
        ws.setWkDateTo(99999999);
        ws.setWkLine(10);
        ws.setVhKind(2);
        ws.setVhNo(2001);
        ws.setVhAmount(BigDecimal.ZERO);
        ws.setVhTaxAmount(BigDecimal.ZERO);
        ws.setVhTotal(BigDecimal.ZERO);
        doReturn(true).when(suppfSpy).isInvalidKey();
        doReturn(true).when(purdfSpy).isInvalidKey();

        invokePrivate("processPurchaseHeaderRecord");

        assertEquals("RETURN", ws.getWkKindTxt().trim());
        assertEquals(1, ws.getWkPurCnt());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processPurchaseHeaderRecord_kindOther_setsPurchKindText() {
        ws.setVhDelFlag(0);
        ws.setVhStatus(0);
        ws.setVhDate(20260101);
        ws.setWkDateFrom(0);
        ws.setWkDateTo(99999999);
        ws.setWkLine(10);
        ws.setVhKind(1);
        ws.setVhNo(2002);
        ws.setVhAmount(BigDecimal.ZERO);
        ws.setVhTaxAmount(BigDecimal.ZERO);
        ws.setVhTotal(BigDecimal.ZERO);
        doReturn(true).when(suppfSpy).isInvalidKey();
        doReturn(true).when(purdfSpy).isInvalidKey();

        invokePrivate("processPurchaseHeaderRecord");

        assertEquals("PURCH", ws.getWkKindTxt().trim());
    }

    /* ── PD-010 / RDT-010 — detail loop ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processPurchaseDetails_startInvalidKey_setsDtlEofAndSkipsLoop() {
        doReturn(true).when(purdfSpy).isInvalidKey();

        invokePrivate("processPurchaseDetails");

        assertEquals(1, ws.getWkDtlEof());
        verify(purdfSpy, never()).readNext();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readNextPurchaseDetail_differentVdNo_stopsScanAtBoundary() {
        ws.setVhNo(7001);
        ws.setVdNo(7001);
        doAnswer(
                        inv -> {
                            ws.setVdNo(7002); // different order number -> boundary reached
                            ws.setVdLine(1);
                            return true;
                        })
                .when(purdfSpy)
                .readNext();
        doReturn(false).when(purdfSpy).isAtEnd();

        invokePrivate("processPurchaseDetails");

        assertEquals(1, ws.getWkDtlEof());
        verify(purdfSpy, times(1)).readNext();
        verify(repfSpy, never()).write();
    }

    /* ── POD-010 — detail line ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printPurchaseDetailLine_prodfFound_populatesFieldsAndWrites() {
        ws.setWkLine(10);
        ws.setWkLinCnt(0);
        ws.setVdLine(3);
        ws.setVdProd(9005);
        ws.setVdQty(new BigDecimal("12"));
        ws.setVdUnitCost(new BigDecimal("1.99"));
        ws.setVdAmount(new BigDecimal("23.88"));
        doAnswer(
                        inv -> {
                            ws.setPrName("Gadget");
                            return true;
                        })
                .when(prodfSpy)
                .readByKey(any());

        invokePrivate("printPurchaseDetailLine");

        assertEquals("Gadget", ws.getWkProdName().trim());
        assertEquals(3, ws.getVdLineE());
        assertEquals(9005, ws.getVdProdE());
        assertEquals(12L, ws.getVdQtyE());
        assertEquals(new BigDecimal("1.99"), ws.getVdCostE());
        assertEquals(23L, ws.getVdAmtE());
        assertEquals(11, ws.getWkLine());
        assertEquals(1, ws.getWkLinCnt());
        verify(repfSpy).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printPurchaseDetailLine_prodfInvalidKey_usesUnknownPlaceholder() {
        ws.setWkLine(10);
        ws.setVdQty(BigDecimal.ONE);
        ws.setVdUnitCost(BigDecimal.TEN);
        ws.setVdAmount(BigDecimal.TEN);
        doReturn(true).when(prodfSpy).isInvalidKey();

        invokePrivate("printPurchaseDetailLine");

        assertEquals("(unknown)", ws.getWkProdName().trim());
    }

    /* ── PS-010 — per-document subtotal ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printPurchaseSubtotal_writesSubtotalAndBlankLine() {
        ws.setWkLine(10);
        ws.setVhAmount(new BigDecimal("1000"));
        ws.setVhTaxAmount(new BigDecimal("100"));
        ws.setVhTotal(new BigDecimal("1100"));

        invokePrivate("printPurchaseSubtotal");

        assertEquals(1000L, ws.getSubNet());
        assertEquals(100L, ws.getSubTax());
        assertEquals(1100L, ws.getSubTot());
        assertEquals(12, ws.getWkLine());
        verify(repfSpy, times(2)).write();
    }

    /* ── LKS-010 — supplier lookup ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupSupplierName_found_usesSpName() {
        ws.setVhSupp(300);
        doAnswer(
                        inv -> {
                            ws.setSpName("Global Parts Ltd");
                            return true;
                        })
                .when(suppfSpy)
                .readByKey(any());

        invokePrivate("lookupSupplierName");

        assertEquals("Global Parts Ltd", ws.getWkSuppName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupSupplierName_invalidKey_usesUnknownPlaceholder() {
        doReturn(true).when(suppfSpy).isInvalidKey();

        invokePrivate("lookupSupplierName");

        assertEquals("(unknown)", ws.getWkSuppName().trim());
    }

    /* ── LKP-010 — product lookup ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupProductName_found_usesPrName() {
        ws.setVdProd(9009);
        doAnswer(
                        inv -> {
                            ws.setPrName("Bolt Set");
                            return true;
                        })
                .when(prodfSpy)
                .readByKey(any());

        invokePrivate("lookupProductName");

        assertEquals("Bolt Set", ws.getWkProdName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupProductName_invalidKey_usesUnknownPlaceholder() {
        doReturn(true).when(prodfSpy).isInvalidKey();

        invokePrivate("lookupProductName");

        assertEquals("(unknown)", ws.getWkProdName().trim());
    }

    /* ── CPH-010 / CP-010 / PH-010 — page breaks ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkPageBreakForHeader_lineBelow50_doesNotPrintHeader() {
        ws.setWkLine(10);

        invokePrivate("checkPageBreakForHeader");

        verify(repfSpy, never()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkPageBreakForHeader_lineAt50_printsPageHeader() {
        ws.setWkLine(50);
        ws.setWkCompany("TEST CO");
        ws.setWkTitle("TEST TITLE");

        invokePrivate("checkPageBreakForHeader");

        assertEquals(1, ws.getWkPage());
        assertEquals(5, ws.getWkLine());
        verify(repfSpy, times(3)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkPageBreakForDetail_lineBelow55_doesNotPrintHeader() {
        ws.setWkLine(54);

        invokePrivate("checkPageBreakForDetail");

        verify(repfSpy, never()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkPageBreakForDetail_lineAt55_printsNewPageHeaderAndResetsLineCounter() {
        ws.setWkLine(55);
        ws.setWkPage(2);

        invokePrivate("checkPageBreakForDetail");

        assertEquals(3, ws.getWkPage());
        assertEquals(5, ws.getWkLine());
        verify(repfSpy, times(3)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printReportHeader_buildsPeriodInfoAndResetsLine() {
        ws.setWkCompany("SAKURA CO");
        ws.setWkTitle("PURCHASE JOURNAL");
        ws.setWkPage(0);
        ws.setWkSysdate(20260101);
        ws.setWkDateFrom(20260101);
        ws.setWkDateTo(20260228);

        invokePrivate("printReportHeader");

        assertEquals(1, ws.getWkPage());
        assertEquals("SAKURA CO", ws.getH1Company().trim());
        assertEquals("PURCHASE JOURNAL", ws.getH1Title().trim());
        assertEquals(1, ws.getH1Page());
        assertEquals(20260101, ws.getH2Date());
        assertTrue(ws.getH2Info().trim().startsWith("PERIOD 20260101 - 20260228"));
        assertEquals(5, ws.getWkLine());
        verify(repfSpy, times(3)).write();
    }

    /* ── PG-010 — report summary ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printReportSummary_noPurchases_writesNoPurchasesMessage() {
        ws.setWkPurCnt(0);
        ws.setWkLine(10);

        invokePrivate("printReportSummary");

        assertEquals("*** NO PURCHASES IN THE SELECTED PERIOD ***", ws.getRepRec().trim());
        verify(repfSpy).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printReportSummary_hasPurchases_writesRuleAndGrandTotal() {
        ws.setWkPurCnt(3);
        ws.setWkLine(10);
        ws.setWkGNet(new BigDecimal("30000"));
        ws.setWkGTax(new BigDecimal("3000"));
        ws.setWkGTot(new BigDecimal("33000"));

        invokePrivate("printReportSummary");

        assertEquals(30000L, ws.getGtNet());
        assertEquals(3000L, ws.getGtTax());
        assertEquals(33000L, ws.getGtTot());
        verify(repfSpy, times(2)).write();
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void finalizeAndCloseFiles_closesAllFilesAndSetsCompletionCodeZero() {
        ws.setWkPurCnt(2);
        ws.setWkLinCnt(9);

        invokePrivate("finalizeAndCloseFiles");

        verify(purhfSpy).close();
        verify(purdfSpy).close();
        verify(suppfSpy).close();
        verify(prodfSpy).close();
        verify(repfSpy).close();
        assertEquals(0, ws.getCompletionCode());
    }

    /* ── AB-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abortOnFileOpenError_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortOnFileOpenError"));

        assertEquals("RP0100", ws.getKaProgid().trim());
        assertEquals("23", ws.getKaFsts().trim());
        assertEquals("EOPEN", ws.getKaMsgcode().trim());
        assertEquals("Report file error", ws.getKaDetail().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── MAIN-000 — full integration flow ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_noPurchases_runsFullPipelineAndCompletesCleanly() {
        mockedUtility.when(com.sakura.runtime.Utility::readStdinLine).thenReturn(null, null);
        doReturn(true).when(purhfSpy).isInvalidKey();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

        assertEquals(0, ws.getCompletionCode());
        verify(purhfSpy).close();
        verify(repfSpy).close();
        verify(repfSpy, times(4)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_purhfOpenErrorStatus99_abendsAndNeverReachesTerm() {
        doReturn("99").when(purhfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

        assertEquals(255, ws.getCompletionCode());
        verify(purhfSpy, never()).close();
        verify(abortxService).execute(any());
    }
}

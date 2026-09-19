package com.sakura.rp0090.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.sakura.rp0090.domain.Rp0090FieldAccess;
import com.sakura.rp0090.runtime.Rp0090Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.OrddfDataset;
import com.sakura.runtime.io.OrdhfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.RepfDataset;
import com.sakura.runtime.io.SyscfDataset;
import com.sakura.runtime.linkage.DateutLinkParm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for {@link Rp0090Service}, derived from COBOL program RP0090 (sales order backlog
 * report: lists open ORDHF orders with outstanding ORDDF detail quantity, grand totals). Ground
 * truth for inputs/expected values: RP0090.cob PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Rp0090ServiceTest {

    @Spy private OrdhfDataset ordhfSpy = new OrdhfDataset();
    @Spy private OrddfDataset orddfSpy = new OrddfDataset();
    @Spy private CustfDataset custfSpy = new CustfDataset();
    @Spy private ProdfDataset prodfSpy = new ProdfDataset();
    @Spy private SyscfDataset syscfSpy = new SyscfDataset();
    @Spy private RepfDataset repfSpy = new RepfDataset();

    private final DateutService dateutService = mock(DateutService.class);
    private final AbortxService abortxService = mock(AbortxService.class);

    private Rp0090Service service;
    private Rp0090FieldAccess ws;

    /** Subclass swapping in the spy datasets since Rp0090Datasets builds its own real files. */
    private static class TestDatasets extends Rp0090Datasets {
        private final OrdhfDataset ordhf;
        private final OrddfDataset orddf;
        private final CustfDataset custf;
        private final ProdfDataset prodf;
        private final SyscfDataset syscf;
        private final RepfDataset repf;

        TestDatasets(
                OrdhfDataset ordhf,
                OrddfDataset orddf,
                CustfDataset custf,
                ProdfDataset prodf,
                SyscfDataset syscf,
                RepfDataset repf) {
            this.ordhf = ordhf;
            this.orddf = orddf;
            this.custf = custf;
            this.prodf = prodf;
            this.syscf = syscf;
            this.repf = repf;
        }

        @Override
        public OrdhfDataset getOrdhf() {
            return ordhf;
        }

        @Override
        public OrddfDataset getOrddf() {
            return orddf;
        }

        @Override
        public CustfDataset getCustf() {
            return custf;
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
        Rp0090Datasets fileSet =
                new TestDatasets(ordhfSpy, orddfSpy, custfSpy, prodfSpy, syscfSpy, repfSpy);
        service = new Rp0090Service(fileSet, dateutService, abortxService);
        ws = getWs();

        for (var spy : new Object[] {ordhfSpy, orddfSpy, custfSpy, prodfSpy, syscfSpy, repfSpy}) {
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
    }

    /* ── reflection helpers ── */

    private Rp0090FieldAccess getWs() throws Exception {
        Field f = Rp0090Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Rp0090FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Rp0090Service.class.getDeclaredMethod(name);
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
     * Stubs readNext()/isAtEnd() on a dataset spy so exactly one record is returned before EOF. The
     * service under test checks isAtEnd() TWICE per readNext() call (once to set the eof flag, once
     * to guard whether to process the record) — a naive sequential doReturn(false, true) stub gets
     * consumed across both checks within a single iteration and never lets the record be processed.
     * Tracking end-of-file as explicit mutable state keeps isAtEnd() consistent across both checks
     * in the same iteration.
     */
    private void stubOneRecordThenEof(
            com.sakura.runtime.record.RawDatasetBase spy, Runnable onFirstRecord) {
        java.util.concurrent.atomic.AtomicBoolean atEnd =
                new java.util.concurrent.atomic.AtomicBoolean(false);
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
    void initializeAndOpenFiles_allStatus00_setsUpEnvironmentAndUsesSyscfCompanyName() {
        doAnswer(
                        inv -> {
                            ws.setSyCompanyName("ACME TRADING CO");
                            return true;
                        })
                .when(syscfSpy)
                .readByKey(any());

        invokePrivate("initializeAndOpenFiles");

        assertEquals("RP0090", ws.getWkProgid().trim());
        assertEquals("SALES ORDER BACKLOG", ws.getWkTitle().trim());
        assertEquals(20260918, ws.getWkSysdate());
        assertEquals("ACME TRADING CO", ws.getWkCompany().trim());
        verify(ordhfSpy).open(FileOpenMode.INPUT);
        verify(orddfSpy).open(FileOpenMode.INPUT);
        verify(custfSpy).open(FileOpenMode.INPUT);
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
    void initializeAndOpenFiles_ordhfStatus35_treatedAsAcceptableButSetsMainEof() {
        doReturn("35").when(ordhfSpy).getFileStatus();

        invokePrivate("initializeAndOpenFiles");

        verify(abortxService, never()).execute(any());
        assertEquals(1, ws.getWkMainEof());
        verify(orddfSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeAndOpenFiles_ordhfStatus30_treatedAsAcceptableButSetsMainEof() {
        doReturn("30").when(ordhfSpy).getFileStatus();

        invokePrivate("initializeAndOpenFiles");

        verify(abortxService, never()).execute(any());
        assertEquals(1, ws.getWkMainEof());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeAndOpenFiles_ordhfOpenErrorStatus23_abendsWithOrdhfLabel() {
        doReturn("23").when(ordhfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeAndOpenFiles"));

        assertEquals("ORDHF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(orddfSpy, never()).open(any());
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

    /* ── PRINT-010 / RH-010 — main order loop ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printOrderBacklogReport_mainEofAlreadySet_skipsStartLoopButPrintsPageHeaderAndFooter() {
        ws.setWkMainEof(1);

        invokePrivate("printOrderBacklogReport");

        verify(ordhfSpy, never()).start(anyString(), anyString());
        verify(ordhfSpy, never()).readNext();
        // WK-LINE starts at 99 so CHECK-PAGE-HDR forces a page header (3 writes) before the
        // "*** NO OPEN ORDERS ***" line (1 write) since WK-G-ORD is still 0.
        verify(repfSpy, times(4)).write();
        assertEquals("*** NO OPEN ORDERS ***", ws.getRepRec().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printOrderBacklogReport_startInvalidKey_setsMainEofAndSkipsLoop() {
        doReturn(true).when(ordhfSpy).isInvalidKey();

        invokePrivate("printOrderBacklogReport");

        assertEquals(1, ws.getWkMainEof());
        verify(ordhfSpy, never()).readNext();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printOrderBacklogReport_singleQualifyingOrderWithBacklog_printsHeaderDetailAndSubtotal() {
        stubOneRecordThenEof(
                ordhfSpy,
                () -> {
                    ws.setOhNo(1001);
                    ws.setOhDelFlag(0);
                    ws.setOhStatus(1);
                    ws.setOhCust(500);
                    ws.setOhDate(20260101);
                    ws.setOhDueDate(20260201);
                });
        stubOneRecordThenEof(
                orddfSpy,
                () -> {
                    ws.setOdNo(1001);
                    ws.setOdLine(1);
                    ws.setOdProd(9001);
                    ws.setOdQty(new BigDecimal("10"));
                    ws.setOdShippedQty(new BigDecimal("4"));
                    ws.setOdUnitPrice(new BigDecimal("2.50"));
                });
        doReturn(true).when(custfSpy).isInvalidKey();
        doAnswer(
                        inv -> {
                            ws.setPrName("Widget");
                            return true;
                        })
                .when(prodfSpy)
                .readByKey(any());

        invokePrivate("printOrderBacklogReport");

        assertEquals(1, ws.getWkGOrd());
        assertEquals(1, ws.getWkGLin());
        assertEquals(new BigDecimal("6"), ws.getWkOrdQty());
        // page-header(3, forced by WK-LINE=99) + order-header(2) + detail(1) + subtotal(2)
        // + footer rule+grand-total(2) = 10
        verify(repfSpy, times(10)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printOrderBacklogReport_orderStatus3_skippedAndReportsNoOpenOrders() {
        stubOneRecordThenEof(
                ordhfSpy,
                () -> {
                    ws.setOhNo(2002);
                    ws.setOhDelFlag(0);
                    ws.setOhStatus(3);
                });

        invokePrivate("printOrderBacklogReport");

        assertEquals(0, ws.getWkGOrd());
        verify(orddfSpy, never()).start(anyString(), anyString());
        assertEquals("*** NO OPEN ORDERS ***", ws.getRepRec().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printOrderBacklogReport_orderDeleted_skippedEntirely() {
        stubOneRecordThenEof(
                ordhfSpy,
                () -> {
                    ws.setOhNo(3003);
                    ws.setOhDelFlag(1);
                    ws.setOhStatus(1);
                });

        invokePrivate("printOrderBacklogReport");

        assertEquals(0, ws.getWkGOrd());
        verify(orddfSpy, never()).start(anyString(), anyString());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printOrderBacklogReport_detailFullyShipped_headerNeverPrintedNoSubtotal() {
        stubOneRecordThenEof(
                ordhfSpy,
                () -> {
                    ws.setOhNo(4004);
                    ws.setOhDelFlag(0);
                    ws.setOhStatus(1);
                });
        stubOneRecordThenEof(
                orddfSpy,
                () -> {
                    ws.setOdNo(4004);
                    ws.setOdLine(1);
                    ws.setOdQty(new BigDecimal("5"));
                    ws.setOdShippedQty(new BigDecimal("5")); // fully shipped -> WK-OUT = 0
                });

        invokePrivate("printOrderBacklogReport");

        assertEquals(0, ws.getWkHdrPrinted());
        assertEquals(0, ws.getWkGOrd());
        verify(custfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printOrderBacklogReport_detailBelongsToDifferentOrder_stopsDetailScanAtBoundary() {
        stubOneRecordThenEof(
                ordhfSpy,
                () -> {
                    ws.setOhNo(5005);
                    ws.setOhDelFlag(0);
                    ws.setOhStatus(0);
                });
        // First ORDDF record already belongs to a later order -> boundary hit immediately.
        doAnswer(
                        inv -> {
                            ws.setOdNo(6006);
                            ws.setOdLine(1);
                            return true;
                        })
                .when(orddfSpy)
                .readNext();
        doReturn(false).when(orddfSpy).isInvalidKey();
        doReturn(false).when(orddfSpy).isAtEnd();

        invokePrivate("printOrderBacklogReport");

        assertEquals(1, ws.getWkDtlEof());
        assertEquals(0, ws.getWkGOrd());
        verify(orddfSpy, times(1)).readNext();
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
        assertEquals(4, ws.getWkLine());
        verify(repfSpy, times(3)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkPageBreak_lineBelow55_doesNotPrintHeader() {
        ws.setWkLine(54);

        invokePrivate("checkPageBreak");

        verify(repfSpy, never()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkPageBreak_lineAt55_printsNewPageHeaderAndResetsLineCounter() {
        ws.setWkLine(55);
        ws.setWkPage(2);

        invokePrivate("checkPageBreak");

        assertEquals(3, ws.getWkPage());
        assertEquals(4, ws.getWkLine());
        verify(repfSpy, times(3)).write();
    }

    /* ── PHD-010 — order header block ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printOrderHeaderBlock_custfKeyFound_usesCustomerName() {
        ws.setOhCust(700);
        doAnswer(
                        inv -> {
                            ws.setCuName("Global Corp");
                            return true;
                        })
                .when(custfSpy)
                .readByKey(any());

        invokePrivate("printOrderHeaderBlock");

        assertEquals("Global Corp", ws.getWkCustName().trim());
        assertEquals("Global Corp", ws.getOhCname().trim());
        verify(repfSpy, times(2)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printOrderHeaderBlock_custfInvalidKey_usesUnknownPlaceholder() {
        doReturn(true).when(custfSpy).isInvalidKey();

        invokePrivate("printOrderHeaderBlock");

        assertEquals("(unknown)", ws.getWkCustName().trim());
        verify(repfSpy, times(2)).write();
    }

    /* ── PDT-010 — order detail line ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printOrderDetailLine_prodfFound_computesOutstandingAmountTruncated() {
        ws.setWkOut(3);
        ws.setOdQty(new BigDecimal("10"));
        ws.setOdShippedQty(new BigDecimal("7"));
        ws.setOdUnitPrice(new BigDecimal("2.499")); // 3 * 2.499 = 7.497 -> truncate DOWN to 7
        doAnswer(
                        inv -> {
                            ws.setPrName("Gadget");
                            return true;
                        })
                .when(prodfSpy)
                .readByKey(any());

        invokePrivate("printOrderDetailLine");

        assertEquals("Gadget", ws.getWkProdName().trim());
        assertEquals(new BigDecimal("7"), ws.getWkOutAmt());
        assertEquals(new BigDecimal("3"), ws.getWkOrdQty());
        assertEquals(new BigDecimal("7"), ws.getWkOrdAmt());
        assertEquals(1, ws.getWkGLin());
        verify(repfSpy).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printOrderDetailLine_prodfInvalidKey_usesUnknownPlaceholder() {
        ws.setWkOut(1);
        ws.setOdQty(BigDecimal.ONE);
        ws.setOdShippedQty(BigDecimal.ZERO);
        ws.setOdUnitPrice(BigDecimal.TEN);
        doReturn(true).when(prodfSpy).isInvalidKey();

        invokePrivate("printOrderDetailLine");

        assertEquals("(unknown)", ws.getWkProdName().trim());
    }

    /* ── PSB-010 — order subtotal ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printOrderSubtotal_writesSubtotalAndBlankLine() {
        ws.setWkOrdQty(new BigDecimal("42"));
        ws.setWkOrdAmt(new BigDecimal("1234"));

        invokePrivate("printOrderSubtotal");

        assertEquals(42, ws.getSubQty());
        assertEquals(1234L, ws.getSubAmt());
        verify(repfSpy, times(2)).write();
    }

    /* ── PG-010 — report footer / grand total ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printReportFooter_noOpenOrders_writesNoOpenOrdersMessage() {
        ws.setWkGOrd(0);

        invokePrivate("printReportFooter");

        assertEquals("*** NO OPEN ORDERS ***", ws.getRepRec().trim());
        verify(repfSpy).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printReportFooter_hasOpenOrders_writesRuleAndGrandTotal() {
        ws.setWkGOrd(3);
        ws.setWkGQty(new BigDecimal("100"));
        ws.setWkGAmt(new BigDecimal("5000"));

        invokePrivate("printReportFooter");

        assertEquals(100, ws.getGtQty());
        assertEquals(5000L, ws.getGtAmt());
        verify(repfSpy, times(2)).write();
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void closeFilesAndFinish_closesAllFilesAndSetsCompletionCodeZero() {
        ws.setWkGOrd(2);
        ws.setWkGLin(9);

        invokePrivate("closeFilesAndFinish");

        verify(ordhfSpy).close();
        verify(orddfSpy).close();
        verify(custfSpy).close();
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

        assertEquals("RP0090", ws.getKaProgid().trim());
        assertEquals("23", ws.getKaFsts().trim());
        assertEquals("EOPEN", ws.getKaMsgcode().trim());
        assertEquals("Report file error", ws.getKaDetail().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── MAIN-000 — full integration flow ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_noOrders_runsFullPipelineAndCompletesCleanly() {
        doReturn(true).when(ordhfSpy).isInvalidKey();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

        assertEquals(0, ws.getCompletionCode());
        verify(ordhfSpy).close();
        verify(repfSpy).close();
        verify(repfSpy, atLeastWrite(1)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_ordhfOpenErrorStatus99_abendsAndNeverReachesPrintOrTerm() {
        doReturn("99").when(ordhfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

        assertEquals(255, ws.getCompletionCode());
        verify(ordhfSpy, never()).close();
        verify(abortxService).execute(any());
    }

    private static org.mockito.verification.VerificationMode atLeastWrite(int n) {
        return org.mockito.Mockito.atLeast(n);
    }
}

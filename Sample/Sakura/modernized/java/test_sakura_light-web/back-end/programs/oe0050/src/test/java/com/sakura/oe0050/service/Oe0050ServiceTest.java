package com.sakura.oe0050.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.oe0050.runtime.Oe0050Datasets;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.OrddfDataset;
import com.sakura.runtime.io.OrdhfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.RepfDataset;
import com.sakura.runtime.io.SyscfDataset;
import com.sakura.runtime.linkage.AbortxLinkParm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Oe0050Service (COBOL OE0050 — batch order acknowledgement / picking list report),
 * generated from {@code OE0050.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: ORDHF/ORDDF/CUSTF/PRODF/SYSCF/REPF are real dataset objects wrapped with {@code
 * spy()} so the record buffer (and therefore {@code Oe0050FieldAccess}) behaves exactly as in
 * production; only the I/O methods (open/close/write/readByKey/start/readNext/isAtEnd/
 * isInvalidKey) are stubbed so no real file access happens. DATEUT runs for real (pure calendar
 * math, no assertions depend on the exact date printed). ABORTX is mocked.
 *
 * <p>REPF has no readable buffer after execute() (a fresh WorkingStorage per run and no public
 * accessor on the service), so every report line written is captured via a {@code doAnswer} on
 * {@code repf.write()} into {@link #reportLines} in write order — the batch-report analogue of
 * capturing screen output. Assertions read that list rather than any post-execute buffer state.
 *
 * <p>Console input (WK-IN-FROM / WK-IN-TO, COBOL {@code ACCEPT ... FROM CONSOLE}) is read via the
 * static {@code Utility.readStdinLine()}. Left un-mocked it returns null (no COB_SYSIN env var in
 * the test process), which the service already treats as blank input — giving the full default
 * range (0 .. 9999999999) for free in every test that does not care about the range. Tests that
 * exercise PARM-RTN's range parsing wrap it in {@code mockStatic(Utility.class,
 * CALLS_REAL_METHODS)} and stub only {@code readStdinLine()}, so every other Utility helper
 * (isNumeric/fieldEquals/...) keeps running for real.
 *
 * <p>Ground-truth review of OE0050.cob vs Oe0050Service.java found the conversion faithful for
 * every branch below (INIT-RTN file-open/abort gating, PARM-RTN numeric-range parsing, the
 * PRT-RTN/READ-HDR/PROCESS-ORDER/PRINT-DETAILS control flow, SET-STAT-TXT's EVALUATE, and the
 * CHECK-PAGE(-HDR)/PAGE-HEAD pagination) — no CONVERT-GAP cases were found for this module.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Oe0050ServiceTest {

    private static final long ORDER_NO = 1000000123L;
    private static final int CUST_CODE = 100;
    private static final int PROD_CODE = 55555555;

    @Mock private AbortxService abortxService;

    private DateutService dateutService;

    private Oe0050Datasets fileSet;
    private OrdhfDataset ordhf;
    private OrddfDataset orddf;
    private CustfDataset custf;
    private ProdfDataset prodf;
    private SyscfDataset syscf;
    private RepfDataset repf;

    private Oe0050Service service;

    private final List<String> reportLines = new ArrayList<>();

    @BeforeEach
    void setUp() {
        dateutService = new DateutService();

        Oe0050Datasets real = new Oe0050Datasets();
        fileSet = spy(real);
        ordhf = spy(real.getOrdhf());
        orddf = spy(real.getOrddf());
        custf = spy(real.getCustf());
        prodf = spy(real.getProdf());
        syscf = spy(real.getSyscf());
        repf = spy(real.getRepf());
        doReturn(ordhf).when(fileSet).getOrdhf();
        doReturn(orddf).when(fileSet).getOrddf();
        doReturn(custf).when(fileSet).getCustf();
        doReturn(prodf).when(fileSet).getProdf();
        doReturn(syscf).when(fileSet).getSyscf();
        doReturn(repf).when(fileSet).getRepf();

        doNothing().when(ordhf).open(any());
        doNothing().when(orddf).open(any());
        doNothing().when(custf).open(any());
        doNothing().when(prodf).open(any());
        doNothing().when(syscf).open(any());
        doNothing().when(repf).open(any());
        doNothing().when(ordhf).close();
        doNothing().when(orddf).close();
        doNothing().when(custf).close();
        doNothing().when(prodf).close();
        doNothing().when(syscf).close();
        doNothing().when(repf).close();

        // Default: START on ORDHF/ORDDF is not the invalid-key path (WK-MAIN-EOF/WK-DTL-EOF
        // stay whatever the surrounding test set) unless a test overrides isInvalidKey().
        doReturn(true).when(ordhf).start(any(), any());
        doReturn(true).when(orddf).start(any(), any());

        // Default: no header records at all -> RH loop never runs, readNext() never invoked.
        doReturn(true).when(ordhf).isInvalidKey();
        // Default: no detail lines for whichever order is being processed.
        doReturn(true).when(orddf).isInvalidKey();
        // Default: customer/product lookups miss -> "(unknown)" fallback text.
        doReturn(true).when(custf).isInvalidKey();
        doReturn(true).when(prodf).isInvalidKey();
        // Default: SYSCF company-name lookup misses -> literal default company name kept.
        doReturn(true).when(syscf).isInvalidKey();

        doReturn(true).when(custf).readByKey(any());
        doReturn(true).when(prodf).readByKey(any());
        doReturn(true).when(syscf).readByKey(any());

        doAnswer(
                        inv -> {
                            reportLines.add(repf.getRecord().getString("REP-REC"));
                            return null;
                        })
                .when(repf)
                .write();

        service = new Oe0050Service(fileSet, dateutService, abortxService);
    }

    @AfterEach
    void tearDown() {
        reportLines.clear();
    }

    /** Scripts one ORDHF header record returned by a single readNext() cycle, then EOF. */
    private void scriptSingleOrderHeader(long orderNo, int delFlag, int status, int custCode) {
        AtomicInteger readCount = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int n = readCount.incrementAndGet();
                            if (n == 1) {
                                ordhf.getRecord().setLong("OH-NO", orderNo);
                                ordhf.getRecord().setInt("OH-DEL-FLAG", delFlag);
                                ordhf.getRecord().setInt("OH-STATUS", status);
                                ordhf.getRecord().setInt("OH-DATE", 20260101);
                                ordhf.getRecord().setInt("OH-DUE-DATE", 20260115);
                                ordhf.getRecord().setInt("OH-CUST", custCode);
                                ordhf.getRecord().setInt("OH-STAFF", 7);
                                ordhf.getRecord().setInt("OH-WHSE", 10);
                                ordhf.getRecord().setString("OH-CUST-PO", "PO-999");
                            }
                            return n == 1;
                        })
                .when(ordhf)
                .readNext();
        doAnswer(inv -> readCount.get() > 1).when(ordhf).isAtEnd();
        doReturn(false).when(ordhf).isInvalidKey();
    }

    /** Scripts one ORDDF detail line for the current order, then EOF (OD-NO diverges). */
    private void scriptSingleDetailLine(
            long orderNo,
            int prodCode,
            BigDecimal qty,
            BigDecimal shippedQty,
            BigDecimal unitPrice,
            BigDecimal amount) {
        AtomicInteger readCount = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int n = readCount.incrementAndGet();
                            if (n == 1) {
                                orddf.getRecord().setLong("OD-NO", orderNo);
                                orddf.getRecord().setInt("OD-LINE", 1);
                                orddf.getRecord().setInt("OD-PROD", prodCode);
                                orddf.getRecord().setDecimal("OD-QTY", qty);
                                orddf.getRecord().setDecimal("OD-SHIPPED-QTY", shippedQty);
                                orddf.getRecord().setDecimal("OD-UNIT-PRICE", unitPrice);
                                orddf.getRecord().setDecimal("OD-AMOUNT", amount);
                            } else {
                                // Second read returns a different order number so RD-010 sees OD-NO
                                // != OH-NO.
                                orddf.getRecord().setLong("OD-NO", orderNo + 1);
                            }
                            return true;
                        })
                .when(orddf)
                .readNext();
        doReturn(false).when(orddf).isAtEnd();
        doReturn(false).when(orddf).isInvalidKey();
    }

    // ───────────────────────── INIT-RTN (file open / abort gating) ─────────────────────────

    @Test
    void execute_syscfFound_overridesDefaultCompanyName() {
        doReturn(false).when(syscf).isInvalidKey();
        syscf.getRecord().setString("SY-COMPANY-NAME", "Acme Trading Co");

        service.execute();

        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains("Acme Trading Co"));
        verify(syscf, times(1)).close();
    }

    @Test
    void execute_syscfNotFound_keepsDefaultCompanyName() {
        service.execute();

        assertThat(reportLines)
                .anySatisfy(l -> assertThat(l).contains("SAKURA Sales Management System"));
    }

    @Test
    void execute_syscfOpenFails_skipsLookupAndKeepsDefaultCompanyName() {
        doReturn("99").when(syscf).getFileStatus();

        service.execute();

        verify(syscf, never()).readByKey(any());
        verify(syscf, never()).close();
        assertThat(reportLines)
                .anySatisfy(l -> assertThat(l).contains("SAKURA Sales Management System"));
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_ordhfOpenStatus35_setsMainEofButDoesNotAbort() {
        doReturn("35").when(ordhf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(ordhf, never()).readNext();
        verify(orddf, times(1)).open(any());
        assertThat(reportLines)
                .anySatisfy(l -> assertThat(l).contains("NO ORDERS IN THE SELECTED RANGE"));
    }

    @Test
    void execute_ordhfOpenStatus30_setsMainEofButDoesNotAbort() {
        doReturn("30").when(ordhf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(ordhf, never()).readNext();
    }

    @Test
    void execute_ordhfOpenFailsWithUnexpectedStatus_abortsWithCompletionCode255() {
        doReturn("99").when(ordhf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(orddf, never()).open(any());
        verify(repf, never()).open(any());
    }

    @Test
    void execute_ordhfOpenFails_abortxCalledWithOrdhfFileNameAndFsts() {
        doReturn("99").when(ordhf).getFileStatus();

        service.execute();

        org.mockito.ArgumentCaptor<AbortxLinkParm> captor =
                org.mockito.ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("ORDHF");
        assertThat(captor.getValue().getKabend().getKaFsts().trim()).isEqualTo("99");
    }

    @Test
    void execute_repfOpenFails_abortsWithCompletionCode255AndRepfFileName() {
        doReturn("99").when(repf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        org.mockito.ArgumentCaptor<AbortxLinkParm> captor =
                org.mockito.ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("REPF");
        // Every input file still opens before REPF's open is checked.
        verify(ordhf, times(1)).open(any());
        verify(orddf, times(1)).open(any());
        verify(custf, times(1)).open(any());
        verify(prodf, times(1)).open(any());
    }

    @Test
    void execute_normalRun_closesAllFilesAndCompletesWithZero() {
        service.execute();

        verify(ordhf, times(1)).close();
        verify(orddf, times(1)).close();
        verify(custf, times(1)).close();
        verify(prodf, times(1)).close();
        verify(repf, times(1)).close();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    // ───────────────────────── PARM-RTN (console range parsing) ─────────────────────────

    @Test
    void execute_blankRangeInputs_defaultsToFullOrderRange() {
        // No header records at all is fine here — this test only checks the page-header line
        // built from WK-ORD-FROM/WK-ORD-TO, which is always printed via the "no orders" path.
        try (MockedStatic<Utility> util = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn(null, null);

            service.execute();

            assertThat(reportLines)
                    .anySatisfy(l -> assertThat(l).contains("ORDER RANGE 0000000000 - 9999999999"));
        }
    }

    @Test
    void execute_numericRangeInputs_parsedIntoOrderRange() {
        try (MockedStatic<Utility> util = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("1000000100", "1000000200");

            service.execute();

            assertThat(reportLines)
                    .anySatisfy(l -> assertThat(l).contains("ORDER RANGE 1000000100 - 1000000200"));
        }
    }

    @Test
    void execute_nonNumericFromInput_fallsBackToZero() {
        try (MockedStatic<Utility> util = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("ABCDEFGHIJ", "1000000200");

            service.execute();

            assertThat(reportLines)
                    .anySatisfy(l -> assertThat(l).contains("ORDER RANGE 0000000000 - 1000000200"));
        }
    }

    @Test
    void execute_nonNumericToInput_fallsBackToHighest() {
        try (MockedStatic<Utility> util = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("1000000100", "ZZZZZZZZZZ");

            service.execute();

            assertThat(reportLines)
                    .anySatisfy(l -> assertThat(l).contains("ORDER RANGE 1000000100 - 9999999999"));
        }
    }

    // ───────────────────────── PRT-RTN / READ-HDR / PROCESS-ORDER main loop
    // ─────────────────────────

    @Test
    void execute_noOrdersInRange_writesNoOrdersMessage() {
        service.execute();

        assertThat(reportLines)
                .anySatisfy(l -> assertThat(l).contains("NO ORDERS IN THE SELECTED RANGE"));
        assertThat(reportLines).noneMatch(l -> l.contains("ORDER TOTAL"));
    }

    @Test
    void execute_orderWithinRangeNotDeleted_printsHeaderAndCustomerBlock() {
        scriptSingleOrderHeader(ORDER_NO, 0, 0, CUST_CODE);
        doReturn(false).when(custf).isInvalidKey();
        custf.getRecord().setString("CU-NAME", "Beta Customer");

        service.execute();

        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains(String.valueOf(ORDER_NO)));
        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains("Beta Customer"));
        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains("Entered"));
        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains("PO-999"));
        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains("GRAND TOTAL"));
    }

    @Test
    void execute_orderNumberAboveRangeTo_stopsReadingImmediately() {
        // WK-ORD-TO defaults to 9999999999 via blank stdin; force an explicit low "to" so the
        // scripted header (ORDER_NO) exceeds it and READ-HDR sets WK-MAIN-EOF without processing.
        try (MockedStatic<Utility> util = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("0", "1000000000");
            scriptSingleOrderHeader(ORDER_NO, 0, 0, CUST_CODE);

            service.execute();

            assertThat(reportLines).noneMatch(l -> l.contains(String.valueOf(ORDER_NO)));
            assertThat(reportLines)
                    .anySatisfy(l -> assertThat(l).contains("NO ORDERS IN THE SELECTED RANGE"));
        }
    }

    @Test
    void execute_orderDeleted_skipsOrderEntirely() {
        scriptSingleOrderHeader(ORDER_NO, 1, 0, CUST_CODE);

        service.execute();

        verify(custf, never()).readByKey(any());
        assertThat(reportLines).noneMatch(l -> l.contains(String.valueOf(ORDER_NO)));
        assertThat(reportLines)
                .anySatisfy(l -> assertThat(l).contains("NO ORDERS IN THE SELECTED RANGE"));
    }

    // ───────────────────────── SET-STAT-TXT (EVALUATE OH-STATUS) ─────────────────────────

    @Test
    void execute_orderStatusAllocated_printsAllocatedText() {
        scriptSingleOrderHeader(ORDER_NO, 0, 1, CUST_CODE);
        service.execute();
        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains("Allocated"));
    }

    @Test
    void execute_orderStatusPartShip_printsPartShipText() {
        scriptSingleOrderHeader(ORDER_NO, 0, 2, CUST_CODE);
        service.execute();
        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains("Part-ship"));
    }

    @Test
    void execute_orderStatusShipped_printsShippedText() {
        scriptSingleOrderHeader(ORDER_NO, 0, 3, CUST_CODE);
        service.execute();
        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains("Shipped"));
    }

    @Test
    void execute_orderStatusInvoiced_printsInvoicedText() {
        scriptSingleOrderHeader(ORDER_NO, 0, 4, CUST_CODE);
        service.execute();
        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains("Invoiced"));
    }

    @Test
    void execute_orderStatusCancelled_printsCancelledText() {
        scriptSingleOrderHeader(ORDER_NO, 0, 9, CUST_CODE);
        service.execute();
        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains("Cancelled"));
    }

    @Test
    void execute_orderStatusOutOfRange_printsQuestionMark() {
        scriptSingleOrderHeader(ORDER_NO, 0, 7, CUST_CODE);
        service.execute();
        // RD-ORD1's "STATUS " FILLER is PIC X(8) holding the 7-char literal "STATUS " ->
        // COBOL space-pads it to 8 chars ("STATUS  "), so two spaces precede O1-STAT.
        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains("STATUS  ?"));
    }

    // ───────────────────────── LOOKUP-CUST / LOOKUP-PROD ─────────────────────────

    @Test
    void execute_customerNotFound_printsUnknownCustomer() {
        scriptSingleOrderHeader(ORDER_NO, 0, 0, CUST_CODE);
        // custf.isInvalidKey() stays true (default) -> "(unknown)"

        service.execute();

        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains("(unknown)"));
    }

    @Test
    void execute_productNotFound_printsUnknownProduct() {
        scriptSingleOrderHeader(ORDER_NO, 0, 0, CUST_CODE);
        scriptSingleDetailLine(
                ORDER_NO,
                PROD_CODE,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                new BigDecimal("12.50"),
                new BigDecimal("125.00"));
        // prodf.isInvalidKey() stays true (default) -> "(unknown)"

        service.execute();

        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains("(unknown)"));
    }

    // ───────────────────────── PRINT-DETAILS / PRINT-ONE-DTL / PRINT-ORDER-TOTAL
    // ─────────────────────────

    @Test
    void execute_detailLine_printsOutstandingQtyAndAddsToOrderNet() {
        scriptSingleOrderHeader(ORDER_NO, 0, 0, CUST_CODE);
        scriptSingleDetailLine(
                ORDER_NO,
                PROD_CODE,
                new BigDecimal("10"),
                new BigDecimal("3"),
                new BigDecimal("12.50"),
                new BigDecimal("125.00"));
        doReturn(false).when(prodf).isInvalidKey();
        prodf.getRecord().setString("PR-NAME", "Widget Deluxe");

        service.execute();

        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains("Widget Deluxe"));
        // D-OUT = OD-QTY(10) - OD-SHIPPED-QTY(3) = 7
        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains(String.valueOf(PROD_CODE)));
        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains("ORDER TOTAL"));
        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains("GRAND TOTAL"));
    }

    @Test
    void execute_noDetailLines_stillPrintsOrderTotalOfZero() {
        scriptSingleOrderHeader(ORDER_NO, 0, 0, CUST_CODE);
        // orddf.isInvalidKey() stays true (default) -> PD-010's START reports invalid key,
        // WK-DTL-EOF=1 immediately, no detail lines printed.

        service.execute();

        verify(orddf, never()).readNext();
        assertThat(reportLines).anySatisfy(l -> assertThat(l).contains("ORDER TOTAL"));
    }

    // ───────────────────────── CHECK-PAGE-HDR / CHECK-PAGE / PAGE-HEAD pagination
    // ─────────────────────────

    @Test
    void execute_multipleOrdersTriggerPageBreak_printsMultiplePageHeaders() {
        // Three orders in sequence: WK-LINE starts at 99 (forces the very first page break),
        // then each order emits 3 header lines (ORD1/ORD2/COLH) + 2 total lines (OTOT + blank),
        // for 5 lines per order with no detail lines. CHECK-PAGE-HDR triggers at WK-LINE>=50,
        // so several orders in a row eventually force a second page header.
        AtomicInteger readCount = new AtomicInteger(0);
        long[] orderNos = {1000000001L, 1000000002L, 1000000003L};
        doAnswer(
                        inv -> {
                            int n = readCount.incrementAndGet();
                            if (n <= orderNos.length) {
                                ordhf.getRecord().setLong("OH-NO", orderNos[n - 1]);
                                ordhf.getRecord().setInt("OH-DEL-FLAG", 0);
                                ordhf.getRecord().setInt("OH-STATUS", 0);
                                ordhf.getRecord().setInt("OH-CUST", CUST_CODE);
                            }
                            return n <= orderNos.length;
                        })
                .when(ordhf)
                .readNext();
        doAnswer(inv -> readCount.get() > orderNos.length).when(ordhf).isAtEnd();
        doReturn(false).when(ordhf).isInvalidKey();

        service.execute();

        long pageHeaderCount = reportLines.stream().filter(l -> l.contains("PAGE:")).count();
        assertThat(pageHeaderCount).isGreaterThanOrEqualTo(1);
        for (long orderNo : orderNos) {
            assertThat(reportLines)
                    .anySatisfy(l -> assertThat(l).contains(String.valueOf(orderNo)));
        }
    }

    // ───────────────────────── PRINT-GRAND (grand total) ─────────────────────────

    @Test
    void execute_ordersProcessed_grandTotalReflectsOrderCountAndNet() {
        scriptSingleOrderHeader(ORDER_NO, 0, 0, CUST_CODE);
        scriptSingleDetailLine(
                ORDER_NO,
                PROD_CODE,
                BigDecimal.ONE,
                BigDecimal.ZERO,
                new BigDecimal("50.00"),
                new BigDecimal("50.00"));

        service.execute();

        assertThat(reportLines)
                .anySatisfy(l -> assertThat(l).contains("GRAND TOTAL").contains("ORD"));
        // With a single order in the run, GT-ORD (PIC ZZZ,ZZ9) prints as "1".
        assertThat(reportLines).anyMatch(l -> l.contains("GRAND TOTAL") && l.trim().endsWith("1"));
    }

    // ───────────────────────── CONVERT-GAP: REP-REC not fully re-padded on a shorter MOVE
    // ─────────────────────────

    @Test
    void execute_orderTotalLineTrailingBytes_shouldBeSpacePadded_convertGap() {
        // CONVERT-GAP: COBOL "MOVE RD-OTOT TO REP-REC" is a group-level alphanumeric MOVE, which
        // COBOL defines as space-padding the ENTIRE 132-byte REP-REC beyond RD-OTOT's own 60
        // bytes. Oe0050Service#writeOrderTotalLine -> ws.copyRepRecFromRdOtot() delegates to a
        // raw copyBytes("REP-REC", "RD-OTOT") that only overwrites RD-OTOT's own length and
        // leaves REP-REC's tail holding whatever the immediately preceding write (RD-COLH, 104
        // bytes, written by processOrderHeader() just before PRINT-DETAILS) left there. So this
        // line's tail — ground truth: blank — instead shows stray "PRICE"/"AMOUNT"/"OUTST"
        // column-header text bleeding through from RD-COLH.
        scriptSingleOrderHeader(ORDER_NO, 0, 0, CUST_CODE);

        service.execute();

        String orderTotalLine =
                reportLines.stream()
                        .filter(l -> l.contains("ORDER TOTAL"))
                        .findFirst()
                        .orElseThrow();
        assertThat(orderTotalLine.substring(60).trim()).isEmpty(); // COBOL ground truth: blank
    }
}

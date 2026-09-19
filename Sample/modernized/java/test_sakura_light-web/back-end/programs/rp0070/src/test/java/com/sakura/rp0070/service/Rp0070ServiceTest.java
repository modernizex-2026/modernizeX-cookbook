package com.sakura.rp0070.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0070.runtime.Rp0070Datasets;
import com.sakura.runtime.io.ArlfDataset;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.RepfDataset;
import com.sakura.runtime.io.SyscfDataset;
import com.sakura.runtime.linkage.AbortxLinkParm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Rp0070Service (COBOL RP0070 - accounts-receivable aging report), generated from
 * {@code RP0070.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: ARLF/CUSTF/SYSCF/REPF are real dataset objects wrapped with {@code spy()} so
 * the record buffer (and therefore {@code Rp0070FieldAccess}, which registers those buffers at
 * construction time) works exactly as in production; only I/O methods
 * (open/close/start/readNext/isAtEnd/readByKey/isInvalidKey/getFileStatus/ write) are stubbed so no
 * real file/DB access happens. DATEUT runs for real (pure calendar math) so aging bucket boundaries
 * are computed against the live "today" via LocalDate.now(), exactly matching AG-010's
 * DIFF(AL-DATE, WK-SYSDATE). ABORTX is mocked. Every REPF.write() call is captured into {@link
 * #reportLines} (the padded REP-REC content at the moment of the call) so report content/order can
 * be asserted directly, since there is no screen renderer to snapshot working-storage from.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Rp0070ServiceTest {

    private record LedgerRow(int cust, int date, BigDecimal debit, BigDecimal credit) {}

    private DateutService dateutService;
    private AbortxService abortxService;

    private Rp0070Datasets fileSet;
    private ArlfDataset arlf;
    private CustfDataset custf;
    private SyscfDataset syscf;
    private RepfDataset repf;

    private Rp0070Service service;

    private final List<String> reportLines = new ArrayList<>();
    private final List<LedgerRow> ledgerRows = new ArrayList<>();
    private final Map<Integer, String> custNamesByCode = new HashMap<>();
    private final AtomicInteger ledgerIndex = new AtomicInteger(0);
    private final AtomicBoolean arlfAtEnd = new AtomicBoolean(false);
    private final AtomicBoolean arlfStartInvalid = new AtomicBoolean(false);
    private final AtomicBoolean custFound = new AtomicBoolean(false);

    private static final LocalDate TODAY = LocalDate.now();

    private static int ymd(LocalDate d) {
        return d.getYear() * 10000 + d.getMonthValue() * 100 + d.getDayOfMonth();
    }

    @BeforeEach
    void setUp() {
        dateutService = new DateutService();
        abortxService = org.mockito.Mockito.mock(AbortxService.class);

        Rp0070Datasets real = new Rp0070Datasets();
        fileSet = spy(real);
        arlf = spy(real.getArlf());
        custf = spy(real.getCustf());
        syscf = spy(real.getSyscf());
        repf = spy(real.getRepf());
        doReturn(arlf).when(fileSet).getArlf();
        doReturn(custf).when(fileSet).getCustf();
        doReturn(syscf).when(fileSet).getSyscf();
        doReturn(repf).when(fileSet).getRepf();

        doNothing().when(arlf).open(any());
        doNothing().when(arlf).close();
        doReturn("00").when(arlf).getFileStatus();
        doReturn(true).when(arlf).start(any(), any());
        doAnswer(inv -> arlfStartInvalid.get()).when(arlf).isInvalidKey();
        doAnswer(
                        inv -> {
                            int idx = ledgerIndex.getAndIncrement();
                            if (idx < ledgerRows.size()) {
                                LedgerRow r = ledgerRows.get(idx);
                                arlf.getRecord().setInt("AL-CUST", r.cust());
                                arlf.getRecord().setInt("AL-DATE", r.date());
                                arlf.getRecord().setDecimal("AL-DEBIT", r.debit());
                                arlf.getRecord().setDecimal("AL-CREDIT", r.credit());
                                arlfAtEnd.set(false);
                                return true;
                            }
                            arlfAtEnd.set(true);
                            return false;
                        })
                .when(arlf)
                .readNext();
        doAnswer(inv -> arlfAtEnd.get()).when(arlf).isAtEnd();

        doNothing().when(custf).open(any());
        doNothing().when(custf).close();
        doReturn("00").when(custf).getFileStatus();
        doAnswer(
                        inv -> {
                            int code = custf.getRecord().getInt("CU-CODE");
                            String name = custNamesByCode.get(code);
                            if (name != null) {
                                custf.getRecord().setString("CU-NAME", name);
                                custFound.set(true);
                            } else {
                                custFound.set(false);
                            }
                            return custFound.get();
                        })
                .when(custf)
                .readByKey(any());
        doAnswer(inv -> !custFound.get()).when(custf).isInvalidKey();

        doNothing().when(syscf).open(any());
        doNothing().when(syscf).close();
        doReturn("00").when(syscf).getFileStatus();
        doReturn(false).when(syscf).isInvalidKey();
        doReturn(true).when(syscf).readByKey(any());
        syscf.getRecord().setString("SY-COMPANY-NAME", "Default Sakura Co");

        doNothing().when(repf).open(any());
        doNothing().when(repf).close();
        doReturn("00").when(repf).getFileStatus();
        doAnswer(
                        inv -> {
                            reportLines.add(repf.getRecord().getString("REP-REC"));
                            return null;
                        })
                .when(repf)
                .write();

        service = new Rp0070Service(fileSet, dateutService, abortxService);
    }

    private void addLedgerLine(int cust, LocalDate date, long debit, long credit) {
        ledgerRows.add(
                new LedgerRow(
                        cust, ymd(date), BigDecimal.valueOf(debit), BigDecimal.valueOf(credit)));
    }

    private void addCustomer(int code, String name) {
        custNamesByCode.put(code, name);
    }

    /** Fixed-offset field extraction, per RD-LINE / RT-LINE copybook layout (see RP0070.cob). */
    private static String seg(String line, int start, int len) {
        return line.substring(start, start + len);
    }

    private static long editedNumber(String field) {
        String cleaned = field.trim().replace(",", "");
        return cleaned.isEmpty() ? 0L : Long.parseLong(cleaned);
    }

    private static int raCode(String line) {
        return Integer.parseInt(seg(line, 1, 6).trim());
    }

    private static String raName(String line) {
        return seg(line, 8, 22).trim();
    }

    private static long raB0(String line) {
        return editedNumber(seg(line, 31, 15));
    }

    private static long raB1(String line) {
        return editedNumber(seg(line, 47, 15));
    }

    private static long raB2(String line) {
        return editedNumber(seg(line, 63, 15));
    }

    private static long raB3(String line) {
        return editedNumber(seg(line, 79, 15));
    }

    private static long raTot(String line) {
        return editedNumber(seg(line, 95, 15));
    }

    // ───────────────────────── happy path (ground truth: PROCESS-ONE / AGE-ONE / FLUSH-CUST)
    // ─────────────────────────

    @Test
    void execute_singleCustomerAcrossAllFourBuckets_writesDetailLineAndGrandTotal() {
        addCustomer(100, "Acme Corporation");
        addLedgerLine(100, TODAY.minusDays(10), 500, 100); // net=400  -> B0
        addLedgerLine(100, TODAY.minusDays(45), 200, 0); // net=200  -> B1
        addLedgerLine(100, TODAY.minusDays(75), 0, 50); // net=-50  -> B2
        addLedgerLine(100, TODAY.minusDays(120), 1000, 0); // net=1000 -> B3

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(repf, times(8)).write(); // 5 page-header lines + 1 detail + 2 grand-total lines
        String detail = reportLines.get(5);
        assertThat(raCode(detail)).isEqualTo(100);
        assertThat(raName(detail)).isEqualTo("Acme Corporation");
        assertThat(raB0(detail)).isEqualTo(400L);
        assertThat(raB1(detail)).isEqualTo(200L);
        assertThat(raB2(detail)).isEqualTo(-50L);
        assertThat(raB3(detail)).isEqualTo(1000L);
        assertThat(raTot(detail)).isEqualTo(1550L);
        String grandTotal = reportLines.get(7);
        assertThat(raB0(grandTotal)).isEqualTo(400L);
        assertThat(raTot(grandTotal)).isEqualTo(1550L);
        verify(arlf, times(1)).close();
        verify(custf, times(1)).close();
        verify(repf, times(1)).close();
        verify(abortxService, never()).execute(any());
    }

    @Test
    void execute_twoCustomers_flushesOnCustomerBreakAndSumsGrandTotals() {
        addCustomer(100, "First Customer");
        addCustomer(200, "Second Customer");
        addLedgerLine(100, TODAY.minusDays(5), 300, 0); // net=300 -> B0
        addLedgerLine(200, TODAY.minusDays(5), 700, 0); // net=700 -> B0

        service.execute();

        // 5 header lines (first flush only, WK-LINE starts at 99) + 2 detail + 2 grand-total lines
        verify(repf, times(9)).write();
        String detail1 = reportLines.get(5);
        String detail2 = reportLines.get(6);
        assertThat(raCode(detail1)).isEqualTo(100);
        assertThat(raB0(detail1)).isEqualTo(300L);
        assertThat(raCode(detail2)).isEqualTo(200);
        assertThat(raB0(detail2)).isEqualTo(700L);
        String grandTotal = reportLines.get(8);
        assertThat(raB0(grandTotal)).isEqualTo(1000L);
        assertThat(raTot(grandTotal)).isEqualTo(1000L);
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_customerNetBalanceZero_skipsDetailLineAndGrandTotalContribution() {
        addCustomer(100, "Zero Balance Co");
        addLedgerLine(
                100, TODAY.minusDays(5), 100, 100); // net=0 -> WK-CUR-BAL=0 -> FC-010 returns early

        service.execute();

        // FC-010 returns before CHECK-PAGE for this customer; PG-010 (WK-G-CNT=0) triggers the
        // page header itself, then writes the "no outstanding receivables" message.
        verify(repf, times(6)).write();
        assertThat(reportLines.get(5).trim()).isEqualTo("*** NO OUTSTANDING RECEIVABLES ***");
        verify(custf, never()).readByKey(any());
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_customerNotFoundInCustf_usesUnknownPlaceholderName() {
        addLedgerLine(
                100, TODAY.minusDays(5), 500, 0); // net=500, no addCustomer(100, ...) -> not found

        service.execute();

        String detail = reportLines.get(5);
        assertThat(raName(detail)).isEqualTo("(unknown)");
        assertThat(raB0(detail)).isEqualTo(500L);
    }

    @Test
    void execute_noLedgerRecords_printsNoOutstandingReceivablesMessage() {
        service.execute();

        verify(repf, times(6)).write(); // 5 header lines + message
        assertThat(reportLines.get(5).trim()).isEqualTo("*** NO OUTSTANDING RECEIVABLES ***");
        verify(arlf, times(1)).readNext();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_arlfStartInvalidKey_treatsAsNoRecordsWithoutReadingArlf() {
        arlfStartInvalid.set(true);
        addLedgerLine(100, TODAY.minusDays(5), 100, 0); // never reached: START fails first

        service.execute();

        verify(arlf, never()).readNext();
        assertThat(reportLines.get(5).trim()).isEqualTo("*** NO OUTSTANDING RECEIVABLES ***");
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    // ───────────────────────── edge (ground truth: INIT-010 SYSCF / ARLF status handling)
    // ─────────────────────────

    @Test
    void execute_syscfReadSuccess_overridesCompanyNameInReportHeader() {
        syscf.getRecord().setString("SY-COMPANY-NAME", "Acme SAKURA HQ");

        service.execute();

        assertThat(reportLines.get(0)).contains("Acme SAKURA HQ");
    }

    @Test
    void execute_syscfOpenFails_keepsHardcodedDefaultCompanyName() {
        doReturn("99").when(syscf).getFileStatus();

        service.execute();

        verify(syscf, never()).readByKey(any());
        assertThat(reportLines.get(0)).contains("SAKURA Sales Management System");
    }

    @Test
    void execute_syscfReadInvalidKey_keepsHardcodedDefaultCompanyName() {
        doReturn(true).when(syscf).isInvalidKey();

        service.execute();

        verify(syscf, times(1)).close();
        assertThat(reportLines.get(0)).contains("SAKURA Sales Management System");
    }

    @Test
    void execute_arlfFileStatus35_notAbortedTreatedAsEmptyFile() {
        doReturn("35").when(arlf).getFileStatus();

        service.execute();

        verify(abortxService, never()).execute(any());
        verify(arlf, never()).start(any(), any());
        verify(arlf, never()).readNext();
        verify(custf, times(1)).open(any());
        verify(repf, times(1)).open(any());
        assertThat(reportLines.get(5).trim()).isEqualTo("*** NO OUTSTANDING RECEIVABLES ***");
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_arlfFileStatus30_notAbortedTreatedAsEmptyFile() {
        doReturn("30").when(arlf).getFileStatus();

        service.execute();

        verify(abortxService, never()).execute(any());
        verify(arlf, never()).readNext();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    // ───────────────────────── error (ground truth: INIT-010 ABEND-RTN) ─────────────────────────

    @Test
    void execute_arlfOpenUnexpectedStatus_abortsWithCompletionCode255() {
        doReturn("99").when(arlf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService, times(1)).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("ARLF");
        verify(custf, never()).open(any());
        verify(repf, never()).open(any());
    }

    @Test
    void execute_repfOpenFails_abortsWithCompletionCode255() {
        doReturn("99").when(repf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService, times(1)).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("REPF");
        verify(arlf, times(1)).open(any());
        verify(custf, times(1)).open(any());
        verify(repf, never()).write();
    }

    // ───────────────────────── aging bucket boundaries (ground truth: AG-010 EVALUATE)
    // ─────────────────────────

    @Test
    void execute_agingBoundaryDays_assignsToCorrectBucketAtEachEdge() {
        addCustomer(100, "Boundary Co");
        addLedgerLine(100, TODAY.minusDays(30), 1, 0); // <=30 -> B0
        addLedgerLine(100, TODAY.minusDays(31), 1, 0); // <=60 -> B1
        addLedgerLine(100, TODAY.minusDays(60), 1, 0); // <=60 -> B1
        addLedgerLine(100, TODAY.minusDays(61), 1, 0); // <=90 -> B2
        addLedgerLine(100, TODAY.minusDays(90), 1, 0); // <=90 -> B2
        addLedgerLine(100, TODAY.minusDays(91), 1, 0); // >90  -> B3

        service.execute();

        String detail = reportLines.get(5);
        assertThat(raB0(detail)).isEqualTo(1L);
        assertThat(raB1(detail)).isEqualTo(2L);
        assertThat(raB2(detail)).isEqualTo(2L);
        assertThat(raB3(detail)).isEqualTo(1L);
    }

    @Test
    void execute_futureLedgerDate_negativeAgeBucketedIntoZeroToThirty() {
        addCustomer(100, "Future Dated Co");
        addLedgerLine(100, TODAY.plusDays(5), 50, 0); // WK-AGE negative, still <= 30 -> B0

        service.execute();

        String detail = reportLines.get(5);
        assertThat(raB0(detail)).isEqualTo(50L);
        assertThat(raB1(detail)).isEqualTo(0L);
        assertThat(raB2(detail)).isEqualTo(0L);
        assertThat(raB3(detail)).isEqualTo(0L);
    }
}

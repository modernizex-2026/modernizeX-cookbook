package com.sakura.rp0120.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0120.domain.Rp0120FieldAccess;
import com.sakura.rp0120.runtime.Rp0120Datasets;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.InvhfDataset;
import com.sakura.runtime.io.RepfDataset;
import com.sakura.runtime.io.StaffDataset;
import com.sakura.runtime.io.SyscfDataset;
import com.sakura.runtime.linkage.AbortxLinkParm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ground truth: RP0120.cob (SAKURA Sales Management System — sales-rep performance report). Every
 * input/expected value below is derived from the COBOL PROCEDURE DIVISION, not from the Java under
 * test.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Rp0120ServiceTest {

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;

    private Rp0120Datasets fileSet;
    private InvhfDataset invhf;
    private StaffDataset staff;
    private SyscfDataset syscf;
    private RepfDataset repf;
    private Rp0120FieldAccess testWs;
    private Rp0120Service service;

    private final List<String> reportLines = new ArrayList<>();
    private final Map<Integer, String> staffNames = new HashMap<>();
    private final AtomicBoolean staffInvalid = new AtomicBoolean(false);
    private final AtomicBoolean syscfInvalid = new AtomicBoolean(false);

    private static final class Invoice {
        final int date;
        final int staffCode;
        final int delFlag;
        final int status;
        final long amount;
        final long taxAmount;
        final long total;
        final long costTotal;

        Invoice(
                int date,
                int staffCode,
                int delFlag,
                int status,
                long amount,
                long taxAmount,
                long total,
                long costTotal) {
            this.date = date;
            this.staffCode = staffCode;
            this.delFlag = delFlag;
            this.status = status;
            this.amount = amount;
            this.taxAmount = taxAmount;
            this.total = total;
            this.costTotal = costTotal;
        }
    }

    @BeforeEach
    void setUp() {
        Rp0120Datasets real = new Rp0120Datasets();
        fileSet = spy(real);
        invhf = spy(real.getInvhf());
        staff = spy(real.getStaff());
        syscf = spy(real.getSyscf());
        repf = spy(real.getRepf());
        doReturn(invhf).when(fileSet).getInvhf();
        doReturn(staff).when(fileSet).getStaff();
        doReturn(syscf).when(fileSet).getSyscf();
        doReturn(repf).when(fileSet).getRepf();

        // default I/O stubs: everything "opens fine", no real disk/DB access
        doNothing().when(invhf).open(any());
        doNothing().when(invhf).close();
        doReturn("00").when(invhf).getFileStatus();
        doReturn(false).when(invhf).isInvalidKey();
        doReturn(true).when(invhf).start(anyString(), anyString());

        doNothing().when(staff).open(any());
        doNothing().when(staff).close();
        doReturn("00").when(staff).getFileStatus();

        doNothing().when(syscf).open(any());
        doNothing().when(syscf).close();
        doReturn("00").when(syscf).getFileStatus();

        doNothing().when(repf).open(any());
        doNothing().when(repf).close();
        doReturn("00").when(repf).getFileStatus();

        service = new Rp0120Service(fileSet, dateutService, abortxService);
        // testWs shares the same underlying record buffers as the service's own
        // WorkingStorage/file buffers (same spy instances) — lets the test set
        // invoice/staff/syscf input fields and read back the REP-REC output.
        testWs = new Rp0120FieldAccess(null, fileSet);

        doAnswer(
                        inv -> {
                            String key = String.valueOf((Object) inv.getArgument(0)).trim();
                            Integer code = key.isEmpty() ? null : Integer.valueOf(key);
                            if (code != null && staffNames.containsKey(code)) {
                                testWs.setSfName(staffNames.get(code));
                                staffInvalid.set(false);
                            } else {
                                staffInvalid.set(true);
                            }
                            return !staffInvalid.get();
                        })
                .when(staff)
                .readByKey(anyString());
        doAnswer(inv -> staffInvalid.get()).when(staff).isInvalidKey();

        doAnswer(inv -> !syscfInvalid.get()).when(syscf).readByKey(anyString());
        doAnswer(inv -> syscfInvalid.get()).when(syscf).isInvalidKey();

        doAnswer(
                        inv -> {
                            reportLines.add(testWs.getRepRec());
                            return null;
                        })
                .when(repf)
                .write();
    }

    @AfterEach
    void tearDown() {
        reportLines.clear();
        staffNames.clear();
        staffInvalid.set(false);
        syscfInvalid.set(false);
    }

    /** Feeds INVHF READ NEXT with a fixed list of invoices, then EOF. */
    private void stubInvoices(List<Invoice> invoices) {
        AtomicInteger idx = new AtomicInteger(0);
        AtomicBoolean atEnd = new AtomicBoolean(false);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i >= invoices.size()) {
                                atEnd.set(true);
                                return false;
                            }
                            Invoice rec = invoices.get(i);
                            testWs.setIhDate(rec.date);
                            testWs.setIhNo(i + 1);
                            testWs.setIhStaff(rec.staffCode);
                            testWs.setIhDelFlag(rec.delFlag);
                            testWs.setIhStatus(rec.status);
                            testWs.setIhAmount(BigDecimal.valueOf(rec.amount));
                            testWs.setIhTaxAmount(BigDecimal.valueOf(rec.taxAmount));
                            testWs.setIhTotal(BigDecimal.valueOf(rec.total));
                            testWs.setIhCostTotal(BigDecimal.valueOf(rec.costTotal));
                            atEnd.set(false);
                            return true;
                        })
                .when(invhf)
                .readNext();
        doAnswer(inv -> atEnd.get()).when(invhf).isAtEnd();
    }

    private void stubStdin(MockedStatic<Utility> utilMock, String from, String to) {
        utilMock.when(Utility::readStdinLine).thenReturn(from, to);
    }

    private String field(String line, int start, int endExclusive) {
        return line.substring(start, endExclusive).trim();
    }

    private long numericField(String line, int start, int endExclusive) {
        String raw = field(line, start, endExclusive).replace(",", "");
        return raw.isEmpty() ? 0L : Long.parseLong(raw);
    }

    // ── happy path ──────────────────────────────────────────────────────

    @Test
    void execute_happyPath_sortsStaffAscendingAndWritesGrandTotal() {
        // SYSCF read succeeds by default (syscfInvalid=false) — give it a
        // company name so WK-COMPANY isn't overwritten with blank.
        testWs.setSyCompanyName("SAKURA Sales Management System");
        staffNames.put(10, "Alice");
        staffNames.put(20, "Bob");
        stubInvoices(
                List.of(
                        new Invoice(20260110, 20, 0, 1, 100, 10, 110, 60),
                        new Invoice(20260111, 10, 0, 1, 200, 20, 220, 150)));
        doAnswer(
                        inv -> {
                            DateutLinkParmSet(inv.getArgument(0));
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        try (MockedStatic<Utility> util = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            stubStdin(util, "", "");
            service.execute();
        }

        assertThat(service.getCompletionCode()).isEqualTo(0);
        // header: [0]=RPT-H1 [1]=RPT-H2 [2]=blank [3]=RC-HEAD [4]=RPT-RULE
        assertThat(field(reportLines.get(0), 1, 41)).isEqualTo("SAKURA Sales Management System");
        assertThat(field(reportLines.get(0), 45, 90)).isEqualTo("SALES REP PERFORMANCE");
        assertThat(field(reportLines.get(1), 25, 95)).isEqualTo("PERIOD 00000000 - 99999999");

        String line1 = reportLines.get(5);
        String line2 = reportLines.get(6);
        assertThat(numericField(line1, 1, 5)).isEqualTo(10);
        assertThat(field(line1, 6, 30)).isEqualTo("Alice");
        assertThat(numericField(line2, 1, 5)).isEqualTo(20);
        assertThat(field(line2, 6, 30)).isEqualTo("Bob");

        String grandTotal = reportLines.get(reportLines.size() - 1);
        assertThat(numericField(grandTotal, 31, 37)).isEqualTo(2);
        assertThat(numericField(grandTotal, 38, 53)).isEqualTo(300);
        assertThat(numericField(grandTotal, 54, 69)).isEqualTo(30);
        assertThat(numericField(grandTotal, 70, 85)).isEqualTo(330);
        assertThat(numericField(grandTotal, 86, 101)).isEqualTo(120);
    }

    // helper kept separate so the dateut mock body stays readable
    private void DateutLinkParmSet(Object paramsObj) {
        com.sakura.runtime.linkage.DateutLinkParm params =
                (com.sakura.runtime.linkage.DateutLinkParm) paramsObj;
        params.getKdate().setKdDate1(20260115);
    }

    @Test
    void execute_customDateRangeFromStdin_appliesToHeaderPeriod() {
        stubInvoices(List.of());
        try (MockedStatic<Utility> util = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            stubStdin(util, "20240101", "20241231");
            service.execute();
        }
        assertThat(field(reportLines.get(1), 25, 95)).isEqualTo("PERIOD 20240101 - 20241231");
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_invoiceDateAfterWkDateTo_stopsGatherLoopBeforeAccumulating() {
        staffNames.put(10, "Alice");
        stubInvoices(
                List.of(
                        new Invoice(20240501, 10, 0, 1, 50, 5, 55, 20),
                        new Invoice(20241231, 10, 0, 1, 999, 999, 999, 999)));
        try (MockedStatic<Utility> util = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            stubStdin(util, "20240101", "20240601");
            service.execute();
        }
        verify(invhf, org.mockito.Mockito.times(2)).readNext();
        String grandTotal = reportLines.get(reportLines.size() - 1);
        assertThat(numericField(grandTotal, 31, 37)).isEqualTo(1);
        assertThat(numericField(grandTotal, 38, 53)).isEqualTo(50);
    }

    @Test
    void execute_deletedOrCancelledInvoicesSkipped_writesNoInvoicesMessage() {
        stubInvoices(
                List.of(
                        new Invoice(20260101, 10, 1, 1, 100, 10, 110, 60),
                        new Invoice(20260102, 20, 0, 9, 100, 10, 110, 60)));
        try (MockedStatic<Utility> util = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            stubStdin(util, "", "");
            service.execute();
        }
        // header(5 lines) + "no invoices" message, no RPT-RULE/RT-LINE grand total pair
        assertThat(reportLines).hasSize(6);
        assertThat(reportLines.get(5).trim())
                .isEqualTo("*** NO INVOICES IN THE SELECTED PERIOD ***");
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_staffTableOverflow_secondDistinctStaffSkippedWhenTableFull() throws Exception {
        Field wsField = Rp0120Service.class.getDeclaredField("ws");
        wsField.setAccessible(true);
        Rp0120FieldAccess serviceWs = (Rp0120FieldAccess) wsField.get(service);
        // CONVERT-GAP setup helper — WK-ST-MAX is a hard-coded COBOL constant (300);
        // shrinking it here to 1 exercises the overflow branch without needing 301 records.
        serviceWs.setWkStMax(1);

        staffNames.put(10, "Alice");
        staffNames.put(20, "Bob");
        stubInvoices(
                List.of(
                        new Invoice(20260101, 10, 0, 1, 100, 10, 110, 60),
                        new Invoice(20260102, 20, 0, 1, 500, 50, 550, 300)));
        try (MockedStatic<Utility> util = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            stubStdin(util, "", "");
            service.execute();
        }
        String grandTotal = reportLines.get(reportLines.size() - 1);
        assertThat(numericField(grandTotal, 31, 37)).isEqualTo(1);
        assertThat(numericField(grandTotal, 38, 53)).isEqualTo(100);
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_staffLookupUnknown_marksUnknownStaffOnReportLine() {
        stubInvoices(List.of(new Invoice(20260101, 99, 0, 1, 10, 1, 11, 5)));
        try (MockedStatic<Utility> util = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            stubStdin(util, "", "");
            service.execute();
        }
        String detail = reportLines.get(5);
        assertThat(field(detail, 6, 30)).isEqualTo("(unknown staff)");
    }

    @Test
    void execute_staffCodeZero_marksNoRepAssignedAndSkipsStaffLookup() {
        stubInvoices(List.of(new Invoice(20260101, 0, 0, 1, 10, 1, 11, 5)));
        try (MockedStatic<Utility> util = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            stubStdin(util, "", "");
            service.execute();
        }
        String detail = reportLines.get(5);
        assertThat(field(detail, 6, 30)).isEqualTo("(no rep assigned)");
        verify(staff, never()).readByKey(anyString());
    }

    @Test
    void execute_syscfInvalidKey_keepsDefaultCompanyName() {
        syscfInvalid.set(true);
        stubInvoices(List.of());
        try (MockedStatic<Utility> util = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            stubStdin(util, "", "");
            service.execute();
        }
        assertThat(field(reportLines.get(0), 1, 41)).isEqualTo("SAKURA Sales Management System");
    }

    @Test
    void execute_syscfOpenNotOk_skipsCompanyLookupEntirely() {
        doReturn("35").when(syscf).getFileStatus();
        stubInvoices(List.of());
        try (MockedStatic<Utility> util = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            stubStdin(util, "", "");
            service.execute();
        }
        verify(syscf, never()).readByKey(anyString());
        verify(syscf, never()).close();
        assertThat(field(reportLines.get(0), 1, 41)).isEqualTo("SAKURA Sales Management System");
    }

    @Test
    void execute_invhfOpenStatusInvalid_abortsBeforeStaffAndRepfOpen() {
        doReturn("93").when(invhf).getFileStatus();
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        try (MockedStatic<Utility> util = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            stubStdin(util, "", "");
            service.execute();
        }
        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("INVHF");
        verify(staff, never()).open(any());
        verify(repf, never()).open(any());
    }

    @Test
    void execute_repfOpenStatusInvalid_abortsWithReprFileTag() {
        doReturn("99").when(repf).getFileStatus();
        stubInvoices(List.of());
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        try (MockedStatic<Utility> util = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            stubStdin(util, "", "");
            service.execute();
        }
        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("REPF");
        // GATHER-RTN never ran — report body never written
        assertThat(reportLines).isEmpty();
    }

    @Test
    void execute_invhfStartInvalidKey_setsEofBeforeAnyRead() {
        doReturn(true).when(invhf).isInvalidKey();
        stubInvoices(List.of(new Invoice(20260101, 10, 0, 1, 10, 1, 11, 5)));
        try (MockedStatic<Utility> util = mockStatic(Utility.class, CALLS_REAL_METHODS)) {
            stubStdin(util, "", "");
            service.execute();
        }
        verify(invhf, never()).readNext();
        assertThat(reportLines.get(5).trim())
                .isEqualTo("*** NO INVOICES IN THE SELECTED PERIOD ***");
    }
}

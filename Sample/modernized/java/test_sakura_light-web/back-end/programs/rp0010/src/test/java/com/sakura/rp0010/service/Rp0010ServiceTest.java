package com.sakura.rp0010.service;

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
import com.sakura.rp0010.domain.Rp0010FieldAccess;
import com.sakura.rp0010.runtime.Rp0010Datasets;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.RegnfDataset;
import com.sakura.runtime.io.RepfDataset;
import com.sakura.runtime.io.StaffDataset;
import com.sakura.runtime.io.SyscfDataset;
import com.sakura.runtime.linkage.DateutLinkParm;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Rp0010Service (COBOL RP0010 — customer master list report), generated from {@code
 * RP0010.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: CUSTF/REGNF/STAFF/SYSCF/REPF are real dataset objects wrapped with {@code
 * spy()} so the record buffer (and therefore {@code Rp0010FieldAccess}, which registers those
 * buffers) works exactly as in production; only I/O methods
 * (open/close/write/start/readNext/readByKey/getFileStatus/isInvalidKey/isAtEnd) are stubbed so no
 * real file/DB access happens. DATEUT is mocked to return a fixed "today" (its TODY function reads
 * the real system clock, which would make WK-SYSDATE non-deterministic). ABORTX is mocked. Console
 * input (from/to customer code prompts) goes through the static {@code Utility.readStdinLine()} —
 * mocked via MockedStatic with CALLS_REAL_METHODS so every other Utility helper (fieldEquals,
 * isNumeric, ...) still runs for real.
 *
 * <p>Report output (REP-REC) is a single flat buffer reused for every WRITE. A separate {@code
 * Rp0010FieldAccess} (resultWs), wired to the same fileSet, decodes it. Because WK-LINE starts at
 * 99 (always {@literal >}= 55), the very first customer record always triggers a page-header write
 * first, so write-call indices are deterministic: 0-4 = header lines, 5.. = one write per
 * non-deleted customer detail line (in input order), then 2 more writes for the grand-total
 * section. A snapshot of the decoded fields is captured at every write() call (into {@link
 * #writeSnapshots}) so per-customer detail lines can be inspected even though later writes
 * overwrite the shared buffer.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Rp0010ServiceTest {

    private static final int FIXED_SYSDATE = 20260315;

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;

    private Rp0010Datasets fileSet;
    private CustfDataset custf;
    private RegnfDataset regnf;
    private StaffDataset staff;
    private SyscfDataset syscf;
    private RepfDataset repf;

    private Rp0010Service service;
    private Rp0010FieldAccess resultWs;

    private MockedStatic<Utility> mockedUtility;

    private final List<Map<String, Object>> writeSnapshots = new ArrayList<>();
    private final AtomicInteger startCuCode = new AtomicInteger(Integer.MIN_VALUE);

    @BeforeEach
    void setUp() {
        Rp0010Datasets real = new Rp0010Datasets();
        fileSet = spy(real);
        custf = spy(real.getCustf());
        regnf = spy(real.getRegnf());
        staff = spy(real.getStaff());
        syscf = spy(real.getSyscf());
        repf = spy(real.getRepf());
        doReturn(custf).when(fileSet).getCustf();
        doReturn(regnf).when(fileSet).getRegnf();
        doReturn(staff).when(fileSet).getStaff();
        doReturn(syscf).when(fileSet).getSyscf();
        doReturn(repf).when(fileSet).getRepf();

        doNothing().when(custf).open(any());
        doNothing().when(regnf).open(any());
        doNothing().when(staff).open(any());
        doNothing().when(syscf).open(any());
        doNothing().when(repf).open(any());
        doNothing().when(custf).close();
        doNothing().when(regnf).close();
        doNothing().when(staff).close();
        doNothing().when(syscf).close();
        doNothing().when(repf).close();

        doReturn("00").when(custf).getFileStatus();
        doReturn("00").when(regnf).getFileStatus();
        doReturn("00").when(staff).getFileStatus();
        doReturn("00").when(syscf).getFileStatus();
        doReturn("00").when(repf).getFileStatus();

        doReturn(false).when(custf).isInvalidKey();
        doReturn(false).when(regnf).isInvalidKey();
        doReturn(false).when(staff).isInvalidKey();
        doReturn(false).when(syscf).isInvalidKey();

        doReturn(true).when(regnf).readByKey(any());
        doReturn(true).when(staff).readByKey(any());
        doReturn(true).when(syscf).readByKey(any());

        // Captures the CU-CODE moved into the buffer immediately before START (ground
        // truth: PRINT-010 "MOVE WK-CUST-FROM TO CU-CODE; START CUSTF KEY NOT < CU-CODE").
        doAnswer(
                        inv -> {
                            startCuCode.set(custf.getRecord().getInt("CU-CODE"));
                            return true;
                        })
                .when(custf)
                .start(any(), any());

        // DATEUT stub: only TODY is used by RP0010 (INIT-010), fixed "today".
        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            p.getKdate().setKdDate1(FIXED_SYSDATE);
                            p.getKdate().setKdStatus("00");
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        mockedUtility = mockStatic(Utility.class, CALLS_REAL_METHODS);

        service = new Rp0010Service(fileSet, dateutService, abortxService);
        resultWs = new Rp0010FieldAccess(null, fileSet);

        // NOTE: H1-COMPANY / RC-CODE / RC-NAME / RT-CREDIT etc. are WORKING-STORAGE
        // group items (RPT-H1/RD-CUST/RT-CUST) byte-copied into REP-REC just before
        // WRITE; they are not part of REPF's own FD scope, so Rp0010FieldAccess
        // (registered against fileSet only, no WorkingStorage) cannot resolve those
        // names. Only "REP-REC" itself (the FD 01-item) is resolvable here. Decode the
        // fixed-width sub-fields by column offset, computed from the RD-CUST/RT-CUST/
        // RPT-H1 copybook layouts (both RD-CUST and RT-CUST place their money columns
        // at the same [78,93)/[94,109) offsets, so one pair of offsets covers detail
        // and grand-total lines alike).
        doAnswer(
                        inv -> {
                            String repRec = resultWs.getRepRec();
                            if (repRec == null) {
                                repRec = "";
                            }
                            StringBuilder padded = new StringBuilder(repRec);
                            while (padded.length() < 132) {
                                padded.append(' ');
                            }
                            repRec = padded.toString();
                            Map<String, Object> snap = new LinkedHashMap<>();
                            snap.put("H1-COMPANY", repRec.substring(1, 41).trim());
                            snap.put("RC-CODE", repRec.substring(1, 7).trim());
                            snap.put("RC-NAME", repRec.substring(9, 39).trim());
                            snap.put("RC-REGION", repRec.substring(40, 58).trim());
                            snap.put("RC-STAFF", repRec.substring(59, 77).trim());
                            snap.put("AMOUNT1", digitsOnly(repRec.substring(78, 93)));
                            snap.put("AMOUNT2", digitsOnly(repRec.substring(94, 109)));
                            snap.put("REP-REC", repRec.trim());
                            writeSnapshots.add(snap);
                            return null;
                        })
                .when(repf)
                .write();

        stubConsole("", "");
        stubCustomers(List.of());
    }

    @AfterEach
    void tearDown() {
        mockedUtility.close();
        writeSnapshots.clear();
    }

    /** Stubs the two console prompts (from customer code, then to customer code) in order. */
    private void stubConsole(String fromInput, String toInput) {
        mockedUtility.when(Utility::readStdinLine).thenReturn(fromInput, toInput);
    }

    /** Strips a COBOL numeric-edited amount (commas/spaces/sign) down to its digit sequence. */
    private static long digitsOnly(String editedAmount) {
        String digits = editedAmount.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0L : Long.parseLong(digits);
    }

    private static final class Customer {
        final int code;
        final String name;
        final int region;
        final int staffCode;
        final long credit;
        final long balance;
        final int delFlag;

        Customer(
                int code,
                String name,
                int region,
                int staffCode,
                long credit,
                long balance,
                int delFlag) {
            this.code = code;
            this.name = name;
            this.region = region;
            this.staffCode = staffCode;
            this.credit = credit;
            this.balance = balance;
            this.delFlag = delFlag;
        }
    }

    /** Feeds {@code customers} through CUSTF.readNext()/isAtEnd() in order, then AT END. */
    private void stubCustomers(List<Customer> customers) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i < customers.size()) {
                                Customer c = customers.get(i);
                                custf.getRecord().setInt("CU-CODE", c.code);
                                custf.getRecord().setString("CU-NAME", c.name);
                                custf.getRecord().setInt("CU-REGION", c.region);
                                custf.getRecord().setInt("CU-STAFF", c.staffCode);
                                custf.getRecord()
                                        .setDecimal(
                                                "CU-CREDIT-LIMIT", BigDecimal.valueOf(c.credit));
                                custf.getRecord()
                                        .setDecimal("CU-BALANCE", BigDecimal.valueOf(c.balance));
                                custf.getRecord().setInt("CU-DEL-FLAG", c.delFlag);
                                return true;
                            }
                            return false;
                        })
                .when(custf)
                .readNext();
        doAnswer(inv -> idx.get() > customers.size()).when(custf).isAtEnd();
    }

    // ───────────────────────── happy path ─────────────────────────

    @Test
    void execute_twoCustomersOneDeleted_writesDetailLineForActiveCustomerAndAccumulatesTotals() {
        regnf.getRecord().setString("RG-NAME", "North Region");
        staff.getRecord().setString("SF-NAME", "John Smith");
        stubCustomers(
                List.of(
                        new Customer(100, "Alpha Co", 10, 20, 5000, 1200, 0),
                        new Customer(200, "Beta Ltd", 0, 0, 3000, 400, 1)));

        service.execute();

        assertThat(writeSnapshots).hasSize(8);
        Map<String, Object> detail = writeSnapshots.get(5);
        assertThat(detail.get("RC-CODE")).isEqualTo("000100");
        assertThat(detail.get("RC-NAME")).isEqualTo("Alpha Co");
        assertThat(detail.get("RC-REGION")).isEqualTo("North Region");
        assertThat(detail.get("RC-STAFF")).isEqualTo("John Smith");
        assertThat(detail.get("AMOUNT1")).isEqualTo(5000L);
        assertThat(detail.get("AMOUNT2")).isEqualTo(1200L);

        Map<String, Object> grandTotal = writeSnapshots.get(writeSnapshots.size() - 1);
        assertThat(grandTotal.get("AMOUNT1")).isEqualTo(5000L);
        assertThat(grandTotal.get("AMOUNT2")).isEqualTo(1200L);
        verify(regnf, times(1)).readByKey(any());
        verify(staff, times(1)).readByKey(any());
        verify(custf, times(1)).close();
        verify(regnf, times(1)).close();
        verify(staff, times(1)).close();
        verify(repf, times(1)).close();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_multipleActiveCustomers_accumulatesGrandTotalAcrossAllOfThem() {
        stubCustomers(
                List.of(
                        new Customer(100, "A", 0, 0, 1000, 200, 0),
                        new Customer(101, "B", 0, 0, 2500, 750, 0)));

        service.execute();

        verify(repf, times(9)).write();
        Map<String, Object> grandTotal = writeSnapshots.get(writeSnapshots.size() - 1);
        assertThat(grandTotal.get("AMOUNT1")).isEqualTo(3500L);
        assertThat(grandTotal.get("AMOUNT2")).isEqualTo(950L);
    }

    // ───────────────────────── SYSCF company-name lookup (ground truth: INIT-010)
    // ─────────────────────────

    @Test
    void execute_syscfInvalidKey_keepsDefaultCompanyName() {
        doReturn(true).when(syscf).isInvalidKey();

        service.execute();

        assertThat(writeSnapshots.get(0).get("H1-COMPANY"))
                .isEqualTo("SAKURA Sales Management System");
        verify(syscf, times(1)).readByKey(any());
        verify(syscf, times(1)).close();
    }

    @Test
    void execute_syscfFound_overridesCompanyNameFromSyCompanyName() {
        syscf.getRecord().setString("SY-COMPANY-NAME", "Sakura HQ Trading");

        service.execute();

        assertThat(writeSnapshots.get(0).get("H1-COMPANY")).isEqualTo("Sakura HQ Trading");
    }

    // ───────────────────────── customer range parsing (ground truth: PARM-010)
    // ─────────────────────────

    @Test
    void execute_numericRangeFromConsole_parsesFromCodeAndAppliesToCodeBoundary() {
        stubConsole("000150", "000300");
        stubCustomers(
                List.of(
                        new Customer(300, "AtBoundary", 0, 0, 1000, 500, 0),
                        new Customer(301, "PastBoundary", 0, 0, 999, 999, 0)));

        service.execute();

        assertThat(startCuCode.get()).isEqualTo(150);
        verify(custf, times(2)).readNext();
        Map<String, Object> grandTotal = writeSnapshots.get(writeSnapshots.size() - 1);
        assertThat(grandTotal.get("AMOUNT1")).isEqualTo(1000L);
        assertThat(grandTotal.get("AMOUNT2")).isEqualTo(500L);
    }

    @Test
    void execute_nonNumericFromInput_defaultsCustFromToZero() {
        stubConsole("ABC123", "");
        stubCustomers(List.of(new Customer(1, "First", 0, 0, 10, 5, 0)));

        service.execute();

        assertThat(startCuCode.get()).isEqualTo(0);
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    // ───────────────────────── region / staff lookups (ground truth: LOOKUP-REGION / LOOKUP-STAFF)
    // ─────────────────────────

    @Test
    void execute_regionAndStaffZeroOrNotFound_showsBlankOrUnknownAccordingly() {
        doReturn(true).when(regnf).isInvalidKey();
        doReturn(true).when(staff).isInvalidKey();
        stubCustomers(
                List.of(
                        new Customer(100, "Cust A", 0, 0, 1000, 100, 0),
                        new Customer(200, "Cust B", 99, 88, 2000, 200, 0)));

        service.execute();

        Map<String, Object> first = writeSnapshots.get(5);
        assertThat(first.get("RC-REGION")).isEqualTo("");
        assertThat(first.get("RC-STAFF")).isEqualTo("");

        Map<String, Object> second = writeSnapshots.get(6);
        assertThat(second.get("RC-REGION")).isEqualTo("(unknown)");
        assertThat(second.get("RC-STAFF")).isEqualTo("(unknown)");

        verify(regnf, times(1)).readByKey(any());
        verify(staff, times(1)).readByKey(any());
    }

    // ───────────────────────── no customers selected (ground truth: PRINT-TOTALS)
    // ─────────────────────────

    @Test
    void execute_startInvalidKey_noCustomersFoundAndReadNextNeverCalled() {
        doReturn(true).when(custf).isInvalidKey();

        service.execute();

        verify(custf, never()).readNext();
        assertThat(writeSnapshots.get(writeSnapshots.size() - 1).get("REP-REC"))
                .isEqualTo("*** NO CUSTOMERS SELECTED ***");
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    // ───────────────────────── file open lifecycle (ground truth: INIT-RTN / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_custfOpenFailsHardStatus_abortsWithCompletionCode255AndSkipsRemainingOpens() {
        doReturn("99").when(custf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(custf, times(1)).open(any());
        verify(regnf, never()).open(any());
        verify(staff, never()).open(any());
        verify(repf, never()).open(any());
    }

    @Test
    void execute_custfOpenStatus35_allowedNoAbortButMarksEofAndSkipsCustomerLoop() {
        doReturn("35").when(custf).getFileStatus();

        service.execute();

        verify(abortxService, never()).execute(any());
        verify(regnf, times(1)).open(any());
        verify(staff, times(1)).open(any());
        verify(repf, times(1)).open(any());
        verify(custf, never()).start(any(), any());
        verify(custf, never()).readNext();
        assertThat(writeSnapshots.get(writeSnapshots.size() - 1).get("REP-REC"))
                .isEqualTo("*** NO CUSTOMERS SELECTED ***");
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_repfOpenFails_abortsAfterOpeningCustfRegnfStaff() {
        doReturn("99").when(repf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(custf, times(1)).open(any());
        verify(regnf, times(1)).open(any());
        verify(staff, times(1)).open(any());
        verify(repf, never()).write();
    }
}

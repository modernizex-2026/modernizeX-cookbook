package com.sakura.bt0020.service;

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
import com.sakura.bt0020.runtime.Bt0020Datasets;
import com.sakura.dateut.service.DateutService;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.io.AplfDataset;
import com.sakura.runtime.io.ArlfDataset;
import com.sakura.runtime.io.InvhfDataset;
import com.sakura.runtime.io.PurhfDataset;
import com.sakura.runtime.io.SyscfDataset;
import com.sakura.runtime.linkage.AbortxLinkParm;
import com.sakura.runtime.linkage.DateutLinkParm;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Bt0020Service (COBOL BT0020 — monthly close), generated from {@code BT0020.cob}
 * PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: SYSCF/INVHF/PURHF/ARLF/APLF are real dataset objects wrapped with {@code spy()}
 * so the record buffer (and therefore {@code Bt0020FieldAccess}, which registers those buffers at
 * construction time) works exactly as in production; only I/O methods
 * (open/close/start/readNext/rewrite/readByKey/getFileStatus/isInvalidKey/ isAtEnd) are stubbed so
 * no real file/DB access happens. DATEUT is fully mocked (TODY reads the real system clock, which
 * would make "today" non-deterministic) — the stub reproduces just enough of TODY/EOM/ADDD
 * semantics for these fixtures. ABORTX is mocked since it is an unrelated logging subprogram.
 * Console input (target-month / confirm prompts) goes through the static {@code
 * Utility.readStdinLine()} — mocked via MockedStatic with CALLS_REAL_METHODS so every other Utility
 * helper (fieldEquals, isNumeric, padRight, ...) still runs for real.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Bt0020ServiceTest {

    private static final int FIXED_SYSDATE = 20260615;
    private static final int INITIAL_CURR_YM = 202606;
    private static final int INITIAL_LAST_MON_CLOSE = 202605;
    private static final int PER_START = 20260601;
    private static final int PER_END = 20260630;
    private static final int NEXT_YM = 202607;

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;

    private Bt0020Datasets fileSet;
    private SyscfDataset syscf;
    private InvhfDataset invhf;
    private PurhfDataset purhf;
    private ArlfDataset arlf;
    private AplfDataset aplf;

    private Bt0020Service service;

    private MockedStatic<com.sakura.runtime.Utility> mockedUtility;

    /**
     * Captures the KD-DATE1 fed INTO each "EOM " DATEUT call, before the stub overwrites it with
     * the result.
     */
    private final List<Integer> eomCalcInputs = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Bt0020Datasets real = new Bt0020Datasets();
        fileSet = spy(real);
        syscf = spy(real.getSyscf());
        invhf = spy(real.getInvhf());
        purhf = spy(real.getPurhf());
        arlf = spy(real.getArlf());
        aplf = spy(real.getAplf());
        doReturn(syscf).when(fileSet).getSyscf();
        doReturn(invhf).when(fileSet).getInvhf();
        doReturn(purhf).when(fileSet).getPurhf();
        doReturn(arlf).when(fileSet).getArlf();
        doReturn(aplf).when(fileSet).getAplf();

        for (var f : List.of(syscf, invhf, purhf, arlf, aplf)) {
            doNothing().when(f).open(any());
            doNothing().when(f).close();
            doNothing().when(f).rewrite();
            doReturn("00").when(f).getFileStatus();
            doReturn(false).when(f).isInvalidKey();
        }
        doReturn(true).when(syscf).readByKey(any());
        doReturn(true).when(invhf).start(any(), any());
        doReturn(true).when(purhf).start(any(), any());
        doReturn(true).when(arlf).start(any(), any());
        doReturn(true).when(aplf).start(any(), any());

        syscf.getRecord().setInt("SY-CURR-YM", INITIAL_CURR_YM);
        syscf.getRecord().setInt("SY-LAST-MON-CLOSE", INITIAL_LAST_MON_CLOSE);

        stubDateut();
        stubInvoices(List.of());
        stubPurchases(List.of());
        stubArEntries(List.of());
        stubApEntries(List.of());

        mockedUtility = mockStatic(com.sakura.runtime.Utility.class, CALLS_REAL_METHODS);
        stubConsole("", "Y"); // blank target month (-> default = SY-CURR-YM), confirm Y

        service = new Bt0020Service(fileSet, dateutService, abortxService);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        mockedUtility.close();
    }

    // ───────────────────────── DATEUT / console stubs ─────────────────────────

    private void stubDateut() {
        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            String func =
                                    p.getKdate().getKdFunc() == null
                                            ? ""
                                            : p.getKdate().getKdFunc().trim();
                            switch (func) {
                                case "TODY" -> {
                                    p.getKdate().setKdDate1(FIXED_SYSDATE);
                                    p.getKdate().setKdStatus("00");
                                }
                                case "EOM" -> {
                                    eomCalcInputs.add(p.getKdate().getKdDate1());
                                    p.getKdate()
                                            .setKdDate1(endOfMonthYmd(p.getKdate().getKdDate1()));
                                    p.getKdate().setKdStatus("00");
                                }
                                case "ADDD" -> {
                                    p.getKdate()
                                            .setKdDate1(
                                                    addDaysYmd(
                                                            p.getKdate().getKdDate1(),
                                                            p.getKdate().getKdDays()));
                                    p.getKdate().setKdStatus("00");
                                }
                                default -> p.getKdate().setKdStatus("99");
                            }
                            return null;
                        })
                .when(dateutService)
                .execute(any());
    }

    private static int endOfMonthYmd(int startYmd) {
        LocalDate d =
                LocalDate.parse(String.valueOf(startYmd), DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate eom = d.withDayOfMonth(d.lengthOfMonth());
        return Integer.parseInt(eom.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

    private static int addDaysYmd(int ymd, int days) {
        LocalDate d =
                LocalDate.parse(String.valueOf(ymd), DateTimeFormatter.ofPattern("yyyyMMdd"))
                        .plusDays(days);
        return Integer.parseInt(d.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

    /** Stubs the two console prompts (target month, then confirm) in order. */
    private void stubConsole(String targetMonthInput, String confirmInput) {
        mockedUtility
                .when(com.sakura.runtime.Utility::readStdinLine)
                .thenReturn(targetMonthInput, confirmInput);
    }

    // ───────────────────────── record fixtures ─────────────────────────

    private record InvoiceRec(
            int date, long no, int status, int closeYm, int kind, long amount, long taxAmount) {}

    private record PurchaseRec(
            int date, long no, int status, int closeYm, int kind, long amount, long taxAmount) {}

    private record ArRec(int cust, int date, long balance, long debit, long credit, int closeYm) {}

    private record ApRec(int supp, int date, long balance, long debit, long credit, int closeYm) {}

    private void stubInvoices(List<InvoiceRec> recs) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i < recs.size()) {
                                InvoiceRec r = recs.get(i);
                                invhf.getRecord().setInt("IH-DATE", r.date());
                                invhf.getRecord().setLong("IH-NO", r.no());
                                invhf.getRecord().setInt("IH-STATUS", r.status());
                                invhf.getRecord().setInt("IH-CLOSE-YM", r.closeYm());
                                invhf.getRecord().setInt("IH-KIND", r.kind());
                                invhf.getRecord()
                                        .setDecimal("IH-AMOUNT", BigDecimal.valueOf(r.amount()));
                                invhf.getRecord()
                                        .setDecimal(
                                                "IH-TAX-AMOUNT", BigDecimal.valueOf(r.taxAmount()));
                            }
                            return true;
                        })
                .when(invhf)
                .readNext();
        doAnswer(inv -> idx.get() > recs.size()).when(invhf).isAtEnd();
    }

    private void stubPurchases(List<PurchaseRec> recs) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i < recs.size()) {
                                PurchaseRec r = recs.get(i);
                                purhf.getRecord().setInt("VH-DATE", r.date());
                                purhf.getRecord().setLong("VH-NO", r.no());
                                purhf.getRecord().setInt("VH-STATUS", r.status());
                                purhf.getRecord().setInt("VH-CLOSE-YM", r.closeYm());
                                purhf.getRecord().setInt("VH-KIND", r.kind());
                                purhf.getRecord()
                                        .setDecimal("VH-AMOUNT", BigDecimal.valueOf(r.amount()));
                                purhf.getRecord()
                                        .setDecimal(
                                                "VH-TAX-AMOUNT", BigDecimal.valueOf(r.taxAmount()));
                            }
                            return true;
                        })
                .when(purhf)
                .readNext();
        doAnswer(inv -> idx.get() > recs.size()).when(purhf).isAtEnd();
    }

    private void stubArEntries(List<ArRec> recs) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i < recs.size()) {
                                ArRec r = recs.get(i);
                                arlf.getRecord().setInt("AL-CUST", r.cust());
                                arlf.getRecord().setInt("AL-DATE", r.date());
                                arlf.getRecord()
                                        .setDecimal("AL-BALANCE", BigDecimal.valueOf(r.balance()));
                                arlf.getRecord()
                                        .setDecimal("AL-DEBIT", BigDecimal.valueOf(r.debit()));
                                arlf.getRecord()
                                        .setDecimal("AL-CREDIT", BigDecimal.valueOf(r.credit()));
                                arlf.getRecord().setInt("AL-CLOSE-YM", r.closeYm());
                            }
                            return true;
                        })
                .when(arlf)
                .readNext();
        doAnswer(inv -> idx.get() > recs.size()).when(arlf).isAtEnd();
    }

    private void stubApEntries(List<ApRec> recs) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i < recs.size()) {
                                ApRec r = recs.get(i);
                                aplf.getRecord().setInt("PL-SUPP", r.supp());
                                aplf.getRecord().setInt("PL-DATE", r.date());
                                aplf.getRecord()
                                        .setDecimal("PL-BALANCE", BigDecimal.valueOf(r.balance()));
                                aplf.getRecord()
                                        .setDecimal("PL-DEBIT", BigDecimal.valueOf(r.debit()));
                                aplf.getRecord()
                                        .setDecimal("PL-CREDIT", BigDecimal.valueOf(r.credit()));
                                aplf.getRecord().setInt("PL-CLOSE-YM", r.closeYm());
                            }
                            return true;
                        })
                .when(aplf)
                .readNext();
        doAnswer(inv -> idx.get() > recs.size()).when(aplf).isAtEnd();
    }

    private ArgumentCaptor<AbortxLinkParm> captureAbortx() {
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService).execute(captor.capture());
        return captor;
    }

    // ───────────────────────── happy path ─────────────────────────

    @Test
    void execute_defaultMonthNoLedgerActivity_closesMonthAndAdvancesSyscf() {
        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(abortxService, never()).execute(any());
        verify(syscf, times(1)).rewrite();
        assertThat(syscf.getRecord().getInt("SY-LAST-MON-CLOSE")).isEqualTo(INITIAL_CURR_YM);
        assertThat(syscf.getRecord().getInt("SY-CURR-YM")).isEqualTo(NEXT_YM);
        assertThat(eomCalcInputs).containsExactly(PER_START);
        verify(syscf, times(1)).close();
        verify(invhf, times(1)).close();
        verify(purhf, times(1)).close();
        verify(arlf, times(1)).close();
        verify(aplf, times(1)).close();
    }

    @Test
    void execute_explicitTargetMonthInput_usesEnteredMonthForClosePeriod() {
        stubConsole("202609", "Y");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        assertThat(eomCalcInputs).containsExactly(20260901);
        assertThat(syscf.getRecord().getInt("SY-LAST-MON-CLOSE")).isEqualTo(202609);
    }

    // ───────────────────────── CLOSE-INVOICES (ground truth: CINV-010/CINX-010)
    // ─────────────────────────

    @Test
    void execute_saleInvoiceKind1PostedUnclosed_stampsCloseYmAndStatus() {
        stubInvoices(List.of(new InvoiceRec(PER_START, 1001L, 1, 0, 1, 1000, 100)));

        service.execute();

        verify(invhf, times(1)).rewrite();
        assertThat(invhf.getRecord().getInt("IH-CLOSE-YM")).isEqualTo(INITIAL_CURR_YM);
        assertThat(invhf.getRecord().getInt("IH-STATUS")).isEqualTo(2);
        assertThat(invhf.getRecord().getInt("IH-UPD-DATE")).isEqualTo(FIXED_SYSDATE);
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_creditNoteInvoiceKind2PostedUnclosed_stampsCloseYmAndStatus() {
        stubInvoices(List.of(new InvoiceRec(PER_START, 1002L, 1, 0, 2, 500, 50)));

        service.execute();

        verify(invhf, times(1)).rewrite();
        assertThat(invhf.getRecord().getInt("IH-CLOSE-YM")).isEqualTo(INITIAL_CURR_YM);
        assertThat(invhf.getRecord().getInt("IH-STATUS")).isEqualTo(2);
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_invoiceAlreadyClosedOrUnposted_skippedWithoutRewrite() {
        stubInvoices(
                List.of(
                        new InvoiceRec(
                                PER_START, 2001L, 2, INITIAL_CURR_YM, 1, 100, 10), // already closed
                        new InvoiceRec(
                                PER_START, 2002L, 0, 0, 1, 200, 20))); // not posted (status != 1)

        service.execute();

        verify(invhf, never()).rewrite();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_invoiceRewriteInvalidKey_abortsWithCompletionCode255() {
        // 1st isInvalidKey() call is the START check (must pass); 2nd is the REWRITE check (fails).
        doReturn(false, true).when(invhf).isInvalidKey();
        stubInvoices(List.of(new InvoiceRec(PER_START, 3001L, 1, 0, 1, 100, 10)));

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("INVHF");
        assertThat(captor.getValue().getKabend().getKaDetail().trim())
                .isEqualTo("REWRITE INVHF failed");
    }

    // ───────────────────────── CLOSE-PURCHASES (ground truth: CPUR-010/CPUX-010)
    // ─────────────────────────

    @Test
    void execute_purchaseDateOutsidePeriod_skippedEntirelyWithoutRewrite() {
        stubPurchases(
                List.of(
                        new PurchaseRec(PER_START - 1, 4001L, 1, 0, 1, 300, 30), // before period
                        new PurchaseRec(PER_END + 1, 4002L, 1, 0, 1, 300, 30), // after period
                        new PurchaseRec(PER_START, 4003L, 1, 0, 1, 400, 40))); // in range -> closed

        service.execute();

        verify(purhf, times(1)).rewrite();
        assertThat(purhf.getRecord().getInt("VH-CLOSE-YM")).isEqualTo(INITIAL_CURR_YM);
        assertThat(purhf.getRecord().getInt("VH-STATUS")).isEqualTo(2);
    }

    @Test
    void execute_creditNotePurchaseKind2PostedUnclosed_stampsCloseYmAndStatus() {
        stubPurchases(List.of(new PurchaseRec(PER_START, 4004L, 1, 0, 2, 250, 25)));

        service.execute();

        verify(purhf, times(1)).rewrite();
        assertThat(purhf.getRecord().getInt("VH-CLOSE-YM")).isEqualTo(INITIAL_CURR_YM);
        assertThat(purhf.getRecord().getInt("VH-STATUS")).isEqualTo(2);
    }

    @Test
    void execute_purchaseRewriteInvalidKey_abortsWithCompletionCode255() {
        // 1st isInvalidKey() call is the START check (must pass); 2nd is the REWRITE check (fails).
        doReturn(false, true).when(purhf).isInvalidKey();
        stubPurchases(List.of(new PurchaseRec(PER_START, 4005L, 1, 0, 1, 100, 10)));

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("PURHF");
        assertThat(captor.getValue().getKabend().getKaDetail().trim())
                .isEqualTo("REWRITE PURHF failed");
    }

    // ───────────────────────── ROLL-AR (ground truth: RAR-010/RARX-010/AR-BREAK)
    // ─────────────────────────

    @Test
    void execute_arMultipleCustomersControlBreak_stampsInPeriodEntriesOnly() {
        stubArEntries(
                List.of(
                        new ArRec(100, PER_START, 1500, 1000, 500, 0), // cust 100 -> stamped
                        new ArRec(
                                100,
                                PER_START + 1,
                                1600,
                                600,
                                500,
                                INITIAL_CURR_YM), // cust 100, already closed
                        new ArRec(
                                200, PER_START, 900, 900, 0,
                                0))); // cust 200 -> stamped, triggers break

        service.execute();

        verify(arlf, times(2)).rewrite();
        assertThat(arlf.getRecord().getInt("AL-CLOSE-YM")).isEqualTo(INITIAL_CURR_YM);
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_arEntryDateOutsidePeriod_notStampedNoRewrite() {
        stubArEntries(List.of(new ArRec(100, PER_START - 1, 2000, 100, 50, 0)));

        service.execute();

        verify(arlf, never()).rewrite();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_arRewriteInvalidKey_abortsWithCompletionCode255() {
        // 1st isInvalidKey() call is the START check (must pass); 2nd is the REWRITE check (fails).
        doReturn(false, true).when(arlf).isInvalidKey();
        stubArEntries(List.of(new ArRec(100, PER_START, 1000, 500, 200, 0)));

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("ARLF");
        assertThat(captor.getValue().getKabend().getKaDetail().trim())
                .isEqualTo("REWRITE ARLF failed");
    }

    // ───────────────────────── ROLL-AP (ground truth: RAP-010/RAPX-010/AP-BREAK)
    // ─────────────────────────

    @Test
    void execute_apSupplierEntryInPeriod_stampsCloseYm() {
        stubApEntries(List.of(new ApRec(300, PER_START, 700, 700, 0, 0)));

        service.execute();

        verify(aplf, times(1)).rewrite();
        assertThat(aplf.getRecord().getInt("PL-CLOSE-YM")).isEqualTo(INITIAL_CURR_YM);
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_apRewriteInvalidKey_abortsWithCompletionCode255() {
        // 1st isInvalidKey() call is the START check (must pass); 2nd is the REWRITE check (fails).
        doReturn(false, true).when(aplf).isInvalidKey();
        stubApEntries(List.of(new ApRec(300, PER_START, 700, 700, 0, 0)));

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("APLF");
        assertThat(captor.getValue().getKabend().getKaDetail().trim())
                .isEqualTo("REWRITE APLF failed");
    }

    // ───────────────────────── prompts / confirmation (ground truth:
    // ACCEPT-TARGET/VALIDATE-YM/CONFIRM-RUN) ─────────────────────────

    @Test
    void execute_nonNumericTargetMonthInput_cancelsRunWithCompletionCode0() {
        stubConsole("ABCDEF", "Y");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(abortxService, never()).execute(any());
        verify(syscf, never()).rewrite();
        verify(syscf, times(1)).close();
    }

    @Test
    void execute_targetMonthPartOutOfRange_cancelsRunWithCompletionCode0() {
        stubConsole("202513", "Y"); // month part 13 -> invalid

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(abortxService, never()).execute(any());
        verify(syscf, never()).rewrite();
    }

    @Test
    void execute_confirmDeclined_cancelsRunWithoutAnyRewrite() {
        stubConsole("", "N");
        stubInvoices(List.of(new InvoiceRec(PER_START, 5001L, 1, 0, 1, 100, 10)));

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(invhf, never()).rewrite();
        verify(syscf, never()).rewrite();
    }

    @Test
    void execute_confirmLowercaseY_stillProceedsToCloseProcessing() {
        stubConsole("", "y");
        stubInvoices(List.of(new InvoiceRec(PER_START, 5002L, 1, 0, 1, 100, 10)));

        service.execute();

        verify(invhf, times(1)).rewrite();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_computeClosePeriodEomFails_abortsWithCompletionCode255() {
        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            String func =
                                    p.getKdate().getKdFunc() == null
                                            ? ""
                                            : p.getKdate().getKdFunc().trim();
                            if ("TODY".equals(func)) {
                                p.getKdate().setKdDate1(FIXED_SYSDATE);
                                p.getKdate().setKdStatus("00");
                            } else if ("EOM".equals(func)) {
                                p.getKdate().setKdStatus("99");
                            } else {
                                p.getKdate().setKdStatus("00");
                            }
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("SYSCF");
        assertThat(captor.getValue().getKabend().getKaDetail().trim()).isEqualTo("EOM calc failed");
    }

    // ───────────────────────── file lifecycle (ground truth: OPEN-FILES / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_syscfOpenFailsPersistently_abortsWithCompletionCode255AndAbendFields() {
        doReturn("99").when(syscf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        AbortxLinkParm.Kabend kabend = captor.getValue().getKabend();
        assertThat(kabend.getKaProgid().trim()).isEqualTo("BT0020");
        assertThat(kabend.getKaMsgcode().trim()).isEqualTo("EBATCH");
        assertThat(kabend.getKaFsts().trim()).isEqualTo("99");
        assertThat(kabend.getKaFile().trim()).isEqualTo("SYSCF");
        assertThat(kabend.getKaDetail().trim()).isEqualTo("Open SYSCF failed");
        verify(invhf, never()).open(any());
    }

    @Test
    void execute_invhfOpenStatus35_reopensAsOutputThenReopensIoSucceeds() {
        doReturn("35", "00", "00", "00").when(invhf).getFileStatus();

        service.execute();

        verify(invhf, times(3)).open(any());
        verify(invhf, times(1)).open(FileOpenMode.OUTPUT);
        verify(invhf, times(2)).open(FileOpenMode.IO);
        // once from the OUTPUT/IO retry cycle, once more from the unconditional closeAllFiles at
        // program end
        verify(invhf, times(2)).close();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_purhfOpenFailsAfterRetry_abortsWithCompletionCode255() {
        doReturn("99").when(purhf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("PURHF");
        assertThat(captor.getValue().getKabend().getKaDetail().trim())
                .isEqualTo("Open PURHF failed");
        verify(arlf, never()).open(any());
    }

    @Test
    void execute_syscfControlRecordMissing_abortsWithCompletionCode255() {
        doReturn(true).when(syscf).isInvalidKey();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        assertThat(captor.getValue().getKabend().getKaDetail().trim())
                .isEqualTo("System control record missing");
    }

    // ───────────────────────── UPDATE-SYSCF (ground truth: USYS-010) ─────────────────────────

    @Test
    void execute_updateSyscfRereadFails_abortsWithCompletionCode255() {
        // 1st isInvalidKey (RSYS-010) must pass; 2nd (USYS-010 re-read) fails.
        doReturn(false, true).when(syscf).isInvalidKey();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        assertThat(captor.getValue().getKabend().getKaDetail().trim())
                .isEqualTo("Re-read SYSCF failed");
        verify(syscf, never()).rewrite();
    }

    @Test
    void execute_updateSyscfRewriteFails_abortsWithCompletionCode255() {
        // RSYS-010 read ok, USYS-010 re-read ok, USYS-010 post-REWRITE check fails.
        doReturn(false, false, true).when(syscf).isInvalidKey();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = captureAbortx();
        assertThat(captor.getValue().getKabend().getKaDetail().trim())
                .isEqualTo("REWRITE SYSCF failed");
    }
}

package com.sakura.bt0010.service;

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
import com.sakura.bt0010.runtime.Bt0010Datasets;
import com.sakura.dateut.service.DateutService;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.io.InvhfDataset;
import com.sakura.runtime.io.SyscfDataset;
import com.sakura.runtime.linkage.AbortxLinkParm;
import com.sakura.runtime.linkage.DateutLinkParm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Bt0010Service (COBOL BT0010 — daily close / sales invoice posting), generated from
 * {@code BT0010.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: INVHF/SYSCF are real dataset objects wrapped with {@code spy()} so the record
 * buffer (and therefore {@code Bt0010FieldAccess}, which registers those buffers at construction
 * time) works exactly as in production; only I/O methods
 * (open/close/start/readNext/rewrite/readByKey/getFileStatus/isInvalidKey/isAtEnd) are stubbed so
 * no real file/DB access happens. DATEUT is fully mocked (its TODY function reads the real system
 * clock, which would make "today" non-deterministic) — the stub below reproduces just enough of
 * DATEUT's TODY/ADDD/VALD semantics for these fixtures. ABORTX is mocked since it is an unrelated
 * logging subprogram. Console input (target-date / confirm prompts) goes through the static {@code
 * Utility.readStdinLine()} — mocked via MockedStatic with CALLS_REAL_METHODS so every other Utility
 * helper (fieldEquals, isNumeric, padRight, ...) still runs for real.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Bt0010ServiceTest {

    private static final int FIXED_SYSDATE = 20260315;

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;

    private Bt0010Datasets fileSet;
    private InvhfDataset invhf;
    private SyscfDataset syscf;

    private Bt0010Service service;

    private org.mockito.MockedStatic<com.sakura.runtime.Utility> mockedUtility;

    @BeforeEach
    void setUp() {
        Bt0010Datasets real = new Bt0010Datasets();
        fileSet = spy(real);
        invhf = spy(real.getInvhf());
        syscf = spy(real.getSyscf());
        doReturn(invhf).when(fileSet).getInvhf();
        doReturn(syscf).when(fileSet).getSyscf();

        doNothing().when(invhf).open(any());
        doNothing().when(invhf).close();
        doNothing().when(invhf).rewrite();
        doReturn("00").when(invhf).getFileStatus();
        doReturn(false).when(invhf).isInvalidKey();
        doReturn(false).when(invhf).isAtEnd();
        doReturn(true).when(invhf).start(any(), any());

        doNothing().when(syscf).open(any());
        doNothing().when(syscf).close();
        doNothing().when(syscf).rewrite();
        doReturn("00").when(syscf).getFileStatus();
        doReturn(false).when(syscf).isInvalidKey();
        doReturn(true).when(syscf).readByKey(any());

        syscf.getRecord().setString("SY-COMPANY-NAME", "Sakura Corp");
        syscf.getRecord().setInt("SY-LAST-DAY-CLOSE", 20260314);

        // DATEUT stub: TODY returns the fixed "today"; ADDD adds KD-DAYS to KD-DATE1;
        // VALD accepts any real yyyymmdd calendar date (rejects malformed/impossible ones).
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
                                case "ADDD" -> {
                                    p.getKdate()
                                            .setKdDate1(
                                                    addDaysYmd(
                                                            p.getKdate().getKdDate1(),
                                                            p.getKdate().getKdDays()));
                                    p.getKdate().setKdStatus("00");
                                }
                                case "VALD" -> p.getKdate()
                                        .setKdStatus(
                                                isValidYmd(p.getKdate().getKdDate1())
                                                        ? "00"
                                                        : "99");
                                default -> p.getKdate().setKdStatus("99");
                            }
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        mockedUtility =
                org.mockito.Mockito.mockStatic(
                        com.sakura.runtime.Utility.class, org.mockito.Mockito.CALLS_REAL_METHODS);

        service = new Bt0010Service(fileSet, dateutService, abortxService);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        mockedUtility.close();
    }

    private static int addDaysYmd(int ymd, int days) {
        LocalDate d =
                LocalDate.parse(String.valueOf(ymd), DateTimeFormatter.ofPattern("yyyyMMdd"))
                        .plusDays(days);
        return Integer.parseInt(d.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

    private static boolean isValidYmd(int ymd) {
        try {
            LocalDate.parse(String.valueOf(ymd), DateTimeFormatter.ofPattern("yyyyMMdd"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /** Stubs the two console prompts (target date, then confirm) in order. */
    private void stubConsole(String targetDateInput, String confirmInput) {
        mockedUtility
                .when(com.sakura.runtime.Utility::readStdinLine)
                .thenReturn(targetDateInput, confirmInput);
    }

    /** One invoice-header fixture used to drive INVHF's readNext() sequence. */
    private static final class Invoice {
        final int date;
        final long no;
        final int status;
        final int kind;
        final BigDecimal amount;
        final BigDecimal taxAmount;
        final BigDecimal total;
        final BigDecimal costTotal;

        Invoice(
                int date,
                long no,
                int status,
                int kind,
                long amount,
                long taxAmount,
                long total,
                long costTotal) {
            this.date = date;
            this.no = no;
            this.status = status;
            this.kind = kind;
            this.amount = BigDecimal.valueOf(amount);
            this.taxAmount = BigDecimal.valueOf(taxAmount);
            this.total = BigDecimal.valueOf(total);
            this.costTotal = BigDecimal.valueOf(costTotal);
        }
    }

    /** Feeds {@code invoices} through INVHF.readNext()/isAtEnd() in order, then AT END. */
    private void stubInvoices(List<Invoice> invoices) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i < invoices.size()) {
                                Invoice rec = invoices.get(i);
                                invhf.getRecord().setInt("IH-DATE", rec.date);
                                invhf.getRecord().setLong("IH-NO", rec.no);
                                invhf.getRecord().setInt("IH-STATUS", rec.status);
                                invhf.getRecord().setInt("IH-KIND", rec.kind);
                                invhf.getRecord().setDecimal("IH-AMOUNT", rec.amount);
                                invhf.getRecord().setDecimal("IH-TAX-AMOUNT", rec.taxAmount);
                                invhf.getRecord().setDecimal("IH-TOTAL", rec.total);
                                invhf.getRecord().setDecimal("IH-COST-TOTAL", rec.costTotal);
                            }
                            return true;
                        })
                .when(invhf)
                .readNext();
        doAnswer(inv -> idx.get() > invoices.size()).when(invhf).isAtEnd();
    }

    // ───────────────────────── happy path ─────────────────────────

    @Test
    void execute_defaultDateWithSaleAndReturnInvoices_postsBothAndAccumulatesTotals() {
        stubConsole(
                "", "Y"); // blank -> default date (SY-LAST-DAY-CLOSE + 1 day = FIXED_SYSDATE); Y ->
        // proceed
        stubInvoices(
                List.of(
                        new Invoice(FIXED_SYSDATE, 1001L, 0, 1, 1000, 100, 1100, 600), // sale
                        new Invoice(FIXED_SYSDATE, 1002L, 0, 2, 200, 20, 220, 120))); // return

        service.execute();

        verify(invhf, times(2)).rewrite();
        verify(syscf, times(1)).rewrite();
        assertThat(service.getCompletionCode()).isEqualTo(0);
        assertThat(syscf.getRecord().getInt("SY-LAST-DAY-CLOSE")).isEqualTo(FIXED_SYSDATE);
        assertThat(invhf.getRecord().getInt("IH-UPD-DATE")).isEqualTo(FIXED_SYSDATE);
        // 1000 - 200 = 800 net sale amount; 1100 - 220 = 880 gross; 100-20=80 tax; 600-120=480 cost
        assertThat(syscf.getRecord().getInt("SY-LAST-DAY-CLOSE")).isEqualTo(FIXED_SYSDATE);
    }

    @Test
    void execute_alreadyPostedInvoice_incrementsSkipCountWithoutRewrite() {
        stubConsole("", "Y");
        stubInvoices(
                List.of(
                        new Invoice(
                                FIXED_SYSDATE,
                                2001L,
                                1,
                                1,
                                500,
                                50,
                                550,
                                300))); // IH-STATUS already 1

        service.execute();

        verify(invhf, never()).rewrite();
        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(syscf, times(1)).rewrite();
    }

    @Test
    void execute_invoiceDateAfterTargetDate_stopsWithoutProcessingIt() {
        stubConsole("", "Y");
        stubInvoices(List.of(new Invoice(FIXED_SYSDATE + 1, 3001L, 0, 1, 500, 50, 550, 300)));

        service.execute();

        verify(invhf, never()).rewrite();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_explicitTargetDateInput_usesEnteredDateForPostingAndCloseUpdate() {
        stubConsole("20260310", "Y");
        stubInvoices(List.of(new Invoice(20260310, 4001L, 0, 1, 700, 70, 770, 400)));

        service.execute();

        verify(invhf, times(1)).rewrite();
        assertThat(syscf.getRecord().getInt("SY-LAST-DAY-CLOSE")).isEqualTo(20260310);
    }

    // ───────────────────────── edge: prompts / confirmation ─────────────────────────

    @Test
    void execute_dateutAddDaysFails_fallsBackToSystemDateAsDefault() {
        // ADDD returns a bad status this time -> INIT-010 falls back to WK-SYSDATE.
        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            String func =
                                    p.getKdate().getKdFunc() == null
                                            ? ""
                                            : p.getKdate().getKdFunc().trim();
                            if ("ADDD".equals(func)) {
                                p.getKdate().setKdStatus("99");
                            } else if ("TODY".equals(func)) {
                                p.getKdate().setKdDate1(FIXED_SYSDATE);
                                p.getKdate().setKdStatus("00");
                            } else {
                                p.getKdate().setKdStatus("00");
                            }
                            return null;
                        })
                .when(dateutService)
                .execute(any());
        stubConsole("", "Y");
        stubInvoices(List.of());

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        // default date fell back to FIXED_SYSDATE (not the ADDD result) and posting ran to
        // completion.
        verify(syscf, times(1)).rewrite();
        assertThat(syscf.getRecord().getInt("SY-LAST-DAY-CLOSE")).isEqualTo(FIXED_SYSDATE);
    }

    @Test
    void execute_nonNumericTargetDateInput_cancelsRunWithoutPosting() {
        stubConsole("ABCDEFGH", "Y");
        stubInvoices(List.of());

        service.execute();

        verify(invhf, never()).rewrite();
        verify(syscf, never()).rewrite();
        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(invhf, times(1)).close();
        verify(syscf, times(1)).close();
    }

    @Test
    void execute_invalidCalendarDateInput_cancelsRunWithoutPosting() {
        stubConsole("20261340", "Y"); // month 13 -> not a real calendar date
        stubInvoices(List.of());

        service.execute();

        verify(invhf, never()).rewrite();
        verify(syscf, never()).rewrite();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_targetDateInFuture_cancelsRunWithoutPosting() {
        stubConsole("20260316", "Y"); // one day after FIXED_SYSDATE
        stubInvoices(List.of());

        service.execute();

        verify(invhf, never()).rewrite();
        verify(syscf, never()).rewrite();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_confirmDeclined_cancelsRunWithoutPosting() {
        stubConsole("", "N");
        stubInvoices(List.of(new Invoice(FIXED_SYSDATE, 5001L, 0, 1, 100, 10, 110, 60)));

        service.execute();

        verify(invhf, never()).rewrite();
        verify(syscf, never()).rewrite();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_confirmLowercaseY_stillPosts() {
        stubConsole("", "y");
        stubInvoices(List.of(new Invoice(FIXED_SYSDATE, 6001L, 0, 1, 100, 10, 110, 60)));

        service.execute();

        verify(invhf, times(1)).rewrite();
    }

    @Test
    void execute_noInvoicesPostedForTargetDate_summaryPrintedWithoutAverageDivision() {
        stubConsole("", "Y");
        stubInvoices(
                List.of()); // WK-POST-CNT stays 0 -> average branch guarded (no divide-by-zero)

        service.execute();

        verify(invhf, never()).rewrite();
        verify(syscf, times(1)).rewrite();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    // ───────────────────────── error: file lifecycle (ground truth: OPEN-FILES / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_invhfOpenFailsPersistently_abortsWithCompletionCode255() {
        doReturn("99").when(invhf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("INVHF");
        verify(syscf, never()).open(any());
    }

    @Test
    void execute_invhfOpenStatus35_reopensAsOutputThenReopensIo() {
        doReturn("35", "00", "00", "00").when(invhf).getFileStatus();
        stubConsole(
                "ABCDEFGH", "Y"); // cancel quickly once files are open; not exercising posting here

        service.execute();

        verify(invhf, times(3)).open(any());
        verify(invhf, times(1)).open(FileOpenMode.OUTPUT);
        verify(invhf, times(2)).open(FileOpenMode.IO);
        // one CLOSE INVHF happens inside the FSTS=35 retry sequence itself, plus TERM-RTN's close.
        verify(invhf, times(2)).close();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_syscfOpenFailsPersistently_abortsWithCompletionCode255() {
        doReturn("99").when(syscf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("SYSCF");
        verify(syscf, never()).readByKey(any());
    }

    @Test
    void execute_syscfControlRecordMissing_abortsWithCompletionCode255() {
        doReturn(true).when(syscf).isInvalidKey();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaDetail().trim())
                .isEqualTo("System control record missing");
    }

    @Test
    void execute_invhfStartFails_abortsWithCompletionCode255() {
        doReturn("00", "70")
                .when(invhf)
                .getFileStatus(); // open ok, START returns an unexpected status
        stubConsole("", "Y");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaDetail().trim())
                .isEqualTo("START INVHF failed");
    }

    @Test
    void execute_readNextFails_abortsWithCompletionCode255() {
        doReturn("00", "00", "55")
                .when(invhf)
                .getFileStatus(); // open ok, start ok, READ NEXT bad status
        stubConsole("", "Y");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaDetail().trim())
                .isEqualTo("READ NEXT INVHF failed");
    }

    @Test
    void execute_invhfRewriteInvalidKey_abortsWithCompletionCode255() {
        // 1st isInvalidKey() call is START's (must be false so the read loop runs);
        // 2nd is the post-REWRITE check (fails).
        doReturn(false, true).when(invhf).isInvalidKey();
        stubConsole("", "Y");
        stubInvoices(List.of(new Invoice(FIXED_SYSDATE, 7001L, 0, 1, 100, 10, 110, 60)));

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaDetail().trim())
                .isEqualTo("REWRITE INVHF failed");
    }

    @Test
    void execute_syscfReReadFailsDuringUpdateStep_abortsWithReReadMessage() {
        // 1st readByKey/isInvalidKey (RSYS-010) must pass; 2nd (USYS-010 re-read) fails.
        doReturn(false, true).when(syscf).isInvalidKey();
        stubConsole("", "Y");
        stubInvoices(List.of());

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaDetail().trim())
                .isEqualTo("Re-read SYSCF failed");
        verify(syscf, never()).rewrite();
    }

    @Test
    void execute_syscfRewriteInvalidKeyDuringUpdateStep_abortsWithRewriteMessage() {
        // Calls in order: RSYS-010 read (must pass), USYS-010 re-read (must pass),
        // USYS-010 post-REWRITE check (fails).
        doReturn(false, false, true).when(syscf).isInvalidKey();
        stubConsole("", "Y");
        stubInvoices(List.of());

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaDetail().trim())
                .isEqualTo("REWRITE SYSCF failed");
    }
}

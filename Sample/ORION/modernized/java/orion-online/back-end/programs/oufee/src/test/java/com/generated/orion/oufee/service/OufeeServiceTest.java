package com.generated.orion.oufee.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.generated.orion.oufee.accessor.OufeeFields;

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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for OufeeService, converted from COBOL program OUFEE (fee assessment SUB). All I/O
 * (CICS READ/STARTBR/READNEXT/REWRITE/WRITE/ENDBR) is mocked via AppService; OufeeFields is
 * exercised as a real object (backed by the OUFEE_WS.xml layout) so account/control record state
 * flows naturally between mocked I/O calls, exactly as the single KOPS-AREA/working-storage buffer
 * does in the COBOL program.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OufeeServiceTest {

    private static final int RESP_NORMAL = 0;
    private static final int RESP_NOTFND = 13;
    private static final int RESP_ENDFILE = 20;
    private static final int RESP_OTHER = 99;

    @Mock private AppService appService;

    private final OufeeService service = new OufeeService();

    /** Queue of account-record setups consumed one per readNext() call. */
    private final Deque<java.util.function.Consumer<OufeeFields>> pendingAccounts =
            new ArrayDeque<>();

    @BeforeEach
    void setUp() {
        when(appService.getEibcalen()).thenReturn(0);

        doAnswer(
                        inv -> {
                            OufeeFields f = (OufeeFields) inv.getArgument(1);
                            java.util.function.Consumer<OufeeFields> setup = pendingAccounts.poll();
                            if (setup != null) {
                                setup.accept(f);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());

        // Simulates a CICS READ ... UPDATE echoing the current record back into ctx.f
        // (the same buffer instance already holds any previously READNEXT'd account data,
        // and CT-LAST-VALUE defaults to 100 unless a test overrides it below).
        doAnswer(
                        inv -> {
                            OufeeFields f = (OufeeFields) inv.getArgument(1);
                            if (f.getCtLastValue() == 0) {
                                f.setCtLastValue(100);
                            }
                            fixBrokenDisplayDefaults(f);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    private void queueAccount(
            long acId,
            String activeStatus,
            BigDecimal currBal,
            BigDecimal creditLimit,
            BigDecimal cycCredit) {
        pendingAccounts.add(
                f -> {
                    f.setAcId(acId);
                    f.setAcActiveStatus(activeStatus);
                    f.setAcCurrBal(currBal);
                    f.setAcCreditLimit(creditLimit);
                    f.setAcCycCredit(cycCredit);
                });
    }

    /**
     * Pre-existing defect in orion-common's {@code RecordBuffer.applyInitialValues()} (NOT part of
     * this OUFEE conversion): signed/decimal DISPLAY-numeric VALUE literals such as "+35.00" or
     * "+0.0200" are written as raw ASCII text instead of proper zoned-decimal bytes, so WS-LATE-FEE
     * / WS-OVLIM-FEE / WS-MIN-DUE-PCT / WS-MIN-DUE-FLOOR throw NumericValueException on first read
     * via a freshly constructed WorkingStorage. Every test re-applies the COBOL VALUE clauses here
     * (via the normal setDecimal path, which encodes correctly) before any account is evaluated, so
     * OUFEE's own business logic can be exercised despite the shared-library defect. See findings
     * note.
     */
    private void fixBrokenDisplayDefaults(OufeeFields f) {
        f.setWsLateFee(new BigDecimal("35.00"));
        f.setWsOvlimFee(new BigDecimal("29.00"));
        f.setWsMinDuePct(new BigDecimal("0.0200"));
        f.setWsMinDueFloor(new BigDecimal("25.00"));
    }

    // ── Happy path ──────────────────────────────────────────────────────────

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_singleOverLimitAccount_assessesOverlimitFeeAndPostsAll() {
        queueAccount(
                123L,
                "A",
                new BigDecimal("600.00"),
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"));
        when(appService.getEibresp())
                .thenReturn(
                        RESP_NORMAL, // 1500-LOAD-COUNTER read ctrl
                        RESP_NORMAL, // 3100-START-BROWSE
                        RESP_NORMAL, // 3200-READ-NEXT-ACCT (account #1)
                        RESP_NORMAL, // 4400-UPDATE-ACCT read-for-update
                        RESP_NORMAL, // 4400-UPDATE-ACCT rewrite
                        RESP_NORMAL, // 4500-WRITE-TRAN write
                        RESP_ENDFILE, // 3200-READ-NEXT-ACCT (end of file)
                        RESP_NORMAL, // 3400-END-BROWSE
                        RESP_NORMAL, // 5500-SAVE-COUNTER read ctrl
                        RESP_NORMAL); // 5500-SAVE-COUNTER rewrite ctrl

        OufeeFields result = runAndCapture(appService);

        assertThat(result.getKoStatus()).isEqualTo("O");
        assertThat(result.getKoStatusMsg().trim()).isEqualTo("FEE ASSESSMENT COMPLETE.");
        assertThat(result.getKoReadCnt())
                .isEqualTo(1); // only the initial readnext succeeds; the 2nd is ENDFILE
        assertThat(result.getKoSelectCnt()).isEqualTo(1);
        assertThat(result.getKoUpdateCnt()).isEqualTo(1);
        assertThat(result.getKoPostedCnt()).isEqualTo(1);
        assertThat(result.getKoRejectCnt()).isEqualTo(0);
        assertThat(result.getKoTranCnt()).isEqualTo(1);
        assertThat(result.getKoC1()).isEqualTo(1);
        assertThat(result.getKoC2()).isEqualTo(0);
        assertThat(result.getKoC3()).isEqualTo(0);
        assertThat(result.getKoAmt1()).isEqualByComparingTo("29.00");
        assertThat(result.getKoAmt3()).isEqualByComparingTo("29.00");
        assertThat(result.getAcCurrBal()).isEqualByComparingTo("629.00");
        assertThat(result.getTrTypeCd()).isEqualTo(result.getWsFeeType());
        assertThat(result.getTrAmt()).isEqualByComparingTo("29.00");
        assertThat(result.getTrSource().trim()).isEqualTo("LATEFEE");

        // rewriteFile: account balance update (4400) + control counter save (5500, ctrl found)
        verify(appService, times(2)).rewriteFile(anyString(), any());
        // writeFile: fee transaction only (ctrl record was found, so it's rewritten, not written)
        verify(appService, times(1)).writeFile(anyString(), any(), anyString(), anyInt());
        verify(appService, times(1)).endBrowse(anyString());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_singleDelinquentAccount_assessesLateFee() {
        // curr bal 200 > 0, minDue = max(200*0.02, 25) = 25.00, cycCredit(1.00) < 25 -> delinquent
        queueAccount(
                456L,
                "A",
                new BigDecimal("200.00"),
                new BigDecimal("900.00"),
                new BigDecimal("1.00"));
        when(appService.getEibresp())
                .thenReturn(
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_ENDFILE,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL);

        OufeeFields result = runAndCapture(appService);

        assertThat(result.getKoC1()).isEqualTo(0);
        assertThat(result.getKoC2()).isEqualTo(1);
        assertThat(result.getKoC3()).isEqualTo(0);
        assertThat(result.getKoAmt2()).isEqualByComparingTo("35.00");
        assertThat(result.getKoAmt1()).isEqualByComparingTo("35.00");
        assertThat(result.getAcCurrBal()).isEqualByComparingTo("235.00");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_accountOverLimitAndDelinquent_assessesBothFeesAndCountsBoth() {
        // over credit limit (600 > 500) AND delinquent (cycCredit 1.00 < minDue 25.00 floor)
        queueAccount(
                789L,
                "A",
                new BigDecimal("600.00"),
                new BigDecimal("500.00"),
                new BigDecimal("1.00"));
        when(appService.getEibresp())
                .thenReturn(
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_ENDFILE,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL);

        OufeeFields result = runAndCapture(appService);

        assertThat(result.getKoC1()).isEqualTo(1);
        assertThat(result.getKoC2()).isEqualTo(1);
        assertThat(result.getKoC3()).isEqualTo(1);
        assertThat(result.getKoAmt2()).isEqualByComparingTo("35.00");
        assertThat(result.getKoAmt3()).isEqualByComparingTo("29.00");
        assertThat(result.getKoAmt1()).isEqualByComparingTo("64.00");
    }

    // ── CONVERT-GAP: MIN-DUE rounding ────────────────────────────────────────

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_minDueBoundary_CONVERT_GAP_cobolRoundsHalfUpJavaTruncates() {
        // CONVERT-GAP: COBOL "COMPUTE WS-MIN-DUE ROUNDED = AC-CURR-BAL * WS-MIN-DUE-PCT"
        // rounds half-up to 2 decimals. The converted Java (evaluateFeeEligibility) does
        // ctx.f.setWsMinDue(currBal.multiply(minDuePct)) with no explicit rounding; the
        // underlying RecordCodec.encodeField applies RoundingMode.DOWN (truncation) when
        // the BigDecimal is written into the 2-decimal WS-MIN-DUE buffer field.
        // 1253.75 * 0.0200 = 25.0750 -> COBOL ROUNDED => 25.08, Java truncates => 25.07.
        // With AC-CYC-CREDIT = 25.07 this changes the delinquency verdict:
        //   COBOL: 25.07 < 25.08 -> DELINQUENT -> fee assessed (late fee 35.00).
        //   Java:  25.07 < 25.07 -> NOT delinquent -> no fee assessed.
        // Expected values below are the COBOL ground truth; this test is expected to FAIL
        // against the current Java implementation, correctly flagging the gap.
        queueAccount(
                999L,
                "A",
                new BigDecimal("1253.75"),
                new BigDecimal("2000.00"),
                new BigDecimal("25.07"));
        when(appService.getEibresp())
                .thenReturn(
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_ENDFILE,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL);

        OufeeFields result = runAndCapture(appService);

        assertThat(result.getKoC2()).isEqualTo(1); // CONVERT-GAP: Java produces 0
        assertThat(result.getKoAmt2())
                .isEqualByComparingTo("35.00"); // CONVERT-GAP: Java produces 0.00
        assertThat(result.getKoSelectCnt()).isEqualTo(1); // CONVERT-GAP: Java produces 0
    }

    // ── Eligibility edges ─────────────────────────────────────────────────────

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_twoAccounts_inactiveSkippedThenEligibleProcessed_accumulatesCounters() {
        queueAccount(
                1L,
                "N",
                new BigDecimal("50.00"),
                new BigDecimal("500.00"),
                new BigDecimal("100.00"));
        queueAccount(
                2L,
                "A",
                new BigDecimal("600.00"),
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"));
        when(appService.getEibresp())
                .thenReturn(
                        RESP_NORMAL, // load counter
                        RESP_NORMAL, // startbr
                        RESP_NORMAL, // readnext acct1 (inactive)
                        RESP_NORMAL, // readnext acct2 (eligible, over-limit)
                        RESP_NORMAL, // update-acct read-for-update
                        RESP_NORMAL, // update-acct rewrite
                        RESP_NORMAL, // write-tran
                        RESP_ENDFILE, // readnext -> end
                        RESP_NORMAL, // endbrowse
                        RESP_NORMAL, // save-counter read ctrl
                        RESP_NORMAL); // save-counter rewrite ctrl

        OufeeFields result = runAndCapture(appService);

        assertThat(result.getKoReadCnt())
                .isEqualTo(2); // acct1 + acct2; the final ENDFILE readnext doesn't count
        assertThat(result.getKoSkipCnt()).isEqualTo(1);
        assertThat(result.getKoSelectCnt()).isEqualTo(1);
        assertThat(result.getKoC1()).isEqualTo(1);
        assertThat(result.getKoTranCnt()).isEqualTo(1);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_accountNotEligible_noFeeAssessedNoAcctIoBeyondRead() {
        // Non-positive balance: not over limit, and AC-CURR-BAL > 0 check fails so
        // delinquency is never evaluated -> not eligible -> no update/write calls at all.
        queueAccount(
                5L,
                "A",
                new BigDecimal("0.00"),
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"));
        when(appService.getEibresp())
                .thenReturn(RESP_NORMAL, RESP_NORMAL, RESP_NORMAL, RESP_ENDFILE, RESP_NORMAL);
        // no koTranCnt -> save-counter paragraph short-circuits (KO-TRAN-CNT = ZERO -> exit),
        // so only 5 getEibresp() calls occur (load, startbr, readnext#1, readnext#2, endbrowse).

        OufeeFields result = runAndCapture(appService);

        assertThat(result.getKoSelectCnt()).isEqualTo(0);
        assertThat(result.getKoTranCnt()).isEqualTo(0);
        verify(appService, never()).rewriteFile(anyString(), any());
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    // ── Browse start / read errors ───────────────────────────────────────────

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_startBrowseNotFound_setsNoAccountsThenFinaliseOverwritesMessage() {
        when(appService.getEibresp())
                .thenReturn(
                        RESP_NORMAL, // load counter
                        RESP_NOTFND); // startbr -> NOTFND

        OufeeFields result = runAndCapture(appService);

        // 9000-FINALISE always overwrites KO-STATUS-MSG when not KO-ERROR, even though
        // 3100-START-BROWSE set "NO ACCOUNTS TO ASSESS." moments earlier — this matches
        // COBOL exactly (no CONVERT-GAP here).
        assertThat(result.getKoStatus()).isEqualTo("O");
        assertThat(result.getKoStatusMsg().trim()).isEqualTo("FEE ASSESSMENT COMPLETE.");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_startBrowseOtherError_setsErrorStatusAndSkipsBrowseEntirely() {
        when(appService.getEibresp())
                .thenReturn(
                        RESP_NORMAL, // load counter
                        RESP_OTHER); // startbr -> unexpected error

        OufeeFields result = runAndCapture(appService);

        assertThat(result.getKoStatus()).isEqualTo("E");
        assertThat(result.getKoStatusMsg().trim()).isEqualTo("STARTBR ACCTFILE FAILED.");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_readNextOtherError_setsErrorStatusAndStillEndsBrowse() {
        when(appService.getEibresp())
                .thenReturn(
                        RESP_NORMAL, // load counter
                        RESP_NORMAL, // startbr ok
                        RESP_OTHER, // readnext -> unexpected error
                        RESP_NORMAL); // endbrowse (still called since BR-STARTED was Y)

        OufeeFields result = runAndCapture(appService);

        assertThat(result.getKoStatus()).isEqualTo("E");
        assertThat(result.getKoStatusMsg().trim()).isEqualTo("READNEXT ACCTFILE FAILED.");
        verify(appService, times(1)).endBrowse(anyString());
    }

    // ── Counter load/save ────────────────────────────────────────────────────

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_loadCounterCtrlNotFound_startsSequenceFromZero() {
        doAnswer(
                        inv -> { // ctrl record missing: do NOT populate CT-LAST-VALUE
                            fixBrokenDisplayDefaults((OufeeFields) inv.getArgument(1));
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());
        queueAccount(
                1L,
                "A",
                new BigDecimal("600.00"),
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"));
        when(appService.getEibresp())
                .thenReturn(
                        RESP_NOTFND, // load counter -> not found
                        RESP_NORMAL, // startbr
                        RESP_NORMAL, // readnext acct1
                        RESP_NORMAL, // update-acct read-for-update
                        RESP_NORMAL, // update-acct rewrite
                        RESP_NORMAL, // write-tran (TR-ID built from WS-NEXT-ID-NUM = 0 + 1 = 1)
                        RESP_ENDFILE, // readnext -> end
                        RESP_NORMAL, // endbrowse
                        RESP_NOTFND, // save-counter read ctrl -> still not found -> WRITE branch
                        RESP_NORMAL); // save-counter write ctrl

        OufeeFields result = runAndCapture(appService);

        assertThat(result.getKoTranCnt()).isEqualTo(1);
        assertThat(result.getCtDesc().trim()).isEqualTo("TRAN ID COUNTER");
        assertThat(result.getCtLastValue()).isEqualTo(1);
        // one writeFile for the fee transaction, one for the (missing) control record
        verify(appService, times(2)).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_saveCounterCtrlMissing_writesNewCtrlRecordInsteadOfRewrite() {
        queueAccount(
                1L,
                "A",
                new BigDecimal("600.00"),
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"));
        when(appService.getEibresp())
                .thenReturn(
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_NORMAL,
                        RESP_ENDFILE,
                        RESP_NORMAL,
                        RESP_NOTFND); // save-counter read ctrl -> not found -> WRITE branch (no 2nd
        // resp consumed by rewrite)

        OufeeFields result = runAndCapture(appService);

        assertThat(result.getCtDesc().trim()).isEqualTo("TRAN ID COUNTER");
        // writeFile: fee transaction (TRANFILE) + new control record (CTRLFILE, not found)
        verify(appService, times(2)).writeFile(anyString(), any(), anyString(), anyInt());
        verify(appService, times(1))
                .rewriteFile(anyString(), any()); // the account balance update only
    }

    // ── Update / write failures ──────────────────────────────────────────────

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_updateAcctReadForUpdateFails_incrementsRejectButStillWritesTransactionAndWarns() {
        // COBOL 4400-UPDATE-ACCT's GO TO 4400-EXIT only exits that paragraph; the caller
        // 4000-PROCESS-ACCT unconditionally PERFORMs 4500-WRITE-TRAN afterwards, so the
        // fee transaction is written even though the balance update failed. This is a
        // faithful conversion (no CONVERT-GAP) — verified explicitly here.
        // First readFileForUpdate call (ctrl load) must populate CT-LAST-VALUE; the second
        // (account read-for-update, which the RESP_OTHER stub below makes "fail") must not.
        java.util.concurrent.atomic.AtomicInteger callNo =
                new java.util.concurrent.atomic.AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int n = callNo.incrementAndGet();
                            OufeeFields f = (OufeeFields) inv.getArgument(1);
                            if (n == 1) {
                                f.setCtLastValue(100); // ctrl load succeeds
                            }
                            // n == 2 -> acct read-for-update "fails" (resp handled separately
                            // below); no field mutation
                            fixBrokenDisplayDefaults(f);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());

        queueAccount(
                1L,
                "A",
                new BigDecimal("600.00"),
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"));
        when(appService.getEibresp())
                .thenReturn(
                        RESP_NORMAL, // load counter
                        RESP_NORMAL, // startbr
                        RESP_NORMAL, // readnext acct1
                        RESP_OTHER, // update-acct read-for-update FAILS -> reject, no rewrite
                        RESP_NORMAL, // write-tran (still executed)
                        RESP_ENDFILE, // readnext -> end
                        RESP_NORMAL, // endbrowse
                        RESP_NORMAL, // save-counter read ctrl
                        RESP_NORMAL); // save-counter rewrite ctrl

        OufeeFields result = runAndCapture(appService);

        assertThat(result.getKoRejectCnt()).isEqualTo(1);
        assertThat(result.getKoUpdateCnt()).isEqualTo(0);
        assertThat(result.getKoPostedCnt()).isEqualTo(0);
        assertThat(result.getKoTranCnt()).isEqualTo(1);
        assertThat(result.getKoStatus()).isEqualTo("W");
        // one rewriteFile call: the 5500-SAVE-COUNTER ctrl save (the 4400 account rewrite
        // never happens because the read-for-update failed first).
        verify(appService, times(1)).rewriteFile(anyString(), any());
        verify(appService, times(1)).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_writeFeeTransactionFails_doesNotIncrementTranCount() {
        queueAccount(
                1L,
                "A",
                new BigDecimal("600.00"),
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"));
        when(appService.getEibresp())
                .thenReturn(
                        RESP_NORMAL, // load counter
                        RESP_NORMAL, // startbr
                        RESP_NORMAL, // readnext acct1
                        RESP_NORMAL, // update-acct read-for-update
                        RESP_NORMAL, // update-acct rewrite
                        RESP_OTHER, // write-tran FAILS
                        RESP_ENDFILE); // readnext -> end
        // koTranCnt stays 0 -> 5500-SAVE-COUNTER short-circuits, endbrowse still runs.

        OufeeFields result = runAndCapture(appService);

        assertThat(result.getKoTranCnt()).isEqualTo(0);
        assertThat(result.getKoUpdateCnt()).isEqualTo(1);
        verify(appService, times(1)).endBrowse(anyString());
    }

    // ── mainLine COMMAREA passthrough (public entry point wrapper) ───────────

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_commareaAsByteArray_writesInAndPublishesBackOut() {
        when(appService.getEibcalen()).thenReturn(10);
        byte[] incoming = new byte[10];
        when(appService.getCommarea()).thenReturn(incoming);
        when(appService.getEibresp())
                .thenReturn(RESP_NOTFND, RESP_NOTFND); // load counter, startbr -> quick exit

        service.mainLine(appService);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(1)).setCommarea(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(byte[].class);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_commareaAsObjectArrayWithBytePayload_restoresIntoSameSlot() {
        when(appService.getEibcalen()).thenReturn(10);
        Object[] params = new Object[] {new byte[10]};
        when(appService.getCommarea()).thenReturn(params);
        when(appService.getEibresp()).thenReturn(RESP_NOTFND, RESP_NOTFND);

        service.mainLine(appService);

        assertThat(params[0]).isInstanceOf(byte[].class);
        verify(appService, never()).setCommarea(any());
    }

    // ── Trivial delegation methods ────────────────────────────────────────────

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void programMetadata_delegatesToBmsMetadataAndReportsProgramName() {
        assertThat(service.getProgramName()).isEqualTo("OUFEE");
        assertThat(service.getButtonDefs()).isEmpty();
        service.registerFsetFields(null);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private OufeeFields runAndCapture(AppService svc) {
        service.mainLine(svc);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        // readFileForUpdate is always called at least once (1500-LOAD-COUNTER); every call
        // targets the same shared OufeeFields buffer for the whole task, so any captured
        // value reflects the final field state.
        verify(appService, atLeastOnce())
                .readFileForUpdate(anyString(), captor.capture(), anyString(), anyInt());
        return (OufeeFields) captor.getValue();
    }
}

package com.generated.orion.ouflag.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.generated.orion.ouflag.accessor.OuflagFields;
import com.generated.orion.ouflag.metadata.OuflagBmsMetadata;
import com.generated.orion.ouflag.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Ground truth: OUFLAG.cbl (ORION-CCMS). Every input/expected value below is derived from the COBOL
 * PROCEDURE DIVISION, not from OuflagService's current behavior.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class OuflagServiceTest {

    private static final int RESP_NORMAL = 0;
    private static final int RESP_NOTFND = 13;
    private static final int RESP_ENDFILE = 20;
    private static final int RESP_OTHER = 17;

    private static final String STATUS_ACTIVE = "Y";
    private static final String STATUS_INACTIVE = "N";

    @Mock private AppService appService;

    private final OuflagService service = new OuflagService();
    private final AtomicInteger currentResp = new AtomicInteger(RESP_NORMAL);

    private record ReadStep(int resp, Consumer<OuflagFields> mutator) {}

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(appService.getEibcalen()).thenReturn(1);
        org.mockito.Mockito.lenient()
                .when(appService.getEibresp())
                .thenAnswer(inv -> currentResp.get());
    }

    private static OuflagFields newFields() {
        OuflagFields f = new OuflagFields(new WorkingStorage());
        f.aliasGroup("KFL-PARM", "CA-WORK-AREA");
        return f;
    }

    private byte[] buildInputCommarea(Consumer<OuflagFields> setup) {
        OuflagFields f = newFields();
        setup.accept(f);
        return f.sliceBytes("ORION-COMMAREA");
    }

    private OuflagFields runMainLine(Consumer<OuflagFields> inputSetup) {
        byte[] input = buildInputCommarea(inputSetup);
        when(appService.getCommarea()).thenReturn(input);
        service.mainLine(appService);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(1)).setCommarea(captor.capture());
        OuflagFields out = newFields();
        out.writeBytes("ORION-COMMAREA", (byte[]) captor.getValue());
        return out;
    }

    private void stubStartBrowse(int resp) {
        doAnswer(
                        inv -> {
                            currentResp.set(resp);
                            return null;
                        })
                .when(appService)
                .startBrowse(any(), any(), anyInt());
    }

    private void stubReadNext(List<ReadStep> steps) {
        Queue<ReadStep> queue = new ArrayDeque<>(steps);
        doAnswer(
                        inv -> {
                            ReadStep step = queue.poll();
                            OuflagFields into = (OuflagFields) inv.getArgument(1);
                            step.mutator().accept(into);
                            currentResp.set(step.resp());
                            return null;
                        })
                .when(appService)
                .readNext(any(), any());
    }

    private void stubReadForUpdate(List<ReadStep> steps) {
        Queue<ReadStep> queue = new ArrayDeque<>(steps);
        doAnswer(
                        inv -> {
                            ReadStep step = queue.poll();
                            OuflagFields into = (OuflagFields) inv.getArgument(1);
                            step.mutator().accept(into);
                            currentResp.set(step.resp());
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(any(), any(), any(), anyInt());
    }

    private void stubReadForUpdateAlways(int resp, String activeStatus) {
        doAnswer(
                        inv -> {
                            OuflagFields into = (OuflagFields) inv.getArgument(1);
                            into.setAcActiveStatus(activeStatus);
                            currentResp.set(resp);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(any(), any(), any(), anyInt());
    }

    private void stubRewrite(int resp) {
        doAnswer(
                        inv -> {
                            currentResp.set(resp);
                            return null;
                        })
                .when(appService)
                .rewriteFile(any(), any());
    }

    private static Consumer<OuflagFields> account(
            long id,
            String status,
            String expiryDate,
            String creditLimit,
            String currBal,
            String cycCredit) {
        return f -> {
            f.setAcId(id);
            f.setAcActiveStatus(status);
            f.setAcExpiryDate(expiryDate);
            f.setAcCreditLimit(new BigDecimal(creditLimit));
            f.setAcCurrBal(new BigDecimal(currBal));
            f.setAcCycCredit(new BigDecimal(cycCredit));
        };
    }

    // ---------------------------------------------------------------
    // 1000-INIT — mode validation / cutoff requirement (COBOL 105-136)
    // ---------------------------------------------------------------

    @Test
    void mainLine_modeInvalid_returnsStatus99WithMessage() {
        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("XXXX");
                            f.setKflMax(0);
                        });

        assertEquals("99", out.getKflStatus());
        assertEquals("INVALID MODE - BOTH / DELQ / EXPY", out.getKflMsg().trim());
        verify(appService, never()).startBrowse(any(), any(), anyInt());
    }

    @Test
    void mainLine_modeExpyCutoffSpaces_returnsStatus99() {
        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("EXPY");
                            f.setKflMax(0);
                        });

        assertEquals("99", out.getKflStatus());
        assertEquals("EXPIRY CUTOFF DATE REQUIRED", out.getKflMsg().trim());
        verify(appService, never()).startBrowse(any(), any(), anyInt());
    }

    @Test
    void mainLine_modeBothCutoffLowValues_returnsStatus99() {
        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("BOTH");
                            f.fillLowValues("KFL-CUTOFF");
                            f.setKflMax(0);
                        });

        assertEquals("99", out.getKflStatus());
        assertEquals("EXPIRY CUTOFF DATE REQUIRED", out.getKflMsg().trim());
    }

    @Test
    void mainLine_modeDelqOnly_doesNotRequireCutoff() {
        stubStartBrowse(RESP_NOTFND);

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("DELQ");
                            f.setKflMax(0);
                        });

        assertEquals("10", out.getKflStatus());
        assertEquals("NO ACCOUNTS PROCESSED", out.getKflMsg().trim());
    }

    // ---------------------------------------------------------------
    // 2000-POSITION — STARTBR outcomes (COBOL 140-158)
    // ---------------------------------------------------------------

    @Test
    void mainLine_startBrowseNotFound_skipsLoopReturnsStatus10() {
        stubStartBrowse(RESP_NOTFND);

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("BOTH");
                            f.setKflCutoff("2026-09-24");
                            f.setKflMax(0);
                        });

        assertEquals("10", out.getKflStatus());
        assertEquals("NO ACCOUNTS PROCESSED", out.getKflMsg().trim());
        verify(appService, never()).readNext(any(), any());
        verify(appService, never()).endBrowse(any());
    }

    @Test
    void mainLine_startBrowseEndfile_skipsLoopReturnsStatus10() {
        stubStartBrowse(RESP_ENDFILE);

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("BOTH");
                            f.setKflCutoff("2026-09-24");
                            f.setKflMax(0);
                        });

        assertEquals("10", out.getKflStatus());
        verify(appService, never()).readNext(any(), any());
    }

    @Test
    void mainLine_startBrowseOtherError_returnsStatus99() {
        stubStartBrowse(RESP_OTHER);

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("BOTH");
                            f.setKflCutoff("2026-09-24");
                            f.setKflMax(0);
                        });

        assertEquals("99", out.getKflStatus());
        assertEquals("ACCTFILE STARTBR FAILED", out.getKflMsg().trim());
        verify(appService, never()).readNext(any(), any());
    }

    @Test
    void mainLine_startAcctProvided_startsBrowseAtGivenKey() {
        stubStartBrowse(RESP_NOTFND);
        ArgumentCaptor<String> ridfldCaptor = ArgumentCaptor.forClass(String.class);

        runMainLine(
                f -> {
                    f.setKflMode("DELQ");
                    f.setKflMax(0);
                    f.setKflStartAcct(12345L);
                });

        verify(appService).startBrowse(any(), ridfldCaptor.capture(), anyInt());
        assertEquals(12345L, Long.parseLong(ridfldCaptor.getValue().trim()));
    }

    // ---------------------------------------------------------------
    // 3100-READ-ACCT — READNEXT failure (COBOL 173-189)
    // ---------------------------------------------------------------

    @Test
    void mainLine_readNextError_setsEofIncrementsErrorsAndOverwritesMsg() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(List.of(new ReadStep(RESP_OTHER, f -> {})));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("BOTH");
                            f.setKflCutoff("2026-09-24");
                            f.setKflMax(0);
                        });

        // 3100-READ-ACCT sets KFL-MSG to the READNEXT failure, but 6000-SET-STATUS
        // unconditionally overwrites it when KFL-READ is still zero (COBOL 355-361).
        assertEquals("10", out.getKflStatus());
        assertEquals("NO ACCOUNTS PROCESSED", out.getKflMsg().trim());
        assertEquals(0, out.getKflRead());
        assertEquals(1, out.getKflErrors());
        verify(appService, times(1)).endBrowse(any());
    }

    // ---------------------------------------------------------------
    // 3200-EVAL-ACCT — inactive accounts skipped (COBOL 194-207)
    // ---------------------------------------------------------------

    @Test
    void mainLine_inactiveAccount_incrementsSkippedAndSkipsDelqAndExpiry() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(
                                        1,
                                        STATUS_INACTIVE,
                                        "2020-01-01",
                                        "1000.00",
                                        "0.00",
                                        "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("BOTH");
                            f.setKflCutoff("2026-09-24");
                            f.setKflMax(0);
                        });

        assertEquals(1, out.getKflRead());
        assertEquals(1, out.getKflSkipped());
        assertEquals(0, out.getKflCurrent());
        assertEquals(0, out.getKflDelq());
        verify(appService, never()).readFileForUpdate(any(), any(), any(), anyInt());
    }

    // ---------------------------------------------------------------
    // 3300-EVAL-DELQ / 3310-FLAG-DELQ (COBOL 211-267)
    // ---------------------------------------------------------------

    @Test
    void mainLine_currBalZeroOrNegative_incrementsCurrentNotDelq() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, "2020-01-01", "1000.00", "0.00", "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("DELQ");
                            f.setKflMax(0);
                        });

        assertEquals(1, out.getKflCurrent());
        assertEquals(0, out.getKflDelq());
    }

    @Test
    void mainLine_cycCreditMeetsMinDue_incrementsCurrent() {
        stubStartBrowse(RESP_NORMAL);
        // curr_bal=1000.00 -> raw min due 20.00, below floor 25.00 -> min due = 25.00.
        // cyc_credit 30.00 >= 25.00 -> current.
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(
                                        1,
                                        STATUS_ACTIVE,
                                        "2020-01-01",
                                        "1000.00",
                                        "1000.00",
                                        "30.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("DELQ");
                            f.setKflMax(0);
                        });

        assertEquals(1, out.getKflCurrent());
        assertEquals(0, out.getKflDelq());
    }

    @Test
    void mainLine_cycCreditBelowMinDue_flagsDelinquentBucketB30() {
        // curr_bal=719.98, credit_limit=800.00 -> min due raw 14.3996 -> below floor -> 25.00.
        // cyc_credit 0.00 < 25.00 -> delinquent. util = 719.98/800*100 = 89.9975 -> bucket B30
        // once truncated (see CONVERT-GAP test below for the rounding divergence detail).
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(
                                        1,
                                        STATUS_ACTIVE,
                                        "2020-01-01",
                                        "500.00",
                                        "500.00",
                                        "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("DELQ");
                            f.setKflMax(0);
                        });

        assertEquals(1, out.getKflDelq());
        assertEquals(1, out.getKflB30());
        assertEquals(0, out.getKflB60());
        assertEquals(0, out.getKflB90());
        assertEquals(0, new BigDecimal("25.00").compareTo(out.getKflShortfall()));
        assertEquals(0, new BigDecimal("500.00").compareTo(out.getKflDelqBal()));
    }

    @Test
    void mainLine_utilizationAtOrAbove100_bucketB90() {
        // curr_bal=1000.00, credit_limit=500.00 -> util = 200.00 >= 100 -> B90.
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(
                                        1,
                                        STATUS_ACTIVE,
                                        "2020-01-01",
                                        "500.00",
                                        "1000.00",
                                        "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("DELQ");
                            f.setKflMax(0);
                        });

        assertEquals(1, out.getKflDelq());
        assertEquals(1, out.getKflB90());
        assertEquals(0, out.getKflB60());
        assertEquals(0, out.getKflB30());
    }

    @Test
    void mainLine_utilizationBetween90And100_bucketB60() {
        // curr_bal=950.00, credit_limit=1000.00 -> util = 95.00 -> [90,100) -> B60.
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(
                                        1,
                                        STATUS_ACTIVE,
                                        "2020-01-01",
                                        "1000.00",
                                        "950.00",
                                        "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("DELQ");
                            f.setKflMax(0);
                        });

        assertEquals(1, out.getKflDelq());
        assertEquals(1, out.getKflB60());
        assertEquals(0, out.getKflB90());
        assertEquals(0, out.getKflB30());
    }

    @Test
    void mainLine_creditLimitZero_utilizationForced999_bucketB90() {
        // AC-CREDIT-LIMIT <= 0 -> WS-UTIL forced to 999.99 -> always B90.
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, "2020-01-01", "0.00", "100.00", "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("DELQ");
                            f.setKflMax(0);
                        });

        assertEquals(1, out.getKflDelq());
        assertEquals(1, out.getKflB90());
    }

    // ---------------------------------------------------------------
    // CONVERT-GAP: COMPUTE ... ROUNDED (COBOL 216, 237-238) vs. Java's
    // decimal-field storage, which truncates (RoundingMode.DOWN) instead of
    // rounding half-up when a BigDecimal is persisted through setDecimal.
    // ---------------------------------------------------------------

    @Test
    void mainLine_minDueRoundingTruncation_cobolFlagsDelqButJavaTreatsCurrent() {
        // CONVERT-GAP: AC-CURR-BAL=1250.25 * WS-MIN-DUE-PCT(0.02) = 25.005 raw.
        // COBOL "COMPUTE WS-MIN-DUE ROUNDED" rounds half-up to 2 dp -> 25.01,
        // which is NOT below the 25.00 floor, so min due stays 25.01. With
        // AC-CYC-CREDIT=25.00, COBOL: 25.00 >= 25.01 is false -> FLAG-DELQ (KFL-DELQ=1).
        // The Java service persists WS-MIN-DUE via setDecimal, which truncates
        // (RoundingMode.DOWN) to 2 dp -> 25.00 (not 25.01); 25.00 < floor(25.00) is
        // false so min due also stays 25.00, but then 25.00 >= 25.00 is TRUE ->
        // the account is counted as current (KFL-CURRENT=1), not delinquent.
        // This test asserts the COBOL-correct expectation and is EXPECTED TO FAIL
        // against the current Java implementation, exposing the rounding-mode gap.
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(
                                        1,
                                        STATUS_ACTIVE,
                                        "2020-01-01",
                                        "5000.00",
                                        "1250.25",
                                        "25.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("DELQ");
                            f.setKflMax(0);
                        });

        assertEquals(
                1,
                out.getKflDelq(),
                "CONVERT-GAP: COBOL ROUNDED min-due (25.01) should flag delinquency");
        assertEquals(0, out.getKflCurrent());
    }

    @Test
    void mainLine_utilRoundingTruncation_cobolBucketB60ButJavaBucketsB30() {
        // CONVERT-GAP: AC-CURR-BAL=719.98 / AC-CREDIT-LIMIT=800.00 * 100 = 89.9975 raw.
        // COBOL "COMPUTE WS-UTIL ROUNDED" rounds half-up to 2 dp -> 90.00 -> bucket B60
        // (>= 90.00 and < 100.00). The Java service truncates (RoundingMode.DOWN) when
        // WS-UTIL is persisted -> 89.99 -> bucket B30. Min due floors to 25.00 either
        // way (raw 719.98*0.02=14.3996 < floor) so the delinquency flag itself is not
        // affected — only the utilisation bucket. EXPECTED TO FAIL against current Java.
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(
                                        1,
                                        STATUS_ACTIVE,
                                        "2020-01-01",
                                        "800.00",
                                        "719.98",
                                        "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("DELQ");
                            f.setKflMax(0);
                        });

        assertEquals(1, out.getKflDelq());
        assertEquals(
                1,
                out.getKflB60(),
                "CONVERT-GAP: COBOL ROUNDED utilisation (90.00) should land in bucket B60");
        assertEquals(0, out.getKflB30());
    }

    // ---------------------------------------------------------------
    // 3400-CHECK-EXPIRY — capture table (COBOL 256-267)
    // ---------------------------------------------------------------

    @Test
    void mainLine_expiryDateSpaces_notCaptured() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, "          ", "500.00", "0.00", "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("EXPY");
                            f.setKflCutoff("2026-09-24");
                            f.setKflMax(0);
                        });

        assertEquals(0, out.getKflExpired());
        verify(appService, never()).readFileForUpdate(any(), any(), any(), anyInt());
    }

    @Test
    void mainLine_expiryDateLowValues_notCaptured() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                f -> {
                                    f.setAcId(1);
                                    f.setAcActiveStatus(STATUS_ACTIVE);
                                    f.fillLowValues("AC-EXPIRY-DATE");
                                    f.setAcCreditLimit(new BigDecimal("500.00"));
                                    f.setAcCurrBal(new BigDecimal("0.00"));
                                    f.setAcCycCredit(new BigDecimal("0.00"));
                                }),
                        new ReadStep(RESP_ENDFILE, f -> {})));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("EXPY");
                            f.setKflCutoff("2026-09-24");
                            f.setKflMax(0);
                        });

        assertEquals(0, out.getKflExpired());
        verify(appService, never()).readFileForUpdate(any(), any(), any(), anyInt());
    }

    @Test
    void mainLine_expiryBeforeCutoff_capturedForDeactivation() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(42, STATUS_ACTIVE, "2025-01-01", "500.00", "0.00", "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubReadForUpdate(
                List.of(new ReadStep(RESP_NORMAL, f -> f.setAcActiveStatus(STATUS_ACTIVE))));
        stubRewrite(RESP_NORMAL);

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("EXPY");
                            f.setKflCutoff("2026-09-24");
                            f.setKflMax(0);
                        });

        assertEquals(1, out.getKflExpired());
        verify(appService, times(1)).readFileForUpdate(any(), any(), any(), anyInt());
        verify(appService, times(1)).rewriteFile(any(), any());
    }

    @Test
    void mainLine_expiryOnOrAfterCutoff_notCaptured() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, "2026-09-24", "500.00", "0.00", "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("EXPY");
                            f.setKflCutoff("2026-09-24");
                            f.setKflMax(0);
                        });

        assertEquals(0, out.getKflExpired());
        verify(appService, never()).readFileForUpdate(any(), any(), any(), anyInt());
    }

    @Test
    void mainLine_expiryTableCapReached_setsCappedAndStopsLoopBeforeEof() {
        // WS-MAX-EXP is 300 (COBOL constant). The 301st expired account must never be
        // read: WS-CAPPED is set once WS-EXP-CNT reaches 300 on the 300th capture.
        List<ReadStep> steps = new ArrayList<>();
        for (int i = 1; i <= 301; i++) {
            steps.add(
                    new ReadStep(
                            RESP_NORMAL,
                            account(i, STATUS_ACTIVE, "2025-01-01", "500.00", "0.00", "0.00")));
        }
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(steps);
        stubReadForUpdateAlways(RESP_NORMAL, STATUS_ACTIVE);
        stubRewrite(RESP_NORMAL);

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("EXPY");
                            f.setKflCutoff("2026-09-24");
                            f.setKflMax(0);
                        });

        assertEquals(300, out.getKflRead());
        // 300 reads for the scan loop itself, plus one more from 4000-PEEK-NEXT
        // (COBOL 272-294) which always fires once the loop exits, capped or not.
        verify(appService, times(301)).readNext(any(), any());
    }

    // ---------------------------------------------------------------
    // WS-MAX read cap + 4000-PEEK-NEXT (COBOL 166-168, 272-294)
    // ---------------------------------------------------------------

    @Test
    void mainLine_maxCapReached_peeksNextAndPublishesResumeKey() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, "2020-01-01", "500.00", "0.00", "0.00")),
                        new ReadStep(
                                RESP_NORMAL,
                                account(
                                        99,
                                        STATUS_ACTIVE,
                                        "2020-01-01",
                                        "500.00",
                                        "0.00",
                                        "0.00"))));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("DELQ");
                            f.setKflMax(1);
                        });

        assertEquals("Y", out.getKflMore().trim());
        assertEquals(99L, out.getKflNextAcct());
        assertEquals(1, out.getKflRead());
        verify(appService, times(2)).readNext(any(), any());
    }

    @Test
    void mainLine_peekNextOtherError_setsNoMoreAndIncrementsErrors() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, "2020-01-01", "500.00", "0.00", "0.00")),
                        new ReadStep(RESP_OTHER, f -> {})));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("DELQ");
                            f.setKflMax(1);
                        });

        assertEquals("N", out.getKflMore().trim());
        assertEquals(1, out.getKflErrors());
    }

    @Test
    void mainLine_peekNextEndfile_setsNoMoreWithoutErrors() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, "2020-01-01", "500.00", "0.00", "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("DELQ");
                            f.setKflMax(1);
                        });

        assertEquals("N", out.getKflMore().trim());
        assertEquals(0, out.getKflErrors());
    }

    // ---------------------------------------------------------------
    // 5600-DEACTIVATE-ONE (COBOL 315-347)
    // ---------------------------------------------------------------

    @Test
    void mainLine_deactivateReadForUpdateError_incrementsErrorsSkipsRewrite() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, "2025-01-01", "500.00", "0.00", "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubReadForUpdate(List.of(new ReadStep(RESP_OTHER, f -> {})));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("EXPY");
                            f.setKflCutoff("2026-09-24");
                            f.setKflMax(0);
                        });

        assertEquals(0, out.getKflExpired());
        assertEquals(1, out.getKflErrors());
        verify(appService, never()).rewriteFile(any(), any());
    }

    @Test
    void mainLine_deactivateAlreadyInactive_unlocksWithoutRewriteOrError() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, "2025-01-01", "500.00", "0.00", "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        // Re-read for UPDATE finds the account already deactivated by a concurrent task.
        stubReadForUpdate(
                List.of(new ReadStep(RESP_NORMAL, f -> f.setAcActiveStatus(STATUS_INACTIVE))));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("EXPY");
                            f.setKflCutoff("2026-09-24");
                            f.setKflMax(0);
                        });

        assertEquals(0, out.getKflExpired());
        assertEquals(0, out.getKflErrors());
        verify(appService, never()).rewriteFile(any(), any());
    }

    @Test
    void mainLine_deactivateRewriteError_incrementsErrorsNotExpired() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, "2025-01-01", "500.00", "0.00", "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubReadForUpdate(
                List.of(new ReadStep(RESP_NORMAL, f -> f.setAcActiveStatus(STATUS_ACTIVE))));
        stubRewrite(RESP_OTHER);

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("EXPY");
                            f.setKflCutoff("2026-09-24");
                            f.setKflMax(0);
                        });

        assertEquals(0, out.getKflExpired());
        assertEquals(1, out.getKflErrors());
    }

    // ---------------------------------------------------------------
    // 6000-SET-STATUS + full happy-path (COBOL 351-363)
    // ---------------------------------------------------------------

    @Test
    void mainLine_noAccountsProcessed_returnsStatus10() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(List.of(new ReadStep(RESP_ENDFILE, f -> {})));

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("BOTH");
                            f.setKflCutoff("2026-09-24");
                            f.setKflMax(0);
                        });

        assertEquals("10", out.getKflStatus());
        assertEquals("NO ACCOUNTS PROCESSED", out.getKflMsg().trim());
    }

    @Test
    void mainLine_happyPathBothModes_returnsStatus00WithAggregatedCounts() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(
                                        1,
                                        STATUS_INACTIVE,
                                        "2020-01-01",
                                        "500.00",
                                        "0.00",
                                        "0.00")),
                        new ReadStep(
                                RESP_NORMAL,
                                account(
                                        2,
                                        STATUS_ACTIVE,
                                        "2027-01-01",
                                        "500.00",
                                        "1000.00",
                                        "1000.00")),
                        new ReadStep(
                                RESP_NORMAL,
                                account(
                                        3,
                                        STATUS_ACTIVE,
                                        "2025-01-01",
                                        "500.00",
                                        "1000.00",
                                        "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubReadForUpdate(
                List.of(new ReadStep(RESP_NORMAL, f -> f.setAcActiveStatus(STATUS_ACTIVE))));
        stubRewrite(RESP_NORMAL);

        OuflagFields out =
                runMainLine(
                        f -> {
                            f.setKflMode("BOTH");
                            f.setKflCutoff("2026-09-24");
                            f.setKflMax(0);
                        });

        assertEquals("00", out.getKflStatus());
        assertEquals("ACCOUNT FLAGGING COMPLETE", out.getKflMsg().trim());
        assertEquals(3, out.getKflRead());
        assertEquals(1, out.getKflSkipped());
        assertEquals(1, out.getKflCurrent());
        assertEquals(1, out.getKflDelq());
        assertEquals(1, out.getKflB90());
        assertEquals(1, out.getKflExpired());
        assertEquals(0, out.getKflErrors());
        assertEquals("N", out.getKflMore().trim());
    }

    // ---------------------------------------------------------------
    // AppProgram plumbing
    // ---------------------------------------------------------------

    @Test
    void getProgramName_returnsOuflag() {
        assertEquals("OUFLAG", service.getProgramName());
    }

    @Test
    void getButtonDefs_delegatesToMetadata() {
        assertEquals(OuflagBmsMetadata.getButtonDefs(), service.getButtonDefs());
    }

    @Test
    void registerFsetFields_delegatesToMetadataWithoutThrowing() {
        assertDoesNotThrow(() -> service.registerFsetFields(null));
    }
}

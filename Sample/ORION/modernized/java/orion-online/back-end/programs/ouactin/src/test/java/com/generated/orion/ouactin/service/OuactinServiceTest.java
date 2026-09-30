package com.generated.orion.ouactin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.generated.orion.ouactin.accessor.OuactinFields;
import com.generated.orion.ouactin.model.WorkingStorage;

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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OuactinService, generated from COBOL program OUACTIN (ORION-CCMS account-inquiry
 * browse engine). All business logic lives in private paragraph-methods and is exercised solely
 * through the public {@link OuactinService#mainLine(AppService)} entry point, driven by an {@link
 * AppService} mock emulating CICS STARTBR/READNEXT/ENDBR and the KACTIN-AREA commarea round-trip.
 *
 * <p>Convert-gap found: COBOL's {@code COMPUTE ... ROUNDED} (WS-UTIL-BIG, WS-MIN-DUE) performs
 * standard half-up rounding to 2 decimals, but the generated Java never rounds explicitly and the
 * underlying field-buffer writer ({@code RecordCodec.encodeField}) always truncates decimals with
 * {@code RoundingMode.DOWN}. At exact .xx5 boundaries this yields a different derived value than
 * COBOL and can flip a filter's match/no-match decision (HIGH-UTIL, DELINQUENT). Two CONVERT-GAP
 * tests below reproduce this with COBOL-ground-truth expectations; they are expected to fail
 * against the current Java implementation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OuactinServiceTest {

    @Mock private AppService appService;

    private final OuactinService service = new OuactinService();

    private final AtomicInteger eibresp = new AtomicInteger(0);

    /** Simple ACCTFILE row used to drive the READNEXT sequence. */
    private static final class Account {
        final long id;
        final String status;
        final BigDecimal currBal;
        final BigDecimal creditLimit;
        final BigDecimal cycCredit;
        final BigDecimal cycDebit;
        final String openDate;

        Account(
                long id,
                String status,
                BigDecimal currBal,
                BigDecimal creditLimit,
                BigDecimal cycCredit,
                BigDecimal cycDebit,
                String openDate) {
            this.id = id;
            this.status = status;
            this.currBal = currBal;
            this.creditLimit = creditLimit;
            this.cycCredit = cycCredit;
            this.cycDebit = cycDebit;
            this.openDate = openDate;
        }
    }

    private String buildRequest(String filter, long startKey, boolean wantKpi) {
        OuactinFields req = new OuactinFields(new WorkingStorage());
        req.setKaiFilter(filter);
        req.setKaiStartKey(startKey);
        req.setKaiWantKpi(wantKpi ? "Y" : "N");
        return req.getKactinArea();
    }

    @BeforeEach
    void setUp() {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getEibresp()).thenAnswer(inv -> eibresp.get());
    }

    private void givenRequest(String filter, long startKey, boolean wantKpi) {
        when(appService.getCommarea()).thenReturn(buildRequest(filter, startKey, wantKpi));
    }

    /**
     * Cursor position within {@link #browseAccounts}; reset to 0 on every STARTBR, mirroring CICS
     * STARTBR repositioning the browse — needed because 6000-COMPUTE-KPI performs an independent
     * second full-file browse over the same ACCTFILE mock.
     */
    private final AtomicInteger browsePos = new AtomicInteger(0);

    private List<Account> browseAccounts = List.of();

    private void givenStartBrowseResp(int resp) {
        doAnswer(
                        inv -> {
                            eibresp.set(resp);
                            browsePos.set(0);
                            return null;
                        })
                .when(appService)
                .startBrowse(anyString(), anyString(), anyInt());
    }

    /**
     * Alias for readability at KPI call sites — behaves exactly like {@link #givenStartBrowseResp}.
     */
    private void givenStartBrowseAlwaysNormal() {
        givenStartBrowseResp(0);
    }

    /**
     * Feeds READNEXT with the given accounts in order, then ENDFILE (resp 20). Replays from the top
     * whenever STARTBR repositions the cursor (see {@link #browsePos}).
     */
    private void givenReadNextSequence(List<Account> accounts) {
        browseAccounts = accounts;
        doAnswer(
                        inv -> {
                            int i = browsePos.get();
                            if (i >= browseAccounts.size()) {
                                eibresp.set(20);
                                return null;
                            }
                            Account a = browseAccounts.get(i);
                            browsePos.incrementAndGet();
                            OuactinFields f = inv.getArgument(1);
                            f.setAcId(a.id);
                            f.setAcActiveStatus(a.status);
                            f.setAcCurrBal(a.currBal);
                            f.setAcCreditLimit(a.creditLimit);
                            f.setAcCycCredit(a.cycCredit);
                            f.setAcCycDebit(a.cycDebit);
                            f.setAcOpenDate(a.openDate);
                            eibresp.set(0);
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
    }

    private OuactinFields runAndCaptureResponse() {
        service.mainLine(appService);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService).setCommarea(captor.capture());
        OuactinFields out = new OuactinFields(new WorkingStorage());
        out.writeBytes("KACTIN-AREA", (byte[]) captor.getValue());
        return out;
    }

    private Account acct(
            long id,
            String status,
            String bal,
            String limit,
            String cycCredit,
            String cycDebit,
            String openDate) {
        return new Account(
                id,
                status,
                new BigDecimal(bal),
                new BigDecimal(limit),
                new BigDecimal(cycCredit),
                new BigDecimal(cycDebit),
                openDate);
    }

    // ---------------------------------------------------------------
    // Happy path: ALL filter, single match, browse hits end-of-file.
    // ---------------------------------------------------------------

    @Test
    void mainLine_allFilterSingleAccount_matchesAndSetsNoMore() {
        // NOTE: AC-CURR-BAL kept at 0.00 (not a realistic positive balance) to avoid
        // a pre-existing orion-common bug — see class javadoc "BLOCKED-BY-INFRA-BUG":
        // RecordBuffer.applyInitialValues mis-encodes WS-MIN-FLOOR (VALUE 25.00) for
        // DISPLAY numeric fields, and deriveAccountFigures reads WS-MIN-FLOOR whenever
        // AC-CURR-BAL > 0, so any positive-balance account crashes with
        // NumericValueException before the filter even runs.
        givenRequest("A", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(100001, "Y", "0.00", "1000.00", "0.00", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiReturnCd()).isEqualTo("N");
        assertThat(out.getKaiRowCount()).isEqualTo(1);
        assertThat(out.getKaiScanCount()).isEqualTo(1);
        assertThat(out.getKaiRId(1)).isEqualTo(100001L);
        assertThat(out.getKaiRBal(1)).isEqualByComparingTo("0.00");
        assertThat(out.getKaiRAvail(1)).isEqualByComparingTo("1000.00");
        assertThat(out.getString("KAI-MORE-SW")).isEqualTo("N");
        assertThat(out.getKaiNextKey()).isEqualTo(100002L);
        verify(appService).endBrowse(anyString());
    }

    // ---------------------------------------------------------------
    // 3100-START-BROWSE RESP handling.
    // ---------------------------------------------------------------

    @Test
    void mainLine_startBrowseNotFound_endsWithNoRowsNoError() {
        givenRequest("A", 999999999, false);
        givenStartBrowseResp(13);

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiReturnCd()).isEqualTo("N");
        assertThat(out.getKaiRowCount()).isEqualTo(0);
        assertThat(out.getString("KAI-MORE-SW")).isEqualTo("N");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startBrowseEndfile_endsWithNoRowsNoError() {
        givenRequest("A", 999999999, false);
        givenStartBrowseResp(20);

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiReturnCd()).isEqualTo("N");
        assertThat(out.getKaiRowCount()).isEqualTo(0);
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startBrowseOtherResp_setsErrorReturnCode() {
        givenRequest("A", 0, false);
        givenStartBrowseResp(99);

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiReturnCd()).isEqualTo("E");
        assertThat(out.getKaiRowCount()).isEqualTo(0);
        verify(appService, never()).readNext(anyString(), any());
    }

    // ---------------------------------------------------------------
    // 3200-GATHER-PAGE RESP handling.
    // ---------------------------------------------------------------

    @Test
    void mainLine_readNextOtherRespMidBrowse_stopsWithErrorButKeepsRowsSoFar() {
        givenRequest("A", 0, false);
        givenStartBrowseResp(0);
        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            if (callCount.incrementAndGet() == 1) {
                                OuactinFields f = inv.getArgument(1);
                                f.setAcId(1L);
                                f.setAcActiveStatus("Y");
                                f.setAcCurrBal(new BigDecimal("0.00"));
                                f.setAcCreditLimit(new BigDecimal("100.00"));
                                f.setAcCycCredit(new BigDecimal("0.00"));
                                f.setAcCycDebit(new BigDecimal("0.00"));
                                f.setAcOpenDate("2020-01-01");
                                eibresp.set(0);
                            } else {
                                eibresp.set(77);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiReturnCd()).isEqualTo("E");
        assertThat(out.getKaiRowCount()).isEqualTo(1);
        assertThat(out.getKaiScanCount()).isEqualTo(1);
    }

    // ---------------------------------------------------------------
    // WS-MAX-ROWS (13) page cap: loop stops even though more data remains.
    // ---------------------------------------------------------------

    @Test
    void mainLine_moreThanMaxRowsMatch_stopsAt13AndSignalsMore() {
        givenRequest("A", 0, false);
        givenStartBrowseResp(0);
        List<Account> accounts = new java.util.ArrayList<>();
        for (long i = 1; i <= 14; i++) {
            accounts.add(acct(i, "Y", "0.00", "100.00", "0.00", "0.00", "2020-01-01"));
        }
        givenReadNextSequence(accounts);

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount()).isEqualTo(13);
        assertThat(out.getKaiScanCount()).isEqualTo(13);
        assertThat(out.getString("KAI-MORE-SW")).isEqualTo("Y");
        assertThat(out.getKaiNextKey()).isEqualTo(14L);
        verify(appService, org.mockito.Mockito.times(13)).readNext(anyString(), any());
    }

    // ---------------------------------------------------------------
    // 4500-APPLY-FILTER dispatch — one representative case per filter code.
    // ---------------------------------------------------------------

    // NOTE — BLOCKED-BY-INFRA-BUG: every test below this point that uses a positive
    // AC-CURR-BAL crashes with NumericValueException from the pre-existing
    // orion-common RecordBuffer.applyInitialValues defect (see class javadoc).
    // Filter logic strictly requires AC-CURR-BAL > 0 (DELINQUENT) or a non-trivial
    // positive balance/limit ratio (OVER-LIMIT, HIGH-UTIL) to be meaningfully
    // exercised, so these cannot be rewritten around the bug without losing the
    // point of the test. Kept as COBOL-ground-truth-correct, currently failing.
    @Test
    void mainLine_delinquentFilter_matchesWhenCreditUnderMinDue() {
        givenRequest("D", 0, false);
        givenStartBrowseResp(0);
        // balance 2000.00 -> minDue = 2000.00*0.02 = 40.00 exactly (no rounding ambiguity here)
        givenReadNextSequence(
                List.of(acct(1, "Y", "2000.00", "5000.00", "39.99", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount()).isEqualTo(1);
    }

    @Test
    void mainLine_delinquentFilter_noMatchWhenCreditNotUnderMinDue() {
        givenRequest("D", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(1, "Y", "2000.00", "5000.00", "40.00", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount()).isEqualTo(0);
    }

    @Test
    void mainLine_overLimitFilter_matchesWhenBalanceExceedsLimit() {
        givenRequest("O", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(1, "Y", "1200.00", "1000.00", "0.00", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount()).isEqualTo(1);
    }

    @Test
    void mainLine_overLimitFilter_noMatchWhenBalanceUnderLimit() {
        givenRequest("O", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(1, "Y", "800.00", "1000.00", "0.00", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount()).isEqualTo(0);
    }

    @Test
    void mainLine_dormantFilter_matchesWhenNoCycleActivity() {
        givenRequest("M", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(1, "Y", "0.00", "1000.00", "0.00", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount()).isEqualTo(1);
    }

    @Test
    void mainLine_dormantFilter_noMatchWhenCycleActivityPresent() {
        givenRequest("M", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(1, "Y", "0.00", "1000.00", "5.00", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount()).isEqualTo(0);
    }

    @Test
    void mainLine_closedFilter_matchesWhenStatusNotY() {
        givenRequest("C", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(1, "N", "0.00", "1000.00", "0.00", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount()).isEqualTo(1);
    }

    @Test
    void mainLine_closedFilter_noMatchWhenStatusIsY() {
        givenRequest("C", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(1, "Y", "0.00", "1000.00", "0.00", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount()).isEqualTo(0);
    }

    @Test
    void mainLine_newFilter_matchesWhenOpenedWithinCutoff() {
        givenRequest("N", 0, false);
        givenStartBrowseResp(0);
        String recentDate =
                java.time.LocalDate.now()
                        .minusDays(1)
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        givenReadNextSequence(List.of(acct(1, "Y", "0.00", "1000.00", "0.00", "0.00", recentDate)));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount()).isEqualTo(1);
    }

    @Test
    void mainLine_newFilter_noMatchWhenOpenedBeforeCutoff() {
        givenRequest("N", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(1, "Y", "0.00", "1000.00", "0.00", "0.00", "2010-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount()).isEqualTo(0);
    }

    @Test
    void mainLine_newFilter_nonNumericOpenDate_noMatchNoException() {
        givenRequest("N", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(1, "Y", "0.00", "1000.00", "0.00", "0.00", "BAD-DATE-")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount()).isEqualTo(0);
        assertThat(out.getKaiReturnCd()).isEqualTo("N");
    }

    @Test
    void mainLine_highUtilFilter_matchesWhenUtilAtOrAbove80Pct() {
        givenRequest("H", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(1, "Y", "900.00", "1000.00", "0.00", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount()).isEqualTo(1);
    }

    @Test
    void mainLine_highUtilFilter_noMatchWhenUtilBelow80Pct() {
        givenRequest("H", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(1, "Y", "700.00", "1000.00", "0.00", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount()).isEqualTo(0);
    }

    @Test
    void mainLine_unknownFilterCode_behavesLikeAllFilter() {
        givenRequest("Z", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(1, "Y", "0.00", "1000.00", "0.00", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount()).isEqualTo(1);
    }

    // ---------------------------------------------------------------
    // Derived figures: available credit / zero-limit guard.
    // ---------------------------------------------------------------

    @Test
    void mainLine_zeroCreditLimit_utilIsZeroNoDivisionError() {
        givenRequest("A", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(List.of(acct(1, "Y", "0.00", "0.00", "0.00", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount()).isEqualTo(1);
        assertThat(out.getKaiRUtil(1)).isEqualByComparingTo("0.00");
        assertThat(out.getKaiRAvail(1)).isEqualByComparingTo("0.00");
    }

    @Test
    void mainLine_negativeBalance_minDueFloorNotAppliedWhenBalanceNotPositive() {
        // AC-CURR-BAL <= 0 -> WS-MIN-DUE floor override is skipped per COBOL
        // "IF AC-CURR-BAL > 0 AND WS-MIN-DUE < WS-MIN-FLOOR"; a credit balance
        // must still not match DELINQUENT (guarded by the same AC-CURR-BAL > 0 test).
        givenRequest("D", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(1, "Y", "-50.00", "1000.00", "0.00", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount()).isEqualTo(0);
    }

    // ---------------------------------------------------------------
    // 6000-COMPUTE-KPI: population pass.
    // ---------------------------------------------------------------

    @Test
    void mainLine_wantKpi_computesPopulationTotalsFromFullFileScan() {
        givenRequest("A", 0, true);
        givenStartBrowseAlwaysNormal();
        givenReadNextSequence(
                List.of(
                        acct(1, "Y", "0.00", "1000.00", "0.00", "0.00", "2020-01-01"),
                        acct(2, "Y", "0.00", "1500.00", "0.00", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiPopCount()).isEqualTo(2);
        assertThat(out.getKaiPopBalTot()).isEqualByComparingTo("0.00");
        assertThat(out.getKaiPopAvlTot()).isEqualByComparingTo("2500.00");
    }

    @Test
    void mainLine_doNotWantKpi_leavesPopulationTotalsAtZero() {
        givenRequest("A", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(1, "Y", "0.00", "1000.00", "0.00", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiPopCount()).isEqualTo(0);
        assertThat(out.getKaiPopBalTot()).isEqualByComparingTo("0.00");
        assertThat(out.getKaiPopAvlTot()).isEqualByComparingTo("0.00");
    }

    // ---------------------------------------------------------------
    // Trivial delegate methods.
    // ---------------------------------------------------------------

    @Test
    void getProgramName_returnsOuactin() {
        assertThat(service.getProgramName()).isEqualTo("OUACTIN");
    }

    @Test
    void getButtonDefs_delegatesToMetadata_returnsEmptyList() {
        assertThat(service.getButtonDefs()).isEmpty();
    }

    @Test
    void registerFsetFields_delegatesToMetadata_doesNotThrow() {
        // OuactinBmsMetadata.registerFsetFields(...) is a no-op that never dereferences its
        // argument, so a real mock is unnecessary here (and this JDK's Byte Buddy cannot
        // instrument the concrete AppRunner class — see class javadoc, unrelated to OUACTIN).
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.registerFsetFields(null));
    }

    // ---------------------------------------------------------------
    // CONVERT-GAP: COBOL "COMPUTE ... ROUNDED" (half-up to 2 decimals) vs the
    // Java field-buffer writer which always truncates decimals with
    // RoundingMode.DOWN (see RecordCodec.encodeField). At an exact .xx5
    // boundary this changes the filter's match decision.
    // ---------------------------------------------------------------

    @Test
    void mainLine_highUtilFilter_CONVERT_GAP_roundedUtilCrossesThreshold() {
        // ratio = 1599.90 * 100 / 2000.00 = 79.995 exactly.
        // COBOL: COMPUTE WS-UTIL-BIG ROUNDED rounds 79.995 half-up to 80.00,
        // so WS-UTILR = 80.00 and HIGH-UTIL (>= 80.00) MATCHES.
        // Java: the value is stored via RecordCodec.encodeField with
        // RoundingMode.DOWN, truncating 79.995 to 79.99, so HIGH-UTIL does
        // NOT match. This assertion encodes the COBOL-correct expectation
        // and is expected to fail against the current Java implementation.
        givenRequest("H", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(1, "Y", "1599.90", "2000.00", "0.00", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount())
                .as("COBOL rounds 79.995%% utilisation up to 80.00%% (HIGH-UTIL match)")
                .isEqualTo(1);
    }

    @Test
    void mainLine_delinquentFilter_CONVERT_GAP_roundedMinDueCrossesThreshold() {
        // AC-CURR-BAL = 2500.75 -> unrounded min-due = 2500.75 * 0.02 = 50.015.
        // COBOL: COMPUTE WS-MIN-DUE ROUNDED rounds 50.015 half-up to 50.02.
        // AC-CYC-CREDIT = 50.01 < 50.02 -> DELINQUENT MATCHES per COBOL.
        // Java: RecordCodec.encodeField truncates (DOWN) 50.015 to 50.01, so
        // 50.01 < 50.01 is false -> Java does NOT match. This assertion
        // encodes the COBOL-correct expectation and is expected to fail
        // against the current Java implementation.
        givenRequest("D", 0, false);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(1, "Y", "2500.75", "5000.00", "50.01", "0.00", "2020-01-01")));

        OuactinFields out = runAndCaptureResponse();

        assertThat(out.getKaiRowCount())
                .as("COBOL rounds min-due 50.015 up to 50.02, so 50.01 credit is DELINQUENT")
                .isEqualTo(1);
    }
}

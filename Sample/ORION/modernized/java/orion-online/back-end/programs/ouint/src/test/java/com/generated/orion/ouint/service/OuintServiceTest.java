package com.generated.orion.ouint.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.generated.orion.ouint.accessor.OuintFields;
import com.generated.orion.ouint.model.WorkingStorage;

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
 * Unit tests for OuintService, generated from COBOL program OUINT (ORION-CCMS revolving-balance
 * interest assessment SUB). All business logic lives in private paragraph-methods and is exercised
 * solely through the public {@link OuintService#mainLine(AppService)} entry point, driven by an
 * {@link AppService} mock emulating CICS STARTBR/READNEXT/READ/READ-UPDATE/REWRITE/ENDBR and the
 * ORION-COMMAREA round-trip (KOPS-AREA aliased onto CA-WORK-AREA).
 *
 * <p>Convert-gap found: COBOL's {@code COMPUTE WS-INT-AMT ROUNDED = (AC-CURR-BAL * WS-USED-RATE) /
 * 1200} rounds half-up directly to WS-INT-AMT's 2 decimal places (PIC S9(11)V99). The generated
 * Java ({@code computeInterest}) instead divides to scale 12 with HALF_UP, and the *storage* layer
 * that later writes WS-INT-AMT into the record buffer ({@code RecordCodec.encodeField}) always
 * truncates to 2 decimals with {@code RoundingMode.DOWN}. At an exact .xx5 boundary this drops a
 * cent that COBOL would have kept, understating KO-AMT-1 / the posted balance. The CONVERT-GAP test
 * below reproduces this with a COBOL-ground-truth expectation; it is expected to fail against the
 * current Java implementation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OuintServiceTest {

    @Mock private AppService appService;

    private final OuintService service = new OuintService();

    private final AtomicInteger eibresp = new AtomicInteger(0);

    /** Simple ACCTFILE row used to drive the READNEXT sequence. */
    private static final class Account {
        final long id;
        final String status;
        final BigDecimal currBal;
        final String groupId;

        Account(long id, String status, BigDecimal currBal, String groupId) {
            this.id = id;
            this.status = status;
            this.currBal = currBal;
            this.groupId = groupId;
        }
    }

    private String buildRequest(long parmAcct) {
        OuintFields req = new OuintFields(new WorkingStorage());
        // KO-PARM-ACCT lives in KOPS-AREA, overlaid onto CA-WORK-AREA (inside
        // ORION-COMMAREA) — alias before writing so the value lands in the bytes that
        // getOrionCommarea() actually reads back (mirrors the caller-side overlay).
        req.aliasGroup("KOPS-AREA", "CA-WORK-AREA");
        req.setKoParmAcct(parmAcct);
        return req.getOrionCommarea();
    }

    @BeforeEach
    void setUp() {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getEibresp()).thenAnswer(inv -> eibresp.get());
    }

    private void givenRequest(long parmAcct) {
        when(appService.getCommarea()).thenReturn(buildRequest(parmAcct));
    }

    private void givenStartBrowseResp(int resp) {
        doAnswer(
                        inv -> {
                            eibresp.set(resp);
                            return null;
                        })
                .when(appService)
                .startBrowse(anyString(), anyString(), anyInt());
    }

    /** Feeds READNEXT with the given accounts in order, then ENDFILE (resp 20). */
    private void givenReadNextSequence(List<Account> accounts) {
        AtomicInteger pos = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = pos.get();
                            if (i >= accounts.size()) {
                                eibresp.set(20);
                                return null;
                            }
                            Account a = accounts.get(i);
                            pos.incrementAndGet();
                            OuintFields f = inv.getArgument(1);
                            f.setAcId(a.id);
                            f.setAcActiveStatus(a.status);
                            f.setAcCurrBal(a.currBal);
                            f.setAcGroupId(a.groupId);
                            eibresp.set(0);
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
    }

    /** DGRPFILE READ (interest rate lookup) always returns the given rate with resp 0. */
    private void givenRateFound(BigDecimal rate) {
        doAnswer(
                        inv -> {
                            OuintFields f = inv.getArgument(1);
                            f.setDgIntRate(rate);
                            eibresp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(anyString(), any(), anyString(), anyInt());
    }

    private void givenRateNotFound() {
        doAnswer(
                        inv -> {
                            eibresp.set(13);
                            return null;
                        })
                .when(appService)
                .readFile(anyString(), any(), anyString(), anyInt());
    }

    /** READ FOR UPDATE returns the current AC-CURR-BAL (carried over from READNEXT) with resp 0. */
    private void givenReadForUpdateNormal(BigDecimal currBal) {
        doAnswer(
                        inv -> {
                            OuintFields f = inv.getArgument(1);
                            f.setAcCurrBal(currBal);
                            f.setAcCycDebit(BigDecimal.ZERO);
                            eibresp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    private void givenReadForUpdateFails(int resp) {
        doAnswer(
                        inv -> {
                            eibresp.set(resp);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    private void givenRewriteResp(int resp) {
        doAnswer(
                        inv -> {
                            eibresp.set(resp);
                            return null;
                        })
                .when(appService)
                .rewriteFile(anyString(), any());
    }

    private OuintFields runAndCaptureResponse() {
        service.mainLine(appService);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService).setCommarea(captor.capture());
        OuintFields out = new OuintFields(new WorkingStorage());
        // KO-* fields live in KOPS-AREA, which OUINT overlays onto CA-WORK-AREA (inside
        // ORION-COMMAREA) via "SET ADDRESS OF KOPS-AREA TO ADDRESS OF CA-WORK-AREA" — the
        // alias must be re-established on this fresh accessor before writing the captured
        // bytes, exactly as runMainProgram does at the start of every request.
        out.aliasGroup("KOPS-AREA", "CA-WORK-AREA");
        out.writeBytes("ORION-COMMAREA", (byte[]) captor.getValue());
        return out;
    }

    private Account acct(long id, String status, String bal, String groupId) {
        return new Account(id, status, new BigDecimal(bal), groupId);
    }

    // ---------------------------------------------------------------
    // Happy path: single active account, positive balance, rate found, posts interest.
    // ---------------------------------------------------------------

    @Test
    void mainLine_noFilterActiveAccountRateFound_computesAndPostsInterest() {
        givenRequest(0);
        givenStartBrowseResp(0);
        givenReadNextSequence(List.of(acct(100001, "Y", "1000.00", "GG")));
        givenRateFound(new BigDecimal("12.00"));
        givenReadForUpdateNormal(new BigDecimal("1000.00"));
        givenRewriteResp(0);

        OuintFields out = runAndCaptureResponse();

        // interest = 1000.00 * 12.00 / 1200 = 10.00 exactly (no rounding ambiguity)
        assertThat(out.getKoStatus()).isEqualTo("O");
        assertThat(out.getKoStatusMsg().trim()).isEqualTo("INTEREST ASSESSMENT COMPLETE.");
        assertThat(out.getKoReadCnt()).isEqualTo(1);
        assertThat(out.getKoSelectCnt()).isEqualTo(1);
        assertThat(out.getKoPostedCnt()).isEqualTo(1);
        assertThat(out.getKoUpdateCnt()).isEqualTo(1);
        assertThat(out.getKoSkipCnt()).isEqualTo(0);
        assertThat(out.getKoRejectCnt()).isEqualTo(0);
        assertThat(out.getKoC1()).isEqualTo(0);
        assertThat(out.getKoAmt1()).isEqualByComparingTo("10.00");
        assertThat(out.getKoAmt2()).isEqualByComparingTo("1000.00");
        assertThat(out.getKoAmt3()).isEqualByComparingTo("1010.00");
        verify(appService).endBrowse(anyString());
    }

    @Test
    void mainLine_filterActive_stopsAtFirstNonMatchingAccount() {
        givenRequest(100001);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(acct(100001, "Y", "500.00", "GG"), acct(100002, "Y", "500.00", "GG")));
        givenRateFound(new BigDecimal("12.00"));
        givenReadForUpdateNormal(new BigDecimal("500.00"));
        givenRewriteResp(0);

        OuintFields out = runAndCaptureResponse();

        // account 2 has a different id than the filter -> browse stops before processing it
        assertThat(out.getKoReadCnt()).isEqualTo(2);
        assertThat(out.getKoSelectCnt()).isEqualTo(1);
        assertThat(out.getKoPostedCnt()).isEqualTo(1);
        assertThat(out.getKoStatus()).isEqualTo("O");
        verify(appService, times(2)).readNext(anyString(), any());
    }

    @Test
    void mainLine_rateNotFoundForGroup_usesDefaultRateAndIncrementsC1() {
        givenRequest(0);
        givenStartBrowseResp(0);
        givenReadNextSequence(List.of(acct(1, "Y", "1000.00", "ZZ")));
        givenRateNotFound();
        givenReadForUpdateNormal(new BigDecimal("1000.00"));
        givenRewriteResp(0);

        OuintFields out = runAndCaptureResponse();

        // default rate 18.99 -> interest = 1000.00 * 18.99 / 1200 = 15.825 -> COBOL rounds to 15.83
        assertThat(out.getKoC1()).isEqualTo(1);
        assertThat(out.getKoPostedCnt()).isEqualTo(1);
        assertThat(out.getKoAmt1())
                .isEqualByComparingTo("15.83")
                .as(
                        "CONVERT-GAP: COBOL ROUNDED half-up rounds 15.825 to 15.83, but Java's"
                            + " field-buffer writer truncates with RoundingMode.DOWN to 15.82 (see"
                            + " class javadoc) - this assertion encodes the COBOL-correct"
                            + " expectation and is expected to fail against the current Java"
                            + " implementation");
    }

    // ---------------------------------------------------------------
    // 4000-PROCESS-ACCT screening: inactive / non-positive-balance accounts are skipped.
    // ---------------------------------------------------------------

    @Test
    void mainLine_inactiveAccount_skippedWithoutRateLookup() {
        givenRequest(0);
        givenStartBrowseResp(0);
        givenReadNextSequence(List.of(acct(1, "N", "1000.00", "GG")));

        OuintFields out = runAndCaptureResponse();

        assertThat(out.getKoSkipCnt()).isEqualTo(1);
        assertThat(out.getKoSelectCnt()).isEqualTo(0);
        assertThat(out.getKoAmt2()).isEqualByComparingTo("1000.00");
        assertThat(out.getKoAmt3()).isEqualByComparingTo("1000.00");
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_nonPositiveBalance_skippedWithoutRateLookup() {
        givenRequest(0);
        givenStartBrowseResp(0);
        givenReadNextSequence(List.of(acct(1, "Y", "0.00", "GG")));

        OuintFields out = runAndCaptureResponse();

        assertThat(out.getKoSkipCnt()).isEqualTo(1);
        assertThat(out.getKoSelectCnt()).isEqualTo(0);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_negativeBalance_skippedWithoutRateLookup() {
        givenRequest(0);
        givenStartBrowseResp(0);
        givenReadNextSequence(List.of(acct(1, "Y", "-50.00", "GG")));

        OuintFields out = runAndCaptureResponse();

        assertThat(out.getKoSkipCnt()).isEqualTo(1);
        assertThat(out.getKoAmt3()).isEqualByComparingTo("-50.00");
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    // ---------------------------------------------------------------
    // 4300-UPDATE-ACCT: computed interest <= 0 recorded as a skip, no READ/REWRITE.
    // ---------------------------------------------------------------

    @Test
    void mainLine_zeroRateFromDisclosureGroup_interestZero_recordedAsSkip() {
        givenRequest(0);
        givenStartBrowseResp(0);
        givenReadNextSequence(List.of(acct(1, "Y", "1000.00", "GG")));
        givenRateFound(BigDecimal.ZERO);

        OuintFields out = runAndCaptureResponse();

        assertThat(out.getKoSelectCnt()).isEqualTo(1);
        assertThat(out.getKoSkipCnt()).isEqualTo(1);
        assertThat(out.getKoPostedCnt()).isEqualTo(0);
        assertThat(out.getKoAmt3()).isEqualByComparingTo("1000.00");
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    // ---------------------------------------------------------------
    // 4300-UPDATE-ACCT: READ UPDATE / REWRITE failure handling.
    // ---------------------------------------------------------------

    @Test
    void mainLine_readForUpdateFails_countsRejectWithoutInterestApplied() {
        givenRequest(0);
        givenStartBrowseResp(0);
        givenReadNextSequence(List.of(acct(1, "Y", "1000.00", "GG")));
        givenRateFound(new BigDecimal("12.00"));
        givenReadForUpdateFails(99);

        OuintFields out = runAndCaptureResponse();

        assertThat(out.getKoRejectCnt()).isEqualTo(1);
        assertThat(out.getKoPostedCnt()).isEqualTo(0);
        assertThat(out.getKoUpdateCnt()).isEqualTo(0);
        assertThat(out.getKoAmt1()).isEqualByComparingTo("0.00");
        // reject path adds the ORIGINAL (pre-interest) balance snapshotted on READNEXT
        assertThat(out.getKoAmt3()).isEqualByComparingTo("1000.00");
        assertThat(out.getKoStatus()).isEqualTo("W");
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void mainLine_rewriteFails_countsRejectButAmt3IncludesInterest() {
        givenRequest(0);
        givenStartBrowseResp(0);
        givenReadNextSequence(List.of(acct(1, "Y", "1000.00", "GG")));
        givenRateFound(new BigDecimal("12.00"));
        givenReadForUpdateNormal(new BigDecimal("1000.00"));
        givenRewriteResp(99);

        OuintFields out = runAndCaptureResponse();

        // COBOL adds WS-INT-AMT to AC-CURR-BAL/AC-CYC-DEBIT unconditionally BEFORE the
        // REWRITE call, so KO-AMT-3 still reflects the post-interest balance even though
        // the REWRITE itself failed and the account is counted as rejected, not posted.
        assertThat(out.getKoRejectCnt()).isEqualTo(1);
        assertThat(out.getKoPostedCnt()).isEqualTo(0);
        assertThat(out.getKoAmt1()).isEqualByComparingTo("0.00");
        assertThat(out.getKoAmt3()).isEqualByComparingTo("1010.00");
        assertThat(out.getKoStatus()).isEqualTo("W");
    }

    // ---------------------------------------------------------------
    // 3100-START-BROWSE RESP handling.
    // ---------------------------------------------------------------

    @Test
    void mainLine_startBrowseNotFound_noErrorAndFinaliseOverwritesMessage() {
        // 3100-START-BROWSE sets KO-STATUS-MSG to "NO ACCOUNTS TO ASSESS." on DFHRESP(NOTFND)
        // without raising KO-ERROR, but 9000-FINALISE unconditionally overwrites
        // KO-STATUS-MSG with "INTEREST ASSESSMENT COMPLETE." whenever KO-ERROR is not set
        // (COBOL: "IF KO-ERROR CONTINUE ELSE ... MOVE 'INTEREST ASSESSMENT COMPLETE.' ...")
        // — so the final message is the completion message, not the not-found one.
        givenRequest(0);
        givenStartBrowseResp(13);

        OuintFields out = runAndCaptureResponse();

        assertThat(out.getKoStatus()).isEqualTo("O");
        assertThat(out.getKoStatusMsg().trim()).isEqualTo("INTEREST ASSESSMENT COMPLETE.");
        assertThat(out.getKoReadCnt()).isEqualTo(0);
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startBrowseOtherResp_setsErrorStatus() {
        givenRequest(0);
        givenStartBrowseResp(99);

        OuintFields out = runAndCaptureResponse();

        assertThat(out.getKoStatus()).isEqualTo("E");
        assertThat(out.getKoStatusMsg().trim()).isEqualTo("STARTBR ACCTFILE FAILED.");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    // ---------------------------------------------------------------
    // 3200-READ-NEXT-ACCT RESP handling.
    // ---------------------------------------------------------------

    @Test
    void mainLine_emptyAccountFile_immediateEndfile_noAccountsProcessed() {
        givenRequest(0);
        givenStartBrowseResp(0);
        givenReadNextSequence(List.of());

        OuintFields out = runAndCaptureResponse();

        assertThat(out.getKoStatus()).isEqualTo("O");
        assertThat(out.getKoStatusMsg().trim()).isEqualTo("INTEREST ASSESSMENT COMPLETE.");
        assertThat(out.getKoReadCnt()).isEqualTo(0);
        assertThat(out.getKoSelectCnt()).isEqualTo(0);
        verify(appService).endBrowse(anyString());
    }

    @Test
    void mainLine_readNextOtherRespMidBrowse_stopsWithErrorButEndsBrowse() {
        givenRequest(0);
        givenStartBrowseResp(0);
        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            if (callCount.incrementAndGet() == 1) {
                                OuintFields f = inv.getArgument(1);
                                f.setAcId(1L);
                                f.setAcActiveStatus("Y");
                                f.setAcCurrBal(new BigDecimal("500.00"));
                                f.setAcGroupId("GG");
                                eibresp.set(0);
                            } else {
                                eibresp.set(77);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
        givenRateFound(new BigDecimal("12.00"));
        givenReadForUpdateNormal(new BigDecimal("500.00"));
        givenRewriteResp(0);

        OuintFields out = runAndCaptureResponse();

        assertThat(out.getKoStatus()).isEqualTo("E");
        assertThat(out.getKoStatusMsg().trim()).isEqualTo("READNEXT ACCTFILE FAILED.");
        assertThat(out.getKoReadCnt()).isEqualTo(1);
        assertThat(out.getKoSelectCnt()).isEqualTo(1);
        // 3400-END-BROWSE runs unconditionally once BR-STARTED, even when KO-ERROR is set.
        verify(appService).endBrowse(anyString());
    }

    // ---------------------------------------------------------------
    // Multiple accounts in one browse: mixed skip / post outcomes accumulate correctly.
    // ---------------------------------------------------------------

    @Test
    void mainLine_multipleAccountsMixedOutcomes_accumulatesCountersAndAmounts() {
        givenRequest(0);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        acct(1, "Y", "1000.00", "GG"),
                        acct(2, "N", "300.00", "GG"),
                        acct(3, "Y", "-10.00", "GG")));
        givenRateFound(new BigDecimal("12.00"));
        givenReadForUpdateNormal(new BigDecimal("1000.00"));
        givenRewriteResp(0);

        OuintFields out = runAndCaptureResponse();

        assertThat(out.getKoReadCnt()).isEqualTo(3);
        assertThat(out.getKoSelectCnt()).isEqualTo(1);
        assertThat(out.getKoSkipCnt()).isEqualTo(2);
        assertThat(out.getKoPostedCnt()).isEqualTo(1);
        assertThat(out.getKoAmt2()).isEqualByComparingTo("1290.00");
        assertThat(out.getKoAmt3()).isEqualByComparingTo("1300.00");
    }

    // ---------------------------------------------------------------
    // Trivial delegate methods.
    // ---------------------------------------------------------------

    @Test
    void getProgramName_returnsOuint() {
        assertThat(service.getProgramName()).isEqualTo("OUINT");
    }

    @Test
    void getButtonDefs_delegatesToMetadata_returnsEmptyList() {
        assertThat(service.getButtonDefs()).isEmpty();
    }

    @Test
    void registerFsetFields_delegatesToMetadata_doesNotThrow() {
        // OuintBmsMetadata.registerFsetFields(...) is a no-op that never dereferences its
        // argument, so a real AppRunner mock/instance is unnecessary here.
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.registerFsetFields(null));
    }
}

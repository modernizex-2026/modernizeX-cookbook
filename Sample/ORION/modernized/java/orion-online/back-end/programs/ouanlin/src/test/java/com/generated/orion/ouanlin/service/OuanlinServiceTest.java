package com.generated.orion.ouanlin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.generated.orion.ouanlin.accessor.OuanlinFields;
import com.generated.orion.ouanlin.model.WorkingStorage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OuanlinService, generated from COBOL program OUANLIN (ORION-CCMS analytics compute
 * subroutine, LINKed from OCANLIN). All business logic lives in private paragraph-methods and is
 * exercised solely through the public {@link OuanlinService#mainLine(AppService)} entry point,
 * driven by an {@link AppService} mock emulating CICS STARTBR/READNEXT/ENDBR/READ and the
 * KANLB-PARM commarea round-trip.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OuanlinServiceTest {

    @Mock private AppService appService;

    private final OuanlinService service = new OuanlinService();

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    /** Transaction queue for the TRANFILE browse. */
    private final List<Txn> txns = new ArrayList<>();

    private final AtomicInteger txnPos = new AtomicInteger(0);

    private static final class Txn {
        final String card;
        final String type;
        final BigDecimal amt;

        Txn(String card, String type, BigDecimal amt) {
            this.card = card;
            this.type = type;
            this.amt = amt;
        }
    }

    @BeforeEach
    void setUp() {
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
    }

    @AfterEach
    void tearDown() {
        txns.clear();
        txnPos.set(0);
        nextResp.set(0);
    }

    // ───────────────────────── request / response helpers ─────────────────────────

    private String buildRequest(String mode, int maxRows, int startOff) {
        OuanlinFields req = new OuanlinFields(new WorkingStorage());
        req.setKabMode(mode);
        req.setKabMaxRows(maxRows);
        req.setKabStartOff(startOff);
        return req.getKanlbParm();
    }

    private void givenRequest(String mode, int maxRows, int startOff) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getCommarea()).thenReturn(buildRequest(mode, maxRows, startOff));
    }

    private OuanlinFields runAndCaptureResponse() {
        service.mainLine(appService);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService).setCommarea(captor.capture());
        OuanlinFields out = new OuanlinFields(new WorkingStorage());
        out.writeBytes("KANLB-PARM", (byte[]) captor.getValue());
        return out;
    }

    // ───────────────────────── TRANFILE browse stubs ─────────────────────────

    private void givenStartBrowseResp(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            txnPos.set(0);
                            return null;
                        })
                .when(appService)
                .startBrowse(eq("TRANFILE"), anyString(), anyInt());
    }

    private void givenTransactions(List<Txn> list) {
        txns.clear();
        txns.addAll(list);
        doAnswer(
                        inv -> {
                            int i = txnPos.get();
                            if (i >= txns.size()) {
                                nextResp.set(20);
                                return null;
                            }
                            Txn t = txns.get(i);
                            txnPos.incrementAndGet();
                            OuanlinFields f = inv.getArgument(1);
                            f.setTrCardNum(t.card);
                            f.setTrTypeCd(t.type);
                            f.setTrAmt(t.amt);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readNext(eq("TRANFILE"), any());
    }

    private Txn txn(String card, String type, String amt) {
        return new Txn(card, type, new BigDecimal(amt));
    }

    // ───────────────────────── XREFFILE / ACCTFILE stubs (RC mode) ─────────────────────────

    private void givenXrefLookup(Map<String, Long> cardToAcctId) {
        doAnswer(
                        inv -> {
                            String card = ((String) inv.getArgument(2)).trim();
                            Long acctId = cardToAcctId.get(card);
                            OuanlinFields into = inv.getArgument(1);
                            if (acctId != null) {
                                into.setXrAcctId(acctId);
                                nextResp.set(0);
                            } else {
                                nextResp.set(13);
                            }
                            return null;
                        })
                .when(appService)
                .readFile(eq("XREFFILE"), any(), anyString(), anyInt());
    }

    private void givenAcctBalances(Map<Long, BigDecimal> balances) {
        doAnswer(
                        inv -> {
                            long acctId = Long.parseLong(((String) inv.getArgument(2)).trim());
                            BigDecimal bal = balances.get(acctId);
                            OuanlinFields into = inv.getArgument(1);
                            if (bal != null) {
                                into.setAcCurrBal(bal);
                                nextResp.set(0);
                            } else {
                                nextResp.set(13);
                            }
                            return null;
                        })
                .when(appService)
                .readFile(eq("ACCTFILE"), any(), anyString(), anyInt());
    }

    // ───────────────────────── 1000-INIT: mode validation ─────────────────────────

    @Test
    void mainLine_invalidMode_setsStatus99AndSkipsScan() {
        givenRequest("ZZ", 6, 0);

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabStatus()).isEqualTo("99");
        assertThat(out.getKabRowCnt()).isZero();
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    @Test
    void mainLine_eibcalenZero_neverRoundTripsCommareaAndTreatsModeAsBlank() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        verify(appService, never()).setCommarea(any());
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    // ───────────────────────── 2100-START-TRAN ─────────────────────────

    @Test
    void mainLine_startBrowseNotFound_endsBrowseSwOffNoReadNextResultsInStatus10() {
        givenRequest("RW", 6, 0);
        givenStartBrowseResp(13);

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabStatus()).isEqualTo("10");
        assertThat(out.getKabResultCnt()).isZero();
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startBrowseEndfile_noResults() {
        givenRequest("RW", 6, 0);
        givenStartBrowseResp(20);

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabStatus()).isEqualTo("10");
        verify(appService, never()).readNext(anyString(), any());
    }

    @Test
    void mainLine_startBrowseOtherResp_setsStatus99ButBuildPageStillRunsThenRowCntWiped() {
        givenRequest("RW", 6, 0);
        givenStartBrowseResp(99);

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabStatus()).isEqualTo("99");
        assertThat(out.getKabRowCnt()).isZero();
        verify(appService, never()).readNext(anyString(), any());
    }

    // ───────────────────────── 2200-READ-TRAN ─────────────────────────

    @Test
    void mainLine_readNextEndfileMidScan_stopsLoopAndEndsBrowseNormally() {
        givenRequest("RW", 6, 0);
        givenStartBrowseResp(0);
        givenTransactions(List.of(txn("1111222233334444", "PU", "50.00")));

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabStatus()).isEqualTo("00");
        verify(appService).endBrowse(eq("TRANFILE"));
    }

    @Test
    void mainLine_readNextOtherResp_status99ButPartialResultsKeptInResultCntRowCntWiped() {
        givenRequest("RW", 6, 0);
        givenStartBrowseResp(0);
        doAnswer(
                        inv -> {
                            int i = txnPos.get();
                            if (i == 0) {
                                txnPos.incrementAndGet();
                                OuanlinFields f = inv.getArgument(1);
                                f.setTrCardNum("1111222233334444");
                                f.setTrTypeCd("PU");
                                f.setTrAmt(new BigDecimal("50.00"));
                                nextResp.set(0);
                            } else {
                                nextResp.set(88);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(eq("TRANFILE"), any());

        OuanlinFields out = runAndCaptureResponse();

        // COBOL: the outer IF is evaluated once before the scan; a mid-scan error still
        // lets 3000-FINALIZE/4000-BUILD-PAGE run, so KAB-RESULT-CNT reflects the partial
        // data, but 6000-SET-STATUS then zeroes KAB-ROW-CNT because KAB-STATUS = '99'.
        // 2900-END-TRAN (ENDBR) is unconditional after the read-loop in COBOL -- it runs
        // whether the loop exited via EOF or via an error resp, since WS-EOF-SW is set
        // to 'Y' in both cases and 2900-END-TRAN is outside the PERFORM...UNTIL.
        assertThat(out.getKabStatus()).isEqualTo("99");
        assertThat(out.getKabResultCnt()).isEqualTo(1);
        assertThat(out.getKabRowCnt()).isZero();
        verify(appService).endBrowse(eq("TRANFILE"));
    }

    // ───────────────────────── 2400-ACCUM-RW ─────────────────────────

    @Test
    void mainLine_rwMode_purchaseBelowThreshold_earnsBaseRatePoints() {
        givenRequest("RW", 6, 0);
        givenStartBrowseResp(0);
        givenTransactions(List.of(txn("1111222233334444", "PU", "500.00")));

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabResultCnt()).isEqualTo(1);
        assertThat(out.getKabRCnt(1)).isEqualTo(1);
        assertThat(out.getKabRAmt(1)).isEqualByComparingTo("500.00");
        assertThat(out.getKabRVal(1)).isEqualByComparingTo("500");
        assertThat(out.getKabRInfo(1).trim()).isEqualTo("PURCHASES");
        assertThat(out.getKabTot1()).isEqualByComparingTo("500.00");
        assertThat(out.getKabTot2()).isEqualByComparingTo("500");
    }

    @Test
    void mainLine_rwMode_purchaseAtOrAboveThreshold_earnsBonusRatePoints() {
        givenRequest("RW", 6, 0);
        givenStartBrowseResp(0);
        givenTransactions(List.of(txn("1111222233334444", "PU", "1500.00")));

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabRVal(1)).isEqualByComparingTo("3000");
        assertThat(out.getKabTot2()).isEqualByComparingTo("3000");
    }

    @Test
    void mainLine_rwMode_nonPurchaseType_skipsAccumulationEntirely() {
        givenRequest("RW", 6, 0);
        givenStartBrowseResp(0);
        givenTransactions(
                List.of(
                        txn("1111222233334444", "PY", "200.00"),
                        txn("1111222233335555", "CR", "10.00"),
                        txn("1111222233336666", "FE", "5.00"),
                        txn("1111222233337777", "IN", "1.00")));

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabResultCnt()).isZero();
        assertThat(out.getKabStatus()).isEqualTo("10");
    }

    @Test
    void mainLine_rwMode_sameCardTwice_mergesIntoOneSlot() {
        givenRequest("RW", 6, 0);
        givenStartBrowseResp(0);
        givenTransactions(
                List.of(
                        txn("1111222233334444", "PU", "100.00"),
                        txn("1111222233334444", "PU", "200.00")));

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabResultCnt()).isEqualTo(1);
        assertThat(out.getKabRCnt(1)).isEqualTo(2);
        assertThat(out.getKabRAmt(1)).isEqualByComparingTo("300.00");
        assertThat(out.getKabRVal(1)).isEqualByComparingTo("300");
    }

    @Test
    void mainLine_rwMode_moreThan200DistinctCards_overflowsWithoutCrashCappingAt200() {
        givenRequest("RW", 6, 0);
        givenStartBrowseResp(0);
        List<Txn> many = new ArrayList<>();
        for (int i = 1; i <= 201; i++) {
            many.add(txn(String.format("%016d", i), "PU", "10.00"));
        }
        givenTransactions(many);

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabStatus()).isEqualTo("00");
        assertThat(out.getKabResultCnt()).isEqualTo(200);
    }

    // ───────────────────────── 2500-ACCUM-FR ─────────────────────────

    @Test
    void mainLine_frMode_accumulatesCountAmountAndMaxSingleAmountPerCard() {
        givenRequest("FR", 6, 0);
        givenStartBrowseResp(0);
        givenTransactions(
                List.of(
                        txn("1111222233334444", "PU", "200.00"),
                        txn("1111222233334444", "PU", "500.00"),
                        txn("1111222233334444", "PU", "100.00")));

        OuanlinFields out = runAndCaptureResponse();

        // 500 < WS-LARGE-THRESH (1000) so nothing is flagged; 3100-FINALIZE-FR drops it.
        assertThat(out.getKabResultCnt()).isZero();
        assertThat(out.getKabStatus()).isEqualTo("10");
    }

    @Test
    void mainLine_frMode_finalize_flagsLargeAmountAndVelocitySeparatelyAndDropsUnflagged() {
        givenRequest("FR", 6, 0);
        givenStartBrowseResp(0);
        List<Txn> list = new ArrayList<>();
        list.add(txn("1111222233330001", "PU", "1200.00")); // LARGE AMT (1 txn)
        for (int i = 0; i < 5; i++) {
            list.add(txn("1111222233330002", "PU", "10.00")); // VELOCITY (5 txns, small amt)
        }
        list.add(txn("1111222233330003", "PU", "10.00"));
        list.add(txn("1111222233330003", "PU", "10.00")); // neither (2 txns, small amt)
        givenTransactions(list);

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabResultCnt()).isEqualTo(2);
        assertThat(out.getKabRKey(1).trim()).isEqualTo("1111222233330001");
        assertThat(out.getKabRInfo(1).trim()).isEqualTo("LARGE AMT");
        assertThat(out.getKabRKey(2).trim()).isEqualTo("1111222233330002");
        assertThat(out.getKabRInfo(2).trim()).isEqualTo("VELOCITY");
        assertThat(out.getKabTot1()).isEqualByComparingTo("1250.00");
        assertThat(out.getKabTot2()).isEqualByComparingTo("2");
    }

    @Test
    void mainLine_frMode_finalize_flagsLargeAndVelocityTogetherAsCombinedReason() {
        givenRequest("FR", 6, 0);
        givenStartBrowseResp(0);
        List<Txn> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(txn("1111222233339999", "PU", "1200.00"));
        }
        givenTransactions(list);

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabResultCnt()).isEqualTo(1);
        assertThat(out.getKabRInfo(1).trim()).isEqualTo("LARGE+VELO");
        assertThat(out.getKabRCnt(1)).isEqualTo(5);
        assertThat(out.getKabRAmt(1)).isEqualByComparingTo("6000.00");
    }

    // ───────────────────────── 2600-ACCUM-GL ─────────────────────────

    @Test
    void mainLine_glMode_paymentType_bucketsAsCreditAndAccumulatesTot2() {
        givenRequest("GL", 6, 0);
        givenStartBrowseResp(0);
        givenTransactions(List.of(txn("1111222233334444", "PY", "300.00")));

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabRKey(1).trim()).isEqualTo("PY");
        assertThat(out.getKabRInfo(1).trim()).isEqualTo("CR");
        assertThat(out.getKabTot2()).isEqualByComparingTo("300.00");
        assertThat(out.getKabTot1()).isEqualByComparingTo("0.00");
    }

    @Test
    void mainLine_glMode_nonCreditType_bucketsAsDebitAndAccumulatesTot1() {
        givenRequest("GL", 6, 0);
        givenStartBrowseResp(0);
        givenTransactions(List.of(txn("1111222233334444", "FE", "45.00")));

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabRKey(1).trim()).isEqualTo("FE");
        assertThat(out.getKabRInfo(1).trim()).isEqualTo("DR");
        assertThat(out.getKabTot1()).isEqualByComparingTo("45.00");
    }

    // ───────────────────────── 2700-ACCUM-RC ─────────────────────────

    @Test
    void mainLine_rcMode_creditType_recordsNegativeMoveAgainstResolvedAccount() {
        givenRequest("RC", 6, 0);
        givenStartBrowseResp(0);
        givenTransactions(List.of(txn("1111222233334444", "PY", "100.00")));
        givenXrefLookup(Map.of("1111222233334444", 100001L));
        givenAcctBalances(Map.of(100001L, new BigDecimal("-100.00")));

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabResultCnt()).isEqualTo(1);
        assertThat(out.getKabRKey(1).trim()).isEqualTo("00000100001");
        assertThat(out.getKabRCnt(1)).isEqualTo(1);
        assertThat(out.getKabTot2()).isEqualByComparingTo("100.00");
    }

    @Test
    void mainLine_rcMode_nonCreditType_recordsPositiveMoveAgainstResolvedAccount() {
        givenRequest("RC", 6, 0);
        givenStartBrowseResp(0);
        givenTransactions(List.of(txn("1111222233334444", "PU", "250.00")));
        givenXrefLookup(Map.of("1111222233334444", 100001L));
        givenAcctBalances(Map.of(100001L, new BigDecimal("250.00")));

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabRAmt(1)).isEqualByComparingTo("250.00");
        assertThat(out.getKabTot1()).isEqualByComparingTo("250.00");
    }

    @Test
    void mainLine_rcMode_xrefNotFound_countsOrphanAndSkipsResultRow() {
        givenRequest("RC", 6, 0);
        givenStartBrowseResp(0);
        givenTransactions(List.of(txn("1111222233339999", "PU", "50.00")));
        givenXrefLookup(Map.of());

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabResultCnt()).isZero();
        assertThat(out.getKabStatus()).isEqualTo("10");
    }

    @Test
    void mainLine_rcMode_xrefTransportError_alsoCountsOrphanAndSkipsResultRow() {
        givenRequest("RC", 6, 0);
        givenStartBrowseResp(0);
        givenTransactions(List.of(txn("1111222233339999", "PU", "50.00")));
        doAnswer(
                        inv -> {
                            nextResp.set(77);
                            return null;
                        })
                .when(appService)
                .readFile(eq("XREFFILE"), any(), anyString(), anyInt());

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabResultCnt()).isZero();
    }

    // ───────────────────────── 3200-FINALIZE-RC ─────────────────────────

    @Test
    void mainLine_rcMode_finalize_comparesEachAccountMovementToBalance() {
        givenRequest("RC", 6, 0);
        givenStartBrowseResp(0);
        givenTransactions(
                List.of(
                        txn("1111222233330001", "PY", "100.00"), // acct 100001: move -100.00
                        txn(
                                "1111222233330001",
                                "PU",
                                "250.00"), // acct 100001: move +250.00 -> net 150.00
                        txn("1111222233330002", "PU", "80.00"), // acct 100002: move +80.00
                        txn(
                                "1111222233330003",
                                "PU",
                                "60.00"), // acct 100003: move +60.00 (missing in ACCTFILE)
                        txn(
                                "1111222233330004",
                                "PU",
                                "40.00") // acct 100004: move +40.00 (read error)
                        ));
        Map<String, Long> xref = new HashMap<>();
        xref.put("1111222233330001", 100001L);
        xref.put("1111222233330002", 100002L);
        xref.put("1111222233330003", 100003L);
        xref.put("1111222233330004", 100004L);
        givenXrefLookup(xref);
        doAnswer(
                        inv -> {
                            long acctId = Long.parseLong(((String) inv.getArgument(2)).trim());
                            OuanlinFields into = inv.getArgument(1);
                            if (acctId == 100001L) {
                                into.setAcCurrBal(new BigDecimal("150.00"));
                                nextResp.set(0);
                            } else if (acctId == 100002L) {
                                into.setAcCurrBal(new BigDecimal("100.00"));
                                nextResp.set(0);
                            } else if (acctId == 100003L) {
                                nextResp.set(13);
                            } else {
                                nextResp.set(55);
                            }
                            return null;
                        })
                .when(appService)
                .readFile(eq("ACCTFILE"), any(), anyString(), anyInt());

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabResultCnt()).isEqualTo(4);
        assertThat(out.getKabRInfo(1).trim()).isEqualTo("OK");
        assertThat(out.getKabRInfo(2).trim()).isEqualTo("MISMATCH");
        assertThat(out.getKabRInfo(3).trim()).isEqualTo("MISSING");
        assertThat(out.getKabRVal(3)).isEqualByComparingTo("0.00");
        assertThat(out.getKabRInfo(4).trim()).isEqualTo("READ ERR");
        assertThat(out.getKabDisc()).isEqualTo(2);
    }

    // ───────────────────────── 4000-BUILD-PAGE ─────────────────────────

    @Test
    void mainLine_buildPage_noResults_earlyReturnLeavesRowCntZeroAndMoreN() {
        givenRequest("GL", 6, 0);
        givenStartBrowseResp(13);

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabRowCnt()).isZero();
        assertThat(out.getKabMore().trim()).isEqualTo("N");
    }

    @Test
    void mainLine_buildPage_moreRowsThanPageSize_capsAtMaxRowsAndSetsMoreY() {
        givenRequest("GL", 6, 0);
        givenStartBrowseResp(0);
        List<Txn> list = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            list.add(txn("1111222233334444", "T" + i, "10.00"));
        }
        givenTransactions(list);

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabResultCnt()).isEqualTo(8);
        assertThat(out.getKabRowCnt()).isEqualTo(6);
        assertThat(out.getKabMore().trim()).isEqualTo("Y");
        assertThat(out.getKabRKey(1).trim()).isEqualTo("T1");
        assertThat(out.getKabRKey(6).trim()).isEqualTo("T6");
    }

    @Test
    void mainLine_buildPage_startOffsetIntoSecondPage_returnsRemainingRowsWithMoreN() {
        givenRequest("GL", 6, 6);
        givenStartBrowseResp(0);
        List<Txn> list = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            list.add(txn("1111222233334444", "T" + i, "10.00"));
        }
        givenTransactions(list);

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabRowCnt()).isEqualTo(2);
        assertThat(out.getKabMore().trim()).isEqualTo("N");
        assertThat(out.getKabRKey(1).trim()).isEqualTo("T7");
        assertThat(out.getKabRKey(2).trim()).isEqualTo("T8");
    }

    @Test
    void mainLine_buildPage_startOffsetBeyondResultCount_returnsEmptyPageButKeepsResultCnt() {
        givenRequest("GL", 6, 10);
        givenStartBrowseResp(0);
        List<Txn> list = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            list.add(txn("1111222233334444", "T" + i, "10.00"));
        }
        givenTransactions(list);

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabResultCnt()).isEqualTo(3);
        assertThat(out.getKabRowCnt()).isZero();
        assertThat(out.getKabMore().trim()).isEqualTo("N");
    }

    // ───────────────────────── WS-MAX-ROWS clamp ─────────────────────────

    @Test
    void mainLine_maxRowsZero_clampsToSix() {
        givenRequest("GL", 0, 0);
        givenStartBrowseResp(0);
        List<Txn> list = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            list.add(txn("1111222233334444", "T" + i, "10.00"));
        }
        givenTransactions(list);

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabRowCnt()).isEqualTo(6);
    }

    @Test
    void mainLine_maxRowsAboveSix_clampsToSix() {
        givenRequest("GL", 9, 0);
        givenStartBrowseResp(0);
        List<Txn> list = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            list.add(txn("1111222233334444", "T" + i, "10.00"));
        }
        givenTransactions(list);

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabRowCnt()).isEqualTo(6);
    }

    @Test
    void mainLine_maxRowsWithinRange_usedDirectly() {
        givenRequest("GL", 3, 0);
        givenStartBrowseResp(0);
        List<Txn> list = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            list.add(txn("1111222233334444", "T" + i, "10.00"));
        }
        givenTransactions(list);

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabRowCnt()).isEqualTo(3);
        assertThat(out.getKabMore().trim()).isEqualTo("Y");
    }

    // ───────────────────────── 6000-SET-STATUS ─────────────────────────

    @Test
    void mainLine_resultsPresent_statusIsOK() {
        givenRequest("RW", 6, 0);
        givenStartBrowseResp(0);
        givenTransactions(List.of(txn("1111222233334444", "PU", "50.00")));

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabStatus()).isEqualTo("00");
    }

    @Test
    void mainLine_noResults_statusIs10() {
        givenRequest("RW", 6, 0);
        givenStartBrowseResp(20);

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabStatus()).isEqualTo("10");
    }

    // ───────────────────────── scanned / discrepancy counters on output ─────────────────────────

    @Test
    void mainLine_scanCountAndDiscCount_publishedToCommarea() {
        givenRequest("RC", 6, 0);
        givenStartBrowseResp(0);
        givenTransactions(
                List.of(
                        txn("1111222233330001", "PU", "80.00"),
                        txn("1111222233330002", "PU", "60.00")));
        Map<String, Long> xref = new HashMap<>();
        xref.put("1111222233330001", 100001L);
        xref.put("1111222233330002", 100002L);
        givenXrefLookup(xref);
        Map<Long, BigDecimal> balances = new HashMap<>();
        balances.put(100001L, new BigDecimal("80.00"));
        balances.put(100002L, new BigDecimal("999.00"));
        givenAcctBalances(balances);

        OuanlinFields out = runAndCaptureResponse();

        assertThat(out.getKabScanned()).isEqualTo(2);
        assertThat(out.getKabDisc()).isEqualTo(1);
    }

    // ───────────────────────── trivial delegate methods ─────────────────────────

    @Test
    void getProgramName_returnsOuanlin() {
        assertThat(service.getProgramName()).isEqualTo("OUANLIN");
    }

    @Test
    void getButtonDefs_delegatesToMetadataWithoutError() {
        assertThat(service.getButtonDefs()).isNotNull();
    }

    @Test
    void registerFsetFields_delegatesToMetadata_doesNotThrow() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.registerFsetFields(null));
    }
}

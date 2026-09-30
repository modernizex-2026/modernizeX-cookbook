package com.generated.orion.outrnin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.generated.orion.outrnin.accessor.OutrninFields;
import com.generated.orion.outrnin.model.WorkingStorage;

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
 * Unit tests for OutrninService, generated from COBOL program OUTRNIN (ORION-CCMS
 * transaction-inquiry browse / analytics engine). All business logic lives in private
 * paragraph-methods and is exercised solely through the public {@link
 * OutrninService#mainLine(AppService)} entry point, driven by an {@link AppService} mock emulating
 * CICS STARTBR/READNEXT/ENDBR, TCATFILE/TTYPFILE READ, and the KTRNIN-AREA commarea round-trip.
 *
 * <p>Convert-gap found: COBOL's 2600-LOOKUP-DESC builds WS-TYCAT via {@code STRING TR-TYPE-CD '/'
 * WS-CAT-ED}. WS-CAT-ED is PIC 9(04) DISPLAY, so its in-memory character representation is always 4
 * zero-padded digits (e.g. "0007"), and STRING copies those characters verbatim, yielding e.g.
 * "PU/0007" (matches the PIC X(07) KTR-TYCAT width exactly). The generated Java instead does {@code
 * String.valueOf(trTypeCd) + "/" + String.valueOf(getWsCatEd())} where {@code getWsCatEd()} is an
 * {@code int} — {@code String.valueOf(7)} yields "7", not "0007", dropping the leading zeros COBOL
 * would keep. See the CONVERT-GAP test below.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OutrninServiceTest {

    @Mock private AppService appService;

    private final OutrninService service = new OutrninService();

    private final AtomicInteger eibresp = new AtomicInteger(0);

    /** Simple TRANFILE row used to drive the READNEXT sequence. */
    private static final class Txn {
        final String id;
        final String cardNum;
        final String typeCd;
        final int catCd;
        final int merchantId;
        final String merchantName;
        final BigDecimal amt;
        final String procTs;

        Txn(
                String id,
                String cardNum,
                String typeCd,
                int catCd,
                int merchantId,
                String merchantName,
                String amt,
                String procTs) {
            this.id = id;
            this.cardNum = cardNum;
            this.typeCd = typeCd;
            this.catCd = catCd;
            this.merchantId = merchantId;
            this.merchantName = merchantName;
            this.amt = new BigDecimal(amt);
            this.procTs = procTs;
        }
    }

    private Txn txn(
            String id,
            String cardNum,
            String typeCd,
            int catCd,
            int merchantId,
            String merchantName,
            String amt,
            String procTs) {
        return new Txn(id, cardNum, typeCd, catCd, merchantId, merchantName, amt, procTs);
    }

    @BeforeEach
    void setUp() {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getEibresp()).thenAnswer(inv -> eibresp.get());
        // Default: both lookup files miss -> WS-DESC falls through to "UNKNOWN",
        // matching COBOL 2600-LOOKUP-DESC / 2650-LOOKUP-TYPE default path.
        doAnswer(
                        inv -> {
                            eibresp.set(1);
                            return null;
                        })
                .when(appService)
                .readFile(eq("TCATFILE"), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            eibresp.set(1);
                            return null;
                        })
                .when(appService)
                .readFile(eq("TTYPFILE"), any(), anyString(), anyInt());
    }

    private void givenRequest(
            String filter, String startKey, java.util.function.Consumer<OutrninFields> customizer) {
        OutrninFields req = new OutrninFields(new WorkingStorage());
        req.setKtiFilter(filter);
        req.setKtiStartKey(startKey);
        if (customizer != null) {
            customizer.accept(req);
        }
        when(appService.getCommarea()).thenReturn(req.getKtrninArea());
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

    /** Feeds READNEXT with the given transactions in order, then ENDFILE (resp 20). */
    private void givenReadNextSequence(List<Txn> txns) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i >= txns.size()) {
                                eibresp.set(20);
                                return null;
                            }
                            Txn t = txns.get(i);
                            OutrninFields f = inv.getArgument(1);
                            f.setTrId(t.id);
                            f.setTrCardNum(t.cardNum);
                            f.setTrTypeCd(t.typeCd);
                            f.setTrCatCd(t.catCd);
                            f.setTrMerchantId(t.merchantId);
                            f.setTrMerchantName(t.merchantName);
                            f.setTrAmt(t.amt);
                            f.setTrProcTs(t.procTs);
                            eibresp.set(0);
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
    }

    private void givenTcatLookup(int resp, String desc) {
        doAnswer(
                        inv -> {
                            eibresp.set(resp);
                            if (resp == 0) {
                                OutrninFields into = inv.getArgument(1);
                                into.setTcDesc(desc);
                            }
                            return null;
                        })
                .when(appService)
                .readFile(eq("TCATFILE"), any(), anyString(), anyInt());
    }

    private void givenTtypLookup(int resp, String desc) {
        doAnswer(
                        inv -> {
                            eibresp.set(resp);
                            if (resp == 0) {
                                OutrninFields into = inv.getArgument(1);
                                into.setTtDesc(desc);
                            }
                            return null;
                        })
                .when(appService)
                .readFile(eq("TTYPFILE"), any(), anyString(), anyInt());
    }

    private OutrninFields runAndCaptureResponse() {
        service.mainLine(appService);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService).setCommarea(captor.capture());
        OutrninFields out = new OutrninFields(new WorkingStorage());
        out.writeBytes("KTRNIN-AREA", (byte[]) captor.getValue());
        return out;
    }

    // ---------------------------------------------------------------
    // Happy path: CARD filter, single matching transaction, category
    // description found on the first TCATFILE lookup.
    // ---------------------------------------------------------------

    @Test
    void mainLine_cardFilterMatch_returnsRowWithTcatDescriptionAndAnalytics() {
        givenRequest("C", "", f -> f.setKtiCard("4111111111111111"));
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "4111111111111111",
                                "PU",
                                1234,
                                500,
                                "Acme Store",
                                "25.50",
                                "2024-01-15")));
        givenTcatLookup(0, "Groceries");

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getString("KTI-RETURN-CD")).isEqualTo("N");
        assertThat(out.getKtiRowCount()).isEqualTo(1);
        assertThat(out.getKtiScanCount()).isEqualTo(1);
        assertThat(out.getKtiMatchCount()).isEqualTo(1);
        assertThat(out.getKtiNetTotal()).isEqualByComparingTo("25.50");
        assertThat(out.getKtiPurchCnt()).isEqualTo(1);
        assertThat(out.getKtiPurchSum()).isEqualByComparingTo("25.50");
        assertThat(out.getKtiMaxAmt()).isEqualByComparingTo("25.50");
        assertThat(out.getKtiMaxId().trim()).isEqualTo("TRN0000000000001");
        assertThat(out.getKtrId(1).trim()).isEqualTo("TRN0000000000001");
        assertThat(out.getKtrCard(1).trim()).isEqualTo("4111111111111111");
        assertThat(out.getKtrTycat(1).trim()).isEqualTo("PU/1234");
        assertThat(out.getKtrDesc(1).trim()).isEqualTo("Groceries");
        assertThat(out.getKtrMerch(1).trim()).isEqualTo("Acme Store");
        assertThat(out.getKtrAmt(1)).isEqualByComparingTo("25.50");
        assertThat(out.getKtrDate(1).trim()).isEqualTo("2024-01-15");
        assertThat(out.getString("KTI-MORE-SW")).isEqualTo("N");
        assertThat(out.getKtiNextKey().trim()).isEqualTo("TRN0000000000001");
        verify(appService).endBrowse(anyString());
    }

    @Test
    void mainLine_cardFilterNoMatch_returnsZeroRows() {
        givenRequest("C", "", f -> f.setKtiCard("4111111111111111"));
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "5222222222222222",
                                "PU",
                                1234,
                                500,
                                "Acme Store",
                                "25.50",
                                "2024-01-15")));

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getKtiRowCount()).isEqualTo(0);
        assertThat(out.getKtiScanCount()).isEqualTo(1);
        assertThat(out.getKtiMatchCount()).isEqualTo(0);
    }

    // ---------------------------------------------------------------
    // DATE filter.
    // ---------------------------------------------------------------

    @Test
    void mainLine_dateFilterWithinRange_matches() {
        givenRequest(
                "D",
                "",
                f -> {
                    f.setKtiDateFrom("2024-01-01");
                    f.setKtiDateTo("2024-01-31");
                });
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "4111111111111111",
                                "PY",
                                10,
                                500,
                                "Acme Store",
                                "10.00",
                                "2024-01-15")));

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getKtiRowCount()).isEqualTo(1);
        assertThat(out.getKtiPayCnt()).isEqualTo(1);
    }

    @Test
    void mainLine_dateFilterOutsideRange_noMatch() {
        givenRequest(
                "D",
                "",
                f -> {
                    f.setKtiDateFrom("2024-01-01");
                    f.setKtiDateTo("2024-01-31");
                });
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "4111111111111111",
                                "PY",
                                10,
                                500,
                                "Acme Store",
                                "10.00",
                                "2024-02-01")));

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getKtiRowCount()).isEqualTo(0);
    }

    // ---------------------------------------------------------------
    // MERCHANT filter.
    // ---------------------------------------------------------------

    @Test
    void mainLine_merchantFilterMatch_matches() {
        givenRequest("M", "", f -> f.setKtiMerchId(777));
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "4111111111111111",
                                "PU",
                                10,
                                777,
                                "Acme Store",
                                "10.00",
                                "2024-01-15")));

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getKtiRowCount()).isEqualTo(1);
    }

    @Test
    void mainLine_merchantFilterNoMatch_noMatch() {
        givenRequest("M", "", f -> f.setKtiMerchId(777));
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "4111111111111111",
                                "PU",
                                10,
                                888,
                                "Acme Store",
                                "10.00",
                                "2024-01-15")));

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getKtiRowCount()).isEqualTo(0);
    }

    // ---------------------------------------------------------------
    // TYPE+CATEGORY filter.
    // ---------------------------------------------------------------

    @Test
    void mainLine_typeCatFilterBothMatch_matches() {
        givenRequest(
                "T",
                "",
                f -> {
                    f.setKtiFType("FE");
                    f.setKtiFCat(20);
                });
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "4111111111111111",
                                "FE",
                                20,
                                500,
                                "Acme Store",
                                "5.00",
                                "2024-01-15")));

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getKtiRowCount()).isEqualTo(1);
        assertThat(out.getKtiFeeCnt()).isEqualTo(1);
    }

    @Test
    void mainLine_typeCatFilterCategoryMismatch_noMatch() {
        givenRequest(
                "T",
                "",
                f -> {
                    f.setKtiFType("FE");
                    f.setKtiFCat(20);
                });
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "4111111111111111",
                                "FE",
                                21,
                                500,
                                "Acme Store",
                                "5.00",
                                "2024-01-15")));

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getKtiRowCount()).isEqualTo(0);
    }

    // ---------------------------------------------------------------
    // AMOUNT filter.
    // ---------------------------------------------------------------

    @Test
    void mainLine_amountFilterAtThreshold_matches() {
        givenRequest("A", "", f -> f.setKtiAmtThresh(new BigDecimal("100.00")));
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "4111111111111111",
                                "IN",
                                30,
                                500,
                                "Acme Store",
                                "100.00",
                                "2024-01-15")));

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getKtiRowCount()).isEqualTo(1);
        assertThat(out.getKtiIntCnt()).isEqualTo(1);
    }

    @Test
    void mainLine_amountFilterBelowThreshold_noMatch() {
        givenRequest("A", "", f -> f.setKtiAmtThresh(new BigDecimal("100.00")));
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "4111111111111111",
                                "IN",
                                30,
                                500,
                                "Acme Store",
                                "99.99",
                                "2024-01-15")));

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getKtiRowCount()).isEqualTo(0);
    }

    // ---------------------------------------------------------------
    // Unknown filter code: COBOL EVALUATE TRUE ... WHEN OTHER CONTINUE
    // leaves WS-MATCH-SW at its pre-set 'N' -> nothing ever matches.
    // ---------------------------------------------------------------

    @Test
    void mainLine_unknownFilterCode_neverMatches() {
        givenRequest("Z", "", null);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "4111111111111111",
                                "PU",
                                10,
                                500,
                                "Acme Store",
                                "10.00",
                                "2024-01-15")));

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getKtiRowCount()).isEqualTo(0);
        assertThat(out.getKtiScanCount()).isEqualTo(1);
        assertThat(out.getKtiMatchCount()).isEqualTo(0);
    }

    // ---------------------------------------------------------------
    // 2600-LOOKUP-DESC / 2650-LOOKUP-TYPE fallback chain.
    // ---------------------------------------------------------------

    @Test
    void mainLine_tcatLookupFails_fallsBackToTtypDescription() {
        givenRequest("C", "", f -> f.setKtiCard("4111111111111111"));
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "4111111111111111",
                                "PU",
                                1234,
                                500,
                                "Acme Store",
                                "10.00",
                                "2024-01-15")));
        givenTcatLookup(1, null);
        givenTtypLookup(0, "Purchase");

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getKtrDesc(1).trim()).isEqualTo("Purchase");
    }

    @Test
    void mainLine_bothLookupsFail_descriptionIsUnknown() {
        givenRequest("C", "", f -> f.setKtiCard("4111111111111111"));
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "4111111111111111",
                                "PU",
                                1234,
                                500,
                                "Acme Store",
                                "10.00",
                                "2024-01-15")));
        givenTcatLookup(1, null);
        givenTtypLookup(1, null);

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getKtrDesc(1).trim()).isEqualTo("UNKNOWN");
    }

    // ---------------------------------------------------------------
    // WS-MAX-ROWS (6) page cap: loop keeps scanning/accumulating but
    // stops storing rows and signals KTI-MORE-SW.
    // ---------------------------------------------------------------

    @Test
    void mainLine_moreThanMaxRowsMatch_stopsAt6AndSignalsMore() {
        givenRequest("A", "", f -> f.setKtiAmtThresh(BigDecimal.ZERO));
        givenStartBrowseResp(0);
        List<Txn> txns = new java.util.ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            txns.add(
                    txn(
                            String.format("TRN%013d", i),
                            "4111111111111111",
                            "PU",
                            10,
                            500,
                            "Acme Store",
                            "10.00",
                            "2024-01-15"));
        }
        givenReadNextSequence(txns);

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getKtiRowCount()).isEqualTo(6);
        assertThat(out.getKtiScanCount()).isEqualTo(7);
        assertThat(out.getKtiMatchCount()).isEqualTo(7);
        assertThat(out.getString("KTI-MORE-SW")).isEqualTo("Y");
        assertThat(out.getKtiNextKey().trim()).isEqualTo("TRN0000000000006");
        // 7 transaction reads + 1 final READNEXT that returns ENDFILE (resp 20) to exit the loop.
        verify(appService, times(8)).readNext(anyString(), any());
    }

    // ---------------------------------------------------------------
    // Resume paging: rows at/below KTI-START-KEY are still accumulated
    // into the analytics but are NOT re-stored or counted toward MORE.
    // ---------------------------------------------------------------

    @Test
    void mainLine_rowsAtOrBelowStartKey_accumulateOnlyNotStored() {
        givenRequest("A", "TRN0000000000002", f -> f.setKtiAmtThresh(BigDecimal.ZERO));
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "4111111111111111",
                                "PU",
                                10,
                                500,
                                "Acme Store",
                                "5.00",
                                "2024-01-15"),
                        txn(
                                "TRN0000000000002",
                                "4111111111111111",
                                "PU",
                                10,
                                500,
                                "Acme Store",
                                "6.00",
                                "2024-01-15"),
                        txn(
                                "TRN0000000000003",
                                "4111111111111111",
                                "PU",
                                10,
                                500,
                                "Acme Store",
                                "7.00",
                                "2024-01-15")));

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getKtiScanCount()).isEqualTo(3);
        assertThat(out.getKtiMatchCount()).isEqualTo(3);
        assertThat(out.getKtiNetTotal()).isEqualByComparingTo("18.00");
        assertThat(out.getKtiRowCount()).isEqualTo(1);
        assertThat(out.getKtrId(1).trim()).isEqualTo("TRN0000000000003");
        assertThat(out.getString("KTI-MORE-SW")).isEqualTo("N");
    }

    // ---------------------------------------------------------------
    // 2700-ACCUMULATE: per-type subtotals and running max across matches.
    // ---------------------------------------------------------------

    @Test
    void mainLine_multipleTypes_accumulatesPerTypeSubtotalsAndRunningMax() {
        givenRequest("A", "", f -> f.setKtiAmtThresh(BigDecimal.ZERO));
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "4111111111111111",
                                "PU",
                                10,
                                500,
                                "Acme Store",
                                "10.00",
                                "2024-01-15"),
                        txn(
                                "TRN0000000000002",
                                "4111111111111111",
                                "PY",
                                10,
                                500,
                                "Acme Store",
                                "50.00",
                                "2024-01-15"),
                        txn(
                                "TRN0000000000003",
                                "4111111111111111",
                                "FE",
                                10,
                                500,
                                "Acme Store",
                                "5.00",
                                "2024-01-15"),
                        txn(
                                "TRN0000000000004",
                                "4111111111111111",
                                "IN",
                                10,
                                500,
                                "Acme Store",
                                "2.00",
                                "2024-01-15"),
                        txn(
                                "TRN0000000000005",
                                "4111111111111111",
                                "XX",
                                10,
                                500,
                                "Acme Store",
                                "1.00",
                                "2024-01-15")));

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getKtiMatchCount()).isEqualTo(5);
        assertThat(out.getKtiNetTotal()).isEqualByComparingTo("68.00");
        assertThat(out.getKtiPurchCnt()).isEqualTo(1);
        assertThat(out.getKtiPurchSum()).isEqualByComparingTo("10.00");
        assertThat(out.getKtiPayCnt()).isEqualTo(1);
        assertThat(out.getKtiPaySum()).isEqualByComparingTo("50.00");
        assertThat(out.getKtiFeeCnt()).isEqualTo(1);
        assertThat(out.getKtiFeeSum()).isEqualByComparingTo("5.00");
        assertThat(out.getKtiIntCnt()).isEqualTo(1);
        assertThat(out.getKtiIntSum()).isEqualByComparingTo("2.00");
        assertThat(out.getKtiMaxAmt()).isEqualByComparingTo("50.00");
        assertThat(out.getKtiMaxId().trim()).isEqualTo("TRN0000000000002");
    }

    // ---------------------------------------------------------------
    // No matches at all: KTI-NEXT-KEY must stay blank (never overwritten
    // by WS-LAST-KEY when KTI-ROW-COUNT stays zero).
    // ---------------------------------------------------------------

    @Test
    void mainLine_noMatches_nextKeyStaysBlank() {
        givenRequest("C", "", f -> f.setKtiCard("0000000000000000"));
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "4111111111111111",
                                "PU",
                                10,
                                500,
                                "Acme Store",
                                "10.00",
                                "2024-01-15")));

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getKtiRowCount()).isEqualTo(0);
        assertThat(out.getKtiNextKey().trim()).isEmpty();
    }

    // ---------------------------------------------------------------
    // 2100-START-BROWSE RESP handling.
    // ---------------------------------------------------------------

    @Test
    void mainLine_startBrowseNotFound_endsWithNoRowsNoError() {
        givenRequest("A", "", f -> f.setKtiAmtThresh(BigDecimal.ZERO));
        givenStartBrowseResp(13);

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getString("KTI-RETURN-CD")).isEqualTo("N");
        assertThat(out.getKtiRowCount()).isEqualTo(0);
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startBrowseEndfile_endsWithNoRowsNoError() {
        givenRequest("A", "", f -> f.setKtiAmtThresh(BigDecimal.ZERO));
        givenStartBrowseResp(20);

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getString("KTI-RETURN-CD")).isEqualTo("N");
        assertThat(out.getKtiRowCount()).isEqualTo(0);
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startBrowseOtherResp_setsErrorReturnCode() {
        givenRequest("A", "", f -> f.setKtiAmtThresh(BigDecimal.ZERO));
        givenStartBrowseResp(99);

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getString("KTI-RETURN-CD")).isEqualTo("E");
        assertThat(out.getKtiRowCount()).isEqualTo(0);
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    // ---------------------------------------------------------------
    // 2200-READ-NEXT RESP handling — an error mid-browse still closes
    // the browse (ENDBR runs because STARTBR itself succeeded), unlike
    // a STARTBR-time failure above.
    // ---------------------------------------------------------------

    @Test
    void mainLine_readNextOtherRespMidBrowse_stopsWithErrorButKeepsRowsSoFarAndClosesBrowse() {
        givenRequest("A", "", f -> f.setKtiAmtThresh(BigDecimal.ZERO));
        givenStartBrowseResp(0);
        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            if (callCount.incrementAndGet() == 1) {
                                OutrninFields f = inv.getArgument(1);
                                f.setTrId("TRN0000000000001");
                                f.setTrCardNum("4111111111111111");
                                f.setTrTypeCd("PU");
                                f.setTrCatCd(10);
                                f.setTrMerchantId(500);
                                f.setTrMerchantName("Acme Store");
                                f.setTrAmt(new BigDecimal("10.00"));
                                f.setTrProcTs("2024-01-15");
                                eibresp.set(0);
                            } else {
                                eibresp.set(77);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getString("KTI-RETURN-CD")).isEqualTo("E");
        assertThat(out.getKtiRowCount()).isEqualTo(1);
        assertThat(out.getKtiScanCount()).isEqualTo(1);
        verify(appService).endBrowse(anyString());
    }

    // ---------------------------------------------------------------
    // CONVERT-GAP: WS-CAT-ED (PIC 9(04) DISPLAY) formatted into WS-TYCAT
    // via COBOL STRING keeps its zero-padded 4-digit representation, but
    // the generated Java uses String.valueOf(int) which drops leading
    // zeros. See class javadoc for full explanation.
    // ---------------------------------------------------------------

    @Test
    void mainLine_categoryBelow1000_CONVERT_GAP_tycatKeepsLeadingZerosInCobol() {
        givenRequest("C", "", f -> f.setKtiCard("4111111111111111"));
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "4111111111111111",
                                "PU",
                                7,
                                500,
                                "Acme Store",
                                "10.00",
                                "2024-01-15")));
        givenTcatLookup(0, "Misc");

        OutrninFields out = runAndCaptureResponse();

        assertThat(out.getKtrTycat(1).trim())
                .as(
                        "COBOL STRING TR-TYPE-CD '/' WS-CAT-ED keeps WS-CAT-ED's 4-digit "
                                + "zero-padded DISPLAY representation ('PU/0007'); the generated "
                                + "Java uses String.valueOf(int) and drops the leading zeros")
                .isEqualTo("PU/0007");
    }

    // ---------------------------------------------------------------
    // mainLine commarea unwrapping: Object[] variants (CALL USING style
    // BY REFERENCE params), covering the branches never hit when
    // getCommarea() returns a plain String/byte[] as in the tests above.
    // ---------------------------------------------------------------

    @Test
    void mainLine_objectArrayCommareaWithByteArray_roundTripsThroughSameArraySlot() {
        OutrninFields req = new OutrninFields(new WorkingStorage());
        req.setKtiFilter("A");
        req.setKtiAmtThresh(BigDecimal.ZERO);
        Object[] params = new Object[] {req.sliceBytes("KTRNIN-AREA")};
        when(appService.getCommarea()).thenReturn(params);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                List.of(
                        txn(
                                "TRN0000000000001",
                                "4111111111111111",
                                "PU",
                                10,
                                500,
                                "Acme Store",
                                "10.00",
                                "2024-01-15")));

        service.mainLine(appService);

        assertThat(params[0]).isInstanceOf(byte[].class);
        OutrninFields out = new OutrninFields(new WorkingStorage());
        out.writeBytes("KTRNIN-AREA", (byte[]) params[0]);
        assertThat(out.getKtiRowCount()).isEqualTo(1);
        verify(appService, never()).setCommarea(any());
    }

    @Test
    void mainLine_objectArrayCommareaWithString_setsGroupFromStringElement() {
        OutrninFields req = new OutrninFields(new WorkingStorage());
        req.setKtiFilter("A");
        req.setKtiAmtThresh(BigDecimal.ZERO);
        Object[] params = new Object[] {req.getKtrninArea()};
        when(appService.getCommarea()).thenReturn(params);
        givenStartBrowseResp(20);

        service.mainLine(appService);

        assertThat(params[0]).isInstanceOf(String.class);
        OutrninFields out = new OutrninFields(new WorkingStorage());
        out.setKtrninArea((String) params[0]);
        assertThat(out.getString("KTI-RETURN-CD")).isEqualTo("N");
        assertThat(out.getKtiRowCount()).isEqualTo(0);
        verify(appService, never()).setCommarea(any());
    }

    // ---------------------------------------------------------------
    // Trivial delegate methods.
    // ---------------------------------------------------------------

    @Test
    void getProgramName_returnsOutrnin() {
        assertThat(service.getProgramName()).isEqualTo("OUTRNIN");
    }

    @Test
    void getButtonDefs_delegatesToMetadata_returnsEmptyList() {
        assertThat(service.getButtonDefs()).isEmpty();
    }

    @Test
    void registerFsetFields_delegatesToMetadata_doesNotThrow() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.registerFsetFields(null));
    }

    @Test
    void bmsMetadata_getMapNames_returnsEmptyList() {
        assertThat(com.generated.orion.outrnin.metadata.OutrninBmsMetadata.getMapNames()).isEmpty();
    }

    @Test
    void bmsMetadata_getLayoutResource_returnsWorkingStorageXmlPath() {
        assertThat(com.generated.orion.outrnin.metadata.OutrninBmsMetadata.getLayoutResource())
                .isEqualTo("layout/OUTRNIN_WS.xml");
    }

    @Test
    void bmsMetadata_getFieldMapping_returnsEmptyMapping() {
        assertThat(
                        com.generated.orion.outrnin.metadata.OutrninBmsMetadata.getFieldMapping(
                                "ANY-MAP"))
                .isNotNull();
    }
}

package com.generated.orion.ouxref.service;

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
import com.generated.orion.ouxref.accessor.OuxrefFields;
import com.generated.orion.ouxref.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OuxrefService, generated from COBOL program OUXREF (ORION-CCMS on-line card
 * cross-reference rebuild/validate sub). Business logic lives entirely in private paragraph-methods
 * and is exercised only through the public {@link OuxrefService#mainLine(AppService)} entry point,
 * driven by an {@link AppService} mock emulating CICS STARTBR/READNEXT/READ/READ-UPDATE/
 * REWRITE/WRITE/ENDBR and the KUX-PARM (KCOMM/CA-WORK-AREA overlay) round-trip.
 *
 * <p>The KUX-PARM group is aliased onto CA-WORK-AREA (see OUXREF.cbl "SET ADDRESS OF KUX-PARM TO
 * ADDRESS OF CA-WORK-AREA"), so both the request builder and the response reader must call {@code
 * aliasGroup("KUX-PARM", "CA-WORK-AREA")} on their own {@link OuxrefFields} instance before
 * touching any KUX-* field, exactly mirroring the service's own paragraph 0000-MAIN.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OuxrefServiceTest {

    @Mock private AppService appService;

    private final OuxrefService service = new OuxrefService();

    private final AtomicInteger eibresp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        when(appService.getEibcalen()).thenReturn(692);
        when(appService.getEibresp()).thenAnswer(inv -> eibresp.get());
    }

    private String buildRequest(String mode, int max, String startCard) {
        OuxrefFields req = new OuxrefFields(new WorkingStorage());
        req.aliasGroup("KUX-PARM", "CA-WORK-AREA");
        req.setKuxMode(mode);
        req.setKuxMax(max);
        req.setKuxStartCard(startCard == null ? "" : startCard);
        return req.getOrionCommarea();
    }

    private void givenRequest(String mode, int max, String startCard) {
        when(appService.getCommarea()).thenReturn(buildRequest(mode, max, startCard));
    }

    private OuxrefFields runAndCaptureResponse() {
        service.mainLine(appService);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService).setCommarea(captor.capture());
        OuxrefFields out = new OuxrefFields(new WorkingStorage());
        out.aliasGroup("KUX-PARM", "CA-WORK-AREA");
        out.writeBytes("ORION-COMMAREA", (byte[]) captor.getValue());
        return out;
    }

    /** Simple CARDFILE row used to drive the READNEXT (loop + peek) sequence. */
    private static final class Card {
        final String num;
        final long acctId;

        Card(String num, long acctId) {
            this.num = num;
            this.acctId = acctId;
        }
    }

    private Card card(String num, long acctId) {
        return new Card(num, acctId);
    }

    private final AtomicInteger cardPos = new AtomicInteger(0);
    private List<Card> cards = List.of();

    private void givenStartBrowseResp(int resp) {
        doAnswer(
                        inv -> {
                            eibresp.set(resp);
                            cardPos.set(0);
                            return null;
                        })
                .when(appService)
                .startBrowse(anyString(), anyString(), anyInt());
    }

    /** Feeds READNEXT with the given cards in order, then ENDFILE (resp 20) forever after. */
    private void givenCardSequence(List<Card> seq) {
        cards = seq;
        doAnswer(
                        inv -> {
                            int i = cardPos.get();
                            if (i >= cards.size()) {
                                eibresp.set(20);
                                return null;
                            }
                            Card c = cards.get(i);
                            cardPos.incrementAndGet();
                            OuxrefFields f = inv.getArgument(1);
                            f.setCdNum(c.num);
                            f.setCdAcctId(c.acctId);
                            eibresp.set(0);
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
    }

    private void givenAccountRead(int resp) {
        doAnswer(
                        inv -> {
                            eibresp.set(resp);
                            return null;
                        })
                .when(appService)
                .readFile(eq("ACCTFILE"), any(), anyString(), anyInt());
    }

    private void givenXrefRead(int resp, Integer custId) {
        doAnswer(
                        inv -> {
                            eibresp.set(resp);
                            if (resp == 0 && custId != null) {
                                OuxrefFields f = inv.getArgument(1);
                                f.setXrCustId(custId);
                            }
                            return null;
                        })
                .when(appService)
                .readFile(eq("XREFFILE"), any(), anyString(), anyInt());
    }

    private void givenCustomerRead(int resp) {
        doAnswer(
                        inv -> {
                            eibresp.set(resp);
                            return null;
                        })
                .when(appService)
                .readFile(eq("CUSTFILE"), any(), anyString(), anyInt());
    }

    private void givenXrefReadForUpdate(int resp) {
        doAnswer(
                        inv -> {
                            eibresp.set(resp);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq("XREFFILE"), any(), anyString(), anyInt());
    }

    private void givenXrefRewrite(int resp) {
        doAnswer(
                        inv -> {
                            eibresp.set(resp);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq("XREFFILE"), any());
    }

    private void givenXrefWrite(int resp) {
        doAnswer(
                        inv -> {
                            eibresp.set(resp);
                            return null;
                        })
                .when(appService)
                .writeFile(eq("XREFFILE"), any(), anyString(), anyInt());
    }

    /**
     * Wires ACCTFILE / XREFFILE(read) / XREFFILE(update) / rewrite all to NORMAL success, so a card
     * sails through validation and (in REBL mode) the rewrite path.
     */
    private void givenFullCardSuccessRebuild() {
        givenAccountRead(0);
        givenXrefRead(0, 555);
        givenCustomerRead(0);
        givenXrefReadForUpdate(0);
        givenXrefRewrite(0);
    }

    private void givenFullCardSuccessValidateOnly() {
        givenAccountRead(0);
        givenXrefRead(0, 555);
        givenCustomerRead(0);
    }

    // ---------------------------------------------------------------
    // 1000-INIT — mode validation.
    // ---------------------------------------------------------------

    @Test
    void mainLine_invalidMode_setsStatus99AndSkipsAllProcessing() {
        givenRequest("XXXX", 0, "");

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxStatus()).isEqualTo("99");
        assertThat(out.getKuxMsg().trim()).isEqualTo("INVALID MODE - USE REBL OR VALD");
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
        verify(appService, never()).readNext(anyString(), any());
    }

    // ---------------------------------------------------------------
    // 2000-POSITION — STARTBR RESP handling.
    // ---------------------------------------------------------------

    @Test
    void mainLine_startBrowseNotFound_noCardsProcessed_setsStatus10() {
        givenRequest("REBL", 0, "");
        givenStartBrowseResp(13);

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxStatus()).isEqualTo("10");
        assertThat(out.getKuxMsg().trim()).isEqualTo("NO CARDS PROCESSED");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startBrowseEndfile_noCardsProcessed_setsStatus10() {
        givenRequest("REBL", 0, "");
        givenStartBrowseResp(20);

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxStatus()).isEqualTo("10");
        assertThat(out.getKuxMsg().trim()).isEqualTo("NO CARDS PROCESSED");
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startBrowseOtherResp_setsStatus99CardfileStartbrFailed() {
        givenRequest("REBL", 0, "");
        givenStartBrowseResp(99);

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxStatus()).isEqualTo("99");
        assertThat(out.getKuxMsg().trim()).isEqualTo("CARDFILE STARTBR FAILED");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startCardBlank_positionsBrowseAtLowValues() {
        givenRequest("REBL", 0, "                ");
        givenStartBrowseResp(20);
        ArgumentCaptor<String> ridfld = ArgumentCaptor.forClass(String.class);

        runAndCaptureResponse();

        verify(appService).startBrowse(eq("CARDFILE"), ridfld.capture(), eq(0));
        String key = ridfld.getValue();
        assertThat(key).hasSize(16);
        assertThat(key.chars().allMatch(c -> c == 0)).as("CD-NUM filled with LOW-VALUES").isTrue();
    }

    @Test
    void mainLine_startCardProvided_positionsBrowseAtGivenKey() {
        givenRequest("REBL", 0, "0000000012345678");
        givenStartBrowseResp(20);

        runAndCaptureResponse();

        verify(appService).startBrowse(eq("CARDFILE"), eq("0000000012345678"), eq(0));
    }

    // ---------------------------------------------------------------
    // 3200/3300 — VAL-ACCT branch handling.
    // ---------------------------------------------------------------

    @Test
    void mainLine_accountNotFound_skipsCardIncrementsSkipAcct() {
        givenRequest("REBL", 0, "");
        givenStartBrowseResp(0);
        givenCardSequence(List.of(card("0000000000001001", 100L)));
        givenAccountRead(13);

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxRead()).isEqualTo(1);
        assertThat(out.getKuxSkipAcct()).isEqualTo(1);
        assertThat(out.getKuxErrors()).isEqualTo(0);
        assertThat(out.getKuxUpdated()).isEqualTo(0);
        verify(appService, never()).readFile(eq("XREFFILE"), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_accountReadOtherError_incrementsErrorsSkipsCard() {
        givenRequest("REBL", 0, "");
        givenStartBrowseResp(0);
        givenCardSequence(List.of(card("0000000000001001", 100L)));
        givenAccountRead(99);

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxErrors()).isEqualTo(1);
        assertThat(out.getKuxSkipAcct()).isEqualTo(0);
        assertThat(out.getKuxUpdated()).isEqualTo(0);
    }

    // ---------------------------------------------------------------
    // 3400 — READ-XREF branch handling.
    // ---------------------------------------------------------------

    @Test
    void mainLine_xrefNotFound_skipsCardIncrementsSkipXref() {
        givenRequest("REBL", 0, "");
        givenStartBrowseResp(0);
        givenCardSequence(List.of(card("0000000000001001", 100L)));
        givenAccountRead(0);
        givenXrefRead(13, null);

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxSkipXref()).isEqualTo(1);
        assertThat(out.getKuxErrors()).isEqualTo(0);
        verify(appService, never()).readFile(eq("CUSTFILE"), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_xrefReadOtherError_incrementsErrors() {
        givenRequest("REBL", 0, "");
        givenStartBrowseResp(0);
        givenCardSequence(List.of(card("0000000000001001", 100L)));
        givenAccountRead(0);
        givenXrefRead(99, null);

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxErrors()).isEqualTo(1);
        assertThat(out.getKuxSkipXref()).isEqualTo(0);
    }

    // ---------------------------------------------------------------
    // 3500 — VAL-CUST branch handling.
    // ---------------------------------------------------------------

    @Test
    void mainLine_customerNotFound_skipsCardIncrementsSkipCust() {
        givenRequest("REBL", 0, "");
        givenStartBrowseResp(0);
        givenCardSequence(List.of(card("0000000000001001", 100L)));
        givenAccountRead(0);
        givenXrefRead(0, 555);
        givenCustomerRead(13);

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxSkipCust()).isEqualTo(1);
        assertThat(out.getKuxErrors()).isEqualTo(0);
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_customerReadOtherError_incrementsErrors() {
        givenRequest("REBL", 0, "");
        givenStartBrowseResp(0);
        givenCardSequence(List.of(card("0000000000001001", 100L)));
        givenAccountRead(0);
        givenXrefRead(0, 555);
        givenCustomerRead(99);

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxErrors()).isEqualTo(1);
        assertThat(out.getKuxSkipCust()).isEqualTo(0);
    }

    // ---------------------------------------------------------------
    // 3600/3700/3800 — APPLY / REWRITE-XREF / WRITE-XREF.
    // ---------------------------------------------------------------

    @Test
    void mainLine_modeValidate_singleCard_incrementsUpdatedWithoutRewrite() {
        givenRequest("VALD", 0, "");
        givenStartBrowseResp(0);
        givenCardSequence(List.of(card("0000000000001001", 100L)));
        givenFullCardSuccessValidateOnly();

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxUpdated()).isEqualTo(1);
        assertThat(out.getKuxStatus()).isEqualTo("00");
        assertThat(out.getKuxMsg().trim()).isEqualTo("XREF VALIDATION COMPLETE");
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
        verify(appService, never()).rewriteFile(anyString(), any());
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_modeRebuild_singleCard_rewritesExistingXrefIncrementsUpdated() {
        givenRequest("REBL", 0, "");
        givenStartBrowseResp(0);
        givenCardSequence(List.of(card("0000000000001001", 100L)));
        givenFullCardSuccessRebuild();

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxRead()).isEqualTo(1);
        assertThat(out.getKuxUpdated()).isEqualTo(1);
        assertThat(out.getKuxWritten()).isEqualTo(0);
        assertThat(out.getKuxErrors()).isEqualTo(0);
        assertThat(out.getKuxStatus()).isEqualTo("00");
        assertThat(out.getKuxMsg().trim()).isEqualTo("XREF REBUILD COMPLETE");
        assertThat(out.getKuxMore()).isEqualTo("N");
        verify(appService).rewriteFile(eq("XREFFILE"), any());
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
        verify(appService).endBrowse(eq("CARDFILE"));
    }

    @Test
    void mainLine_rewriteReadForUpdateOtherError_incrementsErrorsNoWriteAttempt() {
        givenRequest("REBL", 0, "");
        givenStartBrowseResp(0);
        givenCardSequence(List.of(card("0000000000001001", 100L)));
        givenAccountRead(0);
        givenXrefRead(0, 555);
        givenCustomerRead(0);
        givenXrefReadForUpdate(99);

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxErrors()).isEqualTo(1);
        assertThat(out.getKuxUpdated()).isEqualTo(0);
        verify(appService, never()).rewriteFile(anyString(), any());
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_rewriteFails_incrementsErrorsNotUpdated() {
        givenRequest("REBL", 0, "");
        givenStartBrowseResp(0);
        givenCardSequence(List.of(card("0000000000001001", 100L)));
        givenAccountRead(0);
        givenXrefRead(0, 555);
        givenCustomerRead(0);
        givenXrefReadForUpdate(0);
        givenXrefRewrite(99);

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxErrors()).isEqualTo(1);
        assertThat(out.getKuxUpdated()).isEqualTo(0);
    }

    @Test
    void mainLine_rewriteRecordMissing_writesFreshXrefRecord() {
        givenRequest("REBL", 0, "");
        givenStartBrowseResp(0);
        givenCardSequence(List.of(card("0000000000001001", 100L)));
        givenAccountRead(0);
        givenXrefRead(0, 555);
        givenCustomerRead(0);
        givenXrefReadForUpdate(13);
        givenXrefWrite(0);

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxWritten()).isEqualTo(1);
        assertThat(out.getKuxUpdated()).isEqualTo(0);
        assertThat(out.getKuxErrors()).isEqualTo(0);
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void mainLine_writeXrefFails_incrementsErrorsNotWritten() {
        givenRequest("REBL", 0, "");
        givenStartBrowseResp(0);
        givenCardSequence(List.of(card("0000000000001001", 100L)));
        givenAccountRead(0);
        givenXrefRead(0, 555);
        givenCustomerRead(0);
        givenXrefReadForUpdate(13);
        givenXrefWrite(99);

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxErrors()).isEqualTo(1);
        assertThat(out.getKuxWritten()).isEqualTo(0);
    }

    // ---------------------------------------------------------------
    // 3100-READ-CARD — READNEXT RESP handling mid-loop.
    // ---------------------------------------------------------------

    @Test
    void mainLine_readNextOtherError_stopsLoopIncrementsErrors_finalMsgIsNoCardsProcessed() {
        givenRequest("REBL", 0, "");
        givenStartBrowseResp(0);
        doAnswer(
                        inv -> {
                            eibresp.set(99);
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());

        OuxrefFields out = runAndCaptureResponse();

        // COBOL 6000-SET-STATUS overwrites KUX-MSG whenever KUX-READ = ZEROS, regardless of
        // the earlier "CARDFILE READNEXT FAILED" message set in 3100-READ-CARD — this is the
        // COBOL's own behavior (identical logic in the original), not a convert gap.
        assertThat(out.getKuxRead()).isEqualTo(0);
        assertThat(out.getKuxErrors()).isEqualTo(1);
        assertThat(out.getKuxStatus()).isEqualTo("10");
        assertThat(out.getKuxMsg().trim()).isEqualTo("NO CARDS PROCESSED");
    }

    // ---------------------------------------------------------------
    // WS-MAX cap + 4000-PEEK-NEXT.
    // ---------------------------------------------------------------

    @Test
    void mainLine_maxCapReached_stopsLoopAndPeeksNextCard() {
        givenRequest("VALD", 2, "");
        givenStartBrowseResp(0);
        givenCardSequence(
                List.of(
                        card("0000000000001001", 100L),
                        card("0000000000001002", 200L),
                        card("0000000000001003", 300L)));
        givenFullCardSuccessValidateOnly();

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxRead()).isEqualTo(2);
        assertThat(out.getKuxUpdated()).isEqualTo(2);
        assertThat(out.getKuxMore()).isEqualTo("Y");
        assertThat(out.getKuxNextCard().trim()).isEqualTo("0000000000001003");
        verify(appService, times(3)).readNext(anyString(), any());
        verify(appService).endBrowse(eq("CARDFILE"));
    }

    @Test
    void mainLine_maxCapReached_peekHitsEndOfFile_setsMoreN() {
        givenRequest("VALD", 1, "");
        givenStartBrowseResp(0);
        givenCardSequence(List.of(card("0000000000001001", 100L)));
        givenFullCardSuccessValidateOnly();

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxRead()).isEqualTo(1);
        assertThat(out.getKuxMore()).isEqualTo("N");
        assertThat(out.getKuxNextCard().trim()).isEmpty();
    }

    @Test
    void mainLine_maxCapReached_peekOtherError_setsMoreNIncrementsErrors() {
        givenRequest("VALD", 1, "");
        givenStartBrowseResp(0);
        givenAccountRead(0);
        givenXrefRead(0, 555);
        givenCustomerRead(0);
        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            if (callCount.incrementAndGet() == 1) {
                                OuxrefFields f = inv.getArgument(1);
                                f.setCdNum("0000000000001001");
                                f.setCdAcctId(100L);
                                eibresp.set(0);
                            } else {
                                eibresp.set(77);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());

        OuxrefFields out = runAndCaptureResponse();

        assertThat(out.getKuxRead()).isEqualTo(1);
        assertThat(out.getKuxMore()).isEqualTo("N");
        assertThat(out.getKuxErrors()).isEqualTo(1);
    }

    // ---------------------------------------------------------------
    // Trivial delegate methods.
    // ---------------------------------------------------------------

    @Test
    void getProgramName_returnsOuxref() {
        assertThat(service.getProgramName()).isEqualTo("OUXREF");
    }

    @Test
    void getButtonDefs_delegatesToMetadata_returnsEmptyList() {
        assertThat(service.getButtonDefs()).isEmpty();
    }

    @Test
    void registerFsetFields_delegatesToMetadata_doesNotThrow() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.registerFsetFields(null));
    }
}

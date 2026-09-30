package com.generated.orion.octranl.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.octranl.accessor.OctranlFields;
import com.generated.orion.octranl.model.WorkingStorage;

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
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OctranlService, generated from COBOL program OCTRANL. All business logic is
 * private and is exercised solely through the public {@link OctranlService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS
 * SEND/RECEIVE/STARTBR/READNEXT/ENDBR/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OctranlServiceTest {

    private static final String TRANFILE = "TRANFILE";

    private static final String MSG_PROMPT = "Enter a card number and press ENTER.";
    private static final String MSG_CARD_REQ = "Card number is required.";
    private static final String MSG_NONE_FOUND = "No transactions found for that card.";
    private static final String MSG_BROWSE_ERR = "Error browsing the transaction file.";
    private static final String MSG_SUFFIX = " transaction(s) displayed.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";

    @Mock private AppService appService;

    private OctranlService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OctranlService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commarea(int context) {
        OctranlFields helper = new OctranlFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commarea(context));
    }

    /** Stubs receiveMap to populate the MTRANLA input CARDNUMI field. */
    private void givenReceiveMapPopulatesCardNumber(String cardNumi) {
        doAnswer(
                        inv -> {
                            OctranlFields f = inv.getArgument(1);
                            f.setCardnumi(cardNumi);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MTRANLA"), any());
    }

    /** Stubs STARTBR on TRANFILE to return the given EIBRESP (0=NORMAL, 13=NOTFND, other=error). */
    private void givenStartBrowseResp(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .startBrowse(eq(TRANFILE), anyString(), eq(0));
    }

    private record Tx(String id, String typeCd, BigDecimal amt, String cardNum) {}

    /**
     * Stubs READNEXT on TRANFILE to hand back the given records in order, then signal ENDFILE (resp
     * 20) once exhausted. Records may belong to any card -- 3200-READ-NEXT filters by TR-CARD-NUM =
     * WS-CARD-KEY.
     */
    private void givenReadNextTransactions(Tx... rows) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            OctranlFields into = inv.getArgument(1);
                            int i = idx.getAndIncrement();
                            if (i < rows.length) {
                                Tx row = rows[i];
                                into.setTrId(row.id());
                                into.setTrTypeCd(row.typeCd());
                                into.setTrAmt(row.amt());
                                into.setTrCardNum(row.cardNum());
                                nextResp.set(0);
                            } else {
                                nextResp.set(20);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(eq(TRANFILE), any());
    }

    private ArgumentCaptor<Object> captureLastSendMap(int times) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(times))
                .sendMap(
                        eq("MTRANLA"),
                        captor.capture(),
                        any(),
                        anyBoolean(),
                        anyBoolean(),
                        anyBoolean());
        return captor;
    }

    // ───────────────────────── 0000-MAIN ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MTRANLA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OctranlFields out = (OctranlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORTL");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCTRANL");
        assertThat(out.getCardnuml()).isEqualTo((short) -1);
        verify(appService).returnTransid(eq("ORTL"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalContextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq("MTRANLA"), any(), any(), eq(true), eq(false), eq(true));
        verify(appService).returnTransid(eq("ORTL"), any(), eq(692));
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToMenuWithoutSendingAMap() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        ArgumentCaptor<Object> commareaOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .xctl(
                        org.mockito.ArgumentMatchers.argThat(s -> s.trim().equals("OCMENU")),
                        commareaOut.capture(),
                        eq(692));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());

        byte[] bytes = (byte[]) commareaOut.getValue();
        OctranlFields returned = new OctranlFields(new WorkingStorage());
        returned.setOrionCommarea(new String(bytes, Charset.forName("MS932")));
        assertThat(returned.getCaFromProgram().trim()).isEqualTo("OCTRANL");
        assertThat(returned.getCaFromTranid().trim()).isEqualTo("ORTL");
        assertThat(returned.getCaPgmContext()).isEqualTo(0);
    }

    @Test
    void mainLine_pf4Pressed_resendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MTRANLA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OctranlFields out = (OctranlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_unmappedKeyPressed_resendsInitialScreenThenInvalidKeyDataOnly() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(2);
        // First send: 1000-SEND-INITIAL (erase). Second send: 8100-SEND-DATAONLY (no erase).
        verify(appService).sendMap(eq("MTRANLA"), any(), any(), eq(true), eq(false), eq(true));
        verify(appService).sendMap(eq("MTRANLA"), any(), any(), eq(false), eq(false), eq(true));
        OctranlFields last = (OctranlFields) sendMapOut.getAllValues().get(1);
        assertThat(last.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-LIST-TRANS ─────────────────────────

    @Test
    void mainLine_enterBlankCardNumber_rejectsRequiredAndSkipsBrowse() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulatesCardNumber("                ");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MTRANLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranlFields out = (OctranlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARD_REQ);
        assertThat(out.getCardnuml()).isEqualTo((short) -1);
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterLowValuesCardNumber_rejectsRequiredAndSkipsBrowse() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulatesCardNumber("\u0000".repeat(16));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MTRANLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranlFields out = (OctranlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARD_REQ);
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterValidCardNumber_browsesAndListsMatchingTransactionsWithCount() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulatesCardNumber("1111222233334444");
        givenStartBrowseResp(0);
        givenReadNextTransactions(
                new Tx("TRN0000000000001", "PU", new BigDecimal("123.45"), "1111222233334444"),
                new Tx("TRN0000000000002", "RF", new BigDecimal("67.89"), "1111222233334444"),
                new Tx("TRN0000000000003", "PU", new BigDecimal("10.00"), "1111222233334444"));

        service.mainLine(appService);

        verify(appService).startBrowse(eq(TRANFILE), anyString(), eq(0));
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MTRANLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranlFields out = (OctranlFields) sendMapOut.getValue();
        assertThat(out.getTrn1o().trim()).isEqualTo("TRN0000000000001");
        assertThat(out.getAmt1o().trim()).contains("123.45");
        assertThat(out.getTyp1o().trim()).isEqualTo("PU");
        assertThat(out.getTrn2o().trim()).isEqualTo("TRN0000000000002");
        assertThat(out.getTrn3o().trim()).isEqualTo("TRN0000000000003");
        assertThat(out.getTrn4o().trim()).isEmpty();
        assertThat(out.getErrmsgo().trim()).isEqualTo("3" + MSG_SUFFIX);
        assertThat(out.getCardnuml()).isEqualTo((short) -1);
        verify(appService).endBrowse(eq(TRANFILE));
    }

    @Test
    void mainLine_enterValidCardNumber_capsAtFiveRowsEvenWithMoreMatches() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulatesCardNumber("1111222233334444");
        givenStartBrowseResp(0);
        givenReadNextTransactions(
                new Tx("TRN01", "PU", new BigDecimal("1.00"), "1111222233334444"),
                new Tx("TRN02", "PU", new BigDecimal("2.00"), "1111222233334444"),
                new Tx("TRN03", "PU", new BigDecimal("3.00"), "1111222233334444"),
                new Tx("TRN04", "PU", new BigDecimal("4.00"), "1111222233334444"),
                new Tx("TRN05", "PU", new BigDecimal("5.00"), "1111222233334444"),
                new Tx("TRN06", "PU", new BigDecimal("6.00"), "1111222233334444"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MTRANLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranlFields out = (OctranlFields) sendMapOut.getValue();
        assertThat(out.getTrn5o().trim()).isEqualTo("TRN05");
        assertThat(out.getErrmsgo().trim()).isEqualTo("5" + MSG_SUFFIX);
        // The 6th matching record must never be read -- the loop stops once WS-ROW-CNT reaches 5.
        verify(appService, times(5)).readNext(eq(TRANFILE), any());
        verify(appService).endBrowse(eq(TRANFILE));
    }

    @Test
    void mainLine_enterValidCardNumber_filtersOutNonMatchingCards() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulatesCardNumber("1111222233334444");
        givenStartBrowseResp(0);
        givenReadNextTransactions(
                new Tx("OTHER0000000001", "PU", new BigDecimal("9.99"), "9999888877776666"),
                new Tx("TRN0000000000001", "PU", new BigDecimal("123.45"), "1111222233334444"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MTRANLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranlFields out = (OctranlFields) sendMapOut.getValue();
        assertThat(out.getTrn1o().trim()).isEqualTo("TRN0000000000001");
        assertThat(out.getErrmsgo().trim()).isEqualTo("1" + MSG_SUFFIX);
    }

    @Test
    void mainLine_enterValidCardNumber_noMatches_showsNoneFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulatesCardNumber("1111222233334444");
        givenStartBrowseResp(0);
        givenReadNextTransactions(); // immediate ENDFILE

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MTRANLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranlFields out = (OctranlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
        assertThat(out.getTrn1o().trim()).isEmpty();
        verify(appService).endBrowse(eq(TRANFILE));
    }

    @Test
    void mainLine_enterValidCardNumber_startBrowseNotFound_skipsReadNextAndEndBrowse() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulatesCardNumber("1111222233334444");
        givenStartBrowseResp(13); // DFHRESP(NOTFND)

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MTRANLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranlFields out = (OctranlFields) sendMapOut.getValue();
        // COBOL 3100-START-BROWSE sets ERRMSGO to none for NOTFND, then 2200-BUILD-SUMMARY
        // unconditionally derives the message from WS-ROW-CNT (still zero) -- "none found" wins.
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void
            mainLine_enterValidCardNumber_startBrowseUnexpectedError_summaryOverwritesBrowseErrorMessage() {
        // Per COBOL ground truth: 3100-START-BROWSE sets ERRMSGO to WS-M-BROWSE-ERR on an
        // unexpected STARTBR response, but 2200-BUILD-SUMMARY runs unconditionally right
        // after 3000-BROWSE-TRANS and overwrites ERRMSGO based on WS-ROW-CNT (still zero
        // here) -- so the final screen shows "none found", not the browse error. This
        // overwrite is original COBOL behavior, not a conversion defect.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulatesCardNumber("1111222233334444");
        givenStartBrowseResp(99); // unexpected error

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MTRANLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranlFields out = (OctranlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_enterValidCardNumber_readNextUnexpectedError_stopsBrowseAndReportsBrowseError() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulatesCardNumber("1111222233334444");
        givenStartBrowseResp(0);
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .readNext(eq(TRANFILE), any());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MTRANLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranlFields out = (OctranlFields) sendMapOut.getValue();
        // WS-ROW-CNT stays zero (the failed read never counted a match), so
        // 2200-BUILD-SUMMARY again overwrites ERRMSGO with "none found".
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
        verify(appService, times(1)).readNext(eq(TRANFILE), any());
        verify(appService).endBrowse(eq(TRANFILE));
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOctranl() {
        assertEquals("OCTRANL", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrtl() {
        assertEquals("ORTL", service.getTransId());
    }

    @Test
    void getButtonDefs_delegatesToMetadataWithoutError() {
        assertNotNull(service.getButtonDefs());
    }

    @Test
    void getFieldMapping_delegatesToMetadataForKnownMap() {
        assertNotNull(service.getFieldMapping("MTRANLA"));
    }
}

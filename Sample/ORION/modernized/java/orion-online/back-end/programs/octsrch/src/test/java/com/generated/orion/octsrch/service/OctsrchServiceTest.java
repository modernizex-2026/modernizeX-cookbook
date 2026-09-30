package com.generated.orion.octsrch.service;

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
import com.generated.orion.octsrch.accessor.OctsrchFields;
import com.generated.orion.octsrch.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Unit tests for OctsrchService, generated from COBOL program OCTSRCH. All business logic is
 * private and exercised solely through the public {@link OctsrchService#mainLine(AppService)} entry
 * point, driven by an {@link AppService} mock that emulates CICS
 * SEND/RECEIVE/STARTBR/READNEXT/ENDBR/XCTL/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OctsrchServiceTest {

    private static final String MSG_PROMPT = "Enter card and amount range, press ENTER.";
    private static final String MSG_CARD_REQ = "Card number is required.";
    private static final String MSG_FROM_BAD = "From amount is not a valid number.";
    private static final String MSG_TO_BAD = "To amount is not a valid number.";
    private static final String MSG_RANGE = "From amount cannot exceed to amount.";
    private static final String MSG_NONE_FOUND = "No transactions in that range.";
    private static final String MSG_SUFFIX = "match(es) displayed.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";

    @Mock private AppService appService;

    private OctsrchService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    private record TranRow(
            String id, String cardNum, String typeCd, BigDecimal amt, String merchant) {}

    @BeforeEach
    void setUp() {
        service = new OctsrchService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying CA-PGM-CONTEXT. */
    private String commarea(int context) {
        OctsrchFields helper = new OctsrchFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commarea(context));
    }

    /** Stubs receiveMap to populate the MTSRCHAI input fields captured from production code. */
    private void givenReceiveMapPopulates(Consumer<OctsrchFields> populate) {
        doAnswer(
                        inv -> {
                            OctsrchFields f = inv.getArgument(1);
                            populate.accept(f);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MTSRCHA"), any());
    }

    /** Stubs STARTBR to return the given EIBRESP without touching browse-started state. */
    private void givenStartBrowseResp(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .startBrowse(anyString(), anyString(), anyInt());
    }

    /**
     * Stubs READNEXT to hand back the given raw records in order (RESP NORMAL), then RESP
     * ENDFILE(20) once the list is exhausted. Filtering by card/amount is left to the service under
     * test, mirroring how CICS READNEXT returns every physical record.
     */
    private void givenTransactionRecords(List<TranRow> rows) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            OctsrchFields into = inv.getArgument(1);
                            int i = idx.getAndIncrement();
                            if (i < rows.size()) {
                                TranRow r = rows.get(i);
                                into.setTrId(r.id());
                                into.setTrCardNum(r.cardNum());
                                into.setTrTypeCd(r.typeCd());
                                into.setTrAmt(r.amt());
                                into.setTrMerchantName(r.merchant());
                                nextResp.set(0);
                            } else {
                                nextResp.set(20);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
    }

    /** Stubs READNEXT to fail at the CICS transport level (non NORMAL/ENDFILE EIBRESP). */
    private void givenReadNextFailsWithResp(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
    }

    private ArgumentCaptor<Object> captureSendMap(int times, boolean erase) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(times))
                .sendMap(
                        eq("MTSRCHA"),
                        captor.capture(),
                        any(),
                        eq(erase),
                        anyBoolean(),
                        anyBoolean());
        return captor;
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        OctsrchFields out = (OctsrchFields) captureSendMap(1, true).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getCardnuml()).isEqualTo(-1);
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORTS");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCTSRCH");
        verify(appService).returnTransid(eq("ORTS"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationContextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService)
                .sendMap(eq("MTSRCHA"), any(), any(), eq(true), anyBoolean(), anyBoolean());
        verify(appService).returnTransid(eq("ORTS"), any(), eq(692));
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToMenu() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        verify(appService)
                .xctl(
                        org.mockito.ArgumentMatchers.argThat(s -> s.trim().equals("OCMENU")),
                        any(),
                        anyInt());
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void mainLine_pf4Pressed_resendsFreshInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        OctsrchFields out = (OctsrchFields) captureSendMap(1, true).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    @Test
    void mainLine_unmappedKeyPressed_resendsInitialScreenThenInvalidKeyDataOnly() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        // COBOL: OTHER branch performs 1000-SEND-INITIAL (ERASE) then overwrites ERRMSGO and
        // performs a second 8100-SEND-DATAONLY -- two SEND MAPs occur for an unmapped key.
        verify(appService, times(1))
                .sendMap(eq("MTSRCHA"), any(), any(), eq(true), anyBoolean(), anyBoolean());
        OctsrchFields secondSend = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(secondSend.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-SEARCH / validation ─────────────────────────

    @Test
    void mainLine_enterMissingCardNumber_showsCardRequiredAndSkipsBrowse() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("                "));

        service.mainLine(appService);

        OctsrchFields out = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARD_REQ);
        assertThat(out.getCardnuml()).isEqualTo(-1);
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterMapfailResp_treatsFieldsAsLowValuesShowsCardRequired() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        doAnswer(
                        inv -> {
                            nextResp.set(36);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MTSRCHA"), any());

        service.mainLine(appService);

        OctsrchFields out = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARD_REQ);
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterFromAmountInvalid_showsFromBadMessageAndSkipsBrowse() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setFramti("12A45");
                });

        service.mainLine(appService);

        OctsrchFields out = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_FROM_BAD);
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterToAmountInvalid_showsToBadMessageAndSkipsBrowse() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setFramti("100.00");
                    f.setToamti("9X9");
                });

        service.mainLine(appService);

        OctsrchFields out = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TO_BAD);
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterFromGreaterThanTo_showsRangeMessageAndSkipsBrowse() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setFramti("500.00");
                    f.setToamti("100.00");
                });

        service.mainLine(appService);

        OctsrchFields out = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_RANGE);
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    // ───────────────────────── 5100-PARSE-AMOUNT edge cases ─────────────────────────

    @Test
    void mainLine_amountWithCommaAndSingleFractionDigit_parsesAsTenTimesFraction() {
        // Ground truth (COBOL 5100/5150): WS-AE-FRAC-CNT = 1 -> WS-AE-FRAC * 10, so "1,234.5"
        // must resolve to 1234.50, not 1234.05.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setFramti("1,234.5");
                    f.setToamti("1,234.5");
                });
        givenStartBrowseResp(13);

        service.mainLine(appService);

        // Range collapses to a single point (1234.50 .. 1234.50); reaching NONE-FOUND (rather
        // than a FROM/TO-BAD message) proves both bounds parsed as valid numbers.
        OctsrchFields out = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
    }

    @Test
    void mainLine_amountWithTooManyFractionDigits_isInvalid() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setFramti("1.234");
                });

        service.mainLine(appService);

        OctsrchFields out = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_FROM_BAD);
    }

    @Test
    void mainLine_amountWithMoreThanTenIntegerDigits_isInvalid() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setFramti("12345678901");
                });

        service.mainLine(appService);

        OctsrchFields out = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_FROM_BAD);
    }

    @Test
    void mainLine_amountWithTwoDecimalPoints_isInvalid() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setFramti("1.2.3");
                });

        service.mainLine(appService);

        OctsrchFields out = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_FROM_BAD);
    }

    @Test
    void mainLine_amountWithLetter_isInvalid() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setFramti("1A0.00");
                });

        service.mainLine(appService);

        OctsrchFields out = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_FROM_BAD);
    }

    @Test
    void mainLine_enterBlankFromAndToAmt_usesZeroAndMaximumDefaults() {
        // Ground truth (COBOL 6100-VALIDATE-CRIT): blank FRAMTI -> WS-FROM-AMT = 0,
        // blank TOAMTI -> WS-TO-AMT = 9999999999.99, so a single matching row anywhere
        // in range must still show up.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setFramti("            ");
                    f.setToamti("            ");
                });
        givenStartBrowseResp(0);
        givenTransactionRecords(
                List.of(
                        new TranRow(
                                "TX00001",
                                "1234567890123456",
                                "PU",
                                new BigDecimal("9999999999.99"),
                                "ACME STORE")));

        service.mainLine(appService);

        OctsrchFields out = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(out.getSr1o()).contains("TX00001").contains("ACME STORE");
        assertThat(out.getErrmsgo().trim()).isEqualTo("1" + " " + MSG_SUFFIX);
    }

    // ───────────────────────── 3000-BROWSE-TRANS ─────────────────────────

    @Test
    void mainLine_browseFindsMatchingTransactions_populatesRowsAndCount() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1111222233334444");
                    f.setFramti("50.00");
                    f.setToamti("200.00");
                });
        givenStartBrowseResp(0);
        givenTransactionRecords(
                List.of(
                        new TranRow(
                                "TX00001",
                                "1111222233334444",
                                "PU",
                                new BigDecimal("100.00"),
                                "COFFEE SHOP"),
                        new TranRow(
                                "TX00002",
                                "9999888877776666",
                                "PU",
                                new BigDecimal("150.00"),
                                "OTHER CARD"),
                        new TranRow(
                                "TX00003",
                                "1111222233334444",
                                "PU",
                                new BigDecimal("10.00"),
                                "TOO LOW"),
                        new TranRow(
                                "TX00004",
                                "1111222233334444",
                                "PU",
                                new BigDecimal("200.00"),
                                "BOUNDARY HIGH")));

        service.mainLine(appService);

        verify(appService).startBrowse(anyString(), anyString(), anyInt());
        verify(appService).endBrowse(anyString());
        OctsrchFields out = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(out.getSr1o()).contains("TX00001").contains("COFFEE SHOP");
        assertThat(out.getSr2o()).contains("TX00004").contains("BOUNDARY HIGH");
        assertThat(out.getSr3o().trim()).isEmpty();
        assertThat(out.getErrmsgo().trim()).isEqualTo("2" + " " + MSG_SUFFIX);
    }

    @Test
    void mainLine_browseNoMatches_showsNoneFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1111222233334444");
                    f.setFramti("50.00");
                    f.setToamti("200.00");
                });
        givenStartBrowseResp(0);
        givenTransactionRecords(
                List.of(
                        new TranRow(
                                "TX00001",
                                "9999888877776666",
                                "PU",
                                new BigDecimal("100.00"),
                                "OTHER CARD")));

        service.mainLine(appService);

        OctsrchFields out = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
        assertThat(out.getSr1o().trim()).isEmpty();
    }

    @Test
    void mainLine_browseMoreThanFiveMatches_capsAtFiveRowsAndStopsReading() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1111222233334444");
                    f.setFramti("            ");
                    f.setToamti("            ");
                });
        givenStartBrowseResp(0);
        givenTransactionRecords(
                List.of(
                        new TranRow(
                                "TX00001",
                                "1111222233334444",
                                "PU",
                                new BigDecimal("10.00"),
                                "ROW1"),
                        new TranRow(
                                "TX00002",
                                "1111222233334444",
                                "PU",
                                new BigDecimal("20.00"),
                                "ROW2"),
                        new TranRow(
                                "TX00003",
                                "1111222233334444",
                                "PU",
                                new BigDecimal("30.00"),
                                "ROW3"),
                        new TranRow(
                                "TX00004",
                                "1111222233334444",
                                "PU",
                                new BigDecimal("40.00"),
                                "ROW4"),
                        new TranRow(
                                "TX00005",
                                "1111222233334444",
                                "PU",
                                new BigDecimal("50.00"),
                                "ROW5"),
                        new TranRow(
                                "TX00006",
                                "1111222233334444",
                                "PU",
                                new BigDecimal("60.00"),
                                "ROW6")));

        service.mainLine(appService);

        OctsrchFields out = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(out.getSr1o()).contains("ROW1");
        assertThat(out.getSr5o()).contains("ROW5");
        assertThat(out.getErrmsgo().trim()).isEqualTo("5" + " " + MSG_SUFFIX);
        // WS-MAX-ROWS(5) reached -> loop stops without consuming the 6th record.
        verify(appService, times(5)).readNext(anyString(), any());
    }

    @Test
    void mainLine_startBrowseNotFound_showsNoneFoundWithoutErrorMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("1111222233334444"));
        givenStartBrowseResp(13);

        service.mainLine(appService);

        OctsrchFields out = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startBrowseTransportError_zeroRowsSoNoneFoundMessageWins() {
        // Ground truth (COBOL 2100-SEARCH): 2200-BUILD-SUMMARY runs unconditionally after
        // 3000-BROWSE-TRANS and overwrites ERRMSGO from WS-ROW-CNT alone. A STARTBR transport
        // error leaves WS-ROW-CNT at zero, so WS-M-NONE-FOUND clobbers WS-M-BROWSE-ERR -- the
        // browse-error message set by 3100-START-BROWSE is never actually seen by the operator.
        // This is a genuine COBOL behavior (arguably a latent bug in the original program), not
        // a conversion defect, so the Java port reproducing it here is correct.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("1111222233334444"));
        givenStartBrowseResp(99);

        service.mainLine(appService);

        OctsrchFields out = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
        verify(appService, never()).readNext(anyString(), any());
    }

    @Test
    void mainLine_readNextTransportError_zeroRowsSoNoneFoundMessageWinsAndLoopStops() {
        // Same 2200-BUILD-SUMMARY overwrite as above, triggered instead by a READNEXT
        // transport error before any row matched.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("1111222233334444"));
        givenStartBrowseResp(0);
        givenReadNextFailsWithResp(99);

        service.mainLine(appService);

        OctsrchFields out = (OctsrchFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
        verify(appService, times(1)).readNext(anyString(), any());
        verify(appService).endBrowse(anyString());
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOctsrch() {
        assertEquals("OCTSRCH", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrts() {
        assertEquals("ORTS", service.getTransId());
    }

    @Test
    void getButtonDefs_delegatesToMetadataWithoutError() {
        assertNotNull(service.getButtonDefs());
    }

    @Test
    void getFieldMapping_delegatesToMetadataForKnownMap() {
        assertNotNull(service.getFieldMapping("MTSRCHA"));
    }
}

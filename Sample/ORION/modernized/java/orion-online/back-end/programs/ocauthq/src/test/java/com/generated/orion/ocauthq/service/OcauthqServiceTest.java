package com.generated.orion.ocauthq.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.ocauthq.accessor.OcauthqFields;
import com.generated.orion.ocauthq.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Unit tests for OcauthqService, generated from COBOL program OCAUTHQ (ORION-CCMS card
 * authorization via MQ, structured after the OCACCTV golden reference). All business logic is
 * private and is exercised solely through the public {@link OcauthqService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS
 * SEND/RECEIVE/LINK/XCTL/RETURN.
 *
 * <p>Convert-gap check: OCAUTHQ's Java service mirrors every COBOL paragraph 1:1
 * (0000-MAIN..9000-RETURN), including the AS-DECISION 88-level literals (AS-ERRORED = 'ERROR ') and
 * the WS-ED-BAL(2:15) -&gt; AVAILO reference modification (Java: padRight(...,16).substring(1,16));
 * no behavioral divergence was found against the COBOL ground truth, so no CONVERT-GAP test is
 * included here.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcauthqServiceTest {

    private static final String MAP_NAME = "MAUTHQA";
    private static final String MENU_PGM = "OCMENU";
    private static final String MQREQ_PGM = "OUMQREQ";

    private static final String MSG_PROMPT = "Enter card, amount, merchant; press ENTER.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_CARD_REQUIRED = "Card number is required.";
    private static final String MSG_AMOUNT_REQUIRED = "Amount is required.";
    private static final String MSG_AMOUNT_NOT_POSITIVE = "Amount must be greater than zero.";
    private static final String MSG_AUTH_COMPLETE = "Authorization complete. PF3=Back.";
    private static final String HDR_TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM";

    @Mock private AppService appService;

    private OcauthqService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OcauthqService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commareaWithContext(int context) {
        OcauthqFields helper = new OcauthqFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(context));
    }

    /** Stubs receiveMap to populate the MAUTHQAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(Consumer<OcauthqFields> populate) {
        doAnswer(
                        inv -> {
                            OcauthqFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    /**
     * Stubs the LINK to OUMQREQ so that its (mutated in place) commarea byte[] carries the given
     * AS-DECISION / AS-REASON / AS-AVAIL-CREDIT response and CA-ERR-FLG/CA-ERR-MSG, exactly as
     * production packs AUTH-MSG-AREA into CA-WORK-AREA before returning it.
     */
    private void givenLinkRespondsWith(
            String decision,
            String reason,
            BigDecimal availCredit,
            boolean caErrOn,
            String caErrMsg) {
        doAnswer(
                        inv -> {
                            byte[] commarea = inv.getArgument(1);
                            OcauthqFields decoded = new OcauthqFields(new WorkingStorage());
                            decoded.writeBytes("ORION-COMMAREA", commarea);
                            decoded.setAsDecision(decision);
                            decoded.setAsReason(reason);
                            decoded.setAsAvailCredit(availCredit);
                            decoded.setCaWorkArea(decoded.getAuthMsgArea());
                            decoded.setCaErrFlg(caErrOn ? "Y" : "N");
                            decoded.setCaErrMsg(caErrMsg == null ? "" : caErrMsg);
                            byte[] updated = decoded.sliceBytes("ORION-COMMAREA");
                            System.arraycopy(
                                    updated,
                                    0,
                                    commarea,
                                    0,
                                    Math.min(updated.length, commarea.length));
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .link(argThat(s -> s != null && s.trim().equals(MQREQ_PGM)), any(), anyInt());
    }

    /**
     * Stubs the LINK to OUMQREQ to simulate a CICS failure (non-zero EIBRESP), untouched commarea.
     */
    private void givenLinkFails(int respCode) {
        doAnswer(
                        inv -> {
                            nextResp.set(respCode);
                            return null;
                        })
                .when(appService)
                .link(argThat(s -> s != null && s.trim().equals(MQREQ_PGM)), any(), anyInt());
    }

    /**
     * Replicates COBOL's "MOVE AS-AVAIL-CREDIT TO WS-ED-BAL; MOVE WS-ED-BAL(2:15) TO AVAILO" MOVE
     * chain (2400-SHOW-DECISION) on an isolated accessor, so the expected string is derived the
     * same way production computes it (drop the leading sign position of the 16-byte edited
     * WS-ED-BAL into the 15-byte AVAILO screen field). Ground truth, not a convert gap.
     */
    private String editedAvail(BigDecimal amount) {
        OcauthqFields helper = new OcauthqFields(new WorkingStorage());
        helper.setWsEdBal(amount);
        return Utility.padRight(String.valueOf(helper.getString("WS-ED-BAL")), 16).substring(1, 16);
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcauthqFields out = (OcauthqFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORAQ");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCAUTHQ");
        assertThat(out.getTitleo().trim()).isEqualTo(HDR_TITLE);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-22");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        verify(appService).returnTransid(eq("ORAQ"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(false));
        verify(appService).returnTransid(eq("ORAQ"), any(), eq(692));
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToMenuWithFromProgramInfo() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        ArgumentCaptor<Object> commareaCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .xctl(argThat(s -> s.trim().equals(MENU_PGM)), commareaCaptor.capture(), eq(692));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());

        OcauthqFields decoded = new OcauthqFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo("OCAUTHQ");
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo("ORAQ");
        assertEquals(0, decoded.getCaPgmContext());
    }

    @Test
    void mainLine_pf4Pressed_clearsAndResendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcauthqFields out = (OcauthqFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        // 2000-PROCESS-INPUT's WHEN OTHER performs 1000-SEND-INITIAL (erase=true) then
        // 8100-SEND-DATAONLY (erase=false); both calls share the same mutable field
        // accessor, so only the final (dataonly) call's content can be inspected.
        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(false));
        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME),
                        dataOnlyOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcauthqFields dataOnlyScreen = (OcauthqFields) dataOnlyOut.getValue();
        assertThat(dataOnlyScreen.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-AUTHORIZE / 2200-VALIDATE-INPUT ─────────────────────────

    @Test
    void mainLine_enterKey_cardBlank_showsCardRequiredAndClearsDecisionFields() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("                "); // 16 spaces
                    f.setAmounti("150.00      ");
                    f.setMerchi("ACME STORE          ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcauthqFields out = (OcauthqFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARD_REQUIRED);
        assertThat(out.getDecisno().trim()).isEmpty();
        assertThat(out.getReasono().trim()).isEmpty();
        assertThat(out.getAvailo().trim()).isEmpty();
        verify(appService, never()).link(anyString(), any(), anyInt());
    }

    @Test
    void mainLine_enterKey_cardAllLowValues_showsCardRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.fillLowValues("CARDNUMI");
                    f.setAmounti("150.00      ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcauthqFields out = (OcauthqFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARD_REQUIRED);
        verify(appService, never()).link(anyString(), any(), anyInt());
    }

    @Test
    void mainLine_enterKey_amountBlank_showsAmountRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("4111111111111111");
                    f.setAmounti("            "); // 12 spaces
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcauthqFields out = (OcauthqFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_AMOUNT_REQUIRED);
        verify(appService, never()).link(anyString(), any(), anyInt());
    }

    @Test
    void mainLine_enterKey_amountZero_showsAmountMustBeGreaterThanZeroMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("4111111111111111");
                    f.setAmounti("0.00        ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcauthqFields out = (OcauthqFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_AMOUNT_NOT_POSITIVE);
        verify(appService, never()).link(anyString(), any(), anyInt());
    }

    @Test
    void
            mainLine_enterKey_amountNonNumeric_parsesAsZeroAndShowsAmountMustBeGreaterThanZeroMessage() {
        // FUNCTION NUMVAL(WS-IN-AMT) on garbage input; Utility.parseNumeric mirrors this by
        // falling back to ZERO on NumberFormatException, so the outcome matches COBOL's
        // "amount <= zero" branch rather than raising an exception.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("4111111111111111");
                    f.setAmounti("ABCDEFGHIJKL");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcauthqFields out = (OcauthqFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_AMOUNT_NOT_POSITIVE);
    }

    // ───────────────────────── 2300-CALL-MQREQ / 2400-SHOW-DECISION ─────────────────────────

    @Test
    void mainLine_enterKey_validInputApproved_populatesDecisionAndAvailableBalance() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("4111111111111111");
                    f.setAmounti("150.00      ");
                    f.setMerchi("ACME STORE          ");
                });
        BigDecimal availCredit = new BigDecimal("4850.00");
        givenLinkRespondsWith("APPROVED", "OK                  ", availCredit, false, null);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcauthqFields out = (OcauthqFields) sendMapOut.getValue();
        assertThat(out.getCardnumo().trim()).isEqualTo("4111111111111111");
        assertThat(out.getMercho().trim()).isEqualTo("ACME STORE");
        assertThat(out.getDecisno().trim()).isEqualTo("APPROVED");
        assertThat(out.getReasono().trim()).isEqualTo("OK");
        assertThat(out.getAvailo()).isEqualTo(editedAvail(availCredit));
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_AUTH_COMPLETE);
    }

    @Test
    void
            mainLine_enterKey_validInputDeclinedWithCaErrorFlag_showsCaErrMsgInsteadOfCompleteMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("4111111111111111");
                    f.setAmounti("9999.00     ");
                    f.setMerchi("ACME STORE          ");
                });
        BigDecimal availCredit = BigDecimal.ZERO;
        String caErrMsg = "Credit limit exceeded";
        givenLinkRespondsWith("DECLINED", "INSUFFICIENT CREDIT ", availCredit, true, caErrMsg);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcauthqFields out = (OcauthqFields) sendMapOut.getValue();
        assertThat(out.getDecisno().trim()).isEqualTo("DECLINED");
        assertThat(out.getReasono().trim()).isEqualTo("INSUFFICIENT CREDIT");
        assertThat(out.getErrmsgo().trim()).isEqualTo(caErrMsg);
    }

    @Test
    void mainLine_enterKey_linkToMqreqFails_showsErrorDecisionAndReason() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("4111111111111111");
                    f.setAmounti("150.00      ");
                    f.setMerchi("ACME STORE          ");
                });
        givenLinkFails(12); // any non-zero EIBRESP

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcauthqFields out = (OcauthqFields) sendMapOut.getValue();
        assertThat(out.getDecisno().trim()).isEqualTo("ERROR");
        assertThat(out.getReasono().trim()).isEqualTo("LINK OUMQREQ FAILED");
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcauthq() {
        assertEquals("OCAUTHQ", service.getProgramName());
    }

    @Test
    void getTransId_returnsOraq() {
        assertEquals("ORAQ", service.getTransId());
    }
}

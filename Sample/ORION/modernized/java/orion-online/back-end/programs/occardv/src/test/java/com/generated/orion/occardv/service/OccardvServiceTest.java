package com.generated.orion.occardv.service;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.occardv.accessor.OccardvFields;
import com.generated.orion.occardv.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OccardvService, generated from COBOL program OCCARDV. All business logic is
 * private and is exercised solely through the public {@link OccardvService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS SEND/RECEIVE/READ/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OccardvServiceTest {

    private static final String CARDFILE = "CARDFILE";
    private static final String MAP = "MCARDVA";

    private static final String MSG_PROMPT = "Enter card number and press ENTER.";
    private static final String MSG_VIEW_SELECTED = "Press ENTER to view the selected card.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_REQUIRED = "Please enter all required fields.";
    private static final String MSG_CARDNUM_16 = "Card number must be exactly 16 digits.";
    private static final String MSG_NOTFND = "Record not found.";
    private static final String MSG_DISPLAYED = "Card displayed.";

    private static final String VALID_CARD_NUM = "1111222233334444";

    @Mock private AppService appService;

    private OccardvService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OccardvService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-23", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying CA-PGM-CONTEXT/CA-CARD-NUM. */
    private String commarea(int context, String cardNum) {
        OccardvFields helper = new OccardvFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        if (cardNum != null) {
            helper.setCaCardNum(cardNum);
        }
        return helper.getOrionCommarea();
    }

    private void givenPseudoConversation(int context) {
        givenPseudoConversation(context, null);
    }

    private void givenPseudoConversation(int context, String cardNum) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commarea(context, cardNum));
    }

    /** Stubs receiveMap to populate the MCARDVAI input fields captured from production code. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OccardvFields> populate) {
        doAnswer(
                        inv -> {
                            OccardvFields f = inv.getArgument(1);
                            populate.accept(f);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP), any());
    }

    /**
     * Stubs receiveMap to simulate DFHRESP(MAPFAIL) — production code then wipes MCARDVAI to
     * low-values.
     */
    private void givenReceiveMapMapfails(java.util.function.Consumer<OccardvFields> populate) {
        doAnswer(
                        inv -> {
                            OccardvFields f = inv.getArgument(1);
                            populate.accept(f);
                            nextResp.set(36);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP), any());
    }

    /**
     * Stubs a READ (readFile) on CARDFILE to return the given EIBRESP (0=NORMAL, 13=NOTFND,
     * other=error).
     */
    private void givenReadFileResp(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .readFile(eq(CARDFILE), any(), anyString(), eq(0));
    }

    private OccardvFields captureLastSendMapDataOnly() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP), captor.capture(), any(), eq(false), eq(false), eq(false));
        return (OccardvFields) captor.getValue();
    }

    private OccardvFields captureLastSendMapErase() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP), captor.capture(), any(), eq(true), eq(false), eq(false));
        return (OccardvFields) captor.getValue();
    }

    private void givenValidCardEntered() {
        givenReceiveMapPopulates(f -> f.setCardnumi(VALID_CARD_NUM));
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPromptAndSetsContext() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        OccardvFields out = captureLastSendMapErase();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORCV");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCCARDV");
        verify(appService).returnTransid(eq("ORCV"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationContextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP), any(), any(), eq(true), eq(false), eq(false));
    }

    @Test
    void mainLine_initialScreenWithExistingCardNumber_promptsViewSelectedCard() {
        givenPseudoConversation(1, VALID_CARD_NUM);
        when(appService.getEibaid()).thenReturn("4"); // PF4 -> resend initial screen

        service.mainLine(appService);

        OccardvFields out = captureLastSendMapErase();
        assertThat(out.getCardnumo().trim()).isEqualTo(VALID_CARD_NUM);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_VIEW_SELECTED);
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
                        eq(692));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void mainLine_pf12Pressed_transfersControlToMenu() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("@");

        service.mainLine(appService);

        verify(appService)
                .xctl(
                        org.mockito.ArgumentMatchers.argThat(s -> s.trim().equals("OCMENU")),
                        any(),
                        eq(692));
    }

    @Test
    void mainLine_pf4Pressed_resendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP), any(), any(), eq(true), eq(false), eq(false));
    }

    @Test
    void mainLine_clearPressed_resendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("_");

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP), any(), any(), eq(true), eq(false), eq(false));
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessageWithoutErase() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        OccardvFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2050-RECEIVE ─────────────────────────

    @Test
    void mainLine_receiveMapfail_wipesInputToLowValuesAndRejectsRequired() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        // Even though receiveMap populates a card number, DFHRESP(MAPFAIL) forces
        // MCARDVAI back to low-values before validation runs.
        givenReceiveMapMapfails(f -> f.setCardnumi(VALID_CARD_NUM));

        service.mainLine(appService);

        OccardvFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    // ───────────────────────── 2100-READ-AND-SHOW / 6000-VALIDATE-CARDNUM
    // ─────────────────────────

    @Test
    void mainLine_lookupBlankCardNumber_rejectsRequiredAndSkipsRead() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("                "));

        service.mainLine(appService);

        OccardvFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_lookupNonNumericCardNumber_rejectsExactly16DigitsMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("11112222333X4444"));

        service.mainLine(appService);

        OccardvFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARDNUM_16);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_lookupShortCardNumber_fewerThan16DigitsIsRejected() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("123456789012    "));

        service.mainLine(appService);

        OccardvFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARDNUM_16);
    }

    @Test
    void mainLine_lookupValidCardFound_populatesDetailAndDisplaysCard() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardEntered();
        doAnswer(
                        inv -> {
                            OccardvFields into = inv.getArgument(1);
                            into.setCdNum(VALID_CARD_NUM);
                            into.setCdAcctId(12345L);
                            into.setCdEmbossedName("JOHN SMITH");
                            into.setCdExpiryDate("2030-01-01");
                            into.setCdActiveStatus("Y");
                            return null;
                        })
                .when(appService)
                .readFile(eq(CARDFILE), any(), eq(VALID_CARD_NUM), eq(0));

        service.mainLine(appService);

        OccardvFields out = captureLastSendMapDataOnly();
        assertThat(out.getCardnumo().trim()).isEqualTo(VALID_CARD_NUM);
        assertThat(out.getCdaccto().trim()).isEqualTo("00000012345");
        assertThat(out.getCdnameo().trim()).isEqualTo("JOHN SMITH");
        assertThat(out.getCdexpo().trim()).isEqualTo("2030-01-01");
        assertThat(out.getCdstato().trim()).isEqualTo("Y");
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_DISPLAYED);

        ArgumentCaptor<Object> commareaOut = ArgumentCaptor.forClass(Object.class);
        verify(appService).returnTransid(eq("ORCV"), commareaOut.capture(), eq(692));
        byte[] bytes = (byte[]) commareaOut.getValue();
        OccardvFields returned = new OccardvFields(new WorkingStorage());
        returned.setOrionCommarea(new String(bytes, Charset.forName("MS932")));
        assertThat(returned.getCaCardNum().trim()).isEqualTo(VALID_CARD_NUM);
    }

    @Test
    void mainLine_lookupCardNotFound_showsNotFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardEntered();
        givenReadFileResp(13); // DFHRESP(NOTFND)

        service.mainLine(appService);

        OccardvFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOTFND);
    }

    @Test
    void mainLine_lookupReadUnexpectedError_sendsAbendTextAndReturnsProgram() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardEntered();
        givenReadFileResp(99); // unexpected error -> 9500-ABEND-RTN

        service.mainLine(appService);

        verify(appService).sendText(anyString(), eq(true), eq(true));
        verify(appService).returnProgram();
    }

    @Test
    void mainLine_lookupReadUnexpectedError_convertGap_sendTextShouldCarryMessageText() {
        // CONVERT-GAP: COBOL's 9500-ABEND-RTN does
        //   EXEC CICS SEND TEXT FROM(WS-MSG-TEXT) ...
        // i.e. it sends the literal error message text "OCCARDV: unrecoverable file error.
        // Contact support.". The converted abendOnFileError() instead calls
        //   ctx.appService.sendText(String.valueOf(ctx.f), true, true)
        // which stringifies the WHOLE OccardvFields accessor object (its default
        // Object.toString()) instead of ctx.f.getWsMsgText(). Expected per COBOL ground
        // truth: the text sent should contain the WS-MSG-TEXT message. This test
        // intentionally fails against current Java to flag the gap -- it is not a bug
        // in the test.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardEntered();
        givenReadFileResp(99);

        service.mainLine(appService);

        verify(appService)
                .sendText(
                        org.mockito.ArgumentMatchers.contains("unrecoverable file error"),
                        eq(true),
                        eq(true));
    }

    @Test
    void mainLine_lookupReadUnexpectedError_convertGap_returnTransidShouldNotFollowAbend() {
        // CONVERT-GAP: in COBOL, 9500-ABEND-RTN issues EXEC CICS RETURN (no TRANSID),
        // ending the pseudo-conversational task immediately -- 9000-RETURN is never
        // reached afterward. The converted Java abendOnFileError() calls
        // appService.returnProgram() but does not stop the Java call stack (the mock
        // simply returns normally), so runMainProgram() falls through and
        // returnTransid() fires anyway. Expected per COBOL ground truth: returnTransid
        // must NOT be called after an abend. This test intentionally fails against the
        // current Java to flag the gap -- it is not a bug in the test.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardEntered();
        givenReadFileResp(99);

        service.mainLine(appService);

        verify(appService, never()).returnTransid(anyString(), any(), anyInt());
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOccardv() {
        assertEquals("OCCARDV", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrcv() {
        assertEquals("ORCV", service.getTransId());
    }

    @Test
    void getButtonDefs_delegatesToMetadataWithoutError() {
        assertNotNull(service.getButtonDefs());
    }

    @Test
    void getFieldMapping_delegatesToMetadataForKnownMap() {
        assertNotNull(service.getFieldMapping(MAP));
    }
}

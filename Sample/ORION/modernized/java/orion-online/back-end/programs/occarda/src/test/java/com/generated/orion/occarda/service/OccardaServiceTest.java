package com.generated.orion.occarda.service;

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
import com.generated.orion.occarda.accessor.OccardaFields;
import com.generated.orion.occarda.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Unit tests for OccardaService, generated from COBOL program OCCARDA. All business logic is
 * private and is exercised solely through the public {@link OccardaService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS
 * SEND/RECEIVE/READ/WRITE/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OccardaServiceTest {

    private static final String ACCTFILE = "ACCTFILE";
    private static final String CARDFILE = "CARDFILE";
    private static final String XREFFILE = "XREFFILE";

    private static final String MSG_PROMPT = "Enter new card details and press ENTER.";
    private static final String MSG_CARD_REQ = "Card number must be sixteen digits.";
    private static final String MSG_ACCT_NUM = "Account id must be numeric.";
    private static final String MSG_ACCT_NF = "Account does not exist.";
    private static final String MSG_CARD_DUP = "Card number already exists.";
    private static final String MSG_NAME_REQ = "Embossed name is required.";
    private static final String MSG_CVV_NUM = "CVV must be three numeric digits.";
    private static final String MSG_EXP_BAD = "Expiry date invalid, use YYYY-MM-DD.";
    private static final String MSG_OK = "Card issued successfully.";
    private static final String MSG_WRITE_ERR = "Error writing the card file.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";

    @Mock private AppService appService;

    private OccardaService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OccardaService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commareaWithContext(int context) {
        OccardaFields helper = new OccardaFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(context));
    }

    /** Stubs receiveMap to populate the MCARDAAI input fields captured from production code. */
    private void givenReceiveMapPopulates(Consumer<OccardaFields> populate) {
        doAnswer(
                        inv -> {
                            OccardaFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MCARDAA"), any());
    }

    /**
     * Stubs a keyed file READ: sets EIBRESP for the next getEibresp() call and lets the caller
     * populate the "into" accessor (record found scenarios).
     */
    private void givenReadFile(String fileName, int resp, Consumer<OccardaFields> populate) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            if (populate != null) {
                                populate.accept(inv.getArgument(1));
                            }
                            return null;
                        })
                .when(appService)
                .readFile(eq(fileName), any(), anyString(), anyInt());
    }

    private void givenWriteFile(String fileName, int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .writeFile(eq(fileName), any(), anyString(), anyInt());
    }

    /** Populates a full set of valid input fields for 2100-ADD-CARD. */
    private void givenValidCardInput() {
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setCdaccti("00000012345");
                    f.setCdnamei("JOHN Q PUBLIC");
                    f.setCdcvvi("123");
                    f.setCdexpi("2026-12-31");
                });
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MCARDAA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OccardaFields out = (OccardaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertEquals(-1, out.getCardnuml());
        assertThat(out.getTrnnameo().trim()).isEqualTo("OROD");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCCARDA");
        verify(appService).returnTransid(eq("OROD"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq("MCARDAA"), any(), any(), eq(true), eq(false), eq(true));
        verify(appService).returnTransid(eq("OROD"), any(), eq(692));
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
    void mainLine_pf4Pressed_clearsAndResendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MCARDAA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OccardaFields out = (OccardaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MCARDAA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OccardaFields out = (OccardaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-ADD-CARD happy path ─────────────────────────

    @Test
    void mainLine_addCard_allFieldsValid_writesCardAndCrossReference() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardInput();
        givenReadFile(ACCTFILE, 0, null); // account found
        givenReadFile(CARDFILE, 13, null); // card number unique
        givenReadFile(XREFFILE, 0, f -> f.setXrCustId(555)); // xref stub found -> customer id 555
        givenWriteFile(CARDFILE, 0);
        givenWriteFile(XREFFILE, 0);

        service.mainLine(appService);

        ArgumentCaptor<Object> cardWritten = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeFile(eq(CARDFILE), cardWritten.capture(), anyString(), anyInt());
        OccardaFields card = (OccardaFields) cardWritten.getValue();
        assertThat(card.getCdNum().trim()).isEqualTo("1234567890123456");
        assertEquals(12345L, card.getCdAcctId());
        assertThat(card.getCdCvv().trim()).isEqualTo("123");
        assertThat(card.getCdEmbossedName().trim()).isEqualTo("JOHN Q PUBLIC");
        assertThat(card.getCdExpiryDate().trim()).isEqualTo("2026-12-31");
        assertThat(card.getCdActiveStatus().trim()).isEqualTo("Y");

        ArgumentCaptor<Object> xrefWritten = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeFile(eq(XREFFILE), xrefWritten.capture(), anyString(), anyInt());
        OccardaFields xref = (OccardaFields) xrefWritten.getValue();
        assertThat(xref.getXrCardNum().trim()).isEqualTo("1234567890123456");
        assertEquals(12345L, xref.getXrAcctId());
        assertEquals(555, xref.getXrCustId());

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(2))
                .sendMap(
                        eq("MCARDAA"),
                        sendMapOut.capture(),
                        any(),
                        anyBoolean(),
                        anyBoolean(),
                        anyBoolean());
        OccardaFields lastScreen = (OccardaFields) sendMapOut.getAllValues().get(1);
        assertThat(lastScreen.getErrmsgo().trim()).isEqualTo(MSG_OK);
        assertThat(lastScreen.getCaCardNum().trim()).isEqualTo("1234567890123456");
    }

    @Test
    void mainLine_addCard_xrefStubNotFound_defaultsCustomerIdToZero() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardInput();
        givenReadFile(ACCTFILE, 0, null);
        givenReadFile(CARDFILE, 13, null);
        givenReadFile(XREFFILE, 13, null); // no cross-reference stub on file
        givenWriteFile(CARDFILE, 0);
        givenWriteFile(XREFFILE, 0);

        service.mainLine(appService);

        ArgumentCaptor<Object> xrefWritten = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeFile(eq(XREFFILE), xrefWritten.capture(), anyString(), anyInt());
        OccardaFields xref = (OccardaFields) xrefWritten.getValue();
        assertEquals(0, xref.getXrCustId());
    }

    // ───────────────────────── 5000-VALIDATE-ALL edge cases ─────────────────────────

    @Test
    void mainLine_addCard_cardNumberNonNumeric_rejectsBeforeAnyFileAccess() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("ABCD123456789012"));

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_CARD_REQ, -1);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_addCard_accountIdNonNumeric_rejectsWithAcctNumMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setCdaccti("ABCDEFGHIJK");
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_ACCT_NUM, -1);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_addCard_nameBlank_rejectsWithNameReqMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setCdaccti("00000012345");
                    f.setCdnamei("                                                  ");
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_NAME_REQ, -1);
    }

    @Test
    void mainLine_addCard_cvvWrongLength_rejectsWithCvvNumMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setCdaccti("00000012345");
                    f.setCdnamei("JOHN Q PUBLIC");
                    f.setCdcvvi("12");
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_CVV_NUM, -1);
    }

    @Test
    void mainLine_addCard_expiryDateRejectedByOudate_rejectsWithExpBadMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardInput();
        // KDATE-PARM is a 26-byte group (KD-FUNC X4, KD-DATE-IN X10, KD-DATE-OUT X10,
        // KD-STATUS X2); force the trailing KD-STATUS bytes to a non-'00' code.
        doAnswer(
                        inv -> {
                            Object[] params = inv.getArgument(1);
                            String parm = String.valueOf(params[0]);
                            params[0] = parm.substring(0, parm.length() - 2) + "12";
                            return null;
                        })
                .when(appService)
                .callProgram(eq("OUDATE"), any());

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_EXP_BAD, -1);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_addCard_accountNotFound_rejectsWithAcctNfMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardInput();
        givenReadFile(ACCTFILE, 13, null); // not found

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_ACCT_NF, -1);
        verify(appService, never()).readFile(eq(CARDFILE), any(), anyString(), anyInt());
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_addCard_cardNumberAlreadyExists_rejectsWithCardDupMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardInput();
        givenReadFile(ACCTFILE, 0, null); // account found
        givenReadFile(CARDFILE, 0, null); // card already on file

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_CARD_DUP, -1);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_receiveMapFail_treatsInputAsBlankAndFailsCardValidation() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        nextResp.set(36); // DFHRESP(MAPFAIL)
        // No receiveMap stub: fields stay at their post-fillLowValues default.

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_CARD_REQ, -1);
    }

    // ───────────────────────── 3500/3700-WRITE error branches ─────────────────────────

    @Test
    void mainLine_writeCard_duplicateKeyRaceCondition_rejectsWithCardDupMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardInput();
        givenReadFile(ACCTFILE, 0, null);
        givenReadFile(CARDFILE, 13, null);
        givenWriteFile(CARDFILE, 14); // DFHRESP(DUPREC) on the write itself

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_CARD_DUP, -1, false);
        verify(appService).writeFile(eq(CARDFILE), any(), anyString(), anyInt());
        verify(appService, never()).writeFile(eq(XREFFILE), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_writeCard_unexpectedFileError_rejectsWithWriteErrMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardInput();
        givenReadFile(ACCTFILE, 0, null);
        givenReadFile(CARDFILE, 13, null);
        givenWriteFile(CARDFILE, 99); // unexpected error

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_WRITE_ERR, -1, false);
        verify(appService, never()).writeFile(eq(XREFFILE), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_writeXref_fails_stillReportsSuccessPerCobolOverwrite() {
        // Per COBOL 2100-ADD-CARD: even when 3700-WRITE-XREF sets an error message,
        // the subsequent PERFORM 1000-SEND-INITIAL resets ERRMSGO to WS-M-PROMPT and
        // then MOVE WS-M-OK TO ERRMSGO overwrites it again -- the xref error is never
        // shown to the operator. This is original COBOL behavior, not a convert gap.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardInput();
        givenReadFile(ACCTFILE, 0, null);
        givenReadFile(CARDFILE, 13, null);
        givenReadFile(XREFFILE, 13, null);
        givenWriteFile(CARDFILE, 0);
        givenWriteFile(XREFFILE, 99); // unexpected xref write error

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(2))
                .sendMap(
                        eq("MCARDAA"),
                        sendMapOut.capture(),
                        any(),
                        anyBoolean(),
                        anyBoolean(),
                        anyBoolean());
        OccardaFields lastScreen = (OccardaFields) sendMapOut.getAllValues().get(1);
        assertThat(lastScreen.getErrmsgo().trim()).isEqualTo(MSG_OK);
        verify(appService).writeFile(eq(XREFFILE), any(), anyString(), anyInt());
    }

    // ───────────────────────── 9500-ABEND-RTN ─────────────────────────

    @Test
    void mainLine_accountFileReadError_sendsAbendTextAndReturnsProgram() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardInput();
        givenReadFile(ACCTFILE, 99, null); // unexpected ACCTFILE error -> 9500-ABEND-RTN

        service.mainLine(appService);

        verify(appService).sendText(anyString(), eq(true), eq(true));
        verify(appService).returnProgram();
    }

    @Test
    void mainLine_accountFileReadError_convertGap_sendTextShouldCarryMessageText() {
        // CONVERT-GAP: COBOL's 9500-ABEND-RTN does
        //   EXEC CICS SEND TEXT FROM(WS-MSG-TEXT) ...
        // i.e. it sends the literal error message text. The converted
        // abendUnrecoverableError() instead calls
        //   ctx.appService.sendText(String.valueOf(ctx.f), true, true)
        // which stringifies the WHOLE OccardaFields accessor object (its default
        // Object.toString(), e.g. "com.generated...OccardaFields@1a2b3c") instead of
        // ctx.f.getWsMsgText(). Expected per COBOL ground truth: the text sent should
        // contain the WS-MSG-TEXT message. This test intentionally fails against the
        // current Java to flag the gap -- it is not a bug in the test.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardInput();
        givenReadFile(ACCTFILE, 99, null);

        service.mainLine(appService);

        verify(appService)
                .sendText(
                        org.mockito.ArgumentMatchers.contains("unrecoverable file error"),
                        eq(true),
                        eq(true));
    }

    @Test
    void mainLine_accountFileReadError_convertGap_returnTransidShouldNotFollowAbend() {
        // CONVERT-GAP: in COBOL, 9500-ABEND-RTN issues EXEC CICS RETURN (no TRANSID),
        // which ends the pseudo-conversational task immediately -- 9000-RETURN is never
        // reached afterward. The converted Java abendUnrecoverableError() calls
        // appService.returnProgram() but does not stop the Java call stack, so
        // runMainProgram() falls through and returnTransid() fires anyway, and the
        // enclosing validateAccountExists()/addNewCard() logic keeps running (treating
        // WS-FOUND-FLG as still "N" and sending a normal MCARDAA screen). Expected (per
        // COBOL ground truth): returnTransid must NOT be called after an abend. This
        // test intentionally fails against current Java to flag the gap -- it is not a
        // bug in the test.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardInput();
        givenReadFile(ACCTFILE, 99, null);

        service.mainLine(appService);

        verify(appService, never()).returnTransid(anyString(), any(), anyInt());
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOccarda() {
        assertEquals("OCCARDA", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrod() {
        assertEquals("OROD", service.getTransId());
    }

    @Test
    void getButtonDefs_delegatesToMetadataWithoutError() {
        assertNotNull(service.getButtonDefs());
    }

    @Test
    void getFieldMapping_delegatesToMetadataForKnownMap() {
        assertNotNull(service.getFieldMapping("MCARDAA"));
    }

    // ───────────────────────── shared assertion helper ─────────────────────────

    private void assertFinalErrorMessage(String expectedMessage, int expectedCardnuml) {
        assertFinalErrorMessage(expectedMessage, expectedCardnuml, true);
    }

    private void assertFinalErrorMessage(
            String expectedMessage, int expectedCardnuml, boolean expectNoWriteAttempted) {
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MCARDAA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OccardaFields out = (OccardaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(expectedMessage);
        assertEquals(expectedCardnuml, out.getCardnuml());
        if (expectNoWriteAttempted) {
            verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
        }
    }
}

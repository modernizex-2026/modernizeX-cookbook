package com.generated.orion.occardu.service;

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
import com.generated.orion.occardu.accessor.OccarduFields;
import com.generated.orion.occardu.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OccarduService, generated from COBOL program OCCARDU. All business logic is
 * private and is exercised solely through the public {@link OccarduService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS
 * SEND/RECEIVE/READ/REWRITE/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OccarduServiceTest {

    private static final String CARDFILE = "CARDFILE";
    private static final String MAP = "MCARDUA";

    private static final String MSG_PROMPT = "Enter card number and press ENTER.";
    private static final String MSG_LOAD_SELECTED = "Press ENTER to load the selected card.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_REQUIRED = "Please enter all required fields.";
    private static final String MSG_CARDNUM_16 = "Card number must be exactly 16 digits.";
    private static final String MSG_NOTFND = "Record not found.";
    private static final String MSG_FOUND = "Card found. Change fields, PF5 to save.";
    private static final String MSG_NAME_REQUIRED = "Embossed name is required.";
    private static final String MSG_EXPIRY_INVALID = "Expiry date invalid, use YYYY-MM-DD.";
    private static final String MSG_STATUS_INVALID = "Active status must be Y or N.";
    private static final String MSG_CHANGES_VALID =
            "Changes are valid.  Press PF5 to confirm save.";
    private static final String MSG_ENTER_FIRST = "Enter a card number and press ENTER first.";
    private static final String MSG_UPDATED = "Card updated successfully.";
    private static final String MSG_UPDATE_FAILED = "Update failed during REWRITE.";
    private static final String MSG_NO_LONGER_EXISTS = "Card no longer exists. Update aborted.";

    private static final String VALID_CARD_NUM = "1111222233334444";

    @Mock private AppService appService;

    private OccarduService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OccarduService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-23", "10:00:00"));
    }

    /**
     * Builds a serialized ORION-COMMAREA string carrying CA-PGM-CONTEXT/CA-WORK-AREA/CA-CARD-NUM.
     */
    private String commarea(int context, String workArea, String cardNum) {
        OccarduFields helper = new OccarduFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        if (workArea != null) {
            helper.setCaWorkArea(workArea);
        }
        if (cardNum != null) {
            helper.setCaCardNum(cardNum);
        }
        return helper.getOrionCommarea();
    }

    private void givenPseudoConversation(int context) {
        givenPseudoConversation(context, null, null);
    }

    private void givenPseudoConversation(int context, String workArea, String cardNum) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commarea(context, workArea, cardNum));
    }

    /** Stubs receiveMap to populate the MCARDUAI input fields captured from production code. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OccarduFields> populate) {
        doAnswer(
                        inv -> {
                            OccarduFields f = inv.getArgument(1);
                            populate.accept(f);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP), any());
    }

    /**
     * Stubs OUDATE CALL to accept/reject the expiry date per KD-STATUS ('00'=valid, else invalid).
     */
    private void givenOudateReturns(String kdStatus) {
        doAnswer(
                        inv -> {
                            Object[] params = inv.getArgument(1);
                            OccarduFields helper = new OccarduFields(new WorkingStorage());
                            helper.setKdateParm((String) params[0]);
                            helper.setKdStatus(kdStatus);
                            params[0] = helper.getKdateParm();
                            return null;
                        })
                .when(appService)
                .callProgram(eq("OUDATE"), any());
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

    /** Stubs a READ UPDATE (readFileForUpdate) on CARDFILE to return the given EIBRESP. */
    private void givenReadFileForUpdateResp(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(CARDFILE), any(), anyString(), eq(0));
    }

    /** Stubs REWRITE on CARDFILE to return the given EIBRESP. */
    private void givenRewriteResp(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(CARDFILE), any());
    }

    private OccarduFields captureLastSendMapDataOnly() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP), captor.capture(), any(), eq(false), eq(false), eq(false));
        return (OccarduFields) captor.getValue();
    }

    private OccarduFields captureLastSendMapErase() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP), captor.capture(), any(), eq(true), eq(false), eq(false));
        return (OccarduFields) captor.getValue();
    }

    private void givenValidCardEntered() {
        givenReceiveMapPopulates(f -> f.setCardnumi(VALID_CARD_NUM));
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPromptAndSetsContext() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        OccarduFields out = captureLastSendMapErase();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORCU");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCCARDU");
        verify(appService).returnTransid(eq("ORCU"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationContextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP), any(), any(), eq(true), eq(false), eq(false));
    }

    @Test
    void mainLine_initialScreenWithExistingCardNumber_promptsLoadSelectedCard() {
        givenPseudoConversation(1, "K", VALID_CARD_NUM);
        when(appService.getEibaid()).thenReturn("4"); // PF4 -> resend initial screen

        service.mainLine(appService);

        OccarduFields out = captureLastSendMapErase();
        assertThat(out.getCardnumo().trim()).isEqualTo(VALID_CARD_NUM);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LOAD_SELECTED);
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

        OccarduFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-ENTER / 2200-LOOKUP ─────────────────────────

    @Test
    void mainLine_enterKeyStateNotEdit_dispatchesToLookup() {
        givenPseudoConversation(1, "K", null);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardEntered();
        givenReadFileResp(0);

        service.mainLine(appService);

        verify(appService).readFile(eq(CARDFILE), any(), eq(VALID_CARD_NUM), eq(0));
    }

    @Test
    void mainLine_lookupBlankCardNumber_rejectsRequiredAndSkipsRead() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("                "));

        service.mainLine(appService);

        OccarduFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_lookupNonNumericCardNumber_rejectsExactly16DigitsMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("11112222333X4444"));

        service.mainLine(appService);

        OccarduFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARDNUM_16);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_lookupShortCardNumber_fewerThan16DigitsIsRejected() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("123456789012    "));

        service.mainLine(appService);

        OccarduFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARDNUM_16);
    }

    @Test
    void mainLine_lookupValidCardFound_populatesDetailSwitchesToEditState() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardEntered();
        doAnswer(
                        inv -> {
                            OccarduFields into = inv.getArgument(1);
                            into.setCdNum(VALID_CARD_NUM);
                            into.setCdEmbossedName("JOHN SMITH");
                            into.setCdExpiryDate("2030-01-01");
                            into.setCdActiveStatus("Y");
                            return null;
                        })
                .when(appService)
                .readFile(eq(CARDFILE), any(), eq(VALID_CARD_NUM), eq(0));

        service.mainLine(appService);

        OccarduFields out = captureLastSendMapDataOnly();
        assertThat(out.getCardnumo().trim()).isEqualTo(VALID_CARD_NUM);
        assertThat(out.getCdnameo().trim()).isEqualTo("JOHN SMITH");
        assertThat(out.getCdexpo().trim()).isEqualTo("2030-01-01");
        assertThat(out.getCdstato().trim()).isEqualTo("Y");
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_FOUND);

        ArgumentCaptor<Object> commareaOut = ArgumentCaptor.forClass(Object.class);
        verify(appService).returnTransid(eq("ORCU"), commareaOut.capture(), eq(692));
        byte[] bytes = (byte[]) commareaOut.getValue();
        OccarduFields returned = new OccarduFields(new WorkingStorage());
        returned.setOrionCommarea(new String(bytes, java.nio.charset.Charset.forName("MS932")));
        assertThat(returned.getCaWorkArea().substring(0, 1)).isEqualTo("E");
    }

    @Test
    void mainLine_lookupCardNotFound_showsNotFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidCardEntered();
        givenReadFileResp(13); // DFHRESP(NOTFND)

        service.mainLine(appService);

        OccarduFields out = captureLastSendMapDataOnly();
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
        // i.e. it sends the literal error message text "OCCARDU: unrecoverable file error.
        // Contact support.". The converted handleAbend() instead calls
        //   ctx.appService.sendText(String.valueOf(ctx.f), true, true)
        // which stringifies the WHOLE OccarduFields accessor object (its default
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
        // reached afterward. The converted Java handleAbend() calls
        // appService.returnProgram() but does not stop the Java call stack (the mock
        // simply returns normally), so processMainLine() falls through and
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

    // ───────────────────────── 2250-VALIDATE-ONLY ─────────────────────────

    @Test
    void mainLine_enterEditStateValidChanges_showsChangesValidMessage() {
        givenPseudoConversation(1, "E", VALID_CARD_NUM);
        when(appService.getEibaid()).thenReturn("'");
        givenOudateReturns("00");
        givenReceiveMapPopulates(
                f -> {
                    f.setCdnamei("JANE DOE");
                    f.setCdexpi("2031-05-01");
                    f.setCdstati("Y");
                });

        service.mainLine(appService);

        OccarduFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CHANGES_VALID);
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterEditStateInvalidName_rejectsWithNameRequiredMessage() {
        givenPseudoConversation(1, "E", VALID_CARD_NUM);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCdnamei("                                                  ");
                    f.setCdexpi("2031-05-01");
                    f.setCdstati("Y");
                });

        service.mainLine(appService);

        OccarduFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NAME_REQUIRED);
        // 5000-VALIDATE-ALL short-circuits: name invalid -> expiry/status validation skipped.
        verify(appService, never()).callProgram(eq("OUDATE"), any());
    }

    @Test
    void mainLine_enterEditStateInvalidExpiry_rejectsWithExpiryInvalidMessage() {
        givenPseudoConversation(1, "E", VALID_CARD_NUM);
        when(appService.getEibaid()).thenReturn("'");
        givenOudateReturns("99");
        givenReceiveMapPopulates(
                f -> {
                    f.setCdnamei("JANE DOE");
                    f.setCdexpi("BAD-DATE");
                    f.setCdstati("Y");
                });

        service.mainLine(appService);

        OccarduFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_EXPIRY_INVALID);
    }

    @Test
    void mainLine_enterEditStateInvalidStatus_rejectsWithStatusInvalidMessage() {
        givenPseudoConversation(1, "E", VALID_CARD_NUM);
        when(appService.getEibaid()).thenReturn("'");
        givenOudateReturns("00");
        givenReceiveMapPopulates(
                f -> {
                    f.setCdnamei("JANE DOE");
                    f.setCdexpi("2031-05-01");
                    f.setCdstati("Q");
                });

        service.mainLine(appService);

        OccarduFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_STATUS_INVALID);
    }

    // ───────────────────────── 2300-SAVE ─────────────────────────

    @Test
    void mainLine_pf5NotInEditState_showsEnterCardFirstMessageAndSkipsReceive() {
        givenPseudoConversation(1, "K", null);
        when(appService.getEibaid()).thenReturn("5");

        service.mainLine(appService);

        OccarduFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ENTER_FIRST);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    @Test
    void mainLine_pf5EditStateInvalidChanges_skipsUpdate() {
        givenPseudoConversation(1, "E", VALID_CARD_NUM);
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setCdnamei("                                                  ");
                    f.setCdexpi("2031-05-01");
                    f.setCdstati("Y");
                });

        service.mainLine(appService);

        OccarduFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NAME_REQUIRED);
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_pf5EditStateValidChanges_updatesCardAndPopulatesDetail() {
        givenPseudoConversation(1, "E", VALID_CARD_NUM);
        when(appService.getEibaid()).thenReturn("5");
        givenOudateReturns("00");
        givenReceiveMapPopulates(
                f -> {
                    f.setCdnamei("JANE DOE");
                    f.setCdexpi("2031-05-01");
                    f.setCdstati("Y");
                });
        givenReadFileForUpdateResp(0);
        givenRewriteResp(0);

        service.mainLine(appService);

        verify(appService).readFileForUpdate(eq(CARDFILE), any(), eq(VALID_CARD_NUM), eq(0));
        ArgumentCaptor<Object> rewriteCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService).rewriteFile(eq(CARDFILE), rewriteCaptor.capture());
        OccarduFields rewritten = (OccarduFields) rewriteCaptor.getValue();
        assertThat(rewritten.getCdEmbossedName().trim()).isEqualTo("JANE DOE");
        assertThat(rewritten.getCdExpiryDate().trim()).isEqualTo("2031-05-01");
        assertThat(rewritten.getCdActiveStatus().trim()).isEqualTo("Y");

        OccarduFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_UPDATED);
        assertThat(out.getCdnameo().trim()).isEqualTo("JANE DOE");
    }

    @Test
    void mainLine_pf5RewriteFails_showsUpdateFailedAndSkipsPopulateDetail() {
        givenPseudoConversation(1, "E", VALID_CARD_NUM);
        when(appService.getEibaid()).thenReturn("5");
        givenOudateReturns("00");
        givenReceiveMapPopulates(
                f -> {
                    f.setCdnamei("JANE DOE");
                    f.setCdexpi("2031-05-01");
                    f.setCdstati("Y");
                });
        givenReadFileForUpdateResp(0);
        givenRewriteResp(99); // REWRITE failed (not NORMAL)

        service.mainLine(appService);

        OccarduFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_UPDATE_FAILED);
        // WS-UPD-SW stays 'N' -> 4000-POPULATE-DETAIL must be skipped, CARDNUMO stays low-values.
        assertThat(out.getCardnumo().trim()).isEmpty();
    }

    @Test
    void mainLine_pf5CardNoLongerExists_showsAbortedMessageAndSkipsRewrite() {
        givenPseudoConversation(1, "E", VALID_CARD_NUM);
        when(appService.getEibaid()).thenReturn("5");
        givenOudateReturns("00");
        givenReceiveMapPopulates(
                f -> {
                    f.setCdnamei("JANE DOE");
                    f.setCdexpi("2031-05-01");
                    f.setCdstati("Y");
                });
        givenReadFileForUpdateResp(13); // DFHRESP(NOTFND)

        service.mainLine(appService);

        OccarduFields out = captureLastSendMapDataOnly();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NO_LONGER_EXISTS);
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void mainLine_pf5ReadForUpdateUnexpectedError_sendsAbendTextAndReturnsProgram() {
        givenPseudoConversation(1, "E", VALID_CARD_NUM);
        when(appService.getEibaid()).thenReturn("5");
        givenOudateReturns("00");
        givenReceiveMapPopulates(
                f -> {
                    f.setCdnamei("JANE DOE");
                    f.setCdexpi("2031-05-01");
                    f.setCdstati("Y");
                });
        givenReadFileForUpdateResp(99); // unexpected error -> 9500-ABEND-RTN

        service.mainLine(appService);

        verify(appService).sendText(anyString(), eq(true), eq(true));
        verify(appService).returnProgram();
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOccardu() {
        assertEquals("OCCARDU", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrcu() {
        assertEquals("ORCU", service.getTransId());
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

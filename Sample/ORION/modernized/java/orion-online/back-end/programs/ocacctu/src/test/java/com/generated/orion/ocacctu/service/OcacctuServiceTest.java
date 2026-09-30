package com.generated.orion.ocacctu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.ocacctu.accessor.OcacctuFields;
import com.generated.orion.ocacctu.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OcacctuService, generated from COBOL program OCACCTU. All business logic is
 * private and is exercised solely through the public {@link OcacctuService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS
 * SEND/RECEIVE/READ/REWRITE/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcacctuServiceTest {

    private static final String ACCTFILE = "ACCTFILE";

    private static final String MSG_PROMPT = "Enter account id and press ENTER.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_NOTFND = "Record not found.";
    private static final String MSG_REQUIRED = "Please enter all required fields.";
    private static final String MSG_ACCT_NUM = "Account id must be numeric.";
    private static final String MSG_FOUND = "Account found. Change fields, PF5 to save.";
    private static final String MSG_CONFIRM = "Changes are valid.  Press PF5 to confirm save.";
    private static final String MSG_STATUS_BAD = "Active status must be Y or N.";
    private static final String MSG_CRLIM_BAD = "Credit limit is not a valid amount.";
    private static final String MSG_CSLIM_BAD = "Cash limit is not a valid amount.";
    private static final String MSG_RULE_BAD = "Cash limit cannot exceed credit limit.";
    private static final String MSG_EXPIRY_BAD = "Expiry date invalid, use YYYY-MM-DD.";
    private static final String MSG_GROUP_REQ = "Group id is required.";
    private static final String MSG_ENTER_FIRST = "Enter an account id and press ENTER first.";
    private static final String MSG_UPD_OK = "Account updated successfully.";
    private static final String MSG_UPD_GONE = "Account no longer exists. Update aborted.";
    private static final String MSG_BAL_TOO_HIGH = "Credit limit below current balance. Not saved.";
    private static final String MSG_REWRITE_FAIL = "Update failed during REWRITE.";

    @Mock private AppService appService;

    private OcacctuService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OcacctuService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /**
     * Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT / CA-WORK-AREA
     * state.
     */
    private String commareaWithState(int context, String workAreaState) {
        OcacctuFields helper = new OcacctuFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        if (workAreaState != null) {
            helper.setCaWorkArea(workAreaState);
        }
        return helper.getOrionCommarea();
    }

    /**
     * Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context/state.
     */
    private void givenPseudoConversation(int context, String workAreaState) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea())
                .thenReturn(commareaWithState(context, workAreaState));
    }

    private void givenPseudoConversation(int context) {
        givenPseudoConversation(context, null);
    }

    /** Stubs receiveMap to populate the MACCTUAI input fields captured from production code. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OcacctuFields> populate) {
        doAnswer(
                        inv -> {
                            OcacctuFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MACCTUA"), any());
    }

    /** Stubs the OUDATE subroutine call to accept or reject the expiry date via KD-STATUS. */
    private void givenOudateReturns(boolean valid) {
        doAnswer(
                        inv -> {
                            Object[] params = inv.getArgument(1);
                            String parm = String.valueOf(params[0]);
                            String status = valid ? "00" : "99";
                            params[0] = parm.substring(0, parm.length() - 2) + status;
                            return null;
                        })
                .when(appService)
                .callProgram(eq("OUDATE"), any());
    }

    private void populateValidChangeFields(OcacctuFields f) {
        f.setAcstati("Y");
        f.setAccrlimi("1,000.00");
        f.setAccslimi("500.00");
        f.setAcexpi("2027-01-15");
        f.setAcgrpi("STD001");
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTUA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcacctuFields out = (OcacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORAU");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCACCTU");
        assertThat(out.getCaWorkArea().substring(0, 1)).isEqualTo("K");
        verify(appService).returnTransid(eq("ORAU"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq("MACCTUA"), any(), any(), eq(true), eq(false), eq(false));
        verify(appService).returnTransid(eq("ORAU"), any(), eq(692));
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToMenu() {
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        verify(appService).xctl(argThat(s -> s.trim().equals("OCMENU")), any(), eq(692));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void mainLine_pf12Pressed_transfersControlToMenu() {
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("@");

        service.mainLine(appService);

        verify(appService).xctl(argThat(s -> s.trim().equals("OCMENU")), any(), eq(692));
    }

    @Test
    void mainLine_pf4Pressed_clearsAndResendsInitialScreen() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTUA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcacctuFields out = (OcacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getCaWorkArea().substring(0, 1)).isEqualTo("K");
    }

    @Test
    void mainLine_clearPressed_resendsInitialScreen() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("_");

        service.mainLine(appService);

        verify(appService).sendMap(eq("MACCTUA"), any(), any(), eq(true), eq(false), eq(false));
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTUA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcacctuFields out = (OcacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-ENTER / 2200-LOOKUP ─────────────────────────

    @Test
    void mainLine_enterInKeyState_accountFound_populatesDetailAndEntersEditState() {
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("00000012345"));
        doAnswer(
                        inv -> {
                            nextResp.set(0); // NORMAL -> found
                            return null;
                        })
                .when(appService)
                .readFile(eq(ACCTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTUA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcacctuFields out = (OcacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_FOUND);
        assertThat(out.getCaWorkArea().substring(0, 1)).isEqualTo("E");
        assertThat(out.getAcctido()).isEqualTo(String.format("%011d", 12345));
        verify(appService).readFile(eq(ACCTFILE), any(), eq("12345"), eq(0));
    }

    @Test
    void mainLine_enterInKeyState_accountNotFound_showsNotFoundMessage() {
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("12345"));
        doAnswer(
                        inv -> {
                            nextResp.set(13); // NOTFND
                            return null;
                        })
                .when(appService)
                .readFile(eq(ACCTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTUA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcacctuFields out = (OcacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOTFND);
    }

    @Test
    void mainLine_enterInKeyState_blankAccountId_rejectsWithRequiredMessage() {
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("           "));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTUA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcacctuFields out = (OcacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterInKeyState_nonNumericAccountId_rejectsWithNumericMessage() {
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("ABCDEFGHIJK"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTUA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcacctuFields out = (OcacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ACCT_NUM);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_lookupRead_unexpectedFileError_abends() {
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("12345"));
        doAnswer(
                        inv -> {
                            nextResp.set(99); // unexpected error
                            return null;
                        })
                .when(appService)
                .readFile(eq(ACCTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        verify(appService).sendText(anyString(), eq(true), eq(true));
        verify(appService).returnProgram();
    }

    // ───────────────────────── 2250-VALIDATE-ONLY (edit state, ENTER) ─────────────────────────

    @Test
    void mainLine_enterInEditState_allFieldsValid_promptsConfirmSave() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(this::populateValidChangeFields);
        givenOudateReturns(true);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTUA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcacctuFields out = (OcacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CONFIRM);
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterInEditState_invalidStatus_rejectsWithStatusMessage() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcstati("Q");
                    f.setAccrlimi("1000.00");
                    f.setAccslimi("500.00");
                    f.setAcexpi("2027-01-15");
                    f.setAcgrpi("STD001");
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_STATUS_BAD);
        verify(appService, never()).callProgram(eq("OUDATE"), any());
    }

    // ───────────────────────── 2300-SAVE ─────────────────────────

    @Test
    void mainLine_savePressed_notInEditState_rejectsBeforeReceiving() {
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("5");

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_ENTER_FIRST);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    @Test
    void mainLine_savePressed_allFieldsValid_updatesAccountAndShowsSuccess() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(this::populateValidChangeFields);
        givenOudateReturns(true);
        doAnswer(
                        inv -> {
                            OcacctuFields f = inv.getArgument(1);
                            f.setAcCurrBal(new BigDecimal("100.00"));
                            nextResp.set(0); // NORMAL -> found for update
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(ACCTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0); // rewrite ok
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(ACCTFILE), any());

        service.mainLine(appService);

        ArgumentCaptor<Object> rewritten = ArgumentCaptor.forClass(Object.class);
        verify(appService).rewriteFile(eq(ACCTFILE), rewritten.capture());
        OcacctuFields rec = (OcacctuFields) rewritten.getValue();
        assertThat(rec.getAcActiveStatus().trim()).isEqualTo("Y");
        assertEquals(0, rec.getAcCreditLimit().compareTo(new BigDecimal("1000.00")));
        assertEquals(0, rec.getAcCashLimit().compareTo(new BigDecimal("500.00")));
        assertThat(rec.getAcExpiryDate().trim()).isEqualTo("2027-01-15");
        assertThat(rec.getAcGroupId().trim()).isEqualTo("STD001");

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTUA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcacctuFields out = (OcacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_UPD_OK);
    }

    @Test
    void mainLine_savePressed_invalidFields_doesNotAttemptUpdate() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcstati("Y");
                    f.setAccrlimi("100.00");
                    f.setAccslimi("200.00"); // exceeds credit limit
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_RULE_BAD);
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void mainLine_savePressed_creditLimitBelowCurrentBalance_rejectsWithoutRewrite() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcstati("Y");
                    f.setAccrlimi("100.00");
                    f.setAccslimi("50.00");
                    f.setAcexpi("2027-01-15");
                    f.setAcgrpi("STD001");
                });
        givenOudateReturns(true);
        doAnswer(
                        inv -> {
                            OcacctuFields f = inv.getArgument(1);
                            f.setAcCurrBal(
                                    new BigDecimal("500.00")); // higher than new credit limit
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(ACCTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_BAL_TOO_HIGH);
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void mainLine_savePressed_accountNoLongerExists_rejectsWithGoneMessage() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(this::populateValidChangeFields);
        givenOudateReturns(true);
        doAnswer(
                        inv -> {
                            nextResp.set(13); // NOTFND
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(ACCTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_UPD_GONE);
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void mainLine_savePressed_rewriteFails_rejectsWithRewriteFailMessage() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(this::populateValidChangeFields);
        givenOudateReturns(true);
        doAnswer(
                        inv -> {
                            OcacctuFields f = inv.getArgument(1);
                            f.setAcCurrBal(new BigDecimal("100.00"));
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(ACCTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(99); // rewrite fails
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(ACCTFILE), any());

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_REWRITE_FAIL);
    }

    @Test
    void mainLine_savePressed_updateReadUnexpectedError_abends() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(this::populateValidChangeFields);
        givenOudateReturns(true);
        doAnswer(
                        inv -> {
                            nextResp.set(99); // unexpected error
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(ACCTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        verify(appService).sendText(anyString(), eq(true), eq(true));
        verify(appService).returnProgram();
    }

    // ───────────────────────── 5000-VALIDATE-ALL field-level edge cases ─────────────────────────

    @Test
    void mainLine_enterInEditState_creditLimitTwoDecimalPoints_rejectsWithCreditLimitMessage() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcstati("Y");
                    f.setAccrlimi("12.34.56");
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_CRLIM_BAD);
    }

    @Test
    void mainLine_enterInEditState_cashLimitTooManyFractionDigits_rejectsWithCashLimitMessage() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcstati("Y");
                    f.setAccrlimi("100.00");
                    f.setAccslimi("1.234"); // 3 fraction digits > 2
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_CSLIM_BAD);
    }

    @Test
    void mainLine_enterInEditState_expiryDateInvalid_rejectsWithExpiryMessage() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcstati("Y");
                    f.setAccrlimi("1000.00");
                    f.setAccslimi("500.00");
                    f.setAcexpi("2027-99-99");
                });
        givenOudateReturns(false);

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_EXPIRY_BAD);
    }

    @Test
    void mainLine_enterInEditState_groupIdBlank_rejectsWithGroupRequiredMessage() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcstati("Y");
                    f.setAccrlimi("1000.00");
                    f.setAccslimi("500.00");
                    f.setAcexpi("2027-01-15");
                    f.setAcgrpi("          ");
                });
        givenOudateReturns(true);

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_GROUP_REQ);
    }

    // ───────────────────────── 9500-ABEND-RTN convert-gap regressions ─────────────────────────

    @Test
    void mainLine_lookupRead_convertGap_sendTextShouldCarryMessageText() {
        // CONVERT-GAP: COBOL's 9500-ABEND-RTN does EXEC CICS SEND TEXT FROM(WS-MSG-TEXT),
        // i.e. it sends the literal error message text. The converted abendWithFileError()
        // instead calls ctx.appService.sendText(String.valueOf(ctx.f), true, true), which
        // stringifies the WHOLE OcacctuFields accessor object (its default Object.toString(),
        // e.g. "com.generated...OcacctuFields@1a2b3c") instead of ctx.f.getWsMsgText().
        // Expected per COBOL ground truth: the text sent should contain the WS-MSG-TEXT
        // message. This test intentionally fails against the current Java to flag the gap.
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("12345"));
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .readFile(eq(ACCTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        verify(appService).sendText(contains("unrecoverable file error"), eq(true), eq(true));
    }

    @Test
    void mainLine_lookupRead_convertGap_returnTransidShouldNotFollowAbend() {
        // CONVERT-GAP: in COBOL, 9500-ABEND-RTN issues EXEC CICS RETURN (no TRANSID),
        // ending the pseudo-conversational task immediately - 9000-RETURN is never reached
        // afterward. The converted Java abendWithFileError() calls appService.returnProgram()
        // but does not stop the Java call stack, so runMainProgram() falls through to
        // returnToCics() and returnTransid() fires anyway. Expected per COBOL ground truth:
        // returnTransid must NOT be called after an abend. This test intentionally fails
        // against current Java to flag the gap.
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("12345"));
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .readFile(eq(ACCTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        verify(appService, never()).returnTransid(anyString(), any(), anyInt());
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcacctu() {
        assertEquals("OCACCTU", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrau() {
        assertEquals("ORAU", service.getTransId());
    }

    @Test
    void getButtonDefs_returnsNonEmptyButtonList() {
        assertThat(service.getButtonDefs()).isNotEmpty();
    }

    @Test
    void getFieldMapping_knownMap_returnsPopulatedMapping() {
        com.appruntime.FieldMapping mapping = service.getFieldMapping("MACCTUA");

        assertThat(mapping).isNotNull();
    }

    @Test
    void getFieldMapping_unknownMap_returnsEmptyMapping() {
        com.appruntime.FieldMapping mapping = service.getFieldMapping("UNKNOWN");

        assertThat(mapping).isNotNull();
    }

    // ───────────────────────── shared assertion helper ─────────────────────────

    private void assertFinalErrorMessage(String expectedMessage) {
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTUA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcacctuFields out = (OcacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(expectedMessage);
    }
}

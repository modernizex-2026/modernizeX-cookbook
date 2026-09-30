package com.generated.orion.ocbillp.service;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.ocbillp.accessor.OcbillpFields;
import com.generated.orion.ocbillp.model.WorkingStorage;

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
 * Unit tests for OcbillpService, generated from COBOL program OCBILLP. All business logic is
 * private and is exercised solely through the public {@link OcbillpService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS
 * SEND/RECEIVE/READ/WRITE/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcbillpServiceTest {

    private static final String MAP_NAME = "MBILLPA";
    private static final String ACCTFILE = "ACCTFILE";
    private static final String CTRLFILE = "CTRLFILE";
    private static final String BILLFILE = "BILLFILE";

    private static final String MSG_PROMPT_INITIAL = "Enter account id and press ENTER.";
    private static final String MSG_PROMPT_PAY = "Enter amount, set confirm to Y, press ENTER.";
    private static final String MSG_REQUIRED = "Please enter all required fields.";
    private static final String MSG_NOTFND = "Record not found.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_AMT_REQUIRED = "Please enter a payment amount.";
    private static final String MSG_AMT_INVALID = "Amount is not a valid number.";
    private static final String MSG_AMT_ZERO = "Amount must be greater than zero.";
    private static final String MSG_AMT_EXCEEDS = "Amount exceeds current balance.";
    private static final String MSG_SET_CONFIRM = "Set confirm to Y to post this payment.";
    private static final String MSG_CTRL_MISSING = "Control record BILLID missing.";
    private static final String MSG_CTRL_READ_ERR = "Error reading control file.";
    private static final String MSG_CTRL_UPDATE_ERR = "Error updating control file.";
    private static final String MSG_BILL_DUP = "Duplicate bill id generated.";
    private static final String MSG_BILL_WRITE_ERR = "Error writing bill file.";
    private static final String MSG_ACCT_NOTFND_UPDATE = "Account not found on update.";
    private static final String MSG_ACCT_READ_ERR_UPDATE = "Error reading account for update.";
    private static final String MSG_ACCT_UPDATE_ERR = "Error updating account balance.";

    @Mock private AppService appService;

    private OcbillpService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OcbillpService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /**
     * Builds a serialized ORION-COMMAREA carrying the given CA-PGM-CONTEXT and a WS-STATE-AREA
     * (WS-STAGE/WS-SV-ACCT-ID/WS-SV-BAL) copied into CA-WORK-AREA exactly the way 9000-RETURN does
     * on every real turn — CA-WORK-AREA itself has no meaningful default, so skipping this copy
     * leaves WS-STAGE as spaces after runMainProgram's MOVE CA-WORK-AREA TO WS-STATE-AREA, which
     * silently misroutes every ENTER-key test into the "unknown stage" fallback.
     */
    private String commareaAtStage(int context, String stage, long svAcctId, BigDecimal svBal) {
        OcbillpFields helper = new OcbillpFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        helper.setString("WS-STAGE", stage);
        helper.setWsSvAcctId(svAcctId);
        helper.setWsSvBal(svBal);
        helper.copyBytes("CA-WORK-AREA", "WS-STATE-AREA");
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea())
                .thenReturn(commareaAtStage(context, "0", 0L, BigDecimal.ZERO));
    }

    /** Stubs a pseudo-conversational commarea positioned at the given WS-STAGE with saved state. */
    private void givenPseudoConversationAtStage(String stage, long svAcctId, BigDecimal svBal) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea())
                .thenReturn(commareaAtStage(1, stage, svAcctId, svBal));
    }

    /** Stubs receiveMap to populate the MBILLPAI input fields captured from production code. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OcbillpFields> populate) {
        doAnswer(
                        inv -> {
                            OcbillpFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    /**
     * Positions the mock at stage 1 (awaiting payment amount) with the given saved account/balance.
     */
    private void givenPaymentStage(long acctId, BigDecimal balance) {
        givenPseudoConversationAtStage("1", acctId, balance);
        when(appService.getEibaid()).thenReturn("'");
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcbillpFields out = (OcbillpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT_INITIAL);
        assertThat(out.getWsStage().trim()).isEqualTo("0");
        assertEquals(0L, out.getWsSvAcctId());
        assertEquals(0, out.getWsSvBal().compareTo(BigDecimal.ZERO));
        verify(appService).returnTransid(eq("ORBP"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(false));
        verify(appService).returnTransid(eq("ORBP"), any(), eq(692));
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToMenu() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        verify(appService).xctl(argThat(s -> s.trim().equals("OCMENU")), any(), eq(692));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void mainLine_pf4Pressed_resendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcbillpFields out = (OcbillpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT_INITIAL);
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(2))
                .sendMap(
                        eq(MAP_NAME),
                        sendMapOut.capture(),
                        any(),
                        anyBoolean(),
                        anyBoolean(),
                        anyBoolean());
        OcbillpFields last = (OcbillpFields) sendMapOut.getAllValues().get(1);
        assertThat(last.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2200-PROCESS-ACCT ─────────────────────────

    @Test
    void mainLine_stage0_acctIdBlank_showsRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi(" "));

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_REQUIRED);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_stage0_acctIdAllLowValues_showsRequiredMessage() {
        // Edge case: ACCTIDI left completely untouched (never keyed in) stays at the
        // LOW-VALUES the map buffer was filled with by 1000-SEND-INITIAL.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        // No receiveMap stub: fields stay at their post-fillLowValues default.

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_REQUIRED);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_stage0_accountFound_showsBalanceAndAdvancesToPayStage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("12345"));
        doAnswer(
                        inv -> {
                            OcbillpFields f = inv.getArgument(1);
                            f.setAcCurrBal(new BigDecimal("250.75"));
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(ACCTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<String> ridfld = ArgumentCaptor.forClass(String.class);
        verify(appService).readFile(eq(ACCTFILE), any(), ridfld.capture(), eq(0));
        assertEquals("12345", ridfld.getValue());

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcbillpFields out = (OcbillpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT_PAY);
        assertThat(out.getWsStage().trim()).isEqualTo("1");
        assertEquals(12345L, out.getWsSvAcctId());
        assertEquals(0, out.getWsSvBal().compareTo(new BigDecimal("250.75")));
        assertThat(out.getAcctido().trim()).isEqualTo("00000012345");
    }

    @Test
    void mainLine_stage0_accountNotFound_showsNotFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("99999"));
        nextResp.set(13); // DFHRESP(NOTFND)

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_NOTFND);
    }

    @Test
    void mainLine_stage0_accountReadUnexpectedError_stillShowsNotFoundMessage() {
        // Not a convert gap: 3000-READ-ACCT sets ERR-FLG-ON + "Error reading account
        // file." on the OTHER branch but leaves REC-FOUND unset, so 2200-PROCESS-ACCT's
        // ELSE branch (WS-MSG-NOTFND) always overwrites ERRMSGO afterward. Java mirrors
        // this exactly: WS-FOUND-FLG only becomes "Y" on resp 0.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("12345"));
        nextResp.set(99); // unexpected error

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_NOTFND);
    }

    // ───────────────────────── 2310-VALIDATE-AMOUNT ─────────────────────────

    @Test
    void mainLine_stage1_amountBlank_showsPleaseEnterAmountMessage() {
        givenPaymentStage(12345L, new BigDecimal("500.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti(" ");
                    f.setBlconfi("Y");
                });

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_AMT_REQUIRED);
    }

    @Test
    void mainLine_stage1_amountHasInvalidCharacter_showsNotValidNumberMessage() {
        givenPaymentStage(12345L, new BigDecimal("500.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti("10A.00");
                    f.setBlconfi("Y");
                });

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_AMT_INVALID);
    }

    @Test
    void mainLine_stage1_amountHasTwoDecimalPoints_showsNotValidNumberMessage() {
        givenPaymentStage(12345L, new BigDecimal("500.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti("1.2.3");
                    f.setBlconfi("Y");
                });

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_AMT_INVALID);
    }

    @Test
    void mainLine_stage1_amountHasNoDigits_showsNotValidNumberMessage() {
        givenPaymentStage(12345L, new BigDecimal("500.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti(".");
                    f.setBlconfi("Y");
                });

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_AMT_INVALID);
    }

    @Test
    void mainLine_stage1_amountIsZero_showsGreaterThanZeroMessage() {
        givenPaymentStage(12345L, new BigDecimal("500.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti("0.00");
                    f.setBlconfi("Y");
                });

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_AMT_ZERO);
    }

    @Test
    void mainLine_stage1_amountExceedsBalance_showsExceedsMessage() {
        givenPaymentStage(12345L, new BigDecimal("100.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti("150.00");
                    f.setBlconfi("Y");
                });

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_AMT_EXCEEDS);
    }

    // ───────────────────────── 2320-CHECK-CONFIRM ─────────────────────────

    @Test
    void mainLine_stage1_confirmNotY_showsSetConfirmMessage() {
        givenPaymentStage(12345L, new BigDecimal("500.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti("50.00");
                    f.setBlconfi("N");
                });

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_SET_CONFIRM);
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    // ───────────────────────── 2400-POST-PAYMENT happy path ─────────────────────────

    @Test
    void mainLine_stage1_confirmY_postsPaymentSuccessfully() {
        givenPaymentStage(12345L, new BigDecimal("500.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti("150.50");
                    f.setBlconfi("Y");
                });
        doAnswer(
                        inv -> {
                            OcbillpFields f = inv.getArgument(1);
                            f.setCtLastValue(41L);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(CTRLFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(CTRLFILE), any());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .writeFile(eq(BILLFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            OcbillpFields f = inv.getArgument(1);
                            f.setAcCurrBal(new BigDecimal("500.00"));
                            f.setAcCycCredit(BigDecimal.ZERO);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(ACCTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(ACCTFILE), any());

        service.mainLine(appService);

        ArgumentCaptor<Object> billWritten = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeFile(eq(BILLFILE), billWritten.capture(), anyString(), anyInt());
        OcbillpFields bill = (OcbillpFields) billWritten.getValue();
        assertEquals(12345L, bill.getBlAcctId());
        assertEquals(0, bill.getBlAmount().compareTo(new BigDecimal("150.50")));
        assertThat(bill.getBlStatus().trim()).isEqualTo("P");

        ArgumentCaptor<Object> acctRewritten = ArgumentCaptor.forClass(Object.class);
        verify(appService).rewriteFile(eq(ACCTFILE), acctRewritten.capture());
        OcbillpFields acct = (OcbillpFields) acctRewritten.getValue();
        assertEquals(0, acct.getAcCurrBal().compareTo(new BigDecimal("349.50")));
        assertEquals(0, acct.getAcCycCredit().compareTo(new BigDecimal("150.50")));

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcbillpFields finalScreen = (OcbillpFields) sendMapOut.getValue();
        assertThat(finalScreen.getErrmsgo().trim()).startsWith("Payment posted. Confirmation:");
        assertThat(finalScreen.getWsStage().trim()).isEqualTo("0");
        assertEquals(0L, finalScreen.getWsSvAcctId());
        assertEquals(0, finalScreen.getWsSvBal().compareTo(BigDecimal.ZERO));
    }

    @Test
    void mainLine_stage1_confirmLowercaseY_postsPaymentSuccessfully() {
        givenPaymentStage(12345L, new BigDecimal("500.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti("50.00");
                    f.setBlconfi("y");
                });
        doAnswer(
                        inv -> {
                            OcbillpFields f = inv.getArgument(1);
                            f.setCtLastValue(1L);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(CTRLFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(CTRLFILE), any());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .writeFile(eq(BILLFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            OcbillpFields f = inv.getArgument(1);
                            f.setAcCurrBal(new BigDecimal("500.00"));
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(ACCTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(ACCTFILE), any());

        service.mainLine(appService);

        verify(appService).writeFile(eq(BILLFILE), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_postPayment_confirmNumber_convertGap_missingZeroPadding() {
        // CONVERT-GAP: COBOL's 4100-BUILD-CONFIRM does
        //   STRING 'ORB' WS-ID-11 '00' INTO WS-CONFIRM-NUM
        // WS-ID-11 is PIC 9(11): STRING moves its full zero-padded 11-digit display
        // form, so for a bumped counter of 41 the expected confirmation number is
        // "ORB" + "00000000041" + "00" = "ORB0000000004100".
        // The converted buildConfirmationNumber() instead does
        //   sb.append(String.valueOf(ctx.f.getWsId11()))
        // which yields the bare decimal "41" with no zero-padding, producing
        // "ORB4100" instead. Expected per COBOL ground truth: the full 16-char
        // zero-padded form. This test intentionally fails against the current Java
        // to flag the gap — it is not a bug in the test.
        givenPaymentStage(12345L, new BigDecimal("500.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti("50.00");
                    f.setBlconfi("Y");
                });
        doAnswer(
                        inv -> {
                            OcbillpFields f = inv.getArgument(1);
                            f.setCtLastValue(41L);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(CTRLFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(CTRLFILE), any());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .writeFile(eq(BILLFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            OcbillpFields f = inv.getArgument(1);
                            f.setAcCurrBal(new BigDecimal("500.00"));
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(ACCTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(ACCTFILE), any());

        service.mainLine(appService);

        ArgumentCaptor<Object> billWritten = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeFile(eq(BILLFILE), billWritten.capture(), anyString(), anyInt());
        OcbillpFields bill = (OcbillpFields) billWritten.getValue();
        assertThat(bill.getBlConfirmNum().trim()).isEqualTo("ORB0000000004100");
    }

    // ───────────────────────── 3100-GET-NEXT-BILLID error branches ─────────────────────────

    @Test
    void mainLine_postPayment_ctrlRecordMissing_showsControlMissingMessage() {
        givenPaymentStage(12345L, new BigDecimal("500.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti("50.00");
                    f.setBlconfi("Y");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(13);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(CTRLFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_CTRL_MISSING);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_postPayment_ctrlReadUnexpectedError_showsReadErrorMessage() {
        givenPaymentStage(12345L, new BigDecimal("500.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti("50.00");
                    f.setBlconfi("Y");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(CTRLFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_CTRL_READ_ERR);
    }

    @Test
    void mainLine_postPayment_ctrlRewriteFails_showsUpdateErrorMessage() {
        givenPaymentStage(12345L, new BigDecimal("500.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti("50.00");
                    f.setBlconfi("Y");
                });
        doAnswer(
                        inv -> {
                            OcbillpFields f = inv.getArgument(1);
                            f.setCtLastValue(1L);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(CTRLFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(CTRLFILE), any());

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_CTRL_UPDATE_ERR);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    // ───────────────────────── 3200-WRITE-BILL error branches ─────────────────────────

    @Test
    void mainLine_postPayment_writeBillDuplicate_showsDuplicateMessage() {
        givenPaymentStage(12345L, new BigDecimal("500.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti("50.00");
                    f.setBlconfi("Y");
                });
        doAnswer(
                        inv -> {
                            OcbillpFields f = inv.getArgument(1);
                            f.setCtLastValue(1L);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(CTRLFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(CTRLFILE), any());
        doAnswer(
                        inv -> {
                            nextResp.set(14); // DFHRESP(DUPREC)
                            return null;
                        })
                .when(appService)
                .writeFile(eq(BILLFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_BILL_DUP);
        verify(appService, never()).readFileForUpdate(eq(ACCTFILE), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_postPayment_writeBillUnexpectedError_showsWriteErrorMessage() {
        givenPaymentStage(12345L, new BigDecimal("500.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti("50.00");
                    f.setBlconfi("Y");
                });
        doAnswer(
                        inv -> {
                            OcbillpFields f = inv.getArgument(1);
                            f.setCtLastValue(1L);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(CTRLFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(CTRLFILE), any());
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .writeFile(eq(BILLFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_BILL_WRITE_ERR);
    }

    // ───────────────────────── 3300-UPDATE-ACCT-BAL error branches ─────────────────────────

    @Test
    void mainLine_postPayment_updateAcctNotFound_showsAccountNotFoundOnUpdateMessage() {
        givenPaymentStage(12345L, new BigDecimal("500.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti("50.00");
                    f.setBlconfi("Y");
                });
        doAnswer(
                        inv -> {
                            OcbillpFields f = inv.getArgument(1);
                            f.setCtLastValue(1L);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(CTRLFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(CTRLFILE), any());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .writeFile(eq(BILLFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(13);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(ACCTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_ACCT_NOTFND_UPDATE);
        verify(appService, never()).rewriteFile(eq(ACCTFILE), any());
    }

    @Test
    void mainLine_postPayment_updateAcctReadUnexpectedError_showsReadErrorMessage() {
        givenPaymentStage(12345L, new BigDecimal("500.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti("50.00");
                    f.setBlconfi("Y");
                });
        doAnswer(
                        inv -> {
                            OcbillpFields f = inv.getArgument(1);
                            f.setCtLastValue(1L);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(CTRLFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(CTRLFILE), any());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .writeFile(eq(BILLFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(ACCTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_ACCT_READ_ERR_UPDATE);
    }

    @Test
    void mainLine_postPayment_updateAcctRewriteFails_showsUpdateErrorMessage() {
        givenPaymentStage(12345L, new BigDecimal("500.00"));
        givenReceiveMapPopulates(
                f -> {
                    f.setBlamti("50.00");
                    f.setBlconfi("Y");
                });
        doAnswer(
                        inv -> {
                            OcbillpFields f = inv.getArgument(1);
                            f.setCtLastValue(1L);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(CTRLFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(CTRLFILE), any());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .writeFile(eq(BILLFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            OcbillpFields f = inv.getArgument(1);
                            f.setAcCurrBal(new BigDecimal("500.00"));
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(ACCTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(ACCTFILE), any());

        service.mainLine(appService);

        assertDataOnlyErrorMessage(MSG_ACCT_UPDATE_ERR);
    }

    // ───────────────────────── 2100-HANDLE-ENTER stage fallback ─────────────────────────

    @Test
    void mainLine_stageNotZeroOrOne_sendsInitialScreen() {
        // Simulate a corrupted/legacy WS-STAGE value that is neither "0" nor "1".
        givenPseudoConversationAtStage("9", 0L, BigDecimal.ZERO);
        when(appService.getEibaid()).thenReturn("'");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcbillpFields out = (OcbillpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT_INITIAL);
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcbillp() {
        assertEquals("OCBILLP", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrbp() {
        assertEquals("ORBP", service.getTransId());
    }

    // ───────────────────────── shared assertion helper ─────────────────────────

    /**
     * Asserts the LAST data-only (non-erase) screen sent carries the given error message. Uses
     * atLeastOnce()+last-value instead of a strict single verify because some scenarios are driven
     * through two mainLine() turns (account lookup, then the payment turn under test), each
     * emitting one data-only send.
     */
    private void assertDataOnlyErrorMessage(String expectedMessage) {
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService, org.mockito.Mockito.atLeastOnce())
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        java.util.List<Object> values = sendMapOut.getAllValues();
        OcbillpFields out = (OcbillpFields) values.get(values.size() - 1);
        assertThat(out.getErrmsgo().trim()).isEqualTo(expectedMessage);
    }
}

package com.generated.orion.ocaccta.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
import com.generated.orion.ocaccta.accessor.OcacctaFields;
import com.generated.orion.ocaccta.model.WorkingStorage;

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
 * Unit tests for OcacctaService, generated from COBOL program OCACCTA. All business logic is
 * private and is exercised solely through the public {@link OcacctaService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS
 * SEND/RECEIVE/READ/WRITE/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcacctaServiceTest {

    private static final String CUSTFILE = "CUSTFILE";
    private static final String ACCTFILE = "ACCTFILE";
    private static final String XREFFILE = "XREFFILE";

    private static final String MSG_PROMPT = "Enter new account details and press ENTER.";
    private static final String MSG_ACCT_NUM = "Account id must be numeric.";
    private static final String MSG_CUST_NUM = "Customer id must be numeric.";
    private static final String MSG_CUST_NF = "Customer does not exist.";
    private static final String MSG_ACCT_DUP = "Account id already exists.";
    private static final String MSG_CRLIM_BAD = "Credit limit is not a valid amount.";
    private static final String MSG_CSLIM_BAD = "Cash limit is not a valid amount.";
    private static final String MSG_CS_GT_CR = "Cash limit cannot exceed credit limit.";
    private static final String MSG_OPEN_BAD = "Open date invalid, use YYYY-MM-DD.";
    private static final String MSG_GROUP_REQ = "Disclosure group id is required.";
    private static final String MSG_OK = "Account opened successfully.";
    private static final String MSG_WRITE_ERR = "Error writing the account file.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";

    @Mock private AppService appService;

    private OcacctaService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OcacctaService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commareaWithContext(int context) {
        OcacctaFields helper = new OcacctaFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(context));
    }

    /** Stubs receiveMap to populate the MACCTAAI input fields captured from production code. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OcacctaFields> populate) {
        doAnswer(
                        inv -> {
                            OcacctaFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MACCTAA"), any());
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MACCTAA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OcacctaFields out = (OcacctaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertEquals(-1, out.getAcctidl());
        assertThat(out.getTrnnameo().trim()).isEqualTo("OROA");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCACCTA");
        verify(appService).returnTransid(eq("OROA"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq("MACCTAA"), any(), any(), eq(true), eq(false), eq(true));
        verify(appService).returnTransid(eq("OROA"), any(), eq(692));
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
                .sendMap(
                        anyString(),
                        any(),
                        any(),
                        org.mockito.ArgumentMatchers.anyBoolean(),
                        org.mockito.ArgumentMatchers.anyBoolean(),
                        org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void mainLine_pf4Pressed_clearsAndResendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MACCTAA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OcacctaFields out = (OcacctaFields) sendMapOut.getValue();
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
                        eq("MACCTAA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcacctaFields out = (OcacctaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-OPEN-ACCT happy path ─────────────────────────

    @Test
    void mainLine_openAccount_allFieldsValid_writesAccountAndCrossReference() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("00000012345");
                    f.setCustidi("000012345");
                    f.setAccrlimi("1,000.00");
                    f.setAccslimi("500");
                    f.setAcopeni("2026-01-15");
                    f.setAcgrpi("STD001");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0); // customer found
                            return null;
                        })
                .when(appService)
                .readFile(eq(CUSTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(13); // account not found -> id is unique
                            return null;
                        })
                .when(appService)
                .readFile(eq(ACCTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0); // write ok
                            return null;
                        })
                .when(appService)
                .writeFile(eq(ACCTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0); // xref write ok
                            return null;
                        })
                .when(appService)
                .writeFile(eq(XREFFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> acctWritten = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeFile(eq(ACCTFILE), acctWritten.capture(), anyString(), anyInt());
        OcacctaFields acct = (OcacctaFields) acctWritten.getValue();
        assertEquals(12345L, acct.getAcId());
        assertThat(acct.getAcActiveStatus().trim()).isEqualTo("Y");
        assertEquals(0, acct.getAcCurrBal().compareTo(BigDecimal.ZERO));
        assertEquals(0, acct.getAcCreditLimit().compareTo(new BigDecimal("1000.00")));
        assertEquals(0, acct.getAcCashLimit().compareTo(new BigDecimal("500.00")));

        ArgumentCaptor<Object> xrefWritten = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeFile(eq(XREFFILE), xrefWritten.capture(), anyString(), anyInt());
        OcacctaFields xref = (OcacctaFields) xrefWritten.getValue();
        assertEquals(String.format("%016d", 12345), xref.getXrCardNum());
        assertEquals(12345L, xref.getXrAcctId());
        assertEquals(12345, xref.getXrCustId());

        // Final screen always shows the OK message: 1000-SEND-INITIAL unconditionally
        // resets ERRMSGO to WS-M-PROMPT, then 2100-OPEN-ACCT overwrites it with WS-M-OK —
        // this happens in the COBOL source too (see 2100-OPEN-ACCT), not a convert gap.
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(2))
                .sendMap(
                        eq("MACCTAA"),
                        sendMapOut.capture(),
                        any(),
                        org.mockito.ArgumentMatchers.anyBoolean(),
                        org.mockito.ArgumentMatchers.anyBoolean(),
                        org.mockito.ArgumentMatchers.anyBoolean());
        OcacctaFields lastScreen = (OcacctaFields) sendMapOut.getAllValues().get(1);
        assertThat(lastScreen.getErrmsgo().trim()).isEqualTo(MSG_OK);
    }

    @Test
    void mainLine_openAccount_blankOpenDateDefaultsToToday() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("12345");
                    f.setCustidi("123456789");
                    f.setAccrlimi("100.00");
                    f.setAccslimi("50.00");
                    f.setAcopeni(" ");
                    f.setAcgrpi("STD001");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(CUSTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(13);
                            return null;
                        })
                .when(appService)
                .readFile(eq(ACCTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .writeFile(anyString(), any(), anyString(), anyInt());
        // Simulate the OUDATE subroutine: KD-DATE-OUT occupies offset 4..14 of the
        // 26-byte KDATE-PARM group (see OCACCTA_WS.xml); KD-STATUS (offset 24, len 2)
        // stays "00" (already set before the call) so validation still passes.
        doAnswer(
                        inv -> {
                            Object[] params = inv.getArgument(1);
                            String parm = String.valueOf(params[0]);
                            params[0] = parm.substring(0, 14) + "2026-09-22" + parm.substring(24);
                            return null;
                        })
                .when(appService)
                .callProgram(eq("OUDATE"), any());

        service.mainLine(appService);

        ArgumentCaptor<Object> acctWritten = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeFile(eq(ACCTFILE), acctWritten.capture(), anyString(), anyInt());
        OcacctaFields acct = (OcacctaFields) acctWritten.getValue();
        assertThat(acct.getAcOpenDate().trim()).isEqualTo("2026-09-22");
    }

    // ───────────────────────── 5000-VALIDATE-ALL edge cases ─────────────────────────

    @Test
    void mainLine_openAccount_accountIdNonNumeric_rejectsBeforeAnyFileAccess() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("ABCDEFGHIJK"));

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_ACCT_NUM, -1);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_openAccount_customerIdNonNumeric_rejectsWithCustomerNumericMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("12345");
                    f.setCustidi("ABCDEFGHI");
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_CUST_NUM, -1);
    }

    @Test
    void mainLine_openAccount_creditLimitMalformed_rejectsWithCreditLimitMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("12345");
                    f.setCustidi("123456789");
                    f.setAccrlimi("12.34.56"); // two decimal points -> invalid
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_CRLIM_BAD, -1);
    }

    @Test
    void mainLine_openAccount_creditLimitTooManyIntegerDigits_rejectsWithCreditLimitMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("12345");
                    f.setCustidi("123456789");
                    f.setAccrlimi("12345678901"); // 11 integer digits > 10
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_CRLIM_BAD, -1);
    }

    @Test
    void mainLine_openAccount_cashLimitMalformed_rejectsWithCashLimitMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("12345");
                    f.setCustidi("123456789");
                    f.setAccrlimi("100.00");
                    f.setAccslimi("1.234"); // 3 fraction digits > 2
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_CSLIM_BAD, -1);
    }

    @Test
    void mainLine_openAccount_cashLimitExceedsCreditLimit_rejectsWithRuleMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("12345");
                    f.setCustidi("123456789");
                    f.setAccrlimi("100.00");
                    f.setAccslimi("200.00");
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_CS_GT_CR, -1);
    }

    @Test
    void mainLine_openAccount_openDateInvalid_rejectsWithOpenDateMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("12345");
                    f.setCustidi("123456789");
                    f.setAccrlimi("100.00");
                    f.setAccslimi("50.00");
                    f.setAcopeni("2026-13-99");
                });
        // Simulate OUDATE rejecting the date: KD-STATUS is the last 2 bytes of the
        // 26-byte KDATE-PARM group.
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

        assertFinalErrorMessage(MSG_OPEN_BAD, -1);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_openAccount_groupIdBlank_rejectsWithGroupRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("12345");
                    f.setCustidi("123456789");
                    f.setAccrlimi("100.00");
                    f.setAccslimi("50.00");
                    f.setAcopeni("2026-01-15");
                    f.setAcgrpi("          ");
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_GROUP_REQ, -1);
    }

    @Test
    void mainLine_openAccount_customerNotFound_rejectsWithCustomerNotFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("12345");
                    f.setCustidi("123456789");
                    f.setAccrlimi("100.00");
                    f.setAccslimi("50.00");
                    f.setAcopeni("2026-01-15");
                    f.setAcgrpi("STD001");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(13); // customer not found
                            return null;
                        })
                .when(appService)
                .readFile(eq(CUSTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_CUST_NF, -1);
        verify(appService, never()).readFile(eq(ACCTFILE), any(), anyString(), anyInt());
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_openAccount_accountIdAlreadyExists_rejectsWithAccountDupMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("12345");
                    f.setCustidi("123456789");
                    f.setAccrlimi("100.00");
                    f.setAccslimi("50.00");
                    f.setAcopeni("2026-01-15");
                    f.setAcgrpi("STD001");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0); // customer found
                            return null;
                        })
                .when(appService)
                .readFile(eq(CUSTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0); // account found -> duplicate
                            return null;
                        })
                .when(appService)
                .readFile(eq(ACCTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_ACCT_DUP, -1);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_receiveMapFail_treatsInputAsBlankAndFailsAccountValidation() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        nextResp.set(36); // DFHRESP(MAPFAIL)
        // No receiveMap stub: fields stay at their post-fillLowValues default.

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_ACCT_NUM, -1);
    }

    // ───────────────────────── 3500/3700-WRITE error branches ─────────────────────────

    @Test
    void mainLine_writeAccount_duplicateKeyRaceCondition_rejectsWithAccountDupMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("12345");
                    f.setCustidi("123456789");
                    f.setAccrlimi("100.00");
                    f.setAccslimi("50.00");
                    f.setAcopeni("2026-01-15");
                    f.setAcgrpi("STD001");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(CUSTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(13);
                            return null;
                        })
                .when(appService)
                .readFile(eq(ACCTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(14); // DFHRESP(DUPREC) on the write itself
                            return null;
                        })
                .when(appService)
                .writeFile(eq(ACCTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_ACCT_DUP, -1, false);
        verify(appService).writeFile(eq(ACCTFILE), any(), anyString(), anyInt());
        verify(appService, never()).writeFile(eq(XREFFILE), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_writeAccount_unexpectedFileError_rejectsWithWriteErrorMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("12345");
                    f.setCustidi("123456789");
                    f.setAccrlimi("100.00");
                    f.setAccslimi("50.00");
                    f.setAcopeni("2026-01-15");
                    f.setAcgrpi("STD001");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(CUSTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(13);
                            return null;
                        })
                .when(appService)
                .readFile(eq(ACCTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(99); // unexpected error
                            return null;
                        })
                .when(appService)
                .writeFile(eq(ACCTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_WRITE_ERR, -1, false);
        verify(appService).writeFile(eq(ACCTFILE), any(), anyString(), anyInt());
        verify(appService, never()).writeFile(eq(XREFFILE), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_writeCrossReference_fails_stillReportsSuccessPerCobolOverwrite() {
        // Per COBOL 2100-OPEN-ACCT: even when 3700-WRITE-XREF sets an error message,
        // the subsequent PERFORM 1000-SEND-INITIAL resets ERRMSGO to WS-M-PROMPT and
        // then MOVE WS-M-OK TO ERRMSGO overwrites it again — the xref error is never
        // shown to the operator. This is original COBOL behavior, not a convert gap.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("12345");
                    f.setCustidi("123456789");
                    f.setAccrlimi("100.00");
                    f.setAccslimi("50.00");
                    f.setAcopeni("2026-01-15");
                    f.setAcgrpi("STD001");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(CUSTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(13);
                            return null;
                        })
                .when(appService)
                .readFile(eq(ACCTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .writeFile(eq(ACCTFILE), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(99); // unexpected xref write error
                            return null;
                        })
                .when(appService)
                .writeFile(eq(XREFFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(2))
                .sendMap(
                        eq("MACCTAA"),
                        sendMapOut.capture(),
                        any(),
                        org.mockito.ArgumentMatchers.anyBoolean(),
                        org.mockito.ArgumentMatchers.anyBoolean(),
                        org.mockito.ArgumentMatchers.anyBoolean());
        OcacctaFields lastScreen = (OcacctaFields) sendMapOut.getAllValues().get(1);
        assertThat(lastScreen.getErrmsgo().trim()).isEqualTo(MSG_OK);
        verify(appService).writeFile(eq(XREFFILE), any(), anyString(), anyInt());
    }

    // ───────────────────────── 9500-ABEND-RTN ─────────────────────────

    @Test
    void mainLine_customerFileReadError_sendsAbendTextAndReturnsProgram() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("12345");
                    f.setCustidi("123456789");
                    f.setAccrlimi("100.00");
                    f.setAccslimi("50.00");
                    f.setAcopeni("2026-01-15");
                    f.setAcgrpi("STD001");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(99); // unexpected CUSTFILE error -> 9500-ABEND-RTN
                            return null;
                        })
                .when(appService)
                .readFile(eq(CUSTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        verify(appService).sendText(anyString(), eq(true), eq(true));
        verify(appService).returnProgram();
    }

    @Test
    void mainLine_customerFileReadError_convertGap_sendTextShouldCarryMessageText() {
        // CONVERT-GAP: COBOL's 9500-ABEND-RTN does
        //   EXEC CICS SEND TEXT FROM(WS-MSG-TEXT) ...
        // i.e. it sends the literal error message text. The converted
        // abendWithFileError() instead calls
        //   ctx.appService.sendText(String.valueOf(ctx.f), true, true)
        // which stringifies the WHOLE OcacctaFields accessor object (its default
        // Object.toString(), e.g. "com.generated...OcacctaFields@1a2b3c") instead of
        // ctx.f.getWsMsgText(). Expected per COBOL ground truth: the text sent should
        // contain the WS-MSG-TEXT message. This test intentionally fails against the
        // current Java to flag the gap — it is not a bug in the test.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("12345");
                    f.setCustidi("123456789");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .readFile(eq(CUSTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        verify(appService)
                .sendText(
                        org.mockito.ArgumentMatchers.contains("unrecoverable file error"),
                        eq(true),
                        eq(true));
    }

    @Test
    void mainLine_customerFileReadError_convertGap_returnTransidShouldNotFollowAbend() {
        // CONVERT-GAP: in COBOL, 9500-ABEND-RTN issues EXEC CICS RETURN (no TRANSID),
        // which ends the pseudo-conversational task immediately — 9000-RETURN is never
        // reached afterward. The converted Java abendWithFileError() calls
        // appService.returnProgram() but does not stop the Java call stack, so
        // runMainProgram() falls through to returnToCics() and returnTransid() fires
        // anyway. Expected (per COBOL ground truth): returnTransid must NOT be called
        // after an abend. This test intentionally fails against current Java to flag
        // the gap — it is not a bug in the test.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("12345");
                    f.setCustidi("123456789");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .readFile(eq(CUSTFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        verify(appService, never()).returnTransid(anyString(), any(), anyInt());
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcaccta() {
        assertEquals("OCACCTA", service.getProgramName());
    }

    @Test
    void getTransId_returnsOroa() {
        assertEquals("OROA", service.getTransId());
    }

    // ───────────────────────── shared assertion helper ─────────────────────────

    private void assertFinalErrorMessage(String expectedMessage, int expectedAcctidl) {
        assertFinalErrorMessage(expectedMessage, expectedAcctidl, true);
    }

    private void assertFinalErrorMessage(
            String expectedMessage, int expectedAcctidl, boolean expectNoWriteAttempted) {
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTAA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcacctaFields out = (OcacctaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(expectedMessage);
        assertEquals(expectedAcctidl, out.getAcctidl());
        if (expectNoWriteAttempted) {
            verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
        }
    }
}

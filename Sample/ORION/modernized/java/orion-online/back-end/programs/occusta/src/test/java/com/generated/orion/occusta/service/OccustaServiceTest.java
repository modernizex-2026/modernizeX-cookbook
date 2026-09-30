package com.generated.orion.occusta.service;

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
import com.generated.orion.occusta.accessor.OccustaFields;
import com.generated.orion.occusta.model.WorkingStorage;

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
 * Unit tests for OccustaService, generated from COBOL program OCCUSTA (ORION-CCMS customer add,
 * on-line, following the OCACCTV golden skeleton). All business logic is private and is exercised
 * solely through the public {@link OccustaService#mainLine(AppService)} entry point, driven by an
 * {@link AppService} mock that emulates CICS SEND/RECEIVE/WRITE/XCTL/RETURN.
 *
 * <p>Convert-gap check: OCCUSTA's Java service mirrors every COBOL paragraph 1:1
 * (0000-MAIN..9000-RETURN), including the DFHRESP(NORMAL/DUPREC/MAPFAIL) numeric constants
 * (0/14/36) and the field-by-field validation order (5010..5070). No behavioral divergence was
 * found against the COBOL ground truth, so no CONVERT-GAP test is included here.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OccustaServiceTest {

    private static final String MAP_NAME = "MCUSTAA";
    private static final String MENU_PGM = "OCMENU";

    private static final String MSG_PROMPT = "Enter new customer details and press ENTER.";
    private static final String MSG_INVALID_KEY_FALLBACK = "Invalid key pressed. Please try again.";
    private static final String MSG_CUST_NUM = "Customer id must be numeric.";
    private static final String MSG_FNAME_REQ = "First name is required.";
    private static final String MSG_LNAME_REQ = "Last name is required.";
    private static final String MSG_ADDR_REQ = "Address line 1 is required.";
    private static final String MSG_CITY_REQ = "City is required.";
    private static final String MSG_SSN_NUM = "SSN must be nine numeric digits.";
    private static final String MSG_FICO_NUM = "FICO score must be numeric.";
    private static final String MSG_FICO_RNG = "FICO score must be 300 through 850.";
    private static final String MSG_CUST_DUP = "Customer id already exists.";
    private static final String MSG_OK = "Customer added successfully.";
    private static final String MSG_WRITE_ERR = "Error writing the customer file.";

    @Mock private AppService appService;

    private OccustaService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OccustaService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-23", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commareaWithContext(int context) {
        OccustaFields helper = new OccustaFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(context));
    }

    /** Stubs receiveMap to populate the MCUSTAAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(Consumer<OccustaFields> populate) {
        doAnswer(
                        inv -> {
                            OccustaFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    /** A fully valid customer entry (COBOL ground truth: passes all 5010..5070 checks). */
    private void validCustomer(OccustaFields f) {
        f.setCustidi("000012345");
        f.setCufnami("JOHN");
        f.setCulnami("SMITH");
        f.setCuaddri("123 MAIN ST");
        f.setCucityi("SPRINGFIELD");
        f.setCussni("123456789");
        f.setCuficoi("700");
    }

    /** Sends the ENTER key with the given field overrides applied on top of a valid customer. */
    private void enterWithFields(Consumer<OccustaFields> overrides) {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    validCustomer(f);
                    overrides.accept(f);
                });
    }

    private OccustaFields captureFinalSendMap() {
        ArgumentCaptor<Object> out = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), out.capture(), any(), eq(false), eq(false), eq(true));
        return (OccustaFields) out.getValue();
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OccustaFields out = (OccustaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo("OROC");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCCUSTA");
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-23");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        verify(appService).returnTransid(eq("OROC"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        verify(appService).returnTransid(eq("OROC"), any(), eq(692));
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

        OccustaFields decoded = new OccustaFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo("OCCUSTA");
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo("OROC");
        assertEquals(0, decoded.getCaPgmContext());
    }

    @Test
    void mainLine_pf4Pressed_clearsAndResendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OccustaFields out = (OccustaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage_andSkipsReceive() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        OccustaFields dataOnlyScreen = captureFinalSendMap();
        assertThat(dataOnlyScreen.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY_FALLBACK);
        verify(appService, never()).receiveMap(anyString(), any());
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    // ───────────────────────── 2100-ADD-CUST / MAPFAIL ─────────────────────────

    @Test
    void mainLine_enterKey_mapfailResponse_fillsLowValuesAndFailsCustomerIdValidation() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        // DFHRESP(MAPFAIL) = 36: production sets WS-RESP-CD from EIBRESP right after
        // RECEIVE and, on MAPFAIL, re-initializes MCUSTAAI to LOW-VALUES before validating.
        doAnswer(
                        inv -> {
                            nextResp.set(36);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());

        service.mainLine(appService);

        OccustaFields out = captureFinalSendMap();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CUST_NUM);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    // ───────────────────────── 5010-VAL-CUST ─────────────────────────

    @Test
    void mainLine_enterKey_customerIdBlank_showsCustNumMessage() {
        enterWithFields(f -> f.setCustidi("         "));

        service.mainLine(appService);

        assertThat(captureFinalSendMap().getErrmsgo().trim()).isEqualTo(MSG_CUST_NUM);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_customerIdNonNumeric_showsCustNumMessage() {
        enterWithFields(f -> f.setCustidi("ABC123456"));

        service.mainLine(appService);

        assertThat(captureFinalSendMap().getErrmsgo().trim()).isEqualTo(MSG_CUST_NUM);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    // ───────────────────────── 5020-VAL-FNAME ─────────────────────────

    @Test
    void mainLine_enterKey_firstNameBlank_showsFnameReqMessage() {
        enterWithFields(f -> f.setCufnami(" "));

        service.mainLine(appService);

        assertThat(captureFinalSendMap().getErrmsgo().trim()).isEqualTo(MSG_FNAME_REQ);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_firstNameAllLowValues_showsFnameReqMessage() {
        enterWithFields(f -> f.fillLowValues("CUFNAMI"));

        service.mainLine(appService);

        assertThat(captureFinalSendMap().getErrmsgo().trim()).isEqualTo(MSG_FNAME_REQ);
    }

    // ───────────────────────── 5030-VAL-LNAME ─────────────────────────

    @Test
    void mainLine_enterKey_lastNameBlank_showsLnameReqMessage() {
        enterWithFields(f -> f.setCulnami(" "));

        service.mainLine(appService);

        assertThat(captureFinalSendMap().getErrmsgo().trim()).isEqualTo(MSG_LNAME_REQ);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    // ───────────────────────── 5040-VAL-ADDR ─────────────────────────

    @Test
    void mainLine_enterKey_addressBlank_showsAddrReqMessage() {
        enterWithFields(f -> f.setCuaddri(" "));

        service.mainLine(appService);

        assertThat(captureFinalSendMap().getErrmsgo().trim()).isEqualTo(MSG_ADDR_REQ);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    // ───────────────────────── 5050-VAL-CITY ─────────────────────────

    @Test
    void mainLine_enterKey_cityBlank_showsCityReqMessage() {
        enterWithFields(f -> f.setCucityi(" "));

        service.mainLine(appService);

        assertThat(captureFinalSendMap().getErrmsgo().trim()).isEqualTo(MSG_CITY_REQ);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    // ───────────────────────── 5060-VAL-SSN ─────────────────────────

    @Test
    void mainLine_enterKey_ssnNonNumeric_showsSsnNumMessage() {
        enterWithFields(f -> f.setCussni("12A456789"));

        service.mainLine(appService);

        assertThat(captureFinalSendMap().getErrmsgo().trim()).isEqualTo(MSG_SSN_NUM);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_ssnWrongDigitCount_showsSsnNumMessage() {
        enterWithFields(f -> f.setCussni("1234 6789"));

        service.mainLine(appService);

        assertThat(captureFinalSendMap().getErrmsgo().trim()).isEqualTo(MSG_SSN_NUM);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    // ───────────────────────── 5070-VAL-FICO ─────────────────────────

    @Test
    void mainLine_enterKey_ficoNonNumeric_showsFicoNumMessage() {
        enterWithFields(f -> f.setCuficoi("7A0"));

        service.mainLine(appService);

        assertThat(captureFinalSendMap().getErrmsgo().trim()).isEqualTo(MSG_FICO_NUM);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_ficoBelowRange_showsFicoRngMessage() {
        enterWithFields(f -> f.setCuficoi("250"));

        service.mainLine(appService);

        assertThat(captureFinalSendMap().getErrmsgo().trim()).isEqualTo(MSG_FICO_RNG);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_ficoAboveRange_showsFicoRngMessage() {
        enterWithFields(f -> f.setCuficoi("900"));

        service.mainLine(appService);

        assertThat(captureFinalSendMap().getErrmsgo().trim()).isEqualTo(MSG_FICO_RNG);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    // ───────────────────────── 3500-WRITE-CUST ─────────────────────────

    @Test
    void mainLine_enterKey_validInput_writeSucceeds_showsOkMessageAndBuildsCustRecord() {
        enterWithFields(f -> {});
        doAnswer(
                        inv -> {
                            nextResp.set(0); // DFHRESP(NORMAL)
                            return null;
                        })
                .when(appService)
                .writeFile(anyString(), any(), anyString(), eq(0));

        service.mainLine(appService);

        ArgumentCaptor<Object> writtenRecord = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeFile(anyString(), writtenRecord.capture(), eq("12345"), eq(0));
        OccustaFields written = (OccustaFields) writtenRecord.getValue();
        assertThat(written.getCuId()).isEqualTo(12345);
        assertThat(written.getCuFirstName().trim()).isEqualTo("JOHN");
        assertThat(written.getCuLastName().trim()).isEqualTo("SMITH");
        assertThat(written.getCuSsn()).isEqualTo(123456789);
        assertThat(written.getCuFicoScore()).isEqualTo(700);

        // WS-WRITE-OK: 1000-SEND-INITIAL (erase) followed by 8100-SEND-DATAONLY (dataonly) with
        // WS-M-OK.
        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        OccustaFields finalScreen = captureFinalSendMap();
        assertThat(finalScreen.getErrmsgo().trim()).isEqualTo(MSG_OK);
        assertThat(finalScreen.getCaCustId()).isEqualTo(12345);
    }

    @Test
    void mainLine_enterKey_writeDuplicate_showsCustDupMessage() {
        enterWithFields(f -> {});
        doAnswer(
                        inv -> {
                            nextResp.set(14); // DFHRESP(DUPREC)
                            return null;
                        })
                .when(appService)
                .writeFile(anyString(), any(), anyString(), eq(0));

        service.mainLine(appService);

        assertThat(captureFinalSendMap().getErrmsgo().trim()).isEqualTo(MSG_CUST_DUP);
        // WS-WRITE-BAD path: only 8100-SEND-DATAONLY runs, no second (erase) SEND from
        // 1000-SEND-INITIAL.
        verify(appService, times(1))
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void mainLine_enterKey_writeOtherError_showsWriteErrMessage() {
        enterWithFields(f -> {});
        doAnswer(
                        inv -> {
                            nextResp.set(99); // any other non-NORMAL, non-DUPREC response
                            return null;
                        })
                .when(appService)
                .writeFile(anyString(), any(), anyString(), eq(0));

        service.mainLine(appService);

        assertThat(captureFinalSendMap().getErrmsgo().trim()).isEqualTo(MSG_WRITE_ERR);
        verify(appService, times(1))
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
    }
}

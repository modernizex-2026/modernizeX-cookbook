package com.generated.orion.occustv.service;

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
import com.generated.orion.occustv.accessor.OccustvFields;
import com.generated.orion.occustv.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OccustvService, generated from COBOL program OCCUSTV ("Customer View" on-line
 * inquiry, modeled after the OCACCTV golden skeleton). All business logic is private and is
 * exercised solely through the public {@link OccustvService#mainLine(AppService)} entry point,
 * driven by an {@link AppService} mock that emulates CICS SEND/RECEIVE/READ/XCTL/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OccustvServiceTest {

    private static final String CUSTFILE = "CUSTFILE";
    private static final String MAP_NAME = "MCUSTVA";
    private static final String MENU_PGM = "OCMENU";

    private static final String MSG_PROMPT = "Enter a customer id and press ENTER.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_CUST_FOUND = "Customer details displayed.";
    private static final String MSG_CUST_NOTFND = "Customer not found - check the id and retry.";
    private static final String MSG_ID_REQUIRED = "Customer id is required.";
    private static final String MSG_ID_NOTNUM = "Customer id must be numeric.";
    private static final String HDR_TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM";

    @Mock private AppService appService;

    private OccustvService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OccustvService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-23", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commareaWithContext(int context) {
        OccustvFields helper = new OccustvFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(context));
    }

    /** Stubs receiveMap to populate the MCUSTVAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OccustvFields> populate) {
        doAnswer(
                        inv -> {
                            OccustvFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OccustvFields out = (OccustvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORUV");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCCUSTV");
        assertThat(out.getTitleo().trim()).isEqualTo(HDR_TITLE);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-23");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        verify(appService).returnTransid(eq("ORUV"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(false));
        verify(appService).returnTransid(eq("ORUV"), any(), eq(692));
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

        OccustvFields decoded = new OccustvFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo("OCCUSTV");
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo("ORUV");
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
        OccustvFields out = (OccustvFields) sendMapOut.getValue();
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
        OccustvFields dataOnlyScreen = (OccustvFields) dataOnlyOut.getValue();
        assertThat(dataOnlyScreen.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-READ-AND-SHOW / 2200-EDIT-CUST-ID ─────────────────────────

    @Test
    void mainLine_enterKey_custIdBlank_showsRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("         "));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OccustvFields out = (OccustvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ID_REQUIRED);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_custIdAllLowValues_showsRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.fillLowValues("CUSTIDI"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OccustvFields out = (OccustvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ID_REQUIRED);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_custIdNonNumeric_showsNotNumericMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("12A456789"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OccustvFields out = (OccustvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ID_NOTNUM);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_custIdNumericWithLeadingSpaces_readsUsingParsedId() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("     1234"));
        doAnswer(
                        inv -> {
                            nextResp.set(
                                    13); // DFHRESP(NOTFND) - only interested in the key used for
                            // the read
                            return null;
                        })
                .when(appService)
                .readFile(eq(CUSTFILE), any(), eq("1234"), anyInt());

        service.mainLine(appService);

        verify(appService).readFile(eq(CUSTFILE), any(), eq("1234"), anyInt());
    }

    @Test
    void mainLine_enterKey_customerFound_populatesDetailAndClearsErrorMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("000012345"));
        doAnswer(
                        inv -> {
                            OccustvFields into = inv.getArgument(1);
                            into.setCuFirstName("JOHN");
                            into.setCuLastName("SMITH");
                            into.setCuAddrLine1("100 MAIN ST");
                            into.setCuAddrCity("SPRINGFIELD");
                            into.setCuPhone1("555-1212");
                            into.setCuFicoScore(725);
                            nextResp.set(0); // DFHRESP(NORMAL)
                            return null;
                        })
                .when(appService)
                .readFile(eq(CUSTFILE), any(), eq("12345"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OccustvFields out = (OccustvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CUST_FOUND);
        assertThat(out.getCustido()).isEqualTo(String.format("%09d", 12345));
        assertThat(out.getCunameo().trim()).isEqualTo("JOHN SMITH");
        assertThat(out.getCuaddro().trim()).isEqualTo("100 MAIN ST");
        assertThat(out.getCucityo().trim()).isEqualTo("SPRINGFIELD");
        assertThat(out.getCuphoneo().trim()).isEqualTo("555-1212");
        assertThat(out.getCuficoo()).isEqualTo(String.format("%03d", 725));
    }

    @Test
    void mainLine_enterKey_customerFound_fullNameUsesFirstTokenOfEachNamePart() {
        // 4100-FORMAT-NAME: STRING ... DELIMITED BY SPACE keeps only the first token of
        // CU-FIRST-NAME and CU-LAST-NAME (e.g. "MARY ANN" + "VAN DYKE" -> "MARY VAN").
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("000067890"));
        doAnswer(
                        inv -> {
                            OccustvFields into = inv.getArgument(1);
                            into.setCuFirstName("MARY ANN");
                            into.setCuLastName("VAN DYKE");
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(CUSTFILE), any(), eq("67890"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OccustvFields out = (OccustvFields) sendMapOut.getValue();
        assertThat(out.getCunameo().trim()).isEqualTo("MARY VAN");
    }

    @Test
    void mainLine_enterKey_customerNotFound_showsNotFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("000099999"));
        doAnswer(
                        inv -> {
                            nextResp.set(13); // DFHRESP(NOTFND)
                            return null;
                        })
                .when(appService)
                .readFile(eq(CUSTFILE), any(), eq("99999"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OccustvFields out = (OccustvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CUST_NOTFND);
    }

    @Test
    void mainLine_enterKey_readFileUnexpectedError_finalMessageIsStillNotFoundPerCobolOverwrite() {
        // Per COBOL 3000-READ-CUST: WHEN OTHER sets an interim ERRMSGO ("Error reading the
        // customer file.") but never sets REC-FOUND; 2100-READ-AND-SHOW's WHEN OTHER branch
        // then unconditionally overwrites ERRMSGO with WS-M-CUST-NOTFND. So the interim
        // message is never shown to the operator, in COBOL and in the converted Java alike
        // -- not a convert gap.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("000011111"));
        doAnswer(
                        inv -> {
                            nextResp.set(99); // unexpected error
                            return null;
                        })
                .when(appService)
                .readFile(eq(CUSTFILE), any(), eq("11111"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OccustvFields out = (OccustvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CUST_NOTFND);
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOccustv() {
        assertEquals("OCCUSTV", service.getProgramName());
    }

    @Test
    void getTransId_returnsOruv() {
        assertEquals("ORUV", service.getTransId());
    }
}

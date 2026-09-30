package com.generated.orion.ocmenu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.ocmenu.accessor.OcmenuFields;
import com.generated.orion.ocmenu.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for OcmenuService, generated from COBOL program OCMENU (ORION-CCMS main menu, on-line
 * CICS pseudo-conversational). All business logic is private and is exercised solely through the
 * public {@link OcmenuService#mainLine(AppService)} entry point, driven by an {@link AppService}
 * mock that emulates CICS SEND/RECEIVE/XCTL/RETURN.
 *
 * <p>Convert-gap check: OCMENU's Java service mirrors every COBOL paragraph 1:1
 * (0000-MAIN..9000-RETURN, including CA-FIRST-ENTER via CA-PGM-CONTEXT, the DFHPF3 / DFHENTER /
 * WHEN-OTHER EIBAID dispatch, and the OPTIONI NUMERIC 1-10 dispatch table); no behavioral
 * divergence was found against the COBOL ground truth, so no CONVERT-GAP test is included here.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcmenuServiceTest {

    private static final String MAP_NAME = "MMENUA";
    private static final String TRAN_ID = "ORMN";
    private static final String PGM_NAME = "OCMENU";
    private static final String SGNON_PGM = "OCSGNON";
    private static final int COMMAREA_LENGTH = 692;

    private static final String MSG_PROMPT = "Select an option (1-10) and press ENTER.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_INVALID_OPTION = "Invalid option.";
    private static final String HDR_TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM";

    @Mock private AppService appService;

    private OcmenuService service;

    @BeforeEach
    void setUp() {
        service = new OcmenuService();
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commarea(int context) {
        OcmenuFields helper = new OcmenuFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commarea(context));
    }

    /** Stubs receiveMap to populate the MMENUAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OcmenuFields> populate) {
        doAnswer(
                        inv -> {
                            OcmenuFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    private void givenFormatTimeStub() {
        when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    // ───────────────────────── 0000-MAIN ─────────────────────────

    @Test
    void mainLine_eibcalenZero_xctlsToSignonProgramFirst() {
        when(appService.getEibcalen()).thenReturn(0);
        givenFormatTimeStub();

        service.mainLine(appService);

        verify(appService).xctl(argThat(s -> s.trim().equals(SGNON_PGM)), isNull(), eq(0));
    }

    @Test
    void mainLine_firstEnter_contextZero_sendsInitialScreenWithPrompt() {
        givenPseudoConversation(0);
        givenFormatTimeStub();

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcmenuFields out = (OcmenuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo(TRAN_ID);
        assertThat(out.getPgmnameo().trim()).isEqualTo(PGM_NAME);
        assertThat(out.getTitleo().trim()).isEqualTo(HDR_TITLE);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-22");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        verify(appService).returnTransid(eq(TRAN_ID), any(), eq(COMMAREA_LENGTH));
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_signsOffToSignonProgram() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        ArgumentCaptor<Object> commareaCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .xctl(
                        argThat(s -> s.trim().equals(SGNON_PGM)),
                        commareaCaptor.capture(),
                        eq(COMMAREA_LENGTH));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());

        OcmenuFields decoded = new OcmenuFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertEquals(0, decoded.getCaPgmContext());
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        givenFormatTimeStub();
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
        OcmenuFields dataOnlyScreen = (OcmenuFields) dataOnlyOut.getValue();
        assertThat(dataOnlyScreen.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-DISPATCH ─────────────────────────

    @ParameterizedTest
    @CsvSource({
        "01,OCACCTV",
        "02,OCACCTU",
        "03,OCCARDL",
        "04,OCCARDV",
        "05,OCCARDU",
        "06,OCCUSTV",
        "07,OCTRANL",
        "08,OCTRANA",
        "09,OCBILLP",
        "10,OCRPTMN"
    })
    void mainLine_enterKey_validOption_xctlsToSelectedProgram(
            String optionInput, String expectedTargetPgm) {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setOptioni(optionInput));

        service.mainLine(appService);

        ArgumentCaptor<Object> commareaCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .xctl(
                        argThat(s -> s.trim().equals(expectedTargetPgm)),
                        commareaCaptor.capture(),
                        eq(COMMAREA_LENGTH));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());

        OcmenuFields decoded = new OcmenuFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo(PGM_NAME);
        assertEquals(0, decoded.getCaPgmContext());
    }

    @Test
    void mainLine_enterKey_nonNumericOption_showsInvalidOptionMessage() {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setOptioni("AB"));

        service.mainLine(appService);

        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(false));
        verify(appService)
                .sendMap(
                        eq(MAP_NAME),
                        dataOnlyOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcmenuFields out = (OcmenuFields) dataOnlyOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_OPTION);
        verify(appService, never()).xctl(anyString(), any(), anyInt());
    }

    @Test
    void mainLine_enterKey_optionOutOfRange_showsInvalidOptionMessage() {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setOptioni("11"));

        service.mainLine(appService);

        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME),
                        dataOnlyOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcmenuFields out = (OcmenuFields) dataOnlyOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_OPTION);
        verify(appService, never()).xctl(anyString(), any(), anyInt());
    }

    @Test
    void mainLine_enterKey_optionZero_showsInvalidOptionMessage() {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setOptioni("00"));

        service.mainLine(appService);

        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME),
                        dataOnlyOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcmenuFields out = (OcmenuFields) dataOnlyOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_OPTION);
    }

    @Test
    void mainLine_enterKey_optionBlank_showsInvalidOptionMessage() {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setOptioni("  "));

        service.mainLine(appService);

        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME),
                        dataOnlyOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcmenuFields out = (OcmenuFields) dataOnlyOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_OPTION);
    }

    // ───────────────────────── AppProgram metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcmenu() {
        assertThat(service.getProgramName()).isEqualTo(PGM_NAME);
    }

    @Test
    void getTransId_returnsOrmn() {
        assertThat(service.getTransId()).isEqualTo(TRAN_ID);
    }

    @Test
    void getButtonDefs_delegatesToBmsMetadata() {
        assertThat(service.getButtonDefs()).isNotNull();
    }

    @Test
    void getFieldMapping_delegatesToBmsMetadata() {
        assertThat(service.getFieldMapping(MAP_NAME)).isNotNull();
    }

    // ───────────────────────── 9000-RETURN ─────────────────────────

    @Test
    void mainLine_alwaysReturnsWithTransidAndCommarea() {
        givenPseudoConversation(0);
        givenFormatTimeStub();

        service.mainLine(appService);

        verify(appService).returnTransid(eq(TRAN_ID), any(), eq(COMMAREA_LENGTH));
    }
}

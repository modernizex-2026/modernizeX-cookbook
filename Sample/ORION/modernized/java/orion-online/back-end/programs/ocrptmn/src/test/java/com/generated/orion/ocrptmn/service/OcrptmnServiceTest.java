package com.generated.orion.ocrptmn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.ocrptmn.accessor.OcrptmnFields;
import com.generated.orion.ocrptmn.model.WorkingStorage;

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
 * Unit tests for OcrptmnService, generated from COBOL program OCRPTMN (ORION-CCMS reports/inquiry
 * menu, on-line CICS pseudo-conversational). All business logic is private and is exercised solely
 * through the public {@link OcrptmnService#mainLine(AppService)} entry point, driven by an {@link
 * AppService} mock that emulates CICS SEND/RECEIVE/XCTL/RETURN, mirroring the convention
 * established by the sibling OCMENU test.
 *
 * <p>Convert-gap check: OCRPTMN's Java service mirrors every COBOL paragraph 1:1
 * (0000-MAIN..9000-RETURN, including EIBCALEN=0 vs CA-PGM-CONTEXT re-entry, the DFHPF3 / DFHENTER /
 * WHEN-OTHER EIBAID dispatch, DFHRESP(MAPFAIL)=36 handling, and the OPTIONI NUMERIC 1-6 dispatch
 * table); no behavioral divergence was found against the COBOL ground truth, so no CONVERT-GAP test
 * is included.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcrptmnServiceTest {

    private static final String MAP_NAME = "MRPTMNA";
    private static final String TRAN_ID = "ORRM";
    private static final String PGM_NAME = "OCRPTMN";
    private static final String MENU_PGM = "OCMENU";
    private static final int COMMAREA_LENGTH = 692;

    private static final String MSG_PROMPT = "Select an inquiry (1-6) and press ENTER.";
    private static final String MSG_BAD_OPT = "Invalid option - choose 1 through 6.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String HDR_TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM";

    @Mock private AppService appService;

    private OcrptmnService service;

    @BeforeEach
    void setUp() {
        service = new OcrptmnService();
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commarea(int context) {
        OcrptmnFields helper = new OcrptmnFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commarea(context));
    }

    /** Stubs receiveMap to populate the MRPTMNAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OcrptmnFields> populate) {
        doAnswer(
                        inv -> {
                            OcrptmnFields f = inv.getArgument(1);
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

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_eibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);
        givenFormatTimeStub();

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OcrptmnFields out = (OcrptmnFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getOptionl()).isEqualTo((short) -1);
        assertThat(out.getTrnnameo().trim()).isEqualTo(TRAN_ID);
        assertThat(out.getPgmnameo().trim()).isEqualTo(PGM_NAME);
        assertThat(out.getTitleo().trim()).isEqualTo(HDR_TITLE);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-22");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        verify(appService, never()).xctl(anyString(), any(), anyInt());
        verify(appService).returnTransid(eq(TRAN_ID), any(), eq(COMMAREA_LENGTH));
    }

    @Test
    void mainLine_firstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);
        givenFormatTimeStub();

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_returnsToMainMenu() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        ArgumentCaptor<Object> commareaCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .xctl(
                        argThat(s -> s.trim().equals(MENU_PGM)),
                        commareaCaptor.capture(),
                        eq(COMMAREA_LENGTH));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());

        OcrptmnFields decoded = new OcrptmnFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo(PGM_NAME);
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo(TRAN_ID);
        assertEquals(0, decoded.getCaPgmContext());
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        // WHEN-OTHER performs 1000-SEND-INITIAL (erase=true) then 8100-SEND-DATAONLY
        // (erase=false); both calls share the same mutable field accessor, so only
        // the final (dataonly) call's content can be inspected.
        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), dataOnlyOut.capture(), any(), eq(false), eq(false), eq(true));
        OcrptmnFields dataOnlyScreen = (OcrptmnFields) dataOnlyOut.getValue();
        assertThat(dataOnlyScreen.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-DISPATCH / 2200-SELECT-TARGET ─────────────────────────

    @ParameterizedTest
    @CsvSource({
        "01,OCACCTL,ORLA",
        "02,OCCARDL,ORCL",
        "03,OCCUSTL,ORLC",
        "04,OCTRANL,ORTL",
        "05,OCSTMIN,ORSI",
        "06,OCANLIN,ORAN"
    })
    void mainLine_enterKey_validOption_xctlsToSelectedInquiryProgram(
            String optionInput, String expectedTargetPgm, String expectedTargetTran) {
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

        OcrptmnFields decoded = new OcrptmnFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo(PGM_NAME);
        assertThat(decoded.getCaToProgram().trim()).isEqualTo(expectedTargetPgm);
        assertThat(decoded.getCaToTranid().trim()).isEqualTo(expectedTargetTran);
        assertEquals(0, decoded.getCaPgmContext());
    }

    @Test
    void mainLine_enterKey_mapfailResp_treatsOptionAsBlankAndShowsBadOption() {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        when(appService.getEibresp()).thenReturn(36);
        // receiveMap intentionally left unstubbed (real CICS MAPFAIL delivers no data);
        // production code must fall back to LOW-VALUES on MRPTMNAI, i.e. OPTIONI blank/non-numeric.

        service.mainLine(appService);

        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), dataOnlyOut.capture(), any(), eq(false), eq(false), eq(true));
        OcrptmnFields out = (OcrptmnFields) dataOnlyOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_OPT);
        verify(appService, never()).xctl(anyString(), any(), anyInt());
    }

    @Test
    void mainLine_enterKey_nonNumericOption_showsBadOptionMessage() {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setOptioni("AB"));

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), dataOnlyOut.capture(), any(), eq(false), eq(false), eq(true));
        OcrptmnFields out = (OcrptmnFields) dataOnlyOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_OPT);
        verify(appService, never()).xctl(anyString(), any(), anyInt());
    }

    @Test
    void mainLine_enterKey_optionZero_showsBadOptionMessage() {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setOptioni("00"));

        service.mainLine(appService);

        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), dataOnlyOut.capture(), any(), eq(false), eq(false), eq(true));
        OcrptmnFields out = (OcrptmnFields) dataOnlyOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_OPT);
        verify(appService, never()).xctl(anyString(), any(), anyInt());
    }

    @Test
    void mainLine_enterKey_optionOutOfRange_showsBadOptionMessage() {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setOptioni("07"));

        service.mainLine(appService);

        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), dataOnlyOut.capture(), any(), eq(false), eq(false), eq(true));
        OcrptmnFields out = (OcrptmnFields) dataOnlyOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_OPT);
        verify(appService, never()).xctl(anyString(), any(), anyInt());
    }

    @Test
    void mainLine_enterKey_optionBlank_showsBadOptionMessage() {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setOptioni("  "));

        service.mainLine(appService);

        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), dataOnlyOut.capture(), any(), eq(false), eq(false), eq(true));
        OcrptmnFields out = (OcrptmnFields) dataOnlyOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_OPT);
    }

    // ───────────────────────── AppProgram metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcrptmn() {
        assertThat(service.getProgramName()).isEqualTo(PGM_NAME);
    }

    @Test
    void getTransId_returnsOrrm() {
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

    @Test
    void registerFsetFields_registersOptionAsFsetTrackedField() {
        // AppRunner is a concrete class (not an interface) whose constructor already
        // drives registerFsetFields for every registered program; a real instance
        // with no programs/fileDaos avoids fighting Mockito's inline mock maker
        // (unsupported on this JVM) while still exercising the real registration path.
        AppRunner runner = new AppRunner(java.util.Map.of(), java.util.Map.of());

        service.registerFsetFields(runner);

        assertThat(runner.getFsetFields(MAP_NAME)).isEqualTo(java.util.Set.of("OPTION"));
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

package com.generated.orion.ocutil.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.ocutil.accessor.OcutilFields;
import com.generated.orion.ocutil.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Unit tests for OcutilService, generated from COBOL program OCUTIL (Admin Data-Utilities driver).
 * All paragraphs are private and are exercised indirectly through the public mainLine(AppService)
 * entry point.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OcutilServiceTest {

    private static final int COMMAREA_LENGTH = 692;

    @Mock private AppService appService;

    private final OcutilService service = new OcutilService();

    @BeforeEach
    void setUp() {
        when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-23", "10:00:00"));
    }

    /** Builds the raw commarea string carrying CA-PGM-CONTEXT so mainLine's dispatch branches. */
    private static String commareaWithContext(int context) {
        OcutilFields seed = new OcutilFields(new WorkingStorage());
        seed.setCaPgmContext(context);
        return new String(seed.sliceBytes("ORION-COMMAREA"), StandardCharsets.ISO_8859_1);
    }

    private void stubExistingSession(int pgmContext) {
        when(appService.getEibcalen()).thenReturn(COMMAREA_LENGTH);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(pgmContext));
    }

    private void stubReceiveMap(
            String utopti, String utmodei, String utdatei, String utnumi, String utcyci) {
        doAnswer(
                        inv -> {
                            OcutilFields f = (OcutilFields) inv.getArgument(1);
                            f.setUtopti(utopti);
                            f.setUtmodei(utmodei);
                            f.setUtdatei(utdatei);
                            f.setUtnumi(utnumi);
                            f.setUtcyci(utcyci);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MUTILA"), any());
    }

    // ---------------------------------------------------------------
    // 0000-MAIN / 1000-SEND-INITIAL
    // ---------------------------------------------------------------

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_emptyCommarea_sendsInitialScreen() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> mapOutput = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MUTILA"), mapOutput.capture(), any(), eq(true), eq(false), eq(false));
        OcutilFields f = (OcutilFields) mapOutput.getValue();
        assertEquals("REBL", f.getUtmodeo().trim());
        assertEquals("Select a utility (1-7), key params, press ENTER.", f.getErrmsgo().trim());
        assertEquals("ORUT", f.getTrnnameo().trim());
        assertEquals("OCUTIL", f.getPgmnameo().trim());

        ArgumentCaptor<String> transid = ArgumentCaptor.forClass(String.class);
        verify(appService).returnTransid(transid.capture(), any(), eq(COMMAREA_LENGTH));
        assertEquals("ORUT", transid.getValue().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_nonEmptyCommarea_contextZero_sendsInitialScreen() {
        stubExistingSession(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq("MUTILA"), any(), any(), eq(true), eq(false), eq(false));
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ---------------------------------------------------------------
    // 2000-PROCESS-INPUT dispatch
    // ---------------------------------------------------------------

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_pf3_xctlsToAdminMenu() {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        ArgumentCaptor<String> pgm = ArgumentCaptor.forClass(String.class);
        verify(appService).xctl(pgm.capture(), any(), eq(COMMAREA_LENGTH));
        assertEquals("OCADMEN", pgm.getValue().trim());
        verify(appService, never())
                .sendMap(
                        anyString(),
                        any(),
                        any(),
                        any(Boolean.class),
                        any(Boolean.class),
                        any(Boolean.class));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_pf12_xctlsToAdminMenu() {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("@");

        service.mainLine(appService);

        verify(appService).xctl(eq("OCADMEN "), any(), eq(COMMAREA_LENGTH));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_pf4_resendsInitialScreen() {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        verify(appService).sendMap(eq("MUTILA"), any(), any(), eq(true), eq(false), eq(false));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_clear_resendsInitialScreen() {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("_");

        service.mainLine(appService);

        verify(appService).sendMap(eq("MUTILA"), any(), any(), eq(true), eq(false), eq(false));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_unknownAid_showsInvalidKeyMessage() {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        ArgumentCaptor<Object> mapOutput = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MUTILA"), mapOutput.capture(), any(), eq(false), eq(false), eq(false));
        OcutilFields f = (OcutilFields) mapOutput.getValue();
        assertEquals("Invalid key pressed. Please try again.", f.getErrmsgo().trim());
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ---------------------------------------------------------------
    // 2100-RUN / 2150-EDIT-OPTION / 2200-EDIT-INPUTS
    // ---------------------------------------------------------------

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainLine_enter_mapfailResp_fillsLowValuesAndShowsBlankOptionError() {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("'");
        when(appService.getEibresp()).thenReturn(36);

        service.mainLine(appService);

        ArgumentCaptor<Object> mapOutput = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MUTILA"), mapOutput.capture(), any(), eq(false), eq(false), eq(false));
        OcutilFields f = (OcutilFields) mapOutput.getValue();
        assertEquals("Enter a utility number 1 through 7.", f.getErrmsgo().trim());
        verify(appService, never()).link(anyString(), any(), eq(COMMAREA_LENGTH));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runSelectedUtility_blankOption_showsEnterUtilityNumberError() {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("'");
        stubReceiveMap("  ", "    ", "          ", "       ", "      ");

        service.mainLine(appService);

        ArgumentCaptor<Object> mapOutput = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MUTILA"), mapOutput.capture(), any(), eq(false), eq(false), eq(false));
        OcutilFields f = (OcutilFields) mapOutput.getValue();
        assertEquals("Enter a utility number 1 through 7.", f.getErrmsgo().trim());
        verify(appService, never()).link(anyString(), any(), eq(COMMAREA_LENGTH));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runSelectedUtility_lowValuesOption_treatedAsBlank_showsEnterUtilityNumberError() {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("'");
        stubReceiveMap("\u0000\u0000", "    ", "          ", "       ", "      ");

        service.mainLine(appService);

        ArgumentCaptor<Object> mapOutput = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MUTILA"), mapOutput.capture(), any(), eq(false), eq(false), eq(false));
        OcutilFields f = (OcutilFields) mapOutput.getValue();
        assertEquals("Enter a utility number 1 through 7.", f.getErrmsgo().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runSelectedUtility_nonNumericOption_showsRangeError() {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("'");
        stubReceiveMap("AB", "    ", "          ", "       ", "      ");

        service.mainLine(appService);

        ArgumentCaptor<Object> mapOutput = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MUTILA"), mapOutput.capture(), any(), eq(false), eq(false), eq(false));
        OcutilFields f = (OcutilFields) mapOutput.getValue();
        assertEquals("Utility number must be 1 through 7.", f.getErrmsgo().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runSelectedUtility_optionZero_showsRangeError() {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("'");
        stubReceiveMap("00", "    ", "          ", "       ", "      ");

        service.mainLine(appService);

        ArgumentCaptor<Object> mapOutput = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MUTILA"), mapOutput.capture(), any(), eq(false), eq(false), eq(false));
        OcutilFields f = (OcutilFields) mapOutput.getValue();
        assertEquals("Utility number must be 1 through 7.", f.getErrmsgo().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runSelectedUtility_optionEight_showsRangeError() {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("'");
        stubReceiveMap("08", "    ", "          ", "       ", "      ");

        service.mainLine(appService);

        ArgumentCaptor<Object> mapOutput = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MUTILA"), mapOutput.capture(), any(), eq(false), eq(false), eq(false));
        OcutilFields f = (OcutilFields) mapOutput.getValue();
        assertEquals("Utility number must be 1 through 7.", f.getErrmsgo().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runSelectedUtility_nonNumericMaxCount_showsMaxCountError() {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("'");
        stubReceiveMap("01", "    ", "          ", "ABCDEFG", "      ");

        service.mainLine(appService);

        ArgumentCaptor<Object> mapOutput = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MUTILA"), mapOutput.capture(), any(), eq(false), eq(false), eq(false));
        OcutilFields f = (OcutilFields) mapOutput.getValue();
        assertEquals("Max count must be numeric.", f.getErrmsgo().trim());
        verify(appService, never()).link(anyString(), any(), eq(COMMAREA_LENGTH));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runSelectedUtility_nonNumericCycle_showsCycleError() {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("'");
        stubReceiveMap("01", "    ", "          ", "       ", "ABCDEF");

        service.mainLine(appService);

        ArgumentCaptor<Object> mapOutput = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MUTILA"), mapOutput.capture(), any(), eq(false), eq(false), eq(false));
        OcutilFields f = (OcutilFields) mapOutput.getValue();
        assertEquals("Cycle must be numeric (YYYYMM).", f.getErrmsgo().trim());
        verify(appService, never()).link(anyString(), any(), eq(COMMAREA_LENGTH));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runSelectedUtility_linkFails_showsLinkFailedMessage() {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("'");
        stubReceiveMap("01", "    ", "          ", "       ", "      ");
        when(appService.getEibresp()).thenReturn(12);

        service.mainLine(appService);

        verify(appService).link(eq("OUXREF  "), any(), eq(COMMAREA_LENGTH));
        ArgumentCaptor<Object> mapOutput = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MUTILA"), mapOutput.capture(), any(), eq(false), eq(false), eq(false));
        OcutilFields f = (OcutilFields) mapOutput.getValue();
        assertEquals("Sub-program link failed - check resources.", f.getErrmsgo().trim());
        assertEquals("Y", f.getWsLinkBad().trim());
    }

    // ---------------------------------------------------------------
    // 3100..3700-DO-* + 5100..5700-FMT-* (all seven utilities)
    // ---------------------------------------------------------------

    static Stream<Arguments> utilityOptions() {
        return Stream.of(
                Arguments.of("01", "OUXREF  "),
                Arguments.of("02", "OUSTMB  "),
                Arguments.of("03", "OUFLAG  "),
                Arguments.of("04", "OUIMP   "),
                Arguments.of("05", "OUARCH  "),
                Arguments.of("06", "OUPURG  "),
                Arguments.of("07", "OUBKP   "));
    }

    @ParameterizedTest
    @MethodSource("utilityOptions")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runSelectedUtility_eachOption_linksCorrectSubProgramAndFormatsResult(
            String option, String expectedPgm) {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("'");
        stubReceiveMap(option, "    ", "          ", "       ", "      ");
        when(appService.getEibresp()).thenReturn(0);

        service.mainLine(appService);

        verify(appService, times(1)).link(eq(expectedPgm), any(), eq(COMMAREA_LENGTH));

        ArgumentCaptor<Object> mapOutput = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MUTILA"), mapOutput.capture(), any(), eq(false), eq(false), eq(false));
        OcutilFields f = (OcutilFields) mapOutput.getValue();
        assertEquals("Utility complete - see counts below.", f.getErrmsgo().trim());
        assertEquals("N", f.getWsLinkBad().trim());
        assertFalse(f.getRline1o().isBlank(), "result line 1 must be formatted");
        assertTrue(f.getRline1o().contains("="), "result line 1 must be label=value formatted");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runSelectedUtility_xrefWithNumericMaxAndNonBlankMode_parsesInputsAndOverridesMode() {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("'");
        stubReceiveMap("01", "TEST", "          ", "0000005", "      ");

        service.mainLine(appService);

        verify(appService).link(eq("OUXREF  "), any(), eq(COMMAREA_LENGTH));
        ArgumentCaptor<Object> mapOutput = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MUTILA"), mapOutput.capture(), any(), eq(false), eq(false), eq(false));
        OcutilFields f = (OcutilFields) mapOutput.getValue();
        assertEquals("Utility complete - see counts below.", f.getErrmsgo().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runSelectedUtility_flagWithNonBlankMode_setsKflModeFromRequest() {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("'");
        stubReceiveMap("03", "MODE", "          ", "       ", "      ");

        service.mainLine(appService);

        verify(appService).link(eq("OUFLAG  "), any(), eq(COMMAREA_LENGTH));
        ArgumentCaptor<Object> mapOutput = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MUTILA"), mapOutput.capture(), any(), eq(false), eq(false), eq(false));
        OcutilFields f = (OcutilFields) mapOutput.getValue();
        assertEquals("Utility complete - see counts below.", f.getErrmsgo().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runSelectedUtility_stmbWithNumericCycle_setsKsmCycle() {
        stubExistingSession(1);
        when(appService.getEibaid()).thenReturn("'");
        stubReceiveMap("02", "    ", "          ", "       ", "202601");

        service.mainLine(appService);

        verify(appService).link(eq("OUSTMB  "), any(), eq(COMMAREA_LENGTH));
        ArgumentCaptor<Object> mapOutput = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MUTILA"), mapOutput.capture(), any(), eq(false), eq(false), eq(false));
        OcutilFields f = (OcutilFields) mapOutput.getValue();
        assertEquals("Utility complete - see counts below.", f.getErrmsgo().trim());
    }

    // ---------------------------------------------------------------
    // AppProgram interface metadata methods (no COBOL paragraph equivalent)
    // ---------------------------------------------------------------

    @Test
    void getProgramName_returnsOcutil() {
        assertEquals("OCUTIL", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrut() {
        assertEquals("ORUT", service.getTransId());
    }

    @Test
    void getButtonDefs_delegatesToMetadata() {
        assertNotNull(service.getButtonDefs());
    }

    @Test
    void getFieldMapping_delegatesToMetadata() {
        service.getFieldMapping("MUTILA");
    }
}

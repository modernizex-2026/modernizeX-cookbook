package com.generated.orion.ocops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.ocops.accessor.OcopsFields;
import com.generated.orion.ocops.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

/**
 * Unit tests for OcopsService, generated from COBOL program OCOPS (ORION-CCMS admin batch
 * operations, on-line CICS pseudo-conversational). All business logic is private and is exercised
 * solely through the public {@link OcopsService#mainLine(AppService)} entry point, driven by an
 * {@link AppService} mock that emulates CICS SEND/RECEIVE/LINK/XCTL/RETURN.
 *
 * <p>Convert-gap found: COBOL's 6000-PARSE-PARM does {@code UNSTRING WS-PARM-IN DELIMITED BY ALL
 * SPACE INTO WS-TOK1 WS-TOK2}, but {@link OcopsService}'s {@code parseInputParm} computes the split
 * into a local {@code unstringParts} array and never assigns it back to WS-TOK1 / WS-TOK2 (they
 * stay fixed at a single space). As a result WS-P-ACCT and WS-P-AMT are always zero regardless of
 * the PARM screen input. The CONVERT-GAP tests below assert the COBOL-correct (non-zero) values and
 * are expected to fail against the current Java implementation — this is the intended detection
 * signal, not a test bug.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcopsServiceTest {

    private static final String MAP_NAME = "MOPSA";
    private static final String TRAN_ID = "OROP";
    private static final String PGM_NAME = "OCOPS";
    private static final String MENU_PGM = "OCMENU";
    private static final int COMMAREA_LENGTH = 692;

    private static final String MSG_PROMPT = "Select an operation (1-8) and press ENTER.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_INVALID_OPTION = "Invalid option. Enter 1 through 8.";
    private static final String MSG_LINK_FAILED =
            "Operation could not be started. Contact support.";
    private static final String MSG_RESULT_COMPLETE =
            "Operation complete. Review the counts below.";
    private static final String HDR_TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM";

    @Mock private AppService appService;

    private OcopsService service;

    @BeforeEach
    void setUp() {
        service = new OcopsService();
    }

    private String commarea(int context) {
        OcopsFields helper = new OcopsFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commarea(context));
    }

    private void givenReceiveMapPopulates(java.util.function.Consumer<OcopsFields> populate) {
        doAnswer(
                        inv -> {
                            OcopsFields f = inv.getArgument(1);
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

    /**
     * Stubs appService.link(...) to capture the outgoing KOPS-AREA request fields into the given
     * consumer, then overwrite the byte[] commarea in-place with the sub-program's result fields
     * (KO-STATUS etc.) — mirroring EXEC CICS LINK COMMAREA in/out semantics.
     */
    private void givenLinkPopulatesResult(
            java.util.function.Consumer<OcopsFields> captureRequest,
            java.util.function.Consumer<OcopsFields> populateResult) {
        doAnswer(
                        inv -> {
                            byte[] ca = inv.getArgument(1);
                            OcopsFields decoder = new OcopsFields(new WorkingStorage());
                            decoder.writeBytes("ORION-COMMAREA", ca);
                            // KOPS-AREA is a distinct physical buffer from
                            // CA-WORK-AREA/ORION-COMMAREA
                            // (mirrors COBOL's SET ADDRESS OF KOPS-AREA TO ADDRESS OF
                            // CA-WORK-AREA);
                            // bridge them explicitly the same way the real service does via
                            // copyBytes.
                            decoder.writeBytes("KOPS-AREA", decoder.sliceBytes("CA-WORK-AREA"));
                            captureRequest.accept(decoder);
                            populateResult.accept(decoder);
                            decoder.copyBytes("CA-WORK-AREA", "KOPS-AREA");
                            byte[] updated = decoder.sliceBytes("ORION-COMMAREA");
                            System.arraycopy(updated, 0, ca, 0, ca.length);
                            return null;
                        })
                .when(appService)
                .link(anyString(), any(byte[].class), eq(COMMAREA_LENGTH));
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_eibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);
        givenFormatTimeStub();

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcopsFields out = (OcopsFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo(TRAN_ID);
        assertThat(out.getPgmnameo().trim()).isEqualTo(PGM_NAME);
        assertThat(out.getTitleo().trim()).isEqualTo(HDR_TITLE);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-22");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        verify(appService).returnTransid(eq(TRAN_ID), any(), eq(COMMAREA_LENGTH));
    }

    @Test
    void mainLine_firstEnter_contextZero_sendsInitialScreenWithPrompt() {
        givenPseudoConversation(0);
        givenFormatTimeStub();

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcopsFields out = (OcopsFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @ParameterizedTest
    @CsvSource({"3", "@"})
    void mainLine_pf3OrPf12Pressed_xctlsToMenuProgram(String eibaid) {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn(eibaid);

        service.mainLine(appService);

        ArgumentCaptor<Object> commareaCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .xctl(
                        argThat(s -> s.trim().equals(MENU_PGM)),
                        commareaCaptor.capture(),
                        eq(COMMAREA_LENGTH));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());

        OcopsFields decoded = new OcopsFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo(PGM_NAME);
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo(TRAN_ID);
        assertThat(decoded.getCaPgmContext()).isZero();
    }

    @ParameterizedTest
    @CsvSource({"4", "_"})
    void mainLine_pf4OrClearPressed_sendsInitialScreenAgain(String eibaid) {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn(eibaid);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcopsFields out = (OcopsFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        verify(appService, never()).xctl(anyString(), any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("X");

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
        OcopsFields out = (OcopsFields) dataOnlyOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2050-RECEIVE (MAPFAIL) ─────────────────────────

    @Test
    void mainLine_receiveMapFail_fillsLowValuesAndTreatsOptionAsInvalid() {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        when(appService.getEibresp()).thenReturn(36);

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
        OcopsFields out = (OcopsFields) dataOnlyOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_OPTION);
        verify(appService, never()).link(anyString(), any(), org.mockito.ArgumentMatchers.anyInt());
    }

    // ───────────────────────── 2100-RUN-OPERATION / 7000-BUILD-REQUEST ─────────────────────────

    @Test
    void mainLine_enterKey_nonNumericOption_showsInvalidOptionMessage() {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setOptioni("AB"));

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
        OcopsFields out = (OcopsFields) dataOnlyOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_OPTION);
        verify(appService, never()).link(anyString(), any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @ParameterizedTest
    @CsvSource({"00", "09", "99"})
    void mainLine_enterKey_optionOutOfRange_showsInvalidOptionMessage(String optionInput) {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setOptioni(optionInput));

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
        OcopsFields out = (OcopsFields) dataOnlyOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_OPTION);
        verify(appService, never()).link(anyString(), any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @ParameterizedTest
    @CsvSource({
        "01,OUPOST,POST",
        "02,OUPAY,PAY",
        "03,OUINT,INT",
        "04,OUFEE,FEE",
        "05,OUCHGF,CHGF",
        "06,OUCLOS,CLOS",
        "07,OURNEW,RNEW",
        "08,OUCYCL,CYCL"
    })
    void mainLine_enterKey_validOption_linksToMappedSubProgram(
            String optionInput, String expectedSubPgm, String expectedFunction) {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setOptioni(optionInput));
        java.util.concurrent.atomic.AtomicReference<String> capturedFunction =
                new java.util.concurrent.atomic.AtomicReference<>();
        givenLinkPopulatesResult(
                req -> capturedFunction.set(req.getKoFunction().trim()),
                res -> {
                    res.setKoStatus("O");
                    res.setKoStatusMsg("Processed successfully.");
                    res.setKoReadCnt(10);
                    res.setKoSelectCnt(5);
                    res.setKoPostedCnt(3);
                    res.setKoUpdateCnt(2);
                    res.setKoRejectCnt(1);
                    res.setKoTranCnt(4);
                    res.setKoAmt1(BigDecimal.valueOf(100.50));
                    res.setKoAmt2(BigDecimal.valueOf(-25.75));
                });

        service.mainLine(appService);

        verify(appService)
                .link(
                        argThat(s -> s.trim().equals(expectedSubPgm)),
                        any(byte[].class),
                        eq(COMMAREA_LENGTH));
        assertThat(capturedFunction.get()).isEqualTo(expectedFunction);

        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME),
                        dataOnlyOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcopsFields out = (OcopsFields) dataOnlyOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_RESULT_COMPLETE);
        assertThat(out.getRstato().trim()).isEqualTo("OK");
        assertThat(out.getRmsgo().trim()).isEqualTo("Processed successfully.");
    }

    @Test
    void mainLine_enterKey_linkFails_showsOperationCouldNotBeStartedMessage() {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setOptioni("01"));
        when(appService.getEibresp()).thenReturn(1);

        service.mainLine(appService);

        verify(appService)
                .link(
                        argThat(s -> s.trim().equals("OUPOST")),
                        any(byte[].class),
                        eq(COMMAREA_LENGTH));
        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME),
                        dataOnlyOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcopsFields out = (OcopsFields) dataOnlyOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LINK_FAILED);
    }

    // ───────────────────────── 7500-SHOW-RESULT status mapping ─────────────────────────

    @ParameterizedTest
    @CsvSource({"O,OK", "W,WARNING", "E,ERROR", "X,ERROR"})
    void mainLine_showResult_mapsKoStatusToDisplayStatus(
            String koStatus, String expectedDisplayStatus) {
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setOptioni("01"));
        givenLinkPopulatesResult(
                req -> {},
                res -> {
                    res.setKoStatus(koStatus);
                    res.setKoStatusMsg("status message");
                });

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
        OcopsFields out = (OcopsFields) dataOnlyOut.getValue();
        assertThat(out.getRstato().trim()).isEqualTo(expectedDisplayStatus);
    }

    // ───────────────────────── 6000/6100/6200-PARSE (CONVERT-GAP) ─────────────────────────

    @Test
    void mainLine_parmWithAccountAndAmount_parsesAccountIdPerCobol() {
        // CONVERT-GAP: COBOL's 6000-PARSE-PARM does
        // UNSTRING WS-PARM-IN DELIMITED BY ALL SPACE INTO WS-TOK1 WS-TOK2,
        // so token one ("12345") folds into WS-P-ACCT = 12345 (see 6100-PARSE-ACCT).
        // The Java parseInputParm() computes the split into a local variable and
        // never assigns WS-TOK1/WS-TOK2, so WS-TOK1 stays " " and WS-P-ACCT is
        // always 0 — this test is expected to FAIL against current Java code.
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setOptioni("01");
                    f.setParmi("12345 67.89");
                });
        java.util.concurrent.atomic.AtomicReference<Long> capturedAcct =
                new java.util.concurrent.atomic.AtomicReference<>();
        givenLinkPopulatesResult(
                req -> capturedAcct.set(req.getKoParmAcct()), res -> res.setKoStatus("O"));

        service.mainLine(appService);

        assertThat(capturedAcct.get()).isEqualTo(12345L);
    }

    @Test
    void mainLine_parmWithAccountAndAmount_parsesAmountPerCobol() {
        // CONVERT-GAP: see mainLine_parmWithAccountAndAmount_parsesAccountIdPerCobol.
        // COBOL's 6200-PARSE-AMT folds token two ("67.89") into WS-P-AMT = 67.89.
        // Java's WS-TOK2 never receives the split token, so WS-P-AMT stays 0.00 —
        // this test is expected to FAIL against current Java code.
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setOptioni("01");
                    f.setParmi("12345 67.89");
                });
        java.util.concurrent.atomic.AtomicReference<BigDecimal> capturedAmt =
                new java.util.concurrent.atomic.AtomicReference<>();
        givenLinkPopulatesResult(
                req -> capturedAmt.set(req.getKoParmAmt()), res -> res.setKoStatus("O"));

        service.mainLine(appService);

        assertThat(capturedAmt.get()).isEqualByComparingTo(new BigDecimal("67.89"));
    }

    @Test
    void mainLine_optionRenewWithCardNumber_koParmCardReceivesFullParm() {
        // Not a convert-gap: KO-PARM-CARD is set directly from WS-PARM-IN
        // (7000-BUILD-REQUEST's "WHEN 7" branch), bypassing the broken
        // WS-TOK1/WS-TOK2 split, so this path matches COBOL as-is.
        givenPseudoConversation(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setOptioni("07");
                    f.setParmi("4111222233334444");
                });
        java.util.concurrent.atomic.AtomicReference<String> capturedCard =
                new java.util.concurrent.atomic.AtomicReference<>();
        givenLinkPopulatesResult(
                req -> capturedCard.set(req.getKoParmCard().trim()), res -> res.setKoStatus("O"));

        service.mainLine(appService);

        verify(appService)
                .link(
                        argThat(s -> s.trim().equals("OURNEW")),
                        any(byte[].class),
                        eq(COMMAREA_LENGTH));
        assertThat(capturedCard.get()).isEqualTo("4111222233334444");
    }

    // ───────────────────────── AppProgram metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcops() {
        assertThat(service.getProgramName()).isEqualTo(PGM_NAME);
    }

    @Test
    void getTransId_returnsOrop() {
        assertThat(service.getTransId()).isEqualTo(TRAN_ID);
    }

    @Test
    void getButtonDefs_delegatesToBmsMetadata() {
        assertThat(service.getButtonDefs()).isNotNull().isNotEmpty();
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

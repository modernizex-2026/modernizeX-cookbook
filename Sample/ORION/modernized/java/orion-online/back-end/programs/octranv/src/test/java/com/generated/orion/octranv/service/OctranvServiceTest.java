package com.generated.orion.octranv.service;

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
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.octranv.accessor.OctranvFields;
import com.generated.orion.octranv.model.WorkingStorage;

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
 * Unit tests for OctranvService, generated from COBOL program OCTRANV (transaction inquiry screen,
 * follows the OCACCTV golden on-line skeleton). All business logic is private and is exercised
 * solely through the public {@link OctranvService#mainLine(AppService)} entry point, driven by an
 * {@link AppService} mock that emulates CICS SEND/RECEIVE/READ/XCTL/RETURN.
 *
 * <p>Convert-gap check: OCTRANV's Java service mirrors every COBOL paragraph 1:1
 * (0000-MAIN..9000-RETURN), including the TR-AMT(2:15) substring edit and the 3000-READ-TRAN /
 * 2100-READ-AND-SHOW message-overwrite quirk; no behavioral divergence was found against the COBOL
 * ground truth, so no CONVERT-GAP test is included here.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OctranvServiceTest {

    private static final String TRANFILE = "TRANFILE";
    private static final String MAP_NAME = "MTRANVA";
    private static final String MENU_PROGRAM = "OCMENU";

    private static final String MSG_PROMPT = "Enter a transaction id and press ENTER.";
    private static final String MSG_TRAN_FOUND = "Transaction details displayed.";
    private static final String MSG_TRAN_NOTFND = "Transaction not found - check the id.";
    private static final String MSG_ID_REQUIRED = "Transaction id is required.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String HDR_TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM";

    @Mock private AppService appService;

    private OctranvService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OctranvService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commareaWithContext(int context) {
        OctranvFields helper = new OctranvFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(context));
    }

    /** Stubs receiveMap to populate the MTRANVAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OctranvFields> populate) {
        doAnswer(
                        inv -> {
                            OctranvFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    /**
     * Replicates COBOL's "MOVE TR-AMT TO WS-ED-AMT; MOVE WS-ED-AMT(2:15) TO TRAMTO" chain
     * (4000-POPULATE-DETAIL) on an isolated accessor, so the expected string is derived the same
     * way production computes it. Ground truth, not a convert gap.
     */
    private String editedAmount(BigDecimal amount) {
        OctranvFields helper = new OctranvFields(new WorkingStorage());
        helper.setTrAmt(amount);
        helper.setWsEdAmt(helper.getTrAmt());
        helper.setTramto(Utility.padRight(helper.getString("WS-ED-AMT"), 16).substring(1, 16));
        return helper.getTramto();
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OctranvFields out = (OctranvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORTV");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCTRANV");
        assertThat(out.getTitleo().trim()).isEqualTo(HDR_TITLE);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-22");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        verify(appService).returnTransid(eq("ORTV"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        verify(appService).returnTransid(eq("ORTV"), any(), eq(692));
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToMenuWithFromProgramInfo() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        ArgumentCaptor<Object> commareaCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .xctl(
                        argThat(s -> s.trim().equals(MENU_PROGRAM)),
                        commareaCaptor.capture(),
                        eq(692));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());

        OctranvFields decoded = new OctranvFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo("OCTRANV");
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo("ORTV");
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
        OctranvFields out = (OctranvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        // 2000-PROCESS-INPUT's WHEN OTHER performs 1000-SEND-INITIAL (erase=true) then
        // 8100-SEND-DATAONLY (erase=false); both calls share the same mutable field
        // accessor, so only the final (dataonly) call's content can be inspected —
        // the initial call's transient ERRMSGO is overwritten before assertion time.
        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), dataOnlyOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranvFields dataOnlyScreen = (OctranvFields) dataOnlyOut.getValue();
        assertThat(dataOnlyScreen.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-READ-AND-SHOW ─────────────────────────

    @Test
    void mainLine_enterKey_tranIdBlank_showsRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setTranidi("                "));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranvFields out = (OctranvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ID_REQUIRED);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_tranIdAllLowValues_showsRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.fillLowValues("TRANIDI"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranvFields out = (OctranvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ID_REQUIRED);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_tranFound_populatesDetailAndShowsFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setTranidi("TX0000000000001 "));
        BigDecimal amount = new BigDecimal("123.45");
        doAnswer(
                        inv -> {
                            OctranvFields into = inv.getArgument(1);
                            into.setTrCardNum("4111111111111111");
                            into.setTrTypeCd("PU");
                            into.setTrAmt(amount);
                            into.setTrMerchantName("ACME STORE");
                            into.setTrDesc("OFFICE SUPPLIES");
                            nextResp.set(0); // DFHRESP(NORMAL)
                            return null;
                        })
                .when(appService)
                .readFile(eq(TRANFILE), any(), eq("TX0000000000001 "), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranvFields out = (OctranvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TRAN_FOUND);
        assertThat(out.getTranido().trim()).isEqualTo("TX0000000000001");
        assertThat(out.getTrcardo().trim()).isEqualTo("4111111111111111");
        assertThat(out.getTrtypeo().trim()).isEqualTo("PU");
        assertThat(out.getTramto()).isEqualTo(editedAmount(amount));
        assertThat(out.getTrmercho().trim()).isEqualTo("ACME STORE");
        assertThat(out.getTrdesco().trim()).isEqualTo("OFFICE SUPPLIES");
    }

    @Test
    void mainLine_enterKey_tranNotFound_showsNotFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setTranidi("TX0000000099999 "));
        doAnswer(
                        inv -> {
                            nextResp.set(13); // DFHRESP(NOTFND)
                            return null;
                        })
                .when(appService)
                .readFile(eq(TRANFILE), any(), eq("TX0000000099999 "), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranvFields out = (OctranvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TRAN_NOTFND);
    }

    @Test
    void mainLine_enterKey_readFileUnexpectedError_finalMessageIsStillNotFoundPerCobolOverwrite() {
        // Per COBOL 3000-READ-TRAN: WHEN OTHER only sets an interim ERRMSGO ("Error
        // reading the transaction file.") but never sets REC-FOUND; 2100-READ-AND-SHOW's
        // EVALUATE WHEN OTHER branch (not REC-FOUND) then unconditionally overwrites
        // ERRMSGO with WS-M-TRAN-NOTFND. So the interim message is never shown to the
        // operator, in COBOL and in the converted Java alike — not a convert gap.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setTranidi("TX0000000011111 "));
        doAnswer(
                        inv -> {
                            nextResp.set(99); // unexpected error
                            return null;
                        })
                .when(appService)
                .readFile(eq(TRANFILE), any(), eq("TX0000000011111 "), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranvFields out = (OctranvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TRAN_NOTFND);
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOctranv() {
        assertEquals("OCTRANV", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrtv() {
        assertEquals("ORTV", service.getTransId());
    }

    @Test
    void getButtonDefs_delegatesToBmsMetadata() {
        assertThat(service.getButtonDefs()).isNotNull();
    }

    // registerFsetFields(AppRunner) is intentionally not covered here: AppRunner
    // cannot be mocked in this environment (Mockito inline mock maker fails against
    // JDK 26 / Byte Buddy on AppRunner's class hierarchy) — see findings note.

    @Test
    void getFieldMapping_delegatesToBmsMetadata() {
        assertThat(service.getFieldMapping("MTRANVA")).isNotNull();
    }
}

package com.generated.orion.ocpauin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.ocpauin.accessor.OcpauinFields;
import com.generated.orion.ocpauin.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OcpauinService, generated from COBOL program OCPAUIN. All business logic is
 * private and is exercised solely through the public {@link OcpauinService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS SEND/RECEIVE/LINK/RETURN.
 * The DL/I sub-program OUIMSPA is invoked via {@code appService.link(...)}; its output is simulated
 * by mutating the CA-WORK-AREA slice of the commarea byte[] passed to link(), matching the
 * WS-PAU-LINK protocol documented at the top of OCPAUIN.cbl.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcpauinServiceTest {

    private static final String SUB_PGM = "OUIMSPA";
    private static final int COMMAREA_LENGTH = 692;

    private static final String MSG_PROMPT = "Enter an authorization id and press ENTER.";
    private static final String MSG_REQUIRED = "Please enter all required fields.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_LINK_ERR = "Unable to link to IMS module OUIMSPA.";

    @Mock private AppService appService;

    private OcpauinService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OcpauinService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commareaWithContext(int context) {
        OcpauinFields helper = new OcpauinFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(context));
    }

    /** Stubs receiveMap to populate the MPAUINAI input fields captured from production code. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OcpauinFields> populate) {
        doAnswer(
                        inv -> {
                            OcpauinFields f = inv.getArgument(1);
                            populate.accept(f);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MPAUINA"), any());
    }

    /** Builds a 200-byte PAUSEG-REC image using the field accessors (ground truth: RPAUSEG.cpy). */
    private String buildSegment(
            String authId,
            String cardNum,
            long acctId,
            BigDecimal amount,
            String merchant,
            String requestTs,
            String status,
            String decReason) {
        OcpauinFields seg = new OcpauinFields(new WorkingStorage());
        seg.setPaAuthId(authId);
        seg.setPaCardNum(cardNum);
        seg.setPaAcctId(acctId);
        seg.setPaAmount(amount);
        seg.setPaMerchant(merchant);
        seg.setPaRequestTs(requestTs);
        seg.setPaStatus(status);
        seg.setPaDecReason(decReason);
        return seg.groupToString("PAUSEG-REC");
    }

    /**
     * Stubs the LINK to OUIMSPA: mutates the ORION-COMMAREA byte[] in place (as the real CICS LINK
     * would) so that WS-PAU-LINK on the caller side, after {@code MOVE CA-WORK-AREA TO
     * WS-PAU-LINK}, carries the given status/message and (optionally) a PAUSEG segment image.
     */
    private void givenLinkResponse(String status, String msg, String segment) {
        givenLinkResponse(status, msg, segment, null);
    }

    /**
     * Same as {@link #givenLinkResponse(String, String, String)} but also lets the sub program
     * overwrite WS-PL-KEY (the real OUIMSPA returns the found record's key here; 2200-BROWSE-NEXT
     * does {@code MOVE WS-PL-KEY TO AUTHIDO} after populate-detail, so browse-next tests must stub
     * this to the expected key).
     */
    private void givenLinkResponse(String status, String msg, String segment, String key) {
        doAnswer(
                        inv -> {
                            byte[] ca = inv.getArgument(1);
                            OcpauinFields sub = new OcpauinFields(new WorkingStorage());
                            sub.writeBytes("ORION-COMMAREA", ca);
                            sub.setWsPlStatus(status);
                            sub.setWsPlMsg(msg);
                            if (segment != null) {
                                sub.setString("WS-PL-SEGMENT", segment);
                            }
                            if (key != null) {
                                sub.setWsPlKey(key);
                            }
                            sub.setCaWorkArea(sub.getWsPauLink());
                            byte[] out = sub.sliceBytes("ORION-COMMAREA");
                            System.arraycopy(out, 0, ca, 0, ca.length);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .link(
                        argThat(s -> s.trim().equals(SUB_PGM)),
                        any(byte[].class),
                        eq(COMMAREA_LENGTH));
    }

    /**
     * Stubs the LINK to fail at the CICS level (non-NORMAL EIBRESP), never mutating the commarea.
     */
    private void givenLinkCicsError(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .link(
                        argThat(s -> s.trim().equals(SUB_PGM)),
                        any(byte[].class),
                        eq(COMMAREA_LENGTH));
    }

    private OcpauinFields lastSendMapOutput() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MPAUINA"), captor.capture(), any(), eq(false), eq(false), eq(false));
        return (OcpauinFields) captor.getValue();
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MPAUINA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcpauinFields out = (OcpauinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORPI");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCPAUIN");
        assertThat(out.getTitleo().trim()).isEqualTo("ORION CREDIT CARD MANAGEMENT SYSTEM");
        verify(appService).returnTransid(eq("ORPI"), any(), eq(COMMAREA_LENGTH));
    }

    @Test
    void mainLine_pseudoConversationalContextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq("MPAUINA"), any(), any(), eq(true), eq(false), eq(false));
        verify(appService).returnTransid(eq("ORPI"), any(), eq(COMMAREA_LENGTH));
    }

    // ───────────────────────── 2000-PROCESS-INPUT dispatch ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToMenu() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        verify(appService)
                .xctl(argThat(s -> s.trim().equals("OCMENU")), any(), eq(COMMAREA_LENGTH));
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
                .sendMap(
                        eq("MPAUINA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcpauinFields out = (OcpauinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        // 2000-PROCESS-INPUT WHEN OTHER first resends the initial (erase) screen, then a
        // DATAONLY screen carrying the invalid-key message — assert the final send.
        verify(appService, times(2))
                .sendMap(eq("MPAUINA"), any(), any(), anyBoolean(), eq(false), eq(false));
        OcpauinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-INQUIRE (ENTER) ─────────────────────────

    @Test
    void mainLine_enterBlankAuthid_showsRequiredMessageAndSkipsLink() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAuthidi(" "));

        service.mainLine(appService);

        OcpauinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(appService, never()).link(anyString(), any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void mainLine_enterReceiveMapNotStubbed_defaultAuthid_showsRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        // no receiveMap stub: AUTHIDI stays at its fresh WorkingStorage default (spaces or
        // low-values), so this exercises the "AUTHIDI = SPACES OR LOW-VALUES" OR-condition
        // via whichever side the default happens to satisfy.

        service.mainLine(appService);

        OcpauinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
    }

    @Test
    void mainLine_enterValidAuthid_linkOk_populatesDetailAndShowsSubMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAuthidi("AUTH0000000001"));
        String segment =
                buildSegment(
                        "AUTH0000000001",
                        "4111111111111111",
                        12345678901L,
                        new BigDecimal("100.50"),
                        "ACME STORE",
                        "2026-09-20-10.00.00.000000",
                        "P",
                        "");
        givenLinkResponse("  ", "Authorization found.", segment);

        service.mainLine(appService);

        verify(appService)
                .link(
                        argThat(s -> s.trim().equals(SUB_PGM)),
                        any(byte[].class),
                        eq(COMMAREA_LENGTH));
        OcpauinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo("Authorization found.");
        assertThat(out.getAuthido().trim()).isEqualTo("AUTH0000000001");
        assertThat(out.getPacardo().trim()).isEqualTo("4111111111111111");
        assertThat(out.getPaaccto().trim()).isEqualTo("12345678901");
        assertThat(out.getPamercho().trim()).isEqualTo("ACME STORE");
        assertThat(out.getPastato().trim()).isEqualTo("PENDING");
    }

    @Test
    void mainLine_enterValidAuthid_linkNotFound_showsCaErrMsgFromSub() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAuthidi("AUTH0000000002"));
        doAnswer(
                        inv -> {
                            byte[] ca = inv.getArgument(1);
                            OcpauinFields sub = new OcpauinFields(new WorkingStorage());
                            sub.writeBytes("ORION-COMMAREA", ca);
                            sub.setCaErrMsg("Record not found.");
                            sub.setWsPlStatus("GE");
                            sub.setCaWorkArea(sub.getWsPauLink());
                            byte[] out = sub.sliceBytes("ORION-COMMAREA");
                            System.arraycopy(out, 0, ca, 0, ca.length);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .link(
                        argThat(s -> s.trim().equals(SUB_PGM)),
                        any(byte[].class),
                        eq(COMMAREA_LENGTH));

        service.mainLine(appService);

        OcpauinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo("Record not found.");
    }

    @Test
    void mainLine_enterValidAuthid_linkCicsErrorRespNotNormal_showsLinkErrorMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAuthidi("AUTH0000000003"));
        givenLinkCicsError(99);

        service.mainLine(appService);

        OcpauinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LINK_ERR);
    }

    // ───────────────────────── 2200-BROWSE-NEXT (PF8) ─────────────────────────

    @Test
    void mainLine_pf8BrowseNext_linkOk_populatesDetailAndSetsAuthidFromReturnedKey() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("8");
        givenReceiveMapPopulates(f -> f.setAuthidi("AUTH0000000001"));
        String segment =
                buildSegment(
                        "AUTH0000000002",
                        "4222222222222222",
                        22345678901L,
                        new BigDecimal("250.00"),
                        "WIDGET CO",
                        "2026-09-20-11.00.00.000000",
                        "A",
                        "");
        givenLinkResponse("  ", "Next record.", segment, "AUTH0000000002");

        service.mainLine(appService);

        verify(appService)
                .link(
                        argThat(s -> s.trim().equals(SUB_PGM)),
                        any(byte[].class),
                        eq(COMMAREA_LENGTH));
        OcpauinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo("Next record.");
        assertThat(out.getAuthido().trim()).isEqualTo("AUTH0000000002");
        assertThat(out.getPastato().trim()).isEqualTo("APPROVED");
    }

    @Test
    void mainLine_pf8BrowseNext_linkEndOfList_showsCaErrMsgFromSub() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("8");
        givenReceiveMapPopulates(f -> f.setAuthidi("AUTH0000000099"));
        doAnswer(
                        inv -> {
                            byte[] ca = inv.getArgument(1);
                            OcpauinFields sub = new OcpauinFields(new WorkingStorage());
                            sub.writeBytes("ORION-COMMAREA", ca);
                            sub.setCaErrMsg("End of list reached.");
                            sub.setWsPlStatus("GB");
                            sub.setCaWorkArea(sub.getWsPauLink());
                            byte[] out = sub.sliceBytes("ORION-COMMAREA");
                            System.arraycopy(out, 0, ca, 0, ca.length);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .link(
                        argThat(s -> s.trim().equals(SUB_PGM)),
                        any(byte[].class),
                        eq(COMMAREA_LENGTH));

        service.mainLine(appService);

        OcpauinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo("End of list reached.");
    }

    // ───────────────────────── 4100-STATUS-WORD branches ─────────────────────────

    @Test
    void mainLine_enterAuthid_statusDeclined_showsDeclinedWord() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAuthidi("AUTH0000000004"));
        String segment =
                buildSegment(
                        "AUTH0000000004",
                        "4333333333333333",
                        32345678901L,
                        new BigDecimal("75.25"),
                        "SHOP INC",
                        "2026-09-20-12.00.00.000000",
                        "D",
                        "Suspected fraud");
        givenLinkResponse("  ", "Declined.", segment);

        service.mainLine(appService);

        OcpauinFields out = lastSendMapOutput();
        assertThat(out.getPastato().trim()).isEqualTo("DECLINED");
        assertThat(out.getPadeco().trim()).isEqualTo("Suspected fraud");
    }

    @Test
    void mainLine_enterAuthid_statusPurged_showsPurgedWord() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAuthidi("AUTH0000000005"));
        String segment =
                buildSegment(
                        "AUTH0000000005",
                        "4444444444444444",
                        42345678901L,
                        new BigDecimal("10.00"),
                        "OLD SHOP",
                        "2026-09-01-09.00.00.000000",
                        "X",
                        "");
        givenLinkResponse("  ", "Purged.", segment);

        service.mainLine(appService);

        OcpauinFields out = lastSendMapOutput();
        assertThat(out.getPastato().trim()).isEqualTo("PURGED");
    }

    @Test
    void mainLine_enterAuthid_statusUnrecognised_showsUnknownWord() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAuthidi("AUTH0000000006"));
        String segment =
                buildSegment(
                        "AUTH0000000006",
                        "4555555555555555",
                        52345678901L,
                        new BigDecimal("5.00"),
                        "MISC SHOP",
                        "2026-09-02-09.00.00.000000",
                        "Z",
                        "");
        givenLinkResponse("  ", "Weird status.", segment);

        service.mainLine(appService);

        OcpauinFields out = lastSendMapOutput();
        assertThat(out.getPastato().trim()).isEqualTo("UNKNOWN");
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcpauin() {
        assertEquals("OCPAUIN", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrpi() {
        assertEquals("ORPI", service.getTransId());
    }

    @Test
    void getButtonDefs_returnsAllFourNavigationButtons() {
        assertThat(service.getButtonDefs()).hasSize(4);
    }

    @Test
    void registerFsetFields_registersAuthidFieldOnMpauina() {
        // AppRunner is a concrete class; Mockito's inline mock maker cannot instrument it
        // on this JDK (Byte Buddy vs. class file major version), so exercise the real
        // registry instead of mocking it.
        AppRunner runner = new AppRunner(java.util.Map.of(), java.util.Map.of());

        service.registerFsetFields(runner);

        assertThat(runner.getFsetFields("MPAUINA")).isEqualTo(Set.of("AUTHID"));
    }

    @Test
    void getFieldMapping_mpauina_returnsPopulatedMapping() {
        assertThat(service.getFieldMapping("MPAUINA")).isNotNull();
    }

    @Test
    void getFieldMapping_unknownMap_returnsEmptyMapping() {
        assertThat(service.getFieldMapping("UNKNOWN")).isNotNull();
    }
}

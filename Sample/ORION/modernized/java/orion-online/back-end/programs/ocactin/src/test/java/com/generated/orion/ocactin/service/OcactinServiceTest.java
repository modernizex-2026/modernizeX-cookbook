package com.generated.orion.ocactin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.ocactin.accessor.OcactinFields;
import com.generated.orion.ocactin.model.WorkingStorage;

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
 * Unit tests for OcactinService, generated from COBOL program OCACTIN. All business logic is
 * private and is exercised solely through the public {@link OcactinService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS SEND/RECEIVE/LINK/RETURN.
 * The browse sub-program OUACTIN is invoked via {@code appService.link(...)}; its output is
 * simulated by mutating the commarea byte[] passed to link().
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcactinServiceTest {

    private static final String SUB_PGM = "OUACTIN";
    private static final int KACTIN_AREA_LEN = 767;

    private static final String MSG_PROMPT = "Type a filter and press ENTER.";
    private static final String MSG_BAD_FILTER = "Filter not recognised - see the list above.";
    private static final String MSG_NONE_FOUND = "No accounts match that filter.";
    private static final String MSG_END_FILE = "End of selection - no more accounts.";
    private static final String MSG_LIST_FIRST = "Apply a filter first (press ENTER).";
    private static final String MSG_LINK_ERR = "Unable to reach the account browse engine.";
    private static final String MSG_BROWSE_ERR = "Error browsing the account file.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";

    @Mock private AppService appService;

    private OcactinService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OcactinService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /**
     * Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT and CA-WORK-AREA.
     */
    private String commareaWithContext(int context, String workArea) {
        OcactinFields helper = new OcactinFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        if (workArea != null) {
            helper.setCaWorkArea(workArea);
        }
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        givenPseudoConversation(context, null);
    }

    private void givenPseudoConversation(int context, String workArea) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(context, workArea));
    }

    /** Stubs receiveMap to populate the MACTINAI input fields captured from production code. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OcactinFields> populate) {
        doAnswer(
                        inv -> {
                            OcactinFields f = inv.getArgument(1);
                            populate.accept(f);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MACTINA"), any());
    }

    /** One simulated KACTIN row returned by the browse sub-program. */
    private record Row(
            long id, String status, String bal, String limit, String avail, String util) {}

    /**
     * Stubs the LINK to OUACTIN: mutates the commarea byte[] in place (as the real CICS LINK would)
     * to carry back KAI-RETURN-CD, the row set, the running KPI totals, the resume key and the
     * more-switch.
     */
    private void givenLinkResponse(
            String returnCd,
            int popCount,
            BigDecimal popBal,
            BigDecimal popAvl,
            long nextKey,
            String moreSw,
            Row... rows) {
        doAnswer(
                        inv -> {
                            byte[] ca = inv.getArgument(1);
                            OcactinFields sub = new OcactinFields(new WorkingStorage());
                            sub.writeBytes("KACTIN-AREA", ca);
                            sub.setKaiReturnCd(returnCd);
                            sub.setKaiRowCount(rows.length);
                            for (int i = 0; i < rows.length; i++) {
                                int idx = i + 1;
                                sub.setKaiRId(idx, rows[i].id());
                                sub.setKaiRStatus(idx, rows[i].status());
                                sub.setKaiRBal(idx, new BigDecimal(rows[i].bal()));
                                sub.setKaiRLimit(idx, new BigDecimal(rows[i].limit()));
                                sub.setKaiRAvail(idx, new BigDecimal(rows[i].avail()));
                                sub.setKaiRUtil(idx, new BigDecimal(rows[i].util()));
                            }
                            sub.setKaiPopCount(popCount);
                            sub.setKaiPopBalTot(popBal);
                            sub.setKaiPopAvlTot(popAvl);
                            sub.setKaiNextKey(nextKey);
                            sub.setKaiMoreSw(moreSw);
                            byte[] out = sub.sliceBytes("KACTIN-AREA");
                            System.arraycopy(out, 0, ca, 0, ca.length);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .link(
                        argThat(s -> s.trim().equals(SUB_PGM)),
                        any(byte[].class),
                        eq(KACTIN_AREA_LEN));
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
                        eq(KACTIN_AREA_LEN));
    }

    private ArgumentCaptor<Object> captureLastSendMap(int times) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(times))
                .sendMap(
                        eq("MACTINA"),
                        captor.capture(),
                        any(),
                        anyBoolean(),
                        anyBoolean(),
                        anyBoolean());
        return captor;
    }

    private OcactinFields lastSendMapOutput() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MACTINA"), captor.capture(), any(), eq(false), eq(false), eq(true));
        return (OcactinFields) captor.getValue();
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MACTINA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OcactinFields out = (OcactinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getFdesco().trim()).isEqualTo("ALL");
        assertEquals(-1, out.getFiltl());
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORAI");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCACTIN");
        verify(appService).returnTransid(eq("ORAI"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalContextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq("MACTINA"), any(), any(), eq(true), eq(false), eq(true));
        verify(appService).returnTransid(eq("ORAI"), any(), eq(692));
    }

    // ───────────────────────── 2000-PROCESS-INPUT dispatch ─────────────────────────

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
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void mainLine_pf12Pressed_transfersControlToMenu() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("@");

        service.mainLine(appService);

        verify(appService)
                .xctl(
                        org.mockito.ArgumentMatchers.argThat(s -> s.trim().equals("OCMENU")),
                        any(),
                        eq(692));
    }

    @Test
    void mainLine_pf4Pressed_resendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MACTINA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OcactinFields out = (OcactinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_clearPressed_resendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("_");

        service.mainLine(appService);

        verify(appService).sendMap(eq("MACTINA"), any(), any(), eq(true), eq(false), eq(true));
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        OcactinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-APPLY-FILTER (ENTER) ─────────────────────────

    @Test
    void mainLine_enterAllFilter_runsPageAndShowsThirteenRows() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFilti("ALL"));
        Row[] rows = new Row[13];
        for (int i = 0; i < 13; i++) {
            long id = i + 1;
            rows[i] = new Row(id, "Y", "100.00", "500.00", "400.00", "20.00");
        }
        givenLinkResponse(
                "N", 20, new BigDecimal("2000.00"), new BigDecimal("1800.00"), 14L, "Y", rows);

        service.mainLine(appService);

        verify(appService)
                .link(
                        argThat(s -> s.trim().equals(SUB_PGM)),
                        any(byte[].class),
                        eq(KACTIN_AREA_LEN));
        OcactinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo("13 shown - PF8=more PF7=top");
        assertThat(out.getAid1o().trim()).isEqualTo("00000000001");
        assertThat(out.getAst1o().trim()).isEqualTo("Y");
        assertThat(out.getAid13o().trim()).isEqualTo("00000000013");
        assertThat(out.getPagenoo().trim()).isEqualTo("1");
        assertThat(out.getRuntoto().trim()).isEqualTo("20");
        assertEquals(-1, out.getFiltl());
    }

    @Test
    void mainLine_enterBadFilter_showsBadFilterMessageAndSkipsLink() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFilti("XYZ"));

        service.mainLine(appService);

        OcactinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_FILTER);
        assertEquals(-1, out.getFiltl());
        verify(appService, never()).link(anyString(), any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void mainLine_enterLowercaseDelFilter_convertGap_shouldBeAcceptedCaseInsensitivePerCobol() {
        // CONVERT-GAP: COBOL 6000-PARSE-FILTER does
        //   INSPECT WS-FILT-IN CONVERTING 'abcdefghijklmnopqrstuvwxyz' TO 'ABC...Z'
        // which translates each lowercase character individually, so typing "del"
        // resolves to filter code 'D' (DELINQUENT) exactly like "DEL". The converted
        // Java parseFilterInput() instead does
        //   wsFiltIn.replace("abcdefghijklmnopqrstuvwxyz", "ABC...Z")
        // which looks for the whole 26-character alphabet as one literal substring —
        // it can never occur inside a 10-character field, so the replace is a no-op
        // and lowercase input is never upper-cased. "del" then fails to match any key
        // in FILTER_CODE_TO_STATUS (all uppercase) and is rejected as an invalid filter.
        // Expected per COBOL ground truth: filter accepted, page run against DELINQUENT.
        // This test intentionally fails against current Java to flag the gap.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFilti("del"));
        givenLinkResponse("N", 0, BigDecimal.ZERO, BigDecimal.ZERO, 0L, "N");

        service.mainLine(appService);

        OcactinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isNotEqualTo(MSG_BAD_FILTER);
        verify(appService)
                .link(
                        argThat(s -> s.trim().equals(SUB_PGM)),
                        any(byte[].class),
                        eq(KACTIN_AREA_LEN));
    }

    @Test
    void mainLine_enterBlankFilter_defaultsToAllAndRunsPage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFilti("   "));
        givenLinkResponse("N", 0, BigDecimal.ZERO, BigDecimal.ZERO, 0L, "N");

        service.mainLine(appService);

        OcactinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
        verify(appService)
                .link(
                        argThat(s -> s.trim().equals(SUB_PGM)),
                        any(byte[].class),
                        eq(KACTIN_AREA_LEN));
    }

    @Test
    void mainLine_enterReceiveMapfail_treatsInputAsBlankAndDefaultsToAll() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        nextResp.set(36); // DFHRESP(MAPFAIL) — no receiveMap stub, fields stay at low-values post
        // fillLowValues
        givenLinkResponse("N", 0, BigDecimal.ZERO, BigDecimal.ZERO, 0L, "N");

        service.mainLine(appService);

        verify(appService)
                .link(
                        argThat(s -> s.trim().equals(SUB_PGM)),
                        any(byte[].class),
                        eq(KACTIN_AREA_LEN));
        OcactinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
    }

    @Test
    void mainLine_enterNoAccountsMatch_showsNoneFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFilti("DEL"));
        givenLinkResponse("N", 0, BigDecimal.ZERO, BigDecimal.ZERO, 0L, "N");

        service.mainLine(appService);

        OcactinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
    }

    // ───────────────────────── 3000-CALL-SUB error handling ─────────────────────────

    @Test
    void mainLine_linkCicsErrorRespNotNormal_showsLinkErrorMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFilti("ALL"));
        givenLinkCicsError(99);

        service.mainLine(appService);

        OcactinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LINK_ERR);
        assertEquals(-1, out.getFiltl());
    }

    @Test
    void mainLine_linkReturnsErrorCode_showsBrowseErrorMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFilti("ALL"));
        givenLinkResponse("E", 0, BigDecimal.ZERO, BigDecimal.ZERO, 0L, "N");

        service.mainLine(appService);

        OcactinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BROWSE_ERR);
    }

    // ───────────────────────── 2200-RESTART (PF7) ─────────────────────────

    @Test
    void mainLine_pf7NotYetListed_showsListFirstMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("7");

        service.mainLine(appService);

        OcactinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LIST_FIRST);
        assertEquals(-1, out.getFiltl());
        verify(appService, never()).link(anyString(), any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void mainLine_pf7AfterListed_reRunsFromTop() {
        OcactinFields wa = new OcactinFields(new WorkingStorage());
        wa.setWsStFilter("D");
        wa.setWsStListed("Y");
        givenPseudoConversation(1, wa.getWsState());
        when(appService.getEibaid()).thenReturn("7");
        givenLinkResponse(
                "N",
                3,
                new BigDecimal("300.00"),
                new BigDecimal("200.00"),
                4L,
                "N",
                new Row(1L, "D", "50.00", "500.00", "450.00", "10.00"));

        service.mainLine(appService);

        ArgumentCaptor<Long> startKey = ArgumentCaptor.forClass(Long.class);
        verify(appService)
                .link(
                        argThat(s -> s.trim().equals(SUB_PGM)),
                        any(byte[].class),
                        eq(KACTIN_AREA_LEN));
        OcactinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo("1 shown - end of selection");
        assertThat(out.getPagenoo().trim()).isEqualTo("1");
    }

    // ───────────────────────── 2300-PAGE-FWD (PF8) ─────────────────────────

    @Test
    void mainLine_pf8NotYetListed_showsListFirstMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("8");

        service.mainLine(appService);

        OcactinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LIST_FIRST);
        assertEquals(-1, out.getFiltl());
    }

    @Test
    void mainLine_pf8NoMorePages_showsEndFileMessage() {
        OcactinFields wa = new OcactinFields(new WorkingStorage());
        wa.setWsStFilter("A");
        wa.setWsStListed("Y");
        wa.setWsStMore("N");
        givenPseudoConversation(1, wa.getWsState());
        when(appService.getEibaid()).thenReturn("8");

        service.mainLine(appService);

        OcactinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_END_FILE);
        assertThat(out.getFdesco().trim()).isEqualTo("ALL");
        verify(appService, never()).link(anyString(), any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void mainLine_pf8WithMorePages_runsNextPageFromSavedKey() {
        OcactinFields wa = new OcactinFields(new WorkingStorage());
        wa.setWsStFilter("A");
        wa.setWsStListed("Y");
        wa.setWsStMore("Y");
        wa.setWsStNextKey(15L);
        wa.setWsStPageNo(1);
        wa.setWsStPopCnt(20);
        wa.setWsStPopBal(new BigDecimal("2000.00"));
        wa.setWsStPopAvl(new BigDecimal("1800.00"));
        givenPseudoConversation(1, wa.getWsState());
        when(appService.getEibaid()).thenReturn("8");
        givenLinkResponse(
                "N",
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                20L,
                "N",
                new Row(15L, "Y", "10.00", "500.00", "490.00", "2.00"),
                new Row(16L, "Y", "20.00", "500.00", "480.00", "4.00"));

        service.mainLine(appService);

        ArgumentCaptor<Long> startKey = ArgumentCaptor.forClass(Long.class);
        verify(appService)
                .link(
                        argThat(s -> s.trim().equals(SUB_PGM)),
                        any(byte[].class),
                        eq(KACTIN_AREA_LEN));
        OcactinFields out = lastSendMapOutput();
        assertThat(out.getErrmsgo().trim()).isEqualTo("2 shown - end of selection");
        assertThat(out.getAid1o().trim()).isEqualTo("00000000015");
        // KAI-WANT-KPI is 'N' on page-forward, so the population totals carried
        // forward from page 1 must NOT be overwritten by the (zeroed) sub response.
        assertThat(out.getPgbalo().trim()).isEqualTo("2,000.00");
        assertThat(out.getPagenoo().trim()).isEqualTo("2");
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcactin() {
        assertEquals("OCACTIN", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrai() {
        assertEquals("ORAI", service.getTransId());
    }

    @Test
    void getButtonDefs_returnsAllSevenNavigationButtons() {
        assertThat(service.getButtonDefs()).hasSize(7);
    }

    @Test
    void registerFsetFields_registersFiltFieldOnMactina() {
        AppRunner runner = mock(AppRunner.class);

        service.registerFsetFields(runner);

        verify(runner).registerFsetFields(eq("MACTINA"), eq(Set.of("FILT")));
    }

    @Test
    void getFieldMapping_mactina_returnsPopulatedMapping() {
        assertThat(service.getFieldMapping("MACTINA")).isNotNull();
    }

    @Test
    void getFieldMapping_unknownMap_returnsEmptyMapping() {
        assertThat(service.getFieldMapping("UNKNOWN")).isNotNull();
    }
}

package com.generated.orion.occrdin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
import com.generated.orion.occrdin.accessor.OccrdinFields;
import com.generated.orion.occrdin.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OccrdinService, generated from COBOL program OCCRDIN. All business logic is
 * private and is exercised solely through the public {@link OccrdinService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS SEND/RECEIVE/LINK/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OccrdinServiceTest {

    private static final String MSG_PROMPT = "Type a filter and press ENTER.";
    private static final String MSG_BAD_FILTER = "Filter not recognised - see the list above.";
    private static final String MSG_NONE_FOUND = "No cards match that filter.";
    private static final String MSG_END_FILE = "End of selection - no more cards.";
    private static final String MSG_LIST_FIRST = "Apply a filter first (press ENTER).";
    private static final String MSG_LINK_ERR = "Unable to reach the card browse engine.";
    private static final String MSG_BROWSE_ERR = "Error browsing the card file.";

    @Mock private AppService appService;

    private OccrdinService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OccrdinService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying CA-PGM-CONTEXT/CA-WORK-AREA. */
    private String commarea(int context, String workArea) {
        OccrdinFields helper = new OccrdinFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        if (workArea != null) {
            helper.setCaWorkArea(workArea);
        }
        return helper.getOrionCommarea();
    }

    private void givenPseudoConversation(int context) {
        givenPseudoConversation(context, null);
    }

    private void givenPseudoConversation(int context, String workArea) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commarea(context, workArea));
    }

    /**
     * Builds a CA-WORK-AREA(1:40) snippet: filter(1) + next-key(16) + page-no(3) + act(9) + ina(9)
     * + more(1) + listed(1).
     */
    private String stateWorkArea(
            String filter,
            String nextKey,
            int pageNo,
            int act,
            int ina,
            String more,
            String listed) {
        StringBuilder sb = new StringBuilder();
        sb.append(filter);
        sb.append(String.format("%-16s", nextKey == null ? "" : nextKey));
        sb.append(String.format("%03d", pageNo));
        sb.append(String.format("%09d", act));
        sb.append(String.format("%09d", ina));
        sb.append(more);
        sb.append(listed);
        return sb.toString();
    }

    /**
     * Stubs receiveMap to populate the MCRDINAI input fields (FILTI) captured from production code.
     */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OccrdinFields> populate) {
        doAnswer(
                        inv -> {
                            OccrdinFields f = inv.getArgument(1);
                            populate.accept(f);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MCRDINA"), any());
    }

    /** Stubs the LINK to OUCRDIN to succeed (resp 0) and populate KCRDIN output fields. */
    private void givenLinkSucceeds(java.util.function.Consumer<OccrdinFields> populate) {
        doAnswer(
                        inv -> {
                            byte[] linkCa = inv.getArgument(1);
                            OccrdinFields into = new OccrdinFields(new WorkingStorage());
                            into.writeBytes("KCRDIN-AREA", linkCa);
                            populate.accept(into);
                            byte[] updated = into.sliceBytes("KCRDIN-AREA");
                            System.arraycopy(
                                    updated, 0, linkCa, 0, Math.min(updated.length, linkCa.length));
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .link(anyString(), any(byte[].class), eq(817));
    }

    /** Stubs the LINK to fail at the CICS transport level (non-zero EIBRESP). */
    private void givenLinkFailsWithResp(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .link(anyString(), any(byte[].class), eq(817));
    }

    private ArgumentCaptor<Object> captureLastSendMap(int times, boolean erase) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(times))
                .sendMap(
                        eq("MCRDINA"),
                        captor.capture(),
                        any(),
                        eq(erase),
                        anyBoolean(),
                        anyBoolean());
        return captor;
    }

    // ───────────────────────── 0000-MAIN ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPromptAndAllFilter() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, true);
        OccrdinFields out = (OccrdinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getFdesco().trim()).isEqualTo("ALL");
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORCI");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCCRDIN");
        verify(appService).returnTransid(eq("ORCI"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalContextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService)
                .sendMap(eq("MCRDINA"), any(), any(), eq(true), anyBoolean(), anyBoolean());
        verify(appService).returnTransid(eq("ORCI"), any(), eq(692));
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
                        anyInt());
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
                        anyInt());
    }

    @Test
    void mainLine_pf4Pressed_resendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, true);
        OccrdinFields out = (OccrdinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_clearPressed_resendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("_");

        service.mainLine(appService);

        verify(appService)
                .sendMap(eq("MCRDINA"), any(), any(), eq(true), anyBoolean(), anyBoolean());
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessageDataOnly() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OccrdinFields out = (OccrdinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isNotEmpty();
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-APPLY-FILTER (ENTER) ─────────────────────────

    @Test
    void mainLine_enterAllFilter_runsPageAndCapturesTotals() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFilti("ALL"));
        givenLinkSucceeds(
                f -> {
                    f.setKciReturnCd(" ");
                    f.setKciRowCount(2);
                    f.setKciRNum(1, "1111222233334444");
                    f.setKciRAcct(1, 100L);
                    f.setKciRName(1, "JOHN DOE");
                    f.setKciRExpiry(1, "12/26");
                    f.setKciRStatus(1, "A");
                    f.setKciRNum(2, "1111222233335555");
                    f.setKciRAcct(2, 200L);
                    f.setKciRName(2, "JANE ROE");
                    f.setKciRExpiry(2, "01/27");
                    f.setKciRStatus(2, "A");
                    f.setKciActiveCnt(2);
                    f.setKciInactiveCnt(0);
                    f.setKciMoreSw("N");
                    f.setKciNextKey(" ");
                });

        service.mainLine(appService);

        verify(appService).link(anyString(), any(byte[].class), eq(817));
        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OccrdinFields out = (OccrdinFields) sendMapOut.getValue();
        assertThat(out.getCnm1o().trim()).isEqualTo("1111222233334444");
        assertThat(out.getCac1o().trim()).isEqualTo("00000000100");
        assertThat(out.getCna1o().trim()).isEqualTo("JOHN DOE");
        assertThat(out.getCst2o().trim()).isEqualTo("A");
        assertThat(out.getActcnto().trim()).isEqualTo("2");
        assertThat(out.getInacnto().trim()).isEqualTo("0");
        assertThat(out.getErrmsgo().trim()).isEqualTo("2 shown - end of selection");
    }

    @Test
    void mainLine_enterBadFilter_showsBadFilterMessageAndSkipsLink() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFilti("XYZ"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OccrdinFields out = (OccrdinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_FILTER);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_enterBlankFilter_treatedAsAllFilter() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFilti("          "));
        givenLinkSucceeds(
                f -> {
                    f.setKciReturnCd(" ");
                    f.setKciRowCount(0);
                    f.setKciActiveCnt(0);
                    f.setKciInactiveCnt(0);
                    f.setKciMoreSw("N");
                    f.setKciNextKey(" ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OccrdinFields out = (OccrdinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
        verify(appService).link(anyString(), any(byte[].class), eq(817));
    }

    @Test
    void mainLine_enterActiveFilterAbbreviationLowercase_foldedToUppercaseAndMatchesActive() {
        // Ground truth: COBOL 6000-PARSE-FILTER does
        //   INSPECT WS-FILT-IN CONVERTING 'abcdefghijklmnopqrstuvwxyz' TO 'ABCDEF...'
        // a character-by-character case fold, so a typed "act" becomes "ACT" and matches
        // the ACTIVE filter. Utility.inspectConverting(...) (used by parseFilterInput) is
        // verified to implement this same per-character mapping (not a literal substring
        // replace), so this is NOT a convert gap -- lowercase input is correctly folded
        // and treated as a valid ACTIVE filter, exactly like COBOL.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFilti("act"));
        givenLinkSucceeds(
                f -> {
                    f.setKciReturnCd(" ");
                    f.setKciRowCount(0);
                    f.setKciActiveCnt(0);
                    f.setKciInactiveCnt(0);
                    f.setKciMoreSw("N");
                    f.setKciNextKey(" ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OccrdinFields out = (OccrdinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
        verify(appService).link(anyString(), any(byte[].class), eq(817));
    }

    @Test
    void mainLine_enterLinkTransportError_showsLinkErrorAndSkipsPageDisplay() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFilti("ALL"));
        givenLinkFailsWithResp(99);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OccrdinFields out = (OccrdinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LINK_ERR);
    }

    @Test
    void mainLine_enterSubReturnsError_showsBrowseErrorMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFilti("ALL"));
        givenLinkSucceeds(f -> f.setKciReturnCd("E"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OccrdinFields out = (OccrdinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BROWSE_ERR);
    }

    // ───────────────────────── 2200-RESTART (PF7) ─────────────────────────

    @Test
    void mainLine_pf7WithoutPriorListing_showsListFirstMessageAndSkipsLink() {
        givenPseudoConversation(1, stateWorkArea("A", null, 0, 0, 0, "N", "N"));
        when(appService.getEibaid()).thenReturn("7");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OccrdinFields out = (OccrdinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LIST_FIRST);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_pf7AfterListing_restartsFromTopWithSameFilter() {
        givenPseudoConversation(1, stateWorkArea("Y", "SOMEKEY", 3, 5, 2, "Y", "Y"));
        when(appService.getEibaid()).thenReturn("7");
        givenLinkSucceeds(
                f -> {
                    f.setKciReturnCd(" ");
                    f.setKciRowCount(1);
                    f.setKciRNum(1, "1111222233336666");
                    f.setKciRAcct(1, 300L);
                    f.setKciRName(1, "ACTIVE USER");
                    f.setKciRExpiry(1, "06/28");
                    f.setKciRStatus(1, "ACTIVE");
                    f.setKciActiveCnt(9);
                    f.setKciInactiveCnt(1);
                    f.setKciMoreSw("N");
                    f.setKciNextKey(" ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OccrdinFields out = (OccrdinFields) sendMapOut.getValue();
        assertThat(out.getCnm1o().trim()).isEqualTo("1111222233336666");
        assertThat(out.getPagenoo().trim()).isEqualTo("1");
        assertThat(out.getActcnto().trim()).isEqualTo("9");
        assertThat(out.getInacnto().trim()).isEqualTo("1");
    }

    // ───────────────────────── 2300-PAGE-FWD (PF8) ─────────────────────────

    @Test
    void mainLine_pf8WithoutPriorListing_showsListFirstMessageAndSkipsLink() {
        givenPseudoConversation(1, stateWorkArea("A", null, 0, 0, 0, "N", "N"));
        when(appService.getEibaid()).thenReturn("8");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OccrdinFields out = (OccrdinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LIST_FIRST);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_pf8NoMoreRows_showsEndOfSelectionAndSkipsLink() {
        givenPseudoConversation(1, stateWorkArea("Y", "SOMEKEY", 1, 5, 2, "N", "Y"));
        when(appService.getEibaid()).thenReturn("8");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OccrdinFields out = (OccrdinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_END_FILE);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_pf8MoreAvailable_advancesPageAndDoesNotRecaptureTotals() {
        givenPseudoConversation(1, stateWorkArea("Y", "RESUMEKEY0000000", 1, 5, 2, "Y", "Y"));
        when(appService.getEibaid()).thenReturn("8");
        givenLinkSucceeds(
                f -> {
                    f.setKciReturnCd(" ");
                    f.setKciRowCount(1);
                    f.setKciRNum(1, "1111222233337777");
                    f.setKciRAcct(1, 400L);
                    f.setKciRName(1, "NEXT PAGE USER");
                    f.setKciRExpiry(1, "09/29");
                    f.setKciRStatus(1, "ACTIVE");
                    // KCI-WANT-KPI is "N" on page-forward, so 2400-RUN-PAGE/runPageQuery must NOT
                    // overwrite WS-ST-ACT/WS-ST-INA even though the sub happens to return counts
                    // here.
                    f.setKciActiveCnt(999);
                    f.setKciInactiveCnt(999);
                    f.setKciMoreSw("N");
                    f.setKciNextKey(" ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OccrdinFields out = (OccrdinFields) sendMapOut.getValue();
        assertThat(out.getCnm1o().trim()).isEqualTo("1111222233337777");
        assertThat(out.getPagenoo().trim()).isEqualTo("2");
        // Ground truth per COBOL 2400-RUN-PAGE: totals only refresh when KCI-DO-KPI is set,
        // which only happens on ENTER/PF7 (first page), not on PF8 (KCI-SKIP-KPI).
        assertThat(out.getActcnto().trim()).isEqualTo("5");
        assertThat(out.getInacnto().trim()).isEqualTo("2");
    }

    // ───────────────────────── coverage: full 13-row page, more/done suffix, filter branches
    // ─────────────────────────

    @Test
    void mainLine_enterAllFilterFullPageOfThirteenRows_placesEveryRowAndShowsMoreSuffix() {
        // COBOL ground truth: 4000-SHOW-PAGE performs 4300-PLACE-ROW for WS-I = 1 to
        // KCI-ROW-COUNT (max 13 rows/page); 4900-BUILD-MSG appends WS-M-MORE
        // (' shown - PF8=more PF7=top') when WS-ST-MORE = 'Y'.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFilti("ALL"));
        givenLinkSucceeds(
                f -> {
                    f.setKciReturnCd(" ");
                    f.setKciRowCount(13);
                    for (int i = 1; i <= 13; i++) {
                        f.setKciRNum(i, "111122223333" + String.format("%04d", i));
                        f.setKciRAcct(i, i * 10L);
                        f.setKciRName(i, "CARDHOLDER " + i);
                        f.setKciRExpiry(i, "12/2" + (i % 10));
                        f.setKciRStatus(i, "A");
                    }
                    f.setKciActiveCnt(13);
                    f.setKciInactiveCnt(0);
                    f.setKciMoreSw("Y");
                    f.setKciNextKey("NEXTKEY0000000000");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OccrdinFields out = (OccrdinFields) sendMapOut.getValue();
        assertThat(out.getCnm1o().trim()).isEqualTo("1111222233330001");
        assertThat(out.getCnm13o().trim()).isEqualTo("1111222233330013");
        assertThat(out.getCac13o().trim()).isEqualTo("00000000130");
        assertThat(out.getCna13o().trim()).isEqualTo("CARDHOLDER 13");
        assertThat(out.getErrmsgo().trim()).isEqualTo("13 shown - PF8=more PF7=top");
    }

    @Test
    void mainLine_enterInactiveFilter_parsesAndRunsPage() {
        // CONVERT-GAP-adjacent ground truth: 6000-PARSE-FILTER maps "INA" -> WS-ST-FILTER = 'N'.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFilti("INA"));
        givenLinkSucceeds(
                f -> {
                    f.setKciReturnCd(" ");
                    f.setKciRowCount(0);
                    f.setKciActiveCnt(0);
                    f.setKciInactiveCnt(0);
                    f.setKciMoreSw("N");
                    f.setKciNextKey(" ");
                });

        service.mainLine(appService);

        ArgumentCaptor<byte[]> linkCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(appService).link(anyString(), linkCaptor.capture(), eq(817));
        OccrdinFields sent = new OccrdinFields(new WorkingStorage());
        sent.writeBytes("KCRDIN-AREA", linkCaptor.getValue());
        assertThat(sent.getKciFilter()).isEqualTo("N");
    }

    @Test
    void mainLine_enterExpiringFilter_parsesAndRunsPage() {
        // Ground truth: 6000-PARSE-FILTER maps "EXP" -> WS-ST-FILTER = 'X'.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFilti("EXP"));
        givenLinkSucceeds(
                f -> {
                    f.setKciReturnCd(" ");
                    f.setKciRowCount(0);
                    f.setKciActiveCnt(0);
                    f.setKciInactiveCnt(0);
                    f.setKciMoreSw("N");
                    f.setKciNextKey(" ");
                });

        service.mainLine(appService);

        ArgumentCaptor<byte[]> linkCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(appService).link(anyString(), linkCaptor.capture(), eq(817));
        OccrdinFields sent = new OccrdinFields(new WorkingStorage());
        sent.writeBytes("KCRDIN-AREA", linkCaptor.getValue());
        assertThat(sent.getKciFilter()).isEqualTo("X");
    }

    @Test
    void mainLine_enterExpiringFilterLinkTransportError_showsExpiringDescription() {
        // Ground truth: 3000-CALL-SUB performs 6100-SET-FDESC on LINK failure, and
        // WS-ST-FILTER is already 'X' (EXPIRING) at that point because 2100-APPLY-FILTER
        // sets KCI-FILTER/WS-ST-FILTER before invoking the sub-program.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFilti("EXP"));
        givenLinkFailsWithResp(99);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OccrdinFields out = (OccrdinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LINK_ERR);
        assertThat(out.getFdesco().trim()).isEqualTo("EXPIRING");
    }

    @Test
    void mainLine_pf8NoMoreRowsWithInactiveFilter_showsInactiveDescription() {
        // Ground truth: 2300-PAGE-FWD performs 6100-SET-FDESC when WS-ST-MORE <> 'Y';
        // with WS-ST-FILTER = 'N' this resolves to INACTIVE.
        givenPseudoConversation(1, stateWorkArea("N", "SOMEKEY", 1, 0, 5, "N", "Y"));
        when(appService.getEibaid()).thenReturn("8");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OccrdinFields out = (OccrdinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_END_FILE);
        assertThat(out.getFdesco().trim()).isEqualTo("INACTIVE");
    }

    @Test
    void mainLine_receiveMapRespCd36_treatsInputAsLowValuesAndDefaultsToAllFilter() {
        // Ground truth: 2050-RECEIVE performs "IF EIBRESP = DFHRESP(MAPFAIL) MOVE
        // LOW-VALUES TO MCRDINAI", so a MAPFAIL response clears FILTI and
        // 6000-PARSE-FILTER treats the blank input as the ALL filter.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        doAnswer(
                        inv -> {
                            nextResp.set(36);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MCRDINA"), any());
        givenLinkSucceeds(
                f -> {
                    f.setKciReturnCd(" ");
                    f.setKciRowCount(0);
                    f.setKciActiveCnt(0);
                    f.setKciInactiveCnt(0);
                    f.setKciMoreSw("N");
                    f.setKciNextKey(" ");
                });

        service.mainLine(appService);

        ArgumentCaptor<byte[]> linkCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(appService).link(anyString(), linkCaptor.capture(), eq(817));
        OccrdinFields sent = new OccrdinFields(new WorkingStorage());
        sent.writeBytes("KCRDIN-AREA", linkCaptor.getValue());
        assertThat(sent.getKciFilter()).isEqualTo("A");
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOccrdin() {
        assertEquals("OCCRDIN", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrci() {
        assertEquals("ORCI", service.getTransId());
    }

    @Test
    void getButtonDefs_delegatesToMetadataWithoutError() {
        assertNotNull(service.getButtonDefs());
    }

    @Test
    void getFieldMapping_delegatesToMetadataForKnownMap() {
        assertNotNull(service.getFieldMapping("MCRDINA"));
    }

    @Test
    void registerFsetFields_delegatesToMetadataWithoutError() {
        com.appruntime.AppRunner runner = org.mockito.Mockito.mock(com.appruntime.AppRunner.class);

        service.registerFsetFields(runner);

        verify(runner).registerFsetFields(eq("MCRDINA"), any());
    }
}

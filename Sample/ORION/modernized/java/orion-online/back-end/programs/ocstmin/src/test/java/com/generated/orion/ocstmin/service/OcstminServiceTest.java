package com.generated.orion.ocstmin.service;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.FormatTimeResult;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.ocstmin.accessor.OcstminFields;
import com.generated.orion.ocstmin.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Unit tests for OcstminService, generated from COBOL program OCSTMIN (ORION-CCMS on-line statement
 * inquiry with paging via the OUSTMIN browse subroutine). All business logic is private and is
 * exercised solely through the public {@link OcstminService#mainLine(AppService)} entry point,
 * driven by an {@link AppService} mock that emulates CICS SEND/RECEIVE/LINK/XCTL/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcstminServiceTest {

    private static final String MAP_NAME = "MSTMINA";
    private static final String MENU_PGM = "OCRPTMN";
    private static final String SUB_PGM = "OUSTMIN";
    private static final String TRANID = "ORSI";
    private static final String PGMNAME = "OCSTMIN";
    private static final int COMMAREA_LENGTH = 692;
    private static final int LINK_LENGTH = 567;

    private static final String MSG_PROMPT = "Enter account id (blank=first) and ENTER.";
    private static final String MSG_BAD_ACCT = "Account id must be numeric.";
    private static final String MSG_BAD_CYCLE = "Cycle must be numeric (YYYYMM).";
    private static final String MSG_NONE_FOUND = "No statements found from that point.";
    private static final String MSG_BROWSE_ERR = "Error browsing the statement file.";
    private static final String MSG_END_FILE = "End of file - no more statements.";

    @Mock private AppService appService;

    private OcstminService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OcstminService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    private String commareaWithContext(int context) {
        OcstminFields helper = new OcstminFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(context));
    }

    /**
     * Stubs a pseudo-conversational commarea that additionally carries WS-PAGE-STATE (mirrored into
     * CA-WORK-AREA(1:36) by 9000-RETURN / read back by 0000-MAIN), so PF7/PF8 paging tests can
     * drive WS-PS-MORE / WS-PS-FLT-* without a fragile two-round-trip simulation.
     */
    private void givenPseudoConversationWithPageState(
            int context,
            long nextAcct,
            int nextCycle,
            String more,
            long fltAcct,
            int fltCycle,
            String fltSet) {
        OcstminFields helper = new OcstminFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        helper.setWsPsNextAcct(nextAcct);
        helper.setWsPsNextCycle(nextCycle);
        helper.setWsPsMore(more);
        helper.setWsPsFltAcct(fltAcct);
        helper.setWsPsFltCycle(fltCycle);
        helper.setWsPsFltSet(fltSet);
        helper.setCaWorkArea(
                Utility.setSubstring(
                        String.valueOf(helper.getCaWorkArea()),
                        1,
                        36,
                        String.valueOf(helper.getWsPageState())));
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(helper.getOrionCommarea());
    }

    private void givenReceiveMapPopulates(Consumer<OcstminFields> populate) {
        doAnswer(
                        inv -> {
                            OcstminFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    private record Row(
            long acct,
            int cycle,
            BigDecimal open,
            BigDecimal close,
            BigDecimal minDue,
            String dueDate) {}

    /**
     * Stubs the LINK to OUSTMIN so the (mutated in place) KSTMB-PARM byte[] carries a successful
     * browse result.
     */
    private void givenBrowseSucceeds(
            int rowCnt, long nextAcct, int nextCycle, String more, Row... rows) {
        doAnswer(
                        inv -> {
                            byte[] commarea = inv.getArgument(1);
                            OcstminFields decoded = new OcstminFields(new WorkingStorage());
                            decoded.writeBytes("KSTMB-PARM", commarea);
                            decoded.setKsbStatus("00");
                            decoded.setKsbRowCnt(rowCnt);
                            decoded.setKsbNextAcct(nextAcct);
                            decoded.setKsbNextCycle(nextCycle);
                            decoded.setKsbMore(more);
                            for (int i = 0; i < rows.length; i++) {
                                int idx = i + 1;
                                decoded.setKsbRAcct(idx, rows[i].acct());
                                decoded.setKsbRCycle(idx, rows[i].cycle());
                                decoded.setKsbROpen(idx, rows[i].open());
                                decoded.setKsbRClose(idx, rows[i].close());
                                decoded.setKsbRMindue(idx, rows[i].minDue());
                                decoded.setKsbRDuedt(idx, rows[i].dueDate());
                            }
                            byte[] updated = decoded.sliceBytes("KSTMB-PARM");
                            System.arraycopy(
                                    updated,
                                    0,
                                    commarea,
                                    0,
                                    Math.min(updated.length, commarea.length));
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .link(argThat(s -> s != null && s.trim().equals(SUB_PGM)), any(), eq(LINK_LENGTH));
    }

    /** Stubs the LINK to OUSTMIN to simulate a CICS failure (non-zero EIBRESP). */
    private void givenBrowseLinkFails(int respCode) {
        doAnswer(
                        inv -> {
                            nextResp.set(respCode);
                            return null;
                        })
                .when(appService)
                .link(argThat(s -> s != null && s.trim().equals(SUB_PGM)), any(), eq(LINK_LENGTH));
    }

    private static String normalizeWs(String s) {
        return s.trim().replaceAll("\\s+", " ");
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OcstminFields out = (OcstminFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo(TRANID);
        assertThat(out.getPgmnameo().trim()).isEqualTo(PGMNAME);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-22");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        verify(appService).returnTransid(eq(TRANID), any(), eq(COMMAREA_LENGTH));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        verify(appService).returnTransid(eq(TRANID), any(), eq(COMMAREA_LENGTH));
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
                        argThat(s -> s.trim().equals(MENU_PGM)),
                        commareaCaptor.capture(),
                        eq(COMMAREA_LENGTH));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());

        OcstminFields decoded = new OcstminFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo(PGMNAME);
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo(TRANID);
        assertThat(decoded.getCaPgmContext()).isEqualTo(0);
    }

    @Test
    void mainLine_pf4Pressed_clearsAndResendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OcstminFields out = (OcstminFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), dataOnlyOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstminFields dataOnlyScreen = (OcstminFields) dataOnlyOut.getValue();
        assertThat(dataOnlyScreen.getErrmsgo().trim())
                .isEqualTo(dataOnlyScreen.getWsMsgInvalidKey().trim());
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-SEARCH / 2110-EDIT-ACCT / 2120-EDIT-CYCLE
    // ─────────────────────────

    @Test
    void mainLine_enterKey_blankFilters_startsFromFileBeginningAndDisplaysRows() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setFraccti("           "); // 11 spaces
                    f.setFrcyci("      "); // 6 spaces
                });
        givenBrowseSucceeds(
                2,
                99900000001L,
                202502,
                "N",
                new Row(
                        12345,
                        202501,
                        new BigDecimal("100.00"),
                        new BigDecimal("200.50"),
                        new BigDecimal("25.00"),
                        "2025-02-15"),
                new Row(
                        67890,
                        202501,
                        new BigDecimal("300.00"),
                        new BigDecimal("150.25"),
                        new BigDecimal("10.00"),
                        "2025-02-20"));

        service.mainLine(appService);

        verify(appService).link(argThat(s -> s.trim().equals(SUB_PGM)), any(), eq(LINK_LENGTH));

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstminFields out = (OcstminFields) sendMapOut.getValue();
        assertThat(normalizeWs(out.getErrmsgo())).isEqualTo("2 statement(s) displayed.");
        assertThat(out.getSa1o().trim()).isEqualTo("00000012345");
        assertThat(out.getSc1o().trim()).isEqualTo("202501");
        assertThat(out.getSo1o().trim()).isEqualTo("100.00");
        assertThat(out.getSl1o().trim()).isEqualTo("200.50");
        assertThat(out.getSm1o().trim()).isEqualTo("25.00");
        assertThat(out.getSd1o().trim()).isEqualTo("2025-02-15");
        assertThat(out.getSa2o().trim()).isEqualTo("00000067890");
        assertThat(out.getSa3o().trim()).isEmpty();
    }

    @Test
    void mainLine_enterKey_validAcctAndCycleFilters_setsBrowseStartKeyFromInput() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setFraccti("  123456789"); // 2 spaces + 9 digits = 11 chars -> 123456789
                    f.setFrcyci("202501");
                });
        givenBrowseSucceeds(0, 0, 0, "N");

        service.mainLine(appService);

        ArgumentCaptor<byte[]> linkCommarea = ArgumentCaptor.forClass(byte[].class);
        verify(appService)
                .link(
                        argThat(s -> s.trim().equals(SUB_PGM)),
                        linkCommarea.capture(),
                        eq(LINK_LENGTH));
        OcstminFields sentToBrowse = new OcstminFields(new WorkingStorage());
        sentToBrowse.writeBytes("KSTMB-PARM", linkCommarea.getValue());
        assertThat(sentToBrowse.getKsbStartAcct()).isEqualTo(123456789L);
        assertThat(sentToBrowse.getKsbStartCycle()).isEqualTo(202501);
        assertThat(sentToBrowse.getKsbMode().trim()).isEqualTo("ACCT");
        assertThat(sentToBrowse.getKsbMaxRows()).isEqualTo(6);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstminFields out = (OcstminFields) sendMapOut.getValue();
        assertThat(normalizeWs(out.getErrmsgo())).isEqualTo(MSG_NONE_FOUND);
    }

    @Test
    void mainLine_enterKey_nonNumericAccount_showsBadAcctMessageAndSkipsBrowse() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setFraccti("1234567A89 ");
                    f.setFrcyci("202501");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstminFields out = (OcstminFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_ACCT);
        verify(appService, never()).link(anyString(), any(), anyInt());
    }

    @Test
    void mainLine_enterKey_validAcctNonNumericCycle_showsBadCycleMessageAndSkipsBrowse() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setFraccti("00000012345");
                    f.setFrcyci("20250A");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstminFields out = (OcstminFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_CYCLE);
        verify(appService, never()).link(anyString(), any(), anyInt());
        // only one sendDataOnly happened: the account edit passed silently, only the cycle edit
        // failure sent data.
        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(false), eq(false), eq(true));
    }

    @Test
    void mainLine_enterKey_mapfailReceive_treatsInputAsLowValuesAndBrowsesFromStartOfFile() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        nextResp.set(36); // DFHRESP(MAPFAIL)
        givenReceiveMapPopulates(
                f -> {
                    // production leaves the receive buffer untouched on MAPFAIL; the service
                    // itself fills MSTMINAI with low-values before editing.
                });
        givenBrowseSucceeds(0, 0, 0, "N");

        service.mainLine(appService);

        ArgumentCaptor<byte[]> linkCommarea = ArgumentCaptor.forClass(byte[].class);
        verify(appService)
                .link(
                        argThat(s -> s.trim().equals(SUB_PGM)),
                        linkCommarea.capture(),
                        eq(LINK_LENGTH));
        OcstminFields sentToBrowse = new OcstminFields(new WorkingStorage());
        sentToBrowse.writeBytes("KSTMB-PARM", linkCommarea.getValue());
        assertThat(sentToBrowse.getKsbStartAcct()).isEqualTo(0L);
        assertThat(sentToBrowse.getKsbStartCycle()).isEqualTo(0);
    }

    // ───────────────────────── 3000-DO-BROWSE error path ─────────────────────────

    @Test
    void mainLine_enterKey_browseLinkFails_showsBrowseErrorMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setFraccti("           ");
                    f.setFrcyci("      ");
                });
        givenBrowseLinkFails(99);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstminFields out = (OcstminFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BROWSE_ERR);
        assertThat(out.getSa1o().trim()).isEmpty();
    }

    // ───────────────────────── 2200-PAGE-FWD ─────────────────────────

    @Test
    void mainLine_pf8Pressed_moreAvailable_pagesForwardFromSavedNextKey() {
        givenPseudoConversationWithPageState(1, 22222222222L, 202502, "Y", 0, 0, "N");
        when(appService.getEibaid()).thenReturn("8");
        givenBrowseSucceeds(
                1,
                11111111111L,
                202503,
                "Y",
                new Row(
                        55555,
                        202502,
                        new BigDecimal("10.00"),
                        new BigDecimal("20.00"),
                        new BigDecimal("1.00"),
                        "2025-03-01"));

        service.mainLine(appService);

        verify(appService).link(argThat(s -> s.trim().equals(SUB_PGM)), any(), eq(LINK_LENGTH));
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstminFields out = (OcstminFields) sendMapOut.getValue();
        assertThat(normalizeWs(out.getErrmsgo())).isEqualTo("1 statement(s) displayed.");
        assertThat(out.getSa1o().trim()).isEqualTo("00000055555");
    }

    @Test
    void mainLine_pf8Pressed_noMoreAvailable_showsEndOfFileMessageWithoutBrowsing() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("8");
        // WS-PS-MORE defaults to 'N' (fresh WorkingStorage / commareaWithContext), so no
        // extra stub is required to represent "no more rows".

        service.mainLine(appService);

        verify(appService, never()).link(anyString(), any(), anyInt());
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstminFields out = (OcstminFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_END_FILE);
    }

    // ───────────────────────── 2300-RESTART ─────────────────────────

    @Test
    void mainLine_pf7Pressed_filterWasSet_restartsBrowseFromRememberedFilter() {
        // WS-PS-FLT-* / WS-PS-FLT-SET travel in WS-PAGE-STATE (mirrored into
        // CA-WORK-AREA(1:36) across pseudo-conversational turns); drive them directly
        // rather than re-simulating a prior ENTER round-trip.
        givenPseudoConversationWithPageState(1, 0, 0, "N", 54321L, 202412, "Y");
        when(appService.getEibaid()).thenReturn("7");
        givenBrowseSucceeds(0, 0, 0, "N");

        service.mainLine(appService);

        ArgumentCaptor<byte[]> linkCommarea = ArgumentCaptor.forClass(byte[].class);
        verify(appService)
                .link(
                        argThat(s -> s.trim().equals(SUB_PGM)),
                        linkCommarea.capture(),
                        eq(LINK_LENGTH));
        OcstminFields sentToBrowse = new OcstminFields(new WorkingStorage());
        sentToBrowse.writeBytes("KSTMB-PARM", linkCommarea.getValue());
        assertThat(sentToBrowse.getKsbStartAcct()).isEqualTo(54321L);
        assertThat(sentToBrowse.getKsbStartCycle()).isEqualTo(202412);
    }

    @Test
    void mainLine_pf7Pressed_filterNeverSet_restartsBrowseFromZero() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("7");
        givenBrowseSucceeds(0, 0, 0, "N");

        service.mainLine(appService);

        ArgumentCaptor<byte[]> linkCommarea = ArgumentCaptor.forClass(byte[].class);
        verify(appService)
                .link(
                        argThat(s -> s.trim().equals(SUB_PGM)),
                        linkCommarea.capture(),
                        eq(LINK_LENGTH));
        OcstminFields sentToBrowse = new OcstminFields(new WorkingStorage());
        sentToBrowse.writeBytes("KSTMB-PARM", linkCommarea.getValue());
        assertThat(sentToBrowse.getKsbStartAcct()).isEqualTo(0L);
        assertThat(sentToBrowse.getKsbStartCycle()).isEqualTo(0);
    }

    // ───────────────────────── 4000/4100/4200 row formatting & placement ─────────────────────────

    @Test
    void mainLine_enterKey_sixRowsReturned_allSixScreenLinesPopulatedInOrder() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setFraccti("           ");
                    f.setFrcyci("      ");
                });
        givenBrowseSucceeds(
                6,
                0,
                0,
                "Y",
                new Row(1, 202401, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "2024-02-01"),
                new Row(2, 202402, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "2024-02-02"),
                new Row(3, 202403, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "2024-02-03"),
                new Row(4, 202404, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "2024-02-04"),
                new Row(5, 202405, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "2024-02-05"),
                new Row(
                        6,
                        202406,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        "2024-02-06"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstminFields out = (OcstminFields) sendMapOut.getValue();
        assertThat(out.getSc1o().trim()).isEqualTo("202401");
        assertThat(out.getSc2o().trim()).isEqualTo("202402");
        assertThat(out.getSc3o().trim()).isEqualTo("202403");
        assertThat(out.getSc4o().trim()).isEqualTo("202404");
        assertThat(out.getSc5o().trim()).isEqualTo("202405");
        assertThat(out.getSc6o().trim()).isEqualTo("202406");
        assertThat(out.getSd6o().trim()).isEqualTo("2024-02-06");
        assertThat(normalizeWs(out.getErrmsgo())).isEqualTo("6 statement(s) displayed.");
    }

    // ───────────────────────── BMS metadata delegation ─────────────────────────

    @Test
    void getProgramNameAndTransId_returnCobolProgramIdentity() {
        assertThat(service.getProgramName()).isEqualTo(PGMNAME);
        assertThat(service.getTransId()).isEqualTo(TRANID);
    }

    @Test
    void getButtonDefs_delegatesToBmsMetadata_returnsNonEmptyList() {
        java.util.List<ScreenResponse.ButtonDef> buttonDefs = service.getButtonDefs();

        assertThat(buttonDefs).isNotNull().isNotEmpty();
    }

    @Test
    void registerFsetFields_delegatesToBmsMetadata_registersMapFields() {
        // AppRunner is a concrete framework class (no mocking needed here); a real
        // instance lets us observe the registration through its own getFsetFields lookup.
        AppRunner appRunner = new AppRunner(java.util.Map.of(), java.util.Map.of());

        service.registerFsetFields(appRunner);

        assertThat(appRunner.getFsetFields(MAP_NAME)).isNotEmpty();
    }

    @Test
    void getFieldMapping_forMstminaMap_returnsMapping() {
        FieldMapping mapping = service.getFieldMapping(MAP_NAME);

        assertThat(mapping).isNotNull();
    }
}

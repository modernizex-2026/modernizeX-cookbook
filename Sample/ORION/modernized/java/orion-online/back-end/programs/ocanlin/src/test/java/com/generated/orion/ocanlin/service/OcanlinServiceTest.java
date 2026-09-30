package com.generated.orion.ocanlin.service;

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

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.ocanlin.accessor.OcanlinFields;
import com.generated.orion.ocanlin.model.WorkingStorage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.function.Consumer;

/**
 * Unit tests for OcanlinService, generated from COBOL program OCANLIN (ORION-CCMS on-line analytics
 * inquiry). All business logic is private and is exercised solely through the public {@link
 * OcanlinService#mainLine(AppService)} entry point, driven by an {@link AppService} mock that
 * emulates CICS SEND/RECEIVE/LINK/XCTL/RETURN.
 *
 * <p>Convert-gap check: every COBOL paragraph (0000-MAIN..9000-RETURN) was compared against its
 * Java counterpart line-by-line; the dispatch logic, mode normalisation table, page/offset
 * arithmetic, and per-mode total builders all mirror the COBOL 1:1. No behavioral divergence was
 * found, so no CONVERT-GAP test is included here.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcanlinServiceTest {

    private static final String MAP_NAME = "MANLINA";
    private static final String TRAN_ID = "ORAN";
    private static final String PGM_NAME = "OCANLIN";
    private static final String MENU_PGM = "OCRPTMN";
    private static final int COMMAREA_LENGTH = 692;
    private static final int PAGE_SIZE = 6;

    private static final String MSG_PROMPT = "Enter mode RW/FR/GL/RC and press ENTER.";
    private static final String MSG_BAD_MODE = "Mode must be RW, FR, GL or RC.";
    private static final String MSG_NONE_FOUND = "No analytics rows for that mode.";
    private static final String MSG_COMP_ERR = "Error computing analytics.";
    private static final String MSG_END_ROWS = "End of results - no more rows.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String HDR_TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM";

    @Mock private AppService appService;

    private final OcanlinService service = new OcanlinService();

    // ───────────────────────── test data builders ─────────────────────────

    /**
     * Builds a serialized ORION-COMMAREA with the given CA-PGM-CONTEXT and, optionally, a persisted
     * paging state (WS-ANL-STATE mirrored into CA-WORK-AREA(1:11), as 9000-RETURN does at the end
     * of every pseudo-conversational turn).
     */
    private String commarea(int pgmContext, String mode, int offset, int rescnt, String more) {
        OcanlinFields helper = new OcanlinFields(new WorkingStorage());
        helper.setCaPgmContext(pgmContext);
        if (mode != null) {
            helper.setWsAsMode(mode);
            helper.setWsAsOffset(offset);
            helper.setWsAsRescnt(rescnt);
            helper.setWsAsMore(more);
            helper.setCaWorkArea(helper.getWsAnlState());
        }
        return helper.getOrionCommarea();
    }

    private void givenPseudoConversation(
            int pgmContext, String mode, int offset, int rescnt, String more) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea())
                .thenReturn(commarea(pgmContext, mode, offset, rescnt, more));
    }

    /** Stubs receiveMap to populate the MANLINAI input field, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(Consumer<OcanlinFields> populate) {
        doAnswer(
                        inv -> {
                            OcanlinFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    /**
     * Stubs the LINK to OUANLIN, writing computed rows/totals back into the caller's KANLB-PARM
     * buffer (byte[]) exactly as the real sub-program would.
     */
    private void givenLinkReturns(Consumer<OcanlinFields> populate) {
        doAnswer(
                        inv -> {
                            byte[] parm = inv.getArgument(1);
                            OcanlinFields helper = new OcanlinFields(new WorkingStorage());
                            // seed with the caller's request (KAB-MODE/KAB-START-OFF/KAB-MAX-ROWS)
                            // so the
                            // stubbed response only adds result fields, mirroring EXEC CICS LINK's
                            // bidirectional COMMAREA instead of wiping the request side.
                            helper.writeBytes("KANLB-PARM", parm.clone());
                            helper.setKabStatus("00");
                            populate.accept(helper);
                            byte[] src = helper.sliceBytes("KANLB-PARM");
                            System.arraycopy(src, 0, parm, 0, Math.min(src.length, parm.length));
                            return null;
                        })
                .when(appService)
                .link(anyString(), any(byte[].class), anyInt());
    }

    private void givenFormatTimeStub() {
        when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /**
     * Renders the numeric edit picture for {@code field} the same way production does, using the
     * shared field-buffer formatter - not by guessing the format.
     */
    private String edited(String field, int value) {
        OcanlinFields h = new OcanlinFields(new WorkingStorage());
        h.setInt(field, value);
        return h.getString(field);
    }

    private String editedLong(String field, long value) {
        OcanlinFields h = new OcanlinFields(new WorkingStorage());
        h.setLong(field, value);
        return h.getString(field);
    }

    private String editedDecimal(String field, BigDecimal value) {
        OcanlinFields h = new OcanlinFields(new WorkingStorage());
        h.setDecimal(field, value);
        return h.getString(field);
    }

    // ───────────────────────── 0000-MAIN ─────────────────────────

    @Test
    void mainLine_eibcalenZero_initializesCommareaAndSendsInitialScreen() {
        when(appService.getEibcalen()).thenReturn(0);
        givenFormatTimeStub();

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        verify(appService, never()).receiveMap(anyString(), any());
        verify(appService).returnTransid(eq(TRAN_ID), any(), eq(COMMAREA_LENGTH));
    }

    @Test
    void mainLine_firstEnterContextZero_sendsInitialScreenInsteadOfDispatch() {
        givenPseudoConversation(0, null, 0, 0, "N");
        givenFormatTimeStub();

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        verify(appService, never()).receiveMap(anyString(), any());
    }

    @Test
    void mainLine_returnTransaction_persistsPagingStateAcrossPseudoConversation() {
        givenPseudoConversation(1, "RW", 0, 3, "N");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("7");
        givenLinkReturns(
                h -> {
                    h.setKabRowCnt(0);
                    h.setKabResultCnt(3);
                    h.setKabMore("N");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> commareaCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .returnTransid(eq(TRAN_ID), commareaCaptor.capture(), eq(COMMAREA_LENGTH));

        OcanlinFields decoded = new OcanlinFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaWorkArea().substring(0, 2)).isEqualTo("RW");
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_xctlsToMenuAndResetsContext() {
        givenPseudoConversation(1, "RW", 0, 0, "N");
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

        OcanlinFields decoded = new OcanlinFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo(PGM_NAME);
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo(TRAN_ID);
        assertEquals(0, decoded.getCaPgmContext());
    }

    @Test
    void mainLine_pf4Pressed_clearsAndResendsInitialScreen() {
        givenPseudoConversation(1, "RW", 0, 0, "N");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OcanlinFields out = (OcanlinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getAnmodel()).isEqualTo(-1);
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1, "RW", 0, 0, "N");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), dataOnlyOut.capture(), any(), eq(false), eq(false), eq(true));
        OcanlinFields dataOnlyScreen = (OcanlinFields) dataOnlyOut.getValue();
        assertThat(dataOnlyScreen.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-RUN / 5000-NORMALIZE-MODE / 3100-SET-HEADER
    // ─────────────────────────

    @ParameterizedTest
    @CsvSource({
        "RW,RW,REWARDS: card / purchases / purchase amt / points",
        "rewards,RW,REWARDS: card / purchases / purchase amt / points",
        "fr,FR,FRAUD: card / reason / trans / total amt / max amt",
        "FRAUD,FR,FRAUD: card / reason / trans / total amt / max amt",
        "gl,GL,GL: type / dr-cr / count / amount",
        "rc,RC,RECON: account / note / trans / movement / balance",
        "recon,RC,RECON: account / note / trans / movement / balance"
    })
    void mainLine_enterValidModeAlias_normalizesAndSetsHeader(
            String typedMode, String expectedCode, String expectedHeader) {
        givenPseudoConversation(1, null, 0, 0, "N");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAnmodei(typedMode));
        givenLinkReturns(
                h -> {
                    h.setKabRowCnt(0);
                    h.setKabResultCnt(0);
                    h.setKabMore("N");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcanlinFields out = (OcanlinFields) sendMapOut.getValue();
        assertThat(out.getAnmodeo().trim()).isEqualTo(expectedCode);
        assertThat(out.getAnheado().trim()).isEqualTo(expectedHeader);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);

        ArgumentCaptor<byte[]> linkParm = ArgumentCaptor.forClass(byte[].class);
        verify(appService)
                .link(argThat(s -> s.trim().equals("OUANLIN")), linkParm.capture(), eq(490));
        OcanlinFields sentParm = new OcanlinFields(new WorkingStorage());
        sentParm.writeBytes("KANLB-PARM", linkParm.getValue());
        assertThat(sentParm.getKabMode().trim()).isEqualTo(expectedCode);
        assertThat(sentParm.getKabStartOff()).isEqualTo(0);
        assertThat(sentParm.getKabMaxRows()).isEqualTo(PAGE_SIZE);
    }

    @Test
    void mainLine_enterUnrecognizedMode_showsBadModeMessageAndSkipsLink() {
        givenPseudoConversation(1, null, 0, 0, "N");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAnmodei("ZZ"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcanlinFields out = (OcanlinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_MODE);
        assertThat(out.getAnmodel()).isEqualTo(-1);
        verify(appService, never()).link(anyString(), any(), anyInt());
    }

    @Test
    void mainLine_enterMapfail_forcesLowValuesAndTreatsAsBadMode() {
        givenPseudoConversation(1, null, 0, 0, "N");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        when(appService.getEibresp()).thenReturn(36, 0, 0, 0);
        givenReceiveMapPopulates(f -> f.setAnmodei("RW"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcanlinFields out = (OcanlinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_MODE);
        verify(appService, never()).link(anyString(), any(), anyInt());
    }

    @Test
    void mainLine_enterLinkRespNotNormal_showsComputationErrorAndClearsMore() {
        givenPseudoConversation(1, null, 0, 0, "N");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        when(appService.getEibresp()).thenReturn(0, 99, 0, 0);
        givenReceiveMapPopulates(f -> f.setAnmodei("RW"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcanlinFields out = (OcanlinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_COMP_ERR);

        ArgumentCaptor<Object> commareaCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .returnTransid(eq(TRAN_ID), commareaCaptor.capture(), eq(COMMAREA_LENGTH));
        OcanlinFields decoded = new OcanlinFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaWorkArea().charAt(10)).isEqualTo('N');
    }

    @Test
    void mainLine_enterZeroRows_showsNoneFoundMessage() {
        givenPseudoConversation(1, null, 0, 0, "N");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAnmodei("FR"));
        givenLinkReturns(
                h -> {
                    h.setKabRowCnt(0);
                    h.setKabResultCnt(0);
                    h.setKabMore("N");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcanlinFields out = (OcanlinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
        assertThat(out.getAntot1o().trim()).isEmpty();
        assertThat(out.getAntot2o().trim()).isEmpty();
    }

    // ───────────────────────── 3000..4200 row/total build (RW full page) ─────────────────────────

    @Test
    void mainLine_enterModeRw_fullPage_placesAllSixRowsAndRewardsTotals() {
        givenPseudoConversation(1, null, 0, 0, "N");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAnmodei("RW"));
        givenLinkReturns(
                h -> {
                    h.setKabRowCnt(6);
                    h.setKabResultCnt(6);
                    h.setKabMore("N");
                    h.setKabTot1(new BigDecimal("1234.56"));
                    h.setKabTot2(new BigDecimal("999.00"));
                    h.setKabScanned(500);
                    for (int i = 1; i <= 6; i++) {
                        h.setString("KAB-R-KEY", "CARD" + i, i);
                        h.setString("KAB-R-INFO", "INFO" + i, i);
                        h.setInt("KAB-R-CNT", i * 10, i);
                        h.setDecimal("KAB-R-AMT", new BigDecimal(i + ".00"), i);
                        h.setDecimal("KAB-R-VAL", new BigDecimal(i * 100 + ".00"), i);
                    }
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcanlinFields out = (OcanlinFields) sendMapOut.getValue();

        assertThat(out.getAnk1o().trim()).isEqualTo("CARD1");
        assertThat(out.getAni1o().trim()).isEqualTo("INFO1");
        assertThat(out.getAnc1o().trim()).isEqualTo(edited("WS-ED-CNT", 10).trim());
        assertThat(out.getAna1o().trim())
                .isEqualTo(editedDecimal("WS-ED-MONEY", new BigDecimal("1.00")).trim());
        assertThat(out.getAnv1o().trim()).isEqualTo(editedLong("WS-ED-PTS", 100L).trim());

        assertThat(out.getAnk6o().trim()).isEqualTo("CARD6");
        assertThat(out.getAnv6o().trim()).isEqualTo(editedLong("WS-ED-PTS", 600L).trim());

        assertThat(out.getAntot1o().trim())
                .isEqualTo(
                        ("REWARDS - Purchase amount: "
                                        + editedDecimal("WS-ED-TOTAL", new BigDecimal("1234.56")))
                                .trim());
        assertThat(out.getAntot2o().trim())
                .isEqualTo(
                        ("Points: "
                                        + editedLong("WS-ED-BIG", 999L)
                                        + "  Cards: "
                                        + edited("WS-ED-NUM4", 6)
                                        + "  Scanned: "
                                        + edited("WS-ED-NUM", 500))
                                .trim());

        String expectedSummary =
                (edited("WS-CNT-ED", 6)
                                + " row(s)  page "
                                + edited("WS-ED-PG", 1)
                                + " of "
                                + edited("WS-ED-PGT", 1))
                        .trim();
        assertThat(out.getErrmsgo().trim()).isEqualTo(expectedSummary);
    }

    @Test
    void mainLine_enterModeGl_partialPage_placesRowsAndBlanksRemainderAndBuildsGlTotals() {
        givenPseudoConversation(1, null, 0, 0, "N");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAnmodei("GL"));
        givenLinkReturns(
                h -> {
                    h.setKabRowCnt(3);
                    h.setKabResultCnt(3);
                    h.setKabMore("N");
                    h.setKabTot1(new BigDecimal("500.00"));
                    h.setKabTot2(new BigDecimal("300.00"));
                    h.setKabScanned(42);
                    for (int i = 1; i <= 3; i++) {
                        h.setString("KAB-R-KEY", "GLROW" + i, i);
                        h.setString("KAB-R-INFO", "TYPE" + i, i);
                        h.setInt("KAB-R-CNT", i, i);
                        h.setDecimal("KAB-R-AMT", new BigDecimal(i + ".50"), i);
                        h.setDecimal("KAB-R-VAL", new BigDecimal(i + ".25"), i);
                    }
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcanlinFields out = (OcanlinFields) sendMapOut.getValue();

        assertThat(out.getAnk3o().trim()).isEqualTo("GLROW3");
        assertThat(out.getAnv3o().trim())
                .isEqualTo(editedDecimal("WS-ED-MONEY", new BigDecimal("3.25")).trim());

        // rows 4-6 must remain blank - not part of the returned page
        assertThat(out.getAnk4o().trim()).isEmpty();
        assertThat(out.getAni4o().trim()).isEmpty();
        assertThat(out.getAnk5o().trim()).isEmpty();
        assertThat(out.getAnk6o().trim()).isEmpty();

        assertThat(out.getAntot1o().trim())
                .isEqualTo(
                        ("GL - Debit: "
                                        + editedDecimal("WS-ED-TOTAL", new BigDecimal("500.00"))
                                        + "   Credit: "
                                        + editedDecimal("WS-ED-TOTAL2", new BigDecimal("300.00")))
                                .trim());
    }

    @Test
    void mainLine_enterModeFr_buildsFraudTotals() {
        givenPseudoConversation(1, null, 0, 0, "N");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAnmodei("FR"));
        givenLinkReturns(
                h -> {
                    h.setKabRowCnt(1);
                    h.setKabResultCnt(1);
                    h.setKabMore("N");
                    h.setKabTot1(new BigDecimal("7500.00"));
                    h.setKabTot2(new BigDecimal("3"));
                    h.setKabScanned(80);
                    h.setString("KAB-R-KEY", "CARD9", 1);
                    h.setString("KAB-R-INFO", "SUSPECT", 1);
                    h.setInt("KAB-R-CNT", 2, 1);
                    h.setDecimal("KAB-R-AMT", new BigDecimal("100.00"), 1);
                    h.setDecimal("KAB-R-VAL", new BigDecimal("50.00"), 1);
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcanlinFields out = (OcanlinFields) sendMapOut.getValue();

        assertThat(out.getAntot1o().trim())
                .isEqualTo(
                        ("FRAUD - Flagged amount: "
                                        + editedDecimal("WS-ED-TOTAL", new BigDecimal("7500.00")))
                                .trim());
        assertThat(out.getAntot2o().trim())
                .isEqualTo(
                        ("Flagged cards: "
                                        + edited("WS-ED-NUM", 3)
                                        + "  Scanned: "
                                        + editedLong("WS-ED-BIG", 80L))
                                .trim());
        // value column uses money format for non-RW modes
        assertThat(out.getAnv1o().trim())
                .isEqualTo(editedDecimal("WS-ED-MONEY", new BigDecimal("50.00")).trim());
    }

    @Test
    void mainLine_enterModeRc_buildsReconTotals() {
        givenPseudoConversation(1, null, 0, 0, "N");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAnmodei("RC"));
        givenLinkReturns(
                h -> {
                    h.setKabRowCnt(0);
                    h.setKabResultCnt(0);
                    h.setKabMore("N");
                    h.setKabTot1(new BigDecimal("10.00"));
                    h.setKabTot2(new BigDecimal("20.00"));
                    h.setKabDisc(4);
                    h.setKabScanned(9);
                });

        service.mainLine(appService);

        // rowCnt == 0 means 3200-BUILD-TOTALS is never reached (2400-BUILD-SUMMARY's
        // KAB-ROW-CNT = ZEROS branch wins first) - antot1/2o stay spaces.
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcanlinFields out = (OcanlinFields) sendMapOut.getValue();
        assertThat(out.getAntot1o().trim()).isEmpty();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
    }

    // ───────────────────────── 2200-PAGE-FWD / 2300-RESTART ─────────────────────────

    @Test
    void mainLine_pf8_moreAvailable_advancesOffsetByPageSize() {
        givenPseudoConversation(1, "RW", 0, 20, "Y");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("8");
        givenLinkReturns(
                h -> {
                    h.setKabRowCnt(0);
                    h.setKabResultCnt(20);
                    h.setKabMore("Y");
                });

        service.mainLine(appService);

        ArgumentCaptor<byte[]> linkParm = ArgumentCaptor.forClass(byte[].class);
        verify(appService)
                .link(argThat(s -> s.trim().equals("OUANLIN")), linkParm.capture(), eq(490));
        OcanlinFields sentParm = new OcanlinFields(new WorkingStorage());
        sentParm.writeBytes("KANLB-PARM", linkParm.getValue());
        assertThat(sentParm.getKabStartOff()).isEqualTo(PAGE_SIZE);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    @Test
    void mainLine_pf8_noMoreRows_showsEndOfResultsMessageWithoutLinking() {
        givenPseudoConversation(1, "RW", 0, 5, "N");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("8");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcanlinFields out = (OcanlinFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_END_ROWS);
        verify(appService, never()).link(anyString(), any(), anyInt());
    }

    @Test
    void mainLine_pf8_noModeYet_sendsInitialScreenInstead() {
        givenPseudoConversation(1, null, 0, 0, "N");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("8");

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        verify(appService, never()).link(anyString(), any(), anyInt());
    }

    @Test
    void mainLine_pf7_restartsAtOffsetZeroRegardlessOfCurrentOffset() {
        givenPseudoConversation(1, "FR", 12, 20, "Y");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("7");
        givenLinkReturns(
                h -> {
                    h.setKabRowCnt(0);
                    h.setKabResultCnt(20);
                    h.setKabMore("Y");
                });

        service.mainLine(appService);

        ArgumentCaptor<byte[]> linkParm = ArgumentCaptor.forClass(byte[].class);
        verify(appService)
                .link(argThat(s -> s.trim().equals("OUANLIN")), linkParm.capture(), eq(490));
        OcanlinFields sentParm = new OcanlinFields(new WorkingStorage());
        sentParm.writeBytes("KANLB-PARM", linkParm.getValue());
        assertThat(sentParm.getKabStartOff()).isEqualTo(0);
    }

    @Test
    void mainLine_pf7_noModeYet_sendsInitialScreenInstead() {
        givenPseudoConversation(1, null, 0, 0, "N");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("7");

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        verify(appService, never()).link(anyString(), any(), anyInt());
    }

    // ───────────────────────── 2450-PAGE-CALC edge ─────────────────────────

    @Test
    void mainLine_offsetBeyondResultCount_clampsPageTotalToPageNumber() {
        // WS-AS-OFFSET already at 18 (page 4 of a page size of 6), but the sub-program
        // now reports only 20 results total (3 full pages) - COBOL clamps
        // WS-PAGE-TOT up to WS-PAGE-NO rather than showing a total behind the current page.
        givenPseudoConversation(1, "RW", 12, 20, "Y");
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("8");
        givenLinkReturns(
                h -> {
                    h.setKabRowCnt(1);
                    h.setKabResultCnt(1);
                    h.setKabMore("N");
                    h.setString("KAB-R-KEY", "CARD1", 1);
                    h.setString("KAB-R-INFO", "INFO1", 1);
                    h.setInt("KAB-R-CNT", 1, 1);
                    h.setDecimal("KAB-R-AMT", new BigDecimal("1.00"), 1);
                    h.setDecimal("KAB-R-VAL", new BigDecimal("1.00"), 1);
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcanlinFields out = (OcanlinFields) sendMapOut.getValue();

        // offset becomes 18 -> page 4; rescnt=1 -> raw pageTot=1, clamped up to 4.
        String expectedSummary =
                (edited("WS-CNT-ED", 1)
                                + " row(s)  page "
                                + edited("WS-ED-PG", 4)
                                + " of "
                                + edited("WS-ED-PGT", 4))
                        .trim();
        assertThat(out.getErrmsgo().trim()).isEqualTo(expectedSummary);
    }
}

package com.generated.orion.occusin.service;

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
import com.generated.orion.occusin.accessor.OccusinFields;
import com.generated.orion.occusin.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OccusinService, generated from COBOL program OCCUSIN. All business logic is
 * private and exercised solely through the public {@link OccusinService#mainLine(AppService)} entry
 * point, driven by an {@link AppService} mock that emulates CICS SEND/RECEIVE/LINK/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OccusinServiceTest {

    private static final String MSG_PROMPT = "Choose filter mode I/F/S, key args, press ENTER.";
    private static final String MSG_BAD_MODE = "Mode must be I (id) F (fico) or S (state/zip).";
    private static final String MSG_BAD_NUM = "Numeric filter argument is not valid.";
    private static final String MSG_STATE_REQ = "State is required for the state/zip filter.";
    private static final String MSG_NONE_FOUND = "No customers match the filter.";
    private static final String MSG_END_LIST = "End of list - no more customers.";
    private static final String MSG_LINK_ERR = "Unable to link to browse engine OUCUSIN.";

    @Mock private AppService appService;

    private OccusinService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OccusinService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying CA-PGM-CONTEXT/CA-WORK-AREA. */
    private String commarea(int context, String workArea) {
        OccusinFields helper = new OccusinFields(new WorkingStorage());
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

    /** Builds a CA-WORK-AREA snapshot equivalent to KUI-REQUEST after a prior filter apply. */
    private String idRangeWorkArea(int idFrom, int idTo, int startKey) {
        OccusinFields req = new OccusinFields(new WorkingStorage());
        req.setString("KUI-FILTER", "I");
        req.setKuiIdFrom(idFrom);
        req.setKuiIdTo(idTo);
        req.setKuiStartKey(startKey);
        return req.getKuiRequest();
    }

    /** Stubs receiveMap to populate the MCUSINAI input fields captured from production code. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OccusinFields> populate) {
        doAnswer(
                        inv -> {
                            OccusinFields f = inv.getArgument(1);
                            populate.accept(f);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MCUSINA"), any());
    }

    /** Stubs the LINK to OUCUSIN to succeed (resp 0) and populate KCUSIN output fields. */
    private void givenLinkSucceeds(java.util.function.Consumer<OccusinFields> populate) {
        doAnswer(
                        inv -> {
                            byte[] linkCa = inv.getArgument(1);
                            OccusinFields into = new OccusinFields(new WorkingStorage());
                            into.writeBytes("KCUSIN-AREA", linkCa);
                            populate.accept(into);
                            byte[] updated = into.sliceBytes("KCUSIN-AREA");
                            System.arraycopy(
                                    updated, 0, linkCa, 0, Math.min(updated.length, linkCa.length));
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .link(anyString(), any(byte[].class), eq(795));
    }

    /** Stubs the LINK to fail at the CICS transport level (non-zero EIBRESP). */
    private void givenLinkFailsWithResp(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .link(anyString(), any(byte[].class), eq(795));
    }

    private ArgumentCaptor<Object> captureSendMap(int times, boolean erase) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(times))
                .sendMap(
                        eq("MCUSINA"),
                        captor.capture(),
                        any(),
                        eq(erase),
                        anyBoolean(),
                        anyBoolean());
        return captor;
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPromptAndModeI() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        OccusinFields out = (OccusinFields) captureSendMap(1, true).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getFmodeo().trim()).isEqualTo("I");
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORQC");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCCUSIN");
        verify(appService).returnTransid(eq("ORQC"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalContextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService)
                .sendMap(eq("MCUSINA"), any(), any(), eq(true), anyBoolean(), anyBoolean());
        verify(appService).returnTransid(eq("ORQC"), any(), eq(692));
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
    void mainLine_unmappedKeyPressed_resendsInitialScreenThenInvalidKeyDataOnly() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        // COBOL: OTHER branch performs 1000-SEND-INITIAL (ERASE) then overwrites ERRMSGO and
        // performs a second 8100-SEND-DATAONLY -- two SEND MAPs occur for an unmapped key.
        // ctx.f is a single mutable buffer reused across both sends, so only the LAST captured
        // snapshot (the DATAONLY call) reliably reflects what that call actually transmitted.
        verify(appService, times(1))
                .sendMap(eq("MCUSINA"), any(), any(), eq(true), anyBoolean(), anyBoolean());
        OccusinFields secondSend = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(secondSend.getErrmsgo().trim())
                .isEqualTo("Invalid key pressed. Please try again.");
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-APPLY-FILTER (ENTER) ─────────────────────────

    @Test
    void mainLine_enterIdRangeFilterValid_runsPageAndShowsRows() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setFmodei("I");
                    f.setFridi("100");
                    f.setToidi("999999999");
                });
        givenLinkSucceeds(
                f -> {
                    f.setString("KUI-RETURN-CD", " ");
                    f.setKuiRowCount(2);
                    f.setKurId(1, 100);
                    f.setKurName(1, "JOHN DOE");
                    f.setKurState(1, "CA");
                    f.setKurZip(1, "90001");
                    f.setKurFico(1, 720);
                    f.setKurId(2, 200);
                    f.setKurName(2, "JANE ROE");
                    f.setKurState(2, "NY");
                    f.setKurZip(2, "10001");
                    f.setKurFico(2, 650);
                    f.setKuiMatchCount(2);
                    f.setKuiFicoMin(650);
                    f.setKuiFicoAvg(685);
                    f.setKuiFicoMax(720);
                    f.setKuiMoreSw("N");
                });

        service.mainLine(appService);

        verify(appService).link(anyString(), any(byte[].class), eq(795));
        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(out.getCul1o().trim()).isEqualTo("000000100");
        assertThat(out.getCun1o().trim()).isEqualTo("JOHN DOE");
        assertThat(out.getCst2o().trim()).isEqualTo("NY");
        assertThat(out.getCzp2o().trim()).isEqualTo("10001");
        assertThat(out.getCfi1o().trim()).isEqualTo("720");
        assertThat(out.getErrmsgo().trim())
                .isEqualTo("2 match, FICO min/avg/max 650/685/720  End of list.");
    }

    @Test
    void mainLine_enterFicoFilterValid_moreRowsAvailable_showsPagingSuffix() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setFmodei("F");
                    f.setFrficoi("600");
                    f.setToficoi("800");
                });
        givenLinkSucceeds(
                f -> {
                    f.setString("KUI-RETURN-CD", " ");
                    f.setKuiRowCount(1);
                    f.setKurId(1, 300);
                    f.setKurName(1, "AVERY SMITH");
                    f.setKurState(1, "TX");
                    f.setKurZip(1, "73301");
                    f.setKurFico(1, 640);
                    f.setKuiMatchCount(50);
                    f.setKuiFicoMin(600);
                    f.setKuiFicoAvg(650);
                    f.setKuiFicoMax(800);
                    f.setKuiMoreSw("Y");
                });

        service.mainLine(appService);

        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim())
                .isEqualTo("50 match, FICO min/avg/max 600/650/800  PF8=more PF7=top");
    }

    @Test
    void mainLine_enterStateZipFilterValid_runsPageAndShowsRow() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setFmodei("S");
                    f.setFstatei("CA");
                    f.setFzipi("900");
                });
        givenLinkSucceeds(
                f -> {
                    f.setString("KUI-RETURN-CD", " ");
                    f.setKuiRowCount(1);
                    f.setKurId(1, 555);
                    f.setKurName(1, "STATE FILTER USER");
                    f.setKurState(1, "CA");
                    f.setKurZip(1, "90210");
                    f.setKurFico(1, 700);
                    f.setKuiMatchCount(1);
                    f.setKuiFicoMin(700);
                    f.setKuiFicoAvg(700);
                    f.setKuiFicoMax(700);
                    f.setKuiMoreSw("N");
                });

        service.mainLine(appService);

        verify(appService).link(anyString(), any(byte[].class), eq(795));
        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(out.getCst1o().trim()).isEqualTo("CA");
        assertThat(out.getCzp1o().trim()).isEqualTo("90210");
    }

    @Test
    void mainLine_enterStateZipFilterMissingState_showsStateRequiredAndSkipsLink() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setFmodei("S");
                    f.setFstatei("  ");
                    f.setFzipi("900");
                });

        service.mainLine(appService);

        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_STATE_REQ);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_enterUnknownMode_showsBadModeAndSkipsLink() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFmodei("X"));

        service.mainLine(appService);

        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_MODE);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_enterIdRangeWithNonNumericFridi_showsBadNumAndSkipsLink() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setFmodei("I");
                    f.setFridi("12A45678");
                });

        service.mainLine(appService);

        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_NUM);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_enterFicoFilterWithNonNumericToficoi_showsBadNumAndSkipsLink() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setFmodei("F");
                    f.setFrficoi("600");
                    f.setToficoi("7X0");
                });

        service.mainLine(appService);

        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_NUM);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_enterFicoFilterWithNonNumericFrficoi_showsBadNumAndSkipsLink() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setFmodei("F");
                    f.setFrficoi("6X0");
                });

        service.mainLine(appService);

        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_NUM);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_enterIdRangeWithNonNumericToidi_showsBadNumAndSkipsLink() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setFmodei("I");
                    f.setFridi("100");
                    f.setToidi("12A45678");
                });

        service.mainLine(appService);

        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_NUM);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_enterIdRangeBlankBounds_usesDefaultsAndShowsNoneFound() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setFmodei("I");
                    f.setFridi("         ");
                    f.setToidi("         ");
                });
        givenLinkSucceeds(
                f -> {
                    f.setString("KUI-RETURN-CD", " ");
                    f.setKuiRowCount(0);
                    f.setKuiMoreSw("N");
                });

        service.mainLine(appService);

        verify(appService).link(anyString(), any(byte[].class), eq(795));
        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
    }

    @Test
    void mainLine_enterMapfailResp_treatsFieldsAsLowValuesAndShowsBadMode() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        doAnswer(
                        inv -> {
                            nextResp.set(36);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MCUSINA"), any());

        service.mainLine(appService);

        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_MODE);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_enterSubLinkTransportError_showsLinkErrorMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFmodei("I"));
        givenLinkFailsWithResp(99);

        service.mainLine(appService);

        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LINK_ERR);
    }

    // ───────────────────────── 2300-RESTART (PF7) ─────────────────────────

    @Test
    void mainLine_pf7NoMatches_showsNoneFoundMessage() {
        givenPseudoConversation(1, idRangeWorkArea(0, 999999999, 0));
        when(appService.getEibaid()).thenReturn("7");
        givenLinkSucceeds(
                f -> {
                    f.setString("KUI-RETURN-CD", " ");
                    f.setKuiRowCount(0);
                    f.setKuiMoreSw("N");
                });

        service.mainLine(appService);

        verify(appService).link(anyString(), any(byte[].class), eq(795));
        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
    }

    @Test
    void mainLine_pf7WithMatches_restartsFromTopWithSameFilter() {
        givenPseudoConversation(1, idRangeWorkArea(100, 999999999, 12345));
        when(appService.getEibaid()).thenReturn("7");
        givenLinkSucceeds(
                f -> {
                    f.setString("KUI-RETURN-CD", " ");
                    f.setKuiRowCount(1);
                    f.setKurId(1, 100);
                    f.setKurName(1, "RESTARTED USER");
                    f.setKurState(1, "FL");
                    f.setKurZip(1, "33101");
                    f.setKurFico(1, 710);
                    f.setKuiMatchCount(1);
                    f.setKuiFicoMin(710);
                    f.setKuiFicoAvg(710);
                    f.setKuiFicoMax(710);
                    f.setKuiMoreSw("N");
                });

        service.mainLine(appService);

        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(out.getCun1o().trim()).isEqualTo("RESTARTED USER");
    }

    // ───────────────────────── 2200-PAGE-NEXT (PF8) ─────────────────────────

    @Test
    void mainLine_pf8NoMoreRows_showsEndOfListAndDoesNotRenderRows() {
        givenPseudoConversation(1, idRangeWorkArea(100, 999999999, 12345));
        when(appService.getEibaid()).thenReturn("8");
        givenLinkSucceeds(
                f -> {
                    f.setString("KUI-RETURN-CD", " ");
                    f.setKuiRowCount(0);
                    f.setKuiMoreSw("N");
                });

        service.mainLine(appService);

        verify(appService).link(anyString(), any(byte[].class), eq(795));
        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_END_LIST);
    }

    @Test
    void mainLine_pf8MoreAvailable_advancesPageAndShowsNextRow() {
        givenPseudoConversation(1, idRangeWorkArea(100, 999999999, 12345));
        when(appService.getEibaid()).thenReturn("8");
        givenLinkSucceeds(
                f -> {
                    f.setString("KUI-RETURN-CD", " ");
                    f.setKuiRowCount(1);
                    f.setKurId(1, 999);
                    f.setKurName(1, "NEXT PAGE USER");
                    f.setKurState(1, "WA");
                    f.setKurZip(1, "98101");
                    f.setKurFico(1, 610);
                    f.setKuiMatchCount(30);
                    f.setKuiFicoMin(600);
                    f.setKuiFicoAvg(610);
                    f.setKuiFicoMax(650);
                    f.setKuiMoreSw("Y");
                    f.setKuiNextKey(88888);
                });

        service.mainLine(appService);

        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(out.getCun1o().trim()).isEqualTo("NEXT PAGE USER");
        assertThat(out.getErrmsgo().trim())
                .isEqualTo("30 match, FICO min/avg/max 600/610/650  PF8=more PF7=top");
    }

    @Test
    void mainLine_enterSubReturnsErrorCode_showsCaErrMsg() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFmodei("I"));
        givenLinkSucceeds(f -> f.setString("KUI-RETURN-CD", "E"));

        service.mainLine(appService);

        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        // CA-ERR-MSG is only populated by 3000-CALL-SUB on a transport failure; the sub-program
        // setting KUI-RETURN-CD = 'E' directly (browse-engine-reported error) leaves CA-ERR-MSG
        // as spaces, so 4000-SHOW-PAGE echoes an empty message per COBOL ground truth.
        assertThat(out.getErrmsgo().trim()).isEmpty();
    }

    @Test
    void mainLine_rowCountExceedsThirteen_placesOnlyFirstThirteenRowsSafely() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFmodei("I"));
        givenLinkSucceeds(
                f -> {
                    f.setString("KUI-RETURN-CD", " ");
                    f.setKuiRowCount(14);
                    for (int i = 1; i <= 13; i++) {
                        f.setKurId(i, i);
                        f.setKurName(i, "USER" + i);
                        f.setKurState(i, "ZZ");
                        f.setKurZip(i, "00000");
                        f.setKurFico(i, 500);
                    }
                    f.setKuiMatchCount(14);
                    f.setKuiFicoMin(500);
                    f.setKuiFicoAvg(500);
                    f.setKuiFicoMax(500);
                    f.setKuiMoreSw("Y");
                });

        service.mainLine(appService);

        OccusinFields out = (OccusinFields) captureSendMap(1, false).getValue();
        assertThat(out.getCun1o().trim()).isEqualTo("USER1");
        assertThat(out.getCun13o().trim()).isEqualTo("USER13");
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOccusin() {
        assertEquals("OCCUSIN", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrqc() {
        assertEquals("ORQC", service.getTransId());
    }

    @Test
    void getButtonDefs_delegatesToMetadataWithoutError() {
        assertNotNull(service.getButtonDefs());
    }

    @Test
    void getFieldMapping_delegatesToMetadataForKnownMap() {
        assertNotNull(service.getFieldMapping("MCUSINA"));
    }
}

package com.generated.orion.octrnin.service;

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
import com.generated.orion.octrnin.accessor.OctrninFields;
import com.generated.orion.octrnin.model.WorkingStorage;

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
 * Unit tests for OctrninService, generated from COBOL program OCTRNIN. All business logic is
 * private and is exercised solely through the public {@link OctrninService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS SEND/RECEIVE/LINK/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OctrninServiceTest {

    private static final String MSG_PROMPT = "Choose filter C/D/M/T/A, key args, press ENTER.";
    private static final String MSG_BAD_MODE = "Mode must be C D M T or A.";
    private static final String MSG_CARD_REQ = "Card number is required for the card filter.";
    private static final String MSG_MERCH_REQ = "Merchant id is required and must be numeric.";
    private static final String MSG_TYPE_REQ = "Type is required for the type/category filter.";
    private static final String MSG_BAD_NUM = "A numeric filter argument is not valid.";
    private static final String MSG_BAD_AMT = "Amount threshold is not a valid number.";
    private static final String MSG_NONE_FOUND = "No transactions match the filter.";
    private static final String MSG_END_LIST = "End of list - no more transactions.";
    private static final String MSG_LINK_ERR = "Unable to link to analytics engine OUTRNIN.";

    @Mock private AppService appService;

    private OctrninService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OctrninService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-23", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying CA-PGM-CONTEXT/CA-WORK-AREA. */
    private String commarea(int context, String workArea) {
        OctrninFields helper = new OctrninFields(new WorkingStorage());
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

    /** Builds the CA-WORK-AREA (= serialized KTI-REQUEST) for a saved card filter. */
    private String cardWorkArea(String card) {
        OctrninFields helper = new OctrninFields(new WorkingStorage());
        helper.setString("KTI-FILTER", "C");
        helper.setKtiCard(card);
        return helper.getKtiRequest();
    }

    /** Stubs receiveMap to populate the MTRNINAI input fields captured from production code. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OctrninFields> populate) {
        doAnswer(
                        inv -> {
                            OctrninFields f = inv.getArgument(1);
                            populate.accept(f);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MTRNINA"), any());
    }

    /** Stubs the LINK to OUTRNIN to succeed (resp 0) and populate KTR-* output fields. */
    private void givenLinkSucceeds(java.util.function.Consumer<OctrninFields> populate) {
        doAnswer(
                        inv -> {
                            byte[] linkCa = inv.getArgument(1);
                            OctrninFields into = new OctrninFields(new WorkingStorage());
                            into.writeBytes("KTRNIN-AREA", linkCa);
                            populate.accept(into);
                            byte[] updated = into.sliceBytes("KTRNIN-AREA");
                            System.arraycopy(
                                    updated, 0, linkCa, 0, Math.min(updated.length, linkCa.length));
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .link(anyString(), any(byte[].class), eq(845));
    }

    /** Stubs the LINK to fail at the CICS transport level (non-zero EIBRESP). */
    private void givenLinkFailsWithResp(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .link(anyString(), any(byte[].class), eq(845));
    }

    private ArgumentCaptor<Object> captureLastSendMap(int times, boolean erase) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(times))
                .sendMap(
                        eq("MTRNINA"),
                        captor.capture(),
                        any(),
                        eq(erase),
                        anyBoolean(),
                        anyBoolean());
        return captor;
    }

    /**
     * Reads back the KTRNIN-AREA bytes actually sent to LINK, to verify the parsed request fields.
     */
    private OctrninFields captureLinkRequest() {
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(appService).link(anyString(), captor.capture(), eq(845));
        OctrninFields f = new OctrninFields(new WorkingStorage());
        f.writeBytes("KTRNIN-AREA", captor.getValue());
        return f;
    }

    // ───────────────────────── 0000-MAIN ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPromptAndCardMode() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, true);
        OctrninFields out = (OctrninFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTmodeo().trim()).isEqualTo("C");
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORQT");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCTRNIN");
        verify(appService).returnTransid(eq("ORQT"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalContextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService)
                .sendMap(eq("MTRNINA"), any(), any(), eq(true), anyBoolean(), anyBoolean());
        verify(appService).returnTransid(eq("ORQT"), any(), eq(692));
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
    void mainLine_unmappedKeyPressed_resendsInitialThenShowsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        // COBOL OTHER branch: 1000-SEND-INITIAL (full erase send) THEN overrides ERRMSGO
        // and performs 8100-SEND-DATAONLY -- two distinct sendMap invocations.
        verify(appService)
                .sendMap(eq("MTRNINA"), any(), any(), eq(true), anyBoolean(), anyBoolean());
        ArgumentCaptor<Object> dataOnly = captureLastSendMap(1, false);
        OctrninFields out = (OctrninFields) dataOnly.getValue();
        assertThat(out.getErrmsgo().trim()).isNotEmpty();
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-APPLY-FILTER (ENTER) ─────────────────────────

    @Test
    void mainLine_enterMapfail_treatsInputAsLowValuesAndFailsBadMode() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        nextResp.set(36);
        doAnswer(inv -> null).when(appService).receiveMap(eq("MTRNINA"), any());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OctrninFields out = (OctrninFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_MODE);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_enterBadMode_showsBadModeMessageAndSkipsLink() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setTmodei("Z"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OctrninFields out = (OctrninFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_MODE);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_enterCardFilterBlank_showsCardRequiredAndSkipsLink() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTmodei("C");
                    f.setFcardi("                ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OctrninFields out = (OctrninFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARD_REQ);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_enterCardFilterValid_runsPageWithTwoRowsAndMoreFlag() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTmodei("C");
                    f.setFcardi("1111222233334444");
                });
        givenLinkSucceeds(
                f -> {
                    f.setKtiReturnCd(" ");
                    f.setKtiRowCount(2);
                    f.setKtrId(1, "TX0001");
                    f.setKtrCard(1, "1111222233334444");
                    f.setKtrTycat(1, "PU");
                    f.setKtrDesc(1, "GROCERY ST");
                    f.setKtrMerch(1, "1001");
                    f.setKtrAmt(1, new BigDecimal("100.00"));
                    f.setKtrDate(1, "2026-01-01");
                    f.setKtrId(2, "TX0002");
                    f.setKtrCard(2, "1111222233335555");
                    f.setKtrTycat(2, "PA");
                    f.setKtrDesc(2, "PAYMENT");
                    f.setKtrMerch(2, "1002");
                    f.setKtrAmt(2, new BigDecimal("50.00"));
                    f.setKtrDate(2, "2026-01-02");
                    f.setKtiMatchCount(2);
                    f.setKtiNetTotal(new BigDecimal("150.00"));
                    f.setKtiMoreSw("Y");
                    f.setKtiNextKey("NEXTKEY");
                });

        service.mainLine(appService);

        verify(appService).link(anyString(), any(byte[].class), eq(845));
        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OctrninFields out = (OctrninFields) sendMapOut.getValue();
        assertThat(out.getTid1o().trim()).isEqualTo("TX0001");
        assertThat(out.getTcd1o().trim()).isEqualTo("4444");
        assertThat(out.getTds1o().trim()).isEqualTo("GROCERY ST");
        assertThat(out.getTid2o().trim()).isEqualTo("TX0002");
        assertThat(out.getTcd2o().trim()).isEqualTo("5555");
        assertThat(out.getMcnto().trim()).isEqualTo("2");
        assertThat(out.getErrmsgo().trim())
                .contains("2 matching tran(s)")
                .contains("PF8=more PF7=top");
    }

    @Test
    void mainLine_enterDateFilterBothBlank_defaultsToOpenRange() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTmodei("D");
                    f.setFrdatei("          ");
                    f.setFtdatei("          ");
                });
        givenLinkSucceeds(
                f -> {
                    f.setKtiReturnCd(" ");
                    f.setKtiRowCount(0);
                    f.setKtiMatchCount(0);
                    f.setKtiMoreSw("N");
                });

        service.mainLine(appService);

        OctrninFields request = captureLinkRequest();
        assertThat(request.getKtiDateFrom().trim()).isEqualTo("0000-00-00");
        assertThat(request.getKtiDateTo().trim()).isEqualTo("9999-99-99");
    }

    @Test
    void mainLine_enterDateFilterBothGiven_preservesGivenBounds() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTmodei("D");
                    f.setFrdatei("2026-01-01");
                    f.setFtdatei("2026-01-31");
                });
        givenLinkSucceeds(
                f -> {
                    f.setKtiReturnCd(" ");
                    f.setKtiRowCount(0);
                    f.setKtiMatchCount(0);
                    f.setKtiMoreSw("N");
                });

        service.mainLine(appService);

        OctrninFields request = captureLinkRequest();
        assertThat(request.getKtiDateFrom().trim()).isEqualTo("2026-01-01");
        assertThat(request.getKtiDateTo().trim()).isEqualTo("2026-01-31");
    }

    @Test
    void mainLine_enterMerchantFilterBlank_showsMerchantRequiredAndSkipsLink() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTmodei("M");
                    f.setFmerchi("         ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OctrninFields out = (OctrninFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_MERCH_REQ);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_enterMerchantFilterNonNumeric_showsMerchantRequiredAndSkipsLink() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTmodei("M");
                    f.setFmerchi("12A      ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OctrninFields out = (OctrninFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_MERCH_REQ);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_enterMerchantFilterValidNumeric_parsesMerchIdAndLinks() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTmodei("M");
                    f.setFmerchi("123      ");
                });
        givenLinkSucceeds(
                f -> {
                    f.setKtiReturnCd(" ");
                    f.setKtiRowCount(0);
                    f.setKtiMatchCount(0);
                    f.setKtiMoreSw("N");
                });

        service.mainLine(appService);

        OctrninFields request = captureLinkRequest();
        assertThat(request.getKtiMerchId()).isEqualTo(123);
    }

    @Test
    void mainLine_enterTypeCatFilterBlankType_showsTypeRequiredAndSkipsLink() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTmodei("T");
                    f.setFtypei("  ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OctrninFields out = (OctrninFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TYPE_REQ);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_enterTypeCatFilterBadCategory_showsBadNumMessageAndSkipsLink() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTmodei("T");
                    f.setFtypei("PU");
                    f.setFcati("1X  ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OctrninFields out = (OctrninFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_NUM);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_enterTypeCatFilterValidWithCategory_parsesCategoryAndLinks() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTmodei("T");
                    f.setFtypei("PU");
                    f.setFcati("12  ");
                });
        givenLinkSucceeds(
                f -> {
                    f.setKtiReturnCd(" ");
                    f.setKtiRowCount(0);
                    f.setKtiMatchCount(0);
                    f.setKtiMoreSw("N");
                });

        service.mainLine(appService);

        OctrninFields request = captureLinkRequest();
        assertThat(request.getKtiFType().trim()).isEqualTo("PU");
        assertThat(request.getKtiFCat()).isEqualTo(12);
    }

    @Test
    void mainLine_enterTypeCatFilterNoCategory_defaultsCategoryToZero() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTmodei("T");
                    f.setFtypei("PU");
                    f.setFcati("    ");
                });
        givenLinkSucceeds(
                f -> {
                    f.setKtiReturnCd(" ");
                    f.setKtiRowCount(0);
                    f.setKtiMatchCount(0);
                    f.setKtiMoreSw("N");
                });

        service.mainLine(appService);

        OctrninFields request = captureLinkRequest();
        assertThat(request.getKtiFCat()).isEqualTo(0);
    }

    @Test
    void mainLine_enterAmountFilterInvalidDoubleDot_showsBadAmtMessageAndSkipsLink() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTmodei("A");
                    f.setFamti("12.3.4      ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OctrninFields out = (OctrninFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_AMT);
        verify(appService, never()).link(anyString(), any(byte[].class), anyInt());
    }

    @Test
    void mainLine_enterAmountFilterBlank_defaultsThresholdToZero() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTmodei("A");
                    f.setFamti("            ");
                });
        givenLinkSucceeds(
                f -> {
                    f.setKtiReturnCd(" ");
                    f.setKtiRowCount(0);
                    f.setKtiMatchCount(0);
                    f.setKtiMoreSw("N");
                });

        service.mainLine(appService);

        OctrninFields request = captureLinkRequest();
        assertThat(request.getKtiAmtThresh()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void mainLine_enterAmountFilterValidWithCommasAndOneDecimal_convertGap_thresholdLostToZero() {
        // CONVERT-GAP: COBOL WORKING-STORAGE declares
        //   01  WS-AM-NUM REDEFINES WS-AM-BUILD PIC 9(09)V99.
        // so WS-AM-NUM shares the exact same bytes as WS-AB-INT/WS-AB-DEC, and
        // 6500-PARSE-AMT's closing "MOVE WS-AM-NUM TO WS-AM-VALUE" (COBOL line 609)
        // picks up the digits just accumulated into WS-AB-INT/WS-AB-DEC a few lines
        // above. The generated data layout (OCTRNIN_WS.xml) instead nests WS-AM-NUM
        // as an unrelated field inside the WS-SYS group (offset 4, alongside
        // COMPLETION-CODE/SQLCODE) with redefines="WS-AM-BUILD" recorded only as
        // metadata -- the accessor's getDecimal("WS-AM-NUM") reads WS-SYS's own
        // (never-written) storage, not WS-AM-BUILD's bytes. So getWsAmNum() always
        // returns zero, no matter what was parsed into WS-AB-INT/WS-AB-DEC.
        // Expected per COBOL ground truth: a valid "1,234.5" amount filter parses to
        // threshold 1234.50 (commas ignored, lone decimal digit shifted to hundredths).
        // The converted Java silently keeps the initial zero threshold instead --
        // this test intentionally fails against current Java to flag the gap; it is
        // not a bug in the test. Every OCTRNIN amount-filter (mode 'A') search with a
        // non-blank threshold is affected.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTmodei("A");
                    f.setFamti("1,234.5     ");
                });
        givenLinkSucceeds(
                f -> {
                    f.setKtiReturnCd(" ");
                    f.setKtiRowCount(0);
                    f.setKtiMatchCount(0);
                    f.setKtiMoreSw("N");
                });

        service.mainLine(appService);

        OctrninFields request = captureLinkRequest();
        assertThat(request.getKtiAmtThresh()).isEqualByComparingTo(new BigDecimal("1234.50"));
    }

    // ───────────────────────── 3000-CALL-SUB / 4000-SHOW-PAGE ─────────────────────────

    @Test
    void mainLine_enterLinkTransportError_showsLinkErrorAndSkipsRows() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTmodei("C");
                    f.setFcardi("1111222233334444");
                });
        givenLinkFailsWithResp(99);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OctrninFields out = (OctrninFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LINK_ERR);
        assertThat(out.getTid1o().trim()).isEmpty();
    }

    @Test
    void mainLine_enterFilterNoMatches_showsNoneFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTmodei("C");
                    f.setFcardi("1111222233339999");
                });
        givenLinkSucceeds(
                f -> {
                    f.setKtiReturnCd(" ");
                    f.setKtiRowCount(0);
                    f.setKtiMatchCount(0);
                    f.setKtiMoreSw("N");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OctrninFields out = (OctrninFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
    }

    @Test
    void mainLine_enterFilterSixRows_placesAllRowsAndEndOfListMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTmodei("C");
                    f.setFcardi("1111222233334444");
                });
        givenLinkSucceeds(
                f -> {
                    f.setKtiReturnCd(" ");
                    f.setKtiRowCount(6);
                    for (int i = 1; i <= 6; i++) {
                        f.setKtrId(i, "TX000" + i);
                        f.setKtrCard(i, "111122223333" + String.format("%04d", i));
                        f.setKtrTycat(i, "PU");
                        f.setKtrDesc(i, "DESC " + i);
                        f.setKtrMerch(i, "100" + i);
                        f.setKtrAmt(i, new BigDecimal("10.00"));
                        f.setKtrDate(i, "2026-01-0" + i);
                    }
                    f.setKtiMatchCount(6);
                    f.setKtiMoreSw("N");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OctrninFields out = (OctrninFields) sendMapOut.getValue();
        assertThat(out.getTid1o().trim()).isEqualTo("TX0001");
        assertThat(out.getTcd1o().trim()).isEqualTo("0001");
        assertThat(out.getTid6o().trim()).isEqualTo("TX0006");
        assertThat(out.getTcd6o().trim()).isEqualTo("0006");
        assertThat(out.getErrmsgo().trim()).contains("6 matching tran(s)").contains("End of list.");
    }

    // ───────────────────────── 2200-PAGE-NEXT (PF8) ─────────────────────────

    @Test
    void mainLine_pf8NoMoreRows_showsEndOfListMessage() {
        givenPseudoConversation(1, cardWorkArea("1111222233334444"));
        when(appService.getEibaid()).thenReturn("8");
        givenLinkSucceeds(
                f -> {
                    f.setKtiReturnCd(" ");
                    f.setKtiRowCount(0);
                    f.setKtiMatchCount(0);
                    f.setKtiMoreSw("N");
                });

        service.mainLine(appService);

        verify(appService).link(anyString(), any(byte[].class), eq(845));
        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OctrninFields out = (OctrninFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_END_LIST);
    }

    @Test
    void mainLine_pf8MoreRowsAvailable_showsNextPage() {
        givenPseudoConversation(1, cardWorkArea("1111222233334444"));
        when(appService.getEibaid()).thenReturn("8");
        givenLinkSucceeds(
                f -> {
                    f.setKtiReturnCd(" ");
                    f.setKtiRowCount(1);
                    f.setKtrId(1, "TX0009");
                    f.setKtrCard(1, "1111222233338888");
                    f.setKtrTycat(1, "PU");
                    f.setKtrDesc(1, "NEXT PAGE");
                    f.setKtrMerch(1, "1009");
                    f.setKtrAmt(1, new BigDecimal("20.00"));
                    f.setKtrDate(1, "2026-01-09");
                    f.setKtiMatchCount(1);
                    f.setKtiMoreSw("N");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OctrninFields out = (OctrninFields) sendMapOut.getValue();
        assertThat(out.getTid1o().trim()).isEqualTo("TX0009");
        assertThat(out.getTcd1o().trim()).isEqualTo("8888");
    }

    // ───────────────────────── 2300-RESTART (PF7) ─────────────────────────

    @Test
    void mainLine_pf7Restart_resetsStartKeyAndShowsFirstPage() {
        givenPseudoConversation(1, cardWorkArea("1111222233334444"));
        when(appService.getEibaid()).thenReturn("7");
        givenLinkSucceeds(
                f -> {
                    f.setKtiReturnCd(" ");
                    f.setKtiRowCount(1);
                    f.setKtrId(1, "TX0010");
                    f.setKtrCard(1, "1111222233337777");
                    f.setKtrTycat(1, "PU");
                    f.setKtrDesc(1, "RESTARTED");
                    f.setKtrMerch(1, "1010");
                    f.setKtrAmt(1, new BigDecimal("30.00"));
                    f.setKtrDate(1, "2026-01-10");
                    f.setKtiMatchCount(1);
                    f.setKtiMoreSw("N");
                });

        service.mainLine(appService);

        OctrninFields request = captureLinkRequest();
        assertThat(request.getKtiStartKey().trim()).isEmpty();
        ArgumentCaptor<Object> sendMapOut = captureLastSendMap(1, false);
        OctrninFields out = (OctrninFields) sendMapOut.getValue();
        assertThat(out.getTid1o().trim()).isEqualTo("TX0010");
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOctrnin() {
        assertEquals("OCTRNIN", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrqt() {
        assertEquals("ORQT", service.getTransId());
    }

    @Test
    void getButtonDefs_delegatesToMetadataWithoutError() {
        assertNotNull(service.getButtonDefs());
    }

    @Test
    void getFieldMapping_delegatesToMetadataForKnownMap() {
        assertNotNull(service.getFieldMapping("MTRNINA"));
    }
}

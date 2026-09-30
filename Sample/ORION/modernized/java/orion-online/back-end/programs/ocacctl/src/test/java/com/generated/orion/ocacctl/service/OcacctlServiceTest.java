package com.generated.orion.ocacctl.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.generated.orion.ocacctl.accessor.OcacctlFields;
import com.generated.orion.ocacctl.model.WorkingStorage;

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
 * Unit tests for OcacctlService, generated from COBOL program OCACCTL. All business logic is
 * private and is exercised solely through the public {@link OcacctlService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS
 * SEND/RECEIVE/STARTBR/READNEXT/ENDBR/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcacctlServiceTest {

    private static final String ACCTFILE = "ACCTFILE";

    private static final String MSG_PROMPT = "Enter start account id (or blank) and ENTER.";
    private static final String MSG_BAD_START = "Start account id must be numeric.";
    private static final String MSG_NONE_FOUND = "No accounts found from that point.";
    private static final String MSG_END_FILE = "End of file - no more accounts.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";

    @Mock private AppService appService;

    private OcacctlService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OcacctlService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /**
     * Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT and CA-WORK-AREA.
     */
    private String commareaWithContext(int context, String workArea) {
        OcacctlFields helper = new OcacctlFields(new WorkingStorage());
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

    /** Stubs receiveMap to populate the MACCTLAI input fields captured from production code. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OcacctlFields> populate) {
        doAnswer(
                        inv -> {
                            OcacctlFields f = inv.getArgument(1);
                            populate.accept(f);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MACCTLA"), any());
    }

    /**
     * Stubs STARTBR on ACCTFILE to return the given EIBRESP (0=NORMAL, 13=NOTFND, 20=ENDFILE,
     * other=error).
     */
    private void givenStartBrowseResp(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .startBrowse(eq(ACCTFILE), anyString(), anyInt());
    }

    /**
     * Stubs READNEXT on ACCTFILE to hand back the given account ids in order (status "Y", balance
     * 100.00 + index), then signal ENDFILE (resp 20) once exhausted.
     */
    private void givenReadNextRows(long... ids) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            OcacctlFields into = inv.getArgument(1);
                            int i = idx.getAndIncrement();
                            if (i < ids.length) {
                                into.setAcId(ids[i]);
                                into.setAcActiveStatus("Y");
                                into.setAcCurrBal(
                                        new BigDecimal("100.00").add(BigDecimal.valueOf(i)));
                                nextResp.set(0);
                            } else {
                                nextResp.set(20);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(eq(ACCTFILE), any());
    }

    /** Stubs READNEXT to return one successful row then an unexpected file error (resp 99). */
    private void givenReadNextRowThenError(long id) {
        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            OcacctlFields into = inv.getArgument(1);
                            if (callCount.getAndIncrement() == 0) {
                                into.setAcId(id);
                                into.setAcActiveStatus("Y");
                                into.setAcCurrBal(new BigDecimal("50.00"));
                                nextResp.set(0);
                            } else {
                                nextResp.set(99);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(eq(ACCTFILE), any());
    }

    private ArgumentCaptor<Object> captureLastSendMap() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(2))
                .sendMap(
                        eq("MACCTLA"),
                        captor.capture(),
                        any(),
                        anyBoolean(),
                        anyBoolean(),
                        anyBoolean());
        return captor;
    }

    // ───────────────────────── 0000-MAIN ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq("MACCTLA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OcacctlFields out = (OcacctlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertEquals(-1, out.getFracctl());
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORLA");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCACCTL");
        verify(appService).returnTransid(eq("ORLA"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalContextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq("MACCTLA"), any(), any(), eq(true), eq(false), eq(true));
        verify(appService).returnTransid(eq("ORLA"), any(), eq(692));
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
                        eq(692));
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
                .sendMap(eq("MACCTLA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OcacctlFields out = (OcacctlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = captureLastSendMap();
        // first captured call (index 0) is the initial-screen resend triggered by the
        // unmapped key; index 1 is the follow-up DATAONLY send carrying the error message.
        OcacctlFields lastScreen = (OcacctlFields) sendMapOut.getAllValues().get(1);
        assertThat(lastScreen.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-LIST-FROM-START (ENTER) ─────────────────────────

    @Test
    void mainLine_enterBlankStart_browsesFromZero_fillsFullPageAndSavesNextKey() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFraccti("            "));
        givenStartBrowseResp(0);
        givenReadNextRows(1L, 2L, 3L, 4L, 5L);

        service.mainLine(appService);

        verify(appService).startBrowse(eq(ACCTFILE), anyString(), eq(0));

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcacctlFields out = (OcacctlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo("5 account(s) displayed.");
        assertThat(out.getAcl1o().trim()).isEqualTo("00000000001");
        assertThat(out.getAcs1o().trim()).isEqualTo("Y");
        assertThat(out.getAcb1o().trim()).isNotEmpty();
        assertThat(out.getAcl5o().trim()).isEqualTo("00000000005");
        assertEquals(-1, out.getFracctl());
        verify(appService).endBrowse(eq(ACCTFILE));
    }

    @Test
    void mainLine_enterNumericStart_convertGap_startBrowseKeyShouldBeZeroPadded() {
        // CONVERT-GAP: AC-ID is COBOL PIC 9(11); COBOL's 3100-START-BROWSE does
        // MOVE WS-PAGE-START TO AC-ID then EXEC CICS STARTBR RIDFLD(AC-ID), so the
        // browse key sent to the file is always the fixed-width 11-digit zero-padded
        // form, e.g. "00000000100". The converted startAccountBrowse() instead does
        // appService.startBrowse(file, String.valueOf(ctx.f.getAcId()), 0), which
        // for id 100 sends the unpadded key "100". Against a real fixed-width keyed
        // file this breaks GTEQ positioning. Expected per COBOL ground truth:
        // "00000000100". This test intentionally fails against current Java to flag
        // the gap — it is not a bug in the test.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFraccti("100"));
        givenStartBrowseResp(0);
        givenReadNextRows(100L, 101L);

        service.mainLine(appService);

        ArgumentCaptor<String> startBr = ArgumentCaptor.forClass(String.class);
        verify(appService).startBrowse(eq(ACCTFILE), startBr.capture(), eq(0));
        assertEquals("00000000100", startBr.getValue());
    }

    @Test
    void mainLine_enterNumericStart_startsBrowseAtGivenId() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFraccti("100"));
        givenStartBrowseResp(0);
        givenReadNextRows(100L, 101L);

        service.mainLine(appService);

        verify(appService).startBrowse(eq(ACCTFILE), anyString(), eq(0));

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcacctlFields out = (OcacctlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo("2 account(s) displayed.");
    }

    @Test
    void mainLine_enterReceiveMapfail_treatsInputAsBlankAndBrowsesFromZero() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        nextResp.set(36); // DFHRESP(MAPFAIL) — no receiveMap stub, fields stay at defaults post
        // fill-low-values
        givenStartBrowseResp(0);
        givenReadNextRows(); // no rows -> immediate ENDFILE

        service.mainLine(appService);

        verify(appService).startBrowse(eq(ACCTFILE), anyString(), eq(0));
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcacctlFields out = (OcacctlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
    }

    @Test
    void mainLine_enterNonNumericStart_rejectsBadStartAndSkipsBrowse() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFraccti("ABCDEFGHIJK"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcacctlFields out = (OcacctlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_START);
        assertEquals(-1, out.getFracctl());
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterStartNotFound_showsNoneFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFraccti("999999999"));
        givenStartBrowseResp(13); // DFHRESP(NOTFND)

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcacctlFields out = (OcacctlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_enterBrowseFileError_finalMessageIsNoneFoundPerCobolOverwrite() {
        // Per COBOL 3100-START-BROWSE: an unexpected STARTBR response sets
        // ERRMSGO to WS-M-BROWSE-ERR, but 2100-LIST-FROM-START unconditionally calls
        // 2300-BUILD-SUMMARY afterward, which — since WS-ROW-CNT is still zero —
        // overwrites ERRMSGO with WS-M-NONE-FOUND. This is original COBOL behavior
        // (verified against source), not a convert gap.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFraccti("1"));
        givenStartBrowseResp(99); // unexpected error

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcacctlFields out = (OcacctlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
    }

    @Test
    void mainLine_enterReadNextError_stopsBrowseButKeepsRowsAlreadyRead() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFraccti("1"));
        givenStartBrowseResp(0);
        givenReadNextRowThenError(1L);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcacctlFields out = (OcacctlFields) sendMapOut.getValue();
        // one row was read before the error -> 2300-BUILD-SUMMARY reports the count,
        // overwriting the browse-error message (same COBOL overwrite rule as above).
        assertThat(out.getErrmsgo().trim()).isEqualTo("1 account(s) displayed.");
        assertThat(out.getAcl1o().trim()).isEqualTo("00000000001");
    }

    // ───────────────────────── 2200-PAGE-FWD (PF8) ─────────────────────────

    @Test
    void mainLine_pf8WithNumericWorkArea_resumesBrowseAtSavedKey() {
        givenPseudoConversation(1, "00000000042");
        when(appService.getEibaid()).thenReturn("8");
        givenStartBrowseResp(0);
        givenReadNextRows(42L, 43L);

        service.mainLine(appService);

        verify(appService).startBrowse(eq(ACCTFILE), anyString(), eq(0));
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcacctlFields out = (OcacctlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo("2 account(s) displayed.");
    }

    @Test
    void mainLine_pf8WithNonNumericWorkArea_defaultsToZero() {
        givenPseudoConversation(1, "            ");
        when(appService.getEibaid()).thenReturn("8");
        givenStartBrowseResp(0);
        givenReadNextRows(1L);

        service.mainLine(appService);

        verify(appService).startBrowse(eq(ACCTFILE), anyString(), eq(0));
    }

    @Test
    void mainLine_pf8EndOfFile_showsEndFileMessageNotNoneFound() {
        givenPseudoConversation(1, "00000000999");
        when(appService.getEibaid()).thenReturn("8");
        givenStartBrowseResp(13); // DFHRESP(NOTFND) -> zero rows

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcacctlFields out = (OcacctlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_END_FILE);
    }

    @Test
    void mainLine_pf8PartialPage_reportsCountAndSavesNextKey() {
        givenPseudoConversation(1, "00000000010");
        when(appService.getEibaid()).thenReturn("8");
        givenStartBrowseResp(0);
        givenReadNextRows(10L, 11L, 12L);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTLA"), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcacctlFields out = (OcacctlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo("3 account(s) displayed.");
        assertThat(out.getAcl3o().trim()).isEqualTo("00000000012");
        assertThat(out.getAcl4o().trim()).isEmpty();
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcacctl() {
        assertEquals("OCACCTL", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrla() {
        assertEquals("ORLA", service.getTransId());
    }
}

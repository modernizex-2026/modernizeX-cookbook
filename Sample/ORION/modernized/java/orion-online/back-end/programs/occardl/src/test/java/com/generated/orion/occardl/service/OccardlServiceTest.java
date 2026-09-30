package com.generated.orion.occardl.service;

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
import com.generated.orion.occardl.accessor.OccardlFields;
import com.generated.orion.occardl.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OccardlService, generated from COBOL program OCCARDL. All business logic is
 * private and is exercised solely through the public {@link OccardlService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS
 * SEND/RECEIVE/STARTBR/READNEXT/ENDBR/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OccardlServiceTest {

    private static final String CARDFILE = "CARDFILE";

    private static final String MSG_PROMPT = "Enter account id and press ENTER to list cards.";
    private static final String MSG_REQUIRED_KEY_MISSING = "Please enter a value.";
    private static final String MSG_NOT_NUMERIC = "Account id must be numeric.";
    private static final String MSG_LIST_FIRST = "List an account first (press ENTER).";
    private static final String MSG_NO_MORE = "No more cards for this account.";
    private static final String MSG_NONE_FOUND = "No cards found for this account.";
    private static final String MSG_MORE_PAGES = "Cards listed. PF8=next PF7=top PF3=menu.";
    private static final String MSG_END_OF_LIST = "End of list. PF7=top PF3=menu.";

    @Mock private AppService appService;

    private OccardlService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OccardlService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /**
     * Builds a serialized ORION-COMMAREA string carrying the given
     * CA-PGM-CONTEXT/CA-WORK-AREA/CA-ACCT-ID.
     */
    private String commarea(int context, String workArea, Long acctId) {
        OccardlFields helper = new OccardlFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        if (workArea != null) {
            helper.setCaWorkArea(workArea);
        }
        if (acctId != null) {
            helper.setCaAcctId(acctId);
        }
        return helper.getOrionCommarea();
    }

    private void givenPseudoConversation(int context) {
        givenPseudoConversation(context, null, null);
    }

    private void givenPseudoConversation(int context, String workArea, Long acctId) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commarea(context, workArea, acctId));
    }

    /**
     * Stubs receiveMap to populate the MCARDLAI input fields (ACCTIDI) captured from production
     * code.
     */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OccardlFields> populate) {
        doAnswer(
                        inv -> {
                            OccardlFields f = inv.getArgument(1);
                            populate.accept(f);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MCARDLA"), any());
    }

    /** Stubs STARTBR on CARDFILE to return the given EIBRESP (0=NORMAL, 13=NOTFND, other=error). */
    private void givenStartBrowseResp(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .startBrowse(eq(CARDFILE), anyString(), eq(0));
    }

    /**
     * Stubs READNEXT on CARDFILE to hand back the given card numbers (for the account already
     * stored in CA-ACCT-ID) in order, active status "Y", then signal ENDFILE (resp 20) once
     * exhausted.
     */
    private void givenReadNextCards(long acctId, String... cardNums) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            OccardlFields into = inv.getArgument(1);
                            int i = idx.getAndIncrement();
                            if (i < cardNums.length) {
                                into.setCdAcctId(acctId);
                                into.setCdNum(cardNums[i]);
                                into.setCdActiveStatus("Y");
                                nextResp.set(0);
                            } else {
                                nextResp.set(20);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(eq(CARDFILE), any());
    }

    /**
     * Stubs READNEXT to return rows belonging to a DIFFERENT account (filtered out by 3200-GATHER).
     */
    private void givenReadNextForeignThenEnd(long foreignAcctId, String cardNum) {
        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            OccardlFields into = inv.getArgument(1);
                            if (callCount.getAndIncrement() == 0) {
                                into.setCdAcctId(foreignAcctId);
                                into.setCdNum(cardNum);
                                into.setCdActiveStatus("Y");
                                nextResp.set(0);
                            } else {
                                nextResp.set(20);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(eq(CARDFILE), any());
    }

    private ArgumentCaptor<Object> captureLastSendMap(int times) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(times))
                .sendMap(
                        eq("MCARDLA"),
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
                .sendMap(
                        eq("MCARDLA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OccardlFields out = (OccardlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORCL");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCCARDL");
        verify(appService).returnTransid(eq("ORCL"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalContextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq("MCARDLA"), any(), any(), eq(true), eq(false), eq(false));
        verify(appService).returnTransid(eq("ORCL"), any(), eq(692));
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
                .sendMap(
                        eq("MCARDLA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OccardlFields out = (OccardlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_clearPressed_resendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("_");

        service.mainLine(appService);

        verify(appService).sendMap(eq("MCARDLA"), any(), any(), eq(true), eq(false), eq(false));
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessageWithoutErase() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MCARDLA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OccardlFields out = (OccardlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isNotEmpty();
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-FIRST-PAGE (ENTER / PF7) ─────────────────────────

    @Test
    void mainLine_enterValidAccountId_browsesFullPageAndSavesResumeKeyAndListState() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("00000000123"));
        givenStartBrowseResp(0);
        givenReadNextCards(
                123L,
                "1111222233334444",
                "1111222233335555",
                "1111222233336666",
                "1111222233337777",
                "1111222233338888");

        service.mainLine(appService);

        verify(appService).startBrowse(eq(CARDFILE), anyString(), eq(0));
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MCARDLA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OccardlFields out = (OccardlFields) sendMapOut.getValue();
        assertThat(out.getAcctido().trim()).isEqualTo("00000000123");
        assertThat(out.getCard1o().trim()).isEqualTo("1111222233334444");
        assertThat(out.getStat1o().trim()).isEqualTo("Y");
        assertThat(out.getCard5o().trim()).isEqualTo("1111222233338888");
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_MORE_PAGES);
        verify(appService).endBrowse(eq(CARDFILE));
    }

    @Test
    void mainLine_enterBlankAccountId_rejectsRequiredAndSkipsBrowse() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("           "));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MCARDLA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OccardlFields out = (OccardlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isNotEmpty();
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterNonNumericAccountId_rejectsAndSkipsBrowse() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("ABCDEFGHIJK"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MCARDLA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OccardlFields out = (OccardlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOT_NUMERIC);
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterAccountIdWithEmbeddedSpaces_skipsSpacesAndAccumulatesDigits() {
        // Ground truth per COBOL 6000-VALIDATE-ACCTID: a SPACE character at any position
        // (including embedded mid-field) is simply skipped (CONTINUE) -- only a non-space,
        // non-digit character marks the input invalid. "1  2  3    " therefore parses as
        // the numeric value 123, not as an error.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("1  2  3    "));
        givenStartBrowseResp(13); // NOTFND -> zero rows, but proves validation passed

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MCARDLA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OccardlFields out = (OccardlFields) sendMapOut.getValue();
        assertThat(out.getAcctido().trim()).isEqualTo("00000000123");
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
    }

    @Test
    void mainLine_enterStartBrowseNotFound_showsNoCardsFoundAndSkipsEndBrowse() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("00000000999"));
        givenStartBrowseResp(13); // DFHRESP(NOTFND)

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MCARDLA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OccardlFields out = (OccardlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
        verify(appService, never()).readNext(anyString(), any());
        // COBOL 3000-BROWSE-CARDS: "IF BR-END GO TO 3000-EXIT" bypasses 3300-END-BROWSE entirely.
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_enterPartialPage_reportsEndOfListWhenFewerThanFiveMatch() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("00000000042"));
        givenStartBrowseResp(0);
        givenReadNextCards(42L, "1111222233334444", "1111222233335555");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MCARDLA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OccardlFields out = (OccardlFields) sendMapOut.getValue();
        assertThat(out.getCard1o().trim()).isEqualTo("1111222233334444");
        assertThat(out.getCard2o().trim()).isEqualTo("1111222233335555");
        assertThat(out.getCard3o().trim()).isEmpty();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_END_OF_LIST);
    }

    @Test
    void mainLine_enterCardsBelongToOtherAccount_filtersThemOutAsNoCardsFound() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("00000000042"));
        givenStartBrowseResp(0);
        givenReadNextForeignThenEnd(999L, "1111222233334444");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MCARDLA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OccardlFields out = (OccardlFields) sendMapOut.getValue();
        assertThat(out.getCard1o().trim()).isEmpty();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
    }

    @Test
    void mainLine_enterFullPage_convertGap_resumeCardShouldBeLastCardReadNotStartKey() {
        // CONVERT-GAP: in COBOL, EXEC CICS READNEXT RIDFLD(WS-BROWSE-KEY) is a bidirectional
        // parameter -- CICS writes the key of the record just read back into WS-BROWSE-KEY,
        // which 3100-READ-NEXT then copies into WS-RESUME-CARD on every successful read. So
        // after listing a full page, WS-RESUME-CARD (saved into CA-WORK-AREA(2:16) for the
        // next PF8) equals the LAST card's key (here "1111222233338888"). The converted
        // AppService.readNext(fileName, into) signature has no RIDFLD out-parameter, and
        // readNextCard() does `wsResumeCard = wsBrowseKey`, where wsBrowseKey was set only
        // once in startCardBrowse() from WS-START-CARD and is never advanced afterward. So
        // the Java resume key stays frozen at the browse's starting key (low-values on the
        // first page) instead of tracking the last row read -- forward paging (PF8) cannot
        // work correctly. Expected per COBOL ground truth: resume card == last card read.
        // This test intentionally fails against current Java to flag the gap -- it is not a
        // bug in the test.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("00000000123"));
        givenStartBrowseResp(0);
        givenReadNextCards(
                123L,
                "1111222233334444",
                "1111222233335555",
                "1111222233336666",
                "1111222233337777",
                "1111222233338888");

        service.mainLine(appService);

        ArgumentCaptor<Object> commareaOut = ArgumentCaptor.forClass(Object.class);
        verify(appService).returnTransid(eq("ORCL"), commareaOut.capture(), eq(692));
        byte[] commareaBytes = (byte[]) commareaOut.getValue();
        String commareaStr = new String(commareaBytes, java.nio.charset.Charset.forName("MS932"));
        OccardlFields returned = new OccardlFields(new WorkingStorage());
        returned.setOrionCommarea(commareaStr);
        String resumeCard = returned.getCaWorkArea().substring(1, 16).trim();
        assertThat(resumeCard).isEqualTo("1111222233338888");
    }

    // ───────────────────────── 2200-NEXT-PAGE (PF8) ─────────────────────────

    @Test
    void mainLine_pf8WithoutPriorListing_showsListFirstMessageAndSkipsBrowse() {
        givenPseudoConversation(1, "K", null); // CA-WORK-AREA(1:1) = 'K' (key entry), not 'L'
        when(appService.getEibaid()).thenReturn("8");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MCARDLA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OccardlFields out = (OccardlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LIST_FIRST);
        verify(appService, never()).receiveMap(anyString(), any());
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    @Test
    void mainLine_pf8AfterListing_resumesBrowseAndReturnsNextRows() {
        givenPseudoConversation(1, "L", 42L);
        when(appService.getEibaid()).thenReturn("8");
        givenStartBrowseResp(0);
        givenReadNextCards(42L, "2222333344445555", "2222333344446666");

        service.mainLine(appService);

        verify(appService).startBrowse(eq(CARDFILE), anyString(), eq(0));
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MCARDLA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OccardlFields out = (OccardlFields) sendMapOut.getValue();
        // Per COBOL 3000-BROWSE-CARDS: STARTBR positions GTEQ the saved resume key, so the
        // first READNEXT re-reads that same already-shown record (3100-READ-NEXT "skip"
        // call before the gather loop) -- only the SECOND stubbed card is a genuinely new row.
        assertThat(out.getCard1o().trim()).isEqualTo("2222333344446666");
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_END_OF_LIST);
    }

    @Test
    void mainLine_pf8NoMoreRows_showsNoMoreCardsMessageOverwritingNoCardsFound() {
        // Per COBOL 4000-SHOW-PAGE / 2200-NEXT-PAGE: when zero rows come back, 4000-SHOW-PAGE
        // first sets ERRMSGO to "No cards found for this account.", then 2200-NEXT-PAGE
        // unconditionally overwrites it with "No more cards for this account." -- this
        // two-step overwrite is original COBOL behavior (verified against source), not a
        // convert gap.
        givenPseudoConversation(1, "L", 42L);
        when(appService.getEibaid()).thenReturn("8");
        givenStartBrowseResp(0);
        givenReadNextCards(42L); // no cards -> immediate ENDFILE

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MCARDLA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OccardlFields out = (OccardlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NO_MORE);
    }

    // ───────────────────────── 3050/9500 unexpected STARTBR error → abend
    // ─────────────────────────

    @Test
    void mainLine_startBrowseUnexpectedError_sendsAbendTextAndReturnsProgram() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("00000000042"));
        givenStartBrowseResp(99); // unexpected error -> 9500-ABEND-RTN

        service.mainLine(appService);

        verify(appService).sendText(anyString(), eq(true), eq(true));
        verify(appService).returnProgram();
    }

    @Test
    void mainLine_startBrowseUnexpectedError_convertGap_sendTextShouldCarryMessageText() {
        // CONVERT-GAP: COBOL's 9500-ABEND-RTN does
        //   EXEC CICS SEND TEXT FROM(WS-MSG-TEXT) ...
        // i.e. it sends the literal error message text. The converted
        // abendWithFileError() instead calls
        //   ctx.appService.sendText(String.valueOf(ctx.f), true, true)
        // which stringifies the WHOLE OccardlFields accessor object (its default
        // Object.toString(), e.g. "com.generated...OccardlFields@1a2b3c") instead of
        // ctx.f.getWsMsgText(). Expected per COBOL ground truth: the text sent should
        // contain the WS-MSG-TEXT message. This test intentionally fails against the
        // current Java to flag the gap -- it is not a bug in the test.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("00000000042"));
        givenStartBrowseResp(99);

        service.mainLine(appService);

        verify(appService)
                .sendText(
                        org.mockito.ArgumentMatchers.contains("unrecoverable file error"),
                        eq(true),
                        eq(true));
    }

    @Test
    void mainLine_startBrowseUnexpectedError_convertGap_returnTransidShouldNotFollowAbend() {
        // CONVERT-GAP: in COBOL, 9500-ABEND-RTN issues EXEC CICS RETURN (no TRANSID),
        // which ends the pseudo-conversational task immediately -- 9000-RETURN is never
        // reached afterward. The converted Java abendWithFileError() calls
        // appService.returnProgram() but does not stop the Java call stack (the mock simply
        // returns normally), so runMainProgram() falls through and returnTransid() fires
        // anyway. Expected per COBOL ground truth: returnTransid must NOT be called after
        // an abend. This test intentionally fails against current Java to flag the gap --
        // it is not a bug in the test.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("00000000042"));
        givenStartBrowseResp(99);

        service.mainLine(appService);

        verify(appService, never()).returnTransid(anyString(), any(), anyInt());
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOccardl() {
        assertEquals("OCCARDL", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrcl() {
        assertEquals("ORCL", service.getTransId());
    }

    @Test
    void getButtonDefs_delegatesToMetadataWithoutError() {
        assertNotNull(service.getButtonDefs());
    }

    @Test
    void getFieldMapping_delegatesToMetadataForKnownMap() {
        assertNotNull(service.getFieldMapping("MCARDLA"));
    }
}

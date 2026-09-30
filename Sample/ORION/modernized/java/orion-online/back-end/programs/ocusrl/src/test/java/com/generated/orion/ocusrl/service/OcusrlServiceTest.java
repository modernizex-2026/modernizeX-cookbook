package com.generated.orion.ocusrl.service;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.ocusrl.accessor.OcusrlFields;
import com.generated.orion.ocusrl.model.WorkingStorage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OcusrlService, generated from COBOL program OCUSRL (ORION-CCMS on-line user list).
 * All business logic is private and is exercised solely through the public {@link
 * OcusrlService#mainLine(AppService)} entry point, driven by an {@link AppService} mock emulating
 * CICS SEND/STARTBR/READNEXT/ ENDBR/XCTL/RETURN.
 *
 * <p>Convert-gap check: every COBOL paragraph (0000-MAIN..9000-RETURN) was compared against its
 * Java counterpart line-by-line, including the CICS RESP code mapping (STARTBR NOTFND=13, READNEXT
 * ENDFILE=20), the CA-FIRST-ENTER=CA-PGM-CONTEXT=0 condition (confirmed via KCOMM copybook), the
 * copyBytes(dest, src) direction for WS-STATE-AREA / CA-WORK-AREA, and the STRING ... DELIMITED BY
 * SPACE full-name build (mirrored by split(" ", 2)[0]). No behavioral divergence was found, so no
 * CONVERT-GAP test is included here.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcusrlServiceTest {

    private static final String MAP_NAME = "MUSRLA";
    private static final String TRAN_ID = "ORUL";
    private static final String PGM_NAME = "OCUSRL";
    private static final String ADMEN_PGM = "OCADMEN";
    private static final int COMMAREA_LENGTH = 692;

    private static final String MSG_NO_USERS = "No users found.";
    private static final String MSG_PAGING = "PF8 next page  PF7 top  PF3 exit.";
    private static final String MSG_TOP = "Top of list. PF8 next page.";
    private static final String MSG_NO_MORE = "No more users to display.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_ERR_STARTBR = "Error starting user browse.";
    private static final String MSG_ERR_READ = "Error reading user file.";

    @Mock private AppService appService;

    private final OcusrlService service = new OcusrlService();

    private final AtomicInteger eibresp = new AtomicInteger(0);

    private String commarea(int pgmContext, String lastKey) {
        OcusrlFields helper = new OcusrlFields(new WorkingStorage());
        helper.setCaPgmContext(pgmContext);
        if (lastKey != null) {
            helper.setWsLastKey(lastKey);
            helper.copyBytes("CA-WORK-AREA", "WS-STATE-AREA");
        }
        return helper.getOrionCommarea();
    }

    private void givenPseudoConversation(int pgmContext, String lastKey) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commarea(pgmContext, lastKey));
    }

    private void givenFormatTimeStub() {
        when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** RESP(NORMAL)=0 by default; tests override eibresp explicitly for other paths. */
    private void givenEibrespTracksMutableState() {
        when(appService.getEibresp()).thenAnswer(inv -> eibresp.get());
    }

    private void givenStartBrowseResp(int resp) {
        doAnswer(
                        inv -> {
                            eibresp.set(resp);
                            return null;
                        })
                .when(appService)
                .startBrowse(anyString(), anyString(), anyInt());
    }

    /**
     * Queues USER-REC rows for successive READNEXT calls (used for both the paging
     * repositioning-record skip and the 4-row display loop). When the queue is exhausted,
     * subsequent calls report RESP(ENDFILE)=20.
     */
    private void givenUserFileRows(List<String[]> rows) {
        Queue<String[]> queue = new ArrayDeque<>(rows);
        doAnswer(
                        inv -> {
                            OcusrlFields into = inv.getArgument(1);
                            String[] row = queue.poll();
                            if (row == null) {
                                eibresp.set(20);
                            } else {
                                into.setUsId(row[0]);
                                into.setUsFirstName(row[1]);
                                into.setUsLastName(row[2]);
                                into.setUsType(row[3]);
                                eibresp.set(0);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
    }

    private void givenReadNextError() {
        doAnswer(
                        inv -> {
                            eibresp.set(1);
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
    }

    private OcusrlFields decode(Object commareaArg) {
        OcusrlFields decoded = new OcusrlFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaArg);
        return decoded;
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_eibcalenZero_noUsers_sendsInitialScreenWithNoUsersMessage() {
        when(appService.getEibcalen()).thenReturn(0);
        givenEibrespTracksMutableState();
        givenStartBrowseResp(13);
        givenFormatTimeStub();

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcusrlFields out = (OcusrlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NO_USERS);
        verify(appService, never()).readNext(anyString(), any());
        verify(appService).returnTransid(eq(TRAN_ID), any(), eq(COMMAREA_LENGTH));
    }

    @Test
    void mainLine_eibcalenZero_withUsers_sendsInitialScreenWithPagingMessage() {
        when(appService.getEibcalen()).thenReturn(0);
        givenEibrespTracksMutableState();
        givenStartBrowseResp(0);
        givenUserFileRows(
                Arrays.asList(
                        new String[] {"USER0001", "JOHN", "DOE", "A"},
                        new String[] {"USER0002", "JANE", "ROE", "U"}));
        givenFormatTimeStub();

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcusrlFields out = (OcusrlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PAGING);
        assertThat(out.getUsr1o().trim()).isEqualTo("USER0001");
        assertThat(out.getUnm1o().trim()).isEqualTo("JOHN DOE");
        assertThat(out.getUty1o().trim()).isEqualTo("A");
        assertThat(out.getUsr2o().trim()).isEqualTo("USER0002");
        assertThat(out.getUty2o().trim()).isEqualTo("U");
    }

    @Test
    void mainLine_firstEnterContextZero_sendsInitialScreenInsteadOfDispatch() {
        givenPseudoConversation(0, null);
        givenEibrespTracksMutableState();
        givenStartBrowseResp(13);
        givenFormatTimeStub();

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(false));
        verify(appService, never()).xctl(anyString(), any(), anyInt());
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_xctlsToAdminMenuAndResetsContext() {
        givenPseudoConversation(1, null);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        ArgumentCaptor<Object> commareaCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .xctl(
                        argThat(s -> s.trim().equals(ADMEN_PGM)),
                        commareaCaptor.capture(),
                        eq(COMMAREA_LENGTH));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());

        OcusrlFields decoded = decode(commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo(PGM_NAME);
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo(TRAN_ID);
        assertEquals(0, decoded.getCaPgmContext());
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessageWithoutBrowsing() {
        givenPseudoConversation(1, null);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusrlFields out = (OcusrlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    // ───────────────────────── 2100-PAGE-TOP ─────────────────────────

    @Test
    void mainLine_pf7Pressed_noUsers_showsNoUsersFoundMessage() {
        givenPseudoConversation(1, null);
        givenEibrespTracksMutableState();
        givenStartBrowseResp(13);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("7");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusrlFields out = (OcusrlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NO_USERS);
    }

    @Test
    void mainLine_pf7Pressed_withUsers_showsTopOfListMessage() {
        givenPseudoConversation(1, null);
        givenEibrespTracksMutableState();
        givenStartBrowseResp(0);
        givenUserFileRows(Collections.singletonList(new String[] {"USER0009", "AL", "SMITH", "U"}));
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("7");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusrlFields out = (OcusrlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TOP);
        assertThat(out.getUsr1o().trim()).isEqualTo("USER0009");
        verify(appService, never()).receiveMap(anyString(), any());
    }

    @Test
    void mainLine_enterKeyPressed_behavesLikePf7() {
        givenPseudoConversation(1, null);
        givenEibrespTracksMutableState();
        givenStartBrowseResp(0);
        givenUserFileRows(Collections.singletonList(new String[] {"USER0009", "AL", "SMITH", "U"}));
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("'");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusrlFields out = (OcusrlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TOP);
    }

    @Test
    void mainLine_startBrowseError_setsErrorMessageAndZeroRows() {
        givenPseudoConversation(1, null);
        givenEibrespTracksMutableState();
        givenStartBrowseResp(1);
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("7");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusrlFields out = (OcusrlFields) sendMapOut.getValue();
        // ERR-FLG-ON short-circuits 2200-LIST-PAGE before any row is read, so
        // WS-ROW-COUNT stays 0 and 2100-PAGE-TOP's own message wins.
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NO_USERS);
        verify(appService, never()).readNext(anyString(), any());
    }

    // ───────────────────────── 2150-PAGE-NEXT ─────────────────────────

    @Test
    void mainLine_pf8Pressed_withMoreUsers_skipsRepositionRecordAndShowsNextPage() {
        givenPseudoConversation(1, "USER0002");
        givenEibrespTracksMutableState();
        givenStartBrowseResp(0);
        givenUserFileRows(
                Arrays.asList(
                        new String[] {"USER0002", "JANE", "ROE", "U"},
                        new String[] {"USER0003", "BOB", "LEE", "A"}));
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("8");

        service.mainLine(appService);

        verify(appService).startBrowse(anyString(), eq("USER0002"), eq(0));
        // 1 skip call (consumes the repositioning record at USER0002) + 1 call that
        // returns USER0003 + 1 call that hits end-of-file and stops the loop.
        verify(appService, times(3)).readNext(anyString(), any());
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusrlFields out = (OcusrlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PAGING);
        assertThat(out.getUsr1o().trim()).isEqualTo("USER0003");
    }

    @Test
    void mainLine_pf8Pressed_skipHitsEndOfFile_showsNoMoreUsersMessage() {
        givenPseudoConversation(1, "USER0002");
        givenEibrespTracksMutableState();
        givenStartBrowseResp(0);
        givenUserFileRows(Arrays.asList());
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("8");

        service.mainLine(appService);

        verify(appService, times(1)).readNext(anyString(), any());
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusrlFields out = (OcusrlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NO_MORE);
    }

    @Test
    void mainLine_pf8Pressed_noRemainingUsersAfterSkip_showsNoMoreUsersMessage() {
        givenPseudoConversation(1, "USER0002");
        givenEibrespTracksMutableState();
        givenStartBrowseResp(0);
        givenUserFileRows(Collections.singletonList(new String[] {"USER0002", "JANE", "ROE", "U"}));
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("8");

        service.mainLine(appService);

        verify(appService, times(2)).readNext(anyString(), any());
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusrlFields out = (OcusrlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NO_MORE);
    }

    @Test
    void mainLine_readNextError_stopsLoopEarlyWithErrorMessage() {
        givenPseudoConversation(1, null);
        givenEibrespTracksMutableState();
        givenStartBrowseResp(0);
        givenReadNextError();
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("7");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusrlFields out = (OcusrlFields) sendMapOut.getValue();
        // WS-ROW-COUNT stays 0 because the very first READNEXT errors, so
        // 2100-PAGE-TOP's own "no users" branch wins over the READNEXT error text.
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NO_USERS);
        verify(appService, times(1)).readNext(anyString(), any());
        verify(appService).endBrowse(anyString());
    }

    // ───────────────────────── 2200-LIST-PAGE row limit ─────────────────────────

    @Test
    void mainLine_exactlyFourUsers_fillsAllRowsAndStopsAtRowLimit() {
        givenPseudoConversation(1, null);
        givenEibrespTracksMutableState();
        givenStartBrowseResp(0);
        givenUserFileRows(
                Arrays.asList(
                        new String[] {"USER0001", "AA", "BB", "A"},
                        new String[] {"USER0002", "CC", "DD", "A"},
                        new String[] {"USER0003", "EE", "FF", "A"},
                        new String[] {"USER0004", "GG", "HH", "A"}));
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("7");

        service.mainLine(appService);

        verify(appService, times(4)).readNext(anyString(), any());
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusrlFields out = (OcusrlFields) sendMapOut.getValue();
        assertThat(out.getUsr4o().trim()).isEqualTo("USER0004");
        // PF7 drives 2100-PAGE-TOP, whose own message wins over the generic paging text.
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TOP);
    }

    @Test
    void mainLine_moreThanFourUsers_onlyFirstFourShownAndLastKeySavedForPaging() {
        givenPseudoConversation(1, null);
        givenEibrespTracksMutableState();
        givenStartBrowseResp(0);
        givenUserFileRows(
                Arrays.asList(
                        new String[] {"USER0001", "AA", "BB", "A"},
                        new String[] {"USER0002", "CC", "DD", "A"},
                        new String[] {"USER0003", "EE", "FF", "A"},
                        new String[] {"USER0004", "GG", "HH", "A"},
                        new String[] {"USER0005", "II", "JJ", "A"}));
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("7");

        service.mainLine(appService);

        verify(appService, times(4)).readNext(anyString(), any());
        ArgumentCaptor<Object> commareaCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .returnTransid(eq(TRAN_ID), commareaCaptor.capture(), eq(COMMAREA_LENGTH));
        OcusrlFields decoded = decode(commareaCaptor.getValue());
        assertThat(decoded.getString("CA-WORK-AREA").substring(0, 8)).isEqualTo("USER0004");
    }

    @Test
    void mainLine_pf8_persistsLastKeyAcrossPseudoConversation() {
        givenPseudoConversation(1, "USER0004");
        givenEibrespTracksMutableState();
        givenStartBrowseResp(0);
        givenUserFileRows(
                Arrays.asList(
                        new String[] {"USER0004", "GG", "HH", "A"},
                        new String[] {"USER0005", "II", "JJ", "A"}));
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("8");

        service.mainLine(appService);

        ArgumentCaptor<Object> commareaCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .returnTransid(eq(TRAN_ID), commareaCaptor.capture(), eq(COMMAREA_LENGTH));
        OcusrlFields decoded = decode(commareaCaptor.getValue());
        assertThat(decoded.getString("CA-WORK-AREA").substring(0, 8)).isEqualTo("USER0005");
    }

    // ───────────────────────── 2255-BUILD-NAME ─────────────────────────

    @Test
    void mainLine_buildFullName_combinesFirstAndLastNameWithSingleSpace() {
        givenPseudoConversation(1, null);
        givenEibrespTracksMutableState();
        givenStartBrowseResp(0);
        givenUserFileRows(Collections.singletonList(new String[] {"USER0001", "JOHN", "DOE", "A"}));
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("7");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusrlFields out = (OcusrlFields) sendMapOut.getValue();
        assertThat(out.getUnm1o().trim()).isEqualTo("JOHN DOE");
    }

    @Test
    void mainLine_buildFullName_nameWithEmbeddedSpace_truncatesAtFirstSpace() {
        givenPseudoConversation(1, null);
        givenEibrespTracksMutableState();
        givenStartBrowseResp(0);
        givenUserFileRows(
                Collections.singletonList(new String[] {"USER0001", "MARY ANN", "SMITH", "A"}));
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("7");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusrlFields out = (OcusrlFields) sendMapOut.getValue();
        // COBOL STRING ... DELIMITED BY SPACE stops at the first embedded space,
        // so "MARY ANN" contributes only "MARY" - mirrored by split(" ", 2)[0].
        assertThat(out.getUnm1o().trim()).isEqualTo("MARY SMITH");
    }
}

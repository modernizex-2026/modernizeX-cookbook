package com.generated.orion.ocusru.service;

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
import com.generated.orion.ocusru.accessor.OcusruFields;
import com.generated.orion.ocusru.model.WorkingStorage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OcusruService, generated from COBOL program OCUSRU (ORION-CCMS on-line user
 * update). All business logic is private and is exercised solely through the public {@link
 * OcusruService#mainLine(AppService)} entry point, driven by an {@link AppService} mock emulating
 * CICS SEND/RECEIVE/READ/REWRITE/ XCTL/RETURN.
 *
 * <p>Convert-gap check: every COBOL paragraph (0000-MAIN..9000-RETURN) was compared against its
 * Java counterpart, including the RESP code mapping (NORMAL=0, NOTFND=13), the CA-PGM-CONTEXT=0
 * "first enter" condition, and the WS-SV-USERID-as-update-key rule in 3100-UPDATE-USER. No
 * behavioral divergence was found, so no CONVERT-GAP test is included. One COBOL quirk (not a
 * conversion gap — Java mirrors it faithfully) is called out below: 2200-FETCH-USER's REC-FOUND
 * branch silently overrides 3000-READ-USER's own "Error reading user file." message on an OTHER
 * RESP, so any non-NORMAL/NOTFND read error surfaces as "User not found."
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcusruServiceTest {

    private static final String MAP_NAME = "MUSRUA";
    private static final String TRAN_ID = "ORUP";
    private static final String PGM_NAME = "OCUSRU";
    private static final String ADMEN_PGM = "OCADMEN";
    private static final int COMMAREA_LENGTH = 692;

    private static final String MSG_ENTER_ID = "Enter a user id and press ENTER.";
    private static final String MSG_REQUIRED = "Please enter all required fields.";
    private static final String MSG_NOT_FOUND = "User not found.";
    private static final String MSG_MODIFY = "Modify fields and press ENTER to update.";
    private static final String MSG_FNAME_REQ = "First name is required.";
    private static final String MSG_LNAME_REQ = "Last name is required.";
    private static final String MSG_PWD_REQ = "Password is required.";
    private static final String MSG_TYPE_BAD = "Type must be A (admin) or U (user).";
    private static final String MSG_NO_LONGER = "User no longer exists.";
    private static final String MSG_ERR_READ_UPD = "Error reading user for update.";
    private static final String MSG_ERR_UPDATE = "Error updating user file.";
    private static final String MSG_SUCCESS = "User updated successfully.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";

    @Mock private AppService appService;

    private final OcusruService service = new OcusruService();

    private final AtomicInteger eibresp = new AtomicInteger(0);

    private String commarea(int pgmContext, String stage, String svUserid) {
        OcusruFields helper = new OcusruFields(new WorkingStorage());
        helper.setCaPgmContext(pgmContext);
        if (stage != null) {
            helper.setWsStage(stage);
            helper.setWsSvUserid(svUserid == null ? " " : svUserid);
            helper.copyBytes("CA-WORK-AREA", "WS-STATE-AREA");
        }
        return helper.getOrionCommarea();
    }

    private void givenPseudoConversation(int pgmContext, String stage, String svUserid) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commarea(pgmContext, stage, svUserid));
    }

    private void givenFormatTimeStub() {
        when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** RESP(NORMAL)=0 by default; tests override eibresp explicitly for other paths. */
    private void givenEibrespTracksMutableState() {
        when(appService.getEibresp()).thenAnswer(inv -> eibresp.get());
    }

    private void givenReceivedMap(
            String useridi, String usfnami, String uslnami, String uspwdi, String ustypei) {
        doAnswer(
                        inv -> {
                            OcusruFields into = inv.getArgument(1);
                            into.setUseridi(useridi == null ? " " : useridi);
                            into.setUsfnami(usfnami == null ? " " : usfnami);
                            into.setUslnami(uslnami == null ? " " : uslnami);
                            into.setUspwdi(uspwdi == null ? " " : uspwdi);
                            into.setUstypei(ustypei == null ? " " : ustypei);
                            eibresp.set(0);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    /** Stubs the plain (non-UPDATE) READ used by 2200-FETCH-USER / 3000-READ-USER. */
    private void givenReadFileResp(
            int resp, String firstName, String lastName, String password, String type) {
        doAnswer(
                        inv -> {
                            if (resp == 0) {
                                OcusruFields into = inv.getArgument(1);
                                into.setUsFirstName(firstName);
                                into.setUsLastName(lastName);
                                into.setUsPassword(password);
                                into.setUsType(type);
                            }
                            eibresp.set(resp);
                            return null;
                        })
                .when(appService)
                .readFile(anyString(), any(), anyString(), anyInt());
    }

    /** Stubs the UPDATE READ used by 3100-UPDATE-USER. */
    private void givenReadFileForUpdateResp(int resp) {
        doAnswer(
                        inv -> {
                            eibresp.set(resp);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    private void givenRewriteResp(int resp) {
        doAnswer(
                        inv -> {
                            eibresp.set(resp);
                            return null;
                        })
                .when(appService)
                .rewriteFile(anyString(), any());
    }

    private OcusruFields decode(Object commareaArg) {
        OcusruFields decoded = new OcusruFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaArg);
        return decoded;
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_eibcalenZero_sendsInitialScreenWithEraseAndPromptMessage() {
        when(appService.getEibcalen()).thenReturn(0);
        givenEibrespTracksMutableState();
        givenFormatTimeStub();

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcusruFields out = (OcusruFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ENTER_ID);
        verify(appService).returnTransid(eq(TRAN_ID), any(), eq(COMMAREA_LENGTH));
    }

    @Test
    void mainLine_firstEnterContextZero_sendsInitialScreenInsteadOfDispatch() {
        givenPseudoConversation(0, null, null);
        givenEibrespTracksMutableState();
        givenFormatTimeStub();

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(false));
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_xctlsToAdminMenuAndResetsContext() {
        givenPseudoConversation(1, "0", null);
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
        verify(appService, never()).receiveMap(anyString(), any());

        OcusruFields decoded = decode(commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo(PGM_NAME);
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo(TRAN_ID);
        assertEquals(0, decoded.getCaPgmContext());
    }

    @Test
    void mainLine_pf4Pressed_reSendsInitialScreen() {
        givenPseudoConversation(1, "1", "USER0001");
        givenEibrespTracksMutableState();
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcusruFields out = (OcusruFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ENTER_ID);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    @Test
    void mainLine_unmappedKeyPressed_sendsInitialScreenThenInvalidKeyDataOnly() {
        givenPseudoConversation(1, "0", null);
        givenEibrespTracksMutableState();
        givenFormatTimeStub();
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(false));
        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME),
                        dataOnlyOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OcusruFields out = (OcusruFields) dataOnlyOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
    }

    // ───────────────────────── 2100/2200 — stage 0: fetch user ─────────────────────────

    @Test
    void mainLine_enterPressed_blankUserId_showsRequiredMessageWithoutReading() {
        givenPseudoConversation(1, "0", null);
        givenEibrespTracksMutableState();
        givenFormatTimeStub();
        givenReceivedMap(null, null, null, null, null);
        when(appService.getEibaid()).thenReturn("'");

        service.mainLine(appService);

        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusruFields out = (OcusruFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
    }

    @Test
    void mainLine_enterPressed_userIdNotFound_showsUserNotFoundMessage() {
        givenPseudoConversation(1, "0", null);
        givenEibrespTracksMutableState();
        givenFormatTimeStub();
        givenReceivedMap("USER9999", null, null, null, null);
        givenReadFileResp(13, null, null, null, null);
        when(appService.getEibaid()).thenReturn("'");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusruFields out = (OcusruFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOT_FOUND);
    }

    @Test
    void mainLine_enterPressed_userFound_showsDetailsAndAdvancesToUpdateStage() {
        givenPseudoConversation(1, "0", null);
        givenEibrespTracksMutableState();
        givenFormatTimeStub();
        givenReceivedMap("USER0001", null, null, null, null);
        givenReadFileResp(0, "JOHN", "DOE", "SECRET1", "A");
        when(appService.getEibaid()).thenReturn("'");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusruFields out = (OcusruFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_MODIFY);
        assertThat(out.getUserido().trim()).isEqualTo("USER0001");
        assertThat(out.getUsfnamo().trim()).isEqualTo("JOHN");
        assertThat(out.getUslnamo().trim()).isEqualTo("DOE");
        assertThat(out.getUspwdo().trim()).isEqualTo("SECRET1");
        assertThat(out.getUstypeo().trim()).isEqualTo("A");

        ArgumentCaptor<Object> commareaCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .returnTransid(eq(TRAN_ID), commareaCaptor.capture(), eq(COMMAREA_LENGTH));
        OcusruFields decoded = decode(commareaCaptor.getValue());
        assertThat(decoded.getString("CA-WORK-AREA").substring(0, 1)).isEqualTo("1");
    }

    /**
     * COBOL 3000-READ-USER sets ERR-FLG-ON + "Error reading user file." on an OTHER
     * (non-NORMAL/NOTFND) RESP code, but 2200-FETCH-USER only branches on REC-FOUND, so the
     * not-found path ("User not found.") silently wins regardless of the actual error. This is a
     * pre-existing COBOL quirk (ground truth), faithfully reproduced by fetchUserRecord, which
     * likewise branches solely on WS-FOUND-FLG == "Y".
     */
    @Test
    void mainLine_enterPressed_readErrorOnFetch_stillShowsUserNotFoundPerCobolBehavior() {
        givenPseudoConversation(1, "0", null);
        givenEibrespTracksMutableState();
        givenFormatTimeStub();
        givenReceivedMap("USER0001", null, null, null, null);
        givenReadFileResp(99, null, null, null, null);
        when(appService.getEibaid()).thenReturn("'");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusruFields out = (OcusruFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOT_FOUND);
    }

    // ───────────────────────── 2300/2310 — stage 1: validate + update ─────────────────────────

    @Test
    void mainLine_updateStage_blankFirstName_repopulatesAndShowsRequiredMessage() {
        givenPseudoConversation(1, "1", "USER0001");
        givenEibrespTracksMutableState();
        givenFormatTimeStub();
        givenReceivedMap("USER0001", null, "DOE", "SECRET1", "A");
        when(appService.getEibaid()).thenReturn("'");

        service.mainLine(appService);

        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusruFields out = (OcusruFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_FNAME_REQ);
        assertThat(out.getUserido().trim()).isEqualTo("USER0001");
    }

    @Test
    void mainLine_updateStage_blankLastName_showsLastNameRequiredMessage() {
        givenPseudoConversation(1, "1", "USER0001");
        givenEibrespTracksMutableState();
        givenFormatTimeStub();
        givenReceivedMap("USER0001", "JOHN", null, "SECRET1", "A");
        when(appService.getEibaid()).thenReturn("'");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusruFields out = (OcusruFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LNAME_REQ);
    }

    @Test
    void mainLine_updateStage_blankPassword_showsPasswordRequiredMessage() {
        givenPseudoConversation(1, "1", "USER0001");
        givenEibrespTracksMutableState();
        givenFormatTimeStub();
        givenReceivedMap("USER0001", "JOHN", "DOE", null, "A");
        when(appService.getEibaid()).thenReturn("'");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusruFields out = (OcusruFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PWD_REQ);
    }

    @Test
    void mainLine_updateStage_invalidType_showsTypeMustBeAdminOrUserMessage() {
        givenPseudoConversation(1, "1", "USER0001");
        givenEibrespTracksMutableState();
        givenFormatTimeStub();
        givenReceivedMap("USER0001", "JOHN", "DOE", "SECRET1", "Z");
        when(appService.getEibaid()).thenReturn("'");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusruFields out = (OcusruFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TYPE_BAD);
    }

    @Test
    void mainLine_updateStage_typeU_isAccepted() {
        givenPseudoConversation(1, "1", "USER0001");
        givenEibrespTracksMutableState();
        givenFormatTimeStub();
        givenReceivedMap("USER0001", "JOHN", "DOE", "SECRET1", "U");
        givenReadFileForUpdateResp(0);
        givenRewriteResp(0);
        when(appService.getEibaid()).thenReturn("'");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusruFields out = (OcusruFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_SUCCESS);
    }

    @Test
    void mainLine_updateStage_recordNoLongerExists_repopulatesAndShowsNoLongerExistsMessage() {
        givenPseudoConversation(1, "1", "USER0001");
        givenEibrespTracksMutableState();
        givenFormatTimeStub();
        givenReceivedMap("USER0001", "JOHN", "DOE", "SECRET1", "A");
        givenReadFileForUpdateResp(13);
        when(appService.getEibaid()).thenReturn("'");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusruFields out = (OcusruFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NO_LONGER);
        assertThat(out.getUsfnamo().trim()).isEqualTo("JOHN");
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void mainLine_updateStage_readForUpdateOtherError_showsErrorReadingForUpdateMessage() {
        givenPseudoConversation(1, "1", "USER0001");
        givenEibrespTracksMutableState();
        givenFormatTimeStub();
        givenReceivedMap("USER0001", "JOHN", "DOE", "SECRET1", "A");
        givenReadFileForUpdateResp(99);
        when(appService.getEibaid()).thenReturn("'");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusruFields out = (OcusruFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ERR_READ_UPD);
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void mainLine_updateStage_rewriteFails_showsErrorUpdatingUserFileMessage() {
        givenPseudoConversation(1, "1", "USER0001");
        givenEibrespTracksMutableState();
        givenFormatTimeStub();
        givenReceivedMap("USER0001", "JOHN", "DOE", "SECRET1", "A");
        givenReadFileForUpdateResp(0);
        givenRewriteResp(1);
        when(appService.getEibaid()).thenReturn("'");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusruFields out = (OcusruFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ERR_UPDATE);
    }

    @Test
    void mainLine_updateStage_success_rewritesRecordAndResetsToInitialStage() {
        givenPseudoConversation(1, "1", "USER0001");
        givenEibrespTracksMutableState();
        givenFormatTimeStub();
        givenReceivedMap("USER0001", "JANE", "ROE", "NEWPASS1", "A");
        givenReadFileForUpdateResp(0);
        givenRewriteResp(0);
        when(appService.getEibaid()).thenReturn("'");

        service.mainLine(appService);

        ArgumentCaptor<Object> rewriteCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService).rewriteFile(anyString(), rewriteCaptor.capture());
        OcusruFields rewritten = (OcusruFields) rewriteCaptor.getValue();
        assertThat(rewritten.getUsFirstName().trim()).isEqualTo("JANE");
        assertThat(rewritten.getUsLastName().trim()).isEqualTo("ROE");
        assertThat(rewritten.getUsPassword().trim()).isEqualTo("NEWPASS1");
        assertThat(rewritten.getUsType().trim()).isEqualTo("A");

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusruFields out = (OcusruFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_SUCCESS);

        ArgumentCaptor<Object> commareaCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .returnTransid(eq(TRAN_ID), commareaCaptor.capture(), eq(COMMAREA_LENGTH));
        OcusruFields decoded = decode(commareaCaptor.getValue());
        assertThat(decoded.getString("CA-WORK-AREA").substring(0, 1)).isEqualTo("0");
    }

    @Test
    void mainLine_updateStage_usesSavedUserIdAsKeyNotReceivedUserId() {
        // COBOL 3100-UPDATE-USER always reads under WS-SV-USERID, ignoring
        // whatever USERIDI currently holds on the update screen.
        givenPseudoConversation(1, "1", "USER0001");
        givenEibrespTracksMutableState();
        givenFormatTimeStub();
        givenReceivedMap("IGNOREME", "JANE", "ROE", "NEWPASS1", "A");
        givenReadFileForUpdateResp(0);
        givenRewriteResp(0);
        when(appService.getEibaid()).thenReturn("'");

        service.mainLine(appService);

        verify(appService).readFileForUpdate(anyString(), any(), eq("USER0001"), eq(0));
    }

    // ───────────────────────── 2100-HANDLE-ENTER stage fallback ─────────────────────────

    @Test
    void mainLine_unexpectedStageValue_fallsBackToInitialScreen() {
        givenPseudoConversation(1, "9", "USER0001");
        givenEibrespTracksMutableState();
        givenFormatTimeStub();
        givenReceivedMap("USER0001", null, null, null, null);
        when(appService.getEibaid()).thenReturn("'");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcusruFields out = (OcusruFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ENTER_ID);
    }
}

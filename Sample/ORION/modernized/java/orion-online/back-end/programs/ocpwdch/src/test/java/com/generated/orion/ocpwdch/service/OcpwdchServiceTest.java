package com.generated.orion.ocpwdch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppResp;
import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.ocpwdch.accessor.OcpwdchFields;
import com.generated.orion.ocpwdch.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

/**
 * Unit tests for OcpwdchService, generated from COBOL program OCPWDCH (ORION-CCMS online password
 * change), following the OCCUSTU skeleton. All business logic is private and exercised solely
 * through the public {@link OcpwdchService#mainLine(AppService)} entry point, driven by an {@link
 * AppService} mock that emulates CICS SEND/RECEIVE/READ(UPDATE)/ REWRITE/XCTL/RETURN.
 *
 * <p>Convert-gap check: the Java service mirrors every COBOL paragraph 1:1
 * (0000-MAIN..9000-RETURN), including the EVALUATE EIBAID key dispatch, the WS-FOUND-FLG record
 * lookup, and the CICS response code handling (NORMAL=0 vs any other RESP for READ UPDATE /
 * REWRITE). No behavioral divergence against the COBOL ground truth was found, so no CONVERT-GAP
 * test is included.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcpwdchServiceTest {

    private static final String MAP_NAME = "MPWDCHA";
    private static final String TRANID = "ORPW";
    private static final String PGMNAME = "OCPWDCH";
    private static final String MENU_PGM = "OCMENU";
    private static final String USRSEC = "USRSEC";
    private static final int COMMAREA_LENGTH = 692;

    private static final String MSG_PROMPT = "Enter user id, old and new password.";
    private static final String MSG_REQUIRED = "Please enter all required fields.";
    private static final String MSG_MISMATCH = "New password and confirm do not match.";
    private static final String MSG_NOTFND = "Record not found.";
    private static final String MSG_INCORRECT = "Current password is incorrect.";
    private static final String MSG_CHANGED = "Password changed successfully.";
    private static final String MSG_CHANGE_FAILED = "Password change failed.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String HDR_TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM";

    @Mock private AppService appService;

    private OcpwdchService service;

    @BeforeEach
    void setUp() {
        service = new OcpwdchService();
        lenient().when(appService.getEibresp()).thenReturn(AppResp.NORMAL);
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commarea(int context) {
        OcpwdchFields helper = new OcpwdchFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commarea(context));
    }

    /** Stubs receiveMap to populate the MPWDCHAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OcpwdchFields> populate) {
        doAnswer(
                        inv -> {
                            OcpwdchFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    private static final class UserRow {
        final String id;
        String password = "OLDPASS1";

        UserRow(String id) {
            this.id = id;
        }
    }

    /**
     * Simulates USRSEC READ UPDATE / REWRITE by US-ID over an in-memory map. Missing id -> NOTFND.
     */
    private void givenUserFile(List<UserRow> rows) {
        lenient()
                .doAnswer(
                        inv -> {
                            String ridfld = ((String) inv.getArgument(2)).trim();
                            OcpwdchFields into = inv.getArgument(1);
                            UserRow r =
                                    rows.stream()
                                            .filter(row -> row.id.equals(ridfld))
                                            .findFirst()
                                            .orElse(null);
                            if (r != null) {
                                into.setUsId(r.id);
                                into.setUsPassword(r.password);
                            }
                            when(appService.getEibresp())
                                    .thenReturn(r != null ? AppResp.NORMAL : AppResp.NOTFND);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(
                        argThat(s -> s.trim().equals(USRSEC)),
                        any(),
                        anyString(),
                        org.mockito.ArgumentMatchers.anyInt());

        lenient()
                .doAnswer(
                        inv -> {
                            when(appService.getEibresp()).thenReturn(AppResp.NORMAL);
                            return null;
                        })
                .when(appService)
                .rewriteFile(argThat(s -> s.trim().equals(USRSEC)), any());
    }

    private ArgumentCaptor<Object> captureDataOnlySend() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), captor.capture(), any(), eq(false), eq(false), eq(false));
        return captor;
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcpwdch() {
        assertEquals("OCPWDCH", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrpw() {
        assertEquals("ORPW", service.getTransId());
    }

    @Test
    void getButtonDefs_returnsThePfKeysDeclaredInCobol() {
        List<com.appruntime.ScreenResponse.ButtonDef> buttons = service.getButtonDefs();
        assertThat(buttons).isNotEmpty();
    }

    @Test
    void registerFsetFields_delegatesToBmsMetadataWithoutError() {
        com.appruntime.AppRunner runner =
                new com.appruntime.AppRunner(java.util.Map.of(), java.util.Map.of());
        service.registerFsetFields(runner);
    }

    @Test
    void getFieldMapping_forMpwdcha_returnsPopulatedMapping() {
        com.appruntime.FieldMapping mapping = service.getFieldMapping("MPWDCHA");
        assertThat(mapping).isNotNull();
    }

    @Test
    void getFieldMapping_forUnknownMap_returnsEmptyMapping() {
        assertThat(service.getFieldMapping("UNKNOWN").isEmpty()).isTrue();
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_xctlsToMenuThenSendsInitialScreen() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        verify(appService).xctl(argThat(s -> s.trim().equals(MENU_PGM)), isNull(), eq(0));
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcpwdchFields out = (OcpwdchFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo(TRANID);
        assertThat(out.getPgmnameo().trim()).isEqualTo(PGMNAME);
        assertThat(out.getTitleo().trim()).isEqualTo(HDR_TITLE);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-22");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        assertEquals(1, out.getCaPgmContext());
        verify(appService).returnTransid(eq(TRANID), any(), eq(COMMAREA_LENGTH));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(false));
        verify(appService, never()).xctl(anyString(), any(), org.mockito.ArgumentMatchers.anyInt());
        verify(appService).returnTransid(eq(TRANID), any(), eq(COMMAREA_LENGTH));
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToMenuWithCommarea() {
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

        OcpwdchFields decoded = new OcpwdchFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertEquals(0, decoded.getCaPgmContext());
    }

    @Test
    void mainLine_pf4Pressed_resendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcpwdchFields out = (OcpwdchFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        // 2000-PROCESS-INPUT's WHEN OTHER performs 1000-SEND-INITIAL (erase=true) then
        // 8100-SEND-DATAONLY (erase=false); both share the same mutable field accessor,
        // so only the final (dataonly) call's content reflects the invalid-key message.
        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(false));
        OcpwdchFields dataOnlyScreen = (OcpwdchFields) captureDataOnlySend().getValue();
        assertThat(dataOnlyScreen.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-CHANGE-PWD ─────────────────────────

    @Test
    void mainLine_enterKey_blankUserId_showsRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setUseridi("        "));

        service.mainLine(appService);

        OcpwdchFields out = (OcpwdchFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(appService, never())
                .readFileForUpdate(
                        anyString(), any(), anyString(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void mainLine_enterKey_lowValuesUserId_showsRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        // receiveMap left unstubbed: MPWDCHAI keeps low-values default state, matching
        // USERIDI = LOW-VALUES in the COBOL condition.

        service.mainLine(appService);

        OcpwdchFields out = (OcpwdchFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
    }

    @Test
    void mainLine_enterKey_newPasswordConfirmMismatch_showsMismatchMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setUseridi("USER0001");
                    f.setOldpwdi("OLDPASS1");
                    f.setNewpwdi("NEWPASS1");
                    f.setCfmpwdi("NEWPASS2");
                });

        service.mainLine(appService);

        OcpwdchFields out = (OcpwdchFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_MISMATCH);
        verify(appService, never())
                .readFileForUpdate(
                        anyString(), any(), anyString(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void mainLine_enterKey_userNotFound_showsNotFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setUseridi("NOUSER01");
                    f.setOldpwdi("OLDPASS1");
                    f.setNewpwdi("NEWPASS1");
                    f.setCfmpwdi("NEWPASS1");
                });
        givenUserFile(List.of());

        service.mainLine(appService);

        verify(appService)
                .readFileForUpdate(
                        argThat(s -> s.trim().equals(USRSEC)),
                        any(),
                        eq("NOUSER01"),
                        org.mockito.ArgumentMatchers.anyInt());
        OcpwdchFields out = (OcpwdchFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOTFND);
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void mainLine_enterKey_oldPasswordIncorrect_showsIncorrectMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setUseridi("USER0001");
                    f.setOldpwdi("WRONGPWD");
                    f.setNewpwdi("NEWPASS1");
                    f.setCfmpwdi("NEWPASS1");
                });
        givenUserFile(List.of(new UserRow("USER0001")));

        service.mainLine(appService);

        OcpwdchFields out = (OcpwdchFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INCORRECT);
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    // ───────────────────────── 3000-READ-USER / 4000-UPDATE-PWD ─────────────────────────

    @Test
    void mainLine_enterKey_allFieldsValid_rewritesPasswordAndShowsChangedMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setUseridi("USER0001");
                    f.setOldpwdi("OLDPASS1");
                    f.setNewpwdi("NEWPASS1");
                    f.setCfmpwdi("NEWPASS1");
                });
        givenUserFile(List.of(new UserRow("USER0001")));

        service.mainLine(appService);

        verify(appService)
                .readFileForUpdate(
                        argThat(s -> s.trim().equals(USRSEC)),
                        any(),
                        eq("USER0001"),
                        org.mockito.ArgumentMatchers.anyInt());
        ArgumentCaptor<Object> rewriteCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .rewriteFile(argThat(s -> s.trim().equals(USRSEC)), rewriteCaptor.capture());
        OcpwdchFields rewritten = (OcpwdchFields) rewriteCaptor.getValue();
        assertThat(rewritten.getUsPassword().trim()).isEqualTo("NEWPASS1");

        OcpwdchFields out = (OcpwdchFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CHANGED);
    }

    @Test
    void mainLine_enterKey_rewriteFails_showsChangeFailedMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setUseridi("USER0001");
                    f.setOldpwdi("OLDPASS1");
                    f.setNewpwdi("NEWPASS1");
                    f.setCfmpwdi("NEWPASS1");
                });
        givenUserFile(List.of(new UserRow("USER0001")));
        doAnswer(
                        inv -> {
                            when(appService.getEibresp()).thenReturn(AppResp.ERROR);
                            return null;
                        })
                .when(appService)
                .rewriteFile(argThat(s -> s.trim().equals(USRSEC)), any());

        service.mainLine(appService);

        OcpwdchFields out = (OcpwdchFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CHANGE_FAILED);
    }
}

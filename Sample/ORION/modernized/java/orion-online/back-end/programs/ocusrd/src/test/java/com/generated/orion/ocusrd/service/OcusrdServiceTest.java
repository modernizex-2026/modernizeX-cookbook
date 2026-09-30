package com.generated.orion.ocusrd.service;

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
import com.generated.orion.ocusrd.accessor.OcusrdFields;
import com.generated.orion.ocusrd.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OcusrdService, generated from COBOL program OCUSRD. All business logic is private
 * and exercised solely through the public {@link OcusrdService#mainLine(AppService)} entry point,
 * driven by an {@link AppService} mock that emulates CICS SEND/RECEIVE/READ/DELETE/XCTL/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcusrdServiceTest {

    private static final String MSG_PROMPT = "Enter a user id and press ENTER.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_REQUIRED = "Please enter all required fields.";
    private static final String MSG_NOT_FOUND = "User not found.";
    private static final String MSG_CONFIRM = "Type Y and press ENTER to delete this user.";
    private static final String MSG_DELETED = "User deleted successfully.";
    private static final String MSG_CANCELLED = "Delete cancelled.";
    private static final String MSG_NO_LONGER_EXISTS = "User no longer exists.";
    private static final String MSG_DELETE_ERROR = "Error deleting user file.";

    @Mock private AppService appService;

    private OcusrdService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OcusrdService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-23", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying CA-PGM-CONTEXT/CA-WORK-AREA. */
    private String commarea(int context, String workArea) {
        OcusrdFields helper = new OcusrdFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        if (workArea != null) {
            helper.setString("CA-WORK-AREA", workArea);
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

    /** Builds a WS-STATE-AREA snapshot (stage + saved userid) for CA-WORK-AREA. */
    private String stateArea(String stage, String svUserid) {
        OcusrdFields helper = new OcusrdFields(new WorkingStorage());
        helper.setString("WS-STAGE", stage);
        helper.setWsSvUserid(svUserid == null ? " " : svUserid);
        return helper.groupToString("WS-STATE-AREA");
    }

    /** Stubs receiveMap to populate the MUSRDAI input fields captured from production code. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OcusrdFields> populate) {
        doAnswer(
                        inv -> {
                            OcusrdFields f = inv.getArgument(1);
                            populate.accept(f);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MUSRDA"), any());
    }

    /** Stubs readFile to succeed (resp 0) with USER-REC populated, or fail with the given resp. */
    private void givenReadUserReturns(
            int resp, java.util.function.Consumer<OcusrdFields> populate) {
        doAnswer(
                        inv -> {
                            if (populate != null) {
                                OcusrdFields into = inv.getArgument(1);
                                populate.accept(into);
                            }
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .readFile(anyString(), any(), anyString(), eq(0));
    }

    /** Stubs deleteFile with the given resp code. */
    private void givenDeleteUserReturns(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .deleteFile(anyString(), anyString(), eq(0));
    }

    private ArgumentCaptor<Object> captureSendMap(int times, boolean erase) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(times))
                .sendMap(
                        eq("MUSRDA"),
                        captor.capture(),
                        any(),
                        eq(erase),
                        anyBoolean(),
                        anyBoolean());
        return captor;
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        OcusrdFields out = (OcusrdFields) captureSendMap(1, true).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORUD");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCUSRD");
        verify(appService).returnTransid(eq("ORUD"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalContextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService)
                .sendMap(eq("MUSRDA"), any(), any(), eq(true), anyBoolean(), anyBoolean());
        verify(appService).returnTransid(eq("ORUD"), any(), eq(692));
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToAdminMenu() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        verify(appService)
                .xctl(
                        org.mockito.ArgumentMatchers.argThat(s -> s.trim().equals("OCADMEN")),
                        any(),
                        eq(692));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void mainLine_pf4Pressed_sendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        OcusrdFields out = (OcusrdFields) captureSendMap(1, true).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_unmappedKeyPressed_resendsInitialScreenThenInvalidKeyDataOnly() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        // COBOL: OTHER branch performs 1000-SEND-INITIAL (ERASE) then overwrites ERRMSGO and
        // performs a second 8100-SEND-DATAONLY -- two SEND MAPs occur for an unmapped key.
        verify(appService, times(1))
                .sendMap(eq("MUSRDA"), any(), any(), eq(true), anyBoolean(), anyBoolean());
        OcusrdFields secondSend = (OcusrdFields) captureSendMap(1, false).getValue();
        assertThat(secondSend.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100/2200 FETCH-USER (stage 0) ─────────────────────────

    @Test
    void mainLine_enterStageZeroBlankUserId_showsRequiredMessage() {
        givenPseudoConversation(1, stateArea("0", null));
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setUseridi("        "));

        service.mainLine(appService);

        OcusrdFields out = (OcusrdFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterStageZeroUserFound_showsUserDetailsAndAdvancesStage() {
        givenPseudoConversation(1, stateArea("0", null));
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setUseridi("USER0001"));
        givenReadUserReturns(
                0,
                f -> {
                    f.setUsId("USER0001");
                    f.setUsFirstName("JOHN Q");
                    f.setUsLastName("DOE SMITH");
                });

        service.mainLine(appService);

        OcusrdFields out = (OcusrdFields) captureSendMap(1, false).getValue();
        assertThat(out.getUserido().trim()).isEqualTo("USER0001");
        // 2220-BUILD-NAME: STRING ... DELIMITED BY SPACE takes only the first token of each part.
        assertThat(out.getUsnameo().trim()).isEqualTo("JOHN DOE");
        assertThat(out.getUsconfo().trim()).isEmpty();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CONFIRM);
    }

    @Test
    void mainLine_enterStageZeroUserNotFound_showsNotFoundMessage() {
        givenPseudoConversation(1, stateArea("0", null));
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setUseridi("NOSUCH01"));
        givenReadUserReturns(13, null);

        service.mainLine(appService);

        OcusrdFields out = (OcusrdFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOT_FOUND);
    }

    @Test
    void
            mainLine_enterStageZeroReadFileError_showsNotFoundBecauseFetchUserOverwritesErrorMessage() {
        givenPseudoConversation(1, stateArea("0", null));
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setUseridi("USER0001"));
        givenReadUserReturns(99, null);

        service.mainLine(appService);

        // COBOL ground truth: 3000-READ-USER's OTHER branch sets ERR-FLG-ON and moves
        // MSG_READ_ERROR into ERRMSGO, but never sets REC-FOUND. Back in 2200-FETCH-USER,
        // "IF REC-FOUND" is still false (REC-NOT-FOUND was set at the top of 3000-READ-USER),
        // so the ELSE branch unconditionally overwrites ERRMSGO with 'User not found.' --
        // the read-error message is set then immediately clobbered. This is faithfully
        // reproduced by the Java conversion (WS-FOUND-FLG stays "N" for both NOTFND and OTHER).
        OcusrdFields out = (OcusrdFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOT_FOUND);
    }

    // ───────────────────────── 2300 CONFIRM-DELETE (stage 1) ─────────────────────────

    @Test
    void mainLine_enterStageOneConfirmY_deletesSuccessfully() {
        givenPseudoConversation(1, stateArea("1", "USER0001"));
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setUsconfi("Y"));
        givenDeleteUserReturns(0);

        service.mainLine(appService);

        OcusrdFields out = (OcusrdFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_DELETED);
        verify(appService).deleteFile(anyString(), eq("USER0001"), eq(0));
    }

    @Test
    void mainLine_enterStageOneConfirmLowercaseY_deletesSuccessfully() {
        givenPseudoConversation(1, stateArea("1", "USER0001"));
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setUsconfi("y"));
        givenDeleteUserReturns(0);

        service.mainLine(appService);

        OcusrdFields out = (OcusrdFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_DELETED);
    }

    @Test
    void mainLine_enterStageOneConfirmYButRecordGone_showsNoLongerExistsAndRedisplays() {
        givenPseudoConversation(1, stateArea("1", "USER0001"));
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setUsconfi("Y"));
        givenDeleteUserReturns(13);

        service.mainLine(appService);

        OcusrdFields out = (OcusrdFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NO_LONGER_EXISTS);
        assertThat(out.getUserido().trim()).isEqualTo("USER0001");
    }

    @Test
    void mainLine_enterStageOneConfirmYButDeleteError_showsDeleteErrorAndRedisplays() {
        givenPseudoConversation(1, stateArea("1", "USER0001"));
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setUsconfi("Y"));
        givenDeleteUserReturns(99);

        service.mainLine(appService);

        OcusrdFields out = (OcusrdFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_DELETE_ERROR);
    }

    @Test
    void mainLine_enterStageOneConfirmOther_cancelsDelete() {
        givenPseudoConversation(1, stateArea("1", "USER0001"));
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setUsconfi("N"));

        service.mainLine(appService);

        OcusrdFields out = (OcusrdFields) captureSendMap(1, false).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CANCELLED);
        verify(appService, never()).deleteFile(anyString(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterUnknownStage_sendsInitialScreen() {
        givenPseudoConversation(1, stateArea("9", null));
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> {});

        service.mainLine(appService);

        OcusrdFields out = (OcusrdFields) captureSendMap(1, true).getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcusrd() {
        assertEquals("OCUSRD", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrud() {
        assertEquals("ORUD", service.getTransId());
    }

    @Test
    void getButtonDefs_delegatesToMetadataWithoutError() {
        assertNotNull(service.getButtonDefs());
    }

    @Test
    void getFieldMapping_delegatesToMetadataForKnownMap() {
        assertNotNull(service.getFieldMapping("MUSRDA"));
    }
}

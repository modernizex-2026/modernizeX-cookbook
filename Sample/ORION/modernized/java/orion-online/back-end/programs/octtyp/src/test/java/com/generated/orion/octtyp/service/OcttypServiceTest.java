package com.generated.orion.octtyp.service;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.FormatTimeResult;
import com.appruntime.ScreenResponse;
import com.generated.orion.octtyp.accessor.OcttypFields;
import com.generated.orion.octtyp.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OcttypService, generated from COBOL program OCTTYP. All business logic is private
 * and is exercised solely through the public {@link OcttypService#mainLine(AppService)} entry
 * point, driven by an {@link AppService} mock that emulates CICS
 * SEND/RECEIVE/READ/WRITE/REWRITE/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcttypServiceTest {

    private static final String MAP_NAME = "MTTYPA";

    private static final String MSG_PROMPT = "Enter a type code and press ENTER.";
    private static final String MSG_CD_REQ = "Type code is required.";
    private static final String MSG_DESC_REQ = "Description is required.";
    private static final String MSG_FOUND = "Type found. Change text, PF5 to update.";
    private static final String MSG_NEW = "New type. Enter text, PF5 to add.";
    private static final String MSG_KEY_FIRST = "Enter a type code and press ENTER first.";
    private static final String MSG_ADDED = "Transaction type added.";
    private static final String MSG_UPDATED = "Transaction type updated.";
    private static final String MSG_SAVE_ERR = "Error saving the transaction type.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";

    private static final String ST_KEY = "K";
    private static final String ST_EXIST = "E";
    private static final String ST_NEW = "N";

    @Mock private AppService appService;

    private OcttypService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OcttypService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying CA-PGM-CONTEXT and CA-WORK-AREA(1:1). */
    private String commarea(int context, String workArea) {
        OcttypFields helper = new OcttypFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        if (workArea != null) {
            helper.setCaWorkArea(workArea);
        }
        return helper.getOrionCommarea();
    }

    private void givenPseudoConversation(int context, String workArea) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commarea(context, workArea));
    }

    private void givenPseudoConversation(int context) {
        givenPseudoConversation(context, null);
    }

    private void givenReceiveMapPopulates(java.util.function.Consumer<OcttypFields> populate) {
        doAnswer(
                        inv -> {
                            OcttypFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OcttypFields out = (OcttypFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertEquals(-1, out.getTtcdl());
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORTT");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCTTYP");
        verify(appService).returnTransid(eq("ORTT"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        verify(appService).returnTransid(eq("ORTT"), any(), eq(692));
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToMenu() {
        givenPseudoConversation(1, ST_KEY);
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
    void mainLine_pf4Pressed_clearsAndResendsInitialScreen() {
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OcttypFields out = (OcttypFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcttypFields out = (OcttypFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-LOOKUP ─────────────────────────

    @Test
    void mainLine_lookup_blankTypeCode_rejectsWithCodeRequiredMessage() {
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setTtcdi(" "));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcttypFields out = (OcttypFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CD_REQ);
        assertEquals(-1, out.getTtcdl());
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_lookup_codeFound_showsExistingDescriptionAndMarksExist() {
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setTtcdi("AB"));
        doAnswer(
                        inv -> {
                            OcttypFields into = inv.getArgument(1);
                            into.setTtCd("AB");
                            into.setTtDesc("Existing Description");
                            nextResp.set(0); // found
                            return null;
                        })
                .when(appService)
                .readFile(anyString(), any(), anyString(), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcttypFields out = (OcttypFields) sendMapOut.getValue();
        assertThat(out.getTtcdo().trim()).isEqualTo("AB");
        assertThat(out.getTtdesco().trim()).isEqualTo("Existing Description");
        assertThat(out.getCaWorkArea()).startsWith(ST_EXIST);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_FOUND);
        assertEquals(-1, out.getTtdescl());
    }

    @Test
    void mainLine_lookup_codeNotFound_promptsForNewDescriptionAndMarksNew() {
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setTtcdi("ZZ"));
        doAnswer(
                        inv -> {
                            nextResp.set(13); // not found
                            return null;
                        })
                .when(appService)
                .readFile(anyString(), any(), anyString(), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcttypFields out = (OcttypFields) sendMapOut.getValue();
        assertThat(out.getTtcdo().trim()).isEqualTo("ZZ");
        assertThat(out.getTtdesco().trim()).isEmpty();
        assertThat(out.getCaWorkArea()).startsWith(ST_NEW);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NEW);
    }

    @Test
    void mainLine_lookup_fileReadUnrecoverableError_sendsAbendText() {
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setTtcdi("AB"));
        doAnswer(
                        inv -> {
                            nextResp.set(99); // unrecoverable error
                            return null;
                        })
                .when(appService)
                .readFile(anyString(), any(), anyString(), anyInt());

        service.mainLine(appService);

        verify(appService).sendText(anyString(), eq(true), eq(true));
        verify(appService).returnProgram();
    }

    @Test
    void mainLine_lookup_fileReadUnrecoverableError_convertGap_sendTextShouldCarryMessageText() {
        // CONVERT-GAP: COBOL's 9500-ABEND-RTN does EXEC CICS SEND TEXT FROM(WS-MSG-TEXT),
        // i.e. it sends the literal error message text. The converted abendUnrecoverableFileError()
        // instead calls ctx.appService.sendText(String.valueOf(ctx.f), true, true), which
        // stringifies the WHOLE OcttypFields accessor object (default Object.toString(), e.g.
        // "com.generated...OcttypFields@1a2b3c") instead of ctx.f.getWsMsgText(). Expected per
        // COBOL ground truth: the text sent should contain the WS-MSG-TEXT message. This test
        // intentionally fails against current Java to flag the gap — it is not a bug in the test.
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setTtcdi("AB"));
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .readFile(anyString(), any(), anyString(), anyInt());

        service.mainLine(appService);

        verify(appService)
                .sendText(
                        org.mockito.ArgumentMatchers.contains("unrecoverable file error"),
                        eq(true),
                        eq(true));
    }

    @Test
    void mainLine_lookup_fileReadUnrecoverableError_convertGap_returnTransidShouldNotFollowAbend() {
        // CONVERT-GAP: in COBOL, 9500-ABEND-RTN issues EXEC CICS RETURN (no TRANSID), which
        // ends the pseudo-conversational task immediately — 9000-RETURN is never reached
        // afterward. The converted abendUnrecoverableFileError() calls appService.returnProgram()
        // but does not stop the Java call stack: readTicketTypeRecord() returns, lookupTicketType()
        // falls through into the not-found branch, calls sendDataOnlyScreen(), and runMainLine()
        // still calls returnTransid() at the end. Expected (per COBOL ground truth): returnTransid
        // must NOT be called after an abend. This test intentionally fails against current Java
        // to flag the gap — it is not a bug in the test.
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setTtcdi("AB"));
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .readFile(anyString(), any(), anyString(), anyInt());

        service.mainLine(appService);

        verify(appService, never()).returnTransid(anyString(), any(), anyInt());
    }

    // ───────────────────────── 2300-SAVE ─────────────────────────

    @Test
    void mainLine_save_noKeyLookedUpYet_rejectsWithKeyFirstMessage() {
        givenPseudoConversation(1, ST_KEY); // 'K' — neither Exist nor New
        when(appService.getEibaid()).thenReturn("5");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcttypFields out = (OcttypFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_KEY_FIRST);
        assertEquals(-1, out.getTtcdl());
        verify(appService, never()).receiveMap(anyString(), any());
    }

    @Test
    void mainLine_save_blankDescription_rejectsWithDescriptionRequiredMessage() {
        givenPseudoConversation(1, ST_NEW);
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(f -> f.setTtdesci(" "));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcttypFields out = (OcttypFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_DESC_REQ);
        assertEquals(-1, out.getTtdescl());
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void mainLine_save_existingType_updateSucceeds_showsUpdatedMessage() {
        givenPseudoConversation(1, ST_EXIST);
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(f -> f.setTtdesci("New Description"));
        doAnswer(
                        inv -> {
                            nextResp.set(0); // read-for-update ok
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0); // rewrite ok
                            return null;
                        })
                .when(appService)
                .rewriteFile(anyString(), any());

        service.mainLine(appService);

        ArgumentCaptor<Object> rewritten = ArgumentCaptor.forClass(Object.class);
        verify(appService).rewriteFile(anyString(), rewritten.capture());
        OcttypFields rec = (OcttypFields) rewritten.getValue();
        assertThat(rec.getTtDesc().trim()).isEqualTo("New Description");

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcttypFields out = (OcttypFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_UPDATED);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_save_existingType_rewriteFails_showsSaveErrorMessage() {
        givenPseudoConversation(1, ST_EXIST);
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(f -> f.setTtdesci("New Description"));
        doAnswer(
                        inv -> {
                            nextResp.set(0); // read-for-update ok
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(99); // rewrite fails
                            return null;
                        })
                .when(appService)
                .rewriteFile(anyString(), any());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcttypFields out = (OcttypFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_SAVE_ERR);
    }

    @Test
    void mainLine_save_existingType_recordDeletedSinceLookup_fallsBackToAdd() {
        givenPseudoConversation(1, ST_EXIST);
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(f -> f.setTtdesci("New Description"));
        doAnswer(
                        inv -> {
                            nextResp.set(13); // not found on read-for-update -> fall back to add
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0); // write ok
                            return null;
                        })
                .when(appService)
                .writeFile(anyString(), any(), anyString(), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> written = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeFile(anyString(), written.capture(), anyString(), anyInt());
        OcttypFields rec = (OcttypFields) written.getValue();
        assertThat(rec.getTtDesc().trim()).isEqualTo("New Description");

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcttypFields out = (OcttypFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ADDED);
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void mainLine_save_existingType_readForUpdateUnrecoverableError_sendsAbendText() {
        givenPseudoConversation(1, ST_EXIST);
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(f -> f.setTtdesci("New Description"));
        doAnswer(
                        inv -> {
                            nextResp.set(99); // unrecoverable error
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());

        service.mainLine(appService);

        verify(appService).sendText(anyString(), eq(true), eq(true));
        verify(appService).returnProgram();
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void mainLine_save_newType_addSucceeds_showsAddedMessageAndMarksExist() {
        givenPseudoConversation(1, ST_NEW);
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(f -> f.setTtdesci("Brand New Type"));
        doAnswer(
                        inv -> {
                            nextResp.set(0); // write ok
                            return null;
                        })
                .when(appService)
                .writeFile(anyString(), any(), anyString(), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> written = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeFile(anyString(), written.capture(), anyString(), anyInt());
        OcttypFields rec = (OcttypFields) written.getValue();
        assertThat(rec.getTtDesc().trim()).isEqualTo("Brand New Type");

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcttypFields out = (OcttypFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ADDED);
        assertThat(out.getCaWorkArea()).startsWith(ST_EXIST);
    }

    @Test
    void mainLine_save_newType_duplicateKeyOnWrite_showsSaveErrorMessage() {
        givenPseudoConversation(1, ST_NEW);
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(f -> f.setTtdesci("Brand New Type"));
        doAnswer(
                        inv -> {
                            nextResp.set(14); // DFHRESP(DUPREC)
                            return null;
                        })
                .when(appService)
                .writeFile(anyString(), any(), anyString(), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcttypFields out = (OcttypFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_SAVE_ERR);
    }

    @Test
    void mainLine_save_newType_unexpectedWriteError_showsSaveErrorMessage() {
        givenPseudoConversation(1, ST_NEW);
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(f -> f.setTtdesci("Brand New Type"));
        doAnswer(
                        inv -> {
                            nextResp.set(
                                    99); // unexpected error (not abend per 3600-ADD-TTYP -> WHEN
                            // OTHER just sets SAVE-ERR)
                            return null;
                        })
                .when(appService)
                .writeFile(anyString(), any(), anyString(), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcttypFields out = (OcttypFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_SAVE_ERR);
        verify(appService, never()).sendText(anyString(), anyBoolean(), anyBoolean());
    }

    // ───────────────────────── 2050-RECEIVE / MAPFAIL ─────────────────────────

    @Test
    void mainLine_lookup_receiveMapFail_treatsInputAsBlankAndFailsCodeValidation() {
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("'");
        nextResp.set(36); // DFHRESP(MAPFAIL)
        // No receiveMap stub: fields stay at their post-fillLowValues default (LOW-VALUES).

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcttypFields out = (OcttypFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CD_REQ);
    }

    // ───────────────────────── 7000-XCTL-MENU ─────────────────────────

    @Test
    void mainLine_pf3Pressed_resetsPgmContextToZeroBeforeTransfer() {
        givenPseudoConversation(1, ST_EXIST);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        ArgumentCaptor<Object> commareaOut = ArgumentCaptor.forClass(Object.class);
        verify(appService).xctl(anyString(), commareaOut.capture(), eq(692));
        OcttypFields sent = new OcttypFields(new WorkingStorage());
        sent.setOrionCommarea(
                com.generated.orion.common.infrastructure.Utility.groupToString(
                        commareaOut.getValue()));
        assertEquals(0, sent.getCaPgmContext());
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcttyp() {
        assertEquals("OCTTYP", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrtt() {
        assertEquals("ORTT", service.getTransId());
    }

    @Test
    void getButtonDefs_returnsMttypaKeyBindings() {
        List<ScreenResponse.ButtonDef> buttons = service.getButtonDefs();

        assertThat(buttons)
                .extracting(ScreenResponse.ButtonDef::getAidKey)
                .containsExactly("ENTER", "PF3", "PF4", "PF5");
    }

    @Test
    void getFieldMapping_mttypa_mapsTicketTypeFields() {
        FieldMapping mapping = service.getFieldMapping("MTTYPA");

        assertThat(mapping).isNotNull();
        assertThat(mapping.getDataInFields()).containsKeys("TTCD", "TTDESC");
        assertThat(mapping.getDataOutFields()).containsKeys("TTCD", "TTDESC");
    }

    @Test
    void getFieldMapping_unknownMap_returnsEmptyMapping() {
        FieldMapping mapping = service.getFieldMapping("UNKNOWN");

        assertThat(mapping).isNotNull();
        assertThat(mapping.isEmpty()).isTrue();
    }

    @Test
    void registerFsetFields_delegatesMttypaFieldsToRunner() {
        // Real AppRunner (not a Mockito mock): newer JDKs/bytebuddy cannot instrument this
        // concrete class reliably, and registerFsetFields/getFsetFields are plain in-memory
        // operations, so exercising the real object is both simpler and safer here.
        AppRunner runner = new AppRunner(java.util.Map.of(), java.util.Map.of());

        service.registerFsetFields(runner);

        assertThat(runner.getFsetFields("MTTYPA")).containsExactlyInAnyOrder("TTCD", "TTDESC");
    }
}

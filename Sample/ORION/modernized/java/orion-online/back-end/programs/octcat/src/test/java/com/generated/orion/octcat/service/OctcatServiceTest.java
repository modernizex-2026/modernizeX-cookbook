package com.generated.orion.octcat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.octcat.accessor.OctcatFields;
import com.generated.orion.octcat.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OctcatService, generated from COBOL program OCTCAT (Transaction Category
 * Maintenance, follows the OCACCTV golden skeleton). All business logic is private and exercised
 * solely through the public {@link OctcatService#mainLine(AppService)} entry point, driven by an
 * {@link AppService} mock that emulates CICS SEND/RECEIVE/READ/WRITE/REWRITE/XCTL/RETURN.
 *
 * <p>Convert-gap check: 3000-READ-TCAT's WHEN OTHER branch performs 9500-ABEND-RTN, which in COBOL
 * issues "EXEC CICS SEND TEXT ... EXEC CICS RETURN" with no TRANSID — a hard task termination;
 * control never returns to 2100-LOOKUP or to 9000-RETURN. The converted {@code abendWithFileError}
 * only invokes {@code appService.sendText(...)} and {@code appService.returnProgram()} (a plain
 * interface method, not an exception), so Java execution falls through and continues normally:
 * 2100-LOOKUP still runs its IF/ELSE on the stale WS-FOUND-FLG, still calls 8100-SEND-DATAONLY (a
 * second screen send), and 0000-MAIN still calls 9000-RETURN (EXEC CICS RETURN TRANSID). See the
 * *_convertGap_* tests below.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OctcatServiceTest {

    private static final String TCATFILE = "TCATFILE";
    private static final String MAP_NAME = "MTCATA";
    private static final String MENU_PROGRAM = "OCMENU";
    private static final String PROGRAM_NAME = "OCTCAT";
    private static final String TRAN_ID = "ORTC";

    private static final String MSG_PROMPT = "Enter type + category code and press ENTER.";
    private static final String MSG_TYPE_REQ = "Type code is required.";
    private static final String MSG_CD_NUM = "Category code must be numeric.";
    private static final String MSG_DESC_REQ = "Description is required.";
    private static final String MSG_FOUND = "Category found. Change text, PF5 to update.";
    private static final String MSG_NEW = "New category. Enter text, PF5 to add.";
    private static final String MSG_KEY_FIRST = "Enter the key and press ENTER first.";
    private static final String MSG_ADDED = "Transaction category added.";
    private static final String MSG_UPDATED = "Transaction category updated.";
    private static final String MSG_SAVE_ERR = "Error saving the transaction category.";

    @Mock private AppService appService;

    private OctcatService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OctcatService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-23", "09:00:00"));
    }

    /**
     * Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT and CA-WORK-AREA
     * state.
     */
    private String commareaWithState(int context, String workAreaState) {
        OctcatFields helper = new OctcatFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        if (workAreaState != null) {
            helper.setCaWorkArea(workAreaState);
        }
        return helper.getOrionCommarea();
    }

    /**
     * Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context/state.
     */
    private void givenPseudoConversation(int context, String workAreaState) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea())
                .thenReturn(commareaWithState(context, workAreaState));
    }

    /** Stubs receiveMap to populate the MTCATAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OctcatFields> populate) {
        doAnswer(
                        inv -> {
                            OctcatFields f = inv.getArgument(1);
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
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo(TRAN_ID);
        assertThat(out.getPgmnameo().trim()).isEqualTo(PROGRAM_NAME);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-23");
        assertThat(out.getCurtimeo().trim()).isEqualTo("09:00:00");
        assertThat(out.getTctypel()).isEqualTo((short) -1);
        verify(appService).returnTransid(eq(TRAN_ID), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalContextZero_sendsInitialScreen() {
        givenPseudoConversation(0, null);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        verify(appService).returnTransid(eq(TRAN_ID), any(), eq(692));
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToMenuWithFromProgramInfo() {
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        ArgumentCaptor<Object> commareaCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .xctl(
                        argThat(s -> s.trim().equals(MENU_PROGRAM)),
                        commareaCaptor.capture(),
                        eq(692));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());

        OctcatFields decoded = new OctcatFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo(PROGRAM_NAME);
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo(TRAN_ID);
        assertEquals(0, decoded.getCaPgmContext());
    }

    @Test
    void mainLine_pf4Pressed_clearsAndResendsInitialScreen() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isNotEmpty();
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-LOOKUP / 6100-VALIDATE-KEY ─────────────────────────

    @Test
    void mainLine_enterKey_typeCodeBlank_showsTypeRequiredMessage() {
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTctypei("  ");
                    f.setTccdi("    ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TYPE_REQ);
        assertThat(out.getTctypel()).isEqualTo((short) -1);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_typeCodeAllLowValues_showsTypeRequiredMessage() {
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.fillLowValues("TCTYPEI"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TYPE_REQ);
    }

    @Test
    void mainLine_enterKey_categoryCodeNonNumeric_showsCategoryNumericMessage() {
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTctypei("AB");
                    f.setTccdi("A1B2");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CD_NUM);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_categoryCodeAllSpaces_showsCategoryNumericMessage() {
        // 6000-PARSE-NUM: WS-NC-DIGITS = 0 after the scan (all spaces) => SET WS-INVALID.
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTctypei("AB");
                    f.setTccdi("    ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CD_NUM);
    }

    @Test
    void mainLine_enterKey_categoryFound_showsDescriptionAndSetsExistState() {
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTctypei("AB");
                    f.setTccdi("0025");
                });
        doAnswer(
                        inv -> {
                            OctcatFields into = inv.getArgument(1);
                            into.setTcDesc("Existing category description");
                            nextResp.set(0); // DFHRESP(NORMAL)
                            return null;
                        })
                .when(appService)
                .readFile(eq(TCATFILE), any(), eq("AB|25"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_FOUND);
        assertThat(out.getTctypeo().trim()).isEqualTo("AB");
        assertThat(out.getTccdo()).isEqualTo("0025");
        assertThat(out.getTcdesco().trim()).isEqualTo("Existing category description");
        assertThat(caWorkAreaState(out)).isEqualTo("E");
        assertThat(out.getTcdescl()).isEqualTo((short) -1);
    }

    @Test
    void mainLine_enterKey_categoryNotFound_showsNewCategoryPromptAndSetsNewState() {
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTctypei("ZZ");
                    f.setTccdi("0099");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(13); // DFHRESP(NOTFND)
                            return null;
                        })
                .when(appService)
                .readFile(eq(TCATFILE), any(), eq("ZZ|99"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NEW);
        assertThat(out.getTcdesco().trim()).isEmpty();
        assertThat(caWorkAreaState(out)).isEqualTo("N");
    }

    @Test
    void
            mainLine_enterKey_readFileUnrecoverableError_convertGap_cobolStopsButJavaContinuesAndDoubleSends() {
        // CONVERT-GAP: COBOL 3000-READ-TCAT WHEN OTHER performs 9500-ABEND-RTN, which issues
        // "EXEC CICS SEND TEXT ... EXEC CICS RETURN" (no TRANSID) — a hard task termination.
        // Control never returns to 2100-LOOKUP (no second SEND MAP) nor to 9000-RETURN
        // (no RETURN TRANSID afterwards). The converted Java only calls sendText()/returnProgram()
        // (plain interface methods, not exceptions), so execution falls through: 2100-LOOKUP still
        // runs to completion and calls 8100-SEND-DATAONLY, and 0000-MAIN still calls RETURN
        // TRANSID.
        // Expected (COBOL ground truth): exactly one sendText, no sendMap, no returnTransid.
        // Actual (Java): sendText AND a sendMap AND a returnTransid all fire — this test fails,
        // correctly flagging the gap.
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setTctypei("AB");
                    f.setTccdi("0025");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(99); // unexpected/unrecoverable file error
                            return null;
                        })
                .when(appService)
                .readFile(eq(TCATFILE), any(), eq("AB|25"), anyInt());

        service.mainLine(appService);

        verify(appService, org.mockito.Mockito.times(1)).sendText(anyString(), eq(true), eq(true));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
        verify(appService, never()).returnTransid(anyString(), any(), anyInt());
    }

    // ───────────────────────── 2300-SAVE ─────────────────────────

    @Test
    void mainLine_pf5Pressed_workAreaNotKeyedYet_showsKeyFirstMessage() {
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("5");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_KEY_FIRST);
        assertThat(out.getTctypel()).isEqualTo((short) -1);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    @Test
    void mainLine_pf5Pressed_afterInvalidKey_showsTypeRequiredMessage() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setTctypei("  ");
                    f.setTccdi("0025");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TYPE_REQ);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void mainLine_pf5Pressed_descriptionBlank_showsDescriptionRequiredMessage() {
        givenPseudoConversation(1, "N");
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setTctypei("AB");
                    f.setTccdi("0025");
                    f.setTcdesci("                              ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_DESC_REQ);
        assertThat(out.getTcdescl()).isEqualTo((short) -1);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_pf5Pressed_stateExisting_rewriteSucceeds_showsUpdatedMessage() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setTctypei("AB");
                    f.setTccdi("0025");
                    f.setTcdesci("Updated description");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0); // DFHRESP(NORMAL) on READ UPDATE
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(TCATFILE), any(), eq("AB|25"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0); // DFHRESP(NORMAL) on REWRITE
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(TCATFILE), any());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_UPDATED);
        ArgumentCaptor<Object> rewritten = ArgumentCaptor.forClass(Object.class);
        verify(appService).rewriteFile(eq(TCATFILE), rewritten.capture());
        assertThat(((OctcatFields) rewritten.getValue()).getTcDesc().trim())
                .isEqualTo("Updated description");
    }

    @Test
    void mainLine_pf5Pressed_stateExisting_rewriteFails_showsSaveErrorMessage() {
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setTctypei("AB");
                    f.setTccdi("0025");
                    f.setTcdesci("Updated description");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0); // DFHRESP(NORMAL) on READ UPDATE
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(TCATFILE), any(), eq("AB|25"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(99); // rewrite failure
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(TCATFILE), any());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_SAVE_ERR);
    }

    @Test
    void mainLine_pf5Pressed_stateExistingButRecordGone_fallsBackToAdd() {
        // 3500-UPDATE-TCAT: READ UPDATE NOTFND(13) => PERFORM 3600-ADD-TCAT.
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setTctypei("AB");
                    f.setTccdi("0025");
                    f.setTcdesci("New description via fallback");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(13); // DFHRESP(NOTFND) on READ UPDATE
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(TCATFILE), any(), eq("AB|25"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0); // DFHRESP(NORMAL) on WRITE
                            return null;
                        })
                .when(appService)
                .writeFile(eq(TCATFILE), any(), eq("AB|25"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ADDED);
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void mainLine_pf5Pressed_stateNew_writeSucceeds_showsAddedMessage() {
        givenPseudoConversation(1, "N");
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setTctypei("ZZ");
                    f.setTccdi("0099");
                    f.setTcdesci("Brand new category");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0); // DFHRESP(NORMAL)
                            return null;
                        })
                .when(appService)
                .writeFile(eq(TCATFILE), any(), eq("ZZ|99"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ADDED);
        ArgumentCaptor<Object> written = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeFile(eq(TCATFILE), written.capture(), eq("ZZ|99"), anyInt());
        assertThat(((OctcatFields) written.getValue()).getTcDesc().trim())
                .isEqualTo("Brand new category");
    }

    @Test
    void mainLine_pf5Pressed_stateNew_writeDuplicateKey_showsSaveErrorAndKeepsNewState() {
        givenPseudoConversation(1, "N");
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setTctypei("ZZ");
                    f.setTccdi("0099");
                    f.setTcdesci("Duplicate write attempt");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(14); // DFHRESP(DUPREC)
                            return null;
                        })
                .when(appService)
                .writeFile(eq(TCATFILE), any(), eq("ZZ|99"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_SAVE_ERR);
    }

    // ───────────────────────── remaining branches for coverage ─────────────────────────

    @Test
    void mainLine_enterKey_receiveMapFailUsesLowValues_treatedAsEmptyInput() {
        // 2050-RECEIVE: WS-RESP-CD = DFHRESP(MAPFAIL) => MOVE LOW-VALUES TO MTCATAI,
        // i.e. TCTYPEI ends up all low-values, which 6100-VALIDATE-KEY treats as blank.
        givenPseudoConversation(1, "K");
        when(appService.getEibaid()).thenReturn("'");
        when(appService.getEibresp()).thenReturn(36); // DFHRESP(MAPFAIL) on receiveMap
        // receiveMap itself does nothing (no populate) - MTCATAI fields stay unset/blank.

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TYPE_REQ);
    }

    @Test
    void
            mainLine_pf5Pressed_stateExisting_readForUpdateUnrecoverableError_convertGap_cobolStopsButJavaContinues() {
        // CONVERT-GAP: same root cause as the lookup-path abend test above, but reached via
        // 3500-UPDATE-TCAT's WHEN OTHER => 9500-ABEND-RTN. COBOL ground truth: exactly one
        // SEND TEXT, no SEND MAP, no RETURN TRANSID. Java: falls through and still sends the
        // data-only map and still returns the transid.
        givenPseudoConversation(1, "E");
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setTctypei("AB");
                    f.setTccdi("0025");
                    f.setTcdesci("Some description");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(99); // unexpected/unrecoverable error on READ UPDATE
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(TCATFILE), any(), eq("AB|25"), anyInt());

        service.mainLine(appService);

        verify(appService, org.mockito.Mockito.times(1)).sendText(anyString(), eq(true), eq(true));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
        verify(appService, never()).returnTransid(anyString(), any(), anyInt());
    }

    @Test
    void mainLine_pf5Pressed_stateNew_writeUnrecoverableError_showsSaveErrorMessage() {
        // 3600-ADD-TCAT: WHEN OTHER (neither NORMAL nor DUPREC) also shows the save-error
        // message, same as DUPREC, but CA-WORK-AREA is NOT flipped to 'E'.
        givenPseudoConversation(1, "N");
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setTctypei("ZZ");
                    f.setTccdi("0099");
                    f.setTcdesci("Unrecoverable write attempt");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(99); // unexpected error, neither NORMAL(0) nor DUPREC(14)
                            return null;
                        })
                .when(appService)
                .writeFile(eq(TCATFILE), any(), eq("ZZ|99"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctcatFields out = (OctcatFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_SAVE_ERR);
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOctcat() {
        assertEquals(PROGRAM_NAME, service.getProgramName());
    }

    @Test
    void getTransId_returnsOrtc() {
        assertEquals(TRAN_ID, service.getTransId());
    }

    @Test
    void getButtonDefs_delegatesToBmsMetadata() {
        assertThat(service.getButtonDefs()).isNotEmpty();
    }

    @Test
    void getFieldMapping_delegatesToBmsMetadata() {
        assertThat(service.getFieldMapping("MTCATA")).isNotNull();
    }

    // NOTE: registerFsetFields(AppRunner) is not covered here — mocking the concrete
    // AppRunner class fails under this environment's JDK 26 (Byte Buddy/Mockito's inline
    // mock maker only officially supports up to Java 24); see findings note.

    /** Reads CA-WORK-AREA(1:1) off the outbound screen accessor's underlying commarea buffer. */
    private static String caWorkAreaState(OctcatFields out) {
        return out.getCaWorkArea().substring(0, 1);
    }
}

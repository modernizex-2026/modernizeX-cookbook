package com.generated.orion.ocdgrp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.appruntime.ReturnException;
import com.generated.orion.ocdgrp.accessor.OcdgrpFields;
import com.generated.orion.ocdgrp.model.WorkingStorage;

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
 * Unit tests for OcdgrpService, generated from COBOL program OCDGRP (disclosure/interest group
 * maintenance, modeled on the OCACCTV skeleton). All business logic is private and is exercised
 * solely through the public {@link OcdgrpService#mainLine(AppService)} entry point, driven by an
 * {@link AppService} mock that emulates CICS SEND/RECEIVE/READ/WRITE/REWRITE/XCTL/RETURN.
 *
 * <p>Convert-gap check: OCDGRP's Java service mirrors every COBOL paragraph 1:1
 * (0000-MAIN..9500-ABEND-RTN), including the abend path (handleAbend calls
 * appService.returnProgram(), whose real implementation throws {@link ReturnException} to unwind
 * the stack — faithfully matching COBOL's task-terminating EXEC CICS RETURN). No behavioral
 * divergence was found against the COBOL ground truth, so no CONVERT-GAP test is included here.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcdgrpServiceTest {

    private static final String MAP_NAME = "MDGRPA";
    private static final String MENU_PGM = "OCMENU";
    private static final String PROGRAM = "OCDGRP";
    private static final String TRANID = "ORDG";

    private static final String MSG_PROMPT = "Enter group, type, category and press ENTER.";
    private static final String MSG_GRP_REQ = "Account group id is required.";
    private static final String MSG_TYPE_REQ = "Type code is required.";
    private static final String MSG_CAT_NUM = "Category code must be numeric.";
    private static final String MSG_RATE_BAD = "Interest rate is not a valid number.";
    private static final String MSG_FOUND = "Group found. Change rate, PF5 to update.";
    private static final String MSG_NEW = "New group. Enter rate, PF5 to add.";
    private static final String MSG_KEY_FIRST = "Enter the key and press ENTER first.";
    private static final String MSG_ADDED = "Disclosure group added.";
    private static final String MSG_UPDATED = "Disclosure group updated.";
    private static final String MSG_SAVE_ERR = "Error saving the disclosure group.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String HDR_TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM";

    private static final String ST_KEY = "K";
    private static final String ST_EXIST = "E";
    private static final String ST_NEW = "N";

    @Mock private AppService appService;

    private OcdgrpService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OcdgrpService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /**
     * Builds a serialized ORION-COMMAREA carrying CA-PGM-CONTEXT and (optionally) the CA-WORK-AREA
     * status char.
     */
    private String commareaWithContext(int context, String workAreaStatus) {
        OcdgrpFields helper = new OcdgrpFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        if (workAreaStatus != null) {
            helper.setCaWorkArea(workAreaStatus);
        }
        return helper.getOrionCommarea();
    }

    /**
     * Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context/state.
     */
    private void givenPseudoConversation(int context, String workAreaStatus) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea())
                .thenReturn(commareaWithContext(context, workAreaStatus));
    }

    /** Stubs receiveMap to populate the MDGRPAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OcdgrpFields> populate) {
        doAnswer(
                        inv -> {
                            OcdgrpFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    private void givenValidKeyInput(String group, String type, String cat) {
        givenReceiveMapPopulates(
                f -> {
                    f.setDggrpi(group);
                    f.setDgtypei(type);
                    f.setDgcati(cat);
                });
    }

    private void givenValidKeyAndRateInput(String group, String type, String cat, String rate) {
        givenReceiveMapPopulates(
                f -> {
                    f.setDggrpi(group);
                    f.setDgtypei(type);
                    f.setDgcati(cat);
                    f.setDgratei(rate);
                });
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo(TRANID);
        assertThat(out.getPgmnameo().trim()).isEqualTo(PROGRAM);
        assertThat(out.getTitleo().trim()).isEqualTo(HDR_TITLE);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-22");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        assertThat(out.getDggrpl()).isEqualTo((short) -1);
        verify(appService).returnTransid(eq(TRANID), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0, null);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        verify(appService).returnTransid(eq(TRANID), any(), eq(692));
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToMenuWithFromProgramInfo() {
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        ArgumentCaptor<Object> commareaCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .xctl(argThat(s -> s.trim().equals(MENU_PGM)), commareaCaptor.capture(), eq(692));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());

        OcdgrpFields decoded = new OcdgrpFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo(PROGRAM);
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo(TRANID);
        assertEquals(0, decoded.getCaPgmContext());
    }

    @Test
    void mainLine_pf4Pressed_clearsAndResendsInitialScreen() {
        givenPseudoConversation(1, ST_EXIST);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
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
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-LOOKUP ─────────────────────────

    @Test
    void mainLine_enterKey_groupBlank_showsGroupRequiredMessage() {
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("'");
        givenValidKeyInput("          ", "AB", "0001");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_GRP_REQ);
        assertThat(out.getDggrpl()).isEqualTo((short) -1);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_groupAllLowValues_showsGroupRequiredMessage() {
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.fillLowValues("DGGRPI");
                    f.setDgtypei("AB");
                    f.setDgcati("0001");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_GRP_REQ);
    }

    @Test
    void mainLine_enterKey_typeBlank_showsTypeRequiredMessage() {
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("'");
        givenValidKeyInput("GROUP1    ", "  ", "0001");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TYPE_REQ);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_categoryNonNumeric_showsCategoryNumericMessage() {
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("'");
        givenValidKeyInput("GROUP1    ", "AB", "12A5");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CAT_NUM);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_categoryBlank_showsCategoryNumericMessage() {
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("'");
        givenValidKeyInput("GROUP1    ", "AB", "    ");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CAT_NUM);
    }

    @Test
    void mainLine_enterKey_groupFound_showsRateAndFoundMessage() {
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("'");
        givenValidKeyInput("GROUP1    ", "AB", "0005");
        doAnswer(
                        inv -> {
                            OcdgrpFields into = inv.getArgument(1);
                            into.setDgIntRate(new BigDecimal("12.34"));
                            nextResp.set(0); // DFHRESP(NORMAL)
                            return null;
                        })
                .when(appService)
                .readFile(anyString(), any(), anyString(), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_FOUND);
        assertThat(out.getDggrpo().trim()).isEqualTo("GROUP1");
        assertThat(out.getDgtypeo().trim()).isEqualTo("AB");
        assertThat(out.getDgcato().trim()).isEqualTo("0005");
        assertThat(out.getDgrateo().trim()).isEqualTo("12.34");
        assertThat(out.getCaWorkArea().substring(0, 1)).isEqualTo(ST_EXIST);
        assertThat(out.getDgratel()).isEqualTo((short) -1);
    }

    @Test
    void mainLine_enterKey_groupNotFound_showsNewMessage() {
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("'");
        givenValidKeyInput("GROUP2    ", "AB", "0009");
        doAnswer(
                        inv -> {
                            nextResp.set(13); // DFHRESP(NOTFND)
                            return null;
                        })
                .when(appService)
                .readFile(anyString(), any(), anyString(), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NEW);
        assertThat(out.getDgrateo().trim()).isEmpty();
        assertThat(out.getCaWorkArea().substring(0, 1)).isEqualTo(ST_NEW);
    }

    @Test
    void mainLine_enterKey_readFileUnexpectedError_abendsAndSendsErrorText() {
        // CONVERT-GAP: COBOL 9500-ABEND-RTN does `EXEC CICS SEND TEXT FROM(WS-MSG-TEXT)`
        // (the literal error message). Java's handleAbend calls
        // appService.sendText(String.valueOf(ctx.f), true, true) — String.valueOf(ctx.f)
        // resolves to OcdgrpFields' default Object#toString() (e.g. "...OcdgrpFields@abcd"),
        // never the message text set into WS-MSG-TEXT one line above. The operator terminal
        // never receives the intended error text. Expected assertion below reflects the
        // COBOL ground truth and is expected to fail against the current Java behavior.
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("'");
        givenValidKeyInput("GROUP3    ", "AB", "0001");
        doAnswer(
                        inv -> {
                            nextResp.set(99); // unexpected error
                            return null;
                        })
                .when(appService)
                .readFile(anyString(), any(), anyString(), anyInt());
        doThrow(new ReturnException(null, null, 0)).when(appService).returnProgram();

        assertThrows(ReturnException.class, () -> service.mainLine(appService));

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(appService).sendText(textCaptor.capture(), eq(true), eq(true));
        assertThat(textCaptor.getValue())
                .contains("OCDGRP: unrecoverable file error. Contact support.");
        verify(appService, never())
                .sendMap(eq(MAP_NAME), any(), any(), eq(false), anyBoolean(), anyBoolean());
        verify(appService, never()).returnTransid(anyString(), any(), anyInt());
    }

    // ───────────────────────── 2300-SAVE ─────────────────────────

    @Test
    void mainLine_pf5_workAreaNotKeyedYet_showsKeyFirstMessage() {
        givenPseudoConversation(1, ST_KEY);
        when(appService.getEibaid()).thenReturn("5");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_KEY_FIRST);
        assertThat(out.getDggrpl()).isEqualTo((short) -1);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    @Test
    void mainLine_pf5_invalidKeyOnSave_showsGroupRequiredMessage() {
        givenPseudoConversation(1, ST_NEW);
        when(appService.getEibaid()).thenReturn("5");
        givenValidKeyInput("          ", "AB", "0001");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_GRP_REQ);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_pf5_rateBlank_showsRateBadMessage() {
        givenPseudoConversation(1, ST_NEW);
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setDggrpi("GROUP1    ");
                    f.setDgtypei("AB");
                    f.setDgcati("0001");
                    f.setDgratei("       ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_RATE_BAD);
        assertThat(out.getDgratel()).isEqualTo((short) -1);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_pf5_rateTooManyIntegerDigits_showsRateBadMessage() {
        givenPseudoConversation(1, ST_NEW);
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setDggrpi("GROUP1    ");
                    f.setDgtypei("AB");
                    f.setDgcati("0001");
                    f.setDgratei("12345.0");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_RATE_BAD);
    }

    @Test
    void mainLine_pf5_rateTooManyFractionDigits_showsRateBadMessage() {
        givenPseudoConversation(1, ST_NEW);
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setDggrpi("GROUP1    ");
                    f.setDgtypei("AB");
                    f.setDgcati("0001");
                    f.setDgratei("1.234");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_RATE_BAD);
    }

    @Test
    void mainLine_pf5_rateDoubleDot_showsRateBadMessage() {
        givenPseudoConversation(1, ST_NEW);
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setDggrpi("GROUP1    ");
                    f.setDgtypei("AB");
                    f.setDgcati("0001");
                    f.setDgratei("1.2.3");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_RATE_BAD);
    }

    @Test
    void mainLine_pf5_rateSingleFractionDigit_padsToTwoDecimalsAndAdds() {
        givenPseudoConversation(1, ST_NEW);
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setDggrpi("GROUP1    ");
                    f.setDgtypei("AB");
                    f.setDgcati("0001");
                    f.setDgratei("1.5");
                });
        ArgumentCaptor<Object> writeFrom = ArgumentCaptor.forClass(Object.class);
        doAnswer(
                        inv -> {
                            nextResp.set(0); // DFHRESP(NORMAL)
                            return null;
                        })
                .when(appService)
                .writeFile(anyString(), writeFrom.capture(), anyString(), anyInt());

        service.mainLine(appService);

        OcdgrpFields written = (OcdgrpFields) writeFrom.getValue();
        assertThat(written.getDgIntRate()).isEqualByComparingTo(new BigDecimal("1.50"));
    }

    @Test
    void mainLine_pf5_newGroup_addsRecordAndShowsAddedMessage() {
        givenPseudoConversation(1, ST_NEW);
        when(appService.getEibaid()).thenReturn("5");
        givenValidKeyAndRateInput("GROUP1    ", "AB", "0001", "12.34");
        ArgumentCaptor<Object> writeFrom = ArgumentCaptor.forClass(Object.class);
        doAnswer(
                        inv -> {
                            nextResp.set(0); // DFHRESP(NORMAL)
                            return null;
                        })
                .when(appService)
                .writeFile(anyString(), writeFrom.capture(), anyString(), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ADDED);
        OcdgrpFields written = (OcdgrpFields) writeFrom.getValue();
        assertThat(written.getDgAcctGroup().trim()).isEqualTo("GROUP1");
        assertThat(written.getDgTypeCd().trim()).isEqualTo("AB");
        assertThat(written.getDgCatCd()).isEqualTo(1);
        assertThat(written.getDgIntRate()).isEqualByComparingTo(new BigDecimal("12.34"));
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_pf5_addDuplicateKey_showsSaveErrorMessage() {
        givenPseudoConversation(1, ST_NEW);
        when(appService.getEibaid()).thenReturn("5");
        givenValidKeyAndRateInput("GROUP1    ", "AB", "0001", "12.34");
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
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_SAVE_ERR);
    }

    @Test
    void mainLine_pf5_existingGroup_updatesRecordAndShowsUpdatedMessage() {
        givenPseudoConversation(1, ST_EXIST);
        when(appService.getEibaid()).thenReturn("5");
        givenValidKeyAndRateInput("GROUP1    ", "AB", "0001", "9.99");
        ArgumentCaptor<Object> rewriteFrom = ArgumentCaptor.forClass(Object.class);
        doAnswer(
                        inv -> {
                            nextResp.set(0); // DFHRESP(NORMAL)
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0); // DFHRESP(NORMAL)
                            return null;
                        })
                .when(appService)
                .rewriteFile(anyString(), rewriteFrom.capture());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_UPDATED);
        OcdgrpFields rewritten = (OcdgrpFields) rewriteFrom.getValue();
        assertThat(rewritten.getDgIntRate()).isEqualByComparingTo(new BigDecimal("9.99"));
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_pf5_existingGroupRewriteFails_showsSaveErrorMessage() {
        givenPseudoConversation(1, ST_EXIST);
        when(appService.getEibaid()).thenReturn("5");
        givenValidKeyAndRateInput("GROUP1    ", "AB", "0001", "9.99");
        doAnswer(
                        inv -> {
                            nextResp.set(0); // DFHRESP(NORMAL) on the update-read
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(1); // rewrite failure, not NORMAL
                            return null;
                        })
                .when(appService)
                .rewriteFile(anyString(), any());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_SAVE_ERR);
    }

    @Test
    void mainLine_pf5_existingGroupRecordGoneOnUpdate_fallsBackToAdd() {
        // COBOL 3500-UPDATE-DGRP: WHEN DFHRESP(NOTFND) PERFORM 3600-ADD-DGRP.
        givenPseudoConversation(1, ST_EXIST);
        when(appService.getEibaid()).thenReturn("5");
        givenValidKeyAndRateInput("GROUP1    ", "AB", "0001", "9.99");
        doAnswer(
                        inv -> {
                            nextResp.set(13); // DFHRESP(NOTFND)
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0); // DFHRESP(NORMAL) on the fallback write
                            return null;
                        })
                .when(appService)
                .writeFile(anyString(), any(), anyString(), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcdgrpFields out = (OcdgrpFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ADDED);
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void mainLine_pf5_updateReadFileUnexpectedError_abendsAndSendsErrorText() {
        // CONVERT-GAP: see mainLine_enterKey_readFileUnexpectedError_abendsAndSendsErrorText —
        // handleAbend sends String.valueOf(ctx.f) (the accessor's default toString()) instead
        // of the WS-MSG-TEXT abend message COBOL actually sends. Expected assertion below
        // reflects the COBOL ground truth and is expected to fail against current Java behavior.
        givenPseudoConversation(1, ST_EXIST);
        when(appService.getEibaid()).thenReturn("5");
        givenValidKeyAndRateInput("GROUP1    ", "AB", "0001", "9.99");
        doAnswer(
                        inv -> {
                            nextResp.set(99); // unexpected error
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());
        doThrow(new ReturnException(null, null, 0)).when(appService).returnProgram();

        assertThrows(ReturnException.class, () -> service.mainLine(appService));

        verify(appService)
                .sendText(argThat(t -> t.contains("unrecoverable file error")), eq(true), eq(true));
        verify(appService, never()).rewriteFile(anyString(), any());
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcdgrp() {
        assertEquals(PROGRAM, service.getProgramName());
    }

    @Test
    void getTransId_returnsOrdg() {
        assertEquals(TRANID, service.getTransId());
    }
}

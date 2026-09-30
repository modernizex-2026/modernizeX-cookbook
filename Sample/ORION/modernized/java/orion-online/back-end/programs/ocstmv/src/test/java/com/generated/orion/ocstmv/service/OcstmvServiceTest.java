package com.generated.orion.ocstmv.service;

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
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.ocstmv.accessor.OcstmvFields;
import com.generated.orion.ocstmv.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Unit tests for OcstmvService, generated from COBOL program OCSTMV (ORION-CCMS "Statement View",
 * following the OCACCTV golden on-line skeleton). All business logic is private and is exercised
 * solely through the public {@link OcstmvService#mainLine(AppService)} entry point, driven by an
 * {@link AppService} mock that emulates CICS SEND/RECEIVE/READ/XCTL/RETURN.
 *
 * <p>Convert-gap check: OCSTMV's Java service mirrors every COBOL paragraph 1:1
 * (0000-MAIN..9500-ABEND-RTN) with one exception. COBOL's 9500-ABEND-RTN does {@code EXEC CICS SEND
 * TEXT FROM(WS-MSG-TEXT)}, i.e. it sends the specific message text "OCSTMV: unrecoverable file
 * error. Contact support." that was just moved into WS-MSG-TEXT. The converted {@code
 * _9500AbendRtn} instead calls {@code appService.sendText(String.valueOf(ctx.f), true, true)} —
 * {@code ctx.f} is the whole {@code OcstmvFields} accessor object, not the WS-MSG-TEXT field, so
 * {@code String.valueOf(ctx.f)} yields the default {@code Object.toString()} (e.g.
 * "OcstmvFields@612af486") and the operator never actually sees the error message. See the
 * CONVERT-GAP test below, which is expected to fail against the COBOL ground truth until fixed
 * (e.g. by changing the call to {@code ctx.f.getWsMsgText()}). {@code returnProgram()} throws
 * {@link ReturnException} in production to unwind the stack, faithfully matching COBOL's
 * task-terminating EXEC CICS RETURN — that part is not a gap.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcstmvServiceTest {

    private static final String MAP_NAME = "MSTMVA";
    private static final String MENU_PGM = "OCMENU";
    private static final String PROGRAM = "OCSTMV";
    private static final String TRANID = "ORSV";
    private static final int COMMAREA_LEN = 692;

    private static final String MSG_PROMPT = "Enter account id and cycle, press ENTER.";
    private static final String MSG_ACCT_NUM = "Account id must be numeric.";
    private static final String MSG_CYC_NUM = "Cycle must be six digits (YYYYMM).";
    private static final String MSG_CYC_MM = "Cycle month must be 01 through 12.";
    private static final String MSG_NOTFND = "No statement for that account and cycle.";
    private static final String MSG_SHOWN = "Statement displayed.";

    @Mock private AppService appService;

    private OcstmvService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OcstmvService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /**
     * Reads a field's freshly-initialized (copybook VALUE clause) default via a scratch accessor.
     */
    private static String defaultOf(Function<OcstmvFields, String> getter) {
        return getter.apply(new OcstmvFields(new WorkingStorage()));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commareaWithContext(int context) {
        OcstmvFields helper = new OcstmvFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(context));
    }

    /** Stubs receiveMap to populate the MSTMVAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OcstmvFields> populate) {
        doAnswer(
                        inv -> {
                            OcstmvFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    /**
     * Replicates COBOL's "MOVE ST-xxx-BAL TO WS-ED-BAL; MOVE WS-ED-BAL(2:15) TO STxxxO" MOVE chain
     * (4000-POPULATE-DETAIL) on an isolated accessor, so the expected string is derived the same
     * way production computes it (drop the 1-byte leading edit position).
     */
    private static String editedBalance(BigDecimal amount) {
        OcstmvFields helper = new OcstmvFields(new WorkingStorage());
        helper.setWsEdBal(amount);
        return Utility.padRight(helper.getString("WS-ED-BAL"), 16).substring(1, 16);
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OcstmvFields out = (OcstmvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo(TRANID);
        assertThat(out.getPgmnameo().trim()).isEqualTo(PROGRAM);
        assertThat(out.getTitleo().trim()).isEqualTo(defaultOf(OcstmvFields::getWsHdrTitle).trim());
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-22");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        assertEquals(-1, out.getAcctidl());
        verify(appService).returnTransid(eq(TRANID), any(), eq(COMMAREA_LEN));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        verify(appService).returnTransid(eq(TRANID), any(), eq(COMMAREA_LEN));
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToMenuWithFromProgramInfo() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        ArgumentCaptor<Object> commareaCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .xctl(
                        argThat(s -> s.trim().equals(MENU_PGM)),
                        commareaCaptor.capture(),
                        eq(COMMAREA_LEN));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());

        OcstmvFields decoded = new OcstmvFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo(PROGRAM);
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo(TRANID);
        assertEquals(0, decoded.getCaPgmContext());
    }

    @Test
    void mainLine_pf4Pressed_clearsAndResendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OcstmvFields out = (OcstmvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), dataOnlyOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstmvFields dataOnlyScreen = (OcstmvFields) dataOnlyOut.getValue();
        assertThat(dataOnlyScreen.getErrmsgo().trim())
                .isEqualTo(defaultOf(OcstmvFields::getWsMsgInvalidKey).trim());
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-VIEW-STMT / 6100-VALIDATE-KEY / 6000-PARSE-NUM
    // ─────────────────────────

    @Test
    void mainLine_enterKey_mapfailResp_treatsInputAsLowValues_showsAcctNumMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        when(appService.getEibresp())
                .thenAnswer(inv -> 36); // DFHRESP(MAPFAIL) on the RECEIVE itself
        givenReceiveMapPopulates(f -> f.setAcctidi("00000012345"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstmvFields out = (OcstmvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ACCT_NUM);
        assertEquals(-1, out.getAcctidl());
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_accountIdBlank_showsAcctNumMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("           ");
                    f.setStcyci("202506");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstmvFields out = (OcstmvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ACCT_NUM);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_accountIdNonNumeric_showsAcctNumMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("1234ABCDE12");
                    f.setStcyci("202506");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstmvFields out = (OcstmvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ACCT_NUM);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_cycleNonNumeric_showsCycNumMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("00000012345");
                    f.setStcyci("2025AB");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstmvFields out = (OcstmvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CYC_NUM);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_cycleFewerThanSixDigits_showsCycNumMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("00000012345");
                    f.setStcyci("  2506");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstmvFields out = (OcstmvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CYC_NUM);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_cycleMonthOutOfRange_showsCycMmMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("00000012345");
                    f.setStcyci("202513"); // month 13 - out of range
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstmvFields out = (OcstmvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CYC_MM);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_cycleMonthZero_showsCycMmMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("00000012345");
                    f.setStcyci("202500"); // month 00 - out of range
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstmvFields out = (OcstmvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CYC_MM);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    // ───────────────────────── 3000-READ-STMT / 4000-POPULATE-DETAIL / 4100-CLEAR-DETAIL
    // ─────────────────────────

    @Test
    void mainLine_enterKey_validKeyStatementFound_populatesDetailAndShowsShownMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("00000012345");
                    f.setStcyci("202506");
                });
        BigDecimal openBal = new BigDecimal("1234.56");
        BigDecimal closeBal = new BigDecimal("1450.00");
        BigDecimal minDue = new BigDecimal("35.00");
        doAnswer(
                        inv -> {
                            OcstmvFields into = inv.getArgument(1);
                            into.setStAcctId(12345L);
                            into.setStCycle(202506);
                            into.setStOpenBal(openBal);
                            into.setStCloseBal(closeBal);
                            into.setStMinDue(minDue);
                            into.setStDueDate("2026-06-25");
                            nextResp.set(0); // DFHRESP(NORMAL)
                            return null;
                        })
                .when(appService)
                .readFile(anyString(), any(), eq("12345|202506"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstmvFields out = (OcstmvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_SHOWN);
        assertThat(out.getAcctido()).isEqualTo(String.format("%011d", 12345L));
        assertThat(out.getStcyco()).isEqualTo(String.format("%06d", 202506));
        assertThat(out.getStopeno()).isEqualTo(editedBalance(openBal));
        assertThat(out.getStcloseo()).isEqualTo(editedBalance(closeBal));
        assertThat(out.getStmino()).isEqualTo(editedBalance(minDue));
        assertThat(out.getStdueo().trim()).isEqualTo("2026-06-25");
        assertEquals(-1, out.getAcctidl());
    }

    @Test
    void mainLine_enterKey_validKeyStatementNotFound_clearsDetailAndShowsNotFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("00000099999");
                    f.setStcyci("202506");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(13); // DFHRESP(NOTFND)
                            return null;
                        })
                .when(appService)
                .readFile(anyString(), any(), eq("99999|202506"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OcstmvFields out = (OcstmvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOTFND);
        assertThat(out.getStopeno().trim()).isEmpty();
        assertThat(out.getStcloseo().trim()).isEmpty();
        assertThat(out.getStmino().trim()).isEmpty();
        assertThat(out.getStdueo().trim()).isEmpty();
    }

    @Test
    void mainLine_enterKey_readFileUnexpectedError_abendsAndSendsErrorText_CONVERT_GAP() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("00000011111");
                    f.setStcyci("202506");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(99); // unexpected error
                            return null;
                        })
                .when(appService)
                .readFile(anyString(), any(), eq("11111|202506"), anyInt());
        doThrow(new ReturnException(null, null, 0)).when(appService).returnProgram();

        assertThrows(ReturnException.class, () -> service.mainLine(appService));

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(appService).sendText(textCaptor.capture(), eq(true), eq(true));
        // CONVERT-GAP: COBOL's 9500-ABEND-RTN does SEND TEXT FROM(WS-MSG-TEXT), so the
        // operator should see "OCSTMV: unrecoverable file error. Contact support.". The
        // converted _9500AbendRtn instead passes String.valueOf(ctx.f) (the whole
        // OcstmvFields accessor, not the WS-MSG-TEXT field), which yields the default
        // Object.toString() "OcstmvFields@<hash>" — the real message never reaches the
        // terminal. Expected value below is the COBOL ground truth; this assertion is
        // expected to fail until the source calls ctx.f.getWsMsgText() instead.
        assertThat(textCaptor.getValue())
                .contains("OCSTMV: unrecoverable file error. Contact support.");
        verify(appService, never())
                .sendMap(eq(MAP_NAME), any(), any(), eq(false), anyBoolean(), anyBoolean());
        verify(appService, never()).returnTransid(anyString(), any(), anyInt());
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcstmv() {
        assertEquals(PROGRAM, service.getProgramName());
    }

    @Test
    void getTransId_returnsOrsv() {
        assertEquals(TRANID, service.getTransId());
    }
}

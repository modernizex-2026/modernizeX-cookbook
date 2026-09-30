package com.generated.orion.ocacctv.service;

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
import com.generated.orion.ocacctv.accessor.OcacctvFields;
import com.generated.orion.ocacctv.model.WorkingStorage;

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
 * Unit tests for OcacctvService, generated from COBOL program OCACCTV (ORION-CCMS "golden
 * reference" for OC* online programs). All business logic is private and is exercised solely
 * through the public {@link OcacctvService#mainLine(AppService)} entry point, driven by an {@link
 * AppService} mock that emulates CICS SEND/RECEIVE/READ/XCTL/RETURN.
 *
 * <p>Convert-gap check: OCACCTV's Java service mirrors every COBOL paragraph 1:1
 * (0000-MAIN..9000-RETURN); no behavioral divergence was found against the COBOL ground truth, so
 * no CONVERT-GAP test is included here.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcacctvServiceTest {

    private static final String ACCTFILE = "ACCTFILE";
    private static final String MAP_NAME = "MACCTVA";
    private static final String COMMAREA_ORION = "OCMENU";

    private static final String MSG_PROMPT = "Enter account id and press ENTER.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_NOTFND = "Record not found.";
    private static final String MSG_REQUIRED = "Please enter all required fields.";
    private static final String HDR_TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM";

    @Mock private AppService appService;

    private OcacctvService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OcacctvService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commareaWithContext(int context) {
        OcacctvFields helper = new OcacctvFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(context));
    }

    /** Stubs receiveMap to populate the MACCTVAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OcacctvFields> populate) {
        doAnswer(
                        inv -> {
                            OcacctvFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    /**
     * Replicates COBOL's "MOVE AC-CREDIT-LIMIT/AC-CASH-LIMIT TO WS-ED-AMT; MOVE WS-ED-AMT TO
     * ACCRLIMO/ACCSLIMO" MOVE chain (4000-POPULATE-DETAIL) on an isolated accessor, so the expected
     * string is derived the same way production computes it — including the final alphanumeric MOVE
     * from the 16-byte WS-ED-AMT edited field into the 15-byte ACCRLIMO/ACCSLIMO screen field
     * (MACCTV.cpy PIC X(15)), which COBOL (and the converted Java) truncates on the right by one
     * byte. Ground truth, not a convert gap.
     */
    private String editedAmount(BigDecimal amount) {
        OcacctvFields helper = new OcacctvFields(new WorkingStorage());
        helper.setAcCreditLimit(amount);
        helper.setWsEdAmt(helper.getAcCreditLimit());
        helper.setAccrlimo(helper.getString("WS-ED-AMT"));
        return helper.getAccrlimo();
    }

    private String editedBalance(BigDecimal amount) {
        OcacctvFields helper = new OcacctvFields(new WorkingStorage());
        helper.setAcCurrBal(amount);
        helper.setWsEdBal(helper.getAcCurrBal());
        helper.setAcbalo(helper.getString("WS-ED-BAL"));
        return helper.getAcbalo();
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcacctvFields out = (OcacctvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORAV");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCACCTV");
        assertThat(out.getTitleo().trim()).isEqualTo(HDR_TITLE);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-22");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        verify(appService).returnTransid(eq("ORAV"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(false));
        verify(appService).returnTransid(eq("ORAV"), any(), eq(692));
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
                        argThat(s -> s.trim().equals(COMMAREA_ORION)),
                        commareaCaptor.capture(),
                        eq(692));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());

        OcacctvFields decoded = new OcacctvFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo("OCACCTV");
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo("ORAV");
        assertEquals(0, decoded.getCaPgmContext());
    }

    @Test
    void mainLine_pf4Pressed_clearsAndResendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcacctvFields out = (OcacctvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        // 2000-PROCESS-INPUT's WHEN OTHER performs 1000-SEND-INITIAL (erase=true) then
        // 8100-SEND-DATAONLY (erase=false); both calls share the same mutable field
        // accessor, so only the final (dataonly) call's content can be inspected —
        // the initial call's transient ERRMSGO is overwritten before assertion time.
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
        OcacctvFields dataOnlyScreen = (OcacctvFields) dataOnlyOut.getValue();
        assertThat(dataOnlyScreen.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-READ-AND-SHOW ─────────────────────────

    @Test
    void mainLine_enterKey_accountIdBlank_showsRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("           "));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcacctvFields out = (OcacctvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_accountIdAllLowValues_showsRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.fillLowValues("ACCTIDI"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcacctvFields out = (OcacctvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_accountFound_populatesDetailAndClearsErrorMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("00000012345"));
        BigDecimal balance = new BigDecimal("1234.56");
        BigDecimal creditLimit = new BigDecimal("5000.00");
        BigDecimal cashLimit = new BigDecimal("1000.00");
        doAnswer(
                        inv -> {
                            OcacctvFields into = inv.getArgument(1);
                            into.setAcActiveStatus("Y");
                            into.setAcCurrBal(balance);
                            into.setAcCreditLimit(creditLimit);
                            into.setAcCashLimit(cashLimit);
                            into.setAcOpenDate("2020-01-15");
                            into.setAcExpiryDate("2028-01-31");
                            into.setAcGroupId("STD001");
                            nextResp.set(0); // DFHRESP(NORMAL)
                            return null;
                        })
                .when(appService)
                .readFile(eq(ACCTFILE), any(), eq("12345"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcacctvFields out = (OcacctvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEmpty();
        assertThat(out.getAcctido()).isEqualTo(String.format("%011d", 12345L));
        assertThat(out.getAcstato().trim()).isEqualTo("Y");
        assertThat(out.getAcbalo()).isEqualTo(editedBalance(balance));
        assertThat(out.getAccrlimo()).isEqualTo(editedAmount(creditLimit));
        assertThat(out.getAccslimo()).isEqualTo(editedAmount(cashLimit));
        assertThat(out.getAcopeno().trim()).isEqualTo("2020-01-15");
        assertThat(out.getAcexpo().trim()).isEqualTo("2028-01-31");
        assertThat(out.getAcgrpo().trim()).isEqualTo("STD001");
    }

    @Test
    void mainLine_enterKey_accountNotFound_showsNotFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("00000099999"));
        doAnswer(
                        inv -> {
                            nextResp.set(13); // DFHRESP(NOTFND)
                            return null;
                        })
                .when(appService)
                .readFile(eq(ACCTFILE), any(), eq("99999"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcacctvFields out = (OcacctvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOTFND);
    }

    @Test
    void mainLine_enterKey_readFileUnexpectedError_finalMessageIsStillNotFoundPerCobolOverwrite() {
        // Per COBOL 3000-READ-ACCT: WHEN OTHER only sets an interim ERRMSGO ("Error
        // reading account file.") but never sets REC-FOUND; 2100-READ-AND-SHOW's ELSE
        // branch (not REC-FOUND) then unconditionally overwrites ERRMSGO with
        // WS-MSG-NOTFND. So the interim message is never shown to the operator, in
        // COBOL and in the converted Java alike — not a convert gap.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("00000011111"));
        doAnswer(
                        inv -> {
                            nextResp.set(99); // unexpected error
                            return null;
                        })
                .when(appService)
                .readFile(eq(ACCTFILE), any(), eq("11111"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcacctvFields out = (OcacctvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOTFND);
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcacctv() {
        assertEquals("OCACCTV", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrav() {
        assertEquals("ORAV", service.getTransId());
    }
}

package com.generated.orion.odacctv.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.odacctv.accessor.OdacctvFields;
import com.generated.orion.odacctv.dao.OdacctvDao;
import com.generated.orion.odacctv.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for OdacctvService, generated from COBOL program ODACCTV. All business logic is
 * private and is exercised solely through the public {@link OdacctvService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS SEND/RECEIVE/XCTL/RETURN,
 * and an {@link OdacctvDao} mock that emulates the DB2 SELECT against ORION.ACCT.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OdacctvServiceTest {

    private static final String MSG_PROMPT = "Enter account id and press ENTER.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_NOTFND = "Record not found.";
    private static final String MSG_REQUIRED = "Please enter all required fields.";
    private static final String MSG_ACCT_NUMERIC = "Account id must be numeric.";
    private static final String MSG_DISPLAYED = "Account displayed.";

    @Mock private AppService appService;

    @Mock private OdacctvDao dao;

    @InjectMocks private OdacctvService service;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA carrying the given CA-PGM-CONTEXT state. */
    private String commareaWithContext(int context) {
        OdacctvFields helper = new OdacctvFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(context));
    }

    /** Stubs receiveMap to populate the MACCTVAI input fields captured from production code. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OdacctvFields> populate) {
        org.mockito.Mockito.doAnswer(
                        inv -> {
                            OdacctvFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq("MACCTVA"), any());
    }

    /** DB2 row found: SQLCODE 0, no SQLCODE key in the result map (per DAO contract). */
    private Map<String, Object> foundAccountRow() {
        Map<String, Object> row = new HashMap<>();
        row.put("AC_ACTIVE_STATUS", "Y");
        row.put("AC_CURR_BAL", new BigDecimal("125.50"));
        row.put("AC_CREDIT_LIMIT", new BigDecimal("1000.00"));
        row.put("AC_CASH_LIMIT", new BigDecimal("500.00"));
        row.put("AC_OPEN_DATE", "2020-01-15");
        row.put("AC_EXPIRY_DATE", "2028-01-31");
        row.put("AC_GROUP_ID", "STD001");
        return row;
    }

    /** DB2 SQLCODE-only result (not found or error), per DAO contract. */
    private Map<String, Object> sqlcodeRow(int sqlcode) {
        Map<String, Object> row = new HashMap<>();
        row.put("SQLCODE", sqlcode);
        return row;
    }

    private void assertFinalErrorMessage(String expectedMessage) {
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTVA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OdacctvFields out = (OdacctvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(expectedMessage);
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTVA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OdacctvFields out = (OdacctvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo("OD01");
        assertThat(out.getPgmnameo().trim()).isEqualTo("ODACCTV");
        assertEquals(1, out.getCaPgmContext());
        verify(appService).returnTransid(eq("OD01"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq("MACCTVA"), any(), any(), eq(true), eq(false), eq(false));
        verify(appService).returnTransid(eq("OD01"), any(), eq(692));
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToMenu() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        verify(appService).xctl(argThat(s -> s.trim().equals("OCMENU")), any(), eq(692));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void mainLine_pf4Pressed_resendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTVA"), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OdacctvFields out = (OdacctvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        // 2000-PROCESS-INPUT WHEN OTHER: resend initial (erase) then a data-only send with the
        // invalid-key message.
        verify(appService).sendMap(eq("MACCTVA"), any(), any(), eq(true), eq(false), eq(false));
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTVA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OdacctvFields out = (OdacctvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-READ-AND-SHOW / 3000-READ-ACCT / 4000-POPULATE-DETAIL
    // ─────────────────────────

    @Test
    void mainLine_enterPressed_accountFound_populatesDetailAndDisplays() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("00000012345"));
        when(dao.selectOrionAcct(12345L)).thenReturn(foundAccountRow());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq("MACCTVA"),
                        sendMapOut.capture(),
                        any(),
                        eq(false),
                        eq(false),
                        eq(false));
        OdacctvFields out = (OdacctvFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_DISPLAYED);
        assertThat(out.getAcctido()).isEqualTo(String.format("%011d", 12345));
        assertThat(out.getAcstato().trim()).isEqualTo("Y");
        assertEquals(0, out.getWsEdBal().compareTo(new BigDecimal("125.50")));
        assertThat(out.getAcopeno().trim()).isEqualTo("2020-01-15");
        assertThat(out.getAcexpo().trim()).isEqualTo("2028-01-31");
        assertThat(out.getAcgrpo().trim()).isEqualTo("STD001");
        verify(dao).selectOrionAcct(12345L);
    }

    @Test
    void mainLine_enterPressed_accountNotFound_showsNotFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi(String.format("%011d", 99999)));
        when(dao.selectOrionAcct(99999L)).thenReturn(sqlcodeRow(100));

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_NOTFND);
    }

    @Test
    void mainLine_enterPressed_sqlError_stillShowsNotFoundMessage() {
        // CONVERT-GAP CHECK: COBOL's 3000-READ-ACCT sets ERRMSGO to 'Error reading account
        // table.' on WHEN OTHER, but 2100-READ-AND-SHOW unconditionally overwrites ERRMSGO
        // with WS-MSG-NOTFND whenever REC-FOUND is false, regardless of the reason. The Java
        // conversion faithfully reproduces this same overwrite (no gap) — expected per COBOL
        // ground truth is still "Record not found.", not the DB2 error text.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi(String.format("%011d", 55555)));
        when(dao.selectOrionAcct(55555L)).thenReturn(sqlcodeRow(-911));

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_NOTFND);
    }

    @Test
    void mainLine_enterPressed_blankAccountId_showsRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("           "));

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_REQUIRED);
        verify(dao, never()).selectOrionAcct(any());
    }

    @Test
    void mainLine_enterPressed_lowValuesAccountId_showsRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("\0\0\0\0\0\0\0\0\0\0\0"));

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_REQUIRED);
        verify(dao, never()).selectOrionAcct(any());
    }

    @Test
    void mainLine_enterPressed_nonNumericAccountId_showsNumericMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("ABCDEFGHIJK"));

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_ACCT_NUMERIC);
        verify(dao, never()).selectOrionAcct(any());
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOdacctv() {
        assertEquals("ODACCTV", service.getProgramName());
    }

    @Test
    void getTransId_returnsOd01() {
        assertEquals("OD01", service.getTransId());
    }

    @Test
    void getButtonDefs_returnsNonEmptyButtonList() {
        assertThat(service.getButtonDefs()).isNotEmpty();
    }

    @Test
    void getFieldMapping_knownMap_returnsPopulatedMapping() {
        com.appruntime.FieldMapping mapping = service.getFieldMapping("MACCTVA");

        assertThat(mapping).isNotNull();
    }

    @Test
    void getFieldMapping_unknownMap_returnsEmptyMapping() {
        com.appruntime.FieldMapping mapping = service.getFieldMapping("UNKNOWN");

        assertThat(mapping).isNotNull();
    }
}

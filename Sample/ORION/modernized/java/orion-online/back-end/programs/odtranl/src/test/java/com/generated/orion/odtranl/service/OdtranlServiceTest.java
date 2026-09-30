package com.generated.orion.odtranl.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppResp;
import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.odtranl.accessor.OdtranlFields;
import com.generated.orion.odtranl.dao.OdtranlDao;
import com.generated.orion.odtranl.model.WorkingStorage;

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
import java.util.List;
import java.util.Map;

/**
 * Unit tests for OdtranlService, generated from COBOL program ODTRANL (ORION-CCMS on-line/DB2
 * transaction list). All business logic is private and exercised solely through {@link
 * OdtranlService#mainLine(AppService)}, driven by mocked {@link AppService} (CICS
 * SEND/RECEIVE/XCTL/RETURN/ASKTIME) and {@link OdtranlDao} (DB2 cursor OPEN/FETCH/CLOSE)
 * collaborators.
 *
 * <p>Convert-gap: 3000-LIST-TRANS in Java (fetchTransactionList) discards the SQLCODE returned by
 * {@code dao.openTrancsr(...)} and unconditionally forces SQLCODE=0 right after the call, so the
 * "Error opening transaction cursor." branch (reachable in COBOL whenever the OPEN's SQLCODE != 0)
 * is structurally dead code in Java. See the CONVERT-GAP test below — it is written to the
 * COBOL-expected behavior and is expected to fail against current Java.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OdtranlServiceTest {

    private static final String MAP_NAME = "MTRANLA";
    private static final String TRANID = "OD05";
    private static final String PGMNAME = "ODTRANL";
    private static final String MENU_PGM = "OCMENU";
    private static final int COMMAREA_LENGTH = 692;

    private static final String MSG_PROMPT = "Enter card number and press ENTER.";
    private static final String MSG_REQUIRED = "Please enter all required fields.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_LISTED = "Transactions listed.";
    private static final String MSG_NONE = "No transactions for this card.";
    private static final String MSG_FETCH_ERROR = "Error fetching transactions.";
    private static final String MSG_OPEN_ERROR = "Error opening transaction cursor.";
    private static final String HDR_TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM";
    private static final String CARD_NUM = "4111111111111111";

    @Mock private AppService appService;

    @Mock private OdtranlDao dao;

    @InjectMocks private OdtranlService service;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(appService.getEibresp()).thenReturn(AppResp.NORMAL);
        org.mockito.Mockito.lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commarea(int context) {
        OdtranlFields helper = new OdtranlFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commarea(context));
    }

    /** Stubs receiveMap to populate MTRANLAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OdtranlFields> populate) {
        org.mockito.Mockito.doAnswer(
                        inv -> {
                            OdtranlFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    private ArgumentCaptor<Object> captureDataOnlySend() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), captor.capture(), any(), eq(false), eq(false), eq(false));
        return captor;
    }

    /** Builds a TRANCSR fetch-success row map as returned by OdtranlDao.fetchTrancsr(). */
    private Map<String, Object> row(String trId, BigDecimal amt, String typeCd) {
        Map<String, Object> m = new HashMap<>();
        m.put("TR_ID", trId);
        m.put("TR_AMT", amt);
        m.put("TR_TYPE_CD", typeCd);
        return m;
    }

    /** Builds a TRANCSR fetch map carrying only SQLCODE (no-row / error signal). */
    private Map<String, Object> sqlcode(int code) {
        Map<String, Object> m = new HashMap<>();
        m.put("SQLCODE", code);
        return m;
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOdtranl() {
        assertEquals("ODTRANL", service.getProgramName());
    }

    @Test
    void getTransId_returnsOd05() {
        assertEquals("OD05", service.getTransId());
    }

    @Test
    void getButtonDefs_returnsThePfKeysDeclaredInCobol() {
        List<com.appruntime.ScreenResponse.ButtonDef> buttons = service.getButtonDefs();
        assertThat(buttons).isNotEmpty();
    }

    @Test
    void registerFsetFields_delegatesToBmsMetadataWithoutError() {
        com.appruntime.AppRunner runner = new com.appruntime.AppRunner(Map.of(), Map.of());
        service.registerFsetFields(runner);
    }

    @Test
    void getFieldMapping_forMtranla_returnsPopulatedMapping() {
        com.appruntime.FieldMapping mapping = service.getFieldMapping(MAP_NAME);
        assertThat(mapping).isNotNull();
    }

    @Test
    void getFieldMapping_forUnknownMap_returnsEmptyMapping() {
        assertThat(service.getFieldMapping("UNKNOWN").isEmpty()).isTrue();
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithHeaderAndPromptMessage() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OdtranlFields out = (OdtranlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo(TRANID);
        assertThat(out.getPgmnameo().trim()).isEqualTo(PGMNAME);
        assertThat(out.getTitleo().trim()).isEqualTo(HDR_TITLE);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-22");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        verify(appService).returnTransid(eq(TRANID), any(), eq(COMMAREA_LENGTH));
        verify(appService, never()).xctl(anyString(), any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(false));
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToMenuWithResetContext() {
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

        OdtranlFields decoded = new OdtranlFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertEquals(0, decoded.getCaPgmContext());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo(PGMNAME);
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo(TRANID);
    }

    @Test
    void mainLine_pf4Pressed_resendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OdtranlFields out = (OdtranlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(false));
        OdtranlFields dataOnlyScreen = (OdtranlFields) captureDataOnlySend().getValue();
        assertThat(dataOnlyScreen.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-LIST-AND-SHOW ─────────────────────────

    @Test
    void mainLine_enterKey_blankCardNumber_showsRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("                "));

        service.mainLine(appService);

        OdtranlFields out = (OdtranlFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(dao, never()).openTrancsr(any());
    }

    @Test
    void mainLine_enterKey_lowValuesCardNumber_showsRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        // receiveMap left unstubbed: CARDNUMI keeps low-values default state, matching
        // CARDNUMI = LOW-VALUES in the COBOL condition.

        service.mainLine(appService);

        OdtranlFields out = (OdtranlFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(dao, never()).openTrancsr(any());
    }

    @Test
    void mainLine_enterKey_validCardNumberNoRows_showsNoTransactionsMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi(CARD_NUM));
        when(dao.fetchTrancsr()).thenReturn(sqlcode(100));

        service.mainLine(appService);

        verify(dao).openTrancsr(eq(CARD_NUM));
        verify(dao, times(1)).fetchTrancsr();
        verify(dao).closeTrancsr();
        OdtranlFields out = (OdtranlFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE);
        assertThat(out.getCardnumo().trim()).isEqualTo(CARD_NUM);
    }

    @Test
    void mainLine_enterKey_oneTransaction_populatesFirstRowAndShowsListedMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi(CARD_NUM));
        BigDecimal amt = new BigDecimal("125.50");
        when(dao.fetchTrancsr()).thenReturn(row("TRN00001", amt, "PU"), sqlcode(100));

        service.mainLine(appService);

        verify(dao, times(2)).fetchTrancsr();
        OdtranlFields out = (OdtranlFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LISTED);
        assertThat(out.getTrn1o().trim()).isEqualTo("TRN00001");
        assertThat(out.getTyp1o().trim()).isEqualTo("PU");
        assertThat(new BigDecimal(out.getAmt1o().trim())).isEqualByComparingTo(amt);
        assertThat(out.getTrn2o().trim()).isEmpty();
    }

    @Test
    void mainLine_enterKey_fiveOrMoreRowsAvailable_populatesExactlyFiveRowsAndStopsAtFive() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi(CARD_NUM));
        when(dao.fetchTrancsr())
                .thenReturn(
                        row("TRN00001", new BigDecimal("10.00"), "PU"),
                        row("TRN00002", new BigDecimal("20.00"), "PU"),
                        row("TRN00003", new BigDecimal("30.00"), "PU"),
                        row("TRN00004", new BigDecimal("40.00"), "PU"),
                        row("TRN00005", new BigDecimal("50.00"), "PU"));

        service.mainLine(appService);

        // Cursor VARYING WS-IDX FROM 1 UNTIL WS-IDX > 5 stops after exactly 5 FETCHes,
        // even though this stub would happily answer a 6th call.
        verify(dao, times(5)).fetchTrancsr();
        verify(dao).closeTrancsr();
        OdtranlFields out = (OdtranlFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LISTED);
        assertThat(out.getTrn1o().trim()).isEqualTo("TRN00001");
        assertThat(out.getTrn5o().trim()).isEqualTo("TRN00005");
    }

    @Test
    void mainLine_enterKey_fetchErrorOnFirstRow_stopsLoopAndShowsFetchErrorMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi(CARD_NUM));
        when(dao.fetchTrancsr()).thenReturn(sqlcode(-911));

        service.mainLine(appService);

        verify(dao, times(1)).fetchTrancsr();
        verify(dao).closeTrancsr();
        OdtranlFields out = (OdtranlFields) captureDataOnlySend().getValue();
        // WS-ROW-CNT stays 0, but ERRMSGO was already set by 3100-FETCH-ROW's WHEN OTHER,
        // so 2100-LIST-AND-SHOW's "ERRMSGO = SPACES" guard must NOT overwrite it with
        // "No transactions for this card.".
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_FETCH_ERROR);
    }

    @Test
    void mainLine_enterKey_cursorOpenReportsSqlError_convertGapJavaIgnoresOpenFailure() {
        // CONVERT-GAP: fetchTransactionList() calls ctx.dao.openTrancsr(...) then
        // immediately does ctx.f.setSqlcode(0) unconditionally, discarding whatever
        // SQLCODE the OPEN actually produced. Per COBOL 3000-LIST-TRANS, a non-zero
        // SQLCODE after OPEN TRANCSR must set ERRMSGO="Error opening transaction
        // cursor." and skip the FETCH loop and CLOSE entirely. In Java this branch is
        // structurally dead code: the outcome below is asserted to the COBOL-expected
        // behavior and is expected to FAIL against the current Java implementation.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi(CARD_NUM));
        when(dao.openTrancsr(any())).thenReturn(sqlcode(8));

        service.mainLine(appService);

        OdtranlFields out = (OdtranlFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_OPEN_ERROR);
        verify(dao, never()).fetchTrancsr();
        verify(dao, never()).closeTrancsr();
    }
}

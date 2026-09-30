package com.generated.orion.odcardv.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.odcardv.accessor.OdcardvFields;
import com.generated.orion.odcardv.dao.OdcardvDao;
import com.generated.orion.odcardv.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Unit tests for OdcardvService, generated from COBOL program ODCARDV. All business logic is
 * private and is exercised solely through the public {@link OdcardvService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS SEND/RECEIVE/XCTL/RETURN and
 * an {@link OdcardvDao} mock that emulates the EXEC SQL SELECT against ORION.CARD.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OdcardvServiceTest {

    private static final String MAP_NAME = "MCARDVA";
    private static final String PGM_NAME = "ODCARDV";
    private static final String TRAN_ID = "OD03";
    private static final String MENU_PGM = "OCMENU";
    private static final int COMMAREA_LENGTH = 692;

    private static final String MSG_PROMPT = "Enter card number and press ENTER.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_REQUIRED = "Please enter all required fields.";
    private static final String MSG_NOTFND = "Record not found.";
    private static final String MSG_DISPLAYED = "Card displayed.";

    @Mock private AppService appService;

    @Mock private OdcardvDao dao;

    @InjectMocks private OdcardvService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), any(), any(), any()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commarea(int context) {
        OdcardvFields helper = new OdcardvFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commarea(context));
    }

    /**
     * Stubs receiveMap to populate the MCARDVAI input fields (CARDNUMI) captured from production
     * code.
     */
    private void givenReceiveMapPopulates(Consumer<OdcardvFields> populate) {
        doAnswer(
                        inv -> {
                            OdcardvFields f = inv.getArgument(1);
                            populate.accept(f);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    /** Stubs the DAO SELECT to simulate SQLCODE=0 (row found) with the given card data. */
    private void givenCardFound(
            long acctId, String embossedName, String expiryDate, String activeStatus) {
        Map<String, Object> row = new HashMap<>();
        row.put("CD_ACCT_ID", acctId);
        row.put("CD_EMBOSSED_NAME", embossedName);
        row.put("CD_EXPIRY_DATE", expiryDate);
        row.put("CD_ACTIVE_STATUS", activeStatus);
        when(dao.selectOrionCard(any())).thenReturn(row);
    }

    /**
     * Stubs the DAO SELECT to simulate the given non-zero SQLCODE (e.g. 100 = not found, other =
     * error).
     */
    private void givenSqlcode(int sqlcode) {
        Map<String, Object> row = new HashMap<>();
        row.put("SQLCODE", sqlcode);
        when(dao.selectOrionCard(any())).thenReturn(row);
    }

    private OdcardvFields captureSendMapOutput(boolean expectedErase) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME),
                        captor.capture(),
                        any(),
                        eq(expectedErase),
                        eq(false),
                        eq(false));
        return (OdcardvFields) captor.getValue();
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        OdcardvFields out = captureSendMapOutput(true);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo(TRAN_ID);
        assertThat(out.getPgmnameo().trim()).isEqualTo(PGM_NAME);
        assertThat(out.getTitleo().trim()).isEqualTo("ORION CREDIT CARD MANAGEMENT SYSTEM");
        verify(appService).returnTransid(eq(TRAN_ID), any(), eq(COMMAREA_LENGTH));
    }

    @Test
    void mainLine_pseudoConversationalContextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        captureSendMapOutput(true);
        verify(appService).returnTransid(eq(TRAN_ID), any(), eq(COMMAREA_LENGTH));
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToMenu() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        verify(appService)
                .xctl(argThat(s -> s.trim().equals(MENU_PGM)), any(), eq(COMMAREA_LENGTH));
        verify(appService, never())
                .sendMap(any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
        verify(appService, never()).receiveMap(any(), any());
    }

    @Test
    void mainLine_pf4Pressed_resendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        OdcardvFields out = captureSendMapOutput(true);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        verify(appService, never()).receiveMap(any(), any());
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessageWithoutErase() {
        // COBOL 2000-PROCESS-INPUT WHEN OTHER: first re-sends the initial screen
        // (erase), then overwrites ERRMSGO with WS-MSG-INVALID-KEY and re-sends
        // DATAONLY (no erase) -- two sendMap calls total.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        verify(appService, times(2))
                .sendMap(eq(MAP_NAME), any(), any(), anyBoolean(), eq(false), eq(false));
        OdcardvFields out = captureSendMapOutput(false);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(any(), any());
    }

    // ───────────────────────── 2100-READ-AND-SHOW ─────────────────────────

    @Test
    void mainLine_enterBlankCardNumber_showsRequiredMessageAndSkipsDb() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("                "));

        service.mainLine(appService);

        OdcardvFields out = captureSendMapOutput(false);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(dao, never()).selectOrionCard(any());
    }

    @Test
    void mainLine_enterLowValuesCardNumber_showsRequiredMessageAndSkipsDb() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.fillLowValues("CARDNUMI"));

        service.mainLine(appService);

        OdcardvFields out = captureSendMapOutput(false);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(dao, never()).selectOrionCard(any());
    }

    @Test
    void mainLine_enterValidCardNumber_sqlcodeZero_displaysCardDetails() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("1111222233334444"));
        givenCardFound(42L, "JOHN SMITH", "2027-12-31", "Y");

        service.mainLine(appService);

        OdcardvFields out = captureSendMapOutput(false);
        assertThat(out.getCardnumo().trim()).isEqualTo("1111222233334444");
        assertThat(out.getCdaccto()).isEqualTo("00000000042");
        assertThat(out.getCdnameo().trim()).isEqualTo("JOHN SMITH");
        assertThat(out.getCdexpo().trim()).isEqualTo("2027-12-31");
        assertThat(out.getCdstato().trim()).isEqualTo("Y");
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_DISPLAYED);
    }

    @Test
    void mainLine_enterValidCardNumber_sqlcode100_showsNotFoundAndSkipsDetail() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("9999999999999999"));
        givenSqlcode(100);

        service.mainLine(appService);

        OdcardvFields out = captureSendMapOutput(false);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOTFND);
        // MCARDVAO REDEFINES MCARDVAI in the BMS map (see layout XML: CARDNUMI and CARDNUMO
        // share the same buffer offset) -- this is correct BMS semantics, not a convert gap.
        // 4000-POPULATE-DETAIL is skipped on the not-found path, but CARDNUMO still reads
        // back whatever was typed into CARDNUMI because they are the same physical bytes.
        assertThat(out.getCardnumo().trim()).isEqualTo("9999999999999999");
        assertThat(out.getCdaccto().trim()).isEmpty();
        assertThat(out.getCdnameo().trim()).isEmpty();
    }

    @Test
    void mainLine_enterValidCardNumber_sqlErrorOtherCode_finalMessageIsNotFound() {
        // Per COBOL ground truth: 3000-READ-CARD's WHEN OTHER branch sets ERRMSGO to
        // 'Error reading card table.' AND sets REC-NOT-FOUND -- then 2100-READ-AND-SHOW's
        // ELSE branch (since REC-FOUND is false) unconditionally overwrites ERRMSGO with
        // WS-MSG-NOTFND. This double-overwrite is original COBOL behavior (not a convert
        // gap): the final message the user sees on a DB error is 'Record not found.',
        // not the error text.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("1111222233334444"));
        givenSqlcode(-911);

        service.mainLine(appService);

        OdcardvFields out = captureSendMapOutput(false);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOTFND);
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOdcardv() {
        assertEquals(PGM_NAME, service.getProgramName());
    }

    @Test
    void getTransId_returnsOd03() {
        assertEquals(TRAN_ID, service.getTransId());
    }

    @Test
    void getButtonDefs_delegatesToMetadataWithoutError() {
        assertNotNull(service.getButtonDefs());
    }

    @Test
    void getFieldMapping_delegatesToMetadataForKnownMap() {
        assertNotNull(service.getFieldMapping(MAP_NAME));
    }
}

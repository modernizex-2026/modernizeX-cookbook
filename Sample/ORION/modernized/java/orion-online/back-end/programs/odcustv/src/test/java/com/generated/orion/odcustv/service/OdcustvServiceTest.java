package com.generated.orion.odcustv.service;

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
import com.generated.orion.odcustv.accessor.OdcustvFields;
import com.generated.orion.odcustv.dao.OdcustvDao;
import com.generated.orion.odcustv.model.WorkingStorage;

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
 * Unit tests for OdcustvService, generated from COBOL program ODCUSTV. All business logic is
 * private and is exercised solely through the public {@link OdcustvService#mainLine(AppService)}
 * entry point, driven by an {@link AppService} mock that emulates CICS SEND/RECEIVE/XCTL/RETURN and
 * an {@link OdcustvDao} mock that emulates the EXEC SQL SELECT against ORION.CUST.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OdcustvServiceTest {

    private static final String MAP_NAME = "MCUSTVA";
    private static final String PGM_NAME = "ODCUSTV";
    private static final String TRAN_ID = "OD04";
    private static final String MENU_PGM = "OCMENU";
    private static final int COMMAREA_LENGTH = 692;

    private static final String MSG_PROMPT = "Enter customer id and press ENTER.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_REQUIRED = "Please enter all required fields.";
    private static final String MSG_NOTFND = "Record not found.";
    private static final String MSG_NUMERIC = "Customer id must be numeric.";
    private static final String MSG_DISPLAYED = "Customer displayed.";

    @Mock private AppService appService;

    @Mock private OdcustvDao dao;

    @InjectMocks private OdcustvService service;

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
        OdcustvFields helper = new OdcustvFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commarea(context));
    }

    /**
     * Stubs receiveMap to populate the MCUSTVAI input fields (CUSTIDI) captured from production
     * code.
     */
    private void givenReceiveMapPopulates(Consumer<OdcustvFields> populate) {
        doAnswer(
                        inv -> {
                            OdcustvFields f = inv.getArgument(1);
                            populate.accept(f);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    /** Stubs the DAO SELECT to simulate SQLCODE=0 (row found) with the given customer data. */
    private void givenCustomerFound(
            String firstName,
            String middleName,
            String lastName,
            String addrLine1,
            String addrCity,
            String phone1,
            int ficoScore) {
        Map<String, Object> row = new HashMap<>();
        row.put("CU_FIRST_NAME", firstName);
        row.put("CU_MIDDLE_NAME", middleName);
        row.put("CU_LAST_NAME", lastName);
        row.put("CU_ADDR_LINE_1", addrLine1);
        row.put("CU_ADDR_CITY", addrCity);
        row.put("CU_PHONE_1", phone1);
        row.put("CU_FICO_SCORE", ficoScore);
        when(dao.selectOrionCust(any())).thenReturn(row);
    }

    /**
     * Stubs the DAO SELECT to simulate the given non-zero SQLCODE (e.g. 100 = not found, other =
     * error).
     */
    private void givenSqlcode(int sqlcode) {
        Map<String, Object> row = new HashMap<>();
        row.put("SQLCODE", sqlcode);
        when(dao.selectOrionCust(any())).thenReturn(row);
    }

    private OdcustvFields captureSendMapOutput(boolean expectedErase) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME),
                        captor.capture(),
                        any(),
                        eq(expectedErase),
                        eq(false),
                        eq(false));
        return (OdcustvFields) captor.getValue();
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        OdcustvFields out = captureSendMapOutput(true);
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

        OdcustvFields out = captureSendMapOutput(true);
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
        OdcustvFields out = captureSendMapOutput(false);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(any(), any());
    }

    // ───────────────────────── 2100-READ-AND-SHOW ─────────────────────────

    @Test
    void mainLine_enterBlankCustomerId_showsRequiredMessageAndSkipsDb() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("         "));

        service.mainLine(appService);

        OdcustvFields out = captureSendMapOutput(false);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(dao, never()).selectOrionCust(any());
    }

    @Test
    void mainLine_enterLowValuesCustomerId_showsRequiredMessageAndSkipsDb() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.fillLowValues("CUSTIDI"));

        service.mainLine(appService);

        OdcustvFields out = captureSendMapOutput(false);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(dao, never()).selectOrionCust(any());
    }

    @Test
    void mainLine_enterNonNumericCustomerId_showsNumericMessageAndSkipsDb() {
        // COBOL 2100-READ-AND-SHOW: IF CUSTIDI IS NUMERIC ... ELSE MOVE
        // 'Customer id must be numeric.' TO ERRMSGO -- this ELSE branch has no
        // corresponding COBOL paragraph call, so 3000-READ-CUST is never reached.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("ABCDEFGHI"));

        service.mainLine(appService);

        OdcustvFields out = captureSendMapOutput(false);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NUMERIC);
        verify(dao, never()).selectOrionCust(any());
    }

    @Test
    void mainLine_enterValidCustomerId_sqlcodeZero_displaysCustomerDetails() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("000000042"));
        givenCustomerFound("JOHN", "Q", "SMITH", "1 MAIN ST", "SPRINGFIELD", "555-1234", 710);

        service.mainLine(appService);

        OdcustvFields out = captureSendMapOutput(false);
        assertThat(out.getCustido()).isEqualTo("000000042");
        assertThat(out.getCunameo().trim()).isEqualTo("JOHN Q SMITH");
        assertThat(out.getCuaddro().trim()).isEqualTo("1 MAIN ST");
        assertThat(out.getCucityo().trim()).isEqualTo("SPRINGFIELD");
        assertThat(out.getCuphoneo().trim()).isEqualTo("555-1234");
        assertThat(out.getCuficoo()).isEqualTo("710");
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_DISPLAYED);
    }

    @Test
    void mainLine_enterValidCustomerId_middleNameBlank_buildsNameWithDoubleSpace() {
        // COBOL 4000-POPULATE-DETAIL STRING ... DELIMITED BY SPACE: an all-spaces
        // CU-MIDDLE-NAME contributes zero characters between the two literal
        // spaces, producing a double space in WS-FULL-NAME. Java's split(" ", 2)[0]
        // on an all-spaces field likewise yields "", reproducing the same output.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("000000007"));
        givenCustomerFound(
                "JANE",
                "                         ",
                "DOE",
                "2 OAK AVE",
                "METROPOLIS",
                "555-9876",
                5);

        service.mainLine(appService);

        OdcustvFields out = captureSendMapOutput(false);
        assertThat(out.getCunameo().trim()).isEqualTo("JANE  DOE");
        assertThat(out.getCuficoo()).isEqualTo("005");
    }

    @Test
    void mainLine_enterValidCustomerId_sqlcode100_showsNotFoundAndSkipsDetail() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("999999999"));
        givenSqlcode(100);

        service.mainLine(appService);

        OdcustvFields out = captureSendMapOutput(false);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOTFND);
        // MCUSTVAO REDEFINES MCUSTVAI in the BMS map (see layout XML: CUSTIDI and
        // CUSTIDO share the same buffer offset) -- this is correct BMS semantics,
        // not a convert gap. 4000-POPULATE-DETAIL is skipped on the not-found
        // path, but CUSTIDO still reads back whatever was typed into CUSTIDI
        // because they are the same physical bytes.
        assertThat(out.getCustido().trim()).isEqualTo("999999999");
        assertThat(out.getCunameo().trim()).isEmpty();
    }

    @Test
    void mainLine_enterValidCustomerId_sqlErrorOtherCode_finalMessageIsNotFound() {
        // Per COBOL ground truth: 3000-READ-CUST's WHEN OTHER branch sets ERRMSGO
        // to 'Error reading customer table.' AND sets REC-NOT-FOUND -- then
        // 2100-READ-AND-SHOW's ELSE branch (since REC-FOUND is false) unconditionally
        // overwrites ERRMSGO with WS-MSG-NOTFND. This double-overwrite is original
        // COBOL behavior (not a convert gap): the final message the user sees on a
        // DB error is 'Record not found.', not the error text.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("000000042"));
        givenSqlcode(-911);

        service.mainLine(appService);

        OdcustvFields out = captureSendMapOutput(false);
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOTFND);
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOdcustv() {
        assertEquals(PGM_NAME, service.getProgramName());
    }

    @Test
    void getTransId_returnsOd04() {
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

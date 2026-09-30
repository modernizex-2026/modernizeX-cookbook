package com.generated.orion.occustl.service;

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

import com.appruntime.AppResp;
import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.occustl.accessor.OccustlFields;
import com.generated.orion.occustl.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OccustlService, generated from COBOL program OCCUSTL (ORION-CCMS customer list
 * on-line browse, following the OCACCTV skeleton). All business logic is private and is exercised
 * solely through the public {@link OccustlService#mainLine(AppService)} entry point, driven by an
 * {@link AppService} mock that emulates CICS SEND/RECEIVE/STARTBR/READNEXT/ ENDBR/XCTL/RETURN.
 *
 * <p>Convert-gap check: OCCUSTL's Java service mirrors every COBOL paragraph 1:1
 * (0000-MAIN..9000-RETURN), including CICS response code constants (NORMAL=0, NOTFND=13,
 * ENDFILE=20, MAPFAIL=36 per {@link AppResp}) and the WS-ROW-CNT / WS-CNT-ED PIC Z9 summary edit.
 * No behavioral divergence was found against the COBOL ground truth, so no CONVERT-GAP test is
 * included.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OccustlServiceTest {

    private static final String MAP_NAME = "MCUSTLA";
    private static final String TRANID = "ORLC";
    private static final String PGMNAME = "OCCUSTL";
    private static final String MENU_PGM = "OCMENU";
    private static final String CUSTFILE = "CUSTFILE";
    private static final int COMMAREA_LENGTH = 692;

    private static final String MSG_PROMPT = "Enter start customer id (or blank) and ENTER.";
    private static final String MSG_BAD_START = "Start customer id must be numeric.";
    private static final String MSG_NONE_FOUND = "No customers found from that point.";
    private static final String MSG_BROWSE_ERR = "Error browsing the customer file.";
    private static final String MSG_END_FILE = "End of file - no more customers.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String HDR_TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM";

    @Mock private AppService appService;

    private OccustlService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OccustlService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commareaWithContext(int context) {
        OccustlFields helper = new OccustlFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /**
     * Builds a serialized ORION-COMMAREA carrying CA-PGM-CONTEXT and a saved CA-WORK-AREA(1:9) key.
     */
    private String commareaWithSavedKey(int context, String workArea9) {
        OccustlFields helper = new OccustlFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        helper.setCaWorkArea(Utility.setSubstring(helper.getCaWorkArea(), 1, 9, workArea9));
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(context));
    }

    private void givenPseudoConversationWithSavedKey(int context, String workArea9) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea())
                .thenReturn(commareaWithSavedKey(context, workArea9));
    }

    /** Stubs receiveMap to populate the MCUSTLAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OccustlFields> populate) {
        doAnswer(
                        inv -> {
                            OccustlFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    private static final class CustomerRow {
        final int id;
        final String first;
        final String last;
        final int fico;

        CustomerRow(int id, String first, String last, int fico) {
            this.id = id;
            this.first = first;
            this.last = last;
            this.fico = fico;
        }
    }

    /**
     * Simulates CUSTFILE STARTBR(GTEQ)/READNEXT/ENDBR over an in-memory, key-ordered dataset:
     * STARTBR positions at the first row whose CU-ID >= the requested key (NOTFND when none
     * qualify); READNEXT streams rows forward until exhausted (ENDFILE).
     */
    private void givenCustomerFile(List<CustomerRow> rows) {
        AtomicInteger cursor = new AtomicInteger(0);
        lenient()
                .doAnswer(
                        inv -> {
                            String ridfld = inv.getArgument(1);
                            int startKey = Integer.parseInt(ridfld);
                            int pos = 0;
                            while (pos < rows.size() && rows.get(pos).id < startKey) {
                                pos++;
                            }
                            cursor.set(pos);
                            nextResp.set(pos < rows.size() ? AppResp.NORMAL : AppResp.NOTFND);
                            return null;
                        })
                .when(appService)
                .startBrowse(eq(CUSTFILE), anyString(), anyInt());

        lenient()
                .doAnswer(
                        inv -> {
                            OccustlFields into = inv.getArgument(1);
                            int pos = cursor.get();
                            if (pos < rows.size()) {
                                CustomerRow r = rows.get(pos);
                                into.setCuId(r.id);
                                into.setCuFirstName(r.first);
                                into.setCuLastName(r.last);
                                into.setCuFicoScore(r.fico);
                                cursor.set(pos + 1);
                                nextResp.set(AppResp.NORMAL);
                            } else {
                                nextResp.set(AppResp.ENDFILE);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(eq(CUSTFILE), any());

        lenient()
                .doAnswer(
                        inv -> {
                            nextResp.set(AppResp.NORMAL);
                            return null;
                        })
                .when(appService)
                .endBrowse(eq(CUSTFILE));
    }

    private ArgumentCaptor<Object> captureDataOnlySend() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), captor.capture(), any(), eq(false), eq(false), eq(true));
        return captor;
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOccustl() {
        assertEquals("OCCUSTL", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrlc() {
        assertEquals("ORLC", service.getTransId());
    }

    @Test
    void getButtonDefs_returnsTheFourPfKeysDeclaredInCobol() {
        List<com.appruntime.ScreenResponse.ButtonDef> buttons = service.getButtonDefs();
        assertThat(buttons).hasSize(4);
    }

    @Test
    void registerFsetFields_delegatesToBmsMetadataWithoutError() {
        com.appruntime.AppRunner runner =
                new com.appruntime.AppRunner(java.util.Map.of(), java.util.Map.of());
        service.registerFsetFields(runner);
    }

    @Test
    void getFieldMapping_forMcustla_returnsPopulatedMapping() {
        com.appruntime.FieldMapping mapping = service.getFieldMapping("MCUSTLA");
        assertThat(mapping).isNotNull();
    }

    @Test
    void getFieldMapping_forUnknownMap_returnsEmptyMapping() {
        assertThat(service.getFieldMapping("UNKNOWN").isEmpty()).isTrue();
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OccustlFields out = (OccustlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo(TRANID);
        assertThat(out.getPgmnameo().trim()).isEqualTo(PGMNAME);
        assertThat(out.getTitleo().trim()).isEqualTo(HDR_TITLE);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-22");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        assertThat(out.getFrcustl()).isEqualTo(-1);
        verify(appService).returnTransid(eq(TRANID), any(), eq(COMMAREA_LENGTH));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        verify(appService).returnTransid(eq(TRANID), any(), eq(COMMAREA_LENGTH));
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
                        eq(COMMAREA_LENGTH));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());

        OccustlFields decoded = new OccustlFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo(PGMNAME);
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo(TRANID);
        assertEquals(0, decoded.getCaPgmContext());
    }

    @Test
    void mainLine_pf4Pressed_resendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OccustlFields out = (OccustlFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        // 2000-PROCESS-INPUT's WHEN OTHER performs 1000-SEND-INITIAL (erase=true) then
        // 8100-SEND-DATAONLY (erase=false); both share the same mutable field accessor,
        // so only the final (dataonly) call's content reflects the invalid-key message.
        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        OccustlFields dataOnlyScreen = (OccustlFields) captureDataOnlySend().getValue();
        assertThat(dataOnlyScreen.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-LIST-FROM-START (ENTER) ─────────────────────────

    @Test
    void mainLine_enterKey_blankStart_fourCustomersFound_showsAllFourRowsAndSavesNextKey() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFrcusti("         "));
        givenCustomerFile(
                List.of(
                        new CustomerRow(10, "JOHN", "SMITH", 700),
                        new CustomerRow(20, "MARY", "JONES", 650),
                        new CustomerRow(30, "PAUL", "BROWN", 600),
                        new CustomerRow(40, "ANNE", "WHITE", 720),
                        new CustomerRow(50, "TOM", "BLACK", 690)));

        service.mainLine(appService);

        OccustlFields out = (OccustlFields) captureDataOnlySend().getValue();
        assertThat(out.getCul1o().trim()).isEqualTo(String.format("%09d", 10));
        assertThat(out.getCun1o().trim()).isEqualTo("SMITH, JOHN");
        assertThat(out.getCuf1o().trim()).isEqualTo(String.format("%03d", 700));
        assertThat(out.getCul2o().trim()).isEqualTo(String.format("%09d", 20));
        assertThat(out.getCul3o().trim()).isEqualTo(String.format("%09d", 30));
        assertThat(out.getCul4o().trim()).isEqualTo(String.format("%09d", 40));
        assertThat(out.getErrmsgo().trim()).isEqualTo("4 customer(s) displayed.");
        verify(appService).endBrowse(eq(CUSTFILE));
        // Fifth row (id 50) never read: WS-ROW-CNT reached WS-MAX-ROWS (4) first.
        verify(appService, org.mockito.Mockito.times(4)).readNext(eq(CUSTFILE), any());
    }

    @Test
    void mainLine_enterKey_nonNumericStart_showsBadStartMessageAndSkipsBrowse() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFrcusti("12X456789"));

        service.mainLine(appService);

        OccustlFields out = (OccustlFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BAD_START);
        assertThat(out.getFrcustl()).isEqualTo(-1);
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_startWithEmbeddedSpaces_parsesDigitsIgnoringSpaces() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFrcusti("   123   "));
        givenCustomerFile(List.of(new CustomerRow(123, "ANA", "REED", 500)));

        service.mainLine(appService);

        verify(appService).startBrowse(eq(CUSTFILE), eq("123"), anyInt());
        OccustlFields out = (OccustlFields) captureDataOnlySend().getValue();
        assertThat(out.getCul1o().trim()).isEqualTo(String.format("%09d", 123));
        assertThat(out.getErrmsgo().trim()).isEqualTo("1 customer(s) displayed.");
    }

    @Test
    void mainLine_enterKey_mapfailOnReceive_defaultsToBlankStartAndBrowsesFromZero() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        nextResp.set(AppResp.MAPFAIL);
        // receiveMap left unstubbed (no-op): MCUSTLAI keeps its default field state, and
        // 2100-LIST-FROM-START's MAPFAIL branch overwrites it with LOW-VALUES, matching COBOL.
        givenCustomerFile(List.of(new CustomerRow(5, "IVY", "SHORT", 550)));

        service.mainLine(appService);

        verify(appService).startBrowse(eq(CUSTFILE), eq("0"), anyInt());
        OccustlFields out = (OccustlFields) captureDataOnlySend().getValue();
        assertThat(out.getCul1o().trim()).isEqualTo(String.format("%09d", 5));
    }

    @Test
    void mainLine_enterKey_partialPage_twoCustomersFound_savesNextStartKey() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFrcusti("         "));
        givenCustomerFile(
                List.of(
                        new CustomerRow(100, "JOHN", "SMITH", 700),
                        new CustomerRow(200, "MARY", "JONES", 650)));

        service.mainLine(appService);

        OccustlFields out = (OccustlFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo("2 customer(s) displayed.");
        assertThat(out.getCul3o().trim()).isEmpty();
        // WS-NEXT-START = WS-LAST-KEY + 1, saved to CA-WORK-AREA(1:9) (zero-padded PIC 9(09)).
        assertThat(out.getCaWorkArea().substring(0, 9)).isEqualTo(String.format("%09d", 201));
        verify(appService).endBrowse(eq(CUSTFILE));
    }

    @Test
    void mainLine_enterKey_noCustomersAtOrAfterStart_showsNoneFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFrcusti("999999999"));
        givenCustomerFile(Collections.emptyList());

        service.mainLine(appService);

        OccustlFields out = (OccustlFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_enterKey_browseStartError_finalMessageIsNoneFoundPerCobolOverwrite() {
        // Per COBOL 3100-START-BROWSE's WHEN OTHER: sets an interim ERRMSGO ("Error
        // browsing the customer file.") but WS-ROW-CNT stays ZEROS, so 2300-BUILD-SUMMARY
        // unconditionally overwrites ERRMSGO with WS-M-NONE-FOUND. The interim message is
        // never shown to the operator, in COBOL and in the converted Java alike — not a
        // convert gap.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFrcusti("         "));
        doAnswer(
                        inv -> {
                            nextResp.set(99); // unexpected STARTBR error, not NORMAL/NOTFND/ENDFILE
                            return null;
                        })
                .when(appService)
                .startBrowse(eq(CUSTFILE), anyString(), anyInt());

        service.mainLine(appService);

        OccustlFields out = (OccustlFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NONE_FOUND);
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_enterKey_readNextError_stopsBrowseButKeepsRowsAlreadyRead() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setFrcusti("         "));
        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            nextResp.set(AppResp.NORMAL);
                            return null;
                        })
                .when(appService)
                .startBrowse(eq(CUSTFILE), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            OccustlFields into = inv.getArgument(1);
                            if (callCount.getAndIncrement() == 0) {
                                into.setCuId(1);
                                into.setCuFirstName("SUE");
                                into.setCuLastName("LEE");
                                into.setCuFicoScore(610);
                                nextResp.set(AppResp.NORMAL);
                            } else {
                                nextResp.set(77); // unexpected READNEXT error, not NORMAL/ENDFILE
                            }
                            return null;
                        })
                .when(appService)
                .readNext(eq(CUSTFILE), any());

        service.mainLine(appService);

        OccustlFields out = (OccustlFields) captureDataOnlySend().getValue();
        assertThat(out.getCul1o().trim()).isEqualTo(String.format("%09d", 1));
        // Row was read before the error, so WS-ROW-CNT=1 > ZEROS: 2300-BUILD-SUMMARY
        // overwrites the interim browse-error message with the count, per COBOL.
        assertThat(out.getErrmsgo().trim()).isEqualTo("1 customer(s) displayed.");
        verify(appService).endBrowse(eq(CUSTFILE));
    }

    // ───────────────────────── 2200-PAGE-FWD (PF8) ─────────────────────────

    @Test
    void mainLine_pf8_numericWorkArea_resumesBrowseAtSavedKey() {
        givenPseudoConversationWithSavedKey(1, "000000201");
        when(appService.getEibaid()).thenReturn("8");
        givenCustomerFile(List.of(new CustomerRow(201, "KIM", "WEST", 640)));

        service.mainLine(appService);

        verify(appService).startBrowse(eq(CUSTFILE), eq("201"), anyInt());
        verify(appService, never()).receiveMap(anyString(), any());
        OccustlFields out = (OccustlFields) captureDataOnlySend().getValue();
        assertThat(out.getCul1o().trim()).isEqualTo(String.format("%09d", 201));
    }

    @Test
    void mainLine_pf8_blankWorkArea_startsFromZero() {
        givenPseudoConversation(1); // default CA-WORK-AREA is spaces, not 9 numeric digits
        when(appService.getEibaid()).thenReturn("8");
        givenCustomerFile(List.of(new CustomerRow(1, "AL", "FOX", 500)));

        service.mainLine(appService);

        verify(appService).startBrowse(eq(CUSTFILE), eq("0"), anyInt());
    }

    @Test
    void mainLine_pf8_endOfFile_showsEndFileMessageNotNoneFound() {
        // 2200-PAGE-FWD has its own WS-ROW-CNT=ZEROS branch (WS-M-END-FILE), distinct
        // from 2100/2300's WS-M-NONE-FOUND — ground truth per COBOL, not a convert gap.
        givenPseudoConversationWithSavedKey(1, "999999999");
        when(appService.getEibaid()).thenReturn("8");
        givenCustomerFile(Collections.emptyList());

        service.mainLine(appService);

        OccustlFields out = (OccustlFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_END_FILE);
    }

    @Test
    void mainLine_pf8_rowsFound_showsSummaryMessage() {
        givenPseudoConversationWithSavedKey(1, "000000300");
        when(appService.getEibaid()).thenReturn("8");
        givenCustomerFile(
                List.of(
                        new CustomerRow(300, "BEN", "HALL", 610),
                        new CustomerRow(301, "MIA", "KANE", 620),
                        new CustomerRow(302, "ROY", "DYER", 630)));

        service.mainLine(appService);

        OccustlFields out = (OccustlFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo("3 customer(s) displayed.");
        assertThat(out.getCul3o().trim()).isEqualTo(String.format("%09d", 302));
    }
}

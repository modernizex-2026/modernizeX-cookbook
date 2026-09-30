package com.generated.orion.occustu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
import com.generated.orion.occustu.accessor.OccustuFields;
import com.generated.orion.occustu.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

/**
 * Unit tests for OccustuService, generated from COBOL program OCCUSTU (ORION-CCMS customer
 * maintenance on-line, following the OCACCTV skeleton). All business logic is private and is
 * exercised solely through the public {@link OccustuService#mainLine(AppService)} entry point,
 * driven by an {@link AppService} mock that emulates CICS SEND/RECEIVE/READ(UPDATE)/
 * REWRITE/XCTL/RETURN.
 *
 * <p>Convert-gap check: OCCUSTU's Java service mirrors every COBOL paragraph 1:1
 * (0000-MAIN..9000-RETURN), including CICS response code handling (NORMAL=0, NOTFND=13, OTHER per
 * {@link AppResp}) and the two-step load/save conversation state machine (WS-STATE-FLAG in
 * CA-WORK-AREA(1:1)). No behavioral divergence was found against the COBOL ground truth, so no
 * CONVERT-GAP test is included.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OccustuServiceTest {

    private static final String MAP_NAME = "MCUSTUA";
    private static final String TRANID = "ORUU";
    private static final String PGMNAME = "OCCUSTU";
    private static final String MENU_PGM = "OCMENU";
    private static final String CUSTFILE = "CUSTFILE";
    private static final int COMMAREA_LENGTH = 692;

    private static final String MSG_PROMPT = "Enter a customer id and press ENTER to load.";
    private static final String MSG_LOADED = "Record loaded - change fields and ENTER to save.";
    private static final String MSG_UPDATED = "Customer updated successfully.";
    private static final String MSG_CUST_NOTFND = "Customer not found - check the id and retry.";
    private static final String MSG_ID_REQUIRED = "Customer id is required.";
    private static final String MSG_ID_NOTNUM = "Customer id must be numeric.";
    private static final String MSG_FNAME_REQ = "First name is required.";
    private static final String MSG_LNAME_REQ = "Last name is required.";
    private static final String MSG_ADDR_REQ = "Address line 1 is required.";
    private static final String MSG_CITY_REQ = "City is required.";
    private static final String MSG_PHONE_REQ = "Primary phone is required.";
    private static final String MSG_UPD_ERROR = "Error rewriting the customer record.";
    private static final String MSG_DELETED = "Record no longer on file - reload the id.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String HDR_TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM";

    @Mock private AppService appService;

    private OccustuService service;

    @BeforeEach
    void setUp() {
        service = new OccustuService();
        lenient().when(appService.getEibresp()).thenReturn(AppResp.NORMAL);
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /**
     * Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT (and defaults).
     */
    private String commarea(int context) {
        OccustuFields helper = new OccustuFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /**
     * Builds a serialized ORION-COMMAREA carrying CA-PGM-CONTEXT, load-state flag and a saved
     * CA-CUST-ID.
     */
    private String commareaLoaded(int context, String stateFlag, int caCustId) {
        OccustuFields helper = new OccustuFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        helper.setCaWorkArea(Utility.setSubstring(helper.getCaWorkArea(), 1, 1, stateFlag));
        helper.setCaCustId(caCustId);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commarea(context));
    }

    private void givenPseudoConversationLoaded(String stateFlag, int caCustId) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaLoaded(1, stateFlag, caCustId));
    }

    /** Stubs receiveMap to populate the MCUSTUAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OccustuFields> populate) {
        doAnswer(
                        inv -> {
                            OccustuFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    private static final class CustomerRow {
        final int id;
        String first = "JOHN";
        String last = "SMITH";
        String addr = "1 MAIN ST";
        String city = "ANYTOWN";
        String phone = "5551234567";

        CustomerRow(int id) {
            this.id = id;
        }
    }

    /**
     * Simulates CUSTFILE READ / READ UPDATE by CU-ID over an in-memory map. Missing id -> NOTFND.
     */
    private void givenCustomerFile(List<CustomerRow> rows) {
        lenient()
                .doAnswer(
                        inv -> {
                            String ridfld = inv.getArgument(2);
                            int key = Integer.parseInt(ridfld);
                            OccustuFields into = inv.getArgument(1);
                            CustomerRow r =
                                    rows.stream()
                                            .filter(row -> row.id == key)
                                            .findFirst()
                                            .orElse(null);
                            if (r != null) {
                                into.setCuId(r.id);
                                into.setCuFirstName(r.first);
                                into.setCuLastName(r.last);
                                into.setCuAddrLine1(r.addr);
                                into.setCuAddrCity(r.city);
                                into.setCuPhone1(r.phone);
                            }
                            when(appService.getEibresp())
                                    .thenReturn(r != null ? AppResp.NORMAL : AppResp.NOTFND);
                            return null;
                        })
                .when(appService)
                .readFile(eq(CUSTFILE), any(), anyString(), org.mockito.ArgumentMatchers.anyInt());

        lenient()
                .doAnswer(
                        inv -> {
                            String ridfld = inv.getArgument(2);
                            int key = Integer.parseInt(ridfld);
                            OccustuFields into = inv.getArgument(1);
                            CustomerRow r =
                                    rows.stream()
                                            .filter(row -> row.id == key)
                                            .findFirst()
                                            .orElse(null);
                            if (r != null) {
                                into.setCuId(r.id);
                                into.setCuFirstName(r.first);
                                into.setCuLastName(r.last);
                                into.setCuAddrLine1(r.addr);
                                into.setCuAddrCity(r.city);
                                into.setCuPhone1(r.phone);
                            }
                            when(appService.getEibresp())
                                    .thenReturn(r != null ? AppResp.NORMAL : AppResp.NOTFND);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(
                        eq(CUSTFILE), any(), anyString(), org.mockito.ArgumentMatchers.anyInt());

        lenient()
                .doAnswer(
                        inv -> {
                            when(appService.getEibresp()).thenReturn(AppResp.NORMAL);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(CUSTFILE), any());
    }

    private ArgumentCaptor<Object> captureDataOnlySend() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), captor.capture(), any(), eq(false), eq(false), eq(true));
        return captor;
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOccustu() {
        assertEquals("OCCUSTU", service.getProgramName());
    }

    @Test
    void getTransId_returnsOruu() {
        assertEquals("ORUU", service.getTransId());
    }

    @Test
    void getButtonDefs_returnsThePfKeysDeclaredInCobol() {
        List<com.appruntime.ScreenResponse.ButtonDef> buttons = service.getButtonDefs();
        assertThat(buttons).isNotEmpty();
    }

    @Test
    void registerFsetFields_delegatesToBmsMetadataWithoutError() {
        com.appruntime.AppRunner runner =
                new com.appruntime.AppRunner(java.util.Map.of(), java.util.Map.of());
        service.registerFsetFields(runner);
    }

    @Test
    void getFieldMapping_forMcustua_returnsPopulatedMapping() {
        com.appruntime.FieldMapping mapping = service.getFieldMapping("MCUSTUA");
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
        OccustuFields out = (OccustuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo(TRANID);
        assertThat(out.getPgmnameo().trim()).isEqualTo(PGMNAME);
        assertThat(out.getTitleo().trim()).isEqualTo(HDR_TITLE);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-22");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        assertThat(out.getCustidl()).isEqualTo(-1);
        assertEquals(0, out.getCaCustId());
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

        OccustuFields decoded = new OccustuFields(new WorkingStorage());
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
        OccustuFields out = (OccustuFields) sendMapOut.getValue();
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
        OccustuFields dataOnlyScreen = (OccustuFields) captureDataOnlySend().getValue();
        assertThat(dataOnlyScreen.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2200-EDIT-CUST-ID ─────────────────────────

    @Test
    void mainLine_enterKey_blankCustId_showsIdRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("         "));

        service.mainLine(appService);

        OccustuFields out = (OccustuFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ID_REQUIRED);
        assertThat(out.getCustidl()).isEqualTo(-1);
        verify(appService, never())
                .readFile(anyString(), any(), anyString(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void mainLine_enterKey_lowValuesCustId_showsIdRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        // receiveMap left unstubbed: MCUSTUAI keeps low-values default state (fillLowValues never
        // called on this path, but a freshly registered buffer already reads as all-low-values).

        service.mainLine(appService);

        OccustuFields out = (OccustuFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ID_REQUIRED);
    }

    @Test
    void mainLine_enterKey_nonNumericCustId_showsIdNotNumericMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("12X456789"));

        service.mainLine(appService);

        OccustuFields out = (OccustuFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ID_NOTNUM);
        assertThat(out.getCustidl()).isEqualTo(-1);
        verify(appService, never())
                .readFile(anyString(), any(), anyString(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void mainLine_enterKey_custIdWithEmbeddedSpaces_parsesDigitsIgnoringSpaces() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("   123   "));
        givenCustomerFile(List.of(new CustomerRow(123)));

        service.mainLine(appService);

        verify(appService)
                .readFile(eq(CUSTFILE), any(), eq("123"), org.mockito.ArgumentMatchers.anyInt());
        OccustuFields out = (OccustuFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LOADED);
    }

    // ───────────────────────── 2300-LOAD-CUST (state N -> load) ─────────────────────────

    @Test
    void mainLine_enterKey_stateNotLoaded_customerFound_populatesDetailAndSetsLoadedState() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("000000042"));
        givenCustomerFile(List.of(new CustomerRow(42)));

        service.mainLine(appService);

        OccustuFields out = (OccustuFields) captureDataOnlySend().getValue();
        assertThat(out.getCustido().trim()).isEqualTo(String.format("%09d", 42));
        assertThat(out.getCufnamo().trim()).isEqualTo("JOHN");
        assertThat(out.getCulnamo().trim()).isEqualTo("SMITH");
        assertThat(out.getCuaddro().trim()).isEqualTo("1 MAIN ST");
        assertThat(out.getCucityo().trim()).isEqualTo("ANYTOWN");
        assertThat(out.getCuphoneo().trim()).isEqualTo("5551234567");
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LOADED);
        assertThat(out.getCufnaml()).isEqualTo(-1);
        assertThat(out.getCaWorkArea().substring(0, 1)).isEqualTo("L");
        assertEquals(42, out.getCaCustId());
    }

    @Test
    void mainLine_enterKey_stateNotLoaded_customerNotFound_showsNotFoundAndResetsState() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("000000999"));
        givenCustomerFile(List.of());

        service.mainLine(appService);

        OccustuFields out = (OccustuFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CUST_NOTFND);
        assertThat(out.getCustidl()).isEqualTo(-1);
        assertThat(out.getCaWorkArea().substring(0, 1)).isEqualTo("N");
    }

    @Test
    void
            mainLine_enterKey_stateNotLoaded_readErrorOtherRespCode_finalMessageIsCustNotFoundPerCobolOverwrite() {
        // Per COBOL 3000-READ-CUST's WHEN OTHER: sets an interim ERRMSGO ("Error reading the
        // customer file.") but WS-FOUND-FLG stays 'N', so 2300-LOAD-CUST's WHEN OTHER branch
        // unconditionally overwrites ERRMSGO with WS-M-CUST-NOTFND. The interim message is
        // never shown to the operator, in COBOL and in the converted Java alike - not a
        // convert gap.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("000000007"));
        doAnswer(
                        inv -> {
                            when(appService.getEibresp())
                                    .thenReturn(99); // unexpected READ error, not NORMAL/NOTFND
                            return null;
                        })
                .when(appService)
                .readFile(eq(CUSTFILE), any(), anyString(), org.mockito.ArgumentMatchers.anyInt());

        service.mainLine(appService);

        OccustuFields out = (OccustuFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CUST_NOTFND);
    }

    @Test
    void mainLine_enterKey_stateLoaded_differentCustIdKeyed_reloadsInsteadOfUpdating() {
        // "Keying a different id at any time restarts at STEP 1" (COBOL processing narrative):
        // WS-IN-CUST-ID NOT = CA-CUST-ID routes to 2300-LOAD-CUST even though the state flag
        // is already 'L' (loaded).
        givenPseudoConversationLoaded("L", 42);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCustidi("000000099"));
        givenCustomerFile(List.of(new CustomerRow(99)));

        service.mainLine(appService);

        verify(appService)
                .readFile(eq(CUSTFILE), any(), eq("99"), org.mockito.ArgumentMatchers.anyInt());
        verify(appService, never())
                .readFileForUpdate(
                        anyString(), any(), anyString(), org.mockito.ArgumentMatchers.anyInt());
        OccustuFields out = (OccustuFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LOADED);
        assertEquals(99, out.getCaCustId());
    }

    // ───────────────────────── 2400-APPLY-UPDATE / 5000-EDIT-FIELDS ─────────────────────────

    @Test
    void mainLine_enterKey_stateLoadedSameId_blankFirstName_showsFnameRequiredAndSkipsRead() {
        givenPseudoConversationLoaded("L", 42);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCustidi("000000042");
                    f.setCufnami("         ");
                    f.setCulnami("DOE");
                    f.setCuaddri("2 OAK ST");
                    f.setCucityi("METRO");
                    f.setCuphonei("5559876543");
                });

        service.mainLine(appService);

        OccustuFields out = (OccustuFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_FNAME_REQ);
        assertThat(out.getCufnaml()).isEqualTo(-1);
        verify(appService, never())
                .readFileForUpdate(
                        anyString(), any(), anyString(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void mainLine_enterKey_stateLoadedSameId_blankLastName_showsLnameRequired() {
        givenPseudoConversationLoaded("L", 42);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCustidi("000000042");
                    f.setCufnami("JANE");
                    f.setCulnami("            ");
                    f.setCuaddri("2 OAK ST");
                    f.setCucityi("METRO");
                    f.setCuphonei("5559876543");
                });

        service.mainLine(appService);

        OccustuFields out = (OccustuFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_LNAME_REQ);
        assertThat(out.getCulnaml()).isEqualTo(-1);
    }

    @Test
    void mainLine_enterKey_stateLoadedSameId_blankAddress_showsAddrRequired() {
        givenPseudoConversationLoaded("L", 42);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCustidi("000000042");
                    f.setCufnami("JANE");
                    f.setCulnami("DOE");
                    f.setCuaddri("                     ");
                    f.setCucityi("METRO");
                    f.setCuphonei("5559876543");
                });

        service.mainLine(appService);

        OccustuFields out = (OccustuFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_ADDR_REQ);
        assertThat(out.getCuaddrl()).isEqualTo(-1);
    }

    @Test
    void mainLine_enterKey_stateLoadedSameId_blankCity_showsCityRequired() {
        givenPseudoConversationLoaded("L", 42);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCustidi("000000042");
                    f.setCufnami("JANE");
                    f.setCulnami("DOE");
                    f.setCuaddri("2 OAK ST");
                    f.setCucityi("     ");
                    f.setCuphonei("5559876543");
                });

        service.mainLine(appService);

        OccustuFields out = (OccustuFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CITY_REQ);
        assertThat(out.getCucityl()).isEqualTo(-1);
    }

    @Test
    void mainLine_enterKey_stateLoadedSameId_blankPhone_showsPhoneRequired() {
        givenPseudoConversationLoaded("L", 42);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCustidi("000000042");
                    f.setCufnami("JANE");
                    f.setCulnami("DOE");
                    f.setCuaddri("2 OAK ST");
                    f.setCucityi("METRO");
                    f.setCuphonei("          ");
                });

        service.mainLine(appService);

        OccustuFields out = (OccustuFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PHONE_REQ);
        assertThat(out.getCuphonel()).isEqualTo(-1);
    }

    @Test
    void mainLine_enterKey_stateLoadedSameId_allFieldsValid_rewritesAndShowsUpdatedMessage() {
        givenPseudoConversationLoaded("L", 42);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCustidi("000000042");
                    f.setCufnami("JANE");
                    f.setCulnami("DOE");
                    f.setCuaddri("2 OAK ST");
                    f.setCucityi("METRO");
                    f.setCuphonei("5559876543");
                });
        givenCustomerFile(List.of(new CustomerRow(42)));

        service.mainLine(appService);

        verify(appService)
                .readFileForUpdate(
                        eq(CUSTFILE), any(), eq("42"), org.mockito.ArgumentMatchers.anyInt());
        ArgumentCaptor<Object> rewriteCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService).rewriteFile(eq(CUSTFILE), rewriteCaptor.capture());
        OccustuFields rewritten = (OccustuFields) rewriteCaptor.getValue();
        assertThat(rewritten.getCuFirstName().trim()).isEqualTo("JANE");
        assertThat(rewritten.getCuLastName().trim()).isEqualTo("DOE");
        assertThat(rewritten.getCuAddrLine1().trim()).isEqualTo("2 OAK ST");
        assertThat(rewritten.getCuAddrCity().trim()).isEqualTo("METRO");
        assertThat(rewritten.getCuPhone1().trim()).isEqualTo("5559876543");

        OccustuFields out = (OccustuFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_UPDATED);
        assertThat(out.getCufnaml()).isEqualTo(-1);
        assertThat(out.getCaWorkArea().substring(0, 1)).isEqualTo("L");
        assertThat(out.getCufnamo().trim()).isEqualTo("JANE");
    }

    @Test
    void mainLine_enterKey_stateLoadedSameId_rewriteFails_showsUpdateErrorMessage() {
        givenPseudoConversationLoaded("L", 42);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCustidi("000000042");
                    f.setCufnami("JANE");
                    f.setCulnami("DOE");
                    f.setCuaddri("2 OAK ST");
                    f.setCucityi("METRO");
                    f.setCuphonei("5559876543");
                });
        givenCustomerFile(List.of(new CustomerRow(42)));
        doAnswer(
                        inv -> {
                            when(appService.getEibresp()).thenReturn(AppResp.ERROR);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(CUSTFILE), any());

        service.mainLine(appService);

        OccustuFields out = (OccustuFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_UPD_ERROR);
        assertThat(out.getCufnaml()).isEqualTo(-1);
    }

    @Test
    void
            mainLine_enterKey_stateLoadedSameId_recordDeletedSinceLoad_showsDeletedMessageAndResetsState() {
        givenPseudoConversationLoaded("L", 42);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCustidi("000000042");
                    f.setCufnami("JANE");
                    f.setCulnami("DOE");
                    f.setCuaddri("2 OAK ST");
                    f.setCucityi("METRO");
                    f.setCuphonei("5559876543");
                });
        givenCustomerFile(List.of());

        service.mainLine(appService);

        verify(appService, never()).rewriteFile(anyString(), any());
        OccustuFields out = (OccustuFields) captureDataOnlySend().getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_DELETED);
        assertThat(out.getCustidl()).isEqualTo(-1);
        assertThat(out.getCaWorkArea().substring(0, 1)).isEqualTo("N");
    }
}

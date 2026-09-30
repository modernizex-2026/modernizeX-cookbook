package com.generated.orion.odacctu.service;

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

import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.odacctu.accessor.OdacctuFields;
import com.generated.orion.odacctu.dao.OdacctuDao;
import com.generated.orion.odacctu.model.WorkingStorage;

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
 * Unit tests for OdacctuService, generated from COBOL program ODACCTU (DB2 variant of the OCACCTU
 * online account-update program). All business logic is private and is exercised solely through the
 * public {@link OdacctuService#mainLine(AppService)} entry point, driven by an {@link AppService}
 * mock that emulates CICS SEND/RECEIVE/XCTL/RETURN, and an {@link OdacctuDao} mock that emulates
 * the EXEC SQL SELECT/UPDATE calls.
 *
 * <p>Convert-gap check: every COBOL paragraph (0000-MAIN..9000-RETURN) maps 1:1 onto its Java
 * counterpart, including the "final message overwrite" pattern in 2100-FETCH-ACCT/2200-UPDATE-ACCT
 * (the interim "Error reading/updating account table." message set inside 3000-READ-ACCT /
 * 3100-UPDATE-ACCT is unconditionally replaced by WS-MSG-NOTFND once control returns to the caller,
 * in COBOL and in the converted Java alike). No behavioral divergence was found against the COBOL
 * ground truth, so no CONVERT-GAP test is included here.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OdacctuServiceTest {

    private static final String MAP_NAME = "MACCTUA";
    private static final String TRAN_ID = "OD02";
    private static final String MENU_PGM = "OCMENU";
    private static final int COMMAREA_LENGTH = 692;

    private static final String MSG_PROMPT = "Enter account id and press ENTER to fetch.";
    private static final String MSG_AMEND = "Amend fields and press PF5 to update.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_NOTFND = "Record not found.";
    private static final String MSG_REQUIRED = "Please enter all required fields.";
    private static final String HDR_TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM";

    @Mock private OdacctuDao dao;

    @Mock private AppService appService;

    @InjectMocks private OdacctuService service;

    @BeforeEach
    void setUp() {
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-23", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT / mode flag. */
    private String commareaWith(int context, String modeFlag) {
        OdacctuFields helper = new OdacctuFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        if (modeFlag != null) {
            helper.setCaWorkArea(modeFlag);
        }
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWith(context, null));
    }

    /** Same as above, but also seeds CA-WORK-AREA(1:1) so 2200-UPDATE-ACCT sees MODE-UPDATE. */
    private void givenPseudoConversationInUpdateMode() {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWith(1, "U"));
    }

    /** Stubs receiveMap to populate the MACCTUAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OdacctuFields> populate) {
        doAnswer(
                        inv -> {
                            OdacctuFields f = inv.getArgument(1);
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
     * from the 16-byte WS-ED-AMT edited field into the 13-byte ACCRLIMO/ACCSLIMO screen field,
     * which COBOL (and the converted Java) truncates on the right by 3 bytes. Ground truth, not a
     * convert gap.
     */
    private String editedAmount(BigDecimal amount) {
        OdacctuFields helper = new OdacctuFields(new WorkingStorage());
        helper.setAcCreditLimit(amount);
        helper.setWsEdAmt(helper.getAcCreditLimit());
        helper.setAccrlimo(helper.getString("WS-ED-AMT"));
        return helper.getAccrlimo();
    }

    private Map<String, Object> selectResultFound(
            String status,
            BigDecimal creditLimit,
            BigDecimal cashLimit,
            String expiryDate,
            String groupId) {
        Map<String, Object> result = new HashMap<>();
        result.put("AC_ACTIVE_STATUS", status);
        result.put("AC_CREDIT_LIMIT", creditLimit);
        result.put("AC_CASH_LIMIT", cashLimit);
        result.put("AC_EXPIRY_DATE", expiryDate);
        result.put("AC_GROUP_ID", groupId);
        return result;
    }

    private Map<String, Object> sqlcodeResult(int sqlcode) {
        Map<String, Object> result = new HashMap<>();
        result.put("SQLCODE", sqlcode);
        return result;
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OdacctuFields out = (OdacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo(TRAN_ID);
        assertThat(out.getPgmnameo().trim()).isEqualTo("ODACCTU");
        assertThat(out.getTitleo().trim()).isEqualTo(HDR_TITLE);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-23");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        verify(appService).returnTransid(eq(TRAN_ID), any(), eq(COMMAREA_LENGTH));
    }

    @Test
    void mainLine_pseudoConversationalContextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(false));
        verify(appService).returnTransid(eq(TRAN_ID), any(), eq(COMMAREA_LENGTH));
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

        OdacctuFields decoded = new OdacctuFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo("ODACCTU");
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo(TRAN_ID);
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
        OdacctuFields out = (OdacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        // 2000-PROCESS-INPUT's WHEN OTHER performs 1000-SEND-INITIAL (erase=true) then
        // 8100-SEND-DATAONLY (erase=false); both calls share the same mutable field
        // accessor, so only the final (dataonly) call's content can be inspected.
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
        OdacctuFields dataOnlyScreen = (OdacctuFields) dataOnlyOut.getValue();
        assertThat(dataOnlyScreen.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-FETCH-ACCT ─────────────────────────

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
        OdacctuFields out = (OdacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(dao, never()).selectOrionAcct(any());
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
        OdacctuFields out = (OdacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_REQUIRED);
        verify(dao, never()).selectOrionAcct(any());
    }

    @Test
    void mainLine_enterKey_accountIdNotNumeric_showsMustBeNumericMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("ABCDEFGHIJK"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdacctuFields out = (OdacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo("Account id must be numeric.");
        verify(dao, never()).selectOrionAcct(any());
    }

    @Test
    void mainLine_enterKey_accountFound_populatesDetailSetsUpdateModeAndAmendMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("00000012345"));
        BigDecimal creditLimit = new BigDecimal("5000.00");
        BigDecimal cashLimit = new BigDecimal("1000.00");
        when(dao.selectOrionAcct(any()))
                .thenReturn(selectResultFound("Y", creditLimit, cashLimit, "2028-01-31", "STD001"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdacctuFields out = (OdacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_AMEND);
        assertThat(out.getAcctido()).isEqualTo(String.format("%011d", 12345L));
        assertThat(out.getAcstato().trim()).isEqualTo("Y");
        assertThat(out.getAccrlimo()).isEqualTo(editedAmount(creditLimit));
        assertThat(out.getAccslimo()).isEqualTo(editedAmount(cashLimit));
        assertThat(out.getAcexpo().trim()).isEqualTo("2028-01-31");
        assertThat(out.getAcgrpo().trim()).isEqualTo("STD001");
    }

    @Test
    void mainLine_enterKey_accountNotFound_showsNotFoundMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("00000099999"));
        when(dao.selectOrionAcct(any())).thenReturn(sqlcodeResult(100));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdacctuFields out = (OdacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOTFND);
    }

    @Test
    void mainLine_enterKey_selectSqlError_finalMessageIsStillNotFoundPerCobolOverwrite() {
        // Per COBOL 3000-READ-ACCT: WHEN OTHER sets an interim ERRMSGO ("Error reading
        // account table.") but leaves REC-NOT-FOUND set; 2100-FETCH-ACCT's ELSE branch
        // (not REC-FOUND) then unconditionally overwrites ERRMSGO with WS-MSG-NOTFND. So
        // the interim message is never shown to the operator, in COBOL and in the
        // converted Java alike — not a convert gap.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setAcctidi("00000011111"));
        when(dao.selectOrionAcct(any())).thenReturn(sqlcodeResult(-904));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdacctuFields out = (OdacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOTFND);
    }

    // ───────────────────────── 2200-UPDATE-ACCT / 2300-EDIT-INPUT ─────────────────────────

    @Test
    void mainLine_pf5Pressed_modeNotUpdate_showsPressEnterMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("5");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdacctuFields out = (OdacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo("Press ENTER to fetch a row before PF5.");
        verify(dao, never()).updateOrionAcct(any());
    }

    @Test
    void mainLine_pf5Pressed_invalidAcctId_showsRequiredAndNumericMessage() {
        givenPseudoConversationInUpdateMode();
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(f -> f.setAcctidi("           "));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdacctuFields out = (OdacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo("Account id is required and numeric.");
        verify(dao, never()).updateOrionAcct(any());
    }

    @Test
    void mainLine_pf5Pressed_invalidStatus_showsStatusMustBeYNMessage() {
        givenPseudoConversationInUpdateMode();
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("00000012345");
                    f.setAcstati("X");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdacctuFields out = (OdacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo("Status must be Y or N.");
        verify(dao, never()).updateOrionAcct(any());
    }

    @Test
    void mainLine_pf5Pressed_missingLimits_showsLimitsRequiredMessage() {
        givenPseudoConversationInUpdateMode();
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("00000012345");
                    f.setAcstati("Y");
                    f.setAccrlimi("             ");
                    f.setAccslimi("             ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdacctuFields out = (OdacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo("Credit and cash limits are required.");
        verify(dao, never()).updateOrionAcct(any());
    }

    @Test
    void
            mainLine_pf5Pressed_validInput_updateSucceeds_showsSuccessMessageAndCallsDaoWithParsedValues() {
        givenPseudoConversationInUpdateMode();
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("00000012345");
                    f.setAcstati("Y");
                    f.setAccrlimi("5000.00");
                    f.setAccslimi("1000.00");
                    f.setAcexpi("2028-01-31");
                    f.setAcgrpi("STD001");
                });
        when(dao.updateOrionAcct(any(), any(), any(), any(), any(), any()))
                .thenReturn(sqlcodeResult(0));

        service.mainLine(appService);

        ArgumentCaptor<Object> idCap = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Object> statusCap = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Object> creditCap = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Object> cashCap = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Object> expiryCap = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Object> groupCap = ArgumentCaptor.forClass(Object.class);
        verify(dao)
                .updateOrionAcct(
                        idCap.capture(),
                        statusCap.capture(),
                        creditCap.capture(),
                        cashCap.capture(),
                        expiryCap.capture(),
                        groupCap.capture());
        assertThat(idCap.getValue()).isEqualTo(12345L);
        assertThat(((String) statusCap.getValue()).trim()).isEqualTo("Y");
        assertThat((BigDecimal) creditCap.getValue()).isEqualByComparingTo("5000.00");
        assertThat((BigDecimal) cashCap.getValue()).isEqualByComparingTo("1000.00");
        assertThat(((String) expiryCap.getValue()).trim()).isEqualTo("2028-01-31");
        assertThat(((String) groupCap.getValue()).trim()).isEqualTo("STD001");

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdacctuFields out = (OdacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo("Account updated successfully.");
    }

    @Test
    void mainLine_pf5Pressed_validInput_updateNotFound_showsNotFoundMessage() {
        givenPseudoConversationInUpdateMode();
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("00000099999");
                    f.setAcstati("N");
                    f.setAccrlimi("2500.00");
                    f.setAccslimi("500.00");
                    f.setAcexpi("2027-06-30");
                    f.setAcgrpi("VIP001");
                });
        when(dao.updateOrionAcct(any(), any(), any(), any(), any(), any()))
                .thenReturn(sqlcodeResult(100));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdacctuFields out = (OdacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOTFND);
    }

    @Test
    void mainLine_pf5Pressed_updateSqlError_finalMessageIsStillNotFoundPerCobolOverwrite() {
        // Per COBOL 3100-UPDATE-ACCT: WHEN OTHER sets an interim ERRMSGO ("Error updating
        // account table.") but leaves REC-NOT-FOUND set; 2200-UPDATE-ACCT then
        // unconditionally overwrites ERRMSGO with WS-MSG-NOTFND on any non-success path —
        // same overwrite pattern as the read path, not a convert gap.
        givenPseudoConversationInUpdateMode();
        when(appService.getEibaid()).thenReturn("5");
        givenReceiveMapPopulates(
                f -> {
                    f.setAcctidi("00000012345");
                    f.setAcstati("Y");
                    f.setAccrlimi("5000.00");
                    f.setAccslimi("1000.00");
                    f.setAcexpi("2028-01-31");
                    f.setAcgrpi("STD001");
                });
        when(dao.updateOrionAcct(any(), any(), any(), any(), any(), any()))
                .thenReturn(sqlcodeResult(-904));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdacctuFields out = (OdacctuFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_NOTFND);
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOdacctu() {
        assertEquals("ODACCTU", service.getProgramName());
    }

    @Test
    void getTransId_returnsOd02() {
        assertEquals(TRAN_ID, service.getTransId());
    }

    @Test
    void getButtonDefs_delegatesToBmsMetadata() {
        assertThat(service.getButtonDefs()).isNotNull();
    }

    @Test
    void getFieldMapping_delegatesToBmsMetadataForMacctuaMap() {
        assertThat(service.getFieldMapping(MAP_NAME)).isNotNull();
    }

    @Test
    void registerFsetFields_delegatesToBmsMetadataWithoutThrowing() {
        AppRunner runner = new AppRunner(Map.of(), Map.of());

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> service.registerFsetFields(runner));
    }
}

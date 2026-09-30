package com.generated.orion.odtrana.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.odtrana.accessor.OdtranaFields;
import com.generated.orion.odtrana.dao.OdtranaDao;
import com.generated.orion.odtrana.model.WorkingStorage;

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
 * Unit tests for OdtranaService, generated from COBOL program ODTRANA (DB2 on-line transaction-add
 * screen). All business logic is private and is exercised solely through the public {@link
 * OdtranaService#mainLine(AppService)} entry point, driven by an {@link AppService} mock (CICS
 * SEND/RECEIVE/XCTL/RETURN) and an {@link OdtranaDao} mock (EXEC SQL SELECT/UPDATE/INSERT against
 * DB2).
 *
 * <p>No convert-gap was found between ODTRANA.cbl and OdtranaService: paragraph 0000-MAIN through
 * 9000-RETURN map 1:1, including the COBOL quirk (preserved identically in Java) where
 * 3000-CHECK-CARD/3100-CHECK-TYPE's own "Error reading ... table." message is always overwritten by
 * 2100-ADD-TRAN's ELSE branch, and where 3200-GET-NEXT-ID's UPDATE result and
 * buildTransactionRecord/insertTransaction are never gated on a prior found-flag from the counter
 * read.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OdtranaServiceTest {

    private static final String MAP_NAME = "MTRANAA";
    private static final String MENU_PGM = "OCMENU";
    private static final String TRANID = "OD06";
    private static final String PGMNAME = "ODTRANA";
    private static final String HDR_TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM";
    private static final String MSG_PROMPT = "Enter transaction detail and press ENTER.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_CARD_REQ = "Card number is required.";
    private static final String MSG_TYPE_REQ = "Transaction type is required.";
    private static final String MSG_CAT_NUM = "Category must be numeric.";
    private static final String MSG_AMT_REQ = "Amount is required.";
    private static final String MSG_CARD_NOTFOUND = "Card number not found.";
    private static final String MSG_TYPE_NOTFOUND = "Transaction type not found.";
    private static final String MSG_INSERT_ERROR = "Error inserting transaction row.";

    @Mock private AppService appService;

    @Mock private OdtranaDao dao;

    @InjectMocks private OdtranaService service;

    @BeforeEach
    void setUp() {
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commareaWithContext(int context) {
        OdtranaFields helper = new OdtranaFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(context));
    }

    /** Stubs receiveMap to populate the MTRANAAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OdtranaFields> populate) {
        lenient()
                .doAnswer(
                        inv -> {
                            OdtranaFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    private static Map<String, Object> sqlSuccess(String key, Object value) {
        Map<String, Object> m = new HashMap<>();
        m.put(key, value);
        return m;
    }

    private static Map<String, Object> sqlError(int sqlcode) {
        Map<String, Object> m = new HashMap<>();
        m.put("SQLCODE", sqlcode);
        return m;
    }

    /** Stubs a valid card/type/counter/insert chain leading to a successful add. */
    private void givenFullHappyPathDao(long ctrlLastValue) {
        lenient().when(dao.selectOrionCard(any())).thenReturn(sqlSuccess("CD_ACTIVE_STATUS", "A"));
        lenient().when(dao.selectOrionTtyp(any())).thenReturn(sqlSuccess("TT_DESC", "Purchase"));
        lenient()
                .when(dao.selectOrionCtrl(any()))
                .thenReturn(sqlSuccess("CT_LAST_VALUE", ctrlLastValue));
        lenient().when(dao.updateOrionCtrl(any(), any())).thenReturn(sqlError(0));
        lenient()
                .when(
                        dao.insertOrionTran(
                                any(), any(), any(), any(), any(), any(), any(), any(), any(),
                                any(), any(), any(), any()))
                .thenReturn(sqlError(0));
    }

    private void givenAllFieldsValid() {
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("PU");
                    f.setTrcati("0010");
                    f.setTramti("125.50");
                    f.setTrmerchi("ACME STORE");
                    f.setTrdesci("Groceries");
                });
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OdtranaFields out = (OdtranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo(TRANID);
        assertThat(out.getPgmnameo().trim()).isEqualTo(PGMNAME);
        assertThat(out.getTitleo().trim()).isEqualTo(HDR_TITLE);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-22");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        assertEquals(1, out.getCaPgmContext());
        verify(appService).returnTransid(eq(TRANID), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(false));
        verify(appService).returnTransid(eq(TRANID), any(), eq(692));
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToMenuWithFromProgramInfo() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        ArgumentCaptor<Object> commareaCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .xctl(argThat(s -> s.trim().equals(MENU_PGM)), commareaCaptor.capture(), eq(692));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());

        OdtranaFields decoded = new OdtranaFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo(PGMNAME);
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
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OdtranaFields out = (OdtranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        // 2000-PROCESS-INPUT's WHEN OTHER performs 1000-SEND-INITIAL (erase=true) then
        // 8100-SEND-DATAONLY (erase=false); only the final (dataonly) call's content
        // survives to be inspected since both share the same mutable field accessor.
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
        OdtranaFields dataOnlyScreen = (OdtranaFields) dataOnlyOut.getValue();
        assertThat(dataOnlyScreen.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2200-EDIT-INPUT ─────────────────────────

    @Test
    void mainLine_enterKey_cardNumberBlank_showsCardRequiredAndSkipsDao() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi(" "));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdtranaFields out = (OdtranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARD_REQ);
        verify(dao, never()).selectOrionCard(any());
    }

    @Test
    void mainLine_enterKey_cardNumberAllLowValues_showsCardRequired() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.fillLowValues("CARDNUMI"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdtranaFields out = (OdtranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARD_REQ);
    }

    @Test
    void mainLine_enterKey_typeBlank_showsTypeRequired() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("  ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdtranaFields out = (OdtranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TYPE_REQ);
        verify(dao, never()).selectOrionCard(any());
    }

    @Test
    void mainLine_enterKey_categoryBlank_showsCategoryMustBeNumeric() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("PU");
                    f.setTrcati("    ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdtranaFields out = (OdtranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CAT_NUM);
    }

    @Test
    void mainLine_enterKey_categoryNonNumeric_showsCategoryMustBeNumeric() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("PU");
                    f.setTrcati("AB1C");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdtranaFields out = (OdtranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CAT_NUM);
        verify(dao, never()).selectOrionCard(any());
    }

    @Test
    void mainLine_enterKey_amountBlank_showsAmountRequired() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("PU");
                    f.setTrcati("0010");
                    f.setTramti("            ");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdtranaFields out = (OdtranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_AMT_REQ);
        verify(dao, never()).selectOrionCard(any());
    }

    @Test
    void mainLine_enterKey_amountAllLowValues_showsAmountRequired() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("PU");
                    f.setTrcati("0010");
                    f.fillLowValues("TRAMTI");
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdtranaFields out = (OdtranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_AMT_REQ);
    }

    // ───────────────────────── 3000-CHECK-CARD ─────────────────────────

    @Test
    void mainLine_enterKey_cardNotOnFile_showsCardNotFoundAndSkipsTypeCheck() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenAllFieldsValid();
        when(dao.selectOrionCard(any())).thenReturn(sqlError(100));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdtranaFields out = (OdtranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARD_NOTFOUND);
        verify(dao, never()).selectOrionTtyp(any());
    }

    @Test
    void mainLine_enterKey_cardDbError_stillShowsCardNotFound() {
        // COBOL ground truth: 3000-CHECK-CARD's WHEN OTHER sets ERRMSGO to
        // 'Error reading card table.' but 2100-ADD-TRAN's ELSE branch unconditionally
        // overwrites it with 'Card number not found.' whenever REC-NOT-FOUND — this
        // makes the inner message unreachable in both COBOL and the converted Java,
        // so it is NOT a convert gap, just an identically-preserved COBOL quirk.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenAllFieldsValid();
        when(dao.selectOrionCard(any())).thenReturn(sqlError(-911));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdtranaFields out = (OdtranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARD_NOTFOUND);
    }

    // ───────────────────────── 3100-CHECK-TYPE ─────────────────────────

    @Test
    void mainLine_enterKey_typeNotOnFile_showsTypeNotFoundAndSkipsCounter() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenAllFieldsValid();
        when(dao.selectOrionCard(any())).thenReturn(sqlSuccess("CD_ACTIVE_STATUS", "A"));
        when(dao.selectOrionTtyp(any())).thenReturn(sqlError(100));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdtranaFields out = (OdtranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TYPE_NOTFOUND);
        verify(dao, never()).selectOrionCtrl(any());
    }

    // ───────────────────────── 3200-GET-NEXT-ID / 3300-BUILD-RECORD / 3400-INSERT-TRAN
    // ─────────────────────────

    @Test
    void mainLine_allEditsPass_ctrlMissing_defaultsIdToOneAndStillInserts() {
        // COBOL ground truth: 3200-GET-NEXT-ID's ELSE (SQLCODE<>0) just MOVEs 1 to
        // WS-CT-VALUE — there is no found-flag gate here, so 3300/3400 always run.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenAllFieldsValid();
        when(dao.selectOrionCard(any())).thenReturn(sqlSuccess("CD_ACTIVE_STATUS", "A"));
        when(dao.selectOrionTtyp(any())).thenReturn(sqlSuccess("TT_DESC", "Purchase"));
        when(dao.selectOrionCtrl(any())).thenReturn(sqlError(100));
        when(dao.insertOrionTran(
                        any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any()))
                .thenReturn(sqlError(0));

        service.mainLine(appService);

        verify(dao, never()).updateOrionCtrl(any(), any());
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdtranaFields out = (OdtranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo("Transaction added. Id=0000000000000001");
        verify(dao)
                .insertOrionTran(
                        eq("0000000000000001"),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any());
    }

    @Test
    void mainLine_allEditsPass_updateCtrlFails_insertStillProceedsWithIncrementedId() {
        // COBOL ground truth: EXEC SQL UPDATE ORION.CTRL has no SQLCODE check at all —
        // the incremented WS-CT-VALUE is used for TR-ID regardless of the UPDATE outcome.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenAllFieldsValid();
        givenFullHappyPathDao(41L);
        when(dao.updateOrionCtrl(any(), any())).thenReturn(sqlError(-904));

        service.mainLine(appService);

        verify(dao).updateOrionCtrl(any(), eq(42L));
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdtranaFields out = (OdtranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo("Transaction added. Id=0000000000000042");
    }

    @Test
    void mainLine_allEditsPass_insertFails_showsInsertErrorAndNoSuccessMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenAllFieldsValid();
        givenFullHappyPathDao(41L);
        when(dao.insertOrionTran(
                        any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any()))
                .thenReturn(sqlError(-803));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdtranaFields out = (OdtranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INSERT_ERROR);
    }

    @Test
    void mainLine_allEditsPass_insertSucceeds_buildsRecordAndConfirmsAdd() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenAllFieldsValid();
        givenFullHappyPathDao(41L);

        service.mainLine(appService);

        ArgumentCaptor<String> trTypeCd = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> trCatCd = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> trSource = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> trDesc = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BigDecimal> trAmt = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<Integer> trMerchantId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> trMerchantName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> trMerchantCity = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> trMerchantZip = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> trCardNum = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> trOrigTs = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> trProcTs = ArgumentCaptor.forClass(String.class);
        verify(dao)
                .insertOrionTran(
                        eq("0000000000000042"),
                        trTypeCd.capture(),
                        trCatCd.capture(),
                        trSource.capture(),
                        trDesc.capture(),
                        trAmt.capture(),
                        trMerchantId.capture(),
                        trMerchantName.capture(),
                        trMerchantCity.capture(),
                        trMerchantZip.capture(),
                        trCardNum.capture(),
                        trOrigTs.capture(),
                        trProcTs.capture());

        assertThat(trTypeCd.getValue()).isEqualTo("PU");
        assertThat(trCatCd.getValue()).isEqualTo(10);
        assertThat(trSource.getValue().trim()).isEqualTo("ONLINE");
        assertThat(trDesc.getValue().trim()).isEqualTo("Groceries");
        assertThat(trAmt.getValue()).isEqualByComparingTo(new BigDecimal("125.50"));
        assertThat(trMerchantId.getValue()).isEqualTo(0);
        assertThat(trMerchantName.getValue().trim()).isEqualTo("ACME STORE");
        assertThat(trMerchantCity.getValue().trim()).isEmpty();
        assertThat(trMerchantZip.getValue().trim()).isEmpty();
        assertThat(trCardNum.getValue().trim()).isEqualTo("1234567890123456");
        assertThat(trOrigTs.getValue().trim()).isEqualTo("2026-09-22 10:00:00");
        assertThat(trProcTs.getValue().trim()).isEqualTo("2026-09-22 10:00:00");

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OdtranaFields out = (OdtranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo("Transaction added. Id=0000000000000042");
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOdtrana() {
        assertEquals("ODTRANA", service.getProgramName());
    }

    @Test
    void getTransId_returnsOd06() {
        assertEquals("OD06", service.getTransId());
    }

    @Test
    void getButtonDefs_returnsThreeButtons() {
        assertEquals(3, service.getButtonDefs().size());
    }

    @Test
    void getFieldMapping_forMapName_returnsMapping() {
        assertThat(service.getFieldMapping("MTRANAA")).isNotNull();
    }

    @Test
    void mainLine_alwaysReturnsToTranidOd06() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        verify(appService, times(1)).returnTransid(eq(TRANID), any(), eq(692));
    }
}

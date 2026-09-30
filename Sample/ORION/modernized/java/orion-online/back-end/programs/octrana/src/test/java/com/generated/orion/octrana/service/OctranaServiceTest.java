package com.generated.orion.octrana.service;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.octrana.accessor.OctranaFields;
import com.generated.orion.octrana.model.WorkingStorage;

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
 * Unit tests for OctranaService, generated from COBOL program OCTRANA (transaction-add screen,
 * follows the OCACCTV golden on-line skeleton). All business logic is private and is exercised
 * solely through the public {@link OctranaService#mainLine(AppService)} entry point, driven by an
 * {@link AppService} mock that emulates CICS SEND/RECEIVE/READ/REWRITE/WRITE/XCTL/RETURN.
 *
 * <p>Convert-gap check: OCTRANA's Java service mirrors every COBOL paragraph 1:1
 * (0000-MAIN..9000-RETURN). One divergence was found: 7700-VALIDATE-AMT/FUNCTION NUMVAL accepts
 * grouping commas in COBOL, but the converted amount parsing (shared {@code Utility.parseNumeric})
 * does not strip commas before calling {@code new BigDecimal(...)}, silently defaulting
 * comma-bearing amounts to zero — see {@code
 * mainLine_enterKey_amountHasEmbeddedSpaceAndComma_convertGapCommaNotStrippedByNumval}.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OctranaServiceTest {

    private static final String MAP_NAME = "MTRANAA";
    private static final String XREFFILE = "XREFFILE";
    private static final String TTYPFILE = "TTYPFILE";
    private static final String TCATFILE = "TCATFILE";
    private static final String CTRLFILE = "CTRLFILE";
    private static final String TRANFILE = "TRANFILE";
    private static final String MENU_PGM = "OCMENU";

    private static final String MSG_PROMPT = "Enter the transaction details and press ENTER.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_CARD_REQ = "Card number is required.";
    private static final String MSG_CARD_BAD = "Card number is not on file.";
    private static final String MSG_TYPE_REQ = "Transaction type is required.";
    private static final String MSG_TYPE_BAD = "Transaction type is not valid.";
    private static final String MSG_CAT_REQ = "Category code is required.";
    private static final String MSG_CAT_NUM = "Category code must be numeric.";
    private static final String MSG_CAT_BAD = "Category is not valid for that type.";
    private static final String MSG_AMT_REQ = "Amount is required.";
    private static final String MSG_AMT_BAD = "Amount is not a valid number.";
    private static final String MSG_AMT_ZERO = "Amount must be greater than zero.";
    private static final String MSG_MERCH_REQ = "Merchant name is required.";
    private static final String MSG_DESC_REQ = "Description is required.";
    private static final String MSG_CTR_MISSING = "Transaction counter is not defined.";
    private static final String MSG_CTR_ERROR = "Error updating the transaction counter.";
    private static final String MSG_WRITE_ERROR = "Error writing the transaction record.";
    private static final String HDR_TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM";

    @Mock private AppService appService;

    private OctranaService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OctranaService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commareaWithContext(int context) {
        OctranaFields helper = new OctranaFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(context));
    }

    /**
     * Replicates COBOL's "STRING WS-M-ADD-PREFIX TR-ID WS-M-ADD-SUFFIX INTO ERRMSGO"
     * (2200-CONFIRM-ADD) on an isolated accessor so the expected confirmation message is derived
     * the same way production builds it, including the internal padding from the fixed-width
     * WS-M-ADD-PREFIX/WS-M-ADD-SUFFIX literals. Ground truth, not a convert gap.
     */
    private String confirmMessage(long tranId) {
        OctranaFields helper = new OctranaFields(new WorkingStorage());
        StringBuilder sb = new StringBuilder();
        sb.append(helper.getWsMAddPrefix());
        sb.append(String.format("%016d", tranId));
        sb.append(helper.getWsMAddSuffix());
        return sb.toString().trim();
    }

    /** Stubs receiveMap to populate the MTRANAAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OctranaFields> populate) {
        doAnswer(
                        inv -> {
                            OctranaFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    /** Populates a valid, all-edits-pass MTRANAAI input set (card/type/cat/amt/merch/desc). */
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
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(XREFFILE), any(), eq("1234567890123456"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TTYPFILE), any(), eq("PU"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TCATFILE), any(), eq("PU|10"), anyInt());
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORTA");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCTRANA");
        assertThat(out.getTitleo().trim()).isEqualTo(HDR_TITLE);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-22");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
        verify(appService).returnTransid(eq("ORTA"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        verify(appService).returnTransid(eq("ORTA"), any(), eq(692));
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

        OctranaFields decoded = new OctranaFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo("OCTRANA");
        assertThat(decoded.getCaFromTranid().trim()).isEqualTo("ORTA");
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
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
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
        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        ArgumentCaptor<Object> dataOnlyOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), dataOnlyOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields dataOnlyScreen = (OctranaFields) dataOnlyOut.getValue();
        assertThat(dataOnlyScreen.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 5100-EDIT-CARD ─────────────────────────

    @Test
    void mainLine_enterKey_cardNumberBlank_showsCardRequiredAndStopsEditChain() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("                "));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARD_REQ);
        verify(appService, never()).readFile(eq(XREFFILE), any(), anyString(), anyInt());
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_cardNumberAllLowValues_showsCardRequired() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.fillLowValues("CARDNUMI"));

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARD_REQ);
    }

    @Test
    void mainLine_enterKey_cardNotOnXref_showsCardBad() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(f -> f.setCardnumi("9999999999999999"));
        doAnswer(
                        inv -> {
                            nextResp.set(13);
                            return null;
                        })
                .when(appService)
                .readFile(eq(XREFFILE), any(), eq("9999999999999999"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CARD_BAD);
        // 5000-EDIT-INPUT stops the chain: type edit is gated on WS-EDIT-GOOD.
        verify(appService, never()).readFile(eq(TTYPFILE), any(), anyString(), anyInt());
    }

    // ───────────────────────── 5200-EDIT-TYPE ─────────────────────────

    @Test
    void mainLine_enterKey_typeBlank_showsTypeRequired() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("  ");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(XREFFILE), any(), eq("1234567890123456"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TYPE_REQ);
    }

    @Test
    void mainLine_enterKey_typeNotOnFile_showsTypeBad() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("ZZ");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(XREFFILE), any(), eq("1234567890123456"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(13);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TTYPFILE), any(), eq("ZZ"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TYPE_BAD);
        verify(appService, never()).readFile(eq(TCATFILE), any(), anyString(), anyInt());
    }

    // ───────────────────────── 5300-EDIT-CAT ─────────────────────────

    @Test
    void mainLine_enterKey_categoryBlank_showsCategoryRequired() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("PU");
                    f.setTrcati("    ");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(XREFFILE), any(), eq("1234567890123456"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TTYPFILE), any(), eq("PU"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CAT_REQ);
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
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(XREFFILE), any(), eq("1234567890123456"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TTYPFILE), any(), eq("PU"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CAT_NUM);
        verify(appService, never()).readFile(eq(TCATFILE), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_categoryNotValidForType_showsCategoryBad() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("PU");
                    f.setTrcati("0099");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(XREFFILE), any(), eq("1234567890123456"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TTYPFILE), any(), eq("PU"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(13);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TCATFILE), any(), eq("PU|99"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CAT_BAD);
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_categoryHasLeadingSpace_stillParsesAndPassesCtrlRead() {
        // 7600-VALIDATE-NUM's per-character loop treats an embedded SPACE as CONTINUE
        // (skips it, does not fail validation) as long as at least one digit is present.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("PU");
                    f.setTrcati(" 010");
                    f.setTramti("125.50");
                    f.setTrmerchi("ACME STORE");
                    f.setTrdesci("Groceries");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(XREFFILE), any(), eq("1234567890123456"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TTYPFILE), any(), eq("PU"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TCATFILE), any(), eq("PU|10"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(13);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(
                        eq(CTRLFILE), any(), argThat(s -> s.trim().equals("TRANID")), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        // Edits all passed (category parsed as 10, found on TCATFILE) so the chain reached
        // 6000-GET-NEXT-ID, which reports the counter-missing message.
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CTR_MISSING);
    }

    @Test
    void mainLine_enterKey_amountHasEmbeddedSpaceAndComma_convertGapCommaNotStrippedByNumval() {
        // CONVERT-GAP: COBOL's 7700-VALIDATE-AMT explicitly permits ',' as a grouping
        // separator (WHEN WS-VAL-CH = ',' CONTINUE) and FUNCTION NUMVAL strips grouping
        // commas before converting, so "1,234.56" is ground-truth-valid and yields 1234.56.
        // The converted Java validateAmountField also accepts the comma character (no
        // WS-EDIT-FLAG failure), but the numeric conversion is delegated to
        // Utility.parseNumeric(), which calls `new BigDecimal(norm)` directly — BigDecimal
        // rejects the comma with NumberFormatException, which parseNumeric silently catches
        // and returns ZERO. WS-AMT-NUM ends up 0.00, so 5400-EDIT-AMT's own "<= 0" check
        // then (incorrectly) reports MSG_AMT_ZERO instead of accepting the transaction.
        // This is a shared-infrastructure gap (Utility.parseNumeric), not specific to this
        // program, but it is only reachable/visible through this program's amount field.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("PU");
                    f.setTrcati("0010");
                    f.setTramti(" 1,234.56");
                    f.setTrmerchi("ACME STORE");
                    f.setTrdesci("Groceries");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(XREFFILE), any(), eq("1234567890123456"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TTYPFILE), any(), eq("PU"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TCATFILE), any(), eq("PU|10"), anyInt());
        doAnswer(
                        inv -> {
                            OctranaFields into = inv.getArgument(1);
                            into.setCtLastValue(41L);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(
                        eq(CTRLFILE), any(), argThat(s -> s.trim().equals("TRANID")), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(CTRLFILE), any());
        ArgumentCaptor<Object> writeFrom = ArgumentCaptor.forClass(Object.class);
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .writeFile(
                        eq(TRANFILE),
                        writeFrom.capture(),
                        eq(String.format("%016d", 42L)),
                        anyInt());

        service.mainLine(appService);

        // Expected per COBOL ground truth: all edits pass, the transaction is written with
        // WS-AMT-NUM = 1234.56, and 2200-CONFIRM-ADD sends the confirmation screen
        // (erase=true). CONVERT-GAP: the Java service instead treats the comma-bearing
        // amount as zero and re-sends the data-only entry screen (erase=false) with
        // MSG_AMT_ZERO — this assertion fails on the current Java, correctly flagging the gap.
        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(true));
        OctranaFields written = (OctranaFields) writeFrom.getValue();
        assertThat(written.getWsAmtNum()).isEqualByComparingTo(new BigDecimal("1234.56"));
    }

    @Test
    void mainLine_enterKey_amountHasLeadingPlusSign_parsesAndSucceeds() {
        // 7700-VALIDATE-AMT's per-character loop permits a leading/trailing '+' sign
        // as a CONTINUE (no-op) character alongside digits and the decimal point.
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("PU");
                    f.setTrcati("0010");
                    f.setTramti("+125.50");
                    f.setTrmerchi("ACME STORE");
                    f.setTrdesci("Groceries");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(XREFFILE), any(), eq("1234567890123456"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TTYPFILE), any(), eq("PU"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TCATFILE), any(), eq("PU|10"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(13);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(
                        eq(CTRLFILE), any(), argThat(s -> s.trim().equals("TRANID")), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CTR_MISSING);
    }

    // ───────────────────────── 5400-EDIT-AMT ─────────────────────────

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
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(XREFFILE), any(), eq("1234567890123456"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TTYPFILE), any(), eq("PU"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TCATFILE), any(), eq("PU|10"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_AMT_REQ);
    }

    @Test
    void mainLine_enterKey_amountNotNumeric_showsAmountBad() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("PU");
                    f.setTrcati("0010");
                    f.setTramti("ABCDEF");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(XREFFILE), any(), eq("1234567890123456"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TTYPFILE), any(), eq("PU"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TCATFILE), any(), eq("PU|10"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_AMT_BAD);
    }

    @Test
    void mainLine_enterKey_amountHasTwoDecimalPoints_showsAmountBad() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("PU");
                    f.setTrcati("0010");
                    f.setTramti("1.2.3");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(XREFFILE), any(), eq("1234567890123456"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TTYPFILE), any(), eq("PU"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TCATFILE), any(), eq("PU|10"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_AMT_BAD);
    }

    @Test
    void mainLine_enterKey_amountZero_showsAmountMustBeGreaterThanZero() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("PU");
                    f.setTrcati("0010");
                    f.setTramti("0.00");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(XREFFILE), any(), eq("1234567890123456"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TTYPFILE), any(), eq("PU"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TCATFILE), any(), eq("PU|10"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_AMT_ZERO);
    }

    @Test
    void mainLine_enterKey_amountNegative_showsAmountMustBeGreaterThanZero() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("PU");
                    f.setTrcati("0010");
                    f.setTramti("-5.00");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(XREFFILE), any(), eq("1234567890123456"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TTYPFILE), any(), eq("PU"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TCATFILE), any(), eq("PU|10"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_AMT_ZERO);
    }

    // ───────────────────────── 5500-EDIT-MERCH / 5600-EDIT-DESC ─────────────────────────

    @Test
    void mainLine_enterKey_merchantBlank_showsMerchantRequired() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("PU");
                    f.setTrcati("0010");
                    f.setTramti("125.50");
                    f.setTrmerchi("          ");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(XREFFILE), any(), eq("1234567890123456"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TTYPFILE), any(), eq("PU"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TCATFILE), any(), eq("PU|10"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_MERCH_REQ);
    }

    @Test
    void mainLine_enterKey_descriptionBlank_showsDescriptionRequired() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setCardnumi("1234567890123456");
                    f.setTrtypei("PU");
                    f.setTrcati("0010");
                    f.setTramti("125.50");
                    f.setTrmerchi("ACME STORE");
                    f.setTrdesci("         ");
                });
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(XREFFILE), any(), eq("1234567890123456"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TTYPFILE), any(), eq("PU"), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(eq(TCATFILE), any(), eq("PU|10"), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_DESC_REQ);
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    // ───────────────────────── 6000-GET-NEXT-ID / 6100-REWRITE-CTR ─────────────────────────

    @Test
    void mainLine_allEditsPass_ctrlMissing_showsCounterMissingAndDoesNotWrite() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenAllFieldsValid();
        doAnswer(
                        inv -> {
                            nextResp.set(13);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(
                        eq(CTRLFILE), any(), argThat(s -> s.trim().equals("TRANID")), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CTR_MISSING);
        verify(appService, never()).rewriteFile(anyString(), any());
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_allEditsPass_ctrlReadUnexpectedError_showsCounterError() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenAllFieldsValid();
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(
                        eq(CTRLFILE), any(), argThat(s -> s.trim().equals("TRANID")), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CTR_ERROR);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_allEditsPass_rewriteCtrFails_showsCounterErrorAndResetsCardCursor() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenAllFieldsValid();
        doAnswer(
                        inv -> {
                            OctranaFields into = inv.getArgument(1);
                            into.setCtLastValue(41L);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(
                        eq(CTRLFILE), any(), argThat(s -> s.trim().equals("TRANID")), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(CTRLFILE), any());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_CTR_ERROR);
        assertEquals(-1, out.getInt("CARDNUML"));
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    // ───────────────────────── 6500-WRITE-TRAN / 6600-BUILD-RECORD / 2200-CONFIRM-ADD
    // ─────────────────────────

    @Test
    void mainLine_allEditsPass_writeSucceeds_confirmsAddWithNewTranId() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenAllFieldsValid();
        doAnswer(
                        inv -> {
                            OctranaFields into = inv.getArgument(1);
                            into.setCtLastValue(41L);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(
                        eq(CTRLFILE), any(), argThat(s -> s.trim().equals("TRANID")), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(CTRLFILE), any());
        ArgumentCaptor<Object> writeFrom = ArgumentCaptor.forClass(Object.class);
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .writeFile(
                        eq(TRANFILE),
                        writeFrom.capture(),
                        eq(String.format("%016d", 42L)),
                        anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(confirmMessage(42L));

        OctranaFields written = (OctranaFields) writeFrom.getValue();
        assertThat(written.getTrId()).isEqualTo(String.format("%016d", 42L));
        assertThat(written.getWsAmtNum()).isEqualByComparingTo(new BigDecimal("125.50"));
        assertThat(written.getTrCardNum().trim()).isEqualTo("1234567890123456");
        assertThat(written.getTrDesc().trim()).isEqualTo("Groceries");
        assertThat(written.getTrMerchantName().trim()).isEqualTo("ACME STORE");
        assertThat(written.getTrSource().trim()).isEqualTo("ONLINE");
        assertThat(written.getTrOrigTs().trim()).isEqualTo("2026-09-22 10:00:00");
        assertThat(written.getTrProcTs().trim()).isEqualTo("2026-09-22 10:00:00");
    }

    @Test
    void mainLine_allEditsPass_writeFails_showsWriteErrorAndDoesNotConfirm() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenAllFieldsValid();
        doAnswer(
                        inv -> {
                            OctranaFields into = inv.getArgument(1);
                            into.setCtLastValue(41L);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(
                        eq(CTRLFILE), any(), argThat(s -> s.trim().equals("TRANID")), anyInt());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(CTRLFILE), any());
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .writeFile(eq(TRANFILE), any(), anyString(), anyInt());

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(true));
        OctranaFields out = (OctranaFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_WRITE_ERROR);
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOctrana() {
        assertEquals("OCTRANA", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrta() {
        assertEquals("ORTA", service.getTransId());
    }

    @Test
    void mainLine_alwaysReturnsToTranidOrta() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        verify(appService, times(1)).returnTransid(eq("ORTA"), any(), eq(692));
    }
}

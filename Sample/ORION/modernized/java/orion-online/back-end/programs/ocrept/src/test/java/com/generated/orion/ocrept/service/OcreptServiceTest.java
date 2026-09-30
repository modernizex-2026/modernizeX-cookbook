package com.generated.orion.ocrept.service;

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
import com.generated.orion.ocrept.accessor.OcreptFields;
import com.generated.orion.ocrept.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OcreptService, generated from COBOL program OCREPT (report request:
 * bills/transactions summary by date range). All business logic is private and is exercised solely
 * through the public {@link OcreptService#mainLine(AppService)} entry point, driven by an {@link
 * AppService} mock that emulates CICS SEND/RECEIVE/STARTBR/READNEXT/ ENDBR/CALL/XCTL/RETURN.
 *
 * <p>Convert-gap check: OCREPT's Java service mirrors every COBOL paragraph 1:1
 * (0000-MAIN..9000-RETURN). No behavioral divergence was found against the COBOL ground truth, so
 * no CONVERT-GAP test is included here.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcreptServiceTest {

    private static final String MAP_NAME = "MREPTA";
    private static final String MENU_PGM = "OCMENU";
    private static final String PROGRAM = "OCREPT";
    private static final String TRANID = "ORRP";
    private static final String HDR_TITLE = "ORION CREDIT CARD MANAGEMENT SYSTEM";

    private static final String MSG_PROMPT = "Type 01=Bills 02=Trans, dates YYYY-MM-DD.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";
    private static final String MSG_TYPE_REQUIRED = "Report type is required.";
    private static final String MSG_TYPE_BAD = "Report type must be 01 or 02.";
    private static final String MSG_DATES_REQUIRED = "From and to dates are required.";
    private static final String MSG_FROM_BAD = "From date is not a valid date.";
    private static final String MSG_TO_BAD = "To date is not a valid date.";
    private static final String MSG_FROM_AFTER_TO = "From date is later than to date.";
    private static final String MSG_BILL_STARTBR_ERR = "Error starting bill browse.";
    private static final String MSG_BILL_READ_ERR = "Error reading bill file.";
    private static final String MSG_TRAN_STARTBR_ERR = "Error starting tran browse.";
    private static final String MSG_TRAN_READ_ERR = "Error reading tran file.";

    @Mock private AppService appService;

    private OcreptService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OcreptService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
        // Default OUDATE stub: every call is a valid date (KD-STATUS = '00').
        lenient()
                .doAnswer(
                        inv -> {
                            Object[] params = inv.getArgument(1);
                            String parm = (String) params[0];
                            params[0] = parm.substring(0, parm.length() - 2) + "00";
                            return null;
                        })
                .when(appService)
                .callProgram(eq("OUDATE"), any());
    }

    /** Builds a serialized ORION-COMMAREA carrying CA-PGM-CONTEXT. */
    private String commareaWithContext(int context) {
        OcreptFields helper = new OcreptFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(context));
    }

    /** Stubs receiveMap to populate the MREPTAI input fields, mirroring the production RECEIVE. */
    private void givenReceiveMapPopulates(java.util.function.Consumer<OcreptFields> populate) {
        doAnswer(
                        inv -> {
                            OcreptFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP_NAME), any());
    }

    private void givenReportInput(String type, String from, String to) {
        givenReceiveMapPopulates(
                f -> {
                    f.setRptypei(type);
                    f.setRpfromi(from);
                    f.setRptoi(to);
                });
    }

    /**
     * Overrides the default OUDATE stub with a fixed sequence of KD-STATUS results (per call
     * order).
     */
    private void givenOudateStatuses(String... statuses) {
        AtomicInteger callIdx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            Object[] params = inv.getArgument(1);
                            String parm = (String) params[0];
                            int i = callIdx.getAndIncrement();
                            String status =
                                    i < statuses.length
                                            ? statuses[i]
                                            : statuses[statuses.length - 1];
                            params[0] = parm.substring(0, parm.length() - 2) + status;
                            return null;
                        })
                .when(appService)
                .callProgram(eq("OUDATE"), any());
    }

    /** Stubs the bill browse (STARTBR/READNEXT/ENDBR) for a single scenario. */
    private void givenBillBrowse(int startResp, List<Object[]> records) {
        doAnswer(
                        inv -> {
                            nextResp.set(startResp);
                            return null;
                        })
                .when(appService)
                .startBrowse(anyString(), anyString(), anyInt());
        AtomicInteger idx = new AtomicInteger(0);
        lenient()
                .doAnswer(
                        inv -> {
                            OcreptFields into = inv.getArgument(1);
                            int i = idx.getAndIncrement();
                            if (i < records.size()) {
                                Object[] rec = records.get(i);
                                into.setBlPayDate((String) rec[0]);
                                into.setBlAmount((BigDecimal) rec[1]);
                                nextResp.set(0);
                            } else {
                                nextResp.set(20); // DFHRESP(ENDFILE)
                            }
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
        lenient()
                .doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .endBrowse(anyString());
    }

    /** Stubs the bill browse with a mid-loop read error on the given 1-based record index. */
    private void givenBillBrowseReadError(int errorAtRecord) {
        doAnswer(
                        inv -> {
                            nextResp.set(0); // DFHRESP(NORMAL) start
                            return null;
                        })
                .when(appService)
                .startBrowse(anyString(), anyString(), anyInt());
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.incrementAndGet();
                            nextResp.set(i == errorAtRecord ? 99 : 0);
                            if (i != errorAtRecord) {
                                OcreptFields into = inv.getArgument(1);
                                into.setBlPayDate("2026-01-15");
                                into.setBlAmount(new BigDecimal("10.00"));
                            }
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .endBrowse(anyString());
    }

    /**
     * Stubs the transaction browse with a mid-loop read error on the given 1-based record index.
     */
    private void givenTranBrowseReadError(int errorAtRecord) {
        doAnswer(
                        inv -> {
                            nextResp.set(0); // DFHRESP(NORMAL) start
                            return null;
                        })
                .when(appService)
                .startBrowse(anyString(), anyString(), anyInt());
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.incrementAndGet();
                            nextResp.set(i == errorAtRecord ? 99 : 0);
                            if (i != errorAtRecord) {
                                OcreptFields into = inv.getArgument(1);
                                into.setTrOrigTs("2026-01-15T08:00:00");
                                into.setTrAmt(new BigDecimal("10.00"));
                            }
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
        doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .endBrowse(anyString());
    }

    private void givenTranBrowse(int startResp, List<Object[]> records) {
        doAnswer(
                        inv -> {
                            nextResp.set(startResp);
                            return null;
                        })
                .when(appService)
                .startBrowse(anyString(), anyString(), anyInt());
        AtomicInteger idx = new AtomicInteger(0);
        lenient()
                .doAnswer(
                        inv -> {
                            OcreptFields into = inv.getArgument(1);
                            int i = idx.getAndIncrement();
                            if (i < records.size()) {
                                Object[] rec = records.get(i);
                                into.setTrOrigTs((String) rec[0]);
                                into.setTrAmt((BigDecimal) rec[1]);
                                nextResp.set(0);
                            } else {
                                nextResp.set(20); // DFHRESP(ENDFILE)
                            }
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
        lenient()
                .doAnswer(
                        inv -> {
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .endBrowse(anyString());
    }

    private OcreptFields captureFinalSend() {
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(
                        eq(MAP_NAME), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        return (OcreptFields) sendMapOut.getValue();
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcreptFields out = (OcreptFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo(TRANID);
        assertThat(out.getPgmnameo().trim()).isEqualTo(PROGRAM);
        assertThat(out.getTitleo().trim()).isEqualTo(HDR_TITLE);
        assertThat(out.getCurdateo().trim()).isEqualTo("2026-09-22");
        assertThat(out.getCurtimeo().trim()).isEqualTo("10:00:00");
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

        OcreptFields decoded = new OcreptFields(new WorkingStorage());
        decoded.writeBytes("ORION-COMMAREA", (byte[]) commareaCaptor.getValue());
        assertThat(decoded.getCaFromProgram().trim()).isEqualTo(PROGRAM);
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
                .sendMap(eq(MAP_NAME), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcreptFields out = (OcreptFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        // 2000-PROCESS-INPUT OTHER: resend initial (erase) then 8100-SEND-DATAONLY (no erase)
        verify(appService).sendMap(eq(MAP_NAME), any(), any(), eq(true), eq(false), eq(false));
        OcreptFields out = captureFinalSend();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2120-VALIDATE-TYPE ─────────────────────────

    @Test
    void mainLine_enterKey_reportTypeBlank_showsTypeRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReportInput("  ", "2026-01-01", "2026-01-31");

        service.mainLine(appService);

        OcreptFields out = captureFinalSend();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TYPE_REQUIRED);
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_reportTypeInvalid_showsTypeMustBe01Or02Message() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReportInput("99", "2026-01-01", "2026-01-31");

        service.mainLine(appService);

        OcreptFields out = captureFinalSend();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TYPE_BAD);
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    // ───────────────────────── 2130-VALIDATE-DATES ─────────────────────────

    @Test
    void mainLine_enterKey_datesBlank_showsDatesRequiredMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReportInput("01", "          ", "2026-01-31");

        service.mainLine(appService);

        OcreptFields out = captureFinalSend();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_DATES_REQUIRED);
        verify(appService, never()).callProgram(eq("OUDATE"), any());
    }

    @Test
    void mainLine_enterKey_fromDateInvalid_showsFromDateInvalidMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReportInput("01", "2026-99-99", "2026-01-31");
        givenOudateStatuses("99");

        service.mainLine(appService);

        OcreptFields out = captureFinalSend();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_FROM_BAD);
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_toDateInvalid_showsToDateInvalidMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReportInput("01", "2026-01-01", "2026-99-99");
        givenOudateStatuses("00", "99");

        service.mainLine(appService);

        OcreptFields out = captureFinalSend();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TO_BAD);
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    @Test
    void mainLine_enterKey_fromDateAfterToDate_showsFromLaterThanToMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReportInput("01", "2026-02-01", "2026-01-01");
        givenOudateStatuses("00", "00");

        service.mainLine(appService);

        OcreptFields out = captureFinalSend();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_FROM_AFTER_TO);
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    // ───────────────────────── 2200-REPORT-BILLS ─────────────────────────

    @Test
    void mainLine_enterKey_reportBills_startbrNotFound_zeroRecordsSummary() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReportInput("01", "2026-01-01", "2026-01-31");
        givenBillBrowse(13, List.of()); // DFHRESP(NOTFND)

        service.mainLine(appService);

        OcreptFields out = captureFinalSend();
        assertThat(out.getWsRptCount()).isEqualTo(0);
        assertThat(out.getWsTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(out.getErrmsgo().trim()).matches("Type 01 Count:\\s*0 Total:.*");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService).endBrowse(anyString());
    }

    @Test
    void mainLine_enterKey_reportBills_startbrError_showsBrowseErrorMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReportInput("01", "2026-01-01", "2026-01-31");
        doAnswer(
                        inv -> {
                            nextResp.set(99); // neither NORMAL nor NOTFND
                            return null;
                        })
                .when(appService)
                .startBrowse(anyString(), anyString(), anyInt());

        service.mainLine(appService);

        OcreptFields out = captureFinalSend();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BILL_STARTBR_ERR);
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_enterKey_reportBills_inRangeAndOutOfRangeRecords_accumulatesCountAndTotal() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReportInput("01", "2026-01-01", "2026-01-31");
        givenBillBrowse(
                0,
                List.of(
                        new Object[] {"2026-01-10", new BigDecimal("100.00")}, // in range
                        new Object[] {
                            "2025-12-31", new BigDecimal("50.00")
                        }, // before range — excluded
                        new Object[] {"2026-01-20", new BigDecimal("25.50")} // in range
                        ));

        service.mainLine(appService);

        OcreptFields out = captureFinalSend();
        assertThat(out.getWsRptCount()).isEqualTo(2);
        assertThat(out.getWsTotal()).isEqualByComparingTo(new BigDecimal("125.50"));
        assertThat(out.getErrmsgo().trim()).matches("Type 01 Count:\\s*2 Total:.*");
        verify(appService).endBrowse(anyString());
    }

    @Test
    void mainLine_enterKey_reportBills_readError_showsReadErrorMessageAndEndsBrowse() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReportInput("01", "2026-01-01", "2026-01-31");
        givenBillBrowseReadError(2); // fails on the 2nd READNEXT call

        service.mainLine(appService);

        OcreptFields out = captureFinalSend();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_BILL_READ_ERR);
        // ENDBR still runs unconditionally after the read loop, even on error (matches COBOL).
        verify(appService).endBrowse(anyString());
    }

    // ───────────────────────── 2300-REPORT-TRANS ─────────────────────────

    @Test
    void mainLine_enterKey_reportTrans_inRangeRecords_accumulatesCountAndTotal() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReportInput("02", "2026-01-01", "2026-01-31");
        givenTranBrowse(
                0,
                List.of(
                        new Object[] {
                            "2026-01-15T08:00:00", new BigDecimal("40.00")
                        }, // date part in range
                        new Object[] {
                            "2026-02-01T08:00:00", new BigDecimal("99.00")
                        } // after range — excluded
                        ));

        service.mainLine(appService);

        OcreptFields out = captureFinalSend();
        assertThat(out.getWsRptCount()).isEqualTo(1);
        assertThat(out.getWsTotal()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(out.getErrmsgo().trim()).matches("Type 02 Count:\\s*1 Total:.*");
        verify(appService).endBrowse(anyString());
    }

    @Test
    void mainLine_enterKey_reportTrans_startbrNotFound_zeroRecordsSummary() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReportInput("02", "2026-01-01", "2026-01-31");
        givenTranBrowse(13, List.of()); // DFHRESP(NOTFND)

        service.mainLine(appService);

        OcreptFields out = captureFinalSend();
        assertThat(out.getWsRptCount()).isEqualTo(0);
        assertThat(out.getErrmsgo().trim()).matches("Type 02 Count:\\s*0 Total:.*");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService).endBrowse(anyString());
    }

    @Test
    void mainLine_enterKey_reportTrans_startbrError_showsBrowseErrorMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReportInput("02", "2026-01-01", "2026-01-31");
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .startBrowse(anyString(), anyString(), anyInt());

        service.mainLine(appService);

        OcreptFields out = captureFinalSend();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TRAN_STARTBR_ERR);
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_enterKey_reportTrans_readError_showsReadErrorMessageAndEndsBrowse() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReportInput("02", "2026-01-01", "2026-01-31");
        givenTranBrowseReadError(2); // fails on the 2nd READNEXT call

        service.mainLine(appService);

        OcreptFields out = captureFinalSend();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_TRAN_READ_ERR);
        // ENDBR still runs unconditionally after the read loop, even on error (matches COBOL).
        verify(appService).endBrowse(anyString());
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcrept() {
        assertEquals(PROGRAM, service.getProgramName());
    }

    @Test
    void getTransId_returnsOrrp() {
        assertEquals(TRANID, service.getTransId());
    }
}

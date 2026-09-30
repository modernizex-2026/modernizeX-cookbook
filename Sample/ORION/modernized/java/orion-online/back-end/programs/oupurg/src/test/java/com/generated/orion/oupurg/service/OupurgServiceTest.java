package com.generated.orion.oupurg.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.generated.orion.oupurg.accessor.OupurgFields;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OupurgService, generated from COBOL program OUPURG. Ground truth for
 * input/expected values is the COBOL source at ORION-CCMS/cbl/OUPURG.cbl (paragraphs 0000-MAIN
 * through 6000-SET-STATUS).
 */
@ExtendWith(MockitoExtension.class)
class OupurgServiceTest {

    @Mock private AppService appService;

    private OupurgService service;
    private OupurgService.TaskContext ctx;

    @BeforeEach
    void setUp() {
        service = new OupurgService();
        ctx = new OupurgService.TaskContext(appService);
    }

    /** Invokes a private paragraph method (single TaskContext parameter) via reflection. */
    private void call(String methodName) throws Exception {
        Method m =
                OupurgService.class.getDeclaredMethod(methodName, OupurgService.TaskContext.class);
        m.setAccessible(true);
        m.invoke(service, ctx);
    }

    // ===================================================================
    // 1100-CHK-DATE  (validateCutoffDate)
    // ===================================================================

    @Test
    void validateCutoffDate_wellFormedYyyyMmDd_setsDtOkY() throws Exception {
        ctx.f.setWsDtIn("2026-01-15");

        call("validateCutoffDate");

        assertEquals("Y", ctx.f.getWsDtOk());
    }

    @Test
    void validateCutoffDate_wrongSeparators_setsDtOkN() throws Exception {
        ctx.f.setWsDtIn("2026/01/15");

        call("validateCutoffDate");

        assertEquals("N", ctx.f.getWsDtOk());
    }

    @Test
    void validateCutoffDate_nonNumericSegment_setsDtOkN() throws Exception {
        ctx.f.setWsDtIn("20XX-01-15");

        call("validateCutoffDate");

        assertEquals("N", ctx.f.getWsDtOk());
    }

    @Test
    void validateCutoffDate_blankInput_setsDtOkN() throws Exception {
        ctx.f.setWsDtIn("");

        call("validateCutoffDate");

        assertEquals("N", ctx.f.getWsDtOk());
    }

    // ===================================================================
    // 1000-INIT  (initializeProgram)
    // ===================================================================

    @Test
    void initializeProgram_maxZero_capsAtMaxSave() throws Exception {
        ctx.f.setKpgMax(0);
        ctx.f.setKpgCutoff("2026-01-01");

        call("initializeProgram");

        assertEquals(100, ctx.f.getWsCap());
        assertEquals("00", ctx.f.getKpgStatus());
        assertEquals(0, ctx.f.getKpgRead());
        assertEquals(0, ctx.f.getKpgPurged());
        assertEquals(0, ctx.f.getKpgKept());
        assertEquals(0, ctx.f.getKpgErrors());
        assertEquals("N", ctx.f.getKpgMore());
    }

    @Test
    void initializeProgram_maxBelowMaxSave_capsAtMax() throws Exception {
        ctx.f.setKpgMax(50);
        ctx.f.setKpgCutoff("2026-01-01");

        call("initializeProgram");

        assertEquals(50, ctx.f.getWsCap());
    }

    @Test
    void initializeProgram_maxAboveMaxSave_capsAtMaxSave() throws Exception {
        ctx.f.setKpgMax(200);
        ctx.f.setKpgCutoff("2026-01-01");

        call("initializeProgram");

        assertEquals(100, ctx.f.getWsCap());
    }

    @Test
    void initializeProgram_invalidCutoff_setsStatus99() throws Exception {
        ctx.f.setKpgMax(0);
        ctx.f.setKpgCutoff("BAD-DATE!!");

        call("initializeProgram");

        assertEquals("99", ctx.f.getKpgStatus());
        assertEquals("INVALID CUTOFF - USE YYYY-MM-DD", ctx.f.getKpgMsg().trim());
    }

    // ===================================================================
    // 2000-POSITION  (positionTransactionBrowse)
    // ===================================================================

    @Test
    void positionTransactionBrowse_blankStartTran_startsAtLowValues() throws Exception {
        ctx.f.setKpgStartTran(" ");
        when(appService.getEibresp()).thenReturn(0);

        call("positionTransactionBrowse");

        verify(appService).startBrowse(eq(wsTranfileValue()), anyString(), eq(0));
        assertEquals("Y", ctx.f.getWsBrowseSw());
    }

    @Test
    void positionTransactionBrowse_explicitStartTran_startsAtGivenKey() throws Exception {
        ctx.f.setKpgStartTran("TRAN0005000000");
        when(appService.getEibresp()).thenReturn(0);

        call("positionTransactionBrowse");

        assertEquals("Y", ctx.f.getWsBrowseSw());
        assertTrue(ctx.f.getTrId().trim().startsWith("TRAN0005"));
    }

    @Test
    void positionTransactionBrowse_notFound_setsEofYesButBrowseOff() throws Exception {
        ctx.f.setKpgStartTran(" ");
        when(appService.getEibresp()).thenReturn(13);

        call("positionTransactionBrowse");

        assertEquals("Y", ctx.f.getWsEofSw());
        assertEquals("N", ctx.f.getWsBrowseSw());
    }

    @Test
    void positionTransactionBrowse_endfile_setsEofYes() throws Exception {
        ctx.f.setKpgStartTran(" ");
        when(appService.getEibresp()).thenReturn(20);

        call("positionTransactionBrowse");

        assertEquals("Y", ctx.f.getWsEofSw());
        assertEquals("N", ctx.f.getWsBrowseSw());
    }

    @Test
    void positionTransactionBrowse_otherResp_setsStatus99AndMessage() throws Exception {
        ctx.f.setKpgStartTran(" ");
        when(appService.getEibresp()).thenReturn(99);

        call("positionTransactionBrowse");

        assertEquals("99", ctx.f.getKpgStatus());
        assertEquals("TRANFILE STARTBR FAILED", ctx.f.getKpgMsg().trim());
        assertEquals("N", ctx.f.getWsBrowseSw());
    }

    // ===================================================================
    // 3000-SCAN-LOOP / 3100-READ-TRAN / 3200-CLASSIFY
    // ===================================================================

    @Test
    void scanTransactionRecords_agedRecord_isSavedNotKept() throws Exception {
        ctx.f.setWsCap(100);
        ctx.f.setWsMax(0);
        ctx.f.setKpgCutoff("2026-06-01");
        ctx.f.setWsSaveCnt(0);
        when(appService.getEibresp()).thenReturn(0);
        primeNextRead("TRAN0000000001", "2026-01-01", new BigDecimal("100.00"));

        call("scanTransactionRecords");

        assertEquals(1, ctx.f.getWsSaveCnt());
        assertEquals(0, ctx.f.getKpgKept());
        assertEquals(1, ctx.f.getKpgRead());
        assertEquals(new BigDecimal("100.00"), ctx.f.getWsPAmt(1));
        assertTrue(ctx.f.getWsPId(1).trim().equals("TRAN0000000001"));
    }

    @Test
    void scanTransactionRecords_currentRecord_isKeptNotSaved() throws Exception {
        ctx.f.setWsCap(100);
        ctx.f.setWsMax(0);
        ctx.f.setKpgCutoff("2026-06-01");
        ctx.f.setWsSaveCnt(0);
        ctx.f.setKpgKeepAmt(BigDecimal.ZERO);
        when(appService.getEibresp()).thenReturn(0);
        primeNextRead("TRAN0000000002", "2026-06-01", new BigDecimal("50.00"));

        call("scanTransactionRecords");

        assertEquals(0, ctx.f.getWsSaveCnt());
        assertEquals(1, ctx.f.getKpgKept());
        assertEquals(new BigDecimal("50.00"), ctx.f.getKpgKeepAmt());
    }

    @Test
    void scanTransactionRecords_saveCntReachesCap_setsCapped() throws Exception {
        ctx.f.setWsCap(1);
        ctx.f.setWsMax(0);
        ctx.f.setKpgCutoff("2026-06-01");
        ctx.f.setWsSaveCnt(0);
        when(appService.getEibresp()).thenReturn(0);
        primeNextRead("TRAN0000000003", "2026-01-01", new BigDecimal("10.00"));

        call("scanTransactionRecords");

        assertEquals("Y", ctx.f.getWsCapped());
    }

    @Test
    void scanTransactionRecords_readCountReachesOperatorMax_setsCapped() throws Exception {
        ctx.f.setWsCap(100);
        ctx.f.setWsMax(1);
        ctx.f.setKpgCutoff("2026-06-01");
        ctx.f.setWsSaveCnt(0);
        ctx.f.setKpgKeepAmt(BigDecimal.ZERO);
        when(appService.getEibresp()).thenReturn(0);
        primeNextRead("TRAN0000000004", "2026-06-01", new BigDecimal("10.00"));

        call("scanTransactionRecords");

        assertEquals("Y", ctx.f.getWsCapped());
    }

    @Test
    void scanTransactionRecords_readnextEndfile_setsEofAndSkipsClassify() throws Exception {
        when(appService.getEibresp()).thenReturn(20);

        call("scanTransactionRecords");

        assertEquals("Y", ctx.f.getWsEofSw());
        assertEquals(0, ctx.f.getKpgRead());
    }

    @Test
    void scanTransactionRecords_readnextOtherError_setsEofAndIncrementsErrors() throws Exception {
        when(appService.getEibresp()).thenReturn(99);

        call("scanTransactionRecords");

        assertEquals("Y", ctx.f.getWsEofSw());
        assertEquals(1, ctx.f.getKpgErrors());
        assertEquals("TRANFILE READNEXT FAILED", ctx.f.getKpgMsg().trim());
    }

    // ===================================================================
    // 4000-PEEK-NEXT  (peekNextTransaction)
    // ===================================================================

    @Test
    void peekNextTransaction_alreadyEof_setsMoreNAndNeverReads() throws Exception {
        ctx.f.setWsEofSw("Y");

        call("peekNextTransaction");

        assertEquals("N", ctx.f.getKpgMore());
        verify(appService, never()).readNext(anyString(), any());
    }

    @Test
    void peekNextTransaction_normalRead_setsMoreYAndNextTran() throws Exception {
        ctx.f.setWsEofSw("N");
        when(appService.getEibresp()).thenReturn(0);
        ctx.f.setTrId("TRAN0000009999");

        call("peekNextTransaction");

        assertEquals("Y", ctx.f.getKpgMore());
        assertTrue(ctx.f.getKpgNextTran().trim().equals("TRAN0000009999"));
    }

    @Test
    void peekNextTransaction_endfile_setsMoreN() throws Exception {
        ctx.f.setWsEofSw("N");
        when(appService.getEibresp()).thenReturn(20);

        call("peekNextTransaction");

        assertEquals("N", ctx.f.getKpgMore());
    }

    @Test
    void peekNextTransaction_otherError_setsMoreNAndIncrementsErrors() throws Exception {
        ctx.f.setWsEofSw("N");
        ctx.f.setKpgErrors(0);
        when(appService.getEibresp()).thenReturn(99);

        call("peekNextTransaction");

        assertEquals("N", ctx.f.getKpgMore());
        assertEquals(1, ctx.f.getKpgErrors());
    }

    // ===================================================================
    // 5000-END-BROWSE  (endTransactionBrowse)
    // ===================================================================

    @Test
    void endTransactionBrowse_invokesEndBrowseOnTranfile() throws Exception {
        when(appService.getEibresp()).thenReturn(0);

        call("endTransactionBrowse");

        verify(appService).endBrowse(wsTranfileValue());
    }

    // ===================================================================
    // 5500-PURGE-SAVED / 5600-PURGE-ONE
    // ===================================================================

    @Test
    @Timeout(10)
    void purgeSavedTransactions_mixedSuccessAndFailure_updatesCountersAndAmounts()
            throws Exception {
        ctx.f.setWsSaveCnt(2);
        ctx.f.setWsPId(1, "TRAN0000000001");
        ctx.f.setWsPAmt(1, new BigDecimal("10.00"));
        ctx.f.setWsPId(2, "TRAN0000000002");
        ctx.f.setWsPAmt(2, new BigDecimal("20.00"));
        ctx.f.setKpgPurged(0);
        ctx.f.setKpgPurgeAmt(BigDecimal.ZERO);
        ctx.f.setKpgErrors(0);
        when(appService.getEibresp()).thenReturn(0, 99);

        call("purgeSavedTransactions");

        verify(appService, times(2)).deleteFile(eq(wsTranfileValue()), anyString(), eq(0));
        assertEquals(1, ctx.f.getKpgPurged());
        assertEquals(new BigDecimal("10.00"), ctx.f.getKpgPurgeAmt());
        assertEquals(1, ctx.f.getKpgErrors());
    }

    @Test
    void purgeSavedTransactions_emptySaveTable_noDeletesIssued() throws Exception {
        ctx.f.setWsSaveCnt(0);

        call("purgeSavedTransactions");

        verify(appService, never()).deleteFile(anyString(), anyString(), anyInt());
    }

    // ===================================================================
    // 6000-SET-STATUS  (setCompletionStatus)
    // ===================================================================

    @Test
    void setCompletionStatus_alreadyStatus99_leavesUnchanged() throws Exception {
        ctx.f.setKpgStatus("99");
        ctx.f.setKpgMsg("INVALID CUTOFF - USE YYYY-MM-DD");
        ctx.f.setKpgRead(0);

        call("setCompletionStatus");

        assertEquals("99", ctx.f.getKpgStatus());
        assertEquals("INVALID CUTOFF - USE YYYY-MM-DD", ctx.f.getKpgMsg().trim());
    }

    @Test
    void setCompletionStatus_noRecordsRead_setsStatus10() throws Exception {
        ctx.f.setKpgStatus("00");
        ctx.f.setKpgRead(0);

        call("setCompletionStatus");

        assertEquals("10", ctx.f.getKpgStatus());
        assertEquals("NO TRANSACTIONS PROCESSED", ctx.f.getKpgMsg().trim());
    }

    @Test
    void setCompletionStatus_recordsRead_setsStatus00() throws Exception {
        ctx.f.setKpgStatus("00");
        ctx.f.setKpgRead(5);

        call("setCompletionStatus");

        assertEquals("00", ctx.f.getKpgStatus());
        assertEquals("TRANSACTION PURGE COMPLETE", ctx.f.getKpgMsg().trim());
    }

    // ===================================================================
    // 0000-MAIN  (runMainProgram) - full-flow integration
    // ===================================================================

    @Test
    @Timeout(10)
    void runMainProgram_invalidCutoff_abortsBeforeBrowse() throws Exception {
        // KPG-PARM overlays CA-WORK-AREA (SET ADDRESS OF) once runMainProgram starts,
        // so pre-populate the KPG-* fields through the same alias the paragraph establishes.
        ctx.f.aliasGroup("KPG-PARM", "CA-WORK-AREA");
        ctx.f.setKpgCutoff("NOT-A-DATE");
        ctx.f.setKpgMax(0);
        ctx.f.setKpgStartTran(" ");

        call("runMainProgram");

        assertEquals("99", ctx.f.getKpgStatus());
        assertEquals("INVALID CUTOFF - USE YYYY-MM-DD", ctx.f.getKpgMsg().trim());
        verify(appService, never()).startBrowse(anyString(), anyString(), anyInt());
    }

    @Test
    @Timeout(10)
    void runMainProgram_immediateNotFound_reportsNoTransactionsProcessed() throws Exception {
        ctx.f.aliasGroup("KPG-PARM", "CA-WORK-AREA");
        ctx.f.setKpgCutoff("2026-06-01");
        ctx.f.setKpgMax(0);
        ctx.f.setKpgStartTran(" ");
        when(appService.getEibresp()).thenReturn(13);

        call("runMainProgram");

        assertEquals("10", ctx.f.getKpgStatus());
        assertEquals("NO TRANSACTIONS PROCESSED", ctx.f.getKpgMsg().trim());
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
        verify(appService, never()).deleteFile(anyString(), anyString(), anyInt());
    }

    @Test
    @Timeout(10)
    void runMainProgram_oneAgedRecordThenEndfile_purgesRecordAndReportsComplete() throws Exception {
        ctx.f.aliasGroup("KPG-PARM", "CA-WORK-AREA");
        ctx.f.setKpgCutoff("2026-06-01");
        ctx.f.setKpgMax(0);
        ctx.f.setKpgStartTran(" ");

        AtomicInteger readCall = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            OupurgFields f = (OupurgFields) inv.getArgument(1);
                            if (readCall.getAndIncrement() == 0) {
                                f.setTrId("TRAN0000000001");
                                f.setTrProcTs("2026-01-01");
                                f.setTrAmt(new BigDecimal("100.00"));
                            }
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());

        // order: startBrowse(0), readNext#1(0), readNext#2->ENDFILE(20), endBrowse(0), delete(0)
        when(appService.getEibresp()).thenReturn(0, 0, 20, 0, 0);

        call("runMainProgram");

        assertEquals("00", ctx.f.getKpgStatus());
        assertEquals("TRANSACTION PURGE COMPLETE", ctx.f.getKpgMsg().trim());
        assertEquals(1, ctx.f.getKpgRead());
        assertEquals(1, ctx.f.getKpgPurged());
        assertEquals(new BigDecimal("100.00"), ctx.f.getKpgPurgeAmt());
        assertEquals("N", ctx.f.getKpgMore());
        verify(appService).endBrowse(wsTranfileValue());
        verify(appService, times(1)).deleteFile(eq(wsTranfileValue()), anyString(), eq(0));
    }

    // ===================================================================
    // helpers
    // ===================================================================

    private void primeNextRead(String trId, String procDate, BigDecimal amount) {
        doAnswer(
                        inv -> {
                            ctx.f.setTrId(trId);
                            ctx.f.setTrProcTs(procDate);
                            ctx.f.setTrAmt(amount);
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
    }

    private String wsTranfileValue() {
        return ctx.f.getWsTranfile().trim();
    }
}

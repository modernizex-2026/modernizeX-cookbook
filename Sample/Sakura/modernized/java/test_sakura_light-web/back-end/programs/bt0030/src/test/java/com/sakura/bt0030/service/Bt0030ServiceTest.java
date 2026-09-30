package com.sakura.bt0030.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.abortx.service.AbortxService;
import com.sakura.bt0030.domain.Bt0030FieldAccess;
import com.sakura.bt0030.runtime.Bt0030Datasets;
import com.sakura.dateut.service.DateutService;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.StokfDataset;
import com.sakura.runtime.io.SyscfDataset;
import com.sakura.runtime.linkage.DateutLinkParm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for {@link Bt0030Service}, derived from COBOL program BT0030 (stock monthly update /
 * valuation, batch/console — no screen section). Ground truth for inputs/expected values:
 * BT0030.cob PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Bt0030ServiceTest {

    @Spy private StokfDataset stokfSpy = new StokfDataset();
    @Spy private SyscfDataset syscfSpy = new SyscfDataset();

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;

    private Bt0030Service service;
    private Bt0030FieldAccess ws;

    /** Subclass swapping in the spy datasets since Bt0030Datasets builds its own real files. */
    private static class TestDatasets extends Bt0030Datasets {
        private final StokfDataset stokf;
        private final SyscfDataset syscf;

        TestDatasets(StokfDataset stokf, SyscfDataset syscf) {
            this.stokf = stokf;
            this.syscf = syscf;
        }

        @Override
        public StokfDataset getStokf() {
            return stokf;
        }

        @Override
        public SyscfDataset getSyscf() {
            return syscf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Bt0030Datasets fileSet = new TestDatasets(stokfSpy, syscfSpy);
        service = new Bt0030Service(fileSet, dateutService, abortxService);
        ws = getWs();

        doNothing().when(stokfSpy).open(any());
        doNothing().when(stokfSpy).close();
        doReturn("00").when(stokfSpy).getFileStatus();
        doReturn(false).when(stokfSpy).isInvalidKey();
        doReturn(false).when(stokfSpy).isAtEnd();
        doReturn(true).when(stokfSpy).start(anyString(), anyString());
        doReturn(false).when(stokfSpy).readNext();
        doNothing().when(stokfSpy).rewrite();

        doNothing().when(syscfSpy).open(any());
        doNothing().when(syscfSpy).close();
        doReturn("00").when(syscfSpy).getFileStatus();
        doReturn(false).when(syscfSpy).isInvalidKey();
        doReturn(true).when(syscfSpy).readByKey(any());

        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            p.getKdate().setKdStatus("00");
                            p.getKdate().setKdDate1(20260918);
                            return null;
                        })
                .when(dateutService)
                .execute(any());
    }

    @AfterEach
    void tearDown() {
        // no shared mutable state to reset
    }

    /* ── reflection helpers ── */

    private Bt0030FieldAccess getWs() throws Exception {
        Field f = Bt0030Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Bt0030FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Bt0030Service.class.getDeclaredMethod(name);
            m.setAccessible(true);
            m.invoke(service);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(cause);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /* ── INIT-010 / GTOD-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_happyPath_setsProgidAndSysDateAndProceeds() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("Y");

            invokePrivate("initializeProgram");

            assertEquals("BT0030", ws.getWkProgid());
            assertEquals(20260918, ws.getWkSysdate());
            assertEquals(20260918, ws.getWkSysymd());
            assertEquals(0, ws.getWkAbortFlg());
            verify(stokfSpy).open(FileOpenMode.IO);
            verify(syscfSpy).open(FileOpenMode.INPUT);
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void fetchTodaysDate_success_setsSysdateFromDateut() {
        invokePrivate("fetchTodaysDate");

        assertEquals(20260918, ws.getWkSysdate());
        assertEquals(20260918, ws.getWkSysymd());
        verify(dateutService).execute(any());
    }

    /* ── OPEN-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openProgramFiles_allStatus00_opensBothFilesWithoutAbort() {
        invokePrivate("openProgramFiles");

        verify(stokfSpy, times(1)).open(FileOpenMode.IO);
        verify(syscfSpy, times(1)).open(FileOpenMode.INPUT);
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openProgramFiles_stokfStatus35_reopensAsOutputThenIO() {
        doReturn("35", "00", "00", "00").when(stokfSpy).getFileStatus();

        invokePrivate("openProgramFiles");

        verify(stokfSpy, times(2)).open(FileOpenMode.IO);
        verify(stokfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(stokfSpy, times(1)).close();
        verify(syscfSpy, times(1)).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openProgramFiles_stokfOpenErrorAfterRetry_abortsAndSkipsSyscf() {
        doReturn("23").when(stokfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openProgramFiles"));

        assertEquals("STOKF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(syscfSpy, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openProgramFiles_syscfOpenError_aborts() {
        doReturn("23").when(syscfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openProgramFiles"));

        assertEquals("SYSCF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── RSYS-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readSystemControlRecord_found_success() {
        invokePrivate("readSystemControlRecord");

        assertEquals(1, ws.getSyKey());
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readSystemControlRecord_invalidKey_aborts() {
        doReturn(true).when(syscfSpy).isInvalidKey();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("readSystemControlRecord"));

        assertEquals("SYSCF", ws.getKaFile().trim());
        assertEquals("System control record missing", ws.getKaDetail().trim());
        verify(abortxService).execute(any());
    }

    /* ── CONF-010 — COBOL: CONFIRM-YES 88-level covers "Y" and "y" ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmOperatorProceed_confirmUppercaseY_doesNotAbort() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("Y");

            invokePrivate("confirmOperatorProceed");

            assertEquals(0, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmOperatorProceed_confirmLowercaseY_doesNotAbort() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("y");

            invokePrivate("confirmOperatorProceed");

            assertEquals(0, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmOperatorProceed_confirmN_setsAbortFlag() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("N");

            invokePrivate("confirmOperatorProceed");

            assertEquals(1, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmOperatorProceed_noStdinInput_setsAbortFlag() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn(null);

            invokePrivate("confirmOperatorProceed");

            assertEquals(1, ws.getWkAbortFlg());
        }
    }

    /* ── PROC-010 / PNXT-010 / PONE-010 — warehouse break processing ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processStockRecords_startInvalidKey_noRecordsProcessedAndNoSubtotal() {
        doReturn(true).when(stokfSpy).isInvalidKey();

        invokePrivate("processStockRecords");

        assertEquals(1, ws.getEofFlg());
        assertEquals(0, ws.getWkReadCnt());
        assertEquals(0, ws.getWkWhseCnt());
        verify(stokfSpy, never()).readNext();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processStockRecords_multipleWarehouses_breaksSubtotalsAndAccumulatesGrandTotals() {
        AtomicInteger callIndex = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int idx = callIndex.getAndIncrement();
                            switch (idx) {
                                case 0 -> { // whse 1, item 100: onhand=50, cost=2.00 -> val=100.00
                                    ws.setSkWhse(1);
                                    ws.setSkProd(100);
                                    ws.setSkOnhand(new BigDecimal("50"));
                                    ws.setSkAvgCost(new BigDecimal("2.00"));
                                    ws.setSkAllocated(new BigDecimal("10"));
                                    ws.setSkOnOrder(BigDecimal.ZERO);
                                    ws.setSkYtdIn(new BigDecimal("5"));
                                    ws.setSkYtdOut(new BigDecimal("3"));
                                }
                                case 1 -> { // whse 1, item 101: negative onhand -> housekeeping
                                    // clears avg cost
                                    ws.setSkWhse(1);
                                    ws.setSkProd(101);
                                    ws.setSkOnhand(new BigDecimal("-5"));
                                    ws.setSkAvgCost(new BigDecimal("1.00"));
                                    ws.setSkAllocated(BigDecimal.ZERO);
                                    ws.setSkOnOrder(BigDecimal.ZERO);
                                    ws.setSkYtdIn(BigDecimal.ZERO);
                                    ws.setSkYtdOut(BigDecimal.ZERO);
                                }
                                case 2 -> { // whse 2, item 200: zero onhand, negative allocated ->
                                    // housekeeping clears it
                                    ws.setSkWhse(2);
                                    ws.setSkProd(200);
                                    ws.setSkOnhand(BigDecimal.ZERO);
                                    ws.setSkAvgCost(BigDecimal.ZERO);
                                    ws.setSkAllocated(new BigDecimal("-3"));
                                    ws.setSkOnOrder(BigDecimal.ZERO);
                                    ws.setSkYtdIn(new BigDecimal("1"));
                                    ws.setSkYtdOut(new BigDecimal("1"));
                                }
                                default -> {
                                    /* no more records */
                                }
                            }
                            return idx < 3;
                        })
                .when(stokfSpy)
                .readNext();
        doReturn(false, false, false, true).when(stokfSpy).isAtEnd();

        invokePrivate("processStockRecords");

        assertEquals(1, ws.getEofFlg());
        assertEquals(3, ws.getWkReadCnt());
        assertEquals(2, ws.getWkWhseCnt());
        assertEquals(2, ws.getWkAdjCnt());
        assertEquals(1, ws.getWkNegCnt());
        assertEquals(1, ws.getWkZeroCnt());
        assertEquals(1, ws.getWkZerovalCnt());
        assertEquals(new BigDecimal("95.00"), ws.getWkValTot());
        verify(stokfSpy, times(2)).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processNextStockRecord_atEnd_setsEofFlgAndSkipsFurtherProcessing() {
        doReturn(true).when(stokfSpy).isAtEnd();

        invokePrivate("processNextStockRecord");

        assertEquals(1, ws.getEofFlg());
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processNextStockRecord_fstsErrorNotAtEnd_aborts() {
        doReturn(false).when(stokfSpy).isAtEnd();
        doReturn("99").when(stokfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("processNextStockRecord"));

        assertEquals("STOKF", ws.getKaFile().trim());
        assertEquals("READ NEXT STOKF failed", ws.getKaDetail().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processNextStockRecord_firstRecord_setsPrevWhseWithoutPrintingSubtotal() {
        ws.setWkFirstFlg(1);
        ws.setWkPrevWhse(0);
        ws.setSkWhse(7);
        ws.setSkOnhand(BigDecimal.ZERO);
        ws.setSkAvgCost(BigDecimal.ZERO);
        ws.setSkAllocated(BigDecimal.ZERO);
        ws.setSkOnOrder(BigDecimal.ZERO);
        ws.setSkYtdIn(BigDecimal.ZERO);
        ws.setSkYtdOut(BigDecimal.ZERO);

        invokePrivate("processNextStockRecord");

        assertEquals(0, ws.getWkFirstFlg());
        assertEquals(7, ws.getWkPrevWhse());
        assertEquals(0, ws.getWkWhseCnt());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processNextStockRecord_warehouseChange_printsSubtotalAndResets() {
        ws.setWkFirstFlg(0);
        ws.setWkPrevWhse(3);
        ws.setWkWhItems(5);
        ws.setSkWhse(4);
        ws.setSkOnhand(BigDecimal.ZERO);
        ws.setSkAvgCost(BigDecimal.ZERO);
        ws.setSkAllocated(BigDecimal.ZERO);
        ws.setSkOnOrder(BigDecimal.ZERO);
        ws.setSkYtdIn(BigDecimal.ZERO);
        ws.setSkYtdOut(BigDecimal.ZERO);

        invokePrivate("processNextStockRecord");

        assertEquals(1, ws.getWkWhseCnt());
        assertEquals(4, ws.getWkPrevWhse());
        // reset happened before the new record was accumulated -> only this record counted
        assertEquals(1, ws.getWkWhItems());
    }

    /* ── PONE-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processOneStockRecord_computesValuationAvailableAndAccumulatesTotals() {
        ws.setSkOnhand(new BigDecimal("100"));
        ws.setSkAvgCost(new BigDecimal("3.33"));
        ws.setSkAllocated(new BigDecimal("20"));
        ws.setSkOnOrder(BigDecimal.ZERO);
        ws.setSkYtdIn(new BigDecimal("10"));
        ws.setSkYtdOut(new BigDecimal("4"));

        invokePrivate("processOneStockRecord");

        assertEquals(new BigDecimal("333.00"), ws.getWkItemVal());
        assertEquals(80, ws.getWkAvail());
        assertEquals(1, ws.getWkReadCnt());
        assertEquals(100L, ws.getWkOnhandTot());
        assertEquals(80, ws.getWkAvailTot());
        assertEquals(new BigDecimal("333.00"), ws.getWkValTot());
        assertEquals(10L, ws.getWkYtdinTot());
        assertEquals(4L, ws.getWkYtdoutTot());
        assertEquals(1, ws.getWkWhItems());
        assertEquals(0, ws.getWkZerovalCnt());
        assertEquals(0, ws.getWkNegCnt());
        assertEquals(0, ws.getWkZeroCnt());
        verify(stokfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processOneStockRecord_negativeOnhand_incrementsNegCntAndTriggersHousekeeping() {
        ws.setSkProd(555);
        ws.setSkWhse(9);
        ws.setSkOnhand(new BigDecimal("-8"));
        ws.setSkAvgCost(new BigDecimal("5.00"));
        ws.setSkAllocated(BigDecimal.ZERO);
        ws.setSkOnOrder(BigDecimal.ZERO);
        ws.setSkYtdIn(BigDecimal.ZERO);
        ws.setSkYtdOut(BigDecimal.ZERO);
        ws.setWkSysdate(20260918);

        invokePrivate("processOneStockRecord");

        assertEquals(1, ws.getWkNegCnt());
        assertEquals(0, ws.getSkAvgCost().compareTo(BigDecimal.ZERO));
        assertEquals(1, ws.getWkAdjCnt());
        verify(stokfSpy).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processOneStockRecord_zeroOnhandAndZeroItemVal_incrementsZeroAndZerovalCounts() {
        ws.setSkOnhand(BigDecimal.ZERO);
        ws.setSkAvgCost(BigDecimal.ZERO);
        ws.setSkAllocated(BigDecimal.ZERO);
        ws.setSkOnOrder(BigDecimal.ZERO);
        ws.setSkYtdIn(BigDecimal.ZERO);
        ws.setSkYtdOut(BigDecimal.ZERO);

        invokePrivate("processOneStockRecord");

        assertEquals(1, ws.getWkZeroCnt());
        assertEquals(1, ws.getWkZerovalCnt());
        assertEquals(0, ws.getWkAdjCnt());
        verify(stokfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processOneStockRecord_rewriteInvalidKey_aborts() {
        ws.setSkOnhand(new BigDecimal("-1"));
        ws.setSkAvgCost(new BigDecimal("1.00"));
        ws.setSkAllocated(BigDecimal.ZERO);
        ws.setSkOnOrder(BigDecimal.ZERO);
        ws.setSkYtdIn(BigDecimal.ZERO);
        ws.setSkYtdOut(BigDecimal.ZERO);
        doReturn(true).when(stokfSpy).isInvalidKey();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("processOneStockRecord"));

        assertEquals("STOKF", ws.getKaFile().trim());
        assertEquals("REWRITE STOKF failed", ws.getKaDetail().trim());
        // COBOL: ADD 1 TO WK-ADJ-CNT happens AFTER END-REWRITE, never reached on abort
        assertEquals(0, ws.getWkAdjCnt());
    }

    /* ── HOUS-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void housekeepStockFields_onhandZeroAndAvgCostNonzero_clearsAvgCost() {
        ws.setSkOnhand(BigDecimal.ZERO);
        ws.setSkAvgCost(new BigDecimal("4.50"));
        ws.setSkAllocated(BigDecimal.ZERO);
        ws.setSkOnOrder(BigDecimal.ZERO);
        ws.setWkChgFlg(0);

        invokePrivate("housekeepStockFields");

        assertEquals(0, ws.getSkAvgCost().compareTo(BigDecimal.ZERO));
        assertEquals(1, ws.getWkChgFlg());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void housekeepStockFields_negativeAllocated_clearsAllocated() {
        ws.setSkOnhand(new BigDecimal("10"));
        ws.setSkAvgCost(new BigDecimal("2.00"));
        ws.setSkAllocated(new BigDecimal("-4"));
        ws.setSkOnOrder(BigDecimal.ZERO);
        ws.setWkChgFlg(0);

        invokePrivate("housekeepStockFields");

        assertEquals(BigDecimal.ZERO, ws.getSkAllocated());
        assertEquals(1, ws.getWkChgFlg());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void housekeepStockFields_negativeOnOrder_clearsOnOrder() {
        ws.setSkOnhand(new BigDecimal("10"));
        ws.setSkAvgCost(new BigDecimal("2.00"));
        ws.setSkAllocated(BigDecimal.ZERO);
        ws.setSkOnOrder(new BigDecimal("-2"));
        ws.setWkChgFlg(0);

        invokePrivate("housekeepStockFields");

        assertEquals(BigDecimal.ZERO, ws.getSkOnOrder());
        assertEquals(1, ws.getWkChgFlg());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void housekeepStockFields_noAnomalies_leavesChgFlgUnset() {
        ws.setSkOnhand(new BigDecimal("10"));
        ws.setSkAvgCost(new BigDecimal("2.00"));
        ws.setSkAllocated(new BigDecimal("1"));
        ws.setSkOnOrder(new BigDecimal("1"));
        ws.setWkChgFlg(0);

        invokePrivate("housekeepStockFields");

        assertEquals(0, ws.getWkChgFlg());
        assertEquals(new BigDecimal("2.00"), ws.getSkAvgCost());
    }

    /* ── CLSB-010 / PRSB-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void resetWarehouseSubtotals_resetsAllToZero() {
        ws.setWkWhItems(9);
        ws.setWkWhOnhand(100);
        ws.setWkWhVal(new BigDecimal("50.00"));

        invokePrivate("resetWarehouseSubtotals");

        assertEquals(0, ws.getWkWhItems());
        assertEquals(0L, ws.getWkWhOnhand());
        assertEquals(BigDecimal.ZERO.setScale(0), ws.getWkWhVal().setScale(0));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printWarehouseSubtotal_incrementsWhseCntAndCopiesFieldsForDisplay() {
        ws.setWkWhseCnt(0);
        ws.setWkPrevWhse(12);
        ws.setWkWhItems(3);
        ws.setWkWhOnhand(75);
        ws.setWkWhVal(new BigDecimal("150.25"));

        invokePrivate("printWarehouseSubtotal");

        assertEquals(1, ws.getWkWhseCnt());
        assertEquals(12, ws.getWkEWhse());
        assertEquals(3, ws.getWkECnt());
        assertEquals(75L, ws.getWkEQty());
        assertEquals(new BigDecimal("150.25"), ws.getWkEVal());
    }

    /* ── PSUM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printSummaryReport_copiesAllAccumulatorsIntoDisplayFields() {
        ws.setWkReadCnt(10);
        ws.setWkWhseCnt(2);
        ws.setWkAdjCnt(3);
        ws.setWkZeroCnt(4);
        ws.setWkNegCnt(1);
        ws.setWkZerovalCnt(2);
        ws.setWkOnhandTot(500);
        ws.setWkAvailTot(400);
        ws.setWkYtdinTot(30);
        ws.setWkYtdoutTot(20);
        ws.setWkValTot(new BigDecimal("999.99"));

        invokePrivate("printSummaryReport");

        assertEquals(2, ws.getWkECnt());
        assertEquals(20L, ws.getWkEQty());
        assertEquals(new BigDecimal("999.99"), ws.getWkEVal());
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void closeProgramFiles_closesBothFiles() {
        invokePrivate("closeProgramFiles");

        verify(stokfSpy).close();
        verify(syscfSpy).close();
    }

    /* ── ABND-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abortProgram_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortProgram"));

        assertEquals("BT0030", ws.getKaProgid());
        assertEquals("23", ws.getKaFsts());
        assertEquals("EBATCH", ws.getKaMsgcode());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(stokfSpy).close();
        verify(syscfSpy).close();
    }

    /* ── MAIN-000 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_confirmedByOperator_processesRecordsAndCompletesNormally() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("Y");
            doReturn(true).when(stokfSpy).isInvalidKey(); // empty stock file -> immediate EOF

            assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

            assertEquals(0, ws.getCompletionCode());
            verify(stokfSpy).close();
            verify(syscfSpy).close();
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_operatorCancels_skipsProcessingButStillClosesFiles() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("N");

            assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

            assertEquals(1, ws.getWkAbortFlg());
            assertEquals(0, ws.getWkReadCnt());
            assertEquals(0, ws.getCompletionCode());
            verify(stokfSpy).close();
            verify(syscfSpy).close();
            verify(stokfSpy, never()).readNext();
        }
    }
}

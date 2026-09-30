package com.sakura.bt0070.service;

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
import com.sakura.bt0070.domain.Bt0070FieldAccess;
import com.sakura.bt0070.runtime.Bt0070Datasets;
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
 * Unit tests for {@link Bt0070Service}, derived from COBOL program BT0070 (fiscal-year close,
 * batch/console — no screen section). Ground truth for inputs/expected values: BT0070.cob PROCEDURE
 * DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Bt0070ServiceTest {

    @Spy private SyscfDataset syscfSpy = new SyscfDataset();
    @Spy private StokfDataset stokfSpy = new StokfDataset();

    private DateutService dateutService = org.mockito.Mockito.mock(DateutService.class);
    private AbortxService abortxService = org.mockito.Mockito.mock(AbortxService.class);

    private Bt0070Service service;
    private Bt0070FieldAccess ws;

    /** Subclass swapping in the spy datasets since Bt0070Datasets builds its own real files. */
    private static class TestDatasets extends Bt0070Datasets {
        private final SyscfDataset syscf;
        private final StokfDataset stokf;

        TestDatasets(SyscfDataset syscf, StokfDataset stokf) {
            this.syscf = syscf;
            this.stokf = stokf;
        }

        @Override
        public SyscfDataset getSyscf() {
            return syscf;
        }

        @Override
        public StokfDataset getStokf() {
            return stokf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Bt0070Datasets fileSet = new TestDatasets(syscfSpy, stokfSpy);
        service = new Bt0070Service(fileSet, dateutService, abortxService);
        ws = getWs();

        doNothing().when(syscfSpy).open(any());
        doNothing().when(syscfSpy).close();
        doReturn("00").when(syscfSpy).getFileStatus();
        doReturn(false).when(syscfSpy).isInvalidKey();
        doReturn(true).when(syscfSpy).readByKey(any());
        doNothing().when(syscfSpy).rewrite();

        doNothing().when(stokfSpy).open(any());
        doNothing().when(stokfSpy).close();
        doReturn("00").when(stokfSpy).getFileStatus();
        doReturn(false).when(stokfSpy).isInvalidKey();
        doReturn(false).when(stokfSpy).isAtEnd();
        doReturn(true).when(stokfSpy).start(anyString(), anyString());
        doReturn(false).when(stokfSpy).readNext();
        doNothing().when(stokfSpy).rewrite();

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

    private Bt0070FieldAccess getWs() throws Exception {
        Field f = Bt0070Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Bt0070FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Bt0070Service.class.getDeclaredMethod(name);
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

    /* ── GTOD-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void loadSystemDate_success_setsSysDateFromDateut() {
        invokePrivate("loadSystemDate");

        assertEquals(20260918, ws.getWkSysdate());
        assertEquals(20260918, ws.getWkSysymd());
        verify(dateutService).execute(any());
    }

    /* ── OPEN-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openProgramFiles_allStatus00_opensBothFilesWithoutAbort() {
        invokePrivate("openProgramFiles");

        verify(syscfSpy, times(1)).open(FileOpenMode.IO);
        verify(stokfSpy, times(1)).open(FileOpenMode.IO);
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openProgramFiles_syscfOpenError_abortsAndSkipsStokf() {
        doReturn("23").when(syscfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openProgramFiles"));

        assertEquals("SYSCF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(stokfSpy, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openProgramFiles_stokfStatus35_reopensAsOutputThenIo() {
        doReturn("35", "00", "00", "00").when(stokfSpy).getFileStatus();

        invokePrivate("openProgramFiles");

        verify(stokfSpy, times(2)).open(FileOpenMode.IO);
        verify(stokfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(stokfSpy, times(1)).close();
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openProgramFiles_stokfStatus30_reopensAsOutputThenIo() {
        doReturn("30", "00", "00", "00").when(stokfSpy).getFileStatus();

        invokePrivate("openProgramFiles");

        verify(stokfSpy, times(2)).open(FileOpenMode.IO);
        verify(stokfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openProgramFiles_stokfOpenErrorAfterRetry_aborts() {
        doReturn("23").when(stokfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openProgramFiles"));

        assertEquals("STOKF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── RSYS-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readSystemControlRecord_found_savesOldYm() {
        ws.setSyCurrYm(202504);

        invokePrivate("readSystemControlRecord");

        assertEquals(202504, ws.getWkOldYm());
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readSystemControlRecord_invalidKey_aborts() {
        doReturn(true).when(syscfSpy).isInvalidKey();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("readSystemControlRecord"));

        assertEquals("SYSCF", ws.getKaFile().trim());
        assertEquals("System control record missing", ws.getKaDetail().trim());
    }

    /* ── CFY-010 — COBOL: IF WK-MM NOT < SY-FISCAL-START ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void computeFiscalYearDefaults_monthEqualsFiscalStart_belongsToCurrentYear() {
        ws.setSyCurrYm(202604);
        ws.setSyFiscalStart(4);

        invokePrivate("computeFiscalYearDefaults");

        assertEquals(2026, ws.getWkYyyy());
        assertEquals(4, ws.getWkMm());
        assertEquals(2026, ws.getWkFyYear());
        assertEquals(2027, ws.getWkNewYear());
        assertEquals(202704, ws.getWkDfltYm());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void computeFiscalYearDefaults_monthBeforeFiscalStart_belongsToPriorYear() {
        ws.setSyCurrYm(202602);
        ws.setSyFiscalStart(4);

        invokePrivate("computeFiscalYearDefaults");

        assertEquals(2025, ws.getWkFyYear());
        assertEquals(2026, ws.getWkNewYear());
        assertEquals(202604, ws.getWkDfltYm());
    }

    /* ── ANF-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptNewFiscalMonth_blankInput_usesDefaultYm() {
        ws.setWkDfltYm(202704);
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("");

            invokePrivate("acceptNewFiscalMonth");

            assertEquals(202704, ws.getWkNewYm());
            assertEquals(0, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptNewFiscalMonth_numericInput_usesOperatorValue() {
        ws.setWkDfltYm(202704);
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("202601");

            invokePrivate("acceptNewFiscalMonth");

            assertEquals(202601, ws.getWkNewYm());
            assertEquals(0, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptNewFiscalMonth_nonNumericInput_abortsAndSkipsMonthCheck() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("ABCDEF");

            invokePrivate("acceptNewFiscalMonth");

            assertEquals(1, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptNewFiscalMonth_monthPartTooHigh_aborts() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("202613");

            invokePrivate("acceptNewFiscalMonth");

            assertEquals(202613, ws.getWkNewYm());
            assertEquals(1, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void acceptNewFiscalMonth_monthPartZero_aborts() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("202600");

            invokePrivate("acceptNewFiscalMonth");

            assertEquals(1, ws.getWkAbortFlg());
        }
    }

    /* ── CONF-010 — COBOL: CONFIRM-YES 88-level covers "Y" and "y" ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmFiscalYearClose_confirmUppercaseY_doesNotAbort() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("Y");

            invokePrivate("confirmFiscalYearClose");

            assertEquals(0, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmFiscalYearClose_confirmLowercaseY_doesNotAbort() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("y");

            invokePrivate("confirmFiscalYearClose");

            assertEquals(0, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmFiscalYearClose_confirmN_setsAbortFlag() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("N");

            invokePrivate("confirmFiscalYearClose");

            assertEquals(1, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmFiscalYearClose_noStdinInput_setsAbortFlag() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn(null);

            invokePrivate("confirmFiscalYearClose");

            assertEquals(1, ws.getWkAbortFlg());
        }
    }

    /* ── PROC-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void resetStockYtdFigures_startInvalidKey_noRecordsProcessedAndNoSummaryPrinted() {
        doReturn(true).when(stokfSpy).isInvalidKey();

        invokePrivate("resetStockYtdFigures");

        assertEquals(1, ws.getEofFlg());
        assertEquals(0, ws.getWkReadCnt());
        assertEquals(0, ws.getWkWhseCnt());
        verify(stokfSpy, never()).readNext();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void resetStockYtdFigures_twoWarehouses_breaksControlAndAccumulatesTotals() {
        AtomicInteger callIndex = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int idx = callIndex.getAndIncrement();
                            switch (idx) {
                                case 0 -> { // whse 10, active
                                    ws.setSkWhse(10);
                                    ws.setSkOnhand(new BigDecimal("100"));
                                    ws.setSkAvgCost(new BigDecimal("2.00"));
                                    ws.setSkYtdIn(new BigDecimal("5"));
                                    ws.setSkYtdOut(new BigDecimal("2"));
                                }
                                case 1 -> { // whse 10, inactive
                                    ws.setSkWhse(10);
                                    ws.setSkOnhand(new BigDecimal("50"));
                                    ws.setSkAvgCost(new BigDecimal("1.50"));
                                    ws.setSkYtdIn(BigDecimal.ZERO);
                                    ws.setSkYtdOut(BigDecimal.ZERO);
                                }
                                case 2 -> { // whse 20 -> control break, active
                                    ws.setSkWhse(20);
                                    ws.setSkOnhand(new BigDecimal("10"));
                                    ws.setSkAvgCost(new BigDecimal("3.00"));
                                    ws.setSkYtdIn(new BigDecimal("1"));
                                    ws.setSkYtdOut(BigDecimal.ZERO);
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

        invokePrivate("resetStockYtdFigures");

        assertEquals(1, ws.getEofFlg());
        assertEquals(3, ws.getWkReadCnt());
        assertEquals(3, ws.getWkResetCnt());
        assertEquals(2, ws.getWkActiveCnt());
        assertEquals(2, ws.getWkWhseCnt());
        assertEquals(160L, ws.getWkOnhandTot());
        assertEquals(new BigDecimal("305.00"), ws.getWkValTot());
        assertEquals(6L, ws.getWkYtdinTot());
        assertEquals(2L, ws.getWkYtdoutTot());
        // last warehouse (20) break summary is printed after loop ends (WK-FIRST-FLG = 0)
        assertEquals(1, ws.getWkWhItems());
        assertEquals(1L, ws.getWkWhYtdin());
        assertEquals(new BigDecimal("30.00"), ws.getWkWhVal());
        verify(stokfSpy, times(3)).rewrite();
    }

    /* ── PNXT-010 ── */

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
    void processNextStockRecord_fstsErrorNotOkOrDup_aborts() {
        doReturn(false).when(stokfSpy).isAtEnd();
        doReturn("99").when(stokfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("processNextStockRecord"));

        assertEquals("STOKF", ws.getKaFile().trim());
        assertEquals("READ NEXT STOKF failed", ws.getKaDetail().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processNextStockRecord_fstsDuplicate02_doesNotAbort() {
        doReturn(false).when(stokfSpy).isAtEnd();
        doReturn("02").when(stokfSpy).getFileStatus();
        ws.setSkOnhand(BigDecimal.ZERO);
        ws.setSkAvgCost(BigDecimal.ZERO);
        ws.setSkYtdIn(BigDecimal.ZERO);
        ws.setSkYtdOut(BigDecimal.ZERO);

        invokePrivate("processNextStockRecord");

        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processNextStockRecord_firstRecord_setsPrevWhseWithoutBreakSummary() {
        ws.setWkFirstFlg(1);
        ws.setWkPrevWhse(0);
        ws.setSkWhse(7);
        ws.setSkOnhand(BigDecimal.ZERO);
        ws.setSkAvgCost(BigDecimal.ZERO);
        ws.setSkYtdIn(BigDecimal.ZERO);
        ws.setSkYtdOut(BigDecimal.ZERO);

        invokePrivate("processNextStockRecord");

        assertEquals(0, ws.getWkFirstFlg());
        assertEquals(7, ws.getWkPrevWhse());
        assertEquals(0, ws.getWkWhseCnt());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processNextStockRecord_warehouseChange_printsSummaryAndResetsSubtotal() {
        ws.setWkFirstFlg(0);
        ws.setWkPrevWhse(10);
        ws.setWkWhItems(4);
        ws.setSkWhse(20);
        ws.setSkOnhand(BigDecimal.ZERO);
        ws.setSkAvgCost(BigDecimal.ZERO);
        ws.setSkYtdIn(BigDecimal.ZERO);
        ws.setSkYtdOut(BigDecimal.ZERO);

        invokePrivate("processNextStockRecord");

        assertEquals(1, ws.getWkWhseCnt());
        assertEquals(20, ws.getWkPrevWhse());
        // subtotal was cleared then RESET-ONE added exactly this one record back
        assertEquals(1, ws.getWkWhItems());
    }

    /* ── RONE-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void resetOneStockRecord_activeItem_accumulatesAndZeroesYtd() {
        ws.setSkOnhand(new BigDecimal("100"));
        ws.setSkAvgCost(new BigDecimal("2.50"));
        ws.setSkYtdIn(new BigDecimal("5"));
        ws.setSkYtdOut(new BigDecimal("2"));

        invokePrivate("resetOneStockRecord");

        assertEquals(new BigDecimal("250.00"), ws.getWkItemVal());
        assertEquals(1, ws.getWkReadCnt());
        assertEquals(1, ws.getWkResetCnt());
        assertEquals(1, ws.getWkActiveCnt());
        assertEquals(100L, ws.getWkOnhandTot());
        assertEquals(0, ws.getSkYtdIn().compareTo(BigDecimal.ZERO));
        assertEquals(0, ws.getSkYtdOut().compareTo(BigDecimal.ZERO));
        verify(stokfSpy).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void resetOneStockRecord_zeroActivity_doesNotIncrementActiveCnt() {
        ws.setSkOnhand(new BigDecimal("10"));
        ws.setSkAvgCost(BigDecimal.ZERO);
        ws.setSkYtdIn(BigDecimal.ZERO);
        ws.setSkYtdOut(BigDecimal.ZERO);

        invokePrivate("resetOneStockRecord");

        assertEquals(0, ws.getWkActiveCnt());
        assertEquals(1, ws.getWkResetCnt());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void resetOneStockRecord_rewriteInvalidKey_aborts() {
        ws.setSkOnhand(BigDecimal.ZERO);
        ws.setSkAvgCost(BigDecimal.ZERO);
        ws.setSkYtdIn(BigDecimal.ZERO);
        ws.setSkYtdOut(BigDecimal.ZERO);
        doReturn(true).when(stokfSpy).isInvalidKey();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("resetOneStockRecord"));

        assertEquals("STOKF", ws.getKaFile().trim());
        assertEquals("REWRITE STOKF failed", ws.getKaDetail().trim());
        // COBOL: ADD 1 TO WK-RESET-CNT happens AFTER END-REWRITE, never reached on abort
        assertEquals(0, ws.getWkResetCnt());
    }

    /* ── CLSB-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearWarehouseBreakTotals_resetsAllSubtotalFields() {
        ws.setWkWhItems(9);
        ws.setWkWhYtdin(9);
        ws.setWkWhYtdout(9);
        ws.setWkWhVal(new BigDecimal("9.00"));

        invokePrivate("clearWarehouseBreakTotals");

        assertEquals(0, ws.getWkWhItems());
        assertEquals(0L, ws.getWkWhYtdin());
        assertEquals(0L, ws.getWkWhYtdout());
        assertEquals(0, ws.getWkWhVal().compareTo(BigDecimal.ZERO));
    }

    /* ── PRSB-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printWarehouseBreakSummary_copiesSubtotalsIntoEditFieldsAndIncrementsCount() {
        ws.setWkPrevWhse(30);
        ws.setWkWhItems(4);
        ws.setWkWhYtdin(100);
        ws.setWkWhYtdout(40);
        ws.setWkWhVal(new BigDecimal("12.34"));

        invokePrivate("printWarehouseBreakSummary");

        assertEquals(1, ws.getWkWhseCnt());
        assertEquals(30, ws.getWkEWhse());
        assertEquals(4, ws.getWkECnt());
        assertEquals(40L, ws.getWkEQty());
        assertEquals(new BigDecimal("12.34"), ws.getWkEVal());
    }

    /* ── USYS-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateSystemControlRecord_success_writesNewYm() {
        ws.setWkNewYm(202704);

        invokePrivate("updateSystemControlRecord");

        assertEquals(202704, ws.getSyCurrYm());
        verify(syscfSpy).rewrite();
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateSystemControlRecord_readInvalidKey_aborts() {
        doReturn(true).when(syscfSpy).isInvalidKey();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("updateSystemControlRecord"));

        assertEquals("Re-read SYSCF failed", ws.getKaDetail().trim());
        verify(syscfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateSystemControlRecord_rewriteInvalidKey_aborts() {
        ws.setWkNewYm(202704);
        doReturn(false, true).when(syscfSpy).isInvalidKey();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("updateSystemControlRecord"));

        assertEquals("REWRITE SYSCF failed", ws.getKaDetail().trim());
    }

    /* ── PSUM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printFiscalCloseSummary_copiesAllAccumulatorsIntoDisplayFields() {
        ws.setWkReadCnt(100);
        ws.setWkResetCnt(100);
        ws.setWkActiveCnt(40);
        ws.setWkWhseCnt(5);
        ws.setWkOnhandTot(5000);
        ws.setWkYtdinTot(300);
        ws.setWkYtdoutTot(200);
        ws.setWkValTot(new BigDecimal("999.99"));
        ws.setWkOldYm(202604);
        ws.setWkNewYm(202704);

        invokePrivate("printFiscalCloseSummary");

        // fields are re-used sequentially; last MOVE wins for each display field
        assertEquals(5, ws.getWkECnt());
        assertEquals(200L, ws.getWkEQty());
        assertEquals(new BigDecimal("999.99"), ws.getWkEVal());
        assertEquals(202704, ws.getWkEYm());
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void closeProgramFiles_closesBothFiles() {
        invokePrivate("closeProgramFiles");

        verify(syscfSpy).close();
        verify(stokfSpy).close();
    }

    /* ── ABND-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abortProgram_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortProgram"));

        assertEquals("BT0070", ws.getKaProgid());
        assertEquals("23", ws.getKaFsts());
        assertEquals("EBATCH", ws.getKaMsgcode());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(syscfSpy).close();
        verify(stokfSpy).close();
    }

    /* ── INIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_confirmed_setsProgidAndReadsSyscfAndCallsConfirm() {
        ws.setSyFiscalStart(4);
        ws.setSyCurrYm(202603);
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("", "Y");

            invokePrivate("initializeProgram");

            assertEquals("BT0070", ws.getWkProgid());
            assertEquals(20260918, ws.getWkSysdate());
            assertEquals(0, ws.getWkAbortFlg());
            util.verify(Utility::readStdinLine, times(2));
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_invalidMonthDuringAccept_abortsAndSkipsConfirmPrompt() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("ABCDEF");

            invokePrivate("initializeProgram");

            assertEquals(1, ws.getWkAbortFlg());
            // CONFIRM-RUN is only performed when WK-ABORT-FLG = 0 after ACCEPT-NEW-FY
            util.verify(Utility::readStdinLine, times(1));
        }
    }

    /* ── MAIN-000 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_confirmedByOperator_processesAndCompletesNormally() {
        ws.setSyFiscalStart(4);
        ws.setSyCurrYm(202603);
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("", "Y");
            doReturn(true)
                    .when(stokfSpy)
                    .isInvalidKey(); // empty stock file on START -> immediate EOF

            assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

            assertEquals(0, ws.getCompletionCode());
            verify(syscfSpy).rewrite();
            verify(syscfSpy).close();
            verify(stokfSpy).close();
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_operatorCancels_skipsProcessingButStillClosesFiles() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("", "N");

            assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

            assertEquals(1, ws.getWkAbortFlg());
            assertEquals(0, ws.getWkReadCnt());
            assertEquals(0, ws.getCompletionCode());
            verify(syscfSpy, never()).rewrite();
            verify(syscfSpy).close();
            verify(stokfSpy).close();
            verify(stokfSpy, never()).readNext();
        }
    }
}

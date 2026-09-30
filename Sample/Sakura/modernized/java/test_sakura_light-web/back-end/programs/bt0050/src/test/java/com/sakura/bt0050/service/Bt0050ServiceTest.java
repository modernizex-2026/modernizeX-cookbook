package com.sakura.bt0050.service;

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
import com.sakura.bt0050.domain.Bt0050FieldAccess;
import com.sakura.bt0050.runtime.Bt0050Datasets;
import com.sakura.dateut.service.DateutService;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.AplfDataset;
import com.sakura.runtime.io.SuppfDataset;
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
 * Unit tests for {@link Bt0050Service}, derived from COBOL program BT0050 (accounts-payable balance
 * rebuild, batch/console — no screen section). Ground truth for inputs/expected values: BT0050.cob
 * PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Bt0050ServiceTest {

    @Spy private AplfDataset aplfSpy = new AplfDataset();
    @Spy private SuppfDataset suppfSpy = new SuppfDataset();

    private DateutService dateutService = org.mockito.Mockito.mock(DateutService.class);
    private AbortxService abortxService = org.mockito.Mockito.mock(AbortxService.class);

    private Bt0050Service service;
    private Bt0050FieldAccess ws;

    /** Subclass swapping in the spy datasets since Bt0050Datasets builds its own real files. */
    private static class TestDatasets extends Bt0050Datasets {
        private final AplfDataset aplf;
        private final SuppfDataset suppf;

        TestDatasets(AplfDataset aplf, SuppfDataset suppf) {
            this.aplf = aplf;
            this.suppf = suppf;
        }

        @Override
        public AplfDataset getAplf() {
            return aplf;
        }

        @Override
        public SuppfDataset getSuppf() {
            return suppf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Bt0050Datasets fileSet = new TestDatasets(aplfSpy, suppfSpy);
        service = new Bt0050Service(fileSet, dateutService, abortxService);
        ws = getWs();

        doNothing().when(aplfSpy).open(any());
        doNothing().when(aplfSpy).close();
        doReturn("00").when(aplfSpy).getFileStatus();
        doReturn(false).when(aplfSpy).isInvalidKey();
        doReturn(false).when(aplfSpy).isAtEnd();
        doReturn(true).when(aplfSpy).start(anyString(), anyString());
        doReturn(false).when(aplfSpy).readNext();
        doNothing().when(aplfSpy).rewrite();

        doNothing().when(suppfSpy).open(any());
        doNothing().when(suppfSpy).close();
        doReturn("00").when(suppfSpy).getFileStatus();
        doReturn(false).when(suppfSpy).isInvalidKey();
        doReturn(false).when(suppfSpy).isAtEnd();
        doReturn(true).when(suppfSpy).start(anyString(), anyString());
        doReturn(false).when(suppfSpy).readNext();
        doNothing().when(suppfSpy).rewrite();
        doReturn(true).when(suppfSpy).readByKey(any());

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

    private Bt0050FieldAccess getWs() throws Exception {
        Field f = Bt0050Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Bt0050FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Bt0050Service.class.getDeclaredMethod(name);
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

            assertEquals("BT0050", ws.getWkProgid());
            assertEquals(20260918, ws.getWkSysdate());
            assertEquals(20260918, ws.getWkSysymd());
            assertEquals(0, ws.getWkAbortFlg());
            verify(aplfSpy).open(FileOpenMode.IO);
            verify(suppfSpy).open(FileOpenMode.IO);
        }
    }

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
    void openFiles_allStatus00_opensBothFilesWithoutAbort() {
        invokePrivate("openFiles");

        verify(aplfSpy, times(1)).open(FileOpenMode.IO);
        verify(suppfSpy, times(1)).open(FileOpenMode.IO);
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openFiles_aplfStatus35_reopensAsOutputThenIO() {
        doReturn("35", "00", "00", "00").when(aplfSpy).getFileStatus();

        invokePrivate("openFiles");

        verify(aplfSpy, times(2)).open(FileOpenMode.IO);
        verify(aplfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(aplfSpy, times(1)).close();
        verify(suppfSpy, times(1)).open(FileOpenMode.IO);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openFiles_aplfOpenErrorAfterRetry_abortsAndSkipsSuppf() {
        doReturn("23").when(aplfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openFiles"));

        assertEquals("APLF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(suppfSpy, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openFiles_suppfOpenError_aborts() {
        doReturn("23").when(suppfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openFiles"));

        assertEquals("SUPPF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── CONF-010 — COBOL: CONFIRM-YES 88-level covers "Y" and "y" ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmProceedWithOperator_confirmUppercaseY_doesNotAbort() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("Y");

            invokePrivate("confirmProceedWithOperator");

            assertEquals(0, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmProceedWithOperator_confirmLowercaseY_doesNotAbort() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("y");

            invokePrivate("confirmProceedWithOperator");

            assertEquals(0, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmProceedWithOperator_confirmN_setsAbortFlag() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("N");

            invokePrivate("confirmProceedWithOperator");

            assertEquals(1, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmProceedWithOperator_noStdinInput_setsAbortFlag() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn(null);

            invokePrivate("confirmProceedWithOperator");

            assertEquals(1, ws.getWkAbortFlg());
        }
    }

    /* ── CLRM-010 / CLRX-010 — supplier balance clearing ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearSupplierBalances_startInvalidKey_noRecordsProcessed() {
        doReturn(true).when(suppfSpy).isInvalidKey();

        invokePrivate("clearSupplierBalances");

        assertEquals(1, ws.getEofFlg());
        assertEquals(0, ws.getWkZeroRead());
        verify(suppfSpy, never()).readNext();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearSupplierBalances_mixedBalances_clearsOnlyNonzeroAndCountsAll() {
        AtomicInteger callIndex = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int idx = callIndex.getAndIncrement();
                            switch (idx) {
                                case 0 -> ws.setSpBalance(new BigDecimal("100"));
                                case 1 -> ws.setSpBalance(BigDecimal.ZERO);
                                default -> {
                                    /* no more records */
                                }
                            }
                            return idx < 2;
                        })
                .when(suppfSpy)
                .readNext();
        doReturn(false, false, true).when(suppfSpy).isAtEnd();

        invokePrivate("clearSupplierBalances");

        assertEquals(1, ws.getEofFlg());
        assertEquals(2, ws.getWkZeroRead());
        assertEquals(1, ws.getWkZeroClr());
        verify(suppfSpy, times(1)).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearNextSupplierBalance_atEnd_setsEofFlgAndSkipsFurtherProcessing() {
        doReturn(true).when(suppfSpy).isAtEnd();

        invokePrivate("clearNextSupplierBalance");

        assertEquals(1, ws.getEofFlg());
        verify(suppfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearNextSupplierBalance_zeroBalance_readOnlyNoRewrite() {
        doReturn(false).when(suppfSpy).isAtEnd();
        ws.setSpBalance(BigDecimal.ZERO);

        invokePrivate("clearNextSupplierBalance");

        assertEquals(1, ws.getWkZeroRead());
        assertEquals(0, ws.getWkZeroClr());
        verify(suppfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearNextSupplierBalance_rewriteInvalidKey_aborts() {
        doReturn(false).when(suppfSpy).isAtEnd();
        ws.setSpBalance(new BigDecimal("50"));
        doReturn(true).when(suppfSpy).isInvalidKey();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("clearNextSupplierBalance"));

        assertEquals("SUPPF", ws.getKaFile().trim());
        assertEquals("Clear SUPPF balance failed", ws.getKaDetail().trim());
        // COBOL: ADD 1 TO WK-ZERO-CLR happens AFTER END-REWRITE, never reached on abort
        assertEquals(0, ws.getWkZeroClr());
    }

    /* ── PROC-010 / PNXT-010 — ledger walk with supplier control break ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void rebuildLedgerBalances_startInvalidKey_noEntriesAndNoWriteback() {
        doReturn(true).when(aplfSpy).isInvalidKey();

        invokePrivate("rebuildLedgerBalances");

        assertEquals(1, ws.getEofFlg());
        assertEquals(0, ws.getWkLedgCnt());
        assertEquals(1, ws.getWkFirstFlg());
        verify(aplfSpy, never()).readNext();
        verify(suppfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void rebuildLedgerBalances_twoSuppliersInOrder_breaksControlAndWritesBackLastSupplier() {
        AtomicInteger callIndex = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int idx = callIndex.getAndIncrement();
                            switch (idx) {
                                case 0 -> { // supplier 1, credit 100
                                    ws.setPlSupp(1);
                                    ws.setPlCredit(new BigDecimal("100"));
                                    ws.setPlDebit(BigDecimal.ZERO);
                                }
                                case 1 -> { // supplier 1, debit 30 -> running bal 70
                                    ws.setPlSupp(1);
                                    ws.setPlCredit(BigDecimal.ZERO);
                                    ws.setPlDebit(new BigDecimal("30"));
                                }
                                case 2 -> { // supplier 2 -> control break, writes back supplier 1
                                    // bal=70
                                    ws.setPlSupp(2);
                                    ws.setPlCredit(new BigDecimal("40"));
                                    ws.setPlDebit(BigDecimal.ZERO);
                                }
                                default -> {
                                    /* no more records */
                                }
                            }
                            return idx < 3;
                        })
                .when(aplfSpy)
                .readNext();
        doReturn(false, false, false, true).when(aplfSpy).isAtEnd();

        invokePrivate("rebuildLedgerBalances");

        assertEquals(1, ws.getEofFlg());
        assertEquals(3, ws.getWkLedgCnt());
        assertEquals(2, ws.getWkSuppCnt());
        assertEquals(2, ws.getWkUpdCnt());
        assertEquals(new BigDecimal("40"), ws.getPlBalance());
        assertEquals(30L, ws.getWkDebitTot());
        assertEquals(140L, ws.getWkCreditTot());
        verify(suppfSpy, times(2)).readByKey(any());
        verify(suppfSpy, times(2)).rewrite();
        verify(aplfSpy, times(3)).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processNextLedgerEntry_atEnd_setsEofFlgAndSkipsFurtherProcessing() {
        doReturn(true).when(aplfSpy).isAtEnd();

        invokePrivate("processNextLedgerEntry");

        assertEquals(1, ws.getEofFlg());
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processNextLedgerEntry_fstsErrorNotOkOrDup_aborts() {
        doReturn(false).when(aplfSpy).isAtEnd();
        doReturn("99").when(aplfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("processNextLedgerEntry"));

        assertEquals("APLF", ws.getKaFile().trim());
        assertEquals("READ NEXT APLF failed", ws.getKaDetail().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processNextLedgerEntry_fstsDuplicate02_doesNotAbort() {
        doReturn(false).when(aplfSpy).isAtEnd();
        doReturn("02").when(aplfSpy).getFileStatus();
        ws.setPlSupp(0);
        ws.setPlCredit(BigDecimal.ZERO);
        ws.setPlDebit(BigDecimal.ZERO);

        invokePrivate("processNextLedgerEntry");

        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processNextLedgerEntry_firstRecord_setsPrevSuppWithoutWriteback() {
        ws.setWkFirstFlg(1);
        ws.setWkPrevSupp(0);
        ws.setPlSupp(7);
        ws.setPlCredit(BigDecimal.ZERO);
        ws.setPlDebit(BigDecimal.ZERO);

        invokePrivate("processNextLedgerEntry");

        assertEquals(0, ws.getWkFirstFlg());
        assertEquals(7, ws.getWkPrevSupp());
        assertEquals(0, ws.getWkSuppCnt());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processNextLedgerEntry_supplierChange_writesBackAndResetsRunningBalance() {
        ws.setWkFirstFlg(0);
        ws.setWkPrevSupp(3);
        ws.setWkRunBal(999);
        ws.setPlSupp(4);
        ws.setPlCredit(new BigDecimal("10"));
        ws.setPlDebit(BigDecimal.ZERO);

        invokePrivate("processNextLedgerEntry");

        assertEquals(1, ws.getWkSuppCnt());
        assertEquals(4, ws.getWkPrevSupp());
        // running balance reset to 0 before this entry's credit was applied
        assertEquals(10L, ws.getWkRunBal());
    }

    /* ── PENT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void applyLedgerEntryToBalance_creditAndDebit_recomputesRunningBalanceAndTotals() {
        ws.setWkRunBal(50);
        ws.setPlCredit(new BigDecimal("30"));
        ws.setPlDebit(new BigDecimal("20"));

        invokePrivate("applyLedgerEntryToBalance");

        assertEquals(1, ws.getWkLedgCnt());
        assertEquals(60L, ws.getWkRunBal());
        assertEquals(20L, ws.getWkDebitTot());
        assertEquals(30L, ws.getWkCreditTot());
        assertEquals(BigDecimal.valueOf(60), ws.getPlBalance());
        verify(aplfSpy).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void applyLedgerEntryToBalance_rewriteInvalidKey_aborts() {
        ws.setWkRunBal(0);
        ws.setPlCredit(BigDecimal.ZERO);
        ws.setPlDebit(BigDecimal.ZERO);
        doReturn(true).when(aplfSpy).isInvalidKey();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("applyLedgerEntryToBalance"));

        assertEquals("APLF", ws.getKaFile().trim());
        assertEquals("REWRITE APLF failed", ws.getKaDetail().trim());
    }

    /* ── USUP-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateSupplierBalance_prevSuppZero_returnsImmediatelyWithoutReadingSuppf() {
        ws.setWkPrevSupp(0);

        invokePrivate("updateSupplierBalance");

        assertEquals(0, ws.getWkSuppCnt());
        verify(suppfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateSupplierBalance_found_writesBalanceAndAudit() {
        ws.setWkPrevSupp(5);
        ws.setWkRunBal(250);
        ws.setWkSysdate(20260918);
        ws.setWkUserCode(1);

        invokePrivate("updateSupplierBalance");

        assertEquals(1, ws.getWkSuppCnt());
        assertEquals(BigDecimal.valueOf(250), ws.getSpBalance());
        assertEquals(1, ws.getWkUpdCnt());
        assertEquals(1, ws.getWkNonzeroCnt());
        assertEquals(250L, ws.getWkBalTot());
        verify(suppfSpy).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateSupplierBalance_zeroRunningBalance_noAuditLine() {
        ws.setWkPrevSupp(5);
        ws.setWkRunBal(0);

        invokePrivate("updateSupplierBalance");

        assertEquals(1, ws.getWkUpdCnt());
        assertEquals(0, ws.getWkNonzeroCnt());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateSupplierBalance_notFound_incrementsNfCntAndSkipsRewrite() {
        ws.setWkPrevSupp(5);
        ws.setWkRunBal(100);
        doReturn(true).when(suppfSpy).isInvalidKey();

        invokePrivate("updateSupplierBalance");

        assertEquals(1, ws.getWkNfCnt());
        assertEquals(0, ws.getWkUpdCnt());
        verify(suppfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateSupplierBalance_rewriteInvalidKey_aborts() {
        ws.setWkPrevSupp(5);
        ws.setWkRunBal(100);
        doReturn(false, true).when(suppfSpy).isInvalidKey();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("updateSupplierBalance"));

        assertEquals("SUPPF", ws.getKaFile().trim());
        assertEquals("REWRITE SUPPF failed", ws.getKaDetail().trim());
        // COBOL: ADD 1 TO WK-UPD-CNT happens AFTER END-REWRITE, never reached on abort
        assertEquals(0, ws.getWkUpdCnt());
    }

    /* ── PSUM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printSummaryReport_copiesAllAccumulatorsIntoDisplayFields() {
        ws.setWkZeroRead(10);
        ws.setWkZeroClr(3);
        ws.setWkLedgCnt(20);
        ws.setWkSuppCnt(4);
        ws.setWkUpdCnt(4);
        ws.setWkNfCnt(0);
        ws.setWkNonzeroCnt(2);
        ws.setWkDebitTot(500);
        ws.setWkCreditTot(800);
        ws.setWkBalTot(300);

        invokePrivate("printSummaryReport");

        assertEquals(2, ws.getWkECnt());
        assertEquals(300L, ws.getWkEAmt());
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void closeFiles_closesBothFiles() {
        invokePrivate("closeFiles");

        verify(aplfSpy).close();
        verify(suppfSpy).close();
    }

    /* ── ABND-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abortProgram_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortProgram"));

        assertEquals("BT0050", ws.getKaProgid());
        assertEquals("23", ws.getKaFsts());
        assertEquals("EBATCH", ws.getKaMsgcode());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(aplfSpy).close();
        verify(suppfSpy).close();
    }

    /* ── MAIN-000 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_confirmedByOperator_processesRecordsAndCompletesNormally() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("Y");
            doReturn(true)
                    .when(suppfSpy)
                    .isInvalidKey(); // empty supplier file on START -> immediate EOF
            doReturn(true)
                    .when(aplfSpy)
                    .isInvalidKey(); // empty ledger file on START -> immediate EOF

            assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

            assertEquals(0, ws.getCompletionCode());
            verify(aplfSpy).close();
            verify(suppfSpy).close();
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
            assertEquals(0, ws.getWkLedgCnt());
            assertEquals(0, ws.getCompletionCode());
            verify(aplfSpy).close();
            verify(suppfSpy).close();
            verify(aplfSpy, never()).readNext();
            verify(suppfSpy, never()).readNext();
        }
    }
}

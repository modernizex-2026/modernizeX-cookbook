package com.sakura.bt0040.service;

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
import com.sakura.bt0040.domain.Bt0040FieldAccess;
import com.sakura.bt0040.runtime.Bt0040Datasets;
import com.sakura.dateut.service.DateutService;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.ArlfDataset;
import com.sakura.runtime.io.CustfDataset;
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
 * Unit tests for {@link Bt0040Service}, derived from COBOL program BT0040 (accounts-receivable
 * balance rebuild, batch/console — no screen section). Ground truth for inputs/expected values:
 * BT0040.cob PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Bt0040ServiceTest {

    @Spy private ArlfDataset arlfSpy = new ArlfDataset();
    @Spy private CustfDataset custfSpy = new CustfDataset();

    @Mock private DateutService dateutService;
    @Mock private AbortxService abortxService;

    private Bt0040Service service;
    private Bt0040FieldAccess ws;

    /** Subclass swapping in the spy datasets since Bt0040Datasets builds its own real files. */
    private static class TestDatasets extends Bt0040Datasets {
        private final ArlfDataset arlf;
        private final CustfDataset custf;

        TestDatasets(ArlfDataset arlf, CustfDataset custf) {
            this.arlf = arlf;
            this.custf = custf;
        }

        @Override
        public ArlfDataset getArlf() {
            return arlf;
        }

        @Override
        public CustfDataset getCustf() {
            return custf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Bt0040Datasets fileSet = new TestDatasets(arlfSpy, custfSpy);
        service = new Bt0040Service(fileSet, dateutService, abortxService);
        ws = getWs();

        doNothing().when(arlfSpy).open(any());
        doNothing().when(arlfSpy).close();
        doReturn("00").when(arlfSpy).getFileStatus();
        doReturn(false).when(arlfSpy).isInvalidKey();
        doReturn(false).when(arlfSpy).isAtEnd();
        doReturn(true).when(arlfSpy).start(anyString(), anyString());
        doReturn(false).when(arlfSpy).readNext();
        doNothing().when(arlfSpy).rewrite();

        doNothing().when(custfSpy).open(any());
        doNothing().when(custfSpy).close();
        doReturn("00").when(custfSpy).getFileStatus();
        doReturn(false).when(custfSpy).isInvalidKey();
        doReturn(false).when(custfSpy).isAtEnd();
        doReturn(true).when(custfSpy).start(anyString(), anyString());
        doReturn(false).when(custfSpy).readNext();
        doNothing().when(custfSpy).rewrite();
        doReturn(true).when(custfSpy).readByKey(any());

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

    private Bt0040FieldAccess getWs() throws Exception {
        Field f = Bt0040Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Bt0040FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Bt0040Service.class.getDeclaredMethod(name);
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

            assertEquals("BT0040", ws.getWkProgid());
            assertEquals(20260918, ws.getWkSysdate());
            assertEquals(20260918, ws.getWkSysymd());
            assertEquals(0, ws.getWkAbortFlg());
            verify(arlfSpy).open(FileOpenMode.IO);
            verify(custfSpy).open(FileOpenMode.IO);
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

        verify(arlfSpy, times(1)).open(FileOpenMode.IO);
        verify(custfSpy, times(1)).open(FileOpenMode.IO);
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openFiles_arlfStatus35_reopensAsOutputThenIO() {
        doReturn("35", "00", "00", "00").when(arlfSpy).getFileStatus();

        invokePrivate("openFiles");

        verify(arlfSpy, times(2)).open(FileOpenMode.IO);
        verify(arlfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(arlfSpy, times(1)).close();
        verify(custfSpy, times(1)).open(FileOpenMode.IO);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openFiles_arlfOpenErrorAfterRetry_abortsAndSkipsCustf() {
        doReturn("23").when(arlfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openFiles"));

        assertEquals("ARLF", ws.getKaFile().trim());
        assertEquals("Open ARLF failed", ws.getKaDetail().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(custfSpy, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openFiles_custfOpenError_aborts() {
        doReturn("23").when(custfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openFiles"));

        assertEquals("CUSTF", ws.getKaFile().trim());
        assertEquals("Open CUSTF failed", ws.getKaDetail().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── CONF-010 — COBOL: CONFIRM-YES 88-level covers "Y" and "y" ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmWithOperator_confirmUppercaseY_doesNotAbort() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("Y");

            invokePrivate("confirmWithOperator");

            assertEquals(0, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmWithOperator_confirmLowercaseY_doesNotAbort() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("y");

            invokePrivate("confirmWithOperator");

            assertEquals(0, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmWithOperator_confirmN_setsAbortFlag() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("N");

            invokePrivate("confirmWithOperator");

            assertEquals(1, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmWithOperator_noStdinInput_setsAbortFlag() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn(null);

            invokePrivate("confirmWithOperator");

            assertEquals(1, ws.getWkAbortFlg());
        }
    }

    /* ── CLRM-010 / CLRX-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearCustomerBalances_startInvalidKey_noRecordsProcessed() {
        doReturn(true).when(custfSpy).isInvalidKey();

        invokePrivate("clearCustomerBalances");

        assertEquals(1, ws.getEofFlg());
        assertEquals(0, ws.getWkZeroRead());
        verify(custfSpy, never()).readNext();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearCustomerBalances_multipleRecords_clearsOnlyNonzeroBalances() {
        AtomicInteger callIndex = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int idx = callIndex.getAndIncrement();
                            switch (idx) {
                                case 0 -> ws.setCuBalance(
                                        new BigDecimal("100.00")); // has balance -> cleared
                                case 1 -> ws.setCuBalance(
                                        BigDecimal.ZERO); // already zero -> no rewrite
                                default -> {
                                    /* no more records */
                                }
                            }
                            return idx < 2;
                        })
                .when(custfSpy)
                .readNext();
        doReturn(false, false, true).when(custfSpy).isAtEnd();

        invokePrivate("clearCustomerBalances");

        assertEquals(1, ws.getEofFlg());
        assertEquals(2, ws.getWkZeroRead());
        assertEquals(1, ws.getWkZeroClr());
        verify(custfSpy, times(1)).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearNextCustomerBalance_atEnd_setsEofFlgAndSkipsIncrement() {
        doReturn(true).when(custfSpy).isAtEnd();

        invokePrivate("clearNextCustomerBalance");

        assertEquals(1, ws.getEofFlg());
        assertEquals(0, ws.getWkZeroRead());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearNextCustomerBalance_nonzeroBalance_clearsAndRewrites() {
        ws.setCuBalance(new BigDecimal("50.00"));

        invokePrivate("clearNextCustomerBalance");

        assertEquals(0, ws.getCuBalance().compareTo(BigDecimal.ZERO));
        assertEquals(1, ws.getWkZeroRead());
        assertEquals(1, ws.getWkZeroClr());
        verify(custfSpy).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearNextCustomerBalance_zeroBalance_noRewrite() {
        ws.setCuBalance(BigDecimal.ZERO);

        invokePrivate("clearNextCustomerBalance");

        assertEquals(1, ws.getWkZeroRead());
        assertEquals(0, ws.getWkZeroClr());
        verify(custfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void clearNextCustomerBalance_rewriteInvalidKey_aborts() {
        ws.setCuBalance(new BigDecimal("10.00"));
        doReturn(true).when(custfSpy).isInvalidKey();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("clearNextCustomerBalance"));

        assertEquals("CUSTF", ws.getKaFile().trim());
        assertEquals("Clear CUSTF balance failed", ws.getKaDetail().trim());
        // COBOL: ADD 1 TO WK-ZERO-CLR is after END-REWRITE, never reached on abort
        assertEquals(0, ws.getWkZeroClr());
    }

    /* ── PROC-010 / PNXT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void rebuildLedgerBalances_startInvalidKey_noRecordsAndNoFinalUpdate() {
        doReturn(true).when(arlfSpy).isInvalidKey();

        invokePrivate("rebuildLedgerBalances");

        assertEquals(1, ws.getEofFlg());
        assertEquals(0, ws.getWkLedgCnt());
        assertEquals(0, ws.getWkCustCnt());
        verify(arlfSpy, never()).readNext();
        verify(custfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void rebuildLedgerBalances_singleCustomerMultipleEntries_accumulatesAndUpdatesAtEnd() {
        AtomicInteger callIndex = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int idx = callIndex.getAndIncrement();
                            switch (idx) {
                                case 0 -> {
                                    ws.setAlCust(100);
                                    ws.setAlDebit(new BigDecimal("200"));
                                    ws.setAlCredit(new BigDecimal("50"));
                                }
                                case 1 -> {
                                    ws.setAlCust(100);
                                    ws.setAlDebit(new BigDecimal("30"));
                                    ws.setAlCredit(new BigDecimal("10"));
                                }
                                default -> {
                                    /* no more records */
                                }
                            }
                            return idx < 2;
                        })
                .when(arlfSpy)
                .readNext();
        doReturn(false, false, true).when(arlfSpy).isAtEnd();

        invokePrivate("rebuildLedgerBalances");

        assertEquals(1, ws.getEofFlg());
        assertEquals(2, ws.getWkLedgCnt());
        assertEquals(1, ws.getWkCustCnt());
        assertEquals(1, ws.getWkUpdCnt());
        // running balance = (200-50) + (30-10) = 170
        assertEquals(170L, ws.getWkRunBal());
        assertEquals(0, ws.getCuBalance().compareTo(new BigDecimal("170")));
        verify(custfSpy).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processNextLedgerEntry_atEnd_setsEofFlgAndSkipsFurtherProcessing() {
        doReturn(true).when(arlfSpy).isAtEnd();

        invokePrivate("processNextLedgerEntry");

        assertEquals(1, ws.getEofFlg());
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processNextLedgerEntry_fstsErrorNotAtEnd_aborts() {
        doReturn(false).when(arlfSpy).isAtEnd();
        doReturn("99").when(arlfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("processNextLedgerEntry"));

        assertEquals("ARLF", ws.getKaFile().trim());
        assertEquals("READ NEXT ARLF failed", ws.getKaDetail().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processNextLedgerEntry_firstRecord_setsPrevCustWithoutControlBreak() {
        ws.setWkFirstFlg(1);
        ws.setWkPrevCust(0);
        ws.setAlCust(500);
        ws.setAlDebit(BigDecimal.TEN);
        ws.setAlCredit(BigDecimal.ZERO);

        invokePrivate("processNextLedgerEntry");

        assertEquals(0, ws.getWkFirstFlg());
        assertEquals(500, ws.getWkPrevCust());
        assertEquals(1, ws.getWkLedgCnt());
        verify(custfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processNextLedgerEntry_customerChange_triggersUpdateCustomerBalanceAndResets() {
        ws.setWkFirstFlg(0);
        ws.setWkPrevCust(300);
        ws.setWkRunBal(1000);
        ws.setAlCust(301);
        ws.setAlDebit(BigDecimal.ZERO);
        ws.setAlCredit(BigDecimal.ZERO);

        invokePrivate("processNextLedgerEntry");

        verify(custfSpy).readByKey(any());
        assertEquals(301, ws.getWkPrevCust());
        assertEquals(1, ws.getWkCustCnt());
        assertEquals(0L, ws.getWkRunBal());
    }

    /* ── PENT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void postLedgerEntry_accumulatesBalanceAndRewrites() {
        ws.setWkRunBal(0);
        ws.setAlDebit(new BigDecimal("100"));
        ws.setAlCredit(new BigDecimal("30"));

        invokePrivate("postLedgerEntry");

        assertEquals(70L, ws.getWkRunBal());
        assertEquals(1, ws.getWkLedgCnt());
        assertEquals(100L, ws.getWkDebitTot());
        assertEquals(30L, ws.getWkCreditTot());
        assertEquals(0, ws.getAlBalance().compareTo(new BigDecimal("70")));
        verify(arlfSpy).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void postLedgerEntry_rewriteInvalidKey_aborts() {
        ws.setAlDebit(BigDecimal.ZERO);
        ws.setAlCredit(BigDecimal.ZERO);
        doReturn(true).when(arlfSpy).isInvalidKey();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("postLedgerEntry"));

        assertEquals("ARLF", ws.getKaFile().trim());
        assertEquals("REWRITE ARLF failed", ws.getKaDetail().trim());
    }

    /* ── UCUS-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateCustomerBalance_prevCustZero_doesNothing() {
        ws.setWkPrevCust(0);

        invokePrivate("updateCustomerBalance");

        assertEquals(0, ws.getWkCustCnt());
        verify(custfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateCustomerBalance_customerNotFound_incrementsNfCntAndSkipsRewrite() {
        ws.setWkPrevCust(400);
        ws.setWkRunBal(500);
        doReturn(true).when(custfSpy).isInvalidKey();

        invokePrivate("updateCustomerBalance");

        assertEquals(1, ws.getWkNfCnt());
        assertEquals(1, ws.getWkCustCnt());
        verify(custfSpy, never()).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateCustomerBalance_foundNonzeroBalance_updatesAndAudits() {
        ws.setWkPrevCust(500);
        ws.setWkRunBal(750);
        ws.setWkSysdate(20260918);
        ws.setWkUserCode(42);

        invokePrivate("updateCustomerBalance");

        assertEquals(1, ws.getWkCustCnt());
        assertEquals(750L, ws.getWkBalTot());
        assertEquals(0, ws.getCuBalance().compareTo(new BigDecimal("750")));
        assertEquals(20260918, ws.getCuUpdDate());
        assertEquals(42, ws.getCuUpdUser());
        assertEquals(1, ws.getWkUpdCnt());
        assertEquals(1, ws.getWkNonzeroCnt());
        assertEquals(500, ws.getWkECode());
        assertEquals(750L, ws.getWkEAmt());
        verify(custfSpy).rewrite();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateCustomerBalance_foundZeroBalance_noAuditIncrement() {
        ws.setWkPrevCust(600);
        ws.setWkRunBal(0);

        invokePrivate("updateCustomerBalance");

        assertEquals(1, ws.getWkUpdCnt());
        assertEquals(0, ws.getWkNonzeroCnt());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void updateCustomerBalance_rewriteInvalidKey_aborts() {
        ws.setWkPrevCust(700);
        ws.setWkRunBal(10);
        doReturn(false, true).when(custfSpy).isInvalidKey();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("updateCustomerBalance"));

        assertEquals("CUSTF", ws.getKaFile().trim());
        assertEquals("REWRITE CUSTF failed", ws.getKaDetail().trim());
        // COBOL: ADD 1 TO WK-UPD-CNT is after END-REWRITE, never reached on abort
        assertEquals(0, ws.getWkUpdCnt());
    }

    /* ── PSUM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printSummaryReport_copiesAllAccumulatorsIntoDisplayFields() {
        ws.setWkZeroRead(5);
        ws.setWkZeroClr(2);
        ws.setWkLedgCnt(10);
        ws.setWkCustCnt(3);
        ws.setWkUpdCnt(3);
        ws.setWkNfCnt(0);
        ws.setWkNonzeroCnt(2);
        ws.setWkDebitTot(1000);
        ws.setWkCreditTot(400);
        ws.setWkBalTot(600);

        invokePrivate("printSummaryReport");

        // last-assigned display fields reflect the final MOVE statements in COBOL
        assertEquals(2, ws.getWkECnt());
        assertEquals(600L, ws.getWkEAmt());
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void closeFiles_closesBothFiles() {
        invokePrivate("closeFiles");

        verify(arlfSpy).close();
        verify(custfSpy).close();
    }

    /* ── ABND-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abortProgram_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortProgram"));

        assertEquals("BT0040", ws.getKaProgid());
        assertEquals("23", ws.getKaFsts());
        assertEquals("EBATCH", ws.getKaMsgcode());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(arlfSpy).close();
        verify(custfSpy).close();
    }

    /* ── MAIN-000 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_confirmedByOperator_processesRecordsAndCompletesNormally() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("Y");
            doReturn(true)
                    .when(custfSpy)
                    .isInvalidKey(); // empty custf/arlf -> immediate EOF both loops
            doReturn(true).when(arlfSpy).isInvalidKey();

            assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

            assertEquals(0, ws.getCompletionCode());
            verify(arlfSpy).close();
            verify(custfSpy).close();
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
            verify(arlfSpy).close();
            verify(custfSpy).close();
            verify(arlfSpy, never()).readNext();
        }
    }
}

package com.sakura.bt0090.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.abortx.service.AbortxService;
import com.sakura.bt0090.domain.Bt0090FieldAccess;
import com.sakura.bt0090.runtime.Bt0090Datasets;
import com.sakura.dateut.service.DateutService;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.InvdfDataset;
import com.sakura.runtime.io.InvhfDataset;
import com.sakura.runtime.io.OrddfDataset;
import com.sakura.runtime.io.OrdhfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.RepfDataset;
import com.sakura.runtime.io.StokfDataset;
import com.sakura.runtime.io.SyscfDataset;
import com.sakura.runtime.linkage.DateutLinkParm;

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
 * Unit tests for {@link Bt0090Service}, derived from COBOL program BT0090 (data integrity check —
 * batch/console utility scanning ORDDF/ORDHF/INVDF/INVHF/STOKF/PRODF for referential and value
 * problems; no screen section). Ground truth for inputs/expected values: BT0090.cob PROCEDURE
 * DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Bt0090ServiceTest {

    @Spy private OrddfDataset orddfSpy = new OrddfDataset();
    @Spy private OrdhfDataset ordhfSpy = new OrdhfDataset();
    @Spy private InvdfDataset invdfSpy = new InvdfDataset();
    @Spy private InvhfDataset invhfSpy = new InvhfDataset();
    @Spy private StokfDataset stokfSpy = new StokfDataset();
    @Spy private ProdfDataset prodfSpy = new ProdfDataset();
    @Spy private SyscfDataset syscfSpy = new SyscfDataset();
    @Spy private RepfDataset repfSpy = new RepfDataset();

    private final DateutService dateutService = mock(DateutService.class);
    private final AbortxService abortxService = mock(AbortxService.class);

    private Bt0090Service service;
    private Bt0090FieldAccess ws;

    /** Subclass swapping in the spy datasets since Bt0090Datasets builds its own real files. */
    private static class TestDatasets extends Bt0090Datasets {
        private final OrddfDataset orddf;
        private final OrdhfDataset ordhf;
        private final InvdfDataset invdf;
        private final InvhfDataset invhf;
        private final StokfDataset stokf;
        private final ProdfDataset prodf;
        private final SyscfDataset syscf;
        private final RepfDataset repf;

        TestDatasets(
                OrddfDataset orddf,
                OrdhfDataset ordhf,
                InvdfDataset invdf,
                InvhfDataset invhf,
                StokfDataset stokf,
                ProdfDataset prodf,
                SyscfDataset syscf,
                RepfDataset repf) {
            this.orddf = orddf;
            this.ordhf = ordhf;
            this.invdf = invdf;
            this.invhf = invhf;
            this.stokf = stokf;
            this.prodf = prodf;
            this.syscf = syscf;
            this.repf = repf;
        }

        @Override
        public OrddfDataset getOrddf() {
            return orddf;
        }

        @Override
        public OrdhfDataset getOrdhf() {
            return ordhf;
        }

        @Override
        public InvdfDataset getInvdf() {
            return invdf;
        }

        @Override
        public InvhfDataset getInvhf() {
            return invhf;
        }

        @Override
        public StokfDataset getStokf() {
            return stokf;
        }

        @Override
        public ProdfDataset getProdf() {
            return prodf;
        }

        @Override
        public SyscfDataset getSyscf() {
            return syscf;
        }

        @Override
        public RepfDataset getRepf() {
            return repf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Bt0090Datasets fileSet =
                new TestDatasets(
                        orddfSpy, ordhfSpy, invdfSpy, invhfSpy, stokfSpy, prodfSpy, syscfSpy,
                        repfSpy);
        service = new Bt0090Service(fileSet, dateutService, abortxService);
        ws = getWs();

        for (var spy :
                new Object[] {
                    orddfSpy, ordhfSpy, invdfSpy, invhfSpy, stokfSpy, prodfSpy, syscfSpy, repfSpy
                }) {
            var raw = (com.sakura.runtime.record.RawDatasetBase) spy;
            doNothing().when(raw).open(any());
            doNothing().when(raw).close();
            doReturn("00").when(raw).getFileStatus();
            doReturn(false).when(raw).isInvalidKey();
            doReturn(false).when(raw).isAtEnd();
            doReturn(true).when(raw).start(anyString(), anyString());
            doReturn(false).when(raw).readNext();
            doReturn(true).when(raw).readByKey(any());
            doNothing().when(raw).write();
        }

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

    /* ── reflection helpers ── */

    private Bt0090FieldAccess getWs() throws Exception {
        Field f = Bt0090Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Bt0090FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Bt0090Service.class.getDeclaredMethod(name);
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
        verify(dateutService).execute(any());
    }

    /* ── OPEN-010 / company lookup ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void loadCompanyNameFromSyscf_keyFound_usesCompanyNameFromRecord() {
        doAnswer(
                        inv -> {
                            ws.setSyCompanyName("ACME TRADING CO");
                            return true;
                        })
                .when(syscfSpy)
                .readByKey(any());

        invokePrivate("loadCompanyNameFromSyscf");

        assertEquals("ACME TRADING CO", ws.getWkCompany().trim());
        verify(syscfSpy).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void loadCompanyNameFromSyscf_keyNotFound_keepsDefaultCompany() {
        ws.setWkCompany("SAKURA Sales Management System");
        doReturn(true).when(syscfSpy).isInvalidKey();

        invokePrivate("loadCompanyNameFromSyscf");

        assertEquals("SAKURA Sales Management System", ws.getWkCompany().trim());
        verify(syscfSpy).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void loadCompanyNameFromSyscf_openFails_skipsLookupAndClose() {
        doReturn("35").when(syscfSpy).getFileStatus();

        invokePrivate("loadCompanyNameFromSyscf");

        verify(syscfSpy, never()).readByKey(any());
        verify(syscfSpy, never()).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openAllFiles_allStatus00_opensAllSixInputFilesAndReportOutput() {
        invokePrivate("openAllFiles");

        verify(orddfSpy).open(FileOpenMode.INPUT);
        verify(ordhfSpy).open(FileOpenMode.INPUT);
        verify(invdfSpy).open(FileOpenMode.INPUT);
        verify(invhfSpy).open(FileOpenMode.INPUT);
        verify(stokfSpy).open(FileOpenMode.INPUT);
        verify(prodfSpy).open(FileOpenMode.INPUT);
        verify(repfSpy).open(FileOpenMode.OUTPUT);
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openAllFiles_orddfStatus35_reopensAsOutputThenInput() {
        doReturn("35", "00", "00", "00").when(orddfSpy).getFileStatus();

        invokePrivate("openAllFiles");

        verify(orddfSpy, times(2)).open(FileOpenMode.INPUT);
        verify(orddfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(orddfSpy, times(1)).close();
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openAllFiles_ordhfOpenErrorAfterRetry_abendsWithOrdhfLabel() {
        doReturn("23").when(ordhfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openAllFiles"));

        assertEquals("ORDHF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(invdfSpy, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openAllFiles_repfOpenFails_abendsWithReprLabel() {
        doReturn("35").when(repfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openAllFiles"));

        assertEquals("REPF", ws.getKaFile().trim());
        assertEquals("Open report file failed", ws.getKaDetail().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── CONF-010 — COBOL: CONFIRM-NO 88-level covers "N" and "n" ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmScanProceed_confirmY_doesNotAbort() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("Y");

            invokePrivate("confirmScanProceed");

            assertEquals(0, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmScanProceed_confirmUppercaseN_setsAbortFlag() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("N");

            invokePrivate("confirmScanProceed");

            assertEquals(1, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmScanProceed_confirmLowercaseN_setsAbortFlag() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("n");

            invokePrivate("confirmScanProceed");

            assertEquals(1, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void confirmScanProceed_noStdinInput_treatsAsBlankAndDoesNotAbort() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn(null);

            invokePrivate("confirmScanProceed");

            assertEquals(0, ws.getWkAbortFlg());
        }
    }

    /* ── WRH-010 / PH-010 / CP-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void writeReportHeader_forcesPageBreakAndPrintsHeader() {
        invokePrivate("writeReportHeader");

        assertEquals(4, ws.getWkLine());
        assertEquals(1, ws.getWkPage());
        verify(repfSpy, times(3)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkPageBreak_lineBelow55_doesNotPrintHeader() {
        ws.setWkLine(10);
        ws.setWkPage(1);

        invokePrivate("checkPageBreak");

        assertEquals(1, ws.getWkPage());
        verify(repfSpy, never()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkPageBreak_lineAt55_printsNewPageHeader() {
        ws.setWkLine(55);
        ws.setWkPage(1);
        ws.setWkCompany("TEST CO");

        invokePrivate("checkPageBreak");

        assertEquals(2, ws.getWkPage());
        assertEquals(4, ws.getWkLine());
        verify(repfSpy, times(3)).write();
    }

    /* ── COD-010 / CODN-010 / ROD-010 — ORDDF -> ORDHF orphan check ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkOrphanOrderDetails_startInvalidKey_noRecordsScanned() {
        doReturn(true).when(orddfSpy).isInvalidKey();

        invokePrivate("checkOrphanOrderDetails");

        assertEquals(1, ws.getWkEof());
        assertEquals(0, ws.getWkOrddCnt());
        verify(orddfSpy, never()).readNext();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkOrphanOrderDetails_orphanDetailFound_reportsExceptionAndCounts() {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i == 0) {
                                ws.setOdNo(1001);
                                ws.setOdLine(1);
                            }
                            return i == 0;
                        })
                .when(orddfSpy)
                .readNext();
        doReturn(false, true).when(orddfSpy).isAtEnd();
        doReturn(true).when(ordhfSpy).isInvalidKey();

        invokePrivate("checkOrphanOrderDetails");

        assertEquals(1, ws.getWkOrddCnt());
        assertEquals(1, ws.getWkExOrphOd());
        assertEquals(1, ws.getWkExTotal());
        assertEquals("ORPHAN ORDER-DTL", ws.getExTag().trim());
        assertEquals(1001L, ws.getExKey1());
        // writeSectionHeader (3 lines) + writeExceptionLine (1 line)
        verify(repfSpy, times(4)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkOrphanOrderDetails_readNextFailsWithBadStatus_abends() {
        doReturn(false).when(orddfSpy).isAtEnd();
        doReturn("99").when(orddfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("checkOrphanOrderDetails"));

        assertEquals("ORDDF", ws.getKaFile().trim());
        assertEquals("READ NEXT ORDDF failed", ws.getKaDetail().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkOrphanOrderDetails_headerFound_noExceptionReported() {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i == 0) {
                                ws.setOdNo(2002);
                                ws.setOdLine(1);
                            }
                            return i == 0;
                        })
                .when(orddfSpy)
                .readNext();
        doReturn(false, true).when(orddfSpy).isAtEnd();
        doReturn(false).when(ordhfSpy).isInvalidKey();

        invokePrivate("checkOrphanOrderDetails");

        assertEquals(1, ws.getWkOrddCnt());
        assertEquals(0, ws.getWkExOrphOd());
        assertEquals(0, ws.getWkExTotal());
    }

    /* ── CID-010 / CIDN-010 / RID-010 — INVDF -> INVHF orphan check ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkOrphanInvoiceDetails_orphanDetailFound_reportsException() {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i == 0) {
                                ws.setIdNo(3003);
                                ws.setIdLine(2);
                            }
                            return i == 0;
                        })
                .when(invdfSpy)
                .readNext();
        doReturn(false, true).when(invdfSpy).isAtEnd();
        doReturn(true).when(invhfSpy).isInvalidKey();

        invokePrivate("checkOrphanInvoiceDetails");

        assertEquals(1, ws.getWkInvdCnt());
        assertEquals(1, ws.getWkExOrphId());
        assertEquals(1, ws.getWkExTotal());
        assertEquals("ORPHAN INV-DTL", ws.getExTag().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkOrphanInvoiceDetails_startInvalidKey_noRecordsScanned() {
        doReturn(true).when(invdfSpy).isInvalidKey();

        invokePrivate("checkOrphanInvoiceDetails");

        assertEquals(1, ws.getWkEof());
        assertEquals(0, ws.getWkInvdCnt());
        verify(invdfSpy, never()).readNext();
    }

    /* ── CSP-010 / CSPN-010 / RMP-010 — STOKF -> PRODF missing-product check ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkStockMissingProduct_productMissing_reportsException() {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i == 0) {
                                ws.setSkProd(4004);
                                ws.setSkWhse(1);
                            }
                            return i == 0;
                        })
                .when(stokfSpy)
                .readNext();
        doReturn(false, true).when(stokfSpy).isAtEnd();
        doReturn(true).when(prodfSpy).isInvalidKey();

        invokePrivate("checkStockMissingProduct");

        assertEquals(1, ws.getWkStokCnt());
        assertEquals(1, ws.getWkExProd());
        assertEquals(1, ws.getWkExTotal());
        assertEquals("MISSING PRODUCT", ws.getExTag().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkStockMissingProduct_readNextBadStatus_abends() {
        doReturn(false).when(stokfSpy).isAtEnd();
        doReturn("99").when(stokfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("checkStockMissingProduct"));

        assertEquals("STOKF", ws.getKaFile().trim());
        assertEquals("READ NEXT STOKF failed", ws.getKaDetail().trim());
    }

    /* ── CSN-010 / CSNN-010 / RNG-010 — STOKF negative on-hand check ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkStockNegativeOnHand_negativeQty_reportsExceptionWithMessage() {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i == 0) {
                                ws.setSkProd(5005);
                                ws.setSkWhse(2);
                                ws.setSkOnhand(new BigDecimal("-15"));
                            }
                            return i == 0;
                        })
                .when(stokfSpy)
                .readNext();
        doReturn(false, true).when(stokfSpy).isAtEnd();

        invokePrivate("checkStockNegativeOnHand");

        assertEquals(1, ws.getWkExNeg());
        assertEquals(1, ws.getWkExTotal());
        assertEquals("NEGATIVE ON-HAND", ws.getExTag().trim());
        assertEquals(-15L, ws.getWkEn());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkStockNegativeOnHand_nonNegativeQty_noExceptionReported() {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i == 0) {
                                ws.setSkProd(6006);
                                ws.setSkWhse(3);
                                ws.setSkOnhand(new BigDecimal("42"));
                            }
                            return i == 0;
                        })
                .when(stokfSpy)
                .readNext();
        doReturn(false, true).when(stokfSpy).isAtEnd();

        invokePrivate("checkStockNegativeOnHand");

        assertEquals(0, ws.getWkExNeg());
        assertEquals(0, ws.getWkExTotal());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readStockCheckNegative_atEnd_setsEofAndSkipsCheck() {
        doReturn(true).when(stokfSpy).isAtEnd();

        invokePrivate("readStockCheckNegative");

        assertEquals(1, ws.getWkEof());
        assertEquals(0, ws.getWkExNeg());
    }

    /* ── PSUM-010 / WC-010 / WV-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printSummary_noExceptions_verdictCleanCompletionCodeZero() {
        invokePrivate("printSummary");

        assertEquals(0, ws.getCompletionCode());
        assertEquals(
                " VERDICT : CLEAN - no integrity problems found", ws.getVerdTxt().stripTrailing());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printSummary_hasExceptions_verdictDirtyCompletionCodeFour() {
        ws.setWkExOrphOd(2);
        ws.setWkExTotal(2);

        invokePrivate("printSummary");

        assertEquals(4, ws.getCompletionCode());
        assertEquals(
                " VERDICT : DIRTY - integrity problems detected", ws.getVerdTxt().stripTrailing());
    }

    /* ── TERM-010 / DST-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void terminateProgram_abortFlagZero_writesStatsAndClosesAllFiles() {
        ws.setWkAbortFlg(0);

        invokePrivate("terminateProgram");

        verify(repfSpy).close();
        verify(orddfSpy).close();
        verify(ordhfSpy).close();
        verify(invdfSpy).close();
        verify(invhfSpy).close();
        verify(stokfSpy).close();
        verify(prodfSpy).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void terminateProgram_abortFlagSet_skipsStatsAndReportCloseButClosesInputFiles() {
        ws.setWkAbortFlg(1);

        invokePrivate("terminateProgram");

        verify(repfSpy, never()).close();
        verify(orddfSpy).close();
        verify(stokfSpy).close();
    }

    /* ── ABND-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abendProgram_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abendProgram"));

        assertEquals("BT0090", ws.getKaProgid());
        assertEquals("23", ws.getKaFsts());
        assertEquals("EBATCH", ws.getKaMsgcode());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── MAIN-000 — full integration flows ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_confirmedByOperatorAllFilesEmpty_completesCleanly() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("Y");
            doReturn(true).when(orddfSpy).isInvalidKey();
            doReturn(true).when(invdfSpy).isInvalidKey();
            doReturn(true).when(stokfSpy).isInvalidKey();

            assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

            assertEquals(0, ws.getCompletionCode());
            verify(orddfSpy).close();
            verify(repfSpy).close();
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_operatorCancels_skipsScanningButStillClosesInputFiles() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("N");

            assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

            assertEquals(1, ws.getWkAbortFlg());
            verify(orddfSpy).close();
            verify(repfSpy, never()).close();
            verify(orddfSpy, never()).readNext();
        }
    }

    /** verify() shim kept local so repfSpy write-count assertions read naturally. */
    private static com.sakura.runtime.io.RepfDataset atLeastOnceWrite() {
        return null;
    }
}

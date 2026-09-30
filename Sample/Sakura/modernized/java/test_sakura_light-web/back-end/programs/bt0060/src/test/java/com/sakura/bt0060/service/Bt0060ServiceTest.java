package com.sakura.bt0060.service;

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
import com.sakura.bt0060.domain.Bt0060FieldAccess;
import com.sakura.bt0060.runtime.Bt0060Datasets;
import com.sakura.dateut.service.DateutService;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.OrddfDataset;
import com.sakura.runtime.io.OrdhfDataset;
import com.sakura.runtime.io.ProdfDataset;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for {@link Bt0060Service}, derived from COBOL program BT0060 (purge/archive of
 * logically-deleted masters and old fully-processed orders, batch/console — no screen section).
 * Ground truth for inputs/expected values: BT0060.cob PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Bt0060ServiceTest {

    @Spy private CustfDataset custfSpy = new CustfDataset();
    @Spy private ProdfDataset prodfSpy = new ProdfDataset();
    @Spy private OrdhfDataset ordhfSpy = new OrdhfDataset();
    @Spy private OrddfDataset orddfSpy = new OrddfDataset();

    private DateutService dateutService = org.mockito.Mockito.mock(DateutService.class);
    private AbortxService abortxService = org.mockito.Mockito.mock(AbortxService.class);

    private Bt0060Service service;
    private Bt0060FieldAccess ws;

    /** Subclass swapping in the spy datasets since Bt0060Datasets builds its own real files. */
    private static class TestDatasets extends Bt0060Datasets {
        private final CustfDataset custf;
        private final ProdfDataset prodf;
        private final OrdhfDataset ordhf;
        private final OrddfDataset orddf;

        TestDatasets(
                CustfDataset custf, ProdfDataset prodf, OrdhfDataset ordhf, OrddfDataset orddf) {
            this.custf = custf;
            this.prodf = prodf;
            this.ordhf = ordhf;
            this.orddf = orddf;
        }

        @Override
        public CustfDataset getCustf() {
            return custf;
        }

        @Override
        public ProdfDataset getProdf() {
            return prodf;
        }

        @Override
        public OrdhfDataset getOrdhf() {
            return ordhf;
        }

        @Override
        public OrddfDataset getOrddf() {
            return orddf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Bt0060Datasets fileSet = new TestDatasets(custfSpy, prodfSpy, ordhfSpy, orddfSpy);
        service = new Bt0060Service(fileSet, dateutService, abortxService);
        ws = getWs();

        // Safe defaults for every file: "no records / immediate EOF" so a test that
        // reaches a purge/browse loop without its own stubbing terminates on the
        // first check instead of spinning forever.
        doNothing().when(custfSpy).open(any());
        doNothing().when(custfSpy).close();
        doReturn("00").when(custfSpy).getFileStatus();
        doReturn(true).when(custfSpy).isInvalidKey();
        doReturn(true).when(custfSpy).isAtEnd();
        doReturn(true).when(custfSpy).start(anyString(), anyString());
        doReturn(false).when(custfSpy).readNext();
        doNothing().when(custfSpy).delete();

        doNothing().when(prodfSpy).open(any());
        doNothing().when(prodfSpy).close();
        doReturn("00").when(prodfSpy).getFileStatus();
        doReturn(true).when(prodfSpy).isInvalidKey();
        doReturn(true).when(prodfSpy).isAtEnd();
        doReturn(true).when(prodfSpy).start(anyString(), anyString());
        doReturn(false).when(prodfSpy).readNext();
        doNothing().when(prodfSpy).delete();

        doNothing().when(ordhfSpy).open(any());
        doNothing().when(ordhfSpy).close();
        doReturn("00").when(ordhfSpy).getFileStatus();
        doReturn(true).when(ordhfSpy).isInvalidKey();
        doReturn(true).when(ordhfSpy).isAtEnd();
        doReturn(true).when(ordhfSpy).start(anyString(), anyString());
        doReturn(false).when(ordhfSpy).readNext();
        doNothing().when(ordhfSpy).delete();

        doNothing().when(orddfSpy).open(any());
        doNothing().when(orddfSpy).close();
        doReturn("00").when(orddfSpy).getFileStatus();
        doReturn(true).when(orddfSpy).isInvalidKey();
        doReturn(true).when(orddfSpy).isAtEnd();
        doReturn(true).when(orddfSpy).start(anyString(), anyString());
        doReturn(false).when(orddfSpy).readNext();
        doNothing().when(orddfSpy).delete();

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

    private Bt0060FieldAccess getWs() throws Exception {
        Field f = Bt0060Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Bt0060FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Bt0060Service.class.getDeclaredMethod(name);
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

    /* ── public entry-point / lifecycle overrides ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void mainProcess_delegatesToRunMainProgramChain_viaPublicExecute() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("", "Y");

            // BatchServiceBase.execute() catches ProgramExitSignal internally — normal exit, no
            // throw.
            service.execute();

            assertEquals(0, service.getCompletionCode());
            verify(custfSpy, times(1)).close();
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void getAndSetCompletionCode_roundTripThroughWorkingStorage() {
        service.getFileSet();
        try {
            Method setter = Bt0060Service.class.getDeclaredMethod("setCompletionCode", int.class);
            setter.setAccessible(true);
            setter.invoke(service, 12);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        assertEquals(12, service.getCompletionCode());
    }

    /* ── GTOD-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void loadSystemDate_success_setsSysDateAndSysymdFromDateut() {
        invokePrivate("loadSystemDate");

        assertEquals(20260918, ws.getWkSysdate());
        assertEquals(20260918, ws.getWkSysymd());
        verify(dateutService).execute(any());
    }

    /* ── INIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_blankCutoffAndConfirmYes_usesDefaultCutoffAndProceeds() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("", "Y");

            invokePrivate("initializeProgram");

            assertEquals("BT0060", ws.getWkProgid());
            assertEquals(0, ws.getWkAbortFlg());
            assertEquals(1, ws.getWkPurgeFlg());
            assertEquals(ws.getWkDfltCut(), ws.getWkCutoff());
            verify(custfSpy).open(FileOpenMode.IO);
            verify(prodfSpy).open(FileOpenMode.IO);
            verify(ordhfSpy).open(FileOpenMode.IO);
            verify(orddfSpy).open(FileOpenMode.IO);
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_invalidCutoffInput_abortsAndSkipsConfirmation() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("ABCDEFGH");

            invokePrivate("initializeProgram");

            assertEquals(1, ws.getWkAbortFlg());
            util.verify(Utility::readStdinLine, times(1));
        }
    }

    /* ── OPEN-010 / OCUS-010 / OPRD-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openAllFiles_allStatus00_opensAllFourFilesWithoutAbort() {
        invokePrivate("openAllFiles");

        verify(custfSpy, times(1)).open(FileOpenMode.IO);
        verify(prodfSpy, times(1)).open(FileOpenMode.IO);
        verify(ordhfSpy, times(1)).open(FileOpenMode.IO);
        verify(orddfSpy, times(1)).open(FileOpenMode.IO);
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openCustfWithRetry_status35_reopensAsOutputThenIO() {
        doReturn("35", "00", "00", "00").when(custfSpy).getFileStatus();

        invokePrivate("openCustfWithRetry");

        verify(custfSpy, times(2)).open(FileOpenMode.IO);
        verify(custfSpy, times(1)).open(FileOpenMode.OUTPUT);
        verify(custfSpy, times(1)).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openProdfWithRetry_openErrorAfterRetry_abortsWithProdfCode() {
        doReturn("23").when(prodfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openProdfWithRetry"));

        assertEquals("PRODF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openAllFiles_custfOpenError_abortsAndSkipsRemainingFiles() {
        doReturn("23").when(custfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openAllFiles"));

        assertEquals("CUSTF", ws.getKaFile().trim());
        verify(prodfSpy, never()).open(any());
        verify(ordhfSpy, never()).open(any());
        verify(orddfSpy, never()).open(any());
    }

    /* ── ACUT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void promptAndValidateCutoffDate_blankInput_usesDefaultAndValidates() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("");
            ws.setWkDfltCut(20250101);

            invokePrivate("promptAndValidateCutoffDate");

            assertEquals(20250101, ws.getWkCutoff());
            assertEquals(0, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void promptAndValidateCutoffDate_numericInput_parsesIntoCutoff() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("20240630");

            invokePrivate("promptAndValidateCutoffDate");

            assertEquals(20240630, ws.getWkCutoff());
            assertEquals(0, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void promptAndValidateCutoffDate_nonNumericInput_setsAbortFlagAndSkipsDateutCall() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("BADDATE!");

            invokePrivate("promptAndValidateCutoffDate");

            assertEquals(1, ws.getWkAbortFlg());
            verify(dateutService, never()).execute(any());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void promptAndValidateCutoffDate_calendarValidationFails_setsAbortFlag() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("20240631");
            doAnswer(
                            inv -> {
                                DateutLinkParm p = inv.getArgument(0);
                                p.getKdate().setKdStatus("99");
                                p.getKdate().setKdDate1(p.getKdate().getKdDate1());
                                return null;
                            })
                    .when(dateutService)
                    .execute(any());

            invokePrivate("promptAndValidateCutoffDate");

            assertEquals(1, ws.getWkAbortFlg());
        }
    }

    /* ── CONF-010 — COBOL: CONFIRM-YES 88-level covers "Y" and "y" ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void promptPurgeConfirmation_confirmUppercaseY_setsPurgeFlag() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("Y");

            invokePrivate("promptPurgeConfirmation");

            assertEquals(1, ws.getWkPurgeFlg());
            assertEquals(0, ws.getWkAbortFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void promptPurgeConfirmation_confirmLowercaseY_setsPurgeFlag() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("y");

            invokePrivate("promptPurgeConfirmation");

            assertEquals(1, ws.getWkPurgeFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void promptPurgeConfirmation_confirmN_setsAbortFlag() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("N");

            invokePrivate("promptPurgeConfirmation");

            assertEquals(1, ws.getWkAbortFlg());
            assertEquals(0, ws.getWkPurgeFlg());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void promptPurgeConfirmation_noStdinInput_setsAbortFlag() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn((String) null);

            invokePrivate("promptPurgeConfirmation");

            assertEquals(1, ws.getWkAbortFlg());
        }
    }

    /* ── PCUS-010 / PCUX-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeCustomers_startInvalidKey_noRecordsProcessed() {
        invokePrivate("purgeCustomers");

        assertEquals(1, ws.getEofFlg());
        assertEquals(0, ws.getWkCuRead());
        verify(custfSpy, never()).readNext();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeCustomers_twoRecordsOneDeleted_countsReadAndDeleted() {
        doReturn(false).when(custfSpy).isInvalidKey();
        AtomicInteger callIndex = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int idx = callIndex.getAndIncrement();
                            switch (idx) {
                                case 0 -> ws.setCuDelFlag(1);
                                case 1 -> ws.setCuDelFlag(0);
                                default -> {}
                            }
                            return idx < 2;
                        })
                .when(custfSpy)
                .readNext();
        doReturn(false, false, true).when(custfSpy).isAtEnd();

        invokePrivate("purgeCustomers");

        assertEquals(1, ws.getEofFlg());
        assertEquals(2, ws.getWkCuRead());
        assertEquals(1, ws.getWkCuDel());
        verify(custfSpy, times(1)).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeCustomerRecord_atEnd_setsEofFlagWithoutCountingRead() {
        doReturn(true).when(custfSpy).isAtEnd();

        invokePrivate("purgeCustomerRecord");

        assertEquals(1, ws.getEofFlg());
        assertEquals(0, ws.getWkCuRead());
        verify(custfSpy, never()).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeCustomerRecord_delFlagZero_countsReadOnlyNoDelete() {
        doReturn(false).when(custfSpy).isAtEnd();
        ws.setCuDelFlag(0);

        invokePrivate("purgeCustomerRecord");

        assertEquals(1, ws.getWkCuRead());
        assertEquals(0, ws.getWkCuDel());
        verify(custfSpy, never()).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeCustomerRecord_delFlagOneDeleteSuccess_incrementsCuDel() {
        doReturn(false).when(custfSpy).isAtEnd();
        ws.setCuDelFlag(1);

        invokePrivate("purgeCustomerRecord");

        assertEquals(1, ws.getWkCuRead());
        assertEquals(1, ws.getWkCuDel());
        verify(custfSpy).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeCustomerRecord_deleteFails_abortsWithCustfCode() {
        doReturn(false).when(custfSpy).isAtEnd();
        ws.setCuDelFlag(1);
        doReturn("00", "23").when(custfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("purgeCustomerRecord"));

        assertEquals("CUSTF", ws.getKaFile().trim());
        assertEquals("DELETE CUSTF failed", ws.getKaDetail().trim());
        assertEquals(0, ws.getWkCuDel());
    }

    /* ── PPRD-010 / PPRX-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeProducts_startInvalidKey_noRecordsProcessed() {
        invokePrivate("purgeProducts");

        assertEquals(1, ws.getEofFlg());
        assertEquals(0, ws.getWkPrRead());
        verify(prodfSpy, never()).readNext();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeProducts_twoRecordsOneDeleted_countsReadAndDeleted() {
        doReturn(false).when(prodfSpy).isInvalidKey();
        AtomicInteger callIndex = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int idx = callIndex.getAndIncrement();
                            switch (idx) {
                                case 0 -> ws.setPrDelFlag(1);
                                case 1 -> ws.setPrDelFlag(0);
                                default -> {}
                            }
                            return idx < 2;
                        })
                .when(prodfSpy)
                .readNext();
        doReturn(false, false, true).when(prodfSpy).isAtEnd();

        invokePrivate("purgeProducts");

        assertEquals(1, ws.getEofFlg());
        assertEquals(2, ws.getWkPrRead());
        assertEquals(1, ws.getWkPrDel());
        verify(prodfSpy, times(1)).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeProductRecord_atEnd_setsEofFlagWithoutCountingRead() {
        doReturn(true).when(prodfSpy).isAtEnd();

        invokePrivate("purgeProductRecord");

        assertEquals(1, ws.getEofFlg());
        assertEquals(0, ws.getWkPrRead());
        verify(prodfSpy, never()).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeProductRecord_delFlagZero_countsReadOnlyNoDelete() {
        doReturn(false).when(prodfSpy).isAtEnd();
        ws.setPrDelFlag(0);

        invokePrivate("purgeProductRecord");

        assertEquals(1, ws.getWkPrRead());
        assertEquals(0, ws.getWkPrDel());
        verify(prodfSpy, never()).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeProductRecord_delFlagOneDeleteSuccess_incrementsPrDel() {
        doReturn(false).when(prodfSpy).isAtEnd();
        ws.setPrDelFlag(1);

        invokePrivate("purgeProductRecord");

        assertEquals(1, ws.getWkPrRead());
        assertEquals(1, ws.getWkPrDel());
        verify(prodfSpy).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeProductRecord_deleteFails_abortsWithProdfCode() {
        doReturn(false).when(prodfSpy).isAtEnd();
        ws.setPrDelFlag(1);
        doReturn("00", "23").when(prodfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("purgeProductRecord"));

        assertEquals("PRODF", ws.getKaFile().trim());
        assertEquals("DELETE PRODF failed", ws.getKaDetail().trim());
        assertEquals(0, ws.getWkPrDel());
    }

    /* ── PORD-010 / PORX-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeOrders_startInvalidKey_noRecordsProcessed() {
        invokePrivate("purgeOrders");

        assertEquals(1, ws.getEofFlg());
        assertEquals(0, ws.getWkOhRead());
        verify(ordhfSpy, never()).readNext();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeOrders_twoRecordsOneDeleted_countsReadAndDeletedThroughLoop() {
        doReturn(false).when(ordhfSpy).isInvalidKey();
        doReturn(false).when(orddfSpy).isInvalidKey(); // no detail lines under either header
        AtomicInteger callIndex = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int idx = callIndex.getAndIncrement();
                            switch (idx) {
                                case 0 -> {
                                    ws.setOhDelFlag(1);
                                    ws.setOhDate(20250601);
                                    ws.setOhStatus(1);
                                }
                                case 1 -> {
                                    ws.setOhDelFlag(0);
                                    ws.setOhDate(20250601);
                                    ws.setOhStatus(1);
                                }
                                default -> {}
                            }
                            return idx < 2;
                        })
                .when(ordhfSpy)
                .readNext();
        doReturn(false, false, true).when(ordhfSpy).isAtEnd();

        invokePrivate("purgeOrders");

        assertEquals(1, ws.getEofFlg());
        assertEquals(2, ws.getWkOhRead());
        assertEquals(1, ws.getWkOhDel());
        verify(ordhfSpy, times(1)).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeOrderRecord_atEnd_setsEofFlagWithoutCountingRead() {
        doReturn(true).when(ordhfSpy).isAtEnd();

        invokePrivate("purgeOrderRecord");

        assertEquals(1, ws.getEofFlg());
        assertEquals(0, ws.getWkOhRead());
        verify(ordhfSpy, never()).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeOrderRecord_delFlagOne_triggersDeleteEvenIfNotPastCutoff() {
        doReturn(false).when(ordhfSpy).isAtEnd();
        doReturn(false).when(ordhfSpy).isInvalidKey();
        ws.setOhDelFlag(1);
        ws.setOhDate(20990101);
        ws.setOhStatus(1);
        ws.setWkCutoff(20250101);

        invokePrivate("purgeOrderRecord");

        assertEquals(1, ws.getWkOhRead());
        verify(ordhfSpy).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeOrderRecord_pastCutoffAndStatus4Invoiced_triggersDelete() {
        doReturn(false).when(ordhfSpy).isAtEnd();
        doReturn(false).when(ordhfSpy).isInvalidKey();
        ws.setOhDelFlag(0);
        ws.setWkCutoff(20250101);
        ws.setOhDate(20240101);
        ws.setOhStatus(4);

        invokePrivate("purgeOrderRecord");

        verify(ordhfSpy).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeOrderRecord_pastCutoffAndStatus9Cancelled_triggersDelete() {
        doReturn(false).when(ordhfSpy).isAtEnd();
        doReturn(false).when(ordhfSpy).isInvalidKey();
        ws.setOhDelFlag(0);
        ws.setWkCutoff(20250101);
        ws.setOhDate(20240101);
        ws.setOhStatus(9);

        invokePrivate("purgeOrderRecord");

        verify(ordhfSpy).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeOrderRecord_pastCutoffButStatusOpen_noDelete() {
        doReturn(false).when(ordhfSpy).isAtEnd();
        ws.setOhDelFlag(0);
        ws.setWkCutoff(20250101);
        ws.setOhDate(20240101);
        ws.setOhStatus(1);

        invokePrivate("purgeOrderRecord");

        verify(ordhfSpy, never()).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void purgeOrderRecord_notPastCutoffAndNotDeleted_noDelete() {
        doReturn(false).when(ordhfSpy).isAtEnd();
        ws.setOhDelFlag(0);
        ws.setWkCutoff(20250101);
        ws.setOhDate(20250601);
        ws.setOhStatus(4);

        invokePrivate("purgeOrderRecord");

        verify(ordhfSpy, never()).delete();
    }

    /* ── DORD-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteOrderAndDetails_headerDeleteSuccess_incrementsOhDel() {
        doReturn(false).when(ordhfSpy).isInvalidKey();

        invokePrivate("deleteOrderAndDetails");

        assertEquals(1, ws.getWkOhDel());
        verify(ordhfSpy).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteOrderAndDetails_headerDeleteInvalidKey_abortsAndSkipsCount() {
        doReturn(true).when(ordhfSpy).isInvalidKey();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("deleteOrderAndDetails"));

        assertEquals("ORDHF", ws.getKaFile().trim());
        assertEquals("DELETE ORDHF failed", ws.getKaDetail().trim());
        assertEquals(0, ws.getWkOhDel());
    }

    /* ── DODT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteOrderDetailLines_startInvalidKey_returnsImmediately() {
        // default orddfSpy.isInvalidKey() = true
        invokePrivate("deleteOrderDetailLines");

        verify(orddfSpy, never()).readNext();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteOrderDetailLines_startFstsNotOk_returnsImmediately() {
        doReturn(false).when(orddfSpy).isInvalidKey();
        doReturn("23").when(orddfSpy).getFileStatus();

        invokePrivate("deleteOrderDetailLines");

        verify(orddfSpy, never()).readNext();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteOrderDetailLines_twoMatchingLines_deletesBothAndStopsOnMismatch() {
        doReturn(false).when(orddfSpy).isInvalidKey();
        ws.setOhNo(5);
        AtomicInteger callIndex = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int idx = callIndex.getAndIncrement();
                            switch (idx) {
                                case 0 -> ws.setOdNo(5);
                                case 1 -> ws.setOdNo(5);
                                default -> ws.setOdNo(6); // different order -> loop stops
                            }
                            return true;
                        })
                .when(orddfSpy)
                .readNext();
        doReturn(false, false, false).when(orddfSpy).isAtEnd();

        invokePrivate("deleteOrderDetailLines");

        assertEquals(2, ws.getWkOdDel());
        verify(orddfSpy, times(2)).delete();
    }

    /* ── DODX-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteOrderDetailRecord_atEnd_setsFsts10AndReturns() {
        doReturn(true).when(orddfSpy).isAtEnd();

        invokePrivate("deleteOrderDetailRecord");

        assertEquals("10", ws.getFsts().trim());
        verify(orddfSpy, never()).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteOrderDetailRecord_fstsNotOkAfterRead_returnsWithoutDelete() {
        doReturn(false).when(orddfSpy).isAtEnd();
        doReturn("23").when(orddfSpy).getFileStatus();

        invokePrivate("deleteOrderDetailRecord");

        verify(orddfSpy, never()).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteOrderDetailRecord_odNoMismatch_noDelete() {
        doReturn(false).when(orddfSpy).isAtEnd();
        ws.setOhNo(5);
        ws.setOdNo(6);

        invokePrivate("deleteOrderDetailRecord");

        verify(orddfSpy, never()).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteOrderDetailRecord_matchSuccess_incrementsOdDel() {
        doReturn(false).when(orddfSpy).isAtEnd();
        ws.setOhNo(5);
        ws.setOdNo(5);

        invokePrivate("deleteOrderDetailRecord");

        assertEquals(1, ws.getWkOdDel());
        verify(orddfSpy).delete();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void deleteOrderDetailRecord_matchDeleteFails_abortsWithOrddfCode() {
        doReturn(false).when(orddfSpy).isAtEnd();
        ws.setOhNo(5);
        ws.setOdNo(5);
        doReturn("00", "23").when(orddfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("deleteOrderDetailRecord"));

        assertEquals("ORDDF", ws.getKaFile().trim());
        assertEquals("DELETE ORDDF failed", ws.getKaDetail().trim());
        assertEquals(0, ws.getWkOdDel());
    }

    /* ── PSUM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void logPurgeSummary_copiesCutoffAndFinalCountIntoDisplayFields() {
        ws.setWkCutoff(20250101);
        ws.setWkCuRead(10);
        ws.setWkCuDel(2);
        ws.setWkPrRead(20);
        ws.setWkPrDel(3);
        ws.setWkOhRead(30);
        ws.setWkOhDel(4);
        ws.setWkOdDel(9);

        invokePrivate("logPurgeSummary");

        assertEquals(20250101, ws.getWkEDate());
        // WK-E-CNT is reused for each printed line — its final state is the last count moved into
        // it
        assertEquals(9, ws.getWkECnt());
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void closeAllFiles_closesAllFourFiles() {
        invokePrivate("closeAllFiles");

        verify(custfSpy).close();
        verify(prodfSpy).close();
        verify(ordhfSpy).close();
        verify(orddfSpy).close();
    }

    /* ── ABND-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abortProgram_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortProgram"));

        assertEquals("BT0060", ws.getKaProgid());
        assertEquals("23", ws.getKaFsts());
        assertEquals("EBATCH", ws.getKaMsgcode());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(custfSpy).close();
        verify(prodfSpy).close();
        verify(ordhfSpy).close();
        verify(orddfSpy).close();
    }

    /* ── MAIN-000 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_confirmedByOperator_purgesNothingOnEmptyFilesAndCompletesNormally() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("", "Y");
            // default isInvalidKey()=true on all four files -> every purge loop hits immediate EOF

            assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

            assertEquals(0, ws.getCompletionCode());
            assertEquals(0, ws.getWkCuRead());
            verify(custfSpy).close();
            verify(prodfSpy).close();
            verify(ordhfSpy).close();
            verify(orddfSpy).close();
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_operatorCancels_skipsPurgeButStillCloses() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("", "N");

            assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

            assertEquals(1, ws.getWkAbortFlg());
            assertEquals(0, ws.getWkCuRead());
            assertEquals(0, ws.getCompletionCode());
            verify(custfSpy, never()).readNext();
            verify(custfSpy).close();
            verify(orddfSpy).close();
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_invalidCutoffDate_abortsBeforeConfirmationPromptButStillCloses() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("ABCDEFGH");

            assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

            assertEquals(1, ws.getWkAbortFlg());
            assertEquals(0, ws.getCompletionCode());
            util.verify(Utility::readStdinLine, times(1));
            verify(custfSpy).close();
        }
    }
}

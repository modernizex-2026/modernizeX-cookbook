package com.sakura.rp0040.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0040.domain.Rp0040FieldAccess;
import com.sakura.rp0040.runtime.Rp0040Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.InvhfDataset;
import com.sakura.runtime.io.RepfDataset;
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
 * Unit tests for {@link Rp0040Service}, derived from COBOL program RP0040 (sales summary by
 * customer — reads INVHF in customer order via alternate key, control break on customer, prints
 * customer name/invoice count/amounts plus grand totals). Ground truth for inputs/expected values:
 * RP0040.cob PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Rp0040ServiceTest {

    @Spy private InvhfDataset invhfSpy = new InvhfDataset();
    @Spy private CustfDataset custfSpy = new CustfDataset();
    @Spy private SyscfDataset syscfSpy = new SyscfDataset();
    @Spy private RepfDataset repfSpy = new RepfDataset();

    private final DateutService dateutService = mock(DateutService.class);
    private final AbortxService abortxService = mock(AbortxService.class);

    private Rp0040Service service;
    private Rp0040FieldAccess ws;

    /** Subclass swapping in the spy datasets since Rp0040Datasets builds its own real files. */
    private static class TestDatasets extends Rp0040Datasets {
        private final InvhfDataset invhf;
        private final CustfDataset custf;
        private final SyscfDataset syscf;
        private final RepfDataset repf;

        TestDatasets(InvhfDataset invhf, CustfDataset custf, SyscfDataset syscf, RepfDataset repf) {
            this.invhf = invhf;
            this.custf = custf;
            this.syscf = syscf;
            this.repf = repf;
        }

        @Override
        public InvhfDataset getInvhf() {
            return invhf;
        }

        @Override
        public CustfDataset getCustf() {
            return custf;
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
        Rp0040Datasets fileSet = new TestDatasets(invhfSpy, custfSpy, syscfSpy, repfSpy);
        service = new Rp0040Service(fileSet, dateutService, abortxService);
        ws = getWs();

        for (var spy : new Object[] {invhfSpy, custfSpy, syscfSpy, repfSpy}) {
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

    private Rp0040FieldAccess getWs() throws Exception {
        Field f = Rp0040Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Rp0040FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Rp0040Service.class.getDeclaredMethod(name);
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

    /* ── INIT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_syscfKeyFound_usesCompanyNameFromRecord() {
        doAnswer(
                        inv -> {
                            ws.setSyCompanyName("ACME TRADING CO");
                            return true;
                        })
                .when(syscfSpy)
                .readByKey(any());

        invokePrivate("initializeProgram");

        assertEquals("ACME TRADING CO", ws.getWkCompany().trim());
        assertEquals("RP0040", ws.getWkProgid().trim());
        assertEquals(20260918, ws.getWkSysdate());
        verify(syscfSpy).close();
        verify(invhfSpy).open(FileOpenMode.INPUT);
        verify(repfSpy).open(FileOpenMode.OUTPUT);
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_syscfKeyNotFound_keepsDefaultCompany() {
        doReturn(true).when(syscfSpy).isInvalidKey();

        invokePrivate("initializeProgram");

        assertEquals("SAKURA Sales Management System", ws.getWkCompany().trim());
        verify(syscfSpy).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_syscfOpenFails_skipsLookupAndClose() {
        doReturn("35").when(syscfSpy).getFileStatus();

        invokePrivate("initializeProgram");

        verify(syscfSpy, never()).readByKey(any());
        verify(syscfSpy, never()).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_invhfOpenErrorStatus_abendsWithInvhfLabel() {
        doReturn("23").when(invhfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeProgram"));

        assertEquals("INVHF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(custfSpy, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_invhfStatus35_notAbendButSetsMainEof() {
        doReturn("35").when(invhfSpy).getFileStatus();

        invokePrivate("initializeProgram");

        assertEquals(1, ws.getWkMainEof());
        verify(abortxService, never()).execute(any());
        verify(custfSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_invhfStatus30_notAbendButSetsMainEof() {
        doReturn("30").when(invhfSpy).getFileStatus();

        invokePrivate("initializeProgram");

        assertEquals(1, ws.getWkMainEof());
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_invhfStatus00_mainEofStaysZero() {
        invokePrivate("initializeProgram");

        assertEquals(0, ws.getWkMainEof());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgram_repfOpenFails_abendsWithRepfLabel() {
        doReturn("35").when(repfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeProgram"));

        assertEquals("REPF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── PARM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readDateRangeParameters_bothBlank_usesDefaultFullRange() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("", "");

            invokePrivate("readDateRangeParameters");

            assertEquals(0, ws.getWkDateFrom());
            assertEquals(99999999, ws.getWkDateTo());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readDateRangeParameters_numericRange_parsesBothDates() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("20260101", "20260131");

            invokePrivate("readDateRangeParameters");

            assertEquals(20260101, ws.getWkDateFrom());
            assertEquals(20260131, ws.getWkDateTo());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readDateRangeParameters_nonNumericInput_fallsBackToDefaults() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("ABCDEFGH", "XYZXYZXY");

            invokePrivate("readDateRangeParameters");

            assertEquals(0, ws.getWkDateFrom());
            assertEquals(99999999, ws.getWkDateTo());
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readDateRangeParameters_nullStdin_treatedAsBlankDefaults() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn(null, null);

            invokePrivate("readDateRangeParameters");

            assertEquals(0, ws.getWkDateFrom());
            assertEquals(99999999, ws.getWkDateTo());
        }
    }

    /* ── PRINT-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printSalesSummaryReport_mainEofAlreadySet_skipsStartAndPrintsNoSalesMessage() {
        ws.setWkMainEof(1);

        invokePrivate("printSalesSummaryReport");

        verify(invhfSpy, never()).start(anyString(), anyString());
        verify(invhfSpy, never()).readNext();
        // PRINT-010 always resets WK-LINE to 99, so PG-010's CHECK-PAGE forces a
        // page header (5 writes) before the "no sales" message (1 write) = 6.
        verify(repfSpy, times(6)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printSalesSummaryReport_startInvalidKey_setsMainEofAndPrintsNoSales() {
        doReturn(true).when(invhfSpy).isInvalidKey();

        invokePrivate("printSalesSummaryReport");

        assertEquals(1, ws.getWkMainEof());
        verify(invhfSpy, never()).readNext();
        // Same WK-LINE=99 page-break-before-message behavior as above: 5 + 1 = 6.
        verify(repfSpy, times(6)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printSalesSummaryReport_twoCustomersInRange_writesSummariesAndGrandTotal() {
        // readNextInvoiceRecord() calls isAtEnd() TWICE per iteration (once to flag
        // MAIN-EOF, once to gate PROCESS-ONE) — tie both calls to the same counter as
        // readNext() so a plain doReturn() sequence doesn't get consumed out of step.
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i == 0) {
                                ws.setIhCust(100);
                                ws.setIhDate(20260110);
                                ws.setIhDelFlag(0);
                                ws.setIhStatus(0);
                                ws.setIhAmount(new BigDecimal("100"));
                                ws.setIhTaxAmount(new BigDecimal("8"));
                                ws.setIhTotal(new BigDecimal("108"));
                            } else if (i == 1) {
                                ws.setIhCust(100);
                                ws.setIhDate(20260115);
                                ws.setIhDelFlag(0);
                                ws.setIhStatus(0);
                                ws.setIhAmount(new BigDecimal("50"));
                                ws.setIhTaxAmount(new BigDecimal("4"));
                                ws.setIhTotal(new BigDecimal("54"));
                            } else if (i == 2) {
                                ws.setIhCust(200);
                                ws.setIhDate(20260120);
                                ws.setIhDelFlag(0);
                                ws.setIhStatus(0);
                                ws.setIhAmount(new BigDecimal("200"));
                                ws.setIhTaxAmount(new BigDecimal("16"));
                                ws.setIhTotal(new BigDecimal("216"));
                            }
                            return null;
                        })
                .when(invhfSpy)
                .readNext();
        doAnswer(inv -> idx.get() > 3).when(invhfSpy).isAtEnd();
        doAnswer(
                        inv -> {
                            ws.setCuName("Customer");
                            return true;
                        })
                .when(custfSpy)
                .readByKey(any());

        invokePrivate("printSalesSummaryReport");

        assertEquals(3, ws.getWkGCnt());
        assertEquals(2, ws.getWkCustCnt());
        assertEquals(1, ws.getWkPage());
        // IH-AMOUNT/IH-TAX-AMOUNT/IH-TOTAL are PIC S9(11) COMP-3 (scale 0, no decimals)
        assertEquals(new BigDecimal("350"), ws.getWkGNet());
        assertEquals(new BigDecimal("28"), ws.getWkGTax());
        assertEquals(new BigDecimal("378"), ws.getWkGTot());
        // page header(5) + cust100 summary(1) + cust200 summary(1) + rule(1) + grand total(1) = 9
        // writes
        verify(repfSpy, times(9)).write();
    }

    /* ── RN-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readNextInvoiceRecord_atEnd_setsMainEofWithoutAccumulating() {
        doReturn(true).when(invhfSpy).isAtEnd();

        invokePrivate("readNextInvoiceRecord");

        assertEquals(1, ws.getWkMainEof());
        assertEquals(1, ws.getWkFirst());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readNextInvoiceRecord_notAtEnd_accumulatesRecord() {
        doReturn(false).when(invhfSpy).isAtEnd();
        ws.setIhCust(300);
        ws.setIhDelFlag(0);
        ws.setIhStatus(0);
        ws.setIhDate(20260101);

        invokePrivate("readNextInvoiceRecord");

        assertEquals(0, ws.getWkMainEof());
        assertEquals(0, ws.getWkFirst());
        assertEquals(300, ws.getWkCurCust());
    }

    /* ── PO-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void accumulateCustomerSales_firstRecord_setsCurCustAndAccumulatesWhenValid() {
        ws.setWkFirst(1);
        ws.setIhCust(400);
        ws.setIhDelFlag(0);
        ws.setIhStatus(0);
        ws.setIhDate(20260110);
        ws.setIhAmount(new BigDecimal("10"));
        ws.setIhTaxAmount(new BigDecimal("1"));
        ws.setIhTotal(new BigDecimal("11"));

        invokePrivate("accumulateCustomerSales");

        assertEquals(0, ws.getWkFirst());
        assertEquals(400, ws.getWkCurCust());
        assertEquals(1, ws.getWkCurCnt());
        // IH-AMOUNT is PIC S9(11) COMP-3 (scale 0, no decimals)
        assertEquals(new BigDecimal("10"), ws.getWkCurNet());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void accumulateCustomerSales_customerChange_flushesPreviousAndResets() {
        ws.setWkFirst(0);
        ws.setWkCurCust(400);
        ws.setWkCurCnt(2);
        ws.setWkCurNet(new BigDecimal("100.00"));
        ws.setWkCurTax(new BigDecimal("8.00"));
        ws.setWkCurTot(new BigDecimal("108.00"));
        ws.setIhCust(500);
        ws.setIhDelFlag(0);
        ws.setIhStatus(0);
        ws.setIhDate(20260110);
        doAnswer(
                        inv -> {
                            ws.setCuName("Customer 400");
                            return true;
                        })
                .when(custfSpy)
                .readByKey(any());

        invokePrivate("accumulateCustomerSales");

        assertEquals(500, ws.getWkCurCust());
        assertEquals(1, ws.getWkCustCnt());
        verify(repfSpy, atLeastOnce()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void accumulateCustomerSales_delFlagSet_skipsAccumulation() {
        ws.setWkFirst(1);
        ws.setIhCust(600);
        ws.setIhDelFlag(1);
        ws.setIhStatus(0);
        ws.setIhDate(20260110);

        invokePrivate("accumulateCustomerSales");

        assertEquals(0, ws.getWkCurCnt());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void accumulateCustomerSales_statusNine_skipsAccumulation() {
        ws.setWkFirst(1);
        ws.setIhCust(600);
        ws.setIhDelFlag(0);
        ws.setIhStatus(9);
        ws.setIhDate(20260110);

        invokePrivate("accumulateCustomerSales");

        assertEquals(0, ws.getWkCurCnt());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void accumulateCustomerSales_dateBeforeRange_skipsAccumulation() {
        ws.setWkFirst(1);
        ws.setWkDateFrom(20260201);
        ws.setWkDateTo(99999999);
        ws.setIhCust(600);
        ws.setIhDelFlag(0);
        ws.setIhStatus(0);
        ws.setIhDate(20260110);

        invokePrivate("accumulateCustomerSales");

        assertEquals(0, ws.getWkCurCnt());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void accumulateCustomerSales_dateAfterRange_skipsAccumulation() {
        ws.setWkFirst(1);
        ws.setWkDateFrom(0);
        ws.setWkDateTo(20260101);
        ws.setIhCust(600);
        ws.setIhDelFlag(0);
        ws.setIhStatus(0);
        ws.setIhDate(20260601);

        invokePrivate("accumulateCustomerSales");

        assertEquals(0, ws.getWkCurCnt());
    }

    /* ── RA-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void resetCustomerAccumulators_zeroesAllAccumulators() {
        ws.setWkCurNet(new BigDecimal("10.00"));
        ws.setWkCurTax(new BigDecimal("1.00"));
        ws.setWkCurTot(new BigDecimal("11.00"));
        ws.setWkCurCnt(5);

        invokePrivate("resetCustomerAccumulators");

        assertEquals(BigDecimal.ZERO, ws.getWkCurNet());
        assertEquals(BigDecimal.ZERO, ws.getWkCurTax());
        assertEquals(BigDecimal.ZERO, ws.getWkCurTot());
        assertEquals(0, ws.getWkCurCnt());
    }

    /* ── FC-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void writeCustomerSummaryLine_curCntZero_returnsWithoutWriting() {
        ws.setWkCurCnt(0);

        invokePrivate("writeCustomerSummaryLine");

        verify(repfSpy, never()).write();
        verify(custfSpy, never()).readByKey(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void writeCustomerSummaryLine_customerFound_usesNameFromRecord() {
        ws.setWkCurCust(700);
        ws.setWkCurCnt(3);
        ws.setWkCurNet(new BigDecimal("30.00"));
        ws.setWkCurTax(new BigDecimal("3.00"));
        ws.setWkCurTot(new BigDecimal("33.00"));
        doAnswer(
                        inv -> {
                            ws.setCuName("Widget Corp");
                            return true;
                        })
                .when(custfSpy)
                .readByKey(any());

        invokePrivate("writeCustomerSummaryLine");

        assertEquals("Widget Corp", ws.getRcName().trim());
        assertEquals(700, ws.getRcCode());
        assertEquals(3, ws.getRcCount());
        assertEquals(30L, ws.getRcNet());
        verify(repfSpy).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void writeCustomerSummaryLine_customerNotFound_usesUnknownPlaceholder() {
        ws.setWkCurCust(800);
        ws.setWkCurCnt(1);
        doReturn(true).when(custfSpy).isInvalidKey();

        invokePrivate("writeCustomerSummaryLine");

        assertEquals("(unknown)", ws.getRcName().trim());
        verify(repfSpy).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void writeCustomerSummaryLine_accumulatesGrandTotalsAndCustomerCount() {
        ws.setWkCurCust(900);
        ws.setWkCurCnt(2);
        ws.setWkCurNet(new BigDecimal("20"));
        ws.setWkCurTax(new BigDecimal("2"));
        ws.setWkCurTot(new BigDecimal("22"));
        ws.setWkGNet(new BigDecimal("5"));
        ws.setWkGTax(new BigDecimal("1"));
        ws.setWkGTot(new BigDecimal("6"));
        ws.setWkGCnt(1);
        ws.setWkCustCnt(1);

        invokePrivate("writeCustomerSummaryLine");

        // WK-G-NET/TAX/TOT are PIC S9(15) COMP-3 (scale 0, no decimals)
        assertEquals(new BigDecimal("25"), ws.getWkGNet());
        assertEquals(new BigDecimal("3"), ws.getWkGTax());
        assertEquals(new BigDecimal("28"), ws.getWkGTot());
        assertEquals(3, ws.getWkGCnt());
        assertEquals(2, ws.getWkCustCnt());
    }

    /* ── CP-010 ── */

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
        assertEquals(6, ws.getWkLine());
        verify(repfSpy, times(5)).write();
    }

    /* ── PH-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printPageHeader_incrementsPageAndWritesFiveLinesThenResetsLine() {
        ws.setWkPage(2);
        ws.setWkCompany("SAKURA CORP");
        ws.setWkTitle("SALES SUMMARY BY CUSTOMER");
        ws.setWkSysdate(20260918);
        ws.setWkDateFrom(20260101);
        ws.setWkDateTo(20261231);

        invokePrivate("printPageHeader");

        assertEquals(3, ws.getWkPage());
        assertEquals(6, ws.getWkLine());
        assertEquals("SAKURA CORP", ws.getH1Company().trim());
        assertEquals(3, ws.getH1Page());
        assertEquals("PERIOD 20260101 - 20261231", ws.getH2Info().trim());
        verify(repfSpy, times(5)).write();
    }

    /* ── PG-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printGrandTotal_gCntZero_writesNoSalesMessageOnly() {
        ws.setWkGCnt(0);

        invokePrivate("printGrandTotal");

        verify(repfSpy, times(1)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void printGrandTotal_gCntNonZero_writesRuleAndGrandTotal() {
        ws.setWkGCnt(5);
        ws.setWkGNet(new BigDecimal("500.00"));
        ws.setWkGTax(new BigDecimal("40.00"));
        ws.setWkGTot(new BigDecimal("540.00"));

        invokePrivate("printGrandTotal");

        assertEquals(500L, ws.getRtNet());
        assertEquals(40L, ws.getRtTax());
        assertEquals(540L, ws.getRtTot());
        assertEquals(5, ws.getRtCount());
        verify(repfSpy, times(2)).write();
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void finalizeAndCloseFiles_closesAllFilesAndSetsCompletionCodeZero() {
        invokePrivate("finalizeAndCloseFiles");

        verify(invhfSpy).close();
        verify(custfSpy).close();
        verify(repfSpy).close();
        assertEquals(0, ws.getCompletionCode());
    }

    /* ── AB-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abortProgram_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortProgram"));

        assertEquals("RP0040", ws.getKaProgid().trim());
        assertEquals("23", ws.getKaFsts().trim());
        assertEquals("EOPEN", ws.getKaMsgcode().trim());
        assertEquals("Report file error", ws.getKaDetail().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── MAIN-000 — full integration flows ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_noInvoicesInRange_completesCleanlyWithNoSalesMessage() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("", "");
            doReturn(true).when(invhfSpy).isInvalidKey();

            assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

            assertEquals(0, ws.getCompletionCode());
            verify(invhfSpy).close();
            verify(repfSpy).close();
            // PRINT-010 resets WK-LINE to 99, forcing a page header (5 writes) before
            // the "no sales" message (1 write) = 6.
            verify(repfSpy, times(6)).write();
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_oneCustomerOneInvoice_writesSummaryAndGrandTotalAndCompletesCleanly() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("", "");

            // readNextInvoiceRecord() calls isAtEnd() TWICE per iteration (MAIN-EOF flag,
            // then the PROCESS-ONE gate) — tie both calls to the same counter as readNext()
            // instead of a plain doReturn() sequence, which would be consumed out of step.
            AtomicInteger readIdx = new AtomicInteger(0);
            doAnswer(
                            inv -> {
                                readIdx.getAndIncrement();
                                ws.setIhCust(1000);
                                ws.setIhDate(20260215);
                                ws.setIhDelFlag(0);
                                ws.setIhStatus(0);
                                ws.setIhAmount(new BigDecimal("100"));
                                ws.setIhTaxAmount(new BigDecimal("8"));
                                ws.setIhTotal(new BigDecimal("108"));
                                return null;
                            })
                    .when(invhfSpy)
                    .readNext();
            doAnswer(inv -> readIdx.get() > 1).when(invhfSpy).isAtEnd();

            doAnswer(
                            inv -> {
                                ws.setCuName("Customer D");
                                return true;
                            })
                    .when(custfSpy)
                    .readByKey(any());

            assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

            assertEquals(0, ws.getCompletionCode());
            assertEquals(1, ws.getWkGCnt());
            assertEquals(1, ws.getWkCustCnt());
            // page header(5) + summary(1) + rule(1) + grand total(1) = 8 writes
            verify(repfSpy, times(8)).write();
            verify(invhfSpy).close();
            verify(repfSpy).close();
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_invhfOpenError_abendsBeforeParmOrPrint() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            doReturn("23").when(invhfSpy).getFileStatus();

            assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

            assertEquals("INVHF", ws.getKaFile().trim());
            assertEquals(255, ws.getCompletionCode());
            util.verify(Utility::readStdinLine, never());
        }
    }
}

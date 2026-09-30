package com.sakura.rp0030.service;

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
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0030.domain.Rp0030FieldAccess;
import com.sakura.rp0030.runtime.Rp0030Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.InvdfDataset;
import com.sakura.runtime.io.InvhfDataset;
import com.sakura.runtime.io.ProdfDataset;
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
 * Unit tests for {@link Rp0030Service}, derived from COBOL program RP0030 (sales journal / invoice
 * register — lists INVHF headers with INVDF detail lines for a sales-date range, printing
 * per-invoice subtotal and grand totals). Ground truth for inputs/expected values: RP0030.cob
 * PROCEDURE DIVISION.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Rp0030ServiceTest {

    @Spy private InvhfDataset invhfSpy = new InvhfDataset();
    @Spy private InvdfDataset invdfSpy = new InvdfDataset();
    @Spy private CustfDataset custfSpy = new CustfDataset();
    @Spy private ProdfDataset prodfSpy = new ProdfDataset();
    @Spy private SyscfDataset syscfSpy = new SyscfDataset();
    @Spy private RepfDataset repfSpy = new RepfDataset();

    private final DateutService dateutService = mock(DateutService.class);
    private final AbortxService abortxService = mock(AbortxService.class);

    private Rp0030Service service;
    private Rp0030FieldAccess ws;

    /** Subclass swapping in the spy datasets since Rp0030Datasets builds its own real files. */
    private static class TestDatasets extends Rp0030Datasets {
        private final InvhfDataset invhf;
        private final InvdfDataset invdf;
        private final CustfDataset custf;
        private final ProdfDataset prodf;
        private final SyscfDataset syscf;
        private final RepfDataset repf;

        TestDatasets(
                InvhfDataset invhf,
                InvdfDataset invdf,
                CustfDataset custf,
                ProdfDataset prodf,
                SyscfDataset syscf,
                RepfDataset repf) {
            this.invhf = invhf;
            this.invdf = invdf;
            this.custf = custf;
            this.prodf = prodf;
            this.syscf = syscf;
            this.repf = repf;
        }

        @Override
        public InvhfDataset getInvhf() {
            return invhf;
        }

        @Override
        public InvdfDataset getInvdf() {
            return invdf;
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
        Rp0030Datasets fileSet =
                new TestDatasets(invhfSpy, invdfSpy, custfSpy, prodfSpy, syscfSpy, repfSpy);
        service = new Rp0030Service(fileSet, dateutService, abortxService);
        ws = getWs();

        for (var spy : new Object[] {invhfSpy, invdfSpy, custfSpy, prodfSpy, syscfSpy, repfSpy}) {
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

    private Rp0030FieldAccess getWs() throws Exception {
        Field f = Rp0030Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Rp0030FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Rp0030Service.class.getDeclaredMethod(name);
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
    void initializeProgramAndFiles_syscfKeyFound_usesCompanyNameFromRecord() {
        doAnswer(
                        inv -> {
                            ws.setSyCompanyName("ACME TRADING CO");
                            return true;
                        })
                .when(syscfSpy)
                .readByKey(any());

        invokePrivate("initializeProgramAndFiles");

        assertEquals("ACME TRADING CO", ws.getWkCompany().trim());
        assertEquals("RP0030", ws.getWkProgid().trim());
        assertEquals(20260918, ws.getWkSysdate());
        verify(syscfSpy).close();
        verify(invhfSpy).open(FileOpenMode.INPUT);
        verify(repfSpy).open(FileOpenMode.OUTPUT);
        verify(abortxService, never()).execute(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgramAndFiles_syscfKeyNotFound_keepsDefaultCompany() {
        doReturn(true).when(syscfSpy).isInvalidKey();

        invokePrivate("initializeProgramAndFiles");

        assertEquals("SAKURA Sales Management System", ws.getWkCompany().trim());
        verify(syscfSpy).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgramAndFiles_syscfOpenFails_skipsLookupAndClose() {
        doReturn("35").when(syscfSpy).getFileStatus();

        invokePrivate("initializeProgramAndFiles");

        verify(syscfSpy, never()).readByKey(any());
        verify(syscfSpy, never()).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgramAndFiles_invhfOpenErrorStatus_abendsWithInvhfLabel() {
        doReturn("23").when(invhfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeProgramAndFiles"));

        assertEquals("INVHF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
        verify(invdfSpy, never()).open(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgramAndFiles_invhfStatus35_notAbendButSetsMainEof() {
        doReturn("35").when(invhfSpy).getFileStatus();

        invokePrivate("initializeProgramAndFiles");

        assertEquals(1, ws.getWkMainEof());
        verify(abortxService, never()).execute(any());
        verify(invdfSpy).open(FileOpenMode.INPUT);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgramAndFiles_invhfStatus00_mainEofStaysZero() {
        invokePrivate("initializeProgramAndFiles");

        assertEquals(0, ws.getWkMainEof());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void initializeProgramAndFiles_repfOpenFails_abendsWithRepfLabel() {
        doReturn("35").when(repfSpy).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("initializeProgramAndFiles"));

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
    void generateSalesJournal_mainEofAlreadySet_skipsStartAndPrintsNoInvoicesMessage() {
        ws.setWkMainEof(1);

        invokePrivate("generateSalesJournal");

        verify(invhfSpy, never()).start(anyString(), anyString());
        verify(invhfSpy, never()).readNext();
        // PRINT-010 always sets WK-LINE=99 first, so the summary's page-break check
        // (WK-LINE >= 50) always fires: page header (3 writes) + "no invoices" message (1).
        verify(repfSpy, times(4)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void generateSalesJournal_startInvalidKey_setsMainEofAndPrintsNoInvoices() {
        doReturn(true).when(invhfSpy).isInvalidKey();

        invokePrivate("generateSalesJournal");

        assertEquals(1, ws.getWkMainEof());
        verify(invhfSpy, never()).readNext();
        // Same forced page-break header as above.
        verify(repfSpy, times(4)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void generateSalesJournal_oneHeaderNoDetails_writesInvoiceAndSubtotalThenGrandTotal() {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i == 0) {
                                ws.setIhNo(1001L);
                                ws.setIhDate(20260110);
                                ws.setIhDelFlag(0);
                                ws.setIhStatus(0);
                                ws.setIhKind(1);
                                ws.setIhCust(500);
                                ws.setIhAmount(new BigDecimal("100.00"));
                                ws.setIhTaxAmount(new BigDecimal("8.00"));
                                ws.setIhTotal(new BigDecimal("108.00"));
                            }
                            return i == 0;
                        })
                .when(invhfSpy)
                .readNext();
        // RH-010 checks isAtEnd() twice per iteration (once to set EOF, once to gate the
        // date/process branch) — each readNext() call consumes 2 stubbed values.
        doReturn(false, false, true, true).when(invhfSpy).isAtEnd();
        // No invoice detail lines for this invoice — first readNext on INVDF hits EOF.
        doReturn(true).when(invdfSpy).isAtEnd();
        doAnswer(
                        inv -> {
                            ws.setCuName("Customer A");
                            return true;
                        })
                .when(custfSpy)
                .readByKey(any());

        invokePrivate("generateSalesJournal");

        assertEquals(1, ws.getWkInvCnt());
        assertEquals(0, ws.getWkLinCnt());
        assertEquals("SALE", ws.getWkKindTxt().trim());
        assertEquals("Customer A", ws.getIhCname().trim());
        // PRINT-010 forces WK-LINE=99 so CHECK-PAGE-HDR fires before the header line:
        // page header (3) + invoice header (1) + subtotal (1) + blank (1) + rule (1) + grand total
        // (1) = 8
        verify(repfSpy, times(8)).write();
    }

    /* ── RH-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readNextInvoiceHeader_atEnd_setsMainEof() {
        doReturn(true).when(invhfSpy).isAtEnd();

        invokePrivate("readNextInvoiceHeader");

        assertEquals(1, ws.getWkMainEof());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readNextInvoiceHeader_dateAfterRangeEnd_setsMainEofWithoutProcessing() {
        ws.setWkDateTo(20260101);
        ws.setIhDate(20260601);
        doReturn(false).when(invhfSpy).isAtEnd();

        invokePrivate("readNextInvoiceHeader");

        assertEquals(1, ws.getWkMainEof());
        verify(repfSpy, never()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readNextInvoiceHeader_dateWithinRange_processesInvoice() {
        ws.setWkDateTo(99999999);
        ws.setIhDate(20260110);
        ws.setIhDelFlag(0);
        ws.setIhStatus(0);
        ws.setIhKind(1);
        doReturn(false).when(invhfSpy).isAtEnd();
        doReturn(true).when(invdfSpy).isAtEnd();

        invokePrivate("readNextInvoiceHeader");

        assertEquals(0, ws.getWkMainEof());
        assertEquals(1, ws.getWkInvCnt());
    }

    /* ── PI-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processInvoiceHeader_delFlagSet_skipsWithoutWriting() {
        ws.setIhDelFlag(1);
        ws.setIhStatus(0);

        invokePrivate("processInvoiceHeader");

        verify(repfSpy, never()).write();
        assertEquals(0, ws.getWkInvCnt());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processInvoiceHeader_statusNine_skipsWithoutWriting() {
        ws.setIhDelFlag(0);
        ws.setIhStatus(9);

        invokePrivate("processInvoiceHeader");

        verify(repfSpy, never()).write();
        assertEquals(0, ws.getWkInvCnt());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processInvoiceHeader_kindReturn_setsKindTextReturn() {
        ws.setIhDelFlag(0);
        ws.setIhStatus(0);
        ws.setIhKind(2);
        doReturn(true).when(invdfSpy).isAtEnd();

        invokePrivate("processInvoiceHeader");

        assertEquals("RETURN", ws.getWkKindTxt().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processInvoiceHeader_kindOther_setsKindTextSale() {
        ws.setIhDelFlag(0);
        ws.setIhStatus(0);
        ws.setIhKind(1);
        doReturn(true).when(invdfSpy).isAtEnd();

        invokePrivate("processInvoiceHeader");

        assertEquals("SALE", ws.getWkKindTxt().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processInvoiceHeader_accumulatesGrandTotals() {
        ws.setIhDelFlag(0);
        ws.setIhStatus(0);
        ws.setIhKind(1);
        ws.setIhAmount(new BigDecimal("200.00"));
        ws.setIhTaxAmount(new BigDecimal("16.00"));
        ws.setIhTotal(new BigDecimal("216.00"));
        ws.setWkGNet(new BigDecimal("50.00"));
        ws.setWkGTax(new BigDecimal("4.00"));
        ws.setWkGTot(new BigDecimal("54.00"));
        doReturn(true).when(invdfSpy).isAtEnd();

        invokePrivate("processInvoiceHeader");

        // WK-G-NET/TAX/TOT are COMP-3 with zero decimal places, so the stored value is
        // truncated to scale 0 on write-back — compare by value, not by BigDecimal.equals scale.
        assertEquals(0, ws.getWkGNet().compareTo(new BigDecimal("250")));
        assertEquals(0, ws.getWkGTax().compareTo(new BigDecimal("20")));
        assertEquals(0, ws.getWkGTot().compareTo(new BigDecimal("270")));
    }

    /* ── PD-010 / RD-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processInvoiceDetails_startInvalidKey_noDetailLinesWritten() {
        ws.setIhNo(2002L);
        doReturn(true).when(invdfSpy).isInvalidKey();

        invokePrivate("processInvoiceDetails");

        assertEquals(1, ws.getWkDtlEof());
        verify(invdfSpy, never()).readNext();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void processInvoiceDetails_twoMatchingLines_writesBothAndCountsLines() {
        ws.setIhNo(3003L);
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i == 0 || i == 1) {
                                ws.setIdNo(3003L);
                                ws.setIdLine(i + 1);
                                ws.setIdProd(9000 + i);
                                ws.setIdQty(new BigDecimal("5"));
                                ws.setIdUnitPrice(new BigDecimal("10.00"));
                                ws.setIdAmount(new BigDecimal("50.00"));
                            }
                            return i < 2;
                        })
                .when(invdfSpy)
                .readNext();
        // RD-010 checks isAtEnd() twice per iteration — each readNext() call consumes 2 values;
        // the last stubbed value (true) repeats for the final EOF iteration.
        doReturn(false, false, false, false, true).when(invdfSpy).isAtEnd();
        doReturn(true).when(prodfSpy).isInvalidKey();

        invokePrivate("processInvoiceDetails");

        assertEquals(2, ws.getWkLinCnt());
        verify(repfSpy, times(2)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readNextInvoiceDetail_idNoMismatch_stopsDetailLoopWithoutWriting() {
        ws.setIhNo(4004L);
        ws.setIdNo(9999L);
        doReturn(false).when(invdfSpy).isAtEnd();

        invokePrivate("readNextInvoiceDetail");

        assertEquals(1, ws.getWkDtlEof());
        verify(repfSpy, never()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void readNextInvoiceDetail_atEnd_setsDtlEof() {
        doReturn(true).when(invdfSpy).isAtEnd();

        invokePrivate("readNextInvoiceDetail");

        assertEquals(1, ws.getWkDtlEof());
    }

    /* ── POD-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void writeInvoiceDetailLine_productFound_usesProductNameFromRecord() {
        ws.setIdLine(1);
        ws.setIdProd(7000);
        ws.setIdQty(new BigDecimal("3"));
        ws.setIdUnitPrice(new BigDecimal("12.50"));
        ws.setIdAmount(new BigDecimal("37.50"));
        doAnswer(
                        inv -> {
                            ws.setPrName("Widget");
                            return true;
                        })
                .when(prodfSpy)
                .readByKey(any());

        invokePrivate("writeInvoiceDetailLine");

        assertEquals("Widget", ws.getIdPname().trim());
        assertEquals(1, ws.getWkLinCnt());
        verify(repfSpy).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void writeInvoiceDetailLine_productNotFound_usesUnknownPlaceholder() {
        doReturn(true).when(prodfSpy).isInvalidKey();

        invokePrivate("writeInvoiceDetailLine");

        assertEquals("(unknown)", ws.getIdPname().trim());
    }

    /* ── PS-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void writeInvoiceSubtotal_writesSubtotalLineAndBlankLine() {
        ws.setIhAmount(new BigDecimal("100.00"));
        ws.setIhTaxAmount(new BigDecimal("8.00"));
        ws.setIhTotal(new BigDecimal("108.00"));
        ws.setWkLine(10);

        invokePrivate("writeInvoiceSubtotal");

        assertEquals(100L, ws.getSubNet());
        assertEquals(8L, ws.getSubTax());
        assertEquals(108L, ws.getSubTot());
        assertEquals(12, ws.getWkLine());
        verify(repfSpy, times(2)).write();
    }

    /* ── LKC-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupCustomerName_keyFound_usesCustomerNameFromRecord() {
        doAnswer(
                        inv -> {
                            ws.setCuName("Customer B");
                            return true;
                        })
                .when(custfSpy)
                .readByKey(any());

        invokePrivate("lookupCustomerName");

        assertEquals("Customer B", ws.getWkCustName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupCustomerName_keyNotFound_usesUnknownPlaceholder() {
        doReturn(true).when(custfSpy).isInvalidKey();

        invokePrivate("lookupCustomerName");

        assertEquals("(unknown)", ws.getWkCustName().trim());
    }

    /* ── LKP-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupProductName_keyFound_usesProductNameFromRecord() {
        doAnswer(
                        inv -> {
                            ws.setPrName("Gadget");
                            return true;
                        })
                .when(prodfSpy)
                .readByKey(any());

        invokePrivate("lookupProductName");

        assertEquals("Gadget", ws.getWkProdName().trim());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lookupProductName_keyNotFound_usesUnknownPlaceholder() {
        doReturn(true).when(prodfSpy).isInvalidKey();

        invokePrivate("lookupProductName");

        assertEquals("(unknown)", ws.getWkProdName().trim());
    }

    /* ── CPH-010 / CP-010 / PH-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkPageBreakForHeader_lineBelow50_doesNotPrintHeader() {
        ws.setWkLine(10);
        ws.setWkPage(1);

        invokePrivate("checkPageBreakForHeader");

        assertEquals(1, ws.getWkPage());
        verify(repfSpy, never()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkPageBreakForHeader_lineAt50_printsNewPageHeader() {
        ws.setWkLine(50);
        ws.setWkPage(1);
        ws.setWkCompany("TEST CO");

        invokePrivate("checkPageBreakForHeader");

        assertEquals(2, ws.getWkPage());
        assertEquals(5, ws.getWkLine());
        verify(repfSpy, times(3)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkPageBreakForDetail_lineBelow55_doesNotPrintHeader() {
        ws.setWkLine(20);
        ws.setWkPage(1);

        invokePrivate("checkPageBreakForDetail");

        assertEquals(1, ws.getWkPage());
        verify(repfSpy, never()).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void checkPageBreakForDetail_lineAt55_printsNewPageHeader() {
        ws.setWkLine(55);
        ws.setWkPage(1);

        invokePrivate("checkPageBreakForDetail");

        assertEquals(2, ws.getWkPage());
        assertEquals(5, ws.getWkLine());
        verify(repfSpy, times(3)).write();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void writeReportHeader_incrementsPageAndWritesThreeLinesThenResetsLine() {
        ws.setWkPage(2);
        ws.setWkCompany("SAKURA CORP");
        ws.setWkTitle("SALES JOURNAL");
        ws.setWkSysdate(20260918);
        ws.setWkDateFrom(20260101);
        ws.setWkDateTo(20261231);

        invokePrivate("writeReportHeader");

        assertEquals(3, ws.getWkPage());
        assertEquals(5, ws.getWkLine());
        verify(repfSpy, times(3)).write();
    }

    /* ── PG-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void writeReportSummary_noInvoices_writesOnlyNoInvoicesMessage() {
        ws.setWkInvCnt(0);

        invokePrivate("writeReportSummary");

        verify(repfSpy, times(1)).write();
        assertEquals(0, ws.getCompletionCode());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void writeReportSummary_hasInvoices_writesRuleAndGrandTotal() {
        ws.setWkInvCnt(3);
        ws.setWkGNet(new BigDecimal("300.00"));
        ws.setWkGTax(new BigDecimal("24.00"));
        ws.setWkGTot(new BigDecimal("324.00"));

        invokePrivate("writeReportSummary");

        assertEquals(300L, ws.getGtNet());
        assertEquals(24L, ws.getGtTax());
        assertEquals(324L, ws.getGtTot());
        verify(repfSpy, times(2)).write();
    }

    /* ── TERM-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void closeFilesAndLogSummary_closesAllFilesAndSetsCompletionCodeZero() {
        invokePrivate("closeFilesAndLogSummary");

        verify(invhfSpy).close();
        verify(invdfSpy).close();
        verify(custfSpy).close();
        verify(prodfSpy).close();
        verify(repfSpy).close();
        assertEquals(0, ws.getCompletionCode());
    }

    /* ── AB-010 ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void abortProgram_setsAbendFieldsAndCompletionCodeAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortProgram"));

        assertEquals("RP0030", ws.getKaProgid().trim());
        assertEquals("23", ws.getKaFsts().trim());
        assertEquals("EOPEN", ws.getKaMsgcode().trim());
        assertEquals("Report file error", ws.getKaDetail().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── MAIN-000 — full integration flows ── */

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_noInvoicesInRange_completesCleanlyWithNoInvoicesMessage() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("", "");
            doReturn(true).when(invhfSpy).isInvalidKey();

            assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

            assertEquals(0, ws.getCompletionCode());
            verify(invhfSpy).close();
            verify(repfSpy).close();
            // Forced page-break header (3) + "no invoices" message (1) = 4.
            verify(repfSpy, times(4)).write();
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runMainProgram_oneInvoiceOneDetail_writesFullJournalAndCompletesCleanly() {
        try (MockedStatic<Utility> util =
                mockStatic(Utility.class, org.mockito.Answers.CALLS_REAL_METHODS)) {
            util.when(Utility::readStdinLine).thenReturn("", "");

            AtomicInteger hdrIdx = new AtomicInteger(0);
            doAnswer(
                            inv -> {
                                int i = hdrIdx.getAndIncrement();
                                if (i == 0) {
                                    ws.setIhNo(5005L);
                                    ws.setIhDate(20260215);
                                    ws.setIhDelFlag(0);
                                    ws.setIhStatus(0);
                                    ws.setIhKind(1);
                                    ws.setIhCust(700);
                                    ws.setIhAmount(new BigDecimal("100.00"));
                                    ws.setIhTaxAmount(new BigDecimal("8.00"));
                                    ws.setIhTotal(new BigDecimal("108.00"));
                                }
                                return i == 0;
                            })
                    .when(invhfSpy)
                    .readNext();
            doReturn(false, false, true, true).when(invhfSpy).isAtEnd();

            AtomicInteger dtlIdx = new AtomicInteger(0);
            doAnswer(
                            inv -> {
                                int i = dtlIdx.getAndIncrement();
                                if (i == 0) {
                                    ws.setIdNo(5005L);
                                    ws.setIdLine(1);
                                    ws.setIdProd(8100);
                                    ws.setIdQty(new BigDecimal("2"));
                                    ws.setIdUnitPrice(new BigDecimal("50.00"));
                                    ws.setIdAmount(new BigDecimal("100.00"));
                                }
                                return i == 0;
                            })
                    .when(invdfSpy)
                    .readNext();
            doReturn(false, false, true, true).when(invdfSpy).isAtEnd();

            doAnswer(
                            inv -> {
                                ws.setCuName("Customer C");
                                return true;
                            })
                    .when(custfSpy)
                    .readByKey(any());
            doAnswer(
                            inv -> {
                                ws.setPrName("Product C");
                                return true;
                            })
                    .when(prodfSpy)
                    .readByKey(any());

            assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

            assertEquals(0, ws.getCompletionCode());
            assertEquals(1, ws.getWkInvCnt());
            assertEquals(1, ws.getWkLinCnt());
            // Forced page header (3) + invoice header (1) + detail line (1) + subtotal (1)
            // + blank (1) + rule (1) + grand total (1) = 9 writes
            verify(repfSpy, times(9)).write();
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

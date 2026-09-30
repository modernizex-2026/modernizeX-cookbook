package com.sakura.rp0110.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0110.runtime.Rp0110Datasets;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.RepfDataset;
import com.sakura.runtime.io.StokfDataset;
import com.sakura.runtime.io.SuppfDataset;
import com.sakura.runtime.io.SyscfDataset;
import com.sakura.runtime.linkage.AbortxLinkParm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Rp0110Service, generated from COBOL program RP0110 (Reorder suggestion report).
 * Test cases and expected values are derived from the COBOL PROCEDURE DIVISION logic (RP0110.cob),
 * not from the Java implementation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Rp0110ServiceTest {

    @Mock private Rp0110Datasets fileSet;

    @Spy private ProdfDataset prodf = new ProdfDataset();

    @Spy private StokfDataset stokf = new StokfDataset();

    @Spy private SuppfDataset suppf = new SuppfDataset();

    @Spy private SyscfDataset syscf = new SyscfDataset();

    @Spy private RepfDataset repf = new RepfDataset();

    @Mock private DateutService dateutService;

    @Mock private AbortxService abortxService;

    private final List<String> writtenLines = new ArrayList<>();

    private Rp0110Service service;

    @BeforeEach
    void setUp() {
        when(fileSet.getProdf()).thenReturn(prodf);
        when(fileSet.getStokf()).thenReturn(stokf);
        when(fileSet.getSuppf()).thenReturn(suppf);
        when(fileSet.getSyscf()).thenReturn(syscf);
        when(fileSet.getRepf()).thenReturn(repf);

        doNothing().when(prodf).open(any());
        doNothing().when(prodf).close();
        doReturn("00").when(prodf).getFileStatus();

        doNothing().when(stokf).open(any());
        doNothing().when(stokf).close();
        doReturn("00").when(stokf).getFileStatus();
        // Default: STOKF START finds nothing -> stock sums stay 0 unless a test stubs records.
        doReturn(false).when(stokf).start(any(), any());
        doReturn(true).when(stokf).isInvalidKey();
        doReturn(false).when(stokf).readNext();

        doNothing().when(suppf).open(any());
        doNothing().when(suppf).close();
        doReturn("00").when(suppf).getFileStatus();
        doReturn(false).when(suppf).readByKey(any());
        doReturn(false).when(suppf).isInvalidKey();

        doNothing().when(syscf).open(any());
        doNothing().when(syscf).close();
        doReturn("00").when(syscf).getFileStatus();
        doReturn(false).when(syscf).readByKey(any());
        // Default: SYSCF read is invalid key -> COBOL CONTINUE branch -> default company kept.
        doReturn(true).when(syscf).isInvalidKey();

        doNothing().when(repf).open(any());
        doNothing().when(repf).close();
        doReturn("00").when(repf).getFileStatus();
        doAnswer(
                        inv -> {
                            writtenLines.add(repf.buffer().getString("REP-REC"));
                            return null;
                        })
                .when(repf)
                .write();

        // Default: PRODF START succeeds but the product master is empty (no readNext records).
        stubProductRecords(List.of());

        service = new Rp0110Service(fileSet, dateutService, abortxService);
    }

    @AfterEach
    void tearDown() {
        writtenLines.clear();
    }

    // === helpers =============================================================

    /**
     * PRINT-010 / RDP-010: sequence of PRODF NEXT reads, ending in AT END. Each Runnable must
     * (re)populate ALL fields of prodf.buffer() it needs — PRINT-010 does {@code MOVE 0 TO PR-CODE}
     * right before START, clobbering any value set earlier, exactly like a real READ NEXT would
     * replace the buffer with the next physical record.
     */
    private void stubProductRecords(List<Runnable> recordSetters) {
        doReturn(false).when(prodf).start(any(), any());
        doReturn(false).when(prodf).isInvalidKey();
        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int n = callCount.incrementAndGet();
                            if (n <= recordSetters.size()) {
                                recordSetters.get(n - 1).run();
                            }
                            return null;
                        })
                .when(prodf)
                .readNext();
        doAnswer(inv -> callCount.get() > recordSetters.size()).when(prodf).isAtEnd();
    }

    /**
     * SUM-STOCK / SSR-010: sequence of STOKF NEXT reads (all same PR-CODE group), ending in AT END.
     */
    private void stubStockRecords(List<Runnable> recordSetters) {
        doReturn(false).when(stokf).start(any(), any());
        doReturn(false).when(stokf).isInvalidKey();
        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int n = callCount.incrementAndGet();
                            if (n <= recordSetters.size()) {
                                recordSetters.get(n - 1).run();
                            }
                            return null;
                        })
                .when(stokf)
                .readNext();
        doAnswer(inv -> callCount.get() > recordSetters.size()).when(stokf).isAtEnd();
    }

    private void setBasicQualifyingProduct(
            int prCode,
            String name,
            int suppCode,
            BigDecimal reorderPoint,
            BigDecimal reorderQty,
            BigDecimal stdCost) {
        prodf.buffer().setInt("PR-CODE", prCode);
        prodf.buffer().setInt("PR-DEL-FLAG", 0);
        prodf.buffer().setInt("PR-STOCK-MNG", 1);
        prodf.buffer().setInt("PR-DFLT-SUPP", suppCode);
        prodf.buffer().setString("PR-NAME", name);
        prodf.buffer().setDecimal("PR-REORDER-POINT", reorderPoint);
        prodf.buffer().setDecimal("PR-REORDER-QTY", reorderQty);
        prodf.buffer().setDecimal("PR-STD-COST", stdCost);
        prodf.buffer().setInt("PR-LEAD-DAYS", 7);
    }

    // === INIT-010 : SYSCF company name lookup ================================

    @Test
    void initializeProgram_syscfOpenFails_keepsDefaultCompanyName() {
        doReturn("35").when(syscf).getFileStatus();

        service.execute();

        assertThat(writtenLines.get(0)).contains("SAKURA Sales Management System");
        verify(syscf, never()).readByKey(any());
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void initializeProgram_syscfReadInvalidKey_keepsDefaultCompanyName() {
        // FSTS = "00" after open, but READ SYSCF INVALID KEY -> COBOL CONTINUE (no update)
        doReturn(true).when(syscf).isInvalidKey();

        service.execute();

        assertThat(writtenLines.get(0)).contains("SAKURA Sales Management System");
    }

    @Test
    void initializeProgram_syscfReadValid_updatesCompanyNameFromRecord() {
        doReturn(false).when(syscf).isInvalidKey();
        syscf.buffer().setString("SY-COMPANY-NAME", "ACME TRADING CO");

        service.execute();

        assertThat(writtenLines.get(0)).contains("ACME TRADING CO");
        verify(syscf, times(1)).close();
    }

    // === INIT-010 : file open errors / ABEND-RTN ==============================

    @Test
    void initializeProgram_prodfOpenUnexpectedStatus_abortsWithKaFilePRODF() {
        doReturn("20").when(prodf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(stokf, never()).open(any());
        verify(repf, never()).open(any());

        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService, times(1)).execute(captor.capture());
        AbortxLinkParm.Kabend kabend = captor.getValue().getKabend();
        assertThat(kabend.getKaFile().trim()).isEqualTo("PRODF");
        assertThat(kabend.getKaMsgcode()).isEqualTo("EOPEN ");
        assertThat(kabend.getKaDetail().trim()).isEqualTo("Report file error");
    }

    @Test
    void initializeProgram_prodfOpenStatus35_treatedAsEmptyMasterNoAbort() {
        // FSTS = "35" is an accepted PRODF open status (file-not-found equivalent):
        // no abort, but WK-MAIN-EOF is forced to 1 -> product scan is skipped entirely.
        doReturn("35").when(prodf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(prodf, never()).start(any(), any());
        assertThat(writtenLines).anyMatch(l -> l.contains("*** NO REORDER SUGGESTIONS ***"));
    }

    @Test
    void initializeProgram_repfOpenFails_abortsWithKaFileREPF() {
        doReturn("10").when(repf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService, times(1)).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("REPF");
    }

    // === PARM-010 : supplier filter parsing ===================================

    @Test
    void parseSupplierFilterParm_numericInput_filtersReportBySupplier() {
        try (MockedStatic<Utility> utility =
                mockStatic(Utility.class, org.mockito.Mockito.CALLS_REAL_METHODS)) {
            utility.when(Utility::readStdinLine).thenReturn("00000123");

            service.execute();

            assertThat(writtenLines.get(1)).contains("SUPPLIER: 000123");
        }
    }

    @Test
    void parseSupplierFilterParm_blankInput_reportsAllSuppliers() {
        try (MockedStatic<Utility> utility =
                mockStatic(Utility.class, org.mockito.Mockito.CALLS_REAL_METHODS)) {
            utility.when(Utility::readStdinLine).thenReturn("");

            service.execute();

            assertThat(writtenLines.get(1)).contains("SUPPLIER: ALL");
        }
    }

    @Test
    void parseSupplierFilterParm_nonNumericInput_reportsAllSuppliers() {
        // WK-IN-SUPP NUMERIC test fails (letters present) -> WK-SUPP-FILTER forced to 0.
        try (MockedStatic<Utility> utility =
                mockStatic(Utility.class, org.mockito.Mockito.CALLS_REAL_METHODS)) {
            utility.when(Utility::readStdinLine).thenReturn("ABCDEFGH");

            service.execute();

            assertThat(writtenLines.get(1)).contains("SUPPLIER: ALL");
        }
    }

    @Test
    void evaluateProductForReorder_supplierFilterMismatch_skipsProduct() {
        try (MockedStatic<Utility> utility =
                mockStatic(Utility.class, org.mockito.Mockito.CALLS_REAL_METHODS)) {
            utility.when(Utility::readStdinLine).thenReturn("00000123");

            stubProductRecords(
                    List.of(
                            () ->
                                    setBasicQualifyingProduct(
                                            555,
                                            "WIDGET",
                                            999,
                                            new BigDecimal("100"),
                                            new BigDecimal("10"),
                                            new BigDecimal("5"))));

            service.execute();

            assertThat(writtenLines).noneMatch(l -> l.contains("WIDGET"));
            assertThat(writtenLines).anyMatch(l -> l.contains("*** NO REORDER SUGGESTIONS ***"));
        }
    }

    // === PP-010 : product-level filters ========================================

    @Test
    void evaluateProductForReorder_productDeletedFlag_skipsProduct() {
        stubProductRecords(
                List.of(
                        () -> {
                            setBasicQualifyingProduct(
                                    1001,
                                    "DELETED ITEM",
                                    0,
                                    new BigDecimal("100"),
                                    new BigDecimal("10"),
                                    new BigDecimal("5"));
                            prodf.buffer().setInt("PR-DEL-FLAG", 1);
                        }));

        service.execute();

        assertThat(writtenLines).noneMatch(l -> l.contains("DELETED ITEM"));
        assertThat(writtenLines).anyMatch(l -> l.contains("*** NO REORDER SUGGESTIONS ***"));
    }

    @Test
    void evaluateProductForReorder_notStockManaged_skipsProduct() {
        stubProductRecords(
                List.of(
                        () -> {
                            setBasicQualifyingProduct(
                                    1002,
                                    "NON STOCK ITEM",
                                    0,
                                    new BigDecimal("100"),
                                    new BigDecimal("10"),
                                    new BigDecimal("5"));
                            prodf.buffer().setInt("PR-STOCK-MNG", 0);
                        }));

        service.execute();

        assertThat(writtenLines).noneMatch(l -> l.contains("NON STOCK ITEM"));
    }

    @Test
    void evaluateProductForReorder_projectedAboveReorderPoint_noSuggestionPrinted() {
        // onhand(80) + onorder(0) = 80 > reorder point(70) -> skipped
        stubProductRecords(
                List.of(
                        () ->
                                setBasicQualifyingProduct(
                                        1003,
                                        "PLENTY IN STOCK",
                                        0,
                                        new BigDecimal("70"),
                                        new BigDecimal("10"),
                                        new BigDecimal("5"))));
        stubStockRecords(
                List.of(
                        () -> {
                            stokf.buffer().setInt("SK-PROD", 1003);
                            stokf.buffer().setInt("SK-WHSE", 1);
                            stokf.buffer().setDecimal("SK-ONHAND", new BigDecimal("80"));
                            stokf.buffer().setDecimal("SK-ON-ORDER", new BigDecimal("0"));
                        }));

        service.execute();

        verify(stokf, times(1)).start(any(), any());
        assertThat(writtenLines).noneMatch(l -> l.contains("PLENTY IN STOCK"));
        assertThat(writtenLines).anyMatch(l -> l.contains("*** NO REORDER SUGGESTIONS ***"));
    }

    @Test
    void evaluateProductForReorder_projectedAtReorderPoint_printsSuggestionLine() {
        // onhand(50) + onorder(0) = 50, reorder point = 50 -> WK-PROJECTED > PR-REORDER-POINT
        // is FALSE (equal), so COBOL prints a suggestion (boundary case).
        stubProductRecords(
                List.of(
                        () ->
                                setBasicQualifyingProduct(
                                        1004,
                                        "REORDER NOW",
                                        0,
                                        new BigDecimal("50"),
                                        new BigDecimal("20"),
                                        new BigDecimal("3"))));
        stubStockRecords(
                List.of(
                        () -> {
                            stokf.buffer().setInt("SK-PROD", 1004);
                            stokf.buffer().setInt("SK-WHSE", 1);
                            stokf.buffer().setDecimal("SK-ONHAND", new BigDecimal("50"));
                            stokf.buffer().setDecimal("SK-ON-ORDER", new BigDecimal("0"));
                        }));

        service.execute();

        assertThat(writtenLines).anyMatch(l -> l.contains("00001004") && l.contains("REORDER NOW"));
        assertThat(writtenLines).anyMatch(l -> l.startsWith(" GRAND TOTAL ITEMS :"));
        assertThat(writtenLines).noneMatch(l -> l.contains("*** NO REORDER SUGGESTIONS ***"));
    }

    @Test
    void summarizeStockLevels_multipleWarehouses_sumsOnhandAndOnorderAcrossGroup() {
        stubProductRecords(
                List.of(
                        () ->
                                setBasicQualifyingProduct(
                                        1005,
                                        "MULTI WAREHOUSE",
                                        0,
                                        new BigDecimal("1000"),
                                        new BigDecimal("5"),
                                        new BigDecimal("2"))));
        stubStockRecords(
                List.of(
                        () -> {
                            stokf.buffer().setInt("SK-PROD", 1005);
                            stokf.buffer().setInt("SK-WHSE", 1);
                            stokf.buffer().setDecimal("SK-ONHAND", new BigDecimal("10"));
                            stokf.buffer().setDecimal("SK-ON-ORDER", new BigDecimal("2"));
                        },
                        () -> {
                            stokf.buffer().setInt("SK-PROD", 1005);
                            stokf.buffer().setInt("SK-WHSE", 2);
                            stokf.buffer().setDecimal("SK-ONHAND", new BigDecimal("15"));
                            stokf.buffer().setDecimal("SK-ON-ORDER", new BigDecimal("3"));
                        }));

        service.execute();

        // sum = 10+15+2+3 = 30 <= reorder point 1000 -> suggestion printed
        // 2 real records + 1 AT-END probe read, matching COBOL READ...AT END semantics.
        verify(stokf, times(3)).readNext();
        assertThat(writtenLines).anyMatch(l -> l.contains("MULTI WAREHOUSE"));
    }

    @Test
    void summarizeStockLevels_stockStartInvalidKey_treatsSumsAsZeroAndSuggests() {
        // Default setUp() already stubs STOKF START as invalid key (no stock records).
        stubProductRecords(
                List.of(
                        () ->
                                setBasicQualifyingProduct(
                                        1006,
                                        "NO STOCK RECORD",
                                        0,
                                        new BigDecimal("10"),
                                        new BigDecimal("1"),
                                        new BigDecimal("1"))));

        service.execute();

        verify(stokf, never()).readNext();
        assertThat(writtenLines).anyMatch(l -> l.contains("NO STOCK RECORD"));
    }

    // === LKS-010 : supplier name lookup ========================================

    @Test
    void lookupSupplierName_noDefaultSupplier_usesNoDefaultSupplierMessage() {
        stubProductRecords(
                List.of(
                        () ->
                                setBasicQualifyingProduct(
                                        1007,
                                        "NO SUPPLIER ITEM",
                                        0,
                                        new BigDecimal("1000"),
                                        new BigDecimal("1"),
                                        new BigDecimal("1"))));

        service.execute();

        // RL-SUPP is PIC X(20): "(no default supplier)" (22 chars) is truncated to 20,
        // dropping the closing paren.
        assertThat(writtenLines).anyMatch(l -> l.contains("(no default supplier"));
        verify(suppf, never()).readByKey(any());
    }

    @Test
    void lookupSupplierName_supplierNotFound_usesUnknownSupplierMessage() {
        stubProductRecords(
                List.of(
                        () ->
                                setBasicQualifyingProduct(
                                        1008,
                                        "UNKNOWN SUPP ITEM",
                                        42,
                                        new BigDecimal("1000"),
                                        new BigDecimal("1"),
                                        new BigDecimal("1"))));
        doReturn(true).when(suppf).isInvalidKey();

        service.execute();

        assertThat(writtenLines).anyMatch(l -> l.contains("(unknown supplier)"));
    }

    @Test
    void lookupSupplierName_supplierFound_usesSupplierNameFromRecord() {
        stubProductRecords(
                List.of(
                        () ->
                                setBasicQualifyingProduct(
                                        1009,
                                        "KNOWN SUPP ITEM",
                                        42,
                                        new BigDecimal("1000"),
                                        new BigDecimal("1"),
                                        new BigDecimal("1"))));
        doReturn(false).when(suppf).isInvalidKey();
        suppf.buffer().setString("SP-NAME", "GLOBAL PARTS INC");

        service.execute();

        assertThat(writtenLines).anyMatch(l -> l.contains("GLOBAL PARTS INC"));
    }

    // === PG-010 : report totals ================================================

    @Test
    void printReportTotals_noSuggestions_printsNoReorderMessageOnly() {
        service.execute();

        assertThat(writtenLines).hasSize(6); // 5 header lines + 1 message line
        assertThat(writtenLines.get(5)).contains("*** NO REORDER SUGGESTIONS ***");
    }

    @Test
    void printReportTotals_withSuggestions_printsGrandTotalLine() {
        stubProductRecords(
                List.of(
                        () ->
                                setBasicQualifyingProduct(
                                        1010,
                                        "TOTALED ITEM",
                                        0,
                                        new BigDecimal("1000"),
                                        new BigDecimal("1"),
                                        new BigDecimal("1"))));

        service.execute();

        // 5 header lines + 1 suggestion line + rule + grand-total line = 8
        assertThat(writtenLines).hasSize(8);
        assertThat(writtenLines.get(7)).contains("GRAND TOTAL ITEMS");
    }

    // === TERM-010 =============================================================

    @Test
    void terminateProgram_closesAllFilesAndSetsCompletionCodeZero() {
        service.execute();

        verify(prodf, times(1)).close();
        verify(stokf, times(1)).close();
        verify(suppf, times(1)).close();
        verify(repf, times(1)).close();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }
}

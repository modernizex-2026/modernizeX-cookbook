package com.sakura.rp0060.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0060.runtime.Rp0060Datasets;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.RepfDataset;
import com.sakura.runtime.io.StokfDataset;
import com.sakura.runtime.io.SyscfDataset;
import com.sakura.runtime.io.WhsefDataset;
import com.sakura.runtime.linkage.DateutLinkParm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Rp0060Service (COBOL RP0060 — Inventory valuation list), generated from {@code
 * RP0060.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: STOKF/PRODF/WHSEF/SYSCF/REPF are real dataset objects wrapped with {@code
 * spy()} so the record buffer (and therefore {@code Rp0060FieldAccess}, which registers those
 * buffers — including REP-REC, the FD 01-level of REPF) works exactly as in production; only I/O
 * methods (open/close/write/readByKey/readNext/start/ getFileStatus/isInvalidKey/isAtEnd) are
 * stubbed so no real file access happens. DATEUT/ABORTX are mocked.
 *
 * <p>Because REP-REC is registered from REPF's own buffer, every {@code WRITE REP-REC} (Java:
 * {@code repf.write()}) can be observed by reading {@code repf.getRecord() .getString("REP-REC")}
 * at the instant write() is invoked — this is captured into {@link #repLines} before the next write
 * overwrites the shared buffer.
 *
 * <p>Review of RP0060.cob against Rp0060Service found no CONVERT-GAP: file-open abend gating
 * (STOKF: 00/35/30 allowed, else abend; REPF: only 00 allowed), soft-EOF via STOKF status 35/30,
 * START-driven EOF, warehouse/product "(unknown)" fallback on INVALID KEY, truncating (non-rounded)
 * value computation, warehouse- break subtotal logic, and the SYSCF company-name override are all
 * reproduced faithfully.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Rp0060ServiceTest {

    private DateutService dateutService;
    private AbortxService abortxService;

    private Rp0060Datasets fileSet;
    private StokfDataset stokf;
    private ProdfDataset prodf;
    private WhsefDataset whsef;
    private SyscfDataset syscf;
    private RepfDataset repf;

    private Rp0060Service service;

    private final List<String> repLines = new ArrayList<>();
    private final Map<Integer, String> productNames = new HashMap<>();
    private final Map<Integer, String> warehouseNames = new HashMap<>();

    private record StockRow(int prod, int whse, long onhand, BigDecimal avgCost) {}

    private List<StockRow> stockRows = new ArrayList<>();
    private final AtomicInteger stokfIndex = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        dateutService = mock(DateutService.class);
        abortxService = mock(AbortxService.class);

        Rp0060Datasets real = new Rp0060Datasets();
        fileSet = spy(real);
        stokf = spy(real.getStokf());
        prodf = spy(real.getProdf());
        whsef = spy(real.getWhsef());
        syscf = spy(real.getSyscf());
        repf = spy(real.getRepf());
        doReturn(stokf).when(fileSet).getStokf();
        doReturn(prodf).when(fileSet).getProdf();
        doReturn(whsef).when(fileSet).getWhsef();
        doReturn(syscf).when(fileSet).getSyscf();
        doReturn(repf).when(fileSet).getRepf();

        doNothing().when(stokf).open(any());
        doNothing().when(prodf).open(any());
        doNothing().when(whsef).open(any());
        doNothing().when(syscf).open(any());
        doNothing().when(repf).open(any());
        doNothing().when(stokf).close();
        doNothing().when(prodf).close();
        doNothing().when(whsef).close();
        doNothing().when(syscf).close();
        doNothing().when(repf).close();

        // Default: all files open cleanly.
        doReturn("00").when(stokf).getFileStatus();
        doReturn("00").when(syscf).getFileStatus();
        doReturn("00").when(repf).getFileStatus();

        // Captures REP-REC at the instant of WRITE, before the next write overwrites it.
        doAnswer(
                        inv -> {
                            repLines.add(repf.getRecord().getString("REP-REC"));
                            return null;
                        })
                .when(repf)
                .write();

        // STOKF START: succeeds by default (no INVALID KEY).
        doReturn(true).when(stokf).start(anyString(), anyString());
        doReturn(false).when(stokf).isInvalidKey();

        // STOKF READ NEXT: pulls the next configured row into the buffer; once
        // exhausted, isAtEnd() flips true (mirrors AT END on the following READ NEXT).
        doAnswer(
                        inv -> {
                            int i = stokfIndex.getAndIncrement();
                            if (i < stockRows.size()) {
                                StockRow row = stockRows.get(i);
                                stokf.getRecord().setInt("SK-PROD", row.prod());
                                stokf.getRecord().setInt("SK-WHSE", row.whse());
                                stokf.getRecord()
                                        .setDecimal("SK-ONHAND", BigDecimal.valueOf(row.onhand()));
                                stokf.getRecord().setDecimal("SK-AVG-COST", row.avgCost());
                            }
                            return null;
                        })
                .when(stokf)
                .readNext();
        doAnswer(inv -> stokfIndex.get() > stockRows.size()).when(stokf).isAtEnd();

        // PRODF / WHSEF: found iff a name was registered for the requested code.
        doAnswer(
                        inv -> {
                            int code = prodf.getRecord().getInt("PR-CODE");
                            if (productNames.containsKey(code)) {
                                prodf.getRecord().setString("PR-NAME", productNames.get(code));
                                return true;
                            }
                            return false;
                        })
                .when(prodf)
                .readByKey(any());
        doAnswer(inv -> !productNames.containsKey(prodf.getRecord().getInt("PR-CODE")))
                .when(prodf)
                .isInvalidKey();

        doAnswer(
                        inv -> {
                            int code = whsef.getRecord().getInt("WH-CODE");
                            if (warehouseNames.containsKey(code)) {
                                whsef.getRecord().setString("WH-NAME", warehouseNames.get(code));
                                return true;
                            }
                            return false;
                        })
                .when(whsef)
                .readByKey(any());
        doAnswer(inv -> !warehouseNames.containsKey(whsef.getRecord().getInt("WH-CODE")))
                .when(whsef)
                .isInvalidKey();

        // SYSCF: found by default, no company-name override configured per test.
        doReturn(true).when(syscf).readByKey(any());
        doReturn(false).when(syscf).isInvalidKey();
        syscf.getRecord().setString("SY-COMPANY-NAME", "SAKURA Sales Management System");

        // DATEUT: fixed system date.
        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            p.getKdate().setKdDate1(20260101);
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        productNames.put(100, "Widget A");
        warehouseNames.put(1, "Main WH");

        service = new Rp0060Service(fileSet, dateutService, abortxService);
    }

    private String joinedReport() {
        return String.join("\n", repLines);
    }

    // ───────────────────────── happy path ─────────────────────────

    @Test
    void execute_happyPath_singleWarehouseSingleProduct_writesDetailSubtotalAndGrandTotal() {
        stockRows = List.of(new StockRow(100, 1, 50, new BigDecimal("2.50")));

        service.execute();

        String report = joinedReport();
        assertThat(report).contains("SAKURA Sales Management System");
        assertThat(report).contains("WAREHOUSE: 001");
        assertThat(report).contains("Main WH");
        assertThat(report).contains("00000100");
        assertThat(report).contains("Widget A");
        assertThat(report).contains("125.00");
        assertThat(report).contains("WAREHOUSE SUBTOTAL");
        assertThat(report).contains("GRAND TOTAL");
        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(stokf, times(1)).close();
        verify(prodf, times(1)).close();
        verify(whsef, times(1)).close();
        verify(repf, times(1)).close();
    }

    @Test
    void execute_multipleProductsSameWarehouse_noHeaderReprintAndAccumulatesSubtotal() {
        productNames.put(200, "Widget B");
        stockRows =
                List.of(
                        new StockRow(100, 1, 50, new BigDecimal("2.50")),
                        new StockRow(200, 1, 10, new BigDecimal("3.00")));

        service.execute();

        long warehouseHeaderCount =
                repLines.stream().filter(l -> l.contains("WAREHOUSE: 001")).count();
        assertThat(warehouseHeaderCount).isEqualTo(1);
        String report = joinedReport();
        assertThat(report).contains("00000100").contains("00000200");
        assertThat(report).contains("125.00");
        assertThat(report).contains("30.00");
        // subtotal = 50*2.50 + 10*3.00 = 125.00 + 30.00 = 155.00
        assertThat(report).contains("155.00");
    }

    @Test
    void execute_multipleWarehouses_printsSubtotalPerWarehouseAndTransitionsHeader() {
        productNames.put(200, "Widget B");
        warehouseNames.put(2, "Second WH");
        stockRows =
                List.of(
                        new StockRow(100, 1, 50, new BigDecimal("2.50")),
                        new StockRow(200, 2, 4, new BigDecimal("10.00")));

        service.execute();

        long subtotalCount =
                repLines.stream().filter(l -> l.contains("WAREHOUSE SUBTOTAL")).count();
        assertThat(subtotalCount).isEqualTo(2);
        String report = joinedReport();
        assertThat(report).contains("WAREHOUSE: 001").contains("WAREHOUSE: 002");
        assertThat(report).contains("Main WH").contains("Second WH");
        // grand total qty = 50 + 4 = 54, value = 125.00 + 40.00 = 165.00
        assertThat(report).contains("165.00");
    }

    // ───────────────────────── no-records / soft-EOF paths ─────────────────────────

    @Test
    void execute_stokfStartInvalidKey_printsNoStockRecordsMessage() {
        doReturn(true).when(stokf).isInvalidKey();
        stockRows = List.of();

        service.execute();

        assertThat(joinedReport()).contains("*** NO STOCK RECORDS ***");
        verify(stokf, never()).readNext();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_stokfOpenStatus35_setsEofAndPrintsNoStockRecordsMessage() {
        doReturn("35").when(stokf).getFileStatus();
        stockRows = List.of();

        service.execute();

        assertThat(joinedReport()).contains("*** NO STOCK RECORDS ***");
        verify(stokf, never()).start(anyString(), anyString());
        verify(abortxService, never()).execute(any());
        assertThat(service.getCompletionCode()).isEqualTo(0);
        // PRODF/WHSEF/REPF still open normally even though STOKF is soft-EOF.
        verify(prodf, times(1)).open(any());
        verify(whsef, times(1)).open(any());
        verify(repf, times(1)).open(any());
    }

    @Test
    void execute_stokfOpenStatus30_setsEofAndPrintsNoStockRecordsMessage() {
        doReturn("30").when(stokf).getFileStatus();
        stockRows = List.of();

        service.execute();

        assertThat(joinedReport()).contains("*** NO STOCK RECORDS ***");
        verify(abortxService, never()).execute(any());
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_noRowsButStartSucceeds_readNextImmediatelyAtEnd() {
        stockRows = List.of();

        service.execute();

        assertThat(joinedReport()).contains("*** NO STOCK RECORDS ***");
        verify(stokf, times(1)).readNext();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    // ───────────────────────── file-open abend (ground truth: INIT-RTN / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_stokfOpenFailsPersistently_abortsWithCompletionCode255AndSkipsOtherFileOpens() {
        doReturn("99").when(stokf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(prodf, never()).open(any());
        verify(whsef, never()).open(any());
        verify(repf, never()).open(any());
    }

    @Test
    void execute_repfOpenFails_abortsWithCompletionCode255() {
        doReturn("99").when(repf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(stokf, times(1)).open(any());
        verify(prodf, times(1)).open(any());
        verify(whsef, times(1)).open(any());
    }

    // ───────────────────────── SYSCF company-name override (ground truth: INIT-010)
    // ─────────────────────────

    @Test
    void execute_syscfFound_usesCompanyNameFromSyscf() {
        syscf.getRecord().setString("SY-COMPANY-NAME", "Custom Co Ltd");
        stockRows = List.of(new StockRow(100, 1, 50, new BigDecimal("2.50")));

        service.execute();

        assertThat(joinedReport()).contains("Custom Co Ltd");
    }

    @Test
    void execute_syscfNotFound_usesDefaultCompanyName() {
        doReturn(true).when(syscf).isInvalidKey();
        stockRows = List.of(new StockRow(100, 1, 50, new BigDecimal("2.50")));

        service.execute();

        assertThat(joinedReport()).contains("SAKURA Sales Management System");
    }

    @Test
    void execute_syscfOpenFails_skipsReadAndUsesDefaultCompanyName() {
        doReturn("99").when(syscf).getFileStatus();
        stockRows = List.of(new StockRow(100, 1, 50, new BigDecimal("2.50")));

        service.execute();

        verify(syscf, never()).readByKey(any());
        assertThat(joinedReport()).contains("SAKURA Sales Management System");
    }

    // ───────────────────────── unknown product/warehouse (ground truth: PW-010 / PDT-010)
    // ─────────────────────────

    @Test
    void execute_productNotFound_printsUnknownProductName() {
        stockRows = List.of(new StockRow(999, 1, 5, new BigDecimal("1.00")));

        service.execute();

        assertThat(joinedReport()).contains("(unknown)");
        assertThat(joinedReport()).contains("00000999");
    }

    @Test
    void execute_warehouseNotFound_printsUnknownWarehouseName() {
        stockRows = List.of(new StockRow(100, 9, 5, new BigDecimal("1.00")));

        service.execute();

        assertThat(joinedReport()).contains("WAREHOUSE: 009");
        assertThat(joinedReport()).contains("(unknown)");
    }

    // ───────────────────────── value computation edge case (ground truth: PDT-010 COMPUTE)
    // ─────────────────────────

    @Test
    void execute_zeroOnHandQuantity_computesZeroValueAndStillPrintsLine() {
        stockRows = List.of(new StockRow(100, 1, 0, new BigDecimal("2.50")));

        service.execute();

        assertThat(joinedReport()).contains("00000100");
        assertThat(joinedReport()).contains("0.00");
    }
}

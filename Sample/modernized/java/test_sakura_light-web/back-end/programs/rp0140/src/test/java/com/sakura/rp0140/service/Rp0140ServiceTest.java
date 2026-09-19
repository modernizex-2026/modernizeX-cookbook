package com.sakura.rp0140.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0140.runtime.Rp0140Datasets;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.RepfDataset;
import com.sakura.runtime.io.StokfDataset;
import com.sakura.runtime.io.SyscfDataset;
import com.sakura.runtime.io.WhsefDataset;
import com.sakura.runtime.linkage.AbortxLinkParm;
import com.sakura.runtime.linkage.DateutLinkParm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for Rp0140Service, generated from COBOL program RP0140 (Dead / slow-moving stock
 * report). Test cases and expected values are derived from the COBOL PROCEDURE DIVISION logic
 * (RP0140.cob), not from the Java implementation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Rp0140ServiceTest {

    @Mock private Rp0140Datasets fileSet;

    @Spy private StokfDataset stokf = new StokfDataset();

    @Spy private ProdfDataset prodf = new ProdfDataset();

    @Spy private WhsefDataset whsef = new WhsefDataset();

    @Spy private SyscfDataset syscf = new SyscfDataset();

    @Spy private RepfDataset repf = new RepfDataset();

    @Mock private DateutService dateutService;

    @Mock private AbortxService abortxService;

    private final List<String> repLines = new ArrayList<>();

    /** Days/status returned by the DATEUT "DIFF" call inside CALC-ITEM (CI-010). */
    private int diffDays = 10;

    private String diffStatus = "00";

    private Rp0140Service service;

    @BeforeEach
    void setUp() {
        when(fileSet.getStokf()).thenReturn(stokf);
        when(fileSet.getProdf()).thenReturn(prodf);
        when(fileSet.getWhsef()).thenReturn(whsef);
        when(fileSet.getSyscf()).thenReturn(syscf);
        when(fileSet.getRepf()).thenReturn(repf);

        doNothing().when(stokf).open(any());
        doNothing().when(stokf).close();
        doReturn(false).when(stokf).start(any(), any());
        doReturn(false).when(stokf).readNext();

        doNothing().when(prodf).open(any());
        doNothing().when(prodf).close();
        doReturn(false).when(prodf).readByKey(any());

        doNothing().when(whsef).open(any());
        doNothing().when(whsef).close();
        doReturn(false).when(whsef).readByKey(any());

        doNothing().when(syscf).open(any());
        doNothing().when(syscf).close();
        doReturn(false).when(syscf).readByKey(any());

        doNothing().when(repf).open(any());
        doNothing().when(repf).close();
        doAnswer(
                        inv -> {
                            repLines.add(repf.buffer().getString("REP-REC"));
                            return null;
                        })
                .when(repf)
                .write();

        // CALL DATEUT: "TODY" (INIT-010) sets today's date; "DIFF" (CI-010) sets KD-DAYS/KD-STATUS.
        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            String func =
                                    p.getKdate().getKdFunc() == null
                                            ? ""
                                            : p.getKdate().getKdFunc().trim();
                            if ("TODY".equals(func)) {
                                p.getKdate().setKdDate1(20250101);
                            } else {
                                p.getKdate().setKdDays(diffDays);
                                p.getKdate().setKdStatus(diffStatus);
                            }
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        service = new Rp0140Service(fileSet, dateutService, abortxService);
    }

    /**
     * Queues up N successive STOKF NEXT reads; each setter fills the current buffer before the
     * record is processed.
     */
    private void stubStockRecords(List<Runnable> recordSetters) {
        int[] idx = {0};
        doAnswer(
                        inv -> {
                            if (idx[0] < recordSetters.size()) {
                                recordSetters.get(idx[0]).run();
                            }
                            idx[0]++;
                            return true;
                        })
                .when(stokf)
                .readNext();
        doAnswer(inv -> idx[0] > recordSetters.size()).when(stokf).isAtEnd();
    }

    private Runnable stockRecord(
            int whse, int prod, String onhand, String avgCost, int lastOutDate) {
        return () -> {
            stokf.buffer().setInt("SK-WHSE", whse);
            stokf.buffer().setInt("SK-PROD", prod);
            stokf.buffer().setDecimal("SK-ONHAND", new BigDecimal(onhand));
            stokf.buffer().setDecimal("SK-AVG-COST", new BigDecimal(avgCost));
            stokf.buffer().setInt("SK-LAST-OUT-DATE", lastOutDate);
        };
    }

    private String lastLine() {
        return repLines.get(repLines.size() - 1);
    }

    // === MAIN-000 / INIT-010 ================================================

    @Test
    void mainProgram_sysConfigFound_singleOkItem_happyPath() {
        doReturn(false).when(syscf).isInvalidKey();
        syscf.buffer().setString("SY-COMPANY-NAME", "ACME CORP");

        stubStockRecords(List.of(stockRecord(1, 100, "50", "2.50", 20240101)));
        diffDays = 30;
        diffStatus = "00";

        doReturn(false).when(prodf).isInvalidKey();
        prodf.buffer().setString("PR-NAME", "WIDGET");
        doReturn(false).when(whsef).isInvalidKey();
        whsef.buffer().setString("WH-NAME", "MAIN WH");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        assertThat(repLines).anyMatch(l -> l.contains("ACME CORP"));
        assertThat(repLines).anyMatch(l -> l.contains("WIDGET") && l.trim().endsWith("OK"));
        assertThat(repLines).anyMatch(l -> l.contains("MAIN WH"));
        verify(stokf, times(1)).close();
        verify(prodf, times(1)).close();
        verify(whsef, times(1)).close();
        verify(repf, times(1)).close();
        verify(abortxService, never()).execute(any());
    }

    @Test
    void mainProgram_sysConfigInvalidKey_keepsDefaultCompanyName_andReportsNoStock() {
        doReturn(true).when(syscf).isInvalidKey();
        doReturn(true)
                .when(stokf)
                .isInvalidKey(); // START fails -> WK-MAIN-EOF = 1, no records processed

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        // WK-COMPANY keeps its INIT-010 default since SYSCF read was INVALID KEY.
        assertThat(repLines).anyMatch(l -> l.contains("SAKURA Sales Management System"));
        assertThat(repLines).anyMatch(l -> l.contains("*** NO STOCK ON HAND ***"));
        verify(prodf, never()).readByKey(any());
    }

    @Test
    void mainProgram_stokfOpenUnexpectedStatus_abortsWithCode255() {
        doReturn("99").when(stokf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(prodf, never()).open(any());
        verify(whsef, never()).open(any());
        verify(repf, never()).open(any());

        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService, times(1)).execute(captor.capture());
        AbortxLinkParm.Kabend kabend = captor.getValue().getKabend();
        assertThat(kabend.getKaFile().trim()).isEqualTo("STOKF");
        assertThat(kabend.getKaMsgcode()).isEqualTo("EOPEN ");
        assertThat(kabend.getKaDetail().trim()).isEqualTo("Report file error");
    }

    @Test
    void mainProgram_stokfOpenStatus35_treatedAsEmptyFile_noAbort() {
        doReturn("35").when(stokf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(abortxService, never()).execute(any());
        verify(prodf, times(1)).open(any());
        verify(repf, times(1)).open(any());
        // WK-MAIN-EOF forced to 1 by FSTS <> "00" -> no START/READ against STOKF.
        verify(stokf, never()).start(any(), any());
        assertThat(repLines).anyMatch(l -> l.contains("*** NO STOCK ON HAND ***"));
    }

    @Test
    void mainProgram_repfOpenFails_abortsWithCode255() {
        doReturn("99").when(repf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        ArgumentCaptor<AbortxLinkParm> captor = ArgumentCaptor.forClass(AbortxLinkParm.class);
        verify(abortxService, times(1)).execute(captor.capture());
        assertThat(captor.getValue().getKabend().getKaFile().trim()).isEqualTo("REPF");
    }

    // === PROCESS-ONE / PO-010 ===============================================

    @Test
    void processStockRecord_zeroOnhand_skipsRecord() {
        stubStockRecords(List.of(stockRecord(1, 100, "0", "2.50", 0)));

        service.execute();

        verify(prodf, never()).readByKey(any());
        verify(whsef, never()).readByKey(any());
        assertThat(repLines).anyMatch(l -> l.contains("*** NO STOCK ON HAND ***"));
    }

    @Test
    void processStockRecord_negativeOnhand_skipsRecord() {
        stubStockRecords(List.of(stockRecord(1, 100, "-5", "2.50", 0)));

        service.execute();

        verify(prodf, never()).readByKey(any());
        assertThat(repLines).anyMatch(l -> l.contains("*** NO STOCK ON HAND ***"));
    }

    // === PO-010 control break ================================================

    @Test
    void controlBreak_warehouseChange_writesSubtotalBeforeNewWarehouseHeader() {
        stubStockRecords(
                List.of(
                        stockRecord(1, 100, "10", "1.00", 0),
                        stockRecord(2, 200, "20", "1.00", 0)));

        service.execute();

        long warehouseHeaders = repLines.stream().filter(l -> l.contains("WAREHOUSE:")).count();
        long subtotals = repLines.stream().filter(l -> l.contains("WHSE SUBTOTAL")).count();
        assertThat(warehouseHeaders).isEqualTo(2);
        // FLUSH-WH runs once for warehouse 1 before warehouse 2 starts; the final warehouse's
        // subtotal is only written from PRINT-010 when WK-FIRST = 0, i.e. also once more.
        assertThat(subtotals).isEqualTo(2);
    }

    // === CALC-ITEM / CI-010 ==================================================

    @Test
    void computeIdleDaysAndFlag_lastOutDateZero_setsSentinelDaysAndDeadFlag() {
        stubStockRecords(List.of(stockRecord(1, 100, "10", "1.00", 0)));

        service.execute();

        assertThat(repLines).anyMatch(l -> l.trim().endsWith("DEAD"));
        // SK-LAST-OUT-DATE = 0 short-circuits CI-010 before it ever calls DATEUT "DIFF";
        // DATEUT is only invoked once, for INIT-010's "TODY".
        verify(dateutService, times(1)).execute(any());
    }

    @Test
    void computeIdleDaysAndFlag_dateutStatusNotOk_fallsBackToSentinel() {
        stubStockRecords(List.of(stockRecord(1, 100, "10", "1.00", 20240101)));
        diffDays = 5;
        diffStatus = "99"; // DATEUT DIFF failed -> WK-DAYS forced back to the 99999 sentinel

        service.execute();

        assertThat(repLines).anyMatch(l -> l.trim().endsWith("DEAD"));
        verify(dateutService, times(2)).execute(any());
    }

    @Test
    void computeIdleDaysAndFlag_negativeDays_correctedToZero_okFlag() {
        stubStockRecords(List.of(stockRecord(1, 100, "10", "1.00", 20240101)));
        diffDays = -5;
        diffStatus = "00";

        service.execute();

        assertThat(repLines).anyMatch(l -> l.trim().endsWith("OK"));
    }

    @Test
    void computeIdleDaysAndFlag_boundaryAt90_okForBothItems() {
        stubStockRecords(
                List.of(
                        stockRecord(1, 100, "10", "1.00", 20240101),
                        stockRecord(1, 200, "10", "1.00", 20240102)));
        diffDays = 90;
        diffStatus = "00";

        service.execute();

        // WK-DAYS = 90 is NOT > 90 -> OK for both items (COBOL: WHEN WK-DAYS > 90).
        long okLines = repLines.stream().filter(l -> l.trim().endsWith("OK")).count();
        assertThat(okLines).isEqualTo(2);
    }

    @Test
    void computeIdleDaysAndFlag_boundaryAt91_isSlow() {
        stubStockRecords(List.of(stockRecord(1, 100, "10", "1.00", 20240101)));
        diffDays = 91;
        diffStatus = "00";

        service.execute();

        assertThat(repLines).anyMatch(l -> l.trim().endsWith("SLOW"));
    }

    @Test
    void computeIdleDaysAndFlag_boundaryAt180_isSlowNotDead() {
        stubStockRecords(List.of(stockRecord(1, 100, "10", "1.00", 20240101)));
        diffDays = 180;
        diffStatus = "00";

        service.execute();

        assertThat(repLines).anyMatch(l -> l.trim().endsWith("SLOW"));
    }

    @Test
    void computeIdleDaysAndFlag_boundaryAt181_isDead() {
        stubStockRecords(List.of(stockRecord(1, 100, "10", "1.00", 20240101)));
        diffDays = 181;
        diffStatus = "00";

        service.execute();

        assertThat(repLines).anyMatch(l -> l.trim().endsWith("DEAD"));
    }

    // === LOOKUP-PROD / LOOKUP-WHSE ===========================================

    @Test
    void lookupProductName_invalidKey_usesUnknownProductPlaceholder() {
        stubStockRecords(List.of(stockRecord(1, 100, "10", "1.00", 0)));
        doReturn(true).when(prodf).isInvalidKey();

        service.execute();

        assertThat(repLines).anyMatch(l -> l.contains("(unknown product)"));
    }

    @Test
    void lookupWarehouseName_invalidKey_usesUnknownWarehousePlaceholder() {
        stubStockRecords(List.of(stockRecord(1, 100, "10", "1.00", 0)));
        doReturn(true).when(whsef).isInvalidKey();

        service.execute();

        assertThat(repLines).anyMatch(l -> l.contains("(unknown warehouse)"));
    }

    // === PRINT-GRAND / PG-010 ================================================

    @Test
    void printGrandTotal_noItems_writesNoStockMessage() {
        // START STOKF finds no rows -> WK-MAIN-EOF = 1 immediately (PRINT-010), same as COBOL's
        // START ... INVALID KEY path; without this the STOKF spy's real isInvalidKey()/isAtEnd()
        // never signal EOF and the READ-STOK loop never terminates.
        doReturn(true).when(stokf).isInvalidKey();

        service.execute();

        assertThat(lastLine()).contains("*** NO STOCK ON HAND ***");
        assertThat(repLines).noneMatch(l -> l.contains("GRAND TOTAL"));
    }

    @Test
    void printGrandTotal_withItems_writesGrandTotalsWithCorrectIdleCount() {
        stubStockRecords(
                List.of(
                        stockRecord(1, 100, "10", "1.00", 20240101), // OK -> not idle
                        stockRecord(1, 200, "10", "1.00", 0))); // no movement -> DEAD -> idle
        diffDays = 10;
        diffStatus = "00";

        service.execute();

        String grandLine =
                repLines.stream().filter(l -> l.contains("GRAND TOTAL")).findFirst().orElse("");
        assertThat(grandLine).isNotEmpty();
        assertThat(grandLine.replaceAll("\\s+", "")).contains("ITEMS=2").contains("IDLE=1");
    }

    // === CHECK-PAGE / PAGE-HEAD ==============================================

    @Test
    void pageBreak_midWarehouse_reprintsHeaderAfter55Lines() {
        Runnable sameRecord = stockRecord(1, 100, "10", "1.00", 0);
        stubStockRecords(Collections.nCopies(49, sameRecord));

        service.execute();

        long pageHeaders = repLines.stream().filter(l -> l.contains("PAGE:")).count();
        // CONVERT-GAP: COBOL's "MOVE RD-WHSE TO REP-REC" etc. space-fill the whole 132-byte
        // REP-REC whenever the source record is shorter (RD-WHSE/RC-HEAD/RD-SUB/RD-GRAND are
        // all < 132 bytes). The Java port's copyBytes("REP-REC", fromName) (RecordImage.java
        // writeBytes) only overwrites the source field's own length, so bytes beyond that
        // length keep stale content from the previous, longer write into REP-REC (e.g. RPT-H1's
        // "PAGE: " literal). That stale "PAGE:" text leaks into later, shorter lines and this
        // count comes out as 4 instead of the COBOL-correct 2 -- expected here is COBOL ground
        // truth, so this assertion is expected to FAIL until REP-REC is cleared/space-filled
        // before each copyBytes.
        assertThat(pageHeaders).isEqualTo(2);
    }
}

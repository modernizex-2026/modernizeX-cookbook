package com.sakura.pu0040.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.numgen.service.NumgenService;
import com.sakura.pu0040.domain.Pu0040FieldAccess;
import com.sakura.pu0040.runtime.Pu0040Datasets;
import com.sakura.runtime.ScreenModels.InputFieldDef;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.AplfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.PurdfDataset;
import com.sakura.runtime.io.PurhfDataset;
import com.sakura.runtime.io.SmovfDataset;
import com.sakura.runtime.io.StokfDataset;
import com.sakura.runtime.io.SuppfDataset;
import com.sakura.runtime.linkage.NumgenLinkParm;
import com.sakura.runtime.linkage.TaxcalLinkParm;
import com.sakura.runtime.record.RuntimeFieldAccess;
import com.sakura.taxcal.service.TaxcalService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import java.util.concurrent.atomic.AtomicLong;

/**
 * Unit tests for Pu0040Service (COBOL PU0040 — purchase return entry: returns previously purchased
 * goods to a supplier, writing a NEGATIVE-quantity PURHF/PURDF pair, decreasing stock on-hand,
 * writing a return-out SMOVF movement and DEBITING the AP ledger), generated from {@code
 * PU0040.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>PURHF/PURDF/APLF/STOKF/SMOVF/SUPPF/PRODF are real dataset objects wrapped with {@code spy()}
 * so the record buffer (and therefore {@code Pu0040FieldAccess}) works exactly as in production;
 * only I/O methods are stubbed so no real file/DB access happens. DATEUT runs for real (pure
 * calendar math, no file I/O). TAXCAL/NUMGEN/ABORTX are mocked at the CALL boundary since PU0040
 * talks to them only via {@code execute(XxxLinkParm)}.
 *
 * <p>Each RETURN-LOOP iteration issues exactly one {@code renderer.readEndStatus()} call per screen
 * ACCEPT (header, each detail line, confirm). The outer loop only terminates via ESTS="03" AND
 * VH-SUPP=0 on a header ACCEPT (COBOL EHDR-010) — tests therefore drive a queued VH-SUPP accept
 * sequence ["100" (or a test-specific value), "0"] so the SECOND header ACCEPT cleanly ends the
 * program.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Pu0040ServiceTest {

    private ScreenRendererInstance renderer;
    private DateutService dateutService;
    private TaxcalService taxcalService;
    private NumgenService numgenService;
    private AbortxService abortxService;

    private Pu0040Datasets fileSet;
    private PurhfDataset purhf;
    private PurdfDataset purdf;
    private AplfDataset aplf;
    private StokfDataset stokf;
    private SmovfDataset smovf;
    private SuppfDataset suppf;
    private ProdfDataset prodf;

    private Pu0040Service service;

    private final Map<String, String> acceptValues = new HashMap<>();
    private final Map<String, List<String>> acceptSequences = new HashMap<>();
    private final Map<String, AtomicInteger> acceptCallCounts = new HashMap<>();
    private final List<String> screenInteractions = new ArrayList<>();
    private final List<String> msgLineHistory = new ArrayList<>();

    /**
     * 5-call ESTS sequence that adds exactly one detail line and saves the return: header("00") ->
     * detail-success("00") -> detail-end("03") -> confirm("00", value irrelevant to Y/N branching)
     * -> second-header-end("03", VH-SUPP queue drains to "0").
     */
    private static final String[] ONE_LINE_SAVE_SEQ = {"00", "00", "03", "00", "03"};

    /**
     * 4-call ESTS sequence for a detail attempt that fails validation (no line added): header("00")
     * -> detail-fails("00") -> detail-end("03") -> second-header-end("03").
     */
    private static final String[] ONE_LINE_FAIL_SEQ = {"00", "00", "03", "03"};

    @BeforeEach
    void setUp() {
        renderer = mock(ScreenRendererInstance.class);
        dateutService = new DateutService();
        taxcalService = mock(TaxcalService.class);
        numgenService = mock(NumgenService.class);
        abortxService = mock(AbortxService.class);

        Pu0040Datasets real = new Pu0040Datasets();
        fileSet = spy(real);
        purhf = spy(real.getPurhf());
        purdf = spy(real.getPurdf());
        aplf = spy(real.getAplf());
        stokf = spy(real.getStokf());
        smovf = spy(real.getSmovf());
        suppf = spy(real.getSuppf());
        prodf = spy(real.getProdf());
        doReturn(purhf).when(fileSet).getPurhf();
        doReturn(purdf).when(fileSet).getPurdf();
        doReturn(aplf).when(fileSet).getAplf();
        doReturn(stokf).when(fileSet).getStokf();
        doReturn(smovf).when(fileSet).getSmovf();
        doReturn(suppf).when(fileSet).getSuppf();
        doReturn(prodf).when(fileSet).getProdf();

        doNothing().when(purhf).open(any());
        doNothing().when(purdf).open(any());
        doNothing().when(aplf).open(any());
        doNothing().when(stokf).open(any());
        doNothing().when(smovf).open(any());
        doNothing().when(suppf).open(any());
        doNothing().when(prodf).open(any());
        doNothing().when(purhf).close();
        doNothing().when(purdf).close();
        doNothing().when(aplf).close();
        doNothing().when(stokf).close();
        doNothing().when(smovf).close();
        doNothing().when(suppf).close();
        doNothing().when(prodf).close();

        // Every file opens cleanly (FSTS "00") by default — no ABEND-010 path.
        doReturn("00").when(purhf).getFileStatus();
        doReturn("00").when(purdf).getFileStatus();
        doReturn("00").when(aplf).getFileStatus();
        doReturn("00").when(stokf).getFileStatus();
        doReturn("00").when(smovf).getFileStatus();
        doReturn("00").when(suppf).getFileStatus();
        doReturn("00").when(prodf).getFileStatus();

        // Default: supplier 100 found, not deleted, SP-TAX-TYPE 0 (voucher's own type wins).
        doReturn(true).when(suppf).readByKey(any());
        doReturn(false).when(suppf).isInvalidKey();
        suppf.getRecord().setInt("SP-CODE", 100);
        suppf.getRecord().setString("SP-NAME", "Acme Supplies");
        suppf.getRecord().setInt("SP-DEL-FLAG", 0);
        suppf.getRecord().setInt("SP-TAX-TYPE", 0);
        suppf.getRecord().setDecimal("SP-BALANCE", BigDecimal.valueOf(500));
        doNothing().when(suppf).rewrite();

        // Default: product 5001 found, stock-managed, default warehouse 1.
        doReturn(true).when(prodf).readByKey(any());
        doReturn(false).when(prodf).isInvalidKey();
        prodf.getRecord().setString("PR-NAME", "Widget");
        prodf.getRecord().setInt("PR-TAX-CATEGORY", 1);
        prodf.getRecord().setInt("PR-STOCK-MNG", 1);
        prodf.getRecord().setInt("PR-DFLT-WHSE", 1);
        prodf.getRecord().setInt("PR-DEL-FLAG", 0);
        prodf.getRecord().setDecimal("PR-LAST-COST", BigDecimal.valueOf(20));
        prodf.getRecord().setDecimal("PR-STD-COST", BigDecimal.valueOf(15));

        // Default: STOKF found for prod 5001 / whse 1, ample on-hand, positive avg cost.
        doReturn(true).when(stokf).readByKey(any());
        doReturn(false).when(stokf).isInvalidKey();
        stokf.getRecord().setDecimal("SK-AVG-COST", BigDecimal.valueOf(25));
        stokf.getRecord().setDecimal("SK-ONHAND", BigDecimal.valueOf(100));
        doNothing().when(stokf).rewrite();

        doNothing().when(purhf).write();
        doReturn(false).when(purhf).isInvalidKey();
        doNothing().when(purdf).write();
        doReturn(false).when(purdf).isInvalidKey();
        doNothing().when(aplf).write();
        doReturn(false).when(aplf).isInvalidKey();
        doNothing().when(smovf).write();
        doReturn(false).when(smovf).isInvalidKey();

        // TAXCAL happy path: KT-STATUS "00", net/tax/gross echo the requested amount (no tax).
        doAnswer(
                        inv -> {
                            TaxcalLinkParm p = inv.getArgument(0);
                            BigDecimal amount = p.getKtax().getKtAmount();
                            p.getKtax().setKtStatus("00");
                            p.getKtax().setKtNet(amount);
                            p.getKtax().setKtTax(BigDecimal.ZERO);
                            p.getKtax().setKtGross(amount);
                            return null;
                        })
                .when(taxcalService)
                .execute(any());

        // NUMGEN happy path: sequential numbers per key, starting at 9000000001.
        Map<String, AtomicLong> numgenSeq = new HashMap<>();
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            AtomicLong seq =
                                    numgenSeq.computeIfAbsent(
                                            p.getKnum().getKnumKey(),
                                            k -> new AtomicLong(9000000000L));
                            p.getKnum().setKnumStatus("00");
                            p.getKnum().setKnumNumber(seq.incrementAndGet());
                            return null;
                        })
                .when(numgenService)
                .execute(any());

        acceptValues.put("VH-DATE", "20260115");
        acceptValues.put("VH-SUPP", "100");
        acceptValues.put("VH-TAX-TYPE", "1");
        acceptValues.put("VH-REMARK", "");
        acceptValues.put("WK-D-PROD", "5001");
        acceptValues.put("WK-D-WHSE", "1");
        acceptValues.put("WK-D-QTY", "10");
        acceptValues.put("WK-D-COST", "25.00");
        acceptValues.put("WK-CONFIRM", "Y");
        // Default queue: iteration 1 supplier 100, iteration 2 supplier 0 -> ends the program
        // once ESTS="03" is delivered on the second header ACCEPT.
        sequenceAccept("VH-SUPP", "100", "0");

        when(renderer.acceptField(any()))
                .thenAnswer(
                        inv -> {
                            InputFieldDef field = inv.getArgument(0, InputFieldDef.class);
                            List<String> seq = acceptSequences.get(field.name);
                            if (seq != null && !seq.isEmpty()) {
                                int i =
                                        acceptCallCounts
                                                .computeIfAbsent(
                                                        field.name, k -> new AtomicInteger(0))
                                                .getAndIncrement();
                                return seq.get(Math.min(i, seq.size() - 1));
                            }
                            return acceptValues.getOrDefault(field.name, "");
                        });

        doAnswer(
                        inv -> {
                            com.sakura.runtime.ScreenModels.ScreenDef def = inv.getArgument(0);
                            RuntimeFieldAccess w = inv.getArgument(1);
                            screenInteractions.add("displayScreen:" + def.name);
                            if (w instanceof Pu0040FieldAccess a) {
                                msgLineHistory.add(a.getWkMsgLine().trim());
                                snapshots.add(new Snapshot(a));
                            }
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        service =
                new Pu0040Service(
                        fileSet,
                        dateutService,
                        taxcalService,
                        numgenService,
                        abortxService,
                        renderer);
    }

    /**
     * Overrides (or creates) the queued ACCEPT values for one screen field, one value per call; the
     * LAST value repeats once the queue is exhausted (mirrors Mockito's thenReturn semantics),
     * matching the RETURN-LOOP's normal "run out after N calls".
     */
    private void sequenceAccept(String field, String... values) {
        acceptSequences.put(field, List.of(values));
        acceptCallCounts.remove(field);
    }

    /**
     * RETURN-LOOP always runs one further ("empty") pass after saving a return, purely to read the
     * PF3/VH-SUPP=0 combination that ends the program (COBOL: END-FLG can only be set inside
     * EHDR-010, which runs at the TOP of the loop). That empty pass's CLRR-010 re-runs {@code
     * INITIALIZE VH-REC} and resets every WK-* control field — so PURHF-backed fields (VH-NO,
     * VH-SUPP, VH-KIND, VH-AMOUNT, ...) and WorkingStorage totals (WK-LCNT, WK-NET-TOTAL,
     * WK-TAX-TOTAL, WK-GRS-TOTAL, VH-TAX-TYPE) read via {@link #capturedWs()} AFTER {@code
     * service.execute()} returns reflect that reset, not what iteration 1 wrote. A {@link Snapshot}
     * is captured on every {@code displayScreen} call (values copied out, not just a reference into
     * the shared mutable buffer), so tests recover the state at the exact point a given WK-MSG-LINE
     * was shown — before the emptying pass overwrote it.
     */
    private static final class Snapshot {
        final String msgLine;
        final int vhTaxType;
        final int wkLcnt;
        final long vhNo;
        final int vhSupp;
        final int vhKind;
        final int vhLines;
        final BigDecimal vhAmount;
        final long wkNetTotal;
        final long wkTaxTotal;
        final long wkGrsTotal;

        Snapshot(Pu0040FieldAccess a) {
            this.msgLine = a.getWkMsgLine().trim();
            this.vhTaxType = a.getVhTaxType();
            this.wkLcnt = a.getWkLcnt();
            this.vhNo = a.getVhNo();
            this.vhSupp = a.getVhSupp();
            this.vhKind = a.getVhKind();
            this.vhLines = a.getVhLines();
            this.vhAmount = a.getVhAmount();
            this.wkNetTotal = a.getWkNetTotal();
            this.wkTaxTotal = a.getWkTaxTotal();
            this.wkGrsTotal = a.getWkGrsTotal();
        }
    }

    private final List<Snapshot> snapshots = new ArrayList<>();

    /**
     * The FIRST captured {@link Snapshot} whose WK-MSG-LINE equals {@code message} — the state as
     * of that exact displayScreen call, unaffected by any later CLRR-010 reset.
     */
    private Snapshot snapshotAt(String message) {
        for (Snapshot s : snapshots) {
            if (s.msgLine.equals(message)) {
                return s;
            }
        }
        throw new AssertionError("No displayScreen snapshot captured for WK-MSG-LINE: " + message);
    }

    /**
     * Live handle onto the service's WorkingStorage/FD field accessor, obtained from the first
     * displayScreen() call. Same mutable object throughout the run.
     */
    private Pu0040FieldAccess capturedWs() {
        ArgumentCaptor<RuntimeFieldAccess> captor =
                ArgumentCaptor.forClass(RuntimeFieldAccess.class);
        verify(renderer, org.mockito.Mockito.atLeastOnce()).displayScreen(any(), captor.capture());
        return (Pu0040FieldAccess) captor.getAllValues().get(0);
    }

    // ───────────────────────── file open lifecycle (ground truth: OPEN-FILES / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_purhfOpenFailsPermanently_abortsWithCompletionCode255() {
        doReturn("12").when(purhf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(purdf, never()).open(any());
        verify(renderer, never()).displayScreen(any(), any());
    }

    @Test
    void execute_purhfOpenFileNotFound_retriesAsOutputThenReopensIo() {
        doReturn("35", "00", "00", "00").when(purhf).getFileStatus();
        sequenceAccept("VH-SUPP", "0");
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(purhf, times(1)).open(com.sakura.runtime.DatasetEnums.FileOpenMode.OUTPUT);
        verify(purhf, times(2)).open(com.sakura.runtime.DatasetEnums.FileOpenMode.IO);
        verify(abortxService, never()).execute(any());
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    // ───────────────────────── header entry / loop exit (ground truth: EHDR-010)
    // ─────────────────────────

    @Test
    void execute_pf3WithZeroSupplierOnFirstHeader_endsProgramImmediately() {
        sequenceAccept("VH-SUPP", "0");
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(purhf, times(1)).open(any());
        verify(purhf, times(1)).close();
        verify(suppf, never()).readByKey(any());
    }

    @Test
    void execute_supplierCodeRequired_showsMessageAndLoopsToSecondHeader() {
        sequenceAccept("VH-SUPP", "0");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Supplier code required");
        verify(suppf, never()).readByKey(any());
    }

    // ───────────────────────── supplier validation (ground truth: VHDR-010)
    // ─────────────────────────

    @Test
    void execute_supplierNotFound_showsNotFoundMessage() {
        doReturn(true).when(suppf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Supplier not found");
    }

    @Test
    void execute_supplierDeleted_showsDeletedMessage() {
        suppf.getRecord().setInt("SP-DEL-FLAG", 1);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Supplier is deleted");
    }

    @Test
    void execute_voucherTaxTypeZero_defaultsFromSupplierTaxType() {
        // VH-SUPP stays on the default ["100","0"] queue: iteration 1 must find the real
        // supplier (100) so VHDR-010's defaulting logic actually runs, not exit immediately.
        acceptValues.put("VH-TAX-TYPE", "0");
        suppf.getRecord().setInt("SP-TAX-TYPE", 3);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Header OK - enter return lines");
        // Read at the VHDR-010 success display, before the terminating empty pass's CLRR-010
        // resets VH-TAX-TYPE back to its own default of 1.
        assertThat(snapshotAt("Header OK - enter return lines").vhTaxType).isEqualTo(3);
    }

    @Test
    void execute_voucherAndSupplierTaxTypeBothZero_defaultsToOne() {
        acceptValues.put("VH-TAX-TYPE", "0");
        suppf.getRecord().setInt("SP-TAX-TYPE", 0);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Header OK - enter return lines");
        assertThat(snapshotAt("Header OK - enter return lines").vhTaxType).isEqualTo(1);
    }

    // ───────────────────────── no lines entered (ground truth: RLOOP-010)
    // ─────────────────────────

    @Test
    void execute_noLinesEntered_discardsReturnWithoutSaving() {
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("No lines entered - return discarded");
        assertThat(capturedWs().getWkLcnt()).isEqualTo(0);
        verify(purhf, never()).write();
    }

    // ───────────────────────── detail loop dispatch (ground truth: DLOOP-010)
    // ─────────────────────────

    @Test
    void execute_detailLineCleared_showsMessageAndContinuesLoop() {
        acceptValues.put("WK-CONFIRM", "N");
        when(renderer.readEndStatus()).thenReturn("00", "04", "00", "03", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Line cleared");
        // WK-LCNT read at "Line added" (right after the successful 2nd detail attempt),
        // before the terminating empty pass's CLRR-010 resets it back to 0.
        assertThat(snapshotAt("Line added").wkLcnt).isEqualTo(1);
    }

    @Test
    void execute_detailInvalidKey_showsInvalidKeyMessage() {
        when(renderer.readEndStatus()).thenReturn("00", "09", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Invalid key");
        assertThat(capturedWs().getWkLcnt()).isEqualTo(0);
    }

    // ───────────────────────── process detail (ground truth: PDET-010) ─────────────────────────

    @Test
    void execute_productCodeRequired_showsMessage() {
        acceptValues.put("WK-D-PROD", "0");
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_FAIL_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_FAIL_SEQ, 1, ONE_LINE_FAIL_SEQ.length));

        service.execute();

        assertThat(msgLineHistory).contains("Product code required");
        verify(prodf, never()).readByKey(any());
        assertThat(capturedWs().getWkLcnt()).isEqualTo(0);
    }

    @Test
    void execute_productNotFound_showsMessage() {
        doReturn(true).when(prodf).isInvalidKey();
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_FAIL_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_FAIL_SEQ, 1, ONE_LINE_FAIL_SEQ.length));

        service.execute();

        assertThat(msgLineHistory).contains("Product not found");
    }

    @Test
    void execute_productDeleted_showsMessage() {
        prodf.getRecord().setInt("PR-DEL-FLAG", 1);
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_FAIL_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_FAIL_SEQ, 1, ONE_LINE_FAIL_SEQ.length));

        service.execute();

        assertThat(msgLineHistory).contains("Product is deleted");
    }

    @Test
    void execute_warehouseRequired_whenEnteredZeroAndNoProductDefault() {
        acceptValues.put("WK-D-WHSE", "0");
        prodf.getRecord().setInt("PR-DFLT-WHSE", 0);
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_FAIL_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_FAIL_SEQ, 1, ONE_LINE_FAIL_SEQ.length));

        service.execute();

        assertThat(msgLineHistory).contains("Warehouse required");
    }

    @Test
    void execute_warehouseDefaultsFromProduct_whenEnteredZero() {
        acceptValues.put("WK-D-WHSE", "0");
        prodf.getRecord().setInt("PR-DFLT-WHSE", 7);
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        assertThat(capturedWs().getWlWhse(1)).isEqualTo(7);
    }

    @Test
    void execute_qtyMustBePositive_showsMessage() {
        acceptValues.put("WK-D-QTY", "0");
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_FAIL_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_FAIL_SEQ, 1, ONE_LINE_FAIL_SEQ.length));

        service.execute();

        assertThat(msgLineHistory).contains("Return qty must be positive");
    }

    @Test
    void execute_unitCostRequired_whenNoStockOrProductCostAvailable() {
        acceptValues.put("WK-D-COST", "0");
        doReturn(true).when(stokf).isInvalidKey();
        prodf.getRecord().setDecimal("PR-LAST-COST", BigDecimal.ZERO);
        prodf.getRecord().setDecimal("PR-STD-COST", BigDecimal.ZERO);
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_FAIL_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_FAIL_SEQ, 1, ONE_LINE_FAIL_SEQ.length));

        service.execute();

        assertThat(msgLineHistory).contains("Unit cost required");
        assertThat(capturedWs().getWkLcnt()).isEqualTo(0);
    }

    @Test
    void execute_costDefaultsFromStockAverageCost_whenNotEntered() {
        acceptValues.put("WK-D-COST", "0");
        stokf.getRecord().setDecimal("SK-AVG-COST", BigDecimal.valueOf(30));
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        assertThat(capturedWs().getWlCost(1)).isEqualByComparingTo(BigDecimal.valueOf(30));
    }

    @Test
    void execute_costDefaultsFromProductLastCost_whenStockNotFound() {
        acceptValues.put("WK-D-COST", "0");
        doReturn(true).when(stokf).isInvalidKey();
        prodf.getRecord().setDecimal("PR-LAST-COST", BigDecimal.valueOf(18));
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        assertThat(capturedWs().getWlCost(1)).isEqualByComparingTo(BigDecimal.valueOf(18));
    }

    @Test
    void execute_costDefaultsFromProductStdCost_whenLastCostZero() {
        acceptValues.put("WK-D-COST", "0");
        doReturn(true).when(stokf).isInvalidKey();
        prodf.getRecord().setDecimal("PR-LAST-COST", BigDecimal.ZERO);
        prodf.getRecord().setDecimal("PR-STD-COST", BigDecimal.valueOf(12));
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        assertThat(capturedWs().getWlCost(1)).isEqualByComparingTo(BigDecimal.valueOf(12));
    }

    @Test
    void execute_returnAmountRoundedHalfUpFromQtyTimesCost() {
        acceptValues.put("WK-D-QTY", "3");
        acceptValues.put("WK-D-COST", "2.005");
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        // 3 * 2.005 = 6.015 -> HALF_UP to scale 0 -> 6.
        assertThat(capturedWs().getWlAmount(1)).isEqualTo(6L);
    }

    @Test
    void execute_stockWarning_whenReturnQtyExceedsOnHand_stillAddsLine() {
        acceptValues.put("WK-D-QTY", "500");
        stokf.getRecord().setDecimal("SK-ONHAND", BigDecimal.valueOf(100));
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        assertThat(msgLineHistory).contains("Warning: return qty exceeds on-hand stock");
        // WK-LCNT read at "Line added" — ADD-LINE runs unconditionally after CHECK-STOCK's
        // warning, before the terminating empty pass's CLRR-010 resets it back to 0.
        assertThat(snapshotAt("Line added").wkLcnt).isEqualTo(1);
    }

    @Test
    void execute_stockNotManaged_skipsAvailabilityCheck() {
        prodf.getRecord().setInt("PR-STOCK-MNG", 0);
        acceptValues.put("WK-D-QTY", "999999");
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        assertThat(msgLineHistory).doesNotContain("Warning: return qty exceeds on-hand stock");
        assertThat(capturedWs().getWlStkmng(1)).isEqualTo(0);
    }

    // ───────────────────────── confirm + save (ground truth: CSAV-010 / SRT-010)
    // ─────────────────────────

    @Test
    void execute_confirmNo_discardsReturnWithoutSaving() {
        acceptValues.put("WK-CONFIRM", "N");
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        assertThat(msgLineHistory).contains("Return discarded");
        verify(purhf, never()).write();
        verify(numgenService, never()).execute(any());
    }

    @Test
    void execute_taxcalStatusNotZero_fallsBackToZeroTaxGrossEqualsNet() {
        doAnswer(
                        inv -> {
                            TaxcalLinkParm p = inv.getArgument(0);
                            p.getKtax().setKtStatus("99");
                            return null;
                        })
                .when(taxcalService)
                .execute(any());
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        // Read at CSAV-010's "Review totals" display (right after COMPUTE-TAX-TOTAL), before
        // the terminating empty pass's CLRR-010 resets WK-NET/TAX/GRS-TOTAL back to 0 each —
        // which would make this assertion trivially true regardless of the fallback branch.
        Snapshot s = snapshotAt("Review totals then confirm return");
        assertThat(s.wkTaxTotal).isEqualTo(0);
        assertThat(s.wkGrsTotal).isEqualTo(s.wkNetTotal);
    }

    @Test
    void execute_numberAssignmentFails_showsMessageAndSkipsWrite() {
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("99");
                            return null;
                        })
                .when(numgenService)
                .execute(any());
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        assertThat(msgLineHistory).contains("Number assignment failed");
        verify(purhf, never()).write();
    }

    @Test
    void execute_purhfWriteInvalidKey_showsMessageAndSkipsDetails() {
        doReturn(true).when(purhf).isInvalidKey();
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        assertThat(msgLineHistory).contains("Header write failed");
        verify(purdf, never()).write();
        verify(aplf, never()).write();
    }

    @Test
    void execute_confirmYes_savesFullReturnChainWithNegativeAmounts() {
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        Pu0040FieldAccess ws = capturedWs();
        assertThat(msgLineHistory).contains("Purchase return posted");
        // VH-* (PURHF buffer) read at "Purchase return posted" — the terminating empty pass's
        // CLRR-010 re-runs INITIALIZE VH-REC right after, which would zero them all out.
        Snapshot s = snapshotAt("Purchase return posted");
        assertThat(s.vhNo).isEqualTo(9000000001L);
        assertThat(s.vhSupp).isEqualTo(100);
        assertThat(s.vhKind).isEqualTo(2);
        assertThat(s.vhLines).isEqualTo(1);
        assertThat(s.vhAmount).isEqualByComparingTo(BigDecimal.valueOf(-250));
        // VD-* (PURDF buffer) is untouched by CLRR-010 (only VH-REC is re-initialized), so it
        // is still safe to read straight off the live field accessor after execute() returns.
        assertThat(ws.getVdQty()).isEqualByComparingTo(BigDecimal.valueOf(-10));
        assertThat(ws.getVdAmount()).isEqualByComparingTo(BigDecimal.valueOf(-250));
        verify(purhf, times(1)).write();
        verify(purdf, times(1)).write();
        verify(aplf, times(1)).write();
        verify(suppf, times(1)).rewrite();
    }

    @Test
    void execute_confirmYes_postsApLedgerDebitAndReducesSupplierBalance() {
        suppf.getRecord().setDecimal("SP-BALANCE", BigDecimal.valueOf(1000));
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        Pu0040FieldAccess ws = capturedWs();
        // WK-GRS-TOTAL == net (no tax in happy-path TAXCAL stub) == 250; new balance = 1000 - 250.
        assertThat(ws.getPlDebit()).isEqualByComparingTo(BigDecimal.valueOf(250));
        assertThat(ws.getPlCredit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(ws.getPlBalance().longValue()).isEqualTo(750L);
        assertThat(ws.getPlKind()).isEqualTo(3);
        assertThat(ws.getPlRefType()).isEqualTo(23);
    }

    @Test
    void execute_apSupplierLookupFails_zeroesBalanceBeforeDebiting() {
        AtomicInteger suppReadCount = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            // 1st SUPPF read = VHDR-010 (must succeed so the header validates);
                            // 2nd SUPPF read = PAP-010 (force invalid so SP-BALANCE resets to 0).
                            return suppReadCount.getAndIncrement() > 0;
                        })
                .when(suppf)
                .isInvalidKey();
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        Pu0040FieldAccess ws = capturedWs();
        assertThat(ws.getPlBalance().longValue()).isEqualTo(-250L);
    }

    @Test
    void execute_aplfWriteInvalidKey_showsMessageButStillUpdatesSupplierBalance() {
        doReturn(true).when(aplf).isInvalidKey();
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        assertThat(msgLineHistory).contains("AP ledger write failed");
        verify(suppf, times(1)).rewrite();
    }

    // ───────────────────────── stock / movement posting (ground truth: DST-010 / WMV-010)
    // ─────────────────────────

    @Test
    void execute_stockRecordNotFound_createsNewStockRowWithNegativeOnhand() {
        doReturn(true).when(stokf).isInvalidKey();
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        verify(stokf, times(1)).write();
        verify(stokf, never()).rewrite();
        Pu0040FieldAccess ws = capturedWs();
        assertThat(ws.getSkOnhand()).isEqualByComparingTo(BigDecimal.valueOf(-10));
        verify(smovf, times(1)).write();
    }

    @Test
    void execute_stockRecordFound_decreasesOnhandViaRewrite() {
        stokf.getRecord().setDecimal("SK-ONHAND", BigDecimal.valueOf(100));
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        verify(stokf, times(1)).rewrite();
        verify(stokf, never()).write();
        Pu0040FieldAccess ws = capturedWs();
        assertThat(ws.getSkOnhand()).isEqualByComparingTo(BigDecimal.valueOf(90));
        assertThat(ws.getSmQty()).isEqualByComparingTo(BigDecimal.valueOf(-10));
        assertThat(ws.getSmKind()).isEqualTo(60);
    }

    @Test
    void execute_stkmovNumberAssignmentFails_skipsMovementWriteButKeepsStockUpdate() {
        AtomicInteger numgenCalls = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            boolean isStkmov = "STKMOV".equals(p.getKnum().getKnumKey().trim());
                            p.getKnum().setKnumStatus(isStkmov ? "99" : "00");
                            if (!isStkmov) {
                                p.getKnum()
                                        .setKnumNumber(numgenCalls.incrementAndGet() + 9000000000L);
                            }
                            return null;
                        })
                .when(numgenService)
                .execute(any());
        when(renderer.readEndStatus())
                .thenReturn(
                        ONE_LINE_SAVE_SEQ[0],
                        java.util.Arrays.copyOfRange(
                                ONE_LINE_SAVE_SEQ, 1, ONE_LINE_SAVE_SEQ.length));

        service.execute();

        verify(smovf, never()).write();
        verify(stokf, times(1)).rewrite();
    }
}

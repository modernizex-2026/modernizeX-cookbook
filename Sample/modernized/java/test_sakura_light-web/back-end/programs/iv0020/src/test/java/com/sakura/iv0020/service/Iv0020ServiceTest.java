package com.sakura.iv0020.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.iv0020.domain.Iv0020FieldAccess;
import com.sakura.iv0020.runtime.Iv0020Datasets;
import com.sakura.numgen.service.NumgenService;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ScreenModels.ScreenDef;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.SmovfDataset;
import com.sakura.runtime.io.StokfDataset;
import com.sakura.runtime.io.WhsefDataset;
import com.sakura.runtime.linkage.DateutLinkParm;
import com.sakura.runtime.linkage.NumgenLinkParm;
import com.sakura.runtime.record.RuntimeFieldAccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for Iv0020Service (COBOL IV0020 — stock adjustment), generated from {@code IV0020.cob}
 * PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: STOKF/SMOVF/PRODF/WHSEF are real dataset objects wrapped with {@code spy()} so
 * the record buffer (and therefore {@code Iv0020FieldAccess}, which registers those buffers) works
 * exactly as in production; only the I/O methods
 * (open/close/write/rewrite/readByKey/getFileStatus/isInvalidKey) are stubbed so no real file/DB
 * access happens. DATEUT/NUMGEN/ABORTX are mocked so the injected date and generated sequence
 * number are deterministic.
 *
 * <p>Review of IV0020.cob against Iv0020Service found no CONVERT-GAP: every branch (file-open
 * retry/abend, master validation, new-vs-existing stock, adjustment validation, Y/y confirm, WRITE
 * vs REWRITE, movement numbering, and the COBOL quirk where WRITE-MOVEMENT's own failure message
 * does not stop POST-ADJ from also showing "Adjustment posted") is reproduced faithfully.
 *
 * <p>STOKF's buffer is cleared by CLRW-010 (resetWorkAreaFields) at the *start* of every loop cycle
 * — including the terminating cycle every test needs (PF3 ends the program only on the key prompt
 * of the NEXT cycle). So both the "existing stock" seed values and the post-{@code execute()}
 * buffer state must route around that: seed values are populated inside a {@code readByKey} answer
 * (so they land AFTER the cycle's reset, like a real read would), and the values just before
 * write()/rewrite() are captured into {@link #stockWriteSnapshot} before the next cycle's reset can
 * wipe them.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Iv0020ServiceTest {

    @Mock private ScreenRendererInstance renderer;
    @Mock private DateutService dateutService;
    @Mock private NumgenService numgenService;
    @Mock private AbortxService abortxService;

    private Iv0020Datasets fileSet;
    private StokfDataset stokf;
    private SmovfDataset smovf;
    private ProdfDataset prodf;
    private WhsefDataset whsef;

    private Iv0020Service service;

    private final Map<String, String> acceptValues = new HashMap<>();
    private final List<String> screenInteractions = new ArrayList<>();
    private final List<String> msgLineHistory = new ArrayList<>();
    private final Map<String, Object> stockWriteSnapshot = new LinkedHashMap<>();

    @BeforeEach
    void setUp() {
        Iv0020Datasets real = new Iv0020Datasets();
        fileSet = spy(real);
        stokf = spy(real.getStokf());
        smovf = spy(real.getSmovf());
        prodf = spy(real.getProdf());
        whsef = spy(real.getWhsef());
        doReturn(stokf).when(fileSet).getStokf();
        doReturn(smovf).when(fileSet).getSmovf();
        doReturn(prodf).when(fileSet).getProdf();
        doReturn(whsef).when(fileSet).getWhsef();

        doNothing().when(stokf).open(any());
        doNothing().when(smovf).open(any());
        doNothing().when(prodf).open(any());
        doNothing().when(whsef).open(any());
        doNothing().when(stokf).close();
        doNothing().when(smovf).close();
        doNothing().when(prodf).close();
        doNothing().when(whsef).close();
        // Captures the SK-* buffer at the instant of WRITE/REWRITE — before the next
        // cycle's CLRW-010 reset wipes it (see class javadoc).
        doAnswer(
                        inv -> {
                            stockWriteSnapshot.put(
                                    "SK-ONHAND", stokf.getRecord().getDecimal("SK-ONHAND"));
                            stockWriteSnapshot.put(
                                    "SK-AVG-COST", stokf.getRecord().getDecimal("SK-AVG-COST"));
                            stockWriteSnapshot.put(
                                    "SK-LAST-IN-DATE", stokf.getRecord().getInt("SK-LAST-IN-DATE"));
                            stockWriteSnapshot.put(
                                    "SK-LAST-OUT-DATE",
                                    stokf.getRecord().getInt("SK-LAST-OUT-DATE"));
                            stockWriteSnapshot.put(
                                    "SK-YTD-IN", stokf.getRecord().getDecimal("SK-YTD-IN"));
                            stockWriteSnapshot.put(
                                    "SK-YTD-OUT", stokf.getRecord().getDecimal("SK-YTD-OUT"));
                            return null;
                        })
                .when(stokf)
                .write();
        doAnswer(
                        inv -> {
                            stockWriteSnapshot.put(
                                    "SK-ONHAND", stokf.getRecord().getDecimal("SK-ONHAND"));
                            stockWriteSnapshot.put(
                                    "SK-AVG-COST", stokf.getRecord().getDecimal("SK-AVG-COST"));
                            stockWriteSnapshot.put(
                                    "SK-LAST-IN-DATE", stokf.getRecord().getInt("SK-LAST-IN-DATE"));
                            stockWriteSnapshot.put(
                                    "SK-LAST-OUT-DATE",
                                    stokf.getRecord().getInt("SK-LAST-OUT-DATE"));
                            stockWriteSnapshot.put(
                                    "SK-YTD-IN", stokf.getRecord().getDecimal("SK-YTD-IN"));
                            stockWriteSnapshot.put(
                                    "SK-YTD-OUT", stokf.getRecord().getDecimal("SK-YTD-OUT"));
                            return null;
                        })
                .when(stokf)
                .rewrite();

        // Default: product found, active, std cost 5.00.
        doReturn(true).when(prodf).readByKey(any());
        doReturn(false).when(prodf).isInvalidKey();
        prodf.getRecord().setInt("PR-DEL-FLAG", 0);
        prodf.getRecord().setString("PR-NAME", "Widget");
        prodf.getRecord().setDecimal("PR-STD-COST", new BigDecimal("5.00"));

        // Default: warehouse found, active.
        doReturn(true).when(whsef).readByKey(any());
        doReturn(false).when(whsef).isInvalidKey();
        whsef.getRecord().setInt("WH-DEL-FLAG", 0);
        whsef.getRecord().setString("WH-NAME", "Main WH");

        // Default: existing stock record found — on-hand 100, avg cost 8.50. Values
        // are populated INSIDE the readByKey answer (simulating a real read) because
        // CLRW-010 clears the SK-REC buffer before every READ-STOCK, including this
        // one — setting them here in setUp() would be wiped before the first read.
        doAnswer(
                        inv -> {
                            stokf.getRecord().setDecimal("SK-ONHAND", new BigDecimal("100"));
                            stokf.getRecord().setDecimal("SK-AVG-COST", new BigDecimal("8.50"));
                            stokf.getRecord().setDecimal("SK-YTD-IN", BigDecimal.ZERO);
                            stokf.getRecord().setDecimal("SK-YTD-OUT", BigDecimal.ZERO);
                            return true;
                        })
                .when(stokf)
                .readByKey(any());
        doReturn(false).when(stokf).isInvalidKey();

        acceptValues.put("WK-KEY-PROD", "100");
        acceptValues.put("WK-KEY-WHSE", "1");
        acceptValues.put("WK-ADJ-QTY", "10");
        acceptValues.put("WK-REASON", "Recount");
        acceptValues.put("SC-CONF", "Y");

        when(renderer.acceptField(any()))
                .thenAnswer(
                        inv -> {
                            var field =
                                    inv.getArgument(
                                            0, com.sakura.runtime.ScreenModels.InputFieldDef.class);
                            return acceptValues.getOrDefault(field.name, "");
                        });

        doAnswer(
                        inv -> {
                            ScreenDef def = inv.getArgument(0);
                            RuntimeFieldAccess w = inv.getArgument(1);
                            screenInteractions.add("displayScreen:" + def.name);
                            if (w instanceof Iv0020FieldAccess a) {
                                msgLineHistory.add(a.getWkMsgLine().trim());
                            }
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        // DATEUT: fixed system date 2026-03-15.
        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            p.getKdate().setKdDate1(20260315);
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        // NUMGEN: succeeds by default, assigns 5001.
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("00");
                            p.getKnum().setKnumNumber(5001L);
                            return null;
                        })
                .when(numgenService)
                .execute(any());

        service = new Iv0020Service(fileSet, dateutService, numgenService, abortxService, renderer);
    }

    /**
     * Live handle onto the service's WorkingStorage/FD field accessor, obtained from the first
     * displayScreen() call.
     */
    private Iv0020FieldAccess capturedWs() {
        ArgumentCaptor<RuntimeFieldAccess> captor =
                ArgumentCaptor.forClass(RuntimeFieldAccess.class);
        verify(renderer, org.mockito.Mockito.atLeastOnce()).displayScreen(any(), captor.capture());
        return (Iv0020FieldAccess) captor.getAllValues().get(0);
    }

    // ───────────────────────── happy path ─────────────────────────

    @Test
    void execute_positiveAdjustmentOnExistingStock_rewritesStockAndPostsMovement() {
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(stokf, times(1)).rewrite();
        verify(stokf, never()).write();
        verify(smovf, times(1)).write();
        assertThat((BigDecimal) stockWriteSnapshot.get("SK-ONHAND"))
                .isEqualByComparingTo(new BigDecimal("110"));
        assertThat(stockWriteSnapshot.get("SK-LAST-IN-DATE")).isEqualTo(20260315);
        assertThat((BigDecimal) stockWriteSnapshot.get("SK-YTD-IN"))
                .isEqualByComparingTo(new BigDecimal("10"));
        assertThat(smovf.getRecord().getLong("SM-SEQ")).isEqualTo(5001L);
        assertThat(smovf.getRecord().getInt("SM-PROD")).isEqualTo(100);
        assertThat(smovf.getRecord().getInt("SM-WHSE")).isEqualTo(1);
        assertThat(smovf.getRecord().getInt("SM-KIND")).isEqualTo(30);
        assertThat(smovf.getRecord().getInt("SM-REF-TYPE")).isEqualTo(30);
        assertThat(smovf.getRecord().getDecimal("SM-QTY"))
                .isEqualByComparingTo(new BigDecimal("10"));
        assertThat(smovf.getRecord().getDecimal("SM-UNIT-COST"))
                .isEqualByComparingTo(new BigDecimal("8.50"));
        assertThat(smovf.getRecord().getDecimal("SM-BAL-AFTER"))
                .isEqualByComparingTo(new BigDecimal("110"));
        assertThat(msgLineHistory).contains("Stock found - enter adjustment", "Adjustment posted");
        assertThat(capturedWs().getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_negativeAdjustmentOnExistingStock_updatesYtdOutAndLastOutDate() {
        acceptValues.put("WK-ADJ-QTY", "-30");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(stokf, times(1)).rewrite();
        assertThat((BigDecimal) stockWriteSnapshot.get("SK-ONHAND"))
                .isEqualByComparingTo(new BigDecimal("70"));
        assertThat(stockWriteSnapshot.get("SK-LAST-OUT-DATE")).isEqualTo(20260315);
        assertThat((BigDecimal) stockWriteSnapshot.get("SK-YTD-OUT"))
                .isEqualByComparingTo(new BigDecimal("30"));
        assertThat(msgLineHistory).contains("Adjustment posted");
    }

    @Test
    void execute_noExistingStockRecord_writesNewStockWithStandardCost() {
        // READ-STOCK checks isInvalidKey() twice (new-vs-existing decision, then the
        // STK-FLG assignment) — both must agree "not found"; the third call is the
        // post-WRITE check (write succeeds).
        doReturn(true, true, false).when(stokf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(stokf, times(1)).write();
        verify(stokf, never()).rewrite();
        assertThat((BigDecimal) stockWriteSnapshot.get("SK-AVG-COST"))
                .isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat((BigDecimal) stockWriteSnapshot.get("SK-ONHAND"))
                .isEqualByComparingTo(new BigDecimal("10"));
        assertThat(msgLineHistory)
                .contains("No stock record - will be created", "Adjustment posted");
    }

    @Test
    void execute_confirmLowercaseY_stillPostsAdjustment() {
        acceptValues.put("SC-CONF", "y");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(stokf, times(1)).rewrite();
        verify(smovf, times(1)).write();
        assertThat(msgLineHistory).contains("Adjustment posted");
    }

    // ───────────────────────── main loop / key screen ─────────────────────────

    @Test
    void execute_pf3AtFirstKeyPrompt_endsImmediatelyWithoutProcessing() {
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(stokf, never()).rewrite();
        verify(stokf, never()).write();
        verify(prodf, never()).readByKey(any());
        assertThat(capturedWs().getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_invalidFunctionKeyAtKeyPrompt_showsInvalidKeyMessageAndContinues() {
        when(renderer.readEndStatus()).thenReturn("09", "03");

        service.execute();

        verify(prodf, never()).readByKey(any());
        assertThat(msgLineHistory).contains("Invalid function key");
    }

    @Test
    void execute_productCodeZero_rejectsWithoutValidatingMasters() {
        acceptValues.put("WK-KEY-PROD", "0");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(prodf, never()).readByKey(any());
        verify(stokf, never()).rewrite();
        assertThat(msgLineHistory).contains("Product code must not be zero");
    }

    // ───────────────────────── master validation (ground truth: VALIDATE-MASTERS)
    // ─────────────────────────

    @Test
    void execute_productNotFound_rejectsWithNotFoundMessage() {
        doReturn(true).when(prodf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(whsef, never()).readByKey(any());
        verify(stokf, never()).rewrite();
        assertThat(msgLineHistory).contains("Product not found");
    }

    @Test
    void execute_productDeleted_rejectsWithDeletedMessage() {
        prodf.getRecord().setInt("PR-DEL-FLAG", 1);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(whsef, never()).readByKey(any());
        assertThat(msgLineHistory).contains("Product is deleted");
    }

    @Test
    void execute_warehouseNotFound_rejectsWithNotFoundMessage() {
        doReturn(true).when(whsef).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(stokf, never()).readByKey(any());
        assertThat(msgLineHistory).contains("Warehouse not found");
    }

    @Test
    void execute_warehouseDeleted_rejectsWithDeletedMessage() {
        whsef.getRecord().setInt("WH-DEL-FLAG", 1);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(stokf, never()).readByKey(any());
        assertThat(msgLineHistory).contains("Warehouse is deleted");
    }

    // ───────────────────────── adjustment screen dispatch (ground truth: ENTER-ADJ)
    // ─────────────────────────

    @Test
    void execute_pf3AtAdjustmentPrompt_continuesWithoutMessage() {
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        verify(stokf, never()).rewrite();
        assertThat(msgLineHistory).doesNotContain("Cancelled");
    }

    @Test
    void execute_pf4ClearAtAdjustmentPrompt_showsCancelledMessage() {
        when(renderer.readEndStatus()).thenReturn("00", "04", "03");

        service.execute();

        verify(stokf, never()).rewrite();
        assertThat(msgLineHistory).contains("Cancelled");
    }

    @Test
    void execute_invalidFunctionKeyAtAdjustmentPrompt_showsInvalidKeyMessage() {
        when(renderer.readEndStatus()).thenReturn("00", "99", "03");

        service.execute();

        verify(stokf, never()).rewrite();
        assertThat(msgLineHistory).contains("Invalid function key");
    }

    // ───────────────────────── adjustment validation (ground truth: VALIDATE-ADJ)
    // ─────────────────────────

    @Test
    void execute_adjustmentQtyZero_rejectsWithQtyRequiredMessage() {
        acceptValues.put("WK-ADJ-QTY", "0");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(stokf, never()).rewrite();
        assertThat(msgLineHistory).contains("Adjustment quantity must not be zero");
    }

    @Test
    void execute_negativeResultNotAllowed_rejectsWithNegativeMessage() {
        acceptValues.put("WK-ADJ-QTY", "-150");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(stokf, never()).rewrite();
        assertThat(msgLineHistory).contains("Result would be negative - not allowed");
    }

    @Test
    void execute_reasonBlank_rejectsWithReasonRequiredMessage() {
        acceptValues.put("WK-REASON", " ");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(stokf, never()).rewrite();
        assertThat(msgLineHistory).contains("Reason is required");
    }

    @Test
    void execute_confirmDeclined_discardsAdjustmentWithoutSaving() {
        acceptValues.put("SC-CONF", "N");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(stokf, never()).rewrite();
        verify(stokf, never()).write();
        verify(smovf, never()).write();
        assertThat(msgLineHistory).contains("Adjustment cancelled");
    }

    // ───────────────────────── save-path failures (ground truth: POST-ADJ / WRITE-MOVEMENT)
    // ─────────────────────────

    @Test
    void execute_stockRewriteFails_showsUpdateFailedAndSkipsMovement() {
        // READ-STOCK checks isInvalidKey() twice (new-vs-existing decision, then the
        // STK-FLG assignment) — both must agree "found existing"; the third call is
        // the post-REWRITE check (fails).
        doReturn(false, false, true).when(stokf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(stokf, times(1)).rewrite();
        verify(smovf, never()).write();
        assertThat(msgLineHistory).contains("Stock update failed");
        assertThat(msgLineHistory).doesNotContain("Adjustment posted");
    }

    @Test
    void execute_stockWriteFailsForNewStock_showsWriteFailedAndSkipsMovement() {
        // READ-STOCK's isInvalidKey() must be true (no stock -> new record); the
        // post-WRITE check also reports invalid (write failed).
        doReturn(true, true).when(stokf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(stokf, times(1)).write();
        verify(smovf, never()).write();
        assertThat(msgLineHistory).contains("Stock write failed");
        assertThat(msgLineHistory).doesNotContain("Adjustment posted");
    }

    @Test
    void execute_numgenMovementNumberFails_stockStillUpdatedButNoMovementWritten() {
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("99");
                            return null;
                        })
                .when(numgenService)
                .execute(any());
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(stokf, times(1)).rewrite();
        verify(smovf, never()).write();
        assertThat(msgLineHistory).contains("Movement number assignment failed");
    }

    /**
     * CONVERT-GAP CHECK (none found): COBOL PADJ-010 runs PERFORM WRITE-MOVEMENT then
     * unconditionally MOVEs "Adjustment posted" and DISPLAYs DS-MSG next — even when
     * WRITE-MOVEMENT's own WRITE SM-REC failed and already displayed "Movement write failed".
     * Iv0020Service.postStockAdjustment() mirrors this exactly: it calls writeStockMovementRecord()
     * via runChain with no early-return check, so both messages appear in sequence. This test locks
     * that faithful (if surprising) behavior in place.
     */
    @Test
    void execute_movementWriteFails_showsBothFailureAndPostedMessages() {
        doReturn(true).when(smovf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(stokf, times(1)).rewrite();
        verify(smovf, times(1)).write();
        assertThat(msgLineHistory).containsSequence("Movement write failed", "Adjustment posted");
    }

    // ───────────────────────── file open lifecycle (ground truth: OPEN-FILES / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_stokfOpenFailsPersistently_abortsWithCompletionCode255() {
        doReturn("99").when(stokf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(smovf, never()).open(any());
        verify(prodf, never()).open(any());
        verify(whsef, never()).open(any());
    }

    @Test
    void execute_stokfOpenStatus35_reopensAsOutputThenReopensIo() {
        doReturn("35", "00", "00", "00").when(stokf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(stokf, times(3)).open(any());
        verify(stokf, times(1)).open(FileOpenMode.OUTPUT);
        verify(stokf, times(2)).open(FileOpenMode.IO);
        verify(stokf, times(2)).close();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_prodfOpenFailsPersistently_doesNotAbend() {
        // PRODF/WHSEF have no ABEND-RTN guard in COBOL (only STOKF/SMOVF do) —
        // a persistent open failure on PRODF must NOT abort the program.
        doReturn("99").when(prodf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(abortxService, never()).execute(any());
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }
}

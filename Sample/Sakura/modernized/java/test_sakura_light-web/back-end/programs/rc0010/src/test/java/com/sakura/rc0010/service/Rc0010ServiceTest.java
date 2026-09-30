package com.sakura.rc0010.service;

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
import com.sakura.numgen.service.NumgenService;
import com.sakura.rc0010.domain.Rc0010FieldAccess;
import com.sakura.rc0010.runtime.Rc0010Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ScreenModels.ScreenDef;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.PodfDataset;
import com.sakura.runtime.io.PohfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.RcvdfDataset;
import com.sakura.runtime.io.RcvhfDataset;
import com.sakura.runtime.io.SmovfDataset;
import com.sakura.runtime.io.StokfDataset;
import com.sakura.runtime.io.SuppfDataset;
import com.sakura.runtime.linkage.NumgenLinkParm;
import com.sakura.runtime.record.RuntimeFieldAccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for Rc0010Service (COBOL RC0010 — goods receiving entry), generated from {@code
 * RC0010.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Datasets (POHF/PODF/RCVHF/RCVDF/STOKF/SMOVF/SUPPF/PRODF) are real objects wrapped with {@code
 * spy()} so record buffers (and therefore {@code Rc0010FieldAccess}, which registers those buffers)
 * behave exactly as in production; only I/O methods are stubbed. DATEUT runs for real; NUMGEN and
 * ABORTX are mocked.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Rc0010ServiceTest {

    @Mock private ScreenRendererInstance renderer;
    @Mock private NumgenService numgenService;
    @Mock private AbortxService abortxService;

    private DateutService dateutService;

    private Rc0010Datasets fileSet;
    private PohfDataset pohf;
    private PodfDataset podf;
    private RcvhfDataset rcvhf;
    private RcvdfDataset rcvdf;
    private StokfDataset stokf;
    private SmovfDataset smovf;
    private SuppfDataset suppf;
    private ProdfDataset prodf;

    private Rc0010Service service;

    private final Map<String, String> acceptValues = new HashMap<>();
    private final List<String> screenInteractions = new ArrayList<>();
    private final List<String> msgLineHistory = new ArrayList<>();

    /**
     * WK-SUPP-NAME trimmed at every displayScreen() call — CLRR-010 resets it to space at the top
     * of the next MAIN-010 cycle, so the final ws state does not reflect it.
     */
    private final List<String> suppNameHistory = new ArrayList<>();

    /** PODF NEXT-read sequence used by loadPoLines / scanPoLinesForOutstanding. */
    private List<Map<String, Object>> podLines;

    private int podReadIdx;

    @BeforeEach
    void setUp() {
        dateutService = new DateutService();

        Rc0010Datasets real = new Rc0010Datasets();
        fileSet = spy(real);
        pohf = spy(real.getPohf());
        podf = spy(real.getPodf());
        rcvhf = spy(real.getRcvhf());
        rcvdf = spy(real.getRcvdf());
        stokf = spy(real.getStokf());
        smovf = spy(real.getSmovf());
        suppf = spy(real.getSuppf());
        prodf = spy(real.getProdf());
        doReturn(pohf).when(fileSet).getPohf();
        doReturn(podf).when(fileSet).getPodf();
        doReturn(rcvhf).when(fileSet).getRcvhf();
        doReturn(rcvdf).when(fileSet).getRcvdf();
        doReturn(stokf).when(fileSet).getStokf();
        doReturn(smovf).when(fileSet).getSmovf();
        doReturn(suppf).when(fileSet).getSuppf();
        doReturn(prodf).when(fileSet).getProdf();

        for (RawDatasetLike f :
                new RawDatasetLike[] {
                    w(pohf), w(podf), w(rcvhf), w(rcvdf), w(stokf), w(smovf), w(suppf), w(prodf)
                }) {
            f.stubIo();
        }

        // PO header default: found, active, part-received status.
        doReturn(true).when(pohf).readByKey(any());
        doReturn(false).when(pohf).isInvalidKey();
        pohf.getRecord().setLong("PH-NO", 1000L);
        pohf.getRecord().setInt("PH-DEL-FLAG", 0);
        pohf.getRecord().setInt("PH-STATUS", 1);
        pohf.getRecord().setInt("PH-SUPP", 100);
        pohf.getRecord().setInt("PH-WHSE", 1);

        // Supplier default: found.
        doReturn(true).when(suppf).readByKey(any());
        doReturn(false).when(suppf).isInvalidKey();
        suppf.getRecord().setString("SP-NAME", "Acme Supplier");

        // PODF outstanding line list (START/READ NEXT emulation).
        podLines = new ArrayList<>();
        podLines.add(poLine(1, 5001, 1, 1, 30, new BigDecimal("10.00"), 0));
        podReadIdx = 0;
        // COBOL performs a fresh START + READ-NEXT pass twice per receiving (once in
        // LOAD-PO-LINES, again in UPDATE-PO-STATUS) — reset the cursor on every START.
        doAnswer(
                        inv -> {
                            podReadIdx = 0;
                            return true;
                        })
                .when(podf)
                .start(any(), any());
        doAnswer(
                        inv -> {
                            if (podReadIdx >= podLines.size()) {
                                doReturn(true).when(podf).isAtEnd();
                                return false;
                            }
                            Map<String, Object> row = podLines.get(podReadIdx++);
                            podf.getRecord().setLong("PD-NO", (Long) row.get("PD-NO"));
                            podf.getRecord().setInt("PD-LINE", (Integer) row.get("PD-LINE"));
                            podf.getRecord().setInt("PD-PROD", (Integer) row.get("PD-PROD"));
                            podf.getRecord().setInt("PD-WHSE", (Integer) row.get("PD-WHSE"));
                            podf.getRecord().setInt("PD-STATUS", (Integer) row.get("PD-STATUS"));
                            podf.getRecord().setDecimal("PD-QTY", (BigDecimal) row.get("PD-QTY"));
                            podf.getRecord()
                                    .setDecimal("PD-RECV-QTY", (BigDecimal) row.get("PD-RECV-QTY"));
                            podf.getRecord()
                                    .setDecimal(
                                            "PD-UNIT-COST", (BigDecimal) row.get("PD-UNIT-COST"));
                            doReturn(false).when(podf).isAtEnd();
                            return true;
                        })
                .when(podf)
                .readNext();
        doReturn(true).when(podf).readByKey(any());
        doReturn(false).when(podf).isInvalidKey();
        // Persist UPDATE-PO-LINE's REWRITE back into the row list so a later rescan
        // (UPDATE-PO-STATUS) observes the just-posted PD-RECV-QTY / PD-STATUS.
        doAnswer(
                        inv -> {
                            int line = podf.getRecord().getInt("PD-LINE");
                            for (Map<String, Object> row : podLines) {
                                if (((Integer) row.get("PD-LINE")).intValue() == line) {
                                    row.put(
                                            "PD-RECV-QTY",
                                            podf.getRecord().getDecimal("PD-RECV-QTY"));
                                    row.put("PD-STATUS", podf.getRecord().getInt("PD-STATUS"));
                                }
                            }
                            return null;
                        })
                .when(podf)
                .rewrite();

        // Product default: found, stock-managed.
        doReturn(true).when(prodf).readByKey(any());
        doReturn(false).when(prodf).isInvalidKey();
        prodf.getRecord().setString("PR-NAME", "Widget");
        prodf.getRecord().setInt("PR-STOCK-MNG", 1);

        // Stock default: not found (creates a new stock record).
        doReturn(false).when(stokf).readByKey(any());
        doReturn(true).when(stokf).isInvalidKey();

        acceptValues.put("WK-PO-KEY", "1000");
        acceptValues.put("RH-DATE", "20260315");
        acceptValues.put("WK-D-RECV", "30");
        acceptValues.put("WK-CONFIRM", "Y");

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
                            if (w instanceof Rc0010FieldAccess a) {
                                msgLineHistory.add(a.getWkMsgLine().trim());
                                suppNameHistory.add(a.getWkSuppName().trim());
                            }
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("00");
                            p.getKnum().setKnumNumber(9001L);
                            return null;
                        })
                .when(numgenService)
                .execute(any());

        service = new Rc0010Service(fileSet, dateutService, numgenService, abortxService, renderer);
    }

    private static Map<String, Object> poLine(
            int line, int prod, int whse, int status, int qty, BigDecimal unitCost, int recvQty) {
        Map<String, Object> m = new HashMap<>();
        m.put("PD-NO", 1000L);
        m.put("PD-LINE", line);
        m.put("PD-PROD", prod);
        m.put("PD-WHSE", whse);
        m.put("PD-STATUS", status);
        m.put("PD-QTY", new BigDecimal(qty));
        m.put("PD-RECV-QTY", new BigDecimal(recvQty));
        m.put("PD-UNIT-COST", unitCost);
        return m;
    }

    /** Common no-op stub bundle shared by all eight FD files. */
    private interface RawDatasetLike {
        void stubIo();
    }

    private RawDatasetLike w(com.sakura.runtime.record.RawDatasetBase file) {
        return () -> {
            doNothing().when(file).open(any());
            doNothing().when(file).close();
            doReturn("00").when(file).getFileStatus();
        };
    }

    private Rc0010FieldAccess capturedWs() {
        org.mockito.ArgumentCaptor<RuntimeFieldAccess> captor =
                org.mockito.ArgumentCaptor.forClass(RuntimeFieldAccess.class);
        verify(renderer, org.mockito.Mockito.atLeastOnce()).displayScreen(any(), captor.capture());
        return (Rc0010FieldAccess) captor.getAllValues().get(0);
    }

    // ───────────────────────── happy path ─────────────────────────

    @Test
    void execute_receiveFullOutstandingQty_savesHeaderDetailStockAndMovement() {
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        verify(rcvhf, times(1)).write();
        verify(rcvdf, times(1)).write();
        verify(stokf, times(1)).write();
        verify(smovf, times(1)).write();
        verify(podf, times(1)).rewrite();
        verify(pohf, times(1)).rewrite();

        assertThat(rcvhf.getRecord().getLong("RH-NO")).isEqualTo(9001L);
        assertThat(rcvhf.getRecord().getLong("RH-PO")).isEqualTo(1000L);
        assertThat(rcvhf.getRecord().getInt("RH-SUPP")).isEqualTo(100);
        assertThat(rcvhf.getRecord().getInt("RH-LINES")).isEqualTo(1);

        assertThat(rcvdf.getRecord().getInt("RD-PROD")).isEqualTo(5001);
        assertThat(rcvdf.getRecord().getDecimal("RD-QTY"))
                .isEqualByComparingTo(new BigDecimal("30"));
        assertThat(rcvdf.getRecord().getDecimal("RD-UNIT-COST"))
                .isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(rcvdf.getRecord().getDecimal("RD-AMOUNT"))
                .isEqualByComparingTo(new BigDecimal("300"));

        assertThat(stokf.getRecord().getDecimal("SK-ONHAND"))
                .isEqualByComparingTo(new BigDecimal("30"));
        assertThat(stokf.getRecord().getDecimal("SK-ON-ORDER"))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(stokf.getRecord().getDecimal("SK-AVG-COST"))
                .isEqualByComparingTo(new BigDecimal("10.00"));

        assertThat(smovf.getRecord().getInt("SM-KIND")).isEqualTo(20);
        assertThat(smovf.getRecord().getInt("SM-REF-TYPE")).isEqualTo(22);
        assertThat(smovf.getRecord().getDecimal("SM-QTY"))
                .isEqualByComparingTo(new BigDecimal("30"));

        assertThat(podf.getRecord().getInt("PD-STATUS")).isEqualTo(2);
        assertThat(pohf.getRecord().getInt("PH-STATUS")).isEqualTo(2);

        assertThat(msgLineHistory).contains("Receiving posted");
        assertThat(screenInteractions)
                .contains("displayScreen:DS-CONFIRM", "displayScreen:DS-LINE");
    }

    @Test
    void execute_receivePartialQty_leavesPoLineAndHeaderStatusAsPartial() {
        acceptValues.put("WK-D-RECV", "10");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        assertThat(podf.getRecord().getInt("PD-STATUS")).isEqualTo(1);
        assertThat(pohf.getRecord().getInt("PH-STATUS")).isEqualTo(1);
        assertThat(msgLineHistory).contains("Receiving posted");
    }

    @Test
    void execute_productNotStockManaged_skipsStockAndMovementButPostsDetail() {
        prodf.getRecord().setInt("PR-STOCK-MNG", 0);
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        verify(rcvdf, times(1)).write();
        verify(stokf, never()).write();
        verify(smovf, never()).write();
    }

    @Test
    void execute_existingStockRecord_recomputesWeightedAverageCost() {
        doReturn(true).when(stokf).readByKey(any());
        doReturn(false).when(stokf).isInvalidKey();
        stokf.getRecord().setDecimal("SK-ONHAND", new BigDecimal("70"));
        stokf.getRecord().setDecimal("SK-AVG-COST", new BigDecimal("20.00"));
        stokf.getRecord().setDecimal("SK-ON-ORDER", new BigDecimal("30"));
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        // (70*20 + 30*10) / 100 = 17.00
        verify(stokf, times(1)).rewrite();
        // REWRITE has no seek target for an unopened stream-mode file (never really
        // opened in this test), so its "plain stream path" defers to write() — a
        // single indirect call, not a second independent write.
        verify(stokf, times(1)).write();
        assertThat(stokf.getRecord().getDecimal("SK-AVG-COST"))
                .isEqualByComparingTo(new BigDecimal("17.00"));
        assertThat(stokf.getRecord().getDecimal("SK-ONHAND"))
                .isEqualByComparingTo(new BigDecimal("100"));
        assertThat(stokf.getRecord().getDecimal("SK-ON-ORDER"))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void execute_confirmLowercaseY_stillSavesReceipt() {
        acceptValues.put("WK-CONFIRM", "y");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        verify(rcvhf, times(1)).write();
        assertThat(msgLineHistory).contains("Receiving posted");
    }

    // ───────────────────────── PO acceptance validation (PPO-010) ─────────────────────────

    @Test
    void execute_pf3AtKeyPrompt_endsImmediatelyWithoutTouchingAnyFile() {
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(pohf, never()).readByKey(any());
        verify(rcvhf, never()).write();
        assertThat(capturedWs().getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_invalidFunctionKey_showsInvalidKeyMessageAndLoopsAgain() {
        when(renderer.readEndStatus()).thenReturn("09", "03");

        service.execute();

        verify(pohf, never()).readByKey(any());
        assertThat(msgLineHistory).contains("Invalid function key");
    }

    @Test
    void execute_poKeyZero_rejectsWithPoNumberRequiredMessage() {
        acceptValues.put("WK-PO-KEY", "0");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(pohf, never()).readByKey(any());
        assertThat(msgLineHistory).contains("PO number required");
    }

    @Test
    void execute_poNotFound_rejectsWithPoNotFoundMessage() {
        doReturn(true).when(pohf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(rcvhf, never()).write();
        assertThat(msgLineHistory).contains("PO not found");
    }

    @Test
    void execute_poDeleted_rejectsWithPoIsDeletedMessage() {
        pohf.getRecord().setInt("PH-DEL-FLAG", 1);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(rcvhf, never()).write();
        assertThat(msgLineHistory).contains("PO is deleted");
    }

    @Test
    void execute_poCancelled_rejectsWithPoIsCancelledMessage() {
        pohf.getRecord().setInt("PH-STATUS", 9);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(rcvhf, never()).write();
        assertThat(msgLineHistory).contains("PO is cancelled");
    }

    @Test
    void execute_poFullyReceivedStatus2_rejectsWithAlreadyReceivedMessage() {
        pohf.getRecord().setInt("PH-STATUS", 2);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(rcvhf, never()).write();
        assertThat(msgLineHistory).contains("PO already fully received");
    }

    @Test
    void execute_poFullyReceivedStatus3_rejectsWithAlreadyReceivedMessage() {
        pohf.getRecord().setInt("PH-STATUS", 3);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(rcvhf, never()).write();
        assertThat(msgLineHistory).contains("PO already fully received");
    }

    @Test
    void execute_supplierNotFound_showsUnknownSupplierButStillProceeds() {
        doReturn(true).when(suppf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        assertThat(suppNameHistory).contains("??? unknown supplier");
        verify(rcvhf, times(1)).write();
    }

    @Test
    void execute_noOutstandingLines_rejectsWithNoOutstandingLinesMessage() {
        podLines.clear();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(rcvhf, never()).write();
        assertThat(msgLineHistory).contains("No outstanding lines on this PO");
    }

    // ───────────────────────── LOAD-PO-LINES / ADD-OUTSTANDING (ground truth)
    // ─────────────────────────

    @Test
    void execute_poLineFullyReceivedAlready_excludedFromOutstandingTable() {
        podLines.clear();
        podLines.add(
                poLine(1, 5001, 1, 1, 30, new BigDecimal("10.00"), 30)); // PD-RECV-QTY >= PD-QTY
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("No outstanding lines on this PO");
    }

    @Test
    void execute_poLineStatusCancelled_excludedFromOutstandingTable() {
        podLines.clear();
        podLines.add(poLine(1, 5001, 1, 9, 30, new BigDecimal("10.00"), 0)); // PD-STATUS = 9
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("No outstanding lines on this PO");
    }

    @Test
    void execute_unknownProductOnLine_showsUnknownProductNameButStillReceivable() {
        doReturn(true).when(prodf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        verify(rcvhf, times(1)).write();
        // unknown product => PR-STOCK-MNG defaults to 0 => stock/movement skipped.
        verify(stokf, never()).write();
    }

    // ───────────────────────── ACCEPT-RHEADER (ARH-010) ─────────────────────────

    @Test
    void execute_cancelAtReceivingDatePrompt_discardsWithoutEnteringLines() {
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        verify(rcvhf, never()).write();
        assertThat(msgLineHistory).contains("Cancelled");
    }

    // ───────────────────────── ENTER-RECV-LINES / VALIDATE-RECV-LINE (ground truth)
    // ─────────────────────────

    /**
     * CONVERT-GAP CHECK (none found): COBOL ADD-OUTSTANDING pre-fills WL-RECV to the full
     * outstanding quantity (WL-OUT) before the user ever sees the line; ENTER-RECV- LINES' "03"
     * (PF3) branch only sets WK-ENTRY-DONE — it does not clear an already-defaulted WL-RECV. So
     * pressing PF3 on the very first line prompt still posts a full-quantity receipt for that line.
     * Rc0010Service mirrors this exactly (no reset of wlRecv in the "03" case of
     * enterReceivingLines()). This test locks that faithful (if surprising) behavior in place.
     */
    @Test
    void execute_pf3AtFirstLinePrompt_stillReceivesPrePopulatedFullQuantity() {
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        verify(rcvhf, times(1)).write();
        assertThat(rcvdf.getRecord().getDecimal("RD-QTY"))
                .isEqualByComparingTo(new BigDecimal("30"));
        assertThat(msgLineHistory).contains("Receiving posted");
    }

    @Test
    void execute_pf4SkipLine_setsZeroReceivedAndDiscardsWhenOnlyLine() {
        when(renderer.readEndStatus()).thenReturn("00", "00", "04", "03");

        service.execute();

        verify(rcvhf, never()).write();
        assertThat(msgLineHistory).contains("Line skipped");
        assertThat(msgLineHistory).contains("Nothing received - discarded");
    }

    @Test
    void execute_negativeReceivedQty_rejectsWithNegativeMessageAndStaysOnLine() {
        acceptValues.put("WK-D-RECV", "-1");
        // Line entry retried after error, then skipped with PF4, then confirm PF3 not reached
        // (no lines received) — sequence: PPO ESTS,ARH ESTS,line-1(err),line-1 pf4-skip,end-of-po
        // key.
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "04", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Received qty cannot be negative");
        verify(rcvhf, never()).write();
    }

    @Test
    void execute_receivedQtyExceedsOutstanding_rejectsWithExceedsMessageAndStaysOnLine() {
        acceptValues.put("WK-D-RECV", "999");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "04", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Received qty exceeds outstanding");
        verify(rcvhf, never()).write();
    }

    @Test
    void execute_invalidKeyDuringLineEntry_showsInvalidKeyMessageAndStaysOnLine() {
        when(renderer.readEndStatus()).thenReturn("00", "00", "09", "04", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Invalid key");
    }

    // ───────────────────────── CONFIRM-SAVE (CSAV-010) ─────────────────────────

    @Test
    void execute_confirmDeclined_discardsReceivingWithoutSaving() {
        acceptValues.put("WK-CONFIRM", "N");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        verify(rcvhf, never()).write();
        assertThat(msgLineHistory).contains("Receiving discarded");
    }

    // ───────────────────────── SAVE-RECV failures (ground truth) ─────────────────────────

    @Test
    void execute_numgenRecvNumberFails_doesNotWriteHeaderOrDetail() {
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("99");
                            return null;
                        })
                .when(numgenService)
                .execute(any());
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        verify(rcvhf, never()).write();
        verify(rcvdf, never()).write();
        assertThat(msgLineHistory).contains("Receiving number assignment failed");
    }

    @Test
    void execute_rcvhfWriteInvalidKey_showsWriteFailedAndSkipsLinePosting() {
        doReturn(true).when(rcvhf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        verify(rcvhf, times(1)).write();
        verify(rcvdf, never()).write();
        assertThat(msgLineHistory).contains("Receiving header write failed");
    }

    /**
     * CONVERT-GAP CHECK (none found): COBOL WRITE-MOVEMENT's NUMGEN "STKMOV" failure path is a
     * silent GO TO WMV-999 — no message, and the movement record is simply not written while the
     * stock update (ADJUST-STOCK/CREATE-STOCK) and PO-line update still happened beforehand.
     * Rc0010Service.writeStockMovement() mirrors this exactly (plain return, no message). This test
     * locks that faithful behavior in place.
     */
    @Test
    void execute_stockMovementNumgenFails_stockStillUpdatedButNoMovementWritten() {
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            String key = p.getKnum().getKnumKey();
                            if (key != null && "STKMOV".equals(key.trim())) {
                                p.getKnum().setKnumStatus("99");
                            } else {
                                p.getKnum().setKnumStatus("00");
                                p.getKnum().setKnumNumber(9001L);
                            }
                            return null;
                        })
                .when(numgenService)
                .execute(any());
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        verify(stokf, times(1)).write();
        verify(smovf, never()).write();
        verify(rcvhf, times(1)).write();
    }

    @Test
    void execute_podfInvalidOnUpdatePoLine_skipsPoLineUpdateButHeaderStillPosted() {
        // First isInvalidKey() call is loadPoLines' START (must succeed / not used),
        // subsequent calls represent the composite-key readByKey() in updatePoLineReceived.
        doReturn(false, true).when(podf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        verify(rcvhf, times(1)).write();
        verify(podf, never()).rewrite();
    }

    // ───────────────────────── UPDATE-PO-STATUS (UPS-010/020, ground truth)
    // ─────────────────────────

    @Test
    void execute_otherLineStillOutstandingAfterPost_setsPoHeaderStatusPartial() {
        podLines.clear();
        podLines.add(poLine(1, 5001, 1, 1, 30, new BigDecimal("10.00"), 0));
        podLines.add(
                poLine(
                        2,
                        5002,
                        1,
                        1,
                        50,
                        new BigDecimal("5.00"),
                        10)); // still outstanding, not received
        acceptValues.put("WK-D-RECV", "30");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "04", "00", "03");

        service.execute();

        assertThat(pohf.getRecord().getInt("PH-STATUS")).isEqualTo(1);
    }

    @Test
    void execute_startInvalidKeyOnScanForOutstanding_jumpsDirectlyToHeaderStatusUpdate() {
        // Ground truth: COBOL UPS-010's START ... INVALID KEY GO TO UPS-020 skips the
        // READ-NEXT loop entirely and still updates PH-STATUS (WK-OUT-FLG stays 0 => status 2).
        doReturn(true, false).when(podf).start(any(), any());
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        verify(rcvhf, times(1)).write();
        assertThat(pohf.getRecord().getInt("PH-STATUS")).isEqualTo(2);
    }

    // ───────────────────────── file open lifecycle (OPEN-FILES / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_pohfOpenFailsPersistently_abortsWithCompletionCode255() {
        doReturn("99").when(pohf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(podf, never()).open(any());
    }

    @Test
    void execute_pohfOpenStatus35_reopensAsOutputThenReopensIo() {
        doReturn("35", "00", "00", "00").when(pohf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(pohf, times(3)).open(any());
        verify(pohf, times(1)).open(FileOpenMode.OUTPUT);
        verify(pohf, times(2)).open(FileOpenMode.IO);
        verify(pohf, times(2)).close();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_normalRun_closesAllEightFilesOnTermination() {
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(pohf, times(1)).close();
        verify(podf, times(1)).close();
        verify(rcvhf, times(1)).close();
        verify(rcvdf, times(1)).close();
        verify(stokf, times(1)).close();
        verify(smovf, times(1)).close();
        verify(suppf, times(1)).close();
        verify(prodf, times(1)).close();
    }
}

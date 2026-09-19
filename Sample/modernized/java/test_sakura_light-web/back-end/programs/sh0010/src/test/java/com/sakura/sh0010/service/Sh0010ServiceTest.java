package com.sakura.sh0010.service;

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
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ScreenModels.ScreenDef;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.OrddfDataset;
import com.sakura.runtime.io.OrdhfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.ShpdfDataset;
import com.sakura.runtime.io.ShphfDataset;
import com.sakura.runtime.io.SmovfDataset;
import com.sakura.runtime.io.StokfDataset;
import com.sakura.runtime.linkage.NumgenLinkParm;
import com.sakura.runtime.record.RuntimeFieldAccess;
import com.sakura.sh0010.domain.Sh0010FieldAccess;
import com.sakura.sh0010.runtime.Sh0010Datasets;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Sh0010Service (COBOL SH0010 — shipping entry / picking & despatch), generated from
 * {@code SH0010.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: every FD dataset (ORDHF/ORDDF/SHPHF/SHPDF/STOKF/SMOVF/CUSTF/PRODF) is a real
 * object wrapped with {@code spy()} so the shared record buffer (and therefore {@link
 * Sh0010FieldAccess}, which registers those buffers) works exactly as production; only the I/O
 * methods (open/close/write/rewrite/readByKey/readNext/start/getFileStatus/ isInvalidKey) are
 * stubbed so no real file/DB access happens. ORDHF/CUSTF/PRODF/STOKF are single-record lookups
 * (like AR0010's CUSTF/BANKF); ORDDF is a small in-memory table ({@link #orderLines}) driven by
 * START+READ NEXT / READ BY KEY / REWRITE stubs, because SH0010 actually iterates multiple order
 * detail lines per order (LOAD-ORDER-LINES, CHECK-COMPLETE) and mutates them (UPDATE-ORDER-LINE).
 *
 * <p>DATEUT runs for real (pure calendar math, matches the COBOL copy exactly); NUMGEN and ABORTX
 * are mocked since they are file-I/O subprograms irrelevant to SH0010's own logic.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Sh0010ServiceTest {

    @Mock private ScreenRendererInstance renderer;
    @Mock private NumgenService numgenService;
    @Mock private AbortxService abortxService;

    private DateutService dateutService;

    private Sh0010Datasets fileSet;
    private OrdhfDataset ordhf;
    private OrddfDataset orddf;
    private ShphfDataset shphf;
    private ShpdfDataset shpdf;
    private StokfDataset stokf;
    private SmovfDataset smovf;
    private CustfDataset custf;
    private ProdfDataset prodf;

    private Sh0010Service service;

    private final Map<String, String> acceptValues = new HashMap<>();
    private final List<String> screenInteractions = new ArrayList<>();
    private final List<String> msgLineHistory = new ArrayList<>();

    /**
     * In-memory ORDDF "table" — mutable so UPDATE-ORDER-LINE's rewrite is visible to the later
     * CHECK-COMPLETE scan, exactly like the real indexed file would be.
     */
    private static final class OrderLine {
        long odNo;
        int odLine;
        int odProd;
        int odWhse;
        BigDecimal odQty;
        BigDecimal odShippedQty;
        BigDecimal odAllocQty;
        BigDecimal odUnitPrice;
        int odStatus;
    }

    private final List<OrderLine> orderLines = new ArrayList<>();
    private final AtomicInteger orddfCursor = new AtomicInteger(0);
    private final AtomicBoolean orddfAtEnd = new AtomicBoolean(false);
    private final AtomicBoolean orddfInvalid = new AtomicBoolean(false);

    private void writeOrderLineToBuffer(OrderLine l) {
        orddf.getRecord().setLong("OD-NO", l.odNo);
        orddf.getRecord().setInt("OD-LINE", l.odLine);
        orddf.getRecord().setInt("OD-PROD", l.odProd);
        orddf.getRecord().setInt("OD-WHSE", l.odWhse);
        orddf.getRecord().setDecimal("OD-QTY", l.odQty);
        orddf.getRecord().setDecimal("OD-SHIPPED-QTY", l.odShippedQty);
        orddf.getRecord().setDecimal("OD-ALLOC-QTY", l.odAllocQty);
        orddf.getRecord().setDecimal("OD-UNIT-PRICE", l.odUnitPrice);
        orddf.getRecord().setInt("OD-STATUS", l.odStatus);
    }

    private OrderLine newOrderLine(
            int line, int prod, int whse, long qty, long shipped, long alloc, String price) {
        OrderLine l = new OrderLine();
        l.odNo = 1L;
        l.odLine = line;
        l.odProd = prod;
        l.odWhse = whse;
        l.odQty = BigDecimal.valueOf(qty);
        l.odShippedQty = BigDecimal.valueOf(shipped);
        l.odAllocQty = BigDecimal.valueOf(alloc);
        l.odUnitPrice = new BigDecimal(price);
        l.odStatus = 1;
        return l;
    }

    @BeforeEach
    void setUp() {
        dateutService = new DateutService();

        Sh0010Datasets real = new Sh0010Datasets();
        fileSet = spy(real);
        ordhf = spy(real.getOrdhf());
        orddf = spy(real.getOrddf());
        shphf = spy(real.getShphf());
        shpdf = spy(real.getShpdf());
        stokf = spy(real.getStokf());
        smovf = spy(real.getSmovf());
        custf = spy(real.getCustf());
        prodf = spy(real.getProdf());
        doReturn(ordhf).when(fileSet).getOrdhf();
        doReturn(orddf).when(fileSet).getOrddf();
        doReturn(shphf).when(fileSet).getShphf();
        doReturn(shpdf).when(fileSet).getShpdf();
        doReturn(stokf).when(fileSet).getStokf();
        doReturn(smovf).when(fileSet).getSmovf();
        doReturn(custf).when(fileSet).getCustf();
        doReturn(prodf).when(fileSet).getProdf();

        for (RawDatasetBaseHandle h : allFiles()) {
            doNothing().when(h.file).open(any());
            doNothing().when(h.file).close();
            doReturn("00").when(h.file).getFileStatus();
        }
        doReturn(false).when(ordhf).isInvalidKey();
        doReturn(false).when(custf).isInvalidKey();
        doReturn(false).when(prodf).isInvalidKey();
        doReturn(false).when(stokf).isInvalidKey();
        doReturn(false).when(shphf).isInvalidKey();

        doNothing().when(ordhf).rewrite();
        doNothing().when(shphf).rewrite();
        doNothing().when(stokf).rewrite();
        doNothing().when(shphf).write();
        doNothing().when(shpdf).write();
        doNothing().when(smovf).write();

        doReturn(true).when(ordhf).readByKey(any());
        doReturn(true).when(custf).readByKey(any());
        doReturn(true).when(prodf).readByKey(any());
        doReturn(true).when(stokf).readByKey(any());
        doReturn(true).when(shphf).readByKey(any());

        // ---- ORDDF: START resets the in-memory cursor; READ NEXT walks orderLines in
        // order (COBOL relies on the index being physically ordered by OD-NO/OD-LINE);
        // READ BY KEY looks a single line up by (OD-NO, OD-LINE) already set in the
        // buffer; REWRITE writes the mutated buffer back into the matching OrderLine so
        // later scans (CHECK-COMPLETE) observe UPDATE-ORDER-LINE's effect.
        doAnswer(
                        inv -> {
                            orddfCursor.set(0);
                            orddfAtEnd.set(false);
                            orddfInvalid.set(false);
                            return true;
                        })
                .when(orddf)
                .start(any(), any());
        doAnswer(inv -> orddfInvalid.get()).when(orddf).isInvalidKey();
        doAnswer(
                        inv -> {
                            int i = orddfCursor.getAndIncrement();
                            if (i < orderLines.size()) {
                                writeOrderLineToBuffer(orderLines.get(i));
                                orddfAtEnd.set(false);
                            } else {
                                orddfAtEnd.set(true);
                            }
                            return null;
                        })
                .when(orddf)
                .readNext();
        doAnswer(inv -> orddfAtEnd.get()).when(orddf).isAtEnd();
        doAnswer(
                        inv -> {
                            long odNo = orddf.getRecord().getLong("OD-NO");
                            int odLine = orddf.getRecord().getInt("OD-LINE");
                            OrderLine found =
                                    orderLines.stream()
                                            .filter(l -> l.odNo == odNo && l.odLine == odLine)
                                            .findFirst()
                                            .orElse(null);
                            if (found == null) {
                                orddfInvalid.set(true);
                                return false;
                            }
                            writeOrderLineToBuffer(found);
                            orddfInvalid.set(false);
                            return true;
                        })
                .when(orddf)
                .readByKey(any());
        doAnswer(
                        inv -> {
                            long odNo = orddf.getRecord().getLong("OD-NO");
                            int odLine = orddf.getRecord().getInt("OD-LINE");
                            orderLines.stream()
                                    .filter(l -> l.odNo == odNo && l.odLine == odLine)
                                    .findFirst()
                                    .ifPresent(
                                            l -> {
                                                l.odShippedQty =
                                                        orddf.getRecord()
                                                                .getDecimal("OD-SHIPPED-QTY");
                                                l.odAllocQty =
                                                        orddf.getRecord()
                                                                .getDecimal("OD-ALLOC-QTY");
                                                l.odStatus = orddf.getRecord().getInt("OD-STATUS");
                                            });
                            return null;
                        })
                .when(orddf)
                .rewrite();

        // ---- default header/customer/product/stock data ----
        ordhf.getRecord().setLong("OH-NO", 1L);
        ordhf.getRecord().setInt("OH-DEL-FLAG", 0);
        ordhf.getRecord().setInt("OH-STATUS", 1); // Allocated
        ordhf.getRecord().setInt("OH-CUST", 100);
        ordhf.getRecord().setInt("OH-WHSE", 1);
        ordhf.getRecord().setInt("OH-STAFF", 10);
        ordhf.getRecord().setString("OH-REMARK", "Test order");

        custf.getRecord().setString("CU-NAME", "ACME Ltd");

        prodf.getRecord().setString("PR-NAME", "Widget");
        prodf.getRecord().setInt("PR-STOCK-MNG", 1);

        stokf.getRecord().setDecimal("SK-AVG-COST", new BigDecimal("5.00"));
        stokf.getRecord().setDecimal("SK-ONHAND", new BigDecimal("100"));
        stokf.getRecord().setDecimal("SK-ALLOCATED", new BigDecimal("20"));
        stokf.getRecord().setDecimal("SK-YTD-OUT", BigDecimal.ZERO);

        orderLines.add(newOrderLine(1, 500, 1, 10, 0, 10, "2.50"));

        acceptValues.put("WK-SEL-NO", "1");
        acceptValues.put("WK-EDIT-LN", "0");
        acceptValues.put("WK-EDIT-QTY", "0");
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
                            if (w instanceof Sh0010FieldAccess a) {
                                msgLineHistory.add(a.getWkMsgLine().trim());
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

        service = new Sh0010Service(fileSet, dateutService, numgenService, abortxService, renderer);
    }

    private record RawDatasetBaseHandle(com.sakura.runtime.record.RawDatasetBase file) {}

    private List<RawDatasetBaseHandle> allFiles() {
        return List.of(
                new RawDatasetBaseHandle(ordhf),
                new RawDatasetBaseHandle(orddf),
                new RawDatasetBaseHandle(shphf),
                new RawDatasetBaseHandle(shpdf),
                new RawDatasetBaseHandle(stokf),
                new RawDatasetBaseHandle(smovf),
                new RawDatasetBaseHandle(custf),
                new RawDatasetBaseHandle(prodf));
    }

    // ───────────────────────── happy path ─────────────────────────

    @Test
    void execute_fullyShippedSingleLineOrder_postsShipmentUpdatesStockAndCompletesOrder() {
        when(renderer.readEndStatus()).thenReturn("00", "03", "00", "03");

        service.execute();

        verify(shphf, times(1)).write();
        verify(shpdf, times(1)).write();
        verify(smovf, times(1)).write();
        verify(ordhf, times(1)).rewrite();
        verify(stokf, times(1)).rewrite();
        verify(orddf, times(1)).rewrite();

        assertThat(shphf.getRecord().getLong("XH-NO")).isEqualTo(9001L);
        assertThat(shphf.getRecord().getLong("XH-ORDER")).isEqualTo(1L);
        assertThat(shphf.getRecord().getInt("XH-CUST")).isEqualTo(100);
        assertThat(shphf.getRecord().getInt("XH-LINES")).isEqualTo(1);

        assertThat(shpdf.getRecord().getLong("XD-NO")).isEqualTo(9001L);
        assertThat(shpdf.getRecord().getInt("XD-LINE")).isEqualTo(1);
        assertThat(shpdf.getRecord().getDecimal("XD-QTY"))
                .isEqualByComparingTo(new BigDecimal("10"));
        assertThat(shpdf.getRecord().getDecimal("XD-AMOUNT"))
                .isEqualByComparingTo(new BigDecimal("25"));
        assertThat(shpdf.getRecord().getDecimal("XD-UNIT-COST"))
                .isEqualByComparingTo(new BigDecimal("5.00"));

        assertThat(smovf.getRecord().getInt("SM-KIND")).isEqualTo(10);
        assertThat(smovf.getRecord().getDecimal("SM-QTY"))
                .isEqualByComparingTo(new BigDecimal("-10"));

        assertThat(stokf.getRecord().getDecimal("SK-ONHAND"))
                .isEqualByComparingTo(new BigDecimal("90"));
        assertThat(stokf.getRecord().getDecimal("SK-ALLOCATED"))
                .isEqualByComparingTo(new BigDecimal("10"));
        assertThat(stokf.getRecord().getDecimal("SK-YTD-OUT"))
                .isEqualByComparingTo(new BigDecimal("10"));

        assertThat(orderLines.get(0).odShippedQty).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(orderLines.get(0).odAllocQty).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(orderLines.get(0).odStatus).isEqualTo(3); // fully shipped

        assertThat(ordhf.getRecord().getInt("OH-STATUS")).isEqualTo(3);
        assertThat(msgLineHistory)
                .anyMatch(m -> m.startsWith("Shipment 0000009001 created - 001 line(s)"));
        assertThat(screenInteractions)
                .contains("displayScreen:DS-ORDER", "displayScreen:DS-CONFIRM");
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_partialShipViaAppliedEdit_leavesOrderPartShipped() {
        acceptValues.put("WK-EDIT-LN", "1");
        acceptValues.put("WK-EDIT-QTY", "4");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        verify(shpdf, times(1)).write();
        assertThat(shpdf.getRecord().getDecimal("XD-QTY"))
                .isEqualByComparingTo(new BigDecimal("4"));
        assertThat(orderLines.get(0).odShippedQty).isEqualByComparingTo(new BigDecimal("4"));
        assertThat(orderLines.get(0).odStatus).isEqualTo(2); // part-ship
        assertThat(ordhf.getRecord().getInt("OH-STATUS")).isEqualTo(2);
    }

    @Test
    void execute_confirmLowercaseY_stillPostsShipment() {
        acceptValues.put("WK-CONFIRM", "y");
        when(renderer.readEndStatus()).thenReturn("00", "03", "00", "03");

        service.execute();

        verify(shphf, times(1)).write();
        assertThat(msgLineHistory).anyMatch(m -> m.startsWith("Shipment"));
    }

    // ───────────────────────── main-screen / order-selection edge cases ─────────────────────────

    @Test
    void execute_pf3AtOrderPrompt_endsImmediatelyWithoutReadingOrder() {
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(ordhf, never()).readByKey(any());
        assertThat(service.getCompletionCode()).isEqualTo(0);
        verify(ordhf, times(1)).open(any());
        verify(ordhf, times(1)).close();
    }

    @Test
    void execute_invalidFunctionKeyAtOrderPrompt_showsMessageAndLoops() {
        when(renderer.readEndStatus()).thenReturn("09", "03");

        service.execute();

        verify(ordhf, never()).readByKey(any());
        assertThat(msgLineHistory).contains("Invalid function key");
    }

    @Test
    void execute_selectionNumberZero_rejectsWithoutReadingOrder() {
        acceptValues.put("WK-SEL-NO", "0");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(ordhf, never()).readByKey(any());
        assertThat(msgLineHistory).contains("Order number must not be zero");
    }

    @Test
    void execute_orderNotFound_rejectsWithNotFoundMessage() {
        doReturn(true).when(ordhf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Order not found");
        verify(shphf, never()).write();
    }

    @Test
    void execute_orderDeleted_rejectsWithDeletedMessage() {
        ordhf.getRecord().setInt("OH-DEL-FLAG", 1);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Order is deleted");
        verify(shphf, never()).write();
    }

    @Test
    void execute_orderStatusZero_rejectsWithNotAllocatedMessage() {
        ordhf.getRecord().setInt("OH-STATUS", 0);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Order is not allocated yet - run OE0030");
        verify(shphf, never()).write();
    }

    @Test
    void execute_orderStatusShipped_rejectsWithCannotBeShippedMessage() {
        ordhf.getRecord().setInt("OH-STATUS", 3);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Order cannot be shipped in its status");
        verify(shphf, never()).write();
    }

    @Test
    void execute_orderHasNoDetailLines_rejectsWithNoDetailLinesMessage() {
        orderLines.clear();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Order has no detail lines");
        verify(shphf, never()).write();
    }

    @Test
    void execute_customerNotFound_showsUnknownCustomerButStillProceeds() {
        doReturn(true).when(custf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03", "00", "03");

        service.execute();

        verify(shphf, times(1)).write();
        assertThat(screenInteractions).contains("displayScreen:DS-ORDER");
    }

    // ───────────────────────── ship-loop edit / navigation (ground truth: SHIP-LOOP / APPLY-EDIT /
    // ZERO-ALL / PAGE-*) ─────────────────────────

    @Test
    void execute_zeroAllShipQuantities_discardsShipmentWithNothingToShipMessage() {
        when(renderer.readEndStatus()).thenReturn("00", "04", "03", "03");

        service.execute();

        verify(shphf, never()).write();
        assertThat(msgLineHistory).contains("Nothing to ship - shipment discarded");
    }

    @Test
    void execute_applyEditLineNumberZero_rejectsWithEnterLineMessage() {
        acceptValues.put("WK-EDIT-LN", "0");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Enter a line number to change");
    }

    @Test
    void execute_applyEditLineNumberNotFound_rejectsWithLineNotFoundMessage() {
        acceptValues.put("WK-EDIT-LN", "99");
        acceptValues.put("WK-EDIT-QTY", "1");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Line number not found");
    }

    @Test
    void execute_applyEditNegativeQty_rejectsWithNegativeQuantityMessage() {
        acceptValues.put("WK-EDIT-LN", "1");
        acceptValues.put("WK-EDIT-QTY", "-1");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Quantity cannot be negative");
        verify(shphf, never()).write();
    }

    @Test
    void execute_applyEditQtyExceedsCap_capsToAllocatedAndShowsCappedMessage() {
        acceptValues.put("WK-EDIT-LN", "1");
        acceptValues.put("WK-EDIT-QTY", "999");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Quantity capped to allocated / outstanding");
        assertThat(shpdf.getRecord().getDecimal("XD-QTY"))
                .isEqualByComparingTo(new BigDecimal("10"));
    }

    @Test
    void execute_invalidKeyInShipLoop_showsInvalidKeyMessageAndLoops() {
        when(renderer.readEndStatus()).thenReturn("00", "09", "03", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Invalid key");
    }

    @Test
    void execute_pageDownBeyondLastPage_showsAlreadyAtLastPageMessage() {
        when(renderer.readEndStatus()).thenReturn("00", "06", "03", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Already at last page");
    }

    @Test
    void execute_pageUpAtFirstPage_showsAlreadyAtFirstPageMessage() {
        when(renderer.readEndStatus()).thenReturn("00", "12", "03", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Already at first page");
    }

    @Test
    void execute_pageDownThenPageUp_ratesMultiPageWindowCorrectly() {
        // 14 lines -> WK-PGSIZE (13) means page 1 shows lines 1-13, page 2 shows line 14.
        orderLines.clear();
        for (int i = 1; i <= 14; i++) {
            orderLines.add(newOrderLine(i, 500, 1, 10, 0, 10, "1.00"));
        }
        when(renderer.readEndStatus()).thenReturn("00", "06", "12", "04", "03", "03");

        service.execute();

        verify(shphf, never()).write();
        assertThat(msgLineHistory).contains("Nothing to ship - shipment discarded");
    }

    // ───────────────────────── confirm-shipment (ground truth: CONFIRM-SHIP)
    // ─────────────────────────

    @Test
    void execute_confirmPf3Cancel_showsCancelledMessageWithoutPosting() {
        when(renderer.readEndStatus()).thenReturn("00", "03", "03", "03");

        service.execute();

        verify(shphf, never()).write();
        assertThat(msgLineHistory).contains("Shipment cancelled");
    }

    @Test
    void execute_confirmNo_discardsShipmentWithCancelledMessage() {
        acceptValues.put("WK-CONFIRM", "N");
        when(renderer.readEndStatus()).thenReturn("00", "03", "00", "03");

        service.execute();

        verify(shphf, never()).write();
        assertThat(msgLineHistory).contains("Shipment cancelled");
    }

    // ───────────────────────── post-shipment failure paths (ground truth: POST-SHIPMENT /
    // UPDATE-STOCK / WRITE-MOVEMENT) ─────────────────────────

    @Test
    void execute_shipNumgenFails_doesNotPostShipment() {
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("99");
                            return null;
                        })
                .when(numgenService)
                .execute(any());
        when(renderer.readEndStatus()).thenReturn("00", "03", "00", "03");

        service.execute();

        verify(shphf, never()).write();
        assertThat(msgLineHistory).contains("Ship number assignment failed");
    }

    @Test
    void execute_stockMovementNumgenFails_stillWritesShipmentAndOrderLineButSkipsMovement() {
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
        when(renderer.readEndStatus()).thenReturn("00", "03", "00", "03");

        service.execute();

        verify(shphf, times(1)).write();
        verify(shpdf, times(1)).write();
        verify(smovf, never()).write();
        assertThat(msgLineHistory).contains("Movement number assignment failed");
        assertThat(orderLines.get(0).odShippedQty).isEqualByComparingTo(new BigDecimal("10"));
    }

    @Test
    void execute_stockNotManagedForProduct_skipsStockUpdateAndMovement() {
        prodf.getRecord().setInt("PR-STOCK-MNG", 0);
        when(renderer.readEndStatus()).thenReturn("00", "03", "00", "03");

        service.execute();

        verify(stokf, never()).readByKey(any());
        verify(smovf, never()).write();
        verify(shpdf, times(1)).write();
        assertThat(shpdf.getRecord().getDecimal("XD-UNIT-COST"))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void execute_stockRecordNotFound_zeroCostAndSkipsMovement() {
        doReturn(true).when(stokf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03", "00", "03");

        service.execute();

        verify(stokf, never()).rewrite();
        verify(smovf, never()).write();
        verify(shpdf, times(1)).write();
        assertThat(shpdf.getRecord().getDecimal("XD-UNIT-COST"))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void execute_stockAllocatedWouldGoNegative_flooredToZero() {
        stokf.getRecord().setDecimal("SK-ALLOCATED", new BigDecimal("3"));
        when(renderer.readEndStatus()).thenReturn("00", "03", "00", "03");

        service.execute();

        assertThat(stokf.getRecord().getDecimal("SK-ALLOCATED"))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ───────────────────────── file open lifecycle (ground truth: OPEN-FILES / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_ordhfOpenFailsPersistently_abortsWithCompletionCode255() {
        doReturn("99").when(ordhf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(orddf, never()).open(any());
        verify(renderer, never()).readEndStatus();
    }

    @Test
    void execute_ordhfOpenStatus35_reopensAsOutputThenReopensIo() {
        doReturn("35", "00", "00", "00").when(ordhf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(ordhf, times(3)).open(any());
        verify(ordhf, times(1)).open(FileOpenMode.OUTPUT);
        verify(ordhf, times(2)).open(FileOpenMode.IO);
        verify(ordhf, times(2)).close();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }
}

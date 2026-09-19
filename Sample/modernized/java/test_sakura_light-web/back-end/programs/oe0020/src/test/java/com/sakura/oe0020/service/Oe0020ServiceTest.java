package com.sakura.oe0020.service;

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
import com.sakura.oe0020.domain.Oe0020FieldAccess;
import com.sakura.oe0020.runtime.Oe0020Datasets;
import com.sakura.runtime.ScreenModels.InputFieldDef;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.OrddfDataset;
import com.sakura.runtime.io.OrdhfDataset;
import com.sakura.runtime.io.ProdfDataset;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Oe0020Service (COBOL OE0020 — sales order inquiry / list), generated from {@code
 * OE0020.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: ORDHF/ORDDF/CUSTF/PRODF are real dataset objects wrapped with {@code spy()} so
 * the record buffer (and therefore {@code Oe0020FieldAccess}, which registers those buffers) works
 * exactly as in production; only the I/O methods
 * (open/close/readByKey/readNext/start/getFileStatus/isInvalidKey/isAtEnd) are stubbed so no real
 * file/DB access happens. DATEUT runs for real (pure calendar math); ABORTX is mocked (file I/O
 * irrelevant to OE0020's own logic).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Oe0020ServiceTest {

    @Mock private ScreenRendererInstance renderer;
    @Mock private AbortxService abortxService;

    private DateutService dateutService;

    private Oe0020Datasets fileSet;
    private OrdhfDataset ordhf;
    private OrddfDataset orddf;
    private CustfDataset custf;
    private ProdfDataset prodf;

    private Oe0020Service service;

    private final Map<String, String> acceptValues = new HashMap<>();
    private final List<String> screenInteractions = new ArrayList<>();
    private final List<String> msgLineHistory = new ArrayList<>();

    /**
     * WK-CUST-NAME/WK-CUR-NO/WK-HCNT snapshotted at every displayScreen() call. Needed because
     * CLRS-010 unconditionally resets these fields at the top of every MAIN-RTN cycle — including
     * the terminating cycle every test drives through to end MAIN-000 — so the FINAL ws state never
     * shows the values set mid-cycle. History-based assertion sidesteps that.
     */
    private final List<String> custNameHistory = new ArrayList<>();

    private final List<Long> curNoHistory = new ArrayList<>();
    private final List<Integer> hcntHistory = new ArrayList<>();

    @BeforeEach
    void setUp() {
        dateutService = new DateutService();

        Oe0020Datasets real = new Oe0020Datasets();
        fileSet = spy(real);
        ordhf = spy(real.getOrdhf());
        orddf = spy(real.getOrddf());
        custf = spy(real.getCustf());
        prodf = spy(real.getProdf());
        doReturn(ordhf).when(fileSet).getOrdhf();
        doReturn(orddf).when(fileSet).getOrddf();
        doReturn(custf).when(fileSet).getCustf();
        doReturn(prodf).when(fileSet).getProdf();

        doNothing().when(ordhf).open(any());
        doNothing().when(orddf).open(any());
        doNothing().when(custf).open(any());
        doNothing().when(prodf).open(any());
        doNothing().when(ordhf).close();
        doNothing().when(orddf).close();
        doNothing().when(custf).close();
        doNothing().when(prodf).close();

        // Default: order 1001 found directly, active, status "Entered".
        doReturn(true).when(ordhf).readByKey(any());
        doReturn(false).when(ordhf).isInvalidKey();
        ordhf.getRecord().setLong("OH-NO", 1001L);
        ordhf.getRecord().setInt("OH-DEL-FLAG", 0);
        ordhf.getRecord().setInt("OH-CUST", 100);
        ordhf.getRecord().setInt("OH-DATE", 20260101);
        ordhf.getRecord().setInt("OH-STATUS", 0);

        // ORDHF sequential browse (READ-NEXT-LIVE / customer START, NEXT-ORDER):
        // by default, no further live order after the current one (EOF).
        doReturn(true).when(ordhf).readNext();
        doReturn(true).when(ordhf).isAtEnd();
        doReturn(true).when(ordhf).start(any(), any());

        // Default: customer 100 found.
        doReturn(true).when(custf).readByKey(any());
        doReturn(false).when(custf).isInvalidKey();
        custf.getRecord().setInt("CU-CODE", 100);
        custf.getRecord().setString("CU-NAME", "ACME Corp");

        // Default: product found.
        doReturn(true).when(prodf).readByKey(any());
        doReturn(false).when(prodf).isInvalidKey();
        prodf.getRecord().setString("PR-NAME", "Widget");

        // Default: no order detail lines.
        stubOrderDetailLines(1001L, Collections.emptyList());
        doReturn(true).when(orddf).start(any(), any());
        doReturn(false).when(orddf).isInvalidKey();

        acceptValues.put("WK-SEL-NO", "1001");
        acceptValues.put("WK-SEL-CUST", "0");
        acceptValues.put("WK-SEL-DATE", "0");
        acceptValues.put("WK-DUMMY", " ");

        when(renderer.acceptField(any()))
                .thenAnswer(
                        inv -> {
                            InputFieldDef field = inv.getArgument(0, InputFieldDef.class);
                            return acceptValues.getOrDefault(field.name, "");
                        });

        doAnswer(
                        inv -> {
                            com.sakura.runtime.ScreenModels.ScreenDef def = inv.getArgument(0);
                            RuntimeFieldAccess w = inv.getArgument(1);
                            screenInteractions.add("displayScreen:" + def.name);
                            if (w instanceof Oe0020FieldAccess a) {
                                msgLineHistory.add(a.getWkMsgLine().trim());
                                custNameHistory.add(a.getWkCustName().trim());
                                curNoHistory.add(a.getWkCurNo());
                                hcntHistory.add(a.getWkHcnt());
                            }
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        service = new Oe0020Service(fileSet, dateutService, abortxService, renderer);
    }

    /**
     * Stubs ORDDF's sequential detail scan (START + READ NEXT) to yield {@code lines} rows for
     * order {@code orderNo}, each {@code int[]}: {line, prod, qty, price, amount, shippedQty,
     * allocQty}; EOF fires once every row has been consumed.
     */
    private void stubOrderDetailLines(long orderNo, List<int[]> lines) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i < lines.size()) {
                                int[] row = lines.get(i);
                                orddf.getRecord().setLong("OD-NO", orderNo);
                                orddf.getRecord().setInt("OD-LINE", row[0]);
                                orddf.getRecord().setInt("OD-PROD", row[1]);
                                orddf.getRecord().setDecimal("OD-QTY", BigDecimal.valueOf(row[2]));
                                orddf.getRecord()
                                        .setDecimal("OD-UNIT-PRICE", BigDecimal.valueOf(row[3]));
                                orddf.getRecord()
                                        .setDecimal("OD-AMOUNT", BigDecimal.valueOf(row[4]));
                                orddf.getRecord()
                                        .setDecimal("OD-SHIPPED-QTY", BigDecimal.valueOf(row[5]));
                                orddf.getRecord()
                                        .setDecimal("OD-ALLOC-QTY", BigDecimal.valueOf(row[6]));
                            }
                            return true;
                        })
                .when(orddf)
                .readNext();
        doAnswer(inv -> idx.get() > lines.size()).when(orddf).isAtEnd();
    }

    private static int[] line(
            int lineNo, int prod, int qty, int price, int amount, int shipped, int alloc) {
        return new int[] {lineNo, prod, qty, price, amount, shipped, alloc};
    }

    /**
     * Live handle onto the service's WorkingStorage/FD field accessor, obtained from the first
     * displayScreen() call. Same mutable object throughout the run.
     */
    private Oe0020FieldAccess capturedWs() {
        ArgumentCaptor<RuntimeFieldAccess> captor =
                ArgumentCaptor.forClass(RuntimeFieldAccess.class);
        verify(renderer, org.mockito.Mockito.atLeastOnce()).displayScreen(any(), captor.capture());
        return (Oe0020FieldAccess) captor.getAllValues().get(0);
    }

    // ───────────────────────── find by order number (ground truth: FIND-BY-NUMBER)
    // ─────────────────────────

    @Test
    void execute_orderFoundByNumber_showsOrderDetailsAndEndsProgram() {
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(screenInteractions).contains("displayScreen:DS-ORDER");
        assertThat(capturedWs().getWkStatText().trim()).isEqualTo("Entered");
        assertThat(capturedWs().getCompletionCode()).isEqualTo(0);
        verify(ordhf, times(1)).open(any());
        verify(ordhf, times(1)).close();
    }

    @Test
    void execute_orderNumberNotFound_showsMessageAndReturnsToMenu() {
        doReturn(true).when(ordhf).isInvalidKey();
        acceptValues.put("WK-SEL-NO", "9999");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Order number not found");
        assertThat(screenInteractions).doesNotContain("displayScreen:DS-ORDER");
    }

    @Test
    void execute_orderCancelledOrDeleted_showsMessageAndSkipsDetails() {
        ordhf.getRecord().setInt("OH-DEL-FLAG", 1);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Order is cancelled / deleted");
        assertThat(screenInteractions).doesNotContain("displayScreen:DS-ORDER");
    }

    // ───────────────────────── find by customer (ground truth: FIND-BY-CUSTOMER / READ-NEXT-LIVE)
    // ─────────────────────────

    @Test
    void execute_findByCustomer_foundLiveOrder_showsDetails() {
        acceptValues.put("WK-SEL-NO", "0");
        acceptValues.put("WK-SEL-CUST", "100");
        doReturn(false).when(ordhf).isAtEnd();
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(screenInteractions).contains("displayScreen:DS-ORDER");
        assertThat(msgLineHistory).doesNotContain("No orders for this customer");
    }

    @Test
    void execute_customerNotFound_showsMessage() {
        acceptValues.put("WK-SEL-NO", "0");
        acceptValues.put("WK-SEL-CUST", "999");
        doReturn(true).when(custf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Customer not found");
    }

    @Test
    void execute_customerFoundButStartInvalidKey_showsNoOrdersMessage() {
        acceptValues.put("WK-SEL-NO", "0");
        acceptValues.put("WK-SEL-CUST", "100");
        doReturn(true).when(ordhf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("No orders for this customer");
    }

    @Test
    void execute_customerFoundButNoLiveOrdersEof_showsNoOrdersMessage() {
        acceptValues.put("WK-SEL-NO", "0");
        acceptValues.put("WK-SEL-CUST", "100");
        // ordhf.isAtEnd() stays true (default) — READ-NEXT-LIVE hits EOF immediately.
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("No orders for this customer");
    }

    @Test
    void execute_customerFoundButOrderBelongsToDifferentCustomer_showsNoOrdersMessage() {
        acceptValues.put("WK-SEL-NO", "0");
        acceptValues.put("WK-SEL-CUST", "100");
        // START positions on the first record >= OH-CUST; the record READ NEXT lands on
        // here belongs to a different (higher) customer than the one searched for.
        doAnswer(
                        inv -> {
                            ordhf.getRecord().setInt("OH-CUST", 200);
                            ordhf.getRecord().setInt("OH-DEL-FLAG", 0);
                            return true;
                        })
                .when(ordhf)
                .readNext();
        doReturn(false).when(ordhf).isAtEnd();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("No orders for this customer");
    }

    // ───────────────────────── no selection entered (ground truth: PROCESS-KEY else branch)
    // ─────────────────────────

    @Test
    void execute_noSelectionEntered_showsEnterSelectionMessage() {
        acceptValues.put("WK-SEL-NO", "0");
        acceptValues.put("WK-SEL-CUST", "0");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Enter an order number or a customer code");
    }

    // ───────────────────────── order detail scan (ground truth: LOAD-DETAILS / LOOKUP-PRODUCT)
    // ─────────────────────────

    @Test
    void execute_orderWithDetailLines_buildsWindowAndSingleFullPage() {
        stubOrderDetailLines(
                1001L,
                List.of(
                        line(10, 5001, 3, 100, 300, 3, 0),
                        line(20, 5002, 2, 250, 500, 0, 2),
                        line(30, 5003, 1, 999, 999, 1, 1)));
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        Oe0020FieldAccess ws = capturedWs();
        assertThat(ws.getWkDcnt()).isEqualTo(3);
        assertThat(ws.getWkPageCnt()).isEqualTo(1);
        assertThat(ws.getWwProd(1)).isEqualTo(5001);
        assertThat(ws.getWwName(1).trim()).isEqualTo("Widget");
        assertThat(ws.getWwQty(1)).isEqualTo(3);
        assertThat(ws.getWwLine(4)).isEqualTo(0);
    }

    @Test
    void execute_orderDetailLinesExceed200_stopsAtMaxWithoutInfiniteLoop() {
        List<int[]> rows = new ArrayList<>();
        for (int i = 1; i <= 250; i++) {
            rows.add(line(i, 6000 + i, 1, 100, 100, 0, 0));
        }
        stubOrderDetailLines(1001L, rows);
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        Oe0020FieldAccess ws = capturedWs();
        assertThat(ws.getWkDcnt()).isEqualTo(200);
        assertThat(ws.getWkPageCnt()).isEqualTo(16);
        verify(orddf, times(201)).readNext();
    }

    @Test
    void execute_orderDetailScanStartInvalidKey_showsNoDetailLines() {
        doReturn(true).when(orddf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        Oe0020FieldAccess ws = capturedWs();
        assertThat(ws.getWkDcnt()).isEqualTo(0);
        assertThat(ws.getWkPageCnt()).isEqualTo(1);
    }

    @Test
    void execute_productLookupFails_showsPlaceholderProductName() {
        stubOrderDetailLines(1001L, List.of(line(10, 9999, 1, 100, 100, 0, 0)));
        doReturn(true).when(prodf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(capturedWs().getWwName(1).trim()).isEqualTo("??");
    }

    @Test
    void execute_customerLookupFailsDuringShowOrder_showsUnknownCustomerPlaceholder() {
        doReturn(true).when(custf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(custNameHistory).contains("??? unknown customer");
    }

    // ───────────────────────── order status mapping (ground truth: SET-STATUS-TEXT)
    // ─────────────────────────

    @Test
    void execute_orderStatusCancelled_showsCancelledText() {
        ordhf.getRecord().setInt("OH-STATUS", 9);
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(capturedWs().getWkStatText().trim()).isEqualTo("Cancelled");
    }

    @Test
    void execute_orderStatusUnrecognized_showsUnknownText() {
        // OH-STATUS is PIC 9(1) — 5 is a value COBOL/the buffer can hold that is not one
        // of the mapped codes (0,1,2,3,4,9), landing on the OTHER (Unknown) branch.
        ordhf.getRecord().setInt("OH-STATUS", 5);
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(capturedWs().getWkStatText().trim()).isEqualTo("Unknown");
    }

    // ───────────────────────── pagination (ground truth: PAGE-DOWN / PAGE-UP)
    // ─────────────────────────

    @Test
    void execute_pageDownWithMoreLines_advancesPage() {
        List<int[]> rows = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            rows.add(line(i, 7000 + i, 1, 100, 100, 0, 0));
        }
        stubOrderDetailLines(1001L, rows);
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        assertThat(capturedWs().getWkPageTop()).isEqualTo(14);
        assertThat(msgLineHistory).doesNotContain("Already at last page - PF6 for next order");
    }

    @Test
    void execute_pageDownAtLastPage_showsAlreadyAtLastPageMessage() {
        stubOrderDetailLines(1001L, List.of(line(10, 5001, 1, 100, 100, 0, 0)));
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Already at last page - PF6 for next order");
        assertThat(capturedWs().getWkPageTop()).isEqualTo(1);
    }

    @Test
    void execute_pageUpAtFirstPage_showsAlreadyAtFirstPageMessage() {
        when(renderer.readEndStatus()).thenReturn("00", "12", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Already at first page");
        assertThat(capturedWs().getWkPageTop()).isEqualTo(1);
    }

    @Test
    void execute_pageUpFromBeyondPageSize_subtractsPageSize() {
        List<int[]> rows = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            rows.add(line(i, 7000 + i, 1, 100, 100, 0, 0));
        }
        stubOrderDetailLines(1001L, rows);
        when(renderer.readEndStatus()).thenReturn("00", "00", "12", "03", "03");

        service.execute();

        assertThat(capturedWs().getWkPageTop()).isEqualTo(1);
    }

    // ───────────────────────── browse navigation (ground truth: NEXT-ORDER / PREV-ORDER)
    // ─────────────────────────

    @Test
    void execute_browseNextOrder_movesToNextLiveOrderAndPushesHistory() {
        doAnswer(
                        inv -> {
                            ordhf.getRecord().setLong("OH-NO", 2002L);
                            ordhf.getRecord().setInt("OH-DEL-FLAG", 0);
                            ordhf.getRecord().setInt("OH-CUST", 100);
                            ordhf.getRecord().setInt("OH-STATUS", 1);
                            return true;
                        })
                .when(ordhf)
                .readNext();
        doReturn(false).when(ordhf).isAtEnd();
        when(renderer.readEndStatus()).thenReturn("00", "06", "03", "03");

        service.execute();

        assertThat(curNoHistory).contains(2002L);
        assertThat(hcntHistory).contains(2);
        assertThat(msgLineHistory)
                .doesNotContain("No further orders", "Cannot reposition on current order");
    }

    @Test
    void execute_browseNextOrder_repositionFails_showsMessage() {
        doReturn(false, true).when(ordhf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "06", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Cannot reposition on current order");
    }

    @Test
    void execute_browseNextOrder_noFurtherOrders_showsMessage() {
        // ordhf.isAtEnd() stays true (default) — READ-NEXT-LIVE hits EOF inside NEXT-ORDER.
        when(renderer.readEndStatus()).thenReturn("00", "06", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("No further orders");
    }

    @Test
    void execute_browsePreviousOrder_atFirstHistoryPosition_showsMessage() {
        when(renderer.readEndStatus()).thenReturn("00", "05", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("No previous order in this browse");
    }

    @Test
    void execute_browsePreviousOrder_orderNoLongerAvailable_showsMessage() {
        doAnswer(
                        inv -> {
                            ordhf.getRecord().setLong("OH-NO", 2002L);
                            ordhf.getRecord().setInt("OH-DEL-FLAG", 0);
                            ordhf.getRecord().setInt("OH-CUST", 100);
                            return true;
                        })
                .when(ordhf)
                .readNext();
        doReturn(false).when(ordhf).isAtEnd();
        // 1st isInvalidKey: find-by-number (false); 2nd: NEXT-ORDER reposition (false);
        // 3rd: PREV-ORDER reposition on WH-NO(1) (true).
        doReturn(false, false, true).when(ordhf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "06", "05", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Previous order no longer available");
    }

    // ───────────────────────── function key handling (ground truth: MAIN-RTN / BROWSE-LOOP
    // EVALUATE) ─────────────────────────

    @Test
    void execute_invalidFunctionKeyAtMainScreen_showsMessage() {
        when(renderer.readEndStatus()).thenReturn("01", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Invalid function key");
    }

    @Test
    void execute_browseInvalidFunctionKey_showsPromptMessage() {
        when(renderer.readEndStatus()).thenReturn("00", "99", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("PF5/6 order  ENTER/PF12 page  PF3 back");
    }

    // ───────────────────────── file open lifecycle (ground truth: OPEN-FILES / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_orderFileOpenFails_abortsWithCompletionCode255() {
        doReturn("99").when(ordhf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(orddf, never()).open(any());
        verify(custf, never()).open(any());
        verify(prodf, never()).open(any());
    }
}

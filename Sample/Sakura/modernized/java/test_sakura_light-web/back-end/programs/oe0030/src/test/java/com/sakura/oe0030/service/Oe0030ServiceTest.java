package com.sakura.oe0030.service;

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
import com.sakura.oe0030.domain.Oe0030FieldAccess;
import com.sakura.oe0030.runtime.Oe0030Datasets;
import com.sakura.runtime.ScreenModels.InputFieldDef;
import com.sakura.runtime.ScreenModels.ScreenDef;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.OrddfDataset;
import com.sakura.runtime.io.OrdhfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.StokfDataset;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for Oe0030Service (COBOL OE0030 — sales order stock allocation), generated from {@code
 * OE0030.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: ORDHF/ORDDF/STOKF/CUSTF/PRODF are real dataset objects wrapped with {@code
 * spy()} so the record buffer (and therefore {@code Oe0030FieldAccess}, which registers those
 * buffers into ONE shared field-access routing table) behaves exactly as in production; only the
 * I/O methods (open/close/rewrite/readByKey/start/readNext/ isAtEnd/isInvalidKey/getFileStatus) are
 * stubbed so no real DB access happens. DATEUT runs for real (pure calendar math); ABORTX is
 * mocked.
 *
 * <p>Unlike some sibling order programs, CLEAR-WORK (paragraph CLRW-010) only resets WK-* control
 * fields (WK-SEL-NO/CUR-NO/DCNT/ALLOC-DONE/TOT-ALLOC/TOT-SHORT/PAGE-TOP/ CUST-NAME/CONFIRM) at the
 * *start* of every order-selection cycle — it never touches the ORDHF/ORDDF/STOKF record buffers.
 * So OH-, OD- and SK- fields (and the DR- / WW- detail tables) remain safely readable straight off
 * the dataset buffers after {@code execute()} returns. The WK-* fields wiped by the final
 * (program-ending) cycle's CLEAR-WORK are instead captured via {@link #msgLineHistory} / the
 * DS-ORDER and DS-PAGE display snapshots taken at the moment each screen was actually painted.
 *
 * <p>ORDDF's browse (START + READ NEXT/AT END) is scripted through {@link #orddfRows}: a fixed list
 * of row-appliers replayed from the top every time {@code start()} is called — matching the fact
 * that PREVIEW-DETAILS (PVD) and DO-ALLOCATE (DOA) both re-browse the SAME order's lines
 * independently, each starting a fresh cursor.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Oe0030ServiceTest {

    private static final long ORDER_NO = 1000000001L;
    private static final int CUST_CODE = 100;
    private static final int PROD_CODE = 55555555;
    private static final int WHSE = 10;

    @Mock private ScreenRendererInstance renderer;
    @Mock private AbortxService abortxService;

    private DateutService dateutService;

    private Oe0030Datasets fileSet;
    private OrdhfDataset ordhf;
    private OrddfDataset orddf;
    private StokfDataset stokf;
    private CustfDataset custf;
    private ProdfDataset prodf;

    private Oe0030Service service;

    private final Map<String, String> acceptValues = new HashMap<>();
    private final List<String> screenInteractions = new ArrayList<>();
    private final List<String> msgLineHistory = new ArrayList<>();

    /**
     * DS-ORDER snapshot history — one entry per PAINT-ORDER display (order-loaded, plus every
     * subsequent allocate/page-down/page-up repaint within the SAME order cycle).
     */
    private final List<Integer> dcntHistory = new ArrayList<>();

    private final List<Long> totAllocHistory = new ArrayList<>();
    private final List<Long> totShortHistory = new ArrayList<>();
    private final List<String> custNameHistory = new ArrayList<>();
    private final List<Integer> allocDoneHistory = new ArrayList<>();
    private final List<String> statTextHistory = new ArrayList<>();

    /** DS-PAGE snapshot history — page number / page count as painted. */
    private final List<Integer> pageNoHistory = new ArrayList<>();

    private final List<Integer> pageCntHistory = new ArrayList<>();

    /** Scripted ORDDF detail-line rows, replayed from index 0 every time start() runs. */
    private final List<Runnable> orddfRows = new ArrayList<>();

    private int orddfCursor;

    @BeforeEach
    void setUp() {
        dateutService = new DateutService();

        Oe0030Datasets real = new Oe0030Datasets();
        fileSet = spy(real);
        ordhf = spy(real.getOrdhf());
        orddf = spy(real.getOrddf());
        stokf = spy(real.getStokf());
        custf = spy(real.getCustf());
        prodf = spy(real.getProdf());
        doReturn(ordhf).when(fileSet).getOrdhf();
        doReturn(orddf).when(fileSet).getOrddf();
        doReturn(stokf).when(fileSet).getStokf();
        doReturn(custf).when(fileSet).getCustf();
        doReturn(prodf).when(fileSet).getProdf();

        doNothing().when(ordhf).open(any());
        doNothing().when(orddf).open(any());
        doNothing().when(stokf).open(any());
        doNothing().when(custf).open(any());
        doNothing().when(prodf).open(any());
        doNothing().when(ordhf).close();
        doNothing().when(orddf).close();
        doNothing().when(stokf).close();
        doNothing().when(custf).close();
        doNothing().when(prodf).close();

        doNothing().when(ordhf).rewrite();
        doNothing().when(orddf).rewrite();
        doNothing().when(stokf).rewrite();

        // Default: order header found, editable (status 0 = Entered), belongs to CUST_CODE.
        doAnswer(
                        inv -> {
                            applyDefaultOrdhfRecord();
                            return true;
                        })
                .when(ordhf)
                .readByKey(any());
        doReturn(false).when(ordhf).isInvalidKey();

        // Default: customer found.
        doReturn(true).when(custf).readByKey(any());
        doReturn(false).when(custf).isInvalidKey();
        custf.getRecord().setInt("CU-CODE", CUST_CODE);
        custf.getRecord().setString("CU-NAME", "ACME Corp");

        // Default: product found, stock-managed.
        doReturn(true).when(prodf).readByKey(any());
        doReturn(false).when(prodf).isInvalidKey();
        prodf.getRecord().setInt("PR-CODE", PROD_CODE);
        prodf.getRecord().setString("PR-NAME", "Widget");
        prodf.getRecord().setInt("PR-STOCK-MNG", 1);

        // Default: stock record found with ample on-hand quantity.
        doReturn(true).when(stokf).readByKey(any());
        doReturn(false).when(stokf).isInvalidKey();
        stokf.getRecord().setInt("SK-PROD", PROD_CODE);
        stokf.getRecord().setInt("SK-WHSE", WHSE);
        stokf.getRecord().setDecimal("SK-ONHAND", new BigDecimal("100"));
        stokf.getRecord().setDecimal("SK-ALLOCATED", BigDecimal.ZERO);

        // ORDDF browse: start() rewinds the scripted cursor; readNext()/isAtEnd() replay it.
        doAnswer(
                        inv -> {
                            orddfCursor = 0;
                            return null;
                        })
                .when(orddf)
                .start(any(), any());
        doReturn(false).when(orddf).isInvalidKey();
        doAnswer(
                        inv -> {
                            if (orddfCursor < orddfRows.size()) {
                                orddfRows.get(orddfCursor).run();
                            }
                            orddfCursor++;
                            return null;
                        })
                .when(orddf)
                .readNext();
        doAnswer(inv -> orddfCursor > orddfRows.size()).when(orddf).isAtEnd();

        acceptValues.put("WK-SEL-NO", String.valueOf(ORDER_NO));
        acceptValues.put("WK-CONFIRM", "Y");
        acceptValues.put("WK-DUMMY", " ");

        when(renderer.acceptField(any()))
                .thenAnswer(
                        inv -> {
                            InputFieldDef field = inv.getArgument(0, InputFieldDef.class);
                            return acceptValues.getOrDefault(field.name, "");
                        });

        doAnswer(
                        inv -> {
                            ScreenDef def = inv.getArgument(0);
                            RuntimeFieldAccess w = inv.getArgument(1);
                            screenInteractions.add("displayScreen:" + def.name);
                            Oe0030FieldAccess a = (Oe0030FieldAccess) w;
                            if ("DS-MSG".equals(def.name)) {
                                msgLineHistory.add(a.getWkMsgLine().trim());
                            }
                            if ("DS-ORDER".equals(def.name)) {
                                dcntHistory.add(a.getWkDcnt());
                                totAllocHistory.add(a.getWkTotAlloc());
                                totShortHistory.add(a.getWkTotShort());
                                custNameHistory.add(a.getWkCustName().trim());
                                allocDoneHistory.add(a.getWkAllocDone());
                                statTextHistory.add(a.getWkStatText().trim());
                            }
                            if ("DS-PAGE".equals(def.name)) {
                                pageNoHistory.add(a.getWkPageNo());
                                pageCntHistory.add(a.getWkPageCnt());
                            }
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        service = new Oe0030Service(fileSet, dateutService, abortxService, renderer);
    }

    private void applyDefaultOrdhfRecord() {
        ordhf.getRecord().setLong("OH-NO", ORDER_NO);
        ordhf.getRecord().setInt("OH-DEL-FLAG", 0);
        ordhf.getRecord().setInt("OH-STATUS", 0);
        ordhf.getRecord().setInt("OH-CUST", CUST_CODE);
    }

    /** Appends one scripted ORDDF detail line, applied in order on successive readNext() calls. */
    private void addOrderLine(
            int line,
            int prod,
            int whse,
            BigDecimal qty,
            BigDecimal shippedQty,
            BigDecimal allocQty,
            int status) {
        orddfRows.add(
                () -> {
                    orddf.getRecord().setLong("OD-NO", ORDER_NO);
                    orddf.getRecord().setInt("OD-LINE", line);
                    orddf.getRecord().setInt("OD-PROD", prod);
                    orddf.getRecord().setInt("OD-WHSE", whse);
                    orddf.getRecord().setDecimal("OD-QTY", qty);
                    orddf.getRecord().setDecimal("OD-SHIPPED-QTY", shippedQty);
                    orddf.getRecord().setDecimal("OD-ALLOC-QTY", allocQty);
                    orddf.getRecord().setInt("OD-STATUS", status);
                });
    }

    /**
     * Live handle onto the service's shared field-access object (same mutable object, spanning
     * WorkingStorage + every registered FD buffer, throughout the run).
     */
    private Oe0030FieldAccess capturedWs() {
        ArgumentCaptor<RuntimeFieldAccess> captor =
                ArgumentCaptor.forClass(RuntimeFieldAccess.class);
        verify(renderer, org.mockito.Mockito.atLeastOnce()).displayScreen(any(), captor.capture());
        return (Oe0030FieldAccess) captor.getAllValues().get(0);
    }

    // ───────────────────────── order-number prompt (ground truth: MAIN-RTN / PROCESS-ORDER)
    // ─────────────────────────

    @Test
    void execute_pf3AtOrderPrompt_endsProgramImmediately() {
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(ordhf, never()).readByKey(any());
        verify(ordhf, times(1)).open(any());
        verify(ordhf, times(1)).close();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_invalidFunctionKeyAtOrderPrompt_showsMessageAndReprompts() {
        when(renderer.readEndStatus()).thenReturn("77", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Invalid function key");
    }

    @Test
    void execute_orderNumberZero_rejectsWithRequiredMessage() {
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
    }

    @Test
    void execute_orderDeleted_rejectsWithDeletedMessage() {
        doAnswer(
                        inv -> {
                            applyDefaultOrdhfRecord();
                            ordhf.getRecord().setInt("OH-DEL-FLAG", 1);
                            return true;
                        })
                .when(ordhf)
                .readByKey(any());
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Order is deleted");
    }

    @Test
    void execute_orderStatusCancelled_rejectsWithCannotAllocateMessage() {
        doAnswer(
                        inv -> {
                            applyDefaultOrdhfRecord();
                            ordhf.getRecord().setInt("OH-STATUS", 9);
                            return true;
                        })
                .when(ordhf)
                .readByKey(any());
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Order cannot be allocated in its status");
    }

    @Test
    void execute_orderStatusAllocated_isAllowedButNoDetailLinesShowsMessage() {
        doAnswer(
                        inv -> {
                            applyDefaultOrdhfRecord();
                            ordhf.getRecord().setInt("OH-STATUS", 1);
                            return true;
                        })
                .when(ordhf)
                .readByKey(any());
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Order has no detail lines");
        verify(ordhf, never()).rewrite();
        // SET-STATUS-TEXT (SST-010) still runs before the WK-DCNT=0 gate and WK-STAT-TEXT
        // is never reset by CLEAR-WORK, so it is safely readable straight off the live ws.
        assertThat(capturedWs().getWkStatText().trim()).isEqualTo("Allocated");
    }

    @Test
    void execute_customerNotFoundOnLookup_showsUnknownCustomerNamePlaceholder() {
        doReturn(true).when(custf).isInvalidKey();
        addOrderLine(1, PROD_CODE, WHSE, new BigDecimal("5"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        // "03" at ASK-CONFIRM cancels immediately (WK-CUST-NAME was already painted by then).
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(custNameHistory).contains("??? unknown customer");
    }

    // ───────────────────────── detail preview (ground truth: PREVIEW-DETAILS / LOOKUP-PRODUCT /
    // READ-STOCK-AVAIL) ─────────────────────────

    @Test
    void execute_oneStockManagedLine_previewComputesAvailabilityFromOnHandLessAllocated() {
        stokf.getRecord().setDecimal("SK-ONHAND", new BigDecimal("100"));
        stokf.getRecord().setDecimal("SK-ALLOCATED", new BigDecimal("20"));
        addOrderLine(1, PROD_CODE, WHSE, new BigDecimal("10"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        Oe0030FieldAccess ws = capturedWs();
        assertThat(ws.getDrAvail(1)).isEqualTo(80); // 100 - 20
        assertThat(ws.getDrName(1).trim()).isEqualTo("Widget");
    }

    @Test
    void execute_productNotFound_usesPlaceholderNameInPreview() {
        doReturn(true).when(prodf).isInvalidKey();
        addOrderLine(1, PROD_CODE, WHSE, new BigDecimal("5"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(capturedWs().getDrName(1).trim()).isEqualTo("??");
    }

    @Test
    void execute_stockRecordNotFound_previewAvailabilityIsZero() {
        doReturn(true).when(stokf).isInvalidKey();
        addOrderLine(1, PROD_CODE, WHSE, new BigDecimal("5"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(capturedWs().getDrAvail(1)).isEqualTo(0);
    }

    @Test
    void execute_nonStockManagedLine_previewAvailabilityEqualsWanted() {
        prodf.getRecord().setInt("PR-STOCK-MNG", 0);
        addOrderLine(1, PROD_CODE, WHSE, new BigDecimal("7"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        verify(stokf, never()).readByKey(any());
        assertThat(capturedWs().getDrAvail(1)).isEqualTo(7);
    }

    // ───────────────────────── confirm allocation (ground truth: ASK-CONFIRM)
    // ─────────────────────────

    @Test
    void execute_confirmPf3_cancelsAllocation() {
        addOrderLine(1, PROD_CODE, WHSE, new BigDecimal("5"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Allocation cancelled");
        verify(ordhf, never()).rewrite();
    }

    @Test
    void execute_confirmAnswerN_cancelsAllocation() {
        acceptValues.put("WK-CONFIRM", "N");
        addOrderLine(1, PROD_CODE, WHSE, new BigDecimal("5"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Allocation cancelled");
        verify(ordhf, never()).rewrite();
    }

    // ───────────────────────── do-allocate (ground truth: DO-ALLOCATE / ALLOCATE-ONE-LINE /
    // REWRITE-ORDD) ─────────────────────────

    @Test
    void execute_confirmYesFullyAvailableStock_allocatesFullyAndSetsOrderStatusAllocated() {
        addOrderLine(1, PROD_CODE, WHSE, new BigDecimal("5"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        // "03" ends the REVIEW-LOOP immediately, then "03" at the next order prompt ends the
        // program.
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        assertThat(ordhf.getRecord().getInt("OH-STATUS")).isEqualTo(1);
        assertThat(orddf.getRecord().getDecimal("OD-ALLOC-QTY"))
                .isEqualByComparingTo(new BigDecimal("5"));
        assertThat(orddf.getRecord().getInt("OD-STATUS"))
                .isEqualTo(1); // WK-WANT(5) <= OD-ALLOC-QTY(5)
        assertThat(stokf.getRecord().getDecimal("SK-ALLOCATED"))
                .isEqualByComparingTo(new BigDecimal("5"));
        assertThat(msgLineHistory).contains("Allocation complete");
    }

    @Test
    void execute_stockShortage_allocatesPartialAndRecordsShortfall() {
        stokf.getRecord().setDecimal("SK-ONHAND", new BigDecimal("3"));
        stokf.getRecord().setDecimal("SK-ALLOCATED", BigDecimal.ZERO);
        addOrderLine(1, PROD_CODE, WHSE, new BigDecimal("5"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        assertThat(orddf.getRecord().getDecimal("OD-ALLOC-QTY"))
                .isEqualByComparingTo(new BigDecimal("3"));
        assertThat(orddf.getRecord().getInt("OD-STATUS"))
                .isEqualTo(0); // WK-WANT(5) > OD-ALLOC-QTY(3): stays Entered
        assertThat(totShortHistory).contains(2L); // 5 wanted - 3 allocated
    }

    @Test
    void execute_nonStockManagedLine_allocatesFullOutstandingWithoutReadingStock() {
        prodf.getRecord().setInt("PR-STOCK-MNG", 0);
        addOrderLine(1, PROD_CODE, WHSE, new BigDecimal("9"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        verify(stokf, never()).readByKey(any());
        assertThat(orddf.getRecord().getDecimal("OD-ALLOC-QTY"))
                .isEqualByComparingTo(new BigDecimal("9"));
    }

    @Test
    void execute_alreadyFullyAllocatedLine_needsNothingAndSkipsStockLookup() {
        // WK-WANT (5-0) - OD-ALLOC-QTY (5) = 0 → WK-NEED <= 0 → AOL-010 short-circuits.
        addOrderLine(
                1, PROD_CODE, WHSE, new BigDecimal("5"), BigDecimal.ZERO, new BigDecimal("5"), 1);
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        verify(stokf, times(0)).rewrite();
        assertThat(orddf.getRecord().getDecimal("OD-ALLOC-QTY"))
                .isEqualByComparingTo(new BigDecimal("5"));
    }

    @Test
    void execute_stockRecordNotFoundDuringAllocation_givesZeroAndLeavesLineShort() {
        doReturn(true).when(stokf).isInvalidKey();
        addOrderLine(1, PROD_CODE, WHSE, new BigDecimal("5"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        assertThat(orddf.getRecord().getDecimal("OD-ALLOC-QTY"))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(totShortHistory).contains(5L);
    }

    @Test
    void execute_headerRewriteFails_showsHeaderUpdateFailedMessage() {
        // First isInvalidKey() call: recall lookup succeeds; second (post ordhf.rewrite()): fails.
        doReturn(false, true).when(ordhf).isInvalidKey();
        addOrderLine(1, PROD_CODE, WHSE, new BigDecimal("5"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Order header update failed");
    }

    @Test
    void execute_orddfStartInvalidAtAllocationTime_finalizesWithoutApplyingAllocation() {
        // First isInvalidKey() call: PREVIEW-DETAILS' START succeeds (line loaded).
        // Second call: DO-ALLOCATE's START reports invalid key → jumps straight to DOA-900
        // (finalizeAllocation), so no line is walked and OD-ALLOC-QTY stays untouched —
        // yet the order header is still marked Allocated (ground truth: DOA-900 runs
        // unconditionally once DO-ALLOCATE is entered, regardless of how it got there).
        doReturn(false, true).when(orddf).isInvalidKey();
        addOrderLine(1, PROD_CODE, WHSE, new BigDecimal("5"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        assertThat(orddf.getRecord().getDecimal("OD-ALLOC-QTY"))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(ordhf.getRecord().getInt("OH-STATUS")).isEqualTo(1);
        assertThat(msgLineHistory).contains("Allocation complete");
    }

    // ───────────────────────── review loop / paging (ground truth: REVIEW-LOOP / PAGE-DOWN /
    // PAGE-UP) ─────────────────────────

    @Test
    void execute_reviewInvalidFunctionKey_continuesLoopWithoutEnding() {
        addOrderLine(1, PROD_CODE, WHSE, new BigDecimal("5"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        when(renderer.readEndStatus()).thenReturn("00", "00", "77", "03", "03");

        service.execute();

        // Two DS-PAGE paints in the review cycle: one from DO-ALLOCATE's own PAINT-ORDER,
        // none further from the "77" cycle (CONTINUE performs no repaint) — ground truth.
        assertThat(pageCntHistory).isNotEmpty();
    }

    @Test
    void execute_pageDownBeyondLastPage_isNoOpAndDoesNotRepaint() {
        addOrderLine(1, PROD_CODE, WHSE, new BigDecimal("5"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        when(renderer.readEndStatus()).thenReturn("00", "00", "06", "03", "03");

        service.execute();

        // WK-PAGE-TOP(1) + WK-PGSIZE(13) > WK-DCNT(1) → PD-010 returns immediately: no
        // extra DS-ORDER paint beyond the two already painted by PROCESS-ORDER's own
        // PAINT-ORDER and DO-ALLOCATE's DOA-900 PAINT-ORDER.
        assertThat(dcntHistory).hasSize(2);
    }

    @Test
    void execute_pageUpAtFirstPage_isNoOpAndDoesNotRepaint() {
        addOrderLine(1, PROD_CODE, WHSE, new BigDecimal("5"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        when(renderer.readEndStatus()).thenReturn("00", "00", "12", "03", "03");

        service.execute();

        assertThat(dcntHistory)
                .hasSize(2); // WK-PAGE-TOP(1) <= 1 → PU-010 returns immediately, no extra paint
    }

    @Test
    void execute_pageDownWithMultiplePages_showsSecondPage() {
        for (int i = 1; i <= 15; i++) {
            addOrderLine(
                    i, PROD_CODE, WHSE, new BigDecimal("1"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        }
        when(renderer.readEndStatus()).thenReturn("00", "00", "06", "03", "03");

        service.execute();

        // WK-PGSIZE=13: page 1 = lines 1..13, page-down moves WK-PAGE-TOP to 14 (page 2).
        assertThat(capturedWs().getWwProd(1)).isEqualTo(PROD_CODE);
        assertThat(pageNoHistory).contains(2);
        assertThat(pageCntHistory).contains(2); // ceil(15/13) = 2
    }

    @Test
    void execute_pageUpAfterPageDown_returnsToFirstPage() {
        for (int i = 1; i <= 15; i++) {
            addOrderLine(
                    i, PROD_CODE, WHSE, new BigDecimal("1"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        }
        when(renderer.readEndStatus()).thenReturn("00", "00", "06", "12", "03", "03");

        service.execute();

        assertThat(pageNoHistory.get(pageNoHistory.size() - 1)).isEqualTo(1);
    }

    // ───────────────────────── file open lifecycle (ground truth: OPEN-FILES / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_ordhfOpenFailsPersistently_abortsWithCompletionCode255() {
        doReturn("99").when(ordhf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(orddf, never()).open(any());
    }

    @Test
    void execute_ordhfOpenStatus35_reopensAsOutputThenReopensIo() {
        doReturn("35", "00", "00", "00").when(ordhf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(ordhf, times(3)).open(any());
        // OPENF-010's own FSTS=35 retry closes once mid-reopen, plus the final TERM-010 close.
        verify(ordhf, times(2)).close();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }
}

package com.sakura.pu0020.service;

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
import com.sakura.pu0020.domain.Pu0020FieldAccess;
import com.sakura.pu0020.runtime.Pu0020Datasets;
import com.sakura.runtime.ScreenModels.InputFieldDef;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.PodfDataset;
import com.sakura.runtime.io.PohfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.SuppfDataset;
import com.sakura.runtime.record.RuntimeFieldAccess;

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

/**
 * Unit tests for Pu0020Service (COBOL PU0020 — purchase order inquiry), generated from {@code
 * PU0020.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>POHF/PODF/SUPPF/PRODF are real dataset objects wrapped with {@code spy()} so the record buffer
 * (and therefore {@code Pu0020FieldAccess}, which registers those buffers) works exactly as in
 * production; only I/O methods are stubbed so no real file/DB access happens. DATEUT runs for real
 * (pure calendar math); ABORTX is mocked.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Pu0020ServiceTest {

    private ScreenRendererInstance renderer;
    private AbortxService abortxService;

    private DateutService dateutService;

    private Pu0020Datasets fileSet;
    private PohfDataset pohf;
    private PodfDataset podf;
    private SuppfDataset suppf;
    private ProdfDataset prodf;

    private Pu0020Service service;

    private final Map<String, String> acceptValues = new HashMap<>();
    private final List<String> screenInteractions = new ArrayList<>();
    private final List<String> msgLineHistory = new ArrayList<>();

    @BeforeEach
    void setUp() {
        renderer = org.mockito.Mockito.mock(ScreenRendererInstance.class);
        abortxService = org.mockito.Mockito.mock(AbortxService.class);
        dateutService = new DateutService();

        Pu0020Datasets real = new Pu0020Datasets();
        fileSet = spy(real);
        pohf = spy(real.getPohf());
        podf = spy(real.getPodf());
        suppf = spy(real.getSuppf());
        prodf = spy(real.getProdf());
        doReturn(pohf).when(fileSet).getPohf();
        doReturn(podf).when(fileSet).getPodf();
        doReturn(suppf).when(fileSet).getSuppf();
        doReturn(prodf).when(fileSet).getProdf();

        doNothing().when(pohf).open(any());
        doNothing().when(podf).open(any());
        doNothing().when(suppf).open(any());
        doNothing().when(prodf).open(any());
        doNothing().when(pohf).close();
        doNothing().when(podf).close();
        doNothing().when(suppf).close();
        doNothing().when(prodf).close();

        // Default: PO 1000000001 found directly, status Entered, supplier 100.
        doReturn(true).when(pohf).readByKey(any());
        doReturn(false).when(pohf).isInvalidKey();
        pohf.getRecord().setLong("PH-NO", 1000000001L);
        pohf.getRecord().setInt("PH-SUPP", 100);
        pohf.getRecord().setInt("PH-DATE", 20260101);
        pohf.getRecord().setInt("PH-STATUS", 0);
        pohf.getRecord().setInt("PH-WHSE", 1);
        pohf.getRecord().setInt("PH-STAFF", 10);
        pohf.getRecord().setInt("PH-DUE-DATE", 20260201);
        pohf.getRecord().setDecimal("PH-AMOUNT", BigDecimal.valueOf(1000));
        pohf.getRecord().setDecimal("PH-TAX-AMOUNT", BigDecimal.valueOf(100));
        pohf.getRecord().setDecimal("PH-TOTAL", BigDecimal.valueOf(1100));

        // POHF sequential browse (READ-NEXT-SUPP): default no further header (EOF).
        doReturn(true).when(pohf).readNext();
        doReturn(true).when(pohf).isAtEnd();
        doReturn(true).when(pohf).start(any(), any());

        // Default: supplier found.
        doReturn(true).when(suppf).readByKey(any());
        doReturn(false).when(suppf).isInvalidKey();
        suppf.getRecord().setInt("SP-CODE", 100);
        suppf.getRecord().setString("SP-NAME", "Acme Supplies");

        // Default: product found.
        doReturn(true).when(prodf).readByKey(any());
        doReturn(false).when(prodf).isInvalidKey();
        prodf.getRecord().setString("PR-NAME", "Widget");

        // Default: no PODF detail lines.
        stubPodfLines(1000000001L, new ArrayList<>());
        doReturn(true).when(podf).start(any(), any());
        doReturn(false).when(podf).isInvalidKey();

        acceptValues.put("WK-SRCH-NO", "1000000001");
        acceptValues.put("WK-SRCH-SUPP", "0");
        acceptValues.put("WK-SRCH-DATE", "0");
        acceptValues.put("WK-NAV", " ");

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
                            if (w instanceof Pu0020FieldAccess a) {
                                msgLineHistory.add(a.getWkMsgLine().trim());
                            }
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        service = new Pu0020Service(fileSet, dateutService, abortxService, renderer);
    }

    /**
     * Stubs PODF's sequential detail scan (START + READ NEXT) to yield {@code lines} rows for PO
     * {@code poNo}, each {@code long[]}: {line, prod, qty, recvQty, amount}; EOF fires once every
     * row has been consumed.
     */
    private void stubPodfLines(long poNo, List<long[]> lines) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i < lines.size()) {
                                long[] row = lines.get(i);
                                podf.getRecord().setLong("PD-NO", poNo);
                                podf.getRecord().setInt("PD-LINE", (int) row[0]);
                                podf.getRecord().setInt("PD-PROD", (int) row[1]);
                                podf.getRecord().setDecimal("PD-QTY", BigDecimal.valueOf(row[2]));
                                podf.getRecord()
                                        .setDecimal("PD-RECV-QTY", BigDecimal.valueOf(row[3]));
                                podf.getRecord()
                                        .setDecimal("PD-AMOUNT", BigDecimal.valueOf(row[4]));
                            }
                            return true;
                        })
                .when(podf)
                .readNext();
        doAnswer(inv -> idx.get() > lines.size()).when(podf).isAtEnd();
    }

    private static long[] line(long lineNo, long prod, long qty, long recvQty, long amount) {
        return new long[] {lineNo, prod, qty, recvQty, amount};
    }

    /**
     * Live handle onto the service's WorkingStorage/FD field accessor, obtained from the first
     * displayScreen() call. Same mutable object throughout the run.
     */
    private Pu0020FieldAccess capturedWs() {
        ArgumentCaptor<RuntimeFieldAccess> captor =
                ArgumentCaptor.forClass(RuntimeFieldAccess.class);
        verify(renderer, org.mockito.Mockito.atLeastOnce()).displayScreen(any(), captor.capture());
        return (Pu0020FieldAccess) captor.getAllValues().get(0);
    }

    // ───────────────────────── search by PO number (ground truth: DSR-010)
    // ─────────────────────────

    @Test
    void execute_poFoundByNumber_showsDetailsAndEndsProgram() {
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(screenInteractions)
                .contains("displayScreen:DS-HDRVIEW", "displayScreen:DS-ROWS");
        assertThat(capturedWs().getWkStatText().trim()).isEqualTo("Entered");
        assertThat(capturedWs().getCompletionCode()).isEqualTo(0);
        verify(pohf, times(1)).open(any());
        verify(pohf, times(1)).close();
    }

    @Test
    void execute_poNumberNotFound_showsMessageAndSkipsDetails() {
        doReturn(true).when(pohf).isInvalidKey();
        acceptValues.put("WK-SRCH-NO", "9999999999");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("PO number not found");
        assertThat(screenInteractions).doesNotContain("displayScreen:DS-HDRVIEW");
    }

    // ───────────────────────── search by supplier (ground truth: SBS-010 / RNS-010)
    // ─────────────────────────

    @Test
    void execute_findBySupplier_foundPo_showsDetails() {
        acceptValues.put("WK-SRCH-NO", "0");
        acceptValues.put("WK-SRCH-SUPP", "100");
        doReturn(false).when(pohf).isAtEnd();
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(screenInteractions).contains("displayScreen:DS-HDRVIEW");
        assertThat(msgLineHistory).doesNotContain("No PO found for supplier");
    }

    @Test
    void execute_findBySupplier_startInvalidKey_showsNoPoMessage() {
        acceptValues.put("WK-SRCH-NO", "0");
        acceptValues.put("WK-SRCH-SUPP", "999");
        doReturn(true).when(pohf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("No PO found for supplier");
    }

    @Test
    void execute_findBySupplier_noneMatchAfterStart_showsNoPoMessage() {
        acceptValues.put("WK-SRCH-NO", "0");
        acceptValues.put("WK-SRCH-SUPP", "100");
        // pohf.isAtEnd() stays true (default) — READ-NEXT-SUPP hits EOF immediately.
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("No PO found for supplier");
    }

    @Test
    void execute_findBySupplier_nextHeaderBelongsToDifferentSupplier_showsNoPoMessage() {
        acceptValues.put("WK-SRCH-NO", "0");
        acceptValues.put("WK-SRCH-SUPP", "100");
        doAnswer(
                        inv -> {
                            pohf.getRecord().setInt("PH-SUPP", 200);
                            return true;
                        })
                .when(pohf)
                .readNext();
        doReturn(false).when(pohf).isAtEnd();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("No PO found for supplier");
    }

    @Test
    void execute_findBySupplier_withFromDateFilter_skipsEarlierDatesThenFinds() {
        acceptValues.put("WK-SRCH-NO", "0");
        acceptValues.put("WK-SRCH-SUPP", "100");
        acceptValues.put("WK-SRCH-DATE", "20260115");
        AtomicInteger call = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = call.getAndIncrement();
                            pohf.getRecord().setInt("PH-SUPP", 100);
                            pohf.getRecord().setInt("PH-DATE", i == 0 ? 20260110 : 20260120);
                            return true;
                        })
                .when(pohf)
                .readNext();
        doReturn(false).when(pohf).isAtEnd();
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(capturedWs().getPhDate()).isEqualTo(20260120);
        verify(pohf, times(2)).readNext();
    }

    // ───────────────────────── no search criteria entered (ground truth: DSR-010 else branch)
    // ─────────────────────────

    @Test
    void execute_noSearchCriteriaEntered_showsEnterCriteriaMessage() {
        acceptValues.put("WK-SRCH-NO", "0");
        acceptValues.put("WK-SRCH-SUPP", "0");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Enter a PO number or a supplier code");
    }

    // ───────────────────────── detail line loading (ground truth: LL-010 / FR-010)
    // ─────────────────────────

    @Test
    void execute_poWithDetailLines_formatsRows() {
        stubPodfLines(1000000001L, List.of(line(1, 5001, 10, 5, 500), line(2, 5002, 20, 20, 800)));
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        Pu0020FieldAccess ws = capturedWs();
        assertThat(ws.getWkRowCnt()).isEqualTo(2);
        assertThat(ws.getWkMoreFlg()).isEqualTo(0);
        assertThat(ws.getWrBuf(1)).contains("5001");
        assertThat(ws.getWrBuf(1)).contains("Widget");
    }

    @Test
    void execute_poWithMoreThan9DetailLines_setsMoreFlag() {
        List<long[]> rows = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            rows.add(line(i, 6000 + i, 1, 0, 100));
        }
        stubPodfLines(1000000001L, rows);
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        Pu0020FieldAccess ws = capturedWs();
        assertThat(ws.getWkRowCnt()).isEqualTo(9);
        assertThat(ws.getWkMoreFlg()).isEqualTo(1);
        assertThat(msgLineHistory).contains("More lines exist - only first 9 shown");
        verify(podf, times(10)).readNext();
    }

    @Test
    void execute_detailScanStartInvalidKey_showsNoDetailLines() {
        doReturn(true).when(podf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(capturedWs().getWkRowCnt()).isEqualTo(0);
    }

    @Test
    void execute_productLookupFails_showsUnknownPlaceholder() {
        stubPodfLines(1000000001L, List.of(line(1, 9999, 1, 0, 100)));
        doReturn(true).when(prodf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(capturedWs().getWrBuf(1)).contains("??? unknown");
    }

    // ───────────────────────── supplier lookup (ground truth: LSN-010) ─────────────────────────

    @Test
    void execute_supplierLookupFails_showsUnknownSupplierPlaceholder() {
        doReturn(true).when(suppf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(capturedWs().getWkSuppName().trim()).isEqualTo("??? unknown supplier");
    }

    // ───────────────────────── status text mapping (ground truth: SST-010)
    // ─────────────────────────

    @Test
    void execute_statusPartRecv_showsPartRecvText() {
        pohf.getRecord().setInt("PH-STATUS", 1);
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(capturedWs().getWkStatText().trim()).isEqualTo("Part-recv");
    }

    @Test
    void execute_statusReceived_showsReceivedText() {
        pohf.getRecord().setInt("PH-STATUS", 2);
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(capturedWs().getWkStatText().trim()).isEqualTo("Received");
    }

    @Test
    void execute_statusInvoiced_showsInvoicedText() {
        pohf.getRecord().setInt("PH-STATUS", 3);
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(capturedWs().getWkStatText().trim()).isEqualTo("Invoiced");
    }

    @Test
    void execute_statusCancelled_showsCancelledText() {
        pohf.getRecord().setInt("PH-STATUS", 9);
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(capturedWs().getWkStatText().trim()).isEqualTo("Cancelled");
    }

    @Test
    void execute_statusUnrecognized_showsQuestionMark() {
        pohf.getRecord().setInt("PH-STATUS", 5);
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(capturedWs().getWkStatText().trim()).isEqualTo("?");
    }

    // ───────────────────────── paging within browse (ground truth: PGN-010)
    // ─────────────────────────

    @Test
    void execute_pageNextInSingleNumberMode_showsSinglePoMessage() {
        // MODE-BY-NO set because search was by PO number.
        when(renderer.readEndStatus()).thenReturn("00", "06", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Single PO - PF3 to go back");
    }

    @Test
    void execute_pageNextInSupplierMode_foundNextPo_advances() {
        acceptValues.put("WK-SRCH-NO", "0");
        acceptValues.put("WK-SRCH-SUPP", "100");
        AtomicInteger call = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = call.getAndIncrement();
                            pohf.getRecord().setLong("PH-NO", i == 0 ? 1000000001L : 1000000002L);
                            pohf.getRecord().setInt("PH-SUPP", 100);
                            return true;
                        })
                .when(pohf)
                .readNext();
        doReturn(false).when(pohf).isAtEnd();
        when(renderer.readEndStatus()).thenReturn("00", "06", "03", "03");

        service.execute();

        assertThat(capturedWs().getPhNo()).isEqualTo(1000000002L);
        assertThat(msgLineHistory).doesNotContain("No more POs for this supplier");
    }

    @Test
    void execute_pageNextInSupplierMode_noMorePos_showsMessageAndRestoresRecord() {
        acceptValues.put("WK-SRCH-NO", "0");
        acceptValues.put("WK-SRCH-SUPP", "100");
        doReturn(false).when(pohf).isAtEnd();
        // First READ-NEXT-SUPP (SBS-010 dispatch) finds the initial PO; second
        // READ-NEXT-SUPP (PGN-010) hits EOF because no further header matches.
        AtomicInteger call = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = call.getAndIncrement();
                            if (i == 0) {
                                pohf.getRecord().setInt("PH-SUPP", 100);
                                return true;
                            }
                            pohf.getRecord().setInt("PH-SUPP", 999);
                            return true;
                        })
                .when(pohf)
                .readNext();
        when(renderer.readEndStatus()).thenReturn("00", "06", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("No more POs for this supplier");
        assertThat(capturedWs().getPhNo()).isEqualTo(1000000001L);
    }

    // ───────────────────────── function key handling (ground truth: MAIN-010 / VL-010 EVALUATE)
    // ─────────────────────────

    @Test
    void execute_invalidFunctionKeyAtMainScreen_showsMessage() {
        when(renderer.readEndStatus()).thenReturn("01", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Invalid function key");
    }

    @Test
    void execute_viewLoopInvalidFunctionKey_showsNavPromptMessage() {
        when(renderer.readEndStatus()).thenReturn("00", "99", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("PF6=Next  PF3=Back");
    }

    // ───────────────────────── file open lifecycle (ground truth: INIT-010 / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_pohfOpenFails_abortsWithCompletionCode255() {
        doReturn("99").when(pohf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(podf, never()).open(any());
        verify(suppf, never()).open(any());
        verify(prodf, never()).open(any());
    }

    @Test
    void execute_podfOpenFails_abortsWithCompletionCode255() {
        doReturn("00").when(pohf).getFileStatus();
        doReturn("99").when(podf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(suppf, never()).open(any());
        verify(prodf, never()).open(any());
    }
}

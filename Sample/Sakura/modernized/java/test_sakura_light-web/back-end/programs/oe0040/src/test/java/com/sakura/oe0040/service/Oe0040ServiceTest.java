package com.sakura.oe0040.service;

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
import com.sakura.credit.service.CreditService;
import com.sakura.dateut.service.DateutService;
import com.sakura.oe0040.domain.Oe0040FieldAccess;
import com.sakura.oe0040.runtime.Oe0040Datasets;
import com.sakura.runtime.ScreenModels.InputFieldDef;
import com.sakura.runtime.ScreenModels.ScreenDef;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.OrddfDataset;
import com.sakura.runtime.io.OrdhfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.StokfDataset;
import com.sakura.runtime.linkage.CreditLinkParm;
import com.sakura.runtime.linkage.TaxcalLinkParm;
import com.sakura.runtime.record.RuntimeFieldAccess;
import com.sakura.taxcal.service.TaxcalService;

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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Oe0040Service (COBOL OE0040 — sales order maintenance), generated from {@code
 * OE0040.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: ORDHF/ORDDF/STOKF/CUSTF/PRODF are real dataset objects wrapped with {@code
 * spy()} so the record buffer (and therefore {@code Oe0040FieldAccess}, which registers those
 * buffers) behaves exactly as in production; only the I/O methods
 * (open/close/write/rewrite/delete/readByKey/start/readNext/isInvalidKey) are stubbed so no real DB
 * access happens. DATEUT runs for real (pure calendar math). CREDIT/TAXCAL/ABORTX are mocked — they
 * do their own file I/O irrelevant to OE0040's own logic.
 *
 * <p>Every {@code execute()} run drives the full COBOL control flow: the top-level MAIN-RTN loop
 * always re-enters {@code displayAndAcceptOrderSelection} once more after the maintenance loop ends
 * (to re-prompt for another order number), so every scenario's ESTS/command script ends with one
 * extra "03" to stop the program. WK-CMD / WK-CMD-ARG are scripted per DS-CMD cycle via {@link
 * #cmdQueue} / {@link #cmdArgQueue} (polled in order, falling back to blank/"0"), since a single
 * order-maintenance run issues several different commands, unlike a static field map.
 *
 * <p>CLEAR-ORDER (paragraph CLRO-010) unconditionally clears the ORDHF buffer at the *start* of
 * every order-selection cycle — including the final cycle that ends the program — so OH-* fields
 * are wiped by the time execute() returns. Values written to ORDHF/ORDDF/STOKF are therefore
 * captured via snapshots taken at the moment write()/rewrite() is invoked, not read back from the
 * buffer post-execute. WK-* working storage fields (not tied to any FD buffer) are unaffected and
 * safely readable via {@link #capturedWs()} after execute().
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Oe0040ServiceTest {

    private static final long ORDER_NO = 1000000001L;
    private static final int CUST_CODE = 100;
    private static final int PROD_CODE = 55555555;
    private static final int WHSE = 10;

    @Mock private ScreenRendererInstance renderer;
    @Mock private CreditService creditService;
    @Mock private TaxcalService taxcalService;
    @Mock private AbortxService abortxService;

    private DateutService dateutService;

    private Oe0040Datasets fileSet;
    private OrdhfDataset ordhf;
    private OrddfDataset orddf;
    private StokfDataset stokf;
    private CustfDataset custf;
    private ProdfDataset prodf;

    private Oe0040Service service;

    private final Map<String, String> acceptValues = new HashMap<>();
    private final Deque<String> cmdQueue = new ArrayDeque<>();
    private final Deque<String> cmdArgQueue = new ArrayDeque<>();

    /**
     * Scripted SC-CONF answers for scenarios with more than one Y/N dialog per run (falls back to
     * {@link #acceptValues}'s static "SC-CONF" entry when empty).
     */
    private final Deque<String> confirmQueue = new ArrayDeque<>();

    /**
     * Extra ORDHF field overrides applied after {@link #applyDefaultOrdhfRecord()} on every
     * readByKey() call (e.g. {@code ordhfCustomizer = () -> ordhf.getRecord().setInt("OH-DEL-FLAG",
     * 1);}). Needed because CLRO-010 clears the ORDHF buffer before every read — see readByKey
     * stub.
     */
    private Runnable ordhfCustomizer = () -> {};

    private final List<String> screenInteractions = new ArrayList<>();

    /**
     * WK-MSG-LINE trimmed at every DS-MSG display. CLRO-010 (clearOrderWorkArea) resets
     * WK-DIRTY/WK-DCNT/WK-ALLOCATED/WK-CANCEL-FLG/WK-CUST-NAME to defaults at the START of every
     * order-selection cycle — including the final cycle that ends the program, whose own "Enter the
     * order number to maintain" DS-MSG is therefore always the LAST entry here. So post-execute()
     * ws state for those fields is unusable; {@link #lastMaintValue} reads the value as of the last
     * cycle BEFORE that final reset instead.
     */
    private final List<String> msgLineHistory = new ArrayList<>();

    private final List<Integer> dcntHistory = new ArrayList<>();
    private final List<Integer> dirtyHistory = new ArrayList<>();
    private final List<Integer> allocatedHistory = new ArrayList<>();
    private final List<String> custNameHistory = new ArrayList<>();

    /**
     * Snapshot of OH-* at the moment ordhf.rewrite() is invoked (before next-cycle CLEAR-ORDER
     * wipes it).
     */
    private final Map<String, Object> ordhfRewriteSnapshot = new HashMap<>();

    /** One snapshot per orddf.write()/rewrite() call, in call order. */
    private final List<Map<String, Object>> orddfSaveSnapshots = new ArrayList<>();

    /** One snapshot per stokf.write()/rewrite() call, in call order. */
    private final List<Map<String, Object>> stokfSaveSnapshots = new ArrayList<>();

    @BeforeEach
    void setUp() {
        dateutService = new DateutService();

        Oe0040Datasets real = new Oe0040Datasets();
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
        doNothing().when(orddf).write();
        doNothing().when(orddf).rewrite();
        doNothing().when(orddf).delete();
        doReturn(false).when(orddf).start(any(), any());
        doNothing().when(stokf).write();
        doNothing().when(stokf).rewrite();

        // Default: no pre-existing detail lines for this order (LOAD-DETAILS gate skips the
        // browse).
        doReturn(true).when(orddf).isInvalidKey();

        // Default: order header found, editable (status 0 = Entered), belongs to CUST_CODE.
        // CLRO-010 (clearOrderWorkArea) clears the ORDHF buffer at the START of every
        // order-selection cycle, BEFORE this stub's readByKey() runs — so header fields must
        // be (re-)applied INSIDE the stub itself (ordhfCustomizer, run after the default
        // baseline on every call), not just once here at setup time, or they'd already be wiped.
        doAnswer(
                        inv -> {
                            applyDefaultOrdhfRecord();
                            ordhfCustomizer.run();
                            return true;
                        })
                .when(ordhf)
                .readByKey(any());
        doReturn(false).when(ordhf).isInvalidKey();

        // Default: customer found, active, no price rank, tax round 1.
        doReturn(true).when(custf).readByKey(any());
        doReturn(false).when(custf).isInvalidKey();
        custf.getRecord().setString("CU-NAME", "ACME Corp");
        custf.getRecord().setInt("CU-DEL-FLAG", 0);
        custf.getRecord().setInt("CU-PRICE-RANK", 0);
        custf.getRecord().setInt("CU-TAX-ROUND", 1);

        // Default: product found, active, stock-managed, default whse WHSE.
        doReturn(true).when(prodf).readByKey(any());
        doReturn(false).when(prodf).isInvalidKey();
        prodf.getRecord().setString("PR-NAME", "Widget");
        prodf.getRecord().setInt("PR-DEL-FLAG", 0);
        prodf.getRecord().setInt("PR-TAX-CATEGORY", 1);
        prodf.getRecord().setInt("PR-STOCK-MNG", 1);
        prodf.getRecord().setInt("PR-DFLT-WHSE", WHSE);
        prodf.getRecord().setDecimal("PR-LIST-PRICE", new BigDecimal("100.00"));

        // Default: stock record not found (ADJUST-ALLOC creates one on positive delta).
        doReturn(true).when(stokf).isInvalidKey();

        acceptValues.put("WK-SEL-ORD", String.valueOf(ORDER_NO));
        acceptValues.put("WK-D-PROD", String.valueOf(PROD_CODE));
        acceptValues.put("WK-D-WHSE", String.valueOf(WHSE));
        acceptValues.put("WK-D-QTY", "5");
        acceptValues.put("WK-D-PRICE", "150.00");
        acceptValues.put("SC-CONF", "Y");
        acceptValues.put("OH-DATE", "20260101");
        acceptValues.put("OH-CUST", String.valueOf(CUST_CODE));
        acceptValues.put("OH-STAFF", "1");
        acceptValues.put("OH-WHSE", String.valueOf(WHSE));
        acceptValues.put("OH-DUE-DATE", "20260115");
        acceptValues.put("OH-CUST-PO", "PO123");
        acceptValues.put("OH-TAX-TYPE", "1");
        acceptValues.put("OH-REMARK", "Remark");

        when(renderer.acceptField(any()))
                .thenAnswer(
                        inv -> {
                            InputFieldDef field = inv.getArgument(0, InputFieldDef.class);
                            if ("WK-CMD".equals(field.name)) {
                                return cmdQueue.isEmpty() ? " " : cmdQueue.poll();
                            }
                            if ("WK-CMD-ARG".equals(field.name)) {
                                return cmdArgQueue.isEmpty() ? "0" : cmdArgQueue.poll();
                            }
                            if ("SC-CONF".equals(field.name) && !confirmQueue.isEmpty()) {
                                return confirmQueue.poll();
                            }
                            return acceptValues.getOrDefault(field.name, "");
                        });

        doAnswer(
                        inv -> {
                            ScreenDef def = inv.getArgument(0);
                            RuntimeFieldAccess w = inv.getArgument(1);
                            screenInteractions.add("displayScreen:" + def.name);
                            if ("DS-MSG".equals(def.name) && w instanceof Oe0040FieldAccess a) {
                                msgLineHistory.add(a.getWkMsgLine().trim());
                                dcntHistory.add(a.getWkDcnt());
                                dirtyHistory.add(a.getWkDirty());
                                allocatedHistory.add(a.getWkAllocated());
                                custNameHistory.add(a.getWkCustName().trim());
                            }
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        // Default CREDIT: within limit, no warning.
        doAnswer(
                        inv -> {
                            CreditLinkParm p = inv.getArgument(0);
                            p.getKcred().setKcStatus("00");
                            p.getKcred().setKcExceed(0);
                            return null;
                        })
                .when(creditService)
                .execute(any());

        // Default TAXCAL: pass amount through untaxed (external logic out of scope for OE0040).
        doAnswer(
                        inv -> {
                            TaxcalLinkParm p = inv.getArgument(0);
                            BigDecimal amt = p.getKtax().getKtAmount();
                            p.getKtax().setKtNet(amt);
                            p.getKtax().setKtTax(BigDecimal.ZERO);
                            p.getKtax().setKtGross(amt);
                            p.getKtax().setKtStatus("00");
                            return null;
                        })
                .when(taxcalService)
                .execute(any());

        doAnswer(
                        inv -> {
                            Map<String, Object> snap = new HashMap<>();
                            snap.put("OH-STATUS", ordhf.getRecord().getInt("OH-STATUS"));
                            snap.put("OH-AMOUNT", ordhf.getRecord().getDecimal("OH-AMOUNT"));
                            snap.put("OH-TOTAL", ordhf.getRecord().getDecimal("OH-TOTAL"));
                            snap.put(
                                    "OH-TAX-AMOUNT", ordhf.getRecord().getDecimal("OH-TAX-AMOUNT"));
                            snap.put("OH-LINES", ordhf.getRecord().getInt("OH-LINES"));
                            ordhfRewriteSnapshot.clear();
                            ordhfRewriteSnapshot.putAll(snap);
                            return null;
                        })
                .when(ordhf)
                .rewrite();

        doAnswer(
                        inv -> {
                            orddfSaveSnapshots.add(snapshotOdRecord());
                            return null;
                        })
                .when(orddf)
                .write();
        doAnswer(
                        inv -> {
                            orddfSaveSnapshots.add(snapshotOdRecord());
                            return null;
                        })
                .when(orddf)
                .rewrite();

        doAnswer(
                        inv -> {
                            stokfSaveSnapshots.add(snapshotSkRecord());
                            return null;
                        })
                .when(stokf)
                .write();
        doAnswer(
                        inv -> {
                            stokfSaveSnapshots.add(snapshotSkRecord());
                            return null;
                        })
                .when(stokf)
                .rewrite();

        service =
                new Oe0040Service(
                        fileSet,
                        dateutService,
                        creditService,
                        taxcalService,
                        abortxService,
                        renderer);
    }

    private Map<String, Object> snapshotOdRecord() {
        Map<String, Object> snap = new HashMap<>();
        snap.put("OD-NO", orddf.getRecord().getLong("OD-NO"));
        snap.put("OD-LINE", orddf.getRecord().getInt("OD-LINE"));
        snap.put("OD-PROD", orddf.getRecord().getInt("OD-PROD"));
        snap.put("OD-WHSE", orddf.getRecord().getInt("OD-WHSE"));
        snap.put("OD-QTY", orddf.getRecord().getDecimal("OD-QTY"));
        snap.put("OD-UNIT-PRICE", orddf.getRecord().getDecimal("OD-UNIT-PRICE"));
        snap.put("OD-AMOUNT", orddf.getRecord().getDecimal("OD-AMOUNT"));
        snap.put("OD-ALLOC-QTY", orddf.getRecord().getDecimal("OD-ALLOC-QTY"));
        snap.put("OD-STATUS", orddf.getRecord().getInt("OD-STATUS"));
        return snap;
    }

    private void applyDefaultOrdhfRecord() {
        ordhf.getRecord().setLong("OH-NO", ORDER_NO);
        ordhf.getRecord().setInt("OH-DEL-FLAG", 0);
        ordhf.getRecord().setInt("OH-STATUS", 0);
        ordhf.getRecord().setDecimal("OH-TOTAL", BigDecimal.ZERO);
        ordhf.getRecord().setInt("OH-CUST", CUST_CODE);
        ordhf.getRecord().setInt("OH-TAX-TYPE", 1);
        ordhf.getRecord().setInt("OH-DATE", 20260101);
        ordhf.getRecord().setInt("OH-STAFF", 1);
        ordhf.getRecord().setInt("OH-WHSE", WHSE);
        ordhf.getRecord().setInt("OH-DUE-DATE", 20260115);
        ordhf.getRecord().setString("OH-CUST-PO", "PO123");
        ordhf.getRecord().setString("OH-REMARK", "Remark");
    }

    /** Queues a scripted WK-CMD sequence for successive DS-CMD accepts in one run. */
    private void cmd(String... cmds) {
        cmdQueue.addAll(java.util.List.of(cmds));
    }

    /**
     * Queues WK-CMD-ARG values aligned 1:1 with {@link #cmd}: WK-CMD-ARG is accepted on EVERY
     * DS-CMD cycle (not just C/D), so a command sequence like cmd("A","C") needs cmdArg("0","1") —
     * a placeholder for the cycle where the arg is unused.
     */
    private void cmdArg(String... args) {
        cmdArgQueue.addAll(java.util.List.of(args));
    }

    private Map<String, Object> snapshotSkRecord() {
        Map<String, Object> snap = new HashMap<>();
        snap.put("SK-PROD", stokf.getRecord().getInt("SK-PROD"));
        snap.put("SK-WHSE", stokf.getRecord().getInt("SK-WHSE"));
        snap.put("SK-ALLOCATED", stokf.getRecord().getDecimal("SK-ALLOCATED"));
        return snap;
    }

    /**
     * Live handle onto the service's WorkingStorage/FD field accessor (same mutable object
     * throughout the run) — safe for WK-* fields; OH-* fields are wiped by the final CLEAR-ORDER
     * cycle (see class javadoc).
     */
    private Oe0040FieldAccess capturedWs() {
        ArgumentCaptor<RuntimeFieldAccess> captor =
                ArgumentCaptor.forClass(RuntimeFieldAccess.class);
        verify(renderer, org.mockito.Mockito.atLeastOnce()).displayScreen(any(), captor.capture());
        return (Oe0040FieldAccess) captor.getAllValues().get(0);
    }

    /**
     * Reads {@code history} at the last index whose message wasn't the final reset cycle's "Enter
     * the order number to maintain" — i.e. the last real in-session value, before CLRO-010 wiped it
     * (see {@link #msgLineHistory} javadoc).
     */
    private <T> T lastMaintValue(List<T> history) {
        for (int i = msgLineHistory.size() - 1; i >= 0; i--) {
            if (!"Enter the order number to maintain".equals(msgLineHistory.get(i))) {
                return history.get(i);
            }
        }
        return history.get(history.size() - 1);
    }

    // ───────────────────────── order-selection prompt (ground truth: MAIN-RTN / RECALL-ORDER)
    // ─────────────────────────

    @Test
    void execute_pf3AtOrderNumberPrompt_endsProgramImmediately() {
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(ordhf, never()).readByKey(any());
        verify(ordhf, times(1)).open(any());
        verify(ordhf, times(1)).close();
        assertThat(capturedWs().getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_invalidFunctionKeyAtOrderPrompt_showsMessageAndReprompts() {
        when(renderer.readEndStatus()).thenReturn("77", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Invalid function key");
    }

    @Test
    void execute_orderNumberZero_rejectsWithOrderNumberRequiredMessage() {
        acceptValues.put("WK-SEL-ORD", "0");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(ordhf, never()).readByKey(any());
        assertThat(msgLineHistory).contains("Order number required");
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
        ordhfCustomizer = () -> ordhf.getRecord().setInt("OH-DEL-FLAG", 1);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Order is deleted");
    }

    @Test
    void execute_orderStatusShipped_rejectsWithCannotChangeMessage() {
        ordhfCustomizer = () -> ordhf.getRecord().setInt("OH-STATUS", 3);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Order status Shipped    - cannot change");
    }

    @Test
    void execute_orderStatusCancelled_rejectsWithCannotChangeMessage() {
        ordhfCustomizer = () -> ordhf.getRecord().setInt("OH-STATUS", 9);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Order status Cancelled  - cannot change");
    }

    @Test
    void execute_orderStatusAllocated_entersMaintenanceWithAllocatedFlagSet() {
        ordhfCustomizer = () -> ordhf.getRecord().setInt("OH-STATUS", 1);
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(lastMaintValue(allocatedHistory)).isEqualTo(1);
    }

    @Test
    void execute_customerNotFoundOnRecall_showsUnknownCustomerButStillEntersMaintenance() {
        doReturn(true).when(custf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(lastMaintValue(custNameHistory)).isEqualTo("(unknown customer)");
    }

    // ───────────────────────── maintenance loop / quit (ground truth: MAINTAIN-ORDER / QUIT-CHECK)
    // ─────────────────────────

    @Test
    void execute_quitWithNoChanges_endsMaintenanceImmediatelyWithoutConfirm() {
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        // "No changes - order unchanged" is set but never DISPLAYed in COBOL (QC-010 has no
        // DISPLAY DS-MSG on this branch) since MAINT-END fires immediately — matches ground truth.
        assertThat(msgLineHistory).contains("Order loaded - enter a command");
        verify(ordhf, never()).rewrite();
    }

    @Test
    void execute_invalidFunctionKeyInMaintenance_showsMessageAndContinues() {
        when(renderer.readEndStatus()).thenReturn("00", "88", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Invalid function key");
    }

    @Test
    void execute_pageDownWithNoLines_showsLastPageMessage() {
        when(renderer.readEndStatus()).thenReturn("00", "06", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Last page");
    }

    @Test
    void execute_pageUpAtFirstPage_showsFirstPageMessage() {
        when(renderer.readEndStatus()).thenReturn("00", "12", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("First page");
    }

    @Test
    void execute_invalidDispatchCommand_showsEnterCommandMessage() {
        cmd("Z");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Enter A C D H X or S");
    }

    // ───────────────────────── add line (ground truth: ADD-LINE-RTN / VALIDATE-NEW-LINE /
    // RESOLVE-PRICE) ─────────────────────────

    @Test
    void execute_addLineValid_addsLineAndMarksDirty() {
        cmd("A");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Line added");
        assertThat(lastMaintValue(dcntHistory)).isEqualTo(1);
        assertThat(lastMaintValue(dirtyHistory)).isEqualTo(1);
        assertThat(capturedWs().getDrAmt(1))
                .isEqualTo(750L); // 5 * 150.00 (DR-* array survives CLRO-010)
    }

    @Test
    void execute_addLineAbandoned_doesNotAddLine() {
        cmd("A");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Add abandoned");
        assertThat(capturedWs().getWkDcnt()).isEqualTo(0);
    }

    @Test
    void execute_addLineProductCodeZero_rejectsWithProductRequiredMessage() {
        cmd("A");
        acceptValues.put("WK-D-PROD", "0");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03");

        service.execute();

        verify(prodf, never()).readByKey(any());
        assertThat(msgLineHistory).contains("Product code required");
        assertThat(capturedWs().getWkDcnt()).isEqualTo(0);
    }

    @Test
    void execute_addLineProductNotFound_rejectsWithNotFoundMessage() {
        cmd("A");
        doReturn(true).when(prodf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Product not found");
    }

    @Test
    void execute_addLineProductDeleted_rejectsWithDeletedMessage() {
        cmd("A");
        prodf.getRecord().setInt("PR-DEL-FLAG", 1);
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Product is deleted");
    }

    @Test
    void execute_addLineQuantityZero_rejectsWithQuantityMessage() {
        cmd("A");
        acceptValues.put("WK-D-QTY", "0");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Quantity must be positive");
    }

    @Test
    void execute_addLineWarehouseZero_defaultsToProductDefaultWarehouse() {
        cmd("A");
        acceptValues.put("WK-D-WHSE", "0");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "03");

        service.execute();

        assertThat(capturedWs().getDrWhse(1)).isEqualTo(WHSE); // PR-DFLT-WHSE
    }

    @Test
    void execute_addLineZeroPriceUsesCustomerRankPrice() {
        cmd("A");
        custf.getRecord().setInt("CU-PRICE-RANK", 2);
        prodf.getRecord().setDecimal("PR-RANK-PRICE", new BigDecimal("90.00"), 2);
        acceptValues.put("WK-D-PRICE", "0");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "03");

        service.execute();

        Oe0040FieldAccess ws = capturedWs();
        assertThat(ws.getDrPrice(1)).isEqualByComparingTo(new BigDecimal("90.00"));
        assertThat(ws.getDrAmt(1)).isEqualTo(450L); // 5 * 90.00
    }

    @Test
    void execute_addLineZeroPriceAndNoRankFallsBackToListPrice() {
        cmd("A");
        acceptValues.put("WK-D-PRICE", "0");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "03");

        service.execute();

        Oe0040FieldAccess ws = capturedWs();
        assertThat(ws.getDrPrice(1))
                .isEqualByComparingTo(new BigDecimal("100.00")); // PR-LIST-PRICE
    }

    @Test
    void execute_addLineDcntAtMax_rejectsWithMaximumLinesMessage() {
        // Simulate the 200-line cap by loading 200 pre-existing detail lines via LOAD-DETAILS.
        doReturn(false).when(orddf).isInvalidKey();
        AtomicInteger call = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int n = call.incrementAndGet();
                            if (n <= 200) {
                                orddf.getRecord().setLong("OD-NO", ORDER_NO);
                                orddf.getRecord().setInt("OD-LINE", n);
                                orddf.getRecord().setInt("OD-PROD", PROD_CODE);
                                orddf.getRecord().setInt("OD-WHSE", WHSE);
                                orddf.getRecord().setDecimal("OD-QTY", BigDecimal.ONE);
                                orddf.getRecord()
                                        .setDecimal("OD-UNIT-PRICE", new BigDecimal("10.00"));
                                orddf.getRecord().setDecimal("OD-AMOUNT", BigDecimal.TEN);
                                orddf.getRecord().setInt("OD-TAX-CATEGORY", 1);
                                orddf.getRecord().setDecimal("OD-ALLOC-QTY", BigDecimal.ZERO);
                                orddf.getRecord().setInt("OD-STATUS", 0);
                            }
                            return true;
                        })
                .when(orddf)
                .readNext();
        AtomicInteger endCounter = new AtomicInteger(0);
        doAnswer(inv -> endCounter.incrementAndGet() > 200).when(orddf).isAtEnd();

        cmd("A");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        assertThat(lastMaintValue(dcntHistory)).isEqualTo(200);
        assertThat(msgLineHistory).contains("Maximum 200 lines reached");
    }

    // ───────────────────────── change line (ground truth: CHANGE-LINE-RTN)
    // ─────────────────────────

    @Test
    void execute_changeLineInvalidLineNumber_rejectsWithMessage() {
        cmd("C");
        cmdArgQueue.add("1");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Enter a valid line number");
    }

    @Test
    void execute_changeLineAbandoned_leavesLineUnchanged() {
        cmd("A", "C");
        cmdArg("0", "1");
        acceptValues.put("WK-D-QTY", "5");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Change abandoned");
        assertThat(capturedWs().getDrQty(1)).isEqualTo(5);
    }

    @Test
    void execute_changeLineQuantityZero_rejectsWithQuantityMessage() {
        cmd("A", "C");
        cmdArg("0", "1");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "00", "03", "03");
        acceptValues.put("WK-D-QTY", "0");

        service.execute();

        assertThat(msgLineHistory).contains("Quantity must be positive");
    }

    @Test
    void execute_changeLineNegativePrice_rejectsWithPriceMessage() {
        cmd("A", "C");
        cmdArg("0", "1");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "00", "03", "03");
        acceptValues.put("WK-D-QTY", "5");
        acceptValues.put("WK-D-PRICE", "-1");

        service.execute();

        assertThat(msgLineHistory).contains("Price cannot be negative");
    }

    @Test
    void execute_changeLineValid_updatesQuantityPriceAndAmount() {
        cmd("A", "C");
        cmdArg("0", "1");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "00", "03", "00", "03");
        acceptValues.put("WK-D-QTY", "8");
        acceptValues.put("WK-D-PRICE", "20.00");

        service.execute();

        Oe0040FieldAccess ws = capturedWs();
        assertThat(ws.getDrQty(1)).isEqualTo(8);
        assertThat(ws.getDrPrice(1)).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(ws.getDrAmt(1)).isEqualTo(160L);
        assertThat(msgLineHistory).contains("Line changed");
    }

    // ───────────────────────── delete line (ground truth: DELETE-LINE-RTN / COMPRESS-TABLE)
    // ─────────────────────────

    @Test
    void execute_deleteLineInvalidLineNumber_rejectsWithMessage() {
        cmd("D");
        cmdArgQueue.add("1");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Enter a valid line number");
    }

    @Test
    void execute_deleteNewlyAddedLine_compactsWithoutDeferredDelete() {
        cmd("A", "D");
        cmdArg("0", "1");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03", "00", "03");

        service.execute();

        Oe0040FieldAccess ws = capturedWs();
        assertThat(ws.getWkDcnt()).isEqualTo(0);
        assertThat(ws.getWkDelcnt()).isEqualTo(0); // DR-NEW=1 line: not queued for physical delete
        assertThat(msgLineHistory).contains("Line deleted");
    }

    // ───────────────────────── change header (ground truth: CHANGE-HEADER-RTN / VALIDATE-HEADER)
    // ─────────────────────────

    @Test
    void execute_changeHeaderAbandoned_reLooksUpCustomerAndShowsMessage() {
        cmd("H");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Header change abandoned");
        verify(custf, times(2)).readByKey(any()); // once on recall, once on abandon re-lookup
    }

    @Test
    void execute_changeHeaderCustomerCodeZero_rejectsWithCustomerRequiredMessage() {
        cmd("H");
        acceptValues.put("OH-CUST", "0");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Customer code required");
    }

    @Test
    void execute_changeHeaderCustomerNotFound_rejectsWithNotFoundMessage() {
        cmd("H");
        doReturn(false, true)
                .when(custf)
                .isInvalidKey(); // recall lookup ok, header validation fails
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Customer not found");
    }

    @Test
    void execute_changeHeaderCustomerDeleted_rejectsWithDeletedMessage() {
        cmd("H");
        acceptValues.put("OH-CUST", "200");
        custf.getRecord().setInt("CU-DEL-FLAG", 1);
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Customer is deleted");
    }

    @Test
    void execute_changeHeaderTaxTypeOutOfRange_rejectsWithTaxTypeMessage() {
        cmd("H");
        acceptValues.put("OH-TAX-TYPE", "5");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Tax type must be 1, 2 or 3");
    }

    @Test
    void execute_changeHeaderValid_marksDirtyAndShowsChangedMessage() {
        cmd("H");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Header changed");
        assertThat(lastMaintValue(dirtyHistory)).isEqualTo(1);
    }

    // ───────────────────────── cancel order (ground truth: CANCEL-ORDER-RTN / SAVE-ORDER)
    // ─────────────────────────

    @Test
    void execute_cancelOrderNotConfirmed_leavesOrderUnaffected() {
        cmd("X");
        acceptValues.put("SC-CONF", "N");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Cancel not confirmed");
        verify(ordhf, never()).rewrite();
    }

    @Test
    void execute_cancelOrderConfirmed_rewritesHeaderStatus9AndEndsMaintenance() {
        cmd("X");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(ordhf, times(1)).rewrite();
        // "Order cancelled" is set but never DISPLAYed in COBOL: CANCEL-ORDER-RTN sets
        // MAINT-END right after SAVE-ORDER, so no further PAINT-MAINT shows it — ground truth.
        assertThat(ordhfRewriteSnapshot.get("OH-STATUS")).isEqualTo(9);
    }

    // ───────────────────────── save order (ground truth: SAVE-CMD / SAVE-ORDER / SAVE-ONE-DETAIL)
    // ─────────────────────────

    @Test
    void execute_saveWithNoChanges_rejectsWithNothingToSaveMessage() {
        cmd("S");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Nothing changed - nothing to save");
        verify(ordhf, never()).rewrite();
    }

    @Test
    void execute_saveWithNoLinesRemaining_rejectsWithNoLinesMessage() {
        cmd("A", "D", "S");
        cmdArg("0", "1", "0");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("No lines - use X to cancel the order");
    }

    @Test
    void execute_saveConfirmNo_showsSaveCancelledMessage() {
        cmd("A", "S");
        // First confirm (Save) = N; second confirm (the follow-up Quit) = Y, so the run
        // terminates instead of looping forever on a stuck "N" answer.
        confirmQueue.add("N");
        confirmQueue.add("Y");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "00", "03", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Save cancelled");
        verify(ordhf, never()).rewrite();
    }

    @Test
    void execute_saveNewLineConfirmed_writesDetailAndRewritesHeader() {
        cmd("A", "S");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        verify(orddf, times(1)).write();
        verify(orddf, never()).rewrite();
        verify(ordhf, times(1)).rewrite();
        assertThat(orddfSaveSnapshots).hasSize(1);
        assertThat((BigDecimal) orddfSaveSnapshots.get(0).get("OD-AMOUNT"))
                .isEqualByComparingTo(BigDecimal.valueOf(750L));
        // "Order saved" is set but never DISPLAYed in COBOL: SAVE-CMD sets MAINT-END right
        // after SAVE-ORDER, so no further PAINT-MAINT shows it — ground truth, not a gap.
        assertThat(ordhfRewriteSnapshot.get("OH-STATUS")).isEqualTo(0);
        assertThat(capturedWs().getWkDirty()).isEqualTo(0);
    }

    @Test
    void execute_saveEnteredOrderNewStockManagedLine_createsNoAllocationRecord() {
        cmd("A", "S");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        // WK-ALLOCATED=0 (status Entered): target = DR-ALLOC (0 for a new line) → delta 0 → no
        // allocation write.
        verify(stokf, never()).write();
        assertThat(stokfSaveSnapshots).isEmpty();
    }

    @Test
    void execute_saveAllocatedOrderStockManagedLine_allocatesFullQuantity() {
        ordhfCustomizer = () -> ordhf.getRecord().setInt("OH-STATUS", 1);
        cmd("A", "S");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03", "03");

        service.execute();

        verify(stokf, times(1)).write(); // stock record not found by default → created
        assertThat(stokfSaveSnapshots).hasSize(1);
        assertThat((BigDecimal) stokfSaveSnapshots.get(0).get("SK-ALLOCATED"))
                .isEqualByComparingTo(new BigDecimal("5"));
    }

    @Test
    void execute_saveDetailWriteFails_showsWriteFailedMessage() {
        doReturn(true)
                .when(orddf)
                .isInvalidKey(); // gate: no existing lines; also fails the later write check
        cmd("A", "S");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Detail line write failed");
    }

    @Test
    void execute_saveExistingLineRewriteSucceeds_rewritesWithoutFallbackWrite() {
        // Load one pre-existing, allocated, stock-managed detail line via LOAD-DETAILS.
        doReturn(false, false)
                .when(orddf)
                .isInvalidKey(); // false=proceed to load; false=later rewrite succeeds
        AtomicInteger endCounter = new AtomicInteger(0);
        doAnswer(inv -> endCounter.incrementAndGet() > 1).when(orddf).isAtEnd();
        doAnswer(
                        inv -> {
                            orddf.getRecord().setLong("OD-NO", ORDER_NO);
                            orddf.getRecord().setInt("OD-LINE", 1);
                            orddf.getRecord().setInt("OD-PROD", PROD_CODE);
                            orddf.getRecord().setInt("OD-WHSE", WHSE);
                            orddf.getRecord().setDecimal("OD-QTY", new BigDecimal("5"));
                            orddf.getRecord().setDecimal("OD-UNIT-PRICE", new BigDecimal("100.00"));
                            orddf.getRecord().setDecimal("OD-AMOUNT", new BigDecimal("500"));
                            orddf.getRecord().setInt("OD-TAX-CATEGORY", 1);
                            orddf.getRecord().setDecimal("OD-ALLOC-QTY", new BigDecimal("5"));
                            orddf.getRecord().setInt("OD-STATUS", 0);
                            return true;
                        })
                .when(orddf)
                .readNext();

        // A no-op line edit is needed first: loading pre-existing lines does not set WK-DIRTY,
        // and SAVE-CMD refuses to save while WK-DIRTY = 0 (ground truth: SVC-010).
        cmd("C", "S");
        cmdArgQueue.add("1");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "00", "03");

        service.execute();

        verify(orddf, times(1)).rewrite();
        verify(orddf, never()).write();
        assertThat(orddfSaveSnapshots).hasSize(1);
        assertThat(msgLineHistory).doesNotContain("Detail rewrite failed");
    }

    @Test
    void execute_saveExistingLineRewriteFails_fallsBackToWriteAndReportsFailureIfThatFailsToo() {
        doReturn(false, true)
                .when(orddf)
                .isInvalidKey(); // false=proceed to load; true=rewrite fails, fallback write also
        // fails
        AtomicInteger endCounter = new AtomicInteger(0);
        doAnswer(inv -> endCounter.incrementAndGet() > 1).when(orddf).isAtEnd();
        doAnswer(
                        inv -> {
                            orddf.getRecord().setLong("OD-NO", ORDER_NO);
                            orddf.getRecord().setInt("OD-LINE", 1);
                            orddf.getRecord().setInt("OD-PROD", PROD_CODE);
                            orddf.getRecord().setInt("OD-WHSE", WHSE);
                            orddf.getRecord().setDecimal("OD-QTY", new BigDecimal("5"));
                            orddf.getRecord().setDecimal("OD-UNIT-PRICE", new BigDecimal("100.00"));
                            orddf.getRecord().setDecimal("OD-AMOUNT", new BigDecimal("500"));
                            orddf.getRecord().setInt("OD-TAX-CATEGORY", 1);
                            orddf.getRecord().setDecimal("OD-ALLOC-QTY", new BigDecimal("5"));
                            orddf.getRecord().setInt("OD-STATUS", 0);
                            return true;
                        })
                .when(orddf)
                .readNext();

        cmd("C", "S");
        cmdArgQueue.add("1");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "00", "03");

        service.execute();

        verify(orddf, times(1)).rewrite();
        verify(orddf, times(1)).write();
        assertThat(msgLineHistory).contains("Detail rewrite failed");
    }

    @Test
    void execute_deleteExistingAllocatedLineThenCancel_releasesStockAndDeletesRecord() {
        doReturn(false, false)
                .when(orddf)
                .isInvalidKey(); // false=proceed to load; false=delete succeeds
        AtomicInteger endCounter = new AtomicInteger(0);
        doAnswer(inv -> endCounter.incrementAndGet() > 1).when(orddf).isAtEnd();
        doAnswer(
                        inv -> {
                            orddf.getRecord().setLong("OD-NO", ORDER_NO);
                            orddf.getRecord().setInt("OD-LINE", 1);
                            orddf.getRecord().setInt("OD-PROD", PROD_CODE);
                            orddf.getRecord().setInt("OD-WHSE", WHSE);
                            orddf.getRecord().setDecimal("OD-QTY", new BigDecimal("5"));
                            orddf.getRecord().setDecimal("OD-UNIT-PRICE", new BigDecimal("100.00"));
                            orddf.getRecord().setDecimal("OD-AMOUNT", new BigDecimal("500"));
                            orddf.getRecord().setInt("OD-TAX-CATEGORY", 1);
                            orddf.getRecord().setDecimal("OD-ALLOC-QTY", new BigDecimal("5"));
                            orddf.getRecord().setInt("OD-STATUS", 0);
                            return true;
                        })
                .when(orddf)
                .readNext();
        // Stock exists with 5 allocated so the release delta (-5) is exercised.
        doReturn(false).when(stokf).isInvalidKey();
        stokf.getRecord().setDecimal("SK-ALLOCATED", new BigDecimal("5"));

        // Deleting the order's only line drops WK-DCNT to 0, and SAVE-CMD refuses to save with
        // zero lines (ground truth: SVC-010 "No lines - use X to cancel the order") — so this
        // scenario is driven through Cancel (X), which has no such guard.
        cmd("D", "X");
        cmdArgQueue.add("1");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        verify(orddf, times(1)).delete();
        verify(orddf, never()).write();
        verify(orddf, never()).rewrite();
        verify(stokf, times(1)).rewrite();
        assertThat((BigDecimal) stokfSaveSnapshots.get(0).get("SK-ALLOCATED"))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void execute_headerRewriteFails_showsHeaderRewriteFailedMessage() {
        doReturn(false, true)
                .when(ordhf)
                .isInvalidKey(); // recall lookup ok; header rewrite reports failure
        // orddf.isInvalidKey() stays at its default true (no pre-existing lines, LOAD-DETAILS
        // gate skips the browse) so the new line's write() also reports success (true would
        // route into the real, un-stubbed readNext()/isAtEnd() I/O path if set to false here).
        cmd("A", "S");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Header rewrite failed");
    }

    // ───────────────────────── credit check (ground truth: CREDIT-CHECK) ─────────────────────────

    @Test
    void execute_saveWithCreditLimitExceeded_showsWarningMessage() {
        doAnswer(
                        inv -> {
                            CreditLinkParm p = inv.getArgument(0);
                            p.getKcred().setKcStatus("00");
                            p.getKcred().setKcExceed(1);
                            return null;
                        })
                .when(creditService)
                .execute(any());
        cmd("A", "S");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Warning: customer credit limit exceeded");
    }

    @Test
    void execute_saveWithCreditStatusError_suppressesWarningEvenIfExceeded() {
        doAnswer(
                        inv -> {
                            CreditLinkParm p = inv.getArgument(0);
                            p.getKcred().setKcStatus("99");
                            p.getKcred().setKcExceed(1);
                            return null;
                        })
                .when(creditService)
                .execute(any());
        cmd("A", "S");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        assertThat(msgLineHistory).doesNotContain("Warning: customer credit limit exceeded");
    }

    @Test
    void execute_cancelOrderSkipsCreditCheck() {
        cmd("X");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(creditService, never()).execute(any());
    }

    // ───────────────────────── totals fallbacks (ground truth: COMPUTE-TOTALS)
    // ─────────────────────────

    @Test
    void execute_taxTypeOutOfRange_fallsBackToTaxType1ForTaxcal() {
        ordhfCustomizer = () -> ordhf.getRecord().setInt("OH-TAX-TYPE", 9);
        cmd("A", "S");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        ArgumentCaptor<TaxcalLinkParm> captor = ArgumentCaptor.forClass(TaxcalLinkParm.class);
        verify(taxcalService, org.mockito.Mockito.atLeastOnce()).execute(captor.capture());
        assertThat(captor.getValue().getKtax().getKtTaxType()).isEqualTo(1);
    }

    @Test
    void execute_taxRoundZero_fallsBackToTaxRound1ForTaxcal() {
        custf.getRecord().setInt("CU-TAX-ROUND", 0);
        cmd("A", "S");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "00", "03");

        service.execute();

        ArgumentCaptor<TaxcalLinkParm> captor = ArgumentCaptor.forClass(TaxcalLinkParm.class);
        verify(taxcalService, org.mockito.Mockito.atLeastOnce()).execute(captor.capture());
        assertThat(captor.getValue().getKtax().getKtRound()).isEqualTo(1);
    }

    // ───────────────────────── file open lifecycle (ground truth: OPEN-IO-ORDH / ABEND-RTN)
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
        // OIOH-010's own FSTS=35 retry path closes once mid-reopen, plus the final TERM-010 close.
        verify(ordhf, times(2)).close();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }
}

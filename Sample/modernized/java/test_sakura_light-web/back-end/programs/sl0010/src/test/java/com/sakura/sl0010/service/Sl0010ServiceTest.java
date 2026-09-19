package com.sakura.sl0010.service;

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
import com.sakura.runtime.io.ArlfDataset;
import com.sakura.runtime.io.CprcfDataset;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.io.InvdfDataset;
import com.sakura.runtime.io.InvhfDataset;
import com.sakura.runtime.io.OrdhfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.ShpdfDataset;
import com.sakura.runtime.io.ShphfDataset;
import com.sakura.runtime.io.StokfDataset;
import com.sakura.runtime.linkage.DateutLinkParm;
import com.sakura.runtime.linkage.NumgenLinkParm;
import com.sakura.runtime.linkage.TaxcalLinkParm;
import com.sakura.runtime.record.RuntimeFieldAccess;
import com.sakura.sl0010.domain.Sl0010FieldAccess;
import com.sakura.sl0010.runtime.Sl0010Datasets;
import com.sakura.taxcal.service.TaxcalService;

import org.junit.jupiter.api.AfterEach;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for Sl0010Service (COBOL SL0010 — sales/invoice entry), generated from {@code
 * SL0010.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>INVHF/INVDF/SHPHF/SHPDF/ORDHF/STOKF/CUSTF/PRODF/CPRCF/ARLF are real dataset objects wrapped
 * with {@code spy()} so the record buffer (and therefore {@code Sl0010FieldAccess}, which registers
 * those buffers) behaves exactly as in production; only I/O methods
 * (open/close/write/rewrite/readByKey/start/readNext/ getFileStatus/isInvalidKey) are stubbed so no
 * real file/DB access happens. DATEUT/TAXCAL/NUMGEN/ABORTX are mocked since their own internal
 * logic is covered by their own module's tests.
 *
 * <p>Several buffers (INVHF, INVDF, SHPHF, ARLF, CUSTF, ORDHF) are re-used/cleared by later
 * paragraphs within the same execute() run (e.g. CLRI-010 clears WK-CTL again at the start of the
 * next MAIN-RTN cycle before the program exits). To keep ground-truth assertions honest, values are
 * captured into *Snapshot maps at the moment write()/ rewrite() is invoked, before any later cycle
 * can overwrite them.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Sl0010ServiceTest {

    @org.mockito.Mock private ScreenRendererInstance renderer;
    @org.mockito.Mock private DateutService dateutService;
    @org.mockito.Mock private TaxcalService taxcalService;
    @org.mockito.Mock private NumgenService numgenService;
    @org.mockito.Mock private AbortxService abortxService;

    private Sl0010Datasets fileSet;
    private InvhfDataset invhf;
    private InvdfDataset invdf;
    private ShphfDataset shphf;
    private ShpdfDataset shpdf;
    private OrdhfDataset ordhf;
    private StokfDataset stokf;
    private CustfDataset custf;
    private ProdfDataset prodf;
    private CprcfDataset cprcf;
    private ArlfDataset arlf;

    private Sl0010Service service;

    private final Map<String, String> acceptValues = new HashMap<>();
    private final List<String> screenInteractions = new ArrayList<>();
    private final List<String> msgLineHistory = new ArrayList<>();
    private final Map<String, Object> invhfWriteSnapshot = new HashMap<>();
    private final Map<String, Object> invdfWriteSnapshots = new HashMap<>();
    private final Map<String, Object> arlfWriteSnapshot = new HashMap<>();
    private final Map<String, Object> custfRewriteSnapshot = new HashMap<>();
    private final Map<String, Object> shphfRewriteSnapshot = new HashMap<>();
    private final Map<String, Object> ordhfRewriteSnapshot = new HashMap<>();

    @BeforeEach
    void setUp() {
        Sl0010Datasets real = new Sl0010Datasets();
        fileSet = spy(real);
        invhf = spy(real.getInvhf());
        invdf = spy(real.getInvdf());
        shphf = spy(real.getShphf());
        shpdf = spy(real.getShpdf());
        ordhf = spy(real.getOrdhf());
        stokf = spy(real.getStokf());
        custf = spy(real.getCustf());
        prodf = spy(real.getProdf());
        cprcf = spy(real.getCprcf());
        arlf = spy(real.getArlf());
        doReturn(invhf).when(fileSet).getInvhf();
        doReturn(invdf).when(fileSet).getInvdf();
        doReturn(shphf).when(fileSet).getShphf();
        doReturn(shpdf).when(fileSet).getShpdf();
        doReturn(ordhf).when(fileSet).getOrdhf();
        doReturn(stokf).when(fileSet).getStokf();
        doReturn(custf).when(fileSet).getCustf();
        doReturn(prodf).when(fileSet).getProdf();
        doReturn(cprcf).when(fileSet).getCprcf();
        doReturn(arlf).when(fileSet).getArlf();

        for (var f :
                new Object[] {
                    invhf, invdf, shphf, shpdf, ordhf, stokf, custf, prodf, cprcf, arlf
                }) {
            doNothing().when((com.sakura.runtime.record.RawDatasetBase) f).open(any());
            doNothing().when((com.sakura.runtime.record.RawDatasetBase) f).close();
        }

        doAnswer(
                        inv -> {
                            invhfWriteSnapshot.put("IH-NO", invhf.getRecord().getLong("IH-NO"));
                            invhfWriteSnapshot.put("IH-CUST", invhf.getRecord().getInt("IH-CUST"));
                            invhfWriteSnapshot.put(
                                    "IH-STAFF", invhf.getRecord().getInt("IH-STAFF"));
                            invhfWriteSnapshot.put(
                                    "IH-SHIP-NO", invhf.getRecord().getLong("IH-SHIP-NO"));
                            invhfWriteSnapshot.put(
                                    "IH-CLOSE-YM", invhf.getRecord().getInt("IH-CLOSE-YM"));
                            invhfWriteSnapshot.put(
                                    "IH-TAX-TYPE", invhf.getRecord().getInt("IH-TAX-TYPE"));
                            invhfWriteSnapshot.put(
                                    "IH-AMOUNT", invhf.getRecord().getDecimal("IH-AMOUNT"));
                            invhfWriteSnapshot.put(
                                    "IH-TAX-AMOUNT", invhf.getRecord().getDecimal("IH-TAX-AMOUNT"));
                            invhfWriteSnapshot.put(
                                    "IH-TOTAL", invhf.getRecord().getDecimal("IH-TOTAL"));
                            invhfWriteSnapshot.put(
                                    "IH-COST-TOTAL", invhf.getRecord().getDecimal("IH-COST-TOTAL"));
                            invhfWriteSnapshot.put(
                                    "IH-STATUS", invhf.getRecord().getInt("IH-STATUS"));
                            invhfWriteSnapshot.put("IH-KIND", invhf.getRecord().getInt("IH-KIND"));
                            invhfWriteSnapshot.put(
                                    "IH-LINES", invhf.getRecord().getInt("IH-LINES"));
                            invhfWriteSnapshot.put(
                                    "IH-DEL-FLAG", invhf.getRecord().getInt("IH-DEL-FLAG"));
                            return null;
                        })
                .when(invhf)
                .write();

        doAnswer(
                        inv -> {
                            int line = invdf.getRecord().getInt("ID-LINE");
                            invdfWriteSnapshots.put(
                                    "ID-NO@" + line, invdf.getRecord().getLong("ID-NO"));
                            invdfWriteSnapshots.put(
                                    "ID-PROD@" + line, invdf.getRecord().getInt("ID-PROD"));
                            invdfWriteSnapshots.put(
                                    "ID-WHSE@" + line, invdf.getRecord().getInt("ID-WHSE"));
                            invdfWriteSnapshots.put(
                                    "ID-QTY@" + line, invdf.getRecord().getDecimal("ID-QTY"));
                            invdfWriteSnapshots.put(
                                    "ID-UNIT-PRICE@" + line,
                                    invdf.getRecord().getDecimal("ID-UNIT-PRICE"));
                            invdfWriteSnapshots.put(
                                    "ID-AMOUNT@" + line, invdf.getRecord().getDecimal("ID-AMOUNT"));
                            invdfWriteSnapshots.put(
                                    "ID-UNIT-COST@" + line,
                                    invdf.getRecord().getDecimal("ID-UNIT-COST"));
                            invdfWriteSnapshots.put(
                                    "ID-COST-AMOUNT@" + line,
                                    invdf.getRecord().getDecimal("ID-COST-AMOUNT"));
                            invdfWriteSnapshots.put(
                                    "ID-TAX-CATEGORY@" + line,
                                    invdf.getRecord().getInt("ID-TAX-CATEGORY"));
                            return null;
                        })
                .when(invdf)
                .write();

        doAnswer(
                        inv -> {
                            arlfWriteSnapshot.put("AL-SEQ", arlf.getRecord().getLong("AL-SEQ"));
                            arlfWriteSnapshot.put("AL-CUST", arlf.getRecord().getInt("AL-CUST"));
                            arlfWriteSnapshot.put("AL-KIND", arlf.getRecord().getInt("AL-KIND"));
                            arlfWriteSnapshot.put(
                                    "AL-REF-TYPE", arlf.getRecord().getInt("AL-REF-TYPE"));
                            arlfWriteSnapshot.put(
                                    "AL-REF-NO", arlf.getRecord().getLong("AL-REF-NO"));
                            arlfWriteSnapshot.put(
                                    "AL-DEBIT", arlf.getRecord().getDecimal("AL-DEBIT"));
                            arlfWriteSnapshot.put(
                                    "AL-CREDIT", arlf.getRecord().getDecimal("AL-CREDIT"));
                            arlfWriteSnapshot.put(
                                    "AL-BALANCE", arlf.getRecord().getDecimal("AL-BALANCE"));
                            return null;
                        })
                .when(arlf)
                .write();

        doAnswer(
                        inv -> {
                            custfRewriteSnapshot.put(
                                    "CU-BALANCE", custf.getRecord().getDecimal("CU-BALANCE"));
                            return null;
                        })
                .when(custf)
                .rewrite();

        doAnswer(
                        inv -> {
                            shphfRewriteSnapshot.put(
                                    "XH-STATUS", shphf.getRecord().getInt("XH-STATUS"));
                            return null;
                        })
                .when(shphf)
                .rewrite();

        doAnswer(
                        inv -> {
                            ordhfRewriteSnapshot.put(
                                    "OH-STATUS", ordhf.getRecord().getInt("OH-STATUS"));
                            return null;
                        })
                .when(ordhf)
                .rewrite();

        // Default: customer found, active, staff/tax-type/rank/round pre-set.
        doReturn(true).when(custf).readByKey(any());
        doReturn(false).when(custf).isInvalidKey();
        custf.getRecord().setString("CU-NAME", "ACME Corp");
        custf.getRecord().setInt("CU-DEL-FLAG", 0);
        custf.getRecord().setInt("CU-STAFF", 20);
        custf.getRecord().setInt("CU-TAX-TYPE", 1);
        custf.getRecord().setInt("CU-TAX-ROUND", 1);
        custf.getRecord().setInt("CU-PRICE-RANK", 0);
        custf.getRecord().setInt("CU-CLOSE-DAY", 0);
        custf.getRecord().setDecimal("CU-BALANCE", BigDecimal.ZERO);

        // Default: product found, active, standard cost 10.
        doReturn(true).when(prodf).readByKey(any());
        doReturn(false).when(prodf).isInvalidKey();
        prodf.getRecord().setString("PR-NAME", "Widget");
        prodf.getRecord().setInt("PR-DEL-FLAG", 0);
        prodf.getRecord().setInt("PR-TAX-CATEGORY", 1);
        prodf.getRecord().setInt("PR-DFLT-WHSE", 1);
        prodf.getRecord().setDecimal("PR-LIST-PRICE", new BigDecimal("50"));
        prodf.getRecord().setDecimal("PR-STD-COST", new BigDecimal("10"));

        // Default: no active contract price, no stock record (cost falls back to PR-STD-COST).
        doReturn(false).when(cprcf).readByKey(any());
        doReturn(true).when(cprcf).isInvalidKey();
        doReturn(false).when(stokf).readByKey(any());
        doReturn(true).when(stokf).isInvalidKey();
        doNothing().when(stokf).write();
        doNothing().when(stokf).rewrite();

        // Default: shipment found, active, not deleted, no linked order.
        doReturn(true).when(shphf).readByKey(any());
        doReturn(false).when(shphf).isInvalidKey();
        shphf.getRecord().setInt("XH-DEL-FLAG", 0);
        shphf.getRecord().setInt("XH-STATUS", 0);
        shphf.getRecord().setInt("XH-CUST", 100);
        shphf.getRecord().setInt("XH-STAFF", 20);
        shphf.getRecord().setInt("XH-DATE", 20260301);
        shphf.getRecord().setLong("XH-ORDER", 0L);

        // Default: SHPDF START finds the range, but no lines returned unless a test stubs readNext.
        doReturn(true).when(shpdf).start(any(), any());
        doReturn(false).when(shpdf).isInvalidKey();
        doReturn(true).when(shpdf).isAtEnd();

        // Default: order header found, status 3 (confirmed) so USS/UOS mark-invoiced path is
        // testable.
        doReturn(true).when(ordhf).readByKey(any());
        doReturn(false).when(ordhf).isInvalidKey();
        ordhf.getRecord().setInt("OH-STATUS", 3);

        // Header write/rewrite defaults succeed (no INVALID KEY) unless a test overrides.
        doReturn(false).when(invhf).isInvalidKey();
        doReturn(false).when(invdf).isInvalidKey();
        doReturn(false).when(arlf).isInvalidKey();

        acceptValues.put("WK-SEL-SHIP", "0");
        acceptValues.put("WK-SEL-CUST", "0");
        acceptValues.put("WK-STAFF-IN", "0");
        acceptValues.put("WK-TAXTYPE-IN", "0");
        acceptValues.put("WK-D-PROD", "500");
        acceptValues.put("WK-D-WHSE", "1");
        acceptValues.put("WK-D-QTY", "10");
        acceptValues.put("WK-D-PRICE", "25");
        acceptValues.put("WK-DUMMY", " ");
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
                            if (w instanceof Sl0010FieldAccess a) {
                                msgLineHistory.add(a.getWkMsgLine().trim());
                            }
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            p.getKdate().setKdDate1(20260315);
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        // Default TAXCAL: pass net amount through untouched (no tax) so totals are simple to
        // assert.
        doAnswer(
                        inv -> {
                            TaxcalLinkParm p = inv.getArgument(0);
                            BigDecimal amount = p.getKtax().getKtAmount();
                            p.getKtax().setKtNet(amount);
                            p.getKtax().setKtTax(BigDecimal.ZERO);
                            p.getKtax().setKtGross(amount);
                            return null;
                        })
                .when(taxcalService)
                .execute(any());

        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("00");
                            p.getKnum()
                                    .setKnumNumber(
                                            "INVOICE".equals(p.getKnum().getKnumKey().trim())
                                                    ? 9001L
                                                    : 9002L);
                            return null;
                        })
                .when(numgenService)
                .execute(any());

        service =
                new Sl0010Service(
                        fileSet,
                        dateutService,
                        taxcalService,
                        numgenService,
                        abortxService,
                        renderer);
    }

    @AfterEach
    void tearDown() {
        acceptValues.clear();
        screenInteractions.clear();
        msgLineHistory.clear();
        invhfWriteSnapshot.clear();
        invdfWriteSnapshots.clear();
        arlfWriteSnapshot.clear();
        custfRewriteSnapshot.clear();
        shphfRewriteSnapshot.clear();
        ordhfRewriteSnapshot.clear();
    }

    private BigDecimal invdfSnapshotDecimal(String key) {
        return (BigDecimal) invdfWriteSnapshots.get(key);
    }

    /** Live handle onto the service's WorkingStorage/FD field accessor. */
    private Sl0010FieldAccess capturedWs() {
        ArgumentCaptor<RuntimeFieldAccess> captor =
                ArgumentCaptor.forClass(RuntimeFieldAccess.class);
        verify(renderer, org.mockito.Mockito.atLeastOnce()).displayScreen(any(), captor.capture());
        return (Sl0010FieldAccess) captor.getAllValues().get(0);
    }

    /**
     * Sets up SHPDF to return exactly one shipment detail line on the first readNext(), then signal
     * end-of-file on the next.
     */
    private void stubOneShipmentLine() {
        AtomicInteger calls = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int n = calls.incrementAndGet();
                            if (n == 1) {
                                shpdf.getRecord().setLong("XD-NO", 500L);
                                shpdf.getRecord().setInt("XD-PROD", 500);
                                shpdf.getRecord().setInt("XD-WHSE", 1);
                                shpdf.getRecord().setDecimal("XD-QTY", new BigDecimal("10"));
                                shpdf.getRecord().setDecimal("XD-UNIT-PRICE", new BigDecimal("25"));
                                shpdf.getRecord().setDecimal("XD-UNIT-COST", BigDecimal.ZERO);
                                shpdf.getRecord().setLong("XD-ORDER", 0L);
                                shpdf.getRecord().setInt("XD-ORDER-LINE", 0);
                            }
                            return null;
                        })
                .when(shpdf)
                .readNext();
        doAnswer(inv -> calls.get() > 1).when(shpdf).isAtEnd();
    }

    // ───────────────────────── main menu dispatch (ground truth: MAIN-RTN)
    // ─────────────────────────

    @Test
    void execute_pf3AtMainMenu_endsImmediately() {
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(invhf, times(1)).open(any());
        verify(invhf, times(1)).close();
        assertThat(capturedWs().getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_invalidFunctionKey_showsMessageThenPf3Ends() {
        when(renderer.readEndStatus()).thenReturn("09", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Invalid function key");
        verify(invhf, never()).write();
    }

    @Test
    void execute_noShipOrCustomerEntered_showsMessage() {
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Enter shipment number or customer code");
        verify(shphf, never()).readByKey(any());
        verify(custf, never()).readByKey(any());
    }

    // ───────────────────────── invoice from shipment: happy path ─────────────────────────

    @Test
    void execute_shipmentHappyPathConfirmYes_postsInvoiceFromShipmentLines() {
        acceptValues.put("WK-SEL-SHIP", "500");
        stubOneShipmentLine();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(invhf, times(1)).write();
        verify(invdf, times(1)).write();
        verify(arlf, times(1)).write();
        verify(custf, times(1)).rewrite();
        verify(shphf, times(1)).rewrite();
        verify(cprcf, never())
                .readByKey(any()); // shipment lines carry XD-UNIT-PRICE, never resolved via CPRCF

        assertThat(invhfWriteSnapshot.get("IH-NO")).isEqualTo(9001L);
        assertThat(invhfWriteSnapshot.get("IH-CUST")).isEqualTo(100);
        assertThat(invhfWriteSnapshot.get("IH-STAFF")).isEqualTo(20);
        assertThat(invhfWriteSnapshot.get("IH-SHIP-NO")).isEqualTo(500L);
        assertThat(invhfWriteSnapshot.get("IH-TAX-TYPE"))
                .isEqualTo(1); // defaulted from CU-TAX-TYPE
        assertThat(invhfWriteSnapshot.get("IH-AMOUNT")).isEqualTo(new BigDecimal("250"));
        assertThat(invhfWriteSnapshot.get("IH-STATUS")).isEqualTo(1);
        assertThat(invhfWriteSnapshot.get("IH-KIND")).isEqualTo(1);
        assertThat(invhfWriteSnapshot.get("IH-LINES")).isEqualTo(1);
        assertThat(invhfWriteSnapshot.get("IH-DEL-FLAG")).isEqualTo(0);

        assertThat(invdfWriteSnapshots.get("ID-NO@1")).isEqualTo(9001L);
        assertThat(invdfWriteSnapshots.get("ID-PROD@1")).isEqualTo(500);
        assertThat(invdfSnapshotDecimal("ID-UNIT-PRICE@1"))
                .isEqualByComparingTo(new BigDecimal("25"));
        assertThat(invdfSnapshotDecimal("ID-AMOUNT@1")).isEqualByComparingTo(new BigDecimal("250"));
        assertThat(invdfSnapshotDecimal("ID-UNIT-COST@1"))
                .isEqualByComparingTo(new BigDecimal("10")); // PR-STD-COST fallback

        assertThat(arlfWriteSnapshot.get("AL-CUST")).isEqualTo(100);
        assertThat(arlfWriteSnapshot.get("AL-KIND")).isEqualTo(1);
        assertThat(arlfWriteSnapshot.get("AL-REF-TYPE")).isEqualTo(3);
        assertThat(arlfWriteSnapshot.get("AL-REF-NO")).isEqualTo(9001L);
        assertThat(arlfWriteSnapshot.get("AL-DEBIT")).isEqualTo(new BigDecimal("250"));

        assertThat(custfRewriteSnapshot.get("CU-BALANCE")).isEqualTo(new BigDecimal("250"));
        assertThat(shphfRewriteSnapshot.get("XH-STATUS")).isEqualTo(2);

        assertThat(msgLineHistory).contains("Invoice 0000009001 posted");
        assertThat(capturedWs().getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_shipmentWithLinkedOrderStatusConfirmed_marksOrderInvoiced() {
        acceptValues.put("WK-SEL-SHIP", "500");
        stubOneShipmentLine();
        shphf.getRecord().setLong("XH-ORDER", 777L);
        ordhf.getRecord().setInt("OH-STATUS", 3);
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(ordhf, times(1)).rewrite();
        assertThat(ordhfRewriteSnapshot.get("OH-STATUS")).isEqualTo(4);
    }

    @Test
    void execute_shipmentWithLinkedOrderStatusNotEligible_doesNotMarkOrderInvoiced() {
        acceptValues.put("WK-SEL-SHIP", "500");
        stubOneShipmentLine();
        shphf.getRecord().setLong("XH-ORDER", 777L);
        ordhf.getRecord().setInt("OH-STATUS", 1); // not 2 or 3
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(ordhf, never()).rewrite();
    }

    @Test
    void execute_shipmentWithNoLinkedOrder_skipsOrderLookup() {
        acceptValues.put("WK-SEL-SHIP", "500");
        stubOneShipmentLine();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(ordhf, never()).readByKey(any());
    }

    // ───────────────────────── invoice from shipment: rejection paths ─────────────────────────

    @Test
    void execute_shipmentNotFound_showsMessageAndDiscards() {
        acceptValues.put("WK-SEL-SHIP", "999");
        doReturn(true).when(shphf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Shipment not found");
        verify(invhf, never()).write();
    }

    @Test
    void execute_shipmentDeleted_showsMessageAndDiscards() {
        acceptValues.put("WK-SEL-SHIP", "500");
        shphf.getRecord().setInt("XH-DEL-FLAG", 1);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Shipment is deleted");
        verify(invhf, never()).write();
    }

    @Test
    void execute_shipmentAlreadyInvoiced_showsMessageAndDiscards() {
        acceptValues.put("WK-SEL-SHIP", "500");
        shphf.getRecord().setInt("XH-STATUS", 2);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Shipment is already invoiced");
        verify(invhf, never()).write();
    }

    @Test
    void execute_shipmentCancelled_showsMessageAndDiscards() {
        acceptValues.put("WK-SEL-SHIP", "500");
        shphf.getRecord().setInt("XH-STATUS", 9);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Shipment is cancelled");
        verify(invhf, never()).write();
    }

    @Test
    void execute_customerOfShipmentNotFound_showsMessageAndDiscards() {
        acceptValues.put("WK-SEL-SHIP", "500");
        doReturn(true).when(custf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Customer of shipment not found");
        verify(invhf, never()).write();
    }

    @Test
    void execute_shipmentHasNoLines_showsMessageAndDiscards() {
        acceptValues.put("WK-SEL-SHIP", "500");
        // default SHPDF stub: isAtEnd() true immediately -> WK-DCNT stays 0
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Shipment has no lines");
        verify(invhf, never()).write();
    }

    @Test
    void execute_shipmentStartInvalidKey_treatsAsNoLines() {
        acceptValues.put("WK-SEL-SHIP", "500");
        doReturn(true).when(shpdf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Shipment has no lines");
        verify(shpdf, never()).readNext();
    }

    // ───────────────────────── review loop (ground truth: REVIEW-LOOP) ─────────────────────────

    @Test
    void execute_reviewLoopCancel_discardsWithoutConfirmOrPost() {
        acceptValues.put("WK-SEL-SHIP", "500");
        stubOneShipmentLine();
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Invoice cancelled");
        verify(invhf, never()).write();
        assertThat(capturedWs().getWkFkeyLine().trim()).isEqualTo("ENTER=Next  PF3=End");
    }

    @Test
    void execute_reviewLoopPageDownThenConfirm_stillPostsInvoice() {
        acceptValues.put("WK-SEL-SHIP", "500");
        stubOneShipmentLine();
        // PAGE-DOWN with only 1 line and WK-PGSIZE=12 returns immediately (WK-PAGE-TOP+12 >
        // WK-DCNT).
        when(renderer.readEndStatus()).thenReturn("00", "06", "00", "00", "03");

        service.execute();

        verify(invhf, times(1)).write();
    }

    @Test
    void execute_reviewLoopPageUpThenConfirm_stillPostsInvoice() {
        acceptValues.put("WK-SEL-SHIP", "500");
        stubOneShipmentLine();
        // PAGE-UP with WK-PAGE-TOP already 1 returns immediately (WK-PAGE-TOP <= 1).
        when(renderer.readEndStatus()).thenReturn("00", "12", "00", "00", "03");

        service.execute();

        verify(invhf, times(1)).write();
    }

    @Test
    void execute_confirmDeclined_discardsInvoiceWithoutSaving() {
        acceptValues.put("WK-SEL-SHIP", "500");
        stubOneShipmentLine();
        acceptValues.put("WK-CONFIRM", "N");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(msgLineHistory).contains("Invoice discarded");
    }

    @Test
    void execute_confirmLowercaseY_stillPostsInvoice() {
        acceptValues.put("WK-SEL-SHIP", "500");
        stubOneShipmentLine();
        acceptValues.put("WK-CONFIRM", "y");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(invhf, times(1)).write();
    }

    // ───────────────────────── direct invoice: happy path (ground truth: INVOICE-DIRECT)
    // ─────────────────────────

    @Test
    void execute_directInvoiceHappyPath_postsInvoiceWithEnteredLine() {
        acceptValues.put("WK-SEL-CUST", "100");
        acceptValues.put(
                "WK-STAFF-IN",
                "77"); // EDH-010 unconditionally accepts WK-STAFF-IN (no CU-STAFF fallback)
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        verify(invhf, times(1)).write();
        verify(invdf, times(1)).write();
        verify(cprcf, never())
                .readByKey(any()); // WK-D-PRICE entered by user (25) skips price resolution

        assertThat(invhfWriteSnapshot.get("IH-CUST")).isEqualTo(100);
        assertThat(invhfWriteSnapshot.get("IH-STAFF"))
                .isEqualTo(77); // entered value, no CU-STAFF fallback for staff
        assertThat(invhfWriteSnapshot.get("IH-TAX-TYPE"))
                .isEqualTo(1); // defaulted from CU-TAX-TYPE (WK-TAXTYPE-IN=0 by default)
        assertThat(invdfWriteSnapshots.get("ID-PROD@1")).isEqualTo(500);
        assertThat(invdfSnapshotDecimal("ID-UNIT-PRICE@1"))
                .isEqualByComparingTo(new BigDecimal("25"));
        assertThat(invdfSnapshotDecimal("ID-AMOUNT@1")).isEqualByComparingTo(new BigDecimal("250"));
        assertThat(msgLineHistory).contains("Line added");
    }

    @Test
    void execute_directInvoiceCustomerNotFound_showsMessageWithoutEnteringHeader() {
        acceptValues.put("WK-SEL-CUST", "999");
        doReturn(true).when(custf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(screenInteractions).doesNotContain("displayScreen:DS-DHEAD");
    }

    @Test
    void execute_directInvoiceHeaderPf3Cancel_discardsWithoutEnteringDetail() {
        acceptValues.put("WK-SEL-CUST", "100");
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(screenInteractions).doesNotContain("displayScreen:DS-DETAIL");
    }

    @Test
    void execute_directInvoiceTaxTypeOutOfRange_rejectsHeaderWithMessage() {
        acceptValues.put("WK-SEL-CUST", "100");
        acceptValues.put("WK-TAXTYPE-IN", "5");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Tax type must be 1, 2 or 3");
        verify(invhf, never()).write();
    }

    @Test
    void execute_directInvoiceTaxTypeZeroDefaultsToCustomerTaxType() {
        acceptValues.put("WK-SEL-CUST", "100");
        acceptValues.put("WK-TAXTYPE-IN", "0");
        custf.getRecord().setInt("CU-TAX-TYPE", 2);
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        assertThat(invhfWriteSnapshot.get("IH-TAX-TYPE")).isEqualTo(2);
    }

    @Test
    void execute_directInvoiceLineClearedThenNoLines_discardsInvoice() {
        acceptValues.put("WK-SEL-CUST", "100");
        when(renderer.readEndStatus()).thenReturn("00", "00", "04", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Line cleared", "No lines entered - invoice discarded");
        verify(invhf, never()).write();
    }

    @Test
    void execute_directInvoiceInvalidDetailKey_showsMessageAndKeepsLoopOpen() {
        acceptValues.put("WK-SEL-CUST", "100");
        when(renderer.readEndStatus()).thenReturn("00", "00", "09", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Invalid key");
        verify(invhf, never()).write();
    }

    // ───────────────────────── direct invoice: detail line validation (ground truth:
    // PROCESS-DETAIL / LOOKUP-PRODUCT) ─────────────────────────

    @Test
    void execute_productCodeZero_rejectsDetailLineWithRequiredMessage() {
        acceptValues.put("WK-SEL-CUST", "100");
        acceptValues.put("WK-D-PROD", "0");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03");

        service.execute();

        verify(prodf, never()).readByKey(any());
        assertThat(msgLineHistory).contains("Product code required");
        verify(invhf, never()).write();
    }

    @Test
    void execute_productNotFound_rejectsDetailLineWithMessage() {
        acceptValues.put("WK-SEL-CUST", "100");
        doReturn(true).when(prodf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Product not found");
        verify(invhf, never()).write();
    }

    @Test
    void execute_productDeleted_rejectsDetailLineWithMessage() {
        acceptValues.put("WK-SEL-CUST", "100");
        prodf.getRecord().setInt("PR-DEL-FLAG", 1);
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Product not found");
        verify(invhf, never()).write();
    }

    @Test
    void execute_quantityNotPositive_rejectsDetailLineWithMessage() {
        acceptValues.put("WK-SEL-CUST", "100");
        acceptValues.put("WK-D-QTY", "0");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Quantity must be positive");
        verify(invhf, never()).write();
    }

    @Test
    void execute_warehouseZero_defaultsToProductDefaultWarehouse() {
        acceptValues.put("WK-SEL-CUST", "100");
        acceptValues.put("WK-D-WHSE", "0");
        prodf.getRecord().setInt("PR-DFLT-WHSE", 7);
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        assertThat(invdfWriteSnapshots.get("ID-WHSE@1")).isEqualTo(7);
    }

    // ───────────────────────── price resolution (ground truth: RESOLVE-PRICE / RANK-PRICE)
    // ─────────────────────────

    @Test
    void execute_priceZeroWithActiveContract_resolvesFromCustomerContractPrice() {
        acceptValues.put("WK-SEL-CUST", "100");
        acceptValues.put("WK-D-PRICE", "0");
        doReturn(true).when(cprcf).readByKey(any());
        doReturn(false).when(cprcf).isInvalidKey();
        cprcf.getRecord().setInt("CP-DEL-FLAG", 0);
        cprcf.getRecord().setInt("CP-START-DATE", 20260101);
        cprcf.getRecord().setInt("CP-END-DATE", 0);
        cprcf.getRecord().setDecimal("CP-PRICE", new BigDecimal("18"));
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        assertThat(invdfSnapshotDecimal("ID-UNIT-PRICE@1"))
                .isEqualByComparingTo(new BigDecimal("18"));
    }

    @Test
    void execute_priceZeroWithExpiredContract_fallsBackToRankPrice() {
        acceptValues.put("WK-SEL-CUST", "100");
        acceptValues.put("WK-D-PRICE", "0");
        doReturn(true).when(cprcf).readByKey(any());
        doReturn(false).when(cprcf).isInvalidKey();
        cprcf.getRecord().setInt("CP-DEL-FLAG", 0);
        cprcf.getRecord().setInt("CP-START-DATE", 20200101);
        cprcf.getRecord().setInt("CP-END-DATE", 20201231); // expired before OH-DATE/system date
        cprcf.getRecord().setDecimal("CP-PRICE", new BigDecimal("18"));
        custf.getRecord().setInt("CU-PRICE-RANK", 2);
        prodf.getRecord().setDecimal("PR-RANK-PRICE", new BigDecimal("30"), 2);
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        assertThat(invdfSnapshotDecimal("ID-UNIT-PRICE@1"))
                .isEqualByComparingTo(new BigDecimal("30"));
    }

    @Test
    void execute_priceZeroContractInvalidKey_fallsBackToRankOrListPrice() {
        acceptValues.put("WK-SEL-CUST", "100");
        acceptValues.put("WK-D-PRICE", "0");
        // default cprcf: isInvalidKey() true (no contract)
        custf.getRecord().setInt("CU-PRICE-RANK", 0);
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        assertThat(invdfSnapshotDecimal("ID-UNIT-PRICE@1"))
                .isEqualByComparingTo(new BigDecimal("50")); // PR-LIST-PRICE
    }

    @Test
    void execute_priceZeroRankOutOfRange_fallsBackDirectlyToListPrice() {
        acceptValues.put("WK-SEL-CUST", "100");
        acceptValues.put("WK-D-PRICE", "0");
        custf.getRecord().setInt("CU-PRICE-RANK", 9); // out of 1..5
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        assertThat(invdfSnapshotDecimal("ID-UNIT-PRICE@1"))
                .isEqualByComparingTo(new BigDecimal("50"));
    }

    // ───────────────────────── cost resolution (ground truth: RESOLVE-COST)
    // ─────────────────────────

    @Test
    void execute_stockRecordFound_usesAverageCostAsUnitCost() {
        acceptValues.put("WK-SEL-CUST", "100");
        doReturn(true).when(stokf).readByKey(any());
        doReturn(false).when(stokf).isInvalidKey();
        stokf.getRecord().setDecimal("SK-AVG-COST", new BigDecimal("12.5"));
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        assertThat(invdfSnapshotDecimal("ID-UNIT-COST@1"))
                .isEqualByComparingTo(new BigDecimal("12.5"));
        verify(prodf, times(1))
                .readByKey(any()); // only the LOOKUP-PRODUCT read; no fallback PRODF re-read needed
    }

    @Test
    void execute_stockRecordNotFound_fallsBackToProductStandardCost() {
        acceptValues.put("WK-SEL-CUST", "100");
        // default stokf: isInvalidKey() true (no stock record)
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        assertThat(invdfSnapshotDecimal("ID-UNIT-COST@1"))
                .isEqualByComparingTo(new BigDecimal("10")); // PR-STD-COST
        verify(prodf, times(2)).readByKey(any()); // LOOKUP-PRODUCT + RESOLVE-COST fallback read
    }

    // ───────────────────────── post-invoice failure paths (ground truth: POST-INVOICE / PAL / UCB)
    // ─────────────────────────

    @Test
    void execute_invoiceNumgenFails_showsMessageAndSkipsAllWrites() {
        acceptValues.put("WK-SEL-SHIP", "500");
        stubOneShipmentLine();
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

        verify(invhf, never()).write();
        verify(invdf, never()).write();
        verify(arlf, never()).write();
        assertThat(msgLineHistory).contains("Invoice number assignment failed");
    }

    @Test
    void execute_invhfWriteInvalidKey_showsMessageButStillWritesDetailsAndLedger() {
        acceptValues.put("WK-SEL-SHIP", "500");
        stubOneShipmentLine();
        AtomicBoolean invhfInvalidNow = new AtomicBoolean(false);
        doAnswer(inv -> invhfInvalidNow.get()).when(invhf).isInvalidKey();
        doAnswer(
                        inv -> {
                            invhfWriteSnapshot.put("IH-NO", invhf.getRecord().getLong("IH-NO"));
                            invhfInvalidNow.set(true);
                            return null;
                        })
                .when(invhf)
                .write();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Invoice header write failed");
        verify(invdf, times(1)).write();
        verify(arlf, times(1)).write();
    }

    @Test
    void execute_arLedgerNumgenFails_showsMessageAndSkipsArWriteButStillUpdatesBalance() {
        acceptValues.put("WK-SEL-SHIP", "500");
        stubOneShipmentLine();
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            if ("ARLDG".equals(p.getKnum().getKnumKey().trim())) {
                                p.getKnum().setKnumStatus("99");
                            } else {
                                p.getKnum().setKnumStatus("00");
                                p.getKnum().setKnumNumber(9001L);
                            }
                            return null;
                        })
                .when(numgenService)
                .execute(any());
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        verify(invhf, times(1)).write();
        verify(arlf, never()).write();
        verify(custf, times(1)).rewrite();
        assertThat(msgLineHistory).contains("AR ledger number assignment failed");
    }

    @Test
    void execute_arlfWriteInvalidKey_showsMessageButStillUpdatesCustomerBalance() {
        acceptValues.put("WK-SEL-SHIP", "500");
        stubOneShipmentLine();
        AtomicBoolean arlfInvalidNow = new AtomicBoolean(false);
        doAnswer(inv -> arlfInvalidNow.get()).when(arlf).isInvalidKey();
        doAnswer(
                        inv -> {
                            arlfWriteSnapshot.put("AL-SEQ", arlf.getRecord().getLong("AL-SEQ"));
                            arlfInvalidNow.set(true);
                            return null;
                        })
                .when(arlf)
                .write();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("AR ledger write failed");
        verify(custf, times(1)).rewrite();
    }

    @Test
    void execute_customerBalanceRewriteInvalidKey_showsMessage() {
        acceptValues.put("WK-SEL-SHIP", "500");
        stubOneShipmentLine();
        AtomicBoolean custfInvalidNow = new AtomicBoolean(false);
        doAnswer(inv -> custfInvalidNow.get()).when(custf).isInvalidKey();
        doAnswer(
                        inv -> {
                            custfRewriteSnapshot.put(
                                    "CU-BALANCE", custf.getRecord().getDecimal("CU-BALANCE"));
                            custfInvalidNow.set(true);
                            return null;
                        })
                .when(custf)
                .rewrite();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Customer balance update failed");
    }

    // ───────────────────────── file open lifecycle (ground truth: OPEN-FILES / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_invhfOpenFailsPersistently_abortsWithCompletionCode255() {
        doReturn("99").when(invhf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(invdf, never()).open(any());
        verify(shphf, never()).open(any());
    }

    @Test
    void execute_invhfOpenStatus35_reopensAsOutputThenReopensIo() {
        doReturn("35", "00", "00", "00").when(invhf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(invhf, times(3)).open(any());
        verify(invhf, times(1)).open(FileOpenMode.OUTPUT);
        verify(invhf, times(2)).open(FileOpenMode.IO);
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_ordhfOpenStatus35_reopensWithoutAbendEvenIfFinalStatusFails() {
        // ORDHF's OPEN-FILES retry has no post-retry FSTS check / ABEND — unlike
        // INVHF/INVDF/ARLF/SHPHF/CUSTF. A persistently-bad ORDHF status must not abort.
        doReturn("35", "00", "00", "99").when(ordhf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(ordhf, times(1)).open(FileOpenMode.OUTPUT);
        verify(ordhf, times(2)).open(FileOpenMode.IO);
        verify(abortxService, never()).execute(any());
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_arlfOpenFailsPersistently_abortsWithCompletionCode255() {
        doReturn("99").when(arlf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(shphf, never()).open(any());
    }
}

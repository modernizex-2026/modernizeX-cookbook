package com.sakura.pu0030.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import com.sakura.numgen.service.NumgenService;
import com.sakura.pu0030.domain.Pu0030FieldAccess;
import com.sakura.pu0030.runtime.Pu0030Datasets;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.ScreenModels;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.AplfDataset;
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.PurdfDataset;
import com.sakura.runtime.io.PurhfDataset;
import com.sakura.runtime.io.RcvdfDataset;
import com.sakura.runtime.io.RcvhfDataset;
import com.sakura.runtime.io.SuppfDataset;
import com.sakura.runtime.linkage.DateutLinkParm;
import com.sakura.runtime.linkage.NumgenLinkParm;
import com.sakura.runtime.linkage.TaxcalLinkParm;
import com.sakura.runtime.record.RawDatasetBase;
import com.sakura.runtime.record.RuntimeFieldAccess;
import com.sakura.taxcal.service.TaxcalService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for {@link Pu0030Service} (COBOL PU0030 — purchase entry: turns a completed
 * goods-receiving into an AP purchase transaction). Ground truth for inputs/expected values: {@code
 * PU0030.cob} PROCEDURE DIVISION (INIT-RTN / MAIN-RTN / PROCESS-RECV / SAVE-PURCHASE / POST-AP /
 * ...).
 *
 * <p>RCVHF/RCVDF/PURHF/PURDF/APLF/SUPPF/PRODF are real dataset objects wrapped with {@code spy()}
 * so the record buffer (and therefore {@code Pu0030FieldAccess}, which registers those buffers)
 * works exactly as in production; only I/O methods are stubbed so no real file/DB access happens.
 * DATEUT/TAXCAL/NUMGEN/ABORTX are mocked at the service boundary.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Pu0030ServiceTest {

    @Spy private RcvhfDataset rcvhf = new RcvhfDataset();
    @Spy private RcvdfDataset rcvdf = new RcvdfDataset();
    @Spy private PurhfDataset purhf = new PurhfDataset();
    @Spy private PurdfDataset purdf = new PurdfDataset();
    @Spy private AplfDataset aplf = new AplfDataset();
    @Spy private SuppfDataset suppf = new SuppfDataset();
    @Spy private ProdfDataset prodf = new ProdfDataset();

    @Mock private DateutService dateutService;
    @Mock private TaxcalService taxcalService;
    @Mock private NumgenService numgenService;
    @Mock private AbortxService abortxService;
    @Mock private ScreenRendererInstance renderer;

    private Pu0030Service service;
    private Pu0030FieldAccess ws;

    private final Map<String, String> acceptValues = new HashMap<>();
    private final List<String> screenInteractions = new ArrayList<>();
    private final List<String> msgLineHistory = new ArrayList<>();

    /** Subclass swapping in the spy datasets since Pu0030Datasets builds its own real files. */
    private static class TestDatasets extends Pu0030Datasets {
        private final RcvhfDataset rcvhf;
        private final RcvdfDataset rcvdf;
        private final PurhfDataset purhf;
        private final PurdfDataset purdf;
        private final AplfDataset aplf;
        private final SuppfDataset suppf;
        private final ProdfDataset prodf;

        TestDatasets(
                RcvhfDataset rcvhf,
                RcvdfDataset rcvdf,
                PurhfDataset purhf,
                PurdfDataset purdf,
                AplfDataset aplf,
                SuppfDataset suppf,
                ProdfDataset prodf) {
            this.rcvhf = rcvhf;
            this.rcvdf = rcvdf;
            this.purhf = purhf;
            this.purdf = purdf;
            this.aplf = aplf;
            this.suppf = suppf;
            this.prodf = prodf;
        }

        @Override
        public RcvhfDataset getRcvhf() {
            return rcvhf;
        }

        @Override
        public RcvdfDataset getRcvdf() {
            return rcvdf;
        }

        @Override
        public PurhfDataset getPurhf() {
            return purhf;
        }

        @Override
        public PurdfDataset getPurdf() {
            return purdf;
        }

        @Override
        public AplfDataset getAplf() {
            return aplf;
        }

        @Override
        public SuppfDataset getSuppf() {
            return suppf;
        }

        @Override
        public ProdfDataset getProdf() {
            return prodf;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Pu0030Datasets fileSet = new TestDatasets(rcvhf, rcvdf, purhf, purdf, aplf, suppf, prodf);
        service =
                new Pu0030Service(
                        fileSet,
                        dateutService,
                        taxcalService,
                        numgenService,
                        abortxService,
                        renderer);
        service.setRenderer(renderer);
        ws = getWs();

        for (RawDatasetBase spy :
                new RawDatasetBase[] {rcvhf, rcvdf, purhf, purdf, aplf, suppf, prodf}) {
            doNothing().when(spy).open(any());
            doNothing().when(spy).close();
            doNothing().when(spy).write();
            doNothing().when(spy).rewrite();
            doReturn("00").when(spy).getFileStatus();
            doReturn(false).when(spy).isInvalidKey();
            doReturn(true).when(spy).readByKey(any());
        }

        // Default receiving header: found, not deleted, not cancelled, not yet booked.
        rcvhf.getRecord().setLong("RH-NO", 1000000001L);
        rcvhf.getRecord().setInt("RH-SUPP", 500);
        rcvhf.getRecord().setInt("RH-DEL-FLAG", 0);
        rcvhf.getRecord().setInt("RH-STATUS", 0);

        // Default receiving-detail browse: no lines unless a test stubs otherwise (EOF
        // immediately).
        doReturn(true).when(rcvdf).start(any(), any());
        doReturn(true).when(rcvdf).readNext();
        doReturn(true).when(rcvdf).isAtEnd();

        // Default supplier: found.
        suppf.getRecord().setInt("SP-CODE", 500);
        suppf.getRecord().setString("SP-NAME", "Acme Supplies");
        suppf.getRecord().setInt("SP-TAX-TYPE", 0);
        suppf.getRecord().setDecimal("SP-BALANCE", BigDecimal.valueOf(1000));

        // Default product: found.
        prodf.getRecord().setString("PR-NAME", "Widget");
        prodf.getRecord().setInt("PR-TAX-CATEGORY", 1);

        acceptValues.put("WK-RECV-KEY", "1000000001");
        acceptValues.put("VH-DATE", "20260210");
        acceptValues.put("VH-TAX-TYPE", "2");
        acceptValues.put("WK-CONFIRM", "Y");

        doAnswer(
                        inv -> {
                            ScreenModels.InputFieldDef field = inv.getArgument(0);
                            return acceptValues.getOrDefault(field.name, "");
                        })
                .when(renderer)
                .acceptField(any());

        doAnswer(
                        inv -> {
                            ScreenModels.ScreenDef def = inv.getArgument(0);
                            RuntimeFieldAccess w = inv.getArgument(1);
                            screenInteractions.add("displayScreen:" + def.name);
                            if (w instanceof Pu0030FieldAccess a) {
                                msgLineHistory.add(a.getWkMsgLine().trim());
                            }
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            p.getKdate().setKdStatus("00");
                            p.getKdate().setKdDate1(20260115);
                            return null;
                        })
                .when(dateutService)
                .execute(any());

        doAnswer(
                        inv -> {
                            TaxcalLinkParm p = inv.getArgument(0);
                            BigDecimal net = p.getKtax().getKtAmount();
                            BigDecimal tax = net.multiply(BigDecimal.valueOf(0.1));
                            p.getKtax().setKtStatus("00");
                            p.getKtax().setKtNet(net);
                            p.getKtax().setKtTax(tax);
                            p.getKtax().setKtGross(net.add(tax));
                            return null;
                        })
                .when(taxcalService)
                .execute(any());

        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            String key = p.getKnum().getKnumKey().trim();
                            p.getKnum().setKnumStatus("00");
                            p.getKnum()
                                    .setKnumNumber("PURCH".equals(key) ? 500000001L : 700000001L);
                            return null;
                        })
                .when(numgenService)
                .execute(any());
    }

    @AfterEach
    void tearDown() {
        acceptValues.clear();
        screenInteractions.clear();
        msgLineHistory.clear();
    }

    /* ── reflection helpers ── */

    private Pu0030FieldAccess getWs() throws Exception {
        Field f = Pu0030Service.class.getDeclaredField("ws");
        f.setAccessible(true);
        return (Pu0030FieldAccess) f.get(service);
    }

    private void invokePrivate(String name) {
        try {
            Method m = Pu0030Service.class.getDeclaredMethod(name);
            m.setAccessible(true);
            m.invoke(service);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(cause);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stubs RCVDF's sequential detail scan (START + READ NEXT) to yield {@code lines} rows, each
     * {@code long[]}: {rdNo, prod, whse, qty, unitCostCents, amount}; EOF fires once every row has
     * been consumed.
     */
    private void stubRcvdfLines(List<long[]> lines) {
        AtomicInteger idx = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            int i = idx.getAndIncrement();
                            if (i < lines.size()) {
                                long[] row = lines.get(i);
                                rcvdf.getRecord().setLong("RD-NO", row[0]);
                                rcvdf.getRecord().setInt("RD-PROD", (int) row[1]);
                                rcvdf.getRecord().setInt("RD-WHSE", (int) row[2]);
                                rcvdf.getRecord().setDecimal("RD-QTY", BigDecimal.valueOf(row[3]));
                                rcvdf.getRecord()
                                        .setDecimal(
                                                "RD-UNIT-COST",
                                                BigDecimal.valueOf(row[4]).movePointLeft(2));
                                rcvdf.getRecord()
                                        .setDecimal("RD-AMOUNT", BigDecimal.valueOf(row[5]));
                            }
                            return true;
                        })
                .when(rcvdf)
                .readNext();
        doAnswer(inv -> idx.get() > lines.size()).when(rcvdf).isAtEnd();
    }

    private static long[] line(
            long rdNo, long prod, long whse, long qty, long unitCostCents, long amount) {
        return new long[] {rdNo, prod, whse, qty, unitCostCents, amount};
    }

    /* ── INIT-010 / OPENF-010 / O*-010 ── */

    @Test
    void initializeProgram_setsHeaderFieldsAndSysDate_opensAllFiles() {
        invokePrivate("initializeProgram");

        assertEquals("PU0030", ws.getWkProgid().trim());
        assertEquals("Purchase Entry", ws.getWkTitle().trim());
        assertEquals("ENTER=Next  PF3=End", ws.getWkFkeyLine().trim());
        assertEquals(20260115, ws.getWkSysymd());
        assertEquals(20260115, ws.getWkSysdate());
        assertEquals(202601, ws.getWkSysYm());
        verify(rcvhf).open(FileOpenMode.IO);
        verify(purhf).open(FileOpenMode.IO);
        verify(purdf).open(FileOpenMode.IO);
        verify(aplf).open(FileOpenMode.IO);
        verify(suppf).open(FileOpenMode.IO);
        verify(rcvdf).open(FileOpenMode.INPUT);
        verify(prodf).open(FileOpenMode.INPUT);
    }

    @Test
    void openReceivingHeaderFile_status35_reopensAsOutputThenIo() {
        doReturn("35", "00", "00").when(rcvhf).getFileStatus();

        invokePrivate("openReceivingHeaderFile");

        verify(rcvhf, times(2)).open(FileOpenMode.IO);
        verify(rcvhf, times(1)).open(FileOpenMode.OUTPUT);
        verify(rcvhf, times(1)).close();
    }

    @Test
    void openReceivingHeaderFile_errorStatus_abortsWithRcvhfFile() {
        doReturn("23").when(rcvhf).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openReceivingHeaderFile"));

        assertEquals("RCVHF", ws.getKaFile().trim());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    @Test
    void openAllFiles_purhfErrorStatus_abortsAndSkipsRemainingOpens() {
        doReturn("00").when(rcvhf).getFileStatus();
        doReturn("23").when(purhf).getFileStatus();

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("openAllFiles"));

        assertEquals("PURHF", ws.getKaFile().trim());
        verify(purdf, never()).open(any());
        verify(aplf, never()).open(any());
        verify(suppf, never()).open(any());
        verify(rcvdf, never()).open(any());
        verify(prodf, never()).open(any());
    }

    @Test
    void abortOnFileError_setsAbendFieldsAndThrows() {
        ws.setFsts("23");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("abortOnFileError"));

        assertEquals("PU0030", ws.getKaProgid().trim());
        assertEquals("23", ws.getKaFsts().trim());
        assertEquals("EOPEN ", ws.getKaMsgcode());
        assertEquals(255, ws.getCompletionCode());
        verify(abortxService).execute(any());
    }

    /* ── MAIN-RTN / MAIN-010 EVALUATE ── */

    @Test
    void runMainScreenCycle_recvKeyZero_showsRequiredMessage() {
        acceptValues.put("WK-RECV-KEY", "0");
        when(renderer.readEndStatus()).thenReturn("00");

        invokePrivate("runMainScreenCycle");

        assertTrue(msgLineHistory.contains("Receiving number required"));
    }

    @Test
    void runMainScreenCycle_ests03_setsEndFlag() {
        when(renderer.readEndStatus()).thenReturn("03");

        invokePrivate("runMainScreenCycle");

        assertEquals(1, ws.getEndFlg());
    }

    @Test
    void runMainScreenCycle_invalidFunctionKey_showsMessage() {
        when(renderer.readEndStatus()).thenReturn("01");

        invokePrivate("runMainScreenCycle");

        assertTrue(msgLineHistory.contains("Invalid function key"));
    }

    @Test
    void runMainProgram_endFlgOnFirstEntry_closesAllFilesAndThrowsProgramExitSignal() {
        when(renderer.readEndStatus()).thenReturn("03");

        assertThrows(ProgramExitSignal.class, () -> invokePrivate("runMainProgram"));

        assertEquals(1, ws.getEndFlg());
        verify(rcvhf).close();
        verify(rcvdf).close();
        verify(purhf).close();
        verify(purdf).close();
        verify(aplf).close();
        verify(suppf).close();
        verify(prodf).close();
    }

    /* ── CLRP-010 ── */

    @Test
    void clearWorkingFields_resetsWorkFieldsToDefaults() {
        ws.setWkSysdate(20260101);
        ws.setWkRecvKey(999);
        ws.setWkLcnt(5);
        ws.setWkNetTotal(999);
        ws.setRcvOk(1);
        ws.setWkMoreFlg(1);

        invokePrivate("clearWorkingFields");

        assertEquals(0, ws.getWkRecvKey());
        assertEquals(0, ws.getWkLcnt());
        assertEquals(0, ws.getWkNetTotal());
        assertEquals(0, ws.getWkTaxTotal());
        assertEquals(0, ws.getWkGrsTotal());
        assertEquals(0, ws.getRcvOk());
        assertEquals(0, ws.getWkMoreFlg());
        assertEquals(20260101, ws.getVhDate());
        assertEquals(1, ws.getVhTaxType());
    }

    /* ── PROCESS-RECV / PRC-010 ── */

    @Test
    void processReceivingBooking_recvKeyZero_showsMessageAndSkipsRead() {
        ws.setWkRecvKey(0);

        invokePrivate("processReceivingBooking");

        assertTrue(msgLineHistory.contains("Receiving number required"));
        verify(rcvhf, never()).readByKey(any());
    }

    @Test
    void processReceivingBooking_receivingNotFound_showsMessage() {
        ws.setWkRecvKey(1000000001L);
        doReturn(true).when(rcvhf).isInvalidKey();

        invokePrivate("processReceivingBooking");

        assertTrue(msgLineHistory.contains("Receiving not found"));
    }

    @Test
    void processReceivingBooking_receivingDeleted_showsMessage() {
        ws.setWkRecvKey(1000000001L);
        rcvhf.getRecord().setInt("RH-DEL-FLAG", 1);

        invokePrivate("processReceivingBooking");

        assertTrue(msgLineHistory.contains("Receiving is deleted"));
    }

    @Test
    void processReceivingBooking_receivingCancelled_showsMessage() {
        ws.setWkRecvKey(1000000001L);
        rcvhf.getRecord().setInt("RH-STATUS", 9);

        invokePrivate("processReceivingBooking");

        assertTrue(msgLineHistory.contains("Receiving is cancelled"));
    }

    @Test
    void processReceivingBooking_receivingAlreadyBooked_showsMessage() {
        ws.setWkRecvKey(1000000001L);
        rcvhf.getRecord().setInt("RH-STATUS", 1);

        invokePrivate("processReceivingBooking");

        assertTrue(msgLineHistory.contains("Receiving already booked"));
    }

    @Test
    void processReceivingBooking_noDetailLines_showsMessage() {
        ws.setWkRecvKey(1000000001L);
        // rcvdf.isAtEnd() defaults to true -> LOAD-RCV-LINES yields zero lines.

        invokePrivate("processReceivingBooking");

        assertTrue(msgLineHistory.contains("No detail lines on this receiving"));
        assertEquals(0, ws.getRcvOk());
    }

    @Test
    void processReceivingBooking_cancelAtVoucherHeader_returnsWithoutTaxOrSave() {
        ws.setWkRecvKey(1000000001L);
        stubRcvdfLines(List.of(line(1000000001L, 5001, 1, 10, 250, 2500)));
        when(renderer.readEndStatus()).thenReturn("03");

        invokePrivate("processReceivingBooking");

        assertTrue(msgLineHistory.contains("Cancelled"));
        assertEquals(0, ws.getRcvOk());
        verify(taxcalService, never()).execute(any());
        verify(purhf, never()).write();
    }

    @Test
    void processReceivingBooking_happyPath_computesTaxAndBooksPurchase() {
        ws.setWkRecvKey(1000000001L);
        stubRcvdfLines(List.of(line(1000000001L, 5001, 1, 10, 250, 2500)));
        when(renderer.readEndStatus()).thenReturn("00", "00");

        invokePrivate("processReceivingBooking");

        assertEquals(1, ws.getRcvOk());
        assertTrue(msgLineHistory.contains("Purchase booked"));
        verify(purhf, times(1)).write();
        verify(purdf, times(1)).write();
        verify(aplf, times(1)).write();
        verify(suppf, times(1)).rewrite();
        verify(rcvhf, times(1)).rewrite();
    }

    /* ── LOOKUP-SUPP-NAME / LSN-010 ── */

    @Test
    void loadSupplierName_found_setsNameAndOverridesTaxType() {
        ws.setRhSupp(500);
        suppf.getRecord().setInt("SP-TAX-TYPE", 3);
        ws.setVhTaxType(1);

        invokePrivate("loadSupplierName");

        assertEquals("Acme Supplies", ws.getWkSuppName().trim());
        assertEquals(3, ws.getVhTaxType());
    }

    @Test
    void loadSupplierName_notFound_setsUnknownPlaceholder() {
        ws.setRhSupp(500);
        doReturn(true).when(suppf).isInvalidKey();

        invokePrivate("loadSupplierName");

        assertEquals("??? unknown supplier", ws.getWkSuppName().trim());
    }

    @Test
    void loadSupplierName_taxTypeZero_keepsExistingVhTaxType() {
        ws.setRhSupp(500);
        suppf.getRecord().setInt("SP-TAX-TYPE", 0);
        ws.setVhTaxType(2);

        invokePrivate("loadSupplierName");

        assertEquals(2, ws.getVhTaxType());
    }

    /* ── LOAD-RCV-LINES / LRL-010 ── */

    @Test
    void loadReceivingLines_startInvalidKey_yieldsZeroLines() {
        ws.setRhNo(1000000001L);
        doReturn(true).when(rcvdf).isInvalidKey();

        invokePrivate("loadReceivingLines");

        assertEquals(0, ws.getWkLcnt());
    }

    @Test
    void loadReceivingLines_multipleLinesSameReceiving_accumulatesTotal() {
        ws.setRhNo(1000000001L);
        stubRcvdfLines(
                List.of(
                        line(1000000001L, 5001, 1, 10, 250, 2500),
                        line(1000000001L, 5002, 2, 20, 800, 16000)));

        invokePrivate("loadReceivingLines");

        assertEquals(2, ws.getWkLcnt());
        assertEquals(18500L, ws.getWkNetTotal());
    }

    @Test
    void loadReceivingLines_nextHeaderBelongsToDifferentReceiving_stopsAtBoundary() {
        ws.setRhNo(1000000001L);
        stubRcvdfLines(List.of(line(1000000002L, 5001, 1, 10, 250, 2500)));

        invokePrivate("loadReceivingLines");

        assertEquals(0, ws.getWkLcnt());
    }

    /* ── ADD-PUR-LINE / APL2-010 ── */

    @Test
    void appendReceivingLine_zeroQty_skipped() {
        ws.setWkLcnt(0);
        rcvdf.getRecord().setDecimal("RD-QTY", BigDecimal.ZERO);

        invokePrivate("appendReceivingLine");

        assertEquals(0, ws.getWkLcnt());
    }

    @Test
    void appendReceivingLine_atMax200_doesNotAddMore() {
        ws.setWkLcnt(200);
        rcvdf.getRecord().setDecimal("RD-QTY", BigDecimal.TEN);

        invokePrivate("appendReceivingLine");

        assertEquals(200, ws.getWkLcnt());
    }

    @Test
    void appendReceivingLine_normalLine_addsRowAndAccumulatesTotal() {
        ws.setWkLcnt(0);
        ws.setWkNetTotal(0);
        rcvdf.getRecord().setInt("RD-PROD", 5001);
        rcvdf.getRecord().setInt("RD-WHSE", 1);
        rcvdf.getRecord().setDecimal("RD-QTY", BigDecimal.TEN);
        rcvdf.getRecord().setDecimal("RD-UNIT-COST", BigDecimal.valueOf(2.5));
        rcvdf.getRecord().setDecimal("RD-AMOUNT", BigDecimal.valueOf(25));

        invokePrivate("appendReceivingLine");

        assertEquals(1, ws.getWkLcnt());
        assertEquals(5001, ws.getWlProd(1));
        assertEquals(25L, ws.getWkNetTotal());
        assertEquals("Widget", ws.getWlName(1).trim());
    }

    /* ── LOOKUP-PROD / LKP-010 ── */

    @Test
    void lookupProductName_found_setsNameAndTaxCategory() {
        ws.setWkLcnt(1);
        rcvdf.getRecord().setInt("RD-PROD", 5001);
        prodf.getRecord().setString("PR-NAME", "Widget");
        prodf.getRecord().setInt("PR-TAX-CATEGORY", 2);

        invokePrivate("lookupProductName");

        assertEquals("Widget", ws.getWlName(1).trim());
        assertEquals(2, ws.getWlTaxcat(1));
    }

    @Test
    void lookupProductName_notFound_setsUnknownPlaceholder() {
        ws.setWkLcnt(1);
        doReturn(true).when(prodf).isInvalidKey();

        invokePrivate("lookupProductName");

        assertEquals("??? unknown product", ws.getWlName(1).trim());
    }

    /* ── SETUP-HEADER / SHD-010 ── */

    @Test
    void setVoucherHeaderDefaults_taxTypeZero_setsDefaultOne() {
        ws.setVhTaxType(0);
        ws.setWkSysdate(20260215);

        invokePrivate("setVoucherHeaderDefaults");

        assertEquals(1, ws.getVhTaxType());
        assertEquals(20260215, ws.getVhDate());
    }

    @Test
    void setVoucherHeaderDefaults_taxTypeNonZero_keepsExistingValue() {
        ws.setVhTaxType(3);

        invokePrivate("setVoucherHeaderDefaults");

        assertEquals(3, ws.getVhTaxType());
    }

    /* ── ACCEPT-VHEAD / AVH-010 ── */

    @Test
    void acceptVoucherHeaderFields_normalEntry_setsDateAndTaxType() {
        acceptValues.put("VH-DATE", "20260220");
        acceptValues.put("VH-TAX-TYPE", "2");
        when(renderer.readEndStatus()).thenReturn("00");

        invokePrivate("acceptVoucherHeaderFields");

        assertEquals(20260220, ws.getVhDate());
        assertEquals(2, ws.getVhTaxType());
        assertFalse(msgLineHistory.contains("Cancelled"));
    }

    @Test
    void acceptVoucherHeaderFields_cancel_resetsRcvOkAndShowsMessage() {
        ws.setRcvOk(1);
        when(renderer.readEndStatus()).thenReturn("03");

        invokePrivate("acceptVoucherHeaderFields");

        assertEquals(0, ws.getRcvOk());
        assertTrue(msgLineHistory.contains("Cancelled"));
    }

    /* ── LOAD-REVIEW / LRV-010 ── */

    @Test
    void buildLineRowBuffers_nineOrFewerLines_noMoreFlag() {
        ws.setWkLcnt(3);
        for (int i = 1; i <= 3; i++) {
            ws.setWlProd(i, 5000 + i);
            ws.setWlName(i, "Item " + i);
            ws.setWlQty(i, i);
            ws.setWlCost(i, BigDecimal.valueOf(1.5));
            ws.setWlAmt(i, i * 100L);
        }

        invokePrivate("buildLineRowBuffers");

        assertEquals(0, ws.getWkMoreFlg());
        assertTrue(ws.getWrBuf(1).contains("5001"));
    }

    @Test
    void buildLineRowBuffers_moreThanNineLines_setsMoreFlag() {
        ws.setWkLcnt(11);
        for (int i = 1; i <= 11; i++) {
            ws.setWlProd(i, 6000 + i);
            ws.setWlName(i, "Item " + i);
            ws.setWlQty(i, 1);
            ws.setWlCost(i, BigDecimal.ONE);
            ws.setWlAmt(i, 100L);
        }

        invokePrivate("buildLineRowBuffers");

        assertEquals(1, ws.getWkMoreFlg());
    }

    /* ── COMPUTE-TAX-TOTAL / CTT-010 ── */

    @Test
    void computeVoucherTax_statusSuccess_setsTotalsFromTaxcal() {
        ws.setWkNetTotal(1000);
        ws.setVhTaxType(2);
        ws.setVhDate(20260210);

        invokePrivate("computeVoucherTax");

        assertEquals(1000L, ws.getWkNetTotal());
        assertEquals(100L, ws.getWkTaxTotal());
        assertEquals(1100L, ws.getWkGrsTotal());
    }

    @Test
    void computeVoucherTax_statusFailure_fallsBackToZeroTaxAndGrossEqualsNet() {
        doAnswer(
                        inv -> {
                            TaxcalLinkParm p = inv.getArgument(0);
                            p.getKtax().setKtStatus("99");
                            return null;
                        })
                .when(taxcalService)
                .execute(any());
        ws.setWkNetTotal(500);

        invokePrivate("computeVoucherTax");

        assertEquals(0, ws.getWkTaxTotal());
        assertEquals(500L, ws.getWkGrsTotal());
    }

    /* ── CONFIRM-SAVE / CSAV-010 ── */

    private void setupSaveableReceiving() {
        ws.setRhSupp(500);
        ws.setRhNo(1000000001L);
        ws.setWkRecvKey(1000000001L);
        ws.setWkLcnt(1);
        ws.setWlProd(1, 5001);
        ws.setWlWhse(1, 1);
        ws.setWlQty(1, 10);
        ws.setWlCost(1, BigDecimal.valueOf(2.5));
        ws.setWlAmt(1, 25L);
        ws.setWlTaxcat(1, 1);
        ws.setWkNetTotal(25);
        ws.setWkTaxTotal(2);
        ws.setWkGrsTotal(27);
        ws.setVhDate(20260210);
        ws.setVhTaxType(1);
    }

    @Test
    void confirmAndSavePurchase_confirmUppercaseY_savesPurchase() {
        setupSaveableReceiving();
        acceptValues.put("WK-CONFIRM", "Y");
        when(renderer.readEndStatus()).thenReturn("00");

        invokePrivate("confirmAndSavePurchase");

        assertTrue(msgLineHistory.contains("Purchase booked"));
        verify(purhf, times(1)).write();
    }

    @Test
    void confirmAndSavePurchase_confirmLowercaseY_savesPurchase() {
        setupSaveableReceiving();
        acceptValues.put("WK-CONFIRM", "y");
        when(renderer.readEndStatus()).thenReturn("00");

        invokePrivate("confirmAndSavePurchase");

        assertTrue(msgLineHistory.contains("Purchase booked"));
        verify(purhf, times(1)).write();
    }

    @Test
    void confirmAndSavePurchase_confirmOther_discardsPurchase() {
        setupSaveableReceiving();
        acceptValues.put("WK-CONFIRM", "N");
        when(renderer.readEndStatus()).thenReturn("00");

        invokePrivate("confirmAndSavePurchase");

        assertTrue(msgLineHistory.contains("Purchase discarded"));
        verify(purhf, never()).write();
    }

    /* ── SAVE-PURCHASE / SPU-010 ── */

    @Test
    void savePurchaseVoucher_numgenPurchFails_showsMessageAndSkipsWrite() {
        setupSaveableReceiving();
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("99");
                            return null;
                        })
                .when(numgenService)
                .execute(any());

        invokePrivate("savePurchaseVoucher");

        assertTrue(msgLineHistory.contains("Purchase number assignment failed"));
        verify(purhf, never()).write();
    }

    @Test
    void savePurchaseVoucher_headerWriteFails_skipsDetailAndApAndMarking() {
        setupSaveableReceiving();
        doReturn(true).when(purhf).isInvalidKey();

        invokePrivate("savePurchaseVoucher");

        assertTrue(msgLineHistory.contains("Purchase header write failed"));
        verify(purdf, never()).write();
        verify(aplf, never()).write();
        verify(rcvhf, never()).rewrite();
    }

    @Test
    void savePurchaseVoucher_happyPath_writesHeaderDetailApAndMarksReceivingBooked() {
        setupSaveableReceiving();

        invokePrivate("savePurchaseVoucher");

        assertEquals(500000001L, ws.getVhNo());
        assertTrue(msgLineHistory.contains("Purchase booked"));
        verify(purhf, times(1)).write();
        verify(purdf, times(1)).write();
        verify(aplf, times(1)).write();
        verify(rcvhf, times(1)).rewrite();
    }

    /* ── WRITE-PUR-DETAILS / WPD-010 ── */

    @Test
    void writeVoucherDetailLines_multipleLines_writesEachLine() {
        ws.setVhNo(999L);
        ws.setWkLcnt(2);
        for (int i = 1; i <= 2; i++) {
            ws.setWlProd(i, 5000 + i);
            ws.setWlWhse(i, 1);
            ws.setWlQty(i, 5);
            ws.setWlCost(i, BigDecimal.ONE);
            ws.setWlAmt(i, 5L);
            ws.setWlTaxcat(i, 1);
        }

        invokePrivate("writeVoucherDetailLines");

        verify(purdf, times(2)).write();
    }

    /* ── POST-AP / PAP-010 ── */

    @Test
    void postApLedgerEntry_numgenApldgFails_showsMessageAndSkipsWrite() {
        ws.setRhSupp(500);
        ws.setWkGrsTotal(1000);
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("99");
                            return null;
                        })
                .when(numgenService)
                .execute(any());

        invokePrivate("postApLedgerEntry");

        assertTrue(msgLineHistory.contains("AP ledger number assignment failed"));
        verify(aplf, never()).write();
        verify(suppf, never()).rewrite();
    }

    @Test
    void postApLedgerEntry_supplierNotFound_zerosBalanceBeforeCompute() {
        ws.setRhSupp(500);
        ws.setWkGrsTotal(500);
        doReturn(true).when(suppf).isInvalidKey();

        invokePrivate("postApLedgerEntry");

        assertEquals(500L, ws.getWkNewBal());
        verify(suppf, never()).rewrite();
    }

    @Test
    void postApLedgerEntry_writeFails_showsMessageButStillUpdatesSupplierBalance() {
        ws.setRhSupp(500);
        ws.setWkGrsTotal(500);
        doReturn(true).when(aplf).isInvalidKey();

        invokePrivate("postApLedgerEntry");

        assertTrue(msgLineHistory.contains("AP ledger write failed"));
        verify(suppf, times(1)).rewrite();
    }

    @Test
    void postApLedgerEntry_happyPath_writesLedgerAndUpdatesSupplierBalance() {
        ws.setRhSupp(500);
        ws.setVhDate(20260210);
        ws.setVhCloseYm(202602);
        ws.setVhNo(500000001L);
        ws.setWkGrsTotal(500);

        invokePrivate("postApLedgerEntry");

        assertEquals(1500L, ws.getWkNewBal());
        verify(aplf, times(1)).write();
        verify(suppf, times(1)).rewrite();
        assertEquals(BigDecimal.valueOf(1500), ws.getSpBalance());
    }

    /* ── UPDATE-SUPP-BAL / USB-010 ── */

    @Test
    void updateSupplierBalance_supplierNotFound_returnsWithoutRewrite() {
        ws.setRhSupp(500);
        doReturn(true).when(suppf).isInvalidKey();

        invokePrivate("updateSupplierBalance");

        verify(suppf, never()).rewrite();
    }

    @Test
    void updateSupplierBalance_found_rewritesWithNewBalance() {
        ws.setRhSupp(500);
        ws.setWkNewBal(1500);

        invokePrivate("updateSupplierBalance");

        assertEquals(BigDecimal.valueOf(1500), ws.getSpBalance());
        verify(suppf, times(1)).rewrite();
    }

    /* ── MARK-RECV-BOOKED / MRB-010 ── */

    @Test
    void markReceivingBooked_notFound_returnsWithoutRewrite() {
        ws.setWkRecvKey(999L);
        doReturn(true).when(rcvhf).isInvalidKey();

        invokePrivate("markReceivingBooked");

        verify(rcvhf, never()).rewrite();
    }

    @Test
    void markReceivingBooked_found_setsStatusBookedAndRewrites() {
        ws.setWkRecvKey(1000000001L);

        invokePrivate("markReceivingBooked");

        assertEquals(1, ws.getRhStatus());
        verify(rcvhf, times(1)).rewrite();
    }

    /* ── TERM-RTN / TERM-010 ── */

    @Test
    void closeAllFiles_closesEverySevenFiles() {
        invokePrivate("closeAllFiles");

        verify(rcvhf, times(1)).close();
        verify(rcvdf, times(1)).close();
        verify(purhf, times(1)).close();
        verify(purdf, times(1)).close();
        verify(aplf, times(1)).close();
        verify(suppf, times(1)).close();
        verify(prodf, times(1)).close();
    }

    /* ── full execute() integration: normal book, then end program ── */

    @Test
    void execute_bookOneReceivingThenExit_savesPurchaseAndTerminates() {
        stubRcvdfLines(List.of(line(1000000001L, 5001, 1, 10, 250, 2500)));
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03");

        service.execute();

        assertTrue(msgLineHistory.contains("Purchase booked"));
        assertEquals(0, service.getCompletionCode());
        verify(purhf, times(1)).write();
        verify(rcvhf, times(1)).close();
    }

    @Test
    void execute_fileOpenFails_abortsWithCompletionCode255() {
        doReturn("99").when(rcvhf).getFileStatus();

        service.execute();

        assertEquals(255, service.getCompletionCode());
        verify(abortxService, times(1)).execute(any());
        verify(purhf, never()).open(any());
    }
}

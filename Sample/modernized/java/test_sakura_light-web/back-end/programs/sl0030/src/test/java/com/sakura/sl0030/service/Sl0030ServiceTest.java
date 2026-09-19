package com.sakura.sl0030.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
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
import com.sakura.runtime.io.ProdfDataset;
import com.sakura.runtime.io.SmovfDataset;
import com.sakura.runtime.io.StokfDataset;
import com.sakura.runtime.linkage.DateutLinkParm;
import com.sakura.runtime.linkage.NumgenLinkParm;
import com.sakura.runtime.linkage.TaxcalLinkParm;
import com.sakura.runtime.record.RuntimeFieldAccess;
import com.sakura.sl0030.domain.Sl0030FieldAccess;
import com.sakura.sl0030.runtime.Sl0030Datasets;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for Sl0030Service (COBOL SL0030 — sales return entry), generated from {@code
 * SL0030.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>INVHF/INVDF/STOKF/SMOVF/CUSTF/PRODF/CPRCF/ARLF are real dataset objects wrapped with {@code
 * spy()} so the record buffer (and therefore {@code Sl0030FieldAccess}, which registers those
 * buffers) behaves exactly as in production; only I/O methods
 * (open/close/write/rewrite/readByKey/getFileStatus/isInvalidKey) are stubbed so no real file/DB
 * access happens. DATEUT/TAXCAL/NUMGEN/ABORTX are mocked since their own internal logic is covered
 * by their own module's tests.
 *
 * <p>IH-REC/ID-REC are INITIALIZEd then overwritten every WRITE, so inspecting the dataset buffer
 * after execute() would see whatever the last cycle wrote. Header/detail field values are captured
 * in {@link #invhfWriteSnapshot}/{@link #invdfWriteSnapshots} at the moment write() is invoked to
 * keep the ground-truth checks honest.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Sl0030ServiceTest {

    @Mock private ScreenRendererInstance renderer;
    @Mock private DateutService dateutService;
    @Mock private TaxcalService taxcalService;
    @Mock private NumgenService numgenService;
    @Mock private AbortxService abortxService;

    private Sl0030Datasets fileSet;
    private InvhfDataset invhf;
    private InvdfDataset invdf;
    private StokfDataset stokf;
    private SmovfDataset smovf;
    private CustfDataset custf;
    private ProdfDataset prodf;
    private CprcfDataset cprcf;
    private ArlfDataset arlf;

    private Sl0030Service service;

    private final Map<String, String> acceptValues = new HashMap<>();
    private final List<String> screenInteractions = new ArrayList<>();
    private final List<String> msgLineHistory = new ArrayList<>();
    private final Map<String, Object> invhfWriteSnapshot = new HashMap<>();
    private final Map<String, Object> invdfWriteSnapshots = new HashMap<>();
    private final Map<String, Object> arlfWriteSnapshot = new HashMap<>();
    private final Map<String, Object> smovfWriteSnapshot = new HashMap<>();

    @BeforeEach
    void setUp() {
        Sl0030Datasets real = new Sl0030Datasets();
        fileSet = spy(real);
        invhf = spy(real.getInvhf());
        invdf = spy(real.getInvdf());
        stokf = spy(real.getStokf());
        smovf = spy(real.getSmovf());
        custf = spy(real.getCustf());
        prodf = spy(real.getProdf());
        cprcf = spy(real.getCprcf());
        arlf = spy(real.getArlf());
        doReturn(invhf).when(fileSet).getInvhf();
        doReturn(invdf).when(fileSet).getInvdf();
        doReturn(stokf).when(fileSet).getStokf();
        doReturn(smovf).when(fileSet).getSmovf();
        doReturn(custf).when(fileSet).getCustf();
        doReturn(prodf).when(fileSet).getProdf();
        doReturn(cprcf).when(fileSet).getCprcf();
        doReturn(arlf).when(fileSet).getArlf();

        for (var f : new Object[] {invhf, invdf, stokf, smovf, custf, prodf, cprcf, arlf}) {
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
                            invhfWriteSnapshot.put(
                                    "IH-CLOSE-YM", invhf.getRecord().getInt("IH-CLOSE-YM"));
                            invhfWriteSnapshot.put(
                                    "IH-REMARK", invhf.getRecord().getString("IH-REMARK").trim());
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
                            smovfWriteSnapshot.put("SM-SEQ", smovf.getRecord().getLong("SM-SEQ"));
                            smovfWriteSnapshot.put("SM-PROD", smovf.getRecord().getInt("SM-PROD"));
                            smovfWriteSnapshot.put("SM-WHSE", smovf.getRecord().getInt("SM-WHSE"));
                            smovfWriteSnapshot.put("SM-KIND", smovf.getRecord().getInt("SM-KIND"));
                            smovfWriteSnapshot.put(
                                    "SM-QTY", smovf.getRecord().getDecimal("SM-QTY"));
                            smovfWriteSnapshot.put(
                                    "SM-UNIT-COST", smovf.getRecord().getDecimal("SM-UNIT-COST"));
                            smovfWriteSnapshot.put(
                                    "SM-BAL-AFTER", smovf.getRecord().getDecimal("SM-BAL-AFTER"));
                            smovfWriteSnapshot.put(
                                    "SM-REF-TYPE", smovf.getRecord().getInt("SM-REF-TYPE"));
                            smovfWriteSnapshot.put(
                                    "SM-REF-NO", smovf.getRecord().getLong("SM-REF-NO"));
                            return null;
                        })
                .when(smovf)
                .write();

        doNothing().when(stokf).write();
        doNothing().when(stokf).rewrite();
        doNothing().when(custf).rewrite();

        // Default: customer found, active, no close-day rollover, no price rank.
        doReturn(true).when(custf).readByKey(any());
        doReturn(false).when(custf).isInvalidKey();
        custf.getRecord().setString("CU-NAME", "ACME Corp");
        custf.getRecord().setInt("CU-DEL-FLAG", 0);
        custf.getRecord().setInt("CU-STAFF", 20);
        custf.getRecord().setInt("CU-TAX-TYPE", 1);
        custf.getRecord().setInt("CU-TAX-ROUND", 1);
        custf.getRecord().setInt("CU-PRICE-RANK", 0);
        custf.getRecord().setInt("CU-CLOSE-DAY", 0);
        custf.getRecord().setDecimal("CU-BALANCE", new BigDecimal("1000"));

        // Default: product found, active, not stock-managed (individual tests opt in).
        doReturn(true).when(prodf).readByKey(any());
        doReturn(false).when(prodf).isInvalidKey();
        prodf.getRecord().setString("PR-NAME", "Widget");
        prodf.getRecord().setInt("PR-DEL-FLAG", 0);
        prodf.getRecord().setInt("PR-TAX-CATEGORY", 1);
        prodf.getRecord().setInt("PR-STOCK-MNG", 0);
        prodf.getRecord().setInt("PR-DFLT-WHSE", 1);
        prodf.getRecord().setDecimal("PR-LIST-PRICE", new BigDecimal("50"));
        prodf.getRecord().setDecimal("PR-STD-COST", new BigDecimal("15"));

        // Default: no contract price, stock lookup misses -> cost falls back to PR-STD-COST.
        doReturn(false).when(cprcf).readByKey(any());
        doReturn(true).when(cprcf).isInvalidKey();
        doReturn(false).when(stokf).readByKey(any());
        doReturn(true).when(stokf).isInvalidKey();

        acceptValues.put("WK-SEL-CUST", "100");
        acceptValues.put("WK-ORIG-INV", "0");
        acceptValues.put(
                "WK-STAFF-IN",
                "20"); // ENTER-HEADER has no default-fill for staff; simulate the terminal echoing
        // back CU-STAFF
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
                            if (w instanceof Sl0030FieldAccess a) {
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

        // Default TAXCAL: pass net amount through untouched (no tax).
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
                            String key = p.getKnum().getKnumKey().trim();
                            p.getKnum().setKnumStatus("00");
                            switch (key) {
                                case "INVOICE" -> p.getKnum().setKnumNumber(9001L);
                                case "STKMOV" -> p.getKnum().setKnumNumber(7001L);
                                case "ARLDG" -> p.getKnum().setKnumNumber(5001L);
                                default -> p.getKnum().setKnumNumber(1L);
                            }
                            return null;
                        })
                .when(numgenService)
                .execute(any());

        service =
                new Sl0030Service(
                        fileSet,
                        dateutService,
                        taxcalService,
                        numgenService,
                        abortxService,
                        renderer);
    }

    private BigDecimal invdfSnapshotDecimal(String key) {
        return (BigDecimal) invdfWriteSnapshots.get(key);
    }

    /** Live handle onto the service's WorkingStorage/FD field accessor. */
    private Sl0030FieldAccess capturedWs() {
        ArgumentCaptor<RuntimeFieldAccess> captor =
                ArgumentCaptor.forClass(RuntimeFieldAccess.class);
        verify(renderer, atLeastOnce()).displayScreen(any(), captor.capture());
        return (Sl0030FieldAccess) captor.getAllValues().get(0);
    }

    // ───────────────────────── happy path ─────────────────────────

    @Test
    void execute_validReturnOneLineConfirmYes_postsHeaderDetailArAndCustomerBalance() {
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        verify(invhf, times(1)).write();
        verify(invdf, times(1)).write();
        verify(arlf, times(1)).write();
        verify(custf, times(1)).rewrite();
        verify(numgenService, times(2))
                .execute(any()); // INVOICE + ARLDG (no STKMOV, not stock-managed)
        verify(taxcalService, times(1)).execute(any());

        assertThat(invhfWriteSnapshot.get("IH-NO")).isEqualTo(9001L);
        assertThat(invhfWriteSnapshot.get("IH-CUST")).isEqualTo(100);
        assertThat(invhfWriteSnapshot.get("IH-STAFF"))
                .isEqualTo(20); // WK-STAFF-IN as accepted (pre-filled with CU-STAFF, no
        // PROCESS/re-default in ENTER-HEADER)
        assertThat(invhfWriteSnapshot.get("IH-TAX-TYPE"))
                .isEqualTo(1); // defaulted from CU-TAX-TYPE
        assertThat(invhfWriteSnapshot.get("IH-AMOUNT")).isEqualTo(new BigDecimal("-250"));
        assertThat(invhfWriteSnapshot.get("IH-TOTAL")).isEqualTo(new BigDecimal("-250"));
        assertThat(invhfWriteSnapshot.get("IH-COST-TOTAL"))
                .isEqualTo(new BigDecimal("-150")); // 10 * PR-STD-COST(15)
        assertThat(invhfWriteSnapshot.get("IH-LINES")).isEqualTo(1);
        assertThat(invhfWriteSnapshot.get("IH-STATUS")).isEqualTo(1);
        assertThat(invhfWriteSnapshot.get("IH-KIND")).isEqualTo(2);
        assertThat(invhfWriteSnapshot.get("IH-DEL-FLAG")).isEqualTo(0);
        assertThat(invhfWriteSnapshot.get("IH-REMARK")).isEqualTo("");

        assertThat(invdfWriteSnapshots.get("ID-NO@1")).isEqualTo(9001L);
        assertThat(invdfWriteSnapshots.get("ID-PROD@1")).isEqualTo(500);
        assertThat(invdfSnapshotDecimal("ID-QTY@1")).isEqualByComparingTo(new BigDecimal("-10"));
        assertThat(invdfSnapshotDecimal("ID-UNIT-PRICE@1"))
                .isEqualByComparingTo(new BigDecimal("25"));
        assertThat(invdfSnapshotDecimal("ID-AMOUNT@1"))
                .isEqualByComparingTo(new BigDecimal("-250"));
        assertThat(invdfSnapshotDecimal("ID-UNIT-COST@1"))
                .isEqualByComparingTo(new BigDecimal("15"));
        assertThat(invdfSnapshotDecimal("ID-COST-AMOUNT@1"))
                .isEqualByComparingTo(new BigDecimal("-150"));

        assertThat(arlfWriteSnapshot.get("AL-CREDIT")).isEqualTo(new BigDecimal("250"));
        assertThat(arlfWriteSnapshot.get("AL-BALANCE"))
                .isEqualTo(new BigDecimal("750")); // 1000 - 250
        assertThat(arlfWriteSnapshot.get("AL-KIND")).isEqualTo(3);

        assertThat(msgLineHistory)
                .contains(
                        "Enter return header - PF3 to cancel",
                        "Line added",
                        "Return 0000009001 posted");
        assertThat(capturedWs().getCompletionCode()).isEqualTo(0);
        assertThat(screenInteractions)
                .contains(
                        "displayScreen:DS-CONFIRM",
                        "displayScreen:DS-INV",
                        "displayScreen:DS-TOTAL");
    }

    @Test
    void execute_originalInvoiceProvided_setsRemarkWithReferenceNumber() {
        acceptValues.put("WK-ORIG-INV", "42");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        assertThat(invhfWriteSnapshot.get("IH-REMARK")).isEqualTo("Return ref inv 0000000042");
    }

    @Test
    void execute_closeDayRollover_computesNextMonthCloseYm() {
        custf.getRecord().setInt("CU-CLOSE-DAY", 10); // sysdate day (15) > close day (10)
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        assertThat(invhfWriteSnapshot.get("IH-CLOSE-YM")).isEqualTo(202604);
    }

    // ───────────────────────── customer validation (ground truth: READ-CUSTOMER)
    // ─────────────────────────

    @Test
    void execute_customerCodeZero_rejectsWithRequiredMessage() {
        acceptValues.put("WK-SEL-CUST", "0");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(custf, never()).readByKey(any());
        verify(invhf, never()).write();
        assertThat(msgLineHistory).contains("Customer code required");
    }

    @Test
    void execute_customerNotFound_rejectsWithNotFoundMessage() {
        doReturn(true).when(custf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(msgLineHistory).contains("Customer not found");
    }

    @Test
    void execute_customerDeleted_rejectsWithDeletedMessage() {
        custf.getRecord().setInt("CU-DEL-FLAG", 1);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(msgLineHistory).contains("Customer is deleted");
    }

    // ───────────────────────── header validation (ground truth: ENTER-HEADER)
    // ─────────────────────────

    @Test
    void execute_headerCancelledPf3_discardsReturnWithoutDetailEntry() {
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        verify(prodf, never()).readByKey(any());
        verify(invhf, never()).write();
        assertThat(capturedWs().getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_taxTypeOutOfRange_rejectsHeaderWithRangeMessage() {
        acceptValues.put("WK-TAXTYPE-IN", "5");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(msgLineHistory).contains("Tax type must be 1, 2 or 3");
    }

    // ───────────────────────── detail entry (ground truth: DETAIL-ENTRY-LOOP / PROCESS-DETAIL)
    // ─────────────────────────

    @Test
    void execute_noLinesEntered_discardsReturnWithoutSaveOrTaxCalc() {
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        verify(invhf, never()).write();
        verify(taxcalService, never()).execute(any());
        verify(numgenService, never()).execute(any());
        assertThat(msgLineHistory).contains("No lines entered - return discarded");
    }

    @Test
    void execute_productCodeZero_rejectsDetailLineWithRequiredMessage() {
        acceptValues.put("WK-D-PROD", "0");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03");

        service.execute();

        verify(prodf, never()).readByKey(any());
        verify(invhf, never()).write();
        assertThat(msgLineHistory).contains("Product code required");
    }

    @Test
    void execute_productNotFound_rejectsDetailLineWithNotFoundMessage() {
        doReturn(true).when(prodf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(msgLineHistory).contains("Product not found");
    }

    @Test
    void execute_productDeleted_rejectsDetailLineWithDeletedMessage() {
        prodf.getRecord().setInt("PR-DEL-FLAG", 1);
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(msgLineHistory).contains("Product not found");
    }

    @Test
    void execute_quantityNotPositive_rejectsDetailLineWithQuantityMessage() {
        acceptValues.put("WK-D-QTY", "0");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(msgLineHistory).contains("Return qty must be positive");
    }

    @Test
    void execute_lineClearedEsts04_showsClearedMessageAndKeepsDetailLoopOpen() {
        when(renderer.readEndStatus()).thenReturn("00", "00", "04", "00", "03", "00", "00", "03");

        service.execute();

        verify(invhf, times(1)).write();
        assertThat(msgLineHistory).contains("Line cleared");
    }

    @Test
    void execute_invalidDetailKey_showsInvalidKeyMessageAndKeepsDetailLoopOpen() {
        when(renderer.readEndStatus()).thenReturn("00", "00", "09", "00", "03", "00", "00", "03");

        service.execute();

        verify(invhf, times(1)).write();
        assertThat(msgLineHistory).contains("Invalid key");
    }

    // ───────────────────────── price resolution (ground truth: RESOLVE-PRICE / RANK-PRICE)
    // ─────────────────────────

    @Test
    void execute_priceEnteredByUser_skipsPriceResolution() {
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        verify(cprcf, never()).readByKey(any());
        assertThat(invdfSnapshotDecimal("ID-UNIT-PRICE@1"))
                .isEqualByComparingTo(new BigDecimal("25"));
    }

    @Test
    void execute_priceZeroWithActiveContract_resolvesFromCustomerContractPrice() {
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
        assertThat(invdfSnapshotDecimal("ID-AMOUNT@1"))
                .isEqualByComparingTo(new BigDecimal("-180"));
    }

    @Test
    void execute_priceZeroWithExpiredContract_fallsBackToRankPrice() {
        acceptValues.put("WK-D-PRICE", "0");
        doReturn(true).when(cprcf).readByKey(any());
        doReturn(false).when(cprcf).isInvalidKey();
        cprcf.getRecord().setInt("CP-DEL-FLAG", 0);
        cprcf.getRecord().setInt("CP-START-DATE", 20200101);
        cprcf.getRecord().setInt("CP-END-DATE", 20201231); // expired before WK-SYSDATE 2026-03-15
        cprcf.getRecord().setDecimal("CP-PRICE", new BigDecimal("18"));
        custf.getRecord().setInt("CU-PRICE-RANK", 2);
        prodf.getRecord().setDecimal("PR-RANK-PRICE", new BigDecimal("30"), 2);
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        assertThat(invdfSnapshotDecimal("ID-UNIT-PRICE@1"))
                .isEqualByComparingTo(new BigDecimal("30"));
    }

    @Test
    void execute_priceZeroNoContractNoRank_fallsBackToListPrice() {
        acceptValues.put("WK-D-PRICE", "0");
        custf.getRecord().setInt("CU-PRICE-RANK", 0);
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        assertThat(invdfSnapshotDecimal("ID-UNIT-PRICE@1"))
                .isEqualByComparingTo(new BigDecimal("50"));
    }

    // ───────────────────────── cost resolution (ground truth: RESOLVE-COST)
    // ─────────────────────────

    @Test
    void execute_costFromStockWhenAvailable_usesStockAvgCostNotStdCost() {
        doReturn(true).when(stokf).readByKey(any());
        doReturn(false).when(stokf).isInvalidKey();
        stokf.getRecord().setDecimal("SK-AVG-COST", new BigDecimal("12"));
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        assertThat(invdfSnapshotDecimal("ID-UNIT-COST@1"))
                .isEqualByComparingTo(new BigDecimal("12"));
        assertThat(invdfSnapshotDecimal("ID-COST-AMOUNT@1"))
                .isEqualByComparingTo(new BigDecimal("-120"));
    }

    // ───────────────────────── stock posting (ground truth: INCREASE-STOCK / CREATE-STOCK /
    // WRITE-MOVEMENT) ─────────────────────────

    @Test
    void execute_stockManagedExistingRecord_increasesOnHandAndWritesMovement() {
        prodf.getRecord().setInt("PR-STOCK-MNG", 1);
        doReturn(true).when(stokf).readByKey(any());
        doReturn(false).when(stokf).isInvalidKey();
        stokf.getRecord().setDecimal("SK-ONHAND", new BigDecimal("50"));
        stokf.getRecord().setDecimal("SK-YTD-IN", new BigDecimal("0"));
        stokf.getRecord().setDecimal("SK-AVG-COST", new BigDecimal("12"));
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        verify(stokf, times(1)).rewrite();
        verify(stokf, never()).write();
        verify(smovf, times(1)).write();
        assertThat(stokf.getRecord().getDecimal("SK-ONHAND"))
                .isEqualByComparingTo(new BigDecimal("60"));
        assertThat(smovfWriteSnapshot.get("SM-KIND")).isEqualTo(50);
        assertThat(smovfWriteSnapshot.get("SM-REF-TYPE")).isEqualTo(3);
        assertThat((BigDecimal) smovfWriteSnapshot.get("SM-QTY"))
                .isEqualByComparingTo(new BigDecimal("10"));
        assertThat((BigDecimal) smovfWriteSnapshot.get("SM-BAL-AFTER"))
                .isEqualByComparingTo(new BigDecimal("60"));
    }

    @Test
    void execute_stockManagedNoExistingRecord_createsStockRecordAndWritesMovement() {
        prodf.getRecord().setInt("PR-STOCK-MNG", 1);
        // stokf "not found" for RESOLVE-COST (checked twice: isInvalidKey / !isInvalidKey)
        // and for INCREASE-STOCK's own read, then "not invalid" after the CREATE-STOCK
        // write so WK-STK-FOUND is set and the movement record still gets written.
        doReturn(true, true, true, false).when(stokf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        verify(stokf, times(1)).write();
        verify(stokf, never()).rewrite();
        verify(smovf, times(1)).write();
        assertThat(stokf.getRecord().getDecimal("SK-ONHAND"))
                .isEqualByComparingTo(new BigDecimal("10"));
        assertThat(stokf.getRecord().getDecimal("SK-AVG-COST"))
                .isEqualByComparingTo(new BigDecimal("15")); // PR-STD-COST fallback
    }

    @Test
    void execute_stockNotManaged_skipsStockUpdateAndMovement() {
        // PR-STOCK-MNG stays 0 (default).
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        verify(stokf, times(1)).readByKey(any()); // only RESOLVE-COST reads STOKF
        verify(stokf, never()).rewrite();
        verify(stokf, never()).write();
        verify(smovf, never()).write();
    }

    // ───────────────────────── review / confirm (ground truth: REVIEW-LOOP / ASK-CONFIRM)
    // ─────────────────────────

    @Test
    void execute_reviewCancelledPf3_discardsReturnWithoutConfirmOrPost() {
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "03", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(msgLineHistory).contains("Return cancelled");
        assertThat(msgLineHistory).doesNotContain("Confirm to post the return");
    }

    @Test
    void execute_pageDownAtLastPage_isNoOpButKeepsReviewLoopOpen() {
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "06", "00", "00", "03");

        service.execute();

        verify(invhf, times(1)).write();
        assertThat(capturedWs().getWkPageTop()).isEqualTo(1);
    }

    @Test
    void execute_pageUpAtFirstPage_isNoOpButKeepsReviewLoopOpen() {
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "12", "00", "00", "03");

        service.execute();

        verify(invhf, times(1)).write();
        assertThat(capturedWs().getWkPageTop()).isEqualTo(1);
    }

    @Test
    void execute_confirmDeclined_discardsReturnWithoutSaving() {
        acceptValues.put("WK-CONFIRM", "N");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(msgLineHistory).contains("Return discarded");
    }

    @Test
    void execute_confirmLowercaseY_stillPostsReturn() {
        acceptValues.put("WK-CONFIRM", "y");
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        verify(invhf, times(1)).write();
    }

    // ───────────────────────── post-path failures (ground truth: POST-RETURN / WRITE-RET-* /
    // POST-AR-LEDGER / UPDATE-CUST-BALANCE) ─────────────────────────

    @Test
    void execute_numgenInvoiceFails_showsMessageAndSkipsWrite() {
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("99");
                            return null;
                        })
                .when(numgenService)
                .execute(any());
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        verify(invhf, never()).write();
        verify(invdf, never()).write();
        assertThat(msgLineHistory).contains("Return number assignment failed");
    }

    @Test
    void execute_invhfWriteInvalidKey_stillWritesDetailAndCompletesPosting() {
        doReturn(true).when(invhf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        verify(invhf, times(1)).write();
        verify(invdf, times(1)).write();
        assertThat(msgLineHistory)
                .contains("Return header write failed", "Return 0000009001 posted");
    }

    @Test
    void execute_arLedgerNumgenFails_showsMessageSkipsArWriteButStillUpdatesCustomerBalance() {
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            String key = p.getKnum().getKnumKey().trim();
                            if ("ARLDG".equals(key)) {
                                p.getKnum().setKnumStatus("99");
                            } else {
                                p.getKnum().setKnumStatus("00");
                                p.getKnum().setKnumNumber("INVOICE".equals(key) ? 9001L : 7001L);
                            }
                            return null;
                        })
                .when(numgenService)
                .execute(any());
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        verify(arlf, never()).write();
        verify(custf, times(1)).rewrite();
        assertThat(msgLineHistory).contains("AR ledger number assignment failed");
    }

    @Test
    void execute_stkmovNumgenFails_showsMessageAndSkipsMovementWriteButKeepsStockRewrite() {
        prodf.getRecord().setInt("PR-STOCK-MNG", 1);
        doReturn(true).when(stokf).readByKey(any());
        doReturn(false).when(stokf).isInvalidKey();
        stokf.getRecord().setDecimal("SK-ONHAND", new BigDecimal("50"));
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            String key = p.getKnum().getKnumKey().trim();
                            if ("STKMOV".equals(key)) {
                                p.getKnum().setKnumStatus("99");
                            } else {
                                p.getKnum().setKnumStatus("00");
                                p.getKnum().setKnumNumber("INVOICE".equals(key) ? 9001L : 5001L);
                            }
                            return null;
                        })
                .when(numgenService)
                .execute(any());
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        verify(stokf, times(1)).rewrite();
        verify(smovf, never()).write();
        assertThat(msgLineHistory).contains("Movement number assignment failed");
    }

    @Test
    void execute_custfRewriteInvalidKey_showsBalanceUpdateFailedMessage() {
        doReturn(false, false, true).when(custf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "00", "03", "00", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Customer balance update failed");
    }

    // ───────────────────────── file open lifecycle (ground truth: OPEN-FILES / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_invhfOpenFailsPersistently_abortsWithCompletionCode255() {
        doReturn("99").when(invhf).getFileStatus();

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(invdf, never()).open(any());
        verify(custf, never()).open(any());
        verify(invhf, never()).write();
    }

    @Test
    void execute_invhfOpenStatus35_reopensAsOutputThenReopensIo() {
        doReturn("35", "00", "00", "00").when(invhf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(invhf, times(3)).open(any());
        verify(invhf, times(1)).open(FileOpenMode.OUTPUT);
        verify(invhf, times(2)).open(FileOpenMode.IO);
        verify(invhf, times(2)).close();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_prodfOpenFailsPersistently_doesNotAbort() {
        // PRODF's OPEN-FILES has no retry / no post-open FSTS check / no ABEND — unlike
        // INVHF/INVDF/STOKF/SMOVF/CUSTF/ARLF. A persistently-bad PRODF status must not
        // abort the program.
        doReturn("99").when(prodf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(abortxService, never()).execute(any());
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }
}

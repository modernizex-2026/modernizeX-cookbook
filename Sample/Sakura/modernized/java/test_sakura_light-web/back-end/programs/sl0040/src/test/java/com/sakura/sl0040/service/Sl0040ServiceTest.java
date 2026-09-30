package com.sakura.sl0040.service;

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
import com.sakura.sl0040.domain.Sl0040FieldAccess;
import com.sakura.sl0040.runtime.Sl0040Datasets;
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

/**
 * Unit tests for Sl0040Service (COBOL SL0040 — sales credit note), generated from {@code
 * SL0040.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>INVHF/INVDF/STOKF/SMOVF/CUSTF/PRODF/ARLF are real dataset objects wrapped with {@code spy()}
 * so the record buffer (and therefore {@code Sl0040FieldAccess}, which registers those buffers)
 * behaves exactly as in production; only I/O methods
 * (open/close/write/rewrite/readByKey/start/readNext/getFileStatus/isInvalidKey/isAtEnd) are
 * stubbed so no real file/DB access happens. DATEUT/TAXCAL/NUMGEN/ABORTX are mocked since their own
 * internal logic is covered by their own module's tests.
 *
 * <p>IH-REC/ID-REC are cleared then overwritten on every WRITE, so write field values are captured
 * in snapshot maps at the moment write()/rewrite() is invoked.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Sl0040ServiceTest {

    @Mock private ScreenRendererInstance renderer;
    @Mock private DateutService dateutService;
    @Mock private TaxcalService taxcalService;
    @Mock private NumgenService numgenService;
    @Mock private AbortxService abortxService;

    private Sl0040Datasets fileSet;
    private InvhfDataset invhf;
    private InvdfDataset invdf;
    private StokfDataset stokf;
    private SmovfDataset smovf;
    private CustfDataset custf;
    private ProdfDataset prodf;
    private ArlfDataset arlf;

    private Sl0040Service service;

    private final Map<String, String> acceptValues = new HashMap<>();
    private final List<String> screenInteractions = new ArrayList<>();
    private final List<String> msgLineHistory = new ArrayList<>();
    private final Map<String, Object> invhfWriteSnapshot = new HashMap<>();
    private final Map<String, Object> invdfWriteSnapshots = new HashMap<>();
    private final Map<String, Object> arlfWriteSnapshot = new HashMap<>();
    private final Map<String, Object> smovfWriteSnapshot = new HashMap<>();
    private final Deque<Runnable> invdfLineQueue = new ArrayDeque<>();
    private final boolean[] invdfAtEnd = {false};

    private Runnable invdfLine(
            long idNo,
            int line,
            int prod,
            int whse,
            BigDecimal qty,
            BigDecimal price,
            BigDecimal cost,
            int taxCat) {
        return () -> {
            invdf.getRecord().setLong("ID-NO", idNo);
            invdf.getRecord().setInt("ID-LINE", line);
            invdf.getRecord().setInt("ID-PROD", prod);
            invdf.getRecord().setInt("ID-WHSE", whse);
            invdf.getRecord().setDecimal("ID-QTY", qty);
            invdf.getRecord().setDecimal("ID-UNIT-PRICE", price);
            invdf.getRecord().setDecimal("ID-UNIT-COST", cost);
            invdf.getRecord().setInt("ID-TAX-CATEGORY", taxCat);
        };
    }

    @BeforeEach
    void setUp() {
        Sl0040Datasets real = new Sl0040Datasets();
        fileSet = spy(real);
        invhf = spy(real.getInvhf());
        invdf = spy(real.getInvdf());
        stokf = spy(real.getStokf());
        smovf = spy(real.getSmovf());
        custf = spy(real.getCustf());
        prodf = spy(real.getProdf());
        arlf = spy(real.getArlf());
        doReturn(invhf).when(fileSet).getInvhf();
        doReturn(invdf).when(fileSet).getInvdf();
        doReturn(stokf).when(fileSet).getStokf();
        doReturn(smovf).when(fileSet).getSmovf();
        doReturn(custf).when(fileSet).getCustf();
        doReturn(prodf).when(fileSet).getProdf();
        doReturn(arlf).when(fileSet).getArlf();

        for (var f : new Object[] {invhf, invdf, stokf, smovf, custf, prodf, arlf}) {
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
                                    "IH-REMARK", invhf.getRecord().getString("IH-REMARK"));
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

        // Default: original invoice found, active sale, not cancelled/deleted.
        doReturn(true).when(invhf).readByKey(any());
        doReturn(false).when(invhf).isInvalidKey();
        invhf.getRecord().setInt("IH-DEL-FLAG", 0);
        invhf.getRecord().setInt("IH-KIND", 1);
        invhf.getRecord().setInt("IH-STATUS", 1);
        invhf.getRecord().setInt("IH-CUST", 100);
        invhf.getRecord().setInt("IH-STAFF", 20);
        invhf.getRecord().setInt("IH-TAX-TYPE", 1);

        // Default: customer found.
        doReturn(true).when(custf).readByKey(any());
        doReturn(false).when(custf).isInvalidKey();
        custf.getRecord().setString("CU-NAME", "ACME Corp");
        custf.getRecord().setInt("CU-TAX-TYPE", 1);
        custf.getRecord().setInt("CU-TAX-ROUND", 1);
        custf.getRecord().setInt("CU-CLOSE-DAY", 0);
        custf.getRecord().setDecimal("CU-BALANCE", new BigDecimal("1000"));

        // Default: START always valid, one source line, then AT END.
        doReturn(true).when(invdf).start(any(), any());
        doReturn(false).when(invdf).isInvalidKey();
        doAnswer(
                        inv -> {
                            if (invdfLineQueue.isEmpty()) {
                                invdfAtEnd[0] = true;
                            } else {
                                invdfAtEnd[0] = false;
                                invdfLineQueue.poll().run();
                            }
                            return true;
                        })
                .when(invdf)
                .readNext();
        doAnswer(inv -> invdfAtEnd[0]).when(invdf).isAtEnd();
        invdfLineQueue.add(
                invdfLine(
                        1001L,
                        1,
                        500,
                        1,
                        new BigDecimal("10"),
                        new BigDecimal("25"),
                        new BigDecimal("15"),
                        1));

        // Default: product found, not stock-managed (individual tests opt in).
        doReturn(true).when(prodf).readByKey(any());
        doReturn(false).when(prodf).isInvalidKey();
        prodf.getRecord().setString("PR-NAME", "Widget");
        prodf.getRecord().setInt("PR-STOCK-MNG", 0);

        // Default: stock lookup misses -> CREATE-STOCK path when stock IS managed.
        doReturn(false).when(stokf).readByKey(any());
        doReturn(true).when(stokf).isInvalidKey();

        acceptValues.put("WK-ORIG-INV", "1001");
        acceptValues.put("WK-PICK-LINE", "1");
        acceptValues.put("WK-PICK-QTY", "5");
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
                            if (w instanceof Sl0040FieldAccess a) {
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
                new Sl0040Service(
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
    private Sl0040FieldAccess capturedWs() {
        ArgumentCaptor<RuntimeFieldAccess> captor =
                ArgumentCaptor.forClass(RuntimeFieldAccess.class);
        verify(renderer, org.mockito.Mockito.atLeastOnce()).displayScreen(any(), captor.capture());
        return (Sl0040FieldAccess) captor.getAllValues().get(0);
    }

    // ───────────────────────── happy path ─────────────────────────

    @Test
    void execute_validCreditOneLineConfirmYes_postsHeaderDetailArAndCustomerBalance() {
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        verify(invhf, times(1)).write();
        verify(invdf, times(1)).write();
        verify(arlf, times(1)).write();
        verify(custf, times(1)).rewrite();
        verify(stokf, never()).readByKey(any());
        verify(smovf, never()).write();

        assertThat(invhfWriteSnapshot.get("IH-NO")).isEqualTo(9001L);
        assertThat(invhfWriteSnapshot.get("IH-CUST")).isEqualTo(100);
        assertThat(invhfWriteSnapshot.get("IH-STAFF")).isEqualTo(20);
        assertThat(invhfWriteSnapshot.get("IH-TAX-TYPE")).isEqualTo(1);
        assertThat(invhfWriteSnapshot.get("IH-AMOUNT")).isEqualTo(new BigDecimal("-125"));
        assertThat(invhfWriteSnapshot.get("IH-TOTAL")).isEqualTo(new BigDecimal("-125"));
        assertThat(invhfWriteSnapshot.get("IH-COST-TOTAL"))
                .isEqualTo(new BigDecimal("-75")); // 5 * 15
        assertThat(invhfWriteSnapshot.get("IH-LINES")).isEqualTo(1);
        assertThat(invhfWriteSnapshot.get("IH-STATUS")).isEqualTo(1);
        assertThat(invhfWriteSnapshot.get("IH-KIND")).isEqualTo(2);
        assertThat(((String) invhfWriteSnapshot.get("IH-REMARK")).trim())
                .isEqualTo("Credit vs invoice 0000001001");

        assertThat(invdfWriteSnapshots.get("ID-NO@1")).isEqualTo(9001L);
        assertThat(invdfWriteSnapshots.get("ID-PROD@1")).isEqualTo(500);
        assertThat(invdfSnapshotDecimal("ID-QTY@1")).isEqualByComparingTo(new BigDecimal("-5"));
        assertThat(invdfSnapshotDecimal("ID-AMOUNT@1"))
                .isEqualByComparingTo(new BigDecimal("-125"));
        assertThat(invdfSnapshotDecimal("ID-COST-AMOUNT@1"))
                .isEqualByComparingTo(new BigDecimal("-75"));

        assertThat(arlfWriteSnapshot.get("AL-CREDIT")).isEqualTo(new BigDecimal("125"));
        assertThat(arlfWriteSnapshot.get("AL-BALANCE"))
                .isEqualTo(new BigDecimal("875")); // 1000 - 125
        assertThat(arlfWriteSnapshot.get("AL-KIND")).isEqualTo(3);

        assertThat(msgLineHistory).contains("Credit quantity set", "Credit note 0000009001 posted");
        assertThat(capturedWs().getCompletionCode()).isEqualTo(0);
        assertThat(screenInteractions)
                .contains(
                        "displayScreen:DS-CONFIRM",
                        "displayScreen:DS-INFO",
                        "displayScreen:DS-LIST");
    }

    @Test
    void execute_taxTypeInvalidOnInvoice_defaultsFromCustomerTaxType() {
        invhf.getRecord().setInt("IH-TAX-TYPE", 0);
        custf.getRecord().setInt("CU-TAX-TYPE", 2);
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        assertThat(invhfWriteSnapshot.get("IH-TAX-TYPE")).isEqualTo(2);
    }

    @Test
    void execute_closeDayExceeded_rollsCloseYmToNextMonth() {
        custf.getRecord().setInt("CU-CLOSE-DAY", 10); // sysdate day (15) > close day (10)
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        assertThat(invhfWriteSnapshot.get("IH-CLOSE-YM")).isEqualTo(202604);
    }

    @Test
    void execute_closeDayExceededInDecember_rollsCloseYmToNextYearJanuary() {
        doAnswer(
                        inv -> {
                            DateutLinkParm p = inv.getArgument(0);
                            p.getKdate().setKdDate1(20261215);
                            return null;
                        })
                .when(dateutService)
                .execute(any());
        custf.getRecord().setInt("CU-CLOSE-DAY", 10); // sysdate day (15) > close day (10)
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        assertThat(invhfWriteSnapshot.get("IH-CLOSE-YM")).isEqualTo(202701);
    }

    // ───────────────────────── main screen key validation (ground truth: MAINR-010)
    // ─────────────────────────

    @Test
    void execute_mainKeyInvalidFunctionKey_showsMessageAndKeepsMainLoopOpen() {
        when(renderer.readEndStatus()).thenReturn("09", "03");

        service.execute();

        verify(invhf, never()).readByKey(any());
        assertThat(msgLineHistory).contains("Invalid function key");
        assertThat(capturedWs().getCompletionCode()).isEqualTo(0);
    }

    // ───────────────────────── PROCESS-CREDIT validation (ground truth:
    // PC-010/RSI-010/RC-010/LSL-010) ─────────────────────────

    @Test
    void execute_origInvoiceZero_rejectsWithRequiredMessage() {
        acceptValues.put("WK-ORIG-INV", "0");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(invhf, never()).readByKey(any());
        assertThat(msgLineHistory).contains("Invoice number required");
    }

    @Test
    void execute_origInvoiceNotFound_rejectsWithNotFoundMessage() {
        doReturn(true).when(invhf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(custf, never()).readByKey(any());
        assertThat(msgLineHistory).contains("Original invoice not found");
    }

    @Test
    void execute_origInvoiceDeleted_rejectsWithDeletedMessage() {
        invhf.getRecord().setInt("IH-DEL-FLAG", 1);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(custf, never()).readByKey(any());
        assertThat(msgLineHistory).contains("Original invoice is deleted");
    }

    @Test
    void execute_origInvoiceNotASale_rejectsWithCannotCreditMessage() {
        invhf.getRecord().setInt("IH-KIND", 2); // already a credit note
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(custf, never()).readByKey(any());
        assertThat(msgLineHistory).contains("Source is not a sale - cannot credit");
    }

    @Test
    void execute_origInvoiceCancelled_rejectsWithCancelledMessage() {
        invhf.getRecord().setInt("IH-STATUS", 9);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(custf, never()).readByKey(any());
        assertThat(msgLineHistory).contains("Original invoice is cancelled");
    }

    @Test
    void execute_customerOfInvoiceNotFound_rejectsWithNotFoundMessage() {
        doReturn(true).when(custf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(msgLineHistory).contains("Customer of invoice not found");
    }

    @Test
    void execute_sourceInvoiceHasNoLines_rejectsWithNoLinesMessage() {
        invdfLineQueue.clear();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(msgLineHistory).contains("Source invoice has no lines");
    }

    @Test
    void execute_unknownProductOnSourceLine_stillCreditsLineWithoutStockUpdate() {
        doReturn(true).when(prodf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        verify(invhf, times(1)).write();
        verify(stokf, never()).readByKey(any());
        assertThat(invhfWriteSnapshot.get("IH-LINES")).isEqualTo(1);
    }

    @Test
    void execute_negativeQtyOnSourceLine_usesAbsoluteValueAsOriginalQty() {
        invdfLineQueue.clear();
        invdfLineQueue.add(
                invdfLine(
                        1001L,
                        1,
                        500,
                        1,
                        new BigDecimal("-10"),
                        new BigDecimal("25"),
                        new BigDecimal("15"),
                        1));
        acceptValues.put("WK-PICK-QTY", "11"); // exceeds abs(-10)=10
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(msgLineHistory).contains("Credit qty exceeds invoiced qty");
    }

    // ───────────────────────── PICK-LOOP (ground truth: PKL-010/SPQ-010/CPL-010/PGD-010/PGU-010)
    // ─────────────────────────

    @Test
    void execute_pickLineOutOfRange_showsInvalidLineMessageAndDiscards() {
        acceptValues.put("WK-PICK-LINE", "99");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(msgLineHistory)
                .contains(
                        "Enter a valid source line number",
                        "No quantities picked - credit discarded");
    }

    @Test
    void execute_pickQtyNotPositive_showsQtyMustBePositiveMessage() {
        acceptValues.put("WK-PICK-QTY", "0");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(msgLineHistory).contains("Credit qty must be positive");
    }

    @Test
    void execute_pickQtyExceedsInvoicedQty_showsExceedsMessage() {
        acceptValues.put("WK-PICK-QTY", "999");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(msgLineHistory).contains("Credit qty exceeds invoiced qty");
    }

    @Test
    void execute_clearPickLineEsts04_clearsQuantityAndDiscardsWhenNoneLeft() {
        when(renderer.readEndStatus()).thenReturn("00", "00", "04", "03", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(msgLineHistory)
                .contains(
                        "Credit quantity set",
                        "Credit quantity cleared",
                        "No quantities picked - credit discarded");
    }

    @Test
    void execute_pageDownAtLastPage_isNoOpButKeepsPickLoopOpen() {
        when(renderer.readEndStatus()).thenReturn("00", "06", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Last page");
        assertThat(capturedWs().getWkPageTop()).isEqualTo(1);
    }

    @Test
    void execute_pageUpAtFirstPage_isNoOpButKeepsPickLoopOpen() {
        when(renderer.readEndStatus()).thenReturn("00", "12", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("First page");
        assertThat(capturedWs().getWkPageTop()).isEqualTo(1);
    }

    @Test
    void execute_pickLoopInvalidFunctionKey_showsMessageAndKeepsLoopOpen() {
        when(renderer.readEndStatus()).thenReturn("00", "09", "03", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Invalid function key");
    }

    @Test
    void execute_noQuantitiesPicked_discardsCreditWithoutTaxCalcOrNumgen() {
        when(renderer.readEndStatus()).thenReturn("00", "03", "03");

        service.execute();

        verify(invhf, never()).write();
        verify(numgenService, never()).execute(any());
        assertThat(msgLineHistory).contains("No quantities picked - credit discarded");
    }

    // ───────────────────────── ASK-CONFIRM (ground truth: AC-010) ─────────────────────────

    @Test
    void execute_confirmDeclined_discardsCreditWithoutSaving() {
        acceptValues.put("SC-CONF", "N");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        verify(invhf, never()).write();
        assertThat(msgLineHistory).contains("Credit note discarded");
    }

    @Test
    void execute_confirmLowercaseY_stillPostsCredit() {
        acceptValues.put("SC-CONF", "y");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        verify(invhf, times(1)).write();
    }

    // ───────────────────────── POST-CREDIT failures (ground truth: PST-010/WCH-010)
    // ─────────────────────────

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
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        verify(invhf, never()).write();
        verify(invdf, never()).write();
        assertThat(msgLineHistory).contains("Credit number assignment failed");
    }

    @Test
    void execute_invhfWriteInvalidKey_stillWritesDetailAndCompletesPosting() {
        doReturn(false, true).when(invhf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        verify(invhf, times(1)).write();
        verify(invdf, times(1)).write();
        assertThat(msgLineHistory)
                .contains("Credit header write failed", "Credit note 0000009001 posted");
    }

    // ───────────────────────── stock posting (ground truth: IS-010/CRS-010/WM-010)
    // ─────────────────────────

    @Test
    void execute_stockManagedExistingRecord_increasesOnHandAndWritesMovement() {
        prodf.getRecord().setInt("PR-STOCK-MNG", 1);
        doReturn(false, false).when(stokf).isInvalidKey();
        stokf.getRecord().setDecimal("SK-ONHAND", new BigDecimal("50"));
        stokf.getRecord().setDecimal("SK-YTD-IN", new BigDecimal("0"));
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        verify(stokf, times(1)).rewrite();
        verify(stokf, never()).write();
        verify(smovf, times(1)).write();
        assertThat(stokf.getRecord().getDecimal("SK-ONHAND"))
                .isEqualByComparingTo(new BigDecimal("55")); // 50 + 5
        assertThat(smovfWriteSnapshot.get("SM-KIND")).isEqualTo(50);
        assertThat(smovfWriteSnapshot.get("SM-REF-TYPE")).isEqualTo(3);
        assertThat((BigDecimal) smovfWriteSnapshot.get("SM-QTY"))
                .isEqualByComparingTo(new BigDecimal("5"));
        assertThat((BigDecimal) smovfWriteSnapshot.get("SM-BAL-AFTER"))
                .isEqualByComparingTo(new BigDecimal("55"));
    }

    @Test
    void execute_stockManagedNoExistingRecord_createsStockRecordAndWritesMovement() {
        prodf.getRecord().setInt("PR-STOCK-MNG", 1);
        doReturn(true, false)
                .when(stokf)
                .isInvalidKey(); // not found -> create, then write succeeds
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        verify(stokf, times(1)).write();
        verify(stokf, never()).rewrite();
        verify(smovf, times(1)).write();
        assertThat(stokf.getRecord().getDecimal("SK-ONHAND"))
                .isEqualByComparingTo(new BigDecimal("5"));
        assertThat(stokf.getRecord().getDecimal("SK-AVG-COST"))
                .isEqualByComparingTo(new BigDecimal("15"));
    }

    @Test
    void execute_stockRewriteInvalidKey_showsStockUpdateFailedMessage() {
        prodf.getRecord().setInt("PR-STOCK-MNG", 1);
        doReturn(false, true).when(stokf).isInvalidKey(); // found, then rewrite fails
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Stock update failed");
    }

    @Test
    void execute_stockCreateInvalidKey_showsStockCreateFailedMessage() {
        prodf.getRecord().setInt("PR-STOCK-MNG", 1);
        doReturn(true, true)
                .when(stokf)
                .isInvalidKey(); // not found -> create, then create write fails
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Stock create failed");
        verify(smovf, never()).write(); // WK-STK-FOUND stays 0 -> movement skipped
    }

    @Test
    void execute_stockNotManaged_skipsStockReadWriteAndMovement() {
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        verify(stokf, never()).readByKey(any());
        verify(stokf, never()).write();
        verify(stokf, never()).rewrite();
        verify(smovf, never()).write();
    }

    @Test
    void execute_stkmovNumgenFails_showsMessageAndSkipsMovementWriteButKeepsStockRewrite() {
        prodf.getRecord().setInt("PR-STOCK-MNG", 1);
        doReturn(false, false).when(stokf).isInvalidKey();
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
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        verify(stokf, times(1)).rewrite();
        verify(smovf, never()).write();
        assertThat(msgLineHistory).contains("Movement number assignment failed");
    }

    // ───────────────────────── AR ledger / customer balance (ground truth: PAL-010/UCB-010)
    // ─────────────────────────

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
                                p.getKnum().setKnumNumber(9001L);
                            }
                            return null;
                        })
                .when(numgenService)
                .execute(any());
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        verify(arlf, never()).write();
        verify(custf, times(1)).rewrite();
        assertThat(msgLineHistory).contains("AR ledger number assignment failed");
    }

    @Test
    void execute_arLedgerWriteInvalidKey_showsArWriteFailedMessage() {
        doReturn(true).when(arlf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("AR ledger write failed");
    }

    @Test
    void execute_customerNotFoundOnBalanceUpdate_skipsRewriteSilently() {
        doReturn(false, true).when(custf).isInvalidKey(); // RC-010 found, UCB-010 not found
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        verify(custf, never()).rewrite();
        assertThat(msgLineHistory).doesNotContain("Customer balance update failed");
    }

    @Test
    void execute_customerBalanceRewriteInvalidKey_showsBalanceUpdateFailedMessage() {
        doReturn(false, false, true).when(custf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03", "00", "03");

        service.execute();

        assertThat(msgLineHistory).contains("Customer balance update failed");
    }

    // ───────────────────────── file open lifecycle (ground truth: OPEN-FILES/OIxx-010/ABEND-RTN)
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
        // PRODF's OPEN-FILES has no post-open FSTS check / no ABEND unlike
        // INVHF/INVDF/STOKF/SMOVF/CUSTF/ARLF. A persistently-bad PRODF status must not
        // abort the program.
        doReturn("99").when(prodf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(abortxService, never()).execute(any());
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }
}

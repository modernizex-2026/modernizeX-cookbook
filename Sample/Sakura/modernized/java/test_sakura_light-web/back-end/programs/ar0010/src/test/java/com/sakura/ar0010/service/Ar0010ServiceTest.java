package com.sakura.ar0010.service;

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
import com.sakura.ar0010.domain.Ar0010FieldAccess;
import com.sakura.ar0010.io.RcptfDataset;
import com.sakura.ar0010.runtime.Ar0010Datasets;
import com.sakura.dateut.service.DateutService;
import com.sakura.numgen.service.NumgenService;
import com.sakura.runtime.DatasetEnums.FileOpenMode;
import com.sakura.runtime.ScreenModels.ScreenDef;
import com.sakura.runtime.ScreenRendererInstance;
import com.sakura.runtime.io.ArlfDataset;
import com.sakura.runtime.io.BankfDataset;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.linkage.NumgenLinkParm;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for Ar0010Service (COBOL AR0010 — cash receipt entry), generated from {@code
 * AR0010.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>Test strategy: RCPTF/ARLF/CUSTF/BANKF are real dataset objects wrapped with {@code spy()} so
 * the record buffer (and therefore {@code Ar0010FieldAccess}, which registers those buffers) works
 * exactly as in production; only the I/O methods
 * (open/close/write/rewrite/readByKey/getFileStatus/isInvalidKey) are stubbed so no real file/DB
 * access happens. DATEUT runs for real (pure calendar math, safe and exactly matches the COBOL
 * copy); NUMGEN and ABORTX are mocked since they involve file I/O irrelevant to AR0010's own logic.
 *
 * <p>RCPTF's buffer is cleared by CLR-010 (resetReceiptWorkArea) at the *start* of every loop cycle
 * — including the terminating cycle every test needs (PF3 ends the program only on the header
 * prompt of the NEXT cycle). So inspecting rcptf's buffer after execute() would see the wiped
 * state, not what was written. To keep the ground truth check honest, receipt field values are
 * captured in {@link #rcptWriteSnapshot} at the moment write() is invoked, before the next cycle's
 * reset can clear them. ARLF/CUSTF buffers are not reset mid-run in any of these scenarios, so
 * their post-execute buffer state is asserted directly.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class Ar0010ServiceTest {

    @Mock private ScreenRendererInstance renderer;
    @Mock private NumgenService numgenService;
    @Mock private AbortxService abortxService;

    private DateutService dateutService;

    private Ar0010Datasets fileSet;
    private RcptfDataset rcptf;
    private ArlfDataset arlf;
    private CustfDataset custf;
    private BankfDataset bankf;

    private Ar0010Service service;

    private final Map<String, String> acceptValues = new HashMap<>();
    private final List<String> screenInteractions = new ArrayList<>();
    private final Map<String, Object> rcptWriteSnapshot = new LinkedHashMap<>();

    /**
     * WK-MSG-LINE trimmed at every displayScreen() call. Needed because EHDR-010 unconditionally
     * resets WK-MSG-LINE to "Enter receipt details..." at the top of every cycle — including the
     * terminating cycle every test drives through to end the RCPT-LOOP — so the FINAL ws state
     * never shows a cycle-1 message. History-based assertion sidesteps that without caring about
     * ordering.
     */
    private final List<String> msgLineHistory = new ArrayList<>();

    @BeforeEach
    void setUp() {
        dateutService = new DateutService();

        Ar0010Datasets real = new Ar0010Datasets();
        fileSet = spy(real);
        rcptf = spy(real.getRcptf());
        arlf = spy(real.getArlf());
        custf = spy(real.getCustf());
        bankf = spy(real.getBankf());
        doReturn(rcptf).when(fileSet).getRcptf();
        doReturn(arlf).when(fileSet).getArlf();
        doReturn(custf).when(fileSet).getCustf();
        doReturn(bankf).when(fileSet).getBankf();

        doNothing().when(rcptf).open(any());
        doNothing().when(arlf).open(any());
        doNothing().when(custf).open(any());
        doNothing().when(bankf).open(any());
        doNothing().when(rcptf).close();
        doNothing().when(arlf).close();
        doNothing().when(custf).close();
        doNothing().when(bankf).close();
        doNothing().when(arlf).write();
        doNothing().when(custf).rewrite();

        // Captures the RE-* buffer at the instant of WRITE — before the next cycle's
        // CLR-010 reset wipes it (see class javadoc).
        doAnswer(
                        inv -> {
                            rcptWriteSnapshot.put("RE-NO", rcptf.getRecord().getLong("RE-NO"));
                            rcptWriteSnapshot.put("RE-CUST", rcptf.getRecord().getInt("RE-CUST"));
                            rcptWriteSnapshot.put("RE-DATE", rcptf.getRecord().getInt("RE-DATE"));
                            rcptWriteSnapshot.put(
                                    "RE-METHOD", rcptf.getRecord().getInt("RE-METHOD"));
                            rcptWriteSnapshot.put(
                                    "RE-AMOUNT", rcptf.getRecord().getDecimal("RE-AMOUNT"));
                            rcptWriteSnapshot.put(
                                    "RE-STATUS", rcptf.getRecord().getInt("RE-STATUS"));
                            rcptWriteSnapshot.put(
                                    "RE-DEL-FLAG", rcptf.getRecord().getInt("RE-DEL-FLAG"));
                            rcptWriteSnapshot.put(
                                    "RE-CLOSE-YM", rcptf.getRecord().getInt("RE-CLOSE-YM"));
                            rcptWriteSnapshot.put(
                                    "RE-BANK-CODE", rcptf.getRecord().getInt("RE-BANK-CODE"));
                            return null;
                        })
                .when(rcptf)
                .write();

        // Default: customer found, active, balance 500000.
        doReturn(true).when(custf).readByKey(any());
        doReturn(false).when(custf).isInvalidKey();
        custf.getRecord().setString("CU-NAME", "ACME Corp");
        custf.getRecord().setDecimal("CU-BALANCE", new BigDecimal("500000"));
        custf.getRecord().setInt("CU-DEL-FLAG", 0);

        // Default: bank found, active.
        doReturn(true).when(bankf).readByKey(any());
        doReturn(false).when(bankf).isInvalidKey();
        bankf.getRecord().setString("BK-NAME", "Sakura Bank");
        bankf.getRecord().setInt("BK-DEL-FLAG", 0);

        acceptValues.put("RE-DATE", "20260315");
        acceptValues.put("RE-CUST", "100");
        acceptValues.put("RE-METHOD", "1");
        acceptValues.put("RE-AMOUNT", "1000");
        acceptValues.put("RE-BANK-CODE", "0");
        acceptValues.put("RE-REMARK", "Test remark");
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
                            if (w instanceof Ar0010FieldAccess a) {
                                msgLineHistory.add(a.getWkMsgLine().trim());
                            }
                            return null;
                        })
                .when(renderer)
                .displayScreen(any(), any());

        // Default NUMGEN: any key succeeds with number 9001.
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("00");
                            p.getKnum().setKnumNumber(9001L);
                            return null;
                        })
                .when(numgenService)
                .execute(any());

        service = new Ar0010Service(fileSet, dateutService, numgenService, abortxService, renderer);
    }

    /**
     * Live handle onto the service's WorkingStorage/FD field accessor, obtained from the first
     * displayScreen() call. It is the SAME mutable object throughout the run, so fields not reset
     * by a later cycle's CLR-010 (e.g. WK-MSG-LINE, WK-SESS-CNT) safely reflect end-of-run state.
     */
    private Ar0010FieldAccess capturedWs() {
        ArgumentCaptor<RuntimeFieldAccess> captor =
                ArgumentCaptor.forClass(RuntimeFieldAccess.class);
        verify(renderer, org.mockito.Mockito.atLeastOnce()).displayScreen(any(), captor.capture());
        return (Ar0010FieldAccess) captor.getAllValues().get(0);
    }

    // ───────────────────────── happy path ─────────────────────────

    @Test
    void execute_cashReceiptValidCustomer_savesReceiptPostsLedgerAndUpdatesBalance() {
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(rcptf, times(1)).write();
        verify(arlf, times(1)).write();
        verify(custf, times(1)).rewrite();
        assertThat(rcptWriteSnapshot.get("RE-NO")).isEqualTo(9001L);
        assertThat(rcptWriteSnapshot.get("RE-CUST")).isEqualTo(100);
        assertThat(rcptWriteSnapshot.get("RE-DATE")).isEqualTo(20260315);
        assertThat(rcptWriteSnapshot.get("RE-METHOD")).isEqualTo(1);
        assertThat(rcptWriteSnapshot.get("RE-AMOUNT")).isEqualTo(new BigDecimal("1000"));
        assertThat(rcptWriteSnapshot.get("RE-STATUS")).isEqualTo(0);
        assertThat(rcptWriteSnapshot.get("RE-DEL-FLAG")).isEqualTo(0);
        assertThat(rcptWriteSnapshot.get("RE-CLOSE-YM")).isEqualTo(202603);

        assertThat(arlf.getRecord().getLong("AL-SEQ")).isEqualTo(9001L);
        assertThat(arlf.getRecord().getInt("AL-CUST")).isEqualTo(100);
        assertThat(arlf.getRecord().getInt("AL-KIND")).isEqualTo(2);
        assertThat(arlf.getRecord().getInt("AL-REF-TYPE")).isEqualTo(26);
        assertThat(arlf.getRecord().getDecimal("AL-DEBIT")).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(arlf.getRecord().getDecimal("AL-CREDIT"))
                .isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(arlf.getRecord().getLong("AL-BALANCE")).isEqualTo(499000L);
        assertThat(arlf.getRecord().getString("AL-REMARK").trim()).isEqualTo("Test remark");

        assertThat(custf.getRecord().getDecimal("CU-BALANCE"))
                .isEqualByComparingTo(new BigDecimal("499000"));

        Ar0010FieldAccess ws = capturedWs();
        assertThat(msgLineHistory).contains("Receipt posted");
        assertThat(ws.getWkSessCnt()).isEqualTo(1);
        assertThat(ws.getWkSessAmt()).isEqualTo(1000L);
        assertThat(ws.getCompletionCode()).isEqualTo(0);
        assertThat(screenInteractions)
                .contains("displayScreen:DS-CONFIRM", "displayScreen:DS-HEAD");
    }

    @Test
    void execute_transferMethodWithValidBank_savesReceiptAndReadsBank() {
        acceptValues.put("RE-METHOD", "2");
        acceptValues.put("RE-BANK-CODE", "500");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(bankf, times(1)).readByKey(any());
        verify(rcptf, times(1)).write();
        assertThat(rcptWriteSnapshot.get("RE-METHOD")).isEqualTo(2);
        assertThat(rcptWriteSnapshot.get("RE-BANK-CODE")).isEqualTo(500);
        assertThat(msgLineHistory).contains("Receipt posted");
    }

    @Test
    void execute_confirmLowercaseY_stillSavesReceipt() {
        acceptValues.put("SC-CONF", "y");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(rcptf, times(1)).write();
        assertThat(msgLineHistory).contains("Receipt posted");
    }

    @Test
    void execute_confirmDeclined_discardsReceiptWithoutSaving() {
        acceptValues.put("SC-CONF", "N");
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(rcptf, never()).write();
        verify(arlf, never()).write();
        verify(custf, never()).rewrite();
        assertThat(msgLineHistory).contains("Receipt discarded");
    }

    @Test
    void execute_pf3AtFirstHeaderPrompt_endsImmediatelyWithoutEnteringAnyReceipt() {
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(rcptf, never()).write();
        verify(rcptf, times(1)).open(any());
        verify(rcptf, times(1)).close();
        assertThat(capturedWs().getCompletionCode()).isEqualTo(0);
    }

    @Test
    void execute_pf4Clear_showsClearedMessageAndContinuesLoop() {
        when(renderer.readEndStatus()).thenReturn("04", "03");

        service.execute();

        verify(rcptf, never()).write();
        assertThat(msgLineHistory).contains("Cleared");
    }

    // ───────────────────────── header validation (ground truth: VALIDATE-HEADER / VALIDATE-BANK)
    // ─────────────────────────

    @Test
    void execute_invalidReceiptDate_rejectsWithDateInvalidMessage() {
        acceptValues.put("RE-DATE", "20260230"); // 2026 is not a leap year: Feb 30 does not exist
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(rcptf, never()).write();
        assertThat(msgLineHistory).contains("Receipt date is invalid");
    }

    @Test
    void execute_customerCodeZero_rejectsWithCustomerRequiredMessage() {
        acceptValues.put("RE-CUST", "0");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(custf, never()).readByKey(any());
        verify(rcptf, never()).write();
        assertThat(msgLineHistory).contains("Customer code required");
    }

    @Test
    void execute_customerNotFound_rejectsWithNotFoundMessage() {
        doReturn(true).when(custf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(rcptf, never()).write();
        assertThat(msgLineHistory).contains("Customer not found");
    }

    @Test
    void execute_customerDeleted_rejectsWithDeletedMessage() {
        custf.getRecord().setInt("CU-DEL-FLAG", 1);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(rcptf, never()).write();
        assertThat(msgLineHistory).contains("Customer is deleted");
    }

    @Test
    void execute_amountNotPositive_rejectsWithAmountMessage() {
        acceptValues.put("RE-AMOUNT", "0");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(rcptf, never()).write();
        assertThat(msgLineHistory).contains("Amount must be positive");
    }

    @Test
    void execute_methodOutOfRange_rejectsWithMethodMessage() {
        acceptValues.put("RE-METHOD", "5");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(rcptf, never()).write();
        assertThat(msgLineHistory).contains("Method must be 1 to 4");
    }

    @Test
    void execute_transferMethodMissingBankCode_rejectsWithBankRequiredMessage() {
        acceptValues.put("RE-METHOD", "2");
        acceptValues.put("RE-BANK-CODE", "0");
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(bankf, never()).readByKey(any());
        verify(rcptf, never()).write();
        assertThat(msgLineHistory).contains("Bank code required for transfer");
    }

    @Test
    void execute_bankCodeNotFound_rejectsWithBankNotFoundMessage() {
        acceptValues.put("RE-METHOD", "2");
        acceptValues.put("RE-BANK-CODE", "999");
        doReturn(true).when(bankf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(rcptf, never()).write();
        assertThat(msgLineHistory).contains("Bank code not found");
    }

    @Test
    void execute_bankDeleted_rejectsWithBankDeletedMessage() {
        acceptValues.put("RE-METHOD", "2");
        acceptValues.put("RE-BANK-CODE", "500");
        bankf.getRecord().setInt("BK-DEL-FLAG", 1);
        when(renderer.readEndStatus()).thenReturn("00", "03");

        service.execute();

        verify(rcptf, never()).write();
        assertThat(msgLineHistory).contains("Bank is deleted");
    }

    // ───────────────────────── save-path failures (ground truth: SAVE-RCPT / POST-LEDGER /
    // UPDATE-CUST-BAL) ─────────────────────────

    @Test
    void execute_numgenReceiptNumberFails_doesNotWriteReceipt() {
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            p.getKnum().setKnumStatus("99");
                            return null;
                        })
                .when(numgenService)
                .execute(any());
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(rcptf, never()).write();
        verify(arlf, never()).write();
        assertThat(msgLineHistory).contains("Receipt number assignment failed");
    }

    @Test
    void execute_rcptfWriteInvalidKey_showsWriteFailedAndSkipsLedgerAndBalance() {
        doReturn(true).when(rcptf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(rcptf, times(1)).write();
        verify(arlf, never()).write();
        verify(custf, never()).rewrite();
        assertThat(msgLineHistory).contains("Receipt write failed");
    }

    /**
     * CONVERT-GAP CHECK (none found): COBOL SAV-010 runs PERFORM POST-LEDGER then PERFORM
     * UPDATE-CUST-BAL unconditionally — even when POST-LEDGER's ledger number assignment fails,
     * UPDATE-CUST-BAL still executes and the session counters still advance.
     * Ar0010Service.saveReceiptRecord() mirrors this exactly (no early return after
     * postLedgerEntry()). This test locks that faithful (if surprising) behavior in place.
     */
    @Test
    void execute_ledgerNumgenFails_stillUpdatesCustomerBalanceAndSessionCounters() {
        doAnswer(
                        inv -> {
                            NumgenLinkParm p = inv.getArgument(0);
                            String key = p.getKnum().getKnumKey();
                            if (key != null && "ARLDG".equals(key.trim())) {
                                p.getKnum().setKnumStatus("99");
                            } else {
                                p.getKnum().setKnumStatus("00");
                                p.getKnum().setKnumNumber(9001L);
                            }
                            return null;
                        })
                .when(numgenService)
                .execute(any());
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(rcptf, times(1)).write();
        verify(arlf, never()).write();
        verify(custf, times(1)).rewrite();
        assertThat(custf.getRecord().getDecimal("CU-BALANCE"))
                .isEqualByComparingTo(new BigDecimal("499000"));
        assertThat(capturedWs().getWkSessCnt()).isEqualTo(1);
    }

    /**
     * CONVERT-GAP CHECK (none found): COBOL PLG-010's WRITE AL-REC INVALID KEY has no GO TO — it
     * falls through to PLG-999 EXIT, and the caller SAV-010 still runs UPDATE-CUST-BAL next.
     * Ar0010Service.postLedgerEntry() matches (no return after the invalid-key branch).
     */
    @Test
    void execute_ledgerWriteInvalidKey_stillUpdatesCustomerBalance() {
        doReturn(true).when(arlf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(arlf, times(1)).write();
        verify(custf, times(1)).rewrite();
        assertThat(custf.getRecord().getDecimal("CU-BALANCE"))
                .isEqualByComparingTo(new BigDecimal("499000"));
    }

    @Test
    void execute_customerBalanceReadFailsDuringSave_showsUpdateFailedMessage() {
        // First isInvalidKey() call validates the header (must succeed); the second,
        // inside UPDATE-CUST-BAL's re-read, fails.
        doReturn(false, true).when(custf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(rcptf, times(1)).write();
        verify(custf, never()).rewrite();
        assertThat(msgLineHistory).contains("Customer balance update failed");
    }

    @Test
    void execute_customerBalanceRewriteFails_showsUpdateFailedMessage() {
        // isInvalidKey() is checked 3 times in this scenario: header validation's
        // CUSTF read (must pass), UPDATE-CUST-BAL's re-read (must pass), then the
        // post-REWRITE check (fails).
        doReturn(false, false, true).when(custf).isInvalidKey();
        when(renderer.readEndStatus()).thenReturn("00", "00", "03");

        service.execute();

        verify(custf, times(1)).rewrite();
        assertThat(msgLineHistory).contains("Customer balance update failed");
    }

    // ───────────────────────── file open lifecycle (ground truth: OPEN-FILES / ABEND-RTN)
    // ─────────────────────────

    @Test
    void execute_rcptfOpenFailsPersistently_abortsWithCompletionCode255() {
        doReturn("99").when(rcptf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(255);
        verify(abortxService, times(1)).execute(any());
        verify(rcptf, never()).write();
        verify(arlf, never()).open(any());
        verify(custf, never()).open(any());
    }

    @Test
    void execute_rcptfOpenStatus35_reopensAsOutputThenReopensIo() {
        doReturn("35", "00", "00", "00").when(rcptf).getFileStatus();
        when(renderer.readEndStatus()).thenReturn("03");

        service.execute();

        verify(rcptf, times(3)).open(any());
        verify(rcptf, times(1)).open(FileOpenMode.OUTPUT);
        verify(rcptf, times(2)).open(FileOpenMode.IO);
        verify(rcptf, times(2)).close();
        assertThat(service.getCompletionCode()).isEqualTo(0);
    }
}

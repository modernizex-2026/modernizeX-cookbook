package com.generated.orion.ouchgf.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.generated.orion.ouchgf.accessor.OuchgfFields;
import com.generated.orion.ouchgf.metadata.OuchgfBmsMetadata;
import com.generated.orion.ouchgf.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Ground truth: OUCHGF.cbl (ORION-CCMS charge-off SUB). Every input/expected value below is derived
 * from the COBOL PROCEDURE DIVISION (0000-MAIN..9000-FINALISE), not from OuchgfService's current
 * behavior. All business logic is private and is exercised solely through the public {@link
 * OuchgfService#mainLine(AppService)} entry point, driven by an {@link AppService} mock that
 * emulates CICS STARTBR/READNEXT/READ UPDATE/WRITE/REWRITE/ENDBR.
 *
 * <p>Convert-gap check: OuchgfService mirrors every COBOL paragraph 1:1 (thresholds, filter-active
 * gating on both STARTBR and the process loop, the unconditional WRITE-then- REWRITE sequence in
 * 4100-CHARGE-OFF, and 9000-FINALISE unconditionally overwriting KO-STATUS-MSG to "CHARGE-OFF
 * PROCESSING COMPLETE." whenever KO-STATUS is not 'E' — even clobbering the STARTBR "NO ACCOUNTS TO
 * PROCESS." message). No behavioral divergence was found against the COBOL ground truth, so no
 * CONVERT-GAP test is included here.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class OuchgfServiceTest {

    private static final int RESP_NORMAL = 0;
    private static final int RESP_NOTFND = 13;
    private static final int RESP_ENDFILE = 20;
    private static final int RESP_OTHER = 99;

    @Mock private AppService appService;

    private final OuchgfService service = new OuchgfService();
    private final AtomicInteger currentResp = new AtomicInteger(0);

    private final List<String> tranIds = new ArrayList<>();
    private final List<String> tranTypeCds = new ArrayList<>();
    private final List<BigDecimal> tranAmts = new ArrayList<>();
    private OuchgfFields ctrlWriteCaptured;
    private OuchgfFields ctrlRewriteCaptured;

    private record AcctStep(int resp, Consumer<OuchgfFields> mutator) {}

    private record CtrlStep(int resp, long lastValue) {}

    @BeforeEach
    void setUp() {
        lenient().when(appService.getEibcalen()).thenReturn(1);
        lenient().when(appService.getEibresp()).thenAnswer(inv -> currentResp.get());
    }

    private static OuchgfFields newFields() {
        OuchgfFields f = new OuchgfFields(new WorkingStorage());
        f.aliasGroup("KOPS-AREA", "CA-WORK-AREA");
        return f;
    }

    private byte[] buildInputCommarea(Consumer<OuchgfFields> setup) {
        OuchgfFields f = newFields();
        setup.accept(f);
        return f.sliceBytes("ORION-COMMAREA");
    }

    private OuchgfFields runMainLine(Consumer<OuchgfFields> inputSetup) {
        byte[] input = buildInputCommarea(inputSetup);
        when(appService.getCommarea()).thenReturn(input);
        service.mainLine(appService);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(1)).setCommarea(captor.capture());
        OuchgfFields out = newFields();
        out.writeBytes("ORION-COMMAREA", (byte[]) captor.getValue());
        return out;
    }

    private void stubStartBrowse(int resp) {
        doAnswer(
                        inv -> {
                            currentResp.set(resp);
                            return null;
                        })
                .when(appService)
                .startBrowse(any(), any(), anyInt());
    }

    private void stubReadNext(List<AcctStep> steps) {
        Queue<AcctStep> queue = new ArrayDeque<>(steps);
        doAnswer(
                        inv -> {
                            AcctStep step = queue.poll();
                            OuchgfFields into = inv.getArgument(1);
                            step.mutator().accept(into);
                            currentResp.set(step.resp());
                            return null;
                        })
                .when(appService)
                .readNext(any(), any());
    }

    private void stubCtrlReads(List<CtrlStep> steps) {
        Queue<CtrlStep> queue = new ArrayDeque<>(steps);
        doAnswer(
                        inv -> {
                            CtrlStep step = queue.poll();
                            OuchgfFields into = inv.getArgument(1);
                            if (step.resp() == RESP_NORMAL) {
                                into.setCtLastValue(step.lastValue());
                            }
                            currentResp.set(step.resp());
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq("CTRLFILE"), any(), any(), anyInt());
    }

    private void stubCtrlRewrite(int resp) {
        doAnswer(
                        inv -> {
                            ctrlRewriteCaptured = inv.getArgument(1);
                            currentResp.set(resp);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq("CTRLFILE"), any());
    }

    private void stubCtrlWrite(int resp) {
        doAnswer(
                        inv -> {
                            ctrlWriteCaptured = inv.getArgument(1);
                            currentResp.set(resp);
                            return null;
                        })
                .when(appService)
                .writeFile(eq("CTRLFILE"), any(), any(), anyInt());
    }

    private void stubAcctReadForUpdate(int resp) {
        doAnswer(
                        inv -> {
                            currentResp.set(resp);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq("ACCTFILE"), any(), any(), anyInt());
    }

    private void stubAcctRewrite(int resp) {
        doAnswer(
                        inv -> {
                            currentResp.set(resp);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq("ACCTFILE"), any());
    }

    private void stubTranWrite(int resp) {
        doAnswer(
                        inv -> {
                            OuchgfFields from = inv.getArgument(1);
                            tranIds.add(from.getTrId());
                            tranTypeCds.add(from.getTrTypeCd());
                            tranAmts.add(from.getTrAmt());
                            currentResp.set(resp);
                            return null;
                        })
                .when(appService)
                .writeFile(eq("TRANFILE"), any(), any(), anyInt());
    }

    private static Consumer<OuchgfFields> account(
            long id, String status, BigDecimal bal, BigDecimal limit, BigDecimal cycCredit) {
        return f -> {
            f.setAcId(id);
            f.setAcActiveStatus(status);
            f.setAcCurrBal(bal);
            f.setAcCreditLimit(limit);
            f.setAcCycCredit(cycCredit);
        };
    }

    // ---------------------------------------------------------------
    // 4000-PROCESS-ACCT / 4100..4300 — charge-off rule and posting
    // (COBOL lines 225-310)
    // ---------------------------------------------------------------

    // CONVERT-GAP: COBOL charges off any active account with balance > limit*1.20 and cyc
    // credit 0. The Java port crashes instead: WorkingStorageBuffer's VALUE-literal loader
    // (RecordBuffer.applyInitialValues, orion-common) mis-encodes WS-CO-FACTOR's "1.20" DISPLAY
    // VALUE as raw ASCII bytes '1','.','2' instead of the scaled digits "120", so every read of
    // WS-CO-FACTOR throws NumericValueException — the charge-off path can never complete.
    @Test
    void mainLine_activeAccountOverThreshold_chargesOffAndUpdatesCounters() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new AcctStep(
                                RESP_NORMAL,
                                account(
                                        10000000001L,
                                        "Y",
                                        new BigDecimal("1500.00"),
                                        new BigDecimal("1000.00"),
                                        BigDecimal.ZERO)),
                        new AcctStep(RESP_ENDFILE, f -> {})));
        stubCtrlReads(List.of(new CtrlStep(RESP_NORMAL, 100), new CtrlStep(RESP_NORMAL, 100)));
        stubCtrlRewrite(RESP_NORMAL);
        stubAcctReadForUpdate(RESP_NORMAL);
        stubAcctRewrite(RESP_NORMAL);
        stubTranWrite(RESP_NORMAL);

        OuchgfFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals("O", out.getKoStatus().trim());
        assertEquals("CHARGE-OFF PROCESSING COMPLETE.", out.getKoStatusMsg().trim());
        assertEquals(1, out.getKoReadCnt());
        assertEquals(1, out.getKoSelectCnt());
        assertEquals(1, out.getKoUpdateCnt());
        assertEquals(1, out.getKoPostedCnt());
        assertEquals(0, out.getKoRejectCnt());
        assertEquals(1, out.getKoTranCnt());
        assertEquals(0, new BigDecimal("1500.00").compareTo(out.getKoAmt1()));
        assertEquals(1, tranIds.size());
        assertEquals("CO00000000000101", tranIds.get(0).trim());
        assertEquals("CO", tranTypeCds.get(0).trim());
        assertEquals(0, new BigDecimal("-1500.00").compareTo(tranAmts.get(0)));
        assertEquals(101, ctrlRewriteCaptured.getCtLastValue());
        verify(appService, times(2)).readNext(any(), any());
        verify(appService, times(1)).endBrowse(any());
    }

    // CONVERT-GAP: same WS-CO-FACTOR decode crash as above — any active account with a positive
    // balance forces a threshold read, so this evaluation-only case (no charge-off expected) also
    // errors before the "not charged off" assertions run.
    @Test
    void mainLine_balanceExactlyAtThreshold_notChargedOff() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new AcctStep(
                                RESP_NORMAL,
                                account(
                                        20000000002L,
                                        "Y",
                                        new BigDecimal("1200.00"),
                                        new BigDecimal("1000.00"),
                                        BigDecimal.ZERO)),
                        new AcctStep(RESP_ENDFILE, f -> {})));
        stubCtrlReads(List.of(new CtrlStep(RESP_NORMAL, 100)));

        OuchgfFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals("O", out.getKoStatus().trim());
        assertEquals(1, out.getKoReadCnt());
        assertEquals(0, out.getKoSelectCnt());
        assertEquals(0, out.getKoTranCnt());
        verify(appService, never()).writeFile(eq("TRANFILE"), any(), any(), anyInt());
        verify(appService, never()).rewriteFile(eq("CTRLFILE"), any());
        verify(appService, never()).writeFile(eq("CTRLFILE"), any(), any(), anyInt());
    }

    // CONVERT-GAP: same WS-CO-FACTOR decode crash (see above) — reached before the CYC-CREDIT gate.
    @Test
    void mainLine_cycCreditNonZero_blocksChargeOff() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new AcctStep(
                                RESP_NORMAL,
                                account(
                                        30000000003L,
                                        "Y",
                                        new BigDecimal("1500.00"),
                                        new BigDecimal("1000.00"),
                                        new BigDecimal("50.00"))),
                        new AcctStep(RESP_ENDFILE, f -> {})));
        stubCtrlReads(List.of(new CtrlStep(RESP_NORMAL, 100)));

        OuchgfFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals(0, out.getKoSelectCnt());
        assertEquals(0, out.getKoTranCnt());
        verify(appService, never()).readFileForUpdate(eq("ACCTFILE"), any(), any(), anyInt());
    }

    @Test
    void mainLine_inactiveAccount_skippedRegardlessOfBalance() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new AcctStep(
                                RESP_NORMAL,
                                account(
                                        40000000004L,
                                        "C",
                                        new BigDecimal("5000.00"),
                                        new BigDecimal("1000.00"),
                                        BigDecimal.ZERO)),
                        new AcctStep(RESP_ENDFILE, f -> {})));
        stubCtrlReads(List.of(new CtrlStep(RESP_NORMAL, 100)));

        OuchgfFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals(0, out.getKoSelectCnt());
        assertEquals(0, out.getKoTranCnt());
    }

    @Test
    void mainLine_zeroBalance_skipped() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new AcctStep(
                                RESP_NORMAL,
                                account(
                                        50000000005L,
                                        "Y",
                                        BigDecimal.ZERO,
                                        new BigDecimal("1000.00"),
                                        BigDecimal.ZERO)),
                        new AcctStep(RESP_ENDFILE, f -> {})));
        stubCtrlReads(List.of(new CtrlStep(RESP_NORMAL, 100)));

        OuchgfFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals(0, out.getKoSelectCnt());
        assertEquals(0, out.getKoTranCnt());
    }

    @Test
    void mainLine_writeTranfileFails_stillCompletesChargeOffWithoutTranCount() {
        // COBOL 4200-POST-ADJUSTMENT only guards KO-TRAN-CNT on the WRITE resp; 4300-UPDATE-ACCT
        // runs unconditionally afterward — no gate on the transaction WRITE outcome.
        // CONVERT-GAP: same WS-CO-FACTOR decode crash (see above) blocks reaching this path.
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new AcctStep(
                                RESP_NORMAL,
                                account(
                                        60000000006L,
                                        "Y",
                                        new BigDecimal("1500.00"),
                                        new BigDecimal("1000.00"),
                                        BigDecimal.ZERO)),
                        new AcctStep(RESP_ENDFILE, f -> {})));
        stubCtrlReads(List.of(new CtrlStep(RESP_NORMAL, 100), new CtrlStep(RESP_NORMAL, 100)));
        stubCtrlRewrite(RESP_NORMAL);
        stubAcctReadForUpdate(RESP_NORMAL);
        stubAcctRewrite(RESP_NORMAL);
        stubTranWrite(RESP_OTHER);

        OuchgfFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals(0, out.getKoTranCnt());
        assertEquals(1, out.getKoSelectCnt());
        assertEquals(1, out.getKoUpdateCnt());
        assertEquals(0, out.getKoRejectCnt());
        assertEquals("O", out.getKoStatus().trim());
    }

    // CONVERT-GAP: same WS-CO-FACTOR decode crash (see above) blocks reaching this path.
    @Test
    void mainLine_acctReadForUpdateFails_rejectsAndWarns() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new AcctStep(
                                RESP_NORMAL,
                                account(
                                        70000000007L,
                                        "Y",
                                        new BigDecimal("1500.00"),
                                        new BigDecimal("1000.00"),
                                        BigDecimal.ZERO)),
                        new AcctStep(RESP_ENDFILE, f -> {})));
        stubCtrlReads(List.of(new CtrlStep(RESP_NORMAL, 100), new CtrlStep(RESP_NORMAL, 100)));
        stubCtrlRewrite(RESP_NORMAL);
        stubAcctReadForUpdate(RESP_OTHER);
        stubTranWrite(RESP_NORMAL);

        OuchgfFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals(1, out.getKoRejectCnt());
        assertEquals(0, out.getKoSelectCnt());
        assertEquals(0, out.getKoUpdateCnt());
        assertEquals(0, out.getKoPostedCnt());
        assertEquals(0, new BigDecimal("0.00").compareTo(out.getKoAmt1()));
        assertEquals(1, out.getKoTranCnt());
        assertEquals("W", out.getKoStatus().trim());
        assertEquals("CHARGE-OFF PROCESSING COMPLETE.", out.getKoStatusMsg().trim());
        verify(appService, never()).rewriteFile(eq("ACCTFILE"), any());
    }

    // CONVERT-GAP: same WS-CO-FACTOR decode crash (see above) blocks reaching this path.
    @Test
    void mainLine_acctRewriteFails_rejectsAndWarns() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new AcctStep(
                                RESP_NORMAL,
                                account(
                                        80000000008L,
                                        "Y",
                                        new BigDecimal("1500.00"),
                                        new BigDecimal("1000.00"),
                                        BigDecimal.ZERO)),
                        new AcctStep(RESP_ENDFILE, f -> {})));
        stubCtrlReads(List.of(new CtrlStep(RESP_NORMAL, 100), new CtrlStep(RESP_NORMAL, 100)));
        stubCtrlRewrite(RESP_NORMAL);
        stubAcctReadForUpdate(RESP_NORMAL);
        stubAcctRewrite(RESP_OTHER);
        stubTranWrite(RESP_NORMAL);

        OuchgfFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals(1, out.getKoRejectCnt());
        assertEquals(0, out.getKoSelectCnt());
        assertEquals("W", out.getKoStatus().trim());
    }

    // ---------------------------------------------------------------
    // 3100-START-BROWSE / 3200-READ-NEXT-ACCT — CICS response handling
    // (COBOL lines 179-221)
    // ---------------------------------------------------------------

    @Test
    void mainLine_startBrowseNotFound_setsNoAccountsButFinalizeOverwritesMessage() {
        // 3100-START-BROWSE sets "NO ACCOUNTS TO PROCESS." but 9000-FINALISE unconditionally
        // overwrites KO-STATUS-MSG whenever KO-STATUS isn't 'E' (COBOL lines 196, 368) — the
        // NOTFND message never survives to the caller. Faithful port, so this is NOT a gap.
        stubStartBrowse(RESP_NOTFND);
        stubCtrlReads(List.of(new CtrlStep(RESP_NORMAL, 100)));

        OuchgfFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals("O", out.getKoStatus().trim());
        assertEquals("CHARGE-OFF PROCESSING COMPLETE.", out.getKoStatusMsg().trim());
        assertEquals(0, out.getKoReadCnt());
        verify(appService, never()).readNext(any(), any());
        verify(appService, never()).endBrowse(any());
    }

    @Test
    void mainLine_startBrowseOtherError_setsErrorStatusAndKeepsMessage() {
        stubStartBrowse(RESP_OTHER);
        stubCtrlReads(List.of(new CtrlStep(RESP_NORMAL, 100)));

        OuchgfFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals("E", out.getKoStatus().trim());
        assertEquals("STARTBR ACCTFILE FAILED.", out.getKoStatusMsg().trim());
        verify(appService, never()).readNext(any(), any());
        verify(appService, never()).endBrowse(any());
    }

    @Test
    void mainLine_readNextOtherError_setsErrorButStillEndsBrowse() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(List.of(new AcctStep(RESP_OTHER, f -> {})));
        stubCtrlReads(List.of(new CtrlStep(RESP_NORMAL, 100)));

        OuchgfFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals("E", out.getKoStatus().trim());
        assertEquals("READNEXT ACCTFILE FAILED.", out.getKoStatusMsg().trim());
        assertEquals(0, out.getKoReadCnt());
        verify(appService, times(1)).readNext(any(), any());
        verify(appService, times(1)).endBrowse(any());
    }

    // ---------------------------------------------------------------
    // 4000-PROCESS-ACCT filter gating (WS-FILTER-ON / KO-PARM-ACCT)
    // (COBOL lines 130-133, 180-184, 226-227)
    // ---------------------------------------------------------------

    // CONVERT-GAP: same WS-CO-FACTOR decode crash (see above) blocks reaching this path.
    @Test
    void mainLine_filterActiveMatchingAccount_chargesOffThenStopsOnNextMismatch() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new AcctStep(
                                RESP_NORMAL,
                                account(
                                        555L,
                                        "Y",
                                        new BigDecimal("1500.00"),
                                        new BigDecimal("1000.00"),
                                        BigDecimal.ZERO)),
                        new AcctStep(
                                RESP_NORMAL,
                                account(
                                        556L,
                                        "Y",
                                        new BigDecimal("9000.00"),
                                        new BigDecimal("1000.00"),
                                        BigDecimal.ZERO))));
        stubCtrlReads(List.of(new CtrlStep(RESP_NORMAL, 100), new CtrlStep(RESP_NORMAL, 100)));
        stubCtrlRewrite(RESP_NORMAL);
        stubAcctReadForUpdate(RESP_NORMAL);
        stubAcctRewrite(RESP_NORMAL);
        stubTranWrite(RESP_NORMAL);

        OuchgfFields out = runMainLine(f -> f.setKoParmAcct(555));

        assertEquals(2, out.getKoReadCnt());
        assertEquals(1, out.getKoSelectCnt());
        assertEquals(1, out.getKoTranCnt());
        assertEquals(1, tranIds.size());
        verify(appService, times(2)).readNext(any(), any());
        ArgumentCaptor<String> ridfldCaptor = ArgumentCaptor.forClass(String.class);
        verify(appService).startBrowse(any(), ridfldCaptor.capture(), anyInt());
        assertEquals("555", ridfldCaptor.getValue().trim());
    }

    @Test
    void mainLine_filterActiveFirstAccountMismatch_stopsImmediately() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new AcctStep(
                                RESP_NORMAL,
                                account(
                                        111L,
                                        "Y",
                                        new BigDecimal("9000.00"),
                                        new BigDecimal("1000.00"),
                                        BigDecimal.ZERO))));
        stubCtrlReads(List.of(new CtrlStep(RESP_NORMAL, 100)));

        OuchgfFields out = runMainLine(f -> f.setKoParmAcct(999));

        assertEquals(1, out.getKoReadCnt());
        assertEquals(0, out.getKoSelectCnt());
        assertEquals(0, out.getKoTranCnt());
        verify(appService, times(1)).readNext(any(), any());
        verify(appService, times(1)).endBrowse(any());
    }

    // ---------------------------------------------------------------
    // 1500-LOAD-COUNTER / 5500-SAVE-COUNTER — TRANID sequence persistence
    // (COBOL lines 147-164, 324-355)
    // ---------------------------------------------------------------

    // CONVERT-GAP: same WS-CO-FACTOR decode crash (see above) blocks reaching this path.
    @Test
    void mainLine_ctrlMissingOnLoad_seedsCounterFromZero() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new AcctStep(
                                RESP_NORMAL,
                                account(
                                        90000000009L,
                                        "Y",
                                        new BigDecimal("1500.00"),
                                        new BigDecimal("1000.00"),
                                        BigDecimal.ZERO)),
                        new AcctStep(RESP_ENDFILE, f -> {})));
        stubCtrlReads(List.of(new CtrlStep(RESP_NOTFND, 0), new CtrlStep(RESP_NORMAL, 0)));
        stubCtrlRewrite(RESP_NORMAL);
        stubAcctReadForUpdate(RESP_NORMAL);
        stubAcctRewrite(RESP_NORMAL);
        stubTranWrite(RESP_NORMAL);

        runMainLine(f -> f.setKoParmAcct(0));

        assertEquals("CO00000000000001", tranIds.get(0).trim());
        assertEquals(1, ctrlRewriteCaptured.getCtLastValue());
    }

    // CONVERT-GAP: same WS-CO-FACTOR decode crash (see above) blocks reaching this path.
    @Test
    void mainLine_ctrlReadFailsAtSaveTime_writesNewControlRecord() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new AcctStep(
                                RESP_NORMAL,
                                account(
                                        11100000001L,
                                        "Y",
                                        new BigDecimal("1500.00"),
                                        new BigDecimal("1000.00"),
                                        BigDecimal.ZERO)),
                        new AcctStep(RESP_ENDFILE, f -> {})));
        stubCtrlReads(List.of(new CtrlStep(RESP_NORMAL, 100), new CtrlStep(RESP_OTHER, 0)));
        stubCtrlWrite(RESP_NORMAL);
        stubAcctReadForUpdate(RESP_NORMAL);
        stubAcctRewrite(RESP_NORMAL);
        stubTranWrite(RESP_NORMAL);

        runMainLine(f -> f.setKoParmAcct(0));

        verify(appService, times(1)).writeFile(eq("CTRLFILE"), any(), any(), anyInt());
        verify(appService, never()).rewriteFile(eq("CTRLFILE"), any());
        assertEquals("TRANID", ctrlWriteCaptured.getCtKey().trim());
        assertEquals(101, ctrlWriteCaptured.getCtLastValue());
        assertEquals("TRAN ID COUNTER", ctrlWriteCaptured.getCtDesc().trim());
    }

    @Test
    void mainLine_noChargeOff_skipsSaveCounterEntirely() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(List.of(new AcctStep(RESP_ENDFILE, f -> {})));
        stubCtrlReads(List.of(new CtrlStep(RESP_NORMAL, 100)));

        runMainLine(f -> f.setKoParmAcct(0));

        verify(appService, times(1)).readFileForUpdate(eq("CTRLFILE"), any(), any(), anyInt());
        verify(appService, never()).rewriteFile(eq("CTRLFILE"), any());
        verify(appService, never()).writeFile(eq("CTRLFILE"), any(), any(), anyInt());
    }

    // ---------------------------------------------------------------
    // mainLine commarea marshalling — CALL BY REFERENCE transport (Object[] wrapping
    // a byte[] element), an alternate shape alongside the plain byte[] COMMAREA used
    // by every other test in this class.
    // ---------------------------------------------------------------

    @Test
    void mainLine_commareaAsObjectArrayOfBytes_marshalsInAndOutViaSameArray() {
        stubStartBrowse(RESP_NOTFND);
        stubCtrlReads(List.of(new CtrlStep(RESP_NORMAL, 100)));
        byte[] input = buildInputCommarea(f -> f.setKoParmAcct(0));
        Object[] params = new Object[] {input};
        when(appService.getCommarea()).thenReturn(params);

        service.mainLine(appService);

        verify(appService, never()).setCommarea(any());
        OuchgfFields out = newFields();
        out.writeBytes("ORION-COMMAREA", (byte[]) params[0]);
        assertEquals("O", out.getKoStatus().trim());
        assertEquals("CHARGE-OFF PROCESSING COMPLETE.", out.getKoStatusMsg().trim());
    }

    // ---------------------------------------------------------------
    // AppProgram plumbing
    // ---------------------------------------------------------------

    @Test
    void getProgramName_returnsOuchgf() {
        assertEquals("OUCHGF", service.getProgramName());
    }

    @Test
    void getButtonDefs_delegatesToMetadata() {
        assertEquals(OuchgfBmsMetadata.getButtonDefs(), service.getButtonDefs());
    }

    @Test
    void registerFsetFields_delegatesToMetadataWithoutThrowing() {
        assertDoesNotThrow(() -> service.registerFsetFields(null));
    }
}

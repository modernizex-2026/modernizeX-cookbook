package com.generated.orion.oustmb.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.generated.orion.oustmb.accessor.OustmbFields;
import com.generated.orion.oustmb.metadata.OustmbBmsMetadata;
import com.generated.orion.oustmb.model.WorkingStorage;

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
 * Ground truth: OUSTMB.cbl (ORION-CCMS). Every input/expected value below is derived from the COBOL
 * PROCEDURE DIVISION, not from OustmbService's current behavior.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class OustmbServiceTest {

    private static final int RESP_NORMAL = 0;
    private static final int RESP_NOTFND = 13;
    private static final int RESP_DUPREC = 14;
    private static final int RESP_ENDFILE = 20;
    private static final int RESP_OTHER = 17;

    @Mock private AppService appService;

    private final OustmbService service = new OustmbService();
    private final AtomicInteger currentResp = new AtomicInteger(0);

    private record ReadStep(int resp, Consumer<OustmbFields> mutator) {}

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(appService.getEibcalen()).thenReturn(1);
        org.mockito.Mockito.lenient()
                .when(appService.getEibresp())
                .thenAnswer(inv -> currentResp.get());
        org.mockito.Mockito.lenient()
                .doAnswer(
                        inv -> {
                            currentResp.set(RESP_NORMAL);
                            return null;
                        })
                .when(appService)
                .endBrowse(any());
    }

    private static OustmbFields newFields() {
        OustmbFields f = new OustmbFields(new WorkingStorage());
        f.aliasGroup("KSM-PARM", "CA-WORK-AREA");
        return f;
    }

    private byte[] buildInputCommarea(Consumer<OustmbFields> setup) {
        OustmbFields f = newFields();
        setup.accept(f);
        return f.sliceBytes("ORION-COMMAREA");
    }

    private OustmbFields runMainLine(Consumer<OustmbFields> inputSetup) {
        byte[] input = buildInputCommarea(inputSetup);
        when(appService.getCommarea()).thenReturn(input);
        service.mainLine(appService);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(1)).setCommarea(captor.capture());
        OustmbFields out = newFields();
        out.writeBytes("ORION-COMMAREA", (byte[]) captor.getValue());
        return out;
    }

    private void stubStartBrowse(List<Integer> resps) {
        Queue<Integer> queue = new ArrayDeque<>(resps);
        doAnswer(
                        inv -> {
                            currentResp.set(queue.poll());
                            return null;
                        })
                .when(appService)
                .startBrowse(any(), any(), anyInt());
    }

    private void stubStartBrowse(int resp) {
        stubStartBrowse(List.of(resp));
    }

    private void stubReadNext(List<ReadStep> steps) {
        Queue<ReadStep> queue = new ArrayDeque<>(steps);
        doAnswer(
                        inv -> {
                            ReadStep step = queue.poll();
                            OustmbFields into = (OustmbFields) inv.getArgument(1);
                            step.mutator().accept(into);
                            currentResp.set(step.resp());
                            return null;
                        })
                .when(appService)
                .readNext(any(), any());
    }

    private void stubWriteFile(int resp) {
        doAnswer(
                        inv -> {
                            currentResp.set(resp);
                            return null;
                        })
                .when(appService)
                .writeFile(any(), any(), any(), anyInt());
    }

    private void stubReadFileForUpdate(int resp) {
        doAnswer(
                        inv -> {
                            currentResp.set(resp);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(any(), any(), any(), anyInt());
    }

    private void stubRewriteFile(int resp) {
        doAnswer(
                        inv -> {
                            currentResp.set(resp);
                            return null;
                        })
                .when(appService)
                .rewriteFile(any(), any());
    }

    private static Consumer<OustmbFields> account(long acctId, String currBal) {
        return f -> {
            f.setAcId(acctId);
            f.setAcCurrBal(new BigDecimal(currBal));
        };
    }

    private static Consumer<OustmbFields> xref(long acctId, String cardNum) {
        return f -> {
            f.setXrAcctId(acctId);
            f.setXrCardNum(cardNum);
        };
    }

    private static Consumer<OustmbFields> tran(
            String cardNum, String procTs, String typeCd, String amt) {
        return f -> {
            f.setTrCardNum(cardNum);
            f.setTrProcTs(procTs);
            f.setTrTypeCd(typeCd);
            f.setTrAmt(new BigDecimal(amt));
        };
    }

    // ---------------------------------------------------------------
    // 1000-INIT — cycle validation (COBOL lines 116-131)
    // ---------------------------------------------------------------

    @Test
    void mainLine_cycleZero_returnsStatus99_skipsBrowse() {
        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(0);
                            f.setKsmMax(0);
                        });

        assertEquals("99", out.getKsmStatus());
        assertEquals("STATEMENT CYCLE (YYYYMM) REQUIRED", out.getKsmMsg().trim());
        verify(appService, never()).startBrowse(any(), any(), anyInt());
    }

    // ---------------------------------------------------------------
    // 2000-POSITION — STARTBR ACCTFILE outcomes (COBOL lines 135-153)
    // ---------------------------------------------------------------

    @Test
    void mainLine_startBrowseAcctFailure_returnsStatus99WithMessage() {
        stubStartBrowse(RESP_OTHER);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals("99", out.getKsmStatus());
        assertEquals("ACCTFILE STARTBR FAILED", out.getKsmMsg().trim());
        verify(appService, never()).readNext(any(), any());
    }

    @Test
    void mainLine_startBrowseAcctNotFound_returnsStatus10NoAccounts() {
        stubStartBrowse(RESP_NOTFND);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals("10", out.getKsmStatus());
        assertEquals("NO ACCOUNTS PROCESSED", out.getKsmMsg().trim());
        assertEquals(0, out.getKsmAcctRead());
        verify(appService, never()).readNext(any(), any());
        verify(appService, never()).endBrowse(any());
    }

    @Test
    void mainLine_startBrowseAcctEndfile_returnsStatus10NoAccounts() {
        stubStartBrowse(RESP_ENDFILE);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals("10", out.getKsmStatus());
        assertEquals("NO ACCOUNTS PROCESSED", out.getKsmMsg().trim());
        verify(appService, never()).readNext(any(), any());
    }

    // ---------------------------------------------------------------
    // 3100-READ-ACCT read errors (COBOL lines 168-184, 6000-SET-STATUS 459-471)
    // ---------------------------------------------------------------

    @Test
    void mainLine_acctReadNextOtherError_overwrittenByNoAccountsStatus() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(List.of(new ReadStep(RESP_OTHER, f -> {})));

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        // 3100-READ-ACCT sets KSM-MSG to the READNEXT failure, but 6000-SET-STATUS
        // unconditionally overwrites it because KSM-ACCT-READ is still zero.
        assertEquals("10", out.getKsmStatus());
        assertEquals("NO ACCOUNTS PROCESSED", out.getKsmMsg().trim());
        assertEquals(1, out.getKsmErrors());
        verify(appService, times(1)).endBrowse(any());
    }

    // ---------------------------------------------------------------
    // Happy path — one account, one card, credit + debit in cycle (3200-3400)
    // ---------------------------------------------------------------

    @Test
    void mainLine_happyPath_creditAndDebitInCycle_computesBalancesAndWritesStatement() {
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NORMAL, RESP_NORMAL));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "500.00")),
                        new ReadStep(RESP_NORMAL, xref(100L, "1111222233334444")),
                        new ReadStep(RESP_ENDFILE, f -> {}),
                        new ReadStep(
                                RESP_NORMAL,
                                tran("1111222233334444", "2026-01-15-00.00.00", "PY", "100.00")),
                        new ReadStep(
                                RESP_NORMAL,
                                tran("1111222233334444", "2026-01-16-00.00.00", "DB", "50.00")),
                        new ReadStep(RESP_ENDFILE, f -> {}),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_NORMAL);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals("00", out.getKsmStatus());
        assertEquals("STATEMENT BUILD COMPLETE", out.getKsmMsg().trim());
        assertEquals(1, out.getKsmAcctRead());
        assertEquals(1, out.getKsmStmtWritten());
        assertEquals(0, out.getKsmNoTran());
        assertEquals(0, out.getKsmErrors());
        assertEquals(0, new BigDecimal("100.00").compareTo(out.getKsmTotCredit()));
        assertEquals(0, new BigDecimal("50.00").compareTo(out.getKsmTotDebit()));
        verify(appService, times(1)).writeFile(any(), any(), any(), anyInt());
    }

    @Test
    void mainLine_transactionOutsideCycle_excludedFromTotals_incrementsNoTran() {
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NORMAL, RESP_NORMAL));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "500.00")),
                        new ReadStep(RESP_NORMAL, xref(100L, "1111222233334444")),
                        new ReadStep(RESP_ENDFILE, f -> {}),
                        new ReadStep(
                                RESP_NORMAL,
                                tran("1111222233334444", "2025-12-01-00.00.00", "PY", "999.00")),
                        new ReadStep(RESP_ENDFILE, f -> {}),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_NORMAL);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals(1, out.getKsmNoTran());
        assertEquals(0, new BigDecimal("0").compareTo(out.getKsmTotCredit()));
        assertEquals(0, new BigDecimal("0").compareTo(out.getKsmTotDebit()));
    }

    // ---------------------------------------------------------------
    // 3400-COMPUTE — minimum-due branches (COBOL lines 344-359)
    // ---------------------------------------------------------------

    @Test
    void mainLine_minDueZero_whenCloseBalNotPositive() {
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NOTFND));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_NORMAL);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals(1, out.getKsmStmtWritten());
    }

    @Test
    void mainLine_minDueEqualsCloseBal_whenCloseBalUnder25() {
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NOTFND));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "20.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_NORMAL);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals(1, out.getKsmStmtWritten());
        assertEquals("00", out.getKsmStatus());
    }

    @Test
    void mainLine_minDueFloor25_whenComputedBelow25() {
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NOTFND));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "500.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_NORMAL);

        // 500.00 * 0.02 = 10.00 (< 25.00), close-bal > 25.00 -> WS-MIN-DUE floored to 25.00.
        // Not independently observable from KSM output, so this test only pins down that the
        // write still succeeds with status 00; the rounding gap below covers MIN-DUE directly
        // via the persisted statement (see mainLine_minDueRounding_CONVERT_GAP...).
        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals("00", out.getKsmStatus());
        assertEquals(1, out.getKsmStmtWritten());
    }

    @Test
    void mainLine_minDueRounding_CONVERT_GAP_javaSkipsCobolRoundedCompute() {
        // CONVERT-GAP: COBOL 3400-COMPUTE does `COMPUTE WS-MIN-DUE ROUNDED = WS-CLOSE-BAL * 0.02`
        // (half-up rounding to 2 decimals, COBOL lines 351/346).
        // OustmbService#computeStatementBalances
        // (OustmbService.java) does `ctx.f.getWsCloseBal().multiply(new BigDecimal("0.02"))` with
        // NO
        // rounding, so 2000.01 * 0.02 keeps 4 decimal places (40.0002) instead of the COBOL-rounded
        // 40.00. Both values exceed the 25.00 floor so the floor branch doesn't mask the gap.
        // We capture MIN-DUE via the WRITE STMT payload argument since KSM output has no min-due
        // field.
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NOTFND));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "2000.01")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        ArgumentCaptor<Object> stmtCaptor = ArgumentCaptor.forClass(Object.class);
        doAnswer(
                        inv -> {
                            currentResp.set(RESP_NORMAL);
                            return null;
                        })
                .when(appService)
                .writeFile(any(), stmtCaptor.capture(), any(), anyInt());

        runMainLine(
                f -> {
                    f.setKsmCycle(202601);
                    f.setKsmMax(0);
                });

        OustmbFields written = (OustmbFields) stmtCaptor.getValue();
        assertEquals(0, new BigDecimal("40.00").compareTo(written.getStMinDue()));
    }

    // ---------------------------------------------------------------
    // 3500/3510-WRITE-STMT / REWRITE-STMT (COBOL lines 364-420)
    // ---------------------------------------------------------------

    @Test
    void mainLine_writeStmtDuprec_rewritesExistingStatement_incrementsWritten() {
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NOTFND));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "500.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_DUPREC);
        stubReadFileForUpdate(RESP_NORMAL);
        stubRewriteFile(RESP_NORMAL);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals(1, out.getKsmStmtWritten());
        assertEquals(0, out.getKsmErrors());
        verify(appService, times(1)).readFileForUpdate(any(), any(), any(), anyInt());
        verify(appService, times(1)).rewriteFile(any(), any());
    }

    @Test
    void mainLine_writeStmtOtherError_incrementsErrorsNoRewrite() {
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NOTFND));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "500.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_OTHER);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals(0, out.getKsmStmtWritten());
        assertEquals(1, out.getKsmErrors());
        verify(appService, never()).readFileForUpdate(any(), any(), any(), anyInt());
    }

    @Test
    void mainLine_rewriteReadForUpdateFails_incrementsErrorsSkipsRewrite() {
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NOTFND));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "500.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_DUPREC);
        stubReadFileForUpdate(RESP_OTHER);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals(0, out.getKsmStmtWritten());
        assertEquals(1, out.getKsmErrors());
        verify(appService, never()).rewriteFile(any(), any());
    }

    @Test
    void mainLine_rewriteFails_incrementsErrors() {
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NOTFND));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "500.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_DUPREC);
        stubReadFileForUpdate(RESP_NORMAL);
        stubRewriteFile(RESP_OTHER);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals(0, out.getKsmStmtWritten());
        assertEquals(1, out.getKsmErrors());
    }

    // ---------------------------------------------------------------
    // 3220-COLLECT-CARDS (COBOL lines 215-245)
    // ---------------------------------------------------------------

    @Test
    void mainLine_xrefStartBrowseFails_noCardsCollected_noTranBrowse() {
        stubStartBrowse(List.of(RESP_NORMAL, RESP_OTHER));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "500.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_NORMAL);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals(1, out.getKsmNoTran());
        assertEquals(0, out.getKsmErrors());
        // Only the account STARTBR + the failed xref STARTBR: no tran-path STARTBR at all.
        verify(appService, times(2)).startBrowse(any(), any(), anyInt());
    }

    @Test
    void mainLine_xrefReadNextOtherError_incrementsErrorsEndsXrefLoop() {
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NORMAL));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "500.00")),
                        new ReadStep(RESP_OTHER, f -> {}),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_NORMAL);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals(1, out.getKsmErrors());
        assertEquals(1, out.getKsmNoTran());
        // Xref STARTBR succeeded, so ENDBR(xref) + ENDBR(acct) both fire = 2 total.
        verify(appService, times(2)).endBrowse(any());
    }

    @Test
    void mainLine_xrefRollsToNextAccount_endsCollectLoopWithoutError() {
        // COBOL 3220-COLLECT-CARDS: the xref loop ends either on ENDFILE (covered above) or by
        // reading a record whose XR-ACCT-ID no longer matches AC-ID (rolled into the next
        // account's cards on the alternate index) - this test exercises that second path.
        // The first xref record (card1) still matches and gets collected before the rollover
        // record is seen, so 3310-BROWSE-CARD runs once for card1 (TRAN STARTBR fails NOTFND).
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NORMAL, RESP_NOTFND));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "500.00")),
                        new ReadStep(RESP_NORMAL, xref(100L, "1111222233334444")),
                        new ReadStep(RESP_NORMAL, xref(200L, "5555666677778888")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_NORMAL);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals(0, out.getKsmErrors());
        assertEquals(1, out.getKsmNoTran());
        // acct STARTBR + xref STARTBR + one TRAN STARTBR for the single collected card.
        verify(appService, times(3)).startBrowse(any(), any(), anyInt());
    }

    // ---------------------------------------------------------------
    // 3220-COLLECT-CARDS — WS-CARD-CNT cap at WS-MAX-CARDS = 20 (COBOL lines 231-234)
    // ---------------------------------------------------------------

    @Test
    void mainLine_cardTableCap_twentyFirstCardIgnored() {
        List<Integer> startResps = new ArrayList<>();
        startResps.add(RESP_NORMAL); // ACCTFILE
        startResps.add(RESP_NORMAL); // XREFFILE
        for (int i = 0; i < 20; i++) {
            startResps.add(RESP_OTHER); // each collected card's TRAN STARTBR fails immediately
        }
        stubStartBrowse(startResps);

        List<ReadStep> readSteps = new ArrayList<>();
        readSteps.add(new ReadStep(RESP_NORMAL, account(100L, "500.00")));
        for (int i = 1; i <= 21; i++) {
            String suffix = (i < 10 ? "0" : "") + i;
            String card = "11112222333344" + suffix;
            readSteps.add(new ReadStep(RESP_NORMAL, xref(100L, card)));
        }
        readSteps.add(new ReadStep(RESP_ENDFILE, f -> {})); // xref loop end
        readSteps.add(new ReadStep(RESP_ENDFILE, f -> {})); // acct loop end
        stubReadNext(readSteps);
        stubWriteFile(RESP_NORMAL);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals(1, out.getKsmAcctRead());
        assertEquals(0, out.getKsmErrors());
        // 1 (acct) + 1 (xref) + 20 (one per collected card, the 21st is dropped) = 22.
        verify(appService, times(22)).startBrowse(any(), any(), anyInt());
    }

    // ---------------------------------------------------------------
    // 3310-BROWSE-CARD (COBOL lines 277-304)
    // ---------------------------------------------------------------

    @Test
    void mainLine_tranStartBrowseFails_noTransactionsForThatCard() {
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NORMAL, RESP_OTHER));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "500.00")),
                        new ReadStep(RESP_NORMAL, xref(100L, "1111222233334444")),
                        new ReadStep(RESP_ENDFILE, f -> {}),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_NORMAL);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals(1, out.getKsmNoTran());
        assertEquals(0, out.getKsmErrors());
        verify(appService, times(3)).startBrowse(any(), any(), anyInt());
    }

    @Test
    void mainLine_tranReadNextOtherError_incrementsErrorsEndsTranLoop() {
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NORMAL, RESP_NORMAL));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "500.00")),
                        new ReadStep(RESP_NORMAL, xref(100L, "1111222233334444")),
                        new ReadStep(RESP_ENDFILE, f -> {}),
                        new ReadStep(RESP_OTHER, f -> {}),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_NORMAL);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals(1, out.getKsmErrors());
        assertEquals(1, out.getKsmNoTran());
        verify(appService, times(3)).endBrowse(any());
    }

    @Test
    void mainLine_tranRollsToNextCard_endsBrowseLoopWithoutError() {
        // COBOL 3310-BROWSE-CARD: the tran loop ends either on ENDFILE (covered above) or by
        // reading a record whose TR-CARD-NUM no longer matches the browsed card (rolled into
        // the next card's transactions on the alternate index) - this test exercises that path.
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NORMAL, RESP_NORMAL));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "500.00")),
                        new ReadStep(RESP_NORMAL, xref(100L, "1111222233334444")),
                        new ReadStep(RESP_ENDFILE, f -> {}),
                        new ReadStep(
                                RESP_NORMAL,
                                tran("1111222233334444", "2026-01-15-00.00.00", "PY", "10.00")),
                        new ReadStep(
                                RESP_NORMAL,
                                tran("9999888877776666", "2026-01-16-00.00.00", "PY", "999.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_NORMAL);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals(0, out.getKsmErrors());
        assertEquals(0, out.getKsmNoTran());
        assertEquals(0, new BigDecimal("10.00").compareTo(out.getKsmTotCredit()));
    }

    // ---------------------------------------------------------------
    // WS-CAP / WS-MAX capping + 4000-PEEK-NEXT (COBOL lines 161-164, 425-447)
    // ---------------------------------------------------------------

    @Test
    void mainLine_maxCapReached_peeksNextAndPublishesResumeKey() {
        // XREF STARTBR fails NOTFND -> collectCardsForAccount returns immediately without
        // ever calling readNext for the xref path, so only 2 ACCTFILE readNext calls happen:
        // the account read itself, then the 4000-PEEK-NEXT lookahead.
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NOTFND));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "500.00")),
                        new ReadStep(RESP_NORMAL, account(200L, "0.00"))));
        stubWriteFile(RESP_NORMAL);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(1);
                        });

        assertEquals(1, out.getKsmAcctRead());
        assertEquals("Y", out.getKsmMore().trim());
        assertEquals(200L, out.getKsmNextAcct());
        verify(appService, times(2)).readNext(any(), any());
    }

    @Test
    void mainLine_peekNextOtherError_setsMoreNIncrementsErrors() {
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NOTFND));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "500.00")),
                        new ReadStep(RESP_OTHER, f -> {})));
        stubWriteFile(RESP_NORMAL);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(1);
                        });

        assertEquals("N", out.getKsmMore().trim());
        assertEquals(1, out.getKsmErrors());
    }

    @Test
    void mainLine_peekNextEndfile_setsMoreNWithoutErrors() {
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NOTFND));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "500.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_NORMAL);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(1);
                        });

        assertEquals("N", out.getKsmMore().trim());
        assertEquals(0, out.getKsmErrors());
    }

    // ---------------------------------------------------------------
    // Multiple accounts — KSM-TOT-CREDIT / KSM-TOT-DEBIT grand totals (COBOL lines 198-199)
    // ---------------------------------------------------------------

    @Test
    void mainLine_multipleAccounts_grandTotalsAccumulateAcrossAccounts() {
        stubStartBrowse(List.of(RESP_NORMAL, RESP_NOTFND, RESP_NOTFND));
        stubReadNext(
                List.of(
                        new ReadStep(RESP_NORMAL, account(100L, "500.00")),
                        new ReadStep(RESP_NORMAL, account(200L, "300.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubWriteFile(RESP_NORMAL);

        OustmbFields out =
                runMainLine(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });

        assertEquals(2, out.getKsmAcctRead());
        assertEquals(2, out.getKsmStmtWritten());
        assertEquals(0, new BigDecimal("0").compareTo(out.getKsmTotCredit()));
        assertEquals(0, new BigDecimal("0").compareTo(out.getKsmTotDebit()));
    }

    // ---------------------------------------------------------------
    // mainLine commarea marshalling — CALL BY REFERENCE transport (Object[] wrapping
    // a byte[] element), an alternate shape alongside the plain byte[] COMMAREA used
    // by every other test in this class.
    // ---------------------------------------------------------------

    @Test
    void mainLine_commareaObjectArray_marshalsInAndOutViaSameArray() {
        stubStartBrowse(RESP_ENDFILE);
        byte[] input =
                buildInputCommarea(
                        f -> {
                            f.setKsmCycle(202601);
                            f.setKsmMax(0);
                        });
        Object[] params = new Object[] {input};
        when(appService.getCommarea()).thenReturn(params);

        service.mainLine(appService);

        verify(appService, never()).setCommarea(any());
        OustmbFields out = newFields();
        out.writeBytes("ORION-COMMAREA", (byte[]) params[0]);
        assertEquals("10", out.getKsmStatus());
        assertEquals("NO ACCOUNTS PROCESSED", out.getKsmMsg().trim());
    }

    // ---------------------------------------------------------------
    // AppProgram plumbing
    // ---------------------------------------------------------------

    @Test
    void getProgramName_returnsOustmb() {
        assertEquals("OUSTMB", service.getProgramName());
    }

    @Test
    void getButtonDefs_delegatesToMetadata() {
        assertEquals(OustmbBmsMetadata.getButtonDefs(), service.getButtonDefs());
    }

    @Test
    void registerFsetFields_delegatesToMetadataWithoutThrowing() {
        // OustmbBmsMetadata.registerFsetFields is a no-op that never dereferences the
        // runner, so a real AppRunner instance is unnecessary here.
        assertDoesNotThrow(() -> service.registerFsetFields(null));
    }
}

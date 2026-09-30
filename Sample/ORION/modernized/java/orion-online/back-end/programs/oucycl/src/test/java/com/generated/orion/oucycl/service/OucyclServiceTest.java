package com.generated.orion.oucycl.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.generated.orion.oucycl.accessor.OucyclFields;
import com.generated.orion.oucycl.metadata.OucyclBmsMetadata;
import com.generated.orion.oucycl.model.WorkingStorage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

/**
 * Unit tests for OucyclService, generated from COBOL program OUCYCL (ORION-CCMS end-of-cycle roll:
 * browses ACCTFILE, posts net cycle activity, classifies the carried-forward balance, and rewrites
 * each account).
 *
 * <p>All business logic is private and is exercised solely through the public {@link
 * OucyclService#mainLine(AppService)} entry point, driven by an {@link AppService} mock that
 * emulates CICS STARTBR/READNEXT/READ UPDATE/ REWRITE/ENDBR. Results (KO-* fields) are only
 * observable via the COMMAREA written back through {@code appService.setCommarea(...)}, so every
 * business-logic test drives a real KOPS-AREA/CA-WORK-AREA overlay byte[] commarea in and decodes
 * the returned bytes the same way.
 *
 * <p>Convert-gap check: OUCYCL's Java service mirrors every COBOL paragraph 1:1
 * (0000-MAIN..9000-FINALISE), including the DFHRESP(NORMAL/NOTFND/ENDFILE) numeric constants
 * (0/13/20) and the EVALUATE bucket order in 4200-CLASSIFY. No behavioral divergence was found
 * against the COBOL ground truth, so no CONVERT-GAP test is included here.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OucyclServiceTest {

    @Mock private AppService appService;

    private OucyclService service;

    /** EIBRESP values to return, one per call, in the exact CICS-call order the paragraph makes. */
    private final Deque<Integer> eibrespQueue = new ArrayDeque<>();

    /** Account data to apply on each successive READNEXT call, in browse order. */
    private final Deque<Consumer<OucyclFields>> accountQueue = new ArrayDeque<>();

    @BeforeEach
    void setUp() {
        service = new OucyclService();
        lenient()
                .when(appService.getEibresp())
                .thenAnswer(inv -> eibrespQueue.isEmpty() ? 0 : eibrespQueue.poll());
        lenient().when(appService.getEibcalen()).thenReturn(100);
        lenient()
                .doAnswer(
                        inv -> {
                            OucyclFields into = inv.getArgument(1);
                            Consumer<OucyclFields> setup = accountQueue.poll();
                            if (setup != null) {
                                setup.accept(into);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
    }

    @AfterEach
    void tearDown() {
        eibrespQueue.clear();
        accountQueue.clear();
    }

    /* ---------- COMMAREA in/out helpers ---------- */

    /** Builds the byte[] commarea payload a caller would send, carrying KO-PARM-ACCT. */
    private byte[] commareaWithParmAcct(long parmAcct) {
        OucyclFields helper = new OucyclFields(new WorkingStorage());
        helper.aliasGroup("KOPS-AREA", "CA-WORK-AREA");
        helper.setKoParmAcct(parmAcct);
        return helper.sliceBytes("ORION-COMMAREA");
    }

    /**
     * Captures the byte[] passed to appService.setCommarea(...) and decodes the KO-* result fields.
     */
    private OucyclFields decodeResult() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService).setCommarea(captor.capture());
        byte[] bytes = (byte[]) captor.getValue();
        OucyclFields f = new OucyclFields(new WorkingStorage());
        f.aliasGroup("KOPS-AREA", "CA-WORK-AREA");
        f.writeBytes("ORION-COMMAREA", bytes);
        return f;
    }

    private void account(
            OucyclFields into,
            long id,
            String currBal,
            String cycCredit,
            String cycDebit,
            String creditLimit) {
        into.setAcId(id);
        into.setAcCurrBal(new BigDecimal(currBal));
        into.setAcCycCredit(new BigDecimal(cycCredit));
        into.setAcCycDebit(new BigDecimal(cycDebit));
        into.setAcCreditLimit(new BigDecimal(creditLimit));
    }

    /* ---------- 3100-START-BROWSE / 9000-FINALISE ---------- */

    @Test
    void mainLine_noAccountsToRoll_startBrowseNotFound_setsCompleteStatus() {
        when(appService.getCommarea()).thenReturn(commareaWithParmAcct(0));
        eibrespQueue.addAll(List.of(13)); // STARTBR -> DFHRESP(NOTFND)

        service.mainLine(appService);

        OucyclFields result = decodeResult();
        assertThat(result.getKoStatus()).isEqualTo("O");
        assertThat(result.getKoStatusMsg().trim()).isEqualTo("CYCLE ROLL PROCESSING COMPLETE.");
        assertThat(result.getKoReadCnt()).isZero();
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startBrowseFails_setsErrorStatusAndSkipsFinalizeOverwrite() {
        when(appService.getCommarea()).thenReturn(commareaWithParmAcct(0));
        eibrespQueue.addAll(List.of(99)); // STARTBR -> other error

        service.mainLine(appService);

        OucyclFields result = decodeResult();
        assertThat(result.getKoStatus()).isEqualTo("E");
        assertThat(result.getKoStatusMsg().trim()).isEqualTo("STARTBR ACCTFILE FAILED.");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    /* ---------- 3200-READ-NEXT-ACCT / 3000-BROWSE-DRIVER ---------- */

    @Test
    void mainLine_initialReadNextFails_setsErrorAndStillEndsBrowse() {
        when(appService.getCommarea()).thenReturn(commareaWithParmAcct(0));
        eibrespQueue.addAll(List.of(0, 99, 0)); // STARTBR ok, READNEXT other error, ENDBR

        service.mainLine(appService);

        OucyclFields result = decodeResult();
        assertThat(result.getKoStatus()).isEqualTo("E");
        assertThat(result.getKoStatusMsg().trim()).isEqualTo("READNEXT ACCTFILE FAILED.");
        assertThat(result.getKoReadCnt()).isZero();
        verify(appService, times(1)).endBrowse(anyString());
    }

    @Test
    void mainLine_readNextEndOfFileImmediately_noAccountsProcessed_completesOk() {
        when(appService.getCommarea()).thenReturn(commareaWithParmAcct(0));
        eibrespQueue.addAll(List.of(0, 20, 0)); // STARTBR ok, READNEXT DFHRESP(ENDFILE), ENDBR

        service.mainLine(appService);

        OucyclFields result = decodeResult();
        assertThat(result.getKoStatus()).isEqualTo("O");
        assertThat(result.getKoStatusMsg().trim()).isEqualTo("CYCLE ROLL PROCESSING COMPLETE.");
        assertThat(result.getKoReadCnt()).isZero();
        verify(appService, times(1)).endBrowse(anyString());
    }

    /* ---------- 4000-PROCESS-ACCT filter branch ---------- */

    @Test
    void mainLine_filterActive_firstAccountMismatch_endsBrowseWithoutProcessing() {
        long filterAcct = 555L;
        when(appService.getCommarea()).thenReturn(commareaWithParmAcct(filterAcct));
        eibrespQueue.addAll(List.of(0, 0, 0)); // STARTBR ok, READNEXT ok, ENDBR
        accountQueue.add(f -> account(f, 777L, "100.00", "0.00", "0.00", "1000.00"));

        service.mainLine(appService);

        OucyclFields result = decodeResult();
        assertThat(result.getKoReadCnt()).isEqualTo(1);
        assertThat(result.getKoSelectCnt()).isZero();
        assertThat(result.getKoPostedCnt()).isZero();
        assertThat(result.getKoStatus()).isEqualTo("O");
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());

        ArgumentCaptor<String> ridfld = ArgumentCaptor.forClass(String.class);
        verify(appService).startBrowse(anyString(), ridfld.capture(), eq(0));
        assertThat(ridfld.getValue()).isEqualTo(String.valueOf(filterAcct));
    }

    /* ---------- 4100-CAPTURE / 4200-CLASSIFY / 4300-ROLL-CYCLE ---------- */

    @Test
    void mainLine_multiAccountSweep_classifiesAndRollsEachBucket() {
        when(appService.getCommarea()).thenReturn(commareaWithParmAcct(0));
        eibrespQueue.addAll(
                List.of(
                        0, // STARTBR
                        0, // READNEXT -> A
                        0,
                        0, // A: READ-UPDATE ok, REWRITE ok
                        0, // READNEXT -> B
                        0,
                        0, // B: READ-UPDATE ok, REWRITE ok
                        0, // READNEXT -> C
                        99, // C: READ-UPDATE FAILS (reject, no rewrite)
                        0, // READNEXT -> D
                        0,
                        99, // D: READ-UPDATE ok, REWRITE FAILS (reject)
                        20, // READNEXT -> end of file
                        0 // ENDBR
                        ));
        accountQueue.add(
                f -> account(f, 101L, "50.00", "100.00", "0.00", "1000.00")); // newBal=-50   -> C3
        accountQueue.add(
                f ->
                        account(
                                f, 102L, "0.00", "0.00", "0.00",
                                "1000.00")); // newBal=0     -> C2, no select
        accountQueue.add(
                f ->
                        account(
                                f, 103L, "900.00", "0.00", "200.00",
                                "1000.00")); // newBal=1100  -> C1, reject
        accountQueue.add(
                f ->
                        account(
                                f, 104L, "300.00", "50.00", "50.00",
                                "1000.00")); // newBal=300   -> no bucket, reject

        service.mainLine(appService);

        OucyclFields result = decodeResult();
        assertThat(result.getKoReadCnt()).isEqualTo(4);
        assertThat(result.getKoSelectCnt()).isEqualTo(3);
        assertThat(result.getKoC1()).isEqualTo(1);
        assertThat(result.getKoC2()).isEqualTo(1);
        assertThat(result.getKoC3()).isEqualTo(1);
        assertThat(result.getKoPostedCnt()).isEqualTo(2);
        assertThat(result.getKoUpdateCnt()).isEqualTo(2);
        assertThat(result.getKoRejectCnt()).isEqualTo(2);
        assertThat(result.getKoAmt1()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(result.getKoAmt2()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(result.getKoAmt3()).isEqualByComparingTo(new BigDecimal("-50.00"));
        assertThat(result.getKoStatus()).isEqualTo("W"); // KO-REJECT-CNT > 0 -> warning, not error
        assertThat(result.getKoStatusMsg().trim()).isEqualTo("CYCLE ROLL PROCESSING COMPLETE.");
        // A, B and D all reach REWRITE (D's REWRITE itself fails); only C is rejected before
        // REWRITE.
        verify(appService, times(3)).rewriteFile(anyString(), any());
        verify(appService, times(4)).readFileForUpdate(anyString(), any(), anyString(), eq(0));
    }

    /* ---------- COMMAREA overlay (mainLine lines 34-63) ---------- */

    @Test
    void mainLine_eibcalenZero_skipsCommareaOverlayEntirely() {
        when(appService.getEibcalen()).thenReturn(0);
        eibrespQueue.addAll(List.of(13)); // STARTBR -> no accounts, so mainLine still completes

        service.mainLine(appService);

        verify(appService, never()).getCommarea();
        verify(appService, never()).setCommarea(any());
    }

    /* ---------- Trivial AppProgram accessors ---------- */

    @Test
    void getProgramName_returnsOucycl() {
        assertThat(service.getProgramName()).isEqualTo("OUCYCL");
    }

    @Test
    void getButtonDefs_delegatesToMetadata() {
        assertThat(service.getButtonDefs()).isEqualTo(OucyclBmsMetadata.getButtonDefs());
    }

    @Test
    void registerFsetFields_delegatesToMetadataWithoutError() {
        // OUCYCL is a SUB program with no BMS maps -> OucyclBmsMetadata.registerFsetFields(...)
        // is a true no-op, so it never dereferences its argument; null is enough to prove that.
        assertThatCode(() -> service.registerFsetFields(null)).doesNotThrowAnyException();
    }
}

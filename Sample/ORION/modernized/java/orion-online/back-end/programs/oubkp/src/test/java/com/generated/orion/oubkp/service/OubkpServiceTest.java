package com.generated.orion.oubkp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.generated.orion.oubkp.accessor.OubkpFields;
import com.generated.orion.oubkp.model.WorkingStorage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OubkpService, generated from COBOL program OUBKP (ORION-CCMS on-line transaction
 * backup/extract sub, LINKed with the ORION-COMMAREA commarea overlaying KBK-PARM at CA-WORK-AREA).
 * All business logic lives in private paragraph-methods and is exercised solely through the public
 * {@link OubkpService#mainLine(AppService)} entry point, driven by an {@link AppService} mock
 * emulating CICS STARTBR/READNEXT/WRITE/ENDBR and the KBK-PARM commarea round-trip.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(
        value = 10,
        unit = java.util.concurrent.TimeUnit.SECONDS,
        threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class OubkpServiceTest {

    @Mock private AppService appService;

    private final OubkpService service = new OubkpService();

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    /** Transaction queue for the TRANFILE browse. */
    private final List<Txn> txns = new ArrayList<>();

    private final AtomicInteger txnPos = new AtomicInteger(0);

    /** Per-call response codes for WRITE on BKPFILE (index by call order; default NORMAL). */
    private final List<Integer> writeResps = new ArrayList<>();

    private final AtomicInteger writeCallIdx = new AtomicInteger(0);

    private static final class Txn {
        final String id;
        final String typeCd;
        final int catCd;
        final String cardNum;
        final BigDecimal amt;
        final int merchantId;
        final String origTs;
        final String desc;

        Txn(
                String id,
                String typeCd,
                int catCd,
                String cardNum,
                BigDecimal amt,
                int merchantId,
                String origTs,
                String desc) {
            this.id = id;
            this.typeCd = typeCd;
            this.catCd = catCd;
            this.cardNum = cardNum;
            this.amt = amt;
            this.merchantId = merchantId;
            this.origTs = origTs;
            this.desc = desc;
        }
    }

    private Txn txn(String id, String typeCd, String amt) {
        return new Txn(
                id,
                typeCd,
                1001,
                "1111222233334444",
                new BigDecimal(amt),
                900000001,
                "2026-01-01-00.00.00.000000",
                "TEST DESC");
    }

    @BeforeEach
    void setUp() {
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .doAnswer(
                        inv -> {
                            int idx = writeCallIdx.getAndIncrement();
                            nextResp.set(idx < writeResps.size() ? writeResps.get(idx) : 0);
                            return null;
                        })
                .when(appService)
                .writeFile(eq("BKPFILE "), any(), anyString(), anyInt());
    }

    @AfterEach
    void tearDown() {
        txns.clear();
        txnPos.set(0);
        writeResps.clear();
        writeCallIdx.set(0);
        nextResp.set(0);
    }

    // ───────────────────────── request / response helpers ─────────────────────────

    /**
     * Builds the KBK-PARM request, aliased onto CA-WORK-AREA exactly as SET ADDRESS OF does at
     * runtime.
     */
    private String buildRequest(String startKey, int max) {
        OubkpFields req = new OubkpFields(new WorkingStorage());
        req.aliasGroup("KBK-PARM", "CA-WORK-AREA");
        if (startKey != null) {
            req.setKbkStartKey(startKey);
        }
        req.setKbkMax(max);
        return req.getOrionCommarea();
    }

    private void givenRequest(String startKey, int max) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getCommarea()).thenReturn(buildRequest(startKey, max));
    }

    private OubkpFields runAndCaptureResponse() {
        service.mainLine(appService);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService).setCommarea(captor.capture());
        OubkpFields out = new OubkpFields(new WorkingStorage());
        out.writeBytes("ORION-COMMAREA", (byte[]) captor.getValue());
        out.aliasGroup("KBK-PARM", "CA-WORK-AREA");
        return out;
    }

    // ───────────────────────── TRANFILE browse stubs ─────────────────────────

    private void givenStartBrowseResp(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            txnPos.set(0);
                            return null;
                        })
                .when(appService)
                .startBrowse(eq("TRANFILE"), anyString(), anyInt());
    }

    /** Default queue: NORMAL for each queued txn, then ENDFILE(20) once exhausted. */
    private void givenTransactions(List<Txn> list) {
        txns.clear();
        txns.addAll(list);
        doAnswer(
                        inv -> {
                            int i = txnPos.get();
                            if (i >= txns.size()) {
                                nextResp.set(20);
                                return null;
                            }
                            Txn t = txns.get(i);
                            txnPos.incrementAndGet();
                            OubkpFields f = inv.getArgument(1);
                            f.setTrId(t.id);
                            f.setTrTypeCd(t.typeCd);
                            f.setTrCatCd(t.catCd);
                            f.setTrCardNum(t.cardNum);
                            f.setTrAmt(t.amt);
                            f.setTrMerchantId(t.merchantId);
                            f.setTrOrigTs(t.origTs);
                            f.setTrDesc(t.desc);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readNext(eq("TRANFILE"), any());
    }

    // ───────────────────────── 2000-POSITION ─────────────────────────

    @Test
    void mainLine_startBrowseNotFound_status10AndSkipsExtractionEntirely() {
        givenRequest(null, 0);
        givenStartBrowseResp(13);

        OubkpFields out = runAndCaptureResponse();

        assertThat(out.getKbkStatus()).isEqualTo("10");
        assertThat(out.getKbkMsg().trim()).isEqualTo("NO TRANSACTIONS EXTRACTED");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startBrowseEndfile_status10AndSkipsExtractionEntirely() {
        givenRequest(null, 0);
        givenStartBrowseResp(20);

        OubkpFields out = runAndCaptureResponse();

        assertThat(out.getKbkStatus()).isEqualTo("10");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startBrowseOtherResp_status99AndSkipsExtractionEntirely() {
        // CONVERT-GAP check: COBOL 2000-POSITION WHEN OTHER -> KBK-STATUS='99', and
        // 6000-SET-STATUS leaves '99' untouched (GO TO 6000-EXIT). Java mirrors both.
        givenRequest(null, 0);
        givenStartBrowseResp(80);

        OubkpFields out = runAndCaptureResponse();

        assertThat(out.getKbkStatus()).isEqualTo("99");
        assertThat(out.getKbkMsg().trim()).isEqualTo("TRANFILE STARTBR FAILED");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startKeyProvided_passedAsRidfldToStartBrowse() {
        givenRequest("0000000000000001", 0);
        givenStartBrowseResp(0);
        givenTransactions(List.of());

        runAndCaptureResponse();

        verify(appService).startBrowse(eq("TRANFILE"), eq("0000000000000001"), eq(0));
    }

    // ───────────────────────── 3000..3500 extraction loop ─────────────────────────

    @Test
    void mainLine_happyPath_singleTransaction_writesRecordAndAccumulatesCredit() {
        givenRequest(null, 0);
        givenStartBrowseResp(0);
        givenTransactions(List.of(txn("0000000000000001", "PU", "100.00")));

        OubkpFields out = runAndCaptureResponse();

        assertThat(out.getKbkStatus()).isEqualTo("00");
        assertThat(out.getKbkMsg().trim()).isEqualTo("TRANSACTION BACKUP COMPLETE");
        assertThat(out.getKbkRead()).isEqualTo(1);
        assertThat(out.getKbkWritten()).isEqualTo(1);
        assertThat(out.getKbkErrors()).isZero();
        assertThat(out.getKbkTotAmt()).isEqualByComparingTo("100.00");
        assertThat(out.getKbkCreditAmt()).isEqualByComparingTo("100.00");
        assertThat(out.getKbkDebitAmt()).isEqualByComparingTo("0.00");
        assertThat(out.getKbkMore().trim()).isEqualTo("N");
        verify(appService).endBrowse(eq("TRANFILE"));

        ArgumentCaptor<Object> fromCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .writeFile(eq("BKPFILE "), fromCaptor.capture(), eq("0000000000000001"), eq(0));
        OubkpFields written = (OubkpFields) fromCaptor.getValue();
        assertThat(written.getBkTranId().trim()).isEqualTo("0000000000000001");
        assertThat(written.getBkType().trim()).isEqualTo("PU");
        assertThat(written.getBkCat()).isEqualTo(1001);
        assertThat(written.getBkCardNum().trim()).isEqualTo("1111222233334444");
        assertThat(written.getBkAmt()).isEqualByComparingTo("100.00");
        assertThat(written.getBkMerchId()).isEqualTo(900000001);
        assertThat(written.getBkOrigTs().trim()).isEqualTo("2026-01-01-00.00.00.000000");
        assertThat(written.getBkDesc().trim()).isEqualTo("TEST DESC");
        assertThat(written.getBkBar1()).isEqualTo("|");
    }

    @Test
    void mainLine_negativeAmount_accumulatesToDebitNotCredit() {
        givenRequest(null, 0);
        givenStartBrowseResp(0);
        givenTransactions(List.of(txn("0000000000000001", "RF", "-25.00")));

        OubkpFields out = runAndCaptureResponse();

        assertThat(out.getKbkTotAmt()).isEqualByComparingTo("-25.00");
        assertThat(out.getKbkDebitAmt()).isEqualByComparingTo("-25.00");
        assertThat(out.getKbkCreditAmt()).isEqualByComparingTo("0.00");
    }

    @Test
    void mainLine_multipleTransactions_accumulatesRunningTotals() {
        givenRequest(null, 0);
        givenStartBrowseResp(0);
        givenTransactions(
                List.of(
                        txn("0000000000000001", "PU", "100.00"),
                        txn("0000000000000002", "RF", "-40.00"),
                        txn("0000000000000003", "PU", "10.00")));

        OubkpFields out = runAndCaptureResponse();

        assertThat(out.getKbkRead()).isEqualTo(3);
        assertThat(out.getKbkWritten()).isEqualTo(3);
        assertThat(out.getKbkTotAmt()).isEqualByComparingTo("70.00");
        assertThat(out.getKbkCreditAmt()).isEqualByComparingTo("110.00");
        assertThat(out.getKbkDebitAmt()).isEqualByComparingTo("-40.00");
    }

    @Test
    void mainLine_readNextOtherRespMidScan_setsEofIncrementsErrorsButStatusStaysComplete() {
        givenRequest(null, 0);
        givenStartBrowseResp(0);
        List<Txn> two =
                List.of(
                        txn("0000000000000001", "PU", "10.00"),
                        txn("0000000000000002", "PU", "20.00"));
        txns.addAll(two);
        doAnswer(
                        inv -> {
                            int i = txnPos.get();
                            if (i >= txns.size()) {
                                // Third call: simulate a READNEXT transport error (not natural
                                // end-of-file).
                                nextResp.set(55);
                                return null;
                            }
                            Txn t = txns.get(i);
                            txnPos.incrementAndGet();
                            OubkpFields f = inv.getArgument(1);
                            f.setTrId(t.id);
                            f.setTrTypeCd(t.typeCd);
                            f.setTrAmt(t.amt);
                            nextResp.set(0);
                            return null;
                        })
                .when(appService)
                .readNext(eq("TRANFILE"), any());

        OubkpFields out = runAndCaptureResponse();

        assertThat(out.getKbkRead()).isEqualTo(2);
        assertThat(out.getKbkWritten()).isEqualTo(2);
        assertThat(out.getKbkErrors()).isEqualTo(1);
        // 6000-SET-STATUS overwrites KBK-MSG to COMPLETE since KBK-READ > 0 and status != '99',
        // even though 3100-READ-TRAN set "TRANFILE READNEXT FAILED" moments earlier.
        assertThat(out.getKbkStatus()).isEqualTo("00");
        assertThat(out.getKbkMsg().trim()).isEqualTo("TRANSACTION BACKUP COMPLETE");
        assertThat(out.getKbkMore().trim()).isEqualTo("N");
        verify(appService).endBrowse(eq("TRANFILE"));
    }

    @Test
    void mainLine_writeError_incrementsErrorsNotWrittenButStillAccumulatesAmount() {
        givenRequest(null, 0);
        givenStartBrowseResp(0);
        givenTransactions(List.of(txn("0000000000000001", "PU", "50.00")));
        writeResps.add(1); // simulate DUPREC/other WRITE failure on the only record

        OubkpFields out = runAndCaptureResponse();

        assertThat(out.getKbkRead()).isEqualTo(1);
        assertThat(out.getKbkWritten()).isZero();
        assertThat(out.getKbkErrors()).isEqualTo(1);
        assertThat(out.getKbkTotAmt()).isEqualByComparingTo("50.00");
        assertThat(out.getKbkCreditAmt()).isEqualByComparingTo("50.00");
    }

    // ───────────────────────── WS-MAX cap / 4000-PEEK-NEXT ─────────────────────────

    @Test
    void mainLine_maxCapReached_stopsEarlyThenPeeksNextSettingMoreYAndNextKey() {
        givenRequest(null, 2);
        givenStartBrowseResp(0);
        givenTransactions(
                List.of(
                        txn("0000000000000001", "PU", "10.00"),
                        txn("0000000000000002", "PU", "10.00"),
                        txn("0000000000000003", "PU", "10.00")));

        OubkpFields out = runAndCaptureResponse();

        assertThat(out.getKbkRead()).isEqualTo(2);
        assertThat(out.getKbkWritten()).isEqualTo(2);
        assertThat(out.getKbkMore().trim()).isEqualTo("Y");
        assertThat(out.getKbkNextKey().trim()).isEqualTo("0000000000000003");
        // Peek is a 3rd READNEXT beyond the 2 the cap allowed into the extraction loop.
        verify(appService, times(3)).readNext(eq("TRANFILE"), any());
        verify(appService).endBrowse(eq("TRANFILE"));
    }

    @Test
    void mainLine_maxCapReachedExactlyAtEof_peekSeesEndfileSetsMoreN() {
        givenRequest(null, 1);
        givenStartBrowseResp(0);
        givenTransactions(List.of(txn("0000000000000001", "PU", "10.00")));

        OubkpFields out = runAndCaptureResponse();

        assertThat(out.getKbkRead()).isEqualTo(1);
        assertThat(out.getKbkMore().trim()).isEqualTo("N");
        assertThat(out.getKbkNextKey().trim()).isEmpty();
    }

    // ───────────────────────── 6000-SET-STATUS ─────────────────────────

    @Test
    void mainLine_zeroTransactionsRead_status10EvenThoughBrowseWasOn() {
        givenRequest(null, 0);
        givenStartBrowseResp(0);
        givenTransactions(List.of());

        OubkpFields out = runAndCaptureResponse();

        assertThat(out.getKbkStatus()).isEqualTo("10");
        assertThat(out.getKbkMsg().trim()).isEqualTo("NO TRANSACTIONS EXTRACTED");
        assertThat(out.getKbkRead()).isZero();
        verify(appService).endBrowse(eq("TRANFILE"));
    }

    // ───────────────────────── trivial delegate methods ─────────────────────────

    @Test
    void getProgramName_returnsOubkp() {
        assertThat(service.getProgramName()).isEqualTo("OUBKP");
    }

    @Test
    void getButtonDefs_delegatesToMetadataWithoutError() {
        assertThat(service.getButtonDefs()).isNotNull();
    }

    @Test
    void registerFsetFields_delegatesToMetadata_doesNotThrow() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.registerFsetFields(null));
    }
}

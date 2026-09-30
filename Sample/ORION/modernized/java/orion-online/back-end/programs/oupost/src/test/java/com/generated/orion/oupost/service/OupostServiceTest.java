package com.generated.orion.oupost.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.generated.orion.oupost.accessor.OupostFields;
import com.generated.orion.oupost.metadata.OupostBmsMetadata;
import com.generated.orion.oupost.model.WorkingStorage;

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
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OupostService, generated from COBOL program OUPOST (ORION-CCMS daily transaction
 * posting SUB). All business logic lives in private paragraph methods reachable only through {@link
 * OupostService#mainLine(AppService)}, so every test drives the service through that public entry
 * point with an {@link AppService} mock emulating CICS STARTBR/READNEXT/READ/REWRITE/ENDBR, and
 * inspects the result via the KOPS-AREA overlay round-tripped through the ORION-COMMAREA (mirrors
 * the real {@code SET ADDRESS OF KOPS-AREA TO ADDRESS OF CA-WORK-AREA} in {@code 0000-MAIN}).
 *
 * <p>No CONVERT-GAP found: this program's Java mirrors OUPOST.cbl's paragraph structure and
 * EVALUATE/IF branches faithfully (STARTBR/READNEXT resp handling, card/xref/account rejection
 * counters, credit-vs-debit classification, credit limit guard, REWRITE resp handling, and the
 * FINALISE status/message logic — including the COBOL quirk where 9000-FINALISE unconditionally
 * overwrites KO-STATUS-MSG with "TRANSACTION POSTING COMPLETE." even when STARTBR found zero
 * transactions).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OupostServiceTest {

    @Mock private AppService appService;

    private final OupostService service = new OupostService();

    private final AtomicInteger eibresp = new AtomicInteger(0);

    private static final class Tran {
        final String cardNum;
        final boolean lowValues;
        final String typeCd;
        final BigDecimal amt;

        Tran(String cardNum, boolean lowValues, String typeCd, String amt) {
            this.cardNum = cardNum;
            this.lowValues = lowValues;
            this.typeCd = typeCd;
            this.amt = amt == null ? null : new BigDecimal(amt);
        }
    }

    private static final class Acct {
        final BigDecimal currBal;
        final BigDecimal creditLimit;
        final BigDecimal cycCredit;
        final BigDecimal cycDebit;

        Acct(String currBal, String creditLimit, String cycCredit, String cycDebit) {
            this.currBal = new BigDecimal(currBal);
            this.creditLimit = new BigDecimal(creditLimit);
            this.cycCredit = new BigDecimal(cycCredit);
            this.cycDebit = new BigDecimal(cycDebit);
        }
    }

    private final Deque<Tran> tranQueue = new ArrayDeque<>();
    private final Map<String, Long> xrefByCard = new HashMap<>();
    private final Map<String, Integer> xrefErrorByCard = new HashMap<>();
    private final Map<Long, Acct> acctById = new HashMap<>();
    private final Map<Long, Integer> acctErrorById = new HashMap<>();
    private int rewriteResp = 0;
    private int startBrowseResp = 0;
    private BigDecimal lastRewrittenCurrBal;
    private BigDecimal lastRewrittenCycCredit;
    private BigDecimal lastRewrittenCycDebit;

    private OupostFields newFields() {
        OupostFields f = new OupostFields(new WorkingStorage());
        f.aliasGroup("KOPS-AREA", "CA-WORK-AREA");
        return f;
    }

    @BeforeEach
    void setUp() {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getEibresp()).thenAnswer(inv -> eibresp.get());

        doAnswer(
                        inv -> {
                            eibresp.set(startBrowseResp);
                            return null;
                        })
                .when(appService)
                .startBrowse(anyString(), anyString(), anyInt());

        doAnswer(
                        inv -> {
                            if (tranQueue.isEmpty()) {
                                eibresp.set(20);
                                return null;
                            }
                            Tran t = tranQueue.poll();
                            OupostFields f = inv.getArgument(1);
                            if (t.lowValues) {
                                f.fillLowValues("TR-CARD-NUM");
                            } else {
                                f.setTrCardNum(t.cardNum);
                            }
                            if (t.typeCd != null) {
                                f.setTrTypeCd(t.typeCd);
                            }
                            if (t.amt != null) {
                                f.setTrAmt(t.amt);
                            }
                            eibresp.set(0);
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());

        doAnswer(
                        inv -> {
                            OupostFields f = inv.getArgument(1);
                            String cardNum = f.getXrCardNum().trim();
                            if (xrefErrorByCard.containsKey(cardNum)) {
                                eibresp.set(xrefErrorByCard.get(cardNum));
                                return null;
                            }
                            Long acctId = xrefByCard.get(cardNum);
                            if (acctId == null) {
                                eibresp.set(13);
                                return null;
                            }
                            f.setXrAcctId(acctId);
                            eibresp.set(0);
                            return null;
                        })
                .when(appService)
                .readFile(anyString(), any(), anyString(), anyInt());

        doAnswer(
                        inv -> {
                            OupostFields f = inv.getArgument(1);
                            long acctId = f.getAcId();
                            if (acctErrorById.containsKey(acctId)) {
                                eibresp.set(acctErrorById.get(acctId));
                                return null;
                            }
                            Acct a = acctById.get(acctId);
                            if (a == null) {
                                eibresp.set(13);
                                return null;
                            }
                            f.setAcCurrBal(a.currBal);
                            f.setAcCreditLimit(a.creditLimit);
                            f.setAcCycCredit(a.cycCredit);
                            f.setAcCycDebit(a.cycDebit);
                            eibresp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());

        doAnswer(
                        inv -> {
                            OupostFields f = inv.getArgument(1);
                            lastRewrittenCurrBal = f.getAcCurrBal();
                            lastRewrittenCycCredit = f.getAcCycCredit();
                            lastRewrittenCycDebit = f.getAcCycDebit();
                            eibresp.set(rewriteResp);
                            return null;
                        })
                .when(appService)
                .rewriteFile(anyString(), any());
    }

    private void givenTran(String cardNum, String typeCd, String amt) {
        tranQueue.add(new Tran(cardNum, false, typeCd, amt));
    }

    private void givenTranWithBlankCard() {
        tranQueue.add(new Tran(" ", false, null, "0.00"));
    }

    private void givenTranWithLowValueCard() {
        tranQueue.add(new Tran(null, true, null, "0.00"));
    }

    private void givenXref(String cardNum, long acctId) {
        xrefByCard.put(cardNum, acctId);
    }

    private void givenXrefError(String cardNum, int resp) {
        xrefErrorByCard.put(cardNum, resp);
    }

    private void givenAccount(
            long acctId, String currBal, String creditLimit, String cycCredit, String cycDebit) {
        acctById.put(acctId, new Acct(currBal, creditLimit, cycCredit, cycDebit));
    }

    private void givenAccountError(long acctId, int resp) {
        acctErrorById.put(acctId, resp);
    }

    private void givenCommarea(long filterAcct) {
        OupostFields req = newFields();
        if (filterAcct > 0) {
            req.setKoParmAcct(filterAcct);
        }
        when(appService.getCommarea()).thenReturn(req.getOrionCommarea());
    }

    private OupostFields runAndCaptureResult() {
        service.mainLine(appService);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService).setCommarea(captor.capture());
        OupostFields out = newFields();
        out.writeBytes("ORION-COMMAREA", (byte[]) captor.getValue());
        return out;
    }

    // ---------------------------------------------------------------
    // Happy path: classification (credit vs debit) and posting.
    // ---------------------------------------------------------------

    @Test
    void mainLine_creditTypeCr_postsAndReducesBalanceAndUpdatesCycCredit() {
        givenCommarea(0);
        givenTran("1111222233334444", "CR", "100.00");
        givenXref("1111222233334444", 555L);
        givenAccount(555L, "500.00", "1000.00", "0.00", "0.00");

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoReadCnt()).isEqualTo(1);
        assertThat(out.getKoSelectCnt()).isEqualTo(1);
        assertThat(out.getKoPostedCnt()).isEqualTo(1);
        assertThat(out.getKoUpdateCnt()).isEqualTo(1);
        assertThat(out.getKoRejectCnt()).isEqualTo(0);
        assertThat(out.getKoAmt2()).isEqualByComparingTo("100.00");
        assertThat(out.getKoAmt1()).isEqualByComparingTo("0.00");
        assertThat(out.getKoAmt3()).isEqualByComparingTo("-100.00");
        assertThat(out.getKoStatus()).isEqualTo("O");
        assertThat(out.getKoStatusMsg().trim()).isEqualTo("TRANSACTION POSTING COMPLETE.");
        assertThat(lastRewrittenCurrBal).isEqualByComparingTo("400.00");
        assertThat(lastRewrittenCycCredit).isEqualByComparingTo("100.00");
        verify(appService, times(1)).endBrowse(anyString());
    }

    @Test
    void mainLine_creditTypePy_isAlsoClassifiedAsCredit() {
        givenCommarea(0);
        givenTran("1111222233334444", "PY", "50.00");
        givenXref("1111222233334444", 555L);
        givenAccount(555L, "500.00", "1000.00", "0.00", "0.00");

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoPostedCnt()).isEqualTo(1);
        assertThat(out.getKoAmt2()).isEqualByComparingTo("50.00");
        assertThat(lastRewrittenCurrBal).isEqualByComparingTo("450.00");
    }

    @Test
    void mainLine_debitTypeWithinCreditLimit_postsAndIncreasesBalanceAndUpdatesCycDebit() {
        givenCommarea(0);
        givenTran("1111222233334444", "PU", "100.00");
        givenXref("1111222233334444", 555L);
        givenAccount(555L, "500.00", "1000.00", "0.00", "0.00");

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoPostedCnt()).isEqualTo(1);
        assertThat(out.getKoUpdateCnt()).isEqualTo(1);
        assertThat(out.getKoAmt1()).isEqualByComparingTo("100.00");
        assertThat(out.getKoAmt2()).isEqualByComparingTo("0.00");
        assertThat(out.getKoAmt3()).isEqualByComparingTo("100.00");
        assertThat(lastRewrittenCurrBal).isEqualByComparingTo("600.00");
        assertThat(lastRewrittenCycDebit).isEqualByComparingTo("100.00");
    }

    @Test
    void mainLine_debitProjectedBalanceExactlyAtCreditLimit_stillPosts() {
        givenCommarea(0);
        givenTran("1111222233334444", "PU", "100.00");
        givenXref("1111222233334444", 555L);
        givenAccount(555L, "900.00", "1000.00", "0.00", "0.00");

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoPostedCnt()).isEqualTo(1);
        assertThat(out.getKoRejectCnt()).isEqualTo(0);
        assertThat(out.getKoC3()).isEqualTo(0);
    }

    @Test
    void mainLine_debitProjectedBalanceExceedsCreditLimit_rejectsWithC3AndDoesNotPost() {
        givenCommarea(0);
        givenTran("1111222233334444", "PU", "100.00");
        givenXref("1111222233334444", 555L);
        givenAccount(555L, "950.00", "1000.00", "0.00", "0.00");

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoRejectCnt()).isEqualTo(1);
        assertThat(out.getKoC3()).isEqualTo(1);
        assertThat(out.getKoPostedCnt()).isEqualTo(0);
        assertThat(out.getKoUpdateCnt()).isEqualTo(0);
        verify(appService, never()).rewriteFile(anyString(), any());
        assertThat(out.getKoStatus()).isEqualTo("W");
    }

    // ---------------------------------------------------------------
    // 4000-PROCESS-TRAN: blank/low-value card number.
    // ---------------------------------------------------------------

    @Test
    void mainLine_blankCardNumber_rejectsWithC1AndSkipsXrefLookup() {
        givenCommarea(0);
        givenTranWithBlankCard();

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoRejectCnt()).isEqualTo(1);
        assertThat(out.getKoC1()).isEqualTo(1);
        assertThat(out.getKoSelectCnt()).isEqualTo(0);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_lowValueCardNumber_rejectsWithC1AndSkipsXrefLookup() {
        givenCommarea(0);
        givenTranWithLowValueCard();

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoRejectCnt()).isEqualTo(1);
        assertThat(out.getKoC1()).isEqualTo(1);
        verify(appService, never()).readFile(anyString(), any(), anyString(), anyInt());
    }

    // ---------------------------------------------------------------
    // 4100-READ-XREF RESP handling.
    // ---------------------------------------------------------------

    @Test
    void mainLine_xrefNotFound_rejectsWithC1() {
        givenCommarea(0);
        givenTran("9999999999999999", "CR", "10.00");
        // No xrefByCard entry -> NOTFND (resp 13).

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoRejectCnt()).isEqualTo(1);
        assertThat(out.getKoC1()).isEqualTo(1);
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_xrefOtherError_rejectsWithoutC1() {
        givenCommarea(0);
        givenTran("1111222233334444", "CR", "10.00");
        givenXrefError("1111222233334444", 99);

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoRejectCnt()).isEqualTo(1);
        assertThat(out.getKoC1()).isEqualTo(0);
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    // ---------------------------------------------------------------
    // 4150-CHECK-FILTER: optional single-account filter (KO-PARM-ACCT).
    // ---------------------------------------------------------------

    @Test
    void mainLine_filterActiveAndAccountMismatch_skipsSelectionSilently() {
        givenCommarea(555L);
        givenTran("1111222233334444", "CR", "10.00");
        givenXref("1111222233334444", 777L);

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoSelectCnt()).isEqualTo(0);
        assertThat(out.getKoRejectCnt()).isEqualTo(0);
        assertThat(out.getKoStatus()).isEqualTo("O");
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_filterActiveAndAccountMatches_processesNormally() {
        givenCommarea(555L);
        givenTran("1111222233334444", "CR", "10.00");
        givenXref("1111222233334444", 555L);
        givenAccount(555L, "500.00", "1000.00", "0.00", "0.00");

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoSelectCnt()).isEqualTo(1);
        assertThat(out.getKoPostedCnt()).isEqualTo(1);
    }

    // ---------------------------------------------------------------
    // 4200-READ-ACCT-UPD RESP handling.
    // ---------------------------------------------------------------

    @Test
    void mainLine_accountNotFound_rejectsWithC2() {
        givenCommarea(0);
        givenTran("1111222233334444", "CR", "10.00");
        givenXref("1111222233334444", 555L);
        // No account entry for 555 -> NOTFND (resp 13).

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoRejectCnt()).isEqualTo(1);
        assertThat(out.getKoC2()).isEqualTo(1);
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void mainLine_accountReadOtherError_rejectsWithoutC2() {
        givenCommarea(0);
        givenTran("1111222233334444", "CR", "10.00");
        givenXref("1111222233334444", 555L);
        givenAccountError(555L, 99);

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoRejectCnt()).isEqualTo(1);
        assertThat(out.getKoC2()).isEqualTo(0);
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    // ---------------------------------------------------------------
    // 4400-APPLY-UPDATE: REWRITE failure.
    // ---------------------------------------------------------------

    @Test
    void mainLine_rewriteFails_rejectsWithoutIncrementingPostedOrUpdateCounts() {
        givenCommarea(0);
        givenTran("1111222233334444", "CR", "10.00");
        givenXref("1111222233334444", 555L);
        givenAccount(555L, "500.00", "1000.00", "0.00", "0.00");
        rewriteResp = 99;

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoRejectCnt()).isEqualTo(1);
        assertThat(out.getKoPostedCnt()).isEqualTo(0);
        assertThat(out.getKoUpdateCnt()).isEqualTo(0);
        assertThat(out.getKoAmt2()).isEqualByComparingTo("0.00");
    }

    // ---------------------------------------------------------------
    // 3100-START-BROWSE RESP handling + 9000-FINALISE status/message.
    // ---------------------------------------------------------------

    @Test
    void mainLine_startBrowseNotFound_finaliseOverwritesMessageAndSetsOkStatus() {
        givenCommarea(0);
        startBrowseResp = 13;

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoReadCnt()).isEqualTo(0);
        assertThat(out.getKoStatus()).isEqualTo("O");
        // CONVERT-GAP check (none found): COBOL 9000-FINALISE unconditionally overwrites
        // KO-STATUS-MSG with 'TRANSACTION POSTING COMPLETE.' whenever KO-STATUS is not
        // KO-ERROR, even though 3100-START-BROWSE had set 'NO TRANSACTIONS ON FILE TO POST.'
        // The Java faithfully reproduces this quirk.
        assertThat(out.getKoStatusMsg().trim()).isEqualTo("TRANSACTION POSTING COMPLETE.");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startBrowseOtherError_setsErrorStatusAndPreservesMessage() {
        givenCommarea(0);
        startBrowseResp = 99;

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoStatus()).isEqualTo("E");
        assertThat(out.getKoStatusMsg().trim()).isEqualTo("STARTBR TRANFILE FAILED.");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_readNextOtherErrorMidBrowse_setsErrorStatusAndPreservesMessage() {
        givenCommarea(0);
        givenTran("1111222233334444", "CR", "10.00");
        givenXref("1111222233334444", 555L);
        givenAccount(555L, "500.00", "1000.00", "0.00", "0.00");
        // After the one queued transaction is consumed, force a non-endfile READNEXT error
        // instead of the default ENDFILE (resp 20) by overriding the stub.
        doAnswer(
                        inv -> {
                            if (!tranQueue.isEmpty()) {
                                Tran t = tranQueue.poll();
                                OupostFields f = inv.getArgument(1);
                                f.setTrCardNum(t.cardNum);
                                f.setTrTypeCd(t.typeCd);
                                f.setTrAmt(t.amt);
                                eibresp.set(0);
                            } else {
                                eibresp.set(77);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoReadCnt()).isEqualTo(1);
        assertThat(out.getKoStatus()).isEqualTo("E");
        assertThat(out.getKoStatusMsg().trim()).isEqualTo("READNEXT TRANFILE FAILED.");
        verify(appService, times(1)).endBrowse(anyString());
    }

    // ---------------------------------------------------------------
    // Multiple transactions -> WARN status when any reject occurred.
    // ---------------------------------------------------------------

    @Test
    void mainLine_multipleTransactionsOnePostedOneRejected_setsWarnStatus() {
        givenCommarea(0);
        givenTran("1111222233334444", "CR", "10.00");
        givenXref("1111222233334444", 555L);
        givenAccount(555L, "500.00", "1000.00", "0.00", "0.00");
        givenTranWithBlankCard();

        OupostFields out = runAndCaptureResult();

        assertThat(out.getKoReadCnt()).isEqualTo(2);
        assertThat(out.getKoPostedCnt()).isEqualTo(1);
        assertThat(out.getKoRejectCnt()).isEqualTo(1);
        assertThat(out.getKoStatus()).isEqualTo("W");
    }

    // ---------------------------------------------------------------
    // Metadata / trivial public API (smoke coverage).
    // ---------------------------------------------------------------

    @Test
    void getProgramName_returnsOupost() {
        assertThat(service.getProgramName()).isEqualTo("OUPOST");
    }

    @Test
    void getButtonDefs_delegatesToMetadataAndReturnsEmpty() {
        assertThat(service.getButtonDefs()).isEmpty();
        assertThat(OupostBmsMetadata.getButtonDefs()).isEmpty();
    }

    @Test
    void registerFsetFields_delegatesToMetadataWithoutError() {
        service.registerFsetFields(null);
    }

    @Test
    void oupostBmsMetadata_trivialStaticAccessors_returnExpectedDefaults() {
        assertThat(OupostBmsMetadata.getMapNames()).isEmpty();
        assertThat(OupostBmsMetadata.getLayoutResource()).isEqualTo("layout/OUPOST_WS.xml");
        assertThat(OupostBmsMetadata.getFieldMapping("ANY")).isNotNull();
    }
}

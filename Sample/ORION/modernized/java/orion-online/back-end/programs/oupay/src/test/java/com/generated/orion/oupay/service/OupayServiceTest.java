package com.generated.orion.oupay.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.generated.orion.oupay.accessor.OupayFields;
import com.generated.orion.oupay.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Unit tests for OupayService, generated from COBOL program OUPAY. All business logic is private
 * and is exercised solely through the public {@link OupayService#mainLine(AppService)} entry point,
 * driven by an {@link AppService} mock that emulates CICS READ UPDATE / REWRITE / WRITE.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OupayServiceTest {

    private static final String ACCTFILE = "ACCTFILE";
    private static final String CTRLFILE = "CTRLFILE";
    private static final String BILLFILE = "BILLFILE";
    private static final String BILL_KEY = "BILLID  ";

    private static final BigDecimal AMOUNT_100 = new BigDecimal("100.00");
    private static final BigDecimal BALANCE_500 = new BigDecimal("500.00");
    private static final BigDecimal CYC_CREDIT_20 = new BigDecimal("20.00");
    private static final long ACCT_ID = 12345L;

    @Mock private AppService appService;

    private OupayService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OupayService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
    }

    /**
     * Builds a serialized ORION-COMMAREA carrying KO-PARM-ACCT/AMT/DATE inside KOPS-AREA (aliased
     * onto CA-WORK-AREA).
     */
    private String commarea(long acctId, BigDecimal amt, String payDate) {
        OupayFields helper = new OupayFields(new WorkingStorage());
        helper.aliasGroup("KOPS-AREA", "CA-WORK-AREA");
        helper.setKoParmAcct(acctId);
        helper.setKoParmAmt(amt);
        if (payDate != null) {
            helper.setKoParmDate(payDate);
        }
        return helper.getOrionCommarea();
    }

    private void givenRequest(long acctId, BigDecimal amt, String payDate) {
        when(appService.getEibcalen()).thenReturn(692);
        when(appService.getCommarea()).thenReturn(commarea(acctId, amt, payDate));
    }

    /**
     * Decodes the ORION-COMMAREA bytes written back via setCommarea() into a fresh field accessor.
     */
    private OupayFields captureResult() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService).setCommarea(captor.capture());
        byte[] bytes = (byte[]) captor.getValue();
        OupayFields out = new OupayFields(new WorkingStorage());
        out.writeBytes("ORION-COMMAREA", bytes);
        out.aliasGroup("KOPS-AREA", "CA-WORK-AREA");
        return out;
    }

    private void stubReadAcctForUpdate(int resp, Consumer<OupayFields> populate) {
        doAnswer(
                        inv -> {
                            OupayFields into = inv.getArgument(1);
                            if (populate != null) {
                                populate.accept(into);
                            }
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(ACCTFILE), any(), anyString(), eq(0));
    }

    private void stubReadCtrlForUpdate(int resp, long lastValue) {
        doAnswer(
                        inv -> {
                            OupayFields into = inv.getArgument(1);
                            into.setCtLastValue(lastValue);
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(CTRLFILE), any(), anyString(), eq(0));
    }

    private void stubRewriteAcct(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(ACCTFILE), any());
    }

    private void stubWriteBill(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .writeFile(eq(BILLFILE), any(), anyString(), eq(0));
    }

    private void stubRewriteCtrl(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .rewriteFile(eq(CTRLFILE), any());
    }

    private void stubWriteCtrl(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .writeFile(eq(CTRLFILE), any(), anyString(), eq(0));
    }

    /** Stubs the whole happy path up to (and including) the ACCTFILE rewrite. */
    private void givenValidAccountAndSequence(long ctrlLastValue) {
        stubReadAcctForUpdate(
                0,
                f -> {
                    f.setAcActiveStatus("Y");
                    f.setAcCurrBal(BALANCE_500);
                    f.setAcCycCredit(CYC_CREDIT_20);
                });
        stubReadCtrlForUpdate(0, ctrlLastValue);
        stubRewriteAcct(0);
    }

    private void assertAmount(String label, BigDecimal expected, BigDecimal actual) {
        assertEquals(
                0,
                expected.compareTo(actual),
                label + ": expected " + expected + " but was " + actual);
    }

    // ───────────────────────── 2050-VALIDATE-REQUEST ─────────────────────────

    @Test
    void mainLine_zeroAmount_rejectsWithPositiveAmountMessage() {
        givenRequest(ACCT_ID, BigDecimal.ZERO, null);

        service.mainLine(appService);

        OupayFields out = captureResult();
        assertEquals("W", out.getKoStatus());
        assertEquals("PAYMENT AMOUNT MUST BE POSITIVE.", out.getKoStatusMsg().trim());
        assertEquals(1, out.getKoRejectCnt());
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_negativeAmount_rejectsWithPositiveAmountMessage() {
        givenRequest(ACCT_ID, new BigDecimal("-50.00"), null);

        service.mainLine(appService);

        OupayFields out = captureResult();
        assertEquals("PAYMENT AMOUNT MUST BE POSITIVE.", out.getKoStatusMsg().trim());
        assertEquals(1, out.getKoRejectCnt());
    }

    @Test
    void mainLine_zeroAccountId_rejectsWithAccountRequiredMessage() {
        givenRequest(0L, AMOUNT_100, null);

        service.mainLine(appService);

        OupayFields out = captureResult();
        assertEquals("ACCOUNT ID IS REQUIRED.", out.getKoStatusMsg().trim());
        assertEquals(1, out.getKoRejectCnt());
        verify(appService, never()).readFileForUpdate(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_zeroAmountAndZeroAccount_shortCircuitsOnAmountCheckOnly() {
        // Ground truth (OUPAY.cbl 2050-VALIDATE-REQUEST): the account-id check runs only
        // "IF NOT-REJECTED", so when the amount is already invalid the account check never
        // executes -- exactly one rejection is counted and the amount message wins.
        givenRequest(0L, BigDecimal.ZERO, null);

        service.mainLine(appService);

        OupayFields out = captureResult();
        assertEquals("PAYMENT AMOUNT MUST BE POSITIVE.", out.getKoStatusMsg().trim());
        assertEquals(1, out.getKoRejectCnt());
    }

    // ───────────────────────── 2100-READ-ACCT-UPD ─────────────────────────

    @Test
    void mainLine_accountNotActive_rejectsWithNotActiveMessageAndSkipsSequenceRead() {
        givenRequest(ACCT_ID, AMOUNT_100, null);
        stubReadAcctForUpdate(0, f -> f.setAcActiveStatus("N"));

        service.mainLine(appService);

        OupayFields out = captureResult();
        assertEquals("ACCOUNT IS NOT ACTIVE.", out.getKoStatusMsg().trim());
        assertEquals(1, out.getKoRejectCnt());
        assertEquals(1, out.getKoReadCnt());
        verify(appService, never()).readFileForUpdate(eq(CTRLFILE), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_accountNotFound_rejectsWithNotFoundMessage() {
        givenRequest(ACCT_ID, AMOUNT_100, null);
        stubReadAcctForUpdate(13, null);

        service.mainLine(appService);

        OupayFields out = captureResult();
        assertEquals("ACCOUNT NOT FOUND.", out.getKoStatusMsg().trim());
        assertEquals(1, out.getKoRejectCnt());
        assertEquals(0, out.getKoReadCnt());
    }

    @Test
    void mainLine_readAccountUnexpectedError_setsErrorStatusAndSkipsSequenceRead() {
        givenRequest(ACCT_ID, AMOUNT_100, null);
        stubReadAcctForUpdate(99, null);

        service.mainLine(appService);

        OupayFields out = captureResult();
        assertEquals("E", out.getKoStatus());
        assertEquals("READ ACCTFILE FOR UPDATE FAILED.", out.getKoStatusMsg().trim());
        verify(appService, never()).readFileForUpdate(eq(CTRLFILE), any(), anyString(), anyInt());
    }

    // ───────────────────────── 2200-GET-SEQUENCE ─────────────────────────

    @Test
    void mainLine_ctrlRecordMissing_startsSequenceAtOneAndWritesNewCtrlRecord() {
        givenRequest(ACCT_ID, AMOUNT_100, null);
        stubReadAcctForUpdate(
                0,
                f -> {
                    f.setAcActiveStatus("Y");
                    f.setAcCurrBal(BALANCE_500);
                    f.setAcCycCredit(CYC_CREDIT_20);
                });
        stubReadCtrlForUpdate(13, 0L);
        stubRewriteAcct(0);
        stubWriteBill(0);
        stubWriteCtrl(0);

        service.mainLine(appService);

        OupayFields out = captureResult();
        assertEquals(1, out.getKoC1());

        ArgumentCaptor<Object> ctrlCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeFile(eq(CTRLFILE), ctrlCaptor.capture(), eq(BILL_KEY), eq(0));
        OupayFields writtenCtrl = (OupayFields) ctrlCaptor.getValue();
        assertEquals(BILL_KEY, writtenCtrl.getCtKey());
        assertEquals("BILL ID SEQUENCE", writtenCtrl.getCtDesc().trim());
        assertEquals(1L, writtenCtrl.getCtLastValue());
        verify(appService, never()).rewriteFile(eq(CTRLFILE), any());
    }

    @Test
    void mainLine_readCtrlUnexpectedError_rejectsAndSkipsCreditAndWriteBill() {
        givenRequest(ACCT_ID, AMOUNT_100, null);
        stubReadAcctForUpdate(
                0,
                f -> {
                    f.setAcActiveStatus("Y");
                    f.setAcCurrBal(BALANCE_500);
                    f.setAcCycCredit(CYC_CREDIT_20);
                });
        doAnswer(
                        inv -> {
                            nextResp.set(99);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(eq(CTRLFILE), any(), anyString(), eq(0));

        service.mainLine(appService);

        OupayFields out = captureResult();
        assertEquals("E", out.getKoStatus());
        assertEquals("READ CTRLFILE BILLID FAILED.", out.getKoStatusMsg().trim());
        verify(appService, never()).rewriteFile(eq(ACCTFILE), any());
        verify(appService, never()).writeFile(eq(BILLFILE), any(), anyString(), anyInt());
    }

    // ───────────────────────── 2300-CREDIT-ACCOUNT ─────────────────────────

    @Test
    void mainLine_rewriteAcctFails_rejectsWithRewriteFailedMessageAndSkipsWriteBill() {
        givenRequest(ACCT_ID, AMOUNT_100, null);
        stubReadAcctForUpdate(
                0,
                f -> {
                    f.setAcActiveStatus("Y");
                    f.setAcCurrBal(BALANCE_500);
                    f.setAcCycCredit(CYC_CREDIT_20);
                });
        stubReadCtrlForUpdate(0, 41L);
        stubRewriteAcct(99);

        service.mainLine(appService);

        OupayFields out = captureResult();
        assertEquals("E", out.getKoStatus());
        assertEquals("REWRITE ACCTFILE FAILED.", out.getKoStatusMsg().trim());
        verify(appService, never()).writeFile(eq(BILLFILE), any(), anyString(), anyInt());
    }

    // ───────────────────────── 2400-WRITE-BILL ─────────────────────────

    @Test
    void mainLine_writeBillFails_rejectsWithWriteFailedMessageAndSkipsSaveSequence() {
        givenRequest(ACCT_ID, AMOUNT_100, null);
        givenValidAccountAndSequence(41L);
        stubWriteBill(99);

        service.mainLine(appService);

        OupayFields out = captureResult();
        assertEquals("E", out.getKoStatus());
        assertEquals("WRITE BILLFILE FAILED.", out.getKoStatusMsg().trim());
        assertEquals(0, out.getKoPostedCnt());
        verify(appService, never()).rewriteFile(eq(CTRLFILE), any());
        verify(appService, never()).writeFile(eq(CTRLFILE), any(), anyString(), anyInt());
    }

    // ───────────────────────── 2000/2500 full happy path ─────────────────────────

    @Test
    void mainLine_fullFlowDefaultDate_postsSuccessfullyAndUpdatesAllFiles() {
        givenRequest(ACCT_ID, AMOUNT_100, null);
        givenValidAccountAndSequence(41L);
        stubWriteBill(0);
        stubRewriteCtrl(0);

        service.mainLine(appService);

        OupayFields out = captureResult();
        assertEquals("O", out.getKoStatus());
        assertEquals("PAYMENT POSTED SUCCESSFULLY.", out.getKoStatusMsg().trim());
        assertEquals(1, out.getKoReadCnt());
        assertEquals(1, out.getKoPostedCnt());
        assertEquals(1, out.getKoTranCnt());
        assertEquals(0, out.getKoRejectCnt());
        assertEquals(42, out.getKoC1());
        assertAmount("KO-AMT-1", AMOUNT_100, out.getKoAmt1());
        assertAmount("KO-AMT-2", new BigDecimal("400.00"), out.getKoAmt2());

        ArgumentCaptor<Object> acctCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService).rewriteFile(eq(ACCTFILE), acctCaptor.capture());
        OupayFields rewrittenAcct = (OupayFields) acctCaptor.getValue();
        assertAmount("AC-CURR-BAL", new BigDecimal("400.00"), rewrittenAcct.getAcCurrBal());
        assertAmount("AC-CYC-CREDIT", new BigDecimal("120.00"), rewrittenAcct.getAcCycCredit());

        ArgumentCaptor<Object> billCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeFile(eq(BILLFILE), billCaptor.capture(), eq("42"), eq(0));
        OupayFields writtenBill = (OupayFields) billCaptor.getValue();
        assertEquals(42L, writtenBill.getBlId());
        assertEquals(ACCT_ID, writtenBill.getBlAcctId());
        assertAmount("BL-AMOUNT", AMOUNT_100, writtenBill.getBlAmount());
        assertEquals("P", writtenBill.getBlStatus().trim());
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        assertEquals(today, writtenBill.getBlPayDate().trim());

        ArgumentCaptor<Object> ctrlCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService).rewriteFile(eq(CTRLFILE), ctrlCaptor.capture());
        OupayFields rewrittenCtrl = (OupayFields) ctrlCaptor.getValue();
        assertEquals(42L, rewrittenCtrl.getCtLastValue());
        verify(appService, never()).writeFile(eq(CTRLFILE), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_explicitPayDate_billUsesProvidedDateNotCurrentDate() {
        String payDate = "2024-05-01";
        givenRequest(ACCT_ID, AMOUNT_100, payDate);
        givenValidAccountAndSequence(41L);
        stubWriteBill(0);
        stubRewriteCtrl(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> billCaptor = ArgumentCaptor.forClass(Object.class);
        verify(appService).writeFile(eq(BILLFILE), billCaptor.capture(), anyString(), eq(0));
        OupayFields writtenBill = (OupayFields) billCaptor.getValue();
        assertEquals(payDate, writtenBill.getBlPayDate().trim());
    }

    // ───────────────────────── 2500-SAVE-SEQUENCE ─────────────────────────

    @Test
    void mainLine_saveSequenceRewriteFails_warnsButKeepsPaymentPosted() {
        givenRequest(ACCT_ID, AMOUNT_100, null);
        givenValidAccountAndSequence(41L);
        stubWriteBill(0);
        stubRewriteCtrl(99);

        service.mainLine(appService);

        OupayFields out = captureResult();
        assertEquals("W", out.getKoStatus());
        assertEquals("PAYMENT POSTED, COUNTER UPDATE WARNING.", out.getKoStatusMsg().trim());
        // 9000-FINALISE must NOT overwrite the warning with the success message.
        assertEquals(1, out.getKoPostedCnt());
    }

    @Test
    void mainLine_saveSequenceWriteFailsWhenCtrlWasMissing_warnsButKeepsPaymentPosted() {
        givenRequest(ACCT_ID, AMOUNT_100, null);
        stubReadAcctForUpdate(
                0,
                f -> {
                    f.setAcActiveStatus("Y");
                    f.setAcCurrBal(BALANCE_500);
                    f.setAcCycCredit(CYC_CREDIT_20);
                });
        stubReadCtrlForUpdate(13, 0L);
        stubRewriteAcct(0);
        stubWriteBill(0);
        stubWriteCtrl(99);

        service.mainLine(appService);

        OupayFields out = captureResult();
        assertEquals("W", out.getKoStatus());
        assertEquals("PAYMENT POSTED, COUNTER UPDATE WARNING.", out.getKoStatusMsg().trim());
        assertEquals(1, out.getKoPostedCnt());
    }

    // ───────────────────────── 9000-FINALISE ─────────────────────────

    @Test
    void mainLine_errorStatus_finaliseLeavesErrorMessageUnchanged() {
        givenRequest(ACCT_ID, AMOUNT_100, null);
        stubReadAcctForUpdate(99, null);

        service.mainLine(appService);

        OupayFields out = captureResult();
        assertEquals("E", out.getKoStatus());
        assertEquals("READ ACCTFILE FOR UPDATE FAILED.", out.getKoStatusMsg().trim());
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOupay() {
        assertEquals("OUPAY", service.getProgramName());
    }

    @Test
    void getButtonDefs_delegatesToMetadataWithoutError() {
        assertNotNull(service.getButtonDefs());
    }

    @Test
    void registerFsetFields_delegatesToMetadataWithoutError() {
        AppRunner runner = mock(AppRunner.class);
        assertDoesNotThrow(() -> service.registerFsetFields(runner));
    }
}

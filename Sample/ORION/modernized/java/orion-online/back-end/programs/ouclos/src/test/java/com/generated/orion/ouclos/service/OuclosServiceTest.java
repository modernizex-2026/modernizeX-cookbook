package com.generated.orion.ouclos.service;

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
import com.generated.orion.ouclos.accessor.OuclosFields;
import com.generated.orion.ouclos.metadata.OuclosBmsMetadata;
import com.generated.orion.ouclos.model.WorkingStorage;

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
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Ground truth: OUCLOS.cbl (ORION-CCMS). Every input/expected value below is derived from the COBOL
 * PROCEDURE DIVISION, not from OuclosService's current behavior.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class OuclosServiceTest {

    private static final int RESP_NORMAL = 0;
    private static final int RESP_NOTFND = 13;
    private static final int RESP_ENDFILE = 20;
    private static final int RESP_OTHER = 17;

    private static final String STATUS_ACTIVE = "Y";
    private static final String STATUS_CLOSED = "N";

    @Mock private AppService appService;

    private final OuclosService service = new OuclosService();
    private final AtomicInteger currentResp = new AtomicInteger(RESP_NORMAL);

    private record ReadStep(int resp, Consumer<OuclosFields> mutator) {}

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(appService.getEibcalen()).thenReturn(1);
        org.mockito.Mockito.lenient()
                .when(appService.getEibresp())
                .thenAnswer(inv -> currentResp.get());
    }

    private static OuclosFields newFields() {
        OuclosFields f = new OuclosFields(new WorkingStorage());
        f.aliasGroup("KOPS-AREA", "CA-WORK-AREA");
        return f;
    }

    private static String expiredDate() {
        return LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private static String notExpiredDate() {
        return LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private byte[] buildInputCommarea(Consumer<OuclosFields> setup) {
        OuclosFields f = newFields();
        setup.accept(f);
        return f.sliceBytes("ORION-COMMAREA");
    }

    private OuclosFields runMainLine(Consumer<OuclosFields> inputSetup) {
        byte[] input = buildInputCommarea(inputSetup);
        when(appService.getCommarea()).thenReturn(input);
        service.mainLine(appService);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService, times(1)).setCommarea(captor.capture());
        OuclosFields out = newFields();
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

    private void stubReadNext(List<ReadStep> steps) {
        Queue<ReadStep> queue = new ArrayDeque<>(steps);
        doAnswer(
                        inv -> {
                            ReadStep step = queue.poll();
                            OuclosFields into = (OuclosFields) inv.getArgument(1);
                            step.mutator().accept(into);
                            currentResp.set(step.resp());
                            return null;
                        })
                .when(appService)
                .readNext(any(), any());
    }

    private void stubReadForUpdate(int resp) {
        doAnswer(
                        inv -> {
                            currentResp.set(resp);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(any(), any(), any(), anyInt());
    }

    private void stubRewrite(int resp) {
        doAnswer(
                        inv -> {
                            currentResp.set(resp);
                            return null;
                        })
                .when(appService)
                .rewriteFile(any(), any());
    }

    private static Consumer<OuclosFields> account(
            long id, String status, String expiryDate, String creditLimit, String currBal) {
        return f -> {
            f.setAcId(id);
            f.setAcActiveStatus(status);
            f.setAcExpiryDate(expiryDate);
            f.setAcCreditLimit(new BigDecimal(creditLimit));
            f.setAcCurrBal(new BigDecimal(currBal));
        };
    }

    // ---------------------------------------------------------------
    // 3100-START-BROWSE (COBOL lines 135-157)
    // ---------------------------------------------------------------

    @Test
    void mainLine_startBrowseNotFound_noAccountsMessageThenOverwrittenByFinalise() {
        stubStartBrowse(RESP_NOTFND);

        OuclosFields out = runMainLine(f -> f.setKoParmAcct(0));

        // 3100 sets "NO ACCOUNTS TO PROCESS." but KO-STATUS never becomes 'E', so
        // 9000-FINALISE unconditionally overwrites the message (COBOL lines 264-274).
        assertEquals("O", out.getKoStatus());
        assertEquals("ACCOUNT CLOSE PROCESSING COMPLETE.", out.getKoStatusMsg().trim());
        assertEquals(0, out.getKoReadCnt());
        verify(appService, never()).readNext(any(), any());
        verify(appService, never()).endBrowse(any());
    }

    @Test
    void mainLine_startBrowseOtherError_setsStatusErrorAndKeepsMessage() {
        stubStartBrowse(RESP_OTHER);

        OuclosFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals("E", out.getKoStatus());
        assertEquals("STARTBR ACCTFILE FAILED.", out.getKoStatusMsg().trim());
        verify(appService, never()).readNext(any(), any());
        verify(appService, never()).endBrowse(any());
    }

    @Test
    void mainLine_filterAccountSet_startsBrowseAtFilterKey() {
        stubStartBrowse(RESP_NOTFND);
        ArgumentCaptor<String> ridfldCaptor = ArgumentCaptor.forClass(String.class);

        runMainLine(f -> f.setKoParmAcct(12345));

        verify(appService).startBrowse(any(), ridfldCaptor.capture(), anyInt());
        assertEquals(12345L, Long.parseLong(ridfldCaptor.getValue().trim()));
    }

    @Test
    void mainLine_noFilter_startsBrowseAtZero() {
        stubStartBrowse(RESP_NOTFND);
        ArgumentCaptor<String> ridfldCaptor = ArgumentCaptor.forClass(String.class);

        runMainLine(f -> f.setKoParmAcct(0));

        verify(appService).startBrowse(any(), ridfldCaptor.capture(), anyInt());
        assertEquals(0L, Long.parseLong(ridfldCaptor.getValue().trim()));
    }

    // ---------------------------------------------------------------
    // 3200-READ-NEXT-ACCT (COBOL lines 161-177)
    // ---------------------------------------------------------------

    @Test
    void mainLine_readNextImmediateEndfile_endsBrowseAndCompletes() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(List.of(new ReadStep(RESP_ENDFILE, f -> {})));

        OuclosFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals("O", out.getKoStatus());
        assertEquals("ACCOUNT CLOSE PROCESSING COMPLETE.", out.getKoStatusMsg().trim());
        assertEquals(0, out.getKoReadCnt());
        verify(appService, times(1)).readNext(any(), any());
        verify(appService, times(1)).endBrowse(any());
    }

    @Test
    void mainLine_readNextOtherError_setsStatusErrorAndEndsBrowse() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(List.of(new ReadStep(RESP_OTHER, f -> {})));

        OuclosFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals("E", out.getKoStatus());
        assertEquals("READNEXT ACCTFILE FAILED.", out.getKoStatusMsg().trim());
        verify(appService, times(1)).endBrowse(any());
    }

    // ---------------------------------------------------------------
    // 4000-PROCESS-ACCT — filter mismatch / skip already-closed (COBOL 181-197)
    // ---------------------------------------------------------------

    @Test
    void mainLine_filterActiveAccountMismatch_endsBrowseWithoutFurtherRead() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(999, STATUS_ACTIVE, "        ", "1000.00", "0.00"))));

        OuclosFields out = runMainLine(f -> f.setKoParmAcct(100));

        assertEquals(0, out.getKoSkipCnt());
        assertEquals(0, out.getKoC3());
        assertEquals(0, out.getKoPostedCnt());
        verify(appService, times(1)).readNext(any(), any());
        verify(appService, never()).readFileForUpdate(any(), any(), any(), anyInt());
    }

    @Test
    void mainLine_alreadyClosedAccount_incrementsSkipCntAndKeepsBrowsing() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_CLOSED, "        ", "1000.00", "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));

        OuclosFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals(1, out.getKoSkipCnt());
        assertEquals(0, out.getKoC3());
        verify(appService, never()).readFileForUpdate(any(), any(), any(), anyInt());
        verify(appService, times(2)).readNext(any(), any());
    }

    // ---------------------------------------------------------------
    // 4100-CHECK-CONDITIONS — EXPIRED / ZEROLIMIT / KEEP (COBOL 200-212)
    // ---------------------------------------------------------------

    @Test
    void mainLine_activeAccountKept_notExpiredAndCreditLimitNonZero_incrementsC3() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, notExpiredDate(), "500.00", "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));

        OuclosFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals(1, out.getKoC3());
        assertEquals(0, out.getKoPostedCnt());
        verify(appService, never()).readFileForUpdate(any(), any(), any(), anyInt());
    }

    @Test
    void mainLine_expiryDateAllSpaces_notTreatedAsExpired_fallsThroughToCreditCheck() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, "          ", "0.00", "10.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubReadForUpdate(RESP_NORMAL);
        stubRewrite(RESP_NORMAL);

        OuclosFields out = runMainLine(f -> f.setKoParmAcct(0));

        // AC-EXPIRY-DATE = SPACES bypasses the expired branch entirely (COBOL 203-204);
        // zero credit limit still triggers closure with reason ZEROLIMIT.
        assertEquals(1, out.getKoC2());
        assertEquals(0, out.getKoC1());
        assertEquals(1, out.getKoPostedCnt());
    }

    @Test
    void mainLine_expiredAndZeroLimit_expiredReasonTakesPrecedence() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, expiredDate(), "0.00", "50.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubReadForUpdate(RESP_NORMAL);
        stubRewrite(RESP_NORMAL);

        OuclosFields out = runMainLine(f -> f.setKoParmAcct(0));

        // COBOL 4100-CHECK-CONDITIONS: expiry check comes first as an IF/ELSE, so a
        // simultaneously-zero credit limit never reaches the ZEROLIMIT branch.
        assertEquals(1, out.getKoC1());
        assertEquals(0, out.getKoC2());
        assertEquals(1, out.getKoPostedCnt());
    }

    // ---------------------------------------------------------------
    // 4200-CLOSE-ACCT — READ UPDATE / REWRITE outcomes (COBOL 216-249)
    // ---------------------------------------------------------------

    @Test
    void mainLine_closeAccount_happyPath_expiredReason_updatesAllCountersAndAmount() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, expiredDate(), "500.00", "123.45")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubReadForUpdate(RESP_NORMAL);
        stubRewrite(RESP_NORMAL);

        OuclosFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals(1, out.getKoPostedCnt());
        assertEquals(1, out.getKoUpdateCnt());
        assertEquals(1, out.getKoSelectCnt());
        assertEquals(1, out.getKoC1());
        assertEquals(0, out.getKoRejectCnt());
        assertEquals(0, new BigDecimal("123.45").compareTo(out.getKoAmt1()));
        assertEquals("O", out.getKoStatus());
        verify(appService, times(1)).rewriteFile(any(), any());
    }

    @Test
    void mainLine_closeAccount_readForUpdateFails_incrementsRejectAndSkipsRewrite() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, expiredDate(), "500.00", "123.45")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubReadForUpdate(RESP_OTHER);

        OuclosFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals(1, out.getKoRejectCnt());
        assertEquals(0, out.getKoPostedCnt());
        assertEquals(0, out.getKoC1());
        assertEquals("W", out.getKoStatus());
        verify(appService, never()).rewriteFile(any(), any());
    }

    @Test
    void mainLine_closeAccount_rewriteFails_incrementsRejectOnlyAndLeavesAmountUntouched() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, expiredDate(), "500.00", "123.45")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubReadForUpdate(RESP_NORMAL);
        stubRewrite(RESP_OTHER);

        OuclosFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals(1, out.getKoRejectCnt());
        assertEquals(0, out.getKoPostedCnt());
        assertEquals(0, out.getKoUpdateCnt());
        assertEquals(0, out.getKoSelectCnt());
        assertEquals(0, new BigDecimal("0").compareTo(out.getKoAmt1()));
        assertEquals("W", out.getKoStatus());
    }

    // ---------------------------------------------------------------
    // 9000-FINALISE (COBOL 263-274)
    // ---------------------------------------------------------------

    @Test
    void mainLine_rejectCountPositiveButNoHardError_statusIsWarnNotOk() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, expiredDate(), "500.00", "10.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubReadForUpdate(RESP_OTHER);

        OuclosFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals("W", out.getKoStatus());
        assertEquals("ACCOUNT CLOSE PROCESSING COMPLETE.", out.getKoStatusMsg().trim());
    }

    @Test
    void mainLine_multipleAccountsMixedOutcomes_aggregatesCountersCorrectly() {
        stubStartBrowse(RESP_NORMAL);
        stubReadNext(
                List.of(
                        new ReadStep(
                                RESP_NORMAL,
                                account(1, STATUS_ACTIVE, expiredDate(), "500.00", "100.00")),
                        new ReadStep(
                                RESP_NORMAL,
                                account(2, STATUS_ACTIVE, "          ", "0.00", "50.00")),
                        new ReadStep(
                                RESP_NORMAL,
                                account(3, STATUS_ACTIVE, notExpiredDate(), "300.00", "0.00")),
                        new ReadStep(
                                RESP_NORMAL,
                                account(4, STATUS_CLOSED, "          ", "0.00", "0.00")),
                        new ReadStep(RESP_ENDFILE, f -> {})));
        stubReadForUpdate(RESP_NORMAL);
        stubRewrite(RESP_NORMAL);

        OuclosFields out = runMainLine(f -> f.setKoParmAcct(0));

        assertEquals(4, out.getKoReadCnt());
        assertEquals(2, out.getKoPostedCnt());
        assertEquals(1, out.getKoC1());
        assertEquals(1, out.getKoC2());
        assertEquals(1, out.getKoC3());
        assertEquals(1, out.getKoSkipCnt());
        assertEquals(0, out.getKoRejectCnt());
        assertEquals(0, new BigDecimal("150.00").compareTo(out.getKoAmt1()));
        assertEquals("O", out.getKoStatus());
    }

    // ---------------------------------------------------------------
    // mainLine commarea marshalling — CALL BY REFERENCE transport (Object[] wrapping
    // a byte[] element), an alternate shape alongside the plain byte[] COMMAREA used
    // by every other test in this class.
    // ---------------------------------------------------------------

    @Test
    void mainLine_commareaAsObjectArrayOfBytes_marshalsInAndOutViaSameArray() {
        stubStartBrowse(RESP_NOTFND);
        byte[] input = buildInputCommarea(f -> f.setKoParmAcct(0));
        Object[] params = new Object[] {input};
        when(appService.getCommarea()).thenReturn(params);

        service.mainLine(appService);

        verify(appService, never()).setCommarea(any());
        OuclosFields out = newFields();
        out.writeBytes("ORION-COMMAREA", (byte[]) params[0]);
        assertEquals("O", out.getKoStatus());
        assertEquals("ACCOUNT CLOSE PROCESSING COMPLETE.", out.getKoStatusMsg().trim());
    }

    // ---------------------------------------------------------------
    // AppProgram plumbing
    // ---------------------------------------------------------------

    @Test
    void getProgramName_returnsOuclos() {
        assertEquals("OUCLOS", service.getProgramName());
    }

    @Test
    void getButtonDefs_delegatesToMetadata() {
        assertEquals(OuclosBmsMetadata.getButtonDefs(), service.getButtonDefs());
    }

    @Test
    void registerFsetFields_delegatesToMetadataWithoutThrowing() {
        assertDoesNotThrow(() -> service.registerFsetFields(null));
    }
}

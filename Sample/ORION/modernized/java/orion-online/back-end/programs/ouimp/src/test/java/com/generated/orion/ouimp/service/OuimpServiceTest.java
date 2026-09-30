package com.generated.orion.ouimp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.generated.orion.ouimp.accessor.OuimpFields;
import com.generated.orion.ouimp.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OuimpService, generated from COBOL program OUIMP (ORION-CCMS on-line
 * account-import sub). Two testing styles are used:
 *
 * <ul>
 *   <li>Full control-flow tests drive the public {@link OuimpService#mainLine(AppService)} entry
 *       point through a mocked {@link AppService} (CICS STARTBR/READNEXT/READ
 *       UPDATE/REWRITE/WRITE/ENDBR + the KIM-PARM commarea round-trip).
 *   <li>Paragraph-level tests invoke the private 3400/3600-series validation and apply-account
 *       methods directly via reflection on a hand-built {@link OuimpService.TaskContext}. This is
 *       required because of a severe CONVERT-GAP: {@code parseImportLine} (COBOL 3300-PARSE-LINE)
 *       never performs the UNSTRING of IMP-DATA — it only blanks WS-FIELDS and zeroes WS-FLD-CNT.
 *       WS-F-TYPE is therefore always empty, so the 3200 switch can NEVER reach the
 *       RECTYPE/CUST/ACCT branches through {@code mainLine}; every line falls into the "UNKNOWN
 *       RECTYPE" default and is rejected. The 3400/3410/.../3610 paragraphs are exercised here in
 *       isolation (bypassing the broken parse) so their still-intact business logic remains covered
 *       and regression-guarded.
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OuimpServiceTest {

    @Mock private AppService appService;

    private final OuimpService service = new OuimpService();

    private final AtomicInteger eibresp = new AtomicInteger(0);

    private final Deque<String[]> impQueue = new ArrayDeque<>();

    @BeforeEach
    void setUp() {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getEibresp()).thenAnswer(inv -> eibresp.get());
    }

    // ---------------------------------------------------------------
    // Commarea request/response helpers.
    // ---------------------------------------------------------------

    private String buildRequest(String startKey, int max) {
        OuimpFields req = new OuimpFields(new WorkingStorage());
        req.aliasGroup("KIM-PARM", "CA-WORK-AREA");
        req.setKimStartKey(startKey);
        req.setKimMax(max);
        return req.getOrionCommarea();
    }

    private void givenRequest(String startKey, int max) {
        when(appService.getCommarea()).thenReturn(buildRequest(startKey, max));
    }

    private void givenStartBrowseResp(int resp) {
        doAnswer(
                        inv -> {
                            eibresp.set(resp);
                            return null;
                        })
                .when(appService)
                .startBrowse(anyString(), anyString(), anyInt());
    }

    /**
     * Feeds READNEXT (used by both 3100-READ-LINE and 4000-PEEK-NEXT) with the given IMP-DATA
     * payloads in order, then ENDFILE (resp 20) once exhausted.
     */
    private void givenReadNextSequence(String... impDataLines) {
        impQueue.clear();
        int i = 1;
        for (String data : impDataLines) {
            impQueue.add(new String[] {String.format("KEY%05d", i++), data});
        }
        doAnswer(
                        inv -> {
                            if (impQueue.isEmpty()) {
                                eibresp.set(20);
                                return null;
                            }
                            String[] rec = impQueue.poll();
                            OuimpFields f = inv.getArgument(1);
                            f.setImpKey(rec[0]);
                            f.setImpData(rec[1]);
                            eibresp.set(0);
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
    }

    private OuimpFields runAndCaptureResponse() {
        service.mainLine(appService);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService).setCommarea(captor.capture());
        OuimpFields out = new OuimpFields(new WorkingStorage());
        out.writeBytes("ORION-COMMAREA", (byte[]) captor.getValue());
        out.aliasGroup("KIM-PARM", "CA-WORK-AREA");
        return out;
    }

    // ---------------------------------------------------------------
    // Reflection helper for direct paragraph-level tests.
    // ---------------------------------------------------------------

    private OuimpService.TaskContext newCtx() {
        return new OuimpService.TaskContext(appService);
    }

    private void invokePrivate(String methodName, OuimpService.TaskContext ctx) throws Exception {
        Method m = OuimpService.class.getDeclaredMethod(methodName, OuimpService.TaskContext.class);
        m.setAccessible(true);
        m.invoke(service, ctx);
    }

    // =================================================================
    // Trivial delegate methods.
    // =================================================================

    @Test
    void getProgramName_returnsOuimp() {
        assertThat(service.getProgramName()).isEqualTo("OUIMP");
    }

    @Test
    void getButtonDefs_delegatesToMetadata_returnsEmptyList() {
        assertThat(service.getButtonDefs()).isEmpty();
    }

    @Test
    void registerFsetFields_delegatesToMetadata_doesNotThrow() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.registerFsetFields(null));
    }

    // =================================================================
    // 2000-POSITION: STARTBR RESP handling.
    // =================================================================

    @Test
    void mainLine_startBrowseNotFound_skipsImportAndReportsEmptyFeed() {
        givenRequest("KEYSTART", 0);
        givenStartBrowseResp(13);

        OuimpFields out = runAndCaptureResponse();

        assertThat(out.getKimStatus()).isEqualTo("10");
        assertThat(out.getKimMsg().trim()).isEqualTo("IMPORT FEED IS EMPTY");
        assertThat(out.getKimRead()).isEqualTo(0);
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startBrowseEndfile_skipsImportAndReportsEmptyFeed() {
        givenRequest("KEYSTART", 0);
        givenStartBrowseResp(20);

        OuimpFields out = runAndCaptureResponse();

        assertThat(out.getKimStatus()).isEqualTo("10");
        assertThat(out.getKimRead()).isEqualTo(0);
        verify(appService, never()).readNext(anyString(), any());
    }

    @Test
    void mainLine_startBrowseOtherFailure_setsStatus99AndSkipsBrowseEntirely() {
        givenRequest("KEYSTART", 0);
        givenStartBrowseResp(66);

        OuimpFields out = runAndCaptureResponse();

        assertThat(out.getKimStatus()).isEqualTo("99");
        assertThat(out.getKimMsg().trim()).isEqualTo("IMPFILE STARTBR FAILED");
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void mainLine_startKeyProvided_positionsBrowseAtGivenKey() {
        givenRequest("MYKEY123", 0);
        givenStartBrowseResp(0);
        givenReadNextSequence();

        ArgumentCaptor<String> ridCaptor = ArgumentCaptor.forClass(String.class);
        runAndCaptureResponse();

        verify(appService).startBrowse(anyString(), ridCaptor.capture(), anyInt());
        assertThat(ridCaptor.getValue()).isEqualTo("MYKEY123");
    }

    @Test
    void mainLine_startKeyBlank_positionsBrowseAtLowValues() {
        givenRequest("        ", 0);
        givenStartBrowseResp(0);
        givenReadNextSequence();

        ArgumentCaptor<String> ridCaptor = ArgumentCaptor.forClass(String.class);
        runAndCaptureResponse();

        verify(appService).startBrowse(anyString(), ridCaptor.capture(), anyInt());
        String rid = ridCaptor.getValue();
        assertThat(rid).hasSize(8);
        assertThat(rid.chars().allMatch(c -> c == 0)).isTrue();
    }

    // =================================================================
    // 3000/3100/6000: main import loop, empty feed, read errors.
    // =================================================================

    @Test
    void mainLine_emptyFeed_immediateEndOfFile_setsStatus10() {
        givenRequest("KEYSTART", 0);
        givenStartBrowseResp(0);
        givenReadNextSequence();

        OuimpFields out = runAndCaptureResponse();

        assertThat(out.getKimRead()).isEqualTo(0);
        assertThat(out.getKimStatus()).isEqualTo("10");
        assertThat(out.getKimMsg().trim()).isEqualTo("IMPORT FEED IS EMPTY");
        assertThat(out.getKimMore()).isEqualTo("N");
        verify(appService).endBrowse(anyString());
    }

    @Test
    void mainLine_singleUnrecognizedLine_incrementsReadAndRejectsUnknownRectype() {
        givenRequest("KEYSTART", 0);
        givenStartBrowseResp(0);
        givenReadNextSequence("GARBAGE,DATA,LINE");

        OuimpFields out = runAndCaptureResponse();

        assertThat(out.getKimRead()).isEqualTo(1);
        assertThat(out.getKimRejected()).isEqualTo(1);
        assertThat(out.getKimSkipped()).isEqualTo(0);
        assertThat(out.getKimAdded()).isEqualTo(0);
        assertThat(out.getKimLastReason().trim()).isEqualTo("UNKNOWN RECTYPE");
        assertThat(out.getKimLastKey().trim()).isEqualTo("KEY00001");
        assertThat(out.getKimStatus()).isEqualTo("00");
        assertThat(out.getKimMsg().trim()).isEqualTo("ACCOUNT IMPORT COMPLETE");
    }

    @Test
    void mainLine_multipleUnrecognizedLines_allRejectedIndividually() {
        givenRequest("KEYSTART", 0);
        givenStartBrowseResp(0);
        givenReadNextSequence("LINE1", "LINE2", "LINE3");

        OuimpFields out = runAndCaptureResponse();

        assertThat(out.getKimRead()).isEqualTo(3);
        assertThat(out.getKimRejected()).isEqualTo(3);
        assertThat(out.getKimLastKey().trim()).isEqualTo("KEY00003");
    }

    @Test
    void mainLine_readNextErrorMidLoop_incrementsErrorsButFinalMsgReflectsEmptyRead() {
        // COBOL 6000-SET-STATUS derives KIM-STATUS/KIM-MSG solely from KIM-READ, so a
        // READNEXT failure that happens before any line is counted gets its own
        // "IMPFILE READNEXT FAILED" message overwritten by "IMPORT FEED IS EMPTY".
        // This is COBOL-faithful behavior in Java too (not a convert-gap).
        givenRequest("KEYSTART", 0);
        givenStartBrowseResp(0);
        doAnswer(
                        inv -> {
                            eibresp.set(99);
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());

        OuimpFields out = runAndCaptureResponse();

        assertThat(out.getKimRead()).isEqualTo(0);
        assertThat(out.getKimErrors()).isEqualTo(1);
        assertThat(out.getKimStatus()).isEqualTo("10");
        assertThat(out.getKimMsg().trim()).isEqualTo("IMPORT FEED IS EMPTY");
        // WS-BR-ON was already set true by a successful STARTBR, so per COBOL's
        // "IF WS-BR-ON ... PERFORM 5000-END-BROWSE" structure, ENDBR still runs
        // even though the read itself failed.
        verify(appService).endBrowse(anyString());
    }

    @Test
    void mainLine_capReached_stopsAtMaxAndPeeksNextKey() {
        givenRequest("KEYSTART", 1);
        givenStartBrowseResp(0);
        givenReadNextSequence("LINE1", "LINE2");

        OuimpFields out = runAndCaptureResponse();

        assertThat(out.getKimRead()).isEqualTo(1);
        assertThat(out.getKimRejected()).isEqualTo(1);
        assertThat(out.getKimMore()).isEqualTo("Y");
        assertThat(out.getKimNextKey().trim()).isEqualTo("KEY00002");
        verify(appService, org.mockito.Mockito.times(2)).readNext(anyString(), any());
        verify(appService).endBrowse(anyString());
    }

    // =================================================================
    // CONVERT-GAP: parseImportLine never performs the UNSTRING, so WS-F-TYPE is
    // always blank and the 3200 switch can never reach RECTYPE/CUST/ACCT. These
    // three tests encode the COBOL-ground-truth outcome for typed lines and are
    // EXPECTED TO FAIL against the current Java implementation.
    // =================================================================

    @Test
    void mainLine_CONVERT_GAP_rectypeHeaderLine_cobolSkipsButJavaRejects() {
        // COBOL: WS-F-TYPE = 'RECTYPE' -> WHEN 'RECTYPE' -> ADD 1 TO KIM-SKIPPED.
        givenRequest("KEYSTART", 0);
        givenStartBrowseResp(0);
        givenReadNextSequence("RECTYPE,HDR,FIELDS,...");

        OuimpFields out = runAndCaptureResponse();

        assertThat(out.getKimSkipped())
                .as(
                        "CONVERT-GAP: COBOL skips a RECTYPE header row; Java's parseImportLine"
                                + " never populates WS-F-TYPE, so it falls into the UNKNOWN RECTYPE"
                                + " reject branch")
                .isEqualTo(1);
        assertThat(out.getKimRejected()).isEqualTo(0);
    }

    @Test
    void mainLine_CONVERT_GAP_custLine_cobolSkipsButJavaRejects() {
        givenRequest("KEYSTART", 0);
        givenStartBrowseResp(0);
        givenReadNextSequence("CUST,00000012345,...");

        OuimpFields out = runAndCaptureResponse();

        assertThat(out.getKimSkipped())
                .as("CONVERT-GAP: COBOL skips a CUST row; Java rejects it as UNKNOWN RECTYPE")
                .isEqualTo(1);
        assertThat(out.getKimRejected()).isEqualTo(0);
    }

    @Test
    void mainLine_CONVERT_GAP_validAcctLine_cobolAddsAccountButJavaRejects() {
        // A fully well-formed ACCT line (13 CSV fields, all edits pass per COBOL 3400-VALIDATE)
        // that COBOL would WRITE as a brand-new account (KIM-ADDED/KIM-ACCEPTED = 1).
        givenRequest("KEYSTART", 0);
        givenStartBrowseResp(0);
        givenReadNextSequence(
                "ACCT,00000012345,Y,100.00,500.00,200.00,2020-01-01,2025-01-01,2020-06-01,10.00,5.00,90210,GRP1");

        OuimpFields out = runAndCaptureResponse();

        assertThat(out.getKimAdded())
                .as(
                        "CONVERT-GAP: COBOL would add this valid ACCT line; Java rejects it as "
                                + "UNKNOWN RECTYPE because parseImportLine never runs the UNSTRING")
                .isEqualTo(1);
        assertThat(out.getKimAccepted()).isEqualTo(1);
        assertThat(out.getKimRejected()).isEqualTo(0);
    }

    // =================================================================
    // 3400-VALIDATE (+ 3410/3420/3430/3440/3450 cascade), invoked directly on a
    // hand-built TaskContext to bypass the broken 3300-PARSE-LINE.
    // =================================================================

    private void setParsedFields(
            OuimpService.TaskContext ctx,
            int fldCnt,
            String id,
            String status,
            String bal,
            String crlim,
            String cslim,
            String open,
            String expiry,
            String reiss,
            String cycr,
            String cydr) {
        ctx.f.setWsFldCnt(fldCnt);
        ctx.f.setWsFId(id);
        ctx.f.setWsFStatus(status);
        ctx.f.setWsFBal(bal);
        ctx.f.setWsFCrlim(crlim);
        ctx.f.setWsFCslim(cslim);
        ctx.f.setWsFOpen(open);
        ctx.f.setWsFExpiry(expiry);
        ctx.f.setWsFReiss(reiss);
        ctx.f.setWsFCycr(cycr);
        ctx.f.setWsFCydr(cydr);
    }

    @Test
    void validateImportRecord_allFieldsValid_acceptsAndParsesNumerics() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setParsedFields(
                ctx,
                13,
                "00000012345",
                "Y",
                "100.00",
                "500.00",
                "200.00",
                "2020-01-01",
                "2025-01-01",
                "2020-06-01",
                "10.00",
                "5.00");

        invokePrivate("validateImportRecord", ctx);

        assertThat(ctx.f.getWsValidSw()).isEqualTo("Y");
        assertThat(ctx.f.getWsNId()).isEqualTo(12345L);
        assertThat(ctx.f.getWsNBal()).isEqualByComparingTo("100.00");
        assertThat(ctx.f.getWsNCrlim()).isEqualByComparingTo("500.00");
        assertThat(ctx.f.getWsNCslim()).isEqualByComparingTo("200.00");
        assertThat(ctx.f.getWsNCycr()).isEqualByComparingTo("10.00");
        assertThat(ctx.f.getWsNCydr()).isEqualByComparingTo("5.00");
    }

    @Test
    void validateImportRecord_tooFewFields_rejectsWithReason() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setParsedFields(
                ctx,
                12,
                "00000012345",
                "Y",
                "100.00",
                "500.00",
                "200.00",
                "2020-01-01",
                "2025-01-01",
                "2020-06-01",
                "10.00",
                "5.00");

        invokePrivate("validateImportRecord", ctx);

        assertThat(ctx.f.getWsValidSw()).isEqualTo("N");
        assertThat(ctx.f.getWsRejReason().trim()).isEqualTo("TOO FEW FIELDS");
    }

    @Test
    void validateImportRecord_nonNumericAccountId_rejects() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setParsedFields(
                ctx,
                13,
                "ABCDEFGHIJK",
                "Y",
                "100.00",
                "500.00",
                "200.00",
                "2020-01-01",
                "2025-01-01",
                "2020-06-01",
                "10.00",
                "5.00");

        invokePrivate("validateImportRecord", ctx);

        assertThat(ctx.f.getWsValidSw()).isEqualTo("N");
        assertThat(ctx.f.getWsRejReason().trim()).isEqualTo("INVALID ACCOUNT ID");
    }

    @Test
    void validateImportRecord_zeroAccountId_rejects() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setParsedFields(
                ctx,
                13,
                "00000000000",
                "Y",
                "100.00",
                "500.00",
                "200.00",
                "2020-01-01",
                "2025-01-01",
                "2020-06-01",
                "10.00",
                "5.00");

        invokePrivate("validateImportRecord", ctx);

        assertThat(ctx.f.getWsValidSw()).isEqualTo("N");
        assertThat(ctx.f.getWsRejReason().trim()).isEqualTo("ACCOUNT ID IS ZERO");
    }

    @Test
    void validateImportRecord_invalidStatusFlag_rejects() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setParsedFields(
                ctx,
                13,
                "00000012345",
                "X",
                "100.00",
                "500.00",
                "200.00",
                "2020-01-01",
                "2025-01-01",
                "2020-06-01",
                "10.00",
                "5.00");

        invokePrivate("validateImportRecord", ctx);

        assertThat(ctx.f.getWsValidSw()).isEqualTo("N");
        assertThat(ctx.f.getWsRejReason().trim()).isEqualTo("STATUS NOT Y OR N");
    }

    @Test
    void validateImportRecord_invalidBalance_rejects() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setParsedFields(
                ctx,
                13,
                "00000012345",
                "Y",
                "NOTNUM",
                "500.00",
                "200.00",
                "2020-01-01",
                "2025-01-01",
                "2020-06-01",
                "10.00",
                "5.00");

        invokePrivate("validateImportRecord", ctx);

        assertThat(ctx.f.getWsValidSw()).isEqualTo("N");
        assertThat(ctx.f.getWsRejReason().trim()).isEqualTo("INVALID BALANCE");
    }

    @Test
    void validateImportRecord_invalidCreditLimit_rejects() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setParsedFields(
                ctx,
                13,
                "00000012345",
                "Y",
                "100.00",
                "NOTNUM",
                "200.00",
                "2020-01-01",
                "2025-01-01",
                "2020-06-01",
                "10.00",
                "5.00");

        invokePrivate("validateImportRecord", ctx);

        assertThat(ctx.f.getWsValidSw()).isEqualTo("N");
        assertThat(ctx.f.getWsRejReason().trim()).isEqualTo("INVALID CREDIT LIMIT");
    }

    @Test
    void validateImportRecord_invalidCashLimit_rejects() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setParsedFields(
                ctx,
                13,
                "00000012345",
                "Y",
                "100.00",
                "500.00",
                "NOTNUM",
                "2020-01-01",
                "2025-01-01",
                "2020-06-01",
                "10.00",
                "5.00");

        invokePrivate("validateImportRecord", ctx);

        assertThat(ctx.f.getWsValidSw()).isEqualTo("N");
        assertThat(ctx.f.getWsRejReason().trim()).isEqualTo("INVALID CASH LIMIT");
    }

    @Test
    void validateImportRecord_invalidCycCredit_rejects() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setParsedFields(
                ctx,
                13,
                "00000012345",
                "Y",
                "100.00",
                "500.00",
                "200.00",
                "2020-01-01",
                "2025-01-01",
                "2020-06-01",
                "NOTNUM",
                "5.00");

        invokePrivate("validateImportRecord", ctx);

        assertThat(ctx.f.getWsValidSw()).isEqualTo("N");
        assertThat(ctx.f.getWsRejReason().trim()).isEqualTo("INVALID CYC CREDIT");
    }

    @Test
    void validateImportRecord_invalidCycDebit_rejects() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setParsedFields(
                ctx,
                13,
                "00000012345",
                "Y",
                "100.00",
                "500.00",
                "200.00",
                "2020-01-01",
                "2025-01-01",
                "2020-06-01",
                "10.00",
                "NOTNUM");

        invokePrivate("validateImportRecord", ctx);

        assertThat(ctx.f.getWsValidSw()).isEqualTo("N");
        assertThat(ctx.f.getWsRejReason().trim()).isEqualTo("INVALID CYC DEBIT");
    }

    @Test
    void validateImportRecord_invalidOpenDate_rejects() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setParsedFields(
                ctx,
                13,
                "00000012345",
                "Y",
                "100.00",
                "500.00",
                "200.00",
                "20200101",
                "2025-01-01",
                "2020-06-01",
                "10.00",
                "5.00");

        invokePrivate("validateImportRecord", ctx);

        assertThat(ctx.f.getWsValidSw()).isEqualTo("N");
        assertThat(ctx.f.getWsRejReason().trim()).isEqualTo("INVALID OPEN DATE");
    }

    @Test
    void validateImportRecord_invalidExpiryDate_rejects() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setParsedFields(
                ctx,
                13,
                "00000012345",
                "Y",
                "100.00",
                "500.00",
                "200.00",
                "2020-01-01",
                "2025/01/01",
                "2020-06-01",
                "10.00",
                "5.00");

        invokePrivate("validateImportRecord", ctx);

        assertThat(ctx.f.getWsValidSw()).isEqualTo("N");
        assertThat(ctx.f.getWsRejReason().trim()).isEqualTo("INVALID EXPIRY DATE");
    }

    @Test
    void validateImportRecord_invalidReissueDate_rejects() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setParsedFields(
                ctx,
                13,
                "00000012345",
                "Y",
                "100.00",
                "500.00",
                "200.00",
                "2020-01-01",
                "2025-01-01",
                "2020-06-XX",
                "10.00",
                "5.00");

        invokePrivate("validateImportRecord", ctx);

        assertThat(ctx.f.getWsValidSw()).isEqualTo("N");
        assertThat(ctx.f.getWsRejReason().trim()).isEqualTo("INVALID REISSUE DATE");
    }

    // =================================================================
    // 3450-CHK-DATE, invoked directly for shape-check branch coverage.
    // =================================================================

    @Test
    void validateDateFormat_validShape_setsOk() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        ctx.f.setWsDtIn("2020-01-01");

        invokePrivate("validateDateFormat", ctx);

        assertThat(ctx.f.getWsDtOk()).isEqualTo("Y");
    }

    @Test
    void validateDateFormat_dashesWrongPosition_rejectsFormat() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        ctx.f.setWsDtIn("2020/01-01");

        invokePrivate("validateDateFormat", ctx);

        assertThat(ctx.f.getWsDtOk()).isEqualTo("N");
    }

    @Test
    void validateDateFormat_dashesPresentButNonNumericSegments_rejectsFormat() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        ctx.f.setWsDtIn("20XY-01-01");

        invokePrivate("validateDateFormat", ctx);

        assertThat(ctx.f.getWsDtOk()).isEqualTo("N");
    }

    // =================================================================
    // 3600-APPLY-ACCT / 3610-BUILD-REC / 3700-REJECT, invoked directly with the
    // parsed WS-N-*/WS-STATUS-CHK/WS-F-* fields set by hand (bypassing the
    // broken 3300-PARSE-LINE and reusing the still-correct 3400 outputs shape).
    // =================================================================

    private void setValidatedAccountFields(OuimpService.TaskContext ctx) {
        ctx.f.setWsNId(123456789L);
        ctx.f.setWsStatusChk("Y");
        ctx.f.setWsNBal(new BigDecimal("100.00"));
        ctx.f.setWsNCrlim(new BigDecimal("500.00"));
        ctx.f.setWsNCslim(new BigDecimal("200.00"));
        ctx.f.setWsNCycr(new BigDecimal("10.00"));
        ctx.f.setWsNCydr(new BigDecimal("5.00"));
        ctx.f.setWsFOpen("2020-01-01");
        ctx.f.setWsFExpiry("2025-01-01");
        ctx.f.setWsFReiss("2022-01-01");
        ctx.f.setWsFZip("90210");
        ctx.f.setWsFGroup("GRPA");
    }

    @Test
    void applyAccountUpdate_existingAccount_rewritesAndCountsUpdated() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setValidatedAccountFields(ctx);
        doAnswer(
                        inv -> {
                            eibresp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            eibresp.set(0);
                            return null;
                        })
                .when(appService)
                .rewriteFile(anyString(), any());

        invokePrivate("applyAccountUpdate", ctx);

        assertThat(ctx.f.getKimUpdated()).isEqualTo(1);
        assertThat(ctx.f.getKimAccepted()).isEqualTo(1);
        assertThat(ctx.f.getKimRejected()).isEqualTo(0);
        assertThat(ctx.f.getAcActiveStatus()).isEqualTo("Y");
        assertThat(ctx.f.getAcCurrBal()).isEqualByComparingTo("100.00");
        assertThat(ctx.f.getAcOpenDate().trim()).isEqualTo("2020-01-01");
        assertThat(ctx.f.getAcAddrZip().trim()).isEqualTo("90210");
        verify(appService).rewriteFile(anyString(), any());
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void applyAccountUpdate_existingAccount_rewriteFails_rejectsWithReason() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setValidatedAccountFields(ctx);
        doAnswer(
                        inv -> {
                            eibresp.set(0);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            eibresp.set(88);
                            return null;
                        })
                .when(appService)
                .rewriteFile(anyString(), any());

        invokePrivate("applyAccountUpdate", ctx);

        assertThat(ctx.f.getKimUpdated()).isEqualTo(0);
        assertThat(ctx.f.getKimRejected()).isEqualTo(1);
        assertThat(ctx.f.getKimLastReason().trim()).isEqualTo("REWRITE FAILED");
    }

    @Test
    void applyAccountUpdate_newAccount_writesAndCountsAdded() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setValidatedAccountFields(ctx);
        doAnswer(
                        inv -> {
                            eibresp.set(13);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            eibresp.set(0);
                            return null;
                        })
                .when(appService)
                .writeFile(anyString(), any(), anyString(), anyInt());

        invokePrivate("applyAccountUpdate", ctx);

        assertThat(ctx.f.getKimAdded()).isEqualTo(1);
        assertThat(ctx.f.getKimAccepted()).isEqualTo(1);
        assertThat(ctx.f.getKimRejected()).isEqualTo(0);
        verify(appService).writeFile(anyString(), any(), anyString(), anyInt());
        verify(appService, never()).rewriteFile(anyString(), any());
    }

    @Test
    void applyAccountUpdate_newAccount_writeFails_rejectsWithReason() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setValidatedAccountFields(ctx);
        doAnswer(
                        inv -> {
                            eibresp.set(13);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());
        doAnswer(
                        inv -> {
                            eibresp.set(88);
                            return null;
                        })
                .when(appService)
                .writeFile(anyString(), any(), anyString(), anyInt());

        invokePrivate("applyAccountUpdate", ctx);

        assertThat(ctx.f.getKimAdded()).isEqualTo(0);
        assertThat(ctx.f.getKimRejected()).isEqualTo(1);
        assertThat(ctx.f.getKimLastReason().trim()).isEqualTo("WRITE FAILED");
    }

    @Test
    void applyAccountUpdate_readError_rejectsWithReadErrorReasonAndSkipsWrite() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        setValidatedAccountFields(ctx);
        doAnswer(
                        inv -> {
                            eibresp.set(99);
                            return null;
                        })
                .when(appService)
                .readFileForUpdate(anyString(), any(), anyString(), anyInt());

        invokePrivate("applyAccountUpdate", ctx);

        assertThat(ctx.f.getKimRejected()).isEqualTo(1);
        assertThat(ctx.f.getKimLastReason().trim()).isEqualTo("READ ERROR");
        verify(appService, never()).rewriteFile(anyString(), any());
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void recordRejection_incrementsRejectedAndCapturesLastReasonAndKey() throws Exception {
        OuimpService.TaskContext ctx = newCtx();
        ctx.f.setWsRejReason("SOME REASON");
        ctx.f.setImpKey("KEY00099");

        invokePrivate("recordRejection", ctx);

        assertThat(ctx.f.getKimRejected()).isEqualTo(1);
        assertThat(ctx.f.getKimLastReason().trim()).isEqualTo("SOME REASON");
        assertThat(ctx.f.getKimLastKey().trim()).isEqualTo("KEY00099");
    }
}

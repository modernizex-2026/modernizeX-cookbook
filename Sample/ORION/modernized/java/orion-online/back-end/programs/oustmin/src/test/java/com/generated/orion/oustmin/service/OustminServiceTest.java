package com.generated.orion.oustmin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.generated.orion.oustmin.accessor.OustminFields;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for OustminService (COBOL program OUSTMIN — statement page browse subroutine).
 * Business paragraphs (0000-MAIN..9500-LOG-ERROR) are exercised directly via reflection since they
 * are private and the public entry point (mainLine) is thin CICS-commarea plumbing. Ground truth
 * for every input/expected pair is /cbl/OUSTMIN.cbl.
 */
@ExtendWith(MockitoExtension.class)
class OustminServiceTest {

    private static final String STMTFILE = "STMTFILE";

    @Mock private AppService appService;

    private OustminService service;
    private Class<?> taskContextClass;

    @BeforeEach
    void setUp() throws Exception {
        service = new OustminService();
        taskContextClass =
                Class.forName("com.generated.orion.oustmin.service.OustminService$TaskContext");
    }

    private Object newContext() throws Exception {
        Constructor<?> ctor = taskContextClass.getDeclaredConstructor(AppService.class);
        ctor.setAccessible(true);
        return ctor.newInstance(appService);
    }

    private OustminFields fieldsOf(Object ctx) throws Exception {
        Field f = taskContextClass.getDeclaredField("f");
        f.setAccessible(true);
        return (OustminFields) f.get(ctx);
    }

    private void call(String methodName, Object ctx) throws Exception {
        Method m = OustminService.class.getDeclaredMethod(methodName, taskContextClass);
        m.setAccessible(true);
        m.invoke(service, ctx);
    }

    private void callReportError(Object ctx, String diagCtx) throws Exception {
        Method m =
                OustminService.class.getDeclaredMethod(
                        "reportStmtfileError", taskContextClass, String.class);
        m.setAccessible(true);
        m.invoke(service, ctx, diagCtx);
    }

    // ── getProgramName / getButtonDefs / registerFsetFields ────────────────

    @Test
    void getProgramName_returnsOustmin() {
        assertThat(service.getProgramName()).isEqualTo("OUSTMIN");
    }

    @Test
    void getButtonDefs_delegatesToMetadata_returnsEmptyList() {
        assertThat(service.getButtonDefs()).isEmpty();
    }

    @Test
    void registerFsetFields_delegatesToMetadata_doesNotThrow() {
        service.registerFsetFields(null);
    }

    // ── 1200-VALIDATE-REQ ────────────────────────────────────────────────

    @Test
    void validateRequestMode_maxRowsBelowRange_clampsToSix() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setKsbMaxRows(0);
        f.setKsbMode("ACCT");

        call("validateRequestMode", ctx);

        assertThat(f.getWsMaxRows()).isEqualTo(6);
    }

    @Test
    void validateRequestMode_maxRowsAboveRange_clampsToSix() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setKsbMaxRows(7);
        f.setKsbMode("ACCT");

        call("validateRequestMode", ctx);

        assertThat(f.getWsMaxRows()).isEqualTo(6);
    }

    @Test
    void validateRequestMode_maxRowsWithinRange_keepsValue() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setKsbMaxRows(3);
        f.setKsbMode("ACCT");

        call("validateRequestMode", ctx);

        assertThat(f.getWsMaxRows()).isEqualTo(3);
    }

    @Test
    void validateRequestMode_modeNotAcct_setsStatus99AndLogsDiagnostic() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setKsbMaxRows(6);
        f.setKsbMode("XXXX");
        when(appService.getEibresp()).thenReturn(0);

        call("validateRequestMode", ctx);

        assertThat(f.getKsbStatus()).isEqualTo("99");
        assertThat(f.getWsDiagCtx().trim()).isEqualTo("BAD REQUEST MODE");
        verify(appService).writeQueueTd(eq("CSSL"), eq(f), eq(80));
    }

    @Test
    void validateRequestMode_modeAcct_statusUnchanged() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setKsbMaxRows(6);
        f.setKsbMode("ACCT");
        f.setKsbStatus("00");

        call("validateRequestMode", ctx);

        assertThat(f.getKsbStatus()).isEqualTo("00");
        verify(appService, never())
                .writeQueueTd(anyString(), any(), org.mockito.ArgumentMatchers.anyInt());
    }

    // ── 1100-CLEAR-ROWS ─────────────────────────────────────────────────

    @Test
    void clearResultRows_resetsAllSixRowsToZero() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        for (int i = 1; i <= 6; i++) {
            f.setKsbRAcct(i, 999L);
            f.setKsbRCycle(i, 999);
            f.setKsbROpen(i, BigDecimal.valueOf(50));
            f.setKsbRDuedt(i, "2026-01-01");
        }

        call("clearResultRows", ctx);

        for (int i = 1; i <= 6; i++) {
            assertThat(f.getKsbRAcct(i)).isZero();
            assertThat(f.getKsbRCycle(i)).isZero();
            assertThat(f.getKsbROpen(i)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(f.getKsbRDuedt(i).trim()).isEmpty();
        }
    }

    // ── 1000-INIT ───────────────────────────────────────────────────────

    @Test
    void initializeRequest_resetsWorkFieldsAndValidates() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setKsbMode("ACCT");
        f.setKsbMaxRows(6);

        call("initializeRequest", ctx);

        assertThat(f.getWsBrowseSw()).isEqualTo("N");
        assertThat(f.getWsEofSw()).isEqualTo("N");
        assertThat(f.getWsRowCnt()).isZero();
        assertThat(f.getKsbStatus()).isEqualTo("00");
        assertThat(f.getKsbRowCnt()).isZero();
        assertThat(f.getKsbNextAcct()).isZero();
        assertThat(f.getKsbNextCycle()).isZero();
        assertThat(f.getKsbMore()).isEqualTo("N");
    }

    // ── 2000-POSITION ───────────────────────────────────────────────────

    @Test
    void positionAccountBrowse_normalResponse_setsBrowseOn() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setKsbStartAcct(5000L);
        f.setKsbStartCycle(202405);
        when(appService.getEibresp()).thenReturn(0);

        call("positionAccountBrowse", ctx);

        verify(appService).startBrowse(eq(STMTFILE), eq("5000|202405"), eq(0));
        assertThat(f.getWsBrowseSw()).isEqualTo("Y");
        assertThat(f.getStAcctId()).isEqualTo(5000L);
        assertThat(f.getStCycle()).isEqualTo(202405);
    }

    @Test
    void positionAccountBrowse_notFoundResponse_setsBrowseOffAndEof() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        when(appService.getEibresp()).thenReturn(13);

        call("positionAccountBrowse", ctx);

        assertThat(f.getWsBrowseSw()).isEqualTo("N");
        assertThat(f.getWsEofSw()).isEqualTo("Y");
        assertThat(f.getKsbStatus()).isNotEqualTo("99");
    }

    @Test
    void positionAccountBrowse_endfileResponse_setsBrowseOffAndEof() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        when(appService.getEibresp()).thenReturn(20);

        call("positionAccountBrowse", ctx);

        assertThat(f.getWsBrowseSw()).isEqualTo("N");
        assertThat(f.getWsEofSw()).isEqualTo("Y");
        assertThat(f.getKsbStatus()).isNotEqualTo("99");
    }

    @Test
    void positionAccountBrowse_otherResponse_reportsErrorAndStatus99() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        when(appService.getEibresp()).thenReturn(1);

        call("positionAccountBrowse", ctx);

        assertThat(f.getWsBrowseSw()).isEqualTo("N");
        assertThat(f.getWsEofSw()).isEqualTo("Y");
        assertThat(f.getKsbStatus()).isEqualTo("99");
        assertThat(f.getWsDiagCtx().trim()).isEqualTo("STARTBR STMTFILE");
        verify(appService).writeQueueTd(eq("CSSL"), eq(f), eq(80));
    }

    // ── 3100-READ-ONE / 3200-MOVE-ROW ───────────────────────────────────

    @Test
    void readNextStatementRow_normalResponse_incrementsRowCountAndPopulatesRow() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setWsRowCnt(0);
        doAnswer(
                        inv -> {
                            OustminFields target = (OustminFields) inv.getArgument(1);
                            target.setStAcctId(1001L);
                            target.setStCycle(202401);
                            target.setStOpenBal(new BigDecimal("100.00"));
                            target.setStCloseBal(new BigDecimal("200.00"));
                            target.setStTotalCredit(new BigDecimal("10.00"));
                            target.setStTotalDebit(new BigDecimal("20.00"));
                            target.setStMinDue(new BigDecimal("5.00"));
                            target.setStDueDate("2026-02-01");
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
        when(appService.getEibresp()).thenReturn(0);

        call("readNextStatementRow", ctx);

        assertThat(f.getWsRowCnt()).isEqualTo(1);
        assertThat(f.getKsbRAcct(1)).isEqualTo(1001L);
        assertThat(f.getKsbRCycle(1)).isEqualTo(202401);
        assertThat(f.getKsbROpen(1)).isEqualByComparingTo("100.00");
        assertThat(f.getKsbRClose(1)).isEqualByComparingTo("200.00");
        assertThat(f.getKsbRCredit(1)).isEqualByComparingTo("10.00");
        assertThat(f.getKsbRDebit(1)).isEqualByComparingTo("20.00");
        assertThat(f.getKsbRMindue(1)).isEqualByComparingTo("5.00");
        assertThat(f.getKsbRDuedt(1).trim()).isEqualTo("2026-02-01");
    }

    @Test
    void readNextStatementRow_endfileResponse_setsEofNoRowAdded() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setWsRowCnt(0);
        when(appService.getEibresp()).thenReturn(20);

        call("readNextStatementRow", ctx);

        assertThat(f.getWsRowCnt()).isZero();
        assertThat(f.getWsEofSw()).isEqualTo("Y");
        assertThat(f.getKsbStatus()).isNotEqualTo("99");
    }

    @Test
    void readNextStatementRow_otherResponse_setsEofAndReportsError() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setWsRowCnt(0);
        when(appService.getEibresp()).thenReturn(1);

        call("readNextStatementRow", ctx);

        assertThat(f.getWsRowCnt()).isZero();
        assertThat(f.getWsEofSw()).isEqualTo("Y");
        assertThat(f.getKsbStatus()).isEqualTo("99");
        assertThat(f.getWsDiagCtx().trim()).isEqualTo("READNEXT STMTFILE");
        verify(appService).writeQueueTd(eq("CSSL"), eq(f), eq(80));
    }

    // ── 3000-READ-PAGE ──────────────────────────────────────────────────

    @Test
    @Timeout(10)
    void readResultPage_loopsUntilMaxRowsReached_readsExactCount() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setWsMaxRows(2);
        f.setWsRowCnt(0);
        f.setString("WS-EOF-SW", "N");
        when(appService.getEibresp()).thenReturn(0, 0);

        call("readResultPage", ctx);

        verify(appService, times(2)).readNext(eq(STMTFILE), any());
        assertThat(f.getWsRowCnt()).isEqualTo(2);
    }

    @Test
    @Timeout(10)
    void readResultPage_stopsEarlyOnEof_readsFewerThanMax() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setWsMaxRows(6);
        f.setWsRowCnt(0);
        f.setString("WS-EOF-SW", "N");
        when(appService.getEibresp()).thenReturn(0, 0, 20);

        call("readResultPage", ctx);

        verify(appService, times(3)).readNext(eq(STMTFILE), any());
        assertThat(f.getWsRowCnt()).isEqualTo(2);
        assertThat(f.getWsEofSw()).isEqualTo("Y");
    }

    // ── 4000-PEEK-NEXT ──────────────────────────────────────────────────

    @Test
    void peekNextStatementRow_eofAlreadyTrue_setsMoreNoWithoutReadingAgain() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setString("WS-EOF-SW", "Y");

        call("peekNextStatementRow", ctx);

        assertThat(f.getKsbMore()).isEqualTo("N");
        verify(appService, never()).readNext(anyString(), any());
    }

    @Test
    void peekNextStatementRow_normalResponse_setsMoreYesWithNextKey() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setString("WS-EOF-SW", "N");
        doAnswer(
                        inv -> {
                            OustminFields target = (OustminFields) inv.getArgument(1);
                            target.setStAcctId(2002L);
                            target.setStCycle(202409);
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
        when(appService.getEibresp()).thenReturn(0);

        call("peekNextStatementRow", ctx);

        assertThat(f.getKsbMore()).isEqualTo("Y");
        assertThat(f.getKsbNextAcct()).isEqualTo(2002L);
        assertThat(f.getKsbNextCycle()).isEqualTo(202409);
    }

    @Test
    void peekNextStatementRow_endfileResponse_setsMoreNo() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setString("WS-EOF-SW", "N");
        when(appService.getEibresp()).thenReturn(20);

        call("peekNextStatementRow", ctx);

        assertThat(f.getKsbMore()).isEqualTo("N");
        assertThat(f.getKsbStatus()).isNotEqualTo("99");
    }

    @Test
    void peekNextStatementRow_otherResponse_setsMoreNoAndReportsError() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setString("WS-EOF-SW", "N");
        when(appService.getEibresp()).thenReturn(1);

        call("peekNextStatementRow", ctx);

        assertThat(f.getKsbMore()).isEqualTo("N");
        assertThat(f.getKsbStatus()).isEqualTo("99");
        assertThat(f.getWsDiagCtx().trim()).isEqualTo("PEEK STMTFILE");
        verify(appService).writeQueueTd(eq("CSSL"), eq(f), eq(80));
    }

    // ── 5000-END-BROWSE ─────────────────────────────────────────────────

    @Test
    void endStatementBrowse_normalResponse_noDiagnosticWritten() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        when(appService.getEibresp()).thenReturn(0);

        call("endStatementBrowse", ctx);

        verify(appService).endBrowse(STMTFILE);
        verify(appService, never())
                .writeQueueTd(anyString(), any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void endStatementBrowse_errorResponse_writesDiagnosticButKeepsStatus() throws Exception {
        // CONVERT-GAP CHECK: COBOL 5000-END-BROWSE logs on abnormal RESP but never sets
        // KSB-STATUS to '99' for an ENDBR failure — status is left as previously computed.
        // Java mirrors this exactly (no gap here), so this test locks in that COBOL behavior.
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setKsbStatus("00");
        when(appService.getEibresp()).thenReturn(9);

        call("endStatementBrowse", ctx);

        assertThat(f.getKsbStatus()).isEqualTo("00");
        assertThat(f.getWsDiagCtx().trim()).isEqualTo("ENDBR STMTFILE");
        verify(appService).writeQueueTd(eq("CSSL"), eq(f), eq(80));
    }

    // ── 6000-SET-STATUS ─────────────────────────────────────────────────

    @Test
    void finalizeResponseStatus_statusAlreadyError_preservesStatusAndSetsRowCount()
            throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setKsbStatus("99");
        f.setWsRowCnt(3);

        call("finalizeResponseStatus", ctx);

        assertThat(f.getKsbStatus()).isEqualTo("99");
        assertThat(f.getKsbRowCnt()).isEqualTo(3);
    }

    @Test
    void finalizeResponseStatus_zeroRows_setsStatus10() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setKsbStatus("00");
        f.setWsRowCnt(0);

        call("finalizeResponseStatus", ctx);

        assertThat(f.getKsbStatus()).isEqualTo("10");
        assertThat(f.getKsbRowCnt()).isZero();
    }

    @Test
    void finalizeResponseStatus_rowsPresent_setsStatus00() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setKsbStatus("00");
        f.setWsRowCnt(4);

        call("finalizeResponseStatus", ctx);

        assertThat(f.getKsbStatus()).isEqualTo("00");
        assertThat(f.getKsbRowCnt()).isEqualTo(4);
    }

    // ── 9500-LOG-ERROR / reportStmtfileError ────────────────────────────

    @Test
    void writeDiagnosticQueue_buildsDiagnosticLineAndWritesToQueue() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setWsDiagCtx("SOME CONTEXT");
        f.setWsDiagResp(42);
        when(appService.getEibresp()).thenReturn(0);

        call("writeDiagnosticQueue", ctx);

        assertThat(f.getWdlCtx().trim()).isEqualTo("SOME CONTEXT");
        assertThat(f.getWdlResp()).isEqualTo(42);
        assertThat(f.getWsDiagMsg())
                .contains("OUSTMIN : ")
                .contains("SOME CONTEXT")
                .contains("RESP=");
        verify(appService).writeQueueTd(eq("CSSL"), eq(f), eq(80));
    }

    @Test
    void reportStmtfileError_setsStatus99AndDelegatesToLog() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setWsRespCd(55);
        when(appService.getEibresp()).thenReturn(0);

        callReportError(ctx, "CUSTOM CTX");

        assertThat(f.getKsbStatus()).isEqualTo("99");
        assertThat(f.getWsDiagCtx().trim()).isEqualTo("CUSTOM CTX");
        assertThat(f.getWsDiagResp()).isEqualTo(55);
        verify(appService).writeQueueTd(eq("CSSL"), eq(f), eq(80));
    }

    // ── 0000-MAIN full flow ─────────────────────────────────────────────

    @Test
    @Timeout(10)
    void runMainProgram_happyPath_twoRowsWithMorePage() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setKsbMode("ACCT");
        f.setKsbMaxRows(2);
        f.setKsbStartAcct(7000L);
        f.setKsbStartCycle(202401);

        AtomicInteger readCount = new AtomicInteger(0);
        doAnswer(
                        inv -> {
                            OustminFields target = (OustminFields) inv.getArgument(1);
                            int n = readCount.incrementAndGet();
                            if (n == 1) {
                                target.setStAcctId(7000L);
                                target.setStCycle(202401);
                            } else if (n == 2) {
                                target.setStAcctId(7000L);
                                target.setStCycle(202402);
                            } else {
                                target.setStAcctId(7000L);
                                target.setStCycle(202403);
                            }
                            return null;
                        })
                .when(appService)
                .readNext(anyString(), any());
        // order: startBrowse, read#1, read#2, peek-read#3, endBrowse
        when(appService.getEibresp()).thenReturn(0, 0, 0, 0, 0);

        call("runMainProgram", ctx);

        verify(appService).startBrowse(eq(STMTFILE), eq("7000|202401"), eq(0));
        verify(appService, times(3)).readNext(eq(STMTFILE), any());
        verify(appService).endBrowse(STMTFILE);
        assertThat(f.getKsbStatus()).isEqualTo("00");
        assertThat(f.getKsbRowCnt()).isEqualTo(2);
        assertThat(f.getKsbRCycle(1)).isEqualTo(202401);
        assertThat(f.getKsbRCycle(2)).isEqualTo(202402);
        assertThat(f.getKsbMore()).isEqualTo("Y");
        assertThat(f.getKsbNextCycle()).isEqualTo(202403);
    }

    @Test
    void runMainProgram_badRequestMode_skipsPositionAndSetsStatus99() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        // KSB-MODE deliberately left unset (blank) to mirror a caller forgetting to set it.
        when(appService.getEibresp()).thenReturn(0);

        call("runMainProgram", ctx);

        assertThat(f.getKsbStatus()).isEqualTo("99");
        assertThat(f.getKsbRowCnt()).isZero();
        verify(appService, never())
                .startBrowse(anyString(), anyString(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void runMainProgram_startBrowseNotFound_emptyPageStatus10() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setKsbMode("ACCT");
        f.setKsbMaxRows(6);
        when(appService.getEibresp()).thenReturn(13);

        call("runMainProgram", ctx);

        assertThat(f.getKsbStatus()).isEqualTo("10");
        assertThat(f.getKsbRowCnt()).isZero();
        verify(appService, never()).readNext(anyString(), any());
        verify(appService, never()).endBrowse(anyString());
    }

    @Test
    void runMainProgram_startBrowseError_statusRemains99AfterFinalize() throws Exception {
        Object ctx = newContext();
        OustminFields f = fieldsOf(ctx);
        f.setKsbMode("ACCT");
        f.setKsbMaxRows(6);
        when(appService.getEibresp()).thenReturn(2);

        call("runMainProgram", ctx);

        assertThat(f.getKsbStatus()).isEqualTo("99");
        assertThat(f.getWsDiagCtx().trim()).isEqualTo("STARTBR STMTFILE");
        verify(appService, never()).readNext(anyString(), any());
    }

    // ── mainLine commarea plumbing ──────────────────────────────────────

    @Test
    void mainLine_eibcalenZero_completesWithoutTouchingCommarea() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        verify(appService, never()).getCommarea();
    }

    @Test
    void mainLine_byteArrayCommarea_roundTripsBufferBackToCommarea() {
        byte[] commarea = new byte[567];
        when(appService.getEibcalen()).thenReturn(567);
        when(appService.getCommarea()).thenReturn(commarea);

        service.mainLine(appService);

        verify(appService).setCommarea(any(byte[].class));
    }

    @Test
    void mainLine_objectArrayWithByteElement_writesBackByteArrayIntoSlot() {
        Object[] commarea = new Object[] {new byte[567]};
        when(appService.getEibcalen()).thenReturn(567);
        when(appService.getCommarea()).thenReturn(commarea);

        service.mainLine(appService);

        assertThat(commarea[0]).isInstanceOf(byte[].class);
    }

    @Test
    void mainLine_objectArrayWithNonByteElement_usesStringGroupPathAndWritesBack() {
        Object[] commarea = new Object[] {"ACCT"};
        when(appService.getEibcalen()).thenReturn(4);
        when(appService.getCommarea()).thenReturn(commarea);

        service.mainLine(appService);

        assertThat(commarea[0]).isInstanceOf(String.class);
    }

    @Test
    void mainLine_genericObjectCommarea_usesStringGroupPathAndSetsCommarea() {
        Object commarea = "ACCT";
        when(appService.getEibcalen()).thenReturn(4);
        when(appService.getCommarea()).thenReturn(commarea);

        service.mainLine(appService);

        verify(appService).setCommarea(any(byte[].class));
    }
}

package com.generated.orion.oustmin.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.oustmin.accessor.OustminFields;
import com.generated.orion.oustmin.metadata.OustminBmsMetadata;
import com.generated.orion.oustmin.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUSTMIN. */
@Service
public class OustminService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OustminService.class);

    private static final String KSB_STATUS_ERROR = "99";

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        if (ctx.appService.getEibcalen() > 0 && ctx.appService.getCommarea() != null) {
            Object _commarea = ctx.appService.getCommarea();
            if (_commarea instanceof Object[]) {
                Object[] _params = (Object[]) _commarea;
                if (_params.length > 0 && _params[0] instanceof byte[]) {
                    ctx.f.writeBytes("KSTMB-PARM", (byte[]) _params[0]);
                } else if (_params.length > 0 && _params[0] != null) {
                    ctx.f.setGroup("KSTMB-PARM", String.valueOf(_params[0]));
                }
            } else if (_commarea instanceof byte[]) {
                ctx.f.writeBytes("KSTMB-PARM", (byte[]) _commarea);
            } else {
                ctx.f.setGroup("KSTMB-PARM", String.valueOf(_commarea));
            }
        }
        try {
            runMainProgram(ctx);
        } finally {
            if (ctx.appService.getEibcalen() > 0) {
                if (ctx.appService.getCommarea() instanceof Object[]) {
                    Object[] _params = (Object[]) ctx.appService.getCommarea();
                    if (_params.length > 0) {
                        _params[0] =
                                _params[0] instanceof byte[]
                                        ? (Object) ctx.f.sliceBytes("KSTMB-PARM")
                                        : (Object) ctx.f.groupToString("KSTMB-PARM");
                    }
                } else {
                    ctx.appService.setCommarea(ctx.f.sliceBytes("KSTMB-PARM"));
                }
            }
        }
    }

    @Override
    public String getProgramName() {
        return "OUSTMIN";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OustminBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OustminBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        initializeRequest(ctx);
        if (!Utility.fieldEquals(ctx.f.getKsbStatus(), KSB_STATUS_ERROR)) {
            positionAccountBrowse(ctx);
            if (ctx.f.getWsBrowseSw().equals("Y")) {
                readResultPage(ctx);
                peekNextStatementRow(ctx);
                endStatementBrowse(ctx);
            }
        }
        finalizeResponseStatus(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INIT */
    private void initializeRequest(TaskContext ctx) {
        ctx.f.setString("WS-BROWSE-SW", "N");
        ctx.f.setString("WS-EOF-SW", "N");
        ctx.f.setWsRowCnt(0);
        ctx.f.setKsbStatus("00");
        ctx.f.setKsbRowCnt(0);
        ctx.f.setKsbNextAcct(0);
        ctx.f.setKsbNextCycle(0);
        ctx.f.setKsbMore("N");
        clearResultRows(ctx);
        validateRequestMode(ctx);
    }

    /** COBOL paragraph: 1100-CLEAR-ROWS */
    private void clearResultRows(TaskContext ctx) {
        for (ctx.f.setWsIdx(1); ctx.f.getWsIdx() <= 6; ctx.f.setWsIdx(ctx.f.getWsIdx() + 1)) {
            ctx.f.setKsbRAcct(ctx.f.getWsIdx(), 0);
            ctx.f.setKsbRCycle(ctx.f.getWsIdx(), 0);
            ctx.f.setKsbROpen(ctx.f.getWsIdx(), java.math.BigDecimal.valueOf(0));
            ctx.f.setKsbRClose(ctx.f.getWsIdx(), java.math.BigDecimal.valueOf(0));
            ctx.f.setKsbRCredit(ctx.f.getWsIdx(), java.math.BigDecimal.valueOf(0));
            ctx.f.setKsbRDebit(ctx.f.getWsIdx(), java.math.BigDecimal.valueOf(0));
            ctx.f.setKsbRMindue(ctx.f.getWsIdx(), java.math.BigDecimal.valueOf(0));
            ctx.f.setKsbRDuedt(ctx.f.getWsIdx(), " ");
        }
    }

    /** COBOL paragraph: 1200-VALIDATE-REQ */
    private void validateRequestMode(TaskContext ctx) {
        ctx.f.setWsMaxRows(ctx.f.getKsbMaxRows());
        if (ctx.f.getWsMaxRows() < 1 || ctx.f.getWsMaxRows() > 6) {
            ctx.f.setWsMaxRows(6);
        }
        if (!Utility.fieldEquals(ctx.f.getKsbMode(), "ACCT")) {
            ctx.f.setKsbStatus(KSB_STATUS_ERROR);
            ctx.f.setWsDiagCtx("BAD REQUEST MODE");
            ctx.f.setWsDiagResp(0);
            writeDiagnosticQueue(ctx);
        }
    }

    /** COBOL paragraph: 2000-POSITION */
    private void positionAccountBrowse(TaskContext ctx) {
        ctx.f.setStAcctId(ctx.f.getKsbStartAcct());
        ctx.f.setStCycle(ctx.f.getKsbStartCycle());
        ctx.appService.startBrowse(
                ctx.f.getWsStmtfile(),
                String.valueOf(ctx.f.getStAcctId()) + "|" + String.valueOf(ctx.f.getStCycle()),
                0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BROWSE-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-BROWSE-SW", "N");
            ctx.f.setString("WS-EOF-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-BROWSE-SW", "N");
            ctx.f.setString("WS-EOF-SW", "Y");
        } else {
            ctx.f.setString("WS-BROWSE-SW", "N");
            ctx.f.setString("WS-EOF-SW", "Y");
            reportStmtfileError(ctx, "STARTBR STMTFILE");
        }
    }

    /** COBOL paragraph: 3000-READ-PAGE */
    private void readResultPage(TaskContext ctx) {
        while (!ctx.f.getWsEofSw().equals("Y") && ctx.f.getWsRowCnt() < ctx.f.getWsMaxRows()) {
            readNextStatementRow(ctx);
        }
    }

    /** COBOL paragraph: 3100-READ-ONE */
    private void readNextStatementRow(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsStmtfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setWsRowCnt(ctx.f.getWsRowCnt() + 1);
            populateResultRow(ctx);
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-EOF-SW", "Y");
        } else {
            ctx.f.setString("WS-EOF-SW", "Y");
            reportStmtfileError(ctx, "READNEXT STMTFILE");
        }
    }

    /** COBOL paragraph: 3200-MOVE-ROW */
    private void populateResultRow(TaskContext ctx) {
        ctx.f.setKsbRAcct(ctx.f.getWsRowCnt(), ctx.f.getStAcctId());
        ctx.f.setKsbRCycle(ctx.f.getWsRowCnt(), ctx.f.getStCycle());
        ctx.f.setKsbROpen(ctx.f.getWsRowCnt(), ctx.f.getStOpenBal());
        ctx.f.setKsbRClose(ctx.f.getWsRowCnt(), ctx.f.getStCloseBal());
        ctx.f.setKsbRCredit(ctx.f.getWsRowCnt(), ctx.f.getStTotalCredit());
        ctx.f.setKsbRDebit(ctx.f.getWsRowCnt(), ctx.f.getStTotalDebit());
        ctx.f.setKsbRMindue(ctx.f.getWsRowCnt(), ctx.f.getStMinDue());
        ctx.f.setKsbRDuedt(ctx.f.getWsRowCnt(), ctx.f.getStDueDate());
    }

    /** COBOL paragraph: 4000-PEEK-NEXT */
    private void peekNextStatementRow(TaskContext ctx) {
        if (ctx.f.getWsEofSw().equals("Y")) {
            ctx.f.setKsbMore("N");
            return;
        }
        ctx.appService.readNext(ctx.f.getWsStmtfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKsbMore("Y");
            ctx.f.setKsbNextAcct(ctx.f.getStAcctId());
            ctx.f.setKsbNextCycle(ctx.f.getStCycle());
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setKsbMore("N");
        } else {
            ctx.f.setKsbMore("N");
            reportStmtfileError(ctx, "PEEK STMTFILE");
        }
    }

    /** COBOL paragraph: 5000-END-BROWSE */
    private void endStatementBrowse(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsStmtfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setWsDiagCtx("ENDBR STMTFILE");
            ctx.f.setWsDiagResp(ctx.f.getWsRespCd());
            writeDiagnosticQueue(ctx);
        }
    }

    /** COBOL paragraph: 6000-SET-STATUS */
    private void finalizeResponseStatus(TaskContext ctx) {
        ctx.f.setKsbRowCnt(ctx.f.getWsRowCnt());
        if (Utility.fieldEquals(ctx.f.getKsbStatus(), KSB_STATUS_ERROR)) {
            return;
        }
        if (ctx.f.getWsRowCnt() == 0) {
            ctx.f.setKsbStatus("10");
        } else {
            ctx.f.setKsbStatus("00");
        }
    }

    /** COBOL paragraph: 9500-LOG-ERROR */
    private void writeDiagnosticQueue(TaskContext ctx) {
        ctx.f.setWdlCtx(ctx.f.getWsDiagCtx());
        ctx.f.setWdlResp(ctx.f.getWsDiagResp());
        ctx.f.setWsDiagMsg(ctx.f.getWsDiagLine());
        ctx.appService.writeQueueTd(ctx.f.getWsDiagQname(), ctx.f, 80);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** Mark the request as failed and log the diagnostic context for a stmtfile I/O error. */
    private void reportStmtfileError(TaskContext ctx, String diagCtx) {
        ctx.f.setKsbStatus(KSB_STATUS_ERROR);
        ctx.f.setWsDiagCtx(diagCtx);
        ctx.f.setWsDiagResp(ctx.f.getWsRespCd());
        writeDiagnosticQueue(ctx);
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OustminFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OustminFields(ws);
        }
    }
}

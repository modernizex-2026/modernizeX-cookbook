package com.generated.orion.oustmb.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.oustmb.accessor.OustmbFields;
import com.generated.orion.oustmb.metadata.OustmbBmsMetadata;
import com.generated.orion.oustmb.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUSTMB. */
@Service
public class OustmbService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OustmbService.class);

    /** CICS response code indicating the prior file operation completed normally. */
    private static final int RESP_NORMAL = 0;

    /** CICS response code indicating end-of-file was reached during a browse/read. */
    private static final int RESP_ENDFILE = 20;

    /** KSM-STATUS sentinel indicating a fatal condition that aborts the main flow. */
    private static final String STATUS_FATAL_ERROR = "99";

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
                    ctx.f.writeBytes("ORION-COMMAREA", (byte[]) _params[0]);
                } else if (_params.length > 0 && _params[0] != null) {
                    ctx.f.setGroup("ORION-COMMAREA", String.valueOf(_params[0]));
                }
            } else if (_commarea instanceof byte[]) {
                ctx.f.writeBytes("ORION-COMMAREA", (byte[]) _commarea);
            } else {
                ctx.f.setGroup("ORION-COMMAREA", String.valueOf(_commarea));
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
                                        ? (Object) ctx.f.sliceBytes("ORION-COMMAREA")
                                        : (Object) ctx.f.groupToString("ORION-COMMAREA");
                    }
                } else {
                    ctx.appService.setCommarea(ctx.f.sliceBytes("ORION-COMMAREA"));
                }
            }
        }
    }

    @Override
    public String getProgramName() {
        return "OUSTMB";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OustmbBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OustmbBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.aliasGroup("KSM-PARM", "CA-WORK-AREA");
        initializeWorkingStorage(ctx);
        if (!Utility.fieldEquals(ctx.f.getKsmStatus(), STATUS_FATAL_ERROR)) {
            positionAccountBrowse(ctx);
            if (ctx.f.getWsBrowseSw().equals("Y")) {
                while (!(ctx.f.getWsEofSw().equals("Y")
                        || Utility.fieldEquals(ctx.f.getWsCapped(), "Y"))) {
                    processAccountLoop(ctx);
                }
                peekNextAccountForMore(ctx);
                endAccountBrowse(ctx);
            }
        }
        setFinalStatus(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INIT */
    private void initializeWorkingStorage(TaskContext ctx) {
        ctx.f.setString("WS-EOF-SW", "N");
        ctx.f.setString("WS-BROWSE-SW", "N");
        ctx.f.setWsCapped("N");
        ctx.f.setKsmStatus("00");
        ctx.f.setKsmMsg(" ");
        ctx.f.setKsmAcctRead(0);
        ctx.f.setKsmStmtWritten(0);
        ctx.f.setKsmNoTran(0);
        ctx.f.setKsmErrors(0);
        ctx.f.setKsmTotCredit(java.math.BigDecimal.valueOf(0));
        ctx.f.setKsmTotDebit(java.math.BigDecimal.valueOf(0));
        ctx.f.setKsmNextAcct(0);
        ctx.f.setKsmMore("N");
        ctx.f.setWsMax(ctx.f.getKsmMax());
        if (ctx.f.getKsmCycle() == 0) {
            ctx.f.setKsmStatus(STATUS_FATAL_ERROR);
            ctx.f.setKsmMsg("STATEMENT CYCLE (YYYYMM) REQUIRED");
        }
    }

    /** COBOL paragraph: 2000-POSITION */
    private void positionAccountBrowse(TaskContext ctx) {
        ctx.f.setAcId(ctx.f.getKsmStartAcct());
        ctx.appService.startBrowse(ctx.f.getWsAcctfile(), String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == RESP_NORMAL) {
            ctx.f.setString("WS-BROWSE-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-EOF-SW", "Y");
        } else if (ctx.f.getWsRespCd() == RESP_ENDFILE) {
            ctx.f.setString("WS-EOF-SW", "Y");
        } else {
            ctx.f.setKsmStatus(STATUS_FATAL_ERROR);
            ctx.f.setKsmMsg("ACCTFILE STARTBR FAILED");
        }
    }

    /** COBOL paragraph: 3000-PROC-LOOP */
    private void processAccountLoop(TaskContext ctx) {
        readNextAccount(ctx);
        if (!ctx.f.getWsEofSw().equals("Y")) {
            processAccountRecord(ctx);
            if (ctx.f.getWsMax() > 0 && ctx.f.getKsmAcctRead() >= ctx.f.getWsMax()) {
                ctx.f.setWsCapped("Y");
            }
        }
    }

    /** COBOL paragraph: 3100-READ-ACCT */
    private void readNextAccount(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsAcctfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == RESP_NORMAL) {
            /* CONTINUE */
        } else if (ctx.f.getWsRespCd() == RESP_ENDFILE) {
            ctx.f.setString("WS-EOF-SW", "Y");
        } else {
            ctx.f.setString("WS-EOF-SW", "Y");
            ctx.f.setKsmErrors(ctx.f.getKsmErrors() + 1);
            ctx.f.setKsmMsg("ACCTFILE READNEXT FAILED");
        }
    }

    /** COBOL paragraph: 3200-PROCESS-ACCT */
    private void processAccountRecord(TaskContext ctx) {
        ctx.f.setKsmAcctRead(ctx.f.getKsmAcctRead() + 1);
        resetAccountTotals(ctx);
        collectCardsForAccount(ctx);
        sumTransactionsForCards(ctx);
        if (ctx.f.getWsTranThis() == 0) {
            ctx.f.setKsmNoTran(ctx.f.getKsmNoTran() + 1);
        }
        computeStatementBalances(ctx);
        writeStatementRecord(ctx);
        ctx.f.setKsmTotCredit(ctx.f.getKsmTotCredit().add(ctx.f.getWsTotCredit()));
        ctx.f.setKsmTotDebit(ctx.f.getKsmTotDebit().add(ctx.f.getWsTotDebit()));
    }

    /** COBOL paragraph: 3210-RESET */
    private void resetAccountTotals(TaskContext ctx) {
        ctx.f.setWsCardCnt(0);
        ctx.f.setWsTotCredit(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsTotDebit(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsOpenBal(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsCloseBal(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsMinDue(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsTranThis(0);
    }

    /** COBOL paragraph: 3220-COLLECT-CARDS */
    private void collectCardsForAccount(TaskContext ctx) {
        ctx.f.setWsXrefEof("N");
        ctx.f.setXrAcctId(ctx.f.getAcId());
        ctx.appService.startBrowse(ctx.f.getWsXrefPath(), String.valueOf(ctx.f.getXrAcctId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != RESP_NORMAL) {
            ctx.f.setString("WS-XREF-EOF", "Y");
            return;
        }
        readNextCardXref(ctx);
        while (!ctx.f.getWsXrefEof().equals("Y")) {
            if (ctx.f.getXrAcctId() == ctx.f.getAcId()) {
                if (ctx.f.getWsCardCnt() < ctx.f.getWsMaxCards()) {
                    ctx.f.setWsCardCnt(ctx.f.getWsCardCnt() + 1);
                    ctx.f.setWsCardNum(ctx.f.getWsCardCnt(), ctx.f.getXrCardNum());
                }
                readNextCardXref(ctx);
            } else {
                ctx.f.setString("WS-XREF-EOF", "Y");
            }
        }
        ctx.appService.endBrowse(ctx.f.getWsXrefPath());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 3230-READ-XREF-NEXT */
    private void readNextCardXref(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsXrefPath(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == RESP_NORMAL) {
            /* CONTINUE */
        } else if (ctx.f.getWsRespCd() == RESP_ENDFILE) {
            ctx.f.setString("WS-XREF-EOF", "Y");
        } else {
            ctx.f.setString("WS-XREF-EOF", "Y");
            ctx.f.setKsmErrors(ctx.f.getKsmErrors() + 1);
        }
    }

    /** COBOL paragraph: 3300-SUM-TRANS */
    private void sumTransactionsForCards(TaskContext ctx) {
        for (ctx.f.setWsCx(1);
                ctx.f.getWsCx() <= ctx.f.getWsCardCnt();
                ctx.f.setWsCx(ctx.f.getWsCx() + 1)) {
            browseCardTransactions(ctx);
        }
    }

    /** COBOL paragraph: 3310-BROWSE-CARD */
    private void browseCardTransactions(TaskContext ctx) {
        ctx.f.setWsTranEof("N");
        ctx.f.setTrCardNum(ctx.f.getWsCardNum(ctx.f.getWsCx()));
        ctx.appService.startBrowse(ctx.f.getWsTranPath(), String.valueOf(ctx.f.getTrCardNum()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != RESP_NORMAL) {
            ctx.f.setString("WS-TRAN-EOF", "Y");
            return;
        }
        readNextTransaction(ctx);
        while (!ctx.f.getWsTranEof().equals("Y")) {
            if (Utility.fieldEquals(ctx.f.getTrCardNum(), ctx.f.getWsCardNum(ctx.f.getWsCx()))) {
                accumulateTransactionForCycle(ctx);
                readNextTransaction(ctx);
            } else {
                ctx.f.setString("WS-TRAN-EOF", "Y");
            }
        }
        ctx.appService.endBrowse(ctx.f.getWsTranPath());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 3320-READ-TRAN-NEXT */
    private void readNextTransaction(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsTranPath(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == RESP_NORMAL) {
            /* CONTINUE */
        } else if (ctx.f.getWsRespCd() == RESP_ENDFILE) {
            ctx.f.setString("WS-TRAN-EOF", "Y");
        } else {
            ctx.f.setString("WS-TRAN-EOF", "Y");
            ctx.f.setKsmErrors(ctx.f.getKsmErrors() + 1);
        }
    }

    /** COBOL paragraph: 3330-ACCUM */
    private void accumulateTransactionForCycle(TaskContext ctx) {
        ctx.f.setWsTcycYy(
                Utility.parseNumeric(
                                Utility.padRight(String.valueOf(ctx.f.getTrProcTs()), 4)
                                        .substring(0, 4))
                        .intValue());
        ctx.f.setWsTcycMm(
                Utility.parseNumeric(
                                Utility.padRight(String.valueOf(ctx.f.getTrProcTs()), 7)
                                        .substring(5, 7))
                        .intValue());
        if (ctx.f.getWsTcyc() == ctx.f.getKsmCycle()) {
            ctx.f.setWsTranThis(ctx.f.getWsTranThis() + 1);
            if (Utility.fieldEquals(ctx.f.getTrTypeCd(), "PY")
                    || Utility.fieldEquals(ctx.f.getTrTypeCd(), "CR")) {
                ctx.f.setWsTotCredit(ctx.f.getWsTotCredit().add(ctx.f.getTrAmt()));
            } else {
                ctx.f.setWsTotDebit(ctx.f.getWsTotDebit().add(ctx.f.getTrAmt()));
            }
        }
    }

    /** COBOL paragraph: 3400-COMPUTE */
    private void computeStatementBalances(TaskContext ctx) {
        ctx.f.setWsCloseBal(ctx.f.getAcCurrBal());
        ctx.f.setWsOpenBal(
                ctx.f.getWsCloseBal().subtract(ctx.f.getWsTotDebit()).add(ctx.f.getWsTotCredit()));
        if ((ctx.f.getWsCloseBal().signum() <= 0)) {
            ctx.f.setWsMinDue(java.math.BigDecimal.valueOf(0));
        } else {
            ctx.f.setWsMinDue(ctx.f.getWsCloseBal().multiply(new java.math.BigDecimal("0.02")));
            if ((ctx.f.getWsCloseBal().compareTo(new java.math.BigDecimal("25.00")) <= 0)) {
                ctx.f.setWsMinDue(ctx.f.getWsCloseBal());
            } else {
                if ((ctx.f.getWsMinDue().compareTo(new java.math.BigDecimal("25.00")) < 0)) {
                    ctx.f.setWsMinDue(new java.math.BigDecimal("25.00"));
                }
            }
        }
    }

    /** COBOL paragraph: 3500-WRITE-STMT */
    private void writeStatementRecord(TaskContext ctx) {
        ctx.f.setStmtRec(" ");
        ctx.f.setStAcctId(ctx.f.getAcId());
        ctx.f.setStCycle(ctx.f.getKsmCycle());
        ctx.f.setStOpenBal(ctx.f.getWsOpenBal());
        ctx.f.setStCloseBal(ctx.f.getWsCloseBal());
        ctx.f.setStTotalCredit(ctx.f.getWsTotCredit());
        ctx.f.setStTotalDebit(ctx.f.getWsTotDebit());
        ctx.f.setStMinDue(ctx.f.getWsMinDue());
        ctx.f.setStDueDate(ctx.f.getKsmDueDate());
        ctx.appService.writeFile(
                ctx.f.getWsStmtfile(),
                ctx.f,
                String.valueOf(ctx.f.getStAcctId()) + "|" + String.valueOf(ctx.f.getStCycle()),
                0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == RESP_NORMAL) {
            ctx.f.setKsmStmtWritten(ctx.f.getKsmStmtWritten() + 1);
        } else if (ctx.f.getWsRespCd() == 14) {
            rewriteStatementRecord(ctx);
        } else {
            ctx.f.setKsmErrors(ctx.f.getKsmErrors() + 1);
        }
    }

    /** COBOL paragraph: 3510-REWRITE-STMT */
    private void rewriteStatementRecord(TaskContext ctx) {
        ctx.appService.readFileForUpdate(
                ctx.f.getWsStmtfile(),
                ctx.f,
                String.valueOf(ctx.f.getStAcctId()) + "|" + String.valueOf(ctx.f.getStCycle()),
                0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != RESP_NORMAL) {
            ctx.f.setKsmErrors(ctx.f.getKsmErrors() + 1);
            return;
        }
        ctx.f.setStOpenBal(ctx.f.getWsOpenBal());
        ctx.f.setStCloseBal(ctx.f.getWsCloseBal());
        ctx.f.setStTotalCredit(ctx.f.getWsTotCredit());
        ctx.f.setStTotalDebit(ctx.f.getWsTotDebit());
        ctx.f.setStMinDue(ctx.f.getWsMinDue());
        ctx.f.setStDueDate(ctx.f.getKsmDueDate());
        ctx.appService.rewriteFile(ctx.f.getWsStmtfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == RESP_NORMAL) {
            ctx.f.setKsmStmtWritten(ctx.f.getKsmStmtWritten() + 1);
        } else {
            ctx.f.setKsmErrors(ctx.f.getKsmErrors() + 1);
        }
    }

    /** COBOL paragraph: 4000-PEEK-NEXT */
    private void peekNextAccountForMore(TaskContext ctx) {
        if (ctx.f.getWsEofSw().equals("Y")) {
            ctx.f.setKsmMore("N");
            return;
        }
        ctx.appService.readNext(ctx.f.getWsAcctfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == RESP_NORMAL) {
            ctx.f.setKsmMore("Y");
            ctx.f.setKsmNextAcct(ctx.f.getAcId());
        } else if (ctx.f.getWsRespCd() == RESP_ENDFILE) {
            ctx.f.setKsmMore("N");
        } else {
            ctx.f.setKsmMore("N");
            ctx.f.setKsmErrors(ctx.f.getKsmErrors() + 1);
        }
    }

    /** COBOL paragraph: 5000-END-BROWSE */
    private void endAccountBrowse(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsAcctfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 6000-SET-STATUS */
    private void setFinalStatus(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getKsmStatus(), STATUS_FATAL_ERROR)) {
            return;
        }
        if (ctx.f.getKsmAcctRead() == 0) {
            ctx.f.setKsmStatus("10");
            ctx.f.setKsmMsg("NO ACCOUNTS PROCESSED");
        } else {
            ctx.f.setKsmStatus("00");
            ctx.f.setKsmMsg("STATEMENT BUILD COMPLETE");
        }
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OustmbFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OustmbFields(ws);
        }
    }
}

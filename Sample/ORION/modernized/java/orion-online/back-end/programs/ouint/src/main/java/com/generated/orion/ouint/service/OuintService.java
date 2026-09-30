package com.generated.orion.ouint.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.ouint.accessor.OuintFields;
import com.generated.orion.ouint.metadata.OuintBmsMetadata;
import com.generated.orion.ouint.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUINT. */
@Service
public class OuintService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OuintService.class);

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
        return "OUINT";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OuintBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OuintBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.aliasGroup("KOPS-AREA", "CA-WORK-AREA");
        initializeProgram(ctx);
        runBrowseDriver(ctx);
        finalizeProgram(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INITIALISE */
    private void initializeProgram(TaskContext ctx) {
        ctx.f.setString("KO-STATUS", "O");
        ctx.f.setKoStatusMsg(" ");
        ctx.f.setKoReadCnt(0);
        ctx.f.setKoSelectCnt(0);
        ctx.f.setKoUpdateCnt(0);
        ctx.f.setKoPostedCnt(0);
        ctx.f.setKoRejectCnt(0);
        ctx.f.setKoSkipCnt(0);
        ctx.f.setKoTranCnt(0);
        ctx.f.setKoC1(0);
        ctx.f.setKoC2(0);
        ctx.f.setKoC3(0);
        ctx.f.setKoAmt1(java.math.BigDecimal.valueOf(0));
        ctx.f.setKoAmt2(java.math.BigDecimal.valueOf(0));
        ctx.f.setKoAmt3(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsFilterOn("N");
        if (ctx.f.getKoParmAcct() > 0) {
            ctx.f.setWsFilterAcct(ctx.f.getKoParmAcct());
            ctx.f.setString("WS-FILTER-ON", "Y");
        }
        ctx.f.setString("WS-BR-END-SW", "N");
        ctx.f.setString("WS-BR-STARTED-SW", "N");
    }

    /** COBOL paragraph: 3000-BROWSE-DRIVER */
    private void runBrowseDriver(TaskContext ctx) {
        startAccountBrowse(ctx);
        if (ctx.f.getWsBrStartedSw().equals("Y")) {
            readNextAccount(ctx);
            while (!(ctx.f.getWsBrEndSw().equals("Y") || ctx.f.getKoStatus().equals("E"))) {
                processAccount(ctx);
            }
            endAccountBrowse(ctx);
        }
    }

    /** COBOL paragraph: 3100-START-BROWSE */
    private void startAccountBrowse(TaskContext ctx) {
        if (ctx.f.getWsFilterOn().equals("Y")) {
            ctx.f.setAcId(ctx.f.getWsFilterAcct());
        } else {
            ctx.f.setAcId(0);
        }
        ctx.appService.startBrowse(ctx.f.getWsAcctfile(), String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BR-STARTED-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-BR-END-SW", "Y");
            ctx.f.setKoStatusMsg("NO ACCOUNTS TO ASSESS.");
        } else {
            ctx.f.setString("WS-BR-END-SW", "Y");
            ctx.f.setString("KO-STATUS", "E");
            ctx.f.setKoStatusMsg("STARTBR ACCTFILE FAILED.");
        }
    }

    /** COBOL paragraph: 3200-READ-NEXT-ACCT */
    private void readNextAccount(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsAcctfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKoReadCnt(ctx.f.getKoReadCnt() + 1);
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-BR-END-SW", "Y");
        } else {
            ctx.f.setString("WS-BR-END-SW", "Y");
            ctx.f.setString("KO-STATUS", "E");
            ctx.f.setKoStatusMsg("READNEXT ACCTFILE FAILED.");
        }
    }

    /** COBOL paragraph: 4000-PROCESS-ACCT */
    private void processAccount(TaskContext ctx) {
        if (ctx.f.getWsFilterOn().equals("Y") && ctx.f.getAcId() != ctx.f.getWsFilterAcct()) {
            ctx.f.setString("WS-BR-END-SW", "Y");
        } else {
            ctx.f.setKoAmt2(ctx.f.getKoAmt2().add(ctx.f.getAcCurrBal()));
            if (Utility.fieldEquals(ctx.f.getAcActiveStatus(), "N")) {
                recordSkippedAccount(ctx);
            } else if ((ctx.f.getAcCurrBal().signum() <= 0)) {
                recordSkippedAccount(ctx);
            } else {
                ctx.f.setKoSelectCnt(ctx.f.getKoSelectCnt() + 1);
                lookupInterestRate(ctx);
                computeInterest(ctx);
                updateAccountBalance(ctx);
            }
            readNextAccount(ctx);
        }
    }

    /** COBOL paragraph: 4100-LOOKUP-RATE */
    private void lookupInterestRate(TaskContext ctx) {
        ctx.f.setString("WS-RATE-FOUND-SW", "N");
        ctx.f.setDgAcctGroup(ctx.f.getAcGroupId());
        ctx.f.setDgTypeCd(ctx.f.getWsIntType());
        ctx.f.setDgCatCd(ctx.f.getWsIntCat());
        ctx.appService.readFile(
                ctx.f.getWsDgrpfile(),
                ctx.f,
                String.valueOf(ctx.f.getDgAcctGroup())
                        + "|"
                        + String.valueOf(ctx.f.getDgTypeCd())
                        + "|"
                        + String.valueOf(ctx.f.getDgCatCd()),
                0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-RATE-FOUND-SW", "Y");
            ctx.f.setWsUsedRate(ctx.f.getDgIntRate());
        } else {
            /* CONTINUE */
        }
        if (ctx.f.getWsRateFoundSw().equals("N")) {
            ctx.f.setWsUsedRate(ctx.f.getWsDefaultRate());
            ctx.f.setKoC1(ctx.f.getKoC1() + 1);
        }
    }

    /** COBOL paragraph: 4200-COMPUTE-INTEREST */
    private void computeInterest(TaskContext ctx) {
        ctx.f.setWsIntAmt(
                ctx.f
                        .getAcCurrBal()
                        .multiply(ctx.f.getWsUsedRate())
                        .divide(
                                java.math.BigDecimal.valueOf(1200),
                                12,
                                java.math.RoundingMode.HALF_UP));
        if ((ctx.f.getWsIntAmt().signum() < 0)) {
            ctx.f.setWsIntAmt(java.math.BigDecimal.valueOf(0));
        }
    }

    /** COBOL paragraph: 4300-UPDATE-ACCT */
    private void updateAccountBalance(TaskContext ctx) {
        if ((ctx.f.getWsIntAmt().signum() <= 0)) {
            recordSkippedAccount(ctx);
            exitUpdateAccount(ctx);
            return;
        }
        ctx.appService.readFileForUpdate(
                ctx.f.getWsAcctfile(), ctx.f, String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
            ctx.f.setKoAmt3(ctx.f.getKoAmt3().add(ctx.f.getAcCurrBal()));
            exitUpdateAccount(ctx);
            return;
        }
        ctx.f.setAcCurrBal(ctx.f.getAcCurrBal().add(ctx.f.getWsIntAmt()));
        ctx.f.setAcCycDebit(ctx.f.getAcCycDebit().add(ctx.f.getWsIntAmt()));
        ctx.appService.rewriteFile(ctx.f.getWsAcctfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKoPostedCnt(ctx.f.getKoPostedCnt() + 1);
            ctx.f.setKoUpdateCnt(ctx.f.getKoUpdateCnt() + 1);
            ctx.f.setKoAmt1(ctx.f.getKoAmt1().add(ctx.f.getWsIntAmt()));
            ctx.f.setKoAmt3(ctx.f.getKoAmt3().add(ctx.f.getAcCurrBal()));
        } else {
            ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
            ctx.f.setKoAmt3(ctx.f.getKoAmt3().add(ctx.f.getAcCurrBal()));
        }
    }

    /** COBOL paragraph: 4300-EXIT */
    private void exitUpdateAccount(TaskContext ctx) {
        return;
    }

    /** COBOL paragraph: 3400-END-BROWSE */
    private void endAccountBrowse(TaskContext ctx) {
        if (ctx.f.getWsBrStartedSw().equals("Y")) {
            ctx.appService.endBrowse(ctx.f.getWsAcctfile());
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        }
    }

    /** COBOL paragraph: 9000-FINALISE */
    private void finalizeProgram(TaskContext ctx) {
        if (ctx.f.getKoStatus().equals("E")) {
            /* CONTINUE */
        } else {
            if (ctx.f.getKoRejectCnt() > 0) {
                ctx.f.setString("KO-STATUS", "W");
            } else {
                ctx.f.setString("KO-STATUS", "O");
            }
            ctx.f.setKoStatusMsg("INTEREST ASSESSMENT COMPLETE.");
        }
    }

    /** Record an account excluded from interest posting (inactive or non-positive balance). */
    private void recordSkippedAccount(TaskContext ctx) {
        ctx.f.setKoSkipCnt(ctx.f.getKoSkipCnt() + 1);
        ctx.f.setKoAmt3(ctx.f.getKoAmt3().add(ctx.f.getAcCurrBal()));
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OuintFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OuintFields(ws);
        }
    }
}

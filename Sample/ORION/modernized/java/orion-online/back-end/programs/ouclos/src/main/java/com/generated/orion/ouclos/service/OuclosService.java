package com.generated.orion.ouclos.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.ouclos.accessor.OuclosFields;
import com.generated.orion.ouclos.metadata.OuclosBmsMetadata;
import com.generated.orion.ouclos.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUCLOS. */
@Service
public class OuclosService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OuclosService.class);

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
        return "OUCLOS";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OuclosBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OuclosBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.aliasGroup("KOPS-AREA", "CA-WORK-AREA");
        initializeProgramState(ctx);
        driveBrowseLoop(ctx);
        finalizeCloseStatus(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INITIALISE */
    private void initializeProgramState(TaskContext ctx) {
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
        ctx.f.setWsCurrRaw(
                (java.time.LocalDateTime.now()
                                .format(
                                        java.time.format.DateTimeFormatter.ofPattern(
                                                "yyyyMMddHHmmss"))
                        + "00+0000"));
        ctx.f.setWsCurrN(Utility.padRight(String.valueOf(ctx.f.getWsCurrRaw()), 8).substring(0, 8));
        ctx.f.setWrYear(ctx.f.getWcnYear());
        ctx.f.setWrMon(ctx.f.getWcnMon());
        ctx.f.setWrDay(ctx.f.getWcnDay());
        ctx.f.setString("WS-BR-END-SW", "N");
        ctx.f.setString("WS-BR-STARTED-SW", "N");
    }

    /** COBOL paragraph: 3000-BROWSE-DRIVER */
    private void driveBrowseLoop(TaskContext ctx) {
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
            ctx.f.setKoStatusMsg("NO ACCOUNTS TO PROCESS.");
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
            if (Utility.fieldEquals(ctx.f.getAcActiveStatus(), ctx.f.getWsStatusClosed())) {
                ctx.f.setKoSkipCnt(ctx.f.getKoSkipCnt() + 1);
            } else {
                evaluateCloseConditions(ctx);
                if (ctx.f.getWsCloseSw().equals("Y")) {
                    closeAccount(ctx);
                } else {
                    ctx.f.setKoC3(ctx.f.getKoC3() + 1);
                }
            }
            readNextAccount(ctx);
        }
    }

    /** COBOL paragraph: 4100-CHECK-CONDITIONS */
    private void evaluateCloseConditions(TaskContext ctx) {
        ctx.f.setString("WS-CLOSE-SW", "N");
        ctx.f.setString("WS-REASON-SW", " ");
        if (!Utility.fieldEquals(ctx.f.getAcExpiryDate(), " ")
                && (ctx.f.getAcExpiryDate().compareTo(ctx.f.getWsRunDate()) < 0)) {
            ctx.f.setString("WS-CLOSE-SW", "Y");
            ctx.f.setString("WS-REASON-SW", "E");
        } else {
            if (ctx.f.getAcCreditLimit().compareTo(ctx.f.getWsZeroAmount()) == 0) {
                ctx.f.setString("WS-CLOSE-SW", "Y");
                ctx.f.setString("WS-REASON-SW", "Z");
            }
        }
    }

    /** COBOL paragraph: 4200-CLOSE-ACCT */
    private void closeAccount(TaskContext ctx) {
        ctx.appService.readFileForUpdate(
                ctx.f.getWsAcctfile(), ctx.f, String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
            return;
        }
        ctx.f.setAcActiveStatus(ctx.f.getWsStatusClosed());
        ctx.appService.rewriteFile(ctx.f.getWsAcctfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKoPostedCnt(ctx.f.getKoPostedCnt() + 1);
            ctx.f.setKoUpdateCnt(ctx.f.getKoUpdateCnt() + 1);
            ctx.f.setKoSelectCnt(ctx.f.getKoSelectCnt() + 1);
            ctx.f.setKoAmt1(ctx.f.getKoAmt1().add(ctx.f.getAcCurrBal()));
            if (ctx.f.getWsReasonSw().equals("E")) {
                ctx.f.setKoC1(ctx.f.getKoC1() + 1);
            }
            if (ctx.f.getWsReasonSw().equals("Z")) {
                ctx.f.setKoC2(ctx.f.getKoC2() + 1);
            }
        } else {
            ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
        }
    }

    /** COBOL paragraph: 3400-END-BROWSE */
    private void endAccountBrowse(TaskContext ctx) {
        if (ctx.f.getWsBrStartedSw().equals("Y")) {
            ctx.appService.endBrowse(ctx.f.getWsAcctfile());
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        }
    }

    /** COBOL paragraph: 9000-FINALISE */
    private void finalizeCloseStatus(TaskContext ctx) {
        if (ctx.f.getKoStatus().equals("E")) {
            /* CONTINUE */
        } else {
            if (ctx.f.getKoRejectCnt() > 0) {
                ctx.f.setString("KO-STATUS", "W");
            } else {
                ctx.f.setString("KO-STATUS", "O");
            }
            ctx.f.setKoStatusMsg("ACCOUNT CLOSE PROCESSING COMPLETE.");
        }
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OuclosFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OuclosFields(ws);
        }
    }
}

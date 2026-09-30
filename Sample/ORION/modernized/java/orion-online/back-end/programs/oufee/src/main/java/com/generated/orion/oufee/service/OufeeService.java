package com.generated.orion.oufee.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.oufee.accessor.OufeeFields;
import com.generated.orion.oufee.metadata.OufeeBmsMetadata;
import com.generated.orion.oufee.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUFEE. */
@Service
public class OufeeService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OufeeService.class);

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
        return "OUFEE";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OufeeBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OufeeBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.aliasGroup("KOPS-AREA", "CA-WORK-AREA");
        initializeProgram(ctx);
        loadTranIdCounter(ctx);
        browseAndProcessAccounts(ctx);
        saveTranIdCounter(ctx);
        finalizeStatus(ctx);
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
        ctx.f.setWsCurrRaw(
                (java.time.LocalDateTime.now()
                                .format(
                                        java.time.format.DateTimeFormatter.ofPattern(
                                                "yyyyMMddHHmmss"))
                        + "00+0000"));
        ctx.f.setWsCdt(Utility.padRight(String.valueOf(ctx.f.getWsCurrRaw()), 14).substring(0, 14));
        ctx.f.setPtYear(ctx.f.getWsCdtYear());
        ctx.f.setPtMon(ctx.f.getWsCdtMon());
        ctx.f.setPtDay(ctx.f.getWsCdtDay());
        ctx.f.setPtHh(ctx.f.getWsCdtHh());
        ctx.f.setPtMm(ctx.f.getWsCdtMm());
        ctx.f.setPtSs(ctx.f.getWsCdtSs());
        ctx.f.setString("WS-BR-END-SW", "N");
        ctx.f.setString("WS-BR-STARTED-SW", "N");
    }

    /** COBOL paragraph: 1500-LOAD-COUNTER */
    private void loadTranIdCounter(TaskContext ctx) {
        ctx.f.setCtKey(ctx.f.getWsCtrlKeyId());
        ctx.appService.readFileForUpdate(
                ctx.f.getWsCtrlfile(), ctx.f, String.valueOf(ctx.f.getCtKey()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setWsNextIdNum(ctx.f.getCtLastValue());
            ctx.f.setString("WS-CTRL-AVAIL-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setWsNextIdNum(0);
            ctx.f.setString("WS-CTRL-AVAIL-SW", "N");
        } else {
            ctx.f.setWsNextIdNum(0);
            ctx.f.setString("WS-CTRL-AVAIL-SW", "N");
        }
    }

    /** COBOL paragraph: 3000-BROWSE-DRIVER */
    private void browseAndProcessAccounts(TaskContext ctx) {
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
            if (Utility.fieldEquals(ctx.f.getAcActiveStatus(), "N")) {
                ctx.f.setKoSkipCnt(ctx.f.getKoSkipCnt() + 1);
            } else {
                evaluateFeeEligibility(ctx);
                if (ctx.f.getWsEligSw().equals("Y")) {
                    computeLateFee(ctx);
                    updateAccountBalance(ctx);
                    writeFeeTransaction(ctx);
                }
            }
            readNextAccount(ctx);
        }
    }

    /** COBOL paragraph: 4200-EVAL-ELIGIBILITY */
    private void evaluateFeeEligibility(TaskContext ctx) {
        ctx.f.setString("WS-ELIG-SW", "N");
        ctx.f.setWsOvlimSw("N");
        ctx.f.setWsDelqSw("N");
        if ((ctx.f.getAcCurrBal().compareTo(ctx.f.getAcCreditLimit()) > 0)) {
            ctx.f.setString("WS-OVLIM-SW", "Y");
        }
        if ((ctx.f.getAcCurrBal().signum() > 0)) {
            ctx.f.setWsMinDue(ctx.f.getAcCurrBal().multiply(ctx.f.getWsMinDuePct()));
            if ((ctx.f.getWsMinDue().compareTo(ctx.f.getWsMinDueFloor()) < 0)) {
                ctx.f.setWsMinDue(ctx.f.getWsMinDueFloor());
            }
            if ((ctx.f.getAcCycCredit().compareTo(ctx.f.getWsMinDue()) < 0)) {
                ctx.f.setString("WS-DELQ-SW", "Y");
            }
        }
        if (ctx.f.getWsOvlimSw().equals("Y") || ctx.f.getWsDelqSw().equals("Y")) {
            ctx.f.setString("WS-ELIG-SW", "Y");
        }
    }

    /** COBOL paragraph: 4300-COMPUTE-FEE */
    private void computeLateFee(TaskContext ctx) {
        ctx.f.setWsFeeAmt(java.math.BigDecimal.valueOf(0));
        if (ctx.f.getWsDelqSw().equals("Y")) {
            ctx.f.setWsFeeAmt(ctx.f.getWsFeeAmt().add(ctx.f.getWsLateFee()));
            ctx.f.setKoAmt2(ctx.f.getKoAmt2().add(ctx.f.getWsLateFee()));
            ctx.f.setKoC2(ctx.f.getKoC2() + 1);
        }
        if (ctx.f.getWsOvlimSw().equals("Y")) {
            ctx.f.setWsFeeAmt(ctx.f.getWsFeeAmt().add(ctx.f.getWsOvlimFee()));
            ctx.f.setKoAmt3(ctx.f.getKoAmt3().add(ctx.f.getWsOvlimFee()));
            ctx.f.setKoC1(ctx.f.getKoC1() + 1);
        }
        if (ctx.f.getWsOvlimSw().equals("Y") && ctx.f.getWsDelqSw().equals("Y")) {
            ctx.f.setKoC3(ctx.f.getKoC3() + 1);
        }
        ctx.f.setKoAmt1(ctx.f.getKoAmt1().add(ctx.f.getWsFeeAmt()));
        ctx.f.setKoSelectCnt(ctx.f.getKoSelectCnt() + 1);
    }

    /** COBOL paragraph: 4400-UPDATE-ACCT */
    private void updateAccountBalance(TaskContext ctx) {
        ctx.appService.readFileForUpdate(
                ctx.f.getWsAcctfile(), ctx.f, String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
            return;
        }
        ctx.f.setAcCurrBal(ctx.f.getAcCurrBal().add(ctx.f.getWsFeeAmt()));
        ctx.f.setAcCycDebit(ctx.f.getAcCycDebit().add(ctx.f.getWsFeeAmt()));
        ctx.appService.rewriteFile(ctx.f.getWsAcctfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKoUpdateCnt(ctx.f.getKoUpdateCnt() + 1);
            ctx.f.setKoPostedCnt(ctx.f.getKoPostedCnt() + 1);
        } else {
            ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
        }
    }

    /** COBOL paragraph: 4500-WRITE-TRAN */
    private void writeFeeTransaction(TaskContext ctx) {
        ctx.f.setWsNextIdNum(ctx.f.getWsNextIdNum() + 1);
        ctx.f.setWsIbPrefix(ctx.f.getWsIdPrefix());
        ctx.f.setWsIbSeq(ctx.f.getWsNextIdNum());
        ctx.f.setTranRec("");
        ctx.f.copyBytes("TR-ID", "WS-ID-BUILD");
        ctx.f.setTrTypeCd(ctx.f.getWsFeeType());
        ctx.f.setTrCatCd(ctx.f.getWsFeeCat());
        ctx.f.setTrSource("LATEFEE");
        ctx.f.setTrDesc("FEE ASSESSMENT - OUFEE");
        ctx.f.setTrAmt(ctx.f.getWsFeeAmt());
        ctx.f.setTrMerchantId(0);
        ctx.f.setTrMerchantName("ORION-CCMS");
        ctx.f.setTrMerchantCity(" ");
        ctx.f.setTrMerchantZip(" ");
        ctx.f.setTrCardNum(" ");
        ctx.f.copyBytes("TR-ORIG-TS", "WS-PROC-TS");
        ctx.f.copyBytes("TR-PROC-TS", "WS-PROC-TS");
        ctx.appService.writeFile(ctx.f.getWsTranfile(), ctx.f, String.valueOf(ctx.f.getTrId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKoTranCnt(ctx.f.getKoTranCnt() + 1);
        }
    }

    /** COBOL paragraph: 3400-END-BROWSE */
    private void endAccountBrowse(TaskContext ctx) {
        if (ctx.f.getWsBrStartedSw().equals("Y")) {
            ctx.appService.endBrowse(ctx.f.getWsAcctfile());
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        }
    }

    /** COBOL paragraph: 5500-SAVE-COUNTER */
    private void saveTranIdCounter(TaskContext ctx) {
        if (ctx.f.getKoTranCnt() == 0) {
            return;
        }
        ctx.f.setCtKey(ctx.f.getWsCtrlKeyId());
        ctx.appService.readFileForUpdate(
                ctx.f.getWsCtrlfile(), ctx.f, String.valueOf(ctx.f.getCtKey()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setCtLastValue(ctx.f.getWsNextIdNum());
            ctx.appService.rewriteFile(ctx.f.getWsCtrlfile(), ctx.f);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        } else {
            ctx.f.setCtKey(ctx.f.getWsCtrlKeyId());
            ctx.f.setCtLastValue(ctx.f.getWsNextIdNum());
            ctx.f.setCtDesc("TRAN ID COUNTER");
            ctx.appService.writeFile(
                    ctx.f.getWsCtrlfile(), ctx.f, String.valueOf(ctx.f.getCtKey()), 0);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        }
    }

    /** COBOL paragraph: 9000-FINALISE */
    private void finalizeStatus(TaskContext ctx) {
        if (ctx.f.getKoStatus().equals("E")) {
            /* CONTINUE */
        } else {
            if (ctx.f.getKoRejectCnt() > 0) {
                ctx.f.setString("KO-STATUS", "W");
            } else {
                ctx.f.setString("KO-STATUS", "O");
            }
            ctx.f.setKoStatusMsg("FEE ASSESSMENT COMPLETE.");
        }
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OufeeFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OufeeFields(ws);
        }
    }
}

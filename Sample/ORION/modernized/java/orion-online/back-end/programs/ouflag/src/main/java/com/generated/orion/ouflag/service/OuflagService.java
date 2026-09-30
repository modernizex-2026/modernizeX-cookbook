package com.generated.orion.ouflag.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.ouflag.accessor.OuflagFields;
import com.generated.orion.ouflag.metadata.OuflagBmsMetadata;
import com.generated.orion.ouflag.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUFLAG. */
@Service
public class OuflagService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OuflagService.class);

    /** Scale factor to convert a balance/credit-limit ratio into a percentage. */
    private static final java.math.BigDecimal UTILIZATION_PERCENT_SCALE =
            java.math.BigDecimal.valueOf(100);

    /** Utilization threshold representing 100% of credit limit. */
    private static final java.math.BigDecimal UTILIZATION_FULL_PCT =
            new java.math.BigDecimal("100.00");

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
            runMainProcess(ctx);
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
        return "OUFLAG";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OuflagBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OuflagBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProcess(TaskContext ctx) {
        ctx.f.aliasGroup("KFL-PARM", "CA-WORK-AREA");
        initializeFlaggingRun(ctx);
        if (!Utility.fieldEquals(ctx.f.getKflStatus(), "99")) {
            positionAccountBrowse(ctx);
            if (ctx.f.getWsBrowseSw().equals("Y")) {
                while (!(ctx.f.getWsEofSw().equals("Y")
                        || Utility.fieldEquals(ctx.f.getWsCapped(), "Y"))) {
                    scanAccountLoop(ctx);
                }
                peekNextAccount(ctx);
                endAccountBrowse(ctx);
                if (ctx.f.getWsDoExpy().equals("Y")) {
                    deactivateExpiredAccounts(ctx);
                }
            }
        }
        setCompletionStatus(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INIT */
    private void initializeFlaggingRun(TaskContext ctx) {
        ctx.f.setString("WS-EOF-SW", "N");
        ctx.f.setString("WS-BROWSE-SW", "N");
        ctx.f.setWsCapped("N");
        ctx.f.setWsDoDelq("N");
        ctx.f.setWsDoExpy("N");
        ctx.f.setWsExpCnt(0);
        ctx.f.setKflStatus("00");
        ctx.f.setKflMsg(" ");
        ctx.f.setKflRead(0);
        ctx.f.setKflSkipped(0);
        ctx.f.setKflCurrent(0);
        ctx.f.setKflDelq(0);
        ctx.f.setKflB30(0);
        ctx.f.setKflB60(0);
        ctx.f.setKflB90(0);
        ctx.f.setKflExpired(0);
        ctx.f.setKflErrors(0);
        ctx.f.setKflDelqBal(java.math.BigDecimal.valueOf(0));
        ctx.f.setKflShortfall(java.math.BigDecimal.valueOf(0));
        ctx.f.setKflNextAcct(0);
        ctx.f.setKflMore("N");
        ctx.f.setWsMax(ctx.f.getKflMax());
        switch (Utility.rtrim(ctx.f.getKflMode())) {
            case "BOTH" -> {
                ctx.f.setString("WS-DO-DELQ", "Y");
                ctx.f.setString("WS-DO-EXPY", "Y");
            }
            case "DELQ" -> {
                ctx.f.setString("WS-DO-DELQ", "Y");
            }
            case "EXPY" -> {
                ctx.f.setString("WS-DO-EXPY", "Y");
            }
            default -> {
                ctx.f.setKflStatus("99");
                ctx.f.setKflMsg("INVALID MODE - BOTH / DELQ / EXPY");
            }
        }
        if (!Utility.fieldEquals(ctx.f.getKflStatus(), "99") && ctx.f.getWsDoExpy().equals("Y")) {
            if (Utility.fieldEquals(ctx.f.getKflCutoff(), " ")
                    || ctx.f.isAllLowValues("KFL-CUTOFF")) {
                ctx.f.setKflStatus("99");
                ctx.f.setKflMsg("EXPIRY CUTOFF DATE REQUIRED");
            }
        }
    }

    /** COBOL paragraph: 2000-POSITION */
    private void positionAccountBrowse(TaskContext ctx) {
        ctx.f.setAcId(ctx.f.getKflStartAcct());
        ctx.appService.startBrowse(ctx.f.getWsAcctfile(), String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BROWSE-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-EOF-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-EOF-SW", "Y");
        } else {
            ctx.f.setKflStatus("99");
            ctx.f.setKflMsg("ACCTFILE STARTBR FAILED");
        }
    }

    /** COBOL paragraph: 3000-SCAN-LOOP */
    private void scanAccountLoop(TaskContext ctx) {
        readNextAccount(ctx);
        if (!ctx.f.getWsEofSw().equals("Y")) {
            evaluateAccount(ctx);
            if (ctx.f.getWsMax() > 0 && ctx.f.getKflRead() >= ctx.f.getWsMax()) {
                ctx.f.setWsCapped("Y");
            }
        }
    }

    /** COBOL paragraph: 3100-READ-ACCT */
    private void readNextAccount(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsAcctfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            /* CONTINUE */
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-EOF-SW", "Y");
        } else {
            ctx.f.setString("WS-EOF-SW", "Y");
            ctx.f.setKflErrors(ctx.f.getKflErrors() + 1);
            ctx.f.setKflMsg("ACCTFILE READNEXT FAILED");
        }
    }

    /** COBOL paragraph: 3200-EVAL-ACCT */
    private void evaluateAccount(TaskContext ctx) {
        ctx.f.setKflRead(ctx.f.getKflRead() + 1);
        if (Utility.fieldEquals(ctx.f.getAcActiveStatus(), "N")) {
            ctx.f.setKflSkipped(ctx.f.getKflSkipped() + 1);
            return;
        }
        if (ctx.f.getWsDoDelq().equals("Y")) {
            evaluateDelinquency(ctx);
        }
        if (ctx.f.getWsDoExpy().equals("Y")) {
            checkAccountExpiry(ctx);
        }
    }

    /** COBOL paragraph: 3300-EVAL-DELQ */
    private void evaluateDelinquency(TaskContext ctx) {
        if ((ctx.f.getAcCurrBal().signum() <= 0)) {
            ctx.f.setKflCurrent(ctx.f.getKflCurrent() + 1);
            return;
        }
        ctx.f.setWsMinDue(ctx.f.getAcCurrBal().multiply(ctx.f.getWsMinDuePct()));
        if ((ctx.f.getWsMinDue().compareTo(ctx.f.getWsMinDueFloor()) < 0)) {
            ctx.f.setWsMinDue(ctx.f.getWsMinDueFloor());
        }
        if ((ctx.f.getAcCycCredit().compareTo(ctx.f.getWsMinDue()) >= 0)) {
            ctx.f.setKflCurrent(ctx.f.getKflCurrent() + 1);
        } else {
            flagDelinquentAccount(ctx);
        }
    }

    /** COBOL paragraph: 3310-FLAG-DELQ */
    private void flagDelinquentAccount(TaskContext ctx) {
        ctx.f.setKflDelq(ctx.f.getKflDelq() + 1);
        ctx.f.setWsShortfall(ctx.f.getWsMinDue().subtract(ctx.f.getAcCycCredit()));
        if ((ctx.f.getWsShortfall().signum() < 0)) {
            ctx.f.setWsShortfall(java.math.BigDecimal.valueOf(0));
        }
        if ((ctx.f.getAcCreditLimit().signum() > 0)) {
            ctx.f.setWsUtil(
                    ctx.f
                            .getAcCurrBal()
                            .divide(ctx.f.getAcCreditLimit(), 12, java.math.RoundingMode.HALF_UP)
                            .multiply(UTILIZATION_PERCENT_SCALE));
        } else {
            ctx.f.setWsUtil(new java.math.BigDecimal("999.99"));
        }
        if ((ctx.f.getWsUtil().compareTo(UTILIZATION_FULL_PCT) >= 0)) {
            ctx.f.setKflB90(ctx.f.getKflB90() + 1);
        } else if ((ctx.f.getWsUtil().compareTo(new java.math.BigDecimal("90.00")) >= 0)) {
            ctx.f.setKflB60(ctx.f.getKflB60() + 1);
        } else {
            ctx.f.setKflB30(ctx.f.getKflB30() + 1);
        }
        ctx.f.setKflDelqBal(ctx.f.getKflDelqBal().add(ctx.f.getAcCurrBal()));
        ctx.f.setKflShortfall(ctx.f.getKflShortfall().add(ctx.f.getWsShortfall()));
    }

    /** COBOL paragraph: 3400-CHECK-EXPIRY */
    private void checkAccountExpiry(TaskContext ctx) {
        if (!Utility.fieldEquals(ctx.f.getAcExpiryDate(), " ")
                && !ctx.f.isAllLowValues("AC-EXPIRY-DATE")
                && (ctx.f.getAcExpiryDate().compareTo(ctx.f.getKflCutoff()) < 0)) {
            if (ctx.f.getWsExpCnt() < ctx.f.getWsMaxExp()) {
                ctx.f.setWsExpCnt(ctx.f.getWsExpCnt() + 1);
                ctx.f.setWsExpId(ctx.f.getWsExpCnt(), ctx.f.getAcId());
                if (ctx.f.getWsExpCnt() >= ctx.f.getWsMaxExp()) {
                    ctx.f.setWsCapped("Y");
                }
            }
        }
    }

    /** COBOL paragraph: 4000-PEEK-NEXT */
    private void peekNextAccount(TaskContext ctx) {
        if (ctx.f.getWsEofSw().equals("Y")) {
            ctx.f.setKflMore("N");
            return;
        }
        ctx.appService.readNext(ctx.f.getWsAcctfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKflMore("Y");
            ctx.f.setKflNextAcct(ctx.f.getAcId());
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setKflMore("N");
        } else {
            ctx.f.setKflMore("N");
            ctx.f.setKflErrors(ctx.f.getKflErrors() + 1);
        }
    }

    /** COBOL paragraph: 5000-END-BROWSE */
    private void endAccountBrowse(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsAcctfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 5500-DEACTIVATE-EXPIRED */
    private void deactivateExpiredAccounts(TaskContext ctx) {
        for (ctx.f.setWsEx(1);
                ctx.f.getWsEx() <= ctx.f.getWsExpCnt();
                ctx.f.setWsEx(ctx.f.getWsEx() + 1)) {
            deactivateAccount(ctx);
        }
    }

    /** COBOL paragraph: 5600-DEACTIVATE-ONE */
    private void deactivateAccount(TaskContext ctx) {
        ctx.f.setAcId(ctx.f.getWsExpId(ctx.f.getWsEx()));
        ctx.appService.readFileForUpdate(
                ctx.f.getWsAcctfile(), ctx.f, String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setKflErrors(ctx.f.getKflErrors() + 1);
            return;
        }
        if (Utility.fieldEquals(ctx.f.getAcActiveStatus(), "N")) {
            /* EXEC CICS UNLOCK — file unlock */ ;
            return;
        }
        ctx.f.setAcActiveStatus("N");
        ctx.appService.rewriteFile(ctx.f.getWsAcctfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKflExpired(ctx.f.getKflExpired() + 1);
        } else {
            ctx.f.setKflErrors(ctx.f.getKflErrors() + 1);
        }
    }

    /** COBOL paragraph: 6000-SET-STATUS */
    private void setCompletionStatus(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getKflStatus(), "99")) {
            return;
        }
        if (ctx.f.getKflRead() == 0) {
            ctx.f.setKflStatus("10");
            ctx.f.setKflMsg("NO ACCOUNTS PROCESSED");
        } else {
            ctx.f.setKflStatus("00");
            ctx.f.setKflMsg("ACCOUNT FLAGGING COMPLETE");
        }
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OuflagFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OuflagFields(ws);
        }
    }
}

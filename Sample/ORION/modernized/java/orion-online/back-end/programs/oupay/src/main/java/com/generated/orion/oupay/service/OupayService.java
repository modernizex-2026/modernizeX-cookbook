package com.generated.orion.oupay.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.oupay.accessor.OupayFields;
import com.generated.orion.oupay.metadata.OupayBmsMetadata;
import com.generated.orion.oupay.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUPAY. */
@Service
public class OupayService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OupayService.class);

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
        return "OUPAY";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OupayBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OupayBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.aliasGroup("KOPS-AREA", "CA-WORK-AREA");
        initializeProgram(ctx);
        postPayment(ctx);
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
        ctx.f.setString("WS-REJECT-SW", "N");
        ctx.f.setString("WS-ACCT-LOCK-SW", "N");
        ctx.f.setWsCurrRaw(
                (java.time.LocalDateTime.now()
                                .format(
                                        java.time.format.DateTimeFormatter.ofPattern(
                                                "yyyyMMddHHmmss"))
                        + "00+0000"));
        ctx.f.setWsCurrN(Utility.padRight(String.valueOf(ctx.f.getWsCurrRaw()), 8).substring(0, 8));
        ctx.f.setWcYear(ctx.f.getWcnYear());
        ctx.f.setWcMon(ctx.f.getWcnMon());
        ctx.f.setWcDay(ctx.f.getWcnDay());
        if (Utility.fieldEquals(ctx.f.getKoParmDate(), " ")
                || ctx.f.isAllLowValues("KO-PARM-DATE")) {
            ctx.f.copyBytes("WS-PAY-DATE", "WS-CURR-DATE");
        } else {
            ctx.f.setWsPayDate(ctx.f.getKoParmDate());
        }
    }

    /** COBOL paragraph: 2000-POST-PAYMENT */
    private void postPayment(TaskContext ctx) {
        validateRequest(ctx);
        if (isNotRejected(ctx)) {
            readAccountForUpdate(ctx);
        }
        if (isNotRejected(ctx)) {
            retrieveBillSequence(ctx);
        }
        if (isNotRejected(ctx)) {
            creditAccount(ctx);
        }
        if (isNotRejected(ctx)) {
            writeBillRecord(ctx);
        }
        if (isNotRejected(ctx)) {
            saveBillSequence(ctx);
        }
        if (ctx.f.getWsRejectSw().equals("Y") && ctx.f.getWsAcctLockSw().equals("Y")) {
            /* EXEC CICS UNLOCK — file unlock */ ;
            ctx.f.setString("WS-ACCT-LOCK-SW", "N");
        }
    }

    /** COBOL paragraph: 2050-VALIDATE-REQUEST */
    private void validateRequest(TaskContext ctx) {
        if ((ctx.f.getKoParmAmt().signum() <= 0)) {
            ctx.f.setString("WS-REJECT-SW", "Y");
            ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
            ctx.f.setKoStatusMsg("PAYMENT AMOUNT MUST BE POSITIVE.");
        }
        if (ctx.f.getWsRejectSw().equals("N") && ctx.f.getKoParmAcct() == 0) {
            ctx.f.setString("WS-REJECT-SW", "Y");
            ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
            ctx.f.setKoStatusMsg("ACCOUNT ID IS REQUIRED.");
        }
    }

    /** COBOL paragraph: 2100-READ-ACCT-UPD */
    private void readAccountForUpdate(TaskContext ctx) {
        ctx.f.setAcId(ctx.f.getKoParmAcct());
        ctx.appService.readFileForUpdate(
                ctx.f.getWsAcctfile(), ctx.f, String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKoReadCnt(ctx.f.getKoReadCnt() + 1);
            ctx.f.setString("WS-ACCT-LOCK-SW", "Y");
            if (Utility.fieldEquals(ctx.f.getAcActiveStatus(), "N")) {
                ctx.f.setString("WS-REJECT-SW", "Y");
                ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
                ctx.f.setKoStatusMsg("ACCOUNT IS NOT ACTIVE.");
            }
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-REJECT-SW", "Y");
            ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
            ctx.f.setKoStatusMsg("ACCOUNT NOT FOUND.");
        } else {
            ctx.f.setString("WS-REJECT-SW", "Y");
            ctx.f.setString("KO-STATUS", "E");
            ctx.f.setKoStatusMsg("READ ACCTFILE FOR UPDATE FAILED.");
        }
    }

    /** COBOL paragraph: 2200-GET-SEQUENCE */
    private void retrieveBillSequence(TaskContext ctx) {
        ctx.f.setCtKey(ctx.f.getWsBillKey());
        ctx.appService.readFileForUpdate(
                ctx.f.getWsCtrlfile(), ctx.f, String.valueOf(ctx.f.getCtKey()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setWsBillSeq(ctx.f.getCtLastValue());
            ctx.f.setString("WS-CTRL-AVAIL-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setWsBillSeq(0);
            ctx.f.setString("WS-CTRL-AVAIL-SW", "N");
        } else {
            ctx.f.setString("WS-REJECT-SW", "Y");
            ctx.f.setString("KO-STATUS", "E");
            ctx.f.setKoStatusMsg("READ CTRLFILE BILLID FAILED.");
        }
        if (ctx.f.getWsRejectSw().equals("N")) {
            ctx.f.setWsBillSeq(ctx.f.getWsBillSeq() + 1);
        }
    }

    /** COBOL paragraph: 2300-CREDIT-ACCOUNT */
    private void creditAccount(TaskContext ctx) {
        ctx.f.setAcCurrBal(ctx.f.getAcCurrBal().subtract(ctx.f.getKoParmAmt()));
        ctx.f.setAcCycCredit(ctx.f.getAcCycCredit().add(ctx.f.getKoParmAmt()));
        ctx.f.setWsNewBal(ctx.f.getAcCurrBal());
        ctx.appService.rewriteFile(ctx.f.getWsAcctfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-ACCT-LOCK-SW", "N");
        } else {
            ctx.f.setString("WS-REJECT-SW", "Y");
            ctx.f.setString("KO-STATUS", "E");
            ctx.f.setKoStatusMsg("REWRITE ACCTFILE FAILED.");
        }
    }

    /** COBOL paragraph: 2400-WRITE-BILL */
    private void writeBillRecord(TaskContext ctx) {
        ctx.f.setBillRec("");
        ctx.f.setBlId(ctx.f.getWsBillSeq());
        ctx.f.setBlAcctId(ctx.f.getKoParmAcct());
        ctx.f.setBlAmount(ctx.f.getKoParmAmt());
        ctx.f.setBlPayDate(ctx.f.getWsPayDate());
        ctx.f.setWsCfmSeq(ctx.f.getWsBillSeq());
        ctx.f.copyBytes("BL-CONFIRM-NUM", "WS-CONFIRM");
        ctx.f.setBlStatus("P");
        ctx.appService.writeFile(ctx.f.getWsBillfile(), ctx.f, String.valueOf(ctx.f.getBlId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKoPostedCnt(ctx.f.getKoPostedCnt() + 1);
            ctx.f.setKoTranCnt(ctx.f.getKoTranCnt() + 1);
            ctx.f.setKoC1(Utility.toCobolInt(ctx.f.getWsBillSeq(), 9));
            ctx.f.setKoAmt1(ctx.f.getKoParmAmt());
            ctx.f.setKoAmt2(ctx.f.getWsNewBal());
        } else {
            ctx.f.setString("WS-REJECT-SW", "Y");
            ctx.f.setString("KO-STATUS", "E");
            ctx.f.setKoStatusMsg("WRITE BILLFILE FAILED.");
        }
    }

    /** COBOL paragraph: 2500-SAVE-SEQUENCE */
    private void saveBillSequence(TaskContext ctx) {
        if (ctx.f.getWsCtrlAvailSw().equals("Y")) {
            ctx.f.setCtLastValue(ctx.f.getWsBillSeq());
            ctx.appService.rewriteFile(ctx.f.getWsCtrlfile(), ctx.f);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        } else {
            ctx.f.setCtKey(ctx.f.getWsBillKey());
            ctx.f.setCtLastValue(ctx.f.getWsBillSeq());
            ctx.f.setCtDesc("BILL ID SEQUENCE");
            ctx.appService.writeFile(
                    ctx.f.getWsCtrlfile(), ctx.f, String.valueOf(ctx.f.getCtKey()), 0);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        }
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setKoStatusMsg("PAYMENT POSTED, COUNTER UPDATE WARNING.");
            ctx.f.setString("KO-STATUS", "W");
        }
    }

    /** COBOL paragraph: 9000-FINALISE */
    private void finalizeStatus(TaskContext ctx) {
        if (ctx.f.getKoStatus().equals("E")) {
            /* CONTINUE */
        } else {
            if (ctx.f.getKoPostedCnt() > 0) {
                if (!ctx.f.getKoStatus().equals("W")) {
                    ctx.f.setString("KO-STATUS", "O");
                    ctx.f.setKoStatusMsg("PAYMENT POSTED SUCCESSFULLY.");
                }
            } else {
                ctx.f.setString("KO-STATUS", "W");
                if (Utility.fieldEquals(ctx.f.getKoStatusMsg(), " ")) {
                    ctx.f.setKoStatusMsg("PAYMENT REJECTED.");
                }
            }
        }
    }

    /** Whether the payment request has not been rejected by validation or a posting step so far. */
    private boolean isNotRejected(TaskContext ctx) {
        return ctx.f.getWsRejectSw().equals("N");
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OupayFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OupayFields(ws);
        }
    }
}

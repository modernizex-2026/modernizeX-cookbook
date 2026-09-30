package com.generated.orion.ocbillp.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcbillpLinkParm;
import com.generated.orion.ocbillp.accessor.OcbillpFields;
import com.generated.orion.ocbillp.metadata.OcbillpBmsMetadata;
import com.generated.orion.ocbillp.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCBILLP. */
@Service
public class OcbillpService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcbillpService.class);

    private static final int COMMAREA_LENGTH = 692;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        runMainProgram(ctx);
    }

    @Override
    public String getProgramName() {
        return "OCBILLP";
    }

    @Override
    public String getTransId() {
        return "ORBP";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcbillpBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcbillpBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcbillpBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        if (ctx.appService.getEibcalen() == 0) {
            ctx.f.setOrionCommarea("");
            sendInitialScreen(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            ctx.f.copyBytes("WS-STATE-AREA", "CA-WORK-AREA");
            ctx.f.setCaErrMsg(" ");
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitialScreen(ctx);
            } else {
                dispatchUserInput(ctx);
            }
        }
        returnToCaller(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MBILLPAO");
        ctx.f.setString("WS-STAGE", "0");
        ctx.f.setWsSvAcctId(0);
        ctx.f.setWsSvBal(java.math.BigDecimal.valueOf(0));
        populateHeaderFields(ctx);
        ctx.f.setErrmsgo("Enter account id and press ENTER.");
        ctx.appService.sendMap(
                "MBILLPA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void dispatchUserInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            handleEnterKey(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-HANDLE-ENTER */
    private void handleEnterKey(TaskContext ctx) {
        receiveScreenInput(ctx);
        if (ctx.f.getWsStage().equals("0")) {
            processAccountLookup(ctx);
        } else if (ctx.f.getWsStage().equals("1")) {
            processPaymentRequest(ctx);
        } else {
            sendInitialScreen(ctx);
        }
    }

    /** COBOL paragraph: 2110-RECEIVE-MAP */
    private void receiveScreenInput(TaskContext ctx) {
        ctx.appService.receiveMap("MBILLPA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 2200-PROCESS-ACCT */
    private void processAccountLookup(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getAcctidi(), " ") || ctx.f.isAllLowValues("ACCTIDI")) {
            ctx.f.setErrmsgo(ctx.f.getWsMsgRequired());
            sendDataOnlyScreen(ctx);
        } else {
            ctx.f.setAcId(Utility.parseNumeric(ctx.f.getAcctidi()).longValue());
            ctx.f.setCaAcctId(ctx.f.getAcId());
            readAccountRecord(ctx);
            if (ctx.f.getWsFoundFlg().equals("Y")) {
                showAccountBalance(ctx);
            } else {
                ctx.f.setErrmsgo(ctx.f.getWsMsgNotfnd());
                sendDataOnlyScreen(ctx);
            }
        }
    }

    /** COBOL paragraph: 2210-SHOW-BALANCE */
    private void showAccountBalance(TaskContext ctx) {
        ctx.f.setWsSvAcctId(ctx.f.getAcId());
        ctx.f.setWsSvBal(ctx.f.getAcCurrBal());
        ctx.f.setString("WS-STAGE", "1");
        populatePayScreenFields(ctx);
        ctx.f.setErrmsgo("Enter amount, set confirm to Y, press ENTER.");
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 2300-PROCESS-PAY */
    private void processPaymentRequest(TaskContext ctx) {
        validatePaymentAmount(ctx);
        if (ctx.f.getWsValidFlg().equals("N")) {
            redisplayPayScreen(ctx, null);
            return;
        }
        if (ctx.f.getWsPayAmt().signum() <= 0) {
            redisplayPayScreen(ctx, "Amount must be greater than zero.");
            return;
        }
        if (ctx.f.getWsPayAmt().compareTo(ctx.f.getWsSvBal()) > 0) {
            redisplayPayScreen(ctx, "Amount exceeds current balance.");
            return;
        }
        checkPaymentConfirmation(ctx);
    }

    /** Repopulate and redisplay the pay screen, optionally after a validation error. */
    private void redisplayPayScreen(TaskContext ctx, String errorMessage) {
        if (errorMessage != null) {
            ctx.f.setErrmsgo(errorMessage);
        }
        populatePayScreenFields(ctx);
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 2310-VALIDATE-AMOUNT */
    private void validatePaymentAmount(TaskContext ctx) {
        ctx.f.setWsAmtIn(ctx.f.getBlamti());
        ctx.f.setWsDotCnt(0);
        ctx.f.setWsDigCnt(0);
        ctx.f.setString("WS-VALID-FLG", "Y");
        if (Utility.fieldEquals(ctx.f.getWsAmtIn(), " ") || ctx.f.isAllLowValues("WS-AMT-IN")) {
            ctx.f.setString("WS-VALID-FLG", "N");
            ctx.f.setErrmsgo("Please enter a payment amount.");
            return;
        }
        for (ctx.f.setWsI(1); ctx.f.getWsI() <= 12; ctx.f.setWsI(ctx.f.getWsI() + 1)) {
            ctx.f.setWsCh(
                    Utility.padRight(String.valueOf(ctx.f.getWsAmtIn()), 256)
                            .substring(ctx.f.getWsI() - 1, ctx.f.getWsI() - 1 + 1));
            if (Utility.fieldEquals(ctx.f.getWsCh(), " ")) {
                /* CONTINUE */
            } else if (Utility.fieldEquals(ctx.f.getWsCh(), ".")) {
                ctx.f.setWsDotCnt(ctx.f.getWsDotCnt() + 1);
            } else if (ctx.f.getWsCh().compareTo("0") >= 0 && ctx.f.getWsCh().compareTo("9") <= 0) {
                ctx.f.setWsDigCnt(ctx.f.getWsDigCnt() + 1);
            } else {
                ctx.f.setString("WS-VALID-FLG", "N");
            }
        }
        if (ctx.f.getWsDotCnt() > 1) {
            ctx.f.setString("WS-VALID-FLG", "N");
        }
        if (ctx.f.getWsDigCnt() == 0) {
            ctx.f.setString("WS-VALID-FLG", "N");
        }
        if (ctx.f.getWsValidFlg().equals("N")) {
            ctx.f.setErrmsgo("Amount is not a valid number.");
            return;
        }
        ctx.f.setWsPayAmt(Utility.parseNumeric(String.valueOf(ctx.f.getWsAmtIn())));
    }

    /** COBOL paragraph: 2320-CHECK-CONFIRM */
    private void checkPaymentConfirmation(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getBlconfi(), "Y")
                || Utility.fieldEquals(ctx.f.getBlconfi(), "y")) {
            postPayment(ctx);
        } else {
            redisplayPayScreen(ctx, "Set confirm to Y to post this payment.");
        }
    }

    /** COBOL paragraph: 2400-POST-PAYMENT */
    private void postPayment(TaskContext ctx) {
        getNextBillId(ctx);
        if (reportPaymentErrorAndExit(ctx)) {
            return;
        }
        buildConfirmationNumber(ctx);
        writeBillRecord(ctx);
        if (reportPaymentErrorAndExit(ctx)) {
            return;
        }
        updateAccountBalance(ctx);
        if (reportPaymentErrorAndExit(ctx)) {
            return;
        }
        showPaymentConfirmation(ctx);
    }

    /**
     * Redisplay the pay screen when a payment step failed. Returns true when the caller should stop
     * processing (WS-ERR-FLG is set).
     */
    private boolean reportPaymentErrorAndExit(TaskContext ctx) {
        if (!ctx.f.getWsErrFlg().equals("Y")) {
            return false;
        }
        populatePayScreenFields(ctx);
        sendDataOnlyScreen(ctx);
        return true;
    }

    /** COBOL paragraph: 3000-READ-ACCT */
    private void readAccountRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.appService.readFile(ctx.f.getWsAcctfile(), ctx.f, String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-FOUND-FLG", "N");
        } else {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error reading account file.");
        }
    }

    /** COBOL paragraph: 3100-GET-NEXT-BILLID */
    private void getNextBillId(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setCtKey("BILLID  ");
        ctx.appService.readFileForUpdate(
                ctx.f.getWsCtrlfile(), ctx.f, String.valueOf(ctx.f.getCtKey()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setCtLastValue(ctx.f.getCtLastValue() + 1);
            ctx.f.setBlId(ctx.f.getCtLastValue());
            ctx.f.setWsId11(ctx.f.getCtLastValue());
            rewriteControlRecord(ctx);
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Control record BILLID missing.");
        } else {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error reading control file.");
        }
    }

    /** COBOL paragraph: 3110-REWRITE-CTRL */
    private void rewriteControlRecord(TaskContext ctx) {
        ctx.appService.rewriteFile(ctx.f.getWsCtrlfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error updating control file.");
        }
    }

    /** COBOL paragraph: 3200-WRITE-BILL */
    private void writeBillRecord(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setBlAcctId(ctx.f.getWsSvAcctId());
        ctx.f.setBlAmount(ctx.f.getWsPayAmt());
        ctx.f.setBlPayDate(ctx.f.getWsHdrDate());
        ctx.f.setBlConfirmNum(ctx.f.getWsConfirmNum());
        ctx.f.setBlStatus("P");
        ctx.appService.writeFile(ctx.f.getWsBillfile(), ctx.f, String.valueOf(ctx.f.getBlId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            /* CONTINUE */
        } else if (ctx.f.getWsRespCd() == 14) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Duplicate bill id generated.");
        } else {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error writing bill file.");
        }
    }

    /** COBOL paragraph: 3300-UPDATE-ACCT-BAL */
    private void updateAccountBalance(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setAcId(ctx.f.getWsSvAcctId());
        ctx.appService.readFileForUpdate(
                ctx.f.getWsAcctfile(), ctx.f, String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setAcCurrBal(ctx.f.getAcCurrBal().subtract(ctx.f.getWsPayAmt()));
            ctx.f.setAcCycCredit(ctx.f.getAcCycCredit().add(ctx.f.getWsPayAmt()));
            ctx.f.setWsNewBal(ctx.f.getAcCurrBal());
            rewriteAccountRecord(ctx);
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Account not found on update.");
        } else {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error reading account for update.");
        }
    }

    /** COBOL paragraph: 3310-REWRITE-ACCT */
    private void rewriteAccountRecord(TaskContext ctx) {
        ctx.appService.rewriteFile(ctx.f.getWsAcctfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error updating account balance.");
        }
    }

    /** COBOL paragraph: 4000-POPULATE-PAY-SCREEN */
    private void populatePayScreenFields(TaskContext ctx) {
        ctx.f.setAcctido(String.format("%011d", ctx.f.getWsSvAcctId()));
        ctx.f.setWsEdBal(ctx.f.getWsSvBal());
        ctx.f.setBlbalo(ctx.f.getString("WS-ED-BAL"));
    }

    /** COBOL paragraph: 4100-BUILD-CONFIRM */
    private void buildConfirmationNumber(TaskContext ctx) {
        ctx.f.setWsConfirmNum(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("ORB"));
            sb.append(String.valueOf(ctx.f.getWsId11()));
            sb.append(String.valueOf("00"));
            ctx.f.setWsConfirmNum(sb.toString());
        }
    }

    /** COBOL paragraph: 4200-SHOW-CONFIRMATION */
    private void showPaymentConfirmation(TaskContext ctx) {
        ctx.f.setWsSvBal(ctx.f.getWsNewBal());
        ctx.f.setWsEdBal(ctx.f.getWsNewBal());
        ctx.f.setBlbalo(ctx.f.getString("WS-ED-BAL"));
        ctx.f.setBlamto(" ");
        ctx.f.setBlconfo(" ");
        ctx.f.setErrmsgo(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("Payment posted. Confirmation: "));
            sb.append(String.valueOf(ctx.f.getWsConfirmNum()));
            ctx.f.setErrmsgo(sb.toString());
        }
        ctx.f.setString("WS-STAGE", "0");
        ctx.f.setWsSvAcctId(0);
        ctx.f.setWsSvBal(java.math.BigDecimal.valueOf(0));
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 7000-XCTL-MENU */
    private void transferToMenuProgram(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.f.copyBytes("CA-WORK-AREA", "WS-STATE-AREA");
        ctx.appService.xctl(
                ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8000-POPULATE-HEADER */
    private void populateHeaderFields(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        loadCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnlyScreen(TaskContext ctx) {
        populateHeaderFields(ctx);
        ctx.appService.sendMap(
                "MBILLPA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8500-GET-DATE-TIME */
    private void loadCurrentDateTime(TaskContext ctx) {
        ctx.appService.askTime();
        com.appruntime.FormatTimeResult _ftResult =
                ctx.appService.formatTime(null, "YYYYMMDD", "-", ":");
        ctx.f.setWsHdrDate(_ftResult.getDate());
        ctx.f.setWsHdrTime(_ftResult.getTime());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 9000-RETURN */
    private void returnToCaller(TaskContext ctx) {
        ctx.f.copyBytes("CA-WORK-AREA", "WS-STATE-AREA");
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcbillpFields f;

        final AppService appService;

        final OcbillpLinkParm link = new OcbillpLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcbillpFields(ws);
        }
    }
}

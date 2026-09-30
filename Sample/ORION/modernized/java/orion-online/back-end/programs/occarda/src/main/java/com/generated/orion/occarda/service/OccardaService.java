package com.generated.orion.occarda.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OccardaLinkParm;
import com.generated.orion.occarda.accessor.OccardaFields;
import com.generated.orion.occarda.metadata.OccardaBmsMetadata;
import com.generated.orion.occarda.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCCARDA. */
@Service
public class OccardaService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OccardaService.class);

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
        return "OCCARDA";
    }

    @Override
    public String getTransId() {
        return "OROD";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OccardaBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OccardaBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OccardaBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setCaErrMsg(" ");
        if (ctx.appService.getEibcalen() == 0) {
            ctx.f.setOrionCommarea("");
            sendInitialScreen(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitialScreen(ctx);
            } else {
                processUserInput(ctx);
            }
        }
        ctx.appService.returnTransid(ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), 692);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MCARDAAO");
        populateScreenHeader(ctx);
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setCardnuml((short) -1);
        ctx.appService.sendMap(
                "MCARDAA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processUserInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            addNewCard(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnly(ctx);
        }
    }

    /** COBOL paragraph: 2100-ADD-CARD */
    private void addNewCard(TaskContext ctx) {
        ctx.appService.receiveMap("MCARDAA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MCARDAAI");
        }
        validateAllFields(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            ctx.f.setCardnuml((short) -1);
            sendDataOnly(ctx);
        } else {
            writeCardRecord(ctx);
            if (ctx.f.getWsWriteSw().equals("Y")) {
                writeXrefRecord(ctx);
                ctx.f.setCaCardNum(ctx.f.getWsNewCard());
                sendInitialScreen(ctx);
                ctx.f.setErrmsgo(ctx.f.getWsMOk());
                sendDataOnly(ctx);
            } else {
                ctx.f.setCardnuml((short) -1);
                sendDataOnly(ctx);
            }
        }
    }

    /** COBOL paragraph: 3000-READ-ACCT */
    private void readAccountRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.f.setAcId(ctx.f.getWsNewAcct());
        ctx.appService.readFile(ctx.f.getWsAcctfile(), ctx.f, String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-FOUND-FLG", "N");
        } else {
            abendUnrecoverableError(ctx);
        }
    }

    /** COBOL paragraph: 3100-READ-CARD */
    private void readCardRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.f.setCdNum(ctx.f.getWsNewCard());
        ctx.appService.readFile(ctx.f.getWsCardfile(), ctx.f, String.valueOf(ctx.f.getCdNum()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-FOUND-FLG", "N");
        } else {
            abendUnrecoverableError(ctx);
        }
    }

    /** COBOL paragraph: 3200-READ-STUB */
    private void readXrefStub(TaskContext ctx) {
        ctx.f.setWsNewCust(0);
        ctx.f.setWsXrefKey(ctx.f.getWsNewAcct());
        ctx.f.setXrCardNum(String.format("%016d", ctx.f.getWsXrefKey()));
        ctx.appService.readFile(
                ctx.f.getWsXreffile(), ctx.f, String.valueOf(ctx.f.getXrCardNum()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setWsNewCust(ctx.f.getXrCustId());
        }
    }

    /** COBOL paragraph: 3500-WRITE-CARD */
    private void writeCardRecord(TaskContext ctx) {
        ctx.f.setString("WS-WRITE-SW", "N");
        ctx.f.setCardRec("");
        ctx.f.setCdNum(ctx.f.getWsNewCard());
        ctx.f.setCdAcctId(ctx.f.getWsNewAcct());
        ctx.f.setCdCvv(ctx.f.getWsNewCvv());
        ctx.f.setCdEmbossedName(ctx.f.getWsNewName());
        ctx.f.setCdExpiryDate(ctx.f.getWsNewExpiry());
        ctx.f.setCdActiveStatus("Y");
        ctx.appService.writeFile(ctx.f.getWsCardfile(), ctx.f, String.valueOf(ctx.f.getCdNum()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-WRITE-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 14) {
            ctx.f.setErrmsgo(ctx.f.getWsMCardDup());
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMWriteErr());
        }
    }

    /** COBOL paragraph: 3700-WRITE-XREF */
    private void writeXrefRecord(TaskContext ctx) {
        ctx.f.setXrefRec("");
        ctx.f.setXrCardNum(ctx.f.getWsNewCard());
        ctx.f.setXrAcctId(ctx.f.getWsNewAcct());
        ctx.f.setXrCustId(ctx.f.getWsNewCust());
        ctx.appService.writeFile(
                ctx.f.getWsXreffile(), ctx.f, String.valueOf(ctx.f.getXrCardNum()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            /* CONTINUE */
        } else if (ctx.f.getWsRespCd() == 14) {
            /* CONTINUE */
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMXrefErr());
        }
    }

    /** COBOL paragraph: 5000-VALIDATE-ALL */
    private void validateAllFields(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setErrmsgo(" ");
        if (ctx.f.getWsValidSw().equals("Y")) {
            validateCardNumber(ctx);
        }
        if (ctx.f.getWsValidSw().equals("Y")) {
            validateAccountNumber(ctx);
        }
        if (ctx.f.getWsValidSw().equals("Y")) {
            validateCardholderName(ctx);
        }
        if (ctx.f.getWsValidSw().equals("Y")) {
            validateCvv(ctx);
        }
        if (ctx.f.getWsValidSw().equals("Y")) {
            validateExpiryDate(ctx);
        }
        if (ctx.f.getWsValidSw().equals("Y")) {
            validateAccountExists(ctx);
        }
        if (ctx.f.getWsValidSw().equals("Y")) {
            validateCardUnique(ctx);
        }
    }

    /** COBOL paragraph: 5010-VAL-CARD */
    private void validateCardNumber(TaskContext ctx) {
        ctx.f.setWsNcIn(ctx.f.getCardnumi());
        ctx.f.setWsNcLen(16);
        parseNumericField(ctx);
        if (ctx.f.getWsValidSw().equals("N") || ctx.f.getWsNcDigits() != 16) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMCardReq());
        } else {
            ctx.f.setWsNewCard(ctx.f.getCardnumi());
            ctx.f.setWsNewCard(
                    String.valueOf(ctx.f.getWsNewCard())
                            .replace(
                                    String.valueOf(RuntimeConstants.LOW_VALUES),
                                    String.valueOf(" ")));
        }
    }

    /** COBOL paragraph: 5020-VAL-ACCT */
    private void validateAccountNumber(TaskContext ctx) {
        ctx.f.setWsNcIn(ctx.f.getCdaccti());
        ctx.f.setWsNcLen(11);
        parseNumericField(ctx);
        if (ctx.f.getWsValidSw().equals("N") || ctx.f.getWsNcDigits() > 11) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMAcctNum());
        } else {
            ctx.f.setWsNewAcct(ctx.f.getWsNcValue());
        }
    }

    /** COBOL paragraph: 5030-VAL-NAME */
    private void validateCardholderName(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getCdnamei(), " ") || ctx.f.isAllLowValues("CDNAMEI")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMNameReq());
        } else {
            ctx.f.setWsNewName(ctx.f.getCdnamei());
        }
    }

    /** COBOL paragraph: 5040-VAL-CVV */
    private void validateCvv(TaskContext ctx) {
        ctx.f.setWsNcIn(ctx.f.getCdcvvi());
        ctx.f.setWsNcLen(3);
        parseNumericField(ctx);
        if (ctx.f.getWsValidSw().equals("N") || ctx.f.getWsNcDigits() != 3) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMCvvNum());
        } else {
            ctx.f.setWsNewCvv(ctx.f.getCdcvvi());
            ctx.f.setWsNewCvv(
                    String.valueOf(ctx.f.getWsNewCvv())
                            .replace(
                                    String.valueOf(RuntimeConstants.LOW_VALUES),
                                    String.valueOf(" ")));
        }
    }

    /** COBOL paragraph: 5050-VAL-EXPIRY */
    private void validateExpiryDate(TaskContext ctx) {
        ctx.f.setKdFunc("VALD");
        ctx.f.setKdDateIn(ctx.f.getCdexpi());
        ctx.f.setKdDateOut(" ");
        ctx.f.setKdStatus("00");
        Object[] _ca0 = new Object[] {ctx.f.getKdateParm()};
        ctx.appService.callProgram("OUDATE", _ca0);
        ctx.f.setKdateParm(String.valueOf(_ca0[0]));
        if (Utility.fieldEquals(ctx.f.getKdStatus(), "00")) {
            ctx.f.setWsNewExpiry(ctx.f.getCdexpi());
        } else {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMExpBad());
        }
    }

    /** COBOL paragraph: 5060-VAL-ACCT-EXISTS */
    private void validateAccountExists(TaskContext ctx) {
        readAccountRecord(ctx);
        if (ctx.f.getWsFoundFlg().equals("N")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMAcctNf());
        }
    }

    /** COBOL paragraph: 5070-VAL-CARD-UNIQUE */
    private void validateCardUnique(TaskContext ctx) {
        readCardRecord(ctx);
        if (ctx.f.getWsFoundFlg().equals("Y")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMCardDup());
        } else {
            readXrefStub(ctx);
        }
    }

    /** COBOL paragraph: 6000-PARSE-NUM */
    private void parseNumericField(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsNcValue(0);
        ctx.f.setWsNcDigits(0);
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        for (ctx.f.setWsNcPos(1);
                ctx.f.getWsNcPos() <= ctx.f.getWsNcLen();
                ctx.f.setWsNcPos(ctx.f.getWsNcPos() + 1)) {
            ctx.f.setWsNcChar(
                    Utility.padRight(String.valueOf(ctx.f.getWsNcIn()), 256)
                            .substring(ctx.f.getWsNcPos() - 1, ctx.f.getWsNcPos() - 1 + 1));
            if (Utility.fieldEquals(ctx.f.getWsNcChar(), " ")) {
                /* CONTINUE */
            } else if ((ctx.f.getWsNcChar().compareTo("0") >= 0)
                    && (ctx.f.getWsNcChar().compareTo("9") <= 0)) {
                ctx.f.setWsNcDigit(Utility.parseNumeric(ctx.f.getWsNcChar()).intValue());
                ctx.f.setWsNcValue(((ctx.f.getWsNcValue() * (long) 10) + ctx.f.getWsNcDigit()));
                ctx.f.setWsNcDigits(ctx.f.getWsNcDigits() + 1);
            } else {
                ctx.f.setString("WS-VALID-SW", "N");
            }
        }
        if (ctx.f.getWsNcDigits() == 0) {
            ctx.f.setString("WS-VALID-SW", "N");
        }
    }

    /** COBOL paragraph: 7000-XCTL-MENU */
    private void transferToMenu(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), 692);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8000-POPULATE-HEADER */
    private void populateScreenHeader(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        loadCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnly(TaskContext ctx) {
        populateScreenHeader(ctx);
        ctx.appService.sendMap(
                "MCARDAA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
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

    /** COBOL paragraph: 9500-ABEND-RTN */
    private void abendUnrecoverableError(TaskContext ctx) {
        ctx.f.setWsMsgText("OCCARDA: unrecoverable file error. Contact support.");
        ctx.appService.sendText(String.valueOf(ctx.f), true, true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.appService.returnProgram();
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OccardaFields f;

        final AppService appService;

        final OccardaLinkParm link = new OccardaLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OccardaFields(ws);
        }
    }
}

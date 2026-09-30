package com.generated.orion.ocaccta.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcacctaLinkParm;
import com.generated.orion.ocaccta.accessor.OcacctaFields;
import com.generated.orion.ocaccta.metadata.OcacctaBmsMetadata;
import com.generated.orion.ocaccta.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCACCTA. */
@Service
public class OcacctaService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcacctaService.class);

    /** Scratch buffer length used when scanning a field character-by-character. */
    private static final int FIELD_SCAN_BUFFER_LENGTH = 256;

    /** Byte length of the ORION-COMMAREA passed across program transfers. */
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
        return "OCACCTA";
    }

    @Override
    public String getTransId() {
        return "OROA";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcacctaBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcacctaBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcacctaBmsMetadata.getFieldMapping(mapName);
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
                processInputKey(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MACCTAAO");
        populateHeaderFields(ctx);
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setAcctidl((short) -1);
        ctx.appService.sendMap(
                "MACCTAA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInputKey(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            openAccount(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-OPEN-ACCT */
    private void openAccount(TaskContext ctx) {
        ctx.appService.receiveMap("MACCTAA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MACCTAAI");
        }
        validateAllFields(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            ctx.f.setAcctidl((short) -1);
            sendDataOnlyScreen(ctx);
        } else {
            writeAccountRecord(ctx);
            if (ctx.f.getWsWriteSw().equals("Y")) {
                writeCrossReferenceRecord(ctx);
                ctx.f.setCaAcctId(ctx.f.getWsNewAcct());
                sendInitialScreen(ctx);
                ctx.f.setErrmsgo(ctx.f.getWsMOk());
                sendDataOnlyScreen(ctx);
            } else {
                ctx.f.setAcctidl((short) -1);
                sendDataOnlyScreen(ctx);
            }
        }
    }

    /** COBOL paragraph: 3000-READ-CUST */
    private void readCustomerRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.f.setCuId(ctx.f.getWsNewCust());
        ctx.appService.readFile(ctx.f.getWsCustfile(), ctx.f, String.valueOf(ctx.f.getCuId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-FOUND-FLG", "N");
        } else {
            abendWithFileError(ctx);
        }
    }

    /** COBOL paragraph: 3100-READ-ACCT */
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
            abendWithFileError(ctx);
        }
    }

    /** COBOL paragraph: 3500-WRITE-ACCT */
    private void writeAccountRecord(TaskContext ctx) {
        ctx.f.setString("WS-WRITE-SW", "N");
        ctx.f.setAcctRec("");
        ctx.f.setAcId(ctx.f.getWsNewAcct());
        ctx.f.setAcActiveStatus("Y");
        ctx.f.setAcCurrBal(java.math.BigDecimal.valueOf(0));
        ctx.f.setAcCreditLimit(ctx.f.getWsNewCrlim());
        ctx.f.setAcCashLimit(ctx.f.getWsNewCslim());
        ctx.f.setAcOpenDate(ctx.f.getWsNewOpen());
        ctx.f.setAcExpiryDate(" ");
        ctx.f.setAcReissueDate(" ");
        ctx.f.setAcCycCredit(java.math.BigDecimal.valueOf(0));
        ctx.f.setAcCycDebit(java.math.BigDecimal.valueOf(0));
        ctx.f.setAcAddrZip(" ");
        ctx.f.setAcGroupId(ctx.f.getWsNewGroup());
        ctx.appService.writeFile(ctx.f.getWsAcctfile(), ctx.f, String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-WRITE-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 14) {
            ctx.f.setErrmsgo(ctx.f.getWsMAcctDup());
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMWriteErr());
        }
    }

    /** COBOL paragraph: 3700-WRITE-XREF */
    private void writeCrossReferenceRecord(TaskContext ctx) {
        ctx.f.setXrefRec("");
        ctx.f.setWsXrefKey(ctx.f.getWsNewAcct());
        ctx.f.setXrCardNum(String.format("%016d", ctx.f.getWsXrefKey()));
        ctx.f.setXrAcctId(ctx.f.getWsNewAcct());
        ctx.f.setXrCustId(ctx.f.getWsNewCust());
        ctx.appService.writeFile(
                ctx.f.getWsXreffile(), ctx.f, String.valueOf(ctx.f.getXrCardNum()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != 0 && ctx.f.getWsRespCd() != 14) {
            ctx.f.setErrmsgo(ctx.f.getWsMXrefErr());
        }
    }

    /** COBOL paragraph: 5000-VALIDATE-ALL */
    private void validateAllFields(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setErrmsgo(" ");
        if (isValidationPassing(ctx)) {
            validateAccountId(ctx);
        }
        if (isValidationPassing(ctx)) {
            validateCustomerId(ctx);
        }
        if (isValidationPassing(ctx)) {
            validateCreditLimit(ctx);
        }
        if (isValidationPassing(ctx)) {
            validateCashLimit(ctx);
        }
        if (isValidationPassing(ctx)) {
            validateLimitRules(ctx);
        }
        if (isValidationPassing(ctx)) {
            validateOpenDate(ctx);
        }
        if (isValidationPassing(ctx)) {
            validateGroupId(ctx);
        }
        if (isValidationPassing(ctx)) {
            validateCustomerExists(ctx);
        }
        if (isValidationPassing(ctx)) {
            validateAccountUnique(ctx);
        }
    }

    /** COBOL paragraph: 5010-VAL-ACCT */
    private void validateAccountId(TaskContext ctx) {
        ctx.f.setWsNcIn(ctx.f.getAcctidi());
        ctx.f.setWsNcLen(11);
        parseNumericField(ctx);
        if (ctx.f.getWsValidSw().equals("N") || ctx.f.getWsNcDigits() > 11) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMAcctNum());
        } else {
            ctx.f.setWsNewAcct(ctx.f.getWsNcValue());
        }
    }

    /** COBOL paragraph: 5020-VAL-CUST */
    private void validateCustomerId(TaskContext ctx) {
        ctx.f.setWsNcIn(ctx.f.getCustidi());
        ctx.f.setWsNcLen(9);
        parseNumericField(ctx);
        if (ctx.f.getWsValidSw().equals("N") || ctx.f.getWsNcDigits() > 9) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMCustNum());
        } else {
            ctx.f.setWsNewCust(Utility.toCobolInt(ctx.f.getWsNcValue(), 9));
        }
    }

    /** COBOL paragraph: 5030-VAL-CRLIM */
    private void validateCreditLimit(TaskContext ctx) {
        ctx.f.setWsAeIn(ctx.f.getAccrlimi());
        parseAmountField(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            ctx.f.setErrmsgo(ctx.f.getWsMCrlimBad());
        } else {
            ctx.f.setWsNewCrlim(ctx.f.getWsAeResult());
        }
    }

    /** COBOL paragraph: 5040-VAL-CSLIM */
    private void validateCashLimit(TaskContext ctx) {
        ctx.f.setWsAeIn(ctx.f.getAccslimi());
        parseAmountField(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            ctx.f.setErrmsgo(ctx.f.getWsMCslimBad());
        } else {
            ctx.f.setWsNewCslim(ctx.f.getWsAeResult());
        }
    }

    /** COBOL paragraph: 5050-VAL-RULES */
    private void validateLimitRules(TaskContext ctx) {
        if ((ctx.f.getWsNewCslim().compareTo(ctx.f.getWsNewCrlim()) > 0)) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMCsGtCr());
        }
    }

    /** COBOL paragraph: 5060-VAL-OPEN */
    private void validateOpenDate(TaskContext ctx) {
        ctx.f.setWsNcIn(ctx.f.getAcopeni());
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(
                Utility.padRight(String.valueOf(ctx.f.getWsNcIn()), 10).substring(0, 10), " ")) {
            ctx.f.setKdFunc("TODY");
            ctx.f.setKdDateIn(" ");
            ctx.f.setKdDateOut(" ");
            ctx.f.setKdStatus("00");
            Object[] _ca0 = new Object[] {ctx.f.getKdateParm()};
            ctx.appService.callProgram("OUDATE", _ca0);
            ctx.f.setKdateParm(String.valueOf(_ca0[0]));
            ctx.f.setWsNewOpen(ctx.f.getKdDateOut());
        } else {
            ctx.f.setKdFunc("VALD");
            ctx.f.setKdDateIn(ctx.f.getAcopeni());
            ctx.f.setKdDateOut(" ");
            ctx.f.setKdStatus("00");
            Object[] _ca1 = new Object[] {ctx.f.getKdateParm()};
            ctx.appService.callProgram("OUDATE", _ca1);
            ctx.f.setKdateParm(String.valueOf(_ca1[0]));
            if (Utility.fieldEquals(ctx.f.getKdStatus(), "00")) {
                ctx.f.setWsNewOpen(ctx.f.getAcopeni());
            } else {
                ctx.f.setString("WS-VALID-SW", "N");
                ctx.f.setErrmsgo(ctx.f.getWsMOpenBad());
            }
        }
    }

    /** COBOL paragraph: 5070-VAL-GROUP */
    private void validateGroupId(TaskContext ctx) {
        ctx.f.setWsNcIn(ctx.f.getAcgrpi());
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(
                Utility.padRight(String.valueOf(ctx.f.getWsNcIn()), 10).substring(0, 10), " ")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMGroupReq());
        } else {
            ctx.f.setWsNewGroup(ctx.f.getAcgrpi());
        }
    }

    /** COBOL paragraph: 5080-VAL-CUST-EXISTS */
    private void validateCustomerExists(TaskContext ctx) {
        readCustomerRecord(ctx);
        if (ctx.f.getWsFoundFlg().equals("N")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMCustNf());
        }
    }

    /** COBOL paragraph: 5090-VAL-ACCT-UNIQUE */
    private void validateAccountUnique(TaskContext ctx) {
        readAccountRecord(ctx);
        if (ctx.f.getWsFoundFlg().equals("Y")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMAcctDup());
        }
    }

    /** COBOL paragraph: 5100-PARSE-AMOUNT */
    private void parseAmountField(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsAeInt(0);
        ctx.f.setWsAeIntCnt(0);
        ctx.f.setWsAeFrac(0);
        ctx.f.setWsAeFracCnt(0);
        ctx.f.setWsAeDotSw("N");
        ctx.f.setWsAeResult(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsAeIn(
                String.valueOf(ctx.f.getWsAeIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getWsAeIn(), " ")) {
            ctx.f.setString("WS-VALID-SW", "N");
            return;
        }
        for (ctx.f.setWsAePos(1);
                ctx.f.getWsAePos() <= 13;
                ctx.f.setWsAePos(ctx.f.getWsAePos() + 1)) {
            ctx.f.setWsAeChar(
                    Utility.padRight(String.valueOf(ctx.f.getWsAeIn()), FIELD_SCAN_BUFFER_LENGTH)
                            .substring(ctx.f.getWsAePos() - 1, ctx.f.getWsAePos() - 1 + 1));
            if (Utility.fieldEquals(ctx.f.getWsAeChar(), " ")) {
                /* CONTINUE */
            } else if (Utility.fieldEquals(ctx.f.getWsAeChar(), ",")) {
                /* CONTINUE */
            } else if (Utility.fieldEquals(ctx.f.getWsAeChar(), ".")) {
                if (Utility.fieldEquals(ctx.f.getWsAeDotSw(), "Y")) {
                    ctx.f.setString("WS-VALID-SW", "N");
                } else {
                    ctx.f.setWsAeDotSw("Y");
                }
            } else if ((ctx.f.getWsAeChar().compareTo("0") >= 0)
                    && (ctx.f.getWsAeChar().compareTo("9") <= 0)) {
                accumulateDigit(ctx);
            } else {
                ctx.f.setString("WS-VALID-SW", "N");
            }
        }
        if (ctx.f.getWsAeFracCnt() == 1) {
            ctx.f.setWsAeFrac((ctx.f.getWsAeFrac() * 10));
        }
        ctx.f.setWsAeResult(
                java.math.BigDecimal.valueOf(ctx.f.getWsAeInt())
                        .add(
                                java.math.BigDecimal.valueOf(ctx.f.getWsAeFrac())
                                        .divide(
                                                java.math.BigDecimal.valueOf(100),
                                                12,
                                                java.math.RoundingMode.HALF_UP)));
    }

    /** COBOL paragraph: 5150-ACCUM-DIGIT */
    private void accumulateDigit(TaskContext ctx) {
        ctx.f.setWsAeDigit(Utility.parseNumeric(ctx.f.getWsAeChar()).intValue());
        if (Utility.fieldEquals(ctx.f.getWsAeDotSw(), "Y")) {
            ctx.f.setWsAeFracCnt(ctx.f.getWsAeFracCnt() + 1);
            if (ctx.f.getWsAeFracCnt() > 2) {
                ctx.f.setString("WS-VALID-SW", "N");
            } else {
                ctx.f.setWsAeFrac(((ctx.f.getWsAeFrac() * 10) + ctx.f.getWsAeDigit()));
            }
        } else {
            ctx.f.setWsAeIntCnt(ctx.f.getWsAeIntCnt() + 1);
            if (ctx.f.getWsAeIntCnt() > 10) {
                ctx.f.setString("WS-VALID-SW", "N");
            } else {
                ctx.f.setWsAeInt(((ctx.f.getWsAeInt() * (long) 10) + ctx.f.getWsAeDigit()));
            }
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
                    Utility.padRight(String.valueOf(ctx.f.getWsNcIn()), FIELD_SCAN_BUFFER_LENGTH)
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
        ctx.appService.xctl(
                ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8000-POPULATE-HEADER */
    private void populateHeaderFields(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        fetchCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnlyScreen(TaskContext ctx) {
        populateHeaderFields(ctx);
        ctx.appService.sendMap(
                "MACCTAA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8500-GET-DATE-TIME */
    private void fetchCurrentDateTime(TaskContext ctx) {
        ctx.appService.askTime();
        com.appruntime.FormatTimeResult _ftResult =
                ctx.appService.formatTime(null, "YYYYMMDD", "-", ":");
        ctx.f.setWsHdrDate(_ftResult.getDate());
        ctx.f.setWsHdrTime(_ftResult.getTime());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 9000-RETURN */
    private void returnToCics(TaskContext ctx) {
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /** COBOL paragraph: 9500-ABEND-RTN */
    private void abendWithFileError(TaskContext ctx) {
        ctx.f.setWsMsgText("OCACCTA: unrecoverable file error. Contact support.");
        ctx.appService.sendText(String.valueOf(ctx.f), true, true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.appService.returnProgram();
    }

    /** Whether the running validation pass has not yet failed. */
    private boolean isValidationPassing(TaskContext ctx) {
        return ctx.f.getWsValidSw().equals("Y");
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcacctaFields f;

        final AppService appService;

        final OcacctaLinkParm link = new OcacctaLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcacctaFields(ws);
        }
    }
}

package com.generated.orion.ocacctu.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcacctuLinkParm;
import com.generated.orion.ocacctu.accessor.OcacctuFields;
import com.generated.orion.ocacctu.metadata.OcacctuBmsMetadata;
import com.generated.orion.ocacctu.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCACCTU. */
@Service
public class OcacctuService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcacctuService.class);

    /** Safe padding length used before substring extraction on variable-length input fields. */
    private static final int PAD_LENGTH_FIELD_BUFFER = 256;

    /** Length in bytes of the ORION-COMMAREA passed across program transfers. */
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
        return "OCACCTU";
    }

    @Override
    public String getTransId() {
        return "ORAU";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcacctuBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcacctuBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcacctuBmsMetadata.getFieldMapping(mapName);
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
                dispatchInputByAid(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MACCTUAO");
        populateScreenHeader(ctx);
        ctx.f.setCaWorkArea(
                Utility.setSubstring(
                        String.valueOf(ctx.f.getCaWorkArea()),
                        1,
                        1,
                        String.valueOf(ctx.f.getWsStKey())));
        ctx.f.setErrmsgo("Enter account id and press ENTER.");
        ctx.appService.sendMap(
                "MACCTUA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void dispatchInputByAid(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            xctlToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "@")) {
            xctlToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "_")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "5")) {
            saveAccountChanges(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            processEnterKey(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2050-RECEIVE */
    private void receiveScreenInput(TaskContext ctx) {
        ctx.appService.receiveMap("MACCTUA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MACCTUAI");
        }
    }

    /** COBOL paragraph: 2100-ENTER */
    private void processEnterKey(TaskContext ctx) {
        receiveScreenInput(ctx);
        switch (Utility.rtrim(
                Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 1).substring(0, 1))) {
            case "E" -> {
                validateOnlyMode(ctx);
            }
            default -> {
                lookupAccount(ctx);
            }
        }
    }

    /** COBOL paragraph: 2200-LOOKUP */
    private void lookupAccount(TaskContext ctx) {
        validateAndParseAcctId(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            sendDataOnlyScreen(ctx);
        } else {
            ctx.f.setAcId(ctx.f.getWsNcValue());
            ctx.f.setCaAcctId(ctx.f.getAcId());
            readAccountRecord(ctx);
            if (ctx.f.getWsFoundFlg().equals("Y")) {
                populateAccountDetailScreen(ctx);
                ctx.f.setCaWorkArea(
                        Utility.setSubstring(
                                String.valueOf(ctx.f.getCaWorkArea()),
                                1,
                                1,
                                String.valueOf(ctx.f.getWsStEdit())));
                ctx.f.setErrmsgo("Account found. Change fields, PF5 to save.");
                sendDataOnlyScreen(ctx);
            } else {
                ctx.f.setErrmsgo(ctx.f.getWsMsgNotfnd());
                sendDataOnlyScreen(ctx);
            }
        }
    }

    /** COBOL paragraph: 2250-VALIDATE-ONLY */
    private void validateOnlyMode(TaskContext ctx) {
        validateAllFields(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            sendDataOnlyScreen(ctx);
        } else {
            ctx.f.setErrmsgo("Changes are valid.  Press PF5 to confirm save.");
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2300-SAVE */
    private void saveAccountChanges(TaskContext ctx) {
        if (!Utility.fieldEquals(
                Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 1).substring(0, 1),
                ctx.f.getWsStEdit())) {
            ctx.f.setErrmsgo("Enter an account id and press ENTER first.");
            sendDataOnlyScreen(ctx);
        } else {
            receiveScreenInput(ctx);
            validateAllFields(ctx);
            if (ctx.f.getWsValidSw().equals("N")) {
                sendDataOnlyScreen(ctx);
            } else {
                updateAccountRecord(ctx);
                if (ctx.f.getWsUpdSw().equals("Y")) {
                    populateAccountDetailScreen(ctx);
                }
                sendDataOnlyScreen(ctx);
            }
        }
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
            abendWithFileError(ctx);
        }
    }

    /** COBOL paragraph: 3500-UPDATE-ACCT */
    private void updateAccountRecord(TaskContext ctx) {
        ctx.f.setString("WS-UPD-SW", "N");
        ctx.f.setAcId(ctx.f.getCaAcctId());
        ctx.appService.readFileForUpdate(
                ctx.f.getWsAcctfile(), ctx.f, String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            applyAccountUpdate(ctx);
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setErrmsgo("Account no longer exists. Update aborted.");
        } else {
            abendWithFileError(ctx);
        }
    }

    /** COBOL paragraph: 3600-APPLY-UPDATE */
    private void applyAccountUpdate(TaskContext ctx) {
        if ((ctx.f.getWsNewCrlim().compareTo(ctx.f.getAcCurrBal()) < 0)) {
            ctx.f.setErrmsgo("Credit limit below current balance. Not saved.");
            /* EXEC CICS UNLOCK — file unlock */ ;
            return;
        }
        ctx.f.setAcActiveStatus(ctx.f.getWsNewStatus());
        ctx.f.setAcCreditLimit(ctx.f.getWsNewCrlim());
        ctx.f.setAcCashLimit(ctx.f.getWsNewCslim());
        ctx.f.setAcExpiryDate(ctx.f.getWsNewExpiry());
        ctx.f.setAcGroupId(ctx.f.getWsNewGroup());
        ctx.appService.rewriteFile(ctx.f.getWsAcctfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-UPD-SW", "Y");
            ctx.f.setErrmsgo("Account updated successfully.");
        } else {
            ctx.f.setErrmsgo("Update failed during REWRITE.");
        }
    }

    /** COBOL paragraph: 4000-POPULATE-DETAIL */
    private void populateAccountDetailScreen(TaskContext ctx) {
        ctx.f.setAcctido(String.format("%011d", ctx.f.getAcId()));
        ctx.f.setAcstato(ctx.f.getAcActiveStatus());
        ctx.f.setWsEdAmt(ctx.f.getAcCreditLimit());
        ctx.f.setAccrlimo(
                Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-AMT")), 16)
                        .substring(3, 16));
        ctx.f.setWsEdAmt(ctx.f.getAcCashLimit());
        ctx.f.setAccslimo(
                Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-AMT")), 16)
                        .substring(3, 16));
        ctx.f.setAcexpo(ctx.f.getAcExpiryDate());
        ctx.f.setAcgrpo(ctx.f.getAcGroupId());
    }

    /** COBOL paragraph: 5000-VALIDATE-ALL */
    private void validateAllFields(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setErrmsgo(" ");
        if (!isValidSoFar(ctx)) return;
        validateActiveStatus(ctx);
        if (!isValidSoFar(ctx)) return;
        validateCreditLimit(ctx);
        if (!isValidSoFar(ctx)) return;
        validateCashLimit(ctx);
        if (!isValidSoFar(ctx)) return;
        validateLimitRules(ctx);
        if (!isValidSoFar(ctx)) return;
        validateExpiryDate(ctx);
        if (!isValidSoFar(ctx)) return;
        validateGroupId(ctx);
    }

    /** COBOL paragraph: 5010-VAL-STATUS */
    private void validateActiveStatus(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getAcstati(), "Y")
                || Utility.fieldEquals(ctx.f.getAcstati(), "N")) {
            ctx.f.setWsNewStatus(ctx.f.getAcstati());
        } else {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo("Active status must be Y or N.");
        }
    }

    /** COBOL paragraph: 5020-VAL-CRLIM */
    private void validateCreditLimit(TaskContext ctx) {
        ctx.f.setWsAeIn(ctx.f.getAccrlimi());
        parseAmountField(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            ctx.f.setErrmsgo("Credit limit is not a valid amount.");
        } else {
            ctx.f.setWsNewCrlim(ctx.f.getWsAeResult());
        }
    }

    /** COBOL paragraph: 5030-VAL-CSLIM */
    private void validateCashLimit(TaskContext ctx) {
        ctx.f.setWsAeIn(ctx.f.getAccslimi());
        parseAmountField(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            ctx.f.setErrmsgo("Cash limit is not a valid amount.");
        } else {
            ctx.f.setWsNewCslim(ctx.f.getWsAeResult());
        }
    }

    /** COBOL paragraph: 5040-VAL-RULES */
    private void validateLimitRules(TaskContext ctx) {
        if ((ctx.f.getWsNewCslim().compareTo(ctx.f.getWsNewCrlim()) > 0)) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo("Cash limit cannot exceed credit limit.");
        }
    }

    /** COBOL paragraph: 5050-VAL-EXPIRY */
    private void validateExpiryDate(TaskContext ctx) {
        ctx.f.setKdFunc("VALD");
        ctx.f.setKdDateIn(ctx.f.getAcexpi());
        ctx.f.setKdDateOut(" ");
        ctx.f.setKdStatus("00");
        Object[] _ca0 = new Object[] {ctx.f.getKdateParm()};
        ctx.appService.callProgram("OUDATE", _ca0);
        ctx.f.setKdateParm(String.valueOf(_ca0[0]));
        if (Utility.fieldEquals(ctx.f.getKdStatus(), "00")) {
            ctx.f.setWsNewExpiry(ctx.f.getAcexpi());
        } else {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo("Expiry date invalid, use YYYY-MM-DD.");
        }
    }

    /** COBOL paragraph: 5060-VAL-GROUP */
    private void validateGroupId(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getAcgrpi(), " ") || ctx.f.isAllLowValues("ACGRPI")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo("Group id is required.");
        } else {
            ctx.f.setWsNewGroup(ctx.f.getAcgrpi());
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
                    Utility.padRight(String.valueOf(ctx.f.getWsAeIn()), PAD_LENGTH_FIELD_BUFFER)
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
                accumulateAmountDigit(ctx);
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
    private void accumulateAmountDigit(TaskContext ctx) {
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

    /** COBOL paragraph: 6000-VALIDATE-ACCTID */
    private void validateAndParseAcctId(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsNcIn(ctx.f.getAcctidi());
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getWsNcIn(), " ")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMsgRequired());
            return;
        }
        ctx.f.setWsNcValue(0);
        ctx.f.setWsNcDigits(0);
        for (ctx.f.setWsNcPos(1);
                ctx.f.getWsNcPos() <= 11;
                ctx.f.setWsNcPos(ctx.f.getWsNcPos() + 1)) {
            ctx.f.setWsNcChar(
                    Utility.padRight(String.valueOf(ctx.f.getWsNcIn()), PAD_LENGTH_FIELD_BUFFER)
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
        if (ctx.f.getWsValidSw().equals("N") || ctx.f.getWsNcDigits() == 0) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo("Account id must be numeric.");
        }
    }

    /** COBOL paragraph: 7000-XCTL-MENU */
    private void xctlToMenuProgram(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(
                ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8000-POPULATE-HEADER */
    private void populateScreenHeader(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        fetchCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnlyScreen(TaskContext ctx) {
        populateScreenHeader(ctx);
        ctx.appService.sendMap(
                "MACCTUA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
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
        ctx.f.setWsMsgText("OCACCTU: unrecoverable file error. Contact support.");
        ctx.appService.sendText(String.valueOf(ctx.f), true, true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.appService.returnProgram();
    }

    /** True while the running validation chain has not yet failed. */
    private boolean isValidSoFar(TaskContext ctx) {
        return ctx.f.getWsValidSw().equals("Y");
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcacctuFields f;

        final AppService appService;

        final OcacctuLinkParm link = new OcacctuLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcacctuFields(ws);
        }
    }
}

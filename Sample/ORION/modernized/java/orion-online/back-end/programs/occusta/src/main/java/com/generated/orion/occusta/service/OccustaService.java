package com.generated.orion.occusta.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OccustaLinkParm;
import com.generated.orion.occusta.accessor.OccustaFields;
import com.generated.orion.occusta.metadata.OccustaBmsMetadata;
import com.generated.orion.occusta.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCCUSTA. */
@Service
public class OccustaService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OccustaService.class);

    private static final int COMMAREA_LENGTH = 692;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        processMainLine(ctx);
    }

    @Override
    public String getProgramName() {
        return "OCCUSTA";
    }

    @Override
    public String getTransId() {
        return "OROC";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OccustaBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OccustaBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OccustaBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void processMainLine(TaskContext ctx) {
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
                dispatchInputAction(ctx);
            }
        }
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MCUSTAAO");
        populateScreenHeader(ctx);
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setCustidl((short) -1);
        ctx.appService.sendMap(
                "MCUSTAA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void dispatchInputAction(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            addCustomer(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnly(ctx);
        }
    }

    /** COBOL paragraph: 2100-ADD-CUST */
    private void addCustomer(TaskContext ctx) {
        ctx.appService.receiveMap("MCUSTAA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MCUSTAAI");
        }
        validateAllFields(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            ctx.f.setCustidl((short) -1);
            sendDataOnly(ctx);
        } else {
            writeCustomerRecord(ctx);
            if (ctx.f.getWsWriteSw().equals("Y")) {
                ctx.f.setCaCustId(ctx.f.getWsNewCust());
                sendInitialScreen(ctx);
                ctx.f.setErrmsgo(ctx.f.getWsMOk());
                sendDataOnly(ctx);
            } else {
                ctx.f.setCustidl((short) -1);
                sendDataOnly(ctx);
            }
        }
    }

    /** COBOL paragraph: 3500-WRITE-CUST */
    private void writeCustomerRecord(TaskContext ctx) {
        ctx.f.setString("WS-WRITE-SW", "N");
        ctx.f.setCustRec("");
        ctx.f.setCuId(ctx.f.getWsNewCust());
        ctx.f.setCuFirstName(ctx.f.getCufnami());
        ctx.f.setCuMiddleName(" ");
        ctx.f.setCuLastName(ctx.f.getCulnami());
        ctx.f.setCuAddrLine1(ctx.f.getCuaddri());
        ctx.f.setCuAddrLine2(" ");
        ctx.f.setCuAddrCity(ctx.f.getCucityi());
        ctx.f.setCuAddrState(" ");
        ctx.f.setCuAddrCountry(" ");
        ctx.f.setCuAddrZip(" ");
        ctx.f.setCuPhone1(" ");
        ctx.f.setCuPhone2(" ");
        ctx.f.setCuSsn(ctx.f.getWsNewSsn());
        ctx.f.setCuGovtId(" ");
        ctx.f.setCuDob(" ");
        ctx.f.setCuFicoScore(ctx.f.getWsNewFico());
        ctx.appService.writeFile(ctx.f.getWsCustfile(), ctx.f, String.valueOf(ctx.f.getCuId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-WRITE-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 14) {
            ctx.f.setErrmsgo(ctx.f.getWsMCustDup());
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMWriteErr());
        }
    }

    /** COBOL paragraph: 5000-VALIDATE-ALL */
    private void validateAllFields(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setErrmsgo(" ");
        if (isValidationPassed(ctx)) {
            validateCustomerId(ctx);
        }
        if (isValidationPassed(ctx)) {
            validateFirstName(ctx);
        }
        if (isValidationPassed(ctx)) {
            validateLastName(ctx);
        }
        if (isValidationPassed(ctx)) {
            validateAddress(ctx);
        }
        if (isValidationPassed(ctx)) {
            validateCity(ctx);
        }
        if (isValidationPassed(ctx)) {
            validateSsn(ctx);
        }
        if (isValidationPassed(ctx)) {
            validateFicoScore(ctx);
        }
    }

    /** COBOL paragraph: 5010-VAL-CUST */
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

    /** COBOL paragraph: 5020-VAL-FNAME */
    private void validateFirstName(TaskContext ctx) {
        ctx.f.setWsNcIn(ctx.f.getCufnami());
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getCufnami(), " ") || ctx.f.isAllLowValues("CUFNAMI")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMFnameReq());
        }
    }

    /** COBOL paragraph: 5030-VAL-LNAME */
    private void validateLastName(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getCulnami(), " ") || ctx.f.isAllLowValues("CULNAMI")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMLnameReq());
        }
    }

    /** COBOL paragraph: 5040-VAL-ADDR */
    private void validateAddress(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getCuaddri(), " ") || ctx.f.isAllLowValues("CUADDRI")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMAddrReq());
        }
    }

    /** COBOL paragraph: 5050-VAL-CITY */
    private void validateCity(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getCucityi(), " ") || ctx.f.isAllLowValues("CUCITYI")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMCityReq());
        }
    }

    /** COBOL paragraph: 5060-VAL-SSN */
    private void validateSsn(TaskContext ctx) {
        ctx.f.setWsNcIn(ctx.f.getCussni());
        ctx.f.setWsNcLen(9);
        parseNumericField(ctx);
        if (ctx.f.getWsValidSw().equals("N") || ctx.f.getWsNcDigits() != 9) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMSsnNum());
        } else {
            ctx.f.setWsNewSsn(Utility.toCobolInt(ctx.f.getWsNcValue(), 9));
        }
    }

    /** COBOL paragraph: 5070-VAL-FICO */
    private void validateFicoScore(TaskContext ctx) {
        ctx.f.setWsNcIn(ctx.f.getCuficoi());
        ctx.f.setWsNcLen(3);
        parseNumericField(ctx);
        if (ctx.f.getWsValidSw().equals("N") || ctx.f.getWsNcDigits() > 3) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMFicoNum());
        } else {
            ctx.f.setWsNewFico(Utility.toCobolInt(ctx.f.getWsNcValue(), 3));
            if (ctx.f.getWsNewFico() < 300 || ctx.f.getWsNewFico() > 850) {
                ctx.f.setString("WS-VALID-SW", "N");
                ctx.f.setErrmsgo(ctx.f.getWsMFicoRng());
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
        ctx.appService.xctl(
                ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8000-POPULATE-HEADER */
    private void populateScreenHeader(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        populateCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnly(TaskContext ctx) {
        populateScreenHeader(ctx);
        ctx.appService.sendMap(
                "MCUSTAA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8500-GET-DATE-TIME */
    private void populateCurrentDateTime(TaskContext ctx) {
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

    /** Check whether validation has not yet failed, so the next validation step should run. */
    private boolean isValidationPassed(TaskContext ctx) {
        return ctx.f.getWsValidSw().equals("Y");
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OccustaFields f;

        final AppService appService;

        final OccustaLinkParm link = new OccustaLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OccustaFields(ws);
        }
    }
}

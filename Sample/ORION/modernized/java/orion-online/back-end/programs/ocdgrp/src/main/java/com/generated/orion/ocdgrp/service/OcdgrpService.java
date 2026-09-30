package com.generated.orion.ocdgrp.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcdgrpLinkParm;
import com.generated.orion.ocdgrp.accessor.OcdgrpFields;
import com.generated.orion.ocdgrp.metadata.OcdgrpBmsMetadata;
import com.generated.orion.ocdgrp.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCDGRP. */
@Service
public class OcdgrpService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcdgrpService.class);

    /** Fixed pad length used when scanning numeric/rate input fields character-by-character. */
    private static final int PAD_LENGTH_PARSE_BUFFER = 256;

    /** Serialized length of the ORION-COMMAREA passed across program transfers. */
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
        return "OCDGRP";
    }

    @Override
    public String getTransId() {
        return "ORDG";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcdgrpBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcdgrpBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcdgrpBmsMetadata.getFieldMapping(mapName);
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
                processInputCommand(ctx);
            }
        }
        returnToTransaction(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MDGRPAO");
        populateScreenHeader(ctx);
        setWorkAreaStatus(ctx, String.valueOf(ctx.f.getWsStKey()));
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setDggrpl((short) -1);
        ctx.appService.sendMap(
                "MDGRPA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInputCommand(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            xctlToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "5")) {
            saveDiscountGroup(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            lookupDiscountGroup(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2050-RECEIVE */
    private void receiveScreenInput(TaskContext ctx) {
        ctx.appService.receiveMap("MDGRPA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MDGRPAI");
        }
    }

    /** COBOL paragraph: 2100-LOOKUP */
    private void lookupDiscountGroup(TaskContext ctx) {
        receiveScreenInput(ctx);
        validateGroupKey(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            ctx.f.setDggrpl((short) -1);
            sendDataOnlyScreen(ctx);
        } else {
            readDiscountGroup(ctx);
            ctx.f.setDggrpo(ctx.f.getWsDgGroup());
            ctx.f.setDgtypeo(ctx.f.getWsDgType());
            ctx.f.setWsCatEd(ctx.f.getWsDgCat());
            ctx.f.setDgcato(String.format("%04d", ctx.f.getWsCatEd()));
            if (ctx.f.getWsFoundFlg().equals("Y")) {
                ctx.f.setWsRateEd(ctx.f.getDgIntRate());
                ctx.f.setDgrateo(ctx.f.getString("WS-RATE-ED"));
                setWorkAreaStatus(ctx, String.valueOf(ctx.f.getWsStExist()));
                ctx.f.setErrmsgo(ctx.f.getWsMFound());
            } else {
                ctx.f.setDgrateo(" ");
                setWorkAreaStatus(ctx, String.valueOf(ctx.f.getWsStNew()));
                ctx.f.setErrmsgo(ctx.f.getWsMNew());
            }
            ctx.f.setDgratel((short) -1);
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2300-SAVE */
    private void saveDiscountGroup(TaskContext ctx) {
        String workAreaStatus =
                Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 1).substring(0, 1);
        if (!Utility.fieldEquals(workAreaStatus, ctx.f.getWsStExist())
                && !Utility.fieldEquals(workAreaStatus, ctx.f.getWsStNew())) {
            ctx.f.setErrmsgo(ctx.f.getWsMKeyFirst());
            ctx.f.setDggrpl((short) -1);
            sendDataOnlyScreen(ctx);
            return;
        }
        receiveScreenInput(ctx);
        validateGroupKey(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            ctx.f.setDggrpl((short) -1);
            sendDataOnlyScreen(ctx);
            return;
        }
        validateInterestRate(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            ctx.f.setDgratel((short) -1);
            sendDataOnlyScreen(ctx);
            return;
        }
        if (Utility.fieldEquals(workAreaStatus, ctx.f.getWsStExist())) {
            updateDiscountGroup(ctx);
        } else {
            addDiscountGroup(ctx);
        }
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 3000-READ-DGRP */
    private void readDiscountGroup(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.f.setDgAcctGroup(ctx.f.getWsDgGroup());
        ctx.f.setDgTypeCd(ctx.f.getWsDgType());
        ctx.f.setDgCatCd(ctx.f.getWsDgCat());
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
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-FOUND-FLG", "N");
        } else {
            handleAbend(ctx);
        }
    }

    /** COBOL paragraph: 3500-UPDATE-DGRP */
    private void updateDiscountGroup(TaskContext ctx) {
        ctx.f.setDgAcctGroup(ctx.f.getWsDgGroup());
        ctx.f.setDgTypeCd(ctx.f.getWsDgType());
        ctx.f.setDgCatCd(ctx.f.getWsDgCat());
        ctx.appService.readFileForUpdate(
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
            ctx.f.setDgIntRate(ctx.f.getWsNewRate());
            ctx.appService.rewriteFile(ctx.f.getWsDgrpfile(), ctx.f);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
            if (ctx.f.getWsRespCd() == 0) {
                ctx.f.setErrmsgo(ctx.f.getWsMUpdated());
            } else {
                ctx.f.setErrmsgo(ctx.f.getWsMSaveErr());
            }
        } else if (ctx.f.getWsRespCd() == 13) {
            addDiscountGroup(ctx);
        } else {
            handleAbend(ctx);
        }
    }

    /** COBOL paragraph: 3600-ADD-DGRP */
    private void addDiscountGroup(TaskContext ctx) {
        ctx.f.setDgrpRec("");
        ctx.f.setDgAcctGroup(ctx.f.getWsDgGroup());
        ctx.f.setDgTypeCd(ctx.f.getWsDgType());
        ctx.f.setDgCatCd(ctx.f.getWsDgCat());
        ctx.f.setDgIntRate(ctx.f.getWsNewRate());
        ctx.appService.writeFile(
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
            ctx.f.setErrmsgo(ctx.f.getWsMAdded());
            setWorkAreaStatus(ctx, String.valueOf(ctx.f.getWsStExist()));
        } else if (ctx.f.getWsRespCd() == 14) {
            ctx.f.setErrmsgo(ctx.f.getWsMSaveErr());
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMSaveErr());
        }
    }

    /** COBOL paragraph: 6100-VALIDATE-KEY */
    private void validateGroupKey(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setErrmsgo(" ");
        if (Utility.fieldEquals(ctx.f.getDggrpi(), " ") || ctx.f.isAllLowValues("DGGRPI")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMGrpReq());
            _6100Exit(ctx);
            return;
        }
        ctx.f.setWsDgGroup(ctx.f.getDggrpi());
        ctx.f.setWsDgGroup(
                String.valueOf(ctx.f.getWsDgGroup())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getDgtypei(), " ") || ctx.f.isAllLowValues("DGTYPEI")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMTypeReq());
            _6100Exit(ctx);
            return;
        }
        ctx.f.setWsDgType(ctx.f.getDgtypei());
        ctx.f.setWsDgType(
                String.valueOf(ctx.f.getWsDgType())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        ctx.f.setWsNcIn(ctx.f.getDgcati());
        ctx.f.setWsNcLen(4);
        parseNumericField(ctx);
        if (ctx.f.getWsValidSw().equals("N") || ctx.f.getWsNcDigits() > 4) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMCatNum());
        } else {
            ctx.f.setWsDgCat(Utility.toCobolInt(ctx.f.getWsNcValue(), 4));
        }
    }

    /** COBOL paragraph: 6100-EXIT */
    private void _6100Exit(TaskContext ctx) {
        return;
    }

    /** COBOL paragraph: 6200-VALIDATE-RATE */
    private void validateInterestRate(TaskContext ctx) {
        ctx.f.setWsReIn(ctx.f.getDgratei());
        parseRateInput(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            ctx.f.setErrmsgo(ctx.f.getWsMRateBad());
        } else {
            ctx.f.setWsNewRate(ctx.f.getWsReResult());
        }
    }

    /** COBOL paragraph: 6300-PARSE-RATE */
    private void parseRateInput(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsReInt(0);
        ctx.f.setWsReIntCnt(0);
        ctx.f.setWsReFrac(0);
        ctx.f.setWsReFracCnt(0);
        ctx.f.setWsReDotSw("N");
        ctx.f.setWsReResult(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsReIn(
                String.valueOf(ctx.f.getWsReIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getWsReIn(), " ")) {
            ctx.f.setString("WS-VALID-SW", "N");
            _6300Exit(ctx);
            return;
        }
        for (ctx.f.setWsRePos(1);
                ctx.f.getWsRePos() <= 7;
                ctx.f.setWsRePos(ctx.f.getWsRePos() + 1)) {
            ctx.f.setWsReChar(
                    Utility.padRight(String.valueOf(ctx.f.getWsReIn()), PAD_LENGTH_PARSE_BUFFER)
                            .substring(ctx.f.getWsRePos() - 1, ctx.f.getWsRePos() - 1 + 1));
            if (Utility.fieldEquals(ctx.f.getWsReChar(), " ")) {
                /* CONTINUE */
            } else if (Utility.fieldEquals(ctx.f.getWsReChar(), ".")) {
                if (Utility.fieldEquals(ctx.f.getWsReDotSw(), "Y")) {
                    ctx.f.setString("WS-VALID-SW", "N");
                } else {
                    ctx.f.setWsReDotSw("Y");
                }
            } else if ((ctx.f.getWsReChar().compareTo("0") >= 0)
                    && (ctx.f.getWsReChar().compareTo("9") <= 0)) {
                accumulateRateDigit(ctx);
            } else {
                ctx.f.setString("WS-VALID-SW", "N");
            }
        }
        if (ctx.f.getWsReFracCnt() == 1) {
            ctx.f.setWsReFrac((ctx.f.getWsReFrac() * 10));
        }
        ctx.f.setWsReResult(
                java.math.BigDecimal.valueOf(ctx.f.getWsReInt())
                        .add(
                                java.math.BigDecimal.valueOf(ctx.f.getWsReFrac())
                                        .divide(
                                                java.math.BigDecimal.valueOf(100),
                                                12,
                                                java.math.RoundingMode.HALF_UP)));
    }

    /** COBOL paragraph: 6300-EXIT */
    private void _6300Exit(TaskContext ctx) {
        return;
    }

    /** COBOL paragraph: 6350-ACCUM-DIGIT */
    private void accumulateRateDigit(TaskContext ctx) {
        ctx.f.setWsReDigit(Utility.parseNumeric(ctx.f.getWsReChar()).intValue());
        if (Utility.fieldEquals(ctx.f.getWsReDotSw(), "Y")) {
            ctx.f.setWsReFracCnt(ctx.f.getWsReFracCnt() + 1);
            if (ctx.f.getWsReFracCnt() > 2) {
                ctx.f.setString("WS-VALID-SW", "N");
            } else {
                ctx.f.setWsReFrac(((ctx.f.getWsReFrac() * 10) + ctx.f.getWsReDigit()));
            }
        } else {
            ctx.f.setWsReIntCnt(ctx.f.getWsReIntCnt() + 1);
            if (ctx.f.getWsReIntCnt() > 4) {
                ctx.f.setString("WS-VALID-SW", "N");
            } else {
                ctx.f.setWsReInt(((ctx.f.getWsReInt() * 10) + ctx.f.getWsReDigit()));
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
                    Utility.padRight(String.valueOf(ctx.f.getWsNcIn()), PAD_LENGTH_PARSE_BUFFER)
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
                "MDGRPA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
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
    private void returnToTransaction(TaskContext ctx) {
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /** COBOL paragraph: 9500-ABEND-RTN */
    private void handleAbend(TaskContext ctx) {
        ctx.f.setWsMsgText("OCDGRP: unrecoverable file error. Contact support.");
        ctx.appService.sendText(String.valueOf(ctx.f), true, true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.appService.returnProgram();
    }

    /**
     * Store a status marker in position 1 of the work area to track screen state across requests.
     */
    private void setWorkAreaStatus(TaskContext ctx, String statusCode) {
        ctx.f.setCaWorkArea(
                Utility.setSubstring(String.valueOf(ctx.f.getCaWorkArea()), 1, 1, statusCode));
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcdgrpFields f;

        final AppService appService;

        final OcdgrpLinkParm link = new OcdgrpLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcdgrpFields(ws);
        }
    }
}

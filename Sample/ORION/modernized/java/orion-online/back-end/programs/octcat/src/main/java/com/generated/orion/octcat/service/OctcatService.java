package com.generated.orion.octcat.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OctcatLinkParm;
import com.generated.orion.octcat.accessor.OctcatFields;
import com.generated.orion.octcat.metadata.OctcatBmsMetadata;
import com.generated.orion.octcat.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCTCAT. */
@Service
public class OctcatService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OctcatService.class);

    // TODO(layer1): cross-cohort pattern — promote to batch-common Constants
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
        return "OCTCAT";
    }

    @Override
    public String getTransId() {
        return "ORTC";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OctcatBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OctcatBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OctcatBmsMetadata.getFieldMapping(mapName);
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
                processInputByAid(ctx);
            }
        }
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MTCATAO");
        populateHeaderFields(ctx);
        ctx.f.setCaWorkArea(
                Utility.setSubstring(
                        String.valueOf(ctx.f.getCaWorkArea()),
                        1,
                        1,
                        String.valueOf(ctx.f.getWsStKey())));
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setTctypel((short) -1);
        ctx.appService.sendMap(
                "MTCATA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInputByAid(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "5")) {
            saveTypeCode(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            lookupTypeCode(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyMap(ctx);
        }
    }

    /** COBOL paragraph: 2050-RECEIVE */
    private void receiveInputMap(TaskContext ctx) {
        ctx.appService.receiveMap("MTCATA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MTCATAI");
        }
    }

    /** COBOL paragraph: 2100-LOOKUP */
    private void lookupTypeCode(TaskContext ctx) {
        receiveInputMap(ctx);
        validateKeyFields(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            ctx.f.setTctypel((short) -1);
            sendDataOnlyMap(ctx);
        } else {
            readTcatRecord(ctx);
            ctx.f.setTctypeo(ctx.f.getWsTcType());
            ctx.f.setWsCdEd(ctx.f.getWsTcCd());
            ctx.f.setTccdo(String.format("%04d", ctx.f.getWsCdEd()));
            if (ctx.f.getWsFoundFlg().equals("Y")) {
                ctx.f.setTcdesco(ctx.f.getTcDesc());
                ctx.f.setCaWorkArea(
                        Utility.setSubstring(
                                String.valueOf(ctx.f.getCaWorkArea()),
                                1,
                                1,
                                String.valueOf(ctx.f.getWsStExist())));
                ctx.f.setErrmsgo(ctx.f.getWsMFound());
            } else {
                ctx.f.setTcdesco(" ");
                ctx.f.setCaWorkArea(
                        Utility.setSubstring(
                                String.valueOf(ctx.f.getCaWorkArea()),
                                1,
                                1,
                                String.valueOf(ctx.f.getWsStNew())));
                ctx.f.setErrmsgo(ctx.f.getWsMNew());
            }
            ctx.f.setTcdescl((short) -1);
            sendDataOnlyMap(ctx);
        }
    }

    /** COBOL paragraph: 2300-SAVE */
    private void saveTypeCode(TaskContext ctx) {
        String caWorkAreaKey =
                Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 1).substring(0, 1);
        if (!Utility.fieldEquals(caWorkAreaKey, ctx.f.getWsStExist())
                && !Utility.fieldEquals(caWorkAreaKey, ctx.f.getWsStNew())) {
            ctx.f.setErrmsgo(ctx.f.getWsMKeyFirst());
            ctx.f.setTctypel((short) -1);
            sendDataOnlyMap(ctx);
        } else {
            receiveInputMap(ctx);
            validateKeyFields(ctx);
            if (ctx.f.getWsValidSw().equals("N")) {
                ctx.f.setTctypel((short) -1);
                sendDataOnlyMap(ctx);
            } else {
                if (Utility.fieldEquals(ctx.f.getTcdesci(), " ")
                        || ctx.f.isAllLowValues("TCDESCI")) {
                    ctx.f.setErrmsgo(ctx.f.getWsMDescReq());
                    ctx.f.setTcdescl((short) -1);
                    sendDataOnlyMap(ctx);
                } else {
                    if (Utility.fieldEquals(caWorkAreaKey, ctx.f.getWsStExist())) {
                        updateTcatRecord(ctx);
                    } else {
                        addTcatRecord(ctx);
                    }
                    sendDataOnlyMap(ctx);
                }
            }
        }
    }

    /** COBOL paragraph: 3000-READ-TCAT */
    private void readTcatRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.f.setTcTypeCd(ctx.f.getWsTcType());
        ctx.f.setTcCd(ctx.f.getWsTcCd());
        ctx.appService.readFile(
                ctx.f.getWsTcatfile(),
                ctx.f,
                String.valueOf(ctx.f.getTcTypeCd()) + "|" + String.valueOf(ctx.f.getTcCd()),
                0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-FOUND-FLG", "N");
        } else {
            abendWithFileError(ctx);
        }
    }

    /** COBOL paragraph: 3500-UPDATE-TCAT */
    private void updateTcatRecord(TaskContext ctx) {
        ctx.f.setTcTypeCd(ctx.f.getWsTcType());
        ctx.f.setTcCd(ctx.f.getWsTcCd());
        ctx.appService.readFileForUpdate(
                ctx.f.getWsTcatfile(),
                ctx.f,
                String.valueOf(ctx.f.getTcTypeCd()) + "|" + String.valueOf(ctx.f.getTcCd()),
                0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setTcDesc(ctx.f.getTcdesci());
            ctx.appService.rewriteFile(ctx.f.getWsTcatfile(), ctx.f);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
            if (ctx.f.getWsRespCd() == 0) {
                ctx.f.setErrmsgo(ctx.f.getWsMUpdated());
            } else {
                ctx.f.setErrmsgo(ctx.f.getWsMSaveErr());
            }
        } else if (ctx.f.getWsRespCd() == 13) {
            addTcatRecord(ctx);
        } else {
            abendWithFileError(ctx);
        }
    }

    /** COBOL paragraph: 3600-ADD-TCAT */
    private void addTcatRecord(TaskContext ctx) {
        ctx.f.setTcatRec("");
        ctx.f.setTcTypeCd(ctx.f.getWsTcType());
        ctx.f.setTcCd(ctx.f.getWsTcCd());
        ctx.f.setTcDesc(ctx.f.getTcdesci());
        ctx.appService.writeFile(
                ctx.f.getWsTcatfile(),
                ctx.f,
                String.valueOf(ctx.f.getTcTypeCd()) + "|" + String.valueOf(ctx.f.getTcCd()),
                0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setErrmsgo(ctx.f.getWsMAdded());
            ctx.f.setCaWorkArea(
                    Utility.setSubstring(
                            String.valueOf(ctx.f.getCaWorkArea()),
                            1,
                            1,
                            String.valueOf(ctx.f.getWsStExist())));
        } else if (ctx.f.getWsRespCd() == 14) {
            ctx.f.setErrmsgo(ctx.f.getWsMSaveErr());
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMSaveErr());
        }
    }

    /** COBOL paragraph: 6100-VALIDATE-KEY */
    private void validateKeyFields(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setErrmsgo(" ");
        if (Utility.fieldEquals(ctx.f.getTctypei(), " ") || ctx.f.isAllLowValues("TCTYPEI")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMTypeReq());
            return;
        }
        ctx.f.setWsTcType(ctx.f.getTctypei());
        ctx.f.setWsTcType(
                String.valueOf(ctx.f.getWsTcType())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        ctx.f.setWsNcIn(ctx.f.getTccdi());
        ctx.f.setWsNcLen(4);
        parseNumericField(ctx);
        if (ctx.f.getWsValidSw().equals("N") || ctx.f.getWsNcDigits() > 4) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMCdNum());
        } else {
            ctx.f.setWsTcCd(Utility.toCobolInt(ctx.f.getWsNcValue(), 4));
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
    private void populateHeaderFields(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        loadCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnlyMap(TaskContext ctx) {
        populateHeaderFields(ctx);
        ctx.appService.sendMap(
                "MTCATA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
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
    private void abendWithFileError(TaskContext ctx) {
        ctx.f.setWsMsgText("OCTCAT: unrecoverable file error. Contact support.");
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

        final OctcatFields f;

        final AppService appService;

        final OctcatLinkParm link = new OctcatLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OctcatFields(ws);
        }
    }
}

package com.generated.orion.ocrptmn.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcrptmnLinkParm;
import com.generated.orion.ocrptmn.accessor.OcrptmnFields;
import com.generated.orion.ocrptmn.metadata.OcrptmnBmsMetadata;
import com.generated.orion.ocrptmn.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCRPTMN. */
@Service
public class OcrptmnService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcrptmnService.class);

    /** Byte length of the ORION-COMMAREA passed on XCTL / RETURN. */
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
        return "OCRPTMN";
    }

    @Override
    public String getTransId() {
        return "ORRM";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcrptmnBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcrptmnBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcrptmnBmsMetadata.getFieldMapping(mapName);
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
                processInput(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MRPTMNAO");
        populateScreenHeader(ctx);
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setOptionl((short) -1);
        ctx.appService.sendMap(
                "MRPTMNA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            returnToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            dispatchMenuSelection(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-DISPATCH */
    private void dispatchMenuSelection(TaskContext ctx) {
        ctx.appService.receiveMap("MRPTMNA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MRPTMNAI");
        }
        if (Utility.isNumeric(ctx.f.getRawString("OPTIONI"))) {
            ctx.f.setWsOption(Utility.parseNumeric(ctx.f.getOptioni()).intValue());
        } else {
            ctx.f.setWsOption(0);
        }
        selectTargetProgram(ctx);
        if (Utility.fieldEquals(ctx.f.getWsXctlPgm(), " ")) {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMBadOpt());
            sendDataOnlyScreen(ctx);
        } else {
            xctlToSelectedProgram(ctx);
        }
    }

    /** COBOL paragraph: 2200-SELECT-TARGET */
    private void selectTargetProgram(TaskContext ctx) {
        ctx.f.setWsXctlPgm(" ");
        ctx.f.setWsXctlTran(" ");
        switch (ctx.f.getWsOption()) {
            case 1 -> {
                ctx.f.setWsXctlPgm("OCACCTL");
                ctx.f.setWsXctlTran("ORLA");
            }
            case 2 -> {
                ctx.f.setWsXctlPgm("OCCARDL");
                ctx.f.setWsXctlTran("ORCL");
            }
            case 3 -> {
                ctx.f.setWsXctlPgm("OCCUSTL");
                ctx.f.setWsXctlTran("ORLC");
            }
            case 4 -> {
                ctx.f.setWsXctlPgm("OCTRANL");
                ctx.f.setWsXctlTran("ORTL");
            }
            case 5 -> {
                ctx.f.setWsXctlPgm("OCSTMIN");
                ctx.f.setWsXctlTran("ORSI");
            }
            case 6 -> {
                ctx.f.setWsXctlPgm("OCANLIN");
                ctx.f.setWsXctlTran("ORAN");
            }
            default -> {
                ctx.f.setWsXctlPgm(" ");
                ctx.f.setWsXctlTran(" ");
            }
        }
    }

    /** COBOL paragraph: 7000-XCTL-MAIN */
    private void returnToMenuProgram(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(
                ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 7100-XCTL-TARGET */
    private void xctlToSelectedProgram(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaToProgram(ctx.f.getWsXctlPgm());
        ctx.f.setCaToTranid(ctx.f.getWsXctlTran());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(
                ctx.f.getWsXctlPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
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
    private void sendDataOnlyScreen(TaskContext ctx) {
        populateScreenHeader(ctx);
        ctx.appService.sendMap(
                "MRPTMNA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
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
    private void returnToCics(TaskContext ctx) {
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcrptmnFields f;

        final AppService appService;

        final OcrptmnLinkParm link = new OcrptmnLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcrptmnFields(ws);
        }
    }
}

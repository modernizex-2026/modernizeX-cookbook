package com.generated.orion.ocmenu.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcmenuLinkParm;
import com.generated.orion.ocmenu.accessor.OcmenuFields;
import com.generated.orion.ocmenu.metadata.OcmenuBmsMetadata;
import com.generated.orion.ocmenu.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCMENU. */
@Service
public class OcmenuService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcmenuService.class);

    /** Length in bytes of the ORION-COMMAREA passed across program transfers. */
    private static final int COMMAREA_LENGTH = 692;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        executeMainFlow(ctx);
    }

    @Override
    public String getProgramName() {
        return "OCMENU";
    }

    @Override
    public String getTransId() {
        return "ORMN";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcmenuBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcmenuBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcmenuBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void executeMainFlow(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        if (ctx.appService.getEibcalen() == 0) {
            ctx.appService.xctl(ctx.f.getWsSgnonPgm(), null, 0);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        }
        ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
        if (ctx.f.getCaPgmContext() == 0) {
            sendInitialScreen(ctx);
        } else {
            processUserInput(ctx);
        }
        returnTransaction(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MMENUAO");
        populateHeader(ctx);
        ctx.f.setErrmsgo("Select an option (1-10) and press ENTER.");
        ctx.appService.sendMap(
                "MMENUA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processUserInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            ctx.f.setCaPgmContext(0);
            ctx.appService.xctl(
                    ctx.f.getWsSgnonPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            dispatchMenuOption(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-DISPATCH */
    private void dispatchMenuOption(TaskContext ctx) {
        ctx.appService.receiveMap("MMENUA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (Utility.isNumeric(ctx.f.getRawString("OPTIONI"))) {
            ctx.f.setWsOption(Utility.parseNumeric(ctx.f.getOptioni()).intValue());
        } else {
            ctx.f.setWsOption(0);
        }
        switch (ctx.f.getWsOption()) {
            case 1 -> {
                ctx.f.setWsXctlPgm("OCACCTV");
            }
            case 2 -> {
                ctx.f.setWsXctlPgm("OCACCTU");
            }
            case 3 -> {
                ctx.f.setWsXctlPgm("OCCARDL");
            }
            case 4 -> {
                ctx.f.setWsXctlPgm("OCCARDV");
            }
            case 5 -> {
                ctx.f.setWsXctlPgm("OCCARDU");
            }
            case 6 -> {
                ctx.f.setWsXctlPgm("OCCUSTV");
            }
            case 7 -> {
                ctx.f.setWsXctlPgm("OCTRANL");
            }
            case 8 -> {
                ctx.f.setWsXctlPgm("OCTRANA");
            }
            case 9 -> {
                ctx.f.setWsXctlPgm("OCBILLP");
            }
            case 10 -> {
                ctx.f.setWsXctlPgm("OCRPTMN");
            }
            default -> {
                ctx.f.setWsXctlPgm(" ");
            }
        }
        if (Utility.fieldEquals(ctx.f.getWsXctlPgm(), " ")) {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo("Invalid option.");
            sendDataOnlyScreen(ctx);
        } else {
            ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
            ctx.f.setCaPgmContext(0);
            ctx.appService.xctl(
                    ctx.f.getWsXctlPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        }
    }

    /** COBOL paragraph: 8000-POPULATE-HEADER */
    private void populateHeader(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        ctx.appService.askTime();
        com.appruntime.FormatTimeResult _ftResult =
                ctx.appService.formatTime(null, "YYYYMMDD", "-", ":");
        ctx.f.setWsHdrDate(_ftResult.getDate());
        ctx.f.setWsHdrTime(_ftResult.getTime());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnlyScreen(TaskContext ctx) {
        populateHeader(ctx);
        ctx.appService.sendMap(
                "MMENUA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 9000-RETURN */
    private void returnTransaction(TaskContext ctx) {
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcmenuFields f;

        final AppService appService;

        final OcmenuLinkParm link = new OcmenuLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcmenuFields(ws);
        }
    }
}

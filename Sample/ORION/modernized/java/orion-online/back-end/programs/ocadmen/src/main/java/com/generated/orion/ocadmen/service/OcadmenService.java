package com.generated.orion.ocadmen.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcadmenLinkParm;
import com.generated.orion.ocadmen.accessor.OcadmenFields;
import com.generated.orion.ocadmen.metadata.OcadmenBmsMetadata;
import com.generated.orion.ocadmen.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCADMEN. */
@Service
public class OcadmenService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcadmenService.class);

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
        return "OCADMEN";
    }

    @Override
    public String getTransId() {
        return "ORAD";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcadmenBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcadmenBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcadmenBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        if (ctx.appService.getEibcalen() == 0) {
            ctx.appService.xctl(ctx.f.getWsSgnonPgm(), null, 0);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        }
        ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
        ctx.f.setCaErrMsg(" ");
        if (!ctx.f.getCaUserType().equals("A")) {
            xctlToMenuProgram(ctx);
        } else {
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitialScreen(ctx);
            } else {
                processMenuInput(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MADMENAO");
        populateScreenHeader(ctx);
        ctx.f.setErrmsgo("Select an option (1-4) and press ENTER.");
        ctx.appService.sendMap(
                "MADMENA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processMenuInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            signOffToSignonProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
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
        receiveMenuMap(ctx);
        if (Utility.isNumeric(ctx.f.getRawString("OPTIONI"))) {
            ctx.f.setWsOption(Utility.parseNumeric(ctx.f.getOptioni()).intValue());
        } else {
            ctx.f.setWsOption(0);
        }
        switch (ctx.f.getWsOption()) {
            case 1 -> {
                ctx.f.setWsXctlPgm("OCUSRL");
            }
            case 2 -> {
                ctx.f.setWsXctlPgm("OCUSRA");
            }
            case 3 -> {
                ctx.f.setWsXctlPgm("OCUSRU");
            }
            case 4 -> {
                ctx.f.setWsXctlPgm("OCUSRD");
            }
            default -> {
                ctx.f.setWsXctlPgm(" ");
            }
        }
        if (Utility.fieldEquals(ctx.f.getWsXctlPgm(), " ")) {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo("Invalid option. Choose 1 through 4.");
            sendDataOnlyScreen(ctx);
        } else {
            xctlToSelectedProgram(ctx);
        }
    }

    /** COBOL paragraph: 2110-RECEIVE-MAP */
    private void receiveMenuMap(TaskContext ctx) {
        ctx.appService.receiveMap("MADMENA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 2200-XCTL-FUNCTION */
    private void xctlToSelectedProgram(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(
                ctx.f.getWsXctlPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 7000-SIGN-OFF */
    private void signOffToSignonProgram(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(
                ctx.f.getWsSgnonPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 7100-XCTL-MENU */
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
        captureCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnlyScreen(TaskContext ctx) {
        populateScreenHeader(ctx);
        ctx.appService.sendMap(
                "MADMENA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8500-GET-DATE-TIME */
    private void captureCurrentDateTime(TaskContext ctx) {
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

        final OcadmenFields f;

        final AppService appService;

        final OcadmenLinkParm link = new OcadmenLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcadmenFields(ws);
        }
    }
}

package com.generated.orion.ocpwdch.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcpwdchLinkParm;
import com.generated.orion.ocpwdch.accessor.OcpwdchFields;
import com.generated.orion.ocpwdch.metadata.OcpwdchBmsMetadata;
import com.generated.orion.ocpwdch.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCPWDCH. */
@Service
public class OcpwdchService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcpwdchService.class);

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
        return "OCPWDCH";
    }

    @Override
    public String getTransId() {
        return "ORPW";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcpwdchBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcpwdchBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcpwdchBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        if (ctx.appService.getEibcalen() == 0) {
            ctx.appService.xctl(ctx.f.getWsMenuPgm(), null, 0);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        }
        ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
        if (ctx.f.getCaPgmContext() == 0) {
            sendInitialScreen(ctx);
        } else {
            processScreenInput(ctx);
        }
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MPWDCHAO");
        populateHeaderFields(ctx);
        ctx.f.setErrmsgo("Enter user id, old and new password.");
        ctx.appService.sendMap(
                "MPWDCHA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processScreenInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            ctx.f.setCaPgmContext(0);
            ctx.appService.xctl(
                    ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            changePassword(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-CHANGE-PWD */
    private void changePassword(TaskContext ctx) {
        ctx.appService.receiveMap("MPWDCHA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (Utility.fieldEquals(ctx.f.getUseridi(), " ") || ctx.f.isAllLowValues("USERIDI")) {
            ctx.f.setErrmsgo(ctx.f.getWsMsgRequired());
            sendDataOnlyScreen(ctx);
        } else {
            if (!Utility.fieldEquals(ctx.f.getNewpwdi(), ctx.f.getCfmpwdi())) {
                ctx.f.setErrmsgo("New password and confirm do not match.");
                sendDataOnlyScreen(ctx);
            } else {
                ctx.f.setUsId(ctx.f.getUseridi());
                readUserRecord(ctx);
                if (ctx.f.getWsFoundFlg().equals("N")) {
                    ctx.f.setErrmsgo(ctx.f.getWsMsgNotfnd());
                    sendDataOnlyScreen(ctx);
                } else {
                    if (!Utility.fieldEquals(ctx.f.getUsPassword(), ctx.f.getOldpwdi())) {
                        ctx.f.setErrmsgo("Current password is incorrect.");
                        sendDataOnlyScreen(ctx);
                    } else {
                        updatePassword(ctx);
                    }
                }
            }
        }
    }

    /** COBOL paragraph: 3000-READ-USER */
    private void readUserRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.appService.readFileForUpdate(
                ctx.f.getWsUsrsec(), ctx.f, String.valueOf(ctx.f.getUsId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        }
    }

    /** COBOL paragraph: 4000-UPDATE-PWD */
    private void updatePassword(TaskContext ctx) {
        ctx.f.setUsPassword(ctx.f.getNewpwdi());
        ctx.appService.rewriteFile(ctx.f.getWsUsrsec(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setErrmsgo("Password changed successfully.");
        } else {
            ctx.f.setErrmsgo("Password change failed.");
        }
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 8000-POPULATE-HEADER */
    private void populateHeaderFields(TaskContext ctx) {
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
        populateHeaderFields(ctx);
        ctx.appService.sendMap(
                "MPWDCHA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcpwdchFields f;

        final AppService appService;

        final OcpwdchLinkParm link = new OcpwdchLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcpwdchFields(ws);
        }
    }
}

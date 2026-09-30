package com.generated.orion.ocsgnon.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcsgnonLinkParm;
import com.generated.orion.ocsgnon.accessor.OcsgnonFields;
import com.generated.orion.ocsgnon.metadata.OcsgnonBmsMetadata;
import com.generated.orion.ocsgnon.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCSGNON. */
@Service
public class OcsgnonService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcsgnonService.class);

    /** Length in bytes of the ORION-COMMAREA passed on XCTL/RETURN. */
    private static final int COMMAREA_LENGTH = 692;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        executeMainLine(ctx);
    }

    @Override
    public String getProgramName() {
        return "OCSGNON";
    }

    @Override
    public String getTransId() {
        return "ORSN";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcsgnonBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcsgnonBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcsgnonBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void executeMainLine(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
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
        returnTransaction(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MSGNONAO");
        populateHeader(ctx);
        ctx.f.setErrmsgo("Please sign on.");
        ctx.appService.sendMap(
                "MSGNONA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            signOff(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            validateUser(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnly(ctx);
        }
    }

    /** COBOL paragraph: 2100-VALIDATE-USER */
    private void validateUser(TaskContext ctx) {
        ctx.appService.receiveMap("MSGNONA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (Utility.fieldEquals(ctx.f.getUseridi(), " ") || ctx.f.isAllLowValues("USERIDI")) {
            ctx.f.setErrmsgo(ctx.f.getWsMsgRequired());
            sendDataOnly(ctx);
        } else {
            ctx.f.setUsId(ctx.f.getUseridi());
            readUser(ctx);
            if (ctx.f.getWsFoundFlg().equals("N")) {
                ctx.f.setErrmsgo("User not found.");
                sendDataOnly(ctx);
            } else {
                if (Utility.fieldEquals(ctx.f.getUsPassword(), ctx.f.getPasswdi())) {
                    transferToMenu(ctx);
                } else {
                    ctx.f.setErrmsgo("Invalid password.");
                    sendDataOnly(ctx);
                }
            }
        }
    }

    /** COBOL paragraph: 3000-READ-USER */
    private void readUser(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.appService.readFile(ctx.f.getWsUsrsec(), ctx.f, String.valueOf(ctx.f.getUsId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        }
    }

    /** COBOL paragraph: 4000-TRANSFER-TO-MENU */
    private void transferToMenu(TaskContext ctx) {
        ctx.f.setCaUserId(ctx.f.getUsId());
        ctx.f.setCaUserType(ctx.f.getUsType());
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaPgmContext(0);
        if (ctx.f.getCaUserType().equals("A")) {
            ctx.appService.xctl(
                    ctx.f.getWsAdmenPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        } else {
            ctx.appService.xctl(
                    ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        }
    }

    /** COBOL paragraph: 7000-SIGN-OFF */
    private void signOff(TaskContext ctx) {
        ctx.appService.sendText(String.valueOf(ctx.f), true, true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.appService.returnProgram();
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
    private void sendDataOnly(TaskContext ctx) {
        populateHeader(ctx);
        ctx.appService.sendMap(
                "MSGNONA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
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

        final OcsgnonFields f;

        final AppService appService;

        final OcsgnonLinkParm link = new OcsgnonLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcsgnonFields(ws);
        }
    }
}

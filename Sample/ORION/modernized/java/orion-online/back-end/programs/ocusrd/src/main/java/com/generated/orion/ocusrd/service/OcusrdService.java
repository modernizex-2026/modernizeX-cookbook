package com.generated.orion.ocusrd.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcusrdLinkParm;
import com.generated.orion.ocusrd.accessor.OcusrdFields;
import com.generated.orion.ocusrd.metadata.OcusrdBmsMetadata;
import com.generated.orion.ocusrd.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCUSRD. */
@Service
public class OcusrdService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcusrdService.class);

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
        return "OCUSRD";
    }

    @Override
    public String getTransId() {
        return "ORUD";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcusrdBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcusrdBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcusrdBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        if (ctx.appService.getEibcalen() == 0) {
            ctx.f.setOrionCommarea("");
            sendInitialScreen(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            ctx.f.copyBytes("WS-STATE-AREA", "CA-WORK-AREA");
            ctx.f.setCaErrMsg(" ");
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
        resetMapAndStage(ctx);
        populateHeaderFields(ctx);
        ctx.f.setErrmsgo("Enter a user id and press ENTER.");
        ctx.appService.sendMap(
                "MUSRDA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            xctlToAdminMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            handleEnterKey(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnly(ctx);
        }
    }

    /** COBOL paragraph: 2100-HANDLE-ENTER */
    private void handleEnterKey(TaskContext ctx) {
        receiveMapInput(ctx);
        if (ctx.f.getWsStage().equals("0")) {
            fetchUser(ctx);
        } else if (ctx.f.getWsStage().equals("1")) {
            confirmDelete(ctx);
        } else {
            sendInitialScreen(ctx);
        }
    }

    /** COBOL paragraph: 2110-RECEIVE-MAP */
    private void receiveMapInput(TaskContext ctx) {
        ctx.appService.receiveMap("MUSRDA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 2200-FETCH-USER */
    private void fetchUser(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getUseridi(), " ") || ctx.f.isAllLowValues("USERIDI")) {
            ctx.f.setErrmsgo(ctx.f.getWsMsgRequired());
            sendDataOnly(ctx);
        } else {
            ctx.f.setUsId(ctx.f.getUseridi());
            readUser(ctx);
            if (ctx.f.getWsFoundFlg().equals("Y")) {
                showUserDetails(ctx);
            } else {
                ctx.f.setErrmsgo("User not found.");
                sendDataOnly(ctx);
            }
        }
    }

    /** COBOL paragraph: 2210-SHOW-USER */
    private void showUserDetails(TaskContext ctx) {
        ctx.f.setWsSvUserid(ctx.f.getUsId());
        ctx.f.setUserido(ctx.f.getUsId());
        buildFullName(ctx);
        ctx.f.setUsnameo(ctx.f.getWsFullname());
        ctx.f.setUsconfo(" ");
        ctx.f.setString("WS-STAGE", "1");
        ctx.f.setErrmsgo("Type Y and press ENTER to delete this user.");
        sendDataOnly(ctx);
    }

    /** COBOL paragraph: 2220-BUILD-NAME */
    private void buildFullName(TaskContext ctx) {
        ctx.f.setWsFullname(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(ctx.f.getUsFirstName()).split(String.valueOf(" "), 2)[0]);
            sb.append(String.valueOf(" "));
            sb.append(String.valueOf(ctx.f.getUsLastName()).split(String.valueOf(" "), 2)[0]);
            ctx.f.setWsFullname(sb.toString());
        }
    }

    /** COBOL paragraph: 2300-CONFIRM-DELETE */
    private void confirmDelete(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getUsconfi(), "Y")
                || Utility.fieldEquals(ctx.f.getUsconfi(), "y")) {
            deleteUser(ctx);
            if (ctx.f.getWsErrFlg().equals("Y")) {
                redisplayUserFields(ctx);
                sendDataOnly(ctx);
            } else {
                confirmDeleteDone(ctx);
            }
        } else {
            cancelDelete(ctx);
        }
    }

    /** COBOL paragraph: 2310-REDISPLAY */
    private void redisplayUserFields(TaskContext ctx) {
        ctx.f.setUserido(ctx.f.getWsSvUserid());
        ctx.f.setUsnameo(ctx.f.getWsFullname());
    }

    /** COBOL paragraph: 3000-READ-USER */
    private void readUser(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.appService.readFile(ctx.f.getWsUsrsec(), ctx.f, String.valueOf(ctx.f.getUsId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-FOUND-FLG", "N");
        } else {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error reading user file.");
        }
    }

    /** COBOL paragraph: 3100-DELETE-USER */
    private void deleteUser(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setUsId(ctx.f.getWsSvUserid());
        ctx.appService.deleteFile(ctx.f.getWsUsrsec(), String.valueOf(ctx.f.getUsId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            /* CONTINUE */
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("User no longer exists.");
        } else {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error deleting user file.");
        }
    }

    /** COBOL paragraph: 4100-CONFIRM-DONE */
    private void confirmDeleteDone(TaskContext ctx) {
        resetMapAndStage(ctx);
        ctx.f.setErrmsgo("User deleted successfully.");
        sendDataOnly(ctx);
    }

    /** COBOL paragraph: 4200-CANCELLED */
    private void cancelDelete(TaskContext ctx) {
        resetMapAndStage(ctx);
        ctx.f.setErrmsgo("Delete cancelled.");
        sendDataOnly(ctx);
    }

    /** COBOL paragraph: 7000-XCTL-ADMEN */
    private void xctlToAdminMenu(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.f.copyBytes("CA-WORK-AREA", "WS-STATE-AREA");
        ctx.appService.xctl(
                ctx.f.getWsAdmenPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
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
    private void sendDataOnly(TaskContext ctx) {
        populateHeaderFields(ctx);
        ctx.appService.sendMap(
                "MUSRDA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
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
        ctx.f.copyBytes("CA-WORK-AREA", "WS-STATE-AREA");
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /** Clear the MUSRDAO map buffer and reset the dialog to stage 0. */
    private void resetMapAndStage(TaskContext ctx) {
        ctx.f.fillLowValues("MUSRDAO");
        ctx.f.setString("WS-STAGE", "0");
        ctx.f.setWsSvUserid(" ");
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcusrdFields f;

        final AppService appService;

        final OcusrdLinkParm link = new OcusrdLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcusrdFields(ws);
        }
    }
}

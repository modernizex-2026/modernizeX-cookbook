package com.generated.orion.ocusrl.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcusrlLinkParm;
import com.generated.orion.ocusrl.accessor.OcusrlFields;
import com.generated.orion.ocusrl.metadata.OcusrlBmsMetadata;
import com.generated.orion.ocusrl.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCUSRL. */
@Service
public class OcusrlService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcusrlService.class);

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
        return "OCUSRL";
    }

    @Override
    public String getTransId() {
        return "ORUL";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcusrlBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcusrlBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcusrlBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        if (ctx.appService.getEibcalen() == 0) {
            ctx.f.setOrionCommarea("");
            sendInitialUserListScreen(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            ctx.f.copyBytes("WS-STATE-AREA", "CA-WORK-AREA");
            ctx.f.setCaErrMsg(" ");
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitialUserListScreen(ctx);
            } else {
                processUserInput(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialUserListScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MUSRLAO");
        ctx.f.fillLowValues("WS-START-KEY");
        ctx.f.setString("WS-SKIP-FIRST", "N");
        buildUserListPage(ctx);
        populateScreenHeader(ctx);
        if (ctx.f.getWsRowCount() == 0) {
            ctx.f.setErrmsgo("No users found.");
        } else {
            ctx.f.setErrmsgo("PF8 next page  PF7 top  PF3 exit.");
        }
        ctx.appService.sendMap(
                "MUSRLA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processUserInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToAdminMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "7")) {
            displayTopOfList(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "8")) {
            displayNextPage(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            displayTopOfList(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-PAGE-TOP */
    private void displayTopOfList(TaskContext ctx) {
        ctx.f.fillLowValues("MUSRLAO");
        ctx.f.fillLowValues("WS-START-KEY");
        ctx.f.setString("WS-SKIP-FIRST", "N");
        buildUserListPage(ctx);
        if (ctx.f.getWsRowCount() == 0) {
            ctx.f.setErrmsgo("No users found.");
        } else {
            ctx.f.setErrmsgo("Top of list. PF8 next page.");
        }
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 2150-PAGE-NEXT */
    private void displayNextPage(TaskContext ctx) {
        ctx.f.fillLowValues("MUSRLAO");
        ctx.f.setWsStartKey(ctx.f.getWsLastKey());
        ctx.f.setString("WS-SKIP-FIRST", "Y");
        buildUserListPage(ctx);
        if (ctx.f.getWsRowCount() == 0) {
            ctx.f.setErrmsgo("No more users to display.");
        } else {
            ctx.f.setErrmsgo("PF8 next page  PF7 top  PF3 exit.");
        }
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 2200-LIST-PAGE */
    private void buildUserListPage(TaskContext ctx) {
        clearOutputRows(ctx);
        ctx.f.setWsRowCount(0);
        ctx.f.setWsEndFlg("N");
        ctx.f.setUsId(ctx.f.getWsStartKey());
        startBrowseUserFile(ctx);
        if (ctx.f.getWsErrFlg().equals("Y") || ctx.f.getWsEndFlg().equals("Y")) {
            return;
        }
        if (ctx.f.getWsSkipFirst().equals("Y")) {
            skipFirstRecord(ctx);
        }
        for (ctx.f.setWsRowIdx(1);
                !(ctx.f.getWsRowIdx() > 4 || ctx.f.getWsEndFlg().equals("Y"));
                ctx.f.setWsRowIdx(ctx.f.getWsRowIdx() + 1)) {
            readNextUserRecord(ctx);
            if (ctx.f.getWsFoundFlg().equals("Y")) {
                storeUserRow(ctx);
            }
        }
        endBrowseUserFile(ctx);
    }

    /** COBOL paragraph: 2210-CLEAR-ROWS */
    private void clearOutputRows(TaskContext ctx) {
        ctx.f.setUsr1o(" ");
        ctx.f.setUnm1o(" ");
        ctx.f.setUty1o(" ");
        ctx.f.setUsr2o(" ");
        ctx.f.setUnm2o(" ");
        ctx.f.setUty2o(" ");
        ctx.f.setUsr3o(" ");
        ctx.f.setUnm3o(" ");
        ctx.f.setUty3o(" ");
        ctx.f.setUsr4o(" ");
        ctx.f.setUnm4o(" ");
        ctx.f.setUty4o(" ");
    }

    /** COBOL paragraph: 2220-STARTBR */
    private void startBrowseUserFile(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.appService.startBrowse(ctx.f.getWsUsrsec(), String.valueOf(ctx.f.getUsId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            /* CONTINUE */
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setWsEndFlg("Y");
        } else {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error starting user browse.");
        }
    }

    /** COBOL paragraph: 2230-SKIP-ONE */
    private void skipFirstRecord(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsUsrsec(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setWsEndFlg("Y");
        }
    }

    /** COBOL paragraph: 2240-READ-NEXT */
    private void readNextUserRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.appService.readNext(ctx.f.getWsUsrsec(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setWsEndFlg("Y");
        } else {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error reading user file.");
            ctx.f.setWsEndFlg("Y");
        }
    }

    /** COBOL paragraph: 2250-STORE-ROW */
    private void storeUserRow(TaskContext ctx) {
        buildFullName(ctx);
        ctx.f.setWsRowCount(ctx.f.getWsRowCount() + 1);
        ctx.f.setWsLastKey(ctx.f.getUsId());
        switch (ctx.f.getWsRowIdx()) {
            case 1 -> {
                ctx.f.setUsr1o(ctx.f.getUsId());
                ctx.f.setUnm1o(ctx.f.getWsFullname());
                ctx.f.setUty1o(ctx.f.getUsType());
            }
            case 2 -> {
                ctx.f.setUsr2o(ctx.f.getUsId());
                ctx.f.setUnm2o(ctx.f.getWsFullname());
                ctx.f.setUty2o(ctx.f.getUsType());
            }
            case 3 -> {
                ctx.f.setUsr3o(ctx.f.getUsId());
                ctx.f.setUnm3o(ctx.f.getWsFullname());
                ctx.f.setUty3o(ctx.f.getUsType());
            }
            case 4 -> {
                ctx.f.setUsr4o(ctx.f.getUsId());
                ctx.f.setUnm4o(ctx.f.getWsFullname());
                ctx.f.setUty4o(ctx.f.getUsType());
            }
            default -> {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: 2255-BUILD-NAME */
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

    /** COBOL paragraph: 2260-ENDBR */
    private void endBrowseUserFile(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsUsrsec());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 7000-XCTL-ADMEN */
    private void transferToAdminMenu(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.f.copyBytes("CA-WORK-AREA", "WS-STATE-AREA");
        ctx.appService.xctl(
                ctx.f.getWsAdmenPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
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
    private void sendDataOnlyScreen(TaskContext ctx) {
        populateScreenHeader(ctx);
        ctx.appService.sendMap(
                "MUSRLA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
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
        ctx.f.copyBytes("CA-WORK-AREA", "WS-STATE-AREA");
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcusrlFields f;

        final AppService appService;

        final OcusrlLinkParm link = new OcusrlLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcusrlFields(ws);
        }
    }
}

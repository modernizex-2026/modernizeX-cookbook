package com.generated.orion.octranl.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OctranlLinkParm;
import com.generated.orion.octranl.accessor.OctranlFields;
import com.generated.orion.octranl.metadata.OctranlBmsMetadata;
import com.generated.orion.octranl.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCTRANL. */
@Service
public class OctranlService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OctranlService.class);

    private static final int COMMAREA_LENGTH = 692;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        runMainLine(ctx);
    }

    @Override
    public String getProgramName() {
        return "OCTRANL";
    }

    @Override
    public String getTransId() {
        return "ORTL";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OctranlBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OctranlBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OctranlBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainLine(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setCaErrMsg(" ");
        if (ctx.appService.getEibcalen() == 0) {
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
        ctx.f.fillLowValues("MTRANLAO");
        populateHeader(ctx);
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setCardnuml((short) -1);
        ctx.appService.sendMap(
                "MTRANLA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            listTransactions(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnly(ctx);
        }
    }

    /** COBOL paragraph: 2100-LIST-TRANS */
    private void listTransactions(TaskContext ctx) {
        ctx.appService.receiveMap("MTRANLA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (Utility.fieldEquals(ctx.f.getCardnumi(), " ") || ctx.f.isAllLowValues("CARDNUMI")) {
            ctx.f.setErrmsgo(ctx.f.getWsMCardReq());
            ctx.f.setCardnuml((short) -1);
            sendDataOnly(ctx);
        } else {
            ctx.f.setWsCardKey(ctx.f.getCardnumi());
            ctx.f.setWsCardKey(
                    String.valueOf(ctx.f.getWsCardKey())
                            .replace(
                                    String.valueOf(RuntimeConstants.LOW_VALUES),
                                    String.valueOf(" ")));
            ctx.f.setCaCardNum(ctx.f.getWsCardKey());
            browseTransactions(ctx);
            buildSummaryMessage(ctx);
            ctx.f.setCardnuml((short) -1);
            sendDataOnly(ctx);
        }
    }

    /** COBOL paragraph: 2200-BUILD-SUMMARY */
    private void buildSummaryMessage(TaskContext ctx) {
        if (ctx.f.getWsRowCnt() == 0) {
            ctx.f.setErrmsgo(ctx.f.getWsMNoneFound());
        } else {
            ctx.f.setWsCntEd(ctx.f.getWsRowCnt());
            ctx.f.setErrmsgo(" ");
            ctx.f.setErrmsgo(String.valueOf(ctx.f.getString("WS-CNT-ED")) + ctx.f.getWsMSuffix());
        }
    }

    /** COBOL paragraph: 3000-BROWSE-TRANS */
    private void browseTransactions(TaskContext ctx) {
        ctx.f.setWsRowCnt(0);
        ctx.f.setWsEndFlg("N");
        ctx.f.setString("WS-BROWSE-STARTED", "N");
        clearRowFields(ctx);
        startTransactionBrowse(ctx);
        if (ctx.f.getWsBrowseStarted().equals("Y")) {
            while (!(ctx.f.getWsEndFlg().equals("Y")
                    || ctx.f.getWsRowCnt() >= ctx.f.getWsMaxRows())) {
                readNextTransaction(ctx);
            }
            endTransactionBrowse(ctx);
        }
    }

    /** COBOL paragraph: 3050-CLEAR-ROWS */
    private void clearRowFields(TaskContext ctx) {
        ctx.f.setTrn1o(" ");
        ctx.f.setAmt1o(" ");
        ctx.f.setTyp1o(" ");
        ctx.f.setTrn2o(" ");
        ctx.f.setAmt2o(" ");
        ctx.f.setTyp2o(" ");
        ctx.f.setTrn3o(" ");
        ctx.f.setAmt3o(" ");
        ctx.f.setTyp3o(" ");
        ctx.f.setTrn4o(" ");
        ctx.f.setAmt4o(" ");
        ctx.f.setTyp4o(" ");
        ctx.f.setTrn5o(" ");
        ctx.f.setAmt5o(" ");
        ctx.f.setTyp5o(" ");
    }

    /** COBOL paragraph: 3100-START-BROWSE */
    private void startTransactionBrowse(TaskContext ctx) {
        ctx.f.fillLowValues("TR-ID");
        ctx.appService.startBrowse(ctx.f.getWsTranfile(), String.valueOf(ctx.f.getTrId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BROWSE-STARTED", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-BROWSE-STARTED", "N");
            ctx.f.setString("WS-END-FLG", "Y");
        } else {
            ctx.f.setString("WS-BROWSE-STARTED", "N");
            ctx.f.setString("WS-END-FLG", "Y");
            ctx.f.setErrmsgo(ctx.f.getWsMBrowseErr());
        }
    }

    /** COBOL paragraph: 3200-READ-NEXT */
    private void readNextTransaction(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsTranfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            if (Utility.fieldEquals(ctx.f.getTrCardNum(), ctx.f.getWsCardKey())) {
                ctx.f.setWsRowCnt(ctx.f.getWsRowCnt() + 1);
                moveRowToScreen(ctx);
            }
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-END-FLG", "Y");
        } else {
            ctx.f.setString("WS-END-FLG", "Y");
            ctx.f.setErrmsgo(ctx.f.getWsMBrowseErr());
        }
    }

    /** COBOL paragraph: 3300-END-BROWSE */
    private void endTransactionBrowse(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsTranfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 4000-MOVE-ROW */
    private void moveRowToScreen(TaskContext ctx) {
        ctx.f.setWsEdAmt(ctx.f.getTrAmt());
        switch (ctx.f.getWsRowCnt()) {
            case 1 -> {
                ctx.f.setTrn1o(ctx.f.getTrId());
                ctx.f.setAmt1o(
                        Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-AMT")), 16)
                                .substring(1, 16));
                ctx.f.setTyp1o(ctx.f.getTrTypeCd());
            }
            case 2 -> {
                ctx.f.setTrn2o(ctx.f.getTrId());
                ctx.f.setAmt2o(
                        Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-AMT")), 16)
                                .substring(1, 16));
                ctx.f.setTyp2o(ctx.f.getTrTypeCd());
            }
            case 3 -> {
                ctx.f.setTrn3o(ctx.f.getTrId());
                ctx.f.setAmt3o(
                        Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-AMT")), 16)
                                .substring(1, 16));
                ctx.f.setTyp3o(ctx.f.getTrTypeCd());
            }
            case 4 -> {
                ctx.f.setTrn4o(ctx.f.getTrId());
                ctx.f.setAmt4o(
                        Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-AMT")), 16)
                                .substring(1, 16));
                ctx.f.setTyp4o(ctx.f.getTrTypeCd());
            }
            case 5 -> {
                ctx.f.setTrn5o(ctx.f.getTrId());
                ctx.f.setAmt5o(
                        Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-AMT")), 16)
                                .substring(1, 16));
                ctx.f.setTyp5o(ctx.f.getTrTypeCd());
            }
            default -> {
                /* CONTINUE */
            }
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
    private void populateHeader(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        loadCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnly(TaskContext ctx) {
        populateHeader(ctx);
        ctx.appService.sendMap(
                "MTRANLA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
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

        final OctranlFields f;

        final AppService appService;

        final OctranlLinkParm link = new OctranlLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OctranlFields(ws);
        }
    }
}

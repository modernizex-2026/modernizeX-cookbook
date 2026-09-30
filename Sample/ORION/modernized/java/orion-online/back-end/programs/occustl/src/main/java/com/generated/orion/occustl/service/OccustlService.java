package com.generated.orion.occustl.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OccustlLinkParm;
import com.generated.orion.occustl.accessor.OccustlFields;
import com.generated.orion.occustl.metadata.OccustlBmsMetadata;
import com.generated.orion.occustl.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCCUSTL. */
@Service
public class OccustlService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OccustlService.class);

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
        return "OCCUSTL";
    }

    @Override
    public String getTransId() {
        return "ORLC";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OccustlBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OccustlBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OccustlBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void executeMainFlow(TaskContext ctx) {
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
                dispatchUserInput(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MCUSTLAO");
        populateScreenHeader(ctx);
        clearCustomerRows(ctx);
        ctx.f.setWsNextStart(0);
        ctx.f.setCaWorkArea(
                Utility.setSubstring(
                        String.valueOf(ctx.f.getCaWorkArea()),
                        1,
                        9,
                        String.valueOf(ctx.f.getWsNextStart())));
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setFrcustl((short) -1);
        ctx.appService.sendMap(
                "MCUSTLA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void dispatchUserInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "8")) {
            pageForwardList(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            listCustomersFromStart(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-LIST-FROM-START */
    private void listCustomersFromStart(TaskContext ctx) {
        ctx.appService.receiveMap("MCUSTLA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MCUSTLAI");
        }
        ctx.f.setWsNcIn(ctx.f.getFrcusti());
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getWsNcIn(), " ")) {
            ctx.f.setWsPageStart(0);
        } else {
            ctx.f.setWsNcLen(9);
            parseNumericStartKey(ctx);
            if (ctx.f.getWsValidSw().equals("N")) {
                ctx.f.setErrmsgo(ctx.f.getWsMBadStart());
                ctx.f.setFrcustl((short) -1);
                sendDataOnlyScreen(ctx);
                return;
            }
            ctx.f.setWsPageStart(Utility.toCobolInt(ctx.f.getWsNcValue(), 9));
        }
        browseCustomerPage(ctx);
        buildRowCountSummary(ctx);
        ctx.f.setFrcustl((short) -1);
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 2200-PAGE-FWD */
    private void pageForwardList(TaskContext ctx) {
        if (Utility.isNumeric(
                String.valueOf(
                        Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 9)
                                .substring(0, 9)))) {
            ctx.f.setWsPageStart(
                    Utility.parseNumeric(
                                    Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 9)
                                            .substring(0, 9))
                            .intValue());
        } else {
            ctx.f.setWsPageStart(0);
        }
        browseCustomerPage(ctx);
        if (ctx.f.getWsRowCnt() == 0) {
            ctx.f.setErrmsgo(ctx.f.getWsMEndFile());
        } else {
            buildRowCountSummary(ctx);
        }
        ctx.f.setFrcustl((short) -1);
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 2300-BUILD-SUMMARY */
    private void buildRowCountSummary(TaskContext ctx) {
        if (ctx.f.getWsRowCnt() == 0) {
            ctx.f.setErrmsgo(ctx.f.getWsMNoneFound());
        } else {
            ctx.f.setWsCntEd(ctx.f.getWsRowCnt());
            ctx.f.setErrmsgo(" ");
            {
                StringBuilder sb = new StringBuilder();
                sb.append(String.valueOf(ctx.f.getString("WS-CNT-ED")));
                sb.append(String.valueOf(ctx.f.getWsMSuffix()));
                ctx.f.setErrmsgo(sb.toString());
            }
        }
    }

    /** COBOL paragraph: 3000-BROWSE-PAGE */
    private void browseCustomerPage(TaskContext ctx) {
        ctx.f.setWsRowCnt(0);
        ctx.f.setWsEndFlg("N");
        ctx.f.setString("WS-BROWSE-STARTED", "N");
        clearCustomerRows(ctx);
        startCustomerBrowse(ctx);
        if (ctx.f.getWsBrowseStarted().equals("Y")) {
            while (!(ctx.f.getWsEndFlg().equals("Y")
                    || ctx.f.getWsRowCnt() >= ctx.f.getWsMaxRows())) {
                readNextCustomerRow(ctx);
            }
            endCustomerBrowse(ctx);
        }
        if (ctx.f.getWsRowCnt() > 0) {
            ctx.f.setWsNextStart((ctx.f.getWsLastKey() + 1));
            ctx.f.setCaWorkArea(
                    Utility.setSubstring(
                            String.valueOf(ctx.f.getCaWorkArea()),
                            1,
                            9,
                            String.valueOf(ctx.f.getWsNextStart())));
        }
    }

    /** COBOL paragraph: 3050-CLEAR-ROWS */
    private void clearCustomerRows(TaskContext ctx) {
        ctx.f.setCul1o(" ");
        ctx.f.setCun1o(" ");
        ctx.f.setCuf1o(" ");
        ctx.f.setCul2o(" ");
        ctx.f.setCun2o(" ");
        ctx.f.setCuf2o(" ");
        ctx.f.setCul3o(" ");
        ctx.f.setCun3o(" ");
        ctx.f.setCuf3o(" ");
        ctx.f.setCul4o(" ");
        ctx.f.setCun4o(" ");
        ctx.f.setCuf4o(" ");
    }

    /** COBOL paragraph: 3100-START-BROWSE */
    private void startCustomerBrowse(TaskContext ctx) {
        ctx.f.setCuId(ctx.f.getWsPageStart());
        ctx.appService.startBrowse(ctx.f.getWsCustfile(), String.valueOf(ctx.f.getCuId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BROWSE-STARTED", "Y");
        } else if (ctx.f.getWsRespCd() == 13 || ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-BROWSE-STARTED", "N");
            ctx.f.setString("WS-END-FLG", "Y");
        } else {
            ctx.f.setString("WS-BROWSE-STARTED", "N");
            ctx.f.setString("WS-END-FLG", "Y");
            ctx.f.setErrmsgo(ctx.f.getWsMBrowseErr());
        }
    }

    /** COBOL paragraph: 3200-READ-NEXT */
    private void readNextCustomerRow(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsCustfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setWsRowCnt(ctx.f.getWsRowCnt() + 1);
            ctx.f.setWsLastKey(ctx.f.getCuId());
            populateCustomerRow(ctx);
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-END-FLG", "Y");
        } else {
            ctx.f.setString("WS-END-FLG", "Y");
            ctx.f.setErrmsgo(ctx.f.getWsMBrowseErr());
        }
    }

    /** COBOL paragraph: 3300-END-BROWSE */
    private void endCustomerBrowse(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsCustfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 4000-MOVE-ROW */
    private void populateCustomerRow(TaskContext ctx) {
        ctx.f.setWsNameLine(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(ctx.f.getCuLastName()).split(String.valueOf("  "), 2)[0]);
            sb.append(String.valueOf(", "));
            sb.append(String.valueOf(ctx.f.getCuFirstName()).split(String.valueOf("  "), 2)[0]);
            ctx.f.setWsNameLine(sb.toString());
        }
        switch (ctx.f.getWsRowCnt()) {
            case 1 -> {
                ctx.f.setCul1o(String.format("%09d", ctx.f.getCuId()));
                ctx.f.setCun1o(ctx.f.getWsNameLine());
                ctx.f.setCuf1o(String.format("%03d", ctx.f.getCuFicoScore()));
            }
            case 2 -> {
                ctx.f.setCul2o(String.format("%09d", ctx.f.getCuId()));
                ctx.f.setCun2o(ctx.f.getWsNameLine());
                ctx.f.setCuf2o(String.format("%03d", ctx.f.getCuFicoScore()));
            }
            case 3 -> {
                ctx.f.setCul3o(String.format("%09d", ctx.f.getCuId()));
                ctx.f.setCun3o(ctx.f.getWsNameLine());
                ctx.f.setCuf3o(String.format("%03d", ctx.f.getCuFicoScore()));
            }
            case 4 -> {
                ctx.f.setCul4o(String.format("%09d", ctx.f.getCuId()));
                ctx.f.setCun4o(ctx.f.getWsNameLine());
                ctx.f.setCuf4o(String.format("%03d", ctx.f.getCuFicoScore()));
            }
            default -> {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: 6000-PARSE-NUM */
    private void parseNumericStartKey(TaskContext ctx) {
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
    private void transferToMenuProgram(TaskContext ctx) {
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
        refreshHeaderDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnlyScreen(TaskContext ctx) {
        populateScreenHeader(ctx);
        ctx.appService.sendMap(
                "MCUSTLA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8500-GET-DATE-TIME */
    private void refreshHeaderDateTime(TaskContext ctx) {
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

        final OccustlFields f;

        final AppService appService;

        final OccustlLinkParm link = new OccustlLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OccustlFields(ws);
        }
    }
}

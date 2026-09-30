package com.generated.orion.ocacctl.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcacctlLinkParm;
import com.generated.orion.ocacctl.accessor.OcacctlFields;
import com.generated.orion.ocacctl.metadata.OcacctlBmsMetadata;
import com.generated.orion.ocacctl.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCACCTL. */
@Service
public class OcacctlService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcacctlService.class);

    /** Length in bytes of the ORION-COMMAREA passed across program transfers. */
    private static final int COMMAREA_LENGTH_BYTES = 692;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        runMainLogic(ctx);
    }

    @Override
    public String getProgramName() {
        return "OCACCTL";
    }

    @Override
    public String getTransId() {
        return "ORLA";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcacctlBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcacctlBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcacctlBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainLogic(TaskContext ctx) {
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
                processInputKey(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MACCTLAO");
        populateScreenHeader(ctx);
        clearDisplayRows(ctx);
        ctx.f.setWsNextStart(0);
        ctx.f.setCaWorkArea(
                Utility.setSubstring(
                        String.valueOf(ctx.f.getCaWorkArea()),
                        1,
                        11,
                        String.valueOf(ctx.f.getWsNextStart())));
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setFracctl((short) -1);
        ctx.appService.sendMap(
                "MACCTLA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInputKey(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "8")) {
            pageForwardAndBrowse(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            listAccountsFromStart(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-LIST-FROM-START */
    private void listAccountsFromStart(TaskContext ctx) {
        ctx.appService.receiveMap("MACCTLA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MACCTLAI");
        }
        ctx.f.setWsNcIn(ctx.f.getFraccti());
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getWsNcIn(), " ")) {
            ctx.f.setWsPageStart(0);
        } else {
            ctx.f.setWsNcLen(11);
            parseNumericField(ctx);
            if (ctx.f.getWsValidSw().equals("N")) {
                ctx.f.setErrmsgo(ctx.f.getWsMBadStart());
                ctx.f.setFracctl((short) -1);
                sendDataOnlyScreen(ctx);
                return;
            }
            ctx.f.setWsPageStart(ctx.f.getWsNcValue());
        }
        browseAccountPage(ctx);
        buildSummaryMessage(ctx);
        ctx.f.setFracctl((short) -1);
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 2200-PAGE-FWD */
    private void pageForwardAndBrowse(TaskContext ctx) {
        if (Utility.isNumeric(
                String.valueOf(
                        Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 11)
                                .substring(0, 11)))) {
            ctx.f.setWsPageStart(
                    Utility.parseNumeric(
                                    Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 11)
                                            .substring(0, 11))
                            .longValue());
        } else {
            ctx.f.setWsPageStart(0);
        }
        browseAccountPage(ctx);
        if (ctx.f.getWsRowCnt() == 0) {
            ctx.f.setErrmsgo(ctx.f.getWsMEndFile());
        } else {
            buildSummaryMessage(ctx);
        }
        ctx.f.setFracctl((short) -1);
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 2300-BUILD-SUMMARY */
    private void buildSummaryMessage(TaskContext ctx) {
        if (ctx.f.getWsRowCnt() == 0) {
            ctx.f.setErrmsgo(ctx.f.getWsMNoneFound());
        } else {
            ctx.f.setWsCntEd(ctx.f.getWsRowCnt());
            ctx.f.setErrmsgo(" ");
            ctx.f.setErrmsgo(
                    String.valueOf(ctx.f.getString("WS-CNT-ED"))
                            + String.valueOf(ctx.f.getWsMSuffix()));
        }
    }

    /** COBOL paragraph: 3000-BROWSE-PAGE */
    private void browseAccountPage(TaskContext ctx) {
        ctx.f.setWsRowCnt(0);
        ctx.f.setWsEndFlg("N");
        ctx.f.setString("WS-BROWSE-STARTED", "N");
        clearDisplayRows(ctx);
        startAccountBrowse(ctx);
        if (ctx.f.getWsBrowseStarted().equals("Y")) {
            while (!ctx.f.getWsEndFlg().equals("Y") && ctx.f.getWsRowCnt() < ctx.f.getWsMaxRows()) {
                readNextAccountRecord(ctx);
            }
            endAccountBrowse(ctx);
        }
        if (ctx.f.getWsRowCnt() > 0) {
            ctx.f.setWsNextStart((ctx.f.getWsLastKey() + 1));
            ctx.f.setCaWorkArea(
                    Utility.setSubstring(
                            String.valueOf(ctx.f.getCaWorkArea()),
                            1,
                            11,
                            String.valueOf(ctx.f.getWsNextStart())));
        }
    }

    /** COBOL paragraph: 3050-CLEAR-ROWS */
    private void clearDisplayRows(TaskContext ctx) {
        ctx.f.setAcl1o(" ");
        ctx.f.setAcs1o(" ");
        ctx.f.setAcb1o(" ");
        ctx.f.setAcl2o(" ");
        ctx.f.setAcs2o(" ");
        ctx.f.setAcb2o(" ");
        ctx.f.setAcl3o(" ");
        ctx.f.setAcs3o(" ");
        ctx.f.setAcb3o(" ");
        ctx.f.setAcl4o(" ");
        ctx.f.setAcs4o(" ");
        ctx.f.setAcb4o(" ");
        ctx.f.setAcl5o(" ");
        ctx.f.setAcs5o(" ");
        ctx.f.setAcb5o(" ");
    }

    /** COBOL paragraph: 3100-START-BROWSE */
    private void startAccountBrowse(TaskContext ctx) {
        ctx.f.setAcId(ctx.f.getWsPageStart());
        ctx.appService.startBrowse(ctx.f.getWsAcctfile(), String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BROWSE-STARTED", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-BROWSE-STARTED", "N");
            ctx.f.setString("WS-END-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-BROWSE-STARTED", "N");
            ctx.f.setString("WS-END-FLG", "Y");
        } else {
            ctx.f.setString("WS-BROWSE-STARTED", "N");
            ctx.f.setString("WS-END-FLG", "Y");
            ctx.f.setErrmsgo(ctx.f.getWsMBrowseErr());
        }
    }

    /** COBOL paragraph: 3200-READ-NEXT */
    private void readNextAccountRecord(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsAcctfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setWsRowCnt(ctx.f.getWsRowCnt() + 1);
            ctx.f.setWsLastKey(ctx.f.getAcId());
            populateRowDisplay(ctx);
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-END-FLG", "Y");
        } else {
            ctx.f.setString("WS-END-FLG", "Y");
            ctx.f.setErrmsgo(ctx.f.getWsMBrowseErr());
        }
    }

    /** COBOL paragraph: 3300-END-BROWSE */
    private void endAccountBrowse(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsAcctfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 4000-MOVE-ROW */
    private void populateRowDisplay(TaskContext ctx) {
        ctx.f.setWsEdBal(ctx.f.getAcCurrBal());
        switch (ctx.f.getWsRowCnt()) {
            case 1 -> {
                ctx.f.setAcl1o(String.format("%011d", ctx.f.getAcId()));
                ctx.f.setAcs1o(ctx.f.getAcActiveStatus());
                ctx.f.setAcb1o(
                        Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-BAL")), 16)
                                .substring(1, 16));
            }
            case 2 -> {
                ctx.f.setAcl2o(String.format("%011d", ctx.f.getAcId()));
                ctx.f.setAcs2o(ctx.f.getAcActiveStatus());
                ctx.f.setAcb2o(
                        Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-BAL")), 16)
                                .substring(1, 16));
            }
            case 3 -> {
                ctx.f.setAcl3o(String.format("%011d", ctx.f.getAcId()));
                ctx.f.setAcs3o(ctx.f.getAcActiveStatus());
                ctx.f.setAcb3o(
                        Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-BAL")), 16)
                                .substring(1, 16));
            }
            case 4 -> {
                ctx.f.setAcl4o(String.format("%011d", ctx.f.getAcId()));
                ctx.f.setAcs4o(ctx.f.getAcActiveStatus());
                ctx.f.setAcb4o(
                        Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-BAL")), 16)
                                .substring(1, 16));
            }
            case 5 -> {
                ctx.f.setAcl5o(String.format("%011d", ctx.f.getAcId()));
                ctx.f.setAcs5o(ctx.f.getAcActiveStatus());
                ctx.f.setAcb5o(
                        Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-BAL")), 16)
                                .substring(1, 16));
            }
            default -> {
                /* CONTINUE */
            }
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
    private void transferToMenuProgram(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(
                ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH_BYTES);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8000-POPULATE-HEADER */
    private void populateScreenHeader(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        retrieveCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnlyScreen(TaskContext ctx) {
        populateScreenHeader(ctx);
        ctx.appService.sendMap(
                "MACCTLA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8500-GET-DATE-TIME */
    private void retrieveCurrentDateTime(TaskContext ctx) {
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
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH_BYTES);
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcacctlFields f;

        final AppService appService;

        final OcacctlLinkParm link = new OcacctlLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcacctlFields(ws);
        }
    }
}

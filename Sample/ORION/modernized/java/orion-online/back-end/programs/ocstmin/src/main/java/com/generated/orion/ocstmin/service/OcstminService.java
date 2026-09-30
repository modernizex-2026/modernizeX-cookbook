package com.generated.orion.ocstmin.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcstminLinkParm;
import com.generated.orion.ocstmin.accessor.OcstminFields;
import com.generated.orion.ocstmin.metadata.OcstminBmsMetadata;
import com.generated.orion.ocstmin.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCSTMIN. */
@Service
public class OcstminService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcstminService.class);

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
        return "OCSTMIN";
    }

    @Override
    public String getTransId() {
        return "ORSI";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcstminBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcstminBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcstminBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainLine(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setCaErrMsg(" ");
        if (ctx.appService.getEibcalen() == 0) {
            ctx.f.setOrionCommarea("");
            sendInitialScreen(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            ctx.f.setWsPageState(
                    Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 36).substring(0, 36));
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitialScreen(ctx);
            } else {
                processUserInput(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MSTMINAO");
        populateHeader(ctx);
        clearScreenRows(ctx);
        ctx.f.setWsPageState("");
        ctx.f.setWsPsMore("N");
        ctx.f.setWsPsFltSet("N");
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setFracctl((short) -1);
        ctx.appService.sendMap(
                "MSTMINA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processUserInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "7")) {
            restartBrowse(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "8")) {
            pageForward(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            searchAccounts(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnly(ctx);
        }
    }

    /** COBOL paragraph: 2100-SEARCH */
    private void searchAccounts(TaskContext ctx) {
        ctx.appService.receiveMap("MSTMINA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MSTMINAI");
        }
        validateAccountField(ctx);
        if (isValidationFailed(ctx)) {
            return;
        }
        validateCycleField(ctx);
        if (isValidationFailed(ctx)) {
            return;
        }
        ctx.f.setWsPsFltAcct(ctx.f.getWsStartAcct());
        ctx.f.setWsPsFltCycle(ctx.f.getWsStartCycle());
        ctx.f.setWsPsFltSet("Y");
        browseAccounts(ctx);
        buildSummaryMessage(ctx);
        ctx.f.setFracctl((short) -1);
        sendDataOnly(ctx);
    }

    /** COBOL paragraph: 2110-EDIT-ACCT */
    private void validateAccountField(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsNcIn(ctx.f.getFraccti());
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getWsNcIn(), " ")) {
            ctx.f.setWsStartAcct(0);
        } else {
            ctx.f.setWsNcLen(11);
            parseNumericField(ctx);
            if (isValidationFailed(ctx)) {
                ctx.f.setErrmsgo(ctx.f.getWsMBadAcct());
                ctx.f.setFracctl((short) -1);
                sendDataOnly(ctx);
            } else {
                ctx.f.setWsStartAcct(ctx.f.getWsNcValue());
            }
        }
    }

    /** COBOL paragraph: 2120-EDIT-CYCLE */
    private void validateCycleField(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsNcIn(ctx.f.getFrcyci());
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getWsNcIn(), " ")) {
            ctx.f.setWsStartCycle(0);
        } else {
            ctx.f.setWsNcLen(6);
            parseNumericField(ctx);
            if (isValidationFailed(ctx)) {
                ctx.f.setErrmsgo(ctx.f.getWsMBadCycle());
                ctx.f.setFracctl((short) -1);
                sendDataOnly(ctx);
            } else {
                ctx.f.setWsStartCycle(Utility.toCobolInt(ctx.f.getWsNcValue(), 6));
            }
        }
    }

    /** COBOL paragraph: 2200-PAGE-FWD */
    private void pageForward(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getWsPsMore(), "Y")) {
            ctx.f.setWsStartAcct(ctx.f.getWsPsNextAcct());
            ctx.f.setWsStartCycle(ctx.f.getWsPsNextCycle());
            browseAccounts(ctx);
            buildSummaryMessage(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMEndFile());
        }
        ctx.f.setFracctl((short) -1);
        sendDataOnly(ctx);
    }

    /** COBOL paragraph: 2300-RESTART */
    private void restartBrowse(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getWsPsFltSet(), "Y")) {
            ctx.f.setWsStartAcct(ctx.f.getWsPsFltAcct());
            ctx.f.setWsStartCycle(ctx.f.getWsPsFltCycle());
        } else {
            ctx.f.setWsStartAcct(0);
            ctx.f.setWsStartCycle(0);
        }
        browseAccounts(ctx);
        buildSummaryMessage(ctx);
        ctx.f.setFracctl((short) -1);
        sendDataOnly(ctx);
    }

    /** COBOL paragraph: 2400-BUILD-SUMMARY */
    private void buildSummaryMessage(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getKsbStatus(), "99")) {
            ctx.f.setErrmsgo(ctx.f.getWsMBrowseErr());
        } else if (ctx.f.getKsbRowCnt() == 0) {
            ctx.f.setErrmsgo(ctx.f.getWsMNoneFound());
        } else {
            ctx.f.setWsCntEd(ctx.f.getKsbRowCnt());
            ctx.f.setErrmsgo(" ");
            {
                StringBuilder sb = new StringBuilder();
                sb.append(String.valueOf(ctx.f.getString("WS-CNT-ED")));
                sb.append(String.valueOf(ctx.f.getWsMSuffix()));
                ctx.f.setErrmsgo(sb.toString());
            }
        }
    }

    /** COBOL paragraph: 3000-DO-BROWSE */
    private void browseAccounts(TaskContext ctx) {
        clearScreenRows(ctx);
        ctx.f.setKsbMode("ACCT");
        ctx.f.setKsbStartAcct(ctx.f.getWsStartAcct());
        ctx.f.setKsbStartCycle(ctx.f.getWsStartCycle());
        ctx.f.setKsbMaxRows(6);
        {
            byte[] _linkCa = ctx.f.sliceBytes("KSTMB-PARM");
            ctx.appService.link(ctx.f.getWsSubPgm(), _linkCa, 567);
            ctx.f.writeBytes("KSTMB-PARM", _linkCa);
        }
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setKsbStatus("99");
            ctx.f.setKsbRowCnt(0);
            ctx.f.setWsPsMore("N");
            return;
        }
        ctx.f.setWsPsNextAcct(ctx.f.getKsbNextAcct());
        ctx.f.setWsPsNextCycle(ctx.f.getKsbNextCycle());
        ctx.f.setWsPsMore(ctx.f.getKsbMore());
        if (ctx.f.getKsbRowCnt() > 0) {
            moveRowsToScreen(ctx);
        }
    }

    /** COBOL paragraph: 3050-CLEAR-ROWS */
    private void clearScreenRows(TaskContext ctx) {
        ctx.f.setSa1o(" ");
        ctx.f.setSc1o(" ");
        ctx.f.setSo1o(" ");
        ctx.f.setSl1o(" ");
        ctx.f.setSm1o(" ");
        ctx.f.setSd1o(" ");
        ctx.f.setSa2o(" ");
        ctx.f.setSc2o(" ");
        ctx.f.setSo2o(" ");
        ctx.f.setSl2o(" ");
        ctx.f.setSm2o(" ");
        ctx.f.setSd2o(" ");
        ctx.f.setSa3o(" ");
        ctx.f.setSc3o(" ");
        ctx.f.setSo3o(" ");
        ctx.f.setSl3o(" ");
        ctx.f.setSm3o(" ");
        ctx.f.setSd3o(" ");
        ctx.f.setSa4o(" ");
        ctx.f.setSc4o(" ");
        ctx.f.setSo4o(" ");
        ctx.f.setSl4o(" ");
        ctx.f.setSm4o(" ");
        ctx.f.setSd4o(" ");
        ctx.f.setSa5o(" ");
        ctx.f.setSc5o(" ");
        ctx.f.setSo5o(" ");
        ctx.f.setSl5o(" ");
        ctx.f.setSm5o(" ");
        ctx.f.setSd5o(" ");
        ctx.f.setSa6o(" ");
        ctx.f.setSc6o(" ");
        ctx.f.setSo6o(" ");
        ctx.f.setSl6o(" ");
        ctx.f.setSm6o(" ");
        ctx.f.setSd6o(" ");
    }

    /** COBOL paragraph: 4000-MOVE-ROWS */
    private void moveRowsToScreen(TaskContext ctx) {
        for (ctx.f.setWsIdx(1);
                ctx.f.getWsIdx() <= ctx.f.getKsbRowCnt();
                ctx.f.setWsIdx(ctx.f.getWsIdx() + 1)) {
            formatRow(ctx);
            placeRowOnScreen(ctx);
        }
    }

    /** COBOL paragraph: 4100-FORMAT-ROW */
    private void formatRow(TaskContext ctx) {
        ctx.f.setWsRAcct(String.format("%011d", ctx.f.getKsbRAcct(ctx.f.getWsIdx())));
        ctx.f.setWsRCycle(String.format("%06d", ctx.f.getKsbRCycle(ctx.f.getWsIdx())));
        ctx.f.setWsEdMoney(ctx.f.getKsbROpen(ctx.f.getWsIdx()));
        ctx.f.setWsROpen(ctx.f.getString("WS-ED-MONEY"));
        ctx.f.setWsEdMoney(ctx.f.getKsbRClose(ctx.f.getWsIdx()));
        ctx.f.setWsRClose(ctx.f.getString("WS-ED-MONEY"));
        ctx.f.setWsEdMoney(ctx.f.getKsbRMindue(ctx.f.getWsIdx()));
        ctx.f.setWsRMin(ctx.f.getString("WS-ED-MONEY"));
        ctx.f.setWsRDue(ctx.f.getKsbRDuedt(ctx.f.getWsIdx()));
    }

    /** COBOL paragraph: 4200-PLACE-ROW */
    private void placeRowOnScreen(TaskContext ctx) {
        switch (ctx.f.getWsIdx()) {
            case 1 -> {
                ctx.f.setSa1o(ctx.f.getWsRAcct());
                ctx.f.setSc1o(ctx.f.getWsRCycle());
                ctx.f.setSo1o(ctx.f.getWsROpen());
                ctx.f.setSl1o(ctx.f.getWsRClose());
                ctx.f.setSm1o(ctx.f.getWsRMin());
                ctx.f.setSd1o(ctx.f.getWsRDue());
            }
            case 2 -> {
                ctx.f.setSa2o(ctx.f.getWsRAcct());
                ctx.f.setSc2o(ctx.f.getWsRCycle());
                ctx.f.setSo2o(ctx.f.getWsROpen());
                ctx.f.setSl2o(ctx.f.getWsRClose());
                ctx.f.setSm2o(ctx.f.getWsRMin());
                ctx.f.setSd2o(ctx.f.getWsRDue());
            }
            case 3 -> {
                ctx.f.setSa3o(ctx.f.getWsRAcct());
                ctx.f.setSc3o(ctx.f.getWsRCycle());
                ctx.f.setSo3o(ctx.f.getWsROpen());
                ctx.f.setSl3o(ctx.f.getWsRClose());
                ctx.f.setSm3o(ctx.f.getWsRMin());
                ctx.f.setSd3o(ctx.f.getWsRDue());
            }
            case 4 -> {
                ctx.f.setSa4o(ctx.f.getWsRAcct());
                ctx.f.setSc4o(ctx.f.getWsRCycle());
                ctx.f.setSo4o(ctx.f.getWsROpen());
                ctx.f.setSl4o(ctx.f.getWsRClose());
                ctx.f.setSm4o(ctx.f.getWsRMin());
                ctx.f.setSd4o(ctx.f.getWsRDue());
            }
            case 5 -> {
                ctx.f.setSa5o(ctx.f.getWsRAcct());
                ctx.f.setSc5o(ctx.f.getWsRCycle());
                ctx.f.setSo5o(ctx.f.getWsROpen());
                ctx.f.setSl5o(ctx.f.getWsRClose());
                ctx.f.setSm5o(ctx.f.getWsRMin());
                ctx.f.setSd5o(ctx.f.getWsRDue());
            }
            case 6 -> {
                ctx.f.setSa6o(ctx.f.getWsRAcct());
                ctx.f.setSc6o(ctx.f.getWsRCycle());
                ctx.f.setSo6o(ctx.f.getWsROpen());
                ctx.f.setSl6o(ctx.f.getWsRClose());
                ctx.f.setSm6o(ctx.f.getWsRMin());
                ctx.f.setSd6o(ctx.f.getWsRDue());
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
        fetchCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnly(TaskContext ctx) {
        populateHeader(ctx);
        ctx.appService.sendMap(
                "MSTMINA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8500-GET-DATE-TIME */
    private void fetchCurrentDateTime(TaskContext ctx) {
        ctx.appService.askTime();
        com.appruntime.FormatTimeResult _ftResult =
                ctx.appService.formatTime(null, "YYYYMMDD", "-", ":");
        ctx.f.setWsHdrDate(_ftResult.getDate());
        ctx.f.setWsHdrTime(_ftResult.getTime());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 9000-RETURN */
    private void returnToCics(TaskContext ctx) {
        ctx.f.setCaWorkArea(
                Utility.setSubstring(
                        String.valueOf(ctx.f.getCaWorkArea()),
                        1,
                        36,
                        String.valueOf(ctx.f.getWsPageState())));
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /** Check whether the last field-validation pass failed. */
    private boolean isValidationFailed(TaskContext ctx) {
        return ctx.f.getWsValidSw().equals("N");
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcstminFields f;

        final AppService appService;

        final OcstminLinkParm link = new OcstminLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcstminFields(ws);
        }
    }
}

package com.generated.orion.octsrch.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OctsrchLinkParm;
import com.generated.orion.octsrch.accessor.OctsrchFields;
import com.generated.orion.octsrch.metadata.OctsrchBmsMetadata;
import com.generated.orion.octsrch.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCTSRCH. */
@Service
public class OctsrchService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OctsrchService.class);

    // TODO(layer1): cross-cohort pattern — promote to batch-common Constants
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
        return "OCTSRCH";
    }

    @Override
    public String getTransId() {
        return "ORTS";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OctsrchBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OctsrchBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OctsrchBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
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
                processInput(ctx);
            }
        }
        returnToTransaction(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MTSRCHAO");
        populateScreenHeader(ctx);
        clearSummaryRows(ctx);
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setCardnuml((short) -1);
        ctx.appService.sendMap(
                "MTSRCHA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
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
            searchTransactions(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-SEARCH */
    private void searchTransactions(TaskContext ctx) {
        ctx.appService.receiveMap("MTSRCHA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MTSRCHAI");
        }
        validateSearchCriteria(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            ctx.f.setCardnuml((short) -1);
            sendDataOnlyScreen(ctx);
        } else {
            browseTransactions(ctx);
            buildResultSummary(ctx);
            ctx.f.setCardnuml((short) -1);
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2200-BUILD-SUMMARY */
    private void buildResultSummary(TaskContext ctx) {
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

    /** COBOL paragraph: 3000-BROWSE-TRANS */
    private void browseTransactions(TaskContext ctx) {
        ctx.f.setWsRowCnt(0);
        ctx.f.setWsEndFlg("N");
        ctx.f.setString("WS-BROWSE-STARTED", "N");
        clearSummaryRows(ctx);
        startTransactionBrowse(ctx);
        if (ctx.f.getWsBrowseStarted().equals("Y")) {
            while (!ctx.f.getWsEndFlg().equals("Y") && ctx.f.getWsRowCnt() < ctx.f.getWsMaxRows()) {
                readNextTransaction(ctx);
            }
            endTransactionBrowse(ctx);
        }
    }

    /** COBOL paragraph: 3050-CLEAR-ROWS */
    private void clearSummaryRows(TaskContext ctx) {
        ctx.f.setSr1o(" ");
        ctx.f.setSr2o(" ");
        ctx.f.setSr3o(" ");
        ctx.f.setSr4o(" ");
        ctx.f.setSr5o(" ");
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
            if (Utility.fieldEquals(ctx.f.getTrCardNum(), ctx.f.getWsCardKey())
                    && (ctx.f.getTrAmt().compareTo(ctx.f.getWsFromAmt()) >= 0)
                    && (ctx.f.getTrAmt().compareTo(ctx.f.getWsToAmt()) <= 0)) {
                ctx.f.setWsRowCnt(ctx.f.getWsRowCnt() + 1);
                populateSummaryRow(ctx);
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
    private void populateSummaryRow(TaskContext ctx) {
        ctx.f.setWsEdAmt(ctx.f.getTrAmt());
        ctx.f.setWsAmtDisp(
                Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-AMT")), 16)
                        .substring(1, 16));
        ctx.f.setWsSrLine(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(ctx.f.getTrId()));
            sb.append(String.valueOf(" "));
            sb.append(String.valueOf(ctx.f.getTrTypeCd()));
            sb.append(String.valueOf(" "));
            sb.append(String.valueOf(ctx.f.getWsAmtDisp()));
            sb.append(String.valueOf(" "));
            sb.append(String.valueOf(ctx.f.getTrMerchantName()));
            ctx.f.setWsSrLine(sb.toString());
        }
        switch (ctx.f.getWsRowCnt()) {
            case 1 -> {
                ctx.f.setSr1o(ctx.f.getWsSrLine());
            }
            case 2 -> {
                ctx.f.setSr2o(ctx.f.getWsSrLine());
            }
            case 3 -> {
                ctx.f.setSr3o(ctx.f.getWsSrLine());
            }
            case 4 -> {
                ctx.f.setSr4o(ctx.f.getWsSrLine());
            }
            case 5 -> {
                ctx.f.setSr5o(ctx.f.getWsSrLine());
            }
            default -> {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: 6100-VALIDATE-CRIT */
    private void validateSearchCriteria(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setErrmsgo(" ");
        if (Utility.fieldEquals(ctx.f.getCardnumi(), " ") || ctx.f.isAllLowValues("CARDNUMI")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMCardReq());
            exitValidation(ctx);
            return;
        }
        ctx.f.setWsCardKey(ctx.f.getCardnumi());
        ctx.f.setWsCardKey(
                String.valueOf(ctx.f.getWsCardKey())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        ctx.f.setWsAeIn(ctx.f.getFramti());
        ctx.f.setWsAeIn(
                String.valueOf(ctx.f.getWsAeIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getWsAeIn(), " ")) {
            ctx.f.setWsFromAmt(java.math.BigDecimal.valueOf(0));
        } else {
            parseAmountInput(ctx);
            if (ctx.f.getWsValidSw().equals("N")) {
                ctx.f.setErrmsgo(ctx.f.getWsMFromBad());
                exitValidation(ctx);
                return;
            }
            ctx.f.setWsFromAmt(ctx.f.getWsAeResult());
        }
        ctx.f.setWsAeIn(ctx.f.getToamti());
        ctx.f.setWsAeIn(
                String.valueOf(ctx.f.getWsAeIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getWsAeIn(), " ")) {
            ctx.f.setWsToAmt(new java.math.BigDecimal("9999999999.99"));
        } else {
            parseAmountInput(ctx);
            if (ctx.f.getWsValidSw().equals("N")) {
                ctx.f.setErrmsgo(ctx.f.getWsMToBad());
                exitValidation(ctx);
                return;
            }
            ctx.f.setWsToAmt(ctx.f.getWsAeResult());
        }
        if ((ctx.f.getWsFromAmt().compareTo(ctx.f.getWsToAmt()) > 0)) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMRange());
        }
    }

    /** COBOL paragraph: 6100-EXIT */
    private void exitValidation(TaskContext ctx) {
        return;
    }

    /** COBOL paragraph: 5100-PARSE-AMOUNT */
    private void parseAmountInput(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsAeInt(0);
        ctx.f.setWsAeIntCnt(0);
        ctx.f.setWsAeFrac(0);
        ctx.f.setWsAeFracCnt(0);
        ctx.f.setWsAeDotSw("N");
        ctx.f.setWsAeResult(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsAeIn(
                String.valueOf(ctx.f.getWsAeIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getWsAeIn(), " ")) {
            ctx.f.setString("WS-VALID-SW", "N");
            return;
        }
        for (ctx.f.setWsAePos(1);
                ctx.f.getWsAePos() <= 13;
                ctx.f.setWsAePos(ctx.f.getWsAePos() + 1)) {
            ctx.f.setWsAeChar(
                    Utility.padRight(String.valueOf(ctx.f.getWsAeIn()), 256)
                            .substring(ctx.f.getWsAePos() - 1, ctx.f.getWsAePos() - 1 + 1));
            if (Utility.fieldEquals(ctx.f.getWsAeChar(), " ")) {
                /* CONTINUE */
            } else if (Utility.fieldEquals(ctx.f.getWsAeChar(), ",")) {
                /* CONTINUE */
            } else if (Utility.fieldEquals(ctx.f.getWsAeChar(), ".")) {
                if (Utility.fieldEquals(ctx.f.getWsAeDotSw(), "Y")) {
                    ctx.f.setString("WS-VALID-SW", "N");
                } else {
                    ctx.f.setWsAeDotSw("Y");
                }
            } else if ((ctx.f.getWsAeChar().compareTo("0") >= 0)
                    && (ctx.f.getWsAeChar().compareTo("9") <= 0)) {
                accumulateAmountDigit(ctx);
            } else {
                ctx.f.setString("WS-VALID-SW", "N");
            }
        }
        if (ctx.f.getWsAeFracCnt() == 1) {
            ctx.f.setWsAeFrac((ctx.f.getWsAeFrac() * 10));
        }
        ctx.f.setWsAeResult(
                java.math.BigDecimal.valueOf(ctx.f.getWsAeInt())
                        .add(
                                java.math.BigDecimal.valueOf(ctx.f.getWsAeFrac())
                                        .divide(
                                                java.math.BigDecimal.valueOf(100),
                                                12,
                                                java.math.RoundingMode.HALF_UP)));
    }

    /** COBOL paragraph: 5150-ACCUM-DIGIT */
    private void accumulateAmountDigit(TaskContext ctx) {
        ctx.f.setWsAeDigit(Utility.parseNumeric(ctx.f.getWsAeChar()).intValue());
        if (Utility.fieldEquals(ctx.f.getWsAeDotSw(), "Y")) {
            ctx.f.setWsAeFracCnt(ctx.f.getWsAeFracCnt() + 1);
            if (ctx.f.getWsAeFracCnt() > 2) {
                ctx.f.setString("WS-VALID-SW", "N");
            } else {
                ctx.f.setWsAeFrac(((ctx.f.getWsAeFrac() * 10) + ctx.f.getWsAeDigit()));
            }
        } else {
            ctx.f.setWsAeIntCnt(ctx.f.getWsAeIntCnt() + 1);
            if (ctx.f.getWsAeIntCnt() > 10) {
                ctx.f.setString("WS-VALID-SW", "N");
            } else {
                ctx.f.setWsAeInt(((ctx.f.getWsAeInt() * (long) 10) + ctx.f.getWsAeDigit()));
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
    private void populateScreenHeader(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        loadCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnlyScreen(TaskContext ctx) {
        populateScreenHeader(ctx);
        ctx.appService.sendMap(
                "MTSRCHA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
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
    private void returnToTransaction(TaskContext ctx) {
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OctsrchFields f;

        final AppService appService;

        final OctsrchLinkParm link = new OctsrchLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OctsrchFields(ws);
        }
    }
}

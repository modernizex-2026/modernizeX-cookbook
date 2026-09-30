package com.generated.orion.ocanlin.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcanlinLinkParm;
import com.generated.orion.ocanlin.accessor.OcanlinFields;
import com.generated.orion.ocanlin.metadata.OcanlinBmsMetadata;
import com.generated.orion.ocanlin.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCANLIN. */
@Service
public class OcanlinService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcanlinService.class);

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
        return "OCANLIN";
    }

    @Override
    public String getTransId() {
        return "ORAN";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcanlinBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcanlinBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcanlinBmsMetadata.getFieldMapping(mapName);
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
            ctx.f.setWsAnlState(
                    Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 11).substring(0, 11));
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitialScreen(ctx);
            } else {
                dispatchAidKey(ctx);
            }
        }
        returnTransaction(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MANLINAO");
        populateScreenHeader(ctx);
        clearResultRows(ctx);
        ctx.f.setAnheado(" ");
        ctx.f.setAntot1o(" ");
        ctx.f.setAntot2o(" ");
        ctx.f.setWsAnlState("");
        ctx.f.setWsAsMore("N");
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setAnmodel((short) -1);
        ctx.appService.sendMap(
                "MANLINA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void dispatchAidKey(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            xctlToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "7")) {
            restartAnalytics(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "8")) {
            pageForward(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            runAnalyticsRequest(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-RUN */
    private void runAnalyticsRequest(TaskContext ctx) {
        ctx.appService.receiveMap("MANLINA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MANLINAI");
        }
        ctx.f.setWsModeIn(ctx.f.getAnmodei());
        ctx.f.setWsModeIn(
                String.valueOf(ctx.f.getWsModeIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        normalizeModeCode(ctx);
        if (Utility.fieldEquals(ctx.f.getWsModeCode(), " ")) {
            ctx.f.setErrmsgo(ctx.f.getWsMBadMode());
            ctx.f.setAnmodel((short) -1);
            sendDataOnlyScreen(ctx);
            return;
        }
        ctx.f.setWsAsMode(ctx.f.getWsModeCode());
        ctx.f.setWsAsOffset(0);
        executeAnalyticsLink(ctx);
        buildSummaryMessage(ctx);
        ctx.f.setAnmodel((short) -1);
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 2200-PAGE-FWD */
    private void pageForward(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getWsAsMode(), " ")) {
            sendInitialScreen(ctx);
            return;
        }
        if (Utility.fieldEquals(ctx.f.getWsAsMore(), "Y")) {
            ctx.f.setWsAsOffset(ctx.f.getWsAsOffset() + ctx.f.getWsPageSize());
            executeAnalyticsLink(ctx);
            buildSummaryMessage(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMEndRows());
        }
        ctx.f.setAnmodel((short) -1);
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 2300-RESTART */
    private void restartAnalytics(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getWsAsMode(), " ")) {
            sendInitialScreen(ctx);
            return;
        }
        ctx.f.setWsAsOffset(0);
        executeAnalyticsLink(ctx);
        buildSummaryMessage(ctx);
        ctx.f.setAnmodel((short) -1);
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 2400-BUILD-SUMMARY */
    private void buildSummaryMessage(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getKabStatus(), "99")) {
            ctx.f.setErrmsgo(ctx.f.getWsMCompErr());
        } else if (ctx.f.getKabRowCnt() == 0) {
            ctx.f.setErrmsgo(ctx.f.getWsMNoneFound());
        } else {
            calculatePageNumbers(ctx);
            ctx.f.setWsCntEd(ctx.f.getKabRowCnt());
            ctx.f.setWsEdPg(ctx.f.getWsPageNo());
            ctx.f.setWsEdPgt(ctx.f.getWsPageTot());
            ctx.f.setErrmsgo(" ");
            {
                StringBuilder sb = new StringBuilder();
                sb.append(String.valueOf(ctx.f.getString("WS-CNT-ED")));
                sb.append(String.valueOf(" row(s)  page "));
                sb.append(String.valueOf(ctx.f.getString("WS-ED-PG")));
                sb.append(String.valueOf(" of "));
                sb.append(String.valueOf(ctx.f.getString("WS-ED-PGT")));
                ctx.f.setErrmsgo(sb.toString());
            }
        }
    }

    /** COBOL paragraph: 2450-PAGE-CALC */
    private void calculatePageNumbers(TaskContext ctx) {
        ctx.f.setWsPageNo(((ctx.f.getWsAsOffset() / ctx.f.getWsPageSize()) + 1));
        ctx.f.setWsPageTot(
                (((ctx.f.getWsAsRescnt() + ctx.f.getWsPageSize()) - 1) / ctx.f.getWsPageSize()));
        if (ctx.f.getWsPageTot() < ctx.f.getWsPageNo()) {
            ctx.f.setWsPageTot(ctx.f.getWsPageNo());
        }
    }

    /** COBOL paragraph: 3000-DO-ANALYTICS */
    private void executeAnalyticsLink(TaskContext ctx) {
        clearResultRows(ctx);
        ctx.f.setKabMode(ctx.f.getWsAsMode());
        ctx.f.setKabStartOff(ctx.f.getWsAsOffset());
        ctx.f.setKabMaxRows(ctx.f.getWsPageSize());
        {
            byte[] _linkCa = ctx.f.sliceBytes("KANLB-PARM");
            ctx.appService.link(ctx.f.getWsSubPgm(), _linkCa, 490);
            ctx.f.writeBytes("KANLB-PARM", _linkCa);
        }
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setKabStatus("99");
            ctx.f.setKabRowCnt(0);
            ctx.f.setWsAsMore("N");
            return;
        }
        ctx.f.setWsAsRescnt(ctx.f.getKabResultCnt());
        ctx.f.setWsAsMore(ctx.f.getKabMore());
        setModeHeader(ctx);
        if (ctx.f.getKabRowCnt() > 0) {
            moveResultRows(ctx);
            buildModeTotals(ctx);
        } else {
            ctx.f.setAntot1o(" ");
            ctx.f.setAntot2o(" ");
        }
    }

    /** COBOL paragraph: 3050-CLEAR-ROWS */
    private void clearResultRows(TaskContext ctx) {
        ctx.f.setAnk1o(" ");
        ctx.f.setAni1o(" ");
        ctx.f.setAnc1o(" ");
        ctx.f.setAna1o(" ");
        ctx.f.setAnv1o(" ");
        ctx.f.setAnk2o(" ");
        ctx.f.setAni2o(" ");
        ctx.f.setAnc2o(" ");
        ctx.f.setAna2o(" ");
        ctx.f.setAnv2o(" ");
        ctx.f.setAnk3o(" ");
        ctx.f.setAni3o(" ");
        ctx.f.setAnc3o(" ");
        ctx.f.setAna3o(" ");
        ctx.f.setAnv3o(" ");
        ctx.f.setAnk4o(" ");
        ctx.f.setAni4o(" ");
        ctx.f.setAnc4o(" ");
        ctx.f.setAna4o(" ");
        ctx.f.setAnv4o(" ");
        ctx.f.setAnk5o(" ");
        ctx.f.setAni5o(" ");
        ctx.f.setAnc5o(" ");
        ctx.f.setAna5o(" ");
        ctx.f.setAnv5o(" ");
        ctx.f.setAnk6o(" ");
        ctx.f.setAni6o(" ");
        ctx.f.setAnc6o(" ");
        ctx.f.setAna6o(" ");
        ctx.f.setAnv6o(" ");
    }

    /** COBOL paragraph: 3100-SET-HEADER */
    private void setModeHeader(TaskContext ctx) {
        ctx.f.setAnmodeo(ctx.f.getWsAsMode());
        switch (Utility.rtrim(ctx.f.getWsAsMode())) {
            case "RW" -> {
                ctx.f.setAnheado(ctx.f.getWsHRw());
            }
            case "FR" -> {
                ctx.f.setAnheado(ctx.f.getWsHFr());
            }
            case "GL" -> {
                ctx.f.setAnheado(ctx.f.getWsHGl());
            }
            case "RC" -> {
                ctx.f.setAnheado(ctx.f.getWsHRc());
            }
            default -> {
                ctx.f.setAnheado(" ");
            }
        }
    }

    /** COBOL paragraph: 3200-BUILD-TOTALS */
    private void buildModeTotals(TaskContext ctx) {
        ctx.f.setAntot1o(" ");
        ctx.f.setAntot2o(" ");
        switch (Utility.rtrim(ctx.f.getWsAsMode())) {
            case "RW" -> {
                buildRewardsTotals(ctx);
            }
            case "FR" -> {
                buildFraudTotals(ctx);
            }
            case "GL" -> {
                buildGlTotals(ctx);
            }
            case "RC" -> {
                buildReconTotals(ctx);
            }
            default -> {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: 3210-TOT-RW */
    private void buildRewardsTotals(TaskContext ctx) {
        ctx.f.setWsEdTotal(ctx.f.getKabTot1());
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("REWARDS - Purchase amount: "));
            sb.append(String.valueOf(ctx.f.getString("WS-ED-TOTAL")));
            ctx.f.setAntot1o(sb.toString());
        }
        ctx.f.setWsEdBig(ctx.f.getKabTot2().longValue());
        ctx.f.setWsEdNum4(ctx.f.getWsAsRescnt());
        ctx.f.setWsEdNum(ctx.f.getKabScanned());
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("Points: "));
            sb.append(String.valueOf(ctx.f.getString("WS-ED-BIG")));
            sb.append(String.valueOf("  Cards: "));
            sb.append(String.valueOf(ctx.f.getString("WS-ED-NUM4")));
            sb.append(String.valueOf("  Scanned: "));
            sb.append(String.valueOf(ctx.f.getString("WS-ED-NUM")));
            ctx.f.setAntot2o(sb.toString());
        }
    }

    /** COBOL paragraph: 3220-TOT-FR */
    private void buildFraudTotals(TaskContext ctx) {
        ctx.f.setWsEdTotal(ctx.f.getKabTot1());
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("FRAUD - Flagged amount: "));
            sb.append(String.valueOf(ctx.f.getString("WS-ED-TOTAL")));
            ctx.f.setAntot1o(sb.toString());
        }
        ctx.f.setWsEdNum(ctx.f.getKabTot2().intValue());
        ctx.f.setWsEdBig(ctx.f.getKabScanned());
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("Flagged cards: "));
            sb.append(String.valueOf(ctx.f.getString("WS-ED-NUM")));
            sb.append(String.valueOf("  Scanned: "));
            sb.append(String.valueOf(ctx.f.getString("WS-ED-BIG")));
            ctx.f.setAntot2o(sb.toString());
        }
    }

    /** COBOL paragraph: 3230-TOT-GL */
    private void buildGlTotals(TaskContext ctx) {
        ctx.f.setWsEdTotal(ctx.f.getKabTot1());
        ctx.f.setWsEdTotal2(ctx.f.getKabTot2());
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("GL - Debit: "));
            sb.append(String.valueOf(ctx.f.getString("WS-ED-TOTAL")));
            sb.append(String.valueOf("   Credit: "));
            sb.append(String.valueOf(ctx.f.getString("WS-ED-TOTAL2")));
            ctx.f.setAntot1o(sb.toString());
        }
        ctx.f.setWsEdNum4(ctx.f.getWsAsRescnt());
        ctx.f.setWsEdNum(ctx.f.getKabScanned());
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("Types: "));
            sb.append(String.valueOf(ctx.f.getString("WS-ED-NUM4")));
            sb.append(String.valueOf("   Transactions scanned: "));
            sb.append(String.valueOf(ctx.f.getString("WS-ED-NUM")));
            ctx.f.setAntot2o(sb.toString());
        }
    }

    /** COBOL paragraph: 3240-TOT-RC */
    private void buildReconTotals(TaskContext ctx) {
        ctx.f.setWsEdTotal(ctx.f.getKabTot1());
        ctx.f.setWsEdTotal2(ctx.f.getKabTot2());
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("RECON - Debit: "));
            sb.append(String.valueOf(ctx.f.getString("WS-ED-TOTAL")));
            sb.append(String.valueOf("  Credit: "));
            sb.append(String.valueOf(ctx.f.getString("WS-ED-TOTAL2")));
            ctx.f.setAntot1o(sb.toString());
        }
        ctx.f.setWsEdNum4(ctx.f.getKabDisc());
        ctx.f.setWsEdCnt(ctx.f.getWsAsRescnt());
        ctx.f.setWsEdNum(ctx.f.getKabScanned());
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("Discrepancies: "));
            sb.append(String.valueOf(ctx.f.getString("WS-ED-NUM4")));
            sb.append(String.valueOf("  Accounts: "));
            sb.append(String.valueOf(ctx.f.getString("WS-ED-CNT")));
            sb.append(String.valueOf("  Scanned: "));
            sb.append(String.valueOf(ctx.f.getString("WS-ED-NUM")));
            ctx.f.setAntot2o(sb.toString());
        }
    }

    /** COBOL paragraph: 4000-MOVE-ROWS */
    private void moveResultRows(TaskContext ctx) {
        for (ctx.f.setWsIdx(1);
                ctx.f.getWsIdx() <= ctx.f.getKabRowCnt();
                ctx.f.setWsIdx(ctx.f.getWsIdx() + 1)) {
            formatResultRow(ctx);
            placeResultRow(ctx);
        }
    }

    /** COBOL paragraph: 4100-FORMAT-ROW */
    private void formatResultRow(TaskContext ctx) {
        ctx.f.setWsFKey(ctx.f.getKabRKey(ctx.f.getWsIdx()));
        ctx.f.setWsFInfo(ctx.f.getKabRInfo(ctx.f.getWsIdx()));
        ctx.f.setWsEdCnt(ctx.f.getKabRCnt(ctx.f.getWsIdx()));
        ctx.f.setWsFCnt(ctx.f.getString("WS-ED-CNT"));
        ctx.f.setWsEdMoney(ctx.f.getKabRAmt(ctx.f.getWsIdx()));
        ctx.f.setWsFAmt(ctx.f.getString("WS-ED-MONEY"));
        if (Utility.fieldEquals(ctx.f.getWsAsMode(), "RW")) {
            ctx.f.setWsEdPts(ctx.f.getKabRVal(ctx.f.getWsIdx()).longValue());
            ctx.f.setWsFVal(ctx.f.getString("WS-ED-PTS"));
        } else {
            ctx.f.setWsEdMoney(ctx.f.getKabRVal(ctx.f.getWsIdx()));
            ctx.f.setWsFVal(ctx.f.getString("WS-ED-MONEY"));
        }
    }

    /** COBOL paragraph: 4200-PLACE-ROW */
    private void placeResultRow(TaskContext ctx) {
        switch (ctx.f.getWsIdx()) {
            case 1 -> {
                ctx.f.setAnk1o(ctx.f.getWsFKey());
                ctx.f.setAni1o(ctx.f.getWsFInfo());
                ctx.f.setAnc1o(ctx.f.getWsFCnt());
                ctx.f.setAna1o(ctx.f.getWsFAmt());
                ctx.f.setAnv1o(ctx.f.getWsFVal());
            }
            case 2 -> {
                ctx.f.setAnk2o(ctx.f.getWsFKey());
                ctx.f.setAni2o(ctx.f.getWsFInfo());
                ctx.f.setAnc2o(ctx.f.getWsFCnt());
                ctx.f.setAna2o(ctx.f.getWsFAmt());
                ctx.f.setAnv2o(ctx.f.getWsFVal());
            }
            case 3 -> {
                ctx.f.setAnk3o(ctx.f.getWsFKey());
                ctx.f.setAni3o(ctx.f.getWsFInfo());
                ctx.f.setAnc3o(ctx.f.getWsFCnt());
                ctx.f.setAna3o(ctx.f.getWsFAmt());
                ctx.f.setAnv3o(ctx.f.getWsFVal());
            }
            case 4 -> {
                ctx.f.setAnk4o(ctx.f.getWsFKey());
                ctx.f.setAni4o(ctx.f.getWsFInfo());
                ctx.f.setAnc4o(ctx.f.getWsFCnt());
                ctx.f.setAna4o(ctx.f.getWsFAmt());
                ctx.f.setAnv4o(ctx.f.getWsFVal());
            }
            case 5 -> {
                ctx.f.setAnk5o(ctx.f.getWsFKey());
                ctx.f.setAni5o(ctx.f.getWsFInfo());
                ctx.f.setAnc5o(ctx.f.getWsFCnt());
                ctx.f.setAna5o(ctx.f.getWsFAmt());
                ctx.f.setAnv5o(ctx.f.getWsFVal());
            }
            case 6 -> {
                ctx.f.setAnk6o(ctx.f.getWsFKey());
                ctx.f.setAni6o(ctx.f.getWsFInfo());
                ctx.f.setAnc6o(ctx.f.getWsFCnt());
                ctx.f.setAna6o(ctx.f.getWsFAmt());
                ctx.f.setAnv6o(ctx.f.getWsFVal());
            }
            default -> {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: 5000-NORMALIZE-MODE */
    private void normalizeModeCode(TaskContext ctx) {
        ctx.f.setWsModeCode(" ");
        ctx.f.setWsModeUp(String.valueOf(ctx.f.getWsModeIn()).toUpperCase());
        switch (Utility.rtrim(ctx.f.getWsModeUp())) {
            case "RW" -> {
                ctx.f.setWsModeCode("RW");
            }
            case "REWARDS" -> {
                ctx.f.setWsModeCode("RW");
            }
            case "FR" -> {
                ctx.f.setWsModeCode("FR");
            }
            case "FRAUD" -> {
                ctx.f.setWsModeCode("FR");
            }
            case "GL" -> {
                ctx.f.setWsModeCode("GL");
            }
            case "RC" -> {
                ctx.f.setWsModeCode("RC");
            }
            case "RECON" -> {
                ctx.f.setWsModeCode("RC");
            }
            default -> {
                ctx.f.setWsModeCode(" ");
            }
        }
    }

    /** COBOL paragraph: 7000-XCTL-MENU */
    private void xctlToMenu(TaskContext ctx) {
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
                "MANLINA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
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
    private void returnTransaction(TaskContext ctx) {
        ctx.f.setCaWorkArea(
                Utility.setSubstring(
                        String.valueOf(ctx.f.getCaWorkArea()),
                        1,
                        11,
                        String.valueOf(ctx.f.getWsAnlState())));
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcanlinFields f;

        final AppService appService;

        final OcanlinLinkParm link = new OcanlinLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcanlinFields(ws);
        }
    }
}

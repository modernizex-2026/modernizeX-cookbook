package com.generated.orion.occrdin.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OccrdinLinkParm;
import com.generated.orion.occrdin.accessor.OccrdinFields;
import com.generated.orion.occrdin.metadata.OccrdinBmsMetadata;
import com.generated.orion.occrdin.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCCRDIN. */
@Service
public class OccrdinService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OccrdinService.class);

    /** Length (bytes) of the ORION-COMMAREA passed across program transfers. */
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
        return "OCCRDIN";
    }

    @Override
    public String getTransId() {
        return "ORCI";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OccrdinBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OccrdinBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OccrdinBmsMetadata.getFieldMapping(mapName);
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
                processUserInput(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MCRDINAO");
        ctx.f.setKcrdinArea("");
        populateScreenHeader(ctx);
        clearDisplayRows(ctx);
        ctx.f.setWsStFilter("A");
        ctx.f.fillLowValues("WS-ST-NEXT-KEY");
        ctx.f.setWsStPageNo(0);
        ctx.f.setWsStAct(0);
        ctx.f.setWsStIna(0);
        ctx.f.setWsStMore("N");
        ctx.f.setWsStListed("N");
        ctx.f.setCaWorkArea(
                Utility.setSubstring(
                        String.valueOf(ctx.f.getCaWorkArea()),
                        1,
                        40,
                        String.valueOf(ctx.f.getWsState())));
        ctx.f.setFdesco("ALL");
        showPageTotals(ctx);
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        resetFilterFieldLength(ctx);
        ctx.appService.sendMap(
                "MCRDINA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processUserInput(TaskContext ctx) {
        ctx.f.setWsState(
                Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 40).substring(0, 40));
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "@")) {
            transferToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "_")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            applyFilterCriteria(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "7")) {
            restartListing(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "8")) {
            pageForward(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            refreshFilterDescription(ctx);
            sendDataOnly(ctx);
        }
    }

    /** COBOL paragraph: 2050-RECEIVE */
    private void receiveScreenInput(TaskContext ctx) {
        ctx.appService.receiveMap("MCRDINA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MCRDINAI");
        }
    }

    /** COBOL paragraph: 2100-APPLY-FILTER */
    private void applyFilterCriteria(TaskContext ctx) {
        receiveScreenInput(ctx);
        parseFilterInput(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            ctx.f.setErrmsgo(ctx.f.getWsMBadFilter());
            resetFilterFieldLength(ctx);
            sendDataOnly(ctx);
        } else {
            ctx.f.setKciFilter(ctx.f.getWsStFilter());
            ctx.f.fillLowValues("KCI-START-KEY");
            ctx.f.setString("KCI-WANT-KPI", "Y");
            ctx.f.setWsStPageNo(1);
            ctx.f.setWsStListed("Y");
            runPageQuery(ctx);
        }
    }

    /** COBOL paragraph: 2200-RESTART */
    private void restartListing(TaskContext ctx) {
        if (!Utility.fieldEquals(ctx.f.getWsStListed(), "Y")) {
            ctx.f.setErrmsgo(ctx.f.getWsMListFirst());
            resetFilterFieldLength(ctx);
            sendDataOnly(ctx);
        } else {
            ctx.f.setKciFilter(ctx.f.getWsStFilter());
            ctx.f.fillLowValues("KCI-START-KEY");
            ctx.f.setString("KCI-WANT-KPI", "Y");
            ctx.f.setWsStPageNo(1);
            runPageQuery(ctx);
        }
    }

    /** COBOL paragraph: 2300-PAGE-FWD */
    private void pageForward(TaskContext ctx) {
        if (!Utility.fieldEquals(ctx.f.getWsStListed(), "Y")) {
            ctx.f.setErrmsgo(ctx.f.getWsMListFirst());
            resetFilterFieldLength(ctx);
            sendDataOnly(ctx);
            return;
        }
        if (!Utility.fieldEquals(ctx.f.getWsStMore(), "Y")) {
            refreshFilterDescription(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMEndFile());
            sendDataOnly(ctx);
            return;
        }
        ctx.f.setKciFilter(ctx.f.getWsStFilter());
        ctx.f.setKciStartKey(ctx.f.getWsStNextKey());
        ctx.f.setString("KCI-WANT-KPI", "N");
        ctx.f.setWsStPageNo(ctx.f.getWsStPageNo() + 1);
        runPageQuery(ctx);
    }

    /** COBOL paragraph: 2400-RUN-PAGE */
    private void runPageQuery(TaskContext ctx) {
        callSubProgram(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            resetFilterFieldLength(ctx);
            sendDataOnly(ctx);
            return;
        }
        if (ctx.f.getKciWantKpi().equals("Y")) {
            ctx.f.setWsStAct(ctx.f.getKciActiveCnt());
            ctx.f.setWsStIna(ctx.f.getKciInactiveCnt());
        }
        ctx.f.setWsStNextKey(ctx.f.getKciNextKey());
        ctx.f.setWsStMore(ctx.f.getKciMoreSw());
        showResultPage(ctx);
        resetFilterFieldLength(ctx);
        sendDataOnly(ctx);
    }

    /** COBOL paragraph: 3000-CALL-SUB */
    private void callSubProgram(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        {
            byte[] _linkCa = ctx.f.sliceBytes("KCRDIN-AREA");
            ctx.appService.link(ctx.f.getWsSubPgm(), _linkCa, 817);
            ctx.f.writeBytes("KCRDIN-AREA", _linkCa);
        }
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setString("WS-VALID-SW", "N");
            refreshFilterDescription(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMLinkErr());
            return;
        }
        if (ctx.f.getKciReturnCd().equals("E")) {
            ctx.f.setString("WS-VALID-SW", "N");
            refreshFilterDescription(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMBrowseErr());
        }
    }

    /** COBOL paragraph: 4000-SHOW-PAGE */
    private void showResultPage(TaskContext ctx) {
        refreshFilterDescription(ctx);
        clearDisplayRows(ctx);
        if (ctx.f.getKciRowCount() == 0) {
            ctx.f.setErrmsgo(ctx.f.getWsMNoneFound());
        } else {
            for (ctx.f.setWsI(1);
                    ctx.f.getWsI() <= ctx.f.getKciRowCount();
                    ctx.f.setWsI(ctx.f.getWsI() + 1)) {
                placeRowOnScreen(ctx);
            }
            buildStatusMessage(ctx);
        }
        showPageTotals(ctx);
    }

    /** COBOL paragraph: 4100-CLEAR-ROWS */
    private void clearDisplayRows(TaskContext ctx) {
        ctx.f.setCnm1o(" ");
        ctx.f.setCac1o(" ");
        ctx.f.setCna1o(" ");
        ctx.f.setCex1o(" ");
        ctx.f.setCst1o(" ");
        ctx.f.setCnm2o(" ");
        ctx.f.setCac2o(" ");
        ctx.f.setCna2o(" ");
        ctx.f.setCex2o(" ");
        ctx.f.setCst2o(" ");
        ctx.f.setCnm3o(" ");
        ctx.f.setCac3o(" ");
        ctx.f.setCna3o(" ");
        ctx.f.setCex3o(" ");
        ctx.f.setCst3o(" ");
        ctx.f.setCnm4o(" ");
        ctx.f.setCac4o(" ");
        ctx.f.setCna4o(" ");
        ctx.f.setCex4o(" ");
        ctx.f.setCst4o(" ");
        ctx.f.setCnm5o(" ");
        ctx.f.setCac5o(" ");
        ctx.f.setCna5o(" ");
        ctx.f.setCex5o(" ");
        ctx.f.setCst5o(" ");
        ctx.f.setCnm6o(" ");
        ctx.f.setCac6o(" ");
        ctx.f.setCna6o(" ");
        ctx.f.setCex6o(" ");
        ctx.f.setCst6o(" ");
        ctx.f.setCnm7o(" ");
        ctx.f.setCac7o(" ");
        ctx.f.setCna7o(" ");
        ctx.f.setCex7o(" ");
        ctx.f.setCst7o(" ");
        ctx.f.setCnm8o(" ");
        ctx.f.setCac8o(" ");
        ctx.f.setCna8o(" ");
        ctx.f.setCex8o(" ");
        ctx.f.setCst8o(" ");
        ctx.f.setCnm9o(" ");
        ctx.f.setCac9o(" ");
        ctx.f.setCna9o(" ");
        ctx.f.setCex9o(" ");
        ctx.f.setCst9o(" ");
        ctx.f.setCnm10o(" ");
        ctx.f.setCac10o(" ");
        ctx.f.setCna10o(" ");
        ctx.f.setCex10o(" ");
        ctx.f.setCst10o(" ");
        ctx.f.setCnm11o(" ");
        ctx.f.setCac11o(" ");
        ctx.f.setCna11o(" ");
        ctx.f.setCex11o(" ");
        ctx.f.setCst11o(" ");
        ctx.f.setCnm12o(" ");
        ctx.f.setCac12o(" ");
        ctx.f.setCna12o(" ");
        ctx.f.setCex12o(" ");
        ctx.f.setCst12o(" ");
        ctx.f.setCnm13o(" ");
        ctx.f.setCac13o(" ");
        ctx.f.setCna13o(" ");
        ctx.f.setCex13o(" ");
        ctx.f.setCst13o(" ");
    }

    /** COBOL paragraph: 4300-PLACE-ROW */
    private void placeRowOnScreen(TaskContext ctx) {
        switch (ctx.f.getWsI()) {
            case 1 -> {
                ctx.f.setCnm1o(ctx.f.getKciRNum(1));
                ctx.f.setCac1o(String.format("%011d", ctx.f.getKciRAcct(1)));
                ctx.f.setCna1o(ctx.f.getKciRName(1));
                ctx.f.setCex1o(ctx.f.getKciRExpiry(1));
                ctx.f.setCst1o(ctx.f.getKciRStatus(1));
            }
            case 2 -> {
                ctx.f.setCnm2o(ctx.f.getKciRNum(2));
                ctx.f.setCac2o(String.format("%011d", ctx.f.getKciRAcct(2)));
                ctx.f.setCna2o(ctx.f.getKciRName(2));
                ctx.f.setCex2o(ctx.f.getKciRExpiry(2));
                ctx.f.setCst2o(ctx.f.getKciRStatus(2));
            }
            case 3 -> {
                ctx.f.setCnm3o(ctx.f.getKciRNum(3));
                ctx.f.setCac3o(String.format("%011d", ctx.f.getKciRAcct(3)));
                ctx.f.setCna3o(ctx.f.getKciRName(3));
                ctx.f.setCex3o(ctx.f.getKciRExpiry(3));
                ctx.f.setCst3o(ctx.f.getKciRStatus(3));
            }
            case 4 -> {
                ctx.f.setCnm4o(ctx.f.getKciRNum(4));
                ctx.f.setCac4o(String.format("%011d", ctx.f.getKciRAcct(4)));
                ctx.f.setCna4o(ctx.f.getKciRName(4));
                ctx.f.setCex4o(ctx.f.getKciRExpiry(4));
                ctx.f.setCst4o(ctx.f.getKciRStatus(4));
            }
            case 5 -> {
                ctx.f.setCnm5o(ctx.f.getKciRNum(5));
                ctx.f.setCac5o(String.format("%011d", ctx.f.getKciRAcct(5)));
                ctx.f.setCna5o(ctx.f.getKciRName(5));
                ctx.f.setCex5o(ctx.f.getKciRExpiry(5));
                ctx.f.setCst5o(ctx.f.getKciRStatus(5));
            }
            case 6 -> {
                ctx.f.setCnm6o(ctx.f.getKciRNum(6));
                ctx.f.setCac6o(String.format("%011d", ctx.f.getKciRAcct(6)));
                ctx.f.setCna6o(ctx.f.getKciRName(6));
                ctx.f.setCex6o(ctx.f.getKciRExpiry(6));
                ctx.f.setCst6o(ctx.f.getKciRStatus(6));
            }
            case 7 -> {
                ctx.f.setCnm7o(ctx.f.getKciRNum(7));
                ctx.f.setCac7o(String.format("%011d", ctx.f.getKciRAcct(7)));
                ctx.f.setCna7o(ctx.f.getKciRName(7));
                ctx.f.setCex7o(ctx.f.getKciRExpiry(7));
                ctx.f.setCst7o(ctx.f.getKciRStatus(7));
            }
            case 8 -> {
                ctx.f.setCnm8o(ctx.f.getKciRNum(8));
                ctx.f.setCac8o(String.format("%011d", ctx.f.getKciRAcct(8)));
                ctx.f.setCna8o(ctx.f.getKciRName(8));
                ctx.f.setCex8o(ctx.f.getKciRExpiry(8));
                ctx.f.setCst8o(ctx.f.getKciRStatus(8));
            }
            case 9 -> {
                ctx.f.setCnm9o(ctx.f.getKciRNum(9));
                ctx.f.setCac9o(String.format("%011d", ctx.f.getKciRAcct(9)));
                ctx.f.setCna9o(ctx.f.getKciRName(9));
                ctx.f.setCex9o(ctx.f.getKciRExpiry(9));
                ctx.f.setCst9o(ctx.f.getKciRStatus(9));
            }
            case 10 -> {
                ctx.f.setCnm10o(ctx.f.getKciRNum(10));
                ctx.f.setCac10o(String.format("%011d", ctx.f.getKciRAcct(10)));
                ctx.f.setCna10o(ctx.f.getKciRName(10));
                ctx.f.setCex10o(ctx.f.getKciRExpiry(10));
                ctx.f.setCst10o(ctx.f.getKciRStatus(10));
            }
            case 11 -> {
                ctx.f.setCnm11o(ctx.f.getKciRNum(11));
                ctx.f.setCac11o(String.format("%011d", ctx.f.getKciRAcct(11)));
                ctx.f.setCna11o(ctx.f.getKciRName(11));
                ctx.f.setCex11o(ctx.f.getKciRExpiry(11));
                ctx.f.setCst11o(ctx.f.getKciRStatus(11));
            }
            case 12 -> {
                ctx.f.setCnm12o(ctx.f.getKciRNum(12));
                ctx.f.setCac12o(String.format("%011d", ctx.f.getKciRAcct(12)));
                ctx.f.setCna12o(ctx.f.getKciRName(12));
                ctx.f.setCex12o(ctx.f.getKciRExpiry(12));
                ctx.f.setCst12o(ctx.f.getKciRStatus(12));
            }
            case 13 -> {
                ctx.f.setCnm13o(ctx.f.getKciRNum(13));
                ctx.f.setCac13o(String.format("%011d", ctx.f.getKciRAcct(13)));
                ctx.f.setCna13o(ctx.f.getKciRName(13));
                ctx.f.setCex13o(ctx.f.getKciRExpiry(13));
                ctx.f.setCst13o(ctx.f.getKciRStatus(13));
            }
            default -> {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: 4400-SHOW-TOTALS */
    private void showPageTotals(TaskContext ctx) {
        ctx.f.setWsEdPg(ctx.f.getWsStPageNo());
        ctx.f.setPagenoo(ctx.f.getString("WS-ED-PG"));
        ctx.f.setWsEdCnt(ctx.f.getWsStAct());
        ctx.f.setActcnto(ctx.f.getString("WS-ED-CNT"));
        ctx.f.setWsEdCnt(ctx.f.getWsStIna());
        ctx.f.setInacnto(ctx.f.getString("WS-ED-CNT"));
    }

    /** COBOL paragraph: 4900-BUILD-MSG */
    private void buildStatusMessage(TaskContext ctx) {
        ctx.f.setWsCntEd(ctx.f.getKciRowCount());
        ctx.f.setErrmsgo(" ");
        String suffix =
                Utility.fieldEquals(ctx.f.getWsStMore(), "Y")
                        ? String.valueOf(ctx.f.getWsMMore())
                        : String.valueOf(ctx.f.getWsMDone());
        ctx.f.setErrmsgo(String.valueOf(ctx.f.getString("WS-CNT-ED")) + suffix);
    }

    /** COBOL paragraph: 6000-PARSE-FILTER */
    private void parseFilterInput(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsFiltIn(ctx.f.getFilti());
        ctx.f.setWsFiltIn(
                String.valueOf(ctx.f.getWsFiltIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        ctx.f.setWsFiltIn(
                Utility.inspectConverting(
                        String.valueOf(ctx.f.getWsFiltIn()),
                        String.valueOf("abcdefghijklmnopqrstuvwxyz"),
                        String.valueOf("ABCDEFGHIJKLMNOPQRSTUVWXYZ")));
        if (Utility.fieldEquals(ctx.f.getWsFiltIn(), " ")) {
            ctx.f.setWsStFilter("A");
        } else if (Utility.fieldEquals(
                Utility.padRight(String.valueOf(ctx.f.getWsFiltIn()), 3).substring(0, 3), "ALL")) {
            ctx.f.setWsStFilter("A");
        } else if (Utility.fieldEquals(
                Utility.padRight(String.valueOf(ctx.f.getWsFiltIn()), 3).substring(0, 3), "ACT")) {
            ctx.f.setWsStFilter("Y");
        } else if (Utility.fieldEquals(
                Utility.padRight(String.valueOf(ctx.f.getWsFiltIn()), 3).substring(0, 3), "INA")) {
            ctx.f.setWsStFilter("N");
        } else if (Utility.fieldEquals(
                Utility.padRight(String.valueOf(ctx.f.getWsFiltIn()), 3).substring(0, 3), "EXP")) {
            ctx.f.setWsStFilter("X");
        } else {
            ctx.f.setString("WS-VALID-SW", "N");
        }
    }

    /** COBOL paragraph: 6100-SET-FDESC */
    private void setFilterDescription(TaskContext ctx) {
        switch (Utility.rtrim(ctx.f.getWsStFilter())) {
            case "A" -> {
                ctx.f.setWsFdesc("ALL");
            }
            case "Y" -> {
                ctx.f.setWsFdesc("ACTIVE");
            }
            case "N" -> {
                ctx.f.setWsFdesc("INACTIVE");
            }
            case "X" -> {
                ctx.f.setWsFdesc("EXPIRING");
            }
            default -> {
                ctx.f.setWsFdesc("ALL");
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
        captureCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnly(TaskContext ctx) {
        ctx.f.setCaWorkArea(
                Utility.setSubstring(
                        String.valueOf(ctx.f.getCaWorkArea()),
                        1,
                        40,
                        String.valueOf(ctx.f.getWsState())));
        populateScreenHeader(ctx);
        ctx.appService.sendMap(
                "MCRDINA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8500-GET-DATE-TIME */
    private void captureCurrentDateTime(TaskContext ctx) {
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

    /** Reset the filter output field length to force redisplay on the next map send. */
    private void resetFilterFieldLength(TaskContext ctx) {
        ctx.f.setFiltl((short) -1);
    }

    /** Recompute the filter description text and push it into the screen output field. */
    private void refreshFilterDescription(TaskContext ctx) {
        setFilterDescription(ctx);
        ctx.f.setFdesco(ctx.f.getWsFdesc());
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OccrdinFields f;

        final AppService appService;

        final OccrdinLinkParm link = new OccrdinLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OccrdinFields(ws);
        }
    }
}

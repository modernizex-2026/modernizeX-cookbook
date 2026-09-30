package com.generated.orion.ocactin.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcactinLinkParm;
import com.generated.orion.ocactin.accessor.OcactinFields;
import com.generated.orion.ocactin.metadata.OcactinBmsMetadata;
import com.generated.orion.ocactin.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCACTIN. */
@Service
public class OcactinService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcactinService.class);

    /** Serialized length of ORION-COMMAREA passed across program transfers. */
    private static final int ORION_COMMAREA_LENGTH = 692;

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
        return "OCACTIN";
    }

    @Override
    public String getTransId() {
        return "ORAI";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcactinBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcactinBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcactinBmsMetadata.getFieldMapping(mapName);
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
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitialScreen(ctx);
            } else {
                dispatchAidKey(ctx);
            }
        }
        returnToTransaction(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MACTINAO");
        ctx.f.setKactinArea("");
        populateScreenHeader(ctx);
        clearScreenRows(ctx);
        ctx.f.setWsStFilter("A");
        ctx.f.setWsStNextKey(0);
        ctx.f.setWsStPageNo(0);
        ctx.f.setWsStPopCnt(0);
        ctx.f.setWsStPopBal(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsStPopAvl(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsStMore("N");
        ctx.f.setWsStListed("N");
        ctx.f.setCaWorkArea(
                Utility.setSubstring(
                        String.valueOf(ctx.f.getCaWorkArea()),
                        1,
                        60,
                        String.valueOf(ctx.f.getWsState())));
        ctx.f.setFdesco("ALL");
        displayPageTotals(ctx);
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setFiltl((short) -1);
        ctx.appService.sendMap(
                "MACTINA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void dispatchAidKey(TaskContext ctx) {
        ctx.f.setWsState(
                Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 60).substring(0, 60));
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "@")) {
            transferToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "_")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            applyAccountFilter(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "7")) {
            restartAccountList(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "8")) {
            pageForwardAccountList(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            refreshFilterDescription(ctx);
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2050-RECEIVE */
    private void receiveScreenInput(TaskContext ctx) {
        ctx.appService.receiveMap("MACTINA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MACTINAI");
        }
    }

    /** COBOL paragraph: 2100-APPLY-FILTER */
    private void applyAccountFilter(TaskContext ctx) {
        receiveScreenInput(ctx);
        parseFilterInput(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            ctx.f.setErrmsgo(ctx.f.getWsMBadFilter());
            ctx.f.setFiltl((short) -1);
            sendDataOnlyScreen(ctx);
        } else {
            ctx.f.setKaiFilter(ctx.f.getWsStFilter());
            ctx.f.setKaiStartKey(0);
            ctx.f.setString("KAI-WANT-KPI", "Y");
            ctx.f.setWsStPageNo(1);
            ctx.f.setWsStListed("Y");
            runAccountPage(ctx);
        }
    }

    /** COBOL paragraph: 2200-RESTART */
    private void restartAccountList(TaskContext ctx) {
        if (!Utility.fieldEquals(ctx.f.getWsStListed(), "Y")) {
            ctx.f.setErrmsgo(ctx.f.getWsMListFirst());
            ctx.f.setFiltl((short) -1);
            sendDataOnlyScreen(ctx);
        } else {
            ctx.f.setKaiFilter(ctx.f.getWsStFilter());
            ctx.f.setKaiStartKey(0);
            ctx.f.setString("KAI-WANT-KPI", "Y");
            ctx.f.setWsStPageNo(1);
            runAccountPage(ctx);
        }
    }

    /** COBOL paragraph: 2300-PAGE-FWD */
    private void pageForwardAccountList(TaskContext ctx) {
        if (!Utility.fieldEquals(ctx.f.getWsStListed(), "Y")) {
            ctx.f.setErrmsgo(ctx.f.getWsMListFirst());
            ctx.f.setFiltl((short) -1);
            sendDataOnlyScreen(ctx);
        } else {
            if (!Utility.fieldEquals(ctx.f.getWsStMore(), "Y")) {
                refreshFilterDescription(ctx);
                ctx.f.setErrmsgo(ctx.f.getWsMEndFile());
                sendDataOnlyScreen(ctx);
            } else {
                ctx.f.setKaiFilter(ctx.f.getWsStFilter());
                ctx.f.setKaiStartKey(ctx.f.getWsStNextKey());
                ctx.f.setString("KAI-WANT-KPI", "N");
                ctx.f.setWsStPageNo(ctx.f.getWsStPageNo() + 1);
                runAccountPage(ctx);
            }
        }
    }

    /** COBOL paragraph: 2400-RUN-PAGE */
    private void runAccountPage(TaskContext ctx) {
        callBrowseSubprogram(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            ctx.f.setFiltl((short) -1);
            sendDataOnlyScreen(ctx);
        } else {
            if (ctx.f.getKaiWantKpi().equals("Y")) {
                ctx.f.setWsStPopCnt(ctx.f.getKaiPopCount());
                ctx.f.setWsStPopBal(ctx.f.getKaiPopBalTot());
                ctx.f.setWsStPopAvl(ctx.f.getKaiPopAvlTot());
            }
            ctx.f.setWsStNextKey(ctx.f.getKaiNextKey());
            ctx.f.setWsStMore(ctx.f.getKaiMoreSw());
            displayAccountPage(ctx);
            ctx.f.setFiltl((short) -1);
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 3000-CALL-SUB */
    private void callBrowseSubprogram(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        {
            byte[] _linkCa = ctx.f.sliceBytes("KACTIN-AREA");
            ctx.appService.link(ctx.f.getWsSubPgm(), _linkCa, 767);
            ctx.f.writeBytes("KACTIN-AREA", _linkCa);
        }
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setString("WS-VALID-SW", "N");
            refreshFilterDescription(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMLinkErr());
        } else {
            if (ctx.f.getKaiReturnCd().equals("E")) {
                ctx.f.setString("WS-VALID-SW", "N");
                refreshFilterDescription(ctx);
                ctx.f.setErrmsgo(ctx.f.getWsMBrowseErr());
            }
        }
    }

    /** COBOL paragraph: 4000-SHOW-PAGE */
    private void displayAccountPage(TaskContext ctx) {
        refreshFilterDescription(ctx);
        clearScreenRows(ctx);
        if (ctx.f.getKaiRowCount() == 0) {
            ctx.f.setErrmsgo(ctx.f.getWsMNoneFound());
        } else {
            for (ctx.f.setWsI(1);
                    ctx.f.getWsI() <= ctx.f.getKaiRowCount();
                    ctx.f.setWsI(ctx.f.getWsI() + 1)) {
                formatAccountRow(ctx);
                placeRowOnScreen(ctx);
            }
            buildRowCountMessage(ctx);
        }
        displayPageTotals(ctx);
    }

    /** COBOL paragraph: 4100-CLEAR-ROWS */
    private void clearScreenRows(TaskContext ctx) {
        ctx.f.setAid1o(" ");
        ctx.f.setAst1o(" ");
        ctx.f.setAbl1o(" ");
        ctx.f.setAlm1o(" ");
        ctx.f.setAav1o(" ");
        ctx.f.setAut1o(" ");
        ctx.f.setAid2o(" ");
        ctx.f.setAst2o(" ");
        ctx.f.setAbl2o(" ");
        ctx.f.setAlm2o(" ");
        ctx.f.setAav2o(" ");
        ctx.f.setAut2o(" ");
        ctx.f.setAid3o(" ");
        ctx.f.setAst3o(" ");
        ctx.f.setAbl3o(" ");
        ctx.f.setAlm3o(" ");
        ctx.f.setAav3o(" ");
        ctx.f.setAut3o(" ");
        ctx.f.setAid4o(" ");
        ctx.f.setAst4o(" ");
        ctx.f.setAbl4o(" ");
        ctx.f.setAlm4o(" ");
        ctx.f.setAav4o(" ");
        ctx.f.setAut4o(" ");
        ctx.f.setAid5o(" ");
        ctx.f.setAst5o(" ");
        ctx.f.setAbl5o(" ");
        ctx.f.setAlm5o(" ");
        ctx.f.setAav5o(" ");
        ctx.f.setAut5o(" ");
        ctx.f.setAid6o(" ");
        ctx.f.setAst6o(" ");
        ctx.f.setAbl6o(" ");
        ctx.f.setAlm6o(" ");
        ctx.f.setAav6o(" ");
        ctx.f.setAut6o(" ");
        ctx.f.setAid7o(" ");
        ctx.f.setAst7o(" ");
        ctx.f.setAbl7o(" ");
        ctx.f.setAlm7o(" ");
        ctx.f.setAav7o(" ");
        ctx.f.setAut7o(" ");
        ctx.f.setAid8o(" ");
        ctx.f.setAst8o(" ");
        ctx.f.setAbl8o(" ");
        ctx.f.setAlm8o(" ");
        ctx.f.setAav8o(" ");
        ctx.f.setAut8o(" ");
        ctx.f.setAid9o(" ");
        ctx.f.setAst9o(" ");
        ctx.f.setAbl9o(" ");
        ctx.f.setAlm9o(" ");
        ctx.f.setAav9o(" ");
        ctx.f.setAut9o(" ");
        ctx.f.setAid10o(" ");
        ctx.f.setAst10o(" ");
        ctx.f.setAbl10o(" ");
        ctx.f.setAlm10o(" ");
        ctx.f.setAav10o(" ");
        ctx.f.setAut10o(" ");
        ctx.f.setAid11o(" ");
        ctx.f.setAst11o(" ");
        ctx.f.setAbl11o(" ");
        ctx.f.setAlm11o(" ");
        ctx.f.setAav11o(" ");
        ctx.f.setAut11o(" ");
        ctx.f.setAid12o(" ");
        ctx.f.setAst12o(" ");
        ctx.f.setAbl12o(" ");
        ctx.f.setAlm12o(" ");
        ctx.f.setAav12o(" ");
        ctx.f.setAut12o(" ");
        ctx.f.setAid13o(" ");
        ctx.f.setAst13o(" ");
        ctx.f.setAbl13o(" ");
        ctx.f.setAlm13o(" ");
        ctx.f.setAav13o(" ");
        ctx.f.setAut13o(" ");
    }

    /** COBOL paragraph: 4200-EDIT-ROW */
    private void formatAccountRow(TaskContext ctx) {
        ctx.f.setWsOId(String.format("%011d", ctx.f.getKaiRId(ctx.f.getWsI())));
        ctx.f.setWsOSt(ctx.f.getKaiRStatus(ctx.f.getWsI()));
        ctx.f.setWsEdBal(ctx.f.getKaiRBal(ctx.f.getWsI()));
        ctx.f.setWsOBal(
                Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-BAL")), 16)
                        .substring(1, 16));
        ctx.f.setWsEdBal(ctx.f.getKaiRLimit(ctx.f.getWsI()));
        ctx.f.setWsOLim(
                Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-BAL")), 16)
                        .substring(1, 16));
        ctx.f.setWsEdBal(ctx.f.getKaiRAvail(ctx.f.getWsI()));
        ctx.f.setWsOAvl(
                Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-BAL")), 16)
                        .substring(1, 16));
        ctx.f.setWsEdUtil(ctx.f.getKaiRUtil(ctx.f.getWsI()));
        ctx.f.setWsOUtl(ctx.f.getString("WS-ED-UTIL"));
    }

    /** COBOL paragraph: 4300-PLACE-ROW */
    private void placeRowOnScreen(TaskContext ctx) {
        switch (ctx.f.getWsI()) {
            case 1 -> {
                ctx.f.setAid1o(ctx.f.getWsOId());
                ctx.f.setAst1o(ctx.f.getWsOSt());
                ctx.f.setAbl1o(ctx.f.getWsOBal());
                ctx.f.setAlm1o(ctx.f.getWsOLim());
                ctx.f.setAav1o(ctx.f.getWsOAvl());
                ctx.f.setAut1o(ctx.f.getWsOUtl());
            }
            case 2 -> {
                ctx.f.setAid2o(ctx.f.getWsOId());
                ctx.f.setAst2o(ctx.f.getWsOSt());
                ctx.f.setAbl2o(ctx.f.getWsOBal());
                ctx.f.setAlm2o(ctx.f.getWsOLim());
                ctx.f.setAav2o(ctx.f.getWsOAvl());
                ctx.f.setAut2o(ctx.f.getWsOUtl());
            }
            case 3 -> {
                ctx.f.setAid3o(ctx.f.getWsOId());
                ctx.f.setAst3o(ctx.f.getWsOSt());
                ctx.f.setAbl3o(ctx.f.getWsOBal());
                ctx.f.setAlm3o(ctx.f.getWsOLim());
                ctx.f.setAav3o(ctx.f.getWsOAvl());
                ctx.f.setAut3o(ctx.f.getWsOUtl());
            }
            case 4 -> {
                ctx.f.setAid4o(ctx.f.getWsOId());
                ctx.f.setAst4o(ctx.f.getWsOSt());
                ctx.f.setAbl4o(ctx.f.getWsOBal());
                ctx.f.setAlm4o(ctx.f.getWsOLim());
                ctx.f.setAav4o(ctx.f.getWsOAvl());
                ctx.f.setAut4o(ctx.f.getWsOUtl());
            }
            case 5 -> {
                ctx.f.setAid5o(ctx.f.getWsOId());
                ctx.f.setAst5o(ctx.f.getWsOSt());
                ctx.f.setAbl5o(ctx.f.getWsOBal());
                ctx.f.setAlm5o(ctx.f.getWsOLim());
                ctx.f.setAav5o(ctx.f.getWsOAvl());
                ctx.f.setAut5o(ctx.f.getWsOUtl());
            }
            case 6 -> {
                ctx.f.setAid6o(ctx.f.getWsOId());
                ctx.f.setAst6o(ctx.f.getWsOSt());
                ctx.f.setAbl6o(ctx.f.getWsOBal());
                ctx.f.setAlm6o(ctx.f.getWsOLim());
                ctx.f.setAav6o(ctx.f.getWsOAvl());
                ctx.f.setAut6o(ctx.f.getWsOUtl());
            }
            case 7 -> {
                ctx.f.setAid7o(ctx.f.getWsOId());
                ctx.f.setAst7o(ctx.f.getWsOSt());
                ctx.f.setAbl7o(ctx.f.getWsOBal());
                ctx.f.setAlm7o(ctx.f.getWsOLim());
                ctx.f.setAav7o(ctx.f.getWsOAvl());
                ctx.f.setAut7o(ctx.f.getWsOUtl());
            }
            case 8 -> {
                ctx.f.setAid8o(ctx.f.getWsOId());
                ctx.f.setAst8o(ctx.f.getWsOSt());
                ctx.f.setAbl8o(ctx.f.getWsOBal());
                ctx.f.setAlm8o(ctx.f.getWsOLim());
                ctx.f.setAav8o(ctx.f.getWsOAvl());
                ctx.f.setAut8o(ctx.f.getWsOUtl());
            }
            case 9 -> {
                ctx.f.setAid9o(ctx.f.getWsOId());
                ctx.f.setAst9o(ctx.f.getWsOSt());
                ctx.f.setAbl9o(ctx.f.getWsOBal());
                ctx.f.setAlm9o(ctx.f.getWsOLim());
                ctx.f.setAav9o(ctx.f.getWsOAvl());
                ctx.f.setAut9o(ctx.f.getWsOUtl());
            }
            case 10 -> {
                ctx.f.setAid10o(ctx.f.getWsOId());
                ctx.f.setAst10o(ctx.f.getWsOSt());
                ctx.f.setAbl10o(ctx.f.getWsOBal());
                ctx.f.setAlm10o(ctx.f.getWsOLim());
                ctx.f.setAav10o(ctx.f.getWsOAvl());
                ctx.f.setAut10o(ctx.f.getWsOUtl());
            }
            case 11 -> {
                ctx.f.setAid11o(ctx.f.getWsOId());
                ctx.f.setAst11o(ctx.f.getWsOSt());
                ctx.f.setAbl11o(ctx.f.getWsOBal());
                ctx.f.setAlm11o(ctx.f.getWsOLim());
                ctx.f.setAav11o(ctx.f.getWsOAvl());
                ctx.f.setAut11o(ctx.f.getWsOUtl());
            }
            case 12 -> {
                ctx.f.setAid12o(ctx.f.getWsOId());
                ctx.f.setAst12o(ctx.f.getWsOSt());
                ctx.f.setAbl12o(ctx.f.getWsOBal());
                ctx.f.setAlm12o(ctx.f.getWsOLim());
                ctx.f.setAav12o(ctx.f.getWsOAvl());
                ctx.f.setAut12o(ctx.f.getWsOUtl());
            }
            case 13 -> {
                ctx.f.setAid13o(ctx.f.getWsOId());
                ctx.f.setAst13o(ctx.f.getWsOSt());
                ctx.f.setAbl13o(ctx.f.getWsOBal());
                ctx.f.setAlm13o(ctx.f.getWsOLim());
                ctx.f.setAav13o(ctx.f.getWsOAvl());
                ctx.f.setAut13o(ctx.f.getWsOUtl());
            }
            default -> {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: 4400-SHOW-TOTALS */
    private void displayPageTotals(TaskContext ctx) {
        ctx.f.setWsEdPg(ctx.f.getWsStPageNo());
        ctx.f.setPagenoo(ctx.f.getString("WS-ED-PG"));
        ctx.f.setWsEdRun(ctx.f.getWsStPopCnt());
        ctx.f.setRuntoto(ctx.f.getString("WS-ED-RUN"));
        ctx.f.setWsEdTot(ctx.f.getWsStPopBal());
        ctx.f.setPgbalo(ctx.f.getString("WS-ED-TOT"));
        ctx.f.setWsEdTot(ctx.f.getWsStPopAvl());
        ctx.f.setPgavlo(ctx.f.getString("WS-ED-TOT"));
    }

    /** COBOL paragraph: 4900-BUILD-MSG */
    private void buildRowCountMessage(TaskContext ctx) {
        ctx.f.setWsCntEd(ctx.f.getKaiRowCount());
        ctx.f.setErrmsgo(" ");
        if (Utility.fieldEquals(ctx.f.getWsStMore(), "Y")) {
            {
                StringBuilder sb = new StringBuilder();
                sb.append(String.valueOf(ctx.f.getString("WS-CNT-ED")));
                sb.append(String.valueOf(ctx.f.getWsMMore()));
                ctx.f.setErrmsgo(sb.toString());
            }
        } else {
            {
                StringBuilder sb = new StringBuilder();
                sb.append(String.valueOf(ctx.f.getString("WS-CNT-ED")));
                sb.append(String.valueOf(ctx.f.getWsMDone()));
                ctx.f.setErrmsgo(sb.toString());
            }
        }
    }

    /** Maps a 3-character filter code prefix to its internal filter-status letter. */
    private static final java.util.Map<String, String> FILTER_CODE_TO_STATUS =
            java.util.Map.of(
                    "ALL", "A",
                    "DEL", "D",
                    "OVE", "O",
                    "DOR", "M",
                    "CLO", "C",
                    "NEW", "N",
                    "HIG", "H");

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
        String filterCode =
                Utility.padRight(String.valueOf(ctx.f.getWsFiltIn()), 3).substring(0, 3);
        if (Utility.fieldEquals(ctx.f.getWsFiltIn(), " ")) {
            ctx.f.setWsStFilter("A");
        } else if (FILTER_CODE_TO_STATUS.containsKey(filterCode)) {
            ctx.f.setWsStFilter(FILTER_CODE_TO_STATUS.get(filterCode));
        } else {
            ctx.f.setString("WS-VALID-SW", "N");
        }
    }

    /** COBOL paragraph: 6100-SET-FDESC */
    private void resolveFilterDescription(TaskContext ctx) {
        switch (Utility.rtrim(ctx.f.getWsStFilter())) {
            case "A" -> {
                ctx.f.setWsFdesc("ALL");
            }
            case "D" -> {
                ctx.f.setWsFdesc("DELINQUENT");
            }
            case "O" -> {
                ctx.f.setWsFdesc("OVER-LIMIT");
            }
            case "M" -> {
                ctx.f.setWsFdesc("DORMANT");
            }
            case "C" -> {
                ctx.f.setWsFdesc("CLOSED");
            }
            case "N" -> {
                ctx.f.setWsFdesc("NEW");
            }
            case "H" -> {
                ctx.f.setWsFdesc("HIGH-UTIL");
            }
            default -> {
                ctx.f.setWsFdesc("ALL");
            }
        }
    }

    /** COBOL paragraph: 7000-XCTL-MENU */
    private void transferToMenuProgram(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(
                ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), ORION_COMMAREA_LENGTH);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8000-POPULATE-HEADER */
    private void populateScreenHeader(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        fetchCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnlyScreen(TaskContext ctx) {
        ctx.f.setCaWorkArea(
                Utility.setSubstring(
                        String.valueOf(ctx.f.getCaWorkArea()),
                        1,
                        60,
                        String.valueOf(ctx.f.getWsState())));
        populateScreenHeader(ctx);
        ctx.appService.sendMap(
                "MACTINA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
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
    private void returnToTransaction(TaskContext ctx) {
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), ORION_COMMAREA_LENGTH);
    }

    /** Recompute the filter description text and push it onto the header field. */
    private void refreshFilterDescription(TaskContext ctx) {
        resolveFilterDescription(ctx);
        ctx.f.setFdesco(ctx.f.getWsFdesc());
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcactinFields f;

        final AppService appService;

        final OcactinLinkParm link = new OcactinLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcactinFields(ws);
        }
    }
}

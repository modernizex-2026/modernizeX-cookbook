package com.generated.orion.odtranl.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OdtranlLinkParm;
import com.generated.orion.odtranl.accessor.OdtranlFields;
import com.generated.orion.odtranl.dao.OdtranlDao;
import com.generated.orion.odtranl.metadata.OdtranlBmsMetadata;
import com.generated.orion.odtranl.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program ODTRANL. */
@Service
public class OdtranlService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OdtranlService.class);

    private static final int COMMAREA_LENGTH = 692;

    @Autowired private OdtranlDao dao;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        ctx.dao = this.dao;
        executeMainLogic(ctx);
    }

    @Override
    public String getProgramName() {
        return "ODTRANL";
    }

    @Override
    public String getTransId() {
        return "OD05";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OdtranlBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OdtranlBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OdtranlBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void executeMainLogic(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setCaErrMsg(" ");
        if (ctx.appService.getEibcalen() == 0) {
            sendInitialScreen(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitialScreen(ctx);
            } else {
                dispatchInputByAidKey(ctx);
            }
        }
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MTRANLAO");
        populateScreenHeader(ctx);
        ctx.f.setErrmsgo("Enter card number and press ENTER.");
        ctx.appService.sendMap(
                "MTRANLA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void dispatchInputByAidKey(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            listTransactionsForCard(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-LIST-AND-SHOW */
    private void listTransactionsForCard(TaskContext ctx) {
        ctx.appService.receiveMap("MTRANLA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (Utility.fieldEquals(ctx.f.getCardnumi(), " ") || ctx.f.isAllLowValues("CARDNUMI")) {
            ctx.f.setErrmsgo(ctx.f.getWsMsgRequired());
            sendDataOnlyScreen(ctx);
        } else {
            ctx.f.setTrCardNum(ctx.f.getCardnumi());
            ctx.f.setCaCardNum(ctx.f.getCardnumi());
            ctx.f.fillLowValues("MTRANLAO");
            ctx.f.setCardnumo(ctx.f.getTrCardNum());
            fetchTransactionList(ctx);
            if (ctx.f.getWsRowCnt() > 0) {
                ctx.f.setErrmsgo("Transactions listed.");
            } else {
                if (Utility.fieldEquals(ctx.f.getErrmsgo(), " ")
                        || ctx.f.isAllLowValues("ERRMSGO")) {
                    ctx.f.setErrmsgo("No transactions for this card.");
                }
            }
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 3000-LIST-TRANS */
    private void fetchTransactionList(TaskContext ctx) {
        ctx.f.setWsRowCnt(0);
        ctx.f.setWsEndFlg("N");
        // DB2 OPEN CURSOR TRANCSR
        ctx.dao.openTrancsr(ctx.f.getTrCardNum());
        ctx.f.setSqlcode(0);
        if (ctx.f.getSqlcode() != 0) {
            ctx.f.setErrmsgo("Error opening transaction cursor.");
        } else {
            for (ctx.f.setWsIdx(1);
                    !(ctx.f.getWsIdx() > 5 || ctx.f.getWsEndFlg().equals("Y"));
                    ctx.f.setWsIdx(ctx.f.getWsIdx() + 1)) {
                fetchNextTransactionRow(ctx);
            }
            // DB2 CLOSE CURSOR TRANCSR
            ctx.dao.closeTrancsr();
            ctx.f.setSqlcode(0);
        }
    }

    /** COBOL paragraph: 3100-FETCH-ROW */
    private void fetchNextTransactionRow(TaskContext ctx) {
        // DB2 FETCH TRANCSR
        {
            java.util.Map<String, Object> _sqlResult = dao.fetchTrancsr();
            if (!_sqlResult.containsKey("SQLCODE")) {
                ctx.f.setTrId(
                        (_sqlResult.get("TR_ID") == null
                                ? ""
                                : String.valueOf(_sqlResult.get("TR_ID"))));
                ctx.f.setTrAmt(
                        (_sqlResult.get("TR_AMT") == null
                                ? java.math.BigDecimal.ZERO
                                : new java.math.BigDecimal(
                                        ((Number) _sqlResult.get("TR_AMT")).toString())));
                ctx.f.setTrTypeCd(
                        (_sqlResult.get("TR_TYPE_CD") == null
                                ? ""
                                : String.valueOf(_sqlResult.get("TR_TYPE_CD"))));
                ctx.f.setSqlcode(0);
            } else {
                ctx.f.setSqlcode(
                        _sqlResult.get("SQLCODE") != null
                                ? (Integer) _sqlResult.get("SQLCODE")
                                : 0);
            }
        }
        switch (ctx.f.getSqlcode()) {
            case 0 -> {
                ctx.f.setWsRowCnt(ctx.f.getWsRowCnt() + 1);
                populateTransactionRow(ctx);
            }
            case 100 -> {
                ctx.f.setString("WS-END-FLG", "Y");
            }
            default -> {
                ctx.f.setString("WS-END-FLG", "Y");
                ctx.f.setErrmsgo("Error fetching transactions.");
            }
        }
    }

    /** COBOL paragraph: 3200-MOVE-ROW */
    private void populateTransactionRow(TaskContext ctx) {
        ctx.f.setWsEdAmt(ctx.f.getTrAmt());
        switch (ctx.f.getWsIdx()) {
            case 1 -> {
                ctx.f.setTrn1o(ctx.f.getTrId());
                ctx.f.setAmt1o(ctx.f.getString("WS-ED-AMT"));
                ctx.f.setTyp1o(ctx.f.getTrTypeCd());
            }
            case 2 -> {
                ctx.f.setTrn2o(ctx.f.getTrId());
                ctx.f.setAmt2o(ctx.f.getString("WS-ED-AMT"));
                ctx.f.setTyp2o(ctx.f.getTrTypeCd());
            }
            case 3 -> {
                ctx.f.setTrn3o(ctx.f.getTrId());
                ctx.f.setAmt3o(ctx.f.getString("WS-ED-AMT"));
                ctx.f.setTyp3o(ctx.f.getTrTypeCd());
            }
            case 4 -> {
                ctx.f.setTrn4o(ctx.f.getTrId());
                ctx.f.setAmt4o(ctx.f.getString("WS-ED-AMT"));
                ctx.f.setTyp4o(ctx.f.getTrTypeCd());
            }
            case 5 -> {
                ctx.f.setTrn5o(ctx.f.getTrId());
                ctx.f.setAmt5o(ctx.f.getString("WS-ED-AMT"));
                ctx.f.setTyp5o(ctx.f.getTrTypeCd());
            }
            default -> {
                /* CONTINUE */
            }
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
        loadCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnlyScreen(TaskContext ctx) {
        populateScreenHeader(ctx);
        ctx.appService.sendMap(
                "MTRANLA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
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

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OdtranlFields f;

        final AppService appService;

        final OdtranlLinkParm link = new OdtranlLinkParm();

        OdtranlDao dao;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OdtranlFields(ws);
        }
    }
}

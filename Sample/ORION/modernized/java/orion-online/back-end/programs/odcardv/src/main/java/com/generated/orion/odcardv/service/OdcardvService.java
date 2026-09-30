package com.generated.orion.odcardv.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OdcardvLinkParm;
import com.generated.orion.odcardv.accessor.OdcardvFields;
import com.generated.orion.odcardv.dao.OdcardvDao;
import com.generated.orion.odcardv.metadata.OdcardvBmsMetadata;
import com.generated.orion.odcardv.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program ODCARDV. */
@Service
public class OdcardvService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OdcardvService.class);

    private static final int COMMAREA_LENGTH = 692;

    @Autowired private OdcardvDao dao;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        ctx.dao = this.dao;
        runMainLine(ctx);
    }

    @Override
    public String getProgramName() {
        return "ODCARDV";
    }

    @Override
    public String getTransId() {
        return "OD03";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OdcardvBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OdcardvBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OdcardvBmsMetadata.getFieldMapping(mapName);
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
                processUserInput(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MCARDVAO");
        populateScreenHeader(ctx);
        ctx.f.setErrmsgo("Enter card number and press ENTER.");
        ctx.appService.sendMap(
                "MCARDVA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processUserInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            readAndDisplayCard(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-READ-AND-SHOW */
    private void readAndDisplayCard(TaskContext ctx) {
        ctx.appService.receiveMap("MCARDVA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (Utility.fieldEquals(ctx.f.getCardnumi(), " ") || ctx.f.isAllLowValues("CARDNUMI")) {
            ctx.f.setErrmsgo(ctx.f.getWsMsgRequired());
            sendDataOnlyScreen(ctx);
        } else {
            ctx.f.setCdNum(ctx.f.getCardnumi());
            ctx.f.setCaCardNum(ctx.f.getCdNum());
            readCardFromDatabase(ctx);
            if (ctx.f.getWsFoundFlg().equals("Y")) {
                populateCardDetailFields(ctx);
                ctx.f.setErrmsgo("Card displayed.");
                sendDataOnlyScreen(ctx);
            } else {
                ctx.f.setErrmsgo(ctx.f.getWsMsgNotfnd());
                sendDataOnlyScreen(ctx);
            }
        }
    }

    /** COBOL paragraph: 3000-READ-CARD */
    private void readCardFromDatabase(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        // DB2 SELECT INTO — ORION.CARD
        {
            java.util.Map<String, Object> _sqlResult = dao.selectOrionCard(ctx.f.getCdNum());
            if (!_sqlResult.containsKey("SQLCODE")) {
                ctx.f.setCdAcctId(
                        (_sqlResult.get("CD_ACCT_ID") == null
                                ? 0L
                                : ((Number) _sqlResult.get("CD_ACCT_ID")).longValue()));
                ctx.f.setCdEmbossedName(
                        (_sqlResult.get("CD_EMBOSSED_NAME") == null
                                ? ""
                                : String.valueOf(_sqlResult.get("CD_EMBOSSED_NAME"))));
                ctx.f.setCdExpiryDate(
                        (_sqlResult.get("CD_EXPIRY_DATE") == null
                                ? ""
                                : String.valueOf(_sqlResult.get("CD_EXPIRY_DATE"))));
                ctx.f.setCdActiveStatus(
                        (_sqlResult.get("CD_ACTIVE_STATUS") == null
                                ? ""
                                : String.valueOf(_sqlResult.get("CD_ACTIVE_STATUS"))));
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
                ctx.f.setString("WS-FOUND-FLG", "Y");
            }
            case 100 -> {
                ctx.f.setString("WS-FOUND-FLG", "N");
            }
            default -> {
                ctx.f.setString("WS-FOUND-FLG", "N");
                ctx.f.setErrmsgo("Error reading card table.");
            }
        }
    }

    /** COBOL paragraph: 4000-POPULATE-DETAIL */
    private void populateCardDetailFields(TaskContext ctx) {
        ctx.f.setCardnumo(ctx.f.getCdNum());
        ctx.f.setCdaccto(String.format("%011d", ctx.f.getCdAcctId()));
        ctx.f.setCdnameo(ctx.f.getCdEmbossedName());
        ctx.f.setCdexpo(ctx.f.getCdExpiryDate());
        ctx.f.setCdstato(ctx.f.getCdActiveStatus());
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
                "MCARDVA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
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

        final OdcardvFields f;

        final AppService appService;

        final OdcardvLinkParm link = new OdcardvLinkParm();

        OdcardvDao dao;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OdcardvFields(ws);
        }
    }
}

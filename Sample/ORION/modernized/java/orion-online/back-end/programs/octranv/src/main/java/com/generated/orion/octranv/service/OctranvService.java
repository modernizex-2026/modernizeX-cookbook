package com.generated.orion.octranv.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OctranvLinkParm;
import com.generated.orion.octranv.accessor.OctranvFields;
import com.generated.orion.octranv.metadata.OctranvBmsMetadata;
import com.generated.orion.octranv.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCTRANV. */
@Service
public class OctranvService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OctranvService.class);

    /** Length in bytes of the ORION-COMMAREA passed across program transfers. */
    private static final int COMMAREA_LENGTH = 692;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        processMainLine(ctx);
    }

    @Override
    public String getProgramName() {
        return "OCTRANV";
    }

    @Override
    public String getTransId() {
        return "ORTV";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OctranvBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OctranvBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OctranvBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void processMainLine(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setCaErrMsg(" ");
        if (ctx.appService.getEibcalen() == 0) {
            sendInitialScreen(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitialScreen(ctx);
            } else {
                dispatchAidAction(ctx);
            }
        }
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MTRANVAO");
        populateScreenHeader(ctx);
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setTranidl((short) -1);
        ctx.appService.sendMap(
                "MTRANVA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void dispatchAidAction(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            readAndDisplayTransaction(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-READ-AND-SHOW */
    private void readAndDisplayTransaction(TaskContext ctx) {
        ctx.appService.receiveMap("MTRANVA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (Utility.fieldEquals(ctx.f.getTranidi(), " ") || ctx.f.isAllLowValues("TRANIDI")) {
            ctx.f.setErrmsgo(ctx.f.getWsMIdRequired());
            ctx.f.setTranidl((short) -1);
            sendDataOnlyScreen(ctx);
        } else {
            ctx.f.setWsTranKey(ctx.f.getTranidi());
            ctx.f.setWsTranKey(ctx.f.getWsTranKey().replace(RuntimeConstants.LOW_VALUES, " "));
            ctx.f.setTrId(ctx.f.getWsTranKey());
            ctx.f.setCaTranId(ctx.f.getTrId());
            readTransactionRecord(ctx);
            if (ctx.f.getWsFoundFlg().equals("Y")) {
                populateTransactionDetail(ctx);
                ctx.f.setErrmsgo(ctx.f.getWsMTranFound());
                ctx.f.setTranidl((short) -1);
                sendDataOnlyScreen(ctx);
            } else {
                ctx.f.setErrmsgo(ctx.f.getWsMTranNotfnd());
                ctx.f.setTranidl((short) -1);
                sendDataOnlyScreen(ctx);
            }
        }
    }

    /** COBOL paragraph: 3000-READ-TRAN */
    private void readTransactionRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.appService.readFile(ctx.f.getWsTranfile(), ctx.f, ctx.f.getTrId(), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-FOUND-FLG", "N");
        } else {
            ctx.f.setString("WS-FOUND-FLG", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMReadError());
        }
    }

    /** COBOL paragraph: 4000-POPULATE-DETAIL */
    private void populateTransactionDetail(TaskContext ctx) {
        ctx.f.setTranido(ctx.f.getTrId());
        ctx.f.setTrcardo(ctx.f.getTrCardNum());
        ctx.f.setTrtypeo(ctx.f.getTrTypeCd());
        ctx.f.setWsEdAmt(ctx.f.getTrAmt());
        ctx.f.setTramto(Utility.padRight(ctx.f.getString("WS-ED-AMT"), 16).substring(1, 16));
        ctx.f.setTrmercho(ctx.f.getTrMerchantName());
        ctx.f.setTrdesco(ctx.f.getTrDesc());
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
                "MTRANVA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
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

        final OctranvFields f;

        final AppService appService;

        final OctranvLinkParm link = new OctranvLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OctranvFields(ws);
        }
    }
}

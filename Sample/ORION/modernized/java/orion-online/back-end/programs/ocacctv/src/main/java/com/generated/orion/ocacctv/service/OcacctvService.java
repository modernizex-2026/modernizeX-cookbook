package com.generated.orion.ocacctv.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcacctvLinkParm;
import com.generated.orion.ocacctv.accessor.OcacctvFields;
import com.generated.orion.ocacctv.metadata.OcacctvBmsMetadata;
import com.generated.orion.ocacctv.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCACCTV. */
@Service
public class OcacctvService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcacctvService.class);

    /** Length in bytes of the ORION-COMMAREA passed on XCTL/RETURN. */
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
        return "OCACCTV";
    }

    @Override
    public String getTransId() {
        return "ORAV";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcacctvBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcacctvBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcacctvBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setCaErrMsg(" ");
        if (ctx.appService.getEibcalen() == 0) {
            sendInitialScreen(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitialScreen(ctx);
            } else {
                processScreenInput(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MACCTVAO");
        populateScreenHeader(ctx);
        ctx.f.setErrmsgo("Enter account id and press ENTER.");
        ctx.appService.sendMap(
                "MACCTVA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processScreenInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            readAndShowAccount(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-READ-AND-SHOW */
    private void readAndShowAccount(TaskContext ctx) {
        ctx.appService.receiveMap("MACCTVA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (Utility.fieldEquals(ctx.f.getAcctidi(), " ") || ctx.f.isAllLowValues("ACCTIDI")) {
            ctx.f.setErrmsgo(ctx.f.getWsMsgRequired());
            sendDataOnlyScreen(ctx);
        } else {
            ctx.f.setAcId(Utility.parseNumeric(ctx.f.getAcctidi()).longValue());
            ctx.f.setCaAcctId(ctx.f.getAcId());
            readAccountRecord(ctx);
            if (ctx.f.getWsFoundFlg().equals("Y")) {
                populateAccountDetail(ctx);
                ctx.f.setErrmsgo(" ");
                sendDataOnlyScreen(ctx);
            } else {
                ctx.f.setErrmsgo(ctx.f.getWsMsgNotfnd());
                sendDataOnlyScreen(ctx);
            }
        }
    }

    /** COBOL paragraph: 3000-READ-ACCT */
    private void readAccountRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.appService.readFile(ctx.f.getWsAcctfile(), ctx.f, String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-FOUND-FLG", "N");
        } else {
            ctx.f.setErrmsgo("Error reading account file.");
        }
    }

    /** COBOL paragraph: 4000-POPULATE-DETAIL */
    private void populateAccountDetail(TaskContext ctx) {
        ctx.f.setAcctido(String.format("%011d", ctx.f.getAcId()));
        ctx.f.setAcstato(ctx.f.getAcActiveStatus());
        ctx.f.setWsEdBal(ctx.f.getAcCurrBal());
        ctx.f.setAcbalo(ctx.f.getString("WS-ED-BAL"));
        ctx.f.setWsEdAmt(ctx.f.getAcCreditLimit());
        ctx.f.setAccrlimo(ctx.f.getString("WS-ED-AMT"));
        ctx.f.setWsEdAmt(ctx.f.getAcCashLimit());
        ctx.f.setAccslimo(ctx.f.getString("WS-ED-AMT"));
        ctx.f.setAcopeno(ctx.f.getAcOpenDate());
        ctx.f.setAcexpo(ctx.f.getAcExpiryDate());
        ctx.f.setAcgrpo(ctx.f.getAcGroupId());
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
                "MACCTVA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
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

        final OcacctvFields f;

        final AppService appService;

        final OcacctvLinkParm link = new OcacctvLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcacctvFields(ws);
        }
    }
}

package com.generated.orion.ocauthq.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcauthqLinkParm;
import com.generated.orion.ocauthq.accessor.OcauthqFields;
import com.generated.orion.ocauthq.metadata.OcauthqBmsMetadata;
import com.generated.orion.ocauthq.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCAUTHQ. */
@Service
public class OcauthqService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcauthqService.class);

    // TODO(layer1): cross-cohort pattern — promote to batch-common Constants
    private static final int COMMAREA_LENGTH = 692;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        executeMainFlow(ctx);
    }

    @Override
    public String getProgramName() {
        return "OCAUTHQ";
    }

    @Override
    public String getTransId() {
        return "ORAQ";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcauthqBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcauthqBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcauthqBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void executeMainFlow(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setCaErrMsg(" ");
        if (ctx.appService.getEibcalen() == 0) {
            sendInitialScreen(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitialScreen(ctx);
            } else {
                processAidKeyInput(ctx);
            }
        }
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MAUTHQAO");
        populateScreenHeader(ctx);
        ctx.f.setErrmsgo("Enter card, amount, merchant; press ENTER.");
        ctx.appService.sendMap(
                "MAUTHQA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processAidKeyInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            authorizeTransaction(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-AUTHORIZE */
    private void authorizeTransaction(TaskContext ctx) {
        ctx.appService.receiveMap("MAUTHQA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setWsInCard(ctx.f.getCardnumi());
        ctx.f.setWsInAmt(ctx.f.getAmounti());
        ctx.f.setWsInMerch(ctx.f.getMerchi());
        validateAuthorizationInput(ctx);
        if (ctx.f.getWsErrFlg().equals("Y")) {
            populateEchoFields(ctx);
            ctx.f.setDecisno(" ");
            ctx.f.setReasono(" ");
            ctx.f.setAvailo(" ");
            sendDataOnlyScreen(ctx);
        } else {
            callMqRequestProgram(ctx);
            displayAuthorizationDecision(ctx);
        }
    }

    /** COBOL paragraph: 2200-VALIDATE-INPUT */
    private void validateAuthorizationInput(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        if (Utility.fieldEquals(ctx.f.getWsInCard(), " ") || ctx.f.isAllLowValues("WS-IN-CARD")) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Card number is required.");
        }
        if (ctx.f.getWsErrFlg().equals("N")) {
            if (Utility.fieldEquals(ctx.f.getWsInAmt(), " ") || ctx.f.isAllLowValues("WS-IN-AMT")) {
                ctx.f.setString("WS-ERR-FLG", "Y");
                ctx.f.setErrmsgo("Amount is required.");
            }
        }
        if (ctx.f.getWsErrFlg().equals("N")) {
            ctx.f.setWsAmtNum(Utility.parseNumeric(String.valueOf(ctx.f.getWsInAmt())));
            if ((ctx.f.getWsAmtNum().signum() <= 0)) {
                ctx.f.setString("WS-ERR-FLG", "Y");
                ctx.f.setErrmsgo("Amount must be greater than zero.");
            }
        }
    }

    /** COBOL paragraph: 2300-CALL-MQREQ */
    private void callMqRequestProgram(TaskContext ctx) {
        ctx.f.setAuthMsgArea("");
        ctx.f.setAqMsgType("AREQ");
        ctx.f.setAqCardNum(ctx.f.getWsInCard());
        ctx.f.setAqAcctId(0);
        ctx.f.setAqAmount(ctx.f.getWsAmtNum());
        ctx.f.setAqMerchantId(0);
        ctx.f.setAqMerchantName(ctx.f.getWsInMerch());
        ctx.f.setAqRequestedTs(" ");
        ctx.f.setCaCardNum(ctx.f.getWsInCard());
        ctx.f.setCaWorkArea(ctx.f.getAuthMsgArea());
        {
            byte[] _linkCa = ctx.f.sliceBytes("ORION-COMMAREA");
            ctx.appService.link(ctx.f.getWsMqreqPgm(), _linkCa, COMMAREA_LENGTH);
            ctx.f.writeBytes("ORION-COMMAREA", _linkCa);
        }
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setAuthMsgArea(String.valueOf(ctx.f.getCaWorkArea()));
        } else {
            ctx.f.setAuthMsgArea(" ");
            ctx.f.setString("AS-DECISION", "ERROR   ");
            ctx.f.setAsReason("LINK OUMQREQ FAILED");
            log.info(
                    "OCAUTHQ: LINK RESP={}", String.format("%+010d", (long) (ctx.f.getWsRespCd())));
        }
    }

    /** COBOL paragraph: 2400-SHOW-DECISION */
    private void displayAuthorizationDecision(TaskContext ctx) {
        populateEchoFields(ctx);
        ctx.f.setDecisno(ctx.f.getAsDecision());
        ctx.f.setReasono(ctx.f.getAsReason());
        ctx.f.setWsEdBal(ctx.f.getAsAvailCredit());
        ctx.f.setAvailo(
                Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-BAL")), 16)
                        .substring(1, 16));
        if (ctx.f.getCaErrFlg().equals("Y")) {
            ctx.f.setErrmsgo(ctx.f.getCaErrMsg());
        } else {
            ctx.f.setErrmsgo("Authorization complete. PF3=Back.");
        }
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 2600-POPULATE-ECHO */
    private void populateEchoFields(TaskContext ctx) {
        ctx.f.setCardnumo(ctx.f.getWsInCard());
        ctx.f.setAmounto(ctx.f.getWsInAmt());
        ctx.f.setMercho(ctx.f.getWsInMerch());
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
                "MAUTHQA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
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

        final OcauthqFields f;

        final AppService appService;

        final OcauthqLinkParm link = new OcauthqLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcauthqFields(ws);
        }
    }
}

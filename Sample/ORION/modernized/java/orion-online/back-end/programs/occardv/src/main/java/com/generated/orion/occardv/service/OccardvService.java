package com.generated.orion.occardv.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OccardvLinkParm;
import com.generated.orion.occardv.accessor.OccardvFields;
import com.generated.orion.occardv.metadata.OccardvBmsMetadata;
import com.generated.orion.occardv.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCCARDV. */
@Service
public class OccardvService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OccardvService.class);

    /** Byte length of the ORION-COMMAREA slice passed on XCTL/RETURN TRANSID. */
    private static final int ORION_COMMAREA_LENGTH = 692;

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
        return "OCCARDV";
    }

    @Override
    public String getTransId() {
        return "ORCV";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OccardvBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OccardvBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OccardvBmsMetadata.getFieldMapping(mapName);
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
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), ORION_COMMAREA_LENGTH);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MCARDVAO");
        populateScreenHeader(ctx);
        if (Utility.fieldEquals(ctx.f.getCaCardNum(), " ") || ctx.f.isAllLowValues("CA-CARD-NUM")) {
            ctx.f.setErrmsgo("Enter card number and press ENTER.");
        } else {
            ctx.f.setCardnumo(ctx.f.getCaCardNum());
            ctx.f.setErrmsgo("Press ENTER to view the selected card.");
        }
        ctx.appService.sendMap(
                "MCARDVA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processUserInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "@")) {
            transferToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "_")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            readAndDisplayCard(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2050-RECEIVE */
    private void receiveScreenInput(TaskContext ctx) {
        ctx.appService.receiveMap("MCARDVA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MCARDVAI");
        }
    }

    /** COBOL paragraph: 2100-READ-AND-SHOW */
    private void readAndDisplayCard(TaskContext ctx) {
        receiveScreenInput(ctx);
        validateCardNumber(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            sendDataOnlyScreen(ctx);
        } else {
            ctx.f.setCdNum(ctx.f.getWsCnIn());
            ctx.f.setCaCardNum(ctx.f.getCdNum());
            readCardRecord(ctx);
            if (ctx.f.getWsFoundFlg().equals("Y")) {
                populateCardDetail(ctx);
                ctx.f.setErrmsgo("Card displayed.");
                sendDataOnlyScreen(ctx);
            } else {
                ctx.f.setErrmsgo(ctx.f.getWsMsgNotfnd());
                sendDataOnlyScreen(ctx);
            }
        }
    }

    /** COBOL paragraph: 3000-READ-CARD */
    private void readCardRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.appService.readFile(ctx.f.getWsCardfile(), ctx.f, String.valueOf(ctx.f.getCdNum()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-FOUND-FLG", "N");
        } else {
            abendOnFileError(ctx);
        }
    }

    /** COBOL paragraph: 4000-POPULATE-DETAIL */
    private void populateCardDetail(TaskContext ctx) {
        ctx.f.setCardnumo(ctx.f.getCdNum());
        ctx.f.setCdaccto(String.format("%011d", ctx.f.getCdAcctId()));
        ctx.f.setCdnameo(ctx.f.getCdEmbossedName());
        ctx.f.setCdexpo(ctx.f.getCdExpiryDate());
        ctx.f.setCdstato(ctx.f.getCdActiveStatus());
    }

    /** COBOL paragraph: 6000-VALIDATE-CARDNUM */
    private void validateCardNumber(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsCnIn(ctx.f.getCardnumi());
        ctx.f.setWsCnIn(
                String.valueOf(ctx.f.getWsCnIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getWsCnIn(), " ")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMsgRequired());
            return;
        }
        ctx.f.setWsCnDigits(0);
        for (ctx.f.setWsCnPos(1);
                ctx.f.getWsCnPos() <= 16;
                ctx.f.setWsCnPos(ctx.f.getWsCnPos() + 1)) {
            ctx.f.setWsCnChar(
                    Utility.padRight(String.valueOf(ctx.f.getWsCnIn()), 256)
                            .substring(ctx.f.getWsCnPos() - 1, ctx.f.getWsCnPos() - 1 + 1));
            if ((ctx.f.getWsCnChar().compareTo("0") >= 0)
                    && (ctx.f.getWsCnChar().compareTo("9") <= 0)) {
                ctx.f.setWsCnDigits(ctx.f.getWsCnDigits() + 1);
            } else {
                ctx.f.setString("WS-VALID-SW", "N");
            }
        }
        if (ctx.f.getWsValidSw().equals("N") || ctx.f.getWsCnDigits() != 16) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo("Card number must be exactly 16 digits.");
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

    /** COBOL paragraph: 9500-ABEND-RTN */
    private void abendOnFileError(TaskContext ctx) {
        ctx.f.setWsMsgText("OCCARDV: unrecoverable file error. Contact support.");
        ctx.appService.sendText(String.valueOf(ctx.f), true, true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.appService.returnProgram();
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OccardvFields f;

        final AppService appService;

        final OccardvLinkParm link = new OccardvLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OccardvFields(ws);
        }
    }
}

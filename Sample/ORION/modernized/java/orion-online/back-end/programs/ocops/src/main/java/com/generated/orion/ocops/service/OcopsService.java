package com.generated.orion.ocops.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcopsLinkParm;
import com.generated.orion.ocops.accessor.OcopsFields;
import com.generated.orion.ocops.metadata.OcopsBmsMetadata;
import com.generated.orion.ocops.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCOPS. */
@Service
public class OcopsService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcopsService.class);

    private static final int COMMAREA_LENGTH = 692;

    private static final int TOKEN_BUFFER_LENGTH = 256;

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
        return "OCOPS";
    }

    @Override
    public String getTransId() {
        return "OROP";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcopsBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcopsBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcopsBmsMetadata.getFieldMapping(mapName);
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
                dispatchAidAction(ctx);
            }
        }
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MOPSAO");
        populateScreenHeader(ctx);
        ctx.f.setErrmsgo("Select an operation (1-8) and press ENTER.");
        ctx.appService.sendMap(
                "MOPSA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void dispatchAidAction(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            xctlToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "@")) {
            xctlToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "_")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            runSelectedOperation(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2050-RECEIVE */
    private void receiveOperationScreen(TaskContext ctx) {
        ctx.appService.receiveMap("MOPSA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MOPSAI");
        }
    }

    /** COBOL paragraph: 2100-RUN-OPERATION */
    private void runSelectedOperation(TaskContext ctx) {
        receiveOperationScreen(ctx);
        if (Utility.isNumeric(ctx.f.getRawString("OPTIONI"))) {
            ctx.f.setWsOption(Utility.parseNumeric(ctx.f.getOptioni()).intValue());
        } else {
            ctx.f.setWsOption(0);
        }
        parseInputParm(ctx);
        buildSubProgramRequest(ctx);
        if (Utility.fieldEquals(ctx.f.getWsSubPgm(), " ")) {
            ctx.f.setErrmsgo("Invalid option. Enter 1 through 8.");
            sendDataOnlyScreen(ctx);
        } else {
            linkToSubProgram(ctx);
        }
    }

    /** COBOL paragraph: 2200-LINK-SUB */
    private void linkToSubProgram(TaskContext ctx) {
        ctx.f.copyBytes("CA-WORK-AREA", "KOPS-AREA");
        {
            byte[] _linkCa = ctx.f.sliceBytes("ORION-COMMAREA");
            ctx.appService.link(ctx.f.getWsSubPgm(), _linkCa, COMMAREA_LENGTH);
            ctx.f.writeBytes("ORION-COMMAREA", _linkCa);
        }
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.copyBytes("KOPS-AREA", "CA-WORK-AREA");
            populateResultScreen(ctx);
        } else {
            ctx.f.setErrmsgo("Operation could not be started. Contact support.");
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 6000-PARSE-PARM */
    private void parseInputParm(TaskContext ctx) {
        ctx.f.setWsParmIn(ctx.f.getParmi());
        ctx.f.setWsParmIn(
                String.valueOf(ctx.f.getWsParmIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        ctx.f.setWsTok1(" ");
        ctx.f.setWsTok2(" ");
        String[] unstringParts = String.valueOf(ctx.f.getWsParmIn()).split(String.valueOf(" "), -1);
        parseAccountToken(ctx);
        parseAmountToken(ctx);
    }

    /** COBOL paragraph: 6100-PARSE-ACCT */
    private void parseAccountToken(TaskContext ctx) {
        ctx.f.setWsNwInt(0);
        ctx.f.setWsNwIntCnt(0);
        for (ctx.f.setWsNwPos(1);
                ctx.f.getWsNwPos() <= 11;
                ctx.f.setWsNwPos(ctx.f.getWsNwPos() + 1)) {
            ctx.f.setWsNwChar(
                    Utility.padRight(String.valueOf(ctx.f.getWsTok1()), TOKEN_BUFFER_LENGTH)
                            .substring(ctx.f.getWsNwPos() - 1, ctx.f.getWsNwPos() - 1 + 1));
            if ((ctx.f.getWsNwChar().compareTo("0") >= 0)
                    && (ctx.f.getWsNwChar().compareTo("9") <= 0)) {
                ctx.f.setWsNwDigit(Utility.parseNumeric(ctx.f.getWsNwChar()).intValue());
                ctx.f.setWsNwInt(((ctx.f.getWsNwInt() * (long) 10) + ctx.f.getWsNwDigit()));
                ctx.f.setWsNwIntCnt(ctx.f.getWsNwIntCnt() + 1);
            }
        }
        ctx.f.setWsPAcct(ctx.f.getWsNwInt());
    }

    /** COBOL paragraph: 6200-PARSE-AMT */
    private void parseAmountToken(TaskContext ctx) {
        ctx.f.setWsNwInt(0);
        ctx.f.setWsNwFrac(0);
        ctx.f.setWsNwFracCnt(0);
        ctx.f.setWsNwDotSw("N");
        ctx.f.setWsPAmt(java.math.BigDecimal.valueOf(0));
        for (ctx.f.setWsNwPos(1);
                ctx.f.getWsNwPos() <= 16;
                ctx.f.setWsNwPos(ctx.f.getWsNwPos() + 1)) {
            ctx.f.setWsNwChar(
                    Utility.padRight(String.valueOf(ctx.f.getWsTok2()), TOKEN_BUFFER_LENGTH)
                            .substring(ctx.f.getWsNwPos() - 1, ctx.f.getWsNwPos() - 1 + 1));
            if (Utility.fieldEquals(ctx.f.getWsNwChar(), ".")) {
                ctx.f.setWsNwDotSw("Y");
            } else if ((ctx.f.getWsNwChar().compareTo("0") >= 0)
                    && (ctx.f.getWsNwChar().compareTo("9") <= 0)) {
                ctx.f.setWsNwDigit(Utility.parseNumeric(ctx.f.getWsNwChar()).intValue());
                if (Utility.fieldEquals(ctx.f.getWsNwDotSw(), "Y")) {
                    if (ctx.f.getWsNwFracCnt() < 2) {
                        ctx.f.setWsNwFrac(((ctx.f.getWsNwFrac() * 10) + ctx.f.getWsNwDigit()));
                        ctx.f.setWsNwFracCnt(ctx.f.getWsNwFracCnt() + 1);
                    }
                } else {
                    ctx.f.setWsNwInt(((ctx.f.getWsNwInt() * (long) 10) + ctx.f.getWsNwDigit()));
                }
            } else {
                /* CONTINUE */
            }
        }
        if (ctx.f.getWsNwFracCnt() == 1) {
            ctx.f.setWsNwFrac((ctx.f.getWsNwFrac() * 10));
        }
        ctx.f.setWsPAmt(
                java.math.BigDecimal.valueOf(ctx.f.getWsNwInt())
                        .add(
                                java.math.BigDecimal.valueOf(ctx.f.getWsNwFrac())
                                        .divide(
                                                java.math.BigDecimal.valueOf(100),
                                                12,
                                                java.math.RoundingMode.HALF_UP)));
    }

    /** COBOL paragraph: 7000-BUILD-REQUEST */
    private void buildSubProgramRequest(TaskContext ctx) {
        ctx.f.setWsSubPgm(" ");
        ctx.f.setKopsArea("");
        ctx.f.setKoParmAcct(ctx.f.getWsPAcct());
        ctx.f.setKoParmAmt(ctx.f.getWsPAmt());
        ctx.f.setKoParmCard(" ");
        ctx.f.setKoParmDate(" ");
        switch (ctx.f.getWsOption()) {
            case 1 -> {
                ctx.f.setWsSubPgm("OUPOST");
                ctx.f.setKoFunction("POST");
            }
            case 2 -> {
                ctx.f.setWsSubPgm("OUPAY");
                ctx.f.setKoFunction("PAY ");
            }
            case 3 -> {
                ctx.f.setWsSubPgm("OUINT");
                ctx.f.setKoFunction("INT ");
            }
            case 4 -> {
                ctx.f.setWsSubPgm("OUFEE");
                ctx.f.setKoFunction("FEE ");
            }
            case 5 -> {
                ctx.f.setWsSubPgm("OUCHGF");
                ctx.f.setKoFunction("CHGF");
            }
            case 6 -> {
                ctx.f.setWsSubPgm("OUCLOS");
                ctx.f.setKoFunction("CLOS");
            }
            case 7 -> {
                ctx.f.setWsSubPgm("OURNEW");
                ctx.f.setKoFunction("RNEW");
                ctx.f.setKoParmCard(ctx.f.getWsParmIn());
            }
            case 8 -> {
                ctx.f.setWsSubPgm("OUCYCL");
                ctx.f.setKoFunction("CYCL");
            }
            default -> {
                ctx.f.setWsSubPgm(" ");
            }
        }
    }

    /** COBOL paragraph: 7500-SHOW-RESULT */
    private void populateResultScreen(TaskContext ctx) {
        if (ctx.f.getKoStatus().equals("O")) {
            ctx.f.setWsDStat("OK");
        } else if (ctx.f.getKoStatus().equals("W")) {
            ctx.f.setWsDStat("WARNING");
        } else {
            ctx.f.setWsDStat("ERROR");
        }
        ctx.f.setRstato(ctx.f.getWsDStat());
        ctx.f.setRmsgo(ctx.f.getKoStatusMsg());
        ctx.f.setWsDCnt(ctx.f.getKoReadCnt());
        ctx.f.setRcreado(ctx.f.getString("WS-D-CNT"));
        ctx.f.setWsDCnt(ctx.f.getKoSelectCnt());
        ctx.f.setRcselo(ctx.f.getString("WS-D-CNT"));
        ctx.f.setWsDCnt(ctx.f.getKoPostedCnt());
        ctx.f.setRcposto(ctx.f.getString("WS-D-CNT"));
        ctx.f.setWsDCnt(ctx.f.getKoUpdateCnt());
        ctx.f.setRcupdo(ctx.f.getString("WS-D-CNT"));
        ctx.f.setWsDCnt(ctx.f.getKoRejectCnt());
        ctx.f.setRcrejo(ctx.f.getString("WS-D-CNT"));
        ctx.f.setWsDCnt(ctx.f.getKoTranCnt());
        ctx.f.setRctrano(ctx.f.getString("WS-D-CNT"));
        ctx.f.setWsDAmt(ctx.f.getKoAmt1());
        ctx.f.setRamt1o(ctx.f.getString("WS-D-AMT"));
        ctx.f.setWsDAmt(ctx.f.getKoAmt2());
        ctx.f.setRamt2o(ctx.f.getString("WS-D-AMT"));
        ctx.f.setErrmsgo("Operation complete. Review the counts below.");
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 7000-XCTL-MENU */
    private void xctlToMenuProgram(TaskContext ctx) {
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
                "MOPSA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
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
    private void abendWithErrorMessage(TaskContext ctx) {
        ctx.f.setWsMsgText("OCOPS: unrecoverable error. Contact support.");
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

        final OcopsFields f;

        final AppService appService;

        final OcopsLinkParm link = new OcopsLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcopsFields(ws);
        }
    }
}

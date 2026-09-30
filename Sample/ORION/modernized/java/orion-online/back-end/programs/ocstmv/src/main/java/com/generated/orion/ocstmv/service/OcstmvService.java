package com.generated.orion.ocstmv.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcstmvLinkParm;
import com.generated.orion.ocstmv.accessor.OcstmvFields;
import com.generated.orion.ocstmv.metadata.OcstmvBmsMetadata;
import com.generated.orion.ocstmv.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCSTMV. */
@Service
public class OcstmvService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcstmvService.class);

    private static final int ORION_COMMAREA_LENGTH = 692;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        _0000Main(ctx);
    }

    @Override
    public String getProgramName() {
        return "OCSTMV";
    }

    @Override
    public String getTransId() {
        return "ORSV";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcstmvBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcstmvBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcstmvBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void _0000Main(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setCaErrMsg(" ");
        if (ctx.appService.getEibcalen() == 0) {
            ctx.f.setOrionCommarea("");
            _1000SendInitial(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            if (ctx.f.getCaPgmContext() == 0) {
                _1000SendInitial(ctx);
            } else {
                _2000ProcessInput(ctx);
            }
        }
        _9000Return(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void _1000SendInitial(TaskContext ctx) {
        ctx.f.fillLowValues("MSTMVAO");
        _8000PopulateHeader(ctx);
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setAcctidl((short) -1);
        ctx.appService.sendMap(
                "MSTMVA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void _2000ProcessInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            _7000XctlMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            _1000SendInitial(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            _2100ViewStmt(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            _8100SendDataonly(ctx);
        }
    }

    /** COBOL paragraph: 2100-VIEW-STMT */
    private void _2100ViewStmt(TaskContext ctx) {
        ctx.appService.receiveMap("MSTMVA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MSTMVAI");
        }
        _6100ValidateKey(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            ctx.f.setAcctidl((short) -1);
            _8100SendDataonly(ctx);
        } else {
            _3000ReadStmt(ctx);
            if (ctx.f.getWsFoundFlg().equals("Y")) {
                _4000PopulateDetail(ctx);
                ctx.f.setErrmsgo(ctx.f.getWsMShown());
            } else {
                _4100ClearDetail(ctx);
                ctx.f.setErrmsgo(ctx.f.getWsMNotfnd());
            }
            ctx.f.setAcctidl((short) -1);
            _8100SendDataonly(ctx);
        }
    }

    /** COBOL paragraph: 3000-READ-STMT */
    private void _3000ReadStmt(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.f.setStAcctId(ctx.f.getWsAcct());
        ctx.f.setStCycle(ctx.f.getWsCycle());
        ctx.appService.readFile(
                ctx.f.getWsStmtfile(),
                ctx.f,
                String.valueOf(ctx.f.getStAcctId()) + "|" + String.valueOf(ctx.f.getStCycle()),
                0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-FOUND-FLG", "N");
        } else {
            _9500AbendRtn(ctx);
        }
    }

    /** COBOL paragraph: 4000-POPULATE-DETAIL */
    private void _4000PopulateDetail(TaskContext ctx) {
        ctx.f.setAcctido(String.format("%011d", ctx.f.getStAcctId()));
        ctx.f.setStcyco(String.format("%06d", ctx.f.getStCycle()));
        ctx.f.setStopeno(formatEdBalance(ctx, ctx.f.getStOpenBal()));
        ctx.f.setStcloseo(formatEdBalance(ctx, ctx.f.getStCloseBal()));
        ctx.f.setStmino(formatEdBalance(ctx, ctx.f.getStMinDue()));
        ctx.f.setStdueo(ctx.f.getStDueDate());
    }

    /** Edit a statement balance into its 16-character display form via WS-ED-BAL. */
    private String formatEdBalance(TaskContext ctx, java.math.BigDecimal balance) {
        ctx.f.setWsEdBal(balance);
        return Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-BAL")), 16).substring(1, 16);
    }

    /** COBOL paragraph: 4100-CLEAR-DETAIL */
    private void _4100ClearDetail(TaskContext ctx) {
        ctx.f.setStopeno(" ");
        ctx.f.setStcloseo(" ");
        ctx.f.setStmino(" ");
        ctx.f.setStdueo(" ");
    }

    /** COBOL paragraph: 6100-VALIDATE-KEY */
    private void _6100ValidateKey(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setErrmsgo(" ");
        ctx.f.setWsNcIn(ctx.f.getAcctidi());
        ctx.f.setWsNcLen(11);
        _6000ParseNum(ctx);
        if (ctx.f.getWsValidSw().equals("N") || ctx.f.getWsNcDigits() > 11) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMAcctNum());
            return;
        }
        ctx.f.setWsAcct(ctx.f.getWsNcValue());
        ctx.f.setWsNcIn(ctx.f.getStcyci());
        ctx.f.setWsNcLen(6);
        _6000ParseNum(ctx);
        if (ctx.f.getWsValidSw().equals("N") || ctx.f.getWsNcDigits() != 6) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMCycNum());
            return;
        }
        ctx.f.setWsCycle(Utility.toCobolInt(ctx.f.getWsNcValue(), 6));
        ctx.f.setWsCycMm(
                Utility.parseNumeric(
                                Utility.padRight(String.format("%06d", ctx.f.getWsCycle()), 6)
                                        .substring(4, 6))
                        .intValue());
        if (ctx.f.getWsCycMm() < 1 || ctx.f.getWsCycMm() > 12) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMCycMm());
        }
    }

    /** COBOL paragraph: 6000-PARSE-NUM */
    private void _6000ParseNum(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsNcValue(0);
        ctx.f.setWsNcDigits(0);
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        for (ctx.f.setWsNcPos(1);
                ctx.f.getWsNcPos() <= ctx.f.getWsNcLen();
                ctx.f.setWsNcPos(ctx.f.getWsNcPos() + 1)) {
            ctx.f.setWsNcChar(
                    Utility.padRight(String.valueOf(ctx.f.getWsNcIn()), 256)
                            .substring(ctx.f.getWsNcPos() - 1, ctx.f.getWsNcPos() - 1 + 1));
            if (Utility.fieldEquals(ctx.f.getWsNcChar(), " ")) {
                /* CONTINUE */
            } else if ((ctx.f.getWsNcChar().compareTo("0") >= 0)
                    && (ctx.f.getWsNcChar().compareTo("9") <= 0)) {
                ctx.f.setWsNcDigit(Utility.parseNumeric(ctx.f.getWsNcChar()).intValue());
                ctx.f.setWsNcValue(((ctx.f.getWsNcValue() * (long) 10) + ctx.f.getWsNcDigit()));
                ctx.f.setWsNcDigits(ctx.f.getWsNcDigits() + 1);
            } else {
                ctx.f.setString("WS-VALID-SW", "N");
            }
        }
        if (ctx.f.getWsNcDigits() == 0) {
            ctx.f.setString("WS-VALID-SW", "N");
        }
    }

    /** COBOL paragraph: 7000-XCTL-MENU */
    private void _7000XctlMenu(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(
                ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), ORION_COMMAREA_LENGTH);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8000-POPULATE-HEADER */
    private void _8000PopulateHeader(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        _8500GetDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void _8100SendDataonly(TaskContext ctx) {
        _8000PopulateHeader(ctx);
        ctx.appService.sendMap(
                "MSTMVA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8500-GET-DATE-TIME */
    private void _8500GetDateTime(TaskContext ctx) {
        ctx.appService.askTime();
        com.appruntime.FormatTimeResult _ftResult =
                ctx.appService.formatTime(null, "YYYYMMDD", "-", ":");
        ctx.f.setWsHdrDate(_ftResult.getDate());
        ctx.f.setWsHdrTime(_ftResult.getTime());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 9000-RETURN */
    private void _9000Return(TaskContext ctx) {
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), ORION_COMMAREA_LENGTH);
    }

    /** COBOL paragraph: 9500-ABEND-RTN */
    private void _9500AbendRtn(TaskContext ctx) {
        ctx.f.setWsMsgText("OCSTMV: unrecoverable file error. Contact support.");
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

        final OcstmvFields f;

        final AppService appService;

        final OcstmvLinkParm link = new OcstmvLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcstmvFields(ws);
        }
    }
}

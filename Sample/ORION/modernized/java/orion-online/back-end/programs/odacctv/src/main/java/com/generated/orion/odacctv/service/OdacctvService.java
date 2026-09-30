package com.generated.orion.odacctv.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OdacctvLinkParm;
import com.generated.orion.odacctv.accessor.OdacctvFields;
import com.generated.orion.odacctv.dao.OdacctvDao;
import com.generated.orion.odacctv.metadata.OdacctvBmsMetadata;
import com.generated.orion.odacctv.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program ODACCTV. */
@Service
public class OdacctvService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OdacctvService.class);

    /** Length (in bytes) of the ORION-COMMAREA passed on XCTL/RETURN. */
    private static final int COMMAREA_LENGTH = 692;

    @Autowired private OdacctvDao dao;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        ctx.dao = this.dao;
        executeMainLine(ctx);
    }

    @Override
    public String getProgramName() {
        return "ODACCTV";
    }

    @Override
    public String getTransId() {
        return "OD01";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OdacctvBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OdacctvBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OdacctvBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void executeMainLine(TaskContext ctx) {
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
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
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
    private void processUserInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            readAndDisplayAccount(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-READ-AND-SHOW */
    private void readAndDisplayAccount(TaskContext ctx) {
        ctx.appService.receiveMap("MACCTVA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (Utility.fieldEquals(ctx.f.getAcctidi(), " ") || ctx.f.isAllLowValues("ACCTIDI")) {
            ctx.f.setErrmsgo(ctx.f.getWsMsgRequired());
            sendDataOnlyScreen(ctx);
        } else {
            if (Utility.isNumeric(ctx.f.getRawString("ACCTIDI"))) {
                ctx.f.setAcId(Utility.parseNumeric(ctx.f.getAcctidi()).longValue());
                ctx.f.setCaAcctId(ctx.f.getAcId());
                loadAccountRecord(ctx);
                if (ctx.f.getWsFoundFlg().equals("Y")) {
                    populateAccountDetail(ctx);
                    ctx.f.setErrmsgo("Account displayed.");
                    sendDataOnlyScreen(ctx);
                } else {
                    ctx.f.setErrmsgo(ctx.f.getWsMsgNotfnd());
                    sendDataOnlyScreen(ctx);
                }
            } else {
                ctx.f.setErrmsgo("Account id must be numeric.");
                sendDataOnlyScreen(ctx);
            }
        }
    }

    /** COBOL paragraph: 3000-READ-ACCT */
    private void loadAccountRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        // DB2 SELECT INTO — ORION.ACCT
        {
            java.util.Map<String, Object> _sqlResult = dao.selectOrionAcct(ctx.f.getAcId());
            if (!_sqlResult.containsKey("SQLCODE")) {
                ctx.f.setAcActiveStatus(
                        (_sqlResult.get("AC_ACTIVE_STATUS") == null
                                ? ""
                                : String.valueOf(_sqlResult.get("AC_ACTIVE_STATUS"))));
                ctx.f.setAcCurrBal(
                        (_sqlResult.get("AC_CURR_BAL") == null
                                ? java.math.BigDecimal.ZERO
                                : new java.math.BigDecimal(
                                        ((Number) _sqlResult.get("AC_CURR_BAL")).toString())));
                ctx.f.setAcCreditLimit(
                        (_sqlResult.get("AC_CREDIT_LIMIT") == null
                                ? java.math.BigDecimal.ZERO
                                : new java.math.BigDecimal(
                                        ((Number) _sqlResult.get("AC_CREDIT_LIMIT")).toString())));
                ctx.f.setAcCashLimit(
                        (_sqlResult.get("AC_CASH_LIMIT") == null
                                ? java.math.BigDecimal.ZERO
                                : new java.math.BigDecimal(
                                        ((Number) _sqlResult.get("AC_CASH_LIMIT")).toString())));
                ctx.f.setAcOpenDate(
                        (_sqlResult.get("AC_OPEN_DATE") == null
                                ? ""
                                : String.valueOf(_sqlResult.get("AC_OPEN_DATE"))));
                ctx.f.setAcExpiryDate(
                        (_sqlResult.get("AC_EXPIRY_DATE") == null
                                ? ""
                                : String.valueOf(_sqlResult.get("AC_EXPIRY_DATE"))));
                ctx.f.setAcGroupId(
                        (_sqlResult.get("AC_GROUP_ID") == null
                                ? ""
                                : String.valueOf(_sqlResult.get("AC_GROUP_ID"))));
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
                ctx.f.setErrmsgo("Error reading account table.");
            }
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
        populateCurrentDateTime(ctx);
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
    private void populateCurrentDateTime(TaskContext ctx) {
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

        final OdacctvFields f;

        final AppService appService;

        final OdacctvLinkParm link = new OdacctvLinkParm();

        OdacctvDao dao;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OdacctvFields(ws);
        }
    }
}

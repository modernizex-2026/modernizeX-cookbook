package com.generated.orion.odacctu.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OdacctuLinkParm;
import com.generated.orion.odacctu.accessor.OdacctuFields;
import com.generated.orion.odacctu.dao.OdacctuDao;
import com.generated.orion.odacctu.metadata.OdacctuBmsMetadata;
import com.generated.orion.odacctu.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program ODACCTU. */
@Service
public class OdacctuService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OdacctuService.class);

    /** DB2 SQLCODE returned when the SELECT/UPDATE affects no row. */
    private static final int SQLCODE_NOT_FOUND = 100;

    /** Byte length of the ORION-COMMAREA passed across XCTL/RETURN. */
    private static final int COMMAREA_LENGTH = 692;

    @Autowired private OdacctuDao dao;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        ctx.dao = this.dao;
        runMainDispatch(ctx);
    }

    @Override
    public String getProgramName() {
        return "ODACCTU";
    }

    @Override
    public String getTransId() {
        return "OD02";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OdacctuBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OdacctuBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OdacctuBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainDispatch(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setCaErrMsg(" ");
        if (ctx.appService.getEibcalen() == 0) {
            sendInitialScreen(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitialScreen(ctx);
            } else {
                processInputByAid(ctx);
            }
        }
        returnTransaction(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MACCTUAO");
        populateScreenHeader(ctx);
        ctx.f.setCaWorkArea(
                Utility.setSubstring(
                        String.valueOf(ctx.f.getCaWorkArea()), 1, 1, String.valueOf("F")));
        ctx.f.setErrmsgo("Enter account id and press ENTER to fetch.");
        ctx.appService.sendMap(
                "MACCTUA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInputByAid(TaskContext ctx) {
        ctx.f.setWsModeFlag(
                Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 1).substring(0, 1));
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            xctlToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            fetchAccount(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "5")) {
            updateAccount(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-FETCH-ACCT */
    private void fetchAccount(TaskContext ctx) {
        ctx.appService.receiveMap("MACCTUA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (Utility.fieldEquals(ctx.f.getAcctidi(), " ") || ctx.f.isAllLowValues("ACCTIDI")) {
            ctx.f.setErrmsgo(ctx.f.getWsMsgRequired());
            sendDataOnlyScreen(ctx);
            return;
        }
        if (!Utility.isNumeric(ctx.f.getRawString("ACCTIDI"))) {
            ctx.f.setErrmsgo("Account id must be numeric.");
            sendDataOnlyScreen(ctx);
            return;
        }
        ctx.f.setAcId(Utility.parseNumeric(ctx.f.getAcctidi()).longValue());
        ctx.f.setCaAcctId(ctx.f.getAcId());
        readAccountRecord(ctx);
        if (ctx.f.getWsFoundFlg().equals("Y")) {
            populateAccountDetail(ctx);
            ctx.f.setCaWorkArea(
                    Utility.setSubstring(
                            String.valueOf(ctx.f.getCaWorkArea()), 1, 1, String.valueOf("U")));
            ctx.f.setErrmsgo("Amend fields and press PF5 to update.");
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMsgNotfnd());
        }
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 2200-UPDATE-ACCT */
    private void updateAccount(TaskContext ctx) {
        ctx.appService.receiveMap("MACCTUA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (!ctx.f.getWsModeFlag().equals("U")) {
            ctx.f.setErrmsgo("Press ENTER to fetch a row before PF5.");
            sendDataOnlyScreen(ctx);
            return;
        }
        validateAccountInput(ctx);
        if (ctx.f.getWsValidFlag().equals("Y")) {
            ctx.f.setAcId(Utility.parseNumeric(ctx.f.getAcctidi()).longValue());
            updateAccountRecord(ctx);
            ctx.f.setErrmsgo(
                    ctx.f.getWsFoundFlg().equals("Y")
                            ? "Account updated successfully."
                            : ctx.f.getWsMsgNotfnd());
        }
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 2300-EDIT-INPUT */
    private void validateAccountInput(TaskContext ctx) {
        ctx.f.setString("WS-VALID-FLAG", "Y");
        if (Utility.fieldEquals(ctx.f.getAcctidi(), " ")
                || !Utility.isNumeric(ctx.f.getRawString("ACCTIDI"))) {
            ctx.f.setErrmsgo("Account id is required and numeric.");
            ctx.f.setString("WS-VALID-FLAG", "N");
            return;
        }
        if (!(Utility.fieldEquals(ctx.f.getAcstati(), "Y")
                || Utility.fieldEquals(ctx.f.getAcstati(), "N"))) {
            ctx.f.setErrmsgo("Status must be Y or N.");
            ctx.f.setString("WS-VALID-FLAG", "N");
            return;
        }
        if (Utility.fieldEquals(ctx.f.getAccrlimi(), " ")
                || Utility.fieldEquals(ctx.f.getAccslimi(), " ")) {
            ctx.f.setErrmsgo("Credit and cash limits are required.");
            ctx.f.setString("WS-VALID-FLAG", "N");
            return;
        }
        ctx.f.setAcActiveStatus(ctx.f.getAcstati());
        ctx.f.setAcCreditLimit(Utility.parseNumeric(String.valueOf(ctx.f.getAccrlimi())));
        ctx.f.setAcCashLimit(Utility.parseNumeric(String.valueOf(ctx.f.getAccslimi())));
        ctx.f.setAcExpiryDate(ctx.f.getAcexpi());
        ctx.f.setAcGroupId(ctx.f.getAcgrpi());
    }

    /** COBOL paragraph: 3000-READ-ACCT */
    private void readAccountRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        // DB2 SELECT INTO — ORION.ACCT
        {
            java.util.Map<String, Object> _sqlResult = dao.selectOrionAcct(ctx.f.getAcId());
            if (!_sqlResult.containsKey("SQLCODE")) {
                ctx.f.setAcActiveStatus(
                        (_sqlResult.get("AC_ACTIVE_STATUS") == null
                                ? ""
                                : String.valueOf(_sqlResult.get("AC_ACTIVE_STATUS"))));
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
            case SQLCODE_NOT_FOUND -> {
                ctx.f.setString("WS-FOUND-FLG", "N");
            }
            default -> {
                ctx.f.setString("WS-FOUND-FLG", "N");
                ctx.f.setErrmsgo("Error reading account table.");
            }
        }
    }

    /** COBOL paragraph: 3100-UPDATE-ACCT */
    private void updateAccountRecord(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        // DB2 UPDATE — ORION.ACCT
        {
            java.util.Map<String, Object> _sqlResult =
                    dao.updateOrionAcct(
                            ctx.f.getAcId(),
                            ctx.f.getAcActiveStatus(),
                            ctx.f.getAcCreditLimit(),
                            ctx.f.getAcCashLimit(),
                            ctx.f.getAcExpiryDate(),
                            ctx.f.getAcGroupId());
            ctx.f.setSqlcode(
                    _sqlResult.get("SQLCODE") != null ? (Integer) _sqlResult.get("SQLCODE") : 0);
        }
        switch (ctx.f.getSqlcode()) {
            case 0 -> {
                ctx.f.setString("WS-FOUND-FLG", "Y");
            }
            case SQLCODE_NOT_FOUND -> {
                ctx.f.setString("WS-FOUND-FLG", "N");
            }
            default -> {
                ctx.f.setString("WS-FOUND-FLG", "N");
                ctx.f.setErrmsgo("Error updating account table.");
            }
        }
    }

    /** COBOL paragraph: 4000-POPULATE-DETAIL */
    private void populateAccountDetail(TaskContext ctx) {
        ctx.f.setAcctido(String.format("%011d", ctx.f.getAcId()));
        ctx.f.setAcstato(ctx.f.getAcActiveStatus());
        ctx.f.setWsEdAmt(ctx.f.getAcCreditLimit());
        ctx.f.setAccrlimo(ctx.f.getString("WS-ED-AMT"));
        ctx.f.setWsEdAmt(ctx.f.getAcCashLimit());
        ctx.f.setAccslimo(ctx.f.getString("WS-ED-AMT"));
        ctx.f.setAcexpo(ctx.f.getAcExpiryDate());
        ctx.f.setAcgrpo(ctx.f.getAcGroupId());
    }

    /** COBOL paragraph: 7000-XCTL-MENU */
    private void xctlToMenu(TaskContext ctx) {
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
                "MACCTUA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
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
    private void returnTransaction(TaskContext ctx) {
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OdacctuFields f;

        final AppService appService;

        final OdacctuLinkParm link = new OdacctuLinkParm();

        OdacctuDao dao;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OdacctuFields(ws);
        }
    }
}

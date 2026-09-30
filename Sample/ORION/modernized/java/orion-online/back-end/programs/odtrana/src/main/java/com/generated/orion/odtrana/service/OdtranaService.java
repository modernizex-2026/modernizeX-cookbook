package com.generated.orion.odtrana.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OdtranaLinkParm;
import com.generated.orion.odtrana.accessor.OdtranaFields;
import com.generated.orion.odtrana.dao.OdtranaDao;
import com.generated.orion.odtrana.metadata.OdtranaBmsMetadata;
import com.generated.orion.odtrana.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program ODTRANA. */
@Service
public class OdtranaService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OdtranaService.class);

    /** DB2 SQLCODE meaning "row not found". */
    private static final int SQLCODE_NOT_FOUND = 100;

    /** Fixed length of the ORION-COMMAREA passed between programs. */
    private static final int COMMAREA_LENGTH = 692;

    @Autowired private OdtranaDao dao;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        ctx.dao = this.dao;
        runMainProgram(ctx);
    }

    @Override
    public String getProgramName() {
        return "ODTRANA";
    }

    @Override
    public String getTransId() {
        return "OD06";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OdtranaBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OdtranaBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OdtranaBmsMetadata.getFieldMapping(mapName);
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
                processInputAction(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MTRANAAO");
        populateScreenHeader(ctx);
        ctx.f.setErrmsgo("Enter transaction detail and press ENTER.");
        ctx.appService.sendMap(
                "MTRANAA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInputAction(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            addTransaction(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-ADD-TRAN */
    private void addTransaction(TaskContext ctx) {
        ctx.appService.receiveMap("MTRANAA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        validateTransactionInput(ctx);
        if (ctx.f.getWsValidFlag().equals("Y")) {
            checkCardExists(ctx);
            if (ctx.f.getWsFoundFlg().equals("Y")) {
                checkTransactionType(ctx);
                if (ctx.f.getWsFoundFlg().equals("Y")) {
                    getNextTransactionId(ctx);
                    buildTransactionRecord(ctx);
                    insertTransaction(ctx);
                    if (ctx.f.getWsFoundFlg().equals("Y")) {
                        ctx.f.setErrmsgo(" ");
                        ctx.f.setErrmsgo("Transaction added. Id=" + ctx.f.getTrId());
                    }
                } else {
                    ctx.f.setErrmsgo("Transaction type not found.");
                }
            } else {
                ctx.f.setErrmsgo("Card number not found.");
            }
        }
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 2200-EDIT-INPUT */
    private void validateTransactionInput(TaskContext ctx) {
        ctx.f.setString("WS-VALID-FLAG", "Y");
        if (Utility.fieldEquals(ctx.f.getCardnumi(), " ") || ctx.f.isAllLowValues("CARDNUMI")) {
            ctx.f.setErrmsgo("Card number is required.");
            ctx.f.setString("WS-VALID-FLAG", "N");
        }
        if (ctx.f.getWsValidFlag().equals("Y")) {
            if (Utility.fieldEquals(ctx.f.getTrtypei(), " ") || ctx.f.isAllLowValues("TRTYPEI")) {
                ctx.f.setErrmsgo("Transaction type is required.");
                ctx.f.setString("WS-VALID-FLAG", "N");
            }
        }
        if (ctx.f.getWsValidFlag().equals("Y")) {
            if (Utility.fieldEquals(ctx.f.getTrcati(), " ")
                    || !Utility.isNumeric(ctx.f.getRawString("TRCATI"))) {
                ctx.f.setErrmsgo("Category must be numeric.");
                ctx.f.setString("WS-VALID-FLAG", "N");
            }
        }
        if (ctx.f.getWsValidFlag().equals("Y")) {
            if (Utility.fieldEquals(ctx.f.getTramti(), " ") || ctx.f.isAllLowValues("TRAMTI")) {
                ctx.f.setErrmsgo("Amount is required.");
                ctx.f.setString("WS-VALID-FLAG", "N");
            }
        }
    }

    /** COBOL paragraph: 3000-CHECK-CARD */
    private void checkCardExists(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.f.setTrCardNum(ctx.f.getCardnumi());
        // DB2 SELECT INTO — ORION.CARD
        {
            java.util.Map<String, Object> _sqlResult = dao.selectOrionCard(ctx.f.getTrCardNum());
            if (!_sqlResult.containsKey("SQLCODE")) {
                ctx.f.setWsChkCardStat(
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
            case SQLCODE_NOT_FOUND -> {
                ctx.f.setString("WS-FOUND-FLG", "N");
            }
            default -> {
                ctx.f.setString("WS-FOUND-FLG", "N");
                ctx.f.setErrmsgo("Error reading card table.");
            }
        }
    }

    /** COBOL paragraph: 3100-CHECK-TYPE */
    private void checkTransactionType(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.f.setTrTypeCd(ctx.f.getTrtypei());
        // DB2 SELECT INTO — ORION.TTYP
        {
            java.util.Map<String, Object> _sqlResult = dao.selectOrionTtyp(ctx.f.getTrTypeCd());
            if (!_sqlResult.containsKey("SQLCODE")) {
                ctx.f.setWsChkTypeDesc(
                        (_sqlResult.get("TT_DESC") == null
                                ? ""
                                : String.valueOf(_sqlResult.get("TT_DESC"))));
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
                ctx.f.setErrmsgo("Error reading type table.");
            }
        }
    }

    /** COBOL paragraph: 3200-GET-NEXT-ID */
    private void getNextTransactionId(TaskContext ctx) {
        // DB2 SELECT INTO — ORION.CTRL
        {
            java.util.Map<String, Object> _sqlResult = dao.selectOrionCtrl(ctx.f.getWsCtKey());
            if (!_sqlResult.containsKey("SQLCODE")) {
                ctx.f.setWsCtValue(
                        (_sqlResult.get("CT_LAST_VALUE") == null
                                ? 0L
                                : ((Number) _sqlResult.get("CT_LAST_VALUE")).longValue()));
                ctx.f.setSqlcode(0);
            } else {
                ctx.f.setSqlcode(
                        _sqlResult.get("SQLCODE") != null
                                ? (Integer) _sqlResult.get("SQLCODE")
                                : 0);
            }
        }
        if (ctx.f.getSqlcode() == 0) {
            ctx.f.setWsCtValue(ctx.f.getWsCtValue() + 1);
            // DB2 UPDATE — ORION.CTRL
            {
                java.util.Map<String, Object> _sqlResult =
                        dao.updateOrionCtrl(ctx.f.getWsCtKey(), ctx.f.getWsCtValue());
                ctx.f.setSqlcode(
                        _sqlResult.get("SQLCODE") != null
                                ? (Integer) _sqlResult.get("SQLCODE")
                                : 0);
            }
        } else {
            ctx.f.setWsCtValue(1);
        }
        ctx.f.setWsTranIdN(ctx.f.getWsCtValue());
        ctx.f.setTrId(String.format("%016d", ctx.f.getWsTranIdN()));
    }

    /** COBOL paragraph: 3300-BUILD-RECORD */
    private void buildTransactionRecord(TaskContext ctx) {
        loadCurrentDateTime(ctx);
        ctx.f.setWsTsStamp(" ");
        ctx.f.setWsTsStamp(ctx.f.getWsHdrDate() + " " + ctx.f.getWsHdrTime());
        ctx.f.setTrTypeCd(ctx.f.getTrtypei());
        ctx.f.setTrCatCd(Utility.parseNumeric(ctx.f.getTrcati()).intValue());
        ctx.f.setTrSource("ONLINE");
        ctx.f.setTrDesc(ctx.f.getTrdesci());
        ctx.f.setTrAmt(Utility.parseNumeric(String.valueOf(ctx.f.getTramti())));
        ctx.f.setTrMerchantId(0);
        ctx.f.setTrMerchantName(ctx.f.getTrmerchi());
        ctx.f.setTrMerchantCity(" ");
        ctx.f.setTrMerchantZip(" ");
        ctx.f.setTrCardNum(ctx.f.getCardnumi());
        ctx.f.setTrOrigTs(ctx.f.getWsTsStamp());
        ctx.f.setTrProcTs(ctx.f.getWsTsStamp());
    }

    /** COBOL paragraph: 3400-INSERT-TRAN */
    private void insertTransaction(TaskContext ctx) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        // DB2 INSERT — ORION.TRAN
        {
            java.util.Map<String, Object> _sqlResult =
                    dao.insertOrionTran(
                            ctx.f.getTrId(),
                            ctx.f.getTrTypeCd(),
                            ctx.f.getTrCatCd(),
                            ctx.f.getTrSource(),
                            ctx.f.getTrDesc(),
                            ctx.f.getTrAmt(),
                            ctx.f.getTrMerchantId(),
                            ctx.f.getTrMerchantName(),
                            ctx.f.getTrMerchantCity(),
                            ctx.f.getTrMerchantZip(),
                            ctx.f.getTrCardNum(),
                            ctx.f.getTrOrigTs(),
                            ctx.f.getTrProcTs());
            ctx.f.setSqlcode(
                    _sqlResult.get("SQLCODE") != null ? (Integer) _sqlResult.get("SQLCODE") : 0);
        }
        switch (ctx.f.getSqlcode()) {
            case 0 -> {
                ctx.f.setString("WS-FOUND-FLG", "Y");
            }
            default -> {
                ctx.f.setString("WS-FOUND-FLG", "N");
                ctx.f.setErrmsgo("Error inserting transaction row.");
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
                "MTRANAA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
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

        final OdtranaFields f;

        final AppService appService;

        final OdtranaLinkParm link = new OdtranaLinkParm();

        OdtranaDao dao;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OdtranaFields(ws);
        }
    }
}

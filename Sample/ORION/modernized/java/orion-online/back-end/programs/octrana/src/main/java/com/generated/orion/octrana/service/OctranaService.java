package com.generated.orion.octrana.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OctranaLinkParm;
import com.generated.orion.octrana.accessor.OctranaFields;
import com.generated.orion.octrana.metadata.OctranaBmsMetadata;
import com.generated.orion.octrana.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCTRANA. */
@Service
public class OctranaService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OctranaService.class);

    /** Working buffer size used when padding/truncating input fields for validation. */
    private static final int MAX_FIELD_LENGTH = 256;

    /** Length of the ORION-COMMAREA passed on XCTL/RETURN. */
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
        return "OCTRANA";
    }

    @Override
    public String getTransId() {
        return "ORTA";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OctranaBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OctranaBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OctranaBmsMetadata.getFieldMapping(mapName);
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
                processUserInput(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MTRANAAO");
        populateScreenHeader(ctx);
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setCardnuml((short) -1);
        ctx.appService.sendMap(
                "MTRANAA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
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
        validateInputFields(ctx);
        if (ctx.f.getWsEditFlag().equals("N")) {
            sendDataOnlyScreen(ctx);
        } else {
            assignNextTransactionId(ctx);
            if (ctx.f.getWsIdFlag().equals("Y")) {
                writeTransactionRecord(ctx);
            } else {
                ctx.f.setCardnuml((short) -1);
                sendDataOnlyScreen(ctx);
            }
        }
    }

    /** COBOL paragraph: 2200-CONFIRM-ADD */
    private void confirmTransactionAdded(TaskContext ctx) {
        ctx.f.fillLowValues("MTRANAAO");
        populateScreenHeader(ctx);
        ctx.f.setErrmsgo(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(ctx.f.getWsMAddPrefix()));
            sb.append(String.valueOf(ctx.f.getTrId()));
            sb.append(String.valueOf(ctx.f.getWsMAddSuffix()));
            ctx.f.setErrmsgo(sb.toString());
        }
        ctx.f.setCardnuml((short) -1);
        ctx.appService.sendMap(
                "MTRANAA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 5000-EDIT-INPUT */
    private void validateInputFields(TaskContext ctx) {
        ctx.f.setString("WS-EDIT-FLAG", "Y");
        validateCardNumber(ctx);
        if (ctx.f.getWsEditFlag().equals("Y")) {
            validateTransactionType(ctx);
        }
        if (ctx.f.getWsEditFlag().equals("Y")) {
            validateTransactionCategory(ctx);
        }
        if (ctx.f.getWsEditFlag().equals("Y")) {
            validateTransactionAmount(ctx);
        }
        if (ctx.f.getWsEditFlag().equals("Y")) {
            validateMerchantName(ctx);
        }
        if (ctx.f.getWsEditFlag().equals("Y")) {
            validateDescription(ctx);
        }
    }

    /** COBOL paragraph: 5100-EDIT-CARD */
    private void validateCardNumber(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getCardnumi(), " ") || ctx.f.isAllLowValues("CARDNUMI")) {
            ctx.f.setString("WS-EDIT-FLAG", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMCardReq());
            ctx.f.setCardnuml((short) -1);
        } else {
            ctx.f.setWsCardKey(ctx.f.getCardnumi());
            ctx.f.setWsCardKey(stripLowValues(ctx.f.getWsCardKey()));
            readCardCrossReference(ctx);
            if (ctx.f.getWsFoundFlg().equals("N")) {
                ctx.f.setString("WS-EDIT-FLAG", "N");
                ctx.f.setErrmsgo(ctx.f.getWsMCardBad());
                ctx.f.setCardnuml((short) -1);
            }
        }
    }

    /** COBOL paragraph: 5150-READ-XREF */
    private void readCardCrossReference(TaskContext ctx) {
        ctx.f.setXrCardNum(ctx.f.getWsCardKey());
        readAndSetFoundFlag(ctx, ctx.f.getWsXreffile(), String.valueOf(ctx.f.getXrCardNum()));
    }

    /** COBOL paragraph: 5200-EDIT-TYPE */
    private void validateTransactionType(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getTrtypei(), " ") || ctx.f.isAllLowValues("TRTYPEI")) {
            ctx.f.setString("WS-EDIT-FLAG", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMTypeReq());
            ctx.f.setTrtypel((short) -1);
        } else {
            ctx.f.setTtCd(ctx.f.getTrtypei());
            ctx.f.setTtCd(stripLowValues(ctx.f.getTtCd()));
            readTransactionTypeMaster(ctx);
            if (ctx.f.getWsFoundFlg().equals("N")) {
                ctx.f.setString("WS-EDIT-FLAG", "N");
                ctx.f.setErrmsgo(ctx.f.getWsMTypeBad());
                ctx.f.setTrtypel((short) -1);
            }
        }
    }

    /** COBOL paragraph: 5250-READ-TTYP */
    private void readTransactionTypeMaster(TaskContext ctx) {
        readAndSetFoundFlag(ctx, ctx.f.getWsTtypfile(), String.valueOf(ctx.f.getTtCd()));
    }

    /** COBOL paragraph: 5300-EDIT-CAT */
    private void validateTransactionCategory(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getTrcati(), " ") || ctx.f.isAllLowValues("TRCATI")) {
            ctx.f.setString("WS-EDIT-FLAG", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMCatReq());
            ctx.f.setTrcatl((short) -1);
        } else {
            ctx.f.setWsValStr(" ");
            ctx.f.setWsValStr(ctx.f.getTrcati());
            ctx.f.setWsValLen(4);
            validateNumericField(ctx);
            if (ctx.f.getWsValOk().equals("N")) {
                ctx.f.setString("WS-EDIT-FLAG", "N");
                ctx.f.setErrmsgo(ctx.f.getWsMCatNum());
                ctx.f.setTrcatl((short) -1);
            } else {
                ctx.f.setWsInCat(
                        Utility.parseNumeric(
                                        String.valueOf(
                                                Utility.padRight(
                                                                String.valueOf(ctx.f.getWsValStr()),
                                                                MAX_FIELD_LENGTH)
                                                        .substring(0, ctx.f.getWsValLen())))
                                .intValue());
                ctx.f.setTcTypeCd(ctx.f.getTrtypei());
                ctx.f.setTcCd(ctx.f.getWsInCat());
                ctx.f.setTcTypeCd(stripLowValues(ctx.f.getTcTypeCd()));
                readTransactionCategoryMaster(ctx);
                if (ctx.f.getWsFoundFlg().equals("N")) {
                    ctx.f.setString("WS-EDIT-FLAG", "N");
                    ctx.f.setErrmsgo(ctx.f.getWsMCatBad());
                    ctx.f.setTrcatl((short) -1);
                }
            }
        }
    }

    /** COBOL paragraph: 5350-READ-TCAT */
    private void readTransactionCategoryMaster(TaskContext ctx) {
        readAndSetFoundFlag(
                ctx,
                ctx.f.getWsTcatfile(),
                String.valueOf(ctx.f.getTcTypeCd()) + "|" + String.valueOf(ctx.f.getTcCd()));
    }

    /** COBOL paragraph: 5400-EDIT-AMT */
    private void validateTransactionAmount(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getTramti(), " ") || ctx.f.isAllLowValues("TRAMTI")) {
            ctx.f.setString("WS-EDIT-FLAG", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMAmtReq());
            ctx.f.setTramtl((short) -1);
        } else {
            ctx.f.setWsValStr(" ");
            ctx.f.setWsValStr(ctx.f.getTramti());
            ctx.f.setWsValLen(12);
            validateAmountField(ctx);
            if (ctx.f.getWsValOk().equals("N")) {
                ctx.f.setString("WS-EDIT-FLAG", "N");
                ctx.f.setErrmsgo(ctx.f.getWsMAmtBad());
                ctx.f.setTramtl((short) -1);
            } else {
                ctx.f.setWsAmtNum(
                        Utility.parseNumeric(
                                String.valueOf(
                                        Utility.padRight(
                                                        String.valueOf(ctx.f.getWsValStr()),
                                                        MAX_FIELD_LENGTH)
                                                .substring(0, ctx.f.getWsValLen()))));
                if ((ctx.f.getWsAmtNum().signum() <= 0)) {
                    ctx.f.setString("WS-EDIT-FLAG", "N");
                    ctx.f.setErrmsgo(ctx.f.getWsMAmtZero());
                    ctx.f.setTramtl((short) -1);
                }
            }
        }
    }

    /** COBOL paragraph: 5500-EDIT-MERCH */
    private void validateMerchantName(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getTrmerchi(), " ") || ctx.f.isAllLowValues("TRMERCHI")) {
            ctx.f.setString("WS-EDIT-FLAG", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMMerchReq());
            ctx.f.setTrmerchl((short) -1);
        }
    }

    /** COBOL paragraph: 5600-EDIT-DESC */
    private void validateDescription(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getTrdesci(), " ") || ctx.f.isAllLowValues("TRDESCI")) {
            ctx.f.setString("WS-EDIT-FLAG", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMDescReq());
            ctx.f.setTrdescl((short) -1);
        }
    }

    /** COBOL paragraph: 6000-GET-NEXT-ID */
    private void assignNextTransactionId(TaskContext ctx) {
        ctx.f.setString("WS-ID-FLAG", "N");
        ctx.f.setCtKey("TRANID");
        ctx.appService.readFileForUpdate(
                ctx.f.getWsCtrlfile(), ctx.f, String.valueOf(ctx.f.getCtKey()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setCtLastValue(ctx.f.getCtLastValue() + 1);
            rewriteControlCounter(ctx);
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setErrmsgo(ctx.f.getWsMCtrMissing());
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMCtrError());
        }
    }

    /** COBOL paragraph: 6100-REWRITE-CTR */
    private void rewriteControlCounter(TaskContext ctx) {
        ctx.appService.rewriteFile(ctx.f.getWsCtrlfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setWsNewTranid(ctx.f.getCtLastValue());
            ctx.f.setString("WS-ID-FLAG", "Y");
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMCtrError());
        }
    }

    /** COBOL paragraph: 6500-WRITE-TRAN */
    private void writeTransactionRecord(TaskContext ctx) {
        buildTransactionRecord(ctx);
        ctx.appService.writeFile(ctx.f.getWsTranfile(), ctx.f, String.valueOf(ctx.f.getTrId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            confirmTransactionAdded(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMWriteError());
            ctx.f.setCardnuml((short) -1);
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 6600-BUILD-RECORD */
    private void buildTransactionRecord(TaskContext ctx) {
        ctx.f.setTranRec("");
        loadCurrentDateTime(ctx);
        ctx.f.setWsTimestamp(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(ctx.f.getWsHdrDate()));
            sb.append(String.valueOf(" "));
            sb.append(String.valueOf(ctx.f.getWsHdrTime()));
            ctx.f.setWsTimestamp(sb.toString());
        }
        ctx.f.setTrId(String.format("%016d", ctx.f.getWsNewTranid()));
        ctx.f.setTrTypeCd(ctx.f.getTrtypei());
        ctx.f.setTrTypeCd(stripLowValues(ctx.f.getTrTypeCd()));
        ctx.f.setTrCatCd(ctx.f.getWsInCat());
        ctx.f.setTrSource(ctx.f.getWsMSource());
        ctx.f.setTrDesc(ctx.f.getTrdesci());
        ctx.f.setTrAmt(ctx.f.getWsAmtNum());
        ctx.f.setTrMerchantId(0);
        ctx.f.setTrMerchantName(ctx.f.getTrmerchi());
        ctx.f.setTrMerchantCity(" ");
        ctx.f.setTrMerchantZip(" ");
        ctx.f.setTrCardNum(ctx.f.getWsCardKey());
        ctx.f.setTrOrigTs(ctx.f.getWsTimestamp());
        ctx.f.setTrProcTs(ctx.f.getWsTimestamp());
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

    /** COBOL paragraph: 7600-VALIDATE-NUM */
    private void validateNumericField(TaskContext ctx) {
        ctx.f.setString("WS-VAL-OK", "Y");
        ctx.f.setWsValDigits(0);
        String paddedValStr = Utility.padRight(ctx.f.getWsValStr(), MAX_FIELD_LENGTH);
        ctx.f.setSubstring(
                "WS-VAL-STR",
                1,
                ctx.f.getWsValLen(),
                stripLowValues(paddedValStr.substring(0, ctx.f.getWsValLen())));
        for (ctx.f.setWsValI(1);
                ctx.f.getWsValI() <= ctx.f.getWsValLen();
                ctx.f.setWsValI(ctx.f.getWsValI() + 1)) {
            ctx.f.setWsValCh(
                    paddedValStr.substring(ctx.f.getWsValI() - 1, ctx.f.getWsValI() - 1 + 1));
            if (Utility.fieldEquals(ctx.f.getWsValCh(), " ")) {
                /* CONTINUE */
            } else {
                if ((ctx.f.getWsValCh().compareTo("0") >= 0)
                        && (ctx.f.getWsValCh().compareTo("9") <= 0)) {
                    ctx.f.setWsValDigits(ctx.f.getWsValDigits() + 1);
                } else {
                    ctx.f.setString("WS-VAL-OK", "N");
                }
            }
        }
        if (ctx.f.getWsValDigits() == 0) {
            ctx.f.setString("WS-VAL-OK", "N");
        }
    }

    /** COBOL paragraph: 7700-VALIDATE-AMT */
    private void validateAmountField(TaskContext ctx) {
        ctx.f.setString("WS-VAL-OK", "Y");
        ctx.f.setWsValDigits(0);
        ctx.f.setWsDotCnt(0);
        String paddedValStr = Utility.padRight(ctx.f.getWsValStr(), MAX_FIELD_LENGTH);
        ctx.f.setSubstring(
                "WS-VAL-STR",
                1,
                ctx.f.getWsValLen(),
                stripLowValues(paddedValStr.substring(0, ctx.f.getWsValLen())));
        for (ctx.f.setWsValI(1);
                ctx.f.getWsValI() <= ctx.f.getWsValLen();
                ctx.f.setWsValI(ctx.f.getWsValI() + 1)) {
            ctx.f.setWsValCh(
                    paddedValStr.substring(ctx.f.getWsValI() - 1, ctx.f.getWsValI() - 1 + 1));
            if (Utility.fieldEquals(ctx.f.getWsValCh(), " ")) {
                /* CONTINUE */
            } else if ((ctx.f.getWsValCh().compareTo("0") >= 0)
                    && (ctx.f.getWsValCh().compareTo("9") <= 0)) {
                ctx.f.setWsValDigits(ctx.f.getWsValDigits() + 1);
            } else if (Utility.fieldEquals(ctx.f.getWsValCh(), ".")) {
                ctx.f.setWsDotCnt(ctx.f.getWsDotCnt() + 1);
            } else if (Utility.fieldEquals(ctx.f.getWsValCh(), ",")) {
                /* CONTINUE */
            } else if (Utility.fieldEquals(ctx.f.getWsValCh(), "-")) {
                /* CONTINUE */
            } else if (Utility.fieldEquals(ctx.f.getWsValCh(), "+")) {
                /* CONTINUE */
            } else {
                ctx.f.setString("WS-VAL-OK", "N");
            }
        }
        if (ctx.f.getWsValDigits() == 0) {
            ctx.f.setString("WS-VAL-OK", "N");
        }
        if (ctx.f.getWsDotCnt() > 1) {
            ctx.f.setString("WS-VAL-OK", "N");
        }
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
                "MTRANAA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
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

    /** Strip COBOL low-values padding from a field, replacing it with a blank. */
    private static String stripLowValues(String value) {
        return value.replace(RuntimeConstants.LOW_VALUES, " ");
    }

    /** Read a master/xref file by key and set WS-FOUND-FLG based on the response code. */
    private void readAndSetFoundFlag(TaskContext ctx, String file, String key) {
        ctx.f.setString("WS-FOUND-FLG", "N");
        ctx.appService.readFile(file, ctx.f, key, 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-FOUND-FLG", "Y");
        }
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OctranaFields f;

        final AppService appService;

        final OctranaLinkParm link = new OctranaLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OctranaFields(ws);
        }
    }
}

package com.generated.orion.octrnin.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OctrninLinkParm;
import com.generated.orion.octrnin.accessor.OctrninFields;
import com.generated.orion.octrnin.metadata.OctrninBmsMetadata;
import com.generated.orion.octrnin.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCTRNIN. */
@Service
public class OctrninService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OctrninService.class);

    private static final int FIELD_INPUT_LEN = 256;

    private static final int COMMAREA_LEN = 692;

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
        return "OCTRNIN";
    }

    @Override
    public String getTransId() {
        return "ORQT";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OctrninBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OctrninBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OctrninBmsMetadata.getFieldMapping(mapName);
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
                processScreenInput(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MTRNINAO");
        populateScreenHeader(ctx);
        clearTransactionRows(ctx);
        clearSummaryFields(ctx);
        ctx.f.setKtrninArea("");
        ctx.f.setString("KTI-FILTER", "C");
        ctx.f.fillLowValues("KTI-START-KEY");
        ctx.f.setCaWorkArea(ctx.f.getKtiRequest());
        ctx.f.setTmodeo("C");
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setTmodel((short) -1);
        ctx.appService.sendMap(
                "MTRNINA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processScreenInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenuProgram(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "7")) {
            restartTransactionList(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "8")) {
            pageNextTransactions(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            applyTransactionFilter(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-APPLY-FILTER */
    private void applyTransactionFilter(TaskContext ctx) {
        ctx.appService.receiveMap("MTRNINA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MTRNINAI");
        }
        parseFilterMode(ctx);
        if (isFilterInvalid(ctx)) {
            ctx.f.setTmodel((short) -1);
            sendDataOnlyScreen(ctx);
            return;
        }
        ctx.f.fillLowValues("KTI-START-KEY");
        callTransactionSubprogram(ctx);
        showTransactionPage(ctx);
    }

    /** COBOL paragraph: 2200-PAGE-NEXT */
    private void pageNextTransactions(TaskContext ctx) {
        ctx.f.setKtiRequest(String.valueOf(ctx.f.getCaWorkArea()));
        callTransactionSubprogram(ctx);
        if (ctx.f.getKtiRowCount() == 0) {
            populateSummaryFields(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMEndList());
            ctx.f.setTmodel((short) -1);
            sendDataOnlyScreen(ctx);
        } else {
            showTransactionPage(ctx);
        }
    }

    /** COBOL paragraph: 2300-RESTART */
    private void restartTransactionList(TaskContext ctx) {
        ctx.f.setKtiRequest(String.valueOf(ctx.f.getCaWorkArea()));
        ctx.f.fillLowValues("KTI-START-KEY");
        callTransactionSubprogram(ctx);
        showTransactionPage(ctx);
    }

    /** COBOL paragraph: 3000-CALL-SUB */
    private void callTransactionSubprogram(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaToProgram(ctx.f.getWsSubPgm());
        {
            byte[] _linkCa = ctx.f.sliceBytes("KTRNIN-AREA");
            ctx.appService.link(ctx.f.getWsSubPgm(), _linkCa, 845);
            ctx.f.writeBytes("KTRNIN-AREA", _linkCa);
        }
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setString("KTI-RETURN-CD", "E");
            ctx.f.setCaErrMsg(ctx.f.getWsMLinkErr());
        }
        if (ctx.f.getKtiRowCount() > 0) {
            ctx.f.setKtiStartKey(ctx.f.getKtiNextKey());
        }
        ctx.f.setCaWorkArea(ctx.f.getKtiRequest());
    }

    /** COBOL paragraph: 4000-SHOW-PAGE */
    private void showTransactionPage(TaskContext ctx) {
        clearTransactionRows(ctx);
        if (ctx.f.getKtiReturnCd().equals("E")) {
            clearSummaryFields(ctx);
            ctx.f.setErrmsgo(ctx.f.getCaErrMsg());
            ctx.f.setTmodel((short) -1);
            sendDataOnlyScreen(ctx);
            return;
        }
        populateSummaryFields(ctx);
        if (ctx.f.getKtiRowCount() == 0) {
            ctx.f.setErrmsgo(ctx.f.getWsMNoneFound());
        } else {
            for (ctx.f.setWsRowIdx(1);
                    ctx.f.getWsRowIdx() <= ctx.f.getKtiRowCount();
                    ctx.f.setWsRowIdx(ctx.f.getWsRowIdx() + 1)) {
                placeTransactionRow(ctx);
            }
            buildStatusMessage(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgLine());
        }
        ctx.f.setTmodel((short) -1);
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 4100-CLEAR-ROWS */
    private void clearTransactionRows(TaskContext ctx) {
        ctx.f.setTid1o(" ");
        ctx.f.setTcd1o(" ");
        ctx.f.setTtc1o(" ");
        ctx.f.setTds1o(" ");
        ctx.f.setTmc1o(" ");
        ctx.f.setTam1o(" ");
        ctx.f.setTdt1o(" ");
        ctx.f.setTid2o(" ");
        ctx.f.setTcd2o(" ");
        ctx.f.setTtc2o(" ");
        ctx.f.setTds2o(" ");
        ctx.f.setTmc2o(" ");
        ctx.f.setTam2o(" ");
        ctx.f.setTdt2o(" ");
        ctx.f.setTid3o(" ");
        ctx.f.setTcd3o(" ");
        ctx.f.setTtc3o(" ");
        ctx.f.setTds3o(" ");
        ctx.f.setTmc3o(" ");
        ctx.f.setTam3o(" ");
        ctx.f.setTdt3o(" ");
        ctx.f.setTid4o(" ");
        ctx.f.setTcd4o(" ");
        ctx.f.setTtc4o(" ");
        ctx.f.setTds4o(" ");
        ctx.f.setTmc4o(" ");
        ctx.f.setTam4o(" ");
        ctx.f.setTdt4o(" ");
        ctx.f.setTid5o(" ");
        ctx.f.setTcd5o(" ");
        ctx.f.setTtc5o(" ");
        ctx.f.setTds5o(" ");
        ctx.f.setTmc5o(" ");
        ctx.f.setTam5o(" ");
        ctx.f.setTdt5o(" ");
        ctx.f.setTid6o(" ");
        ctx.f.setTcd6o(" ");
        ctx.f.setTtc6o(" ");
        ctx.f.setTds6o(" ");
        ctx.f.setTmc6o(" ");
        ctx.f.setTam6o(" ");
        ctx.f.setTdt6o(" ");
    }

    /** COBOL paragraph: 4200-PLACE-ROW */
    private void placeTransactionRow(TaskContext ctx) {
        ctx.f.setWsEdAmt(ctx.f.getKtrAmt(ctx.f.getWsRowIdx()));
        String formattedAmt =
                Utility.padRight(String.valueOf(ctx.f.getString("WS-ED-AMT")), 16).substring(1, 16);
        switch (ctx.f.getWsRowIdx()) {
            case 1 -> {
                ctx.f.setTid1o(ctx.f.getKtrId(1));
                ctx.f.setTcd1o(
                        Utility.padRight(String.valueOf(ctx.f.getKtrCard(1)), 16)
                                .substring(12, 16));
                ctx.f.setTtc1o(ctx.f.getKtrTycat(1));
                ctx.f.setTds1o(ctx.f.getKtrDesc(1));
                ctx.f.setTmc1o(ctx.f.getKtrMerch(1));
                ctx.f.setTam1o(formattedAmt);
                ctx.f.setTdt1o(ctx.f.getKtrDate(1));
            }
            case 2 -> {
                ctx.f.setTid2o(ctx.f.getKtrId(2));
                ctx.f.setTcd2o(
                        Utility.padRight(String.valueOf(ctx.f.getKtrCard(2)), 16)
                                .substring(12, 16));
                ctx.f.setTtc2o(ctx.f.getKtrTycat(2));
                ctx.f.setTds2o(ctx.f.getKtrDesc(2));
                ctx.f.setTmc2o(ctx.f.getKtrMerch(2));
                ctx.f.setTam2o(formattedAmt);
                ctx.f.setTdt2o(ctx.f.getKtrDate(2));
            }
            case 3 -> {
                ctx.f.setTid3o(ctx.f.getKtrId(3));
                ctx.f.setTcd3o(
                        Utility.padRight(String.valueOf(ctx.f.getKtrCard(3)), 16)
                                .substring(12, 16));
                ctx.f.setTtc3o(ctx.f.getKtrTycat(3));
                ctx.f.setTds3o(ctx.f.getKtrDesc(3));
                ctx.f.setTmc3o(ctx.f.getKtrMerch(3));
                ctx.f.setTam3o(formattedAmt);
                ctx.f.setTdt3o(ctx.f.getKtrDate(3));
            }
            case 4 -> {
                ctx.f.setTid4o(ctx.f.getKtrId(4));
                ctx.f.setTcd4o(
                        Utility.padRight(String.valueOf(ctx.f.getKtrCard(4)), 16)
                                .substring(12, 16));
                ctx.f.setTtc4o(ctx.f.getKtrTycat(4));
                ctx.f.setTds4o(ctx.f.getKtrDesc(4));
                ctx.f.setTmc4o(ctx.f.getKtrMerch(4));
                ctx.f.setTam4o(formattedAmt);
                ctx.f.setTdt4o(ctx.f.getKtrDate(4));
            }
            case 5 -> {
                ctx.f.setTid5o(ctx.f.getKtrId(5));
                ctx.f.setTcd5o(
                        Utility.padRight(String.valueOf(ctx.f.getKtrCard(5)), 16)
                                .substring(12, 16));
                ctx.f.setTtc5o(ctx.f.getKtrTycat(5));
                ctx.f.setTds5o(ctx.f.getKtrDesc(5));
                ctx.f.setTmc5o(ctx.f.getKtrMerch(5));
                ctx.f.setTam5o(formattedAmt);
                ctx.f.setTdt5o(ctx.f.getKtrDate(5));
            }
            case 6 -> {
                ctx.f.setTid6o(ctx.f.getKtrId(6));
                ctx.f.setTcd6o(
                        Utility.padRight(String.valueOf(ctx.f.getKtrCard(6)), 16)
                                .substring(12, 16));
                ctx.f.setTtc6o(ctx.f.getKtrTycat(6));
                ctx.f.setTds6o(ctx.f.getKtrDesc(6));
                ctx.f.setTmc6o(ctx.f.getKtrMerch(6));
                ctx.f.setTam6o(formattedAmt);
                ctx.f.setTdt6o(ctx.f.getKtrDate(6));
            }
            default -> {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: 4400-SHOW-SUMMARY */
    private void populateSummaryFields(TaskContext ctx) {
        ctx.f.setWsCntEd(ctx.f.getKtiMatchCount());
        ctx.f.setMcnto(ctx.f.getString("WS-CNT-ED"));
        ctx.f.setWsEdSum(ctx.f.getKtiNetTotal());
        ctx.f.setMtoto(ctx.f.getString("WS-ED-SUM"));
        ctx.f.setWsCnt7(ctx.f.getKtiPurchCnt());
        ctx.f.setPcnto(ctx.f.getString("WS-CNT7"));
        ctx.f.setWsEdSum(ctx.f.getKtiPurchSum());
        ctx.f.setPsumo(ctx.f.getString("WS-ED-SUM"));
        ctx.f.setWsCnt7(ctx.f.getKtiPayCnt());
        ctx.f.setYcnto(ctx.f.getString("WS-CNT7"));
        ctx.f.setWsEdSum(ctx.f.getKtiPaySum());
        ctx.f.setYsumo(ctx.f.getString("WS-ED-SUM"));
        ctx.f.setWsCnt7(ctx.f.getKtiFeeCnt());
        ctx.f.setFcnto(ctx.f.getString("WS-CNT7"));
        ctx.f.setWsEdSum(ctx.f.getKtiFeeSum());
        ctx.f.setFsumo(ctx.f.getString("WS-ED-SUM"));
        ctx.f.setWsCnt7(ctx.f.getKtiIntCnt());
        ctx.f.setIcnto(ctx.f.getString("WS-CNT7"));
        ctx.f.setWsEdSum(ctx.f.getKtiIntSum());
        ctx.f.setIsumo(ctx.f.getString("WS-ED-SUM"));
        ctx.f.setXido(ctx.f.getKtiMaxId());
        ctx.f.setWsEdSum(ctx.f.getKtiMaxAmt());
        ctx.f.setXamto(ctx.f.getString("WS-ED-SUM"));
    }

    /** COBOL paragraph: 4500-CLEAR-SUMMARY */
    private void clearSummaryFields(TaskContext ctx) {
        ctx.f.setMcnto(" ");
        ctx.f.setMtoto(" ");
        ctx.f.setPcnto(" ");
        ctx.f.setPsumo(" ");
        ctx.f.setYcnto(" ");
        ctx.f.setYsumo(" ");
        ctx.f.setFcnto(" ");
        ctx.f.setFsumo(" ");
        ctx.f.setIcnto(" ");
        ctx.f.setIsumo(" ");
        ctx.f.setXido(" ");
        ctx.f.setXamto(" ");
    }

    /** COBOL paragraph: 4600-BUILD-MSG */
    private void buildStatusMessage(TaskContext ctx) {
        ctx.f.setWsCntEd(ctx.f.getKtiMatchCount());
        ctx.f.setWsMsgLine(" ");
        if (ctx.f.getKtiMoreSw().equals("Y")) {
            {
                StringBuilder sb = new StringBuilder();
                sb.append(String.valueOf(ctx.f.getString("WS-CNT-ED")));
                sb.append(String.valueOf(" matching tran(s).  PF8=more PF7=top"));
                ctx.f.setWsMsgLine(sb.toString());
            }
        } else {
            {
                StringBuilder sb = new StringBuilder();
                sb.append(String.valueOf(ctx.f.getString("WS-CNT-ED")));
                sb.append(String.valueOf(" matching tran(s).  End of list."));
                ctx.f.setWsMsgLine(sb.toString());
            }
        }
    }

    /** COBOL paragraph: 5000-PARSE-FILTER */
    private void parseFilterMode(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsMode(ctx.f.getTmodei());
        ctx.f.setWsMode(
                String.valueOf(ctx.f.getWsMode())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        ctx.f.setKtrninArea("");
        switch (Utility.rtrim(ctx.f.getWsMode())) {
            case "C" -> {
                parseCardFilter(ctx);
            }
            case "D" -> {
                parseDateFilter(ctx);
            }
            case "M" -> {
                parseMerchantFilter(ctx);
            }
            case "T" -> {
                parseTypeCategoryFilter(ctx);
            }
            case "A" -> {
                parseAmountFilter(ctx);
            }
            default -> {
                ctx.f.setErrmsgo(ctx.f.getWsMBadMode());
                ctx.f.setString("WS-VALID-SW", "N");
            }
        }
    }

    /** COBOL paragraph: 5100-PARSE-CARD */
    private void parseCardFilter(TaskContext ctx) {
        ctx.f.setString("KTI-FILTER", "C");
        ctx.f.setKtiCard(ctx.f.getFcardi());
        ctx.f.setKtiCard(
                String.valueOf(ctx.f.getKtiCard())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getKtiCard(), " ")) {
            ctx.f.setErrmsgo(ctx.f.getWsMCardReq());
            ctx.f.setString("WS-VALID-SW", "N");
        }
    }

    /** COBOL paragraph: 5200-PARSE-DATE */
    private void parseDateFilter(TaskContext ctx) {
        ctx.f.setString("KTI-FILTER", "D");
        ctx.f.setKtiDateFrom(ctx.f.getFrdatei());
        ctx.f.setKtiDateFrom(
                String.valueOf(ctx.f.getKtiDateFrom())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        ctx.f.setKtiDateTo(ctx.f.getFtdatei());
        ctx.f.setKtiDateTo(
                String.valueOf(ctx.f.getKtiDateTo())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getKtiDateFrom(), " ")) {
            ctx.f.setKtiDateFrom("0000-00-00");
        }
        if (Utility.fieldEquals(ctx.f.getKtiDateTo(), " ")) {
            ctx.f.setKtiDateTo("9999-99-99");
        }
    }

    /** COBOL paragraph: 5300-PARSE-MERCH */
    private void parseMerchantFilter(TaskContext ctx) {
        ctx.f.setString("KTI-FILTER", "M");
        ctx.f.setWsNcIn(ctx.f.getFmerchi());
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getWsNcIn(), " ")) {
            ctx.f.setErrmsgo(ctx.f.getWsMMerchReq());
            ctx.f.setString("WS-VALID-SW", "N");
            return;
        }
        ctx.f.setWsNcLen(9);
        parseNumericField(ctx);
        if (isFilterInvalid(ctx)) {
            ctx.f.setErrmsgo(ctx.f.getWsMMerchReq());
        } else {
            ctx.f.setKtiMerchId(ctx.f.getWsNcValue());
        }
    }

    /** COBOL paragraph: 5400-PARSE-TYPECAT */
    private void parseTypeCategoryFilter(TaskContext ctx) {
        ctx.f.setString("KTI-FILTER", "T");
        ctx.f.setKtiFType(ctx.f.getFtypei());
        ctx.f.setKtiFType(
                String.valueOf(ctx.f.getKtiFType())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getKtiFType(), " ")) {
            ctx.f.setErrmsgo(ctx.f.getWsMTypeReq());
            ctx.f.setString("WS-VALID-SW", "N");
            return;
        }
        ctx.f.setKtiFCat(0);
        ctx.f.setWsNcIn(ctx.f.getFcati());
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (!Utility.fieldEquals(ctx.f.getWsNcIn(), " ")) {
            ctx.f.setWsNcLen(4);
            parseNumericField(ctx);
            if (isFilterInvalid(ctx)) {
                ctx.f.setErrmsgo(ctx.f.getWsMBadNum());
            } else {
                ctx.f.setKtiFCat(ctx.f.getWsNcValue());
            }
        }
    }

    /** COBOL paragraph: 5500-PARSE-AMOUNT */
    private void parseAmountFilter(TaskContext ctx) {
        ctx.f.setString("KTI-FILTER", "A");
        ctx.f.setKtiAmtThresh(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsAmIn(ctx.f.getFamti());
        ctx.f.setWsAmIn(
                String.valueOf(ctx.f.getWsAmIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (!Utility.fieldEquals(ctx.f.getWsAmIn(), " ")) {
            parseAmountValue(ctx);
            if (isFilterInvalid(ctx)) {
                ctx.f.setErrmsgo(ctx.f.getWsMBadAmt());
            } else {
                ctx.f.setKtiAmtThresh(ctx.f.getWsAmValue());
            }
        }
    }

    /** COBOL paragraph: 6000-PARSE-NUM */
    private void parseNumericField(TaskContext ctx) {
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
                    Utility.padRight(String.valueOf(ctx.f.getWsNcIn()), FIELD_INPUT_LEN)
                            .substring(ctx.f.getWsNcPos() - 1, ctx.f.getWsNcPos() - 1 + 1));
            if (Utility.fieldEquals(ctx.f.getWsNcChar(), " ")) {
                /* CONTINUE */
            } else if ((ctx.f.getWsNcChar().compareTo("0") >= 0)
                    && (ctx.f.getWsNcChar().compareTo("9") <= 0)) {
                ctx.f.setWsNcDigit(Utility.parseNumeric(ctx.f.getWsNcChar()).intValue());
                ctx.f.setWsNcValue(((ctx.f.getWsNcValue() * 10) + ctx.f.getWsNcDigit()));
                ctx.f.setWsNcDigits(ctx.f.getWsNcDigits() + 1);
            } else {
                ctx.f.setString("WS-VALID-SW", "N");
            }
        }
        if (ctx.f.getWsNcDigits() == 0) {
            ctx.f.setString("WS-VALID-SW", "N");
        }
    }

    /** COBOL paragraph: 6500-PARSE-AMT */
    private void parseAmountValue(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsAmInt(0);
        ctx.f.setWsAmDec(0);
        ctx.f.setWsAmDeccnt(0);
        ctx.f.setWsAmDigcnt(0);
        ctx.f.setWsAmDotSw("N");
        ctx.f.setWsAmIn(
                String.valueOf(ctx.f.getWsAmIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        for (ctx.f.setWsAmPos(1);
                ctx.f.getWsAmPos() <= 12;
                ctx.f.setWsAmPos(ctx.f.getWsAmPos() + 1)) {
            ctx.f.setWsAmChar(
                    Utility.padRight(String.valueOf(ctx.f.getWsAmIn()), FIELD_INPUT_LEN)
                            .substring(ctx.f.getWsAmPos() - 1, ctx.f.getWsAmPos() - 1 + 1));
            if (Utility.fieldEquals(ctx.f.getWsAmChar(), " ")) {
                /* CONTINUE */
            } else if (Utility.fieldEquals(ctx.f.getWsAmChar(), ",")) {
                /* CONTINUE */
            } else if (Utility.fieldEquals(ctx.f.getWsAmChar(), ".")) {
                if (ctx.f.getWsAmDotSw().equals("Y")) {
                    ctx.f.setString("WS-VALID-SW", "N");
                } else {
                    ctx.f.setString("WS-AM-DOT-SW", "Y");
                }
            } else if ((ctx.f.getWsAmChar().compareTo("0") >= 0)
                    && (ctx.f.getWsAmChar().compareTo("9") <= 0)) {
                ctx.f.setWsAmDigit(Utility.parseNumeric(ctx.f.getWsAmChar()).intValue());
                ctx.f.setWsAmDigcnt(ctx.f.getWsAmDigcnt() + 1);
                if (ctx.f.getWsAmDotSw().equals("Y")) {
                    if (ctx.f.getWsAmDeccnt() < 2) {
                        ctx.f.setWsAmDec(((ctx.f.getWsAmDec() * 10) + ctx.f.getWsAmDigit()));
                        ctx.f.setWsAmDeccnt(ctx.f.getWsAmDeccnt() + 1);
                    }
                } else {
                    ctx.f.setWsAmInt(((ctx.f.getWsAmInt() * 10) + ctx.f.getWsAmDigit()));
                }
            } else {
                ctx.f.setString("WS-VALID-SW", "N");
            }
        }
        if (ctx.f.getWsAmDigcnt() == 0) {
            ctx.f.setString("WS-VALID-SW", "N");
        }
        if (ctx.f.getWsAmDotSw().equals("Y") && ctx.f.getWsAmDeccnt() == 1) {
            ctx.f.setWsAmDec((ctx.f.getWsAmDec() * 10));
        }
        ctx.f.setWsAbInt(ctx.f.getWsAmInt());
        ctx.f.setWsAbDec(ctx.f.getWsAmDec());
        ctx.f.setWsAmValue(ctx.f.getWsAmNum());
    }

    /** COBOL paragraph: 7000-XCTL-MENU */
    private void transferToMenuProgram(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LEN);
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
                "MTRNINA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
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

    /** COBOL paragraph: 9000-RETURN */
    private void returnToCics(TaskContext ctx) {
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LEN);
    }

    /** True when the last filter-parsing step marked the input as invalid. */
    private boolean isFilterInvalid(TaskContext ctx) {
        return ctx.f.getWsValidSw().equals("N");
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OctrninFields f;

        final AppService appService;

        final OctrninLinkParm link = new OctrninLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OctrninFields(ws);
        }
    }
}

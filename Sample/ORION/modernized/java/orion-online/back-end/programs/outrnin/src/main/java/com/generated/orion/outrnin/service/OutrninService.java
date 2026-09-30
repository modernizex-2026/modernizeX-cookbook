package com.generated.orion.outrnin.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.outrnin.accessor.OutrninFields;
import com.generated.orion.outrnin.metadata.OutrninBmsMetadata;
import com.generated.orion.outrnin.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUTRNIN. */
@Service
public class OutrninService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OutrninService.class);

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        if (ctx.appService.getEibcalen() > 0 && ctx.appService.getCommarea() != null) {
            Object _commarea = ctx.appService.getCommarea();
            if (_commarea instanceof Object[]) {
                Object[] _params = (Object[]) _commarea;
                if (_params.length > 0 && _params[0] instanceof byte[]) {
                    ctx.f.writeBytes("KTRNIN-AREA", (byte[]) _params[0]);
                } else if (_params.length > 0 && _params[0] != null) {
                    ctx.f.setGroup("KTRNIN-AREA", String.valueOf(_params[0]));
                }
            } else if (_commarea instanceof byte[]) {
                ctx.f.writeBytes("KTRNIN-AREA", (byte[]) _commarea);
            } else {
                ctx.f.setGroup("KTRNIN-AREA", String.valueOf(_commarea));
            }
        }
        try {
            runMainProgram(ctx);
        } finally {
            if (ctx.appService.getEibcalen() > 0) {
                if (ctx.appService.getCommarea() instanceof Object[]) {
                    Object[] _params = (Object[]) ctx.appService.getCommarea();
                    if (_params.length > 0) {
                        _params[0] =
                                _params[0] instanceof byte[]
                                        ? (Object) ctx.f.sliceBytes("KTRNIN-AREA")
                                        : (Object) ctx.f.groupToString("KTRNIN-AREA");
                    }
                } else {
                    ctx.appService.setCommarea(ctx.f.sliceBytes("KTRNIN-AREA"));
                }
            }
        }
    }

    @Override
    public String getProgramName() {
        return "OUTRNIN";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OutrninBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OutrninBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        initializeResponse(ctx);
        browseTransactions(ctx);
        finalizeProgram(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INIT-RESPONSE */
    private void initializeResponse(TaskContext ctx) {
        ctx.f.setString("KTI-RETURN-CD", "N");
        ctx.f.setKtiRowCount(0);
        ctx.f.setKtiScanCount(0);
        ctx.f.setKtiMatchCount(0);
        ctx.f.setKtiNetTotal(java.math.BigDecimal.valueOf(0));
        ctx.f.setKtiMaxAmt(java.math.BigDecimal.valueOf(0));
        ctx.f.setKtiMaxId(" ");
        ctx.f.setKtiPurchCnt(0);
        ctx.f.setKtiPurchSum(java.math.BigDecimal.valueOf(0));
        ctx.f.setKtiPayCnt(0);
        ctx.f.setKtiPaySum(java.math.BigDecimal.valueOf(0));
        ctx.f.setKtiFeeCnt(0);
        ctx.f.setKtiFeeSum(java.math.BigDecimal.valueOf(0));
        ctx.f.setKtiIntCnt(0);
        ctx.f.setKtiIntSum(java.math.BigDecimal.valueOf(0));
        ctx.f.setKtiNextKey(" ");
        ctx.f.setString("KTI-MORE-SW", "N");
        ctx.f.setWsLastKey(" ");
        ctx.f.setString("WS-FIRST-SW", "Y");
        ctx.f.setString("WS-MATCH-SW", "N");
        ctx.f.setWsEndFlg("N");
        ctx.f.setKtiRows("");
    }

    /** COBOL paragraph: 2000-BROWSE-TRANS */
    private void browseTransactions(TaskContext ctx) {
        startTransBrowse(ctx);
        if (!ctx.f.getWsEndFlg().equals("Y")) {
            while (!ctx.f.getWsEndFlg().equals("Y")) {
                readNextTransaction(ctx);
            }
            endTransBrowse(ctx);
        }
        if (ctx.f.getKtiRowCount() > 0) {
            ctx.f.setKtiNextKey(ctx.f.getWsLastKey());
        }
    }

    /** COBOL paragraph: 2100-START-BROWSE */
    private void startTransBrowse(TaskContext ctx) {
        ctx.f.fillLowValues("TR-ID");
        ctx.appService.startBrowse(ctx.f.getWsTranfile(), String.valueOf(ctx.f.getTrId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            /* CONTINUE */
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-END-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-END-FLG", "Y");
        } else {
            ctx.f.setString("WS-END-FLG", "Y");
            ctx.f.setString("KTI-RETURN-CD", "E");
        }
    }

    /** COBOL paragraph: 2200-READ-NEXT */
    private void readNextTransaction(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsTranfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            handleRecord(ctx);
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-END-FLG", "Y");
        } else {
            ctx.f.setString("WS-END-FLG", "Y");
            ctx.f.setString("KTI-RETURN-CD", "E");
        }
    }

    /** COBOL paragraph: 2250-HANDLE-RECORD */
    private void handleRecord(TaskContext ctx) {
        ctx.f.setKtiScanCount(ctx.f.getKtiScanCount() + 1);
        checkFilterMatch(ctx);
        if (ctx.f.getWsMatchSw().equals("Y")) {
            accumulateTotals(ctx);
            if ((ctx.f.getTrId().compareTo(ctx.f.getKtiStartKey()) > 0)) {
                if (ctx.f.getKtiRowCount() < ctx.f.getWsMaxRows()) {
                    storeRow(ctx);
                } else {
                    ctx.f.setString("KTI-MORE-SW", "Y");
                }
            }
        }
    }

    /** COBOL paragraph: 2300-END-BROWSE */
    private void endTransBrowse(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsTranfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 2400-FILTER-CHECK */
    private void checkFilterMatch(TaskContext ctx) {
        ctx.f.setString("WS-MATCH-SW", "N");
        if (ctx.f.getKtiFilter().equals("C")) {
            if (Utility.fieldEquals(ctx.f.getTrCardNum(), ctx.f.getKtiCard())) {
                ctx.f.setString("WS-MATCH-SW", "Y");
            }
        } else if (ctx.f.getKtiFilter().equals("D")) {
            if ((Utility.padRight(String.valueOf(ctx.f.getTrProcTs()), 10)
                                    .substring(0, 10)
                                    .compareTo(ctx.f.getKtiDateFrom())
                            >= 0)
                    && (Utility.padRight(String.valueOf(ctx.f.getTrProcTs()), 10)
                                    .substring(0, 10)
                                    .compareTo(ctx.f.getKtiDateTo())
                            <= 0)) {
                ctx.f.setString("WS-MATCH-SW", "Y");
            }
        } else if (ctx.f.getKtiFilter().equals("M")) {
            if (ctx.f.getTrMerchantId() == ctx.f.getKtiMerchId()) {
                ctx.f.setString("WS-MATCH-SW", "Y");
            }
        } else if (ctx.f.getKtiFilter().equals("T")) {
            if (Utility.fieldEquals(ctx.f.getTrTypeCd(), ctx.f.getKtiFType())
                    && ctx.f.getTrCatCd() == ctx.f.getKtiFCat()) {
                ctx.f.setString("WS-MATCH-SW", "Y");
            }
        } else if (ctx.f.getKtiFilter().equals("A")) {
            if ((ctx.f.getTrAmt().compareTo(ctx.f.getKtiAmtThresh()) >= 0)) {
                ctx.f.setString("WS-MATCH-SW", "Y");
            }
        } else {
            /* CONTINUE */
        }
    }

    /** COBOL paragraph: 2500-STORE-ROW */
    private void storeRow(TaskContext ctx) {
        ctx.f.setKtiRowCount(ctx.f.getKtiRowCount() + 1);
        ctx.f.setWsLastKey(ctx.f.getTrId());
        lookupDescription(ctx);
        ctx.f.setKtrId(ctx.f.getKtiRowCount(), ctx.f.getTrId());
        ctx.f.setKtrCard(ctx.f.getKtiRowCount(), ctx.f.getTrCardNum());
        ctx.f.setKtrTycat(ctx.f.getKtiRowCount(), ctx.f.getWsTycat());
        ctx.f.setKtrDesc(ctx.f.getKtiRowCount(), ctx.f.getWsDesc());
        ctx.f.setKtrMerch(ctx.f.getKtiRowCount(), ctx.f.getTrMerchantName());
        ctx.f.setKtrAmt(ctx.f.getKtiRowCount(), ctx.f.getTrAmt());
        ctx.f.setKtrDate(
                ctx.f.getKtiRowCount(),
                Utility.padRight(String.valueOf(ctx.f.getTrProcTs()), 10).substring(0, 10));
    }

    /** COBOL paragraph: 2600-LOOKUP-DESC */
    private void lookupDescription(TaskContext ctx) {
        ctx.f.setWsCatEd(ctx.f.getTrCatCd());
        ctx.f.setWsTycat(" ");
        ctx.f.setWsTycat(
                String.valueOf(ctx.f.getTrTypeCd()) + "/" + String.valueOf(ctx.f.getWsCatEd()));
        ctx.f.setWsDesc(" ");
        ctx.f.setTcTypeCd(ctx.f.getTrTypeCd());
        ctx.f.setTcCd(ctx.f.getTrCatCd());
        ctx.appService.readFile(
                ctx.f.getWsTcatfile(),
                ctx.f,
                String.valueOf(ctx.f.getTcTypeCd()) + "|" + String.valueOf(ctx.f.getTcCd()),
                0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setWsDesc(ctx.f.getTcDesc());
        } else {
            lookupTransactionType(ctx);
        }
    }

    /** COBOL paragraph: 2650-LOOKUP-TYPE */
    private void lookupTransactionType(TaskContext ctx) {
        ctx.f.setTtCd(ctx.f.getTrTypeCd());
        ctx.appService.readFile(ctx.f.getWsTtypfile(), ctx.f, String.valueOf(ctx.f.getTtCd()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setWsDesc(ctx.f.getTtDesc());
        } else {
            ctx.f.setWsDesc("UNKNOWN");
        }
    }

    /** COBOL paragraph: 2700-ACCUMULATE */
    private void accumulateTotals(TaskContext ctx) {
        ctx.f.setKtiMatchCount(ctx.f.getKtiMatchCount() + 1);
        ctx.f.setKtiNetTotal(ctx.f.getKtiNetTotal().add(ctx.f.getTrAmt()));
        switch (Utility.rtrim(ctx.f.getTrTypeCd())) {
            case "PU" -> {
                ctx.f.setKtiPurchCnt(ctx.f.getKtiPurchCnt() + 1);
                ctx.f.setKtiPurchSum(ctx.f.getKtiPurchSum().add(ctx.f.getTrAmt()));
            }
            case "PY" -> {
                ctx.f.setKtiPayCnt(ctx.f.getKtiPayCnt() + 1);
                ctx.f.setKtiPaySum(ctx.f.getKtiPaySum().add(ctx.f.getTrAmt()));
            }
            case "FE" -> {
                ctx.f.setKtiFeeCnt(ctx.f.getKtiFeeCnt() + 1);
                ctx.f.setKtiFeeSum(ctx.f.getKtiFeeSum().add(ctx.f.getTrAmt()));
            }
            case "IN" -> {
                ctx.f.setKtiIntCnt(ctx.f.getKtiIntCnt() + 1);
                ctx.f.setKtiIntSum(ctx.f.getKtiIntSum().add(ctx.f.getTrAmt()));
            }
            default -> {
                /* CONTINUE */
            }
        }
        if (ctx.f.getWsFirstSw().equals("Y")) {
            ctx.f.setKtiMaxAmt(ctx.f.getTrAmt());
            ctx.f.setKtiMaxId(ctx.f.getTrId());
            ctx.f.setString("WS-FIRST-SW", "N");
        } else {
            if ((ctx.f.getTrAmt().compareTo(ctx.f.getKtiMaxAmt()) > 0)) {
                ctx.f.setKtiMaxAmt(ctx.f.getTrAmt());
                ctx.f.setKtiMaxId(ctx.f.getTrId());
            }
        }
    }

    /** COBOL paragraph: 3000-FINALIZE */
    private void finalizeProgram(TaskContext ctx) {
        /* CONTINUE */
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OutrninFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OutrninFields(ws);
        }
    }
}

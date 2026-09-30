package com.generated.orion.ouanlin.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.ouanlin.accessor.OuanlinFields;
import com.generated.orion.ouanlin.metadata.OuanlinBmsMetadata;
import com.generated.orion.ouanlin.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUANLIN. */
@Service
public class OuanlinService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OuanlinService.class);

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
                    ctx.f.writeBytes("KANLB-PARM", (byte[]) _params[0]);
                } else if (_params.length > 0 && _params[0] != null) {
                    ctx.f.setGroup("KANLB-PARM", String.valueOf(_params[0]));
                }
            } else if (_commarea instanceof byte[]) {
                ctx.f.writeBytes("KANLB-PARM", (byte[]) _commarea);
            } else {
                ctx.f.setGroup("KANLB-PARM", String.valueOf(_commarea));
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
                                        ? (Object) ctx.f.sliceBytes("KANLB-PARM")
                                        : (Object) ctx.f.groupToString("KANLB-PARM");
                    }
                } else {
                    ctx.appService.setCommarea(ctx.f.sliceBytes("KANLB-PARM"));
                }
            }
        }
    }

    @Override
    public String getProgramName() {
        return "OUANLIN";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OuanlinBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OuanlinBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        initializeWorkingStorage(ctx);
        if (!Utility.fieldEquals(ctx.f.getKabStatus(), "99")) {
            scanTransactionFile(ctx);
            finalizeResults(ctx);
            buildResultPage(ctx);
        }
        updateResultStatus(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INIT */
    private void initializeWorkingStorage(TaskContext ctx) {
        ctx.f.setWsResCnt(0);
        ctx.f.setWsScanned(0);
        ctx.f.setWsDiscCnt(0);
        ctx.f.setWsOflowCnt(0);
        ctx.f.setWsOrphCnt(0);
        ctx.f.setWsTot1(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsTot2(java.math.BigDecimal.valueOf(0));
        ctx.f.setString("WS-BROWSE-SW", "N");
        ctx.f.setString("WS-EOF-SW", "N");
        ctx.f.setKabStatus("00");
        ctx.f.setWsMaxRows(ctx.f.getKabMaxRows());
        if (ctx.f.getWsMaxRows() < 1 || ctx.f.getWsMaxRows() > 6) {
            ctx.f.setWsMaxRows(6);
        }
        switch (Utility.rtrim(ctx.f.getKabMode())) {
            case "RW", "FR", "GL", "RC" -> {
                /* CONTINUE */
            }
            default -> {
                ctx.f.setKabStatus("99");
            }
        }
    }

    /** COBOL paragraph: 2000-SCAN-FILE */
    private void scanTransactionFile(TaskContext ctx) {
        startTransactionBrowse(ctx);
        if (ctx.f.getWsBrowseSw().equals("Y")) {
            while (!ctx.f.getWsEofSw().equals("Y") && ctx.f.getWsScanned() < ctx.f.getWsMaxScan()) {
                readNextTransaction(ctx);
            }
            endTransactionBrowse(ctx);
        }
    }

    /** COBOL paragraph: 2100-START-TRAN */
    private void startTransactionBrowse(TaskContext ctx) {
        ctx.f.fillLowValues("TR-ID");
        ctx.appService.startBrowse(ctx.f.getWsTranfile(), String.valueOf(ctx.f.getTrId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BROWSE-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-BROWSE-SW", "N");
            ctx.f.setString("WS-EOF-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-BROWSE-SW", "N");
            ctx.f.setString("WS-EOF-SW", "Y");
        } else {
            ctx.f.setString("WS-BROWSE-SW", "N");
            ctx.f.setString("WS-EOF-SW", "Y");
            ctx.f.setKabStatus("99");
        }
    }

    /** COBOL paragraph: 2200-READ-TRAN */
    private void readNextTransaction(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsTranfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setWsScanned(ctx.f.getWsScanned() + 1);
            dispatchByMode(ctx);
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-EOF-SW", "Y");
        } else {
            ctx.f.setString("WS-EOF-SW", "Y");
            ctx.f.setKabStatus("99");
        }
    }

    /** COBOL paragraph: 2300-DISPATCH */
    private void dispatchByMode(TaskContext ctx) {
        switch (Utility.rtrim(ctx.f.getKabMode())) {
            case "RW" -> {
                accumulateRewardsPoints(ctx);
            }
            case "FR" -> {
                accumulateFraudReviewStats(ctx);
            }
            case "GL" -> {
                accumulateGeneralLedgerEntries(ctx);
            }
            case "RC" -> {
                accumulateReconciliationEntries(ctx);
            }
            default -> {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: 2350-CLASSIFY */
    private void classifyPurchaseTransaction(TaskContext ctx) {
        switch (Utility.rtrim(ctx.f.getTrTypeCd())) {
            case "PY", "CR", "FE", "IN" -> {
                ctx.f.setString("WS-PURCHASE-SW", "N");
            }
            default -> {
                ctx.f.setString("WS-PURCHASE-SW", "Y");
            }
        }
    }

    /** COBOL paragraph: 2400-ACCUM-RW */
    private void accumulateRewardsPoints(TaskContext ctx) {
        classifyPurchaseTransaction(ctx);
        if (ctx.f.getWsPurchaseSw().equals("Y")) {
            ctx.f.setWsWholeAmt(ctx.f.getTrAmt().longValue());
            if ((ctx.f.getTrAmt().compareTo(ctx.f.getWsLargeThresh()) >= 0)) {
                ctx.f.setWsPoints((ctx.f.getWsWholeAmt() * (long) ctx.f.getWsBonusRate()));
            } else {
                ctx.f.setWsPoints((ctx.f.getWsWholeAmt() * (long) ctx.f.getWsBaseRate()));
            }
            ctx.f.setWsKey(ctx.f.getTrCardNum());
            findOrAddResultSlot(ctx);
            if (ctx.f.getWsSlotSw().equals("Y")) {
                ctx.f.setWsReCnt(ctx.f.getWsFidx(), ctx.f.getWsReCnt(ctx.f.getWsFidx()) + 1);
                ctx.f.setWsReAmt(
                        ctx.f.getWsFidx(),
                        ctx.f.getWsReAmt(ctx.f.getWsFidx()).add(ctx.f.getTrAmt()));
                ctx.f.setWsReVal(
                        ctx.f.getWsFidx(),
                        ctx.f
                                .getWsReVal(ctx.f.getWsFidx())
                                .add(java.math.BigDecimal.valueOf(ctx.f.getWsPoints())));
                ctx.f.setWsReInfo(ctx.f.getWsFidx(), "PURCHASES");
                ctx.f.setWsTot1(ctx.f.getWsTot1().add(ctx.f.getTrAmt()));
                ctx.f.setWsTot2(
                        ctx.f.getWsTot2().add(java.math.BigDecimal.valueOf(ctx.f.getWsPoints())));
            }
        }
    }

    /** COBOL paragraph: 2500-ACCUM-FR */
    private void accumulateFraudReviewStats(TaskContext ctx) {
        ctx.f.setWsKey(ctx.f.getTrCardNum());
        findOrAddResultSlot(ctx);
        if (ctx.f.getWsSlotSw().equals("Y")) {
            ctx.f.setWsReCnt(ctx.f.getWsFidx(), ctx.f.getWsReCnt(ctx.f.getWsFidx()) + 1);
            ctx.f.setWsReAmt(
                    ctx.f.getWsFidx(), ctx.f.getWsReAmt(ctx.f.getWsFidx()).add(ctx.f.getTrAmt()));
            if ((ctx.f.getTrAmt().compareTo(ctx.f.getWsReVal(ctx.f.getWsFidx())) > 0)) {
                ctx.f.setWsReVal(ctx.f.getWsFidx(), ctx.f.getTrAmt());
            }
        }
    }

    /** COBOL paragraph: 2600-ACCUM-GL */
    private void accumulateGeneralLedgerEntries(TaskContext ctx) {
        ctx.f.setWsKey(" ");
        ctx.f.setWsKey(
                Utility.setSubstring(
                        String.valueOf(ctx.f.getWsKey()),
                        1,
                        2,
                        String.valueOf(ctx.f.getTrTypeCd())));
        findOrAddResultSlot(ctx);
        if (ctx.f.getWsSlotSw().equals("Y")) {
            ctx.f.setWsReCnt(ctx.f.getWsFidx(), ctx.f.getWsReCnt(ctx.f.getWsFidx()) + 1);
            ctx.f.setWsReAmt(
                    ctx.f.getWsFidx(), ctx.f.getWsReAmt(ctx.f.getWsFidx()).add(ctx.f.getTrAmt()));
            if (Utility.fieldEquals(ctx.f.getTrTypeCd(), "PY")
                    || Utility.fieldEquals(ctx.f.getTrTypeCd(), "CR")) {
                ctx.f.setWsReInfo(ctx.f.getWsFidx(), "CR");
                ctx.f.setWsTot2(ctx.f.getWsTot2().add(ctx.f.getTrAmt()));
            } else {
                ctx.f.setWsReInfo(ctx.f.getWsFidx(), "DR");
                ctx.f.setWsTot1(ctx.f.getWsTot1().add(ctx.f.getTrAmt()));
            }
        }
    }

    /** COBOL paragraph: 2700-ACCUM-RC */
    private void accumulateReconciliationEntries(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getTrTypeCd(), "PY")
                || Utility.fieldEquals(ctx.f.getTrTypeCd(), "CR")) {
            ctx.f.setWsTot2(ctx.f.getWsTot2().add(ctx.f.getTrAmt()));
            ctx.f.setWsMove(ctx.f.getTrAmt().multiply(java.math.BigDecimal.valueOf(-1)));
        } else {
            ctx.f.setWsTot1(ctx.f.getWsTot1().add(ctx.f.getTrAmt()));
            ctx.f.setWsMove(ctx.f.getTrAmt());
        }
        ctx.f.setXrCardNum(ctx.f.getTrCardNum());
        ctx.appService.readFile(
                ctx.f.getWsXreffile(), ctx.f, String.valueOf(ctx.f.getXrCardNum()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setWsKey(" ");
            ctx.f.setWsKey(
                    Utility.setSubstring(
                            String.valueOf(ctx.f.getWsKey()),
                            1,
                            11,
                            String.valueOf(ctx.f.getXrAcctId())));
            findOrAddResultSlot(ctx);
            if (ctx.f.getWsSlotSw().equals("Y")) {
                ctx.f.setWsReCnt(ctx.f.getWsFidx(), ctx.f.getWsReCnt(ctx.f.getWsFidx()) + 1);
                ctx.f.setWsReAmt(
                        ctx.f.getWsFidx(),
                        ctx.f.getWsReAmt(ctx.f.getWsFidx()).add(ctx.f.getWsMove()));
            }
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setWsOrphCnt(ctx.f.getWsOrphCnt() + 1);
        } else {
            ctx.f.setWsOrphCnt(ctx.f.getWsOrphCnt() + 1);
        }
    }

    /** COBOL paragraph: 2900-END-TRAN */
    private void endTransactionBrowse(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsTranfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 3000-FINALIZE */
    private void finalizeResults(TaskContext ctx) {
        switch (Utility.rtrim(ctx.f.getKabMode())) {
            case "FR" -> {
                filterFlaggedFraudEntries(ctx);
            }
            case "RC" -> {
                reconcileAccountBalances(ctx);
            }
            default -> {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: 3100-FINALIZE-FR */
    private void filterFlaggedFraudEntries(TaskContext ctx) {
        ctx.f.setWsNewCnt(0);
        ctx.f.setWsTot1(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsTot2(java.math.BigDecimal.valueOf(0));
        for (ctx.f.setWsIdx(1);
                ctx.f.getWsIdx() <= ctx.f.getWsResCnt();
                ctx.f.setWsIdx(ctx.f.getWsIdx() + 1)) {
            ctx.f.setWsFlagSw("N");
            ctx.f.setWsReason(" ");
            if ((ctx.f.getWsReVal(ctx.f.getWsIdx()).compareTo(ctx.f.getWsLargeThresh()) >= 0)) {
                ctx.f.setWsFlagSw("Y");
                ctx.f.setWsReason("LARGE AMT");
            }
            if (ctx.f.getWsReCnt(ctx.f.getWsIdx()) >= ctx.f.getWsVeloThresh()) {
                ctx.f.setWsFlagSw("Y");
                if (Utility.fieldEquals(ctx.f.getWsReason(), " ")) {
                    ctx.f.setWsReason("VELOCITY");
                } else {
                    ctx.f.setWsReason("LARGE+VELO");
                }
            }
            if (Utility.fieldEquals(ctx.f.getWsFlagSw(), "Y")) {
                ctx.f.setWsNewCnt(ctx.f.getWsNewCnt() + 1);
                ctx.f.setWsReKey(ctx.f.getWsNewCnt(), ctx.f.getWsReKey(ctx.f.getWsIdx()));
                ctx.f.setWsReInfo(ctx.f.getWsNewCnt(), ctx.f.getWsReason());
                ctx.f.setWsReCnt(ctx.f.getWsNewCnt(), ctx.f.getWsReCnt(ctx.f.getWsIdx()));
                ctx.f.setWsReAmt(ctx.f.getWsNewCnt(), ctx.f.getWsReAmt(ctx.f.getWsIdx()));
                ctx.f.setWsReVal(ctx.f.getWsNewCnt(), ctx.f.getWsReVal(ctx.f.getWsIdx()));
                ctx.f.setWsTot1(ctx.f.getWsTot1().add(ctx.f.getWsReAmt(ctx.f.getWsNewCnt())));
                ctx.f.setWsTot2(ctx.f.getWsTot2().add(java.math.BigDecimal.valueOf(1)));
            }
        }
        ctx.f.setWsResCnt(ctx.f.getWsNewCnt());
    }

    /** COBOL paragraph: 3200-FINALIZE-RC */
    private void reconcileAccountBalances(TaskContext ctx) {
        ctx.f.setWsDiscCnt(0);
        for (ctx.f.setWsIdx(1);
                ctx.f.getWsIdx() <= ctx.f.getWsResCnt();
                ctx.f.setWsIdx(ctx.f.getWsIdx() + 1)) {
            ctx.f.setWsKey(ctx.f.getWsReKey(ctx.f.getWsIdx()));
            ctx.f.setAcId(
                    Utility.parseNumeric(
                                    Utility.padRight(String.valueOf(ctx.f.getWsKey()), 11)
                                            .substring(0, 11))
                            .longValue());
            ctx.appService.readFile(
                    ctx.f.getWsAcctfile(), ctx.f, String.valueOf(ctx.f.getAcId()), 0);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
            if (ctx.f.getWsRespCd() == 0) {
                ctx.f.setWsReVal(ctx.f.getWsIdx(), ctx.f.getAcCurrBal());
                ctx.f.setWsDiff(ctx.f.getWsReAmt(ctx.f.getWsIdx()).subtract(ctx.f.getAcCurrBal()));
                if (ctx.f.getWsDiff().signum() == 0) {
                    ctx.f.setWsReInfo(ctx.f.getWsIdx(), "OK");
                } else {
                    ctx.f.setWsReInfo(ctx.f.getWsIdx(), "MISMATCH");
                    ctx.f.setWsDiscCnt(ctx.f.getWsDiscCnt() + 1);
                }
            } else if (ctx.f.getWsRespCd() == 13) {
                ctx.f.setWsReVal(ctx.f.getWsIdx(), java.math.BigDecimal.valueOf(0));
                ctx.f.setWsReInfo(ctx.f.getWsIdx(), "MISSING");
                ctx.f.setWsDiscCnt(ctx.f.getWsDiscCnt() + 1);
            } else {
                ctx.f.setWsReInfo(ctx.f.getWsIdx(), "READ ERR");
            }
        }
    }

    /** COBOL paragraph: 4000-BUILD-PAGE */
    private void buildResultPage(TaskContext ctx) {
        ctx.f.setKabResultCnt(ctx.f.getWsResCnt());
        ctx.f.setKabScanned(ctx.f.getWsScanned());
        ctx.f.setKabDisc(ctx.f.getWsDiscCnt());
        ctx.f.setKabTot1(ctx.f.getWsTot1());
        ctx.f.setKabTot2(ctx.f.getWsTot2());
        clearResultRows(ctx);
        ctx.f.setKabRowCnt(0);
        ctx.f.setKabMore("N");
        if (ctx.f.getWsResCnt() == 0) {
            return;
        }
        ctx.f.setWsStartIdx((ctx.f.getKabStartOff() + 1));
        if (ctx.f.getWsStartIdx() < 1) {
            ctx.f.setWsStartIdx(1);
        }
        ctx.f.setWsOutCnt(0);
        for (ctx.f.setWsIdx(ctx.f.getWsStartIdx());
                !(ctx.f.getWsIdx() > ctx.f.getWsResCnt()
                        || ctx.f.getWsOutCnt() >= ctx.f.getWsMaxRows());
                ctx.f.setWsIdx(ctx.f.getWsIdx() + 1)) {
            ctx.f.setWsOutCnt(ctx.f.getWsOutCnt() + 1);
            ctx.f.setKabRKey(ctx.f.getWsOutCnt(), ctx.f.getWsReKey(ctx.f.getWsIdx()));
            ctx.f.setKabRInfo(ctx.f.getWsOutCnt(), ctx.f.getWsReInfo(ctx.f.getWsIdx()));
            ctx.f.setKabRCnt(ctx.f.getWsOutCnt(), ctx.f.getWsReCnt(ctx.f.getWsIdx()));
            ctx.f.setKabRAmt(ctx.f.getWsOutCnt(), ctx.f.getWsReAmt(ctx.f.getWsIdx()));
            ctx.f.setKabRVal(ctx.f.getWsOutCnt(), ctx.f.getWsReVal(ctx.f.getWsIdx()));
        }
        ctx.f.setKabRowCnt(ctx.f.getWsOutCnt());
        if (ctx.f.getWsIdx() <= ctx.f.getWsResCnt()) {
            ctx.f.setKabMore("Y");
        }
    }

    /** COBOL paragraph: 4050-CLEAR-ROWS */
    private void clearResultRows(TaskContext ctx) {
        for (ctx.f.setWsSidx(1); ctx.f.getWsSidx() <= 6; ctx.f.setWsSidx(ctx.f.getWsSidx() + 1)) {
            ctx.f.setKabRKey(ctx.f.getWsSidx(), " ");
            ctx.f.setKabRInfo(ctx.f.getWsSidx(), " ");
            ctx.f.setKabRCnt(ctx.f.getWsSidx(), 0);
            ctx.f.setKabRAmt(ctx.f.getWsSidx(), java.math.BigDecimal.valueOf(0));
            ctx.f.setKabRVal(ctx.f.getWsSidx(), java.math.BigDecimal.valueOf(0));
        }
    }

    /** COBOL paragraph: 6000-SET-STATUS */
    private void updateResultStatus(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getKabStatus(), "99")) {
            ctx.f.setKabRowCnt(0);
        } else {
            if (ctx.f.getWsResCnt() == 0) {
                ctx.f.setKabStatus("10");
            } else {
                ctx.f.setKabStatus("00");
            }
        }
    }

    /** COBOL paragraph: 7000-FIND-OR-ADD */
    private void findOrAddResultSlot(TaskContext ctx) {
        ctx.f.setString("WS-SLOT-SW", "N");
        ctx.f.setWsFidx(0);
        for (ctx.f.setWsSidx(1);
                ctx.f.getWsSidx() <= ctx.f.getWsResCnt() && !ctx.f.getWsSlotSw().equals("Y");
                ctx.f.setWsSidx(ctx.f.getWsSidx() + 1)) {
            if (Utility.fieldEquals(ctx.f.getWsReKey(ctx.f.getWsSidx()), ctx.f.getWsKey())) {
                ctx.f.setWsFidx(ctx.f.getWsSidx());
                ctx.f.setString("WS-SLOT-SW", "Y");
            }
        }
        if (!ctx.f.getWsSlotSw().equals("Y")) {
            if (ctx.f.getWsResCnt() < ctx.f.getWsMaxRes()) {
                ctx.f.setWsResCnt(ctx.f.getWsResCnt() + 1);
                ctx.f.setWsFidx(ctx.f.getWsResCnt());
                ctx.f.setWsReKey(ctx.f.getWsFidx(), ctx.f.getWsKey());
                ctx.f.setWsReInfo(ctx.f.getWsFidx(), " ");
                ctx.f.setWsReCnt(ctx.f.getWsFidx(), 0);
                ctx.f.setWsReAmt(ctx.f.getWsFidx(), java.math.BigDecimal.valueOf(0));
                ctx.f.setWsReVal(ctx.f.getWsFidx(), java.math.BigDecimal.valueOf(0));
                ctx.f.setString("WS-SLOT-SW", "Y");
            } else {
                ctx.f.setWsOflowCnt(ctx.f.getWsOflowCnt() + 1);
                ctx.f.setString("WS-SLOT-SW", "N");
            }
        }
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OuanlinFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OuanlinFields(ws);
        }
    }
}

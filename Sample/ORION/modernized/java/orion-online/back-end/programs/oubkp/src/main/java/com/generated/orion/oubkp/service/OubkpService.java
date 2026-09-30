package com.generated.orion.oubkp.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.oubkp.accessor.OubkpFields;
import com.generated.orion.oubkp.metadata.OubkpBmsMetadata;
import com.generated.orion.oubkp.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUBKP. */
@Service
public class OubkpService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OubkpService.class);

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
                    ctx.f.writeBytes("ORION-COMMAREA", (byte[]) _params[0]);
                } else if (_params.length > 0 && _params[0] != null) {
                    ctx.f.setGroup("ORION-COMMAREA", String.valueOf(_params[0]));
                }
            } else if (_commarea instanceof byte[]) {
                ctx.f.writeBytes("ORION-COMMAREA", (byte[]) _commarea);
            } else {
                ctx.f.setGroup("ORION-COMMAREA", String.valueOf(_commarea));
            }
        }
        try {
            runBackupExtractionFlow(ctx);
        } finally {
            if (ctx.appService.getEibcalen() > 0) {
                if (ctx.appService.getCommarea() instanceof Object[]) {
                    Object[] _params = (Object[]) ctx.appService.getCommarea();
                    if (_params.length > 0) {
                        _params[0] =
                                _params[0] instanceof byte[]
                                        ? (Object) ctx.f.sliceBytes("ORION-COMMAREA")
                                        : (Object) ctx.f.groupToString("ORION-COMMAREA");
                    }
                } else {
                    ctx.appService.setCommarea(ctx.f.sliceBytes("ORION-COMMAREA"));
                }
            }
        }
    }

    @Override
    public String getProgramName() {
        return "OUBKP";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OubkpBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OubkpBmsMetadata.registerFsetFields(runner);
    }

    /**
     * Orchestrate transaction backup extraction: init, browse position, extract loop, finalize
     * status. COBOL paragraph: 0000-MAIN
     */
    private void runBackupExtractionFlow(TaskContext ctx) {
        ctx.f.aliasGroup("KBK-PARM", "CA-WORK-AREA");
        initializeWorkingStorage(ctx);
        if (!Utility.fieldEquals(ctx.f.getKbkStatus(), "99")) {
            startTransactionBrowse(ctx);
            if (ctx.f.getWsBrowseSw().equals("Y")) {
                while (!(ctx.f.getWsEofSw().equals("Y")
                        || Utility.fieldEquals(ctx.f.getWsCapped(), "Y"))) {
                    extractTransactionIteration(ctx);
                }
                peekNextTransaction(ctx);
                endTransactionBrowse(ctx);
            }
        }
        finalizeCompletionStatus(ctx);
        return;
    }

    /**
     * Reset working-storage counters, amounts, and switches before extraction. COBOL paragraph:
     * 1000-INIT
     */
    private void initializeWorkingStorage(TaskContext ctx) {
        ctx.f.setString("WS-EOF-SW", "N");
        ctx.f.setString("WS-BROWSE-SW", "N");
        ctx.f.setWsCapped("N");
        ctx.f.setKbkStatus("00");
        ctx.f.setKbkMsg(" ");
        ctx.f.setKbkRead(0);
        ctx.f.setKbkWritten(0);
        ctx.f.setKbkErrors(0);
        ctx.f.setKbkTotAmt(java.math.BigDecimal.valueOf(0));
        ctx.f.setKbkCreditAmt(java.math.BigDecimal.valueOf(0));
        ctx.f.setKbkDebitAmt(java.math.BigDecimal.valueOf(0));
        ctx.f.setKbkNextKey(" ");
        ctx.f.setKbkMore("N");
        ctx.f.setWsMax(ctx.f.getKbkMax());
    }

    /**
     * Position the transaction file browse cursor at the requested start key. COBOL paragraph:
     * 2000-POSITION
     */
    private void startTransactionBrowse(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getKbkStartKey(), " ")
                || ctx.f.isAllLowValues("KBK-START-KEY")) {
            ctx.f.fillLowValues("TR-ID");
        } else {
            ctx.f.setTrId(ctx.f.getKbkStartKey());
        }
        ctx.appService.startBrowse(ctx.f.getWsTranfile(), String.valueOf(ctx.f.getTrId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BROWSE-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-EOF-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-EOF-SW", "Y");
        } else {
            ctx.f.setKbkStatus("99");
            ctx.f.setKbkMsg("TRANFILE STARTBR FAILED");
        }
    }

    /**
     * Run one extraction-loop iteration: read a transaction and extract it if not EOF. COBOL
     * paragraph: 3000-EXTRACT-LOOP
     */
    private void extractTransactionIteration(TaskContext ctx) {
        readNextTransaction(ctx);
        if (!ctx.f.getWsEofSw().equals("Y")) {
            extractAndWriteTransaction(ctx);
            if (ctx.f.getWsMax() > 0 && ctx.f.getKbkRead() >= ctx.f.getWsMax()) {
                ctx.f.setWsCapped("Y");
            }
        }
    }

    /**
     * Read the next transaction record from the transaction file browse. COBOL paragraph:
     * 3100-READ-TRAN
     */
    private void readNextTransaction(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsTranfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            /* CONTINUE */
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-EOF-SW", "Y");
        } else {
            ctx.f.setString("WS-EOF-SW", "Y");
            ctx.f.setKbkErrors(ctx.f.getKbkErrors() + 1);
            ctx.f.setKbkMsg("TRANFILE READNEXT FAILED");
        }
    }

    /**
     * Build, write, and accumulate one backup record for the current transaction. COBOL paragraph:
     * 3200-EXTRACT-ONE
     */
    private void extractAndWriteTransaction(TaskContext ctx) {
        ctx.f.setKbkRead(ctx.f.getKbkRead() + 1);
        populateBackupRecord(ctx);
        writeBackupRecord(ctx);
        accumulateTransactionAmounts(ctx);
    }

    /**
     * Populate backup record fields from the current transaction record. COBOL paragraph:
     * 3300-BUILD-BKP-REC
     */
    private void populateBackupRecord(TaskContext ctx) {
        ctx.f.setBkBar1("|");
        ctx.f.setBkBar2("|");
        ctx.f.setBkBar3("|");
        ctx.f.setBkBar4("|");
        ctx.f.setBkBar5("|");
        ctx.f.setBkBar6("|");
        ctx.f.setBkBar7("|");
        ctx.f.setBkTranId(ctx.f.getTrId());
        ctx.f.setBkType(ctx.f.getTrTypeCd());
        ctx.f.setBkCat(ctx.f.getTrCatCd());
        ctx.f.setBkCardNum(ctx.f.getTrCardNum());
        ctx.f.setBkAmt(ctx.f.getTrAmt());
        ctx.f.setBkMerchId(ctx.f.getTrMerchantId());
        ctx.f.setBkOrigTs(ctx.f.getTrOrigTs());
        ctx.f.setBkDesc(ctx.f.getTrDesc());
    }

    /**
     * Write the backup record and track written/error counts by response code. COBOL paragraph:
     * 3400-WRITE-BKP-REC
     */
    private void writeBackupRecord(TaskContext ctx) {
        ctx.appService.writeFile(
                ctx.f.getWsBkpfile(), ctx.f, String.valueOf(ctx.f.getBkTranId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKbkWritten(ctx.f.getKbkWritten() + 1);
        } else {
            ctx.f.setKbkErrors(ctx.f.getKbkErrors() + 1);
        }
    }

    /**
     * Accumulate the transaction amount into total, credit, and debit sums. COBOL paragraph:
     * 3500-ACCUMULATE
     */
    private void accumulateTransactionAmounts(TaskContext ctx) {
        ctx.f.setKbkTotAmt(ctx.f.getKbkTotAmt().add(ctx.f.getTrAmt()));
        if ((ctx.f.getTrAmt().signum() < 0)) {
            ctx.f.setKbkDebitAmt(ctx.f.getKbkDebitAmt().add(ctx.f.getTrAmt()));
        } else {
            ctx.f.setKbkCreditAmt(ctx.f.getKbkCreditAmt().add(ctx.f.getTrAmt()));
        }
    }

    /**
     * Peek the next record after the loop ends to determine if more data remains. COBOL paragraph:
     * 4000-PEEK-NEXT
     */
    private void peekNextTransaction(TaskContext ctx) {
        if (ctx.f.getWsEofSw().equals("Y")) {
            ctx.f.setKbkMore("N");
            return;
        }
        ctx.appService.readNext(ctx.f.getWsTranfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKbkMore("Y");
            ctx.f.setKbkNextKey(ctx.f.getTrId());
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setKbkMore("N");
        } else {
            ctx.f.setKbkMore("N");
            ctx.f.setKbkErrors(ctx.f.getKbkErrors() + 1);
        }
    }

    /** Close the transaction file browse cursor. COBOL paragraph: 5000-END-BROWSE */
    private void endTransactionBrowse(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsTranfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /**
     * Set the final completion status/message based on whether any transactions were read. COBOL
     * paragraph: 6000-SET-STATUS
     */
    private void finalizeCompletionStatus(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getKbkStatus(), "99")) {
            return;
        }
        if (ctx.f.getKbkRead() == 0) {
            ctx.f.setKbkStatus("10");
            ctx.f.setKbkMsg("NO TRANSACTIONS EXTRACTED");
        } else {
            ctx.f.setKbkStatus("00");
            ctx.f.setKbkMsg("TRANSACTION BACKUP COMPLETE");
        }
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OubkpFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OubkpFields(ws);
        }
    }
}

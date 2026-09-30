package com.generated.orion.ouarch.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.ouarch.accessor.OuarchFields;
import com.generated.orion.ouarch.metadata.OuarchBmsMetadata;
import com.generated.orion.ouarch.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUARCH. */
@Service
public class OuarchService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OuarchService.class);

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
            runMainProgram(ctx);
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
        return "OUARCH";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OuarchBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OuarchBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.aliasGroup("KAR-PARM", "CA-WORK-AREA");
        initializeProgram(ctx);
        if (!Utility.fieldEquals(ctx.f.getKarStatus(), "99")) {
            positionTransactionBrowse(ctx);
            if (ctx.f.getWsBrowseSw().equals("Y")) {
                while (!ctx.f.getWsEofSw().equals("Y")
                        && !Utility.fieldEquals(ctx.f.getWsCapped(), "Y")) {
                    scanTransactionRecords(ctx);
                }
                peekNextTransaction(ctx);
                endTransactionBrowse(ctx);
                archiveSavedTransactions(ctx);
            }
        }
        finalizeReturnStatus(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INIT */
    private void initializeProgram(TaskContext ctx) {
        ctx.f.setString("WS-EOF-SW", "N");
        ctx.f.setString("WS-BROWSE-SW", "N");
        ctx.f.setWsCapped("N");
        ctx.f.setWsSaveCnt(0);
        ctx.f.setKarStatus("00");
        ctx.f.setKarMsg(" ");
        ctx.f.setKarRead(0);
        ctx.f.setKarArchived(0);
        ctx.f.setKarDeleted(0);
        ctx.f.setKarKept(0);
        ctx.f.setKarErrors(0);
        ctx.f.setKarArchAmt(java.math.BigDecimal.valueOf(0));
        ctx.f.setKarNextTran(" ");
        ctx.f.setKarMore("N");
        ctx.f.setWsMax(ctx.f.getKarMax());
        ctx.f.setWsCap(ctx.f.getWsMaxSave());
        if (ctx.f.getWsMax() > 0 && ctx.f.getWsMax() < ctx.f.getWsMaxSave()) {
            ctx.f.setWsCap(ctx.f.getWsMax());
        }
        ctx.f.setWsDtIn(ctx.f.getKarCutoff());
        validateCutoffDate(ctx);
        if (!Utility.fieldEquals(ctx.f.getWsDtOk(), "Y")) {
            ctx.f.setKarStatus("99");
            ctx.f.setKarMsg("INVALID CUTOFF - USE YYYY-MM-DD");
        }
    }

    /** COBOL paragraph: 1100-CHK-DATE */
    private void validateCutoffDate(TaskContext ctx) {
        ctx.f.setWsDtOk("N");
        ctx.f.setWsDt(String.valueOf(ctx.f.getWsDtIn()).trim());
        String wsDt = String.valueOf(ctx.f.getWsDt());
        boolean hasDashSeparators =
                Utility.fieldEquals(Utility.padRight(wsDt, 5).substring(4, 5), "-")
                        && Utility.fieldEquals(Utility.padRight(wsDt, 8).substring(7, 8), "-");
        boolean hasNumericParts =
                Utility.isNumeric(Utility.padRight(wsDt, 4).substring(0, 4))
                        && Utility.isNumeric(Utility.padRight(wsDt, 7).substring(5, 7))
                        && Utility.isNumeric(Utility.padRight(wsDt, 10).substring(8, 10));
        if (hasDashSeparators && hasNumericParts) {
            ctx.f.setWsDtOk("Y");
        }
    }

    /** COBOL paragraph: 2000-POSITION */
    private void positionTransactionBrowse(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getKarStartTran(), " ")
                || ctx.f.isAllLowValues("KAR-START-TRAN")) {
            ctx.f.fillLowValues("TR-ID");
        } else {
            ctx.f.setTrId(ctx.f.getKarStartTran());
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
            ctx.f.setKarStatus("99");
            ctx.f.setKarMsg("TRANFILE STARTBR FAILED");
        }
    }

    /** COBOL paragraph: 3000-SCAN-LOOP */
    private void scanTransactionRecords(TaskContext ctx) {
        readNextTransaction(ctx);
        if (!ctx.f.getWsEofSw().equals("Y")) {
            classifyTransactionRecord(ctx);
            if (ctx.f.getWsSaveCnt() >= ctx.f.getWsCap()) {
                ctx.f.setWsCapped("Y");
            }
            if (ctx.f.getWsMax() > 0 && ctx.f.getKarRead() >= ctx.f.getWsMax()) {
                ctx.f.setWsCapped("Y");
            }
        }
    }

    /** COBOL paragraph: 3100-READ-TRAN */
    private void readNextTransaction(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsTranfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            /* CONTINUE */
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-EOF-SW", "Y");
        } else {
            ctx.f.setString("WS-EOF-SW", "Y");
            ctx.f.setKarErrors(ctx.f.getKarErrors() + 1);
            ctx.f.setKarMsg("TRANFILE READNEXT FAILED");
        }
    }

    /** COBOL paragraph: 3200-CLASSIFY */
    private void classifyTransactionRecord(TaskContext ctx) {
        ctx.f.setKarRead(ctx.f.getKarRead() + 1);
        ctx.f.setWsProcDate(
                Utility.padRight(String.valueOf(ctx.f.getTrProcTs()), 10).substring(0, 10));
        if ((ctx.f.getWsProcDate().compareTo(ctx.f.getKarCutoff()) < 0)) {
            if (ctx.f.getWsSaveCnt() < ctx.f.getWsCap()) {
                ctx.f.setWsSaveCnt(ctx.f.getWsSaveCnt() + 1);
                ctx.f.setWsSaveRec(ctx.f.getWsSaveCnt(), ctx.f.getTranRec());
            }
        } else {
            ctx.f.setKarKept(ctx.f.getKarKept() + 1);
        }
    }

    /** COBOL paragraph: 4000-PEEK-NEXT */
    private void peekNextTransaction(TaskContext ctx) {
        if (ctx.f.getWsEofSw().equals("Y")) {
            ctx.f.setKarMore("N");
            return;
        }
        ctx.appService.readNext(ctx.f.getWsTranfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKarMore("Y");
            ctx.f.setKarNextTran(ctx.f.getTrId());
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setKarMore("N");
        } else {
            ctx.f.setKarMore("N");
            ctx.f.setKarErrors(ctx.f.getKarErrors() + 1);
        }
    }

    /** COBOL paragraph: 5000-END-BROWSE */
    private void endTransactionBrowse(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsTranfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 5500-ARCHIVE-SAVED */
    private void archiveSavedTransactions(TaskContext ctx) {
        for (ctx.f.setWsSx(1);
                ctx.f.getWsSx() <= ctx.f.getWsSaveCnt();
                ctx.f.setWsSx(ctx.f.getWsSx() + 1)) {
            ctx.f.setTranRec(String.valueOf(ctx.f.getWsSaveRec(ctx.f.getWsSx())));
            archiveTransactionRecord(ctx);
        }
    }

    /** COBOL paragraph: 5600-ARCHIVE-ONE */
    private void archiveTransactionRecord(TaskContext ctx) {
        ctx.appService.writeFile(ctx.f.getWsArchfile(), ctx.f, String.valueOf(ctx.f.getTrId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKarArchived(ctx.f.getKarArchived() + 1);
            ctx.f.setKarArchAmt(ctx.f.getKarArchAmt().add(ctx.f.getTrAmt()));
            deleteTransactionRecord(ctx);
        } else if (ctx.f.getWsRespCd() == 14) {
            deleteTransactionRecord(ctx);
        } else {
            ctx.f.setKarErrors(ctx.f.getKarErrors() + 1);
        }
    }

    /** COBOL paragraph: 5700-DELETE-ONE */
    private void deleteTransactionRecord(TaskContext ctx) {
        ctx.appService.deleteFile(ctx.f.getWsTranfile(), String.valueOf(ctx.f.getTrId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKarDeleted(ctx.f.getKarDeleted() + 1);
        } else {
            ctx.f.setKarErrors(ctx.f.getKarErrors() + 1);
        }
    }

    /** COBOL paragraph: 6000-SET-STATUS */
    private void finalizeReturnStatus(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getKarStatus(), "99")) {
            return;
        }
        if (ctx.f.getKarRead() == 0) {
            ctx.f.setKarStatus("10");
            ctx.f.setKarMsg("NO TRANSACTIONS PROCESSED");
        } else {
            ctx.f.setKarStatus("00");
            ctx.f.setKarMsg("TRANSACTION ARCHIVE COMPLETE");
        }
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OuarchFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OuarchFields(ws);
        }
    }
}

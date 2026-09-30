package com.generated.orion.oupurg.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.oupurg.accessor.OupurgFields;
import com.generated.orion.oupurg.metadata.OupurgBmsMetadata;
import com.generated.orion.oupurg.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUPURG. */
@Service
public class OupurgService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OupurgService.class);

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
        return "OUPURG";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OupurgBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OupurgBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.aliasGroup("KPG-PARM", "CA-WORK-AREA");
        initializeProgram(ctx);
        if (!Utility.fieldEquals(ctx.f.getKpgStatus(), "99")) {
            positionTransactionBrowse(ctx);
            if (ctx.f.getWsBrowseSw().equals("Y")) {
                while (!ctx.f.getWsEofSw().equals("Y")
                        && !Utility.fieldEquals(ctx.f.getWsCapped(), "Y")) {
                    scanTransactionRecords(ctx);
                }
                peekNextTransaction(ctx);
                endTransactionBrowse(ctx);
                purgeSavedTransactions(ctx);
            }
        }
        setCompletionStatus(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INIT */
    private void initializeProgram(TaskContext ctx) {
        ctx.f.setString("WS-EOF-SW", "N");
        ctx.f.setString("WS-BROWSE-SW", "N");
        ctx.f.setWsCapped("N");
        ctx.f.setWsSaveCnt(0);
        ctx.f.setKpgStatus("00");
        ctx.f.setKpgMsg(" ");
        ctx.f.setKpgRead(0);
        ctx.f.setKpgPurged(0);
        ctx.f.setKpgKept(0);
        ctx.f.setKpgErrors(0);
        ctx.f.setKpgPurgeAmt(java.math.BigDecimal.valueOf(0));
        ctx.f.setKpgKeepAmt(java.math.BigDecimal.valueOf(0));
        ctx.f.setKpgNextTran(" ");
        ctx.f.setKpgMore("N");
        ctx.f.setWsMax(ctx.f.getKpgMax());
        ctx.f.setWsCap(ctx.f.getWsMaxSave());
        if (ctx.f.getWsMax() > 0 && ctx.f.getWsMax() < ctx.f.getWsMaxSave()) {
            ctx.f.setWsCap(ctx.f.getWsMax());
        }
        ctx.f.setWsDtIn(ctx.f.getKpgCutoff());
        validateCutoffDate(ctx);
        if (!Utility.fieldEquals(ctx.f.getWsDtOk(), "Y")) {
            ctx.f.setKpgStatus("99");
            ctx.f.setKpgMsg("INVALID CUTOFF - USE YYYY-MM-DD");
        }
    }

    /** COBOL paragraph: 1100-CHK-DATE */
    private void validateCutoffDate(TaskContext ctx) {
        ctx.f.setWsDtOk("N");
        ctx.f.setWsDt(String.valueOf(ctx.f.getWsDtIn()).trim());
        if (Utility.fieldEquals(
                        Utility.padRight(String.valueOf(ctx.f.getWsDt()), 5).substring(4, 5), "-")
                && Utility.fieldEquals(
                        Utility.padRight(String.valueOf(ctx.f.getWsDt()), 8).substring(7, 8),
                        "-")) {
            if (Utility.isNumeric(
                            String.valueOf(
                                    Utility.padRight(String.valueOf(ctx.f.getWsDt()), 4)
                                            .substring(0, 4)))
                    && Utility.isNumeric(
                            String.valueOf(
                                    Utility.padRight(String.valueOf(ctx.f.getWsDt()), 7)
                                            .substring(5, 7)))
                    && Utility.isNumeric(
                            String.valueOf(
                                    Utility.padRight(String.valueOf(ctx.f.getWsDt()), 10)
                                            .substring(8, 10)))) {
                ctx.f.setWsDtOk("Y");
            }
        }
    }

    /** COBOL paragraph: 2000-POSITION */
    private void positionTransactionBrowse(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getKpgStartTran(), " ")
                || ctx.f.isAllLowValues("KPG-START-TRAN")) {
            ctx.f.fillLowValues("TR-ID");
        } else {
            ctx.f.setTrId(ctx.f.getKpgStartTran());
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
            ctx.f.setKpgStatus("99");
            ctx.f.setKpgMsg("TRANFILE STARTBR FAILED");
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
            if (ctx.f.getWsMax() > 0 && ctx.f.getKpgRead() >= ctx.f.getWsMax()) {
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
            ctx.f.setKpgErrors(ctx.f.getKpgErrors() + 1);
            ctx.f.setKpgMsg("TRANFILE READNEXT FAILED");
        }
    }

    /** COBOL paragraph: 3200-CLASSIFY */
    private void classifyTransactionRecord(TaskContext ctx) {
        ctx.f.setKpgRead(ctx.f.getKpgRead() + 1);
        ctx.f.setWsProcDate(
                Utility.padRight(String.valueOf(ctx.f.getTrProcTs()), 10).substring(0, 10));
        if ((ctx.f.getWsProcDate().compareTo(ctx.f.getKpgCutoff()) < 0)) {
            if (ctx.f.getWsSaveCnt() < ctx.f.getWsCap()) {
                ctx.f.setWsSaveCnt(ctx.f.getWsSaveCnt() + 1);
                ctx.f.setWsPId(ctx.f.getWsSaveCnt(), ctx.f.getTrId());
                ctx.f.setWsPAmt(ctx.f.getWsSaveCnt(), ctx.f.getTrAmt());
            }
        } else {
            ctx.f.setKpgKept(ctx.f.getKpgKept() + 1);
            ctx.f.setKpgKeepAmt(ctx.f.getKpgKeepAmt().add(ctx.f.getTrAmt()));
        }
    }

    /** COBOL paragraph: 4000-PEEK-NEXT */
    private void peekNextTransaction(TaskContext ctx) {
        if (ctx.f.getWsEofSw().equals("Y")) {
            ctx.f.setKpgMore("N");
            return;
        }
        ctx.appService.readNext(ctx.f.getWsTranfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKpgMore("Y");
            ctx.f.setKpgNextTran(ctx.f.getTrId());
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setKpgMore("N");
        } else {
            ctx.f.setKpgMore("N");
            ctx.f.setKpgErrors(ctx.f.getKpgErrors() + 1);
        }
    }

    /** COBOL paragraph: 5000-END-BROWSE */
    private void endTransactionBrowse(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsTranfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 5500-PURGE-SAVED */
    private void purgeSavedTransactions(TaskContext ctx) {
        for (ctx.f.setWsSx(1);
                ctx.f.getWsSx() <= ctx.f.getWsSaveCnt();
                ctx.f.setWsSx(ctx.f.getWsSx() + 1)) {
            purgeOneTransaction(ctx);
        }
    }

    /** COBOL paragraph: 5600-PURGE-ONE */
    private void purgeOneTransaction(TaskContext ctx) {
        ctx.f.setTrId(ctx.f.getWsPId(ctx.f.getWsSx()));
        ctx.appService.deleteFile(ctx.f.getWsTranfile(), String.valueOf(ctx.f.getTrId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKpgPurged(ctx.f.getKpgPurged() + 1);
            ctx.f.setKpgPurgeAmt(ctx.f.getKpgPurgeAmt().add(ctx.f.getWsPAmt(ctx.f.getWsSx())));
        } else {
            ctx.f.setKpgErrors(ctx.f.getKpgErrors() + 1);
        }
    }

    /** COBOL paragraph: 6000-SET-STATUS */
    private void setCompletionStatus(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getKpgStatus(), "99")) {
            return;
        }
        if (ctx.f.getKpgRead() == 0) {
            ctx.f.setKpgStatus("10");
            ctx.f.setKpgMsg("NO TRANSACTIONS PROCESSED");
        } else {
            ctx.f.setKpgStatus("00");
            ctx.f.setKpgMsg("TRANSACTION PURGE COMPLETE");
        }
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OupurgFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OupurgFields(ws);
        }
    }
}

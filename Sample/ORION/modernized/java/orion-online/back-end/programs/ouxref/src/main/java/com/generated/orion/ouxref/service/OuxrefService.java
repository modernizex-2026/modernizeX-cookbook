package com.generated.orion.ouxref.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.ouxref.accessor.OuxrefFields;
import com.generated.orion.ouxref.metadata.OuxrefBmsMetadata;
import com.generated.orion.ouxref.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUXREF. */
@Service
public class OuxrefService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OuxrefService.class);

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
        return "OUXREF";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OuxrefBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OuxrefBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.aliasGroup("KUX-PARM", "CA-WORK-AREA");
        initializeProgram(ctx);
        if (!Utility.fieldEquals(ctx.f.getKuxStatus(), "99")) {
            positionCardBrowse(ctx);
            if (ctx.f.getWsBrowseSw().equals("Y")) {
                while (!(ctx.f.getWsEofSw().equals("Y")
                        || Utility.fieldEquals(ctx.f.getWsCapped(), "Y"))) {
                    readAndProcessCards(ctx);
                }
                peekNextCard(ctx);
                endCardBrowse(ctx);
            }
        }
        setFinalStatus(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INIT */
    private void initializeProgram(TaskContext ctx) {
        ctx.f.setString("WS-EOF-SW", "N");
        ctx.f.setString("WS-BROWSE-SW", "N");
        ctx.f.setWsCapped("N");
        ctx.f.setWsSaveCust(0);
        ctx.f.setKuxStatus("00");
        ctx.f.setKuxMsg(" ");
        ctx.f.setKuxRead(0);
        ctx.f.setKuxWritten(0);
        ctx.f.setKuxUpdated(0);
        ctx.f.setKuxSkipAcct(0);
        ctx.f.setKuxSkipXref(0);
        ctx.f.setKuxSkipCust(0);
        ctx.f.setKuxErrors(0);
        ctx.f.setKuxNextCard(" ");
        ctx.f.setKuxMore("N");
        ctx.f.setWsMax(ctx.f.getKuxMax());
        switch (Utility.rtrim(ctx.f.getKuxMode())) {
            case "REBL" -> {
                ctx.f.setString("WS-MODE-SW", "R");
            }
            case "VALD" -> {
                ctx.f.setString("WS-MODE-SW", "V");
            }
            default -> {
                ctx.f.setKuxStatus("99");
                ctx.f.setKuxMsg("INVALID MODE - USE REBL OR VALD");
            }
        }
    }

    /** COBOL paragraph: 2000-POSITION */
    private void positionCardBrowse(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getKuxStartCard(), " ")
                || ctx.f.isAllLowValues("KUX-START-CARD")) {
            ctx.f.fillLowValues("CD-NUM");
        } else {
            ctx.f.setCdNum(ctx.f.getKuxStartCard());
        }
        ctx.appService.startBrowse(ctx.f.getWsCardfile(), String.valueOf(ctx.f.getCdNum()), 0);
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
            ctx.f.setKuxStatus("99");
            ctx.f.setKuxMsg("CARDFILE STARTBR FAILED");
        }
    }

    /** COBOL paragraph: 3000-READ-LOOP */
    private void readAndProcessCards(TaskContext ctx) {
        readNextCard(ctx);
        if (!ctx.f.getWsEofSw().equals("Y")) {
            processCard(ctx);
            if (ctx.f.getWsMax() > 0 && ctx.f.getKuxRead() >= ctx.f.getWsMax()) {
                ctx.f.setWsCapped("Y");
            }
        }
    }

    /** COBOL paragraph: 3100-READ-CARD */
    private void readNextCard(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsCardfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            /* CONTINUE */
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-EOF-SW", "Y");
        } else {
            ctx.f.setString("WS-EOF-SW", "Y");
            ctx.f.setKuxErrors(ctx.f.getKuxErrors() + 1);
            ctx.f.setKuxMsg("CARDFILE READNEXT FAILED");
        }
    }

    /** COBOL paragraph: 3200-PROCESS-CARD */
    private void processCard(TaskContext ctx) {
        ctx.f.setKuxRead(ctx.f.getKuxRead() + 1);
        ctx.f.setString("WS-CARD-SW", "O");
        ctx.f.setWsSaveCust(0);
        validateAccount(ctx);
        if (ctx.f.getWsCardSw().equals("O")) {
            readCrossReference(ctx);
        }
        if (ctx.f.getWsCardSw().equals("O")) {
            validateCustomer(ctx);
        }
        if (ctx.f.getWsCardSw().equals("O")) {
            applyCardUpdate(ctx);
        }
    }

    /** COBOL paragraph: 3300-VAL-ACCT */
    private void validateAccount(TaskContext ctx) {
        ctx.f.setAcId(ctx.f.getCdAcctId());
        ctx.appService.readFile(ctx.f.getWsAcctfile(), ctx.f, String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            /* CONTINUE */
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setKuxSkipAcct(ctx.f.getKuxSkipAcct() + 1);
            ctx.f.setString("WS-CARD-SW", "S");
        } else {
            ctx.f.setKuxErrors(ctx.f.getKuxErrors() + 1);
            ctx.f.setString("WS-CARD-SW", "S");
        }
    }

    /** COBOL paragraph: 3400-READ-XREF */
    private void readCrossReference(TaskContext ctx) {
        ctx.f.setXrCardNum(ctx.f.getCdNum());
        ctx.appService.readFile(
                ctx.f.getWsXreffile(), ctx.f, String.valueOf(ctx.f.getXrCardNum()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setWsSaveCust(ctx.f.getXrCustId());
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setKuxSkipXref(ctx.f.getKuxSkipXref() + 1);
            ctx.f.setString("WS-CARD-SW", "S");
        } else {
            ctx.f.setKuxErrors(ctx.f.getKuxErrors() + 1);
            ctx.f.setString("WS-CARD-SW", "S");
        }
    }

    /** COBOL paragraph: 3500-VAL-CUST */
    private void validateCustomer(TaskContext ctx) {
        ctx.f.setCuId(ctx.f.getWsSaveCust());
        ctx.appService.readFile(ctx.f.getWsCustfile(), ctx.f, String.valueOf(ctx.f.getCuId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            /* CONTINUE */
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setKuxSkipCust(ctx.f.getKuxSkipCust() + 1);
            ctx.f.setString("WS-CARD-SW", "S");
        } else {
            ctx.f.setKuxErrors(ctx.f.getKuxErrors() + 1);
            ctx.f.setString("WS-CARD-SW", "S");
        }
    }

    /** COBOL paragraph: 3600-APPLY */
    private void applyCardUpdate(TaskContext ctx) {
        if (ctx.f.getWsModeSw().equals("V")) {
            ctx.f.setKuxUpdated(ctx.f.getKuxUpdated() + 1);
        } else {
            rewriteCrossReference(ctx);
        }
    }

    /** COBOL paragraph: 3700-REWRITE-XREF */
    private void rewriteCrossReference(TaskContext ctx) {
        ctx.f.setXrCardNum(ctx.f.getCdNum());
        ctx.appService.readFileForUpdate(
                ctx.f.getWsXreffile(), ctx.f, String.valueOf(ctx.f.getXrCardNum()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setXrAcctId(ctx.f.getCdAcctId());
            ctx.f.setXrCustId(ctx.f.getWsSaveCust());
            ctx.appService.rewriteFile(ctx.f.getWsXreffile(), ctx.f);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
            if (ctx.f.getWsRespCd() == 0) {
                ctx.f.setKuxUpdated(ctx.f.getKuxUpdated() + 1);
            } else {
                ctx.f.setKuxErrors(ctx.f.getKuxErrors() + 1);
            }
        } else if (ctx.f.getWsRespCd() == 13) {
            writeCrossReference(ctx);
        } else {
            ctx.f.setKuxErrors(ctx.f.getKuxErrors() + 1);
        }
    }

    /** COBOL paragraph: 3800-WRITE-XREF */
    private void writeCrossReference(TaskContext ctx) {
        ctx.f.setXrefRec(" ");
        ctx.f.setXrCardNum(ctx.f.getCdNum());
        ctx.f.setXrAcctId(ctx.f.getCdAcctId());
        ctx.f.setXrCustId(ctx.f.getWsSaveCust());
        ctx.appService.writeFile(
                ctx.f.getWsXreffile(), ctx.f, String.valueOf(ctx.f.getXrCardNum()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKuxWritten(ctx.f.getKuxWritten() + 1);
        } else if (ctx.f.getWsRespCd() == 14) {
            ctx.f.setKuxErrors(ctx.f.getKuxErrors() + 1);
        } else {
            ctx.f.setKuxErrors(ctx.f.getKuxErrors() + 1);
        }
    }

    /** COBOL paragraph: 4000-PEEK-NEXT */
    private void peekNextCard(TaskContext ctx) {
        if (ctx.f.getWsEofSw().equals("Y")) {
            ctx.f.setKuxMore("N");
            return;
        }
        ctx.appService.readNext(ctx.f.getWsCardfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKuxMore("Y");
            ctx.f.setKuxNextCard(ctx.f.getCdNum());
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setKuxMore("N");
        } else {
            ctx.f.setKuxMore("N");
            ctx.f.setKuxErrors(ctx.f.getKuxErrors() + 1);
        }
    }

    /** COBOL paragraph: 5000-END-BROWSE */
    private void endCardBrowse(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsCardfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 6000-SET-STATUS */
    private void setFinalStatus(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getKuxStatus(), "99")) {
            return;
        }
        if (ctx.f.getKuxRead() == 0) {
            ctx.f.setKuxStatus("10");
            ctx.f.setKuxMsg("NO CARDS PROCESSED");
        } else {
            ctx.f.setKuxStatus("00");
            if (ctx.f.getWsModeSw().equals("V")) {
                ctx.f.setKuxMsg("XREF VALIDATION COMPLETE");
            } else {
                ctx.f.setKuxMsg("XREF REBUILD COMPLETE");
            }
        }
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OuxrefFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OuxrefFields(ws);
        }
    }
}

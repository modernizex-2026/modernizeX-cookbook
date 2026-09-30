package com.generated.orion.ournew.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.ournew.accessor.OurnewFields;
import com.generated.orion.ournew.metadata.OurnewBmsMetadata;
import com.generated.orion.ournew.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OURNEW. */
@Service
public class OurnewService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OurnewService.class);

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
        return "OURNEW";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OurnewBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OurnewBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        ctx.f.aliasGroup("KOPS-AREA", "CA-WORK-AREA");
        initializeProgram(ctx);
        computeCutoffDate(ctx);
        browseAndProcessCards(ctx);
        finalizeProgramStatus(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INITIALISE */
    private void initializeProgram(TaskContext ctx) {
        ctx.f.setString("KO-STATUS", "O");
        ctx.f.setKoStatusMsg(" ");
        ctx.f.setKoReadCnt(0);
        ctx.f.setKoSelectCnt(0);
        ctx.f.setKoUpdateCnt(0);
        ctx.f.setKoPostedCnt(0);
        ctx.f.setKoRejectCnt(0);
        ctx.f.setKoSkipCnt(0);
        ctx.f.setKoTranCnt(0);
        ctx.f.setKoC1(0);
        ctx.f.setKoC2(0);
        ctx.f.setKoC3(0);
        ctx.f.setKoAmt1(java.math.BigDecimal.valueOf(0));
        ctx.f.setKoAmt2(java.math.BigDecimal.valueOf(0));
        ctx.f.setKoAmt3(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsFilterOn("N");
        if (!Utility.fieldEquals(ctx.f.getKoParmCard(), " ")
                && !ctx.f.isAllLowValues("KO-PARM-CARD")) {
            ctx.f.setWsFilterCard(ctx.f.getKoParmCard());
            ctx.f.setString("WS-FILTER-ON", "Y");
        }
        ctx.f.setString("WS-BR-END-SW", "N");
        ctx.f.setString("WS-BR-STARTED-SW", "N");
    }

    /** COBOL paragraph: 1300-COMPUTE-CUTOFF */
    private void computeCutoffDate(TaskContext ctx) {
        ctx.f.setWsCurrRaw(
                (java.time.LocalDateTime.now()
                                .format(
                                        java.time.format.DateTimeFormatter.ofPattern(
                                                "yyyyMMddHHmmss"))
                        + "00+0000"));
        ctx.f.setWsCurrN(Utility.padRight(String.valueOf(ctx.f.getWsCurrRaw()), 8).substring(0, 8));
        ctx.f.setWsTodayYmd(
                (((ctx.f.getWcnYear() * 10000) + (ctx.f.getWcnMon() * 100)) + ctx.f.getWcnDay()));
        ctx.f.setWsTodayInt(
                Utility.toCobolInt((long) (Utility.integerOfDate(ctx.f.getWsTodayYmd())), 8));
        ctx.f.setWsCutoffInt((ctx.f.getWsTodayInt() + ctx.f.getWsLeadDays()));
        ctx.f.setWsCutoffYmd(
                Utility.toCobolInt((long) (Utility.dateOfInteger(ctx.f.getWsCutoffInt())), 8));
        ctx.f.setCsYear(ctx.f.getWcYear());
        ctx.f.setCsMm(ctx.f.getWcMm());
        ctx.f.setCsDd(ctx.f.getWcDd());
    }

    /** COBOL paragraph: 3000-BROWSE-DRIVER */
    private void browseAndProcessCards(TaskContext ctx) {
        startCardBrowse(ctx);
        if (ctx.f.getWsBrStartedSw().equals("Y")) {
            readNextCard(ctx);
            while (!(ctx.f.getWsBrEndSw().equals("Y") || ctx.f.getKoStatus().equals("E"))) {
                processCard(ctx);
            }
            endCardBrowse(ctx);
        }
    }

    /** COBOL paragraph: 3100-START-BROWSE */
    private void startCardBrowse(TaskContext ctx) {
        if (ctx.f.getWsFilterOn().equals("Y")) {
            ctx.f.setCdNum(ctx.f.getWsFilterCard());
        } else {
            ctx.f.fillLowValues("CD-NUM");
        }
        ctx.appService.startBrowse(ctx.f.getWsCardfile(), String.valueOf(ctx.f.getCdNum()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BR-STARTED-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-BR-END-SW", "Y");
            ctx.f.setKoStatusMsg("NO CARDS TO PROCESS.");
        } else {
            ctx.f.setString("WS-BR-END-SW", "Y");
            ctx.f.setString("KO-STATUS", "E");
            ctx.f.setKoStatusMsg("STARTBR CARDFILE FAILED.");
        }
    }

    /** COBOL paragraph: 3200-READ-NEXT-CARD */
    private void readNextCard(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsCardfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKoReadCnt(ctx.f.getKoReadCnt() + 1);
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-BR-END-SW", "Y");
        } else {
            ctx.f.setString("WS-BR-END-SW", "Y");
            ctx.f.setString("KO-STATUS", "E");
            ctx.f.setKoStatusMsg("READNEXT CARDFILE FAILED.");
        }
    }

    /** COBOL paragraph: 4000-PROCESS-CARD */
    private void processCard(TaskContext ctx) {
        if (ctx.f.getWsFilterOn().equals("Y")
                && !Utility.fieldEquals(ctx.f.getCdNum(), ctx.f.getWsFilterCard())) {
            ctx.f.setString("WS-BR-END-SW", "Y");
        } else {
            validateExpiryDate(ctx);
            if (!Utility.fieldEquals(ctx.f.getCdActiveStatus(), "Y")) {
                ctx.f.setKoC2(ctx.f.getKoC2() + 1);
            } else if (ctx.f.getWsExpValidSw().equals("N")) {
                ctx.f.setKoC3(ctx.f.getKoC3() + 1);
            } else if ((ctx.f.getCdExpiryDate().compareTo(ctx.f.getWsCutoffStr()) <= 0)) {
                reissueCard(ctx);
            } else {
                ctx.f.setKoC1(ctx.f.getKoC1() + 1);
            }
            readNextCard(ctx);
        }
    }

    /** COBOL paragraph: 4100-VALIDATE-EXPIRY */
    private void validateExpiryDate(TaskContext ctx) {
        ctx.f.setString("WS-EXP-VALID-SW", "Y");
        ctx.f.setWsExpYearX(
                Utility.padRight(String.valueOf(ctx.f.getCdExpiryDate()), 4).substring(0, 4));
        ctx.f.setWsExpMmX(
                Utility.padRight(String.valueOf(ctx.f.getCdExpiryDate()), 7).substring(5, 7));
        ctx.f.setWsExpDdX(
                Utility.padRight(String.valueOf(ctx.f.getCdExpiryDate()), 10).substring(8, 10));
        if (!Utility.isNumeric(ctx.f.getRawString("WS-EXP-YEAR-X"))) {
            ctx.f.setString("WS-EXP-VALID-SW", "N");
        }
        if (!Utility.isNumeric(ctx.f.getRawString("WS-EXP-MM-X"))) {
            ctx.f.setString("WS-EXP-VALID-SW", "N");
        }
        if (!Utility.isNumeric(ctx.f.getRawString("WS-EXP-DD-X"))) {
            ctx.f.setString("WS-EXP-VALID-SW", "N");
        }
    }

    /** COBOL paragraph: 4200-REISSUE-CARD */
    private void reissueCard(TaskContext ctx) {
        ctx.f.setWneYear((ctx.f.getWsExpYearN() + ctx.f.getWsExtendYears()));
        ctx.f.setWneMm(ctx.f.getWsExpMmX());
        ctx.f.setWneDd(ctx.f.getWsExpDdX());
        if (Utility.fieldEquals(ctx.f.getWneMm(), "02")
                && Utility.fieldEquals(ctx.f.getWneDd(), "29")) {
            adjustLeapYearDay(ctx);
        }
        ctx.appService.readFileForUpdate(
                ctx.f.getWsCardfile(), ctx.f, String.valueOf(ctx.f.getCdNum()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
            return;
        }
        ctx.f.copyBytes("CD-EXPIRY-DATE", "WS-NEW-EXP");
        ctx.appService.rewriteFile(ctx.f.getWsCardfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKoPostedCnt(ctx.f.getKoPostedCnt() + 1);
            ctx.f.setKoSelectCnt(ctx.f.getKoSelectCnt() + 1);
            ctx.f.setKoUpdateCnt(ctx.f.getKoUpdateCnt() + 1);
        } else {
            ctx.f.setKoRejectCnt(ctx.f.getKoRejectCnt() + 1);
        }
    }

    /** COBOL paragraph: 4250-ADJUST-LEAP */
    private void adjustLeapYearDay(TaskContext ctx) {
        ctx.f.setWsLeapR4((ctx.f.getWneYear() % 4));
        ctx.f.setWsLeapR100((ctx.f.getWneYear() % 100));
        ctx.f.setWsLeapR400((ctx.f.getWneYear() % 400));
        if (ctx.f.getWsLeapR4() == 0
                && (ctx.f.getWsLeapR100() != 0 || ctx.f.getWsLeapR400() == 0)) {
            /* CONTINUE */
        } else {
            ctx.f.setWneDd("28");
        }
    }

    /** COBOL paragraph: 3400-END-BROWSE */
    private void endCardBrowse(TaskContext ctx) {
        if (ctx.f.getWsBrStartedSw().equals("Y")) {
            ctx.appService.endBrowse(ctx.f.getWsCardfile());
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        }
    }

    /** COBOL paragraph: 9000-FINALISE */
    private void finalizeProgramStatus(TaskContext ctx) {
        if (ctx.f.getKoStatus().equals("E")) {
            /* CONTINUE */
        } else {
            if (ctx.f.getKoRejectCnt() > 0) {
                ctx.f.setString("KO-STATUS", "W");
            } else {
                ctx.f.setString("KO-STATUS", "O");
            }
            ctx.f.setKoStatusMsg("CARD RENEWAL PROCESSING COMPLETE.");
        }
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OurnewFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OurnewFields(ws);
        }
    }
}

package com.generated.orion.oucrdin.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.oucrdin.accessor.OucrdinFields;
import com.generated.orion.oucrdin.metadata.OucrdinBmsMetadata;
import com.generated.orion.oucrdin.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUCRDIN. */
@Service
public class OucrdinService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OucrdinService.class);

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
                    ctx.f.writeBytes("KCRDIN-AREA", (byte[]) _params[0]);
                } else if (_params.length > 0 && _params[0] != null) {
                    ctx.f.setGroup("KCRDIN-AREA", String.valueOf(_params[0]));
                }
            } else if (_commarea instanceof byte[]) {
                ctx.f.writeBytes("KCRDIN-AREA", (byte[]) _commarea);
            } else {
                ctx.f.setGroup("KCRDIN-AREA", String.valueOf(_commarea));
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
                                        ? (Object) ctx.f.sliceBytes("KCRDIN-AREA")
                                        : (Object) ctx.f.groupToString("KCRDIN-AREA");
                    }
                } else {
                    ctx.appService.setCommarea(ctx.f.sliceBytes("KCRDIN-AREA"));
                }
            }
        }
    }

    @Override
    public String getProgramName() {
        return "OUCRDIN";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OucrdinBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OucrdinBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        initialiseWorkingStorage(ctx);
        computeCutoffDate(ctx);
        runBrowseDriver(ctx);
        computeKpiCounts(ctx);
        finaliseResponse(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INITIALISE */
    private void initialiseWorkingStorage(TaskContext ctx) {
        ctx.f.setString("KCI-RETURN-CD", "N");
        ctx.f.setKciRowCount(0);
        ctx.f.setKciScanCount(0);
        ctx.f.setKciActiveCnt(0);
        ctx.f.setKciInactiveCnt(0);
        ctx.f.setString("KCI-MORE-SW", "N");
        ctx.f.fillLowValues("KCI-NEXT-KEY");
        ctx.f.fillLowValues("WS-LAST-MATCH");
        ctx.f.setString("WS-BR-END-SW", "N");
        ctx.f.setString("WS-BR-STARTED-SW", "N");
    }

    /** COBOL paragraph: 2000-COMPUTE-CUTOFF */
    private void computeCutoffDate(TaskContext ctx) {
        ctx.f.setWsCdt(
                (java.time.LocalDateTime.now()
                                .format(
                                        java.time.format.DateTimeFormatter.ofPattern(
                                                "yyyyMMddHHmmss"))
                        + "00+0000"));
        ctx.f.setWsTodayN(
                Utility.parseNumeric(
                                Utility.padRight(String.valueOf(ctx.f.getWsCdt()), 8)
                                        .substring(0, 8))
                        .intValue());
        ctx.f.setWsTodayInt(
                Utility.toCobolInt((long) (Utility.integerOfDate(ctx.f.getWsTodayN())), 9));
        ctx.f.setWsCutInt((ctx.f.getWsTodayInt() + 60));
    }

    /** COBOL paragraph: 3000-BROWSE-DRIVER */
    private void runBrowseDriver(TaskContext ctx) {
        startCardBrowse(ctx);
        if (isBrowseStarted(ctx)) {
            if (!ctx.f.isAllLowValues("KCI-START-KEY")) {
                readNextCard(ctx);
            }
            while (!(isBrowseEnded(ctx) || ctx.f.getKciRowCount() >= ctx.f.getWsMaxRows())) {
                gatherPage(ctx);
            }
            if (isBrowseEnded(ctx)) {
                ctx.f.setString("KCI-MORE-SW", "N");
            } else {
                ctx.f.setString("KCI-MORE-SW", "Y");
            }
            if (ctx.f.getKciRowCount() > 0) {
                ctx.f.setKciNextKey(ctx.f.getWsLastMatch());
            }
            endCardBrowse(ctx);
        }
    }

    /** COBOL paragraph: 3100-START-BROWSE */
    private void startCardBrowse(TaskContext ctx) {
        ctx.f.setWsBrowseKey(ctx.f.getKciStartKey());
        ctx.appService.startBrowse(
                ctx.f.getWsCardfile(), String.valueOf(ctx.f.getWsBrowseKey()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BR-STARTED-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-BR-END-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-BR-END-SW", "Y");
        } else {
            ctx.f.setString("WS-BR-END-SW", "Y");
            ctx.f.setString("KCI-RETURN-CD", "E");
            log.info(
                    "OUCRDIN: STARTBR RESP={}",
                    String.format("%+010d", (long) (ctx.f.getWsRespCd())));
        }
    }

    /** COBOL paragraph: 3200-GATHER-PAGE */
    private void gatherPage(TaskContext ctx) {
        readNextCard(ctx);
        if (ctx.f.getWsReadSw().equals("Y")) {
            ctx.f.setKciScanCount(ctx.f.getKciScanCount() + 1);
            deriveExpiryFigures(ctx);
            applyRowFilter(ctx);
            if (ctx.f.getWsMatchSw().equals("Y")) {
                storeMatchedRow(ctx);
            }
        }
    }

    /** COBOL paragraph: 3300-READ-NEXT */
    private void readNextCard(TaskContext ctx) {
        ctx.f.setString("WS-READ-SW", "N");
        ctx.appService.readNext(ctx.f.getWsCardfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-READ-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-BR-END-SW", "Y");
        } else {
            ctx.f.setString("WS-BR-END-SW", "Y");
            ctx.f.setString("KCI-RETURN-CD", "E");
            log.info(
                    "OUCRDIN: READNEXT RESP={}",
                    String.format("%+010d", (long) (ctx.f.getWsRespCd())));
        }
    }

    /** COBOL paragraph: 3400-END-BROWSE */
    private void endCardBrowse(TaskContext ctx) {
        if (isBrowseStarted(ctx)) {
            ctx.appService.endBrowse(ctx.f.getWsCardfile());
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        }
    }

    /** COBOL paragraph: 4000-DERIVE-FIGURES */
    private void deriveExpiryFigures(TaskContext ctx) {
        ctx.f.setString("WS-EXP-VALID-SW", "N");
        ctx.f.setWsExpInt(0);
        String expiryDate = Utility.padRight(String.valueOf(ctx.f.getCdExpiryDate()), 10);
        String yyyy = expiryDate.substring(0, 4);
        String mm = expiryDate.substring(5, 7);
        String dd = expiryDate.substring(8, 10);
        if (Utility.isNumeric(yyyy) && Utility.isNumeric(mm) && Utility.isNumeric(dd)) {
            ctx.f.setWeYyyy(Utility.parseNumeric(yyyy).intValue());
            ctx.f.setWeMm(Utility.parseNumeric(mm).intValue());
            ctx.f.setWeDd(Utility.parseNumeric(dd).intValue());
            ctx.f.setWsExpInt(
                    Utility.toCobolInt((long) (Utility.integerOfDate(ctx.f.getWsExpN())), 9));
            ctx.f.setString("WS-EXP-VALID-SW", "Y");
        }
    }

    /** COBOL paragraph: 4500-APPLY-FILTER */
    private void applyRowFilter(TaskContext ctx) {
        ctx.f.setString("WS-MATCH-SW", "N");
        if (ctx.f.getKciFilter().equals("A")) {
            matchAllRows(ctx);
        } else if (ctx.f.getKciFilter().equals("Y")) {
            matchActiveOnly(ctx);
        } else if (ctx.f.getKciFilter().equals("N")) {
            matchInactiveOnly(ctx);
        } else if (ctx.f.getKciFilter().equals("X")) {
            matchExpiringSoon(ctx);
        } else {
            matchAllRows(ctx);
        }
    }

    /** COBOL paragraph: 4510-TEST-ALL */
    private void matchAllRows(TaskContext ctx) {
        ctx.f.setString("WS-MATCH-SW", "Y");
    }

    /** COBOL paragraph: 4520-TEST-ACTIVE */
    private void matchActiveOnly(TaskContext ctx) {
        if (isCardActive(ctx)) {
            ctx.f.setString("WS-MATCH-SW", "Y");
        }
    }

    /** COBOL paragraph: 4530-TEST-INACTIVE */
    private void matchInactiveOnly(TaskContext ctx) {
        if (!isCardActive(ctx)) {
            ctx.f.setString("WS-MATCH-SW", "Y");
        }
    }

    /** COBOL paragraph: 4540-TEST-EXPIRING */
    private void matchExpiringSoon(TaskContext ctx) {
        if (ctx.f.getWsExpValidSw().equals("Y")
                && ctx.f.getWsExpInt() >= ctx.f.getWsTodayInt()
                && ctx.f.getWsExpInt() <= ctx.f.getWsCutInt()) {
            ctx.f.setString("WS-MATCH-SW", "Y");
        }
    }

    /** COBOL paragraph: 5000-STORE-ROW */
    private void storeMatchedRow(TaskContext ctx) {
        ctx.f.setKciRowCount(ctx.f.getKciRowCount() + 1);
        ctx.f.setKciRNum(ctx.f.getKciRowCount(), ctx.f.getCdNum());
        ctx.f.setKciRAcct(ctx.f.getKciRowCount(), ctx.f.getCdAcctId());
        ctx.f.setKciRName(ctx.f.getKciRowCount(), ctx.f.getCdEmbossedName());
        ctx.f.setKciRExpiry(ctx.f.getKciRowCount(), ctx.f.getCdExpiryDate());
        ctx.f.setKciRStatus(ctx.f.getKciRowCount(), ctx.f.getCdActiveStatus());
        ctx.f.setWsLastMatch(ctx.f.getCdNum());
    }

    /** COBOL paragraph: 6000-COMPUTE-KPI */
    private void computeKpiCounts(TaskContext ctx) {
        if (ctx.f.getKciWantKpi().equals("Y")) {
            ctx.f.setKciActiveCnt(0);
            ctx.f.setKciInactiveCnt(0);
            startKpiBrowse(ctx);
            if (isBrowseStarted(ctx)) {
                while (!isBrowseEnded(ctx)) {
                    readKpiRecord(ctx);
                }
                endKpiBrowse(ctx);
            }
        }
    }

    /** COBOL paragraph: 6100-KPI-START */
    private void startKpiBrowse(TaskContext ctx) {
        ctx.f.setString("WS-BR-END-SW", "N");
        ctx.f.setString("WS-BR-STARTED-SW", "N");
        ctx.f.fillLowValues("WS-BROWSE-KEY");
        ctx.appService.startBrowse(
                ctx.f.getWsCardfile(), String.valueOf(ctx.f.getWsBrowseKey()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BR-STARTED-SW", "Y");
        } else {
            ctx.f.setString("WS-BR-END-SW", "Y");
        }
    }

    /** COBOL paragraph: 6200-KPI-READ */
    private void readKpiRecord(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsCardfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            deriveExpiryFigures(ctx);
            applyRowFilter(ctx);
            if (ctx.f.getWsMatchSw().equals("Y")) {
                if (isCardActive(ctx)) {
                    ctx.f.setKciActiveCnt(ctx.f.getKciActiveCnt() + 1);
                } else {
                    ctx.f.setKciInactiveCnt(ctx.f.getKciInactiveCnt() + 1);
                }
            }
        } else {
            ctx.f.setString("WS-BR-END-SW", "Y");
        }
    }

    /** COBOL paragraph: 6300-KPI-END */
    private void endKpiBrowse(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsCardfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 9000-FINALISE */
    private void finaliseResponse(TaskContext ctx) {
        if (ctx.f.getKciReturnCd().equals("E")) {
            /* CONTINUE */
        } else {
            ctx.f.setString("KCI-RETURN-CD", "N");
        }
        log.info(
                "OUCRDIN: filter {} matched {} scanned {}",
                ctx.f.getKciFilter(),
                String.format("%02d", (long) (ctx.f.getKciRowCount())),
                String.format("%07d", (long) (ctx.f.getKciScanCount())));
    }

    /** Whether the card-file browse has been successfully started. */
    private boolean isBrowseStarted(TaskContext ctx) {
        return ctx.f.getWsBrStartedSw().equals("Y");
    }

    /** Whether the card-file browse has reached its end condition. */
    private boolean isBrowseEnded(TaskContext ctx) {
        return ctx.f.getWsBrEndSw().equals("Y");
    }

    /** Whether the current card record's status is active. */
    private boolean isCardActive(TaskContext ctx) {
        return Utility.fieldEquals(ctx.f.getCdActiveStatus(), "Y");
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OucrdinFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OucrdinFields(ws);
        }
    }
}

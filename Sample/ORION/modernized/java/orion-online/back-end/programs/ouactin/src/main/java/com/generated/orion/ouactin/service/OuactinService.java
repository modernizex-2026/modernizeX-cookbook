package com.generated.orion.ouactin.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.ouactin.accessor.OuactinFields;
import com.generated.orion.ouactin.metadata.OuactinBmsMetadata;
import com.generated.orion.ouactin.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUACTIN. */
@Service
public class OuactinService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OuactinService.class);

    private static final java.math.BigDecimal UTIL_CAP_MAX = new java.math.BigDecimal("999.99");

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
                    ctx.f.writeBytes("KACTIN-AREA", (byte[]) _params[0]);
                } else if (_params.length > 0 && _params[0] != null) {
                    ctx.f.setGroup("KACTIN-AREA", String.valueOf(_params[0]));
                }
            } else if (_commarea instanceof byte[]) {
                ctx.f.writeBytes("KACTIN-AREA", (byte[]) _commarea);
            } else {
                ctx.f.setGroup("KACTIN-AREA", String.valueOf(_commarea));
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
                                        ? (Object) ctx.f.sliceBytes("KACTIN-AREA")
                                        : (Object) ctx.f.groupToString("KACTIN-AREA");
                    }
                } else {
                    ctx.appService.setCommarea(ctx.f.sliceBytes("KACTIN-AREA"));
                }
            }
        }
    }

    @Override
    public String getProgramName() {
        return "OUACTIN";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OuactinBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OuactinBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        initializeProgram(ctx);
        computeCutoffDate(ctx);
        driveAccountBrowse(ctx);
        computeKpiTotals(ctx);
        finalizeProgram(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INITIALISE */
    private void initializeProgram(TaskContext ctx) {
        ctx.f.setString("KAI-RETURN-CD", "N");
        ctx.f.setKaiRowCount(0);
        ctx.f.setKaiScanCount(0);
        ctx.f.setString("KAI-MORE-SW", "N");
        ctx.f.setKaiNextKey(0);
        ctx.f.setKaiPopCount(0);
        ctx.f.setKaiPopBalTot(java.math.BigDecimal.valueOf(0));
        ctx.f.setKaiPopAvlTot(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsLastMatch(0);
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
        ctx.f.setWsCutInt((ctx.f.getWsTodayInt() - 90));
    }

    /** COBOL paragraph: 3000-BROWSE-DRIVER */
    private void driveAccountBrowse(TaskContext ctx) {
        startAccountBrowse(ctx);
        if (ctx.f.getWsBrStartedSw().equals("Y")) {
            while (!isBrowseEnded(ctx) && ctx.f.getKaiRowCount() < ctx.f.getWsMaxRows()) {
                gatherAccountPage(ctx);
            }
            if (isBrowseEnded(ctx)) {
                ctx.f.setString("KAI-MORE-SW", "N");
            } else {
                ctx.f.setString("KAI-MORE-SW", "Y");
            }
            if (ctx.f.getKaiRowCount() > 0) {
                ctx.f.setKaiNextKey((ctx.f.getWsLastMatch() + 1));
            }
            endAccountBrowse(ctx);
        }
    }

    /** COBOL paragraph: 3100-START-BROWSE */
    private void startAccountBrowse(TaskContext ctx) {
        ctx.f.setAcId(ctx.f.getKaiStartKey());
        ctx.appService.startBrowse(ctx.f.getWsAcctfile(), String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BR-STARTED-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-BR-END-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-BR-END-SW", "Y");
        } else {
            ctx.f.setString("WS-BR-END-SW", "Y");
            ctx.f.setString("KAI-RETURN-CD", "E");
            log.info(
                    "OUACTIN: STARTBR RESP={}",
                    String.format("%+010d", (long) (ctx.f.getWsRespCd())));
        }
    }

    /** COBOL paragraph: 3200-GATHER-PAGE */
    private void gatherAccountPage(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsAcctfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKaiScanCount(ctx.f.getKaiScanCount() + 1);
            deriveAccountFigures(ctx);
            applyRowFilter(ctx);
            if (ctx.f.getWsMatchSw().equals("Y")) {
                storeMatchedRow(ctx);
            }
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-BR-END-SW", "Y");
        } else {
            ctx.f.setString("WS-BR-END-SW", "Y");
            ctx.f.setString("KAI-RETURN-CD", "E");
            log.info(
                    "OUACTIN: READNEXT RESP={}",
                    String.format("%+010d", (long) (ctx.f.getWsRespCd())));
        }
    }

    /** COBOL paragraph: 3400-END-BROWSE */
    private void endAccountBrowse(TaskContext ctx) {
        if (ctx.f.getWsBrStartedSw().equals("Y")) {
            ctx.appService.endBrowse(ctx.f.getWsAcctfile());
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        }
    }

    /** COBOL paragraph: 4000-DERIVE-FIGURES */
    private void deriveAccountFigures(TaskContext ctx) {
        ctx.f.setWsAvail(ctx.f.getAcCreditLimit().subtract(ctx.f.getAcCurrBal()));
        if ((ctx.f.getAcCreditLimit().signum() > 0) && (ctx.f.getAcCurrBal().signum() > 0)) {
            ctx.f.setWsUtilBig(
                    ctx.f
                            .getAcCurrBal()
                            .multiply(java.math.BigDecimal.valueOf(100))
                            .divide(ctx.f.getAcCreditLimit(), 12, java.math.RoundingMode.HALF_UP));
            if ((ctx.f.getWsUtilBig().compareTo(UTIL_CAP_MAX) > 0)) {
                ctx.f.setWsUtilr(UTIL_CAP_MAX);
            } else {
                ctx.f.setWsUtilr(ctx.f.getWsUtilBig());
            }
        } else {
            ctx.f.setWsUtilr(java.math.BigDecimal.valueOf(0));
        }
        ctx.f.setWsMinDue(ctx.f.getAcCurrBal().multiply(new java.math.BigDecimal("0.02")));
        if ((ctx.f.getAcCurrBal().signum() > 0)
                && (ctx.f.getWsMinDue().compareTo(ctx.f.getWsMinFloor()) < 0)) {
            ctx.f.setWsMinDue(ctx.f.getWsMinFloor());
        }
    }

    /** COBOL paragraph: 4500-APPLY-FILTER */
    private void applyRowFilter(TaskContext ctx) {
        ctx.f.setString("WS-MATCH-SW", "N");
        if (ctx.f.getKaiFilter().equals("A")) {
            matchAllFilter(ctx);
        } else if (ctx.f.getKaiFilter().equals("D")) {
            matchDelinquentFilter(ctx);
        } else if (ctx.f.getKaiFilter().equals("O")) {
            matchOverLimitFilter(ctx);
        } else if (ctx.f.getKaiFilter().equals("M")) {
            matchDormantFilter(ctx);
        } else if (ctx.f.getKaiFilter().equals("C")) {
            matchClosedFilter(ctx);
        } else if (ctx.f.getKaiFilter().equals("N")) {
            matchNewAccountFilter(ctx);
        } else if (ctx.f.getKaiFilter().equals("H")) {
            matchHighUtilizationFilter(ctx);
        } else {
            matchAllFilter(ctx);
        }
    }

    /** COBOL paragraph: 4510-TEST-ALL */
    private void matchAllFilter(TaskContext ctx) {
        ctx.f.setString("WS-MATCH-SW", "Y");
    }

    /** COBOL paragraph: 4520-TEST-DELINQ */
    private void matchDelinquentFilter(TaskContext ctx) {
        if ((ctx.f.getAcCurrBal().signum() > 0)
                && (ctx.f.getAcCycCredit().compareTo(ctx.f.getWsMinDue()) < 0)) {
            ctx.f.setString("WS-MATCH-SW", "Y");
        }
    }

    /** COBOL paragraph: 4530-TEST-OVERLMT */
    private void matchOverLimitFilter(TaskContext ctx) {
        if ((ctx.f.getAcCurrBal().compareTo(ctx.f.getAcCreditLimit()) > 0)) {
            ctx.f.setString("WS-MATCH-SW", "Y");
        }
    }

    /** COBOL paragraph: 4540-TEST-DORMANT */
    private void matchDormantFilter(TaskContext ctx) {
        if (ctx.f.getAcCycCredit().signum() == 0 && ctx.f.getAcCycDebit().signum() == 0) {
            ctx.f.setString("WS-MATCH-SW", "Y");
        }
    }

    /** COBOL paragraph: 4550-TEST-CLOSED */
    private void matchClosedFilter(TaskContext ctx) {
        if (!Utility.fieldEquals(ctx.f.getAcActiveStatus(), "Y")) {
            ctx.f.setString("WS-MATCH-SW", "Y");
        }
    }

    /** COBOL paragraph: 4560-TEST-NEW */
    private void matchNewAccountFilter(TaskContext ctx) {
        if (Utility.isNumeric(
                        String.valueOf(
                                Utility.padRight(String.valueOf(ctx.f.getAcOpenDate()), 4)
                                        .substring(0, 4)))
                && Utility.isNumeric(
                        String.valueOf(
                                Utility.padRight(String.valueOf(ctx.f.getAcOpenDate()), 7)
                                        .substring(5, 7)))
                && Utility.isNumeric(
                        String.valueOf(
                                Utility.padRight(String.valueOf(ctx.f.getAcOpenDate()), 10)
                                        .substring(8, 10)))) {
            ctx.f.setWoYyyy(
                    Utility.parseNumeric(
                                    Utility.padRight(String.valueOf(ctx.f.getAcOpenDate()), 4)
                                            .substring(0, 4))
                            .intValue());
            ctx.f.setWoMm(
                    Utility.parseNumeric(
                                    Utility.padRight(String.valueOf(ctx.f.getAcOpenDate()), 7)
                                            .substring(5, 7))
                            .intValue());
            ctx.f.setWoDd(
                    Utility.parseNumeric(
                                    Utility.padRight(String.valueOf(ctx.f.getAcOpenDate()), 10)
                                            .substring(8, 10))
                            .intValue());
            ctx.f.setWsOpenInt(
                    Utility.toCobolInt((long) (Utility.integerOfDate(ctx.f.getWsOpenN())), 9));
            if (ctx.f.getWsOpenInt() >= ctx.f.getWsCutInt()) {
                ctx.f.setString("WS-MATCH-SW", "Y");
            }
        }
    }

    /** COBOL paragraph: 4570-TEST-HIGHUTL */
    private void matchHighUtilizationFilter(TaskContext ctx) {
        if ((ctx.f.getWsUtilr().compareTo(new java.math.BigDecimal("80.00")) >= 0)) {
            ctx.f.setString("WS-MATCH-SW", "Y");
        }
    }

    /** COBOL paragraph: 5000-STORE-ROW */
    private void storeMatchedRow(TaskContext ctx) {
        ctx.f.setKaiRowCount(ctx.f.getKaiRowCount() + 1);
        ctx.f.setKaiRId(ctx.f.getKaiRowCount(), ctx.f.getAcId());
        ctx.f.setKaiRStatus(ctx.f.getKaiRowCount(), ctx.f.getAcActiveStatus());
        ctx.f.setKaiRBal(ctx.f.getKaiRowCount(), ctx.f.getAcCurrBal());
        ctx.f.setKaiRLimit(ctx.f.getKaiRowCount(), ctx.f.getAcCreditLimit());
        ctx.f.setKaiRAvail(ctx.f.getKaiRowCount(), ctx.f.getWsAvail());
        ctx.f.setKaiRUtil(ctx.f.getKaiRowCount(), ctx.f.getWsUtilr());
        ctx.f.setWsLastMatch(ctx.f.getAcId());
    }

    /** COBOL paragraph: 6000-COMPUTE-KPI */
    private void computeKpiTotals(TaskContext ctx) {
        if (ctx.f.getKaiWantKpi().equals("Y")) {
            ctx.f.setWsKpiCount(0);
            ctx.f.setWsKpiBal(java.math.BigDecimal.valueOf(0));
            ctx.f.setWsKpiAvl(java.math.BigDecimal.valueOf(0));
            startKpiBrowse(ctx);
            if (ctx.f.getWsBrStartedSw().equals("Y")) {
                while (!isBrowseEnded(ctx)) {
                    readKpiRow(ctx);
                }
                endKpiBrowse(ctx);
            }
            ctx.f.setKaiPopCount(ctx.f.getWsKpiCount());
            ctx.f.setKaiPopBalTot(ctx.f.getWsKpiBal());
            ctx.f.setKaiPopAvlTot(ctx.f.getWsKpiAvl());
        }
    }

    /** COBOL paragraph: 6100-KPI-START */
    private void startKpiBrowse(TaskContext ctx) {
        ctx.f.setString("WS-BR-END-SW", "N");
        ctx.f.setString("WS-BR-STARTED-SW", "N");
        ctx.f.setAcId(0);
        ctx.appService.startBrowse(ctx.f.getWsAcctfile(), String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BR-STARTED-SW", "Y");
        } else {
            ctx.f.setString("WS-BR-END-SW", "Y");
        }
    }

    /** COBOL paragraph: 6200-KPI-READ */
    private void readKpiRow(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsAcctfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            deriveAccountFigures(ctx);
            applyRowFilter(ctx);
            if (ctx.f.getWsMatchSw().equals("Y")) {
                ctx.f.setWsKpiCount(ctx.f.getWsKpiCount() + 1);
                ctx.f.setWsKpiBal(ctx.f.getWsKpiBal().add(ctx.f.getAcCurrBal()));
                ctx.f.setWsKpiAvl(ctx.f.getWsKpiAvl().add(ctx.f.getWsAvail()));
            }
        } else {
            ctx.f.setString("WS-BR-END-SW", "Y");
        }
    }

    /** COBOL paragraph: 6300-KPI-END */
    private void endKpiBrowse(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsAcctfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 9000-FINALISE */
    private void finalizeProgram(TaskContext ctx) {
        if (!ctx.f.getKaiReturnCd().equals("E")) {
            ctx.f.setString("KAI-RETURN-CD", "N");
        }
        log.info(
                "OUACTIN: filter {} matched {} scanned {}",
                ctx.f.getKaiFilter(),
                String.format("%02d", (long) (ctx.f.getKaiRowCount())),
                String.format("%07d", (long) (ctx.f.getKaiScanCount())));
    }

    /** Whether the current account browse has reached end-of-file or an unrecoverable status. */
    private boolean isBrowseEnded(TaskContext ctx) {
        return ctx.f.getWsBrEndSw().equals("Y");
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OuactinFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OuactinFields(ws);
        }
    }
}

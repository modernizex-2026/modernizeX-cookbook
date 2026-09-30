package com.generated.orion.oucusin.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.oucusin.accessor.OucusinFields;
import com.generated.orion.oucusin.metadata.OucusinBmsMetadata;
import com.generated.orion.oucusin.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUCUSIN. */
@Service
public class OucusinService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OucusinService.class);

    /** Pad length used when comparing/truncating zip-code fields by prefix length. */
    private static final int ZIP_FIELD_PAD_LENGTH = 256;

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
                    ctx.f.writeBytes("KCUSIN-AREA", (byte[]) _params[0]);
                } else if (_params.length > 0 && _params[0] != null) {
                    ctx.f.setGroup("KCUSIN-AREA", String.valueOf(_params[0]));
                }
            } else if (_commarea instanceof byte[]) {
                ctx.f.writeBytes("KCUSIN-AREA", (byte[]) _commarea);
            } else {
                ctx.f.setGroup("KCUSIN-AREA", String.valueOf(_commarea));
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
                                        ? (Object) ctx.f.sliceBytes("KCUSIN-AREA")
                                        : (Object) ctx.f.groupToString("KCUSIN-AREA");
                    }
                } else {
                    ctx.appService.setCommarea(ctx.f.sliceBytes("KCUSIN-AREA"));
                }
            }
        }
    }

    @Override
    public String getProgramName() {
        return "OUCUSIN";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OucusinBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OucusinBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
        initializeResponse(ctx);
        browseCustomers(ctx);
        finalizeSummary(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INIT-RESPONSE */
    private void initializeResponse(TaskContext ctx) {
        ctx.f.setString("KUI-RETURN-CD", "N");
        ctx.f.setKuiRowCount(0);
        ctx.f.setKuiScanCount(0);
        ctx.f.setKuiMatchCount(0);
        ctx.f.setKuiFicoTot(0);
        ctx.f.setKuiFicoAvg(0);
        ctx.f.setKuiFicoMin(999);
        ctx.f.setKuiFicoMax(0);
        ctx.f.setKuiNextKey(0);
        ctx.f.setString("KUI-MORE-SW", "N");
        ctx.f.setWsLastKey(0);
        ctx.f.setString("WS-MATCH-SW", "N");
        ctx.f.setWsEndFlg("N");
        ctx.f.setKuiRows("");
    }

    /** COBOL paragraph: 2000-BROWSE-CUSTOMERS */
    private void browseCustomers(TaskContext ctx) {
        startCustomerBrowse(ctx);
        if (!ctx.f.getWsEndFlg().equals("Y")) {
            while (!ctx.f.getWsEndFlg().equals("Y")) {
                readNextCustomer(ctx);
            }
            endCustomerBrowse(ctx);
        }
        if (ctx.f.getKuiRowCount() > 0) {
            ctx.f.setKuiNextKey(ctx.f.getWsLastKey());
        }
    }

    /** COBOL paragraph: 2100-START-BROWSE */
    private void startCustomerBrowse(TaskContext ctx) {
        if (ctx.f.getKuiFilter().equals("I")) {
            ctx.f.setCuId(ctx.f.getKuiIdFrom());
        } else {
            ctx.f.setCuId(0);
        }
        ctx.appService.startBrowse(ctx.f.getWsCustfile(), String.valueOf(ctx.f.getCuId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            /* CONTINUE */
        } else if (ctx.f.getWsRespCd() == 13) {
            markEndOfBrowse(ctx);
        } else if (ctx.f.getWsRespCd() == 20) {
            markEndOfBrowse(ctx);
        } else {
            markEndOfBrowse(ctx);
            ctx.f.setString("KUI-RETURN-CD", "E");
        }
    }

    /** COBOL paragraph: 2200-READ-NEXT */
    private void readNextCustomer(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsCustfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            evaluateCustomerRecord(ctx);
        } else if (ctx.f.getWsRespCd() == 20) {
            markEndOfBrowse(ctx);
        } else {
            markEndOfBrowse(ctx);
            ctx.f.setString("KUI-RETURN-CD", "E");
        }
    }

    /** COBOL paragraph: 2250-HANDLE-RECORD */
    private void evaluateCustomerRecord(TaskContext ctx) {
        ctx.f.setKuiScanCount(ctx.f.getKuiScanCount() + 1);
        if (ctx.f.getKuiFilter().equals("I") && ctx.f.getCuId() > ctx.f.getKuiIdTo()) {
            markEndOfBrowse(ctx);
        } else {
            checkCustomerFilter(ctx);
            if (ctx.f.getWsMatchSw().equals("Y")) {
                ctx.f.setKuiMatchCount(ctx.f.getKuiMatchCount() + 1);
                ctx.f.setKuiFicoTot(ctx.f.getKuiFicoTot() + ctx.f.getCuFicoScore());
                if (ctx.f.getCuFicoScore() < ctx.f.getKuiFicoMin()) {
                    ctx.f.setKuiFicoMin(ctx.f.getCuFicoScore());
                }
                if (ctx.f.getCuFicoScore() > ctx.f.getKuiFicoMax()) {
                    ctx.f.setKuiFicoMax(ctx.f.getCuFicoScore());
                }
                if (ctx.f.getCuId() > ctx.f.getKuiStartKey()) {
                    if (ctx.f.getKuiRowCount() < ctx.f.getWsMaxRows()) {
                        storeMatchedRow(ctx);
                    } else {
                        ctx.f.setString("KUI-MORE-SW", "Y");
                    }
                }
            }
        }
    }

    /** COBOL paragraph: 2300-END-BROWSE */
    private void endCustomerBrowse(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsCustfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 2400-FILTER-CHECK */
    private void checkCustomerFilter(TaskContext ctx) {
        ctx.f.setString("WS-MATCH-SW", "N");
        if (ctx.f.getKuiFilter().equals("I")) {
            if (ctx.f.getCuId() >= ctx.f.getKuiIdFrom() && ctx.f.getCuId() <= ctx.f.getKuiIdTo()) {
                markMatchFound(ctx);
            }
        } else if (ctx.f.getKuiFilter().equals("F")) {
            if (ctx.f.getCuFicoScore() >= ctx.f.getKuiFicoFrom()
                    && ctx.f.getCuFicoScore() <= ctx.f.getKuiFicoTo()) {
                markMatchFound(ctx);
            }
        } else if (ctx.f.getKuiFilter().equals("S")) {
            checkStateZipMatch(ctx);
        } else {
            /* CONTINUE */
        }
    }

    /** COBOL paragraph: 2450-CHECK-STATE-ZIP */
    private void checkStateZipMatch(TaskContext ctx) {
        if (!Utility.fieldEquals(ctx.f.getCuAddrState(), ctx.f.getKuiState())) {
            return;
        }
        ctx.f.setWsZiplen(0);
        for (ctx.f.setWsI(1); ctx.f.getWsI() <= 10; ctx.f.setWsI(ctx.f.getWsI() + 1)) {
            if (!Utility.fieldEquals(
                    Utility.padRight(String.valueOf(ctx.f.getKuiZip()), ZIP_FIELD_PAD_LENGTH)
                            .substring(ctx.f.getWsI() - 1, ctx.f.getWsI() - 1 + 1),
                    " ")) {
                ctx.f.setWsZiplen(ctx.f.getWsI());
            }
        }
        if (ctx.f.getWsZiplen() == 0) {
            markMatchFound(ctx);
        } else {
            if (Utility.fieldEquals(
                    Utility.padRight(String.valueOf(ctx.f.getCuAddrZip()), ZIP_FIELD_PAD_LENGTH)
                            .substring(0, ctx.f.getWsZiplen()),
                    Utility.padRight(String.valueOf(ctx.f.getKuiZip()), ZIP_FIELD_PAD_LENGTH)
                            .substring(0, ctx.f.getWsZiplen()))) {
                markMatchFound(ctx);
            }
        }
    }

    /** COBOL paragraph: 2500-STORE-ROW */
    private void storeMatchedRow(TaskContext ctx) {
        ctx.f.setKuiRowCount(ctx.f.getKuiRowCount() + 1);
        ctx.f.setWsLastKey(ctx.f.getCuId());
        buildCustomerName(ctx);
        ctx.f.setKurId(ctx.f.getKuiRowCount(), ctx.f.getCuId());
        ctx.f.setKurName(ctx.f.getKuiRowCount(), ctx.f.getWsName());
        ctx.f.setKurState(ctx.f.getKuiRowCount(), ctx.f.getCuAddrState());
        ctx.f.setKurZip(ctx.f.getKuiRowCount(), ctx.f.getCuAddrZip());
        ctx.f.setKurFico(ctx.f.getKuiRowCount(), ctx.f.getCuFicoScore());
    }

    /** COBOL paragraph: 2600-BUILD-NAME */
    private void buildCustomerName(TaskContext ctx) {
        ctx.f.setWsName(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(ctx.f.getCuFirstName()).split(String.valueOf("  "), 2)[0]);
            sb.append(String.valueOf(" "));
            sb.append(String.valueOf(ctx.f.getCuLastName()).split(String.valueOf("  "), 2)[0]);
            ctx.f.setWsName(sb.toString());
        }
    }

    /** COBOL paragraph: 3000-FINALIZE */
    private void finalizeSummary(TaskContext ctx) {
        if (ctx.f.getKuiMatchCount() > 0) {
            ctx.f.setKuiFicoAvg(
                    (int)
                            ((int)
                                    Math.round(
                                            (double)
                                                    ((ctx.f.getKuiFicoTot()
                                                            / ctx.f.getKuiMatchCount())))));
        } else {
            ctx.f.setKuiFicoAvg(0);
            ctx.f.setKuiFicoMin(0);
        }
    }

    /** Signal end-of-browse so the customer-scan loop terminates. */
    private void markEndOfBrowse(TaskContext ctx) {
        ctx.f.setString("WS-END-FLG", "Y");
    }

    /** Mark the current customer record as matching the active filter. */
    private void markMatchFound(TaskContext ctx) {
        ctx.f.setString("WS-MATCH-SW", "Y");
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OucusinFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OucusinFields(ws);
        }
    }
}

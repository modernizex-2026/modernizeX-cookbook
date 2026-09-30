package com.generated.orion.ocrept.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcreptLinkParm;
import com.generated.orion.ocrept.accessor.OcreptFields;
import com.generated.orion.ocrept.metadata.OcreptBmsMetadata;
import com.generated.orion.ocrept.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCREPT. */
@Service
public class OcreptService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcreptService.class);

    private static final int COMMAREA_LENGTH = 692;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        _0000Main(ctx);
    }

    @Override
    public String getProgramName() {
        return "OCREPT";
    }

    @Override
    public String getTransId() {
        return "ORRP";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcreptBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcreptBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcreptBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void _0000Main(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        if (ctx.appService.getEibcalen() == 0) {
            ctx.f.setOrionCommarea("");
            _1000SendInitial(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            ctx.f.setCaErrMsg(" ");
            if (ctx.f.getCaPgmContext() == 0) {
                _1000SendInitial(ctx);
            } else {
                _2000ProcessInput(ctx);
            }
        }
        _9000Return(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void _1000SendInitial(TaskContext ctx) {
        ctx.f.fillLowValues("MREPTAO");
        _8000PopulateHeader(ctx);
        ctx.f.setErrmsgo("Type 01=Bills 02=Trans, dates YYYY-MM-DD.");
        ctx.appService.sendMap(
                "MREPTA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void _2000ProcessInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            _7000XctlMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            _1000SendInitial(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            _2100RunReport(ctx);
        } else {
            _1000SendInitial(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            _8100SendDataonly(ctx);
        }
    }

    /** COBOL paragraph: 2100-RUN-REPORT */
    private void _2100RunReport(TaskContext ctx) {
        _2110ReceiveMap(ctx);
        _2120ValidateType(ctx);
        if (hasError(ctx)) {
            _8100SendDataonly(ctx);
            return;
        }
        _2130ValidateDates(ctx);
        if (hasError(ctx)) {
            _8100SendDataonly(ctx);
            return;
        }
        ctx.f.setWsTotal(java.math.BigDecimal.valueOf(0));
        ctx.f.setWsRptCount(0);
        if (ctx.f.getWsRptType().equals("01")) {
            _2200ReportBills(ctx);
        } else if (ctx.f.getWsRptType().equals("02")) {
            _2300ReportTrans(ctx);
        } else {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Unsupported report type.");
        }
        if (!hasError(ctx)) {
            _2400BuildSummary(ctx);
        }
        _8100SendDataonly(ctx);
    }

    /** COBOL paragraph: 2110-RECEIVE-MAP */
    private void _2110ReceiveMap(TaskContext ctx) {
        ctx.appService.receiveMap("MREPTA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 2120-VALIDATE-TYPE */
    private void _2120ValidateType(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        if (Utility.fieldEquals(ctx.f.getRptypei(), " ") || ctx.f.isAllLowValues("RPTYPEI")) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Report type is required.");
            return;
        }
        ctx.f.setWsRptType(ctx.f.getRptypei());
        if (!(ctx.f.getWsRptType().equals("01") || ctx.f.getWsRptType().equals("02"))) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Report type must be 01 or 02.");
        }
    }

    /** COBOL paragraph: 2130-VALIDATE-DATES */
    private void _2130ValidateDates(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        if (Utility.fieldEquals(ctx.f.getRpfromi(), " ")
                || ctx.f.isAllLowValues("RPFROMI")
                || Utility.fieldEquals(ctx.f.getRptoi(), " ")
                || ctx.f.isAllLowValues("RPTOI")) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("From and to dates are required.");
            return;
        }
        ctx.f.setWsFromDate(ctx.f.getRpfromi());
        ctx.f.setWsToDate(ctx.f.getRptoi());
        ctx.f.setKdFunc("VALD");
        ctx.f.setKdDateIn(ctx.f.getWsFromDate());
        ctx.f.setKdDateOut(" ");
        Object[] _ca0 = new Object[] {ctx.f.getKdateParm()};
        ctx.appService.callProgram("OUDATE", _ca0);
        ctx.f.setKdateParm(String.valueOf(_ca0[0]));
        if (!Utility.fieldEquals(ctx.f.getKdStatus(), "00")) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("From date is not a valid date.");
            return;
        }
        ctx.f.setKdFunc("VALD");
        ctx.f.setKdDateIn(ctx.f.getWsToDate());
        ctx.f.setKdDateOut(" ");
        Object[] _ca1 = new Object[] {ctx.f.getKdateParm()};
        ctx.appService.callProgram("OUDATE", _ca1);
        ctx.f.setKdateParm(String.valueOf(_ca1[0]));
        if (!Utility.fieldEquals(ctx.f.getKdStatus(), "00")) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("To date is not a valid date.");
            return;
        }
        if ((ctx.f.getWsFromDate().compareTo(ctx.f.getWsToDate()) > 0)) {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("From date is later than to date.");
        }
    }

    /** COBOL paragraph: 2200-REPORT-BILLS */
    private void _2200ReportBills(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setWsEndFlg("N");
        ctx.f.setBlId(0);
        _2210StartbrBill(ctx);
        if (hasError(ctx)) {
            return;
        }
        while (!ctx.f.getWsEndFlg().equals("Y")) {
            _2220ReadBills(ctx);
        }
        _2230EndbrBill(ctx);
    }

    /** COBOL paragraph: 2210-STARTBR-BILL */
    private void _2210StartbrBill(TaskContext ctx) {
        ctx.appService.startBrowse(ctx.f.getWsBillfile(), String.valueOf(ctx.f.getBlId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BROWSE-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setWsEndFlg("Y");
        } else {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error starting bill browse.");
        }
    }

    /** COBOL paragraph: 2220-READ-BILLS */
    private void _2220ReadBills(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsBillfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setWsRecDate(ctx.f.getBlPayDate());
            if ((ctx.f.getWsRecDate().compareTo(ctx.f.getWsFromDate()) >= 0)
                    && (ctx.f.getWsRecDate().compareTo(ctx.f.getWsToDate()) <= 0)) {
                ctx.f.setWsRptCount(ctx.f.getWsRptCount() + 1);
                ctx.f.setWsTotal(ctx.f.getWsTotal().add(ctx.f.getBlAmount()));
            }
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setWsEndFlg("Y");
        } else {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error reading bill file.");
            ctx.f.setWsEndFlg("Y");
        }
    }

    /** COBOL paragraph: 2230-ENDBR-BILL */
    private void _2230EndbrBill(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsBillfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 2300-REPORT-TRANS */
    private void _2300ReportTrans(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setWsEndFlg("N");
        ctx.f.fillLowValues("TR-ID");
        _2310StartbrTran(ctx);
        if (hasError(ctx)) {
            return;
        }
        while (!ctx.f.getWsEndFlg().equals("Y")) {
            _2320ReadTrans(ctx);
        }
        _2330EndbrTran(ctx);
    }

    /** COBOL paragraph: 2310-STARTBR-TRAN */
    private void _2310StartbrTran(TaskContext ctx) {
        ctx.appService.startBrowse(ctx.f.getWsTranfile(), String.valueOf(ctx.f.getTrId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BROWSE-FLG", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setWsEndFlg("Y");
        } else {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error starting tran browse.");
        }
    }

    /** COBOL paragraph: 2320-READ-TRANS */
    private void _2320ReadTrans(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsTranfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setWsRecDate(
                    Utility.padRight(String.valueOf(ctx.f.getTrOrigTs()), 10).substring(0, 10));
            if ((ctx.f.getWsRecDate().compareTo(ctx.f.getWsFromDate()) >= 0)
                    && (ctx.f.getWsRecDate().compareTo(ctx.f.getWsToDate()) <= 0)) {
                ctx.f.setWsRptCount(ctx.f.getWsRptCount() + 1);
                ctx.f.setWsTotal(ctx.f.getWsTotal().add(ctx.f.getTrAmt()));
            }
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setWsEndFlg("Y");
        } else {
            ctx.f.setString("WS-ERR-FLG", "Y");
            ctx.f.setErrmsgo("Error reading tran file.");
            ctx.f.setWsEndFlg("Y");
        }
    }

    /** COBOL paragraph: 2330-ENDBR-TRAN */
    private void _2330EndbrTran(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsTranfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 2400-BUILD-SUMMARY */
    private void _2400BuildSummary(TaskContext ctx) {
        ctx.f.setWsEdCnt(ctx.f.getWsRptCount());
        ctx.f.setWsEdTot(ctx.f.getWsTotal());
        ctx.f.setErrmsgo(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Type ");
            sb.append(ctx.f.getWsRptType());
            sb.append(" Count: ");
            sb.append(ctx.f.getString("WS-ED-CNT"));
            sb.append(" Total: ");
            sb.append(ctx.f.getString("WS-ED-TOT"));
            ctx.f.setErrmsgo(sb.toString());
        }
    }

    /** COBOL paragraph: 7000-XCTL-MENU */
    private void _7000XctlMenu(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(
                ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8000-POPULATE-HEADER */
    private void _8000PopulateHeader(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        _8500GetDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void _8100SendDataonly(TaskContext ctx) {
        _8000PopulateHeader(ctx);
        ctx.appService.sendMap(
                "MREPTA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8500-GET-DATE-TIME */
    private void _8500GetDateTime(TaskContext ctx) {
        ctx.appService.askTime();
        com.appruntime.FormatTimeResult _ftResult =
                ctx.appService.formatTime(null, "YYYYMMDD", "-", ":");
        ctx.f.setWsHdrDate(_ftResult.getDate());
        ctx.f.setWsHdrTime(_ftResult.getTime());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 9000-RETURN */
    private void _9000Return(TaskContext ctx) {
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /** Whether the current request has an unrecoverable validation/processing error. */
    private boolean hasError(TaskContext ctx) {
        return ctx.f.getWsErrFlg().equals("Y");
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcreptFields f;

        final AppService appService;

        final OcreptLinkParm link = new OcreptLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcreptFields(ws);
        }
    }
}

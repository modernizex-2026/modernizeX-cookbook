package com.generated.orion.occusin.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OccusinLinkParm;
import com.generated.orion.occusin.accessor.OccusinFields;
import com.generated.orion.occusin.metadata.OccusinBmsMetadata;
import com.generated.orion.occusin.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCCUSIN. */
@Service
public class OccusinService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OccusinService.class);

    private static final int COMMAREA_LENGTH = 692;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        runMainLine(ctx);
    }

    @Override
    public String getProgramName() {
        return "OCCUSIN";
    }

    @Override
    public String getTransId() {
        return "ORQC";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OccusinBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OccusinBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OccusinBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainLine(TaskContext ctx) {
        ctx.f.setString("WS-ERR-FLG", "N");
        ctx.f.setCaErrMsg(" ");
        if (ctx.appService.getEibcalen() == 0) {
            ctx.f.setOrionCommarea("");
            sendInitialScreen(ctx);
        } else {
            ctx.f.setOrionCommarea(Utility.groupToString(ctx.appService.getSerializedCommarea()));
            if (ctx.f.getCaPgmContext() == 0) {
                sendInitialScreen(ctx);
            } else {
                processInput(ctx);
            }
        }
        returnTransaction(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MCUSINAO");
        populateHeader(ctx);
        clearAllRows(ctx);
        ctx.f.setKcusinArea("");
        ctx.f.setString("KUI-FILTER", "I");
        ctx.f.setCaWorkArea(ctx.f.getKuiRequest());
        ctx.f.setFmodeo("I");
        ctx.f.setErrmsgo(ctx.f.getWsMPrompt());
        ctx.f.setFmodel((short) -1);
        ctx.appService.sendMap(
                "MCUSINA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            transferToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "7")) {
            restartQuery(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "8")) {
            showNextPage(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            applyFilter(ctx);
        } else {
            sendInitialScreen(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnly(ctx);
        }
    }

    /** COBOL paragraph: 2100-APPLY-FILTER */
    private void applyFilter(TaskContext ctx) {
        ctx.appService.receiveMap("MCUSINA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MCUSINAI");
        }
        parseFilterMode(ctx);
        if (isValidationFailed(ctx)) {
            ctx.f.setFmodel((short) -1);
            sendDataOnly(ctx);
            return;
        }
        ctx.f.setKuiStartKey(0);
        callSubProgram(ctx);
        displayResultPage(ctx);
    }

    /** COBOL paragraph: 2200-PAGE-NEXT */
    private void showNextPage(TaskContext ctx) {
        ctx.f.setKuiRequest(String.valueOf(ctx.f.getCaWorkArea()));
        callSubProgram(ctx);
        if (ctx.f.getKuiRowCount() == 0) {
            ctx.f.setErrmsgo(ctx.f.getWsMEndList());
            ctx.f.setFmodel((short) -1);
            sendDataOnly(ctx);
        } else {
            displayResultPage(ctx);
        }
    }

    /** COBOL paragraph: 2300-RESTART */
    private void restartQuery(TaskContext ctx) {
        ctx.f.setKuiRequest(String.valueOf(ctx.f.getCaWorkArea()));
        ctx.f.setKuiStartKey(0);
        callSubProgram(ctx);
        displayResultPage(ctx);
    }

    /** COBOL paragraph: 3000-CALL-SUB */
    private void callSubProgram(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaToProgram(ctx.f.getWsSubPgm());
        {
            byte[] _linkCa = ctx.f.sliceBytes("KCUSIN-AREA");
            ctx.appService.link(ctx.f.getWsSubPgm(), _linkCa, 795);
            ctx.f.writeBytes("KCUSIN-AREA", _linkCa);
        }
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setString("KUI-RETURN-CD", "E");
            ctx.f.setCaErrMsg(ctx.f.getWsMLinkErr());
        }
        if (ctx.f.getKuiRowCount() > 0) {
            ctx.f.setKuiStartKey(ctx.f.getKuiNextKey());
        }
        ctx.f.setCaWorkArea(ctx.f.getKuiRequest());
    }

    /** COBOL paragraph: 4000-SHOW-PAGE */
    private void displayResultPage(TaskContext ctx) {
        clearAllRows(ctx);
        if (ctx.f.getKuiReturnCd().equals("E")) {
            ctx.f.setErrmsgo(ctx.f.getCaErrMsg());
            ctx.f.setFmodel((short) -1);
            sendDataOnly(ctx);
            return;
        }
        if (ctx.f.getKuiRowCount() == 0) {
            ctx.f.setErrmsgo(ctx.f.getWsMNoneFound());
        } else {
            for (ctx.f.setWsRowIdx(1);
                    ctx.f.getWsRowIdx() <= ctx.f.getKuiRowCount();
                    ctx.f.setWsRowIdx(ctx.f.getWsRowIdx() + 1)) {
                populateRow(ctx);
            }
            buildSummaryMessage(ctx);
            ctx.f.setErrmsgo(ctx.f.getWsMsgLine());
        }
        ctx.f.setFmodel((short) -1);
        sendDataOnly(ctx);
    }

    /** COBOL paragraph: 4100-CLEAR-ROWS */
    private void clearAllRows(TaskContext ctx) {
        ctx.f.setCul1o(" ");
        ctx.f.setCun1o(" ");
        ctx.f.setCst1o(" ");
        ctx.f.setCzp1o(" ");
        ctx.f.setCfi1o(" ");
        ctx.f.setCul2o(" ");
        ctx.f.setCun2o(" ");
        ctx.f.setCst2o(" ");
        ctx.f.setCzp2o(" ");
        ctx.f.setCfi2o(" ");
        ctx.f.setCul3o(" ");
        ctx.f.setCun3o(" ");
        ctx.f.setCst3o(" ");
        ctx.f.setCzp3o(" ");
        ctx.f.setCfi3o(" ");
        ctx.f.setCul4o(" ");
        ctx.f.setCun4o(" ");
        ctx.f.setCst4o(" ");
        ctx.f.setCzp4o(" ");
        ctx.f.setCfi4o(" ");
        ctx.f.setCul5o(" ");
        ctx.f.setCun5o(" ");
        ctx.f.setCst5o(" ");
        ctx.f.setCzp5o(" ");
        ctx.f.setCfi5o(" ");
        ctx.f.setCul6o(" ");
        ctx.f.setCun6o(" ");
        ctx.f.setCst6o(" ");
        ctx.f.setCzp6o(" ");
        ctx.f.setCfi6o(" ");
        ctx.f.setCul7o(" ");
        ctx.f.setCun7o(" ");
        ctx.f.setCst7o(" ");
        ctx.f.setCzp7o(" ");
        ctx.f.setCfi7o(" ");
        ctx.f.setCul8o(" ");
        ctx.f.setCun8o(" ");
        ctx.f.setCst8o(" ");
        ctx.f.setCzp8o(" ");
        ctx.f.setCfi8o(" ");
        ctx.f.setCul9o(" ");
        ctx.f.setCun9o(" ");
        ctx.f.setCst9o(" ");
        ctx.f.setCzp9o(" ");
        ctx.f.setCfi9o(" ");
        ctx.f.setCul10o(" ");
        ctx.f.setCun10o(" ");
        ctx.f.setCst10o(" ");
        ctx.f.setCzp10o(" ");
        ctx.f.setCfi10o(" ");
        ctx.f.setCul11o(" ");
        ctx.f.setCun11o(" ");
        ctx.f.setCst11o(" ");
        ctx.f.setCzp11o(" ");
        ctx.f.setCfi11o(" ");
        ctx.f.setCul12o(" ");
        ctx.f.setCun12o(" ");
        ctx.f.setCst12o(" ");
        ctx.f.setCzp12o(" ");
        ctx.f.setCfi12o(" ");
        ctx.f.setCul13o(" ");
        ctx.f.setCun13o(" ");
        ctx.f.setCst13o(" ");
        ctx.f.setCzp13o(" ");
        ctx.f.setCfi13o(" ");
    }

    /** COBOL paragraph: 4200-PLACE-ROW */
    private void populateRow(TaskContext ctx) {
        switch (ctx.f.getWsRowIdx()) {
            case 1 -> {
                ctx.f.setCul1o(String.format("%09d", ctx.f.getKurId(1)));
                ctx.f.setCun1o(ctx.f.getKurName(1));
                ctx.f.setCst1o(ctx.f.getKurState(1));
                ctx.f.setCzp1o(ctx.f.getKurZip(1));
                ctx.f.setCfi1o(String.format("%03d", ctx.f.getKurFico(1)));
            }
            case 2 -> {
                ctx.f.setCul2o(String.format("%09d", ctx.f.getKurId(2)));
                ctx.f.setCun2o(ctx.f.getKurName(2));
                ctx.f.setCst2o(ctx.f.getKurState(2));
                ctx.f.setCzp2o(ctx.f.getKurZip(2));
                ctx.f.setCfi2o(String.format("%03d", ctx.f.getKurFico(2)));
            }
            case 3 -> {
                ctx.f.setCul3o(String.format("%09d", ctx.f.getKurId(3)));
                ctx.f.setCun3o(ctx.f.getKurName(3));
                ctx.f.setCst3o(ctx.f.getKurState(3));
                ctx.f.setCzp3o(ctx.f.getKurZip(3));
                ctx.f.setCfi3o(String.format("%03d", ctx.f.getKurFico(3)));
            }
            case 4 -> {
                ctx.f.setCul4o(String.format("%09d", ctx.f.getKurId(4)));
                ctx.f.setCun4o(ctx.f.getKurName(4));
                ctx.f.setCst4o(ctx.f.getKurState(4));
                ctx.f.setCzp4o(ctx.f.getKurZip(4));
                ctx.f.setCfi4o(String.format("%03d", ctx.f.getKurFico(4)));
            }
            case 5 -> {
                ctx.f.setCul5o(String.format("%09d", ctx.f.getKurId(5)));
                ctx.f.setCun5o(ctx.f.getKurName(5));
                ctx.f.setCst5o(ctx.f.getKurState(5));
                ctx.f.setCzp5o(ctx.f.getKurZip(5));
                ctx.f.setCfi5o(String.format("%03d", ctx.f.getKurFico(5)));
            }
            case 6 -> {
                ctx.f.setCul6o(String.format("%09d", ctx.f.getKurId(6)));
                ctx.f.setCun6o(ctx.f.getKurName(6));
                ctx.f.setCst6o(ctx.f.getKurState(6));
                ctx.f.setCzp6o(ctx.f.getKurZip(6));
                ctx.f.setCfi6o(String.format("%03d", ctx.f.getKurFico(6)));
            }
            case 7 -> {
                ctx.f.setCul7o(String.format("%09d", ctx.f.getKurId(7)));
                ctx.f.setCun7o(ctx.f.getKurName(7));
                ctx.f.setCst7o(ctx.f.getKurState(7));
                ctx.f.setCzp7o(ctx.f.getKurZip(7));
                ctx.f.setCfi7o(String.format("%03d", ctx.f.getKurFico(7)));
            }
            case 8 -> {
                ctx.f.setCul8o(String.format("%09d", ctx.f.getKurId(8)));
                ctx.f.setCun8o(ctx.f.getKurName(8));
                ctx.f.setCst8o(ctx.f.getKurState(8));
                ctx.f.setCzp8o(ctx.f.getKurZip(8));
                ctx.f.setCfi8o(String.format("%03d", ctx.f.getKurFico(8)));
            }
            case 9 -> {
                ctx.f.setCul9o(String.format("%09d", ctx.f.getKurId(9)));
                ctx.f.setCun9o(ctx.f.getKurName(9));
                ctx.f.setCst9o(ctx.f.getKurState(9));
                ctx.f.setCzp9o(ctx.f.getKurZip(9));
                ctx.f.setCfi9o(String.format("%03d", ctx.f.getKurFico(9)));
            }
            case 10 -> {
                ctx.f.setCul10o(String.format("%09d", ctx.f.getKurId(10)));
                ctx.f.setCun10o(ctx.f.getKurName(10));
                ctx.f.setCst10o(ctx.f.getKurState(10));
                ctx.f.setCzp10o(ctx.f.getKurZip(10));
                ctx.f.setCfi10o(String.format("%03d", ctx.f.getKurFico(10)));
            }
            case 11 -> {
                ctx.f.setCul11o(String.format("%09d", ctx.f.getKurId(11)));
                ctx.f.setCun11o(ctx.f.getKurName(11));
                ctx.f.setCst11o(ctx.f.getKurState(11));
                ctx.f.setCzp11o(ctx.f.getKurZip(11));
                ctx.f.setCfi11o(String.format("%03d", ctx.f.getKurFico(11)));
            }
            case 12 -> {
                ctx.f.setCul12o(String.format("%09d", ctx.f.getKurId(12)));
                ctx.f.setCun12o(ctx.f.getKurName(12));
                ctx.f.setCst12o(ctx.f.getKurState(12));
                ctx.f.setCzp12o(ctx.f.getKurZip(12));
                ctx.f.setCfi12o(String.format("%03d", ctx.f.getKurFico(12)));
            }
            case 13 -> {
                ctx.f.setCul13o(String.format("%09d", ctx.f.getKurId(13)));
                ctx.f.setCun13o(ctx.f.getKurName(13));
                ctx.f.setCst13o(ctx.f.getKurState(13));
                ctx.f.setCzp13o(ctx.f.getKurZip(13));
                ctx.f.setCfi13o(String.format("%03d", ctx.f.getKurFico(13)));
            }
            default -> {
                /* CONTINUE */
            }
        }
    }

    /** COBOL paragraph: 4300-BUILD-SUMMARY */
    private void buildSummaryMessage(TaskContext ctx) {
        ctx.f.setWsCntEd(ctx.f.getKuiMatchCount());
        ctx.f.setWsFicoMinEd(ctx.f.getKuiFicoMin());
        ctx.f.setWsFicoEd(ctx.f.getKuiFicoAvg());
        ctx.f.setWsFicoMaxEd(ctx.f.getKuiFicoMax());
        ctx.f.setWsMsgLine(" ");
        String suffix = ctx.f.getKuiMoreSw().equals("Y") ? "  PF8=more PF7=top" : "  End of list.";
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(ctx.f.getString("WS-CNT-ED")));
        sb.append(String.valueOf(" match, FICO min/avg/max "));
        sb.append(String.valueOf(ctx.f.getString("WS-FICO-MIN-ED")));
        sb.append(String.valueOf("/"));
        sb.append(String.valueOf(ctx.f.getString("WS-FICO-ED")));
        sb.append(String.valueOf("/"));
        sb.append(String.valueOf(ctx.f.getString("WS-FICO-MAX-ED")));
        sb.append(String.valueOf(suffix));
        ctx.f.setWsMsgLine(sb.toString());
    }

    /** COBOL paragraph: 5000-PARSE-FILTER */
    private void parseFilterMode(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsMode(ctx.f.getFmodei());
        ctx.f.setWsMode(
                String.valueOf(ctx.f.getWsMode())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        ctx.f.setKuiRequest("");
        switch (Utility.rtrim(ctx.f.getWsMode())) {
            case "I" -> {
                parseIdRange(ctx);
            }
            case "F" -> {
                parseFicoRange(ctx);
            }
            case "S" -> {
                parseStateZip(ctx);
            }
            default -> {
                ctx.f.setErrmsgo(ctx.f.getWsMBadMode());
                ctx.f.setString("WS-VALID-SW", "N");
            }
        }
    }

    /** COBOL paragraph: 5100-PARSE-IDRANGE */
    private void parseIdRange(TaskContext ctx) {
        ctx.f.setString("KUI-FILTER", "I");
        ctx.f.setKuiIdFrom(0);
        ctx.f.setKuiIdTo(999999999);
        Integer idFrom = parseOptionalNumericBound(ctx, ctx.f.getFridi(), 9);
        if (isValidationFailed(ctx)) {
            ctx.f.setErrmsgo(ctx.f.getWsMBadNum());
            return;
        }
        if (idFrom != null) {
            ctx.f.setKuiIdFrom(idFrom);
        }
        Integer idTo = parseOptionalNumericBound(ctx, ctx.f.getToidi(), 9);
        if (isValidationFailed(ctx)) {
            ctx.f.setErrmsgo(ctx.f.getWsMBadNum());
            return;
        }
        if (idTo != null) {
            ctx.f.setKuiIdTo(idTo);
        }
    }

    /** COBOL paragraph: 5200-PARSE-FICO */
    private void parseFicoRange(TaskContext ctx) {
        ctx.f.setString("KUI-FILTER", "F");
        ctx.f.setKuiFicoFrom(0);
        ctx.f.setKuiFicoTo(999);
        Integer ficoFrom = parseOptionalNumericBound(ctx, ctx.f.getFrficoi(), 3);
        if (isValidationFailed(ctx)) {
            ctx.f.setErrmsgo(ctx.f.getWsMBadNum());
            return;
        }
        if (ficoFrom != null) {
            ctx.f.setKuiFicoFrom(ficoFrom);
        }
        Integer ficoTo = parseOptionalNumericBound(ctx, ctx.f.getToficoi(), 3);
        if (isValidationFailed(ctx)) {
            ctx.f.setErrmsgo(ctx.f.getWsMBadNum());
            return;
        }
        if (ficoTo != null) {
            ctx.f.setKuiFicoTo(ficoTo);
        }
    }

    /** COBOL paragraph: 5300-PARSE-STZIP */
    private void parseStateZip(TaskContext ctx) {
        ctx.f.setString("KUI-FILTER", "S");
        ctx.f.setKuiState(ctx.f.getFstatei());
        ctx.f.setKuiState(
                String.valueOf(ctx.f.getKuiState())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        ctx.f.setKuiZip(ctx.f.getFzipi());
        ctx.f.setKuiZip(
                String.valueOf(ctx.f.getKuiZip())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getKuiState(), " ")) {
            ctx.f.setErrmsgo(ctx.f.getWsMStateReq());
            ctx.f.setString("WS-VALID-SW", "N");
        }
    }

    /** COBOL paragraph: 6000-PARSE-NUM */
    private void parseNumericField(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsNcValue(0);
        ctx.f.setWsNcDigits(0);
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        for (ctx.f.setWsNcPos(1);
                ctx.f.getWsNcPos() <= ctx.f.getWsNcLen();
                ctx.f.setWsNcPos(ctx.f.getWsNcPos() + 1)) {
            ctx.f.setWsNcChar(
                    Utility.padRight(String.valueOf(ctx.f.getWsNcIn()), 256)
                            .substring(ctx.f.getWsNcPos() - 1, ctx.f.getWsNcPos() - 1 + 1));
            if (Utility.fieldEquals(ctx.f.getWsNcChar(), " ")) {
                /* CONTINUE */
            } else if ((ctx.f.getWsNcChar().compareTo("0") >= 0)
                    && (ctx.f.getWsNcChar().compareTo("9") <= 0)) {
                ctx.f.setWsNcDigit(Utility.parseNumeric(ctx.f.getWsNcChar()).intValue());
                ctx.f.setWsNcValue(((ctx.f.getWsNcValue() * 10) + ctx.f.getWsNcDigit()));
                ctx.f.setWsNcDigits(ctx.f.getWsNcDigits() + 1);
            } else {
                ctx.f.setString("WS-VALID-SW", "N");
            }
        }
        if (ctx.f.getWsNcDigits() == 0) {
            ctx.f.setString("WS-VALID-SW", "N");
        }
    }

    /** COBOL paragraph: 7000-XCTL-MENU */
    private void transferToMenu(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(
                ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8000-POPULATE-HEADER */
    private void populateHeader(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        loadCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnly(TaskContext ctx) {
        populateHeader(ctx);
        ctx.appService.sendMap(
                "MCUSINA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8500-GET-DATE-TIME */
    private void loadCurrentDateTime(TaskContext ctx) {
        ctx.appService.askTime();
        com.appruntime.FormatTimeResult _ftResult =
                ctx.appService.formatTime(null, "YYYYMMDD", "-", ":");
        ctx.f.setWsHdrDate(_ftResult.getDate());
        ctx.f.setWsHdrTime(_ftResult.getTime());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 9000-RETURN */
    private void returnTransaction(TaskContext ctx) {
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /** Parse an optional numeric range bound; blank input yields no change to the default. */
    private Integer parseOptionalNumericBound(TaskContext ctx, String rawValue, int length) {
        ctx.f.setWsNcIn(rawValue);
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getWsNcIn(), " ")) {
            return null;
        }
        ctx.f.setWsNcLen(length);
        parseNumericField(ctx);
        return isValidationFailed(ctx) ? null : ctx.f.getWsNcValue();
    }

    /** True when the last field-validation pass marked the input invalid. */
    private boolean isValidationFailed(TaskContext ctx) {
        return ctx.f.getWsValidSw().equals("N");
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OccusinFields f;

        final AppService appService;

        final OccusinLinkParm link = new OccusinLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OccusinFields(ws);
        }
    }
}

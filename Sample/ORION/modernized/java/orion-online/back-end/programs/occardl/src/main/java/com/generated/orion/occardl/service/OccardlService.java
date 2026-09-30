package com.generated.orion.occardl.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OccardlLinkParm;
import com.generated.orion.occardl.accessor.OccardlFields;
import com.generated.orion.occardl.metadata.OccardlBmsMetadata;
import com.generated.orion.occardl.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCCARDL. */
@Service
public class OccardlService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OccardlService.class);

    /** Length in bytes of the ORION-COMMAREA passed across program transfer/return. */
    private static final int COMMAREA_LENGTH = 692;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        runMainProgram(ctx);
    }

    @Override
    public String getProgramName() {
        return "OCCARDL";
    }

    @Override
    public String getTransId() {
        return "ORCL";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OccardlBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OccardlBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OccardlBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void runMainProgram(TaskContext ctx) {
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
        ctx.f.fillLowValues("MCARDLAO");
        populateHeader(ctx);
        ctx.f.setCaWorkArea(
                Utility.setSubstring(
                        String.valueOf(ctx.f.getCaWorkArea()),
                        1,
                        1,
                        String.valueOf(ctx.f.getWsStKey())));
        ctx.f.setCaWorkArea(
                Utility.setSubstring(
                        String.valueOf(ctx.f.getCaWorkArea()),
                        2,
                        16,
                        String.valueOf(RuntimeConstants.LOW_VALUES)));
        ctx.f.setErrmsgo("Enter account id and press ENTER to list cards.");
        ctx.appService.sendMap(
                "MCARDLA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            xctlToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "@")) {
            xctlToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "_")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            handleFirstPage(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "7")) {
            handleFirstPage(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "8")) {
            handleNextPage(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnly(ctx);
        }
    }

    /** COBOL paragraph: 2050-RECEIVE */
    private void receiveScreenInput(TaskContext ctx) {
        ctx.appService.receiveMap("MCARDLA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MCARDLAI");
        }
    }

    /** COBOL paragraph: 2100-FIRST-PAGE */
    private void handleFirstPage(TaskContext ctx) {
        receiveScreenInput(ctx);
        validateAccountId(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            sendDataOnly(ctx);
        } else {
            ctx.f.setCaAcctId(ctx.f.getWsNcValue());
            ctx.f.fillLowValues("WS-START-CARD");
            browseCards(ctx);
            populateCardPage(ctx);
            ctx.f.setCaWorkArea(
                    Utility.setSubstring(
                            String.valueOf(ctx.f.getCaWorkArea()),
                            1,
                            1,
                            String.valueOf(ctx.f.getWsStList())));
            ctx.f.setCaWorkArea(
                    Utility.setSubstring(
                            String.valueOf(ctx.f.getCaWorkArea()),
                            2,
                            16,
                            String.valueOf(ctx.f.getWsResumeCard())));
            sendDataOnly(ctx);
        }
    }

    /** COBOL paragraph: 2200-NEXT-PAGE */
    private void handleNextPage(TaskContext ctx) {
        if (!Utility.fieldEquals(
                Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 1).substring(0, 1),
                ctx.f.getWsStList())) {
            ctx.f.setErrmsgo("List an account first (press ENTER).");
            sendDataOnly(ctx);
        } else {
            receiveScreenInput(ctx);
            ctx.f.setWsStartCard(
                    Utility.padRight(String.valueOf(ctx.f.getCaWorkArea()), 17).substring(1, 17));
            browseCards(ctx);
            populateCardPage(ctx);
            if (ctx.f.getWsRowCnt() == 0) {
                ctx.f.setErrmsgo("No more cards for this account.");
            }
            ctx.f.setCaWorkArea(
                    Utility.setSubstring(
                            String.valueOf(ctx.f.getCaWorkArea()),
                            2,
                            16,
                            String.valueOf(ctx.f.getWsResumeCard())));
            sendDataOnly(ctx);
        }
    }

    /** COBOL paragraph: 3000-BROWSE-CARDS */
    private void browseCards(TaskContext ctx) {
        ctx.f.setWsRowCnt(0);
        ctx.f.setString("WS-BR-END-SW", "N");
        ctx.f.setString("WS-MORE-SW", "N");
        ctx.f.setWsCardTable("");
        startCardBrowse(ctx);
        if (isBrowseEnded(ctx)) {
            return;
        }
        if (!ctx.f.isAllLowValues("WS-START-CARD")) {
            readNextCard(ctx);
        }
        while (ctx.f.getWsRowCnt() < ctx.f.getWsMaxRows() && !isBrowseEnded(ctx)) {
            gatherMatchingCard(ctx);
        }
        if (isBrowseEnded(ctx)) {
            ctx.f.setString("WS-MORE-SW", "N");
        } else {
            ctx.f.setString("WS-MORE-SW", "Y");
        }
        endCardBrowse(ctx);
    }

    /** COBOL paragraph: 3050-START-BROWSE */
    private void startCardBrowse(TaskContext ctx) {
        ctx.f.setString("WS-BR-STARTED-SW", "N");
        ctx.f.setWsBrowseKey(ctx.f.getWsStartCard());
        ctx.appService.startBrowse(
                ctx.f.getWsCardfile(), String.valueOf(ctx.f.getWsBrowseKey()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BR-STARTED-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-BR-END-SW", "Y");
        } else {
            abendWithFileError(ctx);
        }
    }

    /** COBOL paragraph: 3100-READ-NEXT */
    private void readNextCard(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsCardfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setWsResumeCard(ctx.f.getWsBrowseKey());
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-BR-END-SW", "Y");
        } else {
            ctx.f.setString("WS-BR-END-SW", "Y");
        }
    }

    /** COBOL paragraph: 3200-GATHER */
    private void gatherMatchingCard(TaskContext ctx) {
        readNextCard(ctx);
        if (!isBrowseEnded(ctx)) {
            if (ctx.f.getCdAcctId() == ctx.f.getCaAcctId()) {
                ctx.f.setWsRowCnt(ctx.f.getWsRowCnt() + 1);
                ctx.f.setWsRowCard(ctx.f.getWsRowCnt(), ctx.f.getCdNum());
                ctx.f.setWsRowStat(ctx.f.getWsRowCnt(), ctx.f.getCdActiveStatus());
            }
        }
    }

    /** COBOL paragraph: 3300-END-BROWSE */
    private void endCardBrowse(TaskContext ctx) {
        if (ctx.f.getWsBrStartedSw().equals("Y")) {
            ctx.appService.endBrowse(ctx.f.getWsCardfile());
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        }
    }

    /** COBOL paragraph: 4000-SHOW-PAGE */
    private void populateCardPage(TaskContext ctx) {
        ctx.f.setAcctido(String.format("%011d", ctx.f.getCaAcctId()));
        clearCardRows(ctx);
        if (ctx.f.getWsRowCnt() >= 1) {
            ctx.f.setCard1o(ctx.f.getWsRowCard(1));
            ctx.f.setStat1o(ctx.f.getWsRowStat(1));
        }
        if (ctx.f.getWsRowCnt() >= 2) {
            ctx.f.setCard2o(ctx.f.getWsRowCard(2));
            ctx.f.setStat2o(ctx.f.getWsRowStat(2));
        }
        if (ctx.f.getWsRowCnt() >= 3) {
            ctx.f.setCard3o(ctx.f.getWsRowCard(3));
            ctx.f.setStat3o(ctx.f.getWsRowStat(3));
        }
        if (ctx.f.getWsRowCnt() >= 4) {
            ctx.f.setCard4o(ctx.f.getWsRowCard(4));
            ctx.f.setStat4o(ctx.f.getWsRowStat(4));
        }
        if (ctx.f.getWsRowCnt() >= 5) {
            ctx.f.setCard5o(ctx.f.getWsRowCard(5));
            ctx.f.setStat5o(ctx.f.getWsRowStat(5));
        }
        if (ctx.f.getWsRowCnt() == 0) {
            ctx.f.setErrmsgo("No cards found for this account.");
        } else {
            if (ctx.f.getWsMoreSw().equals("Y")) {
                ctx.f.setErrmsgo("Cards listed. PF8=next PF7=top PF3=menu.");
            } else {
                ctx.f.setErrmsgo("End of list. PF7=top PF3=menu.");
            }
        }
    }

    /** COBOL paragraph: 4100-CLEAR-ROWS */
    private void clearCardRows(TaskContext ctx) {
        ctx.f.setCard1o(" ");
        ctx.f.setCard2o(" ");
        ctx.f.setCard3o(" ");
        ctx.f.setCard4o(" ");
        ctx.f.setCard5o(" ");
        ctx.f.setStat1o(" ");
        ctx.f.setStat2o(" ");
        ctx.f.setStat3o(" ");
        ctx.f.setStat4o(" ");
        ctx.f.setStat5o(" ");
    }

    /** COBOL paragraph: 6000-VALIDATE-ACCTID */
    private void validateAccountId(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsNcIn(ctx.f.getAcctidi());
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getWsNcIn(), " ")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo(ctx.f.getWsMsgRequired());
            return;
        }
        ctx.f.setWsNcValue(0);
        ctx.f.setWsNcDigits(0);
        for (ctx.f.setWsNcPos(1);
                ctx.f.getWsNcPos() <= 11;
                ctx.f.setWsNcPos(ctx.f.getWsNcPos() + 1)) {
            ctx.f.setWsNcChar(
                    Utility.padRight(String.valueOf(ctx.f.getWsNcIn()), 256)
                            .substring(ctx.f.getWsNcPos() - 1, ctx.f.getWsNcPos() - 1 + 1));
            if (Utility.fieldEquals(ctx.f.getWsNcChar(), " ")) {
                /* CONTINUE */
            } else if ((ctx.f.getWsNcChar().compareTo("0") >= 0)
                    && (ctx.f.getWsNcChar().compareTo("9") <= 0)) {
                ctx.f.setWsNcDigit(Utility.parseNumeric(ctx.f.getWsNcChar()).intValue());
                ctx.f.setWsNcValue(((ctx.f.getWsNcValue() * (long) 10) + ctx.f.getWsNcDigit()));
                ctx.f.setWsNcDigits(ctx.f.getWsNcDigits() + 1);
            } else {
                ctx.f.setString("WS-VALID-SW", "N");
            }
        }
        if (ctx.f.getWsValidSw().equals("N") || ctx.f.getWsNcDigits() == 0) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo("Account id must be numeric.");
        }
    }

    /** COBOL paragraph: 7000-XCTL-MENU */
    private void xctlToMenu(TaskContext ctx) {
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
        fetchCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnly(TaskContext ctx) {
        populateHeader(ctx);
        ctx.appService.sendMap(
                "MCARDLA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 8500-GET-DATE-TIME */
    private void fetchCurrentDateTime(TaskContext ctx) {
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

    /** COBOL paragraph: 9500-ABEND-RTN */
    private void abendWithFileError(TaskContext ctx) {
        if (ctx.f.getWsBrStartedSw().equals("Y")) {
            ctx.appService.endBrowse(ctx.f.getWsCardfile());
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
        }
        ctx.f.setWsMsgText("OCCARDL: unrecoverable file error. Contact support.");
        ctx.appService.sendText(String.valueOf(ctx.f), true, true);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.appService.returnProgram();
    }

    /** Whether the card-file browse has reached end-of-file or an unrecoverable read status. */
    private boolean isBrowseEnded(TaskContext ctx) {
        return ctx.f.getWsBrEndSw().equals("Y");
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OccardlFields f;

        final AppService appService;

        final OccardlLinkParm link = new OccardlLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OccardlFields(ws);
        }
    }
}

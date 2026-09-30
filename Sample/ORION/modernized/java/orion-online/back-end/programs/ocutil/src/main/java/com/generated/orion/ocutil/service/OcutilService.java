package com.generated.orion.ocutil.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.FieldMapping;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.RuntimeConstants;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.common.linkage.OcutilLinkParm;
import com.generated.orion.ocutil.accessor.OcutilFields;
import com.generated.orion.ocutil.metadata.OcutilBmsMetadata;
import com.generated.orion.ocutil.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OCUTIL. */
@Service
public class OcutilService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OcutilService.class);

    /** Length in bytes of the ORION-COMMAREA exchanged with sub-programs and the CICS region. */
    private static final int COMMAREA_LENGTH = 692;

    /**
     * Entry point: executes the CICS program main line. Called by AppRunner after transaction
     * setup.
     */
    @Override
    public void mainLine(AppService appService) {
        TaskContext ctx = new TaskContext(appService);
        executeMainLine(ctx);
    }

    @Override
    public String getProgramName() {
        return "OCUTIL";
    }

    @Override
    public String getTransId() {
        return "ORUT";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OcutilBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OcutilBmsMetadata.registerFsetFields(runner);
    }

    @Override
    public FieldMapping getFieldMapping(String mapName) {
        return OcutilBmsMetadata.getFieldMapping(mapName);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void executeMainLine(TaskContext ctx) {
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
                processAidInput(ctx);
            }
        }
        returnToCics(ctx);
    }

    /** COBOL paragraph: 1000-SEND-INITIAL */
    private void sendInitialScreen(TaskContext ctx) {
        ctx.f.fillLowValues("MUTILAO");
        populateHeaderFields(ctx);
        ctx.f.setUtmodeo("REBL");
        ctx.f.setErrmsgo("Select a utility (1-7), key params, press ENTER.");
        ctx.appService.sendMap(
                "MUTILA", ctx.f, ctx.f, /* erase */ true, /* freekb */ false, /* cursor */ false);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        ctx.f.setCaPgmContext(1);
    }

    /** COBOL paragraph: 2000-PROCESS-INPUT */
    private void processAidInput(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.appService.getEibaid(), "3")) {
            xctlToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "@")) {
            xctlToMenu(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "4")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "_")) {
            sendInitialScreen(ctx);
        } else if (Utility.fieldEquals(ctx.appService.getEibaid(), "'")) {
            runSelectedUtility(ctx);
        } else {
            ctx.f.setErrmsgo(ctx.f.getWsMsgInvalidKey());
            sendDataOnlyScreen(ctx);
        }
    }

    /** COBOL paragraph: 2100-RUN */
    private void runSelectedUtility(TaskContext ctx) {
        ctx.appService.receiveMap("MUTILA", ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 36) {
            ctx.f.fillLowValues("MUTILAI");
        }
        validateUtilityOption(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            sendDataOnlyScreen(ctx);
            return;
        }
        validateRequestInputs(ctx);
        if (ctx.f.getWsValidSw().equals("N")) {
            sendDataOnlyScreen(ctx);
            return;
        }
        clearResultFields(ctx);
        ctx.f.setWsLinkBad("N");
        switch (ctx.f.getWsOption()) {
            case 1 -> {
                runXrefUtility(ctx);
            }
            case 2 -> {
                runStmbUtility(ctx);
            }
            case 3 -> {
                runFlagUtility(ctx);
            }
            case 4 -> {
                runImpUtility(ctx);
            }
            case 5 -> {
                runArchUtility(ctx);
            }
            case 6 -> {
                runPurgUtility(ctx);
            }
            case 7 -> {
                runBkpUtility(ctx);
            }
            default -> {
                /* CONTINUE */
            }
        }
        if (Utility.fieldEquals(ctx.f.getWsLinkBad(), "Y")) {
            ctx.f.setErrmsgo("Sub-program link failed - check resources.");
        } else {
            ctx.f.setErrmsgo("Utility complete - see counts below.");
        }
        sendDataOnlyScreen(ctx);
    }

    /** COBOL paragraph: 2150-EDIT-OPTION */
    private void validateUtilityOption(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsNcIn(ctx.f.getUtopti());
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (Utility.fieldEquals(ctx.f.getWsNcIn(), " ")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo("Enter a utility number 1 through 7.");
            return;
        }
        ctx.f.setWsNcLen(2);
        parseNumericInput(ctx);
        if (ctx.f.getWsValidSw().equals("N")
                || ctx.f.getWsNcValue() < 1
                || ctx.f.getWsNcValue() > 7) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setErrmsgo("Utility number must be 1 through 7.");
        } else {
            ctx.f.setWsOption(Utility.toCobolInt(ctx.f.getWsNcValue(), 2));
        }
    }

    /** COBOL paragraph: 2200-EDIT-INPUTS */
    private void validateRequestInputs(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsReqMode(ctx.f.getUtmodei());
        ctx.f.setWsReqDate(ctx.f.getUtdatei());
        ctx.f.setWsReqMode(
                String.valueOf(ctx.f.getWsReqMode())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        ctx.f.setWsReqDate(
                String.valueOf(ctx.f.getWsReqDate())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        ctx.f.setWsMaxNum(0);
        ctx.f.setWsCycNum(0);
        ctx.f.setWsNcIn(ctx.f.getUtnumi());
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (!Utility.fieldEquals(ctx.f.getWsNcIn(), " ")) {
            ctx.f.setWsNcLen(7);
            parseNumericInput(ctx);
            if (ctx.f.getWsValidSw().equals("N")) {
                ctx.f.setErrmsgo("Max count must be numeric.");
                return;
            }
            ctx.f.setWsMaxNum(Utility.toCobolInt(ctx.f.getWsNcValue(), 7));
        }
        ctx.f.setWsNcIn(ctx.f.getUtcyci());
        ctx.f.setWsNcIn(
                String.valueOf(ctx.f.getWsNcIn())
                        .replace(String.valueOf(RuntimeConstants.LOW_VALUES), String.valueOf(" ")));
        if (!Utility.fieldEquals(ctx.f.getWsNcIn(), " ")) {
            ctx.f.setWsNcLen(6);
            parseNumericInput(ctx);
            if (ctx.f.getWsValidSw().equals("N")) {
                ctx.f.setErrmsgo("Cycle must be numeric (YYYYMM).");
                return;
            }
            ctx.f.setWsCycNum(Utility.toCobolInt(ctx.f.getWsNcValue(), 6));
        }
    }

    /** COBOL paragraph: 3100-DO-XREF */
    private void runXrefUtility(TaskContext ctx) {
        ctx.f.setKuxParm("");
        if (Utility.fieldEquals(ctx.f.getWsReqMode(), " ")) {
            ctx.f.setKuxMode("REBL");
        } else {
            ctx.f.setKuxMode(ctx.f.getWsReqMode());
        }
        ctx.f.setKuxMax(ctx.f.getWsMaxNum());
        ctx.f.setKuxStartCard(" ");
        ctx.f.setCaWorkArea(ctx.f.getKuxParm());
        ctx.f.setWsLinkPgm(ctx.f.getWsPXref());
        linkToSubProgram(ctx);
        ctx.f.setKuxParm(String.valueOf(ctx.f.getCaWorkArea()));
        formatXrefResult(ctx);
    }

    /** COBOL paragraph: 3200-DO-STMB */
    private void runStmbUtility(TaskContext ctx) {
        ctx.f.setKsmParm("");
        ctx.f.setKsmCycle(ctx.f.getWsCycNum());
        ctx.f.setKsmDueDate(ctx.f.getWsReqDate());
        ctx.f.setKsmStartAcct(0);
        ctx.f.setKsmMax(ctx.f.getWsMaxNum());
        ctx.f.setCaWorkArea(ctx.f.getKsmParm());
        ctx.f.setWsLinkPgm(ctx.f.getWsPStmb());
        linkToSubProgram(ctx);
        ctx.f.setKsmParm(String.valueOf(ctx.f.getCaWorkArea()));
        formatStmbResult(ctx);
    }

    /** COBOL paragraph: 3300-DO-FLAG */
    private void runFlagUtility(TaskContext ctx) {
        ctx.f.setKflParm("");
        if (Utility.fieldEquals(ctx.f.getWsReqMode(), " ")) {
            ctx.f.setKflMode("BOTH");
        } else {
            ctx.f.setKflMode(ctx.f.getWsReqMode());
        }
        ctx.f.setKflCutoff(ctx.f.getWsReqDate());
        ctx.f.setKflStartAcct(0);
        ctx.f.setKflMax(ctx.f.getWsMaxNum());
        ctx.f.setCaWorkArea(ctx.f.getKflParm());
        ctx.f.setWsLinkPgm(ctx.f.getWsPFlag());
        linkToSubProgram(ctx);
        ctx.f.setKflParm(String.valueOf(ctx.f.getCaWorkArea()));
        formatFlagResult(ctx);
    }

    /** COBOL paragraph: 3400-DO-IMP */
    private void runImpUtility(TaskContext ctx) {
        ctx.f.setKimParm("");
        ctx.f.setKimStartKey(" ");
        ctx.f.setKimMax(ctx.f.getWsMaxNum());
        ctx.f.setCaWorkArea(ctx.f.getKimParm());
        ctx.f.setWsLinkPgm(ctx.f.getWsPImp());
        linkToSubProgram(ctx);
        ctx.f.setKimParm(String.valueOf(ctx.f.getCaWorkArea()));
        formatImpResult(ctx);
    }

    /** COBOL paragraph: 3500-DO-ARCH */
    private void runArchUtility(TaskContext ctx) {
        ctx.f.setKarParm("");
        ctx.f.setKarCutoff(ctx.f.getWsReqDate());
        ctx.f.setKarStartTran(" ");
        ctx.f.setKarMax(ctx.f.getWsMaxNum());
        ctx.f.setCaWorkArea(ctx.f.getKarParm());
        ctx.f.setWsLinkPgm(ctx.f.getWsPArch());
        linkToSubProgram(ctx);
        ctx.f.setKarParm(String.valueOf(ctx.f.getCaWorkArea()));
        formatArchResult(ctx);
    }

    /** COBOL paragraph: 3600-DO-PURG */
    private void runPurgUtility(TaskContext ctx) {
        ctx.f.setKpgParm("");
        ctx.f.setKpgCutoff(ctx.f.getWsReqDate());
        ctx.f.setKpgStartTran(" ");
        ctx.f.setKpgMax(ctx.f.getWsMaxNum());
        ctx.f.setCaWorkArea(ctx.f.getKpgParm());
        ctx.f.setWsLinkPgm(ctx.f.getWsPPurg());
        linkToSubProgram(ctx);
        ctx.f.setKpgParm(String.valueOf(ctx.f.getCaWorkArea()));
        formatPurgResult(ctx);
    }

    /** COBOL paragraph: 3700-DO-BKP */
    private void runBkpUtility(TaskContext ctx) {
        ctx.f.setKbkParm("");
        ctx.f.setKbkStartKey(" ");
        ctx.f.setKbkMax(ctx.f.getWsMaxNum());
        ctx.f.setCaWorkArea(ctx.f.getKbkParm());
        ctx.f.setWsLinkPgm(ctx.f.getWsPBkp());
        linkToSubProgram(ctx);
        ctx.f.setKbkParm(String.valueOf(ctx.f.getCaWorkArea()));
        formatBkpResult(ctx);
    }

    /** COBOL paragraph: 4000-LINK-SUB */
    private void linkToSubProgram(TaskContext ctx) {
        {
            byte[] _linkCa = ctx.f.sliceBytes("ORION-COMMAREA");
            ctx.appService.link(ctx.f.getWsLinkPgm(), _linkCa, COMMAREA_LENGTH);
            ctx.f.writeBytes("ORION-COMMAREA", _linkCa);
        }
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() != 0) {
            ctx.f.setWsLinkBad("Y");
        }
    }

    /** COBOL paragraph: 5000-CLEAR-RESULT */
    private void clearResultFields(TaskContext ctx) {
        ctx.f.setRstato(" ");
        ctx.f.setRmsgo(" ");
        ctx.f.setRline1o(" ");
        ctx.f.setRline2o(" ");
        ctx.f.setRline3o(" ");
        ctx.f.setRline4o(" ");
    }

    /** COBOL paragraph: 5100-FMT-XREF */
    private void formatXrefResult(TaskContext ctx) {
        ctx.f.setRstato(ctx.f.getKuxStatus());
        ctx.f.setRmsgo(ctx.f.getKuxMsg());
        ctx.f.setRline1o(
                composeTwoValueLine(
                        ctx,
                        "Read",
                        formatCountValue(ctx, ctx.f.getKuxRead()),
                        "Written",
                        formatCountValue(ctx, ctx.f.getKuxWritten())));
        ctx.f.setRline2o(
                composeTwoValueLine(
                        ctx,
                        "Updated",
                        formatCountValue(ctx, ctx.f.getKuxUpdated()),
                        "Skip-acct",
                        formatCountValue(ctx, ctx.f.getKuxSkipAcct())));
        ctx.f.setRline3o(
                composeTwoValueLine(
                        ctx,
                        "Skip-xref",
                        formatCountValue(ctx, ctx.f.getKuxSkipXref()),
                        "Skip-cust",
                        formatCountValue(ctx, ctx.f.getKuxSkipCust())));
        ctx.f.setRline4o(
                composeTwoValueLine(
                        ctx,
                        "Errors",
                        formatCountValue(ctx, ctx.f.getKuxErrors()),
                        "More",
                        ctx.f.getKuxMore()));
    }

    /** COBOL paragraph: 5200-FMT-STMB */
    private void formatStmbResult(TaskContext ctx) {
        ctx.f.setRstato(ctx.f.getKsmStatus());
        ctx.f.setRmsgo(ctx.f.getKsmMsg());
        ctx.f.setRline1o(
                composeTwoValueLine(
                        ctx,
                        "Acct read",
                        formatCountValue(ctx, ctx.f.getKsmAcctRead()),
                        "Stmts",
                        formatCountValue(ctx, ctx.f.getKsmStmtWritten())));
        ctx.f.setRline2o(
                composeTwoValueLine(
                        ctx,
                        "No-tran",
                        formatCountValue(ctx, ctx.f.getKsmNoTran()),
                        "Errors",
                        formatCountValue(ctx, ctx.f.getKsmErrors())));
        ctx.f.setRline3o(
                composeTwoValueLine(
                        ctx,
                        "Tot cr",
                        formatAmountValue(ctx, ctx.f.getKsmTotCredit()),
                        "Tot dr",
                        formatAmountValue(ctx, ctx.f.getKsmTotDebit())));
        ctx.f.setRline4o(
                composeTwoValueLine(
                        ctx,
                        "More",
                        ctx.f.getKsmMore(),
                        "Next acct",
                        formatCountValue(ctx, Utility.toCobolInt(ctx.f.getKsmNextAcct(), 1))));
    }

    /** COBOL paragraph: 5300-FMT-FLAG */
    private void formatFlagResult(TaskContext ctx) {
        ctx.f.setRstato(ctx.f.getKflStatus());
        ctx.f.setRmsgo(ctx.f.getKflMsg());
        ctx.f.setRline1o(
                composeTwoValueLine(
                        ctx,
                        "Read",
                        formatCountValue(ctx, ctx.f.getKflRead()),
                        "Delinq",
                        formatCountValue(ctx, ctx.f.getKflDelq())));
        ctx.f.setRline2o(
                composeTwoValueLine(
                        ctx,
                        "B30",
                        formatCountValue(ctx, ctx.f.getKflB30()),
                        "B60",
                        formatCountValue(ctx, ctx.f.getKflB60())));
        ctx.f.setRline3o(
                composeTwoValueLine(
                        ctx,
                        "B90",
                        formatCountValue(ctx, ctx.f.getKflB90()),
                        "Expired",
                        formatCountValue(ctx, ctx.f.getKflExpired())));
        ctx.f.setRline4o(
                composeTwoValueLine(
                        ctx,
                        "Errors",
                        formatCountValue(ctx, ctx.f.getKflErrors()),
                        "More",
                        ctx.f.getKflMore()));
    }

    /** COBOL paragraph: 5400-FMT-IMP */
    private void formatImpResult(TaskContext ctx) {
        ctx.f.setRstato(ctx.f.getKimStatus());
        ctx.f.setRmsgo(ctx.f.getKimMsg());
        ctx.f.setRline1o(
                composeTwoValueLine(
                        ctx,
                        "Read",
                        formatCountValue(ctx, ctx.f.getKimRead()),
                        "Accepted",
                        formatCountValue(ctx, ctx.f.getKimAccepted())));
        ctx.f.setRline2o(
                composeTwoValueLine(
                        ctx,
                        "Added",
                        formatCountValue(ctx, ctx.f.getKimAdded()),
                        "Updated",
                        formatCountValue(ctx, ctx.f.getKimUpdated())));
        ctx.f.setRline3o(
                composeTwoValueLine(
                        ctx,
                        "Rejected",
                        formatCountValue(ctx, ctx.f.getKimRejected()),
                        "Skipped",
                        formatCountValue(ctx, ctx.f.getKimSkipped())));
        ctx.f.setRline4o(
                composeTwoValueLine(
                        ctx,
                        "Errors",
                        formatCountValue(ctx, ctx.f.getKimErrors()),
                        "More",
                        ctx.f.getKimMore()));
    }

    /** COBOL paragraph: 5500-FMT-ARCH */
    private void formatArchResult(TaskContext ctx) {
        ctx.f.setRstato(ctx.f.getKarStatus());
        ctx.f.setRmsgo(ctx.f.getKarMsg());
        ctx.f.setRline1o(
                composeTwoValueLine(
                        ctx,
                        "Read",
                        formatCountValue(ctx, ctx.f.getKarRead()),
                        "Archived",
                        formatCountValue(ctx, ctx.f.getKarArchived())));
        ctx.f.setRline2o(
                composeTwoValueLine(
                        ctx,
                        "Deleted",
                        formatCountValue(ctx, ctx.f.getKarDeleted()),
                        "Kept",
                        formatCountValue(ctx, ctx.f.getKarKept())));
        ctx.f.setRline3o(
                composeTwoValueLine(
                        ctx,
                        "Arch amt",
                        formatAmountValue(ctx, ctx.f.getKarArchAmt()),
                        "Errors",
                        formatCountValue(ctx, ctx.f.getKarErrors())));
        ctx.f.setRline4o(composeOneValueLine(ctx, "More", ctx.f.getKarMore()));
    }

    /** COBOL paragraph: 5600-FMT-PURG */
    private void formatPurgResult(TaskContext ctx) {
        ctx.f.setRstato(ctx.f.getKpgStatus());
        ctx.f.setRmsgo(ctx.f.getKpgMsg());
        ctx.f.setRline1o(
                composeTwoValueLine(
                        ctx,
                        "Read",
                        formatCountValue(ctx, ctx.f.getKpgRead()),
                        "Purged",
                        formatCountValue(ctx, ctx.f.getKpgPurged())));
        ctx.f.setRline2o(
                composeTwoValueLine(
                        ctx,
                        "Kept",
                        formatCountValue(ctx, ctx.f.getKpgKept()),
                        "Errors",
                        formatCountValue(ctx, ctx.f.getKpgErrors())));
        ctx.f.setRline3o(
                composeTwoValueLine(
                        ctx,
                        "Purge amt",
                        formatAmountValue(ctx, ctx.f.getKpgPurgeAmt()),
                        "Keep amt",
                        formatAmountValue(ctx, ctx.f.getKpgKeepAmt())));
        ctx.f.setRline4o(composeOneValueLine(ctx, "More", ctx.f.getKpgMore()));
    }

    /** COBOL paragraph: 5700-FMT-BKP */
    private void formatBkpResult(TaskContext ctx) {
        ctx.f.setRstato(ctx.f.getKbkStatus());
        ctx.f.setRmsgo(ctx.f.getKbkMsg());
        ctx.f.setRline1o(
                composeTwoValueLine(
                        ctx,
                        "Read",
                        formatCountValue(ctx, ctx.f.getKbkRead()),
                        "Written",
                        formatCountValue(ctx, ctx.f.getKbkWritten())));
        ctx.f.setRline2o(
                composeTwoValueLine(
                        ctx,
                        "Tot amt",
                        formatAmountValue(ctx, ctx.f.getKbkTotAmt()),
                        "Errors",
                        formatCountValue(ctx, ctx.f.getKbkErrors())));
        ctx.f.setRline3o(
                composeTwoValueLine(
                        ctx,
                        "Credit",
                        formatAmountValue(ctx, ctx.f.getKbkCreditAmt()),
                        "Debit",
                        formatAmountValue(ctx, ctx.f.getKbkDebitAmt())));
        ctx.f.setRline4o(composeOneValueLine(ctx, "More", ctx.f.getKbkMore()));
    }

    /** COBOL paragraph: 6000-PARSE-NUM */
    private void parseNumericInput(TaskContext ctx) {
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
                ctx.f.setWsNcValue(((ctx.f.getWsNcValue() * (long) 10) + ctx.f.getWsNcDigit()));
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
    private void xctlToMenu(TaskContext ctx) {
        ctx.f.setCaFromProgram(ctx.f.getWsPgmname());
        ctx.f.setCaFromTranid(ctx.f.getWsTranid());
        ctx.f.setCaPgmContext(0);
        ctx.appService.xctl(
                ctx.f.getWsMenuPgm(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 7100-FMT2 */
    private void renderTwoFieldLine(TaskContext ctx) {
        ctx.f.setWsLine(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(String.valueOf(ctx.f.getWsL1()).trim()));
            sb.append(String.valueOf("="));
            sb.append(String.valueOf(String.valueOf(ctx.f.getWsV1()).trim()));
            sb.append(String.valueOf("   "));
            sb.append(String.valueOf(String.valueOf(ctx.f.getWsL2()).trim()));
            sb.append(String.valueOf("="));
            sb.append(String.valueOf(String.valueOf(ctx.f.getWsV2()).trim()));
            ctx.f.setWsLine(sb.toString());
        }
    }

    /** COBOL paragraph: 7150-FMT1 */
    private void renderOneFieldLine(TaskContext ctx) {
        ctx.f.setWsLine(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(String.valueOf(ctx.f.getWsL1()).trim()));
            sb.append(String.valueOf("="));
            sb.append(String.valueOf(String.valueOf(ctx.f.getWsV1()).trim()));
            ctx.f.setWsLine(sb.toString());
        }
    }

    /** COBOL paragraph: 8000-POPULATE-HEADER */
    private void populateHeaderFields(TaskContext ctx) {
        ctx.f.setTrnnameo(ctx.f.getWsTranid());
        ctx.f.setPgmnameo(ctx.f.getWsPgmname());
        ctx.f.setTitleo(ctx.f.getWsHdrTitle());
        loadCurrentDateTime(ctx);
        ctx.f.setCurdateo(ctx.f.getWsHdrDate());
        ctx.f.setCurtimeo(ctx.f.getWsHdrTime());
    }

    /** COBOL paragraph: 8100-SEND-DATAONLY */
    private void sendDataOnlyScreen(TaskContext ctx) {
        populateHeaderFields(ctx);
        ctx.appService.sendMap(
                "MUTILA", ctx.f, ctx.f, /* erase */ false, /* freekb */ false, /* cursor */ false);
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
    private void returnToCics(TaskContext ctx) {
        ctx.appService.returnTransid(
                ctx.f.getWsTranid(), ctx.f.sliceBytes("ORION-COMMAREA"), COMMAREA_LENGTH);
    }

    /** Formats an integer count through the WS-E9 edit field to produce its display string. */
    private String formatCountValue(TaskContext ctx, int value) {
        ctx.f.setWsE9(value);
        return ctx.f.getString("WS-E9");
    }

    /** Formats a monetary amount through the WS-EA edit field to produce its display string. */
    private String formatAmountValue(TaskContext ctx, java.math.BigDecimal value) {
        ctx.f.setWsEa(value);
        return ctx.f.getString("WS-EA");
    }

    /** Builds one result-panel line pairing two labeled values. */
    private String composeTwoValueLine(
            TaskContext ctx, String label1, String value1, String label2, String value2) {
        ctx.f.setWsL1(label1);
        ctx.f.setWsV1(value1);
        ctx.f.setWsL2(label2);
        ctx.f.setWsV2(value2);
        renderTwoFieldLine(ctx);
        return ctx.f.getWsLine();
    }

    /** Builds one result-panel line with a single labeled value. */
    private String composeOneValueLine(TaskContext ctx, String label1, String value1) {
        ctx.f.setWsL1(label1);
        ctx.f.setWsV1(value1);
        ctx.f.setWsL2(" ");
        ctx.f.setWsV2(" ");
        renderOneFieldLine(ctx);
        return ctx.f.getWsLine();
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OcutilFields f;

        final AppService appService;

        final OcutilLinkParm link = new OcutilLinkParm();

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OcutilFields(ws);
        }
    }
}

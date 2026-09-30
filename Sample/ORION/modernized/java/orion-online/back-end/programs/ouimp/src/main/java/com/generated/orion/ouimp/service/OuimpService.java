package com.generated.orion.ouimp.service;

import com.appruntime.AppProgram;
import com.appruntime.AppRunner;
import com.appruntime.AppService;
import com.appruntime.ScreenResponse;
import com.generated.orion.common.infrastructure.Utility;
import com.generated.orion.ouimp.accessor.OuimpFields;
import com.generated.orion.ouimp.metadata.OuimpBmsMetadata;
import com.generated.orion.ouimp.model.WorkingStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** CICS program implementation generated from COBOL program OUIMP. */
@Service
public class OuimpService implements AppProgram {
    private static final Logger log = LoggerFactory.getLogger(OuimpService.class);

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
            executeMainProgram(ctx);
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
        return "OUIMP";
    }

    @Override
    public List<ScreenResponse.ButtonDef> getButtonDefs() {
        return OuimpBmsMetadata.getButtonDefs();
    }

    @Override
    public void registerFsetFields(AppRunner runner) {
        OuimpBmsMetadata.registerFsetFields(runner);
    }

    /** COBOL paragraph: 0000-MAIN */
    private void executeMainProgram(TaskContext ctx) {
        ctx.f.aliasGroup("KIM-PARM", "CA-WORK-AREA");
        initializeWorkingStorage(ctx);
        if (!Utility.fieldEquals(ctx.f.getKimStatus(), "99")) {
            positionBrowseCursor(ctx);
            if (ctx.f.getWsBrowseSw().equals("Y")) {
                while (!isEndOfFile(ctx) && !Utility.fieldEquals(ctx.f.getWsCapped(), "Y")) {
                    processImportLoopIteration(ctx);
                }
                peekNextRecord(ctx);
                endBrowseOperation(ctx);
            }
        }
        finalizeImportStatus(ctx);
        return;
    }

    /** COBOL paragraph: 1000-INIT */
    private void initializeWorkingStorage(TaskContext ctx) {
        ctx.f.setString("WS-EOF-SW", "N");
        ctx.f.setString("WS-BROWSE-SW", "N");
        ctx.f.setWsCapped("N");
        ctx.f.setKimStatus("00");
        ctx.f.setKimMsg(" ");
        ctx.f.setKimRead(0);
        ctx.f.setKimAdded(0);
        ctx.f.setKimUpdated(0);
        ctx.f.setKimAccepted(0);
        ctx.f.setKimRejected(0);
        ctx.f.setKimSkipped(0);
        ctx.f.setKimErrors(0);
        ctx.f.setKimLastReason(" ");
        ctx.f.setKimLastKey(" ");
        ctx.f.setKimNextKey(" ");
        ctx.f.setKimMore("N");
        ctx.f.setWsMax(ctx.f.getKimMax());
    }

    /** COBOL paragraph: 2000-POSITION */
    private void positionBrowseCursor(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getKimStartKey(), " ")
                || ctx.f.isAllLowValues("KIM-START-KEY")) {
            ctx.f.fillLowValues("IMP-KEY");
        } else {
            ctx.f.setImpKey(ctx.f.getKimStartKey());
        }
        ctx.appService.startBrowse(ctx.f.getWsImpfile(), String.valueOf(ctx.f.getImpKey()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-BROWSE-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setString("WS-EOF-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-EOF-SW", "Y");
        } else {
            ctx.f.setKimStatus("99");
            ctx.f.setKimMsg("IMPFILE STARTBR FAILED");
        }
    }

    /** COBOL paragraph: 3000-IMPORT-LOOP */
    private void processImportLoopIteration(TaskContext ctx) {
        readNextImportLine(ctx);
        if (!isEndOfFile(ctx)) {
            processImportLine(ctx);
            if (ctx.f.getWsMax() > 0 && ctx.f.getKimRead() >= ctx.f.getWsMax()) {
                ctx.f.setWsCapped("Y");
            }
        }
    }

    /** COBOL paragraph: 3100-READ-LINE */
    private void readNextImportLine(TaskContext ctx) {
        ctx.appService.readNext(ctx.f.getWsImpfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            /* CONTINUE */
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setString("WS-EOF-SW", "Y");
        } else {
            ctx.f.setString("WS-EOF-SW", "Y");
            ctx.f.setKimErrors(ctx.f.getKimErrors() + 1);
            ctx.f.setKimMsg("IMPFILE READNEXT FAILED");
        }
    }

    /** COBOL paragraph: 3200-PROCESS-LINE */
    private void processImportLine(TaskContext ctx) {
        ctx.f.setKimRead(ctx.f.getKimRead() + 1);
        parseImportLine(ctx);
        ctx.f.setWsTypeChk(String.valueOf(ctx.f.getWsFType()).trim());
        switch (Utility.rtrim(ctx.f.getWsTypeChk())) {
            case "RECTYPE" -> {
                ctx.f.setKimSkipped(ctx.f.getKimSkipped() + 1);
            }
            case "CUST" -> {
                ctx.f.setKimSkipped(ctx.f.getKimSkipped() + 1);
            }
            case "ACCT" -> {
                validateImportRecord(ctx);
                if (isValid(ctx)) {
                    applyAccountUpdate(ctx);
                } else {
                    recordRejection(ctx);
                }
            }
            default -> {
                ctx.f.setWsRejReason("UNKNOWN RECTYPE");
                recordRejection(ctx);
            }
        }
    }

    /** COBOL paragraph: 3300-PARSE-LINE */
    private void parseImportLine(TaskContext ctx) {
        ctx.f.setWsFields(" ");
        ctx.f.setWsFldCnt(0);
    }

    /** COBOL paragraph: 3400-VALIDATE */
    private void validateImportRecord(TaskContext ctx) {
        ctx.f.setString("WS-VALID-SW", "Y");
        ctx.f.setWsRejReason(" ");
        if (ctx.f.getWsFldCnt() < 13) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setWsRejReason("TOO FEW FIELDS");
        }
        if (isValid(ctx)) {
            validateAccountId(ctx);
        }
        if (isValid(ctx)) {
            validateStatusFlag(ctx);
        }
        if (isValid(ctx)) {
            validateAmountFields(ctx);
        }
        if (isValid(ctx)) {
            validateDateFields(ctx);
        }
    }

    /** COBOL paragraph: 3410-CHK-ID */
    private void validateAccountId(TaskContext ctx) {
        if (Utility.testNumval(String.valueOf(ctx.f.getWsFId())) != 0) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setWsRejReason("INVALID ACCOUNT ID");
        } else {
            ctx.f.setWsNId(Utility.parseNumeric(String.valueOf(ctx.f.getWsFId())).longValue());
            if (ctx.f.getWsNId() == 0) {
                ctx.f.setString("WS-VALID-SW", "N");
                ctx.f.setWsRejReason("ACCOUNT ID IS ZERO");
            }
        }
    }

    /** COBOL paragraph: 3420-CHK-STATUS */
    private void validateStatusFlag(TaskContext ctx) {
        ctx.f.setWsStatusChk(String.valueOf(ctx.f.getWsFStatus()).trim());
        if (!Utility.fieldEquals(ctx.f.getWsStatusChk(), "Y")
                && !Utility.fieldEquals(ctx.f.getWsStatusChk(), "N")) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setWsRejReason("STATUS NOT Y OR N");
        }
    }

    /** COBOL paragraph: 3430-CHK-AMOUNTS */
    private void validateAmountFields(TaskContext ctx) {
        if (Utility.testNumval(String.valueOf(ctx.f.getWsFBal())) != 0) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setWsRejReason("INVALID BALANCE");
        } else {
            ctx.f.setWsNBal(Utility.parseNumeric(String.valueOf(ctx.f.getWsFBal())));
        }
        if (isValid(ctx)) {
            if (Utility.testNumval(String.valueOf(ctx.f.getWsFCrlim())) != 0) {
                ctx.f.setString("WS-VALID-SW", "N");
                ctx.f.setWsRejReason("INVALID CREDIT LIMIT");
            } else {
                ctx.f.setWsNCrlim(Utility.parseNumeric(String.valueOf(ctx.f.getWsFCrlim())));
            }
        }
        if (isValid(ctx)) {
            if (Utility.testNumval(String.valueOf(ctx.f.getWsFCslim())) != 0) {
                ctx.f.setString("WS-VALID-SW", "N");
                ctx.f.setWsRejReason("INVALID CASH LIMIT");
            } else {
                ctx.f.setWsNCslim(Utility.parseNumeric(String.valueOf(ctx.f.getWsFCslim())));
            }
        }
        if (isValid(ctx)) {
            if (Utility.testNumval(String.valueOf(ctx.f.getWsFCycr())) != 0) {
                ctx.f.setString("WS-VALID-SW", "N");
                ctx.f.setWsRejReason("INVALID CYC CREDIT");
            } else {
                ctx.f.setWsNCycr(Utility.parseNumeric(String.valueOf(ctx.f.getWsFCycr())));
            }
        }
        if (isValid(ctx)) {
            if (Utility.testNumval(String.valueOf(ctx.f.getWsFCydr())) != 0) {
                ctx.f.setString("WS-VALID-SW", "N");
                ctx.f.setWsRejReason("INVALID CYC DEBIT");
            } else {
                ctx.f.setWsNCydr(Utility.parseNumeric(String.valueOf(ctx.f.getWsFCydr())));
            }
        }
    }

    /** COBOL paragraph: 3440-CHK-DATES */
    private void validateDateFields(TaskContext ctx) {
        ctx.f.setWsDtIn(ctx.f.getWsFOpen());
        validateDateFormat(ctx);
        if (!isDateValid(ctx)) {
            ctx.f.setString("WS-VALID-SW", "N");
            ctx.f.setWsRejReason("INVALID OPEN DATE");
        }
        if (isValid(ctx)) {
            ctx.f.setWsDtIn(ctx.f.getWsFExpiry());
            validateDateFormat(ctx);
            if (!isDateValid(ctx)) {
                ctx.f.setString("WS-VALID-SW", "N");
                ctx.f.setWsRejReason("INVALID EXPIRY DATE");
            }
        }
        if (isValid(ctx)) {
            ctx.f.setWsDtIn(ctx.f.getWsFReiss());
            validateDateFormat(ctx);
            if (!isDateValid(ctx)) {
                ctx.f.setString("WS-VALID-SW", "N");
                ctx.f.setWsRejReason("INVALID REISSUE DATE");
            }
        }
    }

    /** COBOL paragraph: 3450-CHK-DATE */
    private void validateDateFormat(TaskContext ctx) {
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

    /** COBOL paragraph: 3600-APPLY-ACCT */
    private void applyAccountUpdate(TaskContext ctx) {
        ctx.f.setAcId(ctx.f.getWsNId());
        ctx.appService.readFileForUpdate(
                ctx.f.getWsAcctfile(), ctx.f, String.valueOf(ctx.f.getAcId()), 0);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setString("WS-EXIST-SW", "Y");
        } else if (ctx.f.getWsRespCd() == 13) {
            ctx.f.setWsExistSw("N");
        } else {
            ctx.f.setWsRejReason("READ ERROR");
            recordRejection(ctx);
            return;
        }
        buildAccountRecord(ctx);
        if (ctx.f.getWsExistSw().equals("Y")) {
            ctx.appService.rewriteFile(ctx.f.getWsAcctfile(), ctx.f);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
            if (ctx.f.getWsRespCd() == 0) {
                ctx.f.setKimUpdated(ctx.f.getKimUpdated() + 1);
                ctx.f.setKimAccepted(ctx.f.getKimAccepted() + 1);
            } else {
                ctx.f.setWsRejReason("REWRITE FAILED");
                recordRejection(ctx);
            }
        } else {
            ctx.appService.writeFile(
                    ctx.f.getWsAcctfile(), ctx.f, String.valueOf(ctx.f.getAcId()), 0);
            ctx.f.setWsRespCd(ctx.appService.getEibresp());
            if (ctx.f.getWsRespCd() == 0) {
                ctx.f.setKimAdded(ctx.f.getKimAdded() + 1);
                ctx.f.setKimAccepted(ctx.f.getKimAccepted() + 1);
            } else {
                ctx.f.setWsRejReason("WRITE FAILED");
                recordRejection(ctx);
            }
        }
    }

    /** COBOL paragraph: 3610-BUILD-REC */
    private void buildAccountRecord(TaskContext ctx) {
        ctx.f.setAcctRec(" ");
        ctx.f.setAcId(ctx.f.getWsNId());
        ctx.f.setAcActiveStatus(ctx.f.getWsStatusChk());
        ctx.f.setAcCurrBal(ctx.f.getWsNBal());
        ctx.f.setAcCreditLimit(ctx.f.getWsNCrlim());
        ctx.f.setAcCashLimit(ctx.f.getWsNCslim());
        ctx.f.setAcOpenDate(String.valueOf(ctx.f.getWsFOpen()).trim());
        ctx.f.setAcExpiryDate(String.valueOf(ctx.f.getWsFExpiry()).trim());
        ctx.f.setAcReissueDate(String.valueOf(ctx.f.getWsFReiss()).trim());
        ctx.f.setAcCycCredit(ctx.f.getWsNCycr());
        ctx.f.setAcCycDebit(ctx.f.getWsNCydr());
        ctx.f.setAcAddrZip(String.valueOf(ctx.f.getWsFZip()).trim());
        ctx.f.setAcGroupId(String.valueOf(ctx.f.getWsFGroup()).trim());
    }

    /** COBOL paragraph: 3700-REJECT */
    private void recordRejection(TaskContext ctx) {
        ctx.f.setKimRejected(ctx.f.getKimRejected() + 1);
        ctx.f.setKimLastReason(ctx.f.getWsRejReason());
        ctx.f.setKimLastKey(ctx.f.getImpKey());
    }

    /** COBOL paragraph: 4000-PEEK-NEXT */
    private void peekNextRecord(TaskContext ctx) {
        if (isEndOfFile(ctx)) {
            ctx.f.setKimMore("N");
            return;
        }
        ctx.appService.readNext(ctx.f.getWsImpfile(), ctx.f);
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
        if (ctx.f.getWsRespCd() == 0) {
            ctx.f.setKimMore("Y");
            ctx.f.setKimNextKey(ctx.f.getImpKey());
        } else if (ctx.f.getWsRespCd() == 20) {
            ctx.f.setKimMore("N");
        } else {
            ctx.f.setKimMore("N");
            ctx.f.setKimErrors(ctx.f.getKimErrors() + 1);
        }
    }

    /** COBOL paragraph: 5000-END-BROWSE */
    private void endBrowseOperation(TaskContext ctx) {
        ctx.appService.endBrowse(ctx.f.getWsImpfile());
        ctx.f.setWsRespCd(ctx.appService.getEibresp());
    }

    /** COBOL paragraph: 6000-SET-STATUS */
    private void finalizeImportStatus(TaskContext ctx) {
        if (Utility.fieldEquals(ctx.f.getKimStatus(), "99")) {
            return;
        }
        if (ctx.f.getKimRead() == 0) {
            ctx.f.setKimStatus("10");
            ctx.f.setKimMsg("IMPORT FEED IS EMPTY");
        } else {
            ctx.f.setKimStatus("00");
            ctx.f.setKimMsg("ACCOUNT IMPORT COMPLETE");
        }
    }

    /** True when the current import record has passed all validation checks. */
    private boolean isValid(TaskContext ctx) {
        return ctx.f.getWsValidSw().equals("Y");
    }

    /** True when the import file browse has reached end-of-file. */
    private boolean isEndOfFile(TaskContext ctx) {
        return ctx.f.getWsEofSw().equals("Y");
    }

    /** True when a parsed date field is in valid CCYY-MM-DD format. */
    private boolean isDateValid(TaskContext ctx) {
        return Utility.fieldEquals(ctx.f.getWsDtOk(), "Y");
    }

    /**
     * Per-request execution context — analogous to CICS Task Control Block. Each HTTP request
     * creates a fresh TaskContext. Thread-safe by design.
     */
    static class TaskContext {
        final WorkingStorage ws;

        final OuimpFields f;

        final AppService appService;

        TaskContext(AppService appService) {
            this.appService = appService;
            this.ws = new WorkingStorage();
            this.f = new OuimpFields(ws);
        }
    }
}

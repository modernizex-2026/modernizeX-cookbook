package com.sakura.bt0010.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.bt0010.domain.Bt0010FieldAccess;
import com.sakura.bt0010.domain.WorkingStorage;
import com.sakura.bt0010.runtime.Bt0010Datasets;
import com.sakura.dateut.service.DateutService;
import com.sakura.runtime.BatchServiceBase;
import com.sakura.runtime.DatasetEnums.*;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.AbstractDatasets;
import com.sakura.runtime.linkage.AbortxLinkParm;
import com.sakura.runtime.linkage.DateutLinkParm;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program BT0010. */
@Service
@Scope("prototype")
public class Bt0010Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Bt0010Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Bt0010FieldAccess ws;

    public Bt0010Service(
            Bt0010Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Bt0010FieldAccess(new WorkingStorage(), fileSet);
        this.dateutService = dateutService;
        this.abortxService = abortxService;
    }

    /**
     * Program entry point — runs the first COBOL paragraph via runChain. Base class run() wraps
     * this in StopRun/Abort/Exception handling.
     */
    @Override
    protected void mainProcess() {
        // Import anchors for types referenced by emitted paragraph bodies:
        // StopRunSignal, JobAbortException, ParagraphJumpSignal, ProgramExitSignal
        runChain(this::runDailyClose);
    }

    /**
     * Returns COBOL COMPLETION-CODE after run() completes. Used by Tasklet to propagate exit code
     * into StepExecutionContext.
     */
    @Override
    public int getCompletionCode() {
        return ws.getCompletionCode();
    }

    /**
     * Set COBOL COMPLETION-CODE. Called by base class run() on Abort (255) / Exception (12) paths.
     */
    @Override
    protected void setCompletionCode(int code) {
        ws.setCompletionCode(code);
    }

    /**
     * Returns this program's FileSet (or null if no files declared). Base class run() uses this for
     * commit/rollback/closeAll lifecycle.
     */
    @Override
    protected AbstractDatasets getFileSet() {
        return fileSet;
    }

    /** COBOL paragraph: MAIN-000 */
    private void runDailyClose() {
        runChain(this::initializeProgram);
        if (ws.getWkAbortFlg() == 0) {
            runChain(this::postInvoicesForTargetDate);
            runChain(this::updateSystemLastCloseDate);
            runChain(this::printDailyCloseSummary);
        }
        runChain(this::closeProgramFiles);
        ws.setCompletionCode(0);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("BT0010");
        log.info(" ");
        log.info("==============================================");
        log.info(" SAKURA-SMS  BT0010  -  DAILY CLOSE");
        log.info("==============================================");
        runChain(this::loadTodaysDate);
        runChain(this::openDataFiles);
        runChain(this::readSystemControlRecord);
        log.info(" Company : {}", ws.getSyCompanyName());
        ws.setKdFunc("ADDD");
        ws.setKdDate1(ws.getSyLastDayClose());
        ws.setKdDays(1);
        dateut(ws.getKdate());
        if (Utility.fieldEquals(ws.getKdStatus(), "00")) {
            ws.setWkDfltDate(ws.getKdDate1());
        } else {
            ws.setWkDfltDate(ws.getWkSysdate());
        }
        runChain(this::promptForTargetDate);
        if (ws.getWkAbortFlg() == 0) {
            runChain(this::promptForConfirmation);
        }
    }

    /** COBOL paragraph: GTOD-010 */
    private void loadTodaysDate() {
        ws.setKdFunc("TODY");
        ws.setKdDate1(0);
        dateut(ws.getKdate());
        ws.setWkSysdate(ws.getKdDate1());
        ws.setWkSysymd(ws.getKdDate1());
        ws.setWkSysYm((ws.getKdDate1() / 100));
    }

    /** COBOL paragraph: OPEN-010 */
    private void openDataFiles() {
        fileSet.getInvhf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getInvhf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
            fileSet.getInvhf().close();
            ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
            fileSet.getInvhf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("INVHF");
            ws.setKaDetail("Open INVHF failed");
            runChain(this::abortProgram);
        }
        fileSet.getSyscf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("SYSCF");
            ws.setKaDetail("Open SYSCF failed");
            runChain(this::abortProgram);
        }
    }

    /** COBOL paragraph: RSYS-010 */
    private void readSystemControlRecord() {
        readSyscfByKey("System control record missing");
    }

    /** COBOL paragraph: ATGT-010 */
    private void promptForTargetDate() {
        ws.setWkEDate(ws.getWkDfltDate());
        log.info(" ");
        log.info(
                " Last daily close was : {}",
                String.format("%08d", (long) (ws.getSyLastDayClose())));
        log.info(" Enter target date YYYYMMDD");
        log.info("   (blank = {}) : ", ws.editedDisplay("WK-E-DATE"));
        ws.setWkInLine(" ");
        String stdinValWkInLine1 = Utility.readStdinLine();
        if (stdinValWkInLine1 == null || stdinValWkInLine1.trim().isEmpty()) {
            stdinValWkInLine1 = "";
        }
        ws.setWkInLine(stdinValWkInLine1);
        if (Utility.fieldEquals(ws.getWkInLine(), " ")) {
            ws.setWkTgtDate(ws.getWkDfltDate());
        } else {
            if (Utility.isNumeric(
                    String.valueOf(
                            Utility.padRight(String.valueOf(ws.getWkInLine()), 8)
                                    .substring(0, 8)))) {
                ws.setWkTgtDate(
                        Utility.parseNumeric(
                                        Utility.padRight(String.valueOf(ws.getWkInLine()), 8)
                                                .substring(0, 8))
                                .intValue());
            } else {
                log.info(" ** invalid date - run cancelled.");
                ws.setWkAbortFlg(1);
                return;
            }
        }
        ws.setKdFunc("VALD");
        ws.setKdDate1(ws.getWkTgtDate());
        dateut(ws.getKdate());
        if (!Utility.fieldEquals(ws.getKdStatus(), "00")) {
            log.info(" ** date is not a valid calendar date.");
            ws.setWkAbortFlg(1);
            return;
        }
        if (ws.getWkTgtDate() > ws.getWkSysdate()) {
            log.info(" ** target date is in the future - blocked.");
            ws.setWkAbortFlg(1);
        }
    }

    /** COBOL paragraph: CONF-010 */
    private void promptForConfirmation() {
        ws.setWkEDate(ws.getWkTgtDate());
        log.info(" ");
        log.info(" Post all entered invoices dated {}", ws.editedDisplay("WK-E-DATE"));
        log.info(" Proceed ? (Y/N) : ");
        ws.setWkConfirm(" ");
        String stdinValWkConfirm2 = Utility.readStdinLine();
        if (stdinValWkConfirm2 == null || stdinValWkConfirm2.trim().isEmpty()) {
            stdinValWkConfirm2 = "";
        }
        ws.setWkConfirm(stdinValWkConfirm2);
        if (!ws.getWkConfirm().equals("Y") && !ws.getWkConfirm().equals("y")) {
            log.info(" ** cancelled by operator.");
            ws.setWkAbortFlg(1);
        }
    }

    /** COBOL paragraph: PROC-010 */
    private void postInvoicesForTargetDate() {
        log.info(" ");
        log.info(" Posting ...");
        ws.setEofFlg(0);
        ws.setIhDate(ws.getWkTgtDate());
        ws.setIhNo(0);
        fileSet.getInvhf().start("IH-DATE", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        if (fileSet.getInvhf().isInvalidKey()) {
            ws.setString("EOF-FLG", "1");
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")
                && !Utility.fieldEquals(ws.getFsts(), "23")
                && !Utility.fieldEquals(ws.getFsts(), "10")) {
            ws.setKaFile("INVHF");
            ws.setKaDetail("START INVHF failed");
            runChain(this::abortProgram);
        }
        while ((ws.getEofFlg() != 1)) {
            runChain(this::readNextInvoiceRecord);
        }
    }

    /** COBOL paragraph: RNXT-010 */
    private void readNextInvoiceRecord() {
        fileSet.getInvhf().readNext();
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        if (fileSet.getInvhf().isAtEnd()) {
            ws.setString("EOF-FLG", "1");
            return;
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00") && !Utility.fieldEquals(ws.getFsts(), "02")) {
            ws.setKaFile("INVHF");
            ws.setKaDetail("READ NEXT INVHF failed");
            runChain(this::abortProgram);
        }
        if (ws.getIhDate() == ws.getWkTgtDate()) {
            runChain(this::postInvoiceRecord);
        } else {
            ws.setString("EOF-FLG", "1");
        }
    }

    /** COBOL paragraph: POST-010 */
    private void postInvoiceRecord() {
        ws.setWkReadCnt(ws.getWkReadCnt() + 1);
        if (ws.getIhStatus() == 0) {
            runChain(this::accumulateInvoiceTotals);
            ws.setIhStatus(1);
            ws.setIhUpdDate(ws.getWkSysdate());
            ws.setIhUpdUser(ws.getWkUserCode());
            fileSet.getInvhf().rewrite();
            ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
            if (fileSet.getInvhf().isInvalidKey()) {
                ws.setKaFile("INVHF");
                ws.setKaDetail("REWRITE INVHF failed");
                runChain(this::abortProgram);
            }
            if (Utility.fieldEquals(ws.getFsts(), "00")) {
                ws.setWkPostCnt(ws.getWkPostCnt() + 1);
                ws.setWkENo(ws.getIhNo());
                ws.setWkEAmt(ws.getIhTotal().longValue());
                log.info(
                        "   posted inv {} total {}",
                        ws.editedDisplay("WK-E-NO"),
                        ws.editedDisplay("WK-E-AMT"));
            }
        } else {
            ws.setWkSkipCnt(ws.getWkSkipCnt() + 1);
        }
    }

    /** COBOL paragraph: ACCU-010 */
    private void accumulateInvoiceTotals() {
        if (ws.getIhKind() == 2) {
            ws.setWkSaleAmt(
                    (BigDecimal.valueOf(ws.getWkSaleAmt()).subtract(ws.getIhAmount())).longValue());
            ws.setWkTaxTot(
                    (BigDecimal.valueOf(ws.getWkTaxTot()).subtract(ws.getIhTaxAmount()))
                            .longValue());
            ws.setWkGrossTot(
                    (BigDecimal.valueOf(ws.getWkGrossTot()).subtract(ws.getIhTotal())).longValue());
            ws.setWkCostTot(
                    (BigDecimal.valueOf(ws.getWkCostTot()).subtract(ws.getIhCostTotal()))
                            .longValue());
            ws.setWkRtnCnt(ws.getWkRtnCnt() + 1);
        } else {
            ws.setWkSaleAmt(
                    (BigDecimal.valueOf(ws.getWkSaleAmt()).add(ws.getIhAmount())).longValue());
            ws.setWkTaxTot(
                    (BigDecimal.valueOf(ws.getWkTaxTot()).add(ws.getIhTaxAmount())).longValue());
            ws.setWkGrossTot(
                    (BigDecimal.valueOf(ws.getWkGrossTot()).add(ws.getIhTotal())).longValue());
            ws.setWkCostTot(
                    (BigDecimal.valueOf(ws.getWkCostTot()).add(ws.getIhCostTotal())).longValue());
            ws.setWkSaleCnt(ws.getWkSaleCnt() + 1);
        }
    }

    /** COBOL paragraph: USYS-010 */
    private void updateSystemLastCloseDate() {
        readSyscfByKey("Re-read SYSCF failed");
        ws.setSyLastDayClose(ws.getWkTgtDate());
        fileSet.getSyscf().rewrite();
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (fileSet.getSyscf().isInvalidKey()) {
            ws.setKaFile("SYSCF");
            ws.setKaDetail("REWRITE SYSCF failed");
            runChain(this::abortProgram);
        }
    }

    /** COBOL paragraph: PSUM-010 */
    private void printDailyCloseSummary() {
        ws.setWkEDate(ws.getWkTgtDate());
        log.info(" ");
        log.info("----------------------------------------------");
        log.info(" DAILY CLOSE SUMMARY");
        log.info("----------------------------------------------");
        log.info(" Target date       : {}", ws.editedDisplay("WK-E-DATE"));
        ws.setWkECnt(ws.getWkReadCnt());
        log.info(" Invoices read     : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkPostCnt());
        log.info(" Invoices posted   : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkSkipCnt());
        log.info(" Already/skipped   : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkSaleCnt());
        log.info(" Sale documents    : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkRtnCnt());
        log.info(" Return documents  : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkEAmt(ws.getWkSaleAmt());
        log.info(" Net sales amount  : {}", ws.editedDisplay("WK-E-AMT"));
        ws.setWkEAmt(ws.getWkTaxTot());
        log.info(" Tax amount        : {}", ws.editedDisplay("WK-E-AMT"));
        ws.setWkEAmt(ws.getWkGrossTot());
        log.info(" Gross amount      : {}", ws.editedDisplay("WK-E-AMT"));
        ws.setWkEAmt(ws.getWkCostTot());
        log.info(" Cost of sales     : {}", ws.editedDisplay("WK-E-AMT"));
        if (ws.getWkPostCnt() > 0) {
            ws.setWkAvg((ws.getWkGrossTot() / ws.getWkPostCnt()));
            ws.setWkEAmt(ws.getWkAvg());
            log.info(" Avg gross / doc   : {}", ws.editedDisplay("WK-E-AMT"));
        }
        log.info("----------------------------------------------");
        log.info(" BT0010 completed normally.");
    }

    /** COBOL paragraph: TERM-010 */
    private void closeProgramFiles() {
        fileSet.getInvhf().close();
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        fileSet.getSyscf().close();
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
    }

    /** COBOL paragraph: ABND-010 */
    private void abortProgram() {
        ws.setKaProgid("BT0010");
        ws.setKaFsts(ws.getFsts());
        ws.setKaMsgcode("EBATCH");
        abortx(ws.getKabend());
        fileSet.getInvhf().close();
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        fileSet.getSyscf().close();
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        ws.setCompletionCode(255);
        throw new ProgramExitSignal();
    }

    /** COBOL CALL DATEUT — delegates to injected DateutService. */
    private void dateut(Object... args) {
        DateutLinkParm params = new DateutLinkParm();
        params.getKdate().setKdFunc(ws.getKdFunc());
        params.getKdate().setKdDate1(ws.getKdDate1());
        params.getKdate().setKdDate2(ws.getKdDate2());
        params.getKdate().setKdDays(ws.getKdDays());
        params.getKdate().setKdWeekday(ws.getKdWeekday());
        params.getKdate().setKdStatus(ws.getKdStatus());
        dateutService.execute(params);
        ws.setKdFunc(params.getKdate().getKdFunc());
        ws.setKdDate1(params.getKdate().getKdDate1());
        ws.setKdDate2(params.getKdate().getKdDate2());
        ws.setKdDays(params.getKdate().getKdDays());
        ws.setKdWeekday(params.getKdate().getKdWeekday());
        ws.setKdStatus(params.getKdate().getKdStatus());
    }

    /** COBOL CALL ABORTX — delegates to injected AbortxService. */
    private void abortx(Object... args) {
        AbortxLinkParm params = new AbortxLinkParm();
        params.getKabend().setKaProgid(ws.getKaProgid());
        params.getKabend().setKaFile(ws.getKaFile());
        params.getKabend().setKaFsts(ws.getKaFsts());
        params.getKabend().setKaMsgcode(ws.getKaMsgcode());
        params.getKabend().setKaDetail(ws.getKaDetail());
        abortxService.execute(params);
        ws.setKaProgid(params.getKabend().getKaProgid());
        ws.setKaFile(params.getKabend().getKaFile());
        ws.setKaFsts(params.getKabend().getKaFsts());
        ws.setKaMsgcode(params.getKabend().getKaMsgcode());
        ws.setKaDetail(params.getKabend().getKaDetail());
    }

    /** Read SYSCF by SY-KEY, aborting with the given detail message on a missing/invalid key. */
    private void readSyscfByKey(String abortDetail) {
        ws.setSyKey(1);
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString("SY-KEY");
            } catch (Exception _e) {
            }
        }
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = fileSet.getSyscf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getSyscf().readByKey(rkVal != null ? rkVal.trim() : "");
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (fileSet.getSyscf().isInvalidKey()) {
            ws.setKaFile("SYSCF");
            ws.setKaDetail(abortDetail);
            runChain(this::abortProgram);
        }
    }
}

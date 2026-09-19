package com.sakura.bt0070.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.bt0070.domain.Bt0070FieldAccess;
import com.sakura.bt0070.domain.WorkingStorage;
import com.sakura.bt0070.runtime.Bt0070Datasets;
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

/** Business logic service generated from COBOL program BT0070. */
@Service
@Scope("prototype")
public class Bt0070Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Bt0070Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Bt0070FieldAccess ws;

    /** Divisor/multiplier used to split a YYYYMM value into its year and month parts. */
    private static final int YM_MONTH_DIVISOR = 100;

    public Bt0070Service(
            Bt0070Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Bt0070FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::runMainProgram);
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
    private void runMainProgram() {
        runChain(this::initializeProgram);
        if (ws.getWkAbortFlg() == 0) {
            runChain(this::resetStockYtdFigures);
            runChain(this::updateSystemControlRecord);
            runChain(this::printFiscalCloseSummary);
        }
        runChain(this::closeProgramFiles);
        ws.setCompletionCode(0);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("BT0070");
        log.info(" ");
        log.info("==============================================");
        log.info(" SAKURA-SMS  BT0070  -  FISCAL YEAR CLOSE");
        log.info("==============================================");
        runChain(this::loadSystemDate);
        runChain(this::openProgramFiles);
        runChain(this::readSystemControlRecord);
        runChain(this::computeFiscalYearDefaults);
        runChain(this::acceptNewFiscalMonth);
        if (ws.getWkAbortFlg() == 0) {
            runChain(this::confirmFiscalYearClose);
        }
    }

    /** COBOL paragraph: GTOD-010 */
    private void loadSystemDate() {
        ws.setKdFunc("TODY");
        ws.setKdDate1(0);
        dateut(ws.getKdate());
        ws.setWkSysdate(ws.getKdDate1());
        ws.setWkSysymd(ws.getKdDate1());
    }

    /** COBOL paragraph: OPEN-010 */
    private void openProgramFiles() {
        fileSet.getSyscf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("SYSCF");
            ws.setKaDetail("Open SYSCF failed");
            runChain(this::abortProgram);
        }
        fileSet.getStokf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getStokf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            fileSet.getStokf().close();
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            fileSet.getStokf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("STOKF");
            ws.setKaDetail("Open STOKF failed");
            runChain(this::abortProgram);
        }
    }

    /** COBOL paragraph: RSYS-010 */
    private void readSystemControlRecord() {
        ws.setSyKey(1);
        String rkVal_0 = "";
        if (rkVal_0 == null || rkVal_0.trim().isEmpty()) {
            try {
                rkVal_0 = ws.getString("SY-KEY");
            } catch (Exception _e) {
            }
        }
        if (rkVal_0 == null || rkVal_0.trim().isEmpty()) {
            try {
                rkVal_0 = fileSet.getSyscf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getSyscf().readByKey(rkVal_0 != null ? rkVal_0.trim() : "");
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (fileSet.getSyscf().isInvalidKey()) {
            ws.setKaFile("SYSCF");
            ws.setKaDetail("System control record missing");
            runChain(this::abortProgram);
        }
        ws.setWkOldYm(ws.getSyCurrYm());
    }

    /** COBOL paragraph: CFY-010 */
    private void computeFiscalYearDefaults() {
        ws.setWkYyyy((ws.getSyCurrYm() / YM_MONTH_DIVISOR));
        ws.setWkMm((ws.getSyCurrYm() - (ws.getWkYyyy() * YM_MONTH_DIVISOR)));
        if (ws.getWkMm() >= ws.getSyFiscalStart()) {
            ws.setWkFyYear(ws.getWkYyyy());
        } else {
            ws.setWkFyYear((ws.getWkYyyy() - 1));
        }
        ws.setWkNewYear((ws.getWkFyYear() + 1));
        ws.setWkDfltYm(((ws.getWkNewYear() * YM_MONTH_DIVISOR) + ws.getSyFiscalStart()));
    }

    /** COBOL paragraph: ANF-010 */
    private void acceptNewFiscalMonth() {
        ws.setWkEYm(ws.getWkDfltYm());
        log.info(" ");
        log.info(" Fiscal start month : {}", String.format("%02d", (long) (ws.getSyFiscalStart())));
        log.info(" Current month      : {}", String.format("%06d", (long) (ws.getSyCurrYm())));
        log.info(" New fiscal year starts YYYYMM");
        log.info("   (blank = {}) : ", ws.editedDisplay("WK-E-YM"));
        ws.setWkInLine(" ");
        String stdinValWkInLine1 = Utility.readStdinLine();
        if (stdinValWkInLine1 == null || stdinValWkInLine1.trim().isEmpty()) {
            stdinValWkInLine1 = "";
        }
        ws.setWkInLine(stdinValWkInLine1);
        if (Utility.fieldEquals(ws.getWkInLine(), " ")) {
            ws.setWkNewYm(ws.getWkDfltYm());
        } else {
            String newYmInput =
                    Utility.padRight(String.valueOf(ws.getWkInLine()), 6).substring(0, 6);
            if (Utility.isNumeric(newYmInput)) {
                ws.setWkNewYm(Utility.parseNumeric(newYmInput).intValue());
            } else {
                log.info(" ** invalid month - run cancelled.");
                ws.setWkAbortFlg(1);
                return;
            }
        }
        ws.setWkMm((ws.getWkNewYm() - ((ws.getWkNewYm() / YM_MONTH_DIVISOR) * YM_MONTH_DIVISOR)));
        if (ws.getWkMm() < 1 || ws.getWkMm() > 12) {
            log.info(" ** month part must be 01-12.");
            ws.setWkAbortFlg(1);
        }
    }

    /** COBOL paragraph: CONF-010 */
    private void confirmFiscalYearClose() {
        ws.setWkEYm(ws.getWkOldYm());
        log.info(" ");
        log.info(" Close fiscal year - reset all stock YTD figures");
        log.info(" Current month now : {}", ws.editedDisplay("WK-E-YM"));
        ws.setWkEYm(ws.getWkNewYm());
        log.info(" New current month : {}", ws.editedDisplay("WK-E-YM"));
        log.info(" Proceed ? (Y/N) : ");
        ws.setWkConfirm(" ");
        String stdinValWkConfirm2 = Utility.readStdinLine();
        if (stdinValWkConfirm2 == null || stdinValWkConfirm2.trim().isEmpty()) {
            stdinValWkConfirm2 = "";
        }
        ws.setWkConfirm(stdinValWkConfirm2);
        if (!((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y")))) {
            log.info(" ** cancelled by operator.");
            ws.setWkAbortFlg(1);
        }
    }

    /** COBOL paragraph: PROC-010 */
    private void resetStockYtdFigures() {
        log.info(" ");
        log.info(" Resetting stock YTD figures ...");
        log.info(" ");
        log.info(" WHSE     ITEMS       YTD-IN      YTD-OUT");
        log.info("----------------------------------------------");
        ws.setEofFlg(0);
        ws.setWkFirstFlg(1);
        ws.setWkPrevWhse(0);
        runChain(this::clearWarehouseBreakTotals);
        ws.setSkWhse(0);
        ws.setSkProd(0);
        fileSet.getStokf().start("SK-WHSE", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setString("EOF-FLG", "1");
        }
        while ((ws.getEofFlg() != 1)) {
            runChain(this::processNextStockRecord);
        }
        if (ws.getWkFirstFlg() == 0) {
            runChain(this::printWarehouseBreakSummary);
        }
    }

    /** COBOL paragraph: PNXT-010 */
    private void processNextStockRecord() {
        fileSet.getStokf().readNext();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isAtEnd()) {
            ws.setString("EOF-FLG", "1");
            return;
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00") && !Utility.fieldEquals(ws.getFsts(), "02")) {
            ws.setKaFile("STOKF");
            ws.setKaDetail("READ NEXT STOKF failed");
            runChain(this::abortProgram);
        }
        if (ws.getWkFirstFlg() == 1) {
            ws.setWkFirstFlg(0);
            ws.setWkPrevWhse(ws.getSkWhse());
        }
        if (ws.getSkWhse() != ws.getWkPrevWhse()) {
            runChain(this::printWarehouseBreakSummary);
            runChain(this::clearWarehouseBreakTotals);
            ws.setWkPrevWhse(ws.getSkWhse());
        }
        runChain(this::resetOneStockRecord);
    }

    /** COBOL paragraph: RONE-010 */
    private void resetOneStockRecord() {
        ws.setWkReadCnt(ws.getWkReadCnt() + 1);
        ws.setWkItemVal(
                (ws.getSkOnhand().multiply(ws.getSkAvgCost()))
                        .setScale(2, java.math.RoundingMode.DOWN));
        ws.setWkOnhandTot(
                (BigDecimal.valueOf(ws.getWkOnhandTot()).add(ws.getSkOnhand())).longValue());
        ws.setWkValTot(ws.getWkValTot().add(ws.getWkItemVal()));
        ws.setWkWhVal(ws.getWkWhVal().add(ws.getWkItemVal()));
        ws.setWkYtdinTot((BigDecimal.valueOf(ws.getWkYtdinTot()).add(ws.getSkYtdIn())).longValue());
        ws.setWkWhYtdin((BigDecimal.valueOf(ws.getWkWhYtdin()).add(ws.getSkYtdIn())).longValue());
        ws.setWkYtdoutTot(
                (BigDecimal.valueOf(ws.getWkYtdoutTot()).add(ws.getSkYtdOut())).longValue());
        ws.setWkWhYtdout(
                (BigDecimal.valueOf(ws.getWkWhYtdout()).add(ws.getSkYtdOut())).longValue());
        ws.setWkWhItems(ws.getWkWhItems() + 1);
        if (ws.getSkYtdIn().signum() != 0 || ws.getSkYtdOut().signum() != 0) {
            ws.setWkActiveCnt(ws.getWkActiveCnt() + 1);
        }
        ws.setSkYtdIn(BigDecimal.ZERO);
        ws.setSkYtdOut(BigDecimal.ZERO);
        fileSet.getStokf().rewrite();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setKaFile("STOKF");
            ws.setKaDetail("REWRITE STOKF failed");
            runChain(this::abortProgram);
        }
        ws.setWkResetCnt(ws.getWkResetCnt() + 1);
    }

    /** COBOL paragraph: CLSB-010 */
    private void clearWarehouseBreakTotals() {
        ws.setWkWhItems(0);
        ws.setWkWhYtdin(0);
        ws.setWkWhYtdout(0);
        ws.setWkWhVal(BigDecimal.ZERO);
    }

    /** COBOL paragraph: PRSB-010 */
    private void printWarehouseBreakSummary() {
        ws.setWkWhseCnt(ws.getWkWhseCnt() + 1);
        ws.setWkEWhse(ws.getWkPrevWhse());
        ws.setWkECnt(ws.getWkWhItems());
        log.info(" {} {}", ws.editedDisplay("WK-E-WHSE"), ws.editedDisplay("WK-E-CNT"));
        ws.setWkEQty(ws.getWkWhYtdin());
        log.info(" {}", ws.editedDisplay("WK-E-QTY"));
        ws.setWkEQty(ws.getWkWhYtdout());
        log.info(" {}", ws.editedDisplay("WK-E-QTY"));
        ws.setWkEVal(ws.getWkWhVal());
        log.info("          valuation {}", ws.editedDisplay("WK-E-VAL"));
    }

    /** COBOL paragraph: USYS-010 */
    private void updateSystemControlRecord() {
        ws.setSyKey(1);
        String rkVal_3 = "";
        if (rkVal_3 == null || rkVal_3.trim().isEmpty()) {
            try {
                rkVal_3 = ws.getString("SY-KEY");
            } catch (Exception _e) {
            }
        }
        if (rkVal_3 == null || rkVal_3.trim().isEmpty()) {
            try {
                rkVal_3 = fileSet.getSyscf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getSyscf().readByKey(rkVal_3 != null ? rkVal_3.trim() : "");
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (fileSet.getSyscf().isInvalidKey()) {
            ws.setKaFile("SYSCF");
            ws.setKaDetail("Re-read SYSCF failed");
            runChain(this::abortProgram);
        }
        ws.setSyCurrYm(ws.getWkNewYm());
        fileSet.getSyscf().rewrite();
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (fileSet.getSyscf().isInvalidKey()) {
            ws.setKaFile("SYSCF");
            ws.setKaDetail("REWRITE SYSCF failed");
            runChain(this::abortProgram);
        }
    }

    /** COBOL paragraph: PSUM-010 */
    private void printFiscalCloseSummary() {
        log.info("----------------------------------------------");
        log.info(" FISCAL YEAR CLOSE SUMMARY");
        log.info("----------------------------------------------");
        ws.setWkECnt(ws.getWkReadCnt());
        log.info(" Stock rows read   : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkResetCnt());
        log.info(" Rows YTD reset    : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkActiveCnt());
        log.info(" Rows had activity : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkWhseCnt());
        log.info(" Warehouses        : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkEQty(ws.getWkOnhandTot());
        log.info(" Total onhand qty  : {}", ws.editedDisplay("WK-E-QTY"));
        ws.setWkEQty(ws.getWkYtdinTot());
        log.info(" Prior YTD in      : {}", ws.editedDisplay("WK-E-QTY"));
        ws.setWkEQty(ws.getWkYtdoutTot());
        log.info(" Prior YTD out     : {}", ws.editedDisplay("WK-E-QTY"));
        ws.setWkEVal(ws.getWkValTot());
        log.info(" Closing valuation : {}", ws.editedDisplay("WK-E-VAL"));
        ws.setWkEYm(ws.getWkOldYm());
        log.info(" Previous month    : {}", ws.editedDisplay("WK-E-YM"));
        ws.setWkEYm(ws.getWkNewYm());
        log.info(" New fiscal month  : {}", ws.editedDisplay("WK-E-YM"));
        log.info("----------------------------------------------");
        log.info(" BT0070 completed normally.");
    }

    /** COBOL paragraph: TERM-010 */
    private void closeProgramFiles() {
        fileSet.getSyscf().close();
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        fileSet.getStokf().close();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
    }

    /** COBOL paragraph: ABND-010 */
    private void abortProgram() {
        ws.setKaProgid("BT0070");
        ws.setKaFsts(ws.getFsts());
        ws.setKaMsgcode("EBATCH");
        abortx(ws.getKabend());
        fileSet.getSyscf().close();
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        fileSet.getStokf().close();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
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
}

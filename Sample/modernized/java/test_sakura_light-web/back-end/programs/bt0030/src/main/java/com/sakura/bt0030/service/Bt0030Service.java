package com.sakura.bt0030.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.bt0030.domain.Bt0030FieldAccess;
import com.sakura.bt0030.domain.WorkingStorage;
import com.sakura.bt0030.runtime.Bt0030Datasets;
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

/** Business logic service generated from COBOL program BT0030. */
@Service
@Scope("prototype")
public class Bt0030Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Bt0030Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Bt0030FieldAccess ws;

    public Bt0030Service(
            Bt0030Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Bt0030FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::processStockRecords);
            runChain(this::printSummaryReport);
        }
        runChain(this::closeProgramFiles);
        ws.setCompletionCode(0);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("BT0030");
        log.info(" ");
        log.info("==============================================");
        log.info(" SAKURA-SMS  BT0030  -  STOCK MONTHLY UPDATE");
        log.info("==============================================");
        runChain(this::fetchTodaysDate);
        runChain(this::openProgramFiles);
        runChain(this::readSystemControlRecord);
        runChain(this::confirmOperatorProceed);
    }

    /** COBOL paragraph: GTOD-010 */
    private void fetchTodaysDate() {
        ws.setKdFunc("TODY");
        ws.setKdDate1(0);
        callDateutService(ws.getKdate());
        ws.setWkSysdate(ws.getKdDate1());
        ws.setWkSysymd(ws.getKdDate1());
    }

    /** COBOL paragraph: OPEN-010 */
    private void openProgramFiles() {
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
        fileSet.getSyscf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("SYSCF");
            ws.setKaDetail("Open SYSCF failed");
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
    }

    /** COBOL paragraph: CONF-010 */
    private void confirmOperatorProceed() {
        ws.setWkEYm(ws.getSyCurrYm());
        log.info(" ");
        log.info(" Accounting month : {}", ws.editedDisplay("WK-E-YM"));
        log.info(" Run stock monthly snapshot / housekeeping ?");
        log.info(" Proceed ? (Y/N) : ");
        ws.setWkConfirm(" ");
        String stdinValWkConfirm1 = Utility.readStdinLine();
        if (stdinValWkConfirm1 == null || stdinValWkConfirm1.trim().isEmpty()) {
            stdinValWkConfirm1 = "";
        }
        ws.setWkConfirm(stdinValWkConfirm1);
        if (!((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y")))) {
            log.info(" ** cancelled by operator.");
            ws.setWkAbortFlg(1);
        }
    }

    /** COBOL paragraph: PROC-010 */
    private void processStockRecords() {
        log.info(" ");
        log.info(" Processing stock rows ...");
        log.info(" ");
        log.info(" WHSE      ITEMS         ONHAND" + "          VALUATION");
        log.info("----------------------------------------------");
        ws.setEofFlg(0);
        ws.setWkFirstFlg(1);
        ws.setWkPrevWhse(0);
        runChain(this::resetWarehouseSubtotals);
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
            runChain(this::printWarehouseSubtotal);
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
            runChain(this::printWarehouseSubtotal);
            runChain(this::resetWarehouseSubtotals);
            ws.setWkPrevWhse(ws.getSkWhse());
        }
        runChain(this::processOneStockRecord);
    }

    /** COBOL paragraph: PONE-010 */
    private void processOneStockRecord() {
        ws.setWkReadCnt(ws.getWkReadCnt() + 1);
        ws.setWkChgFlg(0);
        ws.setWkItemVal(
                (ws.getSkOnhand().multiply(ws.getSkAvgCost()))
                        .setScale(2, java.math.RoundingMode.DOWN));
        ws.setWkAvail(ws.getSkOnhand().subtract(ws.getSkAllocated()).intValue());
        ws.setWkOnhandTot(
                (BigDecimal.valueOf(ws.getWkOnhandTot()).add(ws.getSkOnhand())).longValue());
        ws.setWkWhOnhand(
                (BigDecimal.valueOf(ws.getWkWhOnhand()).add(ws.getSkOnhand())).longValue());
        ws.setWkAvailTot(ws.getWkAvailTot() + ws.getWkAvail());
        ws.setWkValTot(ws.getWkValTot().add(ws.getWkItemVal()));
        ws.setWkWhVal(ws.getWkWhVal().add(ws.getWkItemVal()));
        ws.setWkYtdinTot((BigDecimal.valueOf(ws.getWkYtdinTot()).add(ws.getSkYtdIn())).longValue());
        ws.setWkYtdoutTot(
                (BigDecimal.valueOf(ws.getWkYtdoutTot()).add(ws.getSkYtdOut())).longValue());
        ws.setWkWhItems(ws.getWkWhItems() + 1);
        if (ws.getWkItemVal().signum() == 0) {
            ws.setWkZerovalCnt(ws.getWkZerovalCnt() + 1);
        }
        if ((ws.getSkOnhand().signum() < 0)) {
            ws.setWkNegCnt(ws.getWkNegCnt() + 1);
            ws.setWkEProd(ws.getSkProd());
            ws.setWkEWhse(ws.getSkWhse());
            ws.setWkEQty(ws.getSkOnhand().longValue());
            log.info(
                    "   NEG prod {} wh {} onhand {}",
                    ws.editedDisplay("WK-E-PROD"),
                    ws.editedDisplay("WK-E-WHSE"),
                    ws.editedDisplay("WK-E-QTY"));
        }
        if (ws.getSkOnhand().signum() == 0) {
            ws.setWkZeroCnt(ws.getWkZeroCnt() + 1);
        }
        runChain(this::housekeepStockFields);
        if (ws.getWkChgFlg() == 1) {
            ws.setSkLastOutDate(ws.getWkSysdate());
            fileSet.getStokf().rewrite();
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            if (fileSet.getStokf().isInvalidKey()) {
                ws.setKaFile("STOKF");
                ws.setKaDetail("REWRITE STOKF failed");
                runChain(this::abortProgram);
            }
            ws.setWkAdjCnt(ws.getWkAdjCnt() + 1);
        }
    }

    /** COBOL paragraph: HOUS-010 */
    private void housekeepStockFields() {
        if ((ws.getSkOnhand().signum() <= 0) && ws.getSkAvgCost().signum() != 0) {
            ws.setSkAvgCost(BigDecimal.ZERO);
            ws.setWkChgFlg(1);
        }
        if ((ws.getSkAllocated().signum() < 0)) {
            ws.setSkAllocated(BigDecimal.ZERO);
            ws.setWkChgFlg(1);
        }
        if ((ws.getSkOnOrder().signum() < 0)) {
            ws.setSkOnOrder(BigDecimal.ZERO);
            ws.setWkChgFlg(1);
        }
    }

    /** COBOL paragraph: CLSB-010 */
    private void resetWarehouseSubtotals() {
        ws.setWkWhItems(0);
        ws.setWkWhOnhand(0);
        ws.setWkWhVal(BigDecimal.ZERO);
    }

    /** COBOL paragraph: PRSB-010 */
    private void printWarehouseSubtotal() {
        ws.setWkWhseCnt(ws.getWkWhseCnt() + 1);
        ws.setWkEWhse(ws.getWkPrevWhse());
        ws.setWkECnt(ws.getWkWhItems());
        ws.setWkEQty(ws.getWkWhOnhand());
        ws.setWkEVal(ws.getWkWhVal());
        log.info(
                " {} {} {} {}",
                ws.editedDisplay("WK-E-WHSE"),
                ws.editedDisplay("WK-E-CNT"),
                ws.editedDisplay("WK-E-QTY"),
                ws.editedDisplay("WK-E-VAL"));
    }

    /** COBOL paragraph: PSUM-010 */
    private void printSummaryReport() {
        log.info("----------------------------------------------");
        log.info(" STOCK MONTHLY UPDATE SUMMARY");
        log.info("----------------------------------------------");
        ws.setWkECnt(ws.getWkReadCnt());
        log.info(" Stock rows read   : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkWhseCnt());
        log.info(" Warehouses        : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkAdjCnt());
        log.info(" Rows adjusted     : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkZeroCnt());
        log.info(" Zero-onhand rows  : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkNegCnt());
        log.info(" Negative-onhand   : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkZerovalCnt());
        log.info(" Zero-value rows   : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkEQty(ws.getWkOnhandTot());
        log.info(" Total onhand qty  : {}", ws.editedDisplay("WK-E-QTY"));
        ws.setWkEQty(ws.getWkAvailTot());
        log.info(" Total available   : {}", ws.editedDisplay("WK-E-QTY"));
        ws.setWkEQty(ws.getWkYtdinTot());
        log.info(" Total YTD in      : {}", ws.editedDisplay("WK-E-QTY"));
        ws.setWkEQty(ws.getWkYtdoutTot());
        log.info(" Total YTD out     : {}", ws.editedDisplay("WK-E-QTY"));
        ws.setWkEVal(ws.getWkValTot());
        log.info(" Total valuation   : {}", ws.editedDisplay("WK-E-VAL"));
        log.info("----------------------------------------------");
        log.info(" BT0030 completed normally.");
    }

    /** COBOL paragraph: TERM-010 */
    private void closeProgramFiles() {
        fileSet.getStokf().close();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        fileSet.getSyscf().close();
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
    }

    /** COBOL paragraph: ABND-010 */
    private void abortProgram() {
        ws.setKaProgid("BT0030");
        ws.setKaFsts(ws.getFsts());
        ws.setKaMsgcode("EBATCH");
        callAbortxService(ws.getKabend());
        fileSet.getStokf().close();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        fileSet.getSyscf().close();
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        ws.setCompletionCode(255);
        throw new ProgramExitSignal();
    }

    /** COBOL CALL DATEUT — delegates to injected DateutService. */
    private void callDateutService(Object... args) {
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
    private void callAbortxService(Object... args) {
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

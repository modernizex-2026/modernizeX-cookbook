package com.sakura.rp0060.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0060.domain.Rp0060FieldAccess;
import com.sakura.rp0060.domain.WorkingStorage;
import com.sakura.rp0060.runtime.Rp0060Datasets;
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

/** Business logic service generated from COBOL program RP0060. */
@Service
@Scope("prototype")
public class Rp0060Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Rp0060Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Rp0060FieldAccess ws;

    public Rp0060Service(
            Rp0060Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Rp0060FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::printStockValuationReport);
        runChain(this::closeFilesAndLogSummary);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("RP0060");
        ws.setWkTitle("INVENTORY VALUATION LIST");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysdate(ws.getKdDate1());
        ws.setWkCompany("SAKURA Sales Management System");
        fileSet.getSyscf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "00")) {
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
            if (!fileSet.getSyscf().isInvalidKey()) {
                ws.setWkCompany(ws.getSyCompanyName());
            }
            fileSet.getSyscf().close();
            ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        }
        fileSet.getStokf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")
                && !Utility.fieldEquals(ws.getFsts(), "35")
                && !Utility.fieldEquals(ws.getFsts(), "30")) {
            ws.setKaFile("STOKF");
            runChain(this::abortOnFileOpenError);
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setWkMainEof(1);
        }
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getWhsef().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
        fileSet.getRepf().open(FileOpenMode.OUTPUT);
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("REPF");
            runChain(this::abortOnFileOpenError);
        }
    }

    /** COBOL paragraph: PRINT-010 */
    private void printStockValuationReport() {
        ws.setWkGCnt(0);
        ws.setWkLine(99);
        if ((ws.getWkMainEof() != 1)) {
            ws.setSkWhse(0);
            ws.setSkProd(0);
            fileSet.getStokf().start("SK-WHSE", "NOT LESS THAN");
            ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
            if (fileSet.getStokf().isInvalidKey()) {
                ws.setWkMainEof(1);
            }
        }
        while ((ws.getWkMainEof() != 1)) {
            runChain(this::readNextStockRecord);
        }
        if (ws.getWkFirst() == 0) {
            runChain(this::printWarehouseSubtotal);
            runChain(this::printGrandTotal);
        } else {
            runChain(this::printPageHeader);
            ws.setRepRec("*** NO STOCK RECORDS ***");
            fileSet.getRepf().write();
            ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        }
    }

    /** COBOL paragraph: RN-010 */
    private void readNextStockRecord() {
        fileSet.getStokf().readNext();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isAtEnd()) {
            ws.setWkMainEof(1);
        }
        if (!fileSet.getStokf().isAtEnd()) {
            runChain(this::processStockRecord);
        }
    }

    /** COBOL paragraph: PO-010 */
    private void processStockRecord() {
        if (ws.getWkFirst() == 1) {
            ws.setWkCurWhse(ws.getSkWhse());
            runChain(this::resetSubtotals);
            runChain(this::printWarehouseHeader);
            ws.setWkFirst(0);
        } else {
            if (ws.getSkWhse() != ws.getWkCurWhse()) {
                runChain(this::printWarehouseSubtotal);
                ws.setWkCurWhse(ws.getSkWhse());
                runChain(this::resetSubtotals);
                runChain(this::printWarehouseHeader);
            }
        }
        runChain(this::printProductDetailLine);
    }

    /** COBOL paragraph: RS-010 */
    private void resetSubtotals() {
        ws.setWkSubQty(BigDecimal.ZERO);
        ws.setWkSubVal(BigDecimal.ZERO);
    }

    /** COBOL paragraph: PW-010 */
    private void printWarehouseHeader() {
        runChain(this::checkPageBreak);
        ws.setWkWhName(" ");
        ws.setWhCode(ws.getWkCurWhse());
        String rkVal_1 = "";
        if (rkVal_1 == null || rkVal_1.trim().isEmpty()) {
            try {
                rkVal_1 = ws.getString("WH-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_1 == null || rkVal_1.trim().isEmpty()) {
            try {
                rkVal_1 = fileSet.getWhsef().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getWhsef().readByKey(rkVal_1 != null ? rkVal_1.trim() : "");
        ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
        if (fileSet.getWhsef().isInvalidKey()) {
            ws.setWkWhName("(unknown)");
        }
        if (!fileSet.getWhsef().isInvalidKey()) {
            ws.setWkWhName(ws.getWhName());
        }
        ws.setRepRec(" ");
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setWhdCode(ws.getWkCurWhse());
        ws.setWhdName(ws.getWkWhName());
        ws.copyRepRecFromRdWhd();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
    }

    /** COBOL paragraph: PDT-010 */
    private void printProductDetailLine() {
        runChain(this::checkPageBreak);
        ws.setWkProdName(" ");
        ws.setPrCode(ws.getSkProd());
        String rkVal_2 = "";
        if (rkVal_2 == null || rkVal_2.trim().isEmpty()) {
            try {
                rkVal_2 = ws.getString("PR-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_2 == null || rkVal_2.trim().isEmpty()) {
            try {
                rkVal_2 = fileSet.getProdf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getProdf().readByKey(rkVal_2 != null ? rkVal_2.trim() : "");
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName("(unknown)");
        }
        if (!fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName(ws.getPrName());
        }
        ws.setWkVal(
                (ws.getSkOnhand().multiply(ws.getSkAvgCost()))
                        .setScale(2, java.math.RoundingMode.DOWN));
        ws.setRsProd(ws.getSkProd());
        ws.setRsName(ws.getWkProdName());
        ws.setRsOnhand(ws.getSkOnhand().longValue());
        ws.setRsAvgcost(ws.getSkAvgCost());
        ws.setRsValue(ws.getWkVal());
        ws.copyRepRecFromRdLine();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setWkSubQty(ws.getWkSubQty().add(ws.getSkOnhand()));
        ws.setWkGQty(ws.getWkGQty().add(ws.getSkOnhand()));
        ws.setWkSubVal(ws.getWkSubVal().add(ws.getWkVal()));
        ws.setWkGVal(ws.getWkGVal().add(ws.getWkVal()));
        ws.setWkGCnt(ws.getWkGCnt() + 1);
    }

    /** COBOL paragraph: PSB-010 */
    private void printWarehouseSubtotal() {
        runChain(this::checkPageBreak);
        ws.setSubQty(ws.getWkSubQty().longValue());
        ws.setSubVal(ws.getWkSubVal());
        ws.copyRepRecFromRdSub();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
    }

    /** COBOL paragraph: PG-010 */
    private void printGrandTotal() {
        runChain(this::checkPageBreak);
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setGtQty(ws.getWkGQty().longValue());
        ws.setGtVal(ws.getWkGVal());
        ws.copyRepRecFromRdGrand();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
    }

    /** COBOL paragraph: CP-010 */
    private void checkPageBreak() {
        if (ws.getWkLine() >= 55) {
            runChain(this::printPageHeader);
        }
    }

    /** COBOL paragraph: PH-010 */
    private void printPageHeader() {
        ws.setWkPage(ws.getWkPage() + 1);
        ws.setH1Company(ws.getWkCompany());
        ws.setH1Title(ws.getWkTitle());
        ws.setH1Page(ws.getWkPage());
        ws.copyRepRecFromRptH1();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setH2Date(ws.getWkSysdate());
        ws.setH2Info(" ");
        ws.copyRepRecFromRptH2();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setRepRec(" ");
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.copyRepRecFromRhLine();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(6);
    }

    /** COBOL paragraph: TERM-010 */
    private void closeFilesAndLogSummary() {
        fileSet.getStokf().close();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getWhsef().close();
        ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
        fileSet.getRepf().close();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        log.info(
                "RP0060 complete.  Stock lines = {}",
                String.format("%07d", (long) (ws.getWkGCnt())));
        ws.setCompletionCode(0);
    }

    /** COBOL paragraph: AB-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("RP0060");
        ws.setKaFsts(ws.getFsts());
        ws.setKaMsgcode("EOPEN ");
        ws.setKaDetail("Report file error");
        abortx(ws.getKabend());
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

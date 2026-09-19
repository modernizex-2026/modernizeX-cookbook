package com.sakura.rp0140.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0140.domain.Rp0140FieldAccess;
import com.sakura.rp0140.domain.WorkingStorage;
import com.sakura.rp0140.runtime.Rp0140Datasets;
import com.sakura.runtime.BatchServiceBase;
import com.sakura.runtime.DatasetEnums.*;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.AbstractDatasets;
import com.sakura.runtime.linkage.AbortxLinkParm;
import com.sakura.runtime.linkage.DateutLinkParm;
import com.sakura.runtime.record.RawDatasetBase;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program RP0140. */
@Service
@Scope("prototype")
public class Rp0140Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Rp0140Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Rp0140FieldAccess ws;

    /** Sentinel for "days since last outbound movement" when no valid value could be computed. */
    private static final int SENTINEL_WKDAYS_UNKNOWN = 99999;

    public Rp0140Service(
            Rp0140Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Rp0140FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::printStockReport);
        runChain(this::finalizeAndCloseFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("RP0140");
        ws.setWkTitle("DEAD / SLOW-MOVING STOCK");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysdate(ws.getKdDate1());
        ws.setWkCompany("SAKURA Sales Management System");
        fileSet.getSyscf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (isFstsOk()) {
            ws.setSyKey(1);
            fileSet.getSyscf().readByKey(resolveReadKey("SY-KEY", fileSet.getSyscf()));
            ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
            if (fileSet.getSyscf().isInvalidKey()) {
                /* CONTINUE */
            }
            if (!fileSet.getSyscf().isInvalidKey()) {
                ws.setWkCompany(ws.getSyCompanyName());
            }
            fileSet.getSyscf().close();
            ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        }
        fileSet.getStokf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (!isFstsOk()
                && !Utility.fieldEquals(ws.getFsts(), "35")
                && !Utility.fieldEquals(ws.getFsts(), "30")) {
            ws.setKaFile("STOKF");
            runChain(this::abortProgram);
        }
        if (!isFstsOk()) {
            ws.setWkMainEof(1);
        }
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getWhsef().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
        fileSet.getRepf().open(FileOpenMode.OUTPUT);
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        if (!isFstsOk()) {
            ws.setKaFile("REPF");
            runChain(this::abortProgram);
        }
    }

    /** COBOL paragraph: PRINT-010 */
    private void printStockReport() {
        ws.setWkLine(99);
        ws.setWkFirst(1);
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
            runChain(this::writeWarehouseSubtotal);
        }
        runChain(this::printGrandTotal);
    }

    /** COBOL paragraph: RS-010 */
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
        if ((ws.getSkOnhand().signum() <= 0)) {
            return;
        }
        if (ws.getWkFirst() == 1) {
            ws.setWkCurWhse(ws.getSkWhse());
            runChain(this::resetWarehouseTotals);
            runChain(this::startNewWarehouse);
            ws.setWkFirst(0);
        } else {
            if (ws.getSkWhse() != ws.getWkCurWhse()) {
                runChain(this::writeWarehouseSubtotal);
                ws.setWkCurWhse(ws.getSkWhse());
                runChain(this::resetWarehouseTotals);
                runChain(this::startNewWarehouse);
            }
        }
        runChain(this::computeIdleDaysAndFlag);
        runChain(this::printItemDetailLine);
    }

    /** COBOL paragraph: SW-010 */
    private void startNewWarehouse() {
        runChain(this::lookupWarehouseName);
        runChain(this::checkPageBreak);
        runChain(this::writeWarehouseHeader);
    }

    /** COBOL paragraph: PWH-010 */
    private void writeWarehouseHeader() {
        ws.setWhhCode(ws.getWkCurWhse());
        ws.setWhhName(ws.getWkWhName());
        ws.copyRepRecFromRdWhse();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
        ws.copyRepRecFromRcHead();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
    }

    /** COBOL paragraph: CI-010 */
    private void computeIdleDaysAndFlag() {
        if (ws.getSkLastOutDate() == 0) {
            ws.setWkDays(SENTINEL_WKDAYS_UNKNOWN);
        } else {
            ws.setKdFunc("DIFF");
            ws.setKdDate1(ws.getSkLastOutDate());
            ws.setKdDate2(ws.getWkSysdate());
            dateut(ws.getKdate());
            if (Utility.fieldEquals(ws.getKdStatus(), "00")) {
                ws.setWkDays(ws.getKdDays());
            } else {
                ws.setWkDays(SENTINEL_WKDAYS_UNKNOWN);
            }
        }
        if (ws.getWkDays() < 0) {
            ws.setWkDays(0);
        }
        ws.setWkValue(
                (ws.getSkOnhand().multiply(ws.getSkAvgCost()))
                        .setScale(0, java.math.RoundingMode.HALF_UP));
        if (ws.getWkDays() > 180) {
            ws.setWkFlagTxt("DEAD");
        } else if (ws.getWkDays() > 90) {
            ws.setWkFlagTxt("SLOW");
        } else {
            ws.setWkFlagTxt("OK");
        }
    }

    /** COBOL paragraph: PI-010 */
    private void printItemDetailLine() {
        runChain(this::checkPageBreakAndReprintHeaders);
        runChain(this::lookupProductName);
        ws.setRlProd(ws.getSkProd());
        ws.setRlName(ws.getWkProdName());
        ws.setRlOnhand(ws.getSkOnhand().longValue());
        ws.setRlAvgcost(ws.getSkAvgCost());
        ws.setRlValue(ws.getWkValue().longValue());
        ws.setRlLastout(ws.getSkLastOutDate());
        if (ws.getWkDays() > SENTINEL_WKDAYS_UNKNOWN) {
            ws.setRlDays(SENTINEL_WKDAYS_UNKNOWN);
        } else {
            ws.setRlDays(ws.getWkDays());
        }
        ws.setRlFlag(ws.getWkFlagTxt());
        ws.copyRepRecFromRdLine();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setWkWhItems(ws.getWkWhItems() + 1);
        ws.setWkGItems(ws.getWkGItems() + 1);
        ws.setWkWhValue(ws.getWkWhValue().add(ws.getWkValue()));
        ws.setWkGValue(ws.getWkGValue().add(ws.getWkValue()));
        if (ws.getWkDays() > 90) {
            ws.setWkWhIdleCnt(ws.getWkWhIdleCnt() + 1);
            ws.setWkGIdleCnt(ws.getWkGIdleCnt() + 1);
            ws.setWkWhIdleValue(ws.getWkWhIdleValue().add(ws.getWkValue()));
            ws.setWkGIdleValue(ws.getWkGIdleValue().add(ws.getWkValue()));
        }
    }

    /** COBOL paragraph: LKP-010 */
    private void lookupProductName() {
        ws.setWkProdName(" ");
        ws.setPrCode(ws.getSkProd());
        fileSet.getProdf().readByKey(resolveReadKey("PR-CODE", fileSet.getProdf()));
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName("(unknown product)");
        }
        if (!fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName(ws.getPrName());
        }
    }

    /** COBOL paragraph: LKW-010 */
    private void lookupWarehouseName() {
        ws.setWkWhName(" ");
        ws.setWhCode(ws.getWkCurWhse());
        fileSet.getWhsef().readByKey(resolveReadKey("WH-CODE", fileSet.getWhsef()));
        ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
        if (fileSet.getWhsef().isInvalidKey()) {
            ws.setWkWhName("(unknown warehouse)");
        }
        if (!fileSet.getWhsef().isInvalidKey()) {
            ws.setWkWhName(ws.getWhName());
        }
    }

    /** COBOL paragraph: RW-010 */
    private void resetWarehouseTotals() {
        ws.setWkWhItems(0);
        ws.setWkWhIdleCnt(0);
        ws.setWkWhValue(BigDecimal.ZERO);
        ws.setWkWhIdleValue(BigDecimal.ZERO);
    }

    /** COBOL paragraph: FW-010 */
    private void writeWarehouseSubtotal() {
        runChain(this::checkPageBreakAndReprintHeaders);
        ws.setStItems(ws.getWkWhItems());
        ws.setStIdle(ws.getWkWhIdleCnt());
        ws.setStValue(ws.getWkWhValue().longValue());
        ws.setStIdleval(ws.getWkWhIdleValue().longValue());
        ws.copyRepRecFromRdSub();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setRepRec(" ");
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
    }

    /** COBOL paragraph: CP-010 */
    private void checkPageBreakAndReprintHeaders() {
        if (ws.getWkLine() >= 55) {
            runChain(this::printPageHeader);
            if (ws.getWkFirst() == 0) {
                runChain(this::writeWarehouseHeader);
            }
        }
    }

    /** COBOL paragraph: POY-010 */
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
        ws.setH2Info("SLOW > 90 DAYS   DEAD > 180 DAYS");
        ws.copyRepRecFromRptH2();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setRepRec(" ");
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(4);
    }

    /** COBOL paragraph: PG-010 */
    private void printGrandTotal() {
        if (ws.getWkGItems() == 0) {
            runChain(this::checkPageBreak);
            ws.setRepRec("*** NO STOCK ON HAND ***");
            fileSet.getRepf().write();
            ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
            return;
        }
        runChain(this::checkPageBreakAndReprintHeaders);
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setGtItems(ws.getWkGItems());
        ws.setGtIdle(ws.getWkGIdleCnt());
        ws.setGtValue(ws.getWkGValue().longValue());
        ws.setGtIdleval(ws.getWkGIdleValue().longValue());
        ws.copyRepRecFromRdGrand();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
    }

    /** COBOL paragraph: TERM-010 */
    private void finalizeAndCloseFiles() {
        fileSet.getStokf().close();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getWhsef().close();
        ws.trySetString("FSTS", fileSet.getWhsef().getFileStatus());
        fileSet.getRepf().close();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        log.info(
                "RP0140 complete.  Stock rows reported = {}",
                String.format("%07d", (long) (ws.getWkGItems())));
        log.info(
                "         Idle (>90 days) items         = {}",
                String.format("%07d", (long) (ws.getWkGIdleCnt())));
        ws.setCompletionCode(0);
    }

    /** COBOL paragraph: AB-010 */
    private void abortProgram() {
        ws.setKaProgid("RP0140");
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

    /** Whether the last file operation's status (FSTS) reported success. */
    private boolean isFstsOk() {
        return Utility.fieldEquals(ws.getFsts(), "00");
    }

    /**
     * Resolve the key to read by: prefer the WS field value, fall back to the current record's key.
     */
    private String resolveReadKey(String cobolFieldName, RawDatasetBase file) {
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString(cobolFieldName);
            } catch (Exception _e) {
            }
        }
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = file.extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        return rkVal != null ? rkVal.trim() : "";
    }
}

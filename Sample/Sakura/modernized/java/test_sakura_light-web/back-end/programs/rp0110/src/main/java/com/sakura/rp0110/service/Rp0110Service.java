package com.sakura.rp0110.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0110.domain.Rp0110FieldAccess;
import com.sakura.rp0110.domain.WorkingStorage;
import com.sakura.rp0110.runtime.Rp0110Datasets;
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

/** Business logic service generated from COBOL program RP0110. */
@Service
@Scope("prototype")
public class Rp0110Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Rp0110Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Rp0110FieldAccess ws;

    public Rp0110Service(
            Rp0110Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Rp0110FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::parseSupplierFilterParm);
        runChain(this::printReorderReport);
        runChain(this::terminateProgram);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("RP0110");
        ws.setWkTitle("REORDER SUGGESTION REPORT");
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
            if (fileSet.getSyscf().isInvalidKey()) {
                /* CONTINUE */
            }
            if (!fileSet.getSyscf().isInvalidKey()) {
                ws.setWkCompany(ws.getSyCompanyName());
            }
            fileSet.getSyscf().close();
            ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        }
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")
                && !Utility.fieldEquals(ws.getFsts(), "35")
                && !Utility.fieldEquals(ws.getFsts(), "30")) {
            ws.setKaFile("PRODF");
            runChain(this::abortProgram);
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setWkMainEof(1);
        }
        fileSet.getStokf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        fileSet.getSuppf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        fileSet.getRepf().open(FileOpenMode.OUTPUT);
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("REPF");
            runChain(this::abortProgram);
        }
    }

    /** COBOL paragraph: PARM-010 */
    private void parseSupplierFilterParm() {
        log.info("RP0110 - Reorder suggestion report");
        log.info("Default-supplier filter code (blank=all): ");
        String stdinValWkInSupp1 = Utility.readStdinLine();
        if (stdinValWkInSupp1 == null || stdinValWkInSupp1.trim().isEmpty()) {
            stdinValWkInSupp1 = "";
        }
        ws.setWkInSupp(stdinValWkInSupp1);
        if (Utility.isNumeric(String.valueOf(ws.getWkInSupp()))
                && !Utility.fieldEquals(ws.getWkInSupp(), " ")) {
            ws.setWkSuppFilter(Utility.parseNumeric(ws.getWkInSupp()).intValue());
        } else {
            ws.setWkSuppFilter(0);
        }
    }

    /** COBOL paragraph: PRINT-010 */
    private void printReorderReport() {
        ws.setWkScanCnt(0);
        ws.setWkSugCnt(0);
        ws.setWkLine(99);
        if ((ws.getWkMainEof() != 1)) {
            ws.setPrCode(0);
            fileSet.getProdf().start("PR-CODE", "NOT LESS THAN");
            ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
            if (fileSet.getProdf().isInvalidKey()) {
                ws.setWkMainEof(1);
            }
        }
        while ((ws.getWkMainEof() != 1)) {
            runChain(this::readNextProductRecord);
        }
        runChain(this::printReportTotals);
    }

    /** COBOL paragraph: RDP-010 */
    private void readNextProductRecord() {
        fileSet.getProdf().readNext();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (fileSet.getProdf().isAtEnd()) {
            ws.setWkMainEof(1);
        }
        if (!fileSet.getProdf().isAtEnd()) {
            runChain(this::evaluateProductForReorder);
        }
    }

    /** COBOL paragraph: PP-010 */
    private void evaluateProductForReorder() {
        ws.setWkScanCnt(ws.getWkScanCnt() + 1);
        if (ws.getPrDelFlag() == 1) {
            return;
        }
        if (ws.getPrStockMng() != 1) {
            return;
        }
        if (ws.getWkSuppFilter() != 0) {
            if (ws.getPrDfltSupp() != ws.getWkSuppFilter()) {
                return;
            }
        }
        runChain(this::summarizeStockLevels);
        ws.setWkProjected((ws.getWkOnhandSum() + ws.getWkOnorderSum()));
        if ((BigDecimal.valueOf((long) (ws.getWkProjected())).compareTo(ws.getPrReorderPoint())
                > 0)) {
            return;
        }
        runChain(this::printReorderSuggestionLine);
    }

    /** COBOL paragraph: SS-010 */
    private void summarizeStockLevels() {
        ws.setWkOnhandSum(0);
        ws.setWkOnorderSum(0);
        ws.setWkStkEof(0);
        ws.setSkProd(ws.getPrCode());
        ws.setSkWhse(0);
        fileSet.getStokf().start("SK-PROD", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isInvalidKey()) {
            ws.setWkStkEof(1);
        }
        while ((ws.getWkStkEof() != 1)) {
            runChain(this::accumulateStockRecord);
        }
    }

    /** COBOL paragraph: SSR-010 */
    private void accumulateStockRecord() {
        fileSet.getStokf().readNext();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        if (fileSet.getStokf().isAtEnd()) {
            ws.setWkStkEof(1);
        }
        if (!fileSet.getStokf().isAtEnd()) {
            if (ws.getSkProd() != ws.getPrCode()) {
                ws.setWkStkEof(1);
            } else {
                ws.setWkOnhandSum(
                        (BigDecimal.valueOf(ws.getWkOnhandSum()).add(ws.getSkOnhand()))
                                .longValue());
                ws.setWkOnorderSum(
                        (BigDecimal.valueOf(ws.getWkOnorderSum()).add(ws.getSkOnOrder()))
                                .longValue());
            }
        }
    }

    /** COBOL paragraph: PO-010 */
    private void printReorderSuggestionLine() {
        runChain(this::checkPageBreak);
        runChain(this::lookupSupplierName);
        ws.setWkSugValue(
                (ws.getPrReorderQty().multiply(ws.getPrStdCost()))
                        .setScale(0, java.math.RoundingMode.HALF_UP));
        ws.setRlProd(ws.getPrCode());
        ws.setRlName(ws.getPrName());
        ws.setRlOnhand(ws.getWkOnhandSum());
        ws.setRlOnord(ws.getWkOnorderSum());
        ws.setRlRpoint(ws.getPrReorderPoint().longValue());
        ws.setRlSugqty(ws.getPrReorderQty().longValue());
        ws.setRlValue(ws.getWkSugValue().longValue());
        ws.setRlLead(ws.getPrLeadDays());
        ws.setRlSupp(ws.getWkSuppName());
        ws.copyRepRecFromRdLine();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setWkSugCnt(ws.getWkSugCnt() + 1);
        ws.setWkGItems(ws.getWkGItems() + 1);
        ws.setWkGQty(ws.getWkGQty().add(ws.getPrReorderQty()));
        ws.setWkGValue(ws.getWkGValue().add(ws.getWkSugValue()));
    }

    /** COBOL paragraph: LKS-010 */
    private void lookupSupplierName() {
        ws.setWkSuppName(" ");
        if (ws.getPrDfltSupp() == 0) {
            ws.setWkSuppName("(no default supplier)");
            return;
        }
        ws.setSpCode(ws.getPrDfltSupp());
        String rkVal_2 = "";
        if (rkVal_2 == null || rkVal_2.trim().isEmpty()) {
            try {
                rkVal_2 = ws.getString("SP-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_2 == null || rkVal_2.trim().isEmpty()) {
            try {
                rkVal_2 = fileSet.getSuppf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getSuppf().readByKey(rkVal_2 != null ? rkVal_2.trim() : "");
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setWkSuppName("(unknown supplier)");
        }
        if (!fileSet.getSuppf().isInvalidKey()) {
            ws.setWkSuppName(ws.getSpName());
        }
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
        if (ws.getWkSuppFilter() == 0) {
            ws.setH2Info("SUPPLIER: ALL");
        } else {
            ws.setH2Info("SUPPLIER: " + String.format("%06d", (long) (ws.getWkSuppFilter())));
        }
        ws.copyRepRecFromRptH2();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setRepRec(" ");
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.copyRepRecFromRcHead();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(6);
    }

    /** COBOL paragraph: PG-010 */
    private void printReportTotals() {
        if (ws.getWkSugCnt() == 0) {
            runChain(this::checkPageBreak);
            ws.setRepRec("*** NO REORDER SUGGESTIONS ***");
            fileSet.getRepf().write();
            ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
            return;
        }
        runChain(this::checkPageBreak);
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setRtItems(ws.getWkGItems());
        ws.setRtQty(ws.getWkGQty().longValue());
        ws.setRtValue(ws.getWkGValue().longValue());
        ws.copyRepRecFromRtLine();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
    }

    /** COBOL paragraph: TERM-010 */
    private void terminateProgram() {
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getStokf().close();
        ws.trySetString("FSTS", fileSet.getStokf().getFileStatus());
        fileSet.getSuppf().close();
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        fileSet.getRepf().close();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        log.info(
                "RP0110 complete.  Products scanned = {}",
                String.format("%07d", (long) (ws.getWkScanCnt())));
        log.info(
                "         Reorder suggestions        = {}",
                String.format("%07d", (long) (ws.getWkSugCnt())));
        ws.setCompletionCode(0);
    }

    /** COBOL paragraph: AB-010 */
    private void abortProgram() {
        ws.setKaProgid("RP0110");
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

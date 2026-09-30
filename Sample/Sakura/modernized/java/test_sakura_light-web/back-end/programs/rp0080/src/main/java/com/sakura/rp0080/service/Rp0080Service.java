package com.sakura.rp0080.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0080.domain.Rp0080FieldAccess;
import com.sakura.rp0080.domain.WorkingStorage;
import com.sakura.rp0080.runtime.Rp0080Datasets;
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

/** Business logic service generated from COBOL program RP0080. */
@Service
@Scope("prototype")
public class Rp0080Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Rp0080Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Rp0080FieldAccess ws;

    public Rp0080Service(
            Rp0080Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Rp0080FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::generateApBalanceReport);
        runChain(this::finalizeAndCloseFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("RP0080");
        ws.setWkTitle("ACCOUNTS PAYABLE BALANCE");
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
        fileSet.getAplf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")
                && !Utility.fieldEquals(ws.getFsts(), "35")
                && !Utility.fieldEquals(ws.getFsts(), "30")) {
            ws.setKaFile("APLF");
            runChain(this::abortProgram);
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setWkMainEof(1);
        }
        fileSet.getSuppf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        fileSet.getRepf().open(FileOpenMode.OUTPUT);
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("REPF");
            runChain(this::abortProgram);
        }
    }

    /** COBOL paragraph: PRINT-010 */
    private void generateApBalanceReport() {
        ws.setWkGCnt(0);
        ws.setWkLine(99);
        if ((ws.getWkMainEof() != 1)) {
            ws.setPlSupp(0);
            ws.setPlDate(0);
            fileSet.getAplf().start("PL-SUPP", "NOT LESS THAN");
            ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
            if (fileSet.getAplf().isInvalidKey()) {
                ws.setWkMainEof(1);
            }
        }
        while ((ws.getWkMainEof() != 1)) {
            runChain(this::readNextAplRecord);
        }
        if (ws.getWkFirst() == 0) {
            runChain(this::writeSupplierSummaryLine);
        }
        runChain(this::printGrandTotal);
    }

    /** COBOL paragraph: RN-010 */
    private void readNextAplRecord() {
        fileSet.getAplf().readNext();
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        if (fileSet.getAplf().isAtEnd()) {
            ws.setWkMainEof(1);
        }
        if (!fileSet.getAplf().isAtEnd()) {
            runChain(this::accumulateApRecord);
        }
    }

    /** COBOL paragraph: PO-010 */
    private void accumulateApRecord() {
        if (ws.getWkFirst() == 1) {
            ws.setWkCurSupp(ws.getPlSupp());
            runChain(this::resetSupplierAccumulators);
            ws.setWkFirst(0);
        } else {
            if (ws.getPlSupp() != ws.getWkCurSupp()) {
                runChain(this::writeSupplierSummaryLine);
                ws.setWkCurSupp(ws.getPlSupp());
                runChain(this::resetSupplierAccumulators);
            }
        }
        ws.setWkCurDr(ws.getWkCurDr().add(ws.getPlDebit()));
        ws.setWkCurCr(ws.getWkCurCr().add(ws.getPlCredit()));
        ws.setWkCurCnt(ws.getWkCurCnt() + 1);
    }

    /** COBOL paragraph: RA-010 */
    private void resetSupplierAccumulators() {
        ws.setWkCurDr(BigDecimal.ZERO);
        ws.setWkCurCr(BigDecimal.ZERO);
        ws.setWkCurCnt(0);
    }

    /** COBOL paragraph: FS-010 */
    private void writeSupplierSummaryLine() {
        if (ws.getWkCurCnt() == 0) {
            return;
        }
        ws.setWkCurBal(
                (ws.getWkCurCr().subtract(ws.getWkCurDr()))
                        .setScale(0, java.math.RoundingMode.DOWN));
        runChain(this::checkPageBreak);
        ws.setWkSuppName(" ");
        ws.setSpCode(ws.getWkCurSupp());
        String rkVal_1 = "";
        if (rkVal_1 == null || rkVal_1.trim().isEmpty()) {
            try {
                rkVal_1 = ws.getString("SP-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_1 == null || rkVal_1.trim().isEmpty()) {
            try {
                rkVal_1 = fileSet.getSuppf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getSuppf().readByKey(rkVal_1 != null ? rkVal_1.trim() : "");
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setWkSuppName("(unknown)");
        }
        if (!fileSet.getSuppf().isInvalidKey()) {
            ws.setWkSuppName(ws.getSpName());
        }
        ws.setRsCode(ws.getWkCurSupp());
        ws.setRsName(ws.getWkSuppName());
        ws.setRsDr(ws.getWkCurDr().longValue());
        ws.setRsCr(ws.getWkCurCr().longValue());
        ws.setRsBal(ws.getWkCurBal().longValue());
        ws.copyRepRecFromRdLine();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setWkGDr(ws.getWkGDr().add(ws.getWkCurDr()));
        ws.setWkGCr(ws.getWkGCr().add(ws.getWkCurCr()));
        ws.setWkGBal(ws.getWkGBal().add(ws.getWkCurBal()));
        ws.setWkGCnt(ws.getWkGCnt() + 1);
    }

    /** COBOL paragraph: CP-010 */
    private void checkPageBreak() {
        if (ws.getWkLine() >= 55) {
            runChain(this::printReportHeader);
        }
    }

    /** COBOL paragraph: PH-010 */
    private void printReportHeader() {
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

    /** COBOL paragraph: PG-010 */
    private void printGrandTotal() {
        if (ws.getWkGCnt() == 0) {
            runChain(this::checkPageBreak);
            ws.setRepRec("*** NO PAYABLES ON FILE ***");
            fileSet.getRepf().write();
            ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
            return;
        }
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setRtDr(ws.getWkGDr().longValue());
        ws.setRtCr(ws.getWkGCr().longValue());
        ws.setRtBal(ws.getWkGBal().longValue());
        ws.copyRepRecFromRtLine();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
    }

    /** COBOL paragraph: TERM-010 */
    private void finalizeAndCloseFiles() {
        fileSet.getAplf().close();
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        fileSet.getSuppf().close();
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        fileSet.getRepf().close();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        log.info(
                "RP0080 complete.  Suppliers = {}", String.format("%07d", (long) (ws.getWkGCnt())));
        ws.setCompletionCode(0);
    }

    /** COBOL paragraph: AB-010 */
    private void abortProgram() {
        ws.setKaProgid("RP0080");
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

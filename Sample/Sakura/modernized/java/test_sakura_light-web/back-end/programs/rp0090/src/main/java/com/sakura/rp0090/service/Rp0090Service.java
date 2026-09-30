package com.sakura.rp0090.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0090.domain.Rp0090FieldAccess;
import com.sakura.rp0090.domain.WorkingStorage;
import com.sakura.rp0090.runtime.Rp0090Datasets;
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

/** Business logic service generated from COBOL program RP0090. */
@Service
@Scope("prototype")
public class Rp0090Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Rp0090Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Rp0090FieldAccess ws;

    public Rp0090Service(
            Rp0090Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Rp0090FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::initializeAndOpenFiles);
        runChain(this::printOrderBacklogReport);
        runChain(this::closeFilesAndFinish);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeAndOpenFiles() {
        ws.setWkProgid("RP0090");
        ws.setWkTitle("SALES ORDER BACKLOG");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysdate(ws.getKdDate1());
        ws.setWkCompany("SAKURA Sales Management System");
        fileSet.getSyscf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setSyKey(1);
            readByKeyWithFallback(fileSet.getSyscf(), "SY-KEY");
            if (!fileSet.getSyscf().isInvalidKey()) {
                ws.setWkCompany(ws.getSyCompanyName());
            }
            fileSet.getSyscf().close();
            ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        }
        fileSet.getOrdhf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")
                && !Utility.fieldEquals(ws.getFsts(), "35")
                && !Utility.fieldEquals(ws.getFsts(), "30")) {
            ws.setKaFile("ORDHF");
            runChain(this::abortOnFileOpenError);
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setWkMainEof(1);
        }
        fileSet.getOrddf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        fileSet.getCustf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getRepf().open(FileOpenMode.OUTPUT);
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("REPF");
            runChain(this::abortOnFileOpenError);
        }
    }

    /** COBOL paragraph: PRINT-010 */
    private void printOrderBacklogReport() {
        ws.setWkGOrd(0);
        ws.setWkGLin(0);
        ws.setWkLine(99);
        if ((ws.getWkMainEof() != 1)) {
            ws.setOhNo(0);
            fileSet.getOrdhf().start("OH-NO", "NOT LESS THAN");
            ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
            if (fileSet.getOrdhf().isInvalidKey()) {
                ws.setWkMainEof(1);
            }
        }
        while ((ws.getWkMainEof() != 1)) {
            runChain(this::readNextOrderHeader);
        }
        runChain(this::printReportFooter);
    }

    /** COBOL paragraph: RH-010 */
    private void readNextOrderHeader() {
        fileSet.getOrdhf().readNext();
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        if (fileSet.getOrdhf().isAtEnd()) {
            ws.setWkMainEof(1);
        }
        if (!fileSet.getOrdhf().isAtEnd()) {
            runChain(this::processOrderHeader);
        }
    }

    /** COBOL paragraph: PRO-010 */
    private void processOrderHeader() {
        if (ws.getOhDelFlag() == 1 || ws.getOhStatus() >= 3) {
            return;
        }
        ws.setWkHdrPrinted(0);
        ws.setWkOrdQty(BigDecimal.ZERO);
        ws.setWkOrdAmt(BigDecimal.ZERO);
        runChain(this::processOrderDetails);
        if (ws.getWkHdrPrinted() == 1) {
            runChain(this::printOrderSubtotal);
            ws.setWkGQty(ws.getWkGQty().add(ws.getWkOrdQty()));
            ws.setWkGAmt(ws.getWkGAmt().add(ws.getWkOrdAmt()));
            ws.setWkGOrd(ws.getWkGOrd() + 1);
        }
    }

    /** COBOL paragraph: SD-010 */
    private void processOrderDetails() {
        ws.setWkDtlEof(0);
        ws.setOdNo(ws.getOhNo());
        ws.setOdLine(0);
        fileSet.getOrddf().start("OD-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        if (fileSet.getOrddf().isInvalidKey()) {
            ws.setWkDtlEof(1);
        }
        while ((ws.getWkDtlEof() != 1)) {
            runChain(this::readNextOrderDetail);
        }
    }

    /** COBOL paragraph: RD-010 */
    private void readNextOrderDetail() {
        fileSet.getOrddf().readNext();
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        if (fileSet.getOrddf().isAtEnd()) {
            ws.setWkDtlEof(1);
        }
        if (!fileSet.getOrddf().isAtEnd()) {
            if (ws.getOdNo() != ws.getOhNo()) {
                ws.setWkDtlEof(1);
            } else {
                runChain(this::processOrderDetailLine);
            }
        }
    }

    /** COBOL paragraph: CD-010 */
    private void processOrderDetailLine() {
        ws.setWkOut(ws.getOdQty().subtract(ws.getOdShippedQty()).longValue());
        if (ws.getWkOut() <= 0) {
            return;
        }
        if (ws.getWkHdrPrinted() == 0) {
            runChain(this::printOrderHeaderBlock);
            ws.setWkHdrPrinted(1);
        }
        runChain(this::printOrderDetailLine);
    }

    /** COBOL paragraph: PHD-010 */
    private void printOrderHeaderBlock() {
        runChain(this::checkPageBreakForHeader);
        ws.setWkCustName(" ");
        ws.setCuCode(ws.getOhCust());
        readByKeyWithFallback(fileSet.getCustf(), "CU-CODE");
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName("(unknown)");
        }
        if (!fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName(ws.getCuName());
        }
        ws.setOhNoE(ws.getOhNo());
        ws.setOhDateE(ws.getOhDate());
        ws.setOhCustE(ws.getOhCust());
        ws.setOhCname(ws.getWkCustName());
        ws.setOhDueE(ws.getOhDueDate());
        ws.setOhStE(ws.getOhStatus());
        ws.copyRepRecFromRdOrdh();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
        ws.copyRepRecFromRdDhead();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
    }

    /** COBOL paragraph: PDT-010 */
    private void printOrderDetailLine() {
        runChain(this::checkPageBreak);
        ws.setWkProdName(" ");
        ws.setPrCode(ws.getOdProd());
        readByKeyWithFallback(fileSet.getProdf(), "PR-CODE");
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName("(unknown)");
        }
        if (!fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName(ws.getPrName());
        }
        ws.setWkOutAmt(
                (BigDecimal.valueOf(ws.getWkOut()).multiply(ws.getOdUnitPrice()))
                        .setScale(0, java.math.RoundingMode.DOWN));
        ws.setOdLineE(ws.getOdLine());
        ws.setOdProdE(ws.getOdProd());
        ws.setOdPname(ws.getWkProdName());
        ws.setOdOrdE(ws.getOdQty().intValue());
        ws.setOdShpE(ws.getOdShippedQty().intValue());
        ws.setOdOutE(Utility.toCobolInt(ws.getWkOut(), 1));
        ws.setOdPriceE(ws.getOdUnitPrice());
        ws.setOdAmtE(ws.getWkOutAmt().longValue());
        ws.copyRepRecFromRdOrdd();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setWkOrdQty(ws.getWkOrdQty().add(BigDecimal.valueOf(ws.getWkOut())));
        ws.setWkOrdAmt(ws.getWkOrdAmt().add(ws.getWkOutAmt()));
        ws.setWkGLin(ws.getWkGLin() + 1);
    }

    /** COBOL paragraph: PSB-010 */
    private void printOrderSubtotal() {
        runChain(this::checkPageBreak);
        ws.setSubQty(ws.getWkOrdQty().intValue());
        ws.setSubAmt(ws.getWkOrdAmt().longValue());
        ws.copyRepRecFromRdSub();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setRepRec(" ");
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
    }

    /** COBOL paragraph: CPH-010 */
    private void checkPageBreakForHeader() {
        if (ws.getWkLine() >= 50) {
            runChain(this::printPageHeader);
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
        ws.setH2Info("OPEN ORDERS (STATUS < 3)");
        ws.copyRepRecFromRptH2();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(4);
    }

    /** COBOL paragraph: PG-010 */
    private void printReportFooter() {
        if (ws.getWkGOrd() == 0) {
            runChain(this::checkPageBreakForHeader);
            ws.setRepRec("*** NO OPEN ORDERS ***");
            fileSet.getRepf().write();
            ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
            return;
        }
        runChain(this::checkPageBreak);
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setGtQty(ws.getWkGQty().intValue());
        ws.setGtAmt(ws.getWkGAmt().longValue());
        ws.copyRepRecFromRdGrand();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
    }

    /** COBOL paragraph: TERM-010 */
    private void closeFilesAndFinish() {
        fileSet.getOrdhf().close();
        ws.trySetString("FSTS", fileSet.getOrdhf().getFileStatus());
        fileSet.getOrddf().close();
        ws.trySetString("FSTS", fileSet.getOrddf().getFileStatus());
        fileSet.getCustf().close();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getRepf().close();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        log.info(
                "RP0090 complete.  Open orders = {}  Lines = {}",
                String.format("%07d", (long) (ws.getWkGOrd())),
                String.format("%07d", (long) (ws.getWkGLin())));
        ws.setCompletionCode(0);
    }

    /** COBOL paragraph: AB-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("RP0090");
        ws.setKaFsts(ws.getFsts());
        ws.setKaMsgcode("EOPEN ");
        ws.setKaDetail("Report file error");
        abortx(ws.getKabend());
        ws.setCompletionCode(255);
        throw new ProgramExitSignal();
    }

    /**
     * Read a record by key: prefer the WorkingStorage key field, falling back to the key extracted
     * from the current record buffer if that field is blank.
     */
    private void readByKeyWithFallback(RawDatasetBase file, String keyFieldName) {
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString(keyFieldName);
            } catch (Exception _e) {
            }
        }
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = file.extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        file.readByKey(rkVal != null ? rkVal.trim() : "");
        ws.trySetString("FSTS", file.getFileStatus());
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

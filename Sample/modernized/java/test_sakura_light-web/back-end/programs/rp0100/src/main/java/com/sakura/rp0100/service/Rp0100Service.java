package com.sakura.rp0100.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0100.domain.Rp0100FieldAccess;
import com.sakura.rp0100.domain.WorkingStorage;
import com.sakura.rp0100.runtime.Rp0100Datasets;
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

/** Business logic service generated from COBOL program RP0100. */
@Service
@Scope("prototype")
public class Rp0100Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Rp0100Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Rp0100FieldAccess ws;

    public Rp0100Service(
            Rp0100Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Rp0100FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::readDateRangeParameters);
        runChain(this::processAllPurchaseHeaders);
        runChain(this::finalizeAndCloseFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeAndOpenFiles() {
        ws.setWkProgid("RP0100");
        ws.setWkTitle("PURCHASE JOURNAL");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysdate(ws.getKdDate1());
        ws.setWkCompany("SAKURA Sales Management System");
        fileSet.getSyscf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setSyKey(1);
            readByPrimaryKey(fileSet.getSyscf(), "SY-KEY");
            if (fileSet.getSyscf().isInvalidKey()) {
                /* CONTINUE */
            }
            if (!fileSet.getSyscf().isInvalidKey()) {
                ws.setWkCompany(ws.getSyCompanyName());
            }
            fileSet.getSyscf().close();
            ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        }
        fileSet.getPurhf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getPurhf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")
                && !Utility.fieldEquals(ws.getFsts(), "35")
                && !Utility.fieldEquals(ws.getFsts(), "30")) {
            ws.setKaFile("PURHF");
            runChain(this::abortOnFileOpenError);
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setWkMainEof(1);
        }
        fileSet.getPurdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getPurdf().getFileStatus());
        fileSet.getSuppf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getRepf().open(FileOpenMode.OUTPUT);
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("REPF");
            runChain(this::abortOnFileOpenError);
        }
    }

    /** COBOL paragraph: PARM-010 */
    private void readDateRangeParameters() {
        log.info("RP0100 - Purchase journal");
        log.info("From purchase date YYYYMMDD (blank=earliest): ");
        String stdinValWkInFrom1 = Utility.readStdinLine();
        if (stdinValWkInFrom1 == null || stdinValWkInFrom1.trim().isEmpty()) {
            stdinValWkInFrom1 = "";
        }
        ws.setWkInFrom(stdinValWkInFrom1);
        log.info("To   purchase date YYYYMMDD (blank=latest  ): ");
        String stdinValWkInTo2 = Utility.readStdinLine();
        if (stdinValWkInTo2 == null || stdinValWkInTo2.trim().isEmpty()) {
            stdinValWkInTo2 = "";
        }
        ws.setWkInTo(stdinValWkInTo2);
        if (Utility.isNumeric(String.valueOf(ws.getWkInFrom()))
                && !Utility.fieldEquals(ws.getWkInFrom(), " ")) {
            ws.setWkDateFrom(Utility.parseNumeric(ws.getWkInFrom()).intValue());
        } else {
            ws.setWkDateFrom(0);
        }
        if (Utility.isNumeric(String.valueOf(ws.getWkInTo()))
                && !Utility.fieldEquals(ws.getWkInTo(), " ")) {
            ws.setWkDateTo(Utility.parseNumeric(ws.getWkInTo()).intValue());
        } else {
            ws.setWkDateTo(99999999);
        }
    }

    /** COBOL paragraph: PRINT-010 */
    private void processAllPurchaseHeaders() {
        ws.setWkPurCnt(0);
        ws.setWkLinCnt(0);
        ws.setWkLine(99);
        if ((ws.getWkMainEof() != 1)) {
            ws.setVhNo(0);
            fileSet.getPurhf().start("VH-NO", "NOT LESS THAN");
            ws.trySetString("FSTS", fileSet.getPurhf().getFileStatus());
            if (fileSet.getPurhf().isInvalidKey()) {
                ws.setWkMainEof(1);
            }
        }
        while ((ws.getWkMainEof() != 1)) {
            runChain(this::readNextPurchaseHeader);
        }
        runChain(this::printReportSummary);
    }

    /** COBOL paragraph: RH-010 */
    private void readNextPurchaseHeader() {
        fileSet.getPurhf().readNext();
        ws.trySetString("FSTS", fileSet.getPurhf().getFileStatus());
        if (fileSet.getPurhf().isAtEnd()) {
            ws.setWkMainEof(1);
        }
        if (!fileSet.getPurhf().isAtEnd()) {
            runChain(this::processPurchaseHeaderRecord);
        }
    }

    /** COBOL paragraph: PP-010 */
    private void processPurchaseHeaderRecord() {
        if (ws.getVhDelFlag() == 1 || ws.getVhStatus() == 9) {
            return;
        }
        if (ws.getVhDate() < ws.getWkDateFrom() || ws.getVhDate() > ws.getWkDateTo()) {
            return;
        }
        runChain(this::checkPageBreakForHeader);
        runChain(this::lookupSupplierName);
        switch (ws.getVhKind()) {
            case 2 -> {
                ws.setWkKindTxt("RETURN");
            }
            default -> {
                ws.setWkKindTxt("PURCH");
            }
        }
        ws.setVhNoE(ws.getVhNo());
        ws.setVhDateE(ws.getVhDate());
        ws.setVhSuppE(ws.getVhSupp());
        ws.setVhSname(ws.getWkSuppName());
        ws.setVhKindE(ws.getWkKindTxt());
        ws.copyRepRecFromRdPurh();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setWkPurCnt(ws.getWkPurCnt() + 1);
        runChain(this::processPurchaseDetails);
        runChain(this::printPurchaseSubtotal);
        ws.setWkGNet(ws.getWkGNet().add(ws.getVhAmount()));
        ws.setWkGTax(ws.getWkGTax().add(ws.getVhTaxAmount()));
        ws.setWkGTot(ws.getWkGTot().add(ws.getVhTotal()));
    }

    /** COBOL paragraph: PD-010 */
    private void processPurchaseDetails() {
        ws.setWkDtlEof(0);
        ws.setVdNo(ws.getVhNo());
        ws.setVdLine(0);
        fileSet.getPurdf().start("VD-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getPurdf().getFileStatus());
        if (fileSet.getPurdf().isInvalidKey()) {
            ws.setWkDtlEof(1);
        }
        while ((ws.getWkDtlEof() != 1)) {
            runChain(this::readNextPurchaseDetail);
        }
    }

    /** COBOL paragraph: RDT-010 */
    private void readNextPurchaseDetail() {
        fileSet.getPurdf().readNext();
        ws.trySetString("FSTS", fileSet.getPurdf().getFileStatus());
        if (fileSet.getPurdf().isAtEnd()) {
            ws.setWkDtlEof(1);
        }
        if (!fileSet.getPurdf().isAtEnd()) {
            if (ws.getVdNo() != ws.getVhNo()) {
                ws.setWkDtlEof(1);
            } else {
                runChain(this::printPurchaseDetailLine);
            }
        }
    }

    /** COBOL paragraph: POD-010 */
    private void printPurchaseDetailLine() {
        runChain(this::checkPageBreakForDetail);
        runChain(this::lookupProductName);
        ws.setVdLineE(ws.getVdLine());
        ws.setVdProdE(ws.getVdProd());
        ws.setVdPname(ws.getWkProdName());
        ws.setVdQtyE(ws.getVdQty().longValue());
        ws.setVdCostE(ws.getVdUnitCost());
        ws.setVdAmtE(ws.getVdAmount().longValue());
        ws.copyRepRecFromRdPurd();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setWkLinCnt(ws.getWkLinCnt() + 1);
    }

    /** COBOL paragraph: PS-010 */
    private void printPurchaseSubtotal() {
        runChain(this::checkPageBreakForDetail);
        ws.setSubNet(ws.getVhAmount().longValue());
        ws.setSubTax(ws.getVhTaxAmount().longValue());
        ws.setSubTot(ws.getVhTotal().longValue());
        ws.copyRepRecFromRdSub();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setRepRec(" ");
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
    }

    /** COBOL paragraph: LKS-010 */
    private void lookupSupplierName() {
        ws.setWkSuppName(" ");
        ws.setSpCode(ws.getVhSupp());
        readByPrimaryKey(fileSet.getSuppf(), "SP-CODE");
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setWkSuppName("(unknown)");
        }
        if (!fileSet.getSuppf().isInvalidKey()) {
            ws.setWkSuppName(ws.getSpName());
        }
    }

    /** COBOL paragraph: LKP-010 */
    private void lookupProductName() {
        ws.setWkProdName(" ");
        ws.setPrCode(ws.getVdProd());
        readByPrimaryKey(fileSet.getProdf(), "PR-CODE");
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName("(unknown)");
        }
        if (!fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName(ws.getPrName());
        }
    }

    /** COBOL paragraph: CPH-010 */
    private void checkPageBreakForHeader() {
        if (ws.getWkLine() >= 50) {
            runChain(this::printReportHeader);
        }
    }

    /** COBOL paragraph: CP-010 */
    private void checkPageBreakForDetail() {
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
        {
            StringBuilder sb = new StringBuilder();
            sb.append("PERIOD ");
            sb.append(String.format("%08d", (long) (ws.getWkDateFrom())));
            sb.append(" - ");
            sb.append(String.format("%08d", (long) (ws.getWkDateTo())));
            ws.setH2Info(sb.toString());
        }
        ws.copyRepRecFromRptH2();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(5);
    }

    /** COBOL paragraph: PG-010 */
    private void printReportSummary() {
        if (ws.getWkPurCnt() == 0) {
            runChain(this::checkPageBreakForHeader);
            ws.setRepRec("*** NO PURCHASES IN THE SELECTED PERIOD ***");
            fileSet.getRepf().write();
            ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
            return;
        }
        runChain(this::checkPageBreakForDetail);
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setGtNet(ws.getWkGNet().longValue());
        ws.setGtTax(ws.getWkGTax().longValue());
        ws.setGtTot(ws.getWkGTot().longValue());
        ws.copyRepRecFromRdGrand();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
    }

    /** COBOL paragraph: TERM-010 */
    private void finalizeAndCloseFiles() {
        fileSet.getPurhf().close();
        ws.trySetString("FSTS", fileSet.getPurhf().getFileStatus());
        fileSet.getPurdf().close();
        ws.trySetString("FSTS", fileSet.getPurdf().getFileStatus());
        fileSet.getSuppf().close();
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getRepf().close();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        log.info(
                "RP0100 complete.  Purchases = {}  Lines = {}",
                String.format("%07d", (long) (ws.getWkPurCnt())),
                String.format("%07d", (long) (ws.getWkLinCnt())));
        ws.setCompletionCode(0);
    }

    /** COBOL paragraph: AB-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("RP0100");
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

    /**
     * Read a master record by its primary key, falling back to the current record's key when the
     * key field is not yet populated.
     */
    private void readByPrimaryKey(RawDatasetBase file, String keyField) {
        String keyVal = "";
        if (keyVal == null || keyVal.trim().isEmpty()) {
            try {
                keyVal = ws.getString(keyField);
            } catch (Exception _e) {
            }
        }
        if (keyVal == null || keyVal.trim().isEmpty()) {
            try {
                keyVal = file.extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        file.readByKey(keyVal != null ? keyVal.trim() : "");
        ws.trySetString("FSTS", file.getFileStatus());
    }
}

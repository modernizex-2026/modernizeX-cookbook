package com.sakura.rp0030.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0030.domain.Rp0030FieldAccess;
import com.sakura.rp0030.domain.WorkingStorage;
import com.sakura.rp0030.runtime.Rp0030Datasets;
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

/** Business logic service generated from COBOL program RP0030. */
@Service
@Scope("prototype")
public class Rp0030Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Rp0030Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Rp0030FieldAccess ws;

    public Rp0030Service(
            Rp0030Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Rp0030FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::initializeProgramAndFiles);
        runChain(this::readDateRangeParameters);
        runChain(this::generateSalesJournal);
        runChain(this::closeFilesAndLogSummary);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgramAndFiles() {
        ws.setWkProgid("RP0030");
        ws.setWkTitle("SALES JOURNAL");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysdate(ws.getKdDate1());
        ws.setWkCompany("SAKURA Sales Management System");
        openChecked(fileSet.getSyscf(), FileOpenMode.INPUT);
        if (Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setSyKey(1);
            readByKeyWithFallback(fileSet.getSyscf(), "SY-KEY");
            if (fileSet.getSyscf().isInvalidKey()) {
                /* CONTINUE */
            }
            if (!fileSet.getSyscf().isInvalidKey()) {
                ws.setWkCompany(ws.getSyCompanyName());
            }
            closeChecked(fileSet.getSyscf());
        }
        openChecked(fileSet.getInvhf(), FileOpenMode.INPUT);
        if (!Utility.fieldEquals(ws.getFsts(), "00")
                && !Utility.fieldEquals(ws.getFsts(), "35")
                && !Utility.fieldEquals(ws.getFsts(), "30")) {
            ws.setKaFile("INVHF");
            runChain(this::abortProgram);
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setWkMainEof(1);
        }
        openChecked(fileSet.getInvdf(), FileOpenMode.INPUT);
        openChecked(fileSet.getCustf(), FileOpenMode.INPUT);
        openChecked(fileSet.getProdf(), FileOpenMode.INPUT);
        openChecked(fileSet.getRepf(), FileOpenMode.OUTPUT);
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("REPF");
            runChain(this::abortProgram);
        }
    }

    /** COBOL paragraph: PARM-010 */
    private void readDateRangeParameters() {
        log.info("RP0030 - Sales journal");
        log.info("From sales date YYYYMMDD (blank=earliest): ");
        String stdinValWkInFrom1 = Utility.readStdinLine();
        if (stdinValWkInFrom1 == null || stdinValWkInFrom1.trim().isEmpty()) {
            stdinValWkInFrom1 = "";
        }
        ws.setWkInFrom(stdinValWkInFrom1);
        log.info("To   sales date YYYYMMDD (blank=latest  ): ");
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
    private void generateSalesJournal() {
        ws.setWkInvCnt(0);
        ws.setWkLinCnt(0);
        ws.setWkLine(99);
        if ((ws.getWkMainEof() != 1)) {
            ws.setIhDate(ws.getWkDateFrom());
            ws.setIhNo(0);
            fileSet.getInvhf().start("IH-DATE", "NOT LESS THAN");
            ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
            if (fileSet.getInvhf().isInvalidKey()) {
                ws.setWkMainEof(1);
            }
        }
        while ((ws.getWkMainEof() != 1)) {
            runChain(this::readNextInvoiceHeader);
        }
        runChain(this::writeReportSummary);
    }

    /** COBOL paragraph: RH-010 */
    private void readNextInvoiceHeader() {
        fileSet.getInvhf().readNext();
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        if (fileSet.getInvhf().isAtEnd()) {
            ws.setWkMainEof(1);
        }
        if (!fileSet.getInvhf().isAtEnd()) {
            if (ws.getIhDate() > ws.getWkDateTo()) {
                ws.setWkMainEof(1);
            } else {
                runChain(this::processInvoiceHeader);
            }
        }
    }

    /** COBOL paragraph: PI-010 */
    private void processInvoiceHeader() {
        if (ws.getIhDelFlag() == 1 || ws.getIhStatus() == 9) {
            return;
        }
        runChain(this::checkPageBreakForHeader);
        runChain(this::lookupCustomerName);
        switch (ws.getIhKind()) {
            case 2 -> {
                ws.setWkKindTxt("RETURN");
            }
            default -> {
                ws.setWkKindTxt("SALE");
            }
        }
        ws.setIhNoE(ws.getIhNo());
        ws.setIhDateE(ws.getIhDate());
        ws.setIhCustE(ws.getIhCust());
        ws.setIhCname(ws.getWkCustName());
        ws.setIhKindE(ws.getWkKindTxt());
        ws.copyRepRecFromRdInvh();
        writeRepf();
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setWkInvCnt(ws.getWkInvCnt() + 1);
        runChain(this::processInvoiceDetails);
        runChain(this::writeInvoiceSubtotal);
        ws.setWkGNet(ws.getWkGNet().add(ws.getIhAmount()));
        ws.setWkGTax(ws.getWkGTax().add(ws.getIhTaxAmount()));
        ws.setWkGTot(ws.getWkGTot().add(ws.getIhTotal()));
    }

    /** COBOL paragraph: PD-010 */
    private void processInvoiceDetails() {
        ws.setWkDtlEof(0);
        ws.setIdNo(ws.getIhNo());
        ws.setIdLine(0);
        fileSet.getInvdf().start("ID-NO", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getInvdf().getFileStatus());
        if (fileSet.getInvdf().isInvalidKey()) {
            ws.setWkDtlEof(1);
        }
        while ((ws.getWkDtlEof() != 1)) {
            runChain(this::readNextInvoiceDetail);
        }
    }

    /** COBOL paragraph: RD-010 */
    private void readNextInvoiceDetail() {
        fileSet.getInvdf().readNext();
        ws.trySetString("FSTS", fileSet.getInvdf().getFileStatus());
        if (fileSet.getInvdf().isAtEnd()) {
            ws.setWkDtlEof(1);
        }
        if (!fileSet.getInvdf().isAtEnd()) {
            if (ws.getIdNo() != ws.getIhNo()) {
                ws.setWkDtlEof(1);
            } else {
                runChain(this::writeInvoiceDetailLine);
            }
        }
    }

    /** COBOL paragraph: POD-010 */
    private void writeInvoiceDetailLine() {
        runChain(this::checkPageBreakForDetail);
        runChain(this::lookupProductName);
        ws.setIdLineE(ws.getIdLine());
        ws.setIdProdE(ws.getIdProd());
        ws.setIdPname(ws.getWkProdName());
        ws.setIdQtyE(ws.getIdQty().longValue());
        ws.setIdPriceE(ws.getIdUnitPrice());
        ws.setIdAmtE(ws.getIdAmount().longValue());
        ws.copyRepRecFromRdInvd();
        writeRepf();
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setWkLinCnt(ws.getWkLinCnt() + 1);
    }

    /** COBOL paragraph: PS-010 */
    private void writeInvoiceSubtotal() {
        runChain(this::checkPageBreakForDetail);
        ws.setSubNet(ws.getIhAmount().longValue());
        ws.setSubTax(ws.getIhTaxAmount().longValue());
        ws.setSubTot(ws.getIhTotal().longValue());
        ws.copyRepRecFromRdSub();
        writeRepf();
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setRepRec(" ");
        writeRepf();
        ws.setWkLine(ws.getWkLine() + 1);
    }

    /** COBOL paragraph: LKC-010 */
    private void lookupCustomerName() {
        ws.setWkCustName(" ");
        ws.setCuCode(ws.getIhCust());
        readByKeyWithFallback(fileSet.getCustf(), "CU-CODE");
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName("(unknown)");
        }
        if (!fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName(ws.getCuName());
        }
    }

    /** COBOL paragraph: LKP-010 */
    private void lookupProductName() {
        ws.setWkProdName(" ");
        ws.setPrCode(ws.getIdProd());
        readByKeyWithFallback(fileSet.getProdf(), "PR-CODE");
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
            runChain(this::writeReportHeader);
        }
    }

    /** COBOL paragraph: CP-010 */
    private void checkPageBreakForDetail() {
        if (ws.getWkLine() >= 55) {
            runChain(this::writeReportHeader);
        }
    }

    /** COBOL paragraph: PH-010 */
    private void writeReportHeader() {
        ws.setWkPage(ws.getWkPage() + 1);
        ws.setH1Company(ws.getWkCompany());
        ws.setH1Title(ws.getWkTitle());
        ws.setH1Page(ws.getWkPage());
        ws.copyRepRecFromRptH1();
        writeRepf();
        ws.setH2Date(ws.getWkSysdate());
        ws.setH2Info(" ");
        ws.setH2Info(
                "PERIOD "
                        + String.format("%08d", (long) ws.getWkDateFrom())
                        + " - "
                        + String.format("%08d", (long) ws.getWkDateTo()));
        ws.copyRepRecFromRptH2();
        writeRepf();
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        writeRepf();
        ws.setWkLine(5);
    }

    /** COBOL paragraph: PG-010 */
    private void writeReportSummary() {
        if (ws.getWkInvCnt() == 0) {
            runChain(this::checkPageBreakForHeader);
            ws.setRepRec("*** NO INVOICES IN THE SELECTED PERIOD ***");
            writeRepf();
            return;
        }
        runChain(this::checkPageBreakForDetail);
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        writeRepf();
        ws.setGtNet(ws.getWkGNet().longValue());
        ws.setGtTax(ws.getWkGTax().longValue());
        ws.setGtTot(ws.getWkGTot().longValue());
        ws.copyRepRecFromRdGrand();
        writeRepf();
    }

    /** COBOL paragraph: TERM-010 */
    private void closeFilesAndLogSummary() {
        closeChecked(fileSet.getInvhf());
        closeChecked(fileSet.getInvdf());
        closeChecked(fileSet.getCustf());
        closeChecked(fileSet.getProdf());
        closeChecked(fileSet.getRepf());
        log.info(
                "RP0030 complete.  Invoices = {}  Lines = {}",
                String.format("%07d", (long) (ws.getWkInvCnt())),
                String.format("%07d", (long) (ws.getWkLinCnt())));
        ws.setCompletionCode(0);
    }

    /** COBOL paragraph: AB-010 */
    private void abortProgram() {
        ws.setKaProgid("RP0030");
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

    /** Open a dataset for the given mode and shadow its file status into WorkingStorage FSTS. */
    private void openChecked(RawDatasetBase file, FileOpenMode mode) {
        file.open(mode);
        ws.trySetString("FSTS", file.getFileStatus());
    }

    /** Close a dataset and shadow its file status into WorkingStorage FSTS. */
    private void closeChecked(RawDatasetBase file) {
        file.close();
        ws.trySetString("FSTS", file.getFileStatus());
    }

    /** Write the current REPF output record and shadow its file status into WorkingStorage FSTS. */
    private void writeRepf() {
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
    }

    /**
     * Read a record by key, falling back to the key extracted from the current record when the
     * WorkingStorage field lookup is blank.
     */
    private void readByKeyWithFallback(RawDatasetBase file, String keyField) {
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString(keyField);
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
}

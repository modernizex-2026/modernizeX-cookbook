package com.sakura.rp0050.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0050.domain.Rp0050FieldAccess;
import com.sakura.rp0050.domain.WorkingStorage;
import com.sakura.rp0050.runtime.Rp0050Datasets;
import com.sakura.runtime.BatchServiceBase;
import com.sakura.runtime.DatasetEnums.*;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.AbstractDatasets;
import com.sakura.runtime.linkage.AbortxLinkParm;
import com.sakura.runtime.linkage.DateutLinkParm;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Business logic service generated from COBOL program RP0050. */
@Service
@Scope("prototype")
public class Rp0050Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Rp0050Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Rp0050FieldAccess ws;

    public Rp0050Service(
            Rp0050Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Rp0050FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::processInvoiceHeaders);
        runChain(this::sortProductSalesByCode);
        runChain(this::printSalesReport);
        runChain(this::closeFilesAndFinish);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgramAndFiles() {
        ws.setWkProgid("RP0050");
        ws.setWkTitle("SALES SUMMARY BY PRODUCT");
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
        fileSet.getInvhf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")
                && !Utility.fieldEquals(ws.getFsts(), "35")
                && !Utility.fieldEquals(ws.getFsts(), "30")) {
            ws.setKaFile("INVHF");
            runChain(this::abortOnFileError);
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setWkMainEof(1);
        }
        fileSet.getInvdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getInvdf().getFileStatus());
        fileSet.getProdf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getRepf().open(FileOpenMode.OUTPUT);
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("REPF");
            runChain(this::abortOnFileError);
        }
    }

    /** COBOL paragraph: PARM-010 */
    private void readDateRangeParameters() {
        log.info("RP0050 - Sales summary by product");
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

    /** COBOL paragraph: AC-010 */
    private void processInvoiceHeaders() {
        ws.setWkPn(0);
        if ((ws.getWkMainEof() != 1)) {
            ws.setIhNo(0);
            fileSet.getInvhf().start("IH-NO", "NOT LESS THAN");
            ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
            if (fileSet.getInvhf().isInvalidKey()) {
                ws.setWkMainEof(1);
            }
        }
        while ((ws.getWkMainEof() != 1)) {
            runChain(this::readNextInvoiceHeader);
        }
    }

    /** COBOL paragraph: RH-010 */
    private void readNextInvoiceHeader() {
        fileSet.getInvhf().readNext();
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        if (fileSet.getInvhf().isAtEnd()) {
            ws.setWkMainEof(1);
        }
        if (!fileSet.getInvhf().isAtEnd()) {
            runChain(this::processEligibleHeader);
        }
    }

    /** COBOL paragraph: CH-010 */
    private void processEligibleHeader() {
        if (ws.getIhDelFlag() == 1 || ws.getIhStatus() == 9) {
            return;
        }
        if (ws.getIhDate() < ws.getWkDateFrom() || ws.getIhDate() > ws.getWkDateTo()) {
            return;
        }
        runChain(this::processInvoiceDetails);
    }

    /** COBOL paragraph: SD-010 */
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
                runChain(this::accumulateProductSales);
            }
        }
    }

    /** COBOL paragraph: AD-010 */
    private void accumulateProductSales() {
        ws.setWkFound(0);
        for (ws.setWkSi(1);
                !(ws.getWkSi() > ws.getWkPn() || (ws.getWkFound() == 1));
                ws.setWkSi(ws.getWkSi() + 1)) {
            if (ws.getWkPCode(ws.getWkSi()) == ws.getIdProd()) {
                ws.setString("WK-FOUND", "1");
                ws.setWkPQty(ws.getWkSi(), ws.getWkPQty(ws.getWkSi()).add(ws.getIdQty()));
                ws.setWkPAmt(ws.getWkSi(), ws.getWkPAmt(ws.getWkSi()).add(ws.getIdAmount()));
            }
        }
        if ((ws.getWkFound() != 1)) {
            if (ws.getWkPn() < 2000) {
                ws.setWkPn(ws.getWkPn() + 1);
                ws.setWkPCode(ws.getWkPn(), ws.getIdProd());
                ws.setWkPQty(ws.getWkPn(), ws.getIdQty());
                ws.setWkPAmt(ws.getWkPn(), ws.getIdAmount());
            }
        }
    }

    /** COBOL paragraph: ST-010 */
    private void sortProductSalesByCode() {
        if (ws.getWkPn() < 2) {
            return;
        }
        for (ws.setWkSi(1); ws.getWkSi() < ws.getWkPn(); ws.setWkSi(ws.getWkSi() + 1)) {
            ws.setWkSmin(ws.getWkSi());
            ws.setWkSj((ws.getWkSi() + 1));
            while (ws.getWkSj() <= ws.getWkPn()) {
                if (ws.getWkPCode(ws.getWkSj()) < ws.getWkPCode(ws.getWkSmin())) {
                    ws.setWkSmin(ws.getWkSj());
                }
                ws.setWkSj(ws.getWkSj() + 1);
            }
            if (ws.getWkSmin() != ws.getWkSi()) {
                ws.copyBytes("WK-TMP-ENT", "WK-PENT", new int[0], new int[] {ws.getWkSi()});
                ws.copyBytes(
                        "WK-PENT", "WK-PENT", new int[] {ws.getWkSi()}, new int[] {ws.getWkSmin()});
                ws.copyBytes("WK-PENT", "WK-TMP-ENT", new int[] {ws.getWkSmin()}, new int[0]);
            }
        }
    }

    /** COBOL paragraph: PR-010 */
    private void printSalesReport() {
        ws.setWkLine(99);
        if (ws.getWkPn() == 0) {
            runChain(this::printPageHeader);
            ws.setRepRec("*** NO SALES IN THE SELECTED PERIOD ***");
            fileSet.getRepf().write();
            ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
            return;
        }
        for (ws.setWkSi(1); ws.getWkSi() <= ws.getWkPn(); ws.setWkSi(ws.getWkSi() + 1)) {
            runChain(this::printProductLine);
        }
        runChain(this::printGrandTotal);
    }

    /** COBOL paragraph: P1-010 */
    private void printProductLine() {
        runChain(this::checkPageBreak);
        ws.setWkProdName(" ");
        ws.setPrCode(ws.getWkPCode(ws.getWkSi()));
        String rkVal_3 = "";
        if (rkVal_3 == null || rkVal_3.trim().isEmpty()) {
            try {
                rkVal_3 = ws.getString("PR-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_3 == null || rkVal_3.trim().isEmpty()) {
            try {
                rkVal_3 = fileSet.getProdf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getProdf().readByKey(rkVal_3 != null ? rkVal_3.trim() : "");
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        if (fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName("(unknown)");
        }
        if (!fileSet.getProdf().isInvalidKey()) {
            ws.setWkProdName(ws.getPrName());
        }
        ws.setRpCode(ws.getWkPCode(ws.getWkSi()));
        ws.setRpName(ws.getWkProdName());
        ws.setRpQty(ws.getWkPQty(ws.getWkSi()).longValue());
        ws.setRpAmt(ws.getWkPAmt(ws.getWkSi()).longValue());
        ws.copyRepRecFromRdLine();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setWkGQty(ws.getWkGQty().add(ws.getWkPQty(ws.getWkSi())));
        ws.setWkGAmt(ws.getWkGAmt().add(ws.getWkPAmt(ws.getWkSi())));
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
        {
            StringBuilder sb = new StringBuilder();
            sb.append("PERIOD ");
            sb.append(String.format("%08d", (long) ws.getWkDateFrom()));
            sb.append(" - ");
            sb.append(String.format("%08d", (long) ws.getWkDateTo()));
            ws.setH2Info(sb.toString());
        }
        ws.copyRepRecFromRptH2();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setRepRec(" ");
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.copyRepRecFromRhLine();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setRepRec(ws.getRptRule());
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(6);
    }

    /** COBOL paragraph: PG-010 */
    private void printGrandTotal() {
        runChain(this::checkPageBreak);
        ws.setRepRec(ws.getRptRule());
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setRtQty(ws.getWkGQty().longValue());
        ws.setRtAmt(ws.getWkGAmt().longValue());
        ws.copyRepRecFromRtLine();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
    }

    /** COBOL paragraph: TERM-010 */
    private void closeFilesAndFinish() {
        fileSet.getInvhf().close();
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        fileSet.getInvdf().close();
        ws.trySetString("FSTS", fileSet.getInvdf().getFileStatus());
        fileSet.getProdf().close();
        ws.trySetString("FSTS", fileSet.getProdf().getFileStatus());
        fileSet.getRepf().close();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        log.info("RP0050 complete.  Products = {}", String.format("%05d", (long) (ws.getWkPn())));
        ws.setCompletionCode(0);
    }

    /** COBOL paragraph: AB-010 */
    private void abortOnFileError() {
        ws.setKaProgid("RP0050");
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

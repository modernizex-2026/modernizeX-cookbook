package com.sakura.rp0040.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0040.domain.Rp0040FieldAccess;
import com.sakura.rp0040.domain.WorkingStorage;
import com.sakura.rp0040.runtime.Rp0040Datasets;
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

/** Business logic service generated from COBOL program RP0040. */
@Service
@Scope("prototype")
public class Rp0040Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Rp0040Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Rp0040FieldAccess ws;

    public Rp0040Service(
            Rp0040Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Rp0040FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::readDateRangeParameters);
        runChain(this::printSalesSummaryReport);
        runChain(this::finalizeAndCloseFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("RP0040");
        ws.setWkTitle("SALES SUMMARY BY CUSTOMER");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysdate(ws.getKdDate1());
        ws.setWkCompany("SAKURA Sales Management System");
        fileSet.getSyscf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
        if (isFstsOk()) {
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
            closeFileChecked(fileSet.getSyscf());
        }
        fileSet.getInvhf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        if (!isFstsOk()
                && !Utility.fieldEquals(ws.getFsts(), "35")
                && !Utility.fieldEquals(ws.getFsts(), "30")) {
            ws.setKaFile("INVHF");
            runChain(this::abortProgram);
        }
        if (!isFstsOk()) {
            ws.setWkMainEof(1);
        }
        fileSet.getCustf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        fileSet.getRepf().open(FileOpenMode.OUTPUT);
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        if (!isFstsOk()) {
            ws.setKaFile("REPF");
            runChain(this::abortProgram);
        }
    }

    /** COBOL paragraph: PARM-010 */
    private void readDateRangeParameters() {
        log.info("RP0040 - Sales summary by customer");
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
    private void printSalesSummaryReport() {
        ws.setWkGCnt(0);
        ws.setWkLine(99);
        if ((ws.getWkMainEof() != 1)) {
            ws.setIhCust(0);
            ws.setIhDate(0);
            fileSet.getInvhf().start("IH-CUST", "NOT LESS THAN");
            ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
            if (fileSet.getInvhf().isInvalidKey()) {
                ws.setWkMainEof(1);
            }
        }
        while ((ws.getWkMainEof() != 1)) {
            runChain(this::readNextInvoiceRecord);
        }
        if (ws.getWkFirst() == 0) {
            runChain(this::writeCustomerSummaryLine);
        }
        runChain(this::printGrandTotal);
    }

    /** COBOL paragraph: RN-010 */
    private void readNextInvoiceRecord() {
        fileSet.getInvhf().readNext();
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        if (fileSet.getInvhf().isAtEnd()) {
            ws.setWkMainEof(1);
        }
        if (!fileSet.getInvhf().isAtEnd()) {
            runChain(this::accumulateCustomerSales);
        }
    }

    /** COBOL paragraph: PO-010 */
    private void accumulateCustomerSales() {
        if (ws.getWkFirst() == 1) {
            ws.setWkCurCust(ws.getIhCust());
            runChain(this::resetCustomerAccumulators);
            ws.setWkFirst(0);
        } else {
            if (ws.getIhCust() != ws.getWkCurCust()) {
                runChain(this::writeCustomerSummaryLine);
                ws.setWkCurCust(ws.getIhCust());
                runChain(this::resetCustomerAccumulators);
            }
        }
        if (ws.getIhDelFlag() != 1
                && ws.getIhStatus() != 9
                && ws.getIhDate() >= ws.getWkDateFrom()
                && ws.getIhDate() <= ws.getWkDateTo()) {
            ws.setWkCurNet(ws.getWkCurNet().add(ws.getIhAmount()));
            ws.setWkCurTax(ws.getWkCurTax().add(ws.getIhTaxAmount()));
            ws.setWkCurTot(ws.getWkCurTot().add(ws.getIhTotal()));
            ws.setWkCurCnt(ws.getWkCurCnt() + 1);
        }
    }

    /** COBOL paragraph: RA-010 */
    private void resetCustomerAccumulators() {
        ws.setWkCurNet(BigDecimal.ZERO);
        ws.setWkCurTax(BigDecimal.ZERO);
        ws.setWkCurTot(BigDecimal.ZERO);
        ws.setWkCurCnt(0);
    }

    /** COBOL paragraph: FC-010 */
    private void writeCustomerSummaryLine() {
        if (ws.getWkCurCnt() == 0) {
            return;
        }
        runChain(this::checkPageBreak);
        ws.setWkCustName(" ");
        ws.setCuCode(ws.getWkCurCust());
        String rkVal_3 = "";
        if (rkVal_3 == null || rkVal_3.trim().isEmpty()) {
            try {
                rkVal_3 = ws.getString("CU-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_3 == null || rkVal_3.trim().isEmpty()) {
            try {
                rkVal_3 = fileSet.getCustf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getCustf().readByKey(rkVal_3 != null ? rkVal_3.trim() : "");
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName("(unknown)");
        }
        if (!fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName(ws.getCuName());
        }
        ws.setRcCode(ws.getWkCurCust());
        ws.setRcName(ws.getWkCustName());
        ws.setRcCount(ws.getWkCurCnt());
        ws.setRcNet(ws.getWkCurNet().longValue());
        ws.setRcTax(ws.getWkCurTax().longValue());
        ws.setRcTot(ws.getWkCurTot().longValue());
        ws.copyRepRecFromRdLine();
        writeRepRecord();
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setWkGNet(ws.getWkGNet().add(ws.getWkCurNet()));
        ws.setWkGTax(ws.getWkGTax().add(ws.getWkCurTax()));
        ws.setWkGTot(ws.getWkGTot().add(ws.getWkCurTot()));
        ws.setWkGCnt(ws.getWkGCnt() + ws.getWkCurCnt());
        ws.setWkCustCnt(ws.getWkCustCnt() + 1);
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
        writeRepRecord();
        ws.setH2Date(ws.getWkSysdate());
        ws.setH2Info(" ");
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("PERIOD "));
            sb.append(String.valueOf(String.format("%08d", (long) (ws.getWkDateFrom()))));
            sb.append(String.valueOf(" - "));
            sb.append(String.valueOf(String.format("%08d", (long) (ws.getWkDateTo()))));
            ws.setH2Info(sb.toString());
        }
        ws.copyRepRecFromRptH2();
        writeRepRecord();
        ws.setRepRec(" ");
        writeRepRecord();
        ws.copyRepRecFromRhLine();
        writeRepRecord();
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        writeRepRecord();
        ws.setWkLine(6);
    }

    /** COBOL paragraph: PG-010 */
    private void printGrandTotal() {
        if (ws.getWkGCnt() == 0) {
            runChain(this::checkPageBreak);
            ws.setRepRec("*** NO SALES IN THE SELECTED PERIOD ***");
            writeRepRecord();
            return;
        }
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        writeRepRecord();
        ws.setRtCount(ws.getWkGCnt());
        ws.setRtNet(ws.getWkGNet().longValue());
        ws.setRtTax(ws.getWkGTax().longValue());
        ws.setRtTot(ws.getWkGTot().longValue());
        ws.copyRepRecFromRtLine();
        writeRepRecord();
    }

    /** COBOL paragraph: TERM-010 */
    private void finalizeAndCloseFiles() {
        closeFileChecked(fileSet.getInvhf());
        closeFileChecked(fileSet.getCustf());
        closeFileChecked(fileSet.getRepf());
        log.info(
                "RP0040 complete.  Customers = {}  Invoices = {}",
                String.format("%07d", (long) (ws.getWkCustCnt())),
                String.format("%07d", (long) (ws.getWkGCnt())));
        ws.setCompletionCode(0);
    }

    /** COBOL paragraph: AB-010 */
    private void abortProgram() {
        ws.setKaProgid("RP0040");
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

    /** Check whether the last file operation completed with status "00". */
    private boolean isFstsOk() {
        return Utility.fieldEquals(ws.getFsts(), "00");
    }

    /** Write the current REPF record and capture its resulting file status. */
    private void writeRepRecord() {
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
    }

    /** Close a dataset and capture its resulting file status. */
    private void closeFileChecked(RawDatasetBase file) {
        file.close();
        ws.trySetString("FSTS", file.getFileStatus());
    }
}

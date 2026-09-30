package com.sakura.rp0010.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0010.domain.Rp0010FieldAccess;
import com.sakura.rp0010.domain.WorkingStorage;
import com.sakura.rp0010.runtime.Rp0010Datasets;
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

/** Business logic service generated from COBOL program RP0010. */
@Service
@Scope("prototype")
public class Rp0010Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Rp0010Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Rp0010FieldAccess ws;

    public Rp0010Service(
            Rp0010Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Rp0010FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::readCustomerRangeParams);
        runChain(this::printCustomerList);
        runChain(this::terminateProgram);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("RP0010");
        ws.setWkTitle("CUSTOMER MASTER LIST");
        ws.setKdFunc("TODY");
        dateut(ws.getKdate());
        ws.setWkSysdate(ws.getKdDate1());
        ws.setWkCompany("SAKURA Sales Management System");
        openFileChecked(fileSet.getSyscf(), FileOpenMode.INPUT);
        if (Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setSyKey(1);
            fileSet.getSyscf().readByKey(resolveRecordKey("SY-KEY", fileSet.getSyscf()));
            ws.trySetString("FSTS", fileSet.getSyscf().getFileStatus());
            if (fileSet.getSyscf().isInvalidKey()) {
                /* CONTINUE */
            }
            if (!fileSet.getSyscf().isInvalidKey()) {
                ws.setWkCompany(ws.getSyCompanyName());
            }
            closeFileChecked(fileSet.getSyscf());
        }
        openFileChecked(fileSet.getCustf(), FileOpenMode.INPUT);
        if (!Utility.fieldEquals(ws.getFsts(), "00")
                && !Utility.fieldEquals(ws.getFsts(), "35")
                && !Utility.fieldEquals(ws.getFsts(), "30")) {
            ws.setKaFile("CUSTF");
            runChain(this::abortOnFileOpenError);
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setWkMainEof(1);
        }
        openFileChecked(fileSet.getRegnf(), FileOpenMode.INPUT);
        openFileChecked(fileSet.getStaff(), FileOpenMode.INPUT);
        openFileChecked(fileSet.getRepf(), FileOpenMode.OUTPUT);
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("REPF");
            runChain(this::abortOnFileOpenError);
        }
    }

    /** COBOL paragraph: PARM-010 */
    private void readCustomerRangeParams() {
        log.info("RP0010 - Customer master list");
        log.info("From customer code (blank=first): ");
        String stdinValWkInFrom1 = Utility.readStdinLine();
        if (stdinValWkInFrom1 == null || stdinValWkInFrom1.trim().isEmpty()) {
            stdinValWkInFrom1 = "";
        }
        ws.setWkInFrom(stdinValWkInFrom1);
        log.info("To   customer code (blank=last ): ");
        String stdinValWkInTo2 = Utility.readStdinLine();
        if (stdinValWkInTo2 == null || stdinValWkInTo2.trim().isEmpty()) {
            stdinValWkInTo2 = "";
        }
        ws.setWkInTo(stdinValWkInTo2);
        if (Utility.isNumeric(ws.getWkInFrom()) && !Utility.fieldEquals(ws.getWkInFrom(), " ")) {
            ws.setWkCustFrom(Utility.parseNumeric(ws.getWkInFrom()).intValue());
        } else {
            ws.setWkCustFrom(0);
        }
        if (Utility.isNumeric(ws.getWkInTo()) && !Utility.fieldEquals(ws.getWkInTo(), " ")) {
            ws.setWkCustTo(Utility.parseNumeric(ws.getWkInTo()).intValue());
        } else {
            ws.setWkCustTo(999999);
        }
    }

    /** COBOL paragraph: PRINT-010 */
    private void printCustomerList() {
        ws.setWkCnt(0);
        ws.setWkLine(99);
        if ((ws.getWkMainEof() != 1)) {
            ws.setCuCode(ws.getWkCustFrom());
            fileSet.getCustf().start("CU-CODE", "NOT LESS THAN");
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
            if (fileSet.getCustf().isInvalidKey()) {
                ws.setWkMainEof(1);
            }
        }
        while ((ws.getWkMainEof() != 1)) {
            runChain(this::readNextCustomerRecord);
        }
        runChain(this::printReportTotals);
    }

    /** COBOL paragraph: RN-010 */
    private void readNextCustomerRecord() {
        fileSet.getCustf().readNext();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isAtEnd()) {
            ws.setWkMainEof(1);
        }
        if (!fileSet.getCustf().isAtEnd()) {
            if (ws.getCuCode() > ws.getWkCustTo()) {
                ws.setWkMainEof(1);
            } else {
                runChain(this::writeCustomerDetailLine);
            }
        }
    }

    /** COBOL paragraph: PO-010 */
    private void writeCustomerDetailLine() {
        if (ws.getCuDelFlag() == 1) {
            return;
        }
        runChain(this::checkPageBreak);
        runChain(this::lookupRegionName);
        runChain(this::lookupStaffName);
        ws.setRcCode(ws.getCuCode());
        ws.setRcName(ws.getCuName());
        ws.setRcRegion(ws.getWkRegionName());
        ws.setRcStaff(ws.getWkStaffName());
        ws.setRcCredit(ws.getCuCreditLimit().longValue());
        ws.setRcBalance(ws.getCuBalance().longValue());
        ws.copyRepRecFromRdCust();
        writeReportRecord();
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setWkCnt(ws.getWkCnt() + 1);
        ws.setWkTotCredit(ws.getWkTotCredit().add(ws.getCuCreditLimit()));
        ws.setWkTotBal(ws.getWkTotBal().add(ws.getCuBalance()));
    }

    /** COBOL paragraph: LR-010 */
    private void lookupRegionName() {
        ws.setWkRegionName(" ");
        if (ws.getCuRegion() != 0) {
            ws.setRgCode(ws.getCuRegion());
            fileSet.getRegnf().readByKey(resolveRecordKey("RG-CODE", fileSet.getRegnf()));
            ws.trySetString("FSTS", fileSet.getRegnf().getFileStatus());
            if (fileSet.getRegnf().isInvalidKey()) {
                ws.setWkRegionName("(unknown)");
            }
            if (!fileSet.getRegnf().isInvalidKey()) {
                ws.setWkRegionName(ws.getRgName());
            }
        }
    }

    /** COBOL paragraph: LS-010 */
    private void lookupStaffName() {
        ws.setWkStaffName(" ");
        if (ws.getCuStaff() != 0) {
            ws.setSfCode(ws.getCuStaff());
            fileSet.getStaff().readByKey(resolveRecordKey("SF-CODE", fileSet.getStaff()));
            ws.trySetString("FSTS", fileSet.getStaff().getFileStatus());
            if (fileSet.getStaff().isInvalidKey()) {
                ws.setWkStaffName("(unknown)");
            }
            if (!fileSet.getStaff().isInvalidKey()) {
                ws.setWkStaffName(ws.getSfName());
            }
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
        writeReportRecord();
        ws.setH2Date(ws.getWkSysdate());
        ws.setH2Info(" ");
        ws.copyRepRecFromRptH2();
        writeReportRecord();
        ws.setRepRec(" ");
        writeReportRecord();
        ws.copyRepRecFromRhCust();
        writeReportRecord();
        ws.setRepRec(ws.getRptRule());
        writeReportRecord();
        ws.setWkLine(6);
    }

    /** COBOL paragraph: PT-010 */
    private void printReportTotals() {
        if (ws.getWkCnt() == 0) {
            runChain(this::checkPageBreak);
            ws.setRepRec("*** NO CUSTOMERS SELECTED ***");
            writeReportRecord();
            return;
        }
        ws.setRepRec(ws.getRptRule());
        writeReportRecord();
        ws.setRtCredit(ws.getWkTotCredit().longValue());
        ws.setRtBalance(ws.getWkTotBal().longValue());
        ws.copyRepRecFromRtCust();
        writeReportRecord();
    }

    /** COBOL paragraph: TERM-010 */
    private void terminateProgram() {
        closeFileChecked(fileSet.getCustf());
        closeFileChecked(fileSet.getRegnf());
        closeFileChecked(fileSet.getStaff());
        closeFileChecked(fileSet.getRepf());
        log.info(
                "RP0010 complete.  Customers listed = {}",
                String.format("%07d", (long) (ws.getWkCnt())));
        ws.setCompletionCode(0);
    }

    /** COBOL paragraph: AB-010 */
    private void abortOnFileOpenError() {
        ws.setKaProgid("RP0010");
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

    /** Open a file and record the resulting file status. */
    private void openFileChecked(RawDatasetBase file, FileOpenMode mode) {
        file.open(mode);
        ws.trySetString("FSTS", file.getFileStatus());
    }

    /** Close a file and record the resulting file status. */
    private void closeFileChecked(RawDatasetBase file) {
        file.close();
        ws.trySetString("FSTS", file.getFileStatus());
    }

    /** Write the current report record and record the resulting file status. */
    private void writeReportRecord() {
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
    }

    /**
     * Resolve the read-by-key value for a keyed file, falling back to the current record's key when
     * the primary field is blank.
     */
    private String resolveRecordKey(String keyFieldName, RawDatasetBase file) {
        String rkVal = "";
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = ws.getString(keyFieldName);
            } catch (Exception e) {
                /* ignore */
            }
        }
        if (rkVal == null || rkVal.trim().isEmpty()) {
            try {
                rkVal = file.extractKeyFromCurrentRecord();
            } catch (Exception e) {
                /* ignore */
            }
        }
        return rkVal != null ? rkVal.trim() : "";
    }
}

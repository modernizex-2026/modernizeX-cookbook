package com.sakura.rp0120.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0120.domain.Rp0120FieldAccess;
import com.sakura.rp0120.domain.WorkingStorage;
import com.sakura.rp0120.runtime.Rp0120Datasets;
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

/** Business logic service generated from COBOL program RP0120. */
@Service
@Scope("prototype")
public class Rp0120Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Rp0120Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Rp0120FieldAccess ws;

    public Rp0120Service(
            Rp0120Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Rp0120FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::gatherInvoiceRecords);
        runChain(this::sortStaffSummaryByCode);
        runChain(this::printStaffReport);
        runChain(this::terminateAndCloseFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgramAndFiles() {
        ws.setWkProgid("RP0120");
        ws.setWkTitle("SALES REP PERFORMANCE");
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
            runChain(this::abortWithFileError);
        }
        if (!isFstsOk()) {
            ws.setWkMainEof(1);
        }
        fileSet.getStaff().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getStaff().getFileStatus());
        fileSet.getRepf().open(FileOpenMode.OUTPUT);
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        if (!isFstsOk()) {
            ws.setKaFile("REPF");
            runChain(this::abortWithFileError);
        }
    }

    /** COBOL paragraph: PARM-010 */
    private void readDateRangeParameters() {
        log.info("RP0120 - Sales rep performance report");
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

    /** COBOL paragraph: GAT-010 */
    private void gatherInvoiceRecords() {
        ws.setWkInvCnt(0);
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
            runChain(this::readNextInvoiceRecord);
        }
    }

    /** COBOL paragraph: RI-010 */
    private void readNextInvoiceRecord() {
        fileSet.getInvhf().readNext();
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        if (fileSet.getInvhf().isAtEnd()) {
            ws.setWkMainEof(1);
        }
        if (!fileSet.getInvhf().isAtEnd()) {
            if (ws.getIhDate() > ws.getWkDateTo()) {
                ws.setWkMainEof(1);
            } else {
                runChain(this::accumulateInvoice);
            }
        }
    }

    /** COBOL paragraph: AI-010 */
    private void accumulateInvoice() {
        if (ws.getIhDelFlag() == 1 || ws.getIhStatus() == 9) {
            return;
        }
        ws.setWkInvCnt(ws.getWkInvCnt() + 1);
        runChain(this::findOrCreateStaffSummary);
    }

    /** COBOL paragraph: FS-010 */
    private void findOrCreateStaffSummary() {
        ws.setWkFoundIdx(0);
        ws.setWkSi(1);
        while (!(ws.getWkSi() > ws.getWkStNum() || ws.getWkFoundIdx() > 0)) {
            if (ws.getWkStCode(ws.getWkSi()) == ws.getIhStaff()) {
                ws.setWkFoundIdx(ws.getWkSi());
            } else {
                ws.setWkSi(ws.getWkSi() + 1);
            }
        }
        if (ws.getWkFoundIdx() == 0) {
            if (ws.getWkStNum() < ws.getWkStMax()) {
                ws.setWkStNum(ws.getWkStNum() + 1);
                ws.setWkFoundIdx(ws.getWkStNum());
                ws.setWkStCode(ws.getWkFoundIdx(), ws.getIhStaff());
                ws.setWkStCnt(ws.getWkFoundIdx(), 0);
                ws.setWkStNet(ws.getWkFoundIdx(), BigDecimal.ZERO);
                ws.setWkStTax(ws.getWkFoundIdx(), BigDecimal.ZERO);
                ws.setWkStTot(ws.getWkFoundIdx(), BigDecimal.ZERO);
                ws.setWkStCost(ws.getWkFoundIdx(), BigDecimal.ZERO);
            } else {
                ws.setWkOverflow(ws.getWkOverflow() + 1);
                return;
            }
        }
        ws.setWkStCnt(ws.getWkFoundIdx(), ws.getWkStCnt(ws.getWkFoundIdx()) + 1);
        ws.setWkStNet(ws.getWkFoundIdx(), ws.getWkStNet(ws.getWkFoundIdx()).add(ws.getIhAmount()));
        ws.setWkStTax(
                ws.getWkFoundIdx(), ws.getWkStTax(ws.getWkFoundIdx()).add(ws.getIhTaxAmount()));
        ws.setWkStTot(ws.getWkFoundIdx(), ws.getWkStTot(ws.getWkFoundIdx()).add(ws.getIhTotal()));
        ws.setWkStCost(
                ws.getWkFoundIdx(), ws.getWkStCost(ws.getWkFoundIdx()).add(ws.getIhCostTotal()));
    }

    /** COBOL paragraph: SR-010 */
    private void sortStaffSummaryByCode() {
        if (ws.getWkStNum() < 2) {
            return;
        }
        for (ws.setWkSi(1); ws.getWkSi() < ws.getWkStNum(); ws.setWkSi(ws.getWkSi() + 1)) {
            ws.setWkSmin(ws.getWkSi());
            for (ws.setWkSj(ws.getWkSi());
                    ws.getWkSj() <= ws.getWkStNum();
                    ws.setWkSj(ws.getWkSj() + 1)) {
                if (ws.getWkStCode(ws.getWkSj()) < ws.getWkStCode(ws.getWkSmin())) {
                    ws.setWkSmin(ws.getWkSj());
                }
            }
            if (ws.getWkSmin() != ws.getWkSi()) {
                ws.copyBytes("WK-ST-TEMP", "WK-ST-ENT", new int[0], new int[] {ws.getWkSi()});
                ws.copyBytes(
                        "WK-ST-ENT",
                        "WK-ST-ENT",
                        new int[] {ws.getWkSi()},
                        new int[] {ws.getWkSmin()});
                ws.copyBytes("WK-ST-ENT", "WK-ST-TEMP", new int[] {ws.getWkSmin()}, new int[0]);
            }
        }
    }

    /** COBOL paragraph: PRINT-010 */
    private void printStaffReport() {
        ws.setWkLine(99);
        ws.setWkRepCnt(0);
        for (ws.setWkSi(1); ws.getWkSi() <= ws.getWkStNum(); ws.setWkSi(ws.getWkSi() + 1)) {
            runChain(this::printStaffLine);
        }
        runChain(this::printGrandTotal);
    }

    /** COBOL paragraph: PO-010 */
    private void printStaffLine() {
        runChain(this::checkPageBreak);
        runChain(this::lookupStaffName);
        ws.setWkMargin(
                (ws.getWkStTot(ws.getWkSi()).subtract(ws.getWkStCost(ws.getWkSi())))
                        .setScale(0, java.math.RoundingMode.DOWN));
        ws.setRlCode(ws.getWkStCode(ws.getWkSi()));
        ws.setRlName(ws.getWkStaffName());
        ws.setRlCnt(ws.getWkStCnt(ws.getWkSi()));
        ws.setRlNet(ws.getWkStNet(ws.getWkSi()).longValue());
        ws.setRlTax(ws.getWkStTax(ws.getWkSi()).longValue());
        ws.setRlTot(ws.getWkStTot(ws.getWkSi()).longValue());
        ws.setRlMargin(ws.getWkMargin().longValue());
        ws.copyRepRecFromRdLine();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setWkRepCnt(ws.getWkRepCnt() + 1);
        ws.setWkGCnt(ws.getWkGCnt().add(BigDecimal.valueOf(ws.getWkStCnt(ws.getWkSi()))));
        ws.setWkGNet(ws.getWkGNet().add(ws.getWkStNet(ws.getWkSi())));
        ws.setWkGTax(ws.getWkGTax().add(ws.getWkStTax(ws.getWkSi())));
        ws.setWkGTot(ws.getWkGTot().add(ws.getWkStTot(ws.getWkSi())));
        ws.setWkGMargin(ws.getWkGMargin().add(ws.getWkMargin()));
    }

    /** COBOL paragraph: LKS-010 */
    private void lookupStaffName() {
        ws.setWkStaffName(" ");
        if (ws.getWkStCode(ws.getWkSi()) == 0) {
            ws.setWkStaffName("(no rep assigned)");
            return;
        }
        ws.setSfCode(ws.getWkStCode(ws.getWkSi()));
        String rkVal_3 = "";
        if (rkVal_3 == null || rkVal_3.trim().isEmpty()) {
            try {
                rkVal_3 = ws.getString("SF-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_3 == null || rkVal_3.trim().isEmpty()) {
            try {
                rkVal_3 = fileSet.getStaff().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getStaff().readByKey(rkVal_3 != null ? rkVal_3.trim() : "");
        ws.trySetString("FSTS", fileSet.getStaff().getFileStatus());
        if (fileSet.getStaff().isInvalidKey()) {
            ws.setWkStaffName("(unknown staff)");
        }
        if (!fileSet.getStaff().isInvalidKey()) {
            ws.setWkStaffName(ws.getSfName());
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
        ws.setRepRec(" ");
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.copyRepRecFromRcHead();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setRepRec(ws.getRptRule());
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(6);
    }

    /** COBOL paragraph: PG-010 */
    private void printGrandTotal() {
        if (ws.getWkRepCnt() == 0) {
            runChain(this::checkPageBreak);
            ws.setRepRec("*** NO INVOICES IN THE SELECTED PERIOD ***");
            fileSet.getRepf().write();
            ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
            return;
        }
        runChain(this::checkPageBreak);
        ws.setRepRec(ws.getRptRule());
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setRtCnt(ws.getWkGCnt().intValue());
        ws.setRtNet(ws.getWkGNet().longValue());
        ws.setRtTax(ws.getWkGTax().longValue());
        ws.setRtTot(ws.getWkGTot().longValue());
        ws.setRtMargin(ws.getWkGMargin().longValue());
        ws.copyRepRecFromRtLine();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
    }

    /** COBOL paragraph: TERM-010 */
    private void terminateAndCloseFiles() {
        fileSet.getInvhf().close();
        ws.trySetString("FSTS", fileSet.getInvhf().getFileStatus());
        fileSet.getStaff().close();
        ws.trySetString("FSTS", fileSet.getStaff().getFileStatus());
        fileSet.getRepf().close();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        log.info(
                "RP0120 complete.  Invoices read = {}",
                String.format("%07d", (long) (ws.getWkInvCnt())));
        log.info(
                "         Sales reps reported     = {}",
                String.format("%07d", (long) (ws.getWkRepCnt())));
        if (ws.getWkOverflow() > 0) {
            log.info(
                    " ** table overflow, invoices skipped = {}",
                    String.format("%07d", (long) (ws.getWkOverflow())));
        }
        ws.setCompletionCode(0);
    }

    /** COBOL paragraph: AB-010 */
    private void abortWithFileError() {
        ws.setKaProgid("RP0120");
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

    /** True when the last file operation's status ("FSTS") reported success ("00"). */
    private boolean isFstsOk() {
        return Utility.fieldEquals(ws.getFsts(), "00");
    }
}

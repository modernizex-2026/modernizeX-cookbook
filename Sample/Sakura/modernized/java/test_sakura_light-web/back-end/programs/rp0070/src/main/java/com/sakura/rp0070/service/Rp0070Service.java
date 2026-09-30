package com.sakura.rp0070.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0070.domain.Rp0070FieldAccess;
import com.sakura.rp0070.domain.WorkingStorage;
import com.sakura.rp0070.runtime.Rp0070Datasets;
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

/** Business logic service generated from COBOL program RP0070. */
@Service
@Scope("prototype")
public class Rp0070Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Rp0070Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Rp0070FieldAccess ws;

    public Rp0070Service(
            Rp0070Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Rp0070FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::initializeProgramAndOpenFiles);
        runChain(this::generateAgingReport);
        runChain(this::terminateProgram);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgramAndOpenFiles() {
        ws.setWkProgid("RP0070");
        ws.setWkTitle("ACCOUNTS RECEIVABLE AGING");
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
        fileSet.getArlf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")
                && !Utility.fieldEquals(ws.getFsts(), "35")
                && !Utility.fieldEquals(ws.getFsts(), "30")) {
            ws.setKaFile("ARLF");
            runChain(this::abortOnFileError);
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setWkMainEof(1);
        }
        fileSet.getCustf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        fileSet.getRepf().open(FileOpenMode.OUTPUT);
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("REPF");
            runChain(this::abortOnFileError);
        }
    }

    /** COBOL paragraph: PRINT-010 */
    private void generateAgingReport() {
        ws.setWkGCnt(0);
        ws.setWkLine(99);
        if ((ws.getWkMainEof() != 1)) {
            ws.setAlCust(0);
            ws.setAlDate(0);
            fileSet.getArlf().start("AL-CUST", "NOT LESS THAN");
            ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
            if (fileSet.getArlf().isInvalidKey()) {
                ws.setWkMainEof(1);
            }
        }
        while ((ws.getWkMainEof() != 1)) {
            runChain(this::readNextArRecord);
        }
        if (ws.getWkFirst() == 0) {
            runChain(this::writeCustomerAgingSummary);
        }
        runChain(this::printGrandTotal);
    }

    /** COBOL paragraph: RN-010 */
    private void readNextArRecord() {
        fileSet.getArlf().readNext();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        if (fileSet.getArlf().isAtEnd()) {
            ws.setWkMainEof(1);
        }
        if (!fileSet.getArlf().isAtEnd()) {
            runChain(this::handleCustomerBreakAndAge);
        }
    }

    /** COBOL paragraph: PO-010 */
    private void handleCustomerBreakAndAge() {
        if (ws.getWkFirst() == 1) {
            ws.setWkCurCust(ws.getAlCust());
            runChain(this::resetCustomerAccumulators);
            ws.setWkFirst(0);
        } else {
            if (ws.getAlCust() != ws.getWkCurCust()) {
                runChain(this::writeCustomerAgingSummary);
                ws.setWkCurCust(ws.getAlCust());
                runChain(this::resetCustomerAccumulators);
            }
        }
        runChain(this::computeAgingBucket);
    }

    /** COBOL paragraph: AG-010 */
    private void computeAgingBucket() {
        ws.setKdFunc("DIFF");
        ws.setKdDate1(ws.getAlDate());
        ws.setKdDate2(ws.getWkSysdate());
        dateut(ws.getKdate());
        ws.setWkAge(ws.getKdDays());
        ws.setWkNet(
                (ws.getAlDebit().subtract(ws.getAlCredit()))
                        .setScale(0, java.math.RoundingMode.DOWN));
        if (ws.getWkAge() <= 30) {
            ws.setWkCurB0(ws.getWkCurB0().add(ws.getWkNet()));
        } else if (ws.getWkAge() <= 60) {
            ws.setWkCurB1(ws.getWkCurB1().add(ws.getWkNet()));
        } else if (ws.getWkAge() <= 90) {
            ws.setWkCurB2(ws.getWkCurB2().add(ws.getWkNet()));
        } else {
            ws.setWkCurB3(ws.getWkCurB3().add(ws.getWkNet()));
        }
        ws.setWkCurCnt(ws.getWkCurCnt() + 1);
    }

    /** COBOL paragraph: RA-010 */
    private void resetCustomerAccumulators() {
        ws.setWkCurB0(BigDecimal.ZERO);
        ws.setWkCurB1(BigDecimal.ZERO);
        ws.setWkCurB2(BigDecimal.ZERO);
        ws.setWkCurB3(BigDecimal.ZERO);
        ws.setWkCurCnt(0);
    }

    /** COBOL paragraph: FC-010 */
    private void writeCustomerAgingSummary() {
        if (ws.getWkCurCnt() == 0) {
            return;
        }
        ws.setWkCurBal(
                (ws.getWkCurB0().add(ws.getWkCurB1()).add(ws.getWkCurB2()).add(ws.getWkCurB3()))
                        .setScale(0, java.math.RoundingMode.DOWN));
        if (ws.getWkCurBal().signum() == 0) {
            return;
        }
        runChain(this::checkPageBreak);
        ws.setWkCustName(" ");
        ws.setCuCode(ws.getWkCurCust());
        String rkVal_1 = "";
        if (rkVal_1 == null || rkVal_1.trim().isEmpty()) {
            try {
                rkVal_1 = ws.getString("CU-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_1 == null || rkVal_1.trim().isEmpty()) {
            try {
                rkVal_1 = fileSet.getCustf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getCustf().readByKey(rkVal_1 != null ? rkVal_1.trim() : "");
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName("(unknown)");
        }
        if (!fileSet.getCustf().isInvalidKey()) {
            ws.setWkCustName(ws.getCuName());
        }
        ws.setRaCode(ws.getWkCurCust());
        ws.setRaName(ws.getWkCustName());
        ws.setRaB0(ws.getWkCurB0().longValue());
        ws.setRaB1(ws.getWkCurB1().longValue());
        ws.setRaB2(ws.getWkCurB2().longValue());
        ws.setRaB3(ws.getWkCurB3().longValue());
        ws.setRaTot(ws.getWkCurBal().longValue());
        ws.copyRepRecFromRdLine();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setWkLine(ws.getWkLine() + 1);
        ws.setWkGB0(ws.getWkGB0().add(ws.getWkCurB0()));
        ws.setWkGB1(ws.getWkGB1().add(ws.getWkCurB1()));
        ws.setWkGB2(ws.getWkGB2().add(ws.getWkCurB2()));
        ws.setWkGB3(ws.getWkGB3().add(ws.getWkCurB3()));
        ws.setWkGBal(ws.getWkGBal().add(ws.getWkCurBal()));
        ws.setWkGCnt(ws.getWkGCnt() + 1);
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
            ws.setRepRec("*** NO OUTSTANDING RECEIVABLES ***");
            fileSet.getRepf().write();
            ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
            return;
        }
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        ws.setRtB0(ws.getWkGB0().longValue());
        ws.setRtB1(ws.getWkGB1().longValue());
        ws.setRtB2(ws.getWkGB2().longValue());
        ws.setRtB3(ws.getWkGB3().longValue());
        ws.setRtTot(ws.getWkGBal().longValue());
        ws.copyRepRecFromRtLine();
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
    }

    /** COBOL paragraph: TERM-010 */
    private void terminateProgram() {
        fileSet.getArlf().close();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        fileSet.getCustf().close();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        fileSet.getRepf().close();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        log.info(
                "RP0070 complete.  Customers aged = {}",
                String.format("%07d", (long) (ws.getWkGCnt())));
        ws.setCompletionCode(0);
    }

    /** COBOL paragraph: AB-010 */
    private void abortOnFileError() {
        ws.setKaProgid("RP0070");
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

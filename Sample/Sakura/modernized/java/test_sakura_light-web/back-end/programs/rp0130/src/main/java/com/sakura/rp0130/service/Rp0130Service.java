package com.sakura.rp0130.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.dateut.service.DateutService;
import com.sakura.rp0130.domain.Rp0130FieldAccess;
import com.sakura.rp0130.domain.WorkingStorage;
import com.sakura.rp0130.runtime.Rp0130Datasets;
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

/** Business logic service generated from COBOL program RP0130. */
@Service
@Scope("prototype")
public class Rp0130Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Rp0130Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Rp0130FieldAccess ws;

    public Rp0130Service(
            Rp0130Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Rp0130FieldAccess(new WorkingStorage(), fileSet);
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
        runChain(this::loadReportParameters);
        runChain(this::printCustomerStatements);
        runChain(this::finalizeAndCloseFiles);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("RP0130");
        ws.setWkTitle("CUSTOMER STATEMENT");
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
        fileSet.getCustf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")
                && !Utility.fieldEquals(ws.getFsts(), "35")
                && !Utility.fieldEquals(ws.getFsts(), "30")) {
            ws.setKaFile("CUSTF");
            runChain(this::abortWithFileError);
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setWkMainEof(1);
        }
        fileSet.getArlf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        fileSet.getRepf().open(FileOpenMode.OUTPUT);
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("REPF");
            runChain(this::abortWithFileError);
        }
    }

    /** COBOL paragraph: PARM-010 */
    private void loadReportParameters() {
        log.info("RP0130 - Customer statement");
        ws.setWkInCfrom(readStdinOrEmpty("From customer code (blank=first): "));
        ws.setWkInCto(readStdinOrEmpty("To   customer code (blank=last ): "));
        ws.setWkInDfrom(readStdinOrEmpty("Period start YYYYMMDD (blank=earliest): "));
        ws.setWkInDto(readStdinOrEmpty("Period end   YYYYMMDD (blank=latest  ): "));
        ws.setWkCustFrom(parseIntOrDefault(ws.getWkInCfrom(), 0));
        ws.setWkCustTo(parseIntOrDefault(ws.getWkInCto(), 999999));
        ws.setWkDateFrom(parseIntOrDefault(ws.getWkInDfrom(), 0));
        ws.setWkDateTo(parseIntOrDefault(ws.getWkInDto(), 99999999));
    }

    /** COBOL paragraph: PRINT-010 */
    private void printCustomerStatements() {
        ws.setWkLine(99);
        ws.setWkStmtCnt(0);
        if ((ws.getWkMainEof() != 1)) {
            ws.setCuCode(ws.getWkCustFrom());
            fileSet.getCustf().start("CU-CODE", "NOT LESS THAN");
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
            if (fileSet.getCustf().isInvalidKey()) {
                ws.setWkMainEof(1);
            }
        }
        while ((ws.getWkMainEof() != 1)) {
            runChain(this::readNextCustomer);
        }
        runChain(this::printNoStatementsMessage);
    }

    /** COBOL paragraph: RDC-010 */
    private void readNextCustomer() {
        fileSet.getCustf().readNext();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isAtEnd()) {
            ws.setWkMainEof(1);
        }
        if (!fileSet.getCustf().isAtEnd()) {
            if (ws.getCuCode() > ws.getWkCustTo()) {
                ws.setWkMainEof(1);
            } else {
                if (ws.getCuDelFlag() != 1) {
                    runChain(this::processCustomerStatement);
                }
            }
        }
    }

    /** COBOL paragraph: SC-010 */
    private void processCustomerStatement() {
        ws.setWkOpen(BigDecimal.ZERO);
        ws.setWkRun(BigDecimal.ZERO);
        ws.setWkHdrDone(0);
        ws.setWkLineCnt(0);
        ws.setWkArlEof(0);
        ws.setAlCust(ws.getCuCode());
        ws.setAlDate(0);
        fileSet.getArlf().start("AL-CUST", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        if (fileSet.getArlf().isInvalidKey()) {
            ws.setWkArlEof(1);
        }
        while ((ws.getWkArlEof() != 1)) {
            runChain(this::readNextLedgerEntry);
        }
        runChain(this::printClosingStatement);
    }

    /** COBOL paragraph: SCR-010 */
    private void readNextLedgerEntry() {
        fileSet.getArlf().readNext();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        if (fileSet.getArlf().isAtEnd()) {
            ws.setWkArlEof(1);
        }
        if (!fileSet.getArlf().isAtEnd()) {
            if (ws.getAlCust() != ws.getCuCode()) {
                ws.setWkArlEof(1);
            } else {
                runChain(this::processLedgerEntry);
            }
        }
    }

    /** COBOL paragraph: CL-010 */
    private void processLedgerEntry() {
        ws.setWkNet(
                (ws.getAlDebit().subtract(ws.getAlCredit()))
                        .setScale(0, java.math.RoundingMode.DOWN));
        if (ws.getAlDate() < ws.getWkDateFrom()) {
            ws.setWkOpen(ws.getWkOpen().add(ws.getWkNet()));
            return;
        }
        if (ws.getAlDate() > ws.getWkDateTo()) {
            ws.setWkArlEof(1);
            return;
        }
        if (ws.getWkHdrDone() == 0) {
            runChain(this::printOpeningStatement);
        }
        ws.setWkRun(ws.getWkRun().add(ws.getWkNet()));
        runChain(this::printLedgerLine);
        ws.setWkLineCnt(ws.getWkLineCnt() + 1);
    }

    /** COBOL paragraph: OS-010 */
    private void printOpeningStatement() {
        runChain(this::startNewPage);
        ws.setStCode(ws.getCuCode());
        ws.setStName(ws.getCuName());
        ws.copyRepRecFromRdStmt();
        writeReportLine();
        ws.setRepRec(" ");
        writeReportLine();
        ws.copyRepRecFromRcHead();
        writeReportLine();
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        writeReportLine();
        ws.setOpBal(ws.getWkOpen().longValue());
        ws.copyRepRecFromRdOpen();
        writeReportLine();
        ws.setWkRun(ws.getWkOpen());
        ws.setWkHdrDone(1);
    }

    /** COBOL paragraph: PLL-010 */
    private void printLedgerLine() {
        runChain(this::checkPageBreak);
        ws.setRlDate(ws.getAlDate());
        runChain(this::resolveKindText);
        ws.setRlKind(ws.getWkKindTxt());
        ws.setRlRef(ws.getAlRefNo());
        ws.setRlRemark(ws.getAlRemark());
        ws.setRlDebit(ws.getAlDebit().longValue());
        ws.setRlCredit(ws.getAlCredit().longValue());
        ws.setRlBal(ws.getWkRun().longValue());
        ws.copyRepRecFromRdLine();
        writeReportLine();
    }

    /** COBOL paragraph: KL-010 */
    private void resolveKindText() {
        switch (ws.getAlKind()) {
            case 1 -> {
                ws.setWkKindTxt("SALE");
            }
            case 2 -> {
                ws.setWkKindTxt("RECEIPT");
            }
            case 3 -> {
                ws.setWkKindTxt("RETURN");
            }
            case 4 -> {
                ws.setWkKindTxt("ADJUST");
            }
            default -> {
                ws.setWkKindTxt("OTHER");
            }
        }
    }

    /** COBOL paragraph: FC-010 */
    private void printClosingStatement() {
        if (ws.getWkHdrDone() == 1) {
            runChain(this::checkPageBreak);
            ws.setRepRec(String.valueOf(ws.getRptRule()));
            writeReportLine();
            ws.setClBal(ws.getWkRun().longValue());
            ws.copyRepRecFromRdClose();
            writeReportLine();
            ws.setWkStmtCnt(ws.getWkStmtCnt() + 1);
            return;
        }
        if (ws.getWkOpen().signum() != 0) {
            runChain(this::printOpeningStatement);
            ws.setRepRec(" (no ledger activity in this period)");
            writeReportLine();
            runChain(this::checkPageBreak);
            ws.setRepRec(String.valueOf(ws.getRptRule()));
            writeReportLine();
            ws.setClBal(ws.getWkOpen().longValue());
            ws.copyRepRecFromRdClose();
            writeReportLine();
            ws.setWkStmtCnt(ws.getWkStmtCnt() + 1);
        }
    }

    /** COBOL paragraph: CP-010 */
    private void checkPageBreak() {
        if (ws.getWkLine() >= 55) {
            runChain(this::printPageHeader);
            runChain(this::printStatementSubHeader);
        }
    }

    /** COBOL paragraph: NP-010 */
    private void startNewPage() {
        ws.setWkLine(99);
        runChain(this::printPageHeader);
    }

    /** COBOL paragraph: SSH-010 */
    private void printStatementSubHeader() {
        ws.setStCode(ws.getCuCode());
        ws.setStName(ws.getCuName());
        ws.copyRepRecFromRdStmt();
        writeReportLine();
        ws.copyRepRecFromRcHead();
        writeReportLine();
        ws.setRepRec(String.valueOf(ws.getRptRule()));
        writeReportLine();
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
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("PERIOD "));
            sb.append(String.valueOf(String.format("%08d", (long) (ws.getWkDateFrom()))));
            sb.append(String.valueOf(" - "));
            sb.append(String.valueOf(String.format("%08d", (long) (ws.getWkDateTo()))));
            ws.setH2Info(sb.toString());
        }
        ws.copyRepRecFromRptH2();
        writeReportRecord();
        ws.setRepRec(" ");
        writeReportRecord();
        ws.setWkLine(4);
    }

    /** COBOL paragraph: PN-010 */
    private void printNoStatementsMessage() {
        if (ws.getWkStmtCnt() == 0) {
            runChain(this::startNewPage);
            ws.setRepRec("*** NO CUSTOMER STATEMENTS TO PRINT ***");
            writeReportRecord();
        }
    }

    /** COBOL paragraph: TERM-010 */
    private void finalizeAndCloseFiles() {
        fileSet.getCustf().close();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        fileSet.getArlf().close();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        fileSet.getRepf().close();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
        log.info(
                "RP0130 complete.  Statements printed = {}",
                String.format("%07d", (long) (ws.getWkStmtCnt())));
        ws.setCompletionCode(0);
    }

    /** COBOL paragraph: AB-010 */
    private void abortWithFileError() {
        ws.setKaProgid("RP0130");
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

    /** Write the current REPF record and capture its I/O status. */
    private void writeReportRecord() {
        fileSet.getRepf().write();
        ws.trySetString("FSTS", fileSet.getRepf().getFileStatus());
    }

    /** Write the current REPF record, capture status, and advance the line counter. */
    private void writeReportLine() {
        writeReportRecord();
        ws.setWkLine(ws.getWkLine() + 1);
    }

    /** Prompt on the log and return the trimmed stdin line, or empty string if blank. */
    private String readStdinOrEmpty(String prompt) {
        log.info(prompt);
        String value = Utility.readStdinLine();
        if (value == null || value.trim().isEmpty()) {
            value = "";
        }
        return value;
    }

    /** Parse a numeric input field, or return the default when blank/non-numeric. */
    private int parseIntOrDefault(String input, int defaultValue) {
        if (Utility.isNumeric(input) && !Utility.fieldEquals(input, " ")) {
            return Utility.parseNumeric(input).intValue();
        }
        return defaultValue;
    }
}

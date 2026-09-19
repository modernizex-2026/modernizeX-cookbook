package com.sakura.bt0040.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.bt0040.domain.Bt0040FieldAccess;
import com.sakura.bt0040.domain.WorkingStorage;
import com.sakura.bt0040.runtime.Bt0040Datasets;
import com.sakura.dateut.service.DateutService;
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

/** Business logic service generated from COBOL program BT0040. */
@Service
@Scope("prototype")
public class Bt0040Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Bt0040Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Bt0040FieldAccess ws;

    public Bt0040Service(
            Bt0040Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Bt0040FieldAccess(new WorkingStorage(), fileSet);
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
        if (ws.getWkAbortFlg() == 0) {
            runChain(this::clearCustomerBalances);
            runChain(this::rebuildLedgerBalances);
            runChain(this::printSummaryReport);
        }
        runChain(this::closeFiles);
        ws.setCompletionCode(0);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("BT0040");
        log.info(" ");
        log.info("==============================================");
        log.info(" SAKURA-SMS  BT0040  -  AR BALANCE REBUILD");
        log.info("==============================================");
        runChain(this::loadSystemDate);
        runChain(this::openFiles);
        runChain(this::confirmWithOperator);
    }

    /** COBOL paragraph: GTOD-010 */
    private void loadSystemDate() {
        ws.setKdFunc("TODY");
        ws.setKdDate1(0);
        dateut(ws.getKdate());
        ws.setWkSysdate(ws.getKdDate1());
        ws.setWkSysymd(ws.getKdDate1());
    }

    /** COBOL paragraph: OPEN-010 */
    private void openFiles() {
        fileSet.getArlf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getArlf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
            fileSet.getArlf().close();
            ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
            fileSet.getArlf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("ARLF");
            ws.setKaDetail("Open ARLF failed");
            runChain(this::abortProgram);
        }
        fileSet.getCustf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getCustf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
            fileSet.getCustf().close();
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
            fileSet.getCustf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("CUSTF");
            ws.setKaDetail("Open CUSTF failed");
            runChain(this::abortProgram);
        }
    }

    /** COBOL paragraph: CONF-010 */
    private void confirmWithOperator() {
        log.info(" ");
        log.info(" Rebuild AR ledger running balances and");
        log.info(" customer master balances from the ledger.");
        log.info(" Proceed ? (Y/N) : ");
        ws.setWkConfirm(" ");
        String stdinValWkConfirm0 = Utility.readStdinLine();
        if (stdinValWkConfirm0 == null || stdinValWkConfirm0.trim().isEmpty()) {
            stdinValWkConfirm0 = "";
        }
        ws.setWkConfirm(stdinValWkConfirm0);
        if (!((ws.getWkConfirm().equals("Y") || ws.getWkConfirm().equals("y")))) {
            log.info(" ** cancelled by operator.");
            ws.setWkAbortFlg(1);
        }
    }

    /** COBOL paragraph: CLRM-010 */
    private void clearCustomerBalances() {
        log.info(" ");
        log.info(" Clearing customer balances ...");
        ws.setEofFlg(0);
        ws.setCuCode(0);
        fileSet.getCustf().start("CU-CODE", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setString("EOF-FLG", "1");
        }
        while ((ws.getEofFlg() != 1)) {
            runChain(this::clearNextCustomerBalance);
        }
    }

    /** COBOL paragraph: CLRX-010 */
    private void clearNextCustomerBalance() {
        fileSet.getCustf().readNext();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isAtEnd()) {
            ws.setString("EOF-FLG", "1");
            return;
        }
        ws.setWkZeroRead(ws.getWkZeroRead() + 1);
        if (ws.getCuBalance().signum() != 0) {
            ws.setCuBalance(BigDecimal.ZERO);
            fileSet.getCustf().rewrite();
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
            if (fileSet.getCustf().isInvalidKey()) {
                ws.setKaFile("CUSTF");
                ws.setKaDetail("Clear CUSTF balance failed");
                runChain(this::abortProgram);
            }
            ws.setWkZeroClr(ws.getWkZeroClr() + 1);
        }
    }

    /** COBOL paragraph: PROC-010 */
    private void rebuildLedgerBalances() {
        log.info(" ");
        log.info(" Rebuilding ...");
        ws.setEofFlg(0);
        ws.setWkFirstFlg(1);
        ws.setWkPrevCust(0);
        ws.setWkRunBal(0);
        ws.setAlCust(0);
        ws.setAlDate(0);
        fileSet.getArlf().start("AL-CUST", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        if (fileSet.getArlf().isInvalidKey()) {
            ws.setString("EOF-FLG", "1");
        }
        while ((ws.getEofFlg() != 1)) {
            runChain(this::processNextLedgerEntry);
        }
        if (ws.getWkFirstFlg() == 0) {
            runChain(this::updateCustomerBalance);
        }
    }

    /** COBOL paragraph: PNXT-010 */
    private void processNextLedgerEntry() {
        fileSet.getArlf().readNext();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        if (fileSet.getArlf().isAtEnd()) {
            ws.setString("EOF-FLG", "1");
            return;
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00") && !Utility.fieldEquals(ws.getFsts(), "02")) {
            ws.setKaFile("ARLF");
            ws.setKaDetail("READ NEXT ARLF failed");
            runChain(this::abortProgram);
        }
        if (ws.getWkFirstFlg() == 1) {
            ws.setWkFirstFlg(0);
            ws.setWkPrevCust(ws.getAlCust());
            ws.setWkRunBal(0);
        }
        if (ws.getAlCust() != ws.getWkPrevCust()) {
            runChain(this::updateCustomerBalance);
            ws.setWkPrevCust(ws.getAlCust());
            ws.setWkRunBal(0);
        }
        runChain(this::postLedgerEntry);
    }

    /** COBOL paragraph: PENT-010 */
    private void postLedgerEntry() {
        ws.setWkLedgCnt(ws.getWkLedgCnt() + 1);
        ws.setWkRunBal((BigDecimal.valueOf(ws.getWkRunBal()).add(ws.getAlDebit())).longValue());
        ws.setWkRunBal(
                (BigDecimal.valueOf(ws.getWkRunBal()).subtract(ws.getAlCredit())).longValue());
        ws.setWkDebitTot((BigDecimal.valueOf(ws.getWkDebitTot()).add(ws.getAlDebit())).longValue());
        ws.setWkCreditTot(
                (BigDecimal.valueOf(ws.getWkCreditTot()).add(ws.getAlCredit())).longValue());
        ws.setAlBalance(BigDecimal.valueOf(ws.getWkRunBal()));
        fileSet.getArlf().rewrite();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        if (fileSet.getArlf().isInvalidKey()) {
            ws.setKaFile("ARLF");
            ws.setKaDetail("REWRITE ARLF failed");
            runChain(this::abortProgram);
        }
    }

    /** COBOL paragraph: UCUS-010 */
    private void updateCustomerBalance() {
        if (ws.getWkPrevCust() == 0) {
            return;
        }
        ws.setWkCustCnt(ws.getWkCustCnt() + 1);
        ws.setWkBalTot(ws.getWkBalTot() + ws.getWkRunBal());
        ws.setCuCode(ws.getWkPrevCust());
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
            ws.setWkNfCnt(ws.getWkNfCnt() + 1);
            return;
        }
        ws.setCuBalance(BigDecimal.valueOf(ws.getWkRunBal()));
        ws.setCuUpdDate(ws.getWkSysdate());
        ws.setCuUpdUser(ws.getWkUserCode());
        fileSet.getCustf().rewrite();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isInvalidKey()) {
            ws.setKaFile("CUSTF");
            ws.setKaDetail("REWRITE CUSTF failed");
            runChain(this::abortProgram);
        }
        ws.setWkUpdCnt(ws.getWkUpdCnt() + 1);
        if (ws.getWkRunBal() != 0) {
            ws.setWkNonzeroCnt(ws.getWkNonzeroCnt() + 1);
            ws.setWkECode(ws.getWkPrevCust());
            ws.setWkEAmt(ws.getWkRunBal());
            log.info(
                    "   cust {} balance {}",
                    ws.editedDisplay("WK-E-CODE"),
                    ws.editedDisplay("WK-E-AMT"));
        }
    }

    /** COBOL paragraph: PSUM-010 */
    private void printSummaryReport() {
        log.info(" ");
        log.info("----------------------------------------------");
        log.info(" AR BALANCE REBUILD SUMMARY");
        log.info("----------------------------------------------");
        ws.setWkECnt(ws.getWkZeroRead());
        log.info(" Customers cleared : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkZeroClr());
        log.info("   had prior bal   : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkLedgCnt());
        log.info(" Ledger entries    : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkCustCnt());
        log.info(" Customers in ledg : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkUpdCnt());
        log.info(" Masters updated   : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkNfCnt());
        log.info(" Masters not found : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkNonzeroCnt());
        log.info(" Outstanding accts : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkEAmt(ws.getWkDebitTot());
        log.info(" Total debit       : {}", ws.editedDisplay("WK-E-AMT"));
        ws.setWkEAmt(ws.getWkCreditTot());
        log.info(" Total credit      : {}", ws.editedDisplay("WK-E-AMT"));
        ws.setWkEAmt(ws.getWkBalTot());
        log.info(" Sum of balances   : {}", ws.editedDisplay("WK-E-AMT"));
        log.info("----------------------------------------------");
        log.info(" BT0040 completed normally.");
    }

    /** COBOL paragraph: TERM-010 */
    private void closeFiles() {
        fileSet.getArlf().close();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        fileSet.getCustf().close();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
    }

    /** COBOL paragraph: ABND-010 */
    private void abortProgram() {
        ws.setKaProgid("BT0040");
        ws.setKaFsts(ws.getFsts());
        ws.setKaMsgcode("EBATCH");
        abortx(ws.getKabend());
        fileSet.getArlf().close();
        ws.trySetString("FSTS", fileSet.getArlf().getFileStatus());
        fileSet.getCustf().close();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
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

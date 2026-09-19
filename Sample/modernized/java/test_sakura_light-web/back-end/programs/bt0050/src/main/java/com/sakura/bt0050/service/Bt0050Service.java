package com.sakura.bt0050.service;

import com.sakura.abortx.service.AbortxService;
import com.sakura.bt0050.domain.Bt0050FieldAccess;
import com.sakura.bt0050.domain.WorkingStorage;
import com.sakura.bt0050.runtime.Bt0050Datasets;
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

/** Business logic service generated from COBOL program BT0050. */
@Service
@Scope("prototype")
public class Bt0050Service extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final Bt0050Datasets fileSet;

    /** Injected service for COBOL CALL DATEUT. */
    private DateutService dateutService;

    /** Injected service for COBOL CALL ABORTX. */
    private AbortxService abortxService;

    private final Bt0050FieldAccess ws;

    public Bt0050Service(
            Bt0050Datasets fileSet, DateutService dateutService, AbortxService abortxService) {
        this.fileSet = fileSet;
        this.ws = new Bt0050FieldAccess(new WorkingStorage(), fileSet);
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
            runChain(this::clearSupplierBalances);
            runChain(this::rebuildLedgerBalances);
            runChain(this::printSummaryReport);
        }
        runChain(this::closeFiles);
        ws.setCompletionCode(0);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: INIT-010 */
    private void initializeProgram() {
        ws.setWkProgid("BT0050");
        log.info(" ");
        log.info("==============================================");
        log.info(" SAKURA-SMS  BT0050  -  AP BALANCE REBUILD");
        log.info("==============================================");
        runChain(this::loadSystemDate);
        runChain(this::openFiles);
        runChain(this::confirmProceedWithOperator);
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
        fileSet.getAplf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getAplf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
            fileSet.getAplf().close();
            ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
            fileSet.getAplf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("APLF");
            ws.setKaDetail("Open APLF failed");
            runChain(this::abortProgram);
        }
        fileSet.getSuppf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getSuppf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
            fileSet.getSuppf().close();
            ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
            fileSet.getSuppf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            ws.setKaFile("SUPPF");
            ws.setKaDetail("Open SUPPF failed");
            runChain(this::abortProgram);
        }
    }

    /** COBOL paragraph: CONF-010 */
    private void confirmProceedWithOperator() {
        log.info(" ");
        log.info(" Rebuild AP ledger running balances and");
        log.info(" supplier master balances from the ledger.");
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
    private void clearSupplierBalances() {
        log.info(" ");
        log.info(" Clearing supplier balances ...");
        ws.setEofFlg(0);
        ws.setSpCode(0);
        fileSet.getSuppf().start("SP-CODE", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setString("EOF-FLG", "1");
        }
        while ((ws.getEofFlg() != 1)) {
            runChain(this::clearNextSupplierBalance);
        }
    }

    /** COBOL paragraph: CLRX-010 */
    private void clearNextSupplierBalance() {
        fileSet.getSuppf().readNext();
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        if (fileSet.getSuppf().isAtEnd()) {
            ws.setString("EOF-FLG", "1");
            return;
        }
        ws.setWkZeroRead(ws.getWkZeroRead() + 1);
        if (ws.getSpBalance().signum() != 0) {
            ws.setSpBalance(BigDecimal.ZERO);
            fileSet.getSuppf().rewrite();
            ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
            if (fileSet.getSuppf().isInvalidKey()) {
                ws.setKaFile("SUPPF");
                ws.setKaDetail("Clear SUPPF balance failed");
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
        ws.setWkPrevSupp(0);
        ws.setWkRunBal(0);
        ws.setPlSupp(0);
        ws.setPlDate(0);
        fileSet.getAplf().start("PL-SUPP", "NOT LESS THAN");
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        if (fileSet.getAplf().isInvalidKey()) {
            ws.setString("EOF-FLG", "1");
        }
        while ((ws.getEofFlg() != 1)) {
            runChain(this::processNextLedgerEntry);
        }
        if (ws.getWkFirstFlg() == 0) {
            runChain(this::updateSupplierBalance);
        }
    }

    /** COBOL paragraph: PNXT-010 */
    private void processNextLedgerEntry() {
        fileSet.getAplf().readNext();
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        if (fileSet.getAplf().isAtEnd()) {
            ws.setString("EOF-FLG", "1");
            return;
        }
        if (!Utility.fieldEquals(ws.getFsts(), "00") && !Utility.fieldEquals(ws.getFsts(), "02")) {
            ws.setKaFile("APLF");
            ws.setKaDetail("READ NEXT APLF failed");
            runChain(this::abortProgram);
        }
        if (ws.getWkFirstFlg() == 1) {
            ws.setWkFirstFlg(0);
            ws.setWkPrevSupp(ws.getPlSupp());
            ws.setWkRunBal(0);
        }
        if (ws.getPlSupp() != ws.getWkPrevSupp()) {
            runChain(this::updateSupplierBalance);
            ws.setWkPrevSupp(ws.getPlSupp());
            ws.setWkRunBal(0);
        }
        runChain(this::applyLedgerEntryToBalance);
    }

    /** COBOL paragraph: PENT-010 */
    private void applyLedgerEntryToBalance() {
        ws.setWkLedgCnt(ws.getWkLedgCnt() + 1);
        ws.setWkRunBal((BigDecimal.valueOf(ws.getWkRunBal()).add(ws.getPlCredit())).longValue());
        ws.setWkRunBal(
                (BigDecimal.valueOf(ws.getWkRunBal()).subtract(ws.getPlDebit())).longValue());
        ws.setWkDebitTot((BigDecimal.valueOf(ws.getWkDebitTot()).add(ws.getPlDebit())).longValue());
        ws.setWkCreditTot(
                (BigDecimal.valueOf(ws.getWkCreditTot()).add(ws.getPlCredit())).longValue());
        ws.setPlBalance(BigDecimal.valueOf(ws.getWkRunBal()));
        fileSet.getAplf().rewrite();
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        if (fileSet.getAplf().isInvalidKey()) {
            ws.setKaFile("APLF");
            ws.setKaDetail("REWRITE APLF failed");
            runChain(this::abortProgram);
        }
    }

    /** COBOL paragraph: USUP-010 */
    private void updateSupplierBalance() {
        if (ws.getWkPrevSupp() == 0) {
            return;
        }
        ws.setWkSuppCnt(ws.getWkSuppCnt() + 1);
        ws.setWkBalTot(ws.getWkBalTot() + ws.getWkRunBal());
        ws.setSpCode(ws.getWkPrevSupp());
        String rkVal_1 = "";
        if (rkVal_1 == null || rkVal_1.trim().isEmpty()) {
            try {
                rkVal_1 = ws.getString("SP-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_1 == null || rkVal_1.trim().isEmpty()) {
            try {
                rkVal_1 = fileSet.getSuppf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getSuppf().readByKey(rkVal_1 != null ? rkVal_1.trim() : "");
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setWkNfCnt(ws.getWkNfCnt() + 1);
            return;
        }
        ws.setSpBalance(BigDecimal.valueOf(ws.getWkRunBal()));
        ws.setSpUpdDate(ws.getWkSysdate());
        ws.setSpUpdUser(ws.getWkUserCode());
        fileSet.getSuppf().rewrite();
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
        if (fileSet.getSuppf().isInvalidKey()) {
            ws.setKaFile("SUPPF");
            ws.setKaDetail("REWRITE SUPPF failed");
            runChain(this::abortProgram);
        }
        ws.setWkUpdCnt(ws.getWkUpdCnt() + 1);
        if (ws.getWkRunBal() != 0) {
            ws.setWkNonzeroCnt(ws.getWkNonzeroCnt() + 1);
            ws.setWkECode(ws.getWkPrevSupp());
            ws.setWkEAmt(ws.getWkRunBal());
            log.info(
                    "   supp {} balance {}",
                    ws.editedDisplay("WK-E-CODE"),
                    ws.editedDisplay("WK-E-AMT"));
        }
    }

    /** COBOL paragraph: PSUM-010 */
    private void printSummaryReport() {
        log.info(" ");
        log.info("----------------------------------------------");
        log.info(" AP BALANCE REBUILD SUMMARY");
        log.info("----------------------------------------------");
        ws.setWkECnt(ws.getWkZeroRead());
        log.info(" Suppliers cleared : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkZeroClr());
        log.info("   had prior bal   : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkLedgCnt());
        log.info(" Ledger entries    : {}", ws.editedDisplay("WK-E-CNT"));
        ws.setWkECnt(ws.getWkSuppCnt());
        log.info(" Suppliers in ledg : {}", ws.editedDisplay("WK-E-CNT"));
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
        log.info(" BT0050 completed normally.");
    }

    /** COBOL paragraph: TERM-010 */
    private void closeFiles() {
        fileSet.getAplf().close();
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        fileSet.getSuppf().close();
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
    }

    /** COBOL paragraph: ABND-010 */
    private void abortProgram() {
        ws.setKaProgid("BT0050");
        ws.setKaFsts(ws.getFsts());
        ws.setKaMsgcode("EBATCH");
        abortx(ws.getKabend());
        fileSet.getAplf().close();
        ws.trySetString("FSTS", fileSet.getAplf().getFileStatus());
        fileSet.getSuppf().close();
        ws.trySetString("FSTS", fileSet.getSuppf().getFileStatus());
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

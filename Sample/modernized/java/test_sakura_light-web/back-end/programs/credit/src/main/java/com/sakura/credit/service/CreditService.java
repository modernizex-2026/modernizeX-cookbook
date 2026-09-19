package com.sakura.credit.service;

import com.sakura.credit.domain.CreditFieldAccess;
import com.sakura.credit.domain.WorkingStorage;
import com.sakura.credit.runtime.CreditDatasets;
import com.sakura.runtime.BatchServiceBase;
import com.sakura.runtime.DatasetEnums.*;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.AbstractDatasets;
import com.sakura.runtime.linkage.CreditLinkParm;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Business logic service generated from COBOL program CREDIT. */
@Service
@Scope("prototype")
public class CreditService extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final CreditDatasets fileSet;

    private final CreditLinkParm link = new CreditLinkParm();

    private final CreditFieldAccess ws;

    public CreditService(CreditDatasets fileSet) {
        this.fileSet = fileSet;
        this.ws = new CreditFieldAccess(new WorkingStorage(), fileSet);
    }

    /**
     * Program entry point — runs the first COBOL paragraph via runChain. Base class run() wraps
     * this in StopRun/Abort/Exception handling.
     */
    @Override
    protected void mainProcess() {
        // Import anchors for types referenced by emitted paragraph bodies:
        // StopRunSignal, JobAbortException, ParagraphJumpSignal, ProgramExitSignal
        runChain(this::runCreditCheckProgram);
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
    private void runCreditCheckProgram() {
        link.getKcred().setKcStatus("00");
        link.getKcred().setKcExceed(0);
        link.getKcred().setKcLimit(BigDecimal.ZERO);
        link.getKcred().setKcBalance(BigDecimal.ZERO);
        link.getKcred().setKcNewbal(BigDecimal.ZERO);
        runChain(this::openCustomerFileWithCreate);
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            link.getKcred().setKcStatus("99");
            runChain(this::closeCustomerFile);
            throw new ProgramExitSignal();
        }
        runChain(this::checkCreditLimit);
        runChain(this::closeCustomerFile);
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: OPEN-010 */
    private void openCustomerFileWithCreate() {
        fileSet.getCustf().open(FileOpenMode.INPUT);
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getCustf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
            fileSet.getCustf().close();
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
            fileSet.getCustf().open(FileOpenMode.INPUT);
            ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        }
    }

    /** COBOL paragraph: CLOSE-010 */
    private void closeCustomerFile() {
        fileSet.getCustf().close();
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
    }

    /** COBOL paragraph: CHK-010 */
    private void checkCreditLimit() {
        ws.setCuCode(link.getKcred().getKcCust());
        String rkVal_0 = "";
        if (rkVal_0 == null || rkVal_0.trim().isEmpty()) {
            try {
                rkVal_0 = ws.getString("CU-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_0 == null || rkVal_0.trim().isEmpty()) {
            try {
                rkVal_0 = fileSet.getCustf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getCustf().readByKey(rkVal_0 != null ? rkVal_0.trim() : "");
        ws.trySetString("FSTS", fileSet.getCustf().getFileStatus());
        if (fileSet.getCustf().isInvalidKey()) {
            link.getKcred().setKcStatus("99");
            return;
        }
        link.getKcred().setKcLimit(ws.getCuCreditLimit());
        link.getKcred().setKcBalance(ws.getCuBalance());
        link.getKcred()
                .setKcNewbal(
                        (ws.getCuBalance().add(link.getKcred().getKcAmount()))
                                .setScale(0, java.math.RoundingMode.DOWN));
        if ((link.getKcred().getKcLimit().signum() > 0)
                && (link.getKcred().getKcNewbal().compareTo(link.getKcred().getKcLimit()) > 0)) {
            link.getKcred().setKcExceed(1);
        } else {
            link.getKcred().setKcExceed(0);
        }
    }

    /**
     * Execute this program as a COBOL CALL callee. Copies params into Linkage section, runs
     * business logic, copies back.
     */
    public void execute(CreditLinkParm params) {
        link.getKcred().setKcCust(params.getKcred().getKcCust());
        link.getKcred().setKcAmount(params.getKcred().getKcAmount());
        link.getKcred().setKcLimit(params.getKcred().getKcLimit());
        link.getKcred().setKcBalance(params.getKcred().getKcBalance());
        link.getKcred().setKcNewbal(params.getKcred().getKcNewbal());
        link.getKcred().setKcExceed(params.getKcred().getKcExceed());
        link.getKcred().setKcStatus(params.getKcred().getKcStatus());
        executeSubprogram(this::runCreditCheckProgram);
        params.getKcred().setKcCust(link.getKcred().getKcCust());
        params.getKcred().setKcAmount(link.getKcred().getKcAmount());
        params.getKcred().setKcLimit(link.getKcred().getKcLimit());
        params.getKcred().setKcBalance(link.getKcred().getKcBalance());
        params.getKcred().setKcNewbal(link.getKcred().getKcNewbal());
        params.getKcred().setKcExceed(link.getKcred().getKcExceed());
        params.getKcred().setKcStatus(link.getKcred().getKcStatus());
    }
}

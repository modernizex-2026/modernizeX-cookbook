package com.sakura.chklog.service;

import com.sakura.chklog.domain.ChklogFieldAccess;
import com.sakura.chklog.domain.WorkingStorage;
import com.sakura.chklog.runtime.ChklogDatasets;
import com.sakura.runtime.BatchServiceBase;
import com.sakura.runtime.DatasetEnums.*;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.AbstractDatasets;
import com.sakura.runtime.linkage.ChklogLinkParm;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Business logic service generated from COBOL program CHKLOG. */
@Service
@Scope("prototype")
public class ChklogService extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final ChklogDatasets fileSet;

    private final ChklogLinkParm link = new ChklogLinkParm();

    private final ChklogFieldAccess ws;

    public ChklogService(ChklogDatasets fileSet) {
        this.fileSet = fileSet;
        this.ws = new ChklogFieldAccess(new WorkingStorage(), fileSet);
    }

    /**
     * Program entry point — runs the first COBOL paragraph via runChain. Base class run() wraps
     * this in StopRun/Abort/Exception handling.
     */
    @Override
    protected void mainProcess() {
        // Import anchors for types referenced by emitted paragraph bodies:
        // StopRunSignal, JobAbortException, ParagraphJumpSignal, ProgramExitSignal
        runChain(this::validateUserLogin);
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
    private void validateUserLogin() {
        link.getKlogin().setKlStatus("00");
        link.getKlogin().setKlUserCode(0);
        link.getKlogin().setKlUserName(" ");
        link.getKlogin().setKlRole(0);
        link.getKlogin().setKlAuth(" ");
        fileSet.getUserf().open(FileOpenMode.INPUT);
        if (!Utility.fieldEquals(fileSet.getUserf().getFileStatus(), "00")) {
            link.getKlogin().setKlStatus("99");
            throw new ProgramExitSignal();
        }
        ws.setUsLogin(link.getKlogin().getKlLogin());
        String rkVal_0 = "";
        try {
            rkVal_0 = ws.getString("US-LOGIN");
        } catch (Exception _e) {
        }
        if (rkVal_0 == null || rkVal_0.trim().isEmpty()) {
            try {
                rkVal_0 = fileSet.getUserf().extractKeyFromCurrentRecord();
            } catch (Exception _e2) {
            }
        }
        fileSet.getUserf().readByKey("US-LOGIN", rkVal_0 != null ? rkVal_0.trim() : "");
        if (fileSet.getUserf().isInvalidKey()) {
            link.getKlogin().setKlStatus("99");
        }
        if (!fileSet.getUserf().isInvalidKey()) {
            runChain(this::verifyPasswordAndAuthority);
        }
        fileSet.getUserf().close();
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: CHECK-010 */
    private void verifyPasswordAndAuthority() {
        if (ws.getUsDelFlag() == 1) {
            link.getKlogin().setKlStatus("99");
            return;
        }
        if (!Utility.fieldEquals(
                ws.getUsPassword(), String.valueOf(link.getKlogin().getKlPassword()))) {
            link.getKlogin().setKlStatus("99");
            return;
        }
        link.getKlogin().setKlUserCode(ws.getUsCode());
        link.getKlogin().setKlUserName(ws.getUsName());
        link.getKlogin().setKlRole(ws.getUsRole());
        ws.setWkAuth(
                Utility.setSubstring(
                        String.valueOf(ws.getWkAuth()),
                        1,
                        1,
                        String.valueOf(ws.getUsAuthMaster())));
        ws.setWkAuth(
                Utility.setSubstring(
                        String.valueOf(ws.getWkAuth()), 2, 1, String.valueOf(ws.getUsAuthOrder())));
        ws.setWkAuth(
                Utility.setSubstring(
                        String.valueOf(ws.getWkAuth()), 3, 1, String.valueOf(ws.getUsAuthSales())));
        ws.setWkAuth(
                Utility.setSubstring(
                        String.valueOf(ws.getWkAuth()), 4, 1, String.valueOf(ws.getUsAuthPurch())));
        ws.setWkAuth(
                Utility.setSubstring(
                        String.valueOf(ws.getWkAuth()), 5, 1, String.valueOf(ws.getUsAuthClose())));
        link.getKlogin().setKlAuth(ws.getWkAuth());
    }

    /**
     * Execute this program as a COBOL CALL callee. Copies params into Linkage section, runs
     * business logic, copies back.
     */
    public void execute(ChklogLinkParm params) {
        link.getKlogin().setKlLogin(params.getKlogin().getKlLogin());
        link.getKlogin().setKlPassword(params.getKlogin().getKlPassword());
        link.getKlogin().setKlUserCode(params.getKlogin().getKlUserCode());
        link.getKlogin().setKlUserName(params.getKlogin().getKlUserName());
        link.getKlogin().setKlRole(params.getKlogin().getKlRole());
        link.getKlogin().setKlAuth(params.getKlogin().getKlAuth());
        link.getKlogin().setKlStatus(params.getKlogin().getKlStatus());
        executeSubprogram(this::validateUserLogin);
        params.getKlogin().setKlLogin(link.getKlogin().getKlLogin());
        params.getKlogin().setKlPassword(link.getKlogin().getKlPassword());
        params.getKlogin().setKlUserCode(link.getKlogin().getKlUserCode());
        params.getKlogin().setKlUserName(link.getKlogin().getKlUserName());
        params.getKlogin().setKlRole(link.getKlogin().getKlRole());
        params.getKlogin().setKlAuth(link.getKlogin().getKlAuth());
        params.getKlogin().setKlStatus(link.getKlogin().getKlStatus());
    }
}

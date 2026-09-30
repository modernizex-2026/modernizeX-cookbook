package com.sakura.getmsg.service;

import com.sakura.getmsg.domain.GetmsgFieldAccess;
import com.sakura.getmsg.domain.WorkingStorage;
import com.sakura.getmsg.runtime.GetmsgDatasets;
import com.sakura.runtime.BatchServiceBase;
import com.sakura.runtime.DatasetEnums.*;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.AbstractDatasets;
import com.sakura.runtime.linkage.GetmsgLinkParm;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Business logic service generated from COBOL program GETMSG. */
@Service
@Scope("prototype")
public class GetmsgService extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final GetmsgDatasets fileSet;

    private final GetmsgLinkParm link = new GetmsgLinkParm();

    private final GetmsgFieldAccess ws;

    public GetmsgService(GetmsgDatasets fileSet) {
        this.fileSet = fileSet;
        this.ws = new GetmsgFieldAccess(new WorkingStorage(), fileSet);
    }

    /**
     * Program entry point — runs the first COBOL paragraph via runChain. Base class run() wraps
     * this in StopRun/Abort/Exception handling.
     */
    @Override
    protected void mainProcess() {
        // Import anchors for types referenced by emitted paragraph bodies:
        // StopRunSignal, JobAbortException, ParagraphJumpSignal, ProgramExitSignal
        runChain(this::retrieveMessageByCode);
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
    private void retrieveMessageByCode() {
        link.getKmsg().setKmStatus("00");
        link.getKmsg().setKmText(" ");
        fileSet.getMsgf().open(FileOpenMode.INPUT);
        if (!Utility.fieldEquals(fileSet.getMsgf().getFileStatus(), "00")) {
            link.getKmsg().setKmText(link.getKmsg().getKmCode());
            link.getKmsg().setKmStatus("99");
            throw new ProgramExitSignal();
        }
        ws.setMgCode(link.getKmsg().getKmCode());
        String rkVal_0 = "";
        if (rkVal_0 == null || rkVal_0.trim().isEmpty()) {
            try {
                rkVal_0 = ws.getString("MG-CODE");
            } catch (Exception _e) {
            }
        }
        if (rkVal_0 == null || rkVal_0.trim().isEmpty()) {
            try {
                rkVal_0 = fileSet.getMsgf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getMsgf().readByKey(rkVal_0 != null ? rkVal_0.trim() : "");
        if (fileSet.getMsgf().isInvalidKey()) {
            link.getKmsg().setKmText(link.getKmsg().getKmCode());
            link.getKmsg().setKmStatus("01");
        }
        if (!fileSet.getMsgf().isInvalidKey()) {
            link.getKmsg().setKmText(ws.getMgText());
        }
        fileSet.getMsgf().close();
        throw new ProgramExitSignal();
    }

    /**
     * Execute this program as a COBOL CALL callee. Copies params into Linkage section, runs
     * business logic, copies back.
     */
    public void execute(GetmsgLinkParm params) {
        link.getKmsg().setKmCode(params.getKmsg().getKmCode());
        link.getKmsg().setKmText(params.getKmsg().getKmText());
        link.getKmsg().setKmStatus(params.getKmsg().getKmStatus());
        executeSubprogram(this::retrieveMessageByCode);
        params.getKmsg().setKmCode(link.getKmsg().getKmCode());
        params.getKmsg().setKmText(link.getKmsg().getKmText());
        params.getKmsg().setKmStatus(link.getKmsg().getKmStatus());
    }
}

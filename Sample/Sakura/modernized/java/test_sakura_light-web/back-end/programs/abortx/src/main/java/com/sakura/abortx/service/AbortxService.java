package com.sakura.abortx.service;

import com.sakura.abortx.domain.AbortxFieldAccess;
import com.sakura.abortx.domain.WorkingStorage;
import com.sakura.abortx.runtime.AbortxDatasets;
import com.sakura.runtime.BatchServiceBase;
import com.sakura.runtime.DatasetEnums.*;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.AbstractDatasets;
import com.sakura.runtime.linkage.AbortxLinkParm;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Business logic service generated from COBOL program ABORTX. */
@Service
@Scope("prototype")
public class AbortxService extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final AbortxDatasets fileSet;

    private final AbortxLinkParm link = new AbortxLinkParm();

    private final AbortxFieldAccess ws;

    public AbortxService(AbortxDatasets fileSet) {
        this.fileSet = fileSet;
        this.ws = new AbortxFieldAccess(new WorkingStorage(), fileSet);
    }

    /**
     * Program entry point — runs the first COBOL paragraph via runChain. Base class run() wraps
     * this in StopRun/Abort/Exception handling.
     */
    @Override
    protected void mainProcess() {
        // Import anchors for types referenced by emitted paragraph bodies:
        // StopRunSignal, JobAbortException, ParagraphJumpSignal, ProgramExitSignal
        runChain(this::writeAbendLogAndExit);
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
    private void writeAbendLogAndExit() {
        ws.setWlProgid(link.getKabend().getKaProgid());
        ws.setWlFile(link.getKabend().getKaFile());
        ws.setWlFsts(link.getKabend().getKaFsts());
        ws.setWlMsgcode(link.getKabend().getKaMsgcode());
        ws.setWlDetail(link.getKabend().getKaDetail());
        log.info("*** SAKURA-SMS ABEND ***");
        log.info("{}", ws.getWkLine());
        fileSet.getLogf().open(FileOpenMode.EXTEND);
        if (Utility.fieldEquals(fileSet.getLogf().getFileStatus(), "35")
                || Utility.fieldEquals(fileSet.getLogf().getFileStatus(), "30")) {
            fileSet.getLogf().open(FileOpenMode.OUTPUT);
        }
        if (Utility.fieldEquals(fileSet.getLogf().getFileStatus(), "00")) {
            ws.copyLogRecFromWkLine();
            fileSet.getLogf().write();
            fileSet.getLogf().close();
        }
        throw new ProgramExitSignal();
    }

    /**
     * Execute this program as a COBOL CALL callee. Copies params into Linkage section, runs
     * business logic, copies back.
     */
    public void execute(AbortxLinkParm params) {
        link.getKabend().setKaProgid(params.getKabend().getKaProgid());
        link.getKabend().setKaFile(params.getKabend().getKaFile());
        link.getKabend().setKaFsts(params.getKabend().getKaFsts());
        link.getKabend().setKaMsgcode(params.getKabend().getKaMsgcode());
        link.getKabend().setKaDetail(params.getKabend().getKaDetail());
        executeSubprogram(this::writeAbendLogAndExit);
        params.getKabend().setKaProgid(link.getKabend().getKaProgid());
        params.getKabend().setKaFile(link.getKabend().getKaFile());
        params.getKabend().setKaFsts(link.getKabend().getKaFsts());
        params.getKabend().setKaMsgcode(link.getKabend().getKaMsgcode());
        params.getKabend().setKaDetail(link.getKabend().getKaDetail());
    }
}

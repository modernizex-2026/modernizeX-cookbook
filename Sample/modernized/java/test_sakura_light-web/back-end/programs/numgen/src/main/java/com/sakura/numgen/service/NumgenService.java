package com.sakura.numgen.service;

import com.sakura.numgen.domain.NumgenFieldAccess;
import com.sakura.numgen.domain.WorkingStorage;
import com.sakura.numgen.runtime.NumgenDatasets;
import com.sakura.runtime.BatchServiceBase;
import com.sakura.runtime.DatasetEnums.*;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.AbstractDatasets;
import com.sakura.runtime.linkage.NumgenLinkParm;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Business logic service generated from COBOL program NUMGEN. */
@Service
@Scope("prototype")
public class NumgenService extends BatchServiceBase {
    /** Shared file instances for all FD files in this program. */
    private final NumgenDatasets fileSet;

    private final NumgenLinkParm link = new NumgenLinkParm();

    private final NumgenFieldAccess ws;

    public NumgenService(NumgenDatasets fileSet) {
        this.fileSet = fileSet;
        this.ws = new NumgenFieldAccess(new WorkingStorage(), fileSet);
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
        link.getKnum().setKnumStatus("00");
        runChain(this::openNumcfCreateIfMissing);
        if (!Utility.fieldEquals(ws.getFsts(), "00")) {
            link.getKnum().setKnumStatus("99");
            fileSet.getNumcf().close();
            ws.trySetString("FSTS", fileSet.getNumcf().getFileStatus());
            throw new ProgramExitSignal();
        }
        runChain(this::assignNextNumber);
        fileSet.getNumcf().close();
        ws.trySetString("FSTS", fileSet.getNumcf().getFileStatus());
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: OPEN-010 */
    private void openNumcfCreateIfMissing() {
        fileSet.getNumcf().open(FileOpenMode.IO);
        ws.trySetString("FSTS", fileSet.getNumcf().getFileStatus());
        if (Utility.fieldEquals(ws.getFsts(), "35") || Utility.fieldEquals(ws.getFsts(), "30")) {
            fileSet.getNumcf().open(FileOpenMode.OUTPUT);
            ws.trySetString("FSTS", fileSet.getNumcf().getFileStatus());
            fileSet.getNumcf().close();
            ws.trySetString("FSTS", fileSet.getNumcf().getFileStatus());
            fileSet.getNumcf().open(FileOpenMode.IO);
            ws.trySetString("FSTS", fileSet.getNumcf().getFileStatus());
        }
    }

    /** COBOL paragraph: ASSIGN-010 */
    private void assignNextNumber() {
        ws.setNmKey(link.getKnum().getKnumKey());
        String rkVal_0 = "";
        if (rkVal_0 == null || rkVal_0.trim().isEmpty()) {
            try {
                rkVal_0 = ws.getString("NM-KEY");
            } catch (Exception _e) {
            }
        }
        if (rkVal_0 == null || rkVal_0.trim().isEmpty()) {
            try {
                rkVal_0 = fileSet.getNumcf().extractKeyFromCurrentRecord();
            } catch (Exception _e3) {
            }
        }
        fileSet.getNumcf().readByKey(rkVal_0 != null ? rkVal_0.trim() : "");
        ws.trySetString("FSTS", fileSet.getNumcf().getFileStatus());
        if (fileSet.getNumcf().isInvalidKey()) {
            runChain(this::createNumcfRecord);
        }
        if (!fileSet.getNumcf().isInvalidKey()) {
            ws.setNmCurrent(ws.getNmCurrent() + 1);
            fileSet.getNumcf().rewrite();
            ws.trySetString("FSTS", fileSet.getNumcf().getFileStatus());
            if (fileSet.getNumcf().isInvalidKey()) {
                link.getKnum().setKnumStatus("99");
            }
            link.getKnum().setKnumNumber(ws.getNmCurrent());
        }
    }

    /** COBOL paragraph: CREATE-010 */
    private void createNumcfRecord() {
        fileSet.getNumcf().setRecord();
        ws.setNmKey(link.getKnum().getKnumKey());
        ws.setNmPrefix(" ");
        ws.setNmWidth(10);
        ws.setNmCurrent(1);
        fileSet.getNumcf().write();
        ws.trySetString("FSTS", fileSet.getNumcf().getFileStatus());
        if (fileSet.getNumcf().isInvalidKey()) {
            link.getKnum().setKnumStatus("99");
        }
        link.getKnum().setKnumNumber(ws.getNmCurrent());
    }

    /**
     * Execute this program as a COBOL CALL callee. Copies params into Linkage section, runs
     * business logic, copies back.
     */
    public void execute(NumgenLinkParm params) {
        link.getKnum().setKnumKey(params.getKnum().getKnumKey());
        link.getKnum().setKnumNumber(params.getKnum().getKnumNumber());
        link.getKnum().setKnumStatus(params.getKnum().getKnumStatus());
        executeSubprogram(this::runMainProgram);
        params.getKnum().setKnumKey(link.getKnum().getKnumKey());
        params.getKnum().setKnumNumber(link.getKnum().getKnumNumber());
        params.getKnum().setKnumStatus(link.getKnum().getKnumStatus());
    }
}

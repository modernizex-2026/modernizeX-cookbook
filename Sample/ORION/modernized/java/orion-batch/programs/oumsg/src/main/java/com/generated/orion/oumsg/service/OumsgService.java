package com.generated.orion.oumsg.service;

import com.generated.orion.oumsg.domain.OumsgFieldAccess;
import com.generated.orion.oumsg.domain.WorkingStorage;
import com.generated.orion.runtime.BatchServiceBase;
import com.generated.orion.runtime.DatasetEnums.*;
import com.generated.orion.runtime.ProgramExitSignal;
import com.generated.orion.runtime.io.AbstractDatasets;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Business logic service generated from COBOL program OUMSG. */
@Service
@Scope("prototype")
public class OumsgService extends BatchServiceBase {
    private final OumsgFieldAccess ws = new OumsgFieldAccess(new WorkingStorage());

    /**
     * Program entry point — runs the first COBOL paragraph via runChain. Base class run() wraps
     * this in StopRun/Abort/Exception handling.
     */
    @Override
    protected void mainProcess() {
        // Import anchors for types referenced by emitted paragraph bodies:
        // StopRunSignal, JobAbortException, ParagraphJumpSignal, ProgramExitSignal
        runChain(this::resolveKmStatusMessage);
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
        return null;
    }

    /** COBOL paragraph: 0000-MAIN */
    private void resolveKmStatusMessage() {
        ws.setKmStatus("00");
        switch (String.valueOf(ws.getKmCode()).stripTrailing()) {
            case "I0001" -> {
                ws.setKmText("Operation completed successfully.");
            }
            case "E0001" -> {
                ws.setKmText("Record not found.");
            }
            case "E0002" -> {
                ws.setKmText("Duplicate record.");
            }
            case "E0003" -> {
                ws.setKmText("Please enter required fields.");
            }
            case "E0004" -> {
                ws.setKmText("Invalid data entered.");
            }
            case "E0005" -> {
                ws.setKmText("File access error.");
            }
            case "W0001" -> {
                ws.setKmText("No records to display.");
            }
            default -> {
                ws.setKmText(ws.getKmCode());
                ws.setKmStatus("01");
            }
        }
        throw new ProgramExitSignal();
    }
}

package com.sakura.runtime;

/**
 * Signals COBOL EXIT PROGRAM / GOBACK — leaves the current program, unwinding any PERFORM stack.
 * Distinct from {@link StopRunSignal} (which halts the whole run unit): in a CALLed subprogram,
 * EXIT PROGRAM returns to the caller, whereas STOP RUN terminates the entire run. {@code
 * BatchServiceBase.executeSubprogram} catches this and returns to the caller; {@code run()} catches
 * it as normal program termination (preserving the completion code — e.g. 255 set by an ABEND-RTN
 * that exits before TERM-RTN would reset it).
 */
public class ProgramExitSignal extends RuntimeException {
    public ProgramExitSignal() {
        super("EXIT PROGRAM");
    }

    public ProgramExitSignal(String message) {
        super(message);
    }
}

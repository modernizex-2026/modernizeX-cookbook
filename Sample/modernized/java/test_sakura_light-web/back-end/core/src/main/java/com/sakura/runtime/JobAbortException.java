package com.sakura.runtime;

/**
 * Signals COBOL STOP ABORT — abnormal termination.
 *
 * <p>Caught by Service.execute() which:
 *
 * <ul>
 *   <li>Sets completionCode = 255 (COBOL ACOS abend convention)
 *   <li>Closes files in finally block
 *   <li>Re-throws so the Tasklet records FAILED step exit
 * </ul>
 *
 * <p>Distinct from {@link StopRunSignal} (normal termination, exit 0).
 */
public class JobAbortException extends RuntimeException {
    public JobAbortException() {
        super("STOP ABORT");
    }

    public JobAbortException(String message) {
        super(message);
    }
}

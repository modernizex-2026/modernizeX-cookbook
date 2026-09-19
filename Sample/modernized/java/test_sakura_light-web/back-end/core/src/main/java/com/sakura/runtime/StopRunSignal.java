package com.sakura.runtime;

/**
 * Signals COBOL STOP RUN — terminates the current program. Caught by the Tasklet to perform cleanup
 * (close files, set completion code).
 */
public class StopRunSignal extends RuntimeException {
    public StopRunSignal() {
        super("STOP RUN");
    }

    public StopRunSignal(String message) {
        super(message);
    }
}

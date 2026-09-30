package com.appruntime;

/**
 * Thrown by COBOL {@code STOP RUN} to terminate the run unit. This is a control-flow exception, not
 * an error condition: {@link AppRunner#execute} catches it and treats it as normal program
 * completion (control returns to the terminal), analogous to a top-level GOBACK.
 *
 * <p>STOP RUN from a CALLed subprogram terminates the entire run unit, so this exception is allowed
 * to propagate out of {@link AppRunner#callProgram} up to {@code execute()}.
 */
public class StopRunException extends RuntimeException {

    public StopRunException() {
        super("STOP RUN");
    }

    public StopRunException(String message) {
        super(message);
    }
}

package com.appruntime;

/** Thrown when a CICS program issues EXEC CICS ABEND. */
public class AbendException extends RuntimeException {

    private final String abcode;

    public AbendException(String abcode) {
        super("CICS ABEND: " + abcode);
        this.abcode = abcode;
    }

    public String getAbcode() {
        return abcode;
    }
}

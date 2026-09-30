package com.appruntime;

/**
 * Thrown when a CICS RECEIVE MAP operation fails (MAPFAIL condition). Typically indicates the
 * terminal sent no data.
 */
public class MapfailException extends RuntimeException {

    public MapfailException() {
        super("CICS MAPFAIL");
    }

    public MapfailException(String message) {
        super(message);
    }
}

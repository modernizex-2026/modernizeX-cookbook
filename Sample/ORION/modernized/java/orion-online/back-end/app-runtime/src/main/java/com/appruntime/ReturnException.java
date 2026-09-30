package com.appruntime;

/**
 * Thrown by {@link AppService#returnTransid} to implement CICS RETURN TRANSID flow control. This is
 * a control-flow exception, not an error condition.
 */
public class ReturnException extends RuntimeException {

    private final String transid;
    private final Object commarea;
    private final int length;

    public ReturnException(String transid, Object commarea, int length) {
        super("CICS RETURN TRANSID(" + transid + ")");
        this.transid = transid;
        this.commarea = commarea;
        this.length = length;
    }

    public String getTransid() {
        return transid;
    }

    public Object getCommarea() {
        return commarea;
    }

    public int getLength() {
        return length;
    }
}

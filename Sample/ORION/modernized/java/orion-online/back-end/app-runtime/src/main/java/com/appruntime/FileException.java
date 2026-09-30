package com.appruntime;

/** Base exception for CICS file I/O errors. */
public abstract class FileException extends RuntimeException {

    private final String fileName;
    private final int resp;
    private final int resp2;

    protected FileException(String fileName, int resp, String message) {
        this(fileName, resp, 0, message);
    }

    protected FileException(String fileName, int resp, int resp2, String message) {
        super(message);
        this.fileName = fileName;
        this.resp = resp;
        this.resp2 = resp2;
    }

    public String getFileName() {
        return fileName;
    }

    public int getResp() {
        return resp;
    }

    public int getResp2() {
        return resp2;
    }
}

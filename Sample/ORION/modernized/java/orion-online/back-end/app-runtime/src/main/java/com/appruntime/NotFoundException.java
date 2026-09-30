package com.appruntime;

/** Thrown when a CICS file READ finds no matching record (NOTFND condition). */
public class NotFoundException extends FileException {

    public NotFoundException(String fileName) {
        super(fileName, AppResp.NOTFND, "Record not found in file: " + fileName);
    }
}

package com.appruntime;

/**
 * Thrown when a CICS file WRITE collides with an existing record key. CICS VSAM KSDS: WRITE with an
 * existing PRIMARY key → DUPREC (RESP=14). (DUPKEY/RESP=15 is the alternate-index condition, raised
 * when an operation completes but a non-unique AIX key exists.) A JDBC primary-key violation on
 * INSERT maps to DUPREC, which is what programs test after WRITE.
 */
public class DuplicateException extends FileException {

    public DuplicateException(String fileName) {
        super(fileName, AppResp.DUPREC, "Duplicate key in file: " + fileName);
    }
}

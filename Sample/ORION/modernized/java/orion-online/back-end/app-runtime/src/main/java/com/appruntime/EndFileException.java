package com.appruntime;

/** Thrown when a CICS browse operation reaches end of file (ENDFILE condition). */
public class EndFileException extends FileException {

    public EndFileException(String fileName) {
        super(fileName, AppResp.ENDFILE, "End of file reached: " + fileName);
    }
}

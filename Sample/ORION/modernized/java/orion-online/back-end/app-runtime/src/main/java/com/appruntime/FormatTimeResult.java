package com.appruntime;

/**
 * Result of EXEC CICS FORMATTIME — holds formatted date and time strings. Used as return value from
 * AppService.formatTime() because Java cannot write back to the caller's fields through
 * pass-by-value parameters.
 */
public class FormatTimeResult {
    private final String date;
    private final String time;

    public FormatTimeResult(String date, String time) {
        this.date = date;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}

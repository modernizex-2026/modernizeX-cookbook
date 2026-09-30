package com.appruntime;

/** Constants for EIBRESP values, matching CICS response codes. */
public final class AppResp {

    public static final int NORMAL = 0;
    public static final int ERROR = 1;
    public static final int NOTFND = 13;
    public static final int DUPREC = 14;
    public static final int DUPKEY = 15;

    /** Dataset không được cài đặt/đăng ký — DFHRESP(FILENOTFOUND) */
    public static final int FILENOTFOUND = 12;

    public static final int INVREQ = 16;
    public static final int NOTOPEN = 19;
    public static final int ENDFILE = 20;

    /** START với TRANSID rỗng/không hợp lệ — DFHRESP(TRANSIDERR) */
    public static final int TRANSIDERR = 28;

    /** RETRIEVE không còn/không có start-data — DFHRESP(ENDDATA) */
    public static final int ENDDATA = 29;

    public static final int LENGERR = 22;
    public static final int ITEMERR = 26;
    public static final int PGMIDERR = 27;
    public static final int MAPFAIL = 36;
    public static final int QIDERR = 44;
    public static final int SYSIDERR = 53;
    public static final int DISABLED = 84;

    private AppResp() {
        // Prevent instantiation
    }
}

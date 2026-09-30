package com.appruntime;

/**
 * Interface defining all CICS service operations available to converted programs. Maps CICS API
 * calls to Java method invocations.
 */
public interface AppService {

    // --- Map operations ---
    void sendMap(
            String mapName,
            Object mapOutput,
            Object mapInput,
            boolean eraseScreen,
            boolean freekb,
            boolean cursor);

    void sendMapOnly(String mapName, Object mapOutput, boolean eraseScreen);

    void receiveMap(String mapName, Object mapInput);

    /** EXEC CICS SEND TEXT FROM(data) — send raw text to terminal */
    void sendText(String text, boolean eraseScreen, boolean freekb);

    /** EXEC CICS SEND FROM(data) — send raw data to terminal */
    void sendFrom(Object data, boolean eraseScreen, boolean freekb);

    // --- Program control ---
    void returnTransid(String transid, Object commarea, int length);

    void returnProgram();

    void xctl(String program, Object commarea, int length);

    void link(String program, Object commarea, int length);

    /**
     * COBOL CALL USING — direct subroutine call via Object[] params. Unlike LINK, does NOT
     * save/restore CICS context (commarea, eibcalen). The Object[] acts as shared mutable memory
     * for BY REFERENCE semantics.
     */
    void callProgram(String program, Object[] params);

    // --- File I/O ---
    void readFile(String fileName, Object into, String ridfld, int keyLength);

    void readFileForUpdate(String fileName, Object into, String ridfld, int keyLength);

    void writeFile(String fileName, Object from, String ridfld, int keyLength);

    void rewriteFile(String fileName, Object from);

    void deleteFile(String fileName, String ridfld, int keyLength);

    // --- Browse operations ---
    void startBrowse(String fileName, String ridfld, int keyLength);

    void readNext(String fileName, Object into);

    void readPrev(String fileName, Object into);

    void resetBrowse(String fileName, String ridfld, int keyLength);

    void endBrowse(String fileName);

    // --- Temporary Storage (TS) queue ---
    void writeQueue(String queueName, Object from, boolean rewrite);

    /**
     * EXEC CICS WRITEQ TS ... ITEM(n): item 1-based. rewrite+item>0 → thay item n; rewrite+item==0
     * → thay item cuối; ngược lại append. item ngoài phạm vi → ITEMERR.
     */
    default void writeQueue(String queueName, Object from, boolean rewrite, int item) {
        writeQueue(queueName, from, rewrite);
    }

    void readQueue(String queueName, Object into);

    void deleteQueue(String queueName);

    // --- Transient Data (TD) queue ---
    void writeQueueTd(String queueName, Object from, int length);

    void readQueueTd(String queueName, Object into, int length);

    void deleteQueueTd(String queueName);

    // --- Time operations ---
    void askTime();

    /**
     * EXEC CICS FORMATTIME. dateForm is the requested output option name (YYYYMMDD, DDMMYY, ...);
     * dateSep/timeSep are the DATESEP/TIMESEP characters ("" when the option is absent — CICS emits
     * no separators then).
     */
    FormatTimeResult formatTime(String abstime, String dateForm, String dateSep, String timeSep);

    // --- Miscellaneous ---
    void abend(String abcode);

    /**
     * EXEC CICS HANDLE ABEND LABEL(label) — register a handler that runs when an AbendException is
     * raised during program execution. Passing null clears the registration (equivalent to EXEC
     * CICS HANDLE ABEND CANCEL). The handler is scoped to the current transaction (per-request
     * context).
     */
    default void registerAbendHandler(Runnable handler) {}

    /**
     * EXEC CICS SYNCPOINT ROLLBACK / COBOL ROLLBACK — back out UOW hiện hành về syncpoint trước, mở
     * UOW mới
     */
    default void rollback() {}

    /** EXEC CICS SYNCPOINT / COBOL COMMIT — commit unit-of-work hiện hành ngay, mở UOW mới */
    default void syncpoint() {}

    void assign(Object target, String field);

    /**
     * EXEC CICS START TRANSID(t) FROM(data) — lưu start-data cho task của transid t. Web-runtime:
     * KHÔNG tự chạy async task; task đích chạy khi được invoke, RETRIEVE đọc data.
     */
    default void startTransaction(String transid, Object fromData, int length) {}

    /**
     * EXEC CICS RETRIEVE INTO(x) — đọc start-data của task hiện tại (theo EIBTRNID); không có →
     * EIBRESP ENDDATA(29).
     */
    default void retrieve(Object into) {}

    /** EXEC CICS ENQ RESOURCE(name) — lock a named resource */
    default void enqueue(String resource) {}

    /** EXEC CICS DEQ RESOURCE(name) — unlock a named resource */
    default void dequeue(String resource) {}

    /** EXEC CICS GETMAIN SET(ref) LENGTH(n) — allocate memory block */
    default byte[] getmain(int length) {
        return new byte[length];
    }

    /** EXEC CICS FREEMAIN DATA(ref) — release memory block (no-op in Java) */
    default void freemain(Object data) {}

    // --- EIB accessors ---
    int getEibresp();

    int getEibresp2();

    int getEibcalen();

    String getEibaid();

    String getEibfn();

    String getEibtrmid();

    int getEibdate();

    int getEibtime();

    String getEibtrnid();

    Object getCommarea();

    /**
     * Publish the COMMAREA bytes back to the execution context — used by a LINKed program's
     * mainLine to expose its (modified) COMMAREA region to the caller (LINK in/out semantics).
     */
    void setCommarea(Object commarea);

    /**
     * COMMAREA serialized as string — for COBOL pattern: MOVE DFHCOMMAREA(1:N) TO WS-COMMAREA.
     * Replaces link.getDfhcommarea() which is an empty Linkage placeholder.
     */
    String getSerializedCommarea();

    /** DFHEIBLK — Execute Interface Block, passed to utility programs like DSNTIAC */
    default String getDfheiblk() {
        return "";
    }

    /** SQLCA — SQL Communication Area stub (used by CALL DSNTIAC). */
    default String getSqlca() {
        return "";
    }

    /**
     * IMS DL/I service — available for CICS/DBCTL programs that use EXEC DLI. Returns null if IMS
     * is not configured.
     */
    default DliService getDliService() {
        return null;
    }
}

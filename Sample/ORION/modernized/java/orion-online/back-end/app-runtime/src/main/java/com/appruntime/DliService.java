package com.appruntime;

/**
 * IMS DL/I service abstraction — maps DL/I function codes to Java data access.
 *
 * <p>IMS programs use EXEC DLI (CICS/DBCTL mode) or CALL 'CBLTDLI' (batch mode). Both map to this
 * service interface. Implementation uses Spring JdbcTemplate with table-per-segment schema (Parent
 * FK Chain pattern).
 *
 * <p>PCB status is returned after each call:
 *
 * <ul>
 *   <li>{@code " "} (spaces) — success
 *   <li>{@code "GE"} — segment not found
 *   <li>{@code "GB"} — end of database
 *   <li>{@code "II"} — duplicate key on ISRT
 * </ul>
 *
 * <p>Runtime maintains IMS state: position, parent context, hold state. Generator emits simple
 * calls — runtime resolves parent/hierarchy automatically.
 */
public interface DliService {

    // --- PSB lifecycle ---

    void schedulePsb(String psbName);

    void terminatePsb();

    // --- GU / GHU — random access by key ---

    void getUnique(String segmentName, Object into, String keyField, Object keyValue);

    void getHoldUnique(String segmentName, Object into, String keyField, Object keyValue);

    /** GU with multi-key SSA: WHERE key1=val1 AND key2=val2 */
    default void getUniqueMultiKey(
            String segmentName, Object into, String[] keyFields, Object[] keyValues) {
        if (keyFields != null && keyFields.length > 0) {
            getUnique(segmentName, into, keyFields[0], keyValues[0]);
        } else {
            getUnique(segmentName, into, null, null);
        }
    }

    /** GHU with multi-key SSA */
    default void getHoldUniqueMultiKey(
            String segmentName, Object into, String[] keyFields, Object[] keyValues) {
        if (keyFields != null && keyFields.length > 0) {
            getHoldUnique(segmentName, into, keyFields[0], keyValues[0]);
        } else {
            getHoldUnique(segmentName, into, null, null);
        }
    }

    // --- GN / GHN — sequential forward scan ---

    /** GN unqualified — next row in segment table. */
    void getNext(String segmentName, Object into);

    /** GN qualified — next row matching SSA key filter. */
    void getNext(String segmentName, Object into, String keyField, Object keyValue);

    /** GHN — GN with hold for subsequent REPL/DLET. */
    void getHoldNext(String segmentName, Object into);

    // --- GNP / GHNP — next child under current parent ---

    /** GNP — next child segment under parent established by prior GU. Runtime resolves parent. */
    void getNextWithinParent(String segmentName, Object into);

    /** GHNP — GNP with hold for subsequent REPL/DLET. */
    void getHoldNextWithinParent(String segmentName, Object into);

    // --- ISRT / REPL / DLET ---

    void insert(String segmentName, Object from);

    void insertUnderParent(
            String parentSegment,
            String parentKey,
            Object parentValue,
            String childSegment,
            Object from);

    void replace(String segmentName, Object from);

    void delete(String segmentName);

    // --- CBLTDLI — batch-mode DL/I call interface ---

    /**
     * CALL 'CBLTDLI' — interprets the runtime function code ("GU ", "GHU ", "GN ", "ISRT", "REPL",
     * "DLET", …) and the SSA byte images the COBOL program built, and dispatches to the operations
     * above. Returns the 2-char DL/I PCB status for the caller to write back into its PCB mask (" "
     * OK, "GE" not found, "GB" end of DB, "II" duplicate, "DJ" no hold, "AD" invalid function).
     */
    String cbltdli(String function, Object ioArea, String... ssas);

    // --- CHKP / ROLB ---

    /** CHKP — checkpoint (commit). */
    default void checkpoint(String chkpId) {}

    /** ROLB — rollback. */
    default void rollback() {}

    // --- Status ---

    String getDibstat();
}

package com.appruntime;

/**
 * Accessor-agnostic byte-field API (BMS map byte model — Option C).
 *
 * <p>Lets app-runtime (MapBinder/AppRunner) read/write COBOL fields by name from the generated
 * program's byte buffer without importing the generated DynamicFieldAccessor. The generated {@code
 * DynamicFieldAccessor} implements this interface (BMS projects only).
 *
 * <p>Field names are COBOL names (e.g. {@code "FNAMEO"}, {@code "CUST-NAME OF CUSTMAPO"}).
 */
public interface FieldStore {

    /** Read a field value as String (space/zero padded per COBOL semantics). */
    String getString(String cobolName);

    /**
     * Raw field bytes as a String WITHOUT numeric de-coding — for COBOL class tests (IS NUMERIC /
     * ALPHABETIC). Unlike {@link #getString} (which decodes a numeric field, normalizing
     * binary-zero/space bytes to '0'), this returns the literal stored bytes so an uninitialized
     * DISPLAY numeric field is correctly seen as non-numeric.
     */
    String getRawString(String cobolName);

    /** Write a String value into a field (truncated/padded to field width). */
    void setString(String cobolName, String value);

    /** True if the field name resolves to a buffer slice. */
    boolean has(String cobolName);

    /**
     * Independent point-in-time copy of this store's entire backing bytes. Used to snapshot a
     * record/working-storage for a TS/TD queue (WRITEQ TS FROM(area)), so later mutations of the
     * live store do not bleed into the queued item.
     */
    byte[] snapshot();

    /**
     * Replace this store's backing bytes with a previously taken {@link #snapshot()} (READQ TS/TD
     * INTO).
     */
    void restore(byte[] snapshot);
}

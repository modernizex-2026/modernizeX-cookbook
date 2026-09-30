package com.generated.orion.common.infrastructure.layout;

import java.math.BigDecimal;

/**
 * Typed CRUD interface for a single buffer holding fields keyed by COBOL name.
 *
 * <p>Two implementations:
 *
 * <ul>
 *   <li>{@link RecordBuffer} — single-root, used for FD records (file scope, contiguous byte[]).
 *   <li>{@link WorkingStorageBuffer} — multi-root, used for WORKING-STORAGE (1 byte[] per
 *       01-level).
 * </ul>
 *
 * <p>Method {@code recordByteLength()} is intentionally absent — FD-only concern (file record
 * size), does not apply to the WS multi-buffer model. Caller must know the buffer type to call it
 * (LSP-safe).
 */
public interface FieldBuffer {

    int getInt(String name);

    long getLong(String name);

    String getString(String name);

    BigDecimal getDecimal(String name);

    double getDouble(String name);

    boolean getBoolean(String name);

    String groupToString(String name);

    /**
     * Raw field bytes as a String WITHOUT numeric de-coding — for COBOL class tests (IS NUMERIC /
     * ALPHABETIC). Unlike getString (which decodes a numeric field to its value, normalizing
     * 0x00/0x20 → '0'), this returns the literal stored bytes, so an uninitialized DISPLAY numeric
     * (binary zero) is correctly non-numeric.
     */
    String getRawString(String name);

    void setInt(String name, int v);

    void setLong(String name, long v);

    void setString(String name, String v);

    void setDecimal(String name, BigDecimal v);

    void setDouble(String name, double v);

    void setBoolean(String name, boolean v);

    void setGroup(String name, String v);

    /* ── OCCURS-aware overloads ───────────────────────────────────────────
     * Default impl ignores subs (safe for non-OCCURS fields and for buffers
     * without OCCURS support). RecordBuffer overrides these to apply
     * 1-based COBOL subscripts against ancestor OCCURS groups.
     * subs == empty → identical to no-sub form.                            */
    default int getInt(String name, int... subs) {
        return getInt(name);
    }

    default long getLong(String name, int... subs) {
        return getLong(name);
    }

    default String getString(String name, int... subs) {
        return getString(name);
    }

    default BigDecimal getDecimal(String name, int... subs) {
        return getDecimal(name);
    }

    default double getDouble(String name, int... subs) {
        return getDouble(name);
    }

    default boolean getBoolean(String name, int... subs) {
        return getBoolean(name);
    }

    default String groupToString(String name, int... subs) {
        return groupToString(name);
    }

    default void setInt(String name, int v, int... subs) {
        setInt(name, v);
    }

    default void setLong(String name, long v, int... subs) {
        setLong(name, v);
    }

    default void setString(String name, String v, int... subs) {
        setString(name, v);
    }

    default void setDecimal(String name, BigDecimal v, int... subs) {
        setDecimal(name, v);
    }

    default void setDouble(String name, double v, int... subs) {
        setDouble(name, v);
    }

    default void setBoolean(String name, boolean v, int... subs) {
        setBoolean(name, v);
    }

    default void setGroup(String name, String v, int... subs) {
        setGroup(name, v);
    }

    /** Returns true if {@code name} is a field/group resolvable in this buffer. */
    boolean contains(String name);

    /** Reset the whole buffer (numeric → '0', alphanumeric → space). */
    void clear();

    /* ── Byte-level group operations ────────────────────────────────── */

    /** Return a byte[] copy of the named field/group's bytes. */
    byte[] sliceBytes(String name);

    /** Write {@code src} bytes into the named field/group (byte-copy, no padding). */
    void writeBytes(String name, byte[] src);

    /** Byte-level group MOVE: copy bytes from one field/group to another within this buffer. */
    void copyBytes(String toName, String fromName);

    /* ── Whole-buffer snapshot/restore (TS/TD queue copy semantics) ───────
     *
     * Capture/replace the entire backing byte content of this buffer. Used by the
     * runtime to take an independent point-in-time copy of a record/working-storage
     * (e.g. WRITEQ TS FROM(area)) and to write a stored snapshot back (READQ TS INTO). */

    /** Return an independent copy of this buffer's entire backing bytes. */
    byte[] toBytes();

    /**
     * Replace this buffer's backing bytes with {@code src} (length must match this buffer's size).
     */
    void fromBytes(byte[] src);

    /* ── Byte-level figurative constant checks ───────────────────────── */

    /** True if every byte of the field/group = 0xFF (HIGH-VALUES). */
    boolean isAllHighValues(String name);

    /** True if every byte of the field/group = 0x00 (LOW-VALUES). */
    boolean isAllLowValues(String name);

    /** True if every byte of the field/group = 0x20 (SPACES). */
    boolean isAllSpaces(String name);

    /** True if every byte of the field/group = '0' (0x30, ZEROS for DISPLAY numeric). */
    boolean isAllZeros(String name);

    /* ── Byte-level figurative-constant fills ─────────────────────────
     *
     * MOVE HIGH-VALUES / LOW-VALUES must write raw sentinel bytes (0xFF / 0x00)
     * directly to the field, bypassing the charset encoder.                  */
    /** Fill every byte of the field/group with 0xFF (MOVE HIGH-VALUES). */
    void fillHighValues(String name);

    /** Fill every byte of the field/group with 0x00 (MOVE LOW-VALUES). */
    void fillLowValues(String name);
}

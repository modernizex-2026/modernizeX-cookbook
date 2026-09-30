package com.sakura.runtime.record;

import java.math.BigDecimal;

/**
 * Typed read/write contract over field storage addressed by COBOL data name.
 *
 * <p>Implemented twice:
 *
 * <ul>
 *   <li>{@link RecordImage} — a single contiguous byte[] with one root; backs FD records.
 *   <li>{@link StorageImage} — a family of buffers, one per 01-level; backs WORKING-STORAGE.
 * </ul>
 *
 * <p>{@code recordByteLength()} is left out of this contract on purpose: only a file record has a
 * fixed length, so callers must hold the concrete type to ask for it.
 */
public interface FieldStore {

    int getInt(String name);

    long getLong(String name);

    String getString(String name);

    BigDecimal getDecimal(String name);

    double getDouble(String name);

    boolean getBoolean(String name);

    String groupToString(String name);

    void setInt(String name, int v);

    void setLong(String name, long v);

    void setString(String name, String v);

    void setDecimal(String name, BigDecimal v);

    void setDouble(String name, double v);

    void setBoolean(String name, boolean v);

    void setGroup(String name, String v);

    /* ── Subscripted variants ─────────────────────────────────────────────
     * The defaults drop the subscripts — correct for fields without OCCURS
     * and for stores that do not model arrays. RecordImage overrides them,
     * applying the 1-based COBOL subscripts against the enclosing OCCURS
     * groups. Passing no subscripts behaves like the plain form.           */
    default int getInt(String name, int... subs) {
        return getInt(name);
    }

    /**
     * Strict numeric read — COBOL comparison semantics. A DISPLAY numeric field holding spaces or
     * garbage raises {@link NumericValueException} (COB010 data-exception parity) instead of
     * coercing to zero. Stores without raw-byte access fall back to the lenient read.
     */
    default int getIntStrict(String name) {
        return getInt(name);
    }

    default long getLongStrict(String name) {
        return getLong(name);
    }

    default BigDecimal getDecimalStrict(String name) {
        return getDecimal(name);
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

    /** Whether {@code name} resolves to a field or group in this store. */
    boolean contains(String name);

    /**
     * Formats a field through its COBOL editing picture — zero suppression and insertion characters
     * applied: PIC Z,ZZZ,ZZ9 holding 5 yields " 5"; PIC 9999/99/99 holding 20260716 yields
     * "2026/07/16". The default hands back the raw string, which suits un-edited fields;
     * RecordImage consults the field's edit metadata via {@code EditPictureFormatter}.
     */
    default String editedDisplay(String name) {
        return getString(name);
    }

    /** Reinitializes every field: numerics to '0', alphanumerics to spaces. */
    void clear();

    /* ── Raw byte operations ────────────────────────────────────────── */

    /** Copies out the raw bytes of the named field or group. */
    byte[] sliceBytes(String name);

    /** Stores {@code src} verbatim into the named field or group — no padding, no conversion. */
    void writeBytes(String name, byte[] src);

    /**
     * Slice narrowed by subscripts: when {@code subs} addresses an OCCURS group only that element
     * is copied; without subscripts, the whole node. The default discards the subscripts so
     * array-unaware stores keep compiling; RecordImage overrides. Required by cross-01 {@code MOVE
     * G(a) TO T}.
     */
    default byte[] sliceBytes(String name, int[] subs) {
        return sliceBytes(name);
    }

    /**
     * Write narrowed by subscripts: targets the single addressed OCCURS element. The default falls
     * back to the whole-node write; RecordImage overrides. Required by cross-01 {@code MOVE T TO
     * G(b)}.
     */
    default void writeBytes(String name, int[] subs, byte[] src) {
        writeBytes(name, src);
    }

    /** Raw group MOVE inside this store: the bytes of {@code fromName} land in {@code toName}. */
    void copyBytes(String toName, String fromName);

    /**
     * Group MOVE where either side may be an OCCURS element — COBOL {@code MOVE G(a) TO G(b)} on an
     * array group. Each name is resolved under its own subscripts so precisely one element is
     * copied. The default ignores the subscripts (whole-node copy) for array-unaware stores;
     * RecordImage overrides.
     */
    default void copyBytes(String toName, String fromName, int[] toSubs, int[] fromSubs) {
        copyBytes(toName, fromName);
    }

    /* ── Figurative-constant tests ───────────────────────────────────── */

    /** Whether the field/group is entirely 0xFF bytes (HIGH-VALUES). */
    boolean isAllHighValues(String name);

    /** Whether the field/group is entirely 0x00 bytes (LOW-VALUES). */
    boolean isAllLowValues(String name);

    /** Whether the field/group is entirely 0x20 bytes (SPACES). */
    boolean isAllSpaces(String name);

    /** Whether the field/group is entirely '0' bytes (0x30 — ZEROS of DISPLAY numerics). */
    boolean isAllZeros(String name);

    /* ── Raw sentinel fills ────────────────────────────────────────────
     *
     * MOVE HIGH-VALUES / LOW-VALUES writes the sentinel bytes straight into
     * storage; the charset encoder never sees them.                        */
    /** Floods the field/group with 0xFF (MOVE HIGH-VALUES). */
    void fillHighValues(String name);

    /** Floods the field/group with 0x00 (MOVE LOW-VALUES). */
    void fillLowValues(String name);
}

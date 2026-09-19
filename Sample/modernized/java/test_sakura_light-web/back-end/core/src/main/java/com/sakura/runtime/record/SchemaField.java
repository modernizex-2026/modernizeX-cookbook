package com.sakura.runtime.record;

/**
 * Leaf schema entry describing one elementary field. Every attribute — offset, length, usage, sign,
 * scale, editing metadata — arrives pre-computed from the record-schema XML; nothing is re-derived
 * from PIC strings at runtime.
 */
public final class SchemaField extends SchemaNode {

    public enum Kind {
        NUM,
        ALPHANUMERIC,
        DBCS
    }

    /**
     * Storage representations (COBOL USAGE): DISPLAY — one character byte per digit, sign as
     * overpunch COMP3 — packed decimal, two digits per byte plus a sign nibble BINARY — big-endian
     * two's-complement integer of fixed width COMP1 — 4-byte IEEE 754 float, big-endian COMP2 —
     * 8-byte IEEE 754 double, big-endian
     */
    public enum Usage {
        DISPLAY,
        COMP3,
        BINARY,
        COMP1,
        COMP2
    }

    public enum EditFormat {
        NONE,
        ZERO_SUPPRESS,
        SIGN_SUPPRESS,
        INSERTION
    }

    private final Kind kind;
    private final Usage usage;
    private final boolean signed;
    private final int scale; // digits right of the implied decimal point (V)
    private final String javaType;
    private final String initialValue;
    // editing-picture metadata
    private final EditFormat editFormat;
    private final int editIntegerDigits;
    private final boolean editInsertCommas;
    private final boolean editHasTrailingNine;
    private final String
            editSlashAfter; // digit positions a '/' follows, CSV (PIC ZZZZ/ZZ -> "4"); empty = no
    // slashes
    private final String
            editCommaAfter; // digit positions a ',' follows, CSV (PIC ----,---,--9 -> "4,7"); empty

    // = none

    SchemaField(
            String name,
            String redefinesName,
            int byteOffset,
            int byteLength,
            Kind kind,
            Usage usage,
            boolean signed,
            int scale,
            String javaType,
            String initialValue,
            EditFormat editFormat,
            int editIntegerDigits,
            boolean editInsertCommas,
            boolean editHasTrailingNine,
            String editSlashAfter,
            String editCommaAfter) {
        this.name = name;
        this.redefinesName = redefinesName;
        this.byteOffset = byteOffset;
        this.byteLength = byteLength;
        this.kind = kind;
        this.usage = usage;
        this.signed = signed;
        this.scale = scale;
        this.javaType = javaType;
        this.initialValue = initialValue;
        this.editFormat = editFormat == null ? EditFormat.NONE : editFormat;
        this.editIntegerDigits = editIntegerDigits;
        this.editInsertCommas = editInsertCommas;
        this.editHasTrailingNine = editHasTrailingNine;
        this.editSlashAfter = editSlashAfter == null ? "" : editSlashAfter;
        this.editCommaAfter = editCommaAfter == null ? "" : editCommaAfter;
    }

    /** Convenience constructor for fields that carry no editing metadata. */
    SchemaField(
            String name,
            String redefinesName,
            int byteOffset,
            int byteLength,
            Kind kind,
            Usage usage,
            boolean signed,
            int scale,
            String javaType,
            String initialValue) {
        this(
                name,
                redefinesName,
                byteOffset,
                byteLength,
                kind,
                usage,
                signed,
                scale,
                javaType,
                initialValue,
                EditFormat.NONE,
                0,
                false,
                false,
                "",
                "");
    }

    public Kind getKind() {
        return kind;
    }

    public Usage getUsage() {
        return usage;
    }

    public boolean isSigned() {
        return signed;
    }

    public int getScale() {
        return scale;
    }

    public String getJavaType() {
        return javaType;
    }

    public String getInitialValue() {
        return initialValue;
    }

    public EditFormat getEditFormat() {
        return editFormat;
    }

    public boolean isEdited() {
        return editFormat != EditFormat.NONE;
    }

    public int getEditIntegerDigits() {
        return editIntegerDigits;
    }

    public boolean isEditInsertCommas() {
        return editInsertCommas;
    }

    public boolean isEditHasTrailingNine() {
        return editHasTrailingNine;
    }

    /**
     * Digit positions (CSV) that a '/' follows — PIC ZZZZ/ZZ gives "4", ZZZZ/ZZ/ZZ gives "4,6";
     * empty = no slashes.
     */
    public String getEditSlashAfter() {
        return editSlashAfter;
    }

    /**
     * Digit positions (CSV) that a ',' follows — PIC ----,---,--9 gives "4,7"; empty = plain
     * 3-digit grouping.
     */
    public String getEditCommaAfter() {
        return editCommaAfter;
    }

    public boolean isNumeric() {
        return kind == Kind.NUM;
    }

    public boolean isDecimal() {
        return scale > 0;
    }

    /**
     * Digits left of the decimal point, computed from byte length and usage: DISPLAY holds one
     * digit per byte; COMP-3 holds two per byte minus the sign nibble; BINARY maps 2/4/8 bytes to
     * the standard 4/9/18-digit widths.
     */
    public int getIntegerDigits() {
        int totalDigits;
        switch (usage) {
            case COMP3:
                totalDigits = byteLength * 2 - 1;
                break;
            case BINARY:
                if (byteLength <= 2) {
                    totalDigits = 4;
                } else if (byteLength <= 4) totalDigits = 9;
                else totalDigits = 18;
                break;
            case COMP1:
                // a float carries about 7 significant decimal digits
                totalDigits = 7;
                break;
            case COMP2:
                // a double carries about 15 significant decimal digits
                totalDigits = 15;
                break;
            case DISPLAY:
            default:
                totalDigits = byteLength;
        }
        return Math.max(0, totalDigits - scale);
    }

    public int getFractionDigits() {
        return scale;
    }

    @Override
    public String toString() {
        return "Field{"
                + name
                + " "
                + javaType
                + " offset="
                + byteOffset
                + " size="
                + byteLength
                + " kind="
                + kind
                + " usage="
                + usage
                + (signed ? " signed" : "")
                + (scale > 0 ? " scale=" + scale : "")
                + (isRedefines() ? " REDEFINES " + redefinesName : "")
                + "}";
    }
}

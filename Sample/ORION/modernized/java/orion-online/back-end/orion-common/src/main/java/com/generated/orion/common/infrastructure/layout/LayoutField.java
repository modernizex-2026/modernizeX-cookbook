package com.generated.orion.common.infrastructure.layout;

/**
 * Leaf node: a single schema-driven field. The PIC concept (regex parsing) has been removed. All
 * metadata comes directly from XML layout v2 attributes.
 */
public final class LayoutField extends SchemaNode {

    public enum Kind {
        NUM,
        ALPHANUMERIC,
        DBCS
    }

    /**
     * USAGE families: DISPLAY = ASCII digit/overpunch (1 byte per digit) COMP3 = packed BCD (2
     * digits per byte + sign nibble) BINARY = fixed-width 2's-complement integer (big-endian) COMP1
     * = IEEE 754 single-precision float (4 bytes, big-endian) COMP2 = IEEE 754 double-precision
     * double (8 bytes, big-endian)
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
        SIGN_SUPPRESS
    }

    private final Kind kind;
    private final Usage usage;
    private final boolean signed;
    private final int scale; // fractionDigits (V9 implied decimal)
    private final String javaType;
    private final String initialValue;
    // COBOL edit-picture. editPicture is the authoritative display spec (raw PIC) — the
    // formatter is picture-driven. editFormat + the flags are descriptive metadata.
    private final String editPicture;
    private final EditFormat editFormat;
    private final int editIntegerDigits;
    private final boolean editInsertCommas;
    private final boolean editHasTrailingNine;
    private final String
            editSlashAfter; // CSV digit-positions after which a '/' is inserted (PIC ZZZZ/ZZ ->

    // "4"); "" = none

    LayoutField(
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
            String editPicture,
            EditFormat editFormat,
            int editIntegerDigits,
            boolean editInsertCommas,
            boolean editHasTrailingNine,
            String editSlashAfter) {
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
        this.editPicture = (editPicture == null || editPicture.isEmpty()) ? null : editPicture;
        this.editFormat = editFormat == null ? EditFormat.NONE : editFormat;
        this.editIntegerDigits = editIntegerDigits;
        this.editInsertCommas = editInsertCommas;
        this.editHasTrailingNine = editHasTrailingNine;
        this.editSlashAfter = editSlashAfter == null ? "" : editSlashAfter;
    }

    /** Back-compat constructor — defaults to no editing. */
    LayoutField(
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
                null,
                EditFormat.NONE,
                0,
                false,
                false,
                "");
    }

    private boolean justifiedRight; // COBOL JUSTIFIED RIGHT — set by the loader post-construction

    public Kind getKind() {
        return kind;
    }

    public Usage getUsage() {
        return usage;
    }

    public boolean isSigned() {
        return signed;
    }

    public boolean isJustifiedRight() {
        return justifiedRight;
    }

    public void setJustifiedRight(boolean j) {
        this.justifiedRight = j;
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

    public String getEditPicture() {
        return editPicture;
    }

    public EditFormat getEditFormat() {
        return editFormat;
    }

    /** A field is "edited" iff it carries a display PICTURE (the formatting source of truth). */
    public boolean isEdited() {
        return editPicture != null;
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
     * CSV of digit-positions after which a '/' is inserted (PIC ZZZZ/ZZ -> "4", ZZZZ/ZZ/ZZ ->
     * "4,6"); "" = none.
     */
    public String getEditSlashAfter() {
        return editSlashAfter;
    }

    public boolean isNumeric() {
        return kind == Kind.NUM;
    }

    public boolean isDecimal() {
        return scale > 0;
    }

    /**
     * Total decimal digits storable, derived from byteLength + usage: DISPLAY = 1 digit/byte,
     * COMP-3 packs 2 digits/byte (less 1 sign nibble), BINARY = SMALLINT(2B)/INTEGER(4B)/BIGINT(8B)
     * standard widths.
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
                // IEEE 754 single ~ 7 significant decimal digits
                totalDigits = 7;
                break;
            case COMP2:
                // IEEE 754 double ~ 15 significant decimal digits
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

package com.sakura.runtime.record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Composite schema entry: a COBOL group with its nested groups/fields plus the OCCURS count and
 * per-element size. Under REDEFINES flavor B it additionally records the discriminator and the
 * values that select each variant.
 */
public final class SchemaGroup extends SchemaNode {

    final List<SchemaNode> children = new ArrayList<>();
    private int occurs = 1;
    private int elementSize = 0;

    /**
     * Flavor-B dispatch: field whose value picks a variant (held by the group owning the variants).
     */
    private String discriminatorField;

    /** Flavor-B dispatch: discriminator values under which this group is the active variant. */
    private List<String> variantWhen;

    SchemaGroup(String name, String redefinesName) {
        this.name = name;
        this.redefinesName = redefinesName;
    }

    public List<SchemaNode> children() {
        return Collections.unmodifiableList(children);
    }

    public int getOccurs() {
        return occurs;
    }

    void setOccurs(int occurs) {
        this.occurs = occurs;
    }

    public int getElementSize() {
        return elementSize;
    }

    void setElementSize(int elementSize) {
        this.elementSize = elementSize;
    }

    public boolean isArray() {
        return occurs > 1;
    }

    public String getDiscriminatorField() {
        return discriminatorField;
    }

    void setDiscriminatorField(String f) {
        this.discriminatorField = f;
    }

    public List<String> getVariantWhen() {
        return variantWhen == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(variantWhen);
    }

    void setVariantWhen(List<String> vs) {
        this.variantWhen = vs;
    }

    /** Whether this group is one of the selectable variants of a flavor-B dispatcher. */
    public boolean isVariant() {
        return variantWhen != null && !variantWhen.isEmpty();
    }

    /** Whether this group selects among variants through a discriminator field. */
    public boolean isDispatcher() {
        return discriminatorField != null;
    }

    /** Whether {@code value} is among the discriminator values activating this group. */
    public boolean matchesVariant(String value) {
        if (variantWhen == null || value == null) {
            return false;
        }
        for (String v : variantWhen) {
            if (v.equals(value)) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Group{"
                + name
                + " offset="
                + byteOffset
                + " size="
                + byteLength
                + (occurs > 1 ? " occurs=" + occurs + " elemSize=" + elementSize : "")
                + (isRedefines() ? " REDEFINES " + redefinesName : "")
                + (isDispatcher() ? " DISPATCHER(" + discriminatorField + ")" : "")
                + (isVariant() ? " VARIANT=" + variantWhen : "")
                + " children="
                + children.size()
                + "}";
    }
}

package com.sakura.runtime.record;

/**
 * Base type for every entry of a parsed record schema: a {@link SchemaField} leaf carrying a PIC
 * clause, or a {@link SchemaGroup} holding nested entries.
 */
public abstract class SchemaNode {

    String name;
    String redefinesName;
    SchemaGroup parent;
    int byteOffset;
    int byteLength;

    public String getName() {
        return name;
    }

    public String getRedefinesName() {
        return redefinesName;
    }

    public boolean isRedefines() {
        return redefinesName != null;
    }

    public SchemaGroup getParent() {
        return parent;
    }

    public int getByteOffset() {
        return byteOffset;
    }

    public int getByteLength() {
        return byteLength;
    }
}

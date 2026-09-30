package com.appruntime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Explicit field mapping between BMS field names and Java symbolic map field names, with role
 * classification. Eliminates suffix-based name-pattern detection in MapBinder.
 *
 * <p>The generator produces this mapping authoritatively — the runtime trusts it, does not inspect
 * field name suffixes to guess roles.
 *
 * <p>Roles:
 *
 * <ul>
 *   <li>{@code dataIn} — input data fields (BMS name → Java field name)
 *   <li>{@code dataOut} — output data fields (BMS name → Java field name)
 *   <li>{@code attr} — attribute fields (BMS name → Java field name)
 *   <li>{@code length} — length/cursor fields (BMS name → Java field name)
 * </ul>
 */
public class FieldMapping {

    private final Map<String, String> dataInFields;
    private final Map<String, String> dataOutFields;
    private final Map<String, String> attrFields;
    private final Map<String, String> lengthFields;

    private FieldMapping(Builder builder) {
        this.dataInFields = Collections.unmodifiableMap(builder.dataInFields);
        this.dataOutFields = Collections.unmodifiableMap(builder.dataOutFields);
        this.attrFields = Collections.unmodifiableMap(builder.attrFields);
        this.lengthFields = Collections.unmodifiableMap(builder.lengthFields);
    }

    /** BMS name → Java field name for input data fields. */
    public Map<String, String> getDataInFields() {
        return dataInFields;
    }

    /** BMS name → Java field name for output data fields. */
    public Map<String, String> getDataOutFields() {
        return dataOutFields;
    }

    /** BMS name → Java field name for attribute fields. */
    public Map<String, String> getAttrFields() {
        return attrFields;
    }

    /** BMS name → Java field name for length/cursor fields. */
    public Map<String, String> getLengthFields() {
        return lengthFields;
    }

    /** Empty mapping — no fields. */
    public static FieldMapping empty() {
        return new Builder().build();
    }

    public boolean isEmpty() {
        return dataInFields.isEmpty()
                && dataOutFields.isEmpty()
                && attrFields.isEmpty()
                && lengthFields.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, String> dataInFields = new HashMap<>();
        private final Map<String, String> dataOutFields = new HashMap<>();
        private final Map<String, String> attrFields = new HashMap<>();
        private final Map<String, String> lengthFields = new HashMap<>();

        public Builder addDataIn(String bmsName, String javaFieldName) {
            dataInFields.put(bmsName, javaFieldName);
            return this;
        }

        public Builder addDataOut(String bmsName, String javaFieldName) {
            dataOutFields.put(bmsName, javaFieldName);
            return this;
        }

        public Builder addAttr(String bmsName, String javaFieldName) {
            attrFields.put(bmsName, javaFieldName);
            return this;
        }

        public Builder addLength(String bmsName, String javaFieldName) {
            lengthFields.put(bmsName, javaFieldName);
            return this;
        }

        public FieldMapping build() {
            return new FieldMapping(this);
        }
    }
}

package com.appruntime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Reflection-based binder between terminal I/O fields and symbolic map objects.
 *
 * <p>Uses explicit {@link FieldMapping} (from AppProgram.getFieldMapping()) to bridge between BMS
 * field names (as used in HTML, e.g., "CUSTID") and COBOL symbolic map Java field names (e.g.,
 * "ccustidi", "ccustido").
 *
 * <p>Field role is determined by the FieldMapping's role-classified maps (dataIn, dataOut, attr,
 * length) — the generator produces these authoritatively. No suffix inspection or name-pattern
 * heuristics are used.
 *
 * <p>If fieldMapping is empty or doesn't cover a field, that field is skipped (not guessed).
 */
public class MapBinder {

    private static final Logger log = LoggerFactory.getLogger(MapBinder.class);

    private MapBinder() {
        // Utility helper class
    }

    /**
     * Bind terminal input fields to a symbolic map input object using reflection. Uses
     * fieldMapping's dataIn and length maps to find the correct Java field for each BMS name.
     *
     * @param inputFields BMS field name -> value from terminal (e.g., "CUSTID" -> "123")
     * @param mapInput the symbolic map input object to populate
     * @param fieldMapping explicit role-classified field mapping from AppProgram
     */
    public static void bind(
            Map<String, String> inputFields, Object mapInput, FieldMapping fieldMapping) {
        if (inputFields == null || mapInput == null) {
            return;
        }
        if (fieldMapping == null || fieldMapping.isEmpty()) {
            log.warn(
                    "bind() called without fieldMapping — no fields will be bound. "
                            + "Ensure AppProgram.getFieldMapping() returns a complete mapping.");
            return;
        }

        // FieldMapping already provides BMS name → Java field name, indexed by role
        Map<String, String> bmsToDataField = fieldMapping.getDataInFields();
        Map<String, String> bmsToLengthField = fieldMapping.getLengthFields();

        // Byte model (Option C): the map object is a FieldStore — read/write COBOL fields by name
        // from the byte buffer instead of reflecting on POJO fields (which the byte Fields class
        // does not have). FieldMapping stores lowercase data-field names; buffer keys are
        // uppercase.
        if (mapInput instanceof FieldStore fs) {
            for (Map.Entry<String, String> entry : inputFields.entrySet()) {
                String bmsName = entry.getKey();
                String value = entry.getValue();
                String dataField = bmsToDataField.get(bmsName);
                if (dataField != null && fs.has(dataField.toUpperCase())) {
                    // Empty submitted field → COBOL SPACES (CICS RECEIVE MAP yields x'40').
                    fs.setString(
                            dataField.toUpperCase(),
                            (value == null || value.isEmpty()) ? " " : value);
                }
                String lengthField = bmsToLengthField.get(bmsName);
                if (lengthField != null && fs.has(lengthField.toUpperCase())) {
                    fs.setString(
                            lengthField.toUpperCase(),
                            String.valueOf(value != null ? value.length() : 0));
                }
            }
            return;
        }

        Class<?> clazz = mapInput.getClass();
        for (Map.Entry<String, String> entry : inputFields.entrySet()) {
            String bmsName = entry.getKey();
            String value = entry.getValue();

            // Find data field using explicit mapping
            String dataFieldName = bmsToDataField.get(bmsName);
            if (dataFieldName != null) {
                Field dataField = findField(clazz, dataFieldName);
                if (dataField != null) {
                    setFieldValue(dataField, mapInput, value);
                }
            }

            // Set length field using explicit mapping
            String lengthFieldName = bmsToLengthField.get(bmsName);
            if (lengthFieldName != null) {
                Field lengthField = findField(clazz, lengthFieldName);
                if (lengthField != null) {
                    setLengthField(lengthField, mapInput, value != null ? value.length() : 0);
                }
            }
        }
    }

    /**
     * Extract output data field values from a symbolic map output object into a flat map. Uses
     * fieldMapping's dataOut map to produce correct BMS field names as keys.
     *
     * @param mapOutput the symbolic map output object
     * @param fieldMapping explicit role-classified field mapping from AppProgram
     * @return map of BMS field name -> value
     */
    public static Map<String, Object> extract(Object mapOutput, FieldMapping fieldMapping) {
        Map<String, Object> result = new HashMap<>();
        if (mapOutput == null) {
            return result;
        }
        if (fieldMapping == null || fieldMapping.isEmpty()) {
            log.warn("extract() called without fieldMapping — no fields will be extracted.");
            return result;
        }

        // Byte model (Option C): read output fields by COBOL name from the byte buffer.
        if (mapOutput instanceof FieldStore fs) {
            for (Map.Entry<String, String> entry : fieldMapping.getDataOutFields().entrySet()) {
                String key = entry.getValue().toUpperCase();
                if (!fs.has(key)) {
                    continue;
                }
                String s = fs.getString(key);
                if (s != null) {
                    s = stripNul(s);
                    // COBOL SPACES (all-blank) = field not set by program → skip (let *I fill it).
                    if (!s.isBlank()) {
                        result.put(entry.getKey(), s);
                    }
                }
            }
            return result;
        }

        // Iterate over dataOut entries: BMS name → Java field name
        for (Map.Entry<String, String> entry : fieldMapping.getDataOutFields().entrySet()) {
            String bmsName = entry.getKey();
            String javaFieldName = entry.getValue();
            Field field = findField(mapOutput.getClass(), javaFieldName);
            if (field == null) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(mapOutput);
                if (value != null) {
                    // BMS LOW-VALUES (\u0000) means "don't change this field on screen".
                    // In HTTP (stateless), strip null chars — they have no screen to preserve.
                    // Also skip space-only strings: COBOL SPACES (PIC X default = ' ') means
                    // "field not set by program" — don't let it block meaningful *I data from
                    // extractFromInput() via putIfAbsent.
                    if (value instanceof String) {
                        String s = ((String) value).replace("\u0000", "");
                        if (!s.isBlank()) {
                            result.put(bmsName, s);
                        }
                    } else {
                        // Non-string POJO field (rare — the byte model uses the FieldStore branch
                        // above). Numeric display formatting is owned by the byte-model edit engine
                        // (NumericEditFormatter), the single source of truth — not duplicated here.
                        result.put(bmsName, value);
                    }
                }
            } catch (IllegalAccessException e) {
                log.debug("Cannot access field '{}': {}", javaFieldName, e.getMessage());
            }
        }
        return result;
    }

    /**
     * Extract data fields from the INPUT map object using dataIn mapping. COBOL BMS REDEFINES
     * overlays I/O memory — generated code may write data to *I fields (e.g.,
     * cousr0ai.setFname01i()) while SEND MAP expects data from *O fields. This method reads *I
     * fields as a fallback for the I/O buffer mismatch.
     */
    public static Map<String, Object> extractFromInput(Object mapInput, FieldMapping fieldMapping) {
        Map<String, Object> result = new HashMap<>();
        if (mapInput == null || fieldMapping == null || fieldMapping.isEmpty()) {
            return result;
        }
        // Byte model (Option C): read *I fields by COBOL name from the byte buffer.
        if (mapInput instanceof FieldStore fs) {
            for (Map.Entry<String, String> entry : fieldMapping.getDataInFields().entrySet()) {
                String key = entry.getValue().toUpperCase();
                if (!fs.has(key)) {
                    continue;
                }
                String s = stripNul(fs.getString(key));
                if (s != null && !s.isBlank()) {
                    result.put(entry.getKey(), s);
                }
            }
            return result;
        }
        for (Map.Entry<String, String> entry : fieldMapping.getDataInFields().entrySet()) {
            String bmsName = entry.getKey();
            String javaFieldName = entry.getValue();
            Field field = findField(mapInput.getClass(), javaFieldName);
            if (field == null) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(mapInput);
                if (value != null) {
                    if (value instanceof String) {
                        String s = ((String) value).replace("\u0000", "");
                        // Consistent with extract(): isBlank() skips COBOL SPACES default
                        if (!s.isBlank()) {
                            result.put(bmsName, s);
                        }
                    } else {
                        result.put(bmsName, value);
                    }
                }
            } catch (IllegalAccessException e) {
                log.debug("Cannot access input field '{}': {}", javaFieldName, e.getMessage());
            }
        }
        return result;
    }

    /**
     * Extract attribute fields from the map output object. Uses fieldMapping's attr map for correct
     * BMS name resolution.
     *
     * @param mapOutput the symbolic map output object
     * @param fieldMapping explicit role-classified field mapping from AppProgram
     * @return map of BMS field name -> CSS class
     */
    public static Map<String, String> extractAttrs(Object mapOutput, FieldMapping fieldMapping) {
        Map<String, String> result = new HashMap<>();
        if (mapOutput == null) {
            return result;
        }
        if (fieldMapping == null || fieldMapping.isEmpty()) {
            return result;
        }

        // Byte model (Option C): read attribute bytes by COBOL name from the byte buffer.
        if (mapOutput instanceof FieldStore fs) {
            for (Map.Entry<String, String> entry : fieldMapping.getAttrFields().entrySet()) {
                String key = entry.getValue().toUpperCase();
                if (!fs.has(key)) {
                    continue;
                }
                String s = stripNul(fs.getString(key));
                if (s != null && !s.isEmpty()) {
                    String css = mapAttrToCss(s);
                    if (!css.isEmpty()) {
                        result.put(entry.getKey(), css);
                    }
                }
            }
            return result;
        }

        // Iterate over attr entries: BMS name → Java field name
        for (Map.Entry<String, String> entry : fieldMapping.getAttrFields().entrySet()) {
            String bmsName = entry.getKey();
            String javaFieldName = entry.getValue();
            Field field = findField(mapOutput.getClass(), javaFieldName);
            if (field == null) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(mapOutput);
                if (value != null && !value.toString().isEmpty()) {
                    result.put(bmsName, mapAttrToCss(value.toString()));
                }
            } catch (IllegalAccessException e) {
                log.debug("Cannot access attr field '{}': {}", javaFieldName, e.getMessage());
            }
        }
        return result;
    }

    /**
     * Extract cursor position indicators from length fields. Uses fieldMapping's length map for
     * correct BMS name resolution.
     *
     * @param mapOutput the symbolic map output object
     * @param fieldMapping explicit role-classified field mapping from AppProgram
     * @return map of BMS field name -> cursor indicator (-1 means cursor here)
     */
    public static Map<String, Integer> extractCursors(Object mapOutput, FieldMapping fieldMapping) {
        Map<String, Integer> result = new HashMap<>();
        if (mapOutput == null) {
            return result;
        }
        if (fieldMapping == null || fieldMapping.isEmpty()) {
            return result;
        }

        // Byte model (Option C): a length field holding -1 marks the cursor position.
        if (mapOutput instanceof FieldStore fs) {
            for (Map.Entry<String, String> entry : fieldMapping.getLengthFields().entrySet()) {
                String key = entry.getValue().toUpperCase();
                if (!fs.has(key)) {
                    continue;
                }
                String s = fs.getString(key);
                if (s == null) {
                    continue;
                }
                try {
                    if (Integer.parseInt(s.trim()) == -1) {
                        result.put(entry.getKey(), -1);
                    }
                } catch (NumberFormatException ignored) {
                    // non-numeric length field — no cursor indicator
                }
            }
            return result;
        }

        // Iterate over length entries: BMS name → Java field name
        for (Map.Entry<String, String> entry : fieldMapping.getLengthFields().entrySet()) {
            String bmsName = entry.getKey();
            String javaFieldName = entry.getValue();
            Field field = findField(mapOutput.getClass(), javaFieldName);
            if (field == null) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(mapOutput);
                if (value instanceof Number) {
                    int intValue = ((Number) value).intValue();
                    if (intValue == -1) {
                        result.put(bmsName, intValue);
                    }
                }
            } catch (IllegalAccessException e) {
                log.debug("Cannot access length field '{}': {}", javaFieldName, e.getMessage());
            }
        }
        return result;
    }

    // --- Private helpers ---

    /** Strip COBOL LOW-VALUES (null bytes) from a byte-buffer string read. */
    private static String stripNul(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != 0) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Find a declared field by exact name. No case-insensitive fallback — the generator must
     * produce consistent field names.
     */
    private static Field findField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            log.debug(
                    "Field '{}' not found in class '{}' — check generator field naming consistency",
                    name,
                    clazz.getSimpleName());
            return null;
        }
    }

    private static void setLengthField(Field field, Object target, int length) {
        field.setAccessible(true);
        try {
            Class<?> type = field.getType();
            if (type == short.class || type == Short.class) {
                field.set(target, (short) length);
            } else if (type == int.class || type == Integer.class) {
                field.set(target, length);
            } else if (type == long.class || type == Long.class) {
                field.set(target, (long) length);
            }
        } catch (IllegalAccessException e) {
            log.debug("Cannot set length field '{}': {}", field.getName(), e.getMessage());
        }
    }

    private static void setFieldValue(Field field, Object target, String value) {
        field.setAccessible(true);
        try {
            Class<?> type = field.getType();
            if (type == String.class) {
                // COBOL semantics: a terminal field submitted as empty means the user
                // cleared it — CICS RECEIVE MAP yields SPACES (x'40'), not empty string.
                // Map "" → " " so that generated COBOL conditions (e.g., NOT = SPACES)
                // behave correctly.
                String cobolValue = (value == null || value.isEmpty()) ? " " : value;
                field.set(target, cobolValue);
            } else if (type == int.class || type == Integer.class) {
                field.set(target, Integer.parseInt(value));
            } else if (type == long.class || type == Long.class) {
                field.set(target, Long.parseLong(value));
            } else if (type == short.class || type == Short.class) {
                field.set(target, Short.parseShort(value));
            } else {
                field.set(target, value);
            }
        } catch (IllegalAccessException | NumberFormatException e) {
            log.debug("Cannot set field '{}' to '{}': {}", field.getName(), value, e.getMessage());
        }
    }

    /**
     * Map BMS attribute byte value to CSS class. DFHBMSCA constants are single-byte values (X'F2' =
     * red, X'F4' = green, etc.) Generator may store as hex string "X'F4'" or actual char value.
     */
    private static String mapAttrToCss(String attrValue) {
        if (attrValue == null || attrValue.isBlank()) {
            return "";
        }
        String v = attrValue.trim();

        // Handle COBOL hex literal format: X'F4' → extract hex byte
        if (v.startsWith("X'") && v.endsWith("'") && v.length() == 5) {
            try {
                int b = Integer.parseInt(v.substring(2, 4), 16);
                return mapAttrByteToCss(b);
            } catch (NumberFormatException ignored) {
            }
        }

        // Handle single character (actual byte value from COBOL field)
        if (v.length() == 1) {
            return mapAttrByteToCss(v.charAt(0));
        }

        // Handle named attributes (from BMS map definition)
        return switch (v.toUpperCase()) {
            case "BRT", "BRIGHT" -> "attr-bright";
            case "DRK", "DARK" -> "attr-dark";
            case "ASKIP" -> "attr-askip";
            case "PROT" -> "attr-protected";
            case "UNPROT" -> "attr-unprotected";
            case "NUM" -> "attr-numeric";
            default -> "";
        };
    }

    /** Map DFHBMSCA byte value to CSS class */
    private static String mapAttrByteToCss(int b) {
        return switch (b) {
                // Colors (DFHBMSCA color constants)
            case 0xF1 -> "attr-blue";
            case 0xF2 -> "attr-red";
            case 0xF3 -> "attr-pink";
            case 0xF4 -> "attr-green";
            case 0xF5 -> "attr-turquoise";
            case 0xF6 -> "attr-yellow";
            case 0xF7 -> "attr-neutral";
                // Attributes
            case 0xF8 -> "attr-bright";
            case 0x0C -> "attr-dark";
            case 0xF0 -> "attr-protected";
            case 0x40 -> "attr-unprotected";
            case 0x20 -> "attr-askip";
            case 0xC1 -> "attr-fset";
            default -> "";
        };
    }
}

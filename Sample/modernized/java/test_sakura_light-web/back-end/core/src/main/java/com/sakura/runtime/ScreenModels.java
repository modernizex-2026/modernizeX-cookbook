package com.sakura.runtime;

import java.util.*;

/**
 * Shared data models for COBOL SCREEN SECTION rendering. Contains screen definitions, field
 * metadata, and PICTURE formatting. Used by both ConsoleScreenRenderer and WebSocketScreenRenderer.
 */
public class ScreenModels {

    // ── Screen definition model ──

    public static class ScreenDef {
        public final String name;
        public final boolean clearScreen;
        public final java.util.List<ScreenRow> rows = new java.util.ArrayList<>();
        public final java.util.List<String> inputFieldNames = new java.util.ArrayList<>();
        public final java.util.List<InputFieldDef> inputFieldDefs = new java.util.ArrayList<>();

        /**
         * Inclusive clear-region row range (1-based). Both 0 = no region clear. Set by COBOL `CLEAR
         * DATA TO N` directive on a screen at LINE K → clears rows K..N.
         */
        public int clearLineFrom;

        public int clearLineTo;

        /**
         * Record schema for list/OCCURS-style screens. Null for non-list screens. Renderer
         * auto-extracts one record per displayScreen() call via RuntimeFieldAccess and flushes as
         * `displayRecordList` WS frame on screen transition.
         */
        public RecordSchemaDef recordSchema;

        public ScreenDef(String name, boolean clearScreen) {
            this.name = name;
            this.clearScreen = clearScreen;
        }

        public ScreenDef setClearRegion(int fromLine, int toLine) {
            this.clearLineFrom = fromLine;
            this.clearLineTo = toLine;
            return this;
        }

        public ScreenRow addRow(int line) {
            ScreenRow row = new ScreenRow(line);
            rows.add(row);
            return row;
        }

        /**
         * Row whose line number is resolved at render time from a runtime field. COBOL screens like
         * SC-DISP-GREEN use `screenLine="L-CNT"` (variable), where L-CNT is updated each iteration
         * of a PERFORM VARYING loop.
         */
        public ScreenRow addDynamicRow(String lineFieldName) {
            return addDynamicRow(lineFieldName, 0);
        }

        /**
         * Sibling sub-row variant — line = ws.get(lineFieldName) + lineOffset. COBOL `LINE L-CNT
         * PLUS 1` second sub-row uses offset 1, third uses 2, etc.
         */
        public ScreenRow addDynamicRow(String lineFieldName, int lineOffset) {
            ScreenRow row = new ScreenRow(0);
            row.dynamicLineField = lineFieldName;
            row.lineOffset = lineOffset;
            rows.add(row);
            return row;
        }

        public ScreenDef withInputField(String fieldName) {
            inputFieldNames.add(fieldName);
            return this;
        }

        public ScreenDef withInputFieldDef(InputFieldDef def) {
            inputFieldNames.add(def.name);
            inputFieldDefs.add(def);
            return this;
        }

        /**
         * Add position data for an already-registered input field (call from static initializer).
         */
        public ScreenDef addInputFieldDef(InputFieldDef def) {
            inputFieldDefs.add(def);
            return this;
        }

        public ScreenDef setRecordSchema(RecordSchemaDef schema) {
            this.recordSchema = schema;
            return this;
        }
    }

    /**
     * Schema describing the per-row record shape for a list/OCCURS screen. Populated from
     * manifest.json by ScreenManifestParser; consumed by RecordAccumulator.
     */
    public static class RecordSchemaDef {
        public final String recordName;
        public final java.util.List<RecordFieldDef> fields = new java.util.ArrayList<>();

        public RecordSchemaDef(String recordName) {
            this.recordName = recordName;
        }
    }

    /** Substring modifier (COBOL `(start:length)`). */
    public static class Substring {
        public final int from; // 1-based start
        public final int length;

        public Substring(int from, int length) {
            this.from = from;
            this.length = length;
        }
    }

    public static class RecordFieldDef {
        public final String key; // JSON property name (e.g. 'sinseino')
        public final String sourceField; // COBOL bare name (e.g. 'KB-SINSEINO')
        public final String
                kind; // 'rowIndex' | 'scalar' | 'indexed' | 'indexedLiteral' | 'fixedArray'
        public String indexVar; // for 'indexed' — runtime subscript variable
        public Integer literalIndex; // for 'indexedLiteral'
        public int[] indexes; // for 'fixedArray'
        public Substring substring;
        public String picture;
        public String type; // TS type tag — 'int' | 'string' | 'string[]'

        public RecordFieldDef(String key, String sourceField, String kind) {
            this.key = key;
            this.sourceField = sourceField;
            this.kind = kind;
        }
    }

    public static class ScreenRow {
        public final int line;

        /** Non-null when row line is resolved at runtime from a COBOL variable. */
        public String dynamicLineField;

        /**
         * Added to resolved dynamicLineField value — recovers COBOL `LINE name PLUS N` offset that
         * the external COBOL→XML preprocessor drops. 0 for the first sub-row, 1 for `LINE name PLUS
         * 1`, etc. Ignored when dynamicLineField is null.
         */
        public int lineOffset;

        public final java.util.List<ScreenField> fields = new java.util.ArrayList<>();

        public ScreenRow(int line) {
            this.line = line;
        }

        public ScreenRow literal(int col, String text) {
            fields.add(ScreenField.literal(col, text));
            return this;
        }

        public ScreenRow literalReverse(int col, String text) {
            ScreenField f = ScreenField.literal(col, text);
            f.reverse = true;
            fields.add(f);
            return this;
        }

        public ScreenRow fromField(int col, String fieldName, String picture, String color) {
            fields.add(ScreenField.fromField(col, fieldName, picture, color));
            return this;
        }

        public ScreenRow overLine(int fromCol, int toCol) {
            ScreenField f = new ScreenField();
            f.col = fromCol;
            f.overLineTo = toCol;
            fields.add(f);
            return this;
        }

        public ScreenRow underLine(int fromCol, int toCol) {
            ScreenField f = new ScreenField();
            f.col = fromCol;
            f.underLineTo = toCol;
            fields.add(f);
            return this;
        }

        public ScreenRow vLines(int... cols) {
            ScreenField f = new ScreenField();
            f.verticalLineAt = cols;
            fields.add(f);
            return this;
        }

        /** Set blink attribute on the last added field. */
        public ScreenRow withBlink() {
            if (!fields.isEmpty()) {
                fields.get(fields.size() - 1).blink = true;
            }
            return this;
        }

        /** Set lowlight attribute on the last added field. */
        public ScreenRow withLowlight() {
            if (!fields.isEmpty()) {
                fields.get(fields.size() - 1).lowlight = true;
            }
            return this;
        }
    }

    public static class ScreenField {
        public int col;
        public String type = "";
        public String text;
        public String fieldName;
        public String picture;
        public String color;
        public boolean reverse;
        public boolean underline;
        public boolean highlight;
        public boolean blink;
        public boolean lowlight;
        public int overLineTo;
        public int underLineTo;
        public int[] verticalLineAt;

        public static ScreenField literal(int col, String text) {
            ScreenField f = new ScreenField();
            f.col = col;
            f.type = "literal";
            f.text = text;
            return f;
        }

        public static ScreenField fromField(
                int col, String fieldName, String picture, String color) {
            ScreenField f = new ScreenField();
            f.col = col;
            f.type = "from";
            f.fieldName = fieldName;
            f.picture = picture;
            f.color = color;
            return f;
        }
    }

    public static class InputFieldDef {
        public final String name;
        public final int line;
        public final int col;
        public final int width;
        public final String picture;
        public final String usingField;

        /**
         * ESTS code that the COBOL EVALUATE following this ACCEPT treats as 'CONTINUE/advance'.
         * Populated post-gen by JavaProjectGenerator's advance-ESTS analyzer from the generated
         * service EVALUATE/switch blocks. Used by WebSocketScreenRenderer.mapAidKey to translate
         * plain Enter (aidKey '00') to the field-specific advance code.
         */
        public String advanceEsts;

        public InputFieldDef(
                String name, int line, int col, int width, String picture, String usingField) {
            this.name = name;
            this.line = line;
            this.col = col;
            this.width = width;
            this.picture = picture;
            this.usingField = usingField;
        }

        public InputFieldDef setAdvanceEsts(String code) {
            this.advanceEsts = code;
            return this;
        }
    }

    public static class WindowDef {
        public final String name;
        public final int height;
        public final int width;
        public final int displayLine;
        public final int displayCol;
        public final boolean border;

        public WindowDef(
                String name,
                int height,
                int width,
                int displayLine,
                int displayCol,
                boolean border) {
            this.name = name;
            this.height = height;
            this.width = width;
            this.displayLine = displayLine;
            this.displayCol = displayCol;
            this.border = border;
        }

        @Override
        public String toString() {
            return "WindowDef{name='" + name + "', height=" + height + ", width=" + width + "}";
        }
    }

    // ── PICTURE formatting (numeric-edited / alpha display) ──

    public static String formatPicture(String value, String picture) {
        if (value == null) {
            value = "";
        }
        if (picture == null || picture.isEmpty()) {
            return value;
        }
        String pic = picture.toUpperCase();
        // Alphanumeric (X/N/A): left-aligned, space-padded.
        if (pic.startsWith("X") || pic.startsWith("N") || pic.startsWith("A")) {
            return padRight(value, extractPicLength(pic));
        }
        // Plain numeric (only S/9/V/digits/parens, no edit chars): zero-padded.
        if (pic.matches("[S9V0-9()]+")) {
            return padLeft(value.replaceAll("[^0-9-]", ""), extractPicLength(pic), '0');
        }
        // Numeric-edited (Z / comma / decimal / B / trailing sign): full COBOL
        // editing via the shared PictureFormatter engine (byte-verified vs GnuCOBOL).
        return PictureFormatter.edit(value, picture);
    }

    private static int extractPicLength(String pic) {
        java.util.regex.Matcher m =
                java.util.regex.Pattern.compile("[XNA9SZ]\\((\\d+)\\)").matcher(pic);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        int count = 0;
        for (char c : pic.toCharArray()) {
            if (c == '9' || c == 'X' || c == 'N' || c == 'A' || c == 'Z' || c == '-') {
                count++;
            }
        }
        return count > 0 ? count : pic.length();
    }

    private static String padRight(String s, int len) {
        if (s.length() >= len) {
            return s.substring(0, len);
        }
        return s + " ".repeat(len - s.length());
    }

    private static String padLeft(String s, int len, char pad) {
        if (s.length() >= len) {
            return s.substring(s.length() - len);
        }
        return String.valueOf(pad).repeat(len - s.length()) + s;
    }

    public static String[] coerceInputToPicture(String value, String picture, int width) {
        if (value == null) {
            value = "";
        }
        if (picture == null) return new String[] {value, "0"};
        String pic = picture.toUpperCase().trim();
        boolean signed = pic.startsWith("S9");
        boolean numeric = signed || pic.startsWith("9");
        if (!numeric) return new String[] {value, "0"};
        // Signed PIC (S9...) allows a leading '-' for negative entry. Track it
        // separately from the digit scan so it isn't silently dropped like any
        // other non-digit character (else -2000 would coerce to 2000).
        boolean neg = signed && value.indexOf('-') >= 0;
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c >= '\uFF10' && c <= '\uFF19')
                c = (char) (c - 0xFEE0); // full-width JP digit → half-width
            if (c >= '0' && c <= '9') {
                digits.append(c);
            }
        }
        int stripped = value.length() - digits.length() - (neg ? 1 : 0);
        if (stripped < 0) {
            stripped = 0;
        }
        if (value.trim().isEmpty()) stripped = 0; // empty input → padded zeros, not a strip
        String result = digits.toString();
        if (width > 0 && result.length() > width) {
            result = result.substring(result.length() - width); // keep least-significant
        }
        if (width > 0) {
            result = padLeft(result, width, '0');
        }
        if (neg) {
            result = "-" + result;
        }
        return new String[] {result, String.valueOf(stripped)};
    }

    // ── Field reference resolution (subscripts + reference modification) ──

    /**
     * Resolve a COBOL field reference with optional subscripts and reference modification. Returns
     * the formatted value, or empty string on lookup failure.
     */
    public static String resolveFieldRef(
            com.sakura.runtime.record.RuntimeFieldAccess fields, String fieldRef) {
        if (fieldRef == null || fieldRef.isEmpty()) {
            return "";
        }
        // Fast path: no parens → bare field name.
        int firstParen = fieldRef.indexOf('(');
        if (firstParen < 0) {
            try {
                return String.valueOf(fields.getString(fieldRef));
            } catch (Exception e) {
                return "";
            }
        }
        String base = fieldRef.substring(0, firstParen).trim();
        java.util.List<int[]> rangeSlices = new java.util.ArrayList<>();
        java.util.List<Integer> subs = new java.util.ArrayList<>();
        int i = firstParen;
        while (i < fieldRef.length()) {
            if (fieldRef.charAt(i) != '(') {
                i++;
                continue;
            }
            int close = fieldRef.indexOf(')', i);
            if (close < 0) {
                break;
            }
            String inner = fieldRef.substring(i + 1, close).trim();
            int colon = inner.indexOf(':');
            if (colon >= 0) {
                // Reference modification: START:LEN (1-based start)
                int start = parseIntOrLookup(fields, inner.substring(0, colon).trim());
                int length = parseIntOrLookup(fields, inner.substring(colon + 1).trim());
                rangeSlices.add(new int[] {start, length});
            } else {
                // Subscript(s) — comma- AND whitespace-separated: the manifest emits
                // space-form like `NAME(2 IDX1)` for 2D OCCURS, so a comma-only split
                // would collapse every 2D-OCCURS FROM-field to subscript [0].
                for (String part : inner.split("[,\\s]+")) {
                    String p = part.trim();
                    if (!p.isEmpty()) {
                        subs.add(parseIntOrLookup(fields, p));
                    }
                }
            }
            i = close + 1;
        }
        String value;
        try {
            if (subs.isEmpty()) {
                value = String.valueOf(fields.getString(base));
            } else {
                int[] arr = new int[subs.size()];
                for (int k = 0; k < arr.length; k++) {
                    arr[k] = subs.get(k);
                }
                value = String.valueOf(fields.getString(base, arr));
            }
        } catch (Exception e) {
            return "";
        }
        // Apply reference modification slice (1-based START, LEN chars).
        for (int[] slice : rangeSlices) {
            int start = Math.max(1, slice[0]) - 1;
            int length = Math.max(0, slice[1]);
            if (start >= value.length()) {
                value = "";
                break;
            }
            int end = Math.min(value.length(), start + length);
            value = value.substring(start, end);
        }
        return value;
    }

    /** Parse a subscript token: integer literal, or runtime variable resolved via the accessor. */
    private static int parseIntOrLookup(
            com.sakura.runtime.record.RuntimeFieldAccess fields, String token) {
        if (token == null || token.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException ignored) {
        }
        try {
            return fields.getInt(token);
        } catch (Exception e) {
            return 0;
        }
    }
}

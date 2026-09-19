package com.sakura.runtime;

import static java.math.BigDecimal.ZERO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** COBOL helper methods for runtime operations. Generated utility — do not edit manually. */
public final class Utility {
    private static final Logger log = LoggerFactory.getLogger(Utility.class);

    /** COBOL ACCEPT FROM DATE — 6-digit yyMMdd */
    public static final DateTimeFormatter FMT_YYMMDD = DateTimeFormatter.ofPattern("yyMMdd");

    /** COBOL ACCEPT FROM DATE — 8-digit yyyyMMdd */
    public static final DateTimeFormatter FMT_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 2-digit year component */
    public static final DateTimeFormatter FMT_YY = DateTimeFormatter.ofPattern("yy");

    /** 2-digit month component */
    public static final DateTimeFormatter FMT_MM = DateTimeFormatter.ofPattern("MM");

    /** 2-digit day component */
    public static final DateTimeFormatter FMT_DD = DateTimeFormatter.ofPattern("dd");

    /** COBOL ACCEPT FROM TIME — 6-digit HHmmss */
    public static final DateTimeFormatter FMT_HHMMSS = DateTimeFormatter.ofPattern("HHmmss");

    private static final ThreadLocal<String> STATION_NAME = new ThreadLocal<>();

    private static final ThreadLocal<Path> SCRATCH_DIR = new ThreadLocal<>();

    private static List<String> sysinLines;

    private static int sysinCursor;

    private static boolean sysinInit;

    private Utility() {}

    /** Parse COBOL numeric string to BigDecimal. Handles sign overpunch. */
    public static BigDecimal parseNumeric(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ZERO;
        }
        String norm = value.trim().toUpperCase();
        if (norm.equals("ZEROS") || norm.equals("ZEROES") || norm.equals("ZERO")) {
            return ZERO;
        }
        // Handle sign overpunch (EBCDIC)
        if (!norm.isEmpty()) {
            char last = norm.charAt(norm.length() - 1);
            if (last >= 'J' && last <= 'R') {
                int digit = last - 'J' + 1;
                norm = "-" + norm.substring(0, norm.length() - 1) + digit;
            } else if (last >= 'A' && last <= 'I') {
                int digit = last - 'A' + 1;
                norm = norm.substring(0, norm.length() - 1) + digit;
            } else if (last == '{') {
                norm = norm.substring(0, norm.length() - 1) + "0";
            } else if (last == '}') {
                norm = "-" + norm.substring(0, norm.length() - 1) + "0";
            }
        }
        try {
            return new BigDecimal(norm);
        } catch (NumberFormatException e) {
            return ZERO;
        }
    }

    /**
     * Safe Long.parseLong: returns {@code defaultVal} when value is null/blank/non-numeric. Falls
     * back to {@link #parseNumeric(String)} for COBOL overpunch/figurative semantics.
     */
    public static long parseLongOr(String value, long defaultVal) {
        if (value == null) {
            return defaultVal;
        }
        String s = value.trim();
        if (s.isEmpty()) {
            return defaultVal;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ignore) {
            // Fall back to parseNumeric for COBOL overpunch (J-R, A-I, {, }) and ZERO/SPACES
            try {
                return parseNumeric(s).longValue();
            } catch (RuntimeException ignore2) {
                return defaultVal;
            }
        }
    }

    /**
     * Safe Integer.parseInt: returns {@code defaultVal} when value is null/blank/non-numeric. Falls
     * back to {@link #parseNumeric(String)} for COBOL overpunch/figurative semantics.
     */
    public static int parseIntOr(String value, int defaultVal) {
        if (value == null) {
            return defaultVal;
        }
        String s = value.trim();
        if (s.isEmpty()) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ignore) {
            try {
                return parseNumeric(s).intValue();
            } catch (RuntimeException ignore2) {
                return defaultVal;
            }
        }
    }

    /**
     * Narrow a wide numeric to a COBOL int field of {@code digits} digits using COBOL decimal
     * truncation (keep low {@code digits} digits, sign-preserving), not a binary {@code (int)}
     * wrap.
     */
    public static int toCobolInt(long value, int digits) {
        if (digits <= 0 || digits > 9) {
            return (int) value;
        }
        long m = 1L;
        for (int i = 0; i < digits; i++) {
            m *= 10L;
        }
        return (int) (value % m);
    }

    /**
     * True iff every char of {@code value} is '0' (and value is non-empty). Used for COBOL
     * `&lt;field&gt; = ZERO` figurative comparison on String/group fields.
     */
    public static boolean isAllZeros(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) != '0') {
                return false;
            }
        }
        return true;
    }

    /**
     * True iff every char of {@code value} is a digit '0'-'9'. Implements COBOL `<field> IS
     * NUMERIC` class test for String fields.
     */
    public static boolean isNumeric(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * COBOL class-condition IS ALPHABETIC: every character is A-Z, a-z or space. Null -> false;
     * empty -> true (vacuous, COBOL fields are fixed-width).
     */
    public static boolean isAlphabetic(Object s) {
        if (s == null) {
            return false;
        }
        String v = s.toString();
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            if (c != ' ' && !(c >= 'A' && c <= 'Z') && !(c >= 'a' && c <= 'z')) {
                return false;
            }
        }
        return true;
    }

    /** COBOL IS ALPHABETIC-LOWER: every character is a-z or space. */
    public static boolean isAlphabeticLower(Object s) {
        if (s == null) {
            return false;
        }
        String v = s.toString();
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            if (c != ' ' && !(c >= 'a' && c <= 'z')) {
                return false;
            }
        }
        return true;
    }

    /** COBOL IS ALPHABETIC-UPPER: every character is A-Z or space. */
    public static boolean isAlphabeticUpper(Object s) {
        if (s == null) {
            return false;
        }
        String v = s.toString();
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            if (c != ' ' && !(c >= 'A' && c <= 'Z')) {
                return false;
            }
        }
        return true;
    }

    /** Convert a group (POJO or byte string) to its COBOL alphanumeric representation. */
    public static String groupToString(Object group) {
        if (group == null) {
            return "";
        }
        // RecordImage.toString() returns metadata, not bytes. CALL sub-program code
        // needs the serialized payload so parseIntoGroup can slice into linkage fields.
        if (group instanceof com.sakura.runtime.record.RecordImage rb) {
            return new String(rb.bytes(), rb.layout().charset());
        }
        if (group instanceof byte[] bytes) {
            return new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);
        }
        String result = group.toString();
        if (result.isEmpty()) {
            return result;
        }
        if (result.charAt(0) == '\u00ff' && result.chars().allMatch(c -> c == 0xff)) {
            return RuntimeConstants.HIGH_VALUES;
        }
        if (result.charAt(0) == '\u0000' && result.chars().allMatch(c -> c == 0)) {
            return RuntimeConstants.LOW_VALUES;
        }
        return result;
    }

    /**
     * COBOL alphanumeric equality: pad shorter to longer with space, then byte-compare. Mirror IBM
     * COBOL rule for non-numeric compare of two PIC X fields.
     */
    public static boolean fieldEquals(Object a, Object b) {
        String l = a == null ? "" : a.toString();
        String r = b == null ? "" : b.toString();
        if (r.length() == 1
                && (r.charAt(0) == '\u00FF' || r.charAt(0) == '\u0000')
                && !l.isEmpty()) {
            char sentinel = r.charAt(0);
            for (int i = 0; i < l.length(); i++) {
                if (l.charAt(i) != sentinel) {
                    return false;
                }
            }
            return true;
        }
        if (l.length() == 1
                && (l.charAt(0) == '\u00FF' || l.charAt(0) == '\u0000')
                && !r.isEmpty()) {
            char sentinel = l.charAt(0);
            for (int i = 0; i < r.length(); i++) {
                if (r.charAt(i) != sentinel) {
                    return false;
                }
            }
            return true;
        }
        // PIC N (DBCS) SPACE sentinel: full-width space U+3000 in a 1-char operand
        // matches a multi-char operand iff every char equals U+3000.
        if (r.length() == 1 && r.charAt(0) == '\u3000' && !l.isEmpty()) {
            for (int i = 0; i < l.length(); i++) {
                if (l.charAt(i) != '\u3000') {
                    return false;
                }
            }
            return true;
        }
        if (l.length() == 1 && l.charAt(0) == '\u3000' && !r.isEmpty()) {
            for (int i = 0; i < r.length(); i++) {
                if (r.charAt(i) != '\u3000') {
                    return false;
                }
            }
            return true;
        }
        // Length-normalize before byte compare: pad shorter side with full-width
        // space (U+3000) when either operand contains non-ASCII, else ASCII space.
        char padChar = ' ';
        for (int i = 0; i < l.length(); i++) {
            if (l.charAt(i) > 0x7F) {
                padChar = '\u3000';
                break;
            }
        }
        if (padChar == ' ') {
            for (int i = 0; i < r.length(); i++) {
                if (r.charAt(i) > 0x7F) {
                    padChar = '\u3000';
                    break;
                }
            }
        }
        int max = Math.max(l.length(), r.length());
        if (l.length() < max) {
            StringBuilder sb = new StringBuilder(l);
            while (sb.length() < max) {
                sb.append(padChar);
            }
            l = sb.toString();
        }
        if (r.length() < max) {
            StringBuilder sb = new StringBuilder(r);
            while (sb.length() < max) {
                sb.append(padChar);
            }
            r = sb.toString();
        }
        return l.equals(r);
    }

    /**
     * Strip trailing COBOL pad characters (space, ideographic space U+3000) so a Java switch
     * compares like COBOL pad-to-max alphanumeric equality.
     */
    public static String rtrim(Object s) {
        String v = s == null ? "" : s.toString();
        int end = v.length();
        while (end > 0 && (v.charAt(end - 1) == ' ' || v.charAt(end - 1) == '\u3000')) end--;
        return v.substring(0, end);
    }

    /**
     * COBOL MOVE <string> TO <group>: parse the source value into the dest POJO's leaf fields by
     * declaration order, sliced by each field's @FieldWidth or current String width. Handles
     * String, int/Integer, long/Long, BigDecimal, and nested groups.
     */
    public static void parseIntoGroup(Object dest, Object src) {
        if (dest == null) {
            return;
        }
        String value = (src == null) ? "" : src.toString();
        try {
            java.lang.reflect.Field[] fields = dest.getClass().getDeclaredFields();
            int pos = 0;
            for (java.lang.reflect.Field fld : fields) {
                if (java.lang.reflect.Modifier.isStatic(fld.getModifiers())) {
                    continue;
                }
                Class<?> ft = fld.getType();
                int width = 0;
                FieldWidth ann = fld.getAnnotation(FieldWidth.class);
                if (ann != null) {
                    width = ann.value();
                }
                if (width <= 0 && ft == String.class) {
                    String cap =
                            Character.toUpperCase(fld.getName().charAt(0))
                                    + fld.getName().substring(1);
                    try {
                        java.lang.reflect.Method get = dest.getClass().getMethod("get" + cap);
                        String cur = (String) get.invoke(dest);
                        width = (cur == null) ? 0 : cur.length();
                    } catch (NoSuchMethodException nsme) {
                        /* no getter — width stays 0 */
                    }
                }
                if (width <= 0 || pos >= value.length()) {
                    continue;
                }
                int end = Math.min(value.length(), pos + width);
                String slice = value.substring(pos, end);
                String cap =
                        Character.toUpperCase(fld.getName().charAt(0)) + fld.getName().substring(1);
                try {
                    if (ft == String.class) {
                        java.lang.reflect.Method set =
                                dest.getClass().getMethod("set" + cap, String.class);
                        set.invoke(dest, slice);
                    } else if (ft == int.class || ft == Integer.class) {
                        java.lang.reflect.Method set = dest.getClass().getMethod("set" + cap, ft);
                        set.invoke(dest, parseIntSafe(slice.trim()));
                    } else if (ft == long.class || ft == Long.class) {
                        java.lang.reflect.Method set = dest.getClass().getMethod("set" + cap, ft);
                        set.invoke(dest, parseLongSafe(slice.trim()));
                    } else if (ft == java.math.BigDecimal.class) {
                        java.lang.reflect.Method set =
                                dest.getClass().getMethod("set" + cap, java.math.BigDecimal.class);
                        String trimmed = slice.trim().replaceAll("[^0-9.+-]", "");
                        set.invoke(
                                dest,
                                trimmed.isEmpty()
                                        ? java.math.BigDecimal.ZERO
                                        : new java.math.BigDecimal(trimmed));
                    } else if (!ft.isPrimitive()
                            && !ft.isArray()
                            && !ft.getPackageName().startsWith("java.")) {
                        java.lang.reflect.Method get = dest.getClass().getMethod("get" + cap);
                        Object child = get.invoke(dest);
                        parseIntoGroup(child, slice);
                    }
                    pos = end;
                } catch (NoSuchMethodException nsme) {
                    pos = end;
                }
            }
        } catch (Exception ignore) {
            /* best-effort parse — swallow */
        }
    }

    private static int parseIntSafe(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '-' && sb.length() == 0) {
                    sb.append(c);
                } else if (c >= '0' && c <= '9') {
                    sb.append(c);
                }
            }
            if (sb.length() == 0 || (sb.length() == 1 && sb.charAt(0) == '-')) {
                return 0;
            }
            try {
                return Integer.parseInt(sb.toString());
            } catch (NumberFormatException e2) {
                return 0;
            }
        }
    }

    private static long parseLongSafe(String s) {
        if (s == null || s.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '-' && sb.length() == 0) {
                    sb.append(c);
                } else if (c >= '0' && c <= '9') {
                    sb.append(c);
                }
            }
            if (sb.length() == 0 || (sb.length() == 1 && sb.charAt(0) == '-')) {
                return 0L;
            }
            try {
                return Long.parseLong(sb.toString());
            } catch (NumberFormatException e2) {
                return 0L;
            }
        }
    }

    /** INITIALIZE REPLACING category BY value. */
    public static void initializeReplacingByCategory(Object target, String category, Object value) {
        if (target == null) {
            return;
        }
        if (target instanceof String || target instanceof Number || target instanceof Boolean) {
            return;
        }
        for (var field : target.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Class<?> type = field.getType();
                boolean match = false;
                if ("ALPHANUMERIC".equalsIgnoreCase(category) && type == String.class) {
                    match = true;
                } else if ("NUMERIC".equalsIgnoreCase(category)
                        && (type == int.class || type == long.class || type == BigDecimal.class)) {
                    match = true;
                }
                if (match) {
                    if (type == String.class) {
                        field.set(target, value.toString());
                    } else if (type == int.class) {
                        field.set(target, Integer.parseInt(value.toString()));
                    } else if (type == long.class) {
                        field.set(target, Long.parseLong(value.toString()));
                    } else if (type == BigDecimal.class) {
                        field.set(target, new BigDecimal(value.toString()));
                    }
                }
            } catch (IllegalAccessException e) {
                // skip;
            }
        }
    }

    /**
     * MOVE group TO group (same layout): copy matching-name fields between two POJOs. Nested
     * same-name groups are recursively DEEP-copied (target nested != source nested after the MOVE —
     * COBOL byte-copy semantics); a null dest nested group is instantiated. Leaf values convert
     * like an elementary MOVE (String/int/long/short/BigDecimal bridge).
     */
    public static void copyGroup(Object source, Object dest) {
        if (source == null || dest == null) {
            return;
        }
        for (java.lang.reflect.Field sf : source.getClass().getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(sf.getModifiers())) {
                continue;
            }
            try {
                java.lang.reflect.Field df = dest.getClass().getDeclaredField(sf.getName());
                sf.setAccessible(true);
                df.setAccessible(true);
                if (isGroupFieldType(sf.getType()) && isGroupFieldType(df.getType())) {
                    Object child = df.get(dest);
                    if (child == null) {
                        child = df.getType().getDeclaredConstructor().newInstance();
                        df.set(dest, child);
                    }
                    copyGroup(sf.get(source), child);
                } else {
                    copyLeafField(sf.get(source), df, dest);
                }
            } catch (NoSuchFieldException e) {
                // no same-named field in dest — skip (CORRESPONDING rule)
            } catch (ReflectiveOperationException e) {
                // best-effort — skip field
            }
        }
    }

    /**
     * MOVE CORRESPONDING from TO to: elementary items with matching names move with conversion;
     * matching-name subordinate groups recurse. Runtime contract identical to {@link #copyGroup}
     * (name-matched copy) — kept as its own entry point because the emitted statement names the
     * COBOL verb.
     */
    public static void copyCorresponding(Object from, Object to) {
        copyGroup(from, to);
    }

    /**
     * Cross-FD record MOVE: copy field-by-field BY ORDINAL POSITION between two classes sharing the
     * same byte layout but (possibly) different field names. Nested groups and OCCURS arrays
     * recurse; leaves convert like an elementary MOVE. Shape mismatches skip.
     */
    public static void copyRecordByteLayout(Object source, Object dest) {
        if (source == null || dest == null) {
            return;
        }
        java.lang.reflect.Field[] sfs = source.getClass().getDeclaredFields();
        java.lang.reflect.Field[] dfs = dest.getClass().getDeclaredFields();
        int i = 0;
        int j = 0;
        while (i < sfs.length && j < dfs.length) {
            if (java.lang.reflect.Modifier.isStatic(sfs[i].getModifiers())) {
                i++;
                continue;
            }
            if (java.lang.reflect.Modifier.isStatic(dfs[j].getModifiers())) {
                j++;
                continue;
            }
            java.lang.reflect.Field sf = sfs[i];
            java.lang.reflect.Field df = dfs[j];
            try {
                sf.setAccessible(true);
                df.setAccessible(true);
                if (sf.getType().isArray() && df.getType().isArray()) {
                    Object sa = sf.get(source);
                    Object da = df.get(dest);
                    int n =
                            (sa == null || da == null)
                                    ? 0
                                    : Math.min(
                                            java.lang.reflect.Array.getLength(sa),
                                            java.lang.reflect.Array.getLength(da));
                    for (int k = 0; k < n; k++) {
                        Object sv = java.lang.reflect.Array.get(sa, k);
                        if (isGroupFieldType(sf.getType().getComponentType())
                                && isGroupFieldType(df.getType().getComponentType())) {
                            Object dv = java.lang.reflect.Array.get(da, k);
                            if (dv == null) {
                                dv =
                                        df.getType()
                                                .getComponentType()
                                                .getDeclaredConstructor()
                                                .newInstance();
                                java.lang.reflect.Array.set(da, k, dv);
                            }
                            copyRecordByteLayout(sv, dv);
                        } else if (sv == null
                                || df.getType().getComponentType().isInstance(sv)
                                || df.getType().getComponentType().isPrimitive()) {
                            java.lang.reflect.Array.set(da, k, sv);
                        }
                    }
                } else if (isGroupFieldType(sf.getType()) && isGroupFieldType(df.getType())) {
                    Object child = df.get(dest);
                    if (child == null) {
                        child = df.getType().getDeclaredConstructor().newInstance();
                        df.set(dest, child);
                    }
                    copyRecordByteLayout(sf.get(source), child);
                } else {
                    copyLeafField(sf.get(source), df, dest);
                }
            } catch (ReflectiveOperationException e) {
                // best-effort — skip pair
            }
            i++;
            j++;
        }
    }

    /**
     * COBOL INITIALIZE-style defaults for a group POJO: String -> spaces of the field's @FieldWidth
     * (fallback: current value length; else empty), int/long/short -> 0, BigDecimal -> ZERO,
     * String[] elements -> empty, nested groups recurse.
     */
    public static void initializeGroup(Object target) {
        if (target == null) {
            return;
        }
        for (java.lang.reflect.Field fld : target.getClass().getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(fld.getModifiers())) {
                continue;
            }
            try {
                fld.setAccessible(true);
                Class<?> t = fld.getType();
                if (t == String.class) {
                    int width = 0;
                    for (java.lang.annotation.Annotation an : fld.getAnnotations()) {
                        if (an.annotationType().getSimpleName().equals("FieldWidth")) {
                            width = (Integer) an.annotationType().getMethod("value").invoke(an);
                        }
                    }
                    String cur = (String) fld.get(target);
                    if (width <= 0) {
                        width = cur == null ? 0 : cur.length();
                    }
                    fld.set(target, width <= 0 ? "" : " ".repeat(width));
                } else if (t == int.class) {
                    fld.setInt(target, 0);
                } else if (t == long.class) {
                    fld.setLong(target, 0L);
                } else if (t == short.class) {
                    fld.setShort(target, (short) 0);
                } else if (t == java.math.BigDecimal.class) {
                    fld.set(target, java.math.BigDecimal.ZERO);
                } else if (t == String[].class) {
                    String[] arr = (String[]) fld.get(target);
                    if (arr != null) {
                        java.util.Arrays.fill(arr, "");
                    }
                } else if (isGroupFieldType(t)) {
                    initializeGroup(fld.get(target));
                }
            } catch (ReflectiveOperationException e) {
                // best-effort — skip field
            }
        }
    }

    /** A generated COBOL group POJO: non-primitive, non-array, outside java.*. */
    private static boolean isGroupFieldType(Class<?> t) {
        return !t.isPrimitive()
                && !t.isArray()
                && t != String.class
                && t != java.math.BigDecimal.class
                && !t.getPackageName().startsWith("java.");
    }

    /**
     * Elementary MOVE into a reflective field: same-type assign, else convert via the
     * String/BigDecimal bridge (empty -> zero). Unconvertible values skip (best-effort).
     */
    private static void copyLeafField(Object value, Field df, Object dest) {
        try {
            Class<?> tt = df.getType();
            if (value == null) {
                if (!tt.isPrimitive()) {
                    df.set(dest, null);
                }
                return;
            }
            if (tt.isInstance(value)) {
                df.set(dest, value);
                return;
            }
            String s = value.toString().trim();
            if (tt == int.class || tt == Integer.class) {
                df.set(dest, s.isEmpty() ? 0 : new java.math.BigDecimal(s).intValue());
            } else if (tt == long.class || tt == Long.class) {
                df.set(dest, s.isEmpty() ? 0L : new java.math.BigDecimal(s).longValue());
            } else if (tt == short.class || tt == Short.class) {
                df.set(dest, s.isEmpty() ? (short) 0 : new java.math.BigDecimal(s).shortValue());
            } else if (tt == java.math.BigDecimal.class) {
                df.set(dest, s.isEmpty() ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(s));
            } else if (tt == String.class) {
                df.set(dest, value.toString());
            }
        } catch (IllegalAccessException | NumberFormatException e) {
            // best-effort — skip
        }
    }

    /** COBOL reference modification on String target: target(start:length) = newValue. */
    public static String setSubstring(String target, int start, int length, Object newValue) {
        String t = (target == null) ? "" : target;
        String nv = (newValue == null) ? "" : newValue.toString();
        int s = start - 1;
        if (s < 0) {
            s = 0;
        }
        if (t.length() < s + length) {
            StringBuilder pad = new StringBuilder(t);
            while (pad.length() < s + length) {
                pad.append(' ');
            }
            t = pad.toString();
        }
        boolean isNumeric = !nv.isEmpty() && nv.chars().allMatch(c -> c >= '0' && c <= '9');
        String padded;
        if (nv.length() >= length) {
            padded = nv.substring(0, length);
        } else if (isNumeric) {
            try {
                padded = String.format("%0" + length + "d", Long.parseLong(nv));
            } catch (NumberFormatException ignore) {
                // overflow (PIC 9(20+)) — manual zero-pad keeps full precision
                StringBuilder sb = new StringBuilder(length);
                for (int i = nv.length(); i < length; i++) {
                    sb.append('0');
                }
                sb.append(nv);
                padded = sb.toString();
            }
        } else {
            padded = String.format("%-" + length + "s", nv);
        }
        return t.substring(0, s) + padded + t.substring(s + length);
    }

    /**
     * DBCS reference modification: target(start:length) = newValue. Pads with full-width space
     * U+3000 to preserve 2-byte-per-char MS932 layout.
     */
    public static String setSubstringN(String target, int start, int length, Object newValue) {
        String t = (target == null) ? "" : target;
        String nv = (newValue == null) ? "" : newValue.toString();
        int s = start - 1;
        if (s < 0) {
            s = 0;
        }
        if (t.length() < s + length) {
            StringBuilder pad = new StringBuilder(t);
            while (pad.length() < s + length) {
                pad.append('\u3000');
            }
            t = pad.toString();
        }
        String padded;
        if (nv.length() >= length) {
            padded = nv.substring(0, length);
        } else {
            StringBuilder sb = new StringBuilder(nv);
            while (sb.length() < length) {
                sb.append('\u3000');
            }
            padded = sb.toString();
        }
        return t.substring(0, s) + padded + t.substring(s + length);
    }

    /**
     * Ensure string has at least minLength characters, padding with spaces. COBOL fields are always
     * padded to their PIC width; this emulates that.
     */
    public static String padRight(String value, int minLength) {
        String v = (value == null) ? "" : value;
        if (v.length() >= minLength) {
            return v;
        }
        return v + " ".repeat(Math.max(0, minLength - v.length()));
    }

    /**
     * Convert a long to fullwidth-digit String (zero-padded to charCount). Used for COBOL MOVE
     * numeric → PIC N(charCount). Output is a String of fullwidth digits `\uFF10..\uFF19`
     * ('０'..'９') — MS932 encodes each to a 2-byte DBCS pair, matching the field's byte layout.
     */
    public static String numericToWideDigits(long value, int srcDigits, int targetWidth) {
        String ascii = String.format("%0" + srcDigits + "d", value);
        StringBuilder sb = new StringBuilder(targetWidth);
        for (int i = 0; i < ascii.length(); i++) {
            char c = ascii.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.append((char) (0xFF10 + (c - '0')));
            } else if (c == '-') {
                sb.append('\uFF0D') /* fullwidth minus */;
            } else {
                sb.append(c);
            }
        }
        // COBOL MOVE numeric -> PIC N: LEFT-justify, pad with national space
        // (U+3000 ideographic space -> MS932 0x8140). srcDigits==targetWidth => zero-pad (old
        // behavior).
        while (sb.length() < targetWidth) {
            sb.append('\u3000');
        }
        return sb.toString();
    }

    public static String numericToWideDigits(long value, int charCount) {
        return numericToWideDigits(value, charCount, charCount);
    }

    /**
     * Count populated receivers from UNSTRING split result. Strips trailing empties so {@code
     * split("x",-1)} on "A|B|" reports 2 (not 3). Used as the increment for COBOL UNSTRING ...
     * TALLYING.
     */
    public static int unstringTally(String[] parts) {
        if (parts == null) {
            return 0;
        }
        int n = parts.length;
        while (n > 0 && (parts[n - 1] == null || parts[n - 1].isEmpty())) {
            n--;
        }
        return n;
    }

    /**
     * COBOL STRING ... INTO target WITH POINTER ptr. Writes source into target at 1-based position
     * ptr.
     */
    public static String stringWithPointer(String target, String source, int pointer) {
        if (target == null) {
            target = "";
        }
        if (source == null || source.isEmpty()) {
            return target;
        }
        int startIdx = pointer - 1;
        if (startIdx < 0) {
            startIdx = 0;
        }
        while (target.length() < startIdx + source.length()) target += " ";
        char[] chars = target.toCharArray();
        for (int i = 0; i < source.length() && startIdx + i < chars.length; i++) {
            chars[startIdx + i] = source.charAt(i);
        }
        return new String(chars);
    }

    /**
     * INSPECT ... TALLYING counter FOR ALL pattern: count non-overlapping occurrences of pattern.
     * Null/empty pattern or null value -> 0.
     */
    public static int tallyAll(String value, String pattern) {
        if (value == null || pattern == null || pattern.isEmpty()) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = value.indexOf(pattern, idx)) >= 0) {
            count++;
            idx += pattern.length();
        }
        return count;
    }

    /**
     * INSPECT ... TALLYING counter FOR LEADING pattern: count consecutive occurrences of pattern
     * anchored at position 1. Null/empty pattern -> 0.
     */
    public static int tallyLeading(String value, String pattern) {
        if (value == null || pattern == null || pattern.isEmpty()) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while (value.startsWith(pattern, idx)) {
            count++;
            idx += pattern.length();
        }
        return count;
    }

    /**
     * INSPECT ... REPLACING FIRST from BY to: literal (not regex) replace of the first occurrence
     * only. No occurrence / null from -> value unchanged.
     */
    public static String replaceFirst(String value, String from, String to) {
        if (value == null || from == null || from.isEmpty() || to == null) {
            return value;
        }
        int idx = value.indexOf(from);
        if (idx < 0) {
            return value;
        }
        return value.substring(0, idx) + to + value.substring(idx + from.length());
    }

    /**
     * INSPECT ... REPLACING LEADING from BY to: replace the run of consecutive occurrences of from
     * anchored at position 1 (each by to). COBOL requires equal lengths; this implementation
     * splices to generally.
     */
    public static String replaceLeading(String value, String from, String to) {
        if (value == null || from == null || from.isEmpty() || to == null) {
            return value;
        }
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        while (value.startsWith(from, idx)) {
            sb.append(to);
            idx += from.length();
        }
        return sb.append(value.substring(idx)).toString();
    }

    /**
     * INSPECT ... CONVERTING from TO to: positional character map — each char of value found at
     * position i in from becomes to.charAt(i). Chars outside the map pass through. Null args ->
     * value unchanged.
     */
    public static String inspectConverting(String value, String from, String to) {
        if (value == null || from == null || to == null) {
            return value;
        }
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int p = from.indexOf(chars[i]);
            if (p >= 0 && p < to.length()) {
                chars[i] = to.charAt(p);
            }
        }
        return new String(chars);
    }

    /** COBOL FUNCTION INTEGER-OF-DATE: YYYYMMDD → Lilian day number. */
    public static long integerOfDate(long yyyymmdd) {
        int y = (int) (yyyymmdd / 10000);
        int m = (int) ((yyyymmdd % 10000) / 100);
        int d = (int) (yyyymmdd % 100);
        return java.time.LocalDate.of(y, m, d).toEpochDay() + 141428;
    }

    /** COBOL FUNCTION DATE-OF-INTEGER: Lilian day → YYYYMMDD. */
    public static long dateOfInteger(long lilianDay) {
        java.time.LocalDate d = java.time.LocalDate.ofEpochDay(lilianDay - 141428);
        return (long) d.getYear() * 10000 + d.getMonthValue() * 100 + d.getDayOfMonth();
    }

    /** COBOL FUNCTION TEST-NUMVAL: 0 if value is a valid numeric, else >0. */
    public static int testNumval(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 1;
        }
        try {
            new BigDecimal(value.trim().replace(",", ""));
            return 0;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    /**
     * Map an IBM LE CEEDAYS numeric date picture to a java.time pattern. Uses 'u' (proleptic year)
     * so ResolverStyle.STRICT validates without an era.
     */
    private static String ceedaysToPattern(String picture) {
        String p = (picture == null ? "" : picture.trim().toUpperCase());
        return p.replace("YYYY", "uuuu")
                .replace("YY", "uu")
                .replace("ZM", "M")
                .replace("ZD", "d")
                .replace("DD", "dd");
    }

    /** Strictly parse a date string per an LE picture; null when the date is invalid. */
    private static LocalDate ceedaysParse(String date, String picture) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter f =
                    DateTimeFormatter.ofPattern(ceedaysToPattern(picture))
                            .withResolverStyle(ResolverStyle.STRICT);
            return LocalDate.parse(date.trim(), f);
        } catch (RuntimeException e) {
            // Invalid date value OR malformed picture → not a valid LE date
            return null;
        }
    }

    /** IBM LE CEEDAYS output: Lillian day number for the date, or 0 if invalid. */
    public static int ceedaysLillian(String date, String picture) {
        LocalDate d = ceedaysParse(date, picture);
        return d == null ? 0 : (int) (d.toEpochDay() + 141428);
    }

    /** IBM LE CEEDAYS feedback severity: 0 when the date is valid, 12 (severe) otherwise. */
    public static int ceedaysSeverity(String date, String picture) {
        return ceedaysParse(date, picture) == null ? 12 : 0;
    }

    /** Read a required environment variable. Logs and throws IllegalStateException if missing. */
    public static String requireEnvVar(Object name) {
        String n = String.valueOf(name).trim();
        String v = System.getenv(n);
        if (v == null) {
            log.error("Required environment variable '{}' is not set", n);
            throw new IllegalStateException("Missing required environment variable: " + n);
        }
        return v;
    }

    /** Bind the @WSNAME workstation name for the current (session) thread. */
    public static void setStationName(String station) {
        STATION_NAME.set(station);
    }

    /** Clear the thread-bound @WSNAME workstation name (call in a finally). */
    public static void clearStationName() {
        STATION_NAME.remove();
    }

    /**
     * Resolve the @WSNAME workstation name: thread-bound value, else the cobol.station.name system
     * property, else empty string.
     */
    public static String getStationName() {
        String s = STATION_NAME.get();
        if (s != null && !s.trim().isEmpty()) {
            return s.trim();
        }
        return System.getProperty("cobol.station.name", "");
    }

    /** Bind a per-session scratch directory for MSD file-mode I/O. */
    public static void setScratchDir(Path dir) {
        SCRATCH_DIR.set(dir);
    }

    /** Unbind the scratch directory (call in a finally). */
    public static void clearScratchDir() {
        SCRATCH_DIR.remove();
    }

    /** Resolve the per-session scratch dir; null when running outside a WS session. */
    public static Path getScratchDir() {
        return SCRATCH_DIR.get();
    }

    /**
     * Run the screen-input fallback cascade for one COBOL ACCEPT site. Order: {@code -DSC_<key>} →
     * {@code -D<key>} → {@code COB_<key>} env → {@code <key>} env → {@code screenFallback.get()}.
     * Returns the empty string when every level yields null/blank.
     *
     * @param fieldKey COBOL field name (uppercase, underscore-separated; e.g. {@code "TANTOCD"})
     * @param screenFallback supplier for the interactive-mode renderer call (may be null)
     */
    public static String acceptScreen(String fieldKey, Supplier<String> screenFallback) {
        String v = System.getProperty("SC_" + fieldKey);
        if (v == null) {
            v = System.getProperty(fieldKey);
        }
        if (v == null) {
            v = System.getenv("COB_" + fieldKey);
        }
        if (v == null) {
            v = System.getenv(fieldKey);
        }
        if (v == null && screenFallback != null) {
            v = screenFallback.get();
        }
        return (v == null || v.trim().isEmpty()) ? "" : v;
    }

    /**
     * Broadcast an end-status value to multiple ESTS-* fields via trySetString. Collapses the
     * unrolled N-line block emitted at every {@code renderer.readEndStatus()} site.
     *
     * @param ws the FieldAccessor (the program's *Fields subclass)
     * @param value the end-status value returned by renderer.readEndStatus()
     * @param fieldKeys the COBOL field names (e.g. "ESTS", "ESTS-TANTO", ...)
     */
    public static void broadcastEndStatus(Object ws, String value, String... fieldKeys) {
        if (ws == null || fieldKeys == null) {
            return;
        }
        final int[] emptySubs = new int[0];
        try {
            java.lang.reflect.Method m =
                    ws.getClass()
                            .getMethod("trySetString", String.class, String.class, int[].class);
            for (String key : fieldKeys) {
                if (key != null) {
                    m.invoke(ws, key, value, emptySubs);
                }
            }
        } catch (ReflectiveOperationException e) {
            log.error(
                    "broadcastEndStatus: trySetString not callable on {}: {}",
                    ws.getClass().getName(),
                    e.getMessage());
        }
    }

    /** Hitachi COB_SYSIN line reader: returns first whitespace-token of next line, or null. */
    public static synchronized String readStdinLine() {
        if (!sysinInit) {
            sysinInit = true;
            String path = System.getenv("COB_SYSIN");
            if (path != null && !path.trim().isEmpty()) {
                try {
                    Path p = Paths.get(path);
                    if (Files.exists(p)) {
                        sysinLines = new ArrayList<>();
                        try (BufferedReader r =
                                Files.newBufferedReader(p, Charset.forName("MS932"))) {
                            String l;
                            while ((l = r.readLine()) != null) {
                                sysinLines.add(l);
                            }
                        }
                    }
                } catch (IOException ignore) {
                    sysinLines = null;
                }
            }
        }
        if (sysinLines == null || sysinCursor >= sysinLines.size()) {
            return null;
        }
        String line = sysinLines.get(sysinCursor++);
        if (line == null) {
            return null;
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        int sp = trimmed.indexOf(' ');
        int tab = trimmed.indexOf('\t');
        if (tab >= 0 && (sp < 0 || tab < sp)) {
            sp = tab;
        }
        return sp < 0 ? trimmed : trimmed.substring(0, sp);
    }
}

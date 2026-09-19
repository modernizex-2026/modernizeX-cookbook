package com.sakura.runtime.record;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Field-level encoding and decoding driven by PIC and USAGE. Covers DISPLAY, COMP-3, BINARY, COMP-1
 * (IEEE 754 float) and COMP-2 (IEEE 754 double).
 *
 * <p>Byte order: COMP-1/COMP-2 read and write <b>big-endian</b> — the IBM mainframe / Hitachi VOS3
 * convention, consistent with BINARY. A little-endian port would require a byte swap that is
 * deliberately not implemented here.
 */
public record RecordConverter(RecordSchema layout) {

    /**
     * Numeric decoding is strict by default: a non-digit byte raises {@link NumericValueException}.
     * Setting {@code -Dcobol.numeric.lenient=true} opts into tolerating trailing garbage in a
     * numeric field by zero-filling it (the Hitachi-compatible behavior) — reserve it for
     * production data quirks that have been investigated and accepted.
     */
    private static final boolean LENIENT_NUMERIC =
            Boolean.parseBoolean(System.getProperty("cobol.numeric.lenient", "false"));

    /**
     * A stronger opt-in for DISPLAY fields whose content is genuinely non-numeric (sentinel rows
     * such as "BBBBB"): the value decodes instead of aborting, the way legacy COBOL tolerates
     * alphanumeric junk moved into a numeric item. Goes beyond {@link #LENIENT_NUMERIC}, which only
     * forgives trailing padding. Off by default; enable {@code -Dcobol.numeric.force0=true} solely
     * for known sentinel/garbage rows.
     */
    private static final boolean FORCE0_NUMERIC =
            Boolean.parseBoolean(System.getProperty("cobol.numeric.force0", "false"));

    public Map<String, Object> parse(byte[] rec) {
        if (rec.length < layout.recordByteLength()) {
            throw new IllegalArgumentException(
                    "Record has too few bytes: need "
                            + layout.recordByteLength()
                            + " got "
                            + rec.length);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (SchemaField f : layout.leaves()) {
            byte[] slice =
                    Arrays.copyOfRange(
                            rec, f.getByteOffset(), f.getByteOffset() + f.getByteLength());
            out.put(f.getName(), decodeField(f, slice, layout.charset()));
        }
        return out;
    }

    public byte[] serialize(Map<String, Object> values) {
        byte[] buf = new byte[layout.recordByteLength()];
        Arrays.fill(buf, (byte) 0x20);
        for (SchemaField f : layout.leaves()) {
            Object v = values.get(f.getName());
            if (v == null) {
                continue;
            }
            if (isInRedefinesGroup(f) && isDefaultValue(f, v)) {
                continue;
            }
            byte[] slice = encodeField(f, v, layout.charset());
            System.arraycopy(slice, 0, buf, f.getByteOffset(), f.getByteLength());
        }
        return buf;
    }

    private static boolean isInRedefinesGroup(SchemaField f) {
        SchemaNode n = f.isRedefines() ? f : f.getParent();
        while (n != null) {
            if (n.isRedefines()) {
                return true;
            }
            n = n.getParent();
        }
        return false;
    }

    private static boolean isDefaultValue(SchemaField pic, Object v) {
        if (v == null) {
            return true;
        }
        switch (pic.getKind()) {
            case NUM:
                if (v instanceof BigDecimal bd) {
                    return bd.signum() == 0;
                }
                if (v instanceof Number num) {
                    return num.longValue() == 0L;
                }
                return v.toString().trim().isEmpty();
            case ALPHANUMERIC, DBCS:
                return v.toString().trim().isEmpty();
            default:
                return false;
        }
    }

    /* ── decoding ───────────────────────────────────────────────────── */

    /** Decodes one field from its byte slice; public because {@link RecordImage} shares it. */
    public static Object decodeField(SchemaField pic, byte[] bytes, java.nio.charset.Charset cs) {
        switch (pic.getKind()) {
            case ALPHANUMERIC:
                return new String(bytes, cs);
            case DBCS:
                return new String(bytes, cs);
            case NUM:
                switch (pic.getUsage()) {
                    case COMP3:
                        return decodeComp3(pic, bytes);
                    case BINARY:
                        return decodeBinary(pic, bytes);
                    case COMP1:
                        return decodeComp1(pic, bytes);
                    case COMP2:
                        return decodeComp2(pic, bytes);
                    case DISPLAY:
                    default:
                        return decodeDisplay(pic, bytes);
                }
            default:
                throw new IllegalStateException("Kind not supported: " + pic.getKind());
        }
    }

    /**
     * Strict decode for COBOL numeric comparison semantics: DISPLAY numerics skip the
     * blank/NUL→zero coercion so broken data raises NumericValueException (COB010 parity). Every
     * other kind decodes exactly like {@link #decodeField}.
     */
    public static Object decodeFieldStrict(
            SchemaField pic, byte[] bytes, java.nio.charset.Charset cs) {
        if (pic.getKind() == SchemaField.Kind.NUM && pic.getUsage() == SchemaField.Usage.DISPLAY) {
            return decodeDisplay(pic, bytes, true);
        }
        return decodeField(pic, bytes, cs);
    }

    private static Object decodeDisplay(SchemaField pic, byte[] bytes) {
        return decodeDisplay(pic, bytes, false);
    }

    private static Object decodeDisplay(SchemaField pic, byte[] bytes, boolean strict) {
        boolean negative = false;
        char[] digits = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            if (!strict && (b == 0x20 || b == 0x00)) {
                digits[i] = '0';
                continue;
            } // lenient read: blanks and NULs count as zero digits
            if (b >= '0' && b <= '9') {
                digits[i] = (char) b;
                continue;
            }
            if (i == bytes.length - 1 && pic.isSigned()) {
                if (b == '{') {
                    digits[i] = '0';
                    continue;
                }
                if (b >= 'A' && b <= 'I') {
                    digits[i] = (char) ('0' + (b - 'A' + 1));
                    continue;
                }
                if (b == '}') {
                    digits[i] = '0';
                    negative = true;
                    continue;
                }
                if (b >= 'J' && b <= 'R') {
                    digits[i] = (char) ('0' + (b - 'J' + 1));
                    negative = true;
                    continue;
                }
                // not an overpunch either — handled below
            }
            if (strict) {
                throw new NumericValueException(
                        String.format(
                                "DISPLAY non-numeric byte 0x%02X at relative position %d (strict"
                                        + " numeric compare/arith)",
                                b, i),
                        pic.getName(),
                        pic.getByteOffset() + i);
            }
            // Padding-tolerant scan, active only under LENIENT_NUMERIC. The strict
            // default proceeds to the NumericValueException below so data problems
            // surface. In lenient mode the remainder of the field is inspected:
            // when every remaining byte looks like padding (ASCII space/NUL, 0xA0,
            // or an MS932 double-byte pair whose lead is ≥ 0x80), the digits decoded
            // so far are kept and the tail is zero-filled — the Hitachi-compatible
            // tolerance for aged production data.
            if (LENIENT_NUMERIC) {
                boolean remainderPaddingLike = true;
                for (int j = i; j < bytes.length; j++) {
                    int bj = bytes[j] & 0xFF;
                    if (bj >= '0' && bj <= '9') {
                        remainderPaddingLike = false;
                        break;
                    }
                    if (bj == 0x20 || bj == 0x00 || bj == 0xA0) {
                        continue;
                    }
                    if (bj >= 0x80) {
                        // double-byte lead: its trail byte (0x40-0xFC) is consumed with it
                        if (j + 1 < bytes.length) {
                            j++;
                        }
                        continue;
                    }
                    remainderPaddingLike = false;
                    break;
                }
                if (remainderPaddingLike) {
                    for (int k = i; k < bytes.length; k++) {
                        digits[k] = '0';
                    }
                    return composeNumeric(pic, digits, negative);
                }
            }
            // Ultra-lenient path (-Dcobol.numeric.force0=true): a field that simply is
            // not numeric (sentinel/garbage such as "BBBBB") decodes the way zoned
            // decimal reads it — the LOW nibble of each byte is the digit (0x42 'B'
            // → 2, so "BBBBB" → 22222) — rather than aborting. Legacy sysout prints
            // such sentinels via the zoned read, never as zero, and this matches it.
            // A nibble above 9 degrades to 0.
            if (FORCE0_NUMERIC) {
                for (int k = i; k < bytes.length; k++) {
                    int lo = bytes[k] & 0x0F;
                    digits[k] = (char) ('0' + (lo <= 9 ? lo : 0));
                }
                return composeNumeric(pic, digits, negative);
            }
            // neither digit nor overpunch — numeric data error (SOC7 / file status "09")
            throw new NumericValueException(
                    String.format("DISPLAY non-numeric byte 0x%02X at relative position %d", b, i),
                    pic.getName(),
                    pic.getByteOffset() + i);
        }
        return composeNumeric(pic, digits, negative);
    }

    private static Object decodeComp3(SchemaField pic, byte[] bytes) {
        StringBuilder digits = new StringBuilder();
        boolean negative = false;
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            int hi = (b >>> 4) & 0xF;
            int lo = b & 0xF;
            if (i < bytes.length - 1) {
                // interior bytes: two digit nibbles
                if (hi > 9 || lo > 9) {
                    throw new NumericValueException(
                            String.format(
                                    "COMP-3 invalid digit nibble byte=0x%02X (hi=0x%X lo=0x%X)",
                                    b, hi, lo),
                            pic.getName(),
                            pic.getByteOffset() + i);
                }
                digits.append((char) ('0' + hi));
                digits.append((char) ('0' + lo));
            } else {
                // final byte: digit in the high nibble, sign in the low
                if (hi > 9) {
                    throw new NumericValueException(
                            String.format(
                                    "COMP-3 invalid hi digit nibble byte=0x%02X (hi=0x%X)", b, hi),
                            pic.getName(),
                            pic.getByteOffset() + i);
                }
                // COBOL accepts sign nibbles 0xA-0xF: 0xC positive / 0xD negative for
                // signed items, 0xF unsigned, plus 0xB (negative) and 0xA/0xE
                // (positive) as the rare variants.
                if (lo < 0xA) {
                    throw new NumericValueException(
                            String.format(
                                    "COMP-3 invalid sign nibble byte=0x%02X (lo=0x%X)", b, lo),
                            pic.getName(),
                            pic.getByteOffset() + i);
                }
                digits.append((char) ('0' + hi));
                if (lo == 0xD || lo == 0xB) {
                    negative = true;
                }
            }
        }
        int total = pic.getIntegerDigits() + pic.getFractionDigits();
        String s = digits.toString();
        if (s.length() > total) {
            s = s.substring(s.length() - total);
        }
        return composeNumeric(pic, s.toCharArray(), negative);
    }

    private static Object decodeBinary(SchemaField pic, byte[] bytes) {
        long v = 0;
        if (pic.isSigned() && bytes.length > 0 && (bytes[0] & 0x80) != 0) {
            v = -1L;
        }
        for (int i = 0; i < bytes.length; i++) {
            v = (v << 8) | (bytes[i] & 0xFFL);
        }
        if (pic.getFractionDigits() > 0) {
            return new BigDecimal(BigInteger.valueOf(v), pic.getFractionDigits());
        }
        return v;
    }

    private static Object composeNumeric(SchemaField pic, char[] digits, boolean negative) {
        String raw = new String(digits);
        if (raw.isEmpty()) {
            raw = "0";
        }
        if (pic.getFractionDigits() > 0) {
            BigDecimal v = new BigDecimal(new BigInteger(raw), pic.getFractionDigits());
            return negative ? v.negate() : v;
        }
        BigInteger bi = new BigInteger(raw);
        if (negative) {
            bi = bi.negate();
        }
        if (bi.bitLength() < 63) {
            return bi.longValueExact();
        }
        return bi;
    }

    /* ── encoding ───────────────────────────────────────────────────── */

    /** Encodes one field into its byte slice; public because {@link RecordImage} shares it. */
    public static byte[] encodeField(SchemaField pic, Object value, java.nio.charset.Charset cs) {
        switch (pic.getKind()) {
            case ALPHANUMERIC:
                return encodeAlphanumeric(pic, value, cs);
            case DBCS:
                return encodeDbcs(pic, value, cs);
            case NUM:
                switch (pic.getUsage()) {
                    case COMP3:
                        return encodeComp3(pic, value);
                    case BINARY:
                        return encodeBinary(pic, value);
                    case COMP1:
                        return encodeComp1(pic, value);
                    case COMP2:
                        return encodeComp2(pic, value);
                    case DISPLAY:
                    default:
                        return encodeDisplay(pic, value);
                }
            default:
                throw new IllegalStateException("Kind not supported: " + pic.getKind());
        }
    }

    private static byte[] encodeAlphanumeric(
            SchemaField pic, Object value, java.nio.charset.Charset cs) {
        String s = value != null ? value.toString() : "";
        byte[] enc = s.getBytes(cs);
        byte[] out = new byte[pic.getByteLength()];
        Arrays.fill(out, (byte) 0x20);
        int copy = Math.min(enc.length, out.length);
        System.arraycopy(enc, 0, out, 0, copy);
        return out;
    }

    private static byte[] encodeDbcs(SchemaField pic, Object value, java.nio.charset.Charset cs) {
        String s = value != null ? value.toString() : "";
        // COBOL MOVE into PIC N (national):
        //   - a numeric/alphanumeric source is widened to full-width characters
        //     ("0410" -> "０４１０");
        //   - a national source copies byte-for-byte, keeping any embedded
        //     half-width character exactly as stored.
        // The source PIC kind is not attached to the value here, so content decides:
        // pure ASCII (<= 0x7F) is the numeric/alphanumeric case and is widened;
        // anything already holding a full-width character (kanji/kana) is national
        // content and passes through untouched. The exact fix would key the
        // conversion on the source field kind at the MOVE site; this content test
        // is its faithful stand-in.
        if (isAsciiOnly(s)) {
            s = halfToFullWidth(s);
        }
        int charLen = pic.getIntegerDigits();
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < charLen) sb.append('\u3000');
        if (sb.length() > charLen) {
            sb.setLength(charLen);
        }
        byte[] enc = sb.toString().getBytes(cs);
        byte[] out = new byte[pic.getByteLength()];
        if (enc.length >= out.length) {
            System.arraycopy(enc, 0, out, 0, out.length);
        } else {
            System.arraycopy(enc, 0, out, 0, enc.length);
            byte[] fws = "\u3000".getBytes(cs);
            int idx = enc.length;
            while (idx + fws.length <= out.length) {
                System.arraycopy(fws, 0, out, idx, fws.length);
                idx += fws.length;
            }
        }
        return out;
    }

    /**
     * Full-width conversion for MOVE-to-national: printable ASCII (0x21-0x7E) maps onto
     * U+FF01-U+FF5E and the blank onto the full-width space (U+3000); anything else is already wide
     * and passes through. Applying it twice changes nothing.
     */
    private static String halfToFullWidth(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ' ') {
                b.append('\u3000');
            } else if (c >= '!' && c <= '~') {
                b.append((char) (c - '!' + '\uFF01'));
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }

    /**
     * Whether the string is pure ASCII (&lt;= 0x7F) — a half-width numeric or alphanumeric value
     * rather than national content. Guards {@link #halfToFullWidth}: national-to-national MOVEs
     * keep their embedded half-width characters verbatim, while an ASCII value headed into PIC N is
     * still widened ("010" -&gt; "０１０").
     */
    private static boolean isAsciiOnly(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 0x7F) {
                return false;
            }
        }
        return true;
    }

    private static String prepNumeric(SchemaField pic, Object value, boolean[] negOut) {
        BigDecimal bd;
        if (value instanceof BigDecimal dec) {
            bd = dec;
        } else if (value instanceof Number) bd = new BigDecimal(value.toString());
        else if (value instanceof String str)
            bd = str.trim().isEmpty() ? BigDecimal.ZERO : new BigDecimal(str.trim());
        else bd = BigDecimal.ZERO;
        boolean negative = bd.signum() < 0;
        bd = bd.abs();
        bd =
                (pic.getFractionDigits() > 0)
                        ? bd.setScale(pic.getFractionDigits(), RoundingMode.DOWN)
                        : bd.setScale(0, RoundingMode.DOWN);
        String digits = bd.unscaledValue().toString();
        int totalDigits = pic.getIntegerDigits() + pic.getFractionDigits();
        if (digits.length() > totalDigits) {
            digits = digits.substring(digits.length() - totalDigits);
        } else if (digits.length() < totalDigits) {
            StringBuilder pad = new StringBuilder(totalDigits);
            for (int i = digits.length(); i < totalDigits; i++) {
                pad.append('0');
            }
            pad.append(digits);
            digits = pad.toString();
        }
        negOut[0] = negative;
        return digits;
    }

    private static byte[] encodeDisplay(SchemaField pic, Object value) {
        boolean[] neg = new boolean[1];
        String digits = prepNumeric(pic, value, neg);
        byte[] out = digits.getBytes(StandardCharsets.US_ASCII);
        // Signed zoned decimal (PIC S9 DISPLAY) carries its sign as an overpunch on
        // the units byte: { / A-I positive, } / J-R negative (the IBM/Hitachi zoned
        // convention). Reading accepts both polarities, so writing must produce
        // both — otherwise a positive signed field would keep the plain digit
        // ('0' where '{' belongs) and drift from COBOL.
        if (pic.isSigned() && out.length > 0) {
            int last = out[out.length - 1] - '0';
            if (neg[0]) {
                out[out.length - 1] = (byte) (last == 0 ? '}' : ('J' + last - 1));
            } else {
                out[out.length - 1] = (byte) (last == 0 ? '{' : ('A' + last - 1));
            }
        }
        return out;
    }

    private static byte[] encodeComp3(SchemaField pic, Object value) {
        boolean[] neg = new boolean[1];
        String digits = prepNumeric(pic, value, neg);
        // an odd nibble count gains a leading zero
        if ((digits.length() + 1) % 2 != 0) {
            digits = "0" + digits;
        }
        int byteLen = (digits.length() + 1) / 2;
        byte[] out = new byte[byteLen];
        for (int i = 0; i < byteLen - 1; i++) {
            int hi = digits.charAt(i * 2) - '0';
            int lo = digits.charAt(i * 2 + 1) - '0';
            out[i] = (byte) ((hi << 4) | lo);
        }
        int hi = digits.charAt((byteLen - 1) * 2) - '0';
        int signNibble;
        if (!pic.isSigned()) {
            signNibble = 0xF;
        } else if (neg[0]) signNibble = 0xD;
        else signNibble = 0xC;
        out[byteLen - 1] = (byte) ((hi << 4) | signNibble);
        return out;
    }

    private static byte[] encodeBinary(SchemaField pic, Object value) {
        BigDecimal bd;
        if (value instanceof BigDecimal dec) {
            bd = dec;
        } else if (value instanceof Number) bd = new BigDecimal(value.toString());
        else if (value instanceof String str)
            bd = str.trim().isEmpty() ? BigDecimal.ZERO : new BigDecimal(str.trim());
        else bd = BigDecimal.ZERO;
        bd = bd.setScale(pic.getFractionDigits(), RoundingMode.DOWN);
        long v = bd.unscaledValue().longValueExact();
        byte[] out = new byte[pic.getByteLength()];
        for (int i = pic.getByteLength() - 1; i >= 0; i--) {
            out[i] = (byte) (v & 0xFF);
            v >>>= 8;
        }
        return out;
    }

    /* ── COMP-1 / COMP-2 — big-endian IEEE 754 ─────────────────────────── */

    private static Object decodeComp1(SchemaField pic, byte[] bytes) {
        if (bytes.length < 4) {
            throw new NumericValueException(
                    "COMP-1 expects 4 bytes, got " + bytes.length,
                    pic.getName(),
                    pic.getByteOffset());
        }
        int bits =
                ((bytes[0] & 0xFF) << 24)
                        | ((bytes[1] & 0xFF) << 16)
                        | ((bytes[2] & 0xFF) << 8)
                        | (bytes[3] & 0xFF);
        float f = Float.intBitsToFloat(bits);
        // BigDecimal keeps arithmetic uniform across the numeric kinds;
        // the accessor layer converts on demand.
        return BigDecimal.valueOf(f);
    }

    private static Object decodeComp2(SchemaField pic, byte[] bytes) {
        if (bytes.length < 8) {
            throw new NumericValueException(
                    "COMP-2 expects 8 bytes, got " + bytes.length,
                    pic.getName(),
                    pic.getByteOffset());
        }
        long bits = 0L;
        for (int i = 0; i < 8; i++) {
            bits = (bits << 8) | (bytes[i] & 0xFFL);
        }
        double d = Double.longBitsToDouble(bits);
        return BigDecimal.valueOf(d);
    }

    private static byte[] encodeComp1(SchemaField pic, Object value) {
        float f;
        if (value == null) {
            f = 0.0f;
        } else if (value instanceof Number num) f = num.floatValue();
        else if (value instanceof String str) {
            String s = str.trim();
            f = s.isEmpty() ? 0.0f : Float.parseFloat(s);
        } else f = 0.0f;
        int bits = Float.floatToIntBits(f);
        int width = Math.max(4, pic.getByteLength());
        byte[] out = new byte[width];
        // The four big-endian bytes land at the END of the slice — a schema declaring
        // a wider field is malformed but tolerated. Proper COMP-1 width is exactly 4.
        int off = width - 4;
        out[off] = (byte) ((bits >>> 24) & 0xFF);
        out[off + 1] = (byte) ((bits >>> 16) & 0xFF);
        out[off + 2] = (byte) ((bits >>> 8) & 0xFF);
        out[off + 3] = (byte) (bits & 0xFF);
        return out;
    }

    private static byte[] encodeComp2(SchemaField pic, Object value) {
        double d;
        if (value == null) {
            d = 0.0d;
        } else if (value instanceof Number num) d = num.doubleValue();
        else if (value instanceof String str) {
            String s = str.trim();
            d = s.isEmpty() ? 0.0d : Double.parseDouble(s);
        } else d = 0.0d;
        long bits = Double.doubleToLongBits(d);
        int width = Math.max(8, pic.getByteLength());
        byte[] out = new byte[width];
        int off = width - 8;
        for (int i = 0; i < 8; i++) {
            out[off + i] = (byte) ((bits >>> ((7 - i) * 8)) & 0xFF);
        }
        return out;
    }
}

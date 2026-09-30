package com.generated.orion.common.infrastructure.layout;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encode/decode fields by PIC + USAGE. Supports DISPLAY, COMP-3, BINARY, COMP-1 (IEEE 754 single),
 * COMP-2 (IEEE 754 double).
 *
 * <p>Byte order convention: COMP-1/COMP-2 emit/consume <b>big-endian</b> bytes matching IBM
 * mainframe / Hitachi VOS3 convention (same as BINARY which is also big-endian). For Intel
 * little-endian mainframe ports the byte order would need reversal — currently NOT supported (raise
 * as separate bug if surfaced).
 */
public final class RecordCodec {

    /**
     * Default strict numeric decode (throw NumericValueException on non-digit byte). Opt-in lenient
     * mode via system property for data quality issues that must be tolerated in production. When
     * enabled, garbage bytes at the end of a numeric field are silently zero-filled (Hitachi
     * parity).
     *
     * <p>Default: {@code false} — strict throw. Set {@code -Dcobol.numeric.lenient=true} to restore
     * legacy lenient behaviour for known-bad data, but only after investigation.
     */
    private static final boolean LENIENT_NUMERIC =
            Boolean.parseBoolean(System.getProperty("cobol.numeric.lenient", "false"));

    private final RecordSchema layout;

    public RecordCodec(RecordSchema layout) {
        this.layout = layout;
    }

    public RecordSchema layout() {
        return layout;
    }

    public Map<String, Object> parse(byte[] record) {
        if (record.length < layout.recordByteLength()) {
            throw new IllegalArgumentException(
                    "Record has too few bytes: need "
                            + layout.recordByteLength()
                            + " got "
                            + record.length);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (LayoutField f : layout.leaves()) {
            byte[] slice =
                    Arrays.copyOfRange(
                            record, f.getByteOffset(), f.getByteOffset() + f.getByteLength());
            out.put(f.getName(), decodeField(f, slice, layout.charset()));
        }
        return out;
    }

    public byte[] serialize(Map<String, Object> values) {
        byte[] buf = new byte[layout.recordByteLength()];
        Arrays.fill(buf, (byte) 0x20);
        for (LayoutField f : layout.leaves()) {
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

    private static boolean isInRedefinesGroup(LayoutField f) {
        SchemaNode n = f.isRedefines() ? f : f.getParent();
        while (n != null) {
            if (n.isRedefines()) {
                return true;
            }
            n = n.getParent();
        }
        return false;
    }

    private static boolean isDefaultValue(LayoutField pic, Object v) {
        if (v == null) {
            return true;
        }
        switch (pic.getKind()) {
            case NUM:
                if (v instanceof BigDecimal) {
                    return ((BigDecimal) v).signum() == 0;
                }
                if (v instanceof Number) {
                    return ((Number) v).longValue() == 0L;
                }
                return v.toString().trim().isEmpty();
            case ALPHANUMERIC:
            case DBCS:
                return v.toString().trim().isEmpty();
            default:
                return false;
        }
    }

    /* ── decode ─────────────────────────────────────────────────────── */

    /** Decode a single field from a byte slice — public so {@link RecordBuffer} can reuse it. */
    public static Object decodeField(LayoutField pic, byte[] bytes, java.nio.charset.Charset cs) {
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

    private static Object decodeDisplay(LayoutField pic, byte[] bytes) {
        boolean negative = false;
        char[] digits = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            if (b == 0x20 || b == 0x00) {
                digits[i] = '0';
                continue;
            } // COBOL blank/null → '0'
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
                // fall through to garbage handler
            }
            // Lenient remainder-padding scan, gated by LENIENT_NUMERIC. Default
            // (strict) → fall through to throw NumericValueException, surfacing
            // data quality issues. Lenient mode (opt-in via -Dcobol.numeric.lenient=true)
            // preserves Hitachi-parity tolerance for legacy production data.
            //
            // Lenient semantics: scan from current position. If every remaining byte is
            // "padding-like" (ASCII space/null, 0xA0, OR a valid MS932 DBCS pair lead-byte
            // ≥0x80 + trail-byte), treat the field as having trailing zeroes. Preserve
            // digits already decoded into `digits[0..i-1]` and pad the rest.
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
                        // MS932 DBCS lead byte: skip the trail byte (any 0x40-0xFC).
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
                // Non-digit byte with real digits still following (e.g. untrusted 3270 screen
                // input "ABC" into a PIC 9 field). COBOL does not abend reading this — it yields
                // a defined value and the program guards correctness with IS NUMERIC. Treat this
                // byte as '0' and continue rather than throwing.
                digits[i] = '0';
                continue;
            }
            // byte not digit/overpunch → numeric data error (COBOL SOC7 / status "09")
            throw new NumericValueException(
                    String.format("DISPLAY non-numeric byte 0x%02X at relative position %d", b, i),
                    pic.getName(),
                    pic.getByteOffset() + i);
        }
        return composeNumeric(pic, digits, negative);
    }

    private static Object decodeComp3(LayoutField pic, byte[] bytes) {
        StringBuilder digits = new StringBuilder();
        boolean negative = false;
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            int hi = (b >>> 4) & 0xF;
            int lo = b & 0xF;
            if (i < bytes.length - 1) {
                // Both nibbles are digit positions
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
                // Last byte: hi = digit, lo = sign nibble
                if (hi > 9) {
                    throw new NumericValueException(
                            String.format(
                                    "COMP-3 invalid hi digit nibble byte=0x%02X (hi=0x%X)", b, hi),
                            pic.getName(),
                            pic.getByteOffset() + i);
                }
                // Valid COBOL sign nibbles: 0xA, 0xB (rare neg), 0xC (pos signed), 0xD (neg
                // signed),
                // 0xE (rare pos), 0xF (unsigned positive).
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

    private static Object decodeBinary(LayoutField pic, byte[] bytes) {
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

    private static Object composeNumeric(LayoutField pic, char[] digits, boolean negative) {
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

    /* ── encode ─────────────────────────────────────────────────────── */

    /** Encode a single field into a byte slice — public so {@link RecordBuffer} can reuse it. */
    public static byte[] encodeField(LayoutField pic, Object value, java.nio.charset.Charset cs) {
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
            LayoutField pic, Object value, java.nio.charset.Charset cs) {
        String s = value != null ? value.toString() : "";
        byte[] enc = s.getBytes(cs);
        byte[] out = new byte[pic.getByteLength()];
        Arrays.fill(out, (byte) 0x20);
        int copy = Math.min(enc.length, out.length);
        if (pic.isJustifiedRight()) {
            // COBOL JUSTIFIED RIGHT: value occupies the rightmost bytes, left space-padded
            // (on overflow the leftmost chars are truncated, keeping the rightmost).
            System.arraycopy(enc, enc.length - copy, out, out.length - copy, copy);
        } else {
            System.arraycopy(enc, 0, out, 0, copy);
        }
        return out;
    }

    private static byte[] encodeDbcs(LayoutField pic, Object value, java.nio.charset.Charset cs) {
        String s = value != null ? value.toString() : "";
        // COBOL MOVE into PIC N (national):
        //   - numeric / alphanumeric source  -> zenkaku-convert to full-width (e.g. jigyosho code
        //     "041009" -> "０４１００９"; buten "010" -> "０１０").
        //   - national source (national->national) -> BYTE-PRESERVING copy: keep verbatim,
        //     including any embedded half-width byte (e.g. master name "バンド4" keeps "4").
        // At encode time the source PIC kind isn't carried on the value, so it is approximated by
        // content: a value that is pure ASCII (<=0x7F) is the numeric/alphanumeric case and IS
        // widened; a value that already contains a DBCS/full-width char (kanji/kana) is national
        // content and is preserved as-is. NOTE: the ideal fix is to apply the conversion at the
        // MOVE site keyed on the source field kind (NUM/ALPHANUMERIC -> widen, DBCS -> copy); this
        // content heuristic is the interim equivalent (see delivery backport
        // ZENKAKU_MOVE_TO_NATIONAL).
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
     * Zenkaku conversion for MOVE-to-national: ASCII printable (0x21-0x7E) -> full-width
     * (U+FF01-U+FF5E), ASCII space -> full-width space (U+3000). Other chars unchanged (already
     * full-width / DBCS). Idempotent.
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
     * True if every char is ASCII (&lt;= 0x7F) — i.e. a half-width ASCII/numeric value rather than
     * DBCS national content. Gates {@link #halfToFullWidth} so a national-&gt;national MOVE
     * preserves embedded half-width bytes verbatim (e.g. "バンド4" keeps the half-width "4"), while a
     * pure ASCII/numeric value moved into PIC N is still widened ("010" -&gt; "０１０").
     */
    private static boolean isAsciiOnly(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 0x7F) {
                return false;
            }
        }
        return true;
    }

    private static String prepNumeric(LayoutField pic, Object value, boolean[] negOut) {
        BigDecimal bd;
        if (value instanceof BigDecimal) {
            bd = (BigDecimal) value;
        } else if (value instanceof Number) bd = new BigDecimal(value.toString());
        else if (value instanceof String)
            bd =
                    ((String) value).trim().isEmpty()
                            ? BigDecimal.ZERO
                            : new BigDecimal(((String) value).trim());
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

    /** True iff {@code s} parses as a decimal number (so prepNumeric won't throw on it). */
    private static boolean isParseableNumber(String s) {
        if (s == null) {
            return false;
        }
        String t = s.trim();
        if (t.isEmpty()) return true; // blank → treated as zero by prepNumeric
        try {
            new BigDecimal(t);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static byte[] encodeDisplay(LayoutField pic, Object value) {
        // COBOL DISPLAY numeric fields hold one byte per digit position. A MOVE of non-numeric
        // text (e.g. raw screen input "ABCDEFG") does NOT abend in COBOL — the bytes are stored
        // as-is and a later IS NUMERIC test detects them. Mirror that here instead of throwing:
        // store the raw characters right-justified, space-padded to the field width.
        if (value instanceof String && !isParseableNumber((String) value)) {
            int width = pic.getIntegerDigits() + pic.getFractionDigits();
            byte[] raw = new byte[width];
            java.util.Arrays.fill(raw, (byte) ' ');
            byte[] src = ((String) value).trim().getBytes(StandardCharsets.US_ASCII);
            int copy = Math.min(src.length, width);
            System.arraycopy(src, src.length - copy, raw, width - copy, copy);
            return raw;
        }
        boolean[] neg = new boolean[1];
        String digits = prepNumeric(pic, value, neg);
        byte[] out = digits.getBytes(StandardCharsets.US_ASCII);
        if (pic.isSigned() && neg[0] && out.length > 0) {
            int last = out[out.length - 1] - '0';
            out[out.length - 1] = (byte) (last == 0 ? '}' : ('J' + last - 1));
        }
        return out;
    }

    private static byte[] encodeComp3(LayoutField pic, Object value) {
        boolean[] neg = new boolean[1];
        String digits = prepNumeric(pic, value, neg);
        // Left-pad if total number of nibbles is odd
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

    private static byte[] encodeBinary(LayoutField pic, Object value) {
        BigDecimal bd;
        if (value instanceof BigDecimal) {
            bd = (BigDecimal) value;
        } else if (value instanceof Number) bd = new BigDecimal(value.toString());
        else if (value instanceof String)
            bd =
                    ((String) value).trim().isEmpty()
                            ? BigDecimal.ZERO
                            : new BigDecimal(((String) value).trim());
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

    /* ── COMP-1 / COMP-2 (IEEE 754 single / double, big-endian) ────────── */

    private static Object decodeComp1(LayoutField pic, byte[] bytes) {
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
        // Return BigDecimal for arithmetic consistency with other NUM types.
        // Caller (RecordBuffer.getDouble/getDecimal) handles conversion.
        return BigDecimal.valueOf((double) f);
    }

    private static Object decodeComp2(LayoutField pic, byte[] bytes) {
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

    private static byte[] encodeComp1(LayoutField pic, Object value) {
        float f;
        if (value == null) {
            f = 0.0f;
        } else if (value instanceof Number) f = ((Number) value).floatValue();
        else if (value instanceof String) {
            String s = ((String) value).trim();
            f = s.isEmpty() ? 0.0f : Float.parseFloat(s);
        } else f = 0.0f;
        int bits = Float.floatToIntBits(f);
        int width = Math.max(4, pic.getByteLength());
        byte[] out = new byte[width];
        // Write big-endian into the LAST 4 bytes (in case schema declared a larger width,
        // which would be malformed but be tolerant). Standard COMP-1 width is exactly 4.
        int off = width - 4;
        out[off] = (byte) ((bits >>> 24) & 0xFF);
        out[off + 1] = (byte) ((bits >>> 16) & 0xFF);
        out[off + 2] = (byte) ((bits >>> 8) & 0xFF);
        out[off + 3] = (byte) (bits & 0xFF);
        return out;
    }

    private static byte[] encodeComp2(LayoutField pic, Object value) {
        double d;
        if (value == null) {
            d = 0.0d;
        } else if (value instanceof Number) d = ((Number) value).doubleValue();
        else if (value instanceof String) {
            String s = ((String) value).trim();
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

package com.sakura.runtime.record;

import java.math.BigDecimal;

/**
 * Renders numeric values through pre-parsed COBOL editing pictures, producing the exact fixed-width
 * bytes an edited PIC field stores.
 *
 * <p>Handled pictures: - ZERO_SUPPRESS — PIC Z(n): zero → all blanks (width = digit positions)
 * positive → leading zeros blanked, significant digits kept negative → undefined by the COBOL
 * standard; the magnitude is used
 *
 * <p>- SIGN_SUPPRESS — PIC --..9 / --,---,---,--9 / all-dash ---,---,---: zero → with a pinned
 * trailing `9` the last position prints '0' and the rest blank; without it, the whole field blanks
 * positive → leading zeros and their commas blanked; digits + commas shown negative → '-' occupies
 * the last blanked position, directly left of the first significant digit; digits as in the
 * positive case
 *
 * <p>Overflow (more digits than positions): COBOL silently drops the leftmost digits, and so does
 * this formatter — the rightmost positions win.
 *
 * <p>The output alphabet is plain ASCII ('0'-'9', '-', ',', ' '), so the bytes survive MS932 and
 * UTF-8 round-trips unchanged.
 */
public final class EditPictureFormatter {

    private EditPictureFormatter() {}

    /**
     * Renders one value under the given editing-picture attributes.
     *
     * @param format the editing family (ZERO_SUPPRESS / SIGN_SUPPRESS)
     * @param integerDigits digit positions in the picture (PIC Z(5) → 5, PIC ---,---,--9 → 9)
     * @param insertCommas whether the picture carries `,` separators
     * @param hasTrailingNine whether the picture pins a final `9` (---,---,--9 vs ---,---,---),
     *     which decides how zero prints under SIGN_SUPPRESS
     * @param value the number — any sign, any magnitude; overflow truncates
     * @return the rendered string, exactly integerDigits + comma-count bytes wide
     */
    public static String format(
            SchemaField.EditFormat format,
            int integerDigits,
            boolean insertCommas,
            boolean hasTrailingNine,
            BigDecimal value) {
        return format(format, integerDigits, insertCommas, hasTrailingNine, value, "");
    }

    /**
     * Variant with `/` insertion (PIC ZZZZ/ZZ, ZZZZ/ZZ/ZZ — date-style editing).
     *
     * @param slashAfter digit positions (CSV) that a `/` follows, e.g. "4" or "4,6"; empty means
     *     none. Slashes are placed after suppression has run, keeping them at fixed structural
     *     positions.
     */
    public static String format(
            SchemaField.EditFormat format,
            int integerDigits,
            boolean insertCommas,
            boolean hasTrailingNine,
            BigDecimal value,
            String slashAfter) {
        return format(
                format, integerDigits, insertCommas, hasTrailingNine, value, slashAfter, 0, "");
    }

    /**
     * Variant that also knows the field's fractional scale (COBOL V9(n) / edited `.99`).
     *
     * <p>{@code integerDigits} counts EVERY digit slot of the picture — the parser tallies each
     * `9`/`Z`/`-`/`+`, fraction included when a `.` is present. {@code decimalDigits} (the field's
     * scale) splits that total back into {@code integerDigits - decimalDigits} integer slots and
     * the fraction slots. Comma grouping touches only the integer slots; the point and fraction are
     * appended once suppression is done.
     *
     * <p>Point visibility mirrors COBOL editing: a pinned fraction `9` ({@code hasTrailingNine}, as
     * in {@code ---,---,--9.99}) always prints point and fraction — zero gives "0.00". A
     * suppressible fraction (all Z / all dash, no pinned 9) blanks the entire field for a true
     * zero, point included; any other value prints point and fraction.
     */
    public static String format(
            SchemaField.EditFormat format,
            int integerDigits,
            boolean insertCommas,
            boolean hasTrailingNine,
            BigDecimal value,
            String slashAfter,
            int decimalDigits) {
        return format(
                format,
                integerDigits,
                insertCommas,
                hasTrailingNine,
                value,
                slashAfter,
                decimalDigits,
                "");
    }

    /**
     * Variant that receives the picture's literal comma layout ({@code commaAfter}: digit
     * positions, CSV, that a `,` follows — {@code ----,---,--9} has the non-standard 4-3-3 grouping
     * "4,7"). When supplied, commas land at exactly those positions instead of the usual
     * every-3-from-the-right rule; without this, a non-standard picture would gain one comma too
     * many, overflow the field width by a byte, and lose its last digit on write.
     */
    public static String format(
            SchemaField.EditFormat format,
            int integerDigits,
            boolean insertCommas,
            boolean hasTrailingNine,
            BigDecimal value,
            String slashAfter,
            int decimalDigits,
            String commaAfter) {
        if (format == null || format == SchemaField.EditFormat.NONE) {
            // not meant to be called without editing — degrade to plain zero-padded digits
            return zeroPad(value == null ? "0" : value.toBigInteger().toString(), integerDigits);
        }

        // 1 — capture the sign, continue with the magnitude
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        boolean negative = value.signum() < 0;

        // 1b — divide the digit slots between integer and fraction positions
        if (decimalDigits < 0) {
            decimalDigits = 0;
        }
        if (decimalDigits > integerDigits) {
            decimalDigits = integerDigits;
        }
        int intDigits = integerDigits - decimalDigits;

        // Every value digit, integer then fraction; surplus fraction digits are cut
        // exactly like a COBOL MOVE into an edited item — dropped low-order, no rounding.
        BigDecimal absVal = value.abs().setScale(decimalDigits, java.math.RoundingMode.DOWN);
        String allDigits = absVal.unscaledValue().toString();

        // 2 — normalize to exactly integerDigits digit slots
        if (allDigits.length() > integerDigits) {
            allDigits =
                    allDigits.substring(
                            allDigits.length() - integerDigits); // overflow: keep the right side
        } else {
            allDigits = zeroPad(allDigits, integerDigits);
        }
        String digits = allDigits.substring(0, intDigits); // integer slots
        String fracDigits = allDigits.substring(intDigits); // fraction slots

        // 3 — commas over the integer slots only. A known literal layout (commaAfter)
        // pins them to the picture's own positions — non-standard groupings like
        // ----,---,--9 (4-3-3) come out right; otherwise standard 3-digit grouping.
        String withCommas;
        if (commaAfter != null && !commaAfter.isEmpty()) {
            withCommas = insertCommasAt(digits, commaAfter);
        } else {
            withCommas = insertCommas ? insertThousandCommas(digits) : digits;
        }

        // 4 — suppression: leading zeros and their commas become blanks, ending at the
        // first non-zero digit — or, for SIGN_SUPPRESS with a pinned trailing 9 and a
        // zero value, right before the last position so that 9 still prints a digit.
        char[] chars = withCommas.toCharArray();
        int suppressEnd = findFirstSignificantPosition(chars, format, hasTrailingNine);
        for (int i = 0; i < suppressEnd; i++) {
            chars[i] = ' ';
        }

        // 5 — the SIGN_SUPPRESS minus sign takes the last blanked position, directly
        // left of the first significant digit. With nothing blanked (field completely
        // full) there is no room, and COBOL clips the sign — so do we.
        if (negative && format == SchemaField.EditFormat.SIGN_SUPPRESS) {
            if (suppressEnd > 0) {
                chars[suppressEnd - 1] = '-';
            }
            // no room → sign clipped (extreme overflow)
        }

        // 6 — `/` literals (PIC ZZZZ/ZZ …). Positions count digit slots from the left;
        // inserting right-to-left keeps earlier indices stable. (Slash and comma never
        // meet in one picture here, so slot index equals char index.)
        String intFormatted;
        if (slashAfter != null && !slashAfter.isEmpty()) {
            StringBuilder sb = new StringBuilder(new String(chars));
            String[] parts = slashAfter.split(",");
            int[] pos = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                try {
                    pos[i] = Integer.parseInt(parts[i].trim());
                } catch (NumberFormatException e) {
                    pos[i] = -1;
                }
            }
            java.util.Arrays.sort(pos);
            for (int i = pos.length - 1; i >= 0; i--) {
                int p = pos[i];
                if (p < 0 || p > sb.length()) {
                    continue;
                }
                // A `/` inside the zero-suppressed zone blanks along with it (COBOL:
                // PIC ZZZZ/ZZ/ZZ at 0 is all blanks, never "  /  /  "). The slash
                // prints only when the digit on its left survived suppression.
                char ins = (p >= 1 && p <= chars.length && chars[p - 1] != ' ') ? '/' : ' ';
                sb.insert(p, ins);
            }
            intFormatted = sb.toString();
        } else {
            intFormatted = new String(chars);
        }

        // 7 — point and fraction digits, when visible: a pinned `9` fraction always
        // prints; a suppressible fraction prints only for non-zero values (true zero
        // blanks the whole field, point included).
        if (decimalDigits > 0 && (hasTrailingNine || value.signum() != 0)) {
            return intFormatted + "." + fracDigits;
        }
        return intFormatted;
    }

    /**
     * Index of the first significant digit (1-9) in the comma-laid-out string. For SIGN_SUPPRESS
     * with a pinned trailing 9 and an all-zero value the answer is length-1: the final position
     * must still print '0', so blanking stops before it.
     */
    private static int findFirstSignificantPosition(
            char[] chars, SchemaField.EditFormat format, boolean hasTrailingNine) {
        // INSERTION (all-9 pictures like 9999/99/99) never suppresses: each digit slot
        // prints its digit (100 → "0000/01/00"), only the literals are added — no
        // leading position is ever blanked.
        if (format == SchemaField.EditFormat.INSERTION) {
            return 0;
        }
        // scan for the first non-zero digit
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c >= '1' && c <= '9') {
                return i; // blank everything before it
            }
        }
        // value is all zeros — the format decides
        if (format == SchemaField.EditFormat.SIGN_SUPPRESS && hasTrailingNine) {
            // A conventional --..9 picture ends on a digit slot, so the final char is
            // that pinned 9; blanking stops just before it and it prints '0'.
            return chars.length - 1;
        }
        // ZERO_SUPPRESS, or SIGN_SUPPRESS without a pinned 9 → everything blanks
        return chars.length;
    }

    /** Standard comma grouping, counted from the right: "000012345" → "000,012,345". */
    private static String insertThousandCommas(String digits) {
        StringBuilder sb = new StringBuilder();
        int len = digits.length();
        for (int i = 0; i < len; i++) {
            int posFromRight = len - 1 - i;
            if (i > 0 && posFromRight % 3 == 2) {
                sb.append(',');
            }
            sb.append(digits.charAt(i));
        }
        return sb.toString();
    }

    /**
     * Commas at the picture's own digit positions (counted from the left) — non-standard groupings
     * such as {@code ----,---,--9} ("4,7") come out exact. The CSV is parsed and applied
     * right-to-left so earlier indices stay put; positions outside {@code (0, len)} are ignored, a
     * comma only ever sits between two integer digits.
     */
    private static String insertCommasAt(String digits, String commaAfter) {
        String[] parts = commaAfter.split(",");
        int[] pos = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                pos[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                pos[i] = -1;
            }
        }
        java.util.Arrays.sort(pos);
        StringBuilder sb = new StringBuilder(digits);
        for (int i = pos.length - 1; i >= 0; i--) {
            int p = pos[i];
            if (p <= 0 || p >= digits.length()) {
                continue;
            }
            sb.insert(p, ',');
        }
        return sb.toString();
    }

    /**
     * Rendered byte width: the digit positions plus, under standard grouping, floor((integerDigits
     * - 1) / 3) commas.
     */
    public static int computeOutputWidth(int integerDigits, boolean insertCommas) {
        if (!insertCommas || integerDigits <= 3) {
            return integerDigits;
        }
        return integerDigits + (integerDigits - 1) / 3;
    }

    private static String zeroPad(String s, int width) {
        if (s.length() >= width) {
            return s;
        }
        StringBuilder sb = new StringBuilder(width);
        for (int i = s.length(); i < width; i++) {
            sb.append('0');
        }
        sb.append(s);
        return sb.toString();
    }
}

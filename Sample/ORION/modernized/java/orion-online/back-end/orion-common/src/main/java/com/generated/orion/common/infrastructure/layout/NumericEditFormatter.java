package com.generated.orion.common.infrastructure.layout;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Complete COBOL numeric edit-PICTURE formatter — picture-driven: the raw COBOL edit PICTURE is the
 * single source of truth. Produces the fixed-width display string COBOL stores in an edited field;
 * {@code RecordBuffer} writes it into the field's byte slice verbatim.
 *
 * <p>Supports the full edit grammar:
 *
 * <pre>
 *   digits           9
 *   zero suppress    Z (→ space)        check protect    * (→ '*')
 *   fixed sign       leading/trailing + / -   (+ → '+'/'-' , - → ' '/'-')
 *   CR / DB          trailing credit/debit    (negative → "CR"/"DB", positive → "  ")
 *   floating         run of $ / + / -   (symbol floats to just left of the first significant digit)
 *   fixed currency   single leading $
 *   insertions       ',' thousands   '.' decimal point   'B' blank   '0' zero   '/' slash
 *   repeat factors   Z(5), 9(3), $(4) … expanded first
 * </pre>
 *
 * <p>Negative values: the sign is rendered only where the PICTURE provides a sign position
 * (leading/trailing/floating + / - , or CR/DB). A pure zero-suppress PICTURE (PIC Z…) has no sign
 * position, so COBOL drops the sign — and so do we. Overflow (value wider than the integer digit
 * positions) truncates the leftmost digits, per COBOL.
 */
public final class NumericEditFormatter {

    private NumericEditFormatter() {}

    /** Format {@code value} according to the COBOL edit {@code picture}. */
    public static String format(String picture, BigDecimal value) {
        if (picture == null || picture.isEmpty()) {
            return value == null ? "" : value.toPlainString();
        }
        if (value == null) {
            value = BigDecimal.ZERO;
        }

        String pic = expand(picture.toUpperCase());
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs();

        // 1. Trailing sign / CR / DB. A trailing '+'/'-' counts as a fixed sign only when it is NOT
        // the tail of a floating sign run (e.g. "++++"); guard via the preceding character.
        String suffix = "";
        if (pic.endsWith("CR")) {
            suffix = negative ? "CR" : "  ";
            pic = pic.substring(0, pic.length() - 2);
        } else if (pic.endsWith("DB")) {
            suffix = negative ? "DB" : "  ";
            pic = pic.substring(0, pic.length() - 2);
        } else if (pic.endsWith("+") && !runTail(pic, '+')) {
            suffix = negative ? "-" : "+";
            pic = pic.substring(0, pic.length() - 1);
        } else if (pic.endsWith("-") && !runTail(pic, '-')) {
            suffix = negative ? "-" : " ";
            pic = pic.substring(0, pic.length() - 1);
        }

        // 2. Split integer / fraction at the editing decimal point ('.'), or implied 'V'.
        int dot = pic.indexOf('.');
        boolean printDot = dot >= 0;
        if (dot < 0) {
            dot = pic.indexOf('V');
        }
        String intPic = dot < 0 ? pic : pic.substring(0, dot);
        String fracPic = dot < 0 ? "" : pic.substring(dot + 1);
        int fracCount = countDigitPositions(fracPic);

        // 3. Scale value and split into integer / fraction digit strings.
        abs = abs.setScale(fracCount, RoundingMode.HALF_UP);
        String plain = abs.toPlainString();
        int pdot = plain.indexOf('.');
        String intDigits = pdot < 0 ? plain : plain.substring(0, pdot);
        String fracDigits = pdot < 0 ? "" : plain.substring(pdot + 1);

        // 4. Integer part (fixed/floating sign, fixed '$', Z/*, commas, suppression).
        StringBuilder out = new StringBuilder(formatIntegerPart(intPic, intDigits, negative));

        // 5. Fraction part — digits never suppressed; zero-padded to the PICTURE.
        if (printDot) {
            out.append('.');
            int fi = 0;
            for (int i = 0; i < fracPic.length(); i++) {
                char c = fracPic.charAt(i);
                if (c == '9' || c == 'Z' || c == '*') {
                    out.append(fi < fracDigits.length() ? fracDigits.charAt(fi++) : '0');
                } else if (c == 'B') out.append(' ');
                else if (c == '0') out.append('0');
                else if (c == '/') out.append('/');
                else out.append(c);
            }
        }
        return out.append(suffix).toString();
    }

    /** Integer part: fixed leading sign / '$', floating runs, then standard suppression. */
    private static String formatIntegerPart(String intPic, String intDigits, boolean negative) {
        if (intPic.isEmpty()) {
            return "";
        }
        String prefix = "";
        int start = 0;
        char c0 = intPic.charAt(0);
        boolean runStart = isFloatingRun(intPic, 0, c0); // start of a floating run
        if ((c0 == '+' || c0 == '-') && !runStart) {
            prefix = String.valueOf(negative ? '-' : (c0 == '+' ? '+' : ' ')); // fixed leading sign
            start = 1;
        } else if (c0 == '$' && !runStart) {
            prefix = "$"; // fixed leading currency
            start = 1;
        }
        String body = intPic.substring(start);
        if (!body.isEmpty()) {
            char fc = body.charAt(0);
            if (fc == '$' || fc == '+' || fc == '-') { // floating run (length >= 2 here)
                return prefix + formatFloating(body, fc, intDigits, negative);
            }
        }
        return prefix + formatStandard(body, intDigits);
    }

    /** Standard suppression body: 9 / Z / * / , / B / 0 / / (no floating symbol). */
    private static String formatStandard(String pic, String intDigits) {
        String digits = fitDigits(intDigits, countDigitPositions(pic));
        char fill = pic.indexOf('*') >= 0 ? '*' : ' ';
        StringBuilder sb = new StringBuilder();
        int di = 0;
        boolean suppressing = true;
        for (int i = 0; i < pic.length(); i++) {
            char c = pic.charAt(i);
            if (c == '9') {
                sb.append(digits.charAt(di++));
                suppressing = false;
            } else if (c == 'Z' || c == '*') {
                char d = digits.charAt(di++);
                if (suppressing && d == '0') {
                    sb.append(c == '*' ? '*' : ' ');
                } else {
                    sb.append(d);
                    suppressing = false;
                }
            } else if (c == ',') sb.append(suppressing ? fill : ',');
            else if (c == 'B') sb.append(' ');
            else if (c == '0') sb.append('0');
            else if (c == '/') sb.append('/');
            else sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Floating insertion: a leading run of {@code fc} ($ / + / -). The run of N float chars yields
     * N-1 zero-suppressed digit positions (one slot is reserved for the symbol), and the symbol
     * floats to immediately left of the first significant digit; positions to its left are blank.
     */
    private static String formatFloating(String body, char fc, String intDigits, boolean negative) {
        int width = body.length(); // every char is one output position
        // Desugar: drop the leftmost float char (the reserved symbol slot), turn the remaining
        // float chars into zero-suppressed 'Z' positions; commas / trailing 9s pass through.
        StringBuilder desugar = new StringBuilder();
        boolean droppedAnchor = false;
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (c == fc) {
                if (!droppedAnchor) droppedAnchor = true; // reserved slot — drop
                else desugar.append('Z');
            } else {
                desugar.append(c);
            }
        }
        String suppressed = formatStandard(desugar.toString(), intDigits); // width = width - 1
        int firstNonSpace = 0;
        while (firstNonSpace < suppressed.length() && suppressed.charAt(firstNonSpace) == ' ')
            firstNonSpace++;
        String sig = suppressed.substring(firstNonSpace);
        if (sig.isEmpty()) {
            return repeat(' ', width); // all-zero, no fixed 9 → blank field, no symbol
        }
        char symbol = (fc == '$') ? '$' : (negative ? '-' : (fc == '+' ? '+' : ' '));
        int spaces = Math.max(0, width - sig.length() - 1);
        return repeat(' ', spaces) + symbol + sig;
    }

    /**
     * True if {@code pic} ends with {@code c} that is part of a run (preceded by the same char) —
     * i.e. a floating sign run, not a standalone trailing fixed sign.
     */
    private static boolean runTail(String pic, char c) {
        return pic.length() >= 2 && pic.charAt(pic.length() - 2) == c;
    }

    /**
     * True when position {@code from} starts a COBOL floating insertion string: a second occurrence
     * of the float symbol follows, separated only by simple insertion characters (',' 'B' '0' '/').
     * COBOL allows the float run to be interleaved with insertions — PIC -,---,---,--9.99 is a
     * floating minus, not a fixed sign followed by literals.
     */
    private static boolean isFloatingRun(String pic, int from, char fc) {
        if (fc != '$' && fc != '+' && fc != '-') {
            return false;
        }
        for (int i = from + 1; i < pic.length(); i++) {
            char c = pic.charAt(i);
            if (c == fc) {
                return true;
            }
            if (c != ',' && c != 'B' && c != '0' && c != '/') {
                return false;
            }
        }
        return false;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /** Zero-pad on the left, or truncate leftmost (COBOL overflow) to {@code positions} digits. */
    private static String fitDigits(String digits, int positions) {
        if (positions <= 0) {
            return "";
        }
        if (digits.length() > positions) {
            return digits.substring(digits.length() - positions);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = digits.length(); i < positions; i++) {
            sb.append('0');
        }
        return sb.append(digits).toString();
    }

    /** Count digit-bearing positions (9 / Z / *). Commas / dots / sign / $ are not counted. */
    private static int countDigitPositions(String pic) {
        int n = 0;
        for (int i = 0; i < pic.length(); i++) {
            char c = pic.charAt(i);
            if (c == '9' || c == 'Z' || c == '*') {
                n++;
            }
        }
        return n;
    }

    private static String repeat(char c, int n) {
        if (n <= 0) {
            return "";
        }
        char[] a = new char[n];
        java.util.Arrays.fill(a, c);
        return new String(a);
    }

    /** Expand repeat factors: Z(5) → ZZZZZ, 9(3) → 999, $(4) → $$$$. */
    private static String expand(String pic) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < pic.length()) {
            char c = pic.charAt(i);
            if (i + 1 < pic.length() && pic.charAt(i + 1) == '(') {
                int end = pic.indexOf(')', i + 2);
                if (end > i + 2) {
                    try {
                        int k = Integer.parseInt(pic.substring(i + 2, end).trim());
                        for (int j = 0; j < k; j++) {
                            sb.append(c);
                        }
                        i = end + 1;
                        continue;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }
}

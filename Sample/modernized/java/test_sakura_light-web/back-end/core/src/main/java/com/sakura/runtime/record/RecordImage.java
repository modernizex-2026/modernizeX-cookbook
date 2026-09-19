package com.sakura.runtime.record;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * The raw bytes of one COBOL record, paired with its {@link RecordSchema} so callers read and write
 * by COBOL field name — no POJOs, no reflection.
 *
 * <p>Exposes the familiar accessor surface (getInt/getLong/getString/…); the frequent operations
 * reduce to map lookups plus System.arraycopy.
 */
public final class RecordImage implements FieldStore {

    private final RecordSchema layout;
    private byte[] data;

    public RecordImage(RecordSchema layout) {
        this.layout = layout;
        this.data = newBlank(layout.recordByteLength(), layout.charset());
        clear();
        applyInitialValues(); // VALUE-clause contents (constant tables and the like)
    }

    /** Writes each field's VALUE-clause bytes into the fresh buffer. */
    private void applyInitialValues() {
        for (SchemaField f : layout.leaves()) {
            String iv = f.getInitialValue();
            if (iv == null || iv.isEmpty()) {
                continue;
            }
            iv = unwrapLiteral(iv);
            int offset = f.getByteOffset();
            int length = f.getByteLength();
            if (length <= 0 || offset < 0 || offset + length > data.length) {
                continue;
            }
            byte[] valueBytes = figurativeBytes(iv, length, f, layout.charset());
            if (valueBytes == null && f.getKind() == SchemaField.Kind.NUM) {
                // A numeric field with a numeric literal VALUE (COMP-3 VALUE 0,
                // PIC 9(3) VALUE 11) must go through the converter so packing,
                // right-alignment and the sign nibble come out correct. Raw charset
                // bytes would corrupt it: "11" left-aligned reads as 110, and an
                // ASCII '0' (0x30) inside a packed field is garbage on decode.
                BigDecimal num = tryParseNumericLiteral(iv);
                if (num != null) {
                    valueBytes = RecordConverter.encodeField(f, num, layout.charset());
                }
            }
            if (valueBytes == null) {
                try {
                    valueBytes = iv.getBytes(layout.charset());
                } catch (Exception ignore) {
                    continue;
                }
            }
            int copyLen = Math.min(valueBytes.length, length);
            System.arraycopy(valueBytes, 0, data, offset, copyLen);
        }
    }

    /**
     * Byte fill for a COBOL figurative constant in a VALUE clause. Skipping this step, VALUE ZERO
     * on a PIC X(3) would write the letters 'Z','E','R' (0x5A 0x45 0x52) — and a PIC 9 REDEFINES
     * alias over those bytes then fails to decode. Returns null for anything that is not a
     * figurative constant, in which case the caller encodes plain charset bytes.
     */
    private static byte[] figurativeBytes(
            String iv, int length, SchemaField f, java.nio.charset.Charset charset) {
        if (iv == null) {
            return null;
        }
        String up = iv.trim().toUpperCase();
        if ("ZERO".equals(up) || "ZEROS".equals(up) || "ZEROES".equals(up)) {
            if (f.getKind() == SchemaField.Kind.NUM && f.getUsage() == SchemaField.Usage.COMP3) {
                byte[] b = new byte[length];
                if (length > 0) b[length - 1] = 0x0F; // unsigned positive zero, packed form
                return b;
            }
            // Số KHÔNG phải COMP-3: để encoder chung lo, vì mỗi USAGE có biểu diễn zero riêng.
            // Trước 2026-08-24 mọi usage ngoài COMP-3 đều bị fill ASCII '0' (0x30) ⇒
            // `PIC S9(9) COMP VALUE ZEROS` (4 byte nhị phân) đọc ra 0x30303030 = 808464432 thay vì
            // 0.
            // Đo được: carddemo có 69 field BINARY/COMP1/COMP2 mang VALUE ZERO* — tức 69 field khởi
            // tạo bằng rác. Chương trình nào TEST một field như vậy trước khi ghi sẽ rẽ nhánh sai.
            // COMP-3 giữ nguyên đường cũ (0x0F) để không đổi byte của cohort đang chạy.
            if (f.getKind() == SchemaField.Kind.NUM && f.getUsage() != SchemaField.Usage.DISPLAY) {
                return RecordConverter.encodeField(f, java.math.BigDecimal.ZERO, charset);
            }
            byte[] b = new byte[length];
            java.util.Arrays.fill(b, (byte) '0');
            return b;
        }
        if ("SPACE".equals(up) || "SPACES".equals(up)) {
            byte[] b = new byte[length];
            java.util.Arrays.fill(b, (byte) 0x20);
            return b;
        }
        if ("HIGH-VALUE".equals(up) || "HIGH-VALUES".equals(up)) {
            byte[] b = new byte[length];
            java.util.Arrays.fill(b, (byte) 0xFF);
            return b;
        }
        if ("LOW-VALUE".equals(up)
                || "LOW-VALUES".equals(up)
                || "NULL".equals(up)
                || "NULLS".equals(up)) {
            return new byte[length]; // zero-filled
        }
        return null;
    }

    /**
     * Numeric literal from a VALUE clause ("0", "11", "-5", "1.50") as BigDecimal; null when the
     * text is not a plain number, letting the caller fall back to raw charset bytes (an
     * alphanumeric VALUE on a numeric field is odd but tolerated).
     */
    private static BigDecimal tryParseNumericLiteral(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        if (t.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Removes the COBOL literal quoting from VALUE-clause text — NC"…" and N"…" (national
     * literals), "…" and '…' (alphanumeric) all reduce to their content; any other shape passes
     * through unchanged.
     */
    private static String unwrapLiteral(String s) {
        if (s == null || s.length() < 2) {
            return s;
        }
        int len = s.length();
        if (len >= 4
                && (s.charAt(0) == 'N' || s.charAt(0) == 'n')
                && s.charAt(1) == 'C'
                && s.charAt(2) == '"'
                && s.charAt(len - 1) == '"') {
            return s.substring(3, len - 1);
        }
        if (len >= 3
                && (s.charAt(0) == 'N' || s.charAt(0) == 'n')
                && s.charAt(1) == '"'
                && s.charAt(len - 1) == '"') {
            return s.substring(2, len - 1);
        }
        if (s.charAt(0) == '"' && s.charAt(len - 1) == '"') {
            return s.substring(1, len - 1);
        }
        if (s.charAt(0) == '\'' && s.charAt(len - 1) == '\'') {
            return s.substring(1, len - 1);
        }
        return s;
    }

    public RecordSchema layout() {
        return layout;
    }

    public byte[] bytes() {
        return data;
    }

    public void setBytes(byte[] bytes) {
        if (bytes == null) {
            this.data = newBlank(layout.recordByteLength(), layout.charset());
            clear();
            return;
        }
        int len = layout.recordByteLength();
        if (bytes.length == len) {
            this.data = bytes;
        } else {
            byte[] resized = newBlank(len, layout.charset());
            System.arraycopy(bytes, 0, resized, 0, Math.min(bytes.length, len));
            this.data = resized;
        }
    }

    /**
     * Reinitializes the whole buffer by each field's USAGE: DISPLAY numerics to ASCII '0'; COMP-3
     * to 0x00 digits with sign nibble 0x0C (signed) / 0x0F (unsigned); BINARY to 0x00; ALPHANUMERIC
     * to the ASCII blank; PIC N to the full-width space.
     */
    public void clear() {
        Arrays.fill(data, (byte) 0x20);
        for (SchemaField f : layout.leaves()) {
            if (f.getKind() == SchemaField.Kind.NUM) {
                clearNumericField(f);
            } else if (f.getKind() == SchemaField.Kind.DBCS) {
                // PIC N initializes to the MS932 full-width space (0x81 0x40), never
                // the ASCII blank — and every DBCS leaf needs it, standalone
                // top-level fields included, not only those nested inside groups.
                clearDbcsField(f);
            }
        }
    }

    /**
     * Full-width-space fill (0x81 0x40) for one PIC N leaf, expanded across OCCURS: the same
     * array-ancestor offset arithmetic as {@link #clearNumericField(SchemaField)}, so every
     * occurrence is initialized rather than element[0] alone.
     */
    private void clearDbcsField(SchemaField f) {
        int baseOffset = f.getByteOffset();
        int length = f.getByteLength();
        java.util.List<SchemaGroup> arrays = new java.util.ArrayList<>();
        for (SchemaGroup p = f.getParent(); p != null; p = p.getParent()) {
            if (p.isArray()) {
                arrays.add(p);
            }
        }
        int totalCount = 1;
        for (SchemaGroup g : arrays) {
            totalCount *= g.getOccurs();
        }
        for (int combo = 0; combo < totalCount; combo++) {
            int delta = 0;
            int rem = combo;
            for (SchemaGroup g : arrays) {
                int idx = rem % g.getOccurs();
                rem /= g.getOccurs();
                delta += idx * g.getElementSize();
            }
            int offset = baseOffset + delta;
            // 0x81 0x40 pairs; a stray odd byte (malformed PIC N) keeps its 0x20 fill
            for (int i = 0; i + 1 < length; i += 2) {
                data[offset + i] = (byte) 0x81;
                data[offset + i + 1] = (byte) 0x40;
            }
        }
    }

    private void clearNumericField(SchemaField f) {
        // A leaf under an OCCURS ancestor must be cleared in EVERY occurrence.
        // Leaving element[1+] on the 0x20 sweep is fatal for COMP-3 arrays: the
        // sign-nibble byte keeps a low nibble of 0x0, which the converter rejects
        // as an invalid sign on the next read.
        //
        // The stride between occurrences is the OCCURS GROUP's element size, never
        // the field's own width — a 4-byte field inside a 34-byte element recurs
        // every 34 bytes. The arithmetic matches effectiveOffset():
        // offset = base + Σ idx_k · elemSize_k over the array ancestors. Striding
        // by field width would clear the wrong bytes and miss the real occurrences.
        int baseOffset = f.getByteOffset();
        int length = f.getByteLength();

        // array ancestors, innermost first (order does not matter for coverage)
        java.util.List<SchemaGroup> arrays = new java.util.ArrayList<>();
        for (SchemaGroup p = f.getParent(); p != null; p = p.getParent()) {
            if (p.isArray()) {
                arrays.add(p);
            }
        }
        int totalCount = 1;
        for (SchemaGroup g : arrays) {
            totalCount *= g.getOccurs();
        }

        for (int combo = 0; combo < totalCount; combo++) {
            int delta = 0;
            int rem = combo;
            for (SchemaGroup g : arrays) { // mixed-radix split into per-dimension indexes
                int idx = rem % g.getOccurs();
                rem /= g.getOccurs();
                delta += idx * g.getElementSize();
            }
            int offset = baseOffset + delta;
            switch (f.getUsage()) {
                case DISPLAY:
                    Arrays.fill(data, offset, offset + length, (byte) '0');
                    // INITIALIZE of a signed zoned field yields +0, whose units byte
                    // carries the positive-zero overpunch '{' (IBM/Hitachi zoned rule)
                    // — the analogue of the COMP-3 sign nibble below, and consistent
                    // with what encodeDisplay writes.
                    if (f.isSigned() && length > 0) {
                        data[offset + length - 1] = (byte) '{';
                    }
                    break;
                case COMP3:
                    {
                        Arrays.fill(data, offset, offset + length - 1, (byte) 0x00);
                        if (length > 0) {
                            int signNibble = f.isSigned() ? 0xC : 0xF;
                            data[offset + length - 1] = (byte) (0x00 | signNibble);
                        }
                        break;
                    }
                case BINARY, COMP1, COMP2:
                    Arrays.fill(data, offset, offset + length, (byte) 0x00);
                    break;
                default:
                    Arrays.fill(data, offset, offset + length, (byte) '0');
            }
        }
    }

    /* ── Typed reads ─────────────────────────────────────────────────── */

    public int getInt(String fieldName) {
        Object v = getRaw(fieldName);
        if (v == null) {
            return 0;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        if (v instanceof BigDecimal bd) {
            return bd.intValue();
        }
        String s = v.toString().trim();
        return s.isEmpty() ? 0 : Integer.parseInt(s);
    }

    /**
     * Strict variant of {@link #getInt}: decodes DISPLAY bytes without the blank/NUL→zero coercion,
     * so spaces or garbage under a numeric comparison raise {@link NumericValueException} — the
     * COB010 parity COBOL programs rely on to abend on broken data (e.g. a master left INITIALIZEd
     * after a failed lookup).
     */
    @Override
    public int getIntStrict(String fieldName) {
        Object v = getRawStrict(fieldName);
        if (v == null) {
            return 0;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        if (v instanceof BigDecimal bd) {
            return bd.intValue();
        }
        String s = v.toString().trim();
        return s.isEmpty() ? 0 : Integer.parseInt(s);
    }

    /**
     * Strict variants of {@link #getLong}/{@link #getDecimal} — same COB010 parity as {@link
     * #getIntStrict}; zoned storage does not care how many digits or where the implied decimal
     * point sits.
     */
    @Override
    public long getLongStrict(String fieldName) {
        Object v = getRawStrict(fieldName);
        if (v == null) {
            return 0L;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        String s = v.toString().trim();
        return s.isEmpty() ? 0L : Long.parseLong(s);
    }

    @Override
    public BigDecimal getDecimalStrict(String fieldName) {
        Object v = getRawStrict(fieldName);
        if (v == null) {
            return BigDecimal.ZERO;
        }
        if (v instanceof BigDecimal bd) {
            return bd;
        }
        if (v instanceof Number) {
            return new BigDecimal(v.toString());
        }
        String s = v.toString().trim();
        return s.isEmpty() ? BigDecimal.ZERO : new BigDecimal(s);
    }

    public long getLong(String fieldName) {
        Object v = getRaw(fieldName);
        if (v == null) {
            return 0L;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        if (v instanceof BigDecimal bd) {
            return bd.longValue();
        }
        String s = v.toString().trim();
        return s.isEmpty() ? 0L : Long.parseLong(s);
    }

    public String getString(String fieldName) {
        // Reading an edited field as text hands back the RAW buffer bytes — commas,
        // sign and leading blanks intact. A MOVE from an edited item to PIC X must
        // carry the edited form verbatim; getRaw would collapse it to a BigDecimal.
        SchemaNode n = layout.get(fieldName);
        if (n instanceof SchemaField field && field.isEdited()) {
            byte[] slice =
                    Arrays.copyOfRange(
                            data, n.getByteOffset(), n.getByteOffset() + n.getByteLength());
            return stripReplacementChars(new String(slice, layout.charset()));
        }
        Object v = getRaw(fieldName);
        return v == null ? "" : stripReplacementChars(v.toString());
    }

    /**
     * Edited numeric field rendered through its editing picture (zero suppression, commas,
     * slashes). The buffer holds the plain numeric value, so the edited string is recomputed from
     * it via {@link EditPictureFormatter}; a field without editing falls back to getString.
     */
    @Override
    public String editedDisplay(String fieldName) {
        SchemaNode n = layout.get(fieldName);
        if (n instanceof SchemaField f && f.isEdited()) {
            String s =
                    EditPictureFormatter.format(
                            f.getEditFormat(),
                            f.getEditIntegerDigits(),
                            f.isEditInsertCommas(),
                            f.isEditHasTrailingNine(),
                            getDecimal(fieldName),
                            f.getEditSlashAfter(),
                            f.getScale(),
                            f.getEditCommaAfter());
            // A fixed trailing sign (PIC ...ZZ9-) is not a digit position, yet it
            // still owns the field's final byte on DISPLAY — '-' for negative,
            // blank for positive. The formatted digits are padded to the field's
            // full width with that sign closing the string.
            int w = f.getByteLength();
            if (s.length() < w) {
                char sign = getDecimal(fieldName).signum() < 0 ? '-' : ' ';
                StringBuilder sb = new StringBuilder(s);
                while (sb.length() < w - 1) sb.append(' ');
                if (sb.length() < w) {
                    sb.append(sign);
                }
                s = sb.toString();
            }
            return s;
        }
        return getString(fieldName);
    }

    /**
     * Drops U+FFFD from decoded text. A PIC N field holding ASCII content leaves orphaned MS932
     * lead bytes in its padding, and those decode to the replacement character — which never occurs
     * in legitimate COBOL data, so removing it is always safe. Trailing blanks stay: some flows
     * depend on their padding.
     */
    private static String stripReplacementChars(String s) {
        return s.indexOf('�') < 0 ? s : s.replace("�", "");
    }

    public BigDecimal getDecimal(String fieldName) {
        Object v = getRaw(fieldName);
        if (v == null) {
            return BigDecimal.ZERO;
        }
        if (v instanceof BigDecimal bd) {
            return bd;
        }
        if (v instanceof Number) {
            return new BigDecimal(v.toString());
        }
        String s = v.toString().trim();
        return s.isEmpty() ? BigDecimal.ZERO : new BigDecimal(s);
    }

    public double getDouble(String fieldName) {
        return getDecimal(fieldName).doubleValue();
    }

    public boolean getBoolean(String fieldName) {
        String s = getString(fieldName).trim();
        return !s.isEmpty() && !"0".equals(s) && !"false".equalsIgnoreCase(s);
    }

    public String groupToString(String fieldName) {
        SchemaNode n = layout.get(fieldName);
        if (n == null) {
            return "";
        }
        if (n instanceof SchemaField) {
            return getString(fieldName);
        }
        byte[] slice =
                Arrays.copyOfRange(data, n.getByteOffset(), n.getByteOffset() + n.getByteLength());
        return new String(slice, layout.charset());
    }

    /* ── Typed writes ────────────────────────────────────────────────── */

    public void setInt(String fieldName, int v) {
        setRaw(fieldName, v);
    }

    public void setLong(String fieldName, long v) {
        setRaw(fieldName, v);
    }

    public void setString(String fieldName, String v) {
        setRaw(fieldName, v);
    }

    public void setDecimal(String fieldName, BigDecimal v) {
        setRaw(fieldName, v);
    }

    public void setDouble(String fieldName, double v) {
        setRaw(fieldName, v);
    }

    public void setBoolean(String fieldName, boolean v) {
        setRaw(fieldName, v ? "1" : "0");
    }

    /**
     * Writes a group value. A blank value clears child-by-child according to each USAGE (a blind
     * 0x20 fill would corrupt COMP-3/BINARY children); a non-blank value byte-copies into the
     * group's slice.
     */
    public void setGroup(String fieldName, String value) {
        SchemaNode n = layout.get(fieldName);
        if (n == null) {
            return;
        }
        // HIGH-VALUES / LOW-VALUES sentinels byte-fill the slice directly. A group
        // assignment of HIGH-VALUES has to produce raw 0xFF bytes: pushing "ÿ"
        // through the MS932 encoder substitutes '?' (0x3F), and a later
        // HIGH-VALUES comparison — the classic loop-exit sentinel of a merge-style
        // read routine — never matches, leaving the write loop spinning.
        if (value != null && fillIfFigurative(n, value)) {
            return;
        }
        // The empty string AND the one-char ASCII blank both mean COBOL SPACES and
        // take the kind-aware clear. MOVE SPACE/SPACES TO group arrives as a
        // one-char " " literal; without that branch the group's PIC N leaves would
        // be 0x20-filled (reading back as half-width blanks) instead of carrying
        // the 0x8140 full-width-space pairs.
        boolean blank = (value == null || value.isEmpty() || " ".equals(value));
        if (blank && n instanceof SchemaGroup g) {
            clearGroupLeaves(g);
            return;
        }
        byte[] enc = (value == null ? "" : value).getBytes(layout.charset());
        byte[] slice = new byte[n.getByteLength()];
        Arrays.fill(slice, (byte) 0x20);
        System.arraycopy(enc, 0, slice, 0, Math.min(enc.length, slice.length));
        System.arraycopy(slice, 0, data, n.getByteOffset(), n.getByteLength());
    }

    /**
     * Clears every leaf of a group according to its own USAGE. After the bulk 0x20 sweep the PIC N
     * leaves receive their 0x8140 full-width-space pairs, so comparisons against `　` (U+3000) hold.
     */
    private void clearGroupLeaves(SchemaGroup g) {
        Arrays.fill(data, g.getByteOffset(), g.getByteOffset() + g.getByteLength(), (byte) 0x20);
        clearNumericLeaves(g);
        clearDbcsLeaves(g);
    }

    /**
     * Runs the offset-aware walk at delta 0, so a group-level clear (INITIALIZE / MOVE SPACES)
     * expands OCCURS occurrences exactly as {@link #encodeAt} would.
     */
    private void clearNumericLeaves(SchemaNode node) {
        // INITIALIZE parity: leaves under a REDEFINES stay untouched (COBOL INITIALIZE
        // skips REDEFINES items), so a numeric view over cleared spaces keeps reading
        // spaces — the strict readers can then raise the COB010 equivalent.
        clearNumericLeavesShifted(node, 0, true);
    }

    /**
     * Fills the PIC N leaves with 0x81 0x40 full-width-space pairs — the offset-aware,
     * OCCURS-expanding walk at delta 0 (see {@link #clearDbcsLeavesShifted}).
     */
    private void clearDbcsLeaves(SchemaNode node) {
        clearDbcsLeavesShifted(node, 0);
    }

    /**
     * Kind-aware clear of a group at an explicit offset, OCCURS expanded. {@link #encodeAt} needs
     * it when {@code MOVE SPACE/SPACES TO group(idx)} fires: the subscript moves the target slice
     * away from g.getByteOffset(), which rules out {@link #clearGroupLeaves(SchemaGroup)} (pinned
     * to the static base offset). Same three passes as that method: the bulk 0x20 sweep, the
     * per-leaf numeric clear ('0' / 0x00 / sign nibble), and the per-leaf PIC N 0x8140
     * full-width-space fill.
     */
    private void clearGroupLeavesAt(SchemaGroup g, int off) {
        Arrays.fill(data, off, off + g.getByteLength(), (byte) 0x20);
        int delta = off - g.getByteOffset();
        clearNumericLeavesShifted(g, delta, false);
        clearDbcsLeavesShifted(g, delta);
    }

    private void clearNumericLeavesShifted(SchemaNode node, int delta, boolean skipRedefines) {
        if (node instanceof SchemaField f) {
            if (skipRedefines && isUnderRedefines(f)) {
                return;
            }
            if (f.getKind() == SchemaField.Kind.NUM) {
                int off = f.getByteOffset() + delta;
                int len = f.getByteLength();
                switch (f.getUsage()) {
                    case DISPLAY:
                        Arrays.fill(data, off, off + len, (byte) '0');
                        if (f.isSigned() && len > 0) {
                            data[off + len - 1] =
                                    (byte) '{'; // positive-zero overpunch of S9 (INITIALIZE rule)
                        }
                        break;
                    case COMP3:
                        {
                            Arrays.fill(data, off, off + len - 1, (byte) 0x00);
                            if (len > 0) {
                                int signNibble = f.isSigned() ? 0xC : 0xF;
                                data[off + len - 1] = (byte) (0x00 | signNibble);
                            }
                            break;
                        }
                    case BINARY, COMP1, COMP2:
                        Arrays.fill(data, off, off + len, (byte) 0x00);
                        break;
                    default:
                        Arrays.fill(data, off, off + len, (byte) '0');
                }
            }
        } else if (node instanceof SchemaGroup g) {
            if (g.isArray()) {
                // an OCCURS group clears every occurrence, never element[0] alone
                for (int idx = 0; idx < g.getOccurs(); idx++) {
                    int elemDelta = delta + idx * g.getElementSize();
                    for (SchemaNode c : g.children()) {
                        clearNumericLeavesShifted(c, elemDelta, skipRedefines);
                    }
                }
            } else {
                for (SchemaNode c : g.children()) {
                    clearNumericLeavesShifted(c, delta, skipRedefines);
                }
            }
        }
    }

    /**
     * True when the field itself REDEFINES another item or sits anywhere under a REDEFINES group —
     * the item class COBOL's INITIALIZE leaves untouched.
     */
    private static boolean isUnderRedefines(SchemaField f) {
        if (f.isRedefines()) {
            return true;
        }
        for (SchemaGroup p = f.getParent(); p != null; p = p.getParent()) {
            if (p.isRedefines()) {
                return true;
            }
        }
        return false;
    }

    private void clearDbcsLeavesShifted(SchemaNode node, int delta) {
        if (node instanceof SchemaField f) {
            if (f.getKind() == SchemaField.Kind.DBCS) {
                int off = f.getByteOffset() + delta;
                int len = f.getByteLength();
                for (int i = 0; i + 1 < len; i += 2) {
                    data[off + i] = (byte) 0x81;
                    data[off + i + 1] = (byte) 0x40;
                }
            }
        } else if (node instanceof SchemaGroup g) {
            if (g.isArray()) {
                // Every occurrence of the OCCURS group is cleared. Child offsets are
                // relative to element[0], each further occurrence one element-size
                // stride along. Skipping the expansion would leave occurrences 2..n
                // holding the 0x20 sweep bytes, so a PIC N leaf would read as
                // half-width blanks and `= ALL NC"　"` (U+3000) comparisons fail.
                for (int idx = 0; idx < g.getOccurs(); idx++) {
                    int elemDelta = delta + idx * g.getElementSize();
                    for (SchemaNode c : g.children()) {
                        clearDbcsLeavesShifted(c, elemDelta);
                    }
                }
            } else {
                for (SchemaNode c : g.children()) {
                    clearDbcsLeavesShifted(c, delta);
                }
            }
        }
    }

    /* ── Raw byte operations ─────────────────────────────────────────── */

    @Override
    public byte[] sliceBytes(String fieldName) {
        SchemaNode n = layout.get(fieldName);
        if (n == null) {
            throw new IllegalArgumentException(
                    "Field not in layout " + layout.name() + ": " + fieldName);
        }
        return Arrays.copyOfRange(data, n.getByteOffset(), n.getByteOffset() + n.getByteLength());
    }

    @Override
    public void writeBytes(String fieldName, byte[] src) {
        SchemaNode n = layout.get(fieldName);
        if (n == null) {
            throw new IllegalArgumentException(
                    "Field not in layout " + layout.name() + ": " + fieldName);
        }
        if (src == null) {
            return;
        }
        int len = Math.min(src.length, n.getByteLength());
        System.arraycopy(src, 0, data, n.getByteOffset(), len);
    }

    /**
     * Slice narrowed by subscripts — one element of a self-OCCURS group when {@code subs} addresses
     * it (see {@link #effectiveOffset} / {@link #effectiveLength}); no subscripts means the whole
     * node. This is what lets a cross-01 {@code MOVE G(a) TO T} carry the element, not the array.
     */
    @Override
    public byte[] sliceBytes(String fieldName, int[] subs) {
        SchemaNode n = requireNode(fieldName);
        int off = effectiveOffset(n, subs);
        return Arrays.copyOfRange(data, off, off + effectiveLength(n, subs));
    }

    /**
     * Write narrowed by subscripts — lands on the single addressed element of a self-OCCURS group,
     * so a cross-01 {@code MOVE T TO G(b)} reaches element b rather than the array base.
     */
    @Override
    public void writeBytes(String fieldName, int[] subs, byte[] src) {
        if (src == null) {
            return;
        }
        SchemaNode n = requireNode(fieldName);
        int off = effectiveOffset(n, subs);
        int len = Math.min(src.length, effectiveLength(n, subs));
        System.arraycopy(src, 0, data, off, len);
    }

    @Override
    public void copyBytes(String toName, String fromName) {
        writeBytes(toName, sliceBytes(fromName));
    }

    /* ── Figurative-constant tests ───────────────────────────────────── */
    // COBOL fills HIGH-VALUE / LOW-VALUE / SPACE / ZERO across the field's whole
    // storage width, never as a single character — so the test inspects every
    // byte of the slice. In ASCII/MS932 terms: 0xFF / 0x00 / 0x20 / '0' (0x30).

    @Override
    public boolean isAllHighValues(String fieldName) {
        return allBytesEqual(fieldName, (byte) 0xFF);
    }

    @Override
    public boolean isAllLowValues(String fieldName) {
        return allBytesEqual(fieldName, (byte) 0x00);
    }

    @Override
    public boolean isAllSpaces(String fieldName) {
        return allBytesEqual(fieldName, (byte) 0x20);
    }

    // MOVE HIGH-VALUES / LOW-VALUES writes its sentinel bytes directly: routing "ÿ"
    // through the MS932 encoder would substitute '?' (0x3F) for 0xFF.
    @Override
    public void fillHighValues(String fieldName) {
        fillBytes(fieldName, (byte) 0xFF);
    }

    @Override
    public void fillLowValues(String fieldName) {
        fillBytes(fieldName, (byte) 0x00);
    }

    private void fillBytes(String fieldName, byte b) {
        SchemaNode n = layout.get(fieldName);
        if (n == null) {
            throw new IllegalArgumentException(
                    "Field not in layout " + layout.name() + ": " + fieldName);
        }
        int off = n.getByteOffset();
        int len = n.getByteLength();
        if (len <= 0) {
            return;
        }
        for (int i = 0; i < len; i++) {
            data[off + i] = b;
        }
    }

    /**
     * The ZEROS test respects USAGE: DISPLAY compares against '0' bytes, COMP-3/BINARY against 0x00
     * — the COMP-3 final byte may pair its zero digit with any valid sign nibble (0xA-0xF).
     */
    @Override
    public boolean isAllZeros(String fieldName) {
        SchemaNode n = layout.get(fieldName);
        if (n == null) {
            return false;
        }
        if (n instanceof SchemaField f) {
            switch (f.getUsage()) {
                case DISPLAY:
                    return allBytesEqual(fieldName, (byte) '0');
                case BINARY, COMP1, COMP2:
                    return allBytesEqual(fieldName, (byte) 0x00);
                case COMP3:
                    {
                        // every digit nibble zero; final byte pairs a zero digit with a sign nibble
                        int off = n.getByteOffset();
                        int len = n.getByteLength();
                        if (len <= 0) {
                            return false;
                        }
                        for (int i = 0; i < len - 1; i++) {
                            if (data[off + i] != 0) return false;
                        }
                        int last = data[off + len - 1] & 0xFF;
                        int hi = (last >>> 4) & 0xF;
                        int lo = last & 0xF;
                        return hi == 0 && lo >= 0xA; // any legitimate sign nibble passes
                    }
                default:
                    return false;
            }
        }
        // groups compare '0' across the region — rare, ZEROS on a group implies numeric content
        return allBytesEqual(fieldName, (byte) '0');
    }

    private boolean allBytesEqual(String fieldName, byte expected) {
        SchemaNode n = layout.get(fieldName);
        if (n == null) {
            return false;
        }
        int off = n.getByteOffset();
        int len = n.getByteLength();
        if (len <= 0) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (data[off + i] != expected) {
                return false;
            }
        }
        return true;
    }

    public boolean contains(String fieldName) {
        return layout.get(fieldName) != null;
    }

    /* ── conversion internals ────────────────────────────────────────── */

    private Object getRaw(String fieldName) {
        SchemaNode n = layout.get(fieldName);
        if (n == null) {
            throw new IllegalArgumentException(
                    "Field not in layout " + layout.name() + ": " + fieldName);
        }
        if (!(n instanceof SchemaField f)) {
            return groupToString(fieldName);
        }
        byte[] slice =
                Arrays.copyOfRange(data, f.getByteOffset(), f.getByteOffset() + f.getByteLength());
        // Reading an edited field is a COBOL "de-edit": filler (' ', ',') drops,
        // '-' recovers the sign, the digits parse. Pushing the edited bytes through
        // decodeDisplay would raise NumericValueException whenever code reads such
        // a field back after having totaled into it.
        if (f.isEdited()) {
            String s = new String(slice, layout.charset());
            return decodeEditedNumeric(s);
        }
        return RecordConverter.decodeField(f, slice, layout.charset());
    }

    /**
     * {@link #getRaw} with the strict decoder — identical resolution and de-edit handling; only the
     * final DISPLAY-numeric decode differs.
     */
    private Object getRawStrict(String fieldName) {
        SchemaNode n = layout.get(fieldName);
        if (n == null) {
            throw new IllegalArgumentException(
                    "Field not in layout " + layout.name() + ": " + fieldName);
        }
        if (!(n instanceof SchemaField f)) {
            return groupToString(fieldName);
        }
        byte[] slice =
                Arrays.copyOfRange(data, f.getByteOffset(), f.getByteOffset() + f.getByteLength());
        if (f.isEdited()) {
            String s = new String(slice, layout.charset());
            return decodeEditedNumeric(s);
        }
        return RecordConverter.decodeFieldStrict(f, slice, layout.charset());
    }

    /**
     * COBOL de-edit of an edited numeric string: blanks and commas drop, any '-' makes the value
     * negative, and a '.' fixes the decimal point so the digits after it become the fraction ("
     * 12.34" → 12.34, " 1,234" → 1234). Ignoring the point would fold the fraction into the integer
     * (12.34 → 1234).
     */
    private static BigDecimal decodeEditedNumeric(String s) {
        if (s == null || s.isEmpty()) {
            return BigDecimal.ZERO;
        }
        boolean negative = false;
        boolean afterPoint = false;
        int fractionDigits = 0;
        StringBuilder digits = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '-') {
                negative = true;
            } else if (c == '.') afterPoint = true; // digits beyond this are the fraction
            else if (c >= '0' && c <= '9') {
                digits.append(c);
                if (afterPoint) {
                    fractionDigits++;
                }
            }
            // everything else (' ', ',', '+', '/', 'B', …) is editing filler
        }
        if (digits.length() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal v = new BigDecimal(new java.math.BigInteger(digits.toString()), fractionDigits);
        return negative ? v.negate() : v;
    }

    private void setRaw(String fieldName, Object value) {
        SchemaNode n = layout.get(fieldName);
        if (n == null) {
            throw new IllegalArgumentException(
                    "Field not in layout " + layout.name() + ": " + fieldName);
        }
        // Figurative constants byte-fill the node's slice directly — 0xFF/0x00 would
        // not survive the charset encoder (MS932 turns U+00FF into '?'). Fields and
        // groups alike: MOVE HIGH-VALUES onto a key group is a common pattern.
        if (value instanceof String str && fillIfFigurative(n, str)) {
            return;
        }
        if (!(n instanceof SchemaField f)) {
            setGroup(fieldName, value == null ? "" : value.toString());
            return;
        }
        // Editing pictures (PIC Z…, PIC ---..9, …): a numeric MOVE into an edited
        // field runs the formatter before the bytes land.
        if (f.isEdited() && value != null) {
            java.math.BigDecimal num;
            if (value instanceof java.math.BigDecimal bd) {
                num = bd;
            } else if (value instanceof Number nn)
                num = java.math.BigDecimal.valueOf(nn.longValue());
            else {
                try {
                    num = new java.math.BigDecimal(value.toString().trim());
                } catch (Exception e) {
                    num = java.math.BigDecimal.ZERO;
                }
            }
            String formatted =
                    EditPictureFormatter.format(
                            f.getEditFormat(),
                            f.getEditIntegerDigits(),
                            f.isEditInsertCommas(),
                            f.isEditHasTrailingNine(),
                            num,
                            f.getEditSlashAfter(),
                            f.getScale(),
                            f.getEditCommaAfter());
            byte[] bytes = formatted.getBytes(layout.charset());
            // normalized to the exact field width
            int width = f.getByteLength();
            byte[] out = new byte[width];
            java.util.Arrays.fill(out, (byte) 0x20);
            System.arraycopy(bytes, 0, out, 0, Math.min(bytes.length, width));
            System.arraycopy(out, 0, data, f.getByteOffset(), width);
            return;
        }
        byte[] enc = RecordConverter.encodeField(f, value, layout.charset());
        System.arraycopy(enc, 0, data, f.getByteOffset(), Math.min(enc.length, f.getByteLength()));
    }

    /**
     * When {@code value} is a one-character figurative-constant sentinel, floods the node's slice
     * with the matching byte and reports true — HIGH-VALUES as 0xFF, LOW-VALUES as 0x00. SPACES
     * travels the setGroup blank path instead.
     */
    private boolean fillIfFigurative(SchemaNode n, String value) {
        if (value == null || value.length() != 1) {
            return false;
        }
        char c = value.charAt(0);
        byte fill;
        if (c == 'ÿ') {
            fill = (byte) 0xFF;
        } else if (c == '\u0000') fill = (byte) 0x00;
        else return false;
        Arrays.fill(data, n.getByteOffset(), n.getByteOffset() + n.getByteLength(), fill);
        return true;
    }

    private static byte[] newBlank(int len, java.nio.charset.Charset cs) {
        byte[] buf = new byte[len];
        Arrays.fill(buf, (byte) 0x20);
        return buf;
    }

    @Override
    public String toString() {
        return "RecordImage{" + layout.name() + " " + data.length + "B}";
    }

    /* ── OCCURS subscripts ───────────────────────────────────────────────
     * The schema carries occurs/elementSize on each array group, and the typed
     * wrappers pass COBOL subscripts through as trailing varargs on the
     * getter/setter calls.
     *
     * Conventions: subscripts are 1-based, and subs is read outermost
     * dimension first — the same order as the wrapper arguments, mirroring
     * COBOL "X(I,J)" where I indexes the outer OCCURS.                    */

    /**
     * Out-of-range subscripts throw by default, surfacing genuine defects such as an OCCURS access
     * made while the loop counter is still 0. A port that leans on silent clamping to element[1]
     * may set {@code -Dcobol.subscript.lenient=true}.
     */
    private static final boolean LENIENT_SUBSCRIPT =
            Boolean.parseBoolean(System.getProperty("cobol.subscript.lenient", "false"));

    /**
     * Physical byte offset of {@code n} under {@code subs}: the array chain is walked — every
     * ancestor with occurs&gt;1 plus, when {@code n} is itself an OCCURS group, its own dimension
     * as the innermost — and each matching subscript contributes (sub-1)·elementSize.
     *
     * <p>A subscript may address the OCCURS group ITSELF ({@code MOVE G(a) TO G(b)}, {@code
     * INITIALIZE G(row)}), not only a leaf inside it. That subscript belongs to the group node; an
     * ancestors-only walk would lose it, turning the row copy into a whole-array self-copy (a
     * no-op) and the row clear into a wipe of every row. Callers addressing one element must
     * combine this with {@link #effectiveLength} so the region spans one element, never the array.
     */
    private static int effectiveOffset(SchemaNode n, int[] subs) {
        if (subs == null || subs.length == 0) {
            return n.getByteOffset();
        }
        java.util.ArrayDeque<SchemaGroup> chain = new java.util.ArrayDeque<>();
        if (n instanceof SchemaGroup sg && sg.isArray())
            chain.addFirst(sg); // own dimension innermost
        SchemaGroup p = n.getParent();
        while (p != null) {
            if (p.isArray()) chain.addFirst(p); // ancestors stack up outermost-first
            p = p.getParent();
        }
        int delta = 0;
        int i = 0;
        int limit = Math.min(subs.length, chain.size());
        for (SchemaGroup g : chain) {
            if (i >= limit) {
                break;
            }
            int idx = subs[i] - 1; // 1-based COBOL → 0-based
            // Subscript 0 abends on the mainframe (S0C4/SSRANGE); throwing here is
            // the equivalent behavior and surfaces the defect. The lenient gate
            // retains the clamping some ports relied on.
            if (idx < 0 || idx >= g.getOccurs()) {
                if (LENIENT_SUBSCRIPT) {
                    idx = Math.max(0, Math.min(idx, g.getOccurs() - 1));
                } else {
                    throw new IndexOutOfBoundsException(
                            "COBOL subscript "
                                    + subs[i]
                                    + " out of range 1.."
                                    + g.getOccurs()
                                    + " for "
                                    + n.getName());
                }
            }
            delta += idx * g.getElementSize();
            i++;
        }
        return n.getByteOffset() + delta;
    }

    /**
     * Byte length of the region addressed under {@code subs}: one element when the innermost
     * subscript lands on {@code n}'s own OCCURS dimension (the self-subscripted case of {@link
     * #effectiveOffset}); otherwise the node's full length.
     */
    private static int effectiveLength(SchemaNode n, int[] subs) {
        if (subs != null && subs.length > 0 && n instanceof SchemaGroup g && g.isArray()) {
            int ancestors = 0;
            SchemaGroup p = n.getParent();
            while (p != null) {
                if (p.isArray()) ancestors++;
                p = p.getParent();
            }
            if (subs.length >= ancestors + 1) {
                return g.getElementSize();
            }
        }
        return n.getByteLength();
    }

    /** Decode from an explicit byte offset instead of the field's schema offset. */
    private Object decodeAt(SchemaNode n, int off) {
        return decodeAt(n, off, n.getByteLength());
    }

    /**
     * Length-carrying variant — {@code len} matters only for groups, where a self-array subscript
     * narrows the region to one element (see {@link #effectiveLength}).
     */
    private Object decodeAt(SchemaNode n, int off, int len) {
        if (!(n instanceof SchemaField f)) {
            byte[] slice = Arrays.copyOfRange(data, off, off + len);
            return new String(slice, layout.charset());
        }
        byte[] slice = Arrays.copyOfRange(data, off, off + f.getByteLength());
        if (f.isEdited()) {
            return decodeEditedNumeric(new String(slice, layout.charset()));
        }
        return RecordConverter.decodeField(f, slice, layout.charset());
    }

    /**
     * Encode to an explicit byte offset instead of the field's schema offset — {@link
     * #setRaw(String, Object)} minus the name lookup.
     */
    private void encodeAt(SchemaNode n, Object value, int off) {
        encodeAt(n, value, off, n.getByteLength());
    }

    /**
     * Length-carrying variant — {@code len} matters only for groups, where a self-array subscript
     * narrows the write to one element (see {@link #effectiveLength}); otherwise `INITIALIZE
     * G(row)` would wipe the whole array.
     */
    private void encodeAt(SchemaNode n, Object value, int off, int len) {
        if (value instanceof String str && fillIfFigurativeAt(n, str, off, len)) {
            return;
        }
        if (!(n instanceof SchemaField field)) {
            // Group write at an offset. MOVE SPACE/SPACES TO group(idx) arrives as
            // a one-char ASCII blank literal, so the empty string and " " both mean
            // SPACES and take the kind-aware clear (alpha 0x20, PIC N 0x8140
            // full-width pairs, numerics '0' / sign nibble). Otherwise PIC N leaves
            // inside an OCCURS group would end up as 0x20 bytes that read back as
            // broken half-width characters instead of `　` (U+3000).
            String s = value == null ? "" : value.toString();
            if (s.isEmpty() || " ".equals(s)) {
                if (n instanceof SchemaGroup g && len < g.getByteLength()) {
                    clearGroupElementAt(g, off, len);
                } else {
                    clearGroupLeavesAt((SchemaGroup) n, off);
                }
                return;
            }
            byte[] enc = s.getBytes(layout.charset());
            byte[] slice = new byte[len];
            Arrays.fill(slice, (byte) 0x20);
            System.arraycopy(enc, 0, slice, 0, Math.min(enc.length, slice.length));
            System.arraycopy(slice, 0, data, off, len);
            return;
        }
        if (field.isEdited() && value != null) {
            BigDecimal num;
            if (value instanceof BigDecimal bd) {
                num = bd;
            } else if (value instanceof Number nn) num = BigDecimal.valueOf(nn.longValue());
            else {
                try {
                    num = new BigDecimal(value.toString().trim());
                } catch (Exception e) {
                    num = BigDecimal.ZERO;
                }
            }
            String formatted =
                    EditPictureFormatter.format(
                            field.getEditFormat(),
                            field.getEditIntegerDigits(),
                            field.isEditInsertCommas(),
                            field.isEditHasTrailingNine(),
                            num,
                            field.getEditSlashAfter(),
                            field.getScale(),
                            field.getEditCommaAfter());
            byte[] bytes = formatted.getBytes(layout.charset());
            int width = field.getByteLength();
            byte[] out = new byte[width];
            Arrays.fill(out, (byte) 0x20);
            System.arraycopy(bytes, 0, out, 0, Math.min(bytes.length, width));
            System.arraycopy(out, 0, data, off, width);
            return;
        }
        byte[] enc = RecordConverter.encodeField(field, value, layout.charset());
        System.arraycopy(enc, 0, data, off, Math.min(enc.length, field.getByteLength()));
    }

    private boolean fillIfFigurativeAt(SchemaNode n, String value, int off, int len) {
        if (value == null || value.length() != 1) {
            return false;
        }
        char c = value.charAt(0);
        byte fill;
        if (c == 'ÿ') {
            fill = (byte) 0xFF;
        } else if (c == '\u0000') fill = (byte) 0x00;
        else return false;
        Arrays.fill(data, off, off + len, fill);
        return true;
    }

    /**
     * Clears ONE element ({@code len} bytes) of a self-subscripted OCCURS group at {@code off}: the
     * bulk 0x20 sweep of that element, then the kind-aware numeric/PIC-N passes over the children
     * shifted onto it (child offsets are relative to element[0]; the delta relocates them). Unlike
     * {@link #clearGroupLeavesAt}, the group's own OCCURS dimension stays unexpanded — only the
     * addressed row clears, while OCCURS nested inside the row still expand through the shifted
     * walkers.
     */
    private void clearGroupElementAt(SchemaGroup g, int off, int len) {
        Arrays.fill(data, off, off + len, (byte) 0x20);
        int delta = off - g.getByteOffset();
        for (SchemaNode c : g.children()) {
            clearNumericLeavesShifted(c, delta, false);
            clearDbcsLeavesShifted(c, delta);
        }
    }

    private SchemaNode requireNode(String fieldName) {
        SchemaNode n = layout.get(fieldName);
        if (n == null) {
            throw new IllegalArgumentException(
                    "Field not in layout " + layout.name() + ": " + fieldName);
        }
        return n;
    }

    /* ── Subscripted overrides ────────────────────────────────────────── */

    @Override
    public int getInt(String name, int... subs) {
        if (subs == null || subs.length == 0) {
            return getInt(name);
        }
        Object v = decodeAt(requireNode(name), effectiveOffset(requireNode(name), subs));
        if (v == null) {
            return 0;
        }
        if (v instanceof Number nn) {
            return nn.intValue();
        }
        if (v instanceof BigDecimal bd) {
            return bd.intValue();
        }
        String s = v.toString().trim();
        return s.isEmpty() ? 0 : Integer.parseInt(s);
    }

    @Override
    public long getLong(String name, int... subs) {
        if (subs == null || subs.length == 0) {
            return getLong(name);
        }
        Object v = decodeAt(requireNode(name), effectiveOffset(requireNode(name), subs));
        if (v == null) {
            return 0L;
        }
        if (v instanceof Number nn) {
            return nn.longValue();
        }
        if (v instanceof BigDecimal bd) {
            return bd.longValue();
        }
        String s = v.toString().trim();
        return s.isEmpty() ? 0L : Long.parseLong(s);
    }

    @Override
    public String getString(String name, int... subs) {
        if (subs == null || subs.length == 0) {
            return getString(name);
        }
        SchemaNode n = requireNode(name);
        int off = effectiveOffset(n, subs);
        // edited fields hand back the raw bytes — commas, sign and blanks intact
        if (n instanceof SchemaField field && field.isEdited()) {
            byte[] slice = Arrays.copyOfRange(data, off, off + n.getByteLength());
            return new String(slice, layout.charset());
        }
        Object v = decodeAt(n, off, effectiveLength(n, subs));
        return v == null ? "" : v.toString();
    }

    @Override
    public BigDecimal getDecimal(String name, int... subs) {
        if (subs == null || subs.length == 0) {
            return getDecimal(name);
        }
        Object v = decodeAt(requireNode(name), effectiveOffset(requireNode(name), subs));
        if (v == null) {
            return BigDecimal.ZERO;
        }
        if (v instanceof BigDecimal bd) {
            return bd;
        }
        if (v instanceof Number) {
            return new BigDecimal(v.toString());
        }
        String s = v.toString().trim();
        return s.isEmpty() ? BigDecimal.ZERO : new BigDecimal(s);
    }

    @Override
    public double getDouble(String name, int... subs) {
        return getDecimal(name, subs).doubleValue();
    }

    @Override
    public boolean getBoolean(String name, int... subs) {
        if (subs == null || subs.length == 0) {
            return getBoolean(name);
        }
        String s = getString(name, subs).trim();
        return !s.isEmpty() && !"0".equals(s) && !"false".equalsIgnoreCase(s);
    }

    @Override
    public String groupToString(String name, int... subs) {
        if (subs == null || subs.length == 0) {
            return groupToString(name);
        }
        SchemaNode n = layout.get(name);
        if (n == null) {
            return "";
        }
        if (n instanceof SchemaField) {
            return getString(name, subs);
        }
        int off = effectiveOffset(n, subs);
        byte[] slice = Arrays.copyOfRange(data, off, off + effectiveLength(n, subs));
        return new String(slice, layout.charset());
    }

    @Override
    public void setInt(String name, int v, int... subs) {
        if (subs == null || subs.length == 0) {
            setInt(name, v);
            return;
        }
        SchemaNode n = requireNode(name);
        encodeAt(n, v, effectiveOffset(n, subs));
    }

    @Override
    public void setLong(String name, long v, int... subs) {
        if (subs == null || subs.length == 0) {
            setLong(name, v);
            return;
        }
        SchemaNode n = requireNode(name);
        encodeAt(n, v, effectiveOffset(n, subs));
    }

    @Override
    public void setString(String name, String v, int... subs) {
        if (subs == null || subs.length == 0) {
            setString(name, v);
            return;
        }
        SchemaNode n = requireNode(name);
        encodeAt(n, v, effectiveOffset(n, subs));
    }

    @Override
    public void setDecimal(String name, BigDecimal v, int... subs) {
        if (subs == null || subs.length == 0) {
            setDecimal(name, v);
            return;
        }
        SchemaNode n = requireNode(name);
        encodeAt(n, v, effectiveOffset(n, subs));
    }

    @Override
    public void setDouble(String name, double v, int... subs) {
        if (subs == null || subs.length == 0) {
            setDouble(name, v);
            return;
        }
        SchemaNode n = requireNode(name);
        encodeAt(n, v, effectiveOffset(n, subs));
    }

    @Override
    public void setBoolean(String name, boolean v, int... subs) {
        if (subs == null || subs.length == 0) {
            setBoolean(name, v);
            return;
        }
        SchemaNode n = requireNode(name);
        encodeAt(n, v ? "1" : "0", effectiveOffset(n, subs));
    }

    @Override
    public void setGroup(String name, String v, int... subs) {
        if (subs == null || subs.length == 0) {
            setGroup(name, v);
            return;
        }
        SchemaNode n = requireNode(name);
        // effectiveLength narrows a self-subscripted OCCURS group to one element:
        // `INITIALIZE G(row)` clears just the addressed row, where a base-length
        // write would have flattened the whole array.
        encodeAt(n, v, effectiveOffset(n, subs), effectiveLength(n, subs));
    }

    /**
     * Element-addressed copy — COBOL {@code MOVE G(a) TO G(b)} where G is itself the OCCURS array.
     * Offset AND length resolve under each side's own subscripts, so exactly one element moves; the
     * subscript-less {@link #copyBytes(String, String)} would copy the whole array from its base
     * offset — a self-copy that changes nothing. Plain nodes work too (no subscripts = full node).
     */
    @Override
    public void copyBytes(String toName, String fromName, int[] toSubs, int[] fromSubs) {
        SchemaNode to = requireNode(toName);
        SchemaNode from = requireNode(fromName);
        int toOff = effectiveOffset(to, toSubs);
        int fromOff = effectiveOffset(from, fromSubs);
        int len = Math.min(effectiveLength(to, toSubs), effectiveLength(from, fromSubs));
        System.arraycopy(data, fromOff, data, toOff, len);
    }
}

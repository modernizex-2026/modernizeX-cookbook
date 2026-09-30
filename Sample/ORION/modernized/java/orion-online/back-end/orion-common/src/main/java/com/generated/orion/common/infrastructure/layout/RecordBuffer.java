package com.generated.orion.common.infrastructure.layout;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Raw byte[] buffer for a single COBOL record. Carries an attached schema ({@link RecordSchema}) so
 * callers get/set by COBOL field name. Replaces the POJO + reflection model.
 *
 * <p>API compatible with the legacy {@code FieldAccessor}: getInt/getLong/getString/... Hot path is
 * reflection-free — only map lookups + System.arraycopy.
 */
public final class RecordBuffer implements FieldBuffer {

    private final RecordSchema layout;
    private byte[] data;

    /**
     * Alias view (COBOL SET ADDRESS OF <this 01> TO ADDRESS OF <field>): when non-null, every byte
     * access is redirected LIVE into the host's storage at {@link #aliasBase}. {@code data} is
     * retained but unused while aliased.
     */
    private RecordBuffer aliasHost;

    private int aliasBase;

    public RecordBuffer(RecordSchema layout) {
        this.layout = layout;
        this.data = newBlank(layout.recordByteLength(), layout.charset());
        clear();
        applyInitialValues(); // apply VALUE clause bytes (e.g. KEIN-TABL lookup data)
    }

    /** Apply schema VALUE bytes to the buffer at construction time. */
    private void applyInitialValues() {
        for (LayoutField f : layout.leaves()) {
            String iv = f.getInitialValue();
            if (iv == null || iv.isEmpty()) {
                continue;
            }
            iv = unwrapLiteral(iv);
            int offset = f.getByteOffset();
            int length = f.getByteLength();
            if (length <= 0 || offset < 0 || offset + length > layout.recordByteLength()) {
                continue;
            }
            byte[] valueBytes = figurativeBytes(iv, length, f, layout.charset());
            if (valueBytes == null) {
                try {
                    valueBytes = iv.getBytes(layout.charset());
                } catch (Exception ignore) {
                    continue;
                }
                if (f.getKind() == LayoutField.Kind.NUM
                        && f.getUsage() != LayoutField.Usage.DISPLAY) {
                    // COMP / COMP-3 / BINARY numeric VALUE must be encoded into its binary form,
                    // NOT stored as ASCII digits: VALUE 7 on PIC S9(4) COMP → 0x0007, not '7'
                    // (0x37)
                    // which the byte model would read back as 14080. Covers packed-decimal too.
                    try {
                        valueBytes =
                                RecordCodec.encodeField(
                                        f, new java.math.BigDecimal(iv.trim()), layout.charset());
                    } catch (Exception ignore) {
                        /* not a plain numeric literal → keep charset bytes */
                    }
                } else if (f.getKind() == LayoutField.Kind.NUM
                        && f.getUsage() == LayoutField.Usage.DISPLAY) {
                    boolean allDigits = valueBytes.length > 0;
                    for (byte b : valueBytes) {
                        if (b < '0' || b > '9') {
                            allDigits = false;
                            break;
                        }
                    }
                    if (allDigits && valueBytes.length < length) {
                        // COBOL DISPLAY numeric VALUE is right-justified, zero-padded to the PIC
                        // width
                        // (PIC 9(02) VALUE 6 → "06", not left-justified "6" leaving a cleared '0' →
                        // the field would read "60").
                        byte[] padded = new byte[length];
                        java.util.Arrays.fill(padded, (byte) '0');
                        System.arraycopy(
                                valueBytes,
                                0,
                                padded,
                                length - valueBytes.length,
                                valueBytes.length);
                        valueBytes = padded;
                    } else if (!allDigits) {
                        // A signed or decimal literal — PIC S9V9(4) VALUE +0.0200, PIC 9V99 VALUE
                        // 1.20 —
                        // must be encoded through the picture (scale, sign) exactly as a MOVE
                        // would;
                        // storing the literal's own bytes leaves '+' / '.' in a DISPLAY numeric
                        // field
                        // and every later read throws NumericValueException (ORION OUFLAG/OUCHGF,
                        // 2026-09-24: 17 tests errored on WS-MIN-DUE-PCT / WS-CO-FACTOR). The batch
                        // engine (RecordImage.applyInitialValues) has always parsed it this way.
                        try {
                            valueBytes =
                                    RecordCodec.encodeField(
                                            f,
                                            new java.math.BigDecimal(iv.trim()),
                                            layout.charset());
                        } catch (Exception ignore) {
                            /* not a numeric literal → keep charset bytes */
                        }
                    }
                }
            }
            int copyLen = Math.min(valueBytes.length, length);
            System.arraycopy(valueBytes, 0, buf(), base() + offset, copyLen);
        }
    }

    /**
     * Resolve a COBOL figurative constant into the byte fill for the target field. Without this,
     * VALUE ZERO on a PIC X(3) field would emit literal bytes 'Z','E','R' (0x5A 0x45 0x52). A
     * REDEFINES alias as PIC 9 then fails to decode as DISPLAY numeric. Returns null when the value
     * isn't a figurative constant (caller encodes as charset bytes).
     */
    private static byte[] figurativeBytes(
            String iv, int length, LayoutField f, java.nio.charset.Charset charset) {
        if (iv == null) {
            return null;
        }
        String up = iv.trim().toUpperCase();
        if ("ZERO".equals(up) || "ZEROS".equals(up) || "ZEROES".equals(up)) {
            if (f.getKind() == LayoutField.Kind.NUM && f.getUsage() == LayoutField.Usage.COMP3) {
                byte[] b = new byte[length];
                if (length > 0) b[length - 1] = 0x0F; // packed-decimal unsigned positive zero
                return b;
            }
            // Số KHÔNG phải COMP-3: để encoder chung lo, vì mỗi USAGE có biểu diễn zero riêng.
            // Trước 2026-08-24 mọi usage ngoài COMP-3 đều bị fill ASCII '0' (0x30) ⇒
            // `PIC S9(9) COMP VALUE ZEROS` (4 byte nhị phân) đọc ra 0x30303030 = 808464432 thay vì
            // 0.
            // Đo được: carddemo có 69 field BINARY/COMP1/COMP2 mang VALUE ZERO* — tức 69 field khởi
            // tạo bằng rác. Chương trình nào TEST một field như vậy trước khi ghi sẽ rẽ nhánh sai.
            // COMP-3 giữ nguyên đường cũ (0x0F) để không đổi byte của cohort đang chạy.
            if (f.getKind() == LayoutField.Kind.NUM && f.getUsage() != LayoutField.Usage.DISPLAY) {
                return RecordCodec.encodeField(f, java.math.BigDecimal.ZERO, charset);
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
            return new byte[length]; // all 0x00
        }
        return null;
    }

    /**
     * Strip COBOL literal wrappers from a VALUE clause text: NC"..." → ... (NATIONAL literal —
     * content) N"..." → ... (NATIONAL literal — alt form) "..." → ... (alphanumeric literal) '...'
     * → ... Otherwise return the input unchanged.
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
        // A bare quote-pair ("" or '') is the COBOL doubled-quote escape for a single quote
        // character (e.g. DFHENTER PIC X VALUE ''''/"''"), NOT an empty literal. Stripping it
        // to "" would clear the field (→ space) and break EIBAID=DFHENTER comparisons.
        if (len == 2 && s.charAt(0) == '"' && s.charAt(1) == '"') {
            return "\"";
        }
        if (len == 2 && s.charAt(0) == '\'' && s.charAt(1) == '\'') {
            return "'";
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

    /**
     * Live backing bytes. While aliased there is no live window over the host array, so callers get
     * an independent copy of the window (in-place mutation of the result would be lost; the runtime
     * only mutates {@code bytes()} on FD record buffers, which are never SET ADDRESS targets).
     */
    public byte[] bytes() {
        return aliasHost == null ? data : toBytes();
    }

    /** Backing array for all byte access — the host's (resolved live) when aliased. */
    private byte[] buf() {
        return aliasHost == null ? data : aliasHost.buf();
    }

    /** Displacement of this record inside {@link #buf()} — 0 unless aliased. */
    private int base() {
        return aliasHost == null ? 0 : aliasHost.base() + aliasBase;
    }

    /**
     * COBOL {@code SET ADDRESS OF <this 01> TO ADDRESS OF <hostField>} — re-point this record onto
     * the host field's bytes as a live view (no copy; both names see the same storage). Fail-fast
     * on anything the overlay cannot express: unknown host field, window overrunning the host
     * record, alias cycle.
     */
    public void aliasTo(RecordBuffer host, String hostFieldName) {
        SchemaNode n;
        int ofIdx = hostFieldName.toUpperCase().indexOf(" OF ");
        if (ofIdx >= 0) {
            n =
                    host.layout.findQualified(
                            hostFieldName.substring(0, ofIdx).trim(),
                            hostFieldName.substring(ofIdx + 4).trim());
        } else {
            n = host.layout.get(hostFieldName);
        }
        if (n == null) {
            throw new IllegalArgumentException(
                    "Field not in layout " + host.layout.name() + ": " + hostFieldName);
        }
        for (RecordBuffer p = host; p != null; p = p.aliasHost) {
            if (p == this) {
                throw new IllegalStateException(
                        "SET ADDRESS cycle: " + layout.name() + " -> " + host.layout.name());
            }
        }
        int len = layout.recordByteLength();
        if (n.getByteOffset() + len > host.layout.recordByteLength()) {
            throw new IllegalStateException(
                    "SET ADDRESS OF "
                            + layout.name()
                            + " ("
                            + len
                            + "B) at "
                            + hostFieldName
                            + " overruns host record "
                            + host.layout.name()
                            + " ("
                            + host.layout.recordByteLength()
                            + "B)");
        }
        this.aliasHost = host;
        this.aliasBase = n.getByteOffset();
    }

    /** Whole-buffer snapshot: an independent copy of this record's bytes. */
    @Override
    public byte[] toBytes() {
        return Arrays.copyOfRange(buf(), base(), base() + layout.recordByteLength());
    }

    /**
     * Whole-buffer restore: replace this record's bytes with an independent copy of {@code src}.
     */
    @Override
    public void fromBytes(byte[] src) {
        setBytes(src == null ? null : src.clone());
    }

    public void setBytes(byte[] bytes) {
        if (aliasHost != null) {
            // Aliased: the storage belongs to the host — copy into the window, never
            // replace the array (replacement would silently detach the overlay).
            if (bytes == null) {
                clear();
                return;
            }
            int winLen = layout.recordByteLength();
            int n = Math.min(bytes.length, winLen);
            System.arraycopy(bytes, 0, buf(), base(), n);
            if (n < winLen) {
                Arrays.fill(buf(), base() + n, base() + winLen, (byte) 0x20);
            }
            return;
        }
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
     * Reset the entire buffer according to each field's USAGE: - DISPLAY numeric → binary zero
     * (0x00) = COBOL uninitialized storage; numerically 0 (decodeDisplay maps 0x00 → digit 0) but
     * NOT a valid digit byte, so an uninitialized field correctly tests IS NUMERIC = false
     * (mainframe zoned-decimal semantics). - COMP-3 numeric → 0x00 bytes + sign nibble 0x0C
     * (positive) / 0x0F (unsigned) - BINARY numeric → 0x00 bytes - ALPHANUMERIC/DBCS → ASCII space
     * (0x20).
     */
    public void clear() {
        Arrays.fill(buf(), base(), base() + layout.recordByteLength(), (byte) 0x20);
        for (LayoutField f : layout.leaves()) {
            if (f.getKind() == LayoutField.Kind.NUM) {
                clearNumericField(f);
            } else if (f.getKind() == LayoutField.Kind.DBCS) {
                // DBCS (PIC N) → MS932 fullwidth-space (0x81 0x40), not ASCII 0x20. COBOL
                // initializes PIC N to fullwidth-space. clear() must cover ALL DBCS leaves
                // (top-level/standalone too — vue's group paths only reached nested DBCS).
                clearDbcsField(f);
            }
        }
    }

    /**
     * OCCURS-aware MS932 fullwidth-space (0x81 0x40) fill for a DBCS (PIC N) leaf. Mirrors {@link
     * #clearNumericField(LayoutField)}'s array-ancestor offset arithmetic so every replicated
     * occurrence is initialized, not just element[0].
     */
    private void clearDbcsField(LayoutField f) {
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
            // Fill 0x81 0x40 pairs; an odd trailing byte (malformed PIC N) stays 0x20 from clear().
            for (int i = 0; i + 1 < length; i += 2) {
                buf()[base() + offset + i] = (byte) 0x81;
                buf()[base() + offset + i + 1] = (byte) 0x40;
            }
        }
    }

    private void clearNumericField(LayoutField f) {
        // OCCURS-aware: leaves inside an OCCURS ancestor group must clear EVERY replicated
        // instance, not just element[0]. Without this, element[1+] keeps the 0x20 space-fill —
        // for COMP-3 OCCURS that leaves lo-nibble 0x0 in the sign-nibble byte, which the codec
        // rejects as "invalid sign nibble" on subsequent reads (surfaced via HDR030 genkaZ000
        // reading a freshly-cleared HZAKEI record's ZAK-ZAIKOSU OCCURS 3).
        //
        // The per-occurrence stride is the OCCURS GROUP's elementSize, NOT the field's own byte
        // length. A field 4 bytes wide inside a 34-byte OCCURS element repeats every 34 bytes,
        // not every 4. This mirrors effectiveOffset(): occ offset = baseOffset + Σ idx_k·elemSize_k
        // over the array ancestors. Using field length as the stride cleared the wrong bytes and
        // left the real occurrences space-filled.
        int baseOffset = f.getByteOffset();
        int length = f.getByteLength();

        // Array ancestor chain (innermost-first here; order is irrelevant for full coverage).
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
            for (SchemaGroup g : arrays) { // mixed-radix decompose → per-dim index
                int idx = rem % g.getOccurs();
                rem /= g.getOccurs();
                delta += idx * g.getElementSize();
            }
            int offset = baseOffset + delta;
            switch (f.getUsage()) {
                case DISPLAY:
                    // Binary zero = COBOL uninitialized DISPLAY numeric: decodes to value 0 but is
                    // NOT a valid digit byte, so IS NUMERIC = false until the field is assigned.
                    Arrays.fill(buf(), base() + offset, base() + offset + length, (byte) 0x00);
                    break;
                case COMP3:
                    {
                        Arrays.fill(
                                buf(), base() + offset, base() + offset + length - 1, (byte) 0x00);
                        if (length > 0) {
                            int signNibble = f.isSigned() ? 0xC : 0xF;
                            buf()[base() + offset + length - 1] = (byte) (0x00 | signNibble);
                        }
                        break;
                    }
                case BINARY:
                case COMP1:
                case COMP2:
                    Arrays.fill(buf(), base() + offset, base() + offset + length, (byte) 0x00);
                    break;
                default:
                    Arrays.fill(buf(), base() + offset, base() + offset + length, (byte) 0x00);
            }
        }
    }

    /* ── Typed getters ─────────────────────────────────────────────── */

    public int getInt(String fieldName) {
        Object v = getRaw(fieldName);
        if (v == null) {
            return 0;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        if (v instanceof BigDecimal) {
            return ((BigDecimal) v).intValue();
        }
        String s = v.toString().trim();
        return s.isEmpty() ? 0 : Integer.parseInt(s);
    }

    public long getLong(String fieldName) {
        Object v = getRaw(fieldName);
        if (v == null) {
            return 0L;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        if (v instanceof BigDecimal) {
            return ((BigDecimal) v).longValue();
        }
        String s = v.toString().trim();
        return s.isEmpty() ? 0L : Long.parseLong(s);
    }

    public String getString(String fieldName) {
        // Edited field READ as alphanumeric → return RAW buffer bytes (commas,
        // signs, leading spaces preserved). MOVE F1-MCAR1 TO L-M1-MCAR1 (edited→PIC X)
        // must copy edit-picture form verbatim. getRaw would de-edit to BigDecimal.
        SchemaNode n = layout.get(fieldName);
        if (n instanceof LayoutField && ((LayoutField) n).isEdited()) {
            byte[] slice =
                    Arrays.copyOfRange(
                            buf(),
                            base() + n.getByteOffset(),
                            base() + n.getByteOffset() + n.getByteLength());
            return stripReplacementChars(new String(slice, layout.charset()));
        }
        Object v = getRaw(fieldName);
        return v == null ? "" : stripReplacementChars(v.toString());
    }

    /**
     * Strip U+FFFD REPLACEMENT CHARACTER from decoded values. PIC N(n) holding ASCII content leaves
     * orphan MS932 lead-bytes in the padding that decode to U+FFFD; the replacement char is never
     * legitimate COBOL data so it's always safe to drop. Trailing spaces are left intact — some
     * COBOL flows preserve padding.
     */
    private static String stripReplacementChars(String s) {
        return s.indexOf('�') < 0 ? s : s.replace("�", "");
    }

    public BigDecimal getDecimal(String fieldName) {
        Object v = getRaw(fieldName);
        if (v == null) {
            return BigDecimal.ZERO;
        }
        if (v instanceof BigDecimal) {
            return (BigDecimal) v;
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
        if (n instanceof LayoutField) {
            return getString(fieldName);
        }
        byte[] slice =
                Arrays.copyOfRange(
                        buf(),
                        base() + n.getByteOffset(),
                        base() + n.getByteOffset() + n.getByteLength());
        return new String(slice, layout.charset());
    }

    /* ── Typed setters ─────────────────────────────────────────────── */

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
     * Set group value. Empty/blank value distributes per-child USAGE (otherwise blind 0x20 fill
     * corrupts COMP-3/BINARY children). Non-empty value byte-copies into the slice.
     */
    public void setGroup(String fieldName, String value) {
        SchemaNode n = layout.get(fieldName);
        if (n == null) {
            return;
        }
        // COBOL figurative-constant sentinel (HIGH-VALUES / LOW-VALUES): byte-fill the group's
        // slice directly. The typed wrapper for groups (setRs1Key, setRs2Key, ...) emits
        // `setGroup("RS1-KEY", HIGH_VALUES)` and MUST byte-fill 0xFF — encoding "ÿ" through
        // MS932 charset substitutes to '?' (0x3F), breaking subsequent HIGH-VALUES compare
        // (loop-exit sentinel in merge-style readRtn → infinite write loop).
        if (value != null && fillIfFigurative(n, value)) {
            return;
        }
        // Treat empty string AND single-char ASCII space as COBOL SPACES → kind-aware clear.
        // The generator emits a single-char " " literal for `MOVE SPACE/SPACES TO group`;
        // without the " ".equals branch, DBCS leaves in the group end up 0x20-byte filled
        // (decodes to half-width spaces) instead of MS932 0x8140 fullwidth-space pairs.
        boolean blank = (value == null || value.isEmpty() || " ".equals(value));
        if (blank && n instanceof SchemaGroup) {
            clearGroupLeaves((SchemaGroup) n);
            return;
        }
        byte[] enc = (value == null ? "" : value).getBytes(layout.charset());
        byte[] slice = new byte[n.getByteLength()];
        Arrays.fill(slice, (byte) 0x20);
        System.arraycopy(enc, 0, slice, 0, Math.min(enc.length, slice.length));
        System.arraycopy(slice, 0, buf(), base() + n.getByteOffset(), n.getByteLength());
    }

    /**
     * Clear all leaf fields within a group per their individual USAGE. DBCS leaves get 0x8140
     * (MS932 full-width space) pairs after the bulk 0x20 sweep, so kanji-field comparisons against
     * `　` (U+3000) succeed.
     */
    private void clearGroupLeaves(SchemaGroup g) {
        Arrays.fill(
                buf(),
                base() + g.getByteOffset(),
                base() + g.getByteOffset() + g.getByteLength(),
                (byte) 0x20);
        clearNumericLeaves(g);
        clearDbcsLeaves(g);
    }

    private void clearNumericLeaves(SchemaNode node) {
        if (node instanceof LayoutField) {
            LayoutField f = (LayoutField) node;
            if (f.getKind() == LayoutField.Kind.NUM) {
                clearNumericField(f);
            }
        } else if (node instanceof SchemaGroup) {
            for (SchemaNode c : ((SchemaGroup) node).children()) {
                clearNumericLeaves(c);
            }
        }
    }

    /** Walk DBCS leaves and fill with MS932 full-width-space bytes (0x81 0x40 pair). */
    private void clearDbcsLeaves(SchemaNode node) {
        if (node instanceof LayoutField) {
            LayoutField f = (LayoutField) node;
            if (f.getKind() == LayoutField.Kind.DBCS) {
                int off = f.getByteOffset();
                int len = f.getByteLength();
                // Fill pairs of (0x81, 0x40). If len is odd (malformed PIC N), trailing byte stays
                // 0x20.
                for (int i = 0; i + 1 < len; i += 2) {
                    buf()[base() + off + i] = (byte) 0x81;
                    buf()[base() + off + i + 1] = (byte) 0x40;
                }
            }
        } else if (node instanceof SchemaGroup) {
            for (SchemaNode c : ((SchemaGroup) node).children()) {
                clearDbcsLeaves(c);
            }
        }
    }

    /**
     * OCCURS-aware kind-aware clear of a group at an explicit offset. Used by {@link #encodeAt}
     * when {@code MOVE SPACE/SPACES TO group(idx)} fires — the OCCURS subscript shifts the target
     * slice away from g.getByteOffset(), so we cannot delegate to {@link
     * #clearGroupLeaves(SchemaGroup)} (which only writes at the layout's static base offset).
     * Mirrors that method's three passes: 1) bulk 0x20 sweep, 2) per-leaf numeric usage clear ('0'
     * / 0x00 / sign nibble), 3) per-leaf DBCS 0x8140 fullwidth-space pair fill.
     */
    private void clearGroupLeavesAt(SchemaGroup g, int off) {
        Arrays.fill(buf(), base() + off, base() + off + g.getByteLength(), (byte) 0x20);
        int delta = off - g.getByteOffset();
        clearNumericLeavesShifted(g, delta);
        clearDbcsLeavesShifted(g, delta);
    }

    private void clearNumericLeavesShifted(SchemaNode node, int delta) {
        if (node instanceof LayoutField) {
            LayoutField f = (LayoutField) node;
            if (f.getKind() == LayoutField.Kind.NUM) {
                int off = f.getByteOffset() + delta;
                int len = f.getByteLength();
                switch (f.getUsage()) {
                    case DISPLAY:
                        // Binary zero = uninitialized DISPLAY numeric (see clearNumericField):
                        // value 0,
                        // not a valid digit byte → IS NUMERIC = false until assigned.
                        Arrays.fill(buf(), base() + off, base() + off + len, (byte) 0x00);
                        break;
                    case COMP3:
                        {
                            Arrays.fill(buf(), base() + off, base() + off + len - 1, (byte) 0x00);
                            if (len > 0) {
                                int signNibble = f.isSigned() ? 0xC : 0xF;
                                buf()[base() + off + len - 1] = (byte) (0x00 | signNibble);
                            }
                            break;
                        }
                    case BINARY:
                    case COMP1:
                    case COMP2:
                        Arrays.fill(buf(), base() + off, base() + off + len, (byte) 0x00);
                        break;
                    default:
                        Arrays.fill(buf(), base() + off, base() + off + len, (byte) 0x00);
                }
            }
        } else if (node instanceof SchemaGroup) {
            for (SchemaNode c : ((SchemaGroup) node).children()) {
                clearNumericLeavesShifted(c, delta);
            }
        }
    }

    private void clearDbcsLeavesShifted(SchemaNode node, int delta) {
        if (node instanceof LayoutField) {
            LayoutField f = (LayoutField) node;
            if (f.getKind() == LayoutField.Kind.DBCS) {
                int off = f.getByteOffset() + delta;
                int len = f.getByteLength();
                for (int i = 0; i + 1 < len; i += 2) {
                    buf()[base() + off + i] = (byte) 0x81;
                    buf()[base() + off + i + 1] = (byte) 0x40;
                }
            }
        } else if (node instanceof SchemaGroup) {
            for (SchemaNode c : ((SchemaGroup) node).children()) {
                clearDbcsLeavesShifted(c, delta);
            }
        }
    }

    /* ── Byte-level group operations ──────────────────────────────────── */

    @Override
    public byte[] sliceBytes(String fieldName) {
        SchemaNode n = layout.get(fieldName);
        if (n == null) {
            throw new IllegalArgumentException(
                    "Field not in layout " + layout.name() + ": " + fieldName);
        }
        return Arrays.copyOfRange(
                buf(), base() + n.getByteOffset(), base() + n.getByteOffset() + n.getByteLength());
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
        System.arraycopy(src, 0, buf(), base() + n.getByteOffset(), len);
    }

    @Override
    public void copyBytes(String toName, String fromName) {
        writeBytes(toName, sliceBytes(fromName));
    }

    /* ── Byte-level figurative constant checks ───────────────────────── */
    // Per COBOL spec: HIGH-VALUE/LOW-VALUE/SPACE/ZERO fill field byte-by-byte to full
    // storage width (NOT 1-char). Compare = check every byte in the field's slice.
    // ASCII (MS932): 0xFF / 0x00 / 0x20 / '0'(0x30).

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

    // Byte-level fills for MOVE HIGH-VALUES / LOW-VALUES. setString("ÿ") would route
    // through MS932 which substitutes 0xFF→'?' (0x3F); direct byte fill bypasses that.
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
            buf()[base() + off + i] = b;
        }
    }

    /**
     * ZEROS check is usage-aware: DISPLAY → byte '0' (0x30); COMP-3/BINARY → 0x00 bytes (with
     * COMP-3 last-byte sign nibble 0xA-0xF accepted as positive zero sign).
     */
    @Override
    public boolean isAllZeros(String fieldName) {
        SchemaNode n = layout.get(fieldName);
        if (n == null) {
            return false;
        }
        if (n instanceof LayoutField) {
            LayoutField f = (LayoutField) n;
            switch (f.getUsage()) {
                case DISPLAY:
                    return allDisplayZero(fieldName);
                case BINARY:
                case COMP1:
                case COMP2:
                    return allBytesEqual(fieldName, (byte) 0x00);
                case COMP3:
                    {
                        // All digit nibbles = 0; last byte: hi=0, lo=valid-sign-nibble
                        int off = n.getByteOffset();
                        int len = n.getByteLength();
                        if (len <= 0) {
                            return false;
                        }
                        for (int i = 0; i < len - 1; i++) {
                            if (buf()[base() + off + i] != 0) return false;
                        }
                        int last = buf()[base() + off + len - 1] & 0xFF;
                        int hi = (last >>> 4) & 0xF;
                        int lo = last & 0xF;
                        return hi == 0 && lo >= 0xA; // any valid sign nibble
                    }
                default:
                    return false;
            }
        }
        // Group: check char '0' across the whole region (rare; COBOL ZEROS on group means numeric
        // subgroup)
        return allBytesEqual(fieldName, (byte) '0');
    }

    /** Parse a trailing OCCURS subscript "NAME(1)" / "NAME(1,2)" → {1} / {1,2}; {} if none. */
    private static int[] subscriptOf(String fieldName) {
        int open = fieldName.lastIndexOf(40); // 40 = '('
        if (open <= 0 || fieldName.charAt(fieldName.length() - 1) != 41)
            return new int[0]; // 41 = ')'
        String inner = fieldName.substring(open + 1, fieldName.length() - 1);
        if (inner.indexOf(':') >= 0) return new int[0]; // reference modification, not a subscript
        String[] parts = inner.split(",");
        int[] subs = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                subs[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                return new int[0];
            }
        }
        return subs;
    }

    private static String stripSubscript(String fieldName) {
        return subscriptOf(fieldName).length == 0
                ? fieldName
                : fieldName.substring(0, fieldName.lastIndexOf(40)).trim(); // 40 = '('
    }

    private boolean allBytesEqual(String fieldName, byte expected) {
        int[] subs = subscriptOf(fieldName);
        SchemaNode n = layout.get(subs.length > 0 ? stripSubscript(fieldName) : fieldName);
        if (n == null) {
            return false;
        }
        int off = subs.length > 0 ? effectiveOffset(n, subs) : n.getByteOffset();
        int len = n.getByteLength();
        if (len <= 0) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (buf()[base() + off + i] != expected) {
                return false;
            }
        }
        return true;
    }

    /**
     * DISPLAY-numeric ZEROS test: every byte is zero-equivalent — digit '0' (0x30), binary zero
     * (0x00 = uninitialized storage), or space (0x20). Mirrors decodeDisplay, which maps all three
     * to numeric value 0, so {@code IF X = ZEROS} agrees with {@code value(X) == 0} for a
     * never-assigned (binary-zero) field as well as an explicit MOVE 0. Without this, the 0x00
     * uninitialized fill would make `= ZEROS` falsely false.
     */
    private boolean allDisplayZero(String fieldName) {
        int[] subs = subscriptOf(fieldName);
        SchemaNode n = layout.get(subs.length > 0 ? stripSubscript(fieldName) : fieldName);
        if (n == null) {
            return false;
        }
        int off = subs.length > 0 ? effectiveOffset(n, subs) : n.getByteOffset();
        int len = n.getByteLength();
        if (len <= 0) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            int b = buf()[base() + off + i] & 0xFF;
            if (b != '0' && b != 0x00 && b != 0x20) {
                return false;
            }
        }
        return true;
    }

    /**
     * Raw field bytes as a String WITHOUT numeric de-coding — for COBOL class tests (IS NUMERIC /
     * ALPHABETIC), which inspect the actual stored bytes. Unlike getString (which decodes a numeric
     * field to its value, normalizing 0x00/0x20 → '0'), this returns the literal bytes, so an
     * uninitialized DISPLAY numeric field (binary zero) is correctly seen as non-numeric.
     */
    @Override
    public String getRawString(String fieldName) {
        SchemaNode n = layout.get(fieldName);
        if (n == null) {
            throw new IllegalArgumentException(
                    "Field not in layout " + layout.name() + ": " + fieldName);
        }
        byte[] slice =
                Arrays.copyOfRange(
                        buf(),
                        base() + n.getByteOffset(),
                        base() + n.getByteOffset() + n.getByteLength());
        return new String(slice, layout.charset());
    }

    public boolean contains(String fieldName) {
        return layout.get(fieldName) != null;
    }

    /* ── internals ─────────────────────────────────────────────────── */

    private Object getRaw(String fieldName) {
        SchemaNode n = layout.get(fieldName);
        if (n == null) {
            throw new IllegalArgumentException(
                    "Field not in layout " + layout.name() + ": " + fieldName);
        }
        if (!(n instanceof LayoutField)) {
            return groupToString(fieldName);
        }
        LayoutField f = (LayoutField) n;
        byte[] slice =
                Arrays.copyOfRange(
                        buf(),
                        base() + f.getByteOffset(),
                        base() + f.getByteOffset() + f.getByteLength());
        // Edited field read = COBOL "de-edit". Strip filler (' ',','), recover
        // sign from '-' position, parse digits. Avoids RecordCodec.decodeDisplay
        // NumericValueException
        // when caller reads back an edited PIC (e.g. footRtn MOVE F1-MCAR1 TO LM1-MCAR1).
        if (f.isEdited()) {
            String s = new String(slice, layout.charset());
            return decodeEditedNumeric(s);
        }
        return RecordCodec.decodeField(f, slice, layout.charset());
    }

    /**
     * De-edit một field numeric-edited: filler (' ', ',') bị bỏ, '-' ở bất kỳ đâu làm giá trị âm,
     * và dấu '.' ĐẶT LẠI vị trí thập phân — các chữ số sau nó là phần thập phân (" 12.34" → 12.34,
     * " 1,234" → 1234).
     *
     * <p>Bug gen-UT bắt được trên Mini-Project (2026-08-24): bản online BỎ dấu '.' như mọi filler
     * khác, nên phần thập phân bị gom vào phần nguyên ⇒ {@code PIC -,---,---,--9.99} chứa {@code "
     * 1,234.56"} đọc ra {@code 123456} (×100). Nhánh batch ({@code layout/RecordImage}) đã sửa từ
     * trước; bản này là bản fork còn cũ — đây là copy đúng thuật toán ấy, KHÔNG phải quy ước thứ
     * hai. Vị trí thập phân lấy từ CHÍNH ảnh edited (chứ không phải {@code f.getScale()}) vì ảnh do
     * picture của field này sinh ra: khi picture cắt phần thập phân ({@code MOVE V99 TO PIC ZZZ9})
     * thì de-edit phải ra số nguyên.
     *
     * <p>Đường {@code getString} của field edited KHÔNG đi qua đây (nó copy nguyên ảnh byte), nên
     * vòng ghi-rồi-đọc-lại-dạng-chuỗi mà chương trình đang dựa vào không đổi.
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
            } else if (c == '.') afterPoint = true; // chữ số sau đây là phần thập phân
            else if (c >= '0' && c <= '9') {
                digits.append(c);
                if (afterPoint) {
                    fractionDigits++;
                }
            }
            // còn lại (' ', ',', '+', '/', 'B', …) là filler trình bày
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
        // COBOL figurative constants — fill the node's byte slice directly so we don't
        // lose 0xFF/0x00 through charset encode (e.g. MS932 substitutes U+00FF with '?').
        // Applies to BOTH field and group nodes (groups: MOVE HIGH-VALUES TO RS1-KEY).
        if (value instanceof String && fillIfFigurative(n, (String) value)) {
            return;
        }
        if (!(n instanceof LayoutField)) {
            setGroup(fieldName, value == null ? "" : value.toString());
            return;
        }
        LayoutField f = (LayoutField) n;
        // COBOL edit-picture (PIC Z..., PIC ---..9, etc.)
        // MOVE numeric → edited field: apply format rules before byte-write.
        if (f.isEdited() && value != null) {
            java.math.BigDecimal num;
            if (value instanceof java.math.BigDecimal) {
                num = (java.math.BigDecimal) value;
            } else if (value instanceof Number)
                num = java.math.BigDecimal.valueOf(((Number) value).longValue());
            else {
                try {
                    num = new java.math.BigDecimal(value.toString().trim());
                } catch (Exception e) {
                    num = java.math.BigDecimal.ZERO;
                }
            }
            String formatted = NumericEditFormatter.format(f.getEditPicture(), num);
            byte[] bytes = formatted.getBytes(layout.charset());
            // Pad/truncate to exact field byte length
            int width = f.getByteLength();
            byte[] out = new byte[width];
            java.util.Arrays.fill(out, (byte) 0x20);
            System.arraycopy(bytes, 0, out, 0, Math.min(bytes.length, width));
            System.arraycopy(out, 0, buf(), base() + f.getByteOffset(), width);
            return;
        }
        byte[] enc = RecordCodec.encodeField(f, value, layout.charset());
        System.arraycopy(
                enc, 0, buf(), base() + f.getByteOffset(), Math.min(enc.length, f.getByteLength()));
    }

    /**
     * If {@code value} is a single-char COBOL figurative-constant sentinel, fill the entire field
     * slice with the corresponding byte and return true. HIGH-VALUES → 0xFF, LOW-VALUES → 0x00.
     * (SPACES handled by setGroup blank path.)
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
        Arrays.fill(
                buf(),
                base() + n.getByteOffset(),
                base() + n.getByteOffset() + n.getByteLength(),
                fill);
        return true;
    }

    private static byte[] newBlank(int len, java.nio.charset.Charset cs) {
        byte[] buf = new byte[len];
        Arrays.fill(buf, (byte) 0x20);
        return buf;
    }

    @Override
    public String toString() {
        return "RecordBuffer{" + layout.name() + " " + layout.recordByteLength() + "B}";
    }

    /* ── OCCURS subscript support ────────────────────────────────────────
     * Schema v2 emits occurs/elementSize on SchemaGroup. Typed wrappers
     * pass COBOL subscripts as varargs (e.g. `setOutFuriwake(int sub, int v)`
     * → `setInt('OUT-FURIWAKE', v, sub)`).
     *
     * Convention: COBOL subscripts are 1-based. subs is interpreted in
     * outermost-OCCURS-first order (matches typed-wrapper argument order
     * which mirrors COBOL "X(I,J)" → I is the outer index).            */

    /**
     * Strict mode (default): throw on out-of-range subscript — surfaces real defects like OCCURS
     * access with the loop counter still at its initial 0. Legacy ports that rely on silent
     * element[1] clamping set {@code -Dcobol.subscript.lenient=true}.
     */
    private static final boolean LENIENT_SUBSCRIPT =
            Boolean.parseBoolean(System.getProperty("cobol.subscript.lenient", "false"));

    /**
     * Compute physical byte offset of {@code n} when accessed under {@code subs}. Walks ancestor
     * groups, picks those with occurs>1, and adds (sub-1)*elementSize for each matching subscript.
     */
    private static int effectiveOffset(SchemaNode n, int[] subs) {
        if (subs == null || subs.length == 0) {
            return n.getByteOffset();
        }
        java.util.ArrayDeque<SchemaGroup> chain = new java.util.ArrayDeque<>();
        SchemaGroup p = n.getParent();
        while (p != null) {
            if (p.isArray()) chain.addFirst(p); // outermost first
            p = p.getParent();
        }
        int delta = 0;
        int i = 0;
        int limit = Math.min(subs.length, chain.size());
        for (SchemaGroup g : chain) {
            if (i >= limit) {
                break;
            }
            int idx = subs[i] - 1; // COBOL 1-based → 0-based
            // COBOL z/OS ABENDs (S0C4/SSRANGE) on subscript=0. Throw to surface the defect
            // — equivalent runtime behavior. Lenient gate keeps the legacy-port clamp.
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

    /** Decode at a caller-supplied byte offset (bypasses field.byteOffset). */
    private Object decodeAt(SchemaNode n, int off) {
        if (!(n instanceof LayoutField)) {
            byte[] slice =
                    Arrays.copyOfRange(buf(), base() + off, base() + off + n.getByteLength());
            return new String(slice, layout.charset());
        }
        LayoutField f = (LayoutField) n;
        byte[] slice = Arrays.copyOfRange(buf(), base() + off, base() + off + f.getByteLength());
        if (f.isEdited()) {
            return decodeEditedNumeric(new String(slice, layout.charset()));
        }
        return RecordCodec.decodeField(f, slice, layout.charset());
    }

    /**
     * Encode at a caller-supplied byte offset (bypasses field.byteOffset). Mirrors {@link
     * #setRaw(String, Object)} but without name lookup.
     */
    private void encodeAt(SchemaNode n, Object value, int off) {
        if (value instanceof String && fillIfFigurativeAt(n, (String) value, off)) {
            return;
        }
        if (!(n instanceof LayoutField)) {
            // Group write at offset. COBOL `MOVE SPACE/SPACES TO group(idx)` reaches here
            // because the generator emits a single-char ASCII space literal; treat both
            // empty string AND single ASCII space as SPACES → kind-aware clear of leaves
            // (alpha → 0x20, DBCS → 0x8140 fullwidth pair, numeric → '0' / sign nibble).
            // Without this, DBCS leaves inside an OCCURS group would get 0x20 bytes that
            // decode to broken half-width chars instead of expected `　` (U+3000).
            String s = value == null ? "" : value.toString();
            if (s.isEmpty() || " ".equals(s)) {
                clearGroupLeavesAt((SchemaGroup) n, off);
                return;
            }
            byte[] enc = s.getBytes(layout.charset());
            byte[] slice = new byte[n.getByteLength()];
            Arrays.fill(slice, (byte) 0x20);
            System.arraycopy(enc, 0, slice, 0, Math.min(enc.length, slice.length));
            System.arraycopy(slice, 0, buf(), base() + off, n.getByteLength());
            return;
        }
        LayoutField f = (LayoutField) n;
        if (f.isEdited() && value != null) {
            BigDecimal num;
            if (value instanceof BigDecimal) {
                num = (BigDecimal) value;
            } else if (value instanceof Number)
                num = BigDecimal.valueOf(((Number) value).longValue());
            else {
                try {
                    num = new BigDecimal(value.toString().trim());
                } catch (Exception e) {
                    num = BigDecimal.ZERO;
                }
            }
            String formatted = NumericEditFormatter.format(f.getEditPicture(), num);
            byte[] bytes = formatted.getBytes(layout.charset());
            int width = f.getByteLength();
            byte[] out = new byte[width];
            Arrays.fill(out, (byte) 0x20);
            System.arraycopy(bytes, 0, out, 0, Math.min(bytes.length, width));
            System.arraycopy(out, 0, buf(), base() + off, width);
            return;
        }
        byte[] enc = RecordCodec.encodeField(f, value, layout.charset());
        System.arraycopy(enc, 0, buf(), base() + off, Math.min(enc.length, f.getByteLength()));
    }

    private boolean fillIfFigurativeAt(SchemaNode n, String value, int off) {
        if (value == null || value.length() != 1) {
            return false;
        }
        char c = value.charAt(0);
        byte fill;
        if (c == 'ÿ') {
            fill = (byte) 0xFF;
        } else if (c == '\u0000') fill = (byte) 0x00;
        else return false;
        Arrays.fill(buf(), base() + off, base() + off + n.getByteLength(), fill);
        return true;
    }

    private SchemaNode requireNode(String fieldName) {
        SchemaNode n = layout.get(fieldName);
        if (n == null) {
            throw new IllegalArgumentException(
                    "Field not in layout " + layout.name() + ": " + fieldName);
        }
        return n;
    }

    /* ── OCCURS-aware overrides ───────────────────────────────────────── */

    @Override
    public int getInt(String name, int... subs) {
        if (subs == null || subs.length == 0) {
            return getInt(name);
        }
        Object v = decodeAt(requireNode(name), effectiveOffset(requireNode(name), subs));
        if (v == null) {
            return 0;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        if (v instanceof BigDecimal) {
            return ((BigDecimal) v).intValue();
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
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        if (v instanceof BigDecimal) {
            return ((BigDecimal) v).longValue();
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
        // Edited field → return raw buffer bytes (preserve commas/sign/spaces).
        if (n instanceof LayoutField && ((LayoutField) n).isEdited()) {
            byte[] slice =
                    Arrays.copyOfRange(buf(), base() + off, base() + off + n.getByteLength());
            return new String(slice, layout.charset());
        }
        Object v = decodeAt(n, off);
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
        if (v instanceof BigDecimal) {
            return (BigDecimal) v;
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
        if (n instanceof LayoutField) {
            return getString(name, subs);
        }
        int off = effectiveOffset(n, subs);
        byte[] slice = Arrays.copyOfRange(buf(), base() + off, base() + off + n.getByteLength());
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
        encodeAt(n, v, effectiveOffset(n, subs));
    }
}

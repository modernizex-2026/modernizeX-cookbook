package com.sakura.runtime.record;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fully resolved schema of one record: every node carries its byte offset and length. Produced by
 * {@link SchemaLoader#loadFile} and consumed by {@link RecordConverter}.
 */
public final class RecordSchema {

    private final String name;
    private final Charset charset;
    private final SchemaGroup root;

    /** First occurrence of each bare name — sufficient whenever the name is unique. */
    private final Map<String, SchemaNode> byName = new HashMap<>();

    /** All occurrences of each bare name — the basis for "OF" disambiguation. */
    private final Map<String, List<SchemaNode>> byNameAll = new HashMap<>();

    private final List<SchemaField> leaves = new ArrayList<>();
    private final int recordByteLength;

    /**
     * Hitachi WITH CSV records: RawDatasetBase.write() then produces one comma-separated cell per
     * leaf, CRLF-terminated, instead of fixed-width bytes.
     */
    private final boolean csv;

    /**
     * Flavor-B dispatch state: current value per discriminator field. While a value is set, {@link
     * #get(String)} screens out candidates from non-matching variants; a dispatcher without a value
     * keeps all variants visible, first match winning.
     */
    private final Map<String, String> activeVariants = new HashMap<>();

    RecordSchema(String name, Charset charset, SchemaGroup root) {
        this(name, charset, root, false);
    }

    RecordSchema(String name, Charset charset, SchemaGroup root, boolean csv) {
        this.name = name;
        this.charset = charset;
        this.root = root;
        this.csv = csv;
        index(root);
        resolve(root, 0);
        this.recordByteLength = root.byteLength;
    }

    public String name() {
        return name;
    }

    public Charset charset() {
        return charset;
    }

    public SchemaGroup root() {
        return root;
    }

    public int recordByteLength() {
        return recordByteLength;
    }

    public List<SchemaField> leaves() {
        return leaves;
    }

    public boolean csv() {
        return csv;
    }

    /**
     * Name lookup honoring the COBOL "OF" qualifier — "M-SIMEI OF MEISAI" selects the "M-SIMEI"
     * node whose ancestry contains "MEISAI"; a bare name yields its first occurrence.
     *
     * <p>With flavor-B dispatch active (see {@link #setActiveVariant(String, String)}), candidates
     * belonging to variants that contradict the active value are screened out first; when nothing
     * matches, the plain first-occurrence answer applies.
     */
    public SchemaNode get(String fieldName) {
        if (fieldName == null) {
            return null;
        }
        int idx = fieldName.toUpperCase().indexOf(" OF ");
        if (idx < 0) {
            List<SchemaNode> all = byNameAll.get(fieldName);
            if (all == null || all.isEmpty()) {
                return null;
            }
            for (SchemaNode n : all) {
                if (isLiveUnderActiveVariants(n)) return n;
            }
            // nothing visible: without dispatch state this is plain flavor A — take the
            // first occurrence; with dispatch state set, the name is currently hidden.
            return activeVariants.isEmpty() ? all.get(0) : null;
        }
        String field = fieldName.substring(0, idx).trim();
        String qualifier = fieldName.substring(idx + 4).trim();
        return findQualified(field, qualifier);
    }

    /**
     * Node named {@code field} having an ancestor named {@code qualifier}; candidates visible under
     * the active variants take precedence.
     */
    public SchemaNode findQualified(String field, String qualifier) {
        if (field == null) {
            return null;
        }
        List<SchemaNode> candidates = byNameAll.get(field);
        if (candidates == null || candidates.isEmpty()) {
            return byName.get(field);
        }
        if (qualifier == null) {
            for (SchemaNode n : candidates) {
                if (isLiveUnderActiveVariants(n)) return n;
            }
            return candidates.get(0);
        }
        String qUpper = qualifier.toUpperCase();
        // first choice: a visible candidate with the right ancestor
        for (SchemaNode cand : candidates) {
            if (!isLiveUnderActiveVariants(cand)) {
                continue;
            }
            if (hasAncestor(cand, qUpper)) {
                return cand;
            }
        }
        // otherwise: any candidate with the right ancestor (no dispatch state set)
        for (SchemaNode cand : candidates) {
            if (hasAncestor(cand, qUpper)) {
                return cand;
            }
        }
        return candidates.get(0);
    }

    private static boolean hasAncestor(SchemaNode node, String upperAncestorName) {
        SchemaNode anc = node.getParent();
        while (anc != null) {
            if (anc.name != null && anc.name.toUpperCase().equals(upperAncestorName)) {
                return true;
            }
            anc = anc.getParent();
        }
        return false;
    }

    /* ── Flavor-B dispatch — choosing the active variant at runtime ─────── */

    /**
     * Records the current value of a discriminator field; from then on {@link #get(String)} hides
     * candidates in variants that contradict it. A {@code null} value clears the entry and restores
     * default visibility.
     */
    public void setActiveVariant(String discriminatorField, String value) {
        if (discriminatorField == null) {
            return;
        }
        if (value == null) {
            activeVariants.remove(discriminatorField);
        } else activeVariants.put(discriminatorField, value);
    }

    public String getActiveVariant(String discriminatorField) {
        return activeVariants.get(discriminatorField);
    }

    public void clearActiveVariants() {
        activeVariants.clear();
    }

    /**
     * Whether the node is visible under the current dispatch state: on the way up the tree, every
     * variant group whose dispatcher holds an active value must match that value — one mismatch
     * hides the node.
     */
    private boolean isLiveUnderActiveVariants(SchemaNode node) {
        if (activeVariants.isEmpty()) {
            return true;
        }
        SchemaNode cur = node;
        while (cur != null) {
            SchemaGroup p = cur.getParent();
            if (p == null) {
                break;
            }
            if (cur instanceof SchemaGroup g) {
                if (g.isVariant() && p.isDispatcher()) {
                    String activeValue = activeVariants.get(p.getDiscriminatorField());
                    if (activeValue != null && !g.matchesVariant(activeValue)) {
                        return false;
                    }
                }
            }
            cur = p;
        }
        return true;
    }

    /** Whether the schema can resolve {@code fieldName}, bare or qualified. */
    public boolean contains(String fieldName) {
        return get(fieldName) != null;
    }

    /**
     * Column-name → SQL-type map (snake_case keys) for persistence. Only the primary tree is walked
     * — REDEFINES alternatives and FILLERs are skipped. A field under an OCCURS group expands to N
     * columns suffixed {@code _0 .. _{N-1}}; the 0-based convention is shared with
     * RawDatasetBase.extractColumnsFromBuffer and the seed-data column names.
     */
    public Map<String, String> sqlColumnTypes() {
        Map<String, String> m = new LinkedHashMap<>();
        collectColumnTypes(root, 1, m);
        return m;
    }

    private void collectColumnTypes(SchemaNode n, int occursMult, Map<String, String> out) {
        if (n.isRedefines()) {
            return;
        }
        if (n instanceof SchemaField f) {
            String nm = f.getName();
            if (isFillerName(nm)) {
                return;
            }
            String col = toSnake(nm);
            String type = picToSqlType(f);
            if (occursMult > 1) {
                for (int i = 0; i < occursMult; i++) {
                    out.putIfAbsent(col + "_" + i, type);
                }
            } else {
                out.putIfAbsent(col, type);
            }
            return;
        }
        SchemaGroup g = (SchemaGroup) n;
        int innerMult = occursMult * Math.max(1, g.getOccurs());
        for (SchemaNode c : g.children) {
            collectColumnTypes(c, innerMult, out);
        }
    }

    private static boolean isFillerName(String name) {
        if (name == null) {
            return false;
        }
        String up = name.toUpperCase();
        if (up.equals("FILLER")) {
            return true;
        }
        if (up.startsWith("FILLER-") || up.startsWith("FILLER_")) {
            String rest = up.substring(7);
            if (rest.isEmpty()) {
                return true;
            }
            for (int i = 0; i < rest.length(); i++) {
                if (!Character.isDigit(rest.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /* ── construction helpers ──────────────────────────────────────────── */

    private void index(SchemaNode n) {
        if (n.name != null) {
            byName.putIfAbsent(n.name, n);
            byNameAll.computeIfAbsent(n.name, k -> new ArrayList<>()).add(n);
        }
        if (n instanceof SchemaGroup g) {
            for (SchemaNode c : g.children) {
                index(c);
            }
        }
    }

    private int resolve(SchemaNode n, int offset) {
        // Trust the pre-computed geometry only when the schema supplied BOTH length
        // and offset; otherwise derive it from the parent chain. (REDEFINES members
        // often carry a size without an offset — one attribute alone is not enough.)
        boolean hasSchema = n.byteLength > 0 && n.byteOffset >= 0;
        if (!hasSchema) {
            if (n.isRedefines()) {
                SchemaNode target = byName.get(n.redefinesName);
                if (target == null) {
                    throw new IllegalStateException(
                            "REDEFINES target not found: "
                                    + n.redefinesName
                                    + " (node "
                                    + n.name
                                    + ")");
                }
                offset = target.byteOffset;
            }
            n.byteOffset = offset;
        }

        if (n instanceof SchemaGroup g) {
            int childOffset = g.byteOffset;
            int maxEnd = g.byteOffset;
            for (SchemaNode c : g.children) {
                int end = resolve(c, childOffset);
                if (end > maxEnd) {
                    maxEnd = end;
                }
                if (!c.isRedefines()) {
                    childOffset = end;
                }
            }
            if (!hasSchema) {
                g.byteLength = maxEnd - g.byteOffset;
            }
            return g.byteOffset + g.byteLength;
        } else {
            SchemaField f = (SchemaField) n;
            leaves.add(f);
            return f.byteOffset + f.byteLength;
        }
    }

    private static String toSnake(String fieldName) {
        return fieldName.toLowerCase().replace('-', '_');
    }

    private static String picToSqlType(SchemaField pic) {
        switch (pic.getKind()) {
            case ALPHANUMERIC:
                return "VARCHAR(" + pic.getByteLength() + ")";
            case DBCS:
                return "VARCHAR(" + pic.getIntegerDigits() + ")";
            case NUM:
                if (pic.isDecimal()) {
                    return "NUMERIC("
                            + (pic.getIntegerDigits() + pic.getFractionDigits())
                            + ","
                            + pic.getFractionDigits()
                            + ")";
                }
                int d = pic.getIntegerDigits();
                if (d <= 4) {
                    return "SMALLINT";
                }
                if (d <= 9) {
                    return "INTEGER";
                }
                return "BIGINT";
            default:
                return "TEXT";
        }
    }

    /** Tree rendering for diagnostics. */
    public String dump() {
        StringBuilder sb = new StringBuilder();
        dump(root, 0, sb);
        return sb.toString();
    }

    private void dump(SchemaNode n, int indent, StringBuilder sb) {
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        sb.append(n).append('\n');
        if (n instanceof SchemaGroup g) {
            for (SchemaNode c : g.children) {
                dump(c, indent + 1, sb);
            }
        }
    }
}

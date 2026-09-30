package com.generated.orion.common.infrastructure.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Strict deterministic field-name routing across multiple {@link FieldBuffer} instances.
 *
 * <p>COBOL spec semantics: bare name unique → resolve; ambiguous → throw, caller MUST use {@code
 * "FIELD OF GROUP"} qualifier. NO first-wins fallback.
 *
 * <p>Built once at construction from a list of buffers + their layouts. O(1) routing on hot path
 * via {@code uniqueIndex} map; ambiguous lookups walk {@code allIndex} list.
 */
public final class FieldRouter {

    /** Field name → buffer (only set if unique across all buffers). */
    private final Map<String, FieldBuffer> uniqueIndex = new HashMap<>();

    /** Field name → ALL occurrences (every buffer that contains this name). */
    private final Map<String, List<FieldBuffer>> allIndex = new HashMap<>();

    /** Buffer → its RecordSchema (for ancestor walk on OF qualifier). */
    private final Map<FieldBuffer, RecordSchema> bufferLayout = new HashMap<>();

    private FieldRouter() {}

    /**
     * Build a router from buffer↔layout pairs. Each buffer's layout is scanned: all node names
     * indexed.
     */
    public static FieldRouter buildFor(
            List<? extends FieldBuffer> buffers, List<RecordSchema> layouts) {
        if (buffers.size() != layouts.size()) {
            throw new IllegalArgumentException("buffers/layouts size mismatch");
        }
        FieldRouter r = new FieldRouter();
        for (int i = 0; i < buffers.size(); i++) {
            FieldBuffer buf = buffers.get(i);
            RecordSchema lay = layouts.get(i);
            r.bufferLayout.put(buf, lay);
            indexLayout(lay.root(), buf, r.allIndex);
        }
        // Build uniqueIndex: name appears in exactly 1 buffer (regardless of how many nodes per
        // buffer)
        for (Map.Entry<String, List<FieldBuffer>> e : r.allIndex.entrySet()) {
            // Distinct buffers only
            FieldBuffer first = null;
            boolean unique = true;
            for (FieldBuffer b : e.getValue()) {
                if (first == null) {
                    first = b;
                } else if (b != first) {
                    unique = false;
                    break;
                }
            }
            if (unique && first != null) {
                r.uniqueIndex.put(e.getKey(), first);
            }
        }
        return r;
    }

    /** Convenience overload for single-buffer (FD scope). */
    public static FieldRouter buildFor(FieldBuffer buffer, RecordSchema layout) {
        List<FieldBuffer> bs = new ArrayList<>();
        bs.add(buffer);
        List<RecordSchema> ls = new ArrayList<>();
        ls.add(layout);
        return buildFor(bs, ls);
    }

    private static void indexLayout(
            SchemaNode node, FieldBuffer buf, Map<String, List<FieldBuffer>> out) {
        if (node.getName() != null) {
            out.computeIfAbsent(node.getName(), k -> new ArrayList<>()).add(buf);
        }
        if (node instanceof SchemaGroup g) {
            for (SchemaNode c : g.children()) {
                indexLayout(c, buf, out);
            }
        }
    }

    /**
     * Strip a trailing OCCURS subscript embedded in the name (e.g. "WS-EACH-CARD(1)" →
     * "WS-EACH-CARD") so routing resolves the base field; the subscript is applied later by the
     * buffer via effectiveOffset. Leaves reference-modification "(s:l)" untouched.
     */
    private static String stripSubscript(String name) {
        int open = name.lastIndexOf(40); // 40 = '('
        if (open <= 0 || name.charAt(name.length() - 1) != 41) return name; // 41 = ')'
        String inner = name.substring(open + 1, name.length() - 1);
        if (inner.indexOf(':') >= 0) return name; // reference modification, not a subscript
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c != ',' && c != ' ' && (c < '0' || c > '9')) {
                return name;
            }
        }
        return name.substring(0, open).trim();
    }

    /** Strict routing: bare unique → buffer, OF qualifier → walk ancestor, else throw. */
    public FieldBuffer route(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Field name null/empty");
        }
        name = stripSubscript(name);
        // OF qualifier form
        int idx = name.toUpperCase().indexOf(" OF ");
        if (idx >= 0) {
            String field = name.substring(0, idx).trim();
            String qual = name.substring(idx + 4).trim();
            return resolveQualified(field, qual);
        }
        // Bare name
        FieldBuffer unique = uniqueIndex.get(name);
        if (unique != null) {
            return unique;
        }
        List<FieldBuffer> all = allIndex.get(name);
        if (all == null || all.isEmpty()) {
            throw new IllegalArgumentException("Field not in any scope: " + name);
        }
        // Ambiguous — COBOL spec: must qualify
        throw new IllegalStateException(
                "Field '"
                        + name
                        + "' ambiguous — found in multiple buffers."
                        + " Use 'name OF qualifier' to disambiguate (COBOL semantics).");
    }

    /** Returns true if {@link #route} would succeed on this name (no exception). */
    public boolean canRoute(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        name = stripSubscript(name);
        int idx = name.toUpperCase().indexOf(" OF ");
        if (idx >= 0) {
            try {
                resolveQualified(name.substring(0, idx).trim(), name.substring(idx + 4).trim());
                return true;
            } catch (RuntimeException e) {
                return false;
            }
        }
        return uniqueIndex.containsKey(name);
    }

    /**
     * Find a buffer whose layout has node named {@code field} with ancestor named {@code
     * qualifier}.
     */
    private FieldBuffer resolveQualified(String field, String qualifier) {
        if (field == null || field.isEmpty()) {
            throw new IllegalArgumentException("Qualified field part empty: " + field);
        }
        List<FieldBuffer> candidates = allIndex.get(field);
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException(
                    "Field not found: " + field + " (OF " + qualifier + ")");
        }
        if (qualifier == null || qualifier.isEmpty()) {
            // No qualifier given — fall through to bare resolution
            return route(field);
        }
        String qUpper = qualifier.toUpperCase();
        for (FieldBuffer buf : candidates) {
            RecordSchema lay = bufferLayout.get(buf);
            if (lay != null) {
                SchemaNode node = findNodeNamed(lay.root(), field);
                if (node != null) {
                    // Walk ancestors looking for qualifier
                    SchemaNode anc = node.getParent();
                    while (anc != null) {
                        if (anc.getName() != null && anc.getName().toUpperCase().equals(qUpper)) {
                            return buf;
                        }
                        anc = anc.getParent();
                    }
                    // Also check if the buffer's own root has the qualifier name
                    if (lay.root().getName() != null
                            && lay.root().getName().toUpperCase().equals(qUpper)) {
                        return buf;
                    }
                }
            }
        }
        throw new IllegalArgumentException(
                "Qualifier '" + qualifier + "' does not match any ancestor of '" + field + "'");
    }

    private static SchemaNode findNodeNamed(SchemaNode root, String name) {
        if (name.equals(root.getName())) {
            return root;
        }
        if (root instanceof SchemaGroup g) {
            for (SchemaNode c : g.children()) {
                SchemaNode r = findNodeNamed(c, name);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }
}

package com.sakura.runtime.record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic name-to-buffer resolution over a set of {@link FieldStore}s.
 *
 * <p>Applies the COBOL reference rules strictly: a bare name held by exactly one buffer resolves; a
 * name present in several buffers is rejected until the caller qualifies it as {@code "FIELD OF
 * GROUP"}. There is no first-match fallback.
 *
 * <p>All indexes are computed once up front. The common case is a single map hit; only qualified
 * references walk the candidate list.
 */
public final class FieldResolver {

    /** Names owned by exactly one buffer — the fast path. */
    private final Map<String, FieldStore> uniqueIndex = new HashMap<>();

    /** Every buffer a name occurs in — consulted by qualified references. */
    private final Map<String, List<FieldStore>> allIndex = new HashMap<>();

    /** Schema of each buffer, needed to climb ancestors for an OF qualifier. */
    private final Map<FieldStore, RecordSchema> bufferLayout = new HashMap<>();

    private FieldResolver() {}

    /**
     * Indexes every node name of each buffer/schema pair and derives the unique-name fast path from
     * the result.
     */
    public static FieldResolver buildFor(
            List<? extends FieldStore> buffers, List<RecordSchema> layouts) {
        if (buffers.size() != layouts.size()) {
            throw new IllegalArgumentException("buffers/layouts size mismatch");
        }
        FieldResolver r = new FieldResolver();
        for (int i = 0; i < buffers.size(); i++) {
            FieldStore buf = buffers.get(i);
            RecordSchema lay = layouts.get(i);
            r.bufferLayout.put(buf, lay);
            indexLayout(lay.root(), buf, r.allIndex);
        }
        // A name earns the fast path when all of its occurrences sit in one buffer
        for (Map.Entry<String, List<FieldStore>> e : r.allIndex.entrySet()) {
            // compare buffer identity, not occurrence count
            FieldStore first = null;
            boolean unique = true;
            for (FieldStore b : e.getValue()) {
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

    /** Single-buffer variant, used for file records. */
    public static FieldResolver buildFor(FieldStore buffer, RecordSchema layout) {
        List<FieldStore> bs = new ArrayList<>();
        bs.add(buffer);
        List<RecordSchema> ls = new ArrayList<>();
        ls.add(layout);
        return buildFor(bs, ls);
    }

    private static void indexLayout(
            SchemaNode node, FieldStore buf, Map<String, List<FieldStore>> out) {
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
     * Resolves a name to its owning buffer: unique bare names hit the index, {@code "FIELD OF
     * GROUP"} walks the ancestry, anything else throws.
     */
    public FieldStore route(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Field name null/empty");
        }
        // qualified reference
        int idx = name.toUpperCase().indexOf(" OF ");
        if (idx >= 0) {
            String field = name.substring(0, idx).trim();
            String qual = name.substring(idx + 4).trim();
            return resolveQualified(field, qual);
        }
        // unqualified reference
        FieldStore unique = uniqueIndex.get(name);
        if (unique != null) {
            return unique;
        }
        List<FieldStore> all = allIndex.get(name);
        if (all == null || all.isEmpty()) {
            throw new IllegalArgumentException("Field not in any scope: " + name);
        }
        // several buffers hold this name — COBOL requires qualification
        throw new IllegalStateException(
                "Field '"
                        + name
                        + "' ambiguous — found in multiple buffers."
                        + " Use 'name OF qualifier' to disambiguate (COBOL semantics).");
    }

    /** Whether {@link #route} would resolve this name without throwing. */
    public boolean canRoute(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
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
     * Locates the buffer in which {@code field} has an ancestor (or record root) named {@code
     * qualifier}.
     */
    private FieldStore resolveQualified(String field, String qualifier) {
        if (field == null || field.isEmpty()) {
            throw new IllegalArgumentException("Qualified field part empty: " + field);
        }
        List<FieldStore> candidates = allIndex.get(field);
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException(
                    "Field not found: " + field + " (OF " + qualifier + ")");
        }
        if (qualifier == null || qualifier.isEmpty()) {
            // empty qualifier — treat as an unqualified reference
            return route(field);
        }
        String qUpper = qualifier.toUpperCase();
        for (FieldStore buf : candidates) {
            RecordSchema lay = bufferLayout.get(buf);
            if (lay != null) {
                SchemaNode node = findNodeNamed(lay.root(), field);
                if (node != null) {
                    // climb the parent chain until the qualifier matches
                    SchemaNode anc = node.getParent();
                    while (anc != null) {
                        if (anc.getName() != null && anc.getName().toUpperCase().equals(qUpper)) {
                            return buf;
                        }
                        anc = anc.getParent();
                    }
                    // the record root itself may carry the qualifier name
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

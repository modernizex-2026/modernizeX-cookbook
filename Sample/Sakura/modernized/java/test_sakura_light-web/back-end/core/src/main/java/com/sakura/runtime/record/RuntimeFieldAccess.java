package com.sakura.runtime.record;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Runtime field access by COBOL data name, resolved through {@link FieldResolver}. The generated
 * per-program accessor subclasses hand in their buffers via {@link #register}.
 *
 * <p>{@code register} takes {@link RecordImage} (a file record) as well as {@link StorageImage}
 * (WORKING-STORAGE with its per-01 buffers). The resolver is assembled on first use, placing the
 * WORKING-STORAGE internals and the file records side by side in one namespace.
 *
 * <p>Resolution is strict, per the COBOL reference rules:
 *
 * <ul>
 *   <li>a bare name held by one buffer resolves
 *   <li>a bare name held by several buffers throws — qualify it
 *   <li>{@code "FIELD OF GROUP"} climbs the ancestor chain
 *   <li>an unknown name throws
 * </ul>
 */
public abstract class RuntimeFieldAccess {

    private final List<FieldStore> registered = new ArrayList<>();

    /**
     * Resolver, assembled on first use and reset by every register(). The AtomicReference publishes
     * the built instance safely across threads.
     */
    private final AtomicReference<FieldResolver> unified = new AtomicReference<>();

    protected final void register(FieldStore buffer) {
        if (buffer == null) {
            return;
        }
        registered.add(buffer);
        unified.set(null);
    }

    /** Explicit RecordImage overload — same behavior, clearer call sites. */
    protected final void register(RecordImage buffer) {
        register((FieldStore) buffer);
    }

    private FieldResolver router() {
        FieldResolver r = unified.get();
        if (r != null) {
            return r;
        }
        synchronized (this) {
            r = unified.get();
            if (r != null) {
                return r;
            }
            List<FieldStore> flat = new ArrayList<>();
            List<RecordSchema> flatLayouts = new ArrayList<>();
            for (FieldStore fb : registered) {
                if (fb instanceof RecordImage rb) {
                    flat.add(rb);
                    flatLayouts.add(rb.layout());
                } else if (fb instanceof StorageImage ws) {
                    List<RecordImage> internal = ws.buffers();
                    List<RecordSchema> internalLay = ws.layout().buffers();
                    for (int i = 0; i < internal.size(); i++) {
                        flat.add(internal.get(i));
                        flatLayouts.add(internalLay.get(i));
                    }
                } else {
                    throw new IllegalStateException(
                            "Unsupported FieldStore type in router build: "
                                    + fb.getClass().getName());
                }
            }
            FieldResolver built = FieldResolver.buildFor(flat, flatLayouts);
            unified.set(built);
            return built;
        }
    }

    /* ── Typed reads — subscripts pass through to the subscript-aware store ── */

    public int getInt(String name, int... subs) {
        return router().route(name).getInt(name, subs);
    }

    public int getIntStrict(String name) {
        return router().route(name).getIntStrict(name);
    }

    public long getLongStrict(String name) {
        return router().route(name).getLongStrict(name);
    }

    public BigDecimal getDecimalStrict(String name) {
        return router().route(name).getDecimalStrict(name);
    }

    public long getLong(String name, int... subs) {
        return router().route(name).getLong(name, subs);
    }

    public String getString(String name, int... subs) {
        return router().route(name).getString(name, subs);
    }

    public BigDecimal getDecimal(String name, int... subs) {
        return router().route(name).getDecimal(name, subs);
    }

    public double getDouble(String name, int... subs) {
        return router().route(name).getDouble(name, subs);
    }

    public boolean getBoolean(String name, int... subs) {
        return router().route(name).getBoolean(name, subs);
    }

    public String groupToString(String name, int... s) {
        return router().route(name).groupToString(name, s);
    }

    public Object getGroup(String name, int... subs) {
        return router().route(name).groupToString(name, subs);
    }

    /* ── Typed writes ────────────────────────────────────────────────── */

    public void setInt(String name, int v, int... subs) {
        router().route(name).setInt(name, v, subs);
    }

    public void setLong(String name, long v, int... subs) {
        router().route(name).setLong(name, v, subs);
    }

    public void setString(String name, String v, int... subs) {
        router().route(name).setString(name, v, subs);
    }

    public void setDecimal(String name, BigDecimal v, int... subs) {
        router().route(name).setDecimal(name, v, subs);
    }

    public void setDouble(String name, double v, int... subs) {
        router().route(name).setDouble(name, v, subs);
    }

    public void setBoolean(String name, boolean v, int... subs) {
        router().route(name).setBoolean(name, v, subs);
    }

    public void setGroup(String name, Object v, int... subs) {
        router().route(name).setGroup(name, v == null ? "" : v.toString(), subs);
    }

    public void set(String name, Object v, int... subs) {
        if (v == null) {
            return;
        }
        if (v instanceof Number) {
            setDecimal(name, new BigDecimal(v.toString()), subs);
        } else if (v instanceof Boolean b) setBoolean(name, b, subs);
        else setString(name, v.toString(), subs);
    }

    /** Lenient write: silently skipped when the field cannot be resolved. */
    public void trySetString(String name, String v, int... subs) {
        if (router().canRoute(name)) {
            router().route(name).setString(name, v);
        }
    }

    /**
     * COBOL reference modification — assigns into name(start:length); any trailing ints are OCCURS
     * subscripts.
     */
    public void setSubstring(String name, int start, int length, Object newValue, int... subs) {
        String cur = getString(name, subs);
        if (cur == null) {
            cur = "";
        }
        int s = Math.max(0, start - 1);
        int e = Math.min(cur.length(), s + length);
        String nv = newValue == null ? "" : newValue.toString();
        if (nv.length() > length) {
            nv = nv.substring(0, length);
        } else if (nv.length() < length) nv = String.format("%-" + length + "s", nv);
        StringBuilder sb = new StringBuilder(cur);
        while (sb.length() < e) sb.append(' ');
        sb.replace(s, e, nv);
        setString(name, sb.toString(), subs);
    }

    /** Whether {@code name} resolves — either uniquely bare or validly qualified. */
    public boolean contains(String name) {
        return router().canRoute(name);
    }

    /**
     * Field value rendered through its COBOL editing picture (zero suppression, insertion
     * commas/slashes); plain fields come back raw. Backs the generated DISPLAY of edited numeric
     * items.
     */
    public String editedDisplay(String name) {
        return router().route(name).editedDisplay(name);
    }

    /* ── Figurative-constant tests ───────────────────────────────────── */
    public boolean isAllHighValues(String name) {
        return router().route(name).isAllHighValues(name);
    }

    public boolean isAllLowValues(String name) {
        return router().route(name).isAllLowValues(name);
    }

    public boolean isAllSpaces(String name) {
        return router().route(name).isAllSpaces(name);
    }

    public boolean isAllZeros(String name) {
        return router().route(name).isAllZeros(name);
    }

    /* ── Raw sentinel fills ── */
    public void fillHighValues(String name) {
        router().route(name).fillHighValues(name);
    }

    public void fillLowValues(String name) {
        router().route(name).fillLowValues(name);
    }

    /* ── Raw group MOVE — byte-exact, no string round-trip losses ── */
    public byte[] sliceBytes(String name) {
        return router().route(name).sliceBytes(name);
    }

    public void writeBytes(String name, byte[] src) {
        router().route(name).writeBytes(name, src);
    }

    public void copyBytes(String toName, String fromName) {
        FieldStore toBuf = router().route(toName);
        FieldStore fromBuf = router().route(fromName);
        if (toBuf == fromBuf) {
            toBuf.copyBytes(toName, fromName);
        } else {
            toBuf.writeBytes(toName, fromBuf.sliceBytes(fromName));
        }
    }

    /**
     * Element-addressed group MOVE — COBOL {@code MOVE G(a) TO G(b)} where G is an OCCURS array:
     * each side keeps its own subscripts so exactly one element is transferred. When the two names
     * live in different buffers (separate 01-levels sharing this resolver), each side is
     * sliced/written under its own subscripts — the element travels, never the array base (the
     * temp-record swap pattern).
     */
    public void copyBytes(String toName, String fromName, int[] toSubs, int[] fromSubs) {
        FieldStore toBuf = router().route(toName);
        FieldStore fromBuf = router().route(fromName);
        if (toBuf == fromBuf) {
            toBuf.copyBytes(toName, fromName, toSubs, fromSubs);
        } else {
            toBuf.writeBytes(toName, toSubs, fromBuf.sliceBytes(fromName, fromSubs));
        }
    }
}

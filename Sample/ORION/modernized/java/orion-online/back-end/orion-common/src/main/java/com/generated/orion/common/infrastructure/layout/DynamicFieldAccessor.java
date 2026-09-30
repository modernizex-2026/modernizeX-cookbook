package com.generated.orion.common.infrastructure.layout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Field accessor — looks up COBOL-name → buffer entirely at runtime via {@link FieldRouter}.
 * Subclasses (Pceg*Fields) enumerate their buffers via {@link #register}.
 *
 * <p>{@code register} accepts both {@link RecordBuffer} (FD scope) and {@link WorkingStorageBuffer}
 * (WS scope, holding N internal RecordBuffers). The router is built lazily on first access,
 * flattening WS internal layouts onto the same level as FD.
 *
 * <p>Routing semantics (strict, COBOL spec):
 *
 * <ul>
 *   <li>Bare name unique → resolve
 *   <li>Ambiguous bare → throws (caller must use "FIELD OF GROUP")
 *   <li>"FIELD OF GROUP" → walk ancestor chain
 *   <li>Unknown → throws
 * </ul>
 */
public abstract class DynamicFieldAccessor implements com.appruntime.FieldStore {

    private final List<FieldBuffer> registered = new ArrayList<>();

    /**
     * Lazy-built router. Invalidated on each register(). AtomicReference is a thread-safe holder,
     * so publication of the built router is safe without a volatile field (S3077).
     */
    private final AtomicReference<FieldRouter> unified = new AtomicReference<>();

    protected final void register(FieldBuffer buffer) {
        if (buffer == null) {
            return;
        }
        registered.add(buffer);
        unified.set(null);
    }

    /** Convenience overload — RecordBuffer is a FieldBuffer; explicit signature for clarity. */
    protected final void register(RecordBuffer buffer) {
        register((FieldBuffer) buffer);
    }

    private FieldRouter router() {
        FieldRouter r = unified.get();
        if (r != null) {
            return r;
        }
        synchronized (this) {
            r = unified.get();
            if (r != null) {
                return r;
            }
            List<FieldBuffer> flat = new ArrayList<>();
            List<RecordSchema> flatLayouts = new ArrayList<>();
            for (FieldBuffer fb : registered) {
                if (fb instanceof RecordBuffer rb) {
                    flat.add(rb);
                    flatLayouts.add(rb.layout());
                } else if (fb instanceof WorkingStorageBuffer ws) {
                    List<RecordBuffer> internal = ws.buffers();
                    List<RecordSchema> internalLay = ws.layout().buffers();
                    for (int i = 0; i < internal.size(); i++) {
                        flat.add(internal.get(i));
                        flatLayouts.add(internalLay.get(i));
                    }
                } else {
                    throw new IllegalStateException(
                            "Unsupported FieldBuffer type in router build: "
                                    + fb.getClass().getName());
                }
            }
            FieldRouter built = FieldRouter.buildFor(flat, flatLayouts);
            unified.set(built);
            return built;
        }
    }

    /* ── Typed getters — subs forwarded to OCCURS-aware FieldBuffer overloads ── */

    public int getInt(String name, int... subs) {
        return router().route(name).getInt(name, subs);
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

    /**
     * ẢNH group (String), KHÔNG phải tham chiếu — tên method dễ gây hiểu nhầm nên nói rõ ở đây.
     * Muốn callee ghi trả về được (CALL BY REFERENCE của COBOL) thì phải truyền {@code
     * com.appruntime.GroupRef(fieldStore, tênGroup)}, dựng tại CALL-SITE trong module program —
     * engine layout này KHÔNG được phụ thuộc app-runtime (nó phải compile độc lập, có test canh:
     * RecordBufferAliasTest). Hiện KHÔNG nơi nào gọi method này.
     */
    public Object getGroup(String name, int... subs) {
        return router().route(name).groupToString(name, subs);
    }

    /* ── Typed setters ─────────────────────────────────────────────── */

    public void setInt(String name, int v, int... subs) {
        router().route(name).setInt(name, v, subs);
    }

    public void setLong(String name, long v, int... subs) {
        router().route(name).setLong(name, v, subs);
    }

    public void setString(String name, String v, int... subs) {
        router().route(name).setString(name, v, subs);
    }

    // BMS map byte model (FieldStore) — no-subscript overloads + has(); BMS projects implement
    // FieldStore.
    public String getString(String name) {
        return getString(name, new int[0]);
    }

    /**
     * Raw field bytes WITHOUT numeric de-coding — for COBOL class tests (IS NUMERIC / ALPHABETIC).
     */
    public String getRawString(String name) {
        return router().route(name).getRawString(name);
    }

    public void setString(String name, String v) {
        setString(name, v, new int[0]);
    }

    public boolean has(String name) {
        return contains(name);
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

    /** Best-effort setter — does not throw if the field is missing (skip). */
    public void trySetString(String name, String v, int... subs) {
        if (router().canRoute(name)) {
            router().route(name).setString(name, v);
        }
    }

    /**
     * COBOL reference modification: name(start:length) = newValue. Trailing ints are OCCURS
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

    /** Returns true if {@code name} is resolvable (unique bare or valid qualifier). */
    public boolean contains(String name) {
        return router().canRoute(name);
    }

    /* ── Figurative constant checks ───────────────────────────────────── */
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

    /* ── Byte-level figurative fills ── */
    public void fillHighValues(String name) {
        router().route(name).fillHighValues(name);
    }

    public void fillLowValues(String name) {
        router().route(name).fillLowValues(name);
    }

    /* ── Byte-level group MOVE — replaces lossy setGroup+groupToString round-trip ── */
    public byte[] sliceBytes(String name) {
        return router().route(name).sliceBytes(name);
    }

    public void writeBytes(String name, byte[] src) {
        router().route(name).writeBytes(name, src);
    }

    public void copyBytes(String toName, String fromName) {
        FieldBuffer toBuf = router().route(toName);
        FieldBuffer fromBuf = router().route(fromName);
        if (toBuf == fromBuf) {
            toBuf.copyBytes(toName, fromName);
        } else {
            toBuf.writeBytes(toName, fromBuf.sliceBytes(fromName));
        }
    }

    /* ── Pointer overlay (COBOL SET ADDRESS OF) ───────────────────────── */

    /**
     * COBOL {@code SET ADDRESS OF <group> TO ADDRESS OF <hostField>} — re-point the group's record
     * buffer onto the host field's storage as a live view (LINKAGE area overlaid on another record,
     * e.g. a work area inside a commarea). The overlay is per-accessor state; a fresh accessor
     * starts un-aliased and the generated SET re-establishes it on every run, matching CICS task
     * semantics.
     */
    public void aliasGroup(String groupName, String hostFieldName) {
        FieldBuffer g = router().route(groupName);
        FieldBuffer h = router().route(hostFieldName);
        if (!(g instanceof RecordBuffer alias) || !(h instanceof RecordBuffer host)) {
            throw new IllegalStateException(
                    "SET ADDRESS OF "
                            + groupName
                            + " TO ADDRESS OF "
                            + hostFieldName
                            + ": operands must be record-buffer backed");
        }
        String rootName = alias.layout().root().getName();
        if (rootName == null || !rootName.equalsIgnoreCase(groupName.trim())) {
            throw new IllegalStateException(
                    "SET ADDRESS OF "
                            + groupName
                            + ": only a level-01 record can be re-based (buffer root is "
                            + rootName
                            + ")");
        }
        alias.aliasTo(host, hostFieldName);
    }

    /* ── Whole-record snapshot/restore (TS/TD queue copy semantics) ───────
     *
     * Capture/replace the entire backing byte state across all registered buffers.
     * Used by the runtime to take an independent point-in-time copy (WRITEQ TS/TD
     * FROM(area)) and write a stored snapshot back (READQ TS/TD INTO(area)). The
     * registered-buffer order is stable, so a snapshot taken and restored on the
     * same accessor layout round-trips exactly. */
    public byte[] snapshot() {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        for (FieldBuffer fb : registered) {
            out.writeBytes(fb.toBytes());
        }
        return out.toByteArray();
    }

    public void restore(byte[] snap) {
        if (snap == null) {
            return;
        }
        int off = 0;
        for (FieldBuffer fb : registered) {
            byte[] cur = fb.toBytes();
            int len = cur.length;
            fb.fromBytes(java.util.Arrays.copyOfRange(snap, off, Math.min(off + len, snap.length)));
            off += len;
        }
    }
}

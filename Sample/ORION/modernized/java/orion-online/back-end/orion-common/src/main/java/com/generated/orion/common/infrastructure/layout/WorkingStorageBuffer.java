package com.generated.orion.common.infrastructure.layout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Multi-buffer FieldBuffer for WORKING-STORAGE: N {@link RecordBuffer} (1 per 01-level), dispatched
 * by field name via {@link FieldRouter}. Mirrors COBOL semantics: bare unique → resolve, ambiguous
 * → throw, caller must qualify ("FIELD OF GROUP").
 *
 * <p>Does NOT expose {@code recordByteLength()} — WS has no such concept (LSP-safe).
 */
public final class WorkingStorageBuffer implements FieldBuffer {

    private final StorageSchema layout;
    private final List<RecordBuffer> buffers; // parallel to layout.buffers()
    private final FieldRouter router;

    public WorkingStorageBuffer(StorageSchema layout) {
        this.layout = layout;
        List<RecordBuffer> bufs = new ArrayList<>(layout.buffers().size());
        for (RecordSchema rl : layout.buffers()) {
            bufs.add(new RecordBuffer(rl));
        }
        this.buffers = Collections.unmodifiableList(bufs);
        // FieldBuffer<->RecordSchema pairing (same order)
        List<FieldBuffer> bufList = new ArrayList<>(bufs);
        this.router = FieldRouter.buildFor(bufList, layout.buffers());
    }

    public StorageSchema layout() {
        return layout;
    }

    public List<RecordBuffer> buffers() {
        return buffers;
    }

    public FieldRouter router() {
        return router;
    }

    /* ── FieldBuffer dispatch — pure routing ─────────────────────────── */

    @Override
    public int getInt(String n) {
        return router.route(n).getInt(n);
    }

    @Override
    public long getLong(String n) {
        return router.route(n).getLong(n);
    }

    @Override
    public String getString(String n) {
        return router.route(n).getString(n);
    }

    @Override
    public String getRawString(String n) {
        return router.route(n).getRawString(n);
    }

    @Override
    public BigDecimal getDecimal(String n) {
        return router.route(n).getDecimal(n);
    }

    @Override
    public double getDouble(String n) {
        return router.route(n).getDouble(n);
    }

    @Override
    public boolean getBoolean(String n) {
        return router.route(n).getBoolean(n);
    }

    @Override
    public String groupToString(String n) {
        return router.route(n).groupToString(n);
    }

    @Override
    public void setInt(String n, int v) {
        router.route(n).setInt(n, v);
    }

    @Override
    public void setLong(String n, long v) {
        router.route(n).setLong(n, v);
    }

    @Override
    public void setString(String n, String v) {
        router.route(n).setString(n, v);
    }

    @Override
    public void setDecimal(String n, BigDecimal v) {
        router.route(n).setDecimal(n, v);
    }

    @Override
    public void setDouble(String n, double v) {
        router.route(n).setDouble(n, v);
    }

    @Override
    public void setBoolean(String n, boolean v) {
        router.route(n).setBoolean(n, v);
    }

    @Override
    public void setGroup(String n, String v) {
        router.route(n).setGroup(n, v);
    }

    @Override
    public boolean contains(String n) {
        return router.canRoute(n);
    }

    @Override
    public void clear() {
        for (RecordBuffer rb : buffers) {
            rb.clear();
        }
    }

    /* ── OCCURS-aware overloads — forward subs to RecordBuffer ── */
    @Override
    public int getInt(String n, int... subs) {
        return router.route(n).getInt(n, subs);
    }

    @Override
    public long getLong(String n, int... subs) {
        return router.route(n).getLong(n, subs);
    }

    @Override
    public String getString(String n, int... subs) {
        return router.route(n).getString(n, subs);
    }

    @Override
    public BigDecimal getDecimal(String n, int... subs) {
        return router.route(n).getDecimal(n, subs);
    }

    @Override
    public double getDouble(String n, int... subs) {
        return router.route(n).getDouble(n, subs);
    }

    @Override
    public boolean getBoolean(String n, int... subs) {
        return router.route(n).getBoolean(n, subs);
    }

    @Override
    public String groupToString(String n, int... subs) {
        return router.route(n).groupToString(n, subs);
    }

    @Override
    public void setInt(String n, int v, int... subs) {
        router.route(n).setInt(n, v, subs);
    }

    @Override
    public void setLong(String n, long v, int... subs) {
        router.route(n).setLong(n, v, subs);
    }

    @Override
    public void setString(String n, String v, int... subs) {
        router.route(n).setString(n, v, subs);
    }

    @Override
    public void setDecimal(String n, BigDecimal v, int... subs) {
        router.route(n).setDecimal(n, v, subs);
    }

    @Override
    public void setDouble(String n, double v, int... subs) {
        router.route(n).setDouble(n, v, subs);
    }

    @Override
    public void setBoolean(String n, boolean v, int... subs) {
        router.route(n).setBoolean(n, v, subs);
    }

    @Override
    public void setGroup(String n, String v, int... subs) {
        router.route(n).setGroup(n, v, subs);
    }

    /* ── Figurative constant checks — route to internal buffer ── */
    @Override
    public boolean isAllHighValues(String n) {
        return router.route(n).isAllHighValues(n);
    }

    @Override
    public boolean isAllLowValues(String n) {
        return router.route(n).isAllLowValues(n);
    }

    @Override
    public boolean isAllSpaces(String n) {
        return router.route(n).isAllSpaces(n);
    }

    @Override
    public boolean isAllZeros(String n) {
        return router.route(n).isAllZeros(n);
    }

    /* ── Byte-level figurative fills ── */
    @Override
    public void fillHighValues(String n) {
        router.route(n).fillHighValues(n);
    }

    @Override
    public void fillLowValues(String n) {
        router.route(n).fillLowValues(n);
    }

    /* ── Byte-level group ops — handle same-buffer and cross-buffer ── */
    @Override
    public byte[] sliceBytes(String n) {
        return router.route(n).sliceBytes(n);
    }

    @Override
    public void writeBytes(String n, byte[] src) {
        router.route(n).writeBytes(n, src);
    }

    @Override
    public void copyBytes(String toName, String fromName) {
        FieldBuffer toBuf = router.route(toName);
        FieldBuffer fromBuf = router.route(fromName);
        if (toBuf == fromBuf) {
            toBuf.copyBytes(toName, fromName);
        } else {
            // Cross-internal-buffer move (rare in COBOL but allowed semantically)
            toBuf.writeBytes(toName, fromBuf.sliceBytes(fromName));
        }
    }

    /* ── Whole-buffer snapshot/restore — concat / distribute internal record buffers ── */
    @Override
    public byte[] toBytes() {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        for (RecordBuffer rb : buffers)
            out.writeBytes(rb.bytes()); // writeBytes copies → result independent
        return out.toByteArray();
    }

    @Override
    public void fromBytes(byte[] src) {
        if (src == null) {
            for (RecordBuffer rb : buffers) rb.fromBytes(null);
            return;
        }
        int off = 0;
        for (RecordBuffer rb : buffers) {
            int len = rb.bytes().length;
            rb.fromBytes(java.util.Arrays.copyOfRange(src, off, Math.min(off + len, src.length)));
            off += len;
        }
    }
}

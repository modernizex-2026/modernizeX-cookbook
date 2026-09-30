package com.sakura.runtime.record;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * WORKING-STORAGE backing store: one {@link RecordImage} per 01-level item, with every access
 * forwarded to the owning buffer through {@link FieldResolver}. Resolution follows COBOL rules — a
 * unique bare name resolves directly, an ambiguous one throws until the caller qualifies it ("FIELD
 * OF GROUP").
 *
 * <p>Deliberately offers no {@code recordByteLength()}: record length is a file-buffer notion that
 * WORKING-STORAGE does not have.
 */
public final class StorageImage implements FieldStore {

    private final StorageSchema layout;
    private final List<RecordImage> buffers; // index-aligned with layout.buffers()
    private final FieldResolver router;

    public StorageImage(StorageSchema layout) {
        this.layout = layout;
        List<RecordImage> bufs = new ArrayList<>(layout.buffers().size());
        for (RecordSchema rl : layout.buffers()) {
            bufs.add(new RecordImage(rl));
        }
        this.buffers = Collections.unmodifiableList(bufs);
        // the resolver receives stores and schemas in matching order
        List<FieldStore> bufList = new ArrayList<>(bufs);
        this.router = FieldResolver.buildFor(bufList, layout.buffers());
    }

    public StorageSchema layout() {
        return layout;
    }

    public List<RecordImage> buffers() {
        return buffers;
    }

    public FieldResolver router() {
        return router;
    }

    /* ── FieldStore API — each call is handed to the buffer that owns the name ── */

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
    public String editedDisplay(String n) {
        return router.route(n).editedDisplay(n);
    }

    @Override
    public void clear() {
        for (RecordImage rb : buffers) {
            rb.clear();
        }
    }

    /* ── Subscripted variants — subscripts travel with the delegated call ── */
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

    /* ── Figurative-constant tests, delegated ── */
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

    /* ── Raw sentinel fills ── */
    @Override
    public void fillHighValues(String n) {
        router.route(n).fillHighValues(n);
    }

    @Override
    public void fillLowValues(String n) {
        router.route(n).fillLowValues(n);
    }

    /* ── Raw byte moves — within one buffer or across two ── */
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
        FieldStore toBuf = router.route(toName);
        FieldStore fromBuf = router.route(fromName);
        if (toBuf == fromBuf) {
            toBuf.copyBytes(toName, fromName);
        } else {
            // source and target sit in different 01-level buffers — hop via a byte copy
            toBuf.writeBytes(toName, fromBuf.sliceBytes(fromName));
        }
    }

    @Override
    public void copyBytes(String toName, String fromName, int[] toSubs, int[] fromSubs) {
        FieldStore toBuf = router.route(toName);
        FieldStore fromBuf = router.route(fromName);
        if (toBuf == fromBuf) {
            toBuf.copyBytes(toName, fromName, toSubs, fromSubs);
        } else {
            // Buffers differ (separate 01-levels): resolve each side under its own
            // subscripts so exactly the addressed OCCURS element is carried, never the
            // array base — the pattern behind element swaps through a temporary record.
            toBuf.writeBytes(toName, toSubs, fromBuf.sliceBytes(fromName, fromSubs));
        }
    }
}

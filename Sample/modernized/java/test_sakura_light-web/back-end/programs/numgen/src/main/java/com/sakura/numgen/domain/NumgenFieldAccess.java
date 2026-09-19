package com.sakura.numgen.domain;

import com.sakura.numgen.runtime.NumgenDatasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

/**
 * Field accessor for NUMGEN. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class NumgenFieldAccess extends RuntimeFieldAccess {

    public NumgenFieldAccess(WorkingStorage ws, NumgenDatasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getNumcf() != null) {
            register(fileSet.getNumcf().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getCompletionCode() {
        return getInt("COMPLETION-CODE");
    }

    public void setCompletionCode(int value) {
        setInt("COMPLETION-CODE", value);
    }

    public String getFsts() {
        return getString("FSTS");
    }

    public void setFsts(String value) {
        setString("FSTS", value);
    }

    public long getNmCurrent() {
        return getLong("NM-CURRENT");
    }

    public void setNmCurrent(long value) {
        setLong("NM-CURRENT", value);
    }

    public String getNmKey() {
        return getString("NM-KEY");
    }

    public void setNmKey(String value) {
        setString("NM-KEY", value);
    }

    public String getNmPrefix() {
        return getString("NM-PREFIX");
    }

    public void setNmPrefix(String value) {
        setString("NM-PREFIX", value);
    }

    public int getNmWidth() {
        return getInt("NM-WIDTH");
    }

    public void setNmWidth(int value) {
        setInt("NM-WIDTH", value);
    }
}

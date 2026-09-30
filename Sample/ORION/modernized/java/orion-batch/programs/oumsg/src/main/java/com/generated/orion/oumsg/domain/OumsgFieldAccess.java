package com.generated.orion.oumsg.domain;

import com.generated.orion.runtime.record.RuntimeFieldAccess;

/**
 * Field accessor for OUMSG. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OumsgFieldAccess extends RuntimeFieldAccess {

    public OumsgFieldAccess(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getCompletionCode() {
        return getInt("COMPLETION-CODE");
    }

    public void setCompletionCode(int value) {
        setInt("COMPLETION-CODE", value);
    }

    public String getKmCode() {
        return getString("KM-CODE");
    }

    public void setKmCode(String value) {
        setString("KM-CODE", value);
    }

    public String getKmStatus() {
        return getString("KM-STATUS");
    }

    public void setKmStatus(String value) {
        setString("KM-STATUS", value);
    }

    public String getKmText() {
        return getString("KM-TEXT");
    }

    public void setKmText(String value) {
        setString("KM-TEXT", value);
    }
}

package com.sakura.getmsg.domain;

import com.sakura.getmsg.runtime.GetmsgDatasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

/**
 * Field accessor for GETMSG. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class GetmsgFieldAccess extends RuntimeFieldAccess {

    public GetmsgFieldAccess(WorkingStorage ws, GetmsgDatasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getMsgf() != null) {
            register(fileSet.getMsgf().buffer());
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

    public String getMgCode() {
        return getString("MG-CODE");
    }

    public void setMgCode(String value) {
        setString("MG-CODE", value);
    }

    public String getMgText() {
        return getString("MG-TEXT");
    }

    public void setMgText(String value) {
        setString("MG-TEXT", value);
    }
}

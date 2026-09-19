package com.sakura.abortx.domain;

import com.sakura.abortx.runtime.AbortxDatasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

/**
 * Field accessor for ABORTX. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class AbortxFieldAccess extends RuntimeFieldAccess {

    public AbortxFieldAccess(WorkingStorage ws, AbortxDatasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getLogf() != null) {
            register(fileSet.getLogf().buffer());
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

    public String getLogRec() {
        return getString("LOG-REC");
    }

    public void setLogRec(String value) {
        setString("LOG-REC", value);
    }

    public String getWkLine() {
        return groupToString("WK-LINE");
    }

    public void setWkLine(String value) {
        setGroup("WK-LINE", value);
    }

    public String getWlDetail() {
        return getString("WL-DETAIL");
    }

    public void setWlDetail(String value) {
        setString("WL-DETAIL", value);
    }

    public String getWlFile() {
        return getString("WL-FILE");
    }

    public void setWlFile(String value) {
        setString("WL-FILE", value);
    }

    public String getWlFsts() {
        return getString("WL-FSTS");
    }

    public void setWlFsts(String value) {
        setString("WL-FSTS", value);
    }

    public String getWlMsgcode() {
        return getString("WL-MSGCODE");
    }

    public void setWlMsgcode(String value) {
        setString("WL-MSGCODE", value);
    }

    public String getWlProgid() {
        return getString("WL-PROGID");
    }

    public void setWlProgid(String value) {
        setString("WL-PROGID", value);
    }

    /* ── Byte-level copy wrappers (preserves COMP-3/BINARY raw bytes) ── */
    public void copyLogRecFromWkLine() {
        copyBytes("LOG-REC", "WK-LINE");
    }
}

package com.sakura.dateut.domain;

import com.sakura.runtime.record.RuntimeFieldAccess;

/**
 * Field accessor for DATEUT. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class DateutFieldAccess extends RuntimeFieldAccess {

    public DateutFieldAccess(WorkingStorage ws) {
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

    public int getWk6() {
        return getInt("WK-6");
    }

    public void setWk6(int value) {
        setInt("WK-6", value);
    }

    public int getWkD() {
        return getInt("WK-D");
    }

    public void setWkD(int value) {
        setInt("WK-D", value);
    }

    public int getWkDim() {
        return getInt("WK-DIM");
    }

    public void setWkDim(int value) {
        setInt("WK-DIM", value);
    }

    public int getWkDoe() {
        return getInt("WK-DOE");
    }

    public void setWkDoe(int value) {
        setInt("WK-DOE", value);
    }

    public int getWkDoy() {
        return getInt("WK-DOY");
    }

    public void setWkDoy(int value) {
        setInt("WK-DOY", value);
    }

    public int getWkEra() {
        return getInt("WK-ERA");
    }

    public void setWkEra(int value) {
        setInt("WK-ERA", value);
    }

    public int getWkLeap() {
        return getInt("WK-LEAP");
    }

    public void setWkLeap(int value) {
        setInt("WK-LEAP", value);
    }

    public int getWkM() {
        return getInt("WK-M");
    }

    public void setWkM(int value) {
        setInt("WK-M", value);
    }

    public int getWkMp() {
        return getInt("WK-MP");
    }

    public void setWkMp(int value) {
        setInt("WK-MP", value);
    }

    public int getWkSer() {
        return getInt("WK-SER");
    }

    public void setWkSer(int value) {
        setInt("WK-SER", value);
    }

    public int getWkSer2() {
        return getInt("WK-SER2");
    }

    public void setWkSer2(int value) {
        setInt("WK-SER2", value);
    }

    public int getWkTmp() {
        return getInt("WK-TMP");
    }

    public void setWkTmp(int value) {
        setInt("WK-TMP", value);
    }

    public int getWkY() {
        return getInt("WK-Y");
    }

    public void setWkY(int value) {
        setInt("WK-Y", value);
    }

    public int getWkY2() {
        return getInt("WK-Y2");
    }

    public void setWkY2(int value) {
        setInt("WK-Y2", value);
    }

    public int getWkYoe() {
        return getInt("WK-YOE");
    }

    public void setWkYoe(int value) {
        setInt("WK-YOE", value);
    }

    public int getWkYy() {
        return getInt("WK-YY");
    }

    public void setWkYy(int value) {
        setInt("WK-YY", value);
    }

    public int getWkZ() {
        return getInt("WK-Z");
    }

    public void setWkZ(int value) {
        setInt("WK-Z", value);
    }
}

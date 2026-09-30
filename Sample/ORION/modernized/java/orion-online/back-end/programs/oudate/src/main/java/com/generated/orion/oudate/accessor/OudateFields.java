package com.generated.orion.oudate.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.oudate.model.WorkingStorage;

/**
 * Field accessor for OUDATE. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OudateFields extends DynamicFieldAccessor {

    public OudateFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public String getKdDateIn() {
        return getString("KD-DATE-IN");
    }

    public void setKdDateIn(String value) {
        setString("KD-DATE-IN", value);
    }

    public String getKdDateOut() {
        return getString("KD-DATE-OUT");
    }

    public void setKdDateOut(String value) {
        setString("KD-DATE-OUT", value);
    }

    public String getKdFunc() {
        return getString("KD-FUNC");
    }

    public void setKdFunc(String value) {
        setString("KD-FUNC", value);
    }

    public String getKdStatus() {
        return getString("KD-STATUS");
    }

    public void setKdStatus(String value) {
        setString("KD-STATUS", value);
    }

    public String getKdateParm() {
        return groupToString("KDATE-PARM");
    }

    public void setKdateParm(String value) {
        setGroup("KDATE-PARM", value);
    }

    public String getWsCurr() {
        return getString("WS-CURR");
    }

    public void setWsCurr(String value) {
        setString("WS-CURR", value);
    }

    public int getWsD() {
        return getInt("WS-D");
    }

    public void setWsD(int value) {
        setInt("WS-D", value);
    }

    public int getWsDim() {
        return getInt("WS-DIM");
    }

    public void setWsDim(int value) {
        setInt("WS-DIM", value);
    }

    public int getWsEdD() {
        return getInt("WS-ED-D");
    }

    public void setWsEdD(int value) {
        setInt("WS-ED-D", value);
    }

    public int getWsEdM() {
        return getInt("WS-ED-M");
    }

    public void setWsEdM(int value) {
        setInt("WS-ED-M", value);
    }

    public int getWsEdY() {
        return getInt("WS-ED-Y");
    }

    public void setWsEdY(int value) {
        setInt("WS-ED-Y", value);
    }

    public int getWsM() {
        return getInt("WS-M");
    }

    public void setWsM(int value) {
        setInt("WS-M", value);
    }

    public int getWsY() {
        return getInt("WS-Y");
    }

    public void setWsY(int value) {
        setInt("WS-Y", value);
    }
}

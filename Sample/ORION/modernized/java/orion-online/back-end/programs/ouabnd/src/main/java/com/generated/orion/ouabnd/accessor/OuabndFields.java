package com.generated.orion.ouabnd.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ouabnd.model.WorkingStorage;

/**
 * Field accessor for OUABND. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OuabndFields extends DynamicFieldAccessor {

    public OuabndFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public String getKaDetail() {
        return getString("KA-DETAIL");
    }

    public void setKaDetail(String value) {
        setString("KA-DETAIL", value);
    }

    public String getKaParagraph() {
        return getString("KA-PARAGRAPH");
    }

    public void setKaParagraph(String value) {
        setString("KA-PARAGRAPH", value);
    }

    public String getKaProgram() {
        return getString("KA-PROGRAM");
    }

    public void setKaProgram(String value) {
        setString("KA-PROGRAM", value);
    }

    public String getKabndParm() {
        return groupToString("KABND-PARM");
    }

    public void setKabndParm(String value) {
        setGroup("KABND-PARM", value);
    }

    public String getWsAbendMsg() {
        return getString("WS-ABEND-MSG");
    }

    public void setWsAbendMsg(String value) {
        setString("WS-ABEND-MSG", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }
}

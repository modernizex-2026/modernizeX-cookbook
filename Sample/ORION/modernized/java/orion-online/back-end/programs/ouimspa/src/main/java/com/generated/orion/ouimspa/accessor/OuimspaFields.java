package com.generated.orion.ouimspa.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ouimspa.model.WorkingStorage;

/**
 * Field accessor for OUIMSPA. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OuimspaFields extends DynamicFieldAccessor {

    public OuimspaFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public String getCaErrMsg() {
        return getString("CA-ERR-MSG");
    }

    public void setCaErrMsg(String value) {
        setString("CA-ERR-MSG", value);
    }

    public String getCaWorkArea() {
        return getString("CA-WORK-AREA");
    }

    public void setCaWorkArea(String value) {
        setString("CA-WORK-AREA", value);
    }

    public String getOdliDlet() {
        return getString("ODLI-DLET");
    }

    public void setOdliDlet(String value) {
        setString("ODLI-DLET", value);
    }

    public String getOdliGhu() {
        return getString("ODLI-GHU");
    }

    public void setOdliGhu(String value) {
        setString("ODLI-GHU", value);
    }

    public String getOdliGn() {
        return getString("ODLI-GN");
    }

    public void setOdliGn(String value) {
        setString("ODLI-GN", value);
    }

    public String getOdliGu() {
        return getString("ODLI-GU");
    }

    public void setOdliGu(String value) {
        setString("ODLI-GU", value);
    }

    public String getOdliIsrt() {
        return getString("ODLI-ISRT");
    }

    public void setOdliIsrt(String value) {
        setString("ODLI-ISRT", value);
    }

    public String getOdliOpEq() {
        return getString("ODLI-OP-EQ");
    }

    public void setOdliOpEq(String value) {
        setString("ODLI-OP-EQ", value);
    }

    public String getOdliOpGt() {
        return getString("ODLI-OP-GT");
    }

    public void setOdliOpGt(String value) {
        setString("ODLI-OP-GT", value);
    }

    public String getOdliRepl() {
        return getString("ODLI-REPL");
    }

    public void setOdliRepl(String value) {
        setString("ODLI-REPL", value);
    }

    public String getOdliSsaKeyval() {
        return getString("ODLI-SSA-KEYVAL");
    }

    public void setOdliSsaKeyval(String value) {
        setString("ODLI-SSA-KEYVAL", value);
    }

    public String getOdliSsaRelop() {
        return getString("ODLI-SSA-RELOP");
    }

    public void setOdliSsaRelop(String value) {
        setString("ODLI-SSA-RELOP", value);
    }

    public String getOdliStOk() {
        return getString("ODLI-ST-OK");
    }

    public void setOdliStOk(String value) {
        setString("ODLI-ST-OK", value);
    }

    public String getOrionCommarea() {
        return groupToString("ORION-COMMAREA");
    }

    public void setOrionCommarea(String value) {
        setGroup("ORION-COMMAREA", value);
    }

    public long getPaAcctId() {
        return getLong("PA-ACCT-ID");
    }

    public void setPaAcctId(long value) {
        setLong("PA-ACCT-ID", value);
    }

    public String getPaAuthId() {
        return getString("PA-AUTH-ID");
    }

    public void setPaAuthId(String value) {
        setString("PA-AUTH-ID", value);
    }

    public String getPaCardNum() {
        return getString("PA-CARD-NUM");
    }

    public void setPaCardNum(String value) {
        setString("PA-CARD-NUM", value);
    }

    public String getPaStatus() {
        return getString("PA-STATUS");
    }

    public void setPaStatus(String value) {
        setString("PA-STATUS", value);
    }

    public int getWsCallCount() {
        return getInt("WS-CALL-COUNT");
    }

    public void setWsCallCount(int value) {
        setInt("WS-CALL-COUNT", value);
    }

    public String getWsDliFunc() {
        return getString("WS-DLI-FUNC");
    }

    public void setWsDliFunc(String value) {
        setString("WS-DLI-FUNC", value);
    }

    public String getWsDliStatus() {
        return getString("WS-DLI-STATUS");
    }

    public void setWsDliStatus(String value) {
        setString("WS-DLI-STATUS", value);
    }

    public String getWsPauLink() {
        return groupToString("WS-PAU-LINK");
    }

    public void setWsPauLink(String value) {
        setGroup("WS-PAU-LINK", value);
    }

    public String getWsPlDliStat() {
        return getString("WS-PL-DLI-STAT");
    }

    public void setWsPlDliStat(String value) {
        setString("WS-PL-DLI-STAT", value);
    }

    public String getWsPlFunc() {
        return getString("WS-PL-FUNC");
    }

    public void setWsPlFunc(String value) {
        setString("WS-PL-FUNC", value);
    }

    public String getWsPlKey() {
        return getString("WS-PL-KEY");
    }

    public void setWsPlKey(String value) {
        setString("WS-PL-KEY", value);
    }

    public String getWsPlMsg() {
        return getString("WS-PL-MSG");
    }

    public void setWsPlMsg(String value) {
        setString("WS-PL-MSG", value);
    }

    public String getWsPlSegment() {
        return getString("WS-PL-SEGMENT");
    }

    public void setWsPlSegment(String value) {
        setString("WS-PL-SEGMENT", value);
    }

    public String getWsPlStatus() {
        return getString("WS-PL-STATUS");
    }

    public void setWsPlStatus(String value) {
        setString("WS-PL-STATUS", value);
    }
}

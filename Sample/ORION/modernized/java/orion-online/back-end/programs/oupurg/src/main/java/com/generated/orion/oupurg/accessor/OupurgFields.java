package com.generated.orion.oupurg.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.oupurg.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OUPURG. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OupurgFields extends DynamicFieldAccessor {

    public OupurgFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public String getKpgCutoff() {
        return getString("KPG-CUTOFF");
    }

    public void setKpgCutoff(String value) {
        setString("KPG-CUTOFF", value);
    }

    public int getKpgErrors() {
        return getInt("KPG-ERRORS");
    }

    public void setKpgErrors(int value) {
        setInt("KPG-ERRORS", value);
    }

    public BigDecimal getKpgKeepAmt() {
        return getDecimal("KPG-KEEP-AMT");
    }

    public void setKpgKeepAmt(BigDecimal value) {
        setDecimal("KPG-KEEP-AMT", value);
    }

    public int getKpgKept() {
        return getInt("KPG-KEPT");
    }

    public void setKpgKept(int value) {
        setInt("KPG-KEPT", value);
    }

    public int getKpgMax() {
        return getInt("KPG-MAX");
    }

    public void setKpgMax(int value) {
        setInt("KPG-MAX", value);
    }

    public String getKpgMore() {
        return getString("KPG-MORE");
    }

    public void setKpgMore(String value) {
        setString("KPG-MORE", value);
    }

    public String getKpgMsg() {
        return getString("KPG-MSG");
    }

    public void setKpgMsg(String value) {
        setString("KPG-MSG", value);
    }

    public String getKpgNextTran() {
        return getString("KPG-NEXT-TRAN");
    }

    public void setKpgNextTran(String value) {
        setString("KPG-NEXT-TRAN", value);
    }

    public BigDecimal getKpgPurgeAmt() {
        return getDecimal("KPG-PURGE-AMT");
    }

    public void setKpgPurgeAmt(BigDecimal value) {
        setDecimal("KPG-PURGE-AMT", value);
    }

    public int getKpgPurged() {
        return getInt("KPG-PURGED");
    }

    public void setKpgPurged(int value) {
        setInt("KPG-PURGED", value);
    }

    public int getKpgRead() {
        return getInt("KPG-READ");
    }

    public void setKpgRead(int value) {
        setInt("KPG-READ", value);
    }

    public String getKpgStartTran() {
        return getString("KPG-START-TRAN");
    }

    public void setKpgStartTran(String value) {
        setString("KPG-START-TRAN", value);
    }

    public String getKpgStatus() {
        return getString("KPG-STATUS");
    }

    public void setKpgStatus(String value) {
        setString("KPG-STATUS", value);
    }

    public String getOrionCommarea() {
        return groupToString("ORION-COMMAREA");
    }

    public void setOrionCommarea(String value) {
        setGroup("ORION-COMMAREA", value);
    }

    public BigDecimal getTrAmt() {
        return getDecimal("TR-AMT");
    }

    public void setTrAmt(BigDecimal value) {
        setDecimal("TR-AMT", value);
    }

    public String getTrId() {
        return getString("TR-ID");
    }

    public void setTrId(String value) {
        setString("TR-ID", value);
    }

    public String getTrProcTs() {
        return getString("TR-PROC-TS");
    }

    public void setTrProcTs(String value) {
        setString("TR-PROC-TS", value);
    }

    public String getWsBrowseSw() {
        return getString("WS-BROWSE-SW");
    }

    public void setWsBrowseSw(String value) {
        setString("WS-BROWSE-SW", value);
    }

    public int getWsCap() {
        return getInt("WS-CAP");
    }

    public void setWsCap(int value) {
        setInt("WS-CAP", value);
    }

    public String getWsCapped() {
        return getString("WS-CAPPED");
    }

    public void setWsCapped(String value) {
        setString("WS-CAPPED", value);
    }

    public String getWsDt() {
        return getString("WS-DT");
    }

    public void setWsDt(String value) {
        setString("WS-DT", value);
    }

    public String getWsDtIn() {
        return getString("WS-DT-IN");
    }

    public void setWsDtIn(String value) {
        setString("WS-DT-IN", value);
    }

    public String getWsDtOk() {
        return getString("WS-DT-OK");
    }

    public void setWsDtOk(String value) {
        setString("WS-DT-OK", value);
    }

    public String getWsEofSw() {
        return getString("WS-EOF-SW");
    }

    public void setWsEofSw(String value) {
        setString("WS-EOF-SW", value);
    }

    public int getWsMax() {
        return getInt("WS-MAX");
    }

    public void setWsMax(int value) {
        setInt("WS-MAX", value);
    }

    public int getWsMaxSave() {
        return getInt("WS-MAX-SAVE");
    }

    public void setWsMaxSave(int value) {
        setInt("WS-MAX-SAVE", value);
    }

    public BigDecimal getWsPAmt(int index) {
        return getDecimal("WS-P-AMT", index);
    }

    public void setWsPAmt(int index, BigDecimal value) {
        setDecimal("WS-P-AMT", value, index);
    }

    public String getWsPId(int index) {
        return getString("WS-P-ID", index);
    }

    public void setWsPId(int index, String value) {
        setString("WS-P-ID", value, index);
    }

    public String getWsProcDate() {
        return getString("WS-PROC-DATE");
    }

    public void setWsProcDate(String value) {
        setString("WS-PROC-DATE", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public int getWsSaveCnt() {
        return getInt("WS-SAVE-CNT");
    }

    public void setWsSaveCnt(int value) {
        setInt("WS-SAVE-CNT", value);
    }

    public int getWsSx() {
        return getInt("WS-SX");
    }

    public void setWsSx(int value) {
        setInt("WS-SX", value);
    }

    public String getWsTranfile() {
        return getString("WS-TRANFILE");
    }

    public void setWsTranfile(String value) {
        setString("WS-TRANFILE", value);
    }
}

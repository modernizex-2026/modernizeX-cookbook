package com.generated.orion.ouarch.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ouarch.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OUARCH. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OuarchFields extends DynamicFieldAccessor {

    public OuarchFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public BigDecimal getKarArchAmt() {
        return getDecimal("KAR-ARCH-AMT");
    }

    public void setKarArchAmt(BigDecimal value) {
        setDecimal("KAR-ARCH-AMT", value);
    }

    public int getKarArchived() {
        return getInt("KAR-ARCHIVED");
    }

    public void setKarArchived(int value) {
        setInt("KAR-ARCHIVED", value);
    }

    public String getKarCutoff() {
        return getString("KAR-CUTOFF");
    }

    public void setKarCutoff(String value) {
        setString("KAR-CUTOFF", value);
    }

    public int getKarDeleted() {
        return getInt("KAR-DELETED");
    }

    public void setKarDeleted(int value) {
        setInt("KAR-DELETED", value);
    }

    public int getKarErrors() {
        return getInt("KAR-ERRORS");
    }

    public void setKarErrors(int value) {
        setInt("KAR-ERRORS", value);
    }

    public int getKarKept() {
        return getInt("KAR-KEPT");
    }

    public void setKarKept(int value) {
        setInt("KAR-KEPT", value);
    }

    public int getKarMax() {
        return getInt("KAR-MAX");
    }

    public void setKarMax(int value) {
        setInt("KAR-MAX", value);
    }

    public String getKarMore() {
        return getString("KAR-MORE");
    }

    public void setKarMore(String value) {
        setString("KAR-MORE", value);
    }

    public String getKarMsg() {
        return getString("KAR-MSG");
    }

    public void setKarMsg(String value) {
        setString("KAR-MSG", value);
    }

    public String getKarNextTran() {
        return getString("KAR-NEXT-TRAN");
    }

    public void setKarNextTran(String value) {
        setString("KAR-NEXT-TRAN", value);
    }

    public int getKarRead() {
        return getInt("KAR-READ");
    }

    public void setKarRead(int value) {
        setInt("KAR-READ", value);
    }

    public String getKarStartTran() {
        return getString("KAR-START-TRAN");
    }

    public void setKarStartTran(String value) {
        setString("KAR-START-TRAN", value);
    }

    public String getKarStatus() {
        return getString("KAR-STATUS");
    }

    public void setKarStatus(String value) {
        setString("KAR-STATUS", value);
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

    public String getTranRec() {
        return groupToString("TRAN-REC");
    }

    public void setTranRec(String value) {
        setGroup("TRAN-REC", value);
    }

    public String getWsArchfile() {
        return getString("WS-ARCHFILE");
    }

    public void setWsArchfile(String value) {
        setString("WS-ARCHFILE", value);
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

    public String getWsSaveRec(int index) {
        return getString("WS-SAVE-REC", index);
    }

    public void setWsSaveRec(int index, String value) {
        setString("WS-SAVE-REC", value, index);
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

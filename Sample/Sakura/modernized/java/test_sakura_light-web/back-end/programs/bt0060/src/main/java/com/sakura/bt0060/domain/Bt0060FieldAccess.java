package com.sakura.bt0060.domain;

import com.sakura.bt0060.runtime.Bt0060Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

/**
 * Field accessor for BT0060. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Bt0060FieldAccess extends RuntimeFieldAccess {

    public Bt0060FieldAccess(WorkingStorage ws, Bt0060Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getCustf() != null) {
            register(fileSet.getCustf().buffer());
        }
        if (fileSet != null && fileSet.getProdf() != null) {
            register(fileSet.getProdf().buffer());
        }
        if (fileSet != null && fileSet.getOrdhf() != null) {
            register(fileSet.getOrdhf().buffer());
        }
        if (fileSet != null && fileSet.getOrddf() != null) {
            register(fileSet.getOrddf().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getCompletionCode() {
        return getInt("COMPLETION-CODE");
    }

    public void setCompletionCode(int value) {
        setInt("COMPLETION-CODE", value);
    }

    public int getCuCode() {
        return getInt("CU-CODE");
    }

    public void setCuCode(int value) {
        setInt("CU-CODE", value);
    }

    public int getCuDelFlag() {
        return getInt("CU-DEL-FLAG");
    }

    public void setCuDelFlag(int value) {
        setInt("CU-DEL-FLAG", value);
    }

    public int getEofFlg() {
        return getInt("EOF-FLG");
    }

    public void setEofFlg(int value) {
        setInt("EOF-FLG", value);
    }

    public String getFsts() {
        return getString("FSTS");
    }

    public void setFsts(String value) {
        setString("FSTS", value);
    }

    public String getKaDetail() {
        return getString("KA-DETAIL");
    }

    public void setKaDetail(String value) {
        setString("KA-DETAIL", value);
    }

    public String getKaFile() {
        return getString("KA-FILE");
    }

    public void setKaFile(String value) {
        setString("KA-FILE", value);
    }

    public String getKaFsts() {
        return getString("KA-FSTS");
    }

    public void setKaFsts(String value) {
        setString("KA-FSTS", value);
    }

    public String getKaMsgcode() {
        return getString("KA-MSGCODE");
    }

    public void setKaMsgcode(String value) {
        setString("KA-MSGCODE", value);
    }

    public String getKaProgid() {
        return getString("KA-PROGID");
    }

    public void setKaProgid(String value) {
        setString("KA-PROGID", value);
    }

    public String getKabend() {
        return groupToString("KABEND");
    }

    public void setKabend(String value) {
        setGroup("KABEND", value);
    }

    public int getKdDate1() {
        return getInt("KD-DATE1");
    }

    public void setKdDate1(int value) {
        setInt("KD-DATE1", value);
    }

    public int getKdDate2() {
        return getInt("KD-DATE2");
    }

    public void setKdDate2(int value) {
        setInt("KD-DATE2", value);
    }

    public int getKdDays() {
        return getInt("KD-DAYS");
    }

    public void setKdDays(int value) {
        setInt("KD-DAYS", value);
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

    public int getKdWeekday() {
        return getInt("KD-WEEKDAY");
    }

    public void setKdWeekday(int value) {
        setInt("KD-WEEKDAY", value);
    }

    public String getKdate() {
        return groupToString("KDATE");
    }

    public void setKdate(String value) {
        setGroup("KDATE", value);
    }

    public int getOdLine() {
        return getInt("OD-LINE");
    }

    public void setOdLine(int value) {
        setInt("OD-LINE", value);
    }

    public long getOdNo() {
        return getLong("OD-NO");
    }

    public void setOdNo(long value) {
        setLong("OD-NO", value);
    }

    public int getOhDate() {
        return getInt("OH-DATE");
    }

    public void setOhDate(int value) {
        setInt("OH-DATE", value);
    }

    public int getOhDelFlag() {
        return getInt("OH-DEL-FLAG");
    }

    public void setOhDelFlag(int value) {
        setInt("OH-DEL-FLAG", value);
    }

    public long getOhNo() {
        return getLong("OH-NO");
    }

    public void setOhNo(long value) {
        setLong("OH-NO", value);
    }

    public int getOhStatus() {
        return getInt("OH-STATUS");
    }

    public void setOhStatus(int value) {
        setInt("OH-STATUS", value);
    }

    public int getPrCode() {
        return getInt("PR-CODE");
    }

    public void setPrCode(int value) {
        setInt("PR-CODE", value);
    }

    public int getPrDelFlag() {
        return getInt("PR-DEL-FLAG");
    }

    public void setPrDelFlag(int value) {
        setInt("PR-DEL-FLAG", value);
    }

    public int getWkAbortFlg() {
        return getInt("WK-ABORT-FLG");
    }

    public void setWkAbortFlg(int value) {
        setInt("WK-ABORT-FLG", value);
    }

    public String getWkConfirm() {
        return getString("WK-CONFIRM");
    }

    public void setWkConfirm(String value) {
        setString("WK-CONFIRM", value);
    }

    public int getWkCuDel() {
        return getInt("WK-CU-DEL");
    }

    public void setWkCuDel(int value) {
        setInt("WK-CU-DEL", value);
    }

    public int getWkCuRead() {
        return getInt("WK-CU-READ");
    }

    public void setWkCuRead(int value) {
        setInt("WK-CU-READ", value);
    }

    public int getWkCutoff() {
        return getInt("WK-CUTOFF");
    }

    public void setWkCutoff(int value) {
        setInt("WK-CUTOFF", value);
    }

    public int getWkDfltCut() {
        return getInt("WK-DFLT-CUT");
    }

    public void setWkDfltCut(int value) {
        setInt("WK-DFLT-CUT", value);
    }

    public int getWkECnt() {
        return getInt("WK-E-CNT");
    }

    public void setWkECnt(int value) {
        setInt("WK-E-CNT", value);
    }

    public int getWkEDate() {
        return getInt("WK-E-DATE");
    }

    public void setWkEDate(int value) {
        setInt("WK-E-DATE", value);
    }

    public String getWkInLine() {
        return getString("WK-IN-LINE");
    }

    public void setWkInLine(String value) {
        setString("WK-IN-LINE", value);
    }

    public int getWkOdDel() {
        return getInt("WK-OD-DEL");
    }

    public void setWkOdDel(int value) {
        setInt("WK-OD-DEL", value);
    }

    public int getWkOhDel() {
        return getInt("WK-OH-DEL");
    }

    public void setWkOhDel(int value) {
        setInt("WK-OH-DEL", value);
    }

    public int getWkOhRead() {
        return getInt("WK-OH-READ");
    }

    public void setWkOhRead(int value) {
        setInt("WK-OH-READ", value);
    }

    public int getWkPrDel() {
        return getInt("WK-PR-DEL");
    }

    public void setWkPrDel(int value) {
        setInt("WK-PR-DEL", value);
    }

    public int getWkPrRead() {
        return getInt("WK-PR-READ");
    }

    public void setWkPrRead(int value) {
        setInt("WK-PR-READ", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
    }

    public int getWkPurgeFlg() {
        return getInt("WK-PURGE-FLG");
    }

    public void setWkPurgeFlg(int value) {
        setInt("WK-PURGE-FLG", value);
    }

    public int getWkSysdate() {
        return getInt("WK-SYSDATE");
    }

    public void setWkSysdate(int value) {
        setInt("WK-SYSDATE", value);
    }

    public int getWkSysymd() {
        return getInt("WK-SYSYMD");
    }

    public void setWkSysymd(int value) {
        setInt("WK-SYSYMD", value);
    }
}

package com.sakura.bt0080.domain;

import com.sakura.bt0080.runtime.Bt0080Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

/**
 * Field accessor for BT0080. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Bt0080FieldAccess extends RuntimeFieldAccess {

    public Bt0080FieldAccess(WorkingStorage ws, Bt0080Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getCustf() != null) {
            register(fileSet.getCustf().buffer());
        }
        if (fileSet != null && fileSet.getProdf() != null) {
            register(fileSet.getProdf().buffer());
        }
        if (fileSet != null && fileSet.getCustn() != null) {
            register(fileSet.getCustn().buffer());
        }
        if (fileSet != null && fileSet.getProdn() != null) {
            register(fileSet.getProdn().buffer());
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

    public String getCuRec() {
        return groupToString("CU-REC");
    }

    public void setCuRec(String value) {
        setGroup("CU-REC", value);
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

    public String getNcRec() {
        return getString("NC-REC");
    }

    public void setNcRec(String value) {
        setString("NC-REC", value);
    }

    public String getNpRec() {
        return getString("NP-REC");
    }

    public void setNpRec(String value) {
        setString("NP-REC", value);
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

    public String getPrRec() {
        return groupToString("PR-REC");
    }

    public void setPrRec(String value) {
        setGroup("PR-REC", value);
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

    public int getWkCuCopy() {
        return getInt("WK-CU-COPY");
    }

    public void setWkCuCopy(int value) {
        setInt("WK-CU-COPY", value);
    }

    public int getWkCuRead() {
        return getInt("WK-CU-READ");
    }

    public void setWkCuRead(int value) {
        setInt("WK-CU-READ", value);
    }

    public int getWkCuSkip() {
        return getInt("WK-CU-SKIP");
    }

    public void setWkCuSkip(int value) {
        setInt("WK-CU-SKIP", value);
    }

    public int getWkECnt() {
        return getInt("WK-E-CNT");
    }

    public void setWkECnt(int value) {
        setInt("WK-E-CNT", value);
    }

    public int getWkPrCopy() {
        return getInt("WK-PR-COPY");
    }

    public void setWkPrCopy(int value) {
        setInt("WK-PR-COPY", value);
    }

    public int getWkPrRead() {
        return getInt("WK-PR-READ");
    }

    public void setWkPrRead(int value) {
        setInt("WK-PR-READ", value);
    }

    public int getWkPrSkip() {
        return getInt("WK-PR-SKIP");
    }

    public void setWkPrSkip(int value) {
        setInt("WK-PR-SKIP", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
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

    /* ── Byte-level copy wrappers (preserves COMP-3/BINARY raw bytes) ── */
    public void copyNcRecFromCuRec() {
        copyBytes("NC-REC", "CU-REC");
    }

    public void copyNpRecFromPrRec() {
        copyBytes("NP-REC", "PR-REC");
    }
}

package com.sakura.ms0120.domain;

import com.sakura.ms0120.runtime.Ms0120Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

/**
 * Field accessor for MS0120. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Ms0120FieldAccess extends RuntimeFieldAccess {

    public Ms0120FieldAccess(WorkingStorage ws, Ms0120Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getUserf() != null) {
            register(fileSet.getUserf().buffer());
        }
        if (fileSet != null && fileSet.getStaff() != null) {
            register(fileSet.getStaff().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getCompletionCode() {
        return getInt("COMPLETION-CODE");
    }

    public void setCompletionCode(int value) {
        setInt("COMPLETION-CODE", value);
    }

    public int getEndFlg() {
        return getInt("END-FLG");
    }

    public void setEndFlg(int value) {
        setInt("END-FLG", value);
    }

    public int getErrFlg() {
        return getInt("ERR-FLG");
    }

    public void setErrFlg(int value) {
        setInt("ERR-FLG", value);
    }

    public String getEsts() {
        return getString("ESTS");
    }

    public void setEsts(String value) {
        setString("ESTS", value);
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

    public int getModeFlg() {
        return getInt("MODE-FLG");
    }

    public void setModeFlg(int value) {
        setInt("MODE-FLG", value);
    }

    public int getSfCode() {
        return getInt("SF-CODE");
    }

    public void setSfCode(int value) {
        setInt("SF-CODE", value);
    }

    public String getSfName() {
        return getString("SF-NAME");
    }

    public void setSfName(String value) {
        setString("SF-NAME", value);
    }

    public int getUsAuthClose() {
        return getInt("US-AUTH-CLOSE");
    }

    public void setUsAuthClose(int value) {
        setInt("US-AUTH-CLOSE", value);
    }

    public int getUsAuthMaster() {
        return getInt("US-AUTH-MASTER");
    }

    public void setUsAuthMaster(int value) {
        setInt("US-AUTH-MASTER", value);
    }

    public int getUsAuthOrder() {
        return getInt("US-AUTH-ORDER");
    }

    public void setUsAuthOrder(int value) {
        setInt("US-AUTH-ORDER", value);
    }

    public int getUsAuthPurch() {
        return getInt("US-AUTH-PURCH");
    }

    public void setUsAuthPurch(int value) {
        setInt("US-AUTH-PURCH", value);
    }

    public int getUsAuthSales() {
        return getInt("US-AUTH-SALES");
    }

    public void setUsAuthSales(int value) {
        setInt("US-AUTH-SALES", value);
    }

    public int getUsCode() {
        return getInt("US-CODE");
    }

    public void setUsCode(int value) {
        setInt("US-CODE", value);
    }

    public int getUsDelFlag() {
        return getInt("US-DEL-FLAG");
    }

    public void setUsDelFlag(int value) {
        setInt("US-DEL-FLAG", value);
    }

    public String getUsLogin() {
        return getString("US-LOGIN");
    }

    public void setUsLogin(String value) {
        setString("US-LOGIN", value);
    }

    public String getUsName() {
        return getString("US-NAME");
    }

    public void setUsName(String value) {
        setString("US-NAME", value);
    }

    public String getUsPassword() {
        return getString("US-PASSWORD");
    }

    public void setUsPassword(String value) {
        setString("US-PASSWORD", value);
    }

    public int getUsRole() {
        return getInt("US-ROLE");
    }

    public void setUsRole(int value) {
        setInt("US-ROLE", value);
    }

    public String getWkConfirm() {
        return getString("WK-CONFIRM");
    }

    public void setWkConfirm(String value) {
        setString("WK-CONFIRM", value);
    }

    public String getWkFkeyLine() {
        return getString("WK-FKEY-LINE");
    }

    public void setWkFkeyLine(String value) {
        setString("WK-FKEY-LINE", value);
    }

    public String getWkMsgLine() {
        return getString("WK-MSG-LINE");
    }

    public void setWkMsgLine(String value) {
        setString("WK-MSG-LINE", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
    }

    public int getWkSaveCode() {
        return getInt("WK-SAVE-CODE");
    }

    public void setWkSaveCode(int value) {
        setInt("WK-SAVE-CODE", value);
    }

    public String getWkStaffName() {
        return getString("WK-STAFF-NAME");
    }

    public void setWkStaffName(String value) {
        setString("WK-STAFF-NAME", value);
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

    public String getWkTitle() {
        return getString("WK-TITLE");
    }

    public void setWkTitle(String value) {
        setString("WK-TITLE", value);
    }
}

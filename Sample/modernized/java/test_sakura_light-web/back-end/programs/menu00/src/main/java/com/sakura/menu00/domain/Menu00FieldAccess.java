package com.sakura.menu00.domain;

import com.sakura.runtime.record.RuntimeFieldAccess;

/**
 * Field accessor for MENU00. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Menu00FieldAccess extends RuntimeFieldAccess {

    public Menu00FieldAccess(WorkingStorage ws) {
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

    public int getEndFlg() {
        return getInt("END-FLG");
    }

    public void setEndFlg(int value) {
        setInt("END-FLG", value);
    }

    public String getEsts() {
        return getString("ESTS");
    }

    public void setEsts(String value) {
        setString("ESTS", value);
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

    public String getKlAuth() {
        return getString("KL-AUTH");
    }

    public void setKlAuth(String value) {
        setString("KL-AUTH", value);
    }

    public String getKlLogin() {
        return getString("KL-LOGIN");
    }

    public void setKlLogin(String value) {
        setString("KL-LOGIN", value);
    }

    public String getKlPassword() {
        return getString("KL-PASSWORD");
    }

    public void setKlPassword(String value) {
        setString("KL-PASSWORD", value);
    }

    public int getKlRole() {
        return getInt("KL-ROLE");
    }

    public void setKlRole(int value) {
        setInt("KL-ROLE", value);
    }

    public String getKlStatus() {
        return getString("KL-STATUS");
    }

    public void setKlStatus(String value) {
        setString("KL-STATUS", value);
    }

    public int getKlUserCode() {
        return getInt("KL-USER-CODE");
    }

    public void setKlUserCode(int value) {
        setInt("KL-USER-CODE", value);
    }

    public String getKlUserName() {
        return getString("KL-USER-NAME");
    }

    public void setKlUserName(String value) {
        setString("KL-USER-NAME", value);
    }

    public String getKlogin() {
        return groupToString("KLOGIN");
    }

    public void setKlogin(String value) {
        setGroup("KLOGIN", value);
    }

    public String getWkCall() {
        return getString("WK-CALL");
    }

    public void setWkCall(String value) {
        setString("WK-CALL", value);
    }

    public int getWkChoice() {
        return getInt("WK-CHOICE");
    }

    public void setWkChoice(int value) {
        setInt("WK-CHOICE", value);
    }

    public int getWkLoginTry() {
        return getInt("WK-LOGIN-TRY");
    }

    public void setWkLoginTry(int value) {
        setInt("WK-LOGIN-TRY", value);
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

    public String getWkUserAuth() {
        return getString("WK-USER-AUTH");
    }

    public void setWkUserAuth(String value) {
        setString("WK-USER-AUTH", value);
    }

    public int getWkUserCode() {
        return getInt("WK-USER-CODE");
    }

    public void setWkUserCode(int value) {
        setInt("WK-USER-CODE", value);
    }

    public String getWkUserName() {
        return getString("WK-USER-NAME");
    }

    public void setWkUserName(String value) {
        setString("WK-USER-NAME", value);
    }

    public int getWkUserRole() {
        return getInt("WK-USER-ROLE");
    }

    public void setWkUserRole(int value) {
        setInt("WK-USER-ROLE", value);
    }
}

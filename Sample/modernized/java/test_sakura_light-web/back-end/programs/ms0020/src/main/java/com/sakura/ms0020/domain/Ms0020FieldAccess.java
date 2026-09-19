package com.sakura.ms0020.domain;

import com.sakura.ms0020.runtime.Ms0020Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

/**
 * Field accessor for MS0020. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Ms0020FieldAccess extends RuntimeFieldAccess {

    public Ms0020FieldAccess(WorkingStorage ws, Ms0020Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getSuppf() != null) {
            register(fileSet.getSuppf().buffer());
        }
        if (fileSet != null && fileSet.getBankf() != null) {
            register(fileSet.getBankf().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getBkCode() {
        return getInt("BK-CODE");
    }

    public void setBkCode(int value) {
        setInt("BK-CODE", value);
    }

    public String getBkName() {
        return getString("BK-NAME");
    }

    public void setBkName(String value) {
        setString("BK-NAME", value);
    }

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

    public int getSpAddDate() {
        return getInt("SP-ADD-DATE");
    }

    public void setSpAddDate(int value) {
        setInt("SP-ADD-DATE", value);
    }

    public int getSpAddUser() {
        return getInt("SP-ADD-USER");
    }

    public void setSpAddUser(int value) {
        setInt("SP-ADD-USER", value);
    }

    public String getSpAddr1() {
        return getString("SP-ADDR1");
    }

    public void setSpAddr1(String value) {
        setString("SP-ADDR1", value);
    }

    public String getSpAddr2() {
        return getString("SP-ADDR2");
    }

    public void setSpAddr2(String value) {
        setString("SP-ADDR2", value);
    }

    public String getSpBankAcct() {
        return getString("SP-BANK-ACCT");
    }

    public void setSpBankAcct(String value) {
        setString("SP-BANK-ACCT", value);
    }

    public int getSpBankCode() {
        return getInt("SP-BANK-CODE");
    }

    public void setSpBankCode(int value) {
        setInt("SP-BANK-CODE", value);
    }

    public int getSpCloseDay() {
        return getInt("SP-CLOSE-DAY");
    }

    public void setSpCloseDay(int value) {
        setInt("SP-CLOSE-DAY", value);
    }

    public int getSpCode() {
        return getInt("SP-CODE");
    }

    public void setSpCode(int value) {
        setInt("SP-CODE", value);
    }

    public int getSpDelFlag() {
        return getInt("SP-DEL-FLAG");
    }

    public void setSpDelFlag(int value) {
        setInt("SP-DEL-FLAG", value);
    }

    public String getSpFax() {
        return getString("SP-FAX");
    }

    public void setSpFax(String value) {
        setString("SP-FAX", value);
    }

    public String getSpKana() {
        return getString("SP-KANA");
    }

    public void setSpKana(String value) {
        setString("SP-KANA", value);
    }

    public String getSpName() {
        return getString("SP-NAME");
    }

    public void setSpName(String value) {
        setString("SP-NAME", value);
    }

    public int getSpPayDay() {
        return getInt("SP-PAY-DAY");
    }

    public void setSpPayDay(int value) {
        setInt("SP-PAY-DAY", value);
    }

    public int getSpPayMethod() {
        return getInt("SP-PAY-METHOD");
    }

    public void setSpPayMethod(int value) {
        setInt("SP-PAY-METHOD", value);
    }

    public int getSpPayMonth() {
        return getInt("SP-PAY-MONTH");
    }

    public void setSpPayMonth(int value) {
        setInt("SP-PAY-MONTH", value);
    }

    public int getSpTaxType() {
        return getInt("SP-TAX-TYPE");
    }

    public void setSpTaxType(int value) {
        setInt("SP-TAX-TYPE", value);
    }

    public String getSpTel() {
        return getString("SP-TEL");
    }

    public void setSpTel(String value) {
        setString("SP-TEL", value);
    }

    public int getSpUpdDate() {
        return getInt("SP-UPD-DATE");
    }

    public void setSpUpdDate(int value) {
        setInt("SP-UPD-DATE", value);
    }

    public int getSpUpdUser() {
        return getInt("SP-UPD-USER");
    }

    public void setSpUpdUser(int value) {
        setInt("SP-UPD-USER", value);
    }

    public String getSpZip() {
        return getString("SP-ZIP");
    }

    public void setSpZip(String value) {
        setString("SP-ZIP", value);
    }

    public String getWkBankName() {
        return getString("WK-BANK-NAME");
    }

    public void setWkBankName(String value) {
        setString("WK-BANK-NAME", value);
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

    public int getWkUserCode() {
        return getInt("WK-USER-CODE");
    }

    public void setWkUserCode(int value) {
        setInt("WK-USER-CODE", value);
    }
}

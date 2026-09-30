package com.sakura.ms0010.domain;

import com.sakura.ms0010.runtime.Ms0010Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for MS0010. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Ms0010FieldAccess extends RuntimeFieldAccess {

    public Ms0010FieldAccess(WorkingStorage ws, Ms0010Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getCustf() != null) {
            register(fileSet.getCustf().buffer());
        }
        if (fileSet != null && fileSet.getRegnf() != null) {
            register(fileSet.getRegnf().buffer());
        }
        if (fileSet != null && fileSet.getStaff() != null) {
            register(fileSet.getStaff().buffer());
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

    public int getCuAddDate() {
        return getInt("CU-ADD-DATE");
    }

    public void setCuAddDate(int value) {
        setInt("CU-ADD-DATE", value);
    }

    public int getCuAddUser() {
        return getInt("CU-ADD-USER");
    }

    public void setCuAddUser(int value) {
        setInt("CU-ADD-USER", value);
    }

    public String getCuAddr1() {
        return getString("CU-ADDR1");
    }

    public void setCuAddr1(String value) {
        setString("CU-ADDR1", value);
    }

    public String getCuAddr2() {
        return getString("CU-ADDR2");
    }

    public void setCuAddr2(String value) {
        setString("CU-ADDR2", value);
    }

    public int getCuBankCode() {
        return getInt("CU-BANK-CODE");
    }

    public void setCuBankCode(int value) {
        setInt("CU-BANK-CODE", value);
    }

    public int getCuCloseDay() {
        return getInt("CU-CLOSE-DAY");
    }

    public void setCuCloseDay(int value) {
        setInt("CU-CLOSE-DAY", value);
    }

    public int getCuCode() {
        return getInt("CU-CODE");
    }

    public void setCuCode(int value) {
        setInt("CU-CODE", value);
    }

    public BigDecimal getCuCreditLimit() {
        return getDecimal("CU-CREDIT-LIMIT");
    }

    public void setCuCreditLimit(BigDecimal value) {
        setDecimal("CU-CREDIT-LIMIT", value);
    }

    public int getCuDelFlag() {
        return getInt("CU-DEL-FLAG");
    }

    public void setCuDelFlag(int value) {
        setInt("CU-DEL-FLAG", value);
    }

    public String getCuFax() {
        return getString("CU-FAX");
    }

    public void setCuFax(String value) {
        setString("CU-FAX", value);
    }

    public String getCuKana() {
        return getString("CU-KANA");
    }

    public void setCuKana(String value) {
        setString("CU-KANA", value);
    }

    public String getCuName() {
        return getString("CU-NAME");
    }

    public void setCuName(String value) {
        setString("CU-NAME", value);
    }

    public int getCuPayMethod() {
        return getInt("CU-PAY-METHOD");
    }

    public void setCuPayMethod(int value) {
        setInt("CU-PAY-METHOD", value);
    }

    public int getCuPriceRank() {
        return getInt("CU-PRICE-RANK");
    }

    public void setCuPriceRank(int value) {
        setInt("CU-PRICE-RANK", value);
    }

    public int getCuRegion() {
        return getInt("CU-REGION");
    }

    public void setCuRegion(int value) {
        setInt("CU-REGION", value);
    }

    public int getCuStaff() {
        return getInt("CU-STAFF");
    }

    public void setCuStaff(int value) {
        setInt("CU-STAFF", value);
    }

    public int getCuStartDate() {
        return getInt("CU-START-DATE");
    }

    public void setCuStartDate(int value) {
        setInt("CU-START-DATE", value);
    }

    public int getCuTaxType() {
        return getInt("CU-TAX-TYPE");
    }

    public void setCuTaxType(int value) {
        setInt("CU-TAX-TYPE", value);
    }

    public String getCuTel() {
        return getString("CU-TEL");
    }

    public void setCuTel(String value) {
        setString("CU-TEL", value);
    }

    public int getCuUpdDate() {
        return getInt("CU-UPD-DATE");
    }

    public void setCuUpdDate(int value) {
        setInt("CU-UPD-DATE", value);
    }

    public int getCuUpdUser() {
        return getInt("CU-UPD-USER");
    }

    public void setCuUpdUser(int value) {
        setInt("CU-UPD-USER", value);
    }

    public String getCuZip() {
        return getString("CU-ZIP");
    }

    public void setCuZip(String value) {
        setString("CU-ZIP", value);
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

    public int getRgCode() {
        return getInt("RG-CODE");
    }

    public void setRgCode(int value) {
        setInt("RG-CODE", value);
    }

    public String getRgName() {
        return getString("RG-NAME");
    }

    public void setRgName(String value) {
        setString("RG-NAME", value);
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

    public String getWkRegionName() {
        return getString("WK-REGION-NAME");
    }

    public void setWkRegionName(String value) {
        setString("WK-REGION-NAME", value);
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

    public int getWkUserCode() {
        return getInt("WK-USER-CODE");
    }

    public void setWkUserCode(int value) {
        setInt("WK-USER-CODE", value);
    }
}

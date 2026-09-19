package com.sakura.ar0010.domain;

import com.sakura.ar0010.runtime.Ar0010Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for AR0010. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Ar0010FieldAccess extends RuntimeFieldAccess {

    public Ar0010FieldAccess(WorkingStorage ws, Ar0010Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getRcptf() != null) {
            register(fileSet.getRcptf().buffer());
        }
        if (fileSet != null && fileSet.getArlf() != null) {
            register(fileSet.getArlf().buffer());
        }
        if (fileSet != null && fileSet.getCustf() != null) {
            register(fileSet.getCustf().buffer());
        }
        if (fileSet != null && fileSet.getBankf() != null) {
            register(fileSet.getBankf().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public BigDecimal getAlBalance() {
        return getDecimal("AL-BALANCE");
    }

    public void setAlBalance(BigDecimal value) {
        setDecimal("AL-BALANCE", value);
    }

    public int getAlCloseYm() {
        return getInt("AL-CLOSE-YM");
    }

    public void setAlCloseYm(int value) {
        setInt("AL-CLOSE-YM", value);
    }

    public BigDecimal getAlCredit() {
        return getDecimal("AL-CREDIT");
    }

    public void setAlCredit(BigDecimal value) {
        setDecimal("AL-CREDIT", value);
    }

    public int getAlCust() {
        return getInt("AL-CUST");
    }

    public void setAlCust(int value) {
        setInt("AL-CUST", value);
    }

    public int getAlDate() {
        return getInt("AL-DATE");
    }

    public void setAlDate(int value) {
        setInt("AL-DATE", value);
    }

    public BigDecimal getAlDebit() {
        return getDecimal("AL-DEBIT");
    }

    public void setAlDebit(BigDecimal value) {
        setDecimal("AL-DEBIT", value);
    }

    public int getAlKind() {
        return getInt("AL-KIND");
    }

    public void setAlKind(int value) {
        setInt("AL-KIND", value);
    }

    public long getAlRefNo() {
        return getLong("AL-REF-NO");
    }

    public void setAlRefNo(long value) {
        setLong("AL-REF-NO", value);
    }

    public int getAlRefType() {
        return getInt("AL-REF-TYPE");
    }

    public void setAlRefType(int value) {
        setInt("AL-REF-TYPE", value);
    }

    public String getAlRemark() {
        return getString("AL-REMARK");
    }

    public void setAlRemark(String value) {
        setString("AL-REMARK", value);
    }

    public long getAlSeq() {
        return getLong("AL-SEQ");
    }

    public void setAlSeq(long value) {
        setLong("AL-SEQ", value);
    }

    public int getAlUser() {
        return getInt("AL-USER");
    }

    public void setAlUser(int value) {
        setInt("AL-USER", value);
    }

    public int getBkCode() {
        return getInt("BK-CODE");
    }

    public void setBkCode(int value) {
        setInt("BK-CODE", value);
    }

    public int getBkDelFlag() {
        return getInt("BK-DEL-FLAG");
    }

    public void setBkDelFlag(int value) {
        setInt("BK-DEL-FLAG", value);
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

    public BigDecimal getCuBalance() {
        return getDecimal("CU-BALANCE");
    }

    public void setCuBalance(BigDecimal value) {
        setDecimal("CU-BALANCE", value);
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

    public String getCuName() {
        return getString("CU-NAME");
    }

    public void setCuName(String value) {
        setString("CU-NAME", value);
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

    public int getHdrOk() {
        return getInt("HDR-OK");
    }

    public void setHdrOk(int value) {
        setInt("HDR-OK", value);
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

    public String getKnum() {
        return groupToString("KNUM");
    }

    public void setKnum(String value) {
        setGroup("KNUM", value);
    }

    public String getKnumKey() {
        return getString("KNUM-KEY");
    }

    public void setKnumKey(String value) {
        setString("KNUM-KEY", value);
    }

    public long getKnumNumber() {
        return getLong("KNUM-NUMBER");
    }

    public void setKnumNumber(long value) {
        setLong("KNUM-NUMBER", value);
    }

    public String getKnumStatus() {
        return getString("KNUM-STATUS");
    }

    public void setKnumStatus(String value) {
        setString("KNUM-STATUS", value);
    }

    public int getReAddDate() {
        return getInt("RE-ADD-DATE");
    }

    public void setReAddDate(int value) {
        setInt("RE-ADD-DATE", value);
    }

    public int getReAddUser() {
        return getInt("RE-ADD-USER");
    }

    public void setReAddUser(int value) {
        setInt("RE-ADD-USER", value);
    }

    public BigDecimal getReAmount() {
        return getDecimal("RE-AMOUNT");
    }

    public void setReAmount(BigDecimal value) {
        setDecimal("RE-AMOUNT", value);
    }

    public int getReBankCode() {
        return getInt("RE-BANK-CODE");
    }

    public void setReBankCode(int value) {
        setInt("RE-BANK-CODE", value);
    }

    public int getReCloseYm() {
        return getInt("RE-CLOSE-YM");
    }

    public void setReCloseYm(int value) {
        setInt("RE-CLOSE-YM", value);
    }

    public int getReCust() {
        return getInt("RE-CUST");
    }

    public void setReCust(int value) {
        setInt("RE-CUST", value);
    }

    public int getReDate() {
        return getInt("RE-DATE");
    }

    public void setReDate(int value) {
        setInt("RE-DATE", value);
    }

    public int getReDelFlag() {
        return getInt("RE-DEL-FLAG");
    }

    public void setReDelFlag(int value) {
        setInt("RE-DEL-FLAG", value);
    }

    public int getReMethod() {
        return getInt("RE-METHOD");
    }

    public void setReMethod(int value) {
        setInt("RE-METHOD", value);
    }

    public long getReNo() {
        return getLong("RE-NO");
    }

    public void setReNo(long value) {
        setLong("RE-NO", value);
    }

    public String getReRemark() {
        return getString("RE-REMARK");
    }

    public void setReRemark(String value) {
        setString("RE-REMARK", value);
    }

    public int getReStatus() {
        return getInt("RE-STATUS");
    }

    public void setReStatus(int value) {
        setInt("RE-STATUS", value);
    }

    public String getWkBankName() {
        return getString("WK-BANK-NAME");
    }

    public void setWkBankName(String value) {
        setString("WK-BANK-NAME", value);
    }

    public int getWkCloseYm() {
        return getInt("WK-CLOSE-YM");
    }

    public void setWkCloseYm(int value) {
        setInt("WK-CLOSE-YM", value);
    }

    public String getWkConfirm() {
        return getString("WK-CONFIRM");
    }

    public void setWkConfirm(String value) {
        setString("WK-CONFIRM", value);
    }

    public long getWkCurBal() {
        return getLong("WK-CUR-BAL");
    }

    public void setWkCurBal(long value) {
        setLong("WK-CUR-BAL", value);
    }

    public String getWkCustName() {
        return getString("WK-CUST-NAME");
    }

    public void setWkCustName(String value) {
        setString("WK-CUST-NAME", value);
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

    public long getWkNewBal() {
        return getLong("WK-NEW-BAL");
    }

    public void setWkNewBal(long value) {
        setLong("WK-NEW-BAL", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
    }

    public long getWkReNoD() {
        return getLong("WK-RE-NO-D");
    }

    public void setWkReNoD(long value) {
        setLong("WK-RE-NO-D", value);
    }

    public int getWkRefRcpt() {
        return getInt("WK-REF-RCPT");
    }

    public void setWkRefRcpt(int value) {
        setInt("WK-REF-RCPT", value);
    }

    public long getWkSessAmt() {
        return getLong("WK-SESS-AMT");
    }

    public void setWkSessAmt(long value) {
        setLong("WK-SESS-AMT", value);
    }

    public int getWkSessCnt() {
        return getInt("WK-SESS-CNT");
    }

    public void setWkSessCnt(int value) {
        setInt("WK-SESS-CNT", value);
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

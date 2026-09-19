package com.sakura.ap0010.domain;

import com.sakura.ap0010.runtime.Ap0010Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for AP0010. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Ap0010FieldAccess extends RuntimeFieldAccess {

    public Ap0010FieldAccess(WorkingStorage ws, Ap0010Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getPayf() != null) {
            register(fileSet.getPayf().buffer());
        }
        if (fileSet != null && fileSet.getAplf() != null) {
            register(fileSet.getAplf().buffer());
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

    public BigDecimal getPlBalance() {
        return getDecimal("PL-BALANCE");
    }

    public void setPlBalance(BigDecimal value) {
        setDecimal("PL-BALANCE", value);
    }

    public int getPlCloseYm() {
        return getInt("PL-CLOSE-YM");
    }

    public void setPlCloseYm(int value) {
        setInt("PL-CLOSE-YM", value);
    }

    public BigDecimal getPlCredit() {
        return getDecimal("PL-CREDIT");
    }

    public void setPlCredit(BigDecimal value) {
        setDecimal("PL-CREDIT", value);
    }

    public int getPlDate() {
        return getInt("PL-DATE");
    }

    public void setPlDate(int value) {
        setInt("PL-DATE", value);
    }

    public BigDecimal getPlDebit() {
        return getDecimal("PL-DEBIT");
    }

    public void setPlDebit(BigDecimal value) {
        setDecimal("PL-DEBIT", value);
    }

    public int getPlKind() {
        return getInt("PL-KIND");
    }

    public void setPlKind(int value) {
        setInt("PL-KIND", value);
    }

    public long getPlRefNo() {
        return getLong("PL-REF-NO");
    }

    public void setPlRefNo(long value) {
        setLong("PL-REF-NO", value);
    }

    public int getPlRefType() {
        return getInt("PL-REF-TYPE");
    }

    public void setPlRefType(int value) {
        setInt("PL-REF-TYPE", value);
    }

    public String getPlRemark() {
        return getString("PL-REMARK");
    }

    public void setPlRemark(String value) {
        setString("PL-REMARK", value);
    }

    public long getPlSeq() {
        return getLong("PL-SEQ");
    }

    public void setPlSeq(long value) {
        setLong("PL-SEQ", value);
    }

    public int getPlSupp() {
        return getInt("PL-SUPP");
    }

    public void setPlSupp(int value) {
        setInt("PL-SUPP", value);
    }

    public int getPlUser() {
        return getInt("PL-USER");
    }

    public void setPlUser(int value) {
        setInt("PL-USER", value);
    }

    public int getPyAddDate() {
        return getInt("PY-ADD-DATE");
    }

    public void setPyAddDate(int value) {
        setInt("PY-ADD-DATE", value);
    }

    public int getPyAddUser() {
        return getInt("PY-ADD-USER");
    }

    public void setPyAddUser(int value) {
        setInt("PY-ADD-USER", value);
    }

    public BigDecimal getPyAmount() {
        return getDecimal("PY-AMOUNT");
    }

    public void setPyAmount(BigDecimal value) {
        setDecimal("PY-AMOUNT", value);
    }

    public int getPyBankCode() {
        return getInt("PY-BANK-CODE");
    }

    public void setPyBankCode(int value) {
        setInt("PY-BANK-CODE", value);
    }

    public int getPyCloseYm() {
        return getInt("PY-CLOSE-YM");
    }

    public void setPyCloseYm(int value) {
        setInt("PY-CLOSE-YM", value);
    }

    public int getPyDate() {
        return getInt("PY-DATE");
    }

    public void setPyDate(int value) {
        setInt("PY-DATE", value);
    }

    public int getPyDelFlag() {
        return getInt("PY-DEL-FLAG");
    }

    public void setPyDelFlag(int value) {
        setInt("PY-DEL-FLAG", value);
    }

    public int getPyMethod() {
        return getInt("PY-METHOD");
    }

    public void setPyMethod(int value) {
        setInt("PY-METHOD", value);
    }

    public long getPyNo() {
        return getLong("PY-NO");
    }

    public void setPyNo(long value) {
        setLong("PY-NO", value);
    }

    public String getPyRemark() {
        return getString("PY-REMARK");
    }

    public void setPyRemark(String value) {
        setString("PY-REMARK", value);
    }

    public int getPyStatus() {
        return getInt("PY-STATUS");
    }

    public void setPyStatus(int value) {
        setInt("PY-STATUS", value);
    }

    public int getPySupp() {
        return getInt("PY-SUPP");
    }

    public void setPySupp(int value) {
        setInt("PY-SUPP", value);
    }

    public BigDecimal getSpBalance() {
        return getDecimal("SP-BALANCE");
    }

    public void setSpBalance(BigDecimal value) {
        setDecimal("SP-BALANCE", value);
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

    public String getSpName() {
        return getString("SP-NAME");
    }

    public void setSpName(String value) {
        setString("SP-NAME", value);
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

    public long getWkPyNoD() {
        return getLong("WK-PY-NO-D");
    }

    public void setWkPyNoD(long value) {
        setLong("WK-PY-NO-D", value);
    }

    public int getWkRefPay() {
        return getInt("WK-REF-PAY");
    }

    public void setWkRefPay(int value) {
        setInt("WK-REF-PAY", value);
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

    public String getWkSuppName() {
        return getString("WK-SUPP-NAME");
    }

    public void setWkSuppName(String value) {
        setString("WK-SUPP-NAME", value);
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

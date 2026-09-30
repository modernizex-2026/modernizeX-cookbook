package com.sakura.bt0020.domain;

import com.sakura.bt0020.runtime.Bt0020Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for BT0020. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Bt0020FieldAccess extends RuntimeFieldAccess {

    public Bt0020FieldAccess(WorkingStorage ws, Bt0020Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getSyscf() != null) {
            register(fileSet.getSyscf().buffer());
        }
        if (fileSet != null && fileSet.getInvhf() != null) {
            register(fileSet.getInvhf().buffer());
        }
        if (fileSet != null && fileSet.getPurhf() != null) {
            register(fileSet.getPurhf().buffer());
        }
        if (fileSet != null && fileSet.getArlf() != null) {
            register(fileSet.getArlf().buffer());
        }
        if (fileSet != null && fileSet.getAplf() != null) {
            register(fileSet.getAplf().buffer());
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

    public int getCompletionCode() {
        return getInt("COMPLETION-CODE");
    }

    public void setCompletionCode(int value) {
        setInt("COMPLETION-CODE", value);
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

    public BigDecimal getIhAmount() {
        return getDecimal("IH-AMOUNT");
    }

    public void setIhAmount(BigDecimal value) {
        setDecimal("IH-AMOUNT", value);
    }

    public int getIhCloseYm() {
        return getInt("IH-CLOSE-YM");
    }

    public void setIhCloseYm(int value) {
        setInt("IH-CLOSE-YM", value);
    }

    public int getIhDate() {
        return getInt("IH-DATE");
    }

    public void setIhDate(int value) {
        setInt("IH-DATE", value);
    }

    public int getIhKind() {
        return getInt("IH-KIND");
    }

    public void setIhKind(int value) {
        setInt("IH-KIND", value);
    }

    public long getIhNo() {
        return getLong("IH-NO");
    }

    public void setIhNo(long value) {
        setLong("IH-NO", value);
    }

    public int getIhStatus() {
        return getInt("IH-STATUS");
    }

    public void setIhStatus(int value) {
        setInt("IH-STATUS", value);
    }

    public BigDecimal getIhTaxAmount() {
        return getDecimal("IH-TAX-AMOUNT");
    }

    public void setIhTaxAmount(BigDecimal value) {
        setDecimal("IH-TAX-AMOUNT", value);
    }

    public int getIhUpdDate() {
        return getInt("IH-UPD-DATE");
    }

    public void setIhUpdDate(int value) {
        setInt("IH-UPD-DATE", value);
    }

    public int getIhUpdUser() {
        return getInt("IH-UPD-USER");
    }

    public void setIhUpdUser(int value) {
        setInt("IH-UPD-USER", value);
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

    public int getPlSupp() {
        return getInt("PL-SUPP");
    }

    public void setPlSupp(int value) {
        setInt("PL-SUPP", value);
    }

    public int getSyCurrYm() {
        return getInt("SY-CURR-YM");
    }

    public void setSyCurrYm(int value) {
        setInt("SY-CURR-YM", value);
    }

    public int getSyKey() {
        return getInt("SY-KEY");
    }

    public void setSyKey(int value) {
        setInt("SY-KEY", value);
    }

    public int getSyLastMonClose() {
        return getInt("SY-LAST-MON-CLOSE");
    }

    public void setSyLastMonClose(int value) {
        setInt("SY-LAST-MON-CLOSE", value);
    }

    public BigDecimal getVhAmount() {
        return getDecimal("VH-AMOUNT");
    }

    public void setVhAmount(BigDecimal value) {
        setDecimal("VH-AMOUNT", value);
    }

    public int getVhCloseYm() {
        return getInt("VH-CLOSE-YM");
    }

    public void setVhCloseYm(int value) {
        setInt("VH-CLOSE-YM", value);
    }

    public int getVhDate() {
        return getInt("VH-DATE");
    }

    public void setVhDate(int value) {
        setInt("VH-DATE", value);
    }

    public int getVhKind() {
        return getInt("VH-KIND");
    }

    public void setVhKind(int value) {
        setInt("VH-KIND", value);
    }

    public long getVhNo() {
        return getLong("VH-NO");
    }

    public void setVhNo(long value) {
        setLong("VH-NO", value);
    }

    public int getVhStatus() {
        return getInt("VH-STATUS");
    }

    public void setVhStatus(int value) {
        setInt("VH-STATUS", value);
    }

    public BigDecimal getVhTaxAmount() {
        return getDecimal("VH-TAX-AMOUNT");
    }

    public void setVhTaxAmount(BigDecimal value) {
        setDecimal("VH-TAX-AMOUNT", value);
    }

    public int getWkAbortFlg() {
        return getInt("WK-ABORT-FLG");
    }

    public void setWkAbortFlg(int value) {
        setInt("WK-ABORT-FLG", value);
    }

    public long getWkApClosebal() {
        return getLong("WK-AP-CLOSEBAL");
    }

    public void setWkApClosebal(long value) {
        setLong("WK-AP-CLOSEBAL", value);
    }

    public long getWkApCredit() {
        return getLong("WK-AP-CREDIT");
    }

    public void setWkApCredit(long value) {
        setLong("WK-AP-CREDIT", value);
    }

    public long getWkApDebit() {
        return getLong("WK-AP-DEBIT");
    }

    public void setWkApDebit(long value) {
        setLong("WK-AP-DEBIT", value);
    }

    public int getWkApRead() {
        return getInt("WK-AP-READ");
    }

    public void setWkApRead(int value) {
        setInt("WK-AP-READ", value);
    }

    public int getWkApStamp() {
        return getInt("WK-AP-STAMP");
    }

    public void setWkApStamp(int value) {
        setInt("WK-AP-STAMP", value);
    }

    public int getWkApSupp() {
        return getInt("WK-AP-SUPP");
    }

    public void setWkApSupp(int value) {
        setInt("WK-AP-SUPP", value);
    }

    public long getWkArClosebal() {
        return getLong("WK-AR-CLOSEBAL");
    }

    public void setWkArClosebal(long value) {
        setLong("WK-AR-CLOSEBAL", value);
    }

    public long getWkArCredit() {
        return getLong("WK-AR-CREDIT");
    }

    public void setWkArCredit(long value) {
        setLong("WK-AR-CREDIT", value);
    }

    public int getWkArCust() {
        return getInt("WK-AR-CUST");
    }

    public void setWkArCust(int value) {
        setInt("WK-AR-CUST", value);
    }

    public long getWkArDebit() {
        return getLong("WK-AR-DEBIT");
    }

    public void setWkArDebit(long value) {
        setLong("WK-AR-DEBIT", value);
    }

    public int getWkArRead() {
        return getInt("WK-AR-READ");
    }

    public void setWkArRead(int value) {
        setInt("WK-AR-READ", value);
    }

    public int getWkArStamp() {
        return getInt("WK-AR-STAMP");
    }

    public void setWkArStamp(int value) {
        setInt("WK-AR-STAMP", value);
    }

    public String getWkConfirm() {
        return getString("WK-CONFIRM");
    }

    public void setWkConfirm(String value) {
        setString("WK-CONFIRM", value);
    }

    public long getWkCustBal() {
        return getLong("WK-CUST-BAL");
    }

    public void setWkCustBal(long value) {
        setLong("WK-CUST-BAL", value);
    }

    public int getWkDfltYm() {
        return getInt("WK-DFLT-YM");
    }

    public void setWkDfltYm(int value) {
        setInt("WK-DFLT-YM", value);
    }

    public long getWkEAmt() {
        return getLong("WK-E-AMT");
    }

    public void setWkEAmt(long value) {
        setLong("WK-E-AMT", value);
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

    public int getWkEYm() {
        return getInt("WK-E-YM");
    }

    public void setWkEYm(int value) {
        setInt("WK-E-YM", value);
    }

    public int getWkFirstFlg() {
        return getInt("WK-FIRST-FLG");
    }

    public void setWkFirstFlg(int value) {
        setInt("WK-FIRST-FLG", value);
    }

    public int getWkIdx() {
        return getInt("WK-IDX");
    }

    public void setWkIdx(int value) {
        setInt("WK-IDX", value);
    }

    public String getWkInLine() {
        return getString("WK-IN-LINE");
    }

    public void setWkInLine(String value) {
        setString("WK-IN-LINE", value);
    }

    public long getWkInvAmt() {
        return getLong("WK-INV-AMT");
    }

    public void setWkInvAmt(long value) {
        setLong("WK-INV-AMT", value);
    }

    public int getWkInvClose() {
        return getInt("WK-INV-CLOSE");
    }

    public void setWkInvClose(int value) {
        setInt("WK-INV-CLOSE", value);
    }

    public int getWkInvRead() {
        return getInt("WK-INV-READ");
    }

    public void setWkInvRead(int value) {
        setInt("WK-INV-READ", value);
    }

    public int getWkInvSkip() {
        return getInt("WK-INV-SKIP");
    }

    public void setWkInvSkip(int value) {
        setInt("WK-INV-SKIP", value);
    }

    public long getWkInvTax() {
        return getLong("WK-INV-TAX");
    }

    public void setWkInvTax(long value) {
        setLong("WK-INV-TAX", value);
    }

    public int getWkNextYm() {
        return getInt("WK-NEXT-YM");
    }

    public void setWkNextYm(int value) {
        setInt("WK-NEXT-YM", value);
    }

    public int getWkPerEnd() {
        return getInt("WK-PER-END");
    }

    public void setWkPerEnd(int value) {
        setInt("WK-PER-END", value);
    }

    public int getWkPerStart() {
        return getInt("WK-PER-START");
    }

    public void setWkPerStart(int value) {
        setInt("WK-PER-START", value);
    }

    public int getWkPrevCust() {
        return getInt("WK-PREV-CUST");
    }

    public void setWkPrevCust(int value) {
        setInt("WK-PREV-CUST", value);
    }

    public int getWkPrevSupp() {
        return getInt("WK-PREV-SUPP");
    }

    public void setWkPrevSupp(int value) {
        setInt("WK-PREV-SUPP", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
    }

    public long getWkPurAmt() {
        return getLong("WK-PUR-AMT");
    }

    public void setWkPurAmt(long value) {
        setLong("WK-PUR-AMT", value);
    }

    public int getWkPurClose() {
        return getInt("WK-PUR-CLOSE");
    }

    public void setWkPurClose(int value) {
        setInt("WK-PUR-CLOSE", value);
    }

    public int getWkPurRead() {
        return getInt("WK-PUR-READ");
    }

    public void setWkPurRead(int value) {
        setInt("WK-PUR-READ", value);
    }

    public int getWkPurSkip() {
        return getInt("WK-PUR-SKIP");
    }

    public void setWkPurSkip(int value) {
        setInt("WK-PUR-SKIP", value);
    }

    public long getWkPurTax() {
        return getLong("WK-PUR-TAX");
    }

    public void setWkPurTax(long value) {
        setLong("WK-PUR-TAX", value);
    }

    public long getWkSuppBal() {
        return getLong("WK-SUPP-BAL");
    }

    public void setWkSuppBal(long value) {
        setLong("WK-SUPP-BAL", value);
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

    public int getWkTgtYm() {
        return getInt("WK-TGT-YM");
    }

    public void setWkTgtYm(int value) {
        setInt("WK-TGT-YM", value);
    }

    public int getWkUserCode() {
        return getInt("WK-USER-CODE");
    }

    public void setWkUserCode(int value) {
        setInt("WK-USER-CODE", value);
    }
}

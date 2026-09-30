package com.sakura.bt0050.domain;

import com.sakura.bt0050.runtime.Bt0050Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for BT0050. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Bt0050FieldAccess extends RuntimeFieldAccess {

    public Bt0050FieldAccess(WorkingStorage ws, Bt0050Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getAplf() != null) {
            register(fileSet.getAplf().buffer());
        }
        if (fileSet != null && fileSet.getSuppf() != null) {
            register(fileSet.getSuppf().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
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

    public int getWkAbortFlg() {
        return getInt("WK-ABORT-FLG");
    }

    public void setWkAbortFlg(int value) {
        setInt("WK-ABORT-FLG", value);
    }

    public long getWkBalTot() {
        return getLong("WK-BAL-TOT");
    }

    public void setWkBalTot(long value) {
        setLong("WK-BAL-TOT", value);
    }

    public String getWkConfirm() {
        return getString("WK-CONFIRM");
    }

    public void setWkConfirm(String value) {
        setString("WK-CONFIRM", value);
    }

    public long getWkCreditTot() {
        return getLong("WK-CREDIT-TOT");
    }

    public void setWkCreditTot(long value) {
        setLong("WK-CREDIT-TOT", value);
    }

    public long getWkDebitTot() {
        return getLong("WK-DEBIT-TOT");
    }

    public void setWkDebitTot(long value) {
        setLong("WK-DEBIT-TOT", value);
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

    public int getWkECode() {
        return getInt("WK-E-CODE");
    }

    public void setWkECode(int value) {
        setInt("WK-E-CODE", value);
    }

    public int getWkFirstFlg() {
        return getInt("WK-FIRST-FLG");
    }

    public void setWkFirstFlg(int value) {
        setInt("WK-FIRST-FLG", value);
    }

    public int getWkLedgCnt() {
        return getInt("WK-LEDG-CNT");
    }

    public void setWkLedgCnt(int value) {
        setInt("WK-LEDG-CNT", value);
    }

    public int getWkNfCnt() {
        return getInt("WK-NF-CNT");
    }

    public void setWkNfCnt(int value) {
        setInt("WK-NF-CNT", value);
    }

    public int getWkNonzeroCnt() {
        return getInt("WK-NONZERO-CNT");
    }

    public void setWkNonzeroCnt(int value) {
        setInt("WK-NONZERO-CNT", value);
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

    public long getWkRunBal() {
        return getLong("WK-RUN-BAL");
    }

    public void setWkRunBal(long value) {
        setLong("WK-RUN-BAL", value);
    }

    public int getWkSuppCnt() {
        return getInt("WK-SUPP-CNT");
    }

    public void setWkSuppCnt(int value) {
        setInt("WK-SUPP-CNT", value);
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

    public int getWkUpdCnt() {
        return getInt("WK-UPD-CNT");
    }

    public void setWkUpdCnt(int value) {
        setInt("WK-UPD-CNT", value);
    }

    public int getWkUserCode() {
        return getInt("WK-USER-CODE");
    }

    public void setWkUserCode(int value) {
        setInt("WK-USER-CODE", value);
    }

    public int getWkZeroClr() {
        return getInt("WK-ZERO-CLR");
    }

    public void setWkZeroClr(int value) {
        setInt("WK-ZERO-CLR", value);
    }

    public int getWkZeroRead() {
        return getInt("WK-ZERO-READ");
    }

    public void setWkZeroRead(int value) {
        setInt("WK-ZERO-READ", value);
    }
}

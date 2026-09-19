package com.sakura.ar0020.domain;

import com.sakura.ar0020.runtime.Ar0020Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for AR0020. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Ar0020FieldAccess extends RuntimeFieldAccess {

    public Ar0020FieldAccess(WorkingStorage ws, Ar0020Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getArlf() != null) {
            register(fileSet.getArlf().buffer());
        }
        if (fileSet != null && fileSet.getCustf() != null) {
            register(fileSet.getCustf().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public BigDecimal getAlBalance() {
        return getDecimal("AL-BALANCE");
    }

    public void setAlBalance(BigDecimal value) {
        setDecimal("AL-BALANCE", value);
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

    public BigDecimal getCuCreditLimit() {
        return getDecimal("CU-CREDIT-LIMIT");
    }

    public void setCuCreditLimit(BigDecimal value) {
        setDecimal("CU-CREDIT-LIMIT", value);
    }

    public String getCuName() {
        return getString("CU-NAME");
    }

    public void setCuName(String value) {
        setString("CU-NAME", value);
    }

    public int getEndFlg() {
        return getInt("END-FLG");
    }

    public void setEndFlg(int value) {
        setInt("END-FLG", value);
    }

    public int getEofCust() {
        return getInt("EOF-CUST");
    }

    public void setEofCust(int value) {
        setInt("EOF-CUST", value);
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

    public String getWkAll(int index) {
        return getString("WK-ALL", index);
    }

    public void setWkAll(int index, String value) {
        setString("WK-ALL", value, index);
    }

    public long getWkB030() {
        return getLong("WK-B030");
    }

    public void setWkB030(long value) {
        setLong("WK-B030", value);
    }

    public long getWkB060() {
        return getLong("WK-B060");
    }

    public void setWkB060(long value) {
        setLong("WK-B060", value);
    }

    public long getWkB090() {
        return getLong("WK-B090");
    }

    public void setWkB090(long value) {
        setLong("WK-B090", value);
    }

    public long getWkB90p() {
        return getLong("WK-B90P");
    }

    public void setWkB90p(long value) {
        setLong("WK-B90P", value);
    }

    public long getWkCrLimit() {
        return getLong("WK-CR-LIMIT");
    }

    public void setWkCrLimit(long value) {
        setLong("WK-CR-LIMIT", value);
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

    public int getWkDays() {
        return getInt("WK-DAYS");
    }

    public void setWkDays(int value) {
        setInt("WK-DAYS", value);
    }

    public String getWkDl() {
        return groupToString("WK-DL");
    }

    public void setWkDl(String value) {
        setGroup("WK-DL", value);
    }

    public long getWkDlBal() {
        return getLong("WK-DL-BAL");
    }

    public void setWkDlBal(long value) {
        setLong("WK-DL-BAL", value);
    }

    public long getWkDlCr() {
        return getLong("WK-DL-CR");
    }

    public void setWkDlCr(long value) {
        setLong("WK-DL-CR", value);
    }

    public int getWkDlDate() {
        return getInt("WK-DL-DATE");
    }

    public void setWkDlDate(int value) {
        setInt("WK-DL-DATE", value);
    }

    public long getWkDlDr() {
        return getLong("WK-DL-DR");
    }

    public void setWkDlDr(long value) {
        setLong("WK-DL-DR", value);
    }

    public String getWkDlKind() {
        return getString("WK-DL-KIND");
    }

    public void setWkDlKind(String value) {
        setString("WK-DL-KIND", value);
    }

    public String getWkFkeyLine() {
        return getString("WK-FKEY-LINE");
    }

    public void setWkFkeyLine(String value) {
        setString("WK-FKEY-LINE", value);
    }

    public int getWkIdx() {
        return getInt("WK-IDX");
    }

    public void setWkIdx(int value) {
        setInt("WK-IDX", value);
    }

    public int getWkKeyCust() {
        return getInt("WK-KEY-CUST");
    }

    public void setWkKeyCust(int value) {
        setInt("WK-KEY-CUST", value);
    }

    public String getWkKindLbl() {
        return getString("WK-KIND-LBL");
    }

    public void setWkKindLbl(String value) {
        setString("WK-KIND-LBL", value);
    }

    public int getWkLcnt() {
        return getInt("WK-LCNT");
    }

    public void setWkLcnt(int value) {
        setInt("WK-LCNT", value);
    }

    public String getWkList(int index) {
        return getString("WK-LIST", index);
    }

    public void setWkList(int index, String value) {
        setString("WK-LIST", value, index);
    }

    public String getWkMsgLine() {
        return getString("WK-MSG-LINE");
    }

    public void setWkMsgLine(String value) {
        setString("WK-MSG-LINE", value);
    }

    public long getWkNet() {
        return getLong("WK-NET");
    }

    public void setWkNet(long value) {
        setLong("WK-NET", value);
    }

    public String getWkOver() {
        return getString("WK-OVER");
    }

    public void setWkOver(String value) {
        setString("WK-OVER", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
    }

    public int getWkRow() {
        return getInt("WK-ROW");
    }

    public void setWkRow(int value) {
        setInt("WK-ROW", value);
    }

    public int getWkStart() {
        return getInt("WK-START");
    }

    public void setWkStart(int value) {
        setInt("WK-START", value);
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

    public long getWkTotCr() {
        return getLong("WK-TOT-CR");
    }

    public void setWkTotCr(long value) {
        setLong("WK-TOT-CR", value);
    }

    public long getWkTotDr() {
        return getLong("WK-TOT-DR");
    }

    public void setWkTotDr(long value) {
        setLong("WK-TOT-DR", value);
    }
}

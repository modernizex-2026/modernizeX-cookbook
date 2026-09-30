package com.sakura.ap0020.domain;

import com.sakura.ap0020.runtime.Ap0020Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for AP0020. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Ap0020FieldAccess extends RuntimeFieldAccess {

    public Ap0020FieldAccess(WorkingStorage ws, Ap0020Datasets fileSet) {
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

    public int getEndFlg() {
        return getInt("END-FLG");
    }

    public void setEndFlg(int value) {
        setInt("END-FLG", value);
    }

    public int getEofSupp() {
        return getInt("EOF-SUPP");
    }

    public void setEofSupp(int value) {
        setInt("EOF-SUPP", value);
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

    public int getPlKind() {
        return getInt("PL-KIND");
    }

    public void setPlKind(int value) {
        setInt("PL-KIND", value);
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

    public String getSpName() {
        return getString("SP-NAME");
    }

    public void setSpName(String value) {
        setString("SP-NAME", value);
    }

    public String getWkAll(int index) {
        return getString("WK-ALL", index);
    }

    public void setWkAll(int index, String value) {
        setString("WK-ALL", value, index);
    }

    public long getWkCurBal() {
        return getLong("WK-CUR-BAL");
    }

    public void setWkCurBal(long value) {
        setLong("WK-CUR-BAL", value);
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

    public int getWkKeySupp() {
        return getInt("WK-KEY-SUPP");
    }

    public void setWkKeySupp(int value) {
        setInt("WK-KEY-SUPP", value);
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

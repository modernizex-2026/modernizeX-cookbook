package com.sakura.rp0080.domain;

import com.sakura.rp0080.runtime.Rp0080Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for RP0080. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Rp0080FieldAccess extends RuntimeFieldAccess {

    public Rp0080FieldAccess(WorkingStorage ws, Rp0080Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getAplf() != null) {
            register(fileSet.getAplf().buffer());
        }
        if (fileSet != null && fileSet.getSuppf() != null) {
            register(fileSet.getSuppf().buffer());
        }
        if (fileSet != null && fileSet.getSyscf() != null) {
            register(fileSet.getSyscf().buffer());
        }
        if (fileSet != null && fileSet.getRepf() != null) {
            register(fileSet.getRepf().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getCompletionCode() {
        return getInt("COMPLETION-CODE");
    }

    public void setCompletionCode(int value) {
        setInt("COMPLETION-CODE", value);
    }

    public String getFsts() {
        return getString("FSTS");
    }

    public void setFsts(String value) {
        setString("FSTS", value);
    }

    public String getH1Company() {
        return getString("H1-COMPANY");
    }

    public void setH1Company(String value) {
        setString("H1-COMPANY", value);
    }

    public int getH1Page() {
        return getInt("H1-PAGE");
    }

    public void setH1Page(int value) {
        setInt("H1-PAGE", value);
    }

    public String getH1Title() {
        return getString("H1-TITLE");
    }

    public void setH1Title(String value) {
        setString("H1-TITLE", value);
    }

    public int getH2Date() {
        return getInt("H2-DATE");
    }

    public void setH2Date(int value) {
        setInt("H2-DATE", value);
    }

    public String getH2Info() {
        return getString("H2-INFO");
    }

    public void setH2Info(String value) {
        setString("H2-INFO", value);
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

    public String getRdLine() {
        return groupToString("RD-LINE");
    }

    public void setRdLine(String value) {
        setGroup("RD-LINE", value);
    }

    public String getRepRec() {
        return getString("REP-REC");
    }

    public void setRepRec(String value) {
        setString("REP-REC", value);
    }

    public String getRhLine() {
        return groupToString("RH-LINE");
    }

    public void setRhLine(String value) {
        setGroup("RH-LINE", value);
    }

    public String getRptH1() {
        return groupToString("RPT-H1");
    }

    public void setRptH1(String value) {
        setGroup("RPT-H1", value);
    }

    public String getRptH2() {
        return groupToString("RPT-H2");
    }

    public void setRptH2(String value) {
        setGroup("RPT-H2", value);
    }

    public String getRptRule() {
        return getString("RPT-RULE");
    }

    public void setRptRule(String value) {
        setString("RPT-RULE", value);
    }

    public long getRsBal() {
        return getLong("RS-BAL");
    }

    public void setRsBal(long value) {
        setLong("RS-BAL", value);
    }

    public int getRsCode() {
        return getInt("RS-CODE");
    }

    public void setRsCode(int value) {
        setInt("RS-CODE", value);
    }

    public long getRsCr() {
        return getLong("RS-CR");
    }

    public void setRsCr(long value) {
        setLong("RS-CR", value);
    }

    public long getRsDr() {
        return getLong("RS-DR");
    }

    public void setRsDr(long value) {
        setLong("RS-DR", value);
    }

    public String getRsName() {
        return getString("RS-NAME");
    }

    public void setRsName(String value) {
        setString("RS-NAME", value);
    }

    public long getRtBal() {
        return getLong("RT-BAL");
    }

    public void setRtBal(long value) {
        setLong("RT-BAL", value);
    }

    public long getRtCr() {
        return getLong("RT-CR");
    }

    public void setRtCr(long value) {
        setLong("RT-CR", value);
    }

    public long getRtDr() {
        return getLong("RT-DR");
    }

    public void setRtDr(long value) {
        setLong("RT-DR", value);
    }

    public String getRtLine() {
        return groupToString("RT-LINE");
    }

    public void setRtLine(String value) {
        setGroup("RT-LINE", value);
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

    public String getSyCompanyName() {
        return getString("SY-COMPANY-NAME");
    }

    public void setSyCompanyName(String value) {
        setString("SY-COMPANY-NAME", value);
    }

    public int getSyKey() {
        return getInt("SY-KEY");
    }

    public void setSyKey(int value) {
        setInt("SY-KEY", value);
    }

    public String getWkCompany() {
        return getString("WK-COMPANY");
    }

    public void setWkCompany(String value) {
        setString("WK-COMPANY", value);
    }

    public BigDecimal getWkCurBal() {
        return getDecimal("WK-CUR-BAL");
    }

    public void setWkCurBal(BigDecimal value) {
        setDecimal("WK-CUR-BAL", value);
    }

    public int getWkCurCnt() {
        return getInt("WK-CUR-CNT");
    }

    public void setWkCurCnt(int value) {
        setInt("WK-CUR-CNT", value);
    }

    public BigDecimal getWkCurCr() {
        return getDecimal("WK-CUR-CR");
    }

    public void setWkCurCr(BigDecimal value) {
        setDecimal("WK-CUR-CR", value);
    }

    public BigDecimal getWkCurDr() {
        return getDecimal("WK-CUR-DR");
    }

    public void setWkCurDr(BigDecimal value) {
        setDecimal("WK-CUR-DR", value);
    }

    public int getWkCurSupp() {
        return getInt("WK-CUR-SUPP");
    }

    public void setWkCurSupp(int value) {
        setInt("WK-CUR-SUPP", value);
    }

    public int getWkFirst() {
        return getInt("WK-FIRST");
    }

    public void setWkFirst(int value) {
        setInt("WK-FIRST", value);
    }

    public BigDecimal getWkGBal() {
        return getDecimal("WK-G-BAL");
    }

    public void setWkGBal(BigDecimal value) {
        setDecimal("WK-G-BAL", value);
    }

    public int getWkGCnt() {
        return getInt("WK-G-CNT");
    }

    public void setWkGCnt(int value) {
        setInt("WK-G-CNT", value);
    }

    public BigDecimal getWkGCr() {
        return getDecimal("WK-G-CR");
    }

    public void setWkGCr(BigDecimal value) {
        setDecimal("WK-G-CR", value);
    }

    public BigDecimal getWkGDr() {
        return getDecimal("WK-G-DR");
    }

    public void setWkGDr(BigDecimal value) {
        setDecimal("WK-G-DR", value);
    }

    public int getWkLine() {
        return getInt("WK-LINE");
    }

    public void setWkLine(int value) {
        setInt("WK-LINE", value);
    }

    public int getWkMainEof() {
        return getInt("WK-MAIN-EOF");
    }

    public void setWkMainEof(int value) {
        setInt("WK-MAIN-EOF", value);
    }

    public int getWkPage() {
        return getInt("WK-PAGE");
    }

    public void setWkPage(int value) {
        setInt("WK-PAGE", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
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

    public String getWkTitle() {
        return getString("WK-TITLE");
    }

    public void setWkTitle(String value) {
        setString("WK-TITLE", value);
    }

    /* ── Byte-level copy wrappers (preserves COMP-3/BINARY raw bytes) ── */
    public void copyRepRecFromRdLine() {
        copyBytes("REP-REC", "RD-LINE");
    }

    public void copyRepRecFromRhLine() {
        copyBytes("REP-REC", "RH-LINE");
    }

    public void copyRepRecFromRptH1() {
        copyBytes("REP-REC", "RPT-H1");
    }

    public void copyRepRecFromRptH2() {
        copyBytes("REP-REC", "RPT-H2");
    }

    public void copyRepRecFromRtLine() {
        copyBytes("REP-REC", "RT-LINE");
    }
}

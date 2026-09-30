package com.sakura.rp0070.domain;

import com.sakura.rp0070.runtime.Rp0070Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for RP0070. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Rp0070FieldAccess extends RuntimeFieldAccess {

    public Rp0070FieldAccess(WorkingStorage ws, Rp0070Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getArlf() != null) {
            register(fileSet.getArlf().buffer());
        }
        if (fileSet != null && fileSet.getCustf() != null) {
            register(fileSet.getCustf().buffer());
        }
        if (fileSet != null && fileSet.getSyscf() != null) {
            register(fileSet.getSyscf().buffer());
        }
        if (fileSet != null && fileSet.getRepf() != null) {
            register(fileSet.getRepf().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
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

    public int getCuCode() {
        return getInt("CU-CODE");
    }

    public void setCuCode(int value) {
        setInt("CU-CODE", value);
    }

    public String getCuName() {
        return getString("CU-NAME");
    }

    public void setCuName(String value) {
        setString("CU-NAME", value);
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

    public long getRaB0() {
        return getLong("RA-B0");
    }

    public void setRaB0(long value) {
        setLong("RA-B0", value);
    }

    public long getRaB1() {
        return getLong("RA-B1");
    }

    public void setRaB1(long value) {
        setLong("RA-B1", value);
    }

    public long getRaB2() {
        return getLong("RA-B2");
    }

    public void setRaB2(long value) {
        setLong("RA-B2", value);
    }

    public long getRaB3() {
        return getLong("RA-B3");
    }

    public void setRaB3(long value) {
        setLong("RA-B3", value);
    }

    public int getRaCode() {
        return getInt("RA-CODE");
    }

    public void setRaCode(int value) {
        setInt("RA-CODE", value);
    }

    public String getRaName() {
        return getString("RA-NAME");
    }

    public void setRaName(String value) {
        setString("RA-NAME", value);
    }

    public long getRaTot() {
        return getLong("RA-TOT");
    }

    public void setRaTot(long value) {
        setLong("RA-TOT", value);
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

    public long getRtB0() {
        return getLong("RT-B0");
    }

    public void setRtB0(long value) {
        setLong("RT-B0", value);
    }

    public long getRtB1() {
        return getLong("RT-B1");
    }

    public void setRtB1(long value) {
        setLong("RT-B1", value);
    }

    public long getRtB2() {
        return getLong("RT-B2");
    }

    public void setRtB2(long value) {
        setLong("RT-B2", value);
    }

    public long getRtB3() {
        return getLong("RT-B3");
    }

    public void setRtB3(long value) {
        setLong("RT-B3", value);
    }

    public String getRtLine() {
        return groupToString("RT-LINE");
    }

    public void setRtLine(String value) {
        setGroup("RT-LINE", value);
    }

    public long getRtTot() {
        return getLong("RT-TOT");
    }

    public void setRtTot(long value) {
        setLong("RT-TOT", value);
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

    public int getWkAge() {
        return getInt("WK-AGE");
    }

    public void setWkAge(int value) {
        setInt("WK-AGE", value);
    }

    public String getWkCompany() {
        return getString("WK-COMPANY");
    }

    public void setWkCompany(String value) {
        setString("WK-COMPANY", value);
    }

    public BigDecimal getWkCurB0() {
        return getDecimal("WK-CUR-B0");
    }

    public void setWkCurB0(BigDecimal value) {
        setDecimal("WK-CUR-B0", value);
    }

    public BigDecimal getWkCurB1() {
        return getDecimal("WK-CUR-B1");
    }

    public void setWkCurB1(BigDecimal value) {
        setDecimal("WK-CUR-B1", value);
    }

    public BigDecimal getWkCurB2() {
        return getDecimal("WK-CUR-B2");
    }

    public void setWkCurB2(BigDecimal value) {
        setDecimal("WK-CUR-B2", value);
    }

    public BigDecimal getWkCurB3() {
        return getDecimal("WK-CUR-B3");
    }

    public void setWkCurB3(BigDecimal value) {
        setDecimal("WK-CUR-B3", value);
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

    public int getWkCurCust() {
        return getInt("WK-CUR-CUST");
    }

    public void setWkCurCust(int value) {
        setInt("WK-CUR-CUST", value);
    }

    public String getWkCustName() {
        return getString("WK-CUST-NAME");
    }

    public void setWkCustName(String value) {
        setString("WK-CUST-NAME", value);
    }

    public int getWkFirst() {
        return getInt("WK-FIRST");
    }

    public void setWkFirst(int value) {
        setInt("WK-FIRST", value);
    }

    public BigDecimal getWkGB0() {
        return getDecimal("WK-G-B0");
    }

    public void setWkGB0(BigDecimal value) {
        setDecimal("WK-G-B0", value);
    }

    public BigDecimal getWkGB1() {
        return getDecimal("WK-G-B1");
    }

    public void setWkGB1(BigDecimal value) {
        setDecimal("WK-G-B1", value);
    }

    public BigDecimal getWkGB2() {
        return getDecimal("WK-G-B2");
    }

    public void setWkGB2(BigDecimal value) {
        setDecimal("WK-G-B2", value);
    }

    public BigDecimal getWkGB3() {
        return getDecimal("WK-G-B3");
    }

    public void setWkGB3(BigDecimal value) {
        setDecimal("WK-G-B3", value);
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

    public BigDecimal getWkNet() {
        return getDecimal("WK-NET");
    }

    public void setWkNet(BigDecimal value) {
        setDecimal("WK-NET", value);
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

package com.sakura.rp0040.domain;

import com.sakura.rp0040.runtime.Rp0040Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for RP0040. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Rp0040FieldAccess extends RuntimeFieldAccess {

    public Rp0040FieldAccess(WorkingStorage ws, Rp0040Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getInvhf() != null) {
            register(fileSet.getInvhf().buffer());
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

    public BigDecimal getIhAmount() {
        return getDecimal("IH-AMOUNT");
    }

    public void setIhAmount(BigDecimal value) {
        setDecimal("IH-AMOUNT", value);
    }

    public int getIhCust() {
        return getInt("IH-CUST");
    }

    public void setIhCust(int value) {
        setInt("IH-CUST", value);
    }

    public int getIhDate() {
        return getInt("IH-DATE");
    }

    public void setIhDate(int value) {
        setInt("IH-DATE", value);
    }

    public int getIhDelFlag() {
        return getInt("IH-DEL-FLAG");
    }

    public void setIhDelFlag(int value) {
        setInt("IH-DEL-FLAG", value);
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

    public BigDecimal getIhTotal() {
        return getDecimal("IH-TOTAL");
    }

    public void setIhTotal(BigDecimal value) {
        setDecimal("IH-TOTAL", value);
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

    public int getRcCode() {
        return getInt("RC-CODE");
    }

    public void setRcCode(int value) {
        setInt("RC-CODE", value);
    }

    public int getRcCount() {
        return getInt("RC-COUNT");
    }

    public void setRcCount(int value) {
        setInt("RC-COUNT", value);
    }

    public String getRcName() {
        return getString("RC-NAME");
    }

    public void setRcName(String value) {
        setString("RC-NAME", value);
    }

    public long getRcNet() {
        return getLong("RC-NET");
    }

    public void setRcNet(long value) {
        setLong("RC-NET", value);
    }

    public long getRcTax() {
        return getLong("RC-TAX");
    }

    public void setRcTax(long value) {
        setLong("RC-TAX", value);
    }

    public long getRcTot() {
        return getLong("RC-TOT");
    }

    public void setRcTot(long value) {
        setLong("RC-TOT", value);
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

    public int getRtCount() {
        return getInt("RT-COUNT");
    }

    public void setRtCount(int value) {
        setInt("RT-COUNT", value);
    }

    public String getRtLine() {
        return groupToString("RT-LINE");
    }

    public void setRtLine(String value) {
        setGroup("RT-LINE", value);
    }

    public long getRtNet() {
        return getLong("RT-NET");
    }

    public void setRtNet(long value) {
        setLong("RT-NET", value);
    }

    public long getRtTax() {
        return getLong("RT-TAX");
    }

    public void setRtTax(long value) {
        setLong("RT-TAX", value);
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

    public String getWkCompany() {
        return getString("WK-COMPANY");
    }

    public void setWkCompany(String value) {
        setString("WK-COMPANY", value);
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

    public BigDecimal getWkCurNet() {
        return getDecimal("WK-CUR-NET");
    }

    public void setWkCurNet(BigDecimal value) {
        setDecimal("WK-CUR-NET", value);
    }

    public BigDecimal getWkCurTax() {
        return getDecimal("WK-CUR-TAX");
    }

    public void setWkCurTax(BigDecimal value) {
        setDecimal("WK-CUR-TAX", value);
    }

    public BigDecimal getWkCurTot() {
        return getDecimal("WK-CUR-TOT");
    }

    public void setWkCurTot(BigDecimal value) {
        setDecimal("WK-CUR-TOT", value);
    }

    public int getWkCustCnt() {
        return getInt("WK-CUST-CNT");
    }

    public void setWkCustCnt(int value) {
        setInt("WK-CUST-CNT", value);
    }

    public String getWkCustName() {
        return getString("WK-CUST-NAME");
    }

    public void setWkCustName(String value) {
        setString("WK-CUST-NAME", value);
    }

    public int getWkDateFrom() {
        return getInt("WK-DATE-FROM");
    }

    public void setWkDateFrom(int value) {
        setInt("WK-DATE-FROM", value);
    }

    public int getWkDateTo() {
        return getInt("WK-DATE-TO");
    }

    public void setWkDateTo(int value) {
        setInt("WK-DATE-TO", value);
    }

    public int getWkFirst() {
        return getInt("WK-FIRST");
    }

    public void setWkFirst(int value) {
        setInt("WK-FIRST", value);
    }

    public int getWkGCnt() {
        return getInt("WK-G-CNT");
    }

    public void setWkGCnt(int value) {
        setInt("WK-G-CNT", value);
    }

    public BigDecimal getWkGNet() {
        return getDecimal("WK-G-NET");
    }

    public void setWkGNet(BigDecimal value) {
        setDecimal("WK-G-NET", value);
    }

    public BigDecimal getWkGTax() {
        return getDecimal("WK-G-TAX");
    }

    public void setWkGTax(BigDecimal value) {
        setDecimal("WK-G-TAX", value);
    }

    public BigDecimal getWkGTot() {
        return getDecimal("WK-G-TOT");
    }

    public void setWkGTot(BigDecimal value) {
        setDecimal("WK-G-TOT", value);
    }

    public String getWkInFrom() {
        return getString("WK-IN-FROM");
    }

    public void setWkInFrom(String value) {
        setString("WK-IN-FROM", value);
    }

    public String getWkInTo() {
        return getString("WK-IN-TO");
    }

    public void setWkInTo(String value) {
        setString("WK-IN-TO", value);
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

package com.sakura.rp0010.domain;

import com.sakura.rp0010.runtime.Rp0010Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for RP0010. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Rp0010FieldAccess extends RuntimeFieldAccess {

    public Rp0010FieldAccess(WorkingStorage ws, Rp0010Datasets fileSet) {
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

    public long getRcBalance() {
        return getLong("RC-BALANCE");
    }

    public void setRcBalance(long value) {
        setLong("RC-BALANCE", value);
    }

    public int getRcCode() {
        return getInt("RC-CODE");
    }

    public void setRcCode(int value) {
        setInt("RC-CODE", value);
    }

    public long getRcCredit() {
        return getLong("RC-CREDIT");
    }

    public void setRcCredit(long value) {
        setLong("RC-CREDIT", value);
    }

    public String getRcName() {
        return getString("RC-NAME");
    }

    public void setRcName(String value) {
        setString("RC-NAME", value);
    }

    public String getRcRegion() {
        return getString("RC-REGION");
    }

    public void setRcRegion(String value) {
        setString("RC-REGION", value);
    }

    public String getRcStaff() {
        return getString("RC-STAFF");
    }

    public void setRcStaff(String value) {
        setString("RC-STAFF", value);
    }

    public String getRdCust() {
        return groupToString("RD-CUST");
    }

    public void setRdCust(String value) {
        setGroup("RD-CUST", value);
    }

    public String getRepRec() {
        return getString("REP-REC");
    }

    public void setRepRec(String value) {
        setString("REP-REC", value);
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

    public String getRhCust() {
        return groupToString("RH-CUST");
    }

    public void setRhCust(String value) {
        setGroup("RH-CUST", value);
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

    public long getRtBalance() {
        return getLong("RT-BALANCE");
    }

    public void setRtBalance(long value) {
        setLong("RT-BALANCE", value);
    }

    public long getRtCredit() {
        return getLong("RT-CREDIT");
    }

    public void setRtCredit(long value) {
        setLong("RT-CREDIT", value);
    }

    public String getRtCust() {
        return groupToString("RT-CUST");
    }

    public void setRtCust(String value) {
        setGroup("RT-CUST", value);
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

    public int getWkCnt() {
        return getInt("WK-CNT");
    }

    public void setWkCnt(int value) {
        setInt("WK-CNT", value);
    }

    public String getWkCompany() {
        return getString("WK-COMPANY");
    }

    public void setWkCompany(String value) {
        setString("WK-COMPANY", value);
    }

    public int getWkCustFrom() {
        return getInt("WK-CUST-FROM");
    }

    public void setWkCustFrom(int value) {
        setInt("WK-CUST-FROM", value);
    }

    public int getWkCustTo() {
        return getInt("WK-CUST-TO");
    }

    public void setWkCustTo(int value) {
        setInt("WK-CUST-TO", value);
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

    public String getWkRegionName() {
        return getString("WK-REGION-NAME");
    }

    public void setWkRegionName(String value) {
        setString("WK-REGION-NAME", value);
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

    public String getWkTitle() {
        return getString("WK-TITLE");
    }

    public void setWkTitle(String value) {
        setString("WK-TITLE", value);
    }

    public BigDecimal getWkTotBal() {
        return getDecimal("WK-TOT-BAL");
    }

    public void setWkTotBal(BigDecimal value) {
        setDecimal("WK-TOT-BAL", value);
    }

    public BigDecimal getWkTotCredit() {
        return getDecimal("WK-TOT-CREDIT");
    }

    public void setWkTotCredit(BigDecimal value) {
        setDecimal("WK-TOT-CREDIT", value);
    }

    /* ── Byte-level copy wrappers (preserves COMP-3/BINARY raw bytes) ── */
    public void copyRepRecFromRdCust() {
        copyBytes("REP-REC", "RD-CUST");
    }

    public void copyRepRecFromRhCust() {
        copyBytes("REP-REC", "RH-CUST");
    }

    public void copyRepRecFromRptH1() {
        copyBytes("REP-REC", "RPT-H1");
    }

    public void copyRepRecFromRptH2() {
        copyBytes("REP-REC", "RPT-H2");
    }

    public void copyRepRecFromRtCust() {
        copyBytes("REP-REC", "RT-CUST");
    }
}

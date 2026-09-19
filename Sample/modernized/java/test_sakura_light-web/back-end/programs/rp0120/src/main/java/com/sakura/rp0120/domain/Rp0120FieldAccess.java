package com.sakura.rp0120.domain;

import com.sakura.rp0120.runtime.Rp0120Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for RP0120. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Rp0120FieldAccess extends RuntimeFieldAccess {

    public Rp0120FieldAccess(WorkingStorage ws, Rp0120Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getInvhf() != null) {
            register(fileSet.getInvhf().buffer());
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

    public BigDecimal getIhCostTotal() {
        return getDecimal("IH-COST-TOTAL");
    }

    public void setIhCostTotal(BigDecimal value) {
        setDecimal("IH-COST-TOTAL", value);
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

    public long getIhNo() {
        return getLong("IH-NO");
    }

    public void setIhNo(long value) {
        setLong("IH-NO", value);
    }

    public int getIhStaff() {
        return getInt("IH-STAFF");
    }

    public void setIhStaff(int value) {
        setInt("IH-STAFF", value);
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

    public String getRcHead() {
        return groupToString("RC-HEAD");
    }

    public void setRcHead(String value) {
        setGroup("RC-HEAD", value);
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

    public int getRlCnt() {
        return getInt("RL-CNT");
    }

    public void setRlCnt(int value) {
        setInt("RL-CNT", value);
    }

    public int getRlCode() {
        return getInt("RL-CODE");
    }

    public void setRlCode(int value) {
        setInt("RL-CODE", value);
    }

    public long getRlMargin() {
        return getLong("RL-MARGIN");
    }

    public void setRlMargin(long value) {
        setLong("RL-MARGIN", value);
    }

    public String getRlName() {
        return getString("RL-NAME");
    }

    public void setRlName(String value) {
        setString("RL-NAME", value);
    }

    public long getRlNet() {
        return getLong("RL-NET");
    }

    public void setRlNet(long value) {
        setLong("RL-NET", value);
    }

    public long getRlTax() {
        return getLong("RL-TAX");
    }

    public void setRlTax(long value) {
        setLong("RL-TAX", value);
    }

    public long getRlTot() {
        return getLong("RL-TOT");
    }

    public void setRlTot(long value) {
        setLong("RL-TOT", value);
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

    public int getRtCnt() {
        return getInt("RT-CNT");
    }

    public void setRtCnt(int value) {
        setInt("RT-CNT", value);
    }

    public String getRtLine() {
        return groupToString("RT-LINE");
    }

    public void setRtLine(String value) {
        setGroup("RT-LINE", value);
    }

    public long getRtMargin() {
        return getLong("RT-MARGIN");
    }

    public void setRtMargin(long value) {
        setLong("RT-MARGIN", value);
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

    public String getWkCompany() {
        return getString("WK-COMPANY");
    }

    public void setWkCompany(String value) {
        setString("WK-COMPANY", value);
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

    public int getWkFoundIdx() {
        return getInt("WK-FOUND-IDX");
    }

    public void setWkFoundIdx(int value) {
        setInt("WK-FOUND-IDX", value);
    }

    public BigDecimal getWkGCnt() {
        return getDecimal("WK-G-CNT");
    }

    public void setWkGCnt(BigDecimal value) {
        setDecimal("WK-G-CNT", value);
    }

    public BigDecimal getWkGMargin() {
        return getDecimal("WK-G-MARGIN");
    }

    public void setWkGMargin(BigDecimal value) {
        setDecimal("WK-G-MARGIN", value);
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

    public int getWkInvCnt() {
        return getInt("WK-INV-CNT");
    }

    public void setWkInvCnt(int value) {
        setInt("WK-INV-CNT", value);
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

    public BigDecimal getWkMargin() {
        return getDecimal("WK-MARGIN");
    }

    public void setWkMargin(BigDecimal value) {
        setDecimal("WK-MARGIN", value);
    }

    public int getWkOverflow() {
        return getInt("WK-OVERFLOW");
    }

    public void setWkOverflow(int value) {
        setInt("WK-OVERFLOW", value);
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

    public int getWkRepCnt() {
        return getInt("WK-REP-CNT");
    }

    public void setWkRepCnt(int value) {
        setInt("WK-REP-CNT", value);
    }

    public int getWkSi() {
        return getInt("WK-SI");
    }

    public void setWkSi(int value) {
        setInt("WK-SI", value);
    }

    public int getWkSj() {
        return getInt("WK-SJ");
    }

    public void setWkSj(int value) {
        setInt("WK-SJ", value);
    }

    public int getWkSmin() {
        return getInt("WK-SMIN");
    }

    public void setWkSmin(int value) {
        setInt("WK-SMIN", value);
    }

    public int getWkStCnt(int index) {
        return getInt("WK-ST-CNT", index);
    }

    public void setWkStCnt(int index, int value) {
        setInt("WK-ST-CNT", value, index);
    }

    public int getWkStCode(int index) {
        return getInt("WK-ST-CODE", index);
    }

    public void setWkStCode(int index, int value) {
        setInt("WK-ST-CODE", value, index);
    }

    public BigDecimal getWkStCost(int index) {
        return getDecimal("WK-ST-COST", index);
    }

    public void setWkStCost(int index, BigDecimal value) {
        setDecimal("WK-ST-COST", value, index);
    }

    public String getWkStEnt(int index) {
        return groupToString("WK-ST-ENT", index);
    }

    public void setWkStEnt(int index, String value) {
        setGroup("WK-ST-ENT", value, index);
    }

    public String getWkStEnt() {
        return groupToString("WK-ST-ENT");
    }

    public void setWkStEnt(String value) {
        setGroup("WK-ST-ENT", value);
    }

    public int getWkStMax() {
        return getInt("WK-ST-MAX");
    }

    public void setWkStMax(int value) {
        setInt("WK-ST-MAX", value);
    }

    public BigDecimal getWkStNet(int index) {
        return getDecimal("WK-ST-NET", index);
    }

    public void setWkStNet(int index, BigDecimal value) {
        setDecimal("WK-ST-NET", value, index);
    }

    public int getWkStNum() {
        return getInt("WK-ST-NUM");
    }

    public void setWkStNum(int value) {
        setInt("WK-ST-NUM", value);
    }

    public BigDecimal getWkStTax(int index) {
        return getDecimal("WK-ST-TAX", index);
    }

    public void setWkStTax(int index, BigDecimal value) {
        setDecimal("WK-ST-TAX", value, index);
    }

    public String getWkStTemp() {
        return groupToString("WK-ST-TEMP");
    }

    public void setWkStTemp(String value) {
        setGroup("WK-ST-TEMP", value);
    }

    public BigDecimal getWkStTot(int index) {
        return getDecimal("WK-ST-TOT", index);
    }

    public void setWkStTot(int index, BigDecimal value) {
        setDecimal("WK-ST-TOT", value, index);
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

    /* ── Byte-level copy wrappers (preserves COMP-3/BINARY raw bytes) ── */
    public void copyRepRecFromRcHead() {
        copyBytes("REP-REC", "RC-HEAD");
    }

    public void copyRepRecFromRdLine() {
        copyBytes("REP-REC", "RD-LINE");
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

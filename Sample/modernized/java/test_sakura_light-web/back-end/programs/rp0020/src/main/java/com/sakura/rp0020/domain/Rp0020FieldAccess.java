package com.sakura.rp0020.domain;

import com.sakura.rp0020.runtime.Rp0020Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for RP0020. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Rp0020FieldAccess extends RuntimeFieldAccess {

    public Rp0020FieldAccess(WorkingStorage ws, Rp0020Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getProdf() != null) {
            register(fileSet.getProdf().buffer());
        }
        if (fileSet != null && fileSet.getCatgf() != null) {
            register(fileSet.getCatgf().buffer());
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

    public int getCtCode() {
        return getInt("CT-CODE");
    }

    public void setCtCode(int value) {
        setInt("CT-CODE", value);
    }

    public String getCtName() {
        return getString("CT-NAME");
    }

    public void setCtName(String value) {
        setString("CT-NAME", value);
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

    public int getPrCategory() {
        return getInt("PR-CATEGORY");
    }

    public void setPrCategory(int value) {
        setInt("PR-CATEGORY", value);
    }

    public int getPrCode() {
        return getInt("PR-CODE");
    }

    public void setPrCode(int value) {
        setInt("PR-CODE", value);
    }

    public int getPrDelFlag() {
        return getInt("PR-DEL-FLAG");
    }

    public void setPrDelFlag(int value) {
        setInt("PR-DEL-FLAG", value);
    }

    public BigDecimal getPrListPrice() {
        return getDecimal("PR-LIST-PRICE");
    }

    public void setPrListPrice(BigDecimal value) {
        setDecimal("PR-LIST-PRICE", value);
    }

    public String getPrName() {
        return getString("PR-NAME");
    }

    public void setPrName(String value) {
        setString("PR-NAME", value);
    }

    public BigDecimal getPrStdCost() {
        return getDecimal("PR-STD-COST");
    }

    public void setPrStdCost(BigDecimal value) {
        setDecimal("PR-STD-COST", value);
    }

    public String getPrUnit() {
        return getString("PR-UNIT");
    }

    public void setPrUnit(String value) {
        setString("PR-UNIT", value);
    }

    public String getRdProd() {
        return groupToString("RD-PROD");
    }

    public void setRdProd(String value) {
        setGroup("RD-PROD", value);
    }

    public String getRepRec() {
        return getString("REP-REC");
    }

    public void setRepRec(String value) {
        setString("REP-REC", value);
    }

    public String getRhProd() {
        return groupToString("RH-PROD");
    }

    public void setRhProd(String value) {
        setGroup("RH-PROD", value);
    }

    public String getRpCategory() {
        return getString("RP-CATEGORY");
    }

    public void setRpCategory(String value) {
        setString("RP-CATEGORY", value);
    }

    public int getRpCode() {
        return getInt("RP-CODE");
    }

    public void setRpCode(int value) {
        setInt("RP-CODE", value);
    }

    public BigDecimal getRpCost() {
        return getDecimal("RP-COST");
    }

    public void setRpCost(BigDecimal value) {
        setDecimal("RP-COST", value);
    }

    public String getRpName() {
        return getString("RP-NAME");
    }

    public void setRpName(String value) {
        setString("RP-NAME", value);
    }

    public BigDecimal getRpPrice() {
        return getDecimal("RP-PRICE");
    }

    public void setRpPrice(BigDecimal value) {
        setDecimal("RP-PRICE", value);
    }

    public String getRpUnit() {
        return getString("RP-UNIT");
    }

    public void setRpUnit(String value) {
        setString("RP-UNIT", value);
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

    public String getRtProd() {
        return groupToString("RT-PROD");
    }

    public void setRtProd(String value) {
        setGroup("RT-PROD", value);
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

    public String getWkCatgName() {
        return getString("WK-CATG-NAME");
    }

    public void setWkCatgName(String value) {
        setString("WK-CATG-NAME", value);
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

    public int getWkProdFrom() {
        return getInt("WK-PROD-FROM");
    }

    public void setWkProdFrom(int value) {
        setInt("WK-PROD-FROM", value);
    }

    public int getWkProdTo() {
        return getInt("WK-PROD-TO");
    }

    public void setWkProdTo(int value) {
        setInt("WK-PROD-TO", value);
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
    public void copyRepRecFromRdProd() {
        copyBytes("REP-REC", "RD-PROD");
    }

    public void copyRepRecFromRhProd() {
        copyBytes("REP-REC", "RH-PROD");
    }

    public void copyRepRecFromRptH1() {
        copyBytes("REP-REC", "RPT-H1");
    }

    public void copyRepRecFromRptH2() {
        copyBytes("REP-REC", "RPT-H2");
    }

    public void copyRepRecFromRtProd() {
        copyBytes("REP-REC", "RT-PROD");
    }
}

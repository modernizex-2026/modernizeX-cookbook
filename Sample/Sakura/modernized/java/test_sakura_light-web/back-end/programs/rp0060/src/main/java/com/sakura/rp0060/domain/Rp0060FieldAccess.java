package com.sakura.rp0060.domain;

import com.sakura.rp0060.runtime.Rp0060Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for RP0060. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Rp0060FieldAccess extends RuntimeFieldAccess {

    public Rp0060FieldAccess(WorkingStorage ws, Rp0060Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getStokf() != null) {
            register(fileSet.getStokf().buffer());
        }
        if (fileSet != null && fileSet.getProdf() != null) {
            register(fileSet.getProdf().buffer());
        }
        if (fileSet != null && fileSet.getWhsef() != null) {
            register(fileSet.getWhsef().buffer());
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

    public long getGtQty() {
        return getLong("GT-QTY");
    }

    public void setGtQty(long value) {
        setLong("GT-QTY", value);
    }

    public BigDecimal getGtVal() {
        return getDecimal("GT-VAL");
    }

    public void setGtVal(BigDecimal value) {
        setDecimal("GT-VAL", value);
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

    public int getPrCode() {
        return getInt("PR-CODE");
    }

    public void setPrCode(int value) {
        setInt("PR-CODE", value);
    }

    public String getPrName() {
        return getString("PR-NAME");
    }

    public void setPrName(String value) {
        setString("PR-NAME", value);
    }

    public String getRdGrand() {
        return groupToString("RD-GRAND");
    }

    public void setRdGrand(String value) {
        setGroup("RD-GRAND", value);
    }

    public String getRdLine() {
        return groupToString("RD-LINE");
    }

    public void setRdLine(String value) {
        setGroup("RD-LINE", value);
    }

    public String getRdSub() {
        return groupToString("RD-SUB");
    }

    public void setRdSub(String value) {
        setGroup("RD-SUB", value);
    }

    public String getRdWhd() {
        return groupToString("RD-WHD");
    }

    public void setRdWhd(String value) {
        setGroup("RD-WHD", value);
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

    public BigDecimal getRsAvgcost() {
        return getDecimal("RS-AVGCOST");
    }

    public void setRsAvgcost(BigDecimal value) {
        setDecimal("RS-AVGCOST", value);
    }

    public String getRsName() {
        return getString("RS-NAME");
    }

    public void setRsName(String value) {
        setString("RS-NAME", value);
    }

    public long getRsOnhand() {
        return getLong("RS-ONHAND");
    }

    public void setRsOnhand(long value) {
        setLong("RS-ONHAND", value);
    }

    public int getRsProd() {
        return getInt("RS-PROD");
    }

    public void setRsProd(int value) {
        setInt("RS-PROD", value);
    }

    public BigDecimal getRsValue() {
        return getDecimal("RS-VALUE");
    }

    public void setRsValue(BigDecimal value) {
        setDecimal("RS-VALUE", value);
    }

    public BigDecimal getSkAvgCost() {
        return getDecimal("SK-AVG-COST");
    }

    public void setSkAvgCost(BigDecimal value) {
        setDecimal("SK-AVG-COST", value);
    }

    public BigDecimal getSkOnhand() {
        return getDecimal("SK-ONHAND");
    }

    public void setSkOnhand(BigDecimal value) {
        setDecimal("SK-ONHAND", value);
    }

    public int getSkProd() {
        return getInt("SK-PROD");
    }

    public void setSkProd(int value) {
        setInt("SK-PROD", value);
    }

    public int getSkWhse() {
        return getInt("SK-WHSE");
    }

    public void setSkWhse(int value) {
        setInt("SK-WHSE", value);
    }

    public long getSubQty() {
        return getLong("SUB-QTY");
    }

    public void setSubQty(long value) {
        setLong("SUB-QTY", value);
    }

    public BigDecimal getSubVal() {
        return getDecimal("SUB-VAL");
    }

    public void setSubVal(BigDecimal value) {
        setDecimal("SUB-VAL", value);
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

    public int getWhCode() {
        return getInt("WH-CODE");
    }

    public void setWhCode(int value) {
        setInt("WH-CODE", value);
    }

    public String getWhName() {
        return getString("WH-NAME");
    }

    public void setWhName(String value) {
        setString("WH-NAME", value);
    }

    public int getWhdCode() {
        return getInt("WHD-CODE");
    }

    public void setWhdCode(int value) {
        setInt("WHD-CODE", value);
    }

    public String getWhdName() {
        return getString("WHD-NAME");
    }

    public void setWhdName(String value) {
        setString("WHD-NAME", value);
    }

    public String getWkCompany() {
        return getString("WK-COMPANY");
    }

    public void setWkCompany(String value) {
        setString("WK-COMPANY", value);
    }

    public int getWkCurWhse() {
        return getInt("WK-CUR-WHSE");
    }

    public void setWkCurWhse(int value) {
        setInt("WK-CUR-WHSE", value);
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

    public BigDecimal getWkGQty() {
        return getDecimal("WK-G-QTY");
    }

    public void setWkGQty(BigDecimal value) {
        setDecimal("WK-G-QTY", value);
    }

    public BigDecimal getWkGVal() {
        return getDecimal("WK-G-VAL");
    }

    public void setWkGVal(BigDecimal value) {
        setDecimal("WK-G-VAL", value);
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

    public String getWkProdName() {
        return getString("WK-PROD-NAME");
    }

    public void setWkProdName(String value) {
        setString("WK-PROD-NAME", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
    }

    public BigDecimal getWkSubQty() {
        return getDecimal("WK-SUB-QTY");
    }

    public void setWkSubQty(BigDecimal value) {
        setDecimal("WK-SUB-QTY", value);
    }

    public BigDecimal getWkSubVal() {
        return getDecimal("WK-SUB-VAL");
    }

    public void setWkSubVal(BigDecimal value) {
        setDecimal("WK-SUB-VAL", value);
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

    public BigDecimal getWkVal() {
        return getDecimal("WK-VAL");
    }

    public void setWkVal(BigDecimal value) {
        setDecimal("WK-VAL", value);
    }

    public String getWkWhName() {
        return getString("WK-WH-NAME");
    }

    public void setWkWhName(String value) {
        setString("WK-WH-NAME", value);
    }

    /* ── Byte-level copy wrappers (preserves COMP-3/BINARY raw bytes) ── */
    public void copyRepRecFromRdGrand() {
        copyBytes("REP-REC", "RD-GRAND");
    }

    public void copyRepRecFromRdLine() {
        copyBytes("REP-REC", "RD-LINE");
    }

    public void copyRepRecFromRdSub() {
        copyBytes("REP-REC", "RD-SUB");
    }

    public void copyRepRecFromRdWhd() {
        copyBytes("REP-REC", "RD-WHD");
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
}

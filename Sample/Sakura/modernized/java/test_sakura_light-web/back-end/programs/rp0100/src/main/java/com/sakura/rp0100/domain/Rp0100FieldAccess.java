package com.sakura.rp0100.domain;

import com.sakura.rp0100.runtime.Rp0100Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for RP0100. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Rp0100FieldAccess extends RuntimeFieldAccess {

    public Rp0100FieldAccess(WorkingStorage ws, Rp0100Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getPurhf() != null) {
            register(fileSet.getPurhf().buffer());
        }
        if (fileSet != null && fileSet.getPurdf() != null) {
            register(fileSet.getPurdf().buffer());
        }
        if (fileSet != null && fileSet.getSuppf() != null) {
            register(fileSet.getSuppf().buffer());
        }
        if (fileSet != null && fileSet.getProdf() != null) {
            register(fileSet.getProdf().buffer());
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

    public long getGtNet() {
        return getLong("GT-NET");
    }

    public void setGtNet(long value) {
        setLong("GT-NET", value);
    }

    public long getGtTax() {
        return getLong("GT-TAX");
    }

    public void setGtTax(long value) {
        setLong("GT-TAX", value);
    }

    public long getGtTot() {
        return getLong("GT-TOT");
    }

    public void setGtTot(long value) {
        setLong("GT-TOT", value);
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

    public String getRdPurd() {
        return groupToString("RD-PURD");
    }

    public void setRdPurd(String value) {
        setGroup("RD-PURD", value);
    }

    public String getRdPurh() {
        return groupToString("RD-PURH");
    }

    public void setRdPurh(String value) {
        setGroup("RD-PURH", value);
    }

    public String getRdSub() {
        return groupToString("RD-SUB");
    }

    public void setRdSub(String value) {
        setGroup("RD-SUB", value);
    }

    public String getRepRec() {
        return getString("REP-REC");
    }

    public void setRepRec(String value) {
        setString("REP-REC", value);
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

    public long getSubNet() {
        return getLong("SUB-NET");
    }

    public void setSubNet(long value) {
        setLong("SUB-NET", value);
    }

    public long getSubTax() {
        return getLong("SUB-TAX");
    }

    public void setSubTax(long value) {
        setLong("SUB-TAX", value);
    }

    public long getSubTot() {
        return getLong("SUB-TOT");
    }

    public void setSubTot(long value) {
        setLong("SUB-TOT", value);
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

    public BigDecimal getVdAmount() {
        return getDecimal("VD-AMOUNT");
    }

    public void setVdAmount(BigDecimal value) {
        setDecimal("VD-AMOUNT", value);
    }

    public long getVdAmtE() {
        return getLong("VD-AMT-E");
    }

    public void setVdAmtE(long value) {
        setLong("VD-AMT-E", value);
    }

    public BigDecimal getVdCostE() {
        return getDecimal("VD-COST-E");
    }

    public void setVdCostE(BigDecimal value) {
        setDecimal("VD-COST-E", value);
    }

    public int getVdLine() {
        return getInt("VD-LINE");
    }

    public void setVdLine(int value) {
        setInt("VD-LINE", value);
    }

    public int getVdLineE() {
        return getInt("VD-LINE-E");
    }

    public void setVdLineE(int value) {
        setInt("VD-LINE-E", value);
    }

    public long getVdNo() {
        return getLong("VD-NO");
    }

    public void setVdNo(long value) {
        setLong("VD-NO", value);
    }

    public String getVdPname() {
        return getString("VD-PNAME");
    }

    public void setVdPname(String value) {
        setString("VD-PNAME", value);
    }

    public int getVdProd() {
        return getInt("VD-PROD");
    }

    public void setVdProd(int value) {
        setInt("VD-PROD", value);
    }

    public int getVdProdE() {
        return getInt("VD-PROD-E");
    }

    public void setVdProdE(int value) {
        setInt("VD-PROD-E", value);
    }

    public BigDecimal getVdQty() {
        return getDecimal("VD-QTY");
    }

    public void setVdQty(BigDecimal value) {
        setDecimal("VD-QTY", value);
    }

    public long getVdQtyE() {
        return getLong("VD-QTY-E");
    }

    public void setVdQtyE(long value) {
        setLong("VD-QTY-E", value);
    }

    public BigDecimal getVdUnitCost() {
        return getDecimal("VD-UNIT-COST");
    }

    public void setVdUnitCost(BigDecimal value) {
        setDecimal("VD-UNIT-COST", value);
    }

    public BigDecimal getVhAmount() {
        return getDecimal("VH-AMOUNT");
    }

    public void setVhAmount(BigDecimal value) {
        setDecimal("VH-AMOUNT", value);
    }

    public int getVhDate() {
        return getInt("VH-DATE");
    }

    public void setVhDate(int value) {
        setInt("VH-DATE", value);
    }

    public int getVhDateE() {
        return getInt("VH-DATE-E");
    }

    public void setVhDateE(int value) {
        setInt("VH-DATE-E", value);
    }

    public int getVhDelFlag() {
        return getInt("VH-DEL-FLAG");
    }

    public void setVhDelFlag(int value) {
        setInt("VH-DEL-FLAG", value);
    }

    public int getVhKind() {
        return getInt("VH-KIND");
    }

    public void setVhKind(int value) {
        setInt("VH-KIND", value);
    }

    public String getVhKindE() {
        return getString("VH-KIND-E");
    }

    public void setVhKindE(String value) {
        setString("VH-KIND-E", value);
    }

    public long getVhNo() {
        return getLong("VH-NO");
    }

    public void setVhNo(long value) {
        setLong("VH-NO", value);
    }

    public long getVhNoE() {
        return getLong("VH-NO-E");
    }

    public void setVhNoE(long value) {
        setLong("VH-NO-E", value);
    }

    public String getVhSname() {
        return getString("VH-SNAME");
    }

    public void setVhSname(String value) {
        setString("VH-SNAME", value);
    }

    public int getVhStatus() {
        return getInt("VH-STATUS");
    }

    public void setVhStatus(int value) {
        setInt("VH-STATUS", value);
    }

    public int getVhSupp() {
        return getInt("VH-SUPP");
    }

    public void setVhSupp(int value) {
        setInt("VH-SUPP", value);
    }

    public int getVhSuppE() {
        return getInt("VH-SUPP-E");
    }

    public void setVhSuppE(int value) {
        setInt("VH-SUPP-E", value);
    }

    public BigDecimal getVhTaxAmount() {
        return getDecimal("VH-TAX-AMOUNT");
    }

    public void setVhTaxAmount(BigDecimal value) {
        setDecimal("VH-TAX-AMOUNT", value);
    }

    public BigDecimal getVhTotal() {
        return getDecimal("VH-TOTAL");
    }

    public void setVhTotal(BigDecimal value) {
        setDecimal("VH-TOTAL", value);
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

    public int getWkDtlEof() {
        return getInt("WK-DTL-EOF");
    }

    public void setWkDtlEof(int value) {
        setInt("WK-DTL-EOF", value);
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

    public String getWkKindTxt() {
        return getString("WK-KIND-TXT");
    }

    public void setWkKindTxt(String value) {
        setString("WK-KIND-TXT", value);
    }

    public int getWkLinCnt() {
        return getInt("WK-LIN-CNT");
    }

    public void setWkLinCnt(int value) {
        setInt("WK-LIN-CNT", value);
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

    public int getWkPurCnt() {
        return getInt("WK-PUR-CNT");
    }

    public void setWkPurCnt(int value) {
        setInt("WK-PUR-CNT", value);
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
    public void copyRepRecFromRdGrand() {
        copyBytes("REP-REC", "RD-GRAND");
    }

    public void copyRepRecFromRdPurd() {
        copyBytes("REP-REC", "RD-PURD");
    }

    public void copyRepRecFromRdPurh() {
        copyBytes("REP-REC", "RD-PURH");
    }

    public void copyRepRecFromRdSub() {
        copyBytes("REP-REC", "RD-SUB");
    }

    public void copyRepRecFromRptH1() {
        copyBytes("REP-REC", "RPT-H1");
    }

    public void copyRepRecFromRptH2() {
        copyBytes("REP-REC", "RPT-H2");
    }
}

package com.sakura.rp0110.domain;

import com.sakura.rp0110.runtime.Rp0110Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for RP0110. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Rp0110FieldAccess extends RuntimeFieldAccess {

    public Rp0110FieldAccess(WorkingStorage ws, Rp0110Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getProdf() != null) {
            register(fileSet.getProdf().buffer());
        }
        if (fileSet != null && fileSet.getStokf() != null) {
            register(fileSet.getStokf().buffer());
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

    public int getPrDfltSupp() {
        return getInt("PR-DFLT-SUPP");
    }

    public void setPrDfltSupp(int value) {
        setInt("PR-DFLT-SUPP", value);
    }

    public int getPrLeadDays() {
        return getInt("PR-LEAD-DAYS");
    }

    public void setPrLeadDays(int value) {
        setInt("PR-LEAD-DAYS", value);
    }

    public String getPrName() {
        return getString("PR-NAME");
    }

    public void setPrName(String value) {
        setString("PR-NAME", value);
    }

    public BigDecimal getPrReorderPoint() {
        return getDecimal("PR-REORDER-POINT");
    }

    public void setPrReorderPoint(BigDecimal value) {
        setDecimal("PR-REORDER-POINT", value);
    }

    public BigDecimal getPrReorderQty() {
        return getDecimal("PR-REORDER-QTY");
    }

    public void setPrReorderQty(BigDecimal value) {
        setDecimal("PR-REORDER-QTY", value);
    }

    public BigDecimal getPrStdCost() {
        return getDecimal("PR-STD-COST");
    }

    public void setPrStdCost(BigDecimal value) {
        setDecimal("PR-STD-COST", value);
    }

    public int getPrStockMng() {
        return getInt("PR-STOCK-MNG");
    }

    public void setPrStockMng(int value) {
        setInt("PR-STOCK-MNG", value);
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

    public int getRlLead() {
        return getInt("RL-LEAD");
    }

    public void setRlLead(int value) {
        setInt("RL-LEAD", value);
    }

    public String getRlName() {
        return getString("RL-NAME");
    }

    public void setRlName(String value) {
        setString("RL-NAME", value);
    }

    public long getRlOnhand() {
        return getLong("RL-ONHAND");
    }

    public void setRlOnhand(long value) {
        setLong("RL-ONHAND", value);
    }

    public long getRlOnord() {
        return getLong("RL-ONORD");
    }

    public void setRlOnord(long value) {
        setLong("RL-ONORD", value);
    }

    public int getRlProd() {
        return getInt("RL-PROD");
    }

    public void setRlProd(int value) {
        setInt("RL-PROD", value);
    }

    public long getRlRpoint() {
        return getLong("RL-RPOINT");
    }

    public void setRlRpoint(long value) {
        setLong("RL-RPOINT", value);
    }

    public long getRlSugqty() {
        return getLong("RL-SUGQTY");
    }

    public void setRlSugqty(long value) {
        setLong("RL-SUGQTY", value);
    }

    public String getRlSupp() {
        return getString("RL-SUPP");
    }

    public void setRlSupp(String value) {
        setString("RL-SUPP", value);
    }

    public long getRlValue() {
        return getLong("RL-VALUE");
    }

    public void setRlValue(long value) {
        setLong("RL-VALUE", value);
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

    public int getRtItems() {
        return getInt("RT-ITEMS");
    }

    public void setRtItems(int value) {
        setInt("RT-ITEMS", value);
    }

    public String getRtLine() {
        return groupToString("RT-LINE");
    }

    public void setRtLine(String value) {
        setGroup("RT-LINE", value);
    }

    public long getRtQty() {
        return getLong("RT-QTY");
    }

    public void setRtQty(long value) {
        setLong("RT-QTY", value);
    }

    public long getRtValue() {
        return getLong("RT-VALUE");
    }

    public void setRtValue(long value) {
        setLong("RT-VALUE", value);
    }

    public BigDecimal getSkOnOrder() {
        return getDecimal("SK-ON-ORDER");
    }

    public void setSkOnOrder(BigDecimal value) {
        setDecimal("SK-ON-ORDER", value);
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

    public int getWkGItems() {
        return getInt("WK-G-ITEMS");
    }

    public void setWkGItems(int value) {
        setInt("WK-G-ITEMS", value);
    }

    public BigDecimal getWkGQty() {
        return getDecimal("WK-G-QTY");
    }

    public void setWkGQty(BigDecimal value) {
        setDecimal("WK-G-QTY", value);
    }

    public BigDecimal getWkGValue() {
        return getDecimal("WK-G-VALUE");
    }

    public void setWkGValue(BigDecimal value) {
        setDecimal("WK-G-VALUE", value);
    }

    public String getWkInSupp() {
        return getString("WK-IN-SUPP");
    }

    public void setWkInSupp(String value) {
        setString("WK-IN-SUPP", value);
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

    public long getWkOnhandSum() {
        return getLong("WK-ONHAND-SUM");
    }

    public void setWkOnhandSum(long value) {
        setLong("WK-ONHAND-SUM", value);
    }

    public long getWkOnorderSum() {
        return getLong("WK-ONORDER-SUM");
    }

    public void setWkOnorderSum(long value) {
        setLong("WK-ONORDER-SUM", value);
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

    public long getWkProjected() {
        return getLong("WK-PROJECTED");
    }

    public void setWkProjected(long value) {
        setLong("WK-PROJECTED", value);
    }

    public int getWkScanCnt() {
        return getInt("WK-SCAN-CNT");
    }

    public void setWkScanCnt(int value) {
        setInt("WK-SCAN-CNT", value);
    }

    public int getWkStkEof() {
        return getInt("WK-STK-EOF");
    }

    public void setWkStkEof(int value) {
        setInt("WK-STK-EOF", value);
    }

    public int getWkSugCnt() {
        return getInt("WK-SUG-CNT");
    }

    public void setWkSugCnt(int value) {
        setInt("WK-SUG-CNT", value);
    }

    public BigDecimal getWkSugValue() {
        return getDecimal("WK-SUG-VALUE");
    }

    public void setWkSugValue(BigDecimal value) {
        setDecimal("WK-SUG-VALUE", value);
    }

    public int getWkSuppFilter() {
        return getInt("WK-SUPP-FILTER");
    }

    public void setWkSuppFilter(int value) {
        setInt("WK-SUPP-FILTER", value);
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

package com.sakura.rp0140.domain;

import com.sakura.rp0140.runtime.Rp0140Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for RP0140. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Rp0140FieldAccess extends RuntimeFieldAccess {

    public Rp0140FieldAccess(WorkingStorage ws, Rp0140Datasets fileSet) {
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

    public int getGtIdle() {
        return getInt("GT-IDLE");
    }

    public void setGtIdle(int value) {
        setInt("GT-IDLE", value);
    }

    public long getGtIdleval() {
        return getLong("GT-IDLEVAL");
    }

    public void setGtIdleval(long value) {
        setLong("GT-IDLEVAL", value);
    }

    public int getGtItems() {
        return getInt("GT-ITEMS");
    }

    public void setGtItems(int value) {
        setInt("GT-ITEMS", value);
    }

    public long getGtValue() {
        return getLong("GT-VALUE");
    }

    public void setGtValue(long value) {
        setLong("GT-VALUE", value);
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

    public String getRcHead() {
        return groupToString("RC-HEAD");
    }

    public void setRcHead(String value) {
        setGroup("RC-HEAD", value);
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

    public String getRdWhse() {
        return groupToString("RD-WHSE");
    }

    public void setRdWhse(String value) {
        setGroup("RD-WHSE", value);
    }

    public String getRepRec() {
        return getString("REP-REC");
    }

    public void setRepRec(String value) {
        setString("REP-REC", value);
    }

    public BigDecimal getRlAvgcost() {
        return getDecimal("RL-AVGCOST");
    }

    public void setRlAvgcost(BigDecimal value) {
        setDecimal("RL-AVGCOST", value);
    }

    public int getRlDays() {
        return getInt("RL-DAYS");
    }

    public void setRlDays(int value) {
        setInt("RL-DAYS", value);
    }

    public String getRlFlag() {
        return getString("RL-FLAG");
    }

    public void setRlFlag(String value) {
        setString("RL-FLAG", value);
    }

    public int getRlLastout() {
        return getInt("RL-LASTOUT");
    }

    public void setRlLastout(int value) {
        setInt("RL-LASTOUT", value);
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

    public int getRlProd() {
        return getInt("RL-PROD");
    }

    public void setRlProd(int value) {
        setInt("RL-PROD", value);
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

    public BigDecimal getSkAvgCost() {
        return getDecimal("SK-AVG-COST");
    }

    public void setSkAvgCost(BigDecimal value) {
        setDecimal("SK-AVG-COST", value);
    }

    public int getSkLastOutDate() {
        return getInt("SK-LAST-OUT-DATE");
    }

    public void setSkLastOutDate(int value) {
        setInt("SK-LAST-OUT-DATE", value);
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

    public int getStIdle() {
        return getInt("ST-IDLE");
    }

    public void setStIdle(int value) {
        setInt("ST-IDLE", value);
    }

    public long getStIdleval() {
        return getLong("ST-IDLEVAL");
    }

    public void setStIdleval(long value) {
        setLong("ST-IDLEVAL", value);
    }

    public int getStItems() {
        return getInt("ST-ITEMS");
    }

    public void setStItems(int value) {
        setInt("ST-ITEMS", value);
    }

    public long getStValue() {
        return getLong("ST-VALUE");
    }

    public void setStValue(long value) {
        setLong("ST-VALUE", value);
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

    public int getWhhCode() {
        return getInt("WHH-CODE");
    }

    public void setWhhCode(int value) {
        setInt("WHH-CODE", value);
    }

    public String getWhhName() {
        return getString("WHH-NAME");
    }

    public void setWhhName(String value) {
        setString("WHH-NAME", value);
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

    public int getWkDays() {
        return getInt("WK-DAYS");
    }

    public void setWkDays(int value) {
        setInt("WK-DAYS", value);
    }

    public int getWkFirst() {
        return getInt("WK-FIRST");
    }

    public void setWkFirst(int value) {
        setInt("WK-FIRST", value);
    }

    public String getWkFlagTxt() {
        return getString("WK-FLAG-TXT");
    }

    public void setWkFlagTxt(String value) {
        setString("WK-FLAG-TXT", value);
    }

    public int getWkGIdleCnt() {
        return getInt("WK-G-IDLE-CNT");
    }

    public void setWkGIdleCnt(int value) {
        setInt("WK-G-IDLE-CNT", value);
    }

    public BigDecimal getWkGIdleValue() {
        return getDecimal("WK-G-IDLE-VALUE");
    }

    public void setWkGIdleValue(BigDecimal value) {
        setDecimal("WK-G-IDLE-VALUE", value);
    }

    public int getWkGItems() {
        return getInt("WK-G-ITEMS");
    }

    public void setWkGItems(int value) {
        setInt("WK-G-ITEMS", value);
    }

    public BigDecimal getWkGValue() {
        return getDecimal("WK-G-VALUE");
    }

    public void setWkGValue(BigDecimal value) {
        setDecimal("WK-G-VALUE", value);
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

    public BigDecimal getWkValue() {
        return getDecimal("WK-VALUE");
    }

    public void setWkValue(BigDecimal value) {
        setDecimal("WK-VALUE", value);
    }

    public int getWkWhIdleCnt() {
        return getInt("WK-WH-IDLE-CNT");
    }

    public void setWkWhIdleCnt(int value) {
        setInt("WK-WH-IDLE-CNT", value);
    }

    public BigDecimal getWkWhIdleValue() {
        return getDecimal("WK-WH-IDLE-VALUE");
    }

    public void setWkWhIdleValue(BigDecimal value) {
        setDecimal("WK-WH-IDLE-VALUE", value);
    }

    public int getWkWhItems() {
        return getInt("WK-WH-ITEMS");
    }

    public void setWkWhItems(int value) {
        setInt("WK-WH-ITEMS", value);
    }

    public String getWkWhName() {
        return getString("WK-WH-NAME");
    }

    public void setWkWhName(String value) {
        setString("WK-WH-NAME", value);
    }

    public BigDecimal getWkWhValue() {
        return getDecimal("WK-WH-VALUE");
    }

    public void setWkWhValue(BigDecimal value) {
        setDecimal("WK-WH-VALUE", value);
    }

    /* ── Byte-level copy wrappers (preserves COMP-3/BINARY raw bytes) ── */
    public void copyRepRecFromRcHead() {
        copyBytes("REP-REC", "RC-HEAD");
    }

    public void copyRepRecFromRdGrand() {
        copyBytes("REP-REC", "RD-GRAND");
    }

    public void copyRepRecFromRdLine() {
        copyBytes("REP-REC", "RD-LINE");
    }

    public void copyRepRecFromRdSub() {
        copyBytes("REP-REC", "RD-SUB");
    }

    public void copyRepRecFromRdWhse() {
        copyBytes("REP-REC", "RD-WHSE");
    }

    public void copyRepRecFromRptH1() {
        copyBytes("REP-REC", "RPT-H1");
    }

    public void copyRepRecFromRptH2() {
        copyBytes("REP-REC", "RPT-H2");
    }
}

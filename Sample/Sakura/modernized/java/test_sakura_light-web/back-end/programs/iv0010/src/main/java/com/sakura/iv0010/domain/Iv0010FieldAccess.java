package com.sakura.iv0010.domain;

import com.sakura.iv0010.runtime.Iv0010Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for IV0010. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Iv0010FieldAccess extends RuntimeFieldAccess {

    public Iv0010FieldAccess(WorkingStorage ws, Iv0010Datasets fileSet) {
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
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getBrwFlg() {
        return getInt("BRW-FLG");
    }

    public void setBrwFlg(int value) {
        setInt("BRW-FLG", value);
    }

    public int getCompletionCode() {
        return getInt("COMPLETION-CODE");
    }

    public void setCompletionCode(int value) {
        setInt("COMPLETION-CODE", value);
    }

    public int getEndFlg() {
        return getInt("END-FLG");
    }

    public void setEndFlg(int value) {
        setInt("END-FLG", value);
    }

    public String getEsts() {
        return getString("ESTS");
    }

    public void setEsts(String value) {
        setString("ESTS", value);
    }

    public String getFsts() {
        return getString("FSTS");
    }

    public void setFsts(String value) {
        setString("FSTS", value);
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

    public BigDecimal getPrReorderPoint() {
        return getDecimal("PR-REORDER-POINT");
    }

    public void setPrReorderPoint(BigDecimal value) {
        setDecimal("PR-REORDER-POINT", value);
    }

    public BigDecimal getPrSafetyStock() {
        return getDecimal("PR-SAFETY-STOCK");
    }

    public void setPrSafetyStock(BigDecimal value) {
        setDecimal("PR-SAFETY-STOCK", value);
    }

    public int getRecFlg() {
        return getInt("REC-FLG");
    }

    public void setRecFlg(int value) {
        setInt("REC-FLG", value);
    }

    public BigDecimal getSkAllocated() {
        return getDecimal("SK-ALLOCATED");
    }

    public void setSkAllocated(BigDecimal value) {
        setDecimal("SK-ALLOCATED", value);
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

    public int getWkAvail() {
        return getInt("WK-AVAIL");
    }

    public void setWkAvail(int value) {
        setInt("WK-AVAIL", value);
    }

    public String getWkFkeyLine() {
        return getString("WK-FKEY-LINE");
    }

    public void setWkFkeyLine(String value) {
        setString("WK-FKEY-LINE", value);
    }

    public int getWkKeyProd() {
        return getInt("WK-KEY-PROD");
    }

    public void setWkKeyProd(int value) {
        setInt("WK-KEY-PROD", value);
    }

    public int getWkKeyWhse() {
        return getInt("WK-KEY-WHSE");
    }

    public void setWkKeyWhse(int value) {
        setInt("WK-KEY-WHSE", value);
    }

    public String getWkMsgLine() {
        return getString("WK-MSG-LINE");
    }

    public void setWkMsgLine(String value) {
        setString("WK-MSG-LINE", value);
    }

    public int getWkOrder() {
        return getInt("WK-ORDER");
    }

    public void setWkOrder(int value) {
        setInt("WK-ORDER", value);
    }

    public String getWkPrName() {
        return getString("WK-PR-NAME");
    }

    public void setWkPrName(String value) {
        setString("WK-PR-NAME", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
    }

    public int getWkReorder() {
        return getInt("WK-REORDER");
    }

    public void setWkReorder(int value) {
        setInt("WK-REORDER", value);
    }

    public int getWkSafety() {
        return getInt("WK-SAFETY");
    }

    public void setWkSafety(int value) {
        setInt("WK-SAFETY", value);
    }

    public int getWkSeen() {
        return getInt("WK-SEEN");
    }

    public void setWkSeen(int value) {
        setInt("WK-SEEN", value);
    }

    public String getWkStat() {
        return getString("WK-STAT");
    }

    public void setWkStat(String value) {
        setString("WK-STAT", value);
    }

    public int getWkSysdate() {
        return getInt("WK-SYSDATE");
    }

    public void setWkSysdate(int value) {
        setInt("WK-SYSDATE", value);
    }

    public int getWkSysymd() {
        return getInt("WK-SYSYMD");
    }

    public void setWkSysymd(int value) {
        setInt("WK-SYSYMD", value);
    }

    public String getWkTitle() {
        return getString("WK-TITLE");
    }

    public void setWkTitle(String value) {
        setString("WK-TITLE", value);
    }

    public String getWkWhName() {
        return getString("WK-WH-NAME");
    }

    public void setWkWhName(String value) {
        setString("WK-WH-NAME", value);
    }
}

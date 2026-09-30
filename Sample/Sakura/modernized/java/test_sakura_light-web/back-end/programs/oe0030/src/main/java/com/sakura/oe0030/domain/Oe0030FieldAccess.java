package com.sakura.oe0030.domain;

import com.sakura.oe0030.runtime.Oe0030Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for OE0030. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Oe0030FieldAccess extends RuntimeFieldAccess {

    public Oe0030FieldAccess(WorkingStorage ws, Oe0030Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getOrdhf() != null) {
            register(fileSet.getOrdhf().buffer());
        }
        if (fileSet != null && fileSet.getOrddf() != null) {
            register(fileSet.getOrddf().buffer());
        }
        if (fileSet != null && fileSet.getStokf() != null) {
            register(fileSet.getStokf().buffer());
        }
        if (fileSet != null && fileSet.getCustf() != null) {
            register(fileSet.getCustf().buffer());
        }
        if (fileSet != null && fileSet.getProdf() != null) {
            register(fileSet.getProdf().buffer());
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

    public int getDrAlloc(int index) {
        return getInt("DR-ALLOC", index);
    }

    public void setDrAlloc(int index, int value) {
        setInt("DR-ALLOC", value, index);
    }

    public int getDrAvail(int index) {
        return getInt("DR-AVAIL", index);
    }

    public void setDrAvail(int index, int value) {
        setInt("DR-AVAIL", value, index);
    }

    public int getDrLine(int index) {
        return getInt("DR-LINE", index);
    }

    public void setDrLine(int index, int value) {
        setInt("DR-LINE", value, index);
    }

    public String getDrName(int index) {
        return getString("DR-NAME", index);
    }

    public void setDrName(int index, String value) {
        setString("DR-NAME", value, index);
    }

    public int getDrOrd(int index) {
        return getInt("DR-ORD", index);
    }

    public void setDrOrd(int index, int value) {
        setInt("DR-ORD", value, index);
    }

    public int getDrProd(int index) {
        return getInt("DR-PROD", index);
    }

    public void setDrProd(int index, int value) {
        setInt("DR-PROD", value, index);
    }

    public int getDrShort(int index) {
        return getInt("DR-SHORT", index);
    }

    public void setDrShort(int index, int value) {
        setInt("DR-SHORT", value, index);
    }

    public int getDrStkmng(int index) {
        return getInt("DR-STKMNG", index);
    }

    public void setDrStkmng(int index, int value) {
        setInt("DR-STKMNG", value, index);
    }

    public int getDrWhse(int index) {
        return getInt("DR-WHSE", index);
    }

    public void setDrWhse(int index, int value) {
        setInt("DR-WHSE", value, index);
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

    public BigDecimal getOdAllocQty() {
        return getDecimal("OD-ALLOC-QTY");
    }

    public void setOdAllocQty(BigDecimal value) {
        setDecimal("OD-ALLOC-QTY", value);
    }

    public int getOdLine() {
        return getInt("OD-LINE");
    }

    public void setOdLine(int value) {
        setInt("OD-LINE", value);
    }

    public long getOdNo() {
        return getLong("OD-NO");
    }

    public void setOdNo(long value) {
        setLong("OD-NO", value);
    }

    public int getOdProd() {
        return getInt("OD-PROD");
    }

    public void setOdProd(int value) {
        setInt("OD-PROD", value);
    }

    public BigDecimal getOdQty() {
        return getDecimal("OD-QTY");
    }

    public void setOdQty(BigDecimal value) {
        setDecimal("OD-QTY", value);
    }

    public BigDecimal getOdShippedQty() {
        return getDecimal("OD-SHIPPED-QTY");
    }

    public void setOdShippedQty(BigDecimal value) {
        setDecimal("OD-SHIPPED-QTY", value);
    }

    public int getOdStatus() {
        return getInt("OD-STATUS");
    }

    public void setOdStatus(int value) {
        setInt("OD-STATUS", value);
    }

    public int getOdWhse() {
        return getInt("OD-WHSE");
    }

    public void setOdWhse(int value) {
        setInt("OD-WHSE", value);
    }

    public int getOhCust() {
        return getInt("OH-CUST");
    }

    public void setOhCust(int value) {
        setInt("OH-CUST", value);
    }

    public int getOhDelFlag() {
        return getInt("OH-DEL-FLAG");
    }

    public void setOhDelFlag(int value) {
        setInt("OH-DEL-FLAG", value);
    }

    public long getOhNo() {
        return getLong("OH-NO");
    }

    public void setOhNo(long value) {
        setLong("OH-NO", value);
    }

    public int getOhStatus() {
        return getInt("OH-STATUS");
    }

    public void setOhStatus(int value) {
        setInt("OH-STATUS", value);
    }

    public int getOhUpdDate() {
        return getInt("OH-UPD-DATE");
    }

    public void setOhUpdDate(int value) {
        setInt("OH-UPD-DATE", value);
    }

    public int getOhUpdUser() {
        return getInt("OH-UPD-USER");
    }

    public void setOhUpdUser(int value) {
        setInt("OH-UPD-USER", value);
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

    public int getPrStockMng() {
        return getInt("PR-STOCK-MNG");
    }

    public void setPrStockMng(int value) {
        setInt("PR-STOCK-MNG", value);
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

    public int getWkAllocDone() {
        return getInt("WK-ALLOC-DONE");
    }

    public void setWkAllocDone(int value) {
        setInt("WK-ALLOC-DONE", value);
    }

    public int getWkAvail() {
        return getInt("WK-AVAIL");
    }

    public void setWkAvail(int value) {
        setInt("WK-AVAIL", value);
    }

    public String getWkConfirm() {
        return getString("WK-CONFIRM");
    }

    public void setWkConfirm(String value) {
        setString("WK-CONFIRM", value);
    }

    public long getWkCurNo() {
        return getLong("WK-CUR-NO");
    }

    public void setWkCurNo(long value) {
        setLong("WK-CUR-NO", value);
    }

    public String getWkCustName() {
        return getString("WK-CUST-NAME");
    }

    public void setWkCustName(String value) {
        setString("WK-CUST-NAME", value);
    }

    public int getWkDcnt() {
        return getInt("WK-DCNT");
    }

    public void setWkDcnt(int value) {
        setInt("WK-DCNT", value);
    }

    public String getWkDummy() {
        return getString("WK-DUMMY");
    }

    public void setWkDummy(String value) {
        setString("WK-DUMMY", value);
    }

    public String getWkFkeyLine() {
        return getString("WK-FKEY-LINE");
    }

    public void setWkFkeyLine(String value) {
        setString("WK-FKEY-LINE", value);
    }

    public int getWkGive() {
        return getInt("WK-GIVE");
    }

    public void setWkGive(int value) {
        setInt("WK-GIVE", value);
    }

    public int getWkIdx() {
        return getInt("WK-IDX");
    }

    public void setWkIdx(int value) {
        setInt("WK-IDX", value);
    }

    public int getWkIdx2() {
        return getInt("WK-IDX2");
    }

    public void setWkIdx2(int value) {
        setInt("WK-IDX2", value);
    }

    public String getWkMsgLine() {
        return getString("WK-MSG-LINE");
    }

    public void setWkMsgLine(String value) {
        setString("WK-MSG-LINE", value);
    }

    public int getWkNeed() {
        return getInt("WK-NEED");
    }

    public void setWkNeed(int value) {
        setInt("WK-NEED", value);
    }

    public int getWkPageCnt() {
        return getInt("WK-PAGE-CNT");
    }

    public void setWkPageCnt(int value) {
        setInt("WK-PAGE-CNT", value);
    }

    public int getWkPageNo() {
        return getInt("WK-PAGE-NO");
    }

    public void setWkPageNo(int value) {
        setInt("WK-PAGE-NO", value);
    }

    public int getWkPageTop() {
        return getInt("WK-PAGE-TOP");
    }

    public void setWkPageTop(int value) {
        setInt("WK-PAGE-TOP", value);
    }

    public int getWkPgsize() {
        return getInt("WK-PGSIZE");
    }

    public void setWkPgsize(int value) {
        setInt("WK-PGSIZE", value);
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

    public int getWkReviewEnd() {
        return getInt("WK-REVIEW-END");
    }

    public void setWkReviewEnd(int value) {
        setInt("WK-REVIEW-END", value);
    }

    public long getWkSelNo() {
        return getLong("WK-SEL-NO");
    }

    public void setWkSelNo(long value) {
        setLong("WK-SEL-NO", value);
    }

    public String getWkStatText() {
        return getString("WK-STAT-TEXT");
    }

    public void setWkStatText(String value) {
        setString("WK-STAT-TEXT", value);
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

    public long getWkTotAlloc() {
        return getLong("WK-TOT-ALLOC");
    }

    public void setWkTotAlloc(long value) {
        setLong("WK-TOT-ALLOC", value);
    }

    public long getWkTotShort() {
        return getLong("WK-TOT-SHORT");
    }

    public void setWkTotShort(long value) {
        setLong("WK-TOT-SHORT", value);
    }

    public int getWkUserCode() {
        return getInt("WK-USER-CODE");
    }

    public void setWkUserCode(int value) {
        setInt("WK-USER-CODE", value);
    }

    public int getWkWant() {
        return getInt("WK-WANT");
    }

    public void setWkWant(int value) {
        setInt("WK-WANT", value);
    }

    public int getWwAlloc(int index) {
        return getInt("WW-ALLOC", index);
    }

    public void setWwAlloc(int index, int value) {
        setInt("WW-ALLOC", value, index);
    }

    public int getWwAvail(int index) {
        return getInt("WW-AVAIL", index);
    }

    public void setWwAvail(int index, int value) {
        setInt("WW-AVAIL", value, index);
    }

    public int getWwLine(int index) {
        return getInt("WW-LINE", index);
    }

    public void setWwLine(int index, int value) {
        setInt("WW-LINE", value, index);
    }

    public String getWwName(int index) {
        return getString("WW-NAME", index);
    }

    public void setWwName(int index, String value) {
        setString("WW-NAME", value, index);
    }

    public int getWwOrd(int index) {
        return getInt("WW-ORD", index);
    }

    public void setWwOrd(int index, int value) {
        setInt("WW-ORD", value, index);
    }

    public int getWwProd(int index) {
        return getInt("WW-PROD", index);
    }

    public void setWwProd(int index, int value) {
        setInt("WW-PROD", value, index);
    }

    public int getWwShort(int index) {
        return getInt("WW-SHORT", index);
    }

    public void setWwShort(int index, int value) {
        setInt("WW-SHORT", value, index);
    }
}

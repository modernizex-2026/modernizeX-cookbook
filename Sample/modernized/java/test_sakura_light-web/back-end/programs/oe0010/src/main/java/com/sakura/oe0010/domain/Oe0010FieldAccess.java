package com.sakura.oe0010.domain;

import com.sakura.oe0010.runtime.Oe0010Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for OE0010. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Oe0010FieldAccess extends RuntimeFieldAccess {

    public Oe0010FieldAccess(WorkingStorage ws, Oe0010Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getOrdhf() != null) {
            register(fileSet.getOrdhf().buffer());
        }
        if (fileSet != null && fileSet.getOrddf() != null) {
            register(fileSet.getOrddf().buffer());
        }
        if (fileSet != null && fileSet.getCustf() != null) {
            register(fileSet.getCustf().buffer());
        }
        if (fileSet != null && fileSet.getProdf() != null) {
            register(fileSet.getProdf().buffer());
        }
        if (fileSet != null && fileSet.getCprcf() != null) {
            register(fileSet.getCprcf().buffer());
        }
        if (fileSet != null && fileSet.getStokf() != null) {
            register(fileSet.getStokf().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getCompletionCode() {
        return getInt("COMPLETION-CODE");
    }

    public void setCompletionCode(int value) {
        setInt("COMPLETION-CODE", value);
    }

    public int getCpCust() {
        return getInt("CP-CUST");
    }

    public void setCpCust(int value) {
        setInt("CP-CUST", value);
    }

    public int getCpDelFlag() {
        return getInt("CP-DEL-FLAG");
    }

    public void setCpDelFlag(int value) {
        setInt("CP-DEL-FLAG", value);
    }

    public int getCpEndDate() {
        return getInt("CP-END-DATE");
    }

    public void setCpEndDate(int value) {
        setInt("CP-END-DATE", value);
    }

    public BigDecimal getCpPrice() {
        return getDecimal("CP-PRICE");
    }

    public void setCpPrice(BigDecimal value) {
        setDecimal("CP-PRICE", value);
    }

    public int getCpProd() {
        return getInt("CP-PROD");
    }

    public void setCpProd(int value) {
        setInt("CP-PROD", value);
    }

    public int getCpStartDate() {
        return getInt("CP-START-DATE");
    }

    public void setCpStartDate(int value) {
        setInt("CP-START-DATE", value);
    }

    public int getCuCode() {
        return getInt("CU-CODE");
    }

    public void setCuCode(int value) {
        setInt("CU-CODE", value);
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

    public int getCuPriceRank() {
        return getInt("CU-PRICE-RANK");
    }

    public void setCuPriceRank(int value) {
        setInt("CU-PRICE-RANK", value);
    }

    public int getCuStaff() {
        return getInt("CU-STAFF");
    }

    public void setCuStaff(int value) {
        setInt("CU-STAFF", value);
    }

    public int getCuTaxRound() {
        return getInt("CU-TAX-ROUND");
    }

    public void setCuTaxRound(int value) {
        setInt("CU-TAX-ROUND", value);
    }

    public int getCuTaxType() {
        return getInt("CU-TAX-TYPE");
    }

    public void setCuTaxType(int value) {
        setInt("CU-TAX-TYPE", value);
    }

    public int getDtlDone() {
        return getInt("DTL-DONE");
    }

    public void setDtlDone(int value) {
        setInt("DTL-DONE", value);
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

    public int getHdrOk() {
        return getInt("HDR-OK");
    }

    public void setHdrOk(int value) {
        setInt("HDR-OK", value);
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

    public String getKnum() {
        return groupToString("KNUM");
    }

    public void setKnum(String value) {
        setGroup("KNUM", value);
    }

    public String getKnumKey() {
        return getString("KNUM-KEY");
    }

    public void setKnumKey(String value) {
        setString("KNUM-KEY", value);
    }

    public long getKnumNumber() {
        return getLong("KNUM-NUMBER");
    }

    public void setKnumNumber(long value) {
        setLong("KNUM-NUMBER", value);
    }

    public String getKnumStatus() {
        return getString("KNUM-STATUS");
    }

    public void setKnumStatus(String value) {
        setString("KNUM-STATUS", value);
    }

    public BigDecimal getKtAmount() {
        return getDecimal("KT-AMOUNT");
    }

    public void setKtAmount(BigDecimal value) {
        setDecimal("KT-AMOUNT", value);
    }

    public int getKtCategory() {
        return getInt("KT-CATEGORY");
    }

    public void setKtCategory(int value) {
        setInt("KT-CATEGORY", value);
    }

    public int getKtDate() {
        return getInt("KT-DATE");
    }

    public void setKtDate(int value) {
        setInt("KT-DATE", value);
    }

    public BigDecimal getKtGross() {
        return getDecimal("KT-GROSS");
    }

    public void setKtGross(BigDecimal value) {
        setDecimal("KT-GROSS", value);
    }

    public BigDecimal getKtNet() {
        return getDecimal("KT-NET");
    }

    public void setKtNet(BigDecimal value) {
        setDecimal("KT-NET", value);
    }

    public BigDecimal getKtRate() {
        return getDecimal("KT-RATE");
    }

    public void setKtRate(BigDecimal value) {
        setDecimal("KT-RATE", value);
    }

    public int getKtRound() {
        return getInt("KT-ROUND");
    }

    public void setKtRound(int value) {
        setInt("KT-ROUND", value);
    }

    public String getKtStatus() {
        return getString("KT-STATUS");
    }

    public void setKtStatus(String value) {
        setString("KT-STATUS", value);
    }

    public BigDecimal getKtTax() {
        return getDecimal("KT-TAX");
    }

    public void setKtTax(BigDecimal value) {
        setDecimal("KT-TAX", value);
    }

    public int getKtTaxType() {
        return getInt("KT-TAX-TYPE");
    }

    public void setKtTaxType(int value) {
        setInt("KT-TAX-TYPE", value);
    }

    public String getKtax() {
        return groupToString("KTAX");
    }

    public void setKtax(String value) {
        setGroup("KTAX", value);
    }

    public BigDecimal getOdAllocQty() {
        return getDecimal("OD-ALLOC-QTY");
    }

    public void setOdAllocQty(BigDecimal value) {
        setDecimal("OD-ALLOC-QTY", value);
    }

    public BigDecimal getOdAmount() {
        return getDecimal("OD-AMOUNT");
    }

    public void setOdAmount(BigDecimal value) {
        setDecimal("OD-AMOUNT", value);
    }

    public int getOdDueDate() {
        return getInt("OD-DUE-DATE");
    }

    public void setOdDueDate(int value) {
        setInt("OD-DUE-DATE", value);
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

    public int getOdTaxCategory() {
        return getInt("OD-TAX-CATEGORY");
    }

    public void setOdTaxCategory(int value) {
        setInt("OD-TAX-CATEGORY", value);
    }

    public BigDecimal getOdUnitPrice() {
        return getDecimal("OD-UNIT-PRICE");
    }

    public void setOdUnitPrice(BigDecimal value) {
        setDecimal("OD-UNIT-PRICE", value);
    }

    public int getOdWhse() {
        return getInt("OD-WHSE");
    }

    public void setOdWhse(int value) {
        setInt("OD-WHSE", value);
    }

    public int getOhAddDate() {
        return getInt("OH-ADD-DATE");
    }

    public void setOhAddDate(int value) {
        setInt("OH-ADD-DATE", value);
    }

    public int getOhAddUser() {
        return getInt("OH-ADD-USER");
    }

    public void setOhAddUser(int value) {
        setInt("OH-ADD-USER", value);
    }

    public BigDecimal getOhAmount() {
        return getDecimal("OH-AMOUNT");
    }

    public void setOhAmount(BigDecimal value) {
        setDecimal("OH-AMOUNT", value);
    }

    public int getOhCust() {
        return getInt("OH-CUST");
    }

    public void setOhCust(int value) {
        setInt("OH-CUST", value);
    }

    public String getOhCustPo() {
        return getString("OH-CUST-PO");
    }

    public void setOhCustPo(String value) {
        setString("OH-CUST-PO", value);
    }

    public int getOhDate() {
        return getInt("OH-DATE");
    }

    public void setOhDate(int value) {
        setInt("OH-DATE", value);
    }

    public int getOhDelFlag() {
        return getInt("OH-DEL-FLAG");
    }

    public void setOhDelFlag(int value) {
        setInt("OH-DEL-FLAG", value);
    }

    public int getOhDueDate() {
        return getInt("OH-DUE-DATE");
    }

    public void setOhDueDate(int value) {
        setInt("OH-DUE-DATE", value);
    }

    public int getOhLines() {
        return getInt("OH-LINES");
    }

    public void setOhLines(int value) {
        setInt("OH-LINES", value);
    }

    public long getOhNo() {
        return getLong("OH-NO");
    }

    public void setOhNo(long value) {
        setLong("OH-NO", value);
    }

    public int getOhStaff() {
        return getInt("OH-STAFF");
    }

    public void setOhStaff(int value) {
        setInt("OH-STAFF", value);
    }

    public int getOhStatus() {
        return getInt("OH-STATUS");
    }

    public void setOhStatus(int value) {
        setInt("OH-STATUS", value);
    }

    public BigDecimal getOhTaxAmount() {
        return getDecimal("OH-TAX-AMOUNT");
    }

    public void setOhTaxAmount(BigDecimal value) {
        setDecimal("OH-TAX-AMOUNT", value);
    }

    public int getOhTaxType() {
        return getInt("OH-TAX-TYPE");
    }

    public void setOhTaxType(int value) {
        setInt("OH-TAX-TYPE", value);
    }

    public BigDecimal getOhTotal() {
        return getDecimal("OH-TOTAL");
    }

    public void setOhTotal(BigDecimal value) {
        setDecimal("OH-TOTAL", value);
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

    public int getOhWhse() {
        return getInt("OH-WHSE");
    }

    public void setOhWhse(int value) {
        setInt("OH-WHSE", value);
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

    public BigDecimal getPrRankPrice(int index) {
        return getDecimal("PR-RANK-PRICE", index);
    }

    public void setPrRankPrice(int index, BigDecimal value) {
        setDecimal("PR-RANK-PRICE", value, index);
    }

    public int getPrStockMng() {
        return getInt("PR-STOCK-MNG");
    }

    public void setPrStockMng(int value) {
        setInt("PR-STOCK-MNG", value);
    }

    public int getPrTaxCategory() {
        return getInt("PR-TAX-CATEGORY");
    }

    public void setPrTaxCategory(int value) {
        setInt("PR-TAX-CATEGORY", value);
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

    public String getWkConfirm() {
        return getString("WK-CONFIRM");
    }

    public void setWkConfirm(String value) {
        setString("WK-CONFIRM", value);
    }

    public String getWkCustName() {
        return getString("WK-CUST-NAME");
    }

    public void setWkCustName(String value) {
        setString("WK-CUST-NAME", value);
    }

    public long getWkDAmt() {
        return getLong("WK-D-AMT");
    }

    public void setWkDAmt(long value) {
        setLong("WK-D-AMT", value);
    }

    public int getWkDAvail() {
        return getInt("WK-D-AVAIL");
    }

    public void setWkDAvail(int value) {
        setInt("WK-D-AVAIL", value);
    }

    public String getWkDName() {
        return getString("WK-D-NAME");
    }

    public void setWkDName(String value) {
        setString("WK-D-NAME", value);
    }

    public BigDecimal getWkDPrice() {
        return getDecimal("WK-D-PRICE");
    }

    public void setWkDPrice(BigDecimal value) {
        setDecimal("WK-D-PRICE", value);
    }

    public int getWkDProd() {
        return getInt("WK-D-PROD");
    }

    public void setWkDProd(int value) {
        setInt("WK-D-PROD", value);
    }

    public int getWkDQty() {
        return getInt("WK-D-QTY");
    }

    public void setWkDQty(int value) {
        setInt("WK-D-QTY", value);
    }

    public int getWkDTaxcat() {
        return getInt("WK-D-TAXCAT");
    }

    public void setWkDTaxcat(int value) {
        setInt("WK-D-TAXCAT", value);
    }

    public int getWkDWhse() {
        return getInt("WK-D-WHSE");
    }

    public void setWkDWhse(int value) {
        setInt("WK-D-WHSE", value);
    }

    public String getWkDet() {
        return groupToString("WK-DET");
    }

    public void setWkDet(String value) {
        setGroup("WK-DET", value);
    }

    public String getWkFkeyLine() {
        return getString("WK-FKEY-LINE");
    }

    public void setWkFkeyLine(String value) {
        setString("WK-FKEY-LINE", value);
    }

    public long getWkGrsTotal() {
        return getLong("WK-GRS-TOTAL");
    }

    public void setWkGrsTotal(long value) {
        setLong("WK-GRS-TOTAL", value);
    }

    public int getWkIdx() {
        return getInt("WK-IDX");
    }

    public void setWkIdx(int value) {
        setInt("WK-IDX", value);
    }

    public int getWkLcnt() {
        return getInt("WK-LCNT");
    }

    public void setWkLcnt(int value) {
        setInt("WK-LCNT", value);
    }

    public String getWkMsgLine() {
        return getString("WK-MSG-LINE");
    }

    public void setWkMsgLine(String value) {
        setString("WK-MSG-LINE", value);
    }

    public long getWkNetTotal() {
        return getLong("WK-NET-TOTAL");
    }

    public void setWkNetTotal(long value) {
        setLong("WK-NET-TOTAL", value);
    }

    public long getWkOrdNoD() {
        return getLong("WK-ORD-NO-D");
    }

    public void setWkOrdNoD(long value) {
        setLong("WK-ORD-NO-D", value);
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

    public int getWkSysymd() {
        return getInt("WK-SYSYMD");
    }

    public void setWkSysymd(int value) {
        setInt("WK-SYSYMD", value);
    }

    public long getWkTaxTotal() {
        return getLong("WK-TAX-TOTAL");
    }

    public void setWkTaxTotal(long value) {
        setLong("WK-TAX-TOTAL", value);
    }

    public String getWkTitle() {
        return getString("WK-TITLE");
    }

    public void setWkTitle(String value) {
        setString("WK-TITLE", value);
    }

    public int getWkUserCode() {
        return getInt("WK-USER-CODE");
    }

    public void setWkUserCode(int value) {
        setInt("WK-USER-CODE", value);
    }

    public long getWlAmount(int index) {
        return getLong("WL-AMOUNT", index);
    }

    public void setWlAmount(int index, long value) {
        setLong("WL-AMOUNT", value, index);
    }

    public BigDecimal getWlPrice(int index) {
        return getDecimal("WL-PRICE", index);
    }

    public void setWlPrice(int index, BigDecimal value) {
        setDecimal("WL-PRICE", value, index);
    }

    public int getWlProd(int index) {
        return getInt("WL-PROD", index);
    }

    public void setWlProd(int index, int value) {
        setInt("WL-PROD", value, index);
    }

    public int getWlQty(int index) {
        return getInt("WL-QTY", index);
    }

    public void setWlQty(int index, int value) {
        setInt("WL-QTY", value, index);
    }

    public int getWlTaxcat(int index) {
        return getInt("WL-TAXCAT", index);
    }

    public void setWlTaxcat(int index, int value) {
        setInt("WL-TAXCAT", value, index);
    }

    public int getWlWhse(int index) {
        return getInt("WL-WHSE", index);
    }

    public void setWlWhse(int index, int value) {
        setInt("WL-WHSE", value, index);
    }

    /* ── Synthetic int wrappers (INDEX BY counters, unresolved symbols) ── */
    public int getLx() {
        return getInt("LX");
    }

    public void setLx(int value) {
        setInt("LX", value);
    }
}

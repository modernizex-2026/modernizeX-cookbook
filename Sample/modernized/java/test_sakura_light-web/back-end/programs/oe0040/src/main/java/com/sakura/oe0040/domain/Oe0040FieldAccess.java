package com.sakura.oe0040.domain;

import com.sakura.oe0040.runtime.Oe0040Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for OE0040. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Oe0040FieldAccess extends RuntimeFieldAccess {

    public Oe0040FieldAccess(WorkingStorage ws, Oe0040Datasets fileSet) {
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

    public int getCuTaxRound() {
        return getInt("CU-TAX-ROUND");
    }

    public void setCuTaxRound(int value) {
        setInt("CU-TAX-ROUND", value);
    }

    public int getDlAlloc(int index) {
        return getInt("DL-ALLOC", index);
    }

    public void setDlAlloc(int index, int value) {
        setInt("DL-ALLOC", value, index);
    }

    public int getDlLine(int index) {
        return getInt("DL-LINE", index);
    }

    public void setDlLine(int index, int value) {
        setInt("DL-LINE", value, index);
    }

    public int getDlProd(int index) {
        return getInt("DL-PROD", index);
    }

    public void setDlProd(int index, int value) {
        setInt("DL-PROD", value, index);
    }

    public int getDlStkmng(int index) {
        return getInt("DL-STKMNG", index);
    }

    public void setDlStkmng(int index, int value) {
        setInt("DL-STKMNG", value, index);
    }

    public int getDlWhse(int index) {
        return getInt("DL-WHSE", index);
    }

    public void setDlWhse(int index, int value) {
        setInt("DL-WHSE", value, index);
    }

    public int getDrAlloc(int index) {
        return getInt("DR-ALLOC", index);
    }

    public void setDrAlloc(int index, int value) {
        setInt("DR-ALLOC", value, index);
    }

    public long getDrAmt(int index) {
        return getLong("DR-AMT", index);
    }

    public void setDrAmt(int index, long value) {
        setLong("DR-AMT", value, index);
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

    public int getDrNew(int index) {
        return getInt("DR-NEW", index);
    }

    public void setDrNew(int index, int value) {
        setInt("DR-NEW", value, index);
    }

    public BigDecimal getDrPrice(int index) {
        return getDecimal("DR-PRICE", index);
    }

    public void setDrPrice(int index, BigDecimal value) {
        setDecimal("DR-PRICE", value, index);
    }

    public int getDrProd(int index) {
        return getInt("DR-PROD", index);
    }

    public void setDrProd(int index, int value) {
        setInt("DR-PROD", value, index);
    }

    public int getDrQty(int index) {
        return getInt("DR-QTY", index);
    }

    public void setDrQty(int index, int value) {
        setInt("DR-QTY", value, index);
    }

    public int getDrStatus(int index) {
        return getInt("DR-STATUS", index);
    }

    public void setDrStatus(int index, int value) {
        setInt("DR-STATUS", value, index);
    }

    public int getDrStkmng(int index) {
        return getInt("DR-STKMNG", index);
    }

    public void setDrStkmng(int index, int value) {
        setInt("DR-STKMNG", value, index);
    }

    public int getDrTaxcat(int index) {
        return getInt("DR-TAXCAT", index);
    }

    public void setDrTaxcat(int index, int value) {
        setInt("DR-TAXCAT", value, index);
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

    public int getErrFlg() {
        return getInt("ERR-FLG");
    }

    public void setErrFlg(int value) {
        setInt("ERR-FLG", value);
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

    public BigDecimal getKcAmount() {
        return getDecimal("KC-AMOUNT");
    }

    public void setKcAmount(BigDecimal value) {
        setDecimal("KC-AMOUNT", value);
    }

    public BigDecimal getKcBalance() {
        return getDecimal("KC-BALANCE");
    }

    public void setKcBalance(BigDecimal value) {
        setDecimal("KC-BALANCE", value);
    }

    public int getKcCust() {
        return getInt("KC-CUST");
    }

    public void setKcCust(int value) {
        setInt("KC-CUST", value);
    }

    public int getKcExceed() {
        return getInt("KC-EXCEED");
    }

    public void setKcExceed(int value) {
        setInt("KC-EXCEED", value);
    }

    public BigDecimal getKcLimit() {
        return getDecimal("KC-LIMIT");
    }

    public void setKcLimit(BigDecimal value) {
        setDecimal("KC-LIMIT", value);
    }

    public BigDecimal getKcNewbal() {
        return getDecimal("KC-NEWBAL");
    }

    public void setKcNewbal(BigDecimal value) {
        setDecimal("KC-NEWBAL", value);
    }

    public String getKcStatus() {
        return getString("KC-STATUS");
    }

    public void setKcStatus(String value) {
        setString("KC-STATUS", value);
    }

    public String getKcred() {
        return groupToString("KCRED");
    }

    public void setKcred(String value) {
        setGroup("KCRED", value);
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

    public String getOdRemark() {
        return getString("OD-REMARK");
    }

    public void setOdRemark(String value) {
        setString("OD-REMARK", value);
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

    public String getOhRemark() {
        return getString("OH-REMARK");
    }

    public void setOhRemark(String value) {
        setString("OH-REMARK", value);
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

    public int getPrDfltWhse() {
        return getInt("PR-DFLT-WHSE");
    }

    public void setPrDfltWhse(int value) {
        setInt("PR-DFLT-WHSE", value);
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

    public int getWkAllocated() {
        return getInt("WK-ALLOCATED");
    }

    public void setWkAllocated(int value) {
        setInt("WK-ALLOCATED", value);
    }

    public int getWkApDelta() {
        return getInt("WK-AP-DELTA");
    }

    public void setWkApDelta(int value) {
        setInt("WK-AP-DELTA", value);
    }

    public int getWkApProd() {
        return getInt("WK-AP-PROD");
    }

    public void setWkApProd(int value) {
        setInt("WK-AP-PROD", value);
    }

    public int getWkApTarget() {
        return getInt("WK-AP-TARGET");
    }

    public void setWkApTarget(int value) {
        setInt("WK-AP-TARGET", value);
    }

    public int getWkApWhse() {
        return getInt("WK-AP-WHSE");
    }

    public void setWkApWhse(int value) {
        setInt("WK-AP-WHSE", value);
    }

    public int getWkCancelFlg() {
        return getInt("WK-CANCEL-FLG");
    }

    public void setWkCancelFlg(int value) {
        setInt("WK-CANCEL-FLG", value);
    }

    public String getWkCmd() {
        return getString("WK-CMD");
    }

    public void setWkCmd(String value) {
        setString("WK-CMD", value);
    }

    public int getWkCmdArg() {
        return getInt("WK-CMD-ARG");
    }

    public void setWkCmdArg(int value) {
        setInt("WK-CMD-ARG", value);
    }

    public int getWkCnt() {
        return getInt("WK-CNT");
    }

    public void setWkCnt(int value) {
        setInt("WK-CNT", value);
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

    public int getWkDStkmng() {
        return getInt("WK-D-STKMNG");
    }

    public void setWkDStkmng(int value) {
        setInt("WK-D-STKMNG", value);
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

    public int getWkDcnt() {
        return getInt("WK-DCNT");
    }

    public void setWkDcnt(int value) {
        setInt("WK-DCNT", value);
    }

    public int getWkDelcnt() {
        return getInt("WK-DELCNT");
    }

    public void setWkDelcnt(int value) {
        setInt("WK-DELCNT", value);
    }

    public int getWkDirty() {
        return getInt("WK-DIRTY");
    }

    public void setWkDirty(int value) {
        setInt("WK-DIRTY", value);
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

    public int getWkIdx2() {
        return getInt("WK-IDX2");
    }

    public void setWkIdx2(int value) {
        setInt("WK-IDX2", value);
    }

    public int getWkMaintEnd() {
        return getInt("WK-MAINT-END");
    }

    public void setWkMaintEnd(int value) {
        setInt("WK-MAINT-END", value);
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

    public int getWkNextLine() {
        return getInt("WK-NEXT-LINE");
    }

    public void setWkNextLine(int value) {
        setInt("WK-NEXT-LINE", value);
    }

    public long getWkOrigTotal() {
        return getLong("WK-ORIG-TOTAL");
    }

    public void setWkOrigTotal(long value) {
        setLong("WK-ORIG-TOTAL", value);
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

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
    }

    public long getWkSelOrd() {
        return getLong("WK-SEL-ORD");
    }

    public void setWkSelOrd(long value) {
        setLong("WK-SEL-ORD", value);
    }

    public String getWkStatTxt() {
        return getString("WK-STAT-TXT");
    }

    public void setWkStatTxt(String value) {
        setString("WK-STAT-TXT", value);
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

    public int getWwAlloc(int index) {
        return getInt("WW-ALLOC", index);
    }

    public void setWwAlloc(int index, int value) {
        setInt("WW-ALLOC", value, index);
    }

    public long getWwAmt(int index) {
        return getLong("WW-AMT", index);
    }

    public void setWwAmt(int index, long value) {
        setLong("WW-AMT", value, index);
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

    public BigDecimal getWwPrice(int index) {
        return getDecimal("WW-PRICE", index);
    }

    public void setWwPrice(int index, BigDecimal value) {
        setDecimal("WW-PRICE", value, index);
    }

    public int getWwProd(int index) {
        return getInt("WW-PROD", index);
    }

    public void setWwProd(int index, int value) {
        setInt("WW-PROD", value, index);
    }

    public int getWwQty(int index) {
        return getInt("WW-QTY", index);
    }

    public void setWwQty(int index, int value) {
        setInt("WW-QTY", value, index);
    }
}

package com.sakura.sl0040.domain;

import com.sakura.runtime.record.RuntimeFieldAccess;
import com.sakura.sl0040.runtime.Sl0040Datasets;

import java.math.BigDecimal;

/**
 * Field accessor for SL0040. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Sl0040FieldAccess extends RuntimeFieldAccess {

    public Sl0040FieldAccess(WorkingStorage ws, Sl0040Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getInvhf() != null) {
            register(fileSet.getInvhf().buffer());
        }
        if (fileSet != null && fileSet.getInvdf() != null) {
            register(fileSet.getInvdf().buffer());
        }
        if (fileSet != null && fileSet.getStokf() != null) {
            register(fileSet.getStokf().buffer());
        }
        if (fileSet != null && fileSet.getSmovf() != null) {
            register(fileSet.getSmovf().buffer());
        }
        if (fileSet != null && fileSet.getCustf() != null) {
            register(fileSet.getCustf().buffer());
        }
        if (fileSet != null && fileSet.getProdf() != null) {
            register(fileSet.getProdf().buffer());
        }
        if (fileSet != null && fileSet.getArlf() != null) {
            register(fileSet.getArlf().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public BigDecimal getAlBalance() {
        return getDecimal("AL-BALANCE");
    }

    public void setAlBalance(BigDecimal value) {
        setDecimal("AL-BALANCE", value);
    }

    public int getAlCloseYm() {
        return getInt("AL-CLOSE-YM");
    }

    public void setAlCloseYm(int value) {
        setInt("AL-CLOSE-YM", value);
    }

    public BigDecimal getAlCredit() {
        return getDecimal("AL-CREDIT");
    }

    public void setAlCredit(BigDecimal value) {
        setDecimal("AL-CREDIT", value);
    }

    public int getAlCust() {
        return getInt("AL-CUST");
    }

    public void setAlCust(int value) {
        setInt("AL-CUST", value);
    }

    public int getAlDate() {
        return getInt("AL-DATE");
    }

    public void setAlDate(int value) {
        setInt("AL-DATE", value);
    }

    public BigDecimal getAlDebit() {
        return getDecimal("AL-DEBIT");
    }

    public void setAlDebit(BigDecimal value) {
        setDecimal("AL-DEBIT", value);
    }

    public int getAlKind() {
        return getInt("AL-KIND");
    }

    public void setAlKind(int value) {
        setInt("AL-KIND", value);
    }

    public long getAlRefNo() {
        return getLong("AL-REF-NO");
    }

    public void setAlRefNo(long value) {
        setLong("AL-REF-NO", value);
    }

    public int getAlRefType() {
        return getInt("AL-REF-TYPE");
    }

    public void setAlRefType(int value) {
        setInt("AL-REF-TYPE", value);
    }

    public String getAlRemark() {
        return getString("AL-REMARK");
    }

    public void setAlRemark(String value) {
        setString("AL-REMARK", value);
    }

    public long getAlSeq() {
        return getLong("AL-SEQ");
    }

    public void setAlSeq(long value) {
        setLong("AL-SEQ", value);
    }

    public int getAlUser() {
        return getInt("AL-USER");
    }

    public void setAlUser(int value) {
        setInt("AL-USER", value);
    }

    public int getCompletionCode() {
        return getInt("COMPLETION-CODE");
    }

    public void setCompletionCode(int value) {
        setInt("COMPLETION-CODE", value);
    }

    public BigDecimal getCuBalance() {
        return getDecimal("CU-BALANCE");
    }

    public void setCuBalance(BigDecimal value) {
        setDecimal("CU-BALANCE", value);
    }

    public int getCuCloseDay() {
        return getInt("CU-CLOSE-DAY");
    }

    public void setCuCloseDay(int value) {
        setInt("CU-CLOSE-DAY", value);
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

    public int getCuUpdDate() {
        return getInt("CU-UPD-DATE");
    }

    public void setCuUpdDate(int value) {
        setInt("CU-UPD-DATE", value);
    }

    public int getCuUpdUser() {
        return getInt("CU-UPD-USER");
    }

    public void setCuUpdUser(int value) {
        setInt("CU-UPD-USER", value);
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

    public int getFoundFlg() {
        return getInt("FOUND-FLG");
    }

    public void setFoundFlg(int value) {
        setInt("FOUND-FLG", value);
    }

    public String getFsts() {
        return getString("FSTS");
    }

    public void setFsts(String value) {
        setString("FSTS", value);
    }

    public BigDecimal getIdAmount() {
        return getDecimal("ID-AMOUNT");
    }

    public void setIdAmount(BigDecimal value) {
        setDecimal("ID-AMOUNT", value);
    }

    public BigDecimal getIdCostAmount() {
        return getDecimal("ID-COST-AMOUNT");
    }

    public void setIdCostAmount(BigDecimal value) {
        setDecimal("ID-COST-AMOUNT", value);
    }

    public int getIdLine() {
        return getInt("ID-LINE");
    }

    public void setIdLine(int value) {
        setInt("ID-LINE", value);
    }

    public long getIdNo() {
        return getLong("ID-NO");
    }

    public void setIdNo(long value) {
        setLong("ID-NO", value);
    }

    public int getIdProd() {
        return getInt("ID-PROD");
    }

    public void setIdProd(int value) {
        setInt("ID-PROD", value);
    }

    public BigDecimal getIdQty() {
        return getDecimal("ID-QTY");
    }

    public void setIdQty(BigDecimal value) {
        setDecimal("ID-QTY", value);
    }

    public String getIdRemark() {
        return getString("ID-REMARK");
    }

    public void setIdRemark(String value) {
        setString("ID-REMARK", value);
    }

    public int getIdTaxCategory() {
        return getInt("ID-TAX-CATEGORY");
    }

    public void setIdTaxCategory(int value) {
        setInt("ID-TAX-CATEGORY", value);
    }

    public BigDecimal getIdUnitCost() {
        return getDecimal("ID-UNIT-COST");
    }

    public void setIdUnitCost(BigDecimal value) {
        setDecimal("ID-UNIT-COST", value);
    }

    public BigDecimal getIdUnitPrice() {
        return getDecimal("ID-UNIT-PRICE");
    }

    public void setIdUnitPrice(BigDecimal value) {
        setDecimal("ID-UNIT-PRICE", value);
    }

    public int getIdWhse() {
        return getInt("ID-WHSE");
    }

    public void setIdWhse(int value) {
        setInt("ID-WHSE", value);
    }

    public int getIhAddDate() {
        return getInt("IH-ADD-DATE");
    }

    public void setIhAddDate(int value) {
        setInt("IH-ADD-DATE", value);
    }

    public int getIhAddUser() {
        return getInt("IH-ADD-USER");
    }

    public void setIhAddUser(int value) {
        setInt("IH-ADD-USER", value);
    }

    public BigDecimal getIhAmount() {
        return getDecimal("IH-AMOUNT");
    }

    public void setIhAmount(BigDecimal value) {
        setDecimal("IH-AMOUNT", value);
    }

    public int getIhCloseYm() {
        return getInt("IH-CLOSE-YM");
    }

    public void setIhCloseYm(int value) {
        setInt("IH-CLOSE-YM", value);
    }

    public BigDecimal getIhCostTotal() {
        return getDecimal("IH-COST-TOTAL");
    }

    public void setIhCostTotal(BigDecimal value) {
        setDecimal("IH-COST-TOTAL", value);
    }

    public int getIhCust() {
        return getInt("IH-CUST");
    }

    public void setIhCust(int value) {
        setInt("IH-CUST", value);
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

    public int getIhKind() {
        return getInt("IH-KIND");
    }

    public void setIhKind(int value) {
        setInt("IH-KIND", value);
    }

    public int getIhLines() {
        return getInt("IH-LINES");
    }

    public void setIhLines(int value) {
        setInt("IH-LINES", value);
    }

    public long getIhNo() {
        return getLong("IH-NO");
    }

    public void setIhNo(long value) {
        setLong("IH-NO", value);
    }

    public String getIhRemark() {
        return getString("IH-REMARK");
    }

    public void setIhRemark(String value) {
        setString("IH-REMARK", value);
    }

    public long getIhShipNo() {
        return getLong("IH-SHIP-NO");
    }

    public void setIhShipNo(long value) {
        setLong("IH-SHIP-NO", value);
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

    public int getIhTaxType() {
        return getInt("IH-TAX-TYPE");
    }

    public void setIhTaxType(int value) {
        setInt("IH-TAX-TYPE", value);
    }

    public BigDecimal getIhTotal() {
        return getDecimal("IH-TOTAL");
    }

    public void setIhTotal(BigDecimal value) {
        setDecimal("IH-TOTAL", value);
    }

    public int getIhUpdDate() {
        return getInt("IH-UPD-DATE");
    }

    public void setIhUpdDate(int value) {
        setInt("IH-UPD-DATE", value);
    }

    public int getIhUpdUser() {
        return getInt("IH-UPD-USER");
    }

    public void setIhUpdUser(int value) {
        setInt("IH-UPD-USER", value);
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

    public BigDecimal getSkAvgCost() {
        return getDecimal("SK-AVG-COST");
    }

    public void setSkAvgCost(BigDecimal value) {
        setDecimal("SK-AVG-COST", value);
    }

    public int getSkLastInDate() {
        return getInt("SK-LAST-IN-DATE");
    }

    public void setSkLastInDate(int value) {
        setInt("SK-LAST-IN-DATE", value);
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

    public BigDecimal getSkYtdIn() {
        return getDecimal("SK-YTD-IN");
    }

    public void setSkYtdIn(BigDecimal value) {
        setDecimal("SK-YTD-IN", value);
    }

    public BigDecimal getSmBalAfter() {
        return getDecimal("SM-BAL-AFTER");
    }

    public void setSmBalAfter(BigDecimal value) {
        setDecimal("SM-BAL-AFTER", value);
    }

    public int getSmDate() {
        return getInt("SM-DATE");
    }

    public void setSmDate(int value) {
        setInt("SM-DATE", value);
    }

    public int getSmKind() {
        return getInt("SM-KIND");
    }

    public void setSmKind(int value) {
        setInt("SM-KIND", value);
    }

    public int getSmProd() {
        return getInt("SM-PROD");
    }

    public void setSmProd(int value) {
        setInt("SM-PROD", value);
    }

    public BigDecimal getSmQty() {
        return getDecimal("SM-QTY");
    }

    public void setSmQty(BigDecimal value) {
        setDecimal("SM-QTY", value);
    }

    public long getSmRefNo() {
        return getLong("SM-REF-NO");
    }

    public void setSmRefNo(long value) {
        setLong("SM-REF-NO", value);
    }

    public int getSmRefType() {
        return getInt("SM-REF-TYPE");
    }

    public void setSmRefType(int value) {
        setInt("SM-REF-TYPE", value);
    }

    public long getSmSeq() {
        return getLong("SM-SEQ");
    }

    public void setSmSeq(long value) {
        setLong("SM-SEQ", value);
    }

    public BigDecimal getSmUnitCost() {
        return getDecimal("SM-UNIT-COST");
    }

    public void setSmUnitCost(BigDecimal value) {
        setDecimal("SM-UNIT-COST", value);
    }

    public int getSmUser() {
        return getInt("SM-USER");
    }

    public void setSmUser(int value) {
        setInt("SM-USER", value);
    }

    public int getSmWhse() {
        return getInt("SM-WHSE");
    }

    public void setSmWhse(int value) {
        setInt("SM-WHSE", value);
    }

    public BigDecimal getSrCost(int index) {
        return getDecimal("SR-COST", index);
    }

    public void setSrCost(int index, BigDecimal value) {
        setDecimal("SR-COST", value, index);
    }

    public int getSrCrQty(int index) {
        return getInt("SR-CR-QTY", index);
    }

    public void setSrCrQty(int index, int value) {
        setInt("SR-CR-QTY", value, index);
    }

    public int getSrLine(int index) {
        return getInt("SR-LINE", index);
    }

    public void setSrLine(int index, int value) {
        setInt("SR-LINE", value, index);
    }

    public String getSrName(int index) {
        return getString("SR-NAME", index);
    }

    public void setSrName(int index, String value) {
        setString("SR-NAME", value, index);
    }

    public int getSrOqty(int index) {
        return getInt("SR-OQTY", index);
    }

    public void setSrOqty(int index, int value) {
        setInt("SR-OQTY", value, index);
    }

    public BigDecimal getSrPrice(int index) {
        return getDecimal("SR-PRICE", index);
    }

    public void setSrPrice(int index, BigDecimal value) {
        setDecimal("SR-PRICE", value, index);
    }

    public int getSrProd(int index) {
        return getInt("SR-PROD", index);
    }

    public void setSrProd(int index, int value) {
        setInt("SR-PROD", value, index);
    }

    public int getSrStkmng(int index) {
        return getInt("SR-STKMNG", index);
    }

    public void setSrStkmng(int index, int value) {
        setInt("SR-STKMNG", value, index);
    }

    public int getSrTaxcat(int index) {
        return getInt("SR-TAXCAT", index);
    }

    public void setSrTaxcat(int index, int value) {
        setInt("SR-TAXCAT", value, index);
    }

    public int getSrWhse(int index) {
        return getInt("SR-WHSE", index);
    }

    public void setSrWhse(int index, int value) {
        setInt("SR-WHSE", value, index);
    }

    public int getWkCloseYm() {
        return getInt("WK-CLOSE-YM");
    }

    public void setWkCloseYm(int value) {
        setInt("WK-CLOSE-YM", value);
    }

    public String getWkConfirm() {
        return getString("WK-CONFIRM");
    }

    public void setWkConfirm(String value) {
        setString("WK-CONFIRM", value);
    }

    public long getWkCostTotal() {
        return getLong("WK-COST-TOTAL");
    }

    public void setWkCostTotal(long value) {
        setLong("WK-COST-TOTAL", value);
    }

    public int getWkCrLines() {
        return getInt("WK-CR-LINES");
    }

    public void setWkCrLines(int value) {
        setInt("WK-CR-LINES", value);
    }

    public String getWkCustName() {
        return getString("WK-CUST-NAME");
    }

    public void setWkCustName(String value) {
        setString("WK-CUST-NAME", value);
    }

    public int getWkDay() {
        return getInt("WK-DAY");
    }

    public void setWkDay(int value) {
        setInt("WK-DAY", value);
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

    public long getWkInvNo() {
        return getLong("WK-INV-NO");
    }

    public void setWkInvNo(long value) {
        setLong("WK-INV-NO", value);
    }

    public BigDecimal getWkLineCost() {
        return getDecimal("WK-LINE-COST");
    }

    public void setWkLineCost(BigDecimal value) {
        setDecimal("WK-LINE-COST", value);
    }

    public int getWkLineQty() {
        return getInt("WK-LINE-QTY");
    }

    public void setWkLineQty(int value) {
        setInt("WK-LINE-QTY", value);
    }

    public int getWkMm() {
        return getInt("WK-MM");
    }

    public void setWkMm(int value) {
        setInt("WK-MM", value);
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

    public int getWkNewbal() {
        return getInt("WK-NEWBAL");
    }

    public void setWkNewbal(int value) {
        setInt("WK-NEWBAL", value);
    }

    public long getWkOrigInv() {
        return getLong("WK-ORIG-INV");
    }

    public void setWkOrigInv(long value) {
        setLong("WK-ORIG-INV", value);
    }

    public int getWkOutLine() {
        return getInt("WK-OUT-LINE");
    }

    public void setWkOutLine(int value) {
        setInt("WK-OUT-LINE", value);
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

    public int getWkPickEnd() {
        return getInt("WK-PICK-END");
    }

    public void setWkPickEnd(int value) {
        setInt("WK-PICK-END", value);
    }

    public int getWkPickLine() {
        return getInt("WK-PICK-LINE");
    }

    public void setWkPickLine(int value) {
        setInt("WK-PICK-LINE", value);
    }

    public int getWkPickQty() {
        return getInt("WK-PICK-QTY");
    }

    public void setWkPickQty(int value) {
        setInt("WK-PICK-QTY", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
    }

    public int getWkScnt() {
        return getInt("WK-SCNT");
    }

    public void setWkScnt(int value) {
        setInt("WK-SCNT", value);
    }

    public int getWkSelCust() {
        return getInt("WK-SEL-CUST");
    }

    public void setWkSelCust(int value) {
        setInt("WK-SEL-CUST", value);
    }

    public int getWkStaffIn() {
        return getInt("WK-STAFF-IN");
    }

    public void setWkStaffIn(int value) {
        setInt("WK-STAFF-IN", value);
    }

    public int getWkStkFound() {
        return getInt("WK-STK-FOUND");
    }

    public void setWkStkFound(int value) {
        setInt("WK-STK-FOUND", value);
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

    public int getWkTaxtypeIn() {
        return getInt("WK-TAXTYPE-IN");
    }

    public void setWkTaxtypeIn(int value) {
        setInt("WK-TAXTYPE-IN", value);
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

    public int getWkYm() {
        return getInt("WK-YM");
    }

    public void setWkYm(int value) {
        setInt("WK-YM", value);
    }

    public int getWkYyyy() {
        return getInt("WK-YYYY");
    }

    public void setWkYyyy(int value) {
        setInt("WK-YYYY", value);
    }

    public long getWwAmt(int index) {
        return getLong("WW-AMT", index);
    }

    public void setWwAmt(int index, long value) {
        setLong("WW-AMT", value, index);
    }

    public int getWwCqty(int index) {
        return getInt("WW-CQTY", index);
    }

    public void setWwCqty(int index, int value) {
        setInt("WW-CQTY", value, index);
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

    public int getWwOqty(int index) {
        return getInt("WW-OQTY", index);
    }

    public void setWwOqty(int index, int value) {
        setInt("WW-OQTY", value, index);
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
}

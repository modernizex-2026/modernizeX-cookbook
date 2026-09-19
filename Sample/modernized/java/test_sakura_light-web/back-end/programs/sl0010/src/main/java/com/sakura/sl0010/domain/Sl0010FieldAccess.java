package com.sakura.sl0010.domain;

import com.sakura.runtime.record.RuntimeFieldAccess;
import com.sakura.sl0010.runtime.Sl0010Datasets;

import java.math.BigDecimal;

/**
 * Field accessor for SL0010. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Sl0010FieldAccess extends RuntimeFieldAccess {

    public Sl0010FieldAccess(WorkingStorage ws, Sl0010Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getInvhf() != null) {
            register(fileSet.getInvhf().buffer());
        }
        if (fileSet != null && fileSet.getInvdf() != null) {
            register(fileSet.getInvdf().buffer());
        }
        if (fileSet != null && fileSet.getShphf() != null) {
            register(fileSet.getShphf().buffer());
        }
        if (fileSet != null && fileSet.getShpdf() != null) {
            register(fileSet.getShpdf().buffer());
        }
        if (fileSet != null && fileSet.getOrdhf() != null) {
            register(fileSet.getOrdhf().buffer());
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
        if (fileSet != null && fileSet.getCprcf() != null) {
            register(fileSet.getCprcf().buffer());
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

    public long getDrAmt(int index) {
        return getLong("DR-AMT", index);
    }

    public void setDrAmt(int index, long value) {
        setLong("DR-AMT", value, index);
    }

    public BigDecimal getDrCost(int index) {
        return getDecimal("DR-COST", index);
    }

    public void setDrCost(int index, BigDecimal value) {
        setDecimal("DR-COST", value, index);
    }

    public long getDrCostamt(int index) {
        return getLong("DR-COSTAMT", index);
    }

    public void setDrCostamt(int index, long value) {
        setLong("DR-COSTAMT", value, index);
    }

    public String getDrName(int index) {
        return getString("DR-NAME", index);
    }

    public void setDrName(int index, String value) {
        setString("DR-NAME", value, index);
    }

    public long getDrOrder(int index) {
        return getLong("DR-ORDER", index);
    }

    public void setDrOrder(int index, long value) {
        setLong("DR-ORDER", value, index);
    }

    public int getDrOrdline(int index) {
        return getInt("DR-ORDLINE", index);
    }

    public void setDrOrdline(int index, int value) {
        setInt("DR-ORDLINE", value, index);
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

    public BigDecimal getPrStdCost() {
        return getDecimal("PR-STD-COST");
    }

    public void setPrStdCost(BigDecimal value) {
        setDecimal("PR-STD-COST", value);
    }

    public int getPrTaxCategory() {
        return getInt("PR-TAX-CATEGORY");
    }

    public void setPrTaxCategory(int value) {
        setInt("PR-TAX-CATEGORY", value);
    }

    public BigDecimal getSkAvgCost() {
        return getDecimal("SK-AVG-COST");
    }

    public void setSkAvgCost(BigDecimal value) {
        setDecimal("SK-AVG-COST", value);
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

    public BigDecimal getWkDCost() {
        return getDecimal("WK-D-COST");
    }

    public void setWkDCost(BigDecimal value) {
        setDecimal("WK-D-COST", value);
    }

    public long getWkDCostamt() {
        return getLong("WK-D-COSTAMT");
    }

    public void setWkDCostamt(long value) {
        setLong("WK-D-COSTAMT", value);
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

    public int getWkDay() {
        return getInt("WK-DAY");
    }

    public void setWkDay(int value) {
        setInt("WK-DAY", value);
    }

    public int getWkDcnt() {
        return getInt("WK-DCNT");
    }

    public void setWkDcnt(int value) {
        setInt("WK-DCNT", value);
    }

    public int getWkDtlDone() {
        return getInt("WK-DTL-DONE");
    }

    public void setWkDtlDone(int value) {
        setInt("WK-DTL-DONE", value);
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

    public int getWkMm() {
        return getInt("WK-MM");
    }

    public void setWkMm(int value) {
        setInt("WK-MM", value);
    }

    public int getWkMode() {
        return getInt("WK-MODE");
    }

    public void setWkMode(int value) {
        setInt("WK-MODE", value);
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

    public int getWkReviewEnd() {
        return getInt("WK-REVIEW-END");
    }

    public void setWkReviewEnd(int value) {
        setInt("WK-REVIEW-END", value);
    }

    public int getWkSelCust() {
        return getInt("WK-SEL-CUST");
    }

    public void setWkSelCust(int value) {
        setInt("WK-SEL-CUST", value);
    }

    public long getWkSelShip() {
        return getLong("WK-SEL-SHIP");
    }

    public void setWkSelShip(long value) {
        setLong("WK-SEL-SHIP", value);
    }

    public int getWkShipDate() {
        return getInt("WK-SHIP-DATE");
    }

    public void setWkShipDate(int value) {
        setInt("WK-SHIP-DATE", value);
    }

    public int getWkStaffIn() {
        return getInt("WK-STAFF-IN");
    }

    public void setWkStaffIn(int value) {
        setInt("WK-STAFF-IN", value);
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

    public long getWwCostamt(int index) {
        return getLong("WW-COSTAMT", index);
    }

    public void setWwCostamt(int index, long value) {
        setLong("WW-COSTAMT", value, index);
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

    public int getXdLine() {
        return getInt("XD-LINE");
    }

    public void setXdLine(int value) {
        setInt("XD-LINE", value);
    }

    public long getXdNo() {
        return getLong("XD-NO");
    }

    public void setXdNo(long value) {
        setLong("XD-NO", value);
    }

    public long getXdOrder() {
        return getLong("XD-ORDER");
    }

    public void setXdOrder(long value) {
        setLong("XD-ORDER", value);
    }

    public int getXdOrderLine() {
        return getInt("XD-ORDER-LINE");
    }

    public void setXdOrderLine(int value) {
        setInt("XD-ORDER-LINE", value);
    }

    public int getXdProd() {
        return getInt("XD-PROD");
    }

    public void setXdProd(int value) {
        setInt("XD-PROD", value);
    }

    public BigDecimal getXdQty() {
        return getDecimal("XD-QTY");
    }

    public void setXdQty(BigDecimal value) {
        setDecimal("XD-QTY", value);
    }

    public BigDecimal getXdUnitCost() {
        return getDecimal("XD-UNIT-COST");
    }

    public void setXdUnitCost(BigDecimal value) {
        setDecimal("XD-UNIT-COST", value);
    }

    public BigDecimal getXdUnitPrice() {
        return getDecimal("XD-UNIT-PRICE");
    }

    public void setXdUnitPrice(BigDecimal value) {
        setDecimal("XD-UNIT-PRICE", value);
    }

    public int getXdWhse() {
        return getInt("XD-WHSE");
    }

    public void setXdWhse(int value) {
        setInt("XD-WHSE", value);
    }

    public int getXhCust() {
        return getInt("XH-CUST");
    }

    public void setXhCust(int value) {
        setInt("XH-CUST", value);
    }

    public int getXhDate() {
        return getInt("XH-DATE");
    }

    public void setXhDate(int value) {
        setInt("XH-DATE", value);
    }

    public int getXhDelFlag() {
        return getInt("XH-DEL-FLAG");
    }

    public void setXhDelFlag(int value) {
        setInt("XH-DEL-FLAG", value);
    }

    public long getXhNo() {
        return getLong("XH-NO");
    }

    public void setXhNo(long value) {
        setLong("XH-NO", value);
    }

    public long getXhOrder() {
        return getLong("XH-ORDER");
    }

    public void setXhOrder(long value) {
        setLong("XH-ORDER", value);
    }

    public int getXhStaff() {
        return getInt("XH-STAFF");
    }

    public void setXhStaff(int value) {
        setInt("XH-STAFF", value);
    }

    public int getXhStatus() {
        return getInt("XH-STATUS");
    }

    public void setXhStatus(int value) {
        setInt("XH-STATUS", value);
    }
}

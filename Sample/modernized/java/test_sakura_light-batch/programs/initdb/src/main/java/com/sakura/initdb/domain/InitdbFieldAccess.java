package com.sakura.initdb.domain;

import com.sakura.initdb.runtime.InitdbDatasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for INITDB. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class InitdbFieldAccess extends RuntimeFieldAccess {

    public InitdbFieldAccess(WorkingStorage ws, InitdbDatasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getSyscf() != null) {
            register(fileSet.getSyscf().buffer());
        }
        if (fileSet != null && fileSet.getNumcf() != null) {
            register(fileSet.getNumcf().buffer());
        }
        if (fileSet != null && fileSet.getTaxf() != null) {
            register(fileSet.getTaxf().buffer());
        }
        if (fileSet != null && fileSet.getRegnf() != null) {
            register(fileSet.getRegnf().buffer());
        }
        if (fileSet != null && fileSet.getDeptf() != null) {
            register(fileSet.getDeptf().buffer());
        }
        if (fileSet != null && fileSet.getCatgf() != null) {
            register(fileSet.getCatgf().buffer());
        }
        if (fileSet != null && fileSet.getBankf() != null) {
            register(fileSet.getBankf().buffer());
        }
        if (fileSet != null && fileSet.getWhsef() != null) {
            register(fileSet.getWhsef().buffer());
        }
        if (fileSet != null && fileSet.getStaff() != null) {
            register(fileSet.getStaff().buffer());
        }
        if (fileSet != null && fileSet.getUserf() != null) {
            register(fileSet.getUserf().buffer());
        }
        if (fileSet != null && fileSet.getCustf() != null) {
            register(fileSet.getCustf().buffer());
        }
        if (fileSet != null && fileSet.getSuppf() != null) {
            register(fileSet.getSuppf().buffer());
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
        if (fileSet != null && fileSet.getMsgf() != null) {
            register(fileSet.getMsgf().buffer());
        }
        if (fileSet != null && fileSet.getOrdhf() != null) {
            register(fileSet.getOrdhf().buffer());
        }
        if (fileSet != null && fileSet.getOrddf() != null) {
            register(fileSet.getOrddf().buffer());
        }
        if (fileSet != null && fileSet.getShphf() != null) {
            register(fileSet.getShphf().buffer());
        }
        if (fileSet != null && fileSet.getShpdf() != null) {
            register(fileSet.getShpdf().buffer());
        }
        if (fileSet != null && fileSet.getInvhf() != null) {
            register(fileSet.getInvhf().buffer());
        }
        if (fileSet != null && fileSet.getInvdf() != null) {
            register(fileSet.getInvdf().buffer());
        }
        if (fileSet != null && fileSet.getPohf() != null) {
            register(fileSet.getPohf().buffer());
        }
        if (fileSet != null && fileSet.getPodf() != null) {
            register(fileSet.getPodf().buffer());
        }
        if (fileSet != null && fileSet.getRcvhf() != null) {
            register(fileSet.getRcvhf().buffer());
        }
        if (fileSet != null && fileSet.getRcvdf() != null) {
            register(fileSet.getRcvdf().buffer());
        }
        if (fileSet != null && fileSet.getPurhf() != null) {
            register(fileSet.getPurhf().buffer());
        }
        if (fileSet != null && fileSet.getPurdf() != null) {
            register(fileSet.getPurdf().buffer());
        }
        if (fileSet != null && fileSet.getArlf() != null) {
            register(fileSet.getArlf().buffer());
        }
        if (fileSet != null && fileSet.getAplf() != null) {
            register(fileSet.getAplf().buffer());
        }
        if (fileSet != null && fileSet.getRcptf() != null) {
            register(fileSet.getRcptf().buffer());
        }
        if (fileSet != null && fileSet.getPayf() != null) {
            register(fileSet.getPayf().buffer());
        }
        if (fileSet != null && fileSet.getSmovf() != null) {
            register(fileSet.getSmovf().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public String getBkBranch() {
        return getString("BK-BRANCH");
    }

    public void setBkBranch(String value) {
        setString("BK-BRANCH", value);
    }

    public int getBkCode() {
        return getInt("BK-CODE");
    }

    public void setBkCode(int value) {
        setInt("BK-CODE", value);
    }

    public String getBkName() {
        return getString("BK-NAME");
    }

    public void setBkName(String value) {
        setString("BK-NAME", value);
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

    public int getCtCode() {
        return getInt("CT-CODE");
    }

    public void setCtCode(int value) {
        setInt("CT-CODE", value);
    }

    public int getCtLevel() {
        return getInt("CT-LEVEL");
    }

    public void setCtLevel(int value) {
        setInt("CT-LEVEL", value);
    }

    public String getCtName() {
        return getString("CT-NAME");
    }

    public void setCtName(String value) {
        setString("CT-NAME", value);
    }

    public int getCuBankCode() {
        return getInt("CU-BANK-CODE");
    }

    public void setCuBankCode(int value) {
        setInt("CU-BANK-CODE", value);
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

    public BigDecimal getCuCreditLimit() {
        return getDecimal("CU-CREDIT-LIMIT");
    }

    public void setCuCreditLimit(BigDecimal value) {
        setDecimal("CU-CREDIT-LIMIT", value);
    }

    public String getCuKana() {
        return getString("CU-KANA");
    }

    public void setCuKana(String value) {
        setString("CU-KANA", value);
    }

    public String getCuName() {
        return getString("CU-NAME");
    }

    public void setCuName(String value) {
        setString("CU-NAME", value);
    }

    public int getCuPayMethod() {
        return getInt("CU-PAY-METHOD");
    }

    public void setCuPayMethod(int value) {
        setInt("CU-PAY-METHOD", value);
    }

    public int getCuPriceRank() {
        return getInt("CU-PRICE-RANK");
    }

    public void setCuPriceRank(int value) {
        setInt("CU-PRICE-RANK", value);
    }

    public int getCuRegion() {
        return getInt("CU-REGION");
    }

    public void setCuRegion(int value) {
        setInt("CU-REGION", value);
    }

    public int getCuStaff() {
        return getInt("CU-STAFF");
    }

    public void setCuStaff(int value) {
        setInt("CU-STAFF", value);
    }

    public int getCuStartDate() {
        return getInt("CU-START-DATE");
    }

    public void setCuStartDate(int value) {
        setInt("CU-START-DATE", value);
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

    public int getDpCode() {
        return getInt("DP-CODE");
    }

    public void setDpCode(int value) {
        setInt("DP-CODE", value);
    }

    public String getDpName() {
        return getString("DP-NAME");
    }

    public void setDpName(String value) {
        setString("DP-NAME", value);
    }

    public String getMgCode() {
        return getString("MG-CODE");
    }

    public void setMgCode(String value) {
        setString("MG-CODE", value);
    }

    public String getMgText() {
        return getString("MG-TEXT");
    }

    public void setMgText(String value) {
        setString("MG-TEXT", value);
    }

    public long getNmCurrent() {
        return getLong("NM-CURRENT");
    }

    public void setNmCurrent(long value) {
        setLong("NM-CURRENT", value);
    }

    public String getNmKey() {
        return getString("NM-KEY");
    }

    public void setNmKey(String value) {
        setString("NM-KEY", value);
    }

    public String getNmPrefix() {
        return getString("NM-PREFIX");
    }

    public void setNmPrefix(String value) {
        setString("NM-PREFIX", value);
    }

    public int getNmWidth() {
        return getInt("NM-WIDTH");
    }

    public void setNmWidth(int value) {
        setInt("NM-WIDTH", value);
    }

    public int getPrCategory() {
        return getInt("PR-CATEGORY");
    }

    public void setPrCategory(int value) {
        setInt("PR-CATEGORY", value);
    }

    public int getPrCode() {
        return getInt("PR-CODE");
    }

    public void setPrCode(int value) {
        setInt("PR-CODE", value);
    }

    public int getPrDfltSupp() {
        return getInt("PR-DFLT-SUPP");
    }

    public void setPrDfltSupp(int value) {
        setInt("PR-DFLT-SUPP", value);
    }

    public int getPrDfltWhse() {
        return getInt("PR-DFLT-WHSE");
    }

    public void setPrDfltWhse(int value) {
        setInt("PR-DFLT-WHSE", value);
    }

    public BigDecimal getPrLastCost() {
        return getDecimal("PR-LAST-COST");
    }

    public void setPrLastCost(BigDecimal value) {
        setDecimal("PR-LAST-COST", value);
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

    public BigDecimal getPrSafetyStock() {
        return getDecimal("PR-SAFETY-STOCK");
    }

    public void setPrSafetyStock(BigDecimal value) {
        setDecimal("PR-SAFETY-STOCK", value);
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

    public int getPrTaxCategory() {
        return getInt("PR-TAX-CATEGORY");
    }

    public void setPrTaxCategory(int value) {
        setInt("PR-TAX-CATEGORY", value);
    }

    public String getPrUnit() {
        return getString("PR-UNIT");
    }

    public void setPrUnit(String value) {
        setString("PR-UNIT", value);
    }

    public int getRgCode() {
        return getInt("RG-CODE");
    }

    public void setRgCode(int value) {
        setInt("RG-CODE", value);
    }

    public String getRgName() {
        return getString("RG-NAME");
    }

    public void setRgName(String value) {
        setString("RG-NAME", value);
    }

    public int getSfCode() {
        return getInt("SF-CODE");
    }

    public void setSfCode(int value) {
        setInt("SF-CODE", value);
    }

    public int getSfDept() {
        return getInt("SF-DEPT");
    }

    public void setSfDept(int value) {
        setInt("SF-DEPT", value);
    }

    public String getSfName() {
        return getString("SF-NAME");
    }

    public void setSfName(String value) {
        setString("SF-NAME", value);
    }

    public String getSfTitle() {
        return getString("SF-TITLE");
    }

    public void setSfTitle(String value) {
        setString("SF-TITLE", value);
    }

    public BigDecimal getSkAllocated() {
        return getDecimal("SK-ALLOCATED");
    }

    public void setSkAllocated(BigDecimal value) {
        setDecimal("SK-ALLOCATED", value);
    }

    public String getSkLocation() {
        return getString("SK-LOCATION");
    }

    public void setSkLocation(String value) {
        setString("SK-LOCATION", value);
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

    public int getSpBankCode() {
        return getInt("SP-BANK-CODE");
    }

    public void setSpBankCode(int value) {
        setInt("SP-BANK-CODE", value);
    }

    public int getSpCloseDay() {
        return getInt("SP-CLOSE-DAY");
    }

    public void setSpCloseDay(int value) {
        setInt("SP-CLOSE-DAY", value);
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

    public int getSpPayMethod() {
        return getInt("SP-PAY-METHOD");
    }

    public void setSpPayMethod(int value) {
        setInt("SP-PAY-METHOD", value);
    }

    public int getSpTaxType() {
        return getInt("SP-TAX-TYPE");
    }

    public void setSpTaxType(int value) {
        setInt("SP-TAX-TYPE", value);
    }

    public String getSyCompanyAddr() {
        return getString("SY-COMPANY-ADDR");
    }

    public void setSyCompanyAddr(String value) {
        setString("SY-COMPANY-ADDR", value);
    }

    public String getSyCompanyName() {
        return getString("SY-COMPANY-NAME");
    }

    public void setSyCompanyName(String value) {
        setString("SY-COMPANY-NAME", value);
    }

    public String getSyCompanyTel() {
        return getString("SY-COMPANY-TEL");
    }

    public void setSyCompanyTel(String value) {
        setString("SY-COMPANY-TEL", value);
    }

    public String getSyCompanyZip() {
        return getString("SY-COMPANY-ZIP");
    }

    public void setSyCompanyZip(String value) {
        setString("SY-COMPANY-ZIP", value);
    }

    public int getSyCurrYm() {
        return getInt("SY-CURR-YM");
    }

    public void setSyCurrYm(int value) {
        setInt("SY-CURR-YM", value);
    }

    public int getSyDecRound() {
        return getInt("SY-DEC-ROUND");
    }

    public void setSyDecRound(int value) {
        setInt("SY-DEC-ROUND", value);
    }

    public int getSyFiscalStart() {
        return getInt("SY-FISCAL-START");
    }

    public void setSyFiscalStart(int value) {
        setInt("SY-FISCAL-START", value);
    }

    public int getSyKey() {
        return getInt("SY-KEY");
    }

    public void setSyKey(int value) {
        setInt("SY-KEY", value);
    }

    public int getSyLastDayClose() {
        return getInt("SY-LAST-DAY-CLOSE");
    }

    public void setSyLastDayClose(int value) {
        setInt("SY-LAST-DAY-CLOSE", value);
    }

    public int getSyLastMonClose() {
        return getInt("SY-LAST-MON-CLOSE");
    }

    public void setSyLastMonClose(int value) {
        setInt("SY-LAST-MON-CLOSE", value);
    }

    public BigDecimal getSyTaxDfltRate() {
        return getDecimal("SY-TAX-DFLT-RATE");
    }

    public void setSyTaxDfltRate(BigDecimal value) {
        setDecimal("SY-TAX-DFLT-RATE", value);
    }

    public int getTxCode() {
        return getInt("TX-CODE");
    }

    public void setTxCode(int value) {
        setInt("TX-CODE", value);
    }

    public String getTxName() {
        return getString("TX-NAME");
    }

    public void setTxName(String value) {
        setString("TX-NAME", value);
    }

    public BigDecimal getTxRate() {
        return getDecimal("TX-RATE");
    }

    public void setTxRate(BigDecimal value) {
        setDecimal("TX-RATE", value);
    }

    public int getTxStartDate() {
        return getInt("TX-START-DATE");
    }

    public void setTxStartDate(int value) {
        setInt("TX-START-DATE", value);
    }

    public int getUsAuthClose() {
        return getInt("US-AUTH-CLOSE");
    }

    public void setUsAuthClose(int value) {
        setInt("US-AUTH-CLOSE", value);
    }

    public int getUsAuthMaster() {
        return getInt("US-AUTH-MASTER");
    }

    public void setUsAuthMaster(int value) {
        setInt("US-AUTH-MASTER", value);
    }

    public int getUsAuthOrder() {
        return getInt("US-AUTH-ORDER");
    }

    public void setUsAuthOrder(int value) {
        setInt("US-AUTH-ORDER", value);
    }

    public int getUsAuthPurch() {
        return getInt("US-AUTH-PURCH");
    }

    public void setUsAuthPurch(int value) {
        setInt("US-AUTH-PURCH", value);
    }

    public int getUsAuthSales() {
        return getInt("US-AUTH-SALES");
    }

    public void setUsAuthSales(int value) {
        setInt("US-AUTH-SALES", value);
    }

    public int getUsCode() {
        return getInt("US-CODE");
    }

    public void setUsCode(int value) {
        setInt("US-CODE", value);
    }

    public String getUsLogin() {
        return getString("US-LOGIN");
    }

    public void setUsLogin(String value) {
        setString("US-LOGIN", value);
    }

    public String getUsName() {
        return getString("US-NAME");
    }

    public void setUsName(String value) {
        setString("US-NAME", value);
    }

    public String getUsPassword() {
        return getString("US-PASSWORD");
    }

    public void setUsPassword(String value) {
        setString("US-PASSWORD", value);
    }

    public int getUsRole() {
        return getInt("US-ROLE");
    }

    public void setUsRole(int value) {
        setInt("US-ROLE", value);
    }

    public int getWhCode() {
        return getInt("WH-CODE");
    }

    public void setWhCode(int value) {
        setInt("WH-CODE", value);
    }

    public int getWhManager() {
        return getInt("WH-MANAGER");
    }

    public void setWhManager(int value) {
        setInt("WH-MANAGER", value);
    }

    public String getWhName() {
        return getString("WH-NAME");
    }

    public void setWhName(String value) {
        setString("WH-NAME", value);
    }

    public int getWhType() {
        return getInt("WH-TYPE");
    }

    public void setWhType(int value) {
        setInt("WH-TYPE", value);
    }

    public int getWkI() {
        return getInt("WK-I");
    }

    public void setWkI(int value) {
        setInt("WK-I", value);
    }
}

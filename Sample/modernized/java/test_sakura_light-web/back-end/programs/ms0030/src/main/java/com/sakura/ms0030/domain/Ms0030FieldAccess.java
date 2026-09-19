package com.sakura.ms0030.domain;

import com.sakura.ms0030.runtime.Ms0030Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for MS0030. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Ms0030FieldAccess extends RuntimeFieldAccess {

    public Ms0030FieldAccess(WorkingStorage ws, Ms0030Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getProdf() != null) {
            register(fileSet.getProdf().buffer());
        }
        if (fileSet != null && fileSet.getCatgf() != null) {
            register(fileSet.getCatgf().buffer());
        }
        if (fileSet != null && fileSet.getSuppf() != null) {
            register(fileSet.getSuppf().buffer());
        }
        if (fileSet != null && fileSet.getWhsef() != null) {
            register(fileSet.getWhsef().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getCompletionCode() {
        return getInt("COMPLETION-CODE");
    }

    public void setCompletionCode(int value) {
        setInt("COMPLETION-CODE", value);
    }

    public int getCtCode() {
        return getInt("CT-CODE");
    }

    public void setCtCode(int value) {
        setInt("CT-CODE", value);
    }

    public String getCtName() {
        return getString("CT-NAME");
    }

    public void setCtName(String value) {
        setString("CT-NAME", value);
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

    public int getModeFlg() {
        return getInt("MODE-FLG");
    }

    public void setModeFlg(int value) {
        setInt("MODE-FLG", value);
    }

    public int getPrAddDate() {
        return getInt("PR-ADD-DATE");
    }

    public void setPrAddDate(int value) {
        setInt("PR-ADD-DATE", value);
    }

    public int getPrAddUser() {
        return getInt("PR-ADD-USER");
    }

    public void setPrAddUser(int value) {
        setInt("PR-ADD-USER", value);
    }

    public String getPrBarcode() {
        return getString("PR-BARCODE");
    }

    public void setPrBarcode(String value) {
        setString("PR-BARCODE", value);
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

    public int getPrDfltWhse() {
        return getInt("PR-DFLT-WHSE");
    }

    public void setPrDfltWhse(int value) {
        setInt("PR-DFLT-WHSE", value);
    }

    public String getPrKana() {
        return getString("PR-KANA");
    }

    public void setPrKana(String value) {
        setString("PR-KANA", value);
    }

    public BigDecimal getPrLastCost() {
        return getDecimal("PR-LAST-COST");
    }

    public void setPrLastCost(BigDecimal value) {
        setDecimal("PR-LAST-COST", value);
    }

    public int getPrLeadDays() {
        return getInt("PR-LEAD-DAYS");
    }

    public void setPrLeadDays(int value) {
        setInt("PR-LEAD-DAYS", value);
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

    public String getPrSpec() {
        return getString("PR-SPEC");
    }

    public void setPrSpec(String value) {
        setString("PR-SPEC", value);
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

    public int getPrUpdDate() {
        return getInt("PR-UPD-DATE");
    }

    public void setPrUpdDate(int value) {
        setInt("PR-UPD-DATE", value);
    }

    public int getPrUpdUser() {
        return getInt("PR-UPD-USER");
    }

    public void setPrUpdUser(int value) {
        setInt("PR-UPD-USER", value);
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

    public String getWkCatgName() {
        return getString("WK-CATG-NAME");
    }

    public void setWkCatgName(String value) {
        setString("WK-CATG-NAME", value);
    }

    public String getWkConfirm() {
        return getString("WK-CONFIRM");
    }

    public void setWkConfirm(String value) {
        setString("WK-CONFIRM", value);
    }

    public String getWkFkeyLine() {
        return getString("WK-FKEY-LINE");
    }

    public void setWkFkeyLine(String value) {
        setString("WK-FKEY-LINE", value);
    }

    public int getWkIdx() {
        return getInt("WK-IDX");
    }

    public void setWkIdx(int value) {
        setInt("WK-IDX", value);
    }

    public String getWkMsgLine() {
        return getString("WK-MSG-LINE");
    }

    public void setWkMsgLine(String value) {
        setString("WK-MSG-LINE", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
    }

    public int getWkSaveCode() {
        return getInt("WK-SAVE-CODE");
    }

    public void setWkSaveCode(int value) {
        setInt("WK-SAVE-CODE", value);
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

    public int getWkUserCode() {
        return getInt("WK-USER-CODE");
    }

    public void setWkUserCode(int value) {
        setInt("WK-USER-CODE", value);
    }

    public String getWkWhseName() {
        return getString("WK-WHSE-NAME");
    }

    public void setWkWhseName(String value) {
        setString("WK-WHSE-NAME", value);
    }
}

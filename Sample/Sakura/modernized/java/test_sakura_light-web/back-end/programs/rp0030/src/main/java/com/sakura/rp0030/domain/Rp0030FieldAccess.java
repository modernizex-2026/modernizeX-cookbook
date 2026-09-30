package com.sakura.rp0030.domain;

import com.sakura.rp0030.runtime.Rp0030Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for RP0030. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Rp0030FieldAccess extends RuntimeFieldAccess {

    public Rp0030FieldAccess(WorkingStorage ws, Rp0030Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getInvhf() != null) {
            register(fileSet.getInvhf().buffer());
        }
        if (fileSet != null && fileSet.getInvdf() != null) {
            register(fileSet.getInvdf().buffer());
        }
        if (fileSet != null && fileSet.getCustf() != null) {
            register(fileSet.getCustf().buffer());
        }
        if (fileSet != null && fileSet.getProdf() != null) {
            register(fileSet.getProdf().buffer());
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

    public String getFsts() {
        return getString("FSTS");
    }

    public void setFsts(String value) {
        setString("FSTS", value);
    }

    public long getGtNet() {
        return getLong("GT-NET");
    }

    public void setGtNet(long value) {
        setLong("GT-NET", value);
    }

    public long getGtTax() {
        return getLong("GT-TAX");
    }

    public void setGtTax(long value) {
        setLong("GT-TAX", value);
    }

    public long getGtTot() {
        return getLong("GT-TOT");
    }

    public void setGtTot(long value) {
        setLong("GT-TOT", value);
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

    public BigDecimal getIdAmount() {
        return getDecimal("ID-AMOUNT");
    }

    public void setIdAmount(BigDecimal value) {
        setDecimal("ID-AMOUNT", value);
    }

    public long getIdAmtE() {
        return getLong("ID-AMT-E");
    }

    public void setIdAmtE(long value) {
        setLong("ID-AMT-E", value);
    }

    public int getIdLine() {
        return getInt("ID-LINE");
    }

    public void setIdLine(int value) {
        setInt("ID-LINE", value);
    }

    public int getIdLineE() {
        return getInt("ID-LINE-E");
    }

    public void setIdLineE(int value) {
        setInt("ID-LINE-E", value);
    }

    public long getIdNo() {
        return getLong("ID-NO");
    }

    public void setIdNo(long value) {
        setLong("ID-NO", value);
    }

    public String getIdPname() {
        return getString("ID-PNAME");
    }

    public void setIdPname(String value) {
        setString("ID-PNAME", value);
    }

    public BigDecimal getIdPriceE() {
        return getDecimal("ID-PRICE-E");
    }

    public void setIdPriceE(BigDecimal value) {
        setDecimal("ID-PRICE-E", value);
    }

    public int getIdProd() {
        return getInt("ID-PROD");
    }

    public void setIdProd(int value) {
        setInt("ID-PROD", value);
    }

    public int getIdProdE() {
        return getInt("ID-PROD-E");
    }

    public void setIdProdE(int value) {
        setInt("ID-PROD-E", value);
    }

    public BigDecimal getIdQty() {
        return getDecimal("ID-QTY");
    }

    public void setIdQty(BigDecimal value) {
        setDecimal("ID-QTY", value);
    }

    public long getIdQtyE() {
        return getLong("ID-QTY-E");
    }

    public void setIdQtyE(long value) {
        setLong("ID-QTY-E", value);
    }

    public BigDecimal getIdUnitPrice() {
        return getDecimal("ID-UNIT-PRICE");
    }

    public void setIdUnitPrice(BigDecimal value) {
        setDecimal("ID-UNIT-PRICE", value);
    }

    public BigDecimal getIhAmount() {
        return getDecimal("IH-AMOUNT");
    }

    public void setIhAmount(BigDecimal value) {
        setDecimal("IH-AMOUNT", value);
    }

    public String getIhCname() {
        return getString("IH-CNAME");
    }

    public void setIhCname(String value) {
        setString("IH-CNAME", value);
    }

    public int getIhCust() {
        return getInt("IH-CUST");
    }

    public void setIhCust(int value) {
        setInt("IH-CUST", value);
    }

    public int getIhCustE() {
        return getInt("IH-CUST-E");
    }

    public void setIhCustE(int value) {
        setInt("IH-CUST-E", value);
    }

    public int getIhDate() {
        return getInt("IH-DATE");
    }

    public void setIhDate(int value) {
        setInt("IH-DATE", value);
    }

    public int getIhDateE() {
        return getInt("IH-DATE-E");
    }

    public void setIhDateE(int value) {
        setInt("IH-DATE-E", value);
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

    public String getIhKindE() {
        return getString("IH-KIND-E");
    }

    public void setIhKindE(String value) {
        setString("IH-KIND-E", value);
    }

    public long getIhNo() {
        return getLong("IH-NO");
    }

    public void setIhNo(long value) {
        setLong("IH-NO", value);
    }

    public long getIhNoE() {
        return getLong("IH-NO-E");
    }

    public void setIhNoE(long value) {
        setLong("IH-NO-E", value);
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

    public BigDecimal getIhTotal() {
        return getDecimal("IH-TOTAL");
    }

    public void setIhTotal(BigDecimal value) {
        setDecimal("IH-TOTAL", value);
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

    public String getRdGrand() {
        return groupToString("RD-GRAND");
    }

    public void setRdGrand(String value) {
        setGroup("RD-GRAND", value);
    }

    public String getRdInvd() {
        return groupToString("RD-INVD");
    }

    public void setRdInvd(String value) {
        setGroup("RD-INVD", value);
    }

    public String getRdInvh() {
        return groupToString("RD-INVH");
    }

    public void setRdInvh(String value) {
        setGroup("RD-INVH", value);
    }

    public String getRdSub() {
        return groupToString("RD-SUB");
    }

    public void setRdSub(String value) {
        setGroup("RD-SUB", value);
    }

    public String getRepRec() {
        return getString("REP-REC");
    }

    public void setRepRec(String value) {
        setString("REP-REC", value);
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

    public long getSubNet() {
        return getLong("SUB-NET");
    }

    public void setSubNet(long value) {
        setLong("SUB-NET", value);
    }

    public long getSubTax() {
        return getLong("SUB-TAX");
    }

    public void setSubTax(long value) {
        setLong("SUB-TAX", value);
    }

    public long getSubTot() {
        return getLong("SUB-TOT");
    }

    public void setSubTot(long value) {
        setLong("SUB-TOT", value);
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

    public String getWkCustName() {
        return getString("WK-CUST-NAME");
    }

    public void setWkCustName(String value) {
        setString("WK-CUST-NAME", value);
    }

    public int getWkDateFrom() {
        return getInt("WK-DATE-FROM");
    }

    public void setWkDateFrom(int value) {
        setInt("WK-DATE-FROM", value);
    }

    public int getWkDateTo() {
        return getInt("WK-DATE-TO");
    }

    public void setWkDateTo(int value) {
        setInt("WK-DATE-TO", value);
    }

    public int getWkDtlEof() {
        return getInt("WK-DTL-EOF");
    }

    public void setWkDtlEof(int value) {
        setInt("WK-DTL-EOF", value);
    }

    public BigDecimal getWkGNet() {
        return getDecimal("WK-G-NET");
    }

    public void setWkGNet(BigDecimal value) {
        setDecimal("WK-G-NET", value);
    }

    public BigDecimal getWkGTax() {
        return getDecimal("WK-G-TAX");
    }

    public void setWkGTax(BigDecimal value) {
        setDecimal("WK-G-TAX", value);
    }

    public BigDecimal getWkGTot() {
        return getDecimal("WK-G-TOT");
    }

    public void setWkGTot(BigDecimal value) {
        setDecimal("WK-G-TOT", value);
    }

    public String getWkInFrom() {
        return getString("WK-IN-FROM");
    }

    public void setWkInFrom(String value) {
        setString("WK-IN-FROM", value);
    }

    public String getWkInTo() {
        return getString("WK-IN-TO");
    }

    public void setWkInTo(String value) {
        setString("WK-IN-TO", value);
    }

    public int getWkInvCnt() {
        return getInt("WK-INV-CNT");
    }

    public void setWkInvCnt(int value) {
        setInt("WK-INV-CNT", value);
    }

    public String getWkKindTxt() {
        return getString("WK-KIND-TXT");
    }

    public void setWkKindTxt(String value) {
        setString("WK-KIND-TXT", value);
    }

    public int getWkLinCnt() {
        return getInt("WK-LIN-CNT");
    }

    public void setWkLinCnt(int value) {
        setInt("WK-LIN-CNT", value);
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

    /* ── Byte-level copy wrappers (preserves COMP-3/BINARY raw bytes) ── */
    public void copyRepRecFromRdGrand() {
        copyBytes("REP-REC", "RD-GRAND");
    }

    public void copyRepRecFromRdInvd() {
        copyBytes("REP-REC", "RD-INVD");
    }

    public void copyRepRecFromRdInvh() {
        copyBytes("REP-REC", "RD-INVH");
    }

    public void copyRepRecFromRdSub() {
        copyBytes("REP-REC", "RD-SUB");
    }

    public void copyRepRecFromRptH1() {
        copyBytes("REP-REC", "RPT-H1");
    }

    public void copyRepRecFromRptH2() {
        copyBytes("REP-REC", "RPT-H2");
    }
}

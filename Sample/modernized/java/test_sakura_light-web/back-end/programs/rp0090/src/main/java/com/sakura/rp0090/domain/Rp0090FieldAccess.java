package com.sakura.rp0090.domain;

import com.sakura.rp0090.runtime.Rp0090Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for RP0090. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Rp0090FieldAccess extends RuntimeFieldAccess {

    public Rp0090FieldAccess(WorkingStorage ws, Rp0090Datasets fileSet) {
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

    public long getGtAmt() {
        return getLong("GT-AMT");
    }

    public void setGtAmt(long value) {
        setLong("GT-AMT", value);
    }

    public int getGtQty() {
        return getInt("GT-QTY");
    }

    public void setGtQty(int value) {
        setInt("GT-QTY", value);
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

    public long getOdAmtE() {
        return getLong("OD-AMT-E");
    }

    public void setOdAmtE(long value) {
        setLong("OD-AMT-E", value);
    }

    public int getOdLine() {
        return getInt("OD-LINE");
    }

    public void setOdLine(int value) {
        setInt("OD-LINE", value);
    }

    public int getOdLineE() {
        return getInt("OD-LINE-E");
    }

    public void setOdLineE(int value) {
        setInt("OD-LINE-E", value);
    }

    public long getOdNo() {
        return getLong("OD-NO");
    }

    public void setOdNo(long value) {
        setLong("OD-NO", value);
    }

    public int getOdOrdE() {
        return getInt("OD-ORD-E");
    }

    public void setOdOrdE(int value) {
        setInt("OD-ORD-E", value);
    }

    public int getOdOutE() {
        return getInt("OD-OUT-E");
    }

    public void setOdOutE(int value) {
        setInt("OD-OUT-E", value);
    }

    public String getOdPname() {
        return getString("OD-PNAME");
    }

    public void setOdPname(String value) {
        setString("OD-PNAME", value);
    }

    public BigDecimal getOdPriceE() {
        return getDecimal("OD-PRICE-E");
    }

    public void setOdPriceE(BigDecimal value) {
        setDecimal("OD-PRICE-E", value);
    }

    public int getOdProd() {
        return getInt("OD-PROD");
    }

    public void setOdProd(int value) {
        setInt("OD-PROD", value);
    }

    public int getOdProdE() {
        return getInt("OD-PROD-E");
    }

    public void setOdProdE(int value) {
        setInt("OD-PROD-E", value);
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

    public int getOdShpE() {
        return getInt("OD-SHP-E");
    }

    public void setOdShpE(int value) {
        setInt("OD-SHP-E", value);
    }

    public BigDecimal getOdUnitPrice() {
        return getDecimal("OD-UNIT-PRICE");
    }

    public void setOdUnitPrice(BigDecimal value) {
        setDecimal("OD-UNIT-PRICE", value);
    }

    public String getOhCname() {
        return getString("OH-CNAME");
    }

    public void setOhCname(String value) {
        setString("OH-CNAME", value);
    }

    public int getOhCust() {
        return getInt("OH-CUST");
    }

    public void setOhCust(int value) {
        setInt("OH-CUST", value);
    }

    public int getOhCustE() {
        return getInt("OH-CUST-E");
    }

    public void setOhCustE(int value) {
        setInt("OH-CUST-E", value);
    }

    public int getOhDate() {
        return getInt("OH-DATE");
    }

    public void setOhDate(int value) {
        setInt("OH-DATE", value);
    }

    public int getOhDateE() {
        return getInt("OH-DATE-E");
    }

    public void setOhDateE(int value) {
        setInt("OH-DATE-E", value);
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

    public int getOhDueE() {
        return getInt("OH-DUE-E");
    }

    public void setOhDueE(int value) {
        setInt("OH-DUE-E", value);
    }

    public long getOhNo() {
        return getLong("OH-NO");
    }

    public void setOhNo(long value) {
        setLong("OH-NO", value);
    }

    public long getOhNoE() {
        return getLong("OH-NO-E");
    }

    public void setOhNoE(long value) {
        setLong("OH-NO-E", value);
    }

    public int getOhStE() {
        return getInt("OH-ST-E");
    }

    public void setOhStE(int value) {
        setInt("OH-ST-E", value);
    }

    public int getOhStatus() {
        return getInt("OH-STATUS");
    }

    public void setOhStatus(int value) {
        setInt("OH-STATUS", value);
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

    public String getRdDhead() {
        return groupToString("RD-DHEAD");
    }

    public void setRdDhead(String value) {
        setGroup("RD-DHEAD", value);
    }

    public String getRdGrand() {
        return groupToString("RD-GRAND");
    }

    public void setRdGrand(String value) {
        setGroup("RD-GRAND", value);
    }

    public String getRdOrdd() {
        return groupToString("RD-ORDD");
    }

    public void setRdOrdd(String value) {
        setGroup("RD-ORDD", value);
    }

    public String getRdOrdh() {
        return groupToString("RD-ORDH");
    }

    public void setRdOrdh(String value) {
        setGroup("RD-ORDH", value);
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

    public long getSubAmt() {
        return getLong("SUB-AMT");
    }

    public void setSubAmt(long value) {
        setLong("SUB-AMT", value);
    }

    public int getSubQty() {
        return getInt("SUB-QTY");
    }

    public void setSubQty(int value) {
        setInt("SUB-QTY", value);
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

    public int getWkDtlEof() {
        return getInt("WK-DTL-EOF");
    }

    public void setWkDtlEof(int value) {
        setInt("WK-DTL-EOF", value);
    }

    public BigDecimal getWkGAmt() {
        return getDecimal("WK-G-AMT");
    }

    public void setWkGAmt(BigDecimal value) {
        setDecimal("WK-G-AMT", value);
    }

    public int getWkGLin() {
        return getInt("WK-G-LIN");
    }

    public void setWkGLin(int value) {
        setInt("WK-G-LIN", value);
    }

    public int getWkGOrd() {
        return getInt("WK-G-ORD");
    }

    public void setWkGOrd(int value) {
        setInt("WK-G-ORD", value);
    }

    public BigDecimal getWkGQty() {
        return getDecimal("WK-G-QTY");
    }

    public void setWkGQty(BigDecimal value) {
        setDecimal("WK-G-QTY", value);
    }

    public int getWkHdrPrinted() {
        return getInt("WK-HDR-PRINTED");
    }

    public void setWkHdrPrinted(int value) {
        setInt("WK-HDR-PRINTED", value);
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

    public BigDecimal getWkOrdAmt() {
        return getDecimal("WK-ORD-AMT");
    }

    public void setWkOrdAmt(BigDecimal value) {
        setDecimal("WK-ORD-AMT", value);
    }

    public BigDecimal getWkOrdQty() {
        return getDecimal("WK-ORD-QTY");
    }

    public void setWkOrdQty(BigDecimal value) {
        setDecimal("WK-ORD-QTY", value);
    }

    public long getWkOut() {
        return getLong("WK-OUT");
    }

    public void setWkOut(long value) {
        setLong("WK-OUT", value);
    }

    public BigDecimal getWkOutAmt() {
        return getDecimal("WK-OUT-AMT");
    }

    public void setWkOutAmt(BigDecimal value) {
        setDecimal("WK-OUT-AMT", value);
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
    public void copyRepRecFromRdDhead() {
        copyBytes("REP-REC", "RD-DHEAD");
    }

    public void copyRepRecFromRdGrand() {
        copyBytes("REP-REC", "RD-GRAND");
    }

    public void copyRepRecFromRdOrdd() {
        copyBytes("REP-REC", "RD-ORDD");
    }

    public void copyRepRecFromRdOrdh() {
        copyBytes("REP-REC", "RD-ORDH");
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

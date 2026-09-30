package com.sakura.oe0050.domain;

import com.sakura.oe0050.runtime.Oe0050Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for OE0050. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Oe0050FieldAccess extends RuntimeFieldAccess {

    public Oe0050FieldAccess(WorkingStorage ws, Oe0050Datasets fileSet) {
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

    public long getDAmt() {
        return getLong("D-AMT");
    }

    public void setDAmt(long value) {
        setLong("D-AMT", value);
    }

    public int getDLine() {
        return getInt("D-LINE");
    }

    public void setDLine(int value) {
        setInt("D-LINE", value);
    }

    public String getDName() {
        return getString("D-NAME");
    }

    public void setDName(String value) {
        setString("D-NAME", value);
    }

    public long getDOut() {
        return getLong("D-OUT");
    }

    public void setDOut(long value) {
        setLong("D-OUT", value);
    }

    public BigDecimal getDPrice() {
        return getDecimal("D-PRICE");
    }

    public void setDPrice(BigDecimal value) {
        setDecimal("D-PRICE", value);
    }

    public int getDProd() {
        return getInt("D-PROD");
    }

    public void setDProd(int value) {
        setInt("D-PROD", value);
    }

    public long getDQty() {
        return getLong("D-QTY");
    }

    public void setDQty(long value) {
        setLong("D-QTY", value);
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

    public int getGtOrd() {
        return getInt("GT-ORD");
    }

    public void setGtOrd(int value) {
        setInt("GT-ORD", value);
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

    public int getO1Date() {
        return getInt("O1-DATE");
    }

    public void setO1Date(int value) {
        setInt("O1-DATE", value);
    }

    public int getO1Due() {
        return getInt("O1-DUE");
    }

    public void setO1Due(int value) {
        setInt("O1-DUE", value);
    }

    public long getO1No() {
        return getLong("O1-NO");
    }

    public void setO1No(long value) {
        setLong("O1-NO", value);
    }

    public String getO1Stat() {
        return getString("O1-STAT");
    }

    public void setO1Stat(String value) {
        setString("O1-STAT", value);
    }

    public String getO2Cname() {
        return getString("O2-CNAME");
    }

    public void setO2Cname(String value) {
        setString("O2-CNAME", value);
    }

    public int getO2Cust() {
        return getInt("O2-CUST");
    }

    public void setO2Cust(int value) {
        setInt("O2-CUST", value);
    }

    public String getO2Po() {
        return getString("O2-PO");
    }

    public void setO2Po(String value) {
        setString("O2-PO", value);
    }

    public int getO2Staff() {
        return getInt("O2-STAFF");
    }

    public void setO2Staff(int value) {
        setInt("O2-STAFF", value);
    }

    public int getO2Whse() {
        return getInt("O2-WHSE");
    }

    public void setO2Whse(int value) {
        setInt("O2-WHSE", value);
    }

    public BigDecimal getOdAmount() {
        return getDecimal("OD-AMOUNT");
    }

    public void setOdAmount(BigDecimal value) {
        setDecimal("OD-AMOUNT", value);
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

    public BigDecimal getOdUnitPrice() {
        return getDecimal("OD-UNIT-PRICE");
    }

    public void setOdUnitPrice(BigDecimal value) {
        setDecimal("OD-UNIT-PRICE", value);
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

    public int getOhWhse() {
        return getInt("OH-WHSE");
    }

    public void setOhWhse(int value) {
        setInt("OH-WHSE", value);
    }

    public long getOtNet() {
        return getLong("OT-NET");
    }

    public void setOtNet(long value) {
        setLong("OT-NET", value);
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

    public String getRdColh() {
        return groupToString("RD-COLH");
    }

    public void setRdColh(String value) {
        setGroup("RD-COLH", value);
    }

    public String getRdDtl() {
        return groupToString("RD-DTL");
    }

    public void setRdDtl(String value) {
        setGroup("RD-DTL", value);
    }

    public String getRdGrand() {
        return groupToString("RD-GRAND");
    }

    public void setRdGrand(String value) {
        setGroup("RD-GRAND", value);
    }

    public String getRdOrd1() {
        return groupToString("RD-ORD1");
    }

    public void setRdOrd1(String value) {
        setGroup("RD-ORD1", value);
    }

    public String getRdOrd2() {
        return groupToString("RD-ORD2");
    }

    public void setRdOrd2(String value) {
        setGroup("RD-ORD2", value);
    }

    public String getRdOtot() {
        return groupToString("RD-OTOT");
    }

    public void setRdOtot(String value) {
        setGroup("RD-OTOT", value);
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

    public BigDecimal getWkGNet() {
        return getDecimal("WK-G-NET");
    }

    public void setWkGNet(BigDecimal value) {
        setDecimal("WK-G-NET", value);
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

    public int getWkOrdCnt() {
        return getInt("WK-ORD-CNT");
    }

    public void setWkOrdCnt(int value) {
        setInt("WK-ORD-CNT", value);
    }

    public long getWkOrdFrom() {
        return getLong("WK-ORD-FROM");
    }

    public void setWkOrdFrom(long value) {
        setLong("WK-ORD-FROM", value);
    }

    public BigDecimal getWkOrdNet() {
        return getDecimal("WK-ORD-NET");
    }

    public void setWkOrdNet(BigDecimal value) {
        setDecimal("WK-ORD-NET", value);
    }

    public long getWkOrdTo() {
        return getLong("WK-ORD-TO");
    }

    public void setWkOrdTo(long value) {
        setLong("WK-ORD-TO", value);
    }

    public int getWkOutstand() {
        return getInt("WK-OUTSTAND");
    }

    public void setWkOutstand(int value) {
        setInt("WK-OUTSTAND", value);
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

    public String getWkTitle() {
        return getString("WK-TITLE");
    }

    public void setWkTitle(String value) {
        setString("WK-TITLE", value);
    }

    /* ── Byte-level copy wrappers (preserves COMP-3/BINARY raw bytes) ── */
    public void copyRepRecFromRdColh() {
        copyBytes("REP-REC", "RD-COLH");
    }

    public void copyRepRecFromRdDtl() {
        copyBytes("REP-REC", "RD-DTL");
    }

    public void copyRepRecFromRdGrand() {
        copyBytes("REP-REC", "RD-GRAND");
    }

    public void copyRepRecFromRdOrd1() {
        copyBytes("REP-REC", "RD-ORD1");
    }

    public void copyRepRecFromRdOrd2() {
        copyBytes("REP-REC", "RD-ORD2");
    }

    public void copyRepRecFromRdOtot() {
        copyBytes("REP-REC", "RD-OTOT");
    }

    public void copyRepRecFromRptH1() {
        copyBytes("REP-REC", "RPT-H1");
    }

    public void copyRepRecFromRptH2() {
        copyBytes("REP-REC", "RPT-H2");
    }
}

package com.sakura.rp0050.domain;

import com.sakura.rp0050.runtime.Rp0050Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for RP0050. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Rp0050FieldAccess extends RuntimeFieldAccess {

    public Rp0050FieldAccess(WorkingStorage ws, Rp0050Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getInvhf() != null) {
            register(fileSet.getInvhf().buffer());
        }
        if (fileSet != null && fileSet.getInvdf() != null) {
            register(fileSet.getInvdf().buffer());
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

    public String getFsts() {
        return getString("FSTS");
    }

    public void setFsts(String value) {
        setString("FSTS", value);
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

    public long getIhNo() {
        return getLong("IH-NO");
    }

    public void setIhNo(long value) {
        setLong("IH-NO", value);
    }

    public int getIhStatus() {
        return getInt("IH-STATUS");
    }

    public void setIhStatus(int value) {
        setInt("IH-STATUS", value);
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

    public String getRdLine() {
        return groupToString("RD-LINE");
    }

    public void setRdLine(String value) {
        setGroup("RD-LINE", value);
    }

    public String getRepRec() {
        return getString("REP-REC");
    }

    public void setRepRec(String value) {
        setString("REP-REC", value);
    }

    public String getRhLine() {
        return groupToString("RH-LINE");
    }

    public void setRhLine(String value) {
        setGroup("RH-LINE", value);
    }

    public long getRpAmt() {
        return getLong("RP-AMT");
    }

    public void setRpAmt(long value) {
        setLong("RP-AMT", value);
    }

    public int getRpCode() {
        return getInt("RP-CODE");
    }

    public void setRpCode(int value) {
        setInt("RP-CODE", value);
    }

    public String getRpName() {
        return getString("RP-NAME");
    }

    public void setRpName(String value) {
        setString("RP-NAME", value);
    }

    public long getRpQty() {
        return getLong("RP-QTY");
    }

    public void setRpQty(long value) {
        setLong("RP-QTY", value);
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

    public long getRtAmt() {
        return getLong("RT-AMT");
    }

    public void setRtAmt(long value) {
        setLong("RT-AMT", value);
    }

    public String getRtLine() {
        return groupToString("RT-LINE");
    }

    public void setRtLine(String value) {
        setGroup("RT-LINE", value);
    }

    public long getRtQty() {
        return getLong("RT-QTY");
    }

    public void setRtQty(long value) {
        setLong("RT-QTY", value);
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

    public int getWkFound() {
        return getInt("WK-FOUND");
    }

    public void setWkFound(int value) {
        setInt("WK-FOUND", value);
    }

    public BigDecimal getWkGAmt() {
        return getDecimal("WK-G-AMT");
    }

    public void setWkGAmt(BigDecimal value) {
        setDecimal("WK-G-AMT", value);
    }

    public BigDecimal getWkGQty() {
        return getDecimal("WK-G-QTY");
    }

    public void setWkGQty(BigDecimal value) {
        setDecimal("WK-G-QTY", value);
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

    public BigDecimal getWkPAmt(int index) {
        return getDecimal("WK-P-AMT", index);
    }

    public void setWkPAmt(int index, BigDecimal value) {
        setDecimal("WK-P-AMT", value, index);
    }

    public int getWkPCode(int index) {
        return getInt("WK-P-CODE", index);
    }

    public void setWkPCode(int index, int value) {
        setInt("WK-P-CODE", value, index);
    }

    public BigDecimal getWkPQty(int index) {
        return getDecimal("WK-P-QTY", index);
    }

    public void setWkPQty(int index, BigDecimal value) {
        setDecimal("WK-P-QTY", value, index);
    }

    public int getWkPage() {
        return getInt("WK-PAGE");
    }

    public void setWkPage(int value) {
        setInt("WK-PAGE", value);
    }

    public String getWkPent(int index) {
        return groupToString("WK-PENT", index);
    }

    public void setWkPent(int index, String value) {
        setGroup("WK-PENT", value, index);
    }

    public String getWkPent() {
        return groupToString("WK-PENT");
    }

    public void setWkPent(String value) {
        setGroup("WK-PENT", value);
    }

    public int getWkPn() {
        return getInt("WK-PN");
    }

    public void setWkPn(int value) {
        setInt("WK-PN", value);
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

    public int getWkSi() {
        return getInt("WK-SI");
    }

    public void setWkSi(int value) {
        setInt("WK-SI", value);
    }

    public int getWkSj() {
        return getInt("WK-SJ");
    }

    public void setWkSj(int value) {
        setInt("WK-SJ", value);
    }

    public int getWkSmin() {
        return getInt("WK-SMIN");
    }

    public void setWkSmin(int value) {
        setInt("WK-SMIN", value);
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

    public String getWkTmpEnt() {
        return groupToString("WK-TMP-ENT");
    }

    public void setWkTmpEnt(String value) {
        setGroup("WK-TMP-ENT", value);
    }

    /* ── Byte-level copy wrappers (preserves COMP-3/BINARY raw bytes) ── */
    public void copyRepRecFromRdLine() {
        copyBytes("REP-REC", "RD-LINE");
    }

    public void copyRepRecFromRhLine() {
        copyBytes("REP-REC", "RH-LINE");
    }

    public void copyRepRecFromRptH1() {
        copyBytes("REP-REC", "RPT-H1");
    }

    public void copyRepRecFromRptH2() {
        copyBytes("REP-REC", "RPT-H2");
    }

    public void copyRepRecFromRtLine() {
        copyBytes("REP-REC", "RT-LINE");
    }
}

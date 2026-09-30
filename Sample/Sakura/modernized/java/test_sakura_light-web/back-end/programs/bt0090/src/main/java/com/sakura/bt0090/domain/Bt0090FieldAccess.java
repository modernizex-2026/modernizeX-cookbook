package com.sakura.bt0090.domain;

import com.sakura.bt0090.runtime.Bt0090Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for BT0090. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Bt0090FieldAccess extends RuntimeFieldAccess {

    public Bt0090FieldAccess(WorkingStorage ws, Bt0090Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getOrddf() != null) {
            register(fileSet.getOrddf().buffer());
        }
        if (fileSet != null && fileSet.getOrdhf() != null) {
            register(fileSet.getOrdhf().buffer());
        }
        if (fileSet != null && fileSet.getInvdf() != null) {
            register(fileSet.getInvdf().buffer());
        }
        if (fileSet != null && fileSet.getInvhf() != null) {
            register(fileSet.getInvhf().buffer());
        }
        if (fileSet != null && fileSet.getStokf() != null) {
            register(fileSet.getStokf().buffer());
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

    public long getExKey1() {
        return getLong("EX-KEY1");
    }

    public void setExKey1(long value) {
        setLong("EX-KEY1", value);
    }

    public String getExKey1lbl() {
        return getString("EX-KEY1LBL");
    }

    public void setExKey1lbl(String value) {
        setString("EX-KEY1LBL", value);
    }

    public int getExKey2() {
        return getInt("EX-KEY2");
    }

    public void setExKey2(int value) {
        setInt("EX-KEY2", value);
    }

    public String getExKey2lbl() {
        return getString("EX-KEY2LBL");
    }

    public void setExKey2lbl(String value) {
        setString("EX-KEY2LBL", value);
    }

    public String getExMsg() {
        return getString("EX-MSG");
    }

    public void setExMsg(String value) {
        setString("EX-MSG", value);
    }

    public String getExTag() {
        return getString("EX-TAG");
    }

    public void setExTag(String value) {
        setString("EX-TAG", value);
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

    public long getIhNo() {
        return getLong("IH-NO");
    }

    public void setIhNo(long value) {
        setLong("IH-NO", value);
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

    public long getOhNo() {
        return getLong("OH-NO");
    }

    public void setOhNo(long value) {
        setLong("OH-NO", value);
    }

    public int getPrCode() {
        return getInt("PR-CODE");
    }

    public void setPrCode(int value) {
        setInt("PR-CODE", value);
    }

    public String getRdCnt() {
        return groupToString("RD-CNT");
    }

    public void setRdCnt(String value) {
        setGroup("RD-CNT", value);
    }

    public String getRdEx() {
        return groupToString("RD-EX");
    }

    public void setRdEx(String value) {
        setGroup("RD-EX", value);
    }

    public String getRdSect() {
        return groupToString("RD-SECT");
    }

    public void setRdSect(String value) {
        setGroup("RD-SECT", value);
    }

    public String getRdVerd() {
        return groupToString("RD-VERD");
    }

    public void setRdVerd(String value) {
        setGroup("RD-VERD", value);
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

    public String getSectTxt() {
        return getString("SECT-TXT");
    }

    public void setSectTxt(String value) {
        setString("SECT-TXT", value);
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

    public String getSmLbl() {
        return getString("SM-LBL");
    }

    public void setSmLbl(String value) {
        setString("SM-LBL", value);
    }

    public int getSmVal() {
        return getInt("SM-VAL");
    }

    public void setSmVal(int value) {
        setInt("SM-VAL", value);
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

    public String getVerdTxt() {
        return getString("VERD-TXT");
    }

    public void setVerdTxt(String value) {
        setString("VERD-TXT", value);
    }

    public int getWkAbortFlg() {
        return getInt("WK-ABORT-FLG");
    }

    public void setWkAbortFlg(int value) {
        setInt("WK-ABORT-FLG", value);
    }

    public String getWkCompany() {
        return getString("WK-COMPANY");
    }

    public void setWkCompany(String value) {
        setString("WK-COMPANY", value);
    }

    public String getWkConfirm() {
        return getString("WK-CONFIRM");
    }

    public void setWkConfirm(String value) {
        setString("WK-CONFIRM", value);
    }

    public int getWkECnt() {
        return getInt("WK-E-CNT");
    }

    public void setWkECnt(int value) {
        setInt("WK-E-CNT", value);
    }

    public long getWkEn() {
        return getLong("WK-EN");
    }

    public void setWkEn(long value) {
        setLong("WK-EN", value);
    }

    public int getWkEof() {
        return getInt("WK-EOF");
    }

    public void setWkEof(int value) {
        setInt("WK-EOF", value);
    }

    public int getWkExNeg() {
        return getInt("WK-EX-NEG");
    }

    public void setWkExNeg(int value) {
        setInt("WK-EX-NEG", value);
    }

    public int getWkExOrphId() {
        return getInt("WK-EX-ORPH-ID");
    }

    public void setWkExOrphId(int value) {
        setInt("WK-EX-ORPH-ID", value);
    }

    public int getWkExOrphOd() {
        return getInt("WK-EX-ORPH-OD");
    }

    public void setWkExOrphOd(int value) {
        setInt("WK-EX-ORPH-OD", value);
    }

    public int getWkExProd() {
        return getInt("WK-EX-PROD");
    }

    public void setWkExProd(int value) {
        setInt("WK-EX-PROD", value);
    }

    public int getWkExTotal() {
        return getInt("WK-EX-TOTAL");
    }

    public void setWkExTotal(int value) {
        setInt("WK-EX-TOTAL", value);
    }

    public int getWkInvdCnt() {
        return getInt("WK-INVD-CNT");
    }

    public void setWkInvdCnt(int value) {
        setInt("WK-INVD-CNT", value);
    }

    public int getWkLine() {
        return getInt("WK-LINE");
    }

    public void setWkLine(int value) {
        setInt("WK-LINE", value);
    }

    public String getWkMsgtxt() {
        return getString("WK-MSGTXT");
    }

    public void setWkMsgtxt(String value) {
        setString("WK-MSGTXT", value);
    }

    public int getWkOrddCnt() {
        return getInt("WK-ORDD-CNT");
    }

    public void setWkOrddCnt(int value) {
        setInt("WK-ORDD-CNT", value);
    }

    public int getWkPage() {
        return getInt("WK-PAGE");
    }

    public void setWkPage(int value) {
        setInt("WK-PAGE", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
    }

    public int getWkStokCnt() {
        return getInt("WK-STOK-CNT");
    }

    public void setWkStokCnt(int value) {
        setInt("WK-STOK-CNT", value);
    }

    public int getWkSysdate() {
        return getInt("WK-SYSDATE");
    }

    public void setWkSysdate(int value) {
        setInt("WK-SYSDATE", value);
    }

    /* ── Byte-level copy wrappers (preserves COMP-3/BINARY raw bytes) ── */
    public void copyRepRecFromRdCnt() {
        copyBytes("REP-REC", "RD-CNT");
    }

    public void copyRepRecFromRdEx() {
        copyBytes("REP-REC", "RD-EX");
    }

    public void copyRepRecFromRdSect() {
        copyBytes("REP-REC", "RD-SECT");
    }

    public void copyRepRecFromRdVerd() {
        copyBytes("REP-REC", "RD-VERD");
    }

    public void copyRepRecFromRptH1() {
        copyBytes("REP-REC", "RPT-H1");
    }

    public void copyRepRecFromRptH2() {
        copyBytes("REP-REC", "RPT-H2");
    }
}

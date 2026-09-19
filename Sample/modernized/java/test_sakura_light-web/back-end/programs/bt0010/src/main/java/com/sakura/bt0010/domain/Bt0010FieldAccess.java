package com.sakura.bt0010.domain;

import com.sakura.bt0010.runtime.Bt0010Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for BT0010. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Bt0010FieldAccess extends RuntimeFieldAccess {

    public Bt0010FieldAccess(WorkingStorage ws, Bt0010Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getInvhf() != null) {
            register(fileSet.getInvhf().buffer());
        }
        if (fileSet != null && fileSet.getSyscf() != null) {
            register(fileSet.getSyscf().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getCompletionCode() {
        return getInt("COMPLETION-CODE");
    }

    public void setCompletionCode(int value) {
        setInt("COMPLETION-CODE", value);
    }

    public int getEofFlg() {
        return getInt("EOF-FLG");
    }

    public void setEofFlg(int value) {
        setInt("EOF-FLG", value);
    }

    public String getFsts() {
        return getString("FSTS");
    }

    public void setFsts(String value) {
        setString("FSTS", value);
    }

    public BigDecimal getIhAmount() {
        return getDecimal("IH-AMOUNT");
    }

    public void setIhAmount(BigDecimal value) {
        setDecimal("IH-AMOUNT", value);
    }

    public BigDecimal getIhCostTotal() {
        return getDecimal("IH-COST-TOTAL");
    }

    public void setIhCostTotal(BigDecimal value) {
        setDecimal("IH-COST-TOTAL", value);
    }

    public int getIhDate() {
        return getInt("IH-DATE");
    }

    public void setIhDate(int value) {
        setInt("IH-DATE", value);
    }

    public int getIhKind() {
        return getInt("IH-KIND");
    }

    public void setIhKind(int value) {
        setInt("IH-KIND", value);
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

    public int getSyLastDayClose() {
        return getInt("SY-LAST-DAY-CLOSE");
    }

    public void setSyLastDayClose(int value) {
        setInt("SY-LAST-DAY-CLOSE", value);
    }

    public int getWkAbortFlg() {
        return getInt("WK-ABORT-FLG");
    }

    public void setWkAbortFlg(int value) {
        setInt("WK-ABORT-FLG", value);
    }

    public long getWkAvg() {
        return getLong("WK-AVG");
    }

    public void setWkAvg(long value) {
        setLong("WK-AVG", value);
    }

    public String getWkConfirm() {
        return getString("WK-CONFIRM");
    }

    public void setWkConfirm(String value) {
        setString("WK-CONFIRM", value);
    }

    public long getWkCostTot() {
        return getLong("WK-COST-TOT");
    }

    public void setWkCostTot(long value) {
        setLong("WK-COST-TOT", value);
    }

    public int getWkDfltDate() {
        return getInt("WK-DFLT-DATE");
    }

    public void setWkDfltDate(int value) {
        setInt("WK-DFLT-DATE", value);
    }

    public long getWkEAmt() {
        return getLong("WK-E-AMT");
    }

    public void setWkEAmt(long value) {
        setLong("WK-E-AMT", value);
    }

    public int getWkECnt() {
        return getInt("WK-E-CNT");
    }

    public void setWkECnt(int value) {
        setInt("WK-E-CNT", value);
    }

    public int getWkEDate() {
        return getInt("WK-E-DATE");
    }

    public void setWkEDate(int value) {
        setInt("WK-E-DATE", value);
    }

    public long getWkENo() {
        return getLong("WK-E-NO");
    }

    public void setWkENo(long value) {
        setLong("WK-E-NO", value);
    }

    public long getWkGrossTot() {
        return getLong("WK-GROSS-TOT");
    }

    public void setWkGrossTot(long value) {
        setLong("WK-GROSS-TOT", value);
    }

    public String getWkInLine() {
        return getString("WK-IN-LINE");
    }

    public void setWkInLine(String value) {
        setString("WK-IN-LINE", value);
    }

    public int getWkPostCnt() {
        return getInt("WK-POST-CNT");
    }

    public void setWkPostCnt(int value) {
        setInt("WK-POST-CNT", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
    }

    public int getWkReadCnt() {
        return getInt("WK-READ-CNT");
    }

    public void setWkReadCnt(int value) {
        setInt("WK-READ-CNT", value);
    }

    public int getWkRtnCnt() {
        return getInt("WK-RTN-CNT");
    }

    public void setWkRtnCnt(int value) {
        setInt("WK-RTN-CNT", value);
    }

    public long getWkSaleAmt() {
        return getLong("WK-SALE-AMT");
    }

    public void setWkSaleAmt(long value) {
        setLong("WK-SALE-AMT", value);
    }

    public int getWkSaleCnt() {
        return getInt("WK-SALE-CNT");
    }

    public void setWkSaleCnt(int value) {
        setInt("WK-SALE-CNT", value);
    }

    public int getWkSkipCnt() {
        return getInt("WK-SKIP-CNT");
    }

    public void setWkSkipCnt(int value) {
        setInt("WK-SKIP-CNT", value);
    }

    public int getWkSysYm() {
        return getInt("WK-SYS-YM");
    }

    public void setWkSysYm(int value) {
        setInt("WK-SYS-YM", value);
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

    public long getWkTaxTot() {
        return getLong("WK-TAX-TOT");
    }

    public void setWkTaxTot(long value) {
        setLong("WK-TAX-TOT", value);
    }

    public int getWkTgtDate() {
        return getInt("WK-TGT-DATE");
    }

    public void setWkTgtDate(int value) {
        setInt("WK-TGT-DATE", value);
    }

    public int getWkUserCode() {
        return getInt("WK-USER-CODE");
    }

    public void setWkUserCode(int value) {
        setInt("WK-USER-CODE", value);
    }
}

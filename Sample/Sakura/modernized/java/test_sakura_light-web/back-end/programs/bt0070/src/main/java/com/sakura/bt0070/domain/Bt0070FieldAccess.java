package com.sakura.bt0070.domain;

import com.sakura.bt0070.runtime.Bt0070Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for BT0070. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Bt0070FieldAccess extends RuntimeFieldAccess {

    public Bt0070FieldAccess(WorkingStorage ws, Bt0070Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getSyscf() != null) {
            register(fileSet.getSyscf().buffer());
        }
        if (fileSet != null && fileSet.getStokf() != null) {
            register(fileSet.getStokf().buffer());
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

    public BigDecimal getSkAvgCost() {
        return getDecimal("SK-AVG-COST");
    }

    public void setSkAvgCost(BigDecimal value) {
        setDecimal("SK-AVG-COST", value);
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

    public BigDecimal getSkYtdOut() {
        return getDecimal("SK-YTD-OUT");
    }

    public void setSkYtdOut(BigDecimal value) {
        setDecimal("SK-YTD-OUT", value);
    }

    public int getSyCurrYm() {
        return getInt("SY-CURR-YM");
    }

    public void setSyCurrYm(int value) {
        setInt("SY-CURR-YM", value);
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

    public int getWkAbortFlg() {
        return getInt("WK-ABORT-FLG");
    }

    public void setWkAbortFlg(int value) {
        setInt("WK-ABORT-FLG", value);
    }

    public int getWkActiveCnt() {
        return getInt("WK-ACTIVE-CNT");
    }

    public void setWkActiveCnt(int value) {
        setInt("WK-ACTIVE-CNT", value);
    }

    public String getWkConfirm() {
        return getString("WK-CONFIRM");
    }

    public void setWkConfirm(String value) {
        setString("WK-CONFIRM", value);
    }

    public int getWkDfltYm() {
        return getInt("WK-DFLT-YM");
    }

    public void setWkDfltYm(int value) {
        setInt("WK-DFLT-YM", value);
    }

    public int getWkECnt() {
        return getInt("WK-E-CNT");
    }

    public void setWkECnt(int value) {
        setInt("WK-E-CNT", value);
    }

    public long getWkEQty() {
        return getLong("WK-E-QTY");
    }

    public void setWkEQty(long value) {
        setLong("WK-E-QTY", value);
    }

    public BigDecimal getWkEVal() {
        return getDecimal("WK-E-VAL");
    }

    public void setWkEVal(BigDecimal value) {
        setDecimal("WK-E-VAL", value);
    }

    public int getWkEWhse() {
        return getInt("WK-E-WHSE");
    }

    public void setWkEWhse(int value) {
        setInt("WK-E-WHSE", value);
    }

    public int getWkEYm() {
        return getInt("WK-E-YM");
    }

    public void setWkEYm(int value) {
        setInt("WK-E-YM", value);
    }

    public int getWkFirstFlg() {
        return getInt("WK-FIRST-FLG");
    }

    public void setWkFirstFlg(int value) {
        setInt("WK-FIRST-FLG", value);
    }

    public int getWkFyYear() {
        return getInt("WK-FY-YEAR");
    }

    public void setWkFyYear(int value) {
        setInt("WK-FY-YEAR", value);
    }

    public String getWkInLine() {
        return getString("WK-IN-LINE");
    }

    public void setWkInLine(String value) {
        setString("WK-IN-LINE", value);
    }

    public BigDecimal getWkItemVal() {
        return getDecimal("WK-ITEM-VAL");
    }

    public void setWkItemVal(BigDecimal value) {
        setDecimal("WK-ITEM-VAL", value);
    }

    public int getWkMm() {
        return getInt("WK-MM");
    }

    public void setWkMm(int value) {
        setInt("WK-MM", value);
    }

    public int getWkNewYear() {
        return getInt("WK-NEW-YEAR");
    }

    public void setWkNewYear(int value) {
        setInt("WK-NEW-YEAR", value);
    }

    public int getWkNewYm() {
        return getInt("WK-NEW-YM");
    }

    public void setWkNewYm(int value) {
        setInt("WK-NEW-YM", value);
    }

    public int getWkOldYm() {
        return getInt("WK-OLD-YM");
    }

    public void setWkOldYm(int value) {
        setInt("WK-OLD-YM", value);
    }

    public long getWkOnhandTot() {
        return getLong("WK-ONHAND-TOT");
    }

    public void setWkOnhandTot(long value) {
        setLong("WK-ONHAND-TOT", value);
    }

    public int getWkPrevWhse() {
        return getInt("WK-PREV-WHSE");
    }

    public void setWkPrevWhse(int value) {
        setInt("WK-PREV-WHSE", value);
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

    public int getWkResetCnt() {
        return getInt("WK-RESET-CNT");
    }

    public void setWkResetCnt(int value) {
        setInt("WK-RESET-CNT", value);
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

    public BigDecimal getWkValTot() {
        return getDecimal("WK-VAL-TOT");
    }

    public void setWkValTot(BigDecimal value) {
        setDecimal("WK-VAL-TOT", value);
    }

    public int getWkWhItems() {
        return getInt("WK-WH-ITEMS");
    }

    public void setWkWhItems(int value) {
        setInt("WK-WH-ITEMS", value);
    }

    public BigDecimal getWkWhVal() {
        return getDecimal("WK-WH-VAL");
    }

    public void setWkWhVal(BigDecimal value) {
        setDecimal("WK-WH-VAL", value);
    }

    public long getWkWhYtdin() {
        return getLong("WK-WH-YTDIN");
    }

    public void setWkWhYtdin(long value) {
        setLong("WK-WH-YTDIN", value);
    }

    public long getWkWhYtdout() {
        return getLong("WK-WH-YTDOUT");
    }

    public void setWkWhYtdout(long value) {
        setLong("WK-WH-YTDOUT", value);
    }

    public int getWkWhseCnt() {
        return getInt("WK-WHSE-CNT");
    }

    public void setWkWhseCnt(int value) {
        setInt("WK-WHSE-CNT", value);
    }

    public long getWkYtdinTot() {
        return getLong("WK-YTDIN-TOT");
    }

    public void setWkYtdinTot(long value) {
        setLong("WK-YTDIN-TOT", value);
    }

    public long getWkYtdoutTot() {
        return getLong("WK-YTDOUT-TOT");
    }

    public void setWkYtdoutTot(long value) {
        setLong("WK-YTDOUT-TOT", value);
    }

    public int getWkYyyy() {
        return getInt("WK-YYYY");
    }

    public void setWkYyyy(int value) {
        setInt("WK-YYYY", value);
    }
}

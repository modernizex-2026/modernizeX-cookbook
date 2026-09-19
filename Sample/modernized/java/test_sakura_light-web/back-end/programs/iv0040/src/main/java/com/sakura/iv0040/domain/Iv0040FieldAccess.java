package com.sakura.iv0040.domain;

import com.sakura.iv0040.runtime.Iv0040Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for IV0040. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Iv0040FieldAccess extends RuntimeFieldAccess {

    public Iv0040FieldAccess(WorkingStorage ws, Iv0040Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getSmovf() != null) {
            register(fileSet.getSmovf().buffer());
        }
        if (fileSet != null && fileSet.getProdf() != null) {
            register(fileSet.getProdf().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getBrwFlg() {
        return getInt("BRW-FLG");
    }

    public void setBrwFlg(int value) {
        setInt("BRW-FLG", value);
    }

    public int getCompletionCode() {
        return getInt("COMPLETION-CODE");
    }

    public void setCompletionCode(int value) {
        setInt("COMPLETION-CODE", value);
    }

    public int getEndFlg() {
        return getInt("END-FLG");
    }

    public void setEndFlg(int value) {
        setInt("END-FLG", value);
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

    public BigDecimal getSmBalAfter() {
        return getDecimal("SM-BAL-AFTER");
    }

    public void setSmBalAfter(BigDecimal value) {
        setDecimal("SM-BAL-AFTER", value);
    }

    public int getSmDate() {
        return getInt("SM-DATE");
    }

    public void setSmDate(int value) {
        setInt("SM-DATE", value);
    }

    public int getSmKind() {
        return getInt("SM-KIND");
    }

    public void setSmKind(int value) {
        setInt("SM-KIND", value);
    }

    public int getSmProd() {
        return getInt("SM-PROD");
    }

    public void setSmProd(int value) {
        setInt("SM-PROD", value);
    }

    public BigDecimal getSmQty() {
        return getDecimal("SM-QTY");
    }

    public void setSmQty(BigDecimal value) {
        setDecimal("SM-QTY", value);
    }

    public long getSmRefNo() {
        return getLong("SM-REF-NO");
    }

    public void setSmRefNo(long value) {
        setLong("SM-REF-NO", value);
    }

    public int getSmRefType() {
        return getInt("SM-REF-TYPE");
    }

    public void setSmRefType(int value) {
        setInt("SM-REF-TYPE", value);
    }

    public String getWkDl() {
        return groupToString("WK-DL");
    }

    public void setWkDl(String value) {
        setGroup("WK-DL", value);
    }

    public int getWkDlBal() {
        return getInt("WK-DL-BAL");
    }

    public void setWkDlBal(int value) {
        setInt("WK-DL-BAL", value);
    }

    public int getWkDlDate() {
        return getInt("WK-DL-DATE");
    }

    public void setWkDlDate(int value) {
        setInt("WK-DL-DATE", value);
    }

    public String getWkDlKind() {
        return getString("WK-DL-KIND");
    }

    public void setWkDlKind(String value) {
        setString("WK-DL-KIND", value);
    }

    public int getWkDlQty() {
        return getInt("WK-DL-QTY");
    }

    public void setWkDlQty(int value) {
        setInt("WK-DL-QTY", value);
    }

    public long getWkDlRno() {
        return getLong("WK-DL-RNO");
    }

    public void setWkDlRno(long value) {
        setLong("WK-DL-RNO", value);
    }

    public int getWkDlRtype() {
        return getInt("WK-DL-RTYPE");
    }

    public void setWkDlRtype(int value) {
        setInt("WK-DL-RTYPE", value);
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

    public int getWkKeyDate() {
        return getInt("WK-KEY-DATE");
    }

    public void setWkKeyDate(int value) {
        setInt("WK-KEY-DATE", value);
    }

    public int getWkKeyProd() {
        return getInt("WK-KEY-PROD");
    }

    public void setWkKeyProd(int value) {
        setInt("WK-KEY-PROD", value);
    }

    public String getWkKindLbl() {
        return getString("WK-KIND-LBL");
    }

    public void setWkKindLbl(String value) {
        setString("WK-KIND-LBL", value);
    }

    public String getWkList(int index) {
        return getString("WK-LIST", index);
    }

    public void setWkList(int index, String value) {
        setString("WK-LIST", value, index);
    }

    public String getWkMsgLine() {
        return getString("WK-MSG-LINE");
    }

    public void setWkMsgLine(String value) {
        setString("WK-MSG-LINE", value);
    }

    public int getWkPageNo() {
        return getInt("WK-PAGE-NO");
    }

    public void setWkPageNo(int value) {
        setInt("WK-PAGE-NO", value);
    }

    public String getWkPrName() {
        return getString("WK-PR-NAME");
    }

    public void setWkPrName(String value) {
        setString("WK-PR-NAME", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
    }

    public int getWkRow() {
        return getInt("WK-ROW");
    }

    public void setWkRow(int value) {
        setInt("WK-ROW", value);
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

    public long getWkTotIn() {
        return getLong("WK-TOT-IN");
    }

    public void setWkTotIn(long value) {
        setLong("WK-TOT-IN", value);
    }

    public long getWkTotOut() {
        return getLong("WK-TOT-OUT");
    }

    public void setWkTotOut(long value) {
        setLong("WK-TOT-OUT", value);
    }
}

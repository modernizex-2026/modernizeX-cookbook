package com.sakura.pu0020.domain;

import com.sakura.pu0020.runtime.Pu0020Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for PU0020. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Pu0020FieldAccess extends RuntimeFieldAccess {

    public Pu0020FieldAccess(WorkingStorage ws, Pu0020Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getPohf() != null) {
            register(fileSet.getPohf().buffer());
        }
        if (fileSet != null && fileSet.getPodf() != null) {
            register(fileSet.getPodf().buffer());
        }
        if (fileSet != null && fileSet.getSuppf() != null) {
            register(fileSet.getSuppf().buffer());
        }
        if (fileSet != null && fileSet.getProdf() != null) {
            register(fileSet.getProdf().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
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

    public int getEofFlg() {
        return getInt("EOF-FLG");
    }

    public void setEofFlg(int value) {
        setInt("EOF-FLG", value);
    }

    public String getEsts() {
        return getString("ESTS");
    }

    public void setEsts(String value) {
        setString("ESTS", value);
    }

    public int getFoundFlg() {
        return getInt("FOUND-FLG");
    }

    public void setFoundFlg(int value) {
        setInt("FOUND-FLG", value);
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

    public BigDecimal getPdAmount() {
        return getDecimal("PD-AMOUNT");
    }

    public void setPdAmount(BigDecimal value) {
        setDecimal("PD-AMOUNT", value);
    }

    public int getPdLine() {
        return getInt("PD-LINE");
    }

    public void setPdLine(int value) {
        setInt("PD-LINE", value);
    }

    public long getPdNo() {
        return getLong("PD-NO");
    }

    public void setPdNo(long value) {
        setLong("PD-NO", value);
    }

    public int getPdProd() {
        return getInt("PD-PROD");
    }

    public void setPdProd(int value) {
        setInt("PD-PROD", value);
    }

    public BigDecimal getPdQty() {
        return getDecimal("PD-QTY");
    }

    public void setPdQty(BigDecimal value) {
        setDecimal("PD-QTY", value);
    }

    public BigDecimal getPdRecvQty() {
        return getDecimal("PD-RECV-QTY");
    }

    public void setPdRecvQty(BigDecimal value) {
        setDecimal("PD-RECV-QTY", value);
    }

    public int getPhDate() {
        return getInt("PH-DATE");
    }

    public void setPhDate(int value) {
        setInt("PH-DATE", value);
    }

    public long getPhNo() {
        return getLong("PH-NO");
    }

    public void setPhNo(long value) {
        setLong("PH-NO", value);
    }

    public String getPhRec() {
        return groupToString("PH-REC");
    }

    public void setPhRec(String value) {
        setGroup("PH-REC", value);
    }

    public int getPhStatus() {
        return getInt("PH-STATUS");
    }

    public void setPhStatus(int value) {
        setInt("PH-STATUS", value);
    }

    public int getPhSupp() {
        return getInt("PH-SUPP");
    }

    public void setPhSupp(int value) {
        setInt("PH-SUPP", value);
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

    public long getRbAmt() {
        return getLong("RB-AMT");
    }

    public void setRbAmt(long value) {
        setLong("RB-AMT", value);
    }

    public int getRbLine() {
        return getInt("RB-LINE");
    }

    public void setRbLine(int value) {
        setInt("RB-LINE", value);
    }

    public String getRbName() {
        return getString("RB-NAME");
    }

    public void setRbName(String value) {
        setString("RB-NAME", value);
    }

    public int getRbProd() {
        return getInt("RB-PROD");
    }

    public void setRbProd(int value) {
        setInt("RB-PROD", value);
    }

    public int getRbQty() {
        return getInt("RB-QTY");
    }

    public void setRbQty(int value) {
        setInt("RB-QTY", value);
    }

    public int getRbRecv() {
        return getInt("RB-RECV");
    }

    public void setRbRecv(int value) {
        setInt("RB-RECV", value);
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

    public int getWkMode() {
        return getInt("WK-MODE");
    }

    public void setWkMode(int value) {
        setInt("WK-MODE", value);
    }

    public int getWkMoreFlg() {
        return getInt("WK-MORE-FLG");
    }

    public void setWkMoreFlg(int value) {
        setInt("WK-MORE-FLG", value);
    }

    public String getWkMsgLine() {
        return getString("WK-MSG-LINE");
    }

    public void setWkMsgLine(String value) {
        setString("WK-MSG-LINE", value);
    }

    public String getWkNav() {
        return getString("WK-NAV");
    }

    public void setWkNav(String value) {
        setString("WK-NAV", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
    }

    public String getWkRowBuf() {
        return groupToString("WK-ROW-BUF");
    }

    public void setWkRowBuf(String value) {
        setGroup("WK-ROW-BUF", value);
    }

    public int getWkRowCnt() {
        return getInt("WK-ROW-CNT");
    }

    public void setWkRowCnt(int value) {
        setInt("WK-ROW-CNT", value);
    }

    public String getWkSaveRec() {
        return getString("WK-SAVE-REC");
    }

    public void setWkSaveRec(String value) {
        setString("WK-SAVE-REC", value);
    }

    public int getWkSrchDate() {
        return getInt("WK-SRCH-DATE");
    }

    public void setWkSrchDate(int value) {
        setInt("WK-SRCH-DATE", value);
    }

    public long getWkSrchNo() {
        return getLong("WK-SRCH-NO");
    }

    public void setWkSrchNo(long value) {
        setLong("WK-SRCH-NO", value);
    }

    public int getWkSrchSupp() {
        return getInt("WK-SRCH-SUPP");
    }

    public void setWkSrchSupp(int value) {
        setInt("WK-SRCH-SUPP", value);
    }

    public String getWkStatText() {
        return getString("WK-STAT-TEXT");
    }

    public void setWkStatText(String value) {
        setString("WK-STAT-TEXT", value);
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

    public int getWkViewDone() {
        return getInt("WK-VIEW-DONE");
    }

    public void setWkViewDone(int value) {
        setInt("WK-VIEW-DONE", value);
    }

    public String getWrBuf(int index) {
        return getString("WR-BUF", index);
    }

    public void setWrBuf(int index, String value) {
        setString("WR-BUF", value, index);
    }
}

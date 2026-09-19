package com.sakura.iv0030.domain;

import com.sakura.iv0030.runtime.Iv0030Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for IV0030. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Iv0030FieldAccess extends RuntimeFieldAccess {

    public Iv0030FieldAccess(WorkingStorage ws, Iv0030Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getStokf() != null) {
            register(fileSet.getStokf().buffer());
        }
        if (fileSet != null && fileSet.getSmovf() != null) {
            register(fileSet.getSmovf().buffer());
        }
        if (fileSet != null && fileSet.getProdf() != null) {
            register(fileSet.getProdf().buffer());
        }
        if (fileSet != null && fileSet.getWhsef() != null) {
            register(fileSet.getWhsef().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getCntFlg() {
        return getInt("CNT-FLG");
    }

    public void setCntFlg(int value) {
        setInt("CNT-FLG", value);
    }

    public int getCompletionCode() {
        return getInt("COMPLETION-CODE");
    }

    public void setCompletionCode(int value) {
        setInt("COMPLETION-CODE", value);
    }

    public int getCtBook(int index) {
        return getInt("CT-BOOK", index);
    }

    public void setCtBook(int index, int value) {
        setInt("CT-BOOK", value, index);
    }

    public int getCtCounted(int index) {
        return getInt("CT-COUNTED", index);
    }

    public void setCtCounted(int index, int value) {
        setInt("CT-COUNTED", value, index);
    }

    public int getCtDiff(int index) {
        return getInt("CT-DIFF", index);
    }

    public void setCtDiff(int index, int value) {
        setInt("CT-DIFF", value, index);
    }

    public int getCtProd(int index) {
        return getInt("CT-PROD", index);
    }

    public void setCtProd(int index, int value) {
        setInt("CT-PROD", value, index);
    }

    public int getCtWhse(int index) {
        return getInt("CT-WHSE", index);
    }

    public void setCtWhse(int index, int value) {
        setInt("CT-WHSE", value, index);
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

    public String getKnum() {
        return groupToString("KNUM");
    }

    public void setKnum(String value) {
        setGroup("KNUM", value);
    }

    public String getKnumKey() {
        return getString("KNUM-KEY");
    }

    public void setKnumKey(String value) {
        setString("KNUM-KEY", value);
    }

    public long getKnumNumber() {
        return getLong("KNUM-NUMBER");
    }

    public void setKnumNumber(long value) {
        setLong("KNUM-NUMBER", value);
    }

    public String getKnumStatus() {
        return getString("KNUM-STATUS");
    }

    public void setKnumStatus(String value) {
        setString("KNUM-STATUS", value);
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

    public int getRecFlg() {
        return getInt("REC-FLG");
    }

    public void setRecFlg(int value) {
        setInt("REC-FLG", value);
    }

    public int getRvwFlg() {
        return getInt("RVW-FLG");
    }

    public void setRvwFlg(int value) {
        setInt("RVW-FLG", value);
    }

    public BigDecimal getSkAvgCost() {
        return getDecimal("SK-AVG-COST");
    }

    public void setSkAvgCost(BigDecimal value) {
        setDecimal("SK-AVG-COST", value);
    }

    public int getSkLastInDate() {
        return getInt("SK-LAST-IN-DATE");
    }

    public void setSkLastInDate(int value) {
        setInt("SK-LAST-IN-DATE", value);
    }

    public int getSkLastOutDate() {
        return getInt("SK-LAST-OUT-DATE");
    }

    public void setSkLastOutDate(int value) {
        setInt("SK-LAST-OUT-DATE", value);
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

    public long getSmSeq() {
        return getLong("SM-SEQ");
    }

    public void setSmSeq(long value) {
        setLong("SM-SEQ", value);
    }

    public BigDecimal getSmUnitCost() {
        return getDecimal("SM-UNIT-COST");
    }

    public void setSmUnitCost(BigDecimal value) {
        setDecimal("SM-UNIT-COST", value);
    }

    public int getSmUser() {
        return getInt("SM-USER");
    }

    public void setSmUser(int value) {
        setInt("SM-USER", value);
    }

    public int getSmWhse() {
        return getInt("SM-WHSE");
    }

    public void setSmWhse(int value) {
        setInt("SM-WHSE", value);
    }

    public int getWhCode() {
        return getInt("WH-CODE");
    }

    public void setWhCode(int value) {
        setInt("WH-CODE", value);
    }

    public int getWhDelFlag() {
        return getInt("WH-DEL-FLAG");
    }

    public void setWhDelFlag(int value) {
        setInt("WH-DEL-FLAG", value);
    }

    public String getWhName() {
        return getString("WH-NAME");
    }

    public void setWhName(String value) {
        setString("WH-NAME", value);
    }

    public int getWkBook() {
        return getInt("WK-BOOK");
    }

    public void setWkBook(int value) {
        setInt("WK-BOOK", value);
    }

    public String getWkCl() {
        return groupToString("WK-CL");
    }

    public void setWkCl(String value) {
        setGroup("WK-CL", value);
    }

    public int getWkClBook() {
        return getInt("WK-CL-BOOK");
    }

    public void setWkClBook(int value) {
        setInt("WK-CL-BOOK", value);
    }

    public int getWkClCnt() {
        return getInt("WK-CL-CNT");
    }

    public void setWkClCnt(int value) {
        setInt("WK-CL-CNT", value);
    }

    public int getWkClDiff() {
        return getInt("WK-CL-DIFF");
    }

    public void setWkClDiff(int value) {
        setInt("WK-CL-DIFF", value);
    }

    public String getWkClName() {
        return getString("WK-CL-NAME");
    }

    public void setWkClName(String value) {
        setString("WK-CL-NAME", value);
    }

    public int getWkClProd() {
        return getInt("WK-CL-PROD");
    }

    public void setWkClProd(int value) {
        setInt("WK-CL-PROD", value);
    }

    public int getWkCnt() {
        return getInt("WK-CNT");
    }

    public void setWkCnt(int value) {
        setInt("WK-CNT", value);
    }

    public String getWkConfirm() {
        return getString("WK-CONFIRM");
    }

    public void setWkConfirm(String value) {
        setString("WK-CONFIRM", value);
    }

    public int getWkCounted() {
        return getInt("WK-COUNTED");
    }

    public void setWkCounted(int value) {
        setInt("WK-COUNTED", value);
    }

    public int getWkDcnt() {
        return getInt("WK-DCNT");
    }

    public void setWkDcnt(int value) {
        setInt("WK-DCNT", value);
    }

    public int getWkDiff() {
        return getInt("WK-DIFF");
    }

    public void setWkDiff(int value) {
        setInt("WK-DIFF", value);
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

    public int getWkPcnt() {
        return getInt("WK-PCNT");
    }

    public void setWkPcnt(int value) {
        setInt("WK-PCNT", value);
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

    public int getWkRi() {
        return getInt("WK-RI");
    }

    public void setWkRi(int value) {
        setInt("WK-RI", value);
    }

    public int getWkRpage() {
        return getInt("WK-RPAGE");
    }

    public void setWkRpage(int value) {
        setInt("WK-RPAGE", value);
    }

    public int getWkRrow() {
        return getInt("WK-RROW");
    }

    public void setWkRrow(int value) {
        setInt("WK-RROW", value);
    }

    public int getWkRstart() {
        return getInt("WK-RSTART");
    }

    public void setWkRstart(int value) {
        setInt("WK-RSTART", value);
    }

    public int getWkShowNo() {
        return getInt("WK-SHOW-NO");
    }

    public void setWkShowNo(int value) {
        setInt("WK-SHOW-NO", value);
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

    public int getWkTcnt() {
        return getInt("WK-TCNT");
    }

    public void setWkTcnt(int value) {
        setInt("WK-TCNT", value);
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

    public String getWkWhName() {
        return getString("WK-WH-NAME");
    }

    public void setWkWhName(String value) {
        setString("WK-WH-NAME", value);
    }

    public int getWkWhseIn() {
        return getInt("WK-WHSE-IN");
    }

    public void setWkWhseIn(int value) {
        setInt("WK-WHSE-IN", value);
    }

    /* ── Synthetic int wrappers (INDEX BY counters, unresolved symbols) ── */
    public int getCx() {
        return getInt("CX");
    }

    public void setCx(int value) {
        setInt("CX", value);
    }
}

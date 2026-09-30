package com.sakura.iv0050.domain;

import com.sakura.iv0050.runtime.Iv0050Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for IV0050. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Iv0050FieldAccess extends RuntimeFieldAccess {

    public Iv0050FieldAccess(WorkingStorage ws, Iv0050Datasets fileSet) {
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

    public int getErrFlg() {
        return getInt("ERR-FLG");
    }

    public void setErrFlg(int value) {
        setInt("ERR-FLG", value);
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

    public int getPrDelFlag() {
        return getInt("PR-DEL-FLAG");
    }

    public void setPrDelFlag(int value) {
        setInt("PR-DEL-FLAG", value);
    }

    public String getPrName() {
        return getString("PR-NAME");
    }

    public void setPrName(String value) {
        setString("PR-NAME", value);
    }

    public int getPrStockMng() {
        return getInt("PR-STOCK-MNG");
    }

    public void setPrStockMng(int value) {
        setInt("PR-STOCK-MNG", value);
    }

    public BigDecimal getSkAllocated() {
        return getDecimal("SK-ALLOCATED");
    }

    public void setSkAllocated(BigDecimal value) {
        setDecimal("SK-ALLOCATED", value);
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

    public BigDecimal getSkOnOrder() {
        return getDecimal("SK-ON-ORDER");
    }

    public void setSkOnOrder(BigDecimal value) {
        setDecimal("SK-ON-ORDER", value);
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

    public int getWkAvail() {
        return getInt("WK-AVAIL");
    }

    public void setWkAvail(int value) {
        setInt("WK-AVAIL", value);
    }

    public String getWkConfirm() {
        return getString("WK-CONFIRM");
    }

    public void setWkConfirm(String value) {
        setString("WK-CONFIRM", value);
    }

    public int getWkDstNew() {
        return getInt("WK-DST-NEW");
    }

    public void setWkDstNew(int value) {
        setInt("WK-DST-NEW", value);
    }

    public int getWkDstOnhand() {
        return getInt("WK-DST-ONHAND");
    }

    public void setWkDstOnhand(int value) {
        setInt("WK-DST-ONHAND", value);
    }

    public long getWkEdQty() {
        return getLong("WK-ED-QTY");
    }

    public void setWkEdQty(long value) {
        setLong("WK-ED-QTY", value);
    }

    public String getWkFkeyLine() {
        return getString("WK-FKEY-LINE");
    }

    public void setWkFkeyLine(String value) {
        setString("WK-FKEY-LINE", value);
    }

    public String getWkFromName() {
        return getString("WK-FROM-NAME");
    }

    public void setWkFromName(String value) {
        setString("WK-FROM-NAME", value);
    }

    public int getWkFromWhse() {
        return getInt("WK-FROM-WHSE");
    }

    public void setWkFromWhse(int value) {
        setInt("WK-FROM-WHSE", value);
    }

    public int getWkKeyProd() {
        return getInt("WK-KEY-PROD");
    }

    public void setWkKeyProd(int value) {
        setInt("WK-KEY-PROD", value);
    }

    public String getWkMsgLine() {
        return getString("WK-MSG-LINE");
    }

    public void setWkMsgLine(String value) {
        setString("WK-MSG-LINE", value);
    }

    public int getWkMvBal() {
        return getInt("WK-MV-BAL");
    }

    public void setWkMvBal(int value) {
        setInt("WK-MV-BAL", value);
    }

    public int getWkMvQty() {
        return getInt("WK-MV-QTY");
    }

    public void setWkMvQty(int value) {
        setInt("WK-MV-QTY", value);
    }

    public int getWkMvWhse() {
        return getInt("WK-MV-WHSE");
    }

    public void setWkMvWhse(int value) {
        setInt("WK-MV-WHSE", value);
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

    public int getWkSrcAlloc() {
        return getInt("WK-SRC-ALLOC");
    }

    public void setWkSrcAlloc(int value) {
        setInt("WK-SRC-ALLOC", value);
    }

    public int getWkSrcNew() {
        return getInt("WK-SRC-NEW");
    }

    public void setWkSrcNew(int value) {
        setInt("WK-SRC-NEW", value);
    }

    public int getWkSrcOnhand() {
        return getInt("WK-SRC-ONHAND");
    }

    public void setWkSrcOnhand(int value) {
        setInt("WK-SRC-ONHAND", value);
    }

    public int getWkStkMng() {
        return getInt("WK-STK-MNG");
    }

    public void setWkStkMng(int value) {
        setInt("WK-STK-MNG", value);
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

    public String getWkToName() {
        return getString("WK-TO-NAME");
    }

    public void setWkToName(String value) {
        setString("WK-TO-NAME", value);
    }

    public int getWkToWhse() {
        return getInt("WK-TO-WHSE");
    }

    public void setWkToWhse(int value) {
        setInt("WK-TO-WHSE", value);
    }

    public int getWkTrQty() {
        return getInt("WK-TR-QTY");
    }

    public void setWkTrQty(int value) {
        setInt("WK-TR-QTY", value);
    }

    public BigDecimal getWkUnitCost() {
        return getDecimal("WK-UNIT-COST");
    }

    public void setWkUnitCost(BigDecimal value) {
        setDecimal("WK-UNIT-COST", value);
    }

    public int getWkUserCode() {
        return getInt("WK-USER-CODE");
    }

    public void setWkUserCode(int value) {
        setInt("WK-USER-CODE", value);
    }
}

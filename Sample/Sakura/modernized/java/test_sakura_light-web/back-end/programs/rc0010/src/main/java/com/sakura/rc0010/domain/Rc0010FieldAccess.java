package com.sakura.rc0010.domain;

import com.sakura.rc0010.runtime.Rc0010Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for RC0010. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Rc0010FieldAccess extends RuntimeFieldAccess {

    public Rc0010FieldAccess(WorkingStorage ws, Rc0010Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getPohf() != null) {
            register(fileSet.getPohf().buffer());
        }
        if (fileSet != null && fileSet.getPodf() != null) {
            register(fileSet.getPodf().buffer());
        }
        if (fileSet != null && fileSet.getRcvhf() != null) {
            register(fileSet.getRcvhf().buffer());
        }
        if (fileSet != null && fileSet.getRcvdf() != null) {
            register(fileSet.getRcvdf().buffer());
        }
        if (fileSet != null && fileSet.getStokf() != null) {
            register(fileSet.getStokf().buffer());
        }
        if (fileSet != null && fileSet.getSmovf() != null) {
            register(fileSet.getSmovf().buffer());
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

    public int getPdStatus() {
        return getInt("PD-STATUS");
    }

    public void setPdStatus(int value) {
        setInt("PD-STATUS", value);
    }

    public BigDecimal getPdUnitCost() {
        return getDecimal("PD-UNIT-COST");
    }

    public void setPdUnitCost(BigDecimal value) {
        setDecimal("PD-UNIT-COST", value);
    }

    public int getPdWhse() {
        return getInt("PD-WHSE");
    }

    public void setPdWhse(int value) {
        setInt("PD-WHSE", value);
    }

    public int getPhDelFlag() {
        return getInt("PH-DEL-FLAG");
    }

    public void setPhDelFlag(int value) {
        setInt("PH-DEL-FLAG", value);
    }

    public long getPhNo() {
        return getLong("PH-NO");
    }

    public void setPhNo(long value) {
        setLong("PH-NO", value);
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

    public int getPhWhse() {
        return getInt("PH-WHSE");
    }

    public void setPhWhse(int value) {
        setInt("PH-WHSE", value);
    }

    public int getPoOk() {
        return getInt("PO-OK");
    }

    public void setPoOk(int value) {
        setInt("PO-OK", value);
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

    public int getPrStockMng() {
        return getInt("PR-STOCK-MNG");
    }

    public void setPrStockMng(int value) {
        setInt("PR-STOCK-MNG", value);
    }

    public BigDecimal getRdAmount() {
        return getDecimal("RD-AMOUNT");
    }

    public void setRdAmount(BigDecimal value) {
        setDecimal("RD-AMOUNT", value);
    }

    public int getRdLine() {
        return getInt("RD-LINE");
    }

    public void setRdLine(int value) {
        setInt("RD-LINE", value);
    }

    public long getRdNo() {
        return getLong("RD-NO");
    }

    public void setRdNo(long value) {
        setLong("RD-NO", value);
    }

    public long getRdPo() {
        return getLong("RD-PO");
    }

    public void setRdPo(long value) {
        setLong("RD-PO", value);
    }

    public int getRdPoLine() {
        return getInt("RD-PO-LINE");
    }

    public void setRdPoLine(int value) {
        setInt("RD-PO-LINE", value);
    }

    public int getRdProd() {
        return getInt("RD-PROD");
    }

    public void setRdProd(int value) {
        setInt("RD-PROD", value);
    }

    public BigDecimal getRdQty() {
        return getDecimal("RD-QTY");
    }

    public void setRdQty(BigDecimal value) {
        setDecimal("RD-QTY", value);
    }

    public BigDecimal getRdUnitCost() {
        return getDecimal("RD-UNIT-COST");
    }

    public void setRdUnitCost(BigDecimal value) {
        setDecimal("RD-UNIT-COST", value);
    }

    public int getRdWhse() {
        return getInt("RD-WHSE");
    }

    public void setRdWhse(int value) {
        setInt("RD-WHSE", value);
    }

    public int getRhAddDate() {
        return getInt("RH-ADD-DATE");
    }

    public void setRhAddDate(int value) {
        setInt("RH-ADD-DATE", value);
    }

    public int getRhAddUser() {
        return getInt("RH-ADD-USER");
    }

    public void setRhAddUser(int value) {
        setInt("RH-ADD-USER", value);
    }

    public int getRhDate() {
        return getInt("RH-DATE");
    }

    public void setRhDate(int value) {
        setInt("RH-DATE", value);
    }

    public int getRhDelFlag() {
        return getInt("RH-DEL-FLAG");
    }

    public void setRhDelFlag(int value) {
        setInt("RH-DEL-FLAG", value);
    }

    public int getRhLines() {
        return getInt("RH-LINES");
    }

    public void setRhLines(int value) {
        setInt("RH-LINES", value);
    }

    public long getRhNo() {
        return getLong("RH-NO");
    }

    public void setRhNo(long value) {
        setLong("RH-NO", value);
    }

    public long getRhPo() {
        return getLong("RH-PO");
    }

    public void setRhPo(long value) {
        setLong("RH-PO", value);
    }

    public String getRhRemark() {
        return getString("RH-REMARK");
    }

    public void setRhRemark(String value) {
        setString("RH-REMARK", value);
    }

    public int getRhStatus() {
        return getInt("RH-STATUS");
    }

    public void setRhStatus(int value) {
        setInt("RH-STATUS", value);
    }

    public int getRhSupp() {
        return getInt("RH-SUPP");
    }

    public void setRhSupp(int value) {
        setInt("RH-SUPP", value);
    }

    public int getRhWhse() {
        return getInt("RH-WHSE");
    }

    public void setRhWhse(int value) {
        setInt("RH-WHSE", value);
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

    public BigDecimal getWkAvgNum() {
        return getDecimal("WK-AVG-NUM");
    }

    public void setWkAvgNum(BigDecimal value) {
        setDecimal("WK-AVG-NUM", value);
    }

    public String getWkConfirm() {
        return getString("WK-CONFIRM");
    }

    public void setWkConfirm(String value) {
        setString("WK-CONFIRM", value);
    }

    public int getWkCurline() {
        return getInt("WK-CURLINE");
    }

    public void setWkCurline(int value) {
        setInt("WK-CURLINE", value);
    }

    public int getWkDRecv() {
        return getInt("WK-D-RECV");
    }

    public void setWkDRecv(int value) {
        setInt("WK-D-RECV", value);
    }

    public int getWkEntryDone() {
        return getInt("WK-ENTRY-DONE");
    }

    public void setWkEntryDone(int value) {
        setInt("WK-ENTRY-DONE", value);
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

    public BigDecimal getWkLCost() {
        return getDecimal("WK-L-COST");
    }

    public void setWkLCost(BigDecimal value) {
        setDecimal("WK-L-COST", value);
    }

    public String getWkLName() {
        return getString("WK-L-NAME");
    }

    public void setWkLName(String value) {
        setString("WK-L-NAME", value);
    }

    public int getWkLOrd() {
        return getInt("WK-L-ORD");
    }

    public void setWkLOrd(int value) {
        setInt("WK-L-ORD", value);
    }

    public int getWkLOut() {
        return getInt("WK-L-OUT");
    }

    public void setWkLOut(int value) {
        setInt("WK-L-OUT", value);
    }

    public int getWkLPoline() {
        return getInt("WK-L-POLINE");
    }

    public void setWkLPoline(int value) {
        setInt("WK-L-POLINE", value);
    }

    public int getWkLPos() {
        return getInt("WK-L-POS");
    }

    public void setWkLPos(int value) {
        setInt("WK-L-POS", value);
    }

    public int getWkLPrecv() {
        return getInt("WK-L-PRECV");
    }

    public void setWkLPrecv(int value) {
        setInt("WK-L-PRECV", value);
    }

    public int getWkLProd() {
        return getInt("WK-L-PROD");
    }

    public void setWkLProd(int value) {
        setInt("WK-L-PROD", value);
    }

    public int getWkLcnt() {
        return getInt("WK-LCNT");
    }

    public void setWkLcnt(int value) {
        setInt("WK-LCNT", value);
    }

    public String getWkMsgLine() {
        return getString("WK-MSG-LINE");
    }

    public void setWkMsgLine(String value) {
        setString("WK-MSG-LINE", value);
    }

    public int getWkNewOnhand() {
        return getInt("WK-NEW-ONHAND");
    }

    public void setWkNewOnhand(int value) {
        setInt("WK-NEW-ONHAND", value);
    }

    public int getWkNewStatus() {
        return getInt("WK-NEW-STATUS");
    }

    public void setWkNewStatus(int value) {
        setInt("WK-NEW-STATUS", value);
    }

    public int getWkOutFlg() {
        return getInt("WK-OUT-FLG");
    }

    public void setWkOutFlg(int value) {
        setInt("WK-OUT-FLG", value);
    }

    public long getWkPoKey() {
        return getLong("WK-PO-KEY");
    }

    public void setWkPoKey(long value) {
        setLong("WK-PO-KEY", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
    }

    public int getWkRdLine() {
        return getInt("WK-RD-LINE");
    }

    public void setWkRdLine(int value) {
        setInt("WK-RD-LINE", value);
    }

    public int getWkRecvCnt() {
        return getInt("WK-RECV-CNT");
    }

    public void setWkRecvCnt(int value) {
        setInt("WK-RECV-CNT", value);
    }

    public int getWkRecvDate() {
        return getInt("WK-RECV-DATE");
    }

    public void setWkRecvDate(int value) {
        setInt("WK-RECV-DATE", value);
    }

    public long getWkRhNoD() {
        return getLong("WK-RH-NO-D");
    }

    public void setWkRhNoD(long value) {
        setLong("WK-RH-NO-D", value);
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

    public int getWkUserCode() {
        return getInt("WK-USER-CODE");
    }

    public void setWkUserCode(int value) {
        setInt("WK-USER-CODE", value);
    }

    public BigDecimal getWlCost(int index) {
        return getDecimal("WL-COST", index);
    }

    public void setWlCost(int index, BigDecimal value) {
        setDecimal("WL-COST", value, index);
    }

    public String getWlName(int index) {
        return getString("WL-NAME", index);
    }

    public void setWlName(int index, String value) {
        setString("WL-NAME", value, index);
    }

    public int getWlOrd(int index) {
        return getInt("WL-ORD", index);
    }

    public void setWlOrd(int index, int value) {
        setInt("WL-ORD", value, index);
    }

    public int getWlOut(int index) {
        return getInt("WL-OUT", index);
    }

    public void setWlOut(int index, int value) {
        setInt("WL-OUT", value, index);
    }

    public int getWlPoline(int index) {
        return getInt("WL-POLINE", index);
    }

    public void setWlPoline(int index, int value) {
        setInt("WL-POLINE", value, index);
    }

    public int getWlPrecv(int index) {
        return getInt("WL-PRECV", index);
    }

    public void setWlPrecv(int index, int value) {
        setInt("WL-PRECV", value, index);
    }

    public int getWlProd(int index) {
        return getInt("WL-PROD", index);
    }

    public void setWlProd(int index, int value) {
        setInt("WL-PROD", value, index);
    }

    public int getWlRecv(int index) {
        return getInt("WL-RECV", index);
    }

    public void setWlRecv(int index, int value) {
        setInt("WL-RECV", value, index);
    }

    public int getWlStkmng(int index) {
        return getInt("WL-STKMNG", index);
    }

    public void setWlStkmng(int index, int value) {
        setInt("WL-STKMNG", value, index);
    }

    public int getWlWhse(int index) {
        return getInt("WL-WHSE", index);
    }

    public void setWlWhse(int index, int value) {
        setInt("WL-WHSE", value, index);
    }
}

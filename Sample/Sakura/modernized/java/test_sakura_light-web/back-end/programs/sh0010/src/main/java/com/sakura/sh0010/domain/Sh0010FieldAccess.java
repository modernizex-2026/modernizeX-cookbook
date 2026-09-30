package com.sakura.sh0010.domain;

import com.sakura.runtime.record.RuntimeFieldAccess;
import com.sakura.sh0010.runtime.Sh0010Datasets;

import java.math.BigDecimal;

/**
 * Field accessor for SH0010. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Sh0010FieldAccess extends RuntimeFieldAccess {

    public Sh0010FieldAccess(WorkingStorage ws, Sh0010Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getOrdhf() != null) {
            register(fileSet.getOrdhf().buffer());
        }
        if (fileSet != null && fileSet.getOrddf() != null) {
            register(fileSet.getOrddf().buffer());
        }
        if (fileSet != null && fileSet.getShphf() != null) {
            register(fileSet.getShphf().buffer());
        }
        if (fileSet != null && fileSet.getShpdf() != null) {
            register(fileSet.getShpdf().buffer());
        }
        if (fileSet != null && fileSet.getStokf() != null) {
            register(fileSet.getStokf().buffer());
        }
        if (fileSet != null && fileSet.getSmovf() != null) {
            register(fileSet.getSmovf().buffer());
        }
        if (fileSet != null && fileSet.getCustf() != null) {
            register(fileSet.getCustf().buffer());
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

    public int getDrAlloc(int index) {
        return getInt("DR-ALLOC", index);
    }

    public void setDrAlloc(int index, int value) {
        setInt("DR-ALLOC", value, index);
    }

    public int getDrLine(int index) {
        return getInt("DR-LINE", index);
    }

    public void setDrLine(int index, int value) {
        setInt("DR-LINE", value, index);
    }

    public String getDrName(int index) {
        return getString("DR-NAME", index);
    }

    public void setDrName(int index, String value) {
        setString("DR-NAME", value, index);
    }

    public int getDrOrd(int index) {
        return getInt("DR-ORD", index);
    }

    public void setDrOrd(int index, int value) {
        setInt("DR-ORD", value, index);
    }

    public BigDecimal getDrPrice(int index) {
        return getDecimal("DR-PRICE", index);
    }

    public void setDrPrice(int index, BigDecimal value) {
        setDecimal("DR-PRICE", value, index);
    }

    public int getDrProd(int index) {
        return getInt("DR-PROD", index);
    }

    public void setDrProd(int index, int value) {
        setInt("DR-PROD", value, index);
    }

    public int getDrShip(int index) {
        return getInt("DR-SHIP", index);
    }

    public void setDrShip(int index, int value) {
        setInt("DR-SHIP", value, index);
    }

    public int getDrShipped(int index) {
        return getInt("DR-SHIPPED", index);
    }

    public void setDrShipped(int index, int value) {
        setInt("DR-SHIPPED", value, index);
    }

    public int getDrStkmng(int index) {
        return getInt("DR-STKMNG", index);
    }

    public void setDrStkmng(int index, int value) {
        setInt("DR-STKMNG", value, index);
    }

    public int getDrWhse(int index) {
        return getInt("DR-WHSE", index);
    }

    public void setDrWhse(int index, int value) {
        setInt("DR-WHSE", value, index);
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

    public BigDecimal getOdAllocQty() {
        return getDecimal("OD-ALLOC-QTY");
    }

    public void setOdAllocQty(BigDecimal value) {
        setDecimal("OD-ALLOC-QTY", value);
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

    public int getOdStatus() {
        return getInt("OD-STATUS");
    }

    public void setOdStatus(int value) {
        setInt("OD-STATUS", value);
    }

    public BigDecimal getOdUnitPrice() {
        return getDecimal("OD-UNIT-PRICE");
    }

    public void setOdUnitPrice(BigDecimal value) {
        setDecimal("OD-UNIT-PRICE", value);
    }

    public int getOdWhse() {
        return getInt("OD-WHSE");
    }

    public void setOdWhse(int value) {
        setInt("OD-WHSE", value);
    }

    public int getOhCust() {
        return getInt("OH-CUST");
    }

    public void setOhCust(int value) {
        setInt("OH-CUST", value);
    }

    public int getOhDelFlag() {
        return getInt("OH-DEL-FLAG");
    }

    public void setOhDelFlag(int value) {
        setInt("OH-DEL-FLAG", value);
    }

    public long getOhNo() {
        return getLong("OH-NO");
    }

    public void setOhNo(long value) {
        setLong("OH-NO", value);
    }

    public String getOhRemark() {
        return getString("OH-REMARK");
    }

    public void setOhRemark(String value) {
        setString("OH-REMARK", value);
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

    public int getOhUpdDate() {
        return getInt("OH-UPD-DATE");
    }

    public void setOhUpdDate(int value) {
        setInt("OH-UPD-DATE", value);
    }

    public int getOhUpdUser() {
        return getInt("OH-UPD-USER");
    }

    public void setOhUpdUser(int value) {
        setInt("OH-UPD-USER", value);
    }

    public int getOhWhse() {
        return getInt("OH-WHSE");
    }

    public void setOhWhse(int value) {
        setInt("OH-WHSE", value);
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

    public int getWkCap() {
        return getInt("WK-CAP");
    }

    public void setWkCap(int value) {
        setInt("WK-CAP", value);
    }

    public int getWkComplete() {
        return getInt("WK-COMPLETE");
    }

    public void setWkComplete(int value) {
        setInt("WK-COMPLETE", value);
    }

    public String getWkConfirm() {
        return getString("WK-CONFIRM");
    }

    public void setWkConfirm(String value) {
        setString("WK-CONFIRM", value);
    }

    public BigDecimal getWkCost() {
        return getDecimal("WK-COST");
    }

    public void setWkCost(BigDecimal value) {
        setDecimal("WK-COST", value);
    }

    public String getWkCustName() {
        return getString("WK-CUST-NAME");
    }

    public void setWkCustName(String value) {
        setString("WK-CUST-NAME", value);
    }

    public int getWkDcnt() {
        return getInt("WK-DCNT");
    }

    public void setWkDcnt(int value) {
        setInt("WK-DCNT", value);
    }

    public int getWkEditLn() {
        return getInt("WK-EDIT-LN");
    }

    public void setWkEditLn(int value) {
        setInt("WK-EDIT-LN", value);
    }

    public int getWkEditQty() {
        return getInt("WK-EDIT-QTY");
    }

    public void setWkEditQty(int value) {
        setInt("WK-EDIT-QTY", value);
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

    public int getWkIdx2() {
        return getInt("WK-IDX2");
    }

    public void setWkIdx2(int value) {
        setInt("WK-IDX2", value);
    }

    public String getWkMsgLine() {
        return getString("WK-MSG-LINE");
    }

    public void setWkMsgLine(String value) {
        setString("WK-MSG-LINE", value);
    }

    public long getWkOrderNo() {
        return getLong("WK-ORDER-NO");
    }

    public void setWkOrderNo(long value) {
        setLong("WK-ORDER-NO", value);
    }

    public int getWkPageCnt() {
        return getInt("WK-PAGE-CNT");
    }

    public void setWkPageCnt(int value) {
        setInt("WK-PAGE-CNT", value);
    }

    public int getWkPageNo() {
        return getInt("WK-PAGE-NO");
    }

    public void setWkPageNo(int value) {
        setInt("WK-PAGE-NO", value);
    }

    public int getWkPageTop() {
        return getInt("WK-PAGE-TOP");
    }

    public void setWkPageTop(int value) {
        setInt("WK-PAGE-TOP", value);
    }

    public int getWkPgsize() {
        return getInt("WK-PGSIZE");
    }

    public void setWkPgsize(int value) {
        setInt("WK-PGSIZE", value);
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

    public int getWkRemain() {
        return getInt("WK-REMAIN");
    }

    public void setWkRemain(int value) {
        setInt("WK-REMAIN", value);
    }

    public long getWkSelNo() {
        return getLong("WK-SEL-NO");
    }

    public void setWkSelNo(long value) {
        setLong("WK-SEL-NO", value);
    }

    public int getWkShipCnt() {
        return getInt("WK-SHIP-CNT");
    }

    public void setWkShipCnt(int value) {
        setInt("WK-SHIP-CNT", value);
    }

    public int getWkShipEnd() {
        return getInt("WK-SHIP-END");
    }

    public void setWkShipEnd(int value) {
        setInt("WK-SHIP-END", value);
    }

    public long getWkShipNo() {
        return getLong("WK-SHIP-NO");
    }

    public void setWkShipNo(long value) {
        setLong("WK-SHIP-NO", value);
    }

    public int getWkShipq() {
        return getInt("WK-SHIPQ");
    }

    public void setWkShipq(int value) {
        setInt("WK-SHIPQ", value);
    }

    public String getWkStatText() {
        return getString("WK-STAT-TEXT");
    }

    public void setWkStatText(String value) {
        setString("WK-STAT-TEXT", value);
    }

    public int getWkStkFound() {
        return getInt("WK-STK-FOUND");
    }

    public void setWkStkFound(int value) {
        setInt("WK-STK-FOUND", value);
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

    public long getWkTotAmt() {
        return getLong("WK-TOT-AMT");
    }

    public void setWkTotAmt(long value) {
        setLong("WK-TOT-AMT", value);
    }

    public long getWkTotShip() {
        return getLong("WK-TOT-SHIP");
    }

    public void setWkTotShip(long value) {
        setLong("WK-TOT-SHIP", value);
    }

    public int getWkUserCode() {
        return getInt("WK-USER-CODE");
    }

    public void setWkUserCode(int value) {
        setInt("WK-USER-CODE", value);
    }

    public int getWkXline() {
        return getInt("WK-XLINE");
    }

    public void setWkXline(int value) {
        setInt("WK-XLINE", value);
    }

    public int getWwAlloc(int index) {
        return getInt("WW-ALLOC", index);
    }

    public void setWwAlloc(int index, int value) {
        setInt("WW-ALLOC", value, index);
    }

    public int getWwLine(int index) {
        return getInt("WW-LINE", index);
    }

    public void setWwLine(int index, int value) {
        setInt("WW-LINE", value, index);
    }

    public String getWwName(int index) {
        return getString("WW-NAME", index);
    }

    public void setWwName(int index, String value) {
        setString("WW-NAME", value, index);
    }

    public int getWwOrd(int index) {
        return getInt("WW-ORD", index);
    }

    public void setWwOrd(int index, int value) {
        setInt("WW-ORD", value, index);
    }

    public int getWwProd(int index) {
        return getInt("WW-PROD", index);
    }

    public void setWwProd(int index, int value) {
        setInt("WW-PROD", value, index);
    }

    public int getWwShip(int index) {
        return getInt("WW-SHIP", index);
    }

    public void setWwShip(int index, int value) {
        setInt("WW-SHIP", value, index);
    }

    public int getWwShipped(int index) {
        return getInt("WW-SHIPPED", index);
    }

    public void setWwShipped(int index, int value) {
        setInt("WW-SHIPPED", value, index);
    }

    public BigDecimal getXdAmount() {
        return getDecimal("XD-AMOUNT");
    }

    public void setXdAmount(BigDecimal value) {
        setDecimal("XD-AMOUNT", value);
    }

    public int getXdLine() {
        return getInt("XD-LINE");
    }

    public void setXdLine(int value) {
        setInt("XD-LINE", value);
    }

    public long getXdNo() {
        return getLong("XD-NO");
    }

    public void setXdNo(long value) {
        setLong("XD-NO", value);
    }

    public long getXdOrder() {
        return getLong("XD-ORDER");
    }

    public void setXdOrder(long value) {
        setLong("XD-ORDER", value);
    }

    public int getXdOrderLine() {
        return getInt("XD-ORDER-LINE");
    }

    public void setXdOrderLine(int value) {
        setInt("XD-ORDER-LINE", value);
    }

    public int getXdProd() {
        return getInt("XD-PROD");
    }

    public void setXdProd(int value) {
        setInt("XD-PROD", value);
    }

    public BigDecimal getXdQty() {
        return getDecimal("XD-QTY");
    }

    public void setXdQty(BigDecimal value) {
        setDecimal("XD-QTY", value);
    }

    public BigDecimal getXdUnitCost() {
        return getDecimal("XD-UNIT-COST");
    }

    public void setXdUnitCost(BigDecimal value) {
        setDecimal("XD-UNIT-COST", value);
    }

    public BigDecimal getXdUnitPrice() {
        return getDecimal("XD-UNIT-PRICE");
    }

    public void setXdUnitPrice(BigDecimal value) {
        setDecimal("XD-UNIT-PRICE", value);
    }

    public int getXdWhse() {
        return getInt("XD-WHSE");
    }

    public void setXdWhse(int value) {
        setInt("XD-WHSE", value);
    }

    public int getXhAddDate() {
        return getInt("XH-ADD-DATE");
    }

    public void setXhAddDate(int value) {
        setInt("XH-ADD-DATE", value);
    }

    public int getXhAddUser() {
        return getInt("XH-ADD-USER");
    }

    public void setXhAddUser(int value) {
        setInt("XH-ADD-USER", value);
    }

    public int getXhCust() {
        return getInt("XH-CUST");
    }

    public void setXhCust(int value) {
        setInt("XH-CUST", value);
    }

    public int getXhDate() {
        return getInt("XH-DATE");
    }

    public void setXhDate(int value) {
        setInt("XH-DATE", value);
    }

    public int getXhDelFlag() {
        return getInt("XH-DEL-FLAG");
    }

    public void setXhDelFlag(int value) {
        setInt("XH-DEL-FLAG", value);
    }

    public int getXhLines() {
        return getInt("XH-LINES");
    }

    public void setXhLines(int value) {
        setInt("XH-LINES", value);
    }

    public long getXhNo() {
        return getLong("XH-NO");
    }

    public void setXhNo(long value) {
        setLong("XH-NO", value);
    }

    public long getXhOrder() {
        return getLong("XH-ORDER");
    }

    public void setXhOrder(long value) {
        setLong("XH-ORDER", value);
    }

    public String getXhRemark() {
        return getString("XH-REMARK");
    }

    public void setXhRemark(String value) {
        setString("XH-REMARK", value);
    }

    public int getXhStaff() {
        return getInt("XH-STAFF");
    }

    public void setXhStaff(int value) {
        setInt("XH-STAFF", value);
    }

    public int getXhStatus() {
        return getInt("XH-STATUS");
    }

    public void setXhStatus(int value) {
        setInt("XH-STATUS", value);
    }

    public int getXhWhse() {
        return getInt("XH-WHSE");
    }

    public void setXhWhse(int value) {
        setInt("XH-WHSE", value);
    }
}

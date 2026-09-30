package com.sakura.pu0040.domain;

import com.sakura.pu0040.runtime.Pu0040Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for PU0040. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Pu0040FieldAccess extends RuntimeFieldAccess {

    public Pu0040FieldAccess(WorkingStorage ws, Pu0040Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getPurhf() != null) {
            register(fileSet.getPurhf().buffer());
        }
        if (fileSet != null && fileSet.getPurdf() != null) {
            register(fileSet.getPurdf().buffer());
        }
        if (fileSet != null && fileSet.getAplf() != null) {
            register(fileSet.getAplf().buffer());
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

    public int getDtlDone() {
        return getInt("DTL-DONE");
    }

    public void setDtlDone(int value) {
        setInt("DTL-DONE", value);
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

    public int getHdrOk() {
        return getInt("HDR-OK");
    }

    public void setHdrOk(int value) {
        setInt("HDR-OK", value);
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

    public BigDecimal getKtAmount() {
        return getDecimal("KT-AMOUNT");
    }

    public void setKtAmount(BigDecimal value) {
        setDecimal("KT-AMOUNT", value);
    }

    public int getKtCategory() {
        return getInt("KT-CATEGORY");
    }

    public void setKtCategory(int value) {
        setInt("KT-CATEGORY", value);
    }

    public int getKtDate() {
        return getInt("KT-DATE");
    }

    public void setKtDate(int value) {
        setInt("KT-DATE", value);
    }

    public BigDecimal getKtGross() {
        return getDecimal("KT-GROSS");
    }

    public void setKtGross(BigDecimal value) {
        setDecimal("KT-GROSS", value);
    }

    public BigDecimal getKtNet() {
        return getDecimal("KT-NET");
    }

    public void setKtNet(BigDecimal value) {
        setDecimal("KT-NET", value);
    }

    public BigDecimal getKtRate() {
        return getDecimal("KT-RATE");
    }

    public void setKtRate(BigDecimal value) {
        setDecimal("KT-RATE", value);
    }

    public int getKtRound() {
        return getInt("KT-ROUND");
    }

    public void setKtRound(int value) {
        setInt("KT-ROUND", value);
    }

    public String getKtStatus() {
        return getString("KT-STATUS");
    }

    public void setKtStatus(String value) {
        setString("KT-STATUS", value);
    }

    public BigDecimal getKtTax() {
        return getDecimal("KT-TAX");
    }

    public void setKtTax(BigDecimal value) {
        setDecimal("KT-TAX", value);
    }

    public int getKtTaxType() {
        return getInt("KT-TAX-TYPE");
    }

    public void setKtTaxType(int value) {
        setInt("KT-TAX-TYPE", value);
    }

    public String getKtax() {
        return groupToString("KTAX");
    }

    public void setKtax(String value) {
        setGroup("KTAX", value);
    }

    public BigDecimal getPlBalance() {
        return getDecimal("PL-BALANCE");
    }

    public void setPlBalance(BigDecimal value) {
        setDecimal("PL-BALANCE", value);
    }

    public int getPlCloseYm() {
        return getInt("PL-CLOSE-YM");
    }

    public void setPlCloseYm(int value) {
        setInt("PL-CLOSE-YM", value);
    }

    public BigDecimal getPlCredit() {
        return getDecimal("PL-CREDIT");
    }

    public void setPlCredit(BigDecimal value) {
        setDecimal("PL-CREDIT", value);
    }

    public int getPlDate() {
        return getInt("PL-DATE");
    }

    public void setPlDate(int value) {
        setInt("PL-DATE", value);
    }

    public BigDecimal getPlDebit() {
        return getDecimal("PL-DEBIT");
    }

    public void setPlDebit(BigDecimal value) {
        setDecimal("PL-DEBIT", value);
    }

    public int getPlKind() {
        return getInt("PL-KIND");
    }

    public void setPlKind(int value) {
        setInt("PL-KIND", value);
    }

    public long getPlRefNo() {
        return getLong("PL-REF-NO");
    }

    public void setPlRefNo(long value) {
        setLong("PL-REF-NO", value);
    }

    public int getPlRefType() {
        return getInt("PL-REF-TYPE");
    }

    public void setPlRefType(int value) {
        setInt("PL-REF-TYPE", value);
    }

    public String getPlRemark() {
        return getString("PL-REMARK");
    }

    public void setPlRemark(String value) {
        setString("PL-REMARK", value);
    }

    public long getPlSeq() {
        return getLong("PL-SEQ");
    }

    public void setPlSeq(long value) {
        setLong("PL-SEQ", value);
    }

    public int getPlSupp() {
        return getInt("PL-SUPP");
    }

    public void setPlSupp(int value) {
        setInt("PL-SUPP", value);
    }

    public int getPlUser() {
        return getInt("PL-USER");
    }

    public void setPlUser(int value) {
        setInt("PL-USER", value);
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

    public int getPrDfltWhse() {
        return getInt("PR-DFLT-WHSE");
    }

    public void setPrDfltWhse(int value) {
        setInt("PR-DFLT-WHSE", value);
    }

    public BigDecimal getPrLastCost() {
        return getDecimal("PR-LAST-COST");
    }

    public void setPrLastCost(BigDecimal value) {
        setDecimal("PR-LAST-COST", value);
    }

    public String getPrName() {
        return getString("PR-NAME");
    }

    public void setPrName(String value) {
        setString("PR-NAME", value);
    }

    public BigDecimal getPrStdCost() {
        return getDecimal("PR-STD-COST");
    }

    public void setPrStdCost(BigDecimal value) {
        setDecimal("PR-STD-COST", value);
    }

    public int getPrStockMng() {
        return getInt("PR-STOCK-MNG");
    }

    public void setPrStockMng(int value) {
        setInt("PR-STOCK-MNG", value);
    }

    public int getPrTaxCategory() {
        return getInt("PR-TAX-CATEGORY");
    }

    public void setPrTaxCategory(int value) {
        setInt("PR-TAX-CATEGORY", value);
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

    public BigDecimal getSpBalance() {
        return getDecimal("SP-BALANCE");
    }

    public void setSpBalance(BigDecimal value) {
        setDecimal("SP-BALANCE", value);
    }

    public int getSpCode() {
        return getInt("SP-CODE");
    }

    public void setSpCode(int value) {
        setInt("SP-CODE", value);
    }

    public int getSpDelFlag() {
        return getInt("SP-DEL-FLAG");
    }

    public void setSpDelFlag(int value) {
        setInt("SP-DEL-FLAG", value);
    }

    public String getSpName() {
        return getString("SP-NAME");
    }

    public void setSpName(String value) {
        setString("SP-NAME", value);
    }

    public int getSpTaxType() {
        return getInt("SP-TAX-TYPE");
    }

    public void setSpTaxType(int value) {
        setInt("SP-TAX-TYPE", value);
    }

    public int getSpUpdDate() {
        return getInt("SP-UPD-DATE");
    }

    public void setSpUpdDate(int value) {
        setInt("SP-UPD-DATE", value);
    }

    public int getSpUpdUser() {
        return getInt("SP-UPD-USER");
    }

    public void setSpUpdUser(int value) {
        setInt("SP-UPD-USER", value);
    }

    public BigDecimal getVdAmount() {
        return getDecimal("VD-AMOUNT");
    }

    public void setVdAmount(BigDecimal value) {
        setDecimal("VD-AMOUNT", value);
    }

    public int getVdLine() {
        return getInt("VD-LINE");
    }

    public void setVdLine(int value) {
        setInt("VD-LINE", value);
    }

    public long getVdNo() {
        return getLong("VD-NO");
    }

    public void setVdNo(long value) {
        setLong("VD-NO", value);
    }

    public int getVdProd() {
        return getInt("VD-PROD");
    }

    public void setVdProd(int value) {
        setInt("VD-PROD", value);
    }

    public BigDecimal getVdQty() {
        return getDecimal("VD-QTY");
    }

    public void setVdQty(BigDecimal value) {
        setDecimal("VD-QTY", value);
    }

    public int getVdTaxCategory() {
        return getInt("VD-TAX-CATEGORY");
    }

    public void setVdTaxCategory(int value) {
        setInt("VD-TAX-CATEGORY", value);
    }

    public BigDecimal getVdUnitCost() {
        return getDecimal("VD-UNIT-COST");
    }

    public void setVdUnitCost(BigDecimal value) {
        setDecimal("VD-UNIT-COST", value);
    }

    public int getVdWhse() {
        return getInt("VD-WHSE");
    }

    public void setVdWhse(int value) {
        setInt("VD-WHSE", value);
    }

    public int getVhAddDate() {
        return getInt("VH-ADD-DATE");
    }

    public void setVhAddDate(int value) {
        setInt("VH-ADD-DATE", value);
    }

    public int getVhAddUser() {
        return getInt("VH-ADD-USER");
    }

    public void setVhAddUser(int value) {
        setInt("VH-ADD-USER", value);
    }

    public BigDecimal getVhAmount() {
        return getDecimal("VH-AMOUNT");
    }

    public void setVhAmount(BigDecimal value) {
        setDecimal("VH-AMOUNT", value);
    }

    public int getVhCloseYm() {
        return getInt("VH-CLOSE-YM");
    }

    public void setVhCloseYm(int value) {
        setInt("VH-CLOSE-YM", value);
    }

    public int getVhDate() {
        return getInt("VH-DATE");
    }

    public void setVhDate(int value) {
        setInt("VH-DATE", value);
    }

    public int getVhDelFlag() {
        return getInt("VH-DEL-FLAG");
    }

    public void setVhDelFlag(int value) {
        setInt("VH-DEL-FLAG", value);
    }

    public int getVhKind() {
        return getInt("VH-KIND");
    }

    public void setVhKind(int value) {
        setInt("VH-KIND", value);
    }

    public int getVhLines() {
        return getInt("VH-LINES");
    }

    public void setVhLines(int value) {
        setInt("VH-LINES", value);
    }

    public long getVhNo() {
        return getLong("VH-NO");
    }

    public void setVhNo(long value) {
        setLong("VH-NO", value);
    }

    public long getVhRecvNo() {
        return getLong("VH-RECV-NO");
    }

    public void setVhRecvNo(long value) {
        setLong("VH-RECV-NO", value);
    }

    public String getVhRemark() {
        return getString("VH-REMARK");
    }

    public void setVhRemark(String value) {
        setString("VH-REMARK", value);
    }

    public int getVhStatus() {
        return getInt("VH-STATUS");
    }

    public void setVhStatus(int value) {
        setInt("VH-STATUS", value);
    }

    public int getVhSupp() {
        return getInt("VH-SUPP");
    }

    public void setVhSupp(int value) {
        setInt("VH-SUPP", value);
    }

    public BigDecimal getVhTaxAmount() {
        return getDecimal("VH-TAX-AMOUNT");
    }

    public void setVhTaxAmount(BigDecimal value) {
        setDecimal("VH-TAX-AMOUNT", value);
    }

    public int getVhTaxType() {
        return getInt("VH-TAX-TYPE");
    }

    public void setVhTaxType(int value) {
        setInt("VH-TAX-TYPE", value);
    }

    public BigDecimal getVhTotal() {
        return getDecimal("VH-TOTAL");
    }

    public void setVhTotal(BigDecimal value) {
        setDecimal("VH-TOTAL", value);
    }

    public String getWkConfirm() {
        return getString("WK-CONFIRM");
    }

    public void setWkConfirm(String value) {
        setString("WK-CONFIRM", value);
    }

    public long getWkDAmt() {
        return getLong("WK-D-AMT");
    }

    public void setWkDAmt(long value) {
        setLong("WK-D-AMT", value);
    }

    public int getWkDAvail() {
        return getInt("WK-D-AVAIL");
    }

    public void setWkDAvail(int value) {
        setInt("WK-D-AVAIL", value);
    }

    public BigDecimal getWkDCost() {
        return getDecimal("WK-D-COST");
    }

    public void setWkDCost(BigDecimal value) {
        setDecimal("WK-D-COST", value);
    }

    public String getWkDName() {
        return getString("WK-D-NAME");
    }

    public void setWkDName(String value) {
        setString("WK-D-NAME", value);
    }

    public int getWkDProd() {
        return getInt("WK-D-PROD");
    }

    public void setWkDProd(int value) {
        setInt("WK-D-PROD", value);
    }

    public int getWkDQty() {
        return getInt("WK-D-QTY");
    }

    public void setWkDQty(int value) {
        setInt("WK-D-QTY", value);
    }

    public int getWkDStkmng() {
        return getInt("WK-D-STKMNG");
    }

    public void setWkDStkmng(int value) {
        setInt("WK-D-STKMNG", value);
    }

    public int getWkDTaxcat() {
        return getInt("WK-D-TAXCAT");
    }

    public void setWkDTaxcat(int value) {
        setInt("WK-D-TAXCAT", value);
    }

    public int getWkDWhse() {
        return getInt("WK-D-WHSE");
    }

    public void setWkDWhse(int value) {
        setInt("WK-D-WHSE", value);
    }

    public String getWkDet() {
        return groupToString("WK-DET");
    }

    public void setWkDet(String value) {
        setGroup("WK-DET", value);
    }

    public String getWkFkeyLine() {
        return getString("WK-FKEY-LINE");
    }

    public void setWkFkeyLine(String value) {
        setString("WK-FKEY-LINE", value);
    }

    public long getWkGrsTotal() {
        return getLong("WK-GRS-TOTAL");
    }

    public void setWkGrsTotal(long value) {
        setLong("WK-GRS-TOTAL", value);
    }

    public int getWkIdx() {
        return getInt("WK-IDX");
    }

    public void setWkIdx(int value) {
        setInt("WK-IDX", value);
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

    public long getWkNetTotal() {
        return getLong("WK-NET-TOTAL");
    }

    public void setWkNetTotal(long value) {
        setLong("WK-NET-TOTAL", value);
    }

    public long getWkNewBal() {
        return getLong("WK-NEW-BAL");
    }

    public void setWkNewBal(long value) {
        setLong("WK-NEW-BAL", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
    }

    public String getWkSuppName() {
        return getString("WK-SUPP-NAME");
    }

    public void setWkSuppName(String value) {
        setString("WK-SUPP-NAME", value);
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

    public long getWkTaxTotal() {
        return getLong("WK-TAX-TOTAL");
    }

    public void setWkTaxTotal(long value) {
        setLong("WK-TAX-TOTAL", value);
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

    public long getWkVhNoD() {
        return getLong("WK-VH-NO-D");
    }

    public void setWkVhNoD(long value) {
        setLong("WK-VH-NO-D", value);
    }

    public long getWlAmount(int index) {
        return getLong("WL-AMOUNT", index);
    }

    public void setWlAmount(int index, long value) {
        setLong("WL-AMOUNT", value, index);
    }

    public BigDecimal getWlCost(int index) {
        return getDecimal("WL-COST", index);
    }

    public void setWlCost(int index, BigDecimal value) {
        setDecimal("WL-COST", value, index);
    }

    public int getWlProd(int index) {
        return getInt("WL-PROD", index);
    }

    public void setWlProd(int index, int value) {
        setInt("WL-PROD", value, index);
    }

    public int getWlQty(int index) {
        return getInt("WL-QTY", index);
    }

    public void setWlQty(int index, int value) {
        setInt("WL-QTY", value, index);
    }

    public int getWlStkmng(int index) {
        return getInt("WL-STKMNG", index);
    }

    public void setWlStkmng(int index, int value) {
        setInt("WL-STKMNG", value, index);
    }

    public int getWlTaxcat(int index) {
        return getInt("WL-TAXCAT", index);
    }

    public void setWlTaxcat(int index, int value) {
        setInt("WL-TAXCAT", value, index);
    }

    public int getWlWhse(int index) {
        return getInt("WL-WHSE", index);
    }

    public void setWlWhse(int index, int value) {
        setInt("WL-WHSE", value, index);
    }

    /* ── Synthetic int wrappers (INDEX BY counters, unresolved symbols) ── */
    public int getLx() {
        return getInt("LX");
    }

    public void setLx(int value) {
        setInt("LX", value);
    }
}

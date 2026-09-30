package com.sakura.pu0010.domain;

import com.sakura.pu0010.runtime.Pu0010Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for PU0010. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Pu0010FieldAccess extends RuntimeFieldAccess {

    public Pu0010FieldAccess(WorkingStorage ws, Pu0010Datasets fileSet) {
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

    public BigDecimal getPdAmount() {
        return getDecimal("PD-AMOUNT");
    }

    public void setPdAmount(BigDecimal value) {
        setDecimal("PD-AMOUNT", value);
    }

    public int getPdDueDate() {
        return getInt("PD-DUE-DATE");
    }

    public void setPdDueDate(int value) {
        setInt("PD-DUE-DATE", value);
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

    public int getPhAddDate() {
        return getInt("PH-ADD-DATE");
    }

    public void setPhAddDate(int value) {
        setInt("PH-ADD-DATE", value);
    }

    public int getPhAddUser() {
        return getInt("PH-ADD-USER");
    }

    public void setPhAddUser(int value) {
        setInt("PH-ADD-USER", value);
    }

    public BigDecimal getPhAmount() {
        return getDecimal("PH-AMOUNT");
    }

    public void setPhAmount(BigDecimal value) {
        setDecimal("PH-AMOUNT", value);
    }

    public int getPhDate() {
        return getInt("PH-DATE");
    }

    public void setPhDate(int value) {
        setInt("PH-DATE", value);
    }

    public int getPhDelFlag() {
        return getInt("PH-DEL-FLAG");
    }

    public void setPhDelFlag(int value) {
        setInt("PH-DEL-FLAG", value);
    }

    public int getPhDueDate() {
        return getInt("PH-DUE-DATE");
    }

    public void setPhDueDate(int value) {
        setInt("PH-DUE-DATE", value);
    }

    public int getPhLines() {
        return getInt("PH-LINES");
    }

    public void setPhLines(int value) {
        setInt("PH-LINES", value);
    }

    public long getPhNo() {
        return getLong("PH-NO");
    }

    public void setPhNo(long value) {
        setLong("PH-NO", value);
    }

    public String getPhRemark() {
        return getString("PH-REMARK");
    }

    public void setPhRemark(String value) {
        setString("PH-REMARK", value);
    }

    public int getPhStaff() {
        return getInt("PH-STAFF");
    }

    public void setPhStaff(int value) {
        setInt("PH-STAFF", value);
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

    public BigDecimal getPhTaxAmount() {
        return getDecimal("PH-TAX-AMOUNT");
    }

    public void setPhTaxAmount(BigDecimal value) {
        setDecimal("PH-TAX-AMOUNT", value);
    }

    public int getPhTaxType() {
        return getInt("PH-TAX-TYPE");
    }

    public void setPhTaxType(int value) {
        setInt("PH-TAX-TYPE", value);
    }

    public BigDecimal getPhTotal() {
        return getDecimal("PH-TOTAL");
    }

    public void setPhTotal(BigDecimal value) {
        setDecimal("PH-TOTAL", value);
    }

    public int getPhWhse() {
        return getInt("PH-WHSE");
    }

    public void setPhWhse(int value) {
        setInt("PH-WHSE", value);
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

    public BigDecimal getSkOnOrder() {
        return getDecimal("SK-ON-ORDER");
    }

    public void setSkOnOrder(BigDecimal value) {
        setDecimal("SK-ON-ORDER", value);
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

    public long getWkPoNoD() {
        return getLong("WK-PO-NO-D");
    }

    public void setWkPoNoD(long value) {
        setLong("WK-PO-NO-D", value);
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

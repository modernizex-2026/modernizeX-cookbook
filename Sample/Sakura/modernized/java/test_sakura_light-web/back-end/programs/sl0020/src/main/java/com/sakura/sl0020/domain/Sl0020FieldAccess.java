package com.sakura.sl0020.domain;

import com.sakura.runtime.record.RuntimeFieldAccess;
import com.sakura.sl0020.runtime.Sl0020Datasets;

import java.math.BigDecimal;

/**
 * Field accessor for SL0020. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Sl0020FieldAccess extends RuntimeFieldAccess {

    public Sl0020FieldAccess(WorkingStorage ws, Sl0020Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getInvhf() != null) {
            register(fileSet.getInvhf().buffer());
        }
        if (fileSet != null && fileSet.getInvdf() != null) {
            register(fileSet.getInvdf().buffer());
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

    public long getDrAmt(int index) {
        return getLong("DR-AMT", index);
    }

    public void setDrAmt(int index, long value) {
        setLong("DR-AMT", value, index);
    }

    public long getDrCostamt(int index) {
        return getLong("DR-COSTAMT", index);
    }

    public void setDrCostamt(int index, long value) {
        setLong("DR-COSTAMT", value, index);
    }

    public int getDrLine(int index) {
        return getInt("DR-LINE", index);
    }

    public void setDrLine(int index, int value) {
        setInt("DR-LINE", value, index);
    }

    public long getDrMargin(int index) {
        return getLong("DR-MARGIN", index);
    }

    public void setDrMargin(int index, long value) {
        setLong("DR-MARGIN", value, index);
    }

    public String getDrName(int index) {
        return getString("DR-NAME", index);
    }

    public void setDrName(int index, String value) {
        setString("DR-NAME", value, index);
    }

    public int getDrProd(int index) {
        return getInt("DR-PROD", index);
    }

    public void setDrProd(int index, int value) {
        setInt("DR-PROD", value, index);
    }

    public int getDrQty(int index) {
        return getInt("DR-QTY", index);
    }

    public void setDrQty(int index, int value) {
        setInt("DR-QTY", value, index);
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

    public BigDecimal getIdAmount() {
        return getDecimal("ID-AMOUNT");
    }

    public void setIdAmount(BigDecimal value) {
        setDecimal("ID-AMOUNT", value);
    }

    public BigDecimal getIdCostAmount() {
        return getDecimal("ID-COST-AMOUNT");
    }

    public void setIdCostAmount(BigDecimal value) {
        setDecimal("ID-COST-AMOUNT", value);
    }

    public int getIdLine() {
        return getInt("ID-LINE");
    }

    public void setIdLine(int value) {
        setInt("ID-LINE", value);
    }

    public long getIdNo() {
        return getLong("ID-NO");
    }

    public void setIdNo(long value) {
        setLong("ID-NO", value);
    }

    public int getIdProd() {
        return getInt("ID-PROD");
    }

    public void setIdProd(int value) {
        setInt("ID-PROD", value);
    }

    public BigDecimal getIdQty() {
        return getDecimal("ID-QTY");
    }

    public void setIdQty(BigDecimal value) {
        setDecimal("ID-QTY", value);
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

    public int getIhCust() {
        return getInt("IH-CUST");
    }

    public void setIhCust(int value) {
        setInt("IH-CUST", value);
    }

    public int getIhDate() {
        return getInt("IH-DATE");
    }

    public void setIhDate(int value) {
        setInt("IH-DATE", value);
    }

    public int getIhDelFlag() {
        return getInt("IH-DEL-FLAG");
    }

    public void setIhDelFlag(int value) {
        setInt("IH-DEL-FLAG", value);
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

    public long getWhNo(int index) {
        return getLong("WH-NO", index);
    }

    public void setWhNo(int index, long value) {
        setLong("WH-NO", value, index);
    }

    public int getWkBrowseEnd() {
        return getInt("WK-BROWSE-END");
    }

    public void setWkBrowseEnd(int value) {
        setInt("WK-BROWSE-END", value);
    }

    public long getWkCurNo() {
        return getLong("WK-CUR-NO");
    }

    public void setWkCurNo(long value) {
        setLong("WK-CUR-NO", value);
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

    public String getWkDummy() {
        return getString("WK-DUMMY");
    }

    public void setWkDummy(String value) {
        setString("WK-DUMMY", value);
    }

    public String getWkFkeyLine() {
        return getString("WK-FKEY-LINE");
    }

    public void setWkFkeyLine(String value) {
        setString("WK-FKEY-LINE", value);
    }

    public int getWkHcnt() {
        return getInt("WK-HCNT");
    }

    public void setWkHcnt(int value) {
        setInt("WK-HCNT", value);
    }

    public int getWkHpos() {
        return getInt("WK-HPOS");
    }

    public void setWkHpos(int value) {
        setInt("WK-HPOS", value);
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

    public String getWkKindText() {
        return getString("WK-KIND-TEXT");
    }

    public void setWkKindText(String value) {
        setString("WK-KIND-TEXT", value);
    }

    public long getWkMarginTot() {
        return getLong("WK-MARGIN-TOT");
    }

    public void setWkMarginTot(long value) {
        setLong("WK-MARGIN-TOT", value);
    }

    public int getWkMode() {
        return getInt("WK-MODE");
    }

    public void setWkMode(int value) {
        setInt("WK-MODE", value);
    }

    public String getWkMsgLine() {
        return getString("WK-MSG-LINE");
    }

    public void setWkMsgLine(String value) {
        setString("WK-MSG-LINE", value);
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

    public int getWkSelCust() {
        return getInt("WK-SEL-CUST");
    }

    public void setWkSelCust(int value) {
        setInt("WK-SEL-CUST", value);
    }

    public int getWkSelDate() {
        return getInt("WK-SEL-DATE");
    }

    public void setWkSelDate(int value) {
        setInt("WK-SEL-DATE", value);
    }

    public long getWkSelNo() {
        return getLong("WK-SEL-NO");
    }

    public void setWkSelNo(long value) {
        setLong("WK-SEL-NO", value);
    }

    public String getWkStatText() {
        return getString("WK-STAT-TEXT");
    }

    public void setWkStatText(String value) {
        setString("WK-STAT-TEXT", value);
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

    public long getWwAmt(int index) {
        return getLong("WW-AMT", index);
    }

    public void setWwAmt(int index, long value) {
        setLong("WW-AMT", value, index);
    }

    public long getWwCostamt(int index) {
        return getLong("WW-COSTAMT", index);
    }

    public void setWwCostamt(int index, long value) {
        setLong("WW-COSTAMT", value, index);
    }

    public int getWwLine(int index) {
        return getInt("WW-LINE", index);
    }

    public void setWwLine(int index, int value) {
        setInt("WW-LINE", value, index);
    }

    public int getWwMargin(int index) {
        return getInt("WW-MARGIN", index);
    }

    public void setWwMargin(int index, int value) {
        setInt("WW-MARGIN", value, index);
    }

    public String getWwName(int index) {
        return getString("WW-NAME", index);
    }

    public void setWwName(int index, String value) {
        setString("WW-NAME", value, index);
    }

    public int getWwProd(int index) {
        return getInt("WW-PROD", index);
    }

    public void setWwProd(int index, int value) {
        setInt("WW-PROD", value, index);
    }

    public int getWwQty(int index) {
        return getInt("WW-QTY", index);
    }

    public void setWwQty(int index, int value) {
        setInt("WW-QTY", value, index);
    }
}

package com.generated.orion.outrnin.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.outrnin.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OUTRNIN. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OutrninFields extends DynamicFieldAccessor {

    public OutrninFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public BigDecimal getKtiAmtThresh() {
        return getDecimal("KTI-AMT-THRESH");
    }

    public void setKtiAmtThresh(BigDecimal value) {
        setDecimal("KTI-AMT-THRESH", value);
    }

    public String getKtiCard() {
        return getString("KTI-CARD");
    }

    public void setKtiCard(String value) {
        setString("KTI-CARD", value);
    }

    public String getKtiDateFrom() {
        return getString("KTI-DATE-FROM");
    }

    public void setKtiDateFrom(String value) {
        setString("KTI-DATE-FROM", value);
    }

    public String getKtiDateTo() {
        return getString("KTI-DATE-TO");
    }

    public void setKtiDateTo(String value) {
        setString("KTI-DATE-TO", value);
    }

    public int getKtiFCat() {
        return getInt("KTI-F-CAT");
    }

    public void setKtiFCat(int value) {
        setInt("KTI-F-CAT", value);
    }

    public String getKtiFType() {
        return getString("KTI-F-TYPE");
    }

    public void setKtiFType(String value) {
        setString("KTI-F-TYPE", value);
    }

    public int getKtiFeeCnt() {
        return getInt("KTI-FEE-CNT");
    }

    public void setKtiFeeCnt(int value) {
        setInt("KTI-FEE-CNT", value);
    }

    public BigDecimal getKtiFeeSum() {
        return getDecimal("KTI-FEE-SUM");
    }

    public void setKtiFeeSum(BigDecimal value) {
        setDecimal("KTI-FEE-SUM", value);
    }

    public String getKtiFilter() {
        return getString("KTI-FILTER");
    }

    public void setKtiFilter(String value) {
        setString("KTI-FILTER", value);
    }

    public int getKtiIntCnt() {
        return getInt("KTI-INT-CNT");
    }

    public void setKtiIntCnt(int value) {
        setInt("KTI-INT-CNT", value);
    }

    public BigDecimal getKtiIntSum() {
        return getDecimal("KTI-INT-SUM");
    }

    public void setKtiIntSum(BigDecimal value) {
        setDecimal("KTI-INT-SUM", value);
    }

    public int getKtiMatchCount() {
        return getInt("KTI-MATCH-COUNT");
    }

    public void setKtiMatchCount(int value) {
        setInt("KTI-MATCH-COUNT", value);
    }

    public BigDecimal getKtiMaxAmt() {
        return getDecimal("KTI-MAX-AMT");
    }

    public void setKtiMaxAmt(BigDecimal value) {
        setDecimal("KTI-MAX-AMT", value);
    }

    public String getKtiMaxId() {
        return getString("KTI-MAX-ID");
    }

    public void setKtiMaxId(String value) {
        setString("KTI-MAX-ID", value);
    }

    public int getKtiMerchId() {
        return getInt("KTI-MERCH-ID");
    }

    public void setKtiMerchId(int value) {
        setInt("KTI-MERCH-ID", value);
    }

    public BigDecimal getKtiNetTotal() {
        return getDecimal("KTI-NET-TOTAL");
    }

    public void setKtiNetTotal(BigDecimal value) {
        setDecimal("KTI-NET-TOTAL", value);
    }

    public String getKtiNextKey() {
        return getString("KTI-NEXT-KEY");
    }

    public void setKtiNextKey(String value) {
        setString("KTI-NEXT-KEY", value);
    }

    public int getKtiPayCnt() {
        return getInt("KTI-PAY-CNT");
    }

    public void setKtiPayCnt(int value) {
        setInt("KTI-PAY-CNT", value);
    }

    public BigDecimal getKtiPaySum() {
        return getDecimal("KTI-PAY-SUM");
    }

    public void setKtiPaySum(BigDecimal value) {
        setDecimal("KTI-PAY-SUM", value);
    }

    public int getKtiPurchCnt() {
        return getInt("KTI-PURCH-CNT");
    }

    public void setKtiPurchCnt(int value) {
        setInt("KTI-PURCH-CNT", value);
    }

    public BigDecimal getKtiPurchSum() {
        return getDecimal("KTI-PURCH-SUM");
    }

    public void setKtiPurchSum(BigDecimal value) {
        setDecimal("KTI-PURCH-SUM", value);
    }

    public int getKtiRowCount() {
        return getInt("KTI-ROW-COUNT");
    }

    public void setKtiRowCount(int value) {
        setInt("KTI-ROW-COUNT", value);
    }

    public String getKtiRows() {
        return groupToString("KTI-ROWS");
    }

    public void setKtiRows(String value) {
        setGroup("KTI-ROWS", value);
    }

    public int getKtiScanCount() {
        return getInt("KTI-SCAN-COUNT");
    }

    public void setKtiScanCount(int value) {
        setInt("KTI-SCAN-COUNT", value);
    }

    public String getKtiStartKey() {
        return getString("KTI-START-KEY");
    }

    public void setKtiStartKey(String value) {
        setString("KTI-START-KEY", value);
    }

    public BigDecimal getKtrAmt(int index) {
        return getDecimal("KTR-AMT", index);
    }

    public void setKtrAmt(int index, BigDecimal value) {
        setDecimal("KTR-AMT", value, index);
    }

    public String getKtrCard(int index) {
        return getString("KTR-CARD", index);
    }

    public void setKtrCard(int index, String value) {
        setString("KTR-CARD", value, index);
    }

    public String getKtrDate(int index) {
        return getString("KTR-DATE", index);
    }

    public void setKtrDate(int index, String value) {
        setString("KTR-DATE", value, index);
    }

    public String getKtrDesc(int index) {
        return getString("KTR-DESC", index);
    }

    public void setKtrDesc(int index, String value) {
        setString("KTR-DESC", value, index);
    }

    public String getKtrId(int index) {
        return getString("KTR-ID", index);
    }

    public void setKtrId(int index, String value) {
        setString("KTR-ID", value, index);
    }

    public String getKtrMerch(int index) {
        return getString("KTR-MERCH", index);
    }

    public void setKtrMerch(int index, String value) {
        setString("KTR-MERCH", value, index);
    }

    public String getKtrTycat(int index) {
        return getString("KTR-TYCAT", index);
    }

    public void setKtrTycat(int index, String value) {
        setString("KTR-TYCAT", value, index);
    }

    public String getKtrninArea() {
        return groupToString("KTRNIN-AREA");
    }

    public void setKtrninArea(String value) {
        setGroup("KTRNIN-AREA", value);
    }

    public int getTcCd() {
        return getInt("TC-CD");
    }

    public void setTcCd(int value) {
        setInt("TC-CD", value);
    }

    public String getTcDesc() {
        return getString("TC-DESC");
    }

    public void setTcDesc(String value) {
        setString("TC-DESC", value);
    }

    public String getTcKey() {
        return groupToString("TC-KEY");
    }

    public void setTcKey(String value) {
        setGroup("TC-KEY", value);
    }

    public String getTcTypeCd() {
        return getString("TC-TYPE-CD");
    }

    public void setTcTypeCd(String value) {
        setString("TC-TYPE-CD", value);
    }

    public BigDecimal getTrAmt() {
        return getDecimal("TR-AMT");
    }

    public void setTrAmt(BigDecimal value) {
        setDecimal("TR-AMT", value);
    }

    public String getTrCardNum() {
        return getString("TR-CARD-NUM");
    }

    public void setTrCardNum(String value) {
        setString("TR-CARD-NUM", value);
    }

    public int getTrCatCd() {
        return getInt("TR-CAT-CD");
    }

    public void setTrCatCd(int value) {
        setInt("TR-CAT-CD", value);
    }

    public String getTrId() {
        return getString("TR-ID");
    }

    public void setTrId(String value) {
        setString("TR-ID", value);
    }

    public int getTrMerchantId() {
        return getInt("TR-MERCHANT-ID");
    }

    public void setTrMerchantId(int value) {
        setInt("TR-MERCHANT-ID", value);
    }

    public String getTrMerchantName() {
        return getString("TR-MERCHANT-NAME");
    }

    public void setTrMerchantName(String value) {
        setString("TR-MERCHANT-NAME", value);
    }

    public String getTrProcTs() {
        return getString("TR-PROC-TS");
    }

    public void setTrProcTs(String value) {
        setString("TR-PROC-TS", value);
    }

    public String getTrTypeCd() {
        return getString("TR-TYPE-CD");
    }

    public void setTrTypeCd(String value) {
        setString("TR-TYPE-CD", value);
    }

    public String getTtCd() {
        return getString("TT-CD");
    }

    public void setTtCd(String value) {
        setString("TT-CD", value);
    }

    public String getTtDesc() {
        return getString("TT-DESC");
    }

    public void setTtDesc(String value) {
        setString("TT-DESC", value);
    }

    public int getWsCatEd() {
        return getInt("WS-CAT-ED");
    }

    public void setWsCatEd(int value) {
        setInt("WS-CAT-ED", value);
    }

    public String getWsDesc() {
        return getString("WS-DESC");
    }

    public void setWsDesc(String value) {
        setString("WS-DESC", value);
    }

    public String getWsEndFlg() {
        return getString("WS-END-FLG");
    }

    public void setWsEndFlg(String value) {
        setString("WS-END-FLG", value);
    }

    public String getWsFirstSw() {
        return getString("WS-FIRST-SW");
    }

    public void setWsFirstSw(String value) {
        setString("WS-FIRST-SW", value);
    }

    public String getWsLastKey() {
        return getString("WS-LAST-KEY");
    }

    public void setWsLastKey(String value) {
        setString("WS-LAST-KEY", value);
    }

    public String getWsMatchSw() {
        return getString("WS-MATCH-SW");
    }

    public void setWsMatchSw(String value) {
        setString("WS-MATCH-SW", value);
    }

    public int getWsMaxRows() {
        return getInt("WS-MAX-ROWS");
    }

    public void setWsMaxRows(int value) {
        setInt("WS-MAX-ROWS", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public String getWsTcatfile() {
        return getString("WS-TCATFILE");
    }

    public void setWsTcatfile(String value) {
        setString("WS-TCATFILE", value);
    }

    public String getWsTranfile() {
        return getString("WS-TRANFILE");
    }

    public void setWsTranfile(String value) {
        setString("WS-TRANFILE", value);
    }

    public String getWsTtypfile() {
        return getString("WS-TTYPFILE");
    }

    public void setWsTtypfile(String value) {
        setString("WS-TTYPFILE", value);
    }

    public String getWsTycat() {
        return getString("WS-TYCAT");
    }

    public void setWsTycat(String value) {
        setString("WS-TYCAT", value);
    }
}

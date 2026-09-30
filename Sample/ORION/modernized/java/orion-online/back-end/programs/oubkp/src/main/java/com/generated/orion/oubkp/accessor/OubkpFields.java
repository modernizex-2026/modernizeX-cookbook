package com.generated.orion.oubkp.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.oubkp.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OUBKP. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OubkpFields extends DynamicFieldAccessor {

    public OubkpFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public BigDecimal getBkAmt() {
        return getDecimal("BK-AMT");
    }

    public void setBkAmt(BigDecimal value) {
        setDecimal("BK-AMT", value);
    }

    public String getBkBar1() {
        return getString("BK-BAR-1");
    }

    public void setBkBar1(String value) {
        setString("BK-BAR-1", value);
    }

    public String getBkBar2() {
        return getString("BK-BAR-2");
    }

    public void setBkBar2(String value) {
        setString("BK-BAR-2", value);
    }

    public String getBkBar3() {
        return getString("BK-BAR-3");
    }

    public void setBkBar3(String value) {
        setString("BK-BAR-3", value);
    }

    public String getBkBar4() {
        return getString("BK-BAR-4");
    }

    public void setBkBar4(String value) {
        setString("BK-BAR-4", value);
    }

    public String getBkBar5() {
        return getString("BK-BAR-5");
    }

    public void setBkBar5(String value) {
        setString("BK-BAR-5", value);
    }

    public String getBkBar6() {
        return getString("BK-BAR-6");
    }

    public void setBkBar6(String value) {
        setString("BK-BAR-6", value);
    }

    public String getBkBar7() {
        return getString("BK-BAR-7");
    }

    public void setBkBar7(String value) {
        setString("BK-BAR-7", value);
    }

    public String getBkCardNum() {
        return getString("BK-CARD-NUM");
    }

    public void setBkCardNum(String value) {
        setString("BK-CARD-NUM", value);
    }

    public int getBkCat() {
        return getInt("BK-CAT");
    }

    public void setBkCat(int value) {
        setInt("BK-CAT", value);
    }

    public String getBkDesc() {
        return getString("BK-DESC");
    }

    public void setBkDesc(String value) {
        setString("BK-DESC", value);
    }

    public int getBkMerchId() {
        return getInt("BK-MERCH-ID");
    }

    public void setBkMerchId(int value) {
        setInt("BK-MERCH-ID", value);
    }

    public String getBkOrigTs() {
        return getString("BK-ORIG-TS");
    }

    public void setBkOrigTs(String value) {
        setString("BK-ORIG-TS", value);
    }

    public String getBkTranId() {
        return getString("BK-TRAN-ID");
    }

    public void setBkTranId(String value) {
        setString("BK-TRAN-ID", value);
    }

    public String getBkType() {
        return getString("BK-TYPE");
    }

    public void setBkType(String value) {
        setString("BK-TYPE", value);
    }

    public BigDecimal getKbkCreditAmt() {
        return getDecimal("KBK-CREDIT-AMT");
    }

    public void setKbkCreditAmt(BigDecimal value) {
        setDecimal("KBK-CREDIT-AMT", value);
    }

    public BigDecimal getKbkDebitAmt() {
        return getDecimal("KBK-DEBIT-AMT");
    }

    public void setKbkDebitAmt(BigDecimal value) {
        setDecimal("KBK-DEBIT-AMT", value);
    }

    public int getKbkErrors() {
        return getInt("KBK-ERRORS");
    }

    public void setKbkErrors(int value) {
        setInt("KBK-ERRORS", value);
    }

    public int getKbkMax() {
        return getInt("KBK-MAX");
    }

    public void setKbkMax(int value) {
        setInt("KBK-MAX", value);
    }

    public String getKbkMore() {
        return getString("KBK-MORE");
    }

    public void setKbkMore(String value) {
        setString("KBK-MORE", value);
    }

    public String getKbkMsg() {
        return getString("KBK-MSG");
    }

    public void setKbkMsg(String value) {
        setString("KBK-MSG", value);
    }

    public String getKbkNextKey() {
        return getString("KBK-NEXT-KEY");
    }

    public void setKbkNextKey(String value) {
        setString("KBK-NEXT-KEY", value);
    }

    public int getKbkRead() {
        return getInt("KBK-READ");
    }

    public void setKbkRead(int value) {
        setInt("KBK-READ", value);
    }

    public String getKbkStartKey() {
        return getString("KBK-START-KEY");
    }

    public void setKbkStartKey(String value) {
        setString("KBK-START-KEY", value);
    }

    public String getKbkStatus() {
        return getString("KBK-STATUS");
    }

    public void setKbkStatus(String value) {
        setString("KBK-STATUS", value);
    }

    public BigDecimal getKbkTotAmt() {
        return getDecimal("KBK-TOT-AMT");
    }

    public void setKbkTotAmt(BigDecimal value) {
        setDecimal("KBK-TOT-AMT", value);
    }

    public int getKbkWritten() {
        return getInt("KBK-WRITTEN");
    }

    public void setKbkWritten(int value) {
        setInt("KBK-WRITTEN", value);
    }

    public String getOrionCommarea() {
        return groupToString("ORION-COMMAREA");
    }

    public void setOrionCommarea(String value) {
        setGroup("ORION-COMMAREA", value);
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

    public String getTrDesc() {
        return getString("TR-DESC");
    }

    public void setTrDesc(String value) {
        setString("TR-DESC", value);
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

    public String getTrOrigTs() {
        return getString("TR-ORIG-TS");
    }

    public void setTrOrigTs(String value) {
        setString("TR-ORIG-TS", value);
    }

    public String getTrTypeCd() {
        return getString("TR-TYPE-CD");
    }

    public void setTrTypeCd(String value) {
        setString("TR-TYPE-CD", value);
    }

    public String getWsBkpfile() {
        return getString("WS-BKPFILE");
    }

    public void setWsBkpfile(String value) {
        setString("WS-BKPFILE", value);
    }

    public String getWsBrowseSw() {
        return getString("WS-BROWSE-SW");
    }

    public void setWsBrowseSw(String value) {
        setString("WS-BROWSE-SW", value);
    }

    public String getWsCapped() {
        return getString("WS-CAPPED");
    }

    public void setWsCapped(String value) {
        setString("WS-CAPPED", value);
    }

    public String getWsEofSw() {
        return getString("WS-EOF-SW");
    }

    public void setWsEofSw(String value) {
        setString("WS-EOF-SW", value);
    }

    public int getWsMax() {
        return getInt("WS-MAX");
    }

    public void setWsMax(int value) {
        setInt("WS-MAX", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public String getWsTranfile() {
        return getString("WS-TRANFILE");
    }

    public void setWsTranfile(String value) {
        setString("WS-TRANFILE", value);
    }
}

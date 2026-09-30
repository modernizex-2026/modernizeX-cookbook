package com.generated.orion.ouxref.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ouxref.model.WorkingStorage;

/**
 * Field accessor for OUXREF. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OuxrefFields extends DynamicFieldAccessor {

    public OuxrefFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public long getAcId() {
        return getLong("AC-ID");
    }

    public void setAcId(long value) {
        setLong("AC-ID", value);
    }

    public long getCdAcctId() {
        return getLong("CD-ACCT-ID");
    }

    public void setCdAcctId(long value) {
        setLong("CD-ACCT-ID", value);
    }

    public String getCdNum() {
        return getString("CD-NUM");
    }

    public void setCdNum(String value) {
        setString("CD-NUM", value);
    }

    public int getCuId() {
        return getInt("CU-ID");
    }

    public void setCuId(int value) {
        setInt("CU-ID", value);
    }

    public int getKuxErrors() {
        return getInt("KUX-ERRORS");
    }

    public void setKuxErrors(int value) {
        setInt("KUX-ERRORS", value);
    }

    public int getKuxMax() {
        return getInt("KUX-MAX");
    }

    public void setKuxMax(int value) {
        setInt("KUX-MAX", value);
    }

    public String getKuxMode() {
        return getString("KUX-MODE");
    }

    public void setKuxMode(String value) {
        setString("KUX-MODE", value);
    }

    public String getKuxMore() {
        return getString("KUX-MORE");
    }

    public void setKuxMore(String value) {
        setString("KUX-MORE", value);
    }

    public String getKuxMsg() {
        return getString("KUX-MSG");
    }

    public void setKuxMsg(String value) {
        setString("KUX-MSG", value);
    }

    public String getKuxNextCard() {
        return getString("KUX-NEXT-CARD");
    }

    public void setKuxNextCard(String value) {
        setString("KUX-NEXT-CARD", value);
    }

    public int getKuxRead() {
        return getInt("KUX-READ");
    }

    public void setKuxRead(int value) {
        setInt("KUX-READ", value);
    }

    public int getKuxSkipAcct() {
        return getInt("KUX-SKIP-ACCT");
    }

    public void setKuxSkipAcct(int value) {
        setInt("KUX-SKIP-ACCT", value);
    }

    public int getKuxSkipCust() {
        return getInt("KUX-SKIP-CUST");
    }

    public void setKuxSkipCust(int value) {
        setInt("KUX-SKIP-CUST", value);
    }

    public int getKuxSkipXref() {
        return getInt("KUX-SKIP-XREF");
    }

    public void setKuxSkipXref(int value) {
        setInt("KUX-SKIP-XREF", value);
    }

    public String getKuxStartCard() {
        return getString("KUX-START-CARD");
    }

    public void setKuxStartCard(String value) {
        setString("KUX-START-CARD", value);
    }

    public String getKuxStatus() {
        return getString("KUX-STATUS");
    }

    public void setKuxStatus(String value) {
        setString("KUX-STATUS", value);
    }

    public int getKuxUpdated() {
        return getInt("KUX-UPDATED");
    }

    public void setKuxUpdated(int value) {
        setInt("KUX-UPDATED", value);
    }

    public int getKuxWritten() {
        return getInt("KUX-WRITTEN");
    }

    public void setKuxWritten(int value) {
        setInt("KUX-WRITTEN", value);
    }

    public String getOrionCommarea() {
        return groupToString("ORION-COMMAREA");
    }

    public void setOrionCommarea(String value) {
        setGroup("ORION-COMMAREA", value);
    }

    public String getWsAcctfile() {
        return getString("WS-ACCTFILE");
    }

    public void setWsAcctfile(String value) {
        setString("WS-ACCTFILE", value);
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

    public String getWsCardSw() {
        return getString("WS-CARD-SW");
    }

    public void setWsCardSw(String value) {
        setString("WS-CARD-SW", value);
    }

    public String getWsCardfile() {
        return getString("WS-CARDFILE");
    }

    public void setWsCardfile(String value) {
        setString("WS-CARDFILE", value);
    }

    public String getWsCustfile() {
        return getString("WS-CUSTFILE");
    }

    public void setWsCustfile(String value) {
        setString("WS-CUSTFILE", value);
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

    public String getWsModeSw() {
        return getString("WS-MODE-SW");
    }

    public void setWsModeSw(String value) {
        setString("WS-MODE-SW", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public int getWsSaveCust() {
        return getInt("WS-SAVE-CUST");
    }

    public void setWsSaveCust(int value) {
        setInt("WS-SAVE-CUST", value);
    }

    public String getWsXreffile() {
        return getString("WS-XREFFILE");
    }

    public void setWsXreffile(String value) {
        setString("WS-XREFFILE", value);
    }

    public long getXrAcctId() {
        return getLong("XR-ACCT-ID");
    }

    public void setXrAcctId(long value) {
        setLong("XR-ACCT-ID", value);
    }

    public String getXrCardNum() {
        return getString("XR-CARD-NUM");
    }

    public void setXrCardNum(String value) {
        setString("XR-CARD-NUM", value);
    }

    public int getXrCustId() {
        return getInt("XR-CUST-ID");
    }

    public void setXrCustId(int value) {
        setInt("XR-CUST-ID", value);
    }

    public String getXrefRec() {
        return groupToString("XREF-REC");
    }

    public void setXrefRec(String value) {
        setGroup("XREF-REC", value);
    }
}

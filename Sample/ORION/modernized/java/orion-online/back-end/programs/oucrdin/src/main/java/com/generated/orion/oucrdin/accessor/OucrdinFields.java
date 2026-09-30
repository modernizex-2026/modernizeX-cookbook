package com.generated.orion.oucrdin.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.oucrdin.model.WorkingStorage;

/**
 * Field accessor for OUCRDIN. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OucrdinFields extends DynamicFieldAccessor {

    public OucrdinFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public long getCdAcctId() {
        return getLong("CD-ACCT-ID");
    }

    public void setCdAcctId(long value) {
        setLong("CD-ACCT-ID", value);
    }

    public String getCdActiveStatus() {
        return getString("CD-ACTIVE-STATUS");
    }

    public void setCdActiveStatus(String value) {
        setString("CD-ACTIVE-STATUS", value);
    }

    public String getCdEmbossedName() {
        return getString("CD-EMBOSSED-NAME");
    }

    public void setCdEmbossedName(String value) {
        setString("CD-EMBOSSED-NAME", value);
    }

    public String getCdExpiryDate() {
        return getString("CD-EXPIRY-DATE");
    }

    public void setCdExpiryDate(String value) {
        setString("CD-EXPIRY-DATE", value);
    }

    public String getCdNum() {
        return getString("CD-NUM");
    }

    public void setCdNum(String value) {
        setString("CD-NUM", value);
    }

    public int getKciActiveCnt() {
        return getInt("KCI-ACTIVE-CNT");
    }

    public void setKciActiveCnt(int value) {
        setInt("KCI-ACTIVE-CNT", value);
    }

    public String getKciFilter() {
        return getString("KCI-FILTER");
    }

    public void setKciFilter(String value) {
        setString("KCI-FILTER", value);
    }

    public int getKciInactiveCnt() {
        return getInt("KCI-INACTIVE-CNT");
    }

    public void setKciInactiveCnt(int value) {
        setInt("KCI-INACTIVE-CNT", value);
    }

    public String getKciNextKey() {
        return getString("KCI-NEXT-KEY");
    }

    public void setKciNextKey(String value) {
        setString("KCI-NEXT-KEY", value);
    }

    public long getKciRAcct(int index) {
        return getLong("KCI-R-ACCT", index);
    }

    public void setKciRAcct(int index, long value) {
        setLong("KCI-R-ACCT", value, index);
    }

    public String getKciRExpiry(int index) {
        return getString("KCI-R-EXPIRY", index);
    }

    public void setKciRExpiry(int index, String value) {
        setString("KCI-R-EXPIRY", value, index);
    }

    public String getKciRName(int index) {
        return getString("KCI-R-NAME", index);
    }

    public void setKciRName(int index, String value) {
        setString("KCI-R-NAME", value, index);
    }

    public String getKciRNum(int index) {
        return getString("KCI-R-NUM", index);
    }

    public void setKciRNum(int index, String value) {
        setString("KCI-R-NUM", value, index);
    }

    public String getKciRStatus(int index) {
        return getString("KCI-R-STATUS", index);
    }

    public void setKciRStatus(int index, String value) {
        setString("KCI-R-STATUS", value, index);
    }

    public String getKciReturnCd() {
        return getString("KCI-RETURN-CD");
    }

    public void setKciReturnCd(String value) {
        setString("KCI-RETURN-CD", value);
    }

    public int getKciRowCount() {
        return getInt("KCI-ROW-COUNT");
    }

    public void setKciRowCount(int value) {
        setInt("KCI-ROW-COUNT", value);
    }

    public int getKciScanCount() {
        return getInt("KCI-SCAN-COUNT");
    }

    public void setKciScanCount(int value) {
        setInt("KCI-SCAN-COUNT", value);
    }

    public String getKciStartKey() {
        return getString("KCI-START-KEY");
    }

    public void setKciStartKey(String value) {
        setString("KCI-START-KEY", value);
    }

    public String getKciWantKpi() {
        return getString("KCI-WANT-KPI");
    }

    public void setKciWantKpi(String value) {
        setString("KCI-WANT-KPI", value);
    }

    public String getKcrdinArea() {
        return groupToString("KCRDIN-AREA");
    }

    public void setKcrdinArea(String value) {
        setGroup("KCRDIN-AREA", value);
    }

    public int getWeDd() {
        return getInt("WE-DD");
    }

    public void setWeDd(int value) {
        setInt("WE-DD", value);
    }

    public int getWeMm() {
        return getInt("WE-MM");
    }

    public void setWeMm(int value) {
        setInt("WE-MM", value);
    }

    public int getWeYyyy() {
        return getInt("WE-YYYY");
    }

    public void setWeYyyy(int value) {
        setInt("WE-YYYY", value);
    }

    public String getWsBrEndSw() {
        return getString("WS-BR-END-SW");
    }

    public void setWsBrEndSw(String value) {
        setString("WS-BR-END-SW", value);
    }

    public String getWsBrStartedSw() {
        return getString("WS-BR-STARTED-SW");
    }

    public void setWsBrStartedSw(String value) {
        setString("WS-BR-STARTED-SW", value);
    }

    public String getWsBrowseKey() {
        return getString("WS-BROWSE-KEY");
    }

    public void setWsBrowseKey(String value) {
        setString("WS-BROWSE-KEY", value);
    }

    public String getWsCardfile() {
        return getString("WS-CARDFILE");
    }

    public void setWsCardfile(String value) {
        setString("WS-CARDFILE", value);
    }

    public String getWsCdt() {
        return getString("WS-CDT");
    }

    public void setWsCdt(String value) {
        setString("WS-CDT", value);
    }

    public int getWsCutInt() {
        return getInt("WS-CUT-INT");
    }

    public void setWsCutInt(int value) {
        setInt("WS-CUT-INT", value);
    }

    public int getWsExpInt() {
        return getInt("WS-EXP-INT");
    }

    public void setWsExpInt(int value) {
        setInt("WS-EXP-INT", value);
    }

    public int getWsExpN() {
        return getInt("WS-EXP-N");
    }

    public void setWsExpN(int value) {
        setInt("WS-EXP-N", value);
    }

    public String getWsExpValidSw() {
        return getString("WS-EXP-VALID-SW");
    }

    public void setWsExpValidSw(String value) {
        setString("WS-EXP-VALID-SW", value);
    }

    public String getWsLastMatch() {
        return getString("WS-LAST-MATCH");
    }

    public void setWsLastMatch(String value) {
        setString("WS-LAST-MATCH", value);
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

    public String getWsReadSw() {
        return getString("WS-READ-SW");
    }

    public void setWsReadSw(String value) {
        setString("WS-READ-SW", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public int getWsTodayInt() {
        return getInt("WS-TODAY-INT");
    }

    public void setWsTodayInt(int value) {
        setInt("WS-TODAY-INT", value);
    }

    public int getWsTodayN() {
        return getInt("WS-TODAY-N");
    }

    public void setWsTodayN(int value) {
        setInt("WS-TODAY-N", value);
    }
}

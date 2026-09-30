package com.generated.orion.ournew.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ournew.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OURNEW. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OurnewFields extends DynamicFieldAccessor {

    public OurnewFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public String getCdActiveStatus() {
        return getString("CD-ACTIVE-STATUS");
    }

    public void setCdActiveStatus(String value) {
        setString("CD-ACTIVE-STATUS", value);
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

    public int getCsDd() {
        return getInt("CS-DD");
    }

    public void setCsDd(int value) {
        setInt("CS-DD", value);
    }

    public int getCsMm() {
        return getInt("CS-MM");
    }

    public void setCsMm(int value) {
        setInt("CS-MM", value);
    }

    public int getCsYear() {
        return getInt("CS-YEAR");
    }

    public void setCsYear(int value) {
        setInt("CS-YEAR", value);
    }

    public BigDecimal getKoAmt1() {
        return getDecimal("KO-AMT-1");
    }

    public void setKoAmt1(BigDecimal value) {
        setDecimal("KO-AMT-1", value);
    }

    public BigDecimal getKoAmt2() {
        return getDecimal("KO-AMT-2");
    }

    public void setKoAmt2(BigDecimal value) {
        setDecimal("KO-AMT-2", value);
    }

    public BigDecimal getKoAmt3() {
        return getDecimal("KO-AMT-3");
    }

    public void setKoAmt3(BigDecimal value) {
        setDecimal("KO-AMT-3", value);
    }

    public int getKoC1() {
        return getInt("KO-C1");
    }

    public void setKoC1(int value) {
        setInt("KO-C1", value);
    }

    public int getKoC2() {
        return getInt("KO-C2");
    }

    public void setKoC2(int value) {
        setInt("KO-C2", value);
    }

    public int getKoC3() {
        return getInt("KO-C3");
    }

    public void setKoC3(int value) {
        setInt("KO-C3", value);
    }

    public String getKoParmCard() {
        return getString("KO-PARM-CARD");
    }

    public void setKoParmCard(String value) {
        setString("KO-PARM-CARD", value);
    }

    public int getKoPostedCnt() {
        return getInt("KO-POSTED-CNT");
    }

    public void setKoPostedCnt(int value) {
        setInt("KO-POSTED-CNT", value);
    }

    public int getKoReadCnt() {
        return getInt("KO-READ-CNT");
    }

    public void setKoReadCnt(int value) {
        setInt("KO-READ-CNT", value);
    }

    public int getKoRejectCnt() {
        return getInt("KO-REJECT-CNT");
    }

    public void setKoRejectCnt(int value) {
        setInt("KO-REJECT-CNT", value);
    }

    public int getKoSelectCnt() {
        return getInt("KO-SELECT-CNT");
    }

    public void setKoSelectCnt(int value) {
        setInt("KO-SELECT-CNT", value);
    }

    public int getKoSkipCnt() {
        return getInt("KO-SKIP-CNT");
    }

    public void setKoSkipCnt(int value) {
        setInt("KO-SKIP-CNT", value);
    }

    public String getKoStatus() {
        return getString("KO-STATUS");
    }

    public void setKoStatus(String value) {
        setString("KO-STATUS", value);
    }

    public String getKoStatusMsg() {
        return getString("KO-STATUS-MSG");
    }

    public void setKoStatusMsg(String value) {
        setString("KO-STATUS-MSG", value);
    }

    public int getKoTranCnt() {
        return getInt("KO-TRAN-CNT");
    }

    public void setKoTranCnt(int value) {
        setInt("KO-TRAN-CNT", value);
    }

    public int getKoUpdateCnt() {
        return getInt("KO-UPDATE-CNT");
    }

    public void setKoUpdateCnt(int value) {
        setInt("KO-UPDATE-CNT", value);
    }

    public String getOrionCommarea() {
        return groupToString("ORION-COMMAREA");
    }

    public void setOrionCommarea(String value) {
        setGroup("ORION-COMMAREA", value);
    }

    public int getWcDd() {
        return getInt("WC-DD");
    }

    public void setWcDd(int value) {
        setInt("WC-DD", value);
    }

    public int getWcMm() {
        return getInt("WC-MM");
    }

    public void setWcMm(int value) {
        setInt("WC-MM", value);
    }

    public int getWcYear() {
        return getInt("WC-YEAR");
    }

    public void setWcYear(int value) {
        setInt("WC-YEAR", value);
    }

    public int getWcnDay() {
        return getInt("WCN-DAY");
    }

    public void setWcnDay(int value) {
        setInt("WCN-DAY", value);
    }

    public int getWcnMon() {
        return getInt("WCN-MON");
    }

    public void setWcnMon(int value) {
        setInt("WCN-MON", value);
    }

    public int getWcnYear() {
        return getInt("WCN-YEAR");
    }

    public void setWcnYear(int value) {
        setInt("WCN-YEAR", value);
    }

    public String getWneDd() {
        return getString("WNE-DD");
    }

    public void setWneDd(String value) {
        setString("WNE-DD", value);
    }

    public String getWneMm() {
        return getString("WNE-MM");
    }

    public void setWneMm(String value) {
        setString("WNE-MM", value);
    }

    public int getWneYear() {
        return getInt("WNE-YEAR");
    }

    public void setWneYear(int value) {
        setInt("WNE-YEAR", value);
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

    public String getWsCardfile() {
        return getString("WS-CARDFILE");
    }

    public void setWsCardfile(String value) {
        setString("WS-CARDFILE", value);
    }

    public String getWsCurrN() {
        return groupToString("WS-CURR-N");
    }

    public void setWsCurrN(String value) {
        setGroup("WS-CURR-N", value);
    }

    public String getWsCurrRaw() {
        return getString("WS-CURR-RAW");
    }

    public void setWsCurrRaw(String value) {
        setString("WS-CURR-RAW", value);
    }

    public int getWsCutoffInt() {
        return getInt("WS-CUTOFF-INT");
    }

    public void setWsCutoffInt(int value) {
        setInt("WS-CUTOFF-INT", value);
    }

    public String getWsCutoffStr() {
        return groupToString("WS-CUTOFF-STR");
    }

    public void setWsCutoffStr(String value) {
        setGroup("WS-CUTOFF-STR", value);
    }

    public int getWsCutoffYmd() {
        return getInt("WS-CUTOFF-YMD");
    }

    public void setWsCutoffYmd(int value) {
        setInt("WS-CUTOFF-YMD", value);
    }

    public String getWsExpDdX() {
        return getString("WS-EXP-DD-X");
    }

    public void setWsExpDdX(String value) {
        setString("WS-EXP-DD-X", value);
    }

    public String getWsExpMmX() {
        return getString("WS-EXP-MM-X");
    }

    public void setWsExpMmX(String value) {
        setString("WS-EXP-MM-X", value);
    }

    public String getWsExpValidSw() {
        return getString("WS-EXP-VALID-SW");
    }

    public void setWsExpValidSw(String value) {
        setString("WS-EXP-VALID-SW", value);
    }

    public int getWsExpYearN() {
        return getInt("WS-EXP-YEAR-N");
    }

    public void setWsExpYearN(int value) {
        setInt("WS-EXP-YEAR-N", value);
    }

    public String getWsExpYearX() {
        return getString("WS-EXP-YEAR-X");
    }

    public void setWsExpYearX(String value) {
        setString("WS-EXP-YEAR-X", value);
    }

    public int getWsExtendYears() {
        return getInt("WS-EXTEND-YEARS");
    }

    public void setWsExtendYears(int value) {
        setInt("WS-EXTEND-YEARS", value);
    }

    public String getWsFilterCard() {
        return getString("WS-FILTER-CARD");
    }

    public void setWsFilterCard(String value) {
        setString("WS-FILTER-CARD", value);
    }

    public String getWsFilterOn() {
        return getString("WS-FILTER-ON");
    }

    public void setWsFilterOn(String value) {
        setString("WS-FILTER-ON", value);
    }

    public int getWsLeadDays() {
        return getInt("WS-LEAD-DAYS");
    }

    public void setWsLeadDays(int value) {
        setInt("WS-LEAD-DAYS", value);
    }

    public int getWsLeapR100() {
        return getInt("WS-LEAP-R100");
    }

    public void setWsLeapR100(int value) {
        setInt("WS-LEAP-R100", value);
    }

    public int getWsLeapR4() {
        return getInt("WS-LEAP-R4");
    }

    public void setWsLeapR4(int value) {
        setInt("WS-LEAP-R4", value);
    }

    public int getWsLeapR400() {
        return getInt("WS-LEAP-R400");
    }

    public void setWsLeapR400(int value) {
        setInt("WS-LEAP-R400", value);
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

    public int getWsTodayYmd() {
        return getInt("WS-TODAY-YMD");
    }

    public void setWsTodayYmd(int value) {
        setInt("WS-TODAY-YMD", value);
    }
}

package com.generated.orion.ouclos.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ouclos.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OUCLOS. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OuclosFields extends DynamicFieldAccessor {

    public OuclosFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public String getAcActiveStatus() {
        return getString("AC-ACTIVE-STATUS");
    }

    public void setAcActiveStatus(String value) {
        setString("AC-ACTIVE-STATUS", value);
    }

    public BigDecimal getAcCreditLimit() {
        return getDecimal("AC-CREDIT-LIMIT");
    }

    public void setAcCreditLimit(BigDecimal value) {
        setDecimal("AC-CREDIT-LIMIT", value);
    }

    public BigDecimal getAcCurrBal() {
        return getDecimal("AC-CURR-BAL");
    }

    public void setAcCurrBal(BigDecimal value) {
        setDecimal("AC-CURR-BAL", value);
    }

    public String getAcExpiryDate() {
        return getString("AC-EXPIRY-DATE");
    }

    public void setAcExpiryDate(String value) {
        setString("AC-EXPIRY-DATE", value);
    }

    public long getAcId() {
        return getLong("AC-ID");
    }

    public void setAcId(long value) {
        setLong("AC-ID", value);
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

    public long getKoParmAcct() {
        return getLong("KO-PARM-ACCT");
    }

    public void setKoParmAcct(long value) {
        setLong("KO-PARM-ACCT", value);
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

    public int getWrDay() {
        return getInt("WR-DAY");
    }

    public void setWrDay(int value) {
        setInt("WR-DAY", value);
    }

    public int getWrMon() {
        return getInt("WR-MON");
    }

    public void setWrMon(int value) {
        setInt("WR-MON", value);
    }

    public int getWrYear() {
        return getInt("WR-YEAR");
    }

    public void setWrYear(int value) {
        setInt("WR-YEAR", value);
    }

    public String getWsAcctfile() {
        return getString("WS-ACCTFILE");
    }

    public void setWsAcctfile(String value) {
        setString("WS-ACCTFILE", value);
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

    public String getWsCloseSw() {
        return getString("WS-CLOSE-SW");
    }

    public void setWsCloseSw(String value) {
        setString("WS-CLOSE-SW", value);
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

    public long getWsFilterAcct() {
        return getLong("WS-FILTER-ACCT");
    }

    public void setWsFilterAcct(long value) {
        setLong("WS-FILTER-ACCT", value);
    }

    public String getWsFilterOn() {
        return getString("WS-FILTER-ON");
    }

    public void setWsFilterOn(String value) {
        setString("WS-FILTER-ON", value);
    }

    public String getWsReasonSw() {
        return getString("WS-REASON-SW");
    }

    public void setWsReasonSw(String value) {
        setString("WS-REASON-SW", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public String getWsRunDate() {
        return groupToString("WS-RUN-DATE");
    }

    public void setWsRunDate(String value) {
        setGroup("WS-RUN-DATE", value);
    }

    public String getWsStatusClosed() {
        return getString("WS-STATUS-CLOSED");
    }

    public void setWsStatusClosed(String value) {
        setString("WS-STATUS-CLOSED", value);
    }

    public BigDecimal getWsZeroAmount() {
        return getDecimal("WS-ZERO-AMOUNT");
    }

    public void setWsZeroAmount(BigDecimal value) {
        setDecimal("WS-ZERO-AMOUNT", value);
    }
}

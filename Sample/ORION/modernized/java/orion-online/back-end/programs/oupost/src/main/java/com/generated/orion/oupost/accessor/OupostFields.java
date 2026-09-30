package com.generated.orion.oupost.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.oupost.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OUPOST. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OupostFields extends DynamicFieldAccessor {

    public OupostFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
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

    public BigDecimal getAcCycCredit() {
        return getDecimal("AC-CYC-CREDIT");
    }

    public void setAcCycCredit(BigDecimal value) {
        setDecimal("AC-CYC-CREDIT", value);
    }

    public BigDecimal getAcCycDebit() {
        return getDecimal("AC-CYC-DEBIT");
    }

    public void setAcCycDebit(BigDecimal value) {
        setDecimal("AC-CYC-DEBIT", value);
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

    public String getTrId() {
        return getString("TR-ID");
    }

    public void setTrId(String value) {
        setString("TR-ID", value);
    }

    public String getTrTypeCd() {
        return getString("TR-TYPE-CD");
    }

    public void setTrTypeCd(String value) {
        setString("TR-TYPE-CD", value);
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

    public String getWsDcInd() {
        return getString("WS-DC-IND");
    }

    public void setWsDcInd(String value) {
        setString("WS-DC-IND", value);
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

    public BigDecimal getWsProjBal() {
        return getDecimal("WS-PROJ-BAL");
    }

    public void setWsProjBal(BigDecimal value) {
        setDecimal("WS-PROJ-BAL", value);
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
}

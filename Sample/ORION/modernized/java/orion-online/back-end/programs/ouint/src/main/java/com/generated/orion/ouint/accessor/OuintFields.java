package com.generated.orion.ouint.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ouint.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OUINT. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OuintFields extends DynamicFieldAccessor {

    public OuintFields(WorkingStorage ws) {
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

    public BigDecimal getAcCurrBal() {
        return getDecimal("AC-CURR-BAL");
    }

    public void setAcCurrBal(BigDecimal value) {
        setDecimal("AC-CURR-BAL", value);
    }

    public BigDecimal getAcCycDebit() {
        return getDecimal("AC-CYC-DEBIT");
    }

    public void setAcCycDebit(BigDecimal value) {
        setDecimal("AC-CYC-DEBIT", value);
    }

    public String getAcGroupId() {
        return getString("AC-GROUP-ID");
    }

    public void setAcGroupId(String value) {
        setString("AC-GROUP-ID", value);
    }

    public long getAcId() {
        return getLong("AC-ID");
    }

    public void setAcId(long value) {
        setLong("AC-ID", value);
    }

    public String getDgAcctGroup() {
        return getString("DG-ACCT-GROUP");
    }

    public void setDgAcctGroup(String value) {
        setString("DG-ACCT-GROUP", value);
    }

    public int getDgCatCd() {
        return getInt("DG-CAT-CD");
    }

    public void setDgCatCd(int value) {
        setInt("DG-CAT-CD", value);
    }

    public BigDecimal getDgIntRate() {
        return getDecimal("DG-INT-RATE");
    }

    public void setDgIntRate(BigDecimal value) {
        setDecimal("DG-INT-RATE", value);
    }

    public String getDgKey() {
        return groupToString("DG-KEY");
    }

    public void setDgKey(String value) {
        setGroup("DG-KEY", value);
    }

    public String getDgTypeCd() {
        return getString("DG-TYPE-CD");
    }

    public void setDgTypeCd(String value) {
        setString("DG-TYPE-CD", value);
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

    public BigDecimal getWsDefaultRate() {
        return getDecimal("WS-DEFAULT-RATE");
    }

    public void setWsDefaultRate(BigDecimal value) {
        setDecimal("WS-DEFAULT-RATE", value);
    }

    public String getWsDgrpfile() {
        return getString("WS-DGRPFILE");
    }

    public void setWsDgrpfile(String value) {
        setString("WS-DGRPFILE", value);
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

    public BigDecimal getWsIntAmt() {
        return getDecimal("WS-INT-AMT");
    }

    public void setWsIntAmt(BigDecimal value) {
        setDecimal("WS-INT-AMT", value);
    }

    public int getWsIntCat() {
        return getInt("WS-INT-CAT");
    }

    public void setWsIntCat(int value) {
        setInt("WS-INT-CAT", value);
    }

    public String getWsIntType() {
        return getString("WS-INT-TYPE");
    }

    public void setWsIntType(String value) {
        setString("WS-INT-TYPE", value);
    }

    public String getWsRateFoundSw() {
        return getString("WS-RATE-FOUND-SW");
    }

    public void setWsRateFoundSw(String value) {
        setString("WS-RATE-FOUND-SW", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public BigDecimal getWsUsedRate() {
        return getDecimal("WS-USED-RATE");
    }

    public void setWsUsedRate(BigDecimal value) {
        setDecimal("WS-USED-RATE", value);
    }
}

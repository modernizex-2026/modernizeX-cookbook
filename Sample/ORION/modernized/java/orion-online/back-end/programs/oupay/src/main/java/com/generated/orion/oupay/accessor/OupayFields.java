package com.generated.orion.oupay.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.oupay.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OUPAY. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OupayFields extends DynamicFieldAccessor {

    public OupayFields(WorkingStorage ws) {
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

    public BigDecimal getAcCycCredit() {
        return getDecimal("AC-CYC-CREDIT");
    }

    public void setAcCycCredit(BigDecimal value) {
        setDecimal("AC-CYC-CREDIT", value);
    }

    public long getAcId() {
        return getLong("AC-ID");
    }

    public void setAcId(long value) {
        setLong("AC-ID", value);
    }

    public String getBillRec() {
        return groupToString("BILL-REC");
    }

    public void setBillRec(String value) {
        setGroup("BILL-REC", value);
    }

    public long getBlAcctId() {
        return getLong("BL-ACCT-ID");
    }

    public void setBlAcctId(long value) {
        setLong("BL-ACCT-ID", value);
    }

    public BigDecimal getBlAmount() {
        return getDecimal("BL-AMOUNT");
    }

    public void setBlAmount(BigDecimal value) {
        setDecimal("BL-AMOUNT", value);
    }

    public long getBlId() {
        return getLong("BL-ID");
    }

    public void setBlId(long value) {
        setLong("BL-ID", value);
    }

    public String getBlPayDate() {
        return getString("BL-PAY-DATE");
    }

    public void setBlPayDate(String value) {
        setString("BL-PAY-DATE", value);
    }

    public String getBlStatus() {
        return getString("BL-STATUS");
    }

    public void setBlStatus(String value) {
        setString("BL-STATUS", value);
    }

    public String getCtDesc() {
        return getString("CT-DESC");
    }

    public void setCtDesc(String value) {
        setString("CT-DESC", value);
    }

    public String getCtKey() {
        return getString("CT-KEY");
    }

    public void setCtKey(String value) {
        setString("CT-KEY", value);
    }

    public long getCtLastValue() {
        return getLong("CT-LAST-VALUE");
    }

    public void setCtLastValue(long value) {
        setLong("CT-LAST-VALUE", value);
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

    public BigDecimal getKoParmAmt() {
        return getDecimal("KO-PARM-AMT");
    }

    public void setKoParmAmt(BigDecimal value) {
        setDecimal("KO-PARM-AMT", value);
    }

    public String getKoParmDate() {
        return getString("KO-PARM-DATE");
    }

    public void setKoParmDate(String value) {
        setString("KO-PARM-DATE", value);
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

    public int getWcDay() {
        return getInt("WC-DAY");
    }

    public void setWcDay(int value) {
        setInt("WC-DAY", value);
    }

    public int getWcMon() {
        return getInt("WC-MON");
    }

    public void setWcMon(int value) {
        setInt("WC-MON", value);
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

    public String getWsAcctLockSw() {
        return getString("WS-ACCT-LOCK-SW");
    }

    public void setWsAcctLockSw(String value) {
        setString("WS-ACCT-LOCK-SW", value);
    }

    public String getWsAcctfile() {
        return getString("WS-ACCTFILE");
    }

    public void setWsAcctfile(String value) {
        setString("WS-ACCTFILE", value);
    }

    public String getWsBillKey() {
        return getString("WS-BILL-KEY");
    }

    public void setWsBillKey(String value) {
        setString("WS-BILL-KEY", value);
    }

    public long getWsBillSeq() {
        return getLong("WS-BILL-SEQ");
    }

    public void setWsBillSeq(long value) {
        setLong("WS-BILL-SEQ", value);
    }

    public String getWsBillfile() {
        return getString("WS-BILLFILE");
    }

    public void setWsBillfile(String value) {
        setString("WS-BILLFILE", value);
    }

    public long getWsCfmSeq() {
        return getLong("WS-CFM-SEQ");
    }

    public void setWsCfmSeq(long value) {
        setLong("WS-CFM-SEQ", value);
    }

    public String getWsCtrlAvailSw() {
        return getString("WS-CTRL-AVAIL-SW");
    }

    public void setWsCtrlAvailSw(String value) {
        setString("WS-CTRL-AVAIL-SW", value);
    }

    public String getWsCtrlfile() {
        return getString("WS-CTRLFILE");
    }

    public void setWsCtrlfile(String value) {
        setString("WS-CTRLFILE", value);
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

    public BigDecimal getWsNewBal() {
        return getDecimal("WS-NEW-BAL");
    }

    public void setWsNewBal(BigDecimal value) {
        setDecimal("WS-NEW-BAL", value);
    }

    public String getWsPayDate() {
        return getString("WS-PAY-DATE");
    }

    public void setWsPayDate(String value) {
        setString("WS-PAY-DATE", value);
    }

    public String getWsRejectSw() {
        return getString("WS-REJECT-SW");
    }

    public void setWsRejectSw(String value) {
        setString("WS-REJECT-SW", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }
}

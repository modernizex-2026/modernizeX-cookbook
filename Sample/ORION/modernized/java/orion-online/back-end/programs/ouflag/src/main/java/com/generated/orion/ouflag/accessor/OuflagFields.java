package com.generated.orion.ouflag.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ouflag.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OUFLAG. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OuflagFields extends DynamicFieldAccessor {

    public OuflagFields(WorkingStorage ws) {
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

    public BigDecimal getAcCycCredit() {
        return getDecimal("AC-CYC-CREDIT");
    }

    public void setAcCycCredit(BigDecimal value) {
        setDecimal("AC-CYC-CREDIT", value);
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

    public int getKflB30() {
        return getInt("KFL-B30");
    }

    public void setKflB30(int value) {
        setInt("KFL-B30", value);
    }

    public int getKflB60() {
        return getInt("KFL-B60");
    }

    public void setKflB60(int value) {
        setInt("KFL-B60", value);
    }

    public int getKflB90() {
        return getInt("KFL-B90");
    }

    public void setKflB90(int value) {
        setInt("KFL-B90", value);
    }

    public int getKflCurrent() {
        return getInt("KFL-CURRENT");
    }

    public void setKflCurrent(int value) {
        setInt("KFL-CURRENT", value);
    }

    public String getKflCutoff() {
        return getString("KFL-CUTOFF");
    }

    public void setKflCutoff(String value) {
        setString("KFL-CUTOFF", value);
    }

    public int getKflDelq() {
        return getInt("KFL-DELQ");
    }

    public void setKflDelq(int value) {
        setInt("KFL-DELQ", value);
    }

    public BigDecimal getKflDelqBal() {
        return getDecimal("KFL-DELQ-BAL");
    }

    public void setKflDelqBal(BigDecimal value) {
        setDecimal("KFL-DELQ-BAL", value);
    }

    public int getKflErrors() {
        return getInt("KFL-ERRORS");
    }

    public void setKflErrors(int value) {
        setInt("KFL-ERRORS", value);
    }

    public int getKflExpired() {
        return getInt("KFL-EXPIRED");
    }

    public void setKflExpired(int value) {
        setInt("KFL-EXPIRED", value);
    }

    public int getKflMax() {
        return getInt("KFL-MAX");
    }

    public void setKflMax(int value) {
        setInt("KFL-MAX", value);
    }

    public String getKflMode() {
        return getString("KFL-MODE");
    }

    public void setKflMode(String value) {
        setString("KFL-MODE", value);
    }

    public String getKflMore() {
        return getString("KFL-MORE");
    }

    public void setKflMore(String value) {
        setString("KFL-MORE", value);
    }

    public String getKflMsg() {
        return getString("KFL-MSG");
    }

    public void setKflMsg(String value) {
        setString("KFL-MSG", value);
    }

    public long getKflNextAcct() {
        return getLong("KFL-NEXT-ACCT");
    }

    public void setKflNextAcct(long value) {
        setLong("KFL-NEXT-ACCT", value);
    }

    public int getKflRead() {
        return getInt("KFL-READ");
    }

    public void setKflRead(int value) {
        setInt("KFL-READ", value);
    }

    public BigDecimal getKflShortfall() {
        return getDecimal("KFL-SHORTFALL");
    }

    public void setKflShortfall(BigDecimal value) {
        setDecimal("KFL-SHORTFALL", value);
    }

    public int getKflSkipped() {
        return getInt("KFL-SKIPPED");
    }

    public void setKflSkipped(int value) {
        setInt("KFL-SKIPPED", value);
    }

    public long getKflStartAcct() {
        return getLong("KFL-START-ACCT");
    }

    public void setKflStartAcct(long value) {
        setLong("KFL-START-ACCT", value);
    }

    public String getKflStatus() {
        return getString("KFL-STATUS");
    }

    public void setKflStatus(String value) {
        setString("KFL-STATUS", value);
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

    public String getWsDoDelq() {
        return getString("WS-DO-DELQ");
    }

    public void setWsDoDelq(String value) {
        setString("WS-DO-DELQ", value);
    }

    public String getWsDoExpy() {
        return getString("WS-DO-EXPY");
    }

    public void setWsDoExpy(String value) {
        setString("WS-DO-EXPY", value);
    }

    public String getWsEofSw() {
        return getString("WS-EOF-SW");
    }

    public void setWsEofSw(String value) {
        setString("WS-EOF-SW", value);
    }

    public int getWsEx() {
        return getInt("WS-EX");
    }

    public void setWsEx(int value) {
        setInt("WS-EX", value);
    }

    public int getWsExpCnt() {
        return getInt("WS-EXP-CNT");
    }

    public void setWsExpCnt(int value) {
        setInt("WS-EXP-CNT", value);
    }

    public long getWsExpId(int index) {
        return getLong("WS-EXP-ID", index);
    }

    public void setWsExpId(int index, long value) {
        setLong("WS-EXP-ID", value, index);
    }

    public int getWsMax() {
        return getInt("WS-MAX");
    }

    public void setWsMax(int value) {
        setInt("WS-MAX", value);
    }

    public int getWsMaxExp() {
        return getInt("WS-MAX-EXP");
    }

    public void setWsMaxExp(int value) {
        setInt("WS-MAX-EXP", value);
    }

    public BigDecimal getWsMinDue() {
        return getDecimal("WS-MIN-DUE");
    }

    public void setWsMinDue(BigDecimal value) {
        setDecimal("WS-MIN-DUE", value);
    }

    public BigDecimal getWsMinDueFloor() {
        return getDecimal("WS-MIN-DUE-FLOOR");
    }

    public void setWsMinDueFloor(BigDecimal value) {
        setDecimal("WS-MIN-DUE-FLOOR", value);
    }

    public BigDecimal getWsMinDuePct() {
        return getDecimal("WS-MIN-DUE-PCT");
    }

    public void setWsMinDuePct(BigDecimal value) {
        setDecimal("WS-MIN-DUE-PCT", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public BigDecimal getWsShortfall() {
        return getDecimal("WS-SHORTFALL");
    }

    public void setWsShortfall(BigDecimal value) {
        setDecimal("WS-SHORTFALL", value);
    }

    public BigDecimal getWsUtil() {
        return getDecimal("WS-UTIL");
    }

    public void setWsUtil(BigDecimal value) {
        setDecimal("WS-UTIL", value);
    }
}

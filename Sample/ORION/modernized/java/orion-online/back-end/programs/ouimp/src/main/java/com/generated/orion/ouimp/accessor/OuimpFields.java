package com.generated.orion.ouimp.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ouimp.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OUIMP. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OuimpFields extends DynamicFieldAccessor {

    public OuimpFields(WorkingStorage ws) {
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

    public String getAcAddrZip() {
        return getString("AC-ADDR-ZIP");
    }

    public void setAcAddrZip(String value) {
        setString("AC-ADDR-ZIP", value);
    }

    public BigDecimal getAcCashLimit() {
        return getDecimal("AC-CASH-LIMIT");
    }

    public void setAcCashLimit(BigDecimal value) {
        setDecimal("AC-CASH-LIMIT", value);
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

    public BigDecimal getAcCycDebit() {
        return getDecimal("AC-CYC-DEBIT");
    }

    public void setAcCycDebit(BigDecimal value) {
        setDecimal("AC-CYC-DEBIT", value);
    }

    public String getAcExpiryDate() {
        return getString("AC-EXPIRY-DATE");
    }

    public void setAcExpiryDate(String value) {
        setString("AC-EXPIRY-DATE", value);
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

    public String getAcOpenDate() {
        return getString("AC-OPEN-DATE");
    }

    public void setAcOpenDate(String value) {
        setString("AC-OPEN-DATE", value);
    }

    public String getAcReissueDate() {
        return getString("AC-REISSUE-DATE");
    }

    public void setAcReissueDate(String value) {
        setString("AC-REISSUE-DATE", value);
    }

    public String getAcctRec() {
        return groupToString("ACCT-REC");
    }

    public void setAcctRec(String value) {
        setGroup("ACCT-REC", value);
    }

    public String getImpData() {
        return getString("IMP-DATA");
    }

    public void setImpData(String value) {
        setString("IMP-DATA", value);
    }

    public String getImpKey() {
        return getString("IMP-KEY");
    }

    public void setImpKey(String value) {
        setString("IMP-KEY", value);
    }

    public int getKimAccepted() {
        return getInt("KIM-ACCEPTED");
    }

    public void setKimAccepted(int value) {
        setInt("KIM-ACCEPTED", value);
    }

    public int getKimAdded() {
        return getInt("KIM-ADDED");
    }

    public void setKimAdded(int value) {
        setInt("KIM-ADDED", value);
    }

    public int getKimErrors() {
        return getInt("KIM-ERRORS");
    }

    public void setKimErrors(int value) {
        setInt("KIM-ERRORS", value);
    }

    public String getKimLastKey() {
        return getString("KIM-LAST-KEY");
    }

    public void setKimLastKey(String value) {
        setString("KIM-LAST-KEY", value);
    }

    public String getKimLastReason() {
        return getString("KIM-LAST-REASON");
    }

    public void setKimLastReason(String value) {
        setString("KIM-LAST-REASON", value);
    }

    public int getKimMax() {
        return getInt("KIM-MAX");
    }

    public void setKimMax(int value) {
        setInt("KIM-MAX", value);
    }

    public String getKimMore() {
        return getString("KIM-MORE");
    }

    public void setKimMore(String value) {
        setString("KIM-MORE", value);
    }

    public String getKimMsg() {
        return getString("KIM-MSG");
    }

    public void setKimMsg(String value) {
        setString("KIM-MSG", value);
    }

    public String getKimNextKey() {
        return getString("KIM-NEXT-KEY");
    }

    public void setKimNextKey(String value) {
        setString("KIM-NEXT-KEY", value);
    }

    public int getKimRead() {
        return getInt("KIM-READ");
    }

    public void setKimRead(int value) {
        setInt("KIM-READ", value);
    }

    public int getKimRejected() {
        return getInt("KIM-REJECTED");
    }

    public void setKimRejected(int value) {
        setInt("KIM-REJECTED", value);
    }

    public int getKimSkipped() {
        return getInt("KIM-SKIPPED");
    }

    public void setKimSkipped(int value) {
        setInt("KIM-SKIPPED", value);
    }

    public String getKimStartKey() {
        return getString("KIM-START-KEY");
    }

    public void setKimStartKey(String value) {
        setString("KIM-START-KEY", value);
    }

    public String getKimStatus() {
        return getString("KIM-STATUS");
    }

    public void setKimStatus(String value) {
        setString("KIM-STATUS", value);
    }

    public int getKimUpdated() {
        return getInt("KIM-UPDATED");
    }

    public void setKimUpdated(int value) {
        setInt("KIM-UPDATED", value);
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

    public String getWsDt() {
        return getString("WS-DT");
    }

    public void setWsDt(String value) {
        setString("WS-DT", value);
    }

    public String getWsDtIn() {
        return getString("WS-DT-IN");
    }

    public void setWsDtIn(String value) {
        setString("WS-DT-IN", value);
    }

    public String getWsDtOk() {
        return getString("WS-DT-OK");
    }

    public void setWsDtOk(String value) {
        setString("WS-DT-OK", value);
    }

    public String getWsEofSw() {
        return getString("WS-EOF-SW");
    }

    public void setWsEofSw(String value) {
        setString("WS-EOF-SW", value);
    }

    public String getWsExistSw() {
        return getString("WS-EXIST-SW");
    }

    public void setWsExistSw(String value) {
        setString("WS-EXIST-SW", value);
    }

    public String getWsFBal() {
        return getString("WS-F-BAL");
    }

    public void setWsFBal(String value) {
        setString("WS-F-BAL", value);
    }

    public String getWsFCrlim() {
        return getString("WS-F-CRLIM");
    }

    public void setWsFCrlim(String value) {
        setString("WS-F-CRLIM", value);
    }

    public String getWsFCslim() {
        return getString("WS-F-CSLIM");
    }

    public void setWsFCslim(String value) {
        setString("WS-F-CSLIM", value);
    }

    public String getWsFCycr() {
        return getString("WS-F-CYCR");
    }

    public void setWsFCycr(String value) {
        setString("WS-F-CYCR", value);
    }

    public String getWsFCydr() {
        return getString("WS-F-CYDR");
    }

    public void setWsFCydr(String value) {
        setString("WS-F-CYDR", value);
    }

    public String getWsFExpiry() {
        return getString("WS-F-EXPIRY");
    }

    public void setWsFExpiry(String value) {
        setString("WS-F-EXPIRY", value);
    }

    public String getWsFGroup() {
        return getString("WS-F-GROUP");
    }

    public void setWsFGroup(String value) {
        setString("WS-F-GROUP", value);
    }

    public String getWsFId() {
        return getString("WS-F-ID");
    }

    public void setWsFId(String value) {
        setString("WS-F-ID", value);
    }

    public String getWsFOpen() {
        return getString("WS-F-OPEN");
    }

    public void setWsFOpen(String value) {
        setString("WS-F-OPEN", value);
    }

    public String getWsFReiss() {
        return getString("WS-F-REISS");
    }

    public void setWsFReiss(String value) {
        setString("WS-F-REISS", value);
    }

    public String getWsFStatus() {
        return getString("WS-F-STATUS");
    }

    public void setWsFStatus(String value) {
        setString("WS-F-STATUS", value);
    }

    public String getWsFType() {
        return getString("WS-F-TYPE");
    }

    public void setWsFType(String value) {
        setString("WS-F-TYPE", value);
    }

    public String getWsFZip() {
        return getString("WS-F-ZIP");
    }

    public void setWsFZip(String value) {
        setString("WS-F-ZIP", value);
    }

    public String getWsFields() {
        return groupToString("WS-FIELDS");
    }

    public void setWsFields(String value) {
        setGroup("WS-FIELDS", value);
    }

    public int getWsFldCnt() {
        return getInt("WS-FLD-CNT");
    }

    public void setWsFldCnt(int value) {
        setInt("WS-FLD-CNT", value);
    }

    public String getWsImpfile() {
        return getString("WS-IMPFILE");
    }

    public void setWsImpfile(String value) {
        setString("WS-IMPFILE", value);
    }

    public int getWsMax() {
        return getInt("WS-MAX");
    }

    public void setWsMax(int value) {
        setInt("WS-MAX", value);
    }

    public BigDecimal getWsNBal() {
        return getDecimal("WS-N-BAL");
    }

    public void setWsNBal(BigDecimal value) {
        setDecimal("WS-N-BAL", value);
    }

    public BigDecimal getWsNCrlim() {
        return getDecimal("WS-N-CRLIM");
    }

    public void setWsNCrlim(BigDecimal value) {
        setDecimal("WS-N-CRLIM", value);
    }

    public BigDecimal getWsNCslim() {
        return getDecimal("WS-N-CSLIM");
    }

    public void setWsNCslim(BigDecimal value) {
        setDecimal("WS-N-CSLIM", value);
    }

    public BigDecimal getWsNCycr() {
        return getDecimal("WS-N-CYCR");
    }

    public void setWsNCycr(BigDecimal value) {
        setDecimal("WS-N-CYCR", value);
    }

    public BigDecimal getWsNCydr() {
        return getDecimal("WS-N-CYDR");
    }

    public void setWsNCydr(BigDecimal value) {
        setDecimal("WS-N-CYDR", value);
    }

    public long getWsNId() {
        return getLong("WS-N-ID");
    }

    public void setWsNId(long value) {
        setLong("WS-N-ID", value);
    }

    public String getWsRejReason() {
        return getString("WS-REJ-REASON");
    }

    public void setWsRejReason(String value) {
        setString("WS-REJ-REASON", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public String getWsStatusChk() {
        return getString("WS-STATUS-CHK");
    }

    public void setWsStatusChk(String value) {
        setString("WS-STATUS-CHK", value);
    }

    public String getWsTypeChk() {
        return getString("WS-TYPE-CHK");
    }

    public void setWsTypeChk(String value) {
        setString("WS-TYPE-CHK", value);
    }

    public String getWsValidSw() {
        return getString("WS-VALID-SW");
    }

    public void setWsValidSw(String value) {
        setString("WS-VALID-SW", value);
    }
}

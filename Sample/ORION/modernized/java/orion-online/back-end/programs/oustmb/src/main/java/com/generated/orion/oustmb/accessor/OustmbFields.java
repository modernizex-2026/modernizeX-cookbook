package com.generated.orion.oustmb.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.oustmb.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OUSTMB. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OustmbFields extends DynamicFieldAccessor {

    public OustmbFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public BigDecimal getAcCurrBal() {
        return getDecimal("AC-CURR-BAL");
    }

    public void setAcCurrBal(BigDecimal value) {
        setDecimal("AC-CURR-BAL", value);
    }

    public long getAcId() {
        return getLong("AC-ID");
    }

    public void setAcId(long value) {
        setLong("AC-ID", value);
    }

    public int getKsmAcctRead() {
        return getInt("KSM-ACCT-READ");
    }

    public void setKsmAcctRead(int value) {
        setInt("KSM-ACCT-READ", value);
    }

    public int getKsmCycle() {
        return getInt("KSM-CYCLE");
    }

    public void setKsmCycle(int value) {
        setInt("KSM-CYCLE", value);
    }

    public String getKsmDueDate() {
        return getString("KSM-DUE-DATE");
    }

    public void setKsmDueDate(String value) {
        setString("KSM-DUE-DATE", value);
    }

    public int getKsmErrors() {
        return getInt("KSM-ERRORS");
    }

    public void setKsmErrors(int value) {
        setInt("KSM-ERRORS", value);
    }

    public int getKsmMax() {
        return getInt("KSM-MAX");
    }

    public void setKsmMax(int value) {
        setInt("KSM-MAX", value);
    }

    public String getKsmMore() {
        return getString("KSM-MORE");
    }

    public void setKsmMore(String value) {
        setString("KSM-MORE", value);
    }

    public String getKsmMsg() {
        return getString("KSM-MSG");
    }

    public void setKsmMsg(String value) {
        setString("KSM-MSG", value);
    }

    public long getKsmNextAcct() {
        return getLong("KSM-NEXT-ACCT");
    }

    public void setKsmNextAcct(long value) {
        setLong("KSM-NEXT-ACCT", value);
    }

    public int getKsmNoTran() {
        return getInt("KSM-NO-TRAN");
    }

    public void setKsmNoTran(int value) {
        setInt("KSM-NO-TRAN", value);
    }

    public long getKsmStartAcct() {
        return getLong("KSM-START-ACCT");
    }

    public void setKsmStartAcct(long value) {
        setLong("KSM-START-ACCT", value);
    }

    public String getKsmStatus() {
        return getString("KSM-STATUS");
    }

    public void setKsmStatus(String value) {
        setString("KSM-STATUS", value);
    }

    public int getKsmStmtWritten() {
        return getInt("KSM-STMT-WRITTEN");
    }

    public void setKsmStmtWritten(int value) {
        setInt("KSM-STMT-WRITTEN", value);
    }

    public BigDecimal getKsmTotCredit() {
        return getDecimal("KSM-TOT-CREDIT");
    }

    public void setKsmTotCredit(BigDecimal value) {
        setDecimal("KSM-TOT-CREDIT", value);
    }

    public BigDecimal getKsmTotDebit() {
        return getDecimal("KSM-TOT-DEBIT");
    }

    public void setKsmTotDebit(BigDecimal value) {
        setDecimal("KSM-TOT-DEBIT", value);
    }

    public String getOrionCommarea() {
        return groupToString("ORION-COMMAREA");
    }

    public void setOrionCommarea(String value) {
        setGroup("ORION-COMMAREA", value);
    }

    public long getStAcctId() {
        return getLong("ST-ACCT-ID");
    }

    public void setStAcctId(long value) {
        setLong("ST-ACCT-ID", value);
    }

    public BigDecimal getStCloseBal() {
        return getDecimal("ST-CLOSE-BAL");
    }

    public void setStCloseBal(BigDecimal value) {
        setDecimal("ST-CLOSE-BAL", value);
    }

    public int getStCycle() {
        return getInt("ST-CYCLE");
    }

    public void setStCycle(int value) {
        setInt("ST-CYCLE", value);
    }

    public String getStDueDate() {
        return getString("ST-DUE-DATE");
    }

    public void setStDueDate(String value) {
        setString("ST-DUE-DATE", value);
    }

    public String getStKey() {
        return groupToString("ST-KEY");
    }

    public void setStKey(String value) {
        setGroup("ST-KEY", value);
    }

    public BigDecimal getStMinDue() {
        return getDecimal("ST-MIN-DUE");
    }

    public void setStMinDue(BigDecimal value) {
        setDecimal("ST-MIN-DUE", value);
    }

    public BigDecimal getStOpenBal() {
        return getDecimal("ST-OPEN-BAL");
    }

    public void setStOpenBal(BigDecimal value) {
        setDecimal("ST-OPEN-BAL", value);
    }

    public BigDecimal getStTotalCredit() {
        return getDecimal("ST-TOTAL-CREDIT");
    }

    public void setStTotalCredit(BigDecimal value) {
        setDecimal("ST-TOTAL-CREDIT", value);
    }

    public BigDecimal getStTotalDebit() {
        return getDecimal("ST-TOTAL-DEBIT");
    }

    public void setStTotalDebit(BigDecimal value) {
        setDecimal("ST-TOTAL-DEBIT", value);
    }

    public String getStmtRec() {
        return groupToString("STMT-REC");
    }

    public void setStmtRec(String value) {
        setGroup("STMT-REC", value);
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

    public String getTrProcTs() {
        return getString("TR-PROC-TS");
    }

    public void setTrProcTs(String value) {
        setString("TR-PROC-TS", value);
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

    public int getWsCardCnt() {
        return getInt("WS-CARD-CNT");
    }

    public void setWsCardCnt(int value) {
        setInt("WS-CARD-CNT", value);
    }

    public String getWsCardNum(int index) {
        return getString("WS-CARD-NUM", index);
    }

    public void setWsCardNum(int index, String value) {
        setString("WS-CARD-NUM", value, index);
    }

    public BigDecimal getWsCloseBal() {
        return getDecimal("WS-CLOSE-BAL");
    }

    public void setWsCloseBal(BigDecimal value) {
        setDecimal("WS-CLOSE-BAL", value);
    }

    public int getWsCx() {
        return getInt("WS-CX");
    }

    public void setWsCx(int value) {
        setInt("WS-CX", value);
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

    public int getWsMaxCards() {
        return getInt("WS-MAX-CARDS");
    }

    public void setWsMaxCards(int value) {
        setInt("WS-MAX-CARDS", value);
    }

    public BigDecimal getWsMinDue() {
        return getDecimal("WS-MIN-DUE");
    }

    public void setWsMinDue(BigDecimal value) {
        setDecimal("WS-MIN-DUE", value);
    }

    public BigDecimal getWsOpenBal() {
        return getDecimal("WS-OPEN-BAL");
    }

    public void setWsOpenBal(BigDecimal value) {
        setDecimal("WS-OPEN-BAL", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public String getWsStmtfile() {
        return getString("WS-STMTFILE");
    }

    public void setWsStmtfile(String value) {
        setString("WS-STMTFILE", value);
    }

    public int getWsTcyc() {
        return getInt("WS-TCYC");
    }

    public void setWsTcyc(int value) {
        setInt("WS-TCYC", value);
    }

    public int getWsTcycMm() {
        return getInt("WS-TCYC-MM");
    }

    public void setWsTcycMm(int value) {
        setInt("WS-TCYC-MM", value);
    }

    public int getWsTcycYy() {
        return getInt("WS-TCYC-YY");
    }

    public void setWsTcycYy(int value) {
        setInt("WS-TCYC-YY", value);
    }

    public BigDecimal getWsTotCredit() {
        return getDecimal("WS-TOT-CREDIT");
    }

    public void setWsTotCredit(BigDecimal value) {
        setDecimal("WS-TOT-CREDIT", value);
    }

    public BigDecimal getWsTotDebit() {
        return getDecimal("WS-TOT-DEBIT");
    }

    public void setWsTotDebit(BigDecimal value) {
        setDecimal("WS-TOT-DEBIT", value);
    }

    public String getWsTranEof() {
        return getString("WS-TRAN-EOF");
    }

    public void setWsTranEof(String value) {
        setString("WS-TRAN-EOF", value);
    }

    public String getWsTranPath() {
        return getString("WS-TRAN-PATH");
    }

    public void setWsTranPath(String value) {
        setString("WS-TRAN-PATH", value);
    }

    public int getWsTranThis() {
        return getInt("WS-TRAN-THIS");
    }

    public void setWsTranThis(int value) {
        setInt("WS-TRAN-THIS", value);
    }

    public String getWsXrefEof() {
        return getString("WS-XREF-EOF");
    }

    public void setWsXrefEof(String value) {
        setString("WS-XREF-EOF", value);
    }

    public String getWsXrefPath() {
        return getString("WS-XREF-PATH");
    }

    public void setWsXrefPath(String value) {
        setString("WS-XREF-PATH", value);
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

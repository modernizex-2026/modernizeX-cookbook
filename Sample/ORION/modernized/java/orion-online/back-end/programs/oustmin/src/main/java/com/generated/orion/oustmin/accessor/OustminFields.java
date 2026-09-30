package com.generated.orion.oustmin.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.oustmin.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OUSTMIN. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OustminFields extends DynamicFieldAccessor {

    public OustminFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getKsbMaxRows() {
        return getInt("KSB-MAX-ROWS");
    }

    public void setKsbMaxRows(int value) {
        setInt("KSB-MAX-ROWS", value);
    }

    public String getKsbMode() {
        return getString("KSB-MODE");
    }

    public void setKsbMode(String value) {
        setString("KSB-MODE", value);
    }

    public String getKsbMore() {
        return getString("KSB-MORE");
    }

    public void setKsbMore(String value) {
        setString("KSB-MORE", value);
    }

    public long getKsbNextAcct() {
        return getLong("KSB-NEXT-ACCT");
    }

    public void setKsbNextAcct(long value) {
        setLong("KSB-NEXT-ACCT", value);
    }

    public int getKsbNextCycle() {
        return getInt("KSB-NEXT-CYCLE");
    }

    public void setKsbNextCycle(int value) {
        setInt("KSB-NEXT-CYCLE", value);
    }

    public long getKsbRAcct(int index) {
        return getLong("KSB-R-ACCT", index);
    }

    public void setKsbRAcct(int index, long value) {
        setLong("KSB-R-ACCT", value, index);
    }

    public BigDecimal getKsbRClose(int index) {
        return getDecimal("KSB-R-CLOSE", index);
    }

    public void setKsbRClose(int index, BigDecimal value) {
        setDecimal("KSB-R-CLOSE", value, index);
    }

    public BigDecimal getKsbRCredit(int index) {
        return getDecimal("KSB-R-CREDIT", index);
    }

    public void setKsbRCredit(int index, BigDecimal value) {
        setDecimal("KSB-R-CREDIT", value, index);
    }

    public int getKsbRCycle(int index) {
        return getInt("KSB-R-CYCLE", index);
    }

    public void setKsbRCycle(int index, int value) {
        setInt("KSB-R-CYCLE", value, index);
    }

    public BigDecimal getKsbRDebit(int index) {
        return getDecimal("KSB-R-DEBIT", index);
    }

    public void setKsbRDebit(int index, BigDecimal value) {
        setDecimal("KSB-R-DEBIT", value, index);
    }

    public String getKsbRDuedt(int index) {
        return getString("KSB-R-DUEDT", index);
    }

    public void setKsbRDuedt(int index, String value) {
        setString("KSB-R-DUEDT", value, index);
    }

    public BigDecimal getKsbRMindue(int index) {
        return getDecimal("KSB-R-MINDUE", index);
    }

    public void setKsbRMindue(int index, BigDecimal value) {
        setDecimal("KSB-R-MINDUE", value, index);
    }

    public BigDecimal getKsbROpen(int index) {
        return getDecimal("KSB-R-OPEN", index);
    }

    public void setKsbROpen(int index, BigDecimal value) {
        setDecimal("KSB-R-OPEN", value, index);
    }

    public int getKsbRowCnt() {
        return getInt("KSB-ROW-CNT");
    }

    public void setKsbRowCnt(int value) {
        setInt("KSB-ROW-CNT", value);
    }

    public long getKsbStartAcct() {
        return getLong("KSB-START-ACCT");
    }

    public void setKsbStartAcct(long value) {
        setLong("KSB-START-ACCT", value);
    }

    public int getKsbStartCycle() {
        return getInt("KSB-START-CYCLE");
    }

    public void setKsbStartCycle(int value) {
        setInt("KSB-START-CYCLE", value);
    }

    public String getKsbStatus() {
        return getString("KSB-STATUS");
    }

    public void setKsbStatus(String value) {
        setString("KSB-STATUS", value);
    }

    public String getKstmbParm() {
        return groupToString("KSTMB-PARM");
    }

    public void setKstmbParm(String value) {
        setGroup("KSTMB-PARM", value);
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

    public String getWdlCtx() {
        return getString("WDL-CTX");
    }

    public void setWdlCtx(String value) {
        setString("WDL-CTX", value);
    }

    public int getWdlResp() {
        return getInt("WDL-RESP");
    }

    public void setWdlResp(int value) {
        setInt("WDL-RESP", value);
    }

    public String getWsBrowseSw() {
        return getString("WS-BROWSE-SW");
    }

    public void setWsBrowseSw(String value) {
        setString("WS-BROWSE-SW", value);
    }

    public String getWsDiagCtx() {
        return getString("WS-DIAG-CTX");
    }

    public void setWsDiagCtx(String value) {
        setString("WS-DIAG-CTX", value);
    }

    public String getWsDiagLine() {
        return groupToString("WS-DIAG-LINE");
    }

    public void setWsDiagLine(String value) {
        setGroup("WS-DIAG-LINE", value);
    }

    public String getWsDiagMsg() {
        return getString("WS-DIAG-MSG");
    }

    public void setWsDiagMsg(String value) {
        setString("WS-DIAG-MSG", value);
    }

    public String getWsDiagQname() {
        return getString("WS-DIAG-QNAME");
    }

    public void setWsDiagQname(String value) {
        setString("WS-DIAG-QNAME", value);
    }

    public int getWsDiagResp() {
        return getInt("WS-DIAG-RESP");
    }

    public void setWsDiagResp(int value) {
        setInt("WS-DIAG-RESP", value);
    }

    public String getWsEofSw() {
        return getString("WS-EOF-SW");
    }

    public void setWsEofSw(String value) {
        setString("WS-EOF-SW", value);
    }

    public int getWsIdx() {
        return getInt("WS-IDX");
    }

    public void setWsIdx(int value) {
        setInt("WS-IDX", value);
    }

    public int getWsMaxRows() {
        return getInt("WS-MAX-ROWS");
    }

    public void setWsMaxRows(int value) {
        setInt("WS-MAX-ROWS", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public int getWsRowCnt() {
        return getInt("WS-ROW-CNT");
    }

    public void setWsRowCnt(int value) {
        setInt("WS-ROW-CNT", value);
    }

    public String getWsStmtfile() {
        return getString("WS-STMTFILE");
    }

    public void setWsStmtfile(String value) {
        setString("WS-STMTFILE", value);
    }
}

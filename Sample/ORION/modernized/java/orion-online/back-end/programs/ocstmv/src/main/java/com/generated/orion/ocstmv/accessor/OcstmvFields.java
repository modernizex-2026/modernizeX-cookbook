package com.generated.orion.ocstmv.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ocstmv.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OCSTMV. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OcstmvFields extends DynamicFieldAccessor {

    public OcstmvFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public String getAcctidi() {
        return getString("ACCTIDI");
    }

    public void setAcctidi(String value) {
        setString("ACCTIDI", value);
    }

    public int getAcctidl() {
        return getInt("ACCTIDL");
    }

    public void setAcctidl(int value) {
        setInt("ACCTIDL", value);
    }

    public String getAcctido() {
        return getString("ACCTIDO");
    }

    public void setAcctido(String value) {
        setString("ACCTIDO", value);
    }

    public String getCaErrMsg() {
        return getString("CA-ERR-MSG");
    }

    public void setCaErrMsg(String value) {
        setString("CA-ERR-MSG", value);
    }

    public String getCaFromProgram() {
        return getString("CA-FROM-PROGRAM");
    }

    public void setCaFromProgram(String value) {
        setString("CA-FROM-PROGRAM", value);
    }

    public String getCaFromTranid() {
        return getString("CA-FROM-TRANID");
    }

    public void setCaFromTranid(String value) {
        setString("CA-FROM-TRANID", value);
    }

    public int getCaPgmContext() {
        return getInt("CA-PGM-CONTEXT");
    }

    public void setCaPgmContext(int value) {
        setInt("CA-PGM-CONTEXT", value);
    }

    public String getCurdateo() {
        return getString("CURDATEO");
    }

    public void setCurdateo(String value) {
        setString("CURDATEO", value);
    }

    public String getCurtimeo() {
        return getString("CURTIMEO");
    }

    public void setCurtimeo(String value) {
        setString("CURTIMEO", value);
    }

    public String getErrmsgo() {
        return getString("ERRMSGO");
    }

    public void setErrmsgo(String value) {
        setString("ERRMSGO", value);
    }

    public String getOrionCommarea() {
        return groupToString("ORION-COMMAREA");
    }

    public void setOrionCommarea(String value) {
        setGroup("ORION-COMMAREA", value);
    }

    public String getPgmnameo() {
        return getString("PGMNAMEO");
    }

    public void setPgmnameo(String value) {
        setString("PGMNAMEO", value);
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

    public String getStcloseo() {
        return getString("STCLOSEO");
    }

    public void setStcloseo(String value) {
        setString("STCLOSEO", value);
    }

    public String getStcyci() {
        return getString("STCYCI");
    }

    public void setStcyci(String value) {
        setString("STCYCI", value);
    }

    public String getStcyco() {
        return getString("STCYCO");
    }

    public void setStcyco(String value) {
        setString("STCYCO", value);
    }

    public String getStdueo() {
        return getString("STDUEO");
    }

    public void setStdueo(String value) {
        setString("STDUEO", value);
    }

    public String getStmino() {
        return getString("STMINO");
    }

    public void setStmino(String value) {
        setString("STMINO", value);
    }

    public String getStopeno() {
        return getString("STOPENO");
    }

    public void setStopeno(String value) {
        setString("STOPENO", value);
    }

    public String getTitleo() {
        return getString("TITLEO");
    }

    public void setTitleo(String value) {
        setString("TITLEO", value);
    }

    public String getTrnnameo() {
        return getString("TRNNAMEO");
    }

    public void setTrnnameo(String value) {
        setString("TRNNAMEO", value);
    }

    public long getWsAcct() {
        return getLong("WS-ACCT");
    }

    public void setWsAcct(long value) {
        setLong("WS-ACCT", value);
    }

    public int getWsCycMm() {
        return getInt("WS-CYC-MM");
    }

    public void setWsCycMm(int value) {
        setInt("WS-CYC-MM", value);
    }

    public int getWsCycle() {
        return getInt("WS-CYCLE");
    }

    public void setWsCycle(int value) {
        setInt("WS-CYCLE", value);
    }

    public BigDecimal getWsEdBal() {
        return getDecimal("WS-ED-BAL");
    }

    public void setWsEdBal(BigDecimal value) {
        setDecimal("WS-ED-BAL", value);
    }

    public String getWsFoundFlg() {
        return getString("WS-FOUND-FLG");
    }

    public void setWsFoundFlg(String value) {
        setString("WS-FOUND-FLG", value);
    }

    public String getWsHdrDate() {
        return getString("WS-HDR-DATE");
    }

    public void setWsHdrDate(String value) {
        setString("WS-HDR-DATE", value);
    }

    public String getWsHdrTime() {
        return getString("WS-HDR-TIME");
    }

    public void setWsHdrTime(String value) {
        setString("WS-HDR-TIME", value);
    }

    public String getWsHdrTitle() {
        return getString("WS-HDR-TITLE");
    }

    public void setWsHdrTitle(String value) {
        setString("WS-HDR-TITLE", value);
    }

    public String getWsMAcctNum() {
        return getString("WS-M-ACCT-NUM");
    }

    public void setWsMAcctNum(String value) {
        setString("WS-M-ACCT-NUM", value);
    }

    public String getWsMCycMm() {
        return getString("WS-M-CYC-MM");
    }

    public void setWsMCycMm(String value) {
        setString("WS-M-CYC-MM", value);
    }

    public String getWsMCycNum() {
        return getString("WS-M-CYC-NUM");
    }

    public void setWsMCycNum(String value) {
        setString("WS-M-CYC-NUM", value);
    }

    public String getWsMNotfnd() {
        return getString("WS-M-NOTFND");
    }

    public void setWsMNotfnd(String value) {
        setString("WS-M-NOTFND", value);
    }

    public String getWsMPrompt() {
        return getString("WS-M-PROMPT");
    }

    public void setWsMPrompt(String value) {
        setString("WS-M-PROMPT", value);
    }

    public String getWsMShown() {
        return getString("WS-M-SHOWN");
    }

    public void setWsMShown(String value) {
        setString("WS-M-SHOWN", value);
    }

    public String getWsMenuPgm() {
        return getString("WS-MENU-PGM");
    }

    public void setWsMenuPgm(String value) {
        setString("WS-MENU-PGM", value);
    }

    public String getWsMsgInvalidKey() {
        return getString("WS-MSG-INVALID-KEY");
    }

    public void setWsMsgInvalidKey(String value) {
        setString("WS-MSG-INVALID-KEY", value);
    }

    public String getWsMsgText() {
        return getString("WS-MSG-TEXT");
    }

    public void setWsMsgText(String value) {
        setString("WS-MSG-TEXT", value);
    }

    public String getWsNcChar() {
        return getString("WS-NC-CHAR");
    }

    public void setWsNcChar(String value) {
        setString("WS-NC-CHAR", value);
    }

    public int getWsNcDigit() {
        return getInt("WS-NC-DIGIT");
    }

    public void setWsNcDigit(int value) {
        setInt("WS-NC-DIGIT", value);
    }

    public int getWsNcDigits() {
        return getInt("WS-NC-DIGITS");
    }

    public void setWsNcDigits(int value) {
        setInt("WS-NC-DIGITS", value);
    }

    public String getWsNcIn() {
        return getString("WS-NC-IN");
    }

    public void setWsNcIn(String value) {
        setString("WS-NC-IN", value);
    }

    public int getWsNcLen() {
        return getInt("WS-NC-LEN");
    }

    public void setWsNcLen(int value) {
        setInt("WS-NC-LEN", value);
    }

    public int getWsNcPos() {
        return getInt("WS-NC-POS");
    }

    public void setWsNcPos(int value) {
        setInt("WS-NC-POS", value);
    }

    public long getWsNcValue() {
        return getLong("WS-NC-VALUE");
    }

    public void setWsNcValue(long value) {
        setLong("WS-NC-VALUE", value);
    }

    public String getWsPgmname() {
        return getString("WS-PGMNAME");
    }

    public void setWsPgmname(String value) {
        setString("WS-PGMNAME", value);
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

    public String getWsTranid() {
        return getString("WS-TRANID");
    }

    public void setWsTranid(String value) {
        setString("WS-TRANID", value);
    }

    public String getWsValidSw() {
        return getString("WS-VALID-SW");
    }

    public void setWsValidSw(String value) {
        setString("WS-VALID-SW", value);
    }
}

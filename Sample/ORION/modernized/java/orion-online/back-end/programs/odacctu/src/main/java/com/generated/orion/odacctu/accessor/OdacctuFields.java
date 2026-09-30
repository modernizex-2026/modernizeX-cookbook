package com.generated.orion.odacctu.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.odacctu.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for ODACCTU. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OdacctuFields extends DynamicFieldAccessor {

    public OdacctuFields(WorkingStorage ws) {
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

    public String getAccrlimi() {
        return getString("ACCRLIMI");
    }

    public void setAccrlimi(String value) {
        setString("ACCRLIMI", value);
    }

    public String getAccrlimo() {
        return getString("ACCRLIMO");
    }

    public void setAccrlimo(String value) {
        setString("ACCRLIMO", value);
    }

    public String getAccslimi() {
        return getString("ACCSLIMI");
    }

    public void setAccslimi(String value) {
        setString("ACCSLIMI", value);
    }

    public String getAccslimo() {
        return getString("ACCSLIMO");
    }

    public void setAccslimo(String value) {
        setString("ACCSLIMO", value);
    }

    public String getAcctidi() {
        return getString("ACCTIDI");
    }

    public void setAcctidi(String value) {
        setString("ACCTIDI", value);
    }

    public String getAcctido() {
        return getString("ACCTIDO");
    }

    public void setAcctido(String value) {
        setString("ACCTIDO", value);
    }

    public String getAcexpi() {
        return getString("ACEXPI");
    }

    public void setAcexpi(String value) {
        setString("ACEXPI", value);
    }

    public String getAcexpo() {
        return getString("ACEXPO");
    }

    public void setAcexpo(String value) {
        setString("ACEXPO", value);
    }

    public String getAcgrpi() {
        return getString("ACGRPI");
    }

    public void setAcgrpi(String value) {
        setString("ACGRPI", value);
    }

    public String getAcgrpo() {
        return getString("ACGRPO");
    }

    public void setAcgrpo(String value) {
        setString("ACGRPO", value);
    }

    public String getAcstati() {
        return getString("ACSTATI");
    }

    public void setAcstati(String value) {
        setString("ACSTATI", value);
    }

    public String getAcstato() {
        return getString("ACSTATO");
    }

    public void setAcstato(String value) {
        setString("ACSTATO", value);
    }

    public long getCaAcctId() {
        return getLong("CA-ACCT-ID");
    }

    public void setCaAcctId(long value) {
        setLong("CA-ACCT-ID", value);
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

    public String getCaWorkArea() {
        return getString("CA-WORK-AREA");
    }

    public void setCaWorkArea(String value) {
        setString("CA-WORK-AREA", value);
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

    public int getSqlcode() {
        return getInt("SQLCODE");
    }

    public void setSqlcode(int value) {
        setInt("SQLCODE", value);
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

    public BigDecimal getWsEdAmt() {
        return getDecimal("WS-ED-AMT");
    }

    public void setWsEdAmt(BigDecimal value) {
        setDecimal("WS-ED-AMT", value);
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

    public String getWsMenuPgm() {
        return getString("WS-MENU-PGM");
    }

    public void setWsMenuPgm(String value) {
        setString("WS-MENU-PGM", value);
    }

    public String getWsModeFlag() {
        return getString("WS-MODE-FLAG");
    }

    public void setWsModeFlag(String value) {
        setString("WS-MODE-FLAG", value);
    }

    public String getWsMsgInvalidKey() {
        return getString("WS-MSG-INVALID-KEY");
    }

    public void setWsMsgInvalidKey(String value) {
        setString("WS-MSG-INVALID-KEY", value);
    }

    public String getWsMsgNotfnd() {
        return getString("WS-MSG-NOTFND");
    }

    public void setWsMsgNotfnd(String value) {
        setString("WS-MSG-NOTFND", value);
    }

    public String getWsMsgRequired() {
        return getString("WS-MSG-REQUIRED");
    }

    public void setWsMsgRequired(String value) {
        setString("WS-MSG-REQUIRED", value);
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

    public String getWsTranid() {
        return getString("WS-TRANID");
    }

    public void setWsTranid(String value) {
        setString("WS-TRANID", value);
    }

    public String getWsValidFlag() {
        return getString("WS-VALID-FLAG");
    }

    public void setWsValidFlag(String value) {
        setString("WS-VALID-FLAG", value);
    }
}

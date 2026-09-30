package com.generated.orion.ocacctu.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ocacctu.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OCACCTU. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OcacctuFields extends DynamicFieldAccessor {

    public OcacctuFields(WorkingStorage ws) {
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

    public String getKdDateIn() {
        return getString("KD-DATE-IN");
    }

    public void setKdDateIn(String value) {
        setString("KD-DATE-IN", value);
    }

    public String getKdDateOut() {
        return getString("KD-DATE-OUT");
    }

    public void setKdDateOut(String value) {
        setString("KD-DATE-OUT", value);
    }

    public String getKdFunc() {
        return getString("KD-FUNC");
    }

    public void setKdFunc(String value) {
        setString("KD-FUNC", value);
    }

    public String getKdStatus() {
        return getString("KD-STATUS");
    }

    public void setKdStatus(String value) {
        setString("KD-STATUS", value);
    }

    public String getKdateParm() {
        return groupToString("KDATE-PARM");
    }

    public void setKdateParm(String value) {
        setGroup("KDATE-PARM", value);
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

    public String getWsAcctfile() {
        return getString("WS-ACCTFILE");
    }

    public void setWsAcctfile(String value) {
        setString("WS-ACCTFILE", value);
    }

    public String getWsAeChar() {
        return getString("WS-AE-CHAR");
    }

    public void setWsAeChar(String value) {
        setString("WS-AE-CHAR", value);
    }

    public int getWsAeDigit() {
        return getInt("WS-AE-DIGIT");
    }

    public void setWsAeDigit(int value) {
        setInt("WS-AE-DIGIT", value);
    }

    public String getWsAeDotSw() {
        return getString("WS-AE-DOT-SW");
    }

    public void setWsAeDotSw(String value) {
        setString("WS-AE-DOT-SW", value);
    }

    public int getWsAeFrac() {
        return getInt("WS-AE-FRAC");
    }

    public void setWsAeFrac(int value) {
        setInt("WS-AE-FRAC", value);
    }

    public int getWsAeFracCnt() {
        return getInt("WS-AE-FRAC-CNT");
    }

    public void setWsAeFracCnt(int value) {
        setInt("WS-AE-FRAC-CNT", value);
    }

    public String getWsAeIn() {
        return getString("WS-AE-IN");
    }

    public void setWsAeIn(String value) {
        setString("WS-AE-IN", value);
    }

    public long getWsAeInt() {
        return getLong("WS-AE-INT");
    }

    public void setWsAeInt(long value) {
        setLong("WS-AE-INT", value);
    }

    public int getWsAeIntCnt() {
        return getInt("WS-AE-INT-CNT");
    }

    public void setWsAeIntCnt(int value) {
        setInt("WS-AE-INT-CNT", value);
    }

    public int getWsAePos() {
        return getInt("WS-AE-POS");
    }

    public void setWsAePos(int value) {
        setInt("WS-AE-POS", value);
    }

    public BigDecimal getWsAeResult() {
        return getDecimal("WS-AE-RESULT");
    }

    public void setWsAeResult(BigDecimal value) {
        setDecimal("WS-AE-RESULT", value);
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

    public BigDecimal getWsNewCrlim() {
        return getDecimal("WS-NEW-CRLIM");
    }

    public void setWsNewCrlim(BigDecimal value) {
        setDecimal("WS-NEW-CRLIM", value);
    }

    public BigDecimal getWsNewCslim() {
        return getDecimal("WS-NEW-CSLIM");
    }

    public void setWsNewCslim(BigDecimal value) {
        setDecimal("WS-NEW-CSLIM", value);
    }

    public String getWsNewExpiry() {
        return getString("WS-NEW-EXPIRY");
    }

    public void setWsNewExpiry(String value) {
        setString("WS-NEW-EXPIRY", value);
    }

    public String getWsNewGroup() {
        return getString("WS-NEW-GROUP");
    }

    public void setWsNewGroup(String value) {
        setString("WS-NEW-GROUP", value);
    }

    public String getWsNewStatus() {
        return getString("WS-NEW-STATUS");
    }

    public void setWsNewStatus(String value) {
        setString("WS-NEW-STATUS", value);
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

    public String getWsStEdit() {
        return getString("WS-ST-EDIT");
    }

    public void setWsStEdit(String value) {
        setString("WS-ST-EDIT", value);
    }

    public String getWsStKey() {
        return getString("WS-ST-KEY");
    }

    public void setWsStKey(String value) {
        setString("WS-ST-KEY", value);
    }

    public String getWsTranid() {
        return getString("WS-TRANID");
    }

    public void setWsTranid(String value) {
        setString("WS-TRANID", value);
    }

    public String getWsUpdSw() {
        return getString("WS-UPD-SW");
    }

    public void setWsUpdSw(String value) {
        setString("WS-UPD-SW", value);
    }

    public String getWsValidSw() {
        return getString("WS-VALID-SW");
    }

    public void setWsValidSw(String value) {
        setString("WS-VALID-SW", value);
    }
}

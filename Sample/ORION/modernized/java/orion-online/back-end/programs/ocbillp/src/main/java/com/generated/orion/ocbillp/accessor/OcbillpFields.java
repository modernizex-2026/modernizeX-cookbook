package com.generated.orion.ocbillp.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ocbillp.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OCBILLP. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OcbillpFields extends DynamicFieldAccessor {

    public OcbillpFields(WorkingStorage ws) {
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

    public String getBlConfirmNum() {
        return getString("BL-CONFIRM-NUM");
    }

    public void setBlConfirmNum(String value) {
        setString("BL-CONFIRM-NUM", value);
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

    public String getBlamti() {
        return getString("BLAMTI");
    }

    public void setBlamti(String value) {
        setString("BLAMTI", value);
    }

    public String getBlamto() {
        return getString("BLAMTO");
    }

    public void setBlamto(String value) {
        setString("BLAMTO", value);
    }

    public String getBlbalo() {
        return getString("BLBALO");
    }

    public void setBlbalo(String value) {
        setString("BLBALO", value);
    }

    public String getBlconfi() {
        return getString("BLCONFI");
    }

    public void setBlconfi(String value) {
        setString("BLCONFI", value);
    }

    public String getBlconfo() {
        return getString("BLCONFO");
    }

    public void setBlconfo(String value) {
        setString("BLCONFO", value);
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

    public String getWsAmtIn() {
        return getString("WS-AMT-IN");
    }

    public void setWsAmtIn(String value) {
        setString("WS-AMT-IN", value);
    }

    public String getWsBillfile() {
        return getString("WS-BILLFILE");
    }

    public void setWsBillfile(String value) {
        setString("WS-BILLFILE", value);
    }

    public String getWsCh() {
        return getString("WS-CH");
    }

    public void setWsCh(String value) {
        setString("WS-CH", value);
    }

    public String getWsConfirmNum() {
        return getString("WS-CONFIRM-NUM");
    }

    public void setWsConfirmNum(String value) {
        setString("WS-CONFIRM-NUM", value);
    }

    public String getWsCtrlfile() {
        return getString("WS-CTRLFILE");
    }

    public void setWsCtrlfile(String value) {
        setString("WS-CTRLFILE", value);
    }

    public int getWsDigCnt() {
        return getInt("WS-DIG-CNT");
    }

    public void setWsDigCnt(int value) {
        setInt("WS-DIG-CNT", value);
    }

    public int getWsDotCnt() {
        return getInt("WS-DOT-CNT");
    }

    public void setWsDotCnt(int value) {
        setInt("WS-DOT-CNT", value);
    }

    public BigDecimal getWsEdBal() {
        return getDecimal("WS-ED-BAL");
    }

    public void setWsEdBal(BigDecimal value) {
        setDecimal("WS-ED-BAL", value);
    }

    public String getWsErrFlg() {
        return getString("WS-ERR-FLG");
    }

    public void setWsErrFlg(String value) {
        setString("WS-ERR-FLG", value);
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

    public int getWsI() {
        return getInt("WS-I");
    }

    public void setWsI(int value) {
        setInt("WS-I", value);
    }

    public long getWsId11() {
        return getLong("WS-ID-11");
    }

    public void setWsId11(long value) {
        setLong("WS-ID-11", value);
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

    public BigDecimal getWsNewBal() {
        return getDecimal("WS-NEW-BAL");
    }

    public void setWsNewBal(BigDecimal value) {
        setDecimal("WS-NEW-BAL", value);
    }

    public BigDecimal getWsPayAmt() {
        return getDecimal("WS-PAY-AMT");
    }

    public void setWsPayAmt(BigDecimal value) {
        setDecimal("WS-PAY-AMT", value);
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

    public String getWsStage() {
        return getString("WS-STAGE");
    }

    public void setWsStage(String value) {
        setString("WS-STAGE", value);
    }

    public long getWsSvAcctId() {
        return getLong("WS-SV-ACCT-ID");
    }

    public void setWsSvAcctId(long value) {
        setLong("WS-SV-ACCT-ID", value);
    }

    public BigDecimal getWsSvBal() {
        return getDecimal("WS-SV-BAL");
    }

    public void setWsSvBal(BigDecimal value) {
        setDecimal("WS-SV-BAL", value);
    }

    public String getWsTranid() {
        return getString("WS-TRANID");
    }

    public void setWsTranid(String value) {
        setString("WS-TRANID", value);
    }

    public String getWsValidFlg() {
        return getString("WS-VALID-FLG");
    }

    public void setWsValidFlg(String value) {
        setString("WS-VALID-FLG", value);
    }
}

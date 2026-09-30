package com.generated.orion.ocauthq.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ocauthq.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OCAUTHQ. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OcauthqFields extends DynamicFieldAccessor {

    public OcauthqFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public String getAmounti() {
        return getString("AMOUNTI");
    }

    public void setAmounti(String value) {
        setString("AMOUNTI", value);
    }

    public String getAmounto() {
        return getString("AMOUNTO");
    }

    public void setAmounto(String value) {
        setString("AMOUNTO", value);
    }

    public long getAqAcctId() {
        return getLong("AQ-ACCT-ID");
    }

    public void setAqAcctId(long value) {
        setLong("AQ-ACCT-ID", value);
    }

    public BigDecimal getAqAmount() {
        return getDecimal("AQ-AMOUNT");
    }

    public void setAqAmount(BigDecimal value) {
        setDecimal("AQ-AMOUNT", value);
    }

    public String getAqCardNum() {
        return getString("AQ-CARD-NUM");
    }

    public void setAqCardNum(String value) {
        setString("AQ-CARD-NUM", value);
    }

    public int getAqMerchantId() {
        return getInt("AQ-MERCHANT-ID");
    }

    public void setAqMerchantId(int value) {
        setInt("AQ-MERCHANT-ID", value);
    }

    public String getAqMerchantName() {
        return getString("AQ-MERCHANT-NAME");
    }

    public void setAqMerchantName(String value) {
        setString("AQ-MERCHANT-NAME", value);
    }

    public String getAqMsgType() {
        return getString("AQ-MSG-TYPE");
    }

    public void setAqMsgType(String value) {
        setString("AQ-MSG-TYPE", value);
    }

    public String getAqRequestedTs() {
        return getString("AQ-REQUESTED-TS");
    }

    public void setAqRequestedTs(String value) {
        setString("AQ-REQUESTED-TS", value);
    }

    public BigDecimal getAsAvailCredit() {
        return getDecimal("AS-AVAIL-CREDIT");
    }

    public void setAsAvailCredit(BigDecimal value) {
        setDecimal("AS-AVAIL-CREDIT", value);
    }

    public String getAsDecision() {
        return getString("AS-DECISION");
    }

    public void setAsDecision(String value) {
        setString("AS-DECISION", value);
    }

    public String getAsReason() {
        return getString("AS-REASON");
    }

    public void setAsReason(String value) {
        setString("AS-REASON", value);
    }

    public String getAuthMsgArea() {
        return groupToString("AUTH-MSG-AREA");
    }

    public void setAuthMsgArea(String value) {
        setGroup("AUTH-MSG-AREA", value);
    }

    public String getAvailo() {
        return getString("AVAILO");
    }

    public void setAvailo(String value) {
        setString("AVAILO", value);
    }

    public String getCaCardNum() {
        return getString("CA-CARD-NUM");
    }

    public void setCaCardNum(String value) {
        setString("CA-CARD-NUM", value);
    }

    public String getCaErrFlg() {
        return getString("CA-ERR-FLG");
    }

    public void setCaErrFlg(String value) {
        setString("CA-ERR-FLG", value);
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

    public String getCardnumi() {
        return getString("CARDNUMI");
    }

    public void setCardnumi(String value) {
        setString("CARDNUMI", value);
    }

    public String getCardnumo() {
        return getString("CARDNUMO");
    }

    public void setCardnumo(String value) {
        setString("CARDNUMO", value);
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

    public String getDecisno() {
        return getString("DECISNO");
    }

    public void setDecisno(String value) {
        setString("DECISNO", value);
    }

    public String getErrmsgo() {
        return getString("ERRMSGO");
    }

    public void setErrmsgo(String value) {
        setString("ERRMSGO", value);
    }

    public String getMerchi() {
        return getString("MERCHI");
    }

    public void setMerchi(String value) {
        setString("MERCHI", value);
    }

    public String getMercho() {
        return getString("MERCHO");
    }

    public void setMercho(String value) {
        setString("MERCHO", value);
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

    public String getReasono() {
        return getString("REASONO");
    }

    public void setReasono(String value) {
        setString("REASONO", value);
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

    public BigDecimal getWsAmtNum() {
        return getDecimal("WS-AMT-NUM");
    }

    public void setWsAmtNum(BigDecimal value) {
        setDecimal("WS-AMT-NUM", value);
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

    public String getWsInAmt() {
        return getString("WS-IN-AMT");
    }

    public void setWsInAmt(String value) {
        setString("WS-IN-AMT", value);
    }

    public String getWsInCard() {
        return getString("WS-IN-CARD");
    }

    public void setWsInCard(String value) {
        setString("WS-IN-CARD", value);
    }

    public String getWsInMerch() {
        return getString("WS-IN-MERCH");
    }

    public void setWsInMerch(String value) {
        setString("WS-IN-MERCH", value);
    }

    public String getWsMenuPgm() {
        return getString("WS-MENU-PGM");
    }

    public void setWsMenuPgm(String value) {
        setString("WS-MENU-PGM", value);
    }

    public String getWsMqreqPgm() {
        return getString("WS-MQREQ-PGM");
    }

    public void setWsMqreqPgm(String value) {
        setString("WS-MQREQ-PGM", value);
    }

    public String getWsMsgInvalidKey() {
        return getString("WS-MSG-INVALID-KEY");
    }

    public void setWsMsgInvalidKey(String value) {
        setString("WS-MSG-INVALID-KEY", value);
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
}

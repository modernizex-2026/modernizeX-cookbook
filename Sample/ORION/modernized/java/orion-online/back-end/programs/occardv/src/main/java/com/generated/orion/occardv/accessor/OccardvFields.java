package com.generated.orion.occardv.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.occardv.model.WorkingStorage;

/**
 * Field accessor for OCCARDV. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OccardvFields extends DynamicFieldAccessor {

    public OccardvFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public String getCaCardNum() {
        return getString("CA-CARD-NUM");
    }

    public void setCaCardNum(String value) {
        setString("CA-CARD-NUM", value);
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

    public long getCdAcctId() {
        return getLong("CD-ACCT-ID");
    }

    public void setCdAcctId(long value) {
        setLong("CD-ACCT-ID", value);
    }

    public String getCdActiveStatus() {
        return getString("CD-ACTIVE-STATUS");
    }

    public void setCdActiveStatus(String value) {
        setString("CD-ACTIVE-STATUS", value);
    }

    public String getCdEmbossedName() {
        return getString("CD-EMBOSSED-NAME");
    }

    public void setCdEmbossedName(String value) {
        setString("CD-EMBOSSED-NAME", value);
    }

    public String getCdExpiryDate() {
        return getString("CD-EXPIRY-DATE");
    }

    public void setCdExpiryDate(String value) {
        setString("CD-EXPIRY-DATE", value);
    }

    public String getCdNum() {
        return getString("CD-NUM");
    }

    public void setCdNum(String value) {
        setString("CD-NUM", value);
    }

    public String getCdaccto() {
        return getString("CDACCTO");
    }

    public void setCdaccto(String value) {
        setString("CDACCTO", value);
    }

    public String getCdexpo() {
        return getString("CDEXPO");
    }

    public void setCdexpo(String value) {
        setString("CDEXPO", value);
    }

    public String getCdnameo() {
        return getString("CDNAMEO");
    }

    public void setCdnameo(String value) {
        setString("CDNAMEO", value);
    }

    public String getCdstato() {
        return getString("CDSTATO");
    }

    public void setCdstato(String value) {
        setString("CDSTATO", value);
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

    public String getWsCardfile() {
        return getString("WS-CARDFILE");
    }

    public void setWsCardfile(String value) {
        setString("WS-CARDFILE", value);
    }

    public String getWsCnChar() {
        return getString("WS-CN-CHAR");
    }

    public void setWsCnChar(String value) {
        setString("WS-CN-CHAR", value);
    }

    public int getWsCnDigits() {
        return getInt("WS-CN-DIGITS");
    }

    public void setWsCnDigits(int value) {
        setInt("WS-CN-DIGITS", value);
    }

    public String getWsCnIn() {
        return getString("WS-CN-IN");
    }

    public void setWsCnIn(String value) {
        setString("WS-CN-IN", value);
    }

    public int getWsCnPos() {
        return getInt("WS-CN-POS");
    }

    public void setWsCnPos(int value) {
        setInt("WS-CN-POS", value);
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

    public String getWsValidSw() {
        return getString("WS-VALID-SW");
    }

    public void setWsValidSw(String value) {
        setString("WS-VALID-SW", value);
    }
}

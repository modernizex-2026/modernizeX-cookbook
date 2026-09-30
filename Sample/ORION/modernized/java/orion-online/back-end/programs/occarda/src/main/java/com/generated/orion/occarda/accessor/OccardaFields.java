package com.generated.orion.occarda.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.occarda.model.WorkingStorage;

/**
 * Field accessor for OCCARDA. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OccardaFields extends DynamicFieldAccessor {

    public OccardaFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public long getAcId() {
        return getLong("AC-ID");
    }

    public void setAcId(long value) {
        setLong("AC-ID", value);
    }

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

    public String getCardRec() {
        return groupToString("CARD-REC");
    }

    public void setCardRec(String value) {
        setGroup("CARD-REC", value);
    }

    public String getCardnumi() {
        return getString("CARDNUMI");
    }

    public void setCardnumi(String value) {
        setString("CARDNUMI", value);
    }

    public int getCardnuml() {
        return getInt("CARDNUML");
    }

    public void setCardnuml(int value) {
        setInt("CARDNUML", value);
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

    public String getCdCvv() {
        return getString("CD-CVV");
    }

    public void setCdCvv(String value) {
        setString("CD-CVV", value);
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

    public String getCdaccti() {
        return getString("CDACCTI");
    }

    public void setCdaccti(String value) {
        setString("CDACCTI", value);
    }

    public String getCdcvvi() {
        return getString("CDCVVI");
    }

    public void setCdcvvi(String value) {
        setString("CDCVVI", value);
    }

    public String getCdexpi() {
        return getString("CDEXPI");
    }

    public void setCdexpi(String value) {
        setString("CDEXPI", value);
    }

    public String getCdnamei() {
        return getString("CDNAMEI");
    }

    public void setCdnamei(String value) {
        setString("CDNAMEI", value);
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

    public String getWsCardfile() {
        return getString("WS-CARDFILE");
    }

    public void setWsCardfile(String value) {
        setString("WS-CARDFILE", value);
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

    public String getWsMAcctNf() {
        return getString("WS-M-ACCT-NF");
    }

    public void setWsMAcctNf(String value) {
        setString("WS-M-ACCT-NF", value);
    }

    public String getWsMAcctNum() {
        return getString("WS-M-ACCT-NUM");
    }

    public void setWsMAcctNum(String value) {
        setString("WS-M-ACCT-NUM", value);
    }

    public String getWsMCardDup() {
        return getString("WS-M-CARD-DUP");
    }

    public void setWsMCardDup(String value) {
        setString("WS-M-CARD-DUP", value);
    }

    public String getWsMCardReq() {
        return getString("WS-M-CARD-REQ");
    }

    public void setWsMCardReq(String value) {
        setString("WS-M-CARD-REQ", value);
    }

    public String getWsMCvvNum() {
        return getString("WS-M-CVV-NUM");
    }

    public void setWsMCvvNum(String value) {
        setString("WS-M-CVV-NUM", value);
    }

    public String getWsMExpBad() {
        return getString("WS-M-EXP-BAD");
    }

    public void setWsMExpBad(String value) {
        setString("WS-M-EXP-BAD", value);
    }

    public String getWsMNameReq() {
        return getString("WS-M-NAME-REQ");
    }

    public void setWsMNameReq(String value) {
        setString("WS-M-NAME-REQ", value);
    }

    public String getWsMOk() {
        return getString("WS-M-OK");
    }

    public void setWsMOk(String value) {
        setString("WS-M-OK", value);
    }

    public String getWsMPrompt() {
        return getString("WS-M-PROMPT");
    }

    public void setWsMPrompt(String value) {
        setString("WS-M-PROMPT", value);
    }

    public String getWsMWriteErr() {
        return getString("WS-M-WRITE-ERR");
    }

    public void setWsMWriteErr(String value) {
        setString("WS-M-WRITE-ERR", value);
    }

    public String getWsMXrefErr() {
        return getString("WS-M-XREF-ERR");
    }

    public void setWsMXrefErr(String value) {
        setString("WS-M-XREF-ERR", value);
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

    public long getWsNewAcct() {
        return getLong("WS-NEW-ACCT");
    }

    public void setWsNewAcct(long value) {
        setLong("WS-NEW-ACCT", value);
    }

    public String getWsNewCard() {
        return getString("WS-NEW-CARD");
    }

    public void setWsNewCard(String value) {
        setString("WS-NEW-CARD", value);
    }

    public int getWsNewCust() {
        return getInt("WS-NEW-CUST");
    }

    public void setWsNewCust(int value) {
        setInt("WS-NEW-CUST", value);
    }

    public String getWsNewCvv() {
        return getString("WS-NEW-CVV");
    }

    public void setWsNewCvv(String value) {
        setString("WS-NEW-CVV", value);
    }

    public String getWsNewExpiry() {
        return getString("WS-NEW-EXPIRY");
    }

    public void setWsNewExpiry(String value) {
        setString("WS-NEW-EXPIRY", value);
    }

    public String getWsNewName() {
        return getString("WS-NEW-NAME");
    }

    public void setWsNewName(String value) {
        setString("WS-NEW-NAME", value);
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

    public String getWsWriteSw() {
        return getString("WS-WRITE-SW");
    }

    public void setWsWriteSw(String value) {
        setString("WS-WRITE-SW", value);
    }

    public long getWsXrefKey() {
        return getLong("WS-XREF-KEY");
    }

    public void setWsXrefKey(long value) {
        setLong("WS-XREF-KEY", value);
    }

    public String getWsXreffile() {
        return getString("WS-XREFFILE");
    }

    public void setWsXreffile(String value) {
        setString("WS-XREFFILE", value);
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

    public int getXrCustId() {
        return getInt("XR-CUST-ID");
    }

    public void setXrCustId(int value) {
        setInt("XR-CUST-ID", value);
    }

    public String getXrefRec() {
        return groupToString("XREF-REC");
    }

    public void setXrefRec(String value) {
        setGroup("XREF-REC", value);
    }
}

package com.generated.orion.octsrch.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.octsrch.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OCTSRCH. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OctsrchFields extends DynamicFieldAccessor {

    public OctsrchFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
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

    public int getCardnuml() {
        return getInt("CARDNUML");
    }

    public void setCardnuml(int value) {
        setInt("CARDNUML", value);
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

    public String getFramti() {
        return getString("FRAMTI");
    }

    public void setFramti(String value) {
        setString("FRAMTI", value);
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

    public String getSr1o() {
        return getString("SR1O");
    }

    public void setSr1o(String value) {
        setString("SR1O", value);
    }

    public String getSr2o() {
        return getString("SR2O");
    }

    public void setSr2o(String value) {
        setString("SR2O", value);
    }

    public String getSr3o() {
        return getString("SR3O");
    }

    public void setSr3o(String value) {
        setString("SR3O", value);
    }

    public String getSr4o() {
        return getString("SR4O");
    }

    public void setSr4o(String value) {
        setString("SR4O", value);
    }

    public String getSr5o() {
        return getString("SR5O");
    }

    public void setSr5o(String value) {
        setString("SR5O", value);
    }

    public String getTitleo() {
        return getString("TITLEO");
    }

    public void setTitleo(String value) {
        setString("TITLEO", value);
    }

    public String getToamti() {
        return getString("TOAMTI");
    }

    public void setToamti(String value) {
        setString("TOAMTI", value);
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

    public String getTrId() {
        return getString("TR-ID");
    }

    public void setTrId(String value) {
        setString("TR-ID", value);
    }

    public String getTrMerchantName() {
        return getString("TR-MERCHANT-NAME");
    }

    public void setTrMerchantName(String value) {
        setString("TR-MERCHANT-NAME", value);
    }

    public String getTrTypeCd() {
        return getString("TR-TYPE-CD");
    }

    public void setTrTypeCd(String value) {
        setString("TR-TYPE-CD", value);
    }

    public String getTrnnameo() {
        return getString("TRNNAMEO");
    }

    public void setTrnnameo(String value) {
        setString("TRNNAMEO", value);
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

    public String getWsAmtDisp() {
        return getString("WS-AMT-DISP");
    }

    public void setWsAmtDisp(String value) {
        setString("WS-AMT-DISP", value);
    }

    public String getWsBrowseStarted() {
        return getString("WS-BROWSE-STARTED");
    }

    public void setWsBrowseStarted(String value) {
        setString("WS-BROWSE-STARTED", value);
    }

    public String getWsCardKey() {
        return getString("WS-CARD-KEY");
    }

    public void setWsCardKey(String value) {
        setString("WS-CARD-KEY", value);
    }

    public int getWsCntEd() {
        return getInt("WS-CNT-ED");
    }

    public void setWsCntEd(int value) {
        setInt("WS-CNT-ED", value);
    }

    public BigDecimal getWsEdAmt() {
        return getDecimal("WS-ED-AMT");
    }

    public void setWsEdAmt(BigDecimal value) {
        setDecimal("WS-ED-AMT", value);
    }

    public String getWsEndFlg() {
        return getString("WS-END-FLG");
    }

    public void setWsEndFlg(String value) {
        setString("WS-END-FLG", value);
    }

    public BigDecimal getWsFromAmt() {
        return getDecimal("WS-FROM-AMT");
    }

    public void setWsFromAmt(BigDecimal value) {
        setDecimal("WS-FROM-AMT", value);
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

    public String getWsMBrowseErr() {
        return getString("WS-M-BROWSE-ERR");
    }

    public void setWsMBrowseErr(String value) {
        setString("WS-M-BROWSE-ERR", value);
    }

    public String getWsMCardReq() {
        return getString("WS-M-CARD-REQ");
    }

    public void setWsMCardReq(String value) {
        setString("WS-M-CARD-REQ", value);
    }

    public String getWsMFromBad() {
        return getString("WS-M-FROM-BAD");
    }

    public void setWsMFromBad(String value) {
        setString("WS-M-FROM-BAD", value);
    }

    public String getWsMNoneFound() {
        return getString("WS-M-NONE-FOUND");
    }

    public void setWsMNoneFound(String value) {
        setString("WS-M-NONE-FOUND", value);
    }

    public String getWsMPrompt() {
        return getString("WS-M-PROMPT");
    }

    public void setWsMPrompt(String value) {
        setString("WS-M-PROMPT", value);
    }

    public String getWsMRange() {
        return getString("WS-M-RANGE");
    }

    public void setWsMRange(String value) {
        setString("WS-M-RANGE", value);
    }

    public String getWsMSuffix() {
        return getString("WS-M-SUFFIX");
    }

    public void setWsMSuffix(String value) {
        setString("WS-M-SUFFIX", value);
    }

    public String getWsMToBad() {
        return getString("WS-M-TO-BAD");
    }

    public void setWsMToBad(String value) {
        setString("WS-M-TO-BAD", value);
    }

    public int getWsMaxRows() {
        return getInt("WS-MAX-ROWS");
    }

    public void setWsMaxRows(int value) {
        setInt("WS-MAX-ROWS", value);
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

    public int getWsRowCnt() {
        return getInt("WS-ROW-CNT");
    }

    public void setWsRowCnt(int value) {
        setInt("WS-ROW-CNT", value);
    }

    public String getWsSrLine() {
        return getString("WS-SR-LINE");
    }

    public void setWsSrLine(String value) {
        setString("WS-SR-LINE", value);
    }

    public BigDecimal getWsToAmt() {
        return getDecimal("WS-TO-AMT");
    }

    public void setWsToAmt(BigDecimal value) {
        setDecimal("WS-TO-AMT", value);
    }

    public String getWsTranfile() {
        return getString("WS-TRANFILE");
    }

    public void setWsTranfile(String value) {
        setString("WS-TRANFILE", value);
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

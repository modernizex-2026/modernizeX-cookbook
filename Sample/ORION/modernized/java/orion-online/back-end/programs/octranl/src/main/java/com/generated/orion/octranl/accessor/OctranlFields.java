package com.generated.orion.octranl.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.octranl.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OCTRANL. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OctranlFields extends DynamicFieldAccessor {

    public OctranlFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public String getAmt1o() {
        return getString("AMT1O");
    }

    public void setAmt1o(String value) {
        setString("AMT1O", value);
    }

    public String getAmt2o() {
        return getString("AMT2O");
    }

    public void setAmt2o(String value) {
        setString("AMT2O", value);
    }

    public String getAmt3o() {
        return getString("AMT3O");
    }

    public void setAmt3o(String value) {
        setString("AMT3O", value);
    }

    public String getAmt4o() {
        return getString("AMT4O");
    }

    public void setAmt4o(String value) {
        setString("AMT4O", value);
    }

    public String getAmt5o() {
        return getString("AMT5O");
    }

    public void setAmt5o(String value) {
        setString("AMT5O", value);
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

    public String getTrTypeCd() {
        return getString("TR-TYPE-CD");
    }

    public void setTrTypeCd(String value) {
        setString("TR-TYPE-CD", value);
    }

    public String getTrn1o() {
        return getString("TRN1O");
    }

    public void setTrn1o(String value) {
        setString("TRN1O", value);
    }

    public String getTrn2o() {
        return getString("TRN2O");
    }

    public void setTrn2o(String value) {
        setString("TRN2O", value);
    }

    public String getTrn3o() {
        return getString("TRN3O");
    }

    public void setTrn3o(String value) {
        setString("TRN3O", value);
    }

    public String getTrn4o() {
        return getString("TRN4O");
    }

    public void setTrn4o(String value) {
        setString("TRN4O", value);
    }

    public String getTrn5o() {
        return getString("TRN5O");
    }

    public void setTrn5o(String value) {
        setString("TRN5O", value);
    }

    public String getTrnnameo() {
        return getString("TRNNAMEO");
    }

    public void setTrnnameo(String value) {
        setString("TRNNAMEO", value);
    }

    public String getTyp1o() {
        return getString("TYP1O");
    }

    public void setTyp1o(String value) {
        setString("TYP1O", value);
    }

    public String getTyp2o() {
        return getString("TYP2O");
    }

    public void setTyp2o(String value) {
        setString("TYP2O", value);
    }

    public String getTyp3o() {
        return getString("TYP3O");
    }

    public void setTyp3o(String value) {
        setString("TYP3O", value);
    }

    public String getTyp4o() {
        return getString("TYP4O");
    }

    public void setTyp4o(String value) {
        setString("TYP4O", value);
    }

    public String getTyp5o() {
        return getString("TYP5O");
    }

    public void setTyp5o(String value) {
        setString("TYP5O", value);
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

    public String getWsMSuffix() {
        return getString("WS-M-SUFFIX");
    }

    public void setWsMSuffix(String value) {
        setString("WS-M-SUFFIX", value);
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
}

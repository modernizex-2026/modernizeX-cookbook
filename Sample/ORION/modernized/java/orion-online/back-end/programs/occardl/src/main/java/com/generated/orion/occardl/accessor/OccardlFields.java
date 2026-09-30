package com.generated.orion.occardl.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.occardl.model.WorkingStorage;

/**
 * Field accessor for OCCARDL. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OccardlFields extends DynamicFieldAccessor {

    public OccardlFields(WorkingStorage ws) {
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

    public String getAcctido() {
        return getString("ACCTIDO");
    }

    public void setAcctido(String value) {
        setString("ACCTIDO", value);
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

    public String getCard1o() {
        return getString("CARD1O");
    }

    public void setCard1o(String value) {
        setString("CARD1O", value);
    }

    public String getCard2o() {
        return getString("CARD2O");
    }

    public void setCard2o(String value) {
        setString("CARD2O", value);
    }

    public String getCard3o() {
        return getString("CARD3O");
    }

    public void setCard3o(String value) {
        setString("CARD3O", value);
    }

    public String getCard4o() {
        return getString("CARD4O");
    }

    public void setCard4o(String value) {
        setString("CARD4O", value);
    }

    public String getCard5o() {
        return getString("CARD5O");
    }

    public void setCard5o(String value) {
        setString("CARD5O", value);
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

    public String getCdNum() {
        return getString("CD-NUM");
    }

    public void setCdNum(String value) {
        setString("CD-NUM", value);
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

    public String getStat1o() {
        return getString("STAT1O");
    }

    public void setStat1o(String value) {
        setString("STAT1O", value);
    }

    public String getStat2o() {
        return getString("STAT2O");
    }

    public void setStat2o(String value) {
        setString("STAT2O", value);
    }

    public String getStat3o() {
        return getString("STAT3O");
    }

    public void setStat3o(String value) {
        setString("STAT3O", value);
    }

    public String getStat4o() {
        return getString("STAT4O");
    }

    public void setStat4o(String value) {
        setString("STAT4O", value);
    }

    public String getStat5o() {
        return getString("STAT5O");
    }

    public void setStat5o(String value) {
        setString("STAT5O", value);
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

    public String getWsBrEndSw() {
        return getString("WS-BR-END-SW");
    }

    public void setWsBrEndSw(String value) {
        setString("WS-BR-END-SW", value);
    }

    public String getWsBrStartedSw() {
        return getString("WS-BR-STARTED-SW");
    }

    public void setWsBrStartedSw(String value) {
        setString("WS-BR-STARTED-SW", value);
    }

    public String getWsBrowseKey() {
        return getString("WS-BROWSE-KEY");
    }

    public void setWsBrowseKey(String value) {
        setString("WS-BROWSE-KEY", value);
    }

    public String getWsCardTable() {
        return groupToString("WS-CARD-TABLE");
    }

    public void setWsCardTable(String value) {
        setGroup("WS-CARD-TABLE", value);
    }

    public String getWsCardfile() {
        return getString("WS-CARDFILE");
    }

    public void setWsCardfile(String value) {
        setString("WS-CARDFILE", value);
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

    public String getWsMoreSw() {
        return getString("WS-MORE-SW");
    }

    public void setWsMoreSw(String value) {
        setString("WS-MORE-SW", value);
    }

    public String getWsMsgInvalidKey() {
        return getString("WS-MSG-INVALID-KEY");
    }

    public void setWsMsgInvalidKey(String value) {
        setString("WS-MSG-INVALID-KEY", value);
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

    public String getWsResumeCard() {
        return getString("WS-RESUME-CARD");
    }

    public void setWsResumeCard(String value) {
        setString("WS-RESUME-CARD", value);
    }

    public String getWsRowCard(int index) {
        return getString("WS-ROW-CARD", index);
    }

    public void setWsRowCard(int index, String value) {
        setString("WS-ROW-CARD", value, index);
    }

    public int getWsRowCnt() {
        return getInt("WS-ROW-CNT");
    }

    public void setWsRowCnt(int value) {
        setInt("WS-ROW-CNT", value);
    }

    public String getWsRowStat(int index) {
        return getString("WS-ROW-STAT", index);
    }

    public void setWsRowStat(int index, String value) {
        setString("WS-ROW-STAT", value, index);
    }

    public String getWsStKey() {
        return getString("WS-ST-KEY");
    }

    public void setWsStKey(String value) {
        setString("WS-ST-KEY", value);
    }

    public String getWsStList() {
        return getString("WS-ST-LIST");
    }

    public void setWsStList(String value) {
        setString("WS-ST-LIST", value);
    }

    public String getWsStartCard() {
        return getString("WS-START-CARD");
    }

    public void setWsStartCard(String value) {
        setString("WS-START-CARD", value);
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

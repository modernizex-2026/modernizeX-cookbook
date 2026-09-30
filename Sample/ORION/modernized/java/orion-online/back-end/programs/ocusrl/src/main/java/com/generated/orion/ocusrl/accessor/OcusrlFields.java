package com.generated.orion.ocusrl.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ocusrl.model.WorkingStorage;

/**
 * Field accessor for OCUSRL. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OcusrlFields extends DynamicFieldAccessor {

    public OcusrlFields(WorkingStorage ws) {
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

    public String getUnm1o() {
        return getString("UNM1O");
    }

    public void setUnm1o(String value) {
        setString("UNM1O", value);
    }

    public String getUnm2o() {
        return getString("UNM2O");
    }

    public void setUnm2o(String value) {
        setString("UNM2O", value);
    }

    public String getUnm3o() {
        return getString("UNM3O");
    }

    public void setUnm3o(String value) {
        setString("UNM3O", value);
    }

    public String getUnm4o() {
        return getString("UNM4O");
    }

    public void setUnm4o(String value) {
        setString("UNM4O", value);
    }

    public String getUsFirstName() {
        return getString("US-FIRST-NAME");
    }

    public void setUsFirstName(String value) {
        setString("US-FIRST-NAME", value);
    }

    public String getUsId() {
        return getString("US-ID");
    }

    public void setUsId(String value) {
        setString("US-ID", value);
    }

    public String getUsLastName() {
        return getString("US-LAST-NAME");
    }

    public void setUsLastName(String value) {
        setString("US-LAST-NAME", value);
    }

    public String getUsType() {
        return getString("US-TYPE");
    }

    public void setUsType(String value) {
        setString("US-TYPE", value);
    }

    public String getUsr1o() {
        return getString("USR1O");
    }

    public void setUsr1o(String value) {
        setString("USR1O", value);
    }

    public String getUsr2o() {
        return getString("USR2O");
    }

    public void setUsr2o(String value) {
        setString("USR2O", value);
    }

    public String getUsr3o() {
        return getString("USR3O");
    }

    public void setUsr3o(String value) {
        setString("USR3O", value);
    }

    public String getUsr4o() {
        return getString("USR4O");
    }

    public void setUsr4o(String value) {
        setString("USR4O", value);
    }

    public String getUty1o() {
        return getString("UTY1O");
    }

    public void setUty1o(String value) {
        setString("UTY1O", value);
    }

    public String getUty2o() {
        return getString("UTY2O");
    }

    public void setUty2o(String value) {
        setString("UTY2O", value);
    }

    public String getUty3o() {
        return getString("UTY3O");
    }

    public void setUty3o(String value) {
        setString("UTY3O", value);
    }

    public String getUty4o() {
        return getString("UTY4O");
    }

    public void setUty4o(String value) {
        setString("UTY4O", value);
    }

    public String getWsAdmenPgm() {
        return getString("WS-ADMEN-PGM");
    }

    public void setWsAdmenPgm(String value) {
        setString("WS-ADMEN-PGM", value);
    }

    public String getWsEndFlg() {
        return getString("WS-END-FLG");
    }

    public void setWsEndFlg(String value) {
        setString("WS-END-FLG", value);
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

    public String getWsFullname() {
        return getString("WS-FULLNAME");
    }

    public void setWsFullname(String value) {
        setString("WS-FULLNAME", value);
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

    public String getWsLastKey() {
        return getString("WS-LAST-KEY");
    }

    public void setWsLastKey(String value) {
        setString("WS-LAST-KEY", value);
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

    public int getWsRowCount() {
        return getInt("WS-ROW-COUNT");
    }

    public void setWsRowCount(int value) {
        setInt("WS-ROW-COUNT", value);
    }

    public int getWsRowIdx() {
        return getInt("WS-ROW-IDX");
    }

    public void setWsRowIdx(int value) {
        setInt("WS-ROW-IDX", value);
    }

    public String getWsSkipFirst() {
        return getString("WS-SKIP-FIRST");
    }

    public void setWsSkipFirst(String value) {
        setString("WS-SKIP-FIRST", value);
    }

    public String getWsStartKey() {
        return getString("WS-START-KEY");
    }

    public void setWsStartKey(String value) {
        setString("WS-START-KEY", value);
    }

    public String getWsTranid() {
        return getString("WS-TRANID");
    }

    public void setWsTranid(String value) {
        setString("WS-TRANID", value);
    }

    public String getWsUsrsec() {
        return getString("WS-USRSEC");
    }

    public void setWsUsrsec(String value) {
        setString("WS-USRSEC", value);
    }
}

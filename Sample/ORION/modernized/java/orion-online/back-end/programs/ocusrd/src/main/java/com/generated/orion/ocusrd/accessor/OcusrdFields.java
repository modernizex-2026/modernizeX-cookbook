package com.generated.orion.ocusrd.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ocusrd.model.WorkingStorage;

/**
 * Field accessor for OCUSRD. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OcusrdFields extends DynamicFieldAccessor {

    public OcusrdFields(WorkingStorage ws) {
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

    public String getUsconfi() {
        return getString("USCONFI");
    }

    public void setUsconfi(String value) {
        setString("USCONFI", value);
    }

    public String getUsconfo() {
        return getString("USCONFO");
    }

    public void setUsconfo(String value) {
        setString("USCONFO", value);
    }

    public String getUseridi() {
        return getString("USERIDI");
    }

    public void setUseridi(String value) {
        setString("USERIDI", value);
    }

    public String getUserido() {
        return getString("USERIDO");
    }

    public void setUserido(String value) {
        setString("USERIDO", value);
    }

    public String getUsnameo() {
        return getString("USNAMEO");
    }

    public void setUsnameo(String value) {
        setString("USNAMEO", value);
    }

    public String getWsAdmenPgm() {
        return getString("WS-ADMEN-PGM");
    }

    public void setWsAdmenPgm(String value) {
        setString("WS-ADMEN-PGM", value);
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

    public String getWsSvUserid() {
        return getString("WS-SV-USERID");
    }

    public void setWsSvUserid(String value) {
        setString("WS-SV-USERID", value);
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

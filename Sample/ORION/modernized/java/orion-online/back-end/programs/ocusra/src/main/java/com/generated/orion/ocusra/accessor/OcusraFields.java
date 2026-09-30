package com.generated.orion.ocusra.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ocusra.model.WorkingStorage;

/**
 * Field accessor for OCUSRA. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OcusraFields extends DynamicFieldAccessor {

    public OcusraFields(WorkingStorage ws) {
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

    public String getUsPassword() {
        return getString("US-PASSWORD");
    }

    public void setUsPassword(String value) {
        setString("US-PASSWORD", value);
    }

    public String getUsType() {
        return getString("US-TYPE");
    }

    public void setUsType(String value) {
        setString("US-TYPE", value);
    }

    public String getUserRec() {
        return groupToString("USER-REC");
    }

    public void setUserRec(String value) {
        setGroup("USER-REC", value);
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

    public String getUsfnami() {
        return getString("USFNAMI");
    }

    public void setUsfnami(String value) {
        setString("USFNAMI", value);
    }

    public String getUsfnamo() {
        return getString("USFNAMO");
    }

    public void setUsfnamo(String value) {
        setString("USFNAMO", value);
    }

    public String getUslnami() {
        return getString("USLNAMI");
    }

    public void setUslnami(String value) {
        setString("USLNAMI", value);
    }

    public String getUslnamo() {
        return getString("USLNAMO");
    }

    public void setUslnamo(String value) {
        setString("USLNAMO", value);
    }

    public String getUspwdi() {
        return getString("USPWDI");
    }

    public void setUspwdi(String value) {
        setString("USPWDI", value);
    }

    public String getUspwdo() {
        return getString("USPWDO");
    }

    public void setUspwdo(String value) {
        setString("USPWDO", value);
    }

    public String getUstypei() {
        return getString("USTYPEI");
    }

    public void setUstypei(String value) {
        setString("USTYPEI", value);
    }

    public String getUstypeo() {
        return getString("USTYPEO");
    }

    public void setUstypeo(String value) {
        setString("USTYPEO", value);
    }

    public String getWsAdmenPgm() {
        return getString("WS-ADMEN-PGM");
    }

    public void setWsAdmenPgm(String value) {
        setString("WS-ADMEN-PGM", value);
    }

    public String getWsEditFlg() {
        return getString("WS-EDIT-FLG");
    }

    public void setWsEditFlg(String value) {
        setString("WS-EDIT-FLG", value);
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

    public String getWsTypeIn() {
        return getString("WS-TYPE-IN");
    }

    public void setWsTypeIn(String value) {
        setString("WS-TYPE-IN", value);
    }

    public String getWsUsrsec() {
        return getString("WS-USRSEC");
    }

    public void setWsUsrsec(String value) {
        setString("WS-USRSEC", value);
    }
}

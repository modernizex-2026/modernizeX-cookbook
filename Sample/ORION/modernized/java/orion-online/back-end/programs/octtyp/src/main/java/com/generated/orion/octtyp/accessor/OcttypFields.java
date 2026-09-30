package com.generated.orion.octtyp.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.octtyp.model.WorkingStorage;

/**
 * Field accessor for OCTTYP. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OcttypFields extends DynamicFieldAccessor {

    public OcttypFields(WorkingStorage ws) {
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

    public String getCaWorkArea() {
        return getString("CA-WORK-AREA");
    }

    public void setCaWorkArea(String value) {
        setString("CA-WORK-AREA", value);
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

    public String getTtCd() {
        return getString("TT-CD");
    }

    public void setTtCd(String value) {
        setString("TT-CD", value);
    }

    public String getTtDesc() {
        return getString("TT-DESC");
    }

    public void setTtDesc(String value) {
        setString("TT-DESC", value);
    }

    public String getTtcdi() {
        return getString("TTCDI");
    }

    public void setTtcdi(String value) {
        setString("TTCDI", value);
    }

    public int getTtcdl() {
        return getInt("TTCDL");
    }

    public void setTtcdl(int value) {
        setInt("TTCDL", value);
    }

    public String getTtcdo() {
        return getString("TTCDO");
    }

    public void setTtcdo(String value) {
        setString("TTCDO", value);
    }

    public String getTtdesci() {
        return getString("TTDESCI");
    }

    public void setTtdesci(String value) {
        setString("TTDESCI", value);
    }

    public int getTtdescl() {
        return getInt("TTDESCL");
    }

    public void setTtdescl(int value) {
        setInt("TTDESCL", value);
    }

    public String getTtdesco() {
        return getString("TTDESCO");
    }

    public void setTtdesco(String value) {
        setString("TTDESCO", value);
    }

    public String getTtypRec() {
        return groupToString("TTYP-REC");
    }

    public void setTtypRec(String value) {
        setGroup("TTYP-REC", value);
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

    public String getWsMAdded() {
        return getString("WS-M-ADDED");
    }

    public void setWsMAdded(String value) {
        setString("WS-M-ADDED", value);
    }

    public String getWsMCdReq() {
        return getString("WS-M-CD-REQ");
    }

    public void setWsMCdReq(String value) {
        setString("WS-M-CD-REQ", value);
    }

    public String getWsMDescReq() {
        return getString("WS-M-DESC-REQ");
    }

    public void setWsMDescReq(String value) {
        setString("WS-M-DESC-REQ", value);
    }

    public String getWsMFound() {
        return getString("WS-M-FOUND");
    }

    public void setWsMFound(String value) {
        setString("WS-M-FOUND", value);
    }

    public String getWsMKeyFirst() {
        return getString("WS-M-KEY-FIRST");
    }

    public void setWsMKeyFirst(String value) {
        setString("WS-M-KEY-FIRST", value);
    }

    public String getWsMNew() {
        return getString("WS-M-NEW");
    }

    public void setWsMNew(String value) {
        setString("WS-M-NEW", value);
    }

    public String getWsMPrompt() {
        return getString("WS-M-PROMPT");
    }

    public void setWsMPrompt(String value) {
        setString("WS-M-PROMPT", value);
    }

    public String getWsMSaveErr() {
        return getString("WS-M-SAVE-ERR");
    }

    public void setWsMSaveErr(String value) {
        setString("WS-M-SAVE-ERR", value);
    }

    public String getWsMUpdated() {
        return getString("WS-M-UPDATED");
    }

    public void setWsMUpdated(String value) {
        setString("WS-M-UPDATED", value);
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

    public String getWsStExist() {
        return getString("WS-ST-EXIST");
    }

    public void setWsStExist(String value) {
        setString("WS-ST-EXIST", value);
    }

    public String getWsStKey() {
        return getString("WS-ST-KEY");
    }

    public void setWsStKey(String value) {
        setString("WS-ST-KEY", value);
    }

    public String getWsStNew() {
        return getString("WS-ST-NEW");
    }

    public void setWsStNew(String value) {
        setString("WS-ST-NEW", value);
    }

    public String getWsTranid() {
        return getString("WS-TRANID");
    }

    public void setWsTranid(String value) {
        setString("WS-TRANID", value);
    }

    public String getWsTtCd() {
        return getString("WS-TT-CD");
    }

    public void setWsTtCd(String value) {
        setString("WS-TT-CD", value);
    }

    public String getWsTtypfile() {
        return getString("WS-TTYPFILE");
    }

    public void setWsTtypfile(String value) {
        setString("WS-TTYPFILE", value);
    }
}

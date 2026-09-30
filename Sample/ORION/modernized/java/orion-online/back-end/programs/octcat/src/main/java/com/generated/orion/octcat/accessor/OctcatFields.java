package com.generated.orion.octcat.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.octcat.model.WorkingStorage;

/**
 * Field accessor for OCTCAT. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OctcatFields extends DynamicFieldAccessor {

    public OctcatFields(WorkingStorage ws) {
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

    public int getTcCd() {
        return getInt("TC-CD");
    }

    public void setTcCd(int value) {
        setInt("TC-CD", value);
    }

    public String getTcDesc() {
        return getString("TC-DESC");
    }

    public void setTcDesc(String value) {
        setString("TC-DESC", value);
    }

    public String getTcKey() {
        return groupToString("TC-KEY");
    }

    public void setTcKey(String value) {
        setGroup("TC-KEY", value);
    }

    public String getTcTypeCd() {
        return getString("TC-TYPE-CD");
    }

    public void setTcTypeCd(String value) {
        setString("TC-TYPE-CD", value);
    }

    public String getTcatRec() {
        return groupToString("TCAT-REC");
    }

    public void setTcatRec(String value) {
        setGroup("TCAT-REC", value);
    }

    public String getTccdi() {
        return getString("TCCDI");
    }

    public void setTccdi(String value) {
        setString("TCCDI", value);
    }

    public String getTccdo() {
        return getString("TCCDO");
    }

    public void setTccdo(String value) {
        setString("TCCDO", value);
    }

    public String getTcdesci() {
        return getString("TCDESCI");
    }

    public void setTcdesci(String value) {
        setString("TCDESCI", value);
    }

    public int getTcdescl() {
        return getInt("TCDESCL");
    }

    public void setTcdescl(int value) {
        setInt("TCDESCL", value);
    }

    public String getTcdesco() {
        return getString("TCDESCO");
    }

    public void setTcdesco(String value) {
        setString("TCDESCO", value);
    }

    public String getTctypei() {
        return getString("TCTYPEI");
    }

    public void setTctypei(String value) {
        setString("TCTYPEI", value);
    }

    public int getTctypel() {
        return getInt("TCTYPEL");
    }

    public void setTctypel(int value) {
        setInt("TCTYPEL", value);
    }

    public String getTctypeo() {
        return getString("TCTYPEO");
    }

    public void setTctypeo(String value) {
        setString("TCTYPEO", value);
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

    public int getWsCdEd() {
        return getInt("WS-CD-ED");
    }

    public void setWsCdEd(int value) {
        setInt("WS-CD-ED", value);
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

    public String getWsMCdNum() {
        return getString("WS-M-CD-NUM");
    }

    public void setWsMCdNum(String value) {
        setString("WS-M-CD-NUM", value);
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

    public String getWsMTypeReq() {
        return getString("WS-M-TYPE-REQ");
    }

    public void setWsMTypeReq(String value) {
        setString("WS-M-TYPE-REQ", value);
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

    public int getWsTcCd() {
        return getInt("WS-TC-CD");
    }

    public void setWsTcCd(int value) {
        setInt("WS-TC-CD", value);
    }

    public String getWsTcType() {
        return getString("WS-TC-TYPE");
    }

    public void setWsTcType(String value) {
        setString("WS-TC-TYPE", value);
    }

    public String getWsTcatfile() {
        return getString("WS-TCATFILE");
    }

    public void setWsTcatfile(String value) {
        setString("WS-TCATFILE", value);
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

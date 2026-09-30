package com.generated.orion.occustv.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.occustv.model.WorkingStorage;

/**
 * Field accessor for OCCUSTV. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OccustvFields extends DynamicFieldAccessor {

    public OccustvFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getCaCustId() {
        return getInt("CA-CUST-ID");
    }

    public void setCaCustId(int value) {
        setInt("CA-CUST-ID", value);
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

    public String getCuAddrCity() {
        return getString("CU-ADDR-CITY");
    }

    public void setCuAddrCity(String value) {
        setString("CU-ADDR-CITY", value);
    }

    public String getCuAddrLine1() {
        return getString("CU-ADDR-LINE-1");
    }

    public void setCuAddrLine1(String value) {
        setString("CU-ADDR-LINE-1", value);
    }

    public int getCuFicoScore() {
        return getInt("CU-FICO-SCORE");
    }

    public void setCuFicoScore(int value) {
        setInt("CU-FICO-SCORE", value);
    }

    public String getCuFirstName() {
        return getString("CU-FIRST-NAME");
    }

    public void setCuFirstName(String value) {
        setString("CU-FIRST-NAME", value);
    }

    public int getCuId() {
        return getInt("CU-ID");
    }

    public void setCuId(int value) {
        setInt("CU-ID", value);
    }

    public String getCuLastName() {
        return getString("CU-LAST-NAME");
    }

    public void setCuLastName(String value) {
        setString("CU-LAST-NAME", value);
    }

    public String getCuPhone1() {
        return getString("CU-PHONE-1");
    }

    public void setCuPhone1(String value) {
        setString("CU-PHONE-1", value);
    }

    public String getCuaddro() {
        return getString("CUADDRO");
    }

    public void setCuaddro(String value) {
        setString("CUADDRO", value);
    }

    public String getCucityo() {
        return getString("CUCITYO");
    }

    public void setCucityo(String value) {
        setString("CUCITYO", value);
    }

    public String getCuficoo() {
        return getString("CUFICOO");
    }

    public void setCuficoo(String value) {
        setString("CUFICOO", value);
    }

    public String getCunameo() {
        return getString("CUNAMEO");
    }

    public void setCunameo(String value) {
        setString("CUNAMEO", value);
    }

    public String getCuphoneo() {
        return getString("CUPHONEO");
    }

    public void setCuphoneo(String value) {
        setString("CUPHONEO", value);
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

    public String getCustidi() {
        return getString("CUSTIDI");
    }

    public void setCustidi(String value) {
        setString("CUSTIDI", value);
    }

    public String getCustido() {
        return getString("CUSTIDO");
    }

    public void setCustido(String value) {
        setString("CUSTIDO", value);
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

    public String getWsCustfile() {
        return getString("WS-CUSTFILE");
    }

    public void setWsCustfile(String value) {
        setString("WS-CUSTFILE", value);
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

    public String getWsFullName() {
        return getString("WS-FULL-NAME");
    }

    public void setWsFullName(String value) {
        setString("WS-FULL-NAME", value);
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

    public int getWsInCustId() {
        return getInt("WS-IN-CUST-ID");
    }

    public void setWsInCustId(int value) {
        setInt("WS-IN-CUST-ID", value);
    }

    public String getWsMCustFound() {
        return getString("WS-M-CUST-FOUND");
    }

    public void setWsMCustFound(String value) {
        setString("WS-M-CUST-FOUND", value);
    }

    public String getWsMCustNotfnd() {
        return getString("WS-M-CUST-NOTFND");
    }

    public void setWsMCustNotfnd(String value) {
        setString("WS-M-CUST-NOTFND", value);
    }

    public String getWsMIdNotnum() {
        return getString("WS-M-ID-NOTNUM");
    }

    public void setWsMIdNotnum(String value) {
        setString("WS-M-ID-NOTNUM", value);
    }

    public String getWsMIdRequired() {
        return getString("WS-M-ID-REQUIRED");
    }

    public void setWsMIdRequired(String value) {
        setString("WS-M-ID-REQUIRED", value);
    }

    public String getWsMPrompt() {
        return getString("WS-M-PROMPT");
    }

    public void setWsMPrompt(String value) {
        setString("WS-M-PROMPT", value);
    }

    public String getWsMReadError() {
        return getString("WS-M-READ-ERROR");
    }

    public void setWsMReadError(String value) {
        setString("WS-M-READ-ERROR", value);
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

    public String getWsTranid() {
        return getString("WS-TRANID");
    }

    public void setWsTranid(String value) {
        setString("WS-TRANID", value);
    }

    public String getWsValCh() {
        return getString("WS-VAL-CH");
    }

    public void setWsValCh(String value) {
        setString("WS-VAL-CH", value);
    }

    public int getWsValDigits() {
        return getInt("WS-VAL-DIGITS");
    }

    public void setWsValDigits(int value) {
        setInt("WS-VAL-DIGITS", value);
    }

    public int getWsValI() {
        return getInt("WS-VAL-I");
    }

    public void setWsValI(int value) {
        setInt("WS-VAL-I", value);
    }

    public int getWsValLen() {
        return getInt("WS-VAL-LEN");
    }

    public void setWsValLen(int value) {
        setInt("WS-VAL-LEN", value);
    }

    public String getWsValOk() {
        return getString("WS-VAL-OK");
    }

    public void setWsValOk(String value) {
        setString("WS-VAL-OK", value);
    }

    public String getWsValStr() {
        return getString("WS-VAL-STR");
    }

    public void setWsValStr(String value) {
        setString("WS-VAL-STR", value);
    }
}

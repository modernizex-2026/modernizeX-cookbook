package com.generated.orion.odcustv.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.odcustv.model.WorkingStorage;

/**
 * Field accessor for ODCUSTV. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OdcustvFields extends DynamicFieldAccessor {

    public OdcustvFields(WorkingStorage ws) {
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

    public String getCuMiddleName() {
        return getString("CU-MIDDLE-NAME");
    }

    public void setCuMiddleName(String value) {
        setString("CU-MIDDLE-NAME", value);
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

    public int getSqlcode() {
        return getInt("SQLCODE");
    }

    public void setSqlcode(int value) {
        setInt("SQLCODE", value);
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

    public String getWsMsgNotfnd() {
        return getString("WS-MSG-NOTFND");
    }

    public void setWsMsgNotfnd(String value) {
        setString("WS-MSG-NOTFND", value);
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

    public String getWsTranid() {
        return getString("WS-TRANID");
    }

    public void setWsTranid(String value) {
        setString("WS-TRANID", value);
    }
}

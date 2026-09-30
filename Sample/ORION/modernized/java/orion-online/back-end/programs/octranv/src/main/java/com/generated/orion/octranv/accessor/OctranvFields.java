package com.generated.orion.octranv.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.octranv.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OCTRANV. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OctranvFields extends DynamicFieldAccessor {

    public OctranvFields(WorkingStorage ws) {
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

    public String getCaTranId() {
        return getString("CA-TRAN-ID");
    }

    public void setCaTranId(String value) {
        setString("CA-TRAN-ID", value);
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

    public String getTrDesc() {
        return getString("TR-DESC");
    }

    public void setTrDesc(String value) {
        setString("TR-DESC", value);
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

    public String getTramto() {
        return getString("TRAMTO");
    }

    public void setTramto(String value) {
        setString("TRAMTO", value);
    }

    public String getTranidi() {
        return getString("TRANIDI");
    }

    public void setTranidi(String value) {
        setString("TRANIDI", value);
    }

    public int getTranidl() {
        return getInt("TRANIDL");
    }

    public void setTranidl(int value) {
        setInt("TRANIDL", value);
    }

    public String getTranido() {
        return getString("TRANIDO");
    }

    public void setTranido(String value) {
        setString("TRANIDO", value);
    }

    public String getTrcardo() {
        return getString("TRCARDO");
    }

    public void setTrcardo(String value) {
        setString("TRCARDO", value);
    }

    public String getTrdesco() {
        return getString("TRDESCO");
    }

    public void setTrdesco(String value) {
        setString("TRDESCO", value);
    }

    public String getTrmercho() {
        return getString("TRMERCHO");
    }

    public void setTrmercho(String value) {
        setString("TRMERCHO", value);
    }

    public String getTrnnameo() {
        return getString("TRNNAMEO");
    }

    public void setTrnnameo(String value) {
        setString("TRNNAMEO", value);
    }

    public String getTrtypeo() {
        return getString("TRTYPEO");
    }

    public void setTrtypeo(String value) {
        setString("TRTYPEO", value);
    }

    public BigDecimal getWsEdAmt() {
        return getDecimal("WS-ED-AMT");
    }

    public void setWsEdAmt(BigDecimal value) {
        setDecimal("WS-ED-AMT", value);
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

    public String getWsMTranFound() {
        return getString("WS-M-TRAN-FOUND");
    }

    public void setWsMTranFound(String value) {
        setString("WS-M-TRAN-FOUND", value);
    }

    public String getWsMTranNotfnd() {
        return getString("WS-M-TRAN-NOTFND");
    }

    public void setWsMTranNotfnd(String value) {
        setString("WS-M-TRAN-NOTFND", value);
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

    public String getWsTranKey() {
        return getString("WS-TRAN-KEY");
    }

    public void setWsTranKey(String value) {
        setString("WS-TRAN-KEY", value);
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

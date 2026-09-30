package com.generated.orion.ocpauin.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ocpauin.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OCPAUIN. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OcpauinFields extends DynamicFieldAccessor {

    public OcpauinFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public String getAuthidi() {
        return getString("AUTHIDI");
    }

    public void setAuthidi(String value) {
        setString("AUTHIDI", value);
    }

    public String getAuthido() {
        return getString("AUTHIDO");
    }

    public void setAuthido(String value) {
        setString("AUTHIDO", value);
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

    public String getCaToProgram() {
        return getString("CA-TO-PROGRAM");
    }

    public void setCaToProgram(String value) {
        setString("CA-TO-PROGRAM", value);
    }

    public String getCaTranId() {
        return getString("CA-TRAN-ID");
    }

    public void setCaTranId(String value) {
        setString("CA-TRAN-ID", value);
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

    public long getPaAcctId() {
        return getLong("PA-ACCT-ID");
    }

    public void setPaAcctId(long value) {
        setLong("PA-ACCT-ID", value);
    }

    public BigDecimal getPaAmount() {
        return getDecimal("PA-AMOUNT");
    }

    public void setPaAmount(BigDecimal value) {
        setDecimal("PA-AMOUNT", value);
    }

    public String getPaAuthId() {
        return getString("PA-AUTH-ID");
    }

    public void setPaAuthId(String value) {
        setString("PA-AUTH-ID", value);
    }

    public String getPaCardNum() {
        return getString("PA-CARD-NUM");
    }

    public void setPaCardNum(String value) {
        setString("PA-CARD-NUM", value);
    }

    public String getPaDecReason() {
        return getString("PA-DEC-REASON");
    }

    public void setPaDecReason(String value) {
        setString("PA-DEC-REASON", value);
    }

    public String getPaMerchant() {
        return getString("PA-MERCHANT");
    }

    public void setPaMerchant(String value) {
        setString("PA-MERCHANT", value);
    }

    public String getPaRequestTs() {
        return getString("PA-REQUEST-TS");
    }

    public void setPaRequestTs(String value) {
        setString("PA-REQUEST-TS", value);
    }

    public String getPaStatus() {
        return getString("PA-STATUS");
    }

    public void setPaStatus(String value) {
        setString("PA-STATUS", value);
    }

    public String getPaaccto() {
        return getString("PAACCTO");
    }

    public void setPaaccto(String value) {
        setString("PAACCTO", value);
    }

    public String getPaamto() {
        return getString("PAAMTO");
    }

    public void setPaamto(String value) {
        setString("PAAMTO", value);
    }

    public String getPacardo() {
        return getString("PACARDO");
    }

    public void setPacardo(String value) {
        setString("PACARDO", value);
    }

    public String getPadeco() {
        return getString("PADECO");
    }

    public void setPadeco(String value) {
        setString("PADECO", value);
    }

    public String getPamercho() {
        return getString("PAMERCHO");
    }

    public void setPamercho(String value) {
        setString("PAMERCHO", value);
    }

    public String getPareqtso() {
        return getString("PAREQTSO");
    }

    public void setPareqtso(String value) {
        setString("PAREQTSO", value);
    }

    public String getPastato() {
        return getString("PASTATO");
    }

    public void setPastato(String value) {
        setString("PASTATO", value);
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

    public BigDecimal getWsEdAmt() {
        return getDecimal("WS-ED-AMT");
    }

    public void setWsEdAmt(BigDecimal value) {
        setDecimal("WS-ED-AMT", value);
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

    public String getWsMsgRequired() {
        return getString("WS-MSG-REQUIRED");
    }

    public void setWsMsgRequired(String value) {
        setString("WS-MSG-REQUIRED", value);
    }

    public String getWsPauLink() {
        return groupToString("WS-PAU-LINK");
    }

    public void setWsPauLink(String value) {
        setGroup("WS-PAU-LINK", value);
    }

    public String getWsPgmname() {
        return getString("WS-PGMNAME");
    }

    public void setWsPgmname(String value) {
        setString("WS-PGMNAME", value);
    }

    public String getWsPlKey() {
        return getString("WS-PL-KEY");
    }

    public void setWsPlKey(String value) {
        setString("WS-PL-KEY", value);
    }

    public String getWsPlMsg() {
        return getString("WS-PL-MSG");
    }

    public void setWsPlMsg(String value) {
        setString("WS-PL-MSG", value);
    }

    public String getWsPlStatus() {
        return getString("WS-PL-STATUS");
    }

    public void setWsPlStatus(String value) {
        setString("WS-PL-STATUS", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public String getWsStatusWord() {
        return getString("WS-STATUS-WORD");
    }

    public void setWsStatusWord(String value) {
        setString("WS-STATUS-WORD", value);
    }

    public String getWsSubPgm() {
        return getString("WS-SUB-PGM");
    }

    public void setWsSubPgm(String value) {
        setString("WS-SUB-PGM", value);
    }

    public String getWsTranid() {
        return getString("WS-TRANID");
    }

    public void setWsTranid(String value) {
        setString("WS-TRANID", value);
    }
}

package com.generated.orion.ocops.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ocops.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OCOPS. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OcopsFields extends DynamicFieldAccessor {

    public OcopsFields(WorkingStorage ws) {
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

    public BigDecimal getKoAmt1() {
        return getDecimal("KO-AMT-1");
    }

    public void setKoAmt1(BigDecimal value) {
        setDecimal("KO-AMT-1", value);
    }

    public BigDecimal getKoAmt2() {
        return getDecimal("KO-AMT-2");
    }

    public void setKoAmt2(BigDecimal value) {
        setDecimal("KO-AMT-2", value);
    }

    public String getKoFunction() {
        return getString("KO-FUNCTION");
    }

    public void setKoFunction(String value) {
        setString("KO-FUNCTION", value);
    }

    public long getKoParmAcct() {
        return getLong("KO-PARM-ACCT");
    }

    public void setKoParmAcct(long value) {
        setLong("KO-PARM-ACCT", value);
    }

    public BigDecimal getKoParmAmt() {
        return getDecimal("KO-PARM-AMT");
    }

    public void setKoParmAmt(BigDecimal value) {
        setDecimal("KO-PARM-AMT", value);
    }

    public String getKoParmCard() {
        return getString("KO-PARM-CARD");
    }

    public void setKoParmCard(String value) {
        setString("KO-PARM-CARD", value);
    }

    public String getKoParmDate() {
        return getString("KO-PARM-DATE");
    }

    public void setKoParmDate(String value) {
        setString("KO-PARM-DATE", value);
    }

    public int getKoPostedCnt() {
        return getInt("KO-POSTED-CNT");
    }

    public void setKoPostedCnt(int value) {
        setInt("KO-POSTED-CNT", value);
    }

    public int getKoReadCnt() {
        return getInt("KO-READ-CNT");
    }

    public void setKoReadCnt(int value) {
        setInt("KO-READ-CNT", value);
    }

    public int getKoRejectCnt() {
        return getInt("KO-REJECT-CNT");
    }

    public void setKoRejectCnt(int value) {
        setInt("KO-REJECT-CNT", value);
    }

    public int getKoSelectCnt() {
        return getInt("KO-SELECT-CNT");
    }

    public void setKoSelectCnt(int value) {
        setInt("KO-SELECT-CNT", value);
    }

    public String getKoStatus() {
        return getString("KO-STATUS");
    }

    public void setKoStatus(String value) {
        setString("KO-STATUS", value);
    }

    public String getKoStatusMsg() {
        return getString("KO-STATUS-MSG");
    }

    public void setKoStatusMsg(String value) {
        setString("KO-STATUS-MSG", value);
    }

    public int getKoTranCnt() {
        return getInt("KO-TRAN-CNT");
    }

    public void setKoTranCnt(int value) {
        setInt("KO-TRAN-CNT", value);
    }

    public int getKoUpdateCnt() {
        return getInt("KO-UPDATE-CNT");
    }

    public void setKoUpdateCnt(int value) {
        setInt("KO-UPDATE-CNT", value);
    }

    public String getKopsArea() {
        return groupToString("KOPS-AREA");
    }

    public void setKopsArea(String value) {
        setGroup("KOPS-AREA", value);
    }

    public String getOptioni() {
        return getString("OPTIONI");
    }

    public void setOptioni(String value) {
        setString("OPTIONI", value);
    }

    public String getOrionCommarea() {
        return groupToString("ORION-COMMAREA");
    }

    public void setOrionCommarea(String value) {
        setGroup("ORION-COMMAREA", value);
    }

    public String getParmi() {
        return getString("PARMI");
    }

    public void setParmi(String value) {
        setString("PARMI", value);
    }

    public String getPgmnameo() {
        return getString("PGMNAMEO");
    }

    public void setPgmnameo(String value) {
        setString("PGMNAMEO", value);
    }

    public String getRamt1o() {
        return getString("RAMT1O");
    }

    public void setRamt1o(String value) {
        setString("RAMT1O", value);
    }

    public String getRamt2o() {
        return getString("RAMT2O");
    }

    public void setRamt2o(String value) {
        setString("RAMT2O", value);
    }

    public String getRcposto() {
        return getString("RCPOSTO");
    }

    public void setRcposto(String value) {
        setString("RCPOSTO", value);
    }

    public String getRcreado() {
        return getString("RCREADO");
    }

    public void setRcreado(String value) {
        setString("RCREADO", value);
    }

    public String getRcrejo() {
        return getString("RCREJO");
    }

    public void setRcrejo(String value) {
        setString("RCREJO", value);
    }

    public String getRcselo() {
        return getString("RCSELO");
    }

    public void setRcselo(String value) {
        setString("RCSELO", value);
    }

    public String getRctrano() {
        return getString("RCTRANO");
    }

    public void setRctrano(String value) {
        setString("RCTRANO", value);
    }

    public String getRcupdo() {
        return getString("RCUPDO");
    }

    public void setRcupdo(String value) {
        setString("RCUPDO", value);
    }

    public String getRmsgo() {
        return getString("RMSGO");
    }

    public void setRmsgo(String value) {
        setString("RMSGO", value);
    }

    public String getRstato() {
        return getString("RSTATO");
    }

    public void setRstato(String value) {
        setString("RSTATO", value);
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

    public BigDecimal getWsDAmt() {
        return getDecimal("WS-D-AMT");
    }

    public void setWsDAmt(BigDecimal value) {
        setDecimal("WS-D-AMT", value);
    }

    public int getWsDCnt() {
        return getInt("WS-D-CNT");
    }

    public void setWsDCnt(int value) {
        setInt("WS-D-CNT", value);
    }

    public String getWsDStat() {
        return getString("WS-D-STAT");
    }

    public void setWsDStat(String value) {
        setString("WS-D-STAT", value);
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

    public String getWsMsgText() {
        return getString("WS-MSG-TEXT");
    }

    public void setWsMsgText(String value) {
        setString("WS-MSG-TEXT", value);
    }

    public String getWsNwChar() {
        return getString("WS-NW-CHAR");
    }

    public void setWsNwChar(String value) {
        setString("WS-NW-CHAR", value);
    }

    public int getWsNwDigit() {
        return getInt("WS-NW-DIGIT");
    }

    public void setWsNwDigit(int value) {
        setInt("WS-NW-DIGIT", value);
    }

    public String getWsNwDotSw() {
        return getString("WS-NW-DOT-SW");
    }

    public void setWsNwDotSw(String value) {
        setString("WS-NW-DOT-SW", value);
    }

    public int getWsNwFrac() {
        return getInt("WS-NW-FRAC");
    }

    public void setWsNwFrac(int value) {
        setInt("WS-NW-FRAC", value);
    }

    public int getWsNwFracCnt() {
        return getInt("WS-NW-FRAC-CNT");
    }

    public void setWsNwFracCnt(int value) {
        setInt("WS-NW-FRAC-CNT", value);
    }

    public long getWsNwInt() {
        return getLong("WS-NW-INT");
    }

    public void setWsNwInt(long value) {
        setLong("WS-NW-INT", value);
    }

    public int getWsNwIntCnt() {
        return getInt("WS-NW-INT-CNT");
    }

    public void setWsNwIntCnt(int value) {
        setInt("WS-NW-INT-CNT", value);
    }

    public int getWsNwPos() {
        return getInt("WS-NW-POS");
    }

    public void setWsNwPos(int value) {
        setInt("WS-NW-POS", value);
    }

    public int getWsOption() {
        return getInt("WS-OPTION");
    }

    public void setWsOption(int value) {
        setInt("WS-OPTION", value);
    }

    public long getWsPAcct() {
        return getLong("WS-P-ACCT");
    }

    public void setWsPAcct(long value) {
        setLong("WS-P-ACCT", value);
    }

    public BigDecimal getWsPAmt() {
        return getDecimal("WS-P-AMT");
    }

    public void setWsPAmt(BigDecimal value) {
        setDecimal("WS-P-AMT", value);
    }

    public String getWsParmIn() {
        return getString("WS-PARM-IN");
    }

    public void setWsParmIn(String value) {
        setString("WS-PARM-IN", value);
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

    public String getWsSubPgm() {
        return getString("WS-SUB-PGM");
    }

    public void setWsSubPgm(String value) {
        setString("WS-SUB-PGM", value);
    }

    public String getWsTok1() {
        return getString("WS-TOK1");
    }

    public void setWsTok1(String value) {
        setString("WS-TOK1", value);
    }

    public String getWsTok2() {
        return getString("WS-TOK2");
    }

    public void setWsTok2(String value) {
        setString("WS-TOK2", value);
    }

    public String getWsTranid() {
        return getString("WS-TRANID");
    }

    public void setWsTranid(String value) {
        setString("WS-TRANID", value);
    }
}

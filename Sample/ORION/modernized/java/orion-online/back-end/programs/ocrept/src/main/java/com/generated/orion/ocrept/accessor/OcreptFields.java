package com.generated.orion.ocrept.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ocrept.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OCREPT. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OcreptFields extends DynamicFieldAccessor {

    public OcreptFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public BigDecimal getBlAmount() {
        return getDecimal("BL-AMOUNT");
    }

    public void setBlAmount(BigDecimal value) {
        setDecimal("BL-AMOUNT", value);
    }

    public long getBlId() {
        return getLong("BL-ID");
    }

    public void setBlId(long value) {
        setLong("BL-ID", value);
    }

    public String getBlPayDate() {
        return getString("BL-PAY-DATE");
    }

    public void setBlPayDate(String value) {
        setString("BL-PAY-DATE", value);
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

    public String getKdDateIn() {
        return getString("KD-DATE-IN");
    }

    public void setKdDateIn(String value) {
        setString("KD-DATE-IN", value);
    }

    public String getKdDateOut() {
        return getString("KD-DATE-OUT");
    }

    public void setKdDateOut(String value) {
        setString("KD-DATE-OUT", value);
    }

    public String getKdFunc() {
        return getString("KD-FUNC");
    }

    public void setKdFunc(String value) {
        setString("KD-FUNC", value);
    }

    public String getKdStatus() {
        return getString("KD-STATUS");
    }

    public void setKdStatus(String value) {
        setString("KD-STATUS", value);
    }

    public String getKdateParm() {
        return groupToString("KDATE-PARM");
    }

    public void setKdateParm(String value) {
        setGroup("KDATE-PARM", value);
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

    public String getRpfromi() {
        return getString("RPFROMI");
    }

    public void setRpfromi(String value) {
        setString("RPFROMI", value);
    }

    public String getRptoi() {
        return getString("RPTOI");
    }

    public void setRptoi(String value) {
        setString("RPTOI", value);
    }

    public String getRptypei() {
        return getString("RPTYPEI");
    }

    public void setRptypei(String value) {
        setString("RPTYPEI", value);
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

    public String getTrId() {
        return getString("TR-ID");
    }

    public void setTrId(String value) {
        setString("TR-ID", value);
    }

    public String getTrOrigTs() {
        return getString("TR-ORIG-TS");
    }

    public void setTrOrigTs(String value) {
        setString("TR-ORIG-TS", value);
    }

    public String getTrnnameo() {
        return getString("TRNNAMEO");
    }

    public void setTrnnameo(String value) {
        setString("TRNNAMEO", value);
    }

    public String getWsBillfile() {
        return getString("WS-BILLFILE");
    }

    public void setWsBillfile(String value) {
        setString("WS-BILLFILE", value);
    }

    public int getWsEdCnt() {
        return getInt("WS-ED-CNT");
    }

    public void setWsEdCnt(int value) {
        setInt("WS-ED-CNT", value);
    }

    public BigDecimal getWsEdTot() {
        return getDecimal("WS-ED-TOT");
    }

    public void setWsEdTot(BigDecimal value) {
        setDecimal("WS-ED-TOT", value);
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

    public String getWsFromDate() {
        return getString("WS-FROM-DATE");
    }

    public void setWsFromDate(String value) {
        setString("WS-FROM-DATE", value);
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

    public String getWsPgmname() {
        return getString("WS-PGMNAME");
    }

    public void setWsPgmname(String value) {
        setString("WS-PGMNAME", value);
    }

    public String getWsRecDate() {
        return getString("WS-REC-DATE");
    }

    public void setWsRecDate(String value) {
        setString("WS-REC-DATE", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public int getWsRptCount() {
        return getInt("WS-RPT-COUNT");
    }

    public void setWsRptCount(int value) {
        setInt("WS-RPT-COUNT", value);
    }

    public String getWsRptType() {
        return getString("WS-RPT-TYPE");
    }

    public void setWsRptType(String value) {
        setString("WS-RPT-TYPE", value);
    }

    public String getWsToDate() {
        return getString("WS-TO-DATE");
    }

    public void setWsToDate(String value) {
        setString("WS-TO-DATE", value);
    }

    public BigDecimal getWsTotal() {
        return getDecimal("WS-TOTAL");
    }

    public void setWsTotal(BigDecimal value) {
        setDecimal("WS-TOTAL", value);
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

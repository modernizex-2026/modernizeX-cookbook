package com.generated.orion.ocacctl.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ocacctl.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OCACCTL. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OcacctlFields extends DynamicFieldAccessor {

    public OcacctlFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public String getAcActiveStatus() {
        return getString("AC-ACTIVE-STATUS");
    }

    public void setAcActiveStatus(String value) {
        setString("AC-ACTIVE-STATUS", value);
    }

    public BigDecimal getAcCurrBal() {
        return getDecimal("AC-CURR-BAL");
    }

    public void setAcCurrBal(BigDecimal value) {
        setDecimal("AC-CURR-BAL", value);
    }

    public long getAcId() {
        return getLong("AC-ID");
    }

    public void setAcId(long value) {
        setLong("AC-ID", value);
    }

    public String getAcb1o() {
        return getString("ACB1O");
    }

    public void setAcb1o(String value) {
        setString("ACB1O", value);
    }

    public String getAcb2o() {
        return getString("ACB2O");
    }

    public void setAcb2o(String value) {
        setString("ACB2O", value);
    }

    public String getAcb3o() {
        return getString("ACB3O");
    }

    public void setAcb3o(String value) {
        setString("ACB3O", value);
    }

    public String getAcb4o() {
        return getString("ACB4O");
    }

    public void setAcb4o(String value) {
        setString("ACB4O", value);
    }

    public String getAcb5o() {
        return getString("ACB5O");
    }

    public void setAcb5o(String value) {
        setString("ACB5O", value);
    }

    public String getAcl1o() {
        return getString("ACL1O");
    }

    public void setAcl1o(String value) {
        setString("ACL1O", value);
    }

    public String getAcl2o() {
        return getString("ACL2O");
    }

    public void setAcl2o(String value) {
        setString("ACL2O", value);
    }

    public String getAcl3o() {
        return getString("ACL3O");
    }

    public void setAcl3o(String value) {
        setString("ACL3O", value);
    }

    public String getAcl4o() {
        return getString("ACL4O");
    }

    public void setAcl4o(String value) {
        setString("ACL4O", value);
    }

    public String getAcl5o() {
        return getString("ACL5O");
    }

    public void setAcl5o(String value) {
        setString("ACL5O", value);
    }

    public String getAcs1o() {
        return getString("ACS1O");
    }

    public void setAcs1o(String value) {
        setString("ACS1O", value);
    }

    public String getAcs2o() {
        return getString("ACS2O");
    }

    public void setAcs2o(String value) {
        setString("ACS2O", value);
    }

    public String getAcs3o() {
        return getString("ACS3O");
    }

    public void setAcs3o(String value) {
        setString("ACS3O", value);
    }

    public String getAcs4o() {
        return getString("ACS4O");
    }

    public void setAcs4o(String value) {
        setString("ACS4O", value);
    }

    public String getAcs5o() {
        return getString("ACS5O");
    }

    public void setAcs5o(String value) {
        setString("ACS5O", value);
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

    public String getFraccti() {
        return getString("FRACCTI");
    }

    public void setFraccti(String value) {
        setString("FRACCTI", value);
    }

    public int getFracctl() {
        return getInt("FRACCTL");
    }

    public void setFracctl(int value) {
        setInt("FRACCTL", value);
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

    public String getWsAcctfile() {
        return getString("WS-ACCTFILE");
    }

    public void setWsAcctfile(String value) {
        setString("WS-ACCTFILE", value);
    }

    public String getWsBrowseStarted() {
        return getString("WS-BROWSE-STARTED");
    }

    public void setWsBrowseStarted(String value) {
        setString("WS-BROWSE-STARTED", value);
    }

    public int getWsCntEd() {
        return getInt("WS-CNT-ED");
    }

    public void setWsCntEd(int value) {
        setInt("WS-CNT-ED", value);
    }

    public BigDecimal getWsEdBal() {
        return getDecimal("WS-ED-BAL");
    }

    public void setWsEdBal(BigDecimal value) {
        setDecimal("WS-ED-BAL", value);
    }

    public String getWsEndFlg() {
        return getString("WS-END-FLG");
    }

    public void setWsEndFlg(String value) {
        setString("WS-END-FLG", value);
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

    public long getWsLastKey() {
        return getLong("WS-LAST-KEY");
    }

    public void setWsLastKey(long value) {
        setLong("WS-LAST-KEY", value);
    }

    public String getWsMBadStart() {
        return getString("WS-M-BAD-START");
    }

    public void setWsMBadStart(String value) {
        setString("WS-M-BAD-START", value);
    }

    public String getWsMBrowseErr() {
        return getString("WS-M-BROWSE-ERR");
    }

    public void setWsMBrowseErr(String value) {
        setString("WS-M-BROWSE-ERR", value);
    }

    public String getWsMEndFile() {
        return getString("WS-M-END-FILE");
    }

    public void setWsMEndFile(String value) {
        setString("WS-M-END-FILE", value);
    }

    public String getWsMNoneFound() {
        return getString("WS-M-NONE-FOUND");
    }

    public void setWsMNoneFound(String value) {
        setString("WS-M-NONE-FOUND", value);
    }

    public String getWsMPrompt() {
        return getString("WS-M-PROMPT");
    }

    public void setWsMPrompt(String value) {
        setString("WS-M-PROMPT", value);
    }

    public String getWsMSuffix() {
        return getString("WS-M-SUFFIX");
    }

    public void setWsMSuffix(String value) {
        setString("WS-M-SUFFIX", value);
    }

    public int getWsMaxRows() {
        return getInt("WS-MAX-ROWS");
    }

    public void setWsMaxRows(int value) {
        setInt("WS-MAX-ROWS", value);
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

    public long getWsNextStart() {
        return getLong("WS-NEXT-START");
    }

    public void setWsNextStart(long value) {
        setLong("WS-NEXT-START", value);
    }

    public long getWsPageStart() {
        return getLong("WS-PAGE-START");
    }

    public void setWsPageStart(long value) {
        setLong("WS-PAGE-START", value);
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

    public int getWsRowCnt() {
        return getInt("WS-ROW-CNT");
    }

    public void setWsRowCnt(int value) {
        setInt("WS-ROW-CNT", value);
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

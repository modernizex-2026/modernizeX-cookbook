package com.generated.orion.occustl.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.occustl.model.WorkingStorage;

/**
 * Field accessor for OCCUSTL. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OccustlFields extends DynamicFieldAccessor {

    public OccustlFields(WorkingStorage ws) {
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

    public String getCuf1o() {
        return getString("CUF1O");
    }

    public void setCuf1o(String value) {
        setString("CUF1O", value);
    }

    public String getCuf2o() {
        return getString("CUF2O");
    }

    public void setCuf2o(String value) {
        setString("CUF2O", value);
    }

    public String getCuf3o() {
        return getString("CUF3O");
    }

    public void setCuf3o(String value) {
        setString("CUF3O", value);
    }

    public String getCuf4o() {
        return getString("CUF4O");
    }

    public void setCuf4o(String value) {
        setString("CUF4O", value);
    }

    public String getCul1o() {
        return getString("CUL1O");
    }

    public void setCul1o(String value) {
        setString("CUL1O", value);
    }

    public String getCul2o() {
        return getString("CUL2O");
    }

    public void setCul2o(String value) {
        setString("CUL2O", value);
    }

    public String getCul3o() {
        return getString("CUL3O");
    }

    public void setCul3o(String value) {
        setString("CUL3O", value);
    }

    public String getCul4o() {
        return getString("CUL4O");
    }

    public void setCul4o(String value) {
        setString("CUL4O", value);
    }

    public String getCun1o() {
        return getString("CUN1O");
    }

    public void setCun1o(String value) {
        setString("CUN1O", value);
    }

    public String getCun2o() {
        return getString("CUN2O");
    }

    public void setCun2o(String value) {
        setString("CUN2O", value);
    }

    public String getCun3o() {
        return getString("CUN3O");
    }

    public void setCun3o(String value) {
        setString("CUN3O", value);
    }

    public String getCun4o() {
        return getString("CUN4O");
    }

    public void setCun4o(String value) {
        setString("CUN4O", value);
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

    public String getFrcusti() {
        return getString("FRCUSTI");
    }

    public void setFrcusti(String value) {
        setString("FRCUSTI", value);
    }

    public int getFrcustl() {
        return getInt("FRCUSTL");
    }

    public void setFrcustl(int value) {
        setInt("FRCUSTL", value);
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

    public String getWsCustfile() {
        return getString("WS-CUSTFILE");
    }

    public void setWsCustfile(String value) {
        setString("WS-CUSTFILE", value);
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

    public int getWsLastKey() {
        return getInt("WS-LAST-KEY");
    }

    public void setWsLastKey(int value) {
        setInt("WS-LAST-KEY", value);
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

    public String getWsNameLine() {
        return getString("WS-NAME-LINE");
    }

    public void setWsNameLine(String value) {
        setString("WS-NAME-LINE", value);
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

    public int getWsNextStart() {
        return getInt("WS-NEXT-START");
    }

    public void setWsNextStart(int value) {
        setInt("WS-NEXT-START", value);
    }

    public int getWsPageStart() {
        return getInt("WS-PAGE-START");
    }

    public void setWsPageStart(int value) {
        setInt("WS-PAGE-START", value);
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

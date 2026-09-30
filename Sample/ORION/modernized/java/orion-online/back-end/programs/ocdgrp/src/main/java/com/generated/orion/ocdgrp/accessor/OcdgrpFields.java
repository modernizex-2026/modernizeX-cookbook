package com.generated.orion.ocdgrp.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ocdgrp.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OCDGRP. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OcdgrpFields extends DynamicFieldAccessor {

    public OcdgrpFields(WorkingStorage ws) {
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

    public String getDgAcctGroup() {
        return getString("DG-ACCT-GROUP");
    }

    public void setDgAcctGroup(String value) {
        setString("DG-ACCT-GROUP", value);
    }

    public int getDgCatCd() {
        return getInt("DG-CAT-CD");
    }

    public void setDgCatCd(int value) {
        setInt("DG-CAT-CD", value);
    }

    public BigDecimal getDgIntRate() {
        return getDecimal("DG-INT-RATE");
    }

    public void setDgIntRate(BigDecimal value) {
        setDecimal("DG-INT-RATE", value);
    }

    public String getDgKey() {
        return groupToString("DG-KEY");
    }

    public void setDgKey(String value) {
        setGroup("DG-KEY", value);
    }

    public String getDgTypeCd() {
        return getString("DG-TYPE-CD");
    }

    public void setDgTypeCd(String value) {
        setString("DG-TYPE-CD", value);
    }

    public String getDgcati() {
        return getString("DGCATI");
    }

    public void setDgcati(String value) {
        setString("DGCATI", value);
    }

    public String getDgcato() {
        return getString("DGCATO");
    }

    public void setDgcato(String value) {
        setString("DGCATO", value);
    }

    public String getDggrpi() {
        return getString("DGGRPI");
    }

    public void setDggrpi(String value) {
        setString("DGGRPI", value);
    }

    public int getDggrpl() {
        return getInt("DGGRPL");
    }

    public void setDggrpl(int value) {
        setInt("DGGRPL", value);
    }

    public String getDggrpo() {
        return getString("DGGRPO");
    }

    public void setDggrpo(String value) {
        setString("DGGRPO", value);
    }

    public String getDgratei() {
        return getString("DGRATEI");
    }

    public void setDgratei(String value) {
        setString("DGRATEI", value);
    }

    public int getDgratel() {
        return getInt("DGRATEL");
    }

    public void setDgratel(int value) {
        setInt("DGRATEL", value);
    }

    public String getDgrateo() {
        return getString("DGRATEO");
    }

    public void setDgrateo(String value) {
        setString("DGRATEO", value);
    }

    public String getDgrpRec() {
        return groupToString("DGRP-REC");
    }

    public void setDgrpRec(String value) {
        setGroup("DGRP-REC", value);
    }

    public String getDgtypei() {
        return getString("DGTYPEI");
    }

    public void setDgtypei(String value) {
        setString("DGTYPEI", value);
    }

    public String getDgtypeo() {
        return getString("DGTYPEO");
    }

    public void setDgtypeo(String value) {
        setString("DGTYPEO", value);
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

    public int getWsCatEd() {
        return getInt("WS-CAT-ED");
    }

    public void setWsCatEd(int value) {
        setInt("WS-CAT-ED", value);
    }

    public int getWsDgCat() {
        return getInt("WS-DG-CAT");
    }

    public void setWsDgCat(int value) {
        setInt("WS-DG-CAT", value);
    }

    public String getWsDgGroup() {
        return getString("WS-DG-GROUP");
    }

    public void setWsDgGroup(String value) {
        setString("WS-DG-GROUP", value);
    }

    public String getWsDgType() {
        return getString("WS-DG-TYPE");
    }

    public void setWsDgType(String value) {
        setString("WS-DG-TYPE", value);
    }

    public String getWsDgrpfile() {
        return getString("WS-DGRPFILE");
    }

    public void setWsDgrpfile(String value) {
        setString("WS-DGRPFILE", value);
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

    public String getWsMCatNum() {
        return getString("WS-M-CAT-NUM");
    }

    public void setWsMCatNum(String value) {
        setString("WS-M-CAT-NUM", value);
    }

    public String getWsMFound() {
        return getString("WS-M-FOUND");
    }

    public void setWsMFound(String value) {
        setString("WS-M-FOUND", value);
    }

    public String getWsMGrpReq() {
        return getString("WS-M-GRP-REQ");
    }

    public void setWsMGrpReq(String value) {
        setString("WS-M-GRP-REQ", value);
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

    public String getWsMRateBad() {
        return getString("WS-M-RATE-BAD");
    }

    public void setWsMRateBad(String value) {
        setString("WS-M-RATE-BAD", value);
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

    public BigDecimal getWsNewRate() {
        return getDecimal("WS-NEW-RATE");
    }

    public void setWsNewRate(BigDecimal value) {
        setDecimal("WS-NEW-RATE", value);
    }

    public String getWsPgmname() {
        return getString("WS-PGMNAME");
    }

    public void setWsPgmname(String value) {
        setString("WS-PGMNAME", value);
    }

    public BigDecimal getWsRateEd() {
        return getDecimal("WS-RATE-ED");
    }

    public void setWsRateEd(BigDecimal value) {
        setDecimal("WS-RATE-ED", value);
    }

    public String getWsReChar() {
        return getString("WS-RE-CHAR");
    }

    public void setWsReChar(String value) {
        setString("WS-RE-CHAR", value);
    }

    public int getWsReDigit() {
        return getInt("WS-RE-DIGIT");
    }

    public void setWsReDigit(int value) {
        setInt("WS-RE-DIGIT", value);
    }

    public String getWsReDotSw() {
        return getString("WS-RE-DOT-SW");
    }

    public void setWsReDotSw(String value) {
        setString("WS-RE-DOT-SW", value);
    }

    public int getWsReFrac() {
        return getInt("WS-RE-FRAC");
    }

    public void setWsReFrac(int value) {
        setInt("WS-RE-FRAC", value);
    }

    public int getWsReFracCnt() {
        return getInt("WS-RE-FRAC-CNT");
    }

    public void setWsReFracCnt(int value) {
        setInt("WS-RE-FRAC-CNT", value);
    }

    public String getWsReIn() {
        return getString("WS-RE-IN");
    }

    public void setWsReIn(String value) {
        setString("WS-RE-IN", value);
    }

    public int getWsReInt() {
        return getInt("WS-RE-INT");
    }

    public void setWsReInt(int value) {
        setInt("WS-RE-INT", value);
    }

    public int getWsReIntCnt() {
        return getInt("WS-RE-INT-CNT");
    }

    public void setWsReIntCnt(int value) {
        setInt("WS-RE-INT-CNT", value);
    }

    public int getWsRePos() {
        return getInt("WS-RE-POS");
    }

    public void setWsRePos(int value) {
        setInt("WS-RE-POS", value);
    }

    public BigDecimal getWsReResult() {
        return getDecimal("WS-RE-RESULT");
    }

    public void setWsReResult(BigDecimal value) {
        setDecimal("WS-RE-RESULT", value);
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

    public String getWsValidSw() {
        return getString("WS-VALID-SW");
    }

    public void setWsValidSw(String value) {
        setString("WS-VALID-SW", value);
    }
}

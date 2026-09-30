package com.generated.orion.ocstmin.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ocstmin.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OCSTMIN. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OcstminFields extends DynamicFieldAccessor {

    public OcstminFields(WorkingStorage ws) {
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

    public String getFrcyci() {
        return getString("FRCYCI");
    }

    public void setFrcyci(String value) {
        setString("FRCYCI", value);
    }

    public int getKsbMaxRows() {
        return getInt("KSB-MAX-ROWS");
    }

    public void setKsbMaxRows(int value) {
        setInt("KSB-MAX-ROWS", value);
    }

    public String getKsbMode() {
        return getString("KSB-MODE");
    }

    public void setKsbMode(String value) {
        setString("KSB-MODE", value);
    }

    public String getKsbMore() {
        return getString("KSB-MORE");
    }

    public void setKsbMore(String value) {
        setString("KSB-MORE", value);
    }

    public long getKsbNextAcct() {
        return getLong("KSB-NEXT-ACCT");
    }

    public void setKsbNextAcct(long value) {
        setLong("KSB-NEXT-ACCT", value);
    }

    public int getKsbNextCycle() {
        return getInt("KSB-NEXT-CYCLE");
    }

    public void setKsbNextCycle(int value) {
        setInt("KSB-NEXT-CYCLE", value);
    }

    public long getKsbRAcct(int index) {
        return getLong("KSB-R-ACCT", index);
    }

    public void setKsbRAcct(int index, long value) {
        setLong("KSB-R-ACCT", value, index);
    }

    public BigDecimal getKsbRClose(int index) {
        return getDecimal("KSB-R-CLOSE", index);
    }

    public void setKsbRClose(int index, BigDecimal value) {
        setDecimal("KSB-R-CLOSE", value, index);
    }

    public int getKsbRCycle(int index) {
        return getInt("KSB-R-CYCLE", index);
    }

    public void setKsbRCycle(int index, int value) {
        setInt("KSB-R-CYCLE", value, index);
    }

    public String getKsbRDuedt(int index) {
        return getString("KSB-R-DUEDT", index);
    }

    public void setKsbRDuedt(int index, String value) {
        setString("KSB-R-DUEDT", value, index);
    }

    public BigDecimal getKsbRMindue(int index) {
        return getDecimal("KSB-R-MINDUE", index);
    }

    public void setKsbRMindue(int index, BigDecimal value) {
        setDecimal("KSB-R-MINDUE", value, index);
    }

    public BigDecimal getKsbROpen(int index) {
        return getDecimal("KSB-R-OPEN", index);
    }

    public void setKsbROpen(int index, BigDecimal value) {
        setDecimal("KSB-R-OPEN", value, index);
    }

    public int getKsbRowCnt() {
        return getInt("KSB-ROW-CNT");
    }

    public void setKsbRowCnt(int value) {
        setInt("KSB-ROW-CNT", value);
    }

    public long getKsbStartAcct() {
        return getLong("KSB-START-ACCT");
    }

    public void setKsbStartAcct(long value) {
        setLong("KSB-START-ACCT", value);
    }

    public int getKsbStartCycle() {
        return getInt("KSB-START-CYCLE");
    }

    public void setKsbStartCycle(int value) {
        setInt("KSB-START-CYCLE", value);
    }

    public String getKsbStatus() {
        return getString("KSB-STATUS");
    }

    public void setKsbStatus(String value) {
        setString("KSB-STATUS", value);
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

    public String getSa1o() {
        return getString("SA1O");
    }

    public void setSa1o(String value) {
        setString("SA1O", value);
    }

    public String getSa2o() {
        return getString("SA2O");
    }

    public void setSa2o(String value) {
        setString("SA2O", value);
    }

    public String getSa3o() {
        return getString("SA3O");
    }

    public void setSa3o(String value) {
        setString("SA3O", value);
    }

    public String getSa4o() {
        return getString("SA4O");
    }

    public void setSa4o(String value) {
        setString("SA4O", value);
    }

    public String getSa5o() {
        return getString("SA5O");
    }

    public void setSa5o(String value) {
        setString("SA5O", value);
    }

    public String getSa6o() {
        return getString("SA6O");
    }

    public void setSa6o(String value) {
        setString("SA6O", value);
    }

    public String getSc1o() {
        return getString("SC1O");
    }

    public void setSc1o(String value) {
        setString("SC1O", value);
    }

    public String getSc2o() {
        return getString("SC2O");
    }

    public void setSc2o(String value) {
        setString("SC2O", value);
    }

    public String getSc3o() {
        return getString("SC3O");
    }

    public void setSc3o(String value) {
        setString("SC3O", value);
    }

    public String getSc4o() {
        return getString("SC4O");
    }

    public void setSc4o(String value) {
        setString("SC4O", value);
    }

    public String getSc5o() {
        return getString("SC5O");
    }

    public void setSc5o(String value) {
        setString("SC5O", value);
    }

    public String getSc6o() {
        return getString("SC6O");
    }

    public void setSc6o(String value) {
        setString("SC6O", value);
    }

    public String getSd1o() {
        return getString("SD1O");
    }

    public void setSd1o(String value) {
        setString("SD1O", value);
    }

    public String getSd2o() {
        return getString("SD2O");
    }

    public void setSd2o(String value) {
        setString("SD2O", value);
    }

    public String getSd3o() {
        return getString("SD3O");
    }

    public void setSd3o(String value) {
        setString("SD3O", value);
    }

    public String getSd4o() {
        return getString("SD4O");
    }

    public void setSd4o(String value) {
        setString("SD4O", value);
    }

    public String getSd5o() {
        return getString("SD5O");
    }

    public void setSd5o(String value) {
        setString("SD5O", value);
    }

    public String getSd6o() {
        return getString("SD6O");
    }

    public void setSd6o(String value) {
        setString("SD6O", value);
    }

    public String getSl1o() {
        return getString("SL1O");
    }

    public void setSl1o(String value) {
        setString("SL1O", value);
    }

    public String getSl2o() {
        return getString("SL2O");
    }

    public void setSl2o(String value) {
        setString("SL2O", value);
    }

    public String getSl3o() {
        return getString("SL3O");
    }

    public void setSl3o(String value) {
        setString("SL3O", value);
    }

    public String getSl4o() {
        return getString("SL4O");
    }

    public void setSl4o(String value) {
        setString("SL4O", value);
    }

    public String getSl5o() {
        return getString("SL5O");
    }

    public void setSl5o(String value) {
        setString("SL5O", value);
    }

    public String getSl6o() {
        return getString("SL6O");
    }

    public void setSl6o(String value) {
        setString("SL6O", value);
    }

    public String getSm1o() {
        return getString("SM1O");
    }

    public void setSm1o(String value) {
        setString("SM1O", value);
    }

    public String getSm2o() {
        return getString("SM2O");
    }

    public void setSm2o(String value) {
        setString("SM2O", value);
    }

    public String getSm3o() {
        return getString("SM3O");
    }

    public void setSm3o(String value) {
        setString("SM3O", value);
    }

    public String getSm4o() {
        return getString("SM4O");
    }

    public void setSm4o(String value) {
        setString("SM4O", value);
    }

    public String getSm5o() {
        return getString("SM5O");
    }

    public void setSm5o(String value) {
        setString("SM5O", value);
    }

    public String getSm6o() {
        return getString("SM6O");
    }

    public void setSm6o(String value) {
        setString("SM6O", value);
    }

    public String getSo1o() {
        return getString("SO1O");
    }

    public void setSo1o(String value) {
        setString("SO1O", value);
    }

    public String getSo2o() {
        return getString("SO2O");
    }

    public void setSo2o(String value) {
        setString("SO2O", value);
    }

    public String getSo3o() {
        return getString("SO3O");
    }

    public void setSo3o(String value) {
        setString("SO3O", value);
    }

    public String getSo4o() {
        return getString("SO4O");
    }

    public void setSo4o(String value) {
        setString("SO4O", value);
    }

    public String getSo5o() {
        return getString("SO5O");
    }

    public void setSo5o(String value) {
        setString("SO5O", value);
    }

    public String getSo6o() {
        return getString("SO6O");
    }

    public void setSo6o(String value) {
        setString("SO6O", value);
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

    public int getWsCntEd() {
        return getInt("WS-CNT-ED");
    }

    public void setWsCntEd(int value) {
        setInt("WS-CNT-ED", value);
    }

    public BigDecimal getWsEdMoney() {
        return getDecimal("WS-ED-MONEY");
    }

    public void setWsEdMoney(BigDecimal value) {
        setDecimal("WS-ED-MONEY", value);
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

    public int getWsIdx() {
        return getInt("WS-IDX");
    }

    public void setWsIdx(int value) {
        setInt("WS-IDX", value);
    }

    public String getWsMBadAcct() {
        return getString("WS-M-BAD-ACCT");
    }

    public void setWsMBadAcct(String value) {
        setString("WS-M-BAD-ACCT", value);
    }

    public String getWsMBadCycle() {
        return getString("WS-M-BAD-CYCLE");
    }

    public void setWsMBadCycle(String value) {
        setString("WS-M-BAD-CYCLE", value);
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

    public String getWsPageState() {
        return groupToString("WS-PAGE-STATE");
    }

    public void setWsPageState(String value) {
        setGroup("WS-PAGE-STATE", value);
    }

    public String getWsPgmname() {
        return getString("WS-PGMNAME");
    }

    public void setWsPgmname(String value) {
        setString("WS-PGMNAME", value);
    }

    public long getWsPsFltAcct() {
        return getLong("WS-PS-FLT-ACCT");
    }

    public void setWsPsFltAcct(long value) {
        setLong("WS-PS-FLT-ACCT", value);
    }

    public int getWsPsFltCycle() {
        return getInt("WS-PS-FLT-CYCLE");
    }

    public void setWsPsFltCycle(int value) {
        setInt("WS-PS-FLT-CYCLE", value);
    }

    public String getWsPsFltSet() {
        return getString("WS-PS-FLT-SET");
    }

    public void setWsPsFltSet(String value) {
        setString("WS-PS-FLT-SET", value);
    }

    public String getWsPsMore() {
        return getString("WS-PS-MORE");
    }

    public void setWsPsMore(String value) {
        setString("WS-PS-MORE", value);
    }

    public long getWsPsNextAcct() {
        return getLong("WS-PS-NEXT-ACCT");
    }

    public void setWsPsNextAcct(long value) {
        setLong("WS-PS-NEXT-ACCT", value);
    }

    public int getWsPsNextCycle() {
        return getInt("WS-PS-NEXT-CYCLE");
    }

    public void setWsPsNextCycle(int value) {
        setInt("WS-PS-NEXT-CYCLE", value);
    }

    public String getWsRAcct() {
        return getString("WS-R-ACCT");
    }

    public void setWsRAcct(String value) {
        setString("WS-R-ACCT", value);
    }

    public String getWsRClose() {
        return getString("WS-R-CLOSE");
    }

    public void setWsRClose(String value) {
        setString("WS-R-CLOSE", value);
    }

    public String getWsRCycle() {
        return getString("WS-R-CYCLE");
    }

    public void setWsRCycle(String value) {
        setString("WS-R-CYCLE", value);
    }

    public String getWsRDue() {
        return getString("WS-R-DUE");
    }

    public void setWsRDue(String value) {
        setString("WS-R-DUE", value);
    }

    public String getWsRMin() {
        return getString("WS-R-MIN");
    }

    public void setWsRMin(String value) {
        setString("WS-R-MIN", value);
    }

    public String getWsROpen() {
        return getString("WS-R-OPEN");
    }

    public void setWsROpen(String value) {
        setString("WS-R-OPEN", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public long getWsStartAcct() {
        return getLong("WS-START-ACCT");
    }

    public void setWsStartAcct(long value) {
        setLong("WS-START-ACCT", value);
    }

    public int getWsStartCycle() {
        return getInt("WS-START-CYCLE");
    }

    public void setWsStartCycle(int value) {
        setInt("WS-START-CYCLE", value);
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

    public String getWsValidSw() {
        return getString("WS-VALID-SW");
    }

    public void setWsValidSw(String value) {
        setString("WS-VALID-SW", value);
    }
}

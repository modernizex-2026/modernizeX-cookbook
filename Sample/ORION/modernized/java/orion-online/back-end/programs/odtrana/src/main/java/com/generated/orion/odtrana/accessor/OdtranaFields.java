package com.generated.orion.odtrana.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.odtrana.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for ODTRANA. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OdtranaFields extends DynamicFieldAccessor {

    public OdtranaFields(WorkingStorage ws) {
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

    public String getCardnumi() {
        return getString("CARDNUMI");
    }

    public void setCardnumi(String value) {
        setString("CARDNUMI", value);
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

    public int getTrCatCd() {
        return getInt("TR-CAT-CD");
    }

    public void setTrCatCd(int value) {
        setInt("TR-CAT-CD", value);
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

    public String getTrMerchantCity() {
        return getString("TR-MERCHANT-CITY");
    }

    public void setTrMerchantCity(String value) {
        setString("TR-MERCHANT-CITY", value);
    }

    public int getTrMerchantId() {
        return getInt("TR-MERCHANT-ID");
    }

    public void setTrMerchantId(int value) {
        setInt("TR-MERCHANT-ID", value);
    }

    public String getTrMerchantName() {
        return getString("TR-MERCHANT-NAME");
    }

    public void setTrMerchantName(String value) {
        setString("TR-MERCHANT-NAME", value);
    }

    public String getTrMerchantZip() {
        return getString("TR-MERCHANT-ZIP");
    }

    public void setTrMerchantZip(String value) {
        setString("TR-MERCHANT-ZIP", value);
    }

    public String getTrOrigTs() {
        return getString("TR-ORIG-TS");
    }

    public void setTrOrigTs(String value) {
        setString("TR-ORIG-TS", value);
    }

    public String getTrProcTs() {
        return getString("TR-PROC-TS");
    }

    public void setTrProcTs(String value) {
        setString("TR-PROC-TS", value);
    }

    public String getTrSource() {
        return getString("TR-SOURCE");
    }

    public void setTrSource(String value) {
        setString("TR-SOURCE", value);
    }

    public String getTrTypeCd() {
        return getString("TR-TYPE-CD");
    }

    public void setTrTypeCd(String value) {
        setString("TR-TYPE-CD", value);
    }

    public String getTramti() {
        return getString("TRAMTI");
    }

    public void setTramti(String value) {
        setString("TRAMTI", value);
    }

    public String getTrcati() {
        return getString("TRCATI");
    }

    public void setTrcati(String value) {
        setString("TRCATI", value);
    }

    public String getTrdesci() {
        return getString("TRDESCI");
    }

    public void setTrdesci(String value) {
        setString("TRDESCI", value);
    }

    public String getTrmerchi() {
        return getString("TRMERCHI");
    }

    public void setTrmerchi(String value) {
        setString("TRMERCHI", value);
    }

    public String getTrnnameo() {
        return getString("TRNNAMEO");
    }

    public void setTrnnameo(String value) {
        setString("TRNNAMEO", value);
    }

    public String getTrtypei() {
        return getString("TRTYPEI");
    }

    public void setTrtypei(String value) {
        setString("TRTYPEI", value);
    }

    public String getWsChkCardStat() {
        return getString("WS-CHK-CARD-STAT");
    }

    public void setWsChkCardStat(String value) {
        setString("WS-CHK-CARD-STAT", value);
    }

    public String getWsChkTypeDesc() {
        return getString("WS-CHK-TYPE-DESC");
    }

    public void setWsChkTypeDesc(String value) {
        setString("WS-CHK-TYPE-DESC", value);
    }

    public String getWsCtKey() {
        return getString("WS-CT-KEY");
    }

    public void setWsCtKey(String value) {
        setString("WS-CT-KEY", value);
    }

    public long getWsCtValue() {
        return getLong("WS-CT-VALUE");
    }

    public void setWsCtValue(long value) {
        setLong("WS-CT-VALUE", value);
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

    public long getWsTranIdN() {
        return getLong("WS-TRAN-ID-N");
    }

    public void setWsTranIdN(long value) {
        setLong("WS-TRAN-ID-N", value);
    }

    public String getWsTranid() {
        return getString("WS-TRANID");
    }

    public void setWsTranid(String value) {
        setString("WS-TRANID", value);
    }

    public String getWsTsStamp() {
        return getString("WS-TS-STAMP");
    }

    public void setWsTsStamp(String value) {
        setString("WS-TS-STAMP", value);
    }

    public String getWsValidFlag() {
        return getString("WS-VALID-FLAG");
    }

    public void setWsValidFlag(String value) {
        setString("WS-VALID-FLAG", value);
    }
}

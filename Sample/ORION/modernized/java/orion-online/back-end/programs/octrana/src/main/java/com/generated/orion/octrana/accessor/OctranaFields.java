package com.generated.orion.octrana.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.octrana.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OCTRANA. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OctranaFields extends DynamicFieldAccessor {

    public OctranaFields(WorkingStorage ws) {
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

    public int getCardnuml() {
        return getInt("CARDNUML");
    }

    public void setCardnuml(int value) {
        setInt("CARDNUML", value);
    }

    public String getCtKey() {
        return getString("CT-KEY");
    }

    public void setCtKey(String value) {
        setString("CT-KEY", value);
    }

    public long getCtLastValue() {
        return getLong("CT-LAST-VALUE");
    }

    public void setCtLastValue(long value) {
        setLong("CT-LAST-VALUE", value);
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

    public int getTcCd() {
        return getInt("TC-CD");
    }

    public void setTcCd(int value) {
        setInt("TC-CD", value);
    }

    public String getTcKey() {
        return groupToString("TC-KEY");
    }

    public void setTcKey(String value) {
        setGroup("TC-KEY", value);
    }

    public String getTcTypeCd() {
        return getString("TC-TYPE-CD");
    }

    public void setTcTypeCd(String value) {
        setString("TC-TYPE-CD", value);
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

    public int getTramtl() {
        return getInt("TRAMTL");
    }

    public void setTramtl(int value) {
        setInt("TRAMTL", value);
    }

    public String getTranRec() {
        return groupToString("TRAN-REC");
    }

    public void setTranRec(String value) {
        setGroup("TRAN-REC", value);
    }

    public String getTrcati() {
        return getString("TRCATI");
    }

    public void setTrcati(String value) {
        setString("TRCATI", value);
    }

    public int getTrcatl() {
        return getInt("TRCATL");
    }

    public void setTrcatl(int value) {
        setInt("TRCATL", value);
    }

    public String getTrdesci() {
        return getString("TRDESCI");
    }

    public void setTrdesci(String value) {
        setString("TRDESCI", value);
    }

    public int getTrdescl() {
        return getInt("TRDESCL");
    }

    public void setTrdescl(int value) {
        setInt("TRDESCL", value);
    }

    public String getTrmerchi() {
        return getString("TRMERCHI");
    }

    public void setTrmerchi(String value) {
        setString("TRMERCHI", value);
    }

    public int getTrmerchl() {
        return getInt("TRMERCHL");
    }

    public void setTrmerchl(int value) {
        setInt("TRMERCHL", value);
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

    public int getTrtypel() {
        return getInt("TRTYPEL");
    }

    public void setTrtypel(int value) {
        setInt("TRTYPEL", value);
    }

    public String getTtCd() {
        return getString("TT-CD");
    }

    public void setTtCd(String value) {
        setString("TT-CD", value);
    }

    public BigDecimal getWsAmtNum() {
        return getDecimal("WS-AMT-NUM");
    }

    public void setWsAmtNum(BigDecimal value) {
        setDecimal("WS-AMT-NUM", value);
    }

    public String getWsCardKey() {
        return getString("WS-CARD-KEY");
    }

    public void setWsCardKey(String value) {
        setString("WS-CARD-KEY", value);
    }

    public String getWsCtrlfile() {
        return getString("WS-CTRLFILE");
    }

    public void setWsCtrlfile(String value) {
        setString("WS-CTRLFILE", value);
    }

    public int getWsDotCnt() {
        return getInt("WS-DOT-CNT");
    }

    public void setWsDotCnt(int value) {
        setInt("WS-DOT-CNT", value);
    }

    public String getWsEditFlag() {
        return getString("WS-EDIT-FLAG");
    }

    public void setWsEditFlag(String value) {
        setString("WS-EDIT-FLAG", value);
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

    public String getWsIdFlag() {
        return getString("WS-ID-FLAG");
    }

    public void setWsIdFlag(String value) {
        setString("WS-ID-FLAG", value);
    }

    public int getWsInCat() {
        return getInt("WS-IN-CAT");
    }

    public void setWsInCat(int value) {
        setInt("WS-IN-CAT", value);
    }

    public String getWsMAddPrefix() {
        return getString("WS-M-ADD-PREFIX");
    }

    public void setWsMAddPrefix(String value) {
        setString("WS-M-ADD-PREFIX", value);
    }

    public String getWsMAddSuffix() {
        return getString("WS-M-ADD-SUFFIX");
    }

    public void setWsMAddSuffix(String value) {
        setString("WS-M-ADD-SUFFIX", value);
    }

    public String getWsMAmtBad() {
        return getString("WS-M-AMT-BAD");
    }

    public void setWsMAmtBad(String value) {
        setString("WS-M-AMT-BAD", value);
    }

    public String getWsMAmtReq() {
        return getString("WS-M-AMT-REQ");
    }

    public void setWsMAmtReq(String value) {
        setString("WS-M-AMT-REQ", value);
    }

    public String getWsMAmtZero() {
        return getString("WS-M-AMT-ZERO");
    }

    public void setWsMAmtZero(String value) {
        setString("WS-M-AMT-ZERO", value);
    }

    public String getWsMCardBad() {
        return getString("WS-M-CARD-BAD");
    }

    public void setWsMCardBad(String value) {
        setString("WS-M-CARD-BAD", value);
    }

    public String getWsMCardReq() {
        return getString("WS-M-CARD-REQ");
    }

    public void setWsMCardReq(String value) {
        setString("WS-M-CARD-REQ", value);
    }

    public String getWsMCatBad() {
        return getString("WS-M-CAT-BAD");
    }

    public void setWsMCatBad(String value) {
        setString("WS-M-CAT-BAD", value);
    }

    public String getWsMCatNum() {
        return getString("WS-M-CAT-NUM");
    }

    public void setWsMCatNum(String value) {
        setString("WS-M-CAT-NUM", value);
    }

    public String getWsMCatReq() {
        return getString("WS-M-CAT-REQ");
    }

    public void setWsMCatReq(String value) {
        setString("WS-M-CAT-REQ", value);
    }

    public String getWsMCtrError() {
        return getString("WS-M-CTR-ERROR");
    }

    public void setWsMCtrError(String value) {
        setString("WS-M-CTR-ERROR", value);
    }

    public String getWsMCtrMissing() {
        return getString("WS-M-CTR-MISSING");
    }

    public void setWsMCtrMissing(String value) {
        setString("WS-M-CTR-MISSING", value);
    }

    public String getWsMDescReq() {
        return getString("WS-M-DESC-REQ");
    }

    public void setWsMDescReq(String value) {
        setString("WS-M-DESC-REQ", value);
    }

    public String getWsMMerchReq() {
        return getString("WS-M-MERCH-REQ");
    }

    public void setWsMMerchReq(String value) {
        setString("WS-M-MERCH-REQ", value);
    }

    public String getWsMPrompt() {
        return getString("WS-M-PROMPT");
    }

    public void setWsMPrompt(String value) {
        setString("WS-M-PROMPT", value);
    }

    public String getWsMSource() {
        return getString("WS-M-SOURCE");
    }

    public void setWsMSource(String value) {
        setString("WS-M-SOURCE", value);
    }

    public String getWsMTypeBad() {
        return getString("WS-M-TYPE-BAD");
    }

    public void setWsMTypeBad(String value) {
        setString("WS-M-TYPE-BAD", value);
    }

    public String getWsMTypeReq() {
        return getString("WS-M-TYPE-REQ");
    }

    public void setWsMTypeReq(String value) {
        setString("WS-M-TYPE-REQ", value);
    }

    public String getWsMWriteError() {
        return getString("WS-M-WRITE-ERROR");
    }

    public void setWsMWriteError(String value) {
        setString("WS-M-WRITE-ERROR", value);
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

    public long getWsNewTranid() {
        return getLong("WS-NEW-TRANID");
    }

    public void setWsNewTranid(long value) {
        setLong("WS-NEW-TRANID", value);
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

    public String getWsTcatfile() {
        return getString("WS-TCATFILE");
    }

    public void setWsTcatfile(String value) {
        setString("WS-TCATFILE", value);
    }

    public String getWsTimestamp() {
        return getString("WS-TIMESTAMP");
    }

    public void setWsTimestamp(String value) {
        setString("WS-TIMESTAMP", value);
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

    public String getWsTtypfile() {
        return getString("WS-TTYPFILE");
    }

    public void setWsTtypfile(String value) {
        setString("WS-TTYPFILE", value);
    }

    public String getWsValCh() {
        return getString("WS-VAL-CH");
    }

    public void setWsValCh(String value) {
        setString("WS-VAL-CH", value);
    }

    public int getWsValDigits() {
        return getInt("WS-VAL-DIGITS");
    }

    public void setWsValDigits(int value) {
        setInt("WS-VAL-DIGITS", value);
    }

    public int getWsValI() {
        return getInt("WS-VAL-I");
    }

    public void setWsValI(int value) {
        setInt("WS-VAL-I", value);
    }

    public int getWsValLen() {
        return getInt("WS-VAL-LEN");
    }

    public void setWsValLen(int value) {
        setInt("WS-VAL-LEN", value);
    }

    public String getWsValOk() {
        return getString("WS-VAL-OK");
    }

    public void setWsValOk(String value) {
        setString("WS-VAL-OK", value);
    }

    public String getWsValStr() {
        return getString("WS-VAL-STR");
    }

    public void setWsValStr(String value) {
        setString("WS-VAL-STR", value);
    }

    public String getWsXreffile() {
        return getString("WS-XREFFILE");
    }

    public void setWsXreffile(String value) {
        setString("WS-XREFFILE", value);
    }

    public String getXrCardNum() {
        return getString("XR-CARD-NUM");
    }

    public void setXrCardNum(String value) {
        setString("XR-CARD-NUM", value);
    }
}

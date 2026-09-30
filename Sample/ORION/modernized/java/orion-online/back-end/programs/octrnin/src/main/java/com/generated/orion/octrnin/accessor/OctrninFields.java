package com.generated.orion.octrnin.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.octrnin.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OCTRNIN. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OctrninFields extends DynamicFieldAccessor {

    public OctrninFields(WorkingStorage ws) {
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

    public String getCaToProgram() {
        return getString("CA-TO-PROGRAM");
    }

    public void setCaToProgram(String value) {
        setString("CA-TO-PROGRAM", value);
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

    public String getFamti() {
        return getString("FAMTI");
    }

    public void setFamti(String value) {
        setString("FAMTI", value);
    }

    public String getFcardi() {
        return getString("FCARDI");
    }

    public void setFcardi(String value) {
        setString("FCARDI", value);
    }

    public String getFcati() {
        return getString("FCATI");
    }

    public void setFcati(String value) {
        setString("FCATI", value);
    }

    public String getFcnto() {
        return getString("FCNTO");
    }

    public void setFcnto(String value) {
        setString("FCNTO", value);
    }

    public String getFmerchi() {
        return getString("FMERCHI");
    }

    public void setFmerchi(String value) {
        setString("FMERCHI", value);
    }

    public String getFrdatei() {
        return getString("FRDATEI");
    }

    public void setFrdatei(String value) {
        setString("FRDATEI", value);
    }

    public String getFsumo() {
        return getString("FSUMO");
    }

    public void setFsumo(String value) {
        setString("FSUMO", value);
    }

    public String getFtdatei() {
        return getString("FTDATEI");
    }

    public void setFtdatei(String value) {
        setString("FTDATEI", value);
    }

    public String getFtypei() {
        return getString("FTYPEI");
    }

    public void setFtypei(String value) {
        setString("FTYPEI", value);
    }

    public String getIcnto() {
        return getString("ICNTO");
    }

    public void setIcnto(String value) {
        setString("ICNTO", value);
    }

    public String getIsumo() {
        return getString("ISUMO");
    }

    public void setIsumo(String value) {
        setString("ISUMO", value);
    }

    public BigDecimal getKtiAmtThresh() {
        return getDecimal("KTI-AMT-THRESH");
    }

    public void setKtiAmtThresh(BigDecimal value) {
        setDecimal("KTI-AMT-THRESH", value);
    }

    public String getKtiCard() {
        return getString("KTI-CARD");
    }

    public void setKtiCard(String value) {
        setString("KTI-CARD", value);
    }

    public String getKtiDateFrom() {
        return getString("KTI-DATE-FROM");
    }

    public void setKtiDateFrom(String value) {
        setString("KTI-DATE-FROM", value);
    }

    public String getKtiDateTo() {
        return getString("KTI-DATE-TO");
    }

    public void setKtiDateTo(String value) {
        setString("KTI-DATE-TO", value);
    }

    public int getKtiFCat() {
        return getInt("KTI-F-CAT");
    }

    public void setKtiFCat(int value) {
        setInt("KTI-F-CAT", value);
    }

    public String getKtiFType() {
        return getString("KTI-F-TYPE");
    }

    public void setKtiFType(String value) {
        setString("KTI-F-TYPE", value);
    }

    public int getKtiFeeCnt() {
        return getInt("KTI-FEE-CNT");
    }

    public void setKtiFeeCnt(int value) {
        setInt("KTI-FEE-CNT", value);
    }

    public BigDecimal getKtiFeeSum() {
        return getDecimal("KTI-FEE-SUM");
    }

    public void setKtiFeeSum(BigDecimal value) {
        setDecimal("KTI-FEE-SUM", value);
    }

    public int getKtiIntCnt() {
        return getInt("KTI-INT-CNT");
    }

    public void setKtiIntCnt(int value) {
        setInt("KTI-INT-CNT", value);
    }

    public BigDecimal getKtiIntSum() {
        return getDecimal("KTI-INT-SUM");
    }

    public void setKtiIntSum(BigDecimal value) {
        setDecimal("KTI-INT-SUM", value);
    }

    public int getKtiMatchCount() {
        return getInt("KTI-MATCH-COUNT");
    }

    public void setKtiMatchCount(int value) {
        setInt("KTI-MATCH-COUNT", value);
    }

    public BigDecimal getKtiMaxAmt() {
        return getDecimal("KTI-MAX-AMT");
    }

    public void setKtiMaxAmt(BigDecimal value) {
        setDecimal("KTI-MAX-AMT", value);
    }

    public String getKtiMaxId() {
        return getString("KTI-MAX-ID");
    }

    public void setKtiMaxId(String value) {
        setString("KTI-MAX-ID", value);
    }

    public int getKtiMerchId() {
        return getInt("KTI-MERCH-ID");
    }

    public void setKtiMerchId(int value) {
        setInt("KTI-MERCH-ID", value);
    }

    public String getKtiMoreSw() {
        return getString("KTI-MORE-SW");
    }

    public void setKtiMoreSw(String value) {
        setString("KTI-MORE-SW", value);
    }

    public BigDecimal getKtiNetTotal() {
        return getDecimal("KTI-NET-TOTAL");
    }

    public void setKtiNetTotal(BigDecimal value) {
        setDecimal("KTI-NET-TOTAL", value);
    }

    public String getKtiNextKey() {
        return getString("KTI-NEXT-KEY");
    }

    public void setKtiNextKey(String value) {
        setString("KTI-NEXT-KEY", value);
    }

    public int getKtiPayCnt() {
        return getInt("KTI-PAY-CNT");
    }

    public void setKtiPayCnt(int value) {
        setInt("KTI-PAY-CNT", value);
    }

    public BigDecimal getKtiPaySum() {
        return getDecimal("KTI-PAY-SUM");
    }

    public void setKtiPaySum(BigDecimal value) {
        setDecimal("KTI-PAY-SUM", value);
    }

    public int getKtiPurchCnt() {
        return getInt("KTI-PURCH-CNT");
    }

    public void setKtiPurchCnt(int value) {
        setInt("KTI-PURCH-CNT", value);
    }

    public BigDecimal getKtiPurchSum() {
        return getDecimal("KTI-PURCH-SUM");
    }

    public void setKtiPurchSum(BigDecimal value) {
        setDecimal("KTI-PURCH-SUM", value);
    }

    public String getKtiRequest() {
        return groupToString("KTI-REQUEST");
    }

    public void setKtiRequest(String value) {
        setGroup("KTI-REQUEST", value);
    }

    public String getKtiReturnCd() {
        return getString("KTI-RETURN-CD");
    }

    public void setKtiReturnCd(String value) {
        setString("KTI-RETURN-CD", value);
    }

    public int getKtiRowCount() {
        return getInt("KTI-ROW-COUNT");
    }

    public void setKtiRowCount(int value) {
        setInt("KTI-ROW-COUNT", value);
    }

    public String getKtiStartKey() {
        return getString("KTI-START-KEY");
    }

    public void setKtiStartKey(String value) {
        setString("KTI-START-KEY", value);
    }

    public BigDecimal getKtrAmt(int index) {
        return getDecimal("KTR-AMT", index);
    }

    public void setKtrAmt(int index, BigDecimal value) {
        setDecimal("KTR-AMT", value, index);
    }

    public String getKtrCard(int index) {
        return getString("KTR-CARD", index);
    }

    public void setKtrCard(int index, String value) {
        setString("KTR-CARD", value, index);
    }

    public String getKtrDate(int index) {
        return getString("KTR-DATE", index);
    }

    public void setKtrDate(int index, String value) {
        setString("KTR-DATE", value, index);
    }

    public String getKtrDesc(int index) {
        return getString("KTR-DESC", index);
    }

    public void setKtrDesc(int index, String value) {
        setString("KTR-DESC", value, index);
    }

    public String getKtrId(int index) {
        return getString("KTR-ID", index);
    }

    public void setKtrId(int index, String value) {
        setString("KTR-ID", value, index);
    }

    public String getKtrMerch(int index) {
        return getString("KTR-MERCH", index);
    }

    public void setKtrMerch(int index, String value) {
        setString("KTR-MERCH", value, index);
    }

    public String getKtrTycat(int index) {
        return getString("KTR-TYCAT", index);
    }

    public void setKtrTycat(int index, String value) {
        setString("KTR-TYCAT", value, index);
    }

    public String getKtrninArea() {
        return groupToString("KTRNIN-AREA");
    }

    public void setKtrninArea(String value) {
        setGroup("KTRNIN-AREA", value);
    }

    public String getMcnto() {
        return getString("MCNTO");
    }

    public void setMcnto(String value) {
        setString("MCNTO", value);
    }

    public String getMtoto() {
        return getString("MTOTO");
    }

    public void setMtoto(String value) {
        setString("MTOTO", value);
    }

    public String getOrionCommarea() {
        return groupToString("ORION-COMMAREA");
    }

    public void setOrionCommarea(String value) {
        setGroup("ORION-COMMAREA", value);
    }

    public String getPcnto() {
        return getString("PCNTO");
    }

    public void setPcnto(String value) {
        setString("PCNTO", value);
    }

    public String getPgmnameo() {
        return getString("PGMNAMEO");
    }

    public void setPgmnameo(String value) {
        setString("PGMNAMEO", value);
    }

    public String getPsumo() {
        return getString("PSUMO");
    }

    public void setPsumo(String value) {
        setString("PSUMO", value);
    }

    public String getTam1o() {
        return getString("TAM1O");
    }

    public void setTam1o(String value) {
        setString("TAM1O", value);
    }

    public String getTam2o() {
        return getString("TAM2O");
    }

    public void setTam2o(String value) {
        setString("TAM2O", value);
    }

    public String getTam3o() {
        return getString("TAM3O");
    }

    public void setTam3o(String value) {
        setString("TAM3O", value);
    }

    public String getTam4o() {
        return getString("TAM4O");
    }

    public void setTam4o(String value) {
        setString("TAM4O", value);
    }

    public String getTam5o() {
        return getString("TAM5O");
    }

    public void setTam5o(String value) {
        setString("TAM5O", value);
    }

    public String getTam6o() {
        return getString("TAM6O");
    }

    public void setTam6o(String value) {
        setString("TAM6O", value);
    }

    public String getTcd1o() {
        return getString("TCD1O");
    }

    public void setTcd1o(String value) {
        setString("TCD1O", value);
    }

    public String getTcd2o() {
        return getString("TCD2O");
    }

    public void setTcd2o(String value) {
        setString("TCD2O", value);
    }

    public String getTcd3o() {
        return getString("TCD3O");
    }

    public void setTcd3o(String value) {
        setString("TCD3O", value);
    }

    public String getTcd4o() {
        return getString("TCD4O");
    }

    public void setTcd4o(String value) {
        setString("TCD4O", value);
    }

    public String getTcd5o() {
        return getString("TCD5O");
    }

    public void setTcd5o(String value) {
        setString("TCD5O", value);
    }

    public String getTcd6o() {
        return getString("TCD6O");
    }

    public void setTcd6o(String value) {
        setString("TCD6O", value);
    }

    public String getTds1o() {
        return getString("TDS1O");
    }

    public void setTds1o(String value) {
        setString("TDS1O", value);
    }

    public String getTds2o() {
        return getString("TDS2O");
    }

    public void setTds2o(String value) {
        setString("TDS2O", value);
    }

    public String getTds3o() {
        return getString("TDS3O");
    }

    public void setTds3o(String value) {
        setString("TDS3O", value);
    }

    public String getTds4o() {
        return getString("TDS4O");
    }

    public void setTds4o(String value) {
        setString("TDS4O", value);
    }

    public String getTds5o() {
        return getString("TDS5O");
    }

    public void setTds5o(String value) {
        setString("TDS5O", value);
    }

    public String getTds6o() {
        return getString("TDS6O");
    }

    public void setTds6o(String value) {
        setString("TDS6O", value);
    }

    public String getTdt1o() {
        return getString("TDT1O");
    }

    public void setTdt1o(String value) {
        setString("TDT1O", value);
    }

    public String getTdt2o() {
        return getString("TDT2O");
    }

    public void setTdt2o(String value) {
        setString("TDT2O", value);
    }

    public String getTdt3o() {
        return getString("TDT3O");
    }

    public void setTdt3o(String value) {
        setString("TDT3O", value);
    }

    public String getTdt4o() {
        return getString("TDT4O");
    }

    public void setTdt4o(String value) {
        setString("TDT4O", value);
    }

    public String getTdt5o() {
        return getString("TDT5O");
    }

    public void setTdt5o(String value) {
        setString("TDT5O", value);
    }

    public String getTdt6o() {
        return getString("TDT6O");
    }

    public void setTdt6o(String value) {
        setString("TDT6O", value);
    }

    public String getTid1o() {
        return getString("TID1O");
    }

    public void setTid1o(String value) {
        setString("TID1O", value);
    }

    public String getTid2o() {
        return getString("TID2O");
    }

    public void setTid2o(String value) {
        setString("TID2O", value);
    }

    public String getTid3o() {
        return getString("TID3O");
    }

    public void setTid3o(String value) {
        setString("TID3O", value);
    }

    public String getTid4o() {
        return getString("TID4O");
    }

    public void setTid4o(String value) {
        setString("TID4O", value);
    }

    public String getTid5o() {
        return getString("TID5O");
    }

    public void setTid5o(String value) {
        setString("TID5O", value);
    }

    public String getTid6o() {
        return getString("TID6O");
    }

    public void setTid6o(String value) {
        setString("TID6O", value);
    }

    public String getTitleo() {
        return getString("TITLEO");
    }

    public void setTitleo(String value) {
        setString("TITLEO", value);
    }

    public String getTmc1o() {
        return getString("TMC1O");
    }

    public void setTmc1o(String value) {
        setString("TMC1O", value);
    }

    public String getTmc2o() {
        return getString("TMC2O");
    }

    public void setTmc2o(String value) {
        setString("TMC2O", value);
    }

    public String getTmc3o() {
        return getString("TMC3O");
    }

    public void setTmc3o(String value) {
        setString("TMC3O", value);
    }

    public String getTmc4o() {
        return getString("TMC4O");
    }

    public void setTmc4o(String value) {
        setString("TMC4O", value);
    }

    public String getTmc5o() {
        return getString("TMC5O");
    }

    public void setTmc5o(String value) {
        setString("TMC5O", value);
    }

    public String getTmc6o() {
        return getString("TMC6O");
    }

    public void setTmc6o(String value) {
        setString("TMC6O", value);
    }

    public String getTmodei() {
        return getString("TMODEI");
    }

    public void setTmodei(String value) {
        setString("TMODEI", value);
    }

    public int getTmodel() {
        return getInt("TMODEL");
    }

    public void setTmodel(int value) {
        setInt("TMODEL", value);
    }

    public String getTmodeo() {
        return getString("TMODEO");
    }

    public void setTmodeo(String value) {
        setString("TMODEO", value);
    }

    public String getTrnnameo() {
        return getString("TRNNAMEO");
    }

    public void setTrnnameo(String value) {
        setString("TRNNAMEO", value);
    }

    public String getTtc1o() {
        return getString("TTC1O");
    }

    public void setTtc1o(String value) {
        setString("TTC1O", value);
    }

    public String getTtc2o() {
        return getString("TTC2O");
    }

    public void setTtc2o(String value) {
        setString("TTC2O", value);
    }

    public String getTtc3o() {
        return getString("TTC3O");
    }

    public void setTtc3o(String value) {
        setString("TTC3O", value);
    }

    public String getTtc4o() {
        return getString("TTC4O");
    }

    public void setTtc4o(String value) {
        setString("TTC4O", value);
    }

    public String getTtc5o() {
        return getString("TTC5O");
    }

    public void setTtc5o(String value) {
        setString("TTC5O", value);
    }

    public String getTtc6o() {
        return getString("TTC6O");
    }

    public void setTtc6o(String value) {
        setString("TTC6O", value);
    }

    public int getWsAbDec() {
        return getInt("WS-AB-DEC");
    }

    public void setWsAbDec(int value) {
        setInt("WS-AB-DEC", value);
    }

    public int getWsAbInt() {
        return getInt("WS-AB-INT");
    }

    public void setWsAbInt(int value) {
        setInt("WS-AB-INT", value);
    }

    public String getWsAmChar() {
        return getString("WS-AM-CHAR");
    }

    public void setWsAmChar(String value) {
        setString("WS-AM-CHAR", value);
    }

    public int getWsAmDec() {
        return getInt("WS-AM-DEC");
    }

    public void setWsAmDec(int value) {
        setInt("WS-AM-DEC", value);
    }

    public int getWsAmDeccnt() {
        return getInt("WS-AM-DECCNT");
    }

    public void setWsAmDeccnt(int value) {
        setInt("WS-AM-DECCNT", value);
    }

    public int getWsAmDigcnt() {
        return getInt("WS-AM-DIGCNT");
    }

    public void setWsAmDigcnt(int value) {
        setInt("WS-AM-DIGCNT", value);
    }

    public int getWsAmDigit() {
        return getInt("WS-AM-DIGIT");
    }

    public void setWsAmDigit(int value) {
        setInt("WS-AM-DIGIT", value);
    }

    public String getWsAmDotSw() {
        return getString("WS-AM-DOT-SW");
    }

    public void setWsAmDotSw(String value) {
        setString("WS-AM-DOT-SW", value);
    }

    public String getWsAmIn() {
        return getString("WS-AM-IN");
    }

    public void setWsAmIn(String value) {
        setString("WS-AM-IN", value);
    }

    public int getWsAmInt() {
        return getInt("WS-AM-INT");
    }

    public void setWsAmInt(int value) {
        setInt("WS-AM-INT", value);
    }

    public BigDecimal getWsAmNum() {
        return getDecimal("WS-AM-NUM");
    }

    public void setWsAmNum(BigDecimal value) {
        setDecimal("WS-AM-NUM", value);
    }

    public int getWsAmPos() {
        return getInt("WS-AM-POS");
    }

    public void setWsAmPos(int value) {
        setInt("WS-AM-POS", value);
    }

    public BigDecimal getWsAmValue() {
        return getDecimal("WS-AM-VALUE");
    }

    public void setWsAmValue(BigDecimal value) {
        setDecimal("WS-AM-VALUE", value);
    }

    public int getWsCntEd() {
        return getInt("WS-CNT-ED");
    }

    public void setWsCntEd(int value) {
        setInt("WS-CNT-ED", value);
    }

    public int getWsCnt7() {
        return getInt("WS-CNT7");
    }

    public void setWsCnt7(int value) {
        setInt("WS-CNT7", value);
    }

    public BigDecimal getWsEdAmt() {
        return getDecimal("WS-ED-AMT");
    }

    public void setWsEdAmt(BigDecimal value) {
        setDecimal("WS-ED-AMT", value);
    }

    public BigDecimal getWsEdSum() {
        return getDecimal("WS-ED-SUM");
    }

    public void setWsEdSum(BigDecimal value) {
        setDecimal("WS-ED-SUM", value);
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

    public String getWsMBadAmt() {
        return getString("WS-M-BAD-AMT");
    }

    public void setWsMBadAmt(String value) {
        setString("WS-M-BAD-AMT", value);
    }

    public String getWsMBadMode() {
        return getString("WS-M-BAD-MODE");
    }

    public void setWsMBadMode(String value) {
        setString("WS-M-BAD-MODE", value);
    }

    public String getWsMBadNum() {
        return getString("WS-M-BAD-NUM");
    }

    public void setWsMBadNum(String value) {
        setString("WS-M-BAD-NUM", value);
    }

    public String getWsMCardReq() {
        return getString("WS-M-CARD-REQ");
    }

    public void setWsMCardReq(String value) {
        setString("WS-M-CARD-REQ", value);
    }

    public String getWsMEndList() {
        return getString("WS-M-END-LIST");
    }

    public void setWsMEndList(String value) {
        setString("WS-M-END-LIST", value);
    }

    public String getWsMLinkErr() {
        return getString("WS-M-LINK-ERR");
    }

    public void setWsMLinkErr(String value) {
        setString("WS-M-LINK-ERR", value);
    }

    public String getWsMMerchReq() {
        return getString("WS-M-MERCH-REQ");
    }

    public void setWsMMerchReq(String value) {
        setString("WS-M-MERCH-REQ", value);
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

    public String getWsMTypeReq() {
        return getString("WS-M-TYPE-REQ");
    }

    public void setWsMTypeReq(String value) {
        setString("WS-M-TYPE-REQ", value);
    }

    public String getWsMenuPgm() {
        return getString("WS-MENU-PGM");
    }

    public void setWsMenuPgm(String value) {
        setString("WS-MENU-PGM", value);
    }

    public String getWsMode() {
        return getString("WS-MODE");
    }

    public void setWsMode(String value) {
        setString("WS-MODE", value);
    }

    public String getWsMsgInvalidKey() {
        return getString("WS-MSG-INVALID-KEY");
    }

    public void setWsMsgInvalidKey(String value) {
        setString("WS-MSG-INVALID-KEY", value);
    }

    public String getWsMsgLine() {
        return getString("WS-MSG-LINE");
    }

    public void setWsMsgLine(String value) {
        setString("WS-MSG-LINE", value);
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

    public int getWsNcValue() {
        return getInt("WS-NC-VALUE");
    }

    public void setWsNcValue(int value) {
        setInt("WS-NC-VALUE", value);
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

    public int getWsRowIdx() {
        return getInt("WS-ROW-IDX");
    }

    public void setWsRowIdx(int value) {
        setInt("WS-ROW-IDX", value);
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

    public String getXamto() {
        return getString("XAMTO");
    }

    public void setXamto(String value) {
        setString("XAMTO", value);
    }

    public String getXido() {
        return getString("XIDO");
    }

    public void setXido(String value) {
        setString("XIDO", value);
    }

    public String getYcnto() {
        return getString("YCNTO");
    }

    public void setYcnto(String value) {
        setString("YCNTO", value);
    }

    public String getYsumo() {
        return getString("YSUMO");
    }

    public void setYsumo(String value) {
        setString("YSUMO", value);
    }
}

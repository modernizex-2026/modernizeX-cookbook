package com.generated.orion.occusta.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.occusta.model.WorkingStorage;

/**
 * Field accessor for OCCUSTA. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OccustaFields extends DynamicFieldAccessor {

    public OccustaFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getCaCustId() {
        return getInt("CA-CUST-ID");
    }

    public void setCaCustId(int value) {
        setInt("CA-CUST-ID", value);
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

    public String getCuAddrCity() {
        return getString("CU-ADDR-CITY");
    }

    public void setCuAddrCity(String value) {
        setString("CU-ADDR-CITY", value);
    }

    public String getCuAddrCountry() {
        return getString("CU-ADDR-COUNTRY");
    }

    public void setCuAddrCountry(String value) {
        setString("CU-ADDR-COUNTRY", value);
    }

    public String getCuAddrLine1() {
        return getString("CU-ADDR-LINE-1");
    }

    public void setCuAddrLine1(String value) {
        setString("CU-ADDR-LINE-1", value);
    }

    public String getCuAddrLine2() {
        return getString("CU-ADDR-LINE-2");
    }

    public void setCuAddrLine2(String value) {
        setString("CU-ADDR-LINE-2", value);
    }

    public String getCuAddrState() {
        return getString("CU-ADDR-STATE");
    }

    public void setCuAddrState(String value) {
        setString("CU-ADDR-STATE", value);
    }

    public String getCuAddrZip() {
        return getString("CU-ADDR-ZIP");
    }

    public void setCuAddrZip(String value) {
        setString("CU-ADDR-ZIP", value);
    }

    public String getCuDob() {
        return getString("CU-DOB");
    }

    public void setCuDob(String value) {
        setString("CU-DOB", value);
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

    public String getCuGovtId() {
        return getString("CU-GOVT-ID");
    }

    public void setCuGovtId(String value) {
        setString("CU-GOVT-ID", value);
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

    public String getCuMiddleName() {
        return getString("CU-MIDDLE-NAME");
    }

    public void setCuMiddleName(String value) {
        setString("CU-MIDDLE-NAME", value);
    }

    public String getCuPhone1() {
        return getString("CU-PHONE-1");
    }

    public void setCuPhone1(String value) {
        setString("CU-PHONE-1", value);
    }

    public String getCuPhone2() {
        return getString("CU-PHONE-2");
    }

    public void setCuPhone2(String value) {
        setString("CU-PHONE-2", value);
    }

    public int getCuSsn() {
        return getInt("CU-SSN");
    }

    public void setCuSsn(int value) {
        setInt("CU-SSN", value);
    }

    public String getCuaddri() {
        return getString("CUADDRI");
    }

    public void setCuaddri(String value) {
        setString("CUADDRI", value);
    }

    public String getCucityi() {
        return getString("CUCITYI");
    }

    public void setCucityi(String value) {
        setString("CUCITYI", value);
    }

    public String getCuficoi() {
        return getString("CUFICOI");
    }

    public void setCuficoi(String value) {
        setString("CUFICOI", value);
    }

    public String getCufnami() {
        return getString("CUFNAMI");
    }

    public void setCufnami(String value) {
        setString("CUFNAMI", value);
    }

    public String getCulnami() {
        return getString("CULNAMI");
    }

    public void setCulnami(String value) {
        setString("CULNAMI", value);
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

    public String getCussni() {
        return getString("CUSSNI");
    }

    public void setCussni(String value) {
        setString("CUSSNI", value);
    }

    public String getCustRec() {
        return groupToString("CUST-REC");
    }

    public void setCustRec(String value) {
        setGroup("CUST-REC", value);
    }

    public String getCustidi() {
        return getString("CUSTIDI");
    }

    public void setCustidi(String value) {
        setString("CUSTIDI", value);
    }

    public int getCustidl() {
        return getInt("CUSTIDL");
    }

    public void setCustidl(int value) {
        setInt("CUSTIDL", value);
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

    public String getWsCustfile() {
        return getString("WS-CUSTFILE");
    }

    public void setWsCustfile(String value) {
        setString("WS-CUSTFILE", value);
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

    public String getWsMAddrReq() {
        return getString("WS-M-ADDR-REQ");
    }

    public void setWsMAddrReq(String value) {
        setString("WS-M-ADDR-REQ", value);
    }

    public String getWsMCityReq() {
        return getString("WS-M-CITY-REQ");
    }

    public void setWsMCityReq(String value) {
        setString("WS-M-CITY-REQ", value);
    }

    public String getWsMCustDup() {
        return getString("WS-M-CUST-DUP");
    }

    public void setWsMCustDup(String value) {
        setString("WS-M-CUST-DUP", value);
    }

    public String getWsMCustNum() {
        return getString("WS-M-CUST-NUM");
    }

    public void setWsMCustNum(String value) {
        setString("WS-M-CUST-NUM", value);
    }

    public String getWsMFicoNum() {
        return getString("WS-M-FICO-NUM");
    }

    public void setWsMFicoNum(String value) {
        setString("WS-M-FICO-NUM", value);
    }

    public String getWsMFicoRng() {
        return getString("WS-M-FICO-RNG");
    }

    public void setWsMFicoRng(String value) {
        setString("WS-M-FICO-RNG", value);
    }

    public String getWsMFnameReq() {
        return getString("WS-M-FNAME-REQ");
    }

    public void setWsMFnameReq(String value) {
        setString("WS-M-FNAME-REQ", value);
    }

    public String getWsMLnameReq() {
        return getString("WS-M-LNAME-REQ");
    }

    public void setWsMLnameReq(String value) {
        setString("WS-M-LNAME-REQ", value);
    }

    public String getWsMOk() {
        return getString("WS-M-OK");
    }

    public void setWsMOk(String value) {
        setString("WS-M-OK", value);
    }

    public String getWsMPrompt() {
        return getString("WS-M-PROMPT");
    }

    public void setWsMPrompt(String value) {
        setString("WS-M-PROMPT", value);
    }

    public String getWsMSsnNum() {
        return getString("WS-M-SSN-NUM");
    }

    public void setWsMSsnNum(String value) {
        setString("WS-M-SSN-NUM", value);
    }

    public String getWsMWriteErr() {
        return getString("WS-M-WRITE-ERR");
    }

    public void setWsMWriteErr(String value) {
        setString("WS-M-WRITE-ERR", value);
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

    public int getWsNewCust() {
        return getInt("WS-NEW-CUST");
    }

    public void setWsNewCust(int value) {
        setInt("WS-NEW-CUST", value);
    }

    public int getWsNewFico() {
        return getInt("WS-NEW-FICO");
    }

    public void setWsNewFico(int value) {
        setInt("WS-NEW-FICO", value);
    }

    public int getWsNewSsn() {
        return getInt("WS-NEW-SSN");
    }

    public void setWsNewSsn(int value) {
        setInt("WS-NEW-SSN", value);
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

    public String getWsWriteSw() {
        return getString("WS-WRITE-SW");
    }

    public void setWsWriteSw(String value) {
        setString("WS-WRITE-SW", value);
    }
}

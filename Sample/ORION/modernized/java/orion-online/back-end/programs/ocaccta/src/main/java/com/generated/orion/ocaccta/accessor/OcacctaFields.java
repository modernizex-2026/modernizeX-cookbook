package com.generated.orion.ocaccta.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ocaccta.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OCACCTA. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OcacctaFields extends DynamicFieldAccessor {

    public OcacctaFields(WorkingStorage ws) {
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

    public String getAcAddrZip() {
        return getString("AC-ADDR-ZIP");
    }

    public void setAcAddrZip(String value) {
        setString("AC-ADDR-ZIP", value);
    }

    public BigDecimal getAcCashLimit() {
        return getDecimal("AC-CASH-LIMIT");
    }

    public void setAcCashLimit(BigDecimal value) {
        setDecimal("AC-CASH-LIMIT", value);
    }

    public BigDecimal getAcCreditLimit() {
        return getDecimal("AC-CREDIT-LIMIT");
    }

    public void setAcCreditLimit(BigDecimal value) {
        setDecimal("AC-CREDIT-LIMIT", value);
    }

    public BigDecimal getAcCurrBal() {
        return getDecimal("AC-CURR-BAL");
    }

    public void setAcCurrBal(BigDecimal value) {
        setDecimal("AC-CURR-BAL", value);
    }

    public BigDecimal getAcCycCredit() {
        return getDecimal("AC-CYC-CREDIT");
    }

    public void setAcCycCredit(BigDecimal value) {
        setDecimal("AC-CYC-CREDIT", value);
    }

    public BigDecimal getAcCycDebit() {
        return getDecimal("AC-CYC-DEBIT");
    }

    public void setAcCycDebit(BigDecimal value) {
        setDecimal("AC-CYC-DEBIT", value);
    }

    public String getAcExpiryDate() {
        return getString("AC-EXPIRY-DATE");
    }

    public void setAcExpiryDate(String value) {
        setString("AC-EXPIRY-DATE", value);
    }

    public String getAcGroupId() {
        return getString("AC-GROUP-ID");
    }

    public void setAcGroupId(String value) {
        setString("AC-GROUP-ID", value);
    }

    public long getAcId() {
        return getLong("AC-ID");
    }

    public void setAcId(long value) {
        setLong("AC-ID", value);
    }

    public String getAcOpenDate() {
        return getString("AC-OPEN-DATE");
    }

    public void setAcOpenDate(String value) {
        setString("AC-OPEN-DATE", value);
    }

    public String getAcReissueDate() {
        return getString("AC-REISSUE-DATE");
    }

    public void setAcReissueDate(String value) {
        setString("AC-REISSUE-DATE", value);
    }

    public String getAccrlimi() {
        return getString("ACCRLIMI");
    }

    public void setAccrlimi(String value) {
        setString("ACCRLIMI", value);
    }

    public String getAccslimi() {
        return getString("ACCSLIMI");
    }

    public void setAccslimi(String value) {
        setString("ACCSLIMI", value);
    }

    public String getAcctRec() {
        return groupToString("ACCT-REC");
    }

    public void setAcctRec(String value) {
        setGroup("ACCT-REC", value);
    }

    public String getAcctidi() {
        return getString("ACCTIDI");
    }

    public void setAcctidi(String value) {
        setString("ACCTIDI", value);
    }

    public int getAcctidl() {
        return getInt("ACCTIDL");
    }

    public void setAcctidl(int value) {
        setInt("ACCTIDL", value);
    }

    public String getAcgrpi() {
        return getString("ACGRPI");
    }

    public void setAcgrpi(String value) {
        setString("ACGRPI", value);
    }

    public String getAcopeni() {
        return getString("ACOPENI");
    }

    public void setAcopeni(String value) {
        setString("ACOPENI", value);
    }

    public long getCaAcctId() {
        return getLong("CA-ACCT-ID");
    }

    public void setCaAcctId(long value) {
        setLong("CA-ACCT-ID", value);
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

    public int getCuId() {
        return getInt("CU-ID");
    }

    public void setCuId(int value) {
        setInt("CU-ID", value);
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

    public String getCustidi() {
        return getString("CUSTIDI");
    }

    public void setCustidi(String value) {
        setString("CUSTIDI", value);
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

    public String getWsAeChar() {
        return getString("WS-AE-CHAR");
    }

    public void setWsAeChar(String value) {
        setString("WS-AE-CHAR", value);
    }

    public int getWsAeDigit() {
        return getInt("WS-AE-DIGIT");
    }

    public void setWsAeDigit(int value) {
        setInt("WS-AE-DIGIT", value);
    }

    public String getWsAeDotSw() {
        return getString("WS-AE-DOT-SW");
    }

    public void setWsAeDotSw(String value) {
        setString("WS-AE-DOT-SW", value);
    }

    public int getWsAeFrac() {
        return getInt("WS-AE-FRAC");
    }

    public void setWsAeFrac(int value) {
        setInt("WS-AE-FRAC", value);
    }

    public int getWsAeFracCnt() {
        return getInt("WS-AE-FRAC-CNT");
    }

    public void setWsAeFracCnt(int value) {
        setInt("WS-AE-FRAC-CNT", value);
    }

    public String getWsAeIn() {
        return getString("WS-AE-IN");
    }

    public void setWsAeIn(String value) {
        setString("WS-AE-IN", value);
    }

    public long getWsAeInt() {
        return getLong("WS-AE-INT");
    }

    public void setWsAeInt(long value) {
        setLong("WS-AE-INT", value);
    }

    public int getWsAeIntCnt() {
        return getInt("WS-AE-INT-CNT");
    }

    public void setWsAeIntCnt(int value) {
        setInt("WS-AE-INT-CNT", value);
    }

    public int getWsAePos() {
        return getInt("WS-AE-POS");
    }

    public void setWsAePos(int value) {
        setInt("WS-AE-POS", value);
    }

    public BigDecimal getWsAeResult() {
        return getDecimal("WS-AE-RESULT");
    }

    public void setWsAeResult(BigDecimal value) {
        setDecimal("WS-AE-RESULT", value);
    }

    public String getWsCustfile() {
        return getString("WS-CUSTFILE");
    }

    public void setWsCustfile(String value) {
        setString("WS-CUSTFILE", value);
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

    public String getWsMAcctDup() {
        return getString("WS-M-ACCT-DUP");
    }

    public void setWsMAcctDup(String value) {
        setString("WS-M-ACCT-DUP", value);
    }

    public String getWsMAcctNum() {
        return getString("WS-M-ACCT-NUM");
    }

    public void setWsMAcctNum(String value) {
        setString("WS-M-ACCT-NUM", value);
    }

    public String getWsMCrlimBad() {
        return getString("WS-M-CRLIM-BAD");
    }

    public void setWsMCrlimBad(String value) {
        setString("WS-M-CRLIM-BAD", value);
    }

    public String getWsMCsGtCr() {
        return getString("WS-M-CS-GT-CR");
    }

    public void setWsMCsGtCr(String value) {
        setString("WS-M-CS-GT-CR", value);
    }

    public String getWsMCslimBad() {
        return getString("WS-M-CSLIM-BAD");
    }

    public void setWsMCslimBad(String value) {
        setString("WS-M-CSLIM-BAD", value);
    }

    public String getWsMCustNf() {
        return getString("WS-M-CUST-NF");
    }

    public void setWsMCustNf(String value) {
        setString("WS-M-CUST-NF", value);
    }

    public String getWsMCustNum() {
        return getString("WS-M-CUST-NUM");
    }

    public void setWsMCustNum(String value) {
        setString("WS-M-CUST-NUM", value);
    }

    public String getWsMGroupReq() {
        return getString("WS-M-GROUP-REQ");
    }

    public void setWsMGroupReq(String value) {
        setString("WS-M-GROUP-REQ", value);
    }

    public String getWsMOk() {
        return getString("WS-M-OK");
    }

    public void setWsMOk(String value) {
        setString("WS-M-OK", value);
    }

    public String getWsMOpenBad() {
        return getString("WS-M-OPEN-BAD");
    }

    public void setWsMOpenBad(String value) {
        setString("WS-M-OPEN-BAD", value);
    }

    public String getWsMPrompt() {
        return getString("WS-M-PROMPT");
    }

    public void setWsMPrompt(String value) {
        setString("WS-M-PROMPT", value);
    }

    public String getWsMWriteErr() {
        return getString("WS-M-WRITE-ERR");
    }

    public void setWsMWriteErr(String value) {
        setString("WS-M-WRITE-ERR", value);
    }

    public String getWsMXrefErr() {
        return getString("WS-M-XREF-ERR");
    }

    public void setWsMXrefErr(String value) {
        setString("WS-M-XREF-ERR", value);
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

    public long getWsNewAcct() {
        return getLong("WS-NEW-ACCT");
    }

    public void setWsNewAcct(long value) {
        setLong("WS-NEW-ACCT", value);
    }

    public BigDecimal getWsNewCrlim() {
        return getDecimal("WS-NEW-CRLIM");
    }

    public void setWsNewCrlim(BigDecimal value) {
        setDecimal("WS-NEW-CRLIM", value);
    }

    public BigDecimal getWsNewCslim() {
        return getDecimal("WS-NEW-CSLIM");
    }

    public void setWsNewCslim(BigDecimal value) {
        setDecimal("WS-NEW-CSLIM", value);
    }

    public int getWsNewCust() {
        return getInt("WS-NEW-CUST");
    }

    public void setWsNewCust(int value) {
        setInt("WS-NEW-CUST", value);
    }

    public String getWsNewGroup() {
        return getString("WS-NEW-GROUP");
    }

    public void setWsNewGroup(String value) {
        setString("WS-NEW-GROUP", value);
    }

    public String getWsNewOpen() {
        return getString("WS-NEW-OPEN");
    }

    public void setWsNewOpen(String value) {
        setString("WS-NEW-OPEN", value);
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

    public long getWsXrefKey() {
        return getLong("WS-XREF-KEY");
    }

    public void setWsXrefKey(long value) {
        setLong("WS-XREF-KEY", value);
    }

    public String getWsXreffile() {
        return getString("WS-XREFFILE");
    }

    public void setWsXreffile(String value) {
        setString("WS-XREFFILE", value);
    }

    public long getXrAcctId() {
        return getLong("XR-ACCT-ID");
    }

    public void setXrAcctId(long value) {
        setLong("XR-ACCT-ID", value);
    }

    public String getXrCardNum() {
        return getString("XR-CARD-NUM");
    }

    public void setXrCardNum(String value) {
        setString("XR-CARD-NUM", value);
    }

    public int getXrCustId() {
        return getInt("XR-CUST-ID");
    }

    public void setXrCustId(int value) {
        setInt("XR-CUST-ID", value);
    }

    public String getXrefRec() {
        return groupToString("XREF-REC");
    }

    public void setXrefRec(String value) {
        setGroup("XREF-REC", value);
    }
}

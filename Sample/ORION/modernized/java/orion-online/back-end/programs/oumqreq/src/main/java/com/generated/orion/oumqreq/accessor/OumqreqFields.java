package com.generated.orion.oumqreq.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.oumqreq.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OUMQREQ. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OumqreqFields extends DynamicFieldAccessor {

    public OumqreqFields(WorkingStorage ws) {
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

    public long getAcId() {
        return getLong("AC-ID");
    }

    public void setAcId(long value) {
        setLong("AC-ID", value);
    }

    public long getAqAcctId() {
        return getLong("AQ-ACCT-ID");
    }

    public void setAqAcctId(long value) {
        setLong("AQ-ACCT-ID", value);
    }

    public BigDecimal getAqAmount() {
        return getDecimal("AQ-AMOUNT");
    }

    public void setAqAmount(BigDecimal value) {
        setDecimal("AQ-AMOUNT", value);
    }

    public String getAqCardNum() {
        return getString("AQ-CARD-NUM");
    }

    public void setAqCardNum(String value) {
        setString("AQ-CARD-NUM", value);
    }

    public String getAqMsgType() {
        return getString("AQ-MSG-TYPE");
    }

    public void setAqMsgType(String value) {
        setString("AQ-MSG-TYPE", value);
    }

    public long getAsAcctId() {
        return getLong("AS-ACCT-ID");
    }

    public void setAsAcctId(long value) {
        setLong("AS-ACCT-ID", value);
    }

    public BigDecimal getAsApprovedAmt() {
        return getDecimal("AS-APPROVED-AMT");
    }

    public void setAsApprovedAmt(BigDecimal value) {
        setDecimal("AS-APPROVED-AMT", value);
    }

    public BigDecimal getAsAvailCredit() {
        return getDecimal("AS-AVAIL-CREDIT");
    }

    public void setAsAvailCredit(BigDecimal value) {
        setDecimal("AS-AVAIL-CREDIT", value);
    }

    public String getAsCardNum() {
        return getString("AS-CARD-NUM");
    }

    public void setAsCardNum(String value) {
        setString("AS-CARD-NUM", value);
    }

    public String getAsDecision() {
        return getString("AS-DECISION");
    }

    public void setAsDecision(String value) {
        setString("AS-DECISION", value);
    }

    public String getAsMsgType() {
        return getString("AS-MSG-TYPE");
    }

    public void setAsMsgType(String value) {
        setString("AS-MSG-TYPE", value);
    }

    public String getAsReason() {
        return getString("AS-REASON");
    }

    public void setAsReason(String value) {
        setString("AS-REASON", value);
    }

    public String getAuthResponse() {
        return groupToString("AUTH-RESPONSE");
    }

    public void setAuthResponse(String value) {
        setGroup("AUTH-RESPONSE", value);
    }

    public String getCaErrMsg() {
        return getString("CA-ERR-MSG");
    }

    public void setCaErrMsg(String value) {
        setString("CA-ERR-MSG", value);
    }

    public int getMqBufferLen() {
        return getInt("MQ-BUFFER-LEN");
    }

    public void setMqBufferLen(int value) {
        setInt("MQ-BUFFER-LEN", value);
    }

    public int getMqCc() {
        return getInt("MQ-CC");
    }

    public void setMqCc(int value) {
        setInt("MQ-CC", value);
    }

    public int getMqClNone() {
        return getInt("MQ-CL-NONE");
    }

    public void setMqClNone(int value) {
        setInt("MQ-CL-NONE", value);
    }

    public int getMqCloseOptions() {
        return getInt("MQ-CLOSE-OPTIONS");
    }

    public void setMqCloseOptions(int value) {
        setInt("MQ-CLOSE-OPTIONS", value);
    }

    public int getMqDataLen() {
        return getInt("MQ-DATA-LEN");
    }

    public void setMqDataLen(int value) {
        setInt("MQ-DATA-LEN", value);
    }

    public int getMqGmNoSyncpoint() {
        return getInt("MQ-GM-NO-SYNCPOINT");
    }

    public void setMqGmNoSyncpoint(int value) {
        setInt("MQ-GM-NO-SYNCPOINT", value);
    }

    public int getMqGmWait() {
        return getInt("MQ-GM-WAIT");
    }

    public void setMqGmWait(int value) {
        setInt("MQ-GM-WAIT", value);
    }

    public int getMqGmoOptions() {
        return getInt("MQ-GMO-OPTIONS");
    }

    public void setMqGmoOptions(int value) {
        setInt("MQ-GMO-OPTIONS", value);
    }

    public int getMqHcDefHconn() {
        return getInt("MQ-HC-DEF-HCONN");
    }

    public void setMqHcDefHconn(int value) {
        setInt("MQ-HC-DEF-HCONN", value);
    }

    public int getMqHconn() {
        return getInt("MQ-HCONN");
    }

    public void setMqHconn(int value) {
        setInt("MQ-HCONN", value);
    }

    public int getMqHobjOut() {
        return getInt("MQ-HOBJ-OUT");
    }

    public void setMqHobjOut(int value) {
        setInt("MQ-HOBJ-OUT", value);
    }

    public int getMqHobjRpy() {
        return getInt("MQ-HOBJ-RPY");
    }

    public void setMqHobjRpy(int value) {
        setInt("MQ-HOBJ-RPY", value);
    }

    public String getMqMdFormat() {
        return getString("MQ-MD-FORMAT");
    }

    public void setMqMdFormat(String value) {
        setString("MQ-MD-FORMAT", value);
    }

    public int getMqMdMsgType() {
        return getInt("MQ-MD-MSG-TYPE");
    }

    public void setMqMdMsgType(int value) {
        setInt("MQ-MD-MSG-TYPE", value);
    }

    public String getMqMdReplyQ() {
        return getString("MQ-MD-REPLY-Q");
    }

    public void setMqMdReplyQ(String value) {
        setString("MQ-MD-REPLY-Q", value);
    }

    public int getMqMtRequest() {
        return getInt("MQ-MT-REQUEST");
    }

    public void setMqMtRequest(int value) {
        setInt("MQ-MT-REQUEST", value);
    }

    public String getMqOdObjectName() {
        return getString("MQ-OD-OBJECT-NAME");
    }

    public void setMqOdObjectName(String value) {
        setString("MQ-OD-OBJECT-NAME", value);
    }

    public int getMqOoFailIfQsg() {
        return getInt("MQ-OO-FAIL-IF-QSG");
    }

    public void setMqOoFailIfQsg(int value) {
        setInt("MQ-OO-FAIL-IF-QSG", value);
    }

    public int getMqOoInputShared() {
        return getInt("MQ-OO-INPUT-SHARED");
    }

    public void setMqOoInputShared(int value) {
        setInt("MQ-OO-INPUT-SHARED", value);
    }

    public int getMqOoOutput() {
        return getInt("MQ-OO-OUTPUT");
    }

    public void setMqOoOutput(int value) {
        setInt("MQ-OO-OUTPUT", value);
    }

    public int getMqOpenOptions() {
        return getInt("MQ-OPEN-OPTIONS");
    }

    public void setMqOpenOptions(int value) {
        setInt("MQ-OPEN-OPTIONS", value);
    }

    public int getMqPmNoSyncpoint() {
        return getInt("MQ-PM-NO-SYNCPOINT");
    }

    public void setMqPmNoSyncpoint(int value) {
        setInt("MQ-PM-NO-SYNCPOINT", value);
    }

    public int getMqPmoOptions() {
        return getInt("MQ-PMO-OPTIONS");
    }

    public void setMqPmoOptions(int value) {
        setInt("MQ-PMO-OPTIONS", value);
    }

    public int getMqRc() {
        return getInt("MQ-RC");
    }

    public void setMqRc(int value) {
        setInt("MQ-RC", value);
    }

    public String getOrionCommarea() {
        return groupToString("ORION-COMMAREA");
    }

    public void setOrionCommarea(String value) {
        setGroup("ORION-COMMAREA", value);
    }

    public String getWsAcctfile() {
        return getString("WS-ACCTFILE");
    }

    public void setWsAcctfile(String value) {
        setString("WS-ACCTFILE", value);
    }

    public BigDecimal getWsAmt() {
        return getDecimal("WS-AMT");
    }

    public void setWsAmt(BigDecimal value) {
        setDecimal("WS-AMT", value);
    }

    public String getWsFoundFlg() {
        return getString("WS-FOUND-FLG");
    }

    public void setWsFoundFlg(String value) {
        setString("WS-FOUND-FLG", value);
    }

    public String getWsIoFlg() {
        return getString("WS-IO-FLG");
    }

    public void setWsIoFlg(String value) {
        setString("WS-IO-FLG", value);
    }

    public String getWsMqDownFlg() {
        return getString("WS-MQ-DOWN-FLG");
    }

    public void setWsMqDownFlg(String value) {
        setString("WS-MQ-DOWN-FLG", value);
    }

    public String getWsMqOp() {
        return getString("WS-MQ-OP");
    }

    public void setWsMqOp(String value) {
        setString("WS-MQ-OP", value);
    }

    public String getWsReplyBuf() {
        return getString("WS-REPLY-BUF");
    }

    public void setWsReplyBuf(String value) {
        setString("WS-REPLY-BUF", value);
    }

    public String getWsReqQueue() {
        return getString("WS-REQ-QUEUE");
    }

    public void setWsReqQueue(String value) {
        setString("WS-REQ-QUEUE", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public String getWsRpyQueue() {
        return getString("WS-RPY-QUEUE");
    }

    public void setWsRpyQueue(String value) {
        setString("WS-RPY-QUEUE", value);
    }

    public String getWsStDate() {
        return getString("WS-ST-DATE");
    }

    public void setWsStDate(String value) {
        setString("WS-ST-DATE", value);
    }

    public String getWsStTime() {
        return getString("WS-ST-TIME");
    }

    public void setWsStTime(String value) {
        setString("WS-ST-TIME", value);
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
}

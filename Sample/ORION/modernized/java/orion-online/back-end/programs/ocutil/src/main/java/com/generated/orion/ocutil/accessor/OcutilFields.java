package com.generated.orion.ocutil.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ocutil.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OCUTIL. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OcutilFields extends DynamicFieldAccessor {

    public OcutilFields(WorkingStorage ws) {
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

    public BigDecimal getKarArchAmt() {
        return getDecimal("KAR-ARCH-AMT");
    }

    public void setKarArchAmt(BigDecimal value) {
        setDecimal("KAR-ARCH-AMT", value);
    }

    public int getKarArchived() {
        return getInt("KAR-ARCHIVED");
    }

    public void setKarArchived(int value) {
        setInt("KAR-ARCHIVED", value);
    }

    public String getKarCutoff() {
        return getString("KAR-CUTOFF");
    }

    public void setKarCutoff(String value) {
        setString("KAR-CUTOFF", value);
    }

    public int getKarDeleted() {
        return getInt("KAR-DELETED");
    }

    public void setKarDeleted(int value) {
        setInt("KAR-DELETED", value);
    }

    public int getKarErrors() {
        return getInt("KAR-ERRORS");
    }

    public void setKarErrors(int value) {
        setInt("KAR-ERRORS", value);
    }

    public int getKarKept() {
        return getInt("KAR-KEPT");
    }

    public void setKarKept(int value) {
        setInt("KAR-KEPT", value);
    }

    public int getKarMax() {
        return getInt("KAR-MAX");
    }

    public void setKarMax(int value) {
        setInt("KAR-MAX", value);
    }

    public String getKarMore() {
        return getString("KAR-MORE");
    }

    public void setKarMore(String value) {
        setString("KAR-MORE", value);
    }

    public String getKarMsg() {
        return getString("KAR-MSG");
    }

    public void setKarMsg(String value) {
        setString("KAR-MSG", value);
    }

    public String getKarParm() {
        return groupToString("KAR-PARM");
    }

    public void setKarParm(String value) {
        setGroup("KAR-PARM", value);
    }

    public int getKarRead() {
        return getInt("KAR-READ");
    }

    public void setKarRead(int value) {
        setInt("KAR-READ", value);
    }

    public String getKarStartTran() {
        return getString("KAR-START-TRAN");
    }

    public void setKarStartTran(String value) {
        setString("KAR-START-TRAN", value);
    }

    public String getKarStatus() {
        return getString("KAR-STATUS");
    }

    public void setKarStatus(String value) {
        setString("KAR-STATUS", value);
    }

    public BigDecimal getKbkCreditAmt() {
        return getDecimal("KBK-CREDIT-AMT");
    }

    public void setKbkCreditAmt(BigDecimal value) {
        setDecimal("KBK-CREDIT-AMT", value);
    }

    public BigDecimal getKbkDebitAmt() {
        return getDecimal("KBK-DEBIT-AMT");
    }

    public void setKbkDebitAmt(BigDecimal value) {
        setDecimal("KBK-DEBIT-AMT", value);
    }

    public int getKbkErrors() {
        return getInt("KBK-ERRORS");
    }

    public void setKbkErrors(int value) {
        setInt("KBK-ERRORS", value);
    }

    public int getKbkMax() {
        return getInt("KBK-MAX");
    }

    public void setKbkMax(int value) {
        setInt("KBK-MAX", value);
    }

    public String getKbkMore() {
        return getString("KBK-MORE");
    }

    public void setKbkMore(String value) {
        setString("KBK-MORE", value);
    }

    public String getKbkMsg() {
        return getString("KBK-MSG");
    }

    public void setKbkMsg(String value) {
        setString("KBK-MSG", value);
    }

    public String getKbkParm() {
        return groupToString("KBK-PARM");
    }

    public void setKbkParm(String value) {
        setGroup("KBK-PARM", value);
    }

    public int getKbkRead() {
        return getInt("KBK-READ");
    }

    public void setKbkRead(int value) {
        setInt("KBK-READ", value);
    }

    public String getKbkStartKey() {
        return getString("KBK-START-KEY");
    }

    public void setKbkStartKey(String value) {
        setString("KBK-START-KEY", value);
    }

    public String getKbkStatus() {
        return getString("KBK-STATUS");
    }

    public void setKbkStatus(String value) {
        setString("KBK-STATUS", value);
    }

    public BigDecimal getKbkTotAmt() {
        return getDecimal("KBK-TOT-AMT");
    }

    public void setKbkTotAmt(BigDecimal value) {
        setDecimal("KBK-TOT-AMT", value);
    }

    public int getKbkWritten() {
        return getInt("KBK-WRITTEN");
    }

    public void setKbkWritten(int value) {
        setInt("KBK-WRITTEN", value);
    }

    public int getKflB30() {
        return getInt("KFL-B30");
    }

    public void setKflB30(int value) {
        setInt("KFL-B30", value);
    }

    public int getKflB60() {
        return getInt("KFL-B60");
    }

    public void setKflB60(int value) {
        setInt("KFL-B60", value);
    }

    public int getKflB90() {
        return getInt("KFL-B90");
    }

    public void setKflB90(int value) {
        setInt("KFL-B90", value);
    }

    public String getKflCutoff() {
        return getString("KFL-CUTOFF");
    }

    public void setKflCutoff(String value) {
        setString("KFL-CUTOFF", value);
    }

    public int getKflDelq() {
        return getInt("KFL-DELQ");
    }

    public void setKflDelq(int value) {
        setInt("KFL-DELQ", value);
    }

    public int getKflErrors() {
        return getInt("KFL-ERRORS");
    }

    public void setKflErrors(int value) {
        setInt("KFL-ERRORS", value);
    }

    public int getKflExpired() {
        return getInt("KFL-EXPIRED");
    }

    public void setKflExpired(int value) {
        setInt("KFL-EXPIRED", value);
    }

    public int getKflMax() {
        return getInt("KFL-MAX");
    }

    public void setKflMax(int value) {
        setInt("KFL-MAX", value);
    }

    public String getKflMode() {
        return getString("KFL-MODE");
    }

    public void setKflMode(String value) {
        setString("KFL-MODE", value);
    }

    public String getKflMore() {
        return getString("KFL-MORE");
    }

    public void setKflMore(String value) {
        setString("KFL-MORE", value);
    }

    public String getKflMsg() {
        return getString("KFL-MSG");
    }

    public void setKflMsg(String value) {
        setString("KFL-MSG", value);
    }

    public String getKflParm() {
        return groupToString("KFL-PARM");
    }

    public void setKflParm(String value) {
        setGroup("KFL-PARM", value);
    }

    public int getKflRead() {
        return getInt("KFL-READ");
    }

    public void setKflRead(int value) {
        setInt("KFL-READ", value);
    }

    public long getKflStartAcct() {
        return getLong("KFL-START-ACCT");
    }

    public void setKflStartAcct(long value) {
        setLong("KFL-START-ACCT", value);
    }

    public String getKflStatus() {
        return getString("KFL-STATUS");
    }

    public void setKflStatus(String value) {
        setString("KFL-STATUS", value);
    }

    public int getKimAccepted() {
        return getInt("KIM-ACCEPTED");
    }

    public void setKimAccepted(int value) {
        setInt("KIM-ACCEPTED", value);
    }

    public int getKimAdded() {
        return getInt("KIM-ADDED");
    }

    public void setKimAdded(int value) {
        setInt("KIM-ADDED", value);
    }

    public int getKimErrors() {
        return getInt("KIM-ERRORS");
    }

    public void setKimErrors(int value) {
        setInt("KIM-ERRORS", value);
    }

    public int getKimMax() {
        return getInt("KIM-MAX");
    }

    public void setKimMax(int value) {
        setInt("KIM-MAX", value);
    }

    public String getKimMore() {
        return getString("KIM-MORE");
    }

    public void setKimMore(String value) {
        setString("KIM-MORE", value);
    }

    public String getKimMsg() {
        return getString("KIM-MSG");
    }

    public void setKimMsg(String value) {
        setString("KIM-MSG", value);
    }

    public String getKimParm() {
        return groupToString("KIM-PARM");
    }

    public void setKimParm(String value) {
        setGroup("KIM-PARM", value);
    }

    public int getKimRead() {
        return getInt("KIM-READ");
    }

    public void setKimRead(int value) {
        setInt("KIM-READ", value);
    }

    public int getKimRejected() {
        return getInt("KIM-REJECTED");
    }

    public void setKimRejected(int value) {
        setInt("KIM-REJECTED", value);
    }

    public int getKimSkipped() {
        return getInt("KIM-SKIPPED");
    }

    public void setKimSkipped(int value) {
        setInt("KIM-SKIPPED", value);
    }

    public String getKimStartKey() {
        return getString("KIM-START-KEY");
    }

    public void setKimStartKey(String value) {
        setString("KIM-START-KEY", value);
    }

    public String getKimStatus() {
        return getString("KIM-STATUS");
    }

    public void setKimStatus(String value) {
        setString("KIM-STATUS", value);
    }

    public int getKimUpdated() {
        return getInt("KIM-UPDATED");
    }

    public void setKimUpdated(int value) {
        setInt("KIM-UPDATED", value);
    }

    public String getKpgCutoff() {
        return getString("KPG-CUTOFF");
    }

    public void setKpgCutoff(String value) {
        setString("KPG-CUTOFF", value);
    }

    public int getKpgErrors() {
        return getInt("KPG-ERRORS");
    }

    public void setKpgErrors(int value) {
        setInt("KPG-ERRORS", value);
    }

    public BigDecimal getKpgKeepAmt() {
        return getDecimal("KPG-KEEP-AMT");
    }

    public void setKpgKeepAmt(BigDecimal value) {
        setDecimal("KPG-KEEP-AMT", value);
    }

    public int getKpgKept() {
        return getInt("KPG-KEPT");
    }

    public void setKpgKept(int value) {
        setInt("KPG-KEPT", value);
    }

    public int getKpgMax() {
        return getInt("KPG-MAX");
    }

    public void setKpgMax(int value) {
        setInt("KPG-MAX", value);
    }

    public String getKpgMore() {
        return getString("KPG-MORE");
    }

    public void setKpgMore(String value) {
        setString("KPG-MORE", value);
    }

    public String getKpgMsg() {
        return getString("KPG-MSG");
    }

    public void setKpgMsg(String value) {
        setString("KPG-MSG", value);
    }

    public String getKpgParm() {
        return groupToString("KPG-PARM");
    }

    public void setKpgParm(String value) {
        setGroup("KPG-PARM", value);
    }

    public BigDecimal getKpgPurgeAmt() {
        return getDecimal("KPG-PURGE-AMT");
    }

    public void setKpgPurgeAmt(BigDecimal value) {
        setDecimal("KPG-PURGE-AMT", value);
    }

    public int getKpgPurged() {
        return getInt("KPG-PURGED");
    }

    public void setKpgPurged(int value) {
        setInt("KPG-PURGED", value);
    }

    public int getKpgRead() {
        return getInt("KPG-READ");
    }

    public void setKpgRead(int value) {
        setInt("KPG-READ", value);
    }

    public String getKpgStartTran() {
        return getString("KPG-START-TRAN");
    }

    public void setKpgStartTran(String value) {
        setString("KPG-START-TRAN", value);
    }

    public String getKpgStatus() {
        return getString("KPG-STATUS");
    }

    public void setKpgStatus(String value) {
        setString("KPG-STATUS", value);
    }

    public int getKsmAcctRead() {
        return getInt("KSM-ACCT-READ");
    }

    public void setKsmAcctRead(int value) {
        setInt("KSM-ACCT-READ", value);
    }

    public int getKsmCycle() {
        return getInt("KSM-CYCLE");
    }

    public void setKsmCycle(int value) {
        setInt("KSM-CYCLE", value);
    }

    public String getKsmDueDate() {
        return getString("KSM-DUE-DATE");
    }

    public void setKsmDueDate(String value) {
        setString("KSM-DUE-DATE", value);
    }

    public int getKsmErrors() {
        return getInt("KSM-ERRORS");
    }

    public void setKsmErrors(int value) {
        setInt("KSM-ERRORS", value);
    }

    public int getKsmMax() {
        return getInt("KSM-MAX");
    }

    public void setKsmMax(int value) {
        setInt("KSM-MAX", value);
    }

    public String getKsmMore() {
        return getString("KSM-MORE");
    }

    public void setKsmMore(String value) {
        setString("KSM-MORE", value);
    }

    public String getKsmMsg() {
        return getString("KSM-MSG");
    }

    public void setKsmMsg(String value) {
        setString("KSM-MSG", value);
    }

    public long getKsmNextAcct() {
        return getLong("KSM-NEXT-ACCT");
    }

    public void setKsmNextAcct(long value) {
        setLong("KSM-NEXT-ACCT", value);
    }

    public int getKsmNoTran() {
        return getInt("KSM-NO-TRAN");
    }

    public void setKsmNoTran(int value) {
        setInt("KSM-NO-TRAN", value);
    }

    public String getKsmParm() {
        return groupToString("KSM-PARM");
    }

    public void setKsmParm(String value) {
        setGroup("KSM-PARM", value);
    }

    public long getKsmStartAcct() {
        return getLong("KSM-START-ACCT");
    }

    public void setKsmStartAcct(long value) {
        setLong("KSM-START-ACCT", value);
    }

    public String getKsmStatus() {
        return getString("KSM-STATUS");
    }

    public void setKsmStatus(String value) {
        setString("KSM-STATUS", value);
    }

    public int getKsmStmtWritten() {
        return getInt("KSM-STMT-WRITTEN");
    }

    public void setKsmStmtWritten(int value) {
        setInt("KSM-STMT-WRITTEN", value);
    }

    public BigDecimal getKsmTotCredit() {
        return getDecimal("KSM-TOT-CREDIT");
    }

    public void setKsmTotCredit(BigDecimal value) {
        setDecimal("KSM-TOT-CREDIT", value);
    }

    public BigDecimal getKsmTotDebit() {
        return getDecimal("KSM-TOT-DEBIT");
    }

    public void setKsmTotDebit(BigDecimal value) {
        setDecimal("KSM-TOT-DEBIT", value);
    }

    public int getKuxErrors() {
        return getInt("KUX-ERRORS");
    }

    public void setKuxErrors(int value) {
        setInt("KUX-ERRORS", value);
    }

    public int getKuxMax() {
        return getInt("KUX-MAX");
    }

    public void setKuxMax(int value) {
        setInt("KUX-MAX", value);
    }

    public String getKuxMode() {
        return getString("KUX-MODE");
    }

    public void setKuxMode(String value) {
        setString("KUX-MODE", value);
    }

    public String getKuxMore() {
        return getString("KUX-MORE");
    }

    public void setKuxMore(String value) {
        setString("KUX-MORE", value);
    }

    public String getKuxMsg() {
        return getString("KUX-MSG");
    }

    public void setKuxMsg(String value) {
        setString("KUX-MSG", value);
    }

    public String getKuxParm() {
        return groupToString("KUX-PARM");
    }

    public void setKuxParm(String value) {
        setGroup("KUX-PARM", value);
    }

    public int getKuxRead() {
        return getInt("KUX-READ");
    }

    public void setKuxRead(int value) {
        setInt("KUX-READ", value);
    }

    public int getKuxSkipAcct() {
        return getInt("KUX-SKIP-ACCT");
    }

    public void setKuxSkipAcct(int value) {
        setInt("KUX-SKIP-ACCT", value);
    }

    public int getKuxSkipCust() {
        return getInt("KUX-SKIP-CUST");
    }

    public void setKuxSkipCust(int value) {
        setInt("KUX-SKIP-CUST", value);
    }

    public int getKuxSkipXref() {
        return getInt("KUX-SKIP-XREF");
    }

    public void setKuxSkipXref(int value) {
        setInt("KUX-SKIP-XREF", value);
    }

    public String getKuxStartCard() {
        return getString("KUX-START-CARD");
    }

    public void setKuxStartCard(String value) {
        setString("KUX-START-CARD", value);
    }

    public String getKuxStatus() {
        return getString("KUX-STATUS");
    }

    public void setKuxStatus(String value) {
        setString("KUX-STATUS", value);
    }

    public int getKuxUpdated() {
        return getInt("KUX-UPDATED");
    }

    public void setKuxUpdated(int value) {
        setInt("KUX-UPDATED", value);
    }

    public int getKuxWritten() {
        return getInt("KUX-WRITTEN");
    }

    public void setKuxWritten(int value) {
        setInt("KUX-WRITTEN", value);
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

    public String getRline1o() {
        return getString("RLINE1O");
    }

    public void setRline1o(String value) {
        setString("RLINE1O", value);
    }

    public String getRline2o() {
        return getString("RLINE2O");
    }

    public void setRline2o(String value) {
        setString("RLINE2O", value);
    }

    public String getRline3o() {
        return getString("RLINE3O");
    }

    public void setRline3o(String value) {
        setString("RLINE3O", value);
    }

    public String getRline4o() {
        return getString("RLINE4O");
    }

    public void setRline4o(String value) {
        setString("RLINE4O", value);
    }

    public String getRmsgo() {
        return getString("RMSGO");
    }

    public void setRmsgo(String value) {
        setString("RMSGO", value);
    }

    public String getRstato() {
        return getString("RSTATO");
    }

    public void setRstato(String value) {
        setString("RSTATO", value);
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

    public String getUtcyci() {
        return getString("UTCYCI");
    }

    public void setUtcyci(String value) {
        setString("UTCYCI", value);
    }

    public String getUtdatei() {
        return getString("UTDATEI");
    }

    public void setUtdatei(String value) {
        setString("UTDATEI", value);
    }

    public String getUtmodei() {
        return getString("UTMODEI");
    }

    public void setUtmodei(String value) {
        setString("UTMODEI", value);
    }

    public String getUtmodeo() {
        return getString("UTMODEO");
    }

    public void setUtmodeo(String value) {
        setString("UTMODEO", value);
    }

    public String getUtnumi() {
        return getString("UTNUMI");
    }

    public void setUtnumi(String value) {
        setString("UTNUMI", value);
    }

    public String getUtopti() {
        return getString("UTOPTI");
    }

    public void setUtopti(String value) {
        setString("UTOPTI", value);
    }

    public int getWsCycNum() {
        return getInt("WS-CYC-NUM");
    }

    public void setWsCycNum(int value) {
        setInt("WS-CYC-NUM", value);
    }

    public int getWsE9() {
        return getInt("WS-E9");
    }

    public void setWsE9(int value) {
        setInt("WS-E9", value);
    }

    public BigDecimal getWsEa() {
        return getDecimal("WS-EA");
    }

    public void setWsEa(BigDecimal value) {
        setDecimal("WS-EA", value);
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

    public String getWsL1() {
        return getString("WS-L1");
    }

    public void setWsL1(String value) {
        setString("WS-L1", value);
    }

    public String getWsL2() {
        return getString("WS-L2");
    }

    public void setWsL2(String value) {
        setString("WS-L2", value);
    }

    public String getWsLine() {
        return getString("WS-LINE");
    }

    public void setWsLine(String value) {
        setString("WS-LINE", value);
    }

    public String getWsLinkBad() {
        return getString("WS-LINK-BAD");
    }

    public void setWsLinkBad(String value) {
        setString("WS-LINK-BAD", value);
    }

    public String getWsLinkPgm() {
        return getString("WS-LINK-PGM");
    }

    public void setWsLinkPgm(String value) {
        setString("WS-LINK-PGM", value);
    }

    public int getWsMaxNum() {
        return getInt("WS-MAX-NUM");
    }

    public void setWsMaxNum(int value) {
        setInt("WS-MAX-NUM", value);
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

    public int getWsOption() {
        return getInt("WS-OPTION");
    }

    public void setWsOption(int value) {
        setInt("WS-OPTION", value);
    }

    public String getWsPArch() {
        return getString("WS-P-ARCH");
    }

    public void setWsPArch(String value) {
        setString("WS-P-ARCH", value);
    }

    public String getWsPBkp() {
        return getString("WS-P-BKP");
    }

    public void setWsPBkp(String value) {
        setString("WS-P-BKP", value);
    }

    public String getWsPFlag() {
        return getString("WS-P-FLAG");
    }

    public void setWsPFlag(String value) {
        setString("WS-P-FLAG", value);
    }

    public String getWsPImp() {
        return getString("WS-P-IMP");
    }

    public void setWsPImp(String value) {
        setString("WS-P-IMP", value);
    }

    public String getWsPPurg() {
        return getString("WS-P-PURG");
    }

    public void setWsPPurg(String value) {
        setString("WS-P-PURG", value);
    }

    public String getWsPStmb() {
        return getString("WS-P-STMB");
    }

    public void setWsPStmb(String value) {
        setString("WS-P-STMB", value);
    }

    public String getWsPXref() {
        return getString("WS-P-XREF");
    }

    public void setWsPXref(String value) {
        setString("WS-P-XREF", value);
    }

    public String getWsPgmname() {
        return getString("WS-PGMNAME");
    }

    public void setWsPgmname(String value) {
        setString("WS-PGMNAME", value);
    }

    public String getWsReqDate() {
        return getString("WS-REQ-DATE");
    }

    public void setWsReqDate(String value) {
        setString("WS-REQ-DATE", value);
    }

    public String getWsReqMode() {
        return getString("WS-REQ-MODE");
    }

    public void setWsReqMode(String value) {
        setString("WS-REQ-MODE", value);
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

    public String getWsV1() {
        return getString("WS-V1");
    }

    public void setWsV1(String value) {
        setString("WS-V1", value);
    }

    public String getWsV2() {
        return getString("WS-V2");
    }

    public void setWsV2(String value) {
        setString("WS-V2", value);
    }

    public String getWsValidSw() {
        return getString("WS-VALID-SW");
    }

    public void setWsValidSw(String value) {
        setString("WS-VALID-SW", value);
    }
}

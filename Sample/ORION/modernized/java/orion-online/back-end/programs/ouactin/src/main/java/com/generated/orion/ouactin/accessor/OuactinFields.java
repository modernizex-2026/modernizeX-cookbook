package com.generated.orion.ouactin.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ouactin.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OUACTIN. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OuactinFields extends DynamicFieldAccessor {

    public OuactinFields(WorkingStorage ws) {
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

    public String getKactinArea() {
        return groupToString("KACTIN-AREA");
    }

    public void setKactinArea(String value) {
        setGroup("KACTIN-AREA", value);
    }

    public String getKaiFilter() {
        return getString("KAI-FILTER");
    }

    public void setKaiFilter(String value) {
        setString("KAI-FILTER", value);
    }

    public long getKaiNextKey() {
        return getLong("KAI-NEXT-KEY");
    }

    public void setKaiNextKey(long value) {
        setLong("KAI-NEXT-KEY", value);
    }

    public BigDecimal getKaiPopAvlTot() {
        return getDecimal("KAI-POP-AVL-TOT");
    }

    public void setKaiPopAvlTot(BigDecimal value) {
        setDecimal("KAI-POP-AVL-TOT", value);
    }

    public BigDecimal getKaiPopBalTot() {
        return getDecimal("KAI-POP-BAL-TOT");
    }

    public void setKaiPopBalTot(BigDecimal value) {
        setDecimal("KAI-POP-BAL-TOT", value);
    }

    public int getKaiPopCount() {
        return getInt("KAI-POP-COUNT");
    }

    public void setKaiPopCount(int value) {
        setInt("KAI-POP-COUNT", value);
    }

    public BigDecimal getKaiRAvail(int index) {
        return getDecimal("KAI-R-AVAIL", index);
    }

    public void setKaiRAvail(int index, BigDecimal value) {
        setDecimal("KAI-R-AVAIL", value, index);
    }

    public BigDecimal getKaiRBal(int index) {
        return getDecimal("KAI-R-BAL", index);
    }

    public void setKaiRBal(int index, BigDecimal value) {
        setDecimal("KAI-R-BAL", value, index);
    }

    public long getKaiRId(int index) {
        return getLong("KAI-R-ID", index);
    }

    public void setKaiRId(int index, long value) {
        setLong("KAI-R-ID", value, index);
    }

    public BigDecimal getKaiRLimit(int index) {
        return getDecimal("KAI-R-LIMIT", index);
    }

    public void setKaiRLimit(int index, BigDecimal value) {
        setDecimal("KAI-R-LIMIT", value, index);
    }

    public String getKaiRStatus(int index) {
        return getString("KAI-R-STATUS", index);
    }

    public void setKaiRStatus(int index, String value) {
        setString("KAI-R-STATUS", value, index);
    }

    public BigDecimal getKaiRUtil(int index) {
        return getDecimal("KAI-R-UTIL", index);
    }

    public void setKaiRUtil(int index, BigDecimal value) {
        setDecimal("KAI-R-UTIL", value, index);
    }

    public String getKaiReturnCd() {
        return getString("KAI-RETURN-CD");
    }

    public void setKaiReturnCd(String value) {
        setString("KAI-RETURN-CD", value);
    }

    public int getKaiRowCount() {
        return getInt("KAI-ROW-COUNT");
    }

    public void setKaiRowCount(int value) {
        setInt("KAI-ROW-COUNT", value);
    }

    public int getKaiScanCount() {
        return getInt("KAI-SCAN-COUNT");
    }

    public void setKaiScanCount(int value) {
        setInt("KAI-SCAN-COUNT", value);
    }

    public long getKaiStartKey() {
        return getLong("KAI-START-KEY");
    }

    public void setKaiStartKey(long value) {
        setLong("KAI-START-KEY", value);
    }

    public String getKaiWantKpi() {
        return getString("KAI-WANT-KPI");
    }

    public void setKaiWantKpi(String value) {
        setString("KAI-WANT-KPI", value);
    }

    public int getWoDd() {
        return getInt("WO-DD");
    }

    public void setWoDd(int value) {
        setInt("WO-DD", value);
    }

    public int getWoMm() {
        return getInt("WO-MM");
    }

    public void setWoMm(int value) {
        setInt("WO-MM", value);
    }

    public int getWoYyyy() {
        return getInt("WO-YYYY");
    }

    public void setWoYyyy(int value) {
        setInt("WO-YYYY", value);
    }

    public String getWsAcctfile() {
        return getString("WS-ACCTFILE");
    }

    public void setWsAcctfile(String value) {
        setString("WS-ACCTFILE", value);
    }

    public BigDecimal getWsAvail() {
        return getDecimal("WS-AVAIL");
    }

    public void setWsAvail(BigDecimal value) {
        setDecimal("WS-AVAIL", value);
    }

    public String getWsBrEndSw() {
        return getString("WS-BR-END-SW");
    }

    public void setWsBrEndSw(String value) {
        setString("WS-BR-END-SW", value);
    }

    public String getWsBrStartedSw() {
        return getString("WS-BR-STARTED-SW");
    }

    public void setWsBrStartedSw(String value) {
        setString("WS-BR-STARTED-SW", value);
    }

    public String getWsCdt() {
        return getString("WS-CDT");
    }

    public void setWsCdt(String value) {
        setString("WS-CDT", value);
    }

    public int getWsCutInt() {
        return getInt("WS-CUT-INT");
    }

    public void setWsCutInt(int value) {
        setInt("WS-CUT-INT", value);
    }

    public BigDecimal getWsKpiAvl() {
        return getDecimal("WS-KPI-AVL");
    }

    public void setWsKpiAvl(BigDecimal value) {
        setDecimal("WS-KPI-AVL", value);
    }

    public BigDecimal getWsKpiBal() {
        return getDecimal("WS-KPI-BAL");
    }

    public void setWsKpiBal(BigDecimal value) {
        setDecimal("WS-KPI-BAL", value);
    }

    public int getWsKpiCount() {
        return getInt("WS-KPI-COUNT");
    }

    public void setWsKpiCount(int value) {
        setInt("WS-KPI-COUNT", value);
    }

    public long getWsLastMatch() {
        return getLong("WS-LAST-MATCH");
    }

    public void setWsLastMatch(long value) {
        setLong("WS-LAST-MATCH", value);
    }

    public String getWsMatchSw() {
        return getString("WS-MATCH-SW");
    }

    public void setWsMatchSw(String value) {
        setString("WS-MATCH-SW", value);
    }

    public int getWsMaxRows() {
        return getInt("WS-MAX-ROWS");
    }

    public void setWsMaxRows(int value) {
        setInt("WS-MAX-ROWS", value);
    }

    public BigDecimal getWsMinDue() {
        return getDecimal("WS-MIN-DUE");
    }

    public void setWsMinDue(BigDecimal value) {
        setDecimal("WS-MIN-DUE", value);
    }

    public BigDecimal getWsMinFloor() {
        return getDecimal("WS-MIN-FLOOR");
    }

    public void setWsMinFloor(BigDecimal value) {
        setDecimal("WS-MIN-FLOOR", value);
    }

    public int getWsOpenInt() {
        return getInt("WS-OPEN-INT");
    }

    public void setWsOpenInt(int value) {
        setInt("WS-OPEN-INT", value);
    }

    public int getWsOpenN() {
        return getInt("WS-OPEN-N");
    }

    public void setWsOpenN(int value) {
        setInt("WS-OPEN-N", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public int getWsTodayInt() {
        return getInt("WS-TODAY-INT");
    }

    public void setWsTodayInt(int value) {
        setInt("WS-TODAY-INT", value);
    }

    public int getWsTodayN() {
        return getInt("WS-TODAY-N");
    }

    public void setWsTodayN(int value) {
        setInt("WS-TODAY-N", value);
    }

    public BigDecimal getWsUtilBig() {
        return getDecimal("WS-UTIL-BIG");
    }

    public void setWsUtilBig(BigDecimal value) {
        setDecimal("WS-UTIL-BIG", value);
    }

    public BigDecimal getWsUtilr() {
        return getDecimal("WS-UTILR");
    }

    public void setWsUtilr(BigDecimal value) {
        setDecimal("WS-UTILR", value);
    }
}

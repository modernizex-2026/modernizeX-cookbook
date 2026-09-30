package com.generated.orion.oufee.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.oufee.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OUFEE. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OufeeFields extends DynamicFieldAccessor {

    public OufeeFields(WorkingStorage ws) {
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

    public String getCtDesc() {
        return getString("CT-DESC");
    }

    public void setCtDesc(String value) {
        setString("CT-DESC", value);
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

    public BigDecimal getKoAmt1() {
        return getDecimal("KO-AMT-1");
    }

    public void setKoAmt1(BigDecimal value) {
        setDecimal("KO-AMT-1", value);
    }

    public BigDecimal getKoAmt2() {
        return getDecimal("KO-AMT-2");
    }

    public void setKoAmt2(BigDecimal value) {
        setDecimal("KO-AMT-2", value);
    }

    public BigDecimal getKoAmt3() {
        return getDecimal("KO-AMT-3");
    }

    public void setKoAmt3(BigDecimal value) {
        setDecimal("KO-AMT-3", value);
    }

    public int getKoC1() {
        return getInt("KO-C1");
    }

    public void setKoC1(int value) {
        setInt("KO-C1", value);
    }

    public int getKoC2() {
        return getInt("KO-C2");
    }

    public void setKoC2(int value) {
        setInt("KO-C2", value);
    }

    public int getKoC3() {
        return getInt("KO-C3");
    }

    public void setKoC3(int value) {
        setInt("KO-C3", value);
    }

    public long getKoParmAcct() {
        return getLong("KO-PARM-ACCT");
    }

    public void setKoParmAcct(long value) {
        setLong("KO-PARM-ACCT", value);
    }

    public int getKoPostedCnt() {
        return getInt("KO-POSTED-CNT");
    }

    public void setKoPostedCnt(int value) {
        setInt("KO-POSTED-CNT", value);
    }

    public int getKoReadCnt() {
        return getInt("KO-READ-CNT");
    }

    public void setKoReadCnt(int value) {
        setInt("KO-READ-CNT", value);
    }

    public int getKoRejectCnt() {
        return getInt("KO-REJECT-CNT");
    }

    public void setKoRejectCnt(int value) {
        setInt("KO-REJECT-CNT", value);
    }

    public int getKoSelectCnt() {
        return getInt("KO-SELECT-CNT");
    }

    public void setKoSelectCnt(int value) {
        setInt("KO-SELECT-CNT", value);
    }

    public int getKoSkipCnt() {
        return getInt("KO-SKIP-CNT");
    }

    public void setKoSkipCnt(int value) {
        setInt("KO-SKIP-CNT", value);
    }

    public String getKoStatus() {
        return getString("KO-STATUS");
    }

    public void setKoStatus(String value) {
        setString("KO-STATUS", value);
    }

    public String getKoStatusMsg() {
        return getString("KO-STATUS-MSG");
    }

    public void setKoStatusMsg(String value) {
        setString("KO-STATUS-MSG", value);
    }

    public int getKoTranCnt() {
        return getInt("KO-TRAN-CNT");
    }

    public void setKoTranCnt(int value) {
        setInt("KO-TRAN-CNT", value);
    }

    public int getKoUpdateCnt() {
        return getInt("KO-UPDATE-CNT");
    }

    public void setKoUpdateCnt(int value) {
        setInt("KO-UPDATE-CNT", value);
    }

    public String getOrionCommarea() {
        return groupToString("ORION-COMMAREA");
    }

    public void setOrionCommarea(String value) {
        setGroup("ORION-COMMAREA", value);
    }

    public int getPtDay() {
        return getInt("PT-DAY");
    }

    public void setPtDay(int value) {
        setInt("PT-DAY", value);
    }

    public int getPtHh() {
        return getInt("PT-HH");
    }

    public void setPtHh(int value) {
        setInt("PT-HH", value);
    }

    public int getPtMm() {
        return getInt("PT-MM");
    }

    public void setPtMm(int value) {
        setInt("PT-MM", value);
    }

    public int getPtMon() {
        return getInt("PT-MON");
    }

    public void setPtMon(int value) {
        setInt("PT-MON", value);
    }

    public int getPtSs() {
        return getInt("PT-SS");
    }

    public void setPtSs(int value) {
        setInt("PT-SS", value);
    }

    public int getPtYear() {
        return getInt("PT-YEAR");
    }

    public void setPtYear(int value) {
        setInt("PT-YEAR", value);
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

    public String getTranRec() {
        return groupToString("TRAN-REC");
    }

    public void setTranRec(String value) {
        setGroup("TRAN-REC", value);
    }

    public String getWsAcctfile() {
        return getString("WS-ACCTFILE");
    }

    public void setWsAcctfile(String value) {
        setString("WS-ACCTFILE", value);
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
        return groupToString("WS-CDT");
    }

    public void setWsCdt(String value) {
        setGroup("WS-CDT", value);
    }

    public int getWsCdtDay() {
        return getInt("WS-CDT-DAY");
    }

    public void setWsCdtDay(int value) {
        setInt("WS-CDT-DAY", value);
    }

    public int getWsCdtHh() {
        return getInt("WS-CDT-HH");
    }

    public void setWsCdtHh(int value) {
        setInt("WS-CDT-HH", value);
    }

    public int getWsCdtMm() {
        return getInt("WS-CDT-MM");
    }

    public void setWsCdtMm(int value) {
        setInt("WS-CDT-MM", value);
    }

    public int getWsCdtMon() {
        return getInt("WS-CDT-MON");
    }

    public void setWsCdtMon(int value) {
        setInt("WS-CDT-MON", value);
    }

    public int getWsCdtSs() {
        return getInt("WS-CDT-SS");
    }

    public void setWsCdtSs(int value) {
        setInt("WS-CDT-SS", value);
    }

    public int getWsCdtYear() {
        return getInt("WS-CDT-YEAR");
    }

    public void setWsCdtYear(int value) {
        setInt("WS-CDT-YEAR", value);
    }

    public String getWsCtrlKeyId() {
        return getString("WS-CTRL-KEY-ID");
    }

    public void setWsCtrlKeyId(String value) {
        setString("WS-CTRL-KEY-ID", value);
    }

    public String getWsCtrlfile() {
        return getString("WS-CTRLFILE");
    }

    public void setWsCtrlfile(String value) {
        setString("WS-CTRLFILE", value);
    }

    public String getWsCurrRaw() {
        return getString("WS-CURR-RAW");
    }

    public void setWsCurrRaw(String value) {
        setString("WS-CURR-RAW", value);
    }

    public String getWsDelqSw() {
        return getString("WS-DELQ-SW");
    }

    public void setWsDelqSw(String value) {
        setString("WS-DELQ-SW", value);
    }

    public String getWsEligSw() {
        return getString("WS-ELIG-SW");
    }

    public void setWsEligSw(String value) {
        setString("WS-ELIG-SW", value);
    }

    public BigDecimal getWsFeeAmt() {
        return getDecimal("WS-FEE-AMT");
    }

    public void setWsFeeAmt(BigDecimal value) {
        setDecimal("WS-FEE-AMT", value);
    }

    public int getWsFeeCat() {
        return getInt("WS-FEE-CAT");
    }

    public void setWsFeeCat(int value) {
        setInt("WS-FEE-CAT", value);
    }

    public String getWsFeeType() {
        return getString("WS-FEE-TYPE");
    }

    public void setWsFeeType(String value) {
        setString("WS-FEE-TYPE", value);
    }

    public long getWsFilterAcct() {
        return getLong("WS-FILTER-ACCT");
    }

    public void setWsFilterAcct(long value) {
        setLong("WS-FILTER-ACCT", value);
    }

    public String getWsFilterOn() {
        return getString("WS-FILTER-ON");
    }

    public void setWsFilterOn(String value) {
        setString("WS-FILTER-ON", value);
    }

    public String getWsIbPrefix() {
        return getString("WS-IB-PREFIX");
    }

    public void setWsIbPrefix(String value) {
        setString("WS-IB-PREFIX", value);
    }

    public long getWsIbSeq() {
        return getLong("WS-IB-SEQ");
    }

    public void setWsIbSeq(long value) {
        setLong("WS-IB-SEQ", value);
    }

    public String getWsIdPrefix() {
        return getString("WS-ID-PREFIX");
    }

    public void setWsIdPrefix(String value) {
        setString("WS-ID-PREFIX", value);
    }

    public BigDecimal getWsLateFee() {
        return getDecimal("WS-LATE-FEE");
    }

    public void setWsLateFee(BigDecimal value) {
        setDecimal("WS-LATE-FEE", value);
    }

    public BigDecimal getWsMinDue() {
        return getDecimal("WS-MIN-DUE");
    }

    public void setWsMinDue(BigDecimal value) {
        setDecimal("WS-MIN-DUE", value);
    }

    public BigDecimal getWsMinDueFloor() {
        return getDecimal("WS-MIN-DUE-FLOOR");
    }

    public void setWsMinDueFloor(BigDecimal value) {
        setDecimal("WS-MIN-DUE-FLOOR", value);
    }

    public BigDecimal getWsMinDuePct() {
        return getDecimal("WS-MIN-DUE-PCT");
    }

    public void setWsMinDuePct(BigDecimal value) {
        setDecimal("WS-MIN-DUE-PCT", value);
    }

    public long getWsNextIdNum() {
        return getLong("WS-NEXT-ID-NUM");
    }

    public void setWsNextIdNum(long value) {
        setLong("WS-NEXT-ID-NUM", value);
    }

    public BigDecimal getWsOvlimFee() {
        return getDecimal("WS-OVLIM-FEE");
    }

    public void setWsOvlimFee(BigDecimal value) {
        setDecimal("WS-OVLIM-FEE", value);
    }

    public String getWsOvlimSw() {
        return getString("WS-OVLIM-SW");
    }

    public void setWsOvlimSw(String value) {
        setString("WS-OVLIM-SW", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public String getWsTranfile() {
        return getString("WS-TRANFILE");
    }

    public void setWsTranfile(String value) {
        setString("WS-TRANFILE", value);
    }
}

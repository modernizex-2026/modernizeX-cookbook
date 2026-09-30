package com.generated.orion.ouanlin.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.ouanlin.model.WorkingStorage;

import java.math.BigDecimal;

/**
 * Field accessor for OUANLIN. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OuanlinFields extends DynamicFieldAccessor {

    public OuanlinFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
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

    public int getKabDisc() {
        return getInt("KAB-DISC");
    }

    public void setKabDisc(int value) {
        setInt("KAB-DISC", value);
    }

    public int getKabMaxRows() {
        return getInt("KAB-MAX-ROWS");
    }

    public void setKabMaxRows(int value) {
        setInt("KAB-MAX-ROWS", value);
    }

    public String getKabMode() {
        return getString("KAB-MODE");
    }

    public void setKabMode(String value) {
        setString("KAB-MODE", value);
    }

    public String getKabMore() {
        return getString("KAB-MORE");
    }

    public void setKabMore(String value) {
        setString("KAB-MORE", value);
    }

    public BigDecimal getKabRAmt(int index) {
        return getDecimal("KAB-R-AMT", index);
    }

    public void setKabRAmt(int index, BigDecimal value) {
        setDecimal("KAB-R-AMT", value, index);
    }

    public int getKabRCnt(int index) {
        return getInt("KAB-R-CNT", index);
    }

    public void setKabRCnt(int index, int value) {
        setInt("KAB-R-CNT", value, index);
    }

    public String getKabRInfo(int index) {
        return getString("KAB-R-INFO", index);
    }

    public void setKabRInfo(int index, String value) {
        setString("KAB-R-INFO", value, index);
    }

    public String getKabRKey(int index) {
        return getString("KAB-R-KEY", index);
    }

    public void setKabRKey(int index, String value) {
        setString("KAB-R-KEY", value, index);
    }

    public BigDecimal getKabRVal(int index) {
        return getDecimal("KAB-R-VAL", index);
    }

    public void setKabRVal(int index, BigDecimal value) {
        setDecimal("KAB-R-VAL", value, index);
    }

    public int getKabResultCnt() {
        return getInt("KAB-RESULT-CNT");
    }

    public void setKabResultCnt(int value) {
        setInt("KAB-RESULT-CNT", value);
    }

    public int getKabRowCnt() {
        return getInt("KAB-ROW-CNT");
    }

    public void setKabRowCnt(int value) {
        setInt("KAB-ROW-CNT", value);
    }

    public int getKabScanned() {
        return getInt("KAB-SCANNED");
    }

    public void setKabScanned(int value) {
        setInt("KAB-SCANNED", value);
    }

    public int getKabStartOff() {
        return getInt("KAB-START-OFF");
    }

    public void setKabStartOff(int value) {
        setInt("KAB-START-OFF", value);
    }

    public String getKabStatus() {
        return getString("KAB-STATUS");
    }

    public void setKabStatus(String value) {
        setString("KAB-STATUS", value);
    }

    public BigDecimal getKabTot1() {
        return getDecimal("KAB-TOT-1");
    }

    public void setKabTot1(BigDecimal value) {
        setDecimal("KAB-TOT-1", value);
    }

    public BigDecimal getKabTot2() {
        return getDecimal("KAB-TOT-2");
    }

    public void setKabTot2(BigDecimal value) {
        setDecimal("KAB-TOT-2", value);
    }

    public String getKanlbParm() {
        return groupToString("KANLB-PARM");
    }

    public void setKanlbParm(String value) {
        setGroup("KANLB-PARM", value);
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

    public String getTrId() {
        return getString("TR-ID");
    }

    public void setTrId(String value) {
        setString("TR-ID", value);
    }

    public String getTrTypeCd() {
        return getString("TR-TYPE-CD");
    }

    public void setTrTypeCd(String value) {
        setString("TR-TYPE-CD", value);
    }

    public String getWsAcctfile() {
        return getString("WS-ACCTFILE");
    }

    public void setWsAcctfile(String value) {
        setString("WS-ACCTFILE", value);
    }

    public int getWsBaseRate() {
        return getInt("WS-BASE-RATE");
    }

    public void setWsBaseRate(int value) {
        setInt("WS-BASE-RATE", value);
    }

    public int getWsBonusRate() {
        return getInt("WS-BONUS-RATE");
    }

    public void setWsBonusRate(int value) {
        setInt("WS-BONUS-RATE", value);
    }

    public String getWsBrowseSw() {
        return getString("WS-BROWSE-SW");
    }

    public void setWsBrowseSw(String value) {
        setString("WS-BROWSE-SW", value);
    }

    public BigDecimal getWsDiff() {
        return getDecimal("WS-DIFF");
    }

    public void setWsDiff(BigDecimal value) {
        setDecimal("WS-DIFF", value);
    }

    public int getWsDiscCnt() {
        return getInt("WS-DISC-CNT");
    }

    public void setWsDiscCnt(int value) {
        setInt("WS-DISC-CNT", value);
    }

    public String getWsEofSw() {
        return getString("WS-EOF-SW");
    }

    public void setWsEofSw(String value) {
        setString("WS-EOF-SW", value);
    }

    public int getWsFidx() {
        return getInt("WS-FIDX");
    }

    public void setWsFidx(int value) {
        setInt("WS-FIDX", value);
    }

    public String getWsFlagSw() {
        return getString("WS-FLAG-SW");
    }

    public void setWsFlagSw(String value) {
        setString("WS-FLAG-SW", value);
    }

    public int getWsIdx() {
        return getInt("WS-IDX");
    }

    public void setWsIdx(int value) {
        setInt("WS-IDX", value);
    }

    public String getWsKey() {
        return getString("WS-KEY");
    }

    public void setWsKey(String value) {
        setString("WS-KEY", value);
    }

    public BigDecimal getWsLargeThresh() {
        return getDecimal("WS-LARGE-THRESH");
    }

    public void setWsLargeThresh(BigDecimal value) {
        setDecimal("WS-LARGE-THRESH", value);
    }

    public int getWsMaxRes() {
        return getInt("WS-MAX-RES");
    }

    public void setWsMaxRes(int value) {
        setInt("WS-MAX-RES", value);
    }

    public int getWsMaxRows() {
        return getInt("WS-MAX-ROWS");
    }

    public void setWsMaxRows(int value) {
        setInt("WS-MAX-ROWS", value);
    }

    public int getWsMaxScan() {
        return getInt("WS-MAX-SCAN");
    }

    public void setWsMaxScan(int value) {
        setInt("WS-MAX-SCAN", value);
    }

    public BigDecimal getWsMove() {
        return getDecimal("WS-MOVE");
    }

    public void setWsMove(BigDecimal value) {
        setDecimal("WS-MOVE", value);
    }

    public int getWsNewCnt() {
        return getInt("WS-NEW-CNT");
    }

    public void setWsNewCnt(int value) {
        setInt("WS-NEW-CNT", value);
    }

    public int getWsOflowCnt() {
        return getInt("WS-OFLOW-CNT");
    }

    public void setWsOflowCnt(int value) {
        setInt("WS-OFLOW-CNT", value);
    }

    public int getWsOrphCnt() {
        return getInt("WS-ORPH-CNT");
    }

    public void setWsOrphCnt(int value) {
        setInt("WS-ORPH-CNT", value);
    }

    public int getWsOutCnt() {
        return getInt("WS-OUT-CNT");
    }

    public void setWsOutCnt(int value) {
        setInt("WS-OUT-CNT", value);
    }

    public long getWsPoints() {
        return getLong("WS-POINTS");
    }

    public void setWsPoints(long value) {
        setLong("WS-POINTS", value);
    }

    public String getWsPurchaseSw() {
        return getString("WS-PURCHASE-SW");
    }

    public void setWsPurchaseSw(String value) {
        setString("WS-PURCHASE-SW", value);
    }

    public BigDecimal getWsReAmt(int index) {
        return getDecimal("WS-RE-AMT", index);
    }

    public void setWsReAmt(int index, BigDecimal value) {
        setDecimal("WS-RE-AMT", value, index);
    }

    public int getWsReCnt(int index) {
        return getInt("WS-RE-CNT", index);
    }

    public void setWsReCnt(int index, int value) {
        setInt("WS-RE-CNT", value, index);
    }

    public String getWsReInfo(int index) {
        return getString("WS-RE-INFO", index);
    }

    public void setWsReInfo(int index, String value) {
        setString("WS-RE-INFO", value, index);
    }

    public String getWsReKey(int index) {
        return getString("WS-RE-KEY", index);
    }

    public void setWsReKey(int index, String value) {
        setString("WS-RE-KEY", value, index);
    }

    public BigDecimal getWsReVal(int index) {
        return getDecimal("WS-RE-VAL", index);
    }

    public void setWsReVal(int index, BigDecimal value) {
        setDecimal("WS-RE-VAL", value, index);
    }

    public String getWsReason() {
        return getString("WS-REASON");
    }

    public void setWsReason(String value) {
        setString("WS-REASON", value);
    }

    public int getWsResCnt() {
        return getInt("WS-RES-CNT");
    }

    public void setWsResCnt(int value) {
        setInt("WS-RES-CNT", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public int getWsScanned() {
        return getInt("WS-SCANNED");
    }

    public void setWsScanned(int value) {
        setInt("WS-SCANNED", value);
    }

    public int getWsSidx() {
        return getInt("WS-SIDX");
    }

    public void setWsSidx(int value) {
        setInt("WS-SIDX", value);
    }

    public String getWsSlotSw() {
        return getString("WS-SLOT-SW");
    }

    public void setWsSlotSw(String value) {
        setString("WS-SLOT-SW", value);
    }

    public int getWsStartIdx() {
        return getInt("WS-START-IDX");
    }

    public void setWsStartIdx(int value) {
        setInt("WS-START-IDX", value);
    }

    public BigDecimal getWsTot1() {
        return getDecimal("WS-TOT-1");
    }

    public void setWsTot1(BigDecimal value) {
        setDecimal("WS-TOT-1", value);
    }

    public BigDecimal getWsTot2() {
        return getDecimal("WS-TOT-2");
    }

    public void setWsTot2(BigDecimal value) {
        setDecimal("WS-TOT-2", value);
    }

    public String getWsTranfile() {
        return getString("WS-TRANFILE");
    }

    public void setWsTranfile(String value) {
        setString("WS-TRANFILE", value);
    }

    public int getWsVeloThresh() {
        return getInt("WS-VELO-THRESH");
    }

    public void setWsVeloThresh(int value) {
        setInt("WS-VELO-THRESH", value);
    }

    public long getWsWholeAmt() {
        return getLong("WS-WHOLE-AMT");
    }

    public void setWsWholeAmt(long value) {
        setLong("WS-WHOLE-AMT", value);
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

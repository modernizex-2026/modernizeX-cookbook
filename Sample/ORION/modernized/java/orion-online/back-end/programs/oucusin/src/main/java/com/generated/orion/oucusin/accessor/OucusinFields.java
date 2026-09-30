package com.generated.orion.oucusin.accessor;

import com.generated.orion.common.infrastructure.layout.DynamicFieldAccessor;
import com.generated.orion.oucusin.model.WorkingStorage;

/**
 * Field accessor for OUCUSIN. Backend: DynamicFieldAccessor (raw RecordBuffer byte[]). Typed
 * methods auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OucusinFields extends DynamicFieldAccessor {

    public OucusinFields(WorkingStorage ws) {
        if (ws != null) {
            register(ws.buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
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

    public String getKcusinArea() {
        return groupToString("KCUSIN-AREA");
    }

    public void setKcusinArea(String value) {
        setGroup("KCUSIN-AREA", value);
    }

    public int getKuiFicoAvg() {
        return getInt("KUI-FICO-AVG");
    }

    public void setKuiFicoAvg(int value) {
        setInt("KUI-FICO-AVG", value);
    }

    public int getKuiFicoFrom() {
        return getInt("KUI-FICO-FROM");
    }

    public void setKuiFicoFrom(int value) {
        setInt("KUI-FICO-FROM", value);
    }

    public int getKuiFicoMax() {
        return getInt("KUI-FICO-MAX");
    }

    public void setKuiFicoMax(int value) {
        setInt("KUI-FICO-MAX", value);
    }

    public int getKuiFicoMin() {
        return getInt("KUI-FICO-MIN");
    }

    public void setKuiFicoMin(int value) {
        setInt("KUI-FICO-MIN", value);
    }

    public int getKuiFicoTo() {
        return getInt("KUI-FICO-TO");
    }

    public void setKuiFicoTo(int value) {
        setInt("KUI-FICO-TO", value);
    }

    public long getKuiFicoTot() {
        return getLong("KUI-FICO-TOT");
    }

    public void setKuiFicoTot(long value) {
        setLong("KUI-FICO-TOT", value);
    }

    public String getKuiFilter() {
        return getString("KUI-FILTER");
    }

    public void setKuiFilter(String value) {
        setString("KUI-FILTER", value);
    }

    public int getKuiIdFrom() {
        return getInt("KUI-ID-FROM");
    }

    public void setKuiIdFrom(int value) {
        setInt("KUI-ID-FROM", value);
    }

    public int getKuiIdTo() {
        return getInt("KUI-ID-TO");
    }

    public void setKuiIdTo(int value) {
        setInt("KUI-ID-TO", value);
    }

    public int getKuiMatchCount() {
        return getInt("KUI-MATCH-COUNT");
    }

    public void setKuiMatchCount(int value) {
        setInt("KUI-MATCH-COUNT", value);
    }

    public int getKuiNextKey() {
        return getInt("KUI-NEXT-KEY");
    }

    public void setKuiNextKey(int value) {
        setInt("KUI-NEXT-KEY", value);
    }

    public int getKuiRowCount() {
        return getInt("KUI-ROW-COUNT");
    }

    public void setKuiRowCount(int value) {
        setInt("KUI-ROW-COUNT", value);
    }

    public String getKuiRows() {
        return groupToString("KUI-ROWS");
    }

    public void setKuiRows(String value) {
        setGroup("KUI-ROWS", value);
    }

    public int getKuiScanCount() {
        return getInt("KUI-SCAN-COUNT");
    }

    public void setKuiScanCount(int value) {
        setInt("KUI-SCAN-COUNT", value);
    }

    public int getKuiStartKey() {
        return getInt("KUI-START-KEY");
    }

    public void setKuiStartKey(int value) {
        setInt("KUI-START-KEY", value);
    }

    public String getKuiState() {
        return getString("KUI-STATE");
    }

    public void setKuiState(String value) {
        setString("KUI-STATE", value);
    }

    public String getKuiZip() {
        return getString("KUI-ZIP");
    }

    public void setKuiZip(String value) {
        setString("KUI-ZIP", value);
    }

    public int getKurFico(int index) {
        return getInt("KUR-FICO", index);
    }

    public void setKurFico(int index, int value) {
        setInt("KUR-FICO", value, index);
    }

    public int getKurId(int index) {
        return getInt("KUR-ID", index);
    }

    public void setKurId(int index, int value) {
        setInt("KUR-ID", value, index);
    }

    public String getKurName(int index) {
        return getString("KUR-NAME", index);
    }

    public void setKurName(int index, String value) {
        setString("KUR-NAME", value, index);
    }

    public String getKurState(int index) {
        return getString("KUR-STATE", index);
    }

    public void setKurState(int index, String value) {
        setString("KUR-STATE", value, index);
    }

    public String getKurZip(int index) {
        return getString("KUR-ZIP", index);
    }

    public void setKurZip(int index, String value) {
        setString("KUR-ZIP", value, index);
    }

    public String getWsCustfile() {
        return getString("WS-CUSTFILE");
    }

    public void setWsCustfile(String value) {
        setString("WS-CUSTFILE", value);
    }

    public String getWsEndFlg() {
        return getString("WS-END-FLG");
    }

    public void setWsEndFlg(String value) {
        setString("WS-END-FLG", value);
    }

    public int getWsI() {
        return getInt("WS-I");
    }

    public void setWsI(int value) {
        setInt("WS-I", value);
    }

    public int getWsLastKey() {
        return getInt("WS-LAST-KEY");
    }

    public void setWsLastKey(int value) {
        setInt("WS-LAST-KEY", value);
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

    public String getWsName() {
        return getString("WS-NAME");
    }

    public void setWsName(String value) {
        setString("WS-NAME", value);
    }

    public int getWsRespCd() {
        return getInt("WS-RESP-CD");
    }

    public void setWsRespCd(int value) {
        setInt("WS-RESP-CD", value);
    }

    public int getWsZiplen() {
        return getInt("WS-ZIPLEN");
    }

    public void setWsZiplen(int value) {
        setInt("WS-ZIPLEN", value);
    }
}

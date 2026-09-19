package com.sakura.taxcal.domain;

import com.sakura.runtime.record.RuntimeFieldAccess;
import com.sakura.taxcal.runtime.TaxcalDatasets;

import java.math.BigDecimal;

/**
 * Field accessor for TAXCAL. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class TaxcalFieldAccess extends RuntimeFieldAccess {

    public TaxcalFieldAccess(WorkingStorage ws, TaxcalDatasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getTaxf() != null) {
            register(fileSet.getTaxf().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getCompletionCode() {
        return getInt("COMPLETION-CODE");
    }

    public void setCompletionCode(int value) {
        setInt("COMPLETION-CODE", value);
    }

    public String getFsts() {
        return getString("FSTS");
    }

    public void setFsts(String value) {
        setString("FSTS", value);
    }

    public int getTxCode() {
        return getInt("TX-CODE");
    }

    public void setTxCode(int value) {
        setInt("TX-CODE", value);
    }

    public BigDecimal getTxRate() {
        return getDecimal("TX-RATE");
    }

    public void setTxRate(BigDecimal value) {
        setDecimal("TX-RATE", value);
    }

    public int getTxStartDate() {
        return getInt("TX-START-DATE");
    }

    public void setTxStartDate(int value) {
        setInt("TX-START-DATE", value);
    }

    public int getWkFound() {
        return getInt("WK-FOUND");
    }

    public void setWkFound(int value) {
        setInt("WK-FOUND", value);
    }

    public BigDecimal getWkFrac() {
        return getDecimal("WK-FRAC");
    }

    public void setWkFrac(BigDecimal value) {
        setDecimal("WK-FRAC", value);
    }

    public long getWkInt() {
        return getLong("WK-INT");
    }

    public void setWkInt(long value) {
        setLong("WK-INT", value);
    }

    public BigDecimal getWkRate() {
        return getDecimal("WK-RATE");
    }

    public void setWkRate(BigDecimal value) {
        setDecimal("WK-RATE", value);
    }

    public BigDecimal getWkRaw() {
        return getDecimal("WK-RAW");
    }

    public void setWkRaw(BigDecimal value) {
        setDecimal("WK-RAW", value);
    }
}

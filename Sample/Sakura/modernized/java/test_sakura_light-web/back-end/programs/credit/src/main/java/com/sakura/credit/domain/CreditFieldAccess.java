package com.sakura.credit.domain;

import com.sakura.credit.runtime.CreditDatasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for CREDIT. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class CreditFieldAccess extends RuntimeFieldAccess {

    public CreditFieldAccess(WorkingStorage ws, CreditDatasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getCustf() != null) {
            register(fileSet.getCustf().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getCompletionCode() {
        return getInt("COMPLETION-CODE");
    }

    public void setCompletionCode(int value) {
        setInt("COMPLETION-CODE", value);
    }

    public BigDecimal getCuBalance() {
        return getDecimal("CU-BALANCE");
    }

    public void setCuBalance(BigDecimal value) {
        setDecimal("CU-BALANCE", value);
    }

    public int getCuCode() {
        return getInt("CU-CODE");
    }

    public void setCuCode(int value) {
        setInt("CU-CODE", value);
    }

    public BigDecimal getCuCreditLimit() {
        return getDecimal("CU-CREDIT-LIMIT");
    }

    public void setCuCreditLimit(BigDecimal value) {
        setDecimal("CU-CREDIT-LIMIT", value);
    }

    public String getFsts() {
        return getString("FSTS");
    }

    public void setFsts(String value) {
        setString("FSTS", value);
    }
}

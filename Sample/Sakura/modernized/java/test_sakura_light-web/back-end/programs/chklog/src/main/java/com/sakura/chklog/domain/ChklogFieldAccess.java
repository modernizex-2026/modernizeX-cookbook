package com.sakura.chklog.domain;

import com.sakura.chklog.runtime.ChklogDatasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

/**
 * Field accessor for CHKLOG. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class ChklogFieldAccess extends RuntimeFieldAccess {

    public ChklogFieldAccess(WorkingStorage ws, ChklogDatasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getUserf() != null) {
            register(fileSet.getUserf().buffer());
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

    public int getUsAuthClose() {
        return getInt("US-AUTH-CLOSE");
    }

    public void setUsAuthClose(int value) {
        setInt("US-AUTH-CLOSE", value);
    }

    public int getUsAuthMaster() {
        return getInt("US-AUTH-MASTER");
    }

    public void setUsAuthMaster(int value) {
        setInt("US-AUTH-MASTER", value);
    }

    public int getUsAuthOrder() {
        return getInt("US-AUTH-ORDER");
    }

    public void setUsAuthOrder(int value) {
        setInt("US-AUTH-ORDER", value);
    }

    public int getUsAuthPurch() {
        return getInt("US-AUTH-PURCH");
    }

    public void setUsAuthPurch(int value) {
        setInt("US-AUTH-PURCH", value);
    }

    public int getUsAuthSales() {
        return getInt("US-AUTH-SALES");
    }

    public void setUsAuthSales(int value) {
        setInt("US-AUTH-SALES", value);
    }

    public int getUsCode() {
        return getInt("US-CODE");
    }

    public void setUsCode(int value) {
        setInt("US-CODE", value);
    }

    public int getUsDelFlag() {
        return getInt("US-DEL-FLAG");
    }

    public void setUsDelFlag(int value) {
        setInt("US-DEL-FLAG", value);
    }

    public String getUsLogin() {
        return getString("US-LOGIN");
    }

    public void setUsLogin(String value) {
        setString("US-LOGIN", value);
    }

    public String getUsName() {
        return getString("US-NAME");
    }

    public void setUsName(String value) {
        setString("US-NAME", value);
    }

    public String getUsPassword() {
        return getString("US-PASSWORD");
    }

    public void setUsPassword(String value) {
        setString("US-PASSWORD", value);
    }

    public int getUsRole() {
        return getInt("US-ROLE");
    }

    public void setUsRole(int value) {
        setInt("US-ROLE", value);
    }

    public String getWkAuth() {
        return getString("WK-AUTH");
    }

    public void setWkAuth(String value) {
        setString("WK-AUTH", value);
    }
}

package com.generated.orion.dao;

import com.appruntime.DuplicateException;
import com.appruntime.FieldStore;
import com.appruntime.FileDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * CICS File DAO for ACCTFILE (VSAM INDEXED). ← COBOL: EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR
 * FILE('ACCTFILE') Table: acctfile, PK: (ac_id)
 */
@Repository("ACCTFILE")
public class AcctfileFileDao implements FileDao {
    @Autowired private JdbcTemplate jdbc;

    @Override
    public String getFileName() {
        return "ACCTFILE";
    }

    /** ← EXEC CICS READ FILE RIDFLD */
    @Override
    public Object readByKey(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM: SPACES/LOW-VALUES ridfld never equals a numeric key → NOTFND
        if (k.isEmpty()) {
            return null;
        }
        try {
            return jdbc.queryForMap("SELECT * FROM acctfile WHERE ac_id = CAST(? AS NUMERIC)", k);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    /** ← EXEC CICS READ FILE RIDFLD UPDATE */
    @Override
    public Object readByKeyForUpdate(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM: SPACES/LOW-VALUES ridfld never equals a numeric key → NOTFND
        if (k.isEmpty()) {
            return null;
        }
        try {
            return jdbc.queryForMap(
                    "SELECT * FROM acctfile WHERE ac_id = CAST(? AS NUMERIC) FOR UPDATE", k);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    /** ← EXEC CICS WRITE FILE FROM RIDFLD */
    @Override
    public void write(Object record, Object key) {
        try {
            Object v0 = getFieldValue(record, "acId", "AC-ID");
            Object v1 = getFieldValue(record, "acActiveStatus", "AC-ACTIVE-STATUS");
            Object v2 = getFieldValue(record, "acCurrBal", "AC-CURR-BAL");
            Object v3 = getFieldValue(record, "acCreditLimit", "AC-CREDIT-LIMIT");
            Object v4 = getFieldValue(record, "acCashLimit", "AC-CASH-LIMIT");
            Object v5 = getFieldValue(record, "acOpenDate", "AC-OPEN-DATE");
            Object v6 = getFieldValue(record, "acExpiryDate", "AC-EXPIRY-DATE");
            Object v7 = getFieldValue(record, "acReissueDate", "AC-REISSUE-DATE");
            Object v8 = getFieldValue(record, "acCycCredit", "AC-CYC-CREDIT");
            Object v9 = getFieldValue(record, "acCycDebit", "AC-CYC-DEBIT");
            Object v10 = getFieldValue(record, "acAddrZip", "AC-ADDR-ZIP");
            Object v11 = getFieldValue(record, "acGroupId", "AC-GROUP-ID");
            jdbc.update(
                    "INSERT INTO acctfile (ac_id, ac_active_status, ac_curr_bal, ac_credit_limit,"
                        + " ac_cash_limit, ac_open_date, ac_expiry_date, ac_reissue_date,"
                        + " ac_cyc_credit, ac_cyc_debit, ac_addr_zip, ac_group_id) VALUES (CAST(?"
                        + " AS NUMERIC), ?, CAST(? AS NUMERIC), CAST(? AS NUMERIC), CAST(? AS"
                        + " NUMERIC), ?, ?, ?, CAST(? AS NUMERIC), CAST(? AS NUMERIC), ?, ?)",
                    v0,
                    v1,
                    v2,
                    v3,
                    v4,
                    v5,
                    v6,
                    v7,
                    v8,
                    v9,
                    v10,
                    v11);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException("");
        }
    }

    /** ← EXEC CICS REWRITE FILE FROM */
    @Override
    public void rewrite(Object record) {
        Object v0 = getFieldValue(record, "acActiveStatus", "AC-ACTIVE-STATUS");
        Object v1 = getFieldValue(record, "acCurrBal", "AC-CURR-BAL");
        Object v2 = getFieldValue(record, "acCreditLimit", "AC-CREDIT-LIMIT");
        Object v3 = getFieldValue(record, "acCashLimit", "AC-CASH-LIMIT");
        Object v4 = getFieldValue(record, "acOpenDate", "AC-OPEN-DATE");
        Object v5 = getFieldValue(record, "acExpiryDate", "AC-EXPIRY-DATE");
        Object v6 = getFieldValue(record, "acReissueDate", "AC-REISSUE-DATE");
        Object v7 = getFieldValue(record, "acCycCredit", "AC-CYC-CREDIT");
        Object v8 = getFieldValue(record, "acCycDebit", "AC-CYC-DEBIT");
        Object v9 = getFieldValue(record, "acAddrZip", "AC-ADDR-ZIP");
        Object v10 = getFieldValue(record, "acGroupId", "AC-GROUP-ID");
        Object v11 = getFieldValue(record, "acId", "AC-ID");
        jdbc.update(
                "UPDATE acctfile SET ac_active_status = ?, ac_curr_bal = CAST(? AS NUMERIC),"
                    + " ac_credit_limit = CAST(? AS NUMERIC), ac_cash_limit = CAST(? AS NUMERIC),"
                    + " ac_open_date = ?, ac_expiry_date = ?, ac_reissue_date = ?, ac_cyc_credit ="
                    + " CAST(? AS NUMERIC), ac_cyc_debit = CAST(? AS NUMERIC), ac_addr_zip = ?,"
                    + " ac_group_id = ? WHERE ac_id = CAST(? AS NUMERIC)",
                v0,
                v1,
                v2,
                v3,
                v4,
                v5,
                v6,
                v7,
                v8,
                v9,
                v10,
                v11);
    }

    /** ← EXEC CICS DELETE FILE RIDFLD */
    @Override
    public void delete(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM: SPACES/LOW-VALUES ridfld never equals a numeric key → nothing to delete
        if (k.isEmpty()) {
            return;
        }
        jdbc.update("DELETE FROM acctfile WHERE ac_id = CAST(? AS NUMERIC)", k);
    }

    /** ← EXEC CICS STARTBR FILE RIDFLD → SELECT WHERE pk >= ? ORDER BY pk */
    @Override
    public List<Object> startBrowse(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM STARTBR GTEQ with SPACES/LOW-VALUES ridfld: position at the first record
        if (k.isEmpty()) {
            return new ArrayList<>(jdbc.queryForList("SELECT * FROM acctfile ORDER BY ac_id"));
        }
        return new ArrayList<>(
                jdbc.queryForList(
                        "SELECT * FROM acctfile WHERE ac_id >= CAST(? AS NUMERIC) ORDER BY ac_id",
                        k));
    }

    /** ← EXEC CICS READPREV backward set → SELECT WHERE pk <= ? ORDER BY pk DESC */
    @Override
    public List<Object> startBrowsePrev(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM: no record precedes the SPACES/LOW-VALUES key position
        if (k.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(
                jdbc.queryForList(
                        "SELECT * FROM acctfile WHERE ac_id <= CAST(? AS NUMERIC) ORDER BY ac_id"
                                + " DESC",
                        k));
    }

    private static Object getFieldValue(Object obj, String fieldName, String cobolName) {
        if (obj instanceof FieldStore) {
            return ((FieldStore) obj).getString(cobolName);
        }
        Class<?> clazz = obj.getClass();
        while (clazz != null && !clazz.getPackageName().startsWith("java.")) {
            try {
                java.lang.reflect.Field f = clazz.getDeclaredField(fieldName);
                f.setAccessible(true);
                return f.get(obj);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                return null;
            }
        }
        return null;
    }
}

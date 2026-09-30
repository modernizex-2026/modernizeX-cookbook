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
 * CICS File DAO for STMTFILE (VSAM INDEXED). ← COBOL: EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR
 * FILE('STMTFILE') Table: stmtfile, PK: (st_acct_id, st_cycle)
 */
@Repository("STMTFILE")
public class StmtfileFileDao implements FileDao {
    @Autowired private JdbcTemplate jdbc;

    @Override
    public String getFileName() {
        return "STMTFILE";
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
            String[] keys = k.split("\\|", 2);
            return jdbc.queryForMap(
                    "SELECT * FROM stmtfile WHERE st_acct_id = CAST(? AS NUMERIC) AND st_cycle ="
                            + " CAST(? AS NUMERIC)",
                    (Object[]) keys);
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
            String[] keys = k.split("\\|", 2);
            return jdbc.queryForMap(
                    "SELECT * FROM stmtfile WHERE st_acct_id = CAST(? AS NUMERIC) AND st_cycle ="
                            + " CAST(? AS NUMERIC) FOR UPDATE",
                    (Object[]) keys);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    /** ← EXEC CICS WRITE FILE FROM RIDFLD */
    @Override
    public void write(Object record, Object key) {
        try {
            Object v0 = getFieldValue(record, "stAcctId", "ST-ACCT-ID");
            Object v1 = getFieldValue(record, "stCycle", "ST-CYCLE");
            Object v2 = getFieldValue(record, "stOpenBal", "ST-OPEN-BAL");
            Object v3 = getFieldValue(record, "stCloseBal", "ST-CLOSE-BAL");
            Object v4 = getFieldValue(record, "stTotalCredit", "ST-TOTAL-CREDIT");
            Object v5 = getFieldValue(record, "stTotalDebit", "ST-TOTAL-DEBIT");
            Object v6 = getFieldValue(record, "stMinDue", "ST-MIN-DUE");
            Object v7 = getFieldValue(record, "stDueDate", "ST-DUE-DATE");
            jdbc.update(
                    "INSERT INTO stmtfile (st_acct_id, st_cycle, st_open_bal, st_close_bal,"
                        + " st_total_credit, st_total_debit, st_min_due, st_due_date) VALUES"
                        + " (CAST(? AS NUMERIC), CAST(? AS NUMERIC), CAST(? AS NUMERIC), CAST(? AS"
                        + " NUMERIC), CAST(? AS NUMERIC), CAST(? AS NUMERIC), CAST(? AS NUMERIC),"
                        + " ?)",
                    v0,
                    v1,
                    v2,
                    v3,
                    v4,
                    v5,
                    v6,
                    v7);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException("");
        }
    }

    /** ← EXEC CICS REWRITE FILE FROM */
    @Override
    public void rewrite(Object record) {
        Object v0 = getFieldValue(record, "stOpenBal", "ST-OPEN-BAL");
        Object v1 = getFieldValue(record, "stCloseBal", "ST-CLOSE-BAL");
        Object v2 = getFieldValue(record, "stTotalCredit", "ST-TOTAL-CREDIT");
        Object v3 = getFieldValue(record, "stTotalDebit", "ST-TOTAL-DEBIT");
        Object v4 = getFieldValue(record, "stMinDue", "ST-MIN-DUE");
        Object v5 = getFieldValue(record, "stDueDate", "ST-DUE-DATE");
        Object v6 = getFieldValue(record, "stAcctId", "ST-ACCT-ID");
        Object v7 = getFieldValue(record, "stCycle", "ST-CYCLE");
        jdbc.update(
                "UPDATE stmtfile SET st_open_bal = CAST(? AS NUMERIC), st_close_bal = CAST(? AS"
                    + " NUMERIC), st_total_credit = CAST(? AS NUMERIC), st_total_debit = CAST(? AS"
                    + " NUMERIC), st_min_due = CAST(? AS NUMERIC), st_due_date = ? WHERE st_acct_id"
                    + " = CAST(? AS NUMERIC) AND st_cycle = CAST(? AS NUMERIC)",
                v0,
                v1,
                v2,
                v3,
                v4,
                v5,
                v6,
                v7);
    }

    /** ← EXEC CICS DELETE FILE RIDFLD */
    @Override
    public void delete(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM: SPACES/LOW-VALUES ridfld never equals a numeric key → nothing to delete
        if (k.isEmpty()) {
            return;
        }
        String[] keys = k.split("\\|", 2);
        jdbc.update(
                "DELETE FROM stmtfile WHERE st_acct_id = CAST(? AS NUMERIC) AND st_cycle = CAST(?"
                        + " AS NUMERIC)",
                (Object[]) keys);
    }

    /** ← EXEC CICS STARTBR FILE RIDFLD → SELECT WHERE pk >= ? ORDER BY pk */
    @Override
    public List<Object> startBrowse(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM STARTBR GTEQ with SPACES/LOW-VALUES ridfld: position at the first record
        if (k.isEmpty()) {
            return new ArrayList<>(
                    jdbc.queryForList("SELECT * FROM stmtfile ORDER BY st_acct_id, st_cycle"));
        }
        String[] keys = k.split("\\|", 2);
        return new ArrayList<>(
                jdbc.queryForList(
                        "SELECT * FROM stmtfile WHERE (st_acct_id, st_cycle) >= (CAST(? AS"
                                + " NUMERIC), CAST(? AS NUMERIC)) ORDER BY st_acct_id, st_cycle",
                        (Object[]) keys));
    }

    /** ← EXEC CICS READPREV backward set → SELECT WHERE pk <= ? ORDER BY pk DESC */
    @Override
    public List<Object> startBrowsePrev(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM: no record precedes the SPACES/LOW-VALUES key position
        if (k.isEmpty()) {
            return new ArrayList<>();
        }
        String[] keys = k.split("\\|", 2);
        return new ArrayList<>(
                jdbc.queryForList(
                        "SELECT * FROM stmtfile WHERE (st_acct_id, st_cycle) <= (CAST(? AS"
                            + " NUMERIC), CAST(? AS NUMERIC)) ORDER BY st_acct_id DESC, st_cycle"
                            + " DESC",
                        (Object[]) keys));
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

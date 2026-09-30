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
 * CICS File DAO for BILLFILE (VSAM INDEXED). ← COBOL: EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR
 * FILE('BILLFILE') Table: billfile, PK: (bl_id)
 */
@Repository("BILLFILE")
public class BillfileFileDao implements FileDao {
    @Autowired private JdbcTemplate jdbc;

    @Override
    public String getFileName() {
        return "BILLFILE";
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
            return jdbc.queryForMap("SELECT * FROM billfile WHERE bl_id = CAST(? AS NUMERIC)", k);
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
                    "SELECT * FROM billfile WHERE bl_id = CAST(? AS NUMERIC) FOR UPDATE", k);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    /** ← EXEC CICS WRITE FILE FROM RIDFLD */
    @Override
    public void write(Object record, Object key) {
        try {
            Object v0 = getFieldValue(record, "blId", "BL-ID");
            Object v1 = getFieldValue(record, "blAcctId", "BL-ACCT-ID");
            Object v2 = getFieldValue(record, "blAmount", "BL-AMOUNT");
            Object v3 = getFieldValue(record, "blPayDate", "BL-PAY-DATE");
            Object v4 = getFieldValue(record, "blConfirmNum", "BL-CONFIRM-NUM");
            Object v5 = getFieldValue(record, "blStatus", "BL-STATUS");
            jdbc.update(
                    "INSERT INTO billfile (bl_id, bl_acct_id, bl_amount, bl_pay_date,"
                            + " bl_confirm_num, bl_status) VALUES (CAST(? AS NUMERIC), CAST(? AS"
                            + " NUMERIC), CAST(? AS NUMERIC), ?, ?, ?)",
                    v0,
                    v1,
                    v2,
                    v3,
                    v4,
                    v5);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException("");
        }
    }

    /** ← EXEC CICS REWRITE FILE FROM */
    @Override
    public void rewrite(Object record) {
        Object v0 = getFieldValue(record, "blAcctId", "BL-ACCT-ID");
        Object v1 = getFieldValue(record, "blAmount", "BL-AMOUNT");
        Object v2 = getFieldValue(record, "blPayDate", "BL-PAY-DATE");
        Object v3 = getFieldValue(record, "blConfirmNum", "BL-CONFIRM-NUM");
        Object v4 = getFieldValue(record, "blStatus", "BL-STATUS");
        Object v5 = getFieldValue(record, "blId", "BL-ID");
        jdbc.update(
                "UPDATE billfile SET bl_acct_id = CAST(? AS NUMERIC), bl_amount = CAST(? AS"
                    + " NUMERIC), bl_pay_date = ?, bl_confirm_num = ?, bl_status = ? WHERE bl_id ="
                    + " CAST(? AS NUMERIC)",
                v0,
                v1,
                v2,
                v3,
                v4,
                v5);
    }

    /** ← EXEC CICS DELETE FILE RIDFLD */
    @Override
    public void delete(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM: SPACES/LOW-VALUES ridfld never equals a numeric key → nothing to delete
        if (k.isEmpty()) {
            return;
        }
        jdbc.update("DELETE FROM billfile WHERE bl_id = CAST(? AS NUMERIC)", k);
    }

    /** ← EXEC CICS STARTBR FILE RIDFLD → SELECT WHERE pk >= ? ORDER BY pk */
    @Override
    public List<Object> startBrowse(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM STARTBR GTEQ with SPACES/LOW-VALUES ridfld: position at the first record
        if (k.isEmpty()) {
            return new ArrayList<>(jdbc.queryForList("SELECT * FROM billfile ORDER BY bl_id"));
        }
        return new ArrayList<>(
                jdbc.queryForList(
                        "SELECT * FROM billfile WHERE bl_id >= CAST(? AS NUMERIC) ORDER BY bl_id",
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
                        "SELECT * FROM billfile WHERE bl_id <= CAST(? AS NUMERIC) ORDER BY bl_id"
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

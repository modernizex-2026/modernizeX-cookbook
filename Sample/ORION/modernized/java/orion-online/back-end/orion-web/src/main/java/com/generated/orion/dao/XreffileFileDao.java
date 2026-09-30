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
 * CICS File DAO for XREFFILE (VSAM INDEXED). ← COBOL: EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR
 * FILE('XREFFILE') Table: xreffile, PK: (xr_card_num)
 */
@Repository("XREFFILE")
public class XreffileFileDao implements FileDao {
    @Autowired private JdbcTemplate jdbc;

    @Override
    public String getFileName() {
        return "XREFFILE";
    }

    /** ← EXEC CICS READ FILE RIDFLD */
    @Override
    public Object readByKey(Object key) {
        String k = key == null ? "" : key.toString().trim();
        try {
            return jdbc.queryForMap(
                    "SELECT * FROM xreffile WHERE RTRIM(CAST(xr_card_num AS VARCHAR)) ="
                            + " RTRIM(CAST(? AS VARCHAR))",
                    k);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    /** ← EXEC CICS READ FILE RIDFLD UPDATE */
    @Override
    public Object readByKeyForUpdate(Object key) {
        String k = key == null ? "" : key.toString().trim();
        try {
            return jdbc.queryForMap(
                    "SELECT * FROM xreffile WHERE RTRIM(CAST(xr_card_num AS VARCHAR)) ="
                            + " RTRIM(CAST(? AS VARCHAR)) FOR UPDATE",
                    k);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    /** ← EXEC CICS WRITE FILE FROM RIDFLD */
    @Override
    public void write(Object record, Object key) {
        try {
            Object v0 = getFieldValue(record, "xrCardNum", "XR-CARD-NUM");
            Object v1 = getFieldValue(record, "xrAcctId", "XR-ACCT-ID");
            Object v2 = getFieldValue(record, "xrCustId", "XR-CUST-ID");
            jdbc.update(
                    "INSERT INTO xreffile (xr_card_num, xr_acct_id, xr_cust_id) VALUES (?, CAST(?"
                            + " AS NUMERIC), CAST(? AS NUMERIC))",
                    v0,
                    v1,
                    v2);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException("");
        }
    }

    /** ← EXEC CICS REWRITE FILE FROM */
    @Override
    public void rewrite(Object record) {
        Object v0 = getFieldValue(record, "xrAcctId", "XR-ACCT-ID");
        Object v1 = getFieldValue(record, "xrCustId", "XR-CUST-ID");
        Object v2 = getFieldValue(record, "xrCardNum", "XR-CARD-NUM");
        jdbc.update(
                "UPDATE xreffile SET xr_acct_id = CAST(? AS NUMERIC), xr_cust_id = CAST(? AS"
                        + " NUMERIC) WHERE RTRIM(CAST(xr_card_num AS VARCHAR)) = RTRIM(CAST(? AS"
                        + " VARCHAR))",
                v0,
                v1,
                v2);
    }

    /** ← EXEC CICS DELETE FILE RIDFLD */
    @Override
    public void delete(Object key) {
        String k = key == null ? "" : key.toString().trim();
        jdbc.update(
                "DELETE FROM xreffile WHERE RTRIM(CAST(xr_card_num AS VARCHAR)) = RTRIM(CAST(? AS"
                        + " VARCHAR))",
                k);
    }

    /** ← EXEC CICS STARTBR FILE RIDFLD → SELECT WHERE pk >= ? ORDER BY pk */
    @Override
    public List<Object> startBrowse(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM STARTBR GTEQ with SPACES/LOW-VALUES ridfld: position at the first record
        if (k.isEmpty()) {
            return new ArrayList<>(
                    jdbc.queryForList("SELECT * FROM xreffile ORDER BY xr_card_num"));
        }
        return new ArrayList<>(
                jdbc.queryForList(
                        "SELECT * FROM xreffile WHERE RTRIM(CAST(xr_card_num AS VARCHAR)) >="
                                + " RTRIM(CAST(? AS VARCHAR)) ORDER BY xr_card_num",
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
                        "SELECT * FROM xreffile WHERE RTRIM(CAST(xr_card_num AS VARCHAR)) <="
                                + " RTRIM(CAST(? AS VARCHAR)) ORDER BY xr_card_num DESC",
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

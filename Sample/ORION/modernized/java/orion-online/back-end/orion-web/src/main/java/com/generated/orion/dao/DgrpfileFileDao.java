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
 * CICS File DAO for DGRPFILE (VSAM INDEXED). ← COBOL: EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR
 * FILE('DGRPFILE') Table: dgrpfile, PK: (dg_acct_group, dg_type_cd, dg_cat_cd)
 */
@Repository("DGRPFILE")
public class DgrpfileFileDao implements FileDao {
    @Autowired private JdbcTemplate jdbc;

    @Override
    public String getFileName() {
        return "DGRPFILE";
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
            String[] keys = k.split("\\|", 3);
            return jdbc.queryForMap(
                    "SELECT * FROM dgrpfile WHERE RTRIM(CAST(dg_acct_group AS VARCHAR)) ="
                            + " RTRIM(CAST(? AS VARCHAR)) AND RTRIM(CAST(dg_type_cd AS VARCHAR)) ="
                            + " RTRIM(CAST(? AS VARCHAR)) AND dg_cat_cd = CAST(? AS NUMERIC)",
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
            String[] keys = k.split("\\|", 3);
            return jdbc.queryForMap(
                    "SELECT * FROM dgrpfile WHERE RTRIM(CAST(dg_acct_group AS VARCHAR)) ="
                            + " RTRIM(CAST(? AS VARCHAR)) AND RTRIM(CAST(dg_type_cd AS VARCHAR)) ="
                            + " RTRIM(CAST(? AS VARCHAR)) AND dg_cat_cd = CAST(? AS NUMERIC) FOR"
                            + " UPDATE",
                    (Object[]) keys);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    /** ← EXEC CICS WRITE FILE FROM RIDFLD */
    @Override
    public void write(Object record, Object key) {
        try {
            Object v0 = getFieldValue(record, "dgAcctGroup", "DG-ACCT-GROUP");
            Object v1 = getFieldValue(record, "dgTypeCd", "DG-TYPE-CD");
            Object v2 = getFieldValue(record, "dgCatCd", "DG-CAT-CD");
            Object v3 = getFieldValue(record, "dgIntRate", "DG-INT-RATE");
            jdbc.update(
                    "INSERT INTO dgrpfile (dg_acct_group, dg_type_cd, dg_cat_cd, dg_int_rate)"
                            + " VALUES (?, ?, CAST(? AS NUMERIC), CAST(? AS NUMERIC))",
                    v0,
                    v1,
                    v2,
                    v3);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException("");
        }
    }

    /** ← EXEC CICS REWRITE FILE FROM */
    @Override
    public void rewrite(Object record) {
        Object v0 = getFieldValue(record, "dgIntRate", "DG-INT-RATE");
        Object v1 = getFieldValue(record, "dgAcctGroup", "DG-ACCT-GROUP");
        Object v2 = getFieldValue(record, "dgTypeCd", "DG-TYPE-CD");
        Object v3 = getFieldValue(record, "dgCatCd", "DG-CAT-CD");
        jdbc.update(
                "UPDATE dgrpfile SET dg_int_rate = CAST(? AS NUMERIC) WHERE"
                        + " RTRIM(CAST(dg_acct_group AS VARCHAR)) = RTRIM(CAST(? AS VARCHAR)) AND"
                        + " RTRIM(CAST(dg_type_cd AS VARCHAR)) = RTRIM(CAST(? AS VARCHAR)) AND"
                        + " dg_cat_cd = CAST(? AS NUMERIC)",
                v0,
                v1,
                v2,
                v3);
    }

    /** ← EXEC CICS DELETE FILE RIDFLD */
    @Override
    public void delete(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM: SPACES/LOW-VALUES ridfld never equals a numeric key → nothing to delete
        if (k.isEmpty()) {
            return;
        }
        String[] keys = k.split("\\|", 3);
        jdbc.update(
                "DELETE FROM dgrpfile WHERE RTRIM(CAST(dg_acct_group AS VARCHAR)) = RTRIM(CAST(? AS"
                        + " VARCHAR)) AND RTRIM(CAST(dg_type_cd AS VARCHAR)) = RTRIM(CAST(? AS"
                        + " VARCHAR)) AND dg_cat_cd = CAST(? AS NUMERIC)",
                (Object[]) keys);
    }

    /** ← EXEC CICS STARTBR FILE RIDFLD → SELECT WHERE pk >= ? ORDER BY pk */
    @Override
    public List<Object> startBrowse(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM STARTBR GTEQ with SPACES/LOW-VALUES ridfld: position at the first record
        if (k.isEmpty()) {
            return new ArrayList<>(
                    jdbc.queryForList(
                            "SELECT * FROM dgrpfile ORDER BY dg_acct_group, dg_type_cd,"
                                    + " dg_cat_cd"));
        }
        String[] keys = k.split("\\|", 3);
        return new ArrayList<>(
                jdbc.queryForList(
                        "SELECT * FROM dgrpfile WHERE (RTRIM(CAST(dg_acct_group AS VARCHAR)),"
                            + " RTRIM(CAST(dg_type_cd AS VARCHAR)), dg_cat_cd) >= (RTRIM(CAST(? AS"
                            + " VARCHAR)), RTRIM(CAST(? AS VARCHAR)), CAST(? AS NUMERIC)) ORDER BY"
                            + " dg_acct_group, dg_type_cd, dg_cat_cd",
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
        String[] keys = k.split("\\|", 3);
        return new ArrayList<>(
                jdbc.queryForList(
                        "SELECT * FROM dgrpfile WHERE (RTRIM(CAST(dg_acct_group AS VARCHAR)),"
                            + " RTRIM(CAST(dg_type_cd AS VARCHAR)), dg_cat_cd) <= (RTRIM(CAST(? AS"
                            + " VARCHAR)), RTRIM(CAST(? AS VARCHAR)), CAST(? AS NUMERIC)) ORDER BY"
                            + " dg_acct_group DESC, dg_type_cd DESC, dg_cat_cd DESC",
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

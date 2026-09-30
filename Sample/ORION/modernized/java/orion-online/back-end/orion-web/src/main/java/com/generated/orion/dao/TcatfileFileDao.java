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
 * CICS File DAO for TCATFILE (VSAM INDEXED). ← COBOL: EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR
 * FILE('TCATFILE') Table: tcatfile, PK: (tc_type_cd, tc_cd)
 */
@Repository("TCATFILE")
public class TcatfileFileDao implements FileDao {
    @Autowired private JdbcTemplate jdbc;

    @Override
    public String getFileName() {
        return "TCATFILE";
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
                    "SELECT * FROM tcatfile WHERE RTRIM(CAST(tc_type_cd AS VARCHAR)) = RTRIM(CAST(?"
                            + " AS VARCHAR)) AND tc_cd = CAST(? AS NUMERIC)",
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
                    "SELECT * FROM tcatfile WHERE RTRIM(CAST(tc_type_cd AS VARCHAR)) = RTRIM(CAST(?"
                            + " AS VARCHAR)) AND tc_cd = CAST(? AS NUMERIC) FOR UPDATE",
                    (Object[]) keys);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    /** ← EXEC CICS WRITE FILE FROM RIDFLD */
    @Override
    public void write(Object record, Object key) {
        try {
            Object v0 = getFieldValue(record, "tcTypeCd", "TC-TYPE-CD");
            Object v1 = getFieldValue(record, "tcCd", "TC-CD");
            Object v2 = getFieldValue(record, "tcDesc", "TC-DESC");
            jdbc.update(
                    "INSERT INTO tcatfile (tc_type_cd, tc_cd, tc_desc) VALUES (?, CAST(? AS"
                            + " NUMERIC), ?)",
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
        Object v0 = getFieldValue(record, "tcDesc", "TC-DESC");
        Object v1 = getFieldValue(record, "tcTypeCd", "TC-TYPE-CD");
        Object v2 = getFieldValue(record, "tcCd", "TC-CD");
        jdbc.update(
                "UPDATE tcatfile SET tc_desc = ? WHERE RTRIM(CAST(tc_type_cd AS VARCHAR)) ="
                        + " RTRIM(CAST(? AS VARCHAR)) AND tc_cd = CAST(? AS NUMERIC)",
                v0,
                v1,
                v2);
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
                "DELETE FROM tcatfile WHERE RTRIM(CAST(tc_type_cd AS VARCHAR)) = RTRIM(CAST(? AS"
                        + " VARCHAR)) AND tc_cd = CAST(? AS NUMERIC)",
                (Object[]) keys);
    }

    /** ← EXEC CICS STARTBR FILE RIDFLD → SELECT WHERE pk >= ? ORDER BY pk */
    @Override
    public List<Object> startBrowse(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM STARTBR GTEQ with SPACES/LOW-VALUES ridfld: position at the first record
        if (k.isEmpty()) {
            return new ArrayList<>(
                    jdbc.queryForList("SELECT * FROM tcatfile ORDER BY tc_type_cd, tc_cd"));
        }
        String[] keys = k.split("\\|", 2);
        return new ArrayList<>(
                jdbc.queryForList(
                        "SELECT * FROM tcatfile WHERE (RTRIM(CAST(tc_type_cd AS VARCHAR)), tc_cd)"
                                + " >= (RTRIM(CAST(? AS VARCHAR)), CAST(? AS NUMERIC)) ORDER BY"
                                + " tc_type_cd, tc_cd",
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
                        "SELECT * FROM tcatfile WHERE (RTRIM(CAST(tc_type_cd AS VARCHAR)), tc_cd)"
                                + " <= (RTRIM(CAST(? AS VARCHAR)), CAST(? AS NUMERIC)) ORDER BY"
                                + " tc_type_cd DESC, tc_cd DESC",
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

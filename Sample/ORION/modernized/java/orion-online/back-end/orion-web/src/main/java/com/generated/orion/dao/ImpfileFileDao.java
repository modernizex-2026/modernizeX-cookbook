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
 * CICS File DAO for IMPFILE (VSAM INDEXED). ← COBOL: EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR
 * FILE('IMPFILE') Table: impfile, PK: (imp_key)
 */
@Repository("IMPFILE")
public class ImpfileFileDao implements FileDao {
    @Autowired private JdbcTemplate jdbc;

    @Override
    public String getFileName() {
        return "IMPFILE";
    }

    /** ← EXEC CICS READ FILE RIDFLD */
    @Override
    public Object readByKey(Object key) {
        String k = key == null ? "" : key.toString().trim();
        try {
            return jdbc.queryForMap(
                    "SELECT * FROM impfile WHERE RTRIM(CAST(imp_key AS VARCHAR)) = RTRIM(CAST(? AS"
                            + " VARCHAR))",
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
                    "SELECT * FROM impfile WHERE RTRIM(CAST(imp_key AS VARCHAR)) = RTRIM(CAST(? AS"
                            + " VARCHAR)) FOR UPDATE",
                    k);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    /** ← EXEC CICS WRITE FILE FROM RIDFLD */
    @Override
    public void write(Object record, Object key) {
        try {
            Object v0 = getFieldValue(record, "impKey", "IMP-KEY");
            Object v1 = getFieldValue(record, "impData", "IMP-DATA");
            jdbc.update("INSERT INTO impfile (imp_key, imp_data) VALUES (?, ?)", v0, v1);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException("");
        }
    }

    /** ← EXEC CICS REWRITE FILE FROM */
    @Override
    public void rewrite(Object record) {
        Object v0 = getFieldValue(record, "impData", "IMP-DATA");
        Object v1 = getFieldValue(record, "impKey", "IMP-KEY");
        jdbc.update(
                "UPDATE impfile SET imp_data = ? WHERE RTRIM(CAST(imp_key AS VARCHAR)) ="
                        + " RTRIM(CAST(? AS VARCHAR))",
                v0,
                v1);
    }

    /** ← EXEC CICS DELETE FILE RIDFLD */
    @Override
    public void delete(Object key) {
        String k = key == null ? "" : key.toString().trim();
        jdbc.update(
                "DELETE FROM impfile WHERE RTRIM(CAST(imp_key AS VARCHAR)) = RTRIM(CAST(? AS"
                        + " VARCHAR))",
                k);
    }

    /** ← EXEC CICS STARTBR FILE RIDFLD → SELECT WHERE pk >= ? ORDER BY pk */
    @Override
    public List<Object> startBrowse(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM STARTBR GTEQ with SPACES/LOW-VALUES ridfld: position at the first record
        if (k.isEmpty()) {
            return new ArrayList<>(jdbc.queryForList("SELECT * FROM impfile ORDER BY imp_key"));
        }
        return new ArrayList<>(
                jdbc.queryForList(
                        "SELECT * FROM impfile WHERE RTRIM(CAST(imp_key AS VARCHAR)) >="
                                + " RTRIM(CAST(? AS VARCHAR)) ORDER BY imp_key",
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
                        "SELECT * FROM impfile WHERE RTRIM(CAST(imp_key AS VARCHAR)) <="
                                + " RTRIM(CAST(? AS VARCHAR)) ORDER BY imp_key DESC",
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

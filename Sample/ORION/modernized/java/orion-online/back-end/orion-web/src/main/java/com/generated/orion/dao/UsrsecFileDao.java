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
 * CICS File DAO for USRSEC (VSAM INDEXED). ← COBOL: EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR
 * FILE('USRSEC') Table: usrsec, PK: (us_id)
 */
@Repository("USRSEC")
public class UsrsecFileDao implements FileDao {
    @Autowired private JdbcTemplate jdbc;

    @Override
    public String getFileName() {
        return "USRSEC";
    }

    /** ← EXEC CICS READ FILE RIDFLD */
    @Override
    public Object readByKey(Object key) {
        String k = key == null ? "" : key.toString().trim();
        try {
            return jdbc.queryForMap(
                    "SELECT * FROM usrsec WHERE RTRIM(CAST(us_id AS VARCHAR)) = RTRIM(CAST(? AS"
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
                    "SELECT * FROM usrsec WHERE RTRIM(CAST(us_id AS VARCHAR)) = RTRIM(CAST(? AS"
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
            Object v0 = getFieldValue(record, "usId", "US-ID");
            Object v1 = getFieldValue(record, "usFirstName", "US-FIRST-NAME");
            Object v2 = getFieldValue(record, "usLastName", "US-LAST-NAME");
            Object v3 = getFieldValue(record, "usPassword", "US-PASSWORD");
            Object v4 = getFieldValue(record, "usType", "US-TYPE");
            jdbc.update(
                    "INSERT INTO usrsec (us_id, us_first_name, us_last_name, us_password, us_type)"
                            + " VALUES (?, ?, ?, ?, ?)",
                    v0,
                    v1,
                    v2,
                    v3,
                    v4);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException("");
        }
    }

    /** ← EXEC CICS REWRITE FILE FROM */
    @Override
    public void rewrite(Object record) {
        Object v0 = getFieldValue(record, "usFirstName", "US-FIRST-NAME");
        Object v1 = getFieldValue(record, "usLastName", "US-LAST-NAME");
        Object v2 = getFieldValue(record, "usPassword", "US-PASSWORD");
        Object v3 = getFieldValue(record, "usType", "US-TYPE");
        Object v4 = getFieldValue(record, "usId", "US-ID");
        jdbc.update(
                "UPDATE usrsec SET us_first_name = ?, us_last_name = ?, us_password = ?, us_type ="
                        + " ? WHERE RTRIM(CAST(us_id AS VARCHAR)) = RTRIM(CAST(? AS VARCHAR))",
                v0,
                v1,
                v2,
                v3,
                v4);
    }

    /** ← EXEC CICS DELETE FILE RIDFLD */
    @Override
    public void delete(Object key) {
        String k = key == null ? "" : key.toString().trim();
        jdbc.update(
                "DELETE FROM usrsec WHERE RTRIM(CAST(us_id AS VARCHAR)) = RTRIM(CAST(? AS"
                        + " VARCHAR))",
                k);
    }

    /** ← EXEC CICS STARTBR FILE RIDFLD → SELECT WHERE pk >= ? ORDER BY pk */
    @Override
    public List<Object> startBrowse(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM STARTBR GTEQ with SPACES/LOW-VALUES ridfld: position at the first record
        if (k.isEmpty()) {
            return new ArrayList<>(jdbc.queryForList("SELECT * FROM usrsec ORDER BY us_id"));
        }
        return new ArrayList<>(
                jdbc.queryForList(
                        "SELECT * FROM usrsec WHERE RTRIM(CAST(us_id AS VARCHAR)) >= RTRIM(CAST(?"
                                + " AS VARCHAR)) ORDER BY us_id",
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
                        "SELECT * FROM usrsec WHERE RTRIM(CAST(us_id AS VARCHAR)) <= RTRIM(CAST(?"
                                + " AS VARCHAR)) ORDER BY us_id DESC",
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

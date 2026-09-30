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
 * CICS File DAO for CARDFILE (VSAM INDEXED). ← COBOL: EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR
 * FILE('CARDFILE') Table: cardfile, PK: (cd_num)
 */
@Repository("CARDFILE")
public class CardfileFileDao implements FileDao {
    @Autowired private JdbcTemplate jdbc;

    @Override
    public String getFileName() {
        return "CARDFILE";
    }

    /** ← EXEC CICS READ FILE RIDFLD */
    @Override
    public Object readByKey(Object key) {
        String k = key == null ? "" : key.toString().trim();
        try {
            return jdbc.queryForMap(
                    "SELECT * FROM cardfile WHERE RTRIM(CAST(cd_num AS VARCHAR)) = RTRIM(CAST(? AS"
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
                    "SELECT * FROM cardfile WHERE RTRIM(CAST(cd_num AS VARCHAR)) = RTRIM(CAST(? AS"
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
            Object v0 = getFieldValue(record, "cdNum", "CD-NUM");
            Object v1 = getFieldValue(record, "cdAcctId", "CD-ACCT-ID");
            Object v2 = getFieldValue(record, "cdCvv", "CD-CVV");
            Object v3 = getFieldValue(record, "cdEmbossedName", "CD-EMBOSSED-NAME");
            Object v4 = getFieldValue(record, "cdExpiryDate", "CD-EXPIRY-DATE");
            Object v5 = getFieldValue(record, "cdActiveStatus", "CD-ACTIVE-STATUS");
            jdbc.update(
                    "INSERT INTO cardfile (cd_num, cd_acct_id, cd_cvv, cd_embossed_name,"
                        + " cd_expiry_date, cd_active_status) VALUES (?, CAST(? AS NUMERIC), ?, ?,"
                        + " ?, ?)",
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
        Object v0 = getFieldValue(record, "cdAcctId", "CD-ACCT-ID");
        Object v1 = getFieldValue(record, "cdCvv", "CD-CVV");
        Object v2 = getFieldValue(record, "cdEmbossedName", "CD-EMBOSSED-NAME");
        Object v3 = getFieldValue(record, "cdExpiryDate", "CD-EXPIRY-DATE");
        Object v4 = getFieldValue(record, "cdActiveStatus", "CD-ACTIVE-STATUS");
        Object v5 = getFieldValue(record, "cdNum", "CD-NUM");
        jdbc.update(
                "UPDATE cardfile SET cd_acct_id = CAST(? AS NUMERIC), cd_cvv = ?, cd_embossed_name"
                    + " = ?, cd_expiry_date = ?, cd_active_status = ? WHERE RTRIM(CAST(cd_num AS"
                    + " VARCHAR)) = RTRIM(CAST(? AS VARCHAR))",
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
        jdbc.update(
                "DELETE FROM cardfile WHERE RTRIM(CAST(cd_num AS VARCHAR)) = RTRIM(CAST(? AS"
                        + " VARCHAR))",
                k);
    }

    /** ← EXEC CICS STARTBR FILE RIDFLD → SELECT WHERE pk >= ? ORDER BY pk */
    @Override
    public List<Object> startBrowse(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM STARTBR GTEQ with SPACES/LOW-VALUES ridfld: position at the first record
        if (k.isEmpty()) {
            return new ArrayList<>(jdbc.queryForList("SELECT * FROM cardfile ORDER BY cd_num"));
        }
        return new ArrayList<>(
                jdbc.queryForList(
                        "SELECT * FROM cardfile WHERE RTRIM(CAST(cd_num AS VARCHAR)) >="
                                + " RTRIM(CAST(? AS VARCHAR)) ORDER BY cd_num",
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
                        "SELECT * FROM cardfile WHERE RTRIM(CAST(cd_num AS VARCHAR)) <="
                                + " RTRIM(CAST(? AS VARCHAR)) ORDER BY cd_num DESC",
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

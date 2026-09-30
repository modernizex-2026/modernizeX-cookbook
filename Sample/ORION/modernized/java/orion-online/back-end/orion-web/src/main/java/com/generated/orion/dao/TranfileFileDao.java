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
 * CICS File DAO for TRANFILE (VSAM INDEXED). ← COBOL: EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR
 * FILE('TRANFILE') Table: tranfile, PK: (tr_id)
 */
@Repository("TRANFILE")
public class TranfileFileDao implements FileDao {
    @Autowired private JdbcTemplate jdbc;

    @Override
    public String getFileName() {
        return "TRANFILE";
    }

    /** ← EXEC CICS READ FILE RIDFLD */
    @Override
    public Object readByKey(Object key) {
        String k = key == null ? "" : key.toString().trim();
        try {
            return jdbc.queryForMap(
                    "SELECT * FROM tranfile WHERE RTRIM(CAST(tr_id AS VARCHAR)) = RTRIM(CAST(? AS"
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
                    "SELECT * FROM tranfile WHERE RTRIM(CAST(tr_id AS VARCHAR)) = RTRIM(CAST(? AS"
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
            Object v0 = getFieldValue(record, "trId", "TR-ID");
            Object v1 = getFieldValue(record, "trTypeCd", "TR-TYPE-CD");
            Object v2 = getFieldValue(record, "trCatCd", "TR-CAT-CD");
            Object v3 = getFieldValue(record, "trSource", "TR-SOURCE");
            Object v4 = getFieldValue(record, "trDesc", "TR-DESC");
            Object v5 = getFieldValue(record, "trAmt", "TR-AMT");
            Object v6 = getFieldValue(record, "trMerchantId", "TR-MERCHANT-ID");
            Object v7 = getFieldValue(record, "trMerchantName", "TR-MERCHANT-NAME");
            Object v8 = getFieldValue(record, "trMerchantCity", "TR-MERCHANT-CITY");
            Object v9 = getFieldValue(record, "trMerchantZip", "TR-MERCHANT-ZIP");
            Object v10 = getFieldValue(record, "trCardNum", "TR-CARD-NUM");
            Object v11 = getFieldValue(record, "trOrigTs", "TR-ORIG-TS");
            Object v12 = getFieldValue(record, "trProcTs", "TR-PROC-TS");
            jdbc.update(
                    "INSERT INTO tranfile (tr_id, tr_type_cd, tr_cat_cd, tr_source, tr_desc,"
                        + " tr_amt, tr_merchant_id, tr_merchant_name, tr_merchant_city,"
                        + " tr_merchant_zip, tr_card_num, tr_orig_ts, tr_proc_ts) VALUES (?, ?,"
                        + " CAST(? AS NUMERIC), ?, ?, CAST(? AS NUMERIC), CAST(? AS NUMERIC), ?, ?,"
                        + " ?, ?, ?, ?)",
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
                    v11,
                    v12);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException("");
        }
    }

    /** ← EXEC CICS REWRITE FILE FROM */
    @Override
    public void rewrite(Object record) {
        Object v0 = getFieldValue(record, "trTypeCd", "TR-TYPE-CD");
        Object v1 = getFieldValue(record, "trCatCd", "TR-CAT-CD");
        Object v2 = getFieldValue(record, "trSource", "TR-SOURCE");
        Object v3 = getFieldValue(record, "trDesc", "TR-DESC");
        Object v4 = getFieldValue(record, "trAmt", "TR-AMT");
        Object v5 = getFieldValue(record, "trMerchantId", "TR-MERCHANT-ID");
        Object v6 = getFieldValue(record, "trMerchantName", "TR-MERCHANT-NAME");
        Object v7 = getFieldValue(record, "trMerchantCity", "TR-MERCHANT-CITY");
        Object v8 = getFieldValue(record, "trMerchantZip", "TR-MERCHANT-ZIP");
        Object v9 = getFieldValue(record, "trCardNum", "TR-CARD-NUM");
        Object v10 = getFieldValue(record, "trOrigTs", "TR-ORIG-TS");
        Object v11 = getFieldValue(record, "trProcTs", "TR-PROC-TS");
        Object v12 = getFieldValue(record, "trId", "TR-ID");
        jdbc.update(
                "UPDATE tranfile SET tr_type_cd = ?, tr_cat_cd = CAST(? AS NUMERIC), tr_source = ?,"
                    + " tr_desc = ?, tr_amt = CAST(? AS NUMERIC), tr_merchant_id = CAST(? AS"
                    + " NUMERIC), tr_merchant_name = ?, tr_merchant_city = ?, tr_merchant_zip = ?,"
                    + " tr_card_num = ?, tr_orig_ts = ?, tr_proc_ts = ? WHERE RTRIM(CAST(tr_id AS"
                    + " VARCHAR)) = RTRIM(CAST(? AS VARCHAR))",
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
                v11,
                v12);
    }

    /** ← EXEC CICS DELETE FILE RIDFLD */
    @Override
    public void delete(Object key) {
        String k = key == null ? "" : key.toString().trim();
        jdbc.update(
                "DELETE FROM tranfile WHERE RTRIM(CAST(tr_id AS VARCHAR)) = RTRIM(CAST(? AS"
                        + " VARCHAR))",
                k);
    }

    /** ← EXEC CICS STARTBR FILE RIDFLD → SELECT WHERE pk >= ? ORDER BY pk */
    @Override
    public List<Object> startBrowse(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM STARTBR GTEQ with SPACES/LOW-VALUES ridfld: position at the first record
        if (k.isEmpty()) {
            return new ArrayList<>(jdbc.queryForList("SELECT * FROM tranfile ORDER BY tr_id"));
        }
        return new ArrayList<>(
                jdbc.queryForList(
                        "SELECT * FROM tranfile WHERE RTRIM(CAST(tr_id AS VARCHAR)) >= RTRIM(CAST(?"
                                + " AS VARCHAR)) ORDER BY tr_id",
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
                        "SELECT * FROM tranfile WHERE RTRIM(CAST(tr_id AS VARCHAR)) <= RTRIM(CAST(?"
                                + " AS VARCHAR)) ORDER BY tr_id DESC",
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

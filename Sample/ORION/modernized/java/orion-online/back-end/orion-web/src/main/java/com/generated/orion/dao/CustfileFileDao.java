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
 * CICS File DAO for CUSTFILE (VSAM INDEXED). ← COBOL: EXEC CICS READ/WRITE/REWRITE/DELETE/STARTBR
 * FILE('CUSTFILE') Table: custfile, PK: (cu_id)
 */
@Repository("CUSTFILE")
public class CustfileFileDao implements FileDao {
    @Autowired private JdbcTemplate jdbc;

    @Override
    public String getFileName() {
        return "CUSTFILE";
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
            return jdbc.queryForMap("SELECT * FROM custfile WHERE cu_id = CAST(? AS NUMERIC)", k);
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
                    "SELECT * FROM custfile WHERE cu_id = CAST(? AS NUMERIC) FOR UPDATE", k);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    /** ← EXEC CICS WRITE FILE FROM RIDFLD */
    @Override
    public void write(Object record, Object key) {
        try {
            Object v0 = getFieldValue(record, "cuId", "CU-ID");
            Object v1 = getFieldValue(record, "cuFirstName", "CU-FIRST-NAME");
            Object v2 = getFieldValue(record, "cuMiddleName", "CU-MIDDLE-NAME");
            Object v3 = getFieldValue(record, "cuLastName", "CU-LAST-NAME");
            Object v4 = getFieldValue(record, "cuAddrLine1", "CU-ADDR-LINE-1");
            Object v5 = getFieldValue(record, "cuAddrLine2", "CU-ADDR-LINE-2");
            Object v6 = getFieldValue(record, "cuAddrCity", "CU-ADDR-CITY");
            Object v7 = getFieldValue(record, "cuAddrState", "CU-ADDR-STATE");
            Object v8 = getFieldValue(record, "cuAddrCountry", "CU-ADDR-COUNTRY");
            Object v9 = getFieldValue(record, "cuAddrZip", "CU-ADDR-ZIP");
            Object v10 = getFieldValue(record, "cuPhone1", "CU-PHONE-1");
            Object v11 = getFieldValue(record, "cuPhone2", "CU-PHONE-2");
            Object v12 = getFieldValue(record, "cuSsn", "CU-SSN");
            Object v13 = getFieldValue(record, "cuGovtId", "CU-GOVT-ID");
            Object v14 = getFieldValue(record, "cuDob", "CU-DOB");
            Object v15 = getFieldValue(record, "cuFicoScore", "CU-FICO-SCORE");
            jdbc.update(
                    "INSERT INTO custfile (cu_id, cu_first_name, cu_middle_name, cu_last_name,"
                        + " cu_addr_line_1, cu_addr_line_2, cu_addr_city, cu_addr_state,"
                        + " cu_addr_country, cu_addr_zip, cu_phone_1, cu_phone_2, cu_ssn,"
                        + " cu_govt_id, cu_dob, cu_fico_score) VALUES (CAST(? AS NUMERIC), ?, ?, ?,"
                        + " ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS NUMERIC), ?, ?, CAST(? AS NUMERIC))",
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
                    v12,
                    v13,
                    v14,
                    v15);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException("");
        }
    }

    /** ← EXEC CICS REWRITE FILE FROM */
    @Override
    public void rewrite(Object record) {
        Object v0 = getFieldValue(record, "cuFirstName", "CU-FIRST-NAME");
        Object v1 = getFieldValue(record, "cuMiddleName", "CU-MIDDLE-NAME");
        Object v2 = getFieldValue(record, "cuLastName", "CU-LAST-NAME");
        Object v3 = getFieldValue(record, "cuAddrLine1", "CU-ADDR-LINE-1");
        Object v4 = getFieldValue(record, "cuAddrLine2", "CU-ADDR-LINE-2");
        Object v5 = getFieldValue(record, "cuAddrCity", "CU-ADDR-CITY");
        Object v6 = getFieldValue(record, "cuAddrState", "CU-ADDR-STATE");
        Object v7 = getFieldValue(record, "cuAddrCountry", "CU-ADDR-COUNTRY");
        Object v8 = getFieldValue(record, "cuAddrZip", "CU-ADDR-ZIP");
        Object v9 = getFieldValue(record, "cuPhone1", "CU-PHONE-1");
        Object v10 = getFieldValue(record, "cuPhone2", "CU-PHONE-2");
        Object v11 = getFieldValue(record, "cuSsn", "CU-SSN");
        Object v12 = getFieldValue(record, "cuGovtId", "CU-GOVT-ID");
        Object v13 = getFieldValue(record, "cuDob", "CU-DOB");
        Object v14 = getFieldValue(record, "cuFicoScore", "CU-FICO-SCORE");
        Object v15 = getFieldValue(record, "cuId", "CU-ID");
        jdbc.update(
                "UPDATE custfile SET cu_first_name = ?, cu_middle_name = ?, cu_last_name = ?,"
                    + " cu_addr_line_1 = ?, cu_addr_line_2 = ?, cu_addr_city = ?, cu_addr_state ="
                    + " ?, cu_addr_country = ?, cu_addr_zip = ?, cu_phone_1 = ?, cu_phone_2 = ?,"
                    + " cu_ssn = CAST(? AS NUMERIC), cu_govt_id = ?, cu_dob = ?, cu_fico_score ="
                    + " CAST(? AS NUMERIC) WHERE cu_id = CAST(? AS NUMERIC)",
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
                v12,
                v13,
                v14,
                v15);
    }

    /** ← EXEC CICS DELETE FILE RIDFLD */
    @Override
    public void delete(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM: SPACES/LOW-VALUES ridfld never equals a numeric key → nothing to delete
        if (k.isEmpty()) {
            return;
        }
        jdbc.update("DELETE FROM custfile WHERE cu_id = CAST(? AS NUMERIC)", k);
    }

    /** ← EXEC CICS STARTBR FILE RIDFLD → SELECT WHERE pk >= ? ORDER BY pk */
    @Override
    public List<Object> startBrowse(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM STARTBR GTEQ with SPACES/LOW-VALUES ridfld: position at the first record
        if (k.isEmpty()) {
            return new ArrayList<>(jdbc.queryForList("SELECT * FROM custfile ORDER BY cu_id"));
        }
        return new ArrayList<>(
                jdbc.queryForList(
                        "SELECT * FROM custfile WHERE cu_id >= CAST(? AS NUMERIC) ORDER BY cu_id",
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
                        "SELECT * FROM custfile WHERE cu_id <= CAST(? AS NUMERIC) ORDER BY cu_id"
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

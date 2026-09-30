package com.generated.orion.dao;

import com.appruntime.FileDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * AIX DAO for TRANCARD → queries main table tranfile by tr_card_num. VSAM alternate index: same
 * data as tranfile, different access key.
 */
@Repository("TRANCARD")
public class TrancardFileDao implements FileDao {
    @Autowired private JdbcTemplate jdbc;

    @Override
    public String getFileName() {
        return "TRANCARD";
    }

    /** AIX read: SELECT * FROM tranfile WHERE tr_card_num = ? */
    @Override
    public Object readByKey(Object key) {
        String k = key == null ? "" : key.toString().trim();
        try {
            return jdbc.queryForMap(
                    "SELECT * FROM tranfile WHERE RTRIM(CAST(tr_card_num AS VARCHAR)) ="
                            + " RTRIM(CAST(? AS VARCHAR))",
                    k);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Override
    public Object readByKeyForUpdate(Object key) {
        String k = key == null ? "" : key.toString().trim();
        try {
            return jdbc.queryForMap(
                    "SELECT * FROM tranfile WHERE RTRIM(CAST(tr_card_num AS VARCHAR)) ="
                            + " RTRIM(CAST(? AS VARCHAR)) FOR UPDATE",
                    k);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Override
    public void write(Object record, Object key) {
        throw new UnsupportedOperationException(
                "AIX TRANCARD is read-only — write to main file tranfile");
    }

    @Override
    public void rewrite(Object record) {
        throw new UnsupportedOperationException(
                "AIX TRANCARD is read-only — rewrite on main file tranfile");
    }

    /** ← EXEC CICS DELETE FILE RIDFLD */
    @Override
    public void delete(Object key) {
        String k = key == null ? "" : key.toString().trim();
        jdbc.update(
                "DELETE FROM tranfile WHERE RTRIM(CAST(tr_card_num AS VARCHAR)) = RTRIM(CAST(? AS"
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
                    jdbc.queryForList("SELECT * FROM tranfile ORDER BY tr_card_num LIMIT 1000"));
        }
        return new ArrayList<>(
                jdbc.queryForList(
                        "SELECT * FROM tranfile WHERE RTRIM(CAST(tr_card_num AS VARCHAR)) >="
                                + " RTRIM(CAST(? AS VARCHAR)) ORDER BY tr_card_num LIMIT 1000",
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
                        "SELECT * FROM tranfile WHERE RTRIM(CAST(tr_card_num AS VARCHAR)) <="
                                + " RTRIM(CAST(? AS VARCHAR)) ORDER BY tr_card_num DESC LIMIT 1000",
                        k));
    }
}

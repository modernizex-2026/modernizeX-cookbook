package com.generated.orion.dao;

import com.appruntime.FileDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * AIX DAO for XREFACCT → queries main table xreffile by xr_acct_id. VSAM alternate index: same data
 * as xreffile, different access key.
 */
@Repository("XREFACCT")
public class XrefacctFileDao implements FileDao {
    @Autowired private JdbcTemplate jdbc;

    @Override
    public String getFileName() {
        return "XREFACCT";
    }

    /** AIX read: SELECT * FROM xreffile WHERE xr_acct_id = ? */
    @Override
    public Object readByKey(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM: SPACES/LOW-VALUES ridfld never equals a numeric key → NOTFND
        if (k.isEmpty()) {
            return null;
        }
        try {
            return jdbc.queryForMap(
                    "SELECT * FROM xreffile WHERE xr_acct_id = CAST(? AS NUMERIC)", k);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Override
    public Object readByKeyForUpdate(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM: SPACES/LOW-VALUES ridfld never equals a numeric key → NOTFND
        if (k.isEmpty()) {
            return null;
        }
        try {
            return jdbc.queryForMap(
                    "SELECT * FROM xreffile WHERE xr_acct_id = CAST(? AS NUMERIC) FOR UPDATE", k);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Override
    public void write(Object record, Object key) {
        throw new UnsupportedOperationException(
                "AIX XREFACCT is read-only — write to main file xreffile");
    }

    @Override
    public void rewrite(Object record) {
        throw new UnsupportedOperationException(
                "AIX XREFACCT is read-only — rewrite on main file xreffile");
    }

    /** ← EXEC CICS DELETE FILE RIDFLD */
    @Override
    public void delete(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM: SPACES/LOW-VALUES ridfld never equals a numeric key → nothing to delete
        if (k.isEmpty()) {
            return;
        }
        jdbc.update("DELETE FROM xreffile WHERE xr_acct_id = CAST(? AS NUMERIC)", k);
    }

    /** ← EXEC CICS STARTBR FILE RIDFLD → SELECT WHERE pk >= ? ORDER BY pk */
    @Override
    public List<Object> startBrowse(Object key) {
        String k = key == null ? "" : key.toString().trim();
        // VSAM STARTBR GTEQ with SPACES/LOW-VALUES ridfld: position at the first record
        if (k.isEmpty()) {
            return new ArrayList<>(
                    jdbc.queryForList("SELECT * FROM xreffile ORDER BY xr_acct_id LIMIT 1000"));
        }
        return new ArrayList<>(
                jdbc.queryForList(
                        "SELECT * FROM xreffile WHERE xr_acct_id >= CAST(? AS NUMERIC) ORDER BY"
                                + " xr_acct_id LIMIT 1000",
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
                        "SELECT * FROM xreffile WHERE xr_acct_id <= CAST(? AS NUMERIC) ORDER BY"
                                + " xr_acct_id DESC LIMIT 1000",
                        k));
    }
}

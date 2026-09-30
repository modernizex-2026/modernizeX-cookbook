package com.generated.orion.odacctu.dao.impl;

import com.generated.orion.odacctu.dao.OdacctuDao;
import com.generated.orion.odacctu.support.SqlStatements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * JdbcTemplate implementation for ODACCTU DAO. ← EXEC SQL — uses JdbcTemplate with SqlStatements
 * constants.
 */
@Repository
public class OdacctuDaoImpl implements OdacctuDao {
    @Autowired private JdbcTemplate jdbcTemplate;

    /**
     * ← SQL_001: SELECT AC_ACTIVE_STATUS, AC_CREDIT_LIMIT, AC_CASH_LIMIT, AC_EXPIRY_DATE,
     * AC_GROU...
     */
    @Override
    public Map<String, Object> selectOrionAcct(Object... params) {
        try {
            return jdbcTemplate.queryForMap(SqlStatements.SQL_001, params);
        } catch (EmptyResultDataAccessException ex) {
            Map<String, Object> result = new HashMap<>();
            result.put("SQLCODE", 100);
            return result;
        }
    }

    /**
     * ← SQL_002: UPDATE ORION.ACCT SET AC_ACTIVE_STATUS = ?, AC_CREDIT_LIMIT = ?, AC_CASH_LIMIT ...
     */
    @Override
    public Map<String, Object> updateOrionAcct(Object... params) {
        try {
            int affected = jdbcTemplate.update(SqlStatements.SQL_002, params);
            Map<String, Object> result = new HashMap<>();
            result.put("SQLCODE", affected == 0 ? 100 : 0);
            result.put("AFFECTED_ROWS", affected);
            return result;
        } catch (DataAccessException ex) {
            return sqlErrorResult(sqlCodeFor(ex));
        }
    }

    /**
     * DB2 SQLCODE from standard SQLSTATE of the root SQLException. 23505→-803 unique · 23502→-407
     * not-null · 23503→-530 FK · 23513/23514→-545 check · else -904 (generic error).
     */
    private static int sqlCodeFor(DataAccessException ex) {
        Throwable c = ex.getMostSpecificCause();
        String state = c instanceof SQLException ? ((SQLException) c).getSQLState() : null;
        if (state == null) {
            return -904;
        }
        switch (state) {
            case "23505":
                return -803;
            case "23502":
                return -407;
            case "23503":
                return -530;
            case "23513":
            case "23514":
                return -545;
            default:
                return -904;
        }
    }

    /** DB2-style error result — SQLCODE set, no rows affected. */
    private static Map<String, Object> sqlErrorResult(int sqlcode) {
        Map<String, Object> result = new HashMap<>();
        result.put("SQLCODE", sqlcode);
        result.put("AFFECTED_ROWS", 0);
        return result;
    }
}

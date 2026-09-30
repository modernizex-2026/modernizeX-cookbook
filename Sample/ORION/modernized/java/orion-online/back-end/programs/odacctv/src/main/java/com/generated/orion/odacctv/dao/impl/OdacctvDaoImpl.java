package com.generated.orion.odacctv.dao.impl;

import com.generated.orion.odacctv.dao.OdacctvDao;
import com.generated.orion.odacctv.support.SqlStatements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * JdbcTemplate implementation for ODACCTV DAO. ← EXEC SQL — uses JdbcTemplate with SqlStatements
 * constants.
 */
@Repository
public class OdacctvDaoImpl implements OdacctvDao {
    @Autowired private JdbcTemplate jdbcTemplate;

    /**
     * ← SQL_001: SELECT AC_ACTIVE_STATUS, AC_CURR_BAL, AC_CREDIT_LIMIT, AC_CASH_LIMIT,
     * AC_OPEN_DA...
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
}

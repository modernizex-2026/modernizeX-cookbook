package com.generated.orion.odcustv.dao.impl;

import com.generated.orion.odcustv.dao.OdcustvDao;
import com.generated.orion.odcustv.support.SqlStatements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * JdbcTemplate implementation for ODCUSTV DAO. ← EXEC SQL — uses JdbcTemplate with SqlStatements
 * constants.
 */
@Repository
public class OdcustvDaoImpl implements OdcustvDao {
    @Autowired private JdbcTemplate jdbcTemplate;

    /**
     * ← SQL_001: SELECT CU_FIRST_NAME, CU_MIDDLE_NAME, CU_LAST_NAME, CU_ADDR_LINE_1,
     * CU_ADDR_CITY...
     */
    @Override
    public Map<String, Object> selectOrionCust(Object... params) {
        try {
            return jdbcTemplate.queryForMap(SqlStatements.SQL_001, params);
        } catch (EmptyResultDataAccessException ex) {
            Map<String, Object> result = new HashMap<>();
            result.put("SQLCODE", 100);
            return result;
        }
    }
}

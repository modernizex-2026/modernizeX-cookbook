package com.generated.orion.odcardv.dao.impl;

import com.generated.orion.odcardv.dao.OdcardvDao;
import com.generated.orion.odcardv.support.SqlStatements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * JdbcTemplate implementation for ODCARDV DAO. ← EXEC SQL — uses JdbcTemplate with SqlStatements
 * constants.
 */
@Repository
public class OdcardvDaoImpl implements OdcardvDao {
    @Autowired private JdbcTemplate jdbcTemplate;

    /**
     * ← SQL_001: SELECT CD_ACCT_ID, CD_EMBOSSED_NAME, CD_EXPIRY_DATE, CD_ACTIVE_STATUS FROM
     * ORION...
     */
    @Override
    public Map<String, Object> selectOrionCard(Object... params) {
        try {
            return jdbcTemplate.queryForMap(SqlStatements.SQL_001, params);
        } catch (EmptyResultDataAccessException ex) {
            Map<String, Object> result = new HashMap<>();
            result.put("SQLCODE", 100);
            return result;
        }
    }
}

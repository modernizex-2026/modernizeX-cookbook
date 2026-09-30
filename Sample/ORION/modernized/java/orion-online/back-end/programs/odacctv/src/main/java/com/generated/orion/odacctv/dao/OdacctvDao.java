package com.generated.orion.odacctv.dao;

import java.util.Map;

/**
 * Data access interface for ODACCTV. ← COBOL: EXEC SQL operations (DB2) Implementation:
 * OdacctvDaoImpl
 */
public interface OdacctvDao {
    /**
     * ← SQL_001: SELECT AC_ACTIVE_STATUS, AC_CURR_BAL, AC_CREDIT_LIMIT, AC_CASH_LIMIT,
     * AC_OPEN_DA...
     */
    Map<String, Object> selectOrionAcct(Object... params);
}

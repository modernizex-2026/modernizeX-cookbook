package com.generated.orion.odacctu.dao;

import java.util.Map;

/**
 * Data access interface for ODACCTU. ← COBOL: EXEC SQL operations (DB2) Implementation:
 * OdacctuDaoImpl
 */
public interface OdacctuDao {
    /**
     * ← SQL_001: SELECT AC_ACTIVE_STATUS, AC_CREDIT_LIMIT, AC_CASH_LIMIT, AC_EXPIRY_DATE,
     * AC_GROU...
     */
    Map<String, Object> selectOrionAcct(Object... params);

    /**
     * ← SQL_002: UPDATE ORION.ACCT SET AC_ACTIVE_STATUS = ?, AC_CREDIT_LIMIT = ?, AC_CASH_LIMIT ...
     */
    Map<String, Object> updateOrionAcct(Object... params);
}

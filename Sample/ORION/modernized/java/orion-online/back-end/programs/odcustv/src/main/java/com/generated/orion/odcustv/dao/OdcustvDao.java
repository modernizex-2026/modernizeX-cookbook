package com.generated.orion.odcustv.dao;

import java.util.Map;

/**
 * Data access interface for ODCUSTV. ← COBOL: EXEC SQL operations (DB2) Implementation:
 * OdcustvDaoImpl
 */
public interface OdcustvDao {
    /**
     * ← SQL_001: SELECT CU_FIRST_NAME, CU_MIDDLE_NAME, CU_LAST_NAME, CU_ADDR_LINE_1,
     * CU_ADDR_CITY...
     */
    Map<String, Object> selectOrionCust(Object... params);
}

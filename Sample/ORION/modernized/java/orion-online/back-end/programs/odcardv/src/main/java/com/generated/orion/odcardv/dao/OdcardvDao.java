package com.generated.orion.odcardv.dao;

import java.util.Map;

/**
 * Data access interface for ODCARDV. ← COBOL: EXEC SQL operations (DB2) Implementation:
 * OdcardvDaoImpl
 */
public interface OdcardvDao {
    /**
     * ← SQL_001: SELECT CD_ACCT_ID, CD_EMBOSSED_NAME, CD_EXPIRY_DATE, CD_ACTIVE_STATUS FROM
     * ORION...
     */
    Map<String, Object> selectOrionCard(Object... params);
}

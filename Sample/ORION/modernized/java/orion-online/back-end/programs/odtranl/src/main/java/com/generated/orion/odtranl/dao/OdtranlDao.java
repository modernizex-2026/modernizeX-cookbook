package com.generated.orion.odtranl.dao;

import java.util.Map;

/**
 * Data access interface for ODTRANL. ← COBOL: EXEC SQL operations (DB2) Implementation:
 * OdtranlDaoImpl
 */
public interface OdtranlDao {
    /**
     * ← SQL_001: SELECT TR_ID, TR_AMT, TR_TYPE_CD FROM ORION.TRAN WHERE TR_CARD_NUM = ? ORDER BY
     * ...
     */
    Map<String, Object> openTrancsr(Object... params);

    /** ← SQL_002: CLOSE TRANCSR (cursor state in DAO) */
    Map<String, Object> closeTrancsr(Object... params);

    /** ← SQL_003: FETCH TRANCSR (cursor state in DAO) */
    Map<String, Object> fetchTrancsr(Object... params);
}

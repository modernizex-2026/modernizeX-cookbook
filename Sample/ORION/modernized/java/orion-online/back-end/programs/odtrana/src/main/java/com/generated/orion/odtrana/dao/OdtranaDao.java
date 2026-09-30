package com.generated.orion.odtrana.dao;

import java.util.Map;

/**
 * Data access interface for ODTRANA. ← COBOL: EXEC SQL operations (DB2) Implementation:
 * OdtranaDaoImpl
 */
public interface OdtranaDao {
    /** ← SQL_001: SELECT CD_ACTIVE_STATUS FROM ORION.CARD WHERE CD_NUM = ? */
    Map<String, Object> selectOrionCard(Object... params);

    /** ← SQL_002: SELECT TT_DESC FROM ORION.TTYP WHERE TT_CD = ? */
    Map<String, Object> selectOrionTtyp(Object... params);

    /** ← SQL_003: SELECT CT_LAST_VALUE FROM ORION.CTRL WHERE CT_KEY = ? */
    Map<String, Object> selectOrionCtrl(Object... params);

    /** ← SQL_004: UPDATE ORION.CTRL SET CT_LAST_VALUE = ? WHERE CT_KEY = ? */
    Map<String, Object> updateOrionCtrl(Object... params);

    /**
     * ← SQL_005: INSERT INTO ORION.TRAN ( TR_ID, TR_TYPE_CD, TR_CAT_CD, TR_SOURCE, TR_DESC,
     * TR_AM...
     */
    Map<String, Object> insertOrionTran(Object... params);
}

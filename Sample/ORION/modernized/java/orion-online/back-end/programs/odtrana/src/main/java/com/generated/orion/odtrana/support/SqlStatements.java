package com.generated.orion.odtrana.support;

/**
 * SQL statement constants extracted from EXEC SQL blocks (DB2). Generated — do not edit manually.
 */
public final class SqlStatements {
    /** ← selectOrionCard (SELECT_INTO) */
    public static final String SQL_001 = "SELECT CD_ACTIVE_STATUS FROM ORION.CARD WHERE CD_NUM = ?";

    /** ← selectOrionTtyp (SELECT_INTO) */
    public static final String SQL_002 = "SELECT TT_DESC FROM ORION.TTYP WHERE TT_CD = ?";

    /** ← selectOrionCtrl (SELECT_INTO) */
    public static final String SQL_003 = "SELECT CT_LAST_VALUE FROM ORION.CTRL WHERE CT_KEY = ?";

    /** ← updateOrionCtrl (UPDATE) */
    public static final String SQL_004 = "UPDATE ORION.CTRL SET CT_LAST_VALUE = ? WHERE CT_KEY = ?";

    /** ← insertOrionTran (INSERT) */
    public static final String SQL_005 =
            "INSERT INTO ORION.TRAN ( TR_ID, TR_TYPE_CD, TR_CAT_CD, TR_SOURCE, TR_DESC, TR_AMT,"
                + " TR_MERCHANT_ID, TR_MERCHANT_NAME, TR_MERCHANT_CITY, TR_MERCHANT_ZIP,"
                + " TR_CARD_NUM, TR_ORIG_TS, TR_PROC_TS ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
                + " ?, ? )";

    private SqlStatements() {}
}

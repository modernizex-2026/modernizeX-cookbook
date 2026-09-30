package com.generated.orion.odtranl.support;

/**
 * SQL statement constants extracted from EXEC SQL blocks (DB2). Generated — do not edit manually.
 */
public final class SqlStatements {
    /** ← openTrancsr (OPEN_CURSOR) */
    public static final String SQL_001 =
            "SELECT TR_ID, TR_AMT, TR_TYPE_CD FROM ORION.TRAN WHERE TR_CARD_NUM = ? ORDER BY TR_ID";

    /** ← closeTrancsr (CLOSE_CURSOR) */
    public static final String SQL_002 = "CLOSE TRANCSR (cursor state in DAO)";

    /** ← fetchTrancsr (FETCH) */
    public static final String SQL_003 = "FETCH TRANCSR (cursor state in DAO)";

    private SqlStatements() {}
}

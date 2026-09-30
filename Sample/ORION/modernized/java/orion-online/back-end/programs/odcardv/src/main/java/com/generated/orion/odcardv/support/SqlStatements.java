package com.generated.orion.odcardv.support;

/**
 * SQL statement constants extracted from EXEC SQL blocks (DB2). Generated — do not edit manually.
 */
public final class SqlStatements {
    /** ← selectOrionCard (SELECT_INTO) */
    public static final String SQL_001 =
            "SELECT CD_ACCT_ID, CD_EMBOSSED_NAME, CD_EXPIRY_DATE, CD_ACTIVE_STATUS FROM ORION.CARD"
                    + " WHERE CD_NUM = ?";

    private SqlStatements() {}
}

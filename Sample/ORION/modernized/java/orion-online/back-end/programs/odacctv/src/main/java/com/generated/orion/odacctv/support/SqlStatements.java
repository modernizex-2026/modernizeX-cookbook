package com.generated.orion.odacctv.support;

/**
 * SQL statement constants extracted from EXEC SQL blocks (DB2). Generated — do not edit manually.
 */
public final class SqlStatements {
    /** ← selectOrionAcct (SELECT_INTO) */
    public static final String SQL_001 =
            "SELECT AC_ACTIVE_STATUS, AC_CURR_BAL, AC_CREDIT_LIMIT, AC_CASH_LIMIT, AC_OPEN_DATE,"
                    + " AC_EXPIRY_DATE, AC_GROUP_ID FROM ORION.ACCT WHERE AC_ID = ?";

    private SqlStatements() {}
}

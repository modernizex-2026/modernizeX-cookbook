package com.generated.orion.odacctu.support;

/**
 * SQL statement constants extracted from EXEC SQL blocks (DB2). Generated — do not edit manually.
 */
public final class SqlStatements {
    /** ← selectOrionAcct (SELECT_INTO) */
    public static final String SQL_001 =
            "SELECT AC_ACTIVE_STATUS, AC_CREDIT_LIMIT, AC_CASH_LIMIT, AC_EXPIRY_DATE, AC_GROUP_ID"
                    + " FROM ORION.ACCT WHERE AC_ID = ?";

    /** ← updateOrionAcct (UPDATE) */
    public static final String SQL_002 =
            "UPDATE ORION.ACCT SET AC_ACTIVE_STATUS = ?, AC_CREDIT_LIMIT  = ?, AC_CASH_LIMIT    ="
                    + " ?, AC_EXPIRY_DATE   = ?, AC_GROUP_ID      = ? WHERE AC_ID = ?";

    private SqlStatements() {}
}

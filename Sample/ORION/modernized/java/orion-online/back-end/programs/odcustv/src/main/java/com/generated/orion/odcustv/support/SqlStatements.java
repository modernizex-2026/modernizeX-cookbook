package com.generated.orion.odcustv.support;

/**
 * SQL statement constants extracted from EXEC SQL blocks (DB2). Generated — do not edit manually.
 */
public final class SqlStatements {
    /** ← selectOrionCust (SELECT_INTO) */
    public static final String SQL_001 =
            "SELECT CU_FIRST_NAME, CU_MIDDLE_NAME, CU_LAST_NAME, CU_ADDR_LINE_1, CU_ADDR_CITY,"
                    + " CU_PHONE_1, CU_FICO_SCORE FROM ORION.CUST WHERE CU_ID = ?";

    private SqlStatements() {}
}

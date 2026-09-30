-- SQL queries for ODACCTV
-- Generated from EXEC SQL blocks (DB2).
-- For DBA reference. Runtime SQL is in SqlStatements.java.

-- SQL_001 (selectOrionAcct)
SELECT AC_ACTIVE_STATUS, AC_CURR_BAL, AC_CREDIT_LIMIT, AC_CASH_LIMIT, AC_OPEN_DATE, AC_EXPIRY_DATE, AC_GROUP_ID FROM ORION.ACCT WHERE AC_ID = ?;


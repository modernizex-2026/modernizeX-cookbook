-- SQL queries for ODACCTU
-- Generated from EXEC SQL blocks (DB2).
-- For DBA reference. Runtime SQL is in SqlStatements.java.

-- SQL_001 (selectOrionAcct)
SELECT AC_ACTIVE_STATUS, AC_CREDIT_LIMIT, AC_CASH_LIMIT, AC_EXPIRY_DATE, AC_GROUP_ID FROM ORION.ACCT WHERE AC_ID = ?;

-- SQL_002 (updateOrionAcct)
UPDATE ORION.ACCT SET AC_ACTIVE_STATUS = ?, AC_CREDIT_LIMIT  = ?, AC_CASH_LIMIT    = ?, AC_EXPIRY_DATE   = ?, AC_GROUP_ID      = ? WHERE AC_ID = ?;


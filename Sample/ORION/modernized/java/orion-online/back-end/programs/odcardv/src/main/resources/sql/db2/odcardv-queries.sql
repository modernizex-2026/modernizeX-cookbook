-- SQL queries for ODCARDV
-- Generated from EXEC SQL blocks (DB2).
-- For DBA reference. Runtime SQL is in SqlStatements.java.

-- SQL_001 (selectOrionCard)
SELECT CD_ACCT_ID, CD_EMBOSSED_NAME, CD_EXPIRY_DATE, CD_ACTIVE_STATUS FROM ORION.CARD WHERE CD_NUM = ?;


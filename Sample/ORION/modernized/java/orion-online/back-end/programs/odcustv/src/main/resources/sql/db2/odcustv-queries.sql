-- SQL queries for ODCUSTV
-- Generated from EXEC SQL blocks (DB2).
-- For DBA reference. Runtime SQL is in SqlStatements.java.

-- SQL_001 (selectOrionCust)
SELECT CU_FIRST_NAME, CU_MIDDLE_NAME, CU_LAST_NAME, CU_ADDR_LINE_1, CU_ADDR_CITY, CU_PHONE_1, CU_FICO_SCORE FROM ORION.CUST WHERE CU_ID = ?;


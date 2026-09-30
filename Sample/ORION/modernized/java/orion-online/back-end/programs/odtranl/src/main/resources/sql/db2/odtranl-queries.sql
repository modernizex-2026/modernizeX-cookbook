-- SQL queries for ODTRANL
-- Generated from EXEC SQL blocks (DB2).
-- For DBA reference. Runtime SQL is in SqlStatements.java.

-- SQL_001 (openTrancsr)
SELECT TR_ID, TR_AMT, TR_TYPE_CD FROM ORION.TRAN WHERE TR_CARD_NUM = ? ORDER BY TR_ID;

-- SQL_002 (closeTrancsr)
CLOSE TRANCSR (cursor state in DAO);

-- SQL_003 (fetchTrancsr)
FETCH TRANCSR (cursor state in DAO);


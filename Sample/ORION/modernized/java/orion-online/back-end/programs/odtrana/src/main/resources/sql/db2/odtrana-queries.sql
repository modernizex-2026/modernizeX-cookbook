-- SQL queries for ODTRANA
-- Generated from EXEC SQL blocks (DB2).
-- For DBA reference. Runtime SQL is in SqlStatements.java.

-- SQL_001 (selectOrionCard)
SELECT CD_ACTIVE_STATUS FROM ORION.CARD WHERE CD_NUM = ?;

-- SQL_002 (selectOrionTtyp)
SELECT TT_DESC FROM ORION.TTYP WHERE TT_CD = ?;

-- SQL_003 (selectOrionCtrl)
SELECT CT_LAST_VALUE FROM ORION.CTRL WHERE CT_KEY = ?;

-- SQL_004 (updateOrionCtrl)
UPDATE ORION.CTRL SET CT_LAST_VALUE = ? WHERE CT_KEY = ?;

-- SQL_005 (insertOrionTran)
INSERT INTO ORION.TRAN ( TR_ID, TR_TYPE_CD, TR_CAT_CD, TR_SOURCE, TR_DESC, TR_AMT, TR_MERCHANT_ID, TR_MERCHANT_NAME, TR_MERCHANT_CITY, TR_MERCHANT_ZIP, TR_CARD_NUM, TR_ORIG_TS, TR_PROC_TS ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? );


-- VSAM File → RDBMS Schema (auto-generated)
-- Source: FileDaoGenerator from FileSymbol IR

-- ← VSAM: CUSTFILE (INDEXED)
CREATE TABLE IF NOT EXISTS custfile (
    cu_id INTEGER NOT NULL,  -- PIC 9(09)
    cu_first_name VARCHAR(25),  -- PIC X(25)
    cu_middle_name VARCHAR(25),  -- PIC X(25)
    cu_last_name VARCHAR(25),  -- PIC X(25)
    cu_addr_line_1 VARCHAR(50),  -- PIC X(50)
    cu_addr_line_2 VARCHAR(50),  -- PIC X(50)
    cu_addr_city VARCHAR(50),  -- PIC X(50)
    cu_addr_state VARCHAR(2),  -- PIC X(02)
    cu_addr_country VARCHAR(3),  -- PIC X(03)
    cu_addr_zip VARCHAR(10),  -- PIC X(10)
    cu_phone_1 VARCHAR(15),  -- PIC X(15)
    cu_phone_2 VARCHAR(15),  -- PIC X(15)
    cu_ssn INTEGER,  -- PIC 9(09)
    cu_govt_id VARCHAR(20),  -- PIC X(20)
    cu_dob VARCHAR(10),  -- PIC X(10)
    cu_fico_score SMALLINT,  -- PIC 9(03)
    PRIMARY KEY (cu_id)
);

-- ← VSAM: ACCTFILE (INDEXED)
CREATE TABLE IF NOT EXISTS acctfile (
    ac_id BIGINT NOT NULL,  -- PIC 9(11)
    ac_active_status VARCHAR(1),  -- PIC X(01)
    ac_curr_bal DECIMAL(12,2),  -- PIC S9(10)V99
    ac_credit_limit DECIMAL(12,2),  -- PIC S9(10)V99
    ac_cash_limit DECIMAL(12,2),  -- PIC S9(10)V99
    ac_open_date VARCHAR(10),  -- PIC X(10)
    ac_expiry_date VARCHAR(10),  -- PIC X(10)
    ac_reissue_date VARCHAR(10),  -- PIC X(10)
    ac_cyc_credit DECIMAL(12,2),  -- PIC S9(10)V99
    ac_cyc_debit DECIMAL(12,2),  -- PIC S9(10)V99
    ac_addr_zip VARCHAR(10),  -- PIC X(10)
    ac_group_id VARCHAR(10),  -- PIC X(10)
    PRIMARY KEY (ac_id)
);

-- ← VSAM: CTRLFILE (INDEXED)
CREATE TABLE IF NOT EXISTS ctrlfile (
    ct_key VARCHAR(8) NOT NULL,  -- PIC X(08)
    ct_last_value BIGINT,  -- PIC 9(11)
    ct_desc VARCHAR(30),  -- PIC X(30)
    PRIMARY KEY (ct_key)
);

-- ← VSAM: CARDFILE (INDEXED)
CREATE TABLE IF NOT EXISTS cardfile (
    cd_num VARCHAR(16) NOT NULL,  -- PIC X(16)
    cd_acct_id BIGINT,  -- PIC 9(11)
    cd_cvv VARCHAR(3),  -- PIC X(03)
    cd_embossed_name VARCHAR(50),  -- PIC X(50)
    cd_expiry_date VARCHAR(10),  -- PIC X(10)
    cd_active_status VARCHAR(1),  -- PIC X(01)
    PRIMARY KEY (cd_num)
);

-- ← VSAM: XREFFILE (INDEXED)
CREATE TABLE IF NOT EXISTS xreffile (
    xr_card_num VARCHAR(16) NOT NULL,  -- PIC X(16)
    xr_acct_id BIGINT,  -- PIC 9(11)
    xr_cust_id INTEGER,  -- PIC 9(09)
    PRIMARY KEY (xr_card_num)
);

-- ← VSAM: DGRPFILE (INDEXED)
CREATE TABLE IF NOT EXISTS dgrpfile (
    dg_acct_group VARCHAR(10) NOT NULL,  -- PIC X(10)
    dg_type_cd VARCHAR(2) NOT NULL,  -- PIC X(02)
    dg_cat_cd SMALLINT NOT NULL,  -- PIC 9(04)
    dg_int_rate DECIMAL(6,2),  -- PIC S9(04)V99
    PRIMARY KEY (dg_acct_group, dg_type_cd, dg_cat_cd)
);

-- ← VSAM: USRSEC (INDEXED)
CREATE TABLE IF NOT EXISTS usrsec (
    us_id VARCHAR(8) NOT NULL,  -- PIC X(08)
    us_first_name VARCHAR(20),  -- PIC X(20)
    us_last_name VARCHAR(20),  -- PIC X(20)
    us_password VARCHAR(8),  -- PIC X(08)
    us_type VARCHAR(1),  -- PIC X(01)
    PRIMARY KEY (us_id)
);

-- ← VSAM: BILLFILE (INDEXED)
CREATE TABLE IF NOT EXISTS billfile (
    bl_id BIGINT NOT NULL,  -- PIC 9(11)
    bl_acct_id BIGINT,  -- PIC 9(11)
    bl_amount DECIMAL(12,2),  -- PIC S9(10)V99
    bl_pay_date VARCHAR(10),  -- PIC X(10)
    bl_confirm_num VARCHAR(16),  -- PIC X(16)
    bl_status VARCHAR(1),  -- PIC X(01)
    PRIMARY KEY (bl_id)
);

-- ← VSAM: TRANFILE (INDEXED)
CREATE TABLE IF NOT EXISTS tranfile (
    tr_id VARCHAR(16) NOT NULL,  -- PIC X(16)
    tr_type_cd VARCHAR(2),  -- PIC X(02)
    tr_cat_cd SMALLINT,  -- PIC 9(04)
    tr_source VARCHAR(10),  -- PIC X(10)
    tr_desc VARCHAR(100),  -- PIC X(100)
    tr_amt DECIMAL(11,2),  -- PIC S9(09)V99
    tr_merchant_id INTEGER,  -- PIC 9(09)
    tr_merchant_name VARCHAR(50),  -- PIC X(50)
    tr_merchant_city VARCHAR(50),  -- PIC X(50)
    tr_merchant_zip VARCHAR(10),  -- PIC X(10)
    tr_card_num VARCHAR(16),  -- PIC X(16)
    tr_orig_ts VARCHAR(26),  -- PIC X(26)
    tr_proc_ts VARCHAR(26),  -- PIC X(26)
    PRIMARY KEY (tr_id)
);

-- ← VSAM: STMTFILE (INDEXED)
CREATE TABLE IF NOT EXISTS stmtfile (
    st_acct_id BIGINT NOT NULL,  -- PIC 9(11)
    st_cycle INTEGER NOT NULL,  -- PIC 9(06)
    st_open_bal DECIMAL(12,2),  -- PIC S9(10)V99
    st_close_bal DECIMAL(12,2),  -- PIC S9(10)V99
    st_total_credit DECIMAL(12,2),  -- PIC S9(10)V99
    st_total_debit DECIMAL(12,2),  -- PIC S9(10)V99
    st_min_due DECIMAL(12,2),  -- PIC S9(10)V99
    st_due_date VARCHAR(10),  -- PIC X(10)
    PRIMARY KEY (st_acct_id, st_cycle)
);

-- ← VSAM: TCATFILE (INDEXED)
CREATE TABLE IF NOT EXISTS tcatfile (
    tc_type_cd VARCHAR(2) NOT NULL,  -- PIC X(02)
    tc_cd SMALLINT NOT NULL,  -- PIC 9(04)
    tc_desc VARCHAR(50),  -- PIC X(50)
    PRIMARY KEY (tc_type_cd, tc_cd)
);

-- ← VSAM: TTYPFILE (INDEXED)
CREATE TABLE IF NOT EXISTS ttypfile (
    tt_cd VARCHAR(2) NOT NULL,  -- PIC X(02)
    tt_desc VARCHAR(50),  -- PIC X(50)
    PRIMARY KEY (tt_cd)
);

-- ← VSAM: IMPFILE (INDEXED)
CREATE TABLE IF NOT EXISTS impfile (
    imp_key VARCHAR(8) NOT NULL,  -- PIC X(08)
    imp_data VARCHAR(300),  -- PIC X(300)
    PRIMARY KEY (imp_key)
);

-- DB2 Tables → RDBMS Schema (auto-generated from EXEC SQL analysis)
-- Source: Db2SchemaGenerator from SqlCommandSymbol IR

CREATE SCHEMA IF NOT EXISTS orion;

-- ← DB2: ORION.ACCT
DROP TABLE IF EXISTS orion.acct CASCADE;
CREATE TABLE orion.acct (
    ac_active_status VARCHAR(1),
    ac_credit_limit DECIMAL(12,2),
    ac_cash_limit DECIMAL(12,2),
    ac_expiry_date VARCHAR(10),
    ac_group_id VARCHAR(10),
    ac_id VARCHAR(15) NOT NULL,
    ac_curr_bal DECIMAL(12,2),
    ac_open_date VARCHAR(10),
    PRIMARY KEY (ac_id)
);

-- ← DB2: ORION.CARD
DROP TABLE IF EXISTS orion.card CASCADE;
CREATE TABLE orion.card (
    cd_acct_id BIGINT,
    cd_embossed_name VARCHAR(50),
    cd_expiry_date VARCHAR(10),
    cd_active_status VARCHAR(1),
    cd_num INTEGER NOT NULL,
    PRIMARY KEY (cd_num)
);

-- ← DB2: ORION.CUST
DROP TABLE IF EXISTS orion.cust CASCADE;
CREATE TABLE orion.cust (
    cu_first_name VARCHAR(25),
    cu_middle_name VARCHAR(25),
    cu_last_name VARCHAR(25),
    cu_addr_line_1 VARCHAR(50),
    cu_addr_city VARCHAR(50),
    cu_phone_1 VARCHAR(15),
    cu_fico_score SMALLINT,
    cu_id VARCHAR(15) NOT NULL,
    PRIMARY KEY (cu_id)
);

-- ← DB2: ORION.TTYP
DROP TABLE IF EXISTS orion.ttyp CASCADE;
CREATE TABLE orion.ttyp (
    tt_desc VARCHAR(50),
    tt_cd VARCHAR(10) NOT NULL,
    PRIMARY KEY (tt_cd)
);

-- ← DB2: ORION.CTRL
DROP TABLE IF EXISTS orion.ctrl CASCADE;
CREATE TABLE orion.ctrl (
    ct_last_value BIGINT,
    ct_key VARCHAR(255) NOT NULL,
    PRIMARY KEY (ct_key)
);

-- ← DB2: ORION.TRAN
DROP TABLE IF EXISTS orion.tran CASCADE;
CREATE TABLE orion.tran (
    tr_id VARCHAR(16),
    tr_type_cd VARCHAR(2),
    tr_cat_cd SMALLINT,
    tr_source VARCHAR(10),
    tr_desc VARCHAR(100),
    tr_amt DECIMAL(11,2),
    tr_merchant_id INTEGER,
    tr_merchant_name VARCHAR(50),
    tr_merchant_city VARCHAR(50),
    tr_merchant_zip VARCHAR(10),
    tr_card_num VARCHAR(16) NOT NULL,
    tr_orig_ts VARCHAR(26),
    tr_proc_ts VARCHAR(26),
    PRIMARY KEY (tr_card_num)
);


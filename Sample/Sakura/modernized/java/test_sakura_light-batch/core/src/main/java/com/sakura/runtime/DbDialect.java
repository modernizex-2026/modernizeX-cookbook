package com.sakura.runtime;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;

/** Runtime SQL dialect for file-emulation (Path A) SQL. Detect via {@link #of}. */
public interface DbDialect {
    /** COUNT(*) table-exists probe; bind = raw table name. */
    String tableExistsSql();

    /** COLUMN_NAME query for a table; bind = raw table name (callers lowercase results). */
    String columnsSql();

    /** COLUMN_NAME query for GENERATED ALWAYS identity columns; bind = raw table name. */
    String identityColumnsSql();

    /** CREATE TABLE keyword incl. trailing space (adds IF NOT EXISTS on PostgreSQL). */
    String createTableKeyword();

    /** Rewrite a PostgreSQL-shaped WHERE (CAST .. AS BIGINT/VARCHAR) for this dialect. */
    String normalizeWhere(String where);

    /** Map a PostgreSQL DDL column type (VARCHAR(n), TEXT, ..) to this dialect. */
    String mapColumnType(String pgType);

    DbDialect POSTGRES = new PostgresDialect();
    DbDialect ORACLE = new OracleDialect();

    /** Detect from connection metadata (cached). Defaults to POSTGRES (covers H2). */
    static DbDialect of(JdbcTemplate jdbcTemplate) {
        DbDialect cached = DbDialectHolder.INSTANCE;
        if (cached != null) {
            return cached;
        }
        DbDialect d = DbDialect.POSTGRES;
        try {
            String product =
                    jdbcTemplate.execute(
                            (Connection c) -> c.getMetaData().getDatabaseProductName());
            if (product != null && product.toLowerCase().contains("oracle")) {
                d = DbDialect.ORACLE;
            }
        } catch (Exception ignore) {
            /* default POSTGRES */
        }
        DbDialectHolder.INSTANCE = d;
        return d;
    }
}

final class DbDialectHolder {
    static volatile DbDialect INSTANCE;

    private DbDialectHolder() {}
}

final class PostgresDialect implements DbDialect {
    public String tableExistsSql() {
        return "SELECT COUNT(*) FROM information_schema.tables WHERE LOWER(table_name)=LOWER(?)";
    }

    public String columnsSql() {
        return "SELECT column_name FROM information_schema.columns WHERE"
                + " LOWER(table_name)=LOWER(?)";
    }

    public String identityColumnsSql() {
        return "SELECT column_name FROM information_schema.columns WHERE LOWER(table_name)=LOWER(?)"
                + " AND is_identity='YES' AND identity_generation='ALWAYS'";
    }

    public String createTableKeyword() {
        return "CREATE TABLE IF NOT EXISTS ";
    }

    public String normalizeWhere(String where) {
        return where;
    }

    public String mapColumnType(String pgType) {
        return pgType;
    }
}

final class OracleDialect implements DbDialect {
    public String tableExistsSql() {
        return "SELECT COUNT(*) FROM user_tables WHERE table_name=UPPER(?)";
    }

    public String columnsSql() {
        return "SELECT column_name FROM user_tab_columns WHERE table_name=UPPER(?)";
    }

    public String identityColumnsSql() {
        return "SELECT column_name FROM user_tab_identity_cols WHERE table_name=UPPER(?)"
                + " AND generation_type='ALWAYS'";
    }

    public String createTableKeyword() {
        return "CREATE TABLE ";
    }

    public String normalizeWhere(String where) {
        if (where == null) {
            return null;
        }
        // CAST(x AS BIGINT) -> CAST(x AS NUMBER); CAST(x AS VARCHAR) -> CAST(x AS VARCHAR2(4000))
        return where.replaceAll("(?i)\\bAS\\s+BIGINT\\)", "AS NUMBER)")
                .replaceAll("(?i)\\bAS\\s+VARCHAR\\)", "AS VARCHAR2(4000))");
    }

    public String mapColumnType(String pgType) {
        if (pgType == null) {
            return null;
        }
        String t = pgType.trim();
        String up = t.toUpperCase();
        if (up.equals("TEXT")) {
            return "CLOB";
        }
        if (up.startsWith("VARCHAR(")) return "VARCHAR2(" + t.substring(t.indexOf('(') + 1);
        // BIGINT KHÔNG phải synonym ANSI của Oracle (đo trên Oracle XE 21:
        // `CREATE TABLE t (x BIGINT)` → ORA-00902 invalid datatype; NUMBER(19) thì tạo được).
        // Danh sách synonym Oracle nhận: NUMERIC/DECIMAL/DEC/INTEGER/INT/SMALLINT/FLOAT/REAL —
        // không có BIGINT. PIC 9(10..18) sinh ra BIGINT nên đây là đường đi thật, không phải giả
        // định.
        if (up.equals("BIGINT")) {
            return "NUMBER(19)";
        }
        return t; // NUMERIC/SMALLINT/INTEGER là synonym Oracle nhận được
    }
}

package com.sakura.runtime;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the {@code ORA_CNNCT} env value used by COBOL Pro*COBOL programs and builds a
 * Hikari-pooled DataSource. Expected format: {@code user/password@tns_alias} (Hitachi/Fujitsu
 * Pro*COBOL convention — single 56-char host variable in {@code EXEC SQL CONNECT :host}).
 *
 * <p>Empty input or {@code h2:*} prefix → H2 in-memory (test mode, MODE=Oracle for SQL parity).
 */
public final class OracleConnectParser {

    private static final Pattern USER_PASS_TNS = Pattern.compile("([^/]+)/([^@]+)@(.+)");

    private OracleConnectParser() {}

    /**
     * @param oraCnnct value from {@code ORA_CNNCT} env. May have trailing spaces (PIC X(256)) —
     *     trimmed.
     * @return a Hikari pool sized for a single COBOL program execution. Caller must {@link
     *     HikariDataSource#close()}.
     * @throws IllegalArgumentException if the format is not {@code user/password@tns_alias}.
     */
    public static HikariDataSource build(String oraCnnct) {
        String s = oraCnnct == null ? "" : oraCnnct.trim();
        HikariConfig cfg = new HikariConfig();
        String lower = s.toLowerCase();
        if (s.isEmpty() || lower.startsWith("h2:") || lower.startsWith("h2;")) {
            // H2 fallback (test mode). Optionally load a schema via
            // -Dcobol.h2.init.script=/path/to/schema.sql.
            String initScript = System.getProperty("cobol.h2.init.script");
            String h2Url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=Oracle";
            if (initScript != null && !initScript.isEmpty()) {
                h2Url += ";INIT=RUNSCRIPT FROM '" + initScript + "'";
            }
            cfg.setJdbcUrl(h2Url);
            cfg.setUsername("sa");
            cfg.setPassword("");
            cfg.setDriverClassName("org.h2.Driver");
        } else {
            Matcher m = USER_PASS_TNS.matcher(s);
            if (!m.matches()) {
                throw new IllegalArgumentException(
                        "ORA_CNNCT must be 'user/password@tns_alias' format, got: " + redact(s));
            }
            cfg.setJdbcUrl("jdbc:oracle:thin:@" + m.group(3));
            cfg.setUsername(m.group(1));
            cfg.setPassword(m.group(2));
            cfg.setDriverClassName("oracle.jdbc.OracleDriver");
        }
        cfg.setMaximumPoolSize(2);
        cfg.setAutoCommit(false);
        return new HikariDataSource(cfg);
    }

    /**
     * Redact password before including ORA_CNNCT value in any user-facing message (exception, log).
     * Preserves user + TNS alias for debugging; masks the password between '/' and '@'.
     */
    private static String redact(String v) {
        if (v == null || v.length() < 4) {
            return "***";
        }
        int slash = v.indexOf('/');
        int at = v.indexOf('@');
        if (slash < 0 || at < 0 || at <= slash) {
            return v.substring(0, Math.min(3, v.length())) + "***";
        }
        return v.substring(0, slash + 1) + "***" + v.substring(at);
    }
}

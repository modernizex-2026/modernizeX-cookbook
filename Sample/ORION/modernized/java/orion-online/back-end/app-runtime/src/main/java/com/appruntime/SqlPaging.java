package com.appruntime;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Phân trang cursor theo DIALECT chốt lúc GEN (application.yml {@code cobol.target-db}, mặc định
 * postgres) — nguồn sự thật DUY NHẤT, khớp với DDL đã sinh. KHÔNG dò driver runtime (target-db đã
 * có default nên detect là thừa; xem §AJ).
 *
 * <p>Generator inject phân trang cho cursor browse (OPEN nạp chunk, FETCH nạp chunk kế). Cú pháp
 * khác nhau:
 *
 * <ul>
 *   <li>PostgreSQL (mặc định): {@code ... LIMIT ? OFFSET ?} — args: [..where, limit, offset]
 *   <li>Oracle 12c+: {@code ... OFFSET ? ROWS FETCH NEXT ? ROWS ONLY} — args: [..where, offset,
 *       limit]
 * </ul>
 *
 * <p>Giới hạn đã biết: cursor {@code FOR UPDATE} — splice phân trang TRƯỚC {@code FOR UPDATE}
 * (Oracle không cho FETCH-FIRST đi cùng FOR UPDATE — hiếm; corpus hiện 0).
 */
public final class SqlPaging {

    private SqlPaging() {}

    private static final Pattern FOR_UPDATE = Pattern.compile("(?is)\\s+FOR\\s+UPDATE\\b");

    /**
     * Dialect từ application.yml {@code cobol.target-db}. Mặc định false = PostgreSQL (target-db
     * luôn có default nên không cần dò driver).
     */
    private static volatile boolean oracle = false;

    /**
     * Set từ SqlPagingConfig lúc app khởi động theo `cobol.target-db`. Trống → giữ mặc định (PG).
     */
    public static void setTargetDb(String targetDb) {
        if (targetDb == null || targetDb.isBlank()) {
            return;
        }
        oracle = targetDb.trim().toLowerCase().contains("oracle");
    }

    static boolean isOracle() {
        return oracle;
    }

    /** Test-only: về mặc định PostgreSQL. */
    static void resetDialectCache() {
        oracle = false;
    }

    /** Cú pháp phân trang cho dialect đang chạy (không kèm args). Tách ra để test/soi. */
    static String pagedSql(String baseSql, boolean ora) {
        String suffix = ora ? " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY" : " LIMIT ? OFFSET ?";
        Matcher m = FOR_UPDATE.matcher(baseSql);
        if (m.find()) {
            // splice TRƯỚC FOR UPDATE (PG hợp lệ; Oracle giới hạn — xem javadoc)
            return baseSql.substring(0, m.start()) + suffix + baseSql.substring(m.start());
        }
        return baseSql + suffix;
    }

    /**
     * Args phân trang theo dialect: PG [limit, offset] / Oracle [offset, limit], nối sau
     * where-params.
     */
    static Object[] pagedArgs(Object[] whereParams, int limit, int offset, boolean ora) {
        int n = whereParams != null ? whereParams.length : 0;
        Object[] out = new Object[n + 2];
        if (whereParams != null) {
            System.arraycopy(whereParams, 0, out, 0, n);
        }
        if (ora) {
            out[n] = offset;
            out[n + 1] = limit;
        } else {
            out[n] = limit;
            out[n + 1] = offset;
        }
        return out;
    }

    /**
     * Chạy query browse 1 chunk theo dialect đã chốt (cobol.target-db).
     *
     * @param baseSql SELECT của DECLARE CURSOR (đã normalize, chưa có phân trang)
     * @param whereParams host-var params của WHERE (theo thứ tự {@code ?})
     * @param limit kích thước chunk
     * @param offset offset tuyệt đối của chunk
     */
    public static List<Map<String, Object>> page(
            JdbcTemplate jt, String baseSql, Object[] whereParams, int limit, int offset) {
        return jt.queryForList(
                pagedSql(baseSql, oracle), pagedArgs(whereParams, limit, offset, oracle));
    }
}

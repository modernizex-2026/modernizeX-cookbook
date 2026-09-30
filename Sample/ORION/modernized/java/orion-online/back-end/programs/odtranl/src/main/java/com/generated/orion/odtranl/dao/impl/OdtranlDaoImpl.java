package com.generated.orion.odtranl.dao.impl;

import com.appruntime.SqlPaging;
import com.generated.orion.odtranl.dao.OdtranlDao;
import com.generated.orion.odtranl.support.SqlStatements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JdbcTemplate implementation for ODTRANL DAO. ← EXEC SQL — uses JdbcTemplate with SqlStatements
 * constants.
 */
@Repository
public class OdtranlDaoImpl implements OdtranlDao {
    /** Chunk size — override bằng -Dcobol.cursor.fetch-size=N. */
    private static final int CURSOR_FETCH_SIZE = Integer.getInteger("cobol.cursor.fetch-size", 500);

    @Autowired private JdbcTemplate jdbcTemplate;

    /** Chunk hiện tại của cursor (per-request) — KHÔNG materialize toàn bộ result set. */
    private final ThreadLocal<Map<String, List<Map<String, Object>>>> cursorData =
            ThreadLocal.withInitial(HashMap::new);

    /** Vị trí row trong chunk hiện tại (per-request). */
    private final ThreadLocal<Map<String, Integer>> cursorPosition =
            ThreadLocal.withInitial(HashMap::new);

    /** WHERE params lưu ở OPEN — FETCH dùng lại khi nạp chunk kế. */
    private final ThreadLocal<Map<String, Object[]>> cursorParams =
            ThreadLocal.withInitial(HashMap::new);

    /** OFFSET tuyệt đối của chunk hiện tại. */
    private final ThreadLocal<Map<String, Integer>> cursorOffset =
            ThreadLocal.withInitial(HashMap::new);

    /**
     * ← SQL_001: SELECT TR_ID, TR_AMT, TR_TYPE_CD FROM ORION.TRAN WHERE TR_CARD_NUM = ? ORDER BY
     * ...
     */
    @Override
    public Map<String, Object> openTrancsr(Object... params) {
        cursorParams.get().put("TRANCSR", params);
        cursorOffset.get().put("TRANCSR", 0);
        List<Map<String, Object>> rows =
                SqlPaging.page(jdbcTemplate, SqlStatements.SQL_001, params, CURSOR_FETCH_SIZE, 0);
        cursorData.get().put("TRANCSR", rows);
        cursorPosition.get().put("TRANCSR", 0);
        Map<String, Object> result = new HashMap<>();
        result.put("SQLCODE", 0);
        return result;
    }

    /** ← SQL_002: CLOSE TRANCSR (cursor state in DAO) */
    @Override
    public Map<String, Object> closeTrancsr(Object... params) {
        cursorData.get().remove("TRANCSR");
        cursorPosition.get().remove("TRANCSR");
        cursorParams.get().remove("TRANCSR");
        cursorOffset.get().remove("TRANCSR");
        Map<String, Object> result = new HashMap<>();
        result.put("SQLCODE", 0);
        return result;
    }

    /** ← SQL_003: FETCH TRANCSR (cursor state in DAO) */
    @Override
    public Map<String, Object> fetchTrancsr(Object... params) {
        List<Map<String, Object>> rows = cursorData.get().get("TRANCSR");
        int pos = cursorPosition.get().getOrDefault("TRANCSR", 0);
        if (rows != null && pos >= rows.size() && rows.size() == CURSOR_FETCH_SIZE) {
            int off = cursorOffset.get().getOrDefault("TRANCSR", 0) + CURSOR_FETCH_SIZE;
            rows =
                    SqlPaging.page(
                            jdbcTemplate,
                            SqlStatements.SQL_001,
                            cursorParams.get().get("TRANCSR"),
                            CURSOR_FETCH_SIZE,
                            off);
            cursorData.get().put("TRANCSR", rows);
            cursorOffset.get().put("TRANCSR", off);
            pos = 0;
        }
        if (rows != null && pos < rows.size()) {
            cursorPosition.get().put("TRANCSR", pos + 1);
            return rows.get(pos);
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("SQLCODE", 100);
            return result;
        }
    }
}

package com.appruntime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic JDBC-based FileDao that works with any table/record. Uses reflection to map POJO fields ↔
 * snake_case columns. Returns Map results — AppRunner.copyFromMap handles type conversion.
 */
public class GenericFileDao implements FileDao {

    private static final Logger log = LoggerFactory.getLogger(GenericFileDao.class);

    private final String fileName;
    private final String tableName;
    private final String keyColumn;
    private final JdbcTemplate jdbc;

    // Track last read-for-update key for REWRITE
    private final ThreadLocal<Object> lastUpdateKey = new ThreadLocal<>();

    private static final java.util.regex.Pattern SAFE_IDENTIFIER =
            java.util.regex.Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    public JdbcTemplate getJdbcTemplate() {
        return jdbc;
    }

    public GenericFileDao(String fileName, String tableName, String keyColumn, JdbcTemplate jdbc) {
        if (!SAFE_IDENTIFIER.matcher(tableName).matches()) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }
        if (!SAFE_IDENTIFIER.matcher(keyColumn).matches()) {
            throw new IllegalArgumentException("Invalid key column: " + keyColumn);
        }
        this.fileName = fileName;
        this.tableName = tableName;
        this.keyColumn = keyColumn;
        this.jdbc = jdbc;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public Object readByKey(Object key) {
        // CAST to VARCHAR for portable type-safe comparison (COBOL keys are always
        // string-representable)
        String sql =
                "SELECT * FROM "
                        + tableName
                        + " WHERE CAST("
                        + keyColumn
                        + " AS VARCHAR) = CAST(? AS VARCHAR)";
        List<Map<String, Object>> rows = jdbc.queryForList(sql, normalizeKey(key));
        if (rows.isEmpty()) {
            return null;
        }
        log.debug("READ file={} key={} → found", fileName, key);
        return rows.get(0);
    }

    @Override
    public Object readByKeyForUpdate(Object key) {
        Object normalized = normalizeKey(key);
        String sql =
                "SELECT * FROM "
                        + tableName
                        + " WHERE CAST("
                        + keyColumn
                        + " AS VARCHAR) = CAST(? AS VARCHAR) FOR UPDATE";
        List<Map<String, Object>> rows = jdbc.queryForList(sql, normalized);
        if (rows.isEmpty()) {
            return null;
        }
        lastUpdateKey.set(normalized);
        log.debug("READ FOR UPDATE file={} key={} → found", fileName, key);
        return rows.get(0);
    }

    @Override
    public void write(Object record, Object key) {
        Map<String, Object> fields = extractFields(record);
        if (fields.isEmpty()) {
            log.warn(
                    "WRITE file={} — no fields extracted from record {}",
                    fileName,
                    record.getClass().getSimpleName());
            return;
        }

        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder placeholders = new StringBuilder();
        List<Object> values = new ArrayList<>();

        boolean first = true;
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (!first) {
                sql.append(", ");
                placeholders.append(", ");
            }
            sql.append(entry.getKey());
            placeholders.append("?");
            values.add(entry.getValue());
            first = false;
        }
        sql.append(") VALUES (").append(placeholders).append(")");

        try {
            jdbc.update(sql.toString(), values.toArray());
            log.debug("WRITE file={} key={} → inserted", fileName, key);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new DuplicateException(fileName);
        }
    }

    @Override
    public void rewrite(Object record) {
        Object updateKey = lastUpdateKey.get();
        if (updateKey == null) {
            throw new FileException(
                    fileName,
                    AppResp.INVREQ,
                    "REWRITE without prior READ FOR UPDATE on file: " + fileName) {};
        }

        Map<String, Object> fields = extractFields(record);
        if (fields.isEmpty()) {
            return;
        }

        StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        List<Object> values = new ArrayList<>();

        boolean first = true;
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (entry.getKey().equals(keyColumn)) continue; // don't update the key
            if (!first) {
                sql.append(", ");
            }
            sql.append(entry.getKey()).append(" = ?");
            values.add(entry.getValue());
            first = false;
        }
        sql.append(" WHERE CAST(").append(keyColumn).append(" AS VARCHAR) = CAST(? AS VARCHAR)");
        values.add(updateKey);

        jdbc.update(sql.toString(), values.toArray());
        lastUpdateKey.remove();
        log.debug("REWRITE file={} key={} → updated", fileName, updateKey);
    }

    @Override
    public void delete(Object key) {
        String sql =
                "DELETE FROM "
                        + tableName
                        + " WHERE CAST("
                        + keyColumn
                        + " AS VARCHAR) = CAST(? AS VARCHAR)";
        jdbc.update(sql, normalizeKey(key));
        log.debug("DELETE file={} key={}", fileName, key);
    }

    /** Maximum records per browse page — prevents loading entire table into memory. */
    private static final int BROWSE_PAGE_SIZE = 1000;

    @Override
    public List<Object> startBrowse(Object key) {
        String sql =
                "SELECT * FROM "
                        + tableName
                        + " WHERE CAST("
                        + keyColumn
                        + " AS VARCHAR) >= CAST(? AS VARCHAR) ORDER BY "
                        + keyColumn
                        + " LIMIT "
                        + BROWSE_PAGE_SIZE;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, normalizeKey(key));
        log.debug(
                "STARTBR file={} key={} → {} records (max {})",
                fileName,
                key,
                rows.size(),
                BROWSE_PAGE_SIZE);
        return new ArrayList<>(rows);
    }

    @Override
    public List<Object> startBrowsePrev(Object key) {
        String sql =
                "SELECT * FROM "
                        + tableName
                        + " WHERE CAST("
                        + keyColumn
                        + " AS VARCHAR) <= CAST(? AS VARCHAR) ORDER BY "
                        + keyColumn
                        + " DESC"
                        + " LIMIT "
                        + BROWSE_PAGE_SIZE;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, normalizeKey(key));
        log.debug("STARTBR-PREV file={} key={} → {} records (DESC)", fileName, key, rows.size());
        return new ArrayList<>(rows);
    }

    // --- Helpers ---

    /** Normalize key: trim String keys (COBOL keys are often padded with spaces). */
    private Object normalizeKey(Object key) {
        if (key instanceof String s) {
            String trimmed = s.trim();
            // COBOL numeric keys (PIC 9) may be passed as String to NUMERIC columns.
            // Use CAST in the query instead — see readByKey().
            return trimmed;
        }
        return key;
    }

    /**
     * Extract fields from a POJO record into a map of snake_case column → value. Skips null values
     * and internal fields.
     */
    private Map<String, Object> extractFields(Object record) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (record == null) {
            return result;
        }

        Class<?> clazz = record.getClass();
        while (clazz != null && !clazz.getPackageName().startsWith("java.")) {
            for (Field f : clazz.getDeclaredFields()) {
                // Skip static, synthetic fields
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                if (f.isSynthetic()) {
                    continue;
                }

                f.setAccessible(true);
                try {
                    Object value = f.get(record);
                    if (value != null) {
                        String colName = camelToSnake(f.getName());
                        result.put(colName, value);
                    }
                } catch (IllegalAccessException e) {
                    log.error(
                            "extractFields: cannot access field '{}' on {}: {}",
                            f.getName(),
                            record.getClass().getSimpleName(),
                            e.getMessage());
                }
            }
            clazz = clazz.getSuperclass();
        }
        return result;
    }

    /** Convert camelCase field name to snake_case column name. e.g. "secUsrId" → "sec_usr_id" */
    static String camelToSnake(String camel) {
        if (camel == null || camel.isEmpty()) {
            return camel;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}

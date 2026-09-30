package com.appruntime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;

/**
 * IMS DL/I runtime — maps DL/I operations to PostgreSQL via Spring JdbcTemplate.
 *
 * <p>Maintains IMS state per thread: position (cursor), parent context, hold state. Hierarchy
 * metadata loaded from configuration at startup.
 *
 * <p>Direction 1 (IMS→SQL): DL/I calls → SQL queries. Direction 2 (SQL→IMS): SQL results → PCB
 * status, position, parent context.
 */
public class DliRunner implements DliService {

    private static final Logger log = LoggerFactory.getLogger(DliRunner.class);

    /** IMS PCB Status Codes */
    private static final String STATUS_OK = "  ";

    private static final String STATUS_NOT_FOUND = "GE";
    private static final String STATUS_END_DB = "GB";
    private static final String STATUS_DUPLICATE = "II";
    private static final String STATUS_NO_PARENT =
            "GP"; // GNP khi chưa establish parentage (GU cha trước)
    private static final String STATUS_NO_HOLD = "DJ";
    private static final String STATUS_IO_ERROR = "AO";
    private static final String STATUS_BAD_CALL = "AD"; // function/SSA parameter invalid

    private final JdbcTemplate jdbcTemplate;

    // --- IMS State (per thread) ---
    private final ThreadLocal<String> dibstatHolder = ThreadLocal.withInitial(() -> STATUS_OK);
    private final ThreadLocal<HoldState> holdStateHolder = new ThreadLocal<>();
    private final ThreadLocal<Map<String, SegmentPosition>> positionsHolder =
            ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<String, ParentContext>> parentContextsHolder =
            ThreadLocal.withInitial(HashMap::new);

    // --- Hierarchy metadata (loaded once) ---
    private final Map<String, SegmentMeta> hierarchy = new LinkedHashMap<>();

    // --- Inner classes ---

    private static class HoldState {
        final String segmentName;
        final List<String> keyColumns; // SQL column names
        final List<Object> keyValues;

        HoldState(String segmentName, List<String> keyColumns, List<Object> keyValues) {
            this.segmentName = segmentName;
            this.keyColumns = keyColumns;
            this.keyValues = keyValues;
        }

        HoldState(String segmentName, String keyColumn, Object keyValue) {
            this(segmentName, List.of(keyColumn), List.of(keyValue));
        }
    }

    private static class SegmentPosition {
        final List<Object> lastKeyValues = new ArrayList<>();
        boolean exhausted = false;
    }

    private static class ParentContext {
        final String parentSegment;
        final Map<String, Object> fkValues; // fk_column → value

        ParentContext(String parentSegment, Map<String, Object> fkValues) {
            this.parentSegment = parentSegment;
            this.fkValues = fkValues;
        }
    }

    /** Segment hierarchy metadata — pk/fk/parent info. */
    public static class SegmentMeta {
        public String parentSegment; // null for root
        public List<String> pkColumns; // primary key columns (SQL column names)
        public List<String> fkColumns; // FK columns referencing parent PK
        public List<String> childSegments = new ArrayList<>();
        public Map<String, String> fieldMapping = new HashMap<>(); // IMS field → SQL column
    }

    // --- Constructor ---

    public DliRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Register IMS field → SQL column mapping for a segment. */
    public void registerFieldMapping(String segmentName, String imsField, String sqlColumn) {
        SegmentMeta meta = hierarchy.get(segmentName.toUpperCase());
        if (meta != null) {
            meta.fieldMapping.put(imsField.toUpperCase(), sqlColumn.toLowerCase());
        }
    }

    /**
     * Resolve IMS field name to SQL column name.
     *
     * <p>Strategy (in order): 1. Explicit mapping from registerSegment() fieldMapping 2. Name
     * matching against table columns (normalize + substring) 3. Positional: assign PK columns in
     * order to unresolved fields
     */
    private String resolveColumn(String segmentName, String imsField) {
        if (imsField == null) {
            return null;
        }
        SegmentMeta meta = hierarchy.get(segmentName.toUpperCase());

        // 1. Explicit mapping
        if (meta != null && meta.fieldMapping.containsKey(imsField.toUpperCase())) {
            return meta.fieldMapping.get(imsField.toUpperCase());
        }

        // 2. Name matching against ALL table columns
        String table = segmentName.toLowerCase();
        try {
            List<Map<String, Object>> cols =
                    jdbcTemplate.queryForList(
                            "SELECT column_name FROM information_schema.columns WHERE table_name ="
                                    + " ? ORDER BY ordinal_position",
                            table);
            List<String> columnNames =
                    cols.stream().map(r -> r.get("column_name").toString()).toList();

            String matched = matchColumn(imsField, columnNames);
            if (matched != null) {
                log.debug("DLI resolveColumn: {} → {} (name-matched)", imsField, matched);
                if (meta != null) {
                    meta.fieldMapping.put(imsField.toUpperCase(), matched);
                }
                return matched;
            }

            // 3. Positional: use PK column at index = resolveIndex (tracks multi-key position)
            if (meta != null && !meta.pkColumns.isEmpty()) {
                // Count how many fields have already been resolved for this segment (positional
                // index)
                int resolvedCount =
                        (int)
                                meta.fieldMapping.values().stream()
                                        .filter(v -> meta.pkColumns.contains(v))
                                        .count();
                if (resolvedCount < meta.pkColumns.size()) {
                    String pkCol = meta.pkColumns.get(resolvedCount);
                    log.debug(
                            "DLI resolveColumn: {} → {} (positional PK[{}])",
                            imsField,
                            pkCol,
                            resolvedCount);
                    meta.fieldMapping.put(imsField.toUpperCase(), pkCol);
                    return pkCol;
                }
            }
        } catch (Exception e) {
            log.trace("DLI resolveColumn query failed: {}", e.getMessage());
        }
        return imsField.toLowerCase();
    }

    /**
     * Match an IMS field name to a SQL column name by normalized string comparison. Removes
     * separators (_, -) and lowercases both sides, then checks: 1. Exact match: "ORDERID" =
     * "order_id" (both → "orderid") 2. IMS contained in column: "ORDERID" in "od_order_id"
     * ("orderid" in "odorderid") 3. Column contained in IMS: "acct_id" in "ACCTID" ("acctid" in
     * "acctid")
     */
    private String matchColumn(String imsField, List<String> columns) {
        String normalized = imsField.replaceAll("[_-]", "").toLowerCase();

        for (String col : columns) {
            if (col.replaceAll("[_-]", "").toLowerCase().equals(normalized)) {
                return col;
            }
        }
        for (String col : columns) {
            if (col.replaceAll("[_-]", "").toLowerCase().contains(normalized)) {
                return col;
            }
        }
        for (String col : columns) {
            String colNorm = col.replaceAll("[_-]", "").toLowerCase();
            if (colNorm.length() > 2 && normalized.contains(colNorm)) {
                return col;
            }
        }
        return null;
    }

    /** Register segment hierarchy metadata. Called at startup from config. */
    public void registerSegment(
            String segmentName,
            String parentSegment,
            List<String> pkColumns,
            List<String> fkColumns) {
        SegmentMeta meta = new SegmentMeta();
        meta.parentSegment = parentSegment;
        meta.pkColumns = pkColumns != null ? pkColumns : List.of();
        meta.fkColumns = fkColumns != null ? fkColumns : List.of();
        hierarchy.put(segmentName.toUpperCase(), meta);
        // Register as child of parent
        if (parentSegment != null) {
            SegmentMeta parentMeta = hierarchy.get(parentSegment.toUpperCase());
            if (parentMeta != null) {
                parentMeta.childSegments.add(segmentName.toUpperCase());
            }
        }
    }

    // --- PSB lifecycle ---

    @Override
    public void schedulePsb(String psbName) {
        log.debug("DLI SCHD PSB: {}", psbName);
        dibstatHolder.set(STATUS_OK);
    }

    @Override
    public void terminatePsb() {
        log.debug("DLI TERM PSB");
        holdStateHolder.remove();
        positionsHolder.get().clear();
        parentContextsHolder.get().clear();
        dibstatHolder.set(STATUS_OK);
    }

    // --- GU / GHU ---

    @Override
    public void getUnique(String segmentName, Object into, String keyField, Object keyValue) {
        doGetUnique(segmentName, into, new String[] {keyField}, new Object[] {keyValue}, false);
    }

    @Override
    public void getHoldUnique(String segmentName, Object into, String keyField, Object keyValue) {
        doGetUnique(segmentName, into, new String[] {keyField}, new Object[] {keyValue}, true);
    }

    @Override
    public void getUniqueMultiKey(
            String segmentName, Object into, String[] keyFields, Object[] keyValues) {
        doGetUnique(segmentName, into, keyFields, keyValues, false);
    }

    @Override
    public void getHoldUniqueMultiKey(
            String segmentName, Object into, String[] keyFields, Object[] keyValues) {
        doGetUnique(segmentName, into, keyFields, keyValues, true);
    }

    private void doGetUnique(
            String segmentName, Object into, String[] keyFields, Object[] keyValues, boolean hold) {
        String table = segmentName.toLowerCase();
        getOrDiscoverMeta(segmentName); // Ensure hierarchy metadata populated before resolveColumn

        // Build multi-column WHERE clause
        StringBuilder whereSql = new StringBuilder();
        Object[] params = new Object[keyFields.length];
        for (int i = 0; i < keyFields.length; i++) {
            if (i > 0) {
                whereSql.append(" AND ");
            }
            String column = resolveColumn(segmentName, keyFields[i]);
            whereSql.append(column).append(" = ?");
            // Trim String key values — COBOL PIC X pads with spaces, SQL VARCHAR does not
            params[i] = (keyValues[i] instanceof String s) ? s.trim() : keyValues[i];
        }

        String sql = "SELECT * FROM " + table + " WHERE " + whereSql + (hold ? " FOR UPDATE" : "");

        log.debug(
                "DLI {}: {} WHERE {} params={}",
                hold ? "GHU" : "GU",
                table,
                whereSql,
                java.util.Arrays.toString(params));

        // Resolve column names for holdState (store SQL columns, not IMS field names)
        List<String> resolvedCols = new ArrayList<>();
        for (String kf : keyFields) {
            resolvedCols.add(resolveColumn(segmentName, kf));
        }

        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, params);
            copyFromMap(row, into);
            dibstatHolder.set(STATUS_OK);
            if (hold) {
                List<Object> trimmedVals = new ArrayList<>();
                for (Object v : keyValues) {
                    trimmedVals.add((v instanceof String s) ? s.trim() : v);
                }
                holdStateHolder.set(new HoldState(segmentName, resolvedCols, trimmedVals));
            }
            resetAllPositions();
            setParentContextFromRow(segmentName, row);
        } catch (EmptyResultDataAccessException e) {
            dibstatHolder.set(STATUS_NOT_FOUND);
            if (hold) {
                holdStateHolder.remove();
            }
        }
    }

    // --- GN / GHN ---

    @Override
    public void getNext(String segmentName, Object into) {
        doGetNext(segmentName, into, null, null, false);
    }

    @Override
    public void getNext(String segmentName, Object into, String keyField, Object keyValue) {
        doGetNext(segmentName, into, keyField, keyValue, false);
    }

    @Override
    public void getHoldNext(String segmentName, Object into) {
        doGetNext(segmentName, into, null, null, true);
    }

    private void doGetNext(
            String segmentName, Object into, String ssaKeyField, Object ssaKeyValue, boolean hold) {
        String table = segmentName.toLowerCase();
        SegmentMeta meta = getOrDiscoverMeta(segmentName);
        List<String> pkCols = meta.pkColumns;

        SegmentPosition pos =
                positionsHolder
                        .get()
                        .computeIfAbsent(segmentName.toUpperCase(), k -> new SegmentPosition());
        if (pos.exhausted) {
            dibstatHolder.set(STATUS_END_DB);
            return;
        }

        // Build SQL
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(table);
        List<Object> params = new ArrayList<>();
        List<String> whereClauses = new ArrayList<>();

        // SSA qualification
        if (ssaKeyField != null) {
            whereClauses.add(resolveColumn(segmentName, ssaKeyField) + " = ?");
            params.add((ssaKeyValue instanceof String s) ? s.trim() : ssaKeyValue);
        }

        // Cursor: (pk_cols) > (last_values)
        if (!pos.lastKeyValues.isEmpty() && pkCols.size() == pos.lastKeyValues.size()) {
            String pkTuple =
                    "("
                            + String.join(
                                    ", ", pkCols.stream().map(DliRunner::toSnakeCase).toList())
                            + ")";
            String placeholders =
                    "(" + String.join(", ", Collections.nCopies(pkCols.size(), "?")) + ")";
            whereClauses.add(pkTuple + " > " + placeholders);
            params.addAll(pos.lastKeyValues);
        }

        if (!whereClauses.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", whereClauses));
        }
        sql.append(" ORDER BY ")
                .append(String.join(", ", pkCols.stream().map(DliRunner::toSnakeCase).toList()));
        sql.append(" LIMIT 1");

        if (hold) {
            sql.append(" FOR UPDATE");
        }

        log.debug("DLI {}: {}", hold ? "GHN" : "GN", sql);

        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(sql.toString(), params.toArray());
            copyFromMap(row, into);
            dibstatHolder.set(STATUS_OK);
            // Update cursor position
            pos.lastKeyValues.clear();
            for (String pk : pkCols) {
                pos.lastKeyValues.add(row.get(toSnakeCase(pk)));
            }
            if (hold) {
                List<String> holdCols = new ArrayList<>();
                List<Object> holdVals = new ArrayList<>();
                for (String pk : pkCols) {
                    holdCols.add(pk);
                    holdVals.add(row.get(pk));
                }
                holdStateHolder.set(new HoldState(segmentName, holdCols, holdVals));
            }
            // Direction 2: set parent context for children
            setParentContextFromRow(segmentName, row);
        } catch (EmptyResultDataAccessException e) {
            pos.exhausted = true;
            dibstatHolder.set(STATUS_END_DB);
        }
    }

    // --- GNP / GHNP ---

    @Override
    public void getNextWithinParent(String segmentName, Object into) {
        doGetNextWithinParent(segmentName, into, false);
    }

    @Override
    public void getHoldNextWithinParent(String segmentName, Object into) {
        doGetNextWithinParent(segmentName, into, true);
    }

    private void doGetNextWithinParent(String segmentName, Object into, boolean hold) {
        String table = segmentName.toLowerCase();
        SegmentMeta meta = getOrDiscoverMeta(segmentName);
        List<String> pkCols = meta.pkColumns;

        // Direction 2: resolve parent from context
        ParentContext parentCtx = parentContextsHolder.get().get(segmentName.toUpperCase());
        if (parentCtx == null) {
            // IMS: GNP không có parentage established → status 'GP' (khác 'GE' = hết
            // segment thoả điều kiện dưới parent) — audit middleware round-2.
            log.warn("DLI GNP {}: no parent context (need GU on parent first)", segmentName);
            dibstatHolder.set(STATUS_NO_PARENT);
            return;
        }

        SegmentPosition pos =
                positionsHolder
                        .get()
                        .computeIfAbsent(segmentName.toUpperCase(), k -> new SegmentPosition());
        if (pos.exhausted) {
            dibstatHolder.set(STATUS_NOT_FOUND);
            return;
        }

        // Build SQL with parent FK filter
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(table);
        List<Object> params = new ArrayList<>();
        List<String> whereClauses = new ArrayList<>();

        // FK filter from parent context
        for (Map.Entry<String, Object> fk : parentCtx.fkValues.entrySet()) {
            whereClauses.add(fk.getKey() + " = ?");
            params.add(fk.getValue());
        }

        // Cursor
        if (!pos.lastKeyValues.isEmpty() && pkCols.size() == pos.lastKeyValues.size()) {
            String pkTuple =
                    "("
                            + String.join(
                                    ", ", pkCols.stream().map(DliRunner::toSnakeCase).toList())
                            + ")";
            String placeholders =
                    "(" + String.join(", ", Collections.nCopies(pkCols.size(), "?")) + ")";
            whereClauses.add(pkTuple + " > " + placeholders);
            params.addAll(pos.lastKeyValues);
        }

        sql.append(" WHERE ").append(String.join(" AND ", whereClauses));
        sql.append(" ORDER BY ")
                .append(String.join(", ", pkCols.stream().map(DliRunner::toSnakeCase).toList()));
        sql.append(" LIMIT 1");

        if (hold) {
            sql.append(" FOR UPDATE");
        }

        log.debug("DLI {}: {}", hold ? "GHNP" : "GNP", sql);

        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(sql.toString(), params.toArray());
            copyFromMap(row, into);
            dibstatHolder.set(STATUS_OK);
            pos.lastKeyValues.clear();
            for (String pk : pkCols) {
                pos.lastKeyValues.add(row.get(toSnakeCase(pk)));
            }
            if (hold) {
                List<String> holdCols = new ArrayList<>();
                List<Object> holdVals = new ArrayList<>();
                for (String pk : pkCols) {
                    holdCols.add(pk);
                    holdVals.add(row.get(pk));
                }
                holdStateHolder.set(new HoldState(segmentName, holdCols, holdVals));
            }
        } catch (EmptyResultDataAccessException e) {
            pos.exhausted = true;
            dibstatHolder.set(STATUS_NOT_FOUND);
        }
    }

    // --- REPL ---

    @Override
    public void replace(String segmentName, Object from) {
        HoldState hold = holdStateHolder.get();
        String table = segmentName.toLowerCase();

        if (hold == null || !hold.segmentName.equalsIgnoreCase(segmentName)) {
            log.warn("DLI REPL without prior GHU for segment {}", segmentName);
            dibstatHolder.set(STATUS_NO_HOLD);
            return;
        }

        // Build SET clauses excluding PK columns
        Set<String> pkColSet = new java.util.HashSet<>(hold.keyColumns);
        List<String> setClauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        extractFieldsForUpdate(from, table, setClauses, params, pkColSet);

        // Build composite WHERE from holdState
        StringBuilder whereSql = new StringBuilder();
        for (int i = 0; i < hold.keyColumns.size(); i++) {
            if (i > 0) {
                whereSql.append(" AND ");
            }
            whereSql.append(hold.keyColumns.get(i)).append(" = ?");
            params.add(hold.keyValues.get(i));
        }

        String sql =
                "UPDATE " + table + " SET " + String.join(", ", setClauses) + " WHERE " + whereSql;

        log.debug("DLI REPL: {}", sql);

        try {
            int rows = jdbcTemplate.update(sql, params.toArray());
            dibstatHolder.set(rows > 0 ? STATUS_OK : STATUS_NOT_FOUND);
        } catch (Exception e) {
            log.error("DLI REPL failed: {}", e.getMessage());
            dibstatHolder.set(STATUS_IO_ERROR);
        }
        holdStateHolder.remove();
    }

    // --- ISRT ---

    @Override
    public void insert(String segmentName, Object from) {
        String table = segmentName.toLowerCase();
        List<String> columns = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        extractFieldsForInsert(from, table, columns, placeholders, params);

        String sql =
                "INSERT INTO "
                        + table
                        + " ("
                        + String.join(", ", columns)
                        + ") VALUES ("
                        + String.join(", ", placeholders)
                        + ")";

        log.debug("DLI ISRT: {}", sql);

        try {
            jdbcTemplate.update(sql, params.toArray());
            dibstatHolder.set(STATUS_OK);
            // Position at inserted segment
            resetPositionFor(segmentName);
        } catch (DataIntegrityViolationException e) {
            log.warn("DLI ISRT duplicate key: {}", e.getMessage());
            dibstatHolder.set(STATUS_DUPLICATE);
        } catch (Exception e) {
            log.error("DLI ISRT failed: {}", e.getMessage());
            dibstatHolder.set(STATUS_IO_ERROR);
        }
    }

    @Override
    public void insertUnderParent(
            String parentSegment,
            String parentKey,
            Object parentValue,
            String childSegment,
            Object from) {
        String table = childSegment.toLowerCase();
        List<String> columns = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        extractFieldsForInsert(from, table, columns, placeholders, params);

        if (parentKey != null) {
            String parentColumn = toSnakeCase(parentKey);
            if (!columns.contains(parentColumn)) {
                columns.add(parentColumn);
                placeholders.add("?");
                params.add(parentValue);
            }
        }

        String sql =
                "INSERT INTO "
                        + table
                        + " ("
                        + String.join(", ", columns)
                        + ") VALUES ("
                        + String.join(", ", placeholders)
                        + ")";

        log.debug("DLI ISRT (path): {} under {}", table, parentSegment);

        try {
            jdbcTemplate.update(sql, params.toArray());
            dibstatHolder.set(STATUS_OK);
        } catch (DataIntegrityViolationException e) {
            dibstatHolder.set(STATUS_DUPLICATE);
        } catch (Exception e) {
            log.error("DLI ISRT (path) failed: {}", e.getMessage());
            dibstatHolder.set(STATUS_IO_ERROR);
        }
    }

    // --- DLET ---

    @Override
    public void delete(String segmentName) {
        HoldState hold = holdStateHolder.get();
        String table = segmentName.toLowerCase();

        if (hold == null || !hold.segmentName.equalsIgnoreCase(segmentName)) {
            log.warn("DLI DLET without prior GHU for segment {}", segmentName);
            dibstatHolder.set(STATUS_NO_HOLD);
            return;
        }

        // Cascade: delete children first (if FK has no ON DELETE CASCADE)
        SegmentMeta meta = hierarchy.get(segmentName.toUpperCase());
        if (meta != null) {
            for (String child : meta.childSegments) {
                String childTable = child.toLowerCase();
                SegmentMeta childMeta = hierarchy.get(child);
                if (childMeta != null && !childMeta.fkColumns.isEmpty()) {
                    // Use first hold key value with child FK column
                    String fkCol = childMeta.fkColumns.get(0);
                    Object fkVal = hold.keyValues.get(0);
                    String delChild = "DELETE FROM " + childTable + " WHERE " + fkCol + " = ?";
                    try {
                        jdbcTemplate.update(delChild, fkVal);
                        log.debug("DLI DLET cascade: {} WHERE {} = {}", childTable, fkCol, fkVal);
                    } catch (Exception e) {
                        log.debug("DLI DLET cascade child {}: {}", childTable, e.getMessage());
                    }
                }
            }
        }

        // Build composite WHERE from holdState
        StringBuilder whereSql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < hold.keyColumns.size(); i++) {
            if (i > 0) {
                whereSql.append(" AND ");
            }
            whereSql.append(hold.keyColumns.get(i)).append(" = ?");
            params.add(hold.keyValues.get(i));
        }

        String sql = "DELETE FROM " + table + " WHERE " + whereSql;
        log.debug("DLI DLET: {}", sql);

        try {
            int rows = jdbcTemplate.update(sql, params.toArray());
            dibstatHolder.set(rows > 0 ? STATUS_OK : STATUS_NOT_FOUND);
        } catch (Exception e) {
            log.error("DLI DLET failed: {}", e.getMessage());
            dibstatHolder.set(STATUS_IO_ERROR);
        }
        holdStateHolder.remove();
        resetPositionFor(segmentName);
    }

    // --- CHKP / ROLB ---

    /**
     * Điều phối unit-of-work (AppRunner) — IMS CHKP/ROLB phải commit/back-out UOW (audit
     * middleware: trước đây CHKP no-op, ROLB chỉ reset position → ghi DB không được back-out, lệch
     * IMS). Null khi chạy standalone/test → giữ hành vi cũ.
     */
    private AppService txCoordinator;

    void setTxCoordinator(AppService coordinator) {
        this.txCoordinator = coordinator;
    }

    @Override
    public void checkpoint(String chkpId) {
        log.debug("DLI CHKP: {}", chkpId);
        // IMS CHKP = commit point: chốt unit-of-work hiện hành, mở UOW mới
        if (txCoordinator != null) {
            txCoordinator.syncpoint();
        }
    }

    @Override
    public void rollback() {
        log.debug("DLI ROLB");
        resetAllPositions();
        // IMS ROLB = back out về commit point gần nhất (kèm reset position)
        if (txCoordinator != null) {
            txCoordinator.rollback();
        }
    }

    // --- Status ---

    @Override
    public String getDibstat() {
        return dibstatHolder.get();
    }

    // --- CBLTDLI — batch-mode DL/I entry point ---

    /** DL/I calls without an SSA operate on the segment of the previous call (IMS DB position). */
    private final ThreadLocal<String> lastSegmentHolder = new ThreadLocal<>();

    /**
     * Parsed SSA: {@code SEGNAME(8)} [+ command codes] + optional {@code (FLDNAME(8) OP(2) value)}.
     */
    static final class Ssa {
        final String segment;
        final String field; // null → unqualified
        final String relop; // normalized SQL form: = > >= < <= <>
        final String value;

        Ssa(String segment, String field, String relop, String value) {
            this.segment = segment;
            this.field = field;
            this.relop = relop;
            this.value = value;
        }
    }

    /**
     * Parse a runtime SSA byte image the COBOL program built (fixed IMS layout: segment name in
     * cols 1-8, optional {@code *X} command codes, optional qualification {@code (FLDNAME(8)
     * relop(2) value)}).
     */
    static Ssa parseSsa(String raw) {
        if (raw == null) {
            return null;
        }
        String segment = raw.substring(0, Math.min(8, raw.length())).trim();
        if (segment.isEmpty()) {
            return null;
        }
        int qual = 8;
        if (raw.length() > 8 && raw.charAt(8) == '*') { // skip command codes up to '('
            int paren = raw.indexOf('(', 8);
            qual = paren >= 0 ? paren : raw.length();
        }
        if (qual < raw.length() && raw.charAt(qual) == '(') {
            String field = raw.substring(qual + 1, Math.min(qual + 9, raw.length())).trim();
            String relop =
                    normalizeRelop(
                            raw.length() >= qual + 11 ? raw.substring(qual + 9, qual + 11) : "");
            int close = raw.lastIndexOf(')');
            String value =
                    raw.length() > qual + 11
                            ? raw.substring(qual + 11, close > qual + 11 ? close : raw.length())
                                    .trim()
                            : "";
            return new Ssa(segment, field, relop, value);
        }
        return new Ssa(segment, null, null, null);
    }

    /** Normalize a DL/I relational operator (' =', 'EQ', '> ', 'GT', …) to its SQL form. */
    static String normalizeRelop(String raw) {
        String op = raw == null ? "" : raw.trim().toUpperCase();
        switch (op) {
            case ">":
            case "GT":
                return ">";
            case ">=":
            case "=>":
            case "GE":
                return ">=";
            case "<":
            case "LT":
                return "<";
            case "<=":
            case "=<":
            case "LE":
                return "<=";
            case "!=":
            case "NE":
            case "¬=":
                return "<>";
            default:
                return "="; // ' =', 'EQ' or blank — DL/I default is equal
        }
    }

    /**
     * CALL 'CBLTDLI' — the function code and SSAs arrive as the runtime byte images the COBOL
     * program built (KDLI-style copybook constants). Parses them, dispatches to the
     * table-per-segment engine, and returns the 2-char PCB status for the caller to write back into
     * the PCB mask.
     */
    @Override
    public String cbltdli(String function, Object ioArea, String... ssas) {
        String func = function == null ? "" : function.trim().toUpperCase();

        if (func.equals("CHKP") || func.equals("XRST")) {
            checkpoint(null);
            dibstatHolder.set(STATUS_OK);
            return getDibstat();
        }
        if (func.equals("ROLB") || func.equals("ROLL")) {
            rollback();
            dibstatHolder.set(STATUS_OK);
            return getDibstat();
        }

        List<Ssa> parsed = new ArrayList<>();
        if (ssas != null) {
            for (String raw : ssas) {
                Ssa s = parseSsa(raw);
                if (s != null) {
                    parsed.add(s);
                }
            }
        }
        // Multi-SSA = hierarchical path; the last SSA names the target segment.
        Ssa target = parsed.isEmpty() ? null : parsed.get(parsed.size() - 1);
        if (parsed.size() > 1) {
            log.warn(
                    "CBLTDLI {}: {} SSA levels — only the target segment qualification is applied",
                    func,
                    parsed.size());
        }
        String segment = target != null ? target.segment : resolveImplicitSegment(func);
        if (segment == null) {
            log.warn(
                    "CBLTDLI {}: no SSA and no established position — cannot resolve segment",
                    func);
            dibstatHolder.set(STATUS_BAD_CALL);
            return getDibstat();
        }
        lastSegmentHolder.set(segment);

        switch (func) {
            case "GU":
            case "GHU":
                doRetrieve(segment, ioArea, target, func.startsWith("GH"), false);
                break;
            case "GN":
            case "GHN":
                doRetrieve(segment, ioArea, target, func.startsWith("GH"), true);
                break;
            case "GNP":
            case "GHNP":
                if (target != null && target.field != null) {
                    log.warn("CBLTDLI {} {}: SSA qualification not applied on GNP", func, segment);
                }
                if (func.startsWith("GH")) {
                    getHoldNextWithinParent(segment, ioArea);
                } else getNextWithinParent(segment, ioArea);
                break;
            case "ISRT":
                insert(segment, ioArea);
                break;
            case "REPL":
                replace(segment, ioArea);
                break;
            case "DLET":
                delete(segment);
                break;
            default:
                log.warn("CBLTDLI: unsupported DL/I function '{}'", func);
                dibstatHolder.set(STATUS_BAD_CALL);
        }
        return getDibstat();
    }

    /**
     * Segment for a call without SSA: REPL/DLET target the held segment; G* continue from position.
     */
    private String resolveImplicitSegment(String func) {
        if (func.equals("REPL") || func.equals("DLET")) {
            HoldState hold = holdStateHolder.get();
            if (hold != null) {
                return hold.segmentName;
            }
        }
        return lastSegmentHolder.get();
    }

    /**
     * Shared retrieval for CBLTDLI GU/GHU ({@code sequential=false}) and GN/GHN ({@code
     * sequential=true}). Unlike the EXEC DLI getUnique path, GU here supports range relops ({@code
     * key >}, {@code key >=} — OUIMSPA-style key browsing) and leaves DB position ON the retrieved
     * row, so a following GN returns the row after it.
     */
    private void doRetrieve(
            String segmentName, Object into, Ssa ssa, boolean hold, boolean sequential) {
        String table = segmentName.toLowerCase();
        SegmentMeta meta = getOrDiscoverMeta(segmentName);
        List<String> pkCols = meta.pkColumns;

        SegmentPosition pos;
        if (sequential) {
            pos =
                    positionsHolder
                            .get()
                            .computeIfAbsent(segmentName.toUpperCase(), k -> new SegmentPosition());
            if (pos.exhausted) {
                dibstatHolder.set(STATUS_END_DB);
                return;
            }
        } else {
            // GU establishes a fresh DB position at the retrieved segment
            resetAllPositions();
            pos =
                    positionsHolder
                            .get()
                            .computeIfAbsent(segmentName.toUpperCase(), k -> new SegmentPosition());
        }

        List<String> whereClauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String qualCol = null;
        String relop = "=";
        if (ssa != null && ssa.field != null) {
            qualCol = resolveColumn(segmentName, ssa.field);
            relop = ssa.relop;
            whereClauses.add(qualCol + " " + relop + " ?");
            params.add(ssa.value);
        }
        if (sequential
                && !pos.lastKeyValues.isEmpty()
                && pkCols.size() == pos.lastKeyValues.size()) {
            String pkTuple =
                    "("
                            + String.join(
                                    ", ", pkCols.stream().map(DliRunner::toSnakeCase).toList())
                            + ")";
            String placeholders =
                    "(" + String.join(", ", Collections.nCopies(pkCols.size(), "?")) + ")";
            whereClauses.add(pkTuple + " > " + placeholders);
            params.addAll(pos.lastKeyValues);
        }

        // Range relop → first row in key order past the boundary (IMS key search);
        // '<' / '<=' walk backwards, so order descending.
        boolean range = qualCol != null && !relop.equals("=") && !relop.equals("<>");
        boolean descending = range && relop.startsWith("<");
        List<String> orderCols = new ArrayList<>();
        if (range) {
            orderCols.add(descending ? qualCol + " DESC" : qualCol);
        }
        for (String pk : pkCols) {
            String col = toSnakeCase(pk);
            if (range && col.equals(qualCol)) {
                continue;
            }
            orderCols.add(descending ? col + " DESC" : col);
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(table);
        if (!whereClauses.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", whereClauses));
        }
        if (!orderCols.isEmpty()) {
            sql.append(" ORDER BY ").append(String.join(", ", orderCols));
        }
        sql.append(" LIMIT 1");
        if (hold) {
            sql.append(" FOR UPDATE");
        }

        log.debug(
                "CBLTDLI {}{}: {} params={}",
                sequential ? "GN" : "GU",
                hold ? "(hold)" : "",
                sql,
                params);

        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(sql.toString(), params.toArray());
            copyFromMap(row, into);
            dibstatHolder.set(STATUS_OK);
            pos.lastKeyValues.clear();
            for (String pk : pkCols) {
                pos.lastKeyValues.add(row.get(toSnakeCase(pk)));
            }
            if (hold) {
                List<String> holdCols = new ArrayList<>();
                List<Object> holdVals = new ArrayList<>();
                for (String pk : pkCols) {
                    String col = toSnakeCase(pk);
                    holdCols.add(col);
                    holdVals.add(row.get(col));
                }
                holdStateHolder.set(new HoldState(segmentName, holdCols, holdVals));
            }
            setParentContextFromRow(segmentName, row);
        } catch (EmptyResultDataAccessException e) {
            if (sequential) {
                pos.exhausted = true;
                dibstatHolder.set(STATUS_END_DB);
            } else {
                dibstatHolder.set(STATUS_NOT_FOUND);
                if (hold) {
                    holdStateHolder.remove();
                }
            }
        }
    }

    // --- Position management ---

    private void resetAllPositions() {
        positionsHolder.get().clear();
    }

    private void resetPositionFor(String segmentName) {
        positionsHolder.get().remove(segmentName.toUpperCase());
    }

    /**
     * Auto-discover hierarchy from DB FK metadata when not explicitly registered. Queries
     * information_schema for tables that reference the given table.
     */
    /**
     * Get segment metadata — auto-discover from DB if not registered. Throws if segment cannot be
     * resolved (no table or no PK).
     */
    private SegmentMeta getOrDiscoverMeta(String segmentName) {
        SegmentMeta meta = hierarchy.get(segmentName.toUpperCase());
        if (meta != null) {
            return meta;
        }
        autoDiscoverChildren(segmentName);
        meta = hierarchy.get(segmentName.toUpperCase());
        if (meta == null || meta.pkColumns.isEmpty()) {
            throw new IllegalStateException(
                    "DLI segment '"
                            + segmentName
                            + "' not registered and auto-discovery failed. "
                            + "Ensure table exists with a PRIMARY KEY constraint.");
        }
        return meta;
    }

    private void autoDiscoverChildren(String segmentName) {
        if (hierarchy.containsKey(segmentName.toUpperCase())) {
            return;
        }

        String table = segmentName.toLowerCase();
        try {
            // Get PK columns via constraint_type (no naming pattern assumption)
            List<String> pkCols = queryPrimaryKeyColumns(table);
            if (pkCols.isEmpty()) {
                log.warn(
                        "DLI: no primary key found for table '{}' — segment not registered", table);
                return;
            }
            registerSegment(segmentName, null, pkCols, null);

            // Find child tables via FK referencing this table's PK
            List<Map<String, Object>> fkRows =
                    jdbcTemplate.queryForList(
                            "SELECT kcu.table_name AS child_table, kcu.column_name AS fk_column"
                                + " FROM information_schema.table_constraints tc JOIN"
                                + " information_schema.referential_constraints rc ON"
                                + " rc.constraint_name = tc.constraint_name JOIN"
                                + " information_schema.key_column_usage kcu ON kcu.constraint_name"
                                + " = tc.constraint_name JOIN"
                                + " information_schema.constraint_column_usage ccu ON"
                                + " ccu.constraint_name = rc.unique_constraint_name WHERE"
                                + " tc.constraint_type = 'FOREIGN KEY' AND ccu.table_name = ?",
                            table);
            for (Map<String, Object> fk : fkRows) {
                String childTable = fk.get("child_table").toString();
                String fkCol = fk.get("fk_column").toString();
                List<String> childPkCols = queryPrimaryKeyColumns(childTable);
                if (!childPkCols.isEmpty()) {
                    registerSegment(childTable, segmentName, childPkCols, List.of(fkCol));
                    log.debug(
                            "DLI auto-discovered child: {} → {} (FK: {})",
                            segmentName,
                            childTable,
                            fkCol);
                }
            }
        } catch (Exception e) {
            log.warn(
                    "DLI auto-discover hierarchy failed for '{}': {}", segmentName, e.getMessage());
        }
    }

    /** Query primary key columns for a table using constraint_type (no naming convention). */
    private List<String> queryPrimaryKeyColumns(String table) {
        List<Map<String, Object>> rows =
                jdbcTemplate.queryForList(
                        "SELECT kcu.column_name FROM information_schema.table_constraints tc JOIN"
                            + " information_schema.key_column_usage kcu ON kcu.constraint_name ="
                            + " tc.constraint_name WHERE tc.table_name = ? AND tc.constraint_type ="
                            + " 'PRIMARY KEY' ORDER BY kcu.ordinal_position",
                        table);
        return rows.stream().map(r -> r.get("column_name").toString()).toList();
    }

    /**
     * Direction 2: After GU/GN finds a row, set parent context for all child segments. This allows
     * subsequent GNP calls to auto-resolve parent FK values.
     */
    private void setParentContextFromRow(String segmentName, Map<String, Object> row) {
        // Auto-discover hierarchy if not registered
        autoDiscoverChildren(segmentName);

        SegmentMeta meta = hierarchy.get(segmentName.toUpperCase());
        if (meta == null) {
            return;
        }

        for (String child : meta.childSegments) {
            SegmentMeta childMeta = hierarchy.get(child);
            if (childMeta == null) {
                continue;
            }

            // Map parent PK values → child FK columns
            // Parent PK and child FK are positionally aligned (FK[0] references PK[0])
            Map<String, Object> fkValues = new LinkedHashMap<>();
            for (int i = 0; i < childMeta.fkColumns.size(); i++) {
                String childFkCol = childMeta.fkColumns.get(i);
                // Get value from parent row using parent PK column (not child FK column)
                Object val = null;
                if (i < meta.pkColumns.size()) {
                    String parentPkCol = meta.pkColumns.get(i);
                    val = row.get(parentPkCol);
                    if (val == null) {
                        val = row.get(toSnakeCase(parentPkCol));
                    }
                }
                // Fallback: try child FK column name directly on parent row
                if (val == null) {
                    val = row.get(childFkCol);
                }
                if (val == null) {
                    val = row.get(toSnakeCase(childFkCol));
                }
                fkValues.put(childFkCol, val);
            }
            parentContextsHolder.get().put(child, new ParentContext(segmentName, fkValues));
            // Reset child position (new parent scope)
            resetPositionFor(child);
        }
    }

    // --- Reflection helpers (unchanged) ---

    private void copyFromMap(Map<String, Object> row, Object target) {
        if (target == null) {
            return;
        }
        // Byte-storage model: the segment I/O area is the program FieldStore. Populate the
        // segment's byte-backed fields by COBOL name (db_column "pa_acct_id" → "PA-ACCT-ID").
        if (target instanceof FieldStore) {
            FieldStore fs = (FieldStore) target;
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String cobolName = entry.getKey().toUpperCase().replace('_', '-');
                if (entry.getValue() != null && fs.has(cobolName)) {
                    fs.setString(cobolName, String.valueOf(entry.getValue()));
                }
            }
            return;
        }
        Class<?> clazz = target.getClass();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String dbColumn = entry.getKey().toLowerCase();
            String fieldName = toCamelCase(dbColumn);
            try {
                Field field = findField(clazz, fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    Object value = convertValue(entry.getValue(), field.getType());
                    field.set(target, value);
                }
            } catch (Exception e) {
                log.trace(
                        "Could not map column {} to field {}: {}",
                        dbColumn,
                        fieldName,
                        e.getMessage());
            }
        }
    }

    private void extractFieldsForUpdate(
            Object from,
            String table,
            List<String> setClauses,
            List<Object> params,
            Set<String> skipColumns) {
        // Byte model (see extractFieldsForInsert): REPL writes the segment image from a FieldStore;
        // drive SET columns from the table schema, skipping PK columns (they live in the WHERE).
        if (from instanceof FieldStore fs) {
            for (String col : tableColumns(table)) {
                if (skipColumns.contains(col)) {
                    continue;
                }
                String cobol = col.toUpperCase().replace('_', '-');
                if (!fs.has(cobol)) {
                    continue;
                }
                setClauses.add(col + " = ?");
                params.add(fs.getString(cobol));
            }
            return;
        }
        Class<?> current = from.getClass();
        while (current != null && !current.getPackageName().startsWith("java.")) {
            for (Field f : current.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                f.setAccessible(true);
                try {
                    Object value = f.get(from);
                    if (value == null) {
                        continue;
                    }
                    String column = toSnakeCase(f.getName());
                    if (skipColumns.contains(column)) {
                        continue;
                    }
                    setClauses.add(column + " = ?");
                    params.add(value);
                } catch (IllegalAccessException e) {
                    /* skip */
                }
            }
            current = current.getSuperclass();
        }
    }

    private final Map<String, List<String>> tableColumnsCache =
            new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Column names of a table in declaration order (cached). Drives byte-model FieldStore writes.
     */
    private List<String> tableColumns(String table) {
        return tableColumnsCache.computeIfAbsent(
                table.toLowerCase(),
                t -> {
                    try {
                        return jdbcTemplate
                                .queryForList(
                                        "SELECT column_name FROM information_schema.columns WHERE"
                                                + " table_name = ? ORDER BY ordinal_position",
                                        t)
                                .stream()
                                .map(r -> r.get("column_name").toString())
                                .toList();
                    } catch (Exception e) {
                        log.trace("DLI tableColumns({}) failed: {}", t, e.getMessage());
                        return List.of();
                    }
                });
    }

    private void extractFieldsForInsert(
            Object from,
            String table,
            List<String> columns,
            List<String> placeholders,
            List<Object> params) {
        // Byte model: the segment I/O area is a FieldStore (no reflectable POJO fields — its data
        // lives in byte buffers). ISRT writes the full segment image, so drive the column set from
        // the table schema and read each value by its COBOL name.
        if (from instanceof FieldStore fs) {
            for (String col : tableColumns(table)) {
                String cobol = col.toUpperCase().replace('_', '-');
                if (!fs.has(cobol)) {
                    continue;
                }
                columns.add(col);
                placeholders.add("?");
                params.add(fs.getString(cobol));
            }
            return;
        }
        Class<?> current = from.getClass();
        while (current != null && !current.getPackageName().startsWith("java.")) {
            for (Field f : current.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                f.setAccessible(true);
                try {
                    Object value = f.get(from);
                    if (value == null) {
                        continue;
                    }
                    columns.add(toSnakeCase(f.getName()));
                    placeholders.add("?");
                    params.add(value);
                } catch (IllegalAccessException e) {
                    /* skip */
                }
            }
            current = current.getSuperclass();
        }
    }

    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && !current.getPackageName().startsWith("java.")) {
            for (Field f : current.getDeclaredFields()) {
                if (f.getName().equalsIgnoreCase(fieldName)) {
                    return f;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }
        if (targetType == String.class) {
            return String.valueOf(value);
        }
        if (targetType == int.class || targetType == Integer.class)
            return (value instanceof Number)
                    ? ((Number) value).intValue()
                    : Integer.parseInt(value.toString());
        if (targetType == long.class || targetType == Long.class)
            return (value instanceof Number)
                    ? ((Number) value).longValue()
                    : Long.parseLong(value.toString());
        if (targetType == short.class || targetType == Short.class)
            return (value instanceof Number)
                    ? ((Number) value).shortValue()
                    : Short.parseShort(value.toString());
        if (targetType == BigDecimal.class)
            return (value instanceof BigDecimal) ? value : new BigDecimal(value.toString());
        if (targetType == double.class || targetType == Double.class)
            return (value instanceof Number)
                    ? ((Number) value).doubleValue()
                    : Double.parseDouble(value.toString());
        return value;
    }

    static String toSnakeCase(String name) {
        if (name == null) {
            return null;
        }
        if (name.contains("-")) {
            return name.replace("-", "_").toLowerCase();
        }
        // All uppercase (e.g., IMS field "ACCNTID") → just lowercase, no split
        if (name.equals(name.toUpperCase())) {
            return name.toLowerCase();
        }
        // camelCase → snake_case
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append('_');
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    static String toCamelCase(String column) {
        if (column == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (char c : column.toCharArray()) {
            if (c == '_' || c == '-') {
                nextUpper = true;
            } else {
                sb.append(nextUpper ? Character.toUpperCase(c) : c);
                nextUpper = false;
            }
        }
        return sb.toString();
    }
}

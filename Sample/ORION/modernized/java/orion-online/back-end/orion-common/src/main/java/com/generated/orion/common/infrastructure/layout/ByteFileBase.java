package com.generated.orion.common.infrastructure.layout;

import com.generated.orion.common.infrastructure.DatasetEnums;
import com.generated.orion.common.infrastructure.SqlDialect;
import com.generated.orion.common.infrastructure.Utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * New file base: I/O reads and writes directly on byte[] via {@link RecordBuffer}. No generic POJO,
 * no parseRecord/serializeRecord.
 *
 * <p>Subclasses declare:
 *
 * <ul>
 *   <li>{@link #getFileName()} — COBOL file name
 *   <li>{@link #getAssignTo()} — DD-name (ASSIGN TO)
 *   <li>{@link #layoutResourcePath()} — classpath path of the XML layout
 *   <li>{@link #isBinary()} — override to true for fixed-length binary files
 * </ul>
 */
public abstract class ByteFileBase implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ByteFileBase.class);

    private static final ConcurrentMap<String, RecordSchema> LAYOUT_CACHE =
            new ConcurrentHashMap<>();

    /** Alias so service code (which emits as FileOpenMode.INPUT/OUTPUT) can call it. */
    public static final DatasetEnums.FileOpenMode INPUT = DatasetEnums.FileOpenMode.INPUT;

    public static final DatasetEnums.FileOpenMode OUTPUT = DatasetEnums.FileOpenMode.OUTPUT;
    public static final DatasetEnums.FileOpenMode EXTEND = DatasetEnums.FileOpenMode.EXTEND;
    public static final DatasetEnums.FileOpenMode IO_OPEN = DatasetEnums.FileOpenMode.IO;

    private String fileStatus = "00";
    private boolean endOfFile = false;
    private boolean isOpen = false;
    private DatasetEnums.FileOpenMode openMode;
    private boolean invalidKey = false;

    private InputStream in;
    private OutputStream out;

    /**
     * Backing channel for OUTPUT/EXTEND {@link #out}. Held so {@link #flush()} can {@code
     * force(true)} buffered writes durably to disk — otherwise Windows leaves the directory-entry
     * size stale (Explorer shows 0 bytes) until the handle closes at CLOSE.
     */
    private java.nio.channels.FileChannel outChannel;

    /**
     * IO mode uses a single seekable file handle so REWRITE writes back at the position of the most
     * recent READ (COBOL semantics). The legacy two-stream approach (separate BufferedInputStream +
     * BufferedOutputStream) misaligned positions — read advances `in` but `out` stays at byte 0, so
     * REWRITE would clobber the FIRST record instead of the last-read one.
     */
    private RandomAccessFile raf;

    /** File offset of the most recent READ (binary mode), -1 if not set. */
    private long lastReadPos = -1L;

    /* ── File-mode ISAM index state ─────────────────────────────────────────
     *  Active only when raf != null && !isSequentialFile() (INDEXED + file mode).
     *  Rebuilt on every {@link #open(DatasetEnums.FileOpenMode)} via {@link #rebuildIndex()},
     *  cleared on {@link #close()}. Memory-only — no on-disk index sidecar (per
     *  [[feedback_disk_fill_incident]]). Scratch files are small (<1MB typical), so
     *  O(n) scan-on-open is sub-millisecond and avoids stale-index coherence issues.
     */
    /** Primary key (or composite key, space-joined leaf field names) → byte offset. */
    private java.util.TreeMap<String, Long> primaryIndex;

    /** Alt-key indexes: name → (key value → list of offsets). List supports DUPLICATES. */
    private java.util.Map<String, java.util.TreeMap<String, java.util.List<Long>>> altIndexes;

    /**
     * Tombstones: bit set per record slot (offset / recordLength) marked deleted by file-mode
     * DELETE.
     */
    private java.util.BitSet deletedOffsets;

    /**
     * Browse cursor (primary or alt-key flattened) for {@link #readNext()} after {@link
     * #fileStart}.
     */
    private java.util.Iterator<java.util.Map.Entry<String, Long>> browseCursor;

    /**
     * Alt-key DUPLICATES cursor state: after {@link #fileReadByKey(String, Object)} on an alt key,
     * subsequent readNext() advances through the list of duplicates.
     */
    private String altCursorKeyName;

    private String altCursorKeyValue;
    private int altCursorIndex;
    private java.util.List<Long> altCursorList;

    private RecordBuffer buffer;
    private RecordSchema layout;

    /* ── SQL backend state (active only when JdbcTemplate is injected) ─────────────── */

    /**
     * JdbcTemplate injected by FileSet@PostConstruct when cobol.file.sql=true. Null means SQL mode
     * is OFF and all I/O goes through the byte/file path.
     */
    private JdbcTemplate jdbcTemplate;

    /** sqlMode = (jdbcTemplate != null). Cached for hot-path checks. */
    private boolean sqlMode = false;

    /** Browse cursor: cached result-set rows for SEQUENTIAL READ NEXT or START + READ NEXT. */
    private List<Map<String, Object>> sqlBrowseResults;

    private int sqlBrowseIndex = 0;

    /**
     * Direction the active {@link #sqlBrowseResults} page is ordered/walked. A forward cursor
     * (built by {@link #sqlStart}/READ NEXT) is ascending; a reverse cursor (built by {@link
     * #sqlReadPrev}) is descending. COBOL allows READ NEXT and READ PRIOR after the same START, so
     * a direction change must rebuild the page anchored at the current key instead of walking the
     * existing page the wrong way (which returned records in the wrong order and, for a random-key
     * mistranslation, surfaced STS=23 → false EF001 aborts).
     */
    private boolean sqlBrowseDescending = false;

    /**
     * Reverse-browse anchor for a {@link #sqlReadPrev} that follows a {@link #sqlStart}.
     *
     * <p>COBOL READ PRIOR is "read-at-then-move": after {@code START KEY NOT < K} positions the
     * cursor AT the first record {@code >= K}, the first READ PRIOR returns THAT record, and each
     * subsequent READ PRIOR steps one record backward.
     *
     * <p>{@code lastStartCols} = the key columns START positioned on; {@code lastStartAnchorVals} =
     * the key values of the record the cursor is positioned AT (the first row {@code >= K},
     * captured from the ASC page START built). {@link #sqlReadPrev} builds a descending page {@code
     * WHERE tuple <= anchor} so the first PRIOR returns the positioned record itself.
     */
    private List<String> lastStartCols;

    private Object[] lastStartAnchorVals;

    /** Last successful read's key (used by REWRITE/DELETE). Pipe-delimited for composite keys. */
    private Object lastReadKey;

    /** row_seq from last read row (for precise REWRITE/DELETE without key dependency). */
    private Long lastRowSeq;

    /** Key recorded by READ FOR UPDATE; takes priority over lastReadKey in subsequent REWRITE. */
    private Object lastUpdateKey;

    /** Insertion counter for SEQUENTIAL tables (synthetic row_seq). */
    private long sqlSeqCounter = 0;

    /** Vendor ISAM SELECT WHERE results (separate from browse cursor). */
    private List<Map<String, Object>> selectedRecords = new ArrayList<>();

    private int selectedIndex = 0;

    /**
     * True after {@link #doSqlSelectWhere} runs, regardless of whether it returned rows or hit a
     * SQL exception. Read by {@link #sqlReadNext} so an empty selectedRecords AFTER a SELECT WHERE
     * signals EOF (not a fall-through to keyed-read using stale lastReadKey, which can produce
     * infinite same-row reads if the SELECT WHERE failed silently). Reset by {@link #scratch},
     * {@link #close}, and the next {@link #doSqlSelectWhere} call.
     */
    private boolean selectWhereActive = false;

    /**
     * Per-table column allow-list, populated lazily from INFORMATION_SCHEMA. Filters out
     * POJO/layout fields that don't exist in the DB schema (covers REDEFINES alts).
     */
    private static final Map<String, Set<String>> SCHEMA_COLUMNS_CACHE = new ConcurrentHashMap<>();

    /**
     * Per-table list of GENERATED ALWAYS identity columns — omit from INSERT/UPDATE since
     * PostgreSQL rejects explicit values for these (SQLSTATE 428C9). Discovered 2026-06-04 against
     * customer PG: apgctl/hiotrn/hsyuka/hkbtrn.idno are GENERATED ALWAYS.
     */
    private static final Map<String, Set<String>> IDENTITY_ALWAYS_CACHE = new ConcurrentHashMap<>();

    /** Tables already verified to exist (or created). Static so all instances of a file share. */
    private static final Set<String> ENSURED_TABLES = Collections.synchronizedSet(new HashSet<>());

    /** Reverse lookup: snake_case column name → COBOL field name. Lazy per-instance. */
    private Map<String, LayoutField> snakeToField;

    private static final int BROWSE_PAGE_SIZE = 5000;

    /* ── Sub-class API ─────────────────────────────────────────────── */

    public abstract String getFileName();

    public abstract String getAssignTo();

    protected abstract String layoutResourcePath();

    /**
     * COBOL sequential files are fixed-length binary by default (no record separator). Subclasses
     * override to false for LINE SEQUENTIAL (newline-terminated text records). The generator
     * decides this at emit time — there is no runtime flag.
     */
    protected boolean isBinary() {
        return true;
    }

    /* ── Layout / Buffer ───────────────────────────────────────────── */

    public RecordSchema layout() {
        if (layout == null) {
            String path = layoutResourcePath();
            layout = LAYOUT_CACHE.computeIfAbsent(path, LayoutLoader::loadFile);
        }
        return layout;
    }

    public RecordBuffer buffer() {
        if (buffer == null) {
            buffer = new RecordBuffer(layout());
        }
        return buffer;
    }

    /**
     * Reset record buffer. The no-arg form is canonical (dynamic mode); the legacy {@code
     * setRecord(Object)} compat shim throws if the param is non-null.
     */
    public void setRecord() {
        buffer().clear();
    }

    public void setRecord(Object obj) {
        if (obj != null) {
            throw new IllegalArgumentException(
                    "setRecord(Object) is a compat shim — non-null arg is meaningless in dynamic"
                            + " mode. Use setRecord() no-arg to reset buffer.");
        }
        buffer().clear();
    }

    public RecordBuffer getRecord() {
        return buffer();
    }

    /**
     * Enable SQL backend mode by injecting a JdbcTemplate (gated by FileSet.initSqlMode() which
     * checks {@code cobol.file.sql=true}). When set, all I/O ops (read/write/rewrite/ delete/start)
     * route through SQL paths instead of byte/file paths. Passing null disables SQL mode (revert to
     * binary).
     */
    public void setJdbcTemplate(JdbcTemplate jdbc) {
        this.jdbcTemplate = jdbc;
        this.sqlMode = (jdbc != null);
    }

    /** Whether SQL backend is active for this file. */
    protected boolean isSqlMode() {
        return sqlMode && jdbcTemplate != null;
    }

    /**
     * SQL table name derived from COBOL file name (lowercase + hyphens→underscores). Override in
     * subclass to use a different mapping. Default: HMESSG → "hmessg".
     */
    public String getTableName() {
        String fn = getFileName();
        return fn == null ? null : fn.toLowerCase().replace("-", "_");
    }

    /**
     * COBOL RECORD KEY clause (space-separated for composite keys). Default: auto-detect from
     * layout by finding a group whose name ends in {@code -KEY}, returning the space-joined names
     * of its leaf fields. Override in subclass for explicit control.
     */
    public String getRecordKey() {
        List<String> cols = autoDetectKeyFields();
        return cols.isEmpty() ? null : String.join(" ", cols);
    }

    /**
     * Whether this is a SEQUENTIAL file (no record key). Resolves via {@link #getRecordKey()} —
     * subclass overrides (e.g. {@code HsekyuFile.getRecordKey() = "SEK-SEKYUCD"}) take precedence
     * over layout auto-detection. SEQUENTIAL files get a synthetic {@code row_seq} on INSERT;
     * INDEXED files let PG assign row_seq via {@code nextval()}.
     */
    protected boolean isSequentialFile() {
        String rk = getRecordKey();
        return rk == null || rk.trim().isEmpty();
    }

    /**
     * Auto-detect the RECORD KEY fields from the layout XML. Conventions, in order: 1. A group
     * whose name ends with {@code -KEY} (e.g. HMESSG MES-KEY → MES-MSGCD). 2. A leaf field whose
     * name ends with {@code -KEY} (e.g. HCNTRL CTL-KEY). 3. Multiple sibling leaf fields ending
     * with {@code -KEY} → composite.
     */
    private List<String> autoDetectKeyFields() {
        List<String> result = new ArrayList<>();
        SchemaGroup root = layout().root();
        if (root == null) {
            return result;
        }
        // Pass 1: -KEY group
        collectKeyFields(root, result);
        if (!result.isEmpty()) {
            return result;
        }
        // Pass 2: first -KEY leaf field (avoid collecting from REDEFINES alts, which
        // would produce columns not in the DB schema). LayoutField.isRedefines() only
        // flags the field's own attr — fields inside a REDEFINES group return false —
        // so we cannot rely on it. Take the FIRST match: layout leaves are in document
        // order, so the primary record's -KEY field appears before any REDEFINES alt's.
        for (LayoutField f : layout().leaves()) {
            String n = f.getName();
            if (n == null) {
                continue;
            }
            String up = n.toUpperCase();
            if (up.endsWith("-KEY")) {
                result.add(n);
                return result;
            }
        }
        return result;
    }

    private void collectKeyFields(SchemaGroup grp, List<String> result) {
        for (SchemaNode child : grp.children()) {
            if (child instanceof SchemaGroup g) {
                if (g.isRedefines()) continue; // never take key from REDEFINES alt
                String n = g.getName();
                if (n != null && n.toUpperCase().endsWith("-KEY")) {
                    // Collect leaf field names recursively under this key group.
                    collectLeafFields(g, result);
                    return;
                }
                collectKeyFields(g, result);
                if (!result.isEmpty()) {
                    return;
                }
            }
        }
    }

    private void collectLeafFields(SchemaGroup grp, List<String> result) {
        for (SchemaNode child : grp.children()) {
            if (child instanceof LayoutField f) {
                if (f.isRedefines()) {
                    continue;
                }
                result.add(f.getName());
            } else if (child instanceof SchemaGroup g) {
                collectLeafFields(g, result);
            }
        }
    }

    /**
     * Parse record key into individual column names (snake_case, hyphen → underscore). Each column
     * goes through {@link #dbColumn} to translate via ColumnMappings when the customer DB schema
     * diverges from the generated default.
     */
    public List<String> getKeyColumns() {
        return resolveKeyColumns(getRecordKey());
    }

    /**
     * Resolve a COBOL START key field-name list into DB column names.
     *
     * <p>{@code keySpec} is the space-separated field-name list that {@code START ... KEY}
     * positions on — the primary RECORD KEY <em>or</em> any ALTERNATE key (the exact list the
     * generated code passes to {@link #start(String, String)} / {@link #startLast(String)}). Blank
     * {@code keySpec} falls back to the primary {@link #getRecordKey()}. Used by {@link
     * #sqlStartLast} so a START on an alt key positions on the right columns instead of silently
     * reverting to the primary key.
     */
    public List<String> resolveKeyColumns(String keySpec) {
        String rk = (keySpec == null || keySpec.trim().isEmpty()) ? getRecordKey() : keySpec;
        if (rk == null || rk.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> cols = new ArrayList<>();
        for (String part : rk.split("\\s+")) {
            if (part.isEmpty()) {
                continue;
            }
            String snake = toSnake(part);
            String mapped = dbColumn(snake);
            if (isSkippedColumn(mapped)) {
                // Skipped key column would silently drop the WHERE predicate; fall back to raw
                // name so the SQL fails loud at execute time.
                LOG.error(
                        "Record key column {}.{} maps to SKIP — falling back to raw name",
                        getTableName(),
                        snake);
                cols.add(snake);
            } else {
                cols.add(mapped);
            }
        }
        return cols;
    }

    /**
     * True when {@code keySpec} is a space-separated list of real record FIELD names (so {@link
     * #sqlStartLast} can position on it directly). Distinguishes an alternate-key field list (every
     * token is a leaf field) from a bare FD key-NAME alias declared {@code RECORD KEY IS AGE-KEY} —
     * the alias is not a column, so it must fall back to the primary key. Probes {@link
     * #snakeToField()} using the SAME post-ColumnMappings translation its keys were built with.
     */
    boolean isKeyFieldList(String keySpec) {
        if (keySpec == null || keySpec.trim().isEmpty()) {
            return false;
        }
        Map<String, LayoutField> fields = snakeToField();
        for (String part : keySpec.trim().split("\\s+")) {
            if (part.isEmpty()) {
                continue;
            }
            String mapped = dbColumn(toSnake(part));
            if (isSkippedColumn(mapped) || !fields.containsKey(mapped)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Translate a COBOL-faithful snake_case column name to the target DB column. Identity unless
     * the ColumnMappings feature is generated (the {@code --column-mapping} flag); then it bridges
     * generated → customer DB schema. Used by every SQL emit site.
     */
    protected final String dbColumn(String snakeCol) {
        return snakeCol;
    }

    /**
     * True when {@link #dbColumn} mapped a column to the SKIP sentinel (column omitted from SQL).
     */
    protected final boolean isSkippedColumn(String mappedCol) {
        return false;
    }

    /** First key column (for ORDER BY / range queries that work on lead-key only). */
    public String getKeyColumnName() {
        List<String> cols = getKeyColumns();
        return cols.isEmpty() ? null : cols.get(0);
    }

    /**
     * Build multi-column WHERE clause for composite keys, type-aware: numeric leaves use {@code
     * CAST(col AS BIGINT)=CAST(? AS BIGINT)} so values like buffer-formatted {@code "001"} match
     * PG's {@code 1::int}; string leaves keep the VARCHAR comparison. Without the type split,
     * numeric keys with leading zeros (e.g. COBOL {@code PIC 9(3)} reading {@code "001"} from
     * buffer) never matched PG's {@code CAST(int_col AS VARCHAR)='1'}, so every numeric-key READ
     * returned 0 rows and the COBOL program saw {@code invalidKey=true}.
     */
    private String buildKeyWhere() {
        List<String> cols = getKeyColumns();
        StringBuilder w = new StringBuilder();
        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) {
                w.append(" AND ");
            }
            String col = cols.get(i);
            w.append(keyColRef(col)).append("=").append(keyParamRef(col));
        }
        return dialect().normalizeWhere(w.toString());
    }

    /**
     * Runtime SQL dialect (Postgres/Oracle/H2) detected from the live connection, cached.
     * Centralizes vendor-specific SQL (system catalogs, CREATE TABLE, CAST types).
     */
    private SqlDialect dialect() {
        return jdbcTemplate == null ? SqlDialect.POSTGRES : SqlDialect.of(jdbcTemplate);
    }

    /**
     * Find the primary-record LayoutField for this snake_case column and return true if its
     * javaType is a numeric primitive/wrapper. Used by {@link #buildKeyWhere()} to decide cast
     * type. Falls back to VARCHAR (returns false) when the column can't be located.
     */
    private boolean isNumericKeyColumn(String snakeCol) {
        if (snakeCol == null) {
            return false;
        }
        for (LayoutField f : layout().leaves()) {
            if (f.isRedefines()) {
                continue;
            }
            if (!snakeCol.equals(toSnake(f.getName()))) {
                continue;
            }
            String jt = f.getJavaType();
            return "int".equals(jt)
                    || "long".equals(jt)
                    || "double".equals(jt)
                    || "java.math.BigDecimal".equals(jt)
                    || "BigDecimal".equals(jt);
        }
        return false;
    }

    /** Split a pipe-delimited composite key into individual values (single-key: returns one). */
    private Object[] splitKey(Object key) {
        List<String> cols = getKeyColumns();
        String k = key instanceof String s ? s.trim() : String.valueOf(key).trim();
        if (cols.size() <= 1) return new Object[] {k};
        String[] parts = k.split("\\|", cols.size());
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        return parts;
    }

    /** Capture row_seq from SQL result for precise REWRITE/DELETE targeting. */
    private void captureRowSeq(Map<String, Object> row) {
        Object seq = row != null ? row.get("row_seq") : null;
        lastRowSeq = (seq instanceof Number num) ? num.longValue() : null;
    }

    /** ORDER BY clause for browse queries (joins all key columns). */
    private String keyOrderBy() {
        List<String> cols = getKeyColumns();
        return cols.isEmpty() ? "row_seq" : String.join(", ", cols);
    }

    /**
     * Descending ORDER BY over the given columns — appends {@code DESC} to EACH column, not just
     * the last. {@code "a, b, c DESC"} sorts a,b ASC and only c DESC (a classic SQL trap); reverse
     * browse (READ PRIOR / START LAST) needs every key column descending.
     */
    private String orderByDesc(List<String> cols) {
        if (cols == null || cols.isEmpty()) {
            return "row_seq DESC";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(cols.get(i)).append(" DESC");
        }
        return sb.toString();
    }

    /**
     * True when a browse anchor key is absent or all-zero — used by the bare {@link #sqlReadPrev}
     * (no preceding START) to decide between an anchored reverse scan and a full-table DESC page.
     * {@link #extractKeyFromCurrentRecord()} joins composite segments with {@code |}, so an
     * all-zero composite renders as {@code "0|0|0"}; treat every segment being empty/all-zeros as
     * "no key".
     */
    private static boolean isEmptyOrZeroKey(String keyVal) {
        if (keyVal == null || keyVal.isEmpty()) {
            return true;
        }
        for (String seg : keyVal.split("\\|", -1)) {
            String s = seg.trim();
            if (!s.isEmpty() && !Utility.isAllZeros(s)) {
                return false;
            }
        }
        return true;
    }

    /** Map COBOL START operator to SQL comparison. */
    private String mapStartOp(String op) {
        if (op == null) {
            return ">=";
        }
        switch (op.toUpperCase().trim()) {
            case "EQUAL", "=":
                return ">="; // ISAM EQUAL = positions cursor, READ NEXT browses
            case "GREATER THAN", "GREATER", ">":
                return ">";
            case "LESS THAN", "LESS", "<":
                return "<";
            case "NOT GREATER THAN", "NOT GREATER", "<=":
                return "<=";
            default:
                return ">="; // NOT LESS THAN (default COBOL START)
        }
    }

    /**
     * 2026-05-28 — HDR140-WRITE-001 fix: per-save-block transaction.
     *
     * <p>Faithful translation of the COBOL {@code COMMIT scope="vendor"} semantic: writes
     * accumulate in the connection's transaction through the save chain (UPD-000 → UPD-* →
     * UPD-990); the COBOL COMMIT (emitted as {@code fileSet.commit()}) commits them atomically.
     * Reads outside a save block are independent — each borrows a fresh connection from the pool.
     *
     * <p>First write lazy-binds a connection (auto-commit=false) via {@link
     * org.springframework.transaction.support.TransactionSynchronizationManager}. All subsequent
     * JdbcTemplate operations on the same thread share it (Spring's {@code
     * DataSourceUtils.getConnection} returns the bound resource if present). {@code
     * fileSet.commit()} or {@code fileSet.rollbackIfPending()} unbinds.
     *
     * <p>Replaces the previous "BUG #632" design (per-statement commit + neutralized savepoints)
     * which (a) violated the COBOL atomicity contract because each write committed individually —
     * the explicit {@code COMMIT scope="vendor"} became a no-op, and (b) triggered an SQLSTATE
     * 25P02 cascade whenever any statement threw inside the {@code auto-commit=false} connection
     * because nothing rolled back the aborted transaction (savepoints were no-ops).
     */
    private int update(String sql, Object[] args) {
        ensureSaveTxBound();
        return jdbcTemplate.update(sql, args);
    }

    /**
     * Lazy-bind a connection with {@code auto-commit=false} as the current save-block transaction
     * unless one is already bound. Subsequent JdbcTemplate ops on the same thread automatically use
     * it (via {@code DataSourceUtils.getConnection}). Released by {@link
     * com.generated.infrastructure.FileSet#commit()} (atomic commit) or {@link
     * com.generated.infrastructure.FileSet#rollbackIfPending()} (ABORT path).
     */
    private void ensureSaveTxBound() {
        javax.sql.DataSource ds = jdbcTemplate.getDataSource();
        if (ds == null) {
            return;
        }
        if (org.springframework.transaction.support.TransactionSynchronizationManager.hasResource(
                ds)) {
            return;
        }
        try {
            java.sql.Connection conn = ds.getConnection();
            conn.setAutoCommit(false);
            org.springframework.transaction.support.TransactionSynchronizationManager.bindResource(
                    ds, new org.springframework.jdbc.datasource.ConnectionHolder(conn));
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("ensureSaveTxBound failed: " + e.getMessage(), e);
        }
    }

    /**
     * Real JDBC savepoints on the currently-bound save-block connection. A failed read or write
     * rolls back to its savepoint, leaving the surrounding save-block tx alive for subsequent ops
     * (this is what eliminates the 25P02 cascade). If no save-block tx is bound (read-only ops
     * outside a save block), these are safe no-ops.
     */
    private java.sql.Savepoint currentSavepoint;

    private void sqlSavepoint() {
        javax.sql.DataSource ds = jdbcTemplate.getDataSource();
        if (ds == null
                || !org.springframework.transaction.support.TransactionSynchronizationManager
                        .hasResource(ds)) {
            currentSavepoint = null;
            return;
        }
        try {
            java.sql.Connection conn =
                    org.springframework.jdbc.datasource.DataSourceUtils.getConnection(ds);
            if (!conn.getAutoCommit()) {
                currentSavepoint = conn.setSavepoint();
            }
        } catch (java.sql.SQLException ignored) {
            currentSavepoint = null; // savepoint unsupported → degrade to full rollback below
        }
    }

    private void sqlRelease() {
        if (currentSavepoint == null) {
            return;
        }
        try {
            java.sql.Connection conn =
                    org.springframework.jdbc.datasource.DataSourceUtils.getConnection(
                            jdbcTemplate.getDataSource());
            conn.releaseSavepoint(currentSavepoint);
        } catch (java.sql.SQLException ignored) {
        } finally {
            currentSavepoint = null;
        }
    }

    /**
     * Surgical rollback to a previously-set savepoint. No-op when no savepoint was set — the
     * earlier safety fallback (full {@code conn.rollback()}) was incorrect: in test mode Hikari
     * runs auto-commit=false but per-op semantics, so a blind full-rollback undid legitimate prior
     * writes. Caller must always pair this with {@link #sqlSavepoint()} for the rollback to
     * actually clear PG's aborted-tx state.
     */
    private void sqlRollbackSp() {
        if (currentSavepoint == null) return; // no savepoint = no-op (test mode without tx mgr)
        javax.sql.DataSource ds = jdbcTemplate.getDataSource();
        if (ds == null) {
            currentSavepoint = null;
            return;
        }
        try {
            java.sql.Connection conn =
                    org.springframework.jdbc.datasource.DataSourceUtils.getConnection(ds);
            if (!conn.getAutoCommit()) conn.rollback(currentSavepoint); // surgical
        } catch (java.sql.SQLException ignored) {
            /* connection unusable — Hikari discards on return */
        } finally {
            currentSavepoint = null;
        }
    }

    /** COBOL field name to snake_case SQL column name. {@code MES-MSGCD → mes_msgcd}. */
    protected static String toSnake(String fieldName) {
        if (fieldName == null) {
            return "";
        }
        return fieldName.toLowerCase().replace('-', '_');
    }

    /**
     * Reverse lookup table for non-OCCURS fields: snake_case column name → primary-record
     * LayoutField. Built lazily and cached. Used by {@link #applyRowToBuffer(Map)} for the simple
     * case; OCCURS columns (suffix {@code _<n>}) are handled separately via {@link #occursLookup}.
     */
    private Map<String, LayoutField> snakeToField() {
        if (snakeToField == null) {
            buildLookups();
        }
        return snakeToField;
    }

    /**
     * Reverse lookup for OCCURS leaves: snake_case base column → (LayoutField, occursCount). When a
     * SQL column ends with {@code _<n>}, we look up the base name here and write the value at
     * occurrence n (1-based) using the buffer's subscript-aware setters.
     */
    private Map<String, LayoutField> occursLookup;

    private Map<String, Integer> occursCount;

    private void buildLookups() {
        Map<String, LayoutField> simple = new HashMap<>();
        Map<String, LayoutField> occLeafs = new HashMap<>();
        Map<String, Integer> occCounts = new HashMap<>();
        walkForLookups(layout().root(), 1, simple, occLeafs, occCounts);
        snakeToField = simple;
        occursLookup = occLeafs;
        occursCount = occCounts;
    }

    private void walkForLookups(
            SchemaNode n,
            int occursMult,
            Map<String, LayoutField> simple,
            Map<String, LayoutField> occLeafs,
            Map<String, Integer> occCounts) {
        if (n.isRedefines()) {
            return;
        }
        if (n instanceof LayoutField f) {
            String nm = f.getName();
            if (isFillerName(nm)) {
                return;
            }
            String col = toSnake(nm);
            if (occursMult > 1) {
                // OCCURS: occLeafs keys are the post-translation BASE (column name minus the
                // trailing _N index). Derive by translating index 0 and stripping the suffix
                // so applyRowToBuffer's split matches whatever shape the PG schema uses.
                String firstFull = col + "_0";
                String firstMapped = dbColumn(firstFull);
                if (isSkippedColumn(firstMapped)) {
                    return;
                }
                String base = firstMapped;
                int us = firstMapped.lastIndexOf('_');
                if (us > 0) {
                    base = firstMapped.substring(0, us);
                }
                occLeafs.putIfAbsent(base, f);
                occCounts.putIfAbsent(base, occursMult);
            } else {
                String mapped = dbColumn(col);
                if (isSkippedColumn(mapped)) {
                    return;
                }
                simple.putIfAbsent(mapped, f);
            }
            return;
        }
        SchemaGroup g = (SchemaGroup) n;
        int innerMult = occursMult * Math.max(1, g.getOccurs());
        for (SchemaNode c : g.children()) {
            walkForLookups(c, innerMult, simple, occLeafs, occCounts);
        }
    }

    private Map<String, LayoutField> occursLookup() {
        if (occursLookup == null) {
            buildLookups();
        }
        return occursLookup;
    }

    private Map<String, Integer> occursCount() {
        if (occursCount == null) {
            buildLookups();
        }
        return occursCount;
    }

    /**
     * Walk the primary-record tree and produce a snake_case column → value map for INSERT/UPDATE.
     * For fields nested in an OCCURS group, emits one column per occurrence ({@code
     * par_param_nam_1..par_param_nam_N}, 1-based, flattened — matches legacy POJO generator's
     * column naming convention used by docker/schema.sql). FILLER fields skipped.
     */
    private Map<String, Object> extractColumnsFromBuffer() {
        Map<String, Object> cols = new LinkedHashMap<>();
        walkForExtract(layout().root(), 1, cols);
        splitBlobOnWrite(cols);
        return cols;
    }

    /**
     * Hook for subclasses backing a NEC-style chunked-blob table (e.g. HCNTRL: 400-byte {@code
     * ctl_data} stored as 20 × 20-byte columns {@code data_001..020}). Default no-op;
     * generator-emitted subclasses override to split the single buffer column into chunks BEFORE
     * the column map is sent to INSERT/UPDATE. Mirror of {@link #concatBlobOnRead}.
     */
    protected void splitBlobOnWrite(Map<String, Object> cols) {}

    /**
     * Inverse of {@link #splitBlobOnWrite}: concatenate chunk columns coming back from a SELECT
     * into the single buffer column expected by the COBOL field accessor. Runs BEFORE {@code
     * snakeToField()} lookup in {@link #applyRowToBuffer}.
     */
    protected void concatBlobOnRead(Map<String, Object> row) {}

    private void walkForExtract(SchemaNode n, int occursMult, Map<String, Object> cols) {
        if (n.isRedefines()) {
            return;
        }
        if (n instanceof LayoutField f) {
            String nm = f.getName();
            if (isFillerName(nm)) {
                return;
            }
            String col = toSnake(nm);
            if (occursMult > 1) {
                // DB schema uses 0-based suffixes (e.g. eil_hanbaimei1_0, eil_hanbaimei1_1) but
                // buffer access (readBufferValueAt) takes 1-based COBOL subscripts. Emit columns
                // as col_0..col_{N-1} while reading buffer at subscripts 1..N. Without this
                // offset, INSERT/UPDATE writes to col_1..col_N — col_0 stays empty (no matching
                // DB column for _N → silently lost).
                for (int i = 0; i < occursMult; i++) {
                    String suffixed = col + "_" + i;
                    String mapped = dbColumn(suffixed);
                    if (isSkippedColumn(mapped)) {
                        continue;
                    }
                    try {
                        cols.put(mapped, readBufferValueAt(f, i + 1));
                    } catch (Exception ignore) {
                        cols.put(mapped, "");
                    }
                }
            } else {
                String mapped = dbColumn(col);
                if (isSkippedColumn(mapped)) {
                    return;
                }
                try {
                    cols.put(mapped, readBufferValue(f));
                } catch (Exception ignore) {
                    cols.put(mapped, "");
                }
            }
            return;
        }
        SchemaGroup g = (SchemaGroup) n;
        int innerMult = occursMult * Math.max(1, g.getOccurs());
        for (SchemaNode c : g.children()) {
            walkForExtract(c, innerMult, cols);
        }
    }

    /** Read a leaf value at OCCURS index {@code sub} (1-based). */
    private Object readBufferValueAt(LayoutField f, int sub) {
        String name = f.getName();
        String jt = f.getJavaType();
        if (jt == null) {
            return buffer().getString(name, sub);
        }
        switch (jt) {
            case "int":
                return buffer().getInt(name, sub);
            case "long":
                return buffer().getLong(name, sub);
            case "double":
                return buffer().getDouble(name, sub);
            case "java.math.BigDecimal", "BigDecimal":
                return buffer().getDecimal(name, sub);
            case "boolean":
                return buffer().getBoolean(name, sub) ? 1 : 0;
            case "String":
                return buffer().getString(name, sub);
            default:
                return buffer().getString(name, sub);
        }
    }

    private static boolean isFillerName(String name) {
        if (name == null) {
            return false;
        }
        String up = name.toUpperCase();
        if (up.equals("FILLER")) {
            return true;
        }
        if (up.startsWith("FILLER-") || up.startsWith("FILLER_")) {
            String rest = up.substring(7);
            if (rest.isEmpty()) {
                return true;
            }
            for (int i = 0; i < rest.length(); i++) {
                if (!Character.isDigit(rest.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /** Read a single field value from the buffer, decoding per LayoutField type. */
    private Object readBufferValue(LayoutField f) {
        String name = f.getName();
        String jt = f.getJavaType();
        if (jt == null) {
            return buffer().getString(name);
        }
        switch (jt) {
            case "int":
                return buffer().getInt(name);
            case "long":
                return buffer().getLong(name);
            case "double":
                return buffer().getDouble(name);
            case "java.math.BigDecimal", "BigDecimal":
                return buffer().getDecimal(name);
            case "boolean":
                return buffer().getBoolean(name) ? 1 : 0;
            case "String":
                return buffer().getString(name);
            default:
                return buffer().getString(name);
        }
    }

    /**
     * Apply a SQL result row back into the byte buffer. Columns whose snake-case name matches a
     * primary-record leaf are written via type-appropriate RecordBuffer setter. OCCURS columns
     * (suffix {@code _<n>}) are routed via subscript-aware setters into the proper occurrence.
     * Unknown columns (e.g. row_seq, REDEFINES-only) are skipped.
     */
    private void applyRowToBuffer(Map<String, Object> row) {
        if (row == null) {
            return;
        }
        concatBlobOnRead(row);
        Map<String, LayoutField> simple = snakeToField();
        Map<String, LayoutField> occLeafs = occursLookup();
        Map<String, Integer> occCounts = occursCount();
        for (Map.Entry<String, Object> e : row.entrySet()) {
            String col = e.getKey() == null ? null : e.getKey().toLowerCase();
            Object val = e.getValue();
            if (col == null || val == null) {
                continue;
            }
            LayoutField f = simple.get(col);
            if (f != null) {
                writeBufferValue(f, val);
                continue;
            }
            // Try OCCURS column shape: <base>_<n> (1-based)
            int us = col.lastIndexOf('_');
            if (us > 0) {
                String base = col.substring(0, us);
                String idxStr = col.substring(us + 1);
                LayoutField fo = occLeafs.get(base);
                if (fo != null && !idxStr.isEmpty()) {
                    try {
                        int n = Integer.parseInt(idxStr);
                        Integer count = occCounts.get(base);
                        // DB column suffix is 0-based (col_0..col_{N-1}); buffer subscript is
                        // 1-based COBOL convention. Accept both 0-based and 1-based for
                        // backward compat: prefer 0-based mapping (n+1 → buffer sub).
                        if (count != null && n >= 0 && n < count) {
                            writeBufferValueAt(fo, val, n + 1);
                            continue;
                        }
                        if (count != null && n >= 1 && n <= count) {
                            // legacy 1-based DB schema (some older programs)
                            writeBufferValueAt(fo, val, n);
                        }
                    } catch (NumberFormatException ignore) {
                        /* not an index */
                    }
                }
            }
            // unknown column → skip silently (matches legacy)
        }
    }

    /** Write a leaf value at OCCURS index {@code sub} (1-based). */
    private void writeBufferValueAt(LayoutField f, Object val, int sub) {
        String name = f.getName();
        String jt = f.getJavaType();
        try {
            if (jt == null || "String".equals(jt)) {
                buffer().setString(name, String.valueOf(val), sub);
                return;
            }
            switch (jt) {
                case "int":
                    if (val instanceof Number num) {
                        buffer().setInt(name, num.intValue(), sub);
                    } else buffer().setInt(name, Integer.parseInt(String.valueOf(val).trim()), sub);
                    break;
                case "long":
                    if (val instanceof Number num) {
                        buffer().setLong(name, num.longValue(), sub);
                    } else buffer().setLong(name, Long.parseLong(String.valueOf(val).trim()), sub);
                    break;
                case "double":
                    if (val instanceof Number num) {
                        buffer().setDouble(name, num.doubleValue(), sub);
                    } else
                        buffer().setDouble(
                                        name, Double.parseDouble(String.valueOf(val).trim()), sub);
                    break;
                case "java.math.BigDecimal", "BigDecimal":
                    if (val instanceof BigDecimal bd) {
                        buffer().setDecimal(name, bd, sub);
                    } else if (val instanceof Number num)
                        buffer().setDecimal(name, BigDecimal.valueOf(num.doubleValue()), sub);
                    else buffer().setDecimal(name, new BigDecimal(String.valueOf(val).trim()), sub);
                    break;
                case "boolean":
                    boolean b;
                    if (val instanceof Boolean bool) {
                        b = bool;
                    } else if (val instanceof Number num) b = num.intValue() != 0;
                    else b = !"0".equals(String.valueOf(val).trim());
                    buffer().setBoolean(name, b, sub);
                    break;
                default:
                    buffer().setString(name, String.valueOf(val), sub);
            }
        } catch (Exception ignore) {
            // best-effort write: leave occurrence at previous value
        }
    }

    private void writeBufferValue(LayoutField f, Object val) {
        String name = f.getName();
        String jt = f.getJavaType();
        try {
            if (jt == null || "String".equals(jt)) {
                buffer().setString(name, String.valueOf(val));
                return;
            }
            switch (jt) {
                case "int":
                    if (val instanceof Number num) {
                        buffer().setInt(name, num.intValue());
                    } else buffer().setInt(name, Integer.parseInt(String.valueOf(val).trim()));
                    break;
                case "long":
                    if (val instanceof Number num) {
                        buffer().setLong(name, num.longValue());
                    } else buffer().setLong(name, Long.parseLong(String.valueOf(val).trim()));
                    break;
                case "double":
                    if (val instanceof Number num) {
                        buffer().setDouble(name, num.doubleValue());
                    } else buffer().setDouble(name, Double.parseDouble(String.valueOf(val).trim()));
                    break;
                case "java.math.BigDecimal", "BigDecimal":
                    if (val instanceof BigDecimal bd) {
                        buffer().setDecimal(name, bd);
                    } else if (val instanceof Number num)
                        buffer().setDecimal(name, BigDecimal.valueOf(num.doubleValue()));
                    else buffer().setDecimal(name, new BigDecimal(String.valueOf(val).trim()));
                    break;
                case "boolean":
                    boolean b;
                    if (val instanceof Boolean bool) {
                        b = bool;
                    } else if (val instanceof Number num) b = num.intValue() != 0;
                    else b = !"0".equals(String.valueOf(val).trim());
                    buffer().setBoolean(name, b);
                    break;
                default:
                    buffer().setString(name, String.valueOf(val));
            }
        } catch (Exception ex) {
            // best-effort write; surface the actual cause in WARN log. Previously
            // a silent catch — hid SQL-row → buffer write failures (eg. picture
            // mismatches, NString encoding edge cases). Added 2026-05-28 alongside
            // SUBSCRIPT-PARSE-001 — silent failures here surface as blank cells in
            // FE display, hard to root-cause without telemetry.
            LOG.warn(
                    "writeBufferValue FAILED field={} javaType={} val={} err={}: {}",
                    f.getName(),
                    f.getJavaType(),
                    val == null
                            ? "null"
                            : (val.getClass().getSimpleName()
                                    + ":"
                                    + String.valueOf(val)
                                            .substring(
                                                    0, Math.min(40, String.valueOf(val).length()))),
                    ex.getClass().getSimpleName(),
                    ex.getMessage());
        }
    }

    /** Extract record key value from current buffer. Composite keys returned pipe-delimited. */
    public String extractKeyFromCurrentRecord() {
        String rk = getRecordKey();
        if (rk == null || rk.isEmpty()) {
            return "";
        }
        String[] parts = rk.split("\\s+");
        if (parts.length == 1) {
            try {
                return buffer().getString(parts[0]).trim();
            } catch (Exception e) {
                return "";
            }
        }
        StringBuilder pk = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                pk.append("|");
            }
            try {
                pk.append(buffer().getString(parts[i]).trim());
            } catch (Exception ignore) {
                /* leave empty segment */
            }
        }
        return pk.toString();
    }

    /**
     * Ensure the SQL table exists. Uses layout.sqlColumnTypes() for column types and builds a
     * composite PRIMARY KEY from RECORD KEY metadata. Idempotent + thread-safe.
     */
    protected void sqlEnsureTable() {
        String table = getTableName();
        if (table == null || ENSURED_TABLES.contains(table) || jdbcTemplate == null) {
            return;
        }
        try {
            try {
                Integer cnt =
                        jdbcTemplate.queryForObject(
                                dialect().tableExistsSql(), Integer.class, table);
                if (cnt != null && cnt > 0) {
                    ENSURED_TABLES.add(table);
                    return;
                }
            } catch (Exception e) {
                /* proceed to create */
            }
            Map<String, String> typedCols = layout().sqlColumnTypes();
            if (typedCols == null || typedCols.isEmpty()) {
                ENSURED_TABLES.add(table);
                return;
            }
            List<String> keyCols = getKeyColumns();
            StringBuilder ddl =
                    new StringBuilder(dialect().createTableKeyword()).append(table).append(" (");
            boolean first = true;
            if (isSequentialFile()) {
                ddl.append("row_seq BIGINT PRIMARY KEY");
                first = false;
            }
            for (Map.Entry<String, String> e : typedCols.entrySet()) {
                if (isFillerName(snakeToSourceName(e.getKey()))) {
                    continue;
                }
                if (!first) {
                    ddl.append(", ");
                }
                ddl.append(e.getKey()).append(' ').append(dialect().mapColumnType(e.getValue()));
                first = false;
            }
            if (!isSequentialFile() && !keyCols.isEmpty()) {
                ddl.append(", PRIMARY KEY (").append(String.join(", ", keyCols)).append(")");
            }
            ddl.append(")");
            // BUG-003 fix (back-ported from poc-june-v2-delivery MANUAL_FIXES.patch).
            // Create the work/data table on a SEPARATE auto-commit connection so the DDL
            // persists immediately and independently of the program's transaction. Running it
            // on the shared (transactional) connection means the CREATE is rolled back when the
            // program later aborts — and the program aborts precisely because the table was
            // missing (e.g. HDR140 work files hdr140_tm1_f/tm2_f/tm4_f), so the table can never
            // materialise. An isolated auto-commit CREATE TABLE IF NOT EXISTS breaks that cycle.
            javax.sql.DataSource ds = jdbcTemplate.getDataSource();
            if (ds != null) {
                try (java.sql.Connection ddlConn = ds.getConnection()) {
                    boolean prevAuto = ddlConn.getAutoCommit();
                    ddlConn.setAutoCommit(true);
                    try (java.sql.Statement ddlSt = ddlConn.createStatement()) {
                        ddlSt.execute(ddl.toString());
                    } finally {
                        ddlConn.setAutoCommit(prevAuto);
                    }
                }
            } else {
                jdbcTemplate.execute(ddl.toString());
            }
            ENSURED_TABLES.add(table);
        } catch (Exception e) {
            ENSURED_TABLES.add(table); // avoid retry storm on broken DB
        }
    }

    /**
     * Initialize synthetic row_seq counter from MAX(row_seq)+1 at open time so that subsequent
     * WRITEs against a SEQUENTIAL table that already holds rows (eg. carry-over from a prior app
     * run) don't collide on the PRIMARY KEY. Without this, every fresh JVM start resets the counter
     * to 0 and the first N writes against any non-empty SEQUENTIAL table fail with 23505
     * duplicate-key — observed against {@code prn_f} where COBOL writes printer records during
     * interactive flows. Non-SEQUENTIAL files leave row_seq to PG's DEFAULT nextval(), so this is a
     * no-op for INDEXED files.
     */
    protected void sqlInitSeqCounter() {
        if (!isSequentialFile() || jdbcTemplate == null) {
            return;
        }
        String table = getTableName();
        if (table == null) {
            return;
        }
        try {
            Long max =
                    jdbcTemplate.queryForObject(
                            "SELECT COALESCE(MAX(row_seq), 0) FROM " + table, Long.class);
            sqlSeqCounter = max != null ? max : 0L;
        } catch (Exception e) {
            // Table missing or unreadable — leave counter at default (0). Subsequent
            // INSERTs will fail loudly if there really are conflicting rows.
            sqlSeqCounter = 0L;
        }
    }

    /** Rough reverse of toSnake — only used by isFillerName fallback in DDL emit. */
    private static String snakeToSourceName(String snake) {
        return snake == null ? "" : snake.toUpperCase().replace('_', '-');
    }

    /**
     * Lookup the DB schema column set for our table (cached). Used by WRITE to filter layout
     * columns that don't exist as real DB columns (covers REDEFINES gaps).
     */
    private Set<String> schemaColumns() {
        String table = getTableName();
        if (table == null || jdbcTemplate == null) {
            return Collections.emptySet();
        }
        return SCHEMA_COLUMNS_CACHE.computeIfAbsent(
                table.toLowerCase(),
                tn -> {
                    Set<String> result = new HashSet<>();
                    try {
                        jdbcTemplate.query(
                                dialect().columnsSql(),
                                (rs, i) -> result.add(rs.getString(1).toLowerCase()),
                                table);
                    } catch (Exception ignore) {
                        /* best-effort: ignore */
                    }
                    return result;
                });
    }

    /**
     * Lookup PostgreSQL GENERATED ALWAYS identity columns (cached). INSERT/UPDATE must OMIT these —
     * supplying an explicit value triggers SQLSTATE 428C9 ("cannot insert a non-DEFAULT value into
     * column ..."). Discovered 2026-06-04: customer PG defines apgctl/hiotrn/hsyuka/hkbtrn.idno as
     * GENERATED ALWAYS. Auto-detection via information_schema is robust to future identity columns
     * added customer-side.
     */
    private Set<String> identityAlwaysColumns() {
        String table = getTableName();
        if (table == null || jdbcTemplate == null) {
            return Collections.emptySet();
        }
        return IDENTITY_ALWAYS_CACHE.computeIfAbsent(
                table.toLowerCase(),
                tn -> {
                    Set<String> result = new HashSet<>();
                    try {
                        jdbcTemplate.query(
                                dialect().identityColumnsSql(),
                                new Object[] {table},
                                (rs, i) -> result.add(rs.getString(1).toLowerCase()));
                    } catch (Exception ignore) {
                    }
                    return result;
                });
    }

    /* ── SQL CRUD operations ────────────────────────────────────────── */

    /** SQL READ — by current buffer key (INDEXED) or sequential browse (SEQUENTIAL). */
    protected boolean sqlRead() {
        sqlSavepoint();
        try {
            if (openMode == DatasetEnums.FileOpenMode.OUTPUT) {
                fileStatus = "47";
                return false;
            }
            // Browse mode: return next row from cached results
            if (sqlBrowseResults != null) {
                if (sqlBrowseIndex >= sqlBrowseResults.size()) {
                    endOfFile = true;
                    fileStatus = "10";
                    return false;
                }
                Map<String, Object> row = sqlBrowseResults.get(sqlBrowseIndex++);
                applyRowToBuffer(row);
                captureRowSeq(row);
                if (!isSequentialFile()) {
                    lastReadKey = extractKeyFromCurrentRecord();
                } else lastReadKey = row.get("row_seq");
                fileStatus = "00";
                return true;
            }
            // SEQUENTIAL: browse all rows in insertion order
            if (isSequentialFile()) {
                String orderCol = getKeyColumnName() != null ? getKeyColumnName() : "row_seq";
                try {
                    sqlBrowseResults =
                            jdbcTemplate.queryForList(
                                    "SELECT * FROM "
                                            + getTableName()
                                            + " ORDER BY "
                                            + orderCol
                                            + " FETCH FIRST "
                                            + BROWSE_PAGE_SIZE
                                            + " ROWS ONLY");
                } catch (Exception e) {
                    sqlBrowseResults = new ArrayList<>();
                }
                sqlBrowseIndex = 0;
                return sqlRead();
            }
            // INDEXED: try extracting key from buffer, fallback to browse-all
            String keyVal =
                    lastReadKey != null ? lastReadKey.toString() : extractKeyFromCurrentRecord();
            if (keyVal == null || keyVal.isEmpty() || "0".equals(keyVal)) {
                sqlBrowseResults =
                        jdbcTemplate.queryForList(
                                "SELECT * FROM "
                                        + getTableName()
                                        + " ORDER BY "
                                        + keyOrderBy()
                                        + " FETCH FIRST "
                                        + BROWSE_PAGE_SIZE
                                        + " ROWS ONLY");
                sqlBrowseIndex = 0;
                return sqlRead();
            }
            List<Map<String, Object>> rows =
                    jdbcTemplate.queryForList(
                            "SELECT * FROM " + getTableName() + " WHERE " + buildKeyWhere(),
                            splitKey(keyVal));
            if (rows.isEmpty()) {
                invalidKey = true;
                fileStatus = "23";
                return false;
            }
            applyRowToBuffer(rows.get(0));
            captureRowSeq(rows.get(0));
            sqlRelease();
            fileStatus = "00";
            return true;
        } catch (Exception e) {
            sqlRollbackSp();
            fileStatus = "30";
            return false;
        }
    }

    /** SQL READ by explicit key (Online branch pattern). Bypasses buffer-key extraction. */
    protected boolean sqlReadByExplicitKey(Object key) {
        sqlSavepoint();
        try {
            if (openMode == DatasetEnums.FileOpenMode.OUTPUT) {
                fileStatus = "47";
                return false;
            }
            sqlBrowseResults = null;
            sqlBrowseIndex = 0;
            endOfFile = false;
            String keyVal = key != null ? String.valueOf(key).trim() : "";
            // A keyed random READ with an empty/zero key must look up that EXACT key: COBOL
            // returns INVALID KEY (fileStatus 23) when no such record exists, so the program's
            // INVALID-KEY handler fires. Normalize an empty key to "0" for a numeric key
            // column so the typed WHERE is well-formed (CAST('' AS BIGINT) would throw).
            if (keyVal.isEmpty()) {
                List<String> kc = getKeyColumns();
                if (!kc.isEmpty() && isNumericKeyColumn(kc.get(0))) {
                    keyVal = "0";
                }
            }
            List<Map<String, Object>> rows =
                    jdbcTemplate.queryForList(
                            "SELECT * FROM " + getTableName() + " WHERE " + buildKeyWhere(),
                            splitKey(keyVal));
            if (rows.isEmpty()) {
                invalidKey = true;
                fileStatus = "23";
                return false;
            }
            applyRowToBuffer(rows.get(0));
            captureRowSeq(rows.get(0));
            lastReadKey = extractKeyFromCurrentRecord();
            sqlRelease();
            fileStatus = "00";
            return true;
        } catch (Exception e) {
            LOG.warn(
                    "sqlReadByExplicitKey FAILED file={} table={} key='{}' err={}: {}",
                    getFileName(),
                    getTableName(),
                    key,
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e);
            sqlRollbackSp();
            fileStatus = "30";
            return false;
        }
    }

    /**
     * SQL READ by ALTERNATE KEY field (COBOL `READ FILE KEY altKey`). Maps the COBOL field name to
     * its snake-case column, builds a single-column WHERE, queries the first matching row, and
     * applies it to the buffer. Sets invalidKey=true + fsts=23 when no row matches — caller's `IF
     * INVALID KEY` handler then fires.
     */
    protected boolean sqlReadByAlternateKey(String altKeyField, Object key) {
        sqlSavepoint();
        try {
            if (openMode == DatasetEnums.FileOpenMode.OUTPUT) {
                fileStatus = "47";
                return false;
            }
            sqlBrowseResults = null;
            sqlBrowseIndex = 0;
            endOfFile = false;
            String keyVal = key != null ? String.valueOf(key).trim() : "";
            if (altKeyField == null || altKeyField.isEmpty()) {
                // Caller passed null — fall back to primary-key path.
                return sqlReadByExplicitKey(key);
            }
            // Composite alternate key: altKeyField may be a space-joined column tuple ("F1 F2 F3")
            // and keyVal the matching pipe-delimited value tuple ("v1|v2|v3") — build a per-column
            // AND WHERE. Single-column alternate keys (no space / no pipe) fall through as a 1-term
            // WHERE, identical to the previous behavior.
            String[] altCols = altKeyField.trim().split("\\s+");
            String[] keyParts = keyVal.split("\\|", -1);
            StringBuilder where = new StringBuilder();
            java.util.List<Object> params = new java.util.ArrayList<>();
            for (int i = 0; i < altCols.length; i++) {
                String col = dbColumn(toSnake(altCols[i]));
                if (i > 0) {
                    where.append(" AND ");
                }
                where.append(keyColRef(col)).append("=").append(keyParamRef(col));
                params.add(i < keyParts.length ? keyParts[i].trim() : "");
            }
            List<Map<String, Object>> rows =
                    jdbcTemplate.queryForList(
                            "SELECT * FROM "
                                    + getTableName()
                                    + " WHERE "
                                    + dialect().normalizeWhere(where.toString()),
                            params.toArray());
            if (rows.isEmpty()) {
                invalidKey = true;
                fileStatus = "23";
                return false;
            }
            applyRowToBuffer(rows.get(0));
            captureRowSeq(rows.get(0));
            lastReadKey = extractKeyFromCurrentRecord();
            sqlRelease();
            fileStatus = "00";
            return true;
        } catch (Exception e) {
            sqlRollbackSp();
            fileStatus = "30";
            return false;
        }
    }

    /** SQL READ NEXT — iterate cached browse cursor, selectedRecords, or start a new browse. */
    protected boolean sqlReadNext() {
        sqlSavepoint();
        try {
            if (openMode == DatasetEnums.FileOpenMode.OUTPUT) {
                fileStatus = "47";
                return false;
            }
            // A forward read consumes any pending READ-PRIOR anchor from a preceding START, so a
            // later stray PRIOR falls back to lastReadKey rather than re-anchoring on the START.
            lastStartCols = null;
            lastStartAnchorVals = null;
            if (sqlBrowseResults != null) {
                if (sqlBrowseIndex >= sqlBrowseResults.size()) {
                    endOfFile = true;
                    fileStatus = "10";
                    return false;
                }
                Map<String, Object> row = sqlBrowseResults.get(sqlBrowseIndex++);
                applyRowToBuffer(row);
                captureRowSeq(row);
                if (!isSequentialFile()) {
                    lastReadKey = extractKeyFromCurrentRecord();
                } else lastReadKey = row.get("row_seq");
                fileStatus = "00";
                return true;
            }
            if (selectWhereActive) {
                // SELECT WHERE result set is authoritative — iterate or signal EOF.
                // Do NOT fall through to keyed-read with stale lastReadKey: if doSqlSelectWhere
                // returned 0 rows (legitimately empty, OR exception-cleared due to invalid SQL),
                // the COBOL caller expects AT END, not infinite same-row reads.
                if (selectedIndex >= selectedRecords.size()) {
                    endOfFile = true;
                    fileStatus = "10";
                    return false;
                }
                Map<String, Object> row = selectedRecords.get(selectedIndex++);
                applyRowToBuffer(row);
                captureRowSeq(row);
                lastReadKey = extractKeyFromCurrentRecord();
                fileStatus = "00";
                return true;
            }
            if (isSequentialFile()) {
                String orderCol = getKeyColumnName() != null ? getKeyColumnName() : "row_seq";
                try {
                    sqlBrowseResults =
                            jdbcTemplate.queryForList(
                                    "SELECT * FROM "
                                            + getTableName()
                                            + " ORDER BY "
                                            + orderCol
                                            + " FETCH FIRST "
                                            + BROWSE_PAGE_SIZE
                                            + " ROWS ONLY");
                } catch (Exception e) {
                    sqlBrowseResults = new ArrayList<>();
                }
                sqlBrowseIndex = 0;
                sqlBrowseDescending = false;
                return sqlReadNext();
            }
            // INDEXED without cursor: browse all records
            String keyVal =
                    lastReadKey != null ? lastReadKey.toString() : extractKeyFromCurrentRecord();
            if (keyVal == null || keyVal.isEmpty() || "0".equals(keyVal)) {
                sqlBrowseResults =
                        jdbcTemplate.queryForList(
                                "SELECT * FROM "
                                        + getTableName()
                                        + " ORDER BY "
                                        + keyOrderBy()
                                        + " FETCH FIRST "
                                        + BROWSE_PAGE_SIZE
                                        + " ROWS ONLY");
                sqlBrowseIndex = 0;
                sqlBrowseDescending = false;
                return sqlReadNext();
            }
            List<Map<String, Object>> rows =
                    jdbcTemplate.queryForList(
                            "SELECT * FROM " + getTableName() + " WHERE " + buildKeyWhere(),
                            splitKey(keyVal));
            if (rows.isEmpty()) {
                invalidKey = true;
                fileStatus = "23";
                return false;
            }
            applyRowToBuffer(rows.get(0));
            captureRowSeq(rows.get(0));
            sqlRelease();
            fileStatus = "00";
            return true;
        } catch (Exception e) {
            sqlRollbackSp();
            fileStatus = "30";
            return false;
        }
    }

    /** SQL READ FOR UPDATE — keyed read with row locking. */
    protected boolean sqlReadForUpdate() {
        sqlSavepoint();
        try {
            if (openMode == DatasetEnums.FileOpenMode.INPUT) {
                fileStatus = "47";
                return false;
            }
            String keyVal =
                    lastReadKey != null ? lastReadKey.toString() : extractKeyFromCurrentRecord();
            if (keyVal.isEmpty()) {
                fileStatus = "23";
                return false;
            }
            List<Map<String, Object>> rows =
                    jdbcTemplate.queryForList(
                            "SELECT * FROM "
                                    + getTableName()
                                    + " WHERE "
                                    + buildKeyWhere()
                                    + " FOR UPDATE",
                            splitKey(keyVal));
            if (rows.isEmpty()) {
                invalidKey = true;
                fileStatus = "23";
                return false;
            }
            applyRowToBuffer(rows.get(0));
            captureRowSeq(rows.get(0));
            lastReadKey = extractKeyFromCurrentRecord();
            lastUpdateKey = lastReadKey;
            sqlRelease();
            fileStatus = "00";
            return true;
        } catch (Exception e) {
            sqlRollbackSp();
            fileStatus = "30";
            return false;
        }
    }

    /**
     * SQL READ PREVIOUS — reverse browse (COBOL {@code READ ... PRIOR}).
     *
     * <p>Hitachi ISAM READ PRIOR is <em>read-at-then-move</em>: the first PRIOR after a START
     * returns the record the cursor is positioned AT, and each subsequent PRIOR steps one record
     * backward.
     *
     * <p>An already-descending cursor (built here on the first PRIOR after a START) is walked
     * forward — each step yields the next-older record. A cursor left ascending by {@link
     * #sqlStart}/READ NEXT must NOT be walked forward: that returns records in the wrong order. On
     * the first PRIOR we rebuild a descending page {@code WHERE tuple <= anchor}, where {@code
     * anchor} is the key of the record START positioned AT (captured in {@link
     * #lastStartAnchorVals}); because the anchor row itself satisfies {@code <= anchor}, the first
     * PRIOR returns it (read-at-then-move) and later PRIORs walk the DESC page onward.
     *
     * <p>Without a preceding START (a bare PRIOR), anchor from {@link #lastReadKey} with strict
     * {@code < key} as a best-effort reverse scan.
     */
    protected boolean sqlReadPrev() {
        sqlSavepoint();
        try {
            if (sqlBrowseResults != null && sqlBrowseDescending) {
                if (sqlBrowseIndex >= sqlBrowseResults.size()) {
                    endOfFile = true;
                    fileStatus = "10";
                    return false;
                }
                Map<String, Object> row = sqlBrowseResults.get(sqlBrowseIndex++);
                applyRowToBuffer(row);
                captureRowSeq(row);
                lastReadKey = extractKeyFromCurrentRecord();
                fileStatus = "00";
                return true;
            }
            // First PRIOR after a START (ascending/forward cursor): rebuild a descending page.
            if (lastStartCols != null && !lastStartCols.isEmpty() && lastStartAnchorVals != null) {
                // Read-at-then-move: `<= anchor` INCLUDES the positioned record so the first PRIOR
                // returns it. Composite-aware (correct for alt keys), not just the first column.
                List<Object> params = new ArrayList<>();
                String where = buildTupleWhere(lastStartCols, lastStartAnchorVals, "<=", params);
                sqlBrowseResults =
                        jdbcTemplate.queryForList(
                                "SELECT * FROM "
                                        + getTableName()
                                        + " WHERE "
                                        + where
                                        + " ORDER BY "
                                        + orderByDesc(lastStartCols)
                                        + " FETCH FIRST "
                                        + BROWSE_PAGE_SIZE
                                        + " ROWS ONLY",
                                params.toArray());
            } else {
                String keyVal =
                        lastReadKey != null
                                ? lastReadKey.toString()
                                : extractKeyFromCurrentRecord();
                String firstCol = getKeyColumnName();
                if (firstCol == null || isEmptyOrZeroKey(keyVal)) {
                    sqlBrowseResults =
                            jdbcTemplate.queryForList(
                                    "SELECT * FROM "
                                            + getTableName()
                                            + " ORDER BY "
                                            + orderByDesc(getKeyColumns())
                                            + " FETCH FIRST "
                                            + BROWSE_PAGE_SIZE
                                            + " ROWS ONLY");
                } else {
                    Object[] kp = splitKey(keyVal);
                    sqlBrowseResults =
                            jdbcTemplate.queryForList(
                                    "SELECT * FROM "
                                            + getTableName()
                                            + " WHERE "
                                            + dialect()
                                                    .normalizeWhere(
                                                            "CAST("
                                                                    + firstCol
                                                                    + " AS VARCHAR) < CAST(? AS"
                                                                    + " VARCHAR)")
                                            + " ORDER BY "
                                            + orderByDesc(getKeyColumns())
                                            + " FETCH FIRST "
                                            + BROWSE_PAGE_SIZE
                                            + " ROWS ONLY",
                                    kp[0]);
                }
            }
            sqlBrowseIndex = 0;
            sqlBrowseDescending = true;
            // Anchor consumed; subsequent PRIORs walk the DESC page (no re-anchor).
            lastStartCols = null;
            lastStartAnchorVals = null;
            if (sqlBrowseIndex >= sqlBrowseResults.size()) {
                endOfFile = true;
                fileStatus = "10";
                return false;
            }
            Map<String, Object> row = sqlBrowseResults.get(sqlBrowseIndex++);
            applyRowToBuffer(row);
            captureRowSeq(row);
            lastReadKey = extractKeyFromCurrentRecord();
            fileStatus = "00";
            return true;
        } catch (Exception e) {
            sqlRollbackSp();
            fileStatus = "30";
            return false;
        }
    }

    /** SQL WRITE — INSERT all primary-record columns. SEQUENTIAL adds synthetic row_seq. */
    protected void sqlWrite() {
        sqlSavepoint();
        try {
            if (openMode == DatasetEnums.FileOpenMode.INPUT) {
                fileStatus = "48";
                return;
            }
            Map<String, Object> cols = extractColumnsFromBuffer();
            if (isSequentialFile()) {
                cols.put("row_seq", ++sqlSeqCounter);
            }
            // Filter to columns actually present in DB schema
            Set<String> tableCols = schemaColumns();
            if (!tableCols.isEmpty()) {
                cols.entrySet().removeIf(e -> !tableCols.contains(e.getKey().toLowerCase()));
            }
            // Drop GENERATED ALWAYS identity columns — DB rejects explicit values (SQLSTATE 428C9)
            Set<String> identityAlways = identityAlwaysColumns();
            if (!identityAlways.isEmpty()) {
                cols.entrySet().removeIf(e -> identityAlways.contains(e.getKey().toLowerCase()));
            }
            if (cols.isEmpty()) {
                sqlRollbackSp();
                fileStatus = "30";
                return;
            }
            StringBuilder sql =
                    new StringBuilder("INSERT INTO ").append(getTableName()).append(" (");
            StringBuilder ph = new StringBuilder();
            List<Object> vals = new ArrayList<>();
            boolean first = true;
            for (Map.Entry<String, Object> e : cols.entrySet()) {
                if (!first) {
                    sql.append(',');
                    ph.append(',');
                }
                sql.append(e.getKey());
                ph.append('?');
                vals.add(e.getValue());
                first = false;
            }
            sql.append(") VALUES (").append(ph).append(')');
            // Wrap INSERT in savepoint: on duplicate-key/SQL error, PG marks the whole
            // txn aborted ("25P02") until ROLLBACK. Without the savepoint rollback the
            // caller's next read also fails with STS=30 — observed when HCZ010's APGCTL
            // INSERT collides on PGC-TANTOCD then HDR030's HCNTRL read cascades.
            try {
                update(sql.toString(), vals.toArray());
                sqlRelease();
                fileStatus = "00";
            } catch (DuplicateKeyException dup) {
                sqlRollbackSp();
                // Phase 1 e01fe13: COBOL WRITE INVALID KEY is triggered by Status 22 (Duplicate
                // Key).
                // Setting invalidKey=true here so isInvalidKey() returns true on duplicate-key
                // errors.
                // Without this, the generated COBOL `INVALID KEY` clause wouldn't fire.
                invalidKey = true;
                fileStatus = "22";
            } catch (Exception insertEx) {
                sqlRollbackSp();
                LOG.warn(
                        "sqlWrite FAILED file={} table={} err={}: {}",
                        getFileName(),
                        getTableName(),
                        insertEx.getClass().getSimpleName(),
                        insertEx.getMessage());
                fileStatus = "30";
            }
        } catch (Exception e) {
            sqlRollbackSp();
            fileStatus = "30";
        }
    }

    /** SQL REWRITE — UPDATE WHERE composite key (or row_seq if known). */
    protected void sqlRewrite() {
        // per-save-block tx: write accumulates in the lazy-bound connection (see update() above)
        sqlSavepoint();
        try {
            if (openMode == DatasetEnums.FileOpenMode.INPUT) {
                sqlRollbackSp();
                fileStatus = "48";
                return;
            }
            Object rwKey = lastUpdateKey != null ? lastUpdateKey : lastReadKey;
            if (rwKey == null && lastRowSeq == null) {
                sqlRollbackSp();
                fileStatus = "43";
                return;
            }
            Map<String, Object> cols = extractColumnsFromBuffer();
            Set<String> tableCols = schemaColumns();
            if (!tableCols.isEmpty()) {
                cols.entrySet().removeIf(e -> !tableCols.contains(e.getKey().toLowerCase()));
            }
            // Drop GENERATED ALWAYS identity columns — DB rejects explicit values (SQLSTATE 428C9)
            Set<String> identityAlways = identityAlwaysColumns();
            if (!identityAlways.isEmpty()) {
                cols.entrySet().removeIf(e -> identityAlways.contains(e.getKey().toLowerCase()));
            }
            if (cols.isEmpty()) {
                fileStatus = "30";
                return;
            }
            List<String> keyCols = getKeyColumns();
            Set<String> keyColSet = new HashSet<>(keyCols);
            StringBuilder sql = new StringBuilder("UPDATE ").append(getTableName()).append(" SET ");
            List<Object> vals = new ArrayList<>();
            boolean first = true;
            for (Map.Entry<String, Object> e : cols.entrySet()) {
                if (keyColSet.contains(e.getKey())) {
                    continue;
                }
                if (!first) {
                    sql.append(',');
                }
                sql.append(e.getKey()).append("=?");
                vals.add(e.getValue());
                first = false;
            }
            if (lastRowSeq != null) {
                sql.append(" WHERE row_seq=?");
                vals.add(lastRowSeq);
            } else {
                sql.append(" WHERE ").append(buildKeyWhere());
                Collections.addAll(vals, splitKey(rwKey));
            }
            int n = update(sql.toString(), vals.toArray());
            lastUpdateKey = null;
            lastRowSeq = null;
            sqlRelease();
            if (n > 0) {
                fileStatus = "00";
            } else {
                // Phase 1 e01fe13: COBOL REWRITE INVALID KEY is triggered by Status 23 (Record Not
                // Found).
                // Setting invalidKey=true so isInvalidKey() returns true when update hits 0 rows.
                invalidKey = true;
                fileStatus = "23";
            }
        } catch (Exception e) {
            sqlRollbackSp();
            LOG.warn(
                    "sqlRewrite FAILED file={} table={} err={}: {}",
                    getFileName(),
                    getTableName(),
                    e.getClass().getSimpleName(),
                    e.getMessage());
            fileStatus = "30";
        }
    }

    /** SQL DELETE — DELETE WHERE composite key (or row_seq if known). */
    protected void sqlDelete() {
        // per-save-block tx: write accumulates in the lazy-bound connection (see update() above)
        sqlSavepoint();
        try {
            if (openMode == DatasetEnums.FileOpenMode.INPUT) {
                sqlRollbackSp();
                fileStatus = "48";
                return;
            }
            if (lastReadKey == null && lastRowSeq == null) {
                sqlRollbackSp();
                fileStatus = "43";
                return;
            }
            int n;
            if (lastRowSeq != null) {
                n =
                        update(
                                "DELETE FROM " + getTableName() + " WHERE row_seq=?",
                                new Object[] {lastRowSeq});
                lastRowSeq = null;
            } else {
                n =
                        update(
                                "DELETE FROM " + getTableName() + " WHERE " + buildKeyWhere(),
                                splitKey(lastReadKey));
            }
            sqlRelease();
            fileStatus = n > 0 ? "00" : "23";
        } catch (Exception e) {
            sqlRollbackSp();
            LOG.warn(
                    "sqlDelete FAILED file={} table={} err={}: {}",
                    getFileName(),
                    getTableName(),
                    e.getClass().getSimpleName(),
                    e.getMessage());
            fileStatus = "30";
        }
    }

    /** SQL DELETE by explicit key (supports composite via pipe delimiter). */
    protected void sqlDeleteByKey(Object key) {
        // per-save-block tx: write accumulates in the lazy-bound connection (see update() above)
        sqlSavepoint();
        try {
            int n =
                    update(
                            "DELETE FROM " + getTableName() + " WHERE " + buildKeyWhere(),
                            splitKey(key));
            sqlRelease();
            fileStatus = n > 0 ? "00" : "23";
        } catch (Exception e) {
            sqlRollbackSp();
            LOG.warn(
                    "sqlDeleteByKey FAILED file={} table={} err={}: {}",
                    getFileName(),
                    getTableName(),
                    e.getClass().getSimpleName(),
                    e.getMessage());
            fileStatus = "30";
        }
    }

    /**
     * SQL START — position browse cursor from key with comparison operator.
     *
     * <p>COBOL emits {@code fileSet.getX().start("FIELD-NAME", "NOT LESS THAN")} where the {@code
     * key} arg is the KEY FIELD NAME (matches RECORD KEY declared in FD), not a comparison value.
     * The actual comparison value comes from the current record buffer (COBOL sets buffer fields
     * via accessors right before {@code start()}). Example:
     *
     * <pre>
     *   ws.setEshDatakb(1);
     *   ws.setEshBukacd(3100);
     *   fileSet.getHeshon().start("ESH-DATAKB", "NOT LESS THAN");
     *   // → SQL: SELECT * FROM heshon WHERE esh_datakb >= 1 ORDER BY esh_datakb
     * </pre>
     *
     * <p>Earlier this method used {@code key} (the field name string) as the comparison value —
     * produced SQL like {@code WHERE CAST(esh_datakb AS VARCHAR) >= 'ESH-DATAKB'} which always
     * returned zero rows for numeric columns. Now we always extract the value from the current
     * record buffer via {@link #extractKeyFromCurrentRecord()}.
     */
    protected boolean sqlStart(String key, String operator) {
        sqlSavepoint();
        invalidKey = false;
        try {
            String sqlOp = mapStartOp(operator);
            List<String> cols = getKeyColumns();
            // COBOL `START ... INVALID KEY` semantics: STS=23 must be paired with
            // invalidKey=true so generated code's `if (isInvalidKey()) <handler>` branch
            // fires. Without the flag, callers fall through to the abort path even
            // though they wired a non-fatal handler.
            if (cols.isEmpty()) {
                invalidKey = true;
                fileStatus = "23";
                return false;
            }

            // Pull each key field's current value directly from the record buffer.
            // COBOL `START` semantics: positions to first record where the composite key
            // tuple satisfies the operator vs current working-storage values.
            String[] keyParts = getRecordKey().trim().split("\\s+");
            Object[] vals = new Object[cols.size()];
            for (int i = 0; i < cols.size(); i++) {
                try {
                    vals[i] = buffer().getString(keyParts[i]).trim();
                } catch (Exception e) {
                    vals[i] = "";
                }
            }

            // Build a tuple comparison WHERE clause. For `>=` (or `>` / `<=` / `<`) on a
            // composite key, expand to the equivalent OR-chain to keep per-column type
            // casts intact (numeric columns: BIGINT; string: VARCHAR). For `=`, build a
            // straight AND-chain.
            StringBuilder where = new StringBuilder();
            List<Object> params = new ArrayList<>();
            if ("=".equals(sqlOp)) {
                for (int i = 0; i < cols.size(); i++) {
                    if (i > 0) {
                        where.append(" AND ");
                    }
                    where.append(buildKeyEq(cols.get(i)));
                    params.add(vals[i]);
                }
            } else {
                where.append(buildTupleWhere(cols, vals, sqlOp, params));
            }

            String sql =
                    "SELECT * FROM "
                            + getTableName()
                            + " WHERE "
                            + where
                            + " ORDER BY "
                            + keyOrderBy()
                            + " FETCH FIRST "
                            + BROWSE_PAGE_SIZE
                            + " ROWS ONLY";
            sqlBrowseResults = jdbcTemplate.queryForList(sql, params.toArray());
            sqlBrowseIndex = 0;
            sqlBrowseDescending = false;
            // sqlStart MUST reset endOfFile so a stale EOF from a prior empty SELECT
            // doesn't make the very next readNext() report AT END from the fresh cursor.
            endOfFile = false;
            if (sqlBrowseResults.isEmpty()) {
                lastStartCols = null;
                lastStartAnchorVals = null;
                sqlRelease();
                invalidKey = true;
                fileStatus = "23";
                return false;
            }
            // Capture the key of the record the cursor is positioned AT (first row of the ASC
            // page) so a following READ PRIOR (read-at-then-move) returns THIS record first, then
            // steps backward. Reads the anchor from the positioned row, not the START key values,
            // so `START NOT < (…,99999999,0)` anchors on the real next row, not the sentinel.
            lastStartCols = cols;
            Map<String, Object> anchorRow = new HashMap<>();
            for (Map.Entry<String, Object> e : sqlBrowseResults.get(0).entrySet()) {
                if (e.getKey() != null) {
                    anchorRow.put(e.getKey().toLowerCase(), e.getValue());
                }
            }
            lastStartAnchorVals = new Object[cols.size()];
            for (int i = 0; i < cols.size(); i++) {
                Object v = anchorRow.get(cols.get(i).toLowerCase());
                lastStartAnchorVals[i] = v == null ? "" : String.valueOf(v).trim();
            }
            sqlRelease();
            fileStatus = "00";
            return true;
        } catch (Exception e) {
            sqlRollbackSp();
            fileStatus = "30";
            return false;
        }
    }

    /**
     * SQL START LAST — position at the last record in the named key's order. Builds a descending
     * page (no key filter) so the following {@link #sqlReadPrev} walk returns the last record
     * first, then steps backward. Mirrors {@link #sqlStart} status handling: empty table → STS=23 +
     * invalidKey (COBOL START LAST INVALID KEY). Alt-key aware via {@link #resolveKeyColumns}.
     */
    protected boolean sqlStartLast(String key) {
        sqlSavepoint();
        invalidKey = false;
        try {
            String keySpec = isKeyFieldList(key) ? key : getRecordKey();
            List<String> cols = resolveKeyColumns(keySpec);
            if (cols.isEmpty()) {
                invalidKey = true;
                fileStatus = "23";
                return false;
            }
            sqlBrowseResults =
                    jdbcTemplate.queryForList(
                            "SELECT * FROM "
                                    + getTableName()
                                    + " ORDER BY "
                                    + orderByDesc(cols)
                                    + " FETCH FIRST "
                                    + BROWSE_PAGE_SIZE
                                    + " ROWS ONLY");
            sqlBrowseIndex = 0;
            sqlBrowseDescending = true;
            endOfFile = false;
            // START LAST positions absolutely at end-of-key; a following READ PRIOR must NOT
            // re-anchor from a stale key, so clear the remembered START anchor.
            lastStartCols = null;
            lastStartAnchorVals = null;
            if (sqlBrowseResults.isEmpty()) {
                sqlRelease();
                invalidKey = true;
                fileStatus = "23";
                return false;
            }
            sqlRelease();
            fileStatus = "00";
            return true;
        } catch (Exception e) {
            sqlRollbackSp();
            fileStatus = "30";
            return false;
        }
    }

    /**
     * Column side of a key predicate. String keys are stored SPACE-PADDED to the COBOL PIC width
     * (e.g. US-LOGIN X(12) → "admin "), but callers pass a TRIMMED key value, so the column must be
     * RTRIM'd or the equality never matches. Numeric keys cast to BIGINT (no padding).
     */
    private String keyColRef(String col) {
        return isNumericKeyColumn(col)
                ? "CAST(" + col + " AS BIGINT)"
                : "CAST(RTRIM(" + col + ") AS VARCHAR)";
    }

    /** Bind-param side of a key predicate (type-aware cast; param is already trimmed). */
    private String keyParamRef(String col) {
        return isNumericKeyColumn(col) ? "CAST(? AS BIGINT)" : "CAST(? AS VARCHAR)";
    }

    /** Equality predicate for one key column with type-aware cast (RTRIM string keys). */
    private String buildKeyEq(String col) {
        return dialect().normalizeWhere(keyColRef(col) + "=" + keyParamRef(col));
    }

    /** Comparison predicate (op = >, >=, <, <=) for one key column with type-aware cast. */
    private String buildKeyOp(String col, String op) {
        return dialect().normalizeWhere(keyColRef(col) + op + keyParamRef(col));
    }

    /**
     * Composite-key tuple comparison for {@code >}/{@code >=}/{@code <}/{@code <=}, expanded to the
     * equivalent OR-chain so per-column type casts stay intact:
     *
     * <pre>   (c1 OP v1)
     *        OR (c1=v1 AND c2 OP v2)
     *        OR (c1=v1 AND c2=v2 AND c3 OP v3) ...</pre>
     *
     * Prefix columns use strict comparison; the final column uses {@code op}. Parameters are
     * appended to {@code outParams} in the exact order the placeholders appear. Shared by {@link
     * #sqlStart} (forward position) and {@link #sqlReadPrev} (reverse position).
     */
    private String buildTupleWhere(
            List<String> cols, Object[] vals, String op, List<Object> outParams) {
        String strictOp = op.startsWith(">") ? ">" : "<";
        StringBuilder where = new StringBuilder("(");
        for (int b = 0; b < cols.size(); b++) {
            if (b > 0) {
                where.append(") OR (");
            }
            for (int i = 0; i < b; i++) {
                if (i > 0) {
                    where.append(" AND ");
                }
                where.append(buildKeyEq(cols.get(i)));
                outParams.add(vals[i]);
            }
            if (b > 0) {
                where.append(" AND ");
            }
            boolean isLast = (b == cols.size() - 1);
            where.append(buildKeyOp(cols.get(b), isLast ? op : strictOp));
            outParams.add(vals[b]);
        }
        where.append(')');
        return where.toString();
    }

    /* ── Vendor ISAM extensions (SELECT WHERE / readSelected / scratch) ─────────────── */

    /**
     * Vendor ISAM SELECT WHERE — query file by arbitrary SQL WHERE clause. Results stored in {@link
     * #selectedRecords} for iteration via {@link #readSelected()}.
     */
    public void doSqlSelectWhere(
            String whereClause, String orderByCol, boolean ascending, Object... params) {
        // Normalize any PostgreSQL-shaped CAST in a hand-passed / VendorIsam WHERE for the
        // connected dialect (no-op on PG/H2; BIGINT->NUMBER, VARCHAR->VARCHAR2 on Oracle).
        whereClause = dialect().normalizeWhere(whereClause);
        selectedRecords.clear();
        selectedIndex = 0;
        sqlBrowseResults = null;
        sqlBrowseIndex = 0;
        endOfFile = false;
        selectWhereActive = true;
        if (!isSqlMode()) {
            fileStatus = "00";
            return;
        }
        sqlSavepoint();
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM ").append(getTableName());
            if (whereClause != null && !whereClause.trim().isEmpty()) {
                sql.append(" WHERE ").append(whereClause);
            }
            if (orderByCol != null && !orderByCol.trim().isEmpty()) {
                sql.append(" ORDER BY ")
                        .append(orderByCol.trim().replaceAll("\\s+", ", "))
                        .append(ascending ? " ASC" : " DESC");
            }
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    if (params[i] instanceof String s) {
                        params[i] = s.trim();
                    }
                }
            }
            if (params != null && params.length > 0) {
                selectedRecords = jdbcTemplate.queryForList(sql.toString(), params);
            } else {
                selectedRecords = jdbcTemplate.queryForList(sql.toString());
            }
            sqlRelease();
            fileStatus = "00";
        } catch (Exception e) {
            sqlRollbackSp();
            selectedRecords = new ArrayList<>();
            fileStatus = "30";
        }
    }

    /** True if last SELECT WHERE found any records. */
    public boolean hasSelectedRecords() {
        return !selectedRecords.isEmpty();
    }

    /** Read next record from SELECT WHERE result set. Returns false at end-of-set. */
    public boolean readSelected() {
        if (selectedIndex < selectedRecords.size()) {
            applyRowToBuffer(selectedRecords.get(selectedIndex++));
            fileStatus = "00";
            return true;
        }
        fileStatus = "10";
        return false;
    }

    public int recordLength() {
        return layout().recordByteLength();
    }

    public String getFileStatus() {
        return fileStatus;
    }

    public boolean isEndOfFile() {
        return endOfFile;
    }

    public boolean isAtEnd() {
        return endOfFile;
    }

    public boolean isInvalidKey() {
        return invalidKey;
    }

    /* ── I/O lifecycle ─────────────────────────────────────────────── */

    /**
     * Whether the COBOL {@code SELECT} for this file carried a {@code FILE STATUS} clause.
     * Generated subclasses override it to true; the default is false so a hand-written or legacy
     * subclass keeps the abend-on-open-failure behaviour.
     */
    protected boolean hasFileStatusClause() {
        return false;
    }

    public void open(DatasetEnums.FileOpenMode mode) {
        // SQL mode: no file open; just record state + ensure table exists.
        if (isSqlMode()) {
            this.openMode = mode;
            this.isOpen = true;
            this.endOfFile = false;
            this.fileStatus = "00";
            this.sqlBrowseResults = null;
            this.sqlBrowseIndex = 0;
            this.sqlBrowseDescending = false;
            this.lastStartCols = null;
            this.lastStartAnchorVals = null;
            this.lastReadKey = null;
            this.lastRowSeq = null;
            this.lastUpdateKey = null;
            sqlEnsureTable();
            // COBOL OPEN OUTPUT semantically truncates the file — an atomic, self-contained
            // operation. Use a fresh auto-commit connection so the DELETE is independent of
            // any surrounding save-block tx (1) avoids "idle in transaction" holding row
            // locks while the program waits on an interactive screen (HIKARI zombie scenario
            // backported from ship 912c08e), and (2) works regardless of whether the caller
            // has a save-block tx bound — earlier SAVEPOINT-based approach silently skipped
            // the DELETE when no tx was bound. Failure (e.g. table not yet materialised) is
            // logged and ignored; it does NOT poison the surrounding tx because we never
            // touched it.
            if (mode == DatasetEnums.FileOpenMode.OUTPUT) {
                javax.sql.DataSource ds = jdbcTemplate.getDataSource();
                if (ds != null) {
                    try (java.sql.Connection conn = ds.getConnection()) {
                        conn.setAutoCommit(true);
                        try (java.sql.Statement st = conn.createStatement()) {
                            st.executeUpdate("DELETE FROM " + getTableName());
                        }
                    } catch (java.sql.SQLException e) {
                        LOG.warn(
                                "OPEN OUTPUT clear skipped for {}: {}",
                                getTableName(),
                                e.getMessage());
                    }
                }
            }
            sqlInitSeqCounter();
            return;
        }
        try {
            Path path = resolveFilePath();
            switch (mode) {
                case INPUT:
                    in = new BufferedInputStream(Files.newInputStream(path));
                    break;
                case OUTPUT:
                    Files.createDirectories(path.getParent());
                    // Keep the FileChannel so flush() can force(true) — see outChannel docstring.
                    outChannel =
                            java.nio.channels.FileChannel.open(
                                    path,
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.TRUNCATE_EXISTING,
                                    StandardOpenOption.WRITE);
                    out =
                            new BufferedOutputStream(
                                    java.nio.channels.Channels.newOutputStream(outChannel));
                    break;
                case EXTEND:
                    Files.createDirectories(path.getParent());
                    outChannel =
                            java.nio.channels.FileChannel.open(
                                    path,
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.APPEND,
                                    StandardOpenOption.WRITE);
                    out =
                            new BufferedOutputStream(
                                    java.nio.channels.Channels.newOutputStream(outChannel));
                    break;
                case IO:
                    // Single seekable handle — read advances position; REWRITE seeks back
                    // to lastReadPos so it overwrites the correct record (not byte 0).
                    Files.createDirectories(path.getParent());
                    if (!Files.exists(path)) {
                        Files.createFile(path);
                    }
                    raf = new RandomAccessFile(path.toFile(), "rw");
                    lastReadPos = -1L;
                    // INDEXED + file mode: scan + populate primary/alt-key indexes for ISAM ops.
                    if (!isSequentialFile()) {
                        rebuildIndex();
                    }
                    break;
                default:
                    throw new IllegalStateException("Mode not supported: " + mode);
            }
            this.openMode = mode;
            this.isOpen = true;
            this.endOfFile = false;
            this.fileStatus = "00";
        } catch (IOException e) {
            // Original Hitachi ACOS-77 JCL would abend before PROCEDURE DIVISION when an
            // INPUT file is missing. The Java port (env-var binding) introduced a runtime
            // path that doesn't exist in COBOL, so swallowing here let read() spin on an
            // unopened file and OUTPUT.write() fill disk. Propagate so the service's
            // catch(Exception) sets completionCode=12. endOfFile=true on INPUT is belt-
            // and-suspenders for callers that catch the throw and inspect state instead.
            this.isOpen = false;
            this.fileStatus = (e instanceof java.nio.file.NoSuchFileException) ? "35" : "30";
            if (mode == DatasetEnums.FileOpenMode.INPUT) {
                this.endOfFile = true;
            }
            // A COBOL file declared with FILE STATUS does NOT abend on an unsuccessful
            // OPEN: the status is handed to the program, which decides what to do.
            // Throwing made that contract unreachable -- e.g. `OPEN INPUT f` on a missing
            // file, `IF FSTS = "35" OR "30"` -> `OPEN OUTPUT / CLOSE / OPEN INPUT` to
            // create it, a first-run pattern this port turned into COMPLETION-CODE 12.
            // Without a FILE STATUS clause COBOL has no way to report the failure, so the
            // abend is the faithful behaviour and the throw stays.
            // The original worry -- a swallowed failure letting read() spin or OUTPUT
            // writes run -- is covered where it belongs: read() and write() already
            // return status 47 / 48 when !isOpen.
            if (!hasFileStatusClause()) {
                throw new IllegalStateException(
                        "Failed to open "
                                + getFileName()
                                + " in "
                                + mode
                                + " mode: "
                                + e.getMessage(),
                        e);
            }
        }
    }

    /**
     * Read one record. SQL mode: route through sqlRead(). Binary mode: read exactly recordLength
     * bytes. Text mode: read until newline.
     */
    public boolean read() {
        invalidKey = false;
        if (isSqlMode()) {
            return sqlRead();
        }
        if (!isOpen || (in == null && raf == null)) {
            fileStatus = "47";
            return false;
        }
        try {
            int len = recordLength();
            byte[] tmp = new byte[len];
            int read;
            if (isBinary()) {
                // Eat any leading CR/LF that text-formatted fixtures leave between records.
                // Pure binary records never start with 0x0A/0x0D (those values would belong
                // inside a printable field, not at byte 0 right after the previous record's
                // last byte). Without this, the 2nd sequential read in a multi-record scan
                // starts on the '\n' from record 1, shifting all subsequent field offsets
                // by 1 byte — e.g. HMCURI's MCU-HZAIKO-FLG at offset 115 reads MCU-HHNTEN-FLG
                // instead, breaking the procZ000 skip check in HDB040_C.
                skipBinaryRecordSeparators();
                if (raf != null) {
                    // IO mode: read via RandomAccessFile, capture pos for REWRITE.
                    lastReadPos = raf.getFilePointer();
                    int got = 0;
                    while (got < len) {
                        int n = raf.read(tmp, got, len - got);
                        if (n < 0) {
                            break;
                        }
                        got += n;
                    }
                    if (got <= 0) {
                        endOfFile = true;
                        fileStatus = "10";
                        return false;
                    }
                    if (got < len) {
                        java.util.Arrays.fill(tmp, got, len, (byte) 0x20);
                    }
                } else {
                    read = readFully(in, tmp);
                    if (read <= 0) {
                        endOfFile = true;
                        fileStatus = "10";
                        return false;
                    }
                    if (read < len) {
                        java.util.Arrays.fill(tmp, read, len, (byte) 0x20);
                    }
                }
            } else {
                // Text mode (LINE SEQUENTIAL): only InputStream path supported.
                if (in == null) {
                    fileStatus = "47";
                    return false;
                }
                int b;
                int idx = 0;
                while ((b = in.read()) != -1) {
                    if (b == '\n') {
                        break;
                    }
                    if (b == '\r') {
                        continue;
                    }
                    if (idx < len) {
                        tmp[idx++] = (byte) b;
                    }
                }
                if (b == -1 && idx == 0) {
                    endOfFile = true;
                    fileStatus = "10";
                    return false;
                }
                if (idx < len) {
                    java.util.Arrays.fill(tmp, idx, len, (byte) 0x20);
                }
            }
            buffer().setBytes(tmp);
            fileStatus = "00";
            return true;
        } catch (IOException e) {
            fileStatus = "30";
            return false;
        }
    }

    public boolean readNext() {
        invalidKey = false;
        if (isSqlMode()) {
            return sqlReadNext();
        }
        // File-mode INDEXED: prefer cursor walk (set by fileStart/fileReadByKey on alt-key);
        // fall back to sequential read() when no cursor is active (raw READ NEXT after open).
        if (isFileModeIndexed() && (browseCursor != null || altCursorList != null)) {
            return fileReadNext();
        }
        return read();
    }

    public boolean readByKey(Object key) {
        invalidKey = false;
        if (isSqlMode()) {
            return sqlReadByExplicitKey(key);
        }
        if (isFileModeIndexed()) {
            return fileReadByKey(null, key);
        }
        return read();
    }

    /**
     * READ FILE by ALTERNATE KEY field — COBOL `READ FILE KEY altKey` semantic. The XML AST
     * captures the COBOL `READ ... KEY X` clause as the `keyField` attribute on the READ statement;
     * FileIoHandler routes those reads here so the WHERE clause queries by the specified column
     * instead of the primary RECORD KEY. See {@link #sqlReadByAlternateKey(String, Object)}.
     */
    public boolean readByKey(String altKeyField, Object key) {
        invalidKey = false;
        if (isSqlMode()) {
            return sqlReadByAlternateKey(altKeyField, key);
        }
        if (isFileModeIndexed()) {
            return fileReadByKey(altKeyField, key);
        }
        return read();
    }

    /**
     * COBOL {@code READ ... PRIOR} — reverse sequential read from the current browse position.
     * SQL-mode delegates to {@link #sqlReadPrev()}, which rebuilds a descending page anchored at
     * the position START set on a direction change. File-mode reverse browse is not implemented (no
     * file-mode caller needs it); it falls back to {@link #read()} like the other file-mode paths
     * so callers behave consistently instead of hitting an exception.
     */
    public boolean readPrev() {
        invalidKey = false;
        if (isSqlMode()) {
            return sqlReadPrev();
        }
        return read();
    }

    public boolean start() {
        invalidKey = false;
        if (isSqlMode()) {
            return sqlReadNext();
        }
        return read();
    }

    public boolean start(String key, String operator) {
        invalidKey = false;
        if (isSqlMode()) {
            return sqlStart(key, operator);
        }
        if (isFileModeIndexed()) {
            return fileStart(key, operator);
        }
        return read();
    }

    /**
     * COBOL {@code START ... LAST KEY <key>} — position the browse cursor at the LAST record in the
     * named key's order (ignores buffer key values; used before a backward READ PRIOR walk).
     * SQL-mode delegates to {@link #sqlStartLast(String)}. File-mode is not implemented (no
     * file-mode caller needs it); it falls back to {@link #read()} like the other file-mode paths
     * so callers behave consistently instead of hitting an exception.
     */
    public boolean startLast(String key) {
        invalidKey = false;
        if (isSqlMode()) {
            return sqlStartLast(key);
        }
        return read();
    }

    /* ── File-mode ISAM I/O ─────────────────────────────────────────────────
     *  Active when raf != null && !isSequentialFile() — i.e. INDEXED file declared
     *  with -MSD ASSIGN (scratch files; SQL backing intentionally skipped). The
     *  generator's JavaFileSetGenerator.initSqlMode() omits setJdbcTemplate for
     *  MSD-suffix files, so isSqlMode() returns false and these methods fire.
     *
     *  Design:
     *  - Primary key index built on open() via {@link #rebuildIndex()}.
     *  - Alt-key indexes are multimap (TreeMap&lt;keyStr, List&lt;offset&gt;&gt;) so DUPLICATES
     *    behaves correctly; UNIQUE alt-keys are a degenerate case (single-element list).
     *  - DELETE marks the offset's slot in {@link #deletedOffsets} bitset + zero-fills
     *    the on-disk record (defence-in-depth) + removes from indexes.
     *  - FILE STATUS values match COBOL/NEC ISAM: 00 success, 02 dup warn, 10 EOF,
     *    22 dup key fail, 23 invalid key, 30 IO error, 35 file not found, 43 no prior
     *    READ, 47 wrong open mode for read, 48 wrong mode for write.
     */
    protected boolean isFileModeIndexed() {
        return !isSqlMode() && raf != null && !isSequentialFile();
    }

    /**
     * Subclass override (emitted by JavaFdFileGenerator when alt-keys present): list of
     * alternate-key names matching {@link FileSymbol#getAlternateKeys()}. Composite alt-keys are
     * space-joined ("F1 F2"). Default empty = no alt keys.
     */
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.Collections.emptyList();
    }

    /**
     * Subclass override (emitted when DUPLICATES flag was set in COBOL): true if the named alt key
     * allows duplicate values (multimap semantic at WRITE).
     */
    public boolean isAlternateKeyDuplicates(String altKeyName) {
        return false;
    }

    /**
     * Extract a key value from the CURRENT buffer for any key (primary or alt). Composite keys
     * (space-joined names) return pipe-delimited leaf values.
     */
    public String extractKeyByName(String keyName) {
        if (keyName == null || keyName.isEmpty()) {
            return "";
        }
        String[] parts = keyName.split("\\s+");
        if (parts.length == 1) {
            try {
                return buffer().getString(parts[0]).trim();
            } catch (Exception e) {
                return "";
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append('|');
            }
            try {
                sb.append(buffer().getString(parts[i]).trim());
            } catch (Exception e) {
                /* leave empty segment */
            }
        }
        return sb.toString();
    }

    /**
     * Linear scan to populate primary + alt-key indexes from on-disk records. Detects all-zero
     * records as tombstones (skipped on subsequent reads).
     */
    private void rebuildIndex() {
        if (raf == null) {
            return;
        }
        primaryIndex = new java.util.TreeMap<>();
        altIndexes = new java.util.HashMap<>();
        deletedOffsets = new java.util.BitSet();
        browseCursor = null;
        altCursorKeyName = null;
        altCursorKeyValue = null;
        altCursorIndex = 0;
        altCursorList = null;
        int recLen = recordLength();
        if (recLen <= 0) {
            return;
        }
        java.util.List<String> altNames = getAlternateKeyNames();
        try {
            long fileLen = raf.length();
            byte[] tmp = new byte[recLen];
            for (long off = 0; off + recLen <= fileLen; off += recLen) {
                raf.seek(off);
                int got = 0;
                while (got < recLen) {
                    int n = raf.read(tmp, got, recLen - got);
                    if (n < 0) {
                        break;
                    }
                    got += n;
                }
                if (got < recLen) {
                    break;
                }
                // Zero-fill tombstone detection — file-mode DELETE leaves all-zero bytes.
                boolean zeroFilled = true;
                for (int i = 0; i < recLen; i++) {
                    if (tmp[i] != 0) {
                        zeroFilled = false;
                        break;
                    }
                }
                if (zeroFilled) {
                    deletedOffsets.set((int) (off / recLen));
                    continue;
                }
                System.arraycopy(tmp, 0, buffer().bytes(), 0, recLen);
                String pkStr = extractKeyFromCurrentRecord();
                if (pkStr != null && !pkStr.isEmpty()) {
                    primaryIndex.put(pkStr, off);
                }
                for (String altName : altNames) {
                    String akStr = extractKeyByName(altName);
                    if (akStr == null || akStr.isEmpty()) {
                        continue;
                    }
                    altIndexes
                            .computeIfAbsent(altName, k -> new java.util.TreeMap<>())
                            .computeIfAbsent(akStr, k -> new java.util.ArrayList<>())
                            .add(off);
                }
            }
            // Restore raf position to end-of-file so subsequent appends work correctly.
            raf.seek(fileLen);
        } catch (IOException e) {
            // best-effort; partial index acceptable, subsequent ops will set FSTS=30 on real
            // failures
        }
    }

    /**
     * Read the record at the given byte offset into buffer. Returns false on EOF/IO
     * error/tombstone.
     */
    private boolean readAtOffset(long off) {
        if (raf == null) {
            fileStatus = "47";
            return false;
        }
        int recLen = recordLength();
        if (deletedOffsets != null && deletedOffsets.get((int) (off / recLen))) {
            fileStatus = "23";
            invalidKey = true;
            return false;
        }
        try {
            raf.seek(off);
            byte[] tmp = new byte[recLen];
            int got = 0;
            while (got < recLen) {
                int n = raf.read(tmp, got, recLen - got);
                if (n < 0) {
                    break;
                }
                got += n;
            }
            if (got < recLen) {
                fileStatus = "10";
                endOfFile = true;
                return false;
            }
            System.arraycopy(tmp, 0, buffer().bytes(), 0, recLen);
            lastReadPos = off;
            fileStatus = "00";
            return true;
        } catch (IOException e) {
            fileStatus = "30";
            return false;
        }
    }

    /**
     * File-mode READ KEY IS X — primary key (altKeyName=null) or alternate key. Alt-key path sets
     * DUPLICATES cursor state so subsequent readNext() walks through records sharing the alt-key
     * value before advancing to higher key values.
     */
    private boolean fileReadByKey(String altKeyName, Object keyValue) {
        if (!isFileModeIndexed()) {
            fileStatus = "47";
            return false;
        }
        if (primaryIndex == null) {
            rebuildIndex();
        }
        String key = keyValue == null ? "" : keyValue.toString();
        String pk = getRecordKey();
        boolean isPrimary = (altKeyName == null) || altKeyName.equalsIgnoreCase(pk);
        if (isPrimary) {
            Long off = primaryIndex.get(key);
            if (off == null) {
                fileStatus = "23";
                invalidKey = true;
                return false;
            }
            altCursorKeyName = null;
            altCursorList = null; // primary read clears alt cursor
            return readAtOffset(off);
        }
        java.util.TreeMap<String, java.util.List<Long>> idx = altIndexes.get(altKeyName);
        if (idx == null) {
            fileStatus = "23";
            invalidKey = true;
            return false;
        }
        java.util.List<Long> list = idx.get(key);
        if (list == null || list.isEmpty()) {
            fileStatus = "23";
            invalidKey = true;
            return false;
        }
        altCursorKeyName = altKeyName;
        altCursorKeyValue = key;
        altCursorList = list;
        altCursorIndex = 0;
        return readAtOffset(list.get(0));
    }

    /**
     * File-mode START — primary key only for now. Sets {@link #browseCursor} so subsequent {@link
     * #readNext()} walks records in key order from the start position. Operator semantics: EQ,
     * GE/NOT LESS THAN, GT/GREATER (LE/LT not yet supported).
     */
    private boolean fileStart(String keyValue, String operator) {
        if (!isFileModeIndexed()) {
            fileStatus = "47";
            return false;
        }
        if (primaryIndex == null) {
            rebuildIndex();
        }
        String key = keyValue == null ? "" : keyValue;
        String op = operator == null ? "" : operator.trim().toUpperCase();
        java.util.SortedMap<String, Long> sub;
        switch (op) {
            case "EQ":
            case "EQUAL":
            case "=":
                if (!primaryIndex.containsKey(key)) {
                    fileStatus = "23";
                    invalidKey = true;
                    return false;
                }
                sub = primaryIndex.tailMap(key);
                break;
            case "GE":
            case "GREATER OR EQUAL":
            case "NOT LESS THAN":
            case ">=":
                sub = primaryIndex.tailMap(key);
                break;
            case "GT":
            case "GREATER":
            case ">":
                String higher = primaryIndex.higherKey(key);
                if (higher == null) {
                    fileStatus = "23";
                    invalidKey = true;
                    return false;
                }
                sub = primaryIndex.tailMap(higher);
                break;
            default:
                sub = primaryIndex.tailMap(key);
        }
        if (sub.isEmpty()) {
            fileStatus = "23";
            invalidKey = true;
            return false;
        }
        browseCursor = sub.entrySet().iterator();
        altCursorKeyName = null;
        altCursorList = null;
        fileStatus = "00";
        return true;
    }

    /**
     * File-mode READ NEXT after fileStart() or fileReadByKey(altKey, ...). Walks {@link
     * #browseCursor} for primary START path, or advances {@link #altCursorList} for DUPLICATES
     * alt-key sequential read.
     */
    private boolean fileReadNext() {
        if (!isFileModeIndexed()) {
            fileStatus = "47";
            return false;
        }
        // Alt-key DUPLICATES walk: advance within list, then transition to next key value.
        if (altCursorKeyName != null && altCursorList != null) {
            altCursorIndex++;
            if (altCursorIndex < altCursorList.size()) {
                return readAtOffset(altCursorList.get(altCursorIndex));
            }
            java.util.TreeMap<String, java.util.List<Long>> idx = altIndexes.get(altCursorKeyName);
            if (idx == null) {
                fileStatus = "10";
                endOfFile = true;
                return false;
            }
            java.util.Map.Entry<String, java.util.List<Long>> next =
                    idx.higherEntry(altCursorKeyValue);
            if (next == null) {
                fileStatus = "10";
                endOfFile = true;
                return false;
            }
            altCursorKeyValue = next.getKey();
            altCursorList = next.getValue();
            altCursorIndex = 0;
            return readAtOffset(altCursorList.get(0));
        }
        // Primary key browse cursor.
        if (browseCursor != null && browseCursor.hasNext()) {
            java.util.Map.Entry<String, Long> entry = browseCursor.next();
            long off = entry.getValue();
            int recLen = recordLength();
            // Skip deleted records.
            while (deletedOffsets != null
                    && deletedOffsets.get((int) (off / recLen))
                    && browseCursor.hasNext()) {
                entry = browseCursor.next();
                off = entry.getValue();
            }
            if (deletedOffsets != null && deletedOffsets.get((int) (off / recLen))) {
                fileStatus = "10";
                endOfFile = true;
                return false;
            }
            return readAtOffset(off);
        }
        fileStatus = "10";
        endOfFile = true;
        return false;
    }

    /** File-mode DELETE current record (from last READ). Tombstone + zero-fill + index removal. */
    private boolean fileDelete() {
        if (!isFileModeIndexed()) {
            fileStatus = "47";
            return false;
        }
        if (lastReadPos < 0) {
            fileStatus = "43";
            invalidKey = true;
            return false;
        }
        int recLen = recordLength();
        int slot = (int) (lastReadPos / recLen);
        if (deletedOffsets == null) {
            deletedOffsets = new java.util.BitSet();
        }
        deletedOffsets.set(slot);
        final long deletedOff = lastReadPos;
        // Remove from primary index.
        if (primaryIndex != null) {
            primaryIndex.entrySet().removeIf(e -> e.getValue() == deletedOff);
        }
        // Remove from all alt indexes.
        if (altIndexes != null) {
            for (java.util.TreeMap<String, java.util.List<Long>> idx : altIndexes.values()) {
                for (java.util.List<Long> list : idx.values()) {
                    list.removeIf(o -> o == deletedOff);
                }
            }
        }
        // Zero-fill on disk so rebuildIndex on next open re-detects the tombstone.
        try {
            raf.seek(deletedOff);
            raf.write(new byte[recLen]);
        } catch (IOException e) {
            fileStatus = "30";
            return false;
        }
        fileStatus = "00";
        return true;
    }

    /**
     * File-mode WRITE — append at end-of-file, update indexes. Detects PK collision (FSTS=22)
     * before write. Alt-key collision policy: UNIQUE → FSTS=22 fail, DUPLICATES → FSTS=00.
     */
    private boolean fileWriteWithIndex() {
        if (!isFileModeIndexed()) {
            fileStatus = "48";
            return false;
        }
        if (primaryIndex == null) {
            rebuildIndex();
        }
        String pkStr = extractKeyFromCurrentRecord();
        if (pkStr != null && !pkStr.isEmpty() && primaryIndex.containsKey(pkStr)) {
            fileStatus = "22";
            invalidKey = true;
            return false;
        }
        // Check alt-key UNIQUE collisions before write.
        for (String altName : getAlternateKeyNames()) {
            if (isAlternateKeyDuplicates(altName)) {
                continue;
            }
            String akStr = extractKeyByName(altName);
            if (akStr == null || akStr.isEmpty()) {
                continue;
            }
            java.util.TreeMap<String, java.util.List<Long>> idx = altIndexes.get(altName);
            if (idx != null && idx.containsKey(akStr)) {
                fileStatus = "22";
                invalidKey = true;
                return false;
            }
        }
        try {
            long off = raf.length();
            raf.seek(off);
            raf.write(buffer().bytes(), 0, recordLength());
            // Update indexes.
            if (pkStr != null && !pkStr.isEmpty()) {
                primaryIndex.put(pkStr, off);
            }
            for (String altName : getAlternateKeyNames()) {
                String akStr = extractKeyByName(altName);
                if (akStr == null || akStr.isEmpty()) {
                    continue;
                }
                altIndexes
                        .computeIfAbsent(altName, k -> new java.util.TreeMap<>())
                        .computeIfAbsent(akStr, k -> new java.util.ArrayList<>())
                        .add(off);
            }
            fileStatus = "00";
            return true;
        } catch (IOException e) {
            fileStatus = "30";
            return false;
        }
    }

    public void scratch() {
        // SQL mode: clear cursors. File mode: no-op (legacy behavior).
        if (isSqlMode()) {
            selectedRecords.clear();
            selectedIndex = 0;
            selectWhereActive = false;
            sqlBrowseResults = null;
            sqlBrowseIndex = 0;
            endOfFile = false;
            fileStatus = "00";
        }
    }

    public void writeAfterAdvancing(int lines) {
        write();
        if (out != null) {
            try {
                for (int i = 0; i < lines; i++) {
                    out.write(System.lineSeparator().getBytes());
                }
            } catch (java.io.IOException ignore) {
                /* fall through */
            }
        }
    }

    public void writeAfterAdvancingPage() {
        writeAfterAdvancing(1);
    }

    public void writeBeforeAdvancing(int lines) {
        if (out != null) {
            try {
                for (int i = 0; i < lines; i++) {
                    out.write(System.lineSeparator().getBytes());
                }
            } catch (java.io.IOException ignore) {
                /* fall through */
            }
        }
        write();
    }

    /** Vendor ISAM helper — number of records from the most recent {@link #sqlSelectWhere}. */
    public int getSelectedCount() {
        return isSqlMode() ? selectedRecords.size() : 0;
    }

    /** Vendor ISAM SELECT WHERE — delegates to SQL impl in SQL mode, no-op otherwise. */
    public void sqlSelectWhere(
            String whereClause, String orderByCol, boolean ascending, Object... params) {
        if (isSqlMode()) {
            doSqlSelectWhere(whereClause, orderByCol, ascending, params);
        }
    }

    public void write() {
        if (isSqlMode()) {
            sqlWrite();
            return;
        }
        if (!isOpen || (out == null && raf == null)) {
            fileStatus = "48";
            return;
        }
        // INFRA-WRITE-INDEX-001 (ship-v2 back-port). File-mode INDEXED (MSD scratch
        // files): route through fileWriteWithIndex so primary/alt-key indexes built
        // at open() stay coherent for subsequent within-session READ. Without this,
        // the index is empty post-open on a fresh file and stays empty after writes
        // → any within-session readByKey returns FSTS=23 even for keys just written.
        // CSV-INDEXED is not a real pairing (CSV is sequential by convention), so the
        // csv() guard preserves the raw-bytes path for CSV.
        if (isFileModeIndexed() && !layout().csv()) {
            fileWriteWithIndex();
            return;
        }
        try {
            if (layout().csv()) {
                if (raf != null) {
                    raf.write(serializeCsvRecord());
                } else out.write(serializeCsvRecord());
            } else if (raf != null) {
                raf.write(buffer().bytes(), 0, recordLength());
            } else {
                out.write(buffer().bytes(), 0, recordLength());
                if (!isBinary()) {
                    out.write(System.lineSeparator().getBytes(layout().charset()));
                }
            }
            fileStatus = "00";
        } catch (IOException e) {
            fileStatus = "30";
        }
    }

    /**
     * Serialize the current record as a Hitachi WITH-CSV row: each LEAF field becomes one
     * comma-separated cell, terminated by CRLF. Rules match real NEC WITH CSV output (reference:
     * PCEG5240 output HAI02SJ15SW2SJA0015J01EG52401.csv): - Edited numeric (PIC Z..., ---,--9):
     * emit DE-EDITED raw integer. - Plain numeric DISPLAY (PIC 9/S9): raw digit bytes as-is, NOT
     * quoted. - Alphanumeric (X) / NATIONAL (N): ALWAYS quote with "..." and KEEP full field width
     * (no trim); escape internal " as "". (Was: quote-only-if-ambiguous + trim, wrong dialect tuned
     * to input file FMCJ_T0010.csv; see SENKO_FIELD8_ZENKAKU_BUG_VI.md.)
     */
    private byte[] serializeCsvRecord() {
        byte[] data = buffer().bytes();
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (LayoutField f : layout().leaves()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            int off = f.getByteOffset();
            int len = f.getByteLength();
            if (off < 0 || len <= 0 || off + len > data.length) {
                continue;
            }
            if (f.isEdited()) {
                // De-edit: strip filler/commas, recover sign, emit raw signed integer.
                String raw = new String(data, off, len, layout().charset());
                sb.append(deEditNumeric(raw));
            } else if (f.getKind() == LayoutField.Kind.NUM) {
                // Plain numeric DISPLAY: raw digit bytes as-is, NOT quoted (NEC WITH CSV).
                sb.append(new String(data, off, len, layout().charset()));
            } else {
                // Alphanumeric (X) / National (N): NEC WITH CSV ALWAYS quotes and keeps the
                // FULL field width (X pad ASCII space, N pad full-width space - bytes already
                // in buffer, no trim). Escape internal " as "".
                String cell = new String(data, off, len, layout().charset());
                sb.append('"').append(cell.replace("\"", "\"\"")).append('"');
            }
        }
        sb.append("\r\n");
        return sb.toString().getBytes(layout().charset());
    }

    /** De-edit an edited numeric byte slice (skip spaces/commas, recover sign from '-'). */
    private static String deEditNumeric(String s) {
        if (s == null || s.isEmpty()) {
            return "0";
        }
        boolean negative = false;
        StringBuilder digits = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '-') {
                negative = true;
            } else if (c >= '0' && c <= '9') digits.append(c);
        }
        if (digits.length() == 0) {
            return "0";
        }
        // Strip leading zeros (but keep at least one digit).
        int start = 0;
        while (start < digits.length() - 1 && digits.charAt(start) == '0') start++;
        String body = digits.substring(start);
        return negative ? "-" + body : body;
    }

    public void rewrite() {
        if (isSqlMode()) {
            sqlRewrite();
            return;
        }
        // COBOL REWRITE semantics: overwrite the record at the position of the most
        // recent READ. For IO mode we seek the RandomAccessFile back to lastReadPos
        // and write the bytes directly — NOT via public write(), which now routes
        // INDEXED files through fileWriteWithIndex() and would append at end-of-file
        // instead of overwriting in place (INFRA-WRITE-INDEX-001 ship-v2 back-port).
        // For the legacy stream path (no seek), we honor existing best-effort
        // behavior (write at current output position).
        if (raf != null) {
            if (lastReadPos < 0) {
                fileStatus = "43";
                invalidKey = true;
                return;
            }
            try {
                raf.seek(lastReadPos);
                if (layout().csv()) {
                    raf.write(serializeCsvRecord());
                } else {
                    raf.write(buffer().bytes(), 0, recordLength());
                }
                fileStatus = "00";
            } catch (IOException e) {
                fileStatus = "30";
                return;
            }
        } else {
            // Legacy stream path — no seek capability, defer to write().
            write();
        }
        // Phase 2c: COBOL semantic — REWRITE must be preceded by a matching READ.
        // Resetting lastReadPos after a successful rewrite enforces FSTS=43 on a
        // second REWRITE without intervening READ. Without this, the second call
        // silently overwrote at the same position again, masking caller bugs.
        if ("00".equals(fileStatus)) {
            lastReadPos = -1L;
        }
    }

    public void delete() {
        if (isSqlMode()) {
            sqlDelete();
            return;
        }
        if (isFileModeIndexed()) {
            fileDelete();
            return;
        }
        /* sequential file: no-op */
    }

    /** Delete a record by explicit key (SQL mode only). */
    public void deleteByKey(Object key) {
        if (isSqlMode()) {
            sqlDeleteByKey(key);
        }
    }

    @Override
    public void close() {
        if (isSqlMode()) {
            sqlBrowseResults = null;
            sqlBrowseIndex = 0;
            selectedRecords.clear();
            selectedIndex = 0;
            selectWhereActive = false;
            lastReadKey = null;
            lastRowSeq = null;
            lastUpdateKey = null;
            isOpen = false;
            fileStatus = "00";
            return;
        }
        try {
            if (in != null) in.close();
        } catch (IOException ignore) {
            /* best-effort close: ignore */
        }
        try {
            if (out != null) {
                out.flush();
                out.close();
            }
        } catch (IOException ignore) {
            /* best-effort close: ignore */
        }
        try {
            if (raf != null) raf.close();
        } catch (IOException ignore) {
            /* best-effort close: ignore */
        }
        in = null;
        out = null;
        raf = null;
        outChannel = null;
        lastReadPos = -1L;
        // Clear file-mode ISAM index state — rebuilt on next open() if needed.
        primaryIndex = null;
        altIndexes = null;
        deletedOffsets = null;
        browseCursor = null;
        altCursorKeyName = null;
        altCursorKeyValue = null;
        altCursorIndex = 0;
        altCursorList = null;
        isOpen = false;
    }

    /**
     * Flush buffered OUTPUT/EXTEND writes to disk WITHOUT closing the file.
     *
     * <p>{@link #close()} is the ONLY place {@code out} is flushed, and interactive programs hold a
     * print file (PRN-F) open across an ACCEPT screen — CLOSE is reached only at 終了. If the
     * operator abandons the session before that, the BufferedOutputStream is discarded unflushed
     * and the file — truncated to 0 bytes by OPEN OUTPUT — stays empty. Calling {@code flush()} at
     * a per-transaction boundary (see AbstractFileSet.commit) makes each printed block durable
     * immediately. No-op in SQL mode and when no output stream is open.
     */
    public void flush() {
        if (isSqlMode()) {
            return;
        }
        if (out != null) {
            try {
                out.flush();
                // force(true) durably syncs data + size metadata so the OS updates the
                // directory-entry size immediately. Without it Windows leaves the visible size
                // stale (Explorer shows 0 bytes) until CLOSE, and a Docker bind-mount to the
                // Windows host wouldn't reflect the write either. Small print files → cheap.
                if (outChannel != null && outChannel.isOpen()) {
                    outChannel.force(true);
                }
            } catch (IOException ignore) {
                /* fall through */
            }
        }
    }

    /* ── Helpers ───────────────────────────────────────────────────── */

    /**
     * Explicit path injected by the service (set from WS variable corresponding to the ASSIGN slot
     * — COBOL semantics: `ASSIGN TO DK-RS1` reads the file path from WS variable `RS1`, which the
     * program populated in its INIT paragraph via `ACCEPT … FROM ENVIRONMENT-VALUE`).
     */
    private String injectedPath;

    // Trim because COBOL WS variables are fixed-width (PIC X(256)) → trailing spaces.
    public void setPath(String path) {
        this.injectedPath = path == null ? null : path.trim();
    }

    protected Path resolveFilePath() {
        // Preferred: service has already resolved the path from the WS variable that COBOL
        // associates with this file's ASSIGN clause (DK-<slot> ↔ WS <slot>).
        if (injectedPath != null && !injectedPath.isEmpty()) {
            return Paths.get(injectedPath);
        }
        // MSD scratch files: route through the per-session scratch dir bound by
        // WebSocketScreenHandler at session connect time (Utility.setScratchDir).
        // This isolates two concurrent HDR140 sessions onto distinct paths so they
        // can never collide on /tmp/HDR140/TM1F.dat. Falls through to env-var
        // lookup when running outside a WS session (batch/console/test).
        String assign = getAssignTo();
        if (assign != null && assign.toUpperCase().endsWith("-MSD")) {
            try {
                Path sessionDir = Utility.getScratchDir();
                if (sessionDir != null) {
                    return sessionDir.resolve(getFileName() + ".dat");
                }
            } catch (Throwable ignored) {
                /* Utility may be absent in some test classpaths */
            }
        }
        // Fallback (legacy code paths that don't yet inject path): direct env var lookup.
        String dd = getAssignTo();
        if (dd != null && !dd.isEmpty()) {
            String env = System.getenv(dd);
            if (env == null) {
                env = System.getenv(dd.replace('-', '_'));
            }
            if (env == null) {
                env = System.getenv(dd.toUpperCase().replace('-', '_'));
            }
            if (env == null) {
                String slot = stripDdPrefix(dd);
                if (slot != null) {
                    env = System.getenv("COB_" + slot);
                    if (env == null) {
                        env = System.getenv(slot);
                    }
                }
            }
            if (env != null && !env.isEmpty()) {
                return Paths.get(env);
            }
        }
        return Paths.get("input", getFileName() + ".dat");
    }

    /** Strip Hitachi COBOL DD-name prefix (`DK-`/`DK_`) leaving the slot id (e.g. `RS1`, `WS1`). */
    private static String stripDdPrefix(String dd) {
        String upper = dd.toUpperCase();
        if (upper.startsWith("DK-") || upper.startsWith("DK_")) {
            return upper.substring(3);
        }
        return null;
    }

    private static int readFully(InputStream is, byte[] buf) throws IOException {
        int total = 0;
        while (total < buf.length) {
            int n = is.read(buf, total, buf.length - total);
            if (n < 0) {
                return total == 0 ? -1 : total;
            }
            total += n;
        }
        return total;
    }

    /**
     * Skip leading CR/LF bytes from the current read position so binary fixed-length sequential
     * reads stay aligned when the underlying file uses newline-terminated records (the format
     * Python test-fixture generators produce). Pure binary records never begin with 0x0A/0x0D —
     * those bytes can only appear inside a printable field, never at byte 0 right after the
     * previous record's last byte — so this is safe for both formats.
     */
    private void skipBinaryRecordSeparators() throws IOException {
        if (raf != null) {
            while (true) {
                long pos = raf.getFilePointer();
                int b = raf.read();
                if (b < 0) {
                    return;
                }
                if (b != '\n' && b != '\r') {
                    raf.seek(pos); // un-consume the non-separator byte
                    return;
                }
            }
        } else if (in != null) {
            if (!in.markSupported()) {
                return;
            }
            while (true) {
                in.mark(1);
                int b = in.read();
                if (b < 0) {
                    return;
                }
                if (b != '\n' && b != '\r') {
                    in.reset();
                    return;
                }
            }
        }
    }
}

package com.sakura.runtime.record;

import com.sakura.runtime.DatasetEnums;
import com.sakura.runtime.DbDialect;
import com.sakura.runtime.Utility;

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
 * Base of every generated dataset wrapper: I/O operates directly on the record bytes through {@link
 * RecordImage} — no intermediate POJO layer.
 *
 * <p>Each subclass supplies:
 *
 * <ul>
 *   <li>{@link #getFileName()} — the COBOL file name
 *   <li>{@link #getAssignTo()} — the ASSIGN TO name
 *   <li>{@link #layoutResourcePath()} — classpath location of the record schema XML
 *   <li>{@link #isBinary()} — overridden to false for LINE SEQUENTIAL text files
 * </ul>
 */
public abstract class RawDatasetBase implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(RawDatasetBase.class);

    private static final ConcurrentMap<String, RecordSchema> LAYOUT_CACHE =
            new ConcurrentHashMap<>();

    /** Aliases matching the FileOpenMode.INPUT/OUTPUT names the service code emits. */
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
     * Channel beneath the OUTPUT/EXTEND stream. Kept so {@link #flush()} can {@code force(true)}
     * the buffered writes onto disk — Windows otherwise keeps the directory-entry size stale (a
     * 0-byte file in Explorer) until CLOSE releases the handle.
     */
    private java.nio.channels.FileChannel outChannel;

    /**
     * IO mode runs on one seekable handle so REWRITE lands at the position of the most recent READ,
     * as COBOL requires. Two separate buffered streams would drift apart — the read side advances
     * while the write side sits at byte 0, and REWRITE would then overwrite the FIRST record
     * instead of the last one read.
     */
    private RandomAccessFile raf;

    /** Byte offset of the most recent READ in binary mode; -1 when unset. */
    private long lastReadPos = -1L;

    /* ── ISAM index state, file mode ─────────────────────────────────────
     *  In play only for INDEXED files on the byte/file path (raf != null and a
     *  record key exists). Rebuilt by {@link #rebuildIndex()} on every
     *  {@link #open(DatasetEnums.FileOpenMode)} and dropped at {@link #close()}.
     *  Lives in memory only — no index file beside the data. The files involved
     *  are small, so the O(n) scan at open costs well under a millisecond and
     *  there is no stale index to keep coherent.
     */
    /** Byte offset per primary key (composite keys join their leaf names with spaces). */
    private java.util.TreeMap<String, Long> primaryIndex;

    /** Alternate-key indexes: key name → (value → offsets); the list carries DUPLICATES. */
    private java.util.Map<String, java.util.TreeMap<String, java.util.List<Long>>> altIndexes;

    /**
     * Deletion tombstones, one bit per record slot (offset / recordLength), set by file-mode
     * DELETE.
     */
    private java.util.BitSet deletedOffsets;

    /**
     * Cursor over the primary (or flattened alternate) index, driving {@link #readNext()} after
     * {@link #fileStart}.
     */
    private java.util.Iterator<java.util.Map.Entry<String, Long>> browseCursor;

    /**
     * DUPLICATES cursor: after {@link #fileReadByKey(String, Object)} on an alternate key, the
     * following readNext() calls walk that key's duplicate list.
     */
    private String altCursorKeyName;

    private String altCursorKeyValue;
    private int altCursorIndex;
    private java.util.List<Long> altCursorList;

    private RecordImage buffer;
    private RecordSchema layout;

    /* ── SQL backend state — engaged only when a JdbcTemplate arrives ─────────────── */

    /**
     * Injected under cobol.file.sql=true; while null, SQL mode is off and every operation takes the
     * byte/file path.
     */
    private JdbcTemplate jdbcTemplate;

    /** Mirrors (jdbcTemplate != null), cached for the frequent checks. */
    private boolean sqlMode = false;

    /** Cached result-set page backing SEQUENTIAL READ NEXT and START + READ NEXT. */
    private List<Map<String, Object>> sqlBrowseResults;

    private int sqlBrowseIndex = 0;

    /**
     * Ordering of the active {@link #sqlBrowseResults} page: ascending for a forward cursor ({@link
     * #sqlStart}/READ NEXT), descending for a reverse one ({@link #sqlReadPrev}). COBOL permits
     * READ NEXT and READ PRIOR after the same START, so switching direction rebuilds the page
     * anchored at the current key — walking the existing page backwards returned records out of
     * order and could surface spurious status-23 aborts.
     */
    private boolean sqlBrowseDescending = false;

    /**
     * Anchor for a {@link #sqlReadPrev} issued after a {@link #sqlStart}.
     *
     * <p>COBOL READ PRIOR reads-then-moves: {@code START KEY NOT < K} positions the cursor AT the
     * first record {@code >= K}, the first READ PRIOR returns exactly that record, and each further
     * READ PRIOR steps one record back.
     *
     * <p>{@code lastStartCols} holds the key columns START positioned on; {@code
     * lastStartAnchorVals} the key values of the record the cursor sits at (the first row {@code >=
     * K}, captured off the ascending page START built). {@link #sqlReadPrev} then pages descending
     * {@code WHERE tuple <= anchor}, so the first PRIOR yields the positioned record itself.
     */
    private List<String> lastStartCols;

    private Object[] lastStartAnchorVals;

    /** Key of the last successful read, consumed by REWRITE/DELETE; composite keys pipe-join. */
    private Object lastReadKey;

    /** row_seq of the last row read — pinpoints REWRITE/DELETE without leaning on the key. */
    private Long lastRowSeq;

    /** Key captured by READ FOR UPDATE; outranks lastReadKey in the REWRITE that follows. */
    private Object lastUpdateKey;

    /** Running counter assigning the synthetic row_seq of SEQUENTIAL tables. */
    private long sqlSeqCounter = 0;

    /** Rows returned by the vendor ISAM SELECT WHERE — kept apart from the browse cursor. */
    private List<Map<String, Object>> selectedRecords = new ArrayList<>();

    private int selectedIndex = 0;

    /**
     * Set once {@link #doSqlSelectWhere} has run, rows or no rows, exception or not. {@link
     * #sqlReadNext} consults it so an empty selectedRecords AFTER a SELECT WHERE means EOF — not a
     * fallback keyed read on a stale lastReadKey, which loops forever on the same row when the
     * SELECT WHERE failed silently. Cleared by {@link #scratch}, {@link #close} and the next {@link
     * #doSqlSelectWhere}.
     */
    private boolean selectWhereActive = false;

    /**
     * Column allow-list per table, filled lazily from INFORMATION_SCHEMA; screens out layout fields
     * the DB schema does not carry (REDEFINES alternates).
     */
    private static final Map<String, Set<String>> SCHEMA_COLUMNS_CACHE = new ConcurrentHashMap<>();

    /**
     * GENERATED ALWAYS identity columns per table — excluded from INSERT/UPDATE, which PostgreSQL
     * would reject with SQLSTATE 428C9.
     */
    private static final Map<String, Set<String>> IDENTITY_ALWAYS_CACHE = new ConcurrentHashMap<>();

    /**
     * Tables already confirmed present (or created); static so every instance of a file shares it.
     */
    private static final Set<String> ENSURED_TABLES = Collections.synchronizedSet(new HashSet<>());

    /** snake_case column back to its COBOL field; built lazily per instance. */
    private Map<String, SchemaField> snakeToField;

    private static final int BROWSE_PAGE_SIZE = 5000;

    /* ── Contract for generated subclasses ───────────────────────────── */

    public abstract String getFileName();

    public abstract String getAssignTo();

    protected abstract String layoutResourcePath();

    /**
     * COBOL sequential files default to fixed-length binary — no record separator. LINE SEQUENTIAL
     * files (newline-terminated text) override this to false; the choice is made when the subclass
     * is generated, not by any runtime flag.
     */
    protected boolean isBinary() {
        return true;
    }

    /* ── Schema and record buffer ────────────────────────────────────── */

    public RecordSchema layout() {
        if (layout == null) {
            String path = layoutResourcePath();
            layout = LAYOUT_CACHE.computeIfAbsent(path, SchemaLoader::loadFile);
        }
        return layout;
    }

    public RecordImage buffer() {
        if (buffer == null) {
            buffer = new RecordImage(layout());
        }
        return buffer;
    }

    /**
     * Clears the record buffer. The no-argument form is the real API; the {@code setRecord(Object)}
     * shim exists for source compatibility and rejects any non-null argument.
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

    public RecordImage getRecord() {
        return buffer();
    }

    /**
     * Switches this file onto the SQL backend by handing it a JdbcTemplate (arriving only under
     * {@code cobol.file.sql=true}). From then on every operation — read/write/rewrite/delete/start
     * — travels the SQL path rather than the byte/file path; null switches back to binary.
     */
    public void setJdbcTemplate(JdbcTemplate jdbc) {
        this.jdbcTemplate = jdbc;
        this.sqlMode = (jdbc != null);
    }

    /** Whether this file currently runs on the SQL backend. */
    protected boolean isSqlMode() {
        return sqlMode && jdbcTemplate != null;
    }

    /**
     * Table name derived from the COBOL file name — lowercase, hyphens to underscores. Subclasses
     * override for a different mapping.
     */
    public String getTableName() {
        String fn = getFileName();
        return fn == null ? null : fn.toLowerCase().replace("-", "_");
    }

    /**
     * The RECORD KEY clause, space-separated for composite keys. By default the schema is searched
     * for a group whose name ends in {@code -KEY} and the names of its leaves are joined;
     * subclasses override for explicit control.
     */
    public String getRecordKey() {
        List<String> cols = autoDetectKeyFields();
        return cols.isEmpty() ? null : String.join(" ", cols);
    }

    /**
     * Whether the file is SEQUENTIAL, i.e. has no record key. Decided through {@link
     * #getRecordKey()}, so a subclass override outranks the schema auto-detection. SEQUENTIAL
     * tables receive a synthetic {@code row_seq} on INSERT; INDEXED tables let the database assign
     * it via {@code nextval()}.
     */
    protected boolean isSequentialFile() {
        String rk = getRecordKey();
        return rk == null || rk.trim().isEmpty();
    }

    /**
     * {@code ORGANIZATION RELATIVE}: the record's position IS the address — an external relative
     * record number held in the WORKING-STORAGE item named by the {@code RELATIVE KEY} clause,
     * never a value inside the record. That is a different model from INDEXED ({@link
     * #isFileModeIndexed()}), which looks up a key embedded in the buffer. Defaults to false;
     * RELATIVE files get a generated override so {@link #readByKey(Object)} seeks by record number
     * instead of degrading to a plain sequential {@link #read()} — which would report status 10
     * forever once the cursor hit end of file.
     */
    protected boolean isRelativeOrganization() {
        return false;
    }

    /**
     * {@code READ file INVALID KEY} on a RELATIVE file: seeks straight to the byte offset of the
     * 1-based {@code relativeRecordNumber} and reads there, advancing no cursor. A number with
     * nothing written at it (beyond end of file, or &lt;= 0) is no I/O error — COBOL calls it
     * {@code INVALID KEY} (status "23"), which callers already treat as an ordinary not-found next
     * to "00".
     */
    public boolean readByRelativeKey(long relativeRecordNumber) {
        invalidKey = false;
        if (!isOpen || raf == null) {
            fileStatus = "47";
            return false;
        }
        try {
            int len = recordLength();
            long offset = (relativeRecordNumber - 1) * len;
            if (relativeRecordNumber <= 0 || offset + len > raf.length()) {
                invalidKey = true;
                fileStatus = "23";
                return false;
            }
            raf.seek(offset);
            byte[] tmp = new byte[len];
            int got = 0;
            while (got < len) {
                int n = raf.read(tmp, got, len - got);
                if (n < 0) {
                    break;
                }
                got += n;
            }
            if (got < len) {
                invalidKey = true;
                fileStatus = "23";
                return false;
            }
            lastReadPos = offset;
            buffer().setBytes(tmp);
            fileStatus = "00";
            return true;
        } catch (IOException e) {
            fileStatus = "30";
            return false;
        }
    }

    /**
     * Detects the RECORD KEY fields from the record schema, trying in order: a group whose name
     * ends in {@code -KEY} (its leaves become the key); a single leaf ending in {@code -KEY};
     * several sibling leaves ending in {@code -KEY}, forming a composite.
     */
    private List<String> autoDetectKeyFields() {
        List<String> result = new ArrayList<>();
        SchemaGroup root = layout().root();
        if (root == null) {
            return result;
        }
        // first: a -KEY group
        collectKeyFields(root, result);
        if (!result.isEmpty()) {
            return result;
        }
        // second: the first -KEY leaf. REDEFINES alternates must not contribute
        // (their columns are absent from the DB schema), yet isRedefines() only
        // marks the field's own attribute — members of a REDEFINES group answer
        // false — so it cannot filter here. Taking the FIRST match works because
        // leaves come in document order: the primary record's -KEY precedes any
        // alternate's.
        for (SchemaField f : layout().leaves()) {
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
                if (g.isRedefines()) continue; // REDEFINES alternates never supply the key
                String n = g.getName();
                if (n != null && n.toUpperCase().endsWith("-KEY")) {
                    // every leaf under the key group, in order, becomes part of the key
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
            if (child instanceof SchemaField f) {
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
     * The record key as individual snake_case column names. Every column passes through {@link
     * #dbColumn}, which applies the ColumnMappings translation whenever the target DB schema
     * departs from the generated default.
     */
    public List<String> getKeyColumns() {
        return resolveKeyColumns(getRecordKey());
    }

    /**
     * Turns a COBOL START key field list into DB column names.
     *
     * <p>{@code keySpec} is the space-separated field list {@code START ... KEY} positions on — the
     * primary RECORD KEY <em>or</em> an ALTERNATE key, exactly as the generated code hands it to
     * {@link #start(String, String)} / {@link #startLast(String)}. A blank spec falls back to the
     * primary {@link #getRecordKey()}. {@link #sqlStartLast} relies on this so a START on an
     * alternate key positions on the right columns instead of quietly reverting to the primary.
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
                // A skipped key column would silently lose its WHERE predicate; keep
                // the raw name so the statement fails loudly at execution instead.
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
     * Whether {@code keySpec} names real record FIELDS (space-separated), letting {@link
     * #sqlStartLast} position on them directly. Separates an alternate-key field list — every token
     * a leaf — from a bare key-name alias out of a {@code RECORD KEY IS ...} clause: the alias is
     * no column, so it must defer to the primary key. Probes {@link #snakeToField()} under the SAME
     * post-ColumnMappings translation its keys were built with.
     */
    boolean isKeyFieldList(String keySpec) {
        if (keySpec == null || keySpec.trim().isEmpty()) {
            return false;
        }
        Map<String, SchemaField> fields = snakeToField();
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
     * Maps a COBOL-derived snake_case column onto the target DB column. Identity unless the
     * ColumnMappings feature was generated ({@code --column-mapping}); with it, the call bridges
     * the generated names onto the customer schema. Every SQL construction site funnels through
     * here.
     */
    protected final String dbColumn(String snakeCol) {
        return snakeCol;
    }

    /**
     * Whether {@link #dbColumn} resolved the column to the SKIP sentinel — left out of SQL
     * entirely.
     */
    protected final boolean isSkippedColumn(String mappedCol) {
        return false;
    }

    /** The leading key column — for ORDER BY and range queries that use the lead key only. */
    public String getKeyColumnName() {
        List<String> cols = getKeyColumns();
        return cols.isEmpty() ? null : cols.get(0);
    }

    /**
     * Composite-key WHERE clause, typed per column: numeric leaves compare as {@code CAST(col AS
     * BIGINT)=CAST(? AS BIGINT)} so a buffer-formatted {@code "001"} equals the database's {@code
     * 1}; string leaves stay VARCHAR. Without the split, a leading-zero numeric key (PIC 9(3)
     * reading "001") never equals {@code CAST(int_col AS VARCHAR)='1'} — every keyed READ came back
     * empty and the program saw {@code invalidKey}.
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
     * SQL dialect (Postgres/Oracle/H2), detected once from the live connection. Everything
     * vendor-specific — system catalogs, CREATE TABLE, CAST types — goes through it.
     */
    private DbDialect dialect() {
        return jdbcTemplate == null ? DbDialect.POSTGRES : DbDialect.of(jdbcTemplate);
    }

    /**
     * Whether the primary-record field behind this snake_case column carries a numeric javaType —
     * the input {@link #buildKeyWhere()} uses to pick the cast. An unlocatable column answers
     * false, i.e. VARCHAR.
     */
    private boolean isNumericKeyColumn(String snakeCol) {
        if (snakeCol == null) {
            return false;
        }
        for (SchemaField f : layout().leaves()) {
            if (f.isRedefines()) {
                continue;
            }
            // snakeCol arrives ALREADY ColumnMappings-translated (getKeyColumns →
            // dbColumn), so the schema field's name must undergo the same
            // translation before comparing. Otherwise a renamed column never
            // matches any field, the answer degrades to VARCHAR, and a
            // leading-zero numeric buffer value ("001") misses the database's int
            // column — a false invalidKey on data that exists.
            if (!snakeCol.equals(dbColumn(toSnake(f.getName())))) {
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

    /** Splits a pipe-joined composite key into its values; a single key yields one. */
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

    /** Remembers the row_seq of a fetched row so REWRITE/DELETE can target it precisely. */
    private void captureRowSeq(Map<String, Object> row) {
        Object seq = row != null ? row.get("row_seq") : null;
        lastRowSeq = (seq instanceof Number num) ? num.longValue() : null;
    }

    /** ORDER BY of browse queries — all key columns, in key order. */
    private String keyOrderBy() {
        List<String> cols = getKeyColumns();
        return cols.isEmpty() ? "row_seq" : String.join(", ", cols);
    }

    /**
     * Descending ORDER BY that marks EVERY column {@code DESC} — the classic trap being {@code "a,
     * b, c DESC"}, which sorts a and b ascending. Reverse browse (READ PRIOR / START LAST) needs
     * the full key descending.
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
     * Whether a browse anchor key is missing or all zeros. The bare {@link #sqlReadPrev} (one with
     * no preceding START) uses this to choose between an anchored reverse scan and a whole-table
     * descending page. {@link #extractKeyFromCurrentRecord()} joins composite segments with {@code
     * |}, so an all-zero composite reads {@code "0|0|0"} — every segment empty or all-zeros counts
     * as "no key".
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

    /** COBOL START operator → SQL comparison. */
    private String mapStartOp(String op) {
        if (op == null) {
            return ">=";
        }
        switch (op.toUpperCase().trim()) {
            case "EQUAL", "=":
                return ">="; // ISAM EQUAL positions the cursor; READ NEXT browses on
            case "GREATER THAN", "GREATER", ">":
                return ">";
            case "LESS THAN", "LESS", "<":
                return "<";
            case "NOT GREATER THAN", "NOT GREATER", "<=":
                return "<=";
            default:
                return ">="; // NOT LESS THAN — COBOL's default START
        }
    }

    /**
     * The save-block transaction.
     *
     * <p>Faithfully carries the vendor COMMIT semantic: writes pile up in the connection's
     * transaction across the save chain, and the COBOL COMMIT — emitted as {@code commit()} on the
     * dataset registry — lands them atomically. Reads outside a save block stay independent, each
     * borrowing a fresh pooled connection.
     *
     * <p>The first write lazily binds an auto-commit=false connection through {@link
     * org.springframework.transaction.support.TransactionSynchronizationManager}; every later
     * JdbcTemplate call on the thread shares it (Spring's {@code DataSourceUtils.getConnection}
     * returns the bound resource when one exists). {@code commit()} or {@code rollbackIfPending()}
     * unbinds it.
     */
    private int update(String sql, Object[] args) {
        ensureSaveTxBound();
        return jdbcTemplate.update(sql, args);
    }

    /**
     * Binds an {@code auto-commit=false} connection as the running save-block transaction, unless
     * one is bound already. Later JdbcTemplate calls on the thread pick it up automatically via
     * {@code DataSourceUtils.getConnection}. The dataset registry releases it — {@code commit()}
     * for the atomic commit, {@code rollbackIfPending()} on the abort path.
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
     * Genuine JDBC savepoints on the bound save-block connection. A failing read or write rolls
     * back to its own savepoint and the enclosing transaction lives on for the operations after it
     * — which is precisely what prevents the aborted-transaction (25P02) cascade. With no
     * save-block transaction bound (reads outside a save block), all of this degenerates to no-ops.
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
            currentSavepoint =
                    null; // driver lacks savepoints — the rollback path copes without one
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
     * Targeted rollback to the savepoint set earlier; without one it does nothing. A blanket {@code
     * conn.rollback()} fallback would be wrong here: under a pool running auto-commit=false with
     * per-operation semantics it would erase legitimate earlier writes. Pair every call with {@link
     * #sqlSavepoint()} so the rollback genuinely clears the database's aborted-transaction state.
     */
    private void sqlRollbackSp() {
        if (currentSavepoint == null) return; // nothing to roll back to
        javax.sql.DataSource ds = jdbcTemplate.getDataSource();
        if (ds == null) {
            currentSavepoint = null;
            return;
        }
        try {
            java.sql.Connection conn =
                    org.springframework.jdbc.datasource.DataSourceUtils.getConnection(ds);
            if (!conn.getAutoCommit()) conn.rollback(currentSavepoint); // savepoint only
        } catch (java.sql.SQLException ignored) {
            /* connection unusable — the pool discards it on return */
        } finally {
            currentSavepoint = null;
        }
    }

    /** COBOL field name as snake_case SQL column — lowercase, hyphens to underscores. */
    protected static String toSnake(String fieldName) {
        if (fieldName == null) {
            return "";
        }
        return fieldName.toLowerCase().replace('-', '_');
    }

    /**
     * Reverse index for non-OCCURS fields — snake_case column to primary-record SchemaField, built
     * lazily. {@link #applyRowToBuffer(Map)} uses it for the plain case; columns with an {@code
     * _<n>} suffix go through {@link #occursLookup} instead.
     */
    private Map<String, SchemaField> snakeToField() {
        if (snakeToField == null) {
            buildLookups();
        }
        return snakeToField;
    }

    /**
     * Reverse index for OCCURS leaves — snake_case base column to its field and occurrence count. A
     * SQL column ending {@code _<n>} resolves its base name here, and the value lands at occurrence
     * n (1-based) through the buffer's subscripted setters.
     */
    private Map<String, SchemaField> occursLookup;

    private Map<String, Integer> occursCount;

    private void buildLookups() {
        Map<String, SchemaField> simple = new HashMap<>();
        Map<String, SchemaField> occLeafs = new HashMap<>();
        Map<String, Integer> occCounts = new HashMap<>();
        walkForLookups(layout().root(), 1, simple, occLeafs, occCounts);
        snakeToField = simple;
        occursLookup = occLeafs;
        occursCount = occCounts;
    }

    private void walkForLookups(
            SchemaNode n,
            int occursMult,
            Map<String, SchemaField> simple,
            Map<String, SchemaField> occLeafs,
            Map<String, Integer> occCounts) {
        if (n.isRedefines()) {
            return;
        }
        if (n instanceof SchemaField f) {
            String nm = f.getName();
            if (isFillerName(nm)) {
                return;
            }
            String col = toSnake(nm);
            if (occursMult > 1) {
                // OCCURS: the lookup key is the post-translation BASE — the column
                // minus its trailing _N. Translating index 0 and stripping the
                // suffix keeps applyRowToBuffer's split aligned with whatever
                // shape the DB schema actually uses.
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

    private Map<String, SchemaField> occursLookup() {
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
     * Walks the primary record and builds the snake_case column → value map that feeds
     * INSERT/UPDATE. A field under an OCCURS group flattens to one column per occurrence
     * (suffix-indexed), matching the column naming the seed schema uses. FILLERs are skipped.
     */
    private Map<String, Object> extractColumnsFromBuffer() {
        Map<String, Object> cols = new LinkedHashMap<>();
        walkForExtract(layout().root(), 1, cols);
        splitBlobOnWrite(cols);
        return cols;
    }

    /**
     * Hook for subclasses whose table stores a long field as fixed-size chunk columns (a single
     * wide buffer field persisted as N equal slices). No-op by default; a generated subclass
     * overrides it to split the buffer column into its chunks BEFORE the map goes to INSERT/UPDATE.
     * Inverse of {@link #concatBlobOnRead}.
     */
    protected void splitBlobOnWrite(Map<String, Object> cols) {}

    /**
     * Counterpart of {@link #splitBlobOnWrite}: stitches the chunk columns of a SELECT row back
     * into the single buffer column the field accessor expects. Runs ahead of the {@code
     * snakeToField()} lookup in {@link #applyRowToBuffer}.
     */
    protected void concatBlobOnRead(Map<String, Object> row) {}

    private void walkForExtract(SchemaNode n, int occursMult, Map<String, Object> cols) {
        if (n.isRedefines()) {
            return;
        }
        if (n instanceof SchemaField f) {
            String nm = f.getName();
            if (isFillerName(nm)) {
                return;
            }
            String col = toSnake(nm);
            if (occursMult > 1) {
                // The DB schema suffixes 0-based (col_0..col_{N-1}) while buffer
                // access takes 1-based COBOL subscripts — so columns are emitted
                // at _0.._{N-1} and the buffer is read at 1..N. Skipping the shift
                // would write col_1..col_N: col_0 stays empty and the _N value has
                // no column to land in, lost without a trace.
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

    /** Buffer value of a leaf at OCCURS occurrence {@code sub} (1-based). */
    private Object readBufferValueAt(SchemaField f, int sub) {
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

    /** Buffer value of one field, decoded according to its schema type. */
    private Object readBufferValue(SchemaField f) {
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
     * Writes a SQL result row back into the record bytes. Columns matching a primary-record leaf go
     * through the type-appropriate setter; columns suffixed {@code _<n>} route through the
     * subscripted setters into their occurrence; anything unknown (row_seq, REDEFINES-only) is
     * passed over.
     */
    private void applyRowToBuffer(Map<String, Object> row) {
        if (row == null) {
            return;
        }
        concatBlobOnRead(row);
        Map<String, SchemaField> simple = snakeToField();
        Map<String, SchemaField> occLeafs = occursLookup();
        Map<String, Integer> occCounts = occursCount();
        for (Map.Entry<String, Object> e : row.entrySet()) {
            String col = e.getKey() == null ? null : e.getKey().toLowerCase();
            Object val = e.getValue();
            if (col == null || val == null) {
                continue;
            }
            SchemaField f = simple.get(col);
            if (f != null) {
                writeBufferValue(f, val);
                continue;
            }
            // otherwise try the OCCURS column shape <base>_<n>
            int us = col.lastIndexOf('_');
            if (us > 0) {
                String base = col.substring(0, us);
                String idxStr = col.substring(us + 1);
                SchemaField fo = occLeafs.get(base);
                if (fo != null && !idxStr.isEmpty()) {
                    try {
                        int n = Integer.parseInt(idxStr);
                        Integer count = occCounts.get(base);
                        // Column suffixes are 0-based, buffer subscripts 1-based
                        // (COBOL). Both conventions are accepted, the 0-based
                        // reading first (n+1 becomes the subscript).
                        if (count != null && n >= 0 && n < count) {
                            writeBufferValueAt(fo, val, n + 1);
                            continue;
                        }
                        if (count != null && n >= 1 && n <= count) {
                            // a schema with 1-based suffixes
                            writeBufferValueAt(fo, val, n);
                        }
                    } catch (NumberFormatException ignore) {
                        /* suffix is not an index */
                    }
                }
            }
            // unrecognized columns pass through without effect
        }
    }

    /** Writes a leaf value at OCCURS occurrence {@code sub} (1-based). */
    private void writeBufferValueAt(SchemaField f, Object val, int sub) {
        String name = f.getName();
        String jt = f.getJavaType();
        try {
            if (jt == null || "String".equals(jt)) {
                buffer().setString(name, String.valueOf(val), sub);
                return;
            }
            switch (jt) {
                    // The empty-string→0 rule matches writeBufferValue: numerics default
                    // to 0 in COBOL, and a nullable column arrives as "" — parsing it
                    // would throw and the catch below would drop the write unnoticed.
                    // Keep the two variants aligned.
                case "int":
                    if (val instanceof Number num) {
                        buffer().setInt(name, num.intValue(), sub);
                    } else {
                        String s = String.valueOf(val).trim();
                        buffer().setInt(name, s.isEmpty() ? 0 : Integer.parseInt(s), sub);
                    }
                    break;
                case "long":
                    if (val instanceof Number num) {
                        buffer().setLong(name, num.longValue(), sub);
                    } else {
                        String s = String.valueOf(val).trim();
                        buffer().setLong(name, s.isEmpty() ? 0L : Long.parseLong(s), sub);
                    }
                    break;
                case "double":
                    if (val instanceof Number num) {
                        buffer().setDouble(name, num.doubleValue(), sub);
                    } else {
                        String s = String.valueOf(val).trim();
                        buffer().setDouble(name, s.isEmpty() ? 0.0 : Double.parseDouble(s), sub);
                    }
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
            // the occurrence keeps its previous value
        }
    }

    private void writeBufferValue(SchemaField f, Object val) {
        String name = f.getName();
        String jt = f.getJavaType();
        try {
            if (jt == null || "String".equals(jt)) {
                buffer().setString(name, String.valueOf(val));
                return;
            }
            switch (jt) {
                    // COBOL numerics default to 0, and a NULL/blank column reaches this
                    // point as an empty string — it must become 0 rather than letting
                    // Integer.parseInt throw, where the catch below would swallow the
                    // error and leave the field blank on read-back.
                case "int":
                    if (val instanceof Number num) {
                        buffer().setInt(name, num.intValue());
                    } else {
                        String s = String.valueOf(val).trim();
                        buffer().setInt(name, s.isEmpty() ? 0 : Integer.parseInt(s));
                    }
                    break;
                case "long":
                    if (val instanceof Number num) {
                        buffer().setLong(name, num.longValue());
                    } else {
                        String s = String.valueOf(val).trim();
                        buffer().setLong(name, s.isEmpty() ? 0L : Long.parseLong(s));
                    }
                    break;
                case "double":
                    if (val instanceof Number num) {
                        buffer().setDouble(name, num.doubleValue());
                    } else {
                        String s = String.valueOf(val).trim();
                        buffer().setDouble(name, s.isEmpty() ? 0.0 : Double.parseDouble(s));
                    }
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
            // Best-effort write, but the cause goes to the WARN log: a silent catch
            // would bury row-to-buffer failures (picture mismatches, national-string
            // encoding edges) that only show up later as blank display cells —
            // nearly impossible to trace without this trail.
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

    /** The record-key value in the current buffer; composite keys come back pipe-joined. */
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
                /* segment stays empty */
            }
        }
        return pk.toString();
    }

    /**
     * Guarantees the SQL table exists — column types from layout.sqlColumnTypes(), composite
     * PRIMARY KEY from the RECORD KEY metadata. Idempotent and thread-safe.
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
                /* existence unknown — attempt creation */
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
                // Cột duy nhất trong DDL này từng hard-code kiểu PG. Mọi cột khác đã đi qua
                // mapColumnType() nên chỉ nó vỡ trên Oracle (BIGINT → ORA-00902).
                ddl.append("row_seq ")
                        .append(dialect().mapColumnType("BIGINT"))
                        .append(" PRIMARY KEY");
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
            // The DDL runs on its OWN auto-commit connection so it persists at once,
            // independent of the program's transaction. On the shared transactional
            // connection the CREATE would be rolled back whenever the program
            // aborts — and the program aborts precisely because the table was
            // missing — so the table could never come into being. The isolated
            // auto-commit CREATE breaks that circle.
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
            ENSURED_TABLES.add(table); // a broken DB should not trigger a retry storm
        }
    }

    /**
     * Seeds the synthetic row_seq counter from MAX(row_seq) at open, so WRITEs against a SEQUENTIAL
     * table that already holds rows — say, carried over from an earlier run — don't collide on the
     * PRIMARY KEY. Otherwise every fresh JVM start resets the counter to 0 and the first writes
     * into any non-empty SEQUENTIAL table die on duplicate keys (SQLSTATE 23505). INDEXED files
     * leave row_seq to the database's own DEFAULT nextval(), making this a no-op for them.
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
            // Table absent or unreadable: the counter stays at 0, and any truly
            // conflicting rows will make the INSERTs fail audibly later.
            sqlSeqCounter = 0L;
        }
    }

    /** Approximate inverse of toSnake, needed only by the FILLER filter during DDL emit. */
    private static String snakeToSourceName(String snake) {
        return snake == null ? "" : snake.toUpperCase().replace('_', '-');
    }

    /**
     * The table's actual DB column set, cached. WRITE consults it to drop layout columns with no
     * real column behind them (the REDEFINES gaps).
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
                        /* catalog unavailable — empty set */
                    }
                    return result;
                });
    }

    /**
     * The table's GENERATED ALWAYS identity columns, cached. INSERT/UPDATE must leave them out — an
     * explicit value draws SQLSTATE 428C9. Detecting them through information_schema keeps this
     * robust to identity columns added on the database side.
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

    /* ── SQL CRUD ────────────────────────────────────────────────────── */

    /**
     * SQL READ: keyed on the current buffer key for INDEXED files, a sequential browse otherwise.
     */
    protected boolean sqlRead() {
        sqlSavepoint();
        try {
            if (openMode == DatasetEnums.FileOpenMode.OUTPUT) {
                fileStatus = "47";
                return false;
            }
            // an active browse serves the next cached row
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
            // SEQUENTIAL files browse every row in insertion order
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
            // INDEXED: key from the buffer if it has one, else browse everything
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

    /** SQL READ on an explicitly supplied key — no buffer-key extraction involved. */
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
            // A random READ with an empty or zero key still looks up EXACTLY that
            // key — COBOL answers INVALID KEY (status 23) when no such record
            // exists, and the caller's handler fires. An empty key on a numeric
            // key column becomes "0" so the typed WHERE stays well-formed
            // (CAST('' AS BIGINT) would blow up).
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
     * SQL twin of {@link #readByRelativeKey(long)}. A RELATIVE-organization file embeds no record
     * key in the row — {@link #getKeyColumns()} is empty, and {@link #buildKeyWhere()} would
     * produce a bare {@code WHERE } with nothing behind it (broken SQL, caught as a generic status
     * "30"). The relative record number instead maps onto the synthetic {@code row_seq} that {@link
     * #sqlWrite()} assigns in insertion order, so the lookup is {@code WHERE row_seq = ?}.
     */
    protected boolean sqlReadByRelativeKey(long relativeRecordNumber) {
        sqlSavepoint();
        try {
            if (openMode == DatasetEnums.FileOpenMode.OUTPUT) {
                fileStatus = "47";
                return false;
            }
            sqlBrowseResults = null;
            sqlBrowseIndex = 0;
            endOfFile = false;
            List<Map<String, Object>> rows =
                    jdbcTemplate.queryForList(
                            "SELECT * FROM " + getTableName() + " WHERE row_seq = ?",
                            relativeRecordNumber);
            if (rows.isEmpty()) {
                invalidKey = true;
                fileStatus = "23";
                return false;
            }
            applyRowToBuffer(rows.get(0));
            captureRowSeq(rows.get(0));
            lastReadKey = rows.get(0).get("row_seq");
            sqlRelease();
            fileStatus = "00";
            return true;
        } catch (Exception e) {
            LOG.warn(
                    "sqlReadByRelativeKey FAILED file={} table={} key={} err={}: {}",
                    getFileName(),
                    getTableName(),
                    relativeRecordNumber,
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e);
            sqlRollbackSp();
            fileStatus = "30";
            return false;
        }
    }

    /**
     * SQL READ on an ALTERNATE KEY ({@code READ file KEY altKey}): the COBOL field name becomes its
     * snake_case column, the WHERE is built on it, the first matching row lands in the buffer. No
     * match sets invalidKey with status 23, firing the caller's {@code INVALID KEY} handler.
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
                // no alternate key named — take the primary-key path
                return sqlReadByExplicitKey(key);
            }
            // A composite alternate key arrives as a space-joined field tuple with a
            // pipe-joined value tuple; each pair contributes one AND term. A
            // single-column alternate key naturally reduces to a one-term WHERE.
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

    /**
     * SQL READ NEXT: continue the cached browse, walk SELECT WHERE results, or open a fresh browse.
     */
    protected boolean sqlReadNext() {
        sqlSavepoint();
        try {
            if (openMode == DatasetEnums.FileOpenMode.OUTPUT) {
                fileStatus = "47";
                return false;
            }
            // A forward read uses up any READ-PRIOR anchor a START left behind; a
            // stray PRIOR later falls back to lastReadKey instead of the old START.
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
                // The SELECT WHERE result set is the sole authority: walk it or
                // report EOF. Falling through to a keyed read on a stale
                // lastReadKey is forbidden — with zero rows (genuinely empty or
                // cleared by an exception) the caller expects AT END, not the
                // same row forever.
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
            // INDEXED with no cursor yet: browse the lot
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

    /** SQL READ FOR UPDATE: the keyed read, with the row locked. */
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
     * SQL READ PRIOR — the reverse browse.
     *
     * <p>Hitachi ISAM defines READ PRIOR as <em>read-at-then-move</em>: the first PRIOR after a
     * START yields the record the cursor sits AT, and every further PRIOR steps one record back.
     *
     * <p>A cursor already descending (built here by the first PRIOR after a START) is simply walked
     * on — each step is the next-older record. A cursor an ascending {@link #sqlStart}/READ NEXT
     * left behind must NOT be walked: the order would be wrong. The first PRIOR therefore rebuilds
     * a descending page {@code WHERE tuple <= anchor}, the anchor being the key of the record START
     * positioned at ({@link #lastStartAnchorVals}); the anchor row itself satisfies {@code <=
     * anchor}, so the first PRIOR returns it — read-at-then- move — and the following PRIORs
     * continue down the page.
     *
     * <p>A bare PRIOR with no START before it anchors on {@link #lastReadKey} with a strict {@code
     * < key} — a best-effort reverse scan.
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
            // first PRIOR after a START left the cursor ascending: rebuild descending
            if (lastStartCols != null && !lastStartCols.isEmpty() && lastStartAnchorVals != null) {
                // `<= anchor` INCLUDES the positioned record (read-at-then-move), and
                // the tuple compare covers composite alternate keys, not just column 1.
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
            // anchor spent — later PRIORs walk the descending page without re-anchoring
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

    /**
     * SQL WRITE: INSERT of every primary-record column; SEQUENTIAL files add their synthetic
     * row_seq.
     */
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
            // keep only columns the DB schema really has
            Set<String> tableCols = schemaColumns();
            if (!tableCols.isEmpty()) {
                cols.entrySet().removeIf(e -> !tableCols.contains(e.getKey().toLowerCase()));
            }
            // GENERATED ALWAYS identity columns are excluded — explicit values draw 428C9
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
            // The INSERT sits inside a savepoint: a duplicate-key or SQL error marks
            // the whole transaction aborted (25P02) until something rolls back, and
            // without the savepoint rollback every following read on the connection
            // would fail with status 30 — one colliding INSERT poisoning the rest.
            try {
                update(sql.toString(), vals.toArray());
                sqlRelease();
                fileStatus = "00";
            } catch (DuplicateKeyException dup) {
                sqlRollbackSp();
                // WRITE INVALID KEY corresponds to status 22 (duplicate key);
                // invalidKey must be raised here or the generated INVALID KEY
                // clause would never fire on duplicates.
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

    /** SQL REWRITE: UPDATE keyed on the composite key, or on row_seq when known. */
    protected void sqlRewrite() {
        // the write joins the save-block transaction (see update() above)
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
            // GENERATED ALWAYS identity columns are excluded — explicit values draw 428C9
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
                // REWRITE INVALID KEY corresponds to status 23 (record not found);
                // an update touching zero rows must raise invalidKey.
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

    /** SQL DELETE: keyed on the composite key, or on row_seq when known. */
    protected void sqlDelete() {
        // the write joins the save-block transaction (see update() above)
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

    /** SQL DELETE on an explicit key; composite values arrive pipe-joined. */
    protected void sqlDeleteByKey(Object key) {
        // the write joins the save-block transaction (see update() above)
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
     * SQL START — positions the browse cursor from a key under a comparison operator.
     *
     * <p>The generated call is {@code start("FIELD-NAME", "NOT LESS THAN")}: the {@code key}
     * argument is the KEY FIELD NAME from the FD's RECORD KEY, never a comparison value. The value
     * to compare against sits in the record buffer, which the program filled through its accessors
     * just before the START — the query becomes {@code WHERE key_col >= <buffer value> ORDER BY
     * key_col}.
     *
     * <p>Treating {@code key} itself as the comparison value would produce {@code WHERE
     * CAST(key_col AS VARCHAR) >= 'FIELD-NAME'} — zero rows on any numeric column — which is why
     * the value is always pulled from the buffer.
     */
    protected boolean sqlStart(String key, String operator) {
        sqlSavepoint();
        invalidKey = false;
        try {
            String sqlOp = mapStartOp(operator);
            // Position on the key the START actually names — the primary RECORD KEY
            // or an ALTERNATE key. The argument arrives in one of two shapes:
            //   (a) a space-separated list of real record FIELDS (an alternate key
            //       spelled out) — usable verbatim, so the START lands on the right
            //       columns and alternate-key range bounds hold; or
            //   (b) a bare key-name alias from a RECORD KEY IS clause — no such
            //       column exists, so querying it would fail.
            // The argument is trusted only when every token resolves to a real
            // field; anything else defers to the primary key, which is correct for
            // the alias case.
            String keySpec = isKeyFieldList(key) ? key : getRecordKey();
            List<String> cols = resolveKeyColumns(keySpec);
            // START ... INVALID KEY: status 23 must travel with invalidKey=true or
            // the generated `if (isInvalidKey())` branch never runs and the caller
            // drops into the abort path despite having wired a handler.
            if (cols.isEmpty()) {
                invalidKey = true;
                fileStatus = "23";
                return false;
            }

            // Each key field's current value comes straight from the record buffer:
            // START positions on the first record whose key tuple satisfies the
            // operator against the working-storage values of that moment.
            String[] keyParts = keySpec.trim().split("\\s+");
            Object[] vals = new Object[cols.size()];
            for (int i = 0; i < cols.size(); i++) {
                try {
                    vals[i] = buffer().getString(keyParts[i]).trim();
                } catch (Exception e) {
                    vals[i] = "";
                }
            }

            // The tuple comparison: range operators on a composite key expand into
            // the equivalent OR-chain so each column keeps its own type cast
            // (BIGINT for numerics, VARCHAR for strings); equality is a plain
            // AND-chain.
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

            // The browse orders by the key that was positioned on — READ NEXT walks
            // that same alternate/primary key, not necessarily the primary order.
            String sql =
                    "SELECT * FROM "
                            + getTableName()
                            + " WHERE "
                            + where
                            + " ORDER BY "
                            + String.join(", ", cols)
                            + " FETCH FIRST "
                            + BROWSE_PAGE_SIZE
                            + " ROWS ONLY";
            sqlBrowseResults = jdbcTemplate.queryForList(sql, params.toArray());
            sqlBrowseIndex = 0;
            sqlBrowseDescending = false;
            // START must clear endOfFile — a stale EOF from an earlier empty SELECT
            // would otherwise make the very next readNext() report AT END off a
            // perfectly fresh cursor.
            endOfFile = false;
            if (sqlBrowseResults.isEmpty()) {
                lastStartCols = null;
                lastStartAnchorVals = null;
                sqlRelease();
                invalidKey = true;
                fileStatus = "23";
                return false;
            }
            // The key of the record the cursor now sits AT (first row of the
            // ascending page) is captured so a following READ PRIOR — read-at-then-
            // move — returns this record first and only then steps back. The anchor
            // comes from the positioned ROW, not the START values: a START against
            // a sentinel key must anchor on the real row it found, not the sentinel.
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
     * SQL START LAST — positions on the final record in the named key's order: an unfiltered
     * descending page, so the {@link #sqlReadPrev} walk that follows yields the last record first
     * and steps backward from there. Status handling matches {@link #sqlStart} — an empty table
     * gives status 23 with invalidKey. Alternate keys resolve through {@link #resolveKeyColumns}.
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
            // START LAST is an absolute position at end-of-key; the READ PRIOR that
            // follows must not re-anchor on some stale key, so the anchor is wiped.
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
     * Column side of a key predicate. String keys sit in storage SPACE-PADDED to their PIC width
     * while callers pass trimmed values, so the column is RTRIM'd — without it the equality can
     * never hold. Numeric keys cast to BIGINT and need no padding logic.
     */
    private String keyColRef(String col) {
        return isNumericKeyColumn(col)
                ? "CAST(" + col + " AS BIGINT)"
                : "CAST(RTRIM(" + col + ") AS VARCHAR)";
    }

    /** Parameter side of a key predicate — the matching typed cast; values arrive trimmed. */
    private String keyParamRef(String col) {
        return isNumericKeyColumn(col) ? "CAST(? AS BIGINT)" : "CAST(? AS VARCHAR)";
    }

    /** Typed equality predicate for one key column (string keys RTRIM'd). */
    private String buildKeyEq(String col) {
        return dialect().normalizeWhere(keyColRef(col) + "=" + keyParamRef(col));
    }

    /** Typed comparison predicate for one key column, op one of &gt; &gt;= &lt; &lt;=. */
    private String buildKeyOp(String col, String op) {
        return dialect().normalizeWhere(keyColRef(col) + op + keyParamRef(col));
    }

    /**
     * Composite-key tuple comparison for the range operators, written as the equivalent OR-chain so
     * every column keeps its own type cast:
     *
     * <pre>   (c1 OP v1)
     *        OR (c1=v1 AND c2 OP v2)
     *        OR (c1=v1 AND c2=v2 AND c3 OP v3) ...</pre>
     *
     * The prefix columns compare strictly, the last column with {@code op}; parameters append to
     * {@code outParams} in placeholder order. Both {@link #sqlStart} (forward) and {@link
     * #sqlReadPrev} (reverse) build on it.
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

    /* ── Vendor ISAM extensions — SELECT WHERE / readSelected / scratch ─────────────── */

    /**
     * Vendor ISAM SELECT WHERE: queries the file through an arbitrary SQL WHERE. The rows land in
     * {@link #selectedRecords} and are walked by {@link #readSelected()}.
     */
    public void doSqlSelectWhere(
            String whereClause, String orderByCol, boolean ascending, Object... params) {
        // A hand-written WHERE may carry PostgreSQL-shaped CASTs; the dialect
        // normalizes them (no-op on PG/H2, BIGINT→NUMBER and VARCHAR→VARCHAR2 on Oracle).
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

    /** Whether the last SELECT WHERE produced any rows. */
    public boolean hasSelectedRecords() {
        return !selectedRecords.isEmpty();
    }

    /** Next row of the SELECT WHERE result set; false once the set is exhausted. */
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

    /* ── Open / close lifecycle ──────────────────────────────────────── */

    /**
     * Whether the COBOL {@code SELECT} for this file carried a {@code FILE STATUS} clause.
     * Generated subclasses override it to true; the default is false so a hand-written or legacy
     * subclass keeps the abend-on-open-failure behaviour.
     */
    protected boolean hasFileStatusClause() {
        return false;
    }

    public void open(DatasetEnums.FileOpenMode mode) {
        // SQL mode opens no file: record the state and make sure the table exists.
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
            // OPEN OUTPUT means truncation — an atomic, self-contained act. The
            // DELETE runs on its own auto-commit connection, independent of any
            // save-block transaction: that avoids an idle-in-transaction connection
            // holding row locks while the program waits at a screen, and it works
            // whether or not a save-block transaction is bound (a savepoint-based
            // variant used to skip the DELETE silently when none was). A failure —
            // table not yet materialized, say — is logged and ignored, and cannot
            // poison the surrounding transaction because it never touches it.
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
                    // the FileChannel stays reachable so flush() can force(true) — see outChannel
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
                    // one seekable handle: reads advance it, REWRITE seeks back to
                    // lastReadPos and overwrites the right record — never byte 0
                    Files.createDirectories(path.getParent());
                    if (!Files.exists(path)) {
                        Files.createFile(path);
                    }
                    raf = new RandomAccessFile(path.toFile(), "rw");
                    lastReadPos = -1L;
                    // INDEXED on the file path: scan once, build the key indexes
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
            // On the mainframe, JCL abends before PROCEDURE DIVISION when an INPUT
            // file is missing; the env-var file binding of this port created a
            // runtime path COBOL never had. Swallowing the failure let read() spin
            // on an unopened file and OUTPUT writes fill the disk — so it
            // propagates, and the service's catch sets completion code 12.
            // endOfFile=true on INPUT additionally covers callers that catch the
            // throw and inspect state instead.
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
     * Reads one record — via sqlRead() in SQL mode, exactly recordLength bytes in binary mode, up
     * to the newline in text mode.
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
                // Leading CR/LF left between records by text-formatted fixtures is
                // consumed first. A genuine binary record never begins with
                // 0x0A/0x0D — those bytes belong inside printable fields, not at
                // byte 0 after the previous record. Left in place, the second
                // sequential read would start on record 1's '\n', shifting every
                // later field offset by one byte so each field reads its
                // neighbour's bytes.
                skipBinaryRecordSeparators();
                if (raf != null) {
                    // IO mode reads through the seekable handle, noting the position for REWRITE
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
                // text mode (LINE SEQUENTIAL) exists only on the InputStream path
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
        // INDEXED on the file path: walk the cursor when fileStart/fileReadByKey set
        // one, otherwise fall back to a sequential read() (a raw READ NEXT after open).
        if (isFileModeIndexed() && (browseCursor != null || altCursorList != null)) {
            return fileReadNext();
        }
        return read();
    }

    public boolean readByKey(Object key) {
        invalidKey = false;
        if (isSqlMode()) {
            // RELATIVE organization: the key is a record NUMBER, not a stored value,
            // so it routes to the row_seq lookup — the explicit-key path would build
            // an empty WHERE, RELATIVE files declaring no record key.
            if (isRelativeOrganization() && key instanceof Number) {
                return sqlReadByRelativeKey(((Number) key).longValue());
            }
            return sqlReadByExplicitKey(key);
        }
        if (isFileModeIndexed()) {
            return fileReadByKey(null, key);
        }
        if (isRelativeOrganization() && key instanceof Number) {
            return readByRelativeKey(((Number) key).longValue());
        }
        return read();
    }

    /**
     * READ on an ALTERNATE KEY — {@code READ file KEY altKey}. The record schema records the {@code
     * READ ... KEY X} clause, and those reads land here so the WHERE queries the named column
     * rather than the primary RECORD KEY. See {@link #sqlReadByAlternateKey(String, Object)}.
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
     * {@code READ ... PRIOR} — reverse sequential read from the current browse position. SQL mode
     * goes through {@link #sqlReadPrev()}, which rebuilds a descending page anchored where START
     * left the cursor on a direction change. The file path has no reverse browse (no file-path
     * caller needs one) and falls back to {@link #read()}, matching the other file-path methods so
     * callers behave consistently rather than meeting an exception.
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
     * {@code START ... LAST KEY <key>} — positions the cursor at the LAST record in the named key's
     * order, ignoring buffer key values; used ahead of a backward READ PRIOR walk. SQL mode goes
     * through {@link #sqlStartLast(String)}. The file path is unimplemented (no file-path caller
     * needs it) and falls back to {@link #read()}, matching the other file-path methods so callers
     * stay consistent instead of meeting an exception.
     */
    public boolean startLast(String key) {
        invalidKey = false;
        if (isSqlMode()) {
            return sqlStartLast(key);
        }
        return read();
    }

    /* ── ISAM I/O on the file path ───────────────────────────────────────────
     *  Active when raf != null and the file has a record key — an INDEXED file the
     *  generator chose to back with a local file rather than SQL, so it registers
     *  no JdbcTemplate and isSqlMode() is false, letting these methods run.
     *
     *  Design:
     *  - The primary-key index is built at open() by {@link #rebuildIndex()}.
     *  - Alternate-key indexes are multimaps (value → list of offsets) so
     *    DUPLICATES works; a UNIQUE alternate key is the single-element case.
     *  - DELETE sets the slot's bit in {@link #deletedOffsets}, zero-fills the
     *    on-disk record for good measure, and drops it from the indexes.
     *  - FILE STATUS values follow the ISAM convention: 00 success, 02 duplicate
     *    warning, 10 EOF, 22 duplicate-key failure, 23 invalid key, 30 I/O error,
     *    35 file not found, 43 no prior READ, 47 wrong mode for read, 48 wrong
     *    mode for write.
     */
    protected boolean isFileModeIndexed() {
        return !isSqlMode() && raf != null && !isSequentialFile();
    }

    /**
     * Overridden by the generated subclass when alternate keys exist: the list of alternate-key
     * names. A composite alternate key is space-joined; the default empty list means none.
     */
    public java.util.List<String> getAlternateKeyNames() {
        return java.util.Collections.emptyList();
    }

    /**
     * Overridden by the generated subclass when the COBOL DUPLICATES flag is set: whether the named
     * alternate key allows duplicate values on WRITE.
     */
    public boolean isAlternateKeyDuplicates(String altKeyName) {
        return false;
    }

    /**
     * A key value read from the CURRENT buffer for any key, primary or alternate; composite keys
     * come back pipe-joined.
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
                /* segment stays empty */
            }
        }
        return sb.toString();
    }

    /**
     * One linear pass building the primary and alternate indexes from the on-disk records; an
     * all-zero record is read as a tombstone and skipped.
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
                // an all-zero record is a tombstone — file-path DELETE zero-fills
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
            // leave the handle at end-of-file so later appends land correctly
            raf.seek(fileLen);
        } catch (IOException e) {
            // a partial index is acceptable; genuine failures surface later as status 30
        }
    }

    /**
     * Reads the record at a byte offset into the buffer; false on EOF, I/O error, or a tombstone.
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
     * File-path READ KEY IS X — primary key (altKeyName null) or an alternate key. The alternate
     * path arms the DUPLICATES cursor so a following readNext() walks the records sharing that
     * value before moving to higher keys.
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
            altCursorList = null; // a primary read clears the alternate cursor
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
     * File-path START — primary key only. Sets {@link #browseCursor} so a following {@link
     * #readNext()} walks records in key order from the start position. Operators: EQ, GE / NOT LESS
     * THAN, GT / GREATER (LE and LT are not handled here).
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
     * File-path READ NEXT after fileStart() or fileReadByKey(altKey, ...): walks {@link
     * #browseCursor} on the primary START path, or advances {@link #altCursorList} through a
     * DUPLICATES alternate-key run.
     */
    private boolean fileReadNext() {
        if (!isFileModeIndexed()) {
            fileStatus = "47";
            return false;
        }
        // DUPLICATES walk: advance within the list, then step to the next key value
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
        // primary-key browse cursor
        if (browseCursor != null && browseCursor.hasNext()) {
            java.util.Map.Entry<String, Long> entry = browseCursor.next();
            long off = entry.getValue();
            int recLen = recordLength();
            // step over tombstoned records
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

    /**
     * File-path DELETE of the last-read record: tombstone bit, on-disk zero-fill, index removal.
     */
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
        // drop it from the primary index
        if (primaryIndex != null) {
            primaryIndex.entrySet().removeIf(e -> e.getValue() == deletedOff);
        }
        // and from every alternate index
        if (altIndexes != null) {
            for (java.util.TreeMap<String, java.util.List<Long>> idx : altIndexes.values()) {
                for (java.util.List<Long> list : idx.values()) {
                    list.removeIf(o -> o == deletedOff);
                }
            }
        }
        // zero-fill on disk so the next open's rebuildIndex sees the tombstone again
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
     * File-path WRITE: appends at end-of-file and updates the indexes. A primary-key collision
     * fails with status 22 up front; for alternate keys, UNIQUE collides (status 22) while
     * DUPLICATES is allowed (status 00).
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
        // reject a UNIQUE alternate-key collision before writing
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
            // update the indexes
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
        // SQL mode clears the cursors; the file path does nothing.
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
                /* advancing is best-effort */
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
                /* advancing is best-effort */
            }
        }
        write();
    }

    /** Vendor ISAM helper — row count from the most recent SELECT WHERE. */
    public int getSelectedCount() {
        return isSqlMode() ? selectedRecords.size() : 0;
    }

    /** Vendor ISAM SELECT WHERE — runs the SQL implementation in SQL mode, nothing otherwise. */
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
        // INDEXED on the file path goes through fileWriteWithIndex so the key
        // indexes built at open() stay coherent for a later within-session READ.
        // Otherwise the index is empty after open and never fills, and any keyed
        // read comes back status 23 even for keys just written. CSV is sequential
        // by convention, never truly INDEXED, so the csv() guard keeps CSV on the
        // raw-bytes path.
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
     * Serializes the record as a WITH CSV row — each leaf field one comma-separated cell, the row
     * terminated by CRLF. The cell rules: - edited numeric (PIC Z…, ---,--9): the DE-EDITED raw
     * integer; - plain numeric DISPLAY (PIC 9 / S9): the raw digit bytes, unquoted; - alphanumeric
     * (X) / national (N): always quoted, the FULL field width kept (no trim), an embedded " doubled
     * to "".
     */
    private byte[] serializeCsvRecord() {
        byte[] data = buffer().bytes();
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (SchemaField f : layout().leaves()) {
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
                // de-edit: drop filler/commas, recover the sign, emit the raw signed integer
                String raw = new String(data, off, len, layout().charset());
                sb.append(deEditNumeric(raw));
            } else if (f.getKind() == SchemaField.Kind.NUM) {
                // plain numeric DISPLAY: raw digit bytes, unquoted
                sb.append(new String(data, off, len, layout().charset()));
            } else {
                // alphanumeric / national: always quoted, full field width kept
                // (X padded ASCII space, N padded full-width space — already in the
                // buffer, no trim), an internal " doubled to ""
                String cell = new String(data, off, len, layout().charset());
                sb.append('"').append(cell.replace("\"", "\"\"")).append('"');
            }
        }
        sb.append("\r\n");
        return sb.toString().getBytes(layout().charset());
    }

    /** De-edits an edited numeric slice: blanks and commas dropped, sign recovered from '-'. */
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
        // drop leading zeros, keeping at least one digit
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
        // REWRITE overwrites the record at the position of the most recent READ.
        // IO mode seeks the handle back to lastReadPos and writes the bytes
        // directly — not through the public write(), which now sends INDEXED files
        // to fileWriteWithIndex() and would append at end-of-file rather than
        // overwrite in place. The plain stream path, having no seek, does its
        // best-effort write at the current output position.
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
            // plain stream path — no seek, defer to write()
            write();
        }
        // REWRITE must follow a matching READ. Clearing lastReadPos after a
        // successful rewrite makes a second REWRITE with no READ between report
        // status 43; without it the second call would silently overwrite the same
        // spot again and hide the caller's mistake.
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
        /* sequential files do nothing */
    }

    /** Deletes a record by explicit key (SQL mode only). */
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
            /* close failure is inconsequential */
        }
        try {
            if (out != null) {
                out.flush();
                out.close();
            }
        } catch (IOException ignore) {
            /* close failure is inconsequential */
        }
        try {
            if (raf != null) raf.close();
        } catch (IOException ignore) {
            /* close failure is inconsequential */
        }
        in = null;
        out = null;
        raf = null;
        outChannel = null;
        lastReadPos = -1L;
        // drop the ISAM index state — the next open() rebuilds it if needed
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
     * Flushes buffered OUTPUT/EXTEND writes to disk WITHOUT closing the file.
     *
     * <p>{@link #close()} is otherwise the only place {@code out} is flushed, and an interactive
     * program can hold a print file open across an ACCEPT screen, reaching CLOSE only at exit.
     * Should the operator abandon the session first, the buffered stream is discarded unflushed and
     * the file — truncated to 0 bytes by OPEN OUTPUT — stays empty. Flushing at each transaction
     * boundary (see the dataset registry's commit) makes every printed block durable at once. A
     * no-op in SQL mode and when no output stream is open.
     */
    public void flush() {
        if (isSqlMode()) {
            return;
        }
        if (out != null) {
            try {
                out.flush();
                // force(true) syncs data and size metadata so the OS updates the
                // directory-entry size at once. Without it Windows keeps the
                // visible size stale until CLOSE, and a Docker bind-mount to a
                // Windows host would not reflect the write either. Print files are
                // small, so it is cheap.
                if (outChannel != null && outChannel.isOpen()) {
                    outChannel.force(true);
                }
            } catch (IOException ignore) {
                /* flush is best-effort */
            }
        }
    }

    /* ── Helpers ───────────────────────────────────────────────────── */

    /**
     * A path handed in by the service, taken from the WORKING-STORAGE variable tied to the ASSIGN
     * slot: an {@code ASSIGN TO DK-<slot>} reads its path from the WS item {@code <slot>}, which
     * the program filled in its init paragraph via {@code ACCEPT … FROM ENVIRONMENT-VALUE}.
     */
    private String injectedPath;

    // trimmed because WS variables are fixed width (PIC X(256)) and carry trailing spaces
    public void setPath(String path) {
        this.injectedPath = path == null ? null : path.trim();
    }

    protected Path resolveFilePath() {
        // preferred: the service already resolved the path from the WS variable the
        // ASSIGN clause names (DK-<slot> ↔ WS <slot>)
        if (injectedPath != null && !injectedPath.isEmpty()) {
            return Paths.get(injectedPath);
        }
        // scratch files (ASSIGN suffix -MSD) go to the per-session scratch directory
        // bound at session-connect time, keeping two concurrent sessions of the same
        // program on separate paths so they never collide on a shared scratch file.
        // Outside a session (batch/console/test) this falls through to the env-var lookup.
        String assign = getAssignTo();
        if (assign != null && assign.toUpperCase().endsWith("-MSD")) {
            try {
                Path sessionDir = Utility.getScratchDir();
                if (sessionDir != null) {
                    return sessionDir.resolve(getFileName() + ".dat");
                }
            } catch (Throwable ignored) {
                /* Utility may be off some test classpaths */
            }
        }
        // fallback for paths not injected: a direct env-var lookup
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

    /** Strips the {@code DK-}/{@code DK_} DD-name prefix, leaving the slot id. */
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
     * Skips leading CR/LF at the current read position so a binary fixed-length sequential read
     * stays aligned when the file uses newline-terminated records (the shape many test fixtures
     * take). A genuine binary record never opens with 0x0A/0x0D — those bytes live inside printable
     * fields, never at byte 0 after the previous record — so the skip is safe for either format.
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

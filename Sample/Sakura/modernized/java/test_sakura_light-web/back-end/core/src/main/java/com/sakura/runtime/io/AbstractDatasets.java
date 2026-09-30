package com.sakura.runtime.io;

import com.sakura.runtime.record.RawDatasetBase;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for per-program FileSets. Centralizes JdbcTemplate wiring, SQL-mode init, and file
 * lifecycle. Transaction management is delegated to Spring's @Transactional on BatchServiceBase —
 * commit()/rollbackIfPending() remain as no-op extension points.
 */
public abstract class AbstractDatasets implements AutoCloseable {

    private final List<RawDatasetBase> files = new ArrayList<>();

    @Autowired(required = false)
    protected JdbcTemplate jdbcTemplate;

    @Value("${cobol.file.sql:false}")
    protected boolean isSqlMode;

    protected void registerFile(RawDatasetBase file) {
        if (file != null) {
            files.add(file);
        }
    }

    @PostConstruct
    public void registerFiles() {
        if (jdbcTemplate != null && isSqlMode) {
            files.forEach(f -> f.setJdbcTemplate(jdbcTemplate));
        }
    }

    /**
     * COBOL {@code COMMIT scope="vendor"} — atomic commit of the current save block.
     *
     * <p>Faithful semantic: every write in the save block was lazy-bound via
     * RawDatasetBase.ensureSaveTxBound to ONE connection (auto-commit=false); this commits that
     * connection's accumulated transaction atomically and unbinds it. After unbind, subsequent
     * reads borrow fresh connections from the pool (no shared tx state) until the next write opens
     * a new save-block tx.
     *
     * <p>No-op when no save-block tx is bound (program had no writes since the previous commit, or
     * never wrote — single-write programs persist via the commit at the end of
     * BatchServiceBase.execute()).
     */
    public void commit() {
        // Flush buffered print/output files at this transaction boundary so an
        // interactive program holding a print file open across an ACCEPT loop makes
        // each committed block durable without waiting for CLOSE at program exit. A
        // no-op for SQL-bound and unopened files. Runs BEFORE the SQL guard below so it
        // also fires in text mode (jdbcTemplate == null) — exactly where print files live.
        files.forEach(RawDatasetBase::flush);
        if (jdbcTemplate == null) {
            return;
        }
        javax.sql.DataSource ds = jdbcTemplate.getDataSource();
        if (ds == null) {
            return;
        }
        // Defer to an active outer (Spring-managed) transaction — e.g. the Spring Batch
        // tasklet tx: IT owns the bound connection and commits at its own boundary.
        // Unbinding here would leave the framework's cleanup with nothing to unbind
        // ("No value for key [DataSource] bound to thread"). Interactive and console
        // paths run with no outer tx, so this guard is false there and the manual commit runs.
        if (org.springframework.transaction.support.TransactionSynchronizationManager
                .isActualTransactionActive()) {
            return;
        }
        if (!org.springframework.transaction.support.TransactionSynchronizationManager.hasResource(
                ds)) {
            return;
        }
        org.springframework.jdbc.datasource.ConnectionHolder h =
                (org.springframework.jdbc.datasource.ConnectionHolder)
                        org.springframework.transaction.support.TransactionSynchronizationManager
                                .unbindResource(ds);
        java.sql.Connection conn = h.getConnection();
        try {
            if (!conn.getAutoCommit()) {
                conn.commit();
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("COBOL COMMIT failed: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (java.sql.SQLException ignored) {
            }
            org.springframework.jdbc.datasource.DataSourceUtils.releaseConnection(conn, ds);
        }
    }

    /**
     * ABORT / unexpected-exit hook — rollback any pending save-block tx. Preserves the COBOL
     * invariant that uncommitted writes are NOT persisted when the program aborts: if a save-block
     * tx is still bound (writes happened but COBOL COMMIT was never reached because
     * ABORT/unexpected exit fired first), roll back + unbind + release the connection.
     */
    public void rollbackIfPending() {
        if (jdbcTemplate == null) {
            return;
        }
        javax.sql.DataSource ds = jdbcTemplate.getDataSource();
        if (ds == null) {
            return;
        }
        // Active outer (Spring-managed) tx owns the connection: don't unbind it —
        // instead mark it rollback-only so the framework rolls back at its boundary,
        // preserving the COBOL no-persist-on-abort invariant.
        if (org.springframework.transaction.support.TransactionSynchronizationManager
                .isActualTransactionActive()) {
            try {
                org.springframework.transaction.interceptor.TransactionAspectSupport
                        .currentTransactionStatus()
                        .setRollbackOnly();
            } catch (Exception ignored) {
            }
            return;
        }
        if (!org.springframework.transaction.support.TransactionSynchronizationManager.hasResource(
                ds)) {
            return;
        }
        org.springframework.jdbc.datasource.ConnectionHolder h =
                (org.springframework.jdbc.datasource.ConnectionHolder)
                        org.springframework.transaction.support.TransactionSynchronizationManager
                                .unbindResource(ds);
        java.sql.Connection conn = h.getConnection();
        try {
            if (!conn.getAutoCommit()) {
                conn.rollback();
            }
        } catch (java.sql.SQLException ignored) {
            /* connection unusable — Hikari discards on release below */
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (java.sql.SQLException ignored) {
            }
            org.springframework.jdbc.datasource.DataSourceUtils.releaseConnection(conn, ds);
        }
    }

    public void closeAll() {
        files.forEach(RawDatasetBase::close);
    }

    @Override
    public void close() {
        closeAll();
    }
}

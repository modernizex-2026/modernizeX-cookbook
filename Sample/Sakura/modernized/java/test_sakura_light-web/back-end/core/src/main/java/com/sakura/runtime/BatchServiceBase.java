package com.sakura.runtime;

import com.sakura.runtime.io.AbstractDatasets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * Base class for all generated program services. Centralizes the run() lifecycle:
 * StopRun/Abort/Exception handling, fileSet.commit() at normal end (for programs without an
 * explicit COBOL COMMIT), fileSet.rollbackIfPending() on every error path (uncommitted writes must
 * not persist when the program aborts), and safeCloseAllFiles + closeDao in finally.
 *
 * <p>Transaction model: per-save-block, managed by RawDatasetBase.ensureSaveTxBound (lazy-binds a
 * connection to TSM with autoCommit=false on the first write) +
 * fileSet.commit()/rollbackIfPending() (unbinds + commits/rolls back). Spring's
 * {@code @Transactional} is NOT applied at this level — it would conflict with the manual TSM
 * lifecycle (Spring tries to commit the bound resource at @Transactional method exit, but our code
 * already unbinds it via fileSet.commit). The same model serves every execution path uniformly.
 *
 * <p>Subclasses implement: mainProcess(), getFileSet(), getCompletionCode(),
 * setCompletionCode(int). Oracle programs may override closeDao() to release their DAO's JDBC
 * connection in the finally block.
 */
public abstract class BatchServiceBase implements ScreenRendererAware {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected ScreenRendererInstance renderer;

    @Override
    public void setRenderer(ScreenRendererInstance renderer) {
        this.renderer = renderer;
    }

    public void execute() {
        try {
            mainProcess();
            // Commit at normal exit so a program's pending single write persists.
            // No-op for programs with no file-mode SQL writes.
            if (getFileSet() != null) {
                getFileSet().commit();
            }
        } catch (ProgramExitSignal eps) {
            // EXIT PROGRAM / GOBACK reached mid-flow — normal program termination.
            // Commit pending work and preserve the completion code already set.
            log.info("Program exited: {}", eps.getMessage());
            if (getFileSet() != null) {
                getFileSet().commit();
            }
        } catch (StopRunSignal sre) {
            // STOP RUN — normal COBOL program termination
            log.info("Program terminated: {}", sre.getMessage());
            setRollbackOnlyIfActive();
            if (getFileSet() != null) {
                getFileSet().rollbackIfPending();
            }
        } catch (JobAbortException ae) {
            // STOP ABORT — abnormal termination; uncommitted writes must not persist.
            log.error("Program aborted: {}", ae.getMessage());
            setCompletionCode(255);
            setRollbackOnlyIfActive();
            if (getFileSet() != null) {
                getFileSet().rollbackIfPending();
            }
        } catch (Exception e) {
            log.error("Batch processing failed", e);
            setCompletionCode(12);
            if (getFileSet() != null) {
                getFileSet().rollbackIfPending();
            }
            throw new RuntimeException("Batch processing failed: " + e.getMessage(), e);
        } finally {
            // Any path that bypassed commit/rollback above leaves the pending tx rolled back.
            if (getFileSet() != null) {
                getFileSet().rollbackIfPending();
            }
            safeCloseAllFiles();
            closeDao();
        }
    }

    /** Best-effort close of all files in fileSet. Called from run() finally block. */
    private void safeCloseAllFiles() {
        if (getFileSet() != null) {
            getFileSet().closeAll();
        }
    }

    /**
     * Defensive no-op under the unified per-save-block tx model (no class-level
     * {@code @Transactional} on this base — see class Javadoc). Kept so that if a subclass or
     * wrapper re-introduces a Spring-managed tx (e.g. an outer {@code @Transactional} on a Tasklet
     * method that calls into execute()), the rollback flag still propagates on error paths.
     * Programmatic rollback is always performed via fileSet.rollbackIfPending() above.
     */
    private void setRollbackOnlyIfActive() {
        try {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } catch (Exception ignored) {
            // No active Spring tx — common case. Programmatic rollback handles tx state.
        }
    }

    /**
     * Hook for Oracle programs to close their DAO's JDBC connection in finally. Default is no-op
     * for non-Oracle programs. Idempotent: subclasses' override should be safe to call when no
     * connection is open.
     */
    protected void closeDao() {
        // No-op default; Oracle services override to release DAO connection.
    }

    /**
     * Run nested subprogram logic with COBOL exception propagation semantics: StopRun/Abort
     * propagate to the caller's {@code run()}, other exceptions wrap.
     *
     * <p>Auto-commits pending writes on the success path: a CALL is an atomic unit of work in
     * COBOL, so audit-log / file SQL writes bound to TSM must be durable before control returns to
     * the caller. No-op when no fileSet is bound or no writes are pending.
     */
    public void executeSubprogram(Runnable subprogramLogic) {
        try {
            subprogramLogic.run();
            if (getFileSet() != null) {
                getFileSet().commit();
            }
        } catch (ProgramExitSignal eps) {
            // EXIT PROGRAM / GOBACK in a CALLed subprogram — return to the caller
            // (do NOT propagate: that is STOP RUN's job). Commit the CALL's unit of
            // work like the normal-return path above, then fall through to return.
            log.info("Subprogram returned via EXIT PROGRAM: {}", eps.getMessage());
            if (getFileSet() != null) {
                getFileSet().commit();
            }
        } catch (StopRunSignal sre) {
            log.info("Subprogram propagating STOP RUN: {}", sre.getMessage());
            throw sre;
        } catch (JobAbortException ae) {
            throw ae;
        } catch (Exception e) {
            log.error("Subprogram failed", e);
            throw new RuntimeException("Subprogram failed: " + e.getMessage(), e);
        }
    }

    /**
     * Top-level paragraph execution dispatcher. Catches {@link ParagraphJumpSignal} thrown by
     * cross-paragraph GO TO and runs the target {@code Runnable} (typically a method reference to
     * another paragraph). Stack stays flat regardless of GO TO chain depth.
     *
     * <p>Shared by all generated services: each calls {@code runChain(this::firstParagraph)} from
     * mainProcess().
     */
    protected void runChain(Runnable entry) {
        Runnable current = entry;
        while (current != null) {
            try {
                current.run();
                current = null;
            } catch (ParagraphJumpSignal g) {
                current = g.target;
            }
        }
    }

    protected abstract void mainProcess();

    protected abstract AbstractDatasets getFileSet();

    public abstract int getCompletionCode();

    protected abstract void setCompletionCode(int code);
}

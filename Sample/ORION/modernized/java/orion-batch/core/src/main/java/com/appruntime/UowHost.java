package com.appruntime;

/**
 * The one thing {@link MqRunner} needs from its host: a place to hang work on the unit of work's
 * outcome (CICS syncpoint — MQPUT is visible only after COMMIT, dropped on back-out).
 *
 * <p>Extracted from {@link AppRunner} so the MQ runtime can be embedded in a BATCH cohort, which
 * has no CICS task runner at all: there the host is simply absent and MqRunner sends immediately
 * (its pre-existing standalone behaviour).
 */
public interface UowHost {
    /**
     * Run {@code onCommit} when the current UOW commits, {@code onRollback} when it backs out; with
     * no UOW in flight the commit action runs immediately. Either may be null.
     */
    void registerUowCompletion(Runnable onCommit, Runnable onRollback);
}

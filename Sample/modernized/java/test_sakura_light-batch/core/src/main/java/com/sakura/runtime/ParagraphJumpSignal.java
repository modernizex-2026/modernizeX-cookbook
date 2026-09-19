package com.sakura.runtime;

/**
 * Models COBOL GO TO as a one-way control transfer.
 *
 * <p>Thrown by a paragraph body to signal execution should jump to the target. The target is a
 * {@link Runnable} (typically a method reference like {@code this::acpZ110}). The Service's
 * runChain dispatcher catches the signal and runs the target.
 *
 * <p>fillInStackTrace overridden — GO TO is a hot path; stack traces add overhead.
 */
public final class ParagraphJumpSignal extends RuntimeException {
    public final Runnable target;

    public ParagraphJumpSignal(Runnable target) {
        this.target = target;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}

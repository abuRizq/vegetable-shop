package com.veggieshop.core.tracing;

import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureTask;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * ThreadPoolTaskExecutor that propagates MDC (and optionally Spring SecurityContext)
 * from the submitting thread to the worker thread, and restores previous contexts afterwards.
 *
 * Key traits:
 * - Safe MDC snapshot & restore around every task (Runnable/Callable).
 * - Optional propagation of Spring SecurityContext (toggle via constructor).
 * - Works alongside an external TaskDecorator (double-wrapping is safe).
 * - Restores previous worker-thread contexts in a finally block (no leaks).
 * - No reliance on vendor tracing libs; pure MDC + optional Spring Security.
 *
 * Usage:
 *   MdcThreadPoolTaskExecutor ex = new MdcThreadPoolTaskExecutor(true);
 *   ex.setThreadNamePrefix("async-");
 *   ex.initialize();
 */
public class MdcThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

    private final boolean propagateSecurityContext;

    public MdcThreadPoolTaskExecutor() {
        this(true);
    }

    public MdcThreadPoolTaskExecutor(boolean propagateSecurityContext) {
        this.propagateSecurityContext = propagateSecurityContext;
    }

    // ---------------------------------------------------------------------
    // Wrapping (MDC + optional SecurityContext)
    // ---------------------------------------------------------------------

    @Override
    public void execute(@NonNull Runnable task) {
        super.execute(wrap(task));
    }

    @Override
    public void execute(@NonNull Runnable task, long startTimeout) {
        super.execute(wrap(task), startTimeout);
    }

    @Override
    public Future<?> submit(@NonNull Runnable task) {
        return super.submit(wrap(task));
    }

    @Override
    public <T> Future<T> submit(@NonNull Callable<T> task) {
        return super.submit(wrap(task));
    }

    @Override
    public ListenableFuture<?> submitListenable(@NonNull Runnable task) {
        return super.submitListenable(wrap(task));
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(@NonNull Callable<T> task) {
        return super.submitListenable(wrap(task));
    }

    /**
     * Wrap a Runnable with context snapshotting and restoration.
     */
    @NonNull
    private Runnable wrap(@NonNull Runnable delegate) {
        final ContextSnapshot snapshot = ContextSnapshot.capture(propagateSecurityContext);
        return () -> {
            ContextSnapshot previous = null;
            try {
                previous = ContextSnapshot.apply(snapshot);
                delegate.run();
            } finally {
                ContextSnapshot.restore(previous);
            }
        };
    }

    /**
     * Wrap a Callable with context snapshotting and restoration.
     */
    @NonNull
    private <T> Callable<T> wrap(@NonNull Callable<T> delegate) {
        final ContextSnapshot snapshot = ContextSnapshot.capture(propagateSecurityContext);
        return () -> {
            ContextSnapshot previous = null;
            try {
                previous = ContextSnapshot.apply(snapshot);
                return delegate.call();
            } finally {
                ContextSnapshot.restore(previous);
            }
        };
    }

    // ---------------------------------------------------------------------
    // Utilities
    // ---------------------------------------------------------------------

    /**
     * A minimal, allocation-conscious context snapshot:
     * - Captures MDC map (may be null)
     * - Optionally captures Spring SecurityContext (when available on classpath)
     */
    static final class ContextSnapshot {
        @Nullable
        final Map<String, String> mdc; // MDC is a mutable ThreadLocal map, copy it
        @Nullable
        final Object securityContext;  // type-erased to avoid hard dependency at init time
        final boolean hasSecurity;

        private ContextSnapshot(@Nullable Map<String, String> mdc,
                                @Nullable Object securityContext,
                                boolean hasSecurity) {
            this.mdc = mdc;
            this.securityContext = securityContext;
            this.hasSecurity = hasSecurity;
        }

        /**
         * Capture current thread contexts.
         */
        static ContextSnapshot capture(boolean includeSecurity) {
            Map<String, String> copy = MDC.getCopyOfContextMap();

            Object sec = null;
            boolean hasSec = false;

            if (includeSecurity) {
                // We try to interact with Spring Security via reflection to remain resilient
                // if security is not on the classpath in certain environments/tests.
                try {
                    Class<?> holder = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
                    Object ctx = holder.getMethod("getContext").invoke(null);
                    sec = ctx; // may be empty
                    hasSec = true;
                } catch (ClassNotFoundException e) {
                    // Spring Security not present -> ignore
                } catch (Throwable t) {
                    // Any reflective issue -> do not propagate security context
                    hasSec = false;
                    sec = null;
                }
            }

            return new ContextSnapshot(copy, sec, hasSec);
        }

        /**
         * Apply the snapshot to the current worker thread, returning the previous contexts.
         */
        static ContextSnapshot apply(ContextSnapshot snapshot) {
            // Save previous
            Map<String, String> prevMdc = MDC.getCopyOfContextMap();
            Object prevSec = null;
            boolean hadSec = false;

            // Apply MDC (set or clear)
            if (snapshot.mdc != null) MDC.setContextMap(snapshot.mdc);
            else MDC.clear();

            // Apply SecurityContext if we have one and security is present
            if (snapshot.hasSecurity) {
                try {
                    Class<?> holder = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
                    Object existing = holder.getMethod("getContext").invoke(null);
                    prevSec = existing;
                    hadSec = true;

                    // If snapshot.securityContext is null, set an empty context instead of null
                    Object toSet = snapshot.securityContext != null
                            ? snapshot.securityContext
                            : holder.getMethod("createEmptyContext").invoke(null);

                    holder.getMethod("setContext", Class.forName("org.springframework.security.core.context.SecurityContext"))
                            .invoke(null, toSet);
                } catch (Throwable t) {
                    // Swallow: we simply won't restore security context if reflection fails
                    hadSec = false;
                    prevSec = null;
                }
            }

            return new ContextSnapshot(prevMdc, prevSec, hadSec);
        }

        /**
         * Restore previous contexts into the current thread.
         */
        static void restore(@Nullable ContextSnapshot previous) {
            if (previous == null) {
                // Nothing to restore: clear MDC and leave security as-is
                MDC.clear();
                return;
            }

            // Restore MDC
            if (previous.mdc != null) MDC.setContextMap(previous.mdc);
            else MDC.clear();

            // Restore SecurityContext if previously present
            if (previous.hasSecurity) {
                try {
                    Class<?> holder = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
                    if (previous.securityContext != null) {
                        holder.getMethod("setContext", Class.forName("org.springframework.security.core.context.SecurityContext"))
                                .invoke(null, previous.securityContext);
                    } else {
                        holder.getMethod("clearContext").invoke(null);
                    }
                } catch (Throwable t) {
                    // Ignore restoration failures to avoid masking the original task error
                }
            }
        }
    }

    // ---------------------------------------------------------------------
    // Optional helpers for custom task creation (rarely needed)
    // ---------------------------------------------------------------------

    /**
     * Create a wrapped FutureTask if you need manual task creation with propagation.
     */
    public <T> FutureTask<T> newWrappedFutureTask(Callable<T> callable) {
        return new FutureTask<>(wrap(callable));
    }

    public FutureTask<?> newWrappedFutureTask(Runnable runnable, @Nullable Object result) {
        return new FutureTask<>(wrap(runnable), result);
    }

    public <T> ListenableFutureTask<T> newWrappedListenableTask(Callable<T> callable) {
        return new ListenableFutureTask<>(wrap(callable));
    }

    public ListenableFutureTask<?> newWrappedListenableTask(Runnable runnable, @Nullable Object result) {
        return new ListenableFutureTask<>(wrap(runnable), result);
    }
}

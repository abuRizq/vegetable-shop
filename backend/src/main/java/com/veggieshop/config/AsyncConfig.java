package com.veggieshop.config;

import com.veggieshop.core.tracing.MdcThreadPoolTaskExecutor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Enterprise-grade async & scheduling configuration.
 *
 * Key traits:
 * - Single, primary AsyncTaskExecutor with MDC (and optional SecurityContext) propagation.
 * - Dedicated ThreadPoolTaskScheduler with MDC propagation for @Scheduled tasks.
 * - Micrometer metrics binding for both executor and scheduler.
 * - Graceful shutdown and robust rejection policy (CallerRuns by default).
 * - Clean separation between raw scheduler and the MDC-decorating facade.
 *
 * Notes:
 * - Keep spring-boot-starter-aop on the classpath to enable @Async proxies.
 * - You can toggle sizes/timeouts via 'app.async.*' properties.
 */
@Configuration(proxyBeanMethods = true) // ensure @Bean method calls return the managed singletons
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(AsyncConfig.AsyncProps.class)
public class AsyncConfig implements AsyncConfigurer, SchedulingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    private final AsyncProps props;
    private final ObjectProvider<MeterRegistry> meterRegistryProvider;

    public AsyncConfig(AsyncProps props, ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this.props = props;
        this.meterRegistryProvider = meterRegistryProvider;
    }

    // ---------------------------------------------------------------------
    // @Async executor
    // ---------------------------------------------------------------------

    /**
     * Primary application executor used by @Async methods.
     * Propagates MDC and (optionally) Spring SecurityContext.
     */
    @Bean(name = {"applicationTaskExecutor", "mdcExecutor"})
    @Primary
    public AsyncTaskExecutor applicationTaskExecutor(TaskDecorator mdcTaskDecorator) {
        MdcThreadPoolTaskExecutor ex = new MdcThreadPoolTaskExecutor(props.isPropagateSecurityContext());
        ex.setThreadNamePrefix(props.getThreadNamePrefix());
        ex.setCorePoolSize(props.getCorePoolSize());
        ex.setMaxPoolSize(props.getMaxPoolSize());
        ex.setQueueCapacity(props.getQueueCapacity());
        ex.setKeepAliveSeconds((int) props.getKeepAlive().toSeconds());
        ex.setAllowCoreThreadTimeOut(props.isAllowCoreThreadTimeout());
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds((int) props.getShutdownAwait().toSeconds());
        ex.setTaskDecorator(mdcTaskDecorator);
        ex.setRejectedExecutionHandler(rejectionPolicy(props.getRejectionPolicy()));
        ex.initialize();

        // Bind Micrometer metrics on the underlying ThreadPoolExecutor
        meterRegistryProvider.ifAvailable(reg -> {
            ThreadPoolExecutor tpe = ex.getThreadPoolExecutor();
            if (tpe != null) {
                new ExecutorServiceMetrics(tpe, props.getMetricsName(),
                        Tags.of("executor", "async", "pool", props.getThreadNamePrefix()))
                        .bindTo(reg);
            }
        });

        log.info("Async executor initialized: core={}, max={}, queue={}, keepAlive={}, prefix={}, secCtxPropagation={}, rejection={}",
                props.getCorePoolSize(),
                props.getMaxPoolSize(),
                props.getQueueCapacity(),
                props.getKeepAlive(),
                props.getThreadNamePrefix(),
                props.isPropagateSecurityContext(),
                props.getRejectionPolicy());

        return ex;
    }

    /** Map a simple string policy to a standard RejectedExecutionHandler. */
    private RejectedExecutionHandler rejectionPolicy(String policy) {
        String p = policy == null ? "" : policy.trim().toUpperCase();
        return switch (p) {
            case "ABORT" -> new ThreadPoolExecutor.AbortPolicy();
            case "DISCARD" -> new ThreadPoolExecutor.DiscardPolicy();
            case "DISCARD_OLDEST" -> new ThreadPoolExecutor.DiscardOldestPolicy();
            // CallerRuns provides natural backpressure on the submitting thread
            default -> new ThreadPoolExecutor.CallerRunsPolicy();
        };
    }

    @Override
    public Executor getAsyncExecutor() {
        // With proxyBeanMethods=true this returns the managed singleton (no duplicate instantiation)
        return (Executor) applicationTaskExecutor(mdcTaskDecorator());
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new LoggingAsyncExceptionHandler();
    }

    // ---------------------------------------------------------------------
    // @Scheduled scheduler (backed by ThreadPoolTaskScheduler)
    // ---------------------------------------------------------------------

    /**
     * Low-level scheduler bean (raw). We expose it separately so we can wrap it
     * with MDC propagation while still binding metrics to the underlying SES.
     */
    @Bean
    @ConditionalOnMissingBean(name = "rawTaskScheduler")
    public ThreadPoolTaskScheduler rawTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(props.getSchedulerPoolSize());
        scheduler.setThreadNamePrefix(props.getSchedulerThreadNamePrefix());
        scheduler.setAwaitTerminationSeconds((int) props.getShutdownAwait().toSeconds());
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setErrorHandler(t ->
                log.error("Uncaught error in scheduled task, traceId={}", MDC.get("traceId"), t));
        scheduler.initialize();

        // Bind Micrometer to the underlying ScheduledExecutorService
        meterRegistryProvider.ifAvailable(reg -> {
            ScheduledExecutorService ses = scheduler.getScheduledExecutor();
            if (ses != null) {
                new ExecutorServiceMetrics(ses, props.getMetricsName(),
                        Tags.of("executor", "scheduler", "pool", props.getSchedulerThreadNamePrefix()))
                        .bindTo(reg);
            }
        });

        log.info("Scheduler initialized: pool={}, prefix={}",
                props.getSchedulerPoolSize(), props.getSchedulerThreadNamePrefix());

        return scheduler;
    }

    /**
     * Public TaskScheduler that decorates all tasks with MDC snapshot.
     */
    @Bean
    @Primary
    public TaskScheduler applicationTaskScheduler(TaskDecorator mdcTaskDecorator,
                                                  ThreadPoolTaskScheduler rawTaskScheduler) {
        return new MdcDecoratingTaskScheduler(rawTaskScheduler, mdcTaskDecorator);
    }

    @Override
    public void configureTasks(@NonNull ScheduledTaskRegistrar registrar) {
        // Injecting the already-built beans prevents double instantiation if proxyBeanMethods=false
        registrar.setTaskScheduler(applicationTaskScheduler(mdcTaskDecorator(), rawTaskScheduler()));
    }

    // ---------------------------------------------------------------------
    // TaskDecorator for MDC (reusable, lightweight)
    // ---------------------------------------------------------------------

    /**
     * MDC TaskDecorator that snapshots MDC at submission time and restores it for execution.
     * Use when external executors/schedulers are used; our MdcThreadPoolTaskExecutor already wraps tasks internally.
     */
    @Bean
    @ConditionalOnMissingBean(name = "mdcTaskDecorator")
    public TaskDecorator mdcTaskDecorator() {
        return runnable -> {
            final Map<String, String> submitterMdc = MDC.getCopyOfContextMap();
            return () -> {
                Map<String, String> previous = MDC.getCopyOfContextMap();
                try {
                    if (submitterMdc != null) MDC.setContextMap(submitterMdc);
                    else MDC.clear();
                    runnable.run();
                } finally {
                    if (previous != null) MDC.setContextMap(previous);
                    else MDC.clear();
                }
            };
        };
    }

    // ---------------------------------------------------------------------
    // Properties
    // ---------------------------------------------------------------------

    @ConfigurationProperties(prefix = "app.async")
    public static class AsyncProps {
        /** Core pool for @Async. */
        private int corePoolSize = Math.max(4, Runtime.getRuntime().availableProcessors());
        /** Max pool for @Async (defaults to 4x CPUs or at least 16). */
        private int maxPoolSize = Math.max(16, Runtime.getRuntime().availableProcessors() * 4);
        /** Queue capacity for @Async. */
        private int queueCapacity = 1000;
        /** Keep-alive for non-core threads. */
        private Duration keepAlive = Duration.ofSeconds(60);
        /** Allow core threads to time out (off by default). */
        private boolean allowCoreThreadTimeout = false;
        /** Graceful shutdown await time. */
        private Duration shutdownAwait = Duration.ofSeconds(30);
        /** @Async thread name prefix. */
        private String threadNamePrefix = "async-";

        /** Scheduler pool size for @Scheduled. */
        private int schedulerPoolSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        /** @Scheduled thread name prefix. */
        private String schedulerThreadNamePrefix = "sched-";

        /** Propagate Spring SecurityContext. */
        private boolean propagateSecurityContext = true;
        /** Micrometer metric name. */
        private String metricsName = "app.executor";
        /** Rejection policy: CALLER_RUNS (default) | ABORT | DISCARD | DISCARD_OLDEST */
        private String rejectionPolicy = "CALLER_RUNS";

        // getters/setters
        public int getCorePoolSize() { return corePoolSize; }
        public void setCorePoolSize(int corePoolSize) { this.corePoolSize = corePoolSize; }

        public int getMaxPoolSize() { return maxPoolSize; }
        public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }

        public int getQueueCapacity() { return queueCapacity; }
        public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }

        public Duration getKeepAlive() { return keepAlive; }
        public void setKeepAlive(Duration keepAlive) { this.keepAlive = keepAlive; }

        public boolean isAllowCoreThreadTimeout() { return allowCoreThreadTimeout; }
        public void setAllowCoreThreadTimeout(boolean allowCoreThreadTimeout) { this.allowCoreThreadTimeout = allowCoreThreadTimeout; }

        public Duration getShutdownAwait() { return shutdownAwait; }
        public void setShutdownAwait(Duration shutdownAwait) { this.shutdownAwait = shutdownAwait; }

        public String getThreadNamePrefix() { return threadNamePrefix; }
        public void setThreadNamePrefix(String threadNamePrefix) { this.threadNamePrefix = threadNamePrefix; }

        public int getSchedulerPoolSize() { return schedulerPoolSize; }
        public void setSchedulerPoolSize(int schedulerPoolSize) { this.schedulerPoolSize = schedulerPoolSize; }

        public String getSchedulerThreadNamePrefix() { return schedulerThreadNamePrefix; }
        public void setSchedulerThreadNamePrefix(String schedulerThreadNamePrefix) { this.schedulerThreadNamePrefix = schedulerThreadNamePrefix; }

        public boolean isPropagateSecurityContext() { return propagateSecurityContext; }
        public void setPropagateSecurityContext(boolean propagateSecurityContext) { this.propagateSecurityContext = propagateSecurityContext; }

        public String getMetricsName() { return metricsName; }
        public void setMetricsName(String metricsName) { this.metricsName = metricsName; }

        public String getRejectionPolicy() { return rejectionPolicy; }
        public void setRejectionPolicy(String rejectionPolicy) { this.rejectionPolicy = rejectionPolicy; }
    }

    // ---------------------------------------------------------------------
    // Async exception handler
    // ---------------------------------------------------------------------

    /**
     * Logs uncaught exceptions thrown from @Async void methods (non-Future).
     * For CompletableFuture or Future-based methods, exceptions surface on join/get.
     */
    static final class LoggingAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        private static final Logger logger = LoggerFactory.getLogger(LoggingAsyncExceptionHandler.class);

        @Override
        public void handleUncaughtException(@NonNull Throwable ex,
                                            @NonNull Method method,
                                            @Nullable Object... params) {
            logger.error("Uncaught @Async exception. method={}#{} args={} traceId={}",
                    method.getDeclaringClass().getSimpleName(),
                    method.getName(),
                    abbreviateArgs(params),
                    MDC.get("traceId"),
                    ex);
        }

        private String abbreviateArgs(@Nullable Object[] params) {
            try {
                if (params == null) return "[]";
                int n = Math.min(params.length, 5);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < n; i++) {
                    sb.append(String.valueOf(params[i]));
                    if (i < n - 1) sb.append(", ");
                }
                if (params.length > n) sb.append(" ...").append(params.length - n).append(" more");
                sb.append(']');
                return sb.toString();
            } catch (Exception e) {
                return "[unavailable]";
            }
        }
    }

    // ---------------------------------------------------------------------
    // Wrapper scheduler that decorates tasks with MDC (applies TaskDecorator)
    // ---------------------------------------------------------------------

    /**
     * TaskScheduler that applies a TaskDecorator (MDC snapshot) to every scheduled task.
     * This complements the raw ThreadPoolTaskScheduler to achieve MDC propagation.
     */
    static final class MdcDecoratingTaskScheduler implements TaskScheduler {
        private final TaskScheduler delegate;
        private final TaskDecorator decorator;

        MdcDecoratingTaskScheduler(TaskScheduler delegate, TaskDecorator decorator) {
            this.delegate = delegate;
            this.decorator = decorator;
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable task, org.springframework.scheduling.Trigger trigger) {
            return delegate.schedule(decorator.decorate(task), trigger);
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable task, java.util.Date startTime) {
            return delegate.schedule(decorator.decorate(task), startTime);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, java.util.Date startTime, long period) {
            return delegate.scheduleAtFixedRate(decorator.decorate(task), startTime, period);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
            return delegate.scheduleAtFixedRate(decorator.decorate(task), period);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, java.util.Date startTime, long delay) {
            return delegate.scheduleWithFixedDelay(decorator.decorate(task), startTime, delay);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
            return delegate.scheduleWithFixedDelay(decorator.decorate(task), delay);
        }

        // Spring 6+ Instant/Duration overloads (if available in your target version)
        @Override
        public ScheduledFuture<?> schedule(Runnable task, java.time.Instant startTime) {
            return delegate.schedule(decorator.decorate(task), startTime);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, java.time.Instant startTime, java.time.Duration period) {
            return delegate.scheduleAtFixedRate(decorator.decorate(task), startTime, period);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, java.time.Duration period) {
            return delegate.scheduleAtFixedRate(decorator.decorate(task), period);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, java.time.Instant startTime, java.time.Duration delay) {
            return delegate.scheduleWithFixedDelay(decorator.decorate(task), startTime, delay);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, java.time.Duration delay) {
            return delegate.scheduleWithFixedDelay(decorator.decorate(task), delay);
        }
    }
}

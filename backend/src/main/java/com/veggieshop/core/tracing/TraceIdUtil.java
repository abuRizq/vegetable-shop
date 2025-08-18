package com.veggieshop.core.exception;

import org.slf4j.MDC;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Tiny helper to read/write the current trace id from MDC and expose common header keys.
 * Centralizing this avoids repeating MDC key strings across classes.
 */
public final class TraceIdUtil {

    /** MDC key used across the app for the trace correlation id. */
    public static final String MDC_TRACE_ID_KEY = "traceId";

    /** Common HTTP headers for trace propagation. */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACEPARENT_HEADER = "traceparent";

    private TraceIdUtil() { }

    /**
     * @return the current trace id from MDC, or null if missing/blank.
     */
    @Nullable
    public static String currentTraceId() {
        String v = MDC.get(MDC_TRACE_ID_KEY);
        return (StringUtils.hasText(v)) ? v : null;
    }

    /**
     * Puts a trace id into MDC (null/blank clears).
     */
    public static void setTraceId(@Nullable String traceId) {
        if (!StringUtils.hasText(traceId)) {
            MDC.remove(MDC_TRACE_ID_KEY);
        } else {
            MDC.put(MDC_TRACE_ID_KEY, traceId.trim());
        }
    }

    /**
     * Removes the trace id from MDC (if present).
     */
    public static void clearTraceId() {
        MDC.remove(MDC_TRACE_ID_KEY);
    }
}

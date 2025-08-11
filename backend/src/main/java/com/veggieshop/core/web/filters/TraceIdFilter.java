// src/main/java/com/veggieshop/core/web/filters/TraceIdFilter.java
package com.veggieshop.core.web.filters;

import com.veggieshop.core.exception.TraceIdUtil;
import com.veggieshop.core.tracing.TraceIdGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Single point for trace id & W3C traceparent header management.
 * - Sets X-Trace-Id BEFORE the chain to survive early commits.
 * - Normalizes/builds traceparent AFTER the chain.
 * - Uses TraceIdUtil.MDC_TRACE_ID_KEY for MDC, and TraceIdGenerator utilities.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACEPARENT_HEADER = "traceparent";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String traceId = extractOrGenerateTraceId(request);

        // Put into MDC using the unified key
        MDC.put(TraceIdUtil.MDC_TRACE_ID_KEY, traceId);

        // Ensure X-Trace-Id is present even if response commits early
        setHeaderIfPresentAndChanged(response, TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Normalize/build a valid W3C traceparent for the response
            String incoming = request.getHeader(TRACEPARENT_HEADER);
            String outTraceparent = buildOrNormalizeTraceparent(traceId, incoming);
            setHeaderIfPresentAndChanged(response, TRACEPARENT_HEADER, outTraceparent);

            MDC.remove(TraceIdUtil.MDC_TRACE_ID_KEY);
        }
    }

    /** Try common formats; otherwise generate compact ID. */
    private String extractOrGenerateTraceId(HttpServletRequest request) {
        String traceparent = request.getHeader(TRACEPARENT_HEADER);
        String b3 = request.getHeader("b3");
        String xTraceId = request.getHeader(TRACE_ID_HEADER);
        String xRequestId = request.getHeader("X-Request-Id");
        String xCorrelationId = request.getHeader("X-Correlation-Id");
        String xAmznTraceId = request.getHeader("X-Amzn-Trace-Id");

        String resolved = TraceIdGenerator.resolveOrGenerate(
                traceparent, b3, xTraceId, xRequestId, xCorrelationId, xAmznTraceId
        );
        String normalized = TraceIdGenerator.normalizeToken(resolved);
        return (normalized != null) ? normalized : TraceIdGenerator.newW3CTraceIdHex32();
    }

    /** Keep a valid incoming traceparent; otherwise build a minimal compliant one. */
    private String buildOrNormalizeTraceparent(String traceId, @Nullable String incoming) {
        if (StringUtils.hasText(incoming) && TraceIdGenerator.parseTraceParent(incoming).isPresent()) {
            return sanitizeHeaderValue(incoming);
        }
        String w3cTraceId = TraceIdGenerator.isValidW3CTraceId(traceId)
                ? traceId
                : TraceIdGenerator.newW3CTraceIdHex32();
        String parentId = TraceIdGenerator.newW3CSpanIdHex16();
        return TraceIdGenerator.buildTraceParent(w3cTraceId, parentId, true);
    }

    private void setHeaderIfPresentAndChanged(HttpServletResponse response, String name, @Nullable String value) {
        if (!StringUtils.hasText(value)) return;
        String sanitized = sanitizeHeaderValue(value);
        String existing = response.getHeader(name);
        if (!sanitized.equals(existing)) {
            response.setHeader(name, sanitized);
        }
    }

    private String sanitizeHeaderValue(String value) {
        // Strip CR/LF per RFC; trim spaces
        return value.replaceAll("[\\r\\n]", "").trim();
    }
}

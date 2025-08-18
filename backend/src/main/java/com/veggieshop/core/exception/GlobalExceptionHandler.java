package com.veggieshop.core.exception;

import com.veggieshop.config.ErrorProps;
import com.veggieshop.core.tracing.TraceIdUtil;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Global exception translator:
 * - Negotiates ProblemDetail vs ApiResponse based on query/header + ErrorProps.defaultFormat.
 * - Delegates mapping & building to ProblemDetails / ErrorResponseFactory.
 * - Centralizes logging: WARN for 4xx (no stacktrace), ERROR for 5xx (with stacktrace).
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.veggieshop")
public class GlobalExceptionHandler {

    private static final String PROBLEM_JSON = "application/problem+json";
    private static final String MDC_TRACE_ID_KEY = TraceIdUtil.MDC_TRACE_ID_KEY;

    private final ProblemDetails problemDetails;       // RFC 7807 builder
    private final ErrorResponseFactory errorResponses; // ApiResponse builder
    private final ErrorProps errorProps;               // default format fallback

    public GlobalExceptionHandler(ProblemDetails problemDetails,
                                  ErrorResponseFactory errorResponses,
                                  ErrorProps errorProps) {
        this.problemDetails = problemDetails;
        this.errorResponses = errorResponses;
        this.errorProps = errorProps;
    }

    // =============================================================================================
    // Validation (@Valid body / binding)
    // =============================================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(HttpServletRequest req, MethodArgumentNotValidException ex) {
        Instant now = Instant.now();
        BindingResult br = ex.getBindingResult();

        ResponseEntity<?> resp = wantsProblem(req)
                ? problemDetails.fromBindingErrors(req, br, now)
                : errorResponses.fromBindingResult(req, br, now);

        logByStatus(currentTraceId(), resp.getStatusCode().value(),
                "Validation failed (MethodArgumentNotValid) — " + br.getErrorCount() + " error(s)", ex);
        return resp;
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> handleBindException(HttpServletRequest req, BindException ex) {
        Instant now = Instant.now();
        BindingResult br = ex.getBindingResult();

        ResponseEntity<?> resp = wantsProblem(req)
                ? problemDetails.fromBindingErrors(req, br, now)
                : errorResponses.fromBindingResult(req, br, now);

        logByStatus(currentTraceId(), resp.getStatusCode().value(),
                "Validation failed (BindException) — " + ex.getErrorCount() + " error(s)", ex);
        return resp;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(HttpServletRequest req, ConstraintViolationException ex) {
        return chooseAndLog(req, ex, "Validation failed (ConstraintViolation)");
    }

    // =============================================================================================
    // Common client errors
    // =============================================================================================

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class,
            MethodArgumentTypeMismatchException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            NoHandlerFoundException.class,
            ResponseStatusException.class,
            ErrorResponseException.class
    })
    public ResponseEntity<?> handleCommonClientErrors(HttpServletRequest req, Exception ex) {
        return chooseAndLog(req, ex, "Client error: " + ex.getClass().getSimpleName());
    }

    // =============================================================================================
    // Security
    // =============================================================================================

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthentication(HttpServletRequest req, AuthenticationException ex) {
        return chooseAndLog(req, ex, "Unauthenticated");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(HttpServletRequest req, AccessDeniedException ex) {
        return chooseAndLog(req, ex, "Forbidden");
    }

    // =============================================================================================
    // First-class ApplicationException
    // =============================================================================================

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<?> handleApplication(HttpServletRequest req, ApplicationException ex) {
        Instant now = Instant.now();
        ResponseEntity<?> resp = wantsProblem(req)
                ? problemDetails.fromException(req, ex, now)
                : errorResponses.fromException(req, ex, now);

        logByStatus(currentTraceId(), resp.getStatusCode().value(), "Application exception — " + ex, ex);
        return resp;
    }

    // =============================================================================================
    // Catch-all
    // =============================================================================================

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<?> handleAny(HttpServletRequest req, Throwable ex) {
        Instant now = Instant.now();
        ResponseEntity<?> resp = wantsProblem(req)
                ? problemDetails.fromException(req, ex, now)
                : errorResponses.fromException(req, ex, now);

        int status = resp.getStatusCode().value();
        logByStatus(currentTraceId(), status, "Unhandled throwable mapped to " + status + " — " + ex, ex);
        return resp;
    }

    // =============================================================================================
    // Helpers (negotiation + logging + trace)
    // =============================================================================================

    /**
     * Content negotiation precedence:
     * 1) ?format=problem → ProblemDetail
     * 2) ?format=api     → ApiResponse
     * 3) Accept: application/problem+json vs application/json — honor q-values if present
     * 4) fallback to ErrorProps.defaultFormat() (api|problem), default is "api"
     */
    private boolean wantsProblem(HttpServletRequest request) {
        String format = Optional.ofNullable(request.getParameter("format"))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .orElse(null);
        if ("problem".equals(format)) return true;
        if ("api".equals(format)) return false;

        String accept = Optional.ofNullable(request.getHeader(HttpHeaders.ACCEPT)).orElse("");
        if (!accept.isBlank()) {
            MediaTypeChoice choice = parseAccept(accept);
            if (choice == MediaTypeChoice.PROBLEM) return true;
            if (choice == MediaTypeChoice.API) return false;
        }
        return "problem".equals(errorProps.defaultFormat());
    }

    /** Chooses the builder (ProblemDetail vs ApiResponse) and logs with the 4xx/5xx policy. */
    private ResponseEntity<?> chooseAndLog(HttpServletRequest req, Throwable ex, String msg) {
        Instant now = Instant.now();
        ResponseEntity<?> resp = wantsProblem(req)
                ? problemDetails.fromException(req, ex, now)
                : errorResponses.fromException(req, ex, now);
        logByStatus(currentTraceId(), resp.getStatusCode().value(), msg + " — " + ex, ex);
        return resp;
    }

    /** Pulls traceId from MDC via TraceIdUtil. */
    @Nullable
    private String currentTraceId() {
        String v = MDC.get(MDC_TRACE_ID_KEY);
        return (StringUtils.hasText(v)) ? v : null;
    }

    /** Logging policy: 4xx → WARN (no stacktrace); 5xx → ERROR (with stacktrace). */
    private void logByStatus(@Nullable String traceId, int status, String message, Throwable ex) {
        var logger = org.slf4j.LoggerFactory.getLogger(getClass());
        boolean isClientError = status >= 400 && status < 500;
        if (isClientError) {
            if (traceId == null) logger.warn("{}", message);
            else logger.warn("[traceId={}] {}", traceId, message);
        } else {
            if (traceId == null) logger.error("{}", message, ex);
            else logger.error("[traceId={}] {}", traceId, message, ex);
        }
    }

    // --- Accept parser (q-aware) ---------------------------------------------------------

    private enum MediaTypeChoice { PROBLEM, API, UNKNOWN }
    private static final Pattern TYPE_SPLIT = Pattern.compile("\\s*,\\s*");
    private static final Pattern PARAM_SPLIT = Pattern.compile("\\s*;\\s*");

    private MediaTypeChoice parseAccept(String header) {
        // Return the media type (problem vs api) with the highest q-value; ties prefer API (application/json)
        double bestQ = -1.0;
        MediaTypeChoice best = MediaTypeChoice.UNKNOWN;

        for (String part : TYPE_SPLIT.split(header)) {
            if (part.isBlank()) continue;
            String[] typeAndParams = PARAM_SPLIT.split(part);
            String type = typeAndParams[0].trim().toLowerCase(Locale.ROOT);
            double q = 1.0;
            for (int i = 1; i < typeAndParams.length; i++) {
                String p = typeAndParams[i];
                int eq = p.indexOf('=');
                if (eq > 0) {
                    String k = p.substring(0, eq).trim().toLowerCase(Locale.ROOT);
                    String v = p.substring(eq + 1).trim();
                    if ("q".equals(k)) {
                        try { q = Double.parseDouble(v); } catch (NumberFormatException ignored) { q = 1.0; }
                    }
                }
            }

            MediaTypeChoice current;
            if (type.equals(PROBLEM_JSON)) {
                current = MediaTypeChoice.PROBLEM;
            } else if (type.equals(MediaType.APPLICATION_JSON_VALUE) || type.endsWith("+json")) {
                // Treat generic JSON (and */*+json) as API envelope
                current = MediaTypeChoice.API;
            } else {
                continue;
            }

            if (q > bestQ || (q == bestQ && current == MediaTypeChoice.API && best == MediaTypeChoice.PROBLEM)) {
                bestQ = q;
                best = current;
            }
        }
        return best;
    }
}

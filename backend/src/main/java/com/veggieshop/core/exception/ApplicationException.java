package com.veggieshop.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Extensible base for domain/application exceptions.
 * - Stable fields: httpStatus, code, metadata (defensively copied).
 * - Safe message fallback/truncation (max 500 chars).
 * - Functional enrichers (withMeta/withCause) that preserve the concrete subtype
 *   via abstract newInstance(...).
 */
public abstract class ApplicationException extends RuntimeException implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private final HttpStatus httpStatus;
    private final String code;
    private final Map<String, Object> metadata;
    private final Instant timestamp;

    // ---------------- Constructors ----------------

    protected ApplicationException(HttpStatus status, String code, String message) {
        this(status, code, message, null, Collections.emptyMap());
    }

    protected ApplicationException(HttpStatus status, String code, String message, @Nullable Throwable cause) {
        this(status, code, message, cause, Collections.emptyMap());
    }

    protected ApplicationException(HttpStatus status,
                                   String code,
                                   String message,
                                   @Nullable Throwable cause,
                                   @Nullable Map<String, Object> metadata) {
        super(safeMessage(message, defaultReason(status)), cause);
        this.httpStatus = Objects.requireNonNull(status, "httpStatus");
        this.code = requireCode(code);
        this.metadata = copyUnmodifiable(metadata);
        this.timestamp = Instant.now();
    }

    // ------------- Accessors (used by mapping) -------------

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getCode()           { return code; }
    public Map<String, Object> getMetadata() { return metadata; }
    public Instant getTimestamp()     { return timestamp; }

    // ------------- Functional enrichers (typed copies) -------------

    /** Return a NEW instance of the same subtype with the given cause attached. */
    public final ApplicationException withCause(@Nullable Throwable cause) {
        return newInstance(httpStatus, code, getMessage(), cause, metadata);
    }

    /** Return a NEW instance of the same subtype with one extra meta entry. */
    public final ApplicationException withMeta(String key, Object value) {
        Objects.requireNonNull(key, "key");
        Map<String, Object> m = new LinkedHashMap<>(this.metadata);
        m.put(key, value);
        return newInstance(httpStatus, code, getMessage(), getCause(), Collections.unmodifiableMap(m));
    }

    /** Return a NEW instance of the same subtype with merged metadata. */
    public final ApplicationException withMeta(@Nullable Map<String, ?> extra) {
        if (extra == null || extra.isEmpty()) return this;
        Map<String, Object> m = new LinkedHashMap<>(this.metadata);
        extra.forEach((k, v) -> m.put(String.valueOf(k), v));
        return newInstance(httpStatus, code, getMessage(), getCause(), Collections.unmodifiableMap(m));
    }

    /**
     * Subclasses must implement a typed copy-constructor factory.
     * MUST respect all arguments (status/code/message/cause/metadata).
     */
    public abstract ApplicationException newInstance(HttpStatus status,
                                                     String code,
                                                     String message,
                                                     @Nullable Throwable cause,
                                                     @Nullable Map<String, Object> metadata);

    // ---------------- Internals ----------------

    private static String requireCode(String code) {
        if (!StringUtils.hasText(code)) throw new IllegalArgumentException("code must not be blank");
        return code.trim();
    }

    private static String safeMessage(@Nullable String message, String fallback) {
        if (!StringUtils.hasText(message)) return fallback;
        String t = message.trim();
        return (t.length() > 500) ? t.substring(0, 500) + "..." : t;
    }

    private static String defaultReason(HttpStatus status) {
        return (status != null) ? status.getReasonPhrase() : HttpStatus.BAD_REQUEST.getReasonPhrase();
    }

    private static Map<String, Object> copyUnmodifiable(@Nullable Map<String, Object> src) {
        if (src == null || src.isEmpty()) return Collections.emptyMap();
        return Collections.unmodifiableMap(new LinkedHashMap<>(src));
    }

    @Override
    public String toString() {
        return "ApplicationException{" +
                "httpStatus=" + httpStatus +
                ", code='" + code + '\'' +
                ", message='" + getMessage() + '\'' +
                (getCause() != null ? ", cause=" + getCause().getClass().getSimpleName() : "") +
                (metadata.isEmpty() ? "" : ", meta=" + metadata) +
                ", timestamp=" + timestamp +
                '}';
    }
}

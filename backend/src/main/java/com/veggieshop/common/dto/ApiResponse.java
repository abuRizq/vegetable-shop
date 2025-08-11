// src/main/java/com/veggieshop/common/dto/ApiResponse.java
package com.veggieshop.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.veggieshop.core.validation.RejectedValueSanitizer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;

@Schema(name = "ApiResponse", description = "Standard API response envelope")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        @Schema(description = "Was the request successful?") @JsonProperty("success") boolean success,
        @Schema(description = "HTTP status code (e.g., 200, 400, 500)") @JsonProperty("status") int status,
        @Schema(description = "Stable, machine-friendly code (e.g., USER_NOT_FOUND)") @JsonProperty("code") String code,
        @Schema(description = "Human-readable message") @JsonProperty("message") String message,
        @Schema(description = "Response payload (if any)") @JsonProperty("data") T data,
        @Schema(description = "List of error details (if any)") @JsonProperty("errors") List<ApiError> errors,
        @Schema(description = "Additional metadata") @JsonProperty("meta") Map<String, Object> meta,
        @Schema(description = "Server-side timestamp (UTC)") @JsonProperty("timestamp") Instant timestamp,
        @Schema(description = "Request path (optional)") @JsonProperty("path") String path,
        @Schema(description = "Trace correlation id (if available)") @JsonProperty("traceId") String traceId,
        @Schema(description = "RFC7807 'type' URI equivalent for ApiResponse errors") @JsonProperty("problemType") String problemType
) implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    // --- Success factories ---
    public static <T> ApiResponse<T> ok(@Nullable T data) {
        return ApiResponse.<T>builder(HttpStatus.OK, true).message("OK").data(data).build();
    }
    public static <T> ApiResponse<T> created(@Nullable T data) {
        return ApiResponse.<T>builder(HttpStatus.CREATED, true).message("Created").data(data).build();
    }
    public static <T> ApiResponse<T> accepted(@Nullable T data) {
        return ApiResponse.<T>builder(HttpStatus.ACCEPTED, true).message("Accepted").data(data).build();
    }
    public static ApiResponse<Void> noContent() {
        return ApiResponse.<Void>builder(HttpStatus.NO_CONTENT, true).message("No Content").build();
    }
    public static ApiResponse<Void> ok()       { return ApiResponse.<Void>ok(null); }
    public static ApiResponse<Void> created()  { return ApiResponse.<Void>created(null); }
    public static ApiResponse<Void> accepted() { return ApiResponse.<Void>accepted(null); }

    // --- Error factories ---
    public static <T> ApiResponse<T> fail(HttpStatus status, String code, String message) {
        return ApiResponse.<T>builder(status, false).code(code).message(message).build();
    }
    public static <T> ApiResponse<T> fail(HttpStatus status, String code, String message, @Nullable String problemType) {
        return ApiResponse.<T>builder(status, false).code(code).message(message).problemType(problemType).build();
    }
    public static <T> ApiResponse<T> fail(HttpStatus status, String code, String message, List<ApiError> errors) {
        return ApiResponse.<T>builder(status, false).code(code).message(message).errors(errors).build();
    }
    public static <T> ApiResponse<T> fail(HttpStatus status, String code, String message, List<ApiError> errors, @Nullable String problemType) {
        return ApiResponse.<T>builder(status, false).code(code).message(message).errors(errors).problemType(problemType).build();
    }
    public static <T> ApiResponse<T> validationFailed(Collection<? extends ConstraintViolation<?>> violations) {
        List<ApiError> errs = (violations == null) ? List.of() : violations.stream().map(ApiError::from).toList();
        return ApiResponse.<T>builder(HttpStatus.UNPROCESSABLE_ENTITY, false)
                .code("VALIDATION_FAILED")
                .message("Validation failed")
                .errors(errs.isEmpty() ? null : errs)
                .problemType("https://httpstatuses.io/422")
                .build();
    }
    public static <T> ApiResponse<T> error(HttpStatus status, String code, @Nullable String message, @Nullable Throwable ex) {
        String finalMessage = (n(message) == null && ex != null) ? safeMessage(ex) : message;
        List<ApiError> errs = (ex == null) ? null : List.of(ApiError.of("exception", ex.getClass().getSimpleName(), null));
        return ApiResponse.<T>builder(status, false).code(code).message(finalMessage).errors(errs).build();
    }
    public static <T> ApiResponse<T> error(HttpStatus status, String code, @Nullable String message, @Nullable Throwable ex, @Nullable String problemType) {
        String finalMessage = (n(message) == null && ex != null) ? safeMessage(ex) : message;
        List<ApiError> errs = (ex == null) ? null : List.of(ApiError.of("exception", ex.getClass().getSimpleName(), null));
        return ApiResponse.<T>builder(status, false).code(code).message(finalMessage).errors(errs).problemType(problemType).build();
    }
    public static <T> ApiResponse<T> error(HttpStatus status, String code, Throwable ex) {
        return error(status, code, null, ex);
    }
    public static <T> ApiResponse<T> error(HttpStatus status, String code, String message) {
        return error(status, code, message, null);
    }

    // --- ResponseEntity helpers ---
    public ResponseEntity<ApiResponse<T>> toResponseEntity() { return ResponseEntity.status(status).body(this); }
    public static <T> ResponseEntity<ApiResponse<T>> okEntity(@Nullable T data) { return ApiResponse.<T>ok(data).toResponseEntity(); }
    public static <T> ResponseEntity<ApiResponse<T>> createdEntity(@Nullable T data) { return ApiResponse.<T>created(data).toResponseEntity(); }
    public static <T> ResponseEntity<ApiResponse<T>> acceptedEntity(@Nullable T data) { return ApiResponse.<T>accepted(data).toResponseEntity(); }
    public static ResponseEntity<ApiResponse<Void>> noContentEntity() { return ApiResponse.noContent().toResponseEntity(); }
    public static <T> ResponseEntity<ApiResponse<T>> failEntity(HttpStatus status, String code, String message) { return ApiResponse.<T>fail(status, code, message).toResponseEntity(); }
    public static <T> ResponseEntity<ApiResponse<T>> failEntity(HttpStatus status, String code, String message, List<ApiError> errors) { return ApiResponse.<T>fail(status, code, message, errors).toResponseEntity(); }
    public static <T> ResponseEntity<ApiResponse<T>> failEntity(HttpStatus status, String code, String message, List<ApiError> errors, @Nullable String problemType) { return ApiResponse.<T>fail(status, code, message, errors, problemType).toResponseEntity(); }
    public static <T> ResponseEntity<ApiResponse<T>> errorEntity(HttpStatus status, String code, String message, @Nullable Throwable ex) { return ApiResponse.<T>error(status, code, message, ex).toResponseEntity(); }
    public static <T> ResponseEntity<ApiResponse<T>> errorEntity(HttpStatus status, String code, String message, @Nullable Throwable ex, @Nullable String problemType) { return ApiResponse.<T>error(status, code, message, ex, problemType).toResponseEntity(); }

    // --- Withers ---
    public ApiResponse<T> withMeta(String key, Object value) {
        Objects.requireNonNull(key, "key");
        Map<String, Object> m = new LinkedHashMap<>(meta == null ? Map.of() : meta);
        m.put(key, value);
        return new ApiResponse<>(success, status, code, message, data, errors, Map.copyOf(m), timestamp, path, traceId, problemType);
    }
    public ApiResponse<T> withMeta(Map<String, ?> extra) {
        if (extra == null || extra.isEmpty()) return this;
        Map<String, Object> m = new LinkedHashMap<>(meta == null ? Map.of() : meta);
        extra.forEach((k, v) -> m.put(String.valueOf(k), v));
        return new ApiResponse<>(success, status, code, message, data, errors, Map.copyOf(m), timestamp, path, traceId, problemType);
    }
    public ApiResponse<T> withPath(String newPath) { return new ApiResponse<>(success, status, code, message, data, errors, meta, timestamp, n(newPath), traceId, problemType); }
    public ApiResponse<T> withTraceId(String newTraceId) { return new ApiResponse<>(success, status, code, message, data, errors, meta, timestamp, path, n(newTraceId), problemType); }
    public ApiResponse<T> withProblemType(@Nullable String newProblemType) { return new ApiResponse<>(success, status, code, message, data, errors, meta, timestamp, path, traceId, n(newProblemType)); }

    // --- Builder ---
    public static <T> Builder<T> builder(HttpStatus status, boolean success) { return new Builder<T>().status(status.value()).success(success); }

    public static final class Builder<T> {
        private boolean success;
        private int status = HttpStatus.OK.value();
        @Nullable private String code;
        @Nullable private String message;
        @Nullable private T data;
        @Nullable private List<ApiError> errors;
        @Nullable private Map<String, Object> meta;
        @Nullable private Instant timestamp = Instant.now();
        @Nullable private String path;
        @Nullable private String traceId;
        @Nullable private String problemType;

        public Builder<T> success(boolean v)          { this.success = v; return this; }
        public Builder<T> status(int v)               { this.status = v; return this; }
        public Builder<T> code(String v)              { this.code = v; return this; }
        public Builder<T> message(String v)           { this.message = v; return this; }
        public Builder<T> data(T v)                   { this.data = v; return this; }
        public Builder<T> errors(List<ApiError> v)    { this.errors = v; return this; }
        public Builder<T> meta(Map<String, Object> v) { this.meta = v; return this; }
        public Builder<T> timestamp(Instant v)        { this.timestamp = v; return this; }
        public Builder<T> path(String v)              { this.path = v; return this; }
        public Builder<T> traceId(String v)           { this.traceId = v; return this; }
        public Builder<T> problemType(String v)       { this.problemType = v; return this; }

        public ApiResponse<T> build() {
            List<ApiError> errs = (errors == null || errors.isEmpty()) ? null : List.copyOf(errors);
            Map<String, Object> m = (meta == null || meta.isEmpty()) ? null : Map.copyOf(meta);

            String finalMessage = n(message);
            if (finalMessage == null) {
                HttpStatus hs = HttpStatus.resolve(status);
                if (hs != null) {
                    finalMessage = hs.getReasonPhrase();
                } else if (status >= 200 && status < 300) {
                    finalMessage = "Success";
                } else if (status >= 400 && status < 600) {
                    finalMessage = "Error";
                } else {
                    finalMessage = "HTTP " + status;
                }
            }

            String finalCode = n(code);

            return new ApiResponse<>(
                    success,
                    status,
                    finalCode,
                    finalMessage,
                    data,
                    errs,
                    m,
                    (timestamp == null) ? Instant.now() : timestamp,
                    n(path),
                    n(traceId),
                    n(problemType)
            );
        }
    }

    // --- Nested DTO ---
    @Schema(name = "ApiError", description = "Single error detail")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ApiError(
            @Schema(description = "Field or domain area, e.g., 'email' or 'order'") @JsonProperty("field") String field,
            @Schema(description = "Human-readable error message") @JsonProperty("message") String message,
            @Schema(description = "Sanitized rejected value (truncated/redacted)") @JsonProperty("rejectedValue") String rejectedValue
    ) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;

        public static ApiError of(@Nullable String field, @Nullable String message, @Nullable String rejectedValue) {
            return new ApiError(n(field), n(message), n(rejectedValue));
        }
        public static ApiError from(ConstraintViolation<?> v) {
            String field = (v.getPropertyPath() == null) ? null : v.getPropertyPath().toString();
            String msg = v.getMessage();
            String rejected = RejectedValueSanitizer.sanitize(field, v.getInvalidValue());
            return of(field, msg, rejected);
        }
    }

    @Nullable private static String n(@Nullable String s) { return (s == null || s.isBlank()) ? null : s; }
    private static String safeMessage(Throwable t) {
        String m = t.getMessage();
        if (m == null || m.isBlank()) return t.getClass().getSimpleName();
        return (m.length() > 500) ? m.substring(0, 500) + "..." : m;
    }
}

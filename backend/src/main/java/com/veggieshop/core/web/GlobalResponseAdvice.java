package com.veggieshop.core.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veggieshop.common.dto.ApiResponse;
import com.veggieshop.common.dto.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Global response wrapper for successful REST responses.
 */
@RestControllerAdvice(basePackages = "com.veggieshop")
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    // Paths we never wrap (docs, health, static, error)
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/error",
            "/actuator/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/webjars/**"
    );
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final ObjectMapper objectMapper;

    public GlobalResponseAdvice(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        // Decide in beforeBodyWrite, where we have request/headers context
        return true;
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body,
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {

        // Only consider successful servlet responses
        ServletServerHttpResponse sresp = (response instanceof ServletServerHttpResponse) ? (ServletServerHttpResponse) response : null;
        if (sresp == null) return body;

        int sc = sresp.getServletResponse().getStatus();
        if (sc == 0 || sc < 200 || sc >= 300) return body;

        HttpServletRequest servletRequest = toServletRequest(request);
        if (servletRequest != null) {
            // query param opt-out: ?format=raw
            String format = servletRequest.getParameter("format");
            if ("raw".equalsIgnoreCase(format)) return body;
            // per-endpoint opt-out
            if (isSkipAnnotated(returnType)) return body;
            // excluded paths (swagger/actuator/error)
            if (isExcludedPath(servletRequest.getRequestURI())) return body;
        }

        // Never wrap standard envelopes or RFC7807
        if (body instanceof ProblemDetail || body instanceof ApiResponse || body instanceof PageResponse) {
            return body;
        }
        // Server-Sent Events / streaming
        if (MediaType.TEXT_EVENT_STREAM.includes(selectedContentType)) {
            return body;
        }

        // NEW: hard guard — if content type is not JSON-like, DO NOT wrap (even for String)
        if (!isJsonLike(selectedContentType)) {
            return body;
        }

        // Special-case: if response indicates a file/download (Content-Disposition), don't wrap
        String cd = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        if (StringUtils.hasText(cd)) {
            return body;
        }

        boolean isStringLike =
                (body instanceof String) ||
                        CharSequence.class.isAssignableFrom(returnType.getParameterType());

        // Build standardized envelope with real status
        Instant now = Instant.now();
        HttpStatus hs = HttpStatus.resolve(sc);
        if (hs == null) hs = HttpStatus.OK;

        ApiResponse<Object> wrapped = ApiResponse.<Object>builder(hs, true)
                .message(hs.getReasonPhrase())
                .data(body) // can be null
                .timestamp(now)
                .build();

        // Special-case String bodies: force JSON so clients get a consistent envelope
        if (isStringLike) {
            try {
                String json = objectMapper.writeValueAsString(wrapped);
                response.getHeaders().setContentType(MediaType.valueOf("application/json;charset=UTF-8"));
                return json;
            } catch (Exception e) {
                // Fallback to the original body on serialization issues
                return body;
            }
        }

        // JSON converters will serialize ApiResponse
        // Ensure content-type stays JSON (keep original if already JSON-like)
        if (!MediaType.APPLICATION_JSON.includes(selectedContentType)) {
            HttpHeaders headers = response.getHeaders();
            if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                headers.setContentType(MediaType.APPLICATION_JSON);
            }
        }
        return wrapped;
    }

    // --------------------------------------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------------------------------------

    /** Marker annotation to opt-out of wrapping for specific controllers/methods. */
    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.METHOD})
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Documented
    public @interface SkipWrap { }

    private boolean isSkipAnnotated(MethodParameter mp) {
        return mp.hasMethodAnnotation(SkipWrap.class)
                || (mp.getContainingClass() != null
                && mp.getContainingClass().isAnnotationPresent(SkipWrap.class));
    }

    private boolean isExcludedPath(@Nullable String path) {
        if (!StringUtils.hasText(path)) return false;
        for (String pattern : EXCLUDED_PATHS) {
            if (PATH_MATCHER.match(pattern, path)) return true;
        }
        return false;
    }

    private boolean isJsonLike(@Nullable MediaType mt) {
        if (mt == null) return true; // assume JSON by default when unknown
        if (MediaType.APPLICATION_JSON.includes(mt)) return true;
        // e.g. application/*+json (HAL etc). Problem+json already skipped via instanceof
        return "application".equalsIgnoreCase(mt.getType())
                && mt.getSubtype() != null
                && mt.getSubtype().toLowerCase(Locale.ROOT).endsWith("+json");
    }

    @Nullable
    private HttpServletRequest toServletRequest(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest sreq) {
            return sreq.getServletRequest();
        }
        return null;
    }
}

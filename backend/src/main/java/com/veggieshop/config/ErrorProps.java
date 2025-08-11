package com.veggieshop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Error handling configuration properties.
 *
 * <p>Allows customizing:
 * <ul>
 *   <li><b>typeBase</b>: the base URL used for RFC7807 "type" links (must end with "/").</li>
 *   <li><b>defaultFormat</b>: the default error format when negotiation doesn't decide
 *       ("api" or "problem").</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "app.errors")
public record ErrorProps(
        String typeBase,
        String defaultFormat
) {
    /**
     * Returns the base type URL, ensuring it ends with "/".
     * Falls back to a sensible default when not configured.
     */
    public String typeBase() {
        String base = (typeBase == null || typeBase.isBlank())
                ? "https://docs.veggieshop.example/errors/"
                : typeBase.trim();
        return base.endsWith("/") ? base : base + "/";
    }

    /**
     * Returns the normalized default error format: either "api" or "problem".
     * Falls back to "api" when not configured or invalid.
     */
    public String defaultFormat() {
        String v = (defaultFormat == null) ? "api" : defaultFormat.trim().toLowerCase();
        return "problem".equals(v) ? "problem" : "api";
    }
}

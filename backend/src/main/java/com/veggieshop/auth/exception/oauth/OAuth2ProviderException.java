package com.veggieshop.auth.exception.oauth;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when an error occurs while interacting with an external OAuth2 provider.
 * Defaults to 502 Bad Gateway.
 */
public final class OAuth2ProviderException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.BAD_GATEWAY;
    public static final String     DEFAULT_CODE    = "OAUTH2_PROVIDER_ERROR";
    public static final String     DEFAULT_MESSAGE =
            "An error occurred while communicating with the OAuth2 provider.";

    /**
     * Full-argument constructor.
     */
    protected OAuth2ProviderException(HttpStatus status,
                                      String code,
                                      String message,
                                      Throwable cause,
                                      Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    /** Uses default message. */
    public OAuth2ProviderException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    /** Custom message. */
    public OAuth2ProviderException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    /** Custom message with cause. */
    public OAuth2ProviderException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    /** Custom message with metadata. */
    public OAuth2ProviderException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    /** Custom message with cause and metadata. */
    public OAuth2ProviderException(String message, Throwable cause, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, metadata);
    }

    @Override
    public OAuth2ProviderException newInstance(HttpStatus status,
                                               String code,
                                               String message,
                                               Throwable cause,
                                               Map<String, Object> metadata) {
        return new OAuth2ProviderException(status, code, message, cause, metadata);
    }
}

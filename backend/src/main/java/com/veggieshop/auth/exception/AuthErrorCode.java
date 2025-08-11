package com.veggieshop.auth.exception;

import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Central registry of authentication/authorization error codes with their default
 * HTTP status and user-facing message. Codes are stable and machine-readable;
 * messages are defaults that can be localized at the API layer.
 */
public enum AuthErrorCode {

    // =========================================================================
    // Authentication errors
    // =========================================================================

    /** Wrong username/password or invalid credentials. */
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
            "The provided username or password is incorrect"),

    /** Account locked (e.g., due to multiple failed attempts). */
    ACCOUNT_LOCKED(HttpStatus.LOCKED, "ACCOUNT_LOCKED",
            "Your account has been locked due to multiple failed login attempts"),

    /** Account exists but is not yet activated/verified. */
    ACCOUNT_NOT_ACTIVATED(HttpStatus.FORBIDDEN, "ACCOUNT_NOT_ACTIVATED",
            "Your account is not activated. Please verify your email or contact support"),

    /** Account disabled/suspended by policy or admin. */
    USER_DISABLED(HttpStatus.FORBIDDEN, "USER_DISABLED",
            "Your account has been disabled. Please contact support"),

    /** User record not found. */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND",
            "The specified user does not exist"),

    /** Temporary auth outage or rate limiting; client may retry later. */
    TEMPORARY_AUTHENTICATION_FAILURE(HttpStatus.SERVICE_UNAVAILABLE, "TEMPORARY_AUTHENTICATION_FAILURE",
            "Authentication service is temporarily unavailable. Please try again later"),

    /** Email not verified yet. */
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED",
            "Your email address has not been verified"),

    /** Password does not meet policy requirements. */
    PASSWORD_TOO_WEAK(HttpStatus.BAD_REQUEST, "PASSWORD_TOO_WEAK",
            "The provided password does not meet the security requirements"),

    /** Password found in breach datasets. */
    PASSWORD_COMPROMISED(HttpStatus.BAD_REQUEST, "PASSWORD_COMPROMISED",
            "The provided password has been found in known data breaches"),

    /** Old password provided does not match current credentials. */
    OLD_PASSWORD_INCORRECT(HttpStatus.BAD_REQUEST, "OLD_PASSWORD_INCORRECT",
            "The old password provided is incorrect"),

    // =========================================================================
    // Authorization errors
    // =========================================================================

    /** Authenticated user lacks authority to access a resource. */
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "UNAUTHORIZED_ACCESS",
            "You are not authorized to access this resource"),

    /** Specific permission/role check failed. */
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "PERMISSION_DENIED",
            "You do not have permission to perform this action"),

    // =========================================================================
    // Session and token errors
    // =========================================================================

    /** Referenced session does not exist. */
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND",
            "No active session found"),

    /** Session is present but no longer valid. */
    SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "SESSION_EXPIRED",
            "Your session has expired. Please log in again"),

    /** Token failed validation (format/signature/audience/etc.). */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN",
            "The provided token is invalid"),

    /** Token has passed its expiry time. */
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED",
            "The provided token has expired"),

    /** Token was explicitly revoked/invalidated. */
    TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "TOKEN_REVOKED",
            "The provided token has been revoked"),

    // =========================================================================
    // MFA errors
    // =========================================================================

    /** Additional factor required to proceed. */
    MFA_REQUIRED(HttpStatus.UNAUTHORIZED, "MFA_REQUIRED",
            "Multi-factor authentication is required for this account"),

    /** Account must enroll/set up MFA before proceeding. */
    MFA_SETUP_REQUIRED(HttpStatus.FORBIDDEN, "MFA_SETUP_REQUIRED",
            "Multi-factor authentication must be set up before proceeding"),

    /** Provided MFA code/assertion failed verification. */
    MFA_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "MFA_VERIFICATION_FAILED",
            "Multi-factor authentication verification failed"),

    // =========================================================================
    // Device & IP errors
    // =========================================================================

    /** Login from an unrecognized device. */
    DEVICE_NOT_RECOGNIZED(HttpStatus.FORBIDDEN, "DEVICE_NOT_RECOGNIZED",
            "The device used for login is not recognized"),

    /** IP is blocked due to policy or abuse signals. */
    IP_ADDRESS_BLOCKED(HttpStatus.FORBIDDEN, "IP_ADDRESS_BLOCKED",
            "Access from this IP address has been blocked"),

    /** Brute-force or abnormal failed-attempts pattern detected. */
    BRUTE_FORCE_DETECTED(HttpStatus.TOO_MANY_REQUESTS, "BRUTE_FORCE_DETECTED",
            "Multiple failed attempts detected. Access temporarily blocked"),

    // =========================================================================
    // OAuth2 errors
    // =========================================================================

    /** Provider-side or network error while talking to the OAuth2 provider. */
    OAUTH2_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY, "OAUTH2_PROVIDER_ERROR",
            "An error occurred while communicating with the OAuth2 provider"),

    /** Linking local account to provider failed (duplicates, invalid data, etc.). */
    OAUTH2_LINKING_FAILED(HttpStatus.CONFLICT, "OAUTH2_LINKING_FAILED",
            "Failed to link the account with the OAuth2 provider");

    // =========================================================================
    // Fields & Constructor
    // =========================================================================

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;

    AuthErrorCode(HttpStatus httpStatus, String code, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    // =========================================================================
    // Lookup helpers
    // =========================================================================

    private static final Map<String, AuthErrorCode> BY_CODE;
    static {
        Map<String, AuthErrorCode> m = new LinkedHashMap<>();
        for (AuthErrorCode e : values()) {
            m.put(e.code, e);
        }
        BY_CODE = Collections.unmodifiableMap(m);
    }

    /** Returns the matching AuthErrorCode for a code, or throws if unknown. */
    public static AuthErrorCode fromCode(String code) {
        AuthErrorCode e = BY_CODE.get(code);
        if (e == null) {
            throw new IllegalArgumentException("Unknown AuthErrorCode: " + code);
        }
        return e;
    }

    /** Returns the matching AuthErrorCode for a code, or empty if unknown. */
    public static Optional<AuthErrorCode> maybeFromCode(String code) {
        return Optional.ofNullable(BY_CODE.get(code));
    }
}

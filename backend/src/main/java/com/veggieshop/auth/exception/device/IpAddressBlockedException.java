package com.veggieshop.auth.exception.device;

import com.veggieshop.core.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Raised when a request originates from a blocked IP address.
 * Defaults to 403 Forbidden.
 */
public final class IpAddressBlockedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final HttpStatus DEFAULT_STATUS  = HttpStatus.FORBIDDEN;
    public static final String     DEFAULT_CODE    = "IP_ADDRESS_BLOCKED";
    public static final String     DEFAULT_MESSAGE = "Access denied: your IP address has been blocked.";

    /**
     * Full constructor used for cloning and complete control.
     */
    protected IpAddressBlockedException(HttpStatus status,
                                        String code,
                                        String message,
                                        Throwable cause,
                                        Map<String, Object> metadata) {
        super(status, code, message, cause, metadata);
    }

    public IpAddressBlockedException() {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
    }

    public IpAddressBlockedException(String message) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
    }

    public IpAddressBlockedException(String message, Map<String, Object> metadata) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
    }

    public IpAddressBlockedException(String message, Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
    }

    public IpAddressBlockedException(Throwable cause) {
        this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
    }

    @Override
    public IpAddressBlockedException newInstance(HttpStatus status,
                                                 String code,
                                                 String message,
                                                 Throwable cause,
                                                 Map<String, Object> metadata) {
        return new IpAddressBlockedException(status, code, message, cause, metadata);
    }

    /**
     * Creates an instance with the given IP in metadata.
     */
    public static IpAddressBlockedException ofIp(String ip, String message) {
        return new IpAddressBlockedException(message, Map.of("ip", ip));
    }

    /**
     * Creates an instance with the given IP and TTL (in seconds) in metadata.
     */
    public static IpAddressBlockedException ofIpWithTTL(String ip, String message, long seconds) {
        return new IpAddressBlockedException(message,
                Map.of("ip", ip, "blockTtlSeconds", seconds));
    }
}

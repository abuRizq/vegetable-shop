package com.veggieshop.core.tracing;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for generating and parsing trace / correlation identifiers.
 *
 * <p>Features:
 * <ul>
 *   <li>Compact, time-ordered IDs for logs and correlation</li>
 *   <li>W3C-compliant trace-id (hex32) and span-id (hex16) support</li>
 *   <li>Parsers for "traceparent", B3 single-header, and AWS X-Amzn-Trace-Id formats</li>
 *   <li>Safe token normalization for inbound request headers</li>
 *   <li>Thread-safe and stateless (SecureRandom held in lazy holder)</li>
 * </ul>
 */
public final class TraceIdGenerator {

    /** W3C trace-id length: 16 bytes = 32 lowercase hex characters. */
    public static final int W3C_TRACE_ID_HEX_LEN = 32;

    /** W3C span-id length: 8 bytes = 16 lowercase hex characters. */
    public static final int W3C_SPAN_ID_HEX_LEN = 16;

    /** Default MDC key for trace id. */
    public static final String DEFAULT_MDC_KEY = "traceId";

    // Header-safe token: alphanumeric + . _ - : /
    private static final Pattern SAFE_TOKEN = Pattern.compile("^[A-Za-z0-9._\\-:/]{8,128}$");
    private static final Pattern HEX32 = Pattern.compile("^[0-9a-f]{32}$");
    private static final Pattern HEX16 = Pattern.compile("^[0-9a-f]{16}$");

    // AWS X-Ray Root format: Root=1-<8-hex-epoch>-<24-hex-unique>[;...]
    private static final Pattern AWS_XRAY_ROOT =
            Pattern.compile("(^|;)\\s*Root=1-([0-9a-fA-F]{8})-([0-9a-fA-F]{24})(;|$)");

    private TraceIdGenerator() {
        // Prevent instantiation
    }

    // ---------------------------------------------------------------------------------------------
    // ID Generation
    // ---------------------------------------------------------------------------------------------

    /**
     * Generates a compact, high-entropy, time-ordered correlation id.
     * Format: base36 timestamp (milliseconds) + "-" + 96 bits of randomness in base36.
     *
     * @return a header-friendly compact ID (e.g., l3t1k1i6-08t9q6m1n2i1h0t7z)
     */
    public static String newCompactId() {
        long now = Instant.now().toEpochMilli();
        String time = toBase36Padded(now, 9); // safe until ~year 33658
        byte[] rnd = new byte[12];            // 96 bits of randomness
        rng().nextBytes(rnd);
        String rand = toBase36(rnd);
        return time + "-" + rand;
    }

    /**
     * Generates a W3C-compliant trace-id (16 bytes → 32 lowercase hex).
     */
    public static String newW3CTraceIdHex32() {
        byte[] b = new byte[16];
        rng().nextBytes(b);
        if (allZero(b)) {
            b[0] = 1; // Avoid illegal all-zeros
        }
        return toLowerHex(b);
    }

    /**
     * Generates a W3C-compliant span-id (8 bytes → 16 lowercase hex).
     */
    public static String newW3CSpanIdHex16() {
        byte[] b = new byte[8];
        rng().nextBytes(b);
        if (allZero(b)) {
            b[0] = 1; // Avoid illegal all-zeros
        }
        return toLowerHex(b);
    }

    /**
     * Default generator for this application.
     */
    public static String newId() {
        return newCompactId();
    }

    // ---------------------------------------------------------------------------------------------
    // Parsing / Validation
    // ---------------------------------------------------------------------------------------------

    /**
     * Parses a W3C "traceparent" header and extracts the trace-id (hex32).
     */
    public static Optional<String> parseTraceParent(String traceparent) {
        if (traceparent == null || traceparent.isBlank()) return Optional.empty();

        String first = traceparent.split(",", 2)[0].trim();
        String[] parts = first.split("-", 4);
        if (parts.length < 4) return Optional.empty();

        String traceId = parts[1].trim().toLowerCase(Locale.ROOT);
        return isValidW3CTraceId(traceId) ? Optional.of(traceId) : Optional.empty();
    }

    /**
     * Builds a minimal W3C "traceparent" header value.
     */
    public static String buildTraceParent(String traceId, String parentId, boolean sampled) {
        if (!isValidW3CTraceId(traceId)) {
            throw new IllegalArgumentException("traceId must be lowercase hex32 and not all zeros");
        }
        String pid = (parentId != null && isValidW3CSpanId(parentId)) ? parentId : newW3CSpanIdHex16();
        String flags = sampled ? "01" : "00";
        return "00-" + traceId + "-" + pid + "-" + flags;
    }

    /** Format-only check for W3C trace-id. */
    public static boolean isW3CTraceIdFormat(String value) {
        return value != null && HEX32.matcher(value).matches();
    }

    /** Full validity check for W3C trace-id (format + not all zeros). */
    public static boolean isValidW3CTraceId(String value) {
        return isW3CTraceIdFormat(value) && !isAllZeros(value);
    }

    /** Format-only check for W3C span-id. */
    public static boolean isW3CSpanIdFormat(String value) {
        return value != null && HEX16.matcher(value).matches();
    }

    /** Full validity check for W3C span-id (format + not all zeros). */
    public static boolean isValidW3CSpanId(String value) {
        return isW3CSpanIdFormat(value) && !isAllZeros(value);
    }

    /**
     * Normalizes a generic inbound correlation token from HTTP headers.
     */
    public static String normalizeToken(String candidate) {
        if (candidate == null) return null;
        String c = candidate.trim();
        if (c.isEmpty()) return null;
        if (c.length() > 128) c = c.substring(0, 128);
        return SAFE_TOKEN.matcher(c).matches() ? c : null;
    }

    /**
     * Parses a B3 single-header string and extracts the trace-id.
     */
    public static Optional<String> parseB3(String b3) {
        if (b3 == null || b3.isBlank()) return Optional.empty();
        String first = b3.split(",", 2)[0].trim();
        String[] parts = first.split("-");
        if (parts.length < 2) return Optional.empty();
        String id = parts[0].trim().toLowerCase(Locale.ROOT);
        boolean ok = (HEX32.matcher(id).matches() || HEX16.matcher(id).matches()) && !isAllZeros(id);
        return ok ? Optional.of(id) : Optional.empty();
    }

    /**
     * Parses AWS X-Amzn-Trace-Id and extracts the 24-hex unique part.
     */
    public static Optional<String> parseAwsXRay(String xray) {
        if (xray == null || xray.isBlank()) return Optional.empty();
        Matcher m = AWS_XRAY_ROOT.matcher(xray);
        if (!m.find()) return Optional.empty();
        String unique = m.group(3).toLowerCase(Locale.ROOT);
        String capped = unique.length() > 128 ? unique.substring(0, 128) : unique;
        return SAFE_TOKEN.matcher(capped).matches() ? Optional.of(capped) : Optional.empty();
    }

    /**
     * Resolves a trace id from multiple header formats or generates a new one.
     */
    public static String resolveOrGenerate(String traceparent,
                                           String b3,
                                           String xTraceId,
                                           String xRequestId,
                                           String xCorrelationId,
                                           String xAmznTraceId) {
        return parseTraceParent(traceparent)
                .or(() -> parseB3(b3))
                .or(() -> Optional.ofNullable(normalizeToken(xTraceId)))
                .or(() -> Optional.ofNullable(normalizeToken(xRequestId)))
                .or(() -> Optional.ofNullable(normalizeToken(xCorrelationId)))
                .or(() -> parseAwsXRay(xAmznTraceId))
                .orElseGet(TraceIdGenerator::newCompactId);
    }

    /**
     * Optionally prefixes an id with a short tag while remaining header-safe.
     */
    public static String withPrefix(String baseId, String prefix) {
        Objects.requireNonNull(baseId, "baseId");
        String p = normalizeToken(prefix);
        return (p == null) ? baseId : p + ":" + baseId;
    }

    // ---------------------------------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------------------------------

    private static SecureRandom rng() {
        return Holder.RNG;
    }

    private static final class Holder {
        static final SecureRandom RNG = new SecureRandom();
    }

    private static boolean allZero(byte[] arr) {
        for (byte b : arr) if (b != 0) return false;
        return true;
    }

    private static boolean isAllZeros(String hex) {
        for (int i = 0; i < hex.length(); i++) if (hex.charAt(i) != '0') return false;
        return true;
    }

    private static String toLowerHex(byte[] bytes) {
        final char[] HEX = "0123456789abcdef".toCharArray();
        char[] out = new char[bytes.length * 2];
        for (int i = 0, j = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            out[j++] = HEX[v >>> 4];
            out[j++] = HEX[v & 0x0F];
        }
        return new String(out);
    }

    /**
     * Converts bytes into a fixed-width base36 string.
     */
    private static String toBase36(byte[] bytes) {
        StringBuilder sb = new StringBuilder(24);
        for (int i = 0; i < bytes.length; i += 3) {
            int b0 = bytes[i] & 0xFF;
            int b1 = (i + 1 < bytes.length) ? (bytes[i + 1] & 0xFF) : 0;
            int b2 = (i + 2 < bytes.length) ? (bytes[i + 2] & 0xFF) : 0;
            int val = (b0 << 16) | (b1 << 8) | b2; // 24 bits
            String chunk = Integer.toUnsignedString(val, 36);
            for (int p = chunk.length(); p < 5; p++) sb.append('0'); // pad to width 5
            sb.append(chunk);
        }
        return sb.toString();
    }

    private static String toBase36Padded(long value, int minLen) {
        String s = Long.toUnsignedString(value, 36);
        if (s.length() >= minLen) return s;
        StringBuilder sb = new StringBuilder(minLen);
        for (int i = s.length(); i < minLen; i++) sb.append('0');
        return sb.append(s).toString();
    }
}

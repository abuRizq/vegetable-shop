// src/main/java/com/veggieshop/core/validation/RejectedValueSanitizer.java
package com.veggieshop.core.validation;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Sanitizes "rejected values" before exposing them in API error payloads or logs.
 */
public final class RejectedValueSanitizer {

    /** Max characters to surface for a sanitized value. */
    public static final int MAX_LEN = 256;

    /** Max items to stringify for arrays/collections/maps. */
    public static final int MAX_ITEMS = 20;

    /** Field name patterns that trigger full redaction (case-insensitive). */
    private static final List<Pattern> SENSITIVE_FIELD_PATTERNS = List.of(
            Pattern.compile("password", Pattern.CASE_INSENSITIVE),
            Pattern.compile("pwd", Pattern.CASE_INSENSITIVE),
            Pattern.compile("secret", Pattern.CASE_INSENSITIVE),
            Pattern.compile("authorization", Pattern.CASE_INSENSITIVE),
            Pattern.compile("credential", Pattern.CASE_INSENSITIVE),

            // keep broad for token/card to catch snake_case like access_token / credit_card
            Pattern.compile("token", Pattern.CASE_INSENSITIVE),
            Pattern.compile("bearer", Pattern.CASE_INSENSITIVE),
            Pattern.compile("card", Pattern.CASE_INSENSITIVE),

            // word-bounded for highly specific fields
            Pattern.compile("\\bcvv\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\biban\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bssn\\b", Pattern.CASE_INSENSITIVE)
    );

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** 12..19 digits with optional spaces/hyphens. Further validated via LUHN. */
    private static final Pattern DIGITS_WITH_SEPARATORS_12_19 = Pattern.compile("^[0-9\\-\\s]{12,19}$");
    private static final int MIN_PAN_LEN = 12, MAX_PAN_LEN = 19;

    private RejectedValueSanitizer() {}

    @Nullable
    public static String sanitize(@Nullable String fieldName, @Nullable Object rejectedValue) {
        if (rejectedValue == null) return null;

        if (isSensitiveField(fieldName)) return "******";

        String s = stringify(rejectedValue);
        s = stripControlChars(s);
        s = collapseWhitespace(s);
        s = maybeMaskEmail(s);
        s = maybeMaskCard(s);
        s = truncate(s, MAX_LEN);

        return s.isBlank() ? null : s;
    }

    @Nullable
    public static String sanitize(@Nullable Object rejectedValue) {
        return sanitize(null, rejectedValue);
    }

    // ---- internals ----

    private static boolean isSensitiveField(@Nullable String field) {
        if (!StringUtils.hasText(field)) return false;
        String f = field.trim();
        for (Pattern p : SENSITIVE_FIELD_PATTERNS) {
            if (p.matcher(f).find()) return true;
        }
        return false;
    }

    private static String stringify(Object value) {
        if (value instanceof CharSequence cs) return cs.toString();

        if (value.getClass().isArray()) {
            int len = Array.getLength(value), n = Math.min(len, MAX_ITEMS);
            List<String> items = new ArrayList<>(n);
            for (int i = 0; i < n; i++) items.add(shortAtom(Array.get(value, i)));
            return "[" + String.join(", ", items) + (len > n ? ", …+" + (len - n) : "") + "]";
        }

        if (value instanceof Collection<?> col) {
            int len = col.size(), n = Math.min(len, MAX_ITEMS);
            Iterator<?> it = col.iterator();
            List<String> items = new ArrayList<>(n);
            for (int i = 0; i < n && it.hasNext(); i++) items.add(shortAtom(it.next()));
            return "[" + String.join(", ", items) + (len > n ? ", …+" + (len - n) : "") + "]";
        }

        if (value instanceof Map<?, ?> map) {
            int len = map.size(), n = Math.min(len, MAX_ITEMS);
            List<String> entries = new ArrayList<>(n);
            int i = 0;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (i++ >= n) break;
                entries.add(shortAtom(e.getKey()) + "=" + shortAtom(e.getValue()));
            }
            return "{" + String.join(", ", entries) + (len > n ? ", …+" + (len - n) : "") + "}";
        }

        return String.valueOf(value);
    }

    private static String shortAtom(@Nullable Object o) {
        if (o == null) return "null";
        String s = (o instanceof CharSequence cs) ? cs.toString() : String.valueOf(o);
        s = stripControlChars(s);
        s = collapseWhitespace(s);
        return truncate(s, Math.min(64, MAX_LEN));
    }

    private static String stripControlChars(String s) {
        return s.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                .replace("\r", "")
                .replace("\n", "")
                .replace("\t", " ");
    }

    private static String collapseWhitespace(String s) {
        return s.trim().replaceAll("\\s{2,}", " ");
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private static String maybeMaskEmail(String s) {
        if (!EMAIL.matcher(s).matches()) return s;
        int at = s.indexOf('@');
        if (at <= 1) return "***" + s.substring(at);
        return s.charAt(0) + "***" + s.charAt(at - 1) + s.substring(at);
    }

    private static String maybeMaskCard(String s) {
        if (!DIGITS_WITH_SEPARATORS_12_19.matcher(s).matches()) return s;
        String digits = s.replaceAll("[^0-9]", "");
        int len = digits.length();
        if (len < MIN_PAN_LEN || len > MAX_PAN_LEN) return s;
        if (!passesLuhn(digits)) return s;
        return "**** **** **** " + digits.substring(len - 4);
    }

    static boolean passesLuhn(String digits) {
        int sum = 0; boolean dbl = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (dbl) { d *= 2; if (d > 9) d -= 9; }
            sum += d; dbl = !dbl;
        }
        return sum % 10 == 0;
    }
}

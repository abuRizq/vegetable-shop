package com.veggieshop.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Schema(name = "PageResponse", description = "Generic pagination envelope")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PageResponse<T>(
        @Schema(description = "Page content items") @JsonProperty("items") List<T> items,
        @Schema(description = "Current page number (0-based)") @JsonProperty("page") int page,
        @Schema(description = "Requested page size") @JsonProperty("size") int size,
        @Schema(description = "Total number of elements (or -1 if unknown)") @JsonProperty("totalElements") long totalElements,
        @Schema(description = "Total pages (or -1 if unknown)") @JsonProperty("totalPages") int totalPages,
        @Schema(description = "Is this the first page?") @JsonProperty("first") boolean first,
        @Schema(description = "Is this the last page?") @JsonProperty("last") boolean last,
        @Schema(description = "Is there a next page?") @JsonProperty("hasNext") boolean hasNext,
        @Schema(description = "Is there a previous page?") @JsonProperty("hasPrevious") boolean hasPrevious,
        @Schema(description = "Sort metadata") @JsonProperty("sort") SortInfo sort,
        @Schema(description = "Navigation links") @JsonProperty("links") List<Link> links
) implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    // --------------------------------------------------------------------------------------------
    // Factory methods for Page<T>
    // --------------------------------------------------------------------------------------------

    public static <T> PageResponse<T> from(Page<T> page) {
        Objects.requireNonNull(page, "page");
        int size = page.getSize();
        long totalElements = page.getTotalElements();
        int totalPages = computeTotalPages(size, totalElements);

        int number = page.getNumber();
        PageFlags flags = flags(number, size, totalElements, totalPages);

        return new PageResponse<>(
                List.copyOf(page.getContent()),
                number,
                size,
                totalElements,
                totalPages,
                flags.first(),
                flags.last(),
                flags.hasNext(),
                flags.hasPrevious(),
                SortInfo.from(page.getSort()),
                null
        );
    }

    public static <T, R> PageResponse<R> map(Page<T> page, Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(mapper, "mapper");
        List<R> mapped = page.getContent().stream()
                .map(mapper)
                .collect(Collectors.toCollection(ArrayList::new));

        int size = page.getSize();
        long totalElements = page.getTotalElements();
        int totalPages = computeTotalPages(size, totalElements);
        int number = page.getNumber();
        PageFlags flags = flags(number, size, totalElements, totalPages);

        return new PageResponse<>(
                mapped,
                number,
                size,
                totalElements,
                totalPages,
                flags.first(),
                flags.last(),
                flags.hasNext(),
                flags.hasPrevious(),
                SortInfo.from(page.getSort()),
                null
        );
    }

    // --------------------------------------------------------------------------------------------
    // Factory methods for Slice<T> (unknown totals)
    // --------------------------------------------------------------------------------------------

    public static <T> PageResponse<T> from(Slice<T> slice) {
        Objects.requireNonNull(slice, "slice");
        return new PageResponse<>(
                List.copyOf(slice.getContent()),
                slice.getNumber(),
                slice.getSize(),
                -1L,
                -1,
                slice.isFirst(),
                slice.isLast(),
                slice.hasNext(),
                slice.hasPrevious(),
                SortInfo.from(slice.getSort()),
                null
        );
    }

    public static <T, R> PageResponse<R> map(Slice<T> slice, Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(slice, "slice");
        Objects.requireNonNull(mapper, "mapper");
        List<R> mapped = slice.getContent().stream()
                .map(mapper)
                .collect(Collectors.toCollection(ArrayList::new));
        return new PageResponse<>(
                mapped,
                slice.getNumber(),
                slice.getSize(),
                -1L,
                -1,
                slice.isFirst(),
                slice.isLast(),
                slice.hasNext(),
                slice.hasPrevious(),
                SortInfo.from(slice.getSort()),
                null
        );
    }

    // --------------------------------------------------------------------------------------------
    // Manual construction (computed flags)
    // --------------------------------------------------------------------------------------------

    public static <T> PageResponse<T> ofComputed(List<T> items,
                                                 int page,
                                                 int size,
                                                 long totalElements,
                                                 Sort sort) {
        int totalPages = computeTotalPages(size, totalElements);
        PageFlags flags = flags(page, size, totalElements, totalPages);

        return new PageResponse<>(
                List.copyOf(Objects.requireNonNullElse(items, List.of())),
                page,
                size,
                totalElements,
                totalPages,
                flags.first(),
                flags.last(),
                flags.hasNext(),
                flags.hasPrevious(),
                SortInfo.from(sort),
                null
        );
    }

    // --------------------------------------------------------------------------------------------
    // Links (HATEOAS-style) — optional (unchanged helpers below)
    // --------------------------------------------------------------------------------------------

    public PageResponse<T> withLinks(URI baseUri, Map<String, ?> preservedQueryParams) {
        Objects.requireNonNull(baseUri, "baseUri");

        List<Link> newLinks = new ArrayList<>(5);
        newLinks.add(new Link("self", buildPageUri(baseUri, toMultiValueMap(preservedQueryParams), page, size)));
        if (hasPrevious) {
            newLinks.add(new Link("prev", buildPageUri(baseUri, toMultiValueMap(preservedQueryParams), page - 1, size)));
            newLinks.add(new Link("first", buildPageUri(baseUri, toMultiValueMap(preservedQueryParams), 0, size)));
        }
        if (hasNext) {
            newLinks.add(new Link("next", buildPageUri(baseUri, toMultiValueMap(preservedQueryParams), page + 1, size)));
        }
        if (totalPages > 0) {
            newLinks.add(new Link("last", buildPageUri(baseUri, toMultiValueMap(preservedQueryParams), totalPages - 1, size)));
        }

        return new PageResponse<>(items, page, size, totalElements, totalPages,
                first, last, hasNext, hasPrevious, sort, List.copyOf(newLinks));
    }

    // --------------------------------------------------------------------------------------------
    // Helpers (URI building; supports absolute & relative base URIs)
    // --------------------------------------------------------------------------------------------

    private static String buildPageUri(URI baseUri, Map<String, List<String>> preserved, int page, int size) {
        StringBuilder sb = new StringBuilder();
        if (baseUri.isAbsolute()) {
            sb.append(baseUri.getScheme()).append("://").append(baseUri.getAuthority());
        }
        if (baseUri.getPath() != null) sb.append(baseUri.getPath());

        Map<String, List<String>> q = new LinkedHashMap<>();
        if (baseUri.getQuery() != null && !baseUri.getQuery().isBlank()) {
            mergeQueryString(q, baseUri.getQuery());
        }
        if (preserved != null) {
            for (var e : preserved.entrySet()) {
                if (e.getKey() == null) continue;
                q.put(e.getKey(), new ArrayList<>(e.getValue()));
            }
        }
        q.put("page", List.of(String.valueOf(page)));
        q.put("size", List.of(String.valueOf(size)));

        String query = toQueryString(q);
        if (!query.isEmpty()) sb.append('?').append(query);

        if (baseUri.getFragment() != null) {
            sb.append('#').append(encode(baseUri.getFragment()));
        }
        return sb.toString();
    }

    private static void mergeQueryString(Map<String, List<String>> q, String raw) {
        for (String pair : raw.split("&")) {
            if (pair.isBlank()) continue;
            int i = pair.indexOf('=');
            String k = i >= 0 ? pair.substring(0, i) : pair;
            String v = i >= 0 ? pair.substring(i + 1) : "";
            q.computeIfAbsent(decode(k), __ -> new ArrayList<>()).add(decode(v));
        }
    }

    private static String toQueryString(Map<String, List<String>> q) {
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (var e : q.entrySet()) {
            for (String v : e.getValue()) {
                if (!first) b.append('&');
                b.append(encode(e.getKey())).append('=').append(encode(v));
                first = false;
            }
        }
        return b.toString();
    }

    private static String encode(String s) {
        return URLEncoder.encode(Objects.toString(s, ""), StandardCharsets.UTF_8);
    }

    private static String decode(String s) {
        return URLDecoder.decode(Objects.toString(s, ""), StandardCharsets.UTF_8);
    }

    // --------------------------------------------------------------------------------------------
    // Internals (policy & flags)
    // --------------------------------------------------------------------------------------------

    /** Overflow-safe totalPages computation (caps at Integer.MAX_VALUE). */
    private static int computeTotalPages(int size, long totalElements) {
        if (size <= 0) return -1;
        if (totalElements < 0) return -1;
        if (totalElements == 0) return 0;
        double pages = Math.ceil(totalElements / (double) size);
        return pages > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) pages;
    }

    private record PageFlags(boolean first, boolean last, boolean hasNext, boolean hasPrevious) { }

    private static PageFlags flags(int page, int size, long totalElements, int totalPages) {
        if (size <= 0 || totalElements < 0 || totalPages < 0) {
            boolean hasPrev = page > 0;
            return new PageFlags(page <= 0, false, false, hasPrev);
        }
        if (totalPages == 0) {
            return new PageFlags(true, true, false, false);
        }
        boolean first = page <= 0;
        boolean last = (page + 1) >= totalPages;
        boolean hasPrev = page > 0;
        boolean hasNext = (page + 1) < totalPages;
        return new PageFlags(first, last, hasNext, hasPrev);
    }

    // --------------------------------------------------------------------------------------------
    // Nested DTOs
    // --------------------------------------------------------------------------------------------

    @Schema(name = "SortInfo", description = "Sorting metadata")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SortInfo(
            @Schema(description = "Is any sorting applied?") @JsonProperty("sorted") boolean sorted,
            @Schema(description = "Is the sort order unsorted?") @JsonProperty("unsorted") boolean unsorted,
            @Schema(description = "Is the sort order empty?") @JsonProperty("empty") boolean empty,
            @Schema(description = "Order list") @JsonProperty("orders") List<Order> orders
    ) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;

        public static SortInfo from(Sort sort) {
            if (sort == null || sort.isUnsorted()) {
                return new SortInfo(false, true, true, List.of());
            }
            List<Order> orders = new ArrayList<>();
            sort.forEach(o -> orders.add(new Order(
                    o.getProperty(),
                    o.getDirection().name(),
                    o.isIgnoreCase(),
                    o.getNullHandling().name()
            )));
            return new SortInfo(true, false, false, List.copyOf(orders));
        }

        @Schema(name = "Order", description = "Single order descriptor")
        public record Order(
                @Schema(description = "Property name") @JsonProperty("property") String property,
                @Schema(description = "ASC or DESC") @JsonProperty("direction") String direction,
                @Schema(description = "Ignore case") @JsonProperty("ignoreCase") boolean ignoreCase,
                @Schema(description = "Null handling") @JsonProperty("nullHandling") String nullHandling
        ) implements Serializable {
            @Serial private static final long serialVersionUID = 1L;
        }
    }

    @Schema(name = "Link", description = "Navigation link")
    public record Link(
            @Schema(description = "Relation, e.g., self, next, prev, first, last") @JsonProperty("rel") String rel,
            @Schema(description = "Absolute or relative URL") @JsonProperty("href") String href
    ) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
    }

    // --------------------------------------------------------------------------------------------
    // Utility: convert preserved params into multi-value map
    // --------------------------------------------------------------------------------------------

    private static Map<String, List<String>> toMultiValueMap(Map<String, ?> src) {
        if (src == null || src.isEmpty()) return Map.of();
        Map<String, List<String>> out = new LinkedHashMap<>();
        for (var e : src.entrySet()) {
            if (e.getKey() == null) continue;
            String key = String.valueOf(e.getKey());
            Object val = e.getValue();
            if (val == null) continue;
            List<String> list = new ArrayList<>();
            if (val instanceof Iterable<?> it) {
                for (Object o : it) list.add(String.valueOf(o));
            } else if (val.getClass().isArray()) {
                int len = java.lang.reflect.Array.getLength(val);
                for (int i = 0; i < len; i++) list.add(String.valueOf(java.lang.reflect.Array.get(val, i)));
            } else {
                list.add(String.valueOf(val));
            }
            out.put(key, list);
        }
        return out;
    }
}

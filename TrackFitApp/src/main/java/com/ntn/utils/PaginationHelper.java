package com.ntn.utils;

import java.util.List;
import java.util.Map;

public final class PaginationHelper {

    private PaginationHelper() {}

    public static int normalizePage(Integer page) {
        return (page == null || page < 1) ? 1 : page;
    }

    public static int normalizePageSize(Integer pageSize, int defaultSize) {
        return (pageSize == null || pageSize < 1) ? defaultSize : pageSize;
    }

    public static int computeTotalPages(long totalElements, int pageSize) {
        return (int) Math.ceil(totalElements * 1.0 / Math.max(pageSize, 1));
    }

    public static int parseParam(Map<String, String> params, String key, int defaultValue) {
        if (params == null || params.get(key) == null) return defaultValue;
        try {
            return Math.max(Integer.parseInt(params.get(key)), 1);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static <T> Map<String, Object> buildResponse(int page, int pageSize, long totalElements, List<T> items) {
        int totalPages = computeTotalPages(totalElements, pageSize);
        return Map.of(
                "page", page,
                "pageSize", pageSize,
                "totalPages", totalPages,
                "totalElements", totalElements,
                "items", items
        );
    }
}

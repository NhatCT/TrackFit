package com.ntn.utils;

import java.util.Set;

public final class ImageValidator {

    private ImageValidator() {}

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");

    public static boolean isAllowedContentType(String contentType) {
        return contentType != null && ALLOWED_TYPES.contains(contentType);
    }
}

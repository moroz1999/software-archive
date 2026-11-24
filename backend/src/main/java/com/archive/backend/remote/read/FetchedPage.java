package com.archive.backend.remote.read;

import org.jspecify.annotations.Nullable;

/**
 * A page of another site as it came: the address it came from after redirects, its status, its Content-Type, its body
 * in UTF-8 (none for content that is not text) cut at a limit ({@code truncated}), and the size read.
 */
public record FetchedPage(
        String finalUrl,
        int status,
        String contentType,
        @Nullable String body,
        boolean truncated,
        long size) {}

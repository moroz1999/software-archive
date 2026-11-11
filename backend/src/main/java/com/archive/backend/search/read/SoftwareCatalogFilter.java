package com.archive.backend.search.read;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** What the software catalog is narrowed by; null is no condition. The category finds its whole subtree. */
public record SoftwareCatalogFilter(
        @Nullable String keyword,
        @Nullable String platform,
        @Nullable Integer year,
        @Nullable UUID category,
        @Nullable UUID country) {}

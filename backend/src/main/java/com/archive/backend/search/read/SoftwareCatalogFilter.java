package com.archive.backend.search.read;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * What the software catalog is narrowed by; null or empty is no condition. The category finds its whole subtree. Of the
 * platforms any one will do; of the hardware, any item of a group, and every group chosen (codes by group).
 */
public record SoftwareCatalogFilter(
        @Nullable String keyword,
        @Nullable String platform,
        @Nullable Integer year,
        @Nullable UUID category,
        @Nullable UUID country,
        @Nullable UUID city,
        List<String> platforms,
        Map<String, List<String>> hardware) {

    public SoftwareCatalogFilter {
        platforms = List.copyOf(platforms);
        hardware = Map.copyOf(hardware);
    }

    public SoftwareCatalogFilter(
            @Nullable String keyword,
            @Nullable String platform,
            @Nullable Integer year,
            @Nullable UUID category,
            @Nullable UUID country) {
        this(keyword, platform, year, category, country, null, List.of(), Map.of());
    }
}

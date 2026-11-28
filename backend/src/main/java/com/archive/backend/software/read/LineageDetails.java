package com.archive.backend.software.read;

import java.util.List;
import org.jspecify.annotations.Nullable;

/** Where the work comes from, by the codes of its kind, reliability and rework (docs/domain/software.md#lineage). */
public record LineageDetails(
        @Nullable String kind,
        @Nullable String reliability,
        List<String> rework,
        @Nullable String source) {

    public static final LineageDetails UNKNOWN = new LineageDetails(null, null, List.of(), null);
}

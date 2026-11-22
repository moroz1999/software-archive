package com.archive.backend.importer.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The report of a run: {@code state} is {@code running}, {@code done} or {@code stopped}. */
public record ImportRunDto(
        UUID id,
        String archive,
        @Nullable String section,
        String state,
        Instant startedAt,
        @Nullable Instant endedAt,
        int added,
        int known,
        int skipped,
        int failed,
        ImportRunProgressDto progress,
        List<ImportRunLogLineDto> log) {}

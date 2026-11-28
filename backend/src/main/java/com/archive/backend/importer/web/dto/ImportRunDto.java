package com.archive.backend.importer.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * The report of a run: {@code state} is {@code running}, {@code done} or {@code stopped}; {@code warnings} counts the
 * warnings of its log.
 */
public record ImportRunDto(
        UUID id,
        String archive,
        @Nullable String section,
        @Schema(ref = "ImportRunState") String state,
        Instant startedAt,
        @Nullable Instant endedAt,
        int added,
        int known,
        int skipped,
        int failed,
        int warnings,
        ImportRunProgressDto progress,
        List<ImportRunLogLineDto> log) {}

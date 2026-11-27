package com.archive.backend.assistant.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record ChatRunDto(
        UUID id,
        @Schema(ref = "RunType") String type,
        @Nullable UUID planId,
        @Schema(ref = "RunStatus") String status,
        boolean stopRequested,
        Instant queuedAt,
        int steps) {}

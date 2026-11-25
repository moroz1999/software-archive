package com.archive.backend.assistant.web.dto;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record ChatRunDto(
        UUID id,
        String type,
        @Nullable UUID planId,
        String status,
        boolean stopRequested,
        Instant queuedAt,
        int steps) {}

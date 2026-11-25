package com.archive.backend.assistant.web.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record ChatMessageDto(
        long seq,
        String role,
        String kind,
        String text,
        @JsonRawValue @Nullable String payload,
        Instant createdAt) {}

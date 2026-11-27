package com.archive.backend.assistant.web.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record ChatMessageDto(
        long seq,
        @Schema(ref = "MessageRole") String role,
        @Schema(ref = "MessageKind") String kind,
        String text,

        @JsonRawValue @Schema(types = "object", description = "What the message carries besides its text") @Nullable
        String payload,

        Instant createdAt) {}

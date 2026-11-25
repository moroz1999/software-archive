package com.archive.backend.assistant.read.chat;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A run of a conversation: {@code chat} or {@code execute}, its status and how far it went. */
public record ChatRun(
        UUID id,
        UUID conversationId,
        String type,
        @Nullable UUID planId,
        String status,
        boolean stopRequested,
        Instant queuedAt,
        int steps,
        @Nullable String error) {}

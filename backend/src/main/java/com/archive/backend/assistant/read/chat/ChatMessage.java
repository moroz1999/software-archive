package com.archive.backend.assistant.read.chat;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

/**
 * A message of a conversation: its role ({@code user}, {@code assistant}, {@code system}), its kind ({@code text},
 * {@code toolCall}, {@code plan}, {@code planProgress}, {@code error}, {@code stopped}), its text and what it
 * carries besides, as a JSON object.
 */
public record ChatMessage(
        long seq,
        String role,
        String kind,
        String text,
        @Nullable String payload,
        Instant createdAt) {}

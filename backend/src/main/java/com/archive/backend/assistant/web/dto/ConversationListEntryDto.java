package com.archive.backend.assistant.web.dto;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record ConversationListEntryDto(UUID id, @Nullable String title, Instant lastMessageAt, boolean running) {}

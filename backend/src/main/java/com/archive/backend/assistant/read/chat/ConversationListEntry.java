package com.archive.backend.assistant.read.chat;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A conversation in the list of its owner: none has a title before its first message. */
public record ConversationListEntry(UUID id, @Nullable String title, Instant lastMessageAt, boolean running) {}

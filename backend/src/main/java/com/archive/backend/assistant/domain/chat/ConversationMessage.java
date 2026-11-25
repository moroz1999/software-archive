package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.AggregateRoot;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

/** A message of a conversation, as it was written; it never changes. Its repository gives it the next seq. */
@Getter
@AggregateRoot
public final class ConversationMessage {

    private final ConversationId conversationId;
    private final MessageRole role;
    private final MessageKind kind;
    private final MessageBody body;
    private final Instant createdAt;

    public ConversationMessage(
            ConversationId conversationId, MessageRole role, MessageKind kind, MessageBody body, Instant createdAt) {
        this.conversationId = Objects.requireNonNull(conversationId, "conversationId");
        this.role = Objects.requireNonNull(role, "role");
        this.kind = Objects.requireNonNull(kind, "kind");
        this.body = Objects.requireNonNull(body, "body");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }
}

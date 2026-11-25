package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** Who a message of a conversation is from. */
@ValueObject
public enum MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

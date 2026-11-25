package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** What a run does: answers the conversation, or makes the changes of an approved plan. */
@ValueObject
public enum RunType {
    CHAT,
    EXECUTE
}

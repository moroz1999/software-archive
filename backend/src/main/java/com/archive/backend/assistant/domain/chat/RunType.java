package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.EnumCodes;
import com.archive.backend.shared.domain.ddd.ValueObject;

/** What a run does: answers the conversation, or makes the changes of an approved plan. */
@ValueObject
public enum RunType {
    CHAT,
    EXECUTE;

    /** The code it is known by outside the backend: {@code TOOL_CALL} as {@code toolCall}. */
    public String code() {
        return EnumCodes.camel(this);
    }
}

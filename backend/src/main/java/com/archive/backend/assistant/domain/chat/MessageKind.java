package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.EnumCodes;
import com.archive.backend.shared.domain.ddd.ValueObject;

/** What a message of a conversation is. */
@ValueObject
public enum MessageKind {
    /** Prose: of the editor or of the assistant. */
    TEXT,
    /** A call of a read tool with its result. */
    TOOL_CALL,
    /** A plan proposed. */
    PLAN,
    /** What came of a change of a plan, or of the plan. */
    PLAN_PROGRESS,
    /** Why a run failed. */
    ERROR,
    /** The editor stopped the assistant here. */
    STOPPED;

    /** The code it is known by outside the backend: {@code TOOL_CALL} as {@code toolCall}. */
    public String code() {
        return EnumCodes.camel(this);
    }
}

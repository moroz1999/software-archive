package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.EnumCodes;
import com.archive.backend.shared.domain.ddd.ValueObject;

@ValueObject
public enum PlanStatus {
    /** Waits for the editor. */
    PROPOSED,
    EXECUTING,
    DONE,
    /** A change failed: those before it are made, those after it are not. */
    FAILED,
    /** The editor stopped it. */
    STOPPED,
    /** The editor wrote another message instead of approving it. */
    SUPERSEDED,
    CANCELLED;

    /** The code it is known by outside the backend: {@code TOOL_CALL} as {@code toolCall}. */
    public String code() {
        return EnumCodes.camel(this);
    }
}

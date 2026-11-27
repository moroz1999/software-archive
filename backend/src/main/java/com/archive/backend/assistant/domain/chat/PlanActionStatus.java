package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.EnumCodes;
import com.archive.backend.shared.domain.ddd.ValueObject;

@ValueObject
public enum PlanActionStatus {
    PENDING,
    RUNNING,
    DONE,
    FAILED,
    SKIPPED;

    /** The code it is known by outside the backend: {@code TOOL_CALL} as {@code toolCall}. */
    public String code() {
        return EnumCodes.camel(this);
    }
}

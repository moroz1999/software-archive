package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.EnumCodes;
import com.archive.backend.shared.domain.ddd.ValueObject;

@ValueObject
public enum RunStatus {
    QUEUED,
    RUNNING,
    /** Answered, or a plan made. */
    DONE,
    /** Ended by a plan proposed. */
    WAITING_APPROVAL,
    FAILED,
    STOPPED;

    /** The code it is known by outside the backend: {@code WAITING_APPROVAL} as {@code waitingApproval}. */
    public String code() {
        return EnumCodes.camel(this);
    }

    public boolean active() {
        return this == QUEUED || this == RUNNING;
    }
}

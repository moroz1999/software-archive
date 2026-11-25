package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.ValueObject;

@ValueObject
public enum PlanActionStatus {
    PENDING,
    RUNNING,
    DONE,
    FAILED,
    SKIPPED
}

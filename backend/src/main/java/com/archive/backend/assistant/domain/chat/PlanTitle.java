package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;

/** What a plan does, in a line. */
@ValueObject
public record PlanTitle(String value) {

    public PlanTitle {
        value = value.strip();
        if (value.isEmpty()) {
            throw new InvalidValueException("assistant.plan-title-required", "A plan needs a title.");
        }
    }
}

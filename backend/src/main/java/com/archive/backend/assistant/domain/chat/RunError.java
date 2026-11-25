package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** Why a run failed, as the editor is told. */
@ValueObject
public record RunError(String value) {

    public RunError {
        Objects.requireNonNull(value, "value");
    }
}

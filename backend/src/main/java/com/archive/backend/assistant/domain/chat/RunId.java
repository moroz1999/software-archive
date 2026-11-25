package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.UUID;

@ValueObject
public record RunId(UUID value) {

    public RunId {
        Objects.requireNonNull(value, "value");
    }
}

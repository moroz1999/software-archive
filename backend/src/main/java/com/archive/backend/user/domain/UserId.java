package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.UUID;

@ValueObject
public record UserId(UUID value) {

    public UserId {
        Objects.requireNonNull(value, "value");
    }
}

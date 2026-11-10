package com.archive.backend.group.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.UUID;

/**
 * The id of a group: the team behind its names.
 */
@ValueObject
public record GroupId(UUID value) {

    public GroupId {
        Objects.requireNonNull(value, "value");
    }
}

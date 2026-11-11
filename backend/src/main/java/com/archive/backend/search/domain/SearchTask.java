package com.archive.backend.search.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/**
 * A search document waiting to be brought up to date; {@code renewal} tells it apart from the same entity's task
 * renewed by a later change.
 */
@ValueObject
public record SearchTask(SearchEntity entity, long renewal, Attempts attempts) {

    public SearchTask {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(attempts, "attempts");
    }
}

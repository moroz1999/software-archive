package com.archive.backend.search.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.UUID;

/** An entity with a search document: the document carries the entity's id. */
@ValueObject
public record SearchEntity(SearchEntityType type, UUID id) {

    public SearchEntity {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(id, "id");
    }
}

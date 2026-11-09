package com.archive.backend.author.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.UUID;

/** The id of an author: the person behind the names. */
@ValueObject
public record AuthorId(UUID value) {

    public AuthorId {
        Objects.requireNonNull(value, "value");
    }
}

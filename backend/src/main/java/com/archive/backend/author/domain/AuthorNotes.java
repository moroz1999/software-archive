package com.archive.backend.author.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

@ValueObject
public record AuthorNotes(String value) {

    public AuthorNotes {
        Objects.requireNonNull(value, "value");
    }
}

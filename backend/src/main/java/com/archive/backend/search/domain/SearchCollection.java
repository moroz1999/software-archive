package com.archive.backend.search.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** One build of the search collection of a kind of entities; the alias of the kind points to the one in use. */
@ValueObject
public record SearchCollection(SearchEntityType type, String name) {

    public SearchCollection {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(name, "name");
    }
}

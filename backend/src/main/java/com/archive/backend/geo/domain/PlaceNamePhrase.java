package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Objects;

/** A name of a country or a city to look for, in any language of the site; never empty. */
@ValueObject
public record PlaceNamePhrase(String value) {

    public PlaceNamePhrase {
        value = Objects.requireNonNull(value, "value").strip();
        if (value.isEmpty()) {
            throw new InvalidValueException("geo.name-required", "A name to look for is required.");
        }
    }
}

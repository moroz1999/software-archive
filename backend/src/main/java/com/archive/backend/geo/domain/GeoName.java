package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** The name of a country or a city in one language, as a source writes it. */
@ValueObject
public record GeoName(String value) {

    public GeoName {
        Objects.requireNonNull(value, "value");
        value = value.strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("A place needs a name.");
        }
    }
}

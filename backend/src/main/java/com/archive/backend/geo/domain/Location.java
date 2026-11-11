package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/**
 * Where someone is located; the primary location is where they are best known from.
 */
@ValueObject
public record Location(Place place, boolean primary) {

    public Location {
        Objects.requireNonNull(place, "place");
    }
}

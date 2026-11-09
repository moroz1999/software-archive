package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;

import java.util.Objects;
import java.util.UUID;

@ValueObject
public record CityId(UUID value) {

    public CityId {
        Objects.requireNonNull(value, "value");
    }
}

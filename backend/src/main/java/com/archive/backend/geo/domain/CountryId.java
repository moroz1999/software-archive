package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.UUID;

@ValueObject
public record CountryId(UUID value) {

    public CountryId {
        Objects.requireNonNull(value, "value");
    }
}

package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.UUID;

@ValueObject
public record CountryId(UUID value) {

    /** Where a city whose country is not known belongs until it is sorted out by hand (docs/domain/geo.md). */
    public static final CountryId UNKNOWN = new CountryId(UUID.fromString("00000000-0000-0000-0000-00000000c001"));

    public CountryId {
        Objects.requireNonNull(value, "value");
    }
}

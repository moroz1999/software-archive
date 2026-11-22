package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Map;

/** The names of a country or a city, one per language; the Russian one is always there (docs/domain/geo.md). */
@ValueObject
public record GeoNames(Map<Language, GeoName> values) {

    public GeoNames {
        values = Map.copyOf(values);
        if (!values.containsKey(Language.RU)) {
            throw new InvalidValueException("geo.name-required", "A country or a city needs a name in Russian.");
        }
    }
}

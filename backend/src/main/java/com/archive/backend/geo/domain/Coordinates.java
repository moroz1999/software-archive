package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;

/** Where on the globe a country or a city is: a latitude from −90 to 90 and a longitude from −180 to 180. */
@ValueObject
public record Coordinates(double latitude, double longitude) {

    public Coordinates {
        if (!(latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180)) {
            throw invalid();
        }
    }

    /** Coordinates are given both or not at all. */
    public static InvalidValueException invalid() {
        return new InvalidValueException(
                "geo.coordinates-invalid", "Coordinates are a latitude and a longitude on the globe, both or none.");
    }
}

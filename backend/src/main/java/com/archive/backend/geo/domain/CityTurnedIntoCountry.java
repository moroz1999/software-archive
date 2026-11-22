package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.Objects;

/** A city of no known country turned out to be a country: every place in it is now in that country alone. */
public record CityTurnedIntoCountry(CityId city, CountryId country) implements DomainEvent {

    public CityTurnedIntoCountry {
        Objects.requireNonNull(city, "city");
        Objects.requireNonNull(country, "country");
    }
}

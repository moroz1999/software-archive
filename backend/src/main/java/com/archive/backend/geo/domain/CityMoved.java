package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** A city put in another country, or in none: the places in it are now in that country. */
public record CityMoved(CityId city, @Nullable CountryId country) implements DomainEvent {

    public CityMoved {
        Objects.requireNonNull(city, "city");
    }
}

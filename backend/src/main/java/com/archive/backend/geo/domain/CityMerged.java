package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * A city merged into another: every place in it is now in that one, and it goes. The country is the one of the city it
 * went into, none while that is not known.
 */
public record CityMerged(CityId city, CityId into, @Nullable CountryId country) implements DomainEvent {

    public CityMerged {
        Objects.requireNonNull(city, "city");
        Objects.requireNonNull(into, "into");
    }
}

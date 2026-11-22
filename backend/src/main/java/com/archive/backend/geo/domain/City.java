package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.AggregateRoot;
import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/** A city of the archive, in its country or in none while that is not known (docs/domain/geo.md#keeping). */
@Getter
@AggregateRoot
public final class City {

    private final CityId id;
    private GeoNames names;
    private @Nullable KnownCountry country;
    private @Nullable Coordinates coordinates;

    @Getter(lombok.AccessLevel.NONE)
    private final List<DomainEvent> events = new ArrayList<>();

    public City(CityId id, GeoNames names, @Nullable KnownCountry country, @Nullable Coordinates coordinates) {
        this.id = Objects.requireNonNull(id, "id");
        this.names = Objects.requireNonNull(names, "names");
        this.country = country;
        this.coordinates = coordinates;
    }

    /** A city put in another country, or taken out of one, tells it moved: the places in it go with it. */
    public void edit(GeoNames names, @Nullable KnownCountry country, @Nullable Coordinates coordinates) {
        this.names = Objects.requireNonNull(names, "names");
        this.coordinates = coordinates;
        moveTo(country);
    }

    /** Puts the city in the country, or in none, keeping all else it has; a change tells it moved. */
    public void moveTo(@Nullable KnownCountry country) {
        if (!Objects.equals(countryId(this.country), countryId(country))) {
            events.add(new CityMoved(id, countryId(country)));
        }
        this.country = country;
    }

    /** Only a city no place is in goes: nothing of the archive is lost with it. */
    public void requireDeletable(GeoUses uses) {
        if (uses.cityInUse(id)) {
            throw new CityInUseException();
        }
    }

    public @Nullable CountryId countryId() {
        return countryId(country);
    }

    private static @Nullable CountryId countryId(@Nullable KnownCountry country) {
        return country == null ? null : country.id();
    }

    /** What happened to the city since it was loaded, handed over once. */
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }
}

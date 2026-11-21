package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * A place of the archive: a country, or a city with its country, if its country is known. Only {@link Places} builds one, checking it against
 * the reference book; storage restores what was checked.
 */
@ValueObject
public final class Place {

    private final @Nullable CountryId country;
    private final @Nullable CityId city;

    Place(@Nullable CountryId country, @Nullable CityId city) {
        if (country == null && city == null) {
            throw new IllegalArgumentException("A place is a country or a city.");
        }
        this.country = country;
        this.city = city;
    }

    /**
     * A place read back from storage: it was checked when stored.
     */
    public static Place restore(@Nullable CountryId country, @Nullable CityId city) {
        return new Place(country, city);
    }

    public @Nullable CountryId country() {
        return country;
    }

    public @Nullable CityId city() {
        return city;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof Place place
                && Objects.equals(place.country, country)
                && Objects.equals(place.city, city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, city);
    }

    @Override
    public String toString() {
        return "Place[" + country + ", " + city + "]";
    }
}

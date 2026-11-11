package com.archive.backend.geo.domain;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Builds places, checking them against the reference book: a city of the archive belongs to its own country.
 */
public class Places {

    private final GeoCatalog catalog;

    public Places(GeoCatalog catalog) {
        this.catalog = catalog;
    }

    /**
     * A city given alone is placed in its country.
     */
    public Place of(@Nullable CountryId country, @Nullable CityId city) {
        if (city == null) {
            if (country == null) {
                throw new LocationEmptyException();
            }
            if (!catalog.countryExists(country)) {
                throw new LocationUnknownException();
            }
            return new Place(country, null);
        }
        CountryId cityCountry = catalog.countryOf(city).orElseThrow(LocationUnknownException::new);
        if (country != null && !country.equals(cityCountry)) {
            throw new LocationUnknownException();
        }
        return new Place(cityCountry, city);
    }

    public Locations locations(List<LocationDraft> drafts) {
        return new Locations(drafts.stream()
                .map(draft -> new Location(of(draft.country(), draft.city()), draft.primary()))
                .toList());
    }
}

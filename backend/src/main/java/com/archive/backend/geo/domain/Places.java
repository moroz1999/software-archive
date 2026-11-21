package com.archive.backend.geo.domain;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Builds places, checking them against the reference book: a city of the archive belongs to its own country, or to
 * none while its country is not known.
 */
public class Places {

    private final GeoCatalog catalog;

    public Places(GeoCatalog catalog) {
        this.catalog = catalog;
    }

    /**
     * A city given alone is placed in its country; a city of no known country stands alone, and is in no country given
     * with it.
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
        if (!catalog.cityExists(city)) {
            throw new LocationUnknownException();
        }
        Optional<CountryId> cityCountry = catalog.countryOf(city);
        if (country != null && !cityCountry.equals(Optional.of(country))) {
            throw new LocationUnknownException();
        }
        return new Place(cityCountry.orElse(null), city);
    }

    public Locations locations(List<LocationDraft> drafts) {
        return new Locations(drafts.stream()
                .map(draft -> new Location(of(draft.country(), draft.city()), draft.primary()))
                .toList());
    }
}

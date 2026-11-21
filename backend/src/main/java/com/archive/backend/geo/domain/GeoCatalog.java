package com.archive.backend.geo.domain;

import java.util.Optional;

/**
 * The countries and cities of the archive, the reference book places are checked against.
 */
public interface GeoCatalog {

    boolean countryExists(CountryId country);

    /** Whether the city is one of the archive, of a known country or not. */
    boolean cityExists(CityId city);

    /**
     * The country of a city of the archive; nothing for an unknown city and for one of no known country.
     */
    Optional<CountryId> countryOf(CityId city);
}

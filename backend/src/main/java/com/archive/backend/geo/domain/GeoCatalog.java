package com.archive.backend.geo.domain;

import java.util.Optional;

/**
 * The countries and cities of the archive, the reference book places are checked against.
 */
public interface GeoCatalog {

    boolean countryExists(CountryId country);

    /**
     * The country of a city of the archive; nothing for an unknown city.
     */
    Optional<CountryId> countryOf(CityId city);
}

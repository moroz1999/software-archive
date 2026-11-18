package com.archive.backend.geo.read;

import java.util.List;
import java.util.UUID;

/**
 * Reads countries and cities as plain read models (docs/backend.md).
 */
public interface GeoReadRepository {

    /**
     * All countries, each named in the language of the given code, or in Russian when it has no name in that one; by
     * that name.
     */
    List<CountryItem> findCountries(String language);

    /**
     * The cities of a country, named as countries are, by that name; none for an unknown country.
     */
    List<CityItem> findCities(UUID countryId, String language);
}

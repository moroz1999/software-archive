package com.archive.backend.geo.read;

import java.util.List;
import java.util.Optional;
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

    /**
     * Every country with its cities, named as countries are, both by that name, each with how many places of people,
     * groups and labels are in it.
     */
    List<GeographyCountry> findGeography(String language);

    Optional<CountryDetails> findCountry(UUID id);

    Optional<CityDetails> findCity(UUID id);

    /** The cities of no known country, named as countries are, by that name, each with how many places are in it. */
    List<GeographyCity> findCitiesOfNoCountry(String language);

    /**
     * The countries, and the cities of a country or of none, named alike in any language — letter case, "ё" and "е",
     * spaces and hyphens aside — named as countries are.
     */
    GeoDuplicates findDuplicates(String language);
}

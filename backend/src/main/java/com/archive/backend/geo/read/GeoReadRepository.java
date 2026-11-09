package com.archive.backend.geo.read;

import java.util.List;
import java.util.UUID;

/** Reads countries and cities as plain read models (docs/backend.md). */
public interface GeoReadRepository {

    /** All countries by name. */
    List<CountryItem> findCountries();

    /** The cities of a country by name; none for an unknown country. */
    List<CityItem> findCities(UUID countryId);
}

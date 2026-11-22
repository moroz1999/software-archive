package com.archive.backend.geo.domain;

import org.jspecify.annotations.Nullable;

/** The places of people, groups and labels in a city. */
public interface CityLocations {

    /** Puts every place in the city in the country, or in none. */
    void moveTo(CityId city, @Nullable CountryId country);
}

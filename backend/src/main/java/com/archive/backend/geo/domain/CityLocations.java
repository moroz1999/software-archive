package com.archive.backend.geo.domain;

import org.jspecify.annotations.Nullable;

/** The places of people, groups and labels in a city. */
public interface CityLocations {

    /** Puts every place in the city in the country, or in none. */
    void moveTo(CityId city, @Nullable CountryId country);

    /**
     * Puts every place in the city, and every old label record of it, in the other city and its country; a place an
     * owner then has twice is kept once, primary when either was.
     */
    void mergeInto(CityId city, CityId into, @Nullable CountryId country);
}

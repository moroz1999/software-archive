package com.archive.backend.geo.domain;

/** The cities of a country and the places of people, groups and labels in it. */
public interface CountryLocations {

    /**
     * Puts the cities and every place of the country in the other one; a place an owner then has twice is kept once,
     * primary when either was.
     */
    void mergeInto(CountryId country, CountryId into);
}

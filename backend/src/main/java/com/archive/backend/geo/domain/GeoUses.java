package com.archive.backend.geo.domain;

/** What of the archive is in a country or a city: what would be lost with it. */
public interface GeoUses {

    /** Whether the country has cities, or places are in it directly. */
    boolean countryInUse(CountryId country);

    /** Whether a place of a person, a group or a label is in the city. */
    boolean cityInUse(CityId city);
}

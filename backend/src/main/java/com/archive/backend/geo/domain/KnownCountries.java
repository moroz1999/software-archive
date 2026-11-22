package com.archive.backend.geo.domain;

/** Checks the country chosen for a city against the countries of the archive. */
public class KnownCountries {

    private final GeoCatalog catalog;

    public KnownCountries(GeoCatalog catalog) {
        this.catalog = catalog;
    }

    public KnownCountry of(CountryId country) {
        if (!catalog.countryExists(country)) {
            throw new CountryUnknownException();
        }
        return KnownCountry.checked(country);
    }
}

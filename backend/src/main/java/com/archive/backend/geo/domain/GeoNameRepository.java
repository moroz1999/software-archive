package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.Language;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/** The countries and cities of the archive by their names, in whichever language they are named. */
public interface GeoNameRepository {

    Optional<CityId> cityNamed(GeoName name);

    Optional<CountryId> countryNamed(GeoName name);

    /** Adds a city, in no country when its country is not known. */
    void addCity(CityId city, @Nullable CountryId country, Language language, GeoName name);

    void addCountry(CountryId country, Language language, GeoName name);
}

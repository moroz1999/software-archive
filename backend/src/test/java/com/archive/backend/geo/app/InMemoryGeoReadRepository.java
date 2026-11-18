package com.archive.backend.geo.app;

import com.archive.backend.geo.read.CityItem;
import com.archive.backend.geo.read.CountryItem;
import com.archive.backend.geo.read.GeoReadRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Serves what it was given in each language: naming in a language is the SQL's job. */
class InMemoryGeoReadRepository implements GeoReadRepository {

    private final Map<String, List<CountryItem>> countries = new HashMap<>();
    private final Map<String, Map<UUID, List<CityItem>>> cities = new HashMap<>();

    void addCountry(String language, CountryItem country, CityItem... citiesOfCountry) {
        countries.computeIfAbsent(language, code -> new ArrayList<>()).add(country);
        cities.computeIfAbsent(language, code -> new HashMap<>()).put(country.id(), List.of(citiesOfCountry));
    }

    @Override
    public List<CountryItem> findCountries(String language) {
        return List.copyOf(countries.getOrDefault(language, List.of()));
    }

    @Override
    public List<CityItem> findCities(UUID countryId, String language) {
        return cities.getOrDefault(language, Map.of()).getOrDefault(countryId, List.of());
    }
}

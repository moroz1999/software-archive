package com.archive.backend.geo.app;

import com.archive.backend.geo.read.CityItem;
import com.archive.backend.geo.read.CountryItem;
import com.archive.backend.geo.read.GeoReadRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class InMemoryGeoReadRepository implements GeoReadRepository {

    private final List<CountryItem> countries = new ArrayList<>();
    private final Map<UUID, List<CityItem>> cities = new HashMap<>();

    void addCountry(CountryItem country, CityItem... citiesOfCountry) {
        countries.add(country);
        cities.put(country.id(), List.of(citiesOfCountry));
    }

    @Override
    public List<CountryItem> findCountries() {
        return List.copyOf(countries);
    }

    @Override
    public List<CityItem> findCities(UUID countryId) {
        return cities.getOrDefault(countryId, List.of());
    }
}

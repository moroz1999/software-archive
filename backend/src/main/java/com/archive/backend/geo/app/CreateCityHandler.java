package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.City;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CityRepository;
import com.archive.backend.geo.domain.Country;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.KnownCountries;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCityHandler {

    private final CityRepository cities;
    private final KnownCountries countries;

    @Transactional
    public CityId handle(CreateCityCommand c) {
        Country.requireKeptBy(c.actor());
        CityId id = new CityId(UUID.randomUUID());
        CountryId country = c.country();
        City city = new City(id, c.names(), country == null ? null : countries.of(country), c.coordinates());
        cities.save(city);
        return city.getId();
    }
}

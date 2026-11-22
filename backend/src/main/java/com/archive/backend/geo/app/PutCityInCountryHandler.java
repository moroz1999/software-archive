package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.City;
import com.archive.backend.geo.domain.CityNotFoundException;
import com.archive.backend.geo.domain.CityRepository;
import com.archive.backend.geo.domain.Country;
import com.archive.backend.geo.domain.KnownCountries;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Puts a city in a country, as a city of no known country is sorted out (docs/domain/geo.md#keeping). */
@Service
@RequiredArgsConstructor
public class PutCityInCountryHandler {

    private final CityRepository cities;
    private final KnownCountries countries;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(PutCityInCountryCommand c) {
        Country.requireKeptBy(c.actor());
        City city = cities.findById(c.city()).orElseThrow(CityNotFoundException::new);
        city.moveTo(countries.of(c.country()));
        cities.save(city);
        // The places in the city and the search answer within this transaction (docs/backend.md).
        city.pullEvents().forEach(events::publishEvent);
    }
}

package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.City;
import com.archive.backend.geo.domain.CityNotFoundException;
import com.archive.backend.geo.domain.CityRepository;
import com.archive.backend.geo.domain.Country;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.KnownCountries;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCityHandler {

    private final CityRepository cities;
    private final KnownCountries countries;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(UpdateCityCommand c) {
        Country.requireKeptBy(c.actor());
        City city = cities.findById(c.id()).orElseThrow(CityNotFoundException::new);
        CountryId country = c.country();
        city.edit(c.names(), country == null ? null : countries.of(country), c.coordinates());
        cities.save(city);
        // The places in a moved city and the search answer within this transaction (docs/backend.md).
        city.pullEvents().forEach(events::publishEvent);
    }
}

package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.City;
import com.archive.backend.geo.domain.CityNotFoundException;
import com.archive.backend.geo.domain.CityRepository;
import com.archive.backend.geo.domain.Country;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.CountryRepository;
import com.archive.backend.geo.domain.GeoUses;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Turns a city of no known country into the country it really is (docs/domain/geo.md#keeping). */
@Service
@RequiredArgsConstructor
public class TurnCityIntoCountryHandler {

    private final CityRepository cities;
    private final CountryRepository countries;
    private final GeoUses uses;
    private final ApplicationEventPublisher events;

    @Transactional
    public CountryId handle(TurnCityIntoCountryCommand c) {
        Country.requireKeptBy(c.actor());
        City city = cities.findById(c.city()).orElseThrow(CityNotFoundException::new);
        Country country = city.turnIntoCountry(new CountryId(UUID.randomUUID()), uses);
        countries.save(country);
        // Its places move to the country, and the search is told, within this transaction (docs/backend.md).
        city.pullEvents().forEach(events::publishEvent);
        cities.delete(city);
        return country.getId();
    }
}

package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.City;
import com.archive.backend.geo.domain.CityNotFoundException;
import com.archive.backend.geo.domain.CityRepository;
import com.archive.backend.geo.domain.CityUnknownException;
import com.archive.backend.geo.domain.Country;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Merges a duplicate city into the one that stays (docs/domain/geo.md#keeping). */
@Service
@RequiredArgsConstructor
public class MergeCityHandler {

    private final CityRepository cities;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(MergeCityCommand c) {
        Country.requireKeptBy(c.actor());
        City city = cities.findById(c.city()).orElseThrow(CityNotFoundException::new);
        City into = c.into().equals(c.city()) ? city : cities.findById(c.into()).orElseThrow(CityUnknownException::new);
        city.mergeInto(into);
        // Its places move in answer, and the search is told, within this transaction (docs/backend.md).
        city.pullEvents().forEach(events::publishEvent);
        cities.delete(city);
    }
}

package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.Country;
import com.archive.backend.geo.domain.CountryNotFoundException;
import com.archive.backend.geo.domain.CountryRepository;
import com.archive.backend.geo.domain.CountryUnknownException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Merges a duplicate country into the one that stays (docs/domain/geo.md#keeping). */
@Service
@RequiredArgsConstructor
public class MergeCountryHandler {

    private final CountryRepository countries;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(MergeCountryCommand c) {
        Country.requireKeptBy(c.actor());
        Country country = countries.findById(c.country()).orElseThrow(CountryNotFoundException::new);
        Country into = c.into().equals(c.country())
                ? country
                : countries.findById(c.into()).orElseThrow(() -> new CountryUnknownException().onField("into"));
        country.mergeInto(into);
        // Its cities and places move in answer, and the search is told, within this transaction (docs/backend.md).
        country.pullEvents().forEach(events::publishEvent);
        countries.delete(country);
    }
}

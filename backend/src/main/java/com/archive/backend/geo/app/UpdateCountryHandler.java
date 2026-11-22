package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.Country;
import com.archive.backend.geo.domain.CountryNotFoundException;
import com.archive.backend.geo.domain.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCountryHandler {

    private final CountryRepository countries;

    @Transactional
    public void handle(UpdateCountryCommand c) {
        Country.requireKeptBy(c.actor());
        Country country = countries.findById(c.id()).orElseThrow(CountryNotFoundException::new);
        country.edit(c.names(), c.coordinates());
        countries.save(country);
    }
}

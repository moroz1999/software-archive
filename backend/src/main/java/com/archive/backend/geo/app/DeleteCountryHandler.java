package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.Country;
import com.archive.backend.geo.domain.CountryNotFoundException;
import com.archive.backend.geo.domain.CountryRepository;
import com.archive.backend.geo.domain.GeoUses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCountryHandler {

    private final CountryRepository countries;
    private final GeoUses uses;

    @Transactional
    public void handle(DeleteCountryCommand c) {
        Country.requireKeptBy(c.actor());
        Country country = countries.findById(c.id()).orElseThrow(CountryNotFoundException::new);
        country.requireDeletable(uses);
        countries.delete(country);
    }
}

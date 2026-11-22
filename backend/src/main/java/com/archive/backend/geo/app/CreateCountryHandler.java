package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.Country;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.CountryRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCountryHandler {

    private final CountryRepository countries;

    @Transactional
    public CountryId handle(CreateCountryCommand c) {
        Country.requireKeptBy(c.actor());
        CountryId id = new CountryId(UUID.randomUUID());
        countries.save(new Country(id, c.names(), c.coordinates()));
        return id;
    }
}

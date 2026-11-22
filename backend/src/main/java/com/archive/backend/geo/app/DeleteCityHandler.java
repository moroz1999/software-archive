package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.City;
import com.archive.backend.geo.domain.CityNotFoundException;
import com.archive.backend.geo.domain.CityRepository;
import com.archive.backend.geo.domain.Country;
import com.archive.backend.geo.domain.GeoUses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCityHandler {

    private final CityRepository cities;
    private final GeoUses uses;

    @Transactional
    public void handle(DeleteCityCommand c) {
        Country.requireKeptBy(c.actor());
        City city = cities.findById(c.id()).orElseThrow(CityNotFoundException::new);
        city.requireDeletable(uses);
        cities.delete(city);
    }
}

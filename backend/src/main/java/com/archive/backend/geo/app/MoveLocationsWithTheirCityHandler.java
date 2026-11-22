package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.CityLocations;
import com.archive.backend.geo.domain.CityMerged;
import com.archive.backend.geo.domain.CityMoved;
import com.archive.backend.geo.domain.CityTurnedIntoCountry;
import com.archive.backend.geo.domain.CountryLocations;
import com.archive.backend.geo.domain.CountryMerged;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * The places in a city put in another country, merged into another or turned into a country follow it, in the same transaction
 * (docs/domain/geo.md#keeping).
 */
@Service
@RequiredArgsConstructor
public class MoveLocationsWithTheirCityHandler {

    private final CityLocations locations;
    private final CountryLocations countryLocations;

    @EventListener
    public void on(CityMoved moved) {
        locations.moveTo(moved.city(), moved.country());
    }

    /** The places in a merged city are in the city it went into, in that one's country. */
    @EventListener
    public void on(CityMerged merged) {
        locations.mergeInto(merged.city(), merged.into(), merged.country());
    }

    /** The places in a city turned into a country are in that country alone. */
    @EventListener
    public void on(CityTurnedIntoCountry turned) {
        locations.moveOutTo(turned.city(), turned.country());
    }

    /** The cities of a merged country, and the places in it, are in the country it went into. */
    @EventListener
    public void on(CountryMerged merged) {
        countryLocations.mergeInto(merged.country(), merged.into());
    }
}

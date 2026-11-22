package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.CityLocations;
import com.archive.backend.geo.domain.CityMoved;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/** The places in a city put in another country are in that country, in the same transaction (docs/domain/geo.md). */
@Service
@RequiredArgsConstructor
public class MoveLocationsOfMovedCityHandler {

    private final CityLocations locations;

    @EventListener
    public void on(CityMoved moved) {
        locations.moveTo(moved.city(), moved.country());
    }
}

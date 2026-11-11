package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The locations of someone, in their order: each place once, exactly one of them primary (docs/domain/authors.md).
 * A place given twice is kept where it first stands, primary when either was.
 */
@ValueObject
public record Locations(List<Location> all) {

    public Locations {
        Map<Place, Boolean> places = new LinkedHashMap<>();
        all.forEach(location -> places.merge(location.place(), location.primary(), Boolean::logicalOr));
        List<Location> distinct = places.entrySet().stream()
                .map(entry -> new Location(entry.getKey(), entry.getValue()))
                .toList();
        if (!distinct.isEmpty() && distinct.stream().filter(Location::primary).count() != 1) {
            throw new LocationPrimaryException();
        }
        all = distinct;
    }
}

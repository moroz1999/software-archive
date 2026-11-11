package com.archive.backend.geo.infra;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.Location;
import com.archive.backend.geo.domain.Locations;
import com.archive.backend.geo.domain.Place;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The locations of an owner ↔ their rows. Places are restored without the checks: they were checked when stored.
 */
public final class LocationRows {

    private LocationRows() {}

    /**
     * Records from before the rule may have no location or several marked primary: the first marked one, else the
     * first one, is primary.
     */
    public static Locations toLocations(List<? extends LocationEntity> rows) {
        int primary = IntStream.range(0, rows.size())
                .filter(i -> Boolean.TRUE.equals(rows.get(i).getPrimary()))
                .findFirst()
                .orElse(0);
        return new Locations(IntStream.range(0, rows.size())
                .mapToObj(i -> new Location(toPlace(rows.get(i)), i == primary))
                .toList());
    }

    /**
     * The rows of the locations, as new rows of the owner's kind.
     */
    public static <T extends LocationEntity> List<T> fromLocations(Locations locations, Supplier<T> newRow) {
        return locations.all().stream()
                .map(location -> {
                    CityId city = location.place().city();
                    T row = newRow.get();
                    row.setCountryId(location.place().country().value());
                    row.setCityId(city == null ? null : city.value());
                    row.setPrimary(location.primary());
                    return row;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * A city alone keeps the country it is in.
     */
    private static Place toPlace(LocationEntity row) {
        UUID country = Objects.requireNonNull(row.getPlaceCountryId(), "country");
        UUID city = row.getCityId();
        return Place.restore(new CountryId(country), city == null ? null : new CityId(city));
    }
}

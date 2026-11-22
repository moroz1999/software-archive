package com.archive.backend.geo.infra;

import com.archive.backend.geo.domain.Coordinates;
import com.archive.backend.geo.domain.GeoName;
import com.archive.backend.geo.domain.GeoNames;
import com.archive.backend.shared.domain.Language;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/** The names and the coordinates of countries and cities as their rows keep them. */
final class GeoNamesRows {

    private GeoNamesRows() {}

    static GeoNames toNames(Map<String, String> rows) {
        Map<Language, GeoName> names = new HashMap<>();
        rows.forEach((code, name) -> names.put(Language.known(code), new GeoName(name)));
        return new GeoNames(names);
    }

    static Map<String, String> toRows(GeoNames names) {
        Map<String, String> rows = new HashMap<>();
        names.values().forEach((language, name) -> rows.put(language.code(), name.value()));
        return rows;
    }

    static @Nullable Coordinates toCoordinates(@Nullable Double latitude, @Nullable Double longitude) {
        return latitude == null || longitude == null ? null : new Coordinates(latitude, longitude);
    }
}

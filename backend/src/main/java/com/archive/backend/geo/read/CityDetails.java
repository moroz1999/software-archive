package com.archive.backend.geo.read;

import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A city with its names by the codes of their languages, its country, if known, and its coordinates, if known. */
public record CityDetails(
        UUID id,
        Map<String, String> names,
        @Nullable UUID countryId,
        @Nullable Double latitude,
        @Nullable Double longitude) {}

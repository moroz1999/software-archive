package com.archive.backend.geo.web.dto;

import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Body of adding and of editing a country or a city: its names by the codes of their languages and its coordinates,
 * both or none; a city's country, none while it is not known. A country takes no country.
 */
public record PlaceRequestDto(
        @Nullable Map<String, String> names,
        @Nullable UUID countryId,
        @Nullable Double latitude,
        @Nullable Double longitude) {}

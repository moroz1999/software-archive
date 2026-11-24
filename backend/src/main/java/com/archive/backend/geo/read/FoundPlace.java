package com.archive.backend.geo.read;

import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * A country ({@code country}) or a city ({@code city}) found by a name of it: its name as visitors see it, its names
 * by language, and of a city its country, none while it is not known.
 */
public record FoundPlace(
        UUID id,
        String kind,
        String name,
        Map<String, String> names,
        @Nullable UUID countryId,
        @Nullable String country) {}

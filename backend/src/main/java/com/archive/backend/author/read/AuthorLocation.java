package com.archive.backend.author.read;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Where an author is located: a country, a city of it, or both; the country of a city given alone is its own.
 */
public record AuthorLocation(
        @Nullable UUID countryId,
        @Nullable String country,
        @Nullable UUID cityId,
        @Nullable String city,
        boolean primary) {}

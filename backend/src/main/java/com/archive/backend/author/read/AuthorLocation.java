package com.archive.backend.author.read;

import org.jspecify.annotations.Nullable;

/** Where an author is located: a country, a city of it, or both. */
public record AuthorLocation(
        @Nullable String country, @Nullable String city, boolean primary) {}

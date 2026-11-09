package com.archive.backend.group.read;

import org.jspecify.annotations.Nullable;

/** Where a group is located: a country, a city of it, or both. */
public record GroupLocation(@Nullable String country, @Nullable String city, boolean primary) {
}

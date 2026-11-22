package com.archive.backend.geo.read;

import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A country with its names by the codes of their languages and its coordinates, if known. */
public record CountryDetails(
        UUID id,
        Map<String, String> names,
        @Nullable Double latitude,
        @Nullable Double longitude) {}

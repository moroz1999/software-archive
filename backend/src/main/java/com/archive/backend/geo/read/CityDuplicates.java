package com.archive.backend.geo.read;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Cities of one country that may be one city, with the cities of no known country named alike; of no country when
 * they all are such.
 */
public record CityDuplicates(@Nullable String country, List<GeoDuplicate> cities) {}

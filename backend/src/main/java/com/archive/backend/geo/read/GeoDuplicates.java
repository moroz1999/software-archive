package com.archive.backend.geo.read;

import java.util.List;

/** The countries and the cities that may be duplicates, each group of what may be one. */
public record GeoDuplicates(List<List<GeoDuplicate>> countries, List<CityDuplicates> cities) {}

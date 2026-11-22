package com.archive.backend.geo.web.dto;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Body of putting a city in a country. */
public record CityCountryRequestDto(@Nullable UUID countryId) {}

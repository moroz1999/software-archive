package com.archive.backend.assistant.web.place;

import java.util.UUID;

/** The arguments of put_city_in_country. */
public record PutCityInCountryArguments(UUID id, UUID countryId) {}

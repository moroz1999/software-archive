package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.shared.domain.Language;

public record ListCitiesQuery(CountryId country, Language language) {}

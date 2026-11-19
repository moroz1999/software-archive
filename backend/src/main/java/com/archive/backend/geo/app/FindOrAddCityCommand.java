package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.GeoName;
import com.archive.backend.shared.domain.Language;

/** A city named in one language, its country not known. */
public record FindOrAddCityCommand(GeoName name, Language language) {}

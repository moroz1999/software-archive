package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.GeoName;
import com.archive.backend.shared.domain.Language;

/** A country named in one language. */
public record FindOrAddCountryCommand(GeoName name, Language language) {}

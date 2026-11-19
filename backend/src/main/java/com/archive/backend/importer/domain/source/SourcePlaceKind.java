package com.archive.backend.importer.domain.source;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** A place of a source is a city or a country, or either when the source does not tell which. */
@ValueObject
public enum SourcePlaceKind {
    CITY,
    COUNTRY,
    /** A country when the archive has a country of that name, else a city. */
    CITY_OR_COUNTRY
}

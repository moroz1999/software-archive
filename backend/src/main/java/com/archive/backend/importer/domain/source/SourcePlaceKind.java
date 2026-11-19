package com.archive.backend.importer.domain.source;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** A place of a source is a city or a country. */
@ValueObject
public enum SourcePlaceKind {
    CITY,
    COUNTRY
}

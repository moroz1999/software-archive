package com.archive.backend.search.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** The year of a release date, the software catalog is narrowed by. */
@ValueObject
public record ReleaseYear(int value) {}

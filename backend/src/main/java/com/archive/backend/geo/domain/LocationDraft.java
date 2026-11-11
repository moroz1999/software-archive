package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import org.jspecify.annotations.Nullable;

/**
 * A location as asked for, not yet checked against the reference book.
 */
@ValueObject
public record LocationDraft(
        @Nullable CountryId country, @Nullable CityId city, boolean primary) {}

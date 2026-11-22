package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.Objects;

/** A country merged into another: its cities and every place in it are now in that one, and it goes. */
public record CountryMerged(CountryId country, CountryId into) implements DomainEvent {

    public CountryMerged {
        Objects.requireNonNull(country, "country");
        Objects.requireNonNull(into, "into");
    }
}

package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** A country found in the archive, the only one a city is put in; built by {@link KnownCountries}. */
@ValueObject
public final class KnownCountry {

    private final CountryId id;

    private KnownCountry(CountryId id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    static KnownCountry checked(CountryId id) {
        return new KnownCountry(id);
    }

    /** A country stored as a city's: it was checked when stored. */
    public static KnownCountry restore(CountryId id) {
        return new KnownCountry(id);
    }

    public CountryId id() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof KnownCountry known && known.id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

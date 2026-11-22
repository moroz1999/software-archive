package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.AggregateRoot;
import com.archive.backend.user.domain.Actor;
import java.util.Objects;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/** A country of the archive, kept by administrators (docs/domain/geo.md#keeping). */
@Getter
@AggregateRoot
public final class Country {

    private final CountryId id;
    private GeoNames names;
    private @Nullable Coordinates coordinates;

    public Country(CountryId id, GeoNames names, @Nullable Coordinates coordinates) {
        this.id = Objects.requireNonNull(id, "id");
        this.names = Objects.requireNonNull(names, "names");
        this.coordinates = coordinates;
    }

    /** Countries and cities are kept by administrators only (docs/domain/users.md). */
    public static void requireKeptBy(Actor actor) {
        actor.requireAllowed(actor.mayKeepPlaces());
    }

    public void edit(GeoNames names, @Nullable Coordinates coordinates) {
        this.names = Objects.requireNonNull(names, "names");
        this.coordinates = coordinates;
    }

    /** Only a country with no cities and no places in it goes: nothing of the archive is lost with it. */
    public void requireDeletable(GeoUses uses) {
        if (uses.countryInUse(id)) {
            throw new CountryInUseException();
        }
    }
}

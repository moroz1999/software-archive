package com.archive.backend.geo.domain;

import com.archive.backend.shared.domain.ddd.AggregateRoot;
import com.archive.backend.shared.domain.ddd.DomainEvent;
import com.archive.backend.user.domain.Actor;
import java.util.ArrayList;
import java.util.List;
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

    @Getter(lombok.AccessLevel.NONE)
    private final List<DomainEvent> events = new ArrayList<>();

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

    /** Tells the country is merged into the other one: its cities and places go there, then it goes. */
    public void mergeInto(Country other) {
        if (other.id.equals(id)) {
            throw new MergeIntoItselfException();
        }
        events.add(new CountryMerged(id, other.id));
    }

    /** What happened to the country since it was loaded, handed over once. */
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    /** Only a country with no cities and no places in it goes: nothing of the archive is lost with it. */
    public void requireDeletable(GeoUses uses) {
        if (uses.countryInUse(id)) {
            throw new CountryInUseException();
        }
    }
}

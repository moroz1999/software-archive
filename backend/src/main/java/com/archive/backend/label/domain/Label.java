package com.archive.backend.label.domain;

import com.archive.backend.geo.domain.Locations;
import com.archive.backend.shared.domain.ddd.AggregateRoot;
import com.archive.backend.shared.domain.ddd.DomainEvent;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.AddedBy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

/**
 * A name works are released under, of nobody or of an owner: its name, its locations and whoever added it
 * (docs/domain/authors.md#label). Whose it is belongs to the owner, which only attaches and releases it.
 */
@Getter
@AggregateRoot
public final class Label {

    private final LabelId id;
    private LabelName name;
    private Locations locations;
    /** Whoever added the label may change it (docs/domain/users.md). */
    private final AddedBy addedBy;

    @Getter(lombok.AccessLevel.NONE)
    private final List<DomainEvent> events = new ArrayList<>();

    private Label(LabelId id, LabelName name, Locations locations, AddedBy addedBy) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.locations = Objects.requireNonNull(locations, "locations");
        this.addedBy = Objects.requireNonNull(addedBy, "addedBy");
    }

    /**
     * A label read back from storage.
     */
    public static Label restore(LabelId id, LabelName name, Locations locations, AddedBy addedBy) {
        return new Label(id, name, locations, addedBy);
    }

    /**
     * A new label of nobody with its locations, added by the signed-in user who gives it.
     */
    public static Label create(LabelId id, LabelName name, Locations locations, Actor actor) {
        actor.requireAllowed(actor.mayAddEntries());
        Label label = new Label(id, name, locations, AddedBy.of(actor));
        label.events.add(new LabelCreated(id));
        return label;
    }

    /** Whoever added the label changes it, and administrators any (docs/domain/users.md). */
    public void requireChangeableBy(Actor actor) {
        addedBy.requireChangeableBy(actor);
    }

    /** Only administrators say whose a label is and change it (docs/domain/users.md). */
    public void requireOwnerChangeableBy(Actor actor) {
        actor.requireAllowed(actor.isAdministrator());
    }

    /**
     * Renames and relocates the label; it stays the same label, with its credits and memberships.
     */
    public void edit(LabelName name, Locations locations, Actor actor) {
        requireChangeableBy(actor);
        this.name = Objects.requireNonNull(name, "name");
        this.locations = Objects.requireNonNull(locations, "locations");
        events.add(new LabelChanged(id));
    }

    /** Tells the label is going, with its credits and memberships: its repository deletes it. */
    public void delete(Actor actor) {
        requireChangeableBy(actor);
        events.add(new LabelDeleted(id));
    }

    /** The events since the last pull, emptied. */
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }
}

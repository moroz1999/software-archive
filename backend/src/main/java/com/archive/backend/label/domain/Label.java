package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.ddd.AggregateRoot;
import com.archive.backend.shared.domain.ddd.DomainEvent;
import com.archive.backend.user.domain.Actor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

/**
 * A label of nobody: a name works are released under, not yet known to be a person's or a group's
 * (docs/domain/authors.md).
 */
@Getter
@AggregateRoot
public final class Label {

    private final LabelId id;
    private final LabelName name;

    @Getter(lombok.AccessLevel.NONE)
    private final List<DomainEvent> events = new ArrayList<>();

    private Label(LabelId id, LabelName name) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
    }

    /**
     * A label of nobody read back from storage.
     */
    public static Label restore(LabelId id, LabelName name) {
        return new Label(id, name);
    }

    /**
     * A new label of nobody; only a signed-in user adds entries.
     */
    public static Label create(LabelId id, LabelName name, Actor actor) {
        actor.requireAllowed(actor.mayAddEntries());
        Label label = new Label(id, name);
        label.events.add(new LabelCreated(id));
        return label;
    }

    /** The events since the last pull, emptied. */
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }
}

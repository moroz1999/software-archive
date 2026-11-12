package com.archive.backend.group.domain;

import com.archive.backend.geo.domain.Locations;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.label.domain.OwnLabel;
import com.archive.backend.label.domain.OwnLabels;
import com.archive.backend.label.domain.OwnerOfLabelsChanged;
import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * A team behind its labels, with a name of its own and at least one type (docs/domain/authors.md#groups). Its members
 * are changed apart from these details.
 */
@Getter
public final class Group {

    private final GroupId id;
    private GroupName name;
    private @Nullable GroupNotes notes;
    private GroupTypeSet types;
    private OwnLabels labels;
    private Locations locations;

    @Getter(lombok.AccessLevel.NONE)
    private final List<DomainEvent> events = new ArrayList<>();

    public Group(
            GroupId id,
            GroupName name,
            @Nullable GroupNotes notes,
            GroupTypeSet types,
            OwnLabels labels,
            Locations locations) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.notes = notes;
        this.types = Objects.requireNonNull(types, "types");
        this.labels = Objects.requireNonNull(labels, "labels");
        this.locations = Objects.requireNonNull(locations, "locations");
    }

    public static Group create(
            GroupId id,
            GroupName name,
            @Nullable GroupNotes notes,
            GroupTypeSet types,
            List<LabelDraft> labels,
            Locations locations) {
        Group group = new Group(id, name, notes, types, OwnLabels.of(labels), locations);
        group.events.add(new GroupChanged(id));
        return group;
    }

    /**
     * Replaces the details; labels left out are dropped, not deleted.
     */
    public void edit(
            GroupName name,
            @Nullable GroupNotes notes,
            GroupTypeSet types,
            List<LabelDraft> labels,
            Locations locations) {
        OwnLabels edited = this.labels.replace(labels);
        events.add(labelsChanged());
        events.add(new GroupChanged(id));
        this.name = Objects.requireNonNull(name, "name");
        this.notes = notes;
        this.types = Objects.requireNonNull(types, "types");
        this.labels = edited;
        this.locations = Objects.requireNonNull(locations, "locations");
    }

    /** Tells the group is going: its repository deletes it and leaves its labels to nobody. */
    public void delete() {
        events.add(labelsChanged());
        events.add(new GroupChanged(id));
    }

    private OwnerOfLabelsChanged labelsChanged() {
        return new OwnerOfLabelsChanged(labels.all().stream().map(OwnLabel::id).collect(Collectors.toSet()));
    }

    /**
     * What happened to the group since it was loaded, handed over once.
     */
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }
}

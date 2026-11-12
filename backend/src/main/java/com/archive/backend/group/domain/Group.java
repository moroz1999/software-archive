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
 * A team behind its labels, with a name of its own, at least one type and its roster
 * (docs/domain/authors.md#groups).
 */
@Getter
public final class Group {

    private final GroupId id;
    private GroupName name;
    private @Nullable GroupNotes notes;
    private GroupTypeSet types;
    private OwnLabels labels;
    private Locations locations;
    private GroupMembers members;

    @Getter(lombok.AccessLevel.NONE)
    private final List<DomainEvent> events = new ArrayList<>();

    public Group(
            GroupId id,
            GroupName name,
            @Nullable GroupNotes notes,
            GroupTypeSet types,
            OwnLabels labels,
            Locations locations,
            GroupMembers members) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.notes = notes;
        this.types = Objects.requireNonNull(types, "types");
        this.labels = Objects.requireNonNull(labels, "labels");
        this.locations = Objects.requireNonNull(locations, "locations");
        this.members = Objects.requireNonNull(members, "members");
    }

    public Group(
            GroupId id,
            GroupName name,
            @Nullable GroupNotes notes,
            GroupTypeSet types,
            OwnLabels labels,
            Locations locations) {
        this(id, name, notes, types, labels, locations, GroupMembers.EMPTY);
    }

    public static Group create(
            GroupId id,
            GroupName name,
            @Nullable GroupNotes notes,
            GroupTypeSet types,
            List<LabelDraft> labels,
            Locations locations,
            GroupMembers members) {
        Group group = new Group(id, name, notes, types, OwnLabels.of(labels), locations, members);
        group.markChanged();
        return group;
    }

    public static Group create(
            GroupId id,
            GroupName name,
            @Nullable GroupNotes notes,
            GroupTypeSet types,
            List<LabelDraft> labels,
            Locations locations) {
        return create(id, name, notes, types, labels, locations, GroupMembers.EMPTY);
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
        markChanged();
        this.name = Objects.requireNonNull(name, "name");
        this.notes = notes;
        this.types = Objects.requireNonNull(types, "types");
        this.labels = edited;
        this.locations = Objects.requireNonNull(locations, "locations");
    }

    /** Replaces the complete roster of this group. */
    public void changeMembers(GroupMembers members) {
        this.members = Objects.requireNonNull(members, "members");
        markChanged();
    }

    /** Tells the group is going: its repository deletes it and leaves its labels to nobody. */
    public void delete() {
        events.add(labelsChanged());
        events.add(new GroupChanged(id));
    }

    private OwnerOfLabelsChanged labelsChanged() {
        return new OwnerOfLabelsChanged(labels.all().stream().map(OwnLabel::id).collect(Collectors.toSet()));
    }

    private void markChanged() {
        GroupChanged changed = new GroupChanged(id);
        if (!events.contains(changed)) {
            events.add(changed);
        }
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

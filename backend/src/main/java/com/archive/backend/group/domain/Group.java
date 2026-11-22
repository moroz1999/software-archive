package com.archive.backend.group.domain;

import com.archive.backend.geo.domain.Locations;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.OwnLabel;
import com.archive.backend.label.domain.OwnerOfLabelsChanged;
import com.archive.backend.label.domain.UnmarkedLabelDraft;
import com.archive.backend.label.domain.UnmarkedLabels;
import com.archive.backend.shared.domain.ddd.AggregateRoot;
import com.archive.backend.shared.domain.ddd.DomainEvent;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.AddedBy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * A team behind its labels, with a name of its own, at least one type and its roster
 * (docs/domain/authors.md#groups).
 */
@Getter
@AggregateRoot
public final class Group {

    private final GroupId id;
    private GroupName name;
    private @Nullable GroupNotes notes;
    private GroupTypeSet types;
    private UnmarkedLabels labels;
    private Locations locations;
    private GroupMembers members;
    /** Whoever added the group may change it (docs/domain/users.md). */
    private final AddedBy addedBy;

    @Getter(lombok.AccessLevel.NONE)
    private final List<DomainEvent> events = new ArrayList<>();

    public Group(
            GroupId id,
            GroupName name,
            @Nullable GroupNotes notes,
            GroupTypeSet types,
            UnmarkedLabels labels,
            Locations locations,
            GroupMembers members,
            AddedBy addedBy) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.notes = notes;
        this.types = Objects.requireNonNull(types, "types");
        this.labels = Objects.requireNonNull(labels, "labels");
        this.locations = Objects.requireNonNull(locations, "locations");
        this.members = Objects.requireNonNull(members, "members");
        this.addedBy = Objects.requireNonNull(addedBy, "addedBy");
    }

    public static Group create(
            GroupId id,
            GroupName name,
            @Nullable GroupNotes notes,
            GroupTypeSet types,
            List<UnmarkedLabelDraft> labels,
            Locations locations,
            GroupMembers members,
            AddedBy addedBy) {
        Group group =
                new Group(id, name, notes, types, UnmarkedLabels.of(labels, addedBy), locations, members, addedBy);
        group.markChanged();
        return group;
    }

    /** Whoever added the group changes it, and administrators any (docs/domain/users.md). */
    public void requireChangeableBy(Actor actor) {
        addedBy.requireChangeableBy(actor);
    }

    /**
     * Replaces the details; labels left out are dropped, not deleted, and new ones are added by {@code editor}.
     */
    public void edit(
            GroupName name,
            @Nullable GroupNotes notes,
            GroupTypeSet types,
            List<UnmarkedLabelDraft> labels,
            Locations locations,
            AddedBy editor) {
        UnmarkedLabels edited = this.labels.replace(labels, editor);
        events.add(labelsChanged());
        markChanged();
        this.name = Objects.requireNonNull(name, "name");
        this.notes = notes;
        this.types = Objects.requireNonNull(types, "types");
        this.labels = edited;
        this.locations = Objects.requireNonNull(locations, "locations");
    }

    /** Fills what the group lacks from an import, never rewriting what it has (docs/domain/imports.md#authors). */
    public void supplement(Locations locations) {
        if (this.locations.all().isEmpty()) {
            this.locations = Objects.requireNonNull(locations, "locations");
        }
        markChanged();
    }

    /** Replaces the complete roster of this group. */
    public void changeMembers(GroupMembers members) {
        this.members = Objects.requireNonNull(members, "members");
        markChanged();
    }

    /** Takes a label of nobody found out to be the group's; only its works change their owner. */
    public void adopt(Label label) {
        labels = labels.adopt(new OwnLabel(label.getId(), label.getName(), label.getAddedBy()));
        events.add(new OwnerOfLabelsChanged(Set.of(label.getId())));
        markChanged();
    }

    /** A new group replacing a label under its name, of the types chosen, located where the label was. */
    public static Group replacing(GroupId id, Label label, GroupTypeSet types, AddedBy addedBy) {
        Group group = new Group(
                id,
                new GroupName(label.getName().value()),
                null,
                types,
                UnmarkedLabels.of(List.of(), addedBy),
                label.getLocations(),
                GroupMembers.EMPTY,
                addedBy);
        group.events.add(new LabelReplacedByGroup(label.getId(), id));
        group.events.add(new OwnerOfLabelsChanged(Set.of(label.getId())));
        group.markChanged();
        return group;
    }

    /**
     * Takes in a label found out to be a duplicate of the group: its places are added to the group's, and its credits,
     * memberships and remembered origin become the group's before it goes.
     */
    public void absorb(Label label) {
        events.add(new LabelReplacedByGroup(label.getId(), id));
        events.add(new OwnerOfLabelsChanged(Set.of(label.getId())));
        locations = locations.with(label.getLocations());
        markChanged();
    }

    /** Gives up a label that turned out to be somebody else's; only its works change their owner. */
    public void release(LabelId label) {
        labels = labels.release(label);
        events.add(new OwnerOfLabelsChanged(Set.of(label)));
        markChanged();
    }

    /** Tells the group is going: its repository deletes it and leaves its labels to nobody. */
    public void delete() {
        events.add(labelsChanged());
        events.add(new GroupChanged(id));
        events.add(new GroupDeleted(id));
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

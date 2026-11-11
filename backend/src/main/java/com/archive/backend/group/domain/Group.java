package com.archive.backend.group.domain;

import com.archive.backend.geo.domain.Locations;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.label.domain.OwnLabels;
import java.util.List;
import java.util.Objects;
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
        return new Group(id, name, notes, types, OwnLabels.of(labels), locations);
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
        this.name = Objects.requireNonNull(name, "name");
        this.notes = notes;
        this.types = Objects.requireNonNull(types, "types");
        this.labels = edited;
        this.locations = Objects.requireNonNull(locations, "locations");
    }
}

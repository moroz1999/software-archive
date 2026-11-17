package com.archive.backend.author.domain;

import com.archive.backend.geo.domain.Locations;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.OwnLabel;
import com.archive.backend.label.domain.OwnLabels;
import com.archive.backend.label.domain.OwnerOfLabelsChanged;
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
 * A person behind the labels, known by a real name, a label or both (docs/domain/authors.md).
 */
@Getter
@AggregateRoot
public final class Author {

    private final AuthorId id;
    private @Nullable RealName realName;
    private @Nullable AuthorNotes notes;
    private OwnLabels labels;
    private Locations locations;
    /** Whoever added the person may change them (docs/domain/users.md). */
    private final AddedBy addedBy;

    @Getter(lombok.AccessLevel.NONE)
    private final List<DomainEvent> events = new ArrayList<>();

    public Author(
            AuthorId id,
            @Nullable RealName realName,
            @Nullable AuthorNotes notes,
            OwnLabels labels,
            Locations locations,
            AddedBy addedBy) {
        this.id = Objects.requireNonNull(id, "id");
        this.realName = realName;
        this.notes = notes;
        this.labels = requireNamed(realName, Objects.requireNonNull(labels, "labels"));
        this.locations = Objects.requireNonNull(locations, "locations");
        this.addedBy = Objects.requireNonNull(addedBy, "addedBy");
    }

    public static Author create(
            AuthorId id,
            @Nullable RealName realName,
            @Nullable AuthorNotes notes,
            List<LabelDraft> labels,
            Locations locations,
            AddedBy addedBy) {
        Author author = new Author(id, realName, notes, OwnLabels.of(labels, addedBy), locations, addedBy);
        author.events.add(new AuthorChanged(id));
        return author;
    }

    /** Whoever added the person changes them, and administrators anyone (docs/domain/users.md). */
    public void requireChangeableBy(Actor actor) {
        addedBy.requireChangeableBy(actor);
    }

    /**
     * Replaces everything; labels left out are dropped, not deleted, and new ones are added by {@code editor}.
     */
    public void edit(
            @Nullable RealName realName,
            @Nullable AuthorNotes notes,
            List<LabelDraft> labels,
            Locations locations,
            AddedBy editor) {
        OwnLabels edited = requireNamed(realName, this.labels.replace(labels, editor));
        events.add(labelsChanged());
        events.add(new AuthorChanged(id));
        this.realName = realName;
        this.notes = notes;
        this.labels = edited;
        this.locations = locations;
    }

    private static OwnLabels requireNamed(@Nullable RealName realName, OwnLabels labels) {
        if (realName == null && labels.isEmpty()) {
            throw new AuthorNamelessException();
        }
        return labels;
    }

    /**
     * Takes a label of nobody found out to be the person's, as their display name when the person attaching it says
     * so; only its works change their owner.
     */
    public void adopt(Label label, boolean display) {
        labels = labels.adopt(new OwnLabel(label.getId(), label.getName(), label.getAddedBy()), display);
        events.add(new OwnerOfLabelsChanged(Set.of(label.getId())));
        events.add(new AuthorChanged(id));
    }

    /** A new person replacing a label under the same name, located where the label was. */
    public static Author replacing(AuthorId id, Label label, AddedBy addedBy) {
        Author author = new Author(
                id,
                new RealName(label.getName().value()),
                null,
                OwnLabels.of(List.of(), addedBy),
                label.getLocations(),
                addedBy);
        author.events.add(new LabelReplacedByAuthor(label.getId(), id));
        author.events.add(new OwnerOfLabelsChanged(Set.of(label.getId())));
        author.events.add(new AuthorChanged(id));
        return author;
    }

    /**
     * Gives up a label that turned out to be somebody else's; only its works change their owner. The person keeps
     * their display name while other labels stay, and some name at all.
     */
    public void release(LabelId label) {
        labels = requireNamed(realName, labels.release(label));
        events.add(new OwnerOfLabelsChanged(Set.of(label)));
        events.add(new AuthorChanged(id));
    }

    /** Tells the person is going: its repository deletes it and leaves its labels to nobody. */
    public void delete() {
        events.add(labelsChanged());
        events.add(new AuthorChanged(id));
    }

    private OwnerOfLabelsChanged labelsChanged() {
        return new OwnerOfLabelsChanged(labels.all().stream().map(OwnLabel::id).collect(Collectors.toSet()));
    }

    /**
     * What happened to the person since it was loaded, handed over once.
     */
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }
}

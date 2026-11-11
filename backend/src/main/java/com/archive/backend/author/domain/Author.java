package com.archive.backend.author.domain;

import com.archive.backend.geo.domain.Locations;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.label.domain.OwnLabels;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * A person behind the labels, known by a real name, a label or both (docs/domain/authors.md).
 */
@Getter
public final class Author {

    private final AuthorId id;
    private @Nullable RealName realName;
    private @Nullable AuthorNotes notes;
    private OwnLabels labels;
    private Locations locations;

    public Author(
            AuthorId id,
            @Nullable RealName realName,
            @Nullable AuthorNotes notes,
            OwnLabels labels,
            Locations locations) {
        this.id = Objects.requireNonNull(id, "id");
        this.realName = realName;
        this.notes = notes;
        this.labels = requireNamed(realName, Objects.requireNonNull(labels, "labels"));
        this.locations = Objects.requireNonNull(locations, "locations");
    }

    public static Author create(
            AuthorId id,
            @Nullable RealName realName,
            @Nullable AuthorNotes notes,
            List<LabelDraft> labels,
            Locations locations) {
        return new Author(id, realName, notes, OwnLabels.of(labels), locations);
    }

    /**
     * Replaces everything; labels left out are dropped, not deleted.
     */
    public void edit(
            @Nullable RealName realName, @Nullable AuthorNotes notes, List<LabelDraft> labels, Locations locations) {
        OwnLabels edited = requireNamed(realName, this.labels.replace(labels));
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
}

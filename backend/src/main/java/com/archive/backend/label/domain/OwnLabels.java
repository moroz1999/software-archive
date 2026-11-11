package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

/**
 * The labels of an owner, exactly one of them its display name (docs/domain/authors.md). An owner keeps only labels
 * of its own: a new label gets a new id here, and an id of somebody else's label is refused. A label the owner drops
 * is not deleted: it stays in the archive as a label of nobody.
 */
@ValueObject
public final class OwnLabels {

    private final List<OwnLabel> all;
    private final @Nullable LabelId display;

    private OwnLabels(List<OwnLabel> all, @Nullable LabelId display) {
        this.all = List.copyOf(all);
        this.display = display;
    }

    /**
     * The labels of a new owner.
     */
    public static OwnLabels of(List<LabelDraft> drafts) {
        return new OwnLabels(List.of(), null).replace(drafts);
    }

    /**
     * Labels read back from storage: they were checked when stored.
     */
    public static OwnLabels restore(List<OwnLabel> labels, @Nullable LabelId display) {
        boolean marked =
                display != null && labels.stream().anyMatch(label -> label.id().equals(display));
        if (marked == labels.isEmpty()) {
            throw new IllegalArgumentException(
                    "The display name " + display + " is not one of the labels " + labels + ".");
        }
        return new OwnLabels(labels, display);
    }

    /**
     * The labels the drafts give: drafted labels of its own are renamed and marked, new ones are added, the rest are
     * dropped.
     */
    public OwnLabels replace(List<LabelDraft> drafts) {
        Map<LabelId, OwnLabel> own = all.stream().collect(Collectors.toMap(OwnLabel::id, Function.identity()));
        List<OwnLabel> labels = drafts.stream()
                .map(draft -> new OwnLabel(idOf(draft, own), draft.name()))
                .toList();
        if (!drafts.isEmpty() && drafts.stream().filter(LabelDraft::display).count() != 1) {
            throw new LabelDisplayNameException();
        }
        LabelId marked = null;
        for (int i = 0; i < drafts.size(); i++) {
            if (drafts.get(i).display()) {
                marked = labels.get(i).id();
            }
        }
        return new OwnLabels(labels, marked);
    }

    private static LabelId idOf(LabelDraft draft, Map<LabelId, OwnLabel> own) {
        LabelId id = draft.id();
        if (id == null) {
            return new LabelId(UUID.randomUUID());
        }
        if (!own.containsKey(id)) {
            throw new LabelNotOwnException();
        }
        return id;
    }

    public List<OwnLabel> all() {
        return all;
    }

    /**
     * The display name; nothing only while there are no labels.
     */
    public @Nullable LabelId display() {
        return display;
    }

    public boolean isEmpty() {
        return all.isEmpty();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof OwnLabels labels && labels.all.equals(all) && Objects.equals(labels.display, display);
    }

    @Override
    public int hashCode() {
        return Objects.hash(all, display);
    }

    @Override
    public String toString() {
        return "OwnLabels[" + all + ", display=" + display + "]";
    }
}

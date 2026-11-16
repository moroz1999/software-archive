package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

/**
 * The labels of a group: its alternative names, none of them a display name, as a group is always shown under its own
 * name (docs/domain/authors.md). It keeps only labels of its own: a new label gets a new id here, and an id of
 * somebody else's label is refused. A label it drops is not deleted: it stays in the archive as a label of nobody.
 */
@ValueObject
public final class UnmarkedLabels {

    private final List<OwnLabel> all;

    private UnmarkedLabels(List<OwnLabel> all) {
        this.all = List.copyOf(all);
    }

    /** The labels of a new group. */
    public static UnmarkedLabels of(List<UnmarkedLabelDraft> drafts) {
        return new UnmarkedLabels(List.of()).replace(drafts);
    }

    /** Labels read back from storage. */
    public static UnmarkedLabels restore(List<OwnLabel> labels) {
        return new UnmarkedLabels(labels);
    }

    /** The labels the drafts give: drafted labels of its own are renamed, new ones are added, the rest are dropped. */
    public UnmarkedLabels replace(List<UnmarkedLabelDraft> drafts) {
        Map<LabelId, OwnLabel> own = all.stream().collect(Collectors.toMap(OwnLabel::id, Function.identity()));
        return new UnmarkedLabels(drafts.stream()
                .map(draft -> new OwnLabel(idOf(draft, own), draft.name()))
                .toList());
    }

    private static LabelId idOf(UnmarkedLabelDraft draft, Map<LabelId, OwnLabel> own) {
        LabelId id = draft.id();
        if (id == null) {
            return new LabelId(UUID.randomUUID());
        }
        if (!own.containsKey(id)) {
            throw new LabelNotOwnException();
        }
        return id;
    }

    /** The labels with a label of nobody given to the group. */
    public UnmarkedLabels adopt(OwnLabel label) {
        List<OwnLabel> labels = new ArrayList<>(all);
        labels.add(label);
        return new UnmarkedLabels(labels);
    }

    public List<OwnLabel> all() {
        return all;
    }

    public boolean isEmpty() {
        return all.isEmpty();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof UnmarkedLabels labels && labels.all.equals(all);
    }

    @Override
    public int hashCode() {
        return all.hashCode();
    }

    @Override
    public String toString() {
        return "UnmarkedLabels" + all;
    }
}

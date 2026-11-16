package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** A label of a group as its form gives it: a new one has no id yet. */
@ValueObject
public record UnmarkedLabelDraft(@Nullable LabelId id, LabelName name) {

    public UnmarkedLabelDraft {
        Objects.requireNonNull(name, "name");
    }
}

package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.Objects;

/**
 * A label replaces another it is a duplicate of: that label's credits, memberships and remembered origin become its
 * own, joined with what it already has, then that label goes (docs/domain/authors.md#label).
 */
public record LabelMergedIntoLabel(LabelId label, LabelId into) implements DomainEvent {

    public LabelMergedIntoLabel {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(into, "into");
    }
}

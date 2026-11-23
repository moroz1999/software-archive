package com.archive.backend.group.domain;

import com.archive.backend.label.domain.LabelId;
import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.Objects;

/**
 * A group took a label found out to be its own: wherever it is credited by itself and under the label, the label's
 * credit takes in its own (docs/domain/authors.md#label).
 */
public record LabelAdoptedByGroup(LabelId label, GroupId group) implements DomainEvent {

    public LabelAdoptedByGroup {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(group, "group");
    }
}

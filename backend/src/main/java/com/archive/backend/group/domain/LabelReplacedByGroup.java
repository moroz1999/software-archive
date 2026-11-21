package com.archive.backend.group.domain;

import com.archive.backend.label.domain.LabelId;
import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.Objects;

/**
 * A new group replaces the label under its name: the label's credits, memberships and remembered origin become the
 * group's, then the label goes (docs/domain/authors.md#label).
 */
public record LabelReplacedByGroup(LabelId label, GroupId group) implements DomainEvent {

    public LabelReplacedByGroup {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(group, "group");
    }
}

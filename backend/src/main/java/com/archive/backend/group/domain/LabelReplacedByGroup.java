package com.archive.backend.group.domain;

import com.archive.backend.label.domain.LabelId;
import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.Objects;

/**
 * A group replaces the label: a new one under its name, or the group it is a duplicate of. The label's credits,
 * memberships and remembered origin become the group's, joined with what the group already has, then the label goes
 * (docs/domain/authors.md#label).
 */
public record LabelReplacedByGroup(LabelId label, GroupId group) implements DomainEvent {

    public LabelReplacedByGroup {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(group, "group");
    }
}

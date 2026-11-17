package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.UUID;

/**
 * Whose a label is to be, as its form says: the person or the group by its id, and for a person whether it becomes
 * their display name (docs/domain/authors.md#editing-a-label).
 */
@ValueObject
public record LabelOwnerChoice(LabelOwnerKind kind, UUID id, boolean display) {

    public LabelOwnerChoice {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(id, "id");
    }
}

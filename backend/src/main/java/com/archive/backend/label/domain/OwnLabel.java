package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.user.domain.AddedBy;
import java.util.Objects;

/**
 * A label of its owner, with whoever added it: they and administrators may change it on its own
 * (docs/domain/authors.md#label).
 */
@ValueObject
public record OwnLabel(LabelId id, LabelName name, AddedBy addedBy) {

    public OwnLabel {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(addedBy, "addedBy");
    }
}

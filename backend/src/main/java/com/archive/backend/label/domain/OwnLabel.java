package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/**
 * A label of its owner.
 */
@ValueObject
public record OwnLabel(LabelId id, LabelName name) {

    public OwnLabel {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
    }
}

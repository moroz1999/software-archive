package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/**
 * A label of nobody to attach to its owner, and whether the person attaching it makes it the owner's display name
 * (docs/domain/authors.md#label).
 */
@ValueObject
public record LabelAttachment(LabelId label, boolean display) {

    public LabelAttachment {
        Objects.requireNonNull(label, "label");
    }
}

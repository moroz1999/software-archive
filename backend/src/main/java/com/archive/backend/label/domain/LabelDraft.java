package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * A label as its owner's form gives it: a new one has no id yet; {@code display} marks the display name.
 */
@ValueObject
public record LabelDraft(@Nullable LabelId id, LabelName name, boolean display) {

    public LabelDraft {
        Objects.requireNonNull(name, "name");
    }
}
